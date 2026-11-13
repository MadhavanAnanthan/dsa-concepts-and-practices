# Day 2 — HTTP, HTTPS/TLS, Keep-Alive

## 1. HTTP

**HTTP = Hypertext Transfer Protocol**

HTTP is an application-layer **request/response protocol** used between clients and servers.

### Flow

```text
Client
  ↓
HTTP Request
  ↓
Server
  ↓
HTTP Response
  ↓
Client
```

---

## 2. HTTP Request

An HTTP request mainly contains:

```text
Method
Path / URL
Headers
Body (optional)
```

Example:

```http
POST /users
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Madhavan"
}
```

### Breakdown

```text
POST
→ HTTP method

/users
→ path

Content-Type / Authorization
→ headers

JSON
→ request body
```

---

## 3. HTTP Response

An HTTP response mainly contains:

```text
Status Code
Headers
Body
```

Example:

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 100,
  "name": "Madhavan"
}
```

---

## 4. HTTP Methods

```text
GET
→ Read / retrieve data

POST
→ Create / submit / process

PUT
→ Replace or update the full resource

PATCH
→ Partially update a resource

DELETE
→ Delete a resource
```

Examples:

```text
GET    /users/100
POST   /users
PUT    /users/100
PATCH  /users/100
DELETE /users/100
```

---

## 5. HTTP Headers

Headers carry **metadata** about the request or response.

Common headers:

```text
Content-Type
Accept
Authorization
User-Agent
Cache-Control
```

Examples:

```http
Content-Type: application/json
```

```text
→ Body contains JSON
```

```http
Accept: application/json
```

```text
→ Client prefers JSON response
```

```http
Authorization: Bearer <token>
```

```text
→ Carries authentication information
```

### Remember

```text
Headers
→ metadata

Body
→ actual payload
```

---

## 6. HTTP Status Codes

### Categories

```text
1xx → Informational
2xx → Success
3xx → Redirection
4xx → Client/request-side problem
5xx → Server/upstream-side problem
```

### Common Codes

```text
200 → OK
201 → Created
204 → Success with no response body

301 / 302 → Redirect

400 → Bad Request
401 → Authentication missing / invalid
403 → Authenticated but access not allowed
404 → Resource not found

500 → Internal Server Error
502 → Bad Gateway
503 → Service Unavailable
504 → Gateway Timeout
```

### 401 vs 403

```text
401
→ authentication failed or missing

403
→ identity is known, but access is not allowed
```

---

## 7. HTTP in the Network Flow

```text
DNS
 ↓
IP
 ↓
Port
 ↓
TCP Connection
 ↓
HTTP Request
 ↓
Server
 ↓
HTTP Response
```

HTTP defines **what the application is requesting and returning**.

---

# HTTPS / TLS

## 8. HTTPS

**HTTPS = HTTP protected by TLS**

```text
HTTP + TLS = HTTPS
```

Traditional HTTPS flow:

```text
DNS
 ↓
IP
 ↓
Port 443
 ↓
TCP Handshake
 ↓
TLS Handshake
 ↓
Secure Channel
 ↓
HTTP Request / Response
```

---

## 9. What TLS Provides

TLS mainly provides:

```text
Encryption
Integrity
Authentication
```

### Encryption

Prevents others from reading the HTTP content.

```text
HTTP Data
   ↓
Encrypted Bytes
   ↓
Network
```

### Integrity

Helps detect whether data was modified during transmission.

### Authentication

Allows the client to verify the server identity.

```text
Client
  ↓
api.example.com
  ↓
Server Certificate
  ↓
Certificate Verification
```

---

## 10. TLS Certificate

A TLS certificate proves that the server is authorized to use a hostname/domain.

Example:

```text
api.example.com
```

Certificate providers / managed services include:

```text
Let's Encrypt
DigiCert
Sectigo
GlobalSign
AWS Certificate Manager
Azure-managed certificates
Google Cloud Certificate Manager
```

Certificates can be free or paid.

---

## 11. Domain → DNS → Certificate → HTTPS

```text
Buy Domain
   ↓
Configure DNS
   ↓
Domain points to Public Server / Load Balancer / Gateway
   ↓
Obtain TLS Certificate
   ↓
Attach Certificate where TLS terminates
   ↓
Expose HTTPS on Port 443
   ↓
Serve HTTPS traffic
```

### Why DNS is configured by us

The registrar knows we own the domain, but only we know where our application is hosted.

```text
api.example.com
   ↓
DNS Record
   ↓
Public Load Balancer / Gateway / Server
```

---

## 12. Where Is the Certificate Attached?

The certificate is configured where **TLS terminates**.

Common locations:

```text
Load Balancer
Application Gateway
Reverse Proxy
Web Server
Application Server
```

Typical production architecture:

```text
api.example.com
      ↓
DNS
      ↓
Load Balancer / Gateway :443
      ↓
TLS Certificate attached here
      ↓
TLS terminates
      ↓
