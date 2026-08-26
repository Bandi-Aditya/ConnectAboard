/**
 * ConnectAbroad - Phase 3 Frontend App Controller
 * Manages view routing, real User Profile API integration, profile completion tracking,
 * profile editing modal, real People Discovery API, dynamic specifications filtering,
 * match reasons engine ("Why This Person?"), feed rendering, and social interactions.
 */

document.addEventListener('DOMContentLoaded', () => {
    initAuth();
    initAppNavigation();
    updateNotificationBadge();
    renderFeed('all');
    renderRightSidebar();
    fetchPeopleDirectory(0);
    fetchSameCollegeSection();
    fetchDestinationSection();
    renderJobsView();
    renderHousingView();
    renderExploreView();
    renderMessagesView();
    initComposer();
});

// Global state
let currentUser = null;
let currentProfile = null;
let currentView = 'home';
let activeConversationId = 601;

// Phase 3 People Discovery State
let peopleCurrentPage = 0;
let peoplePageSize = 12;
let peopleTotalPages = 0;
let filterDebounceTimeout = null;

/**
 * Check JWT Token and load current user & real profile
 */
async function initAuth() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    const cachedUser = localStorage.getItem('user');
    if (cachedUser) {
        try {
            currentUser = JSON.parse(cachedUser);
            updateUserUI(currentUser, currentProfile);
        } catch (e) {}
    }

    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const userData = await response.json();
            currentUser = {
                id: userData.id,
                name: userData.name,
                email: userData.email,
                role: userData.role,
                userType: userData.userType
            };
            localStorage.setItem('user', JSON.stringify(currentUser));
            updateUserUI(currentUser, currentProfile);

            await fetchMyProfile();
        } else if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('user');
            window.location.href = '/login.html';
        }
    } catch (err) {
        console.warn("API Offline, using local session state", err);
    }
}

/**
 * Fetch authenticated user profile from GET /api/profiles/me
 */
async function fetchMyProfile() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch('/api/profiles/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            currentProfile = await response.json();
            updateUserUI(currentUser, currentProfile);
            renderProfileCompletionWidget(currentProfile);
            renderUserProfile(currentProfile);
            fetchSameCollegeSection();
            fetchDestinationSection();
        } else if (response.status === 404) {
            currentProfile = null;
            renderProfileCompletionWidget(null);
            renderUserProfile(null);
        }
    } catch (err) {
        console.warn("Could not fetch user profile", err);
    }
}

/**
 * Bind Logged-In User Details to UI
 */
function updateUserUI(user, profile) {
    if (!user) return;

    const nameToDisplay = profile ? profile.userName : user.name;

    const navName = document.getElementById('navUserName');
    if (navName) navName.innerText = nameToDisplay;

    const navAvatar = document.getElementById('navUserAvatar');
    if (navAvatar) {
        const photoOrInitials = profile && profile.profilePhoto ? profile.profilePhoto : getInitials(nameToDisplay);
        navAvatar.innerText = photoOrInitials;
        navAvatar.style.backgroundColor = '#2563eb';
    }

    const miniName = document.getElementById('miniUserName');
    if (miniName) miniName.innerText = nameToDisplay;

    const miniStatus = document.getElementById('miniUserStatus');
    if (miniStatus) {
        if (profile && profile.currentCity && profile.currentCountry) {
            miniStatus.innerText = profile.userType === 'ABROAD'
                ? `🟢 Living in ${profile.currentCity}`
                : `✈️ Target: ${profile.targetCity || profile.targetCountry || 'Abroad'}`;
        } else {
            miniStatus.innerText = user.userType === 'ABROAD' ? '🟢 Living Abroad' : '✈️ Planning to Move';
        }
    }

    const miniAvatar = document.getElementById('miniUserAvatar');
    if (miniAvatar) {
        miniAvatar.innerText = getInitials(nameToDisplay);
        miniAvatar.style.backgroundColor = '#2563eb';
    }

    const welcomeTitle = document.getElementById('welcomeTitle');
    if (welcomeTitle) {
        welcomeTitle.innerText = `Welcome back, ${nameToDisplay} 👋`;
    }
}

/**
 * Render Profile Completion Widget on Home Feed
 */
function renderProfileCompletionWidget(profile) {
    const widgetElem = document.getElementById('profileCompletionWidget');
    if (!widgetElem) return;

    if (!profile || profile.profileCompletion < 100) {
        const pct = profile ? profile.profileCompletion : 0;
        const chk = profile && profile.completionChecklist ? profile.completionChecklist : {};

        widgetElem.innerHTML = `
            <div class="completion-widget-card">
                <div class="completion-header">
                    <div>
                        <div class="completion-title">Complete your ConnectAbroad profile</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted);">
                            Complete your details to connect with alumni, students, and professionals abroad.
                        </div>
                    </div>
                    <div class="completion-percentage-badge">${pct}%</div>
                </div>
                
                <div class="progress-track">
                    <div class="progress-fill" style="width: ${pct}%;"></div>
                </div>

                <div class="checklist-grid">
                    <div class="checklist-item ${chk.basicInfo ? 'done' : 'pending'}">
                        ${chk.basicInfo ? '✓' : '✗'} Basic Information
                    </div>
                    <div class="checklist-item ${chk.college ? 'done' : 'pending'}">
                        ${chk.college ? '✓' : '✗'} College / Institution
                    </div>
                    <div class="checklist-item ${chk.hometown ? 'done' : 'pending'}">
                        ${chk.hometown ? '✓' : '✗'} Hometown / Origin
                    </div>
                    <div class="checklist-item ${chk.currentLocation ? 'done' : 'pending'}">
                        ${chk.currentLocation ? '✓' : '✗'} Current Location
                    </div>
                    <div class="checklist-item ${chk.profession ? 'done' : 'pending'}">
                        ${chk.profession ? '✓' : '✗'} Profession / Field
                    </div>
                    <div class="checklist-item ${chk.journey ? 'done' : 'pending'}">
                        ${chk.journey ? '✓' : '✗'} ${currentUser && currentUser.userType === 'ASPIRING' ? 'Target Destination' : 'Moved Year'}
                    </div>
                    <div class="checklist-item ${chk.profilePhoto ? 'done' : 'pending'}">
                        ${chk.profilePhoto ? '✓' : '✗'} Profile Photo
                    </div>
                    <div class="checklist-item ${chk.bio ? 'done' : 'pending'}">
                        ${chk.bio ? '✓' : '✗'} About & Bio
                    </div>
                </div>

                <div style="display: flex; gap: 0.75rem; align-items: center;">
                    <button class="btn-primary" onclick="openEditProfileModal()">
                        ${profile ? '✏️ Update Profile' : '🚀 Complete Profile'}
                    </button>
                    <button class="btn-secondary" onclick="switchView('profile')">
                        👤 View Profile
                    </button>
                </div>
            </div>
        `;
    } else {
        widgetElem.innerHTML = '';
    }
}

/**
 * Render Social Profile View Page
 */
