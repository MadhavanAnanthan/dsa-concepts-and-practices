# Java Map Interfaces & Internals — Final Interview Notes

> Focus: implementation details, concurrency, ordering, collisions, and interview-level reasoning.  
> Basic `Map<K,V>` usage is intentionally omitted.

---

# 1. Map Implementations — What to Remember

| Implementation | Internal idea | Ordering | Thread-safe | Null key | Null value | Typical complexity |
|---|---|---|---|---|---|---|
| `HashMap` | Hash table + bins | No guaranteed order | No | One | Yes | `O(1)` average |
| `LinkedHashMap` | HashMap + doubly-linked entry order | Insertion/access order | No | One | Yes | `O(1)` average |
| `TreeMap` | Red-black tree | Sorted by key | No | Normally no* | Yes | `O(log n)` |
| `Hashtable` | Legacy synchronized hash table | No guaranteed order | Yes | No | No | `O(1)` average |
| `ConcurrentHashMap` | Concurrent hash table | No guaranteed order | Yes | No | No | `O(1)` average |
| `ConcurrentSkipListMap` | Concurrent skip-list | Sorted by key | Yes | No | No | `O(log n)` |

\* A `TreeMap` using a custom comparator can support `null` only if that comparator explicitly knows how to compare it. With normal natural ordering, `null` keys are not allowed.

---

# 2. HashMap — Internal Mental Model

Think of `HashMap` as an **array of bins/buckets**.

`bin` and `bucket` mean effectively the same thing in this context.

```text
table[]
 |
 +-- [0] -> null
 +-- [1] -> Node
 +-- [2] -> Node -> Node -> Node
 +-- [3] -> TreeNode structure
 +-- [4] -> null
```

A bucket can be:

```text
empty
OR
a Node / linked Node chain
OR
a treeified red-black-tree structure
```

Even if a bucket contains only one mapping, there is still a `Node`-like entry:

```text
bucket 5
   |
   v
Node
 |- hash
 |- key
 |- value
 |- next = null
```

Conceptually:

```java
Node<K,V> {
    int hash;
    K key;
    V value;
    Node<K,V> next;
}
```

If collisions occur:

```text
bucket 5
   |
   v
Node A -> Node B -> Node C
```

The whole `HashMap` never becomes a tree. Only an individual heavily-collided bin can be treeified.

---

# 3. How HashMap Uses `hashCode()` and `equals()`

For:

```java
map.put(key, value);
```

the simplified flow is:

```text
key.hashCode()
      |
      v
HashMap spreads/transforms the hash
      |
      v
bucket index is calculated
      |
      v
inspect that bucket
      |
      +-- matching key -> replace/update value
      |
      +-- different key -> collision handling
```

A useful mental rule:

```text
hashCode() -> narrow down to a bucket
equals()   -> identify the actual matching key
```

`hashCode()` does **not** directly return the bucket number.

HashMap stores a transformed hash in its entry and calculates the array index from it.

---

# 4. Which Objects Can Be HashMap Keys?

Every Java object inherits:

```java
equals()
hashCode()
```

from `java.lang.Object`.

So technically almost any reference object can be used as a key.

For custom domain objects, however, meaningful key behavior usually requires correctly overriding both:

```java
equals()
hashCode()
```

Contract:

> If two objects are equal according to `equals()`, they must produce the same `hashCode()`.

Example flow for an `Employee` key:

```text
Employee
   |
   v
employee.hashCode()
   |
   v
bucket
   |
   v
employee.equals(existingKey)
```

### Important mutable-key problem

Avoid changing fields that participate in `equals()` / `hashCode()` while an object is being used as a HashMap key.

```text
insert key
  hash -> bucket 4

change key field
  new hash -> bucket 9

get(key)
  searches bucket 9

original entry is still in bucket 4
```

This can make the mapping appear "lost".

---

# 5. Does HashMap Itself Have `equals()` and `hashCode()`?

Yes, but those methods are **not used to locate entries inside that same map**.

For:

```java
map.put(employee, value);
```

HashMap uses:

```text
employee.hashCode()
employee.equals(...)
```

It does not use:

```text
map.hashCode()
map.equals(...)
```

The map's own `equals()` / `hashCode()` are useful when comparing a Map itself with another Map, or if a Map object is used as a key somewhere else.

---

# 6. Collision and Treeification

A collision means multiple different keys land in the same bucket.

