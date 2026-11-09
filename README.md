# Team Collaboration Platform

A full-stack team collaboration and project management platform (think a simplified Trello + Slack), built as a portfolio project.

[![CI](https://github.com/SoufianeAmm/team-collaboration-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/SoufianeAmm/team-collaboration-platform/actions/workflows/ci.yml)

**Author:** Soufiane Ammari
**Status: under active development — Phase 14 (CI) complete.**

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
- [x] Phase 14 — GitHub Actions CI
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

## Continuous Integration

Every push and pull request to `master` triggers a GitHub Actions workflow ([.github/workflows/ci.yml](.github/workflows/ci.yml)) that:

- builds and runs the backend's tests (`./mvnw clean verify`)
- lints, typechecks, and builds the frontend (`npm run lint`, `npm run build`)
- builds both Docker images, to catch any drift between the source and the Dockerfiles

No deployment step — this is CI only (build/test automation), not CD. The badge at the top of this README reflects whether the latest commit actually builds cleanly.

## Development notes

This project was built in close collaboration with Claude (Anthropic), used as a pair-programming partner throughout. I drove every product and architecture decision, learned the underlying concepts as the project progressed, and directed each phase; Claude handled implementation and explained the reasoning behind each piece along the way.

### What I learned

- **Layered backend architecture** — why controllers, services, and repositories are kept separate (HTTP handling vs. business logic vs. database access), and why that separation is what keeps one failing request from affecting any other
- **Stateless JWT authentication** — how a signed token replaces server-side sessions, and the security tradeoffs that come with it
- **Real-time features over WebSockets** — STOMP messaging for live chat, task board updates, and notifications, and why that's a separate model from normal request/response REST calls
- **Docker multi-stage builds** — why build tools (Maven, Node) and runtime environments are kept in separate stages, so the final image only ships what's actually needed to run
- **Reverse proxying** — how nginx routing `/api` and `/ws` to the backend collapses two containers into what the browser sees as a single origin, eliminating CORS entirely instead of configuring around it
- **Monolith vs. microservices** — the real tradeoff isn't "microservices are better," it's fault isolation and independent scaling vs. the operational complexity of coordinating many separately-deployed services over a network
- **SQLite's actual limits** — it's a genuinely solid database for low-to-moderate write concurrency on a single server; it's horizontal scaling (multiple backend instances) that requires moving to something like Postgres, not raw data size
- **Writing a clean git history** — organizing a large body of work into commits grouped by feature, instead of one dump, so the project's story is actually readable by someone skimming the log

## License

MIT — see [LICENSE](LICENSE). Copyright (c) 2026 Soufiane Ammari.
