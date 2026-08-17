# Chapter 04: Storage and Retrieval - Compressed Study Notes

Book: DDIA
Chapter: 4, Storage and Retrieval
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 139-175
Raw source used: books/DDIA/raw/chapter-04-chapter-4-storage-and-retrieval.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 4 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter framing and OLTP storage/indexing | 139-141 |
| Log-structured storage, SSTables, LSM-trees, Bloom filters, compaction | 142-148 |
| Embedded storage engines | 149 |
| B-trees, reliability, variants | 149-153 |
| Comparing B-trees and LSM-trees | 153-156 |
| Multicolumn and secondary indexes | 156-157 |
| Storing values within the index | 157 |
| Keeping everything in memory | 157-158 |
| Data storage for analytics and cloud data warehouses | 158-160 |
| Column-oriented storage, compression, sort order, writes | 160-166 |
| Query execution: compilation and vectorization | 166-167 |
| Materialized views and data cubes | 167-169 |
| Multidimensional and full-text indexes | 169-171 |
| Vector embeddings and vector indexes | 171-173 |
| Chapter summary | 174-175 |

## 1. Chapter Frame: Storage Engine Thinking

Source pages: 139-140

- A database must store data when given it and return it later when asked.
- The chapter shifts perspective from the application-facing model/query language to the database-internal storage and retrieval mechanisms.
- Application developers need storage-engine intuition because engine choice and tuning depend on workload.
- The central split is OLTP versus analytics:
  - OLTP storage engines optimize many small reads/writes with low latency.
  - Analytics storage engines optimize large scans and aggregations.
- Two OLTP families dominate the first half:
  - Log-structured engines that write immutable files.
  - B-tree style engines that update fixed-size pages in place.

Interview hook: Do not say "database index equals faster queries" without qualification. The chapter frames indexes as performance structures that improve selected reads but cost disk space and write work.

## 2. Storage and Indexing for OLTP

Source pages: 140-141

- The simple key-value store example appends key-value records to a file on every write.
- Updating a key appends a newer version rather than overwriting the old one; reads must find the latest occurrence.
- Append-only writes are fast because they are simple and sequential.
- The read path is poor without an index: scanning all records for a key is O(n).
- A log, in this chapter, means an append-only sequence of records on disk, not necessarily a human-readable application log.
- Real databases must also handle:
  - Concurrent writes.
  - Reclaiming old log space.
  - Recovery from partially written records.

Tradeoff:
- Appending is excellent for writes.
- Searching an unordered append-only file is terrible for large reads.
- Indexes speed selected reads but slow writes and consume space.

Interview hook: The first storage-engine tradeoff is read optimization versus write overhead. Indexes are derived structures, not primary truth.

## 3. Log-Structured Storage With an In-Memory Hash Index

Source pages: 142-143

- A simple improvement is to keep an in-memory hash map from key to byte offset of the latest value in the append-only log.
- Read path:
  - Look up key in memory.
  - Seek to the file offset.
  - Read the value.
  - If cached by the filesystem, the read may need no disk I/O.

Limitations:
- Old overwritten entries still occupy disk unless reclaimed.
- The hash map must be rebuilt on restart if it is not persisted.
- The hash table must fit in memory.
- On-disk hash maps are hard to make fast because of random I/O, growth costs, and collision handling.
- Range queries are inefficient because each key must be looked up separately.

Mental model: The hash map is a table of bookmarks into the log. It makes point lookup fast but does not organize keys by order.

## 4. SSTables: Sorted String Tables

Source pages: 143-144

- SSTables store key-value pairs sorted by key, with each key appearing once per file.
- Sorting enables a sparse index: keep only selected keys, such as the first key of each block.
- Lookup uses the sparse index to jump near the target key, then scans a small block.
- Blocks can be compressed:
  - Saves disk space.
  - Reduces I/O bandwidth.
  - Costs extra CPU.

Why SSTables improve on hash indexes:
- They do not require every key to be held in memory.
- They support ordered access better than hash maps.
- They allow block compression.

Limitation:
- Direct arbitrary inserts into a sorted file are expensive because maintaining sort order would require rewriting large parts of the file.

Interview hook: Sparse indexes work because sorted files let you infer where missing index keys must fall.

## 5. Constructing and Merging SSTables

Source pages: 144-146

