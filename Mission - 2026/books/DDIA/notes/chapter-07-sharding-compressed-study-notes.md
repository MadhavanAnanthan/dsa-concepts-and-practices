# Chapter 07: Sharding - Compressed Study Notes

Book: DDIA
Chapter: 7, Sharding
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 275-297
Raw source used: books/DDIA/raw/chapter-07-chapter-7-sharding.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 7 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter framing: replication versus sharding | 275-276 |
| Sharding and partitioning terminology | 276-277 |
| Pros and cons of sharding | 277-278 |
| Sharding for multitenancy | 278-279 |
| Sharding of key-value data: goals, skew, hot shards | 279-280 |
| Sharding by key range | 280-281 |
| Rebalancing key-range sharded data | 281-282 |
| Sharding by hash of key and modulo problem | 282-283 |
| Fixed number of shards | 283-285 |
| Sharding by hash range | 285-287 |
| Data warehouse partitioning and range queries | 286 |
| Consistent hashing | 287 |
| Skewed workloads and relieving hot spots | 287-288 |
| Automatic versus manual rebalancing | 288-289 |
| Request routing | 289-292 |
| Sharding and secondary indexes | 292 |
| Local secondary indexes | 292-294 |
| Global secondary indexes | 294-295 |
| Chapter summary | 295-297 |

## 1. Chapter Frame: Sharding Splits Data; Replication Copies Data

Source pages: 275-276

- A distributed database commonly distributes data in two different ways:
  - Replication: store copies of the same data on multiple nodes.
  - Sharding: split a dataset into smaller shards or partitions, storing different shards on different nodes.
- In the chapter's model, each record, row, or document belongs to exactly one shard.
- A shard behaves like a small database, though some database systems support operations across multiple shards.
- Sharding is usually combined with replication:
  - Each record belongs to one shard logically.
  - Copies of that shard may exist on several nodes for fault tolerance.
- With single-leader replication, each shard can have its own leader and followers.
- A node can be leader for some shards and follower for others.

Interview hook: Replication answers "how many copies?" Sharding answers "which subset of the data lives where?" Real distributed databases often use both.

## 2. Sharding and Partitioning: Same Idea, Different Names

Source pages: 276-277

- Different systems use different names for shard-like units:
  - Kafka: partition.
  - CockroachDB: range.
  - HBase and TiDB: region.
  - Couchbase: vBucket.
  - Riak: vnode.
  - Cassandra: token-range.
  - Bigtable, YugabyteDB, and ScyllaDB: tablet.
- Some databases distinguish partitioning from sharding:
  - In PostgreSQL, partitioning can split a large table into several files on the same machine.
  - Sharding splits a dataset across multiple machines.
- In many other systems, partitioning and sharding are used as synonyms.
- Partitioning in this chapter is unrelated to network partitions or netsplits.
- Replication behavior from Chapter 6 applies to replicated shards, but Chapter 7 mostly studies sharding independently of replication.

Interview hook: Be precise with terminology. Ask whether "partition" means a storage-layout split on one machine, a distributed shard across machines, or a network failure.

## 3. Pros and Cons: Sharding Is for Scale, but It Adds Operational and Query Complexity

Source pages: 277-278

- Primary reason for sharding: scalability when data volume or write throughput is too large for one node.
- If read throughput is the main issue, read scaling through replication may be enough.
- Sharding supports horizontal scaling:
  - Add more machines instead of moving to one bigger machine.
  - Divide data and queries so shards can be processed in parallel.
- Sharding is a heavyweight technique and is mostly justified at large scale.
- If a single machine can handle the data and write load, a single-shard database is often simpler.
- The key added complexity is choosing a partition key:
  - Records with the same partition key go to the same shard.
  - Access is efficient when the target shard is known.
  - If the shard is unknown, the system may need to search all shards.
  - Changing the sharding scheme later is difficult.
- Sharding fits key-value data naturally because the key can often be the partition key.
- Sharding is harder for relational data:
  - Secondary-index lookups may not identify a single shard.
  - Joins may involve records on different shards.
