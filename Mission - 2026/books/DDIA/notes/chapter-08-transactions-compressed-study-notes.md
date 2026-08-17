# Chapter 08: Transactions - Compressed Study Notes

Book: DDIA
Chapter: 8, Transactions
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 301-361
Raw source used: books/DDIA/raw/chapter-08-chapter-8-transactions.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 8 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter frame: why transactions exist | 301-302 |
| What exactly is a transaction? | 302-303 |
| The meaning of ACID | 303-307 |
| Replication and durability | 307-308 |
| Single-object and multi-object operations | 308-311 |
| Handling errors and aborts | 311-312 |
| Weak isolation levels overview | 312-314 |
| Read committed | 314-317 |
| Snapshot isolation and repeatable read | 317-323 |
| Preventing lost updates | 323-327 |
| Write skew and phantoms | 327-332 |
| Serializability overview | 332-333 |
| Actual serial execution | 333-337 |
| Two-phase locking | 337-341 |
| Serializable snapshot isolation | 341-347 |
| Distributed transactions and atomic commit | 347-348 |
| Two-phase commit | 348-352 |
| Distributed transactions across different systems | 352-357 |
| Database-internal distributed transactions | 357 |
| Exactly-once message processing revisited | 358-359 |
| Chapter summary | 359-361 |

## 1. Chapter Frame: Transactions Turn Many Failure Cases into Abort and Retry

Source pages: 301-302

- Data systems fail in many ordinary ways:
  - Database software or hardware can fail during a write.
  - Application code can crash halfway through a sequence of operations.
  - Networks can cut off clients from databases or database nodes from each other.
  - Concurrent clients can overwrite each other's changes.
  - Reads can observe partially updated data.
  - Race conditions can create rare but serious bugs.
- Transactions group reads and writes into one logical unit.
- A transaction either commits as a whole or aborts/rolls back as a whole.
- The point is not that transactions are natural or inevitable; they are a programming model that lets the application ignore selected partial-failure and concurrency hazards because the database takes responsibility for them.
- Some systems weaken or avoid transactions for performance or availability, but the source warns that missing transaction guarantees can create serious reliability failures.

Interview hook: A transaction is best explained as an abstraction for reducing partial failure and concurrency complexity, not merely as "BEGIN/COMMIT syntax."

## 2. What Exactly Is a Transaction?

Source pages: 302-303

- Relational databases and some nonrelational databases support transactions.
- The dominant model descends from IBM System R: many implementation details changed, but the core idea remained similar across MySQL, PostgreSQL, Oracle, SQL Server, and others.
- Many late-2000s NoSQL databases dropped transactions or used the term for much weaker guarantees.
- A common belief that transactions cannot scale is described as wrong: systems such as CockroachDB, TiDB, Spanner, FoundationDB, and YugabyteDB combine sharding with consensus to provide strong ACID guarantees at scale.
- That does not make transactions universally necessary. They remain a design choice with advantages and limitations.

Interview hook: Avoid the simplistic "transactions do not scale" claim. The more precise point is that transactions have costs and design constraints, but scalable transactional systems exist.

## 3. ACID Is Useful Terminology, but the Details Matter

Source pages: 303-307

- ACID stands for atomicity, consistency, isolation, and durability.
- The source cautions that "ACID compliant" is often ambiguous because databases differ in their exact guarantees, especially around isolation.
- BASE is described as even vaguer than ACID, essentially meaning "not ACID."

### Atomicity

Source pages: 304

- In ACID, atomicity is about what happens if a transaction contains multiple writes and a fault occurs after only some writes have happened.
- If the transaction cannot commit, the database aborts it and discards or undoes its writes.
- Atomicity is not about concurrent access. Concurrency belongs under isolation.
- The key practical value is retry safety: after an abort, the application can retry knowing the aborted transaction did not partly change the database.
- The source notes that "abortability" would be a clearer name, but "atomicity" is the conventional term.

### Consistency

Source pages: 304-305

- "Consistency" has many meanings in the book:
  - Replica consistency in replication.
  - Consistent snapshots.
  - Consistent hashing.
  - Linearizability in the CAP theorem.
  - ACID consistency as application-specific validity.
- ACID consistency means the database starts in a valid state and committed transactions preserve declared or application-enforced invariants.
- Examples of invariants include balanced credits and debits in accounting.
- Some invariants can be declared as schema constraints: foreign keys, uniqueness constraints, and check constraints.
- More complex invariants may require triggers or materialized views.
- If an invariant is not declared or enforceable by the database, the database cannot protect it; the application must construct transactions correctly.

Interview hook: The C in ACID is partly an application responsibility. The database can only enforce invariants it knows how to check.

### Isolation

Source pages: 305-306

