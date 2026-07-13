# BookMyShow Clone (BMS-2)

A full-stack movie ticket booking platform inspired by BookMyShow — built with **Spring Boot**, **Spring Security (JWT)**, **Apache Kafka**, **Razorpay**, and **MySQL** on the backend, with a vanilla HTML/CSS/JavaScript frontend.

This project was built as a portfolio piece to demonstrate a real, end-to-end booking + payment + notification pipeline: browsing movies, selecting seats with concurrency-safe locking, paying through Razorpay, and receiving an automated confirmation email via a Kafka producer/consumer flow — all secured with role-based JWT authentication.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
    - [1. Clone the repository](#1-clone-the-repository)
    - [2. Set up MySQL](#2-set-up-mysql)
    - [3. Set up Kafka (via Docker)](#3-set-up-kafka-via-docker)
    - [4. Configure secrets](#4-configure-secrets)
    - [5. Run the backend](#5-run-the-backend)
    - [6. Run the frontend](#6-run-the-frontend)
    - [7. Log in](#7-log-in)
- [API Overview](#api-overview)
- [Known Limitations](#known-limitations)
- [License](#license)

---

## Features

**Authentication & Authorization**
- User registration and login with JWT-based authentication
- Passwords hashed with BCrypt (never stored or returned in plaintext)
- Role-based access control (`USER` / `ADMIN`) enforced at the API layer, not just the UI
- Distinct `401` (not authenticated) vs `403` (authenticated, not authorized) responses
- Ownership checks on personal data — a regular user can only view their own booking history; only an admin can view anyone's

**Browsing**
- Anonymous browsing of movies, theaters, cities, screens, and showtimes — no login required until booking
- Search and filter movies by genre/language
- Movie posters, ratings, and showtime listings

**Booking & Seat Selection**
- Live seat map per showtime (Available / Selected / Booked)
- **Pessimistic DB row-locking** on the `Show` during booking creation, so two people can't book the same seat in a race condition
- Bookings move through a `PENDING_PAYMENT → CONFIRMED` lifecycle — a seat is held during checkout but only finalized after payment succeeds
- A scheduled job automatically cancels abandoned `PENDING_PAYMENT` bookings after a configurable timeout, freeing the seat back up

**Payments**
- Razorpay integration for order creation and payment verification
- Signature verification on the backend before a booking is ever confirmed

**Notifications**
- Kafka producer fires a booking-confirmation event after successful payment verification
- A separate Kafka consumer picks up the event and sends a confirmation email via Gmail SMTP
- Configurable retry + error logging on the consumer side, so a failed email doesn't silently disappear

**Admin Panel**
- Full CRUD for movies (add/update/delete, including poster URL)
- Add/manage cities, theaters, screens, seats, and shows
- **Users tab**: view every registered user (name, email, phone, role, join date) and drill into any user's full booking history — passwords are never exposed by the API (bcrypt hashes are excluded from every response, not just hidden in the UI)
- Admin-only endpoints enforced server-side (`hasRole("ADMIN")`), not just hidden in the UI

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.7 |
| Security | Spring Security 7 + JWT (`jjwt` 0.12.6) |
| Persistence | Spring Data JPA / Hibernate + MySQL 8 |
| Messaging | Apache Kafka (Confluent images, via Docker Compose) |
| Payments | Razorpay Java SDK |
| Email | Spring Mail (Gmail SMTP) |
| Frontend | HTML, CSS, vanilla JavaScript (no framework/build step) |
| Build Tool | Maven |

---

## Project Structure

```
BMS-2/
├── src/main/java/com/cfs/BMS2/
│   ├── config/          # Security, CORS, Kafka consumer config
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/response DTOs
│   ├── entity/          # JPA entities
│   ├── enums/           # BookingStatus, Role, SeatType
│   ├── exception/        # Global exception handling
│   ├── filter/          # JWT authentication filter
│   ├── repository/      # Spring Data repositories
│   └── service/         # Business logic
├── src/main/resources/
│   └── application.properties
├── UI/                  # Frontend (plain HTML/CSS/JS, no build step)
│   ├── pages/
│   ├── js/
│   └── css/
├── BMS.sql              # Full schema + seed data
├── docker-compose.yml   # Kafka + Zookeeper
├── SetUpKafkaDocker.md  # Detailed Kafka/Docker command reference
└── pom.xml
```

---

## Prerequisites

Make sure you have these installed before starting:

- **Java 21** (JDK)
- **Maven** (or use the included `mvnw` wrapper)
- **MySQL 8**
- **Docker** (for running Kafka + Zookeeper)
- A **Razorpay** account (test mode is fine) for API keys
- A **Gmail account with an App Password** (for sending confirmation emails — a normal Gmail password will not work; you need 2-Step Verification enabled and an [App Password](https://myaccount.google.com/apppasswords) generated)

---

## Getting Started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd BMS-2
```

### 2. Set up MySQL

The full schema and seed data (cities, sample movies, theaters, screens, seats, shows, and a few test users) live in **`BMS.sql`** at the project root.

```bash
mysql -u root -p < BMS.sql
```

> **Note:** `BMS.sql` creates a database named `bms` (lowercase). `application.properties` connects to `BMS` (uppercase). On Windows/macOS this usually doesn't matter (MySQL is case-insensitive for database names there by default), but on Linux, MySQL database names *are* case-sensitive. If you're on Linux, either edit the first two lines of `BMS.sql` to use `BMS` instead of `bms`, or adjust `spring.datasource.url` in `application.properties` to match whichever case you used.

This seeds four test users (password `pass123` for all), with `rahul@example.com` pre-set as `ADMIN` and the rest as `USER` — log in as Rahul to access the Admin Panel out of the box.

### 3. Set up Kafka (via Docker)

Start Zookeeper and the Kafka broker:

```bash
docker-compose up
```

Once both containers are running, create the Kafka topic this app actually uses for booking notifications. Exec into the Kafka container and run:

```bash
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:19092 --create --topic show-booking-notification --replication-factor 1 --partitions 1
```

You can confirm it was created with:

```bash
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:19092 --list
```

> `show-booking-notification` is the topic name configured in `application.properties` (`app.kafka.topic`). If you rename it there, use the matching name in the `--topic` flag above.
>
> See **`SetUpKafkaDocker.md`** for a much larger reference of Kafka commands (producing/consuming messages manually, multi-broker setups, consumer groups, inspecting logs, etc.) if you want to explore or debug the Kafka side directly.

### 4. Configure secrets

`application.properties` reads several values from placeholders rather than hardcoding them, so real credentials never get committed to git:

```properties
spring.datasource.password=${mysql_pass}
app.razorpay.api.key-id=${razorpay_key_id}
app.razorpay.api.key-secret=${razorpay_key_secret}
spring.mail.username=${gmail_username}
spring.mail.password=${gmail_password}
```

You need to provide these five values as environment variables before running the app. In **IntelliJ IDEA**: open your Run/Debug Configuration for `Bms2Application` → Modify options → Environment Variables, and add:

| Name | Value |
|---|---|
| `mysql_pass` | your MySQL password |
| `razorpay_key_id` | your Razorpay test Key ID |
| `razorpay_key_secret` | your Razorpay test Key Secret |
| `gmail_username` | your Gmail address |
| `gmail_password` | your Gmail **App Password** (not your normal password) |

If you're running from the command line instead, set them as regular OS environment variables (`export mysql_pass=...` on macOS/Linux, `set mysql_pass=...` on Windows) before running `mvnw spring-boot:run`.

### 5. Run the backend

From IntelliJ, just run `Bms2Application`. From the command line:

```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

### 6. Run the frontend

The `UI/` folder is plain HTML/CSS/JS with no build step — serve it with any static file server:

- **IntelliJ**: right-click `UI/index.html` → "Open in Browser"
- **VS Code**: use the "Live Server" extension
- **Command line**: `cd UI && python -m http.server 5500`

The backend's CORS configuration already allows any `localhost`/`127.0.0.1` origin, so it doesn't matter which port your static server uses.

### 7. Log in

Use one of the seeded accounts (all passwords are `pass123`):

| Name | Email | Role | Pre-seeded booking |
|---|---|---|---|
| Rahul Sharma | `rahul@example.com` | ADMIN | Pushpa 2 — 15 Mar, 10:00 AM (Screen 1), seats A1–A2, ₹500 — **CONFIRMED** |
| Priya Patel | `priya@example.com` | USER | Jawan — 15 Mar, 11:00 AM (Screen 2), seat A1, ₹200 — **CONFIRMED** |
| Amit Kumar | `amit@example.com` | USER | Animal — 15 Mar, 6:00 PM (IMAX), seats A1–A2, ₹900 — **CONFIRMED** |
| Sneha Reddy | `sneha@example.com` | USER | Pushpa 2 — 15 Mar, 10:00 AM (Screen 1), seat A3, ₹250 — **CANCELLED** |

These aren't just filler rows — they're set up so **My Bookings** already has something to show the moment you log in, without needing to make a fresh booking first:
- Log in as **Rahul** to see both the Admin Panel (as ADMIN) and a confirmed booking in the same account.
- Log in as **Amit** to see what a multi-seat, premium-screen (IMAX) confirmed booking looks like.
- Log in as **Sneha** to see how a cancelled booking is displayed (and confirm that seat A3 on that same Screen 1 show is available again for someone else to book).

Or register a new account through the UI — new users default to `USER`. To promote any account to `ADMIN`, run:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
```
(then log out and back in, since the role is baked into the JWT at login time).

---

## API Overview

All endpoints are prefixed with `/api`.

| Group | Example endpoints | Auth required |
|---|---|---|
| Auth | `POST /auth/login` | No |
| Users | `POST /users/register` | No |
| Users | `GET /users`, `GET /users/{id}` | Yes — `ADMIN` only |
| Movies / Theaters / Cities / Screens / Seats / Shows | `GET` endpoints | No (public browsing) |
| Movies / Theaters / Cities / Screens / Seats / Shows | `POST` / `PUT` / `DELETE` | Yes — `ADMIN` only |
| Bookings | `POST /bookings` | Yes — any authenticated user |
| Bookings | `GET /bookings/user/{id}` | Yes — the booking owner, or an `ADMIN` |
| Payments | `POST /payments/orders`, `POST /payments/verify` | Yes |

---

## Known Limitations

Being upfront about a few tradeoffs and gaps, rather than hiding them:

- **No automated tests yet.** Everything has been verified through manual end-to-end testing.
- **Pessimistic locking on bookings** blocks concurrent requests for the same show rather than using optimistic retry — a reasonable tradeoff at this scale, but worth knowing if this ever needed to handle high concurrent load.
- **No cascading deletes.** Deleting a theater/screen that still has dependent screens/seats will throw a database constraint error rather than a friendly message.
- **Admin panel Edit forms** are complete for Movies; Theaters/Screens/Seats/Shows/Cities currently only support Add/Delete, not Edit, through the UI (the backend PUT endpoints exist and work — just not wired into the admin form yet for those five).
- **Kafka runs locally via Docker Compose** in this setup. For a real deployment, this would need to point at a managed Kafka provider (or Kafka self-hosted on a VPS) instead of `localhost:9092`.

---

## License

This project was built for educational/portfolio purposes.