```text
Key A ---> bucket 5
Key B ---> bucket 5
Key C ---> bucket 5
```

Collision does **not** mean duplicate key.

HashMap still distinguishes entries using their hash and equality.

Java 8+ can convert a heavily-collided linked bin into a red-black tree.

Typical implementation constants:

```text
TREEIFY_THRESHOLD     = 8
UNTREEIFY_THRESHOLD   = 6
MIN_TREEIFY_CAPACITY  = 64
```

The important part is understanding `MIN_TREEIFY_CAPACITY = 64`.

## Why 64?

Suppose one bucket has many collided entries.

If:

```text
table capacity < 64
```

HashMap generally prefers to **resize the table** rather than immediately treeify that bin.

Why?

Increasing the number of buckets may naturally redistribute the collided keys:

```text
Before resize

bucket 5 -> A -> B -> C -> D -> E -> F -> G -> H
```

After resize, some entries may move:

```text
bucket 5  -> A -> C -> E
bucket 21 -> B -> D -> F -> H
```

Once:

```text
table capacity >= 64
AND
the bin is sufficiently crowded
```

HashMap can treeify that individual bin.

So remember:

```text
Heavy collision + capacity < 64
        -> prefer resize

Heavy collision + capacity >= 64
        -> bin can be treeified
```

`64` refers to the **internal table capacity**, not "64 entries in that bucket" and not simply "map size = 64".

---

# 7. Capacity, Load Factor and Resize

Default concepts:

```text
initial capacity -> commonly 16
load factor      -> commonly 0.75
threshold        -> capacity × load factor
```

Example:

```text
capacity = 16
load factor = 0.75

threshold = 12
```

When size crosses the threshold, HashMap generally grows, commonly doubling its capacity.

The backing table is allocated lazily in modern implementations: creating an empty `HashMap` does not necessarily allocate the full bucket array immediately.

## Does HashMap shrink after removals?

No automatic shrink.

```java
map.clear();
```

removes mappings, but the previously grown internal table generally remains allocated while that HashMap is alive.

Garbage collection can reclaim removed key/value objects if nothing references them, but GC does **not** resize a live HashMap back to a smaller table.

---

# 8. HashMap Null Semantics

`HashMap` supports:

```text
one null key
multiple null values
```

Therefore:

```java
map.get(key) == null
```

is ambiguous:

```text
1. key does not exist
OR
2. key exists and is mapped to null
```

In ordinary single-threaded code this can be disambiguated with:

```java
map.containsKey(key)
```

because, assuming your code does not change the map in between, you can inspect the same stable state.

---

# 9. Check-Then-Act Race

This is unsafe as a concurrent compound operation:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

Possible interleaving:

```text
Thread 1: containsKey(A) -> false
Thread 2: containsKey(A) -> false

Thread 1: put(A, 10)
Thread 2: put(A, 20)
```

The problem is:

```text
check
+
act
```

are separate operations.

Even if individual operations are thread-safe, the combined decision is not automatically atomic.

Use atomic map operations where appropriate:

```java
putIfAbsent()
computeIfAbsent()
compute()
merge()
```

---

# 10. Hashtable — Why No Null Key or Value?

`Hashtable` is a legacy synchronized Map.

It rejects:

```text
null key
null value
```

For a null key, its hashing path expects a real key and invokes key hashing; a null key therefore cannot participate normally.

For values, disallowing null also gives:

```java
table.get(key) == null
```

one clear interpretation:

```text
no mapping exists
```

For modern high-concurrency code, `ConcurrentHashMap` is normally preferred over `Hashtable`.

---

# 11. Why ConcurrentHashMap Does Not Allow Null

This is more important than simply saying "null causes conflicts."

Suppose null values were allowed.

Then:

```java
map.get("A")
```

returning `null` could mean:

```text
A is absent
OR
A exists with value null
```

In a normal single-threaded HashMap, you might follow with:

```java
map.containsKey("A")
```

to distinguish the two.

In concurrent code, however, the map can change between those two observations:

```text
Thread 1                         Thread 2

get(A) -> null

                                 put(A, null)
                                 or remove(A)

containsKey(A) -> ?
```

So `get()` followed by `containsKey()` cannot reliably describe the state that existed at the instant of the original `get()`.

`ConcurrentHashMap` avoids that ambiguity completely:

```text
null values are impossible
```

Therefore:

```java
map.get(key) == null
```

has one meaning for that lookup:

```text
No mapping was observed for that key at that moment.
```

Another thread may insert the key immediately afterward, but the `get()` result itself is unambiguous.

Memory rule:

```text
HashMap:
get() returns null
-> absent OR mapped-to-null

ConcurrentHashMap:
null mappings are forbidden

therefore:
get() returns null
-> no mapping was observed
```

---

# 12. ConcurrentHashMap — High-Level Concurrency Model

For Java 8+, do **not** describe it simply as "segment locking".

That mainly describes older implementations.

Modern ConcurrentHashMap uses a combination of:

```text
volatile reads/writes
CAS
localized synchronization
special coordination during resize/tree operations
```

There is no single global lock protecting the entire map for normal operations.

---

# 13. ConcurrentHashMap Reads

Typical:

```java
map.get(key);
```

does not acquire a normal global lock.

Simplified flow:

```text
hash key
   |
find bin
   |
read node/tree
   |
return value
```

Reads are designed to proceed concurrently with other operations.

---

# 14. ConcurrentHashMap Write — Empty Bin

Suppose:

```text
table[5] = null
```

For a simple insertion, ConcurrentHashMap can use **CAS**.

CAS = Compare-And-Set.

Conceptually:

```text
"If table[5] is still null,
atomically replace it with my new Node."
```

```text
null
 |
 | CAS
 v
Node
```

If another thread changed that location first, the CAS fails and the operation follows the appropriate retry/update path.

So:

```text
empty target bin
-> CAS when possible
-> no normal synchronized block required for that simple insertion
```

---

# 15. ConcurrentHashMap Write — Occupied Bin

Suppose:

```text
bucket/bin 5

Node A -> Node B -> Node C
```

When modification of that occupied bin is required, Java 8+ may synchronize on the **first node/bin head**.

Conceptually:

```java
Node first = table[index];

synchronized (first) {
    // verify bin state
    // traverse/update this bin safely
}
```

Important:

The first node is the **lock object**, but the purpose of that lock is to protect modification of the **whole bin**, not only that one node.

```text
bucket 5

Node A -> Node B -> Node C
  ^
  |
common monitor used by writers of this occupied bin
```

Why use the first node?

Because all writers targeting that occupied bin need one common synchronization point.

If threads independently locked whichever node they happened to reach:

```text
Thread 1 locks Node B
Thread 2 locks Node C
```

both could modify links/state belonging to the same bin at the same time.

Using the bin head gives the bin's writers one agreed monitor.

It also fits naturally because bin traversal begins from that head.

Do **not** memorize:

> "It locks the first node because insertion happens at the end."

That is too narrow.

The stronger explanation is:

> The bin head acts as a common monitor so structural/value updates to that occupied bin are coordinated consistently.

---

# 16. What Does "Localized Synchronization" Mean?

Localized synchronization means:

> Lock only the affected internal area rather than locking the entire ConcurrentHashMap.

Example:

```text
Thread 1 -> bin 5
Thread 2 -> bin 9
```

They can often update concurrently:

```text
bin 5                         bin 9

Node A -> Node B              Node X -> Node Y
  ^                              ^
  |                              |
Thread 1                    Thread 2
```

But:

```text
Thread 1 -> occupied bin 5
Thread 2 -> occupied bin 5
```

may require one writer to wait for the other because both coordinate using that bin's monitor.

So your interview-level rule can be:

```text
Read
-> generally no normal lock

Empty target bin
-> CAS

Occupied target bin needing modification
-> localized/bin-level synchronization

Different bins
-> updates can often proceed concurrently

Same occupied bin
-> writers may block each other
```

Calling this **bin-level/bucket-level locking** is acceptable as a high-level explanation if you immediately clarify:

> It is not a separate lock object stored for every bucket; in common Java 8+ paths the first node/bin head can be used as the monitor.

---

# 17. ConcurrentHashMap and Tree Bins

If a heavily-collided ConcurrentHashMap bin is treeified, modifications still need coordination so that the tree remains structurally valid.

The details are more complex than the simple linked-bin case, so for interviews the important point is:

```text
tree bin
-> concurrent reads remain highly optimized
-> structural modifications require appropriate coordination
```

Do not reduce every internal path to "lock first Node"; tree bins and resizing have specialized internal mechanics.

---

# 18. ConcurrentHashMap Resize

Resize is also designed for concurrency.

A resize does not necessarily mean one thread must stop the world and transfer everything alone.