- Isolation addresses concurrent transactions accessing the same records.
- Example: two clients read a counter value, both add 1, and both write back the same new value, losing one increment.
- The classic formalization is serializability: committed results must be the same as if transactions had executed one at a time.
- Many databases use weaker isolation levels for performance reasons, so some race conditions remain possible.
- The source notes naming traps, including systems whose "serializable" label may actually mean weaker behavior.

### Durability

Source pages: 306-307

- Durability promises that once a transaction commits, the database will not forget its writes even after crashes or hardware faults.
- On one node this usually means nonvolatile storage, fsync-like durability barriers, a write-ahead log, and checksums to detect corrupted or incomplete log entries.
- In replicated databases, durability may mean data has been copied to some number of nodes before commit is acknowledged.
- Perfect durability does not exist; durability techniques reduce risk rather than eliminate it.

Interview hook: Atomicity is "all or nothing on failure"; isolation is "as if alone with respect to concurrency"; durability is "committed writes survive expected failures"; consistency is "invariants remain valid."

## 4. Replication and Durability: Use Several Risk-Reduction Techniques

Source pages: 307-308

- Writing to disk and replication solve different durability risks.
- Disk durability can preserve data when a machine dies, but the data may be unavailable until the disk is recovered or moved.
- Replication can preserve availability, but correlated faults, leader failure under async replication, firmware bugs, filesystem bugs, silent corruption, and backup corruption remain risks.
- Fsync and storage devices are not perfect.
- The chapter's practical conclusion: use disk persistence, remote replication, and backups together; treat theoretical durability guarantees with caution.

Interview hook: Do not frame "replication versus disk" as a total replacement. The chapter frames both as imperfect techniques that should often be combined.

## 5. Single-Object and Multi-Object Operations

Source pages: 308-311

- Atomicity and isolation are most visible when a transaction updates several objects that must stay in sync.
- Email example:
  - A system stores both messages and an unread counter.
  - If another transaction sees the new message before seeing the counter update, it observes an inconsistent intermediate state.
  - If the message insert succeeds but the counter update fails, data becomes inconsistent unless the insert is rolled back.
- Relational databases often define a transaction by a client connection: operations between `BEGIN TRANSACTION` and `COMMIT` belong together; interrupted connections abort the transaction.
- Some nonrelational multi-object APIs do not provide transaction semantics: a multi-put may partially succeed.

### Single-Object Writes

Source pages: 310-311

- Most storage engines provide atomicity and isolation for a single object on one node.
- Example risks avoided:
  - Storing a partial JSON document after a network interruption.
  - Splicing old and new data after power loss.
  - Letting readers see a partially updated object.
- Single-object atomicity can use crash recovery logs.
- Single-object isolation can use object locks.
- Atomic increment and conditional writes prevent some lost updates.
- But single-object operations are not transactions in the usual multi-object sense.
- The source gives examples of single-object linearizable reads or conditional writes that still give no cross-object guarantees.

### Why Multi-Object Transactions Are Needed

Source pages: 311

- Multi-object transactions help when writes to several objects must be coordinated:
  - Relational foreign keys and graph edges must remain valid.
  - Denormalized documents must be updated together.
  - Secondary indexes must be updated consistently with base records.
- Without multi-object transactions, error handling and concurrency control become application problems.

Interview hook: Single-object atomicity is common, but it does not solve denormalized data, secondary indexes, foreign keys, graph references, or multi-row invariants.

## 6. Handling Errors and Aborts

Source pages: 311-312

- A central transaction feature is safe abort and retry.
- ACID systems prefer aborting a transaction over allowing a partially finished result that violates guarantees.
- Some leaderless systems use a best-effort approach: they may not undo already completed writes, so recovery shifts to the application.
- Retrying aborted transactions is useful but has limits:
  - If commit succeeded but the acknowledgment was lost, retrying can duplicate effects unless deduplication exists.
  - Retrying under overload or high contention can worsen the problem.
  - Retry is useful for transient failures, not permanent errors like constraint violations.
  - Side effects outside the database, such as sending email, may not roll back.
  - If the client crashes during retry, unsent data in that client is lost.

Interview hook: "Abort and retry" is only safe when the operation is idempotent or deduplicated and the failure is transient.

## 7. Weak Isolation Levels: Useful but Dangerous

Source pages: 312-314

- Concurrency issues appear when one transaction reads data that another transaction is modifying, or when multiple transactions modify the same data.
- Timing-sensitive concurrency bugs are hard to reproduce and hard to reason about.
- Serializable isolation would make concurrency look like serial execution, but many databases use weaker isolation for performance.
- Weak isolation levels protect against only some anomalies.
- The chapter emphasizes that "ACID database" is not enough: many popular relational databases use weak isolation by default.
- Attackers may deliberately send highly concurrent requests to exploit race conditions, so concurrency bugs are also security concerns.

Interview hook: In interviews, ask "Which isolation level?" before assuming a relational database prevents a given race.

## 8. Read Committed

Source pages: 314-317

