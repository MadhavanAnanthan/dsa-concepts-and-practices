# Chapter 06: Replication - Compressed Study Notes

Book: DDIA
Chapter: 6, Replication
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 221-268
Raw source used: books/DDIA/raw/chapter-06-chapter-6-replication.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 6 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter framing: why replication exists and why changing data is hard | 221-222 |
| Backups and replication | 222 |
| Single-leader replication overview | 222-224 |
| Synchronous versus asynchronous replication | 224-225 |
| Setting up new followers | 225-226 |
| Databases backed by object storage | 226-228 |
| Handling node outages and failover | 228-230 |
| Implementation of replication logs | 230-233 |
| Problems with replication lag | 233-239 |
| Multi-leader replication and geo-distributed operation | 239-242 |
| Multi-leader topologies | 242-244 |
| Sync engines and local-first software | 244-246 |
| Dealing with conflicting writes | 246-253 |
| Leaderless replication | 253-255 |
| Quorums and quorum limitations | 255-259 |
| Single-leader versus leaderless performance | 259-260 |
| Multi-region leaderless operation | 260-261 |
| Detecting concurrent writes, happens-before, and version vectors | 261-267 |
| Chapter summary | 267-268 |

## 1. Chapter Frame: Replication Is Easy Until Data Changes

Source pages: 221-222

- Replication means keeping copies of the same data on multiple networked machines.
- Main motivations:
  - Put data closer to users to reduce latency.
  - Keep the system running when machines, zones, or regions fail.
  - Increase read throughput by serving reads from multiple replicas.
- Chapter assumption: every replica can store the full dataset. Sharding is deferred to Chapter 7.
- Static data is straightforward: copy it once. The hard part is propagating ongoing writes while preserving useful behavior under faults.
- Three replication families:
  - Single-leader.
  - Multi-leader.
  - Leaderless.
- Core design choices include synchronous versus asynchronous replication, failed-replica handling, durability, availability, latency, and consistency.

Interview hook: Replication is not just "copy data." The real topic is how replicas process changes during failures, delays, and concurrent writes.

## 2. Backups and Replication Solve Different Problems

Source pages: 222

- Replication quickly propagates current writes to other nodes.
- Backups preserve old snapshots so data can be restored to an earlier point in time.
- If a user accidentally deletes data, replication spreads the deletion; it does not provide historical recovery.
- Replication and backups complement each other:
  - Snapshots can help initialize followers.
  - Archived replication logs can support backup and recovery.
- Some databases keep immutable historical snapshots internally, but older data may be cheaper to store in object storage while primary storage keeps the current state.

Interview hook: Replication protects availability and current-state durability; backups protect against logical mistakes and the need to go back in time.

## 3. Single-Leader Replication: One Write Authority

Source pages: 222-224

- Each node holding a copy is a replica.
- In leader-based replication:
  - One replica is the leader, primary, or source.
  - Clients send writes to the leader.
  - Followers receive the leader's replication log or change stream and apply writes in the same order.
  - Reads may go to the leader or followers, but followers are read-only from the client perspective.
- In a sharded system, each shard has its own leader; different shards may have different leader nodes.
- Widely used in relational databases, document databases, message brokers, replicated block devices, filesystems, and consensus-based systems.

Terminology:

- Leader, primary, source.
- Follower, read replica, secondary, hot standby.
- Replication log, change stream.

Edge case:

- The older term "master-slave" describes the same pattern but should be avoided.

Interview hook: Single-leader replication centralizes write ordering, which simplifies consistency but creates dependence on the leader.

## 4. Synchronous Versus Asynchronous Replication

Source pages: 224-225

- Synchronous replication means the leader waits for a follower to confirm the write before reporting success.
- Asynchronous replication means the leader sends the change without waiting for the follower.
- Synchronous advantage:
  - At least one follower is guaranteed to have an up-to-date copy if the leader fails.
- Synchronous disadvantage:
  - If the synchronous follower is slow or unavailable, writes block.
