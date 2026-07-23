/* ─── H-Phsar UI — Shared utilities ─── */

const API_BASE = '';   // same origin

// ─── Token helpers ───────────────────────────────────────────────
function getToken() {
    const t = localStorage.getItem('hphsar_token');
    // Guard: reject placeholder values that would fail JWT parsing
    if (!t || t === 'undefined' || t === 'null') return null;
    return t;
}
function setToken(t) {
    if (!t || t === 'undefined' || t === 'null') {
        console.error('[H-Phsar] setToken() called with invalid value:', t);
        return;
    }
    localStorage.setItem('hphsar_token', t);
    console.log('[H-Phsar] Token stored. Length:', t.length);
}
function getRoleId()    { return localStorage.getItem('hphsar_role'); }
function setRoleId(r)   { localStorage.setItem('hphsar_role', String(r)); }
function getUserId()    { return localStorage.getItem('hphsar_userId'); }
function setUserId(id)  { localStorage.setItem('hphsar_userId', String(id)); }
function clearSession() {
    localStorage.removeItem('hphsar_token');
    localStorage.removeItem('hphsar_role');
    localStorage.removeItem('hphsar_userId');
}

function isTokenExpired(token) {
    try {
        // JWT uses base64url — replace chars so atob can decode it, then add padding
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const padded  = base64 + '='.repeat((4 - base64.length % 4) % 4);
        const payload = JSON.parse(atob(padded));
        return !!payload.exp && (Date.now() / 1000) > payload.exp;
    } catch (_) {
        return false; // can't decode — let the server decide
    }
}

function redirectIfNoToken() {
    const token = getToken();
    if (!token) {
        console.warn('[H-Phsar] No token on page load — redirecting to login.');
        window.location.href = '/web/login';
        return;
    }
    if (isTokenExpired(token)) {
        console.warn('[H-Phsar] Token is expired — clearing session and redirecting to login.');
        clearSession();
        window.location.href = '/web/login';
    }
}

function logout() {
    clearSession();
    window.location.href = '/web/login';
}

// ─── HTTP helpers ─────────────────────────────────────────────────
async function apiFetch(method, path, body = null) {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    } else {
        console.warn('[H-Phsar] No token available for request:', method, path);
    }

    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API_BASE + path, opts);
    const json = await res.json().catch(() => ({}));

    if (res.status === 401) {
        console.error('[H-Phsar] 401 on', path, '— token was:', token ? 'present' : 'missing');
        // Debounce: only redirect once, avoid cascading across parallel try/catch blocks
        if (!window._redirecting) {
            window._redirecting = true;
            clearSession();
            setTimeout(() => { window.location.href = '/web/login'; }, 200);
        }
        throw json;
    }
    if (!res.ok) throw json;
    return json;
}

const api = {
    get:    (path)         => apiFetch('GET',    path),
    post:   (path, body)   => apiFetch('POST',   path, body),
    put:    (path, body)   => apiFetch('PUT',    path, body),
    delete: (path)         => apiFetch('DELETE', path),
};

// ─── Toast notifications ──────────────────────────────────────────
function toast(msg, type = 'info', duration = 3000) {
    const icons = { success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', info: 'bi-info-circle-fill' };
    const container = document.getElementById('toast-container') || createToastContainer();
    const el = document.createElement('div');
    el.className = `toast-msg ${type}`;
    el.innerHTML = `<i class="bi ${icons[type]}"></i><span>${msg}</span>`;
    container.appendChild(el);
    setTimeout(() => el.remove(), duration);
}
function createToastContainer() {
    const c = document.createElement('div'); c.id = 'toast-container';
    document.body.appendChild(c); return c;
}

// ─── Pagination helper (1-based page numbers to match server mappers) ──────
function renderPagination(containerId, page, totalPages, onPageChange) {
    const c = document.getElementById(containerId);
    if (!c) return;
    c.innerHTML = '';
    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.className = 'page-btn' + (i === page ? ' active' : '');
        btn.textContent = i;
        btn.onclick = () => onPageChange(i);
        c.appendChild(btn);
    }
}

// ─── Format helpers ───────────────────────────────────────────────
function formatDate(str) {
    if (!str) return '—';
    return new Date(str).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}