- Read committed provides two guarantees:
  - No dirty reads: a transaction reads only committed data.
  - No dirty writes: a transaction overwrites only committed data.

### No Dirty Reads

Source pages: 314-315

- A dirty read occurs when one transaction reads another transaction's uncommitted write.
- Read committed prevents this: writes become visible only when the writing transaction commits.
- This matters because:
  - A reader should not see a partially updated multi-row state.
  - A reader should not make decisions based on data that may later be rolled back.
  - Dirty reads can cause cascading aborts.

### No Dirty Writes

Source pages: 315-316

- A dirty write occurs when one transaction overwrites another transaction's uncommitted write.
- Read committed prevents this by making the second writer wait until the first writer commits or aborts.
- Used-car sales example:
  - If two buyers update listing and invoice rows concurrently, dirty writes could mix the listing winner with the wrong invoice recipient.
- Read committed does not prevent lost updates when the second write happens after the first has committed.

### Implementation

Source pages: 316-317

- Dirty writes are usually prevented with row-level locks held until commit or abort.
- Dirty reads can be prevented by read locks, but that hurts read latency and operability.
- A common alternative keeps both old committed and new uncommitted row versions; readers see the old committed value until commit.
- Read uncommitted is weaker: it prevents dirty writes but allows dirty reads.

Interview hook: Read committed is not "safe transactions." It prevents dirty reads and dirty writes, but lost updates, read skew, phantoms, and write skew can still happen.

## 9. Snapshot Isolation and Repeatable Read

Source pages: 317-323

- Read committed can still produce read skew.
- Bank account example:
  - A transfer moves money between two accounts.
  - A reader sees one account before the transfer and the other after the transfer.
  - The total appears wrong even though each individual read saw committed data.
- Read skew is also called nonrepeatable read in some cases.
- It is temporary for a user refreshing a page, but serious for:
  - Backups, because inconsistent snapshots can become permanent when restored.
  - Analytics and integrity checks, because long scans can mix old and new states.
- Snapshot isolation gives each transaction a consistent view of the database as of one point in time.
- Snapshot isolation is especially useful for long-running read-only queries.

### MVCC

Source pages: 319-322

- Snapshot isolation is commonly implemented with multiversion concurrency control (MVCC).
- Writes use locks to avoid dirty writes, but reads do not require locks.
- Principle: readers do not block writers, and writers do not block readers.
- The database stores multiple committed versions of a row so different in-progress transactions can see different historical snapshots.
- In the PostgreSQL-style explanation:
  - Each transaction gets an increasing transaction ID.
  - Rows record which transaction inserted them.
  - Deletions mark rows as deleted rather than immediately removing them.
  - Updates act like delete plus insert.
  - A garbage collection process removes old versions when no transaction can see them.
- Visibility rules decide which row versions a transaction can see:
  - Ignore writes from transactions in progress when the reader started.
  - Ignore writes from transactions that started later.
  - Ignore writes from aborted transactions.
  - Show other writes.
- Long-running transactions can keep reading old versions that have been overwritten for newer transactions.

### Indexes and Snapshot Isolation

Source pages: 322

- Index entries may point to one version of a row and queries may follow version links to find a visible version.
- Garbage collection can remove old row versions and related index entries once no transaction can see them.
- Some databases store deltas rather than full copies.
- CouchDB, Datomic, and LMDB use immutable copy-on-write B-tree variants where each new root represents a consistent snapshot.

### Naming Confusion

Source pages: 322-323

- MVCC often implements snapshot isolation, but naming varies:
  - PostgreSQL calls snapshot isolation "repeatable read."
  - Oracle calls its snapshot isolation-like level "serializable."
  - MySQL "repeatable read" is weaker than snapshot isolation under the definition cited in the chapter.
  - Db2 uses "repeatable read" for serializability.
- The SQL standard lacks snapshot isolation and its isolation definitions are described as ambiguous and flawed.

Interview hook: Always map database-specific isolation names to actual anomalies prevented. Do not rely on the label alone.

## 10. Preventing Lost Updates

Source pages: 323-327

- Lost update occurs when two transactions read a value, each computes a new value, and the later write overwrites the earlier write without including it.
- Common examples:
  - Counter increments or account balance updates.
  - Local modification inside a JSON document.
  - Two users editing and saving a whole wiki page.

### Atomic Write Operations

Source pages: 324

- Best when the update can be expressed directly in the database.
- Example: `UPDATE counters SET value = value + 1 WHERE key = 'foo';`
- Document databases and Redis also provide atomic operations for certain structures.
- ORMs can accidentally encourage unsafe read-modify-write cycles instead of database atomic operations.

### Explicit Locking

Source pages: 324-325

- The application can lock objects before performing read-modify-write.
- Example: select a game figure `FOR UPDATE`, check rules in application code, then update position.
- Risk: developers must remember every necessary lock.
- Locking multiple objects can deadlock; databases may abort one transaction and require application retry.

