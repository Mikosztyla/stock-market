# Stock Market — High-Availability Setup

## Architecture

```
Client
  │
  ▼
Nginx (load balancer, round-robin)
  ├── app1 (Spring Boot :8080)
  ├── app2 (Spring Boot :8080)
  └── app3 (Spring Boot :8080)
          │
          ▼
     PostgreSQL 16
```

All three application instances share a single PostgreSQL database, so state is consistent regardless of which instance serves the request. When one instance is killed via `POST /chaos`, Nginx automatically retries the request on a healthy instance (`proxy_next_upstream`), making the outage invisible to the caller.

## Prerequisites

- Docker ≥ 24 with the Compose plugin (`docker compose`)  
  Works on Linux, macOS (Intel & Apple Silicon), and Windows (WSL 2 / Docker Desktop).

## Starting the stack

```bash
# PORT is the host port Nginx will listen on
PORT=9000 docker compose up --build
```

All endpoints are then available at `http://localhost:9000`.

If `PORT` is omitted it defaults to **8080**.

## Stopping

```bash
docker compose down          # keep the database volume
docker compose down -v       # also delete the database volume (full reset)
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/wallets/{wallet_id}/stocks/{stock_name}` | Buy or sell one unit of a stock |
| `GET`  | `/wallets/{wallet_id}` | Get wallet contents |
| `GET`  | `/wallets/{wallet_id}/stocks/{stock_name}` | Get quantity of one stock in wallet |
| `GET`  | `/stocks` | Get bank state |
| `POST` | `/stocks` | Set bank state |
| `GET`  | `/log` | Get audit log |
| `POST` | `/chaos` | Kill the serving instance |

### Example workflow

```bash
# 1. Seed the bank with 10 units of AAPL
curl -X POST http://localhost:9000/stocks \
     -H 'Content-Type: application/json' \
     -d '{"stocks":[{"name":"AAPL","quantity":10}]}'

# 2. Buy 1 AAPL into wallet "alice"
curl -X POST http://localhost:9000/wallets/alice/stocks/AAPL \
     -H 'Content-Type: application/json' \
     -d '{"type":"buy"}'

# 3. Check alice's wallet
curl http://localhost:9000/wallets/alice

# 4. Check bank (should show 9 AAPL)
curl http://localhost:9000/stocks

# 5. Kill one instance — the next request is served by another node
curl -X POST http://localhost:9000/chaos
curl http://localhost:9000/stocks   # still works
```

## Project layout

```
.
├── Dockerfile                   # Multi-stage build (Maven → JRE-alpine)
├── docker-compose.yml           # Nginx + 3 app instances + PostgreSQL
├── nginx.conf                   # Nginx upstream / proxy config
├── src/
│   └── main/
│       ├── java/remitly/task/stockmarket/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── model/
│       │   ├── dto/
│       │   └── exceptions/
│       └── resources/
│           └── application.properties
└── pom.xml
```

## High-availability notes

- **Shared state** — PostgreSQL is the single source of truth. All instances read and write to it, so no cache invalidation or replication is needed at the application level.
- **Retry on failure** — Nginx is configured with `proxy_next_upstream error timeout http_502 http_503 http_504` and `proxy_next_upstream_tries 3`, so a `POST /chaos` is transparently retried on one of the remaining two instances.
- **Data persistence** — The `pgdata` Docker volume survives `docker compose down`, so stock and wallet state is preserved across restarts.
- **Schema management** — `spring.jpa.hibernate.ddl-auto=update` is safe for multiple identical instances starting concurrently because they all apply the same idempotent DDL.