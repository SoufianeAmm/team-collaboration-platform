# Team Collaboration Platform

A full-stack team collaboration and project management platform (think a simplified Trello + Slack), built as a portfolio project.

**Author:** Soufiane Ammari
**Status: under active development — Phase 13 (Docker) complete.**

## Stack

- **Frontend:** React, TypeScript, Vite
- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA, WebSockets
- **Database:** SQLite (file-based, no server required locally)

This README will be expanded with architecture diagrams, feature docs, and setup instructions as each phase is built. See the development log below for what's done.

## Development phases

- [x] Phase 1 — Project setup
- [x] Phase 2 — Spring Boot backend + SQLite
- [x] Phase 3 — Database entities and relationships
- [x] Phase 4 — Authentication + JWT + Spring Security
- [x] Phase 5 — Project and task management
- [x] Phase 6 — React frontend
- [x] Phase 7 — Connect React to REST API
- [x] Phase 8 — File uploads
- [x] Phase 9 — Comments
- [x] Phase 10 — WebSockets and real-time chat
- [x] Phase 11 — Real-time task updates
- [x] Phase 12 — Notifications
- [x] Phase 13 — Docker
- [ ] Phase 14 — GitHub Actions CI/CD
- [ ] Phase 15 — Testing and polishing

## Running with Docker

The whole stack (backend + frontend, SQLite data persisted in a named volume) runs with one command — no JDK or Node needed on your machine.

```bash
docker compose up --build
```

Then open **http://localhost:8080** — nginx serves the built frontend and proxies `/api` and `/ws` through to the backend container, so everything's on one origin (no CORS to worry about). The backend is also reachable directly at http://localhost:8081 for debugging.

Stop with `docker compose down` (add `-v` to also wipe the SQLite volume and start fresh next time).

## Running locally (without Docker)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Visit http://localhost:5173 — you should see the default Vite + React starter page.

### Backend

Requires a JDK 17+ on your machine (no separate Maven install needed — the project uses the Maven wrapper).

```bash
cd backend
./mvnw spring-boot:run      # on Windows: mvnw.cmd spring-boot:run
```

Visit http://localhost:8080/api/health — you should see `{"status":"ok","service":"team-collaboration-platform-backend"}`.

This creates a local SQLite file at `backend/app.db` on first run (gitignored).

Try the auth flow:
```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"username":"johndoe","firstName":"John","lastName":"Doe","email":"john@example.com","password":"password123"}'

curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'

curl http://localhost:8080/api/users/me -H "Authorization: Bearer <token from login>"
```

## License

MIT — see [LICENSE](LICENSE). Copyright (c) 2026 Soufiane Ammari.
