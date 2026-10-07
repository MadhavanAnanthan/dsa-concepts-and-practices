## Pair Sum / Two Sum

Pattern: Hashing / Complement
Refer - sumElementToMeetTarget.md to know more about 2, 3, 4 sum problems.
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

## Single Number - XOR

Problem: Find the element that appears only once when every other element appears twice.

Pattern: XOR / Bit Manipulation

Rules:

- `a ^ a = 0`
- `a ^ 0 = a`
- Duplicate values cancel each other.
- The remaining value is the single number.

Pseudocode:

```text
xor = 0

for each number in array
    xor = xor ^ number

return xor
```

Time: `O(n)`  
Space: `O(1)`

Remember: **Pairs cancel using XOR; unique value remains.**


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
```


## 387. First Unique Character in a String

Problem: Find the index of the first non-repeating character.

Pattern: Frequency Counting + Second Traversal

Rules:

- Count frequency of each character first.
- Since input is only `a-z`, use `int[26]`.
- Character index → `c - 'a'`
- Traverse the original string again from left to right.
- If frequency of current character is `1` → return its index.
- If no unique character exists → return `-1`.

Pseudocode:

```text
create frequency array of size 26

for each character c:
    frequency[c - 'a']++

for i from 0 to n - 1:
    if frequency[s[i] - 'a'] == 1:
        return i

return -1