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
````````
curl -X POST http://localhost:8080/products \
-H 'Content-Type: application/json' \
-d '[
{
"name": "Mechanical Keyboard",
"category": "ELECTRONICS",
"price": 150.00,
"stock": 50
},
{
"name": "Gaming Mousepad",
"category": "ACCESSORIES",
"price": 25.00,
"stock": 100
}
]'
`````````
2. Promotion Creation

Define and activate promotion rules.(Note: The productId should be the ID returned from the product creation step).
Endpoint: `POST /promotions` Example Request: (10% off category ELECTRONICS and a B2G1 Free deal)
``````````
curl -X POST http://localhost:8080/promotions \
   -H 'Content-Type: application/json' \
   -d '[
   {
   "type": "PERCENT_OFF_CATEGORY",
   "category": "ELECTRONICS",
   "discountPercentage": 10
   },
   {
   "type": "BUY_X_GET_Y",
   "productId": "keyboard-uuid-or-id",
   "buyCount": 2,
   "getFreeCount": 1
   }
   ]'
 ``````````
3. Cart Quote Calculation

Calculate the final price and itemized breakdown before reservation.
Endpoint: POST /cart/quote Example Request: (Requesting 3 keyboards and 1 mousepad)
``````
curl -X POST http://localhost:8080/cart/quote \
   -H 'Content-Type: application/json' \
   -d '{
   "items": [
   { "productId": "keyboard-uuid-or-id", "qty": 3 },
   { "productId": "mousepad-uuid-or-id", "qty": 1 }
   ],
   "customerSegment": "REGULAR"
   }'
   Example Response Snippet (Expected):{
   "totalOriginalPrice": 475.00,
   "totalDiscountApplied": 55.00,
   "finalPrice": 420.00,
   "itemBreakdown": [
   {
   "productId": "keyboard-uuid-or-id",
   "quantity": 3,
   "appliedPromotions": ["PERCENT_OFF_CATEGORY", "BUY_X_GET_Y"],
   "finalLinePrice": 270.00
   },
   // ...
   ]
   }
 `````````
4. Cart Confirmation and Reservation

Validates stock, reserves inventory (atomically decrements stock), and generates an order.
Endpoint: POST /cart/confirmExample Request: (Same payload as quote, with required Idempotency Key)
``````
curl -X POST http://localhost:8080/cart/confirm \
   -H 'Content-Type: application/json' \
   -H 'Idempotency-Key: 7b8c-a1d2-e3f4-56g7' \
   -d '{
   "items": [
   { "productId": "keyboard-uuid-or-id", "qty": 3 },
   { "productId": "mousepad-uuid-or-id", "qty": 1 }
   ],
   "customerSegment": "REGULAR"
   }'
   Example Response:{
   "orderId": "ORD-2025-ABCD-1234",
   "finalPrice": 420.00,
   "message": "Order confirmed and stock reserved."
   }
 ``````
Error Conditions
* Stock Exhaustion: If stock runs out during confirmation, the service returns a 409 CONFLICT status code.
* Idempotency Failure: If the same Idempotency-Key is reused for a different cart payload, an appropriate error is returned.
* Price Validation: Input validation ensures prices and totals do not become negative; correct rounding (e.g., HALF_UP to cents) is used.