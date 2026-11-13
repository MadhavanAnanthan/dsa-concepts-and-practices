# Day 4 — Sync vs Async, Timeouts, Retries & Idempotency

## 1. Synchronous vs Asynchronous Communication

### Synchronous

The caller waits for the dependent operation to complete before continuing the business flow.

```text
Service A
   ↓
Request
   ↓
Service B
   ↓
Process
   ↓
Response
   ↓
Service A continues
```

Use sync when the result is needed immediately.

Examples:

```text
Login
Inventory check before purchase
Payment authorization
Fetching customer/profile data
```

### Blocking vs Non-Blocking

```text
Synchronous / Asynchronous
→ business-flow dependency

Blocking / Non-blocking
→ thread/I/O behavior
```

A non-blocking WebClient call can still be synchronous at the business level if the caller must wait for the result before continuing.

### Asynchronous

The caller does not need to wait for final downstream processing.

```text
Producer
   ↓
Message / Event
   ↓
Broker / Queue
   ↓
Consumer
```

Use async when:

```text
Immediate result is not required
Work can happen later
Traffic needs buffering
Failure isolation is useful
Services should scale independently
```

Examples:

```text
Email
Notifications
Analytics
Audit events
Report generation
Background jobs
```

### Trade-Off

```text
Sync
→ simple + immediate result + stronger runtime coupling

Async
→ loose coupling + buffering + failure isolation + more complexity
```

Async may introduce:

```text
Eventual consistency
Retries
Duplicate messages
Ordering concerns
Dead-letter queues
Monitoring complexity
```

---

## 2. Synchronous Dependency & Cascading Latency

```text
Client
  ↓
Order Service
  ↓
Inventory Service
  ↓
Payment Service
```

If dependencies are synchronous, downstream latency becomes caller latency.

```text
Slow dependency
→ caller waits
→ threads/connections remain occupied
→ queueing grows
→ upstream latency increases
→ cascading failure risk
```

---

## 3. Timeouts

Every remote call should have a bounded timeout.

Without one:

```text
Service A
   ↓
Calls Service B
   ↓
Wait
   ↓
Wait
   ↓
Resources stay occupied
```

### Pool Timeout

Maximum time to wait for an available connection from the pool.

```text
Need connection
   ↓
No free pooled connection
   ↓
Wait
   ↓
Pool Timeout
```

### Connect Timeout

Maximum time allowed to establish the network connection.

```text
Service A
   ↓
Connect to Service B
   ↓
TCP connection not established
   ↓
Connect Timeout
```

Possible causes:

```text
Service down
Wrong port
Firewall issue
Network issue
Host unreachable
```

### Read / Response Timeout

Connection exists, but the response is too slow.

```text
Connection established ✅
Request sent ✅
Service processing...
   ↓
Response too slow
   ↓
Read Timeout
```

### Timeout Difference

```text
Pool Timeout
→ no reusable connection became available in time

Connect Timeout
→ network connection could not be established in time

Read Timeout
→ connection exists, but response did not arrive in time
```

### Timeout Selection

```text
Too high
→ resources wait too long

Too low
→ healthy requests fail unnecessarily
```

Choose timeouts using:

```text
Observed dependency latency
Tail latency
Business SLA
Overall request deadline
```

### End-to-End Timeout Budget

If total request budget is 2 seconds:

```text
Total Budget
   ↓
Allocate time across dependencies
   ↓
Keep some buffer
```

Downstream timeouts should fit inside the caller's overall deadline.

### Important

> Timeout means uncertainty, not guaranteed failure.

Example:

```text
Order Service
   ↓
POST /payment
   ↓
Payment succeeds ✅
   ↓
Response lost / delayed
   ↓
Order Service gets timeout ❌
```

---

## 4. Retries

Retries are useful mainly for temporary/transient failures.

```text
Service A
   ↓
Temporary failure
   ↓
Retry
   ↓
Success
```

### Common Retryable Cases

Depending on API semantics:

```text
Temporary network failure
Connection reset
Timeout
503 Service Unavailable
429 Too Many Requests
```

