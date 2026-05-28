<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin - Manage Movie Genres</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <link href="${pageContext.request.contextPath}/css/main.css" rel="stylesheet">

    <link href="${pageContext.request.contextPath}/css/admin-custom.css" rel="stylesheet">
</head>
<body class="fade-up">

    <%-- ==========================================
         ADMIN NAVIGATION BAR
         ========================================== --%>
    <header class="site-header" style="height: 60px;">
        <a href="#" class="site-logo">
            <span class="site-logo-text" style="color: var(--primary);">CGV ADMIN DASHBOARD</span>
        </a>
        <nav class="site-nav d-none d-md-flex">
            <a href="${pageContext.request.contextPath}/admin/genre" class="active">Manage Genres</a>
            <a href="${pageContext.request.contextPath}/admin/movie">Manage Movies</a>
            <a href="${pageContext.request.contextPath}/admin/schedule">Schedules</a>
        </nav>
        <div class="site-header-actions">
            <span style="font-size: 14px; font-weight: 500; color: var(--on-surface-var);">Hello, Manager!</span>
        </div>
    </header>

    <%-- ==========================================
         MAIN CONTENT AREA
         ========================================== --%>
    <main class="container py-5 section fade-up" style="animation-delay: 0.1s;">
        
        <div class="section-header">
            <h2 class="section-title">Movie Genres List</h2>
            <p class="text-muted" style="font-size: 14px; color: var(--on-surface-var) !important;">
                Add, edit, or delete movie genres displayed on the system.
            </p>
        </div>

        <%-- 
            SYSTEM MESSAGES
            Handles error and success notifications sent from GenreController.
        --%>
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show border-0" role="alert" style="background-color: var(--primary-container); color: var(--primary-dark); border-radius: var(--r-md);">
                <strong>Error!</strong> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show border-0" role="alert" style="border-radius: var(--r-md);">
                <strong>Success!</strong> ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="row g-4 mt-2">
            
            <%-- ==========================================
                 LEFT COLUMN: ADD NEW GENRE FORM
                 ========================================== --%>
            <div class="col-lg-4">
                <div class="admin-card">
                    <h5 class="mb-4" style="font-family: var(--font-display); font-weight: 600;">Add New Genre</h5>
                    
                    <form action="${pageContext.request.contextPath}/admin/genre" method="POST">
                        <input type="hidden" name="action" value="add">
                        
                        <div class="mb-4">
                            <label class="form-label" style="font-size: 13px; font-weight: 500; color: var(--on-surface-var);">Genre Name *</label>
                            <input type="text" class="form-control" name="genreName" required placeholder="e.g., Action, Comedy...">
                        </div>
                        
                        <button type="submit" class="btn btn-primary w-100">
                            Add Genre
                        </button>
                    </form>
                </div>
            </div>

            <%-- ==========================================
                 RIGHT COLUMN: DATA TABLE
                 ========================================== --%>
            <div class="col-lg-8">
                <div class="admin-card">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead>
                                <tr>
                                    <th width="10%">ID</th>
                                    <th width="50%">Genre Name</th>
                                    <th width="40%" class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%-- Loop through the genreList passed from Controller --%>
                                <c:forEach items="${genreList}" var="g">
                                    <tr>
                                        <td style="color: var(--on-surface-var); font-weight: 500;">#${g.genreID}</td>
                                        
                                        <td>
                                            <form action="${pageContext.request.contextPath}/admin/genre" method="POST" class="d-flex align-items-center gap-2">
                                                <input type="hidden" name="action" value="edit">
                                                <input type="hidden" name="genreID" value="${g.genreID}">
                                                <input type="text" name="genreName" value="${g.genreName}" class="form-control" style="height: 32px; width: 200px;" required>
                                                <button type="submit" class="btn btn-outline btn-table">Edit</button>
                                            </form>
                                        </td>
                                        
                                        <td class="text-end">
                                            <form action="${pageContext.request.contextPath}/admin/genre" method="POST" class="d-inline-block" onsubmit="return confirm('Are you sure you want to delete this genre?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="genreID" value="${g.genreID}">
                                                <button type="submit" class="btn btn-ghost btn-table" style="color: var(--primary);">Delete</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>