- Fully synchronous replication to all followers is usually impractical because any follower outage can stop writes.
- Semisynchronous replication commonly keeps one synchronous follower and other asynchronous followers; if the synchronous follower fails, another follower may take that role.
- Some systems synchronously update a majority quorum and leave a minority asynchronous.
- Fully asynchronous replication allows writes to continue while followers lag, but acknowledged writes may be lost if the leader fails before replicating them.
- Asynchronous replication is common when there are many followers or geographically distributed followers.

Tradeoff:

- Synchronous replication improves durability of acknowledged writes but increases write latency and failure sensitivity.
- Asynchronous replication improves availability and responsiveness but permits data loss and stale reads under failure or lag.

Interview hook: "Write acknowledged" does not always mean "safe on multiple machines." Ask whether replication was synchronous, semisynchronous, quorum-based, or asynchronous.

## 5. Setting Up New Followers

Source pages: 225-226

- A naive file copy is unsafe because ongoing writes mean different files may reflect different points in time.
- Locking the database could create a consistent copy but harms availability.
- Typical no-downtime follower setup:
  - Take a consistent snapshot of the leader.
  - Copy the snapshot to the new follower.
  - Record the snapshot's exact position in the leader's replication log.
  - Have the follower replay all changes since that log position.
  - Once the backlog is processed, the follower has caught up and can stream new changes.
- Log positions have database-specific names, such as log sequence number, binlog coordinates, or GTIDs.
- Archived snapshots and replication logs in object storage can support both follower bootstrap and disaster recovery.

Interview hook: Follower bootstrap requires both a consistent snapshot and a precise log position; one without the other is not enough.

## 6. Databases Backed by Object Storage

Source pages: 226-228

- Some databases use object stores for live database data, not only for archive storage.
- Benefits:
  - Lower cost for less frequently accessed data.
  - High durability from object-store replication across zones or regions.
  - Conditional writes can help implement compare-and-set, transactions, and leadership election.
  - Shared object storage plus open formats can simplify analytics integration.
- Object storage can shift work such as durability, replication, and some coordination into the storage layer.
- Tradeoffs:
  - Higher read/write latency than local disks or virtual block devices.
  - Per-API-call costs encourage batching, which increases latency.
  - Immutable objects make random writes expensive.
  - Lack of standard filesystem semantics can limit systems not designed for object storage.
  - FUSE-style mounts may lack POSIX features expected by some software.
- Design responses:
  - Tier cold data into object storage while keeping hot data on SSD/NVMe/memory.
  - Store WAL in a low-latency system while persisting bulk data in object storage.
  - Zero-disk architecture uses local disks and memory only as cache while object storage holds persistent state.

Interview hook: Object storage simplifies durable replicated storage but pushes systems toward batching, immutability-aware design, caching, and separate low-latency write paths.

## 7. Handling Node Outages

Source pages: 228-230

### Follower Failure: Catch-Up Recovery

- Followers keep a local log of changes received from the leader.
- After a crash or temporary network interruption, a follower reconnects and requests changes after its last processed transaction.
- Performance risk:
  - High write volume or long downtime can create a large backlog.
  - Catch-up load affects both follower and leader.
- Leader log retention tradeoff:
  - Keep logs until the follower returns, risking disk exhaustion.
  - Delete old logs and require the follower to be restored from backup.

### Leader Failure: Failover

- Failover promotes a follower to leader, reroutes clients, and makes other followers consume from the new leader.
- Failover may be manual or automatic.
- Automatic failover steps:
  - Detect leader failure, usually by timeout.
  - Choose a new leader, preferably the most up-to-date follower.
  - Reconfigure clients and replicas to use the new leader.
- Failover hazards:
  - With asynchronous replication, the new leader may lack writes acknowledged by the old leader.
  - The old leader's unreplicated writes may be discarded if it rejoins after a new leader exists.
  - External systems may become inconsistent with the database if lost writes included shared identifiers or state.
  - Split brain can occur if two nodes believe they are leaders and accept writes.
  - Timeouts are hard: too long slows recovery; too short causes unnecessary failover.
- Fencing means preventing an old or duplicate leader from continuing to act as leader.
- Some teams prefer manual failover because automatic failover can fail dangerously.

