# LinkedList – DSA Notes

## 1. What is a Linked List?

A linked list is a linear data structure where elements are stored as separate **nodes**.

Each node contains:

```text
Data
+
Reference to another node
```

Unlike arrays, linked-list nodes do not need to be stored next to each other in memory.

---

## 2. Singly Linked List

A singly linked-list node contains:

```text
data
next
```

Example:

```text
head
 |
 v
+----+------+    +----+------+    +----+------+
| 10 | next | -> | 20 | next | -> | 30 | null |
+----+------+    +----+------+    +----+------+
```

Each node points to the next node.

The last node points to:

```text
null
```

---

## 3. Basic Node Structure in Java

```java
class Node {
    int data;
    Node next;

    Node(int data) {
        this.data = data;
    }
}
```

Example creation:

```java
Node head = new Node(10);
head.next = new Node(20);
head.next.next = new Node(30);
```

---

## 4. Head

`head` stores the reference to the first node.

```text
head
 |
 v
[10] -> [20] -> [30] -> null
```

If:

```java
head == null
```

the linked list is empty.

---

## 5. Traversal

Unlike an array, a linked list does not support direct index-based access.

To reach the third node, we must move through previous nodes.

```text
head
 |
 v
[10] -> [20] -> [30]
          |
       traverse
```

Example:

```java
Node current = head;

while (current != null) {
    System.out.println(current.data);
    current = current.next;
}
```

Complexity:

```text
O(n)
```

---

## 6. Access by Index

To access index `3`, we must start from `head` and traverse.

```text
head
 |
 v
[10] -> [20] -> [30] -> [40]
  0       1       2       3
```

Complexity:

```text
O(n)
```

This is different from arrays and `ArrayList`, which provide:

```text
O(1)
```

random access.

---

## 7. Insert at Beginning

Before:

```text
head
 |
 v
[20] -> [30] -> null
```

Insert `10`:

```java
Node newNode = new Node(10);

newNode.next = head;
head = newNode;
```

After:

```text
head
 |
 v
[10] -> [20] -> [30] -> null
```

Complexity:

```text
O(1)
```

---

## 8. Insert at End

If only `head` is available, we must traverse to the last node.

```text
head
 |
 v
[10] -> [20] -> [30] -> null
```

Then append:

```text
[10] -> [20] -> [30] -> [40] -> null
```

Complexity without tail reference:

```text
O(n)
```

If the implementation maintains a `tail` reference:

```text
tail
 |
 v
[30]
```

then appending can be:

```text
O(1)
```

---

## 9. Insert in Middle

Suppose we already have a reference to the node after which insertion should happen.

Before:

```text
[10] -> [20] -> [40]
```

Insert `30` after `20`:

```java
newNode.next = current.next;
current.next = newNode;
```

After:

```text
[10] -> [20] -> [30] -> [40]
```

The link modification itself is:

```text
O(1)
```

But finding the required node may take:

```text
O(n)
```

Therefore, insertion by index is usually:

```text
O(n)
```

---

## 10. Delete from Beginning

Before:

```text
head
 |
 v
[10] -> [20] -> [30]
```

Delete first node:

```java
head = head.next;
```

After:

```text
head
 |
 v
[20] -> [30]
```

Complexity:

```text
O(1)
```

---

## 11. Delete from Middle

Before:

```text
[10] -> [20] -> [30] -> [40]
```

Delete `30`.

If we have the previous node (`20`):

```java
previous.next = current.next;
```

After:

```text
[10] -> [20] -> [40]
```

Changing the reference is:

```text
O(1)
```

Finding the node / previous node may require:

```text
O(n)
```

---

## 12. Delete from End

In a singly linked list, we need the second-last node.

```text
[10] -> [20] -> [30] -> null
          ^
       need this
```

Then:

```java
secondLast.next = null;
```

Complexity:

```text
O(n)
```

unless additional structure is maintained.

---

## 13. Search

To find a value:

```java
Node current = head;

while (current != null) {
    if (current.data == target) {
        return true;
    }

    current = current.next;
}
```

Complexity:

```text
O(n)
```

---

## 14. Singly Linked List Time Complexity

| Operation | Complexity |
|---|---:|
| Access by index | O(n) |
| Search | O(n) |
| Insert at beginning | O(1) |
| Delete from beginning | O(1) |
| Insert at end without tail | O(n) |
| Insert at end with tail | O(1) |
| Insert after known node | O(1) |
| Delete after known previous node | O(1) |

Important:

> `O(1)` insertion/deletion assumes the required node reference is already known.

Finding that node can still cost `O(n)`.

---

# Doubly Linked List

## 15. Structure

A doubly linked-list node contains:

```text
previous reference
data
next reference
```

Example:

```text
null <- [10] <-> [20] <-> [30] -> null
```

Each node can move:

```text
Forward
Backward
```

