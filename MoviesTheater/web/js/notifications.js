// Bell icon + popup panel behavior for the employee notification widget.
// Expects markup from view/employee/_notifications.jsp on the page.
(function () {
    var wrap = document.getElementById('cgvNotif');
    if (!wrap) {
        return;
    }

    var baseUrl = wrap.dataset.base;
    var bellBtn = document.getElementById('cgvNotifBellBtn');
    var panel = document.getElementById('cgvNotifPanel');
    var badge = document.getElementById('cgvNotifBadge');
    var list = document.getElementById('cgvNotifList');
    var markAllBtn = document.getElementById('cgvNotifMarkAll');

    var POLL_INTERVAL_MS = 30000;

    function renderBadge(count) {
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : String(count);
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }

    function clearChildren(node) {
        while (node.firstChild) {
            node.removeChild(node.firstChild);
        }
    }

    function buildEmptyState() {
        var empty = document.createElement('div');
        empty.className = 'cgv-notif-empty';
        empty.textContent = 'No notifications yet';
        return empty;
    }

    function buildItem(item) {
        var el = document.createElement('div');
        el.className = 'cgv-notif-item' + (item.read ? '' : ' unread');
        el.dataset.id = String(item.id);

        var title = document.createElement('div');
        title.className = 'cgv-notif-item-title';
        title.textContent = item.title;

        var message = document.createElement('div');
        message.className = 'cgv-notif-item-message';
        message.textContent = item.message;

        var time = document.createElement('div');
        time.className = 'cgv-notif-item-time';
        time.textContent = item.timeAgo;

        el.appendChild(title);
        el.appendChild(message);
        el.appendChild(time);
        return el;
    }

    function renderList(items) {
        clearChildren(list);
        if (!items || items.length === 0) {
            list.appendChild(buildEmptyState());
            return;
        }
        items.forEach(function (item) {
            list.appendChild(buildItem(item));
        });
    }

    function loadNotifications() {
        fetch(baseUrl, { credentials: 'same-origin' })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                renderBadge(data.unreadCount || 0);
                renderList(data.items || []);
            })
            .catch(function () { /* ignore transient network errors */ });
    }

    function postAction(body) {
        return fetch(baseUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body,
            credentials: 'same-origin'
        }).then(loadNotifications);
    }

    function openPanel() {
        panel.classList.add('open');
        bellBtn.setAttribute('aria-expanded', 'true');
        loadNotifications();
    }

    function closePanel() {
        panel.classList.remove('open');
        bellBtn.setAttribute('aria-expanded', 'false');
    }

    bellBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        if (panel.classList.contains('open')) {
            closePanel();
        } else {
            openPanel();
        }
    });

    list.addEventListener('click', function (e) {
        var item = e.target.closest('.cgv-notif-item');
        if (item && item.classList.contains('unread')) {
            postAction('action=mark_read&notificationId=' + encodeURIComponent(item.dataset.id))
                .catch(function () { /* ignore */ });
        }
    });

    markAllBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        postAction('action=mark_all_read').catch(function () { /* ignore */ });
    });

    document.addEventListener('click', function (e) {
        if (!wrap.contains(e.target)) {
            closePanel();
        }
    });

    loadNotifications();
    setInterval(loadNotifications, POLL_INTERVAL_MS);
})();
