window.addEventListener('DOMContentLoaded', main);

let currentUser = null;
const userActionsDiv = document.getElementById('user-actions');
const userViewSection = document.getElementById('user-view');
const userInfoDiv = document.getElementById('user-info');
const adminPanelSection = document.getElementById('admin-panel');
const bookViewSection = document.getElementById('book-view');
const bookListContainer = document.getElementById('book-list-container');
const userManagementContainer = document.getElementById('user-management-container');
const uploadForm = document.getElementById('upload-form');
const insufficientPrivilegeView = document.getElementById('insufficient-privilege-view');
const adminBookListContainer = document.getElementById('admin-book-list-container');
const adminMemberListContainer = document.getElementById('admin-member-list-container');
const addBookForm = document.getElementById('add-book-form');
const addMemberForm = document.getElementById('add-member-form');
const borrowingListContainer = document.getElementById('borrowing-list-container');
const issueBookForm = document.getElementById('issue-book-form');
const editModal = document.getElementById('edit-modal');
const modalTitle = document.getElementById('modal-title');
const editForm = document.getElementById('edit-form');
const editFormFields = document.getElementById('edit-form-fields');
const closeModalButton = document.querySelector('.close-button');

// --- GLOBAL API FETCH WRAPPER ---
async function apiFetch(url, options) {
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
}

function showInsufficientPrivilegeView() {
    userViewSection.classList.add('hidden');
    adminPanelSection.classList.add('hidden');
    bookViewSection.classList.add('hidden');
    insufficientPrivilegeView.classList.remove('hidden');
}


// --- CORE FUNCTIONS ---

async function main() {
    await checkUserStatus();
    renderUI();
    if (insufficientPrivilegeView.classList.contains('hidden')) {
        await loadBooks();
    }

    if (currentUser && currentUser.role === 'ADMIN') {
        addBookForm.addEventListener('submit', handleCreateBook);
        addMemberForm.addEventListener('submit', handleCreateMember);
        issueBookForm.addEventListener('submit', handleIssueBook);
        editForm.addEventListener('submit', handleUpdate);

        closeModalButton.onclick = () => editModal.classList.add('hidden');
        window.onclick = (event) => {
            if (event.target == editModal) {
                editModal.classList.add('hidden');
            }
        };
    }
}

async function checkUserStatus() {
    try {
        const response = await apiFetch('/api/users/me');
        if (response.ok) {
            currentUser = await response.json();
        } else {
            currentUser = null;
        }
    } catch (error) {
        if (error.message !== 'Unauthorized' && error.message !== 'Forbidden') {
            console.error('Failed to check user status:', error);
        }
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

        if (currentUser && currentUser.role === 'ADMIN') {
            adminPanelSection.classList.remove('hidden');
            loadUsersForAdmin();
            loadAdminBooks();
            loadAdminMembers();
            loadBorrowings();
            populateIssueBookDropdowns();
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
        const response = await apiFetch('/api/admin/users');
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
        if (error.message !== 'Forbidden') {
            userManagementContainer.innerHTML = '<p>An error occurred while loading users.</p>';
            console.error('Failed to load users:', error);
        }
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
        bookListContainer.innerHTML = '<p>An error occurred while loading books.</p>';
        console.error('Failed to load books:', error);
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
        adminMemberListContainer.innerHTML = '<p>An error occurred while loading members.</p>';
        console.error('Failed to load members:', error);
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
        console.error('Failed to load borrowings:', error);
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

// --- ADMIN ACTION HANDLERS ---

async function changeUserRole(email, newRole) {
    if (!confirm(`Are you sure you want to change the role of ${email} to ${newRole}?`)) return;
    try {
        const response = await apiFetch(`/api/admin/users/${email}/role`, {
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
        const response = await apiFetch('/api/books', {
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
        const response = await apiFetch('/api/members', {
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
        const response = await apiFetch(`/api/books/${id}`, { method: 'DELETE' });
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
        const response = await apiFetch(`/api/members/${id}`, { method: 'DELETE' });
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

async function handleIssueBook(event) {
    event.preventDefault();
    const memberId = document.getElementById('issue-member-select').value;
    const bookId = document.getElementById('issue-book-select').value;
    const issueDate = new Date().toISOString().split('T')[0]; // Today's date in YYYY-MM-DD
    const dueDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // 14 days from now

    const borrowingData = { memberId, bookId, issueDate, dueDate };

    try {
        const response = await apiFetch('/api/borrowings/issue', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(borrowingData)
        });
        if (response.ok) {
            alert('Book issued successfully!');
            issueBookForm.reset();
            loadBorrowings();
        } else {
            alert('Failed to issue book.');
        }
    } catch (error) {
        console.error('Error issuing book:', error);
    }
}

async function handleReturnBook(borrowingId) {
    if (!confirm('Are you sure you want to mark this book as returned?')) return;
    try {
        const response = await apiFetch(`/api/borrowings/${borrowingId}/return`, { method: 'PUT' });
        if (response.ok) {
            alert('Book returned successfully.');
            loadBorrowings();
        } else {
            alert('Failed to return book.');
        }
    } catch (error) {
        console.error('Error returning book:', error);
    }
}


// --- EDIT MODAL FUNCTIONS ---

async function openEditModal(type, id) {
    try {
        const response = await apiFetch(`/api/${type}s/${id}`); // e.g., /api/books/1
        if (!response.ok) throw new Error('Failed to fetch item data.');
        const data = await response.json();

        editForm.dataset.type = type;
        editForm.dataset.id = id;

        let fieldsHtml = '';
        if (type === 'book') {
            modalTitle.innerText = 'Edit Book';
            fieldsHtml = `
                <input type="text" id="edit-book-title" value="${data.title}" placeholder="Title" required>
                <input type="text" id="edit-book-publisher" value="${data.publisher || ''}" placeholder="Publisher">
            `;
        } else if (type === 'member') {
            modalTitle.innerText = 'Edit Member';
            fieldsHtml = `
                <input type="text" id="edit-member-name" value="${data.name}" placeholder="Name" required>
                <input type="text" id="edit-member-address" value="${data.address || ''}" placeholder="Address">
                <input type="text" id="edit-member-telephone" value="${data.telephone || ''}" placeholder="Telephone">
            `;
        }

        editFormFields.innerHTML = fieldsHtml;
        editModal.classList.remove('hidden');

    } catch (error) {
        console.error(`Error opening edit modal for ${type} ${id}:`, error);
        alert('Could not load item details for editing.');
    }
}

async function handleUpdate(event) {
    event.preventDefault();
    const { type, id } = editForm.dataset;
    let payload = {};

    if (type === 'book') {
        payload = {
            title: document.getElementById('edit-book-title').value,
            publisher: document.getElementById('edit-book-publisher').value
        };
    } else if (type === 'member') {
        payload = {
            name: document.getElementById('edit-member-name').value,
            address: document.getElementById('edit-member-address').value,
            telephone: document.getElementById('edit-member-telephone').value
        };
    }

    try {
        const response = await apiFetch(`/api/${type}s/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert(`${type.charAt(0).toUpperCase() + type.slice(1)} updated successfully!`);
            editModal.classList.add('hidden');
            if (type === 'book') {
                loadAdminBooks();
                loadBooks(); // Also refresh public list
            } else {
                loadAdminMembers();
            }
        } else {
            alert(`Failed to update ${type}.`);
        }
    } catch (error) {
        console.error(`Error updating ${type}:`, error);
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
        const response = await apiFetch('/api/users/profile/picture', { method: 'POST', body: formData });
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