/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.BookingScheduleDAO;
import com.cinema.dao.BookingSeatDAO;
import com.cinema.model.BookingCart;
import com.cinema.model.BookingScheduleView;
import com.cinema.model.SeatView;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingController handles movie booking steps (seat selection).
 *
 * @author tuan6b
 */
@WebServlet(name = "BookingController", urlPatterns = {"/booking"})
public class BookingController extends HttpServlet {

    private final BookingSeatDAO seatDAO = new BookingSeatDAO();
    private final BookingScheduleDAO scheduleDAO = new BookingScheduleDAO();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String method = request.getMethod();
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        if ("GET".equalsIgnoreCase(method)) {
            // GET Action routing
            switch (action) {
                case "seat":
                    showSeatPage(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/index.jsp");
                    break;
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // POST Action routing
            if ("selectSeat".equals(action)) {
                selectSeat(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        }
    }

    private void showSeatPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String scheduleIdRaw = request.getParameter("scheduleId");

        if (scheduleIdRaw == null || scheduleIdRaw.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes.jsp");
            return;
        }

        try {
            int scheduleId = Integer.parseInt(scheduleIdRaw);
            BookingScheduleView schedule = scheduleDAO.getScheduleById(scheduleId);

            if (schedule == null) {
                request.setAttribute("error", "Không tìm thấy suất chiếu.");
                request.getRequestDispatcher("Error.jsp").forward(request, response);
                return;
            }

            List<SeatView> seats = seatDAO.getSeatsByScheduleId(scheduleId);

            request.setAttribute("schedule", schedule);
            request.setAttribute("seats", seats);

            request.getRequestDispatcher("seat_selection.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/showtimes.jsp");
        }
    }

    private void selectSeat(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String scheduleIdRaw = request.getParameter("scheduleId");
        String[] seatIdValues = request.getParameterValues("seatIds");

        if (scheduleIdRaw == null || seatIdValues == null || seatIdValues.length == 0) {
            request.setAttribute("error", "Vui lòng chọn ít nhất một ghế.");
            showSeatPage(request, response);
            return;
        }

        try {
            int scheduleId = Integer.parseInt(scheduleIdRaw);
            BookingScheduleView schedule = scheduleDAO.getScheduleById(scheduleId);

            if (schedule == null) {
                request.setAttribute("error", "Không tìm thấy suất chiếu.");
                request.getRequestDispatcher("Error.jsp").forward(request, response);
                return;
            }

            List<Integer> seatIds = new ArrayList<>();
            List<String> seatNames = new ArrayList<>();

            for (String seatIdRaw : seatIdValues) {
                int seatId = Integer.parseInt(seatIdRaw);

                if (seatDAO.isSeatBooked(scheduleId, seatId)) {
                    request.setAttribute("error", "Ghế bạn chọn vừa có người khác đặt. Vui lòng chọn lại.");
                    showSeatPage(request, response);
                    return;
                }

                seatIds.add(seatId);
                seatNames.add(seatDAO.getSeatNameById(seatId));
            }

            double ticketTotal = schedule.getBaseTicketPrice() * seatIds.size();

            BookingCart cart = new BookingCart();
            cart.setScheduleId(scheduleId);
            cart.setSeatIds(seatIds);
            cart.setSeatNames(seatNames);
            cart.setTicketTotal(ticketTotal);

            HttpSession session = request.getSession();
            session.setAttribute("bookingCart", cart);
            session.setAttribute("bookingSchedule", schedule);

            response.sendRedirect(request.getContextPath() + "/food_selection.jsp");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/showtimes.jsp");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Booking Controller - Handles seat selection process";
    }
    // </editor-fold>
}
