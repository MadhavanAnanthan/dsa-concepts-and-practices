# DDIA Chapter 12: Stream Processing - Compressed Study Notes

Book: DDIA  
Chapter: 12 - Stream Processing  
Source PDF: books/DDIA/DDIA.pdf  
Source PDF/pages: PDF pages 511-555; printed pages 487-530  
Method: Compressed section-by-section from `books/DDIA/raw/chapter-12-chapter-12-stream-processing.md` only. Long passages are paraphrased; important concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks are preserved. No external knowledge used.

## Source Map

| Section | Source pages |
| --- | --- |
| Chapter framing | PDF 511-512; pp. 487-488 |
| Transmitting Event Streams | PDF 512-524; pp. 488-500 |
| Databases and Streams | PDF 524-537; pp. 500-512 |
| Processing Streams | PDF 537-550; pp. 513-526 |
| Fault Tolerance | PDF 550-553; pp. 526-528 |
| Chapter Summary | PDF 553-555; pp. 529-530 |

## 1. Chapter Framing: From Batch to Streams

Source: PDF 511-512; pp. 487-488

- Chapter 11 assumed bounded input: a batch job can know when it has read all input and can then produce complete output.
- Many real datasets are unbounded: user actions, sensor readings, logs, and business events continue indefinitely.
- Batch systems can approximate streaming by chopping input into daily, hourly, or second-sized batches, but freshness is limited by the batch interval.
- Stream processing removes fixed batch boundaries and processes events as they happen.
- A stream is data made available incrementally over time. Examples in the chapter include Unix stdin/stdout, lazy lists, file streams, TCP, audio, video, and event streams.
- In this chapter, streams are treated as a data management mechanism: the unbounded, continually processed counterpart to batch data.

Interview hook: Batch processing is bounded and replayable; stream processing is unbounded, incremental, and optimized for freshness.

## 2. Transmitting Event Streams

Source: PDF 512-524; pp. 488-500

### Events, Producers, Consumers, and Topics

- In streaming, a record is usually called an event: a small, self-contained, immutable description of something that happened at a point in time.
- Events usually carry a timestamp from a time-of-day clock.
- Example event sources: page views, purchases, temperature readings, CPU metrics, and web server log lines.
- Events can be encoded as text, JSON, or binary data. Encoding allows events to be stored or sent across the network.
- An event is generated once by a producer, publisher, or sender, and may be processed by many consumers, subscribers, or recipients.
- Related events are grouped into a topic or stream.
- Polling a database or file for new events works in principle, but frequent polling wastes work when low delay is required. Notification is better for continual processing.
- Traditional database triggers offer some change notification, but the chapter characterizes them as limited and not central to database design.

Interview hook: A topic in streaming plays the role that a file name plays in batch processing: it identifies related records/events.

### Messaging Systems

Source: PDF 513-519; pp. 489-495

- Messaging systems notify consumers by pushing messages from producers through a publish/subscribe model.
- Key design questions:
  - If producers outpace consumers, should the system drop messages, buffer messages, or apply backpressure?
  - If nodes crash or go offline, are messages lost, and what durability cost is acceptable?
- Backpressure blocks producers when buffers fill. Unix pipes and TCP use this model.
- Queue buffering raises operational questions: memory exhaustion, disk spill behavior, disk-full behavior, and throughput degradation.
- Message durability typically costs disk writes, replication, or both.
- Whether loss is acceptable depends on meaning: occasional sensor or metric samples may be tolerable, but lost counted events corrupt counters.
- Batch systems have a strong reliability model because failed tasks can be retried and partial output discarded. The chapter later asks how to obtain similar behavior for streams.

Interview hook: Always ask what happens under overload and failure; messaging semantics are largely defined by those answers.

### Direct Messaging

Source: PDF 514-515; pp. 490-491

- Direct producer-to-consumer communication avoids intermediary nodes.
- Examples:
  - UDP multicast for low-latency financial market feeds, with application-level packet recovery if needed.
  - ZeroMQ and nanomsg for brokerless pub/sub over TCP or IP multicast.
  - StatsD-style metrics collection over unreliable UDP.
  - Webhooks, where a service calls a registered callback URL when an event occurs.
- Strength: low latency and simplicity for specialized cases.
- Limitation: applications must handle message loss, and faults tolerated are limited.
- Many direct systems assume producers and consumers are online. If a consumer is offline, it can miss messages; producer retry buffers can be lost if the producer crashes.

