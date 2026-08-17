# Chapter 3: Data Models and Query Languages - Compressed Study Notes

Book: DDIA
Chapter: 3
Source PDF: books/DDIA/DDIA.pdf
Source pages: PDF pages 89-132; printed book pages 65-108
Method: Compressed section by section from `books/DDIA/raw/chapter-03-chapter-3-data-models-and-query-languages.md`. This is not a verbatim extraction. No external knowledge was used.

## What This Chapter Is Really About

Data models shape not only how software is written, but also how engineers think about the problem being solved. Applications are built as layers of data models:

- Real-world domain concepts are modeled as application objects, data structures, and APIs.
- Persistent storage represents those structures using general-purpose models such as documents, relational tables, graphs, events, or DataFrames.
- Database engines represent those models as bytes in memory, on disk, or on the network.
- Hardware eventually represents bytes physically.

Each layer hides lower-level complexity behind a cleaner abstraction. The chapter compares relational, document, graph, event-sourcing, and DataFrame-style models, focusing on which data shapes and query patterns are natural or awkward in each.

Source: PDF pages 89-90; printed pages 65-66

## Declarative Query Languages

Declarative query languages let the user describe the desired result rather than the execution algorithm. SQL, Cypher, SPARQL, and Datalog are examples discussed in the chapter.

The key idea:

- Declarative query: specify patterns, filters, sorting, grouping, aggregation, and transformation.
- Imperative algorithm: specify the exact sequence of operations.

The advantage of declarative languages is that the database query optimizer can choose indexes, join algorithms, join order, and parallel execution strategies. This allows the database to improve performance without requiring query rewrites.

Interview hook: declarative languages separate logical intent from physical execution. This is why the same SQL query may run faster after an index or optimizer improvement without changing application code.

Source: PDF page 90; printed page 66

## Relational Versus Document Models

The relational model, introduced by Edgar Codd, organizes data into relations, called tables in SQL, containing unordered tuples, called rows in SQL. Despite early doubts about efficient implementation, relational databases and SQL became dominant for structured data by the mid-1980s and remain central in analytics and data warehousing.

Many alternatives appeared over time, including network, hierarchical, object, XML, NoSQL, and NewSQL systems. The chapter emphasizes that many competitors generated hype, but SQL adapted by incorporating support for XML, JSON, and graph data.

NoSQL was not a single technology. It was a loose set of ideas around schema flexibility, scalability, open source licensing, and alternative data models. One lasting effect was the popularity of JSON-style document models, originally associated with systems such as MongoDB and Couchbase and later added to many relational databases.

Core tension:

- Relational model: strong support for joins, many-to-one, and many-to-many relationships.
- Document model: natural fit for self-contained, tree-shaped data and flexible schemas.

Source: PDF pages 91-92; printed pages 67-68

## Object-Relational Mismatch and ORMs

Object-oriented applications often store in-memory data as objects, while relational databases store rows, columns, and tables. Translating between those representations is called the object-relational impedance mismatch.

ORM frameworks such as ActiveRecord and Hibernate reduce boilerplate, but the chapter lists several concerns:

- ORMs are complex and cannot fully hide differences between object and relational models.
- OLTP application code may use an ORM, but analytics users still need the relational schema to be sensible.
- ORM support may be limited when organizations use search engines, graph databases, or NoSQL systems.
- Automatically generated schemas may be awkward or inefficient.
- ORMs can make inefficient query patterns easy to write.

Important example: the N+1 query problem. If one query returns N comments and the application then performs one extra query per comment to fetch each author, the application performs N+1 queries instead of one database join.

ORM advantages:

- Reduce repetitive mapping code for simple relational persistence.
- May cache query results.
- May help with schema migrations and administrative tasks.

Interview hook: ORMs are not bad by default; the danger is believing they remove the need to understand the relational model and query behavior.

Source: PDF pages 92-93; printed pages 68-69

## Document Data for One-to-Many Relationships

