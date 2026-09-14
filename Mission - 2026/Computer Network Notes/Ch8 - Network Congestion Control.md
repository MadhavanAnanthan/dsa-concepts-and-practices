# Chapter 8: Network Congestion Control

## Congestion versus flow control

Congestion occurs when offered traffic exceeds a network resource's capacity. Packets build in queues, latency rises, buffers overflow, packets are dropped, and retransmissions can add even more traffic.

- **Flow control** prevents a fast sender from overwhelming a particular receiver.
- **Congestion control** prevents senders from overwhelming the network path.
- **Application backpressure** prevents producers from overwhelming downstream services or queues.

All three limit sending, but they protect different resources.

## Why congestion collapses systems

Consider a router that can transmit 1,000 packets/s while inputs offer 1,500 packets/s. Its buffer temporarily absorbs the difference. Once full, packets drop. Senders retransmit, adding traffic that may also be dropped. Useful throughput can fall even while total transmitted work rises.

In a service architecture the same pattern appears when a database can serve 10,000 queries/s but clients generate 15,000 plus retries. Network congestion control cannot protect an overloaded database; applications also need admission control and backpressure.

## The signals available to a sender

### Packet loss

Traditional TCP treats loss as a sign that the path may be full. Loss can also come from wireless errors or broken links, so it is an imperfect signal.

### Delay

Growing RTT can indicate packets are spending longer in queues. Delay-based approaches try to slow down before buffers overflow.

### Explicit Congestion Notification (ECN)

When supported, network devices mark packets instead of dropping them to signal congestion. Endpoints then reduce load. This can preserve throughput and reduce loss, but requires compatible configuration along the path.

## TCP's control model

TCP limits in-flight data with a **congestion window** (`cwnd`). The usable sending limit is approximately:

```text
in-flight bytes ≤ min(congestion window, receiver advertised window)
```

The exact algorithms vary, but the classic mental model is useful.

### Slow start

A new connection does not know path capacity. It begins with a limited window and increases rapidly as acknowledgements arrive. “Slow” is historical wording; growth is exponential by RTT until a threshold or congestion signal.

### Congestion avoidance

After reaching a threshold, classic algorithms grow more cautiously. An additive-increase/multiplicative-decrease pattern probes for capacity and backs off significantly on congestion.

### Loss recovery

Duplicate acknowledgements or selective acknowledgement information can reveal a gap before a timer expires. A retransmission timeout handles cases without sufficient feedback but incurs a larger pause.

Modern senders may use loss-based algorithms such as CUBIC or model-based algorithms such as BBR. The important system-design idea is that a new or recently idle connection must learn path conditions, and its rate changes over time.

## Bandwidth-delay product and windows

To fill a path, the sender needs enough unacknowledged data to cover bandwidth multiplied by RTT.

Example:

```text
100 Mbit/s × 0.080 s = 8 Mbit ≈ 1 MB in flight
```

If transport or socket windows are much smaller, the connection cannot use the full link even with no loss. This matters for replication, backups, and data transfer across regions.

## Queues and bufferbloat

Buffers absorb short bursts, but oversized unmanaged buffers can stay full and add hundreds of milliseconds of delay. This is **bufferbloat**: throughput may look fine while interactive traffic feels terrible.

Active Queue Management (AQM) schemes drop or mark packets before a queue is completely full. Fair queueing can isolate flows so one bulk transfer does not dominate latency-sensitive traffic.

## Fairness and many connections

Congestion control generally operates per flow. An application that opens many parallel connections may obtain more total bandwidth than one competing connection. This can improve a single transfer but harm fairness and overload endpoints.

HTTP/2/3 multiplexing reduces the need for many connections, but application concurrency still needs bounds. QUIC removes cross-stream transport blocking, not bandwidth competition.

## Application-level overload control

Distributed systems need mechanisms above transport:

### Bounded concurrency

Limit simultaneous work per instance and per dependency. A bounded queue absorbs short bursts; an unbounded queue converts overload into high memory use and deadline violations.

### Backpressure

Consumers signal producers to slow down or stop. Streaming protocols use windows/demand; message systems pause consumers or bound outstanding messages. Backpressure must propagate toward the source to be effective.

### Rate limiting

Token bucket allows controlled bursts while enforcing an average rate. Leaky bucket shapes output more steadily. Limits can be per user, API key, tenant, region, or global, and should return clear signals such as HTTP `429` where appropriate.

### Load shedding

Reject low-priority or excess work early so admitted requests can succeed. A fast rejection is often better than waiting until every request times out.

### Deadlines and cancellation

Propagate a total time budget. If the caller has abandoned a request, cancel downstream work rather than consuming scarce capacity for a result nobody will use.

### Adaptive concurrency

Systems can adjust allowed in-flight work based on latency or queueing signals. Guardrails are necessary because feedback loops can oscillate.

## Retry storms and congestion

A timeout does not mean the server did no work. During slowness, immediate retries increase load, cause more timeouts, and produce a positive feedback loop.

Safe retry design uses:

- exponential backoff;
- random jitter so clients do not synchronize;
- maximum attempts and a total deadline;
- retry budgets;
- idempotency for mutations;
- circuit breakers or load-shed signals;
- honoring `Retry-After` where provided.

Hedged requests can reduce tail latency by sending a duplicate after a delay, but they deliberately add load. Use them only for idempotent operations, with budgets, and when spare capacity exists.

## Congestion in data centers

Data-center links have low base latency, so even short queues matter. Many workers can simultaneously send to one receiver, causing an **incast** burst and switch-buffer overflow. Examples include distributed storage reads and shuffle stages.

Mitigations include pacing, limiting fan-out/concurrency, spreading work over time, ECN/AQM, fair queueing, and designing aggregators that do not require every response at the same instant.

## Capacity planning

Average traffic is not enough. Plan for:

- peak and burst rates;
- traffic growth;
- loss of a zone/link/backend;
- deployments and cache cold starts;
- request-size and cost distributions;
- retry and replication amplification;
- background jobs competing with user requests.

Keep headroom. A system at 100% utilization has no room for variance, failover, or recovery.

## Observability

At the network/transport level, watch:

- RTT and its variation;
- packet loss, retransmissions, and timeouts;
- throughput/goodput;
- congestion and receive windows;
- ECN marks;
- interface and queue drops;
- connection setup failures.

At the application level, watch:

- arrival rate, admitted rate, and rejected rate;
- in-flight requests and queue depth;
- latency percentiles, not only averages;
- timeout and retry counts;
- dependency saturation;
- work completed after caller cancellation.

Correlate layers. Rising HTTP latency with stable server CPU but increasing RTT/retransmissions suggests a different problem than rising database queue depth.

## Example overload sequence

```text
traffic spike
  → service queue grows
  → latency exceeds client timeout
  → clients retry
  → arrival rate grows further
  → dependencies saturate
  → broad outage
```

Break the loop with early admission limits, deadline propagation, bounded retries, backoff/jitter, and graceful degradation.

## Quick review

1. How do flow control, congestion control, and backpressure differ?
2. Why can a large router buffer increase latency without improving useful throughput?
3. Why do retries synchronize without jitter?
4. Why should a service reject some work during overload?

## Key takeaways

- Congestion is a feedback problem: too much offered load creates queues, loss, and potentially more load.
- Transport algorithms learn available path capacity through acknowledgement, delay, loss, or ECN signals.
- Bounded concurrency, backpressure, rate limits, shedding, deadlines, and disciplined retries extend the same control principles to services.
- Capacity headroom and cross-layer observability are required for reliable system behavior.