Interview hook: Failover is a correctness problem, not just an availability feature. The key question is whether the promoted follower has the latest acknowledged writes.

## 8. Implementation of Replication Logs

Source pages: 230-233

### Statement-Based Replication

- Leader logs write statements and sends them to followers.
- Followers execute the same statements.
- Problems:
  - Nondeterministic functions can produce different values on each replica.
  - Autoincrement or data-dependent statements require identical execution order.
  - Triggers, stored procedures, and user-defined functions may create nonidentical side effects.
- Workaround: leader can replace nondeterministic calls with fixed values in the log.
- Compact, but deterministic execution is hard to guarantee in practice.

### Write-Ahead Log Shipping

- The same WAL used for crash recovery is shipped to followers.
- Followers replay the low-level storage changes and build the same files as the leader.
- Advantage: direct and exact replication of storage-engine state.
- Disadvantage:
  - WAL describes low-level disk-block changes.
  - Replication is tightly coupled to storage-engine internals.
  - Leader and followers often cannot safely run different database versions, complicating zero-downtime upgrades.

### Logical Row-Based Replication

- Replication log is decoupled from the physical storage format.
- For relational systems, log records usually describe row-level inserts, deletes, and updates.
- Multi-row transactions produce multiple row records plus a commit record.
- Advantages:
  - Easier backward compatibility across database versions.
  - Supports lower-downtime upgrades.
  - Easier for external systems to parse.
  - Useful for change data capture into warehouses, indexes, or caches.

Interview hook: Compare replication logs by level of abstraction: SQL statements, physical WAL bytes, or logical row changes. The lower the level, the tighter the coupling.

## 9. Problems with Replication Lag

Source pages: 233-239

- Read scaling usually requires asynchronous followers.
- Followers can lag behind the leader, so reads from followers may return stale data.
- Eventual consistency means replicas should converge if writes stop and lag drains, but "eventual" has no fixed time bound.
- Lag can be subsecond in normal cases but may grow to seconds or minutes under capacity pressure, recovery, or network trouble.

### Read-After-Write Consistency

Source pages: 234-235

- Problem: a user writes data, then immediately reads from a stale follower and cannot see their own update.
- Guarantee: users always see their own submitted updates.
- Implementation options:
  - Read user-editable data from the leader or a synchronous follower.
  - For a short interval after a user's update, route their reads to the leader.
  - Track the user's latest write timestamp or log position and serve reads only from replicas caught up to that point.
  - In multi-region systems, route leader-required reads to the leader's region.
- Multi-device complication:
  - A user's last-write metadata must be centralized across devices.
  - Devices may route to different regions, so requests may need region coordination.

### Regions and Availability Zones

Source pages: 236

- A region is a geographic location containing one or more datacenters.
- A zone is a separate datacenter in a region.
- Multi-zone deployment can survive zonal outages.
- Multi-region deployment can survive regional outages but adds latency, lowers throughput, and increases networking cost.

### Monotonic Reads

Source pages: 236-237

- Problem: a user reads from a fresh follower, then a stale follower, and appears to go backward in time.
- Guarantee: after a user sees newer data, later reads by that user should not return older data.
- One implementation: route each user's reads to the same replica, for example by hashing user ID.
- Edge case: if the chosen replica fails, rerouting is needed and must preserve the guarantee if possible.

### Consistent Prefix Reads

Source pages: 237-238

- Problem: causally related writes may appear out of order, such as seeing an answer before the question.
- Guarantee: if writes happened in a certain order, readers see them in that order.
- In sharded systems, independent shards may not share a global write order, so readers may see a mixed-time state.
- Possible solution: place causally related writes on the same shard, though this may be inefficient or impossible.
- More general approaches track causal dependencies.

### Solutions for Replication Lag

Source pages: 238-239

- Applications should explicitly consider what happens if lag grows to minutes or hours.
- Stronger guarantees can sometimes be built in application code by carefully routing reads, but this is complex.
- Strongly consistent databases with transactions provide a simpler programming model by hiding many replication anomalies.
- Weaker consistency remains useful because it can offer better resilience to network interruptions and lower overhead.

