window.addEventListener('DOMContentLoaded', main);

// --- GLOBAL VARIABLES ---
let currentUser = null;

// Sections
const userActionsDiv = document.getElementById('user-actions');
const userViewSection = document.getElementById('user-view');
const userInfoDiv = document.getElementById('user-info');
const publicBookViewSection = document.getElementById('public-book-view');
const memberDashboardSection = document.getElementById('member-dashboard');
const personnelDashboardSection = document.getElementById('personnel-dashboard');
const adminDashboardSection = document.getElementById('admin-dashboard');
const insufficientPrivilegeView = document.getElementById('insufficient-privilege-view');

// Containers
const publicBookListContainer = document.getElementById('public-book-list-container');
const userManagementContainer = document.getElementById('user-management-container');
const adminBookListContainer = document.getElementById('admin-book-list-container');
const adminMemberListContainer = document.getElementById('admin-member-list-container');
const borrowingListContainer = document.getElementById('borrowing-list-container');
const myRequestsContainer = document.getElementById('my-requests-container');
const pendingRequestsContainer = document.getElementById('pending-requests-container');


// Forms & Modals
const uploadForm = document.getElementById('upload-form');
const addBookForm = document.getElementById('add-book-form');
const addMemberForm = document.getElementById('add-member-form');
const issueBookForm = document.getElementById('issue-book-form');
const editModal = document.getElementById('edit-modal');
const modalTitle = document.getElementById('modal-title');
const editForm = document.getElementById('edit-form');
const editFormFields = document.getElementById('edit-form-fields');
const closeModalButton = document.querySelector('.close-button');


// --- API FETCH WRAPPER ---
async function apiFetch(url, options) {
    try {
        const response = await fetch(url, options);
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            window.location.href = '/';
            throw new Error('Unauthorized');
        }
        if (response.status === 403) {
            showInsufficientPrivilegeView();
            throw new Error('Forbidden');
        }
        return response;
    } catch (error) {
        console.error("API Call Error:", error);
        throw error;
    }
}

function showInsufficientPrivilegeView() {
    document.querySelectorAll('main section').forEach(s => s.classList.add('hidden'));
    insufficientPrivilegeView.classList.remove('hidden');
}

// --- CORE FUNCTIONS ---
async function main() {
    await checkUserStatus();
    renderUI();
    await loadPublicBooks();

    if (currentUser) {
        uploadForm.addEventListener('submit', handleProfilePictureUpload);
        addBookForm.addEventListener('submit', handleCreateBook);
        addMemberForm.addEventListener('submit', handleCreateMember);
        issueBookForm.addEventListener('submit', handleIssueBook);
        editForm.addEventListener('submit', handleUpdate);
        closeModalButton.onclick = () => editModal.classList.add('hidden');
        window.onclick = (event) => {
            if (event.target == editModal) editModal.classList.add('hidden');
        };
    }
}

async function checkUserStatus() {
    try {
        const response = await fetch('/api/users/me');
        currentUser = response.ok ? await response.json() : null;
    } catch (error) {
        currentUser = null;
        // Suppress console errors for 401/403 during initial check
    }
}

function renderUI() {
    // Hide all sections first
    document.querySelectorAll('main section').forEach(s => s.classList.add('hidden'));
    publicBookViewSection.classList.remove('hidden');

    userActionsDiv.innerHTML = '';
    if (currentUser) {
        userViewSection.classList.remove('hidden');
        const userHTML = `
            <img src="${currentUser.profilePictureUrl || 'https://via.placeholder.com/60'}" alt="Profile Picture">
            <span>Welcome, <strong>${currentUser.name}</strong> (${currentUser.role})</span>`;
        userInfoDiv.innerHTML = userHTML;

        const logoutButton = document.createElement('button');
        logoutButton.innerText = 'Logout';
        logoutButton.onclick = logout;
        userActionsDiv.appendChild(logoutButton);

        // Role-based dashboard rendering
        switch (currentUser.role) {
            case 'MEMBER':
                memberDashboardSection.classList.remove('hidden');
                loadMyRequests();
                break;
            case 'PERSONNEL':
                personnelDashboardSection.classList.remove('hidden');
                loadPersonnelData();
                break;
            case 'ADMIN':
                personnelDashboardSection.classList.remove('hidden');
                adminDashboardSection.classList.remove('hidden');
                loadPersonnelData();
                loadAdminData();
                break;
        }
    } else {
        const loginButton = document.createElement('button');
        loginButton.innerText = 'Login with Google';
        loginButton.onclick = () => window.location.href = '/oauth2/authorization/google';
        userActionsDiv.appendChild(loginButton);
    }
}

