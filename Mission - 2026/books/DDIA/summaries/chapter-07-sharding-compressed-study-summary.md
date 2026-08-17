# Chapter 07: Sharding - Compressed Study Summary

Book: DDIA
Chapter: 7, Sharding
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 275-297
Raw source used: books/DDIA/raw/chapter-07-chapter-7-sharding.md
Method: Concise chapter-level compression based only on the raw Chapter 7 markdown. Long verbatim copying was avoided.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Sharding versus replication and terminology | 275-277 |
| Pros, cons, and multitenancy | 277-279 |
| Key-value sharding goals and key-range sharding | 279-282 |
| Hash sharding, fixed shards, hash ranges, and consistent hashing | 282-287 |
| Hot spots and automatic/manual rebalancing | 287-289 |
| Request routing | 289-292 |
| Secondary indexes: local and global | 292-295 |
| Chapter summary | 295-297 |

## Compressed Chapter Summary

Chapter 7 studies sharding: splitting a dataset into smaller parts and storing different parts on different nodes. Replication copies the same data onto multiple nodes; sharding divides the data. A record normally belongs to exactly one shard, although that shard may be replicated for fault tolerance. With single-leader replication, each shard can have its own leader and followers, and a node may lead some shards while following others.

The term varies by system: Kafka uses partition, CockroachDB range, HBase and TiDB region, Couchbase vBucket, Riak vnode, Cassandra token-range, and Bigtable/YugabyteDB/ScyllaDB tablet. Some systems distinguish same-machine partitioning from cross-machine sharding; others use the words interchangeably. This chapter's partitioning is not a network partition.

The main reason to shard is scalability when data volume or write throughput exceeds one node. If the problem is read throughput, replication may be enough. Sharding enables horizontal scale-out by assigning different shards to different machines, but it adds complexity. You must choose a partition key, route requests to the correct shard, rebalance as nodes change, and handle queries or writes that cross shard boundaries. Sharding is often natural for key-value workloads and harder for relational workloads with secondary indexes and joins. Multi-shard writes may require distributed transactions, which are slower than single-node transactions and can become bottlenecks.

Sharding is also useful for multitenancy. A SaaS system may assign each tenant to a shard, or group small tenants into shared shards. This can improve resource isolation, permission isolation, fault isolation through cell-based architecture, per-tenant backup/restore, regulatory deletion/export, data residence, and gradual schema rollout. The difficulties are tenant size variation, overhead from many small tenants, moving tenants as they grow, and cross-tenant features that require cross-shard joins.

The goal of key-value sharding is to spread data and query load evenly. Skew occurs when some shards have much more data or traffic than others. A hot shard is an overloaded shard; a hot key is one partition key with unusually high load. The sharding algorithm maps a partition key to a shard and must allow rebalancing.

Key-range sharding assigns contiguous ranges of partition keys to shards. It preserves sorted order, making range scans efficient and enabling the key to behave like a concatenated index. The tradeoff is hot ranges: if keys are timestamps, current writes concentrate in the current time-range shard. Prefixing timestamps with sensor IDs can spread write load, but querying many sensors over a time range then requires many range queries. Key-range systems rebalance by splitting large or hot ranges and merging small adjacent ranges. Splitting is expensive because data must be rewritten, and the shard that needs splitting may already be overloaded.

Hash sharding hashes the partition key before assigning it to a shard. A good deterministic hash distributes skewed input keys across a uniform range; it does not need cryptographic strength. Built-in language hash functions may be unsuitable if different processes produce different hash values. A naive `hash(key) % number_of_nodes` mapping distributes keys but causes most keys to move when the node count changes, so it is poor for rebalancing.

A common alternative is to create a fixed number of shards much larger than the node count. Keys map to shard IDs, and the system separately maps shards to nodes. Adding or removing a node moves whole shards, not individual keys, while the key-to-shard mapping stays stable. This works if the initial shard count estimate is good. If the shard count is too small or too large for future scale, resharding can be expensive, may require extra disk space, and may be difficult without downtime.

Hash-range sharding lets the number of shards adapt. Each shard owns a contiguous range of hash values rather than original keys. This keeps hash uniformity while allowing hot or large hash ranges to split. The tradeoff is that range queries over the partition key become inefficient because adjacent logical keys are scattered. However, composite keys can preserve efficient range queries within one partition key by using the first key component for placement and sorting by later components inside the shard.

Consistent hashing maps keys to shards so that keys are roughly balanced and membership changes move as few keys as possible. In this context, "consistent" means placement stability, not replica consistency or ACID consistency. The chapter mentions Cassandra/ScyllaDB-style range approaches as well as rendezvous hashing and jump consistent hashing.

