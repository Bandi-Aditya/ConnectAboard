/**
 * ConnectAbroad Frontend Application Engine
 * Unified Authenticated API Client, Profile System, People Directory, Connections & Chat
 */

let currentUser = null;
let currentProfile = null;
let currentView = 'home';
let peopleCurrentPage = 0;
let feedCurrentPage = 0;
let filterDebounceTimeout = null;

// Chat / Messaging state
let activeConversationId = null;
let activeRecipientId = null;
let userConversations = [];
let stompClient = null;

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

/**
 * Unified Authenticated API Client Wrapper
 * Ensures JWT is properly attached to all backend requests
 */
async function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        console.warn('No JWT token found in localStorage');
        const isPublicPage = window.location.pathname.endsWith('/login.html') || 
                             window.location.pathname.endsWith('/register.html') || 
                             window.location.pathname.endsWith('/index.html');
        if (!isPublicPage) {
            window.location.href = '/login.html';
        }
        throw new Error('Unauthenticated');
    }

    const headers = options.headers || {};
    if (!headers['Authorization'] && !headers['authorization']) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const mergedOptions = {
        ...options,
        headers: headers
    };

    const response = await fetch(url, mergedOptions);
    
    if (response.status === 401) {
        console.warn(`401 Unauthorized for ${url} - Redirecting to login.`);
        localStorage.removeItem('jwtToken');
        window.location.href = '/login.html';
    }

    return response;
}

async function initApp() {
    const token = localStorage.getItem('jwtToken');
    if (token) {
        await fetchMyUserData();
        initStompClient();
    } else {
        const isPublicPage = window.location.pathname.endsWith('/login.html') || 
                             window.location.pathname.endsWith('/register.html') || 
                             window.location.pathname.endsWith('/index.html');
        if (!isPublicPage) {
            window.location.href = '/login.html';
            return;
        }
    }

    setupNavigation();
    setupDropdown();
    setupGlobalSearch();
    initComposer();
    initEditProfileForm();

    const urlParams = new URLSearchParams(window.location.search);
    const viewParam = urlParams.get('view');
    const targetUserParam = urlParams.get('userId');
    const keywordParam = urlParams.get('keyword');

    if (keywordParam && document.getElementById('searchKeywordInput')) {
        document.getElementById('searchKeywordInput').value = keywordParam;
    }

    if (viewParam) {
        switchView(viewParam);
        if (viewParam === 'messages' && targetUserParam) {
            openChatWithUser(null, parseInt(targetUserParam));
        }
    } else if (window.location.pathname.includes('/dashboard.html')) {
        switchView('home');
    }
}

/**
 * Fetch Current Authenticated User & Profile (/api/users/me & /api/profiles/me)
 */
async function fetchMyUserData() {
    try {
        const userRes = await authenticatedFetch('/api/users/me');
        if (userRes.ok) {
            currentUser = await userRes.json();
            updateHeaderUserInfo(currentUser);
        } else {
            handleLogout();
            return;
        }

        const profileRes = await authenticatedFetch('/api/profiles/me');
        if (profileRes.ok) {
            currentProfile = await profileRes.json();
        } else {
            currentProfile = null;
        }

        updateNotificationBadge();
        updateMessagesBadge();
    } catch (err) {
        console.error("Error fetching user data:", err);
    }
}

function updateHeaderUserInfo(user) {
    const navAvatar = document.getElementById('navUserAvatar');
    const navName = document.getElementById('navUserName');
    const miniAvatar = document.getElementById('miniUserAvatar');
    const miniName = document.getElementById('miniUserName');

    const initials = getInitials(user.name);

    if (navAvatar) navAvatar.innerText = initials;
    if (navName) navName.innerText = user.name.split(' ')[0];
    if (miniAvatar) miniAvatar.innerText = initials;
    if (miniName) miniName.innerText = user.name;

    const myProfLink = document.getElementById('navMyProfileLink');
    if (myProfLink) {
        myProfLink.onclick = () => {
            window.location.href = `/profile.html?id=${user.id}`;
        };
    }

    if (user && user.role === 'ADMIN') {
        const dropdown = document.getElementById('userDropdown');
        if (dropdown && !document.getElementById('menuAdminPanel')) {
            const adminItem = document.createElement('div');
            adminItem.id = 'menuAdminPanel';
            adminItem.className = 'dropdown-item';
            adminItem.style.cursor = 'pointer';
            adminItem.style.fontWeight = '600';
            adminItem.style.color = '#3b82f6';
            adminItem.innerHTML = '🛡️ Admin Panel';
            adminItem.onclick = () => { window.location.href = '/admin.html'; };
            const divider = dropdown.querySelector('.dropdown-divider');
            if (divider) {
                dropdown.insertBefore(adminItem, divider);
            } else {
                dropdown.appendChild(adminItem);
            }
        }
    }
}

function getInitials(name) {
    if (!name) return 'CA';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

/**
 * Navigation & Views
 */
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const view = item.getAttribute('data-view');
            if (view) switchView(view);
        });
    });
}

function switchView(viewName) {
    currentView = viewName;

    const pageViews = document.querySelectorAll('.page-view');
    pageViews.forEach(pv => pv.classList.remove('active'));

    const targetView = document.getElementById(`view-${viewName}`);
    if (targetView) targetView.classList.add('active');

    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        if (item.getAttribute('data-view') === viewName) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    if (viewName === 'home') {
        fetchFeedPosts(0);
        renderRightSidebar();
    } else if (viewName === 'people') {
        fetchPeopleDirectory(0);
        fetchSameCollegeSection();
        fetchDestinationSection();
    } else if (viewName === 'connections') {
        initConnectionsPage();
    } else if (viewName === 'profile') {
        if (currentUser) {
            window.location.href = `/profile.html?id=${currentUser.id}`;
        } else {
            window.location.href = '/profile.html?me=true';
        }
    } else if (viewName === 'messages') {
        loadConversationsView();
    }
}

function setupDropdown() {
    const trigger = document.getElementById('userProfileTrigger');
    const dropdown = document.getElementById('userDropdown');

    const closeAllDropdowns = () => {
        if (dropdown) {
            dropdown.classList.remove('show', 'active');
        }
    };

    if (trigger && dropdown) {
        trigger.setAttribute('tabindex', '0');
        trigger.setAttribute('role', 'button');
        trigger.setAttribute('aria-expanded', 'false');

        const toggleDropdown = (e) => {
            e.stopPropagation();
            const isOpen = dropdown.classList.contains('show') || dropdown.classList.contains('active');
            if (isOpen) {
                dropdown.classList.remove('show', 'active');
                trigger.setAttribute('aria-expanded', 'false');
            } else {
                dropdown.classList.add('show', 'active');
                trigger.setAttribute('aria-expanded', 'true');
            }
        };

        trigger.addEventListener('click', toggleDropdown);
        trigger.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggleDropdown(e);
            }
        });

        document.addEventListener('click', (e) => {
            if (!dropdown.contains(e.target) && !trigger.contains(e.target)) {
                closeAllDropdowns();
                trigger.setAttribute('aria-expanded', 'false');
            }
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeAllDropdowns();
                trigger.setAttribute('aria-expanded', 'false');
            }
        });
    }

    const menuMyProf = document.getElementById('menuMyProfile');
    if (menuMyProf) {
        menuMyProf.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            if (window.location.pathname.includes('/dashboard.html')) {
                if (currentUser) {
                    window.location.href = `/profile.html?id=${currentUser.id}`;
                } else {
                    window.location.href = '/profile.html?me=true';
                }
            } else {
                window.location.href = '/profile.html?me=true';
            }
        });
    }

    const menuEditProf = document.getElementById('menuEditProfile');
    if (menuEditProf) {
        menuEditProf.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            openEditProfileModal();
        });
    }

    const menuConn = document.getElementById('menuConnections');
    if (menuConn) {
        menuConn.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            if (window.location.pathname.includes('/dashboard.html')) {
                switchView('connections');
            } else {
                window.location.href = '/connections.html';
            }
        });
    }

    const menuMsg = document.getElementById('menuMessages');
    if (menuMsg) {
        menuMsg.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            if (window.location.pathname.includes('/dashboard.html')) {
                switchView('messages');
            } else {
                window.location.href = '/dashboard.html?view=messages';
            }
        });
    }

    const menuMyJobs = document.getElementById('menuMyJobs');
    if (menuMyJobs) {
        menuMyJobs.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            window.location.href = '/jobs.html?view=my';
        });
    }

    const menuSavedJobs = document.getElementById('menuSavedJobs');
    if (menuSavedJobs) {
        menuSavedJobs.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            window.location.href = '/jobs.html?view=saved';
        });
    }

    const menuSet = document.getElementById('menuSettings');
    if (menuSet) {
        menuSet.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            openEditProfileModal();
        });
    }

    const logoutBtn = document.getElementById('logoutAction');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            closeAllDropdowns();
            handleLogout();
        });
    }
}

function setupGlobalSearch() {
    const searchInputs = document.querySelectorAll('#globalSearchInput');
    searchInputs.forEach(input => {
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                const keyword = input.value.trim();
                if (keyword) {
                    if (window.location.pathname.includes('/dashboard.html')) {
                        switchView('people');
                        const searchBox = document.getElementById('searchKeywordInput');
                        if (searchBox) {
                            searchBox.value = keyword;
                            fetchPeopleDirectory(0);
                        }
                    } else {
                        window.location.href = `/dashboard.html?view=people&keyword=${encodeURIComponent(keyword)}`;
                    }
                }
            }
        });
    });
}

