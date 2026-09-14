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

---

## Two Sum II - Input Array Is Sorted

Pattern: Opposite-End Two Pointers

### Idea

* Array is already sorted.
* Start one pointer at the smallest value and another at the largest value.
* Calculate:
  `sum = numbers[left] + numbers[right]`

**If sum == target**

* Pair is found.

**If sum > target**

* Sum is too large.
* Move `right--` to get a smaller value.

**If sum < target**

* Sum is too small.
* Move `left++` to get a larger value.

Time: `O(n)`
Space: `O(1)`

### Remember

Because the array is sorted:

* Need smaller sum → move `right`.
* Need larger sum → move `left`.

### Pattern Hint

Sorted array + need pair whose sum = target + constant extra space
→ Opposite-end two pointers
→ `left = 0`, `right = n - 1`
→ Use the sum to decide which pointer to move.


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

## Two Pointer Selection Rule

### Slow / Fast
Use when:
- one pointer scans
- one pointer writes/tracks valid position
- removing/filtering/compacting

Examples:
- Remove Duplicates
- Move Zeroes
- Remove Element

### Opposite Ends
Use when:
- array is sorted
- answer depends on both extremes
- compare left vs right
- largest/smallest candidate can be at either end

Examples:
- Reverse Array
- Sorted Squares
- Two Sum II

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

## Valid Anagram

Pattern: Frequency Counting

### Lowercase `a-z`
- Use `int[26]`.
- Map character to index using:
  `char - 'a'`
- Increment for first string.
- Decrement for second string.
- All counts must end at `0`.

Time: O(n)  
Space: O(1)

### ASCII Reference
- `'A'` = 65
- `'Z'` = 90
- `'a'` = 97
- `'z'` = 122

Examples:
- `'a' - 'a'` → `97 - 97 = 0`
- `'z' - 'a'` → `122 - 97 = 25`

### Mixed Uppercase + Lowercase
If input may contain both `A-Z` and `a-z`:

- Use `int[128]` for ASCII characters, or
- Use `HashMap<Character, Integer>`.

With ASCII array:

`frequency[s.charAt(i)]++`  
`frequency[t.charAt(i)]--`

### Broader / Unknown Characters
- Use `HashMap<Character, Integer>`.

### Remember
- Small fixed character range → frequency array.
- Unknown/large character range → HashMap.
- Frequency array is usually faster and uses constant space when the character range is fixed.

## Single Number

Pattern: Frequency Counting / XOR

### Frequency Array
- Count occurrence of each number.
- Return the number whose frequency is `1`.
- For negative values, use an offset to map them to valid array indexes.

Example:
`index = value + offset`

If range is `-30000` to `30000`:
- array size = `60001`
- offset = `30000`

Time: O(n + range)
Space: O(range)

### Optimal - XOR
XOR rules:
- `x ^ x = 0`
- `x ^ 0 = x`
- Order does not matter.

Since every number appears twice except one:
- duplicate pairs cancel each other.
- remaining value is the single number.

Example:
`[4,1,2,1,2]`

`4 ^ 1 ^ 2 ^ 1 ^ 2`
→ `1 ^ 1 = 0`
→ `2 ^ 2 = 0`
→ remaining = `4`

Time: O(n)
Space: O(1)

Remember:
Every value appears exactly twice except one
→ think XOR.

## Missing Number

Pattern: Math / XOR

### Sum Approach
- Expected numbers are from `0` to `n`.
- Sum all expected values.
- Subtract every number present in input.
- Remaining value is the missing number.

Example:
`nums = [3,0,1]`

Expected:
`0 + 1 + 2 + 3 = 6`

Actual:
`3 + 0 + 1 = 4`

Missing:
`6 - 4 = 2`

Time: O(n)
Space: O(1)

### XOR Approach
XOR expected numbers `0..n`
with all numbers present in the array.

Matching numbers cancel:
- `x ^ x = 0`
- remaining value = missing number.

Time: O(n)
Space: O(1)

Remember:
Expected range and actual values differ by exactly one number
→ subtraction or XOR can reveal it.

## Merge Sorted Array

Pattern: Two Pointers / Three Pointers / Merge from End

- `p1` → last valid element in `nums1`
- `p2` → last element in `nums2`
- `writeInPlace` → last available position in `nums1`

### Approach
- Compare `nums1[p1]` and `nums2[p2]`.
- Put the larger value at `writeInPlace`.
- Move that pointer backward.
- Move `writeInPlace` backward.
- Continue while both arrays still have values.
- If `nums2` still has values left, copy them into `nums1`.

### Why merge from the end?
`nums1` already has free space at the end.

So filling from right to left avoids:
- shifting existing elements
- creating another array
- sorting everything again

