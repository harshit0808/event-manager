# Full-Stack Event Manager

A full-stack event management application: a **Spring Boot** REST API with JWT security and a **React** frontend.
Attendees browse events and register for them; organizers create, edit and delete events, see who registered,
and check attendees in at the door by scanning their QR ticket.

## Features
* **Authentication & roles:** register/login with Spring Security + JWT. Two roles: `USER` (attendee) and `ORGANIZER`.
  Organizer sign-up is protected by a secret **invite code**, so nobody can promote themselves to organizer.
* **Events:** public event list and details; organizers can create, **edit** and **delete** their own events
  (organizers can only change events they own).
* **Registration:** one registration per user per event. Spots are taken with an **atomic database update**, so
  concurrent registrations can never oversell an event.
* **Ticketing:** every registration gets a random, unguessable ticket code, shown as a QR code on the *My Tickets*
  page and emailed to the attendee (sent only after the registration is committed).
* **Check-in:** organizers check attendees in with their ticket code; a ticket can only be used once.
* **Consistent errors:** every error is JSON `{ "message": "..." }` with a proper status
  (400 validation, 401 not logged in, 403 not allowed, 404 not found, 409 conflict).

## Tech stack
* **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA/Hibernate, PostgreSQL, ZXing, Spring Mail
* **Frontend:** React 19, Vite, React Router, Axios, `qrcode.react`
* **Tests/CI:** JUnit 5 + MockMvc on in-memory H2, GitHub Actions

## Running locally

### 1. Database
Create a PostgreSQL database (default name `eventmanager`). Tables are created automatically.

### 2. Backend (`event-manager`)
Configure these **environment variables** (e.g. in your IntelliJ run configuration):

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `JWT_SECRET_KEY` | **yes** | – | Base64 secret, at least 32 bytes. Generate: `openssl rand -base64 32` |
| `DB_PW` | usually | *(empty)* | PostgreSQL password |
| `DB_USER` | no | `postgres` | PostgreSQL user |
| `DATABASE` | no | `eventmanager` | Database name |
| `DB_HOST` / `DB_PORT` | no | `localhost` / `5432` | Database location |
| `ORGANIZER_INVITE_CODE` | to create organizers | *(empty = organizer sign-up disabled)* | Secret code needed to register as organizer |
| `MAILTRAP_USER` / `MAILTRAP_PASSWORD` | no | *(empty)* | SMTP credentials (Mailtrap sandbox). Without them the app works but emails are not delivered |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:5173,http://localhost:3000` | Comma-separated frontend origins allowed to call the API |
| `PORT` | no | `8080` | Server port |

Then run `EventManagerApplication` or:

```bash
cd event-manager
mvn spring-boot:run
```

### 3. Frontend (`event-manager-ui`)
```bash
cd event-manager-ui
cp .env.example .env     # set VITE_API_URL if the backend is not on http://localhost:8080
npm install
npm run dev              # http://localhost:5173
```

## Tests
```bash
cd event-manager
mvn test        # no PostgreSQL or SMTP needed (uses in-memory H2)

cd ../event-manager-ui
npm run lint && npm run build
```

## API overview
| Method & path | Access | Description |
|---|---|---|
| `POST /auth/register` | public | Register (`organizerCode` required for `ROLE_ORGANIZER`) |
| `POST /auth/login` | public | Returns a JWT |
| `GET /api/events`, `GET /api/events/{id}` | public | Browse events |
| `POST /api/events` | organizer | Create an event |
| `PUT /api/events/{id}` | organizer (owner) | Edit an event |
| `DELETE /api/events/{id}` | organizer (owner) | Delete an event and its registrations |
| `POST /api/events/{id}/register` | logged in | Register for an event |
| `GET /api/my-registrations` | logged in | My tickets |
| `GET /api/organizer/my-events` | organizer | Events I organize |
| `GET /api/organizer/my-events/{id}/attendees` | organizer (owner) | Attendee list with check-in status |
| `POST /api/organizer/my-events/{id}/check-in` | organizer (owner) | Check in a ticket `{ "qrCodeData": "..." }` |

## Deployment notes
* Deploy the backend (e.g. Render) with the environment variables above, and set `CORS_ALLOWED_ORIGINS` to your frontend URL.
* Build the frontend with `VITE_API_URL` pointing to the deployed backend.
* The JWT is stored in `localStorage` for simplicity; for a production app consider an HttpOnly cookie instead.
