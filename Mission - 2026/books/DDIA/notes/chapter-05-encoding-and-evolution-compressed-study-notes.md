# Chapter 05: Encoding and Evolution - Compressed Study Notes

Book: DDIA
Chapter: 5, Encoding and Evolution
Source PDF: books/DDIA/DDIA.pdf
Source PDF pages: 185-217
Raw source used: books/DDIA/raw/chapter-05-chapter-5-encoding-and-evolution.md
Method: Compressed section-by-section study notes derived only from the raw Chapter 5 markdown. Long verbatim copying was avoided; concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks were preserved.

## Section-Level Source Page References

| Section | Source PDF pages |
| --- | --- |
| Chapter framing: evolvability, rolling upgrades, compatibility | 185-187 |
| Formats for encoding data: in-memory versus byte sequences | 187-188 |
| Language-specific formats | 188-189 |
| JSON, XML, CSV, JSON Schema, and binary JSON/XML variants | 189-193 |
| Protocol Buffers and schema evolution | 193-195 |
| Avro, reader/writer schemas, schema evolution, and dynamic schemas | 196-201 |
| Merits of schema-based binary encodings | 201-202 |
| Modes of dataflow overview | 202 |
| Dataflow through databases and archival storage | 202-204 |
| Dataflow through services: REST, web services, OpenAPI, gRPC | 204-207 |
| RPC problems | 207-208 |
| Load balancers, service discovery, and service meshes | 208-210 |
| Data encoding and evolution for RPC | 210-211 |
| Durable execution and workflows | 211-213 |
| Event-driven architectures, message brokers, distributed actors | 213-215 |
| Chapter summary | 215-217 |

## 1. Chapter Frame: Evolution Requires Compatibility

Source pages: 185-187

- Applications change because product needs, user understanding, and business circumstances change.
- Data formats often need to change with application features:
  - Add a field.
  - Add a record type.
  - Present existing data differently.
- Relational systems generally have one active schema at a time, changed through migrations.
- Schema-on-read systems may contain mixed old and new data formats because the database does not enforce one schema.
- Code rollout is not instantaneous:
  - Server deployments often use rolling or staged rollout.
  - Client-side applications depend on users installing updates.
- Therefore old code, new code, old data, and new data may coexist.

Key terminology:

- Backward compatibility: newer code can read data written by older code.
- Forward compatibility: older code can read data written by newer code.
- API compatibility has direction:
  - Older client to newer service: backward-compatible request, forward-compatible response.
  - Newer client to older service: forward-compatible request, backward-compatible response.

Important edge case:

- If newer code writes a new field and older code later reads, updates, and writes the record back, the older code should preserve the unknown field.
- If unknown fields are not preserved, the old code may accidentally delete data it does not understand.

Tradeoff:

- Backward compatibility is usually easier because new code can be written with knowledge of old formats.
- Forward compatibility is harder because old code must safely ignore or preserve future additions.

Interview hook: Compatibility is not just about parsing. It is also about update behavior: old writers must not destroy new fields they cannot interpret.

## 2. Encoding and Decoding: Two Representations of Data

Source pages: 187-188

- Programs usually handle data in two forms:
  - In memory: objects, structs, lists, arrays, hash tables, trees, and pointers optimized for CPU manipulation.
  - On disk or network: self-contained byte sequences such as JSON documents.
- Pointers only make sense inside one process, so data must be translated before it crosses process or storage boundaries.

Terminology:

- Encoding: converting in-memory data into bytes.
- Decoding: converting bytes back into in-memory data.
- Other names:
  - Encoding is also called serialization or marshaling.
  - Decoding is also called parsing, deserialization, or unmarshaling.
- The chapter avoids "serialization" because that word has a different meaning in transaction theory.

Exceptions:

- Some systems operate directly on compressed data.
- Some zero-copy formats are designed for both runtime and storage/network use, avoiding a separate conversion step.

Interview hook: Encoding is the boundary contract between processes, versions, and storage eras.

