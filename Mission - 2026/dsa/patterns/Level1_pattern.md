# 392. Is Subsequence

## Pattern

**Same-Direction Two Pointers**

## Idea

* One pointer tracks characters in `s`.
* One pointer scans characters in `t`.
* If both characters match, move the `s` pointer.
* Always move the `t` pointer.
* If the `s` pointer reaches the end, `s` is a subsequence.

## Better Pseudocode

```text
sPointer = 0
tPointer = 0

while sPointer < s.length
      AND tPointer < t.length

    if s[sPointer] == t[tPointer]
        sPointer++

    tPointer++

return sPointer == s.length
```

## Earlier Approach

```text
matched = 0
tPointer = 0

for each character in s

    for remaining characters in t

        if current s character == current t character
            matched++
            tPointer++
            break

return matched == s.length
```

**Better approach:** use the first pseudocode because it is simpler and directly follows the same-direction two-pointer pattern.

## Remember

> `sPointer` = character I am waiting for
> `tPointer` = character I am currently checking

**Time:** O(t.length)
**Space:** O(1)

## Valid Palindrome

Pattern: Opposite-End Two Pointers

### Idea

* Start one pointer from the left and another from the right.
* Skip characters that are not letters or digits.
* Compare the valid characters from both ends.
* Convert characters using `Character.toLowerCase()` before comparing.
* If characters are different → return `false`.
* Otherwise move both pointers inward.

### Better Pseudocode

```text
left = 0
right = s.length - 1

while left < right

    while left < right AND s[left] is not letter/digit
        left++

    while left < right AND s[right] is not letter/digit
        right--

    if lowercase(s[left]) != lowercase(s[right])
        return false

    left++
    right--

return true
```

### Cleaner Java Comparison

Instead of manually checking ASCII values:

```text
'A' to 'Z'
then add 32
```

use:

```java
Character.toLowerCase(s.charAt(left))
Character.toLowerCase(s.charAt(right))
```

Also this check:

```java
!Character.isLetterOrDigit(s.charAt(left)) || s.charAt(left) == ' '
```

can simply be:

```java
!Character.isLetterOrDigit(s.charAt(left))
```

because a space is already not a letter or digit.

### Complexity

Time: `O(n)`
Space: `O(1)`

### Remember

Palindrome means comparing characters from both ends.

### Pattern Hint

Need to compare beginning and end repeatedly
→ Opposite-end two pointers
→ Skip invalid characters
→ Compare lowercase valid characters
→ Move both pointers inward

## Longest Common Prefix

Pattern: `String Traversal`

Technique: `Horizontal Scanning / Vertical Scanning for prefix comparison`

### Initial Approach

* Compare the current prefix with the next string.
* Build the matched characters using `StringBuilder`.
* This works, but `StringBuilder` uses extra space to store the prefix again.

Time: `O(S)`
Space: up to `O(L)` for the builder

`S` = total characters checked
`L` = current prefix length

---

## Horizontal Scanning

Compare **string by string**.

* Start with the first string as the prefix.
* Compare it with the next string.
* Find how many starting characters match.
* Shorten the prefix using `substring(0, index)`.
* Continue with the next string.
* If prefix becomes empty → return `""`.

### Pseudocode

```text
prefix = first string

for each remaining string

    index = 0

    while index is valid
          AND prefix[index] == currentString[index]

        index++

    prefix = prefix.substring(0, index)

    if prefix is empty
        return ""

return prefix
```

Time: `O(S)`
Extra space: `O(1)` logic, apart from new substring objects

### Remember

Horizontal scanning
→ compare **string by string**
→ keep reducing the common prefix

---

## Vertical Scanning

Compare the **same character position across all strings**.

Example:

```text
flower
flow
flight

index 0 → f f f ✓
index 1 → l l l ✓
index 2 → o o i ✗
```

* Take each character from the first string.
* Check the same index in every other string.
* If any string ends or the character is different → return prefix up to that index.

### Pseudocode

```text
for each index i in first string

    currentChar = firstString[i]

    for each remaining string

        if i is outside current string
           OR currentString[i] != currentChar

            return firstString.substring(0, i)

return first string
```

Time: `O(S)`
Extra space: `O(1)` logic

### Remember

Horizontal scanning
→ compare **string by string**

Vertical scanning
→ compare **same index across all strings**

# 724. Find Pivot Index

