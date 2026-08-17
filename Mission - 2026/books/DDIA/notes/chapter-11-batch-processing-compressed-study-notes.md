# DDIA Chapter 11: Batch Processing - Compressed Study Notes

Book: DDIA  
Chapter: 11 - Batch Processing  
Source PDF: books/DDIA/DDIA.pdf  
Source PDF/pages: PDF pages 475-506; printed pages 451-481  
Method: Compressed section-by-section from `books/DDIA/raw/chapter-11-chapter-11-batch-processing.md` only. Long passages are paraphrased; terminology, examples, tradeoffs, limitations, and interview hooks are preserved.

## Source Map

| Section | Source pages |
| --- | --- |
| Chapter framing and batch definition | PDF 475-477; pp. 451-453 |
| Batch Processing with Unix Tools | PDF 478-481; pp. 454-457 |
| Batch Processing in Distributed Systems | PDF 481-490; pp. 457-466 |
| Batch Processing Models | PDF 490-500; pp. 466-476 |
| Batch Use Cases | PDF 500-505; pp. 476-481 |
| Chapter Summary | PDF 505-506; pp. 481-482 |

## 1. What Batch Processing Is

Source: PDF 475-477; pp. 451-453

- Online systems handle requests and responses quickly: web pages, APIs, databases, caches, and search indexes. Their key metric is response time, and they need high availability.
- Batch processing handles larger, offline computations: training AI models, transforming large datasets, or computing analytics over very large data.
- A batch job reads bounded, read-only input and generates output from scratch. It usually does not mutate data transactionally.
- Because output is derived data, bad output can be deleted, code can be fixed, and the job can be rerun.
- This gives batch processing a strong safety property: mistakes in job logic are often recoverable because previous outputs can be retained and switched back to.
- The chapter calls this recovery from buggy code "human fault tolerance."
- Batch jobs also support monitoring jobs: the same input files can be reused to validate metrics or compare a new output with an earlier run.
- Batch frameworks can be more resource-efficient than trying to process the same bulk workloads through OLTP databases or application servers.
- Limitations:
  - Downstream jobs often cannot consume output until the whole upstream job completes.
  - A tiny input change may require reprocessing the whole dataset.
  - Jobs may take minutes, hours, or days.
- Main performance metric: throughput, not low latency.
- Some systems restart the whole job after failure; stronger systems retry smaller failed tasks.
- Stream processing is presented as the alternative: it keeps running and processes changes shortly after they happen.
- MapReduce influenced modern batch processing, but the chapter says it is now largely obsolete in practice; modern systems more often use Spark, Flink, data warehouse query engines, dataflow APIs, SQL, DataFrame APIs, and mature workflow schedulers.
- Storage has shifted from DFS systems such as HDFS, GlusterFS, and CephFS toward object stores such as S3.

Interview hook: Define batch processing as bounded input plus derived output, optimized for throughput and rerunability, not interactive latency.

## 2. Batch Processing with Unix Tools

Source: PDF 478-481; pp. 454-457

### Web Log Example

- The chapter starts with NGINX access logs. Each log line contains fields such as client IP, timestamp, request path, status, response size, referrer, and user agent.
- Log parsing is not just a toy example: the chapter connects it to ad pipelines, payment processing, MapReduce adoption, and the big data movement.

### Simple Log Analysis

- Goal: find the five most popular pages from access logs.
- Unix pipeline shape:
  - Read the log.
  - Extract the requested URL field.
  - Sort URLs so equal URLs become adjacent.
  - Count adjacent duplicates.
  - Sort by count descending.
  - Keep the top five.
- Important primitive: sorting enables counting without keeping all counts in memory.
- Unix tools such as awk, sed, grep, sort, uniq, and xargs can perform many analyses quickly and flexibly.
- Variant examples from the chapter:
  - Exclude CSS paths.
  - Count client IP addresses instead of URLs.

### Chain of Commands vs Custom Program

- The equivalent Python example uses an in-memory hash table from URL to count.
- The custom program is readable but uses a different execution strategy from the Unix pipeline.

### Sorting vs In-Memory Aggregation