function renderUserProfile(profile) {
    const profileContainer = document.getElementById('userProfileContainer');
    if (!profileContainer) return;

    if (!profile) {
        profileContainer.innerHTML = `
            <div class="profile-cover"></div>
            <div class="profile-body-card" style="text-align: center; padding: 3rem 1.5rem;">
                <div class="avatar-circle" style="width: 80px; height: 80px; font-size: 1.8rem; margin: -40px auto 1rem auto; background-color: var(--primary);">
                    ${getInitials(currentUser ? currentUser.name : 'User')}
                </div>
                <h2 style="font-size: 1.4rem; font-weight: 700; margin-bottom: 0.5rem;">Create Your ConnectAbroad Profile</h2>
                <p style="color: var(--text-muted); max-width: 500px; margin: 0 auto 1.5rem auto; font-size: 0.95rem;">
                    You haven't set up your profile yet. Add your college, hometown, location, profession, and abroad journey so fellow graduates and students can connect with you!
                </p>
                <button class="btn-primary" onclick="openEditProfileModal()" style="padding: 0.75rem 1.5rem; font-size: 0.95rem;">
                    🚀 Set Up My Profile Now
                </button>
            </div>
        `;
        return;
    }

    const isAbroad = profile.userType === 'ABROAD';
    const statusText = isAbroad
        ? `🟢 Living in ${escapeHtml(profile.currentCity || profile.currentCountry || 'Abroad')}`
        : `✈️ Planning to move to ${escapeHtml(profile.targetCity || profile.targetCountry || 'Abroad')}`;
    const statusClass = isAbroad ? 'badge-abroad' : 'badge-aspiring';

    const skillsList = profile.skills
        ? profile.skills.split(',').map(s => s.trim()).filter(Boolean)
        : [];

    profileContainer.innerHTML = `
        <div class="profile-cover"></div>
        <div class="profile-body-card">
            <div class="profile-avatar-row">
                <div class="avatar-circle profile-main-avatar" style="background-color: var(--primary);">
                    ${escapeHtml(profile.profilePhoto || getInitials(profile.userName || profile.name))}
                </div>
                <button class="btn-secondary" onclick="openEditProfileModal()">✏️ Edit Profile</button>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 style="font-size: 1.5rem; font-weight: 700; color: var(--text-main);">${escapeHtml(profile.userName || profile.name)}</h2>
                    <p style="color: var(--text-muted); font-size: 0.95rem; font-weight: 500;">
                        ${escapeHtml(profile.profession || 'Community Member')} ${profile.experienceYears ? '• ' + profile.experienceYears + ' yrs exp' : ''}
                    </p>
                </div>
                <span class="badge ${statusClass}" style="font-size: 0.85rem; padding: 0.4rem 0.85rem;">
                    ${statusText}
                </span>
            </div>

            <!-- Meta Details -->
            <div style="display: flex; flex-wrap: wrap; gap: 1.25rem; margin: 1.25rem 0; font-size: 0.9rem; color: var(--text-main);">
                <div>📍 <strong>Location:</strong> ${escapeHtml(profile.currentCity || '')} ${profile.currentCountry ? '(' + escapeHtml(profile.currentCountry) + ')' : ''}</div>
                <div>🎓 <strong>College:</strong> ${escapeHtml(profile.collegeName || 'N/A')}</div>
                <div>🏠 <strong>Hometown:</strong> ${escapeHtml(profile.hometown || 'N/A')}</div>
                <div>👥 <strong>Connections:</strong> ${profile.connectionCount || 0}</div>
            </div>

            <!-- About Section -->
            <div style="margin: 1.5rem 0;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">About</h3>
                <p style="font-size: 0.95rem; line-height: 1.6; color: var(--text-main); background: #f8fafc; padding: 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
                    ${profile.bio ? escapeHtml(profile.bio) : '<em>No bio added yet. Click "Edit Profile" to add an overview.</em>'}
                </p>
            </div>

            <!-- Education Section -->
            <div style="margin: 1.5rem 0;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">Education</h3>
                <div style="background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 1rem;">
                    <div style="font-weight: 700; font-size: 0.95rem; color: var(--text-main);">
                        🎓 ${escapeHtml(profile.collegeName || 'College Not Specified')}
                    </div>
                    <div style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0.25rem;">
                        ${escapeHtml(profile.degree || 'Degree Program')} ${profile.graduationYear ? '• Graduated Class of ' + profile.graduationYear : ''}
                    </div>
                    ${profile.collegeCity ? `<div style="font-size: 0.8rem; color: var(--text-light); margin-top: 0.15rem;">📍 ${escapeHtml(profile.collegeCity)}, ${escapeHtml(profile.collegeCountry || '')}</div>` : ''}
                </div>
            </div>

            <!-- Abroad Journey -->
            <div class="journey-card">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main);">Abroad Journey Flow</h3>
                <div class="journey-flow">
                    <div class="journey-step">
                        <div style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Origin / Hometown</div>
                        <div style="font-size: 0.95rem; font-weight: 700; margin-top: 0.25rem;">📍 ${escapeHtml(profile.hometown || 'India')}</div>
                    </div>
                    <div class="journey-arrow">➔</div>
                    <div class="journey-step">
                        <div style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">
                            ${isAbroad ? 'Current Living Abroad' : 'Target Destination'}
                        </div>
                        <div style="font-size: 0.95rem; font-weight: 700; margin-top: 0.25rem;">
                            ${isAbroad
                                ? `🌐 ${escapeHtml(profile.currentCity || '')} ${escapeHtml(profile.currentCountry || 'Abroad')}`
                                : `🎯 ${escapeHtml(profile.targetCity || '')} ${escapeHtml(profile.targetCountry || 'Abroad')}`
                            }
                        </div>
                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.15rem;">
                            ${isAbroad
                                ? (profile.movedYear ? `Living abroad since ${profile.movedYear}` : 'Living abroad')
                                : (profile.expectedMoveDate ? `Expected move: ${profile.expectedMoveDate}` : 'Planning to move')
                            }
                            ${!isAbroad && profile.targetUniversity ? ` • ${escapeHtml(profile.targetUniversity)}` : ''}
                        </div>
                    </div>
                </div>
            </div>

            <!-- Skills Section -->
            <div style="margin: 1.5rem 0;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">Skills & Specialties</h3>
                <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
                    ${skillsList.length > 0
                        ? skillsList.map(skill => `<span class="badge badge-abroad" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">${escapeHtml(skill)}</span>`).join('')
                        : '<span style="color: var(--text-muted); font-size: 0.85rem;">No skills listed yet.</span>'
                    }
                </div>
            </div>

            <!-- Profile Completion Progress -->
            ${profile.profileCompletion !== undefined ? `
                <div style="border-top: 1px solid var(--border-color); padding-top: 1.25rem; margin-top: 1.5rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                        <span style="font-size: 0.85rem; font-weight: 600; color: var(--text-muted);">Profile Completion Score</span>
                        <span style="font-size: 0.85rem; font-weight: 700; color: var(--primary);">${profile.profileCompletion}%</span>
                    </div>
                    <div class="progress-track" style="margin-bottom: 0;">
                        <div class="progress-fill" style="width: ${profile.profileCompletion}%;"></div>
                    </div>
                </div>
            ` : ''}

        </div>
    `;
}

/**
 * Render Public Social Profile View (For Other Users)
 */
function renderPublicUserProfilePage(publicProfile) {
    const profileContainer = document.getElementById('userProfileContainer');
    if (!profileContainer) return;

    const isAbroad = publicProfile.userType === 'ABROAD';
    const statusText = isAbroad
        ? `🟢 Living in ${escapeHtml(publicProfile.currentCity || publicProfile.currentCountry || 'Abroad')}`
        : `✈️ Planning to move to ${escapeHtml(publicProfile.targetCity || publicProfile.targetCountry || 'Abroad')}`;
    const statusClass = isAbroad ? 'badge-abroad' : 'badge-aspiring';

    const skillsList = publicProfile.skills
        ? publicProfile.skills.split(',').map(s => s.trim()).filter(Boolean)
        : [];

    const matchReasons = publicProfile.matchReasons || [];

    profileContainer.innerHTML = `
        <div class="profile-cover"></div>
        <div class="profile-body-card">
            <div class="profile-avatar-row">
                <div class="avatar-circle profile-main-avatar" style="background-color: var(--primary);">
                    ${escapeHtml(publicProfile.profilePhoto || getInitials(publicProfile.name))}
                </div>
                <div>
                    ${renderConnectionActionButton(publicProfile)}
                </div>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 style="font-size: 1.5rem; font-weight: 700; color: var(--text-main);">${escapeHtml(publicProfile.name)}</h2>
                    <p style="color: var(--text-muted); font-size: 0.95rem; font-weight: 500;">
                        ${escapeHtml(publicProfile.profession || 'Community Member')} ${publicProfile.experienceYears ? '• ' + publicProfile.experienceYears + ' yrs exp' : ''}
                    </p>
                </div>
                <span class="badge ${statusClass}" style="font-size: 0.85rem; padding: 0.4rem 0.85rem;">
                    ${statusText}
                </span>
            </div>

            <!-- Match Reasons Box -->
            ${matchReasons.length > 0 ? `
                <div style="background: var(--success-bg); border: 1px solid var(--success-border); border-radius: var(--radius-sm); padding: 0.75rem 1rem; margin: 1rem 0;">
                    <div style="font-size: 0.8rem; font-weight: 700; color: var(--success-text); text-transform: uppercase; margin-bottom: 0.25rem;">
                        💡 Why This Person Matches Your Journey
                    </div>
                    <div class="match-reasons-box" style="margin: 0;">
                        ${matchReasons.map(r => `<span class="match-chip">${escapeHtml(r)}</span>`).join('')}
                    </div>
                </div>
            ` : ''}

            <!-- Meta Details -->
            <div style="display: flex; flex-wrap: wrap; gap: 1.25rem; margin: 1.25rem 0; font-size: 0.9rem; color: var(--text-main);">
                <div>📍 <strong>Location:</strong> ${escapeHtml(publicProfile.currentCity || '')} ${publicProfile.currentCountry ? '(' + escapeHtml(publicProfile.currentCountry) + ')' : ''}</div>
                <div>🎓 <strong>College:</strong> ${escapeHtml(publicProfile.collegeName || 'N/A')}</div>
                <div>🏠 <strong>Hometown:</strong> ${escapeHtml(publicProfile.hometown || 'N/A')}</div>
                <div>👥 <strong>Connections:</strong> ${publicProfile.connectionCount || 0}</div>
            </div>

            <!-- About Section -->
            <div style="margin: 1.5rem 0;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">About</h3>
                <p style="font-size: 0.95rem; line-height: 1.6; color: var(--text-main); background: #f8fafc; padding: 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
                    ${publicProfile.bio ? escapeHtml(publicProfile.bio) : '<em>No bio information provided.</em>'}
                </p>
            </div>

            <!-- Education Section -->
            <div style="margin: 1.5rem 0;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">Education</h3>
                <div style="background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 1rem;">
                    <div style="font-weight: 700; font-size: 0.95rem; color: var(--text-main);">
                        🎓 ${escapeHtml(publicProfile.collegeName || 'College Not Specified')}
                    </div>
                    <div style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0.25rem;">
                        ${escapeHtml(publicProfile.degree || 'Degree Program')} ${publicProfile.graduationYear ? '• Graduated Class of ' + publicProfile.graduationYear : ''}
                    </div>
                    ${publicProfile.collegeCity ? `<div style="font-size: 0.8rem; color: var(--text-light); margin-top: 0.15rem;">📍 ${escapeHtml(publicProfile.collegeCity)}, ${escapeHtml(publicProfile.collegeCountry || '')}</div>` : ''}
                </div>
            </div>

            <!-- Abroad Journey -->
            <div class="journey-card">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main);">Abroad Journey Flow</h3>
                <div class="journey-flow">
                    <div class="journey-step">
                        <div style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Origin / Hometown</div>
                        <div style="font-size: 0.95rem; font-weight: 700; margin-top: 0.25rem;">📍 ${escapeHtml(publicProfile.hometown || 'India')}</div>
                    </div>
                    <div class="journey-arrow">➔</div>
                    <div class="journey-step">
                        <div style="font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">
                            ${isAbroad ? 'Current Living Abroad' : 'Target Destination'}
                        </div>
                        <div style="font-size: 0.95rem; font-weight: 700; margin-top: 0.25rem;">
                            ${isAbroad
                                ? `🌐 ${escapeHtml(publicProfile.currentCity || '')} ${escapeHtml(publicProfile.currentCountry || 'Abroad')}`
                                : `🎯 ${escapeHtml(publicProfile.targetCity || '')} ${escapeHtml(publicProfile.targetCountry || 'Abroad')}`
                            }
                        </div>
                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.15rem;">
                            ${isAbroad
                                ? (publicProfile.movedYear ? `Living abroad since ${publicProfile.movedYear}` : 'Living abroad')
                                : (publicProfile.expectedMoveDate ? `Expected move: ${publicProfile.expectedMoveDate}` : 'Planning to move')
                            }
                            ${!isAbroad && publicProfile.targetUniversity ? ` • ${escapeHtml(publicProfile.targetUniversity)}` : ''}
                        </div>
                    </div>
                </div>
            </div>

            <!-- Skills Section -->
            <div style="margin: 1.5rem 0;">
                <h3 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">Skills & Specialties</h3>
                <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
                    ${skillsList.length > 0
                        ? skillsList.map(skill => `<span class="badge badge-abroad" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">${escapeHtml(skill)}</span>`).join('')
                        : '<span style="color: var(--text-muted); font-size: 0.85rem;">No skills listed.</span>'
                    }
                </div>
            </div>
        </div>
    `;

    switchView('profile');
}

/**
 * Edit Profile Modal Logic
 */
function openEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    if (!modal) return;

    const userType = (currentProfile && currentProfile.userType) || (currentUser && currentUser.userType) || 'ASPIRING';

    document.getElementById('profName').value = currentProfile ? (currentProfile.userName || '') : (currentUser ? currentUser.name : '');
    document.getElementById('profPhoto').value = currentProfile ? (currentProfile.profilePhoto || '') : '';
    document.getElementById('profBio').value = currentProfile ? (currentProfile.bio || '') : '';
    document.getElementById('profCollegeName').value = currentProfile ? (currentProfile.collegeName || '') : '';
    document.getElementById('profDegree').value = currentProfile ? (currentProfile.degree || '') : '';
    document.getElementById('profGraduationYear').value = currentProfile ? (currentProfile.graduationYear || '') : '';
    document.getElementById('profHometown').value = currentProfile ? (currentProfile.hometown || '') : '';
    document.getElementById('profCurrentCountry').value = currentProfile ? (currentProfile.currentCountry || 'India') : 'India';
    document.getElementById('profCurrentCity').value = currentProfile ? (currentProfile.currentCity || '') : '';
    document.getElementById('profTargetCountry').value = currentProfile ? (currentProfile.targetCountry || 'Canada') : 'Canada';
    document.getElementById('profTargetCity').value = currentProfile ? (currentProfile.targetCity || '') : '';
    document.getElementById('profTargetUniversity').value = currentProfile ? (currentProfile.targetUniversity || '') : '';
    document.getElementById('profExpectedMoveDate').value = currentProfile ? (currentProfile.expectedMoveDate || '') : '';
    document.getElementById('profMovedYear').value = currentProfile ? (currentProfile.movedYear || '') : '';
    document.getElementById('profProfession').value = currentProfile ? (currentProfile.profession || '') : '';
    document.getElementById('profExperienceYears').value = currentProfile ? (currentProfile.experienceYears !== null && currentProfile.experienceYears !== undefined ? currentProfile.experienceYears : '') : '';
    document.getElementById('profSkills').value = currentProfile ? (currentProfile.skills || '') : '';

    const movedGroup = document.getElementById('groupMovedYear');
    const targetCountryGroup = document.getElementById('groupTargetCountry');
    const targetCityGroup = document.getElementById('groupTargetCity');
    const targetUnivGroup = document.getElementById('groupTargetUniv');
    const expectedMoveGroup = document.getElementById('groupExpectedMoveDate');

    if (userType === 'ABROAD') {
        if (movedGroup) movedGroup.style.display = 'block';
        if (targetCountryGroup) targetCountryGroup.style.display = 'none';
        if (targetCityGroup) targetCityGroup.style.display = 'none';
        if (targetUnivGroup) targetUnivGroup.style.display = 'none';
        if (expectedMoveGroup) expectedMoveGroup.style.display = 'none';
    } else {
        if (movedGroup) movedGroup.style.display = 'none';
        if (targetCountryGroup) targetCountryGroup.style.display = 'block';
        if (targetCityGroup) targetCityGroup.style.display = 'block';
        if (targetUnivGroup) targetUnivGroup.style.display = 'block';
        if (expectedMoveGroup) expectedMoveGroup.style.display = 'block';
    }

    modal.classList.add('active');
}

function closeEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    if (modal) modal.classList.remove('active');
}

async function handleSaveProfile(event) {
    event.preventDefault();
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    const payload = {
        name: document.getElementById('profName').value.trim(),
        profilePhoto: document.getElementById('profPhoto').value.trim(),
        bio: document.getElementById('profBio').value.trim(),
        collegeName: document.getElementById('profCollegeName').value.trim(),
        degree: document.getElementById('profDegree').value.trim(),
        graduationYear: document.getElementById('profGraduationYear').value ? parseInt(document.getElementById('profGraduationYear').value) : null,
        hometown: document.getElementById('profHometown').value.trim(),
        currentCountry: document.getElementById('profCurrentCountry').value,
        currentCity: document.getElementById('profCurrentCity').value.trim(),
        targetCountry: document.getElementById('profTargetCountry').value,
        targetCity: document.getElementById('profTargetCity').value.trim(),
        targetUniversity: document.getElementById('profTargetUniversity').value.trim(),
        expectedMoveDate: document.getElementById('profExpectedMoveDate').value || null,
        movedYear: document.getElementById('profMovedYear').value ? parseInt(document.getElementById('profMovedYear').value) : null,
        profession: document.getElementById('profProfession').value.trim(),
        experienceYears: document.getElementById('profExperienceYears').value ? parseInt(document.getElementById('profExperienceYears').value) : null,
        skills: document.getElementById('profSkills').value.trim()
    };

    const isUpdate = currentProfile !== null;
    const url = isUpdate ? '/api/profiles/me' : '/api/profiles';
    const method = isUpdate ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            currentProfile = await response.json();
            if (payload.name && currentUser) {
                currentUser.name = payload.name;
                localStorage.setItem('user', JSON.stringify(currentUser));
            }
            closeEditProfileModal();
            updateUserUI(currentUser, currentProfile);
            renderProfileCompletionWidget(currentProfile);
            renderUserProfile(currentProfile);
            fetchPeopleDirectory(0);
            fetchSameCollegeSection();
            fetchDestinationSection();
        } else {
            const errData = await response.json();
            alert("Error saving profile: " + (errData.message || "Invalid input data"));
        }
    } catch (err) {
        console.error("Save profile error", err);
        alert("Failed to save profile. Please check backend API server.");
    }
}

/**
 * Initialize Router Navigation
 */
function initAppNavigation() {
    const navItems = document.querySelectorAll('.nav-item[data-view]');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            navItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const viewName = item.getAttribute('data-view');
            switchView(viewName);
        });
    });

    const trigger = document.getElementById('userProfileTrigger');
    const dropdown = document.getElementById('userDropdown');
    if (trigger && dropdown) {
        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });

        document.addEventListener('click', () => {
            dropdown.classList.remove('show');
        });
    }

    const logoutBtn = document.getElementById('logoutAction');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('user');
            window.location.href = '/login.html';
        });
    }

    const devPageAction = document.getElementById('devPageAction');
    if (devPageAction) {
        devPageAction.addEventListener('click', () => {
            window.location.href = '/dev.html';
        });
    }

    const filterTabs = document.querySelectorAll('.tab-pill[data-category]');
    filterTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            filterTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const category = tab.getAttribute('data-category');
            renderFeed(category);
        });
    });

    const searchInput = document.getElementById('globalSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            if (query.length > 0) {
                switchView('people');
                const searchParam = document.getElementById('searchKeywordInput');
                if (searchParam) {
                    searchParam.value = query;
                    handleFilterChange();
                }
            }
        });
    }
}

/**
 * View Router Switcher
 */
function switchView(viewName) {
    currentView = viewName;
    const views = document.querySelectorAll('.page-view');
    views.forEach(v => v.classList.remove('active'));

    const targetView = document.getElementById(`view-${viewName}`);
    if (targetView) {
        targetView.classList.add('active');
    }

    if (viewName === 'profile') {
        renderUserProfile(currentProfile);
    } else if (viewName === 'people') {
        fetchPeopleDirectory(peopleCurrentPage);
        fetchSameCollegeSection();
        fetchDestinationSection();
    } else if (viewName === 'connections') {
        switchConnectionsTab('received');
    }
}

/**
 * Phase 3: Fetch Real People Directory from GET /api/profiles
 */
async function fetchPeopleDirectory(page = 0) {
    const container = document.getElementById('peopleDirectoryContainer');
    if (!container) return;

    peopleCurrentPage = page;
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    renderPeopleLoadingState();

    const params = new URLSearchParams();
    params.append('page', peopleCurrentPage);
    params.append('size', peoplePageSize);

    const keyword = document.getElementById('searchKeywordInput')?.value.trim();
    const college = document.getElementById('filterCollegeInput')?.value.trim();
    const country = document.getElementById('filterCountrySelect')?.value;
    const city = document.getElementById('filterCityInput')?.value.trim();
    const profession = document.getElementById('filterProfessionInput')?.value.trim();
    const userType = document.getElementById('filterUserTypeSelect')?.value;

    if (keyword) params.append('keyword', keyword);
    if (college) params.append('college', college);
    if (country) params.append('currentCountry', country);
    if (city) params.append('currentCity', city);
    if (profession) params.append('profession', profession);
    if (userType) params.append('userType', userType);

    try {
        const response = await fetch(`/api/profiles?${params.toString()}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const pageData = await response.json();
            peopleTotalPages = pageData.totalPages;
            if (pageData.content.length === 0) {
                renderPeopleEmptyState();
            } else {
                container.innerHTML = pageData.content.map(p => renderPeopleCard(p)).join('');
            }
            renderPeoplePagination(pageData);
        } else {
            renderPeopleErrorState();
        }
    } catch (err) {
        console.error("Fetch people error", err);
        renderPeopleErrorState();
    }
}

