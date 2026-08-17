# Chapter 10: Consistency and Consensus - Compressed Study Notes

Book: DDIA
Chapter: 10, Consistency and Consensus
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 425-467
Raw source used: books/DDIA/raw/chapter-10-chapter-10-consistency-and-consensus.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 10 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter frame: strong consistency under faults | 425-426 |
| Linearizability: single-copy illusion and recency | 426-428 |
| What makes a system linearizable? | 428-431 |
| Linearizability versus serializability | 431-432 |
| Relying on linearizability | 432-435 |
| Implementing linearizable systems | 435-437 |
| The cost of linearizability and CAP | 437-441 |
| ID generators and distributed ordering | 441-444 |
| Logical clocks, Lamport clocks, and hybrid logical clocks | 444-446 |
| Linearizable ID generators | 447-449 |
| Enforcing constraints using logical clocks | 449 |
| Consensus introduction and impossibility result | 449-451 |
| The many faces of consensus | 451-457 |
| Consensus in practice and shared logs | 457-460 |
| Pros and cons of consensus | 461 |
| Coordination services | 461-464 |
| Chapter summary | 464-467 |

## 1. Chapter Frame: Strong Consistency Under Faults

Source pages: 425-426

- Replication is one of the main tools for fault tolerance, but it creates inconsistency risks: stale reads, concurrent writes, and conflicts.
- The chapter contrasts two philosophies:
  - Eventual consistency exposes replication effects to the application, so application logic must handle conflicts and inconsistencies.
  - Strong consistency hides replication internals and makes the system behave more like a single node.
- Strong consistency is easier for application developers but costs performance and can turn some faults into outages.
- Eventual consistency is inevitable for offline-capable applications; strong consistency is often suitable when replicas have fast, reliable communication.
- The chapter focuses on three linked topics: linearizability, IDs/timestamps, and consensus algorithms.
- The chapter emphasizes edge cases: systems can appear correct without faults yet fail under unlucky timing, message ordering, or partial failures.

Interview hook: Strong consistency is not "free correctness"; it is a design choice that trades latency and some availability for a simpler programming model.

## 2. Linearizability: The Single-Copy Illusion

Source pages: 426-428

- Linearizability means a replicated system behaves as though there is exactly one copy of the data and every operation on it is atomic.
- It is also called atomic consistency, strong consistency, immediate consistency, or external consistency.
- Its key guarantee is recency: once a client successfully completes a write, all later reads by any client must be able to see that write.
- The sports-score example shows a violation: Aaliyah refreshes and sees the final score; Bryce refreshes after hearing her and sees an older replica. Bryce's read should be at least as recent as Aaliyah's.
- Simultaneous reads may legitimately differ because clients do not know the exact processing order. The violation occurs when real-time ordering is known: one operation finished before another began.

Interview hook: Linearizability is best remembered as "after someone has seen the new value, nobody later should see the old value."

## 3. What Makes a System Linearizable?

Source pages: 428-431

- The theory usually discusses a single object called a register; in practice this might be a key, row, document, or similar individual object.
- A read returns a value; a write sets a value and returns a response.
- A read that completes before a write begins must return the old value.
- A read that begins after a write completes must return the new value.
- A read concurrent with a write may return either the old or the new value, because the client cannot know whether the write took effect before the read was processed.
- That rule alone is not enough: values must not appear to flip backward. Once any read observes the new value, later reads must also observe at least that new value until another write supersedes it.
- The mental model is that each operation takes effect at a single instant between its request and response.
- A history is linearizable if all operation instants can be ordered into a valid sequential history and those instants always move forward in real time.
- CAS, or compare-and-set, is an atomic conditional update: update only if the current value equals the expected old value.
- Linearizability does not imply transaction isolation. Other clients may change values between a client's operations, and multi-object anomalies are outside the single-register guarantee.
- Testing linearizability is possible by recording request/response timings and checking whether a valid sequential order exists, but this is computationally expensive.
- Weaker replicated-system guarantees include read-after-write consistency, monotonic reads, and consistent prefix reads. Linearizability includes these and more.

Interview hook: For a timeline question, identify concurrent operations first; then check whether any later operation observes a state older than one already observed.

