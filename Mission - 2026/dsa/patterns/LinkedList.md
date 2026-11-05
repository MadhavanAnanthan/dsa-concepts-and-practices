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
