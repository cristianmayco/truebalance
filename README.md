# TrueBalance - Financial Management System

Complete financial management system with backend REST API and frontend SPA.

## Architecture
- **Backend**: Spring Boot 3.5.9 + PostgreSQL 16
- **Frontend**: React 18 + TypeScript + Vite
- **Deployment**: Docker Compose

## Quick Start

### Prerequisites
- Docker & Docker Compose installed

### Running the Application

1. Start all services:
   ```bash
   docker-compose up -d
   ```

2. Access the application:
   - **Frontend**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **PostgreSQL**: localhost:5432

3. View logs:
   ```bash
   docker-compose logs -f
   # Or for specific service
   docker-compose logs -f frontend
   docker-compose logs -f backend
   docker-compose logs -f postgres
   ```

4. Stop all services:
   ```bash
   docker-compose down
   ```

5. Stop and remove volumes (clean database):
   ```bash
   docker-compose down -v
   ```

### Rebuilding After Changes

If you make code changes:
```bash
docker-compose up -d --build
```

Or rebuild specific service:
```bash
docker-compose up -d --build frontend
docker-compose up -d --build backend
```

## Project Structure
```
truebalance/
├── docker-compose.yml           # Main orchestration file
├── truebalance-backend/         # Spring Boot API
│   ├── src/
│   ├── Dockerfile
│   └── README.md
└── truebalance-frontend/        # React SPA
    └── frontend/
        ├── src/
        ├── Dockerfile
        └── docs/
```

## Database Credentials
- **Database**: truebalance
- **User**: postgres
- **Password**: postgres
- **Port**: 5432

## Service Details

### PostgreSQL
- Container: `truebalance-db`
- Image: postgres:16-alpine
- Volume: `postgres_data` (persistent)

### Backend
- Container: `truebalance-backend`
- Port: 8080
- Health: Waits for postgres to be healthy
- Auto-restart enabled

### Frontend
- Container: `truebalance-frontend`
- Port: 3000 (host) → 80 (container)
- Nginx serves optimized production build
- API requests proxied to backend
- Auto-restart enabled

## Development
For local development without Docker, see individual README files:
- Backend: `truebalance-backend/README.md`
- Frontend: `truebalance-frontend/docs/README.md`

## Troubleshooting

### Port already in use
If ports are already allocated, stop conflicting services or change ports in docker-compose.yml

### Database connection issues
Check postgres health: `docker-compose ps`
View postgres logs: `docker-compose logs postgres`

### Frontend not loading
- Check backend is running: `docker-compose ps`
- Check nginx logs: `docker-compose logs frontend`
- Verify API proxy in nginx.conf

### Clean rebuild
```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```
