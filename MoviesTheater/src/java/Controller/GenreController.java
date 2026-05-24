package Controller;

import dao.GenreDAO;
import model.Genre;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author vjphoalac
 */
public class GenreController extends HttpServlet {
   
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        GenreDAO dao = new GenreDAO();
        List<Genre> list = dao.getAllGenres();
        
        request.setAttribute("genreList", list);
        request.getRequestDispatcher("/views/admin/genre.jsp").forward(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        GenreDAO dao = new GenreDAO();
        
        try {
            if ("add".equals(action)) {
                String genreName = request.getParameter("genreName");
                if (genreName == null || genreName.trim().isEmpty()) {
                    request.setAttribute("error", "Genre name cannot be empty.");
                } else {
                    dao.addGenre(genreName.trim());
                    request.setAttribute("success", "Genre added successfully.");
                }
            } else if ("edit".equals(action)) {
                int genreID = Integer.parseInt(request.getParameter("genreID"));
                String newName = request.getParameter("genreName");
                
                if (newName == null || newName.trim().isEmpty()) {
                    request.setAttribute("error", "Tên thể loại không được để trống!");
                } else {
                    dao.updateGenre(genreID, newName.trim());
                    request.setAttribute("success", "Cập nhật thể loại thành công!");
                }    
            } else if ("delete".equals(action)) {
                int genreID = Integer.parseInt(request.getParameter("genreID"));
                dao.deleteGenre(genreID);
                request.setAttribute("success", "Genre deleted successfully.");
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }
        
        doGet(request, response);
    }
}