## Pattern

**Prefix Sum / Running Sum**

---

## How to Identify This Pattern

Look for:

* Need sum of elements on the left side.
* Need sum of elements on the right side.
* Need an index where both sums are equal.

### Pattern Hint

```text
Left sum + current + right sum = total sum

Need leftSum == rightSum
→ Calculate total sum once
→ Maintain running left sum
→ Derive right sum
```

---

## Core Idea

Calculate total array sum first.

For each index:

```text
rightSum = totalSum - leftSum - nums[i]
```

Then check:

```text
if leftSum == rightSum
    return current index
```

After checking, update:

```text
leftSum += nums[i]
```

---

## Pseudocode

```text
totalSum = sum of all numbers

leftSum = 0

for each index i

    rightSum = totalSum - leftSum - nums[i]

    if leftSum == rightSum
        return i

    leftSum = leftSum + nums[i]

return -1
```

---

## Important Point

Update `leftSum` only **after checking the current index**.

Because the current value should not be part of either side.

```text
left side | current | right side
```

---

## Complexity

```text
Time:  O(n)
Space: O(1)
```

---

## Remember

```text
rightSum = totalSum - leftSum - currentValue
```

### Pattern Hint

```text
Need left-side sum and right-side sum
→ Prefix Sum / Running Sum
→ Total sum once
→ Maintain left sum
→ Calculate right sum from total
```


# 643. Maximum Average Subarray I

## Pattern

**Fixed-Size Sliding Window**

---

## How to Identify This Pattern

Look for these clues:

* Problem asks about a **contiguous subarray / substring**
* Window size is explicitly fixed as `k`
* Need maximum / minimum / sum / average of every group of `k` elements

### Pattern Hint

```text
Contiguous elements
+ fixed size k
+ need max/min/sum/average
→ Fixed-Size Sliding Window
```

---

## Core Idea

First calculate the sum of the first `k` elements.

```text
[1, 12, -5, -6, 50, 3]
 ---------
 first k=4
```

Then move the window one position.

Instead of recalculating all `k` values:

```text
new window sum
= old window sum
- left-most outgoing value
+ new incoming value
```

Example:

```text
First window:
1 + 12 - 5 - 6 = 2

Move window:

remove 1
add 50

2 - 1 + 50 = 51
```

---

## Pseudocode

```text
windowSum = sum of first k elements

maxSum = windowSum

for i from k to end of array

    windowSum = windowSum
                - nums[i - k]
                + nums[i]

    maxSum = max(maxSum, windowSum)

return maxSum / k
```

---

## Important State

Keep two separate values:

```text
windowSum
→ sum of current window

maxSum
→ maximum window sum seen so far
```

Do not replace `windowSum` with `maxSum`.

The next sliding-window calculation must always use the actual current window.

---

## Clean Solution

```java
class Solution {
    public double findMaxAverage(int[] nums, int k) {

        int windowSum = nums[0];

        // Build the first complete window
        for (int i = 1; i < k; i++) {
            windowSum += nums[i];
        }

        int maxSum = windowSum;

        // Slide the window
        for (int i = k; i < nums.length; i++) {

            windowSum = windowSum - nums[i - k] + nums[i];

            maxSum = Math.max(maxSum, windowSum);
        }

        return (double) maxSum / k;
    }
}
```

---

## Why Divide Only at the End?

Every window contains exactly `k` elements.

So:

```text
largest sum
→ also gives largest average
```

Find the maximum sum first, then divide once:

```text
(double) maxSum / k
```

---

## Complexity

```text
Time:  O(n)
Space: O(1)
```

---

## Remember

```text
Fixed window size k
→ build first window
→ remove outgoing left value
→ add incoming right value
→ update best answer
```

The key sliding-window formula:

```text
windowSum = windowSum - nums[i - k] + nums[i]
```

# 217. Contains Duplicate

## Pattern

**HashSet / Seen Before**

## How to Identify This Pattern

Look for:

* Need to know whether a value appeared before
* No index/distance condition
* Only duplicate existence matters

### Pattern Hint

```text
Duplicate check
→ Need only value presence
→ HashSet
```

## Core Idea

Store every seen value in a `HashSet`.

If the current value is already present:

```text
return true
```

Otherwise add it and continue.

## Pseudocode

```text
create HashSet

for each number

    if number exists in set
        return true

    add number to set

return false
```