Interview hook: Direct messaging is not automatically reliable just because packets may be retransmitted; offline consumers and producer crashes still matter.

### Message Brokers

Source: PDF 515-519; pp. 491-495

- A message broker, or message queue, is like a database optimized for message streams.
- Producers write messages to the broker; consumers read from the broker.
- Centralizing messages lets clients connect, disconnect, crash, and recover while durability becomes the broker's responsibility.
- Some brokers keep messages only in memory; others write to disk depending on configuration.
- Slow consumers are often handled by queueing, sometimes unbounded. This makes producer/consumer interaction asynchronous: producers usually wait only for broker buffering, not consumer processing.

### Message Brokers Compared with Databases

Source: PDF 516; p. 492

- Databases normally retain data until explicit deletion; many brokers delete messages after successful delivery.
- Brokers often assume queues are short; long backlogs can degrade throughput, especially if disk spill occurs.
- Databases support indexes and rich queries; brokers typically offer topic or topic-pattern subscription.
- Database queries read a point-in-time snapshot and do not automatically notify clients when results become stale.
- Message brokers do not support arbitrary queries or message updates after sending, but they notify consumers when new data arrives.
- The chapter names JMS and AMQP as representative traditional broker models, with systems such as RabbitMQ, ActiveMQ, IBM MQ, Azure Service Bus, and Google Cloud Pub/Sub.

Interview hook: A message broker is not just a smaller database. Its deletion, query, notification, and backlog assumptions differ.

### Multiple Consumers: Load Balancing and Fan-Out

Source: PDF 516-517; pp. 492-493

- Load balancing: each message goes to one consumer, sharing processing work across consumers. Useful when messages are expensive to process.
- Fan-out: each message goes to all consumers, allowing independent consumers to observe the same stream.
- These can be combined. Kafka consumer groups are the chapter's example: one message goes to one consumer within a group, but to one consumer in each subscribed group.

Interview hook: Load balancing answers "who does the work?"; fan-out answers "which independent applications get a copy?"

### Acknowledgments, Redelivery, and DLQs

Source: PDF 517-519; pp. 493-495

- Brokers use acknowledgments so a consumer can tell the broker that a message was processed successfully.
- If a connection closes or times out before acknowledgment, the broker redelivers the message to another consumer.
- If the message was processed but the acknowledgment was lost, redelivery can cause duplicate side effects unless operations are idempotent or atomic commit/exactly-once processing is used.
- Load balancing plus redelivery can reorder messages. Example: one consumer crashes on message m3 while another processes m4; m3 may be redelivered later, so m4 is processed before m3.
- Reordering is acceptable for independent messages but dangerous when causal dependencies matter.
- Bad messages can cause repeated crashes or blocked progress, especially under strong ordering.
- Dead letter queues move repeatedly failing messages out of the main queue so consumers can continue.
- DLQs require monitoring; a message in a DLQ indicates an error requiring drop, manual correction/reproduction, or consumer code changes.

Interview hook: At-least-once delivery plus redelivery means your consumer must be ready for duplicates; load-balanced redelivery can also break ordering.

## 3. Log-Based Message Brokers

Source: PDF 519-524; pp. 495-500

### Durable Log Mindset

- AMQP/JMS-style systems inherit a transient messaging mindset: even if messages touch disk, they are often deleted after delivery.
- Databases and filesystems usually treat writes as durable until explicitly deleted.
- Transient messaging makes repeatable derived-data processing harder: acknowledging a message can destroy the input, so a consumer cannot simply be rerun over the same data.
- Log-based brokers combine durable storage with low-latency notification.

### Using Logs for Message Storage

Source: PDF 520-521; pp. 496-497

- A log is an append-only sequence of records on disk.
- Producers append messages to the log; consumers read sequentially.
- If a consumer reaches the end, it waits for notification of new appended messages.
- To scale beyond one disk, logs are sharded. A topic can be a group of shards carrying the same event type.
- Within a shard/partition, each message receives a monotonically increasing offset and is totally ordered.
- There is no ordering guarantee across partitions.
- Kafka and Amazon Kinesis Streams are named as log-based brokers; Google Cloud Pub/Sub is described as architecturally similar but exposing a JMS-style API.
- High throughput comes from sharding across machines; fault tolerance comes from replication.

