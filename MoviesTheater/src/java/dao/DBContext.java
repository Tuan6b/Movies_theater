package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for managing database connections to SQL Server.
 * All Data Access Object (DAO) classes in the system should inherit from this class
 * to share a single Connection object and avoid redundant connection logic.
 * 
 * @author CuongPVHE204336
 * @version 1.0 24/05/2026
 */
public class DBContext {
    
    protected Connection connection;

    /**
     * Initializes the connection to the SQL Server immediately when a DAO object is instantiated.
     */
    public DBContext() {
        try {
            // SQL Server name and password
            String user = "sa";
            String pass = "123456";
            
            // Connection string pointing to the CinemaBookingDB database
            String url = "jdbc:sqlserver://localhost:1433;databaseName=CinemaBookingDB;encrypt=true;trustServerCertificate=true";
            
            // Load the Microsoft SQL Server JDBC Driver into memory
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // Establish the connection using DriverManager
            connection = DriverManager.getConnection(url, user, pass);
        }
        catch (ClassNotFoundException | SQLException ex) {
            // Log the severe error trace if the driver is missing or credentials are wrong
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("CSDL Connection Error: " + ex.getMessage());
        }
    }
}