/**
 * Render Profile Card for Discovery Grid
 */
function renderPeopleCard(profile) {
    const isAbroad = profile.userType === 'ABROAD';
    const statusText = isAbroad ? '🟢 Already Abroad' : `✈️ Planning Move (${escapeHtml(profile.targetCountry || 'Abroad')})`;
    const statusClass = isAbroad ? 'badge-abroad' : 'badge-aspiring';

    const skillsList = profile.skills
        ? profile.skills.split(',').map(s => s.trim()).filter(Boolean)
        : [];

    const matchReasons = profile.matchReasons || [];

    return `
        <div class="user-card" style="text-align: left; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
                    <div class="avatar-circle card-avatar" style="background-color: var(--primary);">
                        ${escapeHtml(profile.profilePhoto || getInitials(profile.name))}
                    </div>
                    <span class="badge ${statusClass}">${statusText}</span>
                </div>

                <div class="card-name">${escapeHtml(profile.name)}</div>
                <div class="card-title">${escapeHtml(profile.profession || 'Community Member')} ${profile.experienceYears ? '• ' + profile.experienceYears + ' yrs exp' : ''}</div>
                <div class="card-location" style="margin-top: 0.25rem;">
                    📍 ${escapeHtml(profile.currentCity || '')} ${profile.currentCountry ? '(' + escapeHtml(profile.currentCountry) + ')' : ''}
                </div>
                <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.15rem;">
                    🎓 ${escapeHtml(profile.collegeName || 'N/A')} ${profile.degree ? '• ' + escapeHtml(profile.degree) : ''}
                </div>

                <!-- Match Reasons Chips -->
                ${matchReasons.length > 0 ? `
                    <div class="match-reasons-box">
                        ${matchReasons.map(r => `<span class="match-chip">${escapeHtml(r)}</span>`).join('')}
                    </div>
                ` : ''}

                <!-- Bio Snippet -->
                <p style="font-size: 0.85rem; color: var(--text-main); margin: 0.65rem 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                    "${escapeHtml(profile.bio || 'Connected member of ConnectAbroad community.')}"
                </p>

                <!-- Skills -->
                ${skillsList.length > 0 ? `
                    <div style="display: flex; flex-wrap: wrap; gap: 0.25rem; margin-bottom: 0.75rem;">
                        ${skillsList.slice(0, 3).map(sk => `<span style="font-size: 0.75rem; background: var(--bg-subtle); padding: 0.15rem 0.45rem; border-radius: var(--radius-sm); color: var(--text-muted);">${escapeHtml(sk)}</span>`).join('')}
                    </div>
                ` : ''}
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

/**
 * Curated Section 1: People From Your College
 */
async function fetchSameCollegeSection() {
    const container = document.getElementById('sameCollegeGrid');
    const wrapper = document.getElementById('sameCollegeSectionContainer');
    if (!container || !wrapper) return;

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch('/api/profiles/sections/college?size=3', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const pageData = await response.json();
            if (pageData.content && pageData.content.length > 0) {
                wrapper.style.display = 'block';
                container.innerHTML = pageData.content.map(p => renderPeopleCard(p)).join('');
            } else {
                wrapper.style.display = 'none';
            }
        } else {
            wrapper.style.display = 'none';
        }
    } catch (err) {
        wrapper.style.display = 'none';
    }
}

/**
 * Curated Section 2: Destination & Journey Network
 */
async function fetchDestinationSection() {
    const container = document.getElementById('destinationGrid');
    const wrapper = document.getElementById('destinationSectionContainer');
    const titleElem = document.getElementById('destinationSectionTitle');
    if (!container || !wrapper) return;

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    const isAspiring = currentUser && currentUser.userType === 'ASPIRING';
    const endpoint = isAspiring ? '/api/profiles/sections/destination?size=3' : '/api/profiles/sections/near-you?size=3';

    if (titleElem) {
        titleElem.innerText = isAspiring ? '✈️ People Already In Your Destination' : '📍 People Near You';
    }

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const pageData = await response.json();
            if (pageData.content && pageData.content.length > 0) {
                wrapper.style.display = 'block';
                container.innerHTML = pageData.content.map(p => renderPeopleCard(p)).join('');
            } else {
                wrapper.style.display = 'none';
            }
        } else {
            wrapper.style.display = 'none';
        }
    } catch (err) {
        wrapper.style.display = 'none';
    }
}

/**
 * Pagination Controls Rendering
 */
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

function renderPeopleLoadingState() {
    const container = document.getElementById('peopleDirectoryContainer');
    if (!container) return;

    container.innerHTML = Array(6).fill(0).map(() => `
        <div class="skeleton-card">
            <div style="display: flex; gap: 0.75rem; margin-bottom: 1rem;">
                <div style="width: 50px; height: 50px; border-radius: 50%; background: #e2e8f0;"></div>
                <div style="flex: 1;">
                    <div style="height: 16px; width: 60%; background: #e2e8f0; border-radius: 4px; margin-bottom: 6px;"></div>
                    <div style="height: 12px; width: 40%; background: #e2e8f0; border-radius: 4px;"></div>
                </div>
            </div>
            <div style="height: 12px; width: 90%; background: #e2e8f0; border-radius: 4px; margin-bottom: 6px;"></div>
            <div style="height: 12px; width: 75%; background: #e2e8f0; border-radius: 4px;"></div>
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
                Try changing your college, city, country, or profession filters to discover other community members.
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
            <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">Unable to load people right now</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.25rem;">
                Please check your network or backend API server status.
            </p>
            <button class="btn-primary" onclick="fetchPeopleDirectory(0)">↺ Retry</button>
        </div>
    `;
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

/**
 * Fetch Public Profile by User ID via GET /api/profiles/{id}
 */
async function viewUserProfileByPublicId(userId) {
    if (currentUser && userId === currentUser.id) {
        switchView('profile');
        return;
    }

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`/api/profiles/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const publicProfile = await response.json();
            renderPublicUserProfilePage(publicProfile);
        } else {
            alert("Could not load user profile.");
        }
    } catch (err) {
        console.error("View public profile error", err);
    }
}

