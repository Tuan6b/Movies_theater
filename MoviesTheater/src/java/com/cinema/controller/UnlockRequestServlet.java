package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.UnlockRequestDAO;
import com.cinema.model.Account;
import com.cinema.model.UnlockRequest;
import com.cinema.util.MailUtil;
import com.cinema.util.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class UnlockRequestServlet extends HttpServlet {

    private final UnlockRequestDAO unlockDAO = new UnlockRequestDAO();
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
            List<UnlockRequest> pending = unlockDAO.getPending();
            List<UnlockRequest> all = unlockDAO.getAll();
            request.setAttribute("pendingList", pending);
            request.setAttribute("allList", all);
            request.getRequestDispatcher("/view/admin/unlock-requests.jsp").forward(request, response);
            return;
        }

        request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("review".equals(action)) {
            doReview(request, response);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String reason = request.getParameter("reason");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email.");
            request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập lý do.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
            return;
        }

        Account account = accountDAO.getAccountByEmail(email.trim());
        if (account == null) {
            request.setAttribute("error", "Email không tồn tại trong hệ thống.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
            return;
        }

        if (!account.isIsBlocked()) {
            request.setAttribute("error", "Tài khoản này chưa bị khóa, không cần yêu cầu mở khóa.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
            return;
        }

        List<UnlockRequest> existing = unlockDAO.getByAccountId(account.getAccountId());
        boolean hasPending = existing.stream().anyMatch(r -> "Pending".equals(r.getStatus()));
        if (hasPending) {
            request.setAttribute("error", "Bạn đã có yêu cầu mở khóa đang chờ xử lý.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
            return;
        }

        UnlockRequest req = new UnlockRequest();
        req.setAccountId(account.getAccountId());
        req.setReason(reason.trim());
        int id = unlockDAO.insert(req);

        if (id > 0) {
            SystemLogService.log(account.getAccountId(), "UNLOCK_REQUEST",
                    "Unlock request submitted: " + reason, request.getRemoteAddr());
            request.setAttribute("success", "Yêu cầu mở khóa đã được gửi. Quản trị viên sẽ xem xét sớm nhất.");
        } else {
            request.setAttribute("error", "Gửi yêu cầu thất bại. Vui lòng thử lại sau.");
        }

        request.getRequestDispatcher("/view/auth/unlock-request.jsp").forward(request, response);
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

        boolean ok = unlockDAO.updateStatus(reqId, status, admin.getAccountId());
        if (ok) {
            UnlockRequest req = unlockDAO.getById(reqId);
            if (req != null) {
                Account u = accountDAO.getAccountById(req.getAccountId());
                if (approved) {
                    accountDAO.setBlocked(req.getAccountId(), false);
                    if (u != null && u.getEmail() != null) {
                        String body = "<p>Xin chào <strong>" + MailUtil.escape(u.getFullName()) + "</strong>,</p>"
                                + "<p>Yêu cầu mở khóa tài khoản CGV Cinema (<strong>" + MailUtil.escape(u.getEmail()) + "</strong>) của bạn đã được <strong>chấp thuận</strong>.</p>"
                                + "<p>Bạn có thể đăng nhập và tiếp tục sử dụng dịch vụ.</p>"
                                + "<p>Trân trọng,<br><strong>CGV Cinema Team</strong></p>";
                        try {
                            MailUtil.sendNotificationEmail(u.getEmail(), u.getFullName(),
                                    "Yêu cầu mở khóa tài khoản CGV Cinema đã được chấp thuận", body);
                        } catch (Exception e) {
                            System.err.println("[UNLOCK_APPROVE_EMAIL_FAILED] reqId=" + reqId + " : " + e.getMessage());
                        }
                    }
                    SystemLogService.log(admin.getAccountId(), "UNLOCK_APPROVED",
                            "Approved unlock request #" + reqId + " for account " + req.getAccountId(),
                            request.getRemoteAddr());
                } else if (u != null && u.getEmail() != null) {
                    String body = "<p>Xin chào <strong>" + MailUtil.escape(u.getFullName()) + "</strong>,</p>"
                            + "<p>Yêu cầu mở khóa tài khoản CGV Cinema (<strong>" + MailUtil.escape(u.getEmail()) + "</strong>) của bạn đã bị <strong>từ chối</strong>.</p>"
                            + "<p>Nếu bạn có thắc mắc, vui lòng liên hệ quản trị viên.</p>"
                            + "<p>Trân trọng,<br><strong>CGV Cinema Team</strong></p>";
                    try {
                        MailUtil.sendNotificationEmail(u.getEmail(), u.getFullName(),
                                "Yêu cầu mở khóa tài khoản CGV Cinema bị từ chối", body);
                    } catch (Exception e) {
                        System.err.println("[UNLOCK_REJECT_EMAIL_FAILED] reqId=" + reqId + " : " + e.getMessage());
                    }
                    SystemLogService.log(admin.getAccountId(), "UNLOCK_REJECTED",
                            "Rejected unlock request #" + reqId + " for account " + req.getAccountId(),
                            request.getRemoteAddr());
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/unlock-request/admin");
    }
}