- Hash-table aggregation works when the working set fits in memory.
- Working set is based on distinct URLs, not total log lines. One million requests for one URL still require one key plus a counter.
- Sorting is better when the working set is larger than memory because it can spill sorted segments to disk and merge them efficiently.
- Mergesort has sequential access patterns, which suit disks.
- GNU sort can spill to disk and parallelize across CPU cores, so a simple pipeline can scale surprisingly far.
- Limitation: Unix tools run on one machine. If data exceeds local memory or disk, distributed batch processing is needed.

Interview hook: The Unix pipeline illustrates the core idea behind MapReduce: map/extract, sort/shuffle, reduce/count.

## 3. Batch Processing in Distributed Systems

Source: PDF 481-490; pp. 457-466

### Distributed Operating System Analogy

- A single-machine Unix batch job uses storage, a scheduler, and piped programs.
- Distributed batch frameworks have analogous pieces:
  - distributed storage,
  - job scheduling/orchestration,
  - programs/operators that exchange data through storage or communication channels.
- The chapter says distributed batch frameworks can be thought of as distributed operating systems.

### Distributed Filesystems

Source: PDF 482-484; pp. 458-460

- Local filesystems have layers: block drivers, page cache, filesystem metadata, and the VFS interface.
- Distributed filesystems are similar, but files are broken into large blocks spread across machines.
- Block sizes are much larger than local filesystem blocks:
  - HDFS default: 128 MB.
  - JuiceFS and many object stores: 4 MB.
  - ext4 example: 4,096 bytes.
- Larger blocks reduce metadata overhead and make seeking overhead smaller relative to read time.
- DFS blocks are read by network calls to a data node that stores the block.
- Data nodes expose APIs for remote block reads/writes and store blocks as files on their local filesystems.
- DFS caching parallels local page caching because reads and writes go through each data node's OS page cache. Some systems add client-side or local disk caching.
- Metadata services track file locations, permissions, and directory structures:
  - HDFS NameNode.
  - DeepSeek 3FS metadata service backed by a key-value store such as FoundationDB.
- DFS protocols act like pluggable interfaces for batch systems. S3-compatible APIs are widely adopted by storage systems.
- Some DFSs expose POSIX-like filesystems through FUSE or NFS.

### Distributed Filesystems vs Network Storage

Source: PDF 483-484; pp. 459-460

- DFSs are based on shared-nothing architecture: ordinary machines connected by a datacenter network.
- NAS/SAN systems are shared-disk approaches, often using centralized storage appliances and special network infrastructure.
- Commodity hardware is cheaper but fails more often, so DFSs replicate or erasure-code blocks.
- Replication provides fault tolerance and gives schedulers more choices for running tasks near input data.
- Erasure coding, such as Reed-Solomon, can recover lost data with lower storage overhead than full replication.
- Distributed filesystem redundancy is similar in spirit to RAID, but it operates across machines over the network.

Interview hook: Explain why large DFS blocks are useful: less metadata, better throughput, and lower relative seek overhead for huge datasets.

### Object Stores

Source: PDF 484-485; pp. 460-461

- Object stores such as S3, Google Cloud Storage, Azure Blob Storage, and OpenStack Swift are now common alternatives to DFSs for batch jobs.
- The boundary between DFSs and object stores is blurry because some systems expose both filesystem and object APIs, and FUSE can make object storage look like a filesystem.
- But APIs, performance, and consistency guarantees differ, so compatibility must be checked carefully.
- Object naming:
  - URL contains bucket plus object key.
  - Bucket names are globally unique.
  - Keys are unique within a bucket.
- Objects are read with get and written with put.
- Most objects are immutable once written; updating requires rewriting the whole object. The chapter notes exceptions for append support in Azure Blob Storage and S3 Express One Zone.
- Object stores do not truly have directories. Slashes are part of the key.
- Prefix listing differs from directory listing:
  - It behaves like recursive listing over all objects with a prefix.
  - Empty directories do not naturally exist; zero-byte objects are often used as markers.
- Missing or weak filesystem operations:
  - hard links,
  - symbolic links,
  - file locking,
  - atomic rename.