/**
 * ==========================================================================
 * Phase 4 Connection System Frontend Engine
 * ==========================================================================
 */

/**
 * Toast Notifications
 */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) {
        console.log(`[Toast ${type}]: ${message}`);
        return;
    }

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

/**
 * Notification Badge Counter (GET /api/connections/requests/count)
 */
async function updateNotificationBadge() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch('/api/connections/requests/count', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        if (response.ok) {
            const data = await response.json();
            const count = data.count || 0;
            const badge = document.getElementById('navNotificationBadge');
            if (badge) {
                if (count > 0) {
                    badge.innerText = count;
                    badge.style.display = 'inline-flex';
                } else {
                    badge.innerText = '';
                    badge.style.display = 'none';
                }
            }

            const statReceived = document.getElementById('statPendingReceived');
            if (statReceived) statReceived.innerText = count;
        }
    } catch (err) {
        console.warn("Could not fetch notification count", err);
    }
}

/**
 * Universal Connection Action Button Renderer
 */
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
                <span class="btn-connected">✓ Connected</span>
                <button class="btn-remove" title="Remove Connection" onclick="handleRemoveConnection(event, ${connId}, ${profile.userId})">Remove</button>
            </div>
        `;
    } else if (status === 'PENDING_SENT') {
        return `
            <div style="display: flex; align-items: center; gap: 6px;">
                <span class="btn-pending">⏳ Sent</span>
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
    } else { // NONE or REJECTED
        return `
            <button class="btn-connect" onclick="handleSendConnectionRequest(event, ${profile.userId})">
                + Connect
            </button>
        `;
    }
}

