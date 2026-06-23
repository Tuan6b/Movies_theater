/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.BookingScheduleDAO;
import com.cinema.dao.BookingSeatDAO;
import com.cinema.dao.FoodDAO;
import com.cinema.dao.PromotionDAO;
import com.cinema.model.BookingCart;
import com.cinema.model.BookingScheduleView;
import com.cinema.model.Food;
import com.cinema.model.Promotion;
import com.cinema.model.SeatView;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookingController handles movie booking steps (seat selection).
 *
 * @author TBinh
 */
@WebServlet(name = "BookingController", urlPatterns = {"/booking"})
public class BookingController extends HttpServlet {

    private final BookingSeatDAO seatDAO = new BookingSeatDAO();
    private final BookingScheduleDAO scheduleDAO = new BookingScheduleDAO();
    private final FoodDAO foodDAO = new FoodDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

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
                case "food":
                    showFoodPage(request, response);
                    break;
                case "checkout":
                    showCheckoutPage(request, response);
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
            } else if ("selectFood".equals(action)) {
                selectFood(request, response);
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

            if (seatIdValues.length > 8) {
                request.setAttribute("error", "Bạn chỉ được chọn tối đa 8 ghế cho mỗi lần đặt vé.");
                showSeatPage(request, response);
                return;
            }

            List<Integer> seatIds = new ArrayList<>();
            List<String> seatNames = new ArrayList<>();
            double ticketTotal = 0;

            for (String seatIdRaw : seatIdValues) {
                int seatId = Integer.parseInt(seatIdRaw);

                if (seatDAO.isSeatBooked(scheduleId, seatId)) {
                    request.setAttribute("error", "Ghế bạn chọn vừa có người khác đặt. Vui lòng chọn lại.");
                    showSeatPage(request, response);
                    return;
                }

                seatIds.add(seatId);
                seatNames.add(seatDAO.getSeatNameById(seatId));

                String seatType = seatDAO.getSeatTypeById(seatId);
                double seatPrice = "VIP".equalsIgnoreCase(seatType)
                    ? schedule.getBaseTicketPrice() + 10000
                    : schedule.getBaseTicketPrice();
                ticketTotal += seatPrice;
            }

            BookingCart cart = new BookingCart();
            cart.setScheduleId(scheduleId);
            cart.setSeatIds(seatIds);
            cart.setSeatNames(seatNames);
            cart.setTicketTotal(ticketTotal);

            HttpSession session = request.getSession();
            session.setAttribute("bookingCart", cart);
            session.setAttribute("bookingSchedule", schedule);

            response.sendRedirect(request.getContextPath() + "/booking?action=food");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/showtimes.jsp");
        }
    }

    private void selectFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
        BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");

        if (cart == null || schedule == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        List<Food> foodList = foodDAO.getAllActiveFoods();
        Map<Integer, Integer> foodQuantities = new HashMap<>();
        double foodTotal = 0;

        for (Food food : foodList) {
            String paramName = "qty_" + food.getFoodId();
            String qtyRaw = request.getParameter(paramName);
            if (qtyRaw != null) {
                try {
                    int qty = Integer.parseInt(qtyRaw);
                    if (qty > 0) {
                        foodQuantities.put(food.getFoodId(), qty);
                        foodTotal += food.getPrice() * qty;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        cart.setFoodQuantities(foodQuantities);
        cart.setFoodTotal(foodTotal);

        session.setAttribute("bookingCart", cart);
        response.sendRedirect(request.getContextPath() + "/booking?action=checkout");
    }

    private void showCheckoutPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
        BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");

        if (cart == null || schedule == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        List<Food> foodList = foodDAO.getAllActiveFoods();
        List<Promotion> promotions = promotionDAO.getActivePromotions();

        request.setAttribute("foodList", foodList);
        request.setAttribute("promotions", promotions);
        request.getRequestDispatcher("checkout.jsp").forward(request, response);
    }

    private void showFoodPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
        BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");

        if (cart == null || schedule == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        List<Food> foodList = foodDAO.getAllActiveFoods();
        request.setAttribute("foodList", foodList);
        request.getRequestDispatcher("food_selection.jsp").forward(request, response);
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
