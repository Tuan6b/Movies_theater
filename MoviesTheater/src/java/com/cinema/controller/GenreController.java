package com.cinema.controller;

import com.cinema.dao.GenreDAO;
import com.cinema.model.Account;
import com.cinema.model.Genre;
import java.util.List;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class GenreController extends HttpServlet {

    private boolean isNotManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return true;
        }
        Account account = (Account) session.getAttribute("account");
        if (account.getRoleId() < 4) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return true;
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isNotManager(request, response)) return;

        HttpSession session = request.getSession(false);
        if (session != null) {
            String success = (String) session.getAttribute("flashSuccess");
            String error = (String) session.getAttribute("flashError");
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("flashSuccess");
            }
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("flashError");
            }
        }

        GenreDAO dao = new GenreDAO();
        List<Genre> list = dao.getAllGenres();
        request.setAttribute("genreList", list);
        request.getRequestDispatcher("/view/manager/genre.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isNotManager(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        GenreDAO dao = new GenreDAO();
        HttpSession session = request.getSession();

        try {
            if ("add".equals(action)) {
                String genreName = request.getParameter("genreName");
                if (genreName == null || genreName.trim().isEmpty()) {
                    session.setAttribute("flashError", "Tên thể loại không được để trống!");
                } else if (dao.addGenre(genreName.trim())) {
                    session.setAttribute("flashSuccess", "Thể loại đã được thêm thành công!");
                } else {
                    session.setAttribute("flashError", "Thêm thể loại thất bại!");
                }

            } else if ("edit".equals(action)) {
                int genreID;
                try {
                    genreID = Integer.parseInt(request.getParameter("genreID"));
                } catch (NumberFormatException e) {
                    session.setAttribute("flashError", "ID thể loại không hợp lệ!");
                    response.sendRedirect(request.getContextPath() + "/manager/genre");
                    return;
                }
                String newName = request.getParameter("genreName");
                if (newName == null || newName.trim().isEmpty()) {
                    session.setAttribute("flashError", "Tên thể loại không được để trống!");
                } else if (dao.updateGenre(genreID, newName.trim())) {
                    session.setAttribute("flashSuccess", "Cập nhật thể loại thành công!");
                } else {
                    session.setAttribute("flashError", "Cập nhật thể loại thất bại!");
                }

            } else if ("delete".equals(action)) {
                int genreID;
                try {
                    genreID = Integer.parseInt(request.getParameter("genreID"));
                } catch (NumberFormatException e) {
                    session.setAttribute("flashError", "ID thể loại không hợp lệ!");
                    response.sendRedirect(request.getContextPath() + "/manager/genre");
                    return;
                }
                if (dao.deleteGenre(genreID)) {
                    session.setAttribute("flashSuccess", "Thể loại đã được xoá thành công!");
                } else {
                    session.setAttribute("flashError", "Xoá thể loại thất bại!");
                }
            }
        } catch (Exception e) {
            session.setAttribute("flashError", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/manager/genre");
    }
}
