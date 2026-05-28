package com.cinema.model;

/**
 * Represent the Movie Genre entity in the system.
 * Acts as a Model (Data Transfer Object) to transfer between the DAO, Controller and View layers.
 * 
 * @author CuongPVHE204336
 * @version 1.0 24/05/2026
 */
public class Genre {
    
    /*
     * Implementation details:
     * This class maps directly to the 'Genre' table structure in the SQL Server database.
     * The genreID attribute corresponds to the IDENTITY column (Primary Key).
     * The genreName attribute corresponds to the NVARCHAR column with a UNIQUE constraint.
     */
    
    // Unique indentifier of the movie genre (Primary Key).
    private int genreID;
    
    // The name of movie genre.
    private String genreName;

    // Default constructor.
    public Genre() {
    }

    /**
     * Parameterized constructor to instantiate an object with full information.
     *
     * @param genreID   The unique identifier of the genre (typically retrieved from the database).
     * @param genreName The name of the genre.
     */
    public Genre(int genreID, String genreName) {
        this.genreID = genreID;
        this.genreName = genreName;
    }

    /**
     * Retrieves the genre's identifier.
     *
     * @return An integer representing the genreID.
     */
    public int getGenreID() {
        return genreID;
    }

    /**
     * Updates the genre's identifier.
     *
     * @param genreID The new ID to assign.
     */
    public void setGenreID(int genreID) {
        this.genreID = genreID;
    }

    /**
     * Retrieves the name of the movie genre.
     *
     * @return A string containing the genre name.
     */
    public String getGenreName() {
        return genreName;
    }

    /**
     * Updates the name of the movie genre.
     *
     * @param genreName The new genre name to assign.
     */
    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }
}