- Rename is usually copy plus delete; renaming a "directory" means renaming each object whose key has that prefix.
- Object stores and DFSs are optimized for large objects and larger, less frequent reads compared with key-value stores optimized for small values and low-latency reads/writes.
- HDFS can run compute on machines holding data replicas, saving network transfer. Object stores usually separate compute and storage; this can use more bandwidth but allows CPU/memory and storage to scale independently.

Interview hook: Object store paths look hierarchical, but the directory structure is a key-prefix convention, not a real filesystem tree.

### Distributed Job Orchestration

Source: PDF 485-488; pp. 461-464

- A distributed job orchestrator fills the role played by the OS kernel on one machine: running tasks, scheduling resources, isolating processes, and tracking status.
- Job requests include metadata such as:
  - task count,
  - memory/CPU/disk requirements,
  - job identifier,
  - credentials,
  - input/output parameters,
  - required hardware such as GPUs,
  - location of executable code.
- Common orchestrators: Kubernetes and Hadoop YARN.
- Core components:
  - Task executors: node-level daemons such as kubelet or YARN NodeManager. They start tasks, send heartbeats, track status, retrieve code, and monitor task completion/failure.
  - Resource manager: global cluster state, including hardware, task statuses, network location, and node status. This centralized role may become a scalability or availability bottleneck.
  - Scheduler: receives job requests and assigns tasks to nodes based on job requirements and cluster state.
- Isolation:
  - YARN and Kubernetes use Linux cgroups for security and performance isolation.
  - This prevents unauthorized access and reduces interference between tasks.
- Application-specific schedulers may cooperate with the central scheduler when jobs need domain-specific decisions. YARN calls these ApplicationMasters; Kubernetes calls them operators.

### Resource Allocation

Source: PDF 487-488; pp. 463-464

- Scheduling balances fairness and efficiency.
- Example: a five-node cluster with 160 CPU cores receives two jobs asking for 100 cores each.
- Possible strategies:
  - split resources, run 80 tasks for each, and start remaining tasks as capacity frees;
  - gang schedule one job first, then the other;
  - hold back some cores in anticipation of future work.
- Tradeoffs:
  - Gang scheduling can leave nodes idle and even deadlock if resources are reserved but unavailable.
  - Waiting for all required resources can cause starvation.
  - Preempting tasks improves fairness but reduces efficiency because killed work must be restarted.
- Optimal scheduling for many jobs is intractable; the chapter states the allocation problem is NP-hard.
- Practical schedulers use heuristics such as FIFO, dominant resource fairness, priority queues, quota/capacity-based scheduling, and bin-packing algorithms.

Interview hook: Scheduler design is about tradeoffs; there is no universally optimal policy under competing resource demands.

### Scheduling Workflows

Source: PDF 488-489; pp. 464-465

- Batch pipelines often form a workflow or DAG: the output of one job becomes the input of one or more other jobs.
- Batch workflow is different from durable execution workflow:
  - Batch workflow: sequence of jobs over data files, usually no RPC to external services.
  - Durable execution workflow: sequence of steps often involving RPCs and smaller per-request data.
- Reasons for multi-job workflows:
  - One output feeds multiple teams or consumers.
  - Data moves between tools, such as Spark to HDFS to Trino to S3.
  - Pipeline stages may need different sharding keys.
- Direct task-to-task transfer supports backpressure, as with Unix pipes, and engines such as Spark/Flink can pass task output directly to downstream tasks.
- More commonly, one job writes output to DFS/object storage and another reads it later. This decouples execution times.
- Workflow schedulers wait for all producing jobs to complete successfully before starting dependent consumers.
- Job schedulers inside systems such as YARN or Spark manage one job; workflow schedulers such as Airflow, Dagster, and Prefect manage job dependencies across many jobs.
- Workflows of 50 to 100 jobs are common, and large organizations may have many teams reading one another's outputs.

Interview hook: Distinguish job scheduler from workflow scheduler: task placement inside one job vs dependency management across jobs.

### Handling Faults

Source: PDF 489-490; pp. 465-466

