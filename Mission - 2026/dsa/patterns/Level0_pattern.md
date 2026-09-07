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