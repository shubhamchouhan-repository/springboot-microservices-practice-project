# Spring Boot Microservices: Eureka + API Gateway + User & Order Services

| App | Port | Eureka name | Database |
|---|---|---|---|
| eureka-server | 8761 | - | - |
| api-gateway | 8080 | API-GATEWAY | - |
| user-service | 8081 | USER-SERVICE | `user_service_db` |
| order-service | 8082 | ORDER-SERVICE | `order_service_db` |

**Versions:** Java 21, Spring Boot **3.4.5**, Spring Cloud **2024.0.1** (the release train that matches Boot 3.4.x).
If you change Boot, change Spring Cloud with it: https://spring.io/projects/spring-cloud#overview

## Architecture

```
                         +--------------------+
                         |   Eureka Server    |  8761  (registry / "phone book")
                         +--------------------+
                          ^   ^            ^   ^
              register    |   | lookup     |   | register
         +----------------+   |            |   +----------------+
         |                    |            |                    |
 Client -> API Gateway (8080) +            +-- Order Service (8082) --REST via USER-SERVICE--> User Service (8081)
 (Postman)   /api/users/**  -> lb://USER-SERVICE                |                                 |
             /api/orders/** -> lb://ORDER-SERVICE               v                                 v
                                                         order_service_db                   user_service_db
```

Request flow: `Postman -> Gateway(8080) -> asks Eureka where USER-SERVICE is -> forwards to 8081 -> response back`.
Eureka is only a registry; the **gateway** forwards the traffic.

## Prerequisites
- JDK 21, Maven 3.9+ (or let IntelliJ import each `pom.xml`), MySQL 8 running on localhost:3306
- Environment variables (set them in IntelliJ Run Configuration for user-service and order-service):

| Variable | Default |
|---|---|
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | *(empty)* - **set this to your MySQL password** |
| `DB_HOST` / `DB_PORT` | `localhost` / `3306` |

Databases are created automatically (`createDatabaseIfNotExist=true`).

## Run order
1. `eureka-server` -> open http://localhost:8761
2. `user-service`
3. `order-service`
4. `api-gateway`

Each: `cd <folder> && mvn spring-boot:run` (or run the `*Application` class in IntelliJ).
Wait ~30 s; the Eureka dashboard should list **API-GATEWAY, USER-SERVICE, ORDER-SERVICE** as UP.

## Import into Postman
Import `postman/Microservices.postman_collection.json`. Run requests in order (create user first).

## API checklist (always through the gateway `http://localhost:8080`)

| Method | URL | Body | Success |
|---|---|---|---|
| POST | /api/users | `{"name":"Asha Rao","email":"asha@example.com","phoneNumber":"9876543210"}` | 201 |
| GET | /api/users | - | 200 |
| GET | /api/users/1 | - | 200 (404 if missing) |
| PUT | /api/users/1 | same as POST | 200 |
| DELETE | /api/users/1 | - | 204 |
| POST | /api/orders | `{"userId":1,"productName":"Laptop","quantity":2,"price":499.99}` | 201 (`totalAmount` = 999.98) |
| GET | /api/orders | - | 200 |
| GET | /api/orders/1 | - | 200 |
| GET | /api/orders/user/1 | - | 200 |
| PUT | /api/orders/1 | `{"userId":1,"productName":"Laptop","quantity":3,"price":499.99,"orderStatus":"CONFIRMED"}` | 200 |
| DELETE | /api/orders/1 | - | 204 |

Header for bodies: `Content-Type: application/json`.
Direct (development only): same paths on `:8081` (users) and `:8082` (orders). Clients should only use `:8080`.

Error examples: validation -> 400 with `validationErrors`; unknown id -> 404; duplicate email -> 409;
order for a non-existent user -> 404 `User not found`; User Service down -> 503.

## Service-to-service call
`OrderServiceImpl.createOrder` -> `UserClient` -> `GET http://USER-SERVICE/api/users/{id}` using a
`@LoadBalanced RestClient.Builder`. This call goes **directly** between services (not via the gateway).
Test: create an order with `userId` 9999 -> 404; stop user-service and create an order -> 503.

## Gateway: explicit routes vs discovery locator
Explicit routes (used here) expose only the paths you choose (`/api/users/**`, `/api/orders/**`).
`spring.cloud.gateway.discovery.locator.enabled=true` would auto-expose every registered service under
`/SERVICE-NAME/**`, which is convenient for demos but exposes more than you intend. The Eureka dashboard
stays on 8761, separate from the business routes.

## Troubleshooting
- **404 via gateway:** path doesn't match a route predicate, or the service isn't implementing that path. Try the direct port first.
- **503 from gateway ("Unable to find instance"):** the service isn't registered yet (check dashboard), or the name in `lb://` is wrong. Wait ~30 s after startup.
- **Connection refused:** target app not running or wrong port; check `netstat`/`lsof -i :8081`.
- **Not registering with Eureka:** check `eureka.client.service-url.defaultZone` and that Eureka started first.
- **Port already in use:** stop the old process or change `server.port` in that service's `application.yml`.
- **MySQL access denied:** set `DB_USERNAME` / `DB_PASSWORD`.
- **Lombok errors in IntelliJ:** enable *Settings > Build > Compiler > Annotation Processors > Enable annotation processing*.
- **Which service handled a request:** every log line is tagged `[USER-SERVICE:8081]`, `[ORDER-SERVICE:8082]`, `[API-GATEWAY]`.



### Docker commands to open DB in terminal
    docker exec -it postgres-user psql -U postgres -d test_microservice_demo_user_db
    
    docker exec -it postgres-order psql -U postgres -d test_microservice_demo_order_db
