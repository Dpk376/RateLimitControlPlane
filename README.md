# Rate Limit Control Plane & Dynamic Plugin Gateway

An extensible, production-grade API gateway platform built with Java and Spring Boot. It provides per-tenant distributed rate limiting, real-time observability, and a runtime-loaded plugin architecture that allows third-party integrations to be deployed without core service redeploys.

## Features

- **Dynamic Plugin Registry**: Hot-loads `.jar` extensions at runtime using an isolated `URLClassLoader` and Java `ServiceLoader`.
- **Pre & Post Request Hooks**: Plugins can mutate headers, track latency, inject correlation IDs, or short-circuit requests.
- **Distributed Rate Limiting**: Uses a Redis-backed Token Bucket algorithm (via atomic Lua scripts) to enforce per-tenant quotas.
- **Admin Control Plane**: A REST API to onboard new tenants, configure rate limit quotas, and toggle specific plugins on/off per tenant.
- **Observability Built-In**: Exposes Micrometer/Prometheus metrics for request counts, rate-limit rejections, and plugin execution latency.

---

## High-Level Architecture

```text
Client 
  │
  ▼
[ GatewayFilter ]
  │
  ├── 1. Rate Limit Check (Redis Token Bucket)
  │
  ├── 2. Pre-Request Plugin Hooks (Loaded dynamically)
  │
  ├── 3. Core Service Logic
  │
  └── 4. Post-Response Plugin Hooks
```

The system consists of three main modules:
1. `plugin-sdk`: The contract (`ApiGatewayPlugin` and `RequestContext`) that third-party developers compile against.
2. `gateway-core`: The main Spring Boot edge service containing the rate limiter, dynamic class loader, and admin API.
3. `example-plugins`: Contains `plugin-logging` and `plugin-transform` modules demonstrating the extensibility.

---

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Docker & Docker Compose** (for PostgreSQL and Redis)

---

## Getting Started

### 1. Start Infrastructure Dependencies
The gateway relies on PostgreSQL (for tenant configuration) and Redis (for distributed rate limiting).

```bash
docker-compose up -d
```

### 2. Build the Project
Compile the core gateway and the example plugins.

```bash
mvn clean install
```

### 3. Start the Gateway
```bash
cd gateway-core
mvn spring-boot:run
```
The gateway will start on `http://localhost:8080`.

---

## Working with Plugins

The gateway monitors the `gateway-core/plugins/` directory for new `.jar` files and loads them instantly.

### Deploying a Plugin
While the gateway is running, copy the compiled example plugins into the watched directory:

```bash
mkdir -p gateway-core/plugins
cp plugin-logging/target/plugin-logging-1.0.0-SNAPSHOT.jar gateway-core/plugins/
cp plugin-transform/target/plugin-transform-1.0.0-SNAPSHOT.jar gateway-core/plugins/
```
Watch the gateway console logs. You will see the `FileAlterationMonitor` detect and register the plugins instantly.

---

## Admin API Usage

The Admin API is used to manage tenants and their plugin configurations. It is mounted at `/api/admin/tenants`.

### 1. Onboard a Tenant
```bash
curl -X POST http://localhost:8080/api/admin/tenants \
-H "Content-Type: application/json" \
-d '{
  "apiKey": "my-secret-key",
  "rateLimitQuota": 5,
  "rateLimitReplenishRate": 1
}'
```
*Note the `id` (UUID) returned in the response.*

### 2. Enable a Plugin for the Tenant
Using the `id` from the previous step, enable the logging and transform plugins:

```bash
curl -X POST http://localhost:8080/api/admin/tenants/<TENANT_UUID>/plugins/plugin-logging \
-H "Content-Type: application/json" \
-d '{
  "enabled": true,
  "configJson": "{}"
}'

curl -X POST http://localhost:8080/api/admin/tenants/<TENANT_UUID>/plugins/plugin-transform \
-H "Content-Type: application/json" \
-d '{
  "enabled": true,
  "configJson": "{}"
}'
```

### 3. Update Rate Limits Dynamically
```bash
curl -X PUT http://localhost:8080/api/admin/tenants/<TENANT_UUID>/rate-limits \
-H "Content-Type: application/json" \
-d '{
  "rateLimitQuota": 100,
  "rateLimitReplenishRate": 10
}'
```

---

## Testing the Gateway

Once a tenant is onboarded, you can hit the mock edge endpoint `/api/service/data`.

```bash
curl -v -H "X-API-KEY: my-secret-key" http://localhost:8080/api/service/data
```

**Expected Behavior:**
1. If you exceed the quota, you will receive an `HTTP 429 Too Many Requests`.
2. Because the `plugin-transform` is enabled, your HTTP response will include the custom header: `X-Gateway-Processed-By: TransformPlugin v1.0.0`.
3. The console logs will show the `plugin-logging` intercepting the request and measuring the latency.

---

## Observability

The gateway exposes Prometheus-compatible metrics out of the box.

```bash
curl http://localhost:8080/actuator/prometheus
```

Search for the following custom metrics:
- `gateway_requests_total`: Counts successful requests mapped to specific tenants.
- `gateway_rate_limited_total`: Counts the number of times a tenant was blocked by the Token Bucket.
- `gateway_plugin_execution_time_seconds`: Measures the latency overhead introduced by each individual plugin.