function handleLogout() {
    localStorage.removeItem('jwtToken');
    if (stompClient) {
        try { stompClient.disconnect(); } catch (e) {}
    }
    window.location.href = '/login.html';
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/**
 * ==========================================================================
 * Public Profile Navigation & Standalone Profile Controller
 * ==========================================================================
 */

function viewUserProfileByPublicId(userId) {
    if (currentUser && userId === currentUser.id) {
        window.location.href = '/profile.html?me=true';
    } else {
        window.location.href = `/profile.html?id=${userId}`;
    }
}

async function initStandaloneProfilePage() {
    await fetchMyUserData();

    const urlParams = new URLSearchParams(window.location.search);
    const targetUserId = urlParams.get('id');
    const isMeParam = urlParams.get('me');

    const container = document.getElementById('profilePageContent');
    if (!container) return;

    let apiUrl = '/api/profiles/me';
    if (targetUserId && isMeParam !== 'true') {
        apiUrl = `/api/profiles/${targetUserId}`;
    }

    try {
        const response = await authenticatedFetch(apiUrl);

        if (response.ok) {
            const profile = await response.json();
            renderProfilePageDetails(container, profile);

            const isOwnProfile = currentUser && (profile.userId === currentUser.id || profile.id === currentUser.id);
            const heading = document.getElementById('userPostsHeading');
            if (heading) {
                heading.innerText = isOwnProfile ? 'Your Posts' : `Posts by ${profile.name}`;
            }

            fetchUserPostsTimeline(profile.userId || profile.id);

            const editParam = urlParams.get('edit');
            if (editParam === 'true') {
                openEditProfileModal();
            }
        } else {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem; background: #ffffff; border-radius: var(--radius-lg); border: 1px solid var(--border-color);">
                    <div style="font-size: 3rem; margin-bottom: 0.5rem;">⚠️</div>
                    <h2>Profile Not Found</h2>
                    <p style="color: var(--text-muted); margin-bottom: 1.5rem;">The requested profile does not exist or has been removed.</p>
                    <button class="btn-primary" onclick="window.location.href='/dashboard.html?view=people'">Back to People Directory</button>
                </div>
            `;
        }
    } catch (err) {
        console.error("Fetch profile page error:", err);
    }
}

function renderProfilePageDetails(container, p) {
    const pUserId = p.userId || p.id;
    const isOwnProfile = currentUser && (currentUser.id === p.userId || currentUser.id === p.id);
    const completion = p.profileCompletion || 0;
    const isComplete = completion >= 100;
    const strokeDashoffset = 283 - (283 * completion) / 100;

    const actionBtnHtml = isOwnProfile ? `
        <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
            <button class="btn-primary" onclick="openEditProfileModal()">✏️ Edit Profile</button>
            <button class="btn-secondary" onclick="window.location.href='/connections.html'">🤝 Connections</button>
            <button class="btn-secondary" onclick="window.location.href='/jobs.html?view=my'">💼 My Jobs</button>
        </div>
    ` : `
        <div style="display: flex; gap: 0.5rem; align-items: center;">
            ${renderConnectionActionButton(p)}
            <div style="position: relative;">
                <button class="btn-secondary" style="padding: 0.45rem 0.65rem;" onclick="this.nextElementSibling.classList.toggle('show')">•••</button>
                <div class="dropdown-menu" style="right: 0; top: calc(100% + 4px);">
                    <div class="dropdown-item" onclick="showToast('User reported. Thank you.', 'info')">🚩 Report User</div>
                    <div class="dropdown-item" style="color: var(--danger-text);" onclick="showToast('User blocked.', 'info')">🚫 Block User</div>
                </div>
            </div>
        </div>
    `;

    container.innerHTML = `
        <div style="background: #ffffff; border-radius: var(--radius-lg); border: 1px solid var(--border-color); overflow: hidden; box-shadow: var(--shadow-sm);">
            <div style="height: 120px; background: linear-gradient(135deg, var(--primary) 0%, #1e40af 100%);"></div>
            <div style="padding: 0 2rem 2rem 2rem; position: relative;">
                
                <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-top: -50px; margin-bottom: 1rem; flex-wrap: wrap; gap: 1rem;">
                    
                    ${isOwnProfile ? `
                        <div style="display: flex; flex-direction: column; align-items: center;">
                            <div class="profile-completion-ring-container">
                                <svg class="profile-completion-svg" viewBox="0 0 100 100">
                                    <circle class="profile-completion-bg" cx="50" cy="50" r="45" />
                                    <circle class="profile-completion-progress" cx="50" cy="50" r="45"
                                            stroke-dasharray="283" stroke-dashoffset="${strokeDashoffset}" />
                                </svg>
                                <div class="profile-completion-avatar">
                                    ${p.profilePhoto ? `<img src="${escapeHtml(p.profilePhoto)}" alt="Avatar">` : getInitials(p.name)}
                                </div>
                                ${isComplete ? `<div class="completion-check-badge" title="100% Complete">✓</div>` : ''}
                            </div>
                            <div class="completion-percent-label">${completion}% Profile Complete</div>
                        </div>
                    ` : `
                        <div class="avatar-circle" style="width: 100px; height: 100px; font-size: 2rem; border: 4px solid #ffffff; box-shadow: var(--shadow-md);">
                            ${p.profilePhoto ? `<img src="${escapeHtml(p.profilePhoto)}" alt="Avatar">` : getInitials(p.name)}
                        </div>
                    `}

                    <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                        <span class="header-count-badge">${p.connectionCount || 0} Connections</span>
                        ${actionBtnHtml}
                    </div>
                </div>

                <div style="margin-bottom: 1.25rem;">
                    <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                        <h1 style="font-size: 1.6rem; font-weight: 700; color: var(--text-main); margin: 0;">${escapeHtml(p.name || p.userName)}</h1>
                        <span class="badge ${p.userType === 'ABROAD' ? 'badge-abroad' : 'badge-aspiring'}">
                            ${p.userType === 'ABROAD' ? '✈️ Living Abroad' : '🎯 Aspiring Abroad'}
                        </span>
                    </div>
                    <div style="font-size: 1rem; color: var(--text-muted); font-weight: 500; margin-top: 0.25rem;">
                        ${escapeHtml(p.profession || 'Community Member')} ${p.experienceYears ? '• ' + p.experienceYears + ' yrs exp' : ''}
                    </div>
                    <div style="font-size: 0.88rem; color: var(--text-light); margin-top: 0.35rem; display: flex; gap: 1rem; flex-wrap: wrap;">
                        <span>📍 ${escapeHtml(p.currentCity || '')} ${p.currentCountry ? '(' + escapeHtml(p.currentCountry) + ')' : ''}</span>
                        <span>🎓 ${escapeHtml(p.collegeName || 'College not set')}</span>
                    </div>
                </div>

                <!-- Profile Functional Tabs Header -->
                <div class="profile-tabs-header">
                    <button class="profile-tab-btn active" id="profTabBtn-posts" onclick="switchProfileTab('posts', ${pUserId})">📝 Posts</button>
                    <button class="profile-tab-btn" id="profTabBtn-about" onclick="switchProfileTab('about', ${pUserId})">👤 About</button>
                    <button class="profile-tab-btn" id="profTabBtn-connections" onclick="switchProfileTab('connections', ${pUserId})">🤝 Connections</button>
                    <button class="profile-tab-btn" id="profTabBtn-jobs" onclick="switchProfileTab('jobs', ${pUserId})">💼 Jobs</button>
                </div>

                <!-- Tab 1: Posts (Default Active) -->
                <div id="profTabSec-posts">
                    <!-- Posts timeline content is in userPostsTimelineContainer below -->
                </div>

                <!-- Tab 2: About Section -->
                <div id="profTabSec-about" style="display: none;">
                    ${p.bio ? `
                        <div style="background: var(--bg-subtle); padding: 1rem 1.25rem; border-radius: var(--radius-md); margin-bottom: 1.5rem; border-left: 4px solid var(--primary);">
                            <div style="font-size: 0.8rem; text-transform: uppercase; font-weight: 700; color: var(--text-muted); margin-bottom: 0.35rem;">Bio & Summary</div>
                            <p style="margin: 0; font-size: 0.95rem; color: var(--text-main); line-height: 1.6;">${escapeHtml(p.bio)}</p>
                        </div>
                    ` : ''}

                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1.25rem;">
                        <div style="border: 1px solid var(--border-color); padding: 1.25rem; border-radius: var(--radius-md); background: var(--bg-surface);">
                            <h3 style="font-size: 0.95rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.85rem;">🎓 Education & Background</h3>
                            <div style="display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.88rem; color: var(--text-muted);">
                                <div><strong>College:</strong> ${escapeHtml(p.collegeName || 'N/A')}</div>
                                <div><strong>Degree:</strong> ${escapeHtml(p.degree || 'N/A')}</div>
                                <div><strong>Graduation Year:</strong> ${p.graduationYear || 'N/A'}</div>
                                <div><strong>Hometown:</strong> ${escapeHtml(p.hometown || 'N/A')}</div>
                            </div>
                        </div>

                        <div style="border: 1px solid var(--border-color); padding: 1.25rem; border-radius: var(--radius-md); background: var(--bg-surface);">
                            <h3 style="font-size: 0.95rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.85rem;">✈️ Abroad Journey</h3>
                            <div style="display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.88rem; color: var(--text-muted);">
                                ${p.userType === 'ASPIRING' ? `
                                    <div><strong>Target Destination:</strong> ${escapeHtml(p.targetCity || '')} ${p.targetCountry ? '(' + escapeHtml(p.targetCountry) + ')' : 'N/A'}</div>
                                    <div><strong>Target University:</strong> ${escapeHtml(p.targetUniversity || 'N/A')}</div>
                                ` : `
                                    <div><strong>Current Residence:</strong> ${escapeHtml(p.currentCity || '')} ${p.currentCountry ? '(' + escapeHtml(p.currentCountry) + ')' : 'N/A'}</div>
                                `}
                            </div>
                        </div>
                    </div>

                    ${p.skills ? `
                        <div style="margin-top: 1.25rem; border: 1px solid var(--border-color); padding: 1.25rem; border-radius: var(--radius-md); background: var(--bg-surface);">
                            <h3 style="font-size: 0.95rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.65rem;">💡 Key Skills</h3>
                            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                                ${p.skills.split(',').map(s => `<span class="match-chip" style="font-size:0.82rem; padding: 0.35rem 0.75rem;">${escapeHtml(s.trim())}</span>`).join('')}
                            </div>
                        </div>
                    ` : ''}
                </div>

                <!-- Tab 3: Connections Section -->
                <div id="profTabSec-connections" style="display: none;">
                    <div id="profileConnectionsContainer" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem;">
                        <!-- Loaded dynamically -->
                    </div>
                </div>

                <!-- Tab 4: Jobs Section -->
                <div id="profTabSec-jobs" style="display: none;">
                    <div id="profileJobsContainer" style="display: flex; flex-direction: column; gap: 1rem;">
                        <!-- Loaded dynamically -->
                    </div>
                </div>

            </div>
        </div>
    `;
}

async function fetchUserPostsTimeline(userId) {
    const container = document.getElementById('userPostsTimelineContainer');
    if (!container) return;

    container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 1.5rem;">Loading posts...</div>`;

    try {
        const response = await authenticatedFetch(`/api/profiles/${userId}/posts?page=0&size=10`);

        if (response.ok) {
            const pageData = await response.json();
            if (!pageData.content || pageData.content.length === 0) {
                container.innerHTML = `
                    <div style="text-align: center; padding: 2rem; background: var(--bg-surface); border: 1px dashed var(--border-color); border-radius: var(--radius-md); color: var(--text-muted); font-size: 0.9rem;">
                        No posts created yet by this user.
                    </div>
                `;
            } else {
                container.innerHTML = pageData.content.map(post => renderPostCardHtml(post)).join('');
            }
        } else {
            container.innerHTML = `<div style="text-align: center; color: var(--danger-text); padding: 1rem;">Failed to load posts.</div>`;
        }
    } catch (err) {
        container.innerHTML = `<div style="text-align: center; color: var(--danger-text); padding: 1rem;">Error loading posts.</div>`;
    }
}

function switchProfileTab(tabName, userId) {
    const tabs = document.querySelectorAll('.profile-tab-btn');
    tabs.forEach(t => t.classList.remove('active'));

    const activeTabBtn = document.getElementById(`profTabBtn-${tabName}`);
    if (activeTabBtn) activeTabBtn.classList.add('active');

    const secPosts = document.getElementById('profTabSec-posts');
    const userPostsHeading = document.getElementById('userPostsHeading');
    const userPostsTimelineContainer = document.getElementById('userPostsTimelineContainer');
    const secAbout = document.getElementById('profTabSec-about');
    const secConnections = document.getElementById('profTabSec-connections');
    const secJobs = document.getElementById('profTabSec-jobs');

    if (secPosts) secPosts.style.display = (tabName === 'posts') ? 'block' : 'none';
    if (userPostsHeading) userPostsHeading.style.display = (tabName === 'posts') ? 'block' : 'none';
    if (userPostsTimelineContainer) userPostsTimelineContainer.style.display = (tabName === 'posts') ? 'flex' : 'none';
    if (secAbout) secAbout.style.display = (tabName === 'about') ? 'block' : 'none';
    if (secConnections) secConnections.style.display = (tabName === 'connections') ? 'block' : 'none';
    if (secJobs) secJobs.style.display = (tabName === 'jobs') ? 'block' : 'none';

    if (tabName === 'posts') {
        fetchUserPostsTimeline(userId);
    } else if (tabName === 'connections') {
        fetchProfileConnectionsTab(userId);
    } else if (tabName === 'jobs') {
        fetchProfileJobsTab(userId);
    }
}

async function fetchProfileConnectionsTab(userId) {
    const container = document.getElementById('profileConnectionsContainer');
    if (!container) return;

    container.innerHTML = `<div style="text-align:center; padding:1.5rem; color:var(--text-muted); grid-column: 1 / -1;">Loading connections...</div>`;

    try {
        const isOwn = currentUser && currentUser.id === userId;
        const url = isOwn ? '/api/connections' : `/api/profiles/search?size=12`;
        const res = await authenticatedFetch(url);

        if (res.ok) {
            const data = await res.json();
            const list = isOwn ? data : (data.content || []);

            if (list.length === 0) {
                container.innerHTML = `<div style="text-align:center; padding:1.5rem; color:var(--text-muted); grid-column: 1 / -1;">No connections to show.</div>`;
            } else {
                container.innerHTML = list.map(item => {
                    const u = item.connectedUser ? item.connectedUser : item;
                    const uId = u.userId || u.id;
                    return `
                        <div style="background:var(--bg-surface); border:1px solid var(--border-color); border-radius:var(--radius-md); padding:0.85rem 1rem; display:flex; align-items:center; gap:0.75rem; cursor:pointer;" onclick="viewUserProfileByPublicId(${uId})">
                            <div class="avatar-circle" style="width:40px; height:40px;">
                                ${u.profilePhoto ? `<img src="${escapeHtml(u.profilePhoto)}" alt="Avatar">` : getInitials(u.name)}
                            </div>
                            <div style="flex:1; min-width:0;">
                                <div style="font-weight:600; font-size:0.9rem; color:var(--text-main);">${escapeHtml(u.name)}</div>
                                <div style="font-size:0.78rem; color:var(--text-muted);">${escapeHtml(u.profession || 'Community Member')}</div>
                            </div>
                            <button class="btn-secondary" style="font-size:0.78rem; padding:0.3rem 0.6rem;" onclick="event.stopPropagation(); viewUserProfileByPublicId(${uId})">View</button>
                        </div>
                    `;
                }).join('');
            }
        }
    } catch (err) {
        container.innerHTML = `<div style="text-align:center; color:var(--danger-text); padding:1rem; grid-column: 1 / -1;">Error loading connections.</div>`;
    }
}

async function fetchProfileJobsTab(userId) {
    const container = document.getElementById('profileJobsContainer');
    if (!container) return;

    container.innerHTML = `<div style="text-align:center; padding:1.5rem; color:var(--text-muted);">Loading posted jobs...</div>`;

    try {
        const isOwn = currentUser && currentUser.id === userId;
        const url = isOwn ? '/api/jobs/my' : `/api/jobs/user/${userId}`;
        const res = await authenticatedFetch(url);

        if (res.ok) {
            const pageData = await res.json();
            const items = pageData.content || [];

            if (items.length === 0) {
                container.innerHTML = `<div style="text-align:center; padding:1.5rem; color:var(--text-muted);">No jobs posted by this user yet.</div>`;
            } else {
                container.innerHTML = items.map(job => renderJobCardHtml(job)).join('');
            }
        }
    } catch (err) {
        container.innerHTML = `<div style="text-align:center; color:var(--danger-text); padding:1rem;">Error loading jobs.</div>`;
    }
}

function openEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    if (!modal) {
        window.location.href = '/profile.html?me=true&edit=true';
        return;
    }

    if (currentProfile) {
        const p = currentProfile;
        if (document.getElementById('profName')) document.getElementById('profName').value = p.userName || p.name || '';
        if (document.getElementById('profPhoto')) document.getElementById('profPhoto').value = p.profilePhoto || '';
        if (document.getElementById('profBio')) document.getElementById('profBio').value = p.bio || '';
        if (document.getElementById('profCollegeName')) document.getElementById('profCollegeName').value = p.collegeName || '';
        if (document.getElementById('profCollegeCity')) document.getElementById('profCollegeCity').value = p.collegeCity || '';
        if (document.getElementById('profCollegeCountry')) document.getElementById('profCollegeCountry').value = p.collegeCountry || '';
        if (document.getElementById('profDegree')) document.getElementById('profDegree').value = p.degree || '';
        if (document.getElementById('profGraduationYear')) document.getElementById('profGraduationYear').value = p.graduationYear || '';
        if (document.getElementById('profHometown')) document.getElementById('profHometown').value = p.hometown || '';
        if (document.getElementById('profCurrentCountry')) document.getElementById('profCurrentCountry').value = p.currentCountry || '';
        if (document.getElementById('profCurrentCity')) document.getElementById('profCurrentCity').value = p.currentCity || '';
        if (document.getElementById('profTargetCountry')) document.getElementById('profTargetCountry').value = p.targetCountry || '';
        if (document.getElementById('profTargetCity')) document.getElementById('profTargetCity').value = p.targetCity || '';
        if (document.getElementById('profTargetUniversity')) document.getElementById('profTargetUniversity').value = p.targetUniversity || '';
        if (document.getElementById('profProfession')) document.getElementById('profProfession').value = p.profession || '';
        if (document.getElementById('profExperienceYears')) document.getElementById('profExperienceYears').value = p.experienceYears || '';
        if (document.getElementById('profSkills')) document.getElementById('profSkills').value = p.skills || '';
    }

    modal.style.display = 'flex';
}

function closeEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    if (modal) modal.style.display = 'none';
}

function initEditProfileForm() {
    const form = document.getElementById('editProfileForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const name = document.getElementById('profName')?.value.trim();
        const photo = document.getElementById('profPhoto')?.value.trim();
        const bio = document.getElementById('profBio')?.value.trim();
        const collegeName = document.getElementById('profCollegeName')?.value.trim();
        const degree = document.getElementById('profDegree')?.value.trim();
        const gradYear = document.getElementById('profGraduationYear')?.value;
        const hometown = document.getElementById('profHometown')?.value.trim();
        const currentCountry = document.getElementById('profCurrentCountry')?.value;
        const currentCity = document.getElementById('profCurrentCity')?.value.trim();
        const targetCountry = document.getElementById('profTargetCountry')?.value;
        const targetCity = document.getElementById('profTargetCity')?.value.trim();
        const targetUniversity = document.getElementById('profTargetUniversity')?.value.trim();
        const profession = document.getElementById('profProfession')?.value.trim();
        const experienceYears = document.getElementById('profExperienceYears')?.value;
        const skills = document.getElementById('profSkills')?.value.trim();

        const payload = {
            name: name || undefined,
            profilePhoto: photo || undefined,
            bio: bio || undefined,
            collegeName: collegeName || undefined,
            degree: degree || undefined,
            graduationYear: gradYear ? parseInt(gradYear) : undefined,
            hometown: hometown || undefined,
            currentCountry: currentCountry || undefined,
            currentCity: currentCity || undefined,
            targetCountry: targetCountry || undefined,
            targetCity: targetCity || undefined,
            targetUniversity: targetUniversity || undefined,
            profession: profession || undefined,
            experienceYears: experienceYears ? parseInt(experienceYears) : undefined,
            skills: skills || undefined
        };

        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const response = await authenticatedFetch('/api/profiles/me', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const updatedProfile = await response.json();
                currentProfile = updatedProfile;
                showToast("Profile updated successfully!", "success");
                closeEditProfileModal();
                await fetchMyUserData();

                if (window.location.pathname.includes('/profile.html')) {
                    initStandaloneProfilePage();
                } else if (currentView === 'profile') {
                    const container = document.getElementById('view-profile') || document.getElementById('profilePageContent');
                    if (container) renderProfilePageDetails(container, currentProfile);
                }
            } else {
                const errData = await response.json();
                showToast(errData.message || "Failed to update profile.", "error");
            }
        } catch (err) {
            console.error("Update profile submit error:", err);
            showToast("Error updating profile.", "error");
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });
}