### Usually Not Retryable

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
Validation errors
Permanent business-rule failures
```

### Bounded Retries

Never retry forever.

```text
Attempt 1
   ↓ fail
Retry 1
   ↓ fail
Retry 2
   ↓ fail
Stop
```

Retries should also respect the overall request deadline.

---

## 5. Retry Storm

If many clients retry an already overloaded service:

```text
Service slow
   ↓
Requests fail
   ↓
Clients retry immediately
   ↓
More traffic
   ↓
Service becomes slower
   ↓
More failures
```

Retries can amplify an outage.

### Retry Multiplication Across Layers

```text
Client
 ↓
API Gateway
 ↓
Order Service
 ↓
Payment Service
```

If every layer retries 3 times:

```text
1 request
→ 3
→ 9
→ 27 downstream attempts
```

> Retries should be controlled at the appropriate layer, not blindly enabled everywhere.

### Retry-After

Example:

```http
429 Too Many Requests
Retry-After: 5
```

The client should generally respect server retry guidance.

---

## 6. Good Retry Policy

```text
Retry only transient failures
Bound retry count
Respect overall deadline
Use exponential backoff
Add jitter
Honor Retry-After when available
Use idempotency for state-changing operations
```

---

## 7. Double-Charge / Duplicate-Processing Risk

```text
Client
  ↓
POST /payments
₹5,000
  ↓
Payment succeeds ✅
  ↓
Response lost ❌
  ↓
Client retries
```

Without protection:

```text
First request
→ ₹5,000 charged

Retry
→ ₹5,000 charged again
```

This also applies to:

```text
Order creation
Ticket booking
Hotel reservation
Inventory deduction
Coupon redemption
Money transfer
```

### Pattern

```text
State-changing request
   ↓
Server succeeds
   ↓
Response lost
   ↓
Client uncertain
   ↓
Client retries
   ↓
Duplicate side effect
```

---

## 8. Idempotency

Idempotency means:

> Repeating the same logical operation produces the same intended final effect as doing it once.

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
→ depends on operation/design
```

---

## 9. Idempotency Key

Used to make retry-sensitive operations safe.

```http
POST /payments
Idempotency-Key: payment-abc-123
```

### First Request

```text
Key not found
   ↓
Process payment
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
Orders
Bookings
Ticket purchases
Other retry-sensitive create operations
```

---

## 10. Idempotency Store

Possible stores:

```text
Database
Redis
Dedicated idempotency store
```

Typical stored data:

```text
Idempotency Key
Request Fingerprint
Processing Status
Result / Response
Expiry Time
```

Keys usually expire based on business requirements.

---

## 11. Request Fingerprint

An idempotency key alone only tells the server:

```text
"I have seen this key before."
```

It does not prove the retry is the same request.

Example:

```text
Idempotency-Key: abc123
Amount: ₹5,000
```

Server stores:

```text
Key         → abc123
Fingerprint → hash(amount=5000, currency=INR, account=A1)
Result      → SUCCESS
```

Later the same key is accidentally reused:

```text
Idempotency-Key: abc123
Amount: ₹10,000
```

So compare:

```text
Same key + same fingerprint
→ genuine retry
→ return previous result

Same key + different fingerprint
→ incorrect key reuse
→ reject
```

Fingerprint can be derived from important request fields:

```text
Amount
Currency
Account ID
Order ID
Relevant request payload
```

---

## 12. Exponential Backoff

Retry delays increase after each failure.

```text
Retry 1 → wait 1 sec
Retry 2 → wait 2 sec
Retry 3 → wait 4 sec
Retry 4 → wait 8 sec
```

Conceptually:

```text
delay = baseDelay × 2^retryNumber
```

Purpose:

```text
Reduce pressure on unhealthy service
Give dependency time to recover
Avoid continuous hammering
```

---

## 13. Jitter

Even exponential backoff can create synchronized retries.

Without jitter:

```text
All clients retry at 1 sec
All clients retry at 2 sec
All clients retry at 4 sec
```

