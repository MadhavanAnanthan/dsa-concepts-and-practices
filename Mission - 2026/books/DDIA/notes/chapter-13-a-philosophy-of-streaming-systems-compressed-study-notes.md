# DDIA Chapter 13: A Philosophy of Streaming Systems - Compressed Study Notes

Book: DDIA  
Chapter: 13 - A Philosophy of Streaming Systems  
Source PDF: books/DDIA/DDIA.pdf  
Source PDF/pages: PDF pages 563-604; printed pages 539-579  
Method: Compressed section-by-section from `books/DDIA/raw/chapter-13-chapter-13-a-philosophy-of-streaming-systems.md` only. Long passages are paraphrased; important concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks are preserved. No external knowledge used.

## Source Map

| Section | Source pages |
| --- | --- |
| Chapter framing and Data Integration | PDF 563-568; pp. 539-544 |
| Batch and Stream Processing | PDF 568-570; pp. 544-546 |
| Unbundling Databases | PDF 570-575; pp. 546-551 |
| Designing Applications Around Dataflow | PDF 575-579; pp. 551-555 |
| Observing Derived State | PDF 579-585; pp. 555-561 |
| Aiming for Correctness | PDF 585-599; pp. 561-575 |
| Trust, but Verify | PDF 599-603; pp. 575-578 |
| Chapter Summary | PDF 603-604; p. 579 |

## 1. Chapter Framing: Why a Philosophy of Streaming Systems?

Source: PDF 563-564; pp. 539-540

- The chapter ties together the book's recurring goals: reliability, scalability, and maintainability.
- Unlike earlier chapters, this one is explicitly opinionated. It develops one philosophy of application development built around streams, derived data, and dataflow.
- The central premise is that no one data system is ideal for every access pattern, workload, or organizational need.
- Since complex applications need multiple tools, the hard problem becomes integration: how to keep data correct while it flows through databases, indexes, caches, analytics, models, and notification systems.

Interview hook: Chapter 13 is not mainly about a new tool. It is about treating a whole application architecture as a set of reliable data derivations.

## 2. Data Integration

Source: PDF 563-568; pp. 539-544

### Specialized Tools and Derived Data

- Different storage engines and replication strategies fit different circumstances. The same applies to databases, search systems, analytics systems, caches, and recommendation systems.
- A common example is combining an OLTP database with a full-text search index. The OLTP database is suitable as a durable system of record; the search index is optimized for keyword retrieval.
- As more copies and representations are added, integration becomes harder. Examples include data warehouses, stream processors, denormalized objects, caches, ML models, ranking systems, and notification systems.
- The chapter's answer is to make clear which data is original and which data is derived.

Terminology: A system of record is where new authoritative input is written first. A derived data system is maintained from another source, such as a database change stream or event log.

Interview hook: When discussing multi-system architectures, always ask which system decides the write order and which systems are derived from it.

### Reasoning About Dataflows

Source: PDF 565; p. 541

- When several systems store related data, the architecture must define inputs, outputs, and derivation paths.
- A safer pattern is: write first to the system of record, capture its changes, and apply those changes to derived systems in the same order.
- CDC can keep a search index consistent with the source database if CDC is the only route by which the index is updated.
- Dual writes are dangerous. If the application writes directly to both the database and the search index, concurrent clients may be processed in different orders by different systems, causing permanent inconsistency.
- Funneling all input through one ordering authority makes the architecture resemble state machine replication: all derived representations process the same ordered facts.
- Whether the ordered stream comes from CDC or event sourcing is less important than the principle that write ordering is explicit and shared.

Edge case: Direct writes to multiple systems may appear safe in low concurrency tests, but the order can diverge when concurrent conflicting writes arrive.

Interview hook: "Never dual-write blindly" is one of the most practical lessons of the chapter.

### Derived Data Versus Distributed Transactions

Source: PDF 565-566; pp. 541-542

- Distributed transactions and log-derived systems both try to keep systems consistent, but they use different mechanisms.
- Distributed transactions use atomic commit. Log-based derived systems use deterministic retry and idempotence.
- Transactions often provide immediate read-your-writes behavior. Asynchronous derived systems usually do not.
- Distributed transactions can work where performance and operational costs are acceptable, but the chapter criticizes XA for poor fault tolerance and performance across heterogeneous systems.
- Since broadly adopted, robust distributed transactions across many tools are unlikely soon, log-based derived data is presented as the most promising integration approach.
- The chapter does not dismiss stronger guarantees. It later explores how to build stronger behavior on top of asynchronous derivation.