- Writes first go into an in-memory ordered map called a memtable.
- The memtable can accept keys in any order and later emit them in sorted order.
- When the memtable reaches a threshold, it is written to disk as a new SSTable segment.
- Reads check:
  - Memtable first.
  - Newest segment next.
  - Older segments until key found or no segment remains.
- Background compaction merges segments and discards overwritten/deleted values.

Merging:
- Similar to mergesort.
- Read SSTables side by side.
- Emit keys in sorted order.
- If the same key appears in multiple inputs, keep the newest value.
- Uses little memory because the merge can stream through files.

Crash recovery:
- A separate append-only log records every memtable write.
- On crash, the log restores the memtable.
- Once a memtable is flushed to an SSTable, the corresponding log segment can be discarded.
- Immutable segment files simplify recovery; unfinished SSTables can be discarded.
- Checksums help detect incomplete or corrupted log entries.

Deletes:
- A delete is represented by a tombstone.
- During compaction, the tombstone removes older values.
- Once the tombstone has reached the oldest relevant segment, it can be dropped.

Terminology:
- Memtable: in-memory ordered write buffer.
- SSTable: sorted immutable on-disk segment.
- Segment: one immutable file in the log-structured set.
- Compaction: background merging that removes obsolete entries.
- Tombstone: deletion marker.
- LSM-tree: log-structured merge-tree, the design family based on merging sorted files.

Examples named in the source:
- RocksDB, Cassandra, ScyllaDB, HBase, Bigtable-inspired systems.
- SlateDB and Delta Lake are noted as examples suited to object storage for segment files.

Interview hook: LSM read path may touch multiple files; write path is fast because files are written sequentially and immutably.

## 6. Bloom Filters in LSM Storage

Source pages: 146-147

- LSM reads can be slow when a key is old or absent because many segments may need checking.
- A Bloom filter gives each segment a fast approximate membership test.
- Construction:
  - Hash every key to several bit positions.
  - Set those bits in a bitmap.
- Query:
  - Hash the requested key the same way.
  - If any required bit is 0, the key is definitely absent.
  - If all required bits are 1, the key may be present.

False positives:
- A false positive means the filter says "maybe present" even though the key is absent.
- False positives cause extra work but not wrong answers.
- The source gives a rule of thumb: about 10 bits per key for roughly 1% false positives, with about tenfold reduction for each additional 5 bits per key.

Interview hook: Bloom filters have no false negatives in this use case, so they can safely skip SSTables only when they say "not present."

## 7. Compaction Strategies

Source pages: 148

- Compaction strategy determines which SSTables are merged and when.

Size-tiered compaction:
- Merges newer/smaller SSTables into older/larger SSTables.
- Handles high write throughput because most data is rewritten only a few times in large sequential merges.
- Can require large temporary disk space because older SSTables can become very large.

Leveled compaction:
- Keeps SSTable sizes fixed and organizes them into levels.
- L0 has newest data; later levels are key-range partitioned.
- When a level exceeds its size limit, SSTables are merged into the next level.
- Uses less disk space and makes reads more efficient because fewer SSTables need checking.

Rule of thumb from the source:
- Mostly writes and few reads: size-tiered often performs better.
- Read-dominated workloads: leveled often performs better.
- Frequently rewriting a small key set while many keys are rarely written can favor leveled compaction.

Interview hook: Compaction is where LSM write speed is paid back. Ask which strategy matches read/write mix and disk-space constraints.

## 8. Embedded Storage Engines

Source pages: 149

- Embedded databases run as libraries in the application process rather than as network services.
- They usually read/write local files and expose function calls rather than network APIs.
- Examples in the source include RocksDB, SQLite, LMDB, DuckDB, and KuzuDB.
- Common uses:
  - Mobile apps storing local user data.
  - Backend systems with small single-machine data.
  - Multitenant designs where each tenant is small and isolated enough for a separate embedded database.
- The chapter's storage and retrieval methods apply to both embedded and client/server databases.

Interview hook: "Embedded" describes deployment/API shape, not a fundamentally different indexing theory.

## 9. B-Trees

Source pages: 149-151

- B-trees are the most widely used structure for key-based database records.
- They keep keys sorted, supporting key lookups and range queries.
- Unlike SSTables, B-trees use fixed-size pages/blocks and update pages in place.
- Page sizes named in the source:
  - Traditionally 4 KiB.
  - PostgreSQL uses 8 KiB.
  - MySQL uses 16 KiB by default.
