/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.RoomDAO;
import com.cinema.dao.SeatDAO;
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

    // DAO used for seat operations
    private final SeatDAO seatDAO = new SeatDAO();

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
        // Get seat layout information
        int numberOfRows = Integer.parseInt(
                request.getParameter("numberOfRows"));

        int seatsPerRow = Integer.parseInt(
                request.getParameter("seatsPerRow"));

        int capacity = Integer.parseInt(
                request.getParameter("capacity"));

<<<<<<< Updated upstream
=======
        String currentPage = request.getParameter("page");
        if (currentPage == null || currentPage.isEmpty()) {
            currentPage = "1";
        }

        /*
        * Validate seat layout
        * Total seats must match room capacity
         */
        if ((numberOfRows * seatsPerRow) != capacity) {

            response.sendRedirect(
                    "RoomServlet?error=invalid_layout&page="
                    + currentPage);

            return;
        }

        // Validate capacity
        if (capacity <= 0) {
            response.sendRedirect("RoomServlet?action=list&error=capacity_invalid&page=" + currentPage);
            return;
        }

        // Validate room number uniqueness
        if (roomDAO.isRoomNumberExists(roomNumber)) {
            response.sendRedirect("RoomServlet?error=room_number_exists&page=" + currentPage);
            return;
        }

        // Create Room object and set values
>>>>>>> Stashed changes
        Room room = new Room();

        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        // Set seat layout information
        room.setNumberOfRows(numberOfRows);
        room.setSeatsPerRow(seatsPerRow);

<<<<<<< Updated upstream
        roomDAO.addRoom(room);
=======
        // Insert room into database
        boolean inserted = roomDAO.addRoom(room);

        /*
        * Automatically generate seats
        * after room creation
         */
        if (inserted) {

            // Get newly created room ID
            int roomId = roomDAO.getLatestRoomId();

            // Generate seats
            seatDAO.generateSeats(
                    roomId,
                    numberOfRows,
                    seatsPerRow
            );
        }
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
=======
        // Validate capacity: If invalid, redirect back to the edit form with an error parameter
        if (capacity <= 0) {
            response.sendRedirect("RoomServlet?action=edit&id=" + roomId + "&error=capacity_invalid&page=" + currentPage);
            return;
        }

        // Validate duplicate room number when updating room
        if (roomDAO.isRoomNumberExists(roomNumber)) {

            response.sendRedirect(
                    "RoomServlet?action=edit&id="
                    + roomId
                    + "&error=room_number_exists&page="
                    + currentPage);

            return;
        }

        // Check checkbox status for active field
        boolean active = request.getParameter("active") != null;

        // Create updated Room object
>>>>>>> Stashed changes
        Room room = new Room();

        room.setRoomId(roomId);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setActive(active);

        roomDAO.updateRoom(room);

<<<<<<< Updated upstream
        response.sendRedirect("room");
=======
        response.sendRedirect("RoomServlet?page=" + currentPage);
>>>>>>> Stashed changes
    }

    private void deleteRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        int roomId = Integer.parseInt(
                request.getParameter("id"));

        roomDAO.deleteRoom(roomId);

        response.sendRedirect("RoomServlet");
    }

<<<<<<< Updated upstream
=======
    /**
     * Show edit form for a specific room Loads room data and forwards it to
     * edit JSP
     */
    private void showEditForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Get room ID from request
        int roomId = Integer.parseInt(
                request.getParameter("id"));

        // Retrieve room from database
        Room room = roomDAO.getRoomById(roomId);

        String currentPage = request.getParameter("page");
        request.setAttribute("currentPage", currentPage);

        // Send room data to JSP
        request.setAttribute("room", room);

        // Forward to edit page
        request.getRequestDispatcher("room-edit.jsp")
                .forward(request, response);
    }

>>>>>>> Stashed changes
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