- Cross-shard writes may need distributed transactions.
- Distributed transactions are available in some databases, but the chapter notes that they are usually slower than single-node transactions and can become a system bottleneck.
- Some systems shard even on one machine:
  - One process per CPU core.
  - Useful for CPU parallelism or NUMA-aware memory locality.
  - Examples in the source include Redis, VoltDB, and FoundationDB.

Interview hook: Do not say "shard for performance" generically. Say "shard when data volume or write throughput exceeds one node; avoid it when replication or one machine is sufficient."

## 4. Sharding for Multitenancy: Isolation Benefits, Growth Problems

Source pages: 278-279

- Multitenant systems host many customer datasets in one service.
- A tenant may contain multiple users, but its data is self-contained and separate from other tenants.
- Sharding can implement multitenancy:
  - One tenant per shard.
  - Or several small tenants grouped into one shard.
  - Shards may be physically separate databases or manageable parts of a larger logical database.
- Advantages:
  - Resource isolation: one tenant's expensive operation is less likely to hurt tenants on other shards.
  - Permission isolation: physical separation reduces the blast radius of access-control bugs.
  - Cell-based architecture: application services and storage for a tenant group form an independent cell, limiting faults to that cell.
  - Per-tenant backup and restore: restore one tenant without affecting others.
  - Regulatory compliance: data export and deletion can become simpler when a person's data is isolated in a shard.
  - Data residence: tenant data can be assigned to a required region.
  - Gradual schema rollout: migrations can be rolled out one tenant at a time, reducing risk, though transactional rollout can be hard.
- Challenges:
  - A single tenant may become too large for one node, requiring sharding inside that tenant.
  - One shard per tiny tenant can create too much overhead.
  - Grouping small tenants creates a later migration problem as tenants grow.
  - Features that connect data across tenants become harder because they require cross-shard joins.

Interview hook: Tenant-per-shard is attractive for isolation, not just scale. The catch is tenant size distribution and cross-tenant features.

## 5. Key-Value Sharding Goal: Even Data and Even Load

Source pages: 279-280

- Sharding decides which records go to which nodes.
- The goal is to spread data and query load evenly across nodes.
- In theory, 10 balanced nodes can handle roughly 10 times the data and read/write throughput of one node, ignoring replication.
- When nodes are added or removed, the system should rebalance load across the new cluster size.
- Skew means some shards have more data or query load than others.
- Skew reduces the benefit of sharding; an extreme case makes one node the bottleneck while others idle.
- A hot shard or hot spot is a shard with disproportionately high load.
- A hot key is one partition key with especially high load, such as a celebrity user in a social network.
- The sharding algorithm maps a record's partition key to a shard.
- In key-value stores, the partition key is usually the key or the first part of the key.
- In relational systems, the partition key may be a table column and does not have to be the primary key.
- The algorithm must support rebalancing so hot spots can be relieved.

Interview hook: Balanced key distribution is not the same as balanced workload. A single hot key can defeat an otherwise good sharding algorithm.

## 6. Sharding by Key Range: Sorted Keys, Efficient Range Scans, Hot-Range Risk

Source pages: 280-281

- Key-range sharding assigns contiguous ranges of partition keys to shards.
- The chapter uses a paper encyclopedia analogy:
  - Each volume owns a title range.
  - To find an entry, choose the volume whose range contains the title.
- Ranges do not need to be evenly spaced because real data is not evenly distributed.
- Shard boundaries must adapt to the data distribution.
- Boundaries may be chosen manually or automatically.
- Examples from the source:
  - Manual key-range sharding: Vitess.
  - Automatic variants: Bigtable, HBase, MongoDB range-based sharding, CockroachDB, RethinkDB, FoundationDB.
  - YugabyteDB supports both manual and automatic tablet splitting.
- Inside each shard, keys are stored in sorted order, such as with B-trees or SSTables.
- Advantages:
  - Efficient range scans.
  - The key can act like a concatenated index, helping fetch related records in one query.
- Sensor example:
  - Timestamp keys make it easy to fetch readings for a time range such as a month.
