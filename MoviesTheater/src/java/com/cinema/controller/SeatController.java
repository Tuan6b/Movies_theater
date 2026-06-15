/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.cinema.dao.SeatDAO;
import com.cinema.dao.RoomDAO;
import com.cinema.model.Seat;
import com.cinema.model.Room;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * SeatController handles seat management operations.
 *
 * @author Tuan Phong Nguyen
 */
public class SeatController extends HttpServlet {

    // DAO used for seat operations
    private final SeatDAO seatDAO = new SeatDAO();
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

            // Get action parameter
            String action = request.getParameter("action");

            // Default action
            if (action == null) {
                action = "view";
            }

            // Route request to corresponding method
            switch (action) {

                case "update":

                    updateSeat(request, response);
                    break;

                default:

                    viewSeatLayout(request, response);
                    break;
            }
        }
    }

    /**
     * Display seat layout of a specific room
     */
    private void viewSeatLayout(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int roomId = Integer.parseInt(request.getParameter("roomId"));

        List<Seat> seatList = seatDAO.getSeatsByRoom(roomId);
        Room room = roomDAO.getRoomById(roomId);

        request.setAttribute("seatList", seatList);
        request.setAttribute("room", room);

        request.getRequestDispatcher("seat-layout.jsp").forward(request, response);
    }

    /**
     * Update seat type and status
     */
    private void updateSeat(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        // Get seat ID
        int seatId = Integer.parseInt(
                request.getParameter("seatId"));

        // Get room ID
        int roomId = Integer.parseInt(
                request.getParameter("roomId"));

        // Get selected seat type
        String seatType
                = request.getParameter("seatType");

        /*
     * Checkbox handling:
     * checked -> active
     * unchecked -> inactive
         */
        boolean active
                = request.getParameter("active") != null;

        // Update seat in database
        seatDAO.updateSeat(
                seatId,
                seatType,
                active
        );

        // Reload seat layout
        response.sendRedirect(
                "SeatController?roomId="
                + roomId
        );
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