## 4. Linearizability Versus Serializability

Source pages: 431-432

- Serializability is a transaction isolation guarantee. It makes transactions behave as if they ran one at a time, but the serial order may differ from real-time order.
- Linearizability is a recency guarantee for operations on an individual object. If operation A finishes before operation B starts, B must see a state at least as new as A.
- Linearizability does not group multiple reads/writes into transactions and does not prevent multi-object anomalies such as write skew.
- Serializability alone can allow stale reads because it does not require the serial order to follow real-time order.
- A system that provides both serializability and linearizability provides strict serializability, also called strong one-copy serializability.
- The consistency model for replicated data and the isolation level for transactions can often be chosen independently.
- The chapter notes that single-node databases are typically linearizable, while distributed systems vary. Some systems provide serializability plus limited recency guarantees; others provide strict serializability.

Interview hook: Serializability answers "can transactions be arranged in some serial order?" Linearizability answers "does that order respect real time for individual-object operations?"

## 5. Relying on Linearizability

Source pages: 432-435

- Linearizability matters when stale data can break correctness rather than merely annoy users.
- Leader election and distributed leases require linearizability because two nodes must not both acquire the same leadership lease.
- Coordination services such as ZooKeeper and etcd are used for leases and leader election because they implement fault-tolerant linearizable operations using consensus algorithms.
- ZooKeeper provides linearizable writes, but reads may be stale if they are not served by the current leader; etcd version 3 provides linearizable reads by default.
- Oracle RAC uses fine-grained linearizable locks per disk page, making low-latency interconnects important because these locks sit on the transaction critical path.
- Hard uniqueness constraints require linearizability. Examples include usernames, email addresses, file paths, last available inventory item, and seat assignment.
- A uniqueness constraint resembles a lock or CAS operation: claim a name only if it is currently unclaimed.
- Some constraints can be interpreted loosely. For example, overbooking a flight may be remediated with compensation, so strict linearizability may not always be necessary.
- Hard uniqueness constraints are different from foreign-key or attribute constraints, which can be implemented without linearizability.
- Cross-channel timing dependencies create subtle races. In the video example, the web server writes a video to file storage and then sends a queue message to a transcoder. If storage is not linearizable, the queue message may arrive before the transcoder can read the just-written video.
- The sports-score example has the same pattern: database replication is one channel, human voice is another channel.
- Linearizability is the simplest way to avoid such races, but alternative approaches may be possible when the application controls the extra communication channel.

Interview hook: Ask whether correctness depends on a later observer seeing what an earlier observer already saw. If yes, linearizability may be required.

## 6. Implementing Linearizable Systems

Source pages: 435-437

- A real single copy is trivially linearizable but not fault-tolerant.
- Single-leader replication is potentially linearizable if all reads and writes go through the true leader.
- The danger in single-leader systems is split brain or stale leadership: a node may believe it is still leader and serve requests after it has effectively lost leadership.
- Asynchronous replication can lose committed writes during failover, violating durability and linearizability.
- Sharding with a leader per shard does not affect linearizability for a single object, but cross-shard transactions require separate coordination.
- Consensus algorithms are likely linearizable because they are designed to prevent split brain and handle leader election/failover safely.
- Using consensus does not automatically make every operation linearizable; reads from a node that does not verify current leadership may be stale.
- Multi-leader replication is generally not linearizable because multiple leaders accept writes concurrently and replicate asynchronously, requiring conflict resolution.
- Leaderless Dynamo-style replication is probably not linearizable even with quorum reads/writes.
- Quorum condition `w + r > n` can still be nonlinearizable under variable network delays: a later read may return old data after an earlier read returned new data.
- Dynamo-style quorums can be made linearizable only with extra costs such as synchronous read repair and pre-write quorum reads to establish a newer timestamp.
- Cassandra waits for read repair on quorum reads, but its use of time-of-day timestamps prevents linearizability; Riak avoids synchronous read repair because of performance cost.
- Linearizable CAS cannot be implemented by ordinary leaderless quorum reads/writes; it requires consensus.

Interview hook: Do not equate "quorum" with "linearizable." Quorums overlap, but timing and timestamp rules still determine whether recency is guaranteed.

