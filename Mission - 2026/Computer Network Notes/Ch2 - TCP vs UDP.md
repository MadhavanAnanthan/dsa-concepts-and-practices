# Chapter 2: TCP vs UDP

## Transport layer purpose

IP gets a packet to a host. A transport protocol gets application data to the correct process using **ports** and defines what delivery guarantees applications receive.

TCP and UDP are not simply “reliable versus fast.” They expose different abstractions, and the right choice depends on what the application needs.

## At a glance

| Property | TCP | UDP |
|---|---|---|
| Abstraction | Ordered byte stream | Independent datagrams |
| Connection setup | Yes | No transport handshake |
| Delivery guarantee | Retransmits losses while connection is viable | None |
| Ordering | Delivered in order | May be lost, duplicated, or reordered |
| Message boundaries | Not preserved | Preserved |
| Flow control | Yes | No |
| Congestion control | Yes | Application/protocol must provide it |
| Header overhead | Larger | 8-byte base header |
| Broadcast/multicast | No | Possible where supported |
| Typical uses | HTTP/1.1, HTTP/2, SSH, databases | DNS, voice/video, gaming, QUIC |

“UDP is faster” is incomplete. UDP has less built-in work, but an application that needs reliability, security, congestion control, and session management must implement those features itself or use a protocol such as QUIC.

## TCP: a reliable ordered byte stream

### Connection establishment

TCP normally starts with a three-way handshake:

```text
Client                                Server
  | -------- SYN, seq=x -------------> |
  | <---- SYN-ACK, seq=y, ack=x+1 ----- |
  | -------- ACK, ack=y+1 ------------> |
  |            connection ready         |
```

The handshake proves two-way reachability, synchronizes sequence-number spaces, and negotiates options. It adds roughly one RTT before ordinary application data, though mechanisms such as TCP Fast Open can alter this in supported environments.

### Reliability and ordering

TCP numbers bytes. The receiver acknowledges received ranges; the sender retransmits data inferred to be lost. Checksums detect corruption. Applications read a continuous ordered stream.

TCP does **not** preserve application message boundaries. Two calls to `send()` can arrive in one `read()`, or one send can require many reads. Applications need framing such as a length prefix, delimiter, or self-describing format.

### Sliding window and flow control

The receiver advertises how much buffer space it has. The sender limits unacknowledged bytes to avoid overwhelming the receiver. This is **flow control**, which protects an endpoint.

### Congestion control

The sender also maintains a congestion window based on perceived network capacity. This is **congestion control**, which protects the network. Actual in-flight data is limited by both receiver and congestion windows.

### Head-of-line blocking

TCP exposes ordered bytes. If one segment is missing, later bytes already received cannot be delivered to the application until the gap is repaired. With HTTP/2, many logical streams share one TCP connection, so a lost packet can temporarily delay all streams on that connection.

### Closing a connection

Each direction closes independently with FIN/ACK exchanges. Abrupt failure may use RST. The endpoint that actively closes can enter `TIME_WAIT`, preventing delayed packets from an old connection being mistaken for a new one. Large systems must plan for socket, file-descriptor, memory, and ephemeral-port limits.

## UDP: independent best-effort datagrams

UDP adds source port, destination port, length, and checksum around a message. There is no connection state in the transport protocol and no handshake.

Properties:

- One send corresponds to one datagram; message boundaries are preserved.
- A datagram that is too large risks fragmentation or loss, so applications should respect safe payload sizes.
- The receiver may get nothing, duplicates, or messages in a different order.
- “Connectionless” does not mean stateless everywhere: NATs and firewalls may create temporary mappings for a UDP flow.

UDP is useful when stale data is worse than missing data, as in a live voice frame, or when the application protocol needs custom delivery behavior.

## QUIC: reliable transport built over UDP

QUIC demonstrates that UDP can be a foundation rather than the complete application contract. QUIC provides encrypted connections, reliable streams, loss recovery, and congestion control in user space.

Its independent streams reduce cross-stream head-of-line blocking, and connection IDs allow a connection to survive some IP/port changes, such as moving from Wi-Fi to cellular. HTTP/3 runs over QUIC.

## Common misconceptions

### “TCP guarantees my request was processed exactly once”

It does not. TCP guarantees an ordered byte stream between live endpoints. If a connection fails after a request is processed but before its response reaches the client, the client cannot know what happened. Exactly-once business behavior requires application-level idempotency keys, deduplication, or transactional design.

### “One TCP write equals one packet”

No. The OS can combine or split writes based on buffering, maximum segment size, and network conditions.

### “No response means the request never arrived”

No. The request or only the response may have been lost. This ambiguity is central to distributed systems.

### “UDP has zero latency”

It avoids a transport handshake, but routing, queues, serialization, server work, and any application-level acknowledgement still take time.

## Choosing a transport

Use TCP, or a TCP-based application protocol, when:

- every byte must arrive in order;
- mature OS support and compatibility matter;
- the application benefits from a simple stream abstraction.

Use UDP directly when:

- the application tolerates loss or handles selective recovery;
- message timeliness matters more than perfect ordering;
- multicast/broadcast or very small request-response exchanges are needed;
- you are prepared to implement rate control, security, and reliability where required.

Use QUIC/HTTP/3 when its ecosystem support, reduced handshake cost, and independent streams justify it.

## System-design patterns

### Connection pooling

Creating a TCP and TLS connection per request wastes RTTs and CPU. Clients reuse a bounded pool. Pools need connect timeouts, idle timeouts, health handling, and limits so one dependency cannot consume every socket.

### Timeouts

Use separate timeouts where possible:

- connection timeout;
- TLS handshake timeout;
- request/read timeout;
- idle connection timeout;
- total deadline propagated across service calls.

### Retries

Retry only failures likely to be transient. Use exponential backoff plus jitter, a maximum attempt count, and a retry budget. Retrying non-idempotent operations without a key can duplicate payments or orders. Too many retrying clients create a **retry storm**.

### Keepalive and failure detection

An idle TCP connection can appear alive after a peer or network silently disappears. TCP keepalive or application heartbeats can detect stale connections, but settings must balance detection time and traffic.

## Example: video call versus file upload

A file upload requires every byte, so TCP or a reliable QUIC stream fits. A live video call can discard an old frame rather than pausing playback to recover it; real-time media often uses UDP with timestamps, jitter buffers, selective recovery, and adaptive bitrate.

## Quick review

1. Why must a TCP application define its own message framing?
2. What is the difference between flow control and congestion control?
3. Why can retrying after a TCP timeout duplicate a business action?
4. How does QUIC reduce head-of-line blocking between HTTP streams?

## Key takeaways

- TCP provides a reliable ordered byte stream, not message or exactly-once semantics.
- UDP provides small, independent best-effort datagrams and leaves policy to higher layers.
- Performance depends on RTTs, loss, congestion, implementation, and application behavior—not just header size.
- Robust systems combine transport with deadlines, bounded pools, safe retries, idempotency, and observability.
