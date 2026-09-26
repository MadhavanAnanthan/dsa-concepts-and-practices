# Two Sum / 3Sum - Hashing and Two Pointer Patterns

## Pair Sum / Two Sum

Pattern: Hashing / Complement

### Brute Force

* Check every pair using 2 loops.
* For every element, compare it with all remaining elements.

Time: `O(n²)`  
Space: `O(1)`

### Optimal - Hashing

For each current value:

```text
needed = target - current
```

Check whether `needed` was already seen.

**If only pair values are needed**

* Use `HashSet`.

**If original indices are needed**

* Use `HashMap<number, index>`.
* Store:

```text
value → index
```

* If `needed` already exists:
    * return stored index + current index.

Time: `O(n)` average  
Space: `O(n)`

### Why Hashing Is Useful in Two Sum

The original array may be unsorted.

Example:

```text
[3, 2, 4]
target = 6
```

We could sort it:

```text
[2, 3, 4]
```

and use two pointers.

But sorting changes the original positions.

So when the question asks for the **original indices**, sorting creates an additional problem.

HashMap allows us to:

* Keep the original array unchanged.
* Remember previously visited values.
* Remember their original indices.
* Find the complement in `O(1)` average time.

### Remember

Target is known:

```text
current + needed = target
```

Therefore:

```text
needed = target - current
```

Use hashing for fast lookup.

### Pattern Hint

```text
Unsorted array
+ need original indices
+ pair sum = target

→ HashMap
→ calculate complement
→ needed = target - current
```

---

## Can Two Sum Be Solved Using Two Pointers?

Yes.

If:

* The array is already sorted, or
* Sorting is allowed and original indices are not important,

then Two Sum can be solved using **opposite-end two pointers**.

Example:

```text
[1, 2, 4, 6, 10]
 L           R
```

Suppose:

```text
target = 8
```

Calculate:

```text
sum = nums[left] + nums[right]
```

Because the array is sorted, pointer movement has a predictable effect.

```text
left++
→ moves toward a larger value
→ sum increases

right--
→ moves toward a smaller value
→ sum decreases
```

This is the main reason sorting + two pointers works.

---

## Why Sorting Helps Two Pointers

Sorting gives us **direction**.

Consider an unsorted array:

```text
[6, 1, 10, 2, 4]
```

If the current sum is too large, we cannot confidently say:

```text
move left
```

or:

```text
move right
```

because the next values have no predictable order.

After sorting:

```text
[1, 2, 4, 6, 10]
```

we know:

```text
Need bigger sum
→ move left forward

Need smaller sum
→ move right backward
```

### Important Observation

Sorting removes the need to maintain extra lookup state in some problems.

Instead of remembering values using a HashMap, the ordered data itself tells us **which direction to move**.

---

## Why Opposite-End Two Pointers?

For sorted Two Sum:

```text
[1, 2, 4, 6, 10]
 L           R
```

The two pointers have different responsibilities.

```text
left
→ controls increasing the sum

right
→ controls decreasing the sum
```

Therefore:

```text
sum < target
→ need bigger sum
→ left++

sum > target
→ need smaller sum
→ right--
```

This gives us a clear directional decision.

---

## Why Same-Direction Two Pointers Don't Fit Classic Two Sum

Consider:

```text
[1, 2, 4, 6, 10]

 L
    R
```

Suppose:

```text
target = 8

1 + 2 = 3
```

The sum is too small.

But now:

```text
Should left move?
Should right move?
```

There is no clean rule.

Both pointers moving in the same direction do not give us independent control to increase or decrease the sum.

With opposite ends:

```text
sum too small
→ left++

sum too large
→ right--
```

The decision is clear.

### Remember

Opposite-end two pointers work when:

> Moving one pointer predictably increases the result, while moving the other predictably decreases the result.

Same-direction / slow-fast pointers are useful for different purposes, such as:

* Remove Duplicates
* Move Zeroes
* In-place array modifications
* Maintaining read/write positions
* Some window-based problems

So don't think:

```text
Two pointers = always opposite ends
```

Think:

```text
What does moving each pointer achieve?
```

