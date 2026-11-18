# 643. Maximum Average Subarray I

**LeetCode:** https://leetcode.com/problems/maximum-average-subarray-i/  
**Pattern:** Fixed-Size Sliding Window  
**Learning progression:** Brute Force → Optimize repeated work → Sliding Window

---

## Problem

Given an integer array `nums` and an integer `k`, find the **maximum average** of any **contiguous subarray of exactly `k` elements**.

**Example:**

```text
nums = [1, 12, -5, -6, 50, 3], k = 4

Windows:
[ 1, 12, -5, -6] → sum =  2 → average =  0.50
[12, -5, -6, 50] → sum = 51 → average = 12.75
[-5, -6, 50,  3] → sum = 42 → average = 10.50

Answer = 12.75
```

---

## Approach 1 — Brute Force (My First Solution)

### Idea

Start from every valid index (`0` through `n - k`), calculate the sum of the next `k` elements using an inner loop, and maintain the maximum sum. Divide by `k` at the end.

### Clean Java Code

```java
class Solution {
    public double findMaxAverage(int[] nums, int k) {
        int maxSum = Integer.MIN_VALUE;

        // Consider each possible start of a k-sized subarray.
        for (int start = 0; start <= nums.length - k; start++) {
            int sum = 0;

            // Recalculate all k elements for this window.
            for (int i = start; i < start + k; i++) {
                sum += nums[i];
            }

            maxSum = Math.max(maxSum, sum);
        }

        return (double) maxSum / k;
    }
}
```

### Complexity

- **Time: O((n - k + 1) × k), simplified to O(n × k)** because there are `n - k + 1` windows and we sum `k` numbers for each one. **Worst case O(n²)** when `k` grows proportionally with `n`. For fixed small `k`, it is O(n).
- **Space: O(1)** — only loop counters, sum, and max are stored.

### Why Optimize?

Adjacent windows overlap by `k - 1` elements, yet the brute-force approach adds those elements again. We can reuse the previous sum.

---

## Approach 2 — Optimized Fixed-Size Sliding Window (My Second Solution)

### Core Idea

1. Calculate the sum of the **first `k` elements**.
2. Store it as both `windowSum` and `maxSum`.
3. For each new element, **add the incoming right element** and **subtract the outgoing left element**.
4. Update `maxSum` and divide by `k` once at the end.

### Clean Java Code

```java
class Solution {
    public double findMaxAverage(int[] nums, int k) {
        int windowSum = 0;

        // Build the first full window.
        for (int i = 0; i < k; i++) {
            windowSum += nums[i];
        }

        int maxSum = windowSum;

        // Move the fixed-size window one step at a time.
        for (int i = k; i < nums.length; i++) {
            windowSum += nums[i];       // Incoming element
            windowSum -= nums[i - k];   // Outgoing element
            maxSum = Math.max(maxSum, windowSum);
        }

        return (double) maxSum / k;
    }
}
```

### Complexity

- **Time: O(n)** — first loop visits `k` values and the second visits `n - k` values; total `k + (n - k) = n`.
- **Space: O(1)** — no extra array or collection.

---

## How to Identify the Pattern

Look for these clues:

- The question involves a **contiguous subarray/substring**.
- The window length is **fixed at `k`**.
- You need the **maximum/minimum sum, average, or another aggregatable property** of each `k`-sized window.

```text
Contiguous elements
+ Fixed size k
+ Max/min/sum/average
→ Fixed-Size Sliding Window
```

**Pattern recognition:** If adjacent windows share most elements, ask whether you can update the previous window's result rather than recomputing it.

---

## Key Formula

```text
newWindowSum = oldWindowSum - outgoingLeft + incomingRight

windowSum = windowSum - nums[i - k] + nums[i]
```

Here, `i` is the index of the incoming element and `i - k` is the index of the outgoing element.

---

## Dry Run

Input: `nums = [1, 12, -5, -6, 50, 3]`, `k = 4`.

| Window | Outgoing | Incoming | Current sum | Max sum |
|---|---:|---:|---:|---:|
| `[1, 12, -5, -6]` | — | — | `2` | `2` |
| `[12, -5, -6, 50]` | `1` | `50` | `2 - 1 + 50 = 51` | `51` |
| `[-5, -6, 50, 3]` | `12` | `3` | `51 - 12 + 3 = 42` | `51` |

**Result:** `51 / 4 = 12.75`.

---

## Important State: `windowSum` vs `maxSum`

```text
windowSum → sum of the CURRENT window
maxSum    → maximum window sum seen SO FAR
```

Never use `maxSum` to slide to the next window. The next update must be based on the actual **current** window sum, not the best historical sum.

---

## Why Divide Only at the End?

Every subarray has the same length `k`, so maximizing the **sum** also maximizes its **average**. Track sums as integers and compute `(double) maxSum / k` only after finding the maximum.

The `(double)` cast avoids integer division and preserves the fractional result.

---

## Approach Comparison

| Approach | Method | Time Complexity | Space Complexity |
|---|---|---|---|
| 1. Brute Force | Recompute each window's `k` values | O(n × k), worst O(n²) | O(1) |
| 2. Sliding Window | Reuse previous sum, subtract left and add right | **O(n)** | **O(1)** |

