# 206. Reverse Linked List

**Problem:** [LeetCode 206](https://leetcode.com/problems/reverse-linked-list/)

**Pattern:** Linked List · Iterative Traversal · Three-Pointer / Reference Reversal

## Approach 1 — ArrayList (Reverse Values)

Traverse the linked list and store its values in an `ArrayList`. Traverse the **same nodes** again, assigning values from the array in reverse order. The node connections stay unchanged.

```java
class Solution {
    public ListNode reverseList(ListNode head) {
        ListNode current = head;
        List<Integer> storage = new ArrayList<>();

        while (current != null) {
            storage.add(current.val);
            current = current.next;
        }

        int index = storage.size() - 1;
        ListNode current2 = head;

        while (current2 != null) {
            current2.val = storage.get(index);
            current2 = current2.next;
            index--;
        }

        return head;
    }
}
```

- **TC:** `O(n)` — two linear traversals.
- **Auxiliary SC:** `O(n)` — ArrayList stores `n` values.
- **Note:** Reverses **values**, not the actual links between nodes.

## Approach 2 — Optimized (Reverse Links In Place)

Keep `previous` as the head of the reversed portion, and `head` as the first unprocessed node. Save the upcoming node **before** changing `head.next`.

```java
class Solution {
    public ListNode reverseList(ListNode head) {
        ListNode previous = null;

        while (head != null) {
            ListNode next = head.next; // Save the remaining list
            head.next = previous;       // Reverse the current link
            previous = head;            // Advance reversed portion
            head = next;                // Advance to unprocessed node
        }

        return previous; // New head
    }
}
```

- **TC:** `O(n)` — each node is visited once.
- **Auxiliary SC:** `O(1)` — fixed number of references.
- **Note:** Reuses the original nodes and reverses their **next links**, without changing values.

### Reference Visualization

```text
Original:        10 → 20 → 30 → null
After iteration 1:
  previous → 10 → null       head → 20 → 30 → null
After iteration 2:
  previous → 20 → 10 → null  head → 30 → null
After iteration 3:
  previous → 30 → 20 → 10 → null; head → null
```

## Pattern Recognition

- When a linked-list question asks to **reverse the node order in place**, think of `previous`, `current/head`, and `next` references.
- **Save → Reverse → Advance:** Save `next`, redirect `current.next` to `previous`, then move both references forward.
- `current = current.next` **moves a variable**; `current.next = previous` **changes a node's link**.
- The original first node becomes the tail, so its `next` becomes `null`; after the loop, `previous` is the new head.

## Takeaway

My first approach stored all node values in an `ArrayList`, then traversed the original list again to overwrite values in reverse order. It was easier to build using familiar array operations, but required **O(n) extra space** and did not change node connections. The optimized approach reverses the actual `next` references with **O(1) extra space** while keeping **O(n) time**. The key insight is to **save the next node before breaking its link**; `previous` always points to the reversed portion, while `head` moves through the remaining nodes.

# 141. Linked List Cycle

**Difficulty:** Easy  
**Pattern:** Linked List – Fast & Slow Pointers (Floyd's Cycle Detection)  
**Problem:** [LeetCode 141](https://leetcode.com/problems/linked-list-cycle/)

## Problem Understanding

Given the `head` of a singly linked list, determine whether it contains a cycle.

- Return `true` if a cycle exists.
- Return `false` if traversal eventually reaches `null`.
- Compare **node references**, not values, because different nodes can contain identical values.
- The `pos` parameter is used internally by LeetCode to construct the cycle. It is not passed to our method.

---

## Approach 1: HashSet – Track Visited Nodes

**Idea:** Store visited node references in a `HashSet`. If the same reference appears again, a cycle exists.

```java
public class Solution {
    public boolean hasCycle(ListNode head) {
        Set<ListNode> visited = new HashSet<>();

        while (head != null) {
            if (visited.contains(head)) {
                return true;
            }

            visited.add(head);
            head = head.next;
        }

        return false;
    }
}
```

**Complexity:**
- **TC:** O(n) – Each node is visited at most once before detecting a cycle or reaching `null`.
- **SC:** O(n) – HashSet stores visited node references.

**Learning:** Add the current node to the set **before** moving to the next node. Otherwise, we may incorrectly detect a cycle.

---

## Approach 2: Floyd's Algorithm – My Initial Version

**Idea:** Use two pointers moving at different speeds. A boolean prevents comparing them at their initial position.

```java
public class Solution {
    public boolean hasCycle(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        boolean firstDone = false;

        while (fast != null && fast.next != null) {
            if (slow == fast && firstDone) {
                return true;
            }

            slow = slow.next;
            fast = fast.next.next;
            firstDone = true;
        }

        return false;
    }
}
```

**Complexity:**
- **TC:** O(n) – Pointers traverse the list and eventually meet if a cycle exists.
- **SC:** O(1) – Only two pointers and one boolean are used.

**Learning:** Both pointers initially reference `head`, so checking equality immediately would incorrectly return `true`. The boolean prevents this.

---

## Approach 3: Floyd's Algorithm – Final Optimized Version ✅

**Idea:** Move both pointers first, then compare their references. This removes the unnecessary boolean.

```java
public class Solution {
    public boolean hasCycle(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) {
                return true;
            }
        }

        return false;
    }
}
```

**Complexity:**
- **TC:** O(n)
- **SC:** O(1)

**Why it works:**
- `slow` advances 1 node per iteration.
- `fast` advances 2 nodes per iteration.
- If a cycle exists, fast eventually catches slow.
- If no cycle exists, fast reaches the end of the list.
- Checking `fast != null && fast.next != null` prevents `NullPointerException`.

---

## Approach Comparison

| Approach | Time Complexity | Space Complexity |
|---|---|---|
| HashSet | O(n) | O(n) |
| Floyd's Algorithm – Initial | O(n) | O(1) |
| Floyd's Algorithm – Optimized | O(n) | O(1) |

**Best Approach:** Floyd's Cycle Detection – O(n) time and O(1) space.

---

## Pattern Recognition

**Fast & Slow Pointers (Tortoise and Hare)**

Use this pattern when:
- Detecting a cycle in a linked list.
- Finding the middle node of a linked list.
- Finding the starting node of a cycle.

**Key observation:** Two pointers moving at different speeds must eventually meet if they continuously traverse a finite cycle.

---

## Takeaway

Initially, I understood that normal linked list traversal stops when `head` becomes `null`, but a cyclic linked list never reaches `null`, causing an infinite loop. I first used a `HashSet` to track visited node references, achieving O(n) time and O(n) space. Then I learned Floyd's Cycle Detection using slow and fast pointers, where slow moves one step and fast moves two steps. If a cycle exists, they eventually meet; otherwise, fast reaches `null`. My initial implementation used a boolean to avoid comparing pointers at their starting position. Later, I optimized it by moving both pointers before comparing them, eliminating the boolean and achieving **O(n) time and O(1) space** without knowing the linked list's size.