/**
 * ==========================================================================
 * Real PostgreSQL Social Feed Engine
 * ==========================================================================
 */

async function fetchFeedPosts(page = 0) {
    feedCurrentPage = page;
    const feedContainer = document.getElementById('feedPostsContainer');
    if (!feedContainer) return;

    feedContainer.innerHTML = `
        <div class="post-card" style="text-align: center; color: var(--text-muted); padding: 2rem;">
            Loading updates from your abroad network...
        </div>
    `;

    try {
        const response = await authenticatedFetch(`/api/posts/feed?page=${page}&size=10`);

        if (response.ok) {
            const pageData = await response.json();
            if (!pageData.content || pageData.content.length === 0) {
                renderEmptyFeedState(feedContainer);
            } else {
                feedContainer.innerHTML = pageData.content.map(post => renderPostCardHtml(post)).join('');
            }
        } else {
            renderErrorFeedState(feedContainer);
        }
    } catch (err) {
        console.error("Feed fetch error:", err);
        renderErrorFeedState(feedContainer);
    }
}

function renderEmptyFeedState(container) {
    container.innerHTML = `
        <div class="post-card" style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface);">
            <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">📰</div>
            <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--text-main);">Your feed is empty</h3>
            <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 400px; margin: 0.35rem auto 1.25rem auto;">
                Connect with fellow alumni and professionals to see updates, housing posts, and abroad experiences in your feed!
            </p>
            <button class="btn-primary" onclick="switchView('people')">🔍 Discover People</button>
        </div>
    `;
}

function renderErrorFeedState(container) {
    container.innerHTML = `
        <div class="post-card" style="text-align: center; padding: 2rem; color: var(--danger-text);">
            Unable to load feed. Check server connection.
        </div>
    `;
}

function renderPostCardHtml(post) {
    const author = post.author || {};
    const isLiked = post.likedByCurrentUser;
    const typeLabel = getPostTypeBadgeLabel(post.postType);
    const isJobPost = post.postType === 'JOB';

    return `
        <div class="post-card ${isJobPost ? 'feed-job-card' : ''}" id="post-${post.id}">
            <div class="post-header">
                <div class="post-author-info">
                    <div class="avatar-circle" onclick="viewUserProfileByPublicId(${author.userId})" style="cursor:pointer;">
                        ${author.profilePhoto ? `<img src="${escapeHtml(author.profilePhoto)}" alt="Avatar">` : getInitials(author.name)}
                    </div>
                    <div class="author-details">
                        <div class="author-name" onclick="viewUserProfileByPublicId(${author.userId})" style="cursor:pointer;">
                            ${escapeHtml(author.name)} ${isJobPost ? 'shared a job opportunity' : ''}
                        </div>
                        <div class="author-title">${escapeHtml(author.profession || 'Community Member')} ${author.currentCity ? '• ' + escapeHtml(author.currentCity) : ''}</div>
                        <div class="post-meta">
                            <span class="post-time">${formatTimeAgo(post.createdAt)}</span>
                            ${author.currentCountry ? `<span class="post-location">📍 ${escapeHtml(author.currentCountry)}</span>` : ''}
                        </div>
                    </div>
                </div>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span class="badge ${isJobPost ? 'badge-abroad' : 'badge-aspiring'}">${isJobPost ? '💼 Job Opportunity' : typeLabel}</span>
                    ${post.isMine ? `
                        <button class="btn-cancel" style="font-size: 0.75rem; padding: 0.2rem 0.5rem;" onclick="handleDeletePost(${post.id})">🗑️ Delete</button>
                    ` : ''}
                </div>
            </div>

            <div class="post-content">
                ${escapeHtml(post.content)}
            </div>

            ${isJobPost ? `
                <div style="margin-top: 0.75rem; padding: 0.85rem; background: var(--primary-light); border: 1px solid var(--primary-border); border-radius: var(--radius-md); display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: 700; color: var(--primary); font-size: 0.95rem;">💼 Career & Opportunity Hub</div>
                        <div style="font-size: 0.82rem; color: var(--text-muted);">Explore open positions or post a role for fellow expats.</div>
                    </div>
                    <button class="btn-primary" style="font-size: 0.82rem; padding: 0.4rem 0.85rem;" onclick="window.location.href='/jobs.html'">View Opportunities</button>
                </div>
            ` : ''}

            ${post.imageUrl ? `
                <div style="margin-top: 0.5rem; border-radius: var(--radius-md); overflow: hidden; max-height: 380px;">
                    <img src="${escapeHtml(post.imageUrl)}" alt="Post Media" style="width: 100%; height: 100%; object-fit: cover;">
                </div>
            ` : ''}

            <div class="post-footer">
                <button class="action-btn ${isLiked ? 'liked' : ''}" id="like-btn-${post.id}" onclick="handleToggleLike(${post.id})">
                    ${isLiked ? '❤️' : '🤍'} <span id="like-count-${post.id}">${post.likeCount} Likes</span>
                </button>
                <button class="action-btn" onclick="toggleCommentsBox(${post.id})">
                    💬 <span id="comment-count-${post.id}">${post.commentCount} Comments</span>
                </button>
                <button class="action-btn" onclick="showToast('Post link copied to clipboard!', 'info')">
                    ↗ Share
                </button>
            </div>

            <!-- Comments Expandable Drawer -->
            <div id="comments-box-${post.id}" style="display: none; border-top: 1px solid var(--border-color); padding-top: 0.85rem; margin-top: 0.5rem;">
                <div id="comments-list-${post.id}" style="display: flex; flex-direction: column; gap: 0.65rem; margin-bottom: 0.85rem;">
                    <!-- Loaded dynamically -->
                </div>
                
                <div style="display: flex; gap: 0.5rem;">
                    <input type="text" id="comment-input-${post.id}" class="form-control" style="flex:1; font-size: 0.85rem; padding: 0.45rem 0.75rem;" placeholder="Write a comment..." onkeydown="if(event.key==='Enter') handleAddComment(${post.id})">
                    <button class="btn-primary" style="font-size: 0.8rem; padding: 0.45rem 0.85rem;" onclick="handleAddComment(${post.id})">Comment</button>
                </div>
            </div>

        </div>
    `;
}

function getPostTypeBadgeLabel(postType) {
    switch (postType) {
        case 'ABROAD_EXPERIENCE': return '✈️ Experience';
        case 'QUESTION': return '❓ Question';
        case 'ADVICE': return '💡 Advice';
        case 'JOB': return '💼 Job';
        case 'HOUSING': return '🏠 Housing';
        case 'EVENT': return '📅 Event';
        default: return '📢 Update';
    }
}

function initComposer() {
    const submitPostBtn = document.getElementById('submitPostBtn');
    const composerTextarea = document.getElementById('composerTextarea');
    const typeSelect = document.getElementById('composerPostTypeSelect');
    const imageInput = document.getElementById('composerImageUrlInput');

    if (submitPostBtn && composerTextarea) {
        submitPostBtn.addEventListener('click', async () => {
            const text = composerTextarea.value.trim();
            if (!text) {
                showToast("Post content cannot be empty", 'error');
                return;
            }

            const postType = typeSelect ? typeSelect.value : 'GENERAL';
            const imageUrl = imageInput ? imageInput.value.trim() : null;

            submitPostBtn.disabled = true;

            try {
                const response = await authenticatedFetch('/api/posts', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ content: text, postType: postType, imageUrl: imageUrl })
                });

                if (response.ok) {
                    showToast("Post published successfully!", 'success');
                    composerTextarea.value = '';
                    if (imageInput) imageInput.value = '';
                    fetchFeedPosts(0);
                } else {
                    const errData = await response.json();
                    showToast(errData.message || "Failed to publish post.", 'error');
                }
            } catch (err) {
                console.error("Create post error:", err);
                showToast("Error publishing post.", 'error');
            } finally {
                submitPostBtn.disabled = false;
            }
        });
    }
}

async function handleToggleLike(postId) {
    const likeBtn = document.getElementById(`like-btn-${postId}`);
    const countElem = document.getElementById(`like-count-${postId}`);
    if (!likeBtn || !countElem) return;

    const isLiked = likeBtn.classList.contains('liked');
    const method = isLiked ? 'DELETE' : 'POST';

    try {
        const response = await authenticatedFetch(`/api/posts/${postId}/like`, { method: method });

        if (response.ok) {
            let currentCount = parseInt(countElem.innerText) || 0;
            if (isLiked) {
                likeBtn.classList.remove('liked');
                likeBtn.innerHTML = `🤍 <span id="like-count-${postId}">${Math.max(0, currentCount - 1)} Likes</span>`;
            } else {
                likeBtn.classList.add('liked');
                likeBtn.innerHTML = `❤️ <span id="like-count-${postId}">${currentCount + 1} Likes</span>`;
            }
        }
    } catch (err) {
        console.error("Toggle like error:", err);
    }
}

async function toggleCommentsBox(postId) {
    const box = document.getElementById(`comments-box-${postId}`);
    if (!box) return;

    if (box.style.display === 'none' || !box.style.display) {
        box.style.display = 'block';
        loadPostComments(postId);
    } else {
        box.style.display = 'none';
    }
}

async function loadPostComments(postId) {
    const listContainer = document.getElementById(`comments-list-${postId}`);
    if (!listContainer) return;

    listContainer.innerHTML = `<div style="font-size:0.8rem; color:var(--text-muted);">Loading comments...</div>`;

    try {
        const response = await authenticatedFetch(`/api/posts/${postId}/comments?page=0&size=20`);

        if (response.ok) {
            const pageData = await response.json();
            if (!pageData.content || pageData.content.length === 0) {
                listContainer.innerHTML = `<div style="font-size:0.8rem; color:var(--text-muted);">No comments yet. Be the first to comment!</div>`;
            } else {
                listContainer.innerHTML = pageData.content.map(c => renderCommentHtml(c)).join('');
            }
        }
    } catch (err) {
        listContainer.innerHTML = `<div style="font-size:0.8rem; color:var(--danger-text);">Error loading comments.</div>`;
    }
}

