/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBContext provides database connection for legacy DAOs extending it.
 */
public class DBContext {
    protected Connection connection;

    public DBContext() {
        initConnection();
    }

    private void initConnection() {
        try {
            connection = DBUtils.getConnection();
        } catch (Exception ex) {
            try {
                String user = "sa";
                String pass = "123";
                String url = "jdbc:sqlserver://localhost:1433;databaseName=CinemaBookingDB;encrypt=false";
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(url, user, pass);
            } catch (ClassNotFoundException | SQLException e) {
                Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("CSDL Connection Error: " + e.getMessage());
            }
        }
    }
}
