# Chapter 06: Replication - Compressed Study Summary

Book: DDIA
Chapter: 6, Replication
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 221-268
Raw source used: books/DDIA/raw/chapter-06-chapter-6-replication.md
Method: Concise chapter-level compression based only on the raw Chapter 6 markdown. Long verbatim copying was avoided.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Replication purpose and chapter framing | 221-222 |
| Backups and replication | 222 |
| Single-leader replication | 222-233 |
| Replication lag and consistency guarantees | 233-239 |
| Multi-leader replication | 239-253 |
| Leaderless replication | 253-267 |
| Chapter summary | 267-268 |

## Compressed Chapter Summary

Chapter 6 studies replication: keeping copies of the same data on multiple networked machines. Replication is used to reduce latency by placing data near users, improve availability and durability when machines or regions fail, support disconnected operation, and scale read throughput. The chapter assumes each replica can store the full dataset; sharding is left for the next chapter. Static data is easy to copy, but changing data creates the real challenge: every write must be propagated while machines fail, networks delay messages, and clients may write concurrently.

Replication is not a substitute for backups. Replicas quickly reflect the current state, including accidental deletions. Backups preserve older snapshots so data can be restored to a prior state. The two are complementary: snapshots and replication logs can help initialize followers and support disaster recovery.

Single-leader replication assigns one replica as the write authority. Clients send writes to the leader, and followers apply the leader's changes from a replication log in the same order. Reads can go to the leader or followers, but writes go only to the leader. This makes write ordering easier to understand and is widely used, but it creates leader dependence and raises difficult failover questions.

Replication may be synchronous or asynchronous. Synchronous replication waits for a follower before acknowledging success, giving stronger protection for acknowledged writes but blocking if that follower is unavailable. Fully synchronous replication to all followers is usually impractical, so systems often use semisynchronous replication or quorum-style approaches. Fully asynchronous replication lets the leader keep accepting writes even when followers lag, but if the leader fails before its writes reach followers, acknowledged writes may be lost.

Adding a follower safely requires a consistent leader snapshot and an exact replication-log position. The follower copies the snapshot, replays all changes after that position, catches up, and then continues streaming new changes. Simply copying files while writes are happening can produce an inconsistent copy.

Some systems use object storage for live database data, not only backups. Object stores can reduce cost, provide durable replication, support conditional writes, and simplify integration with open analytical formats. Their tradeoffs are higher latency, API-call costs, batching pressure, immutable-object constraints, and incomplete filesystem semantics. Designs may use tiered storage, separate low-latency WAL storage, or zero-disk architectures where durable state lives in object storage and local disks are cache.

Follower outages are handled by catch-up recovery: the follower remembers the last processed change and asks the leader for missed changes. This can become expensive if the follower was offline for long or the write rate is high. Leader failure is harder. Failover must detect the failure, choose a sufficiently up-to-date follower, redirect clients, and prevent the old leader from continuing as leader. Hazards include lost acknowledged writes with asynchronous replication, split brain, inconsistent external systems, and bad timeout choices.

Replication logs can be implemented at different abstraction levels. Statement-based replication ships executed write statements, but nondeterminism, autoincrement behavior, ordering dependencies, and side effects can make replicas diverge. WAL shipping reuses the physical crash-recovery log, but it tightly couples replication to storage-engine internals and can complicate zero-downtime upgrades. Logical row-based logs describe inserts, updates, deletes, and commits at row level; they are easier to keep compatible across versions and are useful for change data capture.

Asynchronous followers create replication lag. Eventual consistency means replicas should converge if writes stop and lag clears, but there is no fixed bound on how long "eventual" takes. Lag can produce user-visible anomalies. Read-after-write consistency ensures users see their own writes. Monotonic reads prevent a user from seeing newer data and then older data. Consistent prefix reads preserve causal order, such as seeing a question before its answer. Applications can sometimes enforce these guarantees by routing reads to leaders, tracking write timestamps or log positions, or pinning users to replicas, but doing so correctly is complex.

Multi-leader replication allows several leaders to accept writes. It is most useful across regions or disconnected clients because each region or device can accept local writes and replicate them asynchronously later. Compared with a single leader in one region, multi-leader can reduce perceived write latency, survive regional outages better, and tolerate inter-region network problems. Its main cost is weaker consistency: independent leaders cannot reliably enforce global constraints such as uniqueness or nonnegative balances without coordination. It can also interact poorly with autoincrement keys, triggers, and integrity constraints.

Multi-leader topologies determine how writes move between leaders. Circular and star topologies require forwarding and loop-prevention tags but can be disrupted by one failed node. All-to-all topologies avoid some single-path failures but can receive causally related writes out of order. Timestamps do not solve this reliably because distributed clocks are not trustworthy enough for causal ordering; version-vector-like techniques are needed.

