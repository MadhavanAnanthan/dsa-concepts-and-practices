# Chapter 10: Consistency and Consensus - Compressed Study Summary

Book: DDIA
Chapter: 10, Consistency and Consensus
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 425-467
Raw source used: books/DDIA/raw/chapter-10-chapter-10-consistency-and-consensus.md
Method: Concise chapter-level compression based only on the raw Chapter 10 markdown. Long verbatim copying was avoided.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Strong consistency, replication, and chapter frame | 425-426 |
| Linearizability and its real-time recency rule | 426-432 |
| Practical reliance on linearizability | 432-435 |
| Implementation options and leaderless quorum limitations | 435-437 |
| Cost of linearizability, CAP, and network delay | 437-441 |
| ID generators, logical clocks, and ordering guarantees | 441-449 |
| Consensus, FLP, and equivalent problem forms | 449-457 |
| Practical consensus, shared logs, leader epochs, and failover | 457-461 |
| Coordination services and service discovery | 461-464 |
| Chapter summary | 464-467 |

## Compressed Chapter Summary

Chapter 10 studies strong consistency in fault-tolerant systems. Replication helps tolerate faults, but it creates stale reads, write conflicts, and disagreement between replicas. Eventual consistency exposes those issues to the application, while strong consistency tries to hide them by making the system behave like a single node. The chapter focuses on linearizability, ID and timestamp generation, and consensus algorithms as the way to combine strong consistency with fault tolerance.

Linearizability is the central consistency model. It makes a replicated system appear as though there is only one copy of each object and every operation takes effect atomically. Its core guarantee is recency: once a write has completed, all later reads by any client must be able to observe it. The sports-score example captures the intuition: if Aaliyah sees the final score and tells Bryce, Bryce should not refresh afterward and see an older result from a lagging replica. Concurrent operations may legitimately be ordered either way, but once a new value has been observed, later reads must not go backward to an older value.

The formal intuition is that every operation can be assigned a single instant between its request and response. Those instants must form a valid sequential history, and the order must move forward in real time. Reads concurrent with a write may see either old or new values, but if one read has returned the new value, later reads must also see the new value until another write supersedes it. CAS is an important linearizable operation because it conditionally changes a value only if it still has an expected old value.

Linearizability and serializability are different. Serializability is a transaction isolation guarantee across transactions that may read and write multiple objects; it requires the result to match some serial order, but that order need not match real time. Linearizability is a real-time recency guarantee for individual objects and does not provide multi-object transaction isolation. A system that provides both is strictly serializable, also called strong one-copy serializable.

Linearizability is needed when stale reads can break correctness. Leader election and leases need it because two nodes must not both believe they hold leadership. Hard uniqueness constraints need it because two users must not successfully claim the same username or two clients the same file path. Some business constraints can be loose and repaired later, but hard uniqueness constraints cannot. Cross-channel races are another use case: if a web server writes a video to file storage and then sends a queue message to a transcoder, a non-linearizable storage system may let the queue message arrive before the file read can see the new video.

Implementation choices matter. A single copy is linearizable but not fault-tolerant. Single-leader replication can be linearizable if all reads and writes go through the true leader, but stale leaders, split brain, and asynchronous failover can violate the guarantee. Consensus algorithms are designed to prevent split brain and safely implement linearizable storage, but even a consensus-backed system can return stale reads if it reads from a node without checking current leadership. Multi-leader replication is generally not linearizable because writes are accepted concurrently and reconciled later. Leaderless quorum replication is also safest to treat as non-linearizable: even if `w + r > n`, variable delays can let a later read return old data after an earlier read returned new data. Making Dynamo-style quorums linearizable requires extra coordination such as synchronous read repair and pre-write quorum reads, and linearizable CAS still requires consensus.

Linearizability has costs. During a network partition, replicas that cannot coordinate with the current authority must either wait or return errors if linearizability is required. If the application accepts weaker consistency, disconnected replicas may remain available and reconcile later. This is the useful core of CAP: when partitioned, choose linearizable consistency or availability. The chapter warns that the slogan "consistency, availability, partition tolerance: pick two" is misleading because partitions are faults, not optional features. CAP is historically important but narrow: it covers linearizability and partitions, not the full range of delays, failures, and consistency models. Even without partitions, systems often weaken consistency to reduce latency. Linearizable read/write latency is tied to uncertainty in network delays, so in variable-delay networks it is inevitably slower than weaker consistency.

The chapter then connects consistency to ID generation. A single-node autoincrementing counter gives compact unique IDs whose order reflects creation order, and it is linearizable because it is an atomic fetch-and-add operation. But a single ID node is a fault-tolerance risk, a possible throughput bottleneck, and a high-latency dependency for remote regions. Sharded ID assignment, preallocated ID blocks, random UUIDs, and timestamp-based IDs can improve scalability or local generation, but they weaken ordering. Timestamp-based IDs are only approximately ordered because clock skew or clock jumps can place later events before earlier ones.

