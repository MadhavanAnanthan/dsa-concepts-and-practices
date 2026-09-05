## Pair Sum / Two Sum

Pattern: Hashing / Complement

### Brute Force

* Check every pair using 2 loops.
* Time: `O(n²)`
* Space: `O(1)`

### Optimal

* Calculate required value:
  `needed = target - current`
* Check whether `needed` was already seen.

**If only pair values are needed**

* Use `HashSet`.

**If indices are needed**

* Use `HashMap<number, index>`.
* If `needed` exists → return stored index + current index.

Time: `O(n)` average
Space: `O(n)`

### Remember

Target is known → calculate the missing/complement value → use hashing for fast lookup.


### Pattern Hint
Need pair whose sum = target
→ Calculate complement
→ `needed = target - current`
→ Fast lookup using HashSet/HashMap

## Find Maximum in Array

Pattern: Linear Traversal / Running Maximum

Idea:
- Start `max = first element`
- Traverse remaining elements
- If current > max, update max

Time: O(n)
Space: O(1)

Avoid:
Sorting → O(n log n)

## Check if Array is Sorted

Pattern: Traversal / Adjacent Comparison

- Compare `arr[i-1]` with `arr[i]`.
- If previous > current → unsorted.
- Otherwise continue.

Time: O(n)
Space: O(1)

Already optimal.

## Second Largest Element

Pattern: Traversal / Track Top Two

- Start `max` and `secondMax` with `Integer.MIN_VALUE`.
- If `current > max` → `secondMax = max`, then update `max`.
- Else if `secondMax < current < max` → update `secondMax`.

Time: O(n)
Space: O(1)

Already optimal.

## Intersection of Two Arrays

Pattern: Hashing / Set Intersection

- Store `nums1` values in a HashSet.
- Traverse `nums2`.
- If value exists in first Set → add to result Set.
- Convert result Set to array manually.

Time: O(n + m) average
Space: O(n + k)

## Reverse Array / String

Pattern: Two Pointers

### Brute Force
- Create another array.
- Copy elements from end to start.
- Time: O(n)
- Space: O(n)

### Optimal
- Keep `left` at start and `right` at end.
- Swap both values.
- Move `left++` and `right--`.
- Stop when `left >= right`.

Time: O(n)
Space: O(1)

Remember:
Two pointers reduces extra space, not time complexity here.

## Slow / Fast Two Pointer - How to Decide Correctly

Pattern: Two Pointers / Slow-Fast

### First understand the question
Before choosing pointer positions, ask:

- What should `fastPointer` do?
- What should `slowPointer` track?
- Is the first element already valid, or must it also be checked?
- Am I reading values, writing values, or both?

### General Rule

- `fastPointer` → scans/reads elements.
- `slowPointer` → tracks the valid/write position.

Do **not** decide `fast = 0` or `fast = 1` by memorizing previous problems.

Decide based on pointer meaning.

### Example: Remove Duplicates

- First element is already unique.
- `slowPointer` = last unique position.
- `fastPointer` = next element to check.

So:

`slow = 0`  
`fast = 1`

### Example: Move Zeroes

- Every element must be checked, including index `0`.
- `slowPointer` = next position for a non-zero.
- `fastPointer` = scan all values.

So:

`slow = 0`  
`fast = 0`

### Memory Rule

Question meaning
→ define each pointer's responsibility
→ then decide starting positions
→ then test edge cases.

Do not choose pointer positions only because a few test cases pass.

## Two Pointers

Main variations:
- Opposite ends → left/right
- Slow/Fast → one reads, one tracks/writes
- Different speeds → slow 1 step, fast 2 steps

Examples:
- Reverse Array → opposite ends
- Remove Duplicates → slow/fast
- Move Zeroes → slow/fast
- Linked List Cycle → different speeds

## Maximum Consecutive Ones

Pattern: Traversal / Running Count

- `oneCounter` → current consecutive 1s.
- If current = 1 → increment count.
- Update maximum count.
- If current = 0 → reset count to 0.

Time: O(n)
Space: O(1)

Remember:
Consecutive/streak problem
→ maintain current count + best count.

## Best Time to Buy and Sell Stock

Pattern: Traversal / Running Minimum + Greedy

- Track lowest price seen so far.
- For each current price, calculate:
  `profit = currentPrice - minPrice`
- Keep maximum profit.

Time: O(n)
Space: O(1)

Remember:
Buy must happen before sell, so keep the minimum price from the past.

# Binary Search - 3 Core Variations

## 1. Normal Binary Search

Problem:
Find target index in sorted array.

Pattern:
Binary Search / Search Space Reduction

Rules:
- `start = 0`
- `end = nums.length - 1`
- `middle = start + (end - start) / 2`

If:
- `nums[middle] == target` → return middle
- `target < nums[middle]` → `end = middle - 1`
- `target > nums[middle]` → `start = middle + 1`

If loop ends → return `-1`.

Time: O(log n)
Space: O(1)

Remember:
Found target → stop immediately.

## 2. Search Insert Position

Problem:
Find target index.
If not found, return where it should be inserted.

Pattern:
Binary Search / Lower-Bound Style

Use normal binary search.

If target is found:
- return middle

If target is not found:
- when loop ends, `start` is the insertion position.

Time: O(log n)
Space: O(1)

Remember:
After binary search ends,
`start` = correct insertion index.

## 3. First and Last Position

Problem:
Find first and last occurrence of target.

Pattern:
Binary Search / Boundary Search

### First Occurrence
If target found:
- save `middle`
- continue searching LEFT
- `end = middle - 1`

### Last Occurrence
If target found:
- save `middle`
- continue searching RIGHT
- `start = middle + 1`

If target is never found:
- answer remains `-1`

Time: O(log n)
Space: O(1)

Remember:
Normal binary search → found = stop.
Boundary binary search → found = save + keep searching.