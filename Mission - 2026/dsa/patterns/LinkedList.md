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

# 876. Middle of the Linked List

**Difficulty:** Easy  
**Pattern:** Linked List | Fast & Slow Pointers  
**LeetCode:** https://leetcode.com/problems/middle-of-the-linked-list/

## Problem

Given the head of a singly linked list, return the middle node.

If the list contains an even number of nodes, return the **second middle node**.

**Examples:**

```text
Input:  1 → 2 → 3 → 4 → 5
Output: 3

Input:  1 → 2 → 3 → 4 → 5 → 6
Output: 4
```

---

## Approach 1: Two-Pass Traversal (My Initial Solution)

### Intuition

1. Traverse the linked list and count all nodes.
2. Calculate the middle index using `count / 2`.
3. Traverse again until reaching that index.
4. Return the middle node.

### Code

```java
class Solution {
    public ListNode middleNode(ListNode head) {
        int middle = 0;
        ListNode traverseAll = head;

        while (traverseAll != null) {
            traverseAll = traverseAll.next;
            middle++;
        }

        middle = middle / 2;
        int remainingNodes = 0;

        while (head != null) {
            if (middle == remainingNodes) {
                return head;
            }
            head = head.next;
            remainingNodes++;
        }

        return head;
    }
}
```

### Complexity

- **Time:** O(n) — First traversal O(n) + second traversal O(n/2).
- **Space:** O(1) — Only references and counters.

**Important:** Two sequential loops do not mean O(n²).

```text
O(n) + O(n/2) = O(n)
```

**Limitation:** Requires two traversals.

---

## Approach 2: Fast & Slow Pointers (Optimized)

### Intuition

Instead of counting nodes, use two pointers moving at different speeds.

- `slow` moves one node at a time.
- `fast` moves two nodes at a time.
- When `fast` reaches the end, `slow` points to the middle.
- Works for both odd and even-length linked lists.

### Code

```java
class Solution {
    public ListNode middleNode(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }
}
```

### Dry Run 1: Even-Length Linked List

**Input:** `[1,2,3,4,5,6]`

| Iteration | Slow | Fast |
|---|---|---|
| Initial | 1 | 1 |
| 1 | 2 | 3 |
| 2 | 3 | 5 |
| 3 | 4 | null |

**Result:**

- `fast == null` → `fast != null` becomes false.
- Loop terminates.
- `slow` points to node `4`.

**Output: 4** (second middle node).

### Dry Run 2: Odd-Length Linked List

**Input:** `[1,2,3,4,5]`

| Iteration | Slow | Fast |
|---|---|---|
| Initial | 1 | 1 |
| 1 | 2 | 3 |
| 2 | 3 | 5 |

**Result:**

- `fast` still points to the last node (`5`).
- `fast.next == null` → `fast.next != null` becomes false.
- Loop terminates.
- `slow` points to node `3`.

**Output: 3** (exact middle node).

### Why Both While Conditions Are Important

```java
while (fast != null && fast.next != null)
```

**1. `fast != null`**

- For an even-length linked list, `fast` eventually becomes `null`.
- This condition terminates the loop.
- Also prevents accessing `fast.next` when `fast` is null.

**2. `fast.next != null`**

- For an odd-length linked list, `fast` eventually points to the last node.
- The last node's `next` is `null`, so this condition terminates the loop.
- Prevents accessing `fast.next.next` when `fast.next` is null.

**Important:** Java's `&&` uses short-circuit evaluation. If `fast != null` is false, Java does not evaluate `fast.next != null`, preventing a `NullPointerException`.

### Complexity

- **Time:** O(n) — Single traversal; fast advances two nodes per iteration.
- **Space:** O(1) — Only two pointer references.

---

## How to Recognize the Fast & Slow Pointer Pattern

**Problem signals:**

- Find the middle of a linked list.
- Find a relative position without calculating the length.
- Detect a cycle in a linked list.
- Traverse using two pointers at different speeds.

**Thought process:**

```text
Need middle node
       ↓
Linked list has no direct index access
       ↓
Initial approach: Count nodes + traverse again
       ↓
Can we avoid the second traversal?
       ↓
Use two pointers moving at different speeds
       ↓
Fast moves 2 steps, slow moves 1 step
       ↓
Fast reaches end → Slow reaches middle
       ↓
Pattern: Fast & Slow Pointers
```

**Core relationship:**

```text
Fast travels approximately twice
the distance traveled by Slow.

Fast reaches the end
        ↓
Slow reaches the middle
```

**Note:** This middle-finding technique assumes the linked list has no cycle.

---

## Approach Comparison

| Metric | Two-Pass Traversal | Fast & Slow Pointers |
|---|---|---|
| Time | O(n) | O(n) |
| Space | O(1) | O(1) |
| Traversals | Two | One |
| Extra Data Structure | No | No |
| Interview Preference | Good | Optimal |

---

## Pattern Reusability

**LC 141 — Linked List Cycle**

- Slow moves one step, fast moves two steps.
- If they meet during traversal, a cycle exists.

**LC 876 — Middle of Linked List**

- Slow moves one step, fast moves two steps.
- When fast reaches the end, slow identifies the middle.

The pointer movement is similar, but the termination logic and purpose differ.

---

## Key Takeaway

My initial two-pass solution was already **O(n) time and O(1) space**, because sequential loops have additive complexity, not O(n²). The optimized **Fast & Slow Pointer** approach eliminates the second traversal: slow moves one step while fast moves two steps, so when fast reaches the end, slow points to the middle. For **even-length lists**, fast becomes `null`, terminating through `fast != null`, and slow returns the second middle. For **odd-length lists**, fast remains at the last node, and `fast.next != null` terminates the loop. Both conditions are essential for safe traversal and preventing `NullPointerException`. The main pattern-recognition lesson is to consider two pointers moving at different speeds whenever a linked list problem requires finding a relative position without knowing its length.

# 234. Palindrome Linked List

**Difficulty:** Easy  
**Pattern:** Fast & Slow Pointers | Stack | Linked List Reversal  
**LeetCode:** https://leetcode.com/problems/palindrome-linked-list/

## Problem

Given the head of a singly linked list, determine whether the list is a palindrome.

A palindrome reads the same forward and backward.

```text
Input:  1 → 2 → 2 → 1
Output: true

Input:  1 → 2 → 3 → 2 → 1
Output: true

Input:  1 → 2 → 3
Output: false
```

---

## Approach 1: Fast & Slow Pointers + Stack (Initial Solution)

### Intuition

1. Use slow and fast pointers to locate the middle.
2. Push first-half values into a stack.
3. For odd-length lists, skip the middle node.
4. Compare the remaining nodes with stack values using LIFO.
5. Return `false` on a mismatch; otherwise return `true`.

### Code — My Initial Approach

```java
class Solution {
    public boolean isPalindrome(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        Deque<Integer> stack = new ArrayDeque<>();

        while (fast != null && fast.next != null) {
            stack.push(slow.val);
            slow = slow.next;
            fast = fast.next.next;

            // Skip middle for odd-length lists
            if (fast != null && fast.next == null) {
                slow = slow.next;
            }
        }

        while (slow != null && !stack.isEmpty()) {
            int topValue = stack.pop();

            if (slow.val != topValue) {
                return false;
            }

            slow = slow.next;
        }

        return true;
    }
}
```

### Complexity

- **Time:** O(n) — Traverse to the middle and compare the remaining half.
- **Space:** O(n) — Stack stores approximately n/2 values.

### Limitation

Uses additional memory to store the first half of the linked list.

---

## Approach 2: Fast & Slow Pointers + In-Place Reversal (Optimized)

### Intuition

Instead of storing values in a stack:

1. Find the middle using slow and fast pointers.
2. Skip the middle node if the list length is odd.
3. Reverse the second half in place.
4. Compare the first half against the reversed second half.
5. Return `false` for a mismatch; otherwise `true`.