Jitter adds randomness:

```text
Client A → 3.2 sec
Client B → 4.7 sec
Client C → 2.9 sec
Client D → 5.1 sec
```

### Mental Model

```text
Exponential Backoff
→ increase retry delay

Jitter
→ randomize retry timing
```

Together they reduce the **thundering herd** problem.

---

## 14. Retry Decision Flow

```text
Request fails
   ↓
Is failure transient?
 ├── No → Fail fast
 └── Yes
       ↓
Is operation safe/idempotent?
 ├── No → Do not blindly retry
 └── Yes
       ↓
Retry budget available?
 ├── No → Fail
 └── Yes
       ↓
Wait using backoff + jitter
       ↓
Retry
```

---

## 15. Full Day 4 Flow

```text
Service A needs Service B
        ↓
Choose Sync or Async
        ↓
If Sync:
Service A waits for result
        ↓
Dependency slow?
        ↓
Use bounded timeout
        ↓
Call fails / times out
        ↓
Should we retry?
        ↓
Only if failure is transient
        ↓
Could first attempt already have succeeded?
        ↓
Duplicate-processing risk
        ↓
Use Idempotency Key
        ↓
Validate Request Fingerprint
        ↓
Retry with:
Bounded Attempts
+ Exponential Backoff
+ Jitter
+ Overall Deadline
```

---

## 16. System Design Interview Focus

A strong answer should explain:

```text
What failures are retryable?
How many times should I retry?
How long do I wait?
Could the first attempt already have succeeded?
Is the operation idempotent?
Could retries overload the dependency?
```

Good summary answer:

> Use bounded retries only for transient failures, respect the overall request deadline, protect state-changing operations with idempotency, and use exponential backoff with jitter so retries do not overload an already unhealthy dependency.

---

# Fast Recall — One-Liners

```text
Synchronous
→ Caller needs the result before continuing the business flow.

Asynchronous
→ Caller does not need to wait for final downstream processing.

Blocking vs Non-Blocking
→ Describes thread/I/O behavior, not business-flow dependency.

Runtime Coupling
→ A synchronous caller depends directly on downstream availability and latency.

Cascading Failure
→ One slow dependency causes resource buildup and slows upstream services.

Timeout
→ Limits how long a remote call is allowed to wait.

Pool Timeout
→ Maximum wait for an available pooled connection.

Connect Timeout
→ Maximum wait to establish the network connection.

Read Timeout
→ Maximum wait for response data after connection is established.

Timeout Budget
→ Downstream timeouts must fit inside the overall request deadline.

Timeout Result
→ Timeout means uncertainty, not guaranteed operation failure.

Retry
→ Re-attempt a transiently failed operation.

Retryable Failure
→ Temporary failure that may succeed on another attempt.

Retry Storm
→ Retries add load to an already failing dependency.

Bounded Retry
→ Retry only a limited number of times.

Retry-After
→ Server guidance telling the client when to retry.

Double-Charge Risk
→ First request succeeds, response is lost, retry causes duplicate side effect.

Idempotency
→ Repeating the same logical operation keeps the same intended final effect.

Idempotency Key
→ Identifies the same logical retry and prevents duplicate processing.

Idempotency Store
→ Stores key, request information, status, result, and expiry.

Request Fingerprint
→ Confirms that the same idempotency key is being reused for the same request.

Exponential Backoff
→ Increase retry delay after each failure.

Jitter
→ Add randomness to retry timing.

Thundering Herd
→ Many clients retry at the same time and create a traffic spike.
```

---

# Final Mental Model

```text
SYNC / ASYNC
→ Does the caller need the result now?

TIMEOUT
→ How long am I willing to wait?

RETRY
→ Is this failure temporary and worth trying again?

IDEMPOTENCY
→ Can I safely repeat this operation?

IDEMPOTENCY KEY
→ Is this retry the same logical request?

REQUEST FINGERPRINT
→ Is the retry payload actually identical?

BACKOFF
→ How do I reduce retry pressure?

JITTER
→ How do I prevent synchronized retries?
```
