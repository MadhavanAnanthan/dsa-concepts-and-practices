# ArrayList Master Notes (Interview + Production Ready)

---

# 1. Collection Hierarchy

```text
Iterable
    ↑
Collection
    ↑
List
    ↑
ArrayList
```

- `Iterable` provides iteration support.
- `Collection` provides common operations:
  - add()
  - remove()
  - contains()
  - size()
  - isEmpty()
  - clear()
- `List` adds:
  - get(index)
  - set(index, value)
  - add(index, value)
  - indexOf()
- `ArrayList` provides the actual implementation.

### OOP Principle

```text
Interface = What
Implementation = How
```

Example:

```java
List<String> list = new ArrayList<>();
```

List defines the contract.

ArrayList decides how to implement it.

---

# 2. What is ArrayList?

ArrayList is a dynamically resizable array.

Internally:

```java
Object[]
```

is used to store elements.

Unlike normal arrays:

```java
int[] arr = new int[10];
```

whose size is fixed,

ArrayList automatically grows when capacity becomes full.

---

# 3. ArrayList Stores References

```java
ArrayList<Person> persons;
```

Internally stores:

```text
ref1
ref2
ref3
```

Actual objects live elsewhere in heap memory.

ArrayList stores only references.

---

# 4. Capacity vs Size

### Size

Number of elements present.

```java
list.size();
```

### Capacity

Size of internal array.

Example:

```java
ArrayList<Integer> list =
        new ArrayList<>(100);
```

Current:

```text
Size     = 0
Capacity = 100
```

After:

```java
list.add(10);
```

Current:

```text
Size     = 1
Capacity = 100
```

---

# 5. Initial Capacity

```java
new ArrayList<>();
```

In Java 8+, ArrayList does not immediately allocate a 10-element array.

Memory allocation happens during first insertion.

```java
list.add(1);
```

Then default capacity becomes 10.

---

# 6. Dynamic Growth

When capacity is exceeded:

```text
10
```

ArrayList creates a larger array.

Formula:

```java
newCapacity =
    oldCapacity + (oldCapacity >> 1);
```

Approximately:

```text
1.5x growth
```

Example:

```text
10 -> 15 -> 22 -> 33 -> ...
```

Process:

```text
Old Array
     ↓
Create Larger Array
     ↓
Copy Elements
     ↓
Switch Reference
```

Old array becomes unreachable and eventually gets garbage collected.

---

# 7. Why add() is O(1) Amortized?

Most insertions:

```java
list.add(value);
```

Only put element into next free slot.

```text
O(1)
```

Occasionally:

```text
Array Resize
+
Copy All Elements
```

occurs.

That operation is:

```text
O(n)
```

Since resizing occurs infrequently:

```text
Average = Amortized O(1)
```

---

# 8. Time Complexity

## ArrayList

| Operation | Complexity |
|------------|------------|
| Add at End | O(1) Amortized |
| Get by Index | O(1) |
| Search by Value | O(n) |
| Insert Middle | O(n) |
| Remove Middle | O(n) |

---

# 9. ArrayList vs LinkedList

## Internal Structure

### ArrayList

```text
[A][B][C][D]
```

Uses continuous array storage.

### LinkedList

```text
A <-> B <-> C <-> D
```

Uses doubly linked nodes.

Each node stores:

```text
Data
Previous
Next
```

---

## Access by Index

ArrayList:

```java
list.get(i);
```

Direct indexing.

```text
O(1)
```

LinkedList:

Needs traversal.

```text
Head -> Next -> Next
```

```text
O(n)
```

---

## Insertion in Middle

ArrayList:

```text
Shift elements
```

```text
O(n)
```

LinkedList:

Insertion itself is O(1).

But Java first has to find the node.

```text
Traversal O(n)
+
Insertion O(1)
```

Overall:

```text
O(n)
```

---

## Memory Usage

ArrayList:

```text
Less memory
```

LinkedList:

```text
More memory
```

because every node stores:

```text
next
prev
```

references.

---

## Cache Locality

ArrayList:

```text
CPU Cache Friendly
```

Elements stored continuously.

LinkedList:

```text
Nodes scattered in memory
```

Cache misses occur frequently.

This is one major reason ArrayList is usually faster in practice.

---

# 10. Fail-Fast Iterator

Collections like:

```java
ArrayList
LinkedList
HashSet
HashMap
```

have Fail-Fast iterators.

Example:

```java
for(String s : list){
    list.add("X");
}
```

May throw:

```java
ConcurrentModificationException
```

---

# 11. Why Fail-Fast Exists?

Not because of:

```text
❌ Performance
❌ Array Shifting
❌ Immutability
```

Actual reason:

```text
Prevent unpredictable iteration results.
```

Example:

```text
[10,20,30,40]
```

Iterator currently reading:

```text
20
```

Another operation removes:

```text
10
```

Now iterator state becomes inconsistent.

Questions arise:

```text
Did I skip something?
Did I process twice?
What should next() return?
```

Instead of risking corruption:

```java
ConcurrentModificationException
```

is thrown.

---

# 12. How Fail-Fast Works?

Collections maintain:

```java
modCount
```

Every structural modification increases it.

Example:

```java
list.add("A");
```

```text
modCount = 1
```

Iterator stores:

```java
expectedModCount
```

If values differ:

```java
ConcurrentModificationException
```

is thrown.

---

# 13. Fail-Fast is NOT Thread Safety

Purpose:

```text
Detect modification during iteration.
```

Purpose is NOT:

```text
Thread Safety
```

Fail-Fast simply prevents unpredictable results.

---

# 14. Enhanced For Loop Uses Iterator

This:

```java
for(String s : list)
```