Tradeoff: Distributed transactions optimize synchronous certainty; logs optimize loose coupling, replayability, and operational robustness.

Interview hook: The comparison is not "transactions correct, logs incorrect." It is "atomic commit versus deterministic, idempotent replay."

### Limits of Total Ordering

Source: PDF 566-567; pp. 542-543

- A totally ordered log is feasible at modest scale and is common in single-leader replicated databases.
- Total ordering becomes harder when throughput exceeds one machine, when systems span datacenters, when microservices own separate durable state, or when clients maintain offline local state.
- Sharding a log gives order within each shard, but not across shards.
- Separate datacenter leaders avoid cross-region latency but leave cross-region event order undefined.
- Microservices often create independent logs and state, so events from different services have no global order.
- Offline or optimistic clients can see and create events in different orders from the server.
- Total order broadcast is equivalent to consensus, and common consensus algorithms do not let many nodes share one global ordering workload without limits.

Limitation: Total order is a powerful simplification, but it is also a scalability and availability bottleneck.

Interview hook: Total order is consensus. Once you say "global order," you have introduced a coordination problem.

### Ordering Events to Capture Causality

Source: PDF 567-568; pp. 543-544

- If events have no causal link, arbitrary ordering is often acceptable.
- Some ordering can be achieved by routing all updates for the same object ID to the same shard.
- Subtle causal dependencies can span objects and services. The chapter's social example: a user unfriends someone, then posts a message intended only for remaining friends. If the message event is processed before the unfriend event by a notification service, the ex-friend may incorrectly receive the message.
- This problem resembles a time-dependent join between messages and friend lists.
- Possible starting points include logical timestamps, recording the state a user observed before making a decision, and conflict resolution algorithms.
- Conflict resolution can help maintain state under unexpected ordering, but it does not fix external side effects such as sending a notification.

Edge case: Causal bugs often appear at service boundaries, not within a single object update path.

Interview hook: Ask whether the order being preserved is merely per-key order or true causal order across entities.

## 3. Batch and Stream Processing

Source: PDF 568-570; pp. 544-546

### Maintaining Derived State

- Data integration means consuming inputs, transforming, joining, filtering, aggregating, training, evaluating, and writing outputs.
- Batch and stream processors create derived datasets such as search indexes, materialized views, recommendations, and aggregate metrics.
- Batch and stream processing share many principles. The key difference is bounded input versus unbounded input.
- Batch processing encourages deterministic, side-effect-limited transformations over immutable inputs and append-only outputs.
- Stream processing extends that model with managed, fault-tolerant state.
- Deterministic functions with well-defined inputs and outputs improve both fault tolerance and organizational reasoning.
- Derived systems could be maintained synchronously, but asynchronous log-based maintenance contains faults better. A failure in one consumer can be localized instead of causing a distributed transaction to abort everywhere.
- Secondary indexes that cross shard boundaries are also more reliable and scalable when maintained asynchronously.

Tradeoff: Asynchrony weakens freshness by default, but it improves fault isolation and scalability.

Interview hook: The streaming philosophy imports functional dataflow ideas into operational data systems.

### Reprocessing Data for Application Evolution

Source: PDF 569-570; pp. 545-546

- Stream processing keeps derived views fresh with low delay.
- Batch reprocessing allows historical data to be transformed again to produce new views, schemas, or models.
- Without reprocessing, schema evolution is limited to simple compatible changes. With reprocessing, a dataset can be reshaped into a different model.
- The railway gauge example illustrates gradual migration: old and new forms coexist until the new form is proven and the old form can be removed.
- Derived views allow the same gradual migration pattern in software. Old and new schemas can be maintained side by side from the same underlying data.
- A small fraction of users can be shifted to the new view, then more users, with rollback available at every stage.

Memory hook: Reprocessing is the "third rail" of data migration: it lets old and new tracks run side by side.

Interview hook: Reprocessing converts risky one-shot migrations into reversible, staged application evolution.

### Unifying Batch and Stream Processing

Source: PDF 570; p. 546

