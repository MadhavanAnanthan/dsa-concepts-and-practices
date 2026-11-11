# Networking Fundamentals — Network, DNS, TCP

## 1. Network & Internet

- **Network** → group of devices that can communicate.
- **Internet** → network of interconnected networks.
- Traffic may pass through multiple ISPs, backbone networks, Internet exchanges, cloud/data-center networks, etc.

### Flow

```text
Phone / Laptop
    ↓
Home Network
    ↓
ISP
    ↓
Internet Backbone / Exchange
    ↓
Other Networks / Cloud Network
    ↓
Destination Server
```

---

# 2. IP — Internet Protocol

**IP provides addressing and packet-delivery rules so traffic can move toward a destination.**

## IPv4

- 32-bit address
- Four 8-bit numbers
- Each number: `0–255`
- About `2^32 ≈ 4.3 billion` addresses

```text
192.168.1.10
```

## IPv6

- 128-bit address
- Introduced mainly because IPv4 address space is limited

```text
2001:db8::25
```

## Public vs Private IP

```text
IP Version     → IPv4 / IPv6
Address Usage  → Public / Private
```

### Private IP
Used inside private networks.

```text
Phone  → 192.168.1.10
Laptop → 192.168.1.11
TV     → 192.168.1.12
```

Different homes can reuse the same private IP ranges.

### Public IP
Globally routable on the Internet.

> Private addresses can also be IPv4 or IPv6.

---

# 3. Packet

A **packet** is a structured chunk of bytes containing:

```text
Network Metadata / Headers
        +
Actual Payload Data
```

It carries information such as source and destination addresses.

---

# 4. Default Gateway

The **default gateway** is the router/interface a device sends traffic to when the destination is outside its local network.

```text
Phone IP        → 192.168.1.10
Default Gateway → 192.168.1.1
```

### Same Network

```text
Phone → Laptop
```

If both are on the same local subnet, traffic can stay local.

### Outside Network

```text
Phone
  ↓
Default Gateway / Router
  ↓
Internet
```

### Remember

```text
Gateway
→ WHERE do I send non-local traffic?
```

---

# 5. NAT — Network Address Translation

NAT allows private devices to communicate externally using public addressing.

```text
Private IP : Private Port
           ↕
Public IP  : Public Port
```

Example:

```text
192.168.1.10:51000
        ↕
49.x.x.x:62000
```

Another device:

```text
192.168.1.20:51000
        ↕
49.x.x.x:62001
```

The public IP can be shared while different ports distinguish connections.

### Response Flow

```text
Server response
    ↓
49.x.x.x:62000
    ↓
NAT table lookup
    ↓
192.168.1.10:51000
    ↓
Correct device
```

### Remember

```text
Gateway
→ WHERE should outside traffic go?

NAT
→ HOW are private/public IP:port values translated?
```

---

# 6. DNS — Domain Name System

DNS resolves human-readable hostnames into network information such as IP addresses.

```text
api.example.com
       ↓
DNS
       ↓
203.0.113.25
```

## Registrar vs DNS vs Hosting

```text
Registrar
→ where domain is registered

Authoritative DNS
→ stores DNS records

Hosting
→ where application runs
```

These can all be different providers.

## Name Servers

The registrar stores which authoritative name servers handle the domain.

```text
example.com
    ↓
ns1.provider.com
ns2.provider.com
```

## DNS Resolver

The resolver finds DNS answers on behalf of the client.

It may be provided by:

- ISP
- Company network
- Public DNS provider
- Router forwarding to another resolver

## DNS Resolution Flow

```text
Browser / Application DNS Cache
        ↓
OS DNS Cache
        ↓
Recursive DNS Resolver
        ↓
Resolver Cache
        ↓
Root DNS
        ↓
TLD DNS (.com / .in / etc.)
        ↓
Authoritative DNS
        ↓
DNS Record
        ↓
IP / Target Hostname
```

## Important Records

### A
```text
Hostname → IPv4
```

### AAAA
```text
Hostname → IPv6
```

### CNAME
```text
Hostname → Another Hostname
```

Example:

```text
api.example.com
      ↓
myapp.azurewebsites.net
```

## A + AAAA

Not mandatory.

```text
A only      → IPv4
AAAA only   → IPv6
A + AAAA    → Dual-stack
```

Dual-stack mainly improves compatibility and reachability across both IPv4 and IPv6 networks.

It does **not** mean 2× performance.

---

# 7. DNS Caching & TTL

DNS answers are cached to avoid repeating the full lookup.

Possible cache locations:

```text
Browser / Application
OS
Recursive Resolver
```

## TTL — Time To Live

Controls how long a DNS result may remain cached.

```text
api.example.com → 203.0.113.25
TTL = 300 seconds
```

## Trade-off

```text
Long TTL
→ fewer DNS lookups
→ better caching
→ slower DNS changes to propagate

Short TTL
→ faster DNS changes
→ more DNS queries
```

## When Server IP Changes

```text
Old:
api.example.com → 1.1.1.1

New:
api.example.com → 2.2.2.2
```

Some clients may still use the cached old IP until TTL expires.

If the old server is already shut down:

```text
Cached old IP
    ↓
Connection failure
```

---

# 8. DNS in Backend Architecture

```text
api.example.com
      ↓
DNS
      ↓
Load Balancer / API Gateway
      ↓
Spring Boot Instances
```

DNS usually points to stable public infrastructure rather than directly to one application instance.

If one instance fails:

```text
App1 ❌
App2 ✅
App3 ✅
```

the load balancer can stop routing to App1 without changing DNS.

---

# 9. DNS vs HTTP Path

Example:

```text
https://api.example.com/users/100
```