function formatCurrency(n) {
    if (n == null) return '—';
    return '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2 });
}
// Step 3C lifecycle: CART, DRAFT, PENDING, PROCESSING, DISPATCHED, COMPLETED,
// REJECTED, CANCELLED. (CONFIRMED/SHIPPING/DELIVERED were the old, drifted
// seed names — DISPATCHED is the one real "in transit" state; there is no
// separate DELIVERED checkpoint, see OrderStatus's Javadoc for why.)
function orderStatusBadge(status) {
    const map = {
        CART:       'status-pending',
        DRAFT:      'status-pending',
        PENDING:    'status-pending',
        PROCESSING: 'status-preparing',
        DISPATCHED: 'status-dispatching',
        COMPLETED:  'status-complete',
        REJECTED:   'status-declined',
        CANCELLED:  'status-declined',
    };
    const cls = map[status] || 'status-pending';
    return `<span class="status-badge ${cls}">${status || '—'}</span>`;
}
function buyerStatusBadge(status) {
    const map = {
        CART:       'status-pending',
        DRAFT:      'status-pending',
        PENDING:    'status-pending',
        PROCESSING: 'status-preparing',
        DISPATCHED: 'status-dispatching',
        COMPLETED:  'status-complete',
        REJECTED:   'status-declined',
        CANCELLED:  'status-declined',
    };
    const key = String(status || '').toUpperCase();
    const cls = map[key] || 'status-pending';
    return '<span class="status-badge ' + cls + '">' + (status || '—') + '</span>';
}
function publishBadge(isPublish) {
    return isPublish
        ? '<span class="status-badge status-published"><i class="bi bi-check-circle"></i> Published</span>'
        : '<span class="status-badge status-unlisted"><i class="bi bi-slash-circle"></i> Unlisted</span>';
}

// ─── Order Timeline ───────────────────────────────────────────────
function renderOrderTimeline(status) {
    // Step 3C: PENDING -> PROCESSING -> DISPATCHED -> COMPLETED. No separate
    // DELIVERED checkpoint — dispatch is the supplier's last step, and the
    // buyer's own receipt confirmation is what completes the order.
    const statusStep = { PENDING:1, PROCESSING:2, DISPATCHED:3, COMPLETED:4 };
    const step = statusStep[(status||'').toUpperCase()] || 1;
    const rejected = ['REJECTED','CANCELLED'].includes((status||'').toUpperCase());
    const steps = [
        { label:'Ordered',    icon:'bi-bag-check-fill' },
        { label:'Accepted',   icon:'bi-check2-circle' },
        { label:'Dispatched', icon:'bi-truck' },
        { label:'Complete',   icon:'bi-patch-check-fill' },
    ];
    if (rejected) {
        return `<div class="order-timeline">
            <div class="timeline-track"><div class="timeline-fill" style="width:0%"></div></div>
            <div class="timeline-step done"><div class="timeline-dot"><i class="bi bi-bag-check-fill"></i></div><div class="timeline-label">Ordered</div></div>
            <div class="timeline-step rejected"><div class="timeline-dot"><i class="bi bi-x-lg"></i></div><div class="timeline-label">Rejected</div></div>
        </div>`;
    }
    const pct = ((step - 1) / (steps.length - 1)) * 100;
    return `<div class="order-timeline">
        <div class="timeline-track"><div class="timeline-fill" style="width:${pct}%"></div></div>
        ${steps.map((s, i) => {
            const n = i + 1;
            const cls = n < step ? 'done' : n === step ? 'active' : '';
            return `<div class="timeline-step ${cls}">
                <div class="timeline-dot"><i class="bi ${s.icon}"></i></div>
                <div class="timeline-label">${s.label}</div>
            </div>`;
        }).join('')}
    </div>`;
}

// ─── Notifications ────────────────────────────────────────────────
function _notifEndpoint() {
    return getRoleId() === '1'
        ? { list: '/api/v1/suppliers/notifications', markOne: (id) => `/api/v1/suppliers/notifications/${id}/read`, markAll: '/api/v1/suppliers/notifications/read', orderPage: '/web/suppliers/orders' }
        : { list: '/api/v1/buyers/notifications',    markOne: (id) => `/api/v1/buyers/notifications/${id}/read`,    markAll: '/api/v1/buyers/notifications/read',    orderPage: '/web/buyers/orders'    };
}