- Lambda architecture was an early attempt to combine batch and stream processing, but the chapter notes it had problems and fell out of use.
- More recent systems run both historical reprocessing and live event processing in the same processing model, sometimes called kappa architecture.
- Requirements for unification:
  - replay historical events through the same engine as recent events;
  - exactly-once semantics, meaning output as if no fault occurred;
  - event-time windowing, because processing time is meaningless during historical replay.

Interview hook: Unified batch/stream is about one computation over both old and new events, not two divergent code paths.

## 4. Unbundling Databases

Source: PDF 570-575; pp. 546-551

### Databases, Operating Systems, and Processing Systems

- Databases, batch/stream processors, and operating systems all store data and let users process or query it.
- Unix emphasizes low-level composable abstractions such as files and pipes.
- Relational databases emphasize high-level declarative abstractions such as SQL, transactions, indexes, query planning, concurrency control, and recovery.
- The chapter presents unbundling as an attempt to combine the best of both philosophies.

Interview hook: Unix composes simple tools; databases integrate powerful features. Unbundling asks whether data systems can do both.

### Composing Data Storage Technologies

Source: PDF 571-572; pp. 547-548

- Database features such as secondary indexes, materialized views, replication logs, and full-text indexes resemble derived systems built with batch and stream processors.
- Creating an index with `CREATE INDEX` scans a consistent table snapshot, extracts indexed values, sorts/writes the index, catches up with writes since the snapshot, and then maintains the index continuously.
- This process resembles setting up a follower replica and bootstrapping CDC.
- At organization scale, ETL, batch, and stream processes can be viewed as the "meta-database" mechanisms that maintain organization-wide indexes and materialized views.

Interview hook: `CREATE INDEX` is a mini data pipeline: snapshot, transform, catch up, maintain.

### Federated Databases Versus Unbundled Databases

Source: PDF 572-573; pp. 548-549

- Federated databases unify reads by providing a query interface over multiple storage systems.
- Federation follows the relational tradition: elegant high-level query semantics over a complicated implementation.
- Unbundled databases unify writes by using CDC and event logs to keep derived systems synchronized.
- Unbundling follows the Unix tradition: specialized tools communicate through a uniform lower-level API and are composed externally.
- Federation's read mapping problem is manageable. Synchronizing writes across heterogeneous systems is harder.

Tradeoff: Federation helps ask questions across systems; unbundling helps propagate changes across systems.

Interview hook: Federated reads solve "where can I query?" Unbundled writes solve "how do all derived copies stay correct?"

### Making Unbundling Work

Source: PDF 573-574; pp. 549-550

- Distributed transactions across heterogeneous storage systems are problematic.
- Transactions inside one storage or stream system can work well, but transactions spanning products and teams are much harder.
- An ordered event log with idempotent consumers is a simpler, more general integration abstraction.
- Log-based integration gives loose coupling:
  - System level: slow or failed consumers do not block producers or other consumers; the log buffers work until recovery.
  - Human level: teams can own specialized systems with durable, ordered event interfaces between them.

Interview hook: Logs are both technical decoupling and organizational decoupling.

### Unbundled Versus Integrated Systems

Source: PDF 574-575; pp. 550-551

- Unbundling does not replace databases. Databases remain necessary for stream processor state, query serving, and specialized workloads.
- Running many infrastructure pieces has real operational cost: learning curves, configuration, quirks, and unpredictable composed performance.
- Integrated products may be faster and more predictable for workloads they are designed to handle.
- Building for scale you do not need is wasted effort and can create inflexible design.
- Use a single technology if it satisfies the requirements. Unbundling helps when no single product covers the workload breadth.
- The chapter notes improving tools such as change-stream extraction, event-stream protocols, and incremental view maintenance engines.

Limitation: Unbundling is an architectural technique, not a default recommendation for every application.

Interview hook: Do not sell unbundling as "always better." Sell it as a way to compose multiple specialized stores when one store is insufficient.

## 5. Designing Applications Around Dataflow

Source: PDF 575-579; pp. 551-555

### Spreadsheet Mental Model

- Spreadsheets show the desired dataflow behavior: when an input cell changes, dependent formulas recalculate automatically.
- Data systems should similarly update indexes, cached views, and aggregations when underlying records change.
- Modern data systems need the spreadsheet idea plus durability, fault tolerance, scalability, heterogeneous tooling, and long-term evolution.