- Downside:
  - Writes to nearby keys can create a hot shard.
  - Timestamp keys place current writes into the current time range, overloading one shard.
- Mitigation in the sensor example:
  - Prefix timestamp with sensor ID.
  - Write load spreads across many active sensors.
  - Downside: querying multiple sensors over a time range now requires a separate range query per sensor.

Interview hook: Key-range sharding preserves locality. Locality helps range reads but can concentrate writes.

## 7. Rebalancing Key-Range Data: Split, Merge, and Pay the Rewrite Cost

Source pages: 281-282

- Empty databases initially have no natural key ranges.
- Some systems allow pre-splitting:
  - Configure initial shard boundaries before loading data.
  - Requires knowing the expected key distribution.
  - Source examples include HBase and MongoDB.
- As data volume or write throughput grows, key-range systems split an existing shard into smaller shards.
- Each new shard owns a contiguous subrange of the original key range.
- New smaller shards can be distributed across multiple nodes.
- If data is deleted and adjacent shards become small, they may need to be merged.
- The source compares this split/merge behavior to the top level of a B-tree.
- Automatic shard splitting can be triggered by:
  - Shard size, such as HBase's default size mentioned in the source.
  - Persistently high write throughput in some systems.
- Splitting a hot shard can spread write load even if the shard is not storing much data.
- Advantage:
  - Number of shards adapts to data volume.
  - Small datasets need few shards and low overhead.
  - Large datasets can keep shard size below a configurable maximum.
- Limitation:
  - Splitting is expensive because data is rewritten into new files, similar to log-structured compaction.
  - The shard needing a split may already be under high load.
  - The split operation can worsen overload risk.

Interview hook: Automatic splitting is not free. The work needed to fix a hot shard can add load exactly where the system is already stressed.

## 8. Hash Sharding: Uniform Key Distribution, Lost Ordering

Source pages: 282-283

- Hash sharding first hashes the partition key and then maps the hash to a shard.
- It is useful when nearby keys do not need to be stored together, such as tenant IDs in a multitenant application.
- A good hash function spreads skewed input values uniformly across its output range.
- Similar input strings should produce widely distributed hash values, while the same input must always produce the same output.
- The hash function does not need to be cryptographically strong.
- Source examples:
  - MongoDB uses MD5.
  - Cassandra and ScyllaDB use Murmur3.
- Built-in language hash functions may be unsuitable because they may produce different values in different processes.
- Source examples of unsuitable process-dependent hashes include Java Object.hashCode() and Ruby Object#hash.

Interview hook: For database sharding, determinism across processes matters more than cryptographic strength.

## 9. Hash Modulo Node Count: Simple but Bad for Rebalancing

Source pages: 282-283

- A tempting mapping is `hash(key) % N`, where `N` is the number of nodes.
- This maps each key directly to a node number.
- Problem:
  - When `N` changes, most keys move to different nodes.
  - Adding a node causes widespread unnecessary data movement.
- The chapter's example shows a three-node cluster adding a fourth node; keys that previously mapped to one node often move elsewhere.
- The modulo method is easy to compute but inefficient for rebalancing.
- A better approach should move as little data as possible.

Interview hook: `hash % node_count` is a common interview trap. It distributes data, but it makes cluster resizing expensive.

## 10. Fixed Number of Shards: Move Shards, Not Individual Keys

Source pages: 283-285

- A widely used solution is to create many more shards than nodes.
- Example from the source:
  - 10 nodes.
  - 1,000 shards from the beginning.
  - About 100 shards assigned to each node.
  - A key maps to `hash(key) % 1000`.
- The system separately tracks shard-to-node assignment.
- When a node is added:
  - Reassign some existing shards to the new node.
  - Continue until shards are fairly distributed.
