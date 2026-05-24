/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dto.PromotionRequestDTO;
import com.cinema.exception.ConflictException;
import com.cinema.exception.NotFoundException;
import com.cinema.exception.ValidationException;
import com.cinema.model.Promotion;
import com.cinema.service.PromotionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author tuan6b
 */
public class PromotionServlet extends HttpServlet {

    private final PromotionService promotionService = new PromotionService();

    private static final int ROLE_MANAGER = 4;
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter FORM_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final String LIST_JSP =
            "/WEB-INF/manager/promotions/list.jsp";
    private static final String FORM_JSP =
            "/WEB-INF/manager/promotions/form.jsp";
    private static final String LIST_URL = "/manager/promotions";

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

        /*
        if (!checkAuthorization(request, response)) {
            return;
        }
        */

        String method = request.getMethod();
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        if ("GET".equalsIgnoreCase(method)) {
            // === GET LOGIC ===
            switch (action) {
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                default:
                    showList(request, response);
                    break;
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // === POST LOGIC ===
            switch (action) {
                case "create":
                    handleCreate(request, response);
                    break;
                case "update":
                    handleUpdate(request, response);
                    break;
                case "delete":
                    handleDelete(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + LIST_URL);
                    break;
            }
        }
    }

    /**
     * Show the promotion list with optional filters and pagination.
     */
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String type    = request.getParameter("type");
        String status  = request.getParameter("status");
        int page = parseIntParam(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        // Transfer flash messages from session to request then clear them
        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        try {
            List<Promotion> promotions = promotionService.findPromotions(
                    keyword, type, status, page, PAGE_SIZE);
            int totalItems = promotionService.countPromotions(keyword, type, status);
            int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);

            request.setAttribute("promotions", promotions);
            request.setAttribute("totalItems", totalItems);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPage", page);
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("filterType", type != null ? type : "");
            request.setAttribute("filterStatus", status != null ? status : "");
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "System error. Please try again.");
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        }
    }

    /**
     * Show the blank add form.
     */
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("formAction", "create");
        request.setAttribute("pageTitle", "Add New Promotion");
        request.getRequestDispatcher(FORM_JSP).forward(request, response);
    }

    /**
     * Show the edit form pre-filled with existing promotion data.
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("id"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }

        try {
            Promotion p = promotionService.getById(id);

            request.setAttribute("promotion", p);
            request.setAttribute("promotionId", id);

            // Pre-format LocalDateTime for datetime-local input
            if (p.getStartDate() != null) {
                request.setAttribute("startDateStr", p.getStartDate().format(FORM_FORMATTER));
            }
            if (p.getEndDate() != null) {
                request.setAttribute("endDateStr", p.getEndDate().format(FORM_FORMATTER));
            }

            request.setAttribute("formAction", "update");
            request.setAttribute("pageTitle", "Edit Promotion");
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
        } catch (NotFoundException e) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + LIST_URL);
        }
    }

    /**
     * Handle POST create: validate, insert, redirect on success or forward on error.
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PromotionRequestDTO dto = buildDtoFromRequest(request);

        try {
            promotionService.create(dto);
            request.getSession().setAttribute("flashSuccess", "Promotion created successfully.");
            response.sendRedirect(request.getContextPath() + LIST_URL);
        } catch (ValidationException e) {
            request.setAttribute("promotion", dto);
            request.setAttribute("errors", e.getErrors());
            request.setAttribute("formAction", "create");
            request.setAttribute("pageTitle", "Add New Promotion");
            request.setAttribute("startDateStr", nullToEmpty(dto.getStartDate()));
            request.setAttribute("endDateStr", nullToEmpty(dto.getEndDate()));
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("promotion", dto);
            request.setAttribute("errorMsg", "System error. Please try again.");
            request.setAttribute("formAction", "create");
            request.setAttribute("pageTitle", "Add New Promotion");
            request.setAttribute("startDateStr", nullToEmpty(dto.getStartDate()));
            request.setAttribute("endDateStr", nullToEmpty(dto.getEndDate()));
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
        }
    }

    /**
     * Handle POST update: validate, update, redirect on success or forward on error.
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }

        PromotionRequestDTO dto = buildDtoFromRequest(request);

        try {
            promotionService.update(id, dto);
            request.getSession().setAttribute("flashSuccess", "Promotion updated successfully.");
            response.sendRedirect(request.getContextPath() + LIST_URL);
        } catch (ValidationException e) {
            request.setAttribute("promotion", dto);
            request.setAttribute("promotionId", id);
            request.setAttribute("errors", e.getErrors());
            request.setAttribute("formAction", "update");
            request.setAttribute("pageTitle", "Edit Promotion");
            request.setAttribute("startDateStr", nullToEmpty(dto.getStartDate()));
            request.setAttribute("endDateStr", nullToEmpty(dto.getEndDate()));
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
        } catch (ConflictException e) {
            request.setAttribute("promotion", dto);
            request.setAttribute("promotionId", id);
            request.setAttribute("errorMsg", e.getMessage());
            request.setAttribute("formAction", "update");
            request.setAttribute("pageTitle", "Edit Promotion");
            request.setAttribute("startDateStr", nullToEmpty(dto.getStartDate()));
            request.setAttribute("endDateStr", nullToEmpty(dto.getEndDate()));
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
        } catch (NotFoundException e) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + LIST_URL);
        }
    }

    /**
     * Handle POST delete: soft-delete, always redirect back to list.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id > 0) {
            try {
                promotionService.delete(id);
                request.getSession().setAttribute("flashSuccess",
                        "Promotion deactivated successfully.");
            } catch (ConflictException e) {
                request.getSession().setAttribute("flashError", e.getMessage());
            } catch (NotFoundException e) {
                // Already gone — no action needed
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError",
                        "System error. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + LIST_URL);
    }

    /**
     * Build a PromotionRequestDTO from HTML form parameters.
     */
    private PromotionRequestDTO buildDtoFromRequest(HttpServletRequest request) {
        PromotionRequestDTO dto = new PromotionRequestDTO();
        dto.setPromotionCode(request.getParameter("promotionCode"));
        dto.setDescription(request.getParameter("description"));
        dto.setDiscountType(request.getParameter("discountType"));

        String discountValueStr = request.getParameter("discountValue");
        if (discountValueStr != null && !discountValueStr.trim().isEmpty()) {
            try {
                dto.setDiscountValue(new BigDecimal(discountValueStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        String minOrderStr = request.getParameter("minOrderAmount");
        if (minOrderStr != null && !minOrderStr.trim().isEmpty()) {
            try {
                dto.setMinOrderAmount(new BigDecimal(minOrderStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        String maxDiscountStr = request.getParameter("maxDiscountAmount");
        if (maxDiscountStr != null && !maxDiscountStr.trim().isEmpty()) {
            try {
                dto.setMaxDiscountAmount(new BigDecimal(maxDiscountStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        dto.setStartDate(request.getParameter("startDate"));
        dto.setEndDate(request.getParameter("endDate"));

        String usageLimitStr = request.getParameter("usageLimit");
        if (usageLimitStr != null && !usageLimitStr.trim().isEmpty()) {
            try {
                dto.setUsageLimit(Integer.parseInt(usageLimitStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        // Checkbox: "on" when checked, absent (null) when unchecked
        String isActiveStr = request.getParameter("isActive");
        dto.setIsActive("on".equals(isActiveStr) || "true".equals(isActiveStr));

        dto.setUsedCount(parseIntParam(request.getParameter("usedCount"), 0));

        return dto;
    }

    /**
     * Move a flash attribute from session to request and clear it from session.
     */
    private void transferFlash(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    /**
     * Return empty string if input is null, otherwise return the input.
     */
    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    /**
     * Parse an int from a String, returning defaultValue on failure or null.
     */
    private int parseIntParam(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Check session authorization for manager role.
     * Redirects to login if unauthorized and returns false.
     */
    /*
    private boolean checkAuthorization(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        int roleId = getRoleId(session.getAttribute("account"));
        if (roleId != ROLE_MANAGER) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }
    */

    /**
     * Extract roleId from account session object via reflection.
     */
    private int getRoleId(Object account) {
        try {
            java.lang.reflect.Method m = account.getClass().getMethod("getRoleId");
            Object result = m.invoke(account);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception e) {
            System.out.println("[PromotionServlet] Cannot extract roleId: " + e.getMessage());
        }
        return -1;
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
        return "Promotion Manager Servlet - UC43 Manage Promotion + UC44 View Promotion List";
    }// </editor-fold>

}