Interview hook: A Kafka-style topic is a set of ordered partition logs, not one globally ordered stream.

### Logs Compared with Traditional Messaging

Source: PDF 521-522; pp. 497-498

- Fan-out is natural because reading a log does not delete messages; multiple consumers can independently read the same log.
- Load balancing is usually done by assigning whole shards to consumers in a group.
- Downsides of shard-level load balancing:
  - Maximum parallelism is limited by the number of shards.
  - A slow message blocks later messages in the same shard.
  - Splitting one shard across threads complicates offset management.
- JMS/AMQP-style brokers are preferable when message processing is expensive, message-by-message parallelism matters, and ordering is not important.
- Log-based brokers fit high-throughput, fast-per-message workloads where ordering matters.
- Because ordering is only per shard, events that need a fixed order should be routed to the same shard, for example using user ID as a partition key.

Interview hook: Partition key selection is a correctness decision, not only a load-balancing decision.

### Consumer Offsets

Source: PDF 522; p. 498

- Sequential shard consumption makes progress tracking simple: offsets below the consumer's current offset are processed; higher offsets have not been seen.
- The broker only needs to record consumer offsets periodically instead of tracking per-message acknowledgments.
- This reduces bookkeeping and enables batching and pipelining.
- If a consumer fails after processing messages but before recording its offset, those messages can be processed again after restart.
- The offset resembles a database replication log sequence number: a follower can resume without skipping writes.

Interview hook: Consumer offsets give efficient progress tracking but generally imply duplicate processing after failure unless outputs are handled carefully.

### Disk Space, Lag, and Replay

Source: PDF 522-524; pp. 498-500

- Append-only logs must eventually reclaim disk space by deleting or archiving old segments, or by using compaction.
- A slow consumer that falls behind retained history will miss messages.
- A log-based broker is a large fixed-size buffer, similar to a ring buffer but backed by disk.
- The chapter gives a back-of-the-envelope example: at full sequential write speed, a 20 TB disk writing at 250 MB/s fills in about 22 hours; real deployments often retain days or weeks because they do not continuously saturate disk bandwidth.
- Some brokers use object storage or tiered storage, which can also make integration with batch and warehouse systems easier.
- Lag can be monitored. Large retention windows give operators time to fix slow consumers before data loss.
- If one experimental or failed consumer falls behind, other consumers are unaffected.
- Replaying old messages is a major advantage: because reading is non-destructive and offsets are controlled by consumers, a consumer can start from an older offset and regenerate derived output.
- This makes log-based messaging resemble batch processing: immutable input plus repeatable transformation into derived data.

Interview hook: Rewind/replay is what turns a message stream into a durable integration substrate rather than a transient queue.

## 4. Databases and Streams

Source: PDF 524-537; pp. 500-512

### Database Writes as Event Streams

- Event sourcing stores state changes as immutable events in an append-only log and derives read-optimized views from them.
- Even without event sourcing, every database write can be viewed as an event.
- A replication log is a stream of database write events produced by a leader and applied by followers.
- State machine replication says replicas reach the same final state if they process the same deterministic events in the same order.

Interview hook: Database replication logs are already streams; CDC exposes that idea for integration with other systems.

### Keeping Systems in Sync

Source: PDF 525-527; pp. 501-503

- Real applications often combine OLTP databases, caches, search indexes, and analytical stores.
- These systems contain related copies of data and must be synchronized.
- Batch ETL can sync warehouses and derived systems, but full dumps may be too slow for freshness.
- Dual writes mean the application writes to multiple systems directly, such as database, search index, and cache.
- Dual writes are unsafe:
  - Race condition: two clients update the same item; the database observes A then B, while the search index observes B then A, leaving different final values.
  - Fault tolerance problem: one write can succeed while another fails.
  - Atomic commit across systems is expensive.
- A better model is one leader system, such as the database, with downstream systems following its ordered change stream.

Interview hook: Dual writes break because there is no single ordering authority across systems.

### Change Data Capture

Source: PDF 527-531; pp. 503-507

