package com.cinema.controller;

import com.cinema.dao.PromotionDAO;
import com.cinema.exception.ConflictException;
import com.cinema.exception.NotFoundException;
import com.cinema.exception.ValidationException;
import com.cinema.model.Promotion;
import com.cinema.util.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PromotionServlet extends HttpServlet {

    private final PromotionDAO promotionDAO = new PromotionDAO();

    private static final int ROLE_MANAGER = 4;
    private static final int PAGE_SIZE = 5;
    private static final DateTimeFormatter FORM_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final String LIST_JSP = "/view/manager/promotions/list.jsp";
    private static final String FORM_JSP = "/view/manager/promotions/form.jsp";
    private static final String LIST_URL     = "/manager/promotions";
    private static final String UPCOMING_JSP = "/view/manager/promotions/upcoming.jsp";
    private static final String ACTIVE_JSP   = "/view/manager/promotions/active.jsp";
    private static final String EXPIRED_JSP  = "/view/manager/promotions/expired.jsp";
    private static final String INACTIVE_JSP = "/view/manager/promotions/inactive.jsp";
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9\\-_]+$");

    // ========== INNER DTO CLASSES ==========

    public static class PromotionRequestDTO { 
        private String promotionCode;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private String startDate;
        private String endDate;
        private Integer usageLimit;
        private Boolean isActive;
        private int usedCount;

        public PromotionRequestDTO() {}

        public String getPromotionCode() { return promotionCode; }
        public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDiscountType() { return discountType; }
        public void setDiscountType(String discountType) { this.discountType = discountType; }
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        public BigDecimal getMinOrderAmount() { return minOrderAmount; }
        public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
        public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
        public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        public Boolean getIsActive() { return isActive; }
        public boolean isActive() { return Boolean.TRUE.equals(isActive); }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public int getUsedCount() { return usedCount; }
        public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    }

    public static class PromotionResponseDTO {
        private int promotionId;
        private String promotionCode;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private String startDate;
        private String endDate;
        private Integer usageLimit;
        private int usedCount;
        private boolean isActive;
        private String status;

        public PromotionResponseDTO() {}

        public static PromotionResponseDTO fromEntity(Promotion p) {
            PromotionResponseDTO dto = new PromotionResponseDTO();
            dto.setPromotionId(p.getPromotionId());
            dto.setPromotionCode(p.getPromotionCode());
            dto.setDescription(p.getDescription());
            dto.setDiscountType(p.getDiscountType());
            dto.setDiscountValue(p.getDiscountValue());
            dto.setMinOrderAmount(p.getMinOrderAmount());
            dto.setMaxDiscountAmount(p.getMaxDiscountAmount());
            dto.setStartDate(p.getStartDate() != null ? p.getStartDate().toString() : null);
            dto.setEndDate(p.getEndDate() != null ? p.getEndDate().toString() : null);
            dto.setUsageLimit(p.getUsageLimit());
            dto.setUsedCount(p.getUsedCount());
            dto.setIsActive(p.isActive());
            dto.setStatus(computeStatus(p));
            return dto;
        }

        private static String computeStatus(Promotion p) {
            return p.getStatus();
        }

        public int getPromotionId() { return promotionId; }
        public void setPromotionId(int promotionId) { this.promotionId = promotionId; }
        public String getPromotionCode() { return promotionCode; }
        public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDiscountType() { return discountType; }
        public void setDiscountType(String discountType) { this.discountType = discountType; }
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        public BigDecimal getMinOrderAmount() { return minOrderAmount; }
        public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
        public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
        public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        public int getUsedCount() { return usedCount; }
        public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
        public boolean isIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // ========== SERVLET DISPATCH ==========

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
            switch (action) {
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "generateCode":
                    handleGenerateCode(request, response);
                    break;
                default: {
                    String view = request.getParameter("view");
                    if (view == null) {
                        view = "";
                    }
                    switch (view) {
                        case "upcoming": showStatusList(request, response, "upcoming", UPCOMING_JSP); break;
                        case "active":   showStatusList(request, response, "active",   ACTIVE_JSP);   break;
                        case "expired":  showStatusList(request, response, "expired",  EXPIRED_JSP);  break;
                        case "inactive": showStatusList(request, response, "inactive", INACTIVE_JSP); break;
                        default:
                            response.sendRedirect(request.getContextPath() + LIST_URL + "?view=active");
                            break;
                    }
                    break;
                }
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
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
                case "deactivate":
                    handleDeactivate(request, response);
                    break;
                case "reactivate":
                    handleReactivate(request, response);
                    break;
                case "activateEarly":
                    handleActivateEarly(request, response);
                    break;
                case "extend":
                    handleExtend(request, response);
                    break;
                case "hardDelete":
                    handleHardDelete(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + LIST_URL);
                    break;
            }
        }
    }

    public String generateNextCode() throws SQLException {
        String prefix = "KM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String sql = "SELECT COUNT(*) FROM Promotion WHERE PromotionCode LIKE ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.format("%s%03d", prefix, rs.getInt(1) + 1);
                }
            }
        }
        return prefix + "001";
    }

    // ========== CONTROLLER HANDLERS ==========

    private void handleGenerateCode(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String code = promotionDAO.generateNextCode();
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"" + code + "\"}");
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Kh\\u00f4ng th\\u1ec3 t\\u1ea1o m\\u00e3 t\\u1ef1 \\u0111\\u1ed9ng\"}");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String type    = request.getParameter("type");
        String status  = request.getParameter("status");
        String sortBy  = request.getParameter("sort");
        String sortDir = request.getParameter("dir");
        int page = parseIntParam(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        try {
            List<Promotion> promotions = findPromotions(keyword, type, status, page, PAGE_SIZE, sortBy, sortDir);
            int totalItems = countPromotions(keyword, type, status);
            int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);

            request.setAttribute("promotions",    promotions);
            request.setAttribute("totalItems",    totalItems);
            request.setAttribute("totalPages",    totalPages);
            request.setAttribute("currentPage",   page);
            request.setAttribute("keyword",       keyword != null ? keyword : "");
            request.setAttribute("filterType",    type    != null ? type    : "");
            request.setAttribute("filterStatus",  status  != null ? status  : "");
            request.setAttribute("sortBy",        sortBy  != null ? sortBy  : "");
            request.setAttribute("sortDir",       sortDir != null ? sortDir : "DESC");
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "System error. Please try again.");
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        }
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("formAction", "create");
        request.setAttribute("pageTitle", "Add New Promotion");
        request.getRequestDispatcher(FORM_JSP).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("id"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }

        try {
            Promotion p = getById(id);
            request.setAttribute("promotion", p);
            request.setAttribute("promotionId", id);
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

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PromotionRequestDTO dto = buildDtoFromRequest(request);
        try {
            String newStatus = create(dto);
            request.getSession().setAttribute("flashSuccess", "Promotion created successfully.");
            response.sendRedirect(request.getContextPath() + LIST_URL + "?view=" + newStatus);
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

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }
        PromotionRequestDTO dto = buildDtoFromRequest(request);
        try {
            String newStatus = update(id, dto);
            request.getSession().setAttribute("flashSuccess", "Promotion updated successfully.");
            response.sendRedirect(request.getContextPath() + LIST_URL + "?view=" + newStatus);
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

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id > 0) {
            try {
                delete(id);
                request.getSession().setAttribute("flashSuccess",
                        "Promotion deactivated successfully.");
            } catch (ConflictException e) {
                request.getSession().setAttribute("flashError", e.getMessage());
            } catch (NotFoundException e) {
                // Already gone — no action needed
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + LIST_URL);
    }

    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id > 0) {
            try {
                promotionDAO.updateStatus(id, "inactive");
                request.getSession().setAttribute("flashSuccess", "Promotion deactivated.");
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + LIST_URL + "?view=active");
    }

    private void handleReactivate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id > 0) {
            try {
                Promotion p = promotionDAO.findById(id);
                String status = (p != null) ? p.getStatus() : null;
                if (ERROR_REACTIVATE_EXPIRED.equals(decidePromotionAction("reactivate", status, false, 0, null))) {
                    request.getSession().setAttribute("flashError",
                            "Cannot reactivate an expired promotion. Use Extend instead.");
                } else {
                    promotionDAO.updateStatus(id, "active");
                    request.getSession().setAttribute("flashSuccess", "Promotion reactivated.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + LIST_URL + "?view=inactive");
    }

    static final String ERROR_REACTIVATE_EXPIRED = "ERROR_REACTIVATE_EXPIRED";
    static final String SUCCESS_REACTIVATED = "SUCCESS_REACTIVATED";
    static final String ERROR_DELETE_PAID_INVOICE = "ERROR_DELETE_PAID_INVOICE";
    static final String ERROR_DELETE_USED_UPCOMING = "ERROR_DELETE_USED_UPCOMING";
    static final String SUCCESS_DELETED = "SUCCESS_DELETED";

    /**
     * Package-private, extracted from handleReactivate/handleHardDelete for
     * testability. Mirrors the Decision Table in
     * doc/Huong_dan_Decision_Table_Testing.docx section 5.
     *
     * @param actionType "reactivate" or "hardDelete"
     * @param currentStatus current Promotion.status, only relevant for "reactivate"
     * @param hasInvoicePaid whether the promotion is referenced by a paid invoice, only relevant for "hardDelete"
     * @param usedCount Promotion.usedCount, only relevant for "hardDelete"
     * @param returnTo the tab the hard-delete action was invoked from, only relevant for "hardDelete"
     */
    static String decidePromotionAction(String actionType, String currentStatus,
            boolean hasInvoicePaid, int usedCount, String returnTo) {
        if ("reactivate".equals(actionType)) {
            return "expired".equals(currentStatus) ? ERROR_REACTIVATE_EXPIRED : SUCCESS_REACTIVATED;
        }
        if (hasInvoicePaid) {
            return ERROR_DELETE_PAID_INVOICE;
        }
        if (usedCount > 0 && "upcoming".equals(returnTo)) {
            return ERROR_DELETE_USED_UPCOMING;
        }
        return SUCCESS_DELETED;
    }

    private void handleActivateEarly(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        if (id > 0) {
            try {
                promotionDAO.activateEarly(id);
                request.getSession().setAttribute("flashSuccess",
                        "Promotion activated early.");
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + LIST_URL + "?view=upcoming");
    }

    private void handleExtend(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        String newEndDateStr = request.getParameter("newEndDate");
        if (id > 0 && newEndDateStr != null && !newEndDateStr.trim().isEmpty()) {
            try {
                LocalDateTime newEndDate = parseDateTime(newEndDateStr);
                if (newEndDate == null || !newEndDate.isAfter(LocalDateTime.now())) {
                    request.getSession().setAttribute("flashError",
                            "New end date must be in the future.");
                } else {
                    promotionDAO.extendEndDate(id, newEndDate);
                    request.getSession().setAttribute("flashSuccess",
                            "Promotion extended and reactivated.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        } else {
            request.getSession().setAttribute("flashError", "Invalid request.");
        }
        response.sendRedirect(request.getContextPath() + LIST_URL + "?view=expired");
    }

    private void handleHardDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("promotionId"), 0);
        String returnTo = request.getParameter("returnTo");
        if (returnTo == null || returnTo.trim().isEmpty()) {
            returnTo = "";
        }
        if (id > 0) {
            try {
                boolean hasInvoicePaid = promotionDAO.hasInvoicePaid(id);
                int usedCount = 0;
                if (!hasInvoicePaid) {
                    Promotion p = promotionDAO.findById(id);
                    usedCount = (p != null) ? p.getUsedCount() : 0;
                }
                String outcome = decidePromotionAction("hardDelete", null, hasInvoicePaid, usedCount, returnTo);
                if (ERROR_DELETE_PAID_INVOICE.equals(outcome)) {
                    request.getSession().setAttribute("flashError",
                            "Cannot delete: promotion is referenced by paid invoices.");
                } else if (ERROR_DELETE_USED_UPCOMING.equals(outcome)) {
                    request.getSession().setAttribute("flashError",
                            "Cannot cancel a promotion that has already been used.");
                } else {
                    promotionDAO.hardDelete(id);
                    request.getSession().setAttribute("flashSuccess",
                            "Promotion permanently deleted.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        }
        String redirectUrl = LIST_URL + (returnTo.isEmpty() ? "" : "?view=" + returnTo);
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    private void showStatusList(HttpServletRequest request, HttpServletResponse response,
            String status, String jspPath) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String type    = request.getParameter("type");
        String sortBy  = request.getParameter("sort");
        String sortDir = request.getParameter("dir");
        int page = parseIntParam(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        try {
            List<Promotion> promotions = findPromotions(keyword, type, status, page, PAGE_SIZE, sortBy, sortDir);
            int totalItems = countPromotions(keyword, type, status);
            int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);

            request.setAttribute("promotions",  promotions);
            request.setAttribute("totalItems",  totalItems);
            request.setAttribute("totalPages",  totalPages);
            request.setAttribute("currentPage", page);
            request.setAttribute("keyword",     keyword != null ? keyword : "");
            request.setAttribute("filterType",  type    != null ? type    : "");
            request.setAttribute("activeView",  status);
            request.setAttribute("sortBy",      sortBy  != null ? sortBy  : "");
            request.setAttribute("sortDir",     sortDir != null ? sortDir : "DESC");
            request.getRequestDispatcher(jspPath).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "System error. Please try again.");
            request.getRequestDispatcher(jspPath).forward(request, response);
        }
    }

    // ========== BUSINESS LOGIC (merged from PromotionService) ==========

    private List<Promotion> findPromotions(String keyword, String type, String status,
            int page, int pageSize, String sortBy, String sortDir) throws SQLException {
        return promotionDAO.search(keyword, normalizeType(type), status, page, pageSize, sortBy, sortDir);
    }

    private int countPromotions(String keyword, String type, String status) throws SQLException {
        return promotionDAO.countTotal(keyword, normalizeType(type), status);
    }

    private Promotion getById(int id) throws NotFoundException, SQLException {
        Promotion p = promotionDAO.findById(id);
        if (p == null) {
            throw new NotFoundException("Promotion not found");
        }
        return p;
    }

    private String create(PromotionRequestDTO dto) throws ValidationException, SQLException {
        dto.setPromotionCode(promotionDAO.generateNextCode());
        Map<String, String> errors = validateForCreate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        Promotion p = buildEntity(dto);
        promotionDAO.insert(p);
        return p.getStatus();
    }

    private String update(int id, PromotionRequestDTO dto)
            throws NotFoundException, ConflictException, ValidationException, SQLException {
        Promotion existing = promotionDAO.findById(id);
        if (existing == null) {
            throw new NotFoundException("Promotion not found");
        }
        if (existing.getUsedCount() > 0) {
            checkUsedRestrictions(existing, dto);
        }
        Map<String, String> errors = validateForUpdate(dto, existing);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        applyUpdates(existing, dto);
        promotionDAO.update(existing);
        return existing.getStatus();
    }

    private void delete(int id) throws NotFoundException, ConflictException, SQLException {
        Promotion existing = promotionDAO.findById(id);
        if (existing == null) {
            throw new NotFoundException("Promotion not found");
        }
        if (promotionDAO.hasInvoicePaid(id)) {
            throw new ConflictException("Cannot delete promotion used in paid invoices");
        }
        promotionDAO.softDelete(id);
    }

    // ========== VALIDATION ==========

    private Map<String, String> validateForCreate(PromotionRequestDTO dto) throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();
        validateDiscountType(dto.getDiscountType(), errors);
        validateDiscountValue(dto.getDiscountValue(), dto.getDiscountType(), errors);
        validateMinOrderAmount(dto.getMinOrderAmount(), errors);
        validateDates(dto.getStartDate(), dto.getEndDate(), errors);
        if (!errors.containsKey("startDate")
                && dto.getStartDate() != null && !dto.getStartDate().trim().isEmpty()) {
            LocalDateTime startDate = parseDateTime(dto.getStartDate().trim());
            if (startDate != null && !startDate.isAfter(LocalDateTime.now())) {
                errors.put("startDate", "Ngày bắt đầu phải là thời điểm trong tương lai");
            }
        }
        validateUsageLimit(dto.getUsageLimit(), errors);
        return errors;
    }

    private Map<String, String> validateForUpdate(PromotionRequestDTO dto, Promotion existing)
            throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();
        if (dto.getPromotionCode() != null && existing.getUsedCount() == 0) {
            validatePromotionCode(dto.getPromotionCode(), existing.getPromotionId(), errors);
        }
        if (dto.getDiscountType() != null) {
            validateDiscountType(dto.getDiscountType(), errors);
        }
        if (dto.getDiscountValue() != null) {
            String effectiveType = dto.getDiscountType() != null
                    ? dto.getDiscountType() : existing.getDiscountType();
            validateDiscountValue(dto.getDiscountValue(), effectiveType, errors);
        }
        if (dto.getMinOrderAmount() != null) {
            validateMinOrderAmount(dto.getMinOrderAmount(), errors);
        }
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            String effectiveStart = dto.getStartDate() != null
                    ? dto.getStartDate() : existing.getStartDate().format(FORM_FORMATTER);
            String effectiveEnd = dto.getEndDate() != null
                    ? dto.getEndDate() : existing.getEndDate().format(FORM_FORMATTER);
            validateDates(effectiveStart, effectiveEnd, errors);
        }
        if (dto.getUsageLimit() != null) {
            validateUsageLimit(dto.getUsageLimit(), errors);
        }
        return errors;
    }

    private void validatePromotionCode(String code, int excludeId, Map<String, String> errors)
            throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            errors.put("promotionCode", "Promotion code is required");
            return;
        }
        String upper = code.trim().toUpperCase();
        if (upper.length() > 50) {
            errors.put("promotionCode", "Promotion code must not exceed 50 characters");
            return;
        }
        if (!CODE_PATTERN.matcher(upper).matches()) {
            errors.put("promotionCode",
                    "Promotion code may only contain A-Z, 0-9, hyphens and underscores");
            return;
        }
        if (promotionDAO.existsByCode(upper, excludeId)) {
            errors.put("promotionCode", "Promotion code already exists");
        }
    }

    private void validateDiscountType(String discountType, Map<String, String> errors) {
        if (discountType == null || discountType.trim().isEmpty()) {
            errors.put("discountType", "Discount type is required");
            return;
        }
        if (!"Percentage".equals(discountType) && !"FlatAmount".equals(discountType)) {
            errors.put("discountType", "Discount type must be Percentage or FlatAmount");
        }
    }

    private void validateDiscountValue(BigDecimal value, String type,
            Map<String, String> errors) {
        if (value == null) {
            errors.put("discountValue", "Discount value is required");
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("discountValue", "Discount value must be greater than 0");
            return;
        }
        if ("Percentage".equals(type) && value.compareTo(new BigDecimal("100")) > 0) {
            errors.put("discountValue", "Percentage discount cannot exceed 100");
        }
    }

    private void validateMinOrderAmount(BigDecimal amount, Map<String, String> errors) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            errors.put("minOrderAmount", "Minimum order amount must be >= 0");
        }
    }

    // Package-private (not private) so PromotionServletValidateDatesTest, in the
    // same package under test/, can call it directly without reflection.
    void validateDates(String startDateStr, String endDateStr,
            Map<String, String> errors) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            errors.put("startDate", "Start date is required");
        } else {
            startDate = parseDateTime(startDateStr);
            if (startDate == null) {
                errors.put("startDate", "Invalid start date format");
            }
        }

        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            errors.put("endDate", "End date is required");
        } else {
            endDate = parseDateTime(endDateStr);
            if (endDate == null) {
                errors.put("endDate", "Invalid end date format");
            }
        }

        if (startDate != null && endDate != null
                && ChronoUnit.HOURS.between(startDate, endDate) < 24) {
            errors.put("endDate", "End date must be at least 1 day after start date");
        }
    }

    private void validateUsageLimit(Integer usageLimit, Map<String, String> errors) {
        if (usageLimit != null && usageLimit <= 0) {
            errors.put("usageLimit", "Usage limit must be greater than 0");
        }
    }

    private void checkUsedRestrictions(Promotion existing, PromotionRequestDTO dto) {
        if (dto.getPromotionCode() != null
                && !dto.getPromotionCode().trim().toUpperCase().equals(existing.getPromotionCode())) {
            throw new ConflictException(
                    "Cannot change code, type, or value of a promotion that has been used");
        }
        if (dto.getDiscountType() != null
                && !dto.getDiscountType().equals(existing.getDiscountType())) {
            throw new ConflictException(
                    "Cannot change code, type, or value of a promotion that has been used");
        }
        if (dto.getDiscountValue() != null
                && dto.getDiscountValue().compareTo(existing.getDiscountValue()) != 0) {
            throw new ConflictException(
                    "Cannot change code, type, or value of a promotion that has been used");
        }
    }

    private Promotion buildEntity(PromotionRequestDTO dto) {
        Promotion p = new Promotion();
        p.setPromotionCode(dto.getPromotionCode().trim().toUpperCase());
        p.setDescription(dto.getDescription());
        p.setDiscountType(dto.getDiscountType());
        p.setDiscountValue(dto.getDiscountValue());
        p.setMinOrderAmount(dto.getMinOrderAmount() != null
                ? dto.getMinOrderAmount() : BigDecimal.ZERO);
        p.setMaxDiscountAmount("Percentage".equals(dto.getDiscountType())
                ? dto.getMaxDiscountAmount() : null);
        LocalDateTime startDate = parseDateTime(dto.getStartDate().trim());
        LocalDateTime endDate = parseDateTime(dto.getEndDate().trim());
        p.setStartDate(startDate);
        p.setEndDate(endDate);
        p.setUsageLimit(dto.getUsageLimit());

        LocalDateTime now = LocalDateTime.now();
        String status;
        if (endDate != null && endDate.isBefore(now)) {
            status = "expired";
        } else if (startDate != null && startDate.isAfter(now)) {
            status = "upcoming";
        } else {
            status = "active";
        }
        p.setStatus(status);
        p.setActive("active".equals(status));
        return p;
    }

    private void applyUpdates(Promotion existing, PromotionRequestDTO dto) {
        if (dto.getPromotionCode() != null) {
            existing.setPromotionCode(dto.getPromotionCode().trim().toUpperCase());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getDiscountType() != null) {
            existing.setDiscountType(dto.getDiscountType());
        }
        if (dto.getDiscountValue() != null) {
            existing.setDiscountValue(dto.getDiscountValue());
        }
        if (dto.getMinOrderAmount() != null) {
            existing.setMinOrderAmount(dto.getMinOrderAmount());
        }
        if (dto.getMaxDiscountAmount() != null) {
            String effectiveType = dto.getDiscountType() != null
                    ? dto.getDiscountType() : existing.getDiscountType();
            if ("Percentage".equals(effectiveType)) {
                existing.setMaxDiscountAmount(dto.getMaxDiscountAmount());
            }
        }
        if (dto.getStartDate() != null) {
            LocalDateTime parsed = parseDateTime(dto.getStartDate().trim());
            if (parsed != null) {
                existing.setStartDate(parsed);
            }
        }
        if (dto.getEndDate() != null) {
            LocalDateTime parsed = parseDateTime(dto.getEndDate().trim());
            if (parsed != null) {
                existing.setEndDate(parsed);
            }
        }
        if (dto.getUsageLimit() != null) {
            existing.setUsageLimit(dto.getUsageLimit());
        }
        // Recalculate Status from new dates, preserving 'inactive' if explicitly set
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            if (!"inactive".equals(existing.getStatus())) {
                LocalDateTime now = LocalDateTime.now();
                if (existing.getEndDate() != null && existing.getEndDate().isBefore(now)) {
                    existing.setStatus("expired");
                    existing.setActive(false);
                } else if (existing.getStartDate() != null && existing.getStartDate().isAfter(now)) {
                    existing.setStatus("upcoming");
                    existing.setActive(false);
                } else {
                    existing.setStatus("active");
                    existing.setActive(true);
                }
            }
        }
    }

    // ========== UTILITY HELPERS ==========

    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        String t = type.trim();
        return ("Percentage".equals(t) || "FlatAmount".equals(t)) ? t : null;
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String s = str.trim();
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(s, FORM_FORMATTER);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private PromotionRequestDTO buildDtoFromRequest(HttpServletRequest request) {
        PromotionRequestDTO dto = new PromotionRequestDTO();
        dto.setPromotionCode(request.getParameter("promotionCode"));
        dto.setDescription(request.getParameter("description"));
        dto.setDiscountType(request.getParameter("discountType"));

        String discountValueStr = request.getParameter("discountValue");
        if (discountValueStr != null && !discountValueStr.trim().isEmpty()) {
            try { dto.setDiscountValue(new BigDecimal(discountValueStr.trim())); }
            catch (NumberFormatException ignored) {}
        }

        String minOrderStr = request.getParameter("minOrderAmount");
        if (minOrderStr != null && !minOrderStr.trim().isEmpty()) {
            try { dto.setMinOrderAmount(new BigDecimal(minOrderStr.trim())); }
            catch (NumberFormatException ignored) {}
        }

        String maxDiscountStr = request.getParameter("maxDiscountAmount");
        if (maxDiscountStr != null && !maxDiscountStr.trim().isEmpty()) {
            try { dto.setMaxDiscountAmount(new BigDecimal(maxDiscountStr.trim())); }
            catch (NumberFormatException ignored) {}
        }

        dto.setStartDate(request.getParameter("startDate"));
        dto.setEndDate(request.getParameter("endDate"));

        String usageLimitStr = request.getParameter("usageLimit");
        if (usageLimitStr != null && !usageLimitStr.trim().isEmpty()) {
            try { dto.setUsageLimit(Integer.parseInt(usageLimitStr.trim())); }
            catch (NumberFormatException ignored) {}
        }

        String isActiveStr = request.getParameter("isActive");
        dto.setIsActive("on".equals(isActiveStr) || "true".equals(isActiveStr));
        dto.setUsedCount(parseIntParam(request.getParameter("usedCount"), 0));
        return dto;
    }

    private void transferFlash(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Promotion Manager Servlet - UC43 Manage Promotion + UC44 View Promotion List";
    }
}