- Long-running parallel jobs are likely to experience task failures from hardware faults, network issues, or scheduler preemption.
- Preemption is common with lower-priority or cheaper compute capacity:
  - Amazon EC2 spot instances,
  - Azure spot virtual machines,
  - Google Cloud preemptible instances.
- Batch jobs suit low-priority capacity because many are not time-sensitive, but preemptions can be more frequent than hardware faults.
- Since output is regenerated from input, failed partial output can be deleted and a task retried elsewhere.
- Retrying one failed task is better than rerunning the whole job.
- MapReduce and successors keep parallel tasks independent so retry granularity can be one task.
- Intermediate output is harder:
  - MapReduce writes intermediate data to DFS and waits for successful completion before downstream tasks read it. This is robust but I/O-heavy.
  - Spark keeps intermediate data in memory or spills to local disk, and tracks lineage so lost data can be recomputed.
  - Flink uses periodic checkpoint snapshots.

Interview hook: Fault tolerance depends on side-effect-free tasks, deterministic recomputation, and task-level retry.

## 4. Batch Processing Models

Source: PDF 490-500; pp. 466-476

### MapReduce

Source: PDF 490-492; pp. 466-468

- MapReduce resembles the Unix log pipeline:
  1. Read input files and split them into records.
  2. Mapper extracts key-value pairs from each record.
  3. Framework sorts key-value pairs by key.
  4. Reducer iterates over sorted values for each key.
- Input may live in HDFS or object storage and use formats such as Parquet or Avro.
- You write mapper and reducer callbacks; parsing and sorting are handled by the framework.
- Mapper:
  - called once per input record;
  - emits zero or more key-value pairs;
  - keeps no state between records;
  - can run in many parallel tasks.
- Reducer:
  - receives all values for one key;
  - can produce output records;
  - reducers for different keys can run in parallel.
- If another sorting stage is needed, write another MapReduce job.
- The mapper prepares data for sorting; the reducer processes sorted groups.

### MapReduce and Functional Programming

Source: PDF 492; p. 468

- MapReduce comes from functional programming ideas: map and reduce/fold over lists.
- Avoiding mutable state enables parallelism because mapper/reducer calls depend only on explicit input.
- Failed tasks can be rerun with the same input on another node.
- Many SQL-like operations can be implemented on MapReduce.
- Limitations:
  - Raw MapReduce APIs are laborious for complex jobs.
  - Joins must often be implemented manually.
  - MapReduce is slow relative to modern batch processors.
  - File-based I/O prevents downstream jobs from processing upstream output before the upstream job completes.

Interview hook: MapReduce's strength is simple parallelizable semantics; its weakness is rigid stages and heavy intermediate I/O.

### Dataflow Engines

Source: PDF 492-493; pp. 468-469

- Spark and Flink were developed to fix MapReduce problems.
- Dataflow engines handle an entire workflow as one job rather than independent MapReduce subjobs.
- They model data moving through processing stages.
- They support low-level record APIs plus higher-level operators such as join and group by.
- Work is parallelized by sharding input and passing task output to downstream tasks, possibly over the network.
- Operators do not have to alternate map/reduce roles; they can be assembled flexibly.
- Relational-style building blocks include joins, grouping by key, filtering, and aggregation.
- Advantages over MapReduce:
  - Sort only where needed.
  - Fuse consecutive operators that do not change sharding.
  - Scheduler sees dependencies and can optimize locality.
  - Intermediate state can often stay in memory or local disk.
  - Downstream operators can start as soon as input is ready.
  - Existing processes can be reused, reducing startup overhead.
- Result: same computations as MapReduce, usually significantly faster.

Interview hook: Dataflow engines optimize across the whole computation graph; MapReduce only sees rigid job boundaries.

### Shuffling Data

Source: PDF 493-495; pp. 469-471