Logical clocks solve part of the ordering problem without special clock hardware. A logical clock counts events rather than measuring physical time. A Lamport timestamp combines a counter with a node ID. Nodes increment their local counter when generating timestamps and advance it when they observe higher counters from other nodes. Lamport timestamps provide a compact total order consistent with causality, but they are not linearizable because they cannot order events by real-time completion unless communication has conveyed causality. Hybrid logical clocks combine wall-clock-like usefulness with Lamport-style causal ordering: they follow physical time but move forward when observing higher timestamps and remain monotonic even if the physical clock moves backward. Vector clocks can detect concurrency, but their timestamps are larger because they track per-node counters.

Logical clocks still do not provide the same guarantee as a linearizable ID generator. The privacy example shows why: a user changes an account to private on one device, then uploads a photo from another. If separate databases assign non-linearizable timestamps, the photo can receive a timestamp earlier than the privacy change, allowing a snapshot read to expose it. A linearizable ID generator prevents that by ensuring later operations get greater IDs, even across nodes that did not communicate. The simplest implementation is a single timestamp oracle that atomically increments, persists, and replicates a counter; batching can skip IDs after crashes but avoid duplicates or out-of-order IDs. Spanner-style clock uncertainty offers another approach by waiting out the uncertainty interval, but it requires tightly synchronized clocks and accurate uncertainty bounds.

Logical clocks and linearizable IDs are not enough for fault-tolerant locks or uniqueness constraints. If nodes use timestamps to choose the lowest proposal as winner, a node still needs to know no unreachable node has generated a lower timestamp. Waiting for every possible proposer is not fault-tolerant. This leads to consensus.

Consensus is the common distributed-systems problem behind fault-tolerant leader election, linearizable CAS, shared logs, ID generators, and atomic commit. The standard problem asks multiple nodes to agree on one proposed value. Common non-Byzantine algorithms include Viewstamped Replication, Paxos, Raft, and Zab; they assume delayed/dropped messages and crash/restart failures, but not malicious protocol violations. The FLP result says deterministic consensus cannot guarantee termination in a fully asynchronous system with possible crashes. It does not say consensus can never succeed. Practical systems use timeouts or randomization, so consensus can usually be achieved under real-world assumptions.

Single-value consensus has four properties: uniform agreement, integrity, validity, and termination. Agreement, integrity, and validity are safety properties; termination is liveness. A dictator node can satisfy safety but fails liveness if it crashes. A real consensus algorithm requires enough functioning nodes, typically a majority, to make progress. Even if progress stops because a majority is unavailable, safety should still hold: the system should not decide conflicting values.

Several problems are equivalent to consensus. A fault-tolerant linearizable CAS can solve consensus by letting proposers race to set an initially null value; consensus can implement CAS by deciding which proposed conditional update wins. A shared log is also equivalent: all nodes see the same entries in the same order, and the first entry can decide a consensus value. Consensus can build a shared log by running consensus per log slot. Fetch-and-add can be built from CAS, though fetch-and-add alone only solves consensus for two proposers because losing proposers may not know who won if the winner crashes. Atomic commit is also equivalent to consensus, although commit has the extra rule that all participants must have voted to commit; any abort vote forces abort.

In practice, shared logs are the most useful consensus abstraction. Raft, Viewstamped Replication, and Zab provide shared logs directly, and practical Paxos systems usually use Multi-Paxos. Shared logs fit database replication: every log entry is a write, every replica applies writes in the same order, and deterministic application produces the same state. This is state machine replication and connects to event sourcing. Shared logs can also implement serializable transactions, counters, CAS, fencing tokens, and many single-value consensus instances. Sharded databases often use a separate log per shard to scale, but cross-shard guarantees require extra coordination.

Consensus algorithms resemble single-leader replication with automatic failover, but the failover is the hard part. They use epochs, called ballot numbers in Paxos, view numbers in Viewstamped Replication, and term numbers in Raft. Within one epoch, there is one leader. If two leaders conflict, the higher epoch wins. A leader must collect quorum votes to verify that no higher-epoch leader exists before appending entries. There are two overlapping votes: one for leader election and one for accepting each proposed log entry. A new leader must preserve already confirmed log entries. Raft restricts leadership to nodes with sufficiently up-to-date logs; Paxos lets a new leader catch up before appending its own entries. Weakening this requirement, such as with unclean leader election, can improve availability or speed but risks data loss or corruption and steps outside consensus guarantees.