---

# Two Sum II - Input Array Is Sorted

Pattern: Opposite-End Two Pointers

### Idea

The input array is already sorted.

Start:

```text
left = 0
right = n - 1
```

Calculate:

```text
sum = numbers[left] + numbers[right]
```

### If `sum == target`

Pair is found.

### If `sum > target`

Current sum is too large.

We need a smaller value.

```text
right--
```

### If `sum < target`

Current sum is too small.

We need a larger value.

```text
left++
```

Time: `O(n)`  
Space: `O(1)`

### Why We Don't Need HashMap Here

The sorted order already gives enough information to decide the next step.

HashMap would work, but would require:

```text
O(n) space
```

Two pointers can solve it using:

```text
O(1) extra space
```

### Remember

Because the array is sorted:

```text
Need smaller sum
→ right--

Need larger sum
→ left++
```

### Pattern Hint

```text
Sorted array
+ need pair whose sum = target

→ Opposite-end two pointers
→ left = 0
→ right = n - 1
→ use sum to decide direction
```

---

# 3Sum

Pattern: Sorting + Fix One Element + Opposite-End Two Pointers

Problem:

Find unique triplets such that:

```text
a + b + c = 0
```

---

## Main Idea

Instead of trying to manage 3 moving values, **fix one value**.

Suppose:

```text
fixed = a
```

Then:

```text
a + b + c = 0
```

becomes:

```text
b + c = -a
```

Now the remaining problem is basically **Two Sum**.

### Pattern Reduction

```text
3Sum
↓
Fix one value
↓
Remaining two values must satisfy a target
↓
Solve using Two Sum pattern
```

This is the main 3Sum observation.

---

## Why Sort First?

After sorting:

```text
nums[fixed] + nums[left] + nums[right]
```

has predictable pointer movement.

```text
sum < 0
→ sum is too small
→ need bigger value
→ left++

sum > 0
→ sum is too large
→ need smaller value
→ right--

sum == 0
→ triplet found
```

Without sorting, we cannot confidently decide which pointer should move.

---

## 3Sum Pointer Setup

After sorting:

```text
fixed = current index
left = fixed + 1
right = nums.length - 1
```

Example:

```text
[-2, 0, 1, 1, 2]
 ^
 fixed

[-2 | 0, 1, 1, 2]
      L        R
```

---

## Why `left = fixed + 1`?

When one fixed value is completely processed, everything before the next fixed position has already been covered.

Example:

```text
[-2, 0, 1, 1, 2]
```

First:

```text
fixed = -2

[-2 | 0, 1, 1, 2]
      L        R
```

After processing all combinations starting with `-2`:

```text
fixed = 0

[-2, 0 | 1, 1, 2]
          L     R
```

We don't reset `left` back to index `0`.

Why?

Because combinations containing the earlier fixed value were already checked.

Otherwise we could rediscover:

```text
[-2, 0, 2]
```

again with a different ordering.

### Remember

Each fixed value searches only the elements **after itself**.

```text
left = fixed + 1
```

---

## Example

Input:

```text
[-2, 0, 1, 1, 2]
```

Already sorted.

### Fixed = `-2`

```text
[-2, 0, 1, 1, 2]
      L        R
```

Calculate:

```text
-2 + 0 + 2 = 0
```

Found:

```text
[-2, 0, 2]
```

Move both:

```text
left++
right--
```

Now:

```text
[-2, 0, 1, 1, 2]
         L  R
```

Calculate:

```text
-2 + 1 + 1 = 0
```

Found:

```text
[-2, 1, 1]
```

Important observation:

One fixed value can produce multiple valid pairs.

So after finding one answer, **do not stop**.

---

## Why Move Both After Finding a Triplet?

When:

```text
sum == 0
```

the current `left` and `right` values already formed a valid answer.

Move:

```text
left++
right--
```

and continue searching for another combination.

Example with fixed `-2`:

```text
0 + 2 = 2
1 + 1 = 2
```

Both produce valid triplets:

```text
[-2, 0, 2]
[-2, 1, 1]
```

---

# Duplicate Handling in 3Sum