/**
 * Send Connection Request (POST /api/connections/request/{userId})
 */
async function handleSendConnectionRequest(event, targetUserId) {
    if (event) event.stopPropagation();

    const btn = event ? event.currentTarget : null;
    if (btn) btn.disabled = true;

    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch(`/api/connections/request/${targetUserId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection request sent.", 'success');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to send connection request.", 'error');
            if (btn) btn.disabled = false;
        }
    } catch (err) {
        console.error("Send request error", err);
        showToast("Error sending connection request.", 'error');
        if (btn) btn.disabled = false;
    }
}

/**
 * Accept Connection Request (PUT /api/connections/{connectionId}/accept)
 */
async function handleAcceptConnectionRequest(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`/api/connections/${connectionId}/accept`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection request accepted.", 'success');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to accept connection request.", 'error');
        }
    } catch (err) {
        console.error("Accept request error", err);
        showToast("Error accepting connection request.", 'error');
    }
}

/**
 * Reject Connection Request (PUT /api/connections/{connectionId}/reject)
 */
async function handleRejectConnectionRequest(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`/api/connections/${connectionId}/reject`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection request rejected.", 'info');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to reject request.", 'error');
        }
    } catch (err) {
        console.error("Reject request error", err);
        showToast("Error rejecting request.", 'error');
    }
}

/**
 * Cancel Outgoing Connection Request (DELETE /api/connections/{connectionId}/cancel)
 */
async function handleCancelConnectionRequest(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`/api/connections/${connectionId}/cancel`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection request cancelled.", 'info');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to cancel connection request.", 'error');
        }
    } catch (err) {
        console.error("Cancel request error", err);
        showToast("Error cancelling request.", 'error');
    }
}

/**
 * Remove Accepted Connection (DELETE /api/connections/{connectionId})
 */
async function handleRemoveConnection(event, connectionId, targetUserId) {
    if (event) event.stopPropagation();

    if (!confirm("Are you sure you want to remove this connection?")) return;

    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`/api/connections/${connectionId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        if (response.ok) {
            showToast(data.message || "Connection removed.", 'info');
            refreshConnectionUI(targetUserId);
        } else {
            showToast(data.message || "Unable to remove connection.", 'error');
        }
    } catch (err) {
        console.error("Remove connection error", err);
        showToast("Error removing connection.", 'error');
    }
}

/**
 * Refresh UI after connection state change
 */
function refreshConnectionUI(targetUserId) {
    updateNotificationBadge();

    if (currentView === 'people') {
        fetchPeopleDirectory(peopleCurrentPage);
        fetchSameCollegeSection();
        fetchDestinationSection();
    } else if (currentView === 'connections' || document.getElementById('view-connections-page')) {
        loadConnectionsTabData(activeConnectionsTab);
    } else if (targetUserId) {
        viewUserProfileByPublicId(targetUserId);
    }
}

/**
 * Connections Page Tab Switcher & Data Fetching
 */
let activeConnectionsTab = 'received';

function switchConnectionsTab(tabName) {
    activeConnectionsTab = tabName;

    // Update tab button styles
    ['received', 'sent', 'connected'].forEach(t => {
        const standaloneBtn = document.getElementById(`btnTab${t.charAt(0).toUpperCase() + t.slice(1)}`);
        const dashBtn = document.getElementById(`btnDashTab${t.charAt(0).toUpperCase() + t.slice(1)}`);
        
        if (standaloneBtn) standaloneBtn.classList.toggle('active', t === tabName);
        if (dashBtn) dashBtn.classList.toggle('active', t === tabName);
    });

    loadConnectionsTabData(tabName);
}

async function loadConnectionsTabData(tabName) {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    const container = document.getElementById('connectionsContainer') || document.getElementById('dashConnectionsContainer');
    if (!container) return;

    container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);">Loading connections...</div>`;

    if (tabName === 'received') {
        try {
            const res = await fetch('/api/connections/requests/received', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const list = await res.json();
                updateTabBadgeCounts(list.length, null, null);
                renderReceivedRequestsList(container, list);
            }
        } catch (e) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--danger-text);">Error loading requests.</div>`;
        }
    } else if (tabName === 'sent') {
        try {
            const res = await fetch('/api/connections/requests/sent', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const list = await res.json();
                updateTabBadgeCounts(null, list.length, null);
                renderSentRequestsList(container, list);
            }
        } catch (e) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--danger-text);">Error loading sent requests.</div>`;
        }
    } else if (tabName === 'connected') {
        try {
            const res = await fetch('/api/connections', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const list = await res.json();
                updateTabBadgeCounts(null, null, list.length);
                renderMyConnectionsList(container, list);
            }
        } catch (e) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--danger-text);">Error loading connections.</div>`;
        }
    }
}

