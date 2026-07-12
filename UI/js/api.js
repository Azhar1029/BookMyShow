// ===== API BASE URL =====
const API = 'http://localhost:8080/api';

// ===== AUTH TOKEN HELPERS =====
function getAuthToken() {
    return localStorage.getItem('bms_token');
}

function setAuthToken(token) {
    localStorage.setItem('bms_token', token);
}

function clearAuthToken() {
    localStorage.removeItem('bms_token');
}

function authHeaders() {
    const token = getAuthToken();
    return token ? { 'Authorization': `Bearer ${token}` } : {};
}

// A 401 unambiguously means "not authenticated" (missing/invalid/expired token) - the
// backend now distinguishes this from 403 ("authenticated but not allowed"), see
// SecurityConfig's exceptionHandling(). On 401 we clear the stale token and send the
// user to log in again, since retrying won't help without a fresh token. We deliberately
// do NOT do this on 403 - that can mean a valid, logged-in user just lacks permission
// (e.g. a USER hitting an admin-only endpoint), and forcing them to re-login for that
// would be confusing and wouldn't fix anything.
function handleSessionExpired() {
    clearAuthToken();
    localStorage.removeItem('bms_user');
    showToast('Your session has expired. Please log in again.', 'error');
    const loginPath = window.location.pathname.includes('/pages/') ? 'login.html' : 'pages/login.html';
    setTimeout(() => window.location.href = loginPath, 1200);
}

// ===== GENERIC FETCH HELPERS =====
async function apiGet(endpoint) {
    const res = await fetch(`${API}${endpoint}`, {
        headers: { ...authHeaders() }
    });
    if (!res.ok) {
        if (res.status === 401) {
            handleSessionExpired();
            throw new Error('Session expired. Please log in again.');
        }
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    return res.json();
}

async function apiPost(endpoint, data) {
    const res = await fetch(`${API}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify(data)
    });
    if (!res.ok) {
        if (res.status === 401) {
            handleSessionExpired();
            throw new Error('Session expired. Please log in again.');
        }
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    return res.json();
}

async function apiPut(endpoint, data) {
    const res = await fetch(`${API}${endpoint}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: data ? JSON.stringify(data) : undefined
    });
    if (!res.ok) {
        if (res.status === 401) {
            handleSessionExpired();
            throw new Error('Session expired. Please log in again.');
        }
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    return res.json();
}

async function apiDelete(endpoint) {
    const res = await fetch(`${API}${endpoint}`, {
        method: 'DELETE',
        headers: { ...authHeaders() }
    });
    if (!res.ok) {
        if (res.status === 401) {
            handleSessionExpired();
            throw new Error('Session expired. Please log in again.');
        }
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || 'Request failed');
    }
    // might return text
    const text = await res.text();
    try { return JSON.parse(text); } catch { return text; }
}

// ===== USER APIs =====
const UserAPI = {
    register: (data) => apiPost('/users/register', data),
    // Real login now goes through /api/auth/login (JwtAuthenticationFilter / AuthController),
    // which validates credentials against the DB and returns a JWT.
    login: (data) => apiPost('/auth/login', data),
    getAll: () => apiGet('/users'),
    getById: (id) => apiGet(`/users/${id}`)
};

// ===== CITY APIs =====
const CityAPI = {
    add: (data) => apiPost('/cities', data),
    getAll: () => apiGet('/cities'),
    getById: (id) => apiGet(`/cities/${id}`)
};

// ===== MOVIE APIs =====
const MovieAPI = {
    add: (data) => apiPost('/movies', data),
    getAll: () => apiGet('/movies'),
    getById: (id) => apiGet(`/movies/${id}`),
    search: (title) => apiGet(`/movies/search?title=${encodeURIComponent(title)}`),
    getByGenre: (genre) => apiGet(`/movies/genre/${genre}`),
    getByLanguage: (lang) => apiGet(`/movies/language/${lang}`),
    update: (id, data) => apiPut(`/movies/${id}`, data),
    delete: (id) => apiDelete(`/movies/${id}`)
};

// ===== THEATER APIs =====
const TheaterAPI = {
    add: (data) => apiPost('/theaters', data),
    getAll: () => apiGet('/theaters'),
    getById: (id) => apiGet(`/theaters/${id}`),
    getByCity: (cityId) => apiGet(`/theaters/city/${cityId}`)
};

// ===== SCREEN APIs =====
const ScreenAPI = {
    add: (data) => apiPost('/screens', data),
    getAll: () => apiGet('/screens'),
    getById: (id) => apiGet(`/screens/${id}`),
    getByTheater: (theaterId) => apiGet(`/screens/theater/${theaterId}`)
};

// ===== SEAT APIs =====
const SeatAPI = {
    add: (data) => apiPost('/seats', data),
    getByScreen: (screenId) => apiGet(`/seats/screen/${screenId}`),
    getById: (id) => apiGet(`/seats/${id}`)
};

// ===== SHOW APIs =====
const ShowAPI = {
    add: (data) => apiPost('/shows', data),
    getAll: () => apiGet('/shows'),
    getById: (id) => apiGet(`/shows/${id}`),
    getByMovie: (movieId) => apiGet(`/shows/movie/${movieId}`),
    getByMovieAndDate: (movieId, date) => apiGet(`/shows/movie/${movieId}/date?date=${date}`)
};

// ===== BOOKING APIs =====
const BookingAPI = {
    create: (data) => apiPost('/bookings', data),
    getById: (id) => apiGet(`/bookings/${id}`),
    getByUser: (userId) => apiGet(`/bookings/user/${userId}`),
    cancel: (id) => apiPut(`/bookings/${id}/cancel`),
    getAvailableSeats: (showId) => apiGet(`/bookings/show/${showId}/available-seats`)
};

// ===== PAYMENT APIs =====
const PaymentAPI = {

    // Create Razorpay Order
    createOrder: (data) => apiPost('/payments/orders', data),

    // Verify Payment
    verify: (data) => apiPost('/payments/verify', data),

    // Get Razorpay Key
    getKey: () => apiGet('/payments/config')

};