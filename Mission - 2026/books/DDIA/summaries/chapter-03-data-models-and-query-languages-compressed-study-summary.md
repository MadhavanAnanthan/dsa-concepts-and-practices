# Chapter 3: Data Models and Query Languages - Compressed Study Summary

Book: DDIA
Chapter: 3
Source PDF: books/DDIA/DDIA.pdf
Source pages: PDF pages 89-132; printed book pages 65-108
Method: Compressed from `books/DDIA/raw/chapter-03-chapter-3-data-models-and-query-languages.md`. This is not a verbatim extraction. No external knowledge was used.

Chapter 3 explains that data models are central because they shape both software structure and the way engineers think about a problem. Applications are built in layers: domain objects and APIs, persistent data models such as tables/documents/graphs/events, storage-engine byte representations, and finally hardware-level representation. Each layer hides lower-level complexity behind an abstraction.

Declarative query languages are a recurring theme. SQL, Cypher, SPARQL, and Datalog let users describe the desired result rather than the execution algorithm. This gives the query optimizer room to choose indexes, join order, algorithms, and parallel execution strategies without changing application queries.

The relational model stores data as relations/tables containing tuples/rows. It became dominant for structured data and remains especially important in analytics and data warehousing. NoSQL and NewSQL challenged relational systems with ideas around schema flexibility, scalability, and alternative data models, but many ideas have since converged: relational systems added JSON support, and document systems added joins, indexes, and richer query languages. Source: PDF pages 89-92; printed pages 65-68.

The document model is strongest when data is naturally a small tree that is usually read together. The chapter's profile example shows how positions, education, and contact information can be embedded in a single JSON document instead of split across several tables. This improves locality and can reduce application complexity for one-to-few relationships. It is weaker for many-to-one and many-to-many relationships, for very large embedded collections, and when individual nested items need independent identity. Source: PDF pages 93-95 and 104-106; printed pages 69-71 and 80-82.

Normalization stores human-meaningful information once and refers to it by ID. This improves consistency, localization, search, and update behavior. Denormalization duplicates information, often making reads faster but writes more expensive and consistency harder. The chapter frames denormalization as derived data that must be maintained. The social network timeline example shows a nuanced design: timelines may precompute some joins for speed while storing only IDs and hydrating fast-changing post/user details at read time. Source: PDF pages 96-99; printed pages 72-75.

Many-to-many relationships fit naturally in relational models through associative tables and indexes. In document models they often require references to other documents, secondary indexes, or duplicated relationship data. If relationships are stored on both sides, consistency risk appears because the same relationship exists in multiple places. Source: PDF pages 99-101; printed pages 75-77.

For analytics, relational warehouses commonly use star schemas and snowflake schemas. A star schema has a large central fact table of events and surrounding dimension tables that describe the who, what, where, when, how, and why of each event. Snowflake schemas normalize dimensions further; one big table goes further in the opposite direction by denormalizing dimension data into the fact table. Denormalization is less problematic in analytics because historical event data usually changes little. Source: PDF pages 101-103; printed pages 77-79.

Schema flexibility is not the absence of structure. Document databases are better described as schema-on-read: structure is interpreted by application code when data is read. Relational databases traditionally use schema-on-write: the database enforces structure at write time. Schema-on-read helps with heterogeneous data or externally controlled formats, but every reader must handle old and varied document shapes. Schema-on-write documents and enforces structure, but large migrations can be operationally difficult. Source: PDF pages 104-106; printed pages 80-82.

Graph models become natural when relationships are numerous, many-to-many, heterogeneous, and require traversal across multiple hops. A graph has vertices and edges. Property graphs attach labels and properties to both vertices and edges; triple stores represent information as subject-predicate-object statements. The chapter uses people and locations to show how graph queries express "born in the US and living in Europe" through recursive location traversal. Source: PDF pages 108-120; printed pages 84-96.

Cypher and SPARQL express graph pattern queries compactly. SQL can represent graph data and can express recursive traversal with recursive common table expressions, but the chapter shows that the SQL version is far more cumbersome. Datalog takes another approach: facts plus recursive rules that derive virtual tables. It is relational rather than graph-native, but powerful for recursive queries. Source: PDF pages 112-122; printed pages 88-98.

GraphQL is different from the other graph-related query languages. It is a constrained OLTP client query language for requesting a JSON document with exactly the fields needed by a UI. It helps clients evolve without changing server APIs, but it intentionally avoids arbitrary expensive or recursive queries from untrusted clients. Despite its name and JSON-shaped responses, GraphQL can run on top of relational, document, or graph databases. Source: PDF pages 122-125; printed pages 98-101.

Event sourcing stores writes as an immutable append-only event log. CQRS separates the write-optimized event log from read-optimized materialized views, also called projections or read models. In the conference example, bookings, cancellations, seat assignments, and capacity changes become events, while views serve booking status, dashboards, and badge printing. Commands must be validated before becoming events; events are facts and should be named in the past tense. Source: PDF pages 125-127; printed pages 101-103.

Event sourcing advantages include clearer business intent, reproducible view rebuilding, multiple optimized read models, easier creation of new views, auditability, and high write throughput from append-only access. Downsides include deterministic replay requirements, personal-data deletion problems with immutable logs, risk of repeating external side effects during replay, and the need for all views to process events in log order. Source: PDF pages 127-129; printed pages 103-105.

DataFrames, matrices, and arrays are common in analytics, ML, scientific computing, and financial time-series work rather than OLTP systems. DataFrames resemble tables but are manipulated through incremental commands for wrangling data. They support filtering, grouping, aggregation, and merge-style joins, while also helping transform relational-like data into sparse matrices or multidimensional arrays for numerical algorithms. Source: PDF pages 129-131; printed pages 105-107.

The chapter's overall message is that every data model makes some data shapes and queries easy while making others awkward. Documents fit self-contained trees. Relations fit joins and analytics schemas. Graphs fit dense relationship traversal. Event logs fit write-first historical state changes with derived views. DataFrames fit analytical transformation toward matrices. Systems can emulate other models, but the fit of the model and query language matters.

Key memory line: choose the data model that makes the important relationships and queries in your domain simple, explicit, and maintainable.

Confidence: High
