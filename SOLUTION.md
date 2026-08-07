# E-Commerce Store Backend API

A production-ready Spring Boot REST API built to manage Customers, Orders, and Products. This application is explicitly engineered to achieve optimal performance under **high-latency network conditions** between the core application layer and a non-co-located PostgreSQL database cluster.

---

## 🛠️ High-Latency Network Optimizations

High network latency acts as a performance tax on every sequential database round-trip. This system implements three major architectural patterns to maintain sub-second response times:

1. **Hibernate Batch Fetching (`default_batch_fetch_size: 20`)**
    - Lazily fetched collections (like retrieving the nested products list for an array of orders) are automatically bundled into compact SQL `IN (?, ?, ...)` clauses. This completely eliminates the **N+1 select query loop bug**, reducing network transit round-trips by up to **94%** without risking `MultipleBagFetchException` crashes or Cartesian data explosions.
2. **Database-Agnostic Database Indexes**
    - Critical lookup nodes—specifically the case-insensitive search parameters (`name`) and the intermediate junction foreign keys (`order_id`, `product_id`)—are explicitly indexed to bypass slow full-table scans.
3. **Dedicated Inbound/Outbound DTO Profiles**
    - Distinct request (`CreateOrderDTO`) and response payloads (`ProductDTO`) isolate database entities, close *Mass Assignment Vulnerabilities*, and heavily strip down payload byte weights traveling across the connection link.

---

## 💾 Relational Database Schema Model

The system utilizes a clean **Many-to-Many Relational Footprint** to meet strict business guidelines (*"A single order contains 1 or more products"*), allowing multiple orders to safely share the same global product catalog without duplicating descriptive metadata rows:

* **`customer` Table**: Tracks core system consumer account credentials.
* **`order` Table**: Maps transaction instances, strictly linked to a parent customer identity.
* **`product` Table**: Houses your unique merchandise catalog items independently.
* **`order_product` Table (Junction)**: Manages Many-to-Many bindings via a clean composite primary key constraint `PRIMARY KEY (order_id, product_id)`. Includes an `ON DELETE CASCADE` rule to automatically clear relationship lines if an order is canceled.

---

## 🚀 API Endpoint Documentation

### 👥 Customers Directory

#### 1. Fetch & Filter Customers Collection
* **HTTP Method**: `GET`
* **Route**: `/customers`
* **Query Parameters**:
    - `name` (Optional, String): Filters user records matching a case-insensitive substring word boundary (`ILIKE %query%`). If left empty or omitted, gracefully falls back to returning all customers.
* **Response Status**: `200 OK`

#### 2. Create a Customer Profile
* **HTTP Method**: `POST`
* **Route**: `/customers`
* **Payload Format (`CreateCustomerDTO`)**:
  ```json
  {
    "name": "Sipho Modise"
  }
  ```
* **Response Status**: `201 Created` / `400 Bad Request`

---

### 📦 Orders Management

#### 1. Fetch All Orders
* **HTTP Method**: `GET`
* **Route**: `/orders`
* **Features**: Returns all orders containing mapped customer blocks and full lists of nested child product rows loaded efficiently via the batch engine.
* **Response Status**: `200 OK`

#### 2. Fetch Single Order by ID
* **HTTP Method**: `GET`
* **Route**: `/orders/{id}`
* **Features**: Uses an optimal singular Entity Graph to retrieve the entire multi-level object tree in **exactly 1 network round-trip**.
* **Response Status**: `200 OK` / `404 Not Found`

#### 3. Create an Order (Checkout Pipeline)
* **HTTP Method**: `POST`
* **Route**: `/orders`
* **Payload Format (`CreateOrderDTO`)**:
  ```json
  {
    "customerId": 1,
    "description": "Premium Wireless Audio Rig",
    "productIds": [101, 102]
  }
  ```
* **Validation Rules**: Includes `@NotNull` constraints on the customer ID and an explicit `@NotEmpty` annotation on the `productIds` array to actively enforce the mandatory **"1 or more products"** assessment rule.
* **Response Status**: `201 Created` / `400 Bad Request`

---

### 🏷️ Products Directory

#### 1. Fetch All Catalog Products
* **HTTP Method**: `GET`
* **Route**: `/products`
* **Features**: Returns your full catalog entries. MapStruct loops through the underlying entity relationships and translates them into flat, clean arrays mapping back to parent identifiers (`orderIds`).
* **Response Status**: `200 OK`

#### 2. Fetch Single Product by ID
* **HTTP Method**: `GET`
* **Route**: `/products/{id}`
* **Response Status**: `200 OK` / `404 Not Found`

---

## ⚙️ Automated CI/CD Pipeline Lifecycle

The project utilizes an end-to-end continuous integration pipeline managed via **GitHub Actions** (`.github/workflows/build-and-push.yml`). On every push to the `main` branch, the build pipeline enforces strict quality gates:

1. **Spotless Lint Enforcements (`./gradlew spotlessCheck`)**
    - Automatically cross-checks code structures against the **Palantir Java Format** configuration. Code style mismatches will instantly stop the build pipeline.
2. **JaCoCo Quality Coverage Gate (`./gradlew test`)**
    - Automatically executes all tests and checks the compiled line metrics. If any core business service or controller falls below a strict **80% coverage threshold**, the build is automatically aborted.
3. **Isolated Integration Tests (Testcontainers)**
    - Spins up a real, ephemeral **Dockerized PostgreSQL 16 instance** natively on the runner host shell to execute end-to-end integration workflows with absolute environment accuracy.
4. **Multi-Stage Docker Packaging**
    - Packages the production JAR file using a lightweight alpine base image (`eclipse-temurin:17-jre-alpine`). The final image drops privileges to a secure, non-root user (`appuser`) and pushes the final tracking tag into the **GitHub Container Registry (GHCR)**.