- Change data capture (CDC) observes all changes written to a database and extracts them for replication to other systems.
- CDC is especially useful when changes are emitted as a stream immediately after writes.
- Derived systems such as search indexes, caches, and warehouses consume the database change stream and apply changes in the same order.
- CDC makes one database the leader and downstream systems followers.
- A log-based message broker is well suited for CDC because ordering matters.
- Logical replication logs can implement CDC, but challenges include schema changes and modeling updates.
- The chapter names Debezium, Kafka Connect, Maxwell, GoldenGate, and pgcapture as CDC-related tooling/examples.
- CDC is usually asynchronous, so adding a slow consumer does not strongly affect the source database, but replication lag applies.

### Initial Snapshot and Log Compaction

Source: PDF 528-530; pp. 504-506

- If a full log from the beginning exists, database state can be reconstructed by replaying it.
- Often the full log is too large or too slow to replay, so a new derived system needs a consistent snapshot plus a known log offset from which to resume.
- Log compaction keeps only the most recent update per key, discarding older overwritten values in the background.
- Deletes are represented by tombstones in log-structured systems.
- A compacted CDC log needs space roughly proportional to current database contents, not all historical writes.
- A new derived system can read a compacted topic from offset 0 to obtain the latest value for every key without taking a new snapshot of the source database.

Interview hook: Snapshot-plus-offset bootstraps consumers; compaction turns a stream into a recoverable current-state image.

### API Support, Quorum Databases, CDC vs Event Sourcing

Source: PDF 530-531; pp. 506-507

- Many databases now expose change streams as first-class APIs.
- Relational systems often use their own replication logs to transmit changes.
- CDC is harder for eventually consistent, quorum-based databases because there is no single leader log to follow.
- Cassandra's approach, as described in the chapter, exposes raw log segments per node; consumers must merge them into a single stream.
- CDC vs event sourcing:
  - CDC extracts low-level changes from a mutable database, preserving actual write order.
  - Event sourcing makes immutable application-level events the explicit write model.
  - CDC can often be added to existing databases with minimal application changes.
  - Event sourcing is a larger application design choice.

Interview hook: CDC is infrastructure-level capture of database mutations; event sourcing is application-level modeling of facts.

### CDC and Database Schemas

Source: PDF 531-532; pp. 507-508

- CDC can turn internal database schemas into public contracts for downstream consumers.
- Removing a column can break production services if they consume CDC events.
- Data contracts can help manage these compatibility risks.
- The outbox pattern decouples internal schemas from CDC schemas:
  - The service writes domain data and an outbox row in the same database transaction.
  - CDC exposes the outbox schema rather than the internal domain model.
  - This resembles dual writes, but both writes are inside one database, so they can be atomic.
- Outbox tradeoffs: maintaining transformations between internal and outbox schemas is extra work, and writing outbox rows increases database write volume.
- Log compaction differs:
  - CDC updates often contain a full new record value, so the latest event per key can replace older ones.
  - Event-sourced events express intent and usually do not override earlier events, so the full event history is needed. Snapshots can speed reads/recovery but do not replace the raw event log.

Interview hook: Outbox solves cross-system atomicity by moving the publishable event into the same transaction as the business write.

### State, Streams, and Immutability

Source: PDF 532-537; pp. 508-512

- Mutable state and immutable event logs are complementary.
- Current state is the result of applying all events that mutated it over time.
- The chapter uses a calculus analogy: state is the integration of an event stream over time; a change stream is like the derivative of state.
- A durable changelog makes state reproducible.
- The log may be considered the system of record, with databases and indexes as derived read-optimized states.
- Log compaction bridges log and current state by retaining only the newest version per key.

### Advantages of Immutable Events

Source: PDF 533-535; pp. 509-511

- Append-only ledgers are used in accounting: mistakes are corrected by compensating entries rather than erasing history.
- Immutable logs improve auditability and make recovery from buggy writes easier.
- Event logs capture information lost in current-state databases, such as a customer adding an item to a cart and later removing it.
- Several read models can be derived from the same event log, such as analytics stores, search indexes, caches, and application-specific views.
- New features can be built by creating new read-optimized views from existing events and running them alongside older systems.
- This reduces pressure to force one schema to serve both writes and every read pattern.

Interview hook: Immutability is not only about safety; it preserves behavior history that current state discards.

### Concurrency Control and Limitations of Immutability

Source: PDF 535-537; pp. 511-512