### Automatic Lost Update Detection

Source pages: 325-326

- Some snapshot isolation implementations detect lost updates and abort the offending transaction.
- The source names PostgreSQL repeatable read, Oracle serializable, and SQL Server snapshot isolation as doing this.
- MySQL/InnoDB repeatable read does not detect lost updates under the cited definition.
- Advantage: no special application feature must be used, but the application must retry aborted transactions.

### Conditional Writes

Source pages: 326

- Compare-and-set style writes update only if the value has not changed since it was read.
- Wiki example: update content only where the current content or version still matches the value seen earlier.
- The application must check whether the update happened and retry if necessary.
- MVCC implementations may need special behavior so an `UPDATE` or `DELETE` predicate can see concurrent writes even if they are not visible in the transaction snapshot.

### Conflict Resolution and Replication

Source pages: 326-327

- Locks and conditional writes assume one up-to-date copy.
- Multi-leader or leaderless replication allows concurrent writes on different replicas, so those techniques do not directly apply.
- Such systems often create conflicting versions and later merge them.
- Commutative operations can be merged without lost updates; CRDTs use that idea.
- Last-write-wins conflict resolution is prone to lost updates.

Interview hook: Lost update is about two read-modify-write cycles on the same logical value. Name the remedy: atomic update, explicit lock, automatic detection, CAS/version check, or mergeable commutative operation.

## 11. Write Skew and Phantoms

Source pages: 327-332

- Write skew is a subtler anomaly than dirty write or lost update.
- Doctor on-call example:
  - At least one doctor must remain on call.
  - Two doctors each read that two doctors are on call.
  - Each updates only their own row to go off call.
  - Both commit under snapshot isolation, leaving zero doctors on call.
- This is not a dirty write or lost update because the transactions update different rows.
- If the transactions had run serially, the second would have been prevented.
- Write skew can be viewed as a generalization of lost update:
  - Transactions read overlapping data.
  - Then they update some of that data, possibly different objects.

### What Helps and What Does Not

Source pages: 328-329

- Atomic single-object operations do not help because multiple objects are involved.
- Lost update detection in snapshot isolation does not detect write skew in the systems named by the chapter.
- Some constraints can help if the invariant fits database constraints, such as uniqueness.
- Multi-object constraints are often not directly supported, though triggers or materialized views may help.
- If serializable isolation is unavailable, explicit locking of dependency rows is the second-best option when there are rows to lock.

### More Examples

Source pages: 329-331

- Meeting room booking:
  - Check for no overlapping bookings, then insert.
  - Snapshot isolation can allow two overlapping inserts.
- Multiplayer game:
  - Locking one piece prevents two moves of that piece, but may not prevent two pieces from moving to the same square.
- Claiming a username:
  - Check if username is free, then insert.
  - Snapshot isolation alone is unsafe, but uniqueness constraint solves this case.
- Preventing double spending:
  - Two concurrent spending records can each appear valid alone but together make the balance negative.

### Phantoms

Source pages: 331

- Common pattern:
  - A query checks whether a condition holds.
  - Application decides based on that result.
  - Application writes data that changes the result of that condition.
- A phantom occurs when one transaction's write changes the result of another transaction's search query.
- Snapshot isolation avoids simple read-only phantom reads, but read/write transactions can still suffer phantom-related write skew.
- `SELECT FOR UPDATE` cannot lock rows that do not exist, so absence checks are tricky.

### Materializing Conflicts

Source pages: 331-332

- If there is no row to lock, the application can create artificial lock rows.
- Meeting room example: pre-create room/time-slot rows and lock the relevant slots before inserting a booking.
- This turns a phantom conflict into a concrete lock conflict.
- The source treats this as a last resort because it is hard, error-prone, and leaks concurrency control into the data model.

Interview hook: Write skew often follows "read a condition, then write based on it." Snapshot isolation is not enough; use serializable isolation, a fitting constraint, or carefully designed locks.

## 12. Serializability: The Strong Isolation Target

Source pages: 332-333

- Serializable isolation guarantees the result is equivalent to some serial order of transactions.
- If each transaction is correct when run alone, serializable isolation prevents concurrency from breaking those assumptions.
- The chapter says serializability prevents all the race conditions discussed earlier.
- Main implementation strategies:
  - Actual serial execution.
  - Two-phase locking.
  - Optimistic techniques such as serializable snapshot isolation.

Interview hook: Serializable does not necessarily mean no physical concurrency; it means the committed result is equivalent to serial execution.

## 13. Actual Serial Execution

Source pages: 333-337

- Simplest way to avoid concurrency bugs: run one transaction at a time on a single thread.
- This became feasible because:
  - RAM became cheap enough for many active datasets to fit in memory.
  - OLTP transactions are usually short, while analytical queries are often read-only and can run on snapshots.