The chapter uses a LinkedIn-style profile to compare relational and document models. In a relational schema, the user profile has a users table plus separate tables for positions, education, and contact information, each referencing the user. In a JSON document, the profile can be represented as one nested structure.

The JSON representation fits the tree shape of the data:

- One profile has several positions.
- One profile has several education entries.
- One profile has contact information.

The document model has better locality when the application usually loads the whole profile. Instead of multiple table lookups or a multiway join, all relevant profile data is in one document.

Important limit: this works well for one-to-few relationships, not unbounded one-to-many relationships. Embedding thousands of comments on a popular post in one document would be unwieldy; separate records are preferable.

Mental model: use a document when the data naturally forms a small tree that is usually read together.

Source: PDF pages 93-95; printed pages 69-71

## Normalization, Denormalization, and Joins

Normalization means storing human-meaningful information in one place and referring to it by an ID. Denormalization means duplicating the human-meaningful value directly in records that use it.

The chapter uses `region_id` instead of a plain text region name to show why normalization can help:

- Consistent spelling and style.
- Avoiding ambiguity.
- Easier global updates.
- Localization.
- Better search through structured region metadata.

The key advantage of IDs is stability. Human-meaningful labels may change, but internal IDs can remain the same. If duplicated labels change, every copy must be updated, increasing write cost and inconsistency risk.

The cost of normalization is lookup work. To display a human-readable value, the system must resolve the ID through a join or an application-level lookup. Document databases can store normalized or denormalized data, but weak join support in many document databases makes normalization less convenient.

Source: PDF pages 96-97; printed pages 72-73

## Trade-Offs of Normalization

The resume example stores organization and school names as strings. The chapter asks whether those should instead become entities referenced by ID, especially if more attributes such as logos, descriptions, or news feeds need to be stored.

General trade-off:

- Normalized data is usually faster to write and easier to update consistently because there is only one copy.
- Normalized data can be slower to query because joins or lookups are needed.
- Denormalized data is usually faster to read because fewer joins are required.
- Denormalized data costs more to write because multiple copies may need updating.
- Denormalized data uses more storage and creates consistency risks.

Denormalization can be viewed as derived data: redundant copies must be maintained from a source of truth.

OLTP systems often benefit from normalization because both reads and updates matter. Analytics systems often tolerate denormalization better because updates happen in bulk and read-only query speed dominates.

Source: PDF pages 97-98; printed pages 73-74

## Denormalization in Social Network Timelines

The social network timeline example from Chapter 2 reappears as a normalization trade-off. A normalized query joins posts and follows at read time. A materialized timeline precomputes part of that join for faster reads.

The implementation described for X/Twitter does not store the full post content in each materialized timeline entry. It stores IDs and limited metadata. When a timeline is read, the service hydrates those IDs by fetching post content, counters, and sender profile details.

Why keep some things normalized?

- Like counts and reply counts change frequently.
- Usernames and profile photos may change.
- Timelines should show current information.
- Full denormalization would increase storage substantially.

Important lesson: scalable systems may denormalize some parts while leaving fast-changing or bulky data normalized. Joins are not automatically unscalable; hydrating IDs can parallelize well.

Interview hook: deciding to denormalize requires knowing read frequency, write frequency, update frequency, fan-out, outliers, and storage cost.

Source: PDF pages 98-99; printed pages 74-75

## Many-to-One and Many-to-Many Relationships

The chapter distinguishes relationship shapes:

- One-to-many or one-to-few: one resume has several positions; each listed position belongs to one resume.
- Many-to-one: many people live in the same region.
- Many-to-many: one person may have worked for several organizations, and each organization may have many current or past employees.

Relational databases usually represent many-to-many relationships through an associative table, also called a join table. A positions table can associate a user ID with an organization ID.

Many-to-one and many-to-many relationships do not fit cleanly inside one self-contained JSON document. A document can reference other documents by ID, but querying relationships in both directions often requires indexes or duplicated relationship data.

If relationship references are stored on both sides, the relationship becomes denormalized and can become inconsistent. A normalized representation stores the relationship once and uses secondary indexes to query it efficiently from either side.