- Pages refer to other pages by page number, like disk-based pointers.
- Lookup starts at the root, follows child page references based on key ranges, and eventually reaches a leaf page.
- Leaf pages contain values inline or references to pages containing the values.
- Branching factor is the number of child references in a page; in practice it is often several hundred.
- Insert:
  - Find target leaf page.
  - Add key if space exists.
  - If full, split the page and update parent boundaries.
  - Splits may cascade to the root.
- B-trees stay balanced: depth is O(log n).
- The source notes that most databases fit in three or four levels; a four-level tree with 4 KiB pages and branching factor 500 can store up to 250 TB.

Mental model: A B-tree is a sorted routing tree of disk pages. Each internal page narrows the key range until a leaf is reached.

Interview hook: B-tree performance comes from high branching factor, not binary-tree-like depth.

## 10. Making B-Trees Reliable

Source pages: 151-152

- B-trees overwrite pages in place, which creates crash-safety hazards.
- Page splits require multiple pages to be updated consistently.
- If a crash occurs midway, the tree may become inconsistent.
- If hardware cannot atomically write a full page, a torn page can occur.
- Common protection: write-ahead log (WAL).
  - Write every B-tree modification to the WAL before applying it to tree pages.
  - After crash, replay or use the WAL to restore consistency.
  - Filesystems use a related idea called journaling.
- Databases often buffer modified pages in memory for performance.
- Durability requires the WAL to be flushed to disk, such as with fsync, so recovery has the needed data.

Interview hook: WAL is needed not because B-trees are slow, but because in-place multi-page mutation needs a recovery trail.

## 11. B-Tree Variants

Source pages: 152

- Copy-on-write variant:
  - Modified pages are written elsewhere.
  - Parent pages are rewritten to point to new locations.
  - Useful for crash recovery and concurrency control.
- Key abbreviation:
  - Interior pages need enough key data to separate ranges, not necessarily full keys.
  - Smaller separators increase branching factor and reduce depth.
- Sequential leaf layout:
  - Some implementations try to place leaf pages in order on disk to speed range scans.
  - Maintaining this layout is hard as the tree grows.
- Sibling links:
  - Leaf pages may point to left/right siblings so ordered scans do not need to repeatedly traverse parent pages.

Interview hook: B-trees in real systems are families of techniques, not just the textbook insertion algorithm.

## 12. Comparing B-Trees and LSM-Trees

Source pages: 153-156

Core rule of thumb:
- LSM-trees often suit write-heavy applications.
- B-trees often give faster reads.
- Benchmark results depend on workload details, so test with the actual workload.
- Some storage engines mix ideas from both approaches.

Read performance:
- B-tree lookup reads one page per tree level, usually a small predictable number.
- LSM lookup may check several SSTables, though Bloom filters reduce unnecessary I/O.
- B-tree range queries are simple because the tree is ordered.
- LSM range queries must scan relevant ranges across segments and merge results.
- Bloom filters do not help range queries because hashing every possible key in a range is impractical.
- High write pressure in LSMs can create latency spikes when memtables fill and compaction cannot keep up.
- Some engines apply backpressure by suspending reads/writes until flushing catches up.
- Modern SSDs can serve many independent reads in parallel; storage engines must be designed to exploit that.

Sequential versus random writes:
- B-trees may perform scattered random writes when keys are spread across key space.
- LSMs write larger sequential segment files during flush and compaction.
- Sequential writes generally have higher throughput than random writes.
- This advantage is huge on HDDs and smaller but still present on SSDs.

SSD nuance:
- SSDs read/write pages but erase larger blocks.
- Random writes mix valid and invalid pages within erase blocks, increasing garbage collection.
- Garbage collection consumes bandwidth and contributes to flash wear.
- Sequential writes make whole blocks easier to reclaim.

Write amplification:
- LSM writes data to the durability log, then SSTables, then possibly again during compaction.
- B-trees write data to WAL and tree pages, sometimes whole pages for small changes.
- Write amplification is total bytes or I/O operations written compared with ideal append-only writing.
- Higher write amplification reduces write throughput under disk-bandwidth limits and increases SSD wear.
- For typical workloads, LSMs tend to have lower write amplification because they avoid rewriting whole pages and can compress SSTable chunks.
- Benchmarks must run long enough to include compaction effects; empty LSM-tree tests can be misleading.