- Used in systems such as VoltDB/H-Store, Redis, and Datomic.
- Can avoid locking overhead and perform well, but throughput is limited by one CPU core unless sharding helps.

### Stored Procedures

Source pages: 334-336

- Interactive client/server transactions are slow for serial execution because the database waits for network round trips between statements.
- Single-threaded serial systems generally require one-statement transactions or stored procedures submitted to the database ahead of time.
- With in-memory data, a stored procedure can run quickly without waiting for network or disk I/O.
- Traditional stored procedures have problems:
  - Vendor-specific languages.
  - Harder debugging, versioning, deployment, testing, and metrics.
  - Bad database-side code can harm a shared database more severely than bad app-server code.
  - Untrusted tenant code is a security risk.
- Modern implementations may use general-purpose languages.
- Stored procedures can host validation logic when application architecture does not provide another good place.
- VoltDB uses deterministic stored procedures for replication: each replica runs the same procedure and must produce the same result.

### Sharding Serial Execution

Source pages: 336-337

- Serial execution on one core can bottleneck high write throughput.
- If data is sharded so each transaction touches only one shard, each shard can have its own serial transaction thread.
- Throughput can then scale with CPU cores.
- Cross-shard transactions require coordination across shards and are much slower.
- The source cites VoltDB reporting about 1,000 cross-shard writes per second, far below single-shard throughput and not improved simply by adding machines.
- Single-shard feasibility depends heavily on data model and access patterns; secondary indexes can force cross-shard coordination.

### Summary of Serial Execution

Source page: 337

- Works best when:
  - Transactions are small and fast.
  - The active dataset fits in memory.
  - Write throughput fits one CPU core or can be sharded without cross-shard coordination.
  - Cross-shard transactions are rare or acceptable despite poor scalability.

Interview hook: Serial execution trades concurrency-control complexity for strict workload constraints: fast transactions, memory-resident active data, and careful sharding.

## 14. Two-Phase Locking

Source pages: 337-341

- Two-phase locking (2PL), also called strong strict two-phase locking in this context, was the standard serializability algorithm for decades.
- 2PL is not 2PC:
  - 2PL provides serializable isolation.
  - 2PC provides atomic commit in distributed transactions.

### Core Idea

Source pages: 338-339

- 2PL strengthens lock rules beyond read committed.
- Readers can share a lock if no writer has exclusive access.
- Writers need exclusive access.
- If transaction A has read an object, transaction B must wait before writing it.
- If transaction A has written an object, transaction B must wait before reading it.
- Writers block readers and readers block writers, unlike snapshot isolation.
- 2PL protects against lost updates, write skew, and other race conditions.

### Implementation

Source pages: 338-339

- Each object has a shared or exclusive lock.
- Reads require shared locks.
- Writes require exclusive locks.
- A transaction can upgrade a shared lock to exclusive.
- Locks are held until commit or abort.
- "Two-phase" means:
  - Growing phase: acquire locks.
  - Shrinking phase: release locks at the end.
  - The phases must not overlap.
- Deadlocks are common enough that databases detect them and abort one transaction; the application retries.

### Performance

Source pages: 339-340

- 2PL can significantly reduce throughput and worsen latency.
- The main cost is reduced concurrency, not just lock bookkeeping.
- Long reads can block writes for a long time.
- One slow transaction or large transaction can make high-percentile latency very bad.
- Transaction timeouts and slow query monitoring help contain bad queries.
- Deadlocks occur more often than under weaker lock-based isolation and waste work because aborted transactions must retry.

### Predicate and Index-Range Locks

Source pages: 340-341

- Serializable isolation must prevent phantoms.
- Predicate locks conceptually lock all existing and future objects matching a search condition.
- Predicate locks are expensive because checking many active locks is costly.
- Databases usually approximate them with index-range locks, also called next-key locks.
- It is safe to lock a broader range than necessary, though it may reduce concurrency.
- If no suitable index exists, the database can fall back to locking the whole table, which is safe but bad for performance.

Interview hook: 2PL is serializable because it blocks conflicting reads and writes, including phantom-producing writes via predicate or index-range locks. Its downside is blocking and unstable latency.

## 15. Serializable Snapshot Isolation

Source pages: 341-347

- Serializable snapshot isolation (SSI) provides serializability with a small performance penalty compared with snapshot isolation in many workloads.
- SSI is used in systems named by the source, including PostgreSQL serializable isolation, SQL Server In-Memory OLTP/Hekaton, HyPer, CockroachDB, FoundationDB, and BadgerDB.

### Pessimistic Versus Optimistic

Source pages: 342-343

- 2PL is pessimistic: if danger is possible, wait.
- Serial execution is extremely pessimistic: effectively one transaction owns the database or shard for a short time.
- SSI is optimistic: let transactions proceed, then check at commit whether isolation was violated.
- If the execution was not serializable, abort and retry.
- Optimistic control performs badly under high contention because many transactions abort.
- With spare capacity and low contention, it can outperform pessimistic techniques.
- Commutative atomic operations can reduce contention.