// --- DATA LOADING FUNCTIONS ---
async function loadPublicBooks() {
    try {
        const response = await fetch('/api/books');
        const books = await response.json();
        let tableHTML = `<table><thead><tr><th>Title</th><th>Publisher</th><th>Action</th></tr></thead><tbody>`;
        books.forEach(book => {
            let actionCell = 'Login to request';
            if (currentUser && currentUser.role === 'MEMBER') {
                actionCell = `<button class="request-btn" onclick="handleBookRequest(${book.id})">Request</button>`;
            } else if (currentUser) {
                actionCell = '<span class="text-muted">Staff View</span>';
            }
            tableHTML += `<tr><td>${book.title}</td><td>${book.publisher || 'N/A'}</td><td>${actionCell}</td></tr>`;
        });
        tableHTML += '</tbody></table>';
        publicBookListContainer.innerHTML = tableHTML;
    } catch (error) {
        publicBookListContainer.innerHTML = '<p>An error occurred while loading books.</p>';
    }
}

function loadPersonnelData() {
    loadPendingRequests();
    loadAdminBooks();
    loadAdminMembers();
    loadBorrowings();
    populateIssueBookDropdowns();
}

function loadAdminData() {
    loadUsersForAdmin();
}

// --- MEMBER ACTIONS ---
async function handleBookRequest(bookId) {
    if (!confirm('Are you sure you want to request this book?')) return;
    try {
        const response = await apiFetch('/api/requests', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookId })
        });
        if (response.ok) {
            alert('Book request sent successfully!');
            loadMyRequests();
        } else {
            alert('Failed to send request.');
        }
    } catch (error) {
        console.error('Error requesting book:', error);
    }
}

async function loadMyRequests() {
    try {
        const response = await apiFetch('/api/requests/my-requests');
        const requests = await response.json();
        let tableHTML = '<table><thead><tr><th>Book Title</th><th>Request Date</th><th>Status</th></tr></thead><tbody>';
        if (requests.length === 0) {
            tableHTML += '<tr><td colspan="3">You have no book requests.</td></tr>';
        } else {
            requests.forEach(req => {
                tableHTML += `<tr><td>${req.bookTitle}</td><td>${req.requestDate}</td><td class="status-${req.status.toLowerCase()}">${req.status}</td></tr>`;
            });
        }
        tableHTML += '</tbody></table>';
        myRequestsContainer.innerHTML = tableHTML;
    } catch (error) {
        myRequestsContainer.innerHTML = '<p>Error loading your requests.</p>';
    }
}

// --- PERSONNEL/ADMIN ACTIONS ---
async function loadPendingRequests() {
    try {
        const response = await apiFetch('/api/requests');
        const requests = await response.json();
        const pendingRequests = requests.filter(r => r.status === 'PENDING');

        let tableHTML = '<table><thead><tr><th>User</th><th>Book</th><th>Request Date</th><th>Status</th><th>Actions</th></tr></thead><tbody>';

        if (pendingRequests.length === 0) {
            tableHTML += '<tr><td colspan="5">No pending requests.</td></tr>';
        } else {
            pendingRequests.forEach(req => {
                tableHTML += `
                    <tr>
                        <td>${req.userName}</td> 
                        <td>${req.bookTitle}</td>
                        <td>${req.requestDate}</td>
                        <td>${req.status}</td>
                        <td class="action-buttons">
                            <button class="approve-btn" onclick="handleApproveRequest(${req.id})">Approve</button>
                            <button class="reject-btn" onclick="handleRejectRequest(${req.id})">Reject</button>
                        </td>
                    </tr>`;
            });
        }
        tableHTML += '</tbody></table>';
        pendingRequestsContainer.innerHTML = tableHTML;
    } catch (error) {
        pendingRequestsContainer.innerHTML = '<p>Error loading pending requests.</p>';
    }
}

