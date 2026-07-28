package com.cinema.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing the Movie entity. Uses the cls prefix following the
 * project standards.
 *
 * @author TBinh
 */
public class clsMovie {

    private int movieId;
    private String movieName;
    private String description;
    private int duration;
    private java.sql.Timestamp dateAdded; // Thay thế releaseDate
    private java.sql.Date releaseDate;
    private String budget;
    private String globalBoxOffice;
    private int weeklyRevenueRank;
    private int ticketsSoldMilestone;
    private String poster;
    private String trailer;
    private String language;
    private String subtitle;
    private String director;
    private String cast;
    private String country;
    private int ageRestriction;
    private boolean isActive;

    /**
     * Default constructor.
     */
    public clsMovie() {
    }

    public clsMovie(int movieId, String movieName, String description, int duration, Timestamp dateAdded, java.sql.Date releaseDate, String budget, String globalBoxOffice, int weeklyRevenueRank, int ticketsSoldMilestone, String poster, String trailer, String language, String subtitle, String director, String cast, String country, int ageRestriction, boolean isActive) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.description = description;
        this.duration = duration;
        this.dateAdded = dateAdded;
        this.releaseDate = releaseDate;
        this.budget = budget;
        this.globalBoxOffice = globalBoxOffice;
        this.weeklyRevenueRank = weeklyRevenueRank;
        this.ticketsSoldMilestone = ticketsSoldMilestone;
        this.poster = poster;
        this.trailer = trailer;
        this.language = language;
        this.subtitle = subtitle;
        this.director = director;
        this.cast = cast;
        this.country = country;
        this.ageRestriction = ageRestriction;
        this.isActive = isActive;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Timestamp getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Timestamp dateAdded) {
        this.dateAdded = dateAdded;
    }

    public java.sql.Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(java.sql.Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getGlobalBoxOffice() {
        return globalBoxOffice;
    }

    public void setGlobalBoxOffice(String globalBoxOffice) {
        this.globalBoxOffice = globalBoxOffice;
    }

    public int getWeeklyRevenueRank() {
        return weeklyRevenueRank;
    }

    public void setWeeklyRevenueRank(int weeklyRevenueRank) {
        this.weeklyRevenueRank = weeklyRevenueRank;
    }

    public int getTicketsSoldMilestone() {
        return ticketsSoldMilestone;
    }

    public void setTicketsSoldMilestone(int ticketsSoldMilestone) {
        this.ticketsSoldMilestone = ticketsSoldMilestone;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
