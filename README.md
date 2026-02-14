# Atlas - Employee Management System

A full-stack employee management and resource allocation system built with Spring Boot, Angular, and PostgreSQL.

## Features

- **Employee Management**: View and import employee data from Excel
- **Project Tracking**: Create and manage projects with allocation tracking
- **Utilization Monitoring**: Track monthly utilization with visual progress bars
- **Role-Based Access Control (RBAC)**: 5-level hierarchy (Admin, N1-N4)
- **Dashboard**: Real-time statistics and KPIs
- **Dark Theme UI**: Modern Atlas UX design

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA
- **Frontend**: Angular 17, Standalone Components, Signals
- **Database**: PostgreSQL 15
- **Authentication**: JWT
- **Containerization**: Docker & Docker Compose

## Getting Started

### Prerequisites

- Docker & Docker Compose
- (Optional) Java 17 & Node.js 20 for local development

### Quick Start with Docker

```bash
# Clone the repository
git clone <repo-url>
cd atlas

# Start all services
docker-compose up -d

# Access the application
open http://localhost
```

### Local Development

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Default Credentials

| Role | Username | Password |
|------|----------|----------|
| Executive (N1) | Ahmad.Elharany@gizasystems.com | password123 |

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | User authentication |
| `/api/dashboard/stats` | GET | Dashboard statistics |
| `/api/employees` | GET | List employees |
| `/api/employees/import` | POST | Import from Excel |
| `/api/projects` | GET/POST | List/create projects |
| `/api/allocations` | GET/POST/PUT/DELETE | Manage allocations |

## RBAC Hierarchy

- **System Admin**: Full access
- **Executive (N1)**: Company-wide visibility
- **Head (N2)**: Parent Tower visibility
- **Department Manager (N3)**: Tower visibility
- **Team Lead (N4)**: Project visibility

## License

Private - All rights reserved