## 3. Language-Specific Formats

Source pages: 188-189

- Many languages provide built-in object encoding:
  - Java: java.io.Serializable.
  - Python: pickle.
  - Ruby: Marshal.
  - Third-party examples include Kryo for Java.
- These formats are convenient because they save and restore in-memory objects with little code.

Problems:

- Language lock-in:
  - Data becomes tied to one programming language.
  - Cross-language integration becomes difficult.
- Security risk:
  - Decoding may instantiate arbitrary classes.
  - If an attacker supplies malicious bytes, decoding can lead to severe execution risks.
- Weak versioning:
  - Forward and backward compatibility are often not central design goals.
- Efficiency:
  - CPU cost and encoded size may be poor.

Recommendation from the chapter:

- Avoid language-specific built-in encodings except for very transient use.

Interview hook: Built-in object serialization optimizes developer convenience, not long-lived compatibility, interoperability, or safety.

## 4. JSON, XML, CSV, and Their Weak Spots

Source pages: 189-190

- JSON and XML are widely known, standardized, language-independent formats.
- CSV is also common but mainly supports tabular data without nesting.
- Text formats are somewhat human-readable and widely accepted for data interchange.

Limitations:

- XML is often criticized as verbose and complicated.
- Number encoding is ambiguous:
  - XML and CSV cannot distinguish a numeric value from a digit-only string without an external schema.
  - JSON distinguishes strings and numbers but not integers from floats and does not specify numeric precision.
  - Very large integers can be parsed inaccurately in languages that use double-precision floating-point numbers.
- Binary data is awkward:
  - JSON and XML support Unicode text well.
  - They do not directly support arbitrary byte strings.
  - Base64 can work but increases size and relies on schema or convention.
- Schema languages can become complex:
  - XML Schema and JSON Schema can describe rich constraints.
  - Without schema usage, applications often hardcode interpretation rules.
- CSV has no schema:
  - Row and column meaning is application-defined.
  - Adding rows or columns requires manual compatibility handling.
  - Escaping rules exist but parser behavior may vary.

Why they remain popular:

- They are good enough for many interchange cases.
- Agreement between organizations is often harder than inefficiency or awkward syntax.

Interview hook: JSON being "simple" does not mean its data model is precise. Numbers, binary data, and schema interpretation are common traps.

## 5. JSON Schema

Source pages: 190-191

- JSON Schema is widely used to model exchanged or stored JSON data.
- The chapter lists usage in:
  - Web services through OpenAPI.
  - Schema registries.
  - Databases and validators.
- It supports primitive types such as string, number, integer, object, array, boolean, and null.
- It also supports validation constraints, such as restricting a port field to a valid range.

Open versus closed content models:

- Open content model:
  - Allows fields not defined in the schema.
  - In JSON Schema, additionalProperties defaults to true.
  - This often makes the schema a definition of invalid cases rather than an exhaustive list of valid fields.
- Closed content model:
  - Allows only explicitly defined fields.
  - Achieved by disallowing additional properties.

Complexity:

- JSON Schema can express conditional if/else logic, named types, remote references, pattern-based properties, and more.
- These features are powerful but can make schemas hard to reason about and evolve compatibly.

Example concept:

- A JSON object cannot truly have integer keys because JSON object keys are strings.
- JSON Schema can constrain string keys to match digits and constrain values to strings.

Interview hook: Open content models help forward compatibility but can make validation and schema evolution harder to reason about.

## 6. Binary Encodings for JSON/XML and MessagePack

Source pages: 191-193

- JSON and XML are verbose compared with binary formats.
- Many binary variants exist for JSON and XML, including MessagePack, CBOR, BSON, and others.
- They may be more compact or faster in niches, but none has replaced textual JSON/XML in general adoption.

Common characteristic:

- Many binary JSON/XML formats preserve the JSON/XML data model.
- Because they do not prescribe a schema, encoded data still carries field names.

MessagePack example:

- The chapter encodes a record with userName, favoriteNumber, and interests.
- MessagePack includes object/field structure, string lengths, field names, and values.
- The encoded example is 66 bytes versus 81 bytes for whitespace-free JSON.

Tradeoff:

- Binary JSON may save some space and parse faster.
- The gain can be modest because field names remain.
- Human-readability is lost.

Interview hook: Binary without schema is not automatically compact. Field names and self-description still cost bytes.

## 7. Protocol Buffers

Source pages: 193-195

- Protocol Buffers is a binary encoding library developed at Google.
- It is similar in many relevant ways to Apache Thrift.
- It requires a schema written in an interface definition language.
- A code generation tool turns schema definitions into language-specific classes for encoding and decoding.

Schema properties:

- The schema lists fields and types.
- Compared with JSON Schema, the language is simple.
- It does not express rich value constraints such as ranges or regex-like validations.

Encoding properties:

- Field names are omitted from the encoded data.
- Field tags, such as 1, 2, and 3, identify fields compactly.
- Type annotations and length indicators allow the parser to skip unknown fields.
- Integers use variable-length encoding, so smaller numbers use fewer bytes.
- Repeated fields encode list elements as repeated occurrences of the same tag.

Schema evolution rules:

- Field names can change because encoded data uses tags, not names.
- Field tags must not change because they define the encoded meaning.
- New fields can be added if they use new tag numbers.
- Old code can ignore unknown tags, supporting forward compatibility.
- New code can read old data because missing fields receive defaults.
- Removed field tags must not be reused; they can be reserved.
- Some datatype changes are possible, but truncation can occur, such as when old code reads a value that no longer fits its older integer width.

Important edge case:

- Preserving unknown fields matters when old code reads and writes data written by new code.
- If the old code drops unknown tags, it can lose newer data.

Interview hook: In Protocol Buffers, tag numbers are the compatibility contract. Names are documentation; tags are data meaning.

## 8. Avro

Source pages: 196-201

- Apache Avro is a binary encoding format started as a Hadoop subproject.
- It uses schemas but differs from Protocol Buffers in important ways.
- Avro has:
  - Avro IDL for human editing.
  - JSON-based schema representation for machine use.
- Like Protocol Buffers, Avro schemas define fields and types but not complex validation rules like JSON Schema.

Encoding properties:

- Avro schemas do not use tag numbers.
- Encoded bytes contain values in schema order, without field names or datatype identifiers.
- Decoding requires knowing the exact writer's schema used for encoding.
- In the chapter's example, Avro is very compact because schema information is not repeated per record.

Writer's schema and reader's schema:

- Writer's schema: the schema used by the application that encoded the data.
- Reader's schema: the schema expected by the application decoding the data.
- Avro decodes by comparing the writer's schema and reader's schema and resolving differences.
- Field order can differ because schema resolution matches fields by name.
- If writer has a field the reader does not expect, it is ignored.
- If reader expects a field missing from the writer's schema, the reader uses its declared default value.

Schema evolution rules:

- Forward compatibility: writer uses a newer schema than reader.
- Backward compatibility: writer uses an older schema than reader.
- You may add or remove only fields that have default values if you want compatibility.
- Adding a field without a default breaks backward compatibility.
- Removing a field without a default breaks forward compatibility.
- Null must be explicit through a union type; fields are not nullable by default.
- Null can be a default only if it is the first branch of the union.
- Some type changes are possible when Avro can convert between the types.
- Field renaming can use aliases:
  - Backward compatible when the reader maps old writer names through aliases.
  - Not forward compatible in the same way.
- Adding a union branch is backward compatible but not forward compatible.

How readers find the writer's schema:

- Large file with many records:
  - Put the schema once at the beginning of the file using Avro object container files.
- Database with individually written records:
  - Prefix each record with a schema version.
  - Store schema versions in a schema database or registry.
- Network connection:
  - Processes can negotiate schema version on connection setup and use it for the connection lifetime.

Schema registry value:

- Acts as documentation.
- Allows compatibility checking before deployment.
- Version identifiers can be incrementing integers or schema hashes.

Dynamically generated schemas:

- Avro is friendly to generating schemas from another schema source, such as a relational database schema.
- A table can become an Avro record and columns can become fields.
- If the database schema changes, the export process can generate a new Avro schema each time.
- Because Avro matches by field name, old readers can still match fields when schemas evolve.
- Protocol Buffers is less convenient for this case because field tags require careful assignment and must not be reused.

Interview hook: Avro shifts compatibility from stable numeric tags to explicit writer/reader schema resolution. This makes schema distribution central.

## 9. Merits of Schema-Based Binary Encodings

Source pages: 201-202

- Protocol Buffers and Avro show that schemas can support compact binary encodings.
- Their schema languages are much simpler than XML Schema or JSON Schema.
- Similar older ideas exist, such as ASN.1, but the chapter describes ASN.1 as complex and poorly documented for new applications.
- Some databases use proprietary binary protocols and drivers to decode database responses into in-memory structures.

Advantages:

- Compactness:
  - Field names can be omitted.
  - They can be smaller than binary JSON variants.
- Documentation:
  - Required schema serves as live documentation.
  - Because decoding needs the schema, it is less likely to drift from reality.
- Compatibility checks:
  - A schema database can check forward and backward compatibility before deployment.
- Static typing:
  - Generated code can enable compile-time type checking in statically typed languages.
- Schema evolution:
  - Gives schema-on-read-like flexibility with stronger guarantees and tooling.

Operational caution:

- Keep the number of concurrent schema formats low to simplify operations.

Interview hook: Schema-driven formats are not just about saving bytes; they make evolution reviewable and enforceable.

## 10. Modes of Dataflow

Source page: 202

- Compatibility is a relationship between the process that encodes data and the process that decodes it.
- Data flows between processes in several common ways:
  - Through databases.
  - Through service calls.
  - Through workflow engines.
  - Through asynchronous messages.
- Encoding choice affects evolvability because different producers and consumers may be upgraded independently.

Interview hook: Always identify who writes, who reads, and which version each side may be running.

## 11. Dataflow Through Databases

Source pages: 202-204

- In databases:
  - Writer encodes data.
  - Reader decodes data.
- A single application can be viewed as sending data to its future self.
- Backward compatibility is necessary because future code must read past data.
- Forward compatibility is also often necessary because multiple application instances may access the same database during rolling upgrades.

Data outlives code:

- Application code may be replaced in minutes.
- Database records may remain for years in the format in which they were originally written.
- Large migrations are expensive, so databases often defer rewriting data.

Examples:

- LSM-tree engines may rewrite old data into newer formats during compaction.
- Relational databases may add a nullable column without rewriting all existing rows; missing values are filled on read.

Harder migrations:

- Changing a single-valued attribute to multivalued.
- Moving data into a separate table.
- Such changes often require application-level data rewriting.
- The chapter notes that maintaining forward and backward compatibility through complex migrations remains difficult.

Archival storage:

- Database snapshots or warehouse loads are usually encoded with the latest schema.
- Since the data is copied anyway, the copy can use a consistent format.
- Immutable dumps fit formats such as Avro object container files.
- Analytical copies may use column-oriented formats such as Parquet.

Interview hook: "Data outlives code" explains why schema evolution is not just a deployment concern; it is a storage-lifetime concern.

## 12. Dataflow Through Services: REST and Web Services

Source pages: 204-207

- Services expose network APIs.
- Clients make requests to servers.
- The server API is application-specific unless it is a standardized web protocol.
- Services are similar to databases in that clients can submit and query data, but services expose only operations allowed by application logic.
- This gives encapsulation: services can control what clients may do.

Microservices/evolvability goal:

- Teams should be able to deploy services independently.
- Old and new clients and servers may coexist.
- Encodings and APIs must remain compatible across versions.

Web services:

- A web service uses HTTP as the underlying protocol.
- Use cases include:
  - Device/browser clients calling a backend.
  - Internal service-to-service calls.
  - Cross-organization backend API calls.

