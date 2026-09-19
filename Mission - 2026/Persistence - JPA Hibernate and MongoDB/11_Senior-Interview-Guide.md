# Senior Java Persistence Interview Guide

## Explain the stack in 60 seconds

“JDBC is the low-level database API. JPA, now Jakarta Persistence, standardizes ORM concepts such as entities, persistence contexts, lifecycle, JPQL, and relationships. Hibernate is a JPA provider that implements those rules and generates/executes SQL through JDBC. Spring Data JPA sits above JPA and generates repository implementations, derived queries, paging, and auditing integration. I still design database constraints, indexes, transaction boundaries, fetch plans, and inspect generated SQL.”

## High-value questions and answers

### What is a persistence context?

An identity map and unit of work managed through `EntityManager`. For one entity type/ID it maintains one managed instance, tracks lifecycle/state changes, and synchronizes them during flush. It is not thread-safe and should not be shared across threads.

### `persist` versus `merge`?

`persist` makes a new instance managed. `merge` copies state from a new/detached instance into a managed instance and returns that managed instance; the input remains detached. Blind merge of web payloads risks overwriting fields with stale/null values.

### Flush versus commit?

Flush sends pending SQL so the persistence context and database transaction are synchronized. Commit finalizes the database transaction. A flush can fail and a later rollback can undo its SQL.

### What causes an update without calling `save`?

A managed entity changes inside a persistence context. The provider compares state or tracks modifications and issues SQL at flush. `@Transactional` supplies the usual boundary; dirty checking is the ORM behavior.

### Explain owning side and `mappedBy`.

The owning side writes the foreign key or join-table rows. `mappedBy` is placed on the inverse side and names the owning entity's Java field/property. Updating only the inverse collection may not update the database relationship.

### How do you solve N+1?

Confirm it through query counts, then select the fetch plan per use case: fetch join/entity graph for bounded graphs, DTO projection for read models, batch fetching for multiple lazy associations, or query/aggregate redesign. Simply changing everything to EAGER is not a solution.

### Optimistic versus pessimistic locking?

Optimistic locking with `@Version` detects conflicting updates at write time and works well when conflicts are uncommon. Pessimistic locking asks the database to lock rows and is appropriate when conflicts are frequent/costly, but can block, deadlock, and reduce throughput.

### First-level versus second-level cache?

First level is mandatory per persistence context and ensures identity. Second level is optional and shared across contexts, requires provider/cache configuration, and needs explicit consistency/eviction choices. Spring method caching is a separate layer.

### Why disable Open EntityManager in View?

It exposes lazy database access to controllers/serializers, makes query counts unpredictable, and couples API rendering to persistence. Fetch required data in a transactional service/query and return DTOs.

### Why is `@Transactional` sometimes ignored?

Spring proxy interception was bypassed (self-invocation/private method/manual construction), the wrong annotation/manager was used, exception rules did not trigger rollback, or async/thread boundaries lost the context.

### Is JPA suitable for batch processing?

Yes for moderate batches when using JDBC batching and periodic flush/clear, but normal managed entities are expensive at very high volume. Use bulk SQL, `JdbcTemplate`, jOOQ, database loaders, or Hibernate stateless/bulk facilities when appropriate.

### ORM versus ODM?

ORM maps objects to relational tables and joins; ODM maps objects/aggregates to documents. With MongoDB I design from query/aggregate boundaries, embed bounded owned data, reference independent data, and do not expect JPA persistence-context or cascade semantics.

## Scenario questions

### Two requests update the same balance

Start with a database constraint/invariant, `@Version`, and a retry policy around the complete idempotent use case. For highly contended funds movement, consider conditional SQL updates or pessimistic locking. Use an immutable ledger rather than treating one mutable balance as the only record of truth.

### Endpoint returns orders and lines slowly

Measure SQL and rows. Look for N+1, cartesian multiplication, missing indexes, deep offset pagination, and serialization-triggered loads. A paged order summary DTO plus a separate detail query is often better than fetching a large entity graph.

### Need to publish an event after saving

Do not rely on “save then send.” Write business data and an outbox row atomically, publish later, and make the consumer idempotent. Transaction-synchronized callbacks without a durable outbox can lose events after process failure.

### Need MongoDB references like JPA relationships

Re-evaluate the document boundary. Embed owned bounded values; otherwise store IDs or use `@DocumentReference` intentionally. There is no automatic foreign-key integrity/cascade equivalent, so enforce lifecycle and cleanup in application workflows.

## Red flags to mention proactively

- Entity exposure from REST controllers.
- `CascadeType.ALL` everywhere.
- EAGER everywhere or “LAZY fixes N+1.”
- `ddl-auto=update` in production.
- H2-only persistence testing.
- Unbounded `findAll()` or collections.
- Business transactions spanning slow network calls.
- Retrying non-idempotent work blindly.
- Adding a cache before measuring database/query behavior.
- Treating generated SQL as somebody else's concern.

## Final revision checklist

You should be able to draw and explain:

1. Spring Data -> JPA -> Hibernate -> JDBC -> database.
2. Entity lifecycle and `merge` semantics.
3. Relationship ownership and a join entity.
4. Flush versus commit.
5. N+1 and four legitimate remedies.
6. Isolation anomalies and optimistic locking.
7. First/second-level versus service caching.
8. ORM versus MongoDB ODM modeling.
9. Migration and outbox patterns.
10. A measured debugging approach using SQL, plans, metrics, and production-like tests.
