<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% request.setAttribute("activeNav", "staff"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý nhân viên — CGV System Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .toolbar { display:flex; gap:12px; margin-bottom:20px; flex-wrap:wrap; align-items:center; }
        .search-input { flex:1; min-width:200px; padding:10px 14px; border:1px solid var(--cgv-border); border-radius:8px; font-size:14px; outline:none; }
        .search-input:focus { border-color:var(--cgv-red); }
        .toolbar-btn { padding:8px 16px; border:1px solid var(--cgv-border); border-radius:8px; background:#fff; font-size:13px; font-weight:600; cursor:pointer; transition:all 0.2s; text-decoration:none; color:var(--cgv-dark); display:inline-flex; align-items:center; gap:6px; }
        .toolbar-btn:hover { border-color:var(--cgv-red); color:var(--cgv-red); }
        .toolbar-btn.active { background:var(--cgv-red); color:#fff; border-color:var(--cgv-red); }
        .toolbar-btn.primary { background:var(--cgv-red); color:#fff; border-color:var(--cgv-red); }
        .toolbar-btn.primary:hover { background:var(--cgv-red-dark); }
        .toolbar-btn.danger { color:#b91c1c; border-color:#fecaca; }
        .toolbar-btn.danger:hover { background:#fef2f2; border-color:#b91c1c; }
        .role-badge { display:inline-block; padding:2px 10px; border-radius:12px; font-size:11px; font-weight:700; }
        .role-employee { background:#fff3e0; color:#e65100; }
        .role-manager { background:#e3f2fd; color:#1565c0; }
        .role-admin { background:#fce4ec; color:#c62828; }
        table { width:100%; border-collapse:collapse; font-size:14px; }
        table thead tr { background:#fafafa; border-bottom:2px solid var(--cgv-border); }
        table th { padding:12px; text-align:left; white-space:nowrap; user-select:none; }
        table th.sortable { cursor:pointer; }
        table th.sortable:hover { color:var(--cgv-red); }
        table th .sort-icon { opacity:0.3; margin-left:4px; font-size:11px; }
        table th.sortable.active .sort-icon { opacity:1; color:var(--cgv-red); }
        table td { padding:12px; }
        table tbody tr { border-bottom:1px solid var(--cgv-border); transition:background 0.15s; }
        table tbody tr:hover { background:#fafafa; }
        .avatar-cell { display:flex; align-items:center; gap:10px; }
        .avatar-thumb { width:32px; height:32px; border-radius:50%; background:var(--cgv-red); color:#fff; font-size:13px; font-weight:700; display:flex; align-items:center; justify-content:center; overflow:hidden; flex-shrink:0; }
        .avatar-thumb img { width:100%; height:100%; object-fit:cover; }
        .action-btn { padding:5px 10px; border:none; border-radius:5px; font-size:12px; font-weight:600; cursor:pointer; transition:all 0.15s; }
        .action-btn.view { background:#e8f5e9; color:#2e7d32; }
        .action-btn.view:hover { background:#c8e6c9; }
        .action-btn.lock { background:#fef2f2; color:#b91c1c; }
        .action-btn.lock:hover { background:#fecaca; }
        .action-btn.unlock { background:#f0fdf4; color:#16a34a; }
        .action-btn.unlock:hover { background:#bbf7d0; }
        .action-btn.role { background:#e3f2fd; color:#1565c0; }
        .action-btn.role:hover { background:#bbdefb; }
        .stat-cards { display:grid; grid-template-columns:repeat(auto-fit,minmax(150px,1fr)); gap:12px; margin-bottom:24px; }
        .stat-card { background:#fff; border:1px solid var(--cgv-border); border-radius:8px; padding:16px; text-align:center; }
        .stat-card .num { font-size:24px; font-weight:700; }
        .stat-card .label { font-size:11px; color:var(--cgv-text-muted); text-transform:uppercase; margin-top:4px; }
        .batch-bar { display:none; align-items:center; gap:12px; padding:10px 16px; background:#f0fdf4; border:1px solid #86efac; border-radius:8px; margin-bottom:16px; font-size:14px; }
        .batch-bar.show { display:flex; }
        .batch-bar .count { font-weight:700; color:#166534; }
    </style>
</head>
<body class="cgv-body">
<%@ include file="_sidebar.jsp" %>
<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Quản lý nhân viên</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">SA</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
            </div>
        </div>
    </header>
    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <c:if test="${not empty roleStats}">
            <div class="stat-cards">
                <c:forEach var="entry" items="${roleStats}">
                    <div class="stat-card">
                        <div class="num">${entry.value}</div>
                        <div class="label">${entry.key}</div>
                    </div>
                </c:forEach>
            </div>
            </c:if>

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/staff" style="display:contents;">
                    <input type="text" name="q" class="search-input" placeholder="Tìm kiếm theo tên hoặc email..." value="${param.q}">
                    <input type="hidden" name="status" value="${param.status}">
                    <input type="hidden" name="sortBy" value="${param.sortBy}">
                    <input type="hidden" name="sortOrder" value="${param.sortOrder}">
                    <button type="submit" class="toolbar-btn primary">Tìm kiếm</button>
                </form>

                <a href="${pageContext.request.contextPath}/admin/staff?status=blocked${not empty param.q ? '&q='.concat(param.q) : ''}${not empty param.sortBy ? '&sortBy='.concat(param.sortBy).concat('&sortOrder=').concat(param.sortOrder) : ''}"
                   class="toolbar-btn ${param.status eq 'blocked' ? 'active' : ''}">Đã khoá</a>
                <a href="${pageContext.request.contextPath}/admin/staff?status=active${not empty param.q ? '&q='.concat(param.q) : ''}${not empty param.sortBy ? '&sortBy='.concat(param.sortBy).concat('&sortOrder=').concat(param.sortOrder) : ''}"
                   class="toolbar-btn ${param.status eq 'active' ? 'active' : ''}">Hoạt động</a>
                <a href="${pageContext.request.contextPath}/admin/staff" class="toolbar-btn">Tất cả</a>

                <form method="post" action="${pageContext.request.contextPath}/admin/staff" id="exportCsvForm" style="display:inline;">
                    <input type="hidden" name="actionType" value="export-csv">
                    <input type="hidden" name="q" value="${param.q}">
                    <input type="hidden" name="status" value="${param.status}">
                    <button type="submit" class="toolbar-btn" style="margin-left:auto;">Xuất CSV</button>
                </form>
                <a href="${pageContext.request.contextPath}/admin/create-account" class="toolbar-btn" style="color:var(--cgv-red);">+ Tạo tài khoản</a>
            </div>

            <div class="batch-bar" id="batchBar">
                <label><input type="checkbox" id="selectAll" onchange="toggleAll()"> Chọn tất cả</label>
                <span class="count" id="selectedCount">0</span> nhân viên được chọn
                <form method="post" action="${pageContext.request.contextPath}/admin/staff" id="batchForm" style="display:inline;">
                    <input type="hidden" name="action" id="batchAction">
                    <input type="hidden" name="q" value="${param.q}">
                    <input type="hidden" name="status" value="${param.status}">
                    <input type="hidden" name="sortBy" value="${param.sortBy}">
                    <input type="hidden" name="sortOrder" value="${param.sortOrder}">
                    <button type="button" class="toolbar-btn danger" onclick="submitBatch('batch-block')">Khoá</button>
                    <button type="button" class="toolbar-btn" onclick="submitBatch('batch-unblock')">Mở Khoá</button>
                </form>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width:40px;"><input type="checkbox" id="selectAllHeader" onchange="toggleAll()"></th>
                        <th>ID</th>
                        <th class="sortable" onclick="sortBy('name')">NGƯỜI DÙNG<span class="sort-icon">${sortBy eq 'name' ? (sortOrder eq 'DESC' ? '&#9660;' : '&#9650;') : '&#9650;&#9660;'}</span></th>
                        <th class="sortable" onclick="sortBy('email')">EMAIL<span class="sort-icon">${sortBy eq 'email' ? (sortOrder eq 'DESC' ? '&#9660;' : '&#9650;') : '&#9650;&#9660;'}</span></th>
                        <th class="sortable" onclick="sortBy('phone')">SỐ ĐIỆN THOẠI<span class="sort-icon">${sortBy eq 'phone' ? (sortOrder eq 'DESC' ? '&#9660;' : '&#9650;') : '&#9650;&#9660;'}</span></th>
                        <th>VAI TRÒ</th>
                        <th>TRẠNG THÁI</th>
                        <th>NGÀY TẠO</th>
                        <th>THAO TÁC</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${staff}">
                    <tr style="${u.isBlocked ? 'opacity:0.6;background:#fef2f2;' : ''}"
                        data-id="${u.accountId}"
                        data-name="${u.fullName}"
                        data-email="${u.email}"
                        data-phone="${u.phoneNumber}"
                        data-role="${u.roleName}"
                        data-status="${u.isBlocked ? 'Bị khóa' : 'Hoạt động'}"
                        data-created="${u.createdAt}"
                        data-avatar="${u.avatarUrl}">
                        <td><input type="checkbox" class="user-checkbox" value="${u.accountId}" onchange="updateBatchBar()"></td>
                        <td>${u.accountId}</td>
                        <td>
                            <div class="avatar-cell">
                                <div class="avatar-thumb">
                                    <c:choose>
                                        <c:when test="${not empty u.avatarUrl}">
                                            <img src="${pageContext.request.contextPath}/${u.avatarUrl}" alt="avatar">
                                        </c:when>
                                        <c:otherwise>
                                            ${fn:substring(u.fullName, 0, 1)}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                ${u.fullName}
                            </div>
                        </td>
                        <td>${u.email}</td>
                        <td>${not empty u.phoneNumber ? u.phoneNumber : '—'}</td>
                        <td>
                            <c:choose>
                                <c:when test="${u.roleId == 3}"><span class="role-badge role-employee">Employee</span></c:when>
                                <c:when test="${u.roleId == 4}"><span class="role-badge role-manager">Manager</span></c:when>
                                <c:when test="${u.roleId == 5}"><span class="role-badge role-admin">Admin</span></c:when>
                                <c:otherwise>Unknown</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${u.isBlocked}"><span style="color:#b91c1c;font-weight:600;">Bị khóa</span></c:when>
                                <c:otherwise><span style="color:#16a34a;font-weight:600;">Hoạt động</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${u.createdAt}</td>
                        <td style="display:flex;gap:6px;flex-wrap:wrap;">
                            <button type="button" class="action-btn view" onclick="viewStaff(this)">Xem</button>
                            <form method="post" action="${pageContext.request.contextPath}/admin/staff" style="display:inline;">
                                <input type="hidden" name="userId" value="${u.accountId}">
                                <input type="hidden" name="q" value="${param.q}">
                                <input type="hidden" name="status" value="${param.status}">
                                <input type="hidden" name="sortBy" value="${param.sortBy}">
                                <input type="hidden" name="sortOrder" value="${param.sortOrder}">
                                <c:choose>
                                    <c:when test="${u.isBlocked}">
                                        <button type="submit" name="action" value="unblock" class="action-btn unlock">Mở Khoá</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" name="action" value="block" class="action-btn lock" onclick="return confirm('Khóa tài khoản ${u.email}?')">Khoá</button>
                                    </c:otherwise>
                                </c:choose>
                                <select name="roleId" style="padding:4px 6px;font-size:11px;border:1px solid var(--cgv-border);border-radius:4px;">
                                    <option value="2" ${u.roleId == 2 ? 'selected' : ''}>Customer</option>
                                    <option value="3" ${u.roleId == 3 ? 'selected' : ''}>Employee</option>
                                    <option value="4" ${u.roleId == 4 ? 'selected' : ''}>Manager</option>
                                    <option value="5" ${u.roleId == 5 ? 'selected' : ''}>Admin</option>
                                </select>
                                <button type="submit" name="action" value="role" class="action-btn role">Đổi</button>
                            </form>
                        </td>
                    </tr>
                    </c:forEach>
                    <c:if test="${empty staff}">
                    <tr><td colspan="9" style="padding:40px;text-align:center;color:var(--cgv-text-muted);">Không có nhân viên nào.</td></tr>
                    </c:if>
                </tbody>
            </table>

        </div>
    </div>
</div>

<div class="modal-overlay" id="viewModal" onclick="closeViewModal(event)">
    <div class="modal-content view-modal-content">
        <div class="modal-header">
            <h3>Thông tin nhân viên</h3>
            <button type="button" class="modal-close" onclick="closeViewModal()">&times;</button>
        </div>
        <div class="view-modal-body">
            <div class="view-avatar-section">
                <div class="view-avatar" id="viewAvatar"></div>
                <div>
                    <div class="view-name" id="viewName"></div>
                    <div class="view-role" id="viewRole"></div>
                </div>
            </div>
            <div class="view-details">
                <div class="view-row">
                    <span class="view-label">ID</span>
                    <span class="view-value" id="viewId"></span>
                </div>
                <div class="view-row">
                    <span class="view-label">Email</span>
                    <span class="view-value" id="viewEmail"></span>
                </div>
                <div class="view-row">
                    <span class="view-label">Số điện thoại</span>
                    <span class="view-value" id="viewPhone"></span>
                </div>
                <div class="view-row">
                    <span class="view-label">Trạng thái</span>
                    <span class="view-value" id="viewStatus"></span>
                </div>
                <div class="view-row">
                    <span class="view-label">Ngày tạo</span>
                    <span class="view-value" id="viewCreated"></span>
                </div>
            </div>
        </div>
        <div class="modal-actions" style="padding:16px 24px;border-top:1px solid var(--cgv-border);">
            <button type="button" class="btn-cancel" onclick="closeViewModal()">Đóng</button>
        </div>
    </div>
</div>

<style>
.modal-overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,0.5); z-index:1000; align-items:center; justify-content:center; }
.modal-overlay.open { display:flex; }
.view-modal-content { background:#fff; border-radius:16px; width:100%; max-width:440px; box-shadow:0 20px 60px rgba(0,0,0,0.15); animation:modalIn 0.2s ease-out; overflow:hidden; }
@keyframes modalIn { from{opacity:0;transform:scale(0.95)translateY(10px)} to{opacity:1;transform:scale(1)translateY(0)} }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding:20px 24px 0; }
.modal-header h3 { font-size:18px; font-weight:700; color:var(--cgv-dark); margin:0; }
.modal-close { background:none; border:none; font-size:28px; color:#999; cursor:pointer; line-height:1; padding:0 4px; }
.modal-close:hover { color:var(--cgv-dark); }
.view-modal-body { padding:20px 24px; }
.view-avatar-section { display:flex; align-items:center; gap:16px; margin-bottom:24px; padding-bottom:20px; border-bottom:1px solid var(--cgv-border); }
.view-avatar { width:56px; height:56px; border-radius:50%; background:var(--cgv-red); color:#fff; font-size:24px; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; overflow:hidden; }
.view-avatar img { width:100%; height:100%; object-fit:cover; }
.view-name { font-size:18px; font-weight:700; color:var(--cgv-dark); }
.view-role { font-size:13px; color:var(--cgv-text-muted); margin-top:2px; }
.view-details { display:flex; flex-direction:column; gap:14px; }
.view-row { display:flex; align-items:center; }
.view-label { width:120px; font-size:12px; font-weight:600; color:var(--cgv-text-muted); text-transform:uppercase; letter-spacing:0.04em; flex-shrink:0; }
.view-value { font-size:14px; color:var(--cgv-dark); font-weight:500; }
.btn-cancel { padding:10px 24px; background:#f1f1f3; color:#666; border:none; border-radius:8px; font-weight:600; font-size:13px; cursor:pointer; }
.btn-cancel:hover { background:#e5e5e8; }
</style>

<script>
function viewStaff(btn) {
    var tr = btn.closest('tr');
    document.getElementById('viewId').textContent = tr.dataset.id;
    document.getElementById('viewName').textContent = tr.dataset.name;
    document.getElementById('viewEmail').textContent = tr.dataset.email;
    document.getElementById('viewPhone').textContent = tr.dataset.phone || '—';
    document.getElementById('viewRole').textContent = tr.dataset.role;
    document.getElementById('viewStatus').innerHTML = tr.dataset.status === 'Bị khóa'
        ? '<span style="color:#b91c1c;font-weight:600;">Bị khóa</span>'
        : '<span style="color:#16a34a;font-weight:600;">Hoạt động</span>';
    document.getElementById('viewCreated').textContent = tr.dataset.created;

    var avatar = document.getElementById('viewAvatar');
    var avatarUrl = tr.dataset.avatar;
    if (avatarUrl) {
        avatar.innerHTML = '<img src="${pageContext.request.contextPath}/' + avatarUrl + '" alt="avatar">';
    } else {
        avatar.textContent = tr.dataset.name ? tr.dataset.name.charAt(0).toUpperCase() : '?';
    }

    document.getElementById('viewModal').classList.add('open');
}

function closeViewModal(e) {
    if (!e || e.target === document.getElementById('viewModal')) {
        document.getElementById('viewModal').classList.remove('open');
    }
}

function toggleAll() {
    var checked = document.getElementById('selectAll').checked ||
                  document.getElementById('selectAllHeader').checked;
    document.querySelectorAll('.user-checkbox').forEach(function(cb) {
        cb.checked = checked;
    });
    document.getElementById('selectAll').checked = checked;
    document.getElementById('selectAllHeader').checked = checked;
    updateBatchBar();
}

function updateBatchBar() {
    var checked = document.querySelectorAll('.user-checkbox:checked');
    var bar = document.getElementById('batchBar');
    var count = document.getElementById('selectedCount');
    count.textContent = checked.length;
    bar.classList.toggle('show', checked.length > 0);
}

function submitBatch(action) {
    var checked = document.querySelectorAll('.user-checkbox:checked');
    if (checked.length === 0) return;
    if (action === 'batch-block' && !confirm('Khoá ' + checked.length + ' tài khoản?')) return;
    var form = document.getElementById('batchForm');
    checked.forEach(function(cb) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'userIds';
        input.value = cb.value;
        form.appendChild(input);
    });
    document.getElementById('batchAction').value = action;
    form.submit();
}

function sortBy(column) {
    var url = new URL(window.location.href);
    var current = url.searchParams.get('sortBy');
    var order = url.searchParams.get('sortOrder') || 'ASC';
    if (current === column) {
        order = order === 'ASC' ? 'DESC' : 'ASC';
    } else {
        order = 'ASC';
    }
    url.searchParams.set('sortBy', column);
    url.searchParams.set('sortOrder', order);
    window.location.href = url.toString();
}
</script>
</body>
</html>