## Complexity

```text
Time:  O(n) average
Space: O(n)
```

## Remember

```text
Need only:
"Have I seen this value before?"

→ HashSet
```

---

# 219. Contains Duplicate II

## Pattern

**HashMap / Last Seen Index**

## How to Identify This Pattern

Look for:

* Need to know whether the same value appeared before
* Index distance also matters
* Condition like:

```text
i - previousIndex <= k
```

### Pattern Hint

```text
Duplicate
+ position/index condition
→ Need value + last position
→ HashMap<value, index>
```

## Core Idea

Store:

```text
number → last index where it appeared
```

When the same number appears again:

```text
distance = currentIndex - previousIndex
```

If:

```text
distance <= k
```

return `true`.

Then always update the value with its latest index.

## Pseudocode

```text
create HashMap lastIndexSeen

for each index i

    current = nums[i]

    if current exists in map

        previousIndex = map[current]

        if i - previousIndex <= k
            return true

    update map:
    current → i

return false
```

## Why Store the Latest Index?

The latest occurrence gives the smallest distance to future duplicates.

Example:

```text
value appears at:

1, 5, 9
```

At index `9`, comparing with `5` is more useful than comparing with `1`.

## Complexity

```text
Time:  O(n) average
Space: O(n)
```

---

## Main Difference

```text
Contains Duplicate

Need only value
→ HashSet
```

```text
Contains Duplicate II

Need value + where it last appeared
→ HashMap<value, lastSeenIndex>
```

### Final Pattern Rule

```text
Need only presence
→ Set

Need presence + extra information
such as index/count/frequency
→ Map
```

# 20. Valid Parentheses

## Pattern

**Stack / Matching Pairs / Nested Structure**

---

## How to Identify This Pattern

Look for clues like:

* Opening and closing symbols
* Need to match the **most recent unmatched opening symbol**
* Nested structures
* Last opened should be closed first

### Pattern Hint

```text
Most recent unmatched opening
→ Stack
→ LIFO
```

---

## Stack Idea

For opening brackets:

```text
(  [  {
```

push them into the stack.

For a closing bracket:

```text
)  ]  }
```

compare it with the top of the stack.

If it matches:

```text
pop()
```

because that opening bracket is now completed.

If it does not match:

```text
return false
```

At the end:

```text
stack must be empty
```

---

## My Initial Solution

I used ASCII values:

```text
( = 40
) = 41

[ = 91
] = 93

{ = 123
} = 125
```

Example comparison:

```java
s.charAt(index) == 41 && stack.peek() == 40
```

This works, but it is harder to read because I need to remember the ASCII numbers.

I also used:

```java
Stack<Character>
```

and:

```java
stack.size() > 0
```

---

## Better Solution

Use the characters directly:

```java
ch == '('
ch == ')'
ch == '['
ch == ']'
ch == '{'
ch == '}'
```

This is much easier to understand than:

```text
40, 41, 91, 93, 123, 125
```

Also use:

```java
Deque<Character> stack = new ArrayDeque<>();
```

instead of the older:

```java
Stack<Character>
```

---

## Better Pseudocode

```text
create stack

for each character ch

    if ch is an opening bracket
        push ch

    else

        if stack is empty
            return false

        top = stack.peek()

        if closing bracket matches top
            stack.pop()

        else
            return false

return stack is empty
```

---

## Why This Version Is Better

### Old

```java
ch == 41 && stack.peek() == 40
```

Need to remember:

```text
41 = )
40 = (
```

### Better

```java
ch == ')' && stack.peek() == '('
```

The comparison explains itself.

So prefer:

```text
characters directly
```

instead of:

```text
ASCII numbers
```

---

## Java Stack Operations

```text
push(x)
→ add value to top

peek()
→ read top value without removing

pop()
→ remove top value

isEmpty()
→ check whether stack has values
```

Before using `peek()` or `pop()`:

```text
always check stack is not empty
```

---

## Complexity

```text
Time:  O(n)
Space: O(n)
```

---

## Remember

```text
Opening bracket
→ push

Closing bracket
→ compare with most recent opening

Match
→ pop

Mismatch
→ false

End
→ stack must be empty
```

### Best Java Style

```text
ArrayDeque instead of Stack (say in 2 lines why arradeque better than stack in this case)
characters instead of ASCII numbers
isEmpty() instead of size() > 0
```