```text
https
→ protocol

api.example.com
→ hostname resolved by DNS

443
→ HTTPS port

/users/100
→ HTTP path handled by backend
```

Spring Boot handles:

```text
/users/100
→ Controller
→ Service
→ Repository
```

---

# 10. Port

An IP gets traffic to the machine/network destination.

A **port** identifies the service/application endpoint on that machine.

```text
IP   → Which machine/interface?
Port → Which service?
```

Examples:

```text
22   → SSH
80   → HTTP
443  → HTTPS
5432 → PostgreSQL
6379 → Redis
8080 → Common Spring Boot development port
```

---

# 11. Source Port & Destination Port

The server listens on a known destination port.

The client OS usually chooses a temporary source port.

```text
Client
192.168.1.10:53127

        ↕

Server
203.0.113.25:443
```

This helps distinguish many simultaneous connections.

---

# 12. Socket

A **socket** is the OS-managed software endpoint an application uses to send and receive network data.

Think of it as a **logical communication endpoint**, not a physical wire.

```text
Application
    ↓
Socket API
    ↓
Operating System
    ↓
Network Stack
```

For TCP, a connection is commonly identified by:

```text
Source IP
Source Port
Destination IP
Destination Port
```

---

# 13. TCP — Transmission Control Protocol

TCP provides:

- Connection-oriented transport
- Reliable byte stream
- Ordered delivery
- Retransmission of lost data
- Flow control
- Congestion control

For system design, focus mainly on:

```text
Connection
Reliability
Ordering
Retransmission
```

---

# 14. TCP 3-Way Handshake

```text
Client                  Server

SYN ------------------->

    <--------------- SYN-ACK

ACK ------------------->

Connection Established
```

```text
SYN
→ I want to connect

SYN-ACK
→ I received your request and acknowledge it

ACK
→ Confirmed
```

---

# 15. TCP Reliability & Ordering

If data is lost:

```text
1 2 3 4 5

Network loses 3
```

TCP can retransmit it.

If data arrives out of order:

```text
1 3 2 4
```

TCP ensures the application receives the byte stream in order.

---

# 16. TCP vs UDP

## TCP

```text
Connection-oriented
Reliable
Ordered
Retransmission built in
More protocol overhead
```

Typical uses:

```text
REST APIs
HTTP/1.1
HTTP/2
Database connections
File transfers
```

## UDP

```text
Connectionless
No built-in delivery guarantee
No built-in ordering guarantee
No built-in retransmission
Lower protocol overhead
```

Typical uses:

```text
DNS queries
Voice
Live video
Online gaming
```

### Why UDP for Real-Time Traffic?

#### Voice
A tiny missing audio packet is often better skipped than delayed.

#### Live Video
A missing frame may be less harmful than pausing to recover old data.

#### Gaming

```text
t1 → x=10
t2 → x=11   ← lost
t3 → x=12
t4 → x=13
```

The latest state can matter more than an old lost update.

### Important

Do not memorize:

```text
UDP = fast
TCP = slow
```

Better:

```text
TCP
→ provides reliability/order guarantees

UDP
→ simpler datagram transport; higher-level protocol/app decides what guarantees it needs
```

---

# 17. HTTP / HTTPS Placement

For traditional HTTPS over TCP:

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
HTTP Request / Response
```

### Responsibilities

```text
DNS
→ Where is the destination?

IP
→ Which network address?

Port
→ Which service?

TCP
→ Reliable transport

TLS
→ Encryption, integrity, server authentication

HTTP
→ Application request / response
```

---

# 18. Complete Request Flow

Suppose:

```text
https://api.example.com/users/100
```

## Request

```text
User / Browser
      ↓
DNS Cache Check
      ↓
Recursive DNS Resolution if needed
      ↓
api.example.com → IP
      ↓
Destination is outside local network
      ↓
Default Gateway / Router
      ↓
NAT may translate private IP:port
      ↓
ISP
      ↓
Internet Backbone / Other Networks
      ↓
Destination IP + Port 443
      ↓
Socket
      ↓
TCP 3-Way Handshake
SYN → SYN-ACK → ACK
      ↓
TLS Handshake
      ↓
HTTP Request
GET /users/100
      ↓
Load Balancer / API Gateway
      ↓
Spring Boot Instance
      ↓
Controller → Service → Repository
      ↓
Database / Cache / Other Services
```

## Response

```text
Spring Boot
      ↓
HTTP Response
      ↓
TLS / TCP
      ↓
Internet
      ↓
ISP
      ↓
Router
      ↓
NAT Mapping
      ↓
Correct Private IP:Port
      ↓
Client Application
```

---

# 19. Final Mental Model

```text
DNS
→ Where is the hostname pointing?

IP
→ Which network destination?

Default Gateway
→ Where do I send non-local traffic?

Router
→ How is the packet forwarded to another network?

NAT
→ How are private/public IP:ports mapped?

Port
→ Which service on the destination?

Socket
→ Application's network communication endpoint

TCP
→ Reliable, ordered transport

UDP
→ Datagram transport without built-in reliability/order

TLS
→ Secure/encrypted communication

HTTP
→ Application-level request/response
```

---

# 20. One-Paragraph Takeaway

When a client accesses a backend, DNS first resolves the hostname to a network destination. If the destination is outside the local network, traffic goes through the default gateway/router, where NAT may translate private IP:port values to public ones. The destination IP identifies the host/network interface, while the port identifies the service. Applications communicate through sockets, and TCP can establish a reliable ordered connection using the three-way handshake. UDP provides a simpler datagram model for use cases where timeliness may matter more than perfect delivery. For HTTPS, TCP is followed by TLS, then the HTTP request reaches infrastructure such as a load balancer or API gateway and finally the backend application.