- When a node is removed, the process happens in reverse.
- Only whole shards move between nodes.
- The number of shards and key-to-shard mapping do not change.
- During transfer, reads and writes continue using the old assignment until movement completes.
- It is common to choose a shard count divisible by many factors so different node counts can divide the dataset evenly.
- Different hardware capacities can be handled by assigning more shards to stronger nodes.
- Source examples using this approach include Citus, Riak, Elasticsearch, and Couchbase.
- Works well if the initial shard count estimate is good.
- Limitations:
  - Cannot have more nodes than shards.
  - If the shard count is wrong, expensive resharding is needed.
  - Resharding may split shards and rewrite files, using significant extra disk space.
  - Some systems cannot reshard while writes continue, making downtime hard to avoid.
  - Fixed shard count is hard when dataset size varies widely over time.
  - Very large shards make rebalancing and failure recovery expensive.
  - Very small shards create overhead.

Interview hook: Fixed shard count is operationally simple until growth invalidates the original estimate.

## 11. Hash-Range Sharding: Adaptive Shard Count with Hash Uniformity

Source pages: 285-287

- Hash-range sharding combines a hash function with range sharding.
- Each shard owns a contiguous range of hash values rather than a range of original keys.
- It is useful when the number of shards cannot be predicted in advance.
- It avoids key-range hot spots caused by nearby keys such as consecutive timestamps.
- Example from the source:
  - A 16-bit hash returns values from 0 to 65,535.
  - Shard 0 could own 0 to 16,383.
  - Shard 1 could own 16,384 to 32,767.
- Shards can be split when they become too large or too heavily loaded.
- Advantages:
  - Shard count adapts to data volume.
  - Similar input keys are spread across hash ranges.
- Downside:
  - Range queries over the partition key become inefficient because keys in the logical range are scattered across shards.
- Important exception:
  - If a key has multiple columns and only the first column is the partition key, range queries over later columns can still be efficient.
  - As long as all queried records share the same partition key, they stay in the same shard.
- Source examples:
  - YugabyteDB and DynamoDB use hash-range sharding.
  - MongoDB offers it as an option.
  - Cassandra and ScyllaDB use a related approach with multiple hash ranges per node and random boundaries.
- Cassandra/ScyllaDB variant:
  - Split hash space into ranges proportional to node count.
  - Assign several ranges to each node.
  - Random range-boundary imbalance tends to even out when each node owns multiple ranges.
  - When nodes are added or removed, ranges are adjusted, split, or merged to give nodes a fair share with limited data movement.

Interview hook: Hash range gives adaptive rebalancing like key ranges, but the ordering you preserve is hash order, not application key order.

## 12. Data Warehouse Note: Partitioning and Clustering Support Range-Oriented Access

Source page: 286

- Data warehouses use similar ideas with different terms.
- Source examples:
  - BigQuery: partition key chooses the partition, cluster columns define sorting inside a partition.
  - Snowflake: automatically assigns records to micro-partitions and allows table cluster keys.
  - Delta Lake: supports manual and automatic partition assignment plus cluster keys.
- Clustering can improve:
  - Range scan performance.
  - Compression.
  - Filtering performance.

Interview hook: OLTP sharding and analytical partitioning are related, but the terminology and query patterns differ.

## 13. Consistent Hashing: Minimize Movement When Shards Change

Source page: 287

- A consistent hashing algorithm maps keys to a specified number of shards while satisfying two goals:
  - Roughly equal number of keys per shard.
  - When shard count changes, move as few keys as possible.
- "Consistent" here does not mean replica consistency or ACID consistency.
- It means a key tends to remain assigned to the same shard when possible.
- The Cassandra and ScyllaDB algorithm is similar to the original consistent hashing definition.
- Other algorithms mentioned in the source:
  - Highest random weight, also called rendezvous hashing.
  - Jump consistent hashing.
- Some consistent hashing variants assign a new node individual keys that were previously scattered across all nodes, rather than splitting a few existing ranges.
- The preferable variant depends on the application.

Interview hook: Consistent hashing is about stable placement under membership change, not about read/write consistency.

## 14. Skewed Workloads and Hot Spots: Uniform Hashes Do Not Guarantee Uniform Load

Source pages: 287-288

- Consistent hashing distributes keys uniformly, but workload can still be skewed.
- Skew can mean:
  - Some partition keys have much more data.
  - Some keys receive far more requests.