REST:

- REST builds on HTTP.
- It emphasizes:
  - Simple data formats.
  - URLs for resources.
  - HTTP cache control.
  - Authentication.
  - Content type negotiation.

IDLs and frameworks:

- Clients must know endpoints, request formats, and response formats.
- Service definitions can document and evolve endpoints and data models.
- OpenAPI is commonly used for JSON web services.
- Protocol Buffers is used for gRPC service definitions.
- Frameworks such as FastAPI, Spring Boot, and gRPC help implement routing, metrics, caching, authentication, and business logic boundaries.
- Service definitions may be generated from code or used to generate server/client scaffolding.
- IDL tooling can generate client libraries, documentation, compatibility checks, and test UIs.

Interview hook: REST is not "no schema." Practical service evolution still needs a precise API contract, often through an IDL.

## 13. Problems With Remote Procedure Calls

Source pages: 207-208

- RPC tries to make a remote network request look like a local function or method call.
- The chapter calls this abstraction fundamentally flawed because networks behave unlike local calls.

Key differences:

- Network requests are unpredictable:
  - Requests or responses can be lost.
  - Remote machines can be slow or unavailable.
- A network timeout creates uncertainty:
  - The caller does not know whether the request was processed.
- Retrying can duplicate side effects:
  - If the original request succeeded but the response was lost, a retry may perform the action again.
  - Protocols need deduplication or idempotence.
- Latency is slower and more variable than local calls.
- Parameters must be encoded into bytes instead of passed by pointer/reference.
- Client and server may use different programming languages with different data types.

REST contrast:

- Part of REST's appeal is that it treats network state transfer as distinct from local function calls.

Interview hook: The RPC fallacy is hiding network uncertainty. Good API design makes failure, timeout, retry, and idempotence explicit.

## 14. Load Balancing, Service Discovery, and Service Meshes

Source pages: 208-210

- Clients need to know where a service is running; this is service discovery.
- A fixed IP and port is simple but brittle when servers fail, move, or overload.
- Multiple service instances improve availability and scalability, and requests must be spread across them.

Approaches:

- Hardware load balancers:
  - Datacenter appliances that route one host/port to service instances.
  - Can detect downstream failures and shift traffic.
- Software load balancers:
  - Applications such as NGINX or HAProxy.
  - Similar behavior without specialized hardware.
- DNS:
  - Maps a domain name to one or more IP addresses.
  - Works broadly but may cache stale addresses and propagate changes slowly.
- Service discovery systems:
  - Use registries such as etcd or ZooKeeper.
  - Services register host, port, and metadata.
  - Heartbeats signal availability.
  - Clients query the registry and then connect directly.
  - Better for dynamic environments than DNS.
- Service meshes:
  - Combine software load balancing and discovery.
  - Often deployed as a client library, process, or sidecar.
  - Can handle encryption, certificates, observability, failure detection, and traffic tracking at the mesh layer.

Tradeoff:

- Service meshes are sophisticated but complicated.
- Dynamic orchestrated environments may choose meshes.
- Databases or messaging systems may need purpose-built load balancers.
- Simpler deployments may be better served by software load balancers.

Interview hook: Service discovery is part of evolvability because independently deployed services must still find healthy compatible peers.

## 15. Data Encoding and Evolution for RPC

Source pages: 210-211

- RPC clients and servers should be independently deployable.
- Compared with databases, service dataflow can often assume servers are upgraded first and clients second.
- Therefore:
  - Requests need backward compatibility.
  - Responses need forward compatibility.

Encoding-specific rules:

- gRPC with Protocol Buffers follows Protocol Buffers compatibility rules.
- Avro RPC follows Avro compatibility rules.
- REST APIs often use JSON responses and JSON or URI/form-encoded requests.
- Adding optional request parameters and adding response fields are usually compatible.

Hard problem:

- APIs often cross organizational boundaries.
- Service providers may not control clients or force upgrades.
- Compatibility may need to be maintained for a very long time.
- Breaking changes often require running multiple API versions side by side.

Versioning:

- There is no universal API versioning scheme.
- REST approaches include:
  - Version number in URL.
  - Version in HTTP Accept header.
  - Server-side API version selection associated with API keys.

Interview hook: Public API compatibility has a social/operational dimension: clients you do not control may never upgrade.

## 16. Durable Execution and Workflows

Source pages: 211-213

- Service-based applications often require a sequence of service calls.
- A workflow is a graph or sequence of tasks.
- Example: payment processing may include fraud detection, credit card debit, and bank deposit.
- Workflow definitions may be written in:
  - General-purpose languages.
  - Domain-specific languages.
  - Markup languages such as BPEL.

Terminology:

- Some workflow systems call tasks activities or durable functions.
- A workflow engine runs workflows.
- The orchestrator schedules tasks.
- The executor executes tasks.
- Workflows may be triggered by time schedules, services, external sources, or humans.

Types of workflow engines:

- Data orchestration tools for ETL.
- Graphical workflow systems for easier non-engineer workflow definition.
- Durable execution systems for transaction-like service workflows.

Durable execution:

- Tries to provide exactly-once semantics for workflows.
- If a task fails, the framework may re-execute it while skipping RPCs or state changes that already succeeded.
- It can return prior successful results by logging RPCs and state changes to durable storage.

Limitations:

- External services must still provide idempotent APIs.
- Developers must use unique IDs to prevent duplicate external execution.
- Logged call order makes code changes brittle.
- Reordering calls may create undefined behavior during replay.
- Safer approach: deploy a new workflow code version for new executions while existing executions keep old code.
- Deterministic replay is required:
  - Random number generators and system clocks can be problematic.
  - Frameworks may provide deterministic alternatives and static analysis checks.

Interview hook: Durable execution does not magically make external side effects safe. It moves the problem into logs, replay, deterministic code, and idempotent boundaries.

## 17. Event-Driven Architectures

Source pages: 213-215

- In event-driven systems, requests are called events or messages.
- Unlike RPC, the sender usually does not wait for the recipient to process the message.
- Messages often pass through a message broker rather than a direct connection.

Benefits of a broker:

- Buffers messages if recipients are unavailable or overloaded.
- Redelivers messages after consumer crashes.
- Avoids direct service discovery by senders.
- Sends the same message to multiple recipients.
- Decouples sender from recipient.

Communication model:

- Usually asynchronous: send and continue.
- A synchronous RPC-like pattern can be built by waiting for a response on another channel.

Message broker patterns:

- Queue:
  - Producer adds to a named queue.
  - One consumer receives each message.
- Topic:
  - Producer publishes to a named topic.
  - All subscribers receive the message.

Encoding:

- Brokers usually see messages as bytes plus metadata.
- They typically do not enforce a data model.
- Common encodings include Protocol Buffers, Avro, or JSON.
- A schema registry can store valid schema versions and check compatibility.
- AsyncAPI can specify message schemas.

Durability:

- Many brokers write messages to disk.
- Many delete messages after consumption.
- Some can retain messages indefinitely, which is useful for event sourcing.

Important edge case:

- If a consumer republishes messages, it must preserve unknown fields to avoid losing data added by newer producers.

Interview hook: Event-driven architecture decouples time and topology, but schema evolution still matters because producers and consumers upgrade independently.

## 18. Distributed Actor Frameworks

Source pages: 215

- The actor model encapsulates logic in actors rather than sharing state across threads.
- Each actor usually represents a client or entity.
- Actors have local state and communicate through asynchronous messages.
- Each actor processes one message at a time, reducing thread-safety concerns.
- Message delivery is not guaranteed in some error scenarios.

Distributed actors:

- Frameworks such as Akka, Orleans, and Erlang/OTP extend actors across nodes.
- The same message-passing model is used whether actors are local or remote.
- Remote messages are encoded into bytes, sent over the network, and decoded.