Disk space:
- B-trees can fragment as pages are deleted or become unused.
- Free pages inside the file may not be returned to the OS easily.
- Databases may need background page movement, such as PostgreSQL vacuum.
- LSM compaction reduces fragmentation by rewriting data files.
- SSTables can compress blocks well.
- Size-tiered compaction can use more disk space, especially temporarily.
- Deleted records in LSMs may persist until tombstones move through compaction levels.
- Immutable SSTables are useful for snapshots because existing segment files can be retained without copying them.
- B-tree snapshots are harder when pages are overwritten in place.

Interview hook: There is no universal winner. Compare read latency, range queries, write throughput, compaction, write amplification, fragmentation, snapshots, and deletion guarantees.

## 13. Multicolumn and Secondary Indexes

Source pages: 156-157

- Primary-key indexes identify rows/documents/vertices.
- Secondary indexes search by fields other than the primary key.
- Secondary index values are often non-unique.
- Two ways to handle non-unique secondary index entries:
  - Store a list of matching row identifiers, like a postings list.
  - Make entries unique by appending a row identifier.
- Both B-tree and log-structured storage can implement indexes.

Interview hook: The big difference in secondary indexes is not the tree structure but duplicate index values.

## 14. Storing Values Within the Index

Source pages: 157

- Clustered index:
  - Stores the actual row/document/vertex directly inside the index structure.
  - Source examples: InnoDB primary key is clustered; SQL Server allows one clustered index per table.
- Heap file with index references:
  - Index stores references to the actual row, either primary key or disk location.
  - Heap file stores rows in no particular order.
  - Source example: Postgres uses a heap file approach.
- Covering index / included columns:
  - Stores selected extra columns inside the index.
  - Some queries can be answered from the index alone.
  - Faster for covered queries but uses more disk and slows writes due to duplication.

Update edge case:
- If a heap record is updated without changing the key and the new value fits, it may be overwritten in place.
- If it grows, it may move to a new heap location.
- Then all indexes must be updated or a forwarding pointer must remain at the old location.

Interview hook: "Index-only scan" is enabled by covering indexes, but the cost is duplicated data and write overhead.

## 15. Keeping Everything in Memory

Source pages: 157-158

- Disk-oriented structures exist because disks are durable and cheaper per GB than RAM but awkward for performance.
- As RAM gets cheaper, more datasets can live entirely in memory.
- Some in-memory stores are caches where data loss on restart is acceptable.
- Durable in-memory databases can use:
  - Special hardware.
  - Disk logs.
  - Periodic snapshots.
  - Replication to other machines.
- They are still called in-memory databases if reads are served from memory and disk is mainly for durability.
- Disk files also help with backups, inspection, and external analysis.
- Source examples:
  - Memcached for cache use.
  - VoltDB, SingleStore, Oracle TimesTen for relational in-memory databases.
  - RAMCloud as an in-memory key-value store with durability.
  - Redis and Couchbase with weak durability through asynchronous disk writes.
- Counterintuitive point: the speed advantage is not mainly avoiding disk reads, because OS cache may already keep disk blocks in memory. The advantage is avoiding the overhead of encoding memory structures into disk-oriented formats.
- In-memory systems can support data models that are hard with disk indexes, such as Redis data structures.

Interview hook: Do not explain in-memory databases only as "RAM is faster than disk." The chapter emphasizes avoiding disk-format management overhead.

## 16. Data Storage for Analytics

Source pages: 158-160

- Data warehouses often use relational schemas and SQL because SQL fits analytical exploration.
- OLTP databases and data warehouses can look similar at the interface but differ internally due to query patterns.
- Many products specialize in either transaction processing or analytics.
- HTAP systems combine transaction and analytical access through one SQL interface, but internally may use separate engines.

Cloud data warehouses:
- Source examples: Teradata, Vertica, SAP HANA, BigQuery, Redshift, Snowflake.
- Cloud-native warehouses can use object storage and serverless/scalable compute.
- They often decouple query computation from storage.
- They integrate with cloud ingestion and processing services.

