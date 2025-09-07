# ShoppeR – Full Stack E-Commerce

##  Tech Stack
- Backend: Spring Boot, Spring Data JPA, H2
- Frontend: React, Vite
- Database: H2 (in-memory)

##  Features
- Add, update, delete products
- Search & filter
- Cart functionality
- Dark mode
- REST API integration

## Project Structure
shopper-ecommerce/
├── ecom-project/             # The Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/ecom_project/
│   │   │   │       ├── controller/   # REST API controllers
│   │   │   │       ├── model/        # Data models (Entities)
│   │   │   │       ├── repo/         # Repositories for data access
│   │   │   │       ├── service/      # Business logic and services
│   │   │   │       └── EcomProjectApplication.java
│   │   │   └── resources/    # Configuration files and templates
│   │   └── test/
│   └── pom.xml               # Maven configuration file
│
└── ecom-frontend-5-main/     # The React Frontend
    ├── src/
    │   ├── assets/           # Images and static assets
    │   ├── components/       # Reusable React components
    │   ├── Context/          # Context API for state management
    │   └── App.jsx, index.css, main.jsx # Main app files
    ├── node_modules/         # (ignored by Git)
    ├── public/
    └── package.json          # npm dependencies


##  How to Run
### Backend
```bash
cd backend
mvn spring-boot:run
### Frontend
cd frontend
npm install
npm run dev
