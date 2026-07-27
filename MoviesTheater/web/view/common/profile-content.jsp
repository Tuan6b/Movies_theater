<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    com.cinema.model.Account acc = (com.cinema.model.Account) session.getAttribute("account");
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError = (String) session.getAttribute("flashError");
    if (flashSuccess != null) { session.removeAttribute("flashSuccess"); }
    if (flashError != null) { session.removeAttribute("flashError"); }
%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">

<% if (flashSuccess != null) { %>
<div class="flash flash-success"><%= flashSuccess %></div>
<% } %>
<% if (flashError != null) { %>
<div class="flash flash-error"><%= flashError %></div>
<% } %>

<div class="profile-card">
    <div class="profile-sidebar">
        <div class="profile-avatar-section">
            <div class="profile-avatar">
                <%
                    String avatarUrl = acc != null ? acc.getAvatarUrl() : null;
                    if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                %>
                <img src="${pageContext.request.contextPath}/<%= avatarUrl %>" alt="avatar">
                <%
                    } else {
                %>
                <%= acc != null && acc.getFullName() != null ? Character.toUpperCase(acc.getFullName().charAt(0)) : 'U' %>
                <% } %>
            </div>
            <form action="${pageContext.request.contextPath}/avatar-upload" method="post" enctype="multipart/form-data" id="avatarForm">
                <label class="avatar-upload-btn">
                    <input type="file" name="avatar" accept="image/jpeg,image/png,image/gif" onchange="document.getElementById('avatarForm').submit()">
                    Đổi ảnh
                </label>
            </form>
        </div>
        <div class="profile-meta">
            <div class="meta-role"><%= acc != null ? acc.getRoleName() : "Khách" %></div>
            <div class="meta-email"><%= acc != null ? acc.getEmail() : "" %></div>
        </div>
    </div>

    <div class="profile-main">
        <div class="profile-header-row">
            <h2 class="profile-title">Hồ sơ của tôi</h2>
            <button type="button" class="btn-edit" onclick="openModal()">Chỉnh sửa hồ sơ</button>
        </div>

        <div class="profile-info-grid">
            <div class="info-item">
                <span class="info-label">Họ và tên</span>
                <span class="info-value"><%= acc != null ? acc.getFullName() : "" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Email</span>
                <span class="info-value"><%= acc != null ? acc.getEmail() : "" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Số điện thoại</span>
                <span class="info-value"><%= acc != null && acc.getPhoneNumber() != null ? acc.getPhoneNumber() : "Chưa cập nhật" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Ngày sinh</span>
                <span class="info-value"><%= acc != null && acc.getDateOfBirth() != null ? acc.getDateOfBirth() : "Chưa cập nhật" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Địa chỉ</span>
                <span class="info-value"><%= acc != null && acc.getAddress() != null ? acc.getAddress() : "Chưa cập nhật" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Vai trò</span>
                <span class="info-value role-badge"><%= acc != null ? acc.getRoleName() : "" %></span>
            </div>
        </div>

        <div class="profile-actions-row">
            <a href="${pageContext.request.contextPath}/change-password" class="btn-outline">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                Đổi mật khẩu
            </a>
            <button type="button" class="btn-outline btn-outline-danger" onclick="openDeleteModal()">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                Yêu cầu xóa tài khoản
            </button>
        </div>
    </div>
</div>

<div class="modal-overlay" id="editModal" onclick="closeModalOutside(event)">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Chỉnh sửa hồ sơ</h3>
            <button type="button" class="modal-close" onclick="closeModal()">&times;</button>
        </div>
        <div class="edit-avatar-section">
            <div class="edit-avatar-preview-wrap">
                <%
                    String editAvatar = acc != null ? acc.getAvatarUrl() : null;
                %>
                <div class="edit-avatar-preview" id="editAvatarPreview">
                    <% if (editAvatar != null && !editAvatar.trim().isEmpty()) { %>
                    <img id="editAvatarImg" src="${pageContext.request.contextPath}/<%= editAvatar %>" alt="avatar">
                    <% } else { %>
                    <span id="editAvatarLetter"><%= acc != null && acc.getFullName() != null ? Character.toUpperCase(acc.getFullName().charAt(0)) : 'U' %></span>
                    <% } %>
                </div>
            </div>
            <div class="edit-avatar-options">
                <div class="upload-option-box url-only">
                    <span class="upload-option-icon">&#127760;</span>
                    <span class="upload-option-label">Từ URL</span>
                    <div class="url-input-row">
                        <input type="text" id="imageUrlInput" placeholder="https://..." class="url-input">
                        <button type="button" class="url-apply-btn" onclick="submitUrl()">Dùng</button>
                    </div>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/avatar-upload" id="modalUrlForm" style="display:none">
                    <input type="hidden" name="imageUrl" id="hiddenUrlInput">
                </form>
            </div>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/profile" class="modal-form">
            <div class="form-group">
                <label for="editName">Họ và tên</label>
                <input type="text" id="editName" name="fullName" value="<%= acc != null ? acc.getFullName() : "" %>" required>
            </div>
            <div class="form-group">
                <label for="editPhone">Số điện thoại</label>
                <input type="tel" id="editPhone" name="phone" value="<%= acc != null && acc.getPhoneNumber() != null ? acc.getPhoneNumber() : "" %>">
            </div>
            <div class="form-group">
                <label for="editDob">Ngày sinh</label>
                <input type="date" id="editDob" name="dob" value="">
            </div>
            <div class="form-group">
                <label for="editAddress">Địa chỉ</label>
                <input type="text" id="editAddress" name="address" value="">
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-cancel" onclick="closeModal()">Hủy</button>
                <button type="submit" class="btn-save">Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<div class="modal-overlay" id="deleteModal" onclick="closeModalOutside(event)">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Yêu cầu xóa tài khoản</h3>
            <button type="button" class="modal-close" onclick="closeDeleteModal()">&times;</button>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/deletion-request" class="modal-form">
            <p style="color: #666; font-size: 14px; margin: 0;">Khi tài khoản bị xóa, bạn sẽ không thể đăng nhập và đặt vé. Hành động này cần quản trị viên xác nhận.</p>
            <div class="form-group">
                <label for="deleteReason">Lý do xóa tài khoản</label>
                <textarea id="deleteReason" name="reason" rows="3" placeholder="Chia sẻ lý do bạn muốn xóa tài khoản..." required></textarea>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-cancel" onclick="closeDeleteModal()">Hủy</button>
                <button type="submit" class="btn-save btn-danger">Gửi yêu cầu</button>
            </div>
        </form>
    </div>
</div>

<script>
function openModal() {
    document.getElementById('editModal').classList.add('open');
}

function closeModal() {
    document.getElementById('editModal').classList.remove('open');
}

function closeModalOutside(e) {
    if (e.target === document.getElementById('editModal')) {
        closeModal();
    }
    if (e.target === document.getElementById('deleteModal')) {
        closeDeleteModal();
    }
}

function openDeleteModal() {
    document.getElementById('deleteModal').classList.add('open');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.remove('open');
}

function submitUrl() {
    var url = document.getElementById('imageUrlInput').value.trim();
    if (!url) return;
    if (!url.match(/^https?:\/\//)) {
        alert('Vui lòng nhập URL hợp lệ (bắt đầu bằng http:// hoặc https://)');
        return;
    }
    var preview = document.getElementById('editAvatarPreview');
    preview.innerHTML = '<img src="' + url + '" alt="avatar">';
    document.getElementById('hiddenUrlInput').value = url;
    document.getElementById('modalUrlForm').submit();
}

</script>