async function handleApproveRequest(requestId) {
    if (!confirm('Approve this request? A borrowing record will be created.')) return;
    try {
        await apiFetch(`/api/requests/${requestId}/approve`, { method: 'PUT' });
        alert('Request approved!');
        loadPendingRequests();
        loadBorrowings();
    } catch(e) { alert('Failed to approve request.'); }
}

async function handleRejectRequest(requestId) {
    if (!confirm('Are you sure you want to reject this request?')) return;
    try {
        await apiFetch(`/api/requests/${requestId}/reject`, { method: 'PUT' });
        alert('Request rejected.');
        loadPendingRequests();
    } catch(e) { alert('Failed to reject request.'); }
}

async function loadUsersForAdmin() {
    try {
        const response = await apiFetch('/api/admin/users');
        const users = await response.json();
        let tableHTML = `<table><thead><tr><th>Picture</th><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr></thead><tbody>`;
        users.forEach(user => {
            const profilePicUrl = user.profilePictureUrl || 'https://via.placeholder.com/40';
            const roles = ['MEMBER', 'PERSONNEL', 'ADMIN'];
            let options = roles.map(r => `<option value="${r}" ${user.role === r ? 'selected' : ''}>${r}</option>`).join('');
            tableHTML += `
                <tr>
                    <td><img src="${profilePicUrl}" alt="Profile" class="user-list-picture"></td>
                    <td>${user.name}</td>
                    <td>${user.email}</td>
                    <td>${user.role}</td>
                    <td>
                        <select id="role-select-${user.id}">${options}</select>
                        <button onclick="changeUserRole('${user.id}', '${user.email}')">Save</button>
                    </td>
                </tr>`;
        });
        tableHTML += '</tbody></table>';
        userManagementContainer.innerHTML = tableHTML;
    } catch (error) {
        if (error.message !== 'Forbidden') {
            userManagementContainer.innerHTML = '<p>Error loading users.</p>';
        }
    }
}

async function changeUserRole(userId, email) {
    const selectElement = document.getElementById(`role-select-${userId}`);
    const newRole = selectElement.value;
    if (!confirm(`Change ${email}'s role to ${newRole}?`)) return;

    try {
        const response = await apiFetch(`/api/admin/users/${email}/role`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ role: newRole })
        });
        if (response.ok) {
            alert('Role updated successfully.');
            loadUsersForAdmin();
        } else {
            alert('Failed to update role.');
        }
    } catch (error) {
        console.error('Role change error:', error);
    }
}

async function handleProfilePictureUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('file-input');
    if (fileInput.files.length === 0) {
        alert('Please select a file to upload.');
        return;
    }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    try {
        const response = await apiFetch('/api/users/profile/picture', { method: 'POST', body: formData });
        if (response.ok) {
            alert('Profile picture uploaded successfully! Reloading...');
            window.location.reload();
        } else {
            alert('Upload failed.');
        }
    } catch (error) {
        console.error('Upload error:', error);
    }
}

