package com.cinema.controller;

import com.cinema.dao.NotificationDAO;
import com.cinema.model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("count".equals(action)) {
            int count = notificationDAO.countUnread();
            response.setContentType("application/json");
            response.getWriter().print("{\"count\":" + count + "}");
            return;
        }

        if ("list".equals(action)) {
            List<Notification> list = notificationDAO.getUnreadNotifications(20);
            response.setContentType("application/json; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("[");
            for (int i = 0; i < list.size(); i++) {
                Notification n = list.get(i);
                if (i > 0) out.print(",");
                out.print("{");
                out.print("\"id\":" + n.getNotificationId() + ",");
                out.print("\"type\":\"" + jsonEscape(n.getType()) + "\",");
                out.print("\"message\":\"" + jsonEscape(n.getMessage()) + "\",");
                out.print("\"link\":\"" + jsonEscape(n.getLink()) + "\",");
                out.print("\"createdAt\":\"" + (n.getCreatedAt() != null ? n.getCreatedAt().toString() : "") + "\"");
                out.print("}");
            }
            out.print("]");
            return;
        }

        if ("markRead".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    notificationDAO.markAsRead(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            response.setContentType("application/json");
            response.getWriter().print("{\"ok\":true}");
            return;
        }

        if ("markAllRead".equals(action)) {
            notificationDAO.markAllAsRead();
            response.setContentType("application/json");
            response.getWriter().print("{\"ok\":true}");
            return;
        }

        // SSE endpoint for real-time push
        response.setContentType("text/event-stream; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        PrintWriter out = response.getWriter();

        // Send initial count
        int count = notificationDAO.countUnread();
        out.print("data: " + count + "\n\n");
        out.flush();

        // Poll DB every 5 seconds
        for (int i = 0; i < 120; i++) { // 10 minutes max
            try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
            int newCount = notificationDAO.countUnread();
            if (newCount != count) {
                count = newCount;
                out.print("data: " + count + "\n\n");
                out.flush();
            }
        }
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
