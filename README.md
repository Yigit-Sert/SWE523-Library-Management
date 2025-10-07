
 📖 Library Management System

 This project is a web application developed to manage the core operations of a library, including members, books, and borrowing records. The project is built using Java and the Spring Boot framework.

 ![Java](https://img.shields.io/badge/Java-17-blue)![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)![Maven](https://img.shields.io/badge/Maven-4.0.0-red)![Database](https://img.shields.io/badge/Database-MySQL-orange)

 ## ✨ Features

 -   **Member Management:** Add, view, edit, and delete library members.
 -   **Book Management:** Add, view, edit, and delete books from the library's collection.
 -   **Borrowing System:**
     -   Issue books to members with a specified due date.
     -   Track the status of borrowed books (On Loan / Returned).
     -   Record the return of books.
 -   **Web Interface:** An intuitive and clean user interface built with Thymeleaf and Bootstrap for easy interaction.

 ## 🛠️ Technologies Used

 ### Backend

 -   **Java 17**
 -   **Spring Boot 3.5.6**
     -   **Spring Web:** For the web layer and RESTful controllers.
     -   **Spring Data JPA:** For database interaction and repository management.
     -   **Thymeleaf:** Server-side Java template engine for the view layer.
 -   **MySQL:** Relational database for data persistence.
 -   **Hibernate:** JPA implementation for ORM (Object-Relational Mapping).
 -   **Lombok:** To reduce boilerplate code (getters, setters, constructors).
 -   **Maven:** For project build and dependency management.

 ### Frontend

 -   **HTML5**
 -   **CSS3** (with **Bootstrap 5** for styling and layout)
 -   **Thymeleaf:** For integrating backend data into HTML templates.

 ---

 ## 🚀 Setup and Installation

 To get this project up and running on your local machine, follow these steps:

 **Prerequisites:**

 -   JDK 17 or later.
 -   Apache Maven.
 -   A running MySQL server instance.

 **Steps:**

 1.  **Clone the repository:**
     ```bash
     git clone https://github.com/your-username/library-management.git
     cd library-management
     ```

 2.  **Create the MySQL Database:**
     -   Log in to your MySQL server.
     -   Create a new database for the project.
         ```sql
         CREATE DATABASE library_db;
         ```

 3.  **Configure the Application:**
     -   Navigate to the `src/main/resources/` directory.
     -   Create or edit the `application.properties` file.
     -   Update the file with your MySQL database URL, username, and password. See the **Configuration** section below.

 4.  **Build and Run the Application:**
     -   Use the Maven wrapper to build and run the project.
     -   **On Linux/macOS:**
         ```bash
         ./mvnw spring-boot:run
         ```
     -   **On Windows:**
         ```bash
         mvnw.cmd spring-boot:run
         ```
     The application will start by default at `http://localhost:8080`.

 ## ⚙️ Configuration

 The main configuration file is located at `src/main/resources/application.properties`. It is highly recommended to use **environment variables** for sensitive data instead of hardcoding them.

 **Example `application.properties` (with Environment Variables):**

 ```properties
 # --- Database Configuration ---
 spring.datasource.url=${SPRING_DATASOURCE_URL}
 spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
 spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
 spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

 # --- JPA/Hibernate Configuration ---
 # 'update' automatically updates the schema. Use 'validate' or 'none' in production.
 spring.jpa.hibernate.ddl-auto=update
 spring.jpa.show-sql=true
 spring.jpa.properties.hibernate.format_sql=true
 ```

 **Set the following environment variables on your system:**

 -   `SPRING_DATASOURCE_URL`: `jdbc:mysql://localhost:3306/library_db`
 -   `SPRING_DATASOURCE_USERNAME`: `your-mysql-username`
 -   `SPRING_DATASOURCE_PASSWORD`: `your-mysql-password`

 ## 💻 Usage

 Once the application is running, open your web browser and navigate to `http://localhost:8080`.

 -   **Home Page (`/`):** A welcome screen with navigation links.
 -   **Members Page (`/members`):** View, add, edit, and delete library members.
 -   **Books Page (`/books`):** View, add, edit, and delete books.
 -   **Borrowings Page (`/borrowings`):** View all borrowing records, issue a new book, or mark a book as returned.