Uniform hashing does not eliminate workload skew. A celebrity user or viral post can create very high activity on one key. Range-based schemes can place a hot key in its own shard or on a dedicated machine. Application code can split a hot key by adding random prefixes or suffixes, spreading writes across many derived keys, but reads must then fetch and combine those keys. This only reduces write concentration, requires bookkeeping, and should be reserved for known hot keys. Hotness also changes over time and may affect reads and writes differently.

Rebalancing may be automatic, manual, or suggested by the system and approved by an administrator. Automation reduces routine work and can support autoscaling, but rebalancing is expensive: it moves data, reroutes requests, consumes network and node capacity, and must coexist with ongoing writes. Combined with automatic failure detection, it can worsen overload: a slow overloaded node may be treated as failed, triggering movement that adds more load and risks cascading failure. Human approval is slower but can avoid surprises and support planned rebalancing before known traffic surges.

Request routing is the problem of finding the node that owns the shard for a key. It differs from ordinary stateless service discovery because any node cannot handle any key. The chapter describes three patterns: clients contact any node and that node forwards if necessary; clients use a shard-aware routing tier; or clients themselves know shard assignments. The system must decide shard placement, keep placement metadata fault-tolerant, avoid split brain, propagate assignment changes, and handle in-flight requests during shard movement. Many systems use ZooKeeper, etcd, config servers, or built-in Raft coordination for authoritative shard metadata; Riak uses gossip, which is weaker and can allow disagreement about assignments.

Secondary indexes complicate sharding because searches by non-partition attributes do not naturally identify one shard. Local secondary indexes keep each shard's index for only its own records. Writes are simple because only the record's shard is updated, but reads that need all matching records must query every shard and combine results. This creates scatter/gather cost, tail latency amplification, and limited query-throughput scaling. Application-built indexes in key-value stores are risky because races and partial failures can make indexes diverge from source data.

Global secondary indexes cover records from all shards and are themselves sharded by indexed value. A single-condition lookup, such as color = red, can read one index shard to get a postings list. Fetching full records may still touch many primary shards. Multi-condition queries may require intersecting postings lists from different shards, which can be slow when lists are long. Writes are more complex because one record may update several index shards. Distributed transactions can keep primary records and indexes atomic; asynchronous global-index updates can make index reads stale. Global indexes are most attractive when reads dominate writes and postings lists are not too long.

## Chapter-Level Memory Hooks

- Replication copies data; sharding divides data.
- Sharding is for data volume and write throughput; read replicas may solve read throughput without sharding.
- The partition key is the first design decision that determines locality, routing, and future pain.
- Key-range sharding: range scans are good, sequential hot writes are dangerous.
- Hash sharding: distribution is good, range locality is lost.
- `hash % node_count` is a resizing trap.
- Fixed shards move whole shards between nodes but depend on a good initial shard count.
- Hash ranges adapt by splitting ranges of hash values.
- Consistent hashing means fewer keys move when membership changes.
- Hot keys require workload-aware handling; uniform hashes are not enough.
- Rebalancing is a production event, not just an algorithm.
- Routing metadata must be correct, current, and protected from split brain.
- Local indexes make writes local and reads scattered.
- Global indexes make index reads targeted and writes distributed.

## Interview Perspective

- Start with the distinction: replication increases copies; sharding partitions ownership.
- Explain why sharding should be delayed until scale demands it: it complicates routing, indexes, joins, transactions, and operations.
- Compare key-range and hash-based sharding with concrete examples such as timestamp sensor data and tenant IDs.
- Call out the `hash(key) % N` problem when nodes are added or removed.
- Discuss fixed shard count versus adaptive splitting as an operational tradeoff.
- Separate hot shards from hot keys; consistent hashing does not solve celebrity-key traffic.
- For request routing, mention forwarding nodes, routing tiers, shard-aware clients, and the need for consensus-backed placement metadata.
- For indexes, use the rule: local index means simple write and scatter read; global index means targeted index read and complicated write.

## Final Takeaways

- Sharding scales a database by making shards mostly independent, but every cross-shard operation reintroduces coordination.
- There is no universally best shard key or sharding scheme; the right choice depends on data distribution, read patterns, write patterns, range queries, and growth expectations.
- Rebalancing and request routing are central parts of sharding design, not afterthoughts.
- Secondary indexes are where sharded key-value simplicity gives way to distributed query complexity.

Confidence: High