async function loadAdminBooks() {
    try {
        const response = await apiFetch('/api/books');
        const books = await response.json();
        let tableHTML = `<table><thead><tr><th>ID</th><th>Title</th><th>Publisher</th><th>Actions</th></tr></thead><tbody>`;
        books.forEach(book => {
            tableHTML += `
                <tr>
                    <td>${book.id}</td>
                    <td>${book.title}</td>
                    <td>${book.publisher || 'N/A'}</td>
                    <td class="action-buttons">
                        <button class="edit-btn" onclick="openEditModal('book', ${book.id})">Edit</button>
                        <button class="delete-btn" onclick="deleteBook(${book.id})">Delete</button>
                    </td>
                </tr>`;
        });
        tableHTML += '</tbody></table>';
        adminBookListContainer.innerHTML = tableHTML;
    } catch (error) {
        adminBookListContainer.innerHTML = '<p>Error loading books.</p>';
    }
}

async function loadAdminMembers() {
    try {
        const response = await apiFetch('/api/members');
        const members = await response.json();
        let tableHTML = `<table><thead><tr><th>ID</th><th>Name</th><th>Address</th><th>Telephone</th><th>Actions</th></tr></thead><tbody>`;
        members.forEach(member => {
            tableHTML += `
                <tr>
                    <td>${member.id}</td>
                    <td>${member.name}</td>
                    <td>${member.address || 'N/A'}</td>
                    <td>${member.telephone || 'N/A'}</td>
                    <td class="action-buttons">
                        <button class="edit-btn" onclick="openEditModal('member', ${member.id})">Edit</button>
                        <button class="delete-btn" onclick="deleteMember(${member.id})">Delete</button>
                    </td>
                </tr>`;
        });
        tableHTML += '</tbody></table>';
        adminMemberListContainer.innerHTML = tableHTML;
    } catch (error) {
        adminMemberListContainer.innerHTML = '<p>Error loading members.</p>';
    }
}

async function loadBorrowings() {
    try {
        const response = await apiFetch('/api/borrowings');
        const borrowings = await response.json();
        let tableHTML = `<table><thead><tr><th>Member</th><th>Book</th><th>Issue Date</th><th>Due Date</th><th>Return Status</th></tr></thead><tbody>`;
        borrowings.forEach(b => {
            const returnStatus = b.returnDate
                ? `Returned on ${b.returnDate}`
                : `<button class="return-btn" onclick="handleReturnBook(${b.id})">Mark as Returned</button>`;
            tableHTML += `
                <tr>
                    <td>${b.memberName}</td>
                    <td>${b.bookTitle}</td>
                    <td>${b.issueDate}</td>
                    <td>${b.dueDate}</td>
                    <td>${returnStatus}</td>
                </tr>`;
        });
        tableHTML += '</tbody></table>';
        borrowingListContainer.innerHTML = tableHTML;
    } catch (error) {
        borrowingListContainer.innerHTML = '<p>Error loading borrowing records.</p>';
    }
}

async function populateIssueBookDropdowns() {
    try {
        const [membersRes, booksRes] = await Promise.all([apiFetch('/api/members'), apiFetch('/api/books')]);
        const members = await membersRes.json();
        const books = await booksRes.json();

        const memberSelect = document.getElementById('issue-member-select');
        const bookSelect = document.getElementById('issue-book-select');

        memberSelect.length = 1;
        bookSelect.length = 1;

        members.forEach(m => memberSelect.add(new Option(`${m.name} (ID: ${m.id})`, m.id)));
        books.forEach(b => bookSelect.add(new Option(`${b.title} (ID: ${b.id})`, b.id)));
    } catch (error) {
        console.error('Failed to populate dropdowns:', error);
    }
}

async function handleCreateBook(event) {
    event.preventDefault();
    const title = document.getElementById('book-title').value;
    const publisher = document.getElementById('book-publisher').value;
    try {
        const response = await apiFetch('/api/books', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ title, publisher })
        });
        if (response.ok) {
            alert('Book added!');
            addBookForm.reset();
            loadAdminBooks();
            loadPublicBooks();
        } else {
            alert('Failed to add book.');
        }
    } catch (error) { console.error('Error creating book:', error); }
}