- CQRS/event-log consumers are usually asynchronous, so a user may write to the log and then read a derived view that has not yet caught up.
- Synchronous read-view updates would require distributed transactions or waiting for event application, both usually impractical.
- Event sourcing can simplify atomic writes because one user action can be represented as one append to the log.
- If event log and state are sharded the same way, a single-threaded consumer per shard avoids write concurrency by processing one event at a time in log order.
- Immutable data also appears in snapshot isolation and version control systems.
- Limitations:
  - High-churn datasets can make immutable history very large.
  - Compaction and garbage collection become operationally important.
  - Legal or administrative deletion may require actual data removal, not just compensating events.
  - True deletion is difficult because data may exist in storage engines, filesystems, SSDs, backups, and replicas.
  - Crypto-shredding deletes encryption keys to make encrypted data unusable, but shifts mutability and design burden to key management.

Interview hook: Immutability improves replay and auditability, but retention, deletion, and high-churn workloads are hard edge cases.

## 5. Processing Streams

Source: PDF 537-550; pp. 513-526

### What Stream Processors Do

Source: PDF 537-538; pp. 513-514

- Once a stream exists, consumers can:
  - Write events to a database, cache, search index, or similar store.
  - Push events to humans via alerts, notifications, or dashboards.
  - Process one or more input streams into one or more output streams.
- A stream-processing operator/job consumes input streams read-only and writes output append-only.
- Mapping, filtering, sharding, and parallelization resemble batch/dataflow systems.
- The crucial difference is that streams never end.
- Consequences:
  - Full sorting over an unbounded dataset is not meaningful.
  - Sort-merge joins do not directly apply.
  - Fault tolerance cannot rely on restarting a years-long job from the beginning.

Interview hook: Stream processing is dataflow over infinite input; the "never ends" property changes sorting, joins, and recovery.

### Uses of Stream Processing

Source: PDF 538-542; pp. 514-518

#### Complex Event Processing

- CEP searches for patterns of events in streams.
- It is compared to regular expressions for events: standing rules detect event sequences.
- CEP engines use declarative languages or GUIs to define patterns.
- The engine maintains state machines and emits a complex event when a pattern is detected.
- CEP reverses the usual database relationship: queries are stored long-term, and incoming events are checked against them.
- Examples named in the chapter include Esper, Apama, TIBCO StreamBase, Flink, and Spark Streaming SQL.

Interview hook: In CEP, the query is durable and the data is transient; in a normal database query, the data is durable and the query is transient.

#### Stream Analytics

- Stream analytics focuses more on aggregations and statistical metrics than specific event sequences.
- Examples: event rates, rolling averages, comparisons to previous intervals, and anomaly/alert metrics.
- Analytics usually operates over time windows, such as five-minute request rates and 99th percentile response times.
- Probabilistic algorithms such as Bloom filters, HyperLogLog, and percentile estimators can reduce memory use.
- Approximation is an optimization, not an inherent property of stream processing.
- Frameworks named include Storm, Spark Streaming, Flink, Samza, Apache Beam, Kafka Streams, Google Cloud Dataflow, and Azure Stream Analytics.

Interview hook: Streaming can be exact; approximate algorithms are chosen for memory/performance tradeoffs.

#### Maintaining Materialized Views and IVM

- Streams of database changes can keep caches, search indexes, warehouses, and application state up to date.
- For materialized view maintenance, a bounded time window is often insufficient because the view may depend on all relevant history, except obsolete compacted events.
- Some analytics-oriented frameworks assume limited-duration windows, which may not suit durable materialized views.
- Traditional database materialized views are often refreshed by periodic batch jobs or explicit refresh commands.
- Drawbacks of periodic refresh:
  - Poor efficiency because unchanged data is reprocessed.
  - Poor freshness because source changes are invisible until refresh.
- Incremental view maintenance (IVM) converts queries into incremental operators that update only changed parts.
- The chapter names Materialize, RisingWave, ClickHouse, and Feldera as systems using IVM techniques.

Interview hook: IVM is the streaming answer to materialized view refresh: update deltas rather than recomputing the full view.

#### Search on Streams

- Stream search stores queries and evaluates incoming documents/events against them.
- Examples include media monitoring and real-estate alerts.
- Elasticsearch percolator is named as an implementation option.
- Naively testing every document against every query can be slow; indexing queries as well as documents can narrow candidates.

Interview hook: Stream search flips search indexing: instead of indexing documents first, it keeps standing queries and matches each incoming item.

