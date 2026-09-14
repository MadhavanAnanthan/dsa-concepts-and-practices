# Queue, Deque, PriorityQueue & ArrayDeque - Master Notes

---

# 1. Hierarchy

```text
Iterable
   ↑
Collection
   ↑
Queue
   ↑
Deque
```

Implementations:

```text
Queue
 ├── LinkedList
 ├── PriorityQueue
 └── ArrayDeque

Deque
 ├── LinkedList
 └── ArrayDeque
```

---

# 2. Queue

## Definition

Queue follows:

```text
FIFO
(First In First Out)
```

Example:

```text
Insert:

10
20
30

Queue:

[10,20,30]

Removal Order:

10
20
30
```

Real-world examples:

```text
Ticket Counter
Printer Queue
Message Queue
Task Scheduling
```

---

## Important Queue Operations

### Insert

```java
offer(e)
add(e)
```

Both add element at tail.

---

### Read Head

```java
peek()
```

Returns current head.

Does NOT remove it.

Example:

```text
[10,20,30]

peek() → 10
```

Queue remains:

```text
[10,20,30]
```

---

### Remove Head

```java
poll()
remove()
```

Example:

```text
[10,20,30]

poll() → 10
```

Queue becomes:

```text
[20,30]
```

---

## add() vs offer()

### add()

```java
queue.add(10);
```

Insertion failure:

```java
Exception
```

---

### offer()

```java
queue.offer(10);
```

Insertion failure:

```java
false
```

returned.

Preferred in Queue APIs.

---

## remove() vs poll()

### remove()

Throws exception if empty.

```java
NoSuchElementException
```

---

### poll()

Returns:

```java
null
```

if empty.

Safer option.

---

## element() vs peek()

### element()

Throws exception if empty.

---

### peek()

Returns:

```java
null
```

if empty.

Usually preferred.

---

## Complexity

```text
offer() → O(1)
poll()  → O(1)
peek()  → O(1)
```

---

## Thread Safety

```text
Queue implementations are NOT thread-safe by default.
```

Examples:

```java
LinkedList
PriorityQueue
ArrayDeque
```

are not thread-safe.

Concurrent alternatives:

```java
ConcurrentLinkedQueue
BlockingQueue
LinkedBlockingQueue
ArrayBlockingQueue
```

---

# 3. Deque (Double Ended Queue)

## Definition

Deque =

```text
Double Ended Queue
```

Pronunciation:

```text
"Deck"
```

Not:

```text
D-Q
```

---

## Purpose

Insertion and removal possible from BOTH ends.

```text
Head                     Tail

10 <-> 20 <-> 30
```

---

## Important Operations

### Front Operations

```java
offerFirst()
peekFirst()
pollFirst()
```

---

### Rear Operations

```java
offerLast()
peekLast()
pollLast()
```

---

## Example

```java
Deque<Integer> dq =
        new ArrayDeque<>();
```

```java
dq.offerFirst(10);
dq.offerLast(20);
dq.offerLast(30);
```

Result:

```text
[10,20,30]
```

---

## Queue Behavior

FIFO

```java
offerLast()
pollFirst()
```

Example:

```text
10
20
30
```

removal order:

```text
10
20
30
```

---

## Stack Behavior

LIFO

```java
push()
pop()
```

Example:

```text
push(10)
push(20)
push(30)
```

removal order:

```text
30
20
10
```

---

## Why Deque?

Can behave as:

```text
Queue
+
Stack
```

in a single data structure.

Very flexible.

---

## Complexity

```text
offerFirst() → O(1)
offerLast()  → O(1)

pollFirst()  → O(1)
pollLast()   → O(1)

peekFirst()  → O(1)
peekLast()   → O(1)
```

---

## Thread Safety

```text
ArrayDeque → Not Thread Safe
LinkedList → Not Thread Safe
```

---

# 4. LinkedList as Queue/Deque

LinkedList implements:

```text
List
Queue
Deque
```

Therefore:

```java
LinkedList<Integer> list =
        new LinkedList<>();
```

can be used as:

```java
List
Queue
Deque
```

Examples:

```java
Queue<Integer> q =
        new LinkedList<>();

Deque<Integer> dq =
        new LinkedList<>();
```

Both are valid.

---

# 5. PriorityQueue

## Definition

PriorityQueue does NOT follow FIFO.

Elements are removed based on priority.

Default priority:

```text
Smallest Element First
```

---

## Example

```java
PriorityQueue<Integer> pq =
        new PriorityQueue<>();
```

```java
pq.offer(30);
pq.offer(10);
pq.offer(20);
```

Current head:

```java
pq.peek();
```

Output:

```text
10
```

Even though:

```text
30
```

was inserted first.

---

## Removal Order

```text
10
20
30
```

Priority order.

NOT insertion order.

---

# 6. Why PriorityQueue Exists?

Normal Queue:

```text
FIFO
```

PriorityQueue:

```text
Highest Priority First
```

Useful for:

```text
Job Scheduling
Task Scheduling
CPU Scheduling
Dijkstra Algorithm
Top-K Problems
Leaderboards
```

---

# 7. Heap

PriorityQueue internally uses:

```text
Binary Heap
```

---

## Heap is NOT Fully Sorted

Wrong:

```text
Heap = Sorted Tree
```

Correct:

```text
Heap = Partially Ordered Tree
```

---

## Min Heap

```text
         10
       /    \
      20     30
     / \
    25 40
```

Rule:

```text
Parent <= Child
```