async function handleCreateMember(event) {
    event.preventDefault();
    const name = document.getElementById('member-name').value;
    const address = document.getElementById('member-address').value;
    const telephone = document.getElementById('member-telephone').value;
    try {
        const response = await apiFetch('/api/members', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name, address, telephone })
        });
        if (response.ok) {
            alert('Member added!');
            addMemberForm.reset();
            loadAdminMembers();
        } else {
            alert('Failed to add member.');
        }
    } catch (error) { console.error('Error creating member:', error); }
}

async function deleteBook(id) {
    if (!confirm(`Delete book with ID ${id}?`)) return;
    try {
        await apiFetch(`/api/books/${id}`, { method: 'DELETE' });
        alert('Book deleted.');
        loadAdminBooks();
        loadPublicBooks();
    } catch (error) { console.error('Error deleting book:', error); }
}

async function deleteMember(id) {
    if (!confirm(`Delete member with ID ${id}?`)) return;
    try {
        await apiFetch(`/api/members/${id}`, { method: 'DELETE' });
        alert('Member deleted.');
        loadAdminMembers();
    } catch (error) { console.error('Error deleting member:', error); }
}

async function handleIssueBook(event) {
    event.preventDefault();
    const memberId = document.getElementById('issue-member-select').value;
    const bookId = document.getElementById('issue-book-select').value;
    const issueDate = new Date().toISOString().split('T')[0];
    const dueDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

    try {
        const response = await apiFetch('/api/borrowings/issue', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ memberId, bookId, issueDate, dueDate })
        });
        if (response.ok) {
            alert('Book issued!');
            issueBookForm.reset();
            loadBorrowings();
        } else {
            alert('Failed to issue book.');
        }
    } catch (error) { console.error('Error issuing book:', error); }
}

async function handleReturnBook(borrowingId) {
    if (!confirm('Mark this book as returned?')) return;
    try {
        await apiFetch(`/api/borrowings/${borrowingId}/return`, { method: 'PUT' });
        alert('Book returned.');
        loadBorrowings();
    } catch (error) { console.error('Error returning book:', error); }
}

async function openEditModal(type, id) {
    try {
        const response = await apiFetch(`/api/${type}s/${id}`);
        const data = await response.json();
        editForm.dataset.type = type;
        editForm.dataset.id = id;
        let fieldsHtml = '';
        if (type === 'book') {
            modalTitle.innerText = 'Edit Book';
            fieldsHtml = `
                <input type="text" id="edit-book-title" value="${data.title}" required>
                <input type="text" id="edit-book-publisher" value="${data.publisher || ''}">`;
        } else if (type === 'member') {
            modalTitle.innerText = 'Edit Member';
            fieldsHtml = `
                <input type="text" id="edit-member-name" value="${data.name}" required>
                <input type="text" id="edit-member-address" value="${data.address || ''}">
                <input type="text" id="edit-member-telephone" value="${data.telephone || ''}">`;
        }
        editFormFields.innerHTML = fieldsHtml;
        editModal.classList.remove('hidden');
    } catch (error) {
        alert('Could not load item details.');
    }
}

async function handleUpdate(event) {
    event.preventDefault();
    const { type, id } = editForm.dataset;
    let payload = {};
    if (type === 'book') {
        payload = { title: document.getElementById('edit-book-title').value, publisher: document.getElementById('edit-book-publisher').value };
    } else if (type === 'member') {
        payload = { name: document.getElementById('edit-member-name').value, address: document.getElementById('edit-member-address').value, telephone: document.getElementById('edit-member-telephone').value };
    }

    try {
        const response = await apiFetch(`/api/${type}s/${id}`, {
            method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        if (response.ok) {
            alert('Updated!');
            editModal.classList.add('hidden');
            if (type === 'book') {
                loadAdminBooks();
                loadPublicBooks();
            } else {
                loadAdminMembers();
            }
        } else {
            alert(`Failed to update ${type}.`);
        }
    } catch (error) { console.error(`Error updating ${type}:`, error); }
}

function logout() {
    fetch('/logout', { method: 'POST' }).then(() => window.location.href = '/');
}