#### Event-Driven Architectures and RPC

- Actor/message-passing systems overlap with stream processing but are usually not considered stream processors.
- Differences:
  - Actor frameworks manage concurrency and distributed modules; stream processing is primarily data management.
  - Actor messages are often ephemeral and one-to-one; event logs are durable and multi-subscriber.
  - Actors can use arbitrary cyclic communication; stream processors are usually acyclic pipelines from defined input streams to output streams.
- Some crossover exists, such as Storm distributed RPC or stream processing built with actor frameworks.
- Many actor frameworks do not guarantee delivery after crashes unless additional retry logic is implemented.

Interview hook: Event-driven architecture is not automatically stream processing; durability, replay, and dataflow structure matter.

### Reasoning About Time

Source: PDF 542-547; pp. 518-522

#### Event Time vs Processing Time

- Time windows are tricky because the relevant time may be when an event happened, not when the processor sees it.
- Batch processing naturally uses event timestamps for historical data; processing clock time is usually irrelevant.
- Some stream frameworks use processing time because it is simple and works when event-to-processing delay is tiny.
- Processing time breaks down with queueing, network faults, broker contention, processor restarts, reprocessing, or backlog recovery.
- Events can arrive out of order. Example: request A happens before request B, but B's event reaches the broker first.
- The chapter uses Star Wars release order vs episode order as an analogy: processing order can differ from event-time order.
- Confusing processing time with event time creates bad data, such as an artificial traffic spike after a processor restarts and quickly drains backlog.

Interview hook: Ask "time according to whom?" before designing windows, metrics, joins, or alerts.

#### Straggler Events and Clocks

Source: PDF 544-546; pp. 520-522

- With event-time windows, you cannot know with certainty that all events for a window have arrived.
- Stragglers are events that arrive after a window has been declared complete.
- Options:
  - Ignore late events and track/alert on dropped-event counts.
  - Publish corrections and possibly retract earlier output.
- Some systems use special messages indicating that no earlier timestamps will arrive; with multiple producers, consumers must track producer-specific thresholds.
- Producer churn makes this harder.
- Mobile/offline clients complicate timestamps because events may be buffered for hours or days.
- Device clocks represent the user interaction time but may be wrong; server receipt time is more trustworthy but less semantically meaningful.
- One approach logs three timestamps: event time on device, send time on device, and receive time on server. The server/device offset can estimate corrected event time if network delay is negligible and device offset is stable.

Interview hook: Event-time correctness often depends on clock trust, buffering, watermarks/thresholds, and correction strategy.

#### Types of Windows

Source: PDF 545-547; pp. 521-522

- Tumbling window: fixed length; each event belongs to exactly one window.
- Hopping window: fixed length with overlap; useful for smoothing. Can be built from smaller tumbling windows.
- Sliding window: contains events within a certain interval of each other; often requires buffering time-sorted events and expiring old ones.
- Session window: no fixed duration; groups events for the same user until an inactivity gap ends the session.
- Window operations maintain state. Counters can be fixed-size, but sliding windows and stream joins may need large buffers.
- Large windows or high-throughput streams require careful capacity planning for memory or disk state.

Interview hook: Window type determines both semantics and state cost.

### Stream Joins

Source: PDF 547-550; pp. 523-526

#### Stream-Stream Join

- Stream-stream joins relate activity events that occur within a time window.
- Example: joining search events and click events by session ID to compute click-through rates.
- Clicks may never happen; may occur seconds, days, or weeks later; or may arrive before the search event due to delays.
- A join window bounds how long the processor waits, such as one hour.
- Embedding search details in click events is not enough, because it loses searches with no click. Accurate click-through rates require both clicked and non-clicked searches.
- Implementation: maintain recent events indexed by session ID for both streams; on each event, check the other index; emit match events or, on expiry, emit no-click events.

Interview hook: Stream-stream joins are windowed because both sides are unbounded activity streams.

#### Stream-Table Join

- Stream-table joins enrich activity events with database state, such as adding user profile information to events by user ID.
- Remote database lookup per event can be slow and can overload the database.
- A local copy of the table can be used as a hash table or local-disk index.
- Unlike batch, the stream processor is long-running and the table changes over time.
- CDC can keep the local copy updated from the table's changelog.
- Conceptually, this becomes a join between an activity stream and a table-update stream.
- The table side uses a window reaching back to the beginning of time, with newer records overwriting older ones.