## 7. The Cost of Linearizability and CAP

Source pages: 437-441

- Linearizability has a direct availability cost during network partitions.
- In a multi-leader multi-region setup, each region may continue accepting writes during an inter-region network interruption and reconcile later, but the result is not linearizable.
- In a single-leader setup, clients in follower regions must contact the leader for writes and linearizable reads. If the inter-region link fails, those clients cannot perform linearizable operations.
- For any linearizable database, disconnected replicas must either wait for reconnection or return errors; otherwise they might serve stale or conflicting results.
- This is the core CAP tradeoff: under a partition, choose linearizable consistency with unavailability for some replicas, or availability with weaker consistency.
- CAP was historically influential and helped broaden distributed database design, but the chapter treats it as narrow and often misunderstood.
- The formal CAP theorem discusses only linearizability and network partitions; it does not cover dead nodes, network delays, latency tradeoffs, or other consistency models.
- "Consistency, availability, partition tolerance: pick two" is misleading because partitions are faults, not optional design features.
- Better phrasing: when partitioned, choose consistency or availability.
- Some systems are neither CP nor AP under CAP's formal definitions, and CAP's availability definition does not match everyday engineering usage.
- PACELC generalizes the discussion by observing that even without partitions, systems may trade consistency for lower latency.
- Linearizability is rare in practice because it costs latency. Even modern multi-core RAM is not linearizable without memory barriers due to CPU caches and store buffers.
- The chapter attributes many systems' avoidance of linearizability primarily to performance, not only fault tolerance.
- Attiya and Welch's result says linearizable read/write latency is at least proportional to network delay uncertainty. With highly variable delays, linearizable operations are inevitably slower.

Interview hook: CAP is not the full design framework. The sharper practical point is that linearizability requires coordination, and coordination costs latency and availability under partitions.

## 8. ID Generators and Distributed Ordering

Source pages: 441-444

- Many systems need unique IDs as primary keys.
- A single-node autoincrementing counter is compact and ordered: increasing ID order reflects creation order.
- This counter is linearizable because fetch-and-add atomically increments the counter and returns a unique value.
- Linearizability ensures that if Aaliyah's post completes before Bryce's post begins, Bryce's ID is greater. Concurrent posts have no required order beyond uniqueness.
- A single-node in-memory counter is easy, but persistence and fault tolerance are harder. The single node is also a bottleneck and a high-latency dependency for remote regions.
- Sharded ID assignment can reserve bits for shard IDs, but loses reliable creation ordering across shards.
- Preallocated ID blocks reduce central coordination but also lose global ordering because a node with a higher block may issue IDs before a node with a lower block.
- Random UUIDs can be generated locally and avoid coordination, but their order carries no creation-time meaning and they use more space.
- Timestamp-based IDs combine wall-clock time with uniqueness bits such as shard number, local sequence, or randomness. Examples in the source include version 7 UUIDs, Snowflake, ULIDs, Hazelcast Flake IDs, and MongoDB ObjectIDs.
- Timestamp-based IDs are only approximately ordered. Clock skew or clock jumps can place later events before earlier events.
- High-precision clock synchronization can reduce ordering anomalies, but the chapter then moves to logical clocks as a hardware-independent alternative.

Interview hook: Unique is not the same as ordered, and ordered is not the same as linearizable.

## 9. Logical Clocks, Lamport Clocks, and Hybrid Logical Clocks

Source pages: 444-446

