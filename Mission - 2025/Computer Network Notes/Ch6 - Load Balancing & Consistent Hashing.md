# Chapter 6: Load Balancing and Consistent Hashing

## Why load balancing exists

One server has limited CPU, memory, network capacity, and availability. A load balancer presents one service endpoint and distributes work across multiple backends.

Load balancing supports:

- horizontal scaling;
- failure detection and traffic removal;
- rolling deployment and draining;
- geographic or availability-zone distribution;
- TLS termination and policy enforcement;
- a stable endpoint while backend instances change.

It does not create capacity by itself. If every backend is saturated or a shared database is the bottleneck, redistributing requests cannot fix the underlying limit.

## Where load balancing can happen

### DNS-level

DNS returns one or more addresses, possibly using weights, health, or geography. It scales well but reacts slowly because of caches and existing connections.

### Layer 4

An L4 load balancer routes TCP/UDP flows using addresses, ports, and connection state. It is fast and works without understanding HTTP. A given connection normally stays with one backend.

### Layer 7

An L7 proxy parses application protocol data and can route by hostname, path, method, header, cookie, or tenant. It can terminate TLS, authenticate, cache, rate-limit, and add tracing, at the cost of more CPU and complexity.

### Client-side

A client discovers instances and chooses one directly. This removes a central data-plane hop and can use local load information, but pushes discovery, balancing, retries, and policy into every client library.

Large architectures often combine these: DNS selects a region, a regional L4 balancer accepts connections, and an L7 gateway routes requests to services.

## Common algorithms

| Algorithm | Best fit | Main limitation |
|---|---|---|
| Round robin | Similar backends and request costs | Ignores active work |
| Weighted round robin | Different backend capacities | Static weights may become stale |
| Random | Simple distribution at scale | Ignores current load |
| Power of two choices | Large dynamic pools | Needs two load samples |
| Least connections | Connections approximate work | Long-lived/idle connections can mislead |
| Least response time | Latency reflects load | Feedback can oscillate; needs measurement |
| Hash by key | Stable affinity | Rebalancing and hot keys |

The **power of two choices** samples two backends and selects the less loaded one. It often achieves much better balance than pure random selection with little coordination.

## Health checks

- A **liveness** check asks whether an instance should be restarted or removed.
- A **readiness** check asks whether it can accept new traffic now.
- A **startup** check allows slow initialization without premature failure.

Checks should be cheap and representative. A shallow check can route traffic to a process unable to serve; a deep check that fails whenever one optional dependency fails can remove every instance at once.

Use thresholds so one missed probe does not flap an instance. Health checks are sampled signals, not proof that the next request will succeed.

## Draining and graceful changes

For deployment or shutdown:

1. mark the instance unready so it receives no new traffic;
2. allow in-flight requests and long-lived connections time to finish;
3. close or migrate remaining work after a deadline;
4. stop the process.

Without draining, rolling deployments can create avoidable errors even when enough capacity remains.

## Session affinity

Sticky sessions route a client to the same backend, often by cookie or source hash. They can support in-memory sessions, but create uneven load, harder failover, and deployment friction.

Prefer stateless application servers where practical. Store session data in a shared system or use self-contained, carefully secured tokens. Affinity remains useful for caches, streaming, and workloads where locality has measurable value.

## Proxy details that affect correctness

### Source identity

A proxy creates a new connection, so the backend may see the proxy's IP. Trusted L7 proxies can add standardized `Forwarded` or `X-Forwarded-For` headers. Never trust these headers from arbitrary Internet clients.

### Connection reuse and multiplexing

The number of client requests is not the number of backend connections. A proxy may reuse pools or multiplex streams. A balancing decision made per connection can become uneven when a few connections carry very different numbers of requests.

### Timeouts and limits

Align connect, idle, request, and upstream timeouts deliberately. Bound headers, bodies, queues, connections, and concurrent streams. A queue hides short bursts but increases latency and can turn overload into timeouts.

## Consistent hashing

Normal modulo sharding chooses:

```text
serverIndex = hash(key) mod N
```

When `N` changes, most keys remap. For a cache fleet, that can cause a huge cache miss storm.

Consistent hashing places both servers and keys on a logical hash ring. A key goes to the first server clockwise from its hash position.

```text
             S1
        K4        K1
    S4                S2
        K3        K2
             S3
```

When a server is added or removed, mostly the neighboring portion of the ring moves instead of nearly every key.

### Virtual nodes

Each physical server owns many positions called virtual nodes. They smooth distribution, allow weights proportional to capacity, and reduce the chance that one server receives a very large range.

### Replication

For fault tolerance, a key can map to the next `R` distinct servers on the ring. Replicas should span physical failure domains. Replication introduces questions about consistency, repair, conflict resolution, and failover.

### Limitations

- A popular key can overload one owner even with a perfectly balanced ring.
- Membership changes still move some data and need controlled handoff.
- Different clients must agree on membership and hash functions.
- Unequal records or request costs can make key-count balance misleading.

Alternatives include rendezvous hashing, which scores every server for a key and selects the highest-scoring one. It often simplifies membership and replication selection.

## Consistent hashing versus load balancing

They solve related but different problems:

- Load balancing spreads interchangeable requests based on current or configured capacity.
- Consistent hashing preserves key-to-node affinity while membership changes.

Use consistent hashing for distributed caches, partition ownership, or locality-sensitive state. Do not use it automatically for stateless web requests: least-loaded or round-robin routing may balance work better.

## Overload and failure behavior

### Retry amplification

If a request passes through multiple layers and each retries three times, a single user request can multiply into many backend attempts. Put retries at a deliberate layer, use budgets/backoff/jitter, and stop when the deadline is exhausted.

### Slow backends

Slow instances accumulate more concurrent work. Least-connections or latency-aware algorithms can help, but the service also needs deadlines, circuit breaking, concurrency limits, and load shedding.

### Thundering herd

When a backend recovers, immediately sending all traffic to it can cause another failure. Warm it gradually and use slow start/ramped weights.

### Load-balancer failure

Avoid a single control or data-plane instance. Use redundant balancers, anycast/virtual IP failover, multiple zones, and tested configuration rollback. A bad global configuration can defeat hardware redundancy.

## Capacity and observability

Monitor:

- requests/connections per backend;
- active and queued work;
- latency and errors by backend and route;
- health-check transitions and flapping;
- rejected requests and retry counts;
- connection-pool saturation;
- distribution skew and hot keys;
- load-balancer CPU, memory, network, and file descriptors.

Keep spare capacity for failures and deployments. If a three-zone service runs each zone at 90%, losing one zone cannot be absorbed safely.

## Quick review

1. Why can L4 connection balancing be uneven for HTTP/2 traffic?
2. What is the risk of making readiness depend on every downstream service?
3. Why does consistent hashing reduce cache disruption when nodes change?
4. How can retries make a partial overload become a full outage?

## Key takeaways

- Choose the balancing layer and algorithm based on the traffic abstraction and available signals.
- Readiness, draining, timeouts, and bounded queues are essential to safe operation.
- Consistent hashing provides stable key affinity, not automatic load equality.
- Plan for skew, hot keys, balancer failure, retry amplification, and reduced capacity during failures.
