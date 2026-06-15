/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.listener;

import com.cinema.dao.PromotionDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author tuan6b
 */
@WebListener
public class PromotionStatusScheduler implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Run immediately at startup, then every hour to keep IsActive in sync with dates
        scheduler.scheduleAtFixedRate(() -> {
            try {
                promotionDAO.syncStatusByDates();
            } catch (SQLException e) {
                System.err.println("[PromotionStatusScheduler] Status sync failed: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