- Shuffle is the distributed sorting operation that makes joins and aggregations scalable.
- Warning from the chapter: shuffle does not mean randomization here. It produces a sorted/grouped arrangement.
- Inputs and outputs are sharded because datasets may be petabytes in size.
- In MapReduce:
  - each input shard gets a mapper;
  - reducer count is chosen by the job author;
  - each mapper writes one local output file per reducer;
  - a hash of the key determines reducer assignment;
  - mapper output files are sorted;
  - reducers fetch their partition from every mapper;
  - reducers merge sorted files and call reduce once per key.
- Reducer outputs are written as output shards to DFS/object storage.
- Modern engines and warehouses optimize shuffle with in-memory techniques and external sorting services; BigQuery is given as an example.

Interview hook: Shuffle is the hidden engine behind distributed group by, join, and aggregation.

### Joins and Grouping

Source: PDF 495-497; pp. 471-473

- Example: join website activity events with user profiles to analyze whether certain pages are more popular among age groups.
- Activity events are a fact table; user database is a dimension table.
- User ID is the join key.
- One mapper emits page views keyed by user ID.
- Another mapper emits user profile data keyed by user ID.
- Shuffle guarantees all records for the same user ID reach the same reducer.
- Secondary sort can arrange user profile first, followed by activity events in timestamp order.
- Reducer keeps one user record in memory and iterates through that user's activity events, emitting joined results.
- This is a sort-merge join: sorted mapper output is merged by reducers.
- A following job can group by URL and aggregate views by age group.

Interview hook: Sort-merge join scales because each reducer only needs the records for one key group, not a full table in memory.

### Query Languages

Source: PDF 497-498; pp. 473-474

- Once petabyte-scale operation became mature, focus moved to usability.
- MapReduce, dataflow engines, and cloud warehouses increasingly use SQL as the common batch language.
- SQL fits because data warehouses, analytics, ETL tools, developers, and analysts already use it.
- Benefits:
  - less code than handwritten MapReduce;
  - interactive query exploration;
  - access for analysts, product, sales, finance, and other teams;
  - query engines can optimize execution plans.
- Query engines convert SQL into syntax trees and physical operators.
- Cost-based optimizers in Hive, Trino, Spark, and Flink can choose join algorithms and reorder joins to reduce intermediate state.
- Other niche query languages include Pig-like relational pipelines, DataFrame APIs, Morel, jq, JMESPath, JSONPath, and graph query languages such as Gremlin.

Interview hook: High-level languages help both humans and machines: fewer handwritten jobs and more optimizer freedom.

### Batch Processing and Cloud Data Warehouses Converge

Source: PDF 498-499; pp. 474-475

- Historically:
  - data warehouses used specialized appliances and SQL over relational data;
  - batch frameworks emphasized scalability, flexibility, general-purpose code, and arbitrary formats.
- Convergence:
  - batch frameworks now support SQL and columnar formats such as Parquet;
  - warehouses moved to cloud scale and adopted scheduling, fault tolerance, shuffling, and distributed storage techniques;
  - warehouses now expose non-SQL models such as DataFrame-style libraries;
  - workflow orchestrators integrate with warehouses.
- Limits of SQL/warehouses:
  - iterative graph algorithms such as PageRank;
  - complex ML;
  - nonrelational and multimodal AI data such as images, video, and audio;
  - row-by-row computation on column-oriented storage;
  - cost for very large jobs.
- Decision factors: cost, convenience, implementation ease, and availability. Large enterprises often use many processing systems; smaller companies may use one.

Interview hook: The modern boundary between batch frameworks and cloud warehouses is operational and economic as much as technical.

### DataFrames

Source: PDF 499-500; pp. 475-476

- DataFrames are table-like collections of rows with typed columns, familiar from R and Pandas.
- Users call functions for relational operations such as filter, join, sort, and aggregate instead of writing one large SQL query.
- Local DataFrames were originally in-memory and limited to one machine.
- Distributed frameworks such as Spark, Flink, and Daft added DataFrame APIs so data scientists could work with large batch datasets.
- Migration caveat: local DataFrames are usually indexed and ordered; distributed DataFrames generally are not, which can cause performance surprises.
- Execution models differ:
  - Pandas executes operations immediately.
  - Spark builds a query plan, optimizes it, then runs on a distributed dataflow engine.
  - Daft supports both client-side and server-side computation.