### Code — My Optimized Solution

```java
class Solution {

    public ListNode reverseList(ListNode head) {
        ListNode previous = null;

        while (head != null) {
            ListNode next = head.next;
            head.next = previous;
            previous = head;
            head = next;
        }

        return previous;
    }

    public boolean isPalindrome(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        // Step 1: Find middle
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // Step 2: Skip middle for odd-length lists
        if (fast != null) {
            slow = slow.next;
        }

        // Step 3: Reverse second half
        ListNode reversedRef = reverseList(slow);
        ListNode source = head;

        // Step 4: Compare
        while (reversedRef != null) {
            if (source.val != reversedRef.val) {
                return false;
            }

            source = source.next;
            reversedRef = reversedRef.next;
        }

        return true;
    }
}
```

### Complexity

- **Time:** O(n) — Finding middle, reversing and comparing are sequential O(n) operations.
- **Space:** O(1) — Only a constant number of node references.

**Important:** This approach modifies the original linked list. If the original structure must be preserved, reverse the second half again after comparison to restore it.

---

## Understanding Reverse Linked List — LC 206 Revision

### Original

```text
1 → 2 → 3 → null
```

### Goal

```text
null ← 1 ← 2 ← 3
```

### Four Important Statements

```java
ListNode next = head.next;
ListNode previous = null;

head.next = previous;
previous = head;
head = next;
```

The actual repeated sequence is:

```java
ListNode previous = null;

while (head != null) {
    ListNode next = head.next; // 1. Preserve next reference
    head.next = previous;      // 2. Reverse link
    previous = head;           // 3. Advance previous
    head = next;               // 4. Advance head
}
```

### Dry Run

Original: `1 → 2 → 3 → null`

| Iteration | Current head | Saved next | Previous after reversal |
|---|---|---|---|
| Initial | 1 | — | null |
| 1 | 1 | 2 | 1 → null |
| 2 | 2 | 3 | 2 → 1 → null |
| 3 | 3 | null | 3 → 2 → 1 → null |

At termination:

```text
head = null
previous = 3 → 2 → 1 → null
```

Return `previous`, because it points to the new head.

**Key:** Save the next reference before changing `head.next`, or the remaining linked list could become unreachable.

---

## Odd vs Even-Length Handling

### Odd: [1,2,3,2,1]

```text
1 → 2 → 3 → 2 → 1
        ↑
       slow
```

- `fast` stops at the last node.
- `fast != null` is true.
- Skip the middle node (`3`).
- Reverse `2 → 1` into `1 → 2`.
- Compare with the first two nodes.

### Even: [1,2,2,1]

```text
1 → 2 → 2 → 1
        ↑
       slow
```

- `fast` becomes null.
- `fast != null` is false.
- Do not skip any node.
- Reverse `2 → 1` into `1 → 2`.
- Compare with the first two nodes.

---

## Pattern Recognition

**Question:** How can I recognize this approach in another problem?

```text
Need to check palindrome
         ↓
Singly linked list cannot traverse backward
         ↓
Initial idea: Stack to simulate reverse order
         ↓
Can we eliminate extra memory?
         ↓
Find middle using slow/fast pointers
         ↓
Reverse second half in place
         ↓
Compare both halves
         ↓
O(n) Time + O(1) Auxiliary Space
```

### Reusable Techniques

| Problem | Technique |
|---|---|
| LC 141 — Linked List Cycle | Fast & Slow Pointers |
| LC 876 — Middle of Linked List | Fast & Slow Pointers |
| LC 206 — Reverse Linked List | In-place Pointer Reversal |
| LC 234 — Palindrome Linked List | Middle + Reverse + Compare |

---

## Approach Comparison

| Metric | Stack Approach | Reversal Approach |
|---|---|---|
| Time | O(n) | O(n) |
| Auxiliary Space | O(n) | O(1) |
| Extra Data Structure | Stack | None |
| Modifies Original List | No | Yes |
| Interview Preference | Good | Optimal space |

---

## Key Takeaway

My initial solution combined **fast/slow pointers and a stack**, achieving O(n) time and O(n) space. To optimize the space, I reused **LC 876 (Find Middle)** and **LC 206 (Reverse Linked List)**: locate the middle, skip it for odd-length lists, reverse the second half in place, and compare node values using two references. This achieves **O(n) time and O(1) auxiliary space**. The most important learning is pointer manipulation during reversal: save `next`, reverse `head.next`, move `previous`, then move `head`. The broader pattern is that reversing part of a linked list can replace an auxiliary stack when backward comparison is needed. This solution modifies the list, so restoring the reversed half may be necessary when the original structure must remain unchanged.

# 328. Odd Even Linked List

