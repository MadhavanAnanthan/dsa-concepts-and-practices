# Chapter 09: The Trouble with Distributed Systems - Compressed Study Notes

Book: DDIA
Chapter: 9, The Trouble with Distributed Systems
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 369-413
Raw source used: books/DDIA/raw/chapter-09-chapter-9-the-trouble-with-distributed-systems.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 9 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter frame: pessimism as a reliability skill | 369 |
| Faults and partial failures | 370-371 |
| Unreliable networks and asynchronous packet delivery | 371-372 |
| Limitations of TCP | 372-374 |
| Network faults in practice | 374-375 |
| Fault detection | 375-376 |
| Timeouts and unbounded delays | 376-379 |
| Synchronous versus asynchronous networks | 379-382 |
| Unreliable clocks | 382 |
| Monotonic versus time-of-day clocks | 383-384 |
| Clock synchronization and accuracy | 384-386 |
| Relying on synchronized clocks | 386-390 |
| Process pauses and leases | 390-393 |
| Response time guarantees and garbage collection | 393-395 |
| Knowledge, truth, and lies | 395-396 |
| Majority rules and quorums | 396-397 |
| Distributed locks, leases, zombies, and fencing | 397-401 |
| Byzantine faults | 401-404 |
| System model and reality | 404-408 |
| Formal methods, fault injection, and randomized testing | 408-411 |
| The power of determinism | 411-412 |
| Chapter summary | 412-413 |

## 1. Chapter Frame: Distributed Systems Require Pessimism

Source pages: 369

- Reliability means the system as a whole keeps working when faults happen.
- The chapter argues that developers must shift from happy-path thinking to fault-oriented thinking.
- Rare events matter at scale: a low-probability event can become routine in a large enough system.
- Distributed systems differ from single-machine software because networks, clocks, and nodes can fail independently and ambiguously.
- The chapter prepares for Chapter 10 by cataloging the problems that fault-tolerant algorithms must handle.

Interview hook: The first mental shift is that distributed systems are built for a world where "probably fine" is not a correctness argument.

## 2. Faults and Partial Failures

Source pages: 370-371

- A single computer usually behaves as fully working or fully failed, at least from the software's point of view.
- Computer design intentionally hides messy physical reality behind deterministic abstractions: CPU instructions, memory reads, and disk writes are expected to behave predictably.
- Distributed systems expose more physical uncertainty because components can fail independently.
- A partial failure means one part of the system is broken while other parts keep running.
- Partial failures are nondeterministic:
  - An operation involving several nodes can succeed sometimes and fail other times.
  - The caller may not know whether an operation succeeded.
- Fault-tolerant distributed systems can be more reliable than single-node systems because unreliable components can be combined with redundancy.
- The cost is design complexity: failures must be anticipated, tested, and handled.

Interview hook: Partial failure is the defining difficulty of distributed systems. It is not merely "a server crashed"; it is "some participants may continue while others cannot tell what happened."

## 3. Unreliable Networks

Source pages: 371-372

- The chapter focuses on shared-nothing systems: machines with their own memory and disk communicating only by network messages.
- Most datacenter and internet networks are asynchronous packet networks.
- An asynchronous packet network gives no guarantee about:
  - Whether a packet arrives.
  - When it arrives.
  - Whether a reply arrives.
- If a request gets no response, the sender cannot distinguish:
  - Request lost.
  - Request delayed.
  - Remote node crashed.
  - Remote node paused.
  - Response lost.
  - Response delayed.
- Timeout is the ordinary fallback: after waiting long enough, the caller gives up.
- A timeout does not prove the request failed; the request may still be delivered or processed later.

Interview hook: "No response" is ambiguous. It is an observation, not a diagnosis.

## 4. The Limitations of TCP

Source pages: 372-374

- TCP is useful because it turns a byte stream into packets, retransmits missing packets, reorders packets, detects some corruption, and applies congestion control/backpressure.
- TCP "reliability" is connection-level reliability, not end-to-end application certainty.
- Important limitations:
  - TCP cannot know whether the outbound packet or acknowledgement was lost.
  - TCP cannot make an unplugged or broken network deliver packets.
  - If a connection fails, the application may not know how much data the remote node actually processed.
  - A TCP acknowledgement means the remote operating system received data, not that the application processed the request.
  - If the application reconnects and retransmits, duplicates can occur.
