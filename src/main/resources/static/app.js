/**
 * NoteVault Frontend Application Logic
 * Vanilla JavaScript (Zero Build, Lightweight SPA)
 */

// Application State
const state = {
    user: null,
    notes: [],
    isRegisterMode: false,
    pendingDeleteNoteId: null,
    searchDebounceTimer: null
};

// DOM Elements
const authSection = document.getElementById('authSection');
const notesSection = document.getElementById('notesSection');
const userProfileNav = document.getElementById('userProfileNav');
const navUserName = document.getElementById('navUserName');
const navUserEmail = document.getElementById('navUserEmail');
const navUserAvatar = document.getElementById('navUserAvatar');
const logoutBtn = document.getElementById('logoutBtn');

// Auth Form Elements
const authForm = document.getElementById('authForm');
const authTitle = document.getElementById('authTitle');
const authSubtitle = document.getElementById('authSubtitle');
const tabLogin = document.getElementById('tabLogin');
const tabRegister = document.getElementById('tabRegister');
const nameFieldGroup = document.getElementById('nameFieldGroup');
const nameInput = document.getElementById('nameInput');
const emailInput = document.getElementById('emailInput');
const passwordInput = document.getElementById('passwordInput');
const passwordHint = document.getElementById('passwordHint');
const authSubmitBtn = document.getElementById('authSubmitBtn');

// Notes Elements
const notesGrid = document.getElementById('notesGrid');
const emptyState = document.getElementById('emptyState');
const emptyTitle = document.getElementById('emptyTitle');
const emptySubtitle = document.getElementById('emptySubtitle');
const emptyActionBtn = document.getElementById('emptyActionBtn');
const noteCount = document.getElementById('noteCount');
const searchInput = document.getElementById('searchInput');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const openNewNoteModalBtn = document.getElementById('openNewNoteModalBtn');

// Note Modal Elements
const noteModal = document.getElementById('noteModal');
const modalTitle = document.getElementById('modalTitle');
const noteForm = document.getElementById('noteForm');
const editNoteId = document.getElementById('editNoteId');
const noteTitleInput = document.getElementById('noteTitleInput');
const noteContentInput = document.getElementById('noteContentInput');
const closeNoteModalBtn = document.getElementById('closeNoteModalBtn');
const cancelNoteModalBtn = document.getElementById('cancelNoteModalBtn');

// Delete Modal Elements
const deleteModal = document.getElementById('deleteModal');
const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

// Toast Container
const toastContainer = document.getElementById('toastContainer');

// ============================================================================
// Initialization & Session Management
// ============================================================================

function init() {
    loadUserSession();
    attachEventListeners();

    if (state.user && state.user.token) {
        showDashboard();
        fetchNotes();
    } else {
        showAuth();
    }
}

function loadUserSession() {
    try {
        const stored = localStorage.getItem('notevault_user');
        if (stored) {
            state.user = JSON.parse(stored);
        }
    } catch (e) {
        console.error('Error loading session from localStorage', e);
        localStorage.removeItem('notevault_user');
    }
}

function saveUserSession(userData) {
    state.user = userData;
    localStorage.setItem('notevault_user', JSON.stringify(userData));
}

function clearUserSession() {
    state.user = null;
    localStorage.removeItem('notevault_user');
    state.notes = [];
}

// ============================================================================
// API Helper
// ============================================================================