Interview hook: Stream-table join equals enrichment plus a continuously maintained local table.

#### Table-Table Join

- Table-table joins maintain materialized views from two database changelog streams.
- Example: social network home timeline cache.
- Required event handling:
  - New post: add to timelines of followers.
  - Post/account deletion: remove from affected timelines.
  - Follow: add recent followee posts to follower timeline.
  - Unfollow: remove followee posts from follower timeline.
- The stream processor maintains follower state so it knows which timelines to update.
- The timeline cache is a materialized view of a join between posts and follows.
- The chapter uses a derivative/product-rule analogy: changes to the join result come from changes on either side joined with current state on the other side.

Interview hook: A table-table stream join is materialized view maintenance over changelogs.

#### Time Dependence of Joins

Source: PDF 549-550; pp. 525-526

- All join types require maintaining state from one input and querying it while processing the other input.
- Event order matters: follow then unfollow differs from unfollow then follow.
- Sharded logs preserve ordering within a shard, not across different streams or shards.
- If cross-stream ordering is undefined, joins can become nondeterministic: rerunning the same job may produce different results.
- Example: joining sales to tax rates should use the tax rate valid at the sale time, not necessarily the current rate.
- Data warehouses call this a slowly changing dimension problem.
- A deterministic solution is to use unique identifiers for versions of joined records, such as a tax-rate version ID included in an invoice.
- Tradeoff: retaining all versions prevents ordinary log compaction.
- Alternative: denormalize needed state, such as the applicable tax rate, directly into each event.

Interview hook: Stream joins are not just key lookups; they are time-dependent state lookups.

## 6. Fault Tolerance

Source: PDF 550-553; pp. 526-528

### Exactly-Once as Visible Effect

- Batch fault tolerance can discard failed task output and retry tasks because input files are immutable and output becomes visible only after success.
- This makes output appear as if each record was processed exactly once, even if some records were physically processed more than once.
- The chapter notes that "effectively-once" would describe this better.
- In streams, output is continuous and the job never finishes, so a framework cannot simply wait until completion before revealing output.

Interview hook: Exactly-once is about externally visible effects, not about whether CPU executed a function once.

### Microbatching and Checkpointing

Source: PDF 551; p. 527

- Microbatching divides a stream into small batches and processes each like a mini batch job.
- Spark Streaming is named as using microbatching.
- Typical batch size is around one second: smaller batches increase coordination overhead; larger batches increase output latency.
- Microbatching creates a processing-time tumbling window equal to the batch size, so larger/event-time windows need explicit state carryover.
- Flink-style checkpointing periodically writes rolling snapshots of operator state to durable storage.
- Checkpoints are triggered by barriers in the stream, similar to microbatch boundaries, but do not force a specific window size.
- Inside the framework, microbatching/checkpointing can provide batch-like exactly-once semantics.
- Once output leaves the framework, such as database writes, external broker publishes, or emails, the framework cannot discard duplicated side effects by itself.

Interview hook: Checkpoints protect internal state; external side effects still need idempotence or atomic commit.

### Atomic Commit Revisited

Source: PDF 551-552; pp. 527-528

- Exactly-once visible effects require all outputs and side effects of processing an event to persist if and only if processing succeeds.
- This includes downstream messages, external messages, email/push notifications, database writes, operator state, and input acknowledgments or consumer offset advancement.
- These actions must be atomic: all happen or none happen.
- Traditional XA-style distributed transactions have problems, but restricted stream frameworks can implement efficient internal atomic commit.
- The chapter names Google Cloud Dataflow, VoltDB, and Apache Kafka as using such approaches.
- These systems avoid heterogeneous transactions by managing state changes and messaging inside the stream-processing framework.
- Transaction overhead can be amortized by processing several input messages per transaction.

Interview hook: Exactly-once across arbitrary external systems is the hard part; internal framework transactions are a narrower problem.

### Idempotence

Source: PDF 552; p. 528

- Idempotence means repeating an operation has the same effect as doing it once.
- Deleting a key is idempotent; incrementing a counter is not.
- Non-idempotent operations can sometimes be made idempotent with metadata.
- Example: write the Kafka offset that triggered an external database update alongside the value, so retries can detect whether the update was already applied.
- Idempotence assumptions:
  - Failure recovery replays the same messages in the same order.
  - Processing is deterministic.
  - No other node concurrently updates the same value.
  - Fencing may be needed to stop a supposedly dead node from interfering after failover.