async function openNotifications() {
    document.getElementById('notif-overlay').classList.add('open');
    document.getElementById('notif-drawer').classList.add('open');
    await _loadNotifications();
}
function closeNotifications() {
    document.getElementById('notif-overlay').classList.remove('open');
    document.getElementById('notif-drawer').classList.remove('open');
}

async function _loadNotifications() {
    const ep   = _notifEndpoint();
    const list = document.getElementById('notif-list');
    list.innerHTML = '<div class="text-center text-muted py-4" style="font-size:13px;">Loading...</div>';
    try {
        const res   = await api.get(ep.list);
        const items = res.data || [];
        if (!items.length) {
            list.innerHTML = '<div class="text-center text-muted py-4" style="font-size:13px;">No notifications yet.</div>';
            document.getElementById('notif-badge').classList.remove('show');
            return;
        }
        const unread = items.filter(n => !n.seen).length;
        const badge  = document.getElementById('notif-badge');
        if (unread > 0) { badge.textContent = unread > 9 ? '9+' : unread; badge.classList.add('show'); }
        else badge.classList.remove('show');

        list.innerHTML = items.map(n => `
            <div class="notif-item ${n.seen ? '' : 'unread'}" id="notif-${n.id}" onclick="markNotifRead(${n.id})">
                <div class="notif-icon"><i class="bi bi-bell-fill"></i></div>
                <div class="notif-content">
                    <div class="notif-title">${n.title || '—'}</div>
                    <div class="notif-time">${n.createdDate ? formatDate(n.createdDate) : ''}</div>
                    ${n.orderId ? `<a class="notif-link" href="${ep.orderPage}?orderId=${n.orderId}" onclick="event.stopPropagation()"><i class="bi bi-arrow-right-circle me-1"></i>View Order #${n.orderId}</a>` : ''}
                </div>
                ${n.seen ? '' : '<div class="notif-unread-dot"></div>'}
            </div>`).join('');
    } catch (e) {
        list.innerHTML = '<div class="text-center text-danger py-4" style="font-size:13px;">Could not load notifications.</div>';
    }
}

async function markNotifRead(id) {
    const ep  = _notifEndpoint();
    const el  = document.getElementById(`notif-${id}`);
    if (el) el.classList.remove('unread');
    try { await api.put(ep.markOne(id)); } catch (_) {}
}

async function markAllNotifRead() {
    const ep = _notifEndpoint();
    try {
        await api.put(ep.markAll);
        document.getElementById('notif-badge').classList.remove('show');
        document.querySelectorAll('.notif-item.unread').forEach(el => el.classList.remove('unread'));
        document.querySelectorAll('.notif-unread-dot').forEach(el => el.remove());
    } catch (_) {}
}

async function _loadNotifBadge() {
    try {
        const ep  = _notifEndpoint();
        const res = await api.get(ep.list);
        const unread = (res.data || []).filter(n => !n.seen).length;
        const badge  = document.getElementById('notif-badge');
        if (badge) {
            if (unread > 0) { badge.textContent = unread > 9 ? '9+' : unread; badge.classList.add('show'); }
            else badge.classList.remove('show');
        }
    } catch (_) {}
}

// ─── Active sidebar link + auto token guard on protected pages ───
document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname;

    // Auto-redirect to login if no token (or expired token) on any protected page
    if (path.startsWith('/web/suppliers/') || path.startsWith('/web/buyers/')) {
        const token = getToken();
        if (!token || isTokenExpired(token)) {
            console.warn('[H-Phsar] No valid token on protected page, redirecting to login.');
            clearSession();
            window.location.href = '/web/login';
            return;
        }
    }

    document.querySelectorAll('.sidebar-nav a').forEach(a => {
        if (a.getAttribute('href') === path) a.classList.add('active');
    });
    const userEl = document.getElementById('topbar-user-name');
    if (userEl) userEl.textContent = getUserId() ? 'User #' + getUserId() : 'Account';

    // Load unread notification count on every protected page
    if (path.startsWith('/web/suppliers/') || path.startsWith('/web/buyers/')) {
        _loadNotifBadge();
    }
});
