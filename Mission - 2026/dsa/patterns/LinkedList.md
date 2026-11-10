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