Spring Boot instances
```

Reverse proxy example:

```text
Internet
   ↓
Nginx :443
   ↓
TLS Certificate
   ↓
Spring Boot :8080
```

### Key Rule

```text
Certificate
→ attach where HTTPS/TLS ends
```

---

## 13. TLS Handshake

Simplified flow:

```text
Client
  ↓
Client Hello
  ↓
Server Hello + Certificate
  ↓
Client verifies certificate
  ↓
Shared session keys established
  ↓
Encrypted communication starts
```

For system design, deep cryptography internals are not required.

---

## 14. Why Session Keys?

Asymmetric cryptography is relatively expensive.

TLS typically uses:

```text
Asymmetric Crypto
→ authentication + key establishment

Symmetric Session Key
→ actual data encryption
```

---

# Keep-Alive & Connection Pooling

## 15. HTTP Keep-Alive

Without keep-alive:

```text
Request 1
→ TCP Handshake
→ TLS Handshake
→ HTTP
→ Close

Request 2
→ TCP Handshake
→ TLS Handshake
→ HTTP
→ Close
```

With keep-alive:

```text
TCP Connection
   ↓
TLS Session
   ↓
Request 1 ↔ Response 1
Request 2 ↔ Response 2
Request 3 ↔ Response 3
   ↓
Connection Reused
```

### Benefits

```text
Fewer TCP handshakes
Fewer TLS handshakes
Lower latency
Lower CPU/network overhead
Better throughput
```

### Important

Keep-alive does **not** mean the connection stays open forever.

It may close because of:

```text
Idle timeout
Server timeout
Client timeout
Network failure
Maximum connection lifetime
```

---

## 16. Connection Pooling

Keep-alive allows a connection to remain reusable.

A **connection pool** manages multiple reusable connections.

```text
Service A

Connection Pool
├── Connection 1
├── Connection 2
├── Connection 3
└── Connection 4

       ↓

Service B
```

### Flow

```text
Request
  ↓
Borrow available connection
  ↓
Send HTTP request
  ↓
Receive response
  ↓
Return connection to pool
```

### Benefits

```text
Connection reuse
Concurrency
Lower latency
Reduced handshake overhead
```

### Keep-Alive vs Pooling

```text
Keep-Alive
→ keep a connection reusable

Connection Pool
→ manage many reusable connections
```

---

## 17. Spring Boot / Microservice Relevance

Example:

```text
Order Service
   ↓
HTTP Client
   ↓
Connection Pool
   ↓
Payment Service
```

Instead of creating a new TCP/TLS connection for every request, the client can reuse existing connections.

Examples:

```text
RestTemplate
WebClient
Java HttpClient
Apache HttpClient
```

---

## 18. Connection Pool Exhaustion

Suppose:

```text
Pool Size = 10
```

but:

```text
50 Concurrent Requests
```

Then:

```text
10 Requests
→ use connections

Remaining Requests
→ wait for free connection
```

If the wait exceeds the configured limit:

```text
Pool Timeout
```

---

# Failure Points

## 19. DNS Failure

```text
Client
 ↓
DNS ❌
```

No destination address is obtained.

---

## 20. Connect Timeout

```text
DNS ✅
 ↓
TCP Connection Attempt
 ↓
Timeout ❌
```

Meaning:

```text
Connection could not be established in time
```

---

## 21. TLS Failure

```text
TCP ✅
 ↓
TLS Handshake
 ↓
Failure ❌
```

Possible reasons:

```text
Expired certificate
Hostname mismatch
Untrusted certificate
TLS compatibility issue
```

---

## 22. Read / Response Timeout

```text
Connection Established ✅
Request Sent ✅
Server Too Slow
 ↓
Read Timeout ❌
```

### Timeout Difference

```text
Connect Timeout
→ connection could not be established in time

Read Timeout
→ connection exists, but response did not arrive in time

Pool Timeout
→ no reusable connection became available in time
```

---

# Full Day 2 Flow

## 23. End-to-End Request Flow

Suppose:

```text
https://api.example.com/users/100
```

```text
Client
  ↓
DNS resolves api.example.com
  ↓
Destination IP + Port 443
  ↓
TCP 3-Way Handshake
  ↓
TLS Handshake
  ↓
Certificate Verification
  ↓
Shared Encryption Keys Established
  ↓
Secure HTTPS Connection
  ↓
HTTP Request

GET /users/100
Headers
Optional Body

  ↓
Load Balancer / API Gateway
  ↓
Spring Boot
  ↓
Controller
  ↓
Service
  ↓
Repository / Cache / DB / Other Service
  ↓
HTTP Response

Status Code
Headers
Body

  ↓
TLS Encrypts Response
  ↓
TCP Transports Response
  ↓
Client Receives and Decrypts Response
  ↓
Connection Can Stay Alive
  ↓
Future Requests Can Reuse Pooled Connection
```

---

# Day 2 — One-Line Summary

```text
HTTP
→ Application-level request/response protocol.

HTTP Request
→ Method + Path + Headers + optional Body.