- Application-level acknowledgement is needed to know a request succeeded.
- Protocols often frame messages by sending a length header followed by the bytes of the message.

Interview hook: TCP solves packet handling within a connection; it does not solve distributed request semantics.

## 5. Network Faults in Practice

Source pages: 374-375

- Network faults are common enough that software must handle them.
- The chapter lists real and studied failure patterns:
  - Single-machine disconnects.
  - Rack-level disconnects.
  - Switch, load balancer, and human configuration errors.
  - Wide-area fiber disruptions.
  - Cloud-region round-trip times that can reach minutes at high percentiles.
  - Datacenter packet delays beyond a minute during reconfiguration.
  - Partial communication failures where A can talk to B and B can talk to C, but A cannot talk to C.
  - One-way link behavior, where sending and receiving do not fail symmetrically.
- A network partition or netsplit is one kind of network interruption; it is unrelated to sharding partitions.
- Undefined network-fault handling can lead to deadlock, unrecoverable clusters, or data loss.
- Handling a network fault does not always mean hiding it from users; showing an error can be valid if the system recovers cleanly.
- The system's behavior under network faults should be deliberately tested.

Interview hook: "The network is reliable" is a dangerous assumption even inside a datacenter.

## 6. Fault Detection

Source pages: 375-376

- Many systems need automatic fault detection:
  - Load balancers remove dead nodes from rotation.
  - Replicated databases promote a follower when the leader fails.
- Some failures provide explicit feedback:
  - TCP RST/FIN when no process is listening.
  - Local process crash notification scripts.
  - Switch management interfaces showing hardware-level link failures.
  - ICMP unreachable messages.
- But explicit feedback cannot be relied upon.
- In the general case, the system retries, waits for a timeout, and eventually suspects the node.
- Timeout selection balances:
  - False positives: alive-but-slow nodes declared dead.
  - False negatives: dead nodes detected too late.
- Premature suspicion can cause duplicate work, extra load, and cascading failure.

Interview hook: Failure detection is suspicion management, not perfect diagnosis.

## 7. Timeouts and Unbounded Delays

Source pages: 376-379

- In a hypothetical bounded-delay system, a timeout could be calculated as a known network round trip plus known request-processing time.
- Real asynchronous networks usually lack those bounds.
- Queueing is the major source of variable network delay:
  - Switch queues fill when many senders target the same destination.
  - Destination operating systems queue incoming work when CPU cores or threads are busy.
  - Virtual machines can be paused while another VM runs.
  - TCP queues data at the sender because of congestion control.
  - TCP retransmission hides packet loss from the application but exposes extra delay.
- UDP avoids TCP retransmission and flow-control delay, so it can be better for data that is worthless if late, such as voice packets.
- UDP still suffers from queues and scheduling delays.
- In shared cloud and multitenant environments, noisy neighbors can make network delay highly variable.
- Timeout values should be chosen empirically by measuring round-trip distributions and considering application tolerance for delayed detection versus premature suspicion.
- Adaptive failure detectors can continually adjust timeout behavior based on observed response times and jitter.

Interview hook: A timeout is a policy choice under uncertainty, not a truth boundary.

## 8. Synchronous Versus Asynchronous Networks

Source pages: 379-382

- Telephone networks historically used circuit switching:
  - A fixed bandwidth allocation is reserved for the duration of a call.
  - Since capacity is reserved, queueing is avoided and delay can be bounded.
- TCP connections are not circuits:
  - They opportunistically use available bandwidth.
  - They consume little when idle.
  - They adapt dynamically to capacity.
- Ethernet and IP use packet switching, optimized for bursty traffic such as web pages, email, and file transfer.
- Packet switching improves utilization but introduces queueing and unbounded delay.
- Circuit-style guarantees require static resource partitioning, which reduces utilization and increases cost.
- Dynamic resource sharing gives better utilization and lower cost but variable delays.
- QoS, admission control, and related mechanisms can reduce delay variability in some environments, but the chapter says they are not generally available across multitenant datacenters, public clouds, or the internet.
- Therefore, most systems must assume congestion, queueing, and unbounded delays.

Interview hook: Unbounded network delay is partly an economic tradeoff: cheaper dynamic sharing instead of expensive reserved capacity.

## 9. Unreliable Clocks

Source pages: 382