### Complexity

Time: `O(m + n)`

Why?
- Each valid element from `nums1` is processed at most once.
- Each element from `nums2` is processed at most once.

So:

`m + n` operations → `O(m + n)`

Space: `O(1)`

Why?
- No extra array, Set, or Map is created.
- Only a few integer variables are used:
  - `p1`
  - `p2`
  - `writeInPlace`

A fixed number of variables → `O(1)` extra space.

### Compared with Arrays.sort()

Copy + sort:

Time:
`O((m+n) log(m+n))`

Three-pointer merge:

Time:
`O(m+n)`

So three-pointer merge is more efficient because both input arrays are already sorted.

### Remember

Two sorted arrays + free space at end
→ compare largest values
→ fill from right to left
→ `O(m+n)` time, `O(1)` space.

# 977. Squares of a Sorted Array

## Pattern
Two Pointers / Opposite Ends

## Idea
- The array is already sorted.
- Squaring can disturb the order because negative numbers may become large positive values.
- The **largest absolute value must be at one of the two ends** of a sorted array.
- Therefore, the largest square must also come from either the left end or the right end.
- Compare the squares of both ends.
- Put the larger square into the result array from right to left.
- Move only the pointer whose value was used.

## Why Opposite-End Two Pointers?
Because the array is sorted, the **largest absolute value must be at one of the two ends**.

Example:

```text
[-7, -3, 2, 3, 11]

Left end  → |-7| = 7
Right end → |11| = 11

---

# 53. Maximum Subarray

## Pattern

**Kadane's Algorithm / Running Sum**

## How to Identify This Pattern

Look for:

* Need to find the maximum sum
* Elements must be **contiguous**
* Need an `O(n)` solution
* Need to decide whether the previous running sum is useful for future elements

### Pattern Hint

```text
Maximum sum
+ contiguous subarray
→ Running sum
→ If running sum becomes negative, discard it
→ Kadane's Algorithm
```

## Core Idea

Maintain:

```text
currentSum → sum of the current subarray

maxSum → maximum sum found so far
```

For every number:

```text
add current number to currentSum

update maxSum

if currentSum becomes negative
    reset currentSum to 0
```

Why?

Because a negative running sum will only reduce the sum of any future subarray.

Example:

```text
currentSum = -5

next number = 10
```

Keeping the previous sum:

```text
-5 + 10 = 5
```

Starting fresh:

```text
10
```

So carrying `-5` is not useful.

## Pseudocode

```text
maxSum = Integer.MIN_VALUE
currentSum = 0

for each num in nums

    currentSum = currentSum + num

    maxSum = max(maxSum, currentSum)

    if currentSum < 0
        currentSum = 0

return maxSum
```

## Why Update `maxSum` Before Resetting?

Consider:

```text
[-5, -2, -8]
```

All numbers are negative.

If we reset before updating `maxSum`, we may end up considering:

```text
0
```

But `0` is not part of the array.

Correct answer:

```text
-2
```

Therefore the order should be:

```text
1. Add current number
2. Update maxSum
3. Reset currentSum if negative
```

## Why Initialize `maxSum` With `Integer.MIN_VALUE`?

Because the array can contain only negative numbers.

Example:

```text
[-5, -2, -8]
```

If:

```text
maxSum = 0
```

the answer would incorrectly remain:

```text
0
```

Instead:

```text
maxSum = Integer.MIN_VALUE
```

allows negative values to become the answer.

## Example

```text
nums = [-2,1,-3,4,-1,2,1,-5,4]
```

```text
-2
currentSum = -2
maxSum = -2
reset currentSum = 0
```

```text
1
currentSum = 1
maxSum = 1
```

```text
-3
currentSum = -2
maxSum = 1
reset currentSum = 0
```

```text
4
currentSum = 4
maxSum = 4
```

```text
-1
currentSum = 3
maxSum = 4
```

```text
2
currentSum = 5
maxSum = 5
```

```text
1
currentSum = 6
maxSum = 6
```

Best subarray:

```text
[4, -1, 2, 1]
```

Answer:

```text
6
```

## Complexity

```text
Time:  O(n)
Space: O(1)
```

Only one traversal is required.

Only fixed variables are used:

```text
currentSum
maxSum
```

---

## Main Difference

```text
Maximum Element

Need largest individual value
→ Track max
```

```text
Maximum Subarray

Need largest sum of contiguous elements
→ Track running sum
→ Kadane's Algorithm
```

### Final Pattern Rule

```text
Contiguous subarray
+ maximum sum
→ Think Running Sum / Kadane's Algorithm
```

```text
If current running sum becomes negative
→ discard it
→ because it cannot help a future subarray
```