- Apache Arrow is cited as a shared columnar data model for client and server execution engines.

Interview hook: Similar API does not imply identical execution semantics; eager local DataFrames and optimized distributed DataFrames behave differently.

## 5. Batch Use Cases

Source: PDF 500-505; pp. 476-481

### General Use Case Pattern

- Batch jobs work well when:
  - data volume is large;
  - data freshness is not critical;
  - bulk throughput matters more than low latency.
- Examples from the chapter:
  - accounting and inventory reconciliation,
  - manufacturing demand forecasting,
  - recommendation model training,
  - batch-based financial systems.

### Extract-Transform-Load

Source: PDF 500-501; pp. 476-477

- ETL/ELT pipelines extract from production systems, transform data, and load downstream systems such as warehouses.
- Batch jobs suit ETL because many transformations are embarrassingly parallel:
  - filtering,
  - projecting fields,
  - common warehouse transformations.
- Workflow schedulers help schedule, orchestrate, retry, debug, and mark failures.
- Airflow is cited as having built-in source, sink, and query operators for systems such as MySQL, PostgreSQL, Snowflake, Spark, and Flink.
- Debuggability is a key advantage: failed files can be inspected, transformation logic fixed, and jobs rerun.
- Organizational trend:
  - data pipelines used to be owned by central data engineering teams;
  - improved models and metadata management allow more product/application teams to publish their own data.
- Data mesh, data contract, and data fabric are cited as practices for safe organizational data publishing.
- ETL and analytics increasingly share systems such as SparkSQL, Trino, and DuckDB.

Interview hook: ETL uses batch not merely for scale, but for rerunability, debuggability, and workflow management.

### Analytics

Source: PDF 501-502; pp. 477-478

- OLAP queries often scan many records and perform grouping and aggregation.
- Batch frameworks can execute such analytical SQL over data in DFS/object stores.
- Table metadata can be managed with formats/catalogs such as Apache Iceberg and Unity.
- This architecture is called a data lakehouse.
- Two query styles:
  - Pre-aggregation queries: roll up data into OLAP cubes or data marts, often on a schedule.
  - Ad hoc queries: users iteratively investigate business questions, user behavior, and operational issues.
- Response time matters for ad hoc use because users ask follow-up questions based on results.
- SQL support enables integration with spreadsheets and visualization tools such as Tableau, Power BI, Looker, and Apache Superset.

Interview hook: Analytics in batch systems can be scheduled precomputation or interactive exploration over large stored datasets.

### Machine Learning

Source: PDF 502-503; pp. 478-479

- ML uses batch processing for:
  - feature engineering,
  - model training,
  - batch inference.
- Feature engineering transforms raw data into model-ready forms. Predictive models often need numeric features.
- Model training uses training data as input and model weights as output.
- Batch inference makes predictions in bulk when real-time output is unnecessary, including test-set evaluation.
- Spark MLlib and FlinkML are cited as batch ML toolkits.
- Recommendation and ranking systems often use graph processing.
- Graph algorithms often propagate information along edges repeatedly until a condition is met.
- Bulk synchronous parallel (BSP), also known as the Pregel model, is cited for graph batch processing. Implementations include Apache Giraph, Spark GraphX, and Flink Gelly.
- LLM data preparation/training uses batch processing for:
  - extracting text from HTML and fixing malformed text;
  - removing low-quality, irrelevant, and duplicate documents;
  - tokenizing text and converting it into embeddings.
- Kubeflow, Flyte, and Ray are cited as frameworks for AI/ML batch workloads.
- Interactive notebooks such as Jupyter or Hex often use batch systems through DataFrame APIs or SQL.

Interview hook: In ML, the batch job often produces a model, features, embeddings, or bulk predictions rather than a conventional report.

### Serving Derived Data

Source: PDF 503-505; pp. 479-481

- Batch jobs often build derived datasets such as:
  - product recommendations,
  - user-facing reports,
  - ML features.