function renderCommentHtml(c) {
    const author = c.author || {};
    return `
        <div style="background: var(--bg-subtle); padding: 0.5rem 0.75rem; border-radius: var(--radius-sm); font-size: 0.85rem; display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
                <strong style="color: var(--text-main); cursor: pointer;" onclick="viewUserProfileByPublicId(${author.userId})">${escapeHtml(author.name)}:</strong>
                <span style="color: var(--text-main); margin-left: 0.25rem;">${escapeHtml(c.content)}</span>
            </div>
            ${c.isMine ? `
                <button style="background:none; border:none; color:var(--text-muted); cursor:pointer; font-size:0.75rem;" title="Delete Comment" onclick="handleDeleteComment(${c.postId}, ${c.id})">✕</button>
            ` : ''}
        </div>
    `;
}

async function handleAddComment(postId) {
    const input = document.getElementById(`comment-input-${postId}`);
    if (!input) return;

    const text = input.value.trim();
    if (!text) return;

    input.value = '';

    try {
        const response = await authenticatedFetch(`/api/posts/${postId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: text })
        });

        if (response.ok) {
            loadPostComments(postId);
            const countElem = document.getElementById(`comment-count-${postId}`);
            if (countElem) {
                let cnt = parseInt(countElem.innerText) || 0;
                countElem.innerText = `${cnt + 1} Comments`;
            }
        }
    } catch (err) {
        console.error("Add comment error:", err);
    }
}

async function handleDeleteComment(postId, commentId) {
    try {
        const response = await authenticatedFetch(`/api/comments/${commentId}`, { method: 'DELETE' });
        if (response.ok) {
            loadPostComments(postId);
        }
    } catch (err) {}
}

async function handleDeletePost(postId) {
    if (!confirm("Are you sure you want to delete this post?")) return;

    try {
        const response = await authenticatedFetch(`/api/posts/${postId}`, { method: 'DELETE' });
        if (response.ok) {
            showToast("Post deleted.", 'info');
            fetchFeedPosts(feedCurrentPage);
        }
    } catch (err) {}
}

/**
 * ==========================================================================
 * Real People Directory & Profile Search Engine
 * ==========================================================================
 */
async function fetchPeopleDirectory(page = 0) {
    peopleCurrentPage = page;
    renderPeopleLoadingState();

    const keyword = document.getElementById('searchKeywordInput')?.value || '';
    const college = document.getElementById('filterCollegeInput')?.value || '';
    const country = document.getElementById('filterCountrySelect')?.value || '';
    const city = document.getElementById('filterCityInput')?.value || '';
    const profession = document.getElementById('filterProfessionInput')?.value || '';
    const userType = document.getElementById('filterUserTypeSelect')?.value || '';

    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', 6);
    if (keyword) params.append('keyword', keyword);
    if (college) params.append('college', college);
    if (country) params.append('currentCountry', country);
    if (city) params.append('currentCity', city);
    if (profession) params.append('profession', profession);
    if (userType) params.append('userType', userType);

    try {
        const response = await authenticatedFetch(`/api/profiles/search?${params.toString()}`);

        if (response.ok) {
            const pageData = await response.json();
            if (!pageData.content || pageData.content.length === 0) {
                renderPeopleEmptyState();
            } else {
                const container = document.getElementById('peopleDirectoryContainer');
                if (container) {
                    container.innerHTML = pageData.content.map(p => renderPeopleCard(p)).join('');
                }
                renderPeoplePagination(pageData);
            }
        } else {
            renderPeopleErrorState();
        }
    } catch (err) {
        renderPeopleErrorState();
    }
}

function renderPeopleCard(profile) {
    const matchReasons = profile.matchReasons || [];

    return `
        <div class="user-card" id="user-card-${profile.userId}">
            <div class="user-card-header">
                <div class="user-avatar-container" onclick="viewUserProfileByPublicId(${profile.userId})">
                    <div class="avatar-circle">
                        ${profile.profilePhoto ? `<img src="${escapeHtml(profile.profilePhoto)}" alt="Avatar">` : getInitials(profile.name)}
                    </div>
                </div>

                <div class="user-card-body">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                        <span class="badge ${profile.userType === 'ABROAD' ? 'badge-abroad' : 'badge-aspiring'}">
                            ${profile.userType === 'ABROAD' ? '✈️ Abroad' : '🎯 Aspiring'}
                        </span>
                        <span style="font-size: 0.78rem; color: var(--text-muted); font-weight: 600;">${profile.connectionCount || 0} Connections</span>
                    </div>

                    <div class="card-name" onclick="viewUserProfileByPublicId(${profile.userId})">${escapeHtml(profile.name)}</div>
                    <div class="card-title">${escapeHtml(profile.profession || 'Community Member')} ${profile.experienceYears ? '• ' + profile.experienceYears + ' yrs exp' : ''}</div>
                    <div class="card-location" style="margin-top: 0.25rem;">
                        📍 ${escapeHtml(profile.currentCity || '')} ${profile.currentCountry ? '(' + escapeHtml(profile.currentCountry) + ')' : ''}
                    </div>
                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.15rem;">
                        🎓 ${escapeHtml(profile.collegeName || 'N/A')} ${profile.degree ? '• ' + escapeHtml(profile.degree) : ''}
                    </div>

                    ${matchReasons.length > 0 ? `
                        <div class="match-reasons-box">
                            ${matchReasons.map(r => `<span class="match-chip">${escapeHtml(r)}</span>`).join('')}
                        </div>
                    ` : ''}

                    <p style="font-size: 0.85rem; color: var(--text-main); margin: 0.65rem 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                        "${escapeHtml(profile.bio || 'Connected member of ConnectAbroad community.')}"
                    </p>
                </div>
            </div>

            <div style="display: flex; gap: 0.5rem; align-items: center; justify-content: space-between; margin-top: 0.75rem; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                <button class="btn-secondary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;" onclick="viewUserProfileByPublicId(${profile.userId})">
                    View Profile
                </button>
                <div>
                    ${renderConnectionActionButton(profile)}
                </div>
            </div>
        </div>
    `;
}

function renderConnectionActionButton(profile) {
    if (!currentUser) return '';
    if (currentUser.id === profile.userId) {
        return '<span style="font-size:0.8rem; color:var(--text-muted); font-weight:500;">👤 You</span>';
    }

    const status = profile.connectionStatus || 'NONE';
    const connId = profile.connectionId;

    if (status === 'CONNECTED') {
        return `
            <div style="display: flex; align-items: center; gap: 6px;">
                <button class="btn-primary" style="font-size:0.8rem; padding: 0.35rem 0.75rem;" onclick="openChatWithUser(event, ${profile.userId})">💬 Message</button>
                <button class="btn-remove" title="Remove Connection" onclick="handleRemoveConnection(event, ${connId}, ${profile.userId})">Remove</button>
            </div>
        `;
    } else if (status === 'PENDING_SENT') {
        return `
            <div style="display: flex; align-items: center; gap: 6px;">
                <span class="btn-pending">⏳ Request Sent</span>
                <button class="btn-cancel" title="Cancel Request" onclick="handleCancelConnectionRequest(event, ${connId}, ${profile.userId})">Cancel</button>
            </div>
        `;
    } else if (status === 'PENDING_RECEIVED') {
        return `
            <div style="display: flex; align-items: center; gap: 6px;">
                <button class="btn-accept" onclick="handleAcceptConnectionRequest(event, ${connId}, ${profile.userId})">✓ Accept</button>
                <button class="btn-reject" onclick="handleRejectConnectionRequest(event, ${connId}, ${profile.userId})">✗ Reject</button>
            </div>
        `;
    } else {
        return `
            <button class="btn-connect" onclick="handleSendConnectionRequest(event, ${profile.userId})">
                + Connect
            </button>
        `;
    }
}

async function fetchSameCollegeSection() {
    const container = document.getElementById('sameCollegeGrid');
    const wrapper = document.getElementById('sameCollegeSectionContainer');
    if (!container || !wrapper) return;

    try {
        const response = await authenticatedFetch('/api/profiles/sections/college?size=3');
        if (response.ok) {
            const pageData = await response.json();
            if (pageData.content && pageData.content.length > 0) {
                wrapper.style.display = 'block';
                container.innerHTML = pageData.content.map(p => renderPeopleCard(p)).join('');
            } else {
                wrapper.style.display = 'none';
            }
        }
    } catch (e) {
        wrapper.style.display = 'none';
    }
}

async function fetchDestinationSection() {
    const container = document.getElementById('destinationGrid');
    const wrapper = document.getElementById('destinationSectionContainer');
    if (!container || !wrapper) return;

    const isAspiring = currentUser && currentUser.userType === 'ASPIRING';
    const endpoint = isAspiring ? '/api/profiles/sections/destination?size=3' : '/api/profiles/sections/near-you?size=3';

    try {
        const response = await authenticatedFetch(endpoint);
        if (response.ok) {
            const pageData = await response.json();
            if (pageData.content && pageData.content.length > 0) {
                wrapper.style.display = 'block';
                container.innerHTML = pageData.content.map(p => renderPeopleCard(p)).join('');
            } else {
                wrapper.style.display = 'none';
            }
        }
    } catch (e) {
        wrapper.style.display = 'none';
    }
}

function handleFilterChange() {
    if (filterDebounceTimeout) clearTimeout(filterDebounceTimeout);
    filterDebounceTimeout = setTimeout(() => {
        fetchPeopleDirectory(0);
    }, 300);
}

function resetPeopleFilters() {
    if (document.getElementById('searchKeywordInput')) document.getElementById('searchKeywordInput').value = '';
    if (document.getElementById('filterCollegeInput')) document.getElementById('filterCollegeInput').value = '';
    if (document.getElementById('filterCountrySelect')) document.getElementById('filterCountrySelect').value = '';
    if (document.getElementById('filterCityInput')) document.getElementById('filterCityInput').value = '';
    if (document.getElementById('filterProfessionInput')) document.getElementById('filterProfessionInput').value = '';
    if (document.getElementById('filterUserTypeSelect')) document.getElementById('filterUserTypeSelect').value = '';

    fetchPeopleDirectory(0);
}

function renderPeopleLoadingState() {
    const container = document.getElementById('peopleDirectoryContainer');
    if (!container) return;

    container.innerHTML = Array(3).fill(0).map(() => `
        <div class="skeleton-card">
            <div style="display: flex; gap: 0.75rem; margin-bottom: 1rem;">
                <div style="width: 50px; height: 50px; border-radius: 50%; background: #e2e8f0;"></div>
                <div style="flex: 1;">
                    <div style="height: 16px; width: 60%; background: #e2e8f0; border-radius: 4px; margin-bottom: 6px;"></div>
                    <div style="height: 12px; width: 40%; background: #e2e8f0; border-radius: 4px;"></div>
                </div>
            </div>
            <div style="height: 12px; width: 90%; background: #e2e8f0; border-radius: 4px;"></div>
        </div>
    `).join('');
}

function renderPeopleEmptyState() {
    const container = document.getElementById('peopleDirectoryContainer');
    if (!container) return;

    container.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1.5rem; background: #ffffff; border: 1px dashed var(--border-color); border-radius: var(--radius-md);">
            <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🔍</div>
            <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">No people found</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; max-width: 450px; margin: 0 auto 1.25rem auto;">
                Try changing your search filters to discover other community members.
            </p>
            <button class="btn-secondary" onclick="resetPeopleFilters()">↺ Clear Filters</button>
        </div>
    `;
}

function renderPeopleErrorState() {
    const container = document.getElementById('peopleDirectoryContainer');
    if (!container) return;

    container.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1.5rem; background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-md);">
            <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">⚠️</div>
            <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">Unable to load directory right now</h3>
            <button class="btn-primary" onclick="fetchPeopleDirectory(0)">↺ Retry</button>
        </div>
    `;
}

function renderPeoplePagination(pageData) {
    const container = document.getElementById('peoplePaginationContainer');
    if (!container) return;

    if (pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    container.innerHTML = `
        <button class="pagination-btn" ${pageData.page === 0 ? 'disabled' : ''} onclick="fetchPeopleDirectory(${pageData.page - 1})">
            ◀ Previous
        </button>
        <span style="font-weight: 600; font-size: 0.9rem; color: var(--text-main);">
            Page ${pageData.page + 1} of ${pageData.totalPages}
        </span>
        <button class="pagination-btn" ${pageData.last ? 'disabled' : ''} onclick="fetchPeopleDirectory(${pageData.page + 1})">
            Next ▶
        </button>
    `;
}

/**
 * ==========================================================================
 * Connection System Engine
 * ==========================================================================
 */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✓';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px) scale(0.95)';
        setTimeout(() => {
            if (toast.parentNode) toast.parentNode.removeChild(toast);
        }, 300);
    }, 3500);
}

async function updateNotificationBadge() {
    try {
        const response = await authenticatedFetch('/api/notifications/unread-count');
        if (response.ok) {
            const data = await response.json();
            const count = data.count || 0;
            const badge = document.getElementById('navNotificationBadge');
            const navBtn = document.getElementById('navNotificationBtn');
            const countText = count > 99 ? '99+' : count;

            if (badge) {
                if (count > 0) {
                    badge.innerText = countText;
                    badge.style.display = 'inline-flex';
                } else {
                    badge.innerText = '';
                    badge.style.display = 'none';
                }
            }

            if (navBtn) {
                const existingBadge = navBtn.querySelector('.nav-badge');
                if (existingBadge) {
                    if (count > 0) {
                        existingBadge.innerText = countText;
                        existingBadge.style.display = 'inline-flex';
                    } else {
                        existingBadge.innerText = '';
                        existingBadge.style.display = 'none';
                    }
                }
            }
        }
    } catch (err) {
        console.error('Error updating notification badge:', err);
    }
}

async function handleSendConnectionRequest(event, targetUserId) {
    if (event) event.stopPropagation();

    try {
        const response = await authenticatedFetch(`/api/connections/request/${targetUserId}`, { method: 'POST' });
        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection request sent.", 'success');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to send request.", 'error');
        }
    } catch (err) {
        showToast("Error sending request.", 'error');
    }
}

async function handleAcceptConnectionRequest(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    try {
        const response = await authenticatedFetch(`/api/connections/${connectionId}/accept`, { method: 'PUT' });
        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection accepted!", 'success');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to accept request.", 'error');
        }
    } catch (err) {
        showToast("Error accepting connection.", 'error');
    }
}

async function handleRejectConnectionRequest(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    try {
        const response = await authenticatedFetch(`/api/connections/${connectionId}/reject`, { method: 'PUT' });
        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection request rejected.", 'info');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to reject request.", 'error');
        }
    } catch (err) {
        showToast("Error rejecting request.", 'error');
    }
}

async function handleCancelConnectionRequest(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    try {
        const response = await authenticatedFetch(`/api/connections/${connectionId}/cancel`, { method: 'DELETE' });
        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Request cancelled.", 'info');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to cancel request.", 'error');
        }
    } catch (err) {
        showToast("Error cancelling request.", 'error');
    }
}

async function handleRemoveConnection(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    if (!confirm("Are you sure you want to remove this connection?")) return;

    try {
        const response = await authenticatedFetch(`/api/connections/${connectionId}`, { method: 'DELETE' });
        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection removed.", 'info');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to remove connection.", 'error');
        }
    } catch (err) {
        showToast("Error removing connection.", 'error');
    }
}

function refreshConnectionUI(targetUserId) {
    updateNotificationBadge();
    fetchMyUserData();

    if (window.location.pathname.includes('/profile.html')) {
        initStandaloneProfilePage();
    } else if (currentView === 'people') {
        fetchPeopleDirectory(peopleCurrentPage);
    } else if (currentView === 'connections' || document.getElementById('view-connections-page')) {
        loadConnectionsTabData(activeConnectionsTab);
    }
}

/**
 * Connections Page Controller
 */
let activeConnectionsTab = 'received';
let rawLoadedConnectionsList = [];

function initConnectionsPage() {
    updateNotificationBadge();
    switchConnectionsTab('received');
}

function switchConnectionsTab(tabName) {
    activeConnectionsTab = tabName;

    ['received', 'sent', 'connected'].forEach(t => {
        const btn = document.getElementById(`btnTab${t.charAt(0).toUpperCase() + t.slice(1)}`) ||
                    document.getElementById(`btnDashTab${t.charAt(0).toUpperCase() + t.slice(1)}`);
        if (btn) btn.classList.toggle('active', t === tabName);
    });

    loadConnectionsTabData(tabName);
}

async function loadConnectionsTabData(tabName) {
    const container = document.getElementById('connectionsContainer') || document.getElementById('dashConnectionsContainer');
    if (!container) return;

    container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);">Loading connections...</div>`;

    let url = '/api/connections/requests/received';
    if (tabName === 'sent') url = '/api/connections/requests/sent';
    if (tabName === 'connected') url = '/api/connections';

    try {
        const res = await authenticatedFetch(url);
        if (res.ok) {
            rawLoadedConnectionsList = await res.json();
            fetchHeaderConnectionCount();
            renderConnectionsTabList(container, rawLoadedConnectionsList, tabName);
        } else {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--danger-text);">Error loading data.</div>`;
        }
    } catch (e) {
        container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--danger-text);">Error loading data.</div>`;
    }
}

