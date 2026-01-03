# 📖 Library Management System (Microservices)

This project is a distributed web application developed to manage the core operations of a library. It allows users to login via Google OAuth2, request books, and allows administrators/personnel to manage members and inventory.

The system is built using a **Microservices Architecture** with **Java (Spring Boot)**, **Node.js**, and **MySQL**, orchestrated via **Docker** and **Kubernetes**.

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![Node.js](https://img.shields.io/badge/Node.js-18-green)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue)

## 🏗️ Architecture

The system consists of the following microservices:

1.  **Member Service (Port 8081):** Manages user profiles, Google OAuth2 authentication, and roles.
2.  **Book Service (Port 8083):** Manages the book inventory.
3.  **Borrowing Service (Port 8082):** Handles borrowing logic, issue dates, and return records.
4.  **View Service (Port 8080):** A Node.js/Express Gateway that serves the frontend and proxies API requests to backend services.

## ✨ Features

-   **Authentication:** Secure login via Google OAuth2.
-   **Role-Based Access:** Distinct capabilities for Members, Personnel, and Admins.
-   **Member Management:** Administer library members and personnel.
-   **Book Management:** Add, view, edit, and delete books (Admin/Personnel).
-   **Borrowing System:**
    -   Request books (Members).
    -   Approve/Reject requests (Personnel).
    -   Issue and Return tracking.
-   **Distributed Caching:** Redis is used for session management and caching.

## 🛠️ Technologies Used

### Backend
-   **Java 17** & **Spring Boot 3.5.6**
-   **Spring Security (OAuth2 Client)**
-   **Spring Data JPA** & **MySQL** (Per-service databases)
-   **Redis:** For distributed session storage and caching.
-   **Maven:** Build tool.

### Frontend / Gateway
-   **Node.js & Express:** Serves static content and acts as an API Gateway.
-   **HTML5, CSS3, Vanilla JavaScript:** Frontend UI.
-   **Nginx:** Reverse proxy (for Docker Compose setup).

---

## 🚀 Setup and Installation (Docker Compose)

The easiest way to run the application locally is using Docker Compose.

**Prerequisites:**
-   Docker & Docker Compose installed.
-   Google Cloud Console Project (for OAuth2 Credentials).

**Steps:**

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Yigit-Sert/SWE523-Library-Management.git
    cd library-management
    ```

2.  **Configure Environment Variables:**
    Create a `.env` file in the project root. You can use the example provided:
    ```bash
    cp .env.example .env
    ```
    **Important:** Open `.env` and update `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` with your own credentials.

3.  **Build and Run:**
    ```bash
    docker compose up --build
    ```
    *This will build the Java JARs, the Node image, setup MySQL databases, and start Redis.*

4.  **Access the Application:**
    Open your browser and navigate to: `http://localhost:8080`

---

## ☸️ Kubernetes Deployment

To deploy this application on a Kubernetes cluster (e.g., Minikube, Docker Desktop K8s), follow these steps:

1.  **Create Secrets:**
    The project uses a `secrets.yaml` file to manage sensitive data. A template is provided in `k8s/secrets.example.yaml`.

    Copy the example file:
    ```bash
    cp k8s/secrets.example.yaml k8s/secrets.yaml
    ```
    **Edit `k8s/secrets.yaml`** and insert your Base64 encoded (or plain string, depending on your setup/editor) MySQL passwords and Google OAuth keys.

2.  **Apply Configuration:**
    Apply the secrets and database deployments first:
    ```bash
    kubectl apply -f k8s/secrets.yaml
    kubectl apply -f k8s/redis.yaml
    kubectl apply -f k8s/member-db.yaml
    kubectl apply -f k8s/book-db.yaml
    kubectl apply -f k8s/borrowing-db.yaml
    ```

3.  **Deploy Microservices:**
    Wait for the databases to be healthy, then deploy the services:
    ```bash
    kubectl apply -f k8s/member-service.yaml
    kubectl apply -f k8s/book-service.yaml
    kubectl apply -f k8s/borrowing-service.yaml
    ```

4.  **Deploy View Layer:**
    Finally, deploy the frontend/gateway service:
    ```bash
    kubectl apply -f k8s/view-service.yaml
    ```

5.  **Access the Application:**
    The `view-service` is exposed as a LoadBalancer.
    -   **Docker Desktop / Local K8s:** Access via `http://localhost:8080`.
    -   **Minikube:** Run `minikube service view-service` to get the URL.

---

## ⚙️ Configuration Details

If you wish to run services manually (without Docker), you must configure the `application.properties` in each service's `src/main/resources` folder or pass Environment Variables.

**Key Environment Variables:**

| Variable | Description |
| :--- | :--- |
| `DB_HOST` | Hostname of the MySQL server (e.g., `localhost` or `member-db`) |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `REDIS_HOST` | Hostname of the Redis server |
| `GOOGLE_CLIENT_ID` | OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Client Secret |

**Databases:**
-   `member-service` uses `member_db`
-   `book-service` uses `book_db`
-   `borrowing-service` uses `borrowing_db`