Modern open analytics stack components:
- Query engine: parses SQL, optimizes plans, executes distributed tasks. Examples from source include Trino, Apache DataFusion, Presto, Spark, Flink.
- Storage format: encodes rows as bytes in files, often on object storage or distributed filesystems. Examples include Parquet, ORC, Lance, Nimble.
- Table format: defines which immutable files form a table plus schema and features such as inserts/deletes, time travel, GC, and transactions. Examples include Apache Iceberg and Delta.
- Data catalog: defines tables in a database, supports table lifecycle operations, and exposes metadata. Examples include Polaris, Unity Catalog, and Iceberg catalog.

Interview hook: Analytics architecture increasingly separates query engine, storage files, table metadata, and catalog governance.

## 17. Column-Oriented Storage

Source pages: 160-162

- Fact tables can be huge and wide, but analytical queries usually touch only a few columns.
- Row-oriented storage keeps all values of a row together, causing analytics queries to load and parse many unneeded attributes.
- Column-oriented storage keeps values from each column together.
- A query reads only the columns it uses, reducing disk I/O and parsing work.
- The rows must remain in the same order across columns so the nth value in each column belongs to the same row.
- Practical column stores break a table into blocks of thousands or millions of rows, then store columns separately inside each block.
- Blocks are often organized by timestamp ranges because many queries filter by date.
- Columnar storage appears in analytical databases, storage formats, in-memory analytics formats, and some time-series databases.
- The source notes that columnar storage can apply to nonrelational data too, such as Parquet's document-oriented support based on Dremel shredding/striping.

Interview hook: Column stores optimize "scan few columns across many rows"; row stores optimize "fetch/update whole records."

## 18. Column Compression

Source pages: 163-164

- Columnar storage often compresses well because values in a column repeat.
- Bitmap encoding:
  - For a column with n distinct values, create n bitmaps.
  - Each bitmap has one bit per row.
  - Bit is 1 if the row has that value.
- Sparse bitmaps can be run-length encoded by storing counts of repeated 0s or 1s.
- Roaring bitmaps choose between bitmap representations depending on which is compact.
- Bitmap operations support common warehouse predicates:
  - IN predicate: bitwise OR across value bitmaps.
  - AND predicate across columns: bitwise AND across matching row-position bitmaps.
- Bitmaps can also support graph-style queries where set intersections are needed.

Important distinction:
- Column-oriented databases are not the same as wide-column/column-family databases.
- Wide-column systems such as Bigtable, Accumulo, and HBase are row-oriented because they store a row's values together.

Interview hook: Bitmap indexes work well because all columns share row order; bit position k refers to the same row across columns.

## 19. Sort Order in Column Storage

Source pages: 164-165

- Row order in a column store can be chosen to improve query speed and compression.
- Columns must not be sorted independently; entire rows are sorted together, then stored by column.
- Sorting by commonly filtered columns, such as date_key, lets queries scan fewer rows.
- Secondary sort keys group useful subsets, such as products within a date.
- Sorting improves compression because repeated values form long runs.
- The first sort key benefits most; later sort keys are progressively less ordered and compress less well.

Interview hook: Column stores still have row order. The order is a physical optimization used for pruning and compression.

## 20. Writing to Column-Oriented Storage

Source pages: 165-166

- Data warehouse reads are often large aggregations; column layout, compression, and sorting help reads.
- Writes are often bulk imports through ETL.
- Inserting one row into the middle of a sorted compressed column store is inefficient because many compressed columns would need rewriting.
- Bulk writes amortize rewrite costs.
- Many systems use a log-structured approach:
  - Recent writes go to a row-oriented sorted in-memory store.
  - Accumulated writes are merged with column files and written as new immutable files.
  - Object storage fits because old files are immutable and new files are written in bulk.
- Queries combine disk column files with recent in-memory writes, hiding the difference from users.
- Source examples include Snowflake, Vertica, Apache Pinot, and Apache Druid.

Interview hook: Column stores are read-optimized, but they handle writes by batching and merging, not by rewriting one row at a time.

## 21. Query Execution: Compilation and Vectorization

Source pages: 166-167

- Analytical SQL is broken into a query plan with operators.
- Operators may run across multiple machines.
- Query planners optimize which operators to use, their order, and where they run.
- Large scans also require CPU efficiency, not just reduced disk I/O.

Query compilation:
- The engine generates code for the query.
- The generated code iterates through relevant columns, performs comparisons/calculations, and writes matches to output.
- It is compiled to machine code, often using LLVM.
- Similar in spirit to JIT compilation.