Memory hook: Spreadsheet recalculation is the intuitive model; event logs and stream processors are the distributed, durable version.

### Application Code as a Derivation Function

Source: PDF 575-576; pp. 551-552

- Every derived dataset is produced by a transformation function.
- Examples:
  - Secondary index: extract indexed fields and arrange them for lookup.
  - Full-text index: language detection, segmentation, stemming or lemmatization, spelling correction, synonyms, then inverted lookup structures.
  - ML model: feature extraction and statistical analysis over training data; predictions derive from input plus learned parameters.
  - UI cache: aggregate and shape data as the UI needs it.
- Standard derivations such as secondary indexes are built into databases. Application-specific derivations require custom code.
- Databases support triggers, stored procedures, and user-defined functions, but the chapter treats them as secondary to database design rather than ideal application deployment environments.

Interview hook: Application code is often the missing custom derivation function between source data and optimized read models.

### Separating Application Code and State

Source: PDF 576-577; pp. 552-553

- Databases could theoretically run arbitrary application code, but in practice they are poor fits for modern deployment needs: dependencies, package management, version control, rolling upgrades, monitoring, metrics, network calls, and external integration.
- Cluster/application platforms are designed for running code; databases are designed for state.
- Common web architectures use stateless services and place persistent state in databases.
- In that model, the database behaves like a mutable shared variable accessed over the network.
- The weakness is that most databases are passive: clients read or poll rather than subscribe to changes.

Tradeoff: Stateless application servers are easy to scale, but they make state change notification an extra architectural concern.

### Dataflow as Interplay Between State and Code

Source: PDF 577-579; pp. 553-555

- Dataflow thinking changes the relationship between code and state. Instead of treating databases as passive variables, application code reacts to state changes and triggers other state changes.
- This idea appears in CDC, actor systems, triggers, and incremental view maintenance.
- Unbundling applies the idea outside the primary database, to caches, search indexes, ML systems, and analytics stores.
- Maintaining derived data requires stable ordering and fault-tolerant delivery because a single lost message can permanently desynchronize the derived dataset.
- Modern stream processors can run application code as stream operators while providing ordering and reliability at scale.
- Stream operators resemble Unix tools connected by pipes: each consumes state-change streams and emits state-change streams.

Interview hook: In dataflow architecture, services are not just called; they subscribe, transform, and emit.

### Stream Processors and Services

Source: PDF 578-579; pp. 554-555

- Service-oriented architectures commonly use synchronous REST/RPC calls. Their main benefit is organizational loose coupling.
- Stream dataflow has similar organizational decomposition but uses one-way asynchronous message streams.
- Example: purchase processing needs a currency exchange rate.
  - Microservice approach: synchronously query an exchange rate service or database.
  - Dataflow approach: subscribe to exchange rate updates in advance and keep a local copy for purchase processing.
- The dataflow version can avoid a network request on the critical path and is more robust to another service's temporary failure.
- This is a stream join between purchase events and exchange-rate update events.
- The join is time-dependent: reprocessing the purchase later requires the historical exchange rate at purchase time.

Interview hook: The fastest and most reliable remote call is the one moved off the request path by subscribing to state changes.

## 6. Observing Derived State

Source: PDF 579-585; pp. 555-561

### Write Path and Read Path

Source: PDF 579-581; pp. 555-557

- The write path is the journey of new information through batch/stream processing into derived datasets.
- The read path is the work done when a user asks for data.
- A derived dataset is where the write path and read path meet.
- This boundary is a tradeoff: more precomputation on the write path can reduce read-time work; less precomputation makes writes cheaper but reads more expensive.
- Search index example:
  - With an index, writes update terms for each document; reads search indexed terms.
  - Without an index, writes are cheaper but reads must scan documents.
  - Precomputing all query results would make reads cheap but is infeasible because possible queries are enormous.
  - Caching common query results is a middle ground and can be viewed as a materialized view.
- The social network home-timeline example also shifts the boundary differently for ordinary users versus celebrities.

Interview hook: Caches, indexes, and materialized views all move work from read time to write time.

### Stateful, Offline-Capable Clients

Source: PDF 581-583; pp. 557-559

