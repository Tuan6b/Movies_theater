<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "movies"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Movie Catalog — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Movie Catalog</h1>
        <div class="cgv-header-right">
            <div class="cgv-search-wrap">
                <svg class="cgv-search-icon" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input class="cgv-search" type="text" placeholder="Search movies..."
                       value="${param.q}" name="q">
            </div>
            <div class="cgv-header-actions">
                <button class="cgv-bell-btn" title="Notifications">
                    <svg width="18" height="20" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                    </svg>
                </button>
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">
                                ${fn:substring(sessionScope.LOGIN_USER.fullName, 0, 2)}
                            </c:when>
                            <c:otherwise>MG</c:otherwise>
                        </c:choose>
                    </div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">${sessionScope.LOGIN_USER.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">

        <div class="cgv-list-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?filter=all"
                       class="cgv-pill ${empty param.filter || param.filter eq 'all' ? 'active' : ''}">All Genres</a>
                    <a href="?filter=showing"
                       class="cgv-pill ${param.filter eq 'showing' ? 'active' : ''}">Now Showing</a>
                    <a href="?filter=coming"
                       class="cgv-pill ${param.filter eq 'coming' ? 'active' : ''}">Coming Soon</a>
                </div>
                <a href="?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/>
                        <line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add New Movie
                </a>
            </div>

            <div class="cgv-movie-grid">
                <c:choose>
                    <c:when test="${not empty movieList}">
                        <c:forEach var="movie" items="${movieList}">
                            <div class="cgv-movie-card">
                                <div class="cgv-movie-thumb">
                                    <c:choose>
                                        <c:when test="${not empty movie.posterUrl}">
                                            <img src="${movie.posterUrl}" alt="${movie.title}">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="cgv-movie-thumb-empty">No poster</div>
                                        </c:otherwise>
                                    </c:choose>

                                    <span class="cgv-movie-status
                                        <c:choose>
                                            <c:when test="${movie.status eq 'NOW_SHOWING'}">now-showing</c:when>
                                            <c:when test="${movie.status eq 'COMING_SOON'}">coming-soon</c:when>
                                            <c:otherwise>hidden</c:otherwise>
                                        </c:choose>">
                                        <c:choose>
                                            <c:when test="${movie.status eq 'NOW_SHOWING'}">NOW SHOWING</c:when>
                                            <c:when test="${movie.status eq 'COMING_SOON'}">COMING SOON</c:when>
                                            <c:otherwise>HIDDEN</c:otherwise>
                                        </c:choose>
                                    </span>

                                    <div class="cgv-movie-overlay">
                                        <a href="?action=edit&id=${movie.movieId}"
                                           class="cgv-movie-overlay-btn" title="Edit">
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                                            </svg>
                                        </a>
                                        <a href="?action=delete&id=${movie.movieId}"
                                           class="cgv-movie-overlay-btn" title="Delete"
                                           onclick="return confirm('Delete this movie?')">
                                            <svg width="16" height="18" viewBox="0 0 24 24" fill="none"
                                                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                <polyline points="3 6 5 6 21 6"/>
                                                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                                                <path d="M10 11v6M14 11v6"/>
                                                <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                                            </svg>
                                        </a>
                                    </div>
                                </div>
                                <div>
                                    <div class="cgv-movie-title">${movie.title}</div>
                                    <div class="cgv-movie-meta">${movie.genre} • ${movie.duration}</div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <%-- Placeholder cards shown when no data is loaded --%>
                        <div class="cgv-movie-card">
                            <div class="cgv-movie-thumb">
                                <div class="cgv-movie-thumb-empty">No poster</div>
                                <span class="cgv-movie-status now-showing">NOW SHOWING</span>
                            </div>
                            <div>
                                <div class="cgv-movie-title">Interstellar Drift</div>
                                <div class="cgv-movie-meta">Sci-Fi • 2h 45m</div>
                            </div>
                        </div>
                        <div class="cgv-movie-card">
                            <div class="cgv-movie-thumb">
                                <div class="cgv-movie-thumb-empty">No poster</div>
                                <span class="cgv-movie-status coming-soon">COMING SOON</span>
                            </div>
                            <div>
                                <div class="cgv-movie-title">Neon Echoes</div>
                                <div class="cgv-movie-meta">Action/Crime • 1h 58m</div>
                            </div>
                        </div>
                        <div class="cgv-movie-card">
                            <div class="cgv-movie-thumb">
                                <div class="cgv-movie-thumb-empty">No poster</div>
                                <span class="cgv-movie-status hidden">HIDDEN</span>
                            </div>
                            <div>
                                <div class="cgv-movie-title">The Last Reel</div>
                                <div class="cgv-movie-meta">Documentary • 1h 20m</div>
                            </div>
                        </div>
                        <div class="cgv-movie-card">
                            <div class="cgv-movie-thumb">
                                <div class="cgv-movie-thumb-empty">No poster</div>
                                <span class="cgv-movie-status now-showing">NOW SHOWING</span>
                            </div>
                            <div>
                                <div class="cgv-movie-title">Aetheria Bound</div>
                                <div class="cgv-movie-meta">Fantasy • 2h 12m</div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">OVERVIEW</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty totalMovies ? totalMovies : '42'}</div>
                        <div class="cgv-stat-key">TOTAL LIBRARY</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty upcomingMovies ? upcomingMovies : '12'}</div>
                        <div class="cgv-stat-key">UPCOMING RELEASES</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty avgCapacity ? avgCapacity : '98%'}</div>
                        <div class="cgv-stat-key">AVG. CAPACITY</div>
                    </div>
                </div>
            </div>

            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">RECENT EVENTS</div>
                <div class="cgv-events-list">
                    <c:choose>
                        <c:when test="${not empty recentEvents}">
                            <c:forEach var="evt" items="${recentEvents}">
                                <div>
                                    <div class="cgv-event-title">${evt.title}</div>
                                    <div class="cgv-event-desc">${evt.description}</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">New Entry Added</div>
                                <div class="cgv-event-desc">"Neon Echoes" cataloged</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Archive Update</div>
                                <div class="cgv-event-desc">"The Last Reel" moved to hidden</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </aside>

    </div>
</div>

</body>
</html>
