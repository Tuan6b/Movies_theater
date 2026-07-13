<%-- Notification bell + right-side popup panel. Include inside cgv-header-right,
     before cgv-user-wrap. Notification text is in English per team decision. --%>
<%@ page pageEncoding="UTF-8" %>
<div class="cgv-notif" id="cgvNotif" data-base="${pageContext.request.contextPath}/employee/notifications">
    <button type="button" class="cgv-bell-btn" id="cgvNotifBellBtn"
            aria-label="Notifications" aria-haspopup="true" aria-expanded="false">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" stroke-linejoin="round" width="20" height="20">
            <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <span class="cgv-notif-badge" id="cgvNotifBadge" style="display:none;">0</span>
    </button>

    <div class="cgv-notif-panel" id="cgvNotifPanel">
        <div class="cgv-notif-panel-header">
            <span>Notifications</span>
            <button type="button" class="cgv-notif-markall" id="cgvNotifMarkAll">Mark all as read</button>
        </div>
        <div class="cgv-notif-list" id="cgvNotifList">
            <div class="cgv-notif-empty">No notifications yet</div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/notifications.js"></script>