Smallest element stays at root.

---

## Max Heap

```text
         50
       /    \
      30     20
```

Rule:

```text
Parent >= Child
```

Largest element stays at root.

---

## Heap Storage

Although conceptually a tree,

internally Java stores heap in:

```text
Array
```

Example:

```text
[10,20,30,25,40]
```

---

## Parent/Child Formula

For index:

```java
i
```

Children:

```java
left  = 2*i + 1
right = 2*i + 2
```

Parent:

```java
parent = (i - 1) / 2
```

Very common interview question.

---

# 8. PriorityQueue Complexity

### Insert

```java
offer()
```

```text
O(log n)
```

Heap adjustment required.

---

### Remove

```java
poll()
```

```text
O(log n)
```

Heap rebalancing required.

---

### Read Top Element

```java
peek()
```

```text
O(1)
```

Root access.

---

# 9. Min Heap (Default)

```java
PriorityQueue<Integer> pq =
        new PriorityQueue<>();
```

Removal order:

```text
Smallest → Largest
```

Example:

```text
10
20
30
40
```

---

# 10. Max Heap

Use custom comparator.

```java
PriorityQueue<Integer> pq =
    new PriorityQueue<>(
        Comparator.reverseOrder()
    );
```

Removal order:

```text
Largest → Smallest
```

Example:

```text
40
30
20
10
```

---

# 11. PriorityQueue Constructor

```java
public PriorityQueue(
    int initialCapacity,
    Comparator<? super E> comparator
)
```

Purpose:

```text
initialCapacity
```

Initial heap size.

```text
comparator
```

Custom priority rule.

---

## Example

Max Heap

```java
PriorityQueue<Integer> pq =
    new PriorityQueue<>(
        11,
        Comparator.reverseOrder()
    );
```

---

## Custom Object Priority

```java
PriorityQueue<Employee> pq =
    new PriorityQueue<>(
        Comparator.comparing(
            Employee::getSalary
        )
    );
```

Smallest salary gets highest priority.

---

# 12. Comparator vs Comparable

PriorityQueue uses:

```java
Comparable
```

or

```java
Comparator
```

to determine priority.

Without comparator:

```java
PriorityQueue<Integer>
PriorityQueue<String>
```

work because they already implement Comparable.

For custom objects:

```java
Employee
Student
Order
Task
```

provide:

```java
Comparable
```

or

```java
Comparator
```

---

# 13. Thread Safety

PriorityQueue is:

```text
NOT Thread Safe
```

Concurrent version:

```java
PriorityBlockingQueue
```

---

# 14. ArrayDeque

## Definition

ArrayDeque is a resizable circular array.

Internally:

```text
Dynamic Circular Array
```

Not LinkedList.

---

## Purpose

Java developers often use:

```text
ArrayDeque as Queue
ArrayDeque as Stack
```

because it is usually faster than LinkedList.

---

## Internal Idea

Instead of:

```text
Node -> Node -> Node
```

it uses:

```text
Circular Array
```

with head and tail pointers.

---

## Why Faster Than LinkedList?

### ArrayDeque

```text
Continuous Memory
```

Benefits:

```text
Better Cache Locality
Less Memory
No Node Objects
```

---

### LinkedList

```text
Node Objects
Next References
Prev References
```

More memory overhead.

---

## Queue Usage

```java
offerLast()
pollFirst()
```

FIFO

---

## Stack Usage

```java
push()
pop()
```

LIFO

---

## Complexity

```text
offerFirst() → O(1)
offerLast()  → O(1)

pollFirst()  → O(1)
pollLast()   → O(1)

peekFirst()  → O(1)
peekLast()   → O(1)
```

Amortized due to resizing.

---

## Thread Safety

```text
ArrayDeque is NOT Thread Safe.
```

---

# 15. Queue Family Comparison

## Queue

```text
FIFO
```

Use when:

```text
First In → First Out
```

Examples:

```text
Message Processing
Request Processing
```

---

## Deque

```text
Insert/Remove from BOTH ends
```

Use when:

```text
Need Queue + Stack behavior
```

Examples:

```text
Sliding Window Problems
LRU Cache
Palindrome Problems
```

---

## PriorityQueue

```text
Highest Priority Element First
```

Use when:

```text
Need Top Element Quickly
```

Examples:

```text
Top K Elements
Task Scheduling
Dijkstra
Median Problems
```

---

## ArrayDeque

```text
Fast Queue + Fast Stack
```

Preferred over:

```text
Stack
LinkedList
```

in many modern Java applications.

---

# Final Interview Summary

✅ Queue = FIFO

✅ peek() = Read Head

✅ poll() = Read + Remove Head

✅ offer() = Insert

✅ Deque = Double Ended Queue ("Deck")

✅ ArrayDeque implements Deque

✅ LinkedList implements List + Queue + Deque

✅ PriorityQueue ≠ FIFO

✅ Default PriorityQueue = Min Heap

✅ Max Heap via Comparator.reverseOrder()

✅ Heap = Partially Ordered Tree

✅ Heap stored internally in Array

✅ Parent/Child Index Formula

✅ PriorityQueue uses Comparable/Comparator

✅ PriorityQueue Constructor with Comparator

✅ ArrayDeque usually faster than LinkedList

✅ Queue/Deque/PriorityQueue/ArrayDeque are NOT thread-safe

✅ Concurrent alternatives:
- ConcurrentLinkedQueue
- BlockingQueue
- PriorityBlockingQueue