Source: PDF pages 99-101; printed pages 75-77

## Stars, Snowflakes, and Analytics Schemas

Data warehouses are usually relational and commonly use star schemas, snowflake schemas, dimensional modeling, or one big table.

In a star schema:

- A central fact table stores events.
- Each fact row represents something that happened, such as a purchase or page view.
- Dimension tables describe the who, what, where, when, how, and why of each event.
- Fact rows contain attributes and foreign keys into dimensions.

Fact tables can become extremely large because they store individual events. Dimension tables may be wide because they hold analysis metadata.

Snowflake schemas normalize dimensions further into subdimensions. For example, product brand and category may be separate tables referenced from the product dimension. Snowflakes are more normalized; star schemas are often simpler for analysts.

One big table denormalizes dimension attributes into the fact table by precomputing joins. This uses more storage but can improve query speed. In analytics, denormalization is less problematic because historical event data typically does not change often.

Interview hook: star schema design optimizes analyst query simplicity and read performance, not OLTP update efficiency.

Source: PDF pages 101-103; printed pages 77-79

## When to Use the Document Model

Arguments for the document model:

- Schema flexibility.
- Locality when reading a whole document.
- Natural mapping to application objects for some tree-shaped data.

Arguments for the relational model:

- Better support for joins.
- Better fit for many-to-one and many-to-many relationships.
- Ability to directly refer to individual records by ID.

Use a document model when the application data is document-like: a tree of one-to-many relationships where the whole tree is commonly loaded at once. Splitting such a structure into many relational tables can make schemas and application code cumbersome.

Document model limitations:

- Nested items are not usually addressable as independent records.
- Large documents can be inefficient when only small parts are needed.
- Frequent small updates can be expensive if the whole document must be rewritten.

The chapter also notes a case where documents handle ordering naturally: reorderable lists can be represented as arrays. Relational systems need additional techniques to represent user-controlled order.

Source: PDF pages 104-105; printed pages 80-81

## Schema Flexibility: Schema-on-Read and Schema-on-Write

Calling document databases schemaless is misleading. Application code usually assumes structure; the schema is implicit rather than enforced by the database.

Important terms:

- Schema-on-read: structure is interpreted when data is read.
- Schema-on-write: database enforces the schema when data is written.

The chapter compares this to dynamic versus static type checking. Neither approach is universally best.

Schema changes show the difference. If a document database changes from storing full name to first and last name, new documents can use the new fields while application code handles old documents at read time. In a relational database, the schema is changed explicitly, and existing rows may be migrated.

Trade-off:

- Schema-on-read can be easier with heterogeneous data or data from uncontrolled external systems.
- Schema-on-read pushes compatibility handling into all readers.
- Schema-on-write documents and enforces structure.
- Large schema migrations can be operationally challenging.

Source: PDF pages 104-106; printed pages 80-82

## Data Locality for Reads and Writes

Documents are often stored contiguously as JSON, XML, or binary encodings such as BSON. If the application needs most of the document at once, locality improves read performance by reducing separate lookups.

The locality advantage disappears or reverses when:

- The application needs only a small part of a large document.
- The whole document must be loaded anyway.
- Frequent small updates require rewriting the whole document.

Practical guideline from the chapter: keep documents fairly small and avoid frequent small updates.

Locality is not exclusive to document databases. The chapter mentions relational or wide-column features that can colocate related data, including interleaved rows, multi-table index cluster tables, and column families.

Source: PDF page 106; printed page 82

## Query Languages for Documents

Relational databases are usually queried with SQL. Document databases vary more widely:

- Some allow only primary-key access.
- Some support secondary indexes inside documents.
- Some provide richer query languages.

XML systems use XQuery and XPath. JSON has JSON Pointer and JSONPath. MongoDB's aggregation pipeline is presented as a JSON-syntax query language for JSON documents.

The chapter compares SQL and MongoDB aggregation using an example that counts shark sightings per month. Both filter by family, group by month, and sum the number of observed animals. The expressiveness is similar for this aggregation; the syntax differs.