async function fetchHeaderConnectionCount() {
    try {
        const res = await authenticatedFetch('/api/connections');
        if (res.ok) {
            const list = await res.json();
            const badge = document.getElementById('connectionsHeaderCountBadge');
            if (badge) badge.innerText = `${list.length} Connections`;

            const statConnected = document.getElementById('statTotalConnected');
            if (statConnected) statConnected.innerText = list.length;
        }
    } catch (e) {}
}

function handleConnectionsSearch() {
    const query = document.getElementById('connectionsSearchInput')?.value.trim().toLowerCase() || '';
    const container = document.getElementById('connectionsContainer') || document.getElementById('dashConnectionsContainer');
    if (!container) return;

    if (!query) {
        renderConnectionsTabList(container, rawLoadedConnectionsList, activeConnectionsTab);
        return;
    }

    const filtered = rawLoadedConnectionsList.filter(item => {
        const u = item.user;
        if (!u) return false;
        const name = (u.name || '').toLowerCase();
        const prof = (u.profession || '').toLowerCase();
        const city = (u.currentCity || '').toLowerCase();
        const country = (u.currentCountry || '').toLowerCase();
        const college = (u.collegeName || '').toLowerCase();

        return name.includes(query) || prof.includes(query) || city.includes(query) || country.includes(query) || college.includes(query);
    });

    renderConnectionsTabList(container, filtered, activeConnectionsTab);
}