- Applications use clocks for durations and points in time.
- Duration questions include timeouts, response-time percentiles, throughput over an interval, and time spent on a site.
- Point-in-time questions include publish timestamps, scheduled reminders, cache expiry, and log timestamps.
- Distributed time is hard because:
  - Message travel takes time.
  - Network delay varies.
  - Each machine has its own hardware clock.
  - Hardware clocks drift.
  - Synchronization is imperfect.
- NTP can adjust machine clocks using time servers, but it does not eliminate uncertainty.

Interview hook: In distributed systems, "what time is it?" and "which event happened first?" are different questions.

## 10. Monotonic Versus Time-of-Day Clocks

Source pages: 383-384

### Time-of-day clocks

- A time-of-day clock reports calendar time.
- It is useful for timestamps that humans interpret.
- It is often synchronized by NTP.
- It can jump backward or forward because of clock corrections, leap seconds, or timezone/DST behavior if UTC is not used.
- It is unsuitable for measuring elapsed time.

### Monotonic clocks

- A monotonic clock is designed for durations such as timeouts and response times.
- Its absolute value is meaningless; only differences between two readings on the same machine matter.
- Monotonic readings from different machines should not be compared.
- NTP can slew the rate of a monotonic clock, but should not make it jump backward or forward.
- Monotonic clocks are usually the right tool for local elapsed-time measurement in distributed systems.

Interview hook: Use time-of-day clocks for "when"; use monotonic clocks for "how long."

## 11. Clock Synchronization and Accuracy

Source pages: 384-386

- Time-of-day clocks need external synchronization to be useful across machines.
- Sources of inaccuracy include:
  - Quartz clock drift, affected by factors such as temperature.
  - Refusal to synchronize or forced reset when local time differs too much.
  - Firewalls or misconfiguration blocking NTP.
  - Network delay limiting NTP accuracy.
  - Misconfigured NTP servers.
  - Leap seconds.
  - Virtualized clocks and VM pauses.
  - Untrusted clocks on user-controlled devices.
- Very accurate synchronization is possible with investment in special hardware, PTP, GPS or atomic clocks, deployment discipline, and monitoring.
- The chapter treats clock synchronization as possible but operationally fragile.

Interview hook: Clock resolution and clock accuracy are different. A nanosecond timestamp can still be wrong by milliseconds or more.

## 12. Relying on Synchronized Clocks

Source pages: 386-390

- Robust software must assume clocks can be wrong, just as it assumes networks can fail.
- Incorrect clocks can be subtle because most things keep working while the clock drifts.
- Systems that require synchronized clocks should monitor clock offsets and remove nodes whose clocks drift too far.

### Timestamps for ordering events

Source pages: 386-388

- It is tempting to use time-of-day timestamps to order writes across nodes.
- The chapter's multi-leader example shows a causally later increment receiving an earlier timestamp due to clock skew.
- With last-write-wins conflict resolution, the causally later value can be discarded, losing an update.
- Problems with timestamp ordering:
  - Writes can disappear silently when a lagging clock cannot beat a faster earlier timestamp.
  - LWW cannot distinguish sequential writes from truly concurrent writes.
  - Equal timestamps require tiebreakers, which can still violate causality.
- Logical clocks are safer for ordering events because they track relative order rather than wall-clock time.

### Clock readings with confidence intervals

Source pages: 388-389

- A clock reading should be treated as a range, not an exact point.
- The confidence interval depends on the time source, drift since synchronization, uncertainty of the server, and network round-trip time.
- Most clock APIs do not expose uncertainty.
- TrueTime and ClockBound are cited as APIs that expose an interval such as earliest/latest possible time.

### Synchronized clocks for global snapshots

Source pages: 389-390

- MVCC usually needs monotonically increasing transaction IDs.
- In a distributed database, global transaction IDs are hard because they require coordination and must reflect causality.
- Spanner uses clock uncertainty intervals for snapshot isolation across datacenters.
- If two time intervals do not overlap, their order is certain.
- Spanner waits out the uncertainty interval before committing a read/write transaction so later transactions do not overlap in uncertainty.
- Accurate clock sources reduce the waiting period but the core requirement is knowing the uncertainty interval.

Interview hook: Wall-clock timestamps are dangerous for causality unless the system explicitly accounts for uncertainty.

## 13. Process Pauses

Source pages: 390-393

