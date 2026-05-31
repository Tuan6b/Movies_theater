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

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/Login", "/Register", "/login.jsp", "/register.jsp",
            "/LoginGoogle", "/LoginGoogle/",
            "/css/", "/Image/", "/js/",
            "/showtimes", "/RoomServlet",
            "/admin/genre",
            "/Error.jsp", "/index.jsp", "/"
    );

    private static final List<String> MANAGER_PATHS = Arrays.asList(
            "/manager", "/manager/"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();

        if (path.isEmpty()) {
            path = "/";
        }

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Account account = null;
        if (session != null) {
            account = (Account) session.getAttribute("account");
        }

        if (account == null) {
            session = request.getSession();
            String redirectUri = request.getRequestURI();
            String qs = request.getQueryString();
            if (qs != null) {
                redirectUri += "?" + qs;
            }
            session.setAttribute("redirectAfterLogin", redirectUri);
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        if (isManagerPath(path)) {
            if (account.getRoleId() < 3) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        for (String p : PUBLIC_PATHS) {
            if (path.equals(p) || path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean isManagerPath(String path) {
        for (String p : MANAGER_PATHS) {
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
