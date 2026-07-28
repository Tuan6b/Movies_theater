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

    private final FoodDAO foodDAO = new FoodDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

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
            case "showAddForm":
                showAddForm(request, response);
                break;
            case "add":
                addFood(request, response);
                break;
            case "showEditForm":
                showEditForm(request, response);
                break;
            case "edit":
                updateFood(request, response);
                break;
            case "delete":
                deleteFood(request, response);
                break;
            case "restore":
                restoreFood(request, response);
                break;
            default:
                listFood(request, response);
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

    private void listFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = getTypeParam(request);
        boolean isCombo = type.equals("combo");
        List<Food> foodList = foodDAO.getFoodsByType(isCombo);
        request.setAttribute("foodList", foodList);
        request.setAttribute("currentType", type);
        request.getRequestDispatcher("/view/employee/food-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("currentType", getTypeParam(request));
        request.getRequestDispatcher("/view/employee/food-add.jsp").forward(request, response);
    }

    private void addFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String foodName = request.getParameter("foodName");
        String priceStr = request.getParameter("price");
        String image = request.getParameter("image");
        String type = request.getParameter("type");
        if (type == null) type = "retail";
        boolean isCombo = type.equals("combo");

        if (foodName == null || foodName.trim().isEmpty()) {
            request.getSession().setAttribute("flashError", "Please enter a food name.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=showAddForm&type=" + type);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 1000) {
                request.getSession().setAttribute("flashError", "Price must be at least 1,000 VND.");
                response.sendRedirect(request.getContextPath() + "/FoodController?action=showAddForm&type=" + type);
                return;
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid price.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=showAddForm&type=" + type);
            return;
        }

        Food food = new Food();
        food.setFoodName(foodName.trim());
        food.setPrice(price);
        food.setImage(image != null ? image.trim() : null);
        food.setCombo(isCombo);
        food.setActive(true);
        foodDAO.addFood(food);

        request.getSession().setAttribute("flashSuccess", "Food item added successfully.");
        response.sendRedirect(request.getContextPath() + "/FoodController?type=" + type);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
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
        request.getRequestDispatcher("/view/employee/food-edit.jsp").forward(request, response);
    }

    private void updateFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
            request.getSession().setAttribute("flashError", "Please enter a food name.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=showEditForm&id=" + id + "&type=" + type);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 1000) {
                request.getSession().setAttribute("flashError", "Price must be at least 1,000 VND.");
                response.sendRedirect(request.getContextPath() + "/FoodController?action=showEditForm&id=" + id + "&type=" + type);
                return;
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid price.");
            response.sendRedirect(request.getContextPath() + "/FoodController?action=showEditForm&id=" + id + "&type=" + type);
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
        food.setCombo(isCombo);
        foodDAO.updateFood(food);

        request.getSession().setAttribute("flashSuccess", "Food item updated successfully.");
        response.sendRedirect(request.getContextPath() + "/FoodController?type=" + type);
    }

    private void deleteFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                foodDAO.deleteFood(id);
                request.getSession().setAttribute("flashSuccess", "Food item deactivated.");
            } catch (NumberFormatException ignored) {
            }
        }
        String type = request.getParameter("type");
        if (type == null) type = "retail";
        response.sendRedirect(request.getContextPath() + "/FoodController?type=" + type);
    }

    private void restoreFood(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                foodDAO.restoreFood(id);
                request.getSession().setAttribute("flashSuccess", "Food item restored.");
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
    //@Override
    /*protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }*/

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