- Web and mobile clients increasingly maintain local state and can operate offline.
- Device-local state can be seen as a cache or replica of server-side state.
- The UI pixels are a materialized view of client model objects, which are a local replica of remote state.
- Traditional web pages read data once and become stale until reloaded or polled.
- Server-sent events and WebSockets allow servers to push changes to connected browsers.
- Pushing state changes to clients extends the write path all the way to end-user devices.
- Initial client setup still needs a read path, but ongoing updates can arrive as a stream.
- Offline devices resemble disconnected log consumers. On reconnection, they need offsets or equivalent tracking so they can catch up without missing events.

Edge case: Client devices are intermittent subscribers. The architecture must handle disconnection as normal, not exceptional.

Interview hook: Local-first/offline-capable clients are derived-state replicas at the edge.

### End-to-End Event Streams

Source: PDF 583; p. 559

- UI frameworks already update rendered output when local state changes.
- The chapter suggests extending this model so server-pushed state-change events enter the same client-side pipeline.
- A state change could flow from one user's device, through event logs and derived systems, to another user's UI with low delay.
- Instant messaging and online games already use this kind of low-delay architecture.
- The obstacle is that request/response assumptions are deeply embedded in databases, frameworks, libraries, and protocols.
- To extend the write path to end users, systems need more native publish/subscribe behavior.

Limitation: Many tools support one response to one request more naturally than a stream of future responses.

### Reads Are Events Too

Source: PDF 583-584; pp. 559-560

- Usually, writes go through event logs and reads go directly to storage nodes.
- Another model is to represent read requests as events and route both reads and writes through a stream processor.
- The processor responds to read events by emitting results on an output stream.
- This is equivalent to a stream-table join between read queries and database state.
- A one-off read is a join that is immediately forgotten. A subscription is a persistent join with past and future events.
- Logging reads can help track causal dependencies and provenance. Example: an online shop may need to know what shipping date and inventory status a customer saw before deciding to buy.
- Cost: durable read logs add storage and I/O overhead; optimization remains an open research problem in the chapter.

Interview hook: A subscription is not just a long read. It is a durable/persistent relationship between a query and future changes.

### Multishard Data Processing

Source: PDF 584-585; pp. 560-561

- Sending simple single-shard reads through streams may be overkill.
- The idea becomes more useful for complex queries that touch many shards and need routing, sharding, and joining.
- Examples:
  - Computing how many people saw a URL by unioning follower sets across shards.
  - Fraud prevention by joining a purchase event with reputation scores for IP address, email, billing address, shipping address, and other sharded datasets.
- Data warehouse query engines already use similar internal execution graphs.
- If a conventional database can provide this feature, it is probably simpler to use it. Stream-based request processing is an option when conventional tools hit limits.

Interview hook: Stream processors can express multishard joins, but do not replace query engines unless the workload demands it.

## 7. Aiming for Correctness

Source: PDF 585-599; pp. 561-575

### Why Correctness Is Hard

Source: PDF 585-586; pp. 561-562

- Stateless read-only services can often be restarted after bugs. Stateful systems remember effects, so bugs can persist indefinitely.
- Transactions have long been the standard correctness tool, but weak isolation levels and complex configurations make safety hard to reason about.
- Some systems trade away strong transaction semantics for performance and scalability, but the resulting "consistency" claims are often unclear.
- Correctness problems often hide under low concurrency and fault-free conditions, then appear under crashes, network problems, or high concurrency.
- Serializability and atomic commit are useful but costly, especially in geographically distributed or highly scalable designs.

Interview hook: Correctness is about behavior under concurrency and faults, not only passing normal-path tests.

### The End-to-End Argument for Databases

Source: PDF 586-590; pp. 562-566

- Strong database safety properties do not guarantee the application is safe. Application bugs can still write bad data or delete good data.
- Immutable append-only data helps recover from mistakes, but it is not enough alone.
- Exactly-once semantics means the final effect is as if the operation happened once, even if retries occurred after faults.
- Idempotence is a practical way to achieve this, but non-idempotent operations need extra metadata and care.
- Money transfer example:
  - A client sends a transaction that credits one account and debits another.
  - The connection fails after `COMMIT` is sent but before the client receives the result.
  - Retrying the transaction may transfer money twice.