Source: PDF pages 106-107; printed pages 82-83

## Convergence of Relational and Document Databases

Relational and document databases began as different approaches but have become more similar:

- Relational databases added JSON types, JSON query operators, and indexes inside documents.
- Some document databases added joins, secondary indexes, and declarative query languages.

This convergence helps developers because many applications need both relational-style references and document-style flexibility. The chapter describes relational-document hybrids as a powerful combination.

Source: PDF pages 107-108; printed pages 83-84

## Graph-Like Data Models

Graphs are useful when many-to-many relationships are common and complex. A graph contains:

- Vertices, also called nodes or entities.
- Edges, also called relationships or arcs.

Examples:

- Social graph: people as vertices, relationships as edges.
- Web graph: pages as vertices, links as edges.
- Road or rail network: junctions as vertices, routes as edges.

Graphs can also store heterogeneous data. The chapter mentions examples where people, places, events, comments, check-ins, organizations, and facts can all be represented in one graph.

Data representation options include:

- Adjacency list: each vertex stores neighbor IDs; good for traversal.
- Adjacency matrix: a two-dimensional vertex-by-vertex representation; useful in matrix-oriented settings.

Source: PDF pages 108-110; printed pages 84-86

## Property Graphs

In the property graph model, each vertex has:

- A unique identifier.
- A label describing the object type.
- Incoming and outgoing edges.
- Properties as key-value pairs.

Each edge has:

- A unique identifier.
- Tail vertex.
- Head vertex.
- A label describing the relationship.
- Properties as key-value pairs.

A property graph can be represented using relational tables: one table for vertices and one for edges. Indexes on edge tail and head allow efficient traversal in both directions.

Important strengths:

- Any vertex can connect to any other vertex.
- Traversal can move forward or backward through edges.
- Different labels allow different object and relationship types in one model.
- Graphs are evolvable because new relationship types can be added without redesigning the entire schema.

Limitation: a graph edge connects two vertices, while a relational join table row can represent relationships involving three or more entities. The graph workaround is to introduce an additional vertex or use a hypergraph.

Source: PDF pages 110-112; printed pages 86-88

## Cypher

Cypher is a query language for property graphs. It uses pattern matching with an arrow notation that resembles graph edges.

The chapter's running query asks for people born in the United States and living in Europe. In Cypher, this is expressed by matching:

- A person with a `BORN_IN` edge to a location within the United States.
- The same person with a `LIVES_IN` edge to a location within Europe.

The `WITHIN*0..` pattern means following a `WITHIN` edge zero or more times, allowing traversal through arbitrary levels of a location hierarchy.

Important idea: the query describes the pattern, not the execution path. The database may scan people first, or it may start from indexed location vertices and traverse backward.

Source: PDF pages 112-114; printed pages 88-90

## Graph Queries in SQL

Graph data can be stored in relational tables, but querying variable-length paths in SQL is awkward. Each edge traversal is effectively a join with the edges table. Relational queries often know the number of joins in advance, while graph traversals may require an unknown number of hops.

SQL can express recursive traversal using recursive common table expressions (`WITH RECURSIVE`). The chapter rewrites the Cypher migration query in SQL and shows that a concise graph query becomes much longer and harder to read.

Main lesson: one data model can emulate another, but the query language may become clumsy if it does not match the shape of the problem.

Source: PDF pages 114-116; printed pages 90-92

## Triple Stores and RDF

Triple stores represent information as `(subject, predicate, object)` statements. Example: a person likes a thing.

Relationship to property graphs:

- Subject corresponds to a vertex.
- If the object is a primitive value, predicate and object act like a property key and value.
- If the object is another vertex, predicate acts like an edge label from subject to object.

The chapter shows the same graph data encoded in Turtle, a compact RDF-related format. RDF was designed for internet-wide data exchange and often uses URIs for subjects, predicates, and objects to avoid naming conflicts when combining data from different sources.

Semantic Web context:

- The original Semantic Web vision did not succeed as imagined.
- Its legacy includes linked data standards, structured vocabularies, knowledge graphs, Open Graph-style metadata, and RDF tooling.
- Triple stores can still be useful as internal application data models.

Source: PDF pages 116-119; printed pages 92-95

## SPARQL

SPARQL is a query language for RDF triple stores. It predates Cypher, and the chapter notes that Cypher's pattern matching was borrowed from SPARQL.

SPARQL can express the same born-in-US and lives-in-Europe query concisely. Since RDF uses predicates for both properties and edges, matching a property and traversing a relationship use the same basic syntax.

Interview hook: SPARQL and Cypher are different graph query ecosystems, but both demonstrate the value of pattern-oriented graph querying for multi-hop relationships.

Source: PDF pages 119-120; printed pages 95-96

## Datalog

Datalog is an older query language based on relations rather than graphs, but it is powerful for recursive graph-like queries.

In Datalog:

- Database contents are facts.
- A fact corresponds to a row in a relational table.
- Rules derive virtual tables from facts and other rules.
- Rules can be recursive.

The chapter represents locations and graph edges as Datalog facts. It then builds derived rules:

- `within_recursive` finds all places contained within other places through repeated `within` relationships.
- `migrated` derives where a person was born and where they live.
- `us_to_europe` filters migrated people to those born in the United States and living in Europe.

Datalog requires a different style of thinking. Instead of writing one pattern-matching query, you define reusable rules, similar to functions that can call each other.

Source: PDF pages 120-122; printed pages 96-98

## GraphQL

GraphQL is much more restrictive than Cypher, SPARQL, SQL, or Datalog. It is designed for OLTP-style client queries where client software requests a JSON document with the exact structure and fields needed for a UI.

Benefits:

- Client code can change requested fields without changing server APIs.
- Responses mirror the requested query shape.
- The server does not need to know every UI-specific field combination in advance.

Costs and limits:

- Organizations often need tooling to translate GraphQL queries into internal service calls.
- Authorization, rate limiting, and performance require care.
- GraphQL intentionally avoids arbitrary expensive queries from untrusted clients.
- It does not support recursive queries.
- It only allows joins/relationships exposed by the GraphQL schema.

The chapter's chat example shows duplication in GraphQL responses: sender names, image URLs, and reply contents may be repeated to make UI rendering simple. The underlying database can remain normalized; the GraphQL response does not have to match the storage model.

Important distinction: despite its name and JSON-shaped responses, GraphQL can be implemented on relational, document, or graph databases.

Source: PDF pages 122-125; printed pages 98-101

## Event Sourcing and CQRS

The earlier models generally query data in the same form in which it is written. Event sourcing and CQRS split those concerns.

Event sourcing:

- Every state change is written as an immutable event.
- Events are appended to a log.
- Events represent facts that happened.
- Later events may supersede earlier events, but the old events remain part of history.

CQRS:

- The write-optimized representation is separated from read-optimized representations.
- Materialized views, also called projections or read models, are derived from the event log.

The chapter uses a conference management example. Events may represent registrations opening, bookings made, cancellations, seat assignments, and capacity changes. Views can then be built for booking status, organizer dashboards, and badge printing.

Important terminology:

- A command is an incoming request.
- The command must be validated.
- Once valid and executed, it becomes an event/fact in the log.
- Event consumers that build views should not reject valid events from the log.

Event names should be in the past tense because they record facts that already happened.

Source: PDF pages 125-127; printed pages 101-103

## Event Sourcing Advantages

Advantages listed in the chapter:

- Events communicate user or business intent better than low-level row mutations.
- Materialized views can be deleted and recomputed from the log, which helps fix bugs in view-maintenance code.
- Multiple read models can be optimized for different queries.
- Views can use any data model and may be denormalized.
- New views can be built from existing events.
- New features can add event types or event properties without rewriting old events.
- Later events can reverse earlier mistakes.
- The event log can serve as an audit log.
- Append-only logs can handle high write throughput and absorb bursts, while downstream views catch up.

Mental model: the event log is the source of truth; read models are replaceable caches derived from it.