- Physical clocks measure elapsed real time. Logical clocks are algorithms that count events.
- A logical timestamp does not tell wall-clock time; it gives an order relation between events.
- The chapter's desired logical-clock properties are compactness, uniqueness, total ordering, and consistency with causality.
- A Lamport timestamp is a pair of `(counter, node ID)`.
- A node increments its counter whenever it generates a timestamp.
- When a node sees another node's timestamp with a higher counter, it advances its local counter to match before continuing.
- Ties on counter are broken by node ID, producing a total order.
- Lamport clocks preserve causality: if event A happened before event B, A's timestamp is less than B's timestamp.
- Lamport clocks are not linearizable; they order events that have communicated causally, but they cannot order events based only on real-time completion across nodes that have not communicated.
- Lamport clocks also lack direct physical-time meaning, so a separate time field is needed for queries like "messages posted on a date."
- Hybrid logical clocks combine physical time with Lamport-style causality tracking.
- A hybrid logical clock counts time units like a physical clock, but moves forward when it observes a higher timestamp from another node.
- It is incremented when generating timestamps, so it remains monotonic even if the underlying physical clock moves backward.
- Hybrid logical clocks can be treated much like wall-clock timestamps while preserving happens-before ordering; the chapter mentions CockroachDB as an example user.
- Lamport and hybrid logical clocks order concurrent timestamps arbitrarily. They generally cannot tell whether two differently valued timestamps were concurrent.
- Vector clocks can detect concurrency by tracking a counter per node, but their timestamps are much larger because they may contain one integer per node.

Interview hook: Lamport clocks give a total causal order, vector clocks reveal concurrency, and neither automatically gives linearizable recency.

## 10. Linearizable ID Generators

Source pages: 447-449

- Logical clocks are weaker than a linearizable ID generator.
- Linearizability requires that if request A completes before request B begins, B gets a higher ID, even if the involved nodes never communicate with each other.
- Lamport and hybrid logical clocks can only ensure a node produces timestamps greater than timestamps it has observed.
- The privacy example shows the risk: a user sets an account private on a laptop, then uploads a photo from a phone. If the account database and photo database use non-linearizable timestamps, the photo may receive an earlier timestamp than the privacy change, so a snapshot read may treat the account as public and expose the photo.
- Possible fixes include forcing the photo database to read account status or having client devices track latest write timestamps, but these are easy to miss or difficult across multiple devices.
- A linearizable ID generator solves this by ensuring the later photo upload gets a greater timestamp than the earlier privacy change.
- The simplest linearizable ID generator is a single node that atomically increments and returns a counter, persists the counter, and replicates it for fault tolerance.
- TiDB/TiKV's timestamp oracle is given as a practical example.
- Batching can reduce disk and replication cost: persist and replicate a range of IDs, then hand them out from memory. Crashes may skip IDs but must not create duplicates or out-of-order IDs.
- Sharding or multi-region distribution makes strict ID ordering hard because independent generators cannot guarantee linearizable order.
- Spanner's alternative relies on physical clock uncertainty intervals and waits out the uncertainty before returning; this can guarantee ordering without cross-region communication if the uncertainty bound is correct.
- The downside of the clock-uncertainty approach is the need for tight clock synchronization and correct uncertainty calculation.

Interview hook: A linearizable timestamp oracle is a small but central serialization point; batching hides some cost but does not remove the ordering bottleneck.

## 11. Enforcing Constraints Using Logical Clocks

Source pages: 449

- Logical clocks or linearizable IDs are not sufficient by themselves for fault-tolerant locks and uniqueness constraints.
- If nodes race to acquire a lock or username, you could assign timestamps and declare the lowest timestamp the winner.
- The missing piece is knowledge: a node must know that no other node has generated a lower timestamp.
- To be certain, it would need responses from every node that might have generated a timestamp.
- If a node has failed or is unreachable, waiting for everyone can halt the system.
- Fault-tolerant locks, leases, and similar constructs therefore need something stronger: consensus.

Interview hook: Ordering proposals is easier than deciding safely which proposal won when some proposers may be unreachable.

## 12. Consensus and the Impossibility Result

Source pages: 449-451

- Consensus is the shared problem behind fault-tolerant leader election, linearizable ID generation, and distributed CAS/locks.
- The standard formulation asks multiple nodes to agree on a single value.
- Important non-Byzantine consensus algorithms include Viewstamped Replication, Paxos, Raft, and Zab.
- These algorithms assume messages may be delayed or dropped and nodes may crash, restart, or become disconnected, but nodes do not act maliciously or violate the protocol.
- Byzantine consensus is different and can tolerate malicious or contradictory behavior, typically under stronger assumptions; the chapter keeps it out of scope.
- The FLP result proves that no deterministic algorithm in a fully asynchronous model can guarantee consensus termination if a node may crash.
- FLP does not say consensus can never be reached; it says guaranteed termination is impossible under those assumptions.
- Practical systems escape the impossibility by using timeouts to suspect crashes or by using randomization, so consensus can usually be achieved in practice.