- A celebrity social-media post can create huge read/write volume on one key.
- More flexible sharding may be required.
- If shards are based on key ranges or hash ranges, a single hot key may be placed in its own shard and possibly on a dedicated machine.
- Application-level mitigation:
  - Add a random prefix or suffix to a known hot key.
  - Two random digits split writes across 100 derived keys.
  - Those derived keys can be placed on different shards.
- Tradeoffs of randomizing a hot key:
  - Reads must fetch all derived keys and combine results.
  - Read load per hot-key shard is not reduced; only write load is split.
  - Requires bookkeeping to know which keys are split.
  - Should only be used for a small number of hot keys because most keys do not need the overhead.
  - Requires a process for converting an ordinary key into a specially managed hot key.
- Load changes over time:
  - A viral post may be hot for a few days and then cool down.
  - Some keys are hot for writes; others are hot for reads.
  - Different hot-key types may need different strategies.
- Some large-scale cloud services have automated hot-shard handling, but the chapter does not describe those mechanisms in detail.

Interview hook: Hashing solves placement skew, not popularity skew. Hot-key strategy must consider read versus write heat.

## 15. Automatic Versus Manual Rebalancing: Convenience Versus Predictability

Source pages: 288-289

- Systems vary in how rebalancing is triggered:
  - Fully automatic split and movement.
  - Fully manual administrator configuration.
  - Middle ground where the system suggests a plan and an administrator approves it.
- Source middle-ground examples: Couchbase and Riak.
- Benefits of automatic rebalancing:
  - Less normal operational work.
  - Can support autoscaling as workload changes.
  - Cloud databases may add/remove shards in response to large load changes.
- Risks:
  - Rebalancing is expensive.
  - It reroutes requests and moves large data volumes.
  - Poorly controlled rebalancing can overload nodes or network links.
  - The system must continue processing writes during movement.
  - If the system is near maximum write throughput, shard splitting may not keep up with incoming writes.
- Dangerous interaction:
  - Automatic failure detection may classify a slow overloaded node as dead.
  - Automatic rebalancing then moves load elsewhere.
  - Extra movement load can worsen cluster pressure.
  - This can contribute to cascading failure.
- Human-in-the-loop rebalancing:
  - Slower than full automation.
  - Can prevent operational surprises.
  - Useful for preemptive rebalancing before known traffic events.

Interview hook: Automation can fix routine imbalance, but it can also amplify overload if failure detection and rebalancing react too aggressively.

## 16. Request Routing: Find the Node That Owns the Shard

Source pages: 289-292

- Request routing asks: given a key, which IP address and port should the client contact?
- It resembles service discovery, but sharded databases differ because not every node can handle every key.
- A request for a key must reach a replica of the shard containing that key.
- Routing must know:
  - Key-to-shard assignment.
  - Shard-to-node assignment.
- Three routing approaches:
  - Client contacts any node. If the node owns the shard, it handles the request; otherwise it forwards the request to the right node.
  - Client sends requests to a routing tier, which forwards each request to the correct node.
  - Client is shard-aware and connects directly to the correct node.
- Key problems:
  - Who decides shard placement?
  - If one coordinator decides, how is that coordinator made fault-tolerant?
  - If coordinator failover exists, how is split brain avoided?
  - How do clients, nodes, or routers learn about shard-assignment changes?
  - During shard movement, how are in-flight requests to the old node handled after cutover?
- Many systems use a coordination service such as ZooKeeper or etcd.
- Coordination service role:
  - Maintain authoritative shard-to-node mapping.
  - Use consensus algorithms for fault tolerance and split-brain protection.
  - Allow nodes to register themselves.
  - Notify routers or shard-aware clients when ownership changes.
- Source examples:
  - HBase and SolrCloud use ZooKeeper for shard assignment.
  - Kubernetes uses etcd for service-instance tracking.
  - MongoDB uses config servers and mongos routing daemons.
  - Kafka, YugabyteDB, TiDB, and ScyllaDB use built-in Raft-based coordination.
  - Riak uses gossip to disseminate cluster state, with weaker consistency than consensus.
