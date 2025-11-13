# PromoPricer — Cart Pricing & Reservation Microservice

This microservice handles cart pricing calculation, applies pluggable promotion rules, and atomically reserves inventory upon order confirmation.

---

## Technology Stack

- **Language:** Java 17+
- **Framework:** Spring Boot 3.x
- **Database:** H2 (In-memory for local runtime)
- **Dependencies:** Spring Web, Spring Data JPA, Jakarta Validation

---

## How to Run

The application is built using Maven and can be started directly via the Spring Boot plugin.

### Prerequisites

- Java Development Kit (JDK) 17 or newer.

### Steps

1. Clone the repository:

```bash
git clone https://github.com/habtamuworkugselassie/promo-pricer.git
cd promo-pricer
`````

2. Build and Run:
```bash
./gradlew bootRun
`````
The service will start on http://localhost:8080.

## Design Assumptions & Decisions

The architecture follows the requirements focusing on clean design and testability:
1. Rules Engine: Promotions are implemented using the Strategy pattern for rule encapsulation and the Chain of Responsibility/Pipeline pattern to ensure promotions are applied in a defined, sequential order (e.g., category-based discounts before BxGy rules).
2. Data Persistence: An in-memory H2 database is used for the prototype, storing Product and Promotion definitions, as well as managing Cart states and Order confirmations.
3. Concurrency Control: The POST /cart/confirm endpoint uses Optimistic Locking (via a version field in the Cart entity) to ensure thread safety and prevent double-reservation of stock under concurrent calls.
4. Idempotency: An Idempotency-Key header in the POST /cart/confirm request is used to ensure repeated confirmation requests only result in a single stock reservation and order creation.

## API Endpoints
The following are the core endpoints with example curl commands.
1. Product Creation
Define initial products.
Endpoint: `POST /products` Example Request:

product #1
````````
curl --location 'http://localhost:8080/products' \
--header 'Content-Type: application/json' \
--data '{
    "id": "df9de2a2-0e6d-409b-9dd5-3d8a73aea6c4",
    "name": "Full Chicken",
    "category": "FOOD",
    "price": 1500.00,
    "stock": 100
}'
`````````
product #2
```agsl
curl --location 'http://localhost:8080/products' \
--header 'Content-Type: application/json' \
--data '{
    "id": "5b86c458-121d-47ef-8114-7b1608cd47e7",
    "name": "Mechanical Keyboard",
    "category": "ELECTRONICS",
    "price": 150.00,
    "stock": 50
}'
```
2. Promotion Creation

Define and activate promotion rules.(Note: The productId should be the ID returned from the product creation step).
Endpoint: `POST /promotions` Example Request: (10% off category ELECTRONICS and a B2G1 Free deal)

promotion #1
``````````
curl --location 'http://localhost:8080/promotions' \
--header 'Content-Type: application/json' \
--data '{
  "name": "B3G1 Free Chicken Promotion",
  "type": "BUY_X_GET_Y",
  "config": "{\"productId\": \"df9de2a2-0e6d-409b-9dd5-3d8a73aea6c4\", \"buy\": 5, \"get\": 1}",
  "targetSegments": "REGULAR, PREMIUM"
}'
 ``````````
promotion #2

````agsl
curl --location 'http://localhost:8080/promotions' \
--header 'Content-Type: application/json' \
--data '{
  "name": "B3G1 Free Keyboard Promotion",
  "type": "BUY_X_GET_Y",
  "config": "{\"productId\": \"5b86c458-121d-47ef-8114-7b1608cd47e7\", \"buy\": 3, \"get\": 1}",
  "targetSegments": "REGULAR, PREMIUM"
}'
````
promotion #3
```agsl
curl --location 'http://localhost:8080/promotions' \
--header 'Content-Type: application/json' \
--data '{
  "name": "Spring Premium 10% Off Electronics",
  "type": "PERCENT_OFF_CATEGORY",
  "config": "{\"category\": \"ELECTRONICS\", \"percentage\": 10}",
  "targetSegments": "REGULAR"
}'
```
3. Cart Quote Calculation

Calculate the final price and itemized breakdown before reservation.
Endpoint: POST /cart/quote Example Request: (Requesting 3 keyboards and 1 chicken)
``````
curl --location 'http://localhost:8080/cart/quote' \
--header 'Content-Type: application/json' \
--data '{
    "items": [
        {
            "productId": "5b86c458-121d-47ef-8114-7b1608cd47e7",
            "qty": 3
        },
        {
            "productId": "df9de2a2-0e6d-409b-9dd5-3d8a73aea6c4",
            "qty": 5
        }
    ],
    "customerSegment": "REGULAR"
}'
 `````````
4. Cart Confirmation and Reservation

Validates stock, reserves inventory (atomically decrements stock), and generates an order.
Endpoint: POST /cart/confirmExample Request: (Same payload as quote, with required Idempotency Key)
``````
curl --location 'http://localhost:8080/cart/confirm' \
--header 'Idempotency-Key: 7b8c-a1d2-e3f4-56g7' \
--header 'Content-Type: application/json' \
--data '{
    "items": [
        {
            "productId": "5b86c458-121d-47ef-8114-7b1608cd47e7",
            "qty": 3
        },
        {
            "productId": "df9de2a2-0e6d-409b-9dd5-3d8a73aea6c4",
            "qty": 5
        }
    ],
    "customerSegment": "REGULAR"
}'
 ``````

Error Conditions
* Stock Exhaustion: If stock runs out during confirmation, the service returns a 409 CONFLICT status code.
* Idempotency Failure: If the same Idempotency-Key is reused for a different cart payload, an appropriate error is returned.
* Price Validation: Input validation ensures prices and totals do not become negative; correct rounding (e.g., HALF_UP to cents) is used.