function renderConnectionsTabList(container, list, tabName) {
    if (list.length === 0) {
        let emptyText = "No incoming requests right now.";
        if (tabName === 'sent') emptyText = "No sent requests.";
        if (tabName === 'connected') emptyText = "No connections yet. Connect with alumni and professionals to grow your network.";

        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px dashed var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🤝</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">${escapeHtml(emptyText)}</h3>
                ${tabName === 'connected' ? `
                    <button class="btn-primary" style="margin-top: 1rem;" onclick="switchView('people')">🔍 Discover People</button>
                ` : ''}
            </div>
        `;
        return;
    }

    if (tabName === 'received') {
        const countBadge = document.getElementById('tabReceivedCount');
        if (countBadge) countBadge.innerText = list.length;
        const statRec = document.getElementById('statPendingReceived');
        if (statRec) statRec.innerText = list.length;

        container.innerHTML = list.map(req => {
            const u = req.user;
            return `
                <div class="social-profile-card">
                    <div class="social-card-header">
                        <div class="social-card-avatar" onclick="viewUserProfileByPublicId(${u.userId})">
                            ${u.profilePhoto ? `<img src="${escapeHtml(u.profilePhoto)}" alt="Avatar">` : getInitials(u.name)}
                        </div>
                        <div class="social-card-details">
                            <div class="social-card-name" onclick="viewUserProfileByPublicId(${u.userId})">${escapeHtml(u.name)}</div>
                            <div class="social-card-profession">${escapeHtml(u.profession || 'Community Member')}</div>
                            <div class="social-card-location">📍 ${escapeHtml(u.currentCity || '')} ${u.currentCountry ? '(' + escapeHtml(u.currentCountry) + ')' : ''}</div>
                            <div class="social-card-college">🎓 ${escapeHtml(u.collegeName || 'N/A')}</div>
                        </div>
                    </div>
                    ${u.bio ? `<div class="social-card-bio">"${escapeHtml(u.bio)}"</div>` : ''}
                    <div class="social-card-actions">
                        <button class="btn-accept" style="flex:1;" onclick="handleAcceptConnectionRequest(event, ${req.connectionId}, ${u.userId})">✓ Accept</button>
                        <button class="btn-reject" style="flex:1;" onclick="handleRejectConnectionRequest(event, ${req.connectionId}, ${u.userId})">✗ Ignore</button>
                    </div>
                </div>
            `;
        }).join('');

    } else if (tabName === 'sent') {
        const countBadge = document.getElementById('tabSentCount');
        if (countBadge) countBadge.innerText = list.length;

        container.innerHTML = list.map(req => {
            const u = req.user;
            return `
                <div class="social-profile-card">
                    <div class="social-card-header">
                        <div class="social-card-avatar" onclick="viewUserProfileByPublicId(${u.userId})">
                            ${u.profilePhoto ? `<img src="${escapeHtml(u.profilePhoto)}" alt="Avatar">` : getInitials(u.name)}
                        </div>
                        <div class="social-card-details">
                            <div class="social-card-name" onclick="viewUserProfileByPublicId(${u.userId})">${escapeHtml(u.name)}</div>
                            <div class="social-card-profession">${escapeHtml(u.profession || 'Community Member')}</div>
                            <div class="social-card-location">📍 ${escapeHtml(u.currentCity || '')} ${u.currentCountry ? '(' + escapeHtml(u.currentCountry) + ')' : ''}</div>
                            <div class="social-card-college">🎓 ${escapeHtml(u.collegeName || 'N/A')}</div>
                        </div>
                    </div>
                    <div class="social-card-actions">
                        <span class="btn-pending" style="flex:1; text-align:center;">⏳ Sent</span>
                        <button class="btn-cancel" style="flex:1;" onclick="handleCancelConnectionRequest(event, ${req.connectionId}, ${u.userId})">Cancel</button>
                    </div>
                </div>
            `;
        }).join('');

    } else if (tabName === 'connected') {
        const countBadge = document.getElementById('tabConnectedCount');
        if (countBadge) countBadge.innerText = list.length;

        container.innerHTML = list.map(conn => {
            const u = conn.user;
            return `
                <div class="social-profile-card">
                    <div class="social-card-header">
                        <div class="social-card-avatar" onclick="viewUserProfileByPublicId(${u.userId})">
                            ${u.profilePhoto ? `<img src="${escapeHtml(u.profilePhoto)}" alt="Avatar">` : getInitials(u.name)}
                        </div>
                        <div class="social-card-details">
                            <div class="social-card-name" onclick="viewUserProfileByPublicId(${u.userId})">${escapeHtml(u.name)}</div>
                            <div class="social-card-profession">${escapeHtml(u.profession || 'Community Member')}</div>
                            <div class="social-card-location">📍 ${escapeHtml(u.currentCity || '')} ${u.currentCountry ? '(' + escapeHtml(u.currentCountry) + ')' : ''}</div>
                            <div class="social-card-college">🎓 ${escapeHtml(u.collegeName || 'N/A')}</div>
                        </div>
                    </div>
                    <div class="social-card-actions">
                        <button class="btn-primary" style="flex:1;" onclick="openChatWithUser(event, ${u.userId})">💬 Message</button>
                        <button class="btn-secondary" style="flex:1;" onclick="viewUserProfileByPublicId(${u.userId})">View Profile</button>
                    </div>
                </div>
            `;
        }).join('');
    }
}

function renderRightSidebar() {
    const list = document.getElementById('peopleSuggestionsList');
    if (!list) return;

    authenticatedFetch('/api/profiles/people?size=3')
        .then(r => r.json())
        .then(data => {
            if (data.content && data.content.length > 0) {
                list.innerHTML = data.content.map(u => `
                    <div class="suggestion-item" style="display:flex; align-items:center; gap:0.75rem; margin-bottom:0.75rem;">
                        <div class="avatar-circle" style="width:36px; height:36px; font-size:0.85rem;" onclick="viewUserProfileByPublicId(${u.userId})">
                            ${getInitials(u.name)}
                        </div>
                        <div class="suggestion-info" style="flex:1; min-width:0;" onclick="viewUserProfileByPublicId(${u.userId})">
                            <div class="suggestion-name" style="font-weight:600; font-size:0.85rem; cursor:pointer;">${escapeHtml(u.name)}</div>
                            <div class="suggestion-meta" style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(u.currentCity || u.currentCountry || '')}</div>
                        </div>
                    </div>
                `).join('');
            } else {
                list.innerHTML = `<div style="font-size:0.85rem; color:var(--text-muted);">No suggestions right now.</div>`;
            }
        }).catch(() => {
            list.innerHTML = `<div style="font-size:0.85rem; color:var(--text-muted);">No suggestions right now.</div>`;
        });
}

/**
 * ==========================================================================
 * PHASE 5: STOMP WebSocket Real-Time Chat Engine
 * ==========================================================================
 */

function initStompClient() {
    const token = localStorage.getItem('jwtToken');
    if (!token || !currentUser) return;
    if (stompClient && stompClient.connected) return;

    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({ 'Authorization': `Bearer ${token}` }, (frame) => {
            console.log('STOMP WebSocket Connected successfully');

            stompClient.subscribe(`/user/${currentUser.id}/queue/messages`, (message) => {
                if (message.body) {
                    const msgResponse = JSON.parse(message.body);
                    handleIncomingChatMessage(msgResponse);
                }
            });

            stompClient.subscribe(`/user/${currentUser.id}/queue/notifications`, (message) => {
                if (message.body) {
                    const notifResponse = JSON.parse(message.body);
                    handleIncomingNotification(notifResponse);
                }
            });
        }, (error) => {
            console.warn('STOMP connection failed, retrying...', error);
            setTimeout(initStompClient, 5000);
        });
    } catch (e) {
        console.error("STOMP initialization exception:", e);
    }
}

async function loadConversationsView() {
    const convListElem = document.getElementById('conversationsList');
    if (convListElem) {
        convListElem.innerHTML = `<div style="text-align:center; padding:1.5rem; color:var(--text-muted);">Loading chats...</div>`;
    }

    try {
        const res = await authenticatedFetch('/api/conversations');

        if (res.ok) {
            userConversations = await res.json();
            updateMessagesBadge();
            renderConversationsSidebarList(userConversations);

            if (userConversations.length > 0) {
                if (!activeConversationId) {
                    openConversation(userConversations[0].id, userConversations[0].otherUser.userId);
                } else {
                    const activeConv = userConversations.find(c => c.id === activeConversationId);
                    if (activeConv) {
                        openConversation(activeConv.id, activeConv.otherUser.userId);
                    }
                }
            } else {
                renderEmptyChatArea();
            }
        } else {
            renderEmptyChatArea();
        }
    } catch (e) {
        console.error("Error loading conversations:", e);
        renderEmptyChatArea();
    }
}

function updateMessagesBadge() {
    if (!userConversations) return;
    let totalUnread = 0;
    userConversations.forEach(c => totalUnread += (c.unreadCount || 0));

    const badge = document.getElementById('sidebarMessagesBadge');
    if (badge) {
        badge.innerText = totalUnread > 0 ? totalUnread : '0';
        badge.style.display = totalUnread > 0 ? 'inline-block' : 'none';
    }
}

function renderConversationsSidebarList(list) {
    const container = document.getElementById('conversationsList');
    if (!container) return;

    if (list.length === 0) {
        container.innerHTML = `
            <div style="padding: 2rem 1rem; text-align: center; color: var(--text-muted); font-size: 0.88rem;">
                No conversations yet.<br>Connect with people to start chatting.
            </div>
        `;
        return;
    }

    container.innerHTML = list.map(c => {
        const other = c.otherUser;
        const isActive = c.id === activeConversationId;
        const unread = c.unreadCount > 0 ? `<span class="conv-unread-badge">${c.unreadCount}</span>` : '';
        const timeAgo = formatTimeAgo(c.lastMessageAt);

        return `
            <div class="conv-item ${isActive ? 'active' : ''}" onclick="openConversation(${c.id}, ${other.userId})">
                <div class="conv-item-avatar">
                    ${other.profilePhoto ? `<img src="${escapeHtml(other.profilePhoto)}" alt="Avatar">` : getInitials(other.name)}
                </div>
                <div class="conv-item-body">
                    <div class="conv-item-top">
                        <span class="conv-item-name">${escapeHtml(other.name)}</span>
                        <span class="conv-item-time">${timeAgo}</span>
                    </div>
                    <div class="conv-item-preview" style="display:flex; justify-content:space-between; align-items:center;">
                        <span>${escapeHtml(c.lastMessage || 'No messages yet')}</span>
                        ${unread}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function openChatWithUser(event, targetUserId) {
    if (event) event.stopPropagation();

    if (!window.location.pathname.includes('/dashboard.html')) {
        window.location.href = `/dashboard.html?view=messages&userId=${targetUserId}`;
        return;
    }

    switchView('messages');

    try {
        const res = await authenticatedFetch(`/api/conversations/${targetUserId}`, { method: 'POST' });

        if (res.ok) {
            const conv = await res.json();
            activeConversationId = conv.id;
            activeRecipientId = targetUserId;
            await loadConversationsView();
        } else {
            const errData = await res.json();
            showToast(errData.message || "You must be connected with this user before starting a conversation.", 'error');
            renderEmptyChatArea(errData.message || "You must be connected with this user before starting a conversation.");
        }
    } catch (e) {
        console.error("Open chat with user error:", e);
        showToast("Error opening conversation.", 'error');
    }
}

async function openConversation(conversationId, recipientId) {
    activeConversationId = conversationId;
    activeRecipientId = recipientId;

    renderConversationsSidebarList(userConversations);

    try {
        const res = await authenticatedFetch(`/api/conversations/${conversationId}/messages?page=0&size=50`);

        if (res.ok) {
            const pageData = await res.json();
            renderActiveChatWindow(pageData.content || []);

            const targetConv = userConversations.find(c => c.id === conversationId);
            if (targetConv) targetConv.unreadCount = 0;
            updateMessagesBadge();
        }
    } catch (e) {
        console.error("Fetch conversation messages error:", e);
    }
}

function renderActiveChatWindow(messages) {
    const chatContainer = document.getElementById('view-messages');
    if (!chatContainer) return;

    const conv = userConversations.find(c => c.id === activeConversationId);
    const otherUser = conv ? conv.otherUser : null;

    const chatArea = chatContainer.querySelector('.chat-area');
    if (!chatArea) return;

    chatArea.innerHTML = `
        <div class="chat-header">
            <div class="avatar-circle" style="width: 40px; height: 40px; font-size: 0.9rem;" onclick="viewUserProfileByPublicId(${otherUser ? otherUser.userId : activeRecipientId})">
                ${otherUser && otherUser.profilePhoto ? `<img src="${escapeHtml(otherUser.profilePhoto)}" alt="Avatar">` : getInitials(otherUser ? otherUser.name : 'User')}
            </div>
            <div class="chat-header-info">
                <span class="chat-header-name" style="cursor:pointer;" onclick="viewUserProfileByPublicId(${otherUser ? otherUser.userId : activeRecipientId})">
                    ${escapeHtml(otherUser ? otherUser.name : 'Chat')}
                </span>
                <span class="chat-header-sub">
                    ${escapeHtml(otherUser ? (otherUser.profession || 'Community Member') + ' • ' + (otherUser.currentCity || otherUser.currentCountry || '') : '')}
                </span>
            </div>
        </div>

        <div class="chat-messages" id="chatMessagesStream">
            ${messages.map(m => renderSingleChatMessage(m)).join('')}
        </div>

        <div class="chat-input-box">
            <input type="text" id="chatMessageInput" placeholder="Type a message..." onkeydown="if(event.key==='Enter') sendChatMessage()">
            <button class="btn-primary" onclick="sendChatMessage()">Send</button>
        </div>
    `;

    scrollChatToBottom();
}

function renderSingleChatMessage(m) {
    const isSentByMe = currentUser && m.senderId === currentUser.id;
    const timeStr = new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const readStatus = isSentByMe ? (m.readAt ? '✓✓ Read' : '✓ Sent') : '';

    return `
        <div class="chat-message-row ${isSentByMe ? 'sent' : 'received'}" id="msg-${m.id}">
            <div class="chat-bubble">
                ${escapeHtml(m.content)}
            </div>
            <div class="chat-message-meta">
                <span>${timeStr}</span>
                ${readStatus ? `<span>• ${readStatus}</span>` : ''}
            </div>
        </div>
    `;
}

async function sendChatMessage() {
    const input = document.getElementById('chatMessageInput');
    if (!input) return;

    const text = input.value.trim();
    if (!text || !activeConversationId || !activeRecipientId) return;

    input.value = '';

    const payload = {
        conversationId: activeConversationId,
        recipientId: activeRecipientId,
        content: text
    };

    if (stompClient && stompClient.connected) {
        stompClient.send("/app/chat.send", {}, JSON.stringify(payload));
    } else {
        try {
            const res = await authenticatedFetch(`/api/conversations/${activeConversationId}/messages`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const msgObj = await res.json();
                handleIncomingChatMessage(msgObj);
            } else {
                const errData = await res.json();
                showToast(errData.message || "Could not send message.", 'error');
            }
        } catch (e) {
            console.error("REST send message fallback error:", e);
        }
    }
}

function handleIncomingChatMessage(msgObj) {
    if (msgObj.conversationId === activeConversationId) {
        const stream = document.getElementById('chatMessagesStream');
        if (stream) {
            stream.insertAdjacentHTML('beforeend', renderSingleChatMessage(msgObj));
            scrollChatToBottom();
        }
    }

    loadConversationsView();
}

function scrollChatToBottom() {
    const stream = document.getElementById('chatMessagesStream');
    if (stream) {
        stream.scrollTop = stream.scrollHeight;
    }
}

function renderEmptyChatArea(messageText = null) {
    const chatContainer = document.getElementById('view-messages');
    if (!chatContainer) return;

    const chatArea = chatContainer.querySelector('.chat-area');
    if (!chatArea) return;

    chatArea.innerHTML = `
        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; text-align: center; padding: 2rem; color: var(--text-muted);">
            <div style="font-size: 3rem; margin-bottom: 0.75rem;">💬</div>
            <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">Private Messages</h3>
            <p style="font-size: 0.88rem; max-width: 360px; margin-top: 0.25rem;">
                ${escapeHtml(messageText || "Connect with people from your college or destination to start chatting.")}
            </p>
            <button class="btn-primary" style="margin-top: 1rem;" onclick="switchView('people')">🔍 Discover People</button>
        </div>
    `;
}

function formatTimeAgo(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffSec = Math.floor((now - date) / 1000);

    if (diffSec < 60) return 'just now';
    if (diffSec < 3600) return `${Math.floor(diffSec / 60)}m`;
    if (diffSec < 86400) return `${Math.floor(diffSec / 3600)}h`;
    return date.toLocaleDateString();
}

/**
 * ==========================================================================
 * Phase 7 — Jobs & Opportunities + Actionable Social Profiles Engine
 * ==========================================================================
 */

let jobsCurrentTab = 'all'; // 'all', 'saved', 'my'
let jobsCurrentPage = 0;

async function initJobsPage() {
    await fetchMyUserData();

    const urlParams = new URLSearchParams(window.location.search);
    const viewParam = urlParams.get('view');
    const actionParam = urlParams.get('action');

    if (viewParam === 'saved') {
        switchJobsTab('saved');
    } else if (viewParam === 'my') {
        switchJobsTab('my');
    } else {
        switchJobsTab('all');
    }

    if (actionParam === 'new') {
        openPostJobModal();
    }

    const searchInput = document.getElementById('jobSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') applyJobFilters();
        });
    }
}

function switchJobsTab(tab) {
    jobsCurrentTab = tab;
    jobsCurrentPage = 0;

    const tabAll = document.getElementById('tabAllJobs');
    const tabSaved = document.getElementById('tabSavedJobs');
    const tabMy = document.getElementById('tabMyJobs');
    const filterCard = document.getElementById('jobsFilterCard');

    if (tabAll) tabAll.classList.toggle('active', tab === 'all');
    if (tabSaved) tabSaved.classList.toggle('active', tab === 'saved');
    if (tabMy) tabMy.classList.toggle('active', tab === 'my');

    if (filterCard) {
        filterCard.style.display = (tab === 'all') ? 'block' : 'none';
    }

    fetchJobs(0);
}

async function fetchJobs(page = 0) {
    jobsCurrentPage = page;
    const container = document.getElementById('jobsFeedContainer');
    if (!container) return;

    container.innerHTML = `
        <div style="text-align: center; color: var(--text-muted); padding: 3rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
            Loading job opportunities...
        </div>
    `;

    let url = `/api/jobs?page=${page}&size=10`;

    if (jobsCurrentTab === 'saved') {
        url = `/api/jobs/saved?page=${page}&size=10`;
    } else if (jobsCurrentTab === 'my') {
        url = `/api/jobs/my?page=${page}&size=10`;
    } else {
        const keyword = document.getElementById('jobSearchInput')?.value.trim() || '';
        const country = document.getElementById('filterCountry')?.value || '';
        const city = document.getElementById('filterCity')?.value.trim() || '';
        const empType = document.getElementById('filterEmploymentType')?.value || '';
        const workMode = document.getElementById('filterWorkMode')?.value || '';

        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', 10);
        if (keyword) params.append('keyword', keyword);
        if (country) params.append('country', country);
        if (city) params.append('city', city);
        if (empType) params.append('employmentType', empType);
        if (workMode) params.append('workMode', workMode);

        url = `/api/jobs?${params.toString()}`;
    }

    try {
        const response = await authenticatedFetch(url);

        if (response.ok) {
            const pageData = await response.json();
            const items = pageData.content || [];

            if (items.length === 0) {
                renderEmptyJobsState(container);
            } else {
                container.innerHTML = items.map(item => {
                    const job = item.job ? item.job : item;
                    return renderJobCardHtml(job);
                }).join('');

                renderJobsPagination(pageData);
            }
        } else {
            container.innerHTML = `
                <div style="text-align: center; color: var(--danger-text); padding: 2rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
                    Unable to load job opportunities.
                </div>
            `;
        }
    } catch (err) {
        console.error("Fetch jobs error:", err);
    }
}

