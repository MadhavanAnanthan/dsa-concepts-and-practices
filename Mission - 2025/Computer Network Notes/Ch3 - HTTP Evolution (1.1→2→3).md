# Chapter 3: HTTP Evolution — HTTP/1.1 to HTTP/2 to HTTP/3

## HTTP's job

HTTP is an application-layer request-response protocol. It defines message semantics—methods, status codes, headers, caching, and representations—while the underlying transport carries the bytes.

```http
GET /products/42 HTTP/1.1
Host: api.example.com
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: max-age=60

{"id":42,"name":"Keyboard"}
```

HTTP versions mostly change framing and transport behavior. The meaning of `GET`, `404`, or `Cache-Control` remains broadly consistent.

## Core HTTP concepts

### URL and origin

A URL such as `https://api.example.com:443/users?id=7` contains a scheme, host, optional port, path, and query. An **origin** is the tuple `(scheme, host, port)`. Browsers use origin boundaries for security and connection management.

### Methods

| Method | Intended meaning | Safe? | Idempotent? |
|---|---|---:|---:|
| GET | Read a representation | Yes | Yes |
| HEAD | GET headers without body | Yes | Yes |
| POST | Submit/create/process | No | Not inherently |
| PUT | Replace resource at known URI | No | Yes |
| PATCH | Partially update | No | Not inherently |
| DELETE | Remove resource | No | Yes by intended final state |

**Safe** means the client does not request a state change. **Idempotent** means repeating the same request has the same intended effect, though logs and timestamps may still change. These properties guide caching and retries.

### Status code classes

- `1xx`: informational.
- `2xx`: success, such as `200`, `201`, `204`.
- `3xx`: redirection or cache validation, such as `301`, `302`, `304`.
- `4xx`: client-side request issue, such as `400`, `401`, `403`, `404`, `409`, `429`.
- `5xx`: server/proxy failure, such as `500`, `502`, `503`, `504`.

Do not blindly retry every `5xx`; retry policy depends on method safety, deadline, and error cause. `Retry-After` can tell clients when to try again.

### Headers and bodies

Headers carry metadata: content type, compression, authentication, caching, tracing, and negotiation. `Content-Type` describes the body representation; `Accept` describes what the client can receive. `Content-Encoding: gzip` means the representation was compressed.

## HTTP/1.0 and HTTP/1.1

Early HTTP commonly opened one TCP connection per request. HTTP/1.1 made persistent connections the default, allowing several sequential requests on one connection.

Important HTTP/1.1 features:

- mandatory `Host` header enables many sites on one IP;
- persistent connections reduce repeated TCP/TLS handshakes;
- chunked transfer can stream a response of initially unknown length;
- standardized cache controls and conditional requests;
- request pipelining exists but is rarely used because response ordering is fragile.

### HTTP/1.1 head-of-line limitation

One connection normally handles one outstanding request safely at a time. Browsers work around this with several connections per origin. That adds handshakes, sockets, and competition between TCP congestion controllers.

Text framing can also be ambiguous when intermediaries disagree about message length, contributing to request-smuggling vulnerabilities. Correct proxy parsing is security-critical.

## HTTP/2

HTTP/2 keeps HTTP semantics but uses binary frames and multiplexes many logical **streams** over one TCP connection.

Key features:

- **Multiplexing:** frames from many requests and responses share one connection.
- **HPACK header compression:** reduces repeated header bytes.
- **Stream prioritization:** offers scheduling hints, though real support varies.
- **Flow control:** per-stream and per-connection limits prevent an uncontrolled receiver.
- **Server push:** allows proactive responses, but proved difficult to use effectively and is not broadly relied upon.

### What HTTP/2 fixes

An application no longer needs several TCP connections just to overlap requests. A slow HTTP response does not block another response at the HTTP framing layer.

### What it does not fix

All streams still share one ordered TCP byte stream. When a TCP packet is lost, later bytes cannot be delivered to HTTP/2 until the missing bytes arrive. This is transport-level head-of-line blocking.

