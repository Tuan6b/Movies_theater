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
 * RoomServlet is the Controller in MVC architecture.
 * It handles all HTTP requests related to Room management:
 * list, add, update, delete, and edit operations.
 */
public class RoomServlet extends HttpServlet {

    // DAO layer used to interact with database
    private final RoomDAO roomDAO = new RoomDAO();

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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

            // Default action is list if no action provided
            if (action == null) {
                action = "list";
            }

            // Route request to appropriate handler method
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

    /**
     * Display list of all rooms
     * Data is retrieved from DAO and forwarded to JSP view
     */
    private void listRooms(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Get all rooms from database
        List<Room> roomList = roomDAO.getAllRooms();

        // Store list in request scope for JSP
        request.setAttribute("roomList", roomList);

        // Forward to list page
        request.getRequestDispatcher("room-list.jsp")
                .forward(request, response);
    }

    /**
     * Handle adding a new room
     * Includes basic validation for capacity (> 0)
     */
    private void addRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        // Parse capacity from request
        int capacity = Integer.parseInt(
                request.getParameter("capacity"));

        // Validate capacity
        if (capacity <= 0) {
            response.getWriter().println(
                    "Capacity must be greater than 0");
            return;
        }

        // Create Room object and set values
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);

        // Insert room into database
        roomDAO.addRoom(room);

        // Redirect to list page after success
        response.sendRedirect("RoomServlet");
    }

    /**
     * Handle updating existing room information
     */
    private void updateRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Get room ID
        int roomId = Integer.parseInt(
                request.getParameter("roomId"));

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        // Parse capacity
        int capacity = Integer.parseInt(
                request.getParameter("capacity"));

        // Validate capacity
        if (capacity <= 0) {
            response.getWriter().println(
                    "Capacity must be greater than 0");
            return;
        }

        // Check checkbox status for active field
        boolean active = request.getParameter("active") != null;

        // Create updated Room object
        Room room = new Room();
        room.setRoomId(roomId);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setActive(active);

        // Update database
        roomDAO.updateRoom(room);

        // Redirect to list page
        response.sendRedirect("RoomServlet");
    }

    /**
     * Soft delete room (set IsActive = 0 instead of deleting record)
     */
    private void deleteRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        // Get room ID from request
        int roomId = Integer.parseInt(
                request.getParameter("id"));

        // Deactivate room in database
        roomDAO.deleteRoom(roomId);

        // Redirect to list page
        response.sendRedirect("RoomServlet");
    }

    /**
     * Show edit form for a specific room
     * Loads room data and forwards it to edit JSP
     */
    private void showEditForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Get room ID from request
        int roomId = Integer.parseInt(
                request.getParameter("id"));

        // Retrieve room from database
        Room room = roomDAO.getRoomById(roomId);

        // Send room data to JSP
        request.setAttribute("room", room);

        // Forward to edit page
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