### Outdated Premise

Source pages: 343

- Write skew follows a pattern:
  - Read data.
  - Make a decision based on what was read.
  - Write based on that decision.
- Under snapshot isolation, the read premise may become false before commit.
- The database must detect whether a transaction acted on a stale premise.

### Detection Mechanisms

Source pages: 343-346

- SSI detects two cases:
  - Reads of stale MVCC versions where an ignored write later commits.
  - Writes that affect data previously read by another transaction.
- For stale MVCC reads, the database tracks writes ignored because of snapshot visibility and checks at commit whether they committed.
- It waits until commit to avoid unnecessary aborts, especially for read-only transactions and writes that may still abort.
- For writes affecting prior reads, the database records which transactions read which index ranges or table-level data.
- Later writes check whether they affect recently read data.
- The tracking acts like a tripwire rather than a blocking lock.
- If conflict patterns make a transaction nonserializable, one transaction aborts.

### Performance

Source pages: 346-347

- SSI performance depends on engineering details such as read/write tracking granularity.
- Fine-grained tracking reduces unnecessary aborts but costs more bookkeeping.
- Coarser tracking is cheaper but may abort more transactions.
- SSI avoids lock waiting between readers and writers, giving more predictable latency than 2PL.
- Long-running read-only queries remain attractive because they can use snapshots without locks.
- SSI can scale beyond one CPU core and across machines; the source names FoundationDB as distributing conflict detection.
- SSI still has overhead compared with plain snapshot isolation.
- Abort rate matters. Long read/write transactions are more likely to conflict and abort. Long read-only transactions are okay.

Interview hook: SSI is optimistic serializability: snapshot reads plus conflict detection at commit. It avoids much blocking but can abort under contention.

## 16. Distributed Transactions and Atomic Commit

Source pages: 347-348

- A distributed transaction touches multiple nodes, such as multiple shards or a global secondary index stored separately from primary data.
- Distributed concurrency control resembles single-node techniques, but distributed atomicity is a separate challenge.
- On one node, atomic commit relies on a durable log: data first, then a commit record. The disk write of the commit record is the decisive point.
- In a distributed transaction, sending commit to all nodes independently is unsafe because some nodes may commit while others abort or crash.
- Once a node has committed, the write may become visible to other transactions and cannot simply be retracted.
- Atomic commitment means all participants commit or all abort.

Interview hook: Distributed atomic commit is not just "send COMMIT everywhere." You need a protocol that prevents mixed commit/abort outcomes.

## 17. Two-Phase Commit

Source pages: 348-352

- Two-phase commit (2PC) achieves atomic commit across multiple nodes.
- It is used internally in some databases and exposed through XA transactions or WS-AtomicTransaction.
- 2PC introduces a coordinator, also called transaction manager.
- Participants are the database nodes or systems involved in the transaction.

### Protocol Flow

Source pages: 349-351

- The application performs reads and writes on participants.
- Phase 1: coordinator sends prepare requests.
- Participants reply yes if they can definitely commit, or no if they cannot.
- Phase 2:
  - If all participants say yes, coordinator sends commit.
  - If any participant says no or prepare fails, coordinator sends abort.
- Details that make atomicity work:
  - A global transaction ID identifies the distributed transaction.
  - Participants perform local transaction work under that ID.
  - On prepare, a participant must write data durably and check conflicts/constraints.
  - By voting yes, a participant promises it can commit later and gives up unilateral abort.
  - The coordinator writes its final commit/abort decision to its log; that is the commit point.
  - After the decision is durable, the coordinator must retry commit/abort requests until participants receive them.
- Two points of no return:
  - Participant votes yes.
  - Coordinator durably decides commit or abort.

### Coordinator Failure

Source pages: 351-352

- If the coordinator fails before prepare, participants can abort.
- If a participant has voted yes and loses contact with the coordinator, it cannot safely commit or abort alone.
- The participant is in doubt or uncertain.
- 2PC can complete only after the coordinator recovers and reads its log.
- If the coordinator's log is lost, automatic recovery may be impossible and manual resolution is required.
- 2PC is a blocking atomic commit protocol.

### Three-Phase Commit

Source page: 352

- Three-phase commit was proposed to avoid blocking.
- It assumes bounded network delay and bounded node response time.
- In practical systems with unbounded delays and pauses, it cannot guarantee atomicity.
- The chapter points toward fault-tolerant consensus as a better practical solution.

Interview hook: 2PC's safety comes from durable promises, but its operational weakness is in-doubt participants waiting on the coordinator.

## 18. Distributed Transactions Across Different Systems

Source pages: 352-357

- Distributed transactions have a mixed reputation:
  - They provide safety that is hard to achieve otherwise.
  - They can cause operational problems, performance costs, and availability concerns.
