# Chapter 04: Storage and Retrieval - Compressed Study Summary

Book: DDIA
Chapter: 4, Storage and Retrieval
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 139-175
Raw source used: books/DDIA/raw/chapter-04-chapter-4-storage-and-retrieval.md
Method: Concise chapter-level compression based only on the raw Chapter 4 markdown. Long verbatim copying was avoided.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| OLTP storage and indexing | 139-141 |
| Log-structured storage, SSTables, LSM-trees, Bloom filters, compaction | 142-148 |
| Embedded storage engines | 149 |
| B-trees and variants | 149-153 |
| B-trees versus LSM-trees | 153-156 |
| Secondary indexes and index value placement | 156-157 |
| In-memory databases | 157-158 |
| Analytics storage and cloud warehouse architecture | 158-160 |
| Column-oriented storage and analytics execution | 160-169 |
| Multidimensional, full-text, and vector indexes | 169-173 |
| Chapter summary | 174-175 |

## Compressed Chapter Summary

Chapter 4 explains how databases store data and retrieve it efficiently. The key lesson is that storage engines are workload-shaped: OLTP systems optimize many small low-latency reads and writes, while analytical systems optimize large scans and aggregations.

For OLTP, the simplest storage model is an append-only log. It writes quickly because each update is appended, but reads are slow without an index because a lookup may scan the whole file. An index is a derived structure that speeds selected reads at the cost of extra disk space and slower writes.

Log-structured storage improves append-only design with sorted immutable files. SSTables store key-value pairs sorted by key, use sparse indexes to jump to the right block, and compress blocks to reduce disk I/O. Writes first enter a sorted in-memory memtable; when large enough, the memtable is flushed as an SSTable segment. Reads check the memtable and newer segments before older ones. Background compaction merges segments, keeps the newest value for duplicate keys, and removes obsolete entries. Deletes use tombstones, which are removed after compaction has made them safe to drop.

LSM-trees are the broader family of storage engines built around immutable sorted segments and background merging. Bloom filters reduce unnecessary segment reads by quickly saying either "definitely absent" or "maybe present." False positives cause extra work but not incorrect results. Compaction strategy matters: size-tiered compaction favors high write throughput, while leveled compaction often improves reads and disk-space behavior.

B-trees are the classic update-in-place alternative. They organize sorted keys into fixed-size pages, route lookups from root page to leaf page, and rely on high branching factor to keep tree depth small. Inserts may split full pages and update parent pages, while updates overwrite pages in place. Because multi-page changes can fail midway, B-tree implementations commonly use a write-ahead log before modifying pages, enabling crash recovery. Variants include copy-on-write pages, abbreviated separator keys, sequential leaf layout attempts, and sibling links for scans.

The central OLTP comparison is not absolute. LSM-trees usually suit write-heavy workloads because they convert scattered updates into larger sequential writes, but reads may need multiple segment checks and compaction can create latency spikes. B-trees often provide faster, more predictable reads and efficient range scans, but they can perform random writes, require WAL recovery, and may fragment over time. Both suffer write amplification, and real benchmarks must match the actual workload and run long enough to include compaction or maintenance effects.

Secondary indexes extend lookup beyond primary keys. Since secondary index values are often non-unique, an index entry may contain a list of matching row IDs or be made unique by appending a row ID. Indexes may store the full row as a clustered index, point to rows in a heap file, or include selected extra columns as a covering index. Covering indexes can answer some queries without reading the heap, but they duplicate data and slow writes.

In-memory databases keep serving data from memory and use disk mainly for durability through logs, snapshots, replication, or special hardware. Their performance advantage is not simply "RAM is faster than disk"; the chapter emphasizes avoiding the overhead of encoding in-memory structures into disk-oriented formats. They can also support data models that are awkward for disk-based indexes.

Analytics storage looks different because warehouse queries often scan many rows but only a few columns. Column-oriented storage stores values by column rather than by row, so queries read only the columns they need. The rows must remain aligned across columns. Column stores typically divide tables into blocks, often by timestamp range, and store columns separately inside each block.

Column compression is powerful because values in one column often repeat. Bitmap encoding creates one bitmap per distinct value and uses bit operations for predicates such as IN and AND. Run-length encoding and roaring bitmaps make sparse or repetitive bitmaps compact. Sorting rows by commonly filtered columns improves scan pruning and compression, especially for the first sort key. Writes are often batched: recent writes go to an in-memory row-oriented store, then are merged into immutable column files in bulk.

Analytical query execution must also minimize CPU cost. Query compilation generates machine code for a query, while vectorized processing interprets operators over batches of column values. Both approaches benefit from sequential memory access, tight loops, SIMD/thread parallelism, and operating on compressed data where possible.

Materialized views and data cubes precompute repeated query results. They improve read performance for known aggregate patterns but require maintenance when base data changes and are less flexible than raw data. Data warehouses therefore often keep raw data and use cubes as targeted accelerators.

For more advanced search, single-attribute B-tree/LSM range indexes are not enough. Concatenated indexes help only when queries match the leading fields. Multidimensional indexes, such as spatial indexes, can prune several dimensions at once. Full-text search uses inverted indexes from term to postings list, with bitmaps enabling efficient intersections. Trigram indexes support substring and regex-style search but can be large; fuzzy search can use automata for edit-distance matching. Semantic search uses vector embeddings and vector indexes. Flat vector indexes are exact but slow, while IVF and HNSW reduce comparisons and trade exactness for speed.

## Chapter-Level Memory Hooks

- Append-only log: simple fast write, slow scan read.
- Index: derived read accelerator with write and storage cost.
- SSTable: sorted immutable segment with sparse index and compression.
- Memtable: in-memory sorted buffer before SSTable flush.
- LSM-tree: write to memory/log, flush to sorted files, compact later.
- Bloom filter: no false negatives for segment skipping; false positives only add work.
- Tombstone: delete marker cleaned up through compaction.
- B-tree: fixed pages, high branching factor, in-place updates.
- WAL: crash-recovery record before page mutation.
- LSM versus B-tree: sequential write throughput versus predictable read/range behavior.
- Clustered/heap/covering: different placements of row data relative to indexes.
- Column store: scan few columns across many rows.
- Bitmap compression: predicates become fast bit operations.
- Query compilation/vectorization: analytics performance depends heavily on CPU efficiency.
- Materialized cube: faster repeated aggregates, less query flexibility.
- Inverted index: term to postings list.
- Vector index: nearest-neighbor search over embeddings.

## Interview Perspective

- First classify the workload: OLTP or analytics, point lookups or range scans, read-heavy or write-heavy, update/delete patterns, and latency sensitivity.
- Explain indexes as tradeoffs, not free acceleration.
- Compare LSM and B-tree using read path, write path, compaction, WAL, write amplification, disk space, snapshots, and deletion behavior.
- For warehouses, explain why columnar storage avoids reading unused columns and why compression plus vectorized execution matter.
- For search, map query type to index type: concatenated for ordered prefixes, multidimensional for multi-axis ranges, inverted for terms, vector for semantic similarity.

## Final Takeaways

- Storage engine internals determine performance behavior under real workloads.
- OLTP engines generally choose between log-structured immutable files and B-tree update-in-place pages.
- LSM-trees favor write throughput but require compaction and careful read optimization.
- B-trees favor predictable reads and range scans but need WAL recovery and can fragment.
- Analytical engines use columnar layout, compression, sort order, and CPU-efficient execution for large scans.
- Specialized indexes exist because no single index structure serves every query shape.

Confidence: High
