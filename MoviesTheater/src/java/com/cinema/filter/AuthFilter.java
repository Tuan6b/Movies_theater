package com.cinema.filter;

import com.cinema.model.Account;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthFilter extends HttpFilter implements Filter {

    // Public pages
    private static final List<String> EXACT_PUBLIC_PATHS = Arrays.asList(
            "/",
            "/index.jsp",
            "/view/customer/index.jsp",
            "/view/customer/home.jsp",
            "/view/customer/movie-detail.jsp",
            "/view/customer/showtimes.jsp",
            "/HomeController",
            "/MovieDetailController",
            "/view/auth/login.jsp",
            "/view/auth/register.jsp",
            "/view/auth/forgot-password.jsp",
            "/view/auth/new-password.jsp",
            "/Login",
            "/Register",
            "/view/common/Error.jsp",
            "/showtimes",
            "/RoomServlet",
            "/TMDBController"
    );

    // Public resources
    private static final List<String> PREFIX_PUBLIC_PATHS = Arrays.asList(
            "/css/",
            "/js/",
            "/Image/",
            "/LoginGoogle",
            "/forgot-password",
            "/new-password"
    );

    // Manager-only functions
    private static final List<String> MANAGER_ONLY_PATHS = Arrays.asList(
            "/manager/promotions",
            "/manager/food",
            "/manager/analytics",
            "/manager/users",
            "/manager/genre",
            "/manager/shifts",
            "/manager/employees"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();

        if (path == null || path.isEmpty()) {
            path = "/";
        }

        // ===== PUBLIC URL =====
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // ===== CHECK LOGIN =====
        HttpSession session = request.getSession(false);
        Account account = null;

        if (session != null) {
            account = (Account) session.getAttribute("account");
        }

        if (account == null) {

            session = request.getSession();

            String redirectUri = request.getRequestURI();

            if (request.getQueryString() != null) {
                redirectUri += "?" + request.getQueryString();
            }

            session.setAttribute("redirectAfterLogin", redirectUri);

            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        int roleId = account.getRoleId();

        /*
         * Role Mapping
         * 2 = Customer
         * 3 = Employee
         * 4 = Manager
         * 5 = Admin
         */

        // ===== ADMIN ONLY =====
        if ((path.startsWith("/admin") || path.startsWith("/view/admin")) && roleId < 5) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied");
            return;
        }

        // ===== MANAGER ONLY =====
        if (isPathInList(path, MANAGER_ONLY_PATHS) && roleId < 4) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied");
            return;
        }

        // ===== EMPLOYEE AREA =====
        if ((path.startsWith("/manager") || path.startsWith("/view/manager")) && roleId < 4) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied");
            return;
        }

        // ===== EMPLOYEE DASHBOARD =====
        if ((path.startsWith("/employee") || path.startsWith("/view/employee")) && roleId < 3) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied");
            return;
        }

        // ===== FOOD CONTROLLER (employee+) =====
        if ("/FoodController".equals(path) && roleId < 3) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {

        if (EXACT_PUBLIC_PATHS.contains(path)) {
            return true;
        }

        for (String p : PREFIX_PUBLIC_PATHS) {
            if (path.startsWith(p)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPathInList(String path, List<String> pathList) {

        for (String p : pathList) {
            if (path.equals(p) || path.startsWith(p)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}