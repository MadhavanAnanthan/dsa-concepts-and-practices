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