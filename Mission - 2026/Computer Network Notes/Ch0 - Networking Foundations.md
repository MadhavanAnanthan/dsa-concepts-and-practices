# Chapter 0: Networking Foundations

## Goal

Build a mental model of how bytes move from one application to another. Networking is not one magical connection: every hop and layer performs a small job, and the complete path emerges from those jobs working together.

## 1. What is a computer network?

A computer network is a group of devices that exchange data over a communication medium using agreed rules called **protocols**.

- **End hosts:** laptops, phones, servers, containers, and IoT devices where applications run.
- **Links:** Ethernet, Wi-Fi, fiber, cellular, or virtual links that carry signals.
- **Switches:** move frames within a local network by using MAC addresses.
- **Routers:** move packets between different IP networks.
- **Middleboxes:** firewalls, NAT gateways, proxies, and load balancers that inspect or transform traffic.

The Internet is a **network of networks**. Each organization operates its own network and connects it to other networks through routers and Internet service providers.

## 2. The unit changes at each layer

When an application sends a message, every layer wraps it with metadata needed for its job. This is called **encapsulation**.

```text
Application data:  HTTP request
Transport segment: [TCP header | HTTP request]
Network packet:    [IP header | TCP header | HTTP request]
Link frame:        [Ethernet header | IP packet | trailer]
Physical signal:   bits represented as electrical/light/radio signals
```

The receiver reverses this process, called **decapsulation**. A common generic term for any layer's unit is **PDU** (Protocol Data Unit).

Important consequence: a 1 MB application message may become many packets because every link has a maximum frame size.

## 3. Addresses and identifiers

Different identifiers solve different scopes of the delivery problem.

| Identifier | Scope | Example | Main purpose |
|---|---|---|---|
| Domain name | Human/application | `api.example.com` | Stable, memorable service name |
| IP address | Across networks | `203.0.113.10` | Locate and route to a host/interface |
| Port | Inside a host | `443` | Select the destination process/service |
| MAC address | One local link | `3C:52:82:...` | Deliver a frame to the next local device |

A socket endpoint is commonly represented as `(IP address, port, transport protocol)`. A TCP connection is uniquely identified by the source IP, source port, destination IP, and destination port.

### IPv4 and IPv6

- **IPv4** uses 32-bit addresses, such as `192.0.2.20`. Its limited address space led to widespread NAT.
- **IPv6** uses 128-bit addresses, such as `2001:db8::20`, providing vastly more addresses and simpler end-to-end addressing.

Addresses belong to interfaces, not abstract machines. A laptop can have one address on Wi-Fi and another on Ethernet.

## 4. Local delivery versus routed delivery

A host uses its IP address and subnet prefix to decide whether a destination is local.

Example: host `192.168.1.20/24` treats `192.168.1.80` as local, but sends traffic for `8.8.8.8` to its **default gateway**.

For local IPv4 delivery, the sender uses **ARP** to discover the MAC address associated with a local IP. IPv6 uses **Neighbor Discovery**. For a remote destination, it discovers the gateway's MAC address. The Ethernet MAC addresses normally change at every routed hop; the end-to-end IP addresses normally remain the same unless NAT is involved.

```text
Laptop --frame--> Home router --packet--> ISP routers --> Server network
       gateway MAC             routing decisions             server MAC
```

## 5. Switching and routing

### Switch

A Layer-2 switch learns which MAC addresses appear on which ports. It forwards a frame only to the relevant port when it knows the destination, and floods certain unknown or broadcast frames inside the broadcast domain.

### Router

A router reads the destination IP, performs a longest-prefix match in its routing table, decrements the packet's TTL/hop limit, and forwards it to a next hop. Routers choose the next step, not the entire end-to-end path.

Key distinction:

- A **switch** connects devices in the same LAN.
- A **router** connects different IP networks and separates broadcast domains.

## 6. Subnets and CIDR

CIDR notation describes how many leading address bits identify a network.

- `10.0.0.0/8` has a large address range.
- `10.20.0.0/16` is a smaller range inside it.
- `10.20.30.0/24` commonly provides 256 total IPv4 addresses.

Subnetting matters in system design because it controls address allocation, routing boundaries, fault isolation, and security policy. Cloud VPCs use the same idea: public/private subnets are routing and policy arrangements, not different kinds of IP addresses.