function renderEmptyJobsState(container) {
    if (jobsCurrentTab === 'saved') {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 3rem; margin-bottom: 0.5rem;">🔖</div>
                <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--text-main);">You haven't saved any jobs yet</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 400px; margin: 0.35rem auto 1.25rem auto;">
                    Bookmark relevant roles to review and apply later.
                </p>
                <button class="btn-primary" onclick="switchJobsTab('all')">Explore Jobs</button>
            </div>
        `;
    } else if (jobsCurrentTab === 'my') {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 3rem; margin-bottom: 0.5rem;">📋</div>
                <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--text-main);">You haven't posted any jobs</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 400px; margin: 0.35rem auto 1.25rem auto;">
                    Share job openings with fellow expats and students moving abroad.
                </p>
                <button class="btn-primary" onclick="openPostJobModal()">+ Post a Job</button>
            </div>
        `;
    } else {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 3rem; margin-bottom: 0.5rem;">🌐</div>
                <h3 style="font-size: 1.15rem; font-weight: 700; color: var(--text-main);">No job opportunities yet</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 400px; margin: 0.35rem auto 1.25rem auto;">
                    Be the first to share an opportunity with the ConnectAbroad network.
                </p>
                <button class="btn-primary" onclick="openPostJobModal()">+ Post a Job</button>
            </div>
        `;
    }
}

function renderJobCardHtml(job) {
    const poster = job.postedBy || {};
    const empTypeClass = `type-${(job.employmentType || '').toLowerCase().replace('_', '')}`;
    const workModeClass = `work-${(job.workMode || '').toLowerCase().replace('_', '')}`;

    const empTypeDisplay = (job.employmentType || '').replace('_', '-');
    const workModeDisplay = job.workMode || '';

    const skills = job.requiredSkills ? job.requiredSkills.split(',') : [];
    const isSaved = job.saved || false;
    const isClosed = job.status === 'CLOSED';

    return `
        <div class="job-card" id="job-card-${job.id}">
            <div class="job-card-header">
                <div class="job-poster-info">
                    <div class="avatar-circle" onclick="viewUserProfileByPublicId(${poster.userId})" style="cursor:pointer; width: 44px; height: 44px;">
                        ${poster.profilePhoto ? `<img src="${escapeHtml(poster.profilePhoto)}" alt="Avatar">` : getInitials(poster.name)}
                    </div>
                    <div>
                        <div class="job-title" onclick="window.location.href='/job.html?id=${job.id}'">${escapeHtml(job.title)}</div>
                        <div class="job-company">${escapeHtml(job.companyName)}</div>
                        <div style="font-size: 0.78rem; color: var(--text-light); margin-top: 0.15rem;">
                            Posted by <strong class="clickable-user" onclick="viewUserProfileByPublicId(${poster.userId})">${escapeHtml(poster.name)}</strong> (${escapeHtml(poster.profession || 'Expat Member')})
                        </div>
                    </div>
                </div>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    ${isClosed ? `<span class="job-badge badge-status-closed">CLOSED</span>` : `<span class="job-badge badge-status-active">ACTIVE</span>`}
                </div>
            </div>

            <div class="job-badges">
                <span class="job-badge ${empTypeClass}">💼 ${escapeHtml(empTypeDisplay)}</span>
                <span class="job-badge ${workModeClass}">🏠 ${escapeHtml(workModeDisplay)}</span>
                <span class="job-badge work-onsite">📍 ${escapeHtml(job.city)}, ${escapeHtml(job.country)}</span>
                ${job.salaryMin ? `<span class="job-badge work-onsite">💰 ${job.currency || '$'}${job.salaryMin}${job.salaryMax ? ' - ' + job.salaryMax : ''}</span>` : ''}
            </div>

            <p style="font-size: 0.88rem; color: var(--text-main); line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin: 0;">
                ${escapeHtml(job.description)}
            </p>

            ${skills.length > 0 ? `
                <div class="job-skills">
                    ${skills.map(s => `<span class="job-skill-chip">${escapeHtml(s.trim())}</span>`).join('')}
                </div>
            ` : ''}

            <div class="job-card-footer">
                <span class="job-time">Posted ${formatTimeAgo(job.createdAt)}</span>
                
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <button class="btn-save-job ${isSaved ? 'saved' : ''}" onclick="toggleSaveJob(event, ${job.id}, ${isSaved})">
                        ${isSaved ? '♥ Saved' : '♡ Save'}
                    </button>
                    <button class="btn-primary" style="font-size: 0.82rem; padding: 0.4rem 0.85rem;" onclick="window.location.href='/job.html?id=${job.id}'">
                        View Job
                    </button>
                    ${job.mine ? `
                        ${!isClosed ? `<button class="btn-cancel" style="font-size: 0.82rem; padding: 0.4rem 0.65rem;" onclick="handleCloseJob(${job.id})">🔒 Close</button>` : ''}
                        <button class="btn-remove" style="font-size: 0.82rem; padding: 0.4rem 0.65rem;" onclick="handleDeleteJob(${job.id})">🗑️ Delete</button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
}

function renderJobsPagination(pageData) {
    const container = document.getElementById('jobsPagination');
    if (!container) return;

    if (pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';
    if (!pageData.first) {
        html += `<button class="btn-secondary" onclick="fetchJobs(${pageData.pageNumber - 1})">← Previous</button>`;
    }
    html += `<span style="align-self: center; font-size: 0.88rem; color: var(--text-muted);">Page ${pageData.pageNumber + 1} of ${pageData.totalPages}</span>`;
    if (!pageData.last) {
        html += `<button class="btn-secondary" onclick="fetchJobs(${pageData.pageNumber + 1})">Next →</button>`;
    }
    container.innerHTML = html;
}

function applyJobFilters() {
    jobsCurrentPage = 0;
    fetchJobs(0);
}

function resetJobFilters() {
    if (document.getElementById('jobSearchInput')) document.getElementById('jobSearchInput').value = '';
    if (document.getElementById('filterCountry')) document.getElementById('filterCountry').value = '';
    if (document.getElementById('filterCity')) document.getElementById('filterCity').value = '';
    if (document.getElementById('filterEmploymentType')) document.getElementById('filterEmploymentType').value = '';
    if (document.getElementById('filterWorkMode')) document.getElementById('filterWorkMode').value = '';

    applyJobFilters();
}

async function toggleSaveJob(event, jobId, isCurrentlySaved) {
    if (event) event.stopPropagation();

    const method = isCurrentlySaved ? 'DELETE' : 'POST';
    try {
        const response = await authenticatedFetch(`/api/jobs/${jobId}/save`, { method: method });
        if (response.ok) {
            showToast(isCurrentlySaved ? "Job removed from saved items" : "Job saved successfully!", "info");
            fetchJobs(jobsCurrentPage);
        } else {
            showToast("Failed to update saved job status", "error");
        }
    } catch (err) {
        console.error("Toggle save job error:", err);
    }
}

function openPostJobModal() {
    const modal = document.getElementById('postJobModal');
    if (modal) modal.style.display = 'flex';
}

function closePostJobModal() {
    const modal = document.getElementById('postJobModal');
    if (modal) modal.style.display = 'none';
}

async function handlePostJobSubmit(e) {
    e.preventDefault();

    const title = document.getElementById('jobTitleInput')?.value.trim();
    const companyName = document.getElementById('jobCompanyInput')?.value.trim();
    const country = document.getElementById('jobCountryInput')?.value;
    const city = document.getElementById('jobCityInput')?.value.trim();
    const empType = document.getElementById('jobEmpTypeInput')?.value;
    const workMode = document.getElementById('jobWorkModeInput')?.value;
    const salaryMin = document.getElementById('jobSalaryMin')?.value;
    const salaryMax = document.getElementById('jobSalaryMax')?.value;
    const currency = document.getElementById('jobCurrency')?.value;
    const experience = document.getElementById('jobExperience')?.value.trim();
    const skills = document.getElementById('jobSkills')?.value.trim();
    const appUrl = document.getElementById('jobAppUrl')?.value.trim();
    const contactEmail = document.getElementById('jobContactEmail')?.value.trim();
    const description = document.getElementById('jobDescInput')?.value.trim();

    if (!title || !companyName || !country || !city || !description) {
        showToast("Please fill all required fields", "error");
        return;
    }

    const payload = {
        title, companyName, country, city,
        employmentType: empType,
        workMode: workMode,
        salaryMin: salaryMin ? parseFloat(salaryMin) : undefined,
        salaryMax: salaryMax ? parseFloat(salaryMax) : undefined,
        currency, experienceRequired: experience,
        requiredSkills: skills, applicationUrl: appUrl,
        contactEmail, description
    };

    try {
        const response = await authenticatedFetch('/api/jobs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            showToast("Job opportunity posted successfully!", "success");
            closePostJobModal();
            document.getElementById('postJobForm')?.reset();
            if (window.location.pathname.includes('/jobs.html')) {
                switchJobsTab('my');
            } else {
                window.location.href = '/jobs.html?view=my';
            }
        } else {
            const errData = await response.json();
            showToast(errData.message || "Failed to post job.", "error");
        }
    } catch (err) {
        console.error("Post job error:", err);
        showToast("Error creating job opportunity", "error");
    }
}

async function handleCloseJob(jobId) {
    if (!confirm("Are you sure you want to close this job? Closed jobs will no longer appear in active feeds.")) return;

    try {
        const response = await authenticatedFetch(`/api/jobs/${jobId}/close`, { method: 'PATCH' });
        if (response.ok) {
            showToast("Job has been closed.", "info");
            fetchJobs(jobsCurrentPage);
        }
    } catch (err) {
        console.error("Close job error:", err);
    }
}

async function handleDeleteJob(jobId) {
    if (!confirm("Are you sure you want to permanently delete this job listing?")) return;

    try {
        const response = await authenticatedFetch(`/api/jobs/${jobId}`, { method: 'DELETE' });
        if (response.ok) {
            showToast("Job listing deleted.", "info");
            fetchJobs(jobsCurrentPage);
        }
    } catch (err) {
        console.error("Delete job error:", err);
    }
}

/**
 * Job Detail Page Engine (job.html?id=27)
 */
async function initJobDetailPage() {
    await fetchMyUserData();

    const urlParams = new URLSearchParams(window.location.search);
    const jobId = urlParams.get('id');

    const container = document.getElementById('jobDetailContainer');
    const posterWidget = document.getElementById('jobPosterDetails');

    if (!jobId || !container) return;

    try {
        const response = await authenticatedFetch(`/api/jobs/${jobId}`);

        if (response.ok) {
            const job = await response.json();
            renderJobDetailPage(container, posterWidget, job);
        } else {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
                    <h2>Job Listing Not Found</h2>
                    <p style="color: var(--text-muted); margin-top: 0.5rem;">This job listing may have been closed or deleted by the poster.</p>
                    <button class="btn-primary" style="margin-top: 1rem;" onclick="window.location.href='/jobs.html'">Explore Active Jobs</button>
                </div>
            `;
        }
    } catch (err) {
        console.error("Fetch job detail error:", err);
    }
}

function renderJobDetailPage(container, posterWidget, job) {
    const poster = job.postedBy || {};
    const empTypeDisplay = (job.employmentType || '').replace('_', '-');
    const workModeDisplay = job.workMode || '';
    const skills = job.requiredSkills ? job.requiredSkills.split(',') : [];
    const isSaved = job.saved || false;

    container.innerHTML = `
        <div style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.5rem; box-shadow: var(--shadow-sm);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; flex-wrap: wrap;">
                <div>
                    <h1 style="font-size: 1.5rem; font-weight: 700; color: var(--text-main);">${escapeHtml(job.title)}</h1>
                    <div style="font-size: 1.1rem; font-weight: 600; color: var(--text-muted); margin-top: 0.2rem;">${escapeHtml(job.companyName)}</div>
                </div>
                <div>
                    ${job.status === 'CLOSED' ? `<span class="job-badge badge-status-closed">CLOSED</span>` : `<span class="job-badge badge-status-active">ACTIVE OPPORTUNITY</span>`}
                </div>
            </div>

            <div class="job-badges" style="margin-top: 1rem;">
                <span class="job-badge type-fulltime">💼 ${escapeHtml(empTypeDisplay)}</span>
                <span class="job-badge work-remote">🏠 ${escapeHtml(workModeDisplay)}</span>
                <span class="job-badge work-onsite">📍 ${escapeHtml(job.city)}, ${escapeHtml(job.country)}</span>
                ${job.salaryMin ? `<span class="job-badge work-onsite">💰 ${job.currency || '$'}${job.salaryMin}${job.salaryMax ? ' - ' + job.salaryMax : ''}</span>` : ''}
                ${job.experienceRequired ? `<span class="job-badge work-onsite">⌛ ${escapeHtml(job.experienceRequired)}</span>` : ''}
            </div>

            <div style="margin-top: 1.5rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.75rem;">About the Role</h3>
                <p style="font-size: 0.95rem; color: var(--text-main); line-height: 1.6; white-space: pre-line;">${escapeHtml(job.description)}</p>
            </div>

            ${skills.length > 0 ? `
                <div style="margin-top: 1.25rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
                    <h3 style="font-size: 0.95rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.65rem;">Required Skills</h3>
                    <div class="job-skills">
                        ${skills.map(s => `<span class="job-skill-chip">${escapeHtml(s.trim())}</span>`).join('')}
                    </div>
                </div>
            ` : ''}

            <!-- Application Action Buttons -->
            <div style="margin-top: 1.5rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem; display: flex; gap: 1rem; flex-wrap: wrap; align-items: center;">
                ${job.applicationUrl ? `
                    <a href="${escapeHtml(job.applicationUrl)}" target="_blank" class="btn-primary" style="padding: 0.65rem 1.25rem; font-size: 0.95rem;">🚀 Apply Now</a>
                ` : job.contactEmail ? `
                    <a href="mailto:${escapeHtml(job.contactEmail)}" class="btn-primary" style="padding: 0.65rem 1.25rem; font-size: 0.95rem;">✉️ Apply via Email (${escapeHtml(job.contactEmail)})</a>
                ` : `
                    <button class="btn-primary" style="padding: 0.65rem 1.25rem; font-size: 0.95rem;" onclick="openChatWithUser(event, ${poster.userId})">💬 Contact Poster</button>
                `}

                <button class="btn-save-job ${isSaved ? 'saved' : ''}" style="padding: 0.65rem 1.1rem; font-size: 0.95rem;" onclick="toggleSaveJob(event, ${job.id}, ${isSaved})">
                    ${isSaved ? '♥ Saved Job' : '♡ Save Job'}
                </button>
            </div>
        </div>
    `;

    if (posterWidget) {
        posterWidget.innerHTML = `
            <div style="display: flex; flex-direction: column; align-items: center; text-align: center; gap: 0.75rem;">
                <div class="avatar-circle" style="width: 72px; height: 72px; font-size: 1.5rem; cursor: pointer;" onclick="viewUserProfileByPublicId(${poster.userId})">
                    ${poster.profilePhoto ? `<img src="${escapeHtml(poster.profilePhoto)}" alt="Avatar">` : getInitials(poster.name)}
                </div>
                <div>
                    <div style="font-size: 1.05rem; font-weight: 700; color: var(--text-main); cursor: pointer;" onclick="viewUserProfileByPublicId(${poster.userId})">
                        ${escapeHtml(poster.name)}
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.15rem;">
                        ${escapeHtml(poster.profession || 'Community Member')}
                    </div>
                    <div style="font-size: 0.8rem; color: var(--text-light); margin-top: 0.25rem;">
                        📍 ${escapeHtml(poster.currentCity || '')} ${poster.currentCountry ? '(' + escapeHtml(poster.currentCountry) + ')' : ''}
                    </div>
                </div>

                <div style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%; margin-top: 0.5rem;">
                    <button class="btn-secondary" style="width: 100%;" onclick="viewUserProfileByPublicId(${poster.userId})">
                        👤 View Profile
                    </button>
                    ${currentUser && currentUser.id !== poster.userId ? `
                        <button class="btn-primary" style="width: 100%;" onclick="openChatWithUser(event, ${poster.userId})">
                            💬 Message Poster
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
    }
}

