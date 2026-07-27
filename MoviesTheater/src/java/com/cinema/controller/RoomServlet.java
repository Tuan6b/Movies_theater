package com.cinema.controller;

import com.cinema.dao.RoomDAO;
import com.cinema.dao.SeatDAO;
import com.cinema.dao.ScheduleDAO;
import com.cinema.model.Room;
import java.io.IOException;
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
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

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

    /**
     * Display list of all rooms Data is retrieved from DAO and forwarded to JSP
     * view Display paginated list of rooms
     */
    private void listRooms(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;
        int recordsPerPage = 5;

        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        String filter = request.getParameter("filter");
        if (filter == null) filter = "active";

        Boolean isActive = null;
        if ("active".equals(filter)) isActive = true;
        else if ("inactive".equals(filter)) isActive = false;

        int offset = (page - 1) * recordsPerPage;

        List<Room> roomList = roomDAO.getRoomsByPage(offset, recordsPerPage, isActive);
        int totalRecords = roomDAO.getTotalRoomsCount(isActive);
        int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

        request.setAttribute("roomList", roomList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentFilter", filter);

        request.getRequestDispatcher("/view/manager/room-list.jsp").forward(request, response);
    }

    /**
     * Handle adding a new room Includes basic validation for capacity (> 0)
     */
    private void addRoom(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        int numberOfRows = Integer.parseInt(
                request.getParameter("numberOfRows"));

        int seatsPerRow = Integer.parseInt(
                request.getParameter("seatsPerRow"));

        int capacity = numberOfRows * seatsPerRow;

        String currentPage = request.getParameter("page");
        if (currentPage == null || currentPage.isEmpty()) {
            currentPage = "1";
        }

        if (numberOfRows > 10 || seatsPerRow > 10 || numberOfRows < 1 || seatsPerRow < 1) {
            response.sendRedirect("RoomServlet?error=invalid_dimensions&page=" + currentPage);
            return;
        }

        if (roomDAO.isRoomNumberExists(roomNumber)) {
            response.sendRedirect("RoomServlet?error=room_number_exists&page=" + currentPage);
            return;
        }

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setNumberOfRows(numberOfRows);
        room.setSeatsPerRow(seatsPerRow);

        int roomId = roomDAO.addRoomAndGetId(room);

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

        int roomId = Integer.parseInt(
                request.getParameter("roomId"));

        String roomNumber = request.getParameter("roomNumber");
        String roomType = request.getParameter("roomType");

        int numberOfRows = Integer.parseInt(
                request.getParameter("numberOfRows"));
        int seatsPerRow = Integer.parseInt(
                request.getParameter("seatsPerRow"));

        int capacity = numberOfRows * seatsPerRow;

        String currentPage = request.getParameter("page");
        if (currentPage == null || currentPage.isEmpty()) {
            currentPage = "1";
        }
        String currentFilter = request.getParameter("filter");
        if (currentFilter == null || currentFilter.isEmpty()) {
            currentFilter = "active";
        }

        if (numberOfRows > 10 || seatsPerRow > 10 || numberOfRows < 1 || seatsPerRow < 1) {
            response.sendRedirect(
                    "RoomServlet?action=edit&id="
                    + roomId
                    + "&error=invalid_dimensions&page="
                    + currentPage + "&filter=" + currentFilter);
            return;
        }

        if (roomDAO.isRoomNumberExists(roomNumber, roomId)) {
            response.sendRedirect(
                    "RoomServlet?action=edit&id="
                    + roomId
                    + "&error=room_number_exists&page="
                    + currentPage + "&filter=" + currentFilter);
            return;
        }

        Room oldRoom = roomDAO.getRoomById(roomId);

        boolean active = request.getParameter("active") != null;

        // If layout changed but room has schedules → reject
        if (oldRoom != null
                && (oldRoom.getNumberOfRows() != numberOfRows
                    || oldRoom.getSeatsPerRow() != seatsPerRow)
                && scheduleDAO.hasSchedulesForRoom(roomId)) {
            response.sendRedirect("RoomServlet?action=edit&id="
                    + roomId + "&error=has_schedules&page="
                    + currentPage + "&filter=" + currentFilter);
            return;
        }

        Room room = new Room();
        room.setRoomId(roomId);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setNumberOfRows(numberOfRows);
        room.setSeatsPerRow(seatsPerRow);
        room.setActive(active);

        roomDAO.updateRoom(room);

        if (oldRoom != null
                && (oldRoom.getNumberOfRows() != numberOfRows
                    || oldRoom.getSeatsPerRow() != seatsPerRow)) {
            if (seatDAO.deleteSeatsByRoom(roomId)) {
                seatDAO.generateSeats(roomId, numberOfRows, seatsPerRow);
            } else {
                response.sendRedirect("RoomServlet?action=edit&id="
                        + roomId + "&error=cannot_change_layout&page="
                        + currentPage + "&filter=" + currentFilter);
                return;
            }
        }

        response.sendRedirect("RoomServlet?page=" + currentPage
                + "&filter=" + currentFilter);
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

        String currentPage = request.getParameter("page");
        if (currentPage == null || currentPage.isEmpty()) {
            currentPage = "1";
        }
        String currentFilter = request.getParameter("filter");
        if (currentFilter == null || currentFilter.isEmpty()) {
            currentFilter = "active";
        }

        // Block deactivation if room has non-cancelled schedules
        if (scheduleDAO.hasSchedulesForRoom(roomId)) {
            response.sendRedirect("RoomServlet?page=" + currentPage
                    + "&filter=" + currentFilter + "&error=room_has_schedules");
            return;
        }

        // Deactivate room in database
        roomDAO.deleteRoom(roomId);

        response.sendRedirect("RoomServlet?page=" + currentPage
                + "&filter=" + currentFilter);
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

        // Check if room has schedules (layout cannot be changed)
        boolean hasSchedules = scheduleDAO.hasSchedulesForRoom(roomId);
        request.setAttribute("hasSchedules", hasSchedules);

        // Send room data to JSP
        request.setAttribute("room", room);

        // Forward to edit page
        request.getRequestDispatcher("/view/manager/room-edit.jsp")
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