- Leases are locks with timeouts; they are often used to decide which node may act as leader.
- A lease-checking loop can be unsafe even if it checks whether the lease is valid before processing.
- The unsafe gap is between the check and the action: the process can pause after checking and resume after the lease has expired.
- Sources of long or arbitrary pauses include:
  - Lock or queue contention.
  - Stop-the-world garbage collection.
  - VM suspend/resume or live migration.
  - Laptop or phone suspend/resume.
  - OS context switching, hypervisor scheduling, and steal time.
  - Synchronous disk I/O.
  - Network-backed storage I/O.
  - Paging and thrashing.
  - SIGSTOP and later SIGCONT.
- A paused node may be declared dead by the rest of the system while it later resumes without noticing.
- Distributed systems cannot use single-machine thread-safety tools directly because they have no shared memory.

Interview hook: A node must assume it can be paused at any instruction, while the rest of the world keeps moving.

## 14. Response Time Guarantees and Garbage Collection

Source pages: 393-395

- Hard real-time systems guarantee responses before specified deadlines.
- Real-time does not mean high throughput; it means deadline correctness.
- Real-time guarantees require support across the stack:
  - Real-time operating system scheduling.
  - Worst-case execution-time documentation.
  - Restrictions on dynamic allocation.
  - Careful testing and measurement.
- These systems are expensive and restrictive, so they are usually used for safety-critical embedded contexts.
- Server-side data systems generally do not use hard real-time guarantees, so they must tolerate pauses.
- Garbage collection impact can be reduced by:
  - Using modern low-pause collectors.
  - Choosing languages without garbage collection.
  - Reusing object pools or allocating off-heap.
  - Draining traffic before a planned GC pause.
  - Restarting processes periodically before costly full collections.
- These mitigations reduce pause impact but do not eliminate all pauses.

Interview hook: Hard real-time is a different engineering regime, not a tuning flag for ordinary servers.

## 15. Knowledge, Truth, and Lies

Source pages: 395-396

- Distributed systems have no shared memory and communicate only by unreliable messages.
- A node cannot know another node's state directly; it only infers from received or missing messages.
- Network failure and node failure cannot always be distinguished.
- A system model states the assumptions under which an algorithm is designed.
- Correct algorithms can be proved within a system model, even when the model offers few guarantees.

Interview hook: Distributed correctness starts by stating what the system is allowed to assume.

## 16. The Majority Rules

Source pages: 396-397

- A node cannot always trust its own view.
- Examples:
  - A node can receive messages but its outgoing messages are lost.
  - A paused node may resume after others have declared it dead.
- Many algorithms use quorums to avoid depending on one node's perception.
- A quorum is a voting threshold; the common case is a majority.
- A majority quorum lets the system tolerate a minority of faulty nodes.
- Majority quorums are safe because two conflicting majorities cannot both exist at the same time.
- If a quorum declares a node dead, the node must step down even if it believes it is alive.

Interview hook: In distributed systems, authority often comes from quorum, not self-belief.

## 17. Distributed Locks, Leases, Zombies, and Fencing

Source pages: 397-401

- Distributed locks and leases are easy to misuse.
- Leases are used when only one node/client should perform a role:
  - One leader per shard.
  - One transaction/client updating a resource.
  - One node processing a file.
- The severity of duplicate leaseholders depends on the use case:
  - Duplicate computation may only waste resources.
  - Duplicate writes can corrupt data.
- A zombie is a former leaseholder that has not realized it lost the lease.
- Delayed requests create a similar risk: a request from an old leaseholder can arrive after a new leaseholder has taken over.
- Shutting down suspected zombies is incomplete because it may be too late and does not solve delayed messages.
- Fencing tokens are the robust pattern:
  - The lock service returns a monotonically increasing token whenever it grants a lease.
  - Every write includes the token.
  - The storage service rejects writes with tokens older than the highest token it has already accepted.
- Token names vary by system: sequencers, epoch numbers, ballot numbers, or term numbers play similar roles in different systems.
- Fencing requires the downstream storage or service to check token freshness.
- Conditional writes or compare-and-set-like checks can serve the same purpose.
- With leaderless replicated stores, a fencing token can be embedded into the most significant part of the timestamp so newer leaseholders dominate older ones under LWW.
- Quorum reads and repair eventually overwrite zombie writes that reached only a minority replica.

Interview hook: A distributed lock is incomplete unless the protected resource can reject stale holders. The lock service alone does not fence the resource.

## 18. Byzantine Faults

Source pages: 401-404