/**
 * ==========================================================================
 * PHASE 8: Real-Time In-App Notification System Frontend Engine
 * ==========================================================================
 */

function toggleNotificationDropdown(event) {
    if (event) event.stopPropagation();
    const dropdown = document.getElementById('notificationDropdown');
    const userDropdown = document.getElementById('userDropdown');
    if (userDropdown) userDropdown.style.display = 'none';

    if (!dropdown) return;

    const isShowing = dropdown.style.display === 'flex' || dropdown.style.display === 'block';
    if (isShowing) {
        dropdown.style.display = 'none';
    } else {
        dropdown.style.display = 'flex';
        loadDropdownNotifications();
    }
}

document.addEventListener('click', (e) => {
    const dropdown = document.getElementById('notificationDropdown');
    const btn = document.getElementById('navNotificationBtn');
    if (dropdown && dropdown.style.display !== 'none') {
        if (!dropdown.contains(e.target) && (!btn || !btn.contains(e.target))) {
            dropdown.style.display = 'none';
        }
    }
});

async function loadDropdownNotifications() {
    const listContainer = document.getElementById('notificationDropdownList');
    if (!listContainer) return;

    listContainer.innerHTML = `<div style="padding:1.5rem; text-align:center; color:var(--text-muted); font-size:0.85rem;">Loading notifications...</div>`;

    try {
        const res = await authenticatedFetch('/api/notifications?page=0&size=5');
        if (res.ok) {
            const pageData = await res.json();
            const notifications = pageData.content || [];
            renderDropdownNotificationsList(notifications, listContainer);
        } else {
            listContainer.innerHTML = `<div style="padding:1.5rem; text-align:center; color:var(--text-muted); font-size:0.85rem;">Failed to load notifications</div>`;
        }
    } catch (err) {
        console.error('Error loading dropdown notifications:', err);
        listContainer.innerHTML = `<div style="padding:1.5rem; text-align:center; color:var(--text-muted); font-size:0.85rem;">Error loading notifications</div>`;
    }
}

function renderDropdownNotificationsList(notifications, container) {
    if (!notifications || notifications.length === 0) {
        container.innerHTML = `<div style="padding: 2rem 1rem; text-align: center; color: var(--text-muted); font-size: 0.85rem;">No notifications yet</div>`;
        return;
    }

    let html = '';
    notifications.forEach(n => {
        const icon = getNotificationIcon(n.type);
        const actorName = n.actor ? escapeHtml(n.actor.name) : 'System';
        const avatarHtml = n.actor && n.actor.profilePhoto
            ? `<img src="${escapeHtml(n.actor.profilePhoto)}" alt="${actorName}" style="width:36px; height:36px; border-radius:50%; object-fit:cover;">`
            : `<div class="avatar-circle" style="width:36px; height:36px; font-size:0.85rem;">${getInitials(actorName)}</div>`;

        const relTime = formatRelativeTime(n.createdAt);
        const unreadClass = !n.isRead ? 'unread' : '';

        html += `
            <div class="notification-item ${unreadClass}" onclick="handleNotificationItemClick(event, ${n.id}, '${n.referenceType}', ${n.referenceId}, ${n.isRead})">
                <div class="notification-icon-wrapper">
                    ${avatarHtml}
                    <div class="notification-type-badge">${icon}</div>
                </div>
                <div class="notification-content">
                    <div class="notification-title">${escapeHtml(n.title)}</div>
                    <div class="notification-message">${escapeHtml(n.message)}</div>
                    <div class="notification-time">${relTime}</div>
                </div>
                ${!n.isRead ? '<div class="notification-unread-dot"></div>' : ''}
            </div>
        `;
    });

    container.innerHTML = html;
}

function getNotificationIcon(type) {
    switch (type) {
        case 'CONNECTION_REQUEST': return '👤';
        case 'CONNECTION_ACCEPTED': return '👤';
        case 'POST_LIKE': return '❤️';
        case 'POST_COMMENT': return '💬';
        case 'NEW_MESSAGE': return '📩';
        case 'JOB_POSTED':
        case 'JOB_RECOMMENDATION': return '💼';
        default: return '🔔';
    }
}

async function handleNotificationItemClick(event, notificationId, referenceType, referenceId, isRead) {
    if (event) event.stopPropagation();

    const dropdown = document.getElementById('notificationDropdown');
    if (dropdown) dropdown.style.display = 'none';

    if (!isRead) {
        try {
            await authenticatedFetch(`/api/notifications/${notificationId}/read`, { method: 'PATCH' });
            updateNotificationBadge();
        } catch (err) {
            console.error('Error marking notification as read:', err);
        }
    }

    navigateNotificationDestination(referenceType, referenceId);
}

function navigateNotificationDestination(referenceType, referenceId) {
    if (!referenceType || !referenceId) return;

    if (referenceType === 'PROFILE') {
        window.location.href = `/profile.html?id=${referenceId}`;
    } else if (referenceType === 'POST') {
        window.location.href = `/dashboard.html?post=${referenceId}`;
    } else if (referenceType === 'CONNECTION') {
        window.location.href = `/connections.html`;
    } else if (referenceType === 'MESSAGE') {
        window.location.href = `/dashboard.html?view=messages&userId=${referenceId}`;
    } else if (referenceType === 'JOB') {
        window.location.href = `/job.html?id=${referenceId}`;
    } else {
        window.location.href = `/dashboard.html`;
    }
}

async function markAllNotificationsAsRead(event) {
    if (event) event.stopPropagation();
    try {
        await authenticatedFetch('/api/notifications/read-all', { method: 'PATCH' });
        updateNotificationBadge();

        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown && dropdown.style.display !== 'none') {
            loadDropdownNotifications();
        }

        if (window.location.pathname.includes('/notifications.html')) {
            loadFullNotificationsPage(notifCurrentPage);
        }
        showToast('All notifications marked as read', 'success');
    } catch (err) {
        console.error('Error marking all notifications as read:', err);
    }
}

function handleIncomingNotification(notification) {
    updateNotificationBadge();

    const icon = getNotificationIcon(notification.type);
    showToast(`${icon} ${notification.message}`, 'info');

    const dropdown = document.getElementById('notificationDropdown');
    if (dropdown && dropdown.style.display !== 'none') {
        loadDropdownNotifications();
    }

    if (window.location.pathname.includes('/notifications.html')) {
        loadFullNotificationsPage(0);
    }
}

let notifCurrentPage = 0;
const notifPageSize = 15;

async function initFullNotificationsPage() {
    notifCurrentPage = 0;
    await loadFullNotificationsPage(0);
}

async function loadFullNotificationsPage(page) {
    const container = document.getElementById('fullNotificationList');
    if (!container) return;

    notifCurrentPage = page;
    container.innerHTML = `<div style="padding:2rem; text-align:center; color:var(--text-muted);">Loading notifications...</div>`;

    try {
        const res = await authenticatedFetch(`/api/notifications?page=${page}&size=${notifPageSize}`);
        if (res.ok) {
            const pageData = await res.json();
            renderFullNotificationsList(pageData, container);
            updateNotificationPaginationControls(pageData);
        } else {
            container.innerHTML = `<div style="padding:2rem; text-align:center; color:var(--text-muted);">Failed to load notifications.</div>`;
        }
    } catch (err) {
        console.error('Error loading full notifications page:', err);
        container.innerHTML = `<div style="padding:2rem; text-align:center; color:var(--text-muted);">Error loading notifications.</div>`;
    }
}

function renderFullNotificationsList(pageData, container) {
    const notifications = pageData.content || [];
    if (notifications.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1rem;">
                <div style="font-size: 3rem; margin-bottom: 0.5rem;">🔔</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.25rem;">No notifications found</h3>
                <p style="font-size: 0.85rem; color: var(--text-muted);">You're all caught up!</p>
            </div>
        `;
        return;
    }

    const today = [];
    const yesterday = [];
    const earlier = [];

    const now = new Date();
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const startOfYesterday = startOfToday - (24 * 60 * 60 * 1000);

    notifications.forEach(n => {
        const time = new Date(n.createdAt).getTime();
        if (time >= startOfToday) {
            today.push(n);
        } else if (time >= startOfYesterday) {
            yesterday.push(n);
        } else {
            earlier.push(n);
        }
    });

    let html = '';

    if (today.length > 0) {
        html += `<div class="notification-section-header">TODAY</div>`;
        today.forEach(n => html += renderFullNotificationCard(n));
    }

    if (yesterday.length > 0) {
        html += `<div class="notification-section-header">YESTERDAY</div>`;
        yesterday.forEach(n => html += renderFullNotificationCard(n));
    }

    if (earlier.length > 0) {
        html += `<div class="notification-section-header">EARLIER</div>`;
        earlier.forEach(n => html += renderFullNotificationCard(n));
    }

    container.innerHTML = html;
}

function renderFullNotificationCard(n) {
    const icon = getNotificationIcon(n.type);
    const actorName = n.actor ? escapeHtml(n.actor.name) : 'System';
    const avatarHtml = n.actor && n.actor.profilePhoto
        ? `<img src="${escapeHtml(n.actor.profilePhoto)}" alt="${actorName}" style="width:42px; height:42px; border-radius:50%; object-fit:cover;">`
        : `<div class="avatar-circle" style="width:42px; height:42px; font-size:1rem;">${getInitials(actorName)}</div>`;

    const relTime = formatRelativeTime(n.createdAt);
    const unreadClass = !n.isRead ? 'unread' : '';

    return `
        <div class="notification-item ${unreadClass}" style="padding: 1rem;" onclick="handleNotificationItemClick(event, ${n.id}, '${n.referenceType}', ${n.referenceId}, ${n.isRead})">
            <div class="notification-icon-wrapper">
                ${avatarHtml}
                <div class="notification-type-badge">${icon}</div>
            </div>
            <div class="notification-content">
                <div class="notification-title" style="font-size: 0.95rem;">${escapeHtml(n.title)}</div>
                <div class="notification-message" style="font-size: 0.88rem; color: var(--text-main); margin-top: 0.15rem;">${escapeHtml(n.message)}</div>
                <div class="notification-time" style="margin-top: 0.35rem;">${relTime}</div>
            </div>
            <div style="display: flex; align-items: center; gap: 0.5rem;">
                ${!n.isRead ? '<div class="notification-unread-dot" style="width: 10px; height: 10px;"></div>' : ''}
                <button onclick="deleteSingleNotification(event, ${n.id})" title="Delete notification" style="background: none; border: none; font-size: 0.9rem; color: var(--text-light); cursor: pointer; padding: 0.25rem;">🗑️</button>
            </div>
        </div>
    `;
}

async function deleteSingleNotification(event, notificationId) {
    if (event) event.stopPropagation();
    try {
        const res = await authenticatedFetch(`/api/notifications/${notificationId}`, { method: 'DELETE' });
        if (res.ok) {
            updateNotificationBadge();
            loadFullNotificationsPage(notifCurrentPage);
            showToast('Notification deleted', 'success');
        }
    } catch (err) {
        console.error('Error deleting notification:', err);
    }
}

function updateNotificationPaginationControls(pageData) {
    const prevBtn = document.getElementById('notifPrevBtn');
    const nextBtn = document.getElementById('notifNextBtn');
    const indicator = document.getElementById('notifPageIndicator');

    if (prevBtn) prevBtn.disabled = pageData.page === 0;
    if (nextBtn) nextBtn.disabled = pageData.last;
    if (indicator) {
        const totalPages = pageData.totalPages === 0 ? 1 : pageData.totalPages;
        indicator.innerText = `Page ${pageData.page + 1} of ${totalPages}`;
    }
}

function changeNotificationPage(delta) {
    const newPage = notifCurrentPage + delta;
    if (newPage >= 0) {
        loadFullNotificationsPage(newPage);
    }
}

function formatRelativeTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffSec = Math.floor((now - date) / 1000);

    if (diffSec < 60) return 'Just now';
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin} min ago`;
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) return `${diffHour} hr ago`;
    const diffDays = Math.floor(diffHour / 24);
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;

    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}