- Gossip risk:
  - Different cluster parts can temporarily disagree about shard assignment.
  - This can produce split brain.
  - Leaderless systems can tolerate weaker assignment consistency because their consistency guarantees are already weaker.
- DNS can often be used for the less rapidly changing problem of finding router or random-node IP addresses.
- The request-routing discussion mainly targets sharded OLTP databases.
- Analytical databases also shard data, but queries often aggregate and join across many shards in parallel.

Interview hook: Sharding needs metadata. The hard part is keeping placement metadata correct while nodes and shards move.

## 17. Sharding and Secondary Indexes: Primary-Key Routing Stops Being Enough

Source page: 292

- Earlier sharding schemes assume the client knows the partition key.
- This is easy in key-value models where the partition key is the primary key or first part of it.
- Secondary indexes complicate this assumption.
- A secondary index usually searches for all records with a value rather than identifying one record uniquely.
- Source examples of secondary-index searches:
  - All actions by user 123.
  - Articles containing a word.
  - Cars whose color is red.
- Key-value stores often lack secondary indexes.
- Relational databases and document databases commonly support them.
- Full-text search engines rely heavily on this indexing style.
- Secondary indexes do not map neatly to shards.
- Two main approaches:
  - Local secondary indexes.
  - Global secondary indexes.

Interview hook: Once users search by non-partition attributes, every clean single-shard lookup assumption must be revisited.

## 18. Local Secondary Indexes: Cheap Writes, Expensive Scatter Reads

Source pages: 292-294

- With local secondary indexes, each shard indexes only the records it stores.
- A shard does not index records stored elsewhere.
- Writes only touch the shard containing the written record.
- This is also called a document-partitioned index in information retrieval.
- Used-car example:
  - Listings are sharded by listing ID.
  - Users search by color or make.
  - Each shard maintains its own index entries, such as color:red, for local records only.
  - The list of matching IDs is a postings list.
- Warning for key-value systems:
  - Application-built secondary indexes must be kept consistent with source data.
  - Race conditions and partial write failures can cause index/data divergence.
- Reads:
  - If the partition key is known, search only the relevant shard.
  - If any subset of results is acceptable, query any shard.
  - If all matching records are needed and the partition key is unknown, query all shards and combine results.
- Costs:
  - Scatter/gather reads across all shards can be expensive.
  - Parallel shard queries are prone to tail latency amplification.
  - Adding shards increases storage capacity but may not increase query throughput if every shard must process every query.
- Source examples using local secondary indexes include MongoDB, Riak, Cassandra, Elasticsearch, SolrCloud, and VoltDB.

Interview hook: Local index writes are easy because write locality follows the primary record. Reads are hard because matches may be everywhere.

## 19. Global Secondary Indexes: Efficient Term Lookup, Harder Writes

Source pages: 294-295

- A global secondary index covers data from all shards.
- It cannot be stored on one node because that would become a bottleneck.
- Therefore the global index itself must be sharded.
- It may be sharded differently from the primary-key data.
- Used-car example:
  - IDs of all red cars from all primary shards appear under color:red.
  - The color index may be sharded by indexed value, such as colors a-r in one shard and s-z in another.
- This style is also called term-partitioned indexing.
- The term is the searchable indexed value.
- The indexed term is used as the partition key for the global index.
- Advantages:
  - A single-condition query, such as color = red, can read one shard to fetch the postings list.
- Remaining read cost:
  - Fetching full records still requires reading from shards responsible for those primary IDs.
- Multi-condition cost:
  - Conditions such as color and make may live on different index shards.
  - The system must intersect postings lists.
  - If postings lists are long, moving them over the network and intersecting them can be slow.
- Write cost:
  - One record can affect several index shards.
  - Every indexed term in a document may live on a different shard.
  - Keeping global indexes synchronized with primary data is harder.
  - A distributed transaction is one option for atomic updates.
- Source examples:
  - CockroachDB, TiDB, and YugabyteDB use global secondary indexes.
  - DynamoDB supports both local and global secondary indexes.
  - DynamoDB global indexes are updated asynchronously, so reads from them may be stale.