- Fencing handles nodes that are accidentally stale or delayed; it does not handle a malicious node that forges tokens.
- The chapter mostly assumes unreliable but honest nodes:
  - Nodes may crash, pause, be slow, or have stale state.
  - If they respond, they follow the protocol as they understand it.
- Byzantine faults are arbitrary or deceptive faults, such as contradictory votes.
- Byzantine fault tolerance matters in settings such as:
  - Aerospace systems where radiation can corrupt state.
  - Multi-party systems where participants may cheat.
  - Blockchain-like systems that require agreement among mutually untrusting parties.
- The chapter says most server-side data systems do not use Byzantine fault tolerance because:
  - Nodes are usually controlled by one organization.
  - Tenants are isolated by access control, virtualization, and firewalls.
  - Byzantine protocols are expensive.
  - A shared software bug across all nodes is not solved by running the same buggy implementation many times.
  - Security compromises usually need traditional defenses such as authentication, authorization, encryption, and firewalls.
- Weak anti-lying measures are still useful:
  - Application-level checksums.
  - TLS for corruption protection.
  - Input validation and size checks.
  - Multiple NTP servers with outlier rejection.

Interview hook: Most databases assume crash/recovery faults, not Byzantine adversaries. Know when that assumption is valid.

## 19. System Model and Reality

Source pages: 404-408

- A system model abstracts the faults an algorithm assumes may happen.
- Timing models:
  - Synchronous: bounded network delay, process pauses, and clock error.
  - Partially synchronous: usually behaves within bounds, but sometimes the bounds are exceeded arbitrarily.
  - Asynchronous: no timing assumptions and no clock use.
- Node failure models:
  - Crash-stop: node crashes and never returns.
  - Crash-recovery: node can crash and later restart; stable storage survives, memory does not.
  - Degraded performance/partial functionality: node responds but too slowly or only partly works. Also called limping node, gray failure, or fail-slow.
  - Byzantine: arbitrary behavior, including deception.
- The chapter says partially synchronous with crash-recovery faults is generally the most useful model for real systems.

### Correctness properties

Source pages: 406-407

- Algorithm correctness is defined through properties.
- For fencing-token generation, example properties are:
  - Uniqueness: no duplicate tokens.
  - Monotonic sequence: later requests receive greater tokens when one completes before the other begins.
  - Availability: a non-crashed requester eventually gets a response.
- Safety properties prevent wrong results; once violated, the damage is done.
- Liveness properties promise progress; they often contain "eventually."
- Distributed algorithms commonly require safety to hold under all modeled failures.
- Liveness usually has caveats, such as requiring a majority to remain alive and the network to eventually recover.

### Reality versus model

Source pages: 407-408

- Models simplify reality, but real implementations still face events outside the model.
- Stable storage may be corrupted, erased, or unavailable after reboot.
- Quorum algorithms rely on nodes remembering what they stored; amnesia can break correctness.
- Practical implementations may still need emergency handling for cases that theory assumes away.

Interview hook: Safety means never returning a bad result; liveness means eventually returning a result under stated recovery assumptions.

## 20. Formal Methods and Randomized Testing

Source pages: 408-411

- Distributed systems have enormous state spaces because of concurrency, partial failure, and variable delays.
- Formal verification mathematically analyzes an algorithm against a system model.
- A proof of the algorithm does not prove the production implementation is bug-free, but it can expose design flaws.
- Empirical testing should complement theory.

### Model checking

Source pages: 408-409

- Model checkers verify simplified specifications written in languages such as TLA+, Gallina, or FizzBee.
- They explore possible states and check invariants.
- They usually examine a finite approximation or bounded execution, not every possible real-world state.
- They can find subtle protocol bugs but require keeping the model and implementation aligned.

### Fault injection

Source pages: 409-410

- Fault injection deliberately introduces failures into a running system.
- Examples include network failures, crashes, disk corruption, and process pauses.
- It can run in production-like environments or even production.
- Frameworks such as Jepsen make fault injection easier and have found serious bugs in real systems.

### Deterministic simulation testing

Source pages: 410-411

- DST tests actual code under a simulator that controls network, I/O, clocks, and timing.
- Randomized executions explore many schedules and failures.
- Failures are replayable because the simulator records the exact order of events.
- DST requires controlling sources of nondeterminism.
- Strategies:
  - Application-level deterministic architecture.
  - Runtime-level deterministic asynchronous execution.
  - Machine-level deterministic execution through a custom hypervisor.