Interview hook: Eventual consistency is not a user-facing requirement. Translate it into concrete anomalies: lost-looking writes, time going backward, and causality violations.

## 10. Multi-Leader Replication

Source pages: 239-242

- Single-leader drawback: if clients cannot reach the one leader, they cannot write.
- Multi-leader replication allows more than one node to accept writes.
- Each leader also behaves as a follower of other leaders.
- Synchronous multi-leader resembles single-leader behavior because writes still block on inter-leader communication.
- The chapter focuses on asynchronous multi-leader replication, where leaders can continue accepting writes during inter-leader network interruption.

### Geo-Distributed Operation

- Multi-leader is most useful across regions, not usually within one region.
- Pattern:
  - Inside each region, use leader-follower replication.
  - Between regions, leaders replicate changes to one another.
- Compared with single-leader multi-region systems:
  - Performance: local-region writes avoid waiting for distant leader round trips.
  - Regional outages: each region can keep operating and catch up later.
  - Network problems: asynchronous inter-region replication tolerates interruptions better.
  - Consistency: weaker than single-leader; global constraints such as uniqueness or nonnegative balances cannot be reliably enforced across independent leaders.
- Multi-leader is less common and can have pitfalls involving autoincrement keys, triggers, and integrity constraints.

Interview hook: Multi-leader improves write availability and regional latency by accepting weaker consistency and requiring conflict resolution.

## 11. Multi-Leader Topologies

Source pages: 242-244

- A replication topology defines how writes travel between leaders.
- Common topologies:
  - Circular: each node forwards to the next.
  - Star/tree: a root forwards writes to others.
  - All-to-all: every leader sends writes to every other leader.
- Circular and star topologies require nodes to forward changes and tag writes with node identifiers to avoid infinite loops.
- Circular/star weakness:
  - One failed node can interrupt replication paths until reconfigured.
- All-to-all advantage:
  - Better fault tolerance through multiple paths.
- All-to-all weakness:
  - Messages can arrive in different orders at different replicas.
  - An update may arrive before the insert it depends on.
- Timestamps are not enough to solve causal ordering because clocks cannot be trusted to be sufficiently synchronized.
- Version vectors can help order causally related updates, but not all multi-leader systems use robust techniques.

Interview hook: All-to-all removes a single replication path bottleneck, but it exposes message reordering and causal dependency problems.

## 12. Sync Engines and Local-First Software

Source pages: 244-246

- Multi-leader also applies to client devices that need to work while disconnected.
- Example: calendar apps on phone and laptop each keep a local replica that accepts writes offline and syncs later.
- In this model, each device behaves like a region with unreliable connectivity.
- Real-time collaboration also has a multi-leader shape:
  - Each browser tab or device can accept edits immediately.
  - Edits are asynchronously replicated to collaborators.
  - Concurrent edits may require conflict resolution.
- A sync engine captures local changes, sends them when possible, receives remote changes, merges them, and updates local UI.
- Offline-first software continues working without network access.
- Local-first software goes further by being designed to keep working even if the original online service disappears, often through open sync protocols.

Advantages of sync engines:

- Local data enables very fast UI response.
- Offline work is treated like large network delay, not a separate mode.
- Frontend code can read/write local state rather than handle every remote-call failure path.
- Reactive UI updates fit naturally with incoming collaborator changes.

Limitations:

- Works best when the needed data can be downloaded in advance.
- Poor fit when the user can access a huge dataset, such as an entire ecommerce catalog.
- Requires careful merge/conflict behavior.

Interview hook: Sync engines move persistence to the client and make server communication a background replication concern.

## 13. Conflicting Writes in Multi-Leader Systems

Source pages: 246-253

- Core issue: two leaders can accept concurrent writes to the same record.
- Writes are concurrent when neither write was aware of the other, not merely when wall-clock times overlap.

### Conflict Avoidance

Source pages: 247-248

- Route all writes for a given record through the same leader.
- Example: user-owned data can be assigned to a home region.
- Breaks down when the designated leader changes during failover or user relocation.
- For inserts, ID spaces can be partitioned, such as odd IDs on one leader and even IDs on another.