function updateTabBadgeCounts(receivedCount, sentCount, connectedCount) {
    if (receivedCount !== null) {
        ['tabReceivedCount', 'dashTabReceivedCount'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerText = receivedCount;
        });
        const stat = document.getElementById('statPendingReceived');
        if (stat) stat.innerText = receivedCount;
    }
    if (sentCount !== null) {
        ['tabSentCount', 'dashTabSentCount'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerText = sentCount;
        });
        const stat = document.getElementById('statPendingSent');
        if (stat) stat.innerText = sentCount;
    }
    if (connectedCount !== null) {
        ['tabConnectedCount', 'dashTabConnectedCount'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerText = connectedCount;
        });
        const stat = document.getElementById('statTotalConnected');
        if (stat) stat.innerText = connectedCount;
    }
}

function renderReceivedRequestsList(container, list) {
    if (list.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px dashed var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">📥</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">No incoming connection requests</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-top: 0.25rem;">
                    When other community members send you a connection request, they will appear here.
                </p>
            </div>
        `;
        return;
    }

    container.innerHTML = list.map(req => {
        const u = req.user;
        return `
            <div class="connection-card">
                <div class="connection-card-avatar">
                    ${escapeHtml(u.profilePhoto || getInitials(u.name))}
                </div>
                <div class="connection-card-info">
                    <div class="connection-card-name" onclick="viewUserProfileByPublicId(${u.userId})">${escapeHtml(u.name)}</div>
                    <div class="connection-card-sub">${escapeHtml(u.profession || 'Community Member')} • ${escapeHtml(u.currentCity || u.currentCountry || '')}</div>
                    <div class="connection-card-meta">
                        <span>🎓 ${escapeHtml(u.collegeName || 'N/A')}</span>
                        <span>•</span>
                        <span>Received ${new Date(req.createdAt).toLocaleDateString()}</span>
                    </div>
                </div>
                <div class="connection-card-actions">
                    <button class="btn-accept" onclick="handleAcceptConnectionRequest(event, ${req.connectionId}, ${u.userId})">✓ Accept</button>
                    <button class="btn-reject" onclick="handleRejectConnectionRequest(event, ${req.connectionId}, ${u.userId})">✗ Reject</button>
                </div>
            </div>
        `;
    }).join('');
}

function renderSentRequestsList(container, list) {
    if (list.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px dashed var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">📤</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">No sent connection requests</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-top: 0.25rem;">
                    Requests you send to fellow students and alumni will appear here.
                </p>
            </div>
        `;
        return;
    }

    container.innerHTML = list.map(req => {
        const u = req.user;
        return `
            <div class="connection-card">
                <div class="connection-card-avatar">
                    ${escapeHtml(u.profilePhoto || getInitials(u.name))}
                </div>
                <div class="connection-card-info">
                    <div class="connection-card-name" onclick="viewUserProfileByPublicId(${u.userId})">${escapeHtml(u.name)}</div>
                    <div class="connection-card-sub">${escapeHtml(u.profession || 'Community Member')} • ${escapeHtml(u.currentCity || u.currentCountry || '')}</div>
                    <div class="connection-card-meta">
                        <span>🎓 ${escapeHtml(u.collegeName || 'N/A')}</span>
                        <span>•</span>
                        <span>Sent ${new Date(req.createdAt).toLocaleDateString()}</span>
                    </div>
                </div>
                <div class="connection-card-actions">
                    <span class="btn-pending">⏳ Pending</span>
                    <button class="btn-cancel" onclick="handleCancelConnectionRequest(event, ${req.connectionId}, ${u.userId})">Cancel Request</button>
                </div>
            </div>
        `;
    }).join('');
}