Multiple threads can help move ranges/bins from the old table into the new table.

Conceptually:

```text
Old table
   |
   +-- Thread A helps transfer
   +-- Thread B helps transfer
   +-- Thread C helps transfer
   |
   v
New larger table
```

This helps ConcurrentHashMap scale under load.

---

# 19. `computeIfAbsent()`

Example:

```java
map.computeIfAbsent(key, k -> load(k));
```

Meaning:

```text
Is there already a usable mapping for key?
        |
    +---+---+
    |       |
   Yes      No
    |       |
return    run mapping function
value       |
            v
       if result != null
          store it
            |
          return it
```

Example:

```java
map.computeIfAbsent(
    department,
    k -> new ArrayList<>()
).add(employee);
```

This replaces manual patterns such as:

```java
if (!map.containsKey(department)) {
    map.put(department, new ArrayList<>());
}
map.get(department).add(employee);
```

For `ConcurrentHashMap`, `computeIfAbsent()` performs the map-level absent/check-and-establish operation atomically with respect to competing updates for that key.

Important:

If the mapping function returns `null`, no mapping is established.

Also avoid putting long-running or side-effect-heavy work inside these atomic mapping operations without understanding the concurrency implications.

---

# 20. `putIfAbsent()`, `compute()`, and `merge()`

Useful atomic-style operations:

```java
map.putIfAbsent(key, value);
```

```text
Insert only if no mapping is present.
```

```java
map.compute(key, (k, oldValue) -> newValue);
```

```text
Recalculate mapping based on key + current value.
```

```java
map.merge(key, 1, Integer::sum);
```

Useful for counters:

```text
absent  -> insert 1
present -> combine old value with 1
```

These are preferable to manually splitting check/read/write logic when concurrent atomicity matters.

---

# 21. LinkedHashMap

Internal idea:

```text
Hash table
+
doubly-linked ordering between entries
```

Default behavior preserves insertion order.

```java
map.put("C", 3);
map.put("A", 1);
map.put("B", 2);
```

Iteration:

```text
C, A, B
```

It is **not sorted**.

Access-order mode:

```java
new LinkedHashMap<>(16, 0.75f, true);
```

allows recently accessed entries to move through the linked order and is useful as the basis of simple LRU-style caches.

Memory rule:

```text
HashMap       -> no guaranteed iteration order
LinkedHashMap -> insertion/access order
TreeMap       -> sorted key order
```

---

# 22. TreeMap

`TreeMap` is based on a red-black tree.

```text
put/get/remove -> O(log n)
```

It orders **keys**, not values.

It can use:

```text
natural ordering
OR
Comparator
```

Example:

```java
new TreeMap<>(Comparator.reverseOrder());
```

This changes ascending vs descending order; it does **not** by itself make null keys valid.

### Null behavior

Normal natural-order TreeMap:

```text
null key   -> not allowed
null value -> allowed
```

A custom comparator can explicitly support null:

```java
Comparator.nullsFirst(...)
```

So the precise rule is:

> Null-key support depends on whether the comparator can order null; natural ordering cannot.

Useful `NavigableMap` methods:

```java
lowerKey(k)    // greatest key < k
floorKey(k)    // greatest key <= k
higherKey(k)   // smallest key > k
ceilingKey(k)  // smallest key >= k
```

Also:

```java
headMap(...)
tailMap(...)
subMap(...)
```

---

# 23. `entrySet()` — What Actually Happens?

When you write:

```java
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    ...
}
```

you are **iterating mappings already stored in the map**.

Conceptually:

```text
table

bucket 0 -> Node(A,10)
bucket 1 -> null
bucket 2 -> Node(B,20) -> Node(C,30)

             |
             v

entrySet iteration

A=10
B=20
C=30
```

It is not doing a fresh:

```text
hashCode()
+
equals()
```

lookup for each entry.

The internal HashMap node represents the `Map.Entry<K,V>` concept, but application code should work against:

```java
Map.Entry<K,V>
```

not the internal `Node` class.

Therefore, if both key and value are required:

```java
for (Map.Entry<K,V> entry : map.entrySet())
```

is usually preferable to:

```java
for (K key : map.keySet()) {
    V value = map.get(key);
}
```

because `entrySet()` already exposes key and value together rather than performing an additional lookup per key.

Memory rule:

```text
put/get
-> hash + bucket + equality

entrySet iteration
-> traverse existing mappings
```

---