- Global indexes are useful when:
  - Read throughput is higher than write throughput.
  - Postings lists are not too long.

Interview hook: Global index reads narrow the search faster, but writes become multi-shard coordination problems.

## 20. Summary Logic: Every Shard Is Independent Until the Query or Write Crosses Boundaries

Source pages: 295-297

- Sharding is needed when a single machine can no longer store and process the dataset.
- The goal is even distribution of data and query load while avoiding hot spots.
- Rebalancing is required when nodes are added or removed.
- Key-range sharding:
  - Sorted keys.
  - Efficient range queries.
  - Hot-spot risk when nearby keys are frequently accessed.
  - Rebalance by splitting large ranges into subranges.
- Hash sharding:
  - Hash the key before placing it.
  - Better load distribution.
  - Destroys partition-key ordering.
  - Makes range queries inefficient.
  - Often uses a fixed number of shards with multiple shards per node.
  - Rebalancing usually moves entire shards, though splitting is possible.
- Composite-key pattern:
  - Use first part of the key as partition key.
  - Sort records within that shard by the rest of the key.
  - Enables efficient range queries within a single partition key.
- Request routing must find the correct shard and node.
- Coordination services often track shard-to-node assignment.
- Secondary indexes must also be sharded:
  - Local indexes: simple writes, scatter reads.
  - Global indexes: targeted index reads, harder writes and possible stale reads if asynchronous.
- Shards scale because they operate mostly independently.
- Operations writing several shards are problematic because partial success is possible.
- The chapter points forward to later chapters for the question of what happens if one shard write succeeds and another fails.

## Chapter-Level Memory Hooks

- Replication copies; sharding divides.
- A shard is a mini-database, often replicated for fault tolerance.
- Sharding helps data volume and write throughput more directly than read throughput.
- Partition key choice determines query locality.
- Key-range sharding preserves order but risks hot ranges.
- Hash sharding spreads keys but destroys key order.
- `hash % node_count` is easy but bad when node count changes.
- Fixed shard counts make node rebalancing easy until the original shard count becomes wrong.
- Hash-range sharding lets shard count adapt while using hash uniformity.
- Consistent hashing means minimal key movement under shard-count changes, not consistency of reads.
- Hot keys are workload problems, not just placement problems.
- Rebalancing is expensive and can amplify overload.
- Routing requires correct shard metadata.
- Local secondary indexes optimize writes; global secondary indexes optimize selected reads.
- Cross-shard operations are where the independence of shards stops being simple.

## Interview Perspective

- Define sharding separately from replication and explain why production systems often combine them.
- Explain when not to shard: if one machine or read replicas are enough, avoid the added complexity.
- Compare key-range, fixed-shard hash, hash-range, and consistent-hashing approaches.
- For key-range sharding, emphasize range scans and hot sequential writes.
- For hash sharding, emphasize uniform placement and loss of range locality.
- Explain why `hash(key) % number_of_nodes` causes massive data movement on resize.
- Describe rebalancing costs: moving data, rerouting traffic, continuing writes, and possible overload.
- Discuss hot keys separately from hot shards.
- Explain the three request-routing patterns and why coordination metadata must be fault-tolerant.
- Compare local and global secondary indexes using read/write tradeoffs.
- Name the common failure mode of application-managed secondary indexes: data and index drift after races or partial failures.
- For global indexes, mention multi-shard writes, postings-list intersection, and possible stale reads when index updates are asynchronous.

## Final Takeaways

- Sharding is the main mechanism for scaling beyond one node's storage or write capacity, but it is a complexity multiplier.
- The best shard key is workload-dependent: it must balance data, writes, reads, and range-query needs.
- Range-based schemes preserve locality but can concentrate load; hash-based schemes distribute load but scatter ranges.
- Rebalancing strategy matters as much as initial placement because clusters change.
- Secondary indexes are a core reason sharded relational/document databases are harder than sharded key-value stores.
- The clean mental model is: independent shards scale well; every cross-shard query, index update, transaction, or routing change reintroduces coordination.

Confidence: High