- TCP duplicate suppression works only within one TCP connection. It does not suppress duplicate user requests across reconnects, HTTP retries, or manual resubmission.
- To make a request idempotent across hops, generate a unique request ID at the client and pass it through to the database.
- A database uniqueness constraint on request ID can reject duplicates reliably; an application-level check-then-insert may fail under weak isolation.
- The request table also acts like an event log; downstream derived updates can be based on request events.
- The end-to-end argument says some functions can only be implemented completely with application endpoint knowledge. Lower-level mechanisms can help, but cannot solve the whole problem.
- The same applies to integrity checks and encryption: packet checksums and transport security help, but only end-to-end checking/protection covers the full path.

Interview hook: "Exactly once" at a broker or TCP level is not enough. The business request needs an end-to-end identity.

### Applying End-to-End Thinking

Source: PDF 590; p. 566

- Applications need their own end-to-end correctness measures, such as duplicate suppression.
- Transactions simplify many possible errors into commit or abort, but they do not cover every end-to-end failure mode.
- Avoiding distributed transactions often pushes developers into application-level fault tolerance, which is difficult and frequently wrong.
- The chapter argues for better abstractions that support application-specific correctness without sacrificing distributed operational properties.

Interview hook: The hard part is not knowing that retries happen; it is making every retry path preserve the intended business effect.

### Enforcing Constraints

Source: PDF 590-594; pp. 566-570

- Uniqueness constraints include request IDs, usernames, email addresses, file names, seats, and similar "only one winner" rules.
- Related constraints include nonnegative account balances, not selling more inventory than exists, and avoiding overlapping room bookings.
- In distributed systems, strict uniqueness requires consensus: concurrent conflicting requests must be ordered so one can be accepted and the others rejected.
- The common approach is a leader that decides, with a consensus algorithm to handle leader failure and avoid split brain.
- Uniqueness can scale by sharding on the unique value, such as request ID or username.
- Asynchronous multi-leader replication cannot immediately enforce strict uniqueness because separate leaders may accept conflicting writes.
- Log-based messaging can enforce uniqueness when all requests for the same unique value go to the same log shard.
- A single-threaded stream processor consumes that shard sequentially, maintains local taken-state, and emits success or rejection.
- This is the same basic construction as consensus with a shared log, scaled by independent shards.

Interview hook: Strict uniqueness is coordination. Sharding can reduce the scope of coordination, but cannot remove it for conflicting values.

### Multishard Request Processing

Source: PDF 592-594; pp. 568-570

- Multishard operations are harder because one business operation may touch request ID, payer account, payee account, and fee account shards.
- A traditional database transaction would need atomic commit across shards, forcing coordination and likely reducing throughput.
- The chapter's payment example achieves equivalent correctness with sharded logs and deterministic processors:
  - Client assigns a unique request ID and appends the transfer request to the source-account shard.
  - Source-account processor checks available funds in local derived state and records/reserves the payment if allowed.
  - It emits outgoing and incoming payment events to the relevant account shards, carrying the same request ID.
  - Destination and fee processors update their local state and deduplicate by request ID.
  - If the source processor crashes, at-least-once retry plus determinism re-emits the same events; downstream deduplication ignores duplicates.
- Atomicity comes from the initial request event being written atomically to the source log. Once it exists, downstream consequences eventually occur.
- Exactly-once stream processing simplifies implementation by keeping processor state aligned with processed messages.
- A client can wait for an output event, such as outgoing payment or declined payment, to learn the result.

Interview hook: The design replaces one distributed transaction with a chain of deterministic, idempotent event derivations.

### Timeliness and Integrity

Source: PDF 595-597; pp. 571-573

- Transactional systems often make committed writes immediately visible. Unbundled stream stages are asynchronous by default.
- A client may wait for a relevant output event, but waiting only affects when the user learns the result. It does not create the correctness of the underlying state transition.
- The chapter separates two meanings often collapsed into "consistency":
  - Timeliness: users observe up-to-date state.
  - Integrity: data is not lost, corrupted, contradictory, or falsely derived.
- Stale reads are timeliness violations. They can often be fixed by waiting and retrying.
- Corrupt indexes, missing records, double charges, or mismatched credits/debits are integrity violations. They require explicit checking and repair.
- Eventual consistency permits temporary timeliness violations; integrity violations create lasting inconsistency.
- Most applications care more about integrity than timeliness.
- Dataflow systems decouple timeliness and integrity. They may not be fresh unless clients wait, but they can preserve integrity through reliable delivery, duplicate suppression, deterministic derivation, immutable messages, request IDs, and reprocessing.