Sync engines are a client-side form of multi-leader replication. A phone, laptop, or browser tab keeps local data, accepts writes immediately, and syncs changes later. This supports offline-first and local-first software and real-time collaboration. The benefits are fast UI response, fewer explicit remote-call failure paths in frontend code, and natural integration with reactive UI updates. The limitation is that the needed data must usually be downloaded in advance, and concurrent edits require conflict resolution.

Conflicting writes are the central multi-leader problem. A conflict occurs when concurrent writes are made without either write knowing about the other. Conflict avoidance routes all writes for a record to one leader, but it breaks down when leaders change. Last write wins chooses a timestamp winner and discards the others; it converges but can lose successfully accepted writes and is sensitive to clock skew. Manual conflict resolution stores siblings and asks application code or users to merge them, but it complicates APIs and user experience. Automatic resolution uses deterministic merge algorithms to make replicas converge while preserving intent where possible.

CRDTs and operational transformation are two families of automatic conflict-resolution algorithms. OT transforms operation positions to account for concurrent edits already applied. CRDTs commonly assign immutable IDs to elements and define operations relative to those IDs. Both can merge structures such as text, lists, maps, and counters. They cannot enforce every invariant: if concurrent additions violate a maximum-list-size rule, the system must still drop or reject something.

Leaderless replication removes the leader role entirely. Clients, or coordinator nodes acting for clients, send writes to multiple replicas. Reads also contact multiple replicas so stale values can be detected. Missed writes are repaired through read repair, hinted handoff, and anti-entropy background comparison. The system avoids leader failover, but because no one node defines write order, conflict detection and resolution are required.

Leaderless systems often use quorum reads and writes. For n replicas, w write acknowledgments, and r read responses, w + r > n means the read and write sets overlap, so a read should include at least one replica with the latest successful write. Typical choices use majority values, but r and w are configurable to trade latency, availability, and stale-read probability. The formula has limitations: stale restores, rebalancing, concurrent reads/writes, partial failed writes, clock skew, and concurrent writes can all complicate the guarantee.

Performance differs across architectures. Reading from a single leader gives up-to-date responses but limits read throughput and makes users sensitive to leader slowness or failover. Leaderless systems send requests to multiple replicas and can use the fastest responses, reducing tail latency and handling gray failures more gracefully. However, hinted handoff, larger quorums, network partitions, and sloppy quorum behavior add operational and consistency complexity.

Detecting concurrent writes requires causality, not wall-clock time. Operation A happens before B if B knew about or depended on A. Two operations are concurrent if neither happened before the other. On one replica, version numbers can show which values a client read and merged before writing; values newer than that version remain siblings. With multiple replicas, a single version number is insufficient, so systems use version vectors: per-replica version information that travels with reads and writes. Version vectors distinguish overwrites from concurrent conflicts and allow safe read-from-one-replica, write-to-another behavior if siblings are merged correctly.

## Chapter-Level Memory Hooks

- Replication copies current state; backups preserve past state.
- Single-leader gives one write order but makes failover and leader capacity critical.
- Synchronous replication protects acknowledged writes but can block availability.
- Asynchronous replication improves responsiveness but causes lag and possible data loss on failover.
- Replication lag causes three classic anomalies: missing your own writes, time going backward, and causal order inversion.
- Multi-leader is useful for regions, devices, and collaboration, but conflicts are unavoidable.
- LWW is convergence by discarding writes.
- CRDTs and OT are deterministic merge strategies for preserving more user intent.
- Leaderless systems replace failover with quorum reads/writes plus repair.
- Quorum math is w + r > n, but the edge cases prevent it from being a complete consistency guarantee.
- Happens-before defines concurrency; version vectors encode causal knowledge.

## Interview Perspective

- Define the three architectures: single-leader, multi-leader, leaderless.
- Explain the synchronous/asynchronous durability tradeoff using acknowledged writes.
- For single-leader, discuss follower lag, read scaling, and failover hazards.
- For eventual consistency, give concrete anomalies and the named guarantees that address them.
- For multi-leader, emphasize local write latency and regional resilience versus conflicts and weak global constraints.
- For conflicts, compare avoidance, LWW, siblings/manual merge, CRDTs, and OT.
- For leaderless, write the quorum formula and immediately mention limitations.
- For concurrency, say that "same time" is not the definition; lack of causal awareness is.

## Final Takeaways

- Replication is a fault-tolerance and performance tool, but it introduces distributed-system correctness problems.
- The main replication families differ in where writes go and who determines order.
- Weak consistency is not automatically bad, but applications must be designed for the anomalies it permits.
- Stronger guarantees simplify application code but may increase coordination cost.
- Conflict resolution must match application semantics; generic timestamp winners are often too crude.
- Version vectors are the chapter's key mechanism for reasoning about concurrent writes in leaderless and multi-leader-style systems.

Confidence: High