- With caveats, idempotence can achieve exactly-once-like semantics with low overhead.

Interview hook: Idempotence converts duplicate delivery from a correctness bug into a harmless retry, but only under clear assumptions.

### Rebuilding State After Failure

Source: PDF 553; p. 528

- Stateful stream processors must recover windowed aggregates, join indexes, local table copies, and other operator state.
- Option 1: keep state in a replicated remote datastore, but per-message remote queries may be slow.
- Option 2: keep state local and periodically replicate or snapshot it.
- Examples:
  - Flink snapshots operator state to durable storage.
  - Kafka Streams replicates state changes to a compacted Kafka topic.
  - VoltDB redundantly processes each input message on several nodes.
- Sometimes state can be rebuilt by replaying input streams:
  - Short-window aggregates can replay recent events.
  - CDC-maintained local database replicas can be rebuilt from compacted change streams.
- There is no universal best choice; local vs remote state depends on network, disk, bandwidth, and evolving infrastructure characteristics.

Interview hook: Stream fault tolerance is state recovery plus duplicate-safe output.

## Chapter-Level Memory Hooks

- Stream = unbounded, incrementally available data.
- Event = immutable fact about something that happened at a time.
- Topic/stream = related events, like a file for streaming systems.
- Overload choices = drop, buffer, or backpressure.
- Traditional broker = per-message delivery, acknowledgments, deletion after processing.
- Log broker = append-only durable partitions, offsets, replay, fan-out, per-partition order.
- Consumer group = load balancing within group, fan-out across groups.
- CDC = database leader exposes ordered changes; derived systems become followers.
- Dual writes fail because there is no single ordering authority.
- Log compaction = retain latest value per key; compacted log can rebuild current state.
- Event sourcing = application-level immutable events; CDC = database-level mutation stream.
- State is the integral of events; changelog is the derivative of state.
- Event time != processing time.
- Late events require dropping, correcting, or waiting via timestamp thresholds/watermarks.
- Windows = tumbling, hopping, sliding, session.
- Joins = stream-stream windows, stream-table enrichment, table-table materialized views.
- Exactly-once means exactly-once visible effects, usually via checkpointing, atomic commit, or idempotence.

## Interview Perspective

- Explain why stream processing exists: batch jobs over unbounded data create freshness delays.
- Compare AMQP/JMS-style brokers with log-based brokers in terms of deletion, replay, ordering, offsets, fan-out, and load balancing.
- Be precise about delivery guarantees: acknowledgments prevent loss but can create duplicates; redelivery can reorder under load balancing.
- Call out DLQs for poisonous messages that repeatedly fail.
- Explain why Kafka-style partitioning gives order only within a partition and why partition keys matter.
- Use dual writes as the classic consistency failure; use CDC as the ordered-follower solution.
- Distinguish CDC, event sourcing, outbox, and log compaction.
- For stream analytics, always ask whether windows use event time or processing time.
- For late events, discuss drop-vs-correction tradeoffs and clock trust.
- For joins, identify which type it is and what state must be stored.
- For exactly-once, say what system boundary the guarantee covers. Internal framework state is easier than arbitrary external side effects.
- Mention idempotence assumptions: deterministic replay, same order, exclusive update ownership, and fencing when needed.

## Final Takeaways

- Stream processing generalizes batch processing to never-ending input, but the infinite nature of streams changes sorting, joins, state, and fault recovery.
- Message systems are defined by overload and failure behavior: dropping, buffering, backpressure, durability, acknowledgments, and redelivery.
- Log-based brokers are especially powerful because they make stream input durable, replayable, ordered per partition, and suitable for derived data.
- Database writes are streams. CDC and event sourcing use this fact to keep derived systems synchronized and rebuildable.
- Immutability improves auditability, recovery, and view derivation, but deletion, high churn, and schema exposure are real limitations.
- Time is central: event time, processing time, late arrivals, clock trust, and window definitions determine correctness.
- Stream joins require state and careful time semantics.
- Fault tolerance depends on replayable input, recoverable state, and duplicate-safe or atomic outputs.

Confidence: High
