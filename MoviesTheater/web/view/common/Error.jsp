<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>CGV Cinema - Error</title>
</head>
<body>
    <h1 style="color: red;"> An Error Occurred</h1>
    <p>We are sorry, but the application encountered an error while processing your request.</p>
    
    <%
        String errorMessage = (String) request.getAttribute("ERROR");
        if (errorMessage == null) {
            errorMessage = "Unknown system error. Please return to the homepage and try again.";
        }
    %>
    <div style="background-color: #ffe6e6; border: 1px solid #ff9999; padding: 15px; border-radius: 8px; font-weight: bold; color: #cc0000;">
        <%= errorMessage %>
    </div>
    
    <br/><br/>
    <a href="${pageContext.request.contextPath}/">Return to Homepage</a>
</body>
</html>
