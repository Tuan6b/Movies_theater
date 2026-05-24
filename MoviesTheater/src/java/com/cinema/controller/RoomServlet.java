/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.RoomDAO;
import com.cinema.model.Room;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 *
 * @author Tuan Phong Nguyen
 */
public class RoomServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();

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
        try (PrintWriter out = response.getWriter()) {

            String action = request.getParameter("action");

            if (action == null) {
                action = "list";
            }

            switch (action) {

                case "add":
                    addRoom(request, response);
                    break;

                case "edit":
                    showEditForm(request, response);
                    break;

                case "update":
                    updateRoom(request, response);
                    break;

                case "delete":
                    deleteRoom(request, response);
                    break;

                default:
                    listRooms(request, response);
                    break;
            }
        }
    }

    private void listRooms(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<Room> roomList = roomDAO.getAllRooms();

        request.setAttribute("roomList", roomList);

        request.getRequestDispatcher("room-list.jsp")
                .forward(request, response);
    }

    private void addRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        int capacity = Integer.parseInt(
                request.getParameter("capacity"));

        Room room = new Room();

        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);

        roomDAO.addRoom(room);

        response.sendRedirect("RoomServlet");
    }

    private void updateRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int roomId = Integer.parseInt(
                request.getParameter("roomId"));

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        int capacity = Integer.parseInt(
                request.getParameter("capacity"));

        boolean active = request.getParameter("active")
                != null;

        Room room = new Room();

        room.setRoomId(roomId);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setActive(active);

        roomDAO.updateRoom(room);

        response.sendRedirect("RoomServlet");
    }

    private void deleteRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int roomId = Integer.parseInt(
                request.getParameter("id"));

        roomDAO.deleteRoom(roomId);

        response.sendRedirect("RoomServlet");
    }

    private void showEditForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int roomId = Integer.parseInt(
                request.getParameter("id"));

        Room room = roomDAO.getRoomById(roomId);

        request.setAttribute("room", room);

        request.getRequestDispatcher("room-edit.jsp")
                .forward(request, response);
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
        return "Short description";
    }// </editor-fold>

}
