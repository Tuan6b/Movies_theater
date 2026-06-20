/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility for Microsoft SQL Server.
 * Uses JDBC DriverManager with configurable connection parameters.
 *
 * @author tuan6b
 */
public class DBUtils {

    // Connection parameters — update these to match your environment
    static final String SERVER = "localhost";
    static final String PORT = "1433";
    private static final String DATABASE = "CinemaBookingDB";
    static final String USER = "sa";
    static final String PASSWORD = "123";

    private static final String CONNECTION_URL = "jdbc:sqlserver://"
            + SERVER + ":" + PORT
            + ";databaseName=" + DATABASE
            + ";encrypt=false";

    /**
     * Thread-local holding a shared test connection injected by integration tests.
     * When set, getConnection() returns a non-closeable proxy so that DAO
     * try-with-resources blocks cannot close the shared transaction connection.
     */
    private static final ThreadLocal<Connection> TEST_CONNECTION = new ThreadLocal<>();

    /**
     * Register a shared connection for the current thread (used by integration tests).
     * Call setTestConnection before each test and clearTestConnection in @AfterEach.
     *
     * @param conn an open connection with autoCommit=false
     */
    public static void setTestConnection(Connection conn) {
        TEST_CONNECTION.set(conn);
    }

    /**
     * Remove the test connection registration for the current thread.
     */
    public static void clearTestConnection() {
        TEST_CONNECTION.remove();
    }

    /**
     * Get a database connection.
     * In test mode (when a test connection is registered) returns a non-closeable
     * proxy around the shared connection so the test controls the transaction.
     * In production returns a fresh connection from DriverManager.
     *
     * @return Connection to SQL Server
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        Connection testConn = TEST_CONNECTION.get();
        if (testConn != null) {
            return unclosableProxy(testConn);
        }
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC driver not found", e);
        }
        return DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
    }

    /**
     * Wrap a real Connection in a JDK dynamic proxy that ignores close() calls.
     * All other method calls are delegated to the real connection unchanged.
     */
    private static Connection unclosableProxy(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{ Connection.class },
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
        );
    }
}