- Performance cost often comes from extra fsyncs and network round trips.
- The chapter distinguishes two meanings:
  - Database-internal distributed transactions: nodes run the same database software.
  - Heterogeneous distributed transactions: participants are different technologies, such as databases and message brokers.
- Internal transactions can use optimized protocols. Heterogeneous transactions are harder because they must work across different systems.

### Exactly-Once Message Processing

Source pages: 353-354

- Heterogeneous transactions can atomically combine message acknowledgement with database writes.
- If the database transaction commits, the message is acknowledged.
- If either fails, both abort, and the broker can redeliver.
- This supports effectively exactly-once processing when all side effects participate in the same atomic commit protocol.
- If a side effect system, such as email, does not support 2PC, retries may duplicate that side effect.

### XA Transactions

Source pages: 354-355

- XA is a standard for 2PC across heterogeneous technologies.
- XA is a C API, not a network protocol.
- It is supported by many relational databases and message brokers.
- In Java systems, JTA and drivers such as JDBC/JMS can participate.
- The coordinator is often a library inside the application process.
- The coordinator tracks participants, asks them to prepare/commit/abort, and logs decisions on local disk.
- If the application process or machine dies, prepared participants remain in doubt until that coordinator recovers.

### Locks While In Doubt

Source pages: 355

- Prepared transactions must hold their locks until commit or abort.
- If the coordinator is down for 20 minutes, those locks may be held for 20 minutes.
- If the coordinator log is lost, locks may remain until manual resolution.
- This can make large parts of an application unavailable.

### Recovering From Coordinator Failure

Source pages: 355-356

- In theory, restart plus coordinator log recovery resolves in-doubt transactions.
- In practice, orphaned in-doubt transactions can occur if the coordinator cannot determine the outcome.
- Rebooting database servers does not solve it because correct 2PC must preserve locks across restarts.
- Administrators may need to inspect participants and manually commit or roll back.
- Heuristic decisions let a participant unilaterally resolve an in-doubt transaction, but this can break atomicity and is only for catastrophic recovery.

### Problems With XA

Source pages: 356-357

- The coordinator can be a single point of failure.
- Application-server local disks become critical durable state.
- Even a replicated coordinator would not solve the fact that participants and coordinator communicate through application code and drivers.
- XA is lowest-common-denominator:
  - Cannot detect deadlocks across heterogeneous systems.
  - Does not work with SSI-style distributed conflict detection.
- The chapter concludes that keeping heterogeneous systems consistent is important, but XA is not the ideal solution.

Interview hook: XA's hard problem is operational: the application process and coordinator log become part of the transaction's durable correctness boundary.

## 19. Database-Internal Distributed Transactions

Source page: 357

- Internal distributed transactions involve nodes of the same database system.
- The chapter names NewSQL systems such as CockroachDB, TiDB, Spanner, FoundationDB, and YugabyteDB, and also notes Kafka internal distributed transactions.
- Many use 2PC for multi-shard atomicity, but avoid XA's worst problems because they do not need heterogeneous compatibility.
- Improvements over XA include:
  - Replicated coordinator with automatic failover.
  - Direct communication between coordinator and shards.
  - Replicated participant shards to reduce abort risk from faults.
  - Coupling atomic commit with distributed concurrency control.
- Consensus commonly replicates coordinators and shards.
- Snapshot isolation and serializable snapshot isolation can be provided across shards.

Interview hook: Do not judge internal database transactions by XA alone. Same-system distributed transactions can use tighter protocols and consensus-backed fault tolerance.

## 20. Exactly-Once Message Processing Revisited

Source pages: 358-359

- Exactly-once message processing does not necessarily require distributed transactions between broker and database.
- Alternative using only database transactions:
  - Each message has a unique ID.
  - Database stores IDs currently or already processed.
  - On processing, transaction checks whether the ID exists.
  - If absent, insert ID and perform database side effects in the same transaction.
  - Commit the database transaction.
  - Then acknowledge the message to the broker.
  - Later delete the message ID in a separate transaction.
- Crash cases:
  - Crash before database commit: database aborts, broker retries.
  - Crash after commit but before acknowledgement: broker retries, database ID causes duplicate to be dropped.
  - Crash after acknowledgement but before deleting ID: old ID remains but only costs storage.
  - Concurrent retry before abort: uniqueness constraint on message IDs prevents duplicate insertion.
- The key is idempotence: recording the message ID makes retries safe.
- Internal distributed database transactions are still useful if message IDs and affected data live on different shards.

Interview hook: Exactly-once often means "at-least-once delivery plus idempotent processing and transactional deduplication," not necessarily XA.

## 21. Chapter Summary: Anomaly Map

Source pages: 359-361

