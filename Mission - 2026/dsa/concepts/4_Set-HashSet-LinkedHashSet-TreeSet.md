# Set, HashSet, LinkedHashSet, TreeSet - Master Notes (Java + DSA + Interview)

---

# 1. Collection Hierarchy

```text
Iterable
   ↑
Collection
   ↑
Set
```

Implementations:

```text
Set
 ├── HashSet
 ├── LinkedHashSet
 └── TreeSet
```

---

# 2. What is Set?

`Set` is an interface.

It inherits common operations from `Collection`.

Common methods:

```java
add()
remove()
contains()
size()
isEmpty()
clear()
iterator()
```

---

## Primary Contract of Set

```text
No Duplicate Elements
```

Example:

```java
Set<Integer> set = new HashSet<>();

set.add(10);
set.add(20);
set.add(10);
```

Result:

```text
[10,20]
```

Duplicate value is ignored.

---

# 3. Set Characteristics

## Allows

```text
Unique Elements
```

---

## Does Not Guarantee

```text
Ordering
Sorting
Thread Safety
```

All these depend on the implementation.

---

# 4. Set vs List

## List

```text
Duplicates Allowed
Ordered
Index Based
```

Example:

```text
[10,20,10]
```

Valid.

---

## Set

```text
Duplicates Not Allowed
No Index Concept
```

Example:

```text
[10,20]
```

Only one 10 exists.

---

# 5. Does Set Use HashMap Internally?

No.

`Set` is only an interface.

Interfaces do not store data.

Example:

```text
Set
 ├── HashSet
 ├── LinkedHashSet
 └── TreeSet
```

Each implementation decides how storage works.

---

# 6. HashSet

## Purpose

```text
Unique Elements
Fastest Lookup
No Ordering Guarantee
```

---

## Internal Structure

HashSet internally uses:

```java
HashMap<E,Object>
```

---

### Internal Idea

```java
set.add("A");
set.add("B");
```

Internally becomes roughly:

```java
map.put("A", PRESENT);
map.put("B", PRESENT);
```

where:

```java
private static final Object PRESENT =
        new Object();
```

---

## Why Use HashMap Internally?

Java designers reused:

```text
Hashing Logic
Bucket Management
Collision Handling
Resize Logic
Treeification
Fail-Fast Logic
```

instead of implementing everything twice.

This follows:

```text
Code Reuse
Composition
Delegation
DRY Principle
```

---

## Is Memory Wasted?

Technically yes.

Each entry stores:

```text
Key
Dummy Value
Hash
Pointer Information
```

However:

```java
PRESENT
```

is a single shared object.

Java does NOT create a new dummy object per element.

---

## Can We Access PRESENT?

No.

HashSet exposes only:

```java
add()
remove()
contains()
```

Internal dummy values are hidden.

---

# 7. How HashSet Search Works

Example:

```java
set.contains("A");
```

---

## Step 1

Calculate:

```java
hashCode()
```

Example:

```java
"A".hashCode()
```

---

## Step 2

Find bucket.

Conceptually:

```java
bucket = hash % capacity
```

---

## Step 3

Search only inside that bucket.

Entire collection is NOT scanned.

This is why lookup is very fast.

---

# 8. HashSet Complexity

Average Case:

```text
add()       O(1)
remove()    O(1)
contains()  O(1)
```

---

Worst Case:

```text
O(n)
```

if too many collisions occur.

---

Java 8+

Heavy collision chains can become:

```text
Red Black Tree
```

making lookups:

```text
O(log n)
```

instead of O(n).

---

# 9. HashSet Ordering

HashSet provides:

```text
NO insertion order
NO sorting order
```

Example:

```java
set.add(30);
set.add(10);
set.add(20);
```

Output may be:

```text
[20,30,10]
```

Order is not guaranteed.

---

# 10. HashSet Null Handling

Allowed.

```java
set.add(null);
```

Valid.

Only one null exists because duplicates are not allowed.

---

# 11. HashSet Thread Safety

```text
❌ Not Thread Safe
```

Concurrent modifications can cause problems.

Thread-safe alternatives:

```java
Collections.synchronizedSet(...)
```

or

```java
ConcurrentHashMap.newKeySet()
```

---

# 12. LinkedHashSet

## Purpose

```text
Unique Elements
+
Insertion Order Preservation
```

---

## Example

```java
set.add(30);
set.add(10);
set.add(20);
```

Output:

```text
[30,10,20]
```

Exactly same insertion order.

---

# 13. Internal Structure

LinkedHashSet internally uses:

```java
LinkedHashMap
```

---

### LinkedHashMap Internals

```text
Hash Table
+
Doubly Linked List
```

Hash Table:

```text
Fast Lookup
```

Linked List:

```text
Maintains Insertion Order
```

---

# 14. Complexity

Average:

```text
add()       O(1)
contains()  O(1)
remove()    O(1)
```

Slightly more memory than HashSet because of linked list pointers.

---

# 15. Null Handling

Allowed.

```java
set.add(null);
```

Valid.

Only one null allowed.

---

# 16. Thread Safety

```text
❌ Not Thread Safe
```

Thread-safe option:

```java
Collections.synchronizedSet(...)
```

---

# 17. TreeSet

## Purpose

```text
Unique Elements
+
Sorted Elements
```

---

## Example

```java
set.add(30);
set.add(10);
set.add(20);
```

Output:

```text
[10,20,30]
```

Automatically sorted.

---

# 18. Internal Structure

TreeSet uses:

```text
Red Black Tree
```

which is a:

```text
Self Balancing Binary Search Tree
```

---

## Important

TreeSet does NOT use:

```text
HashMap
Hashing
Buckets
```

This is a common interview question.

---

# 19. Sorting Rules

Default sorting uses:

```java
Comparable
```

Examples:

```java
Integer
String
LocalDate
```

already implement Comparable.

---

## Custom Sorting

Use Comparator.

Example:

```java
TreeSet<Integer> set =
    new TreeSet<>(
        Comparator.reverseOrder()
    );
```

Output:

```text
30
20
10
```

---

# 20. Complexity

Because tree traversal is required:

```text
add()       O(log n)
contains()  O(log n)
remove()    O(log n)
```

Slower than HashSet.

---

# 21. Null Handling

Java 8+

```java
set.add(null);
```

Throws:

```java
NullPointerException
```

Reason:

TreeSet must compare elements.

```java
null.compareTo(...)
```

is impossible.

---

# 22. Thread Safety

```text
❌ Not Thread Safe
```

Thread-safe option:

```java
Collections.synchronizedSortedSet(...)
```

---

# 23. Fail-Fast Behavior

All standard Set implementations are Fail-Fast.

Examples:

```java
HashSet
LinkedHashSet
TreeSet
```

---

Example:

```java
for(Integer n : set){
    set.add(100);
}
```

May throw:

```java
ConcurrentModificationException
```

---

## Why?

Not because of:

```text
❌ Performance
❌ Immutability
❌ Array Shifting
```

Actual reason:

```text
Prevent unpredictable iteration results.
```

Collection structure changed while iterator was reading it.

Fail Fast:

```text
Better than producing incorrect result.
```

---

# 24. equals() and hashCode()

Most Important Topic for HashSet.

HashSet determines duplicates using:

```java
hashCode()
equals()
```

Both are important.

---

## Duplicate Detection

### Step 1

Compare hashCode.

### Step 2

If hashCode matches:

```java
equals()
```

is checked.

---

## Interview Rule

If two objects are equal:

```java
obj1.equals(obj2)
```

then:

```java
obj1.hashCode() == obj2.hashCode()
```

must also be true.

---

# 25. When to Use Which Set?

## HashSet

Use when:

```text
Need Fastest Lookup
Order Doesn't Matter
```

Examples:

```text
Duplicate Detection
Visited Nodes
Cache Membership Check
```

---

## LinkedHashSet

Use when:

```text
Need Fast Lookup
+
Insertion Order
```

Examples:

```text
Recently Visited Pages
Unique Ordered Items
Deduplicate While Preserving Order
```

---

## TreeSet

Use when:

```text
Need Sorted Data
```

Examples:

```text
Leaderboard Ranking
Sorted Users
Top Elements
Range Queries
```

---

# 26. Thread-Safe Alternatives

## HashSet

```java
Collections.synchronizedSet(...)
```

or

```java
ConcurrentHashMap.newKeySet()
```

---

## LinkedHashSet

```java
Collections.synchronizedSet(...)
```

---

## TreeSet

```java
Collections.synchronizedSortedSet(...)
```

or

```java
ConcurrentSkipListSet
```

---
27. ## Composition and Delegation

HashSet does NOT inherit from HashMap.

Wrong:

```text
HashSet IS-A HashMap
```

Correct:

```text
HashSet HAS-A HashMap
```

Simplified structure:

```java
class HashSet<E> {
    private transient HashMap<E, Object> map;
}
```

This is called:

```text
Composition
```

because HashSet owns a HashMap instance.

---

## What is Delegation?

Delegation means:

```text
Instead of doing the work itself,
an object forwards the work to another object.
```

HashSet does not implement:

```text
Hashing
Bucket Calculation
Collision Handling
Resize Logic
Treeification
```

it delegates those responsibilities to HashMap.

Example:

```java
set.add("A");
```

Internally:

```java
map.put("A", PRESENT);
```

Flow:

```text
User
 ↓
HashSet.add("A")
 ↓ Delegates
HashMap.put("A", PRESENT)
```

Similarly:

```text
HashSet.contains()
    ↓ delegates to
HashMap.containsKey()

HashSet.remove()
    ↓ delegates to
HashMap.remove()
```

---

## Why Not Inheritance?

Technically Java could have used:

```java
class HashSet extends HashMap
```

because HashMap already contains working implementations.

However this would expose unwanted APIs:

```java
put()
get()
entrySet()
keySet()
values()
```

to HashSet users.

A Set should expose only:

```java
add()
remove()
contains()
```

Therefore Java designers reused HashMap's implementation without exposing HashMap's API.

This is a classic example of:

```text
Composition Over Inheritance
```

where:

```text
Composition = HashSet HAS-A HashMap

Delegation = HashSet forwards work to HashMap
```

# 28. Engineering Lesson

HashSet internally using HashMap demonstrates an important software engineering principle:

```text
Reuse Stable Components
Instead Of Duplicating Logic
```

Java designers reused:

```text
Hashing
Buckets
Collisions
Resize Logic
Treeification
```

already solved by HashMap.

This follows:

```text
DRY Principle
Maintainability
Code Reuse
Composition Over Duplication
```

A common enterprise principle:

```text
A well-tested reusable solution is often better
than writing a new optimized solution from scratch.
```

---

# Final Interview Summary

## HashSet

```text
Unique Values

Internal:
HashMap

Ordering:
None

Null:
Allowed (One)

Complexity:
add()      O(1)
contains() O(1)
remove()   O(1)

Thread Safe:
No
```

---

## LinkedHashSet

```text
Unique Values

Internal:
LinkedHashMap

Ordering:
Insertion Order

Null:
Allowed (One)

Complexity:
add()      O(1)
contains() O(1)
remove()   O(1)

Thread Safe:
No
```

---

## TreeSet

```text
Unique Values

Internal:
Red Black Tree

Ordering:
Sorted

Null:
Not Allowed

Complexity:
add()      O(log n)
contains() O(log n)
remove()   O(log n)

Thread Safe:
No
```

---

# Checklist

✅ Set Hierarchy

✅ Set Contract

✅ HashSet Internals

✅ HashMap Backing

✅ PRESENT Dummy Object

✅ Hashing Basics

✅ Bucket Concept

✅ Complexities

✅ Hash Collisions

✅ equals()

✅ hashCode()

✅ LinkedHashSet Internals

✅ Insertion Order

✅ TreeSet Internals

✅ Red Black Tree

✅ Comparable

✅ Comparator

✅ Null Handling

✅ Fail-Fast

✅ Thread Safety

✅ Thread-Safe Alternatives

✅ Software Engineering Lessons (Reuse over Duplication)