Vectorized processing:
- The query is interpreted but processes batches of column values rather than row by row.
- Predefined operators process vectors/batches and return batches, such as bitmaps.
- Example pattern: equality operators produce bitmaps, then bitwise AND combines predicates.

CPU-friendly traits:
- Sequential memory access to reduce cache misses.
- Tight inner loops to keep pipelines busy and reduce branch mispredictions.
- Parallelism through threads and SIMD.
- Operating directly on compressed data to avoid allocation and copying.

Interview hook: In analytics, query speed is often about CPU pipelines and memory access patterns, not only indexes.

## 22. Materialized Views and Data Cubes

Source pages: 167-169

- A materialized view stores actual query results on disk.
- A virtual view is only a query shortcut expanded at read time.
- Materialized views must be updated when underlying data changes.
- They increase write work but can improve repeated read queries.
- Materialized aggregates cache common aggregate results such as COUNT, SUM, AVG, MIN, and MAX.
- A data cube, or OLAP cube, stores aggregates grouped by dimensions.
- Example dimensions from source: date_key and product_sk; more general examples include date, product, store, promotion, and customer.

Advantages:
- Certain queries become very fast because results are effectively precomputed.

Limitations:
- Less flexible than raw data.
- If a desired question uses a dimension not represented in the cube, the cube cannot answer it.
- Warehouses often keep raw data and use cubes as performance boosts.

Interview hook: Materialization is another read/write tradeoff: precompute for faster reads, pay maintenance cost on writes.

## 23. Multidimensional Indexes

Source pages: 169-170

- B-trees and LSM-trees support range queries over one attribute.
- Concatenated indexes combine fields in a fixed order.
- A concatenated index can answer queries matching the leading field(s), like a phone book sorted by last name then first name.
- It is not useful when querying only a non-leading field, such as first name alone.
- Multidimensional indexes query several columns at once.
- Geospatial example:
  - Restaurants have latitude and longitude.
  - A map viewport requires a two-dimensional range query.
  - A concatenated latitude/longitude index cannot efficiently narrow both dimensions simultaneously.
- Approaches in the source:
  - Space-filling curve to map 2D to one number and use a B-tree.
  - Spatial indexes such as R-trees and Bkd-trees.
  - Regular grids of triangles, squares, or hexagons.
- Other uses:
  - Ecommerce RGB color search.
  - Weather observations by date and temperature.

Interview hook: Concatenated indexes are ordered by prefix; multidimensional indexes prune several axes at once.

## 24. Full-Text Search

Source pages: 170-171

- Full-text search finds documents by keywords appearing anywhere in text.
- Information retrieval also deals with language-specific tokenization, typos, grammatical forms, and synonyms, but those details are outside this chapter's scope.
- Core model:
  - Each term is a dimension.
  - A document has 1 in a term's dimension if it contains that term.
  - Searching for multiple terms is a multidimensional query.
- Inverted index:
  - Key: term.
  - Value: postings list of document IDs containing the term.
  - With sequential document IDs, postings can be represented as sparse bitmaps.
- Intersecting term queries can be answered with bitwise AND across postings bitmaps.
- Source examples:
  - Lucene stores term-to-postings mappings in SSTable-like sorted files and merges them in a log-structured way.
  - PostgreSQL GIN indexes use postings lists for full-text search and JSON document indexing.
- N-grams:
  - Index substrings of length n.
  - Trigrams of "hello" are hel, ell, llo.
  - Support substring and even regex search, but indexes are large.
- Edit distance:
  - Lucene can search words within a given edit distance.
  - It uses finite-state automata/trie-like structures and Levenshtein automata for efficient fuzzy search.

Interview hook: Full-text search is not just "LIKE with indexes"; it uses inverted indexes, postings lists, bitmaps, and sometimes automata.

## 25. Vector Embeddings and Vector Indexes

Source pages: 171-173

- Semantic search aims to match concepts and user intent beyond exact words, synonyms, and typos.
- Embedding models map documents or queries into vector embeddings: arrays of floating-point values representing locations in multidimensional space.
- Semantically similar inputs should be near each other in that space.
- The chapter distinguishes:
  - Vectorized processing: batches of bits/values processed efficiently.
  - Vector embeddings: floating-point coordinates in semantic space.
- Similarity can be measured with cosine similarity or Euclidean distance.
- Embeddings began with text models and expanded to video, audio, images, and multimodal models.
- Semantic search flow:
  - Generate query embedding.
  - Use vector index to find stored document embeddings closest to it.