---

## Final Takeaway (Excel / Revision)

Initially solved using nested loops by recalculating the sum of every `k`-sized contiguous subarray (O(n × k), worst O(n²)). Then learned the **fixed-size sliding window** pattern: calculate the first window once, subtract the outgoing left element and add the incoming right element instead of recomputing the overlapping values. This reduces time complexity to **O(n)** with **O(1)** extra space. Keep `windowSum` (current state) separate from `maxSum` (best answer), and divide by `k` only at the end.

### Quick Recall

```text
Fixed window k
→ Sum first k elements
→ Slide: - outgoing + incoming
→ Update maxSum
→ Return (double) maxSum / k
```

# 3. Longest Substring Without Repeating Characters

**Problem:** [LeetCode 3](https://leetcode.com/problems/longest-substring-without-repeating-characters/)

## Pattern

**Variable-Size Sliding Window** + **HashSet / HashMap**

Find the length of the longest **contiguous substring** without repeated characters.

## How to Identify the Pattern

- The problem asks for a **longest substring** (contiguous characters, not a subsequence).
- A window is valid only when **all characters inside it are unique**.
- We can expand the window to the right, and when a duplicate appears, shrink or move its left boundary.

```text
Contiguous substring
+ maximize length
+ condition: no duplicate characters
→ Variable-size sliding window
```

---

## Approach 1 — Brute Force (My Initial Approach, Corrected)

**Idea:** Start at every index and build a fresh `HashSet`. Keep extending the substring until a duplicate appears, then start again from the next index.

```java
import java.util.HashSet;
import java.util.Set;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        int longest = 0;

        for (int index = 0; index < s.length(); index++) {
            Set<Character> capturedChars = new HashSet<>();
            int counter = 0;

            for (int innerIndex = index; innerIndex < s.length(); innerIndex++) {
                char current = s.charAt(innerIndex);
                if (capturedChars.contains(current)) {
                    break;
                }
                capturedChars.add(current);
                counter++;
            }

            // Must update even when no duplicate is found.
            longest = Math.max(longest, counter);
        }

        return longest;
    }
}
```

**Bug corrected from my original code:** Updating `longest` only in the duplicate (`else`) branch fails on an all-unique string such as `"abc"`. Update it after each inner loop instead. `clear()` is unnecessary because a new set is created for each outer iteration.

- **Time:** O(n²) worst case, as the inner scan can reprocess many suffixes. (More precisely, O(n × min(n, A)) with an alphabet of size `A`.)
- **Auxiliary space:** O(min(n, A)) for the set.

---

## Approach 2 — My First Optimized HashSet Sliding Window

**Idea:** Do not restart at every index. Keep a set of characters in the current window. When the new character is already present, remove characters from the left, including the previous occurrence of the duplicate, before adding the new character.

```java
import java.util.HashSet;
import java.util.Set;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        int longest = 0;
        int left = 0;

        Set<Character> seen = new HashSet<>();
        for (int right = 0; right < s.length(); right++) {
            if (seen.contains(s.charAt(right))) {
                while (left < right) {
                    seen.remove(s.charAt(left));
                    if (s.charAt(left) == s.charAt(right)) {
                        left++;
                        break;
                    }
                    left++;
                }
            }
            seen.add(s.charAt(right));
            longest = Math.max(longest, (right - left) + 1);
        }
        return longest;
    }
}
```

- **Time:** O(n) amortized. Although `while` is nested inside `for`, `left` and `right` each advance at most `n` times.
- **Auxiliary space:** O(min(n, A)).
- **Observation:** Correct approach, but nested `if` + `while` + `break` can be simplified.

---

## Approach 3 — Cleaner HashSet Sliding Window

**Window invariant:** `seen` contains **exactly** the characters in `s[left..right]` after processing each iteration, and there are no duplicates.

```java
import java.util.HashSet;
import java.util.Set;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        int longest = 0;
        int left = 0;
        Set<Character> seen = new HashSet<>();

        for (int right = 0; right < s.length(); right++) {
            char current = s.charAt(right);

            while (seen.contains(current)) {
                seen.remove(s.charAt(left));
                left++;
            }

            seen.add(current);
            longest = Math.max(longest, right - left + 1);
        }
        return longest;
    }
}
```

**Why `while`?** The duplicate may not be at `left`. We must remove *every* character before it, then remove the earlier duplicate, preserving a **contiguous**, valid window.

- **Time:** O(n) amortized; each character is added and removed at most once.
- **Auxiliary space:** O(min(n, A)).

---

## Approach 4 — HashMap Last-Seen Index (Jump `left` Directly)

**Idea:** Store `character → most recent index`. When we see the same character again, jump `left` to just after its previous occurrence **only if that position is ahead of the current `left`**.

```java
import java.util.HashMap;
import java.util.Map;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        int longest = 0;
        int left = 0;
        Map<Character, Integer> lastSeen = new HashMap<>();

        for (int right = 0; right < s.length(); right++) {
            char current = s.charAt(right);

            if (lastSeen.containsKey(current)) {
                left = Math.max(left, lastSeen.get(current) + 1);
            }

            lastSeen.put(current, right);
            longest = Math.max(longest, right - left + 1);
        }
        return longest;
    }
}
```

**Why `Math.max()`?** The map may contain a character's last-seen index **outside the current window**, since old entries are not removed. `left` must never move backward. An equivalent implementation is to update `left` only if `lastSeen.get(current) >= left`.

**Why `lastSeen.get(current) + 1`?** The previous occurrence of `current` must be **excluded** from the new valid substring. If the previous duplicate was at index `p`, the new window must begin at `p + 1`, not `p`.

### Smallest duplicate example — `"SS"`

| `right` | Character | Previous index | New `left` | Valid window | Length |
|---:|:---:|:---:|---:|:---:|---:|
| 0 | S | — | 0 | `"S"` | 1 |
| 1 | S | 0 | `max(0, 0 + 1) = 1` | `"S"` | 1 |

```java
left = Math.max(left, lastSeen.get('S') + 1);
// previous S at 0, new left = 1; exclude the old S
```

Without `+1`, `left` would stay at `0` and the supposed window `"SS"` would incorrectly include two copies of `S`.

**Do not confuse the two `+1`s:**

- `lastSeenIndex + 1`: advance `left` **past the prior duplicate**.
- `right - left + 1`: calculate the **inclusive window length** (so a single-character window has length `1`, not `0`).

- **Time:** O(n) expected (assuming average O(1) HashMap operations).
- **Auxiliary space:** O(min(n, A)); map entries can remain for characters outside the current window.
- **Important:** The HashMap version removes the step-by-step shrinking loop, but **does not improve asymptotic time complexity** beyond the HashSet's O(n).

---

## Dry Run — `"ccbbcc"` (HashMap)

| `right` | Char | Previous index | `left` after update | `lastSeen` after update | Valid window | `longest` |
|---:|:---:|:---:|---:|---|---|---:|
| 0 | c | — | 0 | `{c=0}` | `"c"` | 1 |
| 1 | c | 0 | 1 | `{c=1}` | `"c"` | 1 |
| 2 | b | — | 1 | `{c=1, b=2}` | `"cb"` | 2 |
| 3 | b | 2 | 3 | `{c=1, b=3}` | `"b"` | 2 |
| 4 | c | 1 | **3** | `{c=4, b=3}` | `"bc"` | 2 |
| 5 | c | 4 | 5 | `{c=5, b=3}` | `"c"` | 2 |

At `right = 4`, the earlier `c` is at index `1`, but `left = 3`. So:

```java
left = Math.max(3, 1 + 1); // 3, not 2
```

The earlier `c` is **outside** the current substring, and moving `left` backward would break the window invariant.

**Answer:** `2` (`"cb"` or `"bc"`).

---

## Complexity Comparison

Let `n` be the string length and `A` the alphabet size.

| Approach | Time (worst/expected as noted) | Auxiliary space | Key technique |
|---|---|---|---|
| Brute force + fresh HashSet | O(n²) worst case | O(min(n, A)) | Recalculate from each start |
| My HashSet sliding window | O(n) expected | O(min(n, A)) | Remove from `left` until duplicate is gone |
| Cleaner HashSet sliding window | O(n) expected | O(min(n, A)) | `while (seen.contains(current))` |
| HashMap last-seen indices | O(n) expected | O(min(n, A)) | Jump `left` with `Math.max` |

**Space nuance:** With a fixed character alphabet, `A` is constant, so space is technically O(1) with respect to `n`; we usually write **O(min(n, A))** to show the storage dependency. The typical HashSet/HashMap solutions **still require auxiliary tracking**; their space does not drop to O(1) independent of alphabet size. A fixed-size character array is an alternative for a known bounded alphabet, with its own constant storage cost.

---

## Final Takeaway — My Learning

To find the longest substring without repeating characters, I first used nested loops and a HashSet to recalculate valid substrings from each starting index, giving **O(n²)** worst-case time. I realized I could reduce repeated work using a **variable-size sliding window**: expand using `right`, and when a duplicate occurs, remove characters from `left` until the window is valid again. A HashSet keeps only the characters inside the **current window**, so shrinking automatically removes outdated entries. Both my initial and cleaned-up HashSet solutions achieve **O(n)** time and **O(min(n, alphabet size))** space. To avoid removing elements one by one, a HashMap can store each character's **last-seen index**, allowing `left` to jump directly. Since HashMap retains indices of characters already outside the window, use **`left = Math.max(left, lastSeenIndex + 1)`**: `+1` excludes the previous duplicate (e.g., `"SS"`), and `Math.max` prevents `left` from moving backward (as shown by `"ccbbcc"`). The HashMap version simplifies pointer movement, but **both optimized approaches have O(n) time**, and both still use space to track characters.

### One-Liner for Excel Tracker

**Longest unique substring → variable sliding window:** Brute force repeats scans O(n²); HashSet shrinks from `left` until duplicate removed; HashMap stores last-seen index and jumps using `Math.max(left, prev + 1)` because stale entries remain. Both optimized approaches: **O(n) time, O(min(n, alphabet)) space**.