- Mocked time and network behavior can make simulations faster than wall-clock failure tests.

Interview hook: Model checking tests the model; fault injection tests a deployed-like system; DST tests actual code with controlled nondeterminism.

## 21. The Power of Determinism

Source pages: 411-412

- Nondeterminism is central to distributed-system difficulty:
  - Concurrent execution.
  - Variable network delay.
  - Process pauses.
  - Clock jumps.
  - Crashes.
- Making behavior deterministic simplifies reasoning and testing.
- Determinism appears in:
  - Event sourcing replay.
  - Durable workflow execution.
  - State machine replication.
  - Statement-based replication.
  - Serial transaction execution with stored procedures.
- Full determinism is hard because subtle nondeterminism can remain in hash-table iteration, resource limits, and similar details.

Interview hook: Determinism turns "many possible histories" into "one replayable history," which is why logs and state machines are so powerful.

## Chapter-Level Memory Hooks

- Distributed systems expose partial failure.
- No response is ambiguous.
- TCP does not prove application success.
- Timeouts are guesses under uncertainty.
- Queueing creates unbounded delay.
- Dynamic resource sharing buys utilization at the cost of predictable latency.
- Use monotonic clocks for durations.
- Use time-of-day clocks cautiously for human timestamps.
- Physical clocks do not establish causality by themselves.
- Clock readings should be treated as intervals, not exact points.
- A paused process can become a zombie.
- A lease without fencing can corrupt data.
- Quorum decisions outrank an individual node's opinion.
- Byzantine fault tolerance is for arbitrary or malicious behavior, not ordinary crash-recovery assumptions.
- System models define what faults an algorithm must tolerate.
- Safety is "never wrong"; liveness is "eventually makes progress."
- Model checking examines specifications.
- Fault injection breaks real deployments on purpose.
- Deterministic simulation tests real code with controlled nondeterminism.
- Determinism is a recurring tool for taming distributed uncertainty.

## Interview Perspective

- Start with partial failure: unlike single-machine software, a distributed system can be partly alive and partly broken.
- Explain why "no reply" cannot identify the cause: request lost, response lost, node down, node paused, or message delayed all look similar.
- Distinguish TCP reliability from application-level acknowledgement.
- Discuss timeout tradeoffs: fast detection versus false suspicion and cascading failure.
- Explain queueing as the reason delay can spike even when no component is permanently broken.
- Contrast circuit switching and packet switching through utilization versus latency guarantees.
- Separate time-of-day clocks from monotonic clocks.
- Explain why LWW with physical timestamps can drop causally later writes.
- Use confidence intervals to explain systems such as Spanner: ordering is safe only when uncertainty intervals do not overlap.
- Use process pauses to explain why leases are unsafe without fencing.
- Define zombie as an old leaseholder still acting after losing authority.
- Explain fencing tokens as increasing authority numbers enforced by the protected resource.
- Clarify the fault model: ordinary distributed databases usually assume honest crash-recovery, not Byzantine behavior.
- Define synchronous, partially synchronous, and asynchronous models.
- Define crash-stop, crash-recovery, fail-slow/gray failure, and Byzantine faults.
- Explain safety and liveness with the fencing-token example.
- Compare model checking, fault injection, and deterministic simulation testing.
- End with the pragmatic lesson: avoid distribution when one machine is enough; use proven distributed systems when distribution is necessary.

## Final Takeaways

- Distributed systems are hard because networks, clocks, and processes fail independently and ambiguously.
- Partial failure is normal enough that it must be part of the design and tests.
- TCP helps with packets, but applications still need idempotence, acknowledgements, retries, and duplicate handling.
- Timeout values cannot be universally correct in an asynchronous network.
- Clock synchronization is useful but dangerous when treated as exact.
- Physical timestamps are not a reliable substitute for causality tracking.
- Process pauses make leases unsafe unless stale holders are fenced off at the protected resource.
- Quorums reduce dependence on one node's perspective.
- Byzantine assumptions are expensive and usually outside ordinary server-side database design.
- System models, safety/liveness reasoning, formal methods, fault injection, and DST are complementary tools for building confidence.
- Determinism is a recurring design pattern for making distributed behavior understandable and replayable.
- If a single-node design meets the requirements, the chapter advises avoiding the extra failure modes of distribution.

Confidence: High
