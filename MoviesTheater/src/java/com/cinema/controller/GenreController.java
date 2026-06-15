package com.cinema.controller;

import com.cinema.dao.GenreDAO;
import com.cinema.model.Genre;
import java.util.List;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller servlet handling all HTTP requests related to Genre management.
 * Follows the MVC pattern by processing data via GenreDAO and forwarding to JSP views.
 * 
 * @author CuongPVHE204336
 * @version 1.0 24/05/2026
 */
public class GenreController extends HttpServlet {
   
    /** 
     * Handles the HTTP <code>GET</code> method.
     * Used for retrieving data and displaying the genre management page.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        // Initialize the DAO object to interact with the database.
        GenreDAO dao = new GenreDAO();
        
        // Get the complete list of movie genre from database.
        List<Genre> list = dao.getAllGenres();
        
        // Attach the retrieved list to the request scope for the JSP to render.
        request.setAttribute("genreList", list);
        
        // Forward the request to the JSP Page.
        request.getRequestDispatcher("/views/admin/genre.jsp").forward(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * Used for processing form submissions (Add, Edit, Delete)
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        // Retrieve the "action" paramenter from the hidden input field.
        String action = request.getParameter("action");
        GenreDAO dao = new GenreDAO();
        
        // Process logic based on action: Use a try-catch block to catch exceptions.
        try {
            // "add" case.
            if ("add".equals(action)) {
                
                // Retrieve the user input from the text field.
                String genreName = request.getParameter("genreName");
                
                // Make sure user input is not null or empty.
                if (genreName == null || genreName.trim().isEmpty()) {
                    request.setAttribute("error", "Genre name cannot be empty.");
                } else {
                    // Call DAO method to add new genre into the database.
                    dao.addGenre(genreName.trim());
                    request.setAttribute("success", "Genre added successfully.");
                }
            
            // "edit" case.
            } else if ("edit".equals(action)) {
                
                // Parse ID from String to integer.
                int genreID = Integer.parseInt(request.getParameter("genreID"));
                String newName = request.getParameter("genreName");
                
                // Check the validility of the new name.
                if (newName == null || newName.trim().isEmpty()) {
                    request.setAttribute("error", "Tên thể loại không được để trống!");
                } else {
                    dao.updateGenre(genreID, newName.trim());
                    request.setAttribute("success", "Cập nhật thể loại thành công!");
                }    
                
            // "delete" case.
            } else if ("delete".equals(action)) {
                int genreID = Integer.parseInt(request.getParameter("genreID"));
                dao.deleteGenre(genreID);
                request.setAttribute("success", "Genre deleted successfully.");
            }
        } catch (Exception e) {
            // Catch custom exceptions and display the message to the user.
            request.setAttribute("error", e.getMessage());
        }
        
        /*
         * Forwarding back to doGet() make the page to reload the updated list
         * and display the messages stored in the request attributes.
         */
        doGet(request, response);
    }
}