async function apiRequest(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (state.user && state.user.token) {
        headers['Authorization'] = `Bearer ${state.user.token}`;
    }

    try {
        const response = await fetch(endpoint, {
            ...options,
            headers
        });

        // If 401 Unauthorized, session expired or invalid
        if (response.status === 401 && !endpoint.includes('/api/auth/')) {
            clearUserSession();
            showAuth();
            showToast('Session expired. Please sign in again.', 'error');
            throw new Error('Unauthorized');
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            let errorMsg = (data && (data.message || data.error)) || `Request failed (${response.status})`;
            if (data && data.validationErrors) {
                const firstValError = Object.values(data.validationErrors)[0];
                if (firstValError) errorMsg = firstValError;
            }
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        throw err;
    }
}

// ============================================================================
// Auth Logic
// ============================================================================

function setAuthMode(isRegister) {
    state.isRegisterMode = isRegister;

    if (isRegister) {
        tabRegister.className = "flex-1 py-2 text-sm font-semibold rounded-lg transition bg-brand-600 text-white shadow";
        tabLogin.className = "flex-1 py-2 text-sm font-semibold rounded-lg transition text-slate-400 hover:text-white";
        authTitle.innerText = "Create Your Account";
        authSubtitle.innerText = "Get started with your private, secure notes vault";
        nameFieldGroup.classList.remove('hidden');
        passwordHint.classList.remove('hidden');
        nameInput.setAttribute('required', 'true');
        authSubmitBtn.querySelector('span').innerText = "Create Account";
    } else {
        tabLogin.className = "flex-1 py-2 text-sm font-semibold rounded-lg transition bg-brand-600 text-white shadow";
        tabRegister.className = "flex-1 py-2 text-sm font-semibold rounded-lg transition text-slate-400 hover:text-white";
        authTitle.innerText = "Welcome to NoteVault";
        authSubtitle.innerText = "Sign in to access your personal encrypted vault";
        nameFieldGroup.classList.add('hidden');
        passwordHint.classList.add('hidden');
        nameInput.removeAttribute('required');
        authSubmitBtn.querySelector('span').innerText = "Sign In";
    }
}

async function handleAuthSubmit(e) {
    e.preventDefault();

    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const name = nameInput.value.trim();

    authSubmitBtn.disabled = true;
    const originalText = authSubmitBtn.querySelector('span').innerText;
    authSubmitBtn.querySelector('span').innerText = "Processing...";

    try {
        let res;
        if (state.isRegisterMode) {
            res = await apiRequest('/api/auth/register', {
                method: 'POST',
                body: JSON.stringify({ name, email, password })
            });
            showToast('Account created successfully!', 'success');
        } else {
            res = await apiRequest('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });
            showToast('Welcome back!', 'success');
        }

        if (res && res.data) {
            saveUserSession(res.data);
            showDashboard();
            fetchNotes();
        }
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        authSubmitBtn.disabled = false;
        authSubmitBtn.querySelector('span').innerText = originalText;
    }
}

function handleLogout() {
    clearUserSession();
    showAuth();
    showToast('Signed out successfully', 'success');
}

// ============================================================================
// Notes Operations
// ============================================================================

async function fetchNotes(searchQuery = '') {
    try {
        const url = searchQuery 
            ? `/api/notes?search=${encodeURIComponent(searchQuery)}`
            : '/api/notes';

        const res = await apiRequest(url, { method: 'GET' });
        state.notes = (res && res.data) || [];
        renderNotes(searchQuery);
    } catch (err) {
        showToast(err.message || 'Failed to load notes', 'error');
    }
}

function renderNotes(searchQuery = '') {
    notesGrid.innerHTML = '';
    noteCount.innerText = state.notes.length;

    if (state.notes.length === 0) {
        emptyState.classList.remove('hidden');
        if (searchQuery) {
            emptyTitle.innerText = `No notes matching "${searchQuery}"`;
            emptySubtitle.innerText = "Try searching with a different title keyword.";
            emptyActionBtn.classList.add('hidden');
        } else {
            emptyTitle.innerText = "Your vault is empty";
            emptySubtitle.innerText = "Create your first note to capture ideas, code snippets, or to-dos.";
            emptyActionBtn.classList.remove('hidden');
        }
        return;
    }

    emptyState.classList.add('hidden');

    state.notes.forEach(note => {
        const card = createNoteCard(note);
        notesGrid.appendChild(card);
    });
}

function createNoteCard(note) {
    const card = document.createElement('div');
    card.className = "glass-card rounded-2xl p-5 flex flex-col justify-between group";

    const formattedDate = formatDate(note.createdAt);

    card.innerHTML = `
        <div>
            <div class="flex items-start justify-between gap-3 mb-2">
                <h4 class="text-base font-semibold text-white group-hover:text-brand-400 transition leading-snug break-words">
                    ${escapeHtml(note.title)}
                </h4>
                <div class="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition flex-shrink-0">
                    <button class="edit-note-btn p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition" title="Edit">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                    </button>
                    <button class="delete-note-btn p-1.5 text-slate-400 hover:text-rose-400 rounded-lg hover:bg-rose-500/10 transition" title="Delete">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                </div>
            </div>
            <p class="text-xs text-slate-300 leading-relaxed whitespace-pre-wrap line-clamp-4 break-words">
                ${escapeHtml(note.content)}
            </p>
        </div>
        <div class="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500">
            <span>${formattedDate}</span>
            <span class="text-brand-400/80 font-mono">ID #${note.id}</span>
        </div>
    `;

    // Hook edit & delete buttons
    card.querySelector('.edit-note-btn').addEventListener('click', () => openEditNoteModal(note));
    card.querySelector('.delete-note-btn').addEventListener('click', () => promptDeleteNote(note.id));

    return card;
}

// ============================================================================
// Modal Handlers (Create, Edit, Delete)
// ============================================================================

function openNewNoteModal() {
    modalTitle.innerText = "Create New Note";
    editNoteId.value = "";
    noteTitleInput.value = "";
    noteContentInput.value = "";
    noteModal.classList.remove('hidden');
    noteTitleInput.focus();
}

function openEditNoteModal(note) {
    modalTitle.innerText = "Edit Note";
    editNoteId.value = note.id;
    noteTitleInput.value = note.title;
    noteContentInput.value = note.content;
    noteModal.classList.remove('hidden');
    noteTitleInput.focus();
}

function closeNoteModal() {
    noteModal.classList.add('hidden');
}

async function handleNoteFormSubmit(e) {
    e.preventDefault();

    const id = editNoteId.value;
    const title = noteTitleInput.value.trim();
    const content = noteContentInput.value.trim();

    if (!title || !content) {
        showToast('Title and content are required', 'error');
        return;
    }

    const saveBtn = document.getElementById('saveNoteBtn');
    saveBtn.disabled = true;

    try {
        if (id) {
            // Update
            await apiRequest(`/api/notes/${id}`, {
                method: 'PUT',
                body: JSON.stringify({ title, content })
            });
            showToast('Note updated successfully', 'success');
        } else {
            // Create
            await apiRequest('/api/notes', {
                method: 'POST',
                body: JSON.stringify({ title, content })
            });
            showToast('Note created successfully', 'success');
        }

        closeNoteModal();
        fetchNotes(searchInput.value.trim());
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        saveBtn.disabled = false;
    }
}

function promptDeleteNote(noteId) {
    state.pendingDeleteNoteId = noteId;
    deleteModal.classList.remove('hidden');
}

function closeDeleteModal() {
    state.pendingDeleteNoteId = null;
    deleteModal.classList.add('hidden');
}

async function confirmDeleteNote() {
    if (!state.pendingDeleteNoteId) return;

    try {
        await apiRequest(`/api/notes/${state.pendingDeleteNoteId}`, {
            method: 'DELETE'
        });
        showToast('Note deleted successfully', 'success');
        closeDeleteModal();
        fetchNotes(searchInput.value.trim());
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============================================================================
// Search with Debounce
// ============================================================================

function handleSearchInput(e) {
    const query = e.target.value;

    if (query.trim().length > 0) {
        clearSearchBtn.classList.remove('hidden');
    } else {
        clearSearchBtn.classList.add('hidden');
    }

    clearTimeout(state.searchDebounceTimer);
    state.searchDebounceTimer = setTimeout(() => {
        fetchNotes(query.trim());
    }, 250);
}

function handleClearSearch() {
    searchInput.value = '';
    clearSearchBtn.classList.add('hidden');
    fetchNotes('');
}

// ============================================================================
// UI State Switching
// ============================================================================

function showAuth() {
    authSection.classList.remove('hidden');
    notesSection.classList.add('hidden');
    userProfileNav.classList.remove('flex');
    userProfileNav.classList.add('hidden');
}

function showDashboard() {
    authSection.classList.add('hidden');
    notesSection.classList.remove('hidden');
    userProfileNav.classList.remove('hidden');
    userProfileNav.classList.add('flex');

    if (state.user) {
        navUserName.innerText = state.user.name || 'User';
        navUserEmail.innerText = state.user.email || '';
        navUserAvatar.innerText = (state.user.name ? state.user.name.charAt(0) : 'U').toUpperCase();
    }
}

// ============================================================================
// Toast Notifications
// ============================================================================

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast-enter pointer-events-auto px-4 py-3 rounded-xl shadow-2xl flex items-center gap-2.5 text-xs font-medium border ${
        type === 'success' 
            ? 'bg-slate-900 border-brand-500/40 text-brand-300' 
            : 'bg-slate-900 border-rose-500/40 text-rose-300'
    }`;

    const iconSvg = type === 'success'
        ? `<svg class="w-4 h-4 text-brand-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>`
        : `<svg class="w-4 h-4 text-rose-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;

    toast.innerHTML = `${iconSvg}<span>${escapeHtml(message)}</span>`;

    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ============================================================================
// Utilities
// ============================================================================

function formatDate(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return isoString;

    return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit'
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ============================================================================
// Event Listeners
// ============================================================================

function attachEventListeners() {
    tabLogin.addEventListener('click', () => setAuthMode(false));
    tabRegister.addEventListener('click', () => setAuthMode(true));
    authForm.addEventListener('submit', handleAuthSubmit);
    logoutBtn.addEventListener('click', handleLogout);

    openNewNoteModalBtn.addEventListener('click', openNewNoteModal);
    emptyActionBtn.addEventListener('click', openNewNoteModal);
    closeNoteModalBtn.addEventListener('click', closeNoteModal);
    cancelNoteModalBtn.addEventListener('click', closeNoteModal);
    noteForm.addEventListener('submit', handleNoteFormSubmit);

    cancelDeleteBtn.addEventListener('click', closeDeleteModal);
    confirmDeleteBtn.addEventListener('click', confirmDeleteNote);

    searchInput.addEventListener('input', handleSearchInput);
    clearSearchBtn.addEventListener('click', handleClearSearch);

    // Close modals on Escape key or backdrop click
    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeNoteModal();
            closeDeleteModal();
        }
    });

    noteModal.addEventListener('click', (e) => {
        if (e.target === noteModal) closeNoteModal();
    });

    deleteModal.addEventListener('click', (e) => {
        if (e.target === deleteModal) closeDeleteModal();
    });
}

// Run on load
document.addEventListener('DOMContentLoaded', init);