Node structure:

```java
class Node {
    int data;
    Node prev;
    Node next;
}
```

---

## 16. Advantages of Doubly Linked List

Compared with singly linked lists:

- Can traverse backward.
- Deleting a known node is easier because the node has a `prev` reference.
- Useful for bidirectional navigation.

However, each node needs extra memory for the additional reference.

---

## 17. Deleting a Known Node in Doubly Linked List

Suppose:

```text
[A] <-> [B] <-> [C]
```

Delete `B`.

Update:

```text
A.next = C
C.prev = A
```

Result:

```text
[A] <-> [C]
```

If the node is already known, this can be:

```text
O(1)
```

---

# Java `LinkedList`

## 18. Java LinkedList

Java provides:

```java
LinkedList<Integer> list = new LinkedList<>();
```

`java.util.LinkedList` is implemented as a **doubly linked list**.

Conceptually:

```text
null <- [10] <-> [20] <-> [30] -> null
```

It also maintains references to both ends:

```text
first
last
```

---

## 19. Common Java LinkedList Operations

```java
list.add(10);
list.addFirst(5);
list.addLast(20);

list.removeFirst();
list.removeLast();

list.getFirst();
list.getLast();
```

Because Java's `LinkedList` maintains both ends, operations at the first and last positions can be efficient.

---

## 20. `get(index)` in LinkedList

Example:

```java
list.get(500);
```

A linked list cannot directly calculate the memory location of index `500`.

It must traverse nodes.

Complexity:

```text
O(n)
```

Therefore, repeated indexed access such as:

```java
for (int i = 0; i < list.size(); i++) {
    list.get(i);
}
```

can become inefficient and may approach:

```text
O(n²)
```

Prefer an iterator or enhanced for loop for traversal.

---

# ArrayList vs LinkedList

## 21. Main Comparison

| Operation | ArrayList | LinkedList |
|---|---:|---:|
| Access by index | O(1) | O(n) |
| Search | O(n) | O(n) |
| Add at end | Amortized O(1) | O(1) |
| Add at beginning | O(n) | O(1) |
| Remove beginning | O(n) | O(1) |
| Insert middle by index | O(n) | O(n) |
| Remove middle by index | O(n) | O(n) |

Important:

Linked lists are often said to have:

```text
O(1) insertion/deletion
```

but that is only true when the required node position is already known.

If the position must first be found by index, traversal makes the whole operation:

```text
O(n)
```

---

## 22. Memory Difference

### ArrayList

```text
[ref][ref][ref][ref]
```

One backing array stores references together.

### LinkedList

```text
[data | links] -> [data | links] -> [data | links]
```

Every node is a separate object with link references.

Therefore, linked lists generally use more memory per element.

---

## 23. Cache Locality

Arrays and `ArrayList` generally have better cache locality because elements/references are stored together in an array.

Linked-list nodes may exist in different locations in the heap.

Conceptually:

```text
ArrayList:

[A][B][C][D]
```

versus:

```text
LinkedList:

[A] ---> [B] ---> [C] ---> [D]
```

This is one reason why `ArrayList` is often faster in practice for traversal.

---

# Important Linked List Patterns

## 24. Two Pointer / Fast and Slow Pointer

Use two references moving at different speeds.

```text
slow -> 1 step
fast -> 2 steps
```

Common problems:

- Find middle of linked list
- Detect cycle
- Find cycle start
- Find nth node from end

---

## 25. Reversal Pattern

Original:

```text
1 -> 2 -> 3 -> null
```

Reversed:

```text
3 -> 2 -> 1 -> null
```

Typical variables:

```java
Node previous = null;
Node current = head;
Node next;
```

Core idea:

```text
Save next
Reverse current.next
Move previous
Move current
```

---

## 26. Dummy Node Pattern

A dummy node is a temporary node placed before `head`.

```text
dummy -> head -> ...
```

Useful when:

- Removing nodes
- Merging linked lists
- Handling changes to the first node
- Avoiding special-case logic for the head

---

## 27. Common Linked List DSA Problems

Important problems to practice:

- Reverse a linked list
- Find middle node
- Detect a cycle
- Find cycle starting point
- Remove nth node from end
- Merge two sorted linked lists
- Find intersection of two linked lists
- Check palindrome linked list
- Reverse nodes in groups
- Add two numbers represented by linked lists

---

# Final Mental Model

## ArrayList

Think:

```text
Dynamic Array
```

Strength:

```text
Random access → O(1)
```

Weakness:

```text
Middle insertion/deletion → O(n)
```

---

## LinkedList

Think:

```text
Chain of nodes
```

Strength:

```text
Changing links at a known node → O(1)
```

Weakness:

```text
Finding/accessing a node → O(n)
```

The most important distinction is:

> **ArrayList is optimized for index-based access, while LinkedList is optimized for link manipulation when the required node is already known.**
