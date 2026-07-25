package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.DeletionRequestDAO;
import com.cinema.model.Account;
import com.cinema.model.DeletionRequest;
import com.cinema.util.MailUtil;
import com.cinema.util.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class DeletionRequestServlet extends HttpServlet {

    private final DeletionRequestDAO deletionDAO = new DeletionRequestDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path != null && path.equals("/admin")) {
            HttpSession session = request.getSession(false);
            Account admin = (Account) session.getAttribute("account");
            if (admin == null || admin.getRoleId() < 5) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("pendingList", deletionDAO.getPending());
            request.setAttribute("allList", deletionDAO.getAll());
            request.getRequestDispatcher("/view/admin/deletion-requests.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("review".equals(action)) {
            doReview(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String reason = request.getParameter("reason");
        if (reason == null || reason.trim().isEmpty()) {
            session.setAttribute("flashError", "Vui lòng nhập lý do xóa tài khoản.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        List<DeletionRequest> existing = deletionDAO.getByAccountId(account.getAccountId());
        boolean hasPending = existing.stream().anyMatch(r -> "Pending".equals(r.getStatus()));
        if (hasPending) {
            session.setAttribute("flashError", "Bạn đã có yêu cầu xóa tài khoản đang chờ xử lý.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        DeletionRequest req = new DeletionRequest();
        req.setAccountId(account.getAccountId());
        req.setReason(reason.trim());
        int id = deletionDAO.insert(req);

        if (id > 0) {
            SystemLogService.log(account.getAccountId(), "DELETION_REQUEST",
                    "Account deletion requested: " + reason, request.getRemoteAddr());
            session.setAttribute("flashSuccess", "Yêu cầu xóa tài khoản đã được gửi. Quản trị viên sẽ xem xét.");
        } else {
            session.setAttribute("flashError", "Gửi yêu cầu thất bại. Vui lòng thử lại sau.");
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void doReview(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Account admin = (Account) session.getAttribute("account");
        if (admin == null || admin.getRoleId() < 5) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int reqId = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");
        boolean approved = "Approved".equals(status);

        boolean ok = deletionDAO.updateStatus(reqId, status, admin.getAccountId());
        if (ok) {
            DeletionRequest req = deletionDAO.getById(reqId);
            if (req != null) {
                Account u = accountDAO.getAccountById(req.getAccountId());
                if (approved) {
                    accountDAO.setBlocked(req.getAccountId(), true);
                    SystemLogService.log(admin.getAccountId(), "DELETION_APPROVED",
                            "Approved deletion request #" + reqId + " for account " + req.getAccountId(),
                            request.getRemoteAddr());
                } else if (u != null && u.getEmail() != null) {
                    String body = "<p>Xin chào <strong>" + MailUtil.escape(u.getFullName()) + "</strong>,</p>"
                            + "<p>Yêu cầu xóa tài khoản CGV Cinema (<strong>" + MailUtil.escape(u.getEmail()) + "</strong>) của bạn đã bị <strong>từ chối</strong>.</p>"
                            + "<p>Nếu bạn có thắc mắc, vui lòng liên hệ với quản trị viên để biết thêm chi tiết.</p>"
                            + "<p>Trân trọng,<br><strong>CGV Cinema Team</strong></p>";
                    try {
                        MailUtil.sendNotificationEmail(u.getEmail(), u.getFullName(),
                                "Yêu cầu xóa tài khoản CGV Cinema bị từ chối", body);
                    } catch (Exception e) {
                        System.err.println("[DELETION_REJECT_EMAIL_FAILED] reqId=" + reqId + " : " + e.getMessage());
                    }
                    SystemLogService.log(admin.getAccountId(), "DELETION_REJECTED",
                            "Rejected deletion request #" + reqId + " for account " + req.getAccountId(),
                            request.getRemoteAddr());
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/deletion-request/admin");
    }
}
