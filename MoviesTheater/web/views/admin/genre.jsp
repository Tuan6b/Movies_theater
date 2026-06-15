<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Manage Movie Genre</title>
    </head>
    <body>
        <h2>Genre Management</h2>

        <%-- 
        - SYSTEM MESSAGES SECTION
        - Displays success or error messages passed down from the GenreController.
        - Uses JSTL <c:if> to check if the attributes exist before rendering the paragraph tags.
        --%>
        <c:if test="${not empty error}">
            <p class="error">${error}</p>
        </c:if>
        <c:if test="${not empty success}">
            <p class="success">${success}</p>
        </c:if>

        <%-- 
        - ADD SECTION
        - Form for adding a new movie genre.
        - Sends a POST request with a hidden 'action' parameter set to 'add'.
        --%>
        <fieldset>
            <legend>Add New Genre</legend>
            <form action="${pageContext.request.contextPath}/admin/genre" method="POST">
                <input type="hidden" name="action" value="add">
                <input type="text" name="genreName" required placeholder="Input genre name...">
                <button type="submit">Add</button>
            </form>
        </fieldset>

        <br>

        <%-- 
        - READ & UPDATE & DELETE SECTION
        - Data table displaying all available genres.
        - Iterates through the 'genreList' provided by the GET request in GenreController.
        --%>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Genre Name</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${genreList}" var="g">
                    <tr>
                        <td>${g.genreID}</td>
                        <td>${g.genreName}</td>
                        <td>

                            <%-- 
                            - UPDATE FORM (Inline)
                            - Allows managers to update the genre name directly on the row.
                            - Prepends the current genre name into the input value.
                            --%>
                            <form action="${pageContext.request.contextPath}/admin/genre" method="POST" style="display:inline;">
                                <input type="hidden" name="action" value="edit">
                                <input type="hidden" name="genreID" value="${g.genreID}">
                                <input type="text" name="genreName" value="${g.genreName}" required>
                                <button type="submit">Update</button>
                            </form>

                            <%-- 
                            - DELETE FORM
                            - Uses JavaScript 'onsubmit' to prompt a confirmation dialog 
                            - to prevent accidental deletions by the manager.
                            --%>
                            <form action="${pageContext.request.contextPath}/admin/genre" method="POST" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa thể loại này không?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="genreID" value="${g.genreID}">
                                <button type="submit">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </body>
</html>
