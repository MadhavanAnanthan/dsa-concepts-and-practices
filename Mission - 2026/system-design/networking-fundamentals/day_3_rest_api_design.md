# Day 3 — REST & API Design

## 1. REST Basics

REST is a resource-oriented way of designing APIs using HTTP.

```text
URL
→ identifies the resource

HTTP Method
→ identifies the action
```

Example:

```http
GET    /users/100
POST   /users
PUT    /users/100
PATCH  /users/100
DELETE /users/100
```

### Prefer Nouns, Not Verbs

Prefer:

```text
/users
/orders
/products
```

Avoid:

```text
/getUser
/createOrder
/deleteProduct
```

Because the HTTP method already expresses the action.

### Statelessness

Each request should contain enough information to be processed independently.

```text
Request 1 → Server A
Request 2 → Server B
Request 3 → Server C
```

This helps horizontal scaling.

> Stateless does not mean the server stores no data. It means requests should not depend on hidden per-client state stored only on one server instance.

---

## 2. Resource Design

### Collection vs Individual Resource

```text
/users
→ collection

/users/100
→ one user
```

Create a new user:

```http
POST /users
```

`/users` represents the collection, and POST creates a new resource inside it.

Plural resource names are the common convention:

```http
GET  /users
POST /users

GET    /users/100
PUT    /users/100
DELETE /users/100
```

### Nested Resources

Useful when the relationship is meaningful.

```http
GET /users/100/orders
```

Avoid unnecessarily deep nesting.

Prefer:

```text
/orders/9001/items
```

over:

```text
/users/100/orders/9001/items/5/products/20/reviews
```

> Nest when the parent-child relationship adds useful context; do not mirror the entire database structure in URLs.

---

## 3. Filtering, Sorting & Search

### Filtering

```http
GET /products?category=electronics
GET /products?brand=samsung&minPrice=10000
```

### Sorting

```http
GET /products?sort=price&order=desc
```

### Search

```http
GET /products?q=iphone
```

### Mental Model

```text
Path
→ identifies resource

Query Parameters
→ filter, sort, search, paginate
```

---

## 4. Pagination

Never return huge datasets in one response.

Problems:

```text
High DB load
Large payload
High memory usage
High latency
Poor client experience
```

Two main approaches:

```text
Offset Pagination
Cursor Pagination
```

---

## 5. Offset Pagination

Example:

```http
GET /orders?limit=20&offset=0
GET /orders?limit=20&offset=20
GET /orders?limit=20&offset=40
```

Meaning:

```text
offset
→ how many records to skip

limit
→ how many records to return
```

Conceptually:

```text
Find ordered results
   ↓
Skip N records
   ↓
Return next K records
```

### Advantages

```text
Simple
Easy page-number navigation
Good for small/admin datasets
```

### Problems

Large offsets can become expensive.

```text
OFFSET 2,000,000
→ database still has to locate/skip a large number of rows
```

Changing data can also cause:

```text
Duplicates
Missing records
```

between pages.

---

## 6. Cursor Pagination

Cursor pagination means:

> Continue from the last ordered position already seen.

Example first response:

```json
{
  "data": [
    {"id": 105},
    {"id": 104},
    {"id": 103}
  ],
  "nextCursor": "103"
}
```

Next request:

```http
GET /orders?limit=20&cursor=103
```

Conceptually:

```sql
WHERE id < 103
ORDER BY id DESC
LIMIT 20
```

with a suitable index.

The server does not necessarily remember the previous page. The cursor usually contains or represents the last ordered position seen by the client.

---

## 7. Offset vs Cursor

### Offset

```text
Simple
Supports page numbers
Good for smaller/static datasets
```

But:

```text
Large offsets can be slow
Changing data can shift results
```

### Cursor

```text
Efficient for large datasets
Stable for frequently changing data
Good for infinite scroll
```

But:

```text
Harder to jump directly to page 500
Slightly more complex
```

### When to Use

```text
Admin table with 2,000 rows
→ Offset

Instagram / X / Facebook feed
→ Cursor

Notifications / Chat history
→ Cursor

Transaction / Activity feed
→ Cursor
```

### Feed Example

First page:

```text
A
B
C
```

New posts arrive:

```text
NEW1
NEW2
A
B
C
D
E
```

Offset page 2 may now repeat:

```text
B
C
D
```

Cursor says:

```text
Continue after C
```

so the next result is:

```text
D
E
...
```

A real cursor often uses:

```text
createdAt + unique ID
```

to maintain stable ordering.

### Memory Trick

```text
Offset
→ Give me page 2

Cursor
→ Continue from the last item I saw
```

---

## 8. API Versioning

APIs evolve, but existing clients should not break.

Example breaking change:

```text
Old:
name

New:
firstName + lastName
```

Two common approaches:

```text
URI Versioning
Header Versioning
```

---

## 9. URI Versioning

```http
GET /v1/users/100
GET /v2/users/100
```

### Advantages

```text
Very visible
Easy to debug
Easy to test
Easy to explain
```

### Disadvantage

```text
Version appears in URL
```

For system-design interviews, URI versioning is usually the simplest choice.

---

## 10. Header Versioning

The URL stays the same:

```http
GET /users/100
```

Version is sent in a header:

```http
X-API-Version: 2
```

Conceptually:

```text
Same URL
   ↓
Read version header
   ↓
Use V1 or V2 behavior
```

Another style:

```http
Accept: application/vnd.company.v2+json
```

### Comparison

```text
URI Versioning
→ version visible in URL

Header Versioning
→ same URL, version carried in headers
```

### Trade-Off

```text
URI
→ simpler and more obvious

Header
→ cleaner URL but less visible
```

---

## 11. Backward Compatibility

Do not create a new API version for every small change.

Often safe:

```text
Adding an optional field
```

Potentially breaking:

```text
Removing a field
Renaming a field
Changing a type
Changing response semantics
```

> Prefer backward-compatible changes; create a new version for breaking changes.

---

## 12. API Deprecation

Typical lifecycle:

```text
v1 running
   ↓
v2 released
   ↓
Announce v1 deprecation
   ↓
Give migration period
   ↓
Monitor v1 usage
   ↓
Retire v1
```

---

## 13. Error Design

Avoid vague errors:

```json
{
  "error": "Something went wrong"
}
```

Prefer structured errors:

```json
{
  "code": "INVALID_EMAIL",
  "message": "Email format is invalid",
  "field": "email",
  "requestId": "abc-123"
}
```

Useful fields:

```text
code
message
details
field / errors
requestId / correlationId
```

---

## 14. Validation Errors

```json
{
  "code": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    },
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ],
  "requestId": "req-123"
}
```

---

## 15. Status Code + Error Code

```text
400 → Bad Request
401 → Authentication problem
403 → Not allowed
404 → Resource not found
409 → Conflict
429 → Too many requests
500 → Internal server error
503 → Service unavailable
```

HTTP status gives the broad category.

Application error code gives the exact reason.

Example:

```text
409 Conflict
```

```json
{
  "code": "EMAIL_ALREADY_EXISTS"
}
```

### Mental Model

```text
HTTP Status
→ broad failure category

Application Error Code
→ exact business/system reason
```

---

## 16. Correlation / Request ID

Used to trace one request across multiple services.

```text
Client
 ↓
API Gateway
 ↓
Order Service
 ↓
Payment Service
 ↓
Notification Service
```

Use the same request ID:

```text
requestId = abc123
```

Then:

```text
Search logs by abc123
→ trace the full request
```

> Correlation ID links logs/events belonging to the same distributed request.

---

## 17. Idempotency

Idempotency means:

> Repeating the same operation produces the same intended final effect as doing it once.

Conceptually:

```text
GET
→ idempotent

PUT
→ idempotent

DELETE
→ idempotent

POST
→ generally not idempotent

PATCH
→ depends on operation
```

Example:

```http
DELETE /users/100
```

Calling it twice still leaves the same final state:

```text
User does not exist
```

---

## 18. Why Idempotency Matters

Duplicate requests can happen because of:

```text
Timeouts
Retries
Network failures
Client uncertainty
```

Example:

```text
Client
  ↓
POST payment ₹5,000
  ↓
Server charges successfully
  ↓
Response lost
```

Client retries.

Without protection:

```text
₹5,000 charged twice
```

---

## 19. Idempotency Key

Client sends a unique key:

```http
POST /payments
Idempotency-Key: payment-abc-123
```

### First Request

```text
Key not found
   ↓
Process request
   ↓
Store key + result
   ↓
Return result
```