function renderMyConnectionsList(container, list) {
    if (list.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem 1.5rem; background: var(--bg-surface); border: 1px dashed var(--border-color); border-radius: var(--radius-md);">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">👥</div>
                <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">No active connections yet</h3>
                <p style="font-size: 0.88rem; color: var(--text-muted); margin-top: 0.25rem;">
                    Discover people in your target city or college and click <strong>Connect</strong> to build your network!
                </p>
                <button class="btn-primary" style="margin-top: 1rem;" onclick="switchView('people')">🔍 Discover People</button>
            </div>
        `;
        return;
    }

    container.innerHTML = list.map(conn => {
        const u = conn.user;
        return `
            <div class="connection-card">
                <div class="connection-card-avatar">
                    ${escapeHtml(u.profilePhoto || getInitials(u.name))}
                </div>
                <div class="connection-card-info">
                    <div class="connection-card-name" onclick="viewUserProfileByPublicId(${u.userId})">${escapeHtml(u.name)}</div>
                    <div class="connection-card-sub">${escapeHtml(u.profession || 'Community Member')} • ${escapeHtml(u.currentCity || u.currentCountry || '')}</div>
                    <div class="connection-card-meta">
                        <span>🎓 ${escapeHtml(u.collegeName || 'N/A')}</span>
                        <span>•</span>
                        <span>Connected since ${new Date(conn.connectedAt).toLocaleDateString()}</span>
                    </div>
                </div>
                <div class="connection-card-actions">
                    <button class="btn-secondary" style="font-size: 0.85rem; padding: 0.4rem 0.85rem;" onclick="viewUserProfileByPublicId(${u.userId})">View Profile</button>
                    <button class="btn-remove" onclick="handleRemoveConnection(event, ${conn.connectionId}, ${u.userId})">Remove</button>
                </div>
            </div>
        `;
    }).join('');
}

function initConnectionsPage() {
    updateNotificationBadge();
    switchConnectionsTab('received');
}

/**
 * Render Feed Posts
 */
function renderFeed(category = 'all') {
    const feedContainer = document.getElementById('feedPostsContainer');
    if (!feedContainer) return;

    let postsToRender = mockPosts;
    if (category !== 'all') {
        postsToRender = mockPosts.filter(p => p.category === category);
    }

    if (postsToRender.length === 0) {
        feedContainer.innerHTML = `
            <div class="post-card" style="text-align: center; color: var(--text-muted); padding: 2rem;">
                No posts found in this category. Be the first to share an update!
            </div>
        `;
        return;
    }

    feedContainer.innerHTML = postsToRender.map(post => `
        <div class="post-card" id="post-${post.id}">
            <div class="post-header">
                <div class="post-author-info">
                    <div class="avatar-circle" style="background-color: ${post.avatarBg}">
                        ${post.authorAvatar}
                    </div>
                    <div class="author-details">
                        <div class="author-name">${escapeHtml(post.authorName)}</div>
                        <div class="author-title">${escapeHtml(post.authorTitle)}</div>
                        <div class="post-meta">
                            <span class="post-time">${post.timeAgo}</span>
                            <span class="post-location">📍 ${escapeHtml(post.location)}</span>
                        </div>
                    </div>
                </div>
                <span class="badge ${post.badgeClass}">${post.badgeText}</span>
            </div>

            <div class="post-content">
                ${escapeHtml(post.content)}
            </div>

            <span class="post-tag"># ${post.tag}</span>

            <div class="post-footer">
                <button class="action-btn ${post.isLiked ? 'liked' : ''}" onclick="toggleLike(${post.id})">
                    ${post.isLiked ? '❤️' : '🤍'} <span>${post.likesCount}</span>
                </button>
                <button class="action-btn" onclick="openPostComments(${post.id})">
                    💬 <span>${post.commentsCount} Comments</span>
                </button>
                <button class="action-btn" onclick="sharePost(${post.id})">
                    ↗ Share
                </button>
            </div>
        </div>
    `).join('');
}

function toggleLike(postId) {
    const post = mockPosts.find(p => p.id === postId);
    if (post) {
        post.isLiked = !post.isLiked;
        post.likesCount += post.isLiked ? 1 : -1;
        renderFeed(document.querySelector('.tab-pill.active')?.getAttribute('data-category') || 'all');
    }
}

function initComposer() {
    const submitPostBtn = document.getElementById('submitPostBtn');
    const composerTextarea = document.getElementById('composerTextarea');

    if (submitPostBtn && composerTextarea) {
        submitPostBtn.addEventListener('click', () => {
            const text = composerTextarea.value.trim();
            if (!text) return;

            const newPost = {
                id: Date.now(),
                authorId: currentUser ? currentUser.id : 999,
                authorName: currentProfile ? currentProfile.userName : (currentUser ? currentUser.name : "Aditya Bandi"),
                authorTitle: currentProfile && currentProfile.profession ? currentProfile.profession : "Community Member",
                authorAvatar: currentProfile ? getInitials(currentProfile.userName) : getInitials(currentUser ? currentUser.name : "Aditya"),
                avatarBg: "#2563eb",
                badgeText: currentUser && currentUser.userType === 'ABROAD' ? "Living Abroad" : "Planning Move",
                badgeClass: currentUser && currentUser.userType === 'ABROAD' ? "badge-abroad" : "badge-aspiring",
                location: currentProfile && currentProfile.currentCity ? `${currentProfile.currentCity}, ${currentProfile.currentCountry}` : "Global Community",
                timeAgo: "Just now",
                category: "experience",
                content: text,
                likesCount: 1,
                commentsCount: 0,
                isLiked: true,
                tag: "Community Update"
            };

            mockPosts.unshift(newPost);
            composerTextarea.value = '';
            renderFeed(document.querySelector('.tab-pill.active')?.getAttribute('data-category') || 'all');
        });
    }
}

function renderRightSidebar() {
    const list = document.getElementById('peopleSuggestionsList');
    if (!list) return;

    const suggestions = mockUsers.slice(0, 3);
    list.innerHTML = suggestions.map(u => `
        <div class="suggestion-item">
            <div class="avatar-circle" style="background-color: ${u.avatarBg}; width: 36px; height: 36px; font-size: 0.85rem;">
                ${u.avatarInitials}
            </div>
            <div class="suggestion-info">
                <div class="suggestion-name">${escapeHtml(u.name)}</div>
                <div class="suggestion-meta">${escapeHtml(u.city || u.location)}</div>
            </div>
            <button class="action-btn" style="color: var(--primary); font-weight: 600;" onclick="handleConnectClick(event, ${u.id})">+ Add</button>
        </div>
    `).join('');

    const commList = document.getElementById('communitiesWidgetList');
    if (commList) {
        commList.innerHTML = mockCommunities.slice(0, 3).map(c => `
            <div class="suggestion-item">
                <div style="font-size: 1.2rem;">${c.flag || '🌐'}</div>
                <div class="suggestion-info">
                    <div class="suggestion-name">${escapeHtml(c.name)}</div>
                    <div class="suggestion-meta">${c.membersCount} members</div>
                </div>
            </div>
        `).join('');
    }
}

function renderExploreView() {
    const container = document.getElementById('exploreCountriesContainer');
    if (!container) return;

    container.innerHTML = mockCommunities.map(comm => `
        <div class="user-card" style="text-align: left;">
            <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">${comm.flag || '🌐'}</div>
            <div class="card-name" style="font-size: 1.1rem;">${escapeHtml(comm.name)}</div>
            <div class="card-title" style="margin-bottom: 0.5rem;">${comm.membersCount} active members</div>
            <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1rem;">${escapeHtml(comm.description)}</p>
            <button class="btn-secondary" style="width: 100%;" onclick="alert('Joined ${comm.name}')">Join Community</button>
        </div>
    `).join('');
}

function renderJobsView() {
    const container = document.getElementById('jobsListContainer');
    if (!container) return;

    container.innerHTML = mockJobs.map(job => `
        <div class="card-item">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--text-main);">${escapeHtml(job.title)}</h3>
                    <div style="color: var(--primary); font-weight: 600; font-size: 0.85rem; margin-top: 0.15rem;">🏢 ${escapeHtml(job.company)} • 📍 ${escapeHtml(job.location)}</div>
                </div>
                <span class="badge badge-abroad">${job.payRate}</span>
            </div>
            <p style="font-size: 0.9rem; color: var(--text-main); margin: 0.75rem 0;">${escapeHtml(job.description)}</p>
            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 0.75rem; font-size: 0.8rem; color: var(--text-muted);">
                <span>Posted by ${escapeHtml(job.postedBy)} • ${job.timeAgo}</span>
                <button class="btn-primary" style="padding: 0.35rem 0.85rem; font-size: 0.8rem;" onclick="alert('Contact details: ${job.contact}')">Message Poster</button>
            </div>
        </div>
    `).join('');
}

function renderHousingView() {
    const container = document.getElementById('housingListContainer');
    if (!container) return;

    container.innerHTML = mockHousing.map(h => `
        <div class="card-item">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div>
                    <h3 style="font-size: 1.05rem; font-weight: 700; color: var(--text-main);">${escapeHtml(h.title)}</h3>
                    <div style="color: var(--primary); font-weight: 600; font-size: 0.85rem; margin-top: 0.15rem;">📍 ${escapeHtml(h.location)} • Move-in: ${h.availableFrom}</div>
                </div>
                <span class="badge badge-abroad">${h.rent}</span>
            </div>
            <p style="font-size: 0.9rem; color: var(--text-main); margin: 0.75rem 0;">${escapeHtml(h.description)}</p>
            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 0.75rem; font-size: 0.8rem; color: var(--text-muted);">
                <span>Posted by ${escapeHtml(h.postedBy)} • ${h.timeAgo}</span>
                <button class="btn-primary" style="padding: 0.35rem 0.85rem; font-size: 0.8rem;" onclick="alert('Contact details: ${h.contact}')">Inquire Housing</button>
            </div>
        </div>
    `).join('');
}

function renderMessagesView() {
    const convList = document.getElementById('conversationsList');
    if (!convList) return;

    convList.innerHTML = mockConversations.map(c => `
        <div class="conv-item ${c.id === activeConversationId ? 'active' : ''}" onclick="selectConversation(${c.id})">
            <div class="avatar-circle" style="background-color: ${c.avatarBg}; width: 38px; height: 38px; font-size: 0.85rem;">
                ${c.avatarInitials}
            </div>
            <div class="conv-info">
                <div class="conv-name">${escapeHtml(c.userName)}</div>
                <div class="conv-last">${escapeHtml(c.lastMessage)}</div>
            </div>
            <div class="conv-time">${c.timeAgo}</div>
        </div>
    `).join('');

    renderActiveChat();
}

function selectConversation(id) {
    activeConversationId = id;
    renderMessagesView();
}

function renderActiveChat() {
    const chatContainer = document.getElementById('chatMessagesContainer');
    const headerElem = document.getElementById('chatHeaderName');
    if (!chatContainer) return;

    const activeMsg = mockConversations.find(c => c.id === activeConversationId);
    if (!activeMsg) return;

    if (headerElem) headerElem.innerText = activeMsg.userName;

    chatContainer.innerHTML = activeMsg.conversation.map(c => `
        <div class="message-bubble ${c.sender === 'You' ? 'outgoing' : 'incoming'}">
            <div style="font-size: 0.75rem; font-weight: 600; margin-bottom: 0.15rem; opacity: 0.8;">${c.sender} • ${c.time}</div>
            <div>${escapeHtml(c.text)}</div>
        </div>
    `).join('');

    chatContainer.scrollTop = chatContainer.scrollHeight;
}

function getInitials(name) {
    if (!name) return 'CA';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function(m) {
        return {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        }[m];
    });
}
