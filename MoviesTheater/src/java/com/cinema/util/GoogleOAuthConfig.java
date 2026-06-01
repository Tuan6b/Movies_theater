package com.cinema.util;

public class GoogleOAuthConfig {

    public static final String CLIENT_ID = System.getenv().getOrDefault("GOOGLE_CLIENT_ID", "");
    public static final String CLIENT_SECRET = System.getenv().getOrDefault("GOOGLE_CLIENT_SECRET", "");
    public static final String REDIRECT_URI = "http://localhost:8080/MoviesTheater/LoginGoogle/callback";
    public static final String SCOPE = "openid email profile";

    public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
}
