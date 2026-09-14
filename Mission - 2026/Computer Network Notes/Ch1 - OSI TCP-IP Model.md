# Chapter 1: OSI and TCP/IP Models

## Why layered models exist

Networking is divided into layers so each layer can solve one class of problem behind a stable interface. An application can use HTTP without knowing whether the device is connected by fiber or Wi-Fi, and a router can forward IP packets without understanding the JSON inside them.

The **OSI model** is a seven-layer conceptual model. The **TCP/IP model** more closely reflects the protocols used by the Internet. Neither is a literal description of every implementation; both are tools for reasoning and troubleshooting.

## Layer mapping

| OSI layer | TCP/IP layer | Responsibility | Examples | Data unit |
|---|---|---|---|---|
| 7 Application | Application | User-facing network protocols | HTTP, DNS, SMTP, SSH | Message/data |
| 6 Presentation | Application | Encoding, compression, encryption | JSON, UTF-8, TLS concepts | Data |
| 5 Session | Application | Dialog and session management | RPC sessions, checkpoints | Data |
| 4 Transport | Transport | Process-to-process delivery | TCP, UDP, QUIC* | Segment/datagram |
| 3 Network | Internet | Addressing and inter-network routing | IPv4, IPv6, ICMP | Packet |
| 2 Data Link | Link | Delivery across one local link | Ethernet, Wi-Fi, VLAN, ARP | Frame |
| 1 Physical | Link/Physical | Signals, media, bit transmission | Copper, fiber, radio | Bits |

`*` QUIC runs over UDP but implements reliable streams, congestion control, and security in user space, so strict layer labels can be imperfect.

## The layers in practical terms

### Layer 1: Physical

Transforms bits into electrical, optical, or radio signals. It defines properties such as connectors, frequencies, modulation, and link speeds. Problems here include damaged cables, signal interference, and weak Wi-Fi.

### Layer 2: Data Link

Moves frames across one link or LAN. Ethernet switches learn MAC-to-port mappings. Link-layer checksums detect transmission corruption. VLAN tags create multiple logical LANs on shared switching infrastructure.

A Layer-2 header is replaced at each routed hop because each hop is a new local-link delivery.

### Layer 3: Network

IP provides best-effort packet delivery between networks. Routers use routing tables and prefixes. IP does not promise delivery, ordering, or exactly-once behavior. ICMP carries control and diagnostic messages, including “destination unreachable” and responses used by `ping`.

### Layer 4: Transport

Connects application processes using ports. TCP provides an ordered reliable byte stream plus flow and congestion control. UDP provides independent datagrams with minimal overhead and no delivery guarantees.

### Layers 5–7: Session, presentation, and application

In Internet engineering these responsibilities are usually handled by application libraries and protocols rather than three visibly separate layers. HTTP defines requests and responses; TLS encrypts and authenticates; JSON or Protocol Buffers define representations; application code handles login sessions and business workflows.

## Encapsulation through the stack

Suppose a browser sends `GET /users/42`:

```text
HTTP creates request bytes
  ↓ TCP adds source/destination ports, sequence numbers, flags
  ↓ IP adds source/destination IP and hop limit
  ↓ Ethernet adds source/next-hop MAC and error-detection trailer
  ↓ physical interface sends signals
```

At the server, headers are removed in reverse order. Each layer mostly reads its own header. A router usually processes through Layer 3; a basic switch through Layer 2; a reverse proxy/load balancer can process Layers 4 through 7.

## Devices by layer

| Device | Typical layer | Decision basis |
|---|---|---|
| Repeater/hub | L1 | Reproduce signals/bits |
| Switch/bridge | L2 | Destination MAC and VLAN |
| Router | L3 | Destination IP prefix |
| L4 load balancer | L4 | IP, port, transport connection |
| L7 proxy/API gateway | L7 | Host, URL, headers, cookies, method |
| Firewall | L3–L7 | Policy based on addresses, ports, state, or content |

These labels describe primary behavior. Real devices often operate at several layers.

## Control plane and data plane

- The **control plane** learns or computes where traffic should go. Examples: routing protocols exchange routes; a controller configures load-balancer backends.
- The **data plane** forwards each actual packet using the installed state.

Separating them helps performance and operations: a router can forward packets in hardware while slower control-plane software updates routes.

## End-to-end versus hop-by-hop behavior

- Ethernet delivery, queueing, and MAC addresses are hop-by-hop.
- IP source/destination are normally end-to-end, although NAT can rewrite them.
- TCP reliability is end-to-end between transport endpoints, although a proxy may terminate one connection and create another.
- TLS is end-to-end only as far as the TLS termination point. If TLS ends at a load balancer, that device can see plaintext before optionally re-encrypting to the backend.

Knowing where a protocol terminates is critical to security and debugging.

## Layering is useful, but not absolute

Modern systems deliberately cross layers:

- A browser uses DNS results, network conditions, and certificates to choose a connection.
- QUIC combines transport features and TLS over UDP.
- A CDN uses DNS/anycast routing plus HTTP cache semantics.
- A service mesh uses L4 interception and L7 identity/policy.

Use layers as a mental model, not as a rule that implementations never violate.

## Troubleshooting one layer at a time

When a service is unreachable, test from bottom to top:

1. **Link:** is the interface up and connected to the expected network?
2. **IP:** are address, subnet, gateway, and route correct?
3. **Reachability:** can packets reach the destination? Remember that blocked ICMP does not prove the application is down.
4. **Transport:** is the expected port listening, and can a TCP/UDP exchange occur?
5. **TLS:** does hostname verification pass, and is the certificate valid?
6. **Application:** is HTTP returning the expected status and payload?

Useful tools include `ipconfig`/`ip addr`, `route`, `arp`, `ping`, `traceroute`/`tracert`, `nslookup`/`dig`, `curl`, `ss`/`netstat`, and packet capture tools such as Wireshark.

## System-design implications

- **L4 load balancing** is fast and protocol-agnostic but has little application context.
- **L7 load balancing** enables path routing, authentication, and richer telemetry, but requires parsing and usually terminates connections.
- **TLS termination** placement determines who can inspect data and where encryption boundaries lie.
- **Observability** should exist at multiple layers: HTTP status does not explain packet loss, and network reachability does not prove application health.
- Every proxy creates another connection, queue, timeout, capacity limit, and possible failure point.

## Quick review

1. Why does a switch not need the HTTP path to forward a frame?
2. At which layer do ports exist? At which layer do routes exist?
3. What changes when a load balancer terminates TLS?
4. Why is QUIC difficult to place into exactly one traditional OSI box?

## Key takeaways

- Layering gives independent responsibilities and reusable interfaces.
- TCP/IP combines the upper OSI layers and is the practical Internet model.
- Headers reveal which layer owns a function and where it terminates.
- Layer-by-layer reasoning makes both architecture and troubleshooting clearer.
