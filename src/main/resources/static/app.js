window.addEventListener('DOMContentLoaded', main);

let currentUser = null;
const userActionsDiv = document.getElementById('user-actions');
const userViewSection = document.getElementById('user-view');
const userInfoDiv = document.getElementById('user-info');
const adminPanelSection = document.getElementById('admin-panel');
const bookListContainer = document.getElementById('book-list-container');
const userManagementContainer = document.getElementById('user-management-container');
const uploadForm = document.getElementById('upload-form');
const insufficientPrivilegeView = document.getElementById('insufficient-privilege-view');
const adminBookListContainer = document.getElementById('admin-book-list-container');
const adminMemberListContainer = document.getElementById('admin-member-list-container');
const addBookForm = document.getElementById('add-book-form');
const addMemberForm = document.getElementById('add-member-form');


// --- CORE FUNCTIONS ---

async function main() {
    await checkUserStatus();
    renderUI();
    await loadBooks();

    if (currentUser && currentUser.role === 'ADMIN') {
        addBookForm.addEventListener('submit', handleCreateBook);
        addMemberForm.addEventListener('submit', handleCreateMember);
    }
}

async function checkUserStatus() {
    try {
        const response = await fetch('/api/users/me');
        if (response.ok) {
            currentUser = await response.json();
        } else {
            currentUser = null;
        }
    } catch (error) {
        console.error('Failed to check user status:', error);
        currentUser = null;
    }
}

function renderUI() {
    userActionsDiv.innerHTML = '';
    userViewSection.classList.add('hidden');
    adminPanelSection.classList.add('hidden');
    insufficientPrivilegeView.classList.add('hidden');

    if (currentUser) {
        userViewSection.classList.remove('hidden');
        const userHTML = `<img src="${currentUser.profilePictureUrl || 'https://via.placeholder.com/60'}" alt="Profile Picture"> <span>Welcome, <strong>${currentUser.name}</strong> (${currentUser.role})</span>`;
        userInfoDiv.innerHTML = userHTML;
        const logoutButton = document.createElement('button');
        logoutButton.innerText = 'Logout';
        logoutButton.onclick = logout;
        userActionsDiv.appendChild(logoutButton);

        if (currentUser.role === 'ADMIN') {
            adminPanelSection.classList.remove('hidden');
            loadUsersForAdmin();
            loadAdminBooks();
            loadAdminMembers();
        }
    } else {
        const loginButton = document.createElement('button');
        loginButton.innerText = 'Login with Google';
        loginButton.onclick = () => window.location.href = '/oauth2/authorization/google';
        userActionsDiv.appendChild(loginButton);
    }
}

async function loadBooks() {
    try {
        const response = await fetch('/api/books');
        const books = await response.json();
        let tableHTML = `<table><thead><tr><th>ID</th><th>Title</th><th>Publisher</th></tr></thead><tbody>`;
        books.forEach(book => {
            tableHTML += `<tr><td>${book.id}</td><td>${book.title}</td><td>${book.publisher || 'N/A'}</td></tr>`;
        });
        tableHTML += '</tbody></table>';
        bookListContainer.innerHTML = tableHTML;
    } catch (error) {
        bookListContainer.innerHTML = '<p>An error occurred while loading books.</p>';
        console.error('Failed to load books:', error);
    }
}


// --- ADMIN-SPECIFIC FUNCTIONS ---

async function loadUsersForAdmin() {
    try {
        const response = await fetch('/api/admin/users');
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const users = await response.json();
        let tableHTML = `<table><thead><tr><th>Picture</th><th>Name</th><th>Email</th><th>Role</th><!--<th>Actions</th>--></tr></thead><tbody>`;
        users.forEach(user => {
            const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
            const profilePicUrl = user.profilePictureUrl || 'https://via.placeholder.com/40';
            tableHTML += `<tr><td><img src="${profilePicUrl}" alt="Profile of ${user.name}" class="user-list-picture"></td><td>${user.name}</td><td>${user.email}</td><td>${user.role}</td><!-- <td><button onclick="changeUserRole('${user.email}', '${newRole}')">Make ${newRole}</button></td> --></tr>`;
        });
        tableHTML += '</tbody></table>';
        userManagementContainer.innerHTML = tableHTML;
    } catch (error) {
        userManagementContainer.innerHTML = '<p>An error occurred while loading users. Ensure you have ADMIN privileges.</p>';
        console.error('Failed to load users:', error);
    }
}