- These derived datasets may need to be served from a production database, key-value store, search engine, OLAP system, or warehouse.
- Direct writes from batch tasks into production databases are discouraged:
  - one network request per record is far slower than batch throughput;
  - many parallel tasks can overload the database and harm live queries;
  - external side effects break clean all-or-nothing output semantics;
  - retries can duplicate visible output.
- Better pattern: batch jobs push precomputed datasets to streams such as Kafka topics.
- Benefits of using streams:
  - optimized for sequential writes;
  - buffer between batch and production systems;
  - downstream consumers can throttle reads;
  - one batch output can feed multiple systems;
  - stream can act as a security boundary between batch and production networks.
- Remaining issue: streams alone do not guarantee all-or-nothing visibility. Downstream consumers need to keep received data hidden until notified that the batch job is complete.
- Bootstrapping pattern: build a new database inside the batch job and bulk-load files into the database.
- Bulk-loading examples include TiDB Lightning, Apache Pinot Hadoop import jobs, and RocksDB SST bulk import.
- Bulk import is fast and can support atomic switching between dataset versions.
- Limitation: incrementally updating datasets is harder when each batch build creates a new full database.
- Hybrid approaches support both full dataset swaps and row-based updates; Venice hybrid stores are cited.

Interview hook: Never casually write from hundreds of batch tasks into a live serving database; discuss buffering, bulk load, atomic visibility, and retry semantics.

## Chapter-Level Memory Hooks

- Batch equals bounded input plus derived output; stream equals unbounded input plus continuous processing.
- The key safety idea is rerunability: immutable input plus replaceable output.
- Unix pipeline is the mini-version of distributed batch: extract, sort, count, rank.
- Sorting is the bridge from one-machine pipelines to MapReduce.
- DFS/object store is the batch storage layer; orchestrator is the distributed scheduler; compute engine is the data-processing layer.
- DFS gives data locality; object stores give decoupled compute/storage.
- Job scheduler places tasks; workflow scheduler manages DAG dependencies.
- MapReduce is map, sort/shuffle, reduce; dataflow engines optimize whole DAGs.
- Shuffle is not random: it is distributed grouping/sorting for joins and aggregations.
- SQL/DataFrames are usability layers over distributed execution.
- Batch is excellent for ETL, analytics, ML, and derived data, but poor for low-latency freshness.

## Interview Perspective

- Definition question: Batch processing handles bounded, immutable inputs and produces derived outputs from scratch, prioritizing throughput and rerunability over low latency.
- Tradeoff question: Batch jobs are easier to debug and rerun but may be stale, slow to complete, and expensive when tiny input changes force full recomputation.
- Unix tools question: The pipeline works because sorting places identical keys adjacent, letting uniq count without retaining a full key-count table.
- Scaling question: When data exceeds one machine, distribute storage, scheduling, and computation across a cluster.
- DFS vs object store question: DFSs offer block placement and data locality; object stores offer immutable object APIs, prefix listing, and decoupled compute/storage, but weaker filesystem semantics.
- Scheduling question: Resource allocation balances fairness and efficiency; preemption may improve priority handling but wastes work.
- Fault tolerance question: Because tasks are side-effect-free and outputs are replaceable, failed tasks can be retried independently.
- MapReduce question: Mapper emits key-value pairs; framework sorts/shuffles; reducer processes each key group.
- Dataflow question: Spark/Flink improve on MapReduce by optimizing whole DAGs, reducing intermediate DFS writes, fusing operators, and starting downstream work earlier.
- Shuffle question: The shuffle routes all records for the same key to the same reducer/partition and sorts or groups them for joins and aggregations.
- Serving question: Batch output should usually be published through streams, bulk load, or versioned swaps rather than direct per-record writes to production databases.

## Final Takeaways

- Batch processing is a reliability and scale pattern, not just a performance trick.
- Its power comes from immutable input, replaceable output, and high-throughput execution.
- Sorting and shuffling are foundational because they make counting, grouping, and joining large sharded datasets tractable.
- Modern batch systems moved beyond raw MapReduce toward dataflow engines, SQL, DataFrames, and cloud warehouse convergence.
- The strongest operational design avoids side effects inside tasks and publishes derived data with explicit visibility/commit semantics.

Confidence: High