## HTTP/3 and QUIC

HTTP/3 maps HTTP semantics onto QUIC, which runs over UDP. QUIC supplies encryption, congestion control, loss recovery, and multiple independent streams.

Key advantages:

- a lost packet containing one stream's data does not normally block delivery on unrelated streams;
- TLS 1.3 is integrated into the QUIC handshake, reducing setup round trips;
- connection IDs can preserve sessions across some network changes;
- user-space implementations can evolve without waiting for OS TCP changes.

Trade-offs:

- more CPU and implementation complexity can matter at high scale;
- some networks block or throttle UDP, requiring fallback;
- observability and middlebox tooling developed around TCP may need adaptation;
- QUIC still has congestion control—UDP does not mean unlimited sending.

## Version comparison

| Feature | HTTP/1.1 | HTTP/2 | HTTP/3 |
|---|---|---|---|
| Wire framing | Text-oriented | Binary frames | Binary frames over QUIC |
| Multiplexing | Not practically on one connection | Yes | Yes |
| Underlying transport | TCP | TCP | QUIC over UDP |
| Encryption | Optional by HTTP, usual with HTTPS | Usually TLS in browsers | Mandatory QUIC encryption |
| Cross-stream blocking on packet loss | Multiple connections limit impact | Yes, due to TCP | Greatly reduced |
| Connection migration | No | No | Supported through connection IDs |

## Connection setup and latency

For a new traditional HTTPS connection, the client may pay:

1. DNS resolution;
2. TCP handshake;
3. TLS handshake;
4. HTTP request and response.

Connection reuse removes much of this setup. TLS 1.3 and QUIC can reduce handshake cost, but physical propagation delay remains. **0-RTT** resumption can send some data early, but replay risk means it should be restricted to safe operations.

## Proxies and HTTP architecture

- A **forward proxy** acts for clients, often for policy or privacy.
- A **reverse proxy** acts in front of servers, providing routing, TLS termination, caching, compression, or protection.
- An **API gateway** adds application-aware functions such as authentication, quotas, and request transformation.

Each hop may use a different HTTP version. A browser can use HTTP/3 to a CDN while the CDN uses HTTP/2 or HTTP/1.1 to the origin.

## APIs and system design

### Keep contracts clear

Use resource-oriented URLs where useful, accurate methods/status codes, explicit content types, and versioning/compatibility rules. Protocol correctness makes gateways, caches, clients, and observability work better.

### Protect resources

- enforce request/body/header size limits;
- bound connection and stream counts;
- use rate limits and load shedding;
- propagate deadlines and cancellation;
- stream large bodies instead of buffering everything;
- validate proxy-generated headers such as `X-Forwarded-For` only from trusted proxies.

### Make mutation retries safe

For operations such as payment creation, accept an idempotency key. Store the key with the operation result so a repeated request returns the original result rather than performing the action twice.

### Observe the whole path

Record method, route template, status, latency, response size, protocol version, upstream timing, and trace ID. Avoid using raw URLs with user IDs as metric labels because high cardinality can overwhelm monitoring systems.

## Example request path

```text
Browser --HTTP/3--> CDN --HTTP/2--> API gateway --HTTP/1.1 pool--> Service
          TLS ends          TLS may restart               database call
```

“The site uses HTTP/3” therefore does not mean every internal hop uses HTTP/3.

## Quick review

1. Which parts of HTTP changed from 1.1 to 2, and which semantics stayed stable?
2. Why can one lost TCP packet affect several HTTP/2 streams?
3. Why is connection reuse often more valuable than opening fresh connections?
4. When is an idempotency key necessary?

## Key takeaways

- HTTP defines application semantics; versions improve framing and transport behavior.
- HTTP/2 multiplexes over TCP; HTTP/3 uses independent QUIC streams.
- Connection setup, reuse, proxy hops, and loss can dominate performance.
- Correct methods, caching rules, deadlines, limits, and idempotency make APIs safer and more scalable.