Interview hook: FLP limits guaranteed termination in an asynchronous crash-prone model; it does not make practical consensus algorithms useless.

## 13. Single-Value Consensus

Source pages: 451-453

- Single-value consensus is useful for leader election, lock acquisition, lease acquisition, seat booking, and uniqueness races.
- One or more nodes propose values, and the algorithm decides one proposed value.
- Required properties:
  - Uniform agreement: no two nodes decide differently.
  - Integrity: a node cannot decide twice with different values.
  - Validity: a decided value must have been proposed.
  - Termination: every non-crashed node eventually decides.
- Agreement, integrity, and validity are safety properties; termination is a liveness property.
- A single dictator node can satisfy the first three properties, but not fault-tolerant termination if it fails.
- Consensus requires progress even if some nodes crash permanently.
- No algorithm can decide if all nodes fail, and consensus requires at least a majority of nodes functioning for termination.
- Safety is stronger than liveness in failure cases: even if a majority fails and the system cannot process requests, a correct consensus algorithm should not make inconsistent decisions.

Interview hook: Consensus safety means "never decide conflicting values"; consensus liveness means "eventually decide, assuming enough nodes and eventual communication."

## 14. CAS, Shared Logs, Fetch-and-Add, and Atomic Commit as Consensus

Source pages: 453-457

- Fault-tolerant linearizable CAS and consensus are equivalent.
- CAS can solve consensus by starting with a null value and letting proposers CAS null to their proposed value; the successful value is the decision.
- Consensus can implement CAS by deciding among concurrent proposed updates for the same expected value.
- Shared logs are also equivalent to consensus. A shared log lets multiple nodes append values while every reader sees the same entries in the same order.
- A shared log requires eventual append, reliable delivery, append-only behavior, agreement on prefixes, and validity of entries.
- Total order broadcast, also called atomic broadcast or total order multicast, is the protocol form of a shared log.
- A shared log can solve consensus by deciding whichever proposed value appears first in the log.
- Consensus can build a shared log by running a consensus instance per log slot and retrying proposals that lose a slot.
- A single leader without failover is not enough because it fails the liveness requirement when the leader crashes.
- Fetch-and-add can be implemented using CAS retries, so consensus can implement fetch-and-add.
- Fault-tolerant fetch-and-add alone solves consensus only for two proposers, because losing proposers may not know who won if the winner crashes before announcing its value.
- CAS and shared logs can solve consensus for any number of proposers.
- Atomic commitment requires distributed transaction participants to all commit or all abort.
- Its properties include uniform agreement, integrity, validity, nontriviality, and termination.
- Atomic commitment differs from ordinary consensus because commit is allowed only if all participants voted to commit; any abort vote requires abort.
- Consensus can solve atomic commit by having nodes propose commit only after receiving commit votes from all nodes, otherwise proposing abort.
- Fault-tolerant atomic commit can solve consensus by using transactions and single-node CAS over a quorum.

Interview hook: Many distributed systems problems are the same hard problem in disguise: choosing one outcome safely despite concurrency and failure.

## 15. Consensus in Practice and Shared Logs

Source pages: 457-460

- Most practical consensus systems provide shared logs.
- Raft, Viewstamped Replication, and Zab directly provide shared logs; practical Paxos systems often use Multi-Paxos for a shared log.
- Shared logs fit replication: each log entry represents a write, replicas apply entries in identical order using deterministic logic, and they converge to the same state.
- This is state machine replication and is related to event sourcing.
- Shared logs can implement serializable transactions if log entries are deterministic transactions executed in the same order on each node.
- Sharded strong-consistency databases often maintain one log per shard. This improves scalability but weakens cross-shard guarantees unless additional coordination is added.
- A shared log can implement single-value consensus, CAS, per-seat consensus decisions, counters, fetch-and-add, and fencing tokens.
- In ZooKeeper, a log sequence number called zxid can act as a fencing-related sequence.
- Consensus algorithms often look like single-leader replication with automatic failover.
- Traditional manual failover does not satisfy consensus termination because a human must intervene.
- Common leader-based consensus algorithms use epochs:
  - Paxos calls them ballot numbers.
  - Viewstamped Replication calls them view numbers.
  - Raft calls them term numbers.