### Last Write Wins

Source pages: 248-249

- LWW attaches timestamps and keeps the greatest timestamp.
- For concurrent writes, "last" is not well-defined; the chosen winner is effectively arbitrary.
- LWW ensures convergence but can silently discard successfully accepted writes.
- Safe mainly when conflicts are avoided, such as unique-key inserts with no updates.
- Real-time clocks make LWW sensitive to clock skew; a fast clock can cause later writes to be ignored.

### Manual Conflict Resolution

Source pages: 249-250

- Store concurrent values as siblings and return them to the application or user.
- Application or user writes back a resolved value.
- Problems:
  - API becomes more awkward because a field may become a set of possible values.
  - User-facing conflict resolution is hard to design and confusing for users.
  - Automatic sibling merging can have surprising behavior, such as deleted shopping cart items reappearing.
  - Concurrent conflict resolutions can themselves conflict if not deterministic.

### Automatic Conflict Resolution

Source pages: 250-251

- Goal: all replicas that have processed the same writes converge to the same state regardless of arrival order.
- Eventual consistency plus convergence is strong eventual consistency.
- Merge strategies can preserve intent better than LWW:
  - Text: track inserted/deleted characters and deterministically order concurrent inserts.
  - Collections: track insertions and deletions so deleted items do not reappear.
  - Counters: combine increments and decrements without double-counting or dropping updates.
  - Maps: merge each key independently using appropriate value-level rules.
- Limit: conflict resolution cannot enforce every constraint. If concurrent additions exceed a list limit, some item must be dropped.

### CRDTs and Operational Transformation

Source pages: 251-252

- CRDTs and OT are two algorithm families for automatic conflict resolution.
- OT records operations by positions and transforms indexes to account for concurrent edits already applied.
- CRDTs often assign immutable IDs to elements and express insertions/deletions relative to those IDs.
- Both can support text, lists, maps, and similar data structures.
- OT is commonly associated with collaborative text editing; CRDTs appear in some distributed databases and sync engines.

### Subtle Conflict Types

Source pages: 252-253

- Conflicts are not limited to two writes on the same field.
- Example: two concurrent meeting room bookings can violate the rule that a room cannot be double-booked, even though each write inserts a new record.
- Such conflicts require deeper constraint reasoning and are revisited in later chapters.

Interview hook: Conflict resolution is application semantics. LWW gives convergence by data loss; CRDT/OT-style approaches try to preserve user intent but cannot enforce all global invariants.

## 14. Leaderless Replication

Source pages: 253-255

- Leaderless systems let any replica accept writes directly or through a coordinator.
- Unlike leader-based systems, no single leader defines the write order.
- Dynamo-style systems inspired Riak, Cassandra, and ScyllaDB.
- DynamoDB is named similarly but uses a different single-leader consensus-based architecture in the chapter's description.

### Writing When a Node Is Down

- With three replicas, a client can send a write to all replicas and consider it successful after two acknowledgments.
- A down replica misses the write.
- Reads also contact multiple replicas in parallel.
- Responses include versions/timestamps so the client can choose the newest value or detect conflicts.

### Catching Up on Missed Writes

- Read repair:
  - Reads from multiple replicas reveal stale responses.
  - The client writes the newer value back to stale replicas.
  - Works best for frequently read values.
- Hinted handoff:
  - Another replica temporarily stores writes intended for an unavailable replica.
  - When the unavailable replica returns, hints are handed over.
  - Helps values that are not read.
- Anti-entropy:
  - A background process compares replicas and copies missing data.
  - Does not preserve one global write order and may run with significant delay.

Interview hook: Leaderless systems trade centralized ordering for parallel reads/writes, repair mechanisms, and conflict handling.

## 15. Quorum Reads and Writes

Source pages: 255-259

- Let:
  - n = number of replicas for a value.
  - w = acknowledgments needed for a successful write.
  - r = responses needed for a successful read.
- If w + r > n, the read set and write set overlap, so at least one read replica should have the latest successful write.
- Common choice: odd n, with w and r as majority values.
- Tuning:
  - w < n allows writes despite unavailable nodes.
  - r < n allows reads despite unavailable nodes.
  - w = n, r = 1 favors fast reads but any unavailable replica blocks writes.
