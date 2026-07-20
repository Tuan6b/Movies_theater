/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.BookingScheduleDAO;
import com.cinema.dao.BookingSeatDAO;
import com.cinema.dao.FoodDAO;
import com.cinema.dao.PromotionDAO;
import com.cinema.model.Account;
import com.cinema.model.BookingCart;
import com.cinema.model.BookingScheduleView;
import com.cinema.model.Food;
import com.cinema.model.Promotion;
import com.cinema.model.SeatView;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
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
                case "searchPromotions":
                    searchPromotions(request, response);
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
            } else if ("confirmPayment".equals(action)) {
                confirmPayment(request, response);
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
                request.getRequestDispatcher("/view/common/Error.jsp").forward(request, response);
                return;
            }

            List<SeatView> seats = seatDAO.getSeatsByScheduleId(scheduleId);

            request.setAttribute("schedule", schedule);
            request.setAttribute("seats", seats);

            request.getRequestDispatcher("/view/customer/seat_selection.jsp").forward(request, response);

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
                request.getRequestDispatcher("/view/common/Error.jsp").forward(request, response);
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

        // Collect selected food IDs from request
        List<Integer> selectedIds = new ArrayList<>();
        Map<Integer, Integer> rawQuantities = new HashMap<>();
        for (String paramName : request.getParameterMap().keySet()) {
            if (paramName.startsWith("qty_")) {
                try {
                    int foodId = Integer.parseInt(paramName.substring(4));
                    String qtyRaw = request.getParameter(paramName);
                    int qty = Integer.parseInt(qtyRaw);
                    if (qty > 0) {
                        selectedIds.add(foodId);
                        rawQuantities.put(foodId, qty);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Validate against database: only accept active items with correct prices
        Map<Integer, Food> validFoodMap = foodDAO.getFoodMapByIds(selectedIds);
        Map<Integer, Integer> foodQuantities = new HashMap<>();
        double foodTotal = 0;

        for (Map.Entry<Integer, Integer> entry : rawQuantities.entrySet()) {
            int foodId = entry.getKey();
            int qty = entry.getValue();
            Food food = validFoodMap.get(foodId);
            if (food != null && food.isIsActive()) {
                foodQuantities.put(foodId, qty);
                foodTotal += food.getPrice() * qty;
            }
        }

        cart.setFoodQuantities(foodQuantities);
        cart.setFoodTotal(foodTotal);
        // Reset promotion when food changes
        cart.setAppliedPromotionId(null);
        cart.setAppliedPromotionCode(null);
        cart.setDiscountAmount(0);
        cart.setFinalTotal(0);

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

        List<Food> foodList = foodDAO.getAllFoods();
        List<Promotion> promotions = promotionDAO.getActivePromotions();
        double subtotal = cart.getGrandTotal();

        // Determine best suggested promotion for the customer
        Integer suggestedPromotionId = null;
        double bestDiscount = 0;
        if (promotions != null) {
            for (Promotion p : promotions) {
                if (p.getMinOrderAmount() != null && subtotal < p.getMinOrderAmount().doubleValue()) continue;
                double discount;
                if ("Percentage".equals(p.getDiscountType())) {
                    discount = subtotal * p.getDiscountValue().doubleValue() / 100;
                    if (p.getMaxDiscountAmount() != null && discount > p.getMaxDiscountAmount().doubleValue()) {
                        discount = p.getMaxDiscountAmount().doubleValue();
                    }
                } else {
                    discount = p.getDiscountValue().doubleValue();
                }
                if (discount > bestDiscount) {
                    bestDiscount = discount;
                    suggestedPromotionId = p.getPromotionId();
                }
            }
        }

        request.setAttribute("foodList", foodList);
        request.setAttribute("promotions", promotions);
        request.setAttribute("suggestedPromotionId", suggestedPromotionId);
        request.getRequestDispatcher("/view/customer/checkout.jsp").forward(request, response);
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

        List<Food> comboList = foodDAO.getActiveCombos();
        List<Food> itemList = foodDAO.getActiveIndividualItems();
        request.setAttribute("comboList", comboList);
        request.setAttribute("itemList", itemList);
        request.getRequestDispatcher("/view/customer/food_selection.jsp").forward(request, response);
    }

    private void searchPromotions(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String keyword = request.getParameter("q");
        if (keyword == null || keyword.trim().isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("[]");
            return;
        }

        List<Promotion> results = promotionDAO.searchActivePromotions(keyword);
        List<Map<String, Object>> jsonList = new ArrayList<>();
        for (Promotion p : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getPromotionId());
            item.put("code", p.getPromotionCode());
            item.put("type", p.getDiscountType());
            item.put("value", p.getDiscountValue());
            item.put("minOrder", p.getMinOrderAmount() != null ? p.getMinOrderAmount() : BigDecimal.ZERO);
            item.put("maxDiscount", p.getMaxDiscountAmount());
            item.put("endDate", p.getEndDateDisplay());
            jsonList.add(item);
        }

        String json = new Gson().toJson(jsonList);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }

    private void confirmPayment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
        BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");
        Account account = (Account) session.getAttribute("account");

        if (cart == null || schedule == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        if (account == null) {
            session.setAttribute("redirectAfterLogin",
                    request.getContextPath() + "/booking?action=checkout");
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        // Compute per-seat prices (same logic as selectSeat)
        Map<Integer, Double> seatPrices = new HashMap<>();
        for (int seatId : cart.getSeatIds()) {
            String seatType = seatDAO.getSeatTypeById(seatId);
            double seatPrice = "VIP".equalsIgnoreCase(seatType)
                ? schedule.getBaseTicketPrice() + 10000
                : schedule.getBaseTicketPrice();
            seatPrices.put(seatId, seatPrice);
        }
        cart.setSeatPrices(seatPrices);

        // Validate discount code on backend
        String promoIdRaw = request.getParameter("promotionId");
        if (promoIdRaw != null && !promoIdRaw.trim().isEmpty()) {
            try {
                int promoId = Integer.parseInt(promoIdRaw);
                Promotion promo = promotionDAO.findById(promoId);
                double subtotal = cart.getGrandTotal();

                if (promo == null || !promo.isActive() || !"active".equals(promo.getStatus())) {
                    request.setAttribute("error", "Mã khuyến mãi không hợp lệ hoặc đã hết hạn.");
                    showCheckoutPage(request, response);
                    return;
                }

                if (promo.getEndDate() != null && promo.getEndDate().isBefore(java.time.LocalDateTime.now())) {
                    request.setAttribute("error", "Mã khuyến mãi đã hết hạn.");
                    showCheckoutPage(request, response);
                    return;
                }

                if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
                    request.setAttribute("error", "Mã khuyến mãi đã hết lượt sử dụng.");
                    showCheckoutPage(request, response);
                    return;
                }

                if (promo.getMinOrderAmount() != null && subtotal < promo.getMinOrderAmount().doubleValue()) {
                    request.setAttribute("error", "Đơn hàng chưa đạt giá trị tối thiểu " + String.format("%,.0f", promo.getMinOrderAmount()) + " đ.");
                    showCheckoutPage(request, response);
                    return;
                }

                double discount;
                if ("Percentage".equals(promo.getDiscountType())) {
                    discount = subtotal * promo.getDiscountValue().doubleValue() / 100;
                    if (promo.getMaxDiscountAmount() != null && discount > promo.getMaxDiscountAmount().doubleValue()) {
                        discount = promo.getMaxDiscountAmount().doubleValue();
                    }
                } else {
                    discount = promo.getDiscountValue().doubleValue();
                }

                cart.setAppliedPromotionId(promoId);
                cart.setAppliedPromotionCode(promo.getPromotionCode());
                cart.setDiscountAmount(discount);
                cart.setFinalTotal(subtotal - discount);
            } catch (Exception e) {
                request.setAttribute("error", "Mã khuyến mãi không hợp lệ.");
                showCheckoutPage(request, response);
                return;
            }
        } else {
            cart.setAppliedPromotionId(null);
            cart.setAppliedPromotionCode(null);
            cart.setDiscountAmount(0);
            cart.setFinalTotal(cart.getGrandTotal());
        }

        session.setAttribute("bookingCart", cart);

        // Redirect to VNPAY payment creation
        response.sendRedirect(request.getContextPath() + "/vnpay?action=create");
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