### Retry

```text
Same key received
   ↓
Key already exists
   ↓
Do NOT process again
   ↓
Return previous result
```

### Flow

```text
Client
  ↓
POST + Idempotency-Key
  ↓
Server
  ↓
Check idempotency store
  ↓
Key exists?
 ├── No  → Process → Store Result → Respond
 └── Yes → Return Existing Result
```

Useful for:

```text
Payments
Order creation
Bookings
Ticket purchases
Other retry-sensitive create operations
```

---

## 20. Where Idempotency Keys Are Stored

Possible stores:

```text
Database
Redis
Dedicated idempotency store
```

Typical stored information:

```text
Key
Request fingerprint/hash
Processing status
Response/result
Expiry time
```

Keys do not usually need to live forever.

---

## 21. Important Idempotency Detail

If the same key is reused for a different request, do not blindly return the previous result.

Store/compare:

```text
Idempotency Key
+
Request Fingerprint
```

and reject mismatched reuse.

---

## 22. Complete API Design Example

### Create Order

```http
POST /v1/orders
Idempotency-Key: abc123
Content-Type: application/json
```

Flow:

```text
Client
  ↓
POST /v1/orders
  ↓
Authentication / Authorization
  ↓
Validation
  ↓
Idempotency Check
  ↓
Business Logic
  ↓
Persist
  ↓
201 Created
```

### Fetch Order

```http
GET /v1/orders/9001
```

### List Orders

```http
GET /v1/orders?status=PAID&limit=20&cursor=abc
```

```text
status
→ filtering

limit
→ page size

cursor
→ pagination
```

---

## 23. Full Day 3 Flow

```text
Client
  ↓
API Version
/v1
  ↓
Resource
/orders
  ↓
HTTP Method
GET / POST / PUT / PATCH / DELETE
  ↓
Authentication / Authorization
  ↓
Request Validation
  ↓
Idempotency Check (when needed)
  ↓
Business Logic
  ↓
Database / Cache / Other Services
  ↓
Result
  ↓
HTTP Status Code
  ↓
Structured Response / Error
  ↓
Correlation ID for tracing
```

For collection APIs:

```text
GET /orders
   ↓
Filtering
   ↓
Sorting
   ↓
Pagination
Offset / Cursor
   ↓
Response
```

---

## 24. System Design Interview Focus

```text
Small admin table
→ Offset pagination is fine

Large changing feed
→ Cursor pagination

Breaking API change
→ New API version

Distributed request debugging
→ Correlation ID

Retry-sensitive create operation
→ Idempotency key
```

The goal is not to memorize rules. Explain **why you chose an approach and what trade-off it creates**.

---

# Fast Recall — One-Liners

```text
REST
→ Design APIs around resources and HTTP methods.

Resource
→ Business entity exposed through the API.

POST /users
→ Create a new resource inside the users collection.

Query Parameters
→ Filter, sort, search, and paginate collections.

Offset Pagination
→ Skip N records and return the next K.

Cursor Pagination
→ Continue from the last known ordered position.

Offset
→ Best for simple/smaller page-number based datasets.

Cursor
→ Best for large, frequently changing feeds.

API Versioning
→ Evolve APIs without breaking existing clients.

URI Versioning
→ Version appears in the path, e.g. /v1/users.

Header Versioning
→ Same URL; version is carried in request headers.

Backward Compatibility
→ Change APIs without breaking current consumers.

Deprecation
→ Gradually retire old API versions after migration.

Structured Error
→ Return machine-readable code + message + details.

Correlation ID
→ Trace one request across multiple services.

Idempotency
→ Repeating the same operation keeps the same intended final effect.

Idempotency Key
→ Prevent duplicate processing of retry-sensitive requests.

Request Fingerprint
→ Ensures the same idempotency key is not reused for a different request.
```

---

# Final Mental Model

```text
RESOURCE
→ What am I exposing?

METHOD
→ What action is requested?

QUERY PARAMETERS
→ How should results be filtered/sorted?

PAGINATION
→ How do I safely return large datasets?

VERSIONING
→ How do I evolve without breaking clients?

ERROR DESIGN
→ How does the client understand failure?

CORRELATION ID
→ How do I trace distributed requests?

IDEMPOTENCY
→ How do I make retries safe?
```