HTTP Response
→ Status Code + Headers + Body.

GET
→ Retrieve data.

POST
→ Create or process data.

PUT
→ Replace/update full resource.

PATCH
→ Partially update a resource.

DELETE
→ Delete a resource.

Headers
→ Metadata about request/response.

Body
→ Actual payload.

Status Codes
→ Result of request processing.

HTTPS
→ HTTP protected by TLS.

TLS
→ Provides encryption, integrity, and authentication.

TLS Certificate
→ Proves server identity for a hostname/domain.

TLS Handshake
→ Verifies identity and establishes encryption keys.

Keep-Alive
→ Reuses an existing TCP connection.

Connection Pooling
→ Manages multiple reusable connections.

Connect Timeout
→ Maximum wait to establish a connection.

Read Timeout
→ Maximum wait for response data.

Pool Timeout
→ Maximum wait for an available pooled connection.
```

---

# Day 1 Quick Revision

## IP

```text
IP
→ Addressing and packet routing toward a destination.
```

```text
IPv4
→ 32-bit address

IPv6
→ 128-bit address
```

---

## Public vs Private IP

```text
Private IP
→ Used inside local/private networks.

Public IP
→ Routable on the Internet.
```

Private IPs can also be IPv4 or IPv6.

---

## Packet

```text
Packet
→ Structured chunk of bytes + network metadata.
```

---

## Default Gateway

```text
Default Gateway
→ Router/interface used when destination is outside the local network.
```

Flow:

```text
Device
  ↓
Default Gateway
  ↓
Router
  ↓
Internet
```

---

## NAT

```text
NAT
→ Maps private IP:port communication to public IP:port communication.
```

Example:

```text
192.168.1.10:51000
        ↕
49.x.x.x:62000
```

Response:

```text
49.x.x.x:62000
   ↓
NAT Lookup
   ↓
192.168.1.10:51000
```

---

## DNS

```text
DNS
→ Resolves hostname into network destination information.
```

Flow:

```text
Browser / App Cache
   ↓
OS DNS Cache
   ↓
Recursive Resolver
   ↓
Root
   ↓
TLD
   ↓
Authoritative DNS
   ↓
A / AAAA / CNAME
```

```text
A
→ IPv4

AAAA
→ IPv6

CNAME
→ Hostname → another hostname
```

---

## DNS TTL

```text
TTL
→ Controls how long DNS answers may remain cached.
```

```text
Long TTL
→ better caching, slower DNS changes

Short TTL
→ faster DNS changes, more DNS queries
```

---

## Port

```text
IP
→ Which machine/interface?

Port
→ Which service?
```

Examples:

```text
80   → HTTP
443  → HTTPS
5432 → PostgreSQL
6379 → Redis
8080 → Common Spring Boot development port
```

---

## Socket

```text
Socket
→ OS-managed network communication endpoint used by an application.
```

TCP connection commonly identified by:

```text
Source IP
Source Port
Destination IP
Destination Port
```

---

## TCP

```text
TCP
→ Connection-oriented, reliable, ordered transport.
```

Handshake:

```text
Client                  Server

SYN ------------------->

    <--------------- SYN-ACK

ACK ------------------->

Connection Established
```

---

## UDP

```text
UDP
→ Connectionless datagram transport without built-in reliability or ordering.
```

Typical uses:

```text
DNS
Voice
Live Video
Gaming
```

---

# Day 1 + Day 2 Combined Flow

```text
User enters:
https://api.example.com/users/100

        ↓
DNS Cache / Resolver
        ↓
Hostname → IP
        ↓
Destination outside local network
        ↓
Default Gateway / Router
        ↓
NAT
Private IP:Port → Public IP:Port
        ↓
ISP / Internet
        ↓
Destination IP + Port 443
        ↓
Socket
        ↓
TCP Handshake
SYN → SYN-ACK → ACK
        ↓
TLS Handshake
        ↓
Certificate Verification
        ↓
Secure HTTPS Connection
        ↓
HTTP Request
Method + Path + Headers + Body
        ↓
Load Balancer / API Gateway
        ↓
Spring Boot
        ↓
Controller → Service → Repository
        ↓
HTTP Response
Status + Headers + Body
        ↓
TLS / TCP
        ↓
Internet
        ↓
Router / NAT Mapping
        ↓
Correct Client
        ↓
Connection may remain alive
        ↓
Future requests can reuse pooled connection
```

---

# Final Mental Model

```text
DNS
→ Where is the hostname pointing?

IP
→ Which network destination?

Default Gateway
→ Where do I send non-local traffic?

NAT
→ How are private/public IP:ports mapped?

Port
→ Which service?

Socket
→ Application's network communication endpoint.

TCP
→ Reliable, ordered transport.

UDP
→ Connectionless datagram transport.

TLS
→ Secure transport.

HTTP
→ Application request/response.

Keep-Alive
→ Reuse an existing connection.

Connection Pool
→ Manage multiple reusable connections.
```