Memory hook: Timeliness is "am I seeing the latest?" Integrity is "is the system telling the truth?"

Interview hook: Event-driven systems are not automatically weak. They may weaken timeliness while preserving strong integrity.

### Loosely Interpreted Constraints and Compensation

Source: PDF 597-598; pp. 573-574

- Some business constraints can be temporarily violated and repaired later.
- Examples:
  - If inventory is oversold, order more stock, apologize, or offer a discount.
  - Airlines and hotels may overbook intentionally and compensate affected customers.
  - Banks may allow overdrafts with fees and risk limits.
  - Cross-organization settlement inevitably needs correction mechanisms.
- A compensating transaction corrects a mistake after the fact.
- Whether apology/compensation is acceptable is a business decision.
- The chapter distinguishes integrity from synchronous constraint enforcement: a system may preserve records and money flows correctly while checking some constraints after the write.

Interview hook: Not every business rule needs pre-write coordination. Some need durable facts plus repair workflows.

### Coordination-Avoiding Data Systems

Source: PDF 598-599; pp. 574-575

- Dataflow systems can maintain derived-data integrity without atomic commit, linearizability, or synchronous cross-shard coordination.
- Many applications can tolerate loose constraints if integrity is preserved and violations can be repaired.
- Together, these observations make coordination-avoiding data systems attractive.
- Such systems can run across multiple datacenters with asynchronous replication and no synchronous cross-region coordination.
- They will not be linearizable without coordination, but they can still preserve strong integrity.
- Serializable transactions remain useful at small scope, where they work well. Coordination can be introduced only where recovery is impossible or strict constraints are truly required.
- Coordination reduces apology for inconsistency but can increase apology for outages; the design goal is the business-appropriate tradeoff.

Interview hook: Coordination is a cost center. Spend it where the business cannot tolerate later correction.

## 8. Trust, But Verify

Source: PDF 599-603; pp. 575-578

### System Models and Rare Faults

- Correctness discussions assume a system model: which faults can happen and which cannot.
- Typical assumptions include process crashes, power loss, dropped or delayed messages, durable disks after fsync, correct memory, and correct CPU operations.
- In reality, faults are probabilistic. Rare corruption can still happen at large scale.
- Data can be corrupted in memory, on disk, on networks, and by software bugs.

Interview hook: A system model is not reality; it is a set of assumptions that must be checked against scale and risk.

### Maintaining Integrity Despite Software Bugs

Source: PDF 600; p. 576

- Lower-level checksums do not catch application or database logic bugs.
- Even mature database systems have had bugs in uniqueness or isolation behavior.
- Application code is typically less reviewed and less tested than database code, and may misuse integrity features such as foreign keys or uniqueness constraints.
- ACID consistency assumes transactions correctly preserve invariants. If transaction code is buggy or isolation is used unsafely, integrity is not guaranteed.

Interview hook: ACID does not save an application from incorrect transaction logic.

### Auditing and Self-Validation

Source: PDF 600-601; pp. 576-577

- Since hardware and software can fail, systems need ways to detect corruption.
- Auditing means checking data integrity so problems can be fixed and root causes investigated.
- Auditability matters beyond finance, but finance highlights the need because mistakes are expected and must be corrected.
- Large storage systems do not blindly trust disks; they continuously read, compare replicas, and move data to mitigate silent corruption.
- Backups should be restored periodically to verify they actually work.
- The chapter predicts more self-validating systems that continually audit integrity rather than assume correctness guarantees are absolute.

Memory hook: If you want to know your data or backup is still good, read it and verify it.

### Designing for Auditability

Source: PDF 601-602; pp. 577-578

- Mutable transactions can obscure why several objects changed.
- Event-based systems are more auditable because user input is represented as immutable events and derived state follows deterministically.
- Data provenance becomes clearer when dataflow is explicit.
- Event logs can be checked with hashes; derived state can be checked by rerunning processors or running redundant derivations in parallel.
- Deterministic dataflow also improves debugging because the system can reproduce the circumstances that led to an outcome.

