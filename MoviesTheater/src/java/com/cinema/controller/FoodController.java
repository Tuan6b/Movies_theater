package com.cinema.controller;

import com.cinema.dao.FoodDAO;
import com.cinema.model.Food;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class FoodController extends HttpServlet {

    private static final String LIST_JSP = "/food-list.jsp";
    private static final String ADD_JSP = "/food-add.jsp";
    private static final String EDIT_JSP = "/food-edit.jsp";

    private final FoodDAO foodDAO = new FoodDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "add":
                request.setAttribute("currentType", getTypeParam(request));
                request.getRequestDispatcher(ADD_JSP).forward(request, response);
                break;
            case "edit":
                showEdit(request, response);
                break;
            default:
                list(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        switch (action != null ? action : "") {
            case "add":
                add(request, response);
                break;
            case "edit":
                edit(request, response);
                break;
            case "delete":
                delete(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/FoodController");
                break;
        }
    }

    private String getTypeParam(HttpServletRequest request) {
        String type = request.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            type = "retail";
        }
        return type;
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = getTypeParam(request);
        boolean isCombo = type.equals("combo");
        List<Food> foodList = foodDAO.getFoodsByType(isCombo);
        request.setAttribute("foodList", foodList);
        request.setAttribute("currentType", type);
        request.getRequestDispatcher(LIST_JSP).forward(request, response);
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                Food food = foodDAO.getFoodById(id);
                if (food != null) {
                    request.setAttribute("food", food);
                    request.setAttribute("currentType", food.isIsCombo() ? "combo" : "retail");
                }
            } catch (NumberFormatException ignored) {
            }
        }
        request.getRequestDispatcher(EDIT_JSP).forward(request, response);
    }

    private void add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String foodName = request.getParameter("foodName");
        String priceStr = request.getParameter("price");
        String image = request.getParameter("image");
        String type = request.getParameter("type");
        if (type == null) type = "retail";
        boolean isCombo = type.equals("combo");

        if (foodName == null || foodName.trim().isEmpty()) {
            request.getSession().setAttribute("flashError", "Vui lòng nhập tên món ăn.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=add&type=" + type);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Giá không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=add&type=" + type);
            return;
        }

        Food food = new Food();
        food.setFoodName(foodName.trim());
        food.setPrice(price);
        food.setImage(image != null ? image.trim() : null);
        food.setIsActive(true);
        food.setIsCombo(isCombo);
        foodDAO.addFood(food);

        request.getSession().setAttribute("flashSuccess", "Đã thêm món ăn thành công.");
        response.sendRedirect(request.getContextPath() + "/FoodController?type=" + type);
    }

    private void edit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/FoodController");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/FoodController");
            return;
        }

        String foodName = request.getParameter("foodName");
        String priceStr = request.getParameter("price");
        String image = request.getParameter("image");
        String type = request.getParameter("type");
        if (type == null) type = "retail";
        boolean isCombo = type.equals("combo");

        if (foodName == null || foodName.trim().isEmpty()) {
            request.getSession().setAttribute("flashError", "Vui lòng nhập tên món ăn.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=edit&id=" + id + "&type=" + type);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Giá không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=edit&id=" + id + "&type=" + type);
            return;
        }

        Food food = foodDAO.getFoodById(id);
        if (food == null) {
            response.sendRedirect(request.getContextPath() + "/FoodController");
            return;
        }
        food.setFoodName(foodName.trim());
        food.setPrice(price);
        if (image != null) {
            food.setImage(image.trim());
        }
        food.setIsCombo(isCombo);
        foodDAO.updateFood(food);

        request.getSession().setAttribute("flashSuccess", "Đã cập nhật món ăn thành công.");
        response.sendRedirect(request.getContextPath() + "/FoodController?type=" + type);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                foodDAO.deleteFood(id);
                request.getSession().setAttribute("flashSuccess", "Đã xóa món ăn.");
            } catch (NumberFormatException ignored) {
            }
        }
        String type = request.getParameter("type");
        if (type == null) type = "retail";
        response.sendRedirect(request.getContextPath() + "/FoodController?type=" + type);
    }

    private void transferFlash(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }
}
