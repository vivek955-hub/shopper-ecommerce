### **Project Title:** ShoppeR – Full Stack E-Commerce

---

### **About the Project**

ShoppeR is a complete e-commerce platform that allows users to manage products, search and filter items, and handle a shopping cart. The application is built using a modern full-stack approach, with a powerful backend and a responsive frontend.

### **Key Features**

* **Complete Product Management:** Easily add new products, and update or delete existing ones with full CRUD (Create, Read, Update, Delete) functionality.
* **Intuitive Search & Filter:** Find products quickly using a search bar and various filters.
* **Shopping Cart:** A fully functional cart system to manage selected items before checkout.
* **Modern UI:** Features a sleek and user-friendly interface, including a dark mode option for a better viewing experience.
* **RESTful API:** Integrates the frontend with a robust REST API for seamless data exchange.

### **Tech Stack**

* **Backend:** Spring Boot, Spring Data JPA, H2 Database
* **Frontend:** React, Vite
* **Database:** H2 (an in-memory database, great for development)

### **Project Structure**

This project is a monorepo containing a Spring Boot backend and a React frontend.

shopper-ecommerce                
├── ecom-project/                     # The Spring Boot Backend                                     
└── ecom-frontend-5-main/             # The React Frontend
   

### **How to Run the Project Locally**

To get this project running on your computer, you will need to start both the backend and the frontend separately.

#### **Prerequisites**

* Java Development Kit (JDK) 11 or higher
* Node.js and npm
* Apache Maven

#### **Step 1: Start the Backend**

1.  Open your terminal or command prompt.
2.  Navigate into the `backend` folder:
    ```bash
    cd backend
    ```
3.  Run the application using Maven:
    ```bash
    mvn spring-boot:run
    ```

#### **Step 2: Start the Frontend**

1.  Open a **new** terminal window.
2.  Navigate into the `frontend` folder:
    ```bash
    cd frontend
    ```
3.  Install the required packages:
    ```bash
    npm install
    ```
4.  Start the development server:
    ```bash
    npm run dev
    ```
5.  Open your web browser and go to the address shown in the terminal (usually `http://localhost:5173`).
