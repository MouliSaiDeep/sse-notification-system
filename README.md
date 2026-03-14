# SSE Notification System

A real-time notification service built with **Server-Sent Events (SSE)** and **Spring Boot**. It manages persistent connections, persists events in a PostgreSQL database for reliability, and implements event replay for clients that disconnect and reconnect.

---

## Architecture Overview

```
┌─────────────┐     POST /publish      ┌─────────────────────┐     JDBC      ┌──────────────┐
│   Publisher  │ ──────────────────────►│  Spring Boot App    │ ◄───────────► │  PostgreSQL  │
│   (Client)   │                        │                     │               │  (events,    │
└─────────────┘                        │  ┌───────────────┐  │               │  user_subs)  │
                                       │  │ Connection    │  │               └──────────────┘
┌─────────────┐  GET /stream (SSE)     │  │ Manager       │  │
│  Subscriber  │ ◄─────────────────────│  │ (in-memory)   │  │
│  (Client)    │  text/event-stream    │  └───────────────┘  │
└─────────────┘                        └─────────────────────┘
```

- **Backend:** Java 21, Spring Boot 4.x, Spring Data JPA
- **Database:** PostgreSQL 13 — stores `events` and `user_subscriptions`
- **Containerization:** Docker & Docker Compose
- **Connection Mgmt:** In-memory `ConcurrentHashMap` tracking active SSE emitters per channel
- **Heartbeat:** 30-second periodic `: heartbeat` comment to keep connections alive

---

## Setup Instructions

### Prerequisites

- [Docker](https://www.docker.com/) & Docker Compose

### Quick Start

```bash
docker-compose up --build
```

This single command will:
1. Build the Spring Boot application via a multi-stage Dockerfile
2. Start PostgreSQL and run the seed script (`seeds/init.sql`)
3. Start the app on port **8080** once the database is healthy

### Environment Variables

See [`.env.example`](.env.example) for all configuration:

| Variable       | Default                                    | Description              |
|----------------|--------------------------------------------|--------------------------|
| `DATABASE_URL` | `jdbc:postgresql://db:5432/eventsdb`       | JDBC connection string   |
| `PORT`         | `8080`                                     | Application port         |

---

## API Reference

### Health Check

```
GET /health
→ 200 {"status": "UP"}
```

---

### Subscribe to a Channel

```
POST /api/events/channels/subscribe
Content-Type: application/json

{"userId": 1, "channel": "alerts"}

→ 201 {"status": "subscribed", "userId": 1, "channel": "alerts"}
```

---

### Unsubscribe from a Channel

```
POST /api/events/channels/unsubscribe
Content-Type: application/json

{"userId": 1, "channel": "alerts"}

→ 200 {"status": "unsubscribed", "userId": 1, "channel": "alerts"}
```

---

### List User's Subscribed Channels

```
GET /api/events/channels?userId=1

→ 200 {"userId": 1, "channels": ["alerts", "notifications"]}
```

---

### Publish an Event

```
POST /api/events/publish
Content-Type: application/json

{"channel": "alerts", "eventType": "SYSTEM_ALERT", "payload": {"message": "hello"}}

→ 202 (Accepted, empty body)
```

The event is persisted to the database and broadcast to all active SSE subscribers of the channel.

---

### SSE Stream

```
GET /api/events/stream?userId=1&channels=alerts,notifications
Accept: text/event-stream
Last-Event-ID: <optional>
```

**Response headers:** `Content-Type: text/event-stream`, `Cache-Control: no-cache`, `Connection: keep-alive`

**Event format:**
```
id: 5
event: SYSTEM_ALERT
data: {"message":"hello"}
```

**Heartbeat** (every 30s): `: heartbeat`

**Replay:** If `Last-Event-ID` header is set, the server replays all missed events for the subscribed channels with `id > Last-Event-ID` before streaming live.

---

### Event History (Paginated)

```
GET /api/events/history?channel=alerts&limit=5&afterId=2

→ 200
{
  "events": [
    {"id": 3, "channel": "alerts", "eventType": "SYSTEM_ALERT", "payload": {...}, "createdAt": "..."},
    ...
  ]
}
```

| Param     | Required | Default | Description                        |
|-----------|----------|---------|-------------------------------------|
| `channel` | Yes      | —       | Channel to query                   |
| `afterId` | No       | —       | Return events with `id > afterId`  |
| `limit`   | No       | 50      | Max number of events to return     |

---

### Active Connections

```
GET /api/events/active-connections

→ 200 {"activeConnections": 2}
```

Returns the current number of active SSE client connections.

---

## Testing with curl

### 1. Health Check
```bash
curl http://localhost:8080/health
```

### 2. Subscribe a User to a Channel
```bash
curl -X POST http://localhost:8080/api/events/channels/subscribe \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "channel": "test-channel"}'
```

### 3. List a User's Subscriptions
```bash
curl "http://localhost:8080/api/events/channels?userId=1"
```

### 4. Connect to SSE Stream
```bash
curl -N "http://localhost:8080/api/events/stream?userId=1&channels=alerts"
```

### 5. Publish an Event (in another terminal)
```bash
curl -X POST http://localhost:8080/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"channel": "alerts", "eventType": "SYSTEM_ALERT", "payload": {"message": "Hello!"}}'
```

### 6. Test Event Replay (reconnect with Last-Event-ID)
```bash
curl -N -H "Last-Event-ID: 3" \
  "http://localhost:8080/api/events/stream?userId=1&channels=alerts"
```

### 7. Unsubscribe a User from a Channel
```bash
curl -X POST http://localhost:8080/api/events/channels/unsubscribe \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "channel": "test-channel"}'
```

### 8. Query Event History (with pagination)
```bash
# First page
curl "http://localhost:8080/api/events/history?channel=history-channel&limit=5"

# Next page (use afterId from last event of previous response)
curl "http://localhost:8080/api/events/history?channel=history-channel&afterId=7&limit=5"
```

### 9. Check Active Connections
```bash
curl http://localhost:8080/api/events/active-connections
```

---

## Database Schema

### `events`
| Column       | Type                       | Constraints               |
|-------------|----------------------------|---------------------------|
| `id`        | `BIGSERIAL`                | `PRIMARY KEY`             |
| `channel`   | `VARCHAR(255)`             | `NOT NULL`                |
| `event_type`| `VARCHAR(255)`             | `NOT NULL`                |
| `payload`   | `JSONB`                    | `NOT NULL`                |
| `created_at`| `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()`  |

Index: `idx_events_channel_id (channel, id)`

### `user_subscriptions`
| Column       | Type                       | Constraints               |
|-------------|----------------------------|---------------------------|
| `user_id`   | `INTEGER`                  | `NOT NULL`                |
| `channel`   | `VARCHAR(255)`             | `NOT NULL`                |
| `created_at`| `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()`  |

Composite PK: `(user_id, channel)`

---

## Seed Data

The `seeds/init.sql` script creates:
- **User 1** subscribed to `alerts`, `notifications`, `history-channel`
- **User 2** subscribed to `alerts`, `history-channel`
- 3 sample events in `alerts` and `notifications` channels
- 10 sequential events in `history-channel` (for pagination testing)