Why location transparency works better here than in RPC:

- Actors already assume asynchronous messaging and possible message loss.
- The mismatch between local and remote communication is less severe than when pretending a network call is a local function call.

Compatibility concern:

- Rolling upgrades still require forward and backward compatible message formats because old and new nodes may exchange messages.

Interview hook: Actor location transparency is more plausible than RPC location transparency because the model already accepts asynchronous, lossy communication.

## Chapter-Level Memory Hooks

- Evolvability: design so code and data formats can change without upgrading everything at once.
- Backward compatibility: new code reads old data.
- Forward compatibility: old code reads new data.
- Unknown fields: must often be preserved, not merely ignored.
- Encoding boundary: in-memory structures become byte sequences for storage/network.
- Language-specific serialization: convenient, but risky for interoperability, security, versioning, and efficiency.
- JSON/XML/CSV: widely adopted, but weak around numbers, binary data, and schema precision.
- JSON Schema: powerful validation and content models, but complex to evolve.
- Binary JSON: compactness is limited if field names remain.
- Protocol Buffers: compact schema-based encoding using stable numeric field tags.
- Avro: compact schema-based encoding using writer/reader schema resolution, not tags.
- Schema registry: documentation plus compatibility gate.
- Data outlives code: old stored data can survive many application versions.
- REST/web services: APIs need contracts even when using simple formats.
- RPC danger: remote calls are not local calls because networks fail ambiguously.
- Service discovery: clients need healthy endpoints in dynamic deployments.
- Durable execution: exactly-once-like workflow execution depends on logs, replay, determinism, and idempotent external APIs.
- Message broker: asynchronous buffer and decoupling layer.
- Distributed actors: message passing scales across nodes, but encoded messages still need compatibility.

## Interview Perspective

- Start every answer by identifying the producer, consumer, and upgrade order.
- Define backward and forward compatibility precisely; do not treat them as vague "version compatibility."
- Mention rolling upgrades as the practical reason old and new code coexist.
- For encoding formats, compare:
  - Human readability.
  - Compactness.
  - Schema requirement.
  - Cross-language support.
  - Compatibility rules.
  - Unknown-field behavior.
- Protocol Buffers interview anchor:
  - Field tags are stable.
  - New fields get new tags.
  - Removed tags are not reused.
  - Unknown fields are skipped/preserved.
- Avro interview anchor:
  - Writer's schema is required for decoding.
  - Reader's schema expresses what the application expects.
  - Defaults are essential for adding/removing fields compatibly.
  - Schema distribution is part of the system design.
- Database evolution anchor:
  - Backward compatibility is mandatory because future code reads old data.
  - Forward compatibility appears during rolling upgrades.
  - Large data migrations are expensive because data outlives code.
- Service/API evolution anchor:
  - Public APIs may need indefinite compatibility.
  - Breaking changes often mean multiple live API versions.
- Workflow anchor:
  - Durable execution helps with service failure but requires deterministic replay and idempotent external calls.
- Event-driven anchor:
  - Brokers decouple sender and receiver, but messages remain long-lived contracts.

## Final Takeaways

- Encoding choices affect architecture, not just byte size.
- Evolvable systems assume old and new code and data will coexist.
- Forward compatibility is often the harder direction because old code must tolerate future additions.
- Schema-based binary formats such as Protocol Buffers and Avro provide compact encodings with clearer compatibility semantics than ad hoc text formats.
- Protocol Buffers relies on stable numeric tags; Avro relies on writer/reader schema resolution.
- Schema registries are useful because they document schemas and allow compatibility checks before deployment.
- Databases require compatibility across long-lived stored data; services require compatibility across independently deployed clients and servers; event systems require compatibility across asynchronous producers and consumers.
- RPC abstractions fail when they hide network uncertainty; robust designs expose timeout, retry, idempotence, and compatibility concerns.
- Durable workflows and event-driven systems add reliability patterns, but they do not remove the need for careful schema evolution.

Confidence: High