internally becomes approximately:

```java
Iterator<String> it =
    list.iterator();
```

Therefore:

```text
Can throw ConcurrentModificationException
```

---

# 15. Index Loop Does NOT Use Iterator

```java
for(int i=0;i<list.size();i++)
```

does not use iterator.

Therefore:

```text
No Fail-Fast Monitoring
```

Although logical bugs are still possible.

---

# 16. Iterator.remove()

This is valid:

```java
Iterator<Integer> it =
        list.iterator();

while(it.hasNext()){
    Integer n = it.next();

    if(n == 10){
        it.remove();
    }
}
```

No exception.

Iterator updates internal state correctly.

---

# 17. trimToSize()

Suppose:

```java
ArrayList<Integer> list =
        new ArrayList<>();
```

Capacity becomes:

```text
10000
```

After:

```java
list.clear();
```

Current:

```text
Size = 0
```

But capacity may still be:

```text
10000
```

Calling:

```java
list.trimToSize();
```

reduces capacity to current size.

Useful after processing very large temporary lists.

---

# 18. remove() Interview Trap

List contains:

```java
[10,20,30]
```

### Remove By Index

```java
list.remove(1);
```

Removes:

```text
20
```

---

### Remove By Value

```java
list.remove(Integer.valueOf(20));
```

Removes:

```text
20
```

Different overloaded methods.

---

# 19. Arrays.asList()

Example:

```java
List<String> list =
    Arrays.asList("A","B","C");
```

Important:

```text
NOT a real ArrayList.
```

Think:

```text
Array + List View
```

Internally backed by a fixed-size array.

---

## Allowed

```java
list.get(0);
list.set(0,"X");
list.contains("A");
```

---

## Not Allowed

```java
list.add("D");
list.remove("A");
```

Throws:

```java
UnsupportedOperationException
```

Reason:

Arrays are fixed size.

```text
[A][B][C]
```

cannot become:

```text
[A][B][C][D]
```

without creating a new array.

---

# 20. Arrays.asList() Shares Same Array

```java
String[] arr =
        {"A","B","C"};

List<String> list =
        Arrays.asList(arr);
```

Now:

```java
list.set(0,"X");
```

Array also changes:

```java
arr[0]
```

Output:

```text
X
```

Because both point to same underlying data.

---

# 21. Real Resizable List

If true dynamic resizing is needed:

```java
List<String> list =
    new ArrayList<>(
        Arrays.asList("A","B","C")
    );
```

Now:

```java
list.add("D");
list.remove("A");
```

work correctly.

---

# 22. List.of()

Java 9+

```java
List<String> list =
    List.of("A","B","C");
```

Creates immutable list.

---

## Not Allowed

```java
list.add("D");
list.remove("A");
list.set(0,"X");
```

Throws:

```java
UnsupportedOperationException
```

---

# 23. subList()

Example:

```java
List<Integer> sub =
        list.subList(0,3);
```

Many developers think it creates a new list.

Wrong.

It creates a:

```text
View
```

of the original list.

---

Example

Original:

```text
[1,2,3,4,5]
```

SubList:

```text
[1,2,3]
```

Now:

```java
sub.remove(0);
```

Result:

```text
sub  = [2,3]

list = [2,3,4,5]
```

Both are affected.

---

# 24. Independent Copy of subList()

If independent list is needed:

```java
List<Integer> copy =
    new ArrayList<>(
        list.subList(0,3)
    );
```

Now changes do not affect each other.

---

# 25. Thread Safety

ArrayList is:

```text
NOT Thread Safe
```

Multiple threads modifying same list may cause problems.

Options:

```java
Collections.synchronizedList(...)
```

or

```java
CopyOnWriteArrayList
```

---

# 26. CopyOnWriteArrayList

Best when:

```text
Many Reads
Very Few Writes
```

When write occurs:

```java
add()
remove()
```

Entire array is copied.

```text
Old Array
     ↓
New Array
     ↓
Reference Switched
```

Old array becomes eligible for GC.

---

# 27. Why CopyOnWriteArrayList Doesn't Fail?

Iterator receives:

```text
Snapshot
```

Example:

Iterator starts:

```text
[1,2,3]
```

Another thread adds:

```text
4
```

Current iterator still sees:

```text
[1,2,3]
```

It never sees:

```text
4
```

during that iteration.

Benefits:

```text
✅ Thread Safe
✅ No ConcurrentModificationException
```

Trade-offs:

```text
❌ Expensive Writes
❌ Extra Memory
```

---

# 28. Enterprise Engineering Takeaway

Many ArrayList design decisions follow:

```text
Correctness > Performance
Predictability > Cleverness
Fail Fast > Silent Corruption
```

Fail-Fast exists because producing:

```text
Wrong Results
```

is usually worse than throwing an exception.

Particularly in:

```text
Banking
Payments
Healthcare
Enterprise Systems
```

---

# Final Interview Summary

Know these confidently:

✅ Collection Hierarchy

✅ Dynamic Array

✅ Capacity vs Size

✅ Growth Strategy (1.5x)

✅ Amortized O(1)

✅ Complexity Analysis

✅ ArrayList vs LinkedList

✅ Memory Overhead

✅ Cache Locality

✅ Fail-Fast Iterators

✅ modCount

✅ Iterator.remove()

✅ ConcurrentModificationException Purpose

✅ trimToSize()

✅ remove(index) vs remove(value)

✅ Arrays.asList()

✅ List.of()

✅ subList()

✅ Thread Safety

✅ CopyOnWriteArrayList

✅ Snapshot Iterator

✅ Enterprise Design Principles

After mastering all above, move to **HashMap**. That is the next most important Java Collection topic.