- Transactions hide many failure and concurrency cases behind abort and retry.
- Simple single-record applications may manage without full transactions.
- Complex access patterns benefit because transactions reduce error cases.
- Without transactions, crashes, network failures, power loss, disk-full errors, and concurrency can leave data inconsistent.
- Isolation anomalies:
  - Dirty read: read uncommitted data.
  - Dirty write: overwrite uncommitted data.
  - Read skew: observe different parts of database at different times.
  - Phantom read: another write changes the result of a search query.
  - Lost update: read-modify-write cycles overwrite each other.
  - Write skew: a transaction writes based on a premise that becomes false before commit.
- Isolation prevention summary from the chapter:
  - Read uncommitted allows dirty reads, read skew, phantom reads, lost updates, and write skew.
  - Read committed prevents dirty reads but allows read skew, phantom reads, lost updates, and write skew.
  - Snapshot isolation prevents dirty reads, read skew, and straightforward phantom reads; lost update prevention depends on implementation; write skew remains possible.
  - Serializable prevents all listed anomalies.
- Serializable implementation options:
  - Serial execution: simple if transactions are fast and throughput constraints fit.
  - 2PL: standard but often slow due to blocking.
  - SSI: optimistic, avoids most blocking, aborts nonserializable executions.
- Distributed atomicity:
  - 2PC gives atomic commit across nodes.
  - Same-database distributed transactions can work well.
  - XA across heterogeneous systems is operationally fragile.
  - Idempotence can often give exactly-once processing without cross-system atomic commit.

## Chapter-Level Memory Hooks

- Transaction = bundle reads/writes so partial failure and many concurrency problems become commit or abort.
- Atomicity in ACID means abortability, not thread-style indivisibility.
- Consistency is about invariants; the database only enforces what it knows.
- Isolation names lie unless you know the anomalies prevented.
- Durability is risk reduction, not a magical guarantee.
- Single-object atomic operations are useful but not full multi-object transactions.
- Read committed prevents dirty reads and dirty writes only.
- Snapshot isolation gives a point-in-time read view using MVCC.
- MVCC slogan: readers do not block writers, writers do not block readers.
- Lost update = two read-modify-write cycles collide.
- Write skew = read a condition, write a decision, premise becomes false.
- Phantoms are search-result changes caused by concurrent writes.
- Serializable means equivalent to some serial order, not necessarily physically serial.
- Serial execution is simple but constrained by fast transactions and sharding.
- 2PL is safe but blocking.
- SSI is optimistic: proceed, then abort if commit would violate serializability.
- 2PL is isolation; 2PC is atomic commit.
- 2PC works through durable promises and a coordinator decision log.
- XA makes app/coordinator failure part of data correctness.
- Idempotent message processing can avoid cross-system distributed transactions.

## Interview Perspective

- Start with why transactions exist: they simplify failure handling and concurrency reasoning.
- Define ACID precisely and call out ambiguity around "ACID compliant."
- Explain that consistency is not fully provided by the database unless invariants are declared or encoded.
- Distinguish single-object atomic operations from multi-object transactions.
- For isolation questions, answer by anomalies:
  - Dirty read and dirty write for read committed.
  - Read skew and MVCC for snapshot isolation.
  - Lost update and its remedies.
  - Write skew and phantoms as the reason snapshot isolation is not serializable.
- Mention that database isolation labels differ across vendors.
- Compare serializability implementations:
  - Serial execution: no concurrency, fast stored procedures, sharding constraints.
  - 2PL: shared/exclusive locks, predicate/index-range locks, blocking costs.
  - SSI: snapshot reads, conflict tracking, commit-time aborts.
- For distributed transactions, distinguish:
  - Distributed isolation/concurrency control.
  - Distributed atomic commit.
- Explain 2PC by the promise model: participants promise after prepare; coordinator makes a durable final decision.
- Call out the operational failure mode: in-doubt transactions hold locks.
- Separate XA from internal distributed transactions; XA is constrained by heterogeneous compatibility and application-process coordination.
- For exactly-once processing, emphasize transactional deduplication and idempotence.

## Final Takeaways

- Transactions are a correctness abstraction: they reduce partial failures and race conditions to a smaller set of cases the application can handle.
- Weak isolation levels are practical but dangerous unless you know exactly which anomalies remain possible.
- Snapshot isolation is excellent for consistent reads and read-heavy workloads, but it does not automatically prevent write skew.
- Serializable isolation is the only level in this chapter that prevents the full set of discussed race conditions.
- There are three major serializability families: serial execution, 2PL, and SSI, each with different performance and operational tradeoffs.
- Distributed atomic commit is fundamentally harder than single-node commit because participants must not split between commit and abort.
- 2PC's correctness depends on durable coordinator decisions and participant promises; its weakness is blocking when the coordinator fails.
- Internal distributed transactions can work well when one database controls the protocol; XA across heterogeneous systems is much more fragile.
- Exactly-once processing can often be achieved by making message handling idempotent with a database transaction and unique message IDs.

Confidence: High
