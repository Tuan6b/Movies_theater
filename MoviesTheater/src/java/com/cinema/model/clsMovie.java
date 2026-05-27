/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.model;

import java.sql.Date;

/**
 * Model class representing the Movie entity.
 * Uses the cls prefix following the project standards.
 *
 * @author TBinh
 */
public class clsMovie {

    private int movieId;
    private String movieName;
    private String description;
    private int duration;
    private Date releaseDate;
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

    /**
     * Full constructor.
     *
     * @param movieId ID of the movie
     * @param movieName Name of the movie
     * @param description Plot summary
     * @param duration Duration in minutes
     * @param releaseDate Release date
     * @param poster Poster image URL
     * @param trailer Trailer YouTube URL
     * @param language Spoken language
     * @param subtitle Subtitle language
     * @param director Movie director
     * @param cast Main cast members
     * @param country Production country
     * @param ageRestriction Minimum age limit
     * @param isActive Active status
     */
    public clsMovie(int movieId, String movieName, String description, int duration, Date releaseDate,
            String poster, String trailer, String language, String subtitle, String director,
            String cast, String country, int ageRestriction, boolean isActive) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.description = description;
        this.duration = duration;
        this.releaseDate = releaseDate;
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
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

    @Override
    public String toString() {
        return "clsMovie{" + "movieId=" + movieId + ", movieName=" + movieName + ", duration=" + duration + '}';
    }
}