- Within an epoch, the leader is unique. If two leaders conflict, the higher epoch wins.
- Before appending a log entry, a leader checks for a higher-epoch leader by collecting votes from a quorum.
- Consensus uses two overlapping quorum votes: one to elect a leader, another to accept each log entry.
- This resembles two-phase commit superficially, but differs because any node can start an election and only a quorum is needed; 2PC depends on a coordinator and every participant's yes vote.
- A new leader must preserve already confirmed log entries. Raft restricts leadership to sufficiently up-to-date logs; Paxos lets a leader catch up before appending new entries.
- For strict shared-log properties, a new leader must be up-to-date before processing writes or linearizable reads.
- Weakening this requirement, such as with unclean leader election, can improve recovery speed or availability but steps outside consensus guarantees and risks data loss or corruption.
- Linearizable reads in consensus-backed databases may need quorum confirmation that the current leader is still current.
- Consensus algorithms often assume a fixed voting set, but practical systems add reconfiguration for adding/removing nodes or moving regions.

Interview hook: Consensus is not merely "elect a leader"; it is "elect a leader, prove it is current, and replicate every committed log entry to an overlapping quorum."

## 16. Pros and Cons of Consensus

Source pages: 461

- Consensus is a major breakthrough: it is effectively single-leader replication done correctly with automatic failover, no split brain, and no loss of committed data under the modeled faults.
- Automatic failover without a proven consensus algorithm is likely unsafe.
- A proven consensus algorithm does not guarantee the whole system is correct; bugs can still exist around it.
- Costs:
  - A strict majority is required to operate.
  - Three nodes tolerate one failure; five nodes tolerate two.
  - Every operation requires quorum communication.
  - Adding nodes does not increase throughput and may slow the algorithm.
  - During a partition, only the majority side can make progress.
- Consensus relies on timeouts for failure detection. Wide-area or highly variable networks make timeout tuning hard.
- Too-large timeouts slow recovery; too-small timeouts create unnecessary leader elections and poor performance.
- Some algorithms have sensitive edge cases. The chapter notes Raft cases where unstable links can cause repeated leadership changes or forced resignations; Raft's pre-vote phase addresses this.
- Leader-based protocols such as Paxos can have similar performance issues; EPaxos and derivatives use leaderless protocols that can be more robust to poorly performing nodes or links.

Interview hook: Consensus buys safety and automatic failover with majority quorums, but the price is coordination latency, reduced availability for minorities, and timeout sensitivity.

## 17. Coordination Services

Source pages: 461-464

- Coordination services such as ZooKeeper, etcd, and Consul are prominent consensus users.
- They look like key-value stores but are not meant for high write volume or general-purpose storage.
- They coordinate other distributed systems using small data sets that fit in memory, while still writing to disk for durability and replicating via consensus.
- The chapter gives Kubernetes with etcd and Spark/Flink high-availability modes with ZooKeeper as examples.
- Coordination services combine consensus with features useful for distributed coordination:
  - Locks and leases: only one concurrent contender acquires a lease.
  - Fencing support: monotonically increasing log IDs or revisions can protect resources from delayed old leaseholders.
  - Failure detection: long-lived sessions and heartbeats allow leases to remain active through brief interruptions but expire after timeout.
  - Change notifications: clients can subscribe to key changes instead of polling.
- Failure detection and notifications do not themselves require consensus, but pair well with the consensus-backed atomic operations and fencing.
- Coordination services may also store configuration and notify processes of changes. This does not require consensus, but it can be convenient if the service is already present.
- They are useful for choosing a leader, assigning shards/work to nodes, rebalancing as nodes join, and taking over failed nodes' work.
- Higher-level libraries help, but correct coordination remains difficult; using a coordination service is safer than implementing consensus from scratch.
- A dedicated coordination service can run on a small fixed set of nodes, often three or five, even when the system it coordinates has many nodes or shards.
- Coordination-service data should change slowly, such as leader assignment for a shard. It is not intended for thousands of updates per second.
- Service discovery is a common use, but consensus is often overkill because service discovery usually needs fast, highly available reads more than linearizability.
- Caching service-discovery results, TTL refresh, retrying with latest values, and DNS-style caching are often preferable.
- ZooKeeper observers support scalable stale reads: they receive the log and maintain copies but do not vote. Reads from observers may be stale, but they remain available during some interruptions and increase read throughput.