# 24. ConcurrentSkipListMap

Use when you need:

```text
thread safety
+
sorted keys
```

Typical operations:

```text
O(log n)
```

Mental comparison:

```text
ConcurrentHashMap
-> concurrent + unordered

ConcurrentSkipListMap
-> concurrent + sorted
```

---

# 25. Specialized Maps Worth Recognizing

### `WeakHashMap`

Keys are weakly referenced.

If a key no longer has a strong reference elsewhere, its mapping can become eligible for GC-related removal.

Useful for specialized metadata/cache scenarios, not general-purpose caching.

### `IdentityHashMap`

Uses:

```text
==
```

for key identity semantics instead of normal logical equality via `equals()`.

### `EnumMap`

Optimized for enum keys.

When the key type is an enum, `EnumMap` is usually preferable to a general HashMap.

---

# 26. Final Decision Rule

```text
Need fastest general lookup, no ordering
-> HashMap

Need insertion/access order
-> LinkedHashMap

Need sorted/range-based key operations
-> TreeMap

Need concurrent unordered map
-> ConcurrentHashMap

Need concurrent sorted map
-> ConcurrentSkipListMap
```

---

# 27. Interview Traps

### `HashMap` collision means duplicate key

Wrong.

```text
collision -> same bucket
duplicate -> keys compare equal
```

### Whole HashMap becomes a red-black tree

Wrong.

Only a sufficiently crowded individual bin can be treeified.

### `MIN_TREEIFY_CAPACITY = 64` means 64 entries

Wrong.

It refers to internal table capacity required before preferring treeification over resize.

### `hashCode()` decides equality

Wrong.

It helps locate a candidate bucket; equality confirms the actual key.

### `ConcurrentHashMap` locks the entire map

Wrong for normal Java 8+ operations.

It uses CAS, volatile mechanics, and localized coordination.

### Java 8+ ConcurrentHashMap uses segment locks

Outdated as a general explanation.

### Same occupied bucket means only one individual node is protected

Misleading.

The bin head may be the monitor object, but that monitor coordinates modification of the bin.

### First node is locked because insertion occurs at the end

Too narrow.

The main reason is to provide all writers to that bin a common monitor.

### `ConcurrentHashMap.get() == null` means value might be null

Wrong.

Null values are not allowed, so a null result means no mapping was observed for that lookup.

### `TreeMap` never allows null under any possible configuration

Too absolute.

Natural ordering does not support null keys; a custom comparator can explicitly define null ordering.

### `entrySet()` performs a new HashMap lookup for every entry

Wrong.

It iterates the mappings already stored in the structure.

---

# 28. 30-Second HashMap Explanation

> HashMap uses an internal array of bins. Each mapping is stored as a Node containing the transformed hash, key, value, and link to another entry when collisions occur. The key's `hashCode()` helps calculate the bin and `equals()` identifies the exact key within that bin. In Java 8+, a heavily-collided bin may become a red-black-tree-based structure, but only after the table is sufficiently large; below the minimum treeify capacity, HashMap generally prefers resizing. Average lookup and insertion are O(1), and HashMap is not thread-safe.

---

# 29. 30-Second ConcurrentHashMap Explanation

> Java 8+ ConcurrentHashMap avoids a single global map lock. Reads are generally non-blocking. For a simple insertion into an empty bin it can use CAS; when an occupied bin needs modification, it can use localized synchronization with the bin head acting as a common monitor for that bin. This allows writers targeting different bins to often proceed concurrently. It also disallows null keys and values so that a null result from `get()` unambiguously means no mapping was observed for that lookup.

---

# 30. Final Memory Model

```text
HashMap

table[]
 |
 +-- bin -> Node
 |
 +-- bin -> Node -> Node
 |
 +-- bin -> treeified structure

hashCode()
-> choose candidate bin

equals()
-> identify actual key

heavy collision + small table
-> resize

heavy collision + table capacity >= 64
-> bin can treeify
```

```text
ConcurrentHashMap Java 8+

read
-> generally no normal lock

empty-bin insert
-> CAS

occupied-bin modification
-> localized synchronization
-> bin head can be common monitor

different bins
-> concurrent writes often possible

same occupied bin
-> writers may wait

null values forbidden
-> get(key) == null means no mapping observed
```

```text
Ordering

HashMap
-> none guaranteed

LinkedHashMap
-> insertion/access order

TreeMap
-> sorted key order
```
