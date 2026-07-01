package com.cinema.controller;

import com.cinema.util.TMDBService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "TMDBController", urlPatterns = {"/TMDBController"})
public class TMDBController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Thiết lập trả về định dạng JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Nhận tên phim do giao diện truyền lên
        String movieName = request.getParameter("query");
        if (movieName == null || movieName.trim().isEmpty()) {
            response.getWriter().write("{\"error\": \"Vui lòng nhập tên phim\"}");
            return;
        }

        // Gọi Service ở Bước 1
        Map<String, String> data = TMDBService.fetchMovieData(movieName);
        
        if (data != null) {
            // Chuyển Map kết quả thành chuỗi JSON và gửi lại cho giao diện
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(data));
        } else {
            response.getWriter().write("{\"error\": \"Không tìm thấy phim này\"}");
        }
    }
}