async function loadAdminBooks() {
    try {
        const response = await fetch('/api/books');
        const books = await response.json();
        let tableHTML = `<table><thead><tr><th>ID</th><th>Title</th><th>Publisher</th><th>Actions</th></tr></thead><tbody>`;
        books.forEach(book => {
            tableHTML += `<tr><td>${book.id}</td><td>${book.title}</td><td>${book.publisher || 'N/A'}</td><td><button class="delete-btn" onclick="deleteBook(${book.id})">Delete</button></td></tr>`;
        });
        tableHTML += '</tbody></table>';
        adminBookListContainer.innerHTML = tableHTML;
    } catch (error) {
        adminBookListContainer.innerHTML = '<p>An error occurred while loading books.</p>';
        console.error('Failed to load admin books:', error);
    }
}

async function loadAdminMembers() {
    try {
        const response = await fetch('/api/members');
        const members = await response.json();
        let tableHTML = `<table><thead><tr><th>ID</th><th>Name</th><th>Address</th><th>Telephone</th><th>Actions</th></tr></thead><tbody>`;
        members.forEach(member => {
            tableHTML += `<tr><td>${member.id}</td><td>${member.name}</td><td>${member.address || 'N/A'}</td><td>${member.telephone || 'N/A'}</td><td><button class="delete-btn" onclick="deleteMember(${member.id})">Delete</button></td></tr>`;
        });
        tableHTML += '</tbody></table>';
        adminMemberListContainer.innerHTML = tableHTML;
    } catch (error) {
        adminMemberListContainer.innerHTML = '<p>An error occurred while loading members.</p>';
        console.error('Failed to load members:', error);
    }
}

// --- ADMIN ACTION HANDLERS ---

async function changeUserRole(email, newRole) {
    if (!confirm(`Are you sure you want to change the role of ${email} to ${newRole}?`)) return;
    try {
        const response = await fetch(`/api/admin/users/${email}/role`, {
            method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ role: newRole })
        });
        if (response.ok) {
            alert('Role updated successfully.');
            loadUsersForAdmin();
        } else {
            alert('An error occurred while updating the role.');
        }
    } catch (error) {
        console.error('Role change error:', error);
    }
}

async function handleCreateBook(event) {
    event.preventDefault();
    const title = document.getElementById('book-title').value;
    const publisher = document.getElementById('book-publisher').value;
    const bookData = { title, publisher };

    try {
        const response = await fetch('/api/books', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bookData)
        });
        if (response.ok) {
            alert('Book added successfully!');
            addBookForm.reset();
            loadAdminBooks();
            loadBooks();
        } else {
            alert('Failed to add book.');
        }
    } catch (error) {
        console.error('Error creating book:', error);
    }
}

async function handleCreateMember(event) {
    event.preventDefault();
    const name = document.getElementById('member-name').value;
    const address = document.getElementById('member-address').value;
    const telephone = document.getElementById('member-telephone').value;
    const memberData = { name, address, telephone };

    try {
        const response = await fetch('/api/members', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(memberData)
        });
        if (response.ok) {
            alert('Member added successfully!');
            addMemberForm.reset();
            loadAdminMembers();
        } else {
            alert('Failed to add member.');
        }
    } catch (error) {
        console.error('Error creating member:', error);
    }
}

async function deleteBook(id) {
    if (!confirm(`Are you sure you want to delete book with ID ${id}?`)) return;
    try {
        const response = await fetch(`/api/books/${id}`, { method: 'DELETE' });
        if (response.ok) {
            alert('Book deleted successfully.');
            loadAdminBooks();
            loadBooks();
        } else {
            alert('Failed to delete book.');
        }
    } catch (error) {
        console.error('Error deleting book:', error);
    }
}

async function deleteMember(id) {
    if (!confirm(`Are you sure you want to delete member with ID ${id}?`)) return;
    try {
        const response = await fetch(`/api/members/${id}`, { method: 'DELETE' });
        if (response.ok) {
            alert('Member deleted successfully.');
            loadAdminMembers(); // Refresh the list
        } else {
            alert('Failed to delete member.');
        }
    } catch (error) {
        console.error('Error deleting member:', error);
    }
}


// --- EVENT HANDLERS AND OTHER UTILITIES ---

function logout() {
    fetch('/logout', { method: 'POST' })
        .then(() => {
            currentUser = null;
            window.location.reload();
        })
        .catch(error => console.error('Logout failed:', error));
}

uploadForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const fileInput = document.getElementById('file-input');
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    try {
        const response = await fetch('/api/users/profile/picture', { method: 'POST', body: formData });
        if (response.ok) {
            alert('Profile picture uploaded successfully!');
            window.location.reload();
        } else {
            const errorText = await response.text();
            alert(`An error occurred while uploading: ${errorText}`);
        }
    } catch (error) {
        console.error('Upload error:', error);
        alert('A network error occurred during upload.');
    }
});