- Reads and writes are usually sent to all n replicas in parallel; r and w define how many successes to wait for.
- A failed operation means fewer than r or w replicas responded successfully, regardless of the specific failure cause.

### Quorum Limitations

- Quorums are not absolute guarantees in all edge cases.
- Risks include:
  - Restoring a failed node from stale data can reduce the number of replicas holding the newer value below w.
  - Rebalancing can make nodes disagree about which replicas own a key.
  - Concurrent reads and writes may produce nonmonotonic behavior.
  - Writes that succeed on some replicas but fewer than w are not rolled back, so later reads may or may not see them.
  - Real-time-clock timestamps can silently drop writes when clocks are skewed.
  - Concurrent writes can be ordered differently on different replicas.
- Dynamo-style databases are optimized for workloads that tolerate eventual consistency.
- w and r tune the probability of stale reads; they should not be treated as simple absolute consistency switches.

### Monitoring Staleness

- Leader-based replication can measure lag by comparing follower log positions with the leader's log position.
- Leaderless systems lack a fixed write order, making staleness harder to quantify.
- Hint counts may indicate health, but are difficult to interpret.

Interview hook: The quorum formula is necessary to know, but the real answer includes its edge cases and operational limitations.

## 16. Single-Leader Versus Leaderless Performance

Source pages: 259-260

- Reading from the leader gives up-to-date data but has performance problems:
  - Read throughput is limited by leader capacity.
  - Failover creates visible delay or unavailability.
  - Leader slowness directly affects users.
- Leaderless advantages:
  - No failover role transition.
  - Requests already go to multiple replicas in parallel.
  - A slow or unavailable replica can be ignored in favor of faster responses.
  - Request hedging can reduce tail latency.
  - Gray failures and overloaded nodes are less disruptive because the system does not need to decide whether to fail over.
- Leaderless drawbacks:
  - Hinted handoff and recovery add load during stressed periods.
  - Larger quorums increase response-time risk because more responses are needed.
  - Network interruptions can prevent quorum formation.
  - Sloppy quorum or consistency level ANY can accept writes on reachable nonstandard replicas, but later reads are not guaranteed to see them.
- Multi-leader can be even more resilient to network interruptions because local leaders can handle reads/writes alone, but remote replicas may be arbitrarily stale.

Interview hook: Leaderless systems improve tail behavior by treating slow replicas as normal; the price is weaker consistency and repair complexity.

## 17. Multi-Region Leaderless Operation

Source pages: 260-261

- Leaderless replication is suitable for multi-region systems because it already handles concurrent writes, network interruptions, and latency spikes.
- Cassandra and ScyllaDB pattern:
  - Client sends write to a local coordinator.
  - Coordinator forwards within the local region and to one replica per remote region.
  - Remote replicas forward within their own region.
  - This avoids repeated cross-region requests.
- Consistency options can require:
  - A quorum across all regions.
  - A separate quorum in each region.
  - Only a local-region quorum.
- Local quorums avoid waiting on remote regions but are more likely to return stale data.
- Riak keeps client/database communication local to a region and performs asynchronous cross-region replication in a multi-leader-like style.

Interview hook: In multi-region leaderless systems, "consistency level" is also a latency and geography choice.

## 18. Detecting Concurrent Writes

Source pages: 261-267

- Leaderless systems can receive concurrent writes to the same key in different orders at different replicas.
- If each replica simply overwrites based on arrival order, replicas may permanently disagree.
- Convergence requires conflict resolution such as LWW, manual resolution, or CRDTs.
- LWW is easy but timestamps do not reveal whether writes were causally ordered or concurrent.

### Happens-Before Relation

Source pages: 262-263

- Operation A happens before operation B if B knew about, depended on, or built on A.
- Two operations are concurrent if neither happened before the other.
- Physical time overlap is not the definition of concurrency; awareness and causal dependency are.
- Distributed clocks make exact timing unreliable, and network delays can make operations concurrent even when they occur at different wall-clock times.

