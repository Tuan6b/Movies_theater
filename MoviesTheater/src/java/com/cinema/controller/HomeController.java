package com.cinema.controller;

import com.cinema.dao.tbMovie;
import com.cinema.model.clsMovie;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.cinema.dao.GenreDAO;
import com.cinema.model.Genre;

public class HomeController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        tbMovie movieDAO = new tbMovie();
        GenreDAO genreDAO = new GenreDAO();
        
        // 1. Lấy Thể loại (Genre) từ URL và Lấy danh sách Thể loại cho Dropdown
        String genreShowing = request.getParameter("genreShowing");
        String genreUpcoming = request.getParameter("genreUpcoming");
        
        List<Genre> genreList = genreDAO.getAllGenres();
        request.setAttribute("genreList", genreList);
        
        // 2. Đặt Paging là 12 phim 1 trang
        int pageSize = 12;
        
        // update: Fetch showing and upcoming lists simultaneously for UC-11 UI
        // ================= XỬ LÝ PHIM ĐANG CHIẾU =================
        int pageShowing = 1; 
        if (request.getParameter("pageShowing") != null) {
            try { pageShowing = Integer.parseInt(request.getParameter("pageShowing")); } catch (Exception e) {}
        }
        int totalShowing = movieDAO.getTotalPublicMovies("showing", genreShowing);
        int totalPagesShowing = (int) Math.ceil((double) totalShowing / pageSize);
        List<clsMovie> showingList = movieDAO.getPublicMoviesByPage("showing", (pageShowing - 1) * pageSize, pageSize, genreShowing);
        
        // ================= XỬ LÝ PHIM SẮP CHIẾU =================
        int pageUpcoming = 1;
        if (request.getParameter("pageUpcoming") != null) {
            try { pageUpcoming = Integer.parseInt(request.getParameter("pageUpcoming")); } catch (Exception e) {}
        }
        int totalUpcoming = movieDAO.getTotalPublicMovies("upcoming", genreUpcoming);
        int totalPagesUpcoming = (int) Math.ceil((double) totalUpcoming / pageSize);
        List<clsMovie> upcomingList = movieDAO.getPublicMoviesByPage("upcoming", (pageUpcoming - 1) * pageSize, pageSize, genreUpcoming);
        // end update code
        
        // 3. Đẩy toàn bộ ra View
        request.setAttribute("showingList", showingList);
        request.setAttribute("upcomingList", upcomingList);
        
        request.setAttribute("pageShowing", pageShowing);
        request.setAttribute("totalPagesShowing", totalPagesShowing);
        
        request.setAttribute("pageUpcoming", pageUpcoming);
        request.setAttribute("totalPagesUpcoming", totalPagesUpcoming);
        
        request.setAttribute("genreShowing", genreShowing);
        request.setAttribute("genreUpcoming", genreUpcoming);
        
        request.getRequestDispatcher("/home.jsp").forward(request, response);
    }
}