## 7. NAT and private networks

Private IPv4 ranges such as `10.0.0.0/8`, `172.16.0.0/12`, and `192.168.0.0/16` are not routed on the public Internet. A NAT gateway can translate many internal connections to one public address by maintaining a mapping of internal and external address/port tuples.

Benefits include conserving IPv4 addresses and hiding internal addressing. Costs include state at the gateway, harder inbound connectivity, and weakening the original end-to-end model. NAT is not a replacement for a firewall.

## 8. Common network measurements

- **Bandwidth:** maximum data-carrying capacity, usually bits per second.
- **Throughput:** rate actually achieved by an application.
- **Goodput:** useful application bytes delivered per second, excluding headers and retransmissions.
- **Latency:** time required for data to travel; often discussed as round-trip time (RTT).
- **Jitter:** variation in latency, important for voice, video, and gaming.
- **Packet loss:** percentage of packets that never arrive.

Approximate request latency includes DNS lookup, connection establishment, TLS, server processing, queueing, and data transfer. High bandwidth does not guarantee low latency.

### Bandwidth-delay product

`bandwidth × RTT` estimates how much data must be in flight to fully use a path. A 1 Gbit/s link with a 100 ms RTT needs about 12.5 MB in flight. This is why transport windows matter on fast, long-distance paths.

## 9. Packet size, MTU, and fragmentation

The **MTU** is the largest network-layer packet a link can carry in one frame; Ethernet commonly uses a 1500-byte MTU. Transport protocols split large data into suitable units. Oversized IP packets may be fragmented or dropped, depending on the IP version and path behavior.

Production systems normally rely on Path MTU Discovery and avoid IP fragmentation. Broken discovery can cause a confusing failure where small messages work but large ones stall.

## 10. Unicast, broadcast, multicast, and anycast

- **Unicast:** one sender to one receiver; most Internet traffic.
- **Broadcast:** one sender to all hosts in a local broadcast domain; IPv4 ARP is an example.
- **Multicast:** one sender to an interested group; useful in controlled networks.
- **Anycast:** the same IP prefix is advertised from multiple locations; routing sends a client toward a nearby instance. DNS and CDN providers often use it.

## 11. What happens when a browser opens a URL?

For `https://shop.example.com/products`:

1. The browser parses the URL: scheme `https`, host, and path.
2. DNS resolves the host to one or more IP addresses.
3. The OS decides whether the IP is local and selects a route/gateway.
4. ARP or Neighbor Discovery finds the next-hop link-layer address.
5. The client establishes TCP plus TLS, or QUIC for HTTP/3.
6. The browser sends an HTTP request.
7. Routers forward packets; NAT, firewalls, load balancers, or proxies may process them.
8. The service returns a response, possibly through a CDN/cache.
9. The browser decodes content and may open/reuse connections for more resources.

This flow is the backbone of the remaining chapters.

## 12. System-design connections

Networking choices directly affect architecture:

- Put services in multiple failure domains and expect links to fail.
- Set timeouts because a remote call can wait indefinitely from the caller's perspective.
- Use retries only with backoff, jitter, retry budgets, and idempotent operations.
- Reduce round trips on high-latency paths through connection reuse, batching, or edge placement.
- Observe latency percentiles, errors, saturation, retransmissions, and connection counts—not averages alone.
- Treat every network boundary as a security boundary; authenticate and encrypt sensitive traffic.

The **fallacies of distributed computing** begin with “the network is reliable.” It is not: packets can be delayed, duplicated, reordered, corrupted, or lost, and endpoints can fail independently.

## Quick review

1. Why does a remote packet use the gateway's MAC address but the server's IP address?
2. What is the difference between bandwidth, throughput, and latency?
3. Why can NAT become a scalability bottleneck?
4. Which parts of a request path are reduced by placing a service at the edge?

## Key takeaways

- Names identify services, IP addresses enable routing, ports identify processes, and MAC addresses deliver on a local link.
- Data is encapsulated as it moves down the protocol stack and decapsulated at the receiver.
- Switches handle local frames; routers connect IP networks.
- Real systems must be designed for latency, loss, partial failure, and limited capacity.
