# Ginning Mill ERP

Spring Boot 3.4 backend and Angular 17 frontend for the ginning mill ERP.
The application includes authentication and authorization, audit logging, master
data, kapas purchases, bale production, sales, and document text extraction.

## Run on a new system

### Requirements

Install the following tools before starting:

- Git
- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop or Docker Engine with Compose

Check the installations:

```bash
java -version
mvn -version
node --version
npm --version
docker compose version
```

### One-shot local startup

From the repository root, run this single command:

```bash
chmod +x scripts/start-local.sh
./scripts/start-local.sh
```

The script starts PostgreSQL, installs frontend dependencies when needed, and
runs the backend and frontend together. Press `Ctrl+C` to stop the application
processes. PostgreSQL remains available for the next run.

Alternatively, start each service manually in separate terminals:

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
cd frontend
npm ci
npm start
```

Open:

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

The backend starts with the `dev` profile, connects to the PostgreSQL container,
and applies all Flyway migrations automatically.

To stop the database:

```bash
docker compose down
```

To stop the database and delete its local data:

```bash
docker compose down -v
```

## Default local database

The Docker Compose file creates:

| Setting | Value |
| --- | --- |
| Host | `localhost` |
| Port | `5433` |
| Database | `erp_db` |
| Username | `erp_user` |
| Password | `erp_password` |

The development profile uses these same defaults. Override them when needed:

```bash
export DB_USER=erp_user
export DB_PASSWORD=erp_password
export JWT_SECRET='replace-with-a-long-random-secret'
export GOOGLE_CLIENT_ID='optional-google-client-id'
export CORS_ALLOWED_ORIGINS='http://localhost:4200'
```

Never use the example credentials or JWT secret in a deployed environment.

## Useful commands

Run backend tests:

```bash
mvn test
```

Compile the backend without tests:

```bash
mvn -DskipTests compile
```

Build the frontend:

```bash
cd frontend
npm ci
npm run build
```

Run the frontend in development mode:

```bash
cd frontend
npm start
```

## Authentication endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

## Repository structure

```text
.
├── src/main/java/              # Spring Boot backend source
├── src/main/resources/         # Configuration and Flyway migrations
├── src/test/                   # Backend tests
├── frontend/                   # Angular frontend
├── docker-compose.yml          # Local PostgreSQL
└── pom.xml                     # Maven build
```

## Troubleshooting

### PostgreSQL port is already in use

Stop the process using port `5433`, or change the host-side port in
`docker-compose.yml` and update `src/main/resources/application-dev.yml` to
match.

### The backend cannot connect to PostgreSQL

Check the container and its health:

```bash
docker compose ps
docker compose logs postgres
```

Start it again if necessary:

```bash
docker compose up -d
```

### Frontend dependencies fail to install

Use the Node.js version required by the project and run a clean install:

```bash
rm -rf frontend/node_modules frontend/.angular frontend/dist
cd frontend
npm ci
```
