---

# Binary Search — Core Theory Notes

## Pattern

**Binary Search / Divide Search Space by Half**

## How to Identify

Use Binary Search when:

* The data/search space is sorted or monotonic
* After checking the middle, one complete half can be discarded
* You want better than linear search

### Pattern Hint

```text
Sorted / ordered search space
+ one half can be safely discarded
→ Binary Search
```

---

## Core Idea

At every step:

```text
check middle element

if target == nums[mid]
    found

if target > nums[mid]
    discard left half

if target < nums[mid]
    discard right half
```

So the search space keeps becoming smaller:

```text
n → n/2 → n/4 → n/8 → ...
```

---

## Main Variables

```text
start
end
mid
```

Initialize:

```text
start = 0
end = nums.length - 1
```

For normal Binary Search:

```text
while start <= end
```

`start == end` still means one element remains to be checked.

---

# Mid Calculation

Use:

```text
mid = start + (end - start) / 2
```

This formula is useful for **two reasons**.

## 1. `(end - start) / 2` Is Only an Offset

Suppose:

```text
start = 4
end = 8
```

If you calculate only:

```text
(end - start) / 2
```

you get:

```text
(8 - 4) / 2
= 2
```

But `2` is **not the actual midpoint index**.

It means:

```text
midpoint is 2 positions away from start
```

So we must add `start`:

```text
start + (end - start) / 2

= 4 + 2
= 6
```

Correct midpoint index:

```text
6
```

### Short Rule

```text
(end - start) / 2
→ distance / offset from start

start + (end - start) / 2
→ actual midpoint index
```

---

## 2. It Prevents Integer Overflow

Another possible formula is:

```text
mid = (start + end) / 2
```

Mathematically this is correct.

But in Java, `start + end` can exceed the maximum `int` value.

Java `int` maximum:

```text
2,147,483,647
```

Example:

```text
start = 2,000,000,000
end   = 2,100,000,000
```

If we do:

```text
start + end
```

we get:

```text
4,100,000,000
```

This cannot fit inside an `int`.

So overflow occurs and the result becomes incorrect.

Instead:

```text
start + (end - start) / 2
```

Calculation:

```text
end - start
= 100,000,000

100,000,000 / 2
= 50,000,000

start + 50,000,000
= 2,050,000,000
```

This stays within the `int` range.

### Final Mid Rule

```text
mid = start + (end - start) / 2
```

Why?

```text
1. (end - start) / 2 gives the midpoint offset
2. adding start converts that offset into the actual index
3. it also avoids possible overflow from start + end
```

---

## Moving the Search Range

If:

```text
nums[mid] < target
```

then `mid` and everything before it can be discarded:

```text
start = mid + 1
```

If:

```text
nums[mid] > target
```

then `mid` and everything after it can be discarded:

```text
end = mid - 1
```

We use `+1` and `-1` because `mid` has already been checked.

---

## Pseudocode

```text
start = 0
end = nums.length - 1

while start <= end

    mid = start + (end - start) / 2

    if nums[mid] == target
        return mid

    else if nums[mid] < target
        start = mid + 1

    else
        end = mid - 1

return -1
```

---

## Complexity

```text
Time:  O(log n)
Space: O(1)
```

---

## Final Mental Model

```text
Binary Search is not mainly about remembering code.

Ask:

"After checking mid,
which half can I safely discard?"
```

And remember:

```text
(end - start) / 2
→ midpoint distance from start

start + (end - start) / 2
→ actual midpoint index
→ also overflow-safe
```

---