Consensus is powerful but costly. It is essentially single-leader replication done correctly, with automatic failover, no split brain, and no loss of committed data under its assumptions. But it requires a strict majority to operate, quorum communication for every operation, and careful timeout tuning. Three nodes tolerate one failure; five tolerate two. Adding nodes does not increase throughput and can slow the protocol. During a partition, only the majority side can continue. Highly variable network delays make failure detection difficult: long timeouts slow recovery, short timeouts cause disruptive elections. Some algorithms have edge cases under unstable links; the chapter notes Raft pre-vote as a response and mentions leaderless EPaxos-style protocols as more robust to some poorly performing links or nodes.

Coordination services package consensus for distributed applications. ZooKeeper, etcd, and Consul look like key-value stores but are intended for small, slow-changing coordination data, not high-volume application storage. They provide locks and leases, fencing tokens through monotonically increasing log IDs or revisions, session-based failure detection, and change notifications. These features support leader election, work assignment, shard rebalancing, and failure recovery. They can also store configuration and support service discovery, but service discovery often does not require linearizability and may be better served by caching, TTL refresh, and highly available reads. ZooKeeper observers are an example of non-voting replicas that improve read throughput and availability at the cost of possibly stale reads.

The chapter's final lesson is that strong consistency is achievable but not magic. Linearizability makes distributed data easier to reason about, but it requires coordination. Logical clocks help order events causally, but they cannot safely decide winners under faults. Consensus lets nodes agree on decisions, logs, locks, and commits despite crashes and message delays, but it costs majority quorums, latency, and careful implementation. Use consensus when correctness requires it; use weaker consistency when the application can safely tolerate it.

## Chapter-Level Memory Hooks

- Linearizability is the single-copy illusion plus real-time recency.
- Serializability orders transactions; linearizability orders single-object operations by real time.
- Hard uniqueness, leader election, leases, and cross-channel races often need linearizability.
- Quorums overlap, but overlap alone does not guarantee linearizability.
- CAP is mainly about what happens when linearizable replicas are partitioned.
- Linearizability costs latency even when the network is healthy.
- Unique IDs are not necessarily ordered; ordered IDs are not necessarily linearizable.
- Lamport clocks preserve causality but do not provide recency.
- Hybrid logical clocks add wall-clock usefulness while preserving happens-before order.
- Vector clocks can detect concurrency but are larger.
- A timestamp oracle is a small serialization point for linearizable IDs.
- Consensus turns many hard problems into one agreed decision.
- Consensus safety can hold even when the system cannot make progress.
- Practical consensus usually appears as a shared log.
- Epochs/terms plus overlapping quorums prevent split brain.
- New leaders must be up-to-date or committed data may be lost.
- Coordination services are for slow-changing coordination state, not high-write data.

## Interview Perspective

- Explain linearizability using the Aaliyah/Bryce stale-read example.
- Contrast serializability, linearizability, and strict serializability.
- Explain why hard uniqueness constraints need a single up-to-date decision.
- Describe why file storage plus a message queue can race without linearizable storage.
- Explain why leaderless quorum reads/writes are not automatically linearizable.
- State CAP as "consistent or available when partitioned" and explain why "pick two" is misleading.
- Compare ID-generation strategies and their ordering guarantees.
- Explain Lamport clocks, hybrid logical clocks, and vector clocks.
- Explain why logical clocks are insufficient for fault-tolerant lock acquisition.
- State the FLP result and how practical consensus algorithms work around its assumptions.
- List consensus properties and classify safety versus liveness.
- Explain how CAS, shared logs, fetch-and-add, and atomic commit relate to consensus.
- Describe why shared logs are useful for replication and state machine replication.
- Explain leader epochs/terms, quorum overlap, and why stale leaders are dangerous.
- Discuss consensus costs: majority requirement, quorum communication, partitions, and timeout tuning.
- Explain appropriate and inappropriate uses of ZooKeeper, etcd, and Consul.

## Final Takeaways

- Strong consistency is a tool for correctness under replication, not a default requirement for every system.
- Linearizability is the strongest common replicated-data consistency model because it preserves real-time recency.
- Many distributed data races are really failures to preserve ordering across communication channels.
- ID and timestamp design must be judged by the guarantee required: uniqueness, causality, wall-clock approximation, or linearizable order.
- Consensus is the foundation for fault-tolerant linearizable decisions.
- Shared logs are the practical face of consensus in databases and coordination systems.
- Consensus prevents split brain and lost committed writes, but it requires majority quorums and coordination latency.
- Coordination services are valuable because implementing consensus correctly from scratch is difficult and error-prone.
- Weaker consistency remains appropriate when the application can tolerate stale or conflicting state and needs better latency or availability.

Confidence: High
