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
- when loop ends, `start` is the insertion position. Because if nothing matches start and end will be on same position, and start will be the first index where target can be inserted even for right side.

- If target is not found,SIMPLE - return left, because left ends at the insertion position.
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

# 4. Search in Rotated Sorted Array

### Idea

In every iteration, identify which half is sorted because the full array is rotated.

At least one half will always be sorted. Sometimes both halves can be sorted, but both cannot be unsorted.

Use the sorted half to check whether the target lies within its range:

- If target lies inside the sorted half → continue searching there.
- Otherwise → discard that half and search the other half.

Each iteration removes roughly half:

`n → n/2 → n/4 → n/8...`

### Pattern

`Binary Search + Identify Sorted Half`

### Pseudocode

```text
left = 0
right = n - 1

while left <= right:

    mid = left + (right - left) / 2

    if nums[mid] == target:
        return mid

    if left half is sorted:

        if target lies between nums[left] and nums[mid]:
            search left half
        else:
            search right half

    else:

        if target lies between nums[mid] and nums[right]:
            search right half
        else:
            search left half

return -1
```

### Java Code

```java
class Solution {
    public int search(int[] nums, int target) {

        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {

            int middle = left + (right - left) / 2;

            if (nums[middle] == target) {
                return middle;
            }

            // Left half is sorted
            if (nums[left] <= nums[middle]) {

                if (nums[left] <= target && target < nums[middle]) {
                    right = middle - 1;
                } else {
                    left = middle + 1;
                }

            } else {
                // Right half is sorted

                if (nums[middle] < target && target <= nums[right]) {
                    left = middle + 1;
                } else {
                    right = middle - 1;
                }
            }
        }

        return -1;
    }
}
```

### Remember

```text
Find sorted half
→ Check whether target belongs to it
→ Keep correct half
→ Discard other half
→ Repeat
```

### Complexity

```text
Time  : O(log n)
Space : O(1)
```

---

# 5. Minimum Element in Rotated Sorted Array

### Idea

In every iteration, identify whether the left half is sorted.

If the left half is sorted:

- `nums[left]` is the minimum value of that sorted half.
- Record it.
- Continue searching the other half because it may contain a smaller value.

If the left half is not sorted:

- The rotation/minimum lies within `left...mid`.
- `nums[mid]` itself may be the minimum.
- Record `nums[mid]`.
- Continue searching the left side.

### Pattern

`Binary Search + Track Minimum`

### Pseudocode

```text
left = 0
right = n - 1
result = infinity

while left <= right:

    mid = left + (right - left) / 2

    if left half is sorted:

        result = minimum(result, nums[left])

        discard left half
        search right

    else:

        result = minimum(result, nums[mid])

        keep searching left side

return result
```

### Java Code

```java
class Solution {
    public int findMin(int[] nums) {

        int left = 0;
        int right = nums.length - 1;

        int result = Integer.MAX_VALUE;

        while (left <= right) {

            int middle = left + (right - left) / 2;

            if (nums[left] <= nums[middle]) {

                // Left half is sorted
                result = Math.min(result, nums[left]);

                left = middle + 1;

            } else {

                // Left half contains the rotation/minimum
                result = Math.min(result, nums[middle]);

                right = middle - 1;
            }
        }

        return result;
    }
}
```

### Remember

```text
Sorted half → first element is its minimum.

Unsorted left half
→ rotation/minimum lies within left...mid
→ mid may itself be the minimum.
```

### Complexity

```text
Time  : O(log n)
Space : O(1)
```

---

# Main Difference

```text
Search in Rotated Array
→ Sorted half helps decide WHERE target can be.

Find Minimum
→ Sorted half tells us its minimum immediately.
```

# Square Root of X

## Problem

Return the integer part of `√x`.

Example:

```text
x = 8

√8 ≈ 2.82
Output = 2
```

We need the largest integer `n` such that:

```text
n * n <= x
```

---

## 1. Brute Force — Linear Search

### Idea

Check numbers one by one:

```text
1², 2², 3², 4² ...
```

Keep updating the result while:

```text
i * i <= x
```

Once:

```text
i * i > x
```

stop.

### Code

```java
class Solution {
    public int mySqrt(int x) {
        int result = 0;

        for (int i = 1; i <= x; i++) {
            if ((long) i * i <= x) {
                result = i;
            } else {
                break;
            }
        }

        return result;
    }
}
```

### Complexity

```text
Time  : O(x)
Space : O(1)
```

---

## 2. Why Binary Search?

In brute force, we are checking a known range:

```text
0 ... x
```

For any number `middle`:

```text
middle * middle <= x
```

means `middle` is valid, but there may be a bigger valid answer.

So search right:

```text
left = middle + 1
```

If:

```text
middle * middle > x
```

then `middle` and all bigger numbers are invalid.

So search left:

```text
right = middle - 1
```

This means we can discard half of the search range every time.

---

## Binary Search — My Code

```java
class Solution {
    public int mySqrt(int x) {
        int result = 0;
        int left=0;
        int right=x;

        while(left<=right){
            int middle = left + (right - left)/2;

            if((long) middle * middle <= x){
                result=middle;
                left=middle+1;
            }else {
                right=middle-1;
            }
        }

        return result;
    }
}
```

### Why `result = middle`?

When:

```text
middle * middle <= x
```

`middle` is currently a valid answer.

But we still search the right half to check whether a larger valid number exists.

So `result` keeps the latest valid value.

---

## Complexity

```text
Time  : O(log x)
Space : O(1)
```

---

## Takeaway

Brute force gives the idea first:

```text
Check every possible value from 0 to x.
```

Then notice that the values have a clear boundary:

```text
middle² <= x   → valid
middle² > x    → invalid
```

So instead of checking every number, binary search checks the middle and removes one complete half.

### Binary Search Rule

```text
If we know the answer range
and
checking the middle tells us which half can be removed,
binary search can be used.
```

For this problem:

```text
Search range : 0 to x
Condition    : middle * middle <= x
Goal         : largest valid middle
```
# 374. Guess Number Higher or Lower

## Main Confusion

We receive only:

```java
int n
```

The actual picked number is hidden by LeetCode.

```text
n    → visible input
pick → hidden number
```

We compare our guess with the hidden number using:

```java
guess(num)
```

```text
0  → correct number
-1 → guess is too high → go left
1  → guess is too low  → go right
```

## Brute Force

Try every number from `1` to `n`.

```text
guess(1)
guess(2)
guess(3)
...
```

When:

```text
guess(i) == 0
```

return `i`.

Time: `O(n)`

## Binary Search

Search range:

```text
1 ... n
```

Check the middle:

```text
guess(mid) == 0
→ return mid

guess(mid) == -1
→ right = mid - 1

guess(mid) == 1
→ left = mid + 1
```

### My Code

```java
public class Solution extends GuessGame {
    public int guessNumber(int n) {
        int left = 1;
        int right = n;

        while (left <= right) {
            int middle = left + (right - left) / 2;

            if (guess(middle) == 0) {
                return middle;
            } else if (guess(middle) == -1) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }

        return 0;
    }
}
```

Time: `O(log n)`

## Takeaway

Even though there is no array, binary search works because:

```text
We know the range: 1 to n
and
guess(mid) tells us which half to remove.
```
# 278. First Bad Version

## Pattern

Binary Search on a boundary.

Versions look like:

```text
good good good | bad bad bad bad
                ^
            first bad
```

We need to find the **first `true`** from `isBadVersion()`.

---

## My First Approach

```java
public class Solution extends VersionControl {
    public int firstBadVersion(int n) {
        int left = 1;
        int right = n;
        int result = 0;

        while (left <= right) {
            int middle = left + (right - left) / 2;

            if (isBadVersion(middle)) {
                result = middle;
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }

        return result;
    }
}
```

This is valid.

```text
Bad version found
→ save middle
→ search left side for an earlier bad version
```

Time: `O(log n)`  
Space: `O(1)`

---

## Boundary Style

```java
public class Solution extends VersionControl {
    public int firstBadVersion(int n) {
        int left = 1;
        int right = n;

        while (left < right) {
            int middle = left + (right - left) / 2;

            if (isBadVersion(middle)) {
                right = middle;
            } else {
                left = middle + 1;
            }
        }

        return left;
    }
}
```

### Why `right = middle`?

If `middle` is bad:

```text
middle itself may be the first bad version
```

So don't remove it.

Search:

```text
left ... middle
```

If `middle` is good:

```text
middle cannot be the answer
```

So remove it:

```java
left = middle + 1;
```

Eventually:

```text
left == right
```

and that position is the first bad version.

---

## Important Loop Rule

### Style 1

```text
while (left <= right)

left  = middle + 1
right = middle - 1
```

`middle` is removed every iteration.

If `middle` may be the answer, store it separately.

### Style 2

```text
while (left < right)

left  = middle + 1
right = middle
```

`middle` is kept when it may still be the answer.

Stop when:

```text
left == right
```

---

## Important Takeaway

Do not mix:

```java
while (left <= right)
```

with:

```java
right = middle;
```

because when:

```text

left = right = middle
```

nothing changes, which can cause an infinite loop / Time Limit Exceeded.

For this problem, the second style is cleaner because we are searching for a **boundary: first bad version**.

# 852. Peak Index in a Mountain Array

## Pattern

Binary Search on a mountain / peak.

A valid mountain array always has:

```text
Increasing → Peak → Decreasing

1 3 5 8 6 4 2
      ^
     peak
```

The problem guarantees **one peak**, so both an increasing side and a decreasing side will exist.

---

## Main Idea

Initially I thought we need to compare both neighbours:

```text
arr[mid - 1], arr[mid], arr[mid + 1]
```

But that is not required.

Simply compare:

```java
arr[middle] < arr[middle + 1]
```

### If true

```text
arr[mid] < arr[mid + 1]

We are on the increasing side.
Peak must be on the right.
```

So:

```java
left = middle + 1;
```

### Otherwise

```text
arr[mid] > arr[mid + 1]

We are on the decreasing side,
or middle itself is the peak.
```

So search toward the left.

---

## My Approach

```java
class Solution {
    public int peakIndexInMountainArray(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        int peakIndex = 0;

        while (left <= right) {
            int middle = left + (right - left) / 2;

            if (arr[middle] < arr[middle + 1]) {
                left = middle + 1;
            } else {
                peakIndex = middle;
                right = middle - 1;
            }
        }

        return peakIndex;
    }
}
```

**Because it is a valid mountain array, the peak cannot be the first or last element.**

A cleaner boundary-search version is:

```java
while (left < right) {
    int middle = left + (right - left) / 2;

    if (arr[middle] < arr[middle + 1]) {
        left = middle + 1;
    } else {
        right = middle;
    }
}

return left;
```

This also avoids needing a separate `peakIndex`.

---

## Takeaway

```text
arr[mid] < arr[mid + 1]
→ increasing slope
→ go right

arr[mid] > arr[mid + 1]
→ decreasing slope / possible peak
→ go left
```

We don't need to explicitly check both neighbours.

The key is:

> Comparing `mid` with just `mid + 1` tells us which side of the mountain we are on.

Time: `O(log n)`  
Space: `O(1)`