Interview hook: Use coordination services for slow-changing coordination state, not as a high-write database or universal service-discovery dependency.

## Chapter-Level Memory Hooks

- Strong consistency hides replication complexity but charges latency and availability.
- Linearizability is a recency guarantee, not a transaction isolation level.
- Concurrent operations may be ordered either way; real-time-separated operations may not go backward.
- Once a new value is observed, later reads cannot return an older value.
- Serializability orders transactions; linearizability respects real time for single-object operations.
- Hard uniqueness, leader election, leases, and cross-channel races are classic linearizability use cases.
- Quorum reads/writes do not automatically imply linearizability.
- CAP is best read as "consistent or available when partitioned," not "pick any two."
- Linearizability is slow because it requires coordination proportional to network-delay uncertainty.
- Unique IDs, ordered IDs, causal IDs, and linearizable IDs are different guarantees.
- Lamport clocks preserve causality; vector clocks can detect concurrency; hybrid logical clocks mix physical-time usefulness with causal ordering.
- Logical clocks cannot by themselves solve fault-tolerant locks because a node must know no lower timestamp exists.
- Consensus is the common core behind leader election, CAS, shared logs, fetch-and-add, and atomic commit.
- Consensus safety survives severe faults; progress requires a working majority.
- Practical consensus usually exposes a shared log.
- Epochs plus overlapping quorums prevent split brain and preserve committed entries.
- Consensus-backed linearizable reads may still need quorum confirmation.
- Coordination services are for slow-changing coordination, leases, fencing, failure detection, and notifications.

## Interview Perspective

- Define linearizability with a real-time example.
- Explain why a stale read after another client has observed a new value violates linearizability.
- Distinguish linearizability from serializability and strict serializability.
- Explain why a hard uniqueness constraint requires linearizability.
- Explain cross-channel timing dependencies using file storage plus message queue.
- Compare single-leader, consensus, multi-leader, and leaderless replication for linearizability.
- Explain why `w + r > n` is not enough for linearizability.
- State the practical lesson of CAP without using the misleading "pick two" slogan.
- Explain why linearizable systems are slower in variable-delay networks.
- Compare sharded IDs, preallocated blocks, random UUIDs, timestamp IDs, Lamport timestamps, hybrid logical clocks, vector clocks, and linearizable ID generators.
- Explain why non-linearizable timestamps can break privacy or permission checks across shards/databases.
- Describe FLP and why practical consensus still works.
- List consensus properties: agreement, integrity, validity, termination.
- Explain why safety can hold even when liveness is lost.
- Show how CAS, shared logs, and atomic commit reduce to consensus.
- Explain why shared logs are the practical consensus interface.
- Describe epochs/terms, overlapping quorums, and why new leaders must be up-to-date.
- Explain why unclean leader election is an availability/performance tradeoff that can lose consensus guarantees.
- Explain what coordination services are good for and where they are overkill.

## Final Takeaways

- Chapter 10 turns the messy failure model of Chapter 9 into concrete correctness tools.
- Linearizability makes replicated data feel like one atomic copy, but only by coordinating operations.
- Many systems that appear strongly consistent are not linearizable once network delays, stale leaders, or clock skew enter the picture.
- Strong IDs and timestamps are consistency mechanisms, not just convenience features.
- Logical clocks are excellent for causality but insufficient for fault-tolerant decision making.
- Consensus is the mechanism that lets a group of nodes make one durable, non-conflicting decision despite crashes and message delays.
- Most practical consensus systems implement a shared log, which becomes the backbone for replication, state machines, transactions, counters, and fencing.
- Consensus is powerful but expensive: majority quorums, timeout tuning, and partition behavior must be understood.
- Coordination services package consensus for common distributed-systems tasks, but they should be used for coordination state rather than high-volume application data.

Confidence: High