- R-trees do not work well for high-dimensional vectors, so specialized vector indexes are used.

Vector index types:
- Flat index:
  - Stores vectors as-is.
  - Compares query vector with every vector.
  - Accurate but slow.
- IVF index:
  - Clusters vector space into partitions/centroids.
  - Compares fewer vectors.
  - Approximate because nearby vectors may fall into different partitions.
  - More probes increase accuracy but slow queries.
- HNSW index:
  - Maintains layered proximity graphs.
  - Search starts in sparse top layer, descends into denser layers, and follows edges toward closer vectors.
  - Approximate like IVF.
- Source examples:
  - Faiss has IVF and HNSW variations.
  - PostgreSQL pgvector supports both.

Interview hook: Vector indexes trade accuracy for speed; know flat as exact/slow, IVF and HNSW as approximate/faster.

## Chapter-Level Memory Hooks

- Append-only log: fastest simple write, terrible scan read without an index.
- Index: derived read accelerator with write and space cost.
- Hash index: fast point lookups, poor ranges, must fit memory.
- SSTable: sorted immutable file plus sparse index and compression.
- Memtable: in-memory sorted write buffer before SSTable flush.
- LSM-tree: append, flush sorted segments, compact in background.
- Tombstone: deletion marker that removes older values during compaction.
- Bloom filter: "definitely absent" or "maybe present"; false positives only.
- Size-tiered versus leveled: write-heavy large merges versus read-friendlier levels.
- B-tree: fixed pages, high branching factor, in-place updates.
- WAL: recovery trail before risky page overwrites.
- LSM versus B-tree: write throughput and sequential files versus predictable reads and range scans.
- Clustered, heap, covering: where the row data lives relative to the index.
- In-memory database: speed comes from memory-native structures, not merely avoiding disk reads.
- Column store: read only needed columns for large analytical scans.
- Bitmap compression: one bitmap per value, combine predicates with bit operations.
- Sort order in column stores: prune scans and improve compression.
- Query compilation/vectorization: CPU-efficient analytics execution.
- Materialized view/cube: precompute repeated reads, pay update cost.
- Multidimensional index: prune multiple axes at once.
- Inverted index: term to postings list for full-text search.
- Vector index: nearest-neighbor search over embeddings.

## Interview Perspective

- Start with workload: OLTP or analytics, point reads or range scans, write-heavy or read-heavy, data size, latency, deletion needs, and storage medium.
- For OLTP:
  - Explain append-only log, index need, then LSM and B-tree as two design philosophies.
  - Mention LSM advantages: sequential writes, immutable files, compaction, good write throughput.
  - Mention LSM risks: read checks across segments, compaction pressure, range-query cost, tombstone delay, write amplification.
  - Mention B-tree advantages: predictable point reads, efficient range scans, mature default for relational databases.
  - Mention B-tree risks: random writes, page splits, WAL complexity, fragmentation, write amplification.
- For secondary indexes:
  - Mention duplicate values and postings-list or appended-row-id strategies.
  - Discuss clustered versus heap versus covering indexes as read/write/storage tradeoffs.
- For analytics:
  - Explain why row stores waste work on wide fact-table scans.
  - Explain column stores, compression, sort order, bitmap operations, and bulk writes.
  - Include query execution as a CPU problem solved by compilation or vectorization.
- For search:
  - Use multidimensional indexes for multi-attribute ranges.
  - Use inverted indexes for terms and postings.
  - Use vector indexes for semantic similarity and be explicit about exact versus approximate search.

## Final Takeaways

- Storage engines are shaped by workload more than by the SQL or API surface.
- The first OLTP divide is immutable log-structured storage versus update-in-place page storage.
- Indexes are not free: every read improvement has write, space, and maintenance cost.
- LSM-trees convert random writes into sequential writes but pay through compaction and sometimes more complex reads.
- B-trees offer predictable reads and range scans but require careful crash recovery and can suffer random-write and fragmentation costs.
- Analytics engines use columnar layout, compression, sort order, and CPU-efficient execution because their queries scan many rows but few columns.
- Search workloads require specialized indexes: spatial/multidimensional, inverted, trigram/fuzzy, and vector indexes.
- The practical skill is matching workload symptoms to storage-engine behavior and tuning vocabulary.

Confidence: High