### Capturing Happens-Before on One Replica

Source pages: 263-265

- Server keeps a version number per key.
- Each write increments the version and stores it with the value.
- Reads return all non-overwritten values, called siblings, plus the latest version.
- Clients must include the version from their prior read when writing.
- When a write arrives:
  - Values at or below the supplied version can be overwritten because the client saw and merged them.
  - Values with higher versions must be kept because they are concurrent with the incoming write.
- A write without a prior version is concurrent with existing writes and cannot overwrite them.
- Example: two clients add shopping-cart items concurrently; versions show which values supersede older values and which values remain siblings.

### Version Vectors

Source pages: 266-267

- A single version number is insufficient when multiple replicas accept writes.
- Use a version number per replica and per key.
- Each replica increments its own version and tracks versions seen from other replicas.
- The collection is a version vector.
- Version vectors are exchanged on reads and sent back on writes.
- They let the database distinguish overwrites from concurrent writes.
- Reading from one replica and writing to another is safe if siblings are merged correctly.

Interview hook: Causality, not wall-clock time, is the basis for detecting concurrent writes. Version vectors encode what each write had already seen.

## Chapter-Level Memory Hooks

- Replication copies current data; backups preserve history.
- Single-leader: one write authority, easier ordering, leader bottleneck/failover risk.
- Synchronous replication protects acknowledged writes but can block writes.
- Asynchronous replication improves availability but permits lag and data loss on failover.
- Follower bootstrap needs a consistent snapshot plus replication-log position.
- Failover is risky because the newest leader candidate may not contain all acknowledged writes.
- Replication log choices: statements, physical WAL, logical row changes.
- Eventual consistency means convergence later, not immediate correctness.
- Replication lag anomalies: read-your-writes failure, time going backward, causal order inversion.
- Multi-leader improves regional write availability but weakens global constraints.
- Conflict resolution is mandatory in multi-leader and leaderless systems.
- LWW converges by discarding writes.
- CRDTs and OT preserve more intent by merging concurrent changes deterministically.
- Leaderless systems use parallel reads/writes, repair, hints, anti-entropy, and quorums.
- Quorum rule: w + r > n creates overlap, but edge cases still matter.
- Happens-before defines concurrency; version vectors capture causal knowledge.

## Interview Perspective

- Start with why replication exists: latency, availability, durability, read scalability, disconnected operation.
- Distinguish replication from backup before discussing durability.
- For single-leader systems, emphasize leader write ordering, follower read scaling, replication lag, and failover hazards.
- For synchronous versus asynchronous replication, connect the choice directly to acknowledged-write durability and write availability.
- Explain eventual consistency through user-visible anomalies, not slogans.
- Know the three lag guarantees: read-after-write, monotonic reads, consistent prefix reads.
- For multi-leader, state the central tradeoff: local writes and regional resilience in exchange for conflicts and weaker global invariants.
- Use examples: wiki title conflict, shopping cart deleted-item reappearance, room booking constraint conflict.
- Compare conflict strategies: avoidance, LWW, manual siblings, CRDT/OT.
- For leaderless, define n, w, r and explain w + r > n plus limitations.
- Mention operational monitoring: leader lag is measurable by log positions; leaderless staleness is harder.
- For concurrency, say that wall-clock timestamps are not causality; version vectors are about what each write has observed.

## Final Takeaways

- Replication is fundamentally about managing change under failure, delay, and concurrency.
- Single-leader replication is common because it gives one clear write order, but asynchronous followers introduce stale reads and failover can lose acknowledged writes.
- Multi-leader replication supports local writes across regions or devices, but conflicts and weak global constraints are unavoidable.
- Leaderless replication avoids leader failover and can reduce tail latency, but it relies on quorum tuning, repair mechanisms, and conflict detection.
- Eventual consistency must be evaluated through concrete application behavior when lag becomes large.
- Strong eventual consistency requires convergence plus a conflict-resolution algorithm that preserves the intended semantics of writes as much as possible.
- Version vectors and related mechanisms are the bridge between replication and causality: they let systems distinguish overwrites from concurrent conflicts.

Confidence: High
