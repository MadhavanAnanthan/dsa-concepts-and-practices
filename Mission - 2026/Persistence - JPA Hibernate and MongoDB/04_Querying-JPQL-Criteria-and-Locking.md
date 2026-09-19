# Querying: JPQL, Criteria, SQL, Projections, and Locking

## Choose the simplest tool that keeps the query clear

| Tool | Best fit |
|---|---|
| `find` / repository by ID | primary-key lookup |
| Derived repository method | simple, stable predicates |
| JPQL/HQL | readable entity-oriented static query |
| Criteria API / Specification | composable dynamic predicates |
| Query by Example | simple user-provided matching; limited joins/logic |
| Native SQL | vendor features, tuned SQL, CTE/window/reporting work |
| jOOQ/JdbcClient | strongly SQL-centric read/write path |

JPQL means **Jakarta Persistence Query Language**, not “Java Persistent API.” It queries entities and their attributes, not table and column names.

```java
List<Course> courses = entityManager.createQuery("""
    select c
    from Course c
    where c.students is empty
    order by c.name
    """, Course.class).getResultList();
```

Always bind parameters. Never concatenate user input into JPQL or SQL.

## Joins versus fetch joins

```java
// Join restricts the result; association may remain unloaded.
select o from PurchaseOrder o join o.customer c where c.email = :email

// Fetch join loads the association as a side effect.
select distinct o from PurchaseOrder o
left join fetch o.lines
where o.id = :id
```

A collection fetch join plus pagination is dangerous: row multiplication can make database-level paging incorrect or force in-memory work. Page root IDs first, then fetch their graph, or use projections/batch fetching.

## DTO projections

```java
public record OrderSummary(long id, String customerName, BigDecimal total) {}

select new com.example.OrderSummary(o.id, o.customer.name, o.total)
from PurchaseOrder o
where o.status = :status
```

Use projections for read-only screens and APIs that need a small shape. Loading entire entities just to serialize three columns increases memory, dirty-check work, and accidental lazy loads.

## Criteria and specifications

Criteria is not inherently “easier than SQL.” Its advantage is type-aware, composable dynamic query construction.

```java
Specification<Order> customerEmail(String email) {
    return (root, query, cb) ->
        email == null ? cb.conjunction()
                      : cb.equal(root.get("customer").get("email"), email);
}
```

For compile-time-safe paths, consider the JPA static metamodel or Querydsl. Keep query construction separate from domain mutation.

## Named and native queries

- `@NamedQuery` defines JPQL with an application-wide name and can fail fast during startup.
- `@NamedNativeQuery` defines SQL and needs explicit result mapping when the result does not match an entity.
- Spring Data's `@Query` keeps query text near the repository method.
- Native queries reduce portability; use them deliberately, not apologetically.

## Bulk update/delete warning

```java
int count = entityManager.createQuery("""
    update Account a set a.status = :newStatus where a.lastLogin < :cutoff
    """).executeUpdate();
entityManager.clear();
```

Bulk JPQL bypasses managed entity state, callbacks, and normal dirty checking. The persistence context may now contain stale entities; clear it or isolate the operation. In Spring Data, use `@Modifying` and choose `clearAutomatically`/`flushAutomatically` consciously.

## Optimistic and pessimistic locking

`@Version` enables optimistic locking. Updates include the previously read version; zero affected rows becomes `OptimisticLockException`. This detects a lost update and the caller should retry the **whole business operation** only when retry is safe.

```mermaid
sequenceDiagram
    participant A as Request A
    participant B as Request B
    participant DB as Database version=4
    A->>DB: read row v4
    B->>DB: read row v4
    A->>DB: update ... where version=4 -> v5
    B->>DB: update ... where version=4
    DB-->>B: 0 rows; optimistic conflict
```

Pessimistic locks (`PESSIMISTIC_READ/WRITE`) ask the database to lock rows. They may reduce conflicts but introduce blocking, deadlocks, and timeout handling. Keep transactions short and lock resources in a consistent order.