Interview hook: Event sourcing is not just about replay. It is also about explaining why state exists.

### End-to-End Integrity Checks and Audit Tools

Source: PDF 602-603; pp. 578-579

- Integrity checking should be end to end. The more of the pipeline included in the check, the fewer places corruption can hide.
- Continuous end-to-end integrity checks increase confidence and make change less risky, similar to automated testing.
- Current systems often treat auditability as application-specific rather than a first-class data-system concern.
- Blockchains are shared append-only logs with cryptographic consistency checks; transactions are events and smart contracts resemble stream processors.
- Their overhead is too high for most applications, but some cryptographic tools are useful more broadly.
- Merkle trees can efficiently prove that a record appears in a dataset.
- Certificate transparency uses verified append-only logs and Merkle trees for TLS/SSL certificate validity, with a single leader per log rather than a full blockchain consensus protocol.
- Integrity-checking and auditing algorithms may become more common if made scalable with low performance cost.

Interview hook: Borrow the auditability ideas where useful; do not assume blockchain overhead is justified for ordinary applications.

## Chapter-Level Memory Hooks

- One tool rarely fits every access pattern; integration becomes the core architecture problem.
- System of record first, derived data after.
- CDC/event sourcing differ, but both can provide ordered facts for derivation.
- Dual writes split ordering authority and can create permanent inconsistency.
- Distributed transactions use atomic commit; log-derived systems use deterministic retry and idempotence.
- Total order is consensus, and consensus is a scalability/availability boundary.
- Causality can cross shards, services, and clients; per-key ordering may not be enough.
- Batch = historical reprocessing; stream = low-delay maintenance.
- Reprocessing enables reversible evolution and side-by-side derived views.
- `CREATE INDEX` is a database-internal data pipeline.
- Federation unifies reads; unbundling unifies writes.
- Caches, indexes, and materialized views move work from read path to write path.
- Offline clients are edge replicas of server state.
- Reads can be modeled as events; subscriptions are persistent joins.
- Exactly-once must be understood as exactly-once effect, not exactly one physical execution.
- End-to-end request IDs are required for business-level duplicate suppression.
- Strict uniqueness requires coordination; sharding confines coordination to conflicting values.
- Timeliness asks "how fresh?"; integrity asks "is it true?"
- Compensation can replace synchronous constraints when business cost is acceptable.
- Trust system components most of the time, but verify end-to-end.
- Auditability improves recovery, debugging, and confidence to evolve systems.

## Interview Perspective

- Be ready to explain why dual writes are unsafe and how CDC/event logs make one system the ordering authority.
- Contrast distributed transactions with log-based integration: atomic commit versus replay, idempotence, and loose coupling.
- When asked about event-driven consistency, distinguish timeliness from integrity. This avoids the vague phrase "eventual consistency" and shows design maturity.
- For scalability questions, mention that total ordering is consensus and therefore cannot be casually globalized.
- For correctness questions, emphasize request IDs, idempotent consumers, deterministic processing, and deduplication at the business-operation level.
- For architecture tradeoffs, say unbundling is useful when no single product satisfies all access patterns, but integrated systems are preferable when they meet the requirements.
- For migration questions, use derived views and reprocessing: run old and new models side by side, shift traffic gradually, and keep rollback possible.
- For client architecture questions, describe the client as a local replica/materialized view that catches up from a stream after disconnection.
- For constraints, separate strict constraints that need coordination from loose constraints that can be checked asynchronously and repaired with compensation.
- For auditability, connect immutable events, deterministic derivation, replay, hashes, and end-to-end verification.

## Final Takeaways

- Chapter 13 presents a dataflow philosophy: treat applications as transformations from authoritative events into derived state.
- Logs, CDC, stream processors, and reprocessing let heterogeneous systems stay synchronized without relying on brittle heterogeneous distributed transactions.
- Asynchrony is not a correctness failure by itself. It weakens timeliness, but with deterministic processing, request IDs, idempotence, and replay, it can preserve integrity.
- Unbundling database features makes sense when workload breadth exceeds what one integrated product can serve, but it adds operational complexity.
- Correctness must be designed end to end. Lower-level guarantees are useful, but business operations need application-level identities and verification.
- Auditable, replayable dataflows make systems easier to debug, repair, and evolve.

Confidence: High
