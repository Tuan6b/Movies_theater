package com.cinema.util;

public class GoogleOAuthConfig {

    public static final String CLIENT_ID = "900589394450-iu48et791q5ibg09n1vepdrus59m5j29.apps.googleusercontent.com";
    public static final String CLIENT_SECRET = "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";
    public static final String REDIRECT_URI = "http://localhost:8080/MoviesTheater/LoginGoogle/callback";
    public static final String SCOPE = "openid email profile";

    public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
}