Source: PDF pages 127-128; printed pages 103-104

## Event Sourcing Downsides

Downsides listed in the chapter:

- Derived views must be deterministic. If processing uses external information, such as an exchange rate, the event must include enough data or have a stable historical lookup so recomputation gives the same result.
- Immutable events create problems for personal data deletion requests. Possible mitigations include keeping personal data outside events or using crypto-shredding, but these complicate recomputation.
- Reprocessing events must not repeat external side effects, such as sending confirmation emails.
- All materialized views must process events in the same order as the log, which is difficult in distributed systems.

Event sourcing can be implemented on many databases. The chapter also names specialized systems and notes that message brokers and stream processors can maintain materialized views.

Source: PDF pages 128-129; printed pages 104-105

## DataFrames, Matrices, and Arrays

DataFrames and multidimensional arrays are common in analytical and scientific contexts, not typical OLTP systems.

A DataFrame resembles a table or spreadsheet but is usually manipulated through a series of commands rather than a declarative query language. This matches data science workflows where users incrementally wrangle a private copy of data.

DataFrames support relational-like bulk operations:

- Apply functions to rows.
- Filter rows.
- Group and aggregate.
- Join, often called merge in DataFrame APIs.

DataFrames can also transform relational-like data into matrices or multidimensional arrays used by machine learning algorithms. The chapter's movie-rating example pivots user/movie ratings into a sparse matrix where rows are users and columns are movies.

Nonnumerical data can be converted into numerical matrix form:

- Dates can be scaled into numeric ranges.
- Categories can use one-hot encoding.

Array databases specialize in large multidimensional arrays and are used for scientific datasets such as geospatial raster data, medical imaging, and astronomical observations. DataFrames are also used for financial time-series data and have been added to batch processing frameworks.

Source: PDF pages 129-131; printed pages 105-107

## Chapter Summary

The chapter surveys several data models and their natural use cases:

- Relational model: durable, broadly useful, especially strong for joins, analytics, star schemas, snowflake schemas, and SQL.
- Document model: good for self-contained JSON-like trees where relationships between documents are rare.
- Graph models: good when relationships are dense, many-to-many, heterogeneous, and require multi-hop traversal.
- DataFrames: bridge relational-style data to large-column, matrix, ML, statistics, and scientific workflows.
- Event sourcing: stores writes as an immutable append-only log and derives read-optimized materialized views through CQRS.

Many models can emulate each other, but the result may be awkward when the query language does not match the problem. Database systems are also converging by adding neighboring capabilities, such as JSON support in relational databases and joins in document databases.

A major recurring theme is schema flexibility. Nonrelational models often avoid enforced schemas, but applications still assume structure. The question is whether the schema is explicit and enforced on write, or implicit and interpreted on read.

The chapter ends by noting that many other specialist data models exist, including genome sequence search, financial ledgers, distributed ledgers, and full-text search.

Source: PDF pages 131-132; printed pages 107-108

## High-Value Interview Hooks

- Data model choice affects how naturally you can represent and query the domain.
- Declarative query languages describe what result is needed; optimizers decide how to execute.
- ORMs reduce boilerplate but do not remove relational modeling or query-performance concerns.
- Documents work best for small, self-contained one-to-many trees loaded together.
- Normalization improves consistency and update behavior; denormalization improves some reads but creates derived-data maintenance work.
- Materialized timelines show partial denormalization: precompute expensive relationships while hydrating fast-changing details by ID.
- Star schemas center analytics around fact tables and dimension tables.
- Schema-on-read is flexible but pushes compatibility handling into readers.
- Graph models are strong for many-to-many, heterogeneous, multi-hop relationships.
- Recursive graph traversal is natural in Cypher, SPARQL, and Datalog but clumsy in SQL.
- GraphQL is not a database model; it is a constrained client query language for shaped JSON responses.
- Event sourcing makes the event log the source of truth and treats read models as derived views.
- DataFrames are practical for data wrangling and transforming data into matrix form for ML and scientific analysis.

Confidence: High
