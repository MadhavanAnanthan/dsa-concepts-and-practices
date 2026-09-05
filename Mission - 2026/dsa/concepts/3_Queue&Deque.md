# Queue and Deque — DSA + Java Notes

## Queue

**Queue follows FIFO (First In, First Out).**

```text
Add:    10, 20, 30

Front → [10][20][30] ← Rear

Remove:
10 comes out first
```

### Java Queue

```java
Queue<Integer> queue = new ArrayDeque<>();
```

### Essential Methods

| Operation | Preferred Method | Alternative |
|---|---|---|
| Add | `offer(x)` | `add(x)` |
| Remove front | `poll()` | `remove()` |
| Read front | `peek()` | `element()` |

Prefer `offer()`, `poll()`, and `peek()` in DSA code because they do not throw exceptions for normal empty/full conditions.

### Example

```java
Queue<Integer> queue = new ArrayDeque<>();

queue.offer(10);
queue.offer(20);
queue.offer(30);

System.out.println(queue.peek()); // 10
System.out.println(queue.poll()); // 10
System.out.println(queue.peek()); // 20
```

### Complexity

```text
offer() → O(1) amortized
poll()  → O(1)
peek()  → O(1)
```

### DSA Uses

- BFS
- Tree level-order traversal
- Graph traversal
- Task scheduling
- Processing items in arrival order

### Memory Rule

```text
Queue → FIFO → oldest item comes out first.
```

---

# Deque

**Deque = Double Ended Queue.**

It supports insertion and removal from both ends.

```text
Front ← [10][20][30] → Rear
```

### Java Deque

```java
Deque<Integer> deque = new ArrayDeque<>();
```

### Essential Methods

#### Front

```java
offerFirst(x);
pollFirst();
peekFirst();
```

#### Rear

```java
offerLast(x);
pollLast();
peekLast();
```

### Example

```java
Deque<Integer> deque = new ArrayDeque<>();

deque.offerLast(10);
deque.offerLast(20);
deque.offerFirst(5);

System.out.println(deque); // [5, 10, 20]

System.out.println(deque.pollFirst()); // 5
System.out.println(deque.pollLast());  // 20
```

### Complexity

```text
Insert front → O(1)
Insert rear  → O(1)

Remove front → O(1)
Remove rear  → O(1)

Peek front   → O(1)
Peek rear    → O(1)
```

---

# Queue vs Deque

```text
Queue:
Add at rear
Remove from front
FIFO

Deque:
Add/remove from both front and rear
```

---

# ArrayDeque

`ArrayDeque` can be used as:

- Queue
- Deque
- Stack

### As Queue

```java
Queue<Integer> queue = new ArrayDeque<>();
```

### As Deque

```java
Deque<Integer> deque = new ArrayDeque<>();
```

### As Stack

```java
Deque<Integer> stack = new ArrayDeque<>();

stack.push(10);
stack.push(20);

stack.pop();
```

Prefer `ArrayDeque` over the old `Stack` class for stack behavior.

---

# ArrayDeque vs LinkedList

Both can implement Queue/Deque:

```java
Queue<Integer> q1 = new ArrayDeque<>();
Queue<Integer> q2 = new LinkedList<>();
```

For most DSA problems, prefer:

```text
ArrayDeque
```

because it is generally more memory-efficient and cache-friendly.

---

# Important Java Point

`ArrayDeque` does **not allow `null` elements**.

```java
deque.offer(null); // not allowed
```

This also makes `poll()` returning `null` useful to indicate that the deque is empty.

---

# Queue Internal Idea

A queue conceptually has:

```text
front
rear
```

Example:

```text
[10][20][30]

front = 10
rear  = 30
```

Operations:

```text
enqueue → add at rear
dequeue → remove from front
```

---

# Circular Queue

In a fixed-size array queue, removed positions can be reused by wrapping around.

Typical formula:

```text
nextIndex = (index + 1) % capacity
```

Useful conceptually, but in normal Java DSA problems you can usually use `ArrayDeque` unless manual implementation is requested.

---

# Deque Patterns in DSA

Deque is especially useful for:

- Sliding Window Maximum
- Monotonic Queue
- 0-1 BFS
- Problems requiring both-end access

Typical advanced idea:

```text
Remove expired values from front.
Remove useless candidates from rear.
```

---

# Quick Revision

## Queue

```text
Pattern: FIFO

- Add at rear, remove from front.
- Java: Queue<Integer> q = new ArrayDeque<>();
- Prefer: offer(), poll(), peek()
- Main uses: BFS, level-order traversal, scheduling.
- Time: O(1) for add/remove/peek.

Memory Rule:
Queue → oldest item comes out first.
```

## Deque

```text
Pattern: Double-Ended Queue

- Insert/remove from both front and rear.
- Java: Deque<Integer> dq = new ArrayDeque<>();
- Front: offerFirst(), pollFirst(), peekFirst()
- Rear: offerLast(), pollLast(), peekLast()
- Uses: sliding window, monotonic queue, stack/queue behavior.
- Time: O(1) at both ends.

Memory Rule:
Deque → Queue with access to both ends.
```
