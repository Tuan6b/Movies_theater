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
 * RoomServlet is the Controller in MVC architecture. It handles all HTTP
 * requests related to Room management: list, add, update, delete, and edit
 * operations.
 */
public class RoomServlet extends HttpServlet {

    // DAO layer used to interact with database
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
     * Display list of all rooms Data is retrieved from DAO and forwarded to JSP
     * view Display paginated list of rooms
     */
    private void listRooms(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;          // Default page to display
        int recordsPerPage = 5; // Number of rows per page

        // Get the requested page number from URL parameter
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1; // Fallback to page 1 if format is invalid
            }
        }

        // Calculate the starting row index (offset) for the SQL query
        int offset = (page - 1) * recordsPerPage;

        // Fetch only the records needed for the current page from database
        List<Room> roomList = roomDAO.getRoomsByPage(offset, recordsPerPage);

        // Get total number of rooms to calculate total pages needed
        int totalRecords = roomDAO.getTotalRoomsCount();
        int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

        // Pass pagination data to JSP via request attributes
        request.setAttribute("roomList", roomList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        // Forward request to the view page
        request.getRequestDispatcher("room-list.jsp").forward(request, response);
    }

    /**
     * Handle adding a new room Includes basic validation for capacity (> 0)
     */
    private void addRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");
        // Get seat layout information
        int numberOfRows = Integer.parseInt(
                request.getParameter("numberOfRows"));

        int seatsPerRow = Integer.parseInt(
                request.getParameter("seatsPerRow"));

        // Parse capacity from request
        int capacity = Integer.parseInt(
                request.getParameter("capacity"));

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
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        // Set seat layout information
        room.setNumberOfRows(numberOfRows);
        room.setSeatsPerRow(seatsPerRow);

        // INSERT + GET ID NGAY LẬP TỨC
        int roomId = roomDAO.addRoomAndGetId(room);

        // Generate seats if room created successfully
        if (roomId > 0) {
            seatDAO.generateSeats(roomId, numberOfRows, seatsPerRow);
        }

        response.sendRedirect("RoomServlet?page=" + currentPage);
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

        String currentPage = request.getParameter("page");
        if (currentPage == null || currentPage.isEmpty()) {
            currentPage = "1";
        }

        // Validate capacity: If invalid, redirect back to the edit form with an error parameter
        if (capacity <= 0) {
            response.sendRedirect("RoomServlet?action=edit&id=" + roomId + "&error=capacity_invalid&page=" + currentPage);
            return;
        }

        // Validate duplicate room number when updating room
        if (roomDAO.isRoomNumberExists(roomNumber, roomId)) {

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
        Room room = new Room();
        room.setRoomId(roomId);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setActive(active);

        // Get current seat layout from db
        Room existingRoom = roomDAO.getRoomById(roomId);
        if (existingRoom != null) {
            room.setNumberOfRows(existingRoom.getNumberOfRows());
            room.setSeatsPerRow(existingRoom.getSeatsPerRow());
        }

        roomDAO.updateRoom(room);
        response.sendRedirect("RoomServlet?page=" + currentPage);
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

        String currentPage = request.getParameter("page");
        if (currentPage == null || currentPage.isEmpty()) {
            currentPage = "1";
        }

        // Redirect to list page
        response.sendRedirect("RoomServlet?page=" + currentPage);
    }

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
