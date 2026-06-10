<%-- 
    Document   : seat-layout
    Created on : Jun 10, 2026, 11:40:03 PM
    Author     : Tuan Phong Nguyen
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">

    <head>

        <meta charset="UTF-8">

        <title>
            Seat Layout
        </title>

        <style>

            body{
                font-family: Arial;
                padding: 30px;
                background: #f5f5f5;
            }

            /*
             * Seat grid container
             */
            .seat-grid{
                display: flex;
                flex-direction: column;
                gap: 10px;
            }

            /*
             * Each seat row
             */
            .seat-row{
                display: flex;
                gap: 10px;
            }

            /*
             * Base seat style
             */
            .seat{

                width: 60px;
                height: 60px;

                display: flex;
                justify-content: center;
                align-items: center;

                border-radius: 8px;

                color: white;
                font-weight: bold;

                cursor: pointer;
            }

            /*
             * Normal seat
             */
            .normal{
                background: #3498db;
            }

            /*
             * VIP seat
             */
            .vip{
                background: #f39c12;
            }

            /*
             * Couple seat
             */
            .couple{
                background: #9b59b6;
                width: 120px;
            }

            /*
             * Disabled seat
             */
            .inactive{
                background: #7f8c8d;
            }

            /*
             * Cinema screen
             */
            .screen{

                width: 500px;
                height: 40px;

                background: black;
                color: white;

                display: flex;
                justify-content: center;
                align-items: center;

                margin-bottom: 40px;

                border-radius: 10px;
            }

        </style>

    </head>

    <body>

        <h1>
            Seat Layout
        </h1>

        <!-- Cinema screen -->
        <div class="screen">
            SCREEN
        </div>

        <!-- Seat grid -->
        <div class="seat-grid">

            <c:set var="currentRow" value="" />

            <c:forEach var="seat" items="${seatList}">

                <!-- Create new row -->
                <c:if test="${currentRow ne seat.rowChar}">

                    <c:if test="${currentRow ne ''}">
                    </div>
                </c:if>

                <div class="seat-row">

                    <c:set var="currentRow"
                           value="${seat.rowChar}" />

                </c:if>

                <!-- Seat item -->
                <form action="SeatController"
                      method="post">

                    <!-- Action name -->
                    <input type="hidden"
                           name="action"
                           value="update">

                    <!-- Seat ID -->
                    <input type="hidden"
                           name="seatId"
                           value="${seat.seatId}">

                    <!-- Room ID -->
                    <input type="hidden"
                           name="roomId"
                           value="${seat.roomId}">

                    <div class="seat
                         ${seat.seatType eq 'VIP' ? 'vip' : ''}
                         ${seat.seatType eq 'Couple' ? 'couple' : ''}
                         ${seat.seatType eq 'Normal' ? 'normal' : ''}
                         ${!seat.active ? 'inactive' : ''}">

                        <!-- Seat label -->
                        <div>
                            ${seat.rowChar}${seat.colNumber}
                        </div>

                        <!-- Seat type -->
                        <select name="seatType">

                            <option value="Normal"
                                    ${seat.seatType eq 'Normal'
                                      ? 'selected' : ''}>
                                Normal
                            </option>

                            <option value="VIP"
                                    ${seat.seatType eq 'VIP'
                                      ? 'selected' : ''}>
                                VIP
                            </option>

                            <option value="Couple"
                                    ${seat.seatType eq 'Couple'
                                      ? 'selected' : ''}>
                                Couple
                            </option>

                        </select>

                        <!-- Seat active status -->
                        <label>

                            <input type="checkbox"
                                   name="active"
                                   ${seat.active ? 'checked' : ''}>

                            Active

                        </label>

                        <!-- Save button -->
                        <button type="submit">
                            Save
                        </button>

                    </div>

                </form>

            </c:forEach>

        </div>

    </div>

</body>
</html>