3Sum asks for **unique triplets**.

Duplicates must be handled in two places.

---

## 1. Skip Duplicate Fixed Values

Example:

```text
[-1, -1, -1, 0, 1, 2]
```

After processing the first `-1` as fixed, using the next `-1` as fixed would perform almost the same search again.

So:

```java
if (fixed > 0 && nums[fixed] == nums[fixed - 1]) {
    continue;
}
```

### Remember

```text
Same fixed value again
→ same search space
→ skip it
```

---

## 2. Skip Duplicate Left / Right Values

After finding:

```text
sum == 0
```

move:

```text
left++
right--
```

Then skip repeated values.

Conceptually:

```text
while left value is same as previous left value
    left++

while right value is same as previous right value
    right--
```

This prevents adding the same triplet multiple times.

---

# Early Stop Optimization

After sorting, if:

```text
nums[fixed] > 0
```

we can stop.

Why?

Because everything after `fixed` is also positive.

Example:

```text
[1, 2, 3, 4]
 ^
 fixed
```

Then:

```text
positive + positive + positive
```

cannot equal:

```text
0
```

So:

```java
if (nums[fixed] > 0) {
    break;
}
```

---

# 3Sum Pseudocode

```text
sort nums

for fixed from 0 to n - 3:

    if nums[fixed] > 0:
        break

    if fixed is duplicate:
        continue

    left = fixed + 1
    right = n - 1

    while left < right:

        sum =
            nums[fixed]
            + nums[left]
            + nums[right]

        if sum == 0:

            add triplet

            left++
            right--

            skip duplicate left values
            skip duplicate right values

        else if sum < 0:

            left++

        else:

            right--
```

---

# 3Sum Complexity

Sorting:

```text
O(n log n)
```

For each fixed element, two pointers may scan the remaining array:

```text
O(n)
```

Outer fixed loop:

```text
O(n)
```

Overall:

```text
O(n²)
```

Extra space:

```text
O(1)
```

excluding output and depending on sorting implementation.

---

# Pattern Connection

## Two Sum

```text
Unsorted
+ original indices required

→ HashMap
```

Mental model:

```text
current + needed = target

needed = target - current
```

---

## Two Sum II

```text
Sorted array

→ Opposite-end two pointers
```

Mental model:

```text
sum too small
→ left++

sum too large
→ right--
```

---

## 3Sum

```text
Need 3 values

→ Sort
→ Fix one value
→ Remaining problem becomes Two Sum
→ Opposite-end two pointers
```

Mental model:

```text
3Sum
= 1 fixed value
+ Two Sum
```

---

# Most Important Pattern Observation

Do not select two pointers only because:

```text
"This looks like a two-pointer problem."
```

Ask:

```text
If I move this pointer,
do I know how it changes my condition?
```

For sorted Two Sum / 3Sum:

```text
left++
→ value becomes larger
→ sum increases

right--
→ value becomes smaller
→ sum decreases
```

That predictable direction is **why opposite-end two pointers work**.

---

# Short Revision Rules

```text
Two Sum
→ Unsorted + need original indices
→ HashMap
→ needed = target - current
```

```text
Two Sum II
→ Already sorted
→ opposite-end two pointers
→ smaller sum → left++
→ larger sum → right--
```

```text
3Sum
→ sort
→ fix one
→ remaining problem becomes Two Sum
→ left = fixed + 1
→ right = end
```

```text
3Sum duplicate handling
→ skip duplicate fixed
→ after finding answer move both
→ skip duplicate left/right
```

```text
Why sorting?
→ gives predictable direction
```

```text
Why opposite ends?
→ left can increase the sum
→ right can decrease the sum
```

```text
Why not same direction for classic Two Sum?
→ no clean decision about which pointer should move
→ opposite ends provide two directional controls
```

## Final Pattern Rule

> **Sorting is useful when ordered values allow pointer movement to give predictable information.**

> **Opposite-end two pointers work when one side can be moved to increase the result and the other side can be moved to decrease it.**

> **Hashing is useful when we need fast lookup/state, especially when sorting would destroy information such as original indices.**