**Problem:** [LeetCode 328](https://leetcode.com/problems/odd-even-linked-list/)

Rearrange the **existing nodes** so that nodes at original odd positions come first, followed by nodes at original even positions. Preserve relative order within each group. Positions are **1-based**, not determined by node values.

**Example:** `1 → 2 → 3 → 4 → 5` becomes `1 → 3 → 5 → 2 → 4`.

**Target:** Time **O(n)**, auxiliary space **O(1)**.

## Pattern recognition

- **Linked-list in-place rearrangement / pointer rewiring**.
- Split nodes by **original positions**, retain the start of the even chain, then concatenate.
- Do not confuse changing a local **reference variable** with changing a shared node's **`next` field**.

## Approach 1 — My first attempt: two pointer jumps + two ArrayLists

Collect odd/even *values* using jumping pointers, then overwrite the original list's values.

```java
class Solution {
    public ListNode oddEvenList(ListNode head) {
        ListNode odd = head;
        ListNode even = head;
        boolean firstTime = true;

        List<Integer> oddList = new ArrayList<>();
        List<Integer> evenList = new ArrayList<>();

        while (even != null && even.next != null) {
            if (firstTime) {
                oddList.add(odd.val);
                even = even.next;
                evenList.add(even.val);
                firstTime = false;
            } else {
                odd = odd.next.next;
                oddList.add(odd.val);
                even = even.next.next;
                if (even != null) {
                    evenList.add(even.val);
                }
            }
        }

        ListNode result = head;
        int index = 0;
        while (index < oddList.size() && head != null) {
            head.val = oddList.get(index++);
            head = head.next;
        }
        index = 0;
        while (index < evenList.size() && head != null) {
            head.val = evenList.get(index++);
            head = head.next;
        }
        return result;
    }
}
```

- **TC:** O(n) (collection + rewriting; constants are ignored).
- **SC:** O(n) (the two lists store n values in total).
- **Limitations:** Extra `firstTime` branching; awkward jumps; changes values **rather than rearranging nodes**, so it does **not** satisfy the node-relinking requirement.
- **Edge case:** A single-node input collects no values, but the original head is returned unchanged.

## Approach 2 — Cleaner value-collection version

Use one traversal pointer and a position counter; keep the same value-rewriting strategy.

```java
class Solution {
    public ListNode oddEvenList(ListNode head) {
        if (head == null) return null;

        List<Integer> oddList = new ArrayList<>();
        List<Integer> evenList = new ArrayList<>();
        ListNode current = head;
        int position = 1;

        while (current != null) {
            if (position % 2 == 0) {
                evenList.add(current.val);
            } else {
                oddList.add(current.val);
            }
            current = current.next;
            position++;
        }

        current = head;
        for (int value : oddList) {
            current.val = value;
            current = current.next;
        }
        for (int value : evenList) {
            current.val = value;
            current = current.next;
        }
        return head;
    }
}
```

- **TC:** O(n).
- **SC:** O(n).
- **Improvement:** Removes `firstTime`, double jumps, and index-based rewriting.
- **Still not the required node-rearrangement solution:** Only `val` fields change.

## Approach 3 — Final optimized: rewire existing nodes

Track the tails of the odd and even chains and save the even chain's starting reference.

```java
class Solution {
    public ListNode oddEvenList(ListNode head) {
        if (head == null) {
            return null;
        }

        ListNode odd = head;
        ListNode even = head.next;
        ListNode evenHead = even;

        while (even != null && even.next != null) {
            odd.next = odd.next.next;     // Link current odd to next odd
            odd = odd.next;               // Advance odd reference

            even.next = even.next.next;   // Link current even to next even
            even = even.next;             // Advance even reference
        }

        odd.next = evenHead;              // Append even chain after odd chain
        return head;
    }
}
```

- **TC:** O(n) — each pointer moves through about half the nodes.
- **SC:** O(1) — only three additional node references; **no new nodes**.
- **Why `evenHead`?** `even` moves; `evenHead` continues to reference the first even node for final concatenation.
- **Edge cases:** `null`, one node, two nodes, odd-length and even-length lists.

### Mini trace: `1 → 2 → 3 → 4 → 5`

| Stage | `odd` | `even` | Important rewiring |
|---|---:|---:|---|
| Start | 1 | 2 | `evenHead = 2` |
| Iteration 1 | 3 | 4 | `1.next = 3`, `2.next = 4` |
| Iteration 2 | 5 | null | `3.next = 5`, `4.next = null` |
| Finish | 5 | null | `5.next = evenHead (2)` |

**Result:** `1 → 3 → 5 → 2 → 4 → null`.

## Object references & pointer manipulation — concepts to practise

### A. Independent variables, shared objects

```java
ListNode current = head;  // Both refer to the same first node
current = current.next;  // Only 'current' moves; 'head' stays
```

Assignment of a `ListNode` reference copies the **reference**, not the object. Java does not create a new node here.

### B. Reassigning a reference vs mutating a node

```java
current = current.next;       // Change where the local variable points
current.next = current.next.next; // Change the actual node's next link
current.val = 100;            // Change the actual node's value
```

Other references to the **same node** see its updated `next` and `val`. They do **not** move when `current` is reassigned.

### C. Why odd updates must precede even updates in this code

Initial: `1 → 2 → 3 → 4 → 5`; `odd = 1`, `even = 2`.

**Wrong order:**

```java
even.next = even.next.next; // 2.next changes from 3 to 4
odd.next = odd.next.next;   // Now 1.next becomes 4, NOT 3
```

The later expression follows a link that was already modified. **Pointer writes can change subsequent reads.**

**Working order:**

```java
odd.next = odd.next.next;   // Preserve 1 → 3 first
odd = odd.next;
even.next = even.next.next; // Then preserve 2 → 4
even = even.next;
```

### D. Why returning `head` still works

`head` retains the reference to the first node. Updating `odd.next` / `even.next` mutates the nodes reachable from it. Moving `odd` or `even` does not move `head`.

### E. Practice checklist

- Before rewiring `node.next`, ask: **Which node will become unreachable unless I save a reference?**
- Distinguish `pointer = pointer.next` from `pointer.next = ...`.
- Trace lists of lengths **0, 1, 2, 4, 5, 6**.
- Check whether the last even node ends in `null` (avoid cycles).
- Draw the **actual links** after each assignment, not two imaginary copied lists.

## Complexity summary

| Approach | Time | Auxiliary space | Rearranges actual nodes? |
|---|---|---|---|
| 1. Two jumping pointers + ArrayLists | O(n) | O(n) | No |
| 2. Single pointer + position + ArrayLists | O(n) | O(n) | No |
| 3. Odd/even pointer rewiring | **O(n)** | **O(1)** | **Yes** |

## One-paragraph takeaway

My first solution collected odd/even-positioned values with two pointers and ArrayLists, achieving O(n) time but O(n) space. The cleaner second solution used one pointer and a position counter, making the same idea easier to read without improving complexity. The final solution works in O(n) time and O(1) extra space by rewiring **existing nodes**. The biggest lesson is Java reference semantics: pointer variables independently track positions, but they share the actual nodes. `odd = odd.next` only moves a variable, while `odd.next = ...` changes the list structure for everyone referencing that node. **Assignment order matters**, because modifying `even.next` too early can make `odd.next.next` follow the wrong node.
In linked-list manipulation, always update pointers in dependency order to avoid overwriting references needed by subsequent operations. In LeetCode 328, process odd nodes before even nodes because the odd update depends on the current even node's next reference. Processing even first would overwrite that link, causing the odd traversal to skip nodes.

// one is pending here

# 2. Add Two Numbers

**LeetCode:** https://leetcode.com/problems/add-two-numbers/  
**Difficulty:** Medium  
**Pattern:** Linked List Construction + Digit-by-Digit Simulation (Carry)

## Problem Understanding

Two linked lists represent non-negative integers, with digits stored **in reverse order**. Add the numbers and return the result as a **new linked list**, also in reverse order.

Example:

```text
l1: 2 -> 4 -> 3   (342)
l2: 5 -> 6 -> 4   (465)
---------------------
out: 7 -> 0 -> 8   (807)
```

**Core idea:** Traverse both lists together, treat a missing digit as `0`, calculate the sum with carry, and append the result digit to a new list.

- `sum % 10` gives the digit for the current result node.
- `sum / 10` gives the carry for the next iteration (integer division).
- The largest per-position sum is `9 + 9 + 1 = 19`.

## Approach 1 — My Initial Solution (Head + Current References)

Construct the first result node separately, save its reference in `finalResult`, and then append future nodes using the moving `result` reference. Handle any remaining carry after traversing both input lists.

```java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode result = null;
        ListNode finalResult = null;
        int firstDigit = 0;
        boolean firstTime = true;

        while (l1 != null || l2 != null) {
            int val1 = 0;
            int val2 = 0;
            if (l1 != null) {
                val1 = l1.val;
            }
            if (l2 != null) {
                val2 = l2.val;
            }

            int sum = val1 + val2 + firstDigit;
            firstDigit = 0;
            int lastDigit = sum % 10;
            if (sum > 9) {
                firstDigit = sum / 10;
            }

            if (firstTime) {
                firstTime = false;
                result = new ListNode(lastDigit, null);
                finalResult = result;
            } else {
                result.next = new ListNode(lastDigit, null);
                result = result.next;
            }

            l1 = l1 == null ? null : l1.next;
            l2 = l2 == null ? null : l2.next;
        }

        if (firstDigit > 0) {
            result.next = new ListNode(firstDigit, null);
        }
        return finalResult;
    }
}
```

**Complexity:**
- **Time:** `O(max(m, n))` — each input list is traversed once.
- **Space:** `O(max(m, n))` for the output list; **auxiliary space:** `O(1)`.

**Drawback:** The `firstTime` flag and separate final-carry handling add unnecessary branching.

## Approach 2 — Cleaner Solution (Dummy Node)

Start with a dummy node, append every result digit in the same way, and include `carry != 0` in the loop condition so the last carry is handled automatically.

```java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        int carry = 0;

        while (l1 != null || l2 != null || carry != 0) {
            int val1 = l1 != null ? l1.val : 0;
            int val2 = l2 != null ? l2.val : 0;
            int sum = val1 + val2 + carry;

            carry = sum / 10;
            current.next = new ListNode(sum % 10);
            current = current.next;

            if (l1 != null) l1 = l1.next;
            if (l2 != null) l2 = l2.next;
        }

        return dummy.next;
    }
}
```

**Complexity:**
- **Time:** `O(max(m, n))` — one pass through both lists (plus at most one final carry node).
- **Space:** `O(max(m, n))` for the output list; **auxiliary space:** `O(1)`.

**Improvement:** Fewer conditions and more consistent node insertion; **no asymptotic TC/SC improvement**.

## Pointer Reference Learning

```text
dummy -> 0 (placeholder)
current = dummy

Append 7: dummy -> 7       current -> 7
Append 0: dummy -> 7 -> 0  current -> 0
Append 8: dummy -> 7 -> 0 -> 8  current -> 8

Return dummy.next (node 7), not current (node 8).
```

- `current.next = new ListNode(digit)` **connects** the new node.
- `current = current.next` **moves** the append pointer to that node.
- `dummy.next` preserves the **head of the actual result**, even when `current` moves.
- Without moving `current`, the next insertion overwrites the same `.next` link and disconnects previously appended nodes.

## Quick Dry Run — Carry

```text
l1: 9 -> 9
l2: 9 -> 9

Position 1: 9 + 9 + 0 = 18 -> node 8, carry 1
Position 2: 9 + 9 + 1 = 19 -> node 9, carry 1
Final carry:                    node 1

Result: 8 -> 9 -> 1   (198)
```

## Pattern Recognition

Use **Linked List Construction + Digit Simulation** when you must compute a result incrementally and build a new linked list. The **dummy node** is a general construction technique, not a specialized algorithm like Floyd's cycle detection.

## One-Paragraph Takeaway

This problem mainly reinforces linked-list traversal, object-reference manipulation, and elementary digit-by-digit addition. My first approach correctly creates and appends nodes by preserving the first node in `finalResult` and moving `result` after each insertion. The cleaner dummy-node approach avoids special handling for the first node: append with `current.next = new ListNode(...)`, advance using `current = current.next`, and return `dummy.next`. Both approaches take **O(max(m, n)) time**, require **O(max(m, n)) output space**, and use only **O(1) auxiliary space**.

// need to add intersion of LL

# 21. Merge Two Sorted Lists

**Problem:** [LeetCode 21 — Merge Two Sorted Lists](https://leetcode.com/problems/merge-two-sorted-lists/)

**Pattern:** Two pointers + dummy head + linked-list pointer manipulation (merge step of merge sort).

Given two sorted linked lists, merge their values into one sorted linked list. Let `m` and `n` be their respective lengths.

## Approach 1 — Collect, sort, and rebuild (initial idea)

Traverse both lists, collect values, sort them, and create a new linked list.

```java
import java.util.*;

class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        List<Integer> values = new ArrayList<>();
        while (list1 != null) {
            values.add(list1.val);
            list1 = list1.next;
        }
        while (list2 != null) {
            values.add(list2.val);
            list2 = list2.next;
        }

        Collections.sort(values);
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        for (int value : values) {
            current.next = new ListNode(value);
            current = current.next;
        }
        return dummy.next;
    }
}
```

- **TC:** O((m+n) log(m+n)) due to sorting.
- **SC:** O(m+n) for collected values and the new linked list, excluding sorting implementation overhead.
- **Limitation:** Sorting wastes the fact that *both inputs are already sorted*.

## Approach 2 — Two pointers; handle null inside the loop (my first working code)

```java
class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        while (list1 != null || list2 != null) {
            if (list1 != null && (list2 == null || list1.val <= list2.val)) {
                current.next = new ListNode(list1.val);
                list1 = list1.next;
            } else {
                current.next = new ListNode(list2.val);
                list2 = list2.next;
            }
            current = current.next;
        }
        return dummy.next;
    }
}
```

- **TC:** O(m+n): one input pointer advances each iteration.
- **SC:** O(m+n): a new node is allocated for each input node.
- **Learning:** `current.next = new ListNode(...)` **links** a new node; `current = current.next` **moves** the tail pointer. `dummy.next` is the actual result head.

## Approach 3 — Cleaner loop; attach the remainder (my improved code)

```java
class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                current.next = new ListNode(list1.val);
                list1 = list1.next;
            } else {
                current.next = new ListNode(list2.val);
                list2 = list2.next;
            }
            current = current.next;
        }

        if (list1 != null) {
            current.next = list1;
        } else {
            current.next = list2;
        }
        return dummy.next;
    }
}
```

- **TC:** O(m+n).
- **SC:** **O(m+n) in the worst case**: you create a new node for every comparison until one list ends. Although the remainder is reused, the number of comparisons can still reach `m+n-1` (e.g., if one list's only value comes after nearly all values of the other). The exact new-node count depends on ordering.
- **Improvement:** No repeated null comparison inside the main loop; remaining nodes attach in O(1) time.
- **Trade-off:** Output may be a mixture of new nodes and original nodes. If the original lists must remain fully independent of the result, use Approach 2 instead.

## Approach 4 — Optimal: reuse existing nodes, O(1) auxiliary space

```java
class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                current.next = list1; // link existing node
                list1 = list1.next;
            } else {
                current.next = list2; // link existing node
                list2 = list2.next;
            }
            current = current.next;
        }

        current.next = (list1 != null) ? list1 : list2;
        return dummy.next;
    }
}
```

- **TC:** O(m+n): each input node is processed at most once.
- **Auxiliary SC:** O(1): one dummy node and a few references; no new result nodes.
- **Trade-off:** Re-links existing input nodes, so the original list structures are modified.

## Complexity summary

| Approach | Time | Auxiliary space | Allocates result nodes? |
|---|---|---|---|
| 1. Collect + sort | O((m+n) log(m+n)) | O(m+n) | Yes, all |
| 2. Two pointers + `\|\|` loop | O(m+n) | O(m+n) | Yes, all |
| 3. Two pointers + `&&` loop + remainder | O(m+n) | **O(m+n) worst case** | Some; remainder reused |
| 4. Reuse existing nodes | **O(m+n)** | **O(1)** | No (except dummy) |

> Complexity note: The earlier claim that Approach 3 has O(min(m,n)) space was incorrect. A single node in one list can be larger than all the values in a much longer other list, causing many new nodes to be allocated before either list ends.

## Similar problem / pattern recognition

- **Merge Two Sorted Arrays Without Extra Space:** both problems exploit sorted input and compare elements using two pointers; arrays require swaps/shifts or a gap-style technique when extra array storage is unavailable, whereas linked lists can reuse/relink existing nodes.
- **Merge Sorted Array (LeetCode 88):** same merge principle, but uses index pointers and often merges from the back to fit in `nums1`.
- **Merge step in Merge Sort:** identical sorted-sequence merge idea, using either array indices or linked-list references.

## Pitfalls to remember

- `current = new ListNode(value)` **reassigns a local reference**, not the previous node's `next`.
- `current.next = ...` **attaches** the next node; then `current = current.next` **advances** the result tail.
- Return `dummy.next`, **not** `dummy`.
- Handle equal values (`<=`) so a pointer always advances; each step should append/select one node.
- Once either input reaches null, append the entire remaining chain.
- Avoid magic sentinel values such as `-101` when null checks express the cases clearly.

## Takeaway for tracker

**Merge Two Sorted Lists — Two-pointer merge + dummy node.** My initial collect-and-sort approach was O((m+n) log(m+n)); I improved it to O(m+n) by comparing heads of already-sorted linked lists. `dummy` preserves the head, `current.next` attaches nodes, and `current = current.next` advances the tail. A clean `&&` loop compares while both lists exist, then attaches the remainder. Reusing all existing nodes reduces auxiliary space to O(1), while creating new result nodes uses O(m+n) space in the worst case. **Similar pattern:** merging two sorted arrays / merge step of merge sort.

# 61. Rotate List

**Problem:** [LeetCode 61 — Rotate List](https://leetcode.com/problems/rotate-list/)

**Pattern:** Linked List · Pointer Manipulation · Rotation · Modulo

Given a linked list, rotate it **right** by `k` positions.

```text
Input:  1 → 2 → 3 → 4 → 5 → null, k = 3
Output: 3 → 4 → 5 → 1 → 2 → null
```

A full `n` rotations returns the original list, so use `k %= n` (after checking for an empty list).

---

## Approach 1 — ArrayList (Brute Force)

**Idea:** Store values in an `ArrayList`, compute the effective rotation, and build a new linked list in the required order. This avoids manipulating the original node links.

> Initial thought: reverse the ArrayList, then insert values at the head. Be careful: reversing and repeated head insertion can cancel each other out. Direct indexing is easier and more reliable.

```java
class Solution {
    public ListNode rotateRight(ListNode head, int k) {
        if (head == null || k == 0) return head;

        List<Integer> values = new ArrayList<>();
        ListNode current = head;
        while (current != null) {
            values.add(current.val);
            current = current.next;
        }

        int n = values.size();
        k %= n;
        if (k == 0) return head;

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        // New list begins with the final k values.
        for (int i = 0; i < n; i++) {
            int originalIndex = (n - k + i) % n;
            tail.next = new ListNode(values.get(originalIndex));
            tail = tail.next;
        }

        return dummy.next;
    }
}
```

**Complexity**
- **TC:** `O(n)` — one traversal to collect values + one to rebuild. ArrayList access is `O(1)`.
- **SC:** `O(n)` — `n` stored values and `n` new result nodes.

---

## Approach 2 — Optimal: Reuse Existing Nodes (Pointer Relinking)

**Idea:** Calculate the size and preserve the old tail. The new tail is at **1-based position `n - (k % n)`**; its next node is the new head. Break at the new tail and attach the old tail to the original head.

```java
class Solution {
    public ListNode rotateRight(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) {
            return head;
        }

        // 1. Find list length and preserve old tail.
        int n = 1;
        ListNode oldTail = head;
        while (oldTail.next != null) {
            oldTail = oldTail.next;
            n++;
        }

        // 2. Ignore complete rotations.
        k %= n;
        if (k == 0) return head;

        // 3. Locate new tail (1-based position n - k).
        ListNode newTail = head;
        for (int i = 1; i < n - k; i++) {
            newTail = newTail.next;
        }

        // 4. Preserve the new head before cutting the link.
        ListNode newHead = newTail.next;

        // 5. Reconnect the two segments.
        newTail.next = null;
        oldTail.next = head;

        return newHead;
    }
}
```

**Complexity**
- **TC:** `O(n)` — length/tail traversal + traversal to the new tail.
- **Auxiliary SC:** `O(1)` — only a constant number of node references; no new nodes.

### Dry run: `1 → 2 → 3 → 4 → 5`, `k = 3`

```text
n = 5; k = 3 % 5 = 3; newTail position = 5 - 3 = 2

head         newTail newHead       oldTail
 ↓              ↓      ↓             ↓
 1 ───────────→ 2 ───→ 3 → 4 ───────→ 5 → null

Save newHead = newTail.next  // node 3
newTail.next = null         // 2 → null
oldTail.next = head         // 5 → 1

Returned from newHead:
3 → 4 → 5 → 1 → 2 → null
```

### Reference-manipulation lesson

| Statement | Meaning | Modifies node structure? |
|---|---|---|
| `current = current.next` | Move the local reference to another node | No |
| `current = new ListNode(7)` | Point `current` at a different object | No |
| `current.next = null` | Break the link inside the referenced node | Yes |
| `oldTail.next = head` | Connect the old last node to the original first node | Yes |
| `newHead = newTail.next` | Save a reference before breaking the link | No |

**Critical distinction:** `head`, `current`, `oldTail`, and `newHead` are reference variables. They may point to **different nodes** in the same chain. Reassigning a reference does not modify an existing node; assigning a node's `.next` changes the shared structure reachable through other references.

---

## Comparison

| Approach | Time | Auxiliary space | Mutates original nodes? |
|---|---|---|---|
| ArrayList + rebuild | `O(n)` | `O(n)` | No (for nonzero effective rotation) |
| Find tail + relink | `O(n)` | **`O(1)`** | Yes |

## Edge Cases

- Empty list → return `head` before calculating `k % n`.
- One-node list → unchanged.
- `k = 0` → unchanged.
- `k` is a multiple of `n` → unchanged.
- `k > n` → use `k %= n`.

## Similar Problems / Pattern Recognition

- **21. Merge Two Sorted Lists** — link nodes using references; dummy head and moving tail reference.
- **206. Reverse Linked List** — preserve `next` before rewiring pointers.
- **19. Remove Nth Node From End of List** — locate a split/removal position and update `.next`.
- **189. Rotate Array** — rotation and modulo, but arrays require different movement/reversal techniques.

## One-Paragraph Takeaway (Excel Tracker)

Rotate List — Initially considered collecting values in an ArrayList and rebuilding the rotated list (`O(n)` TC, `O(n)` SC). Optimized by finding the length and old tail, calculating effective rotations with `k % n`, locating the new tail at 1-based position `n-k`, preserving `newHead = newTail.next`, breaking `newTail.next`, and connecting `oldTail.next = head` (`O(n)` TC, `O(1)` auxiliary SC). Core learning: **moving a reference (`current = current.next`) does not change nodes; modifying `.next` changes the actual shared linked-list structure.**

# 707. Design Linked List (Java)

**Problem:** [LeetCode 707 — Design Linked List](https://leetcode.com/problems/design-linked-list/)

**Pattern:** Singly Linked List · Object References · Predecessor Node · Data Structure Design

## Core idea

Implement `get`, `addAtHead`, `addAtTail`, `addAtIndex`, and `deleteAtIndex` without using Java's built-in `LinkedList`. Each `MyLinkedList` object maintains its own state; nodes are separate objects connected using `next` references.

**Index rules:** Indices are zero-based. `get` returns `-1` for an invalid index. `addAtIndex(index, val)` inserts at the head for `index <= 0`, appends for `index == size`, and does nothing for `index > size`. `deleteAtIndex` ignores invalid indices.

---

## Approach 1 — Original: Head-only singly linked list

Maintain only `head`. Traverse to find an index, the tail, or the predecessor of a node.

```java
class MyLinkedList {
    private ListNode head;

    private static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public MyLinkedList() {
        head = null;
    }

    public int get(int index) {
        if (index < 0) return -1;

        ListNode current = head;
        int i = 0;
        while (current != null) {
            if (i == index) return current.val;
            current = current.next;
            i++;
        }
        return -1;
    }

    public void addAtHead(int val) {
        head = new ListNode(val, head);
    }

    public void addAtTail(int val) {
        if (head == null) {
            addAtHead(val);
            return;
        }

        ListNode current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = new ListNode(val);
    }

    public void addAtIndex(int index, int val) {
        if (index <= 0) {
            addAtHead(val);
            return;
        }

        ListNode current = head;
        int i = 0;
        while (current != null) {
            if (i == index - 1) {
                current.next = new ListNode(val, current.next);
                return;
            }
            current = current.next;
            i++;
        }
    }

    public void deleteAtIndex(int index) {
        if (head == null || index < 0) return;
        if (index == 0) {
            head = head.next;
            return;
        }

        ListNode current = head;
        int i = 0;
        while (current != null) {
            if (i == index - 1) {
                if (current.next != null) {
                    current.next = current.next.next;
                }
                return;
            }
            current = current.next;
            i++;
        }
    }
}
```

**Time:** `get` O(n), `addAtHead` O(1), `addAtTail` O(n), `addAtIndex` O(n), `deleteAtIndex` O(n). **Auxiliary space:** O(1) per operation. **Stored nodes:** O(n).

---

## Approach 2 — Better: Head + Size

Maintain a `size` counter to reject invalid indices immediately, without traversing. Tail insertion is still O(n).

```java
class MyLinkedList {
    private Node head;
    private int size;

    private static class Node {
        int val;
        Node next;
        Node(int val) { this.val = val; }
        Node(int val, Node next) { this.val = val; this.next = next; }
    }

    public MyLinkedList() {
        head = null;
        size = 0;
    }

    public int get(int index) {
        if (index < 0 || index >= size) return -1;
        Node current = head;
        for (int i = 0; i < index; i++) current = current.next;
        return current.val;
    }

    public void addAtHead(int val) {
        head = new Node(val, head);
        size++;
    }

    public void addAtTail(int val) {
        if (size == 0) {
            addAtHead(val);
            return;
        }
        Node current = head;
        while (current.next != null) current = current.next;
        current.next = new Node(val);
        size++;
    }

    public void addAtIndex(int index, int val) {
        if (index > size) return;
        if (index <= 0) {
            addAtHead(val);
            return;
        }
        Node previous = head;
        for (int i = 0; i < index - 1; i++) previous = previous.next;
        previous.next = new Node(val, previous.next);
        size++;
    }

    public void deleteAtIndex(int index) {
        if (index < 0 || index >= size) return;
        if (index == 0) {
            head = head.next;
        } else {
            Node previous = head;
            for (int i = 0; i < index - 1; i++) previous = previous.next;
            previous.next = previous.next.next;
        }
        size--;
    }
}
```

**Improvement:** `size` makes index validity checks O(1); valid indexed traversal still takes O(n), and `addAtTail` still takes O(n).

---

## Approach 3 — Optimized: Head + Tail + Size

Maintain these **invariants**:

- `size == 0` ⇒ `head == null` and `tail == null`.
- `size > 0` ⇒ `head != null`, `tail != null`, and `tail.next == null`.
- `size` equals the number of reachable nodes from `head`.

```java
class MyLinkedList {
    private Node head;
    private Node tail;
    private int size;

    private static class Node {
        int val;
        Node next;

        Node(int val) {
            this.val = val;
        }

        Node(int val, Node next) {
            this.val = val;
            this.next = next;
        }
    }

    public MyLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    public int get(int index) {
        if (index < 0 || index >= size) return -1;

        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.val;
    }

    public void addAtHead(int val) {
        Node node = new Node(val, head);
        head = node;
        if (size == 0) {
            tail = node; // First node is both head and tail
        }
        size++;
    }

    public void addAtTail(int val) {
        Node node = new Node(val);
        if (size == 0) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    public void addAtIndex(int index, int val) {
        if (index > size) return;
        if (index <= 0) {
            addAtHead(val);
            return;
        }
        if (index == size) {
            addAtTail(val);
            return;
        }

        Node previous = head;
        for (int i = 0; i < index - 1; i++) {
            previous = previous.next;
        }
        previous.next = new Node(val, previous.next);
        size++;
    }

    public void deleteAtIndex(int index) {
        if (index < 0 || index >= size) return;

        if (index == 0) {
            head = head.next;
            size--;
            if (size == 0) {
                tail = null; // Last node removed
            }
            return;
        }

        Node previous = head;
        for (int i = 0; i < index - 1; i++) {
            previous = previous.next;
        }

        if (previous.next == tail) {
            tail = previous; // Removing the last node
        }
        previous.next = previous.next.next;
        size--;
    }
}
```

### Complexity comparison

Let `n` be the current number of nodes. These are **worst-case** time bounds; successful insertion at the head and direct tail insertion are constant time.

| Operation | Head only | Head + size | Head + tail + size | Extra working space |
|---|---:|---:|---:|---:|
| Constructor | O(1) | O(1) | O(1) | O(1) |
| `get(index)` | O(n) | O(n) | O(n) | O(1) |
| `addAtHead(val)` | O(1) | O(1) | O(1) | O(1) |
| `addAtTail(val)` | O(n) | O(n) | **O(1)** | O(1) |
| `addAtIndex(index, val)` | O(n) | O(n) | O(n) | O(1) |
| `deleteAtIndex(index)` | O(n) | O(n) | O(n) | O(1) |

**Nuance:** In the optimized version, `addAtIndex(index == size)` is O(1) because it delegates to `addAtTail`. Invalid-index checks are O(1) with `size`. All versions store O(n) nodes overall; each insertion allocates one node, which is O(1) per call.

**Note:** A doubly linked list can traverse from the nearer end and makes deletion of a *known node* easier, but index-based `get`/insertion/deletion still have O(n) worst-case time. For this problem, head + tail + size in a singly linked list is a strong balance of simplicity and efficiency.

---

## Pointer manipulation — critical learning

Suppose `10 → 20 → 30`, and `current` points to node `10`.

**Insert 15 at index 1:**

```java
Node newNode = new Node(15, current.next); // 15.next → 20
current.next = newNode;                    // 10.next → 15
// 10 → 15 → 20 → 30
```

**Delete node 20 (index 1):**

```java
current.next = current.next.next;
// 10 → 30
```

**Moving vs modifying references:**

```java
current = current.next;       // Reassigns local reference; does NOT change links
current.next = new Node(40); // Changes the actual node's next link
```

Find the **predecessor** (`index - 1`) for indexed insertion/deletion. Always preserve any necessary link *before* overwriting it.

### Edge cases checklist

- [ ] Empty list: get/delete safely; head/tail insertion creates first node
- [ ] One-node list: deleting index 0 resets **both** head and tail
- [ ] Negative insertion index inserts at head
- [ ] Negative get/delete index is invalid
- [ ] `index == size` appends
- [ ] `index > size` insertion is ignored
- [ ] `index >= size` get/delete is invalid
- [ ] Removing the tail updates `tail`
- [ ] Adding after deleting the final node still works

### Related problems / patterns

- **21. Merge Two Sorted Lists** — dummy node and tail pointer for append
- **206. Reverse Linked List** — pointer reassignment and link reversal
- **19. Remove Nth Node From End of List** — find the predecessor, then bypass a node
- **61. Rotate List** — preserve head/tail references and reconnect links
- **707. Design Linked List** — maintain class-level invariants across operations

## Excel tracker takeaway

> **707. Design Linked List —** Built a custom singly linked list using an instance-level `head` and a static node class. Learned to locate the predecessor (`index - 1`) for insertion/deletion, preserve `next` links, and distinguish moving a reference (`current = current.next`) from modifying the shared node (`current.next = ...`). Improved the design from head-only to head + size and finally head + tail + size: `addAtTail` improves from O(n) to O(1), invalid-index checks become O(1), indexed operations remain O(n), and each operation uses O(1) auxiliary space. Maintain `head`, `tail`, and `size` invariants, especially for empty lists and tail deletion.

# 203. Remove Linked List Elements

**Problem:** [LeetCode 203 — Remove Linked List Elements](https://leetcode.com/problems/remove-linked-list-elements/)

**Pattern:** Linked List · Traversal · Previous/Current References · Dummy Node · In-place Deletion

## Problem

Given the `head` of a singly linked list and an integer `val`, remove **all nodes** whose value equals `val`, and return the updated head.

**Example**

```text
Input:  1 → 2 → 6 → 3 → 4 → 5 → 6 → null, val = 6
Output: 1 → 2 → 3 → 4 → 5 → null
```

## Approach Comparison

Let `n` = number of nodes in the input list.

| Approach | Time complexity | Auxiliary space complexity | Modifies input nodes? |
|---|---|---|---|
| 1. Brute force: ArrayList + rebuild | O(n) | O(n) | No |
| 2. Better: remove matching heads, then `previous`/`current` | O(n) | O(1) | Yes |
| 3. Clean optimal: dummy node | O(n) | O(1) | Yes |

> All approaches take O(n) time. Approaches 2 and 3 have optimal O(1) auxiliary space. Approach 3 is simpler because it does not special-case the head.

---

## 1. Brute Force — ArrayList + New Linked List

**Idea:** Store values not equal to `val`, then create a new linked list from those values.

```java
import java.util.ArrayList;
import java.util.List;

class Solution {
    public ListNode removeElements(ListNode head, int val) {
        List<Integer> values = new ArrayList<>();
        ListNode current = head;

        while (current != null) {
            if (current.val != val) {
                values.add(current.val);
            }
            current = current.next;
        }

        ListNode dummy = new ListNode(0);
        current = dummy;

        for (int value : values) {
            current.next = new ListNode(value);
            current = current.next;
        }

        return dummy.next;
    }
}
```

**TC:** O(n) — one traversal to collect and another to rebuild (at most `n` nodes).

**Auxiliary SC:** O(n) — ArrayList and new output nodes (O(n) total memory).

**Drawback:** Allocates extra nodes and an additional collection even though links can be updated directly.

---

## 2. Better — Your `previous` and `current` Approach

**Idea:** First remove consecutive matching nodes from the head. Then bypass matching nodes using the predecessor reference.

**Your code, cleaned up:** Removed the incorrect `if (val == 0) return head;` because zero is a valid value to remove.

```java
class Solution {
    public ListNode removeElements(ListNode head, int val) {
        // Remove matching nodes from the beginning.
        while (head != null && head.val == val) {
            head = head.next;
        }

        ListNode previous = head;
        ListNode current = head;

        while (current != null) {
            if (current.val == val) {
                previous.next = current.next;
                // Keep previous where it is after deletion.
            } else {
                previous = current;
            }
            current = current.next;
        }

        return head;
    }
}
```

**TC:** O(n) — removed head nodes are not revisited, and the remaining list is traversed once.

**Auxiliary SC:** O(1) — only a few node references; no new nodes.

**Critical pointer rule:** Advance `previous` **only when the node is kept**. When deleting `current`, keep `previous` at the last retained node so consecutive matches are also removed.

---

## 3. Clean Optimal — Dummy Node

**Idea:** Use a dummy predecessor before `head` so deleting the original head works exactly like deleting any other node.

```java
class Solution {
    public ListNode removeElements(ListNode head, int val) {
        ListNode dummy = new ListNode(0, head);
        ListNode current = dummy;

        while (current.next != null) {
            if (current.next.val == val) {
                current.next = current.next.next;
            } else {
                current = current.next;
            }
        }

        return dummy.next;
    }
}
```

**TC:** O(n) — each original node is checked once.

**Auxiliary SC:** O(1) — a single dummy node and a traversal reference.

**Why it's cleaner:** No separate loop for matching head nodes, and no second pointer is needed. The same condition removes nodes at the head, middle, or tail.

---

## Dry Run — Consecutive Matches

```text
Input:  6 → 6 → 1 → 6 → 6 → 2 → null
val:    6

Remove leading 6s:   head → 1 → 6 → 6 → 2
Remove middle 6s:    head → 1 → 2 → null
```

For the dummy-node version, when `current.next` matches, change `current.next` but **do not advance `current`**. That allows the next consecutive match to be checked.

## Edge Cases

- `head == null` → return `null`.
- All nodes match `val` → return `null`.
- Multiple matching nodes at the beginning or middle → remove all.
- No nodes match → return original list unchanged.
- `val == 0` → still remove matching nodes; do **not** return early.

## Pointer / Reference Learning

| Operation | Effect |
|---|---|
| `current = current.next` | Moves a local reference; does not alter the links |
| `previous.next = current.next` | Bypasses the current node in the actual list |
| `head = head.next` | Moves the head reference; useful for matching initial nodes |
| `current.next = current.next.next` | Bypasses the node immediately after `current` |

**Pattern recognition:** When deletion can affect the head, either handle the head separately or add a **dummy predecessor** to unify deletion cases.

## Similar Problems

- [19. Remove Nth Node From End of List](https://leetcode.com/problems/remove-nth-node-from-end-of-list/) — dummy node + predecessor/two pointers.
- [707. Design Linked List](https://leetcode.com/problems/design-linked-list/) — delete at index by rewiring predecessor's `next`.
- [83. Remove Duplicates from Sorted List](https://leetcode.com/problems/remove-duplicates-from-sorted-list/) — bypass unwanted nodes using `next`.

## Excel Tracker Takeaway

**203. Remove Linked List Elements — Traversal + Previous/Current + Dummy Node:** Initially considered collecting non-matching values in an ArrayList and rebuilding the list (TC O(n), SC O(n)). Improved to in-place deletion using `previous`/`current` (TC O(n), SC O(1)). Learned to remove consecutive matching head nodes separately and keep `previous` unchanged after deleting a node. The cleanest approach uses a dummy node to handle head, middle, tail, and consecutive deletions uniformly (TC O(n), SC O(1)).


# LeetCode 83 — Remove Duplicates from Sorted List

**Problem:** [83. Remove Duplicates from Sorted List](https://leetcode.com/problems/remove-duplicates-from-sorted-list/)

**Pattern:** Linked List · In-place deletion · Adjacent comparison (sorted list)

## Problem understanding

Given the head of a **sorted** singly linked list, remove duplicate nodes so each value appears **once**. Keep the original sorted order.

```text
Input:  1 → 1 → 2 → 3 → 3 → null
Output: 1 → 2 → 3 → null
```

**Key observation:** Because the list is sorted, duplicates are always adjacent. We only need to compare neighboring values; no HashSet is required.

## Approach 1 — Brute force: collect unique values, build a new list

Traverse the sorted list and collect a value only if it differs from the last collected value. Then create a **new** linked list using those values. This is simple but uses extra space and new nodes.

```java
import java.util.ArrayList;
import java.util.List;

class Solution {
    public ListNode deleteDuplicates(ListNode head) {
        List<Integer> unique = new ArrayList<>();
        ListNode current = head;

        while (current != null) {
            if (unique.isEmpty() || unique.get(unique.size() - 1) != current.val) {
                unique.add(current.val);
            }
            current = current.next;
        }

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        for (int value : unique) {
            tail.next = new ListNode(value);
            tail = tail.next;
        }
        return dummy.next;
    }
}
```

- **Time complexity:** O(n) — one pass to collect and one to rebuild.
- **Auxiliary space:** O(n) — ArrayList and newly created output nodes (O(n) total additional allocation).

## Approach 2 — Your solution: previous node + current node (optimal)

Your approach **reuses existing nodes** and removes a duplicate by bypassing its reference. `previousNode` stays on the last **kept** node when a duplicate is found.

```java
class Solution {
    public ListNode deleteDuplicates(ListNode head) {
        if (head == null) {
            return head;
        }

        ListNode current = head;
        ListNode previousNode = head;
        int previousValue = current.val;
        current = current.next;

        while (current != null) {
            int currentValue = current.val;

            if (currentValue == previousValue) {
                previousNode.next = current.next;
            } else {
                previousNode = current;
                previousValue = previousNode.val;
            }

            current = current.next;
        }
        return head;
    }
}
```

- **Time complexity:** O(n) — each node is visited once.
- **Auxiliary space:** O(1) — a fixed number of references and variables.
- **Good:** Handles empty lists, all-duplicate lists, and multiple consecutive duplicates.
- **Can simplify:** `previousValue` and a separate `previousNode` are not necessary because the sorted list allows comparison with `current.next` directly.

## Approach 3 — Clean optimal solution: one pointer

Keep `current` at the last retained node. If the next node has the same value, skip it. **Do not advance `current` after deletion**, because further duplicates might follow.

```java
class Solution {
    public ListNode deleteDuplicates(ListNode head) {
        ListNode current = head;

        while (current != null && current.next != null) {
            if (current.val == current.next.val) {
                current.next = current.next.next; // Skip duplicate node
            } else {
                current = current.next;           // Move only if values differ
            }
        }

        return head;
    }
}
```

- **Time complexity:** O(n).
- **Auxiliary space:** O(1).
- **Why it is cleaner:** No extra integer, no previous pointer, no dummy node, and no special case for `head == null`.

## Dry run — clean solution

```text
1 → 1 → 1 → 2 → 3 → 3
^ current

1 == next 1 → current.next = current.next.next
1 → 1 → 2 → 3 → 3
^ current stays here

1 == next 1 → skip again
1 → 2 → 3 → 3
^ current stays here

1 != 2 → move current to 2
2 != 3 → move current to 3
3 == next 3 → skip duplicate

Result: 1 → 2 → 3 → null
```

## Comparison

| Approach | Time | Auxiliary space | Creates new nodes? |
|---|---|---|---|
| ArrayList + rebuild (brute force) | O(n) | O(n) | Yes |
| Your `previousNode` + `current` | O(n) | O(1) | No |
| One-pointer in-place (clean optimal) | O(n) | O(1) | No |

## Reference-management lessons

- `previousNode.next = current.next` **bypasses** the duplicate. It does not delete the object from memory immediately; the removed node becomes unreachable from the returned list when no other references keep it alive.
- In your solution, on duplicates, **don't move `previousNode`**: it must remain at the last retained node.
- In the one-pointer solution, on duplicates, **don't move `current`**: you must check whether another duplicate follows.
- Because the list is **sorted**, checking adjacent nodes is sufficient. This exact approach does not remove all duplicates in an **unsorted** list.
- This problem (LC 83) keeps **one occurrence** of each value. LC 82 removes **all values that appear more than once**; they are different tasks.

## One-paragraph takeaway (Excel-ready)

Initially, I used `previousNode`, `current`, and `previousValue` to remove duplicates by updating `previousNode.next` whenever two values matched. My solution worked with O(n) time and O(1) space, but I learned that because the list is sorted, I can simply compare `current.val` with `current.next.val`. If they match, I skip the next node without moving `current`; otherwise, I move forward. The key learning is to keep the pointer on the last valid node until all consecutive duplicates are removed.



# LeetCode 24 — Swap Nodes in Pairs

**Problem:** Given the head of a singly linked list, swap every two adjacent **nodes** and return the new head. Do not swap node values.

**Example:** `1 → 2 → 3 → 4 → 5 → null` becomes `2 → 1 → 4 → 3 → 5 → null`.

**Pattern:** Linked-list pointer manipulation · Pairwise reversal · Dummy node (cleaner solution)

## 1. Initial attempt — what I learned

My `swapPair(first, second)` helper correctly reversed one pair:

```java
public void swapPair(ListNode first, ListNode second) {
    ListNode nextPair = second.next;
    second.next = first;
    first.next = nextPair;
}
```

But a working solution needs **three separate jobs**:

1. Swap references **inside** the pair.
2. Connect the **previous swapped pair's tail** to the new pair's head.
3. Advance to the **next unswapped pair**.

Originally, I handled job 1 but missed job 2. After swapping `(3,4)`, node `1` still pointed to `3` instead of `4`.

**Reference lesson:** `previousTail.next = current.next` changes the `next` field belonging to `previousTail`. It does **not** change `current.next`, because these are fields on different node objects. Also, `previousTail = current` only reassigns a local variable; it does not modify the linked list.

## 2. Brute force — collect node references in an array

Store **node references**, not values; swap each adjacent pair in the array; then reconnect all the nodes in array order. This obeys the requirement not to swap node values, but costs extra memory.

```java
import java.util.ArrayList;
import java.util.List;

class Solution {
    public ListNode swapPairs(ListNode head) {
        if (head == null || head.next == null) return head;

        List<ListNode> nodes = new ArrayList<>();
        for (ListNode curr = head; curr != null; curr = curr.next) {
            nodes.add(curr);
        }

        for (int i = 0; i + 1 < nodes.size(); i += 2) {
            ListNode temp = nodes.get(i);
            nodes.set(i, nodes.get(i + 1));
            nodes.set(i + 1, temp);
        }

        for (int i = 0; i + 1 < nodes.size(); i++) {
            nodes.get(i).next = nodes.get(i + 1);
        }
        nodes.get(nodes.size() - 1).next = null;
        return nodes.get(0);
    }
}
```

- **Time:** O(n) — collect, swap pairs, reconnect.
- **Auxiliary space:** O(n) — array of node references.
- **Learning:** Easier reasoning, but uses extra space; nodes themselves are reused.

## 3. My iterative approach — corrected version

This stays closest to my own solution: keep `previousTail`, skip the first connection, and use the pair-swapping helper.

```java
class Solution {
    private void swapPair(ListNode oddPosition, ListNode evenPosition) {
        ListNode nextPair = evenPosition.next;
        evenPosition.next = oddPosition;
        oddPosition.next = nextPair;
    }

    public ListNode swapPairs(ListNode head) {
        if (head == null || head.next == null) return head;

        ListNode current = head;
        ListNode result = head.next;
        ListNode previousTail = null;

        while (current != null && current.next != null) {
            // Connect the previous pair to the new head of this pair.
            if (previousTail != null) {
                previousTail.next = current.next;
            }

            swapPair(current, current.next);

            // The old first node is now the tail of this pair.
            previousTail = current;
            current = current.next;
        }
        return result;
    }
}
```

- **Time:** O(n) — each pair is processed once.
- **Auxiliary space:** O(1) — only a few references.
- **Learning:** My logic works; replacing `notFirstTime` with `previousTail != null` removes an unnecessary boolean.

### Why the ordering matters

Before swapping `(3,4)`, the list's first part is `2 → 1 → 3 → 4 → 5`:

```text
previousTail = node 1
current      = node 3
current.next = node 4

previousTail.next = current.next;  // 1.next = 4
swapPair(current, current.next);    // 4.next = 3; 3.next = 5
previousTail = current;             // tail becomes node 3
current = current.next;             // moves to node 5
```

Connecting before the swap works because `current.next` still points to the second node, which will become the pair's new head.

## 4. Clean optimal solution — dummy node

This is the **more interview-friendly** version. A dummy node gives every pair a preceding node, including the first pair, so no `result`, first-iteration flag, or helper is needed.

```java
class Solution {
    public ListNode swapPairs(ListNode head) {
        ListNode dummy = new ListNode(0, head);
        ListNode prev = dummy;

        while (prev.next != null && prev.next.next != null) {
            ListNode first = prev.next;
            ListNode second = first.next;

            first.next = second.next; // 1 → 3 (preserve rest)
            second.next = first;      // 2 → 1
            prev.next = second;       // dummy/previous tail → 2

            prev = first;             // old first is new pair tail
        }
        return dummy.next;
    }
}
```

- **Time:** O(n).
- **Auxiliary space:** O(1) — one dummy node plus references (constant space).
- **Why cleaner:** `prev` always points to the node **before** the current pair, so the same logic works for the first and all later pairs.

### Dry run — `1 → 2 → 3 → 4 → 5`

| Iteration | `prev` before | `first` | `second` | List after swap | `prev` after |
|---|---|---|---|---|---|
| 1 | dummy | 1 | 2 | `dummy → 2 → 1 → 3 → 4 → 5` | 1 |
| 2 | 1 | 3 | 4 | `dummy → 2 → 1 → 4 → 3 → 5` | 3 |
| Stop | 3 | 5 | null | Only one node remains | — |

### How to remember the three assignments

1. **Preserve rest:** `first.next = second.next`.
2. **Reverse pair:** `second.next = first`.
3. **Attach pair:** `prev.next = second`.
4. **Advance:** `prev = first` (because `first` became the tail).

## 5. Comparison

| Approach | Time | Auxiliary space | Main idea |
|---|---|---|---|
| Brute force: array of node references | O(n) | O(n) | Rearrange references in array, then reconnect |
| My corrected helper + previous tail | O(n) | O(1) | Swap each pair, connect previous pair, advance |
| Clean optimal: dummy node | O(n) | O(1) | `prev` anchors every pair; three link updates |

## 6. One-paragraph takeaway

I could swap two adjacent nodes correctly, but initially failed to connect successive swapped pairs. I learned that changing `previousTail.next` does not change `current.next`: every node owns its own `next` field, and reassigning a local reference is different from rewiring a link. My O(1)-space solution works by connecting the previous tail to the second node **before** swapping, then moving to the next pair. The dummy-node version simplifies this further because `prev` consistently represents the node before the current pair, eliminating first-pair special cases.

## 7. Excel tracker takeaway (short)

**Pattern:** Linked-list reference manipulation / pair swapping / dummy node  
**Takeaway:** Initially swapped each pair but missed connecting consecutive pairs. Learned that `prev.next` and `current.next` are independent fields; link the previous tail to the new pair head before rewiring. Dummy node removes first-pair special cases. **TC O(n), SC O(1)** optimal.
