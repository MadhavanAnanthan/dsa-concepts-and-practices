# 2679. Sum in a Matrix

## Problem Idea

You are given a 2D integer array.

For every round:

1. From each row, remove the largest remaining value.
2. Among all removed values in that round, take the largest one.
3. Add it to the total score.
4. Repeat until all values are removed.

---

## Example

```text
Input:
[
  [7,2,1],
  [6,4,2],
  [6,5,3],
  [3,2,1]
]

Round 1:
Removed -> 7, 6, 6, 3
Highest -> 7

Round 2:
Removed -> 2, 4, 5, 2
Highest -> 5

Round 3:
Removed -> 1, 2, 3, 1
Highest -> 3

Answer = 7 + 5 + 3 = 15
```

---

# Approach 1: Brute Force Simulation

## Idea

Directly simulate what the problem says.

For every cycle:

- Go through every row.
- Scan all columns in that row.
- Find the largest value that is still available.
- Mark that value as removed using `-1`.
- Compare all row maximums and keep the largest value for the current cycle.
- Add that value to `total`.

Since one value is removed from every row in each cycle:

```text
Number of cycles = number of columns
                 = nums[0].length
```

Do not use `nums.length` for the number of cycles because:

```text
nums.length     -> number of rows
nums[0].length  -> number of columns
```

---

## Pseudocode

```text
total = 0
counter = 0

while counter < number of columns:

    highestOfEachCycle = 0

    for every row:

        highestValueInCurrentRow = -1
        remember position of highest value

        for every column in current row:

            if current value > highestValueInCurrentRow:
                update highest value
                remember row and column

        highestOfEachCycle =
            max(highestOfEachCycle, highestValueInCurrentRow)

        mark selected element as removed using -1

    total += highestOfEachCycle
    counter++

return total
```

---

## Brute Force Code

```java
class Solution {
    public int matrixSum(int[][] nums) {

        int total = 0;
        int counter = 0;

        while (counter < nums[0].length) {

            int highestOfEachCycle = 0;

            for (int row = 0; row < nums.length; row++) {

                int highestValueInCurrentCycle = -1;
                int highestRowIndex = 0;
                int highestColumnIndex = 0;

                for (int column = 0; column < nums[row].length; column++) {

                    int currentValue = nums[row][column];

                    if (currentValue > highestValueInCurrentCycle) {
                        highestValueInCurrentCycle = currentValue;
                        highestRowIndex = row;
                        highestColumnIndex = column;
                    }
                }

                highestOfEachCycle =
                        Math.max(highestOfEachCycle, highestValueInCurrentCycle);

                nums[highestRowIndex][highestColumnIndex] = -1;
            }

            total += highestOfEachCycle;
            counter++;
        }

        return total;
    }
}
```

---

## Complexity - Brute Force

Let:

```text
R = number of rows
C = number of columns
```

For every one of the `C` cycles:

- traverse `R` rows
- scan `C` values in each row

Therefore:

```text
Time: O(R * C^2)
Space: O(1)
```

We modify the input matrix directly, so no additional data structure is required.

---

# Approach 2: Sort Every Row

## Main Observation

The brute-force solution repeatedly searches for the largest remaining value in every row.

Instead, ask:

```text
Can I arrange each row once so that I don't need to search for its maximum repeatedly?
```

Yes.

Sort every row.

Example:

```text
[7,2,1] -> [1,2,7]
[6,4,2] -> [2,4,6]
[6,5,3] -> [3,5,6]
[3,2,1] -> [1,2,3]
```

Now the largest values are already at the right side.

Removal order from every row becomes:

```text
right -> left
```

So we only need to compare values from the same column across all rows.

---

## Why Sorting Works

For one sorted row:

```text
[1,2,7]
```

The problem's removal order is:

```text
7 -> 2 -> 1
```

That is exactly the same as traversing:

```text
column 2 -> column 1 -> column 0
```

Therefore:

1. Sort every row.
2. Start from the last column.
3. For that column, find the maximum across all rows.
4. Add it to `total`.
5. Move to the previous column.

---

## Pseudocode

```text
for every row:
    sort the row

total = 0

for column from last column to first column:

    highestInEachCycle = 0

    for every row:
        highestInEachCycle =
            max(highestInEachCycle, nums[row][column])

    total += highestInEachCycle

return total
```

---

## Sorting Approach Code

```java
import java.util.Arrays;

class Solution {
    public int matrixSum(int[][] nums) {

        int total = 0;

        for (int row = 0; row < nums.length; row++) {
            Arrays.sort(nums[row]);
        }

        for (int column = nums[0].length - 1; column >= 0; column--) {

            int highestInEachCycle = 0;

            for (int row = 0; row < nums.length; row++) {
                highestInEachCycle =
                        Math.max(highestInEachCycle, nums[row][column]);
            }

            total += highestInEachCycle;
        }

        return total;
    }
}
```

---

## Complexity - Sorting Approach

Sorting every row:

```text
O(R * C log C)
```

Traversing the matrix afterward:

```text
O(R * C)
```

Overall:

```text
Time: O(R * C log C)
Space: depends on sorting implementation
```

---

# Important Mistakes / Learnings

## 1. Number of cycles

Wrong:

```java
while (counter < nums.length)
```

Because `nums.length` is the number of rows.

Correct:

```java
while (counter < nums[0].length)
```

Because every row loses one element per cycle, so the number of cycles equals the number of columns.

---

## 2. Sorting every row

Wrong:

```java
for (int row = 0; row < nums.length; row++) {
    Arrays.sort(nums[0]);
}
```

This keeps sorting only the first row.

Correct:

```java
for (int row = 0; row < nums.length; row++) {
    Arrays.sort(nums[row]);
}
```

`nums[row]` means the current row.

---

## 3. Reverse column traversal

Wrong:

```java
for (int column = nums[0].length - 1; column > 0; column--)
```

This skips column `0`.

Correct:

```java
for (int column = nums[0].length - 1; column >= 0; column--)
```

---

# Pattern Recognition

### Brute-force pattern

```text
Simulation
+
2D Array Traversal
+
Repeated Maximum Search
```

### Better pattern

```text
Repeated min/max search
        ↓
Ask whether sorting once can remove repeated work
```

For this problem:

```text
Repeatedly find max in each row
        ↓
Sort each row once
        ↓
Traverse corresponding columns
```

---

# Short Remember Rule

```text
Brute Force:
C rounds * R rows * C scan
= O(R * C^2)

Better:
Sort each row once
+ scan each column
= O(R * C log C)

Key insight:
Repeated maximum search -> consider sorting once.
```
# 56. Merge Intervals

## Problem Idea

Each row represents one interval:

```text
[start, end]
```

If intervals overlap, merge them.

Example:

```text
[1,3] and [2,6]
```

They overlap because:

```text
nextStart <= currentEnd
2 <= 3
```

Merged:

```text
[1,6]
```

## Pattern

```text
Sorting + Interval Traversal + Merge
```

## Main Idea

1. Sort rows by start value (`interval[0]`).
2. Keep the first interval as `currentRow`.
3. Compare `currentRow` with the next interval.
4. If `currentRowEnd >= nextRowStart`, they overlap.
5. Merge by updating:

```text
currentRowEnd = max(currentRowEnd, nextRowEnd)
```

6. If they do not overlap:
    - store `currentRow` in result
    - move `currentRow` to the next interval
7. Maintain a separate `resultIndex`.
8. After the loop, store the final `currentRow`.
9. Return only the inserted portion of result.

## Why Sort Rows?

Example:

```text
Before:
[[4,7],[1,4],[8,10]]

After sorting by start:
[[1,4],[4,7],[8,10]]
```

Do not sort values inside each row.

Java:

```java
Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
```

## Overlap Condition

```text
currentRowEnd >= nextRowStart
```

Example:

```text
current = [1,3]
next    = [2,6]

3 >= 2
```

So merge:

```text
[1,3] + [2,6]
→ [1,6]
```

Use:

```java
currentRow[1] = Math.max(currentRowEnd, nextRowEnd);
```

Important:

```text
[1,10] and [2,5]
```

must become:

```text
[1,10]
```

so use `max(currentEnd, nextEnd)`.

## If There Is No Overlap

Example:

```text
current = [1,6]
next    = [8,10]

6 >= 8
false
```

Then:

```text
store [1,6]
currentRow = [8,10]
```

## Input Index vs Result Index

They are different.

Example:

```text
Input:
[[1,3],[2,6],[8,10],[15,18]]

Result:
[[1,6],[8,10],[15,18]]
```

Input size = 4  
Result size = 3

So maintain:

```text
resultIndex
```

separately.

## Pseudocode

```text
sort intervals by start

result = array with input size
resultIndex = 0

currentRow = first interval

for each next interval:

    currentEnd = currentRow.end
    nextStart = next.start
    nextEnd = next.end

    if currentEnd >= nextStart:

        currentRow.end =
            max(currentEnd, nextEnd)

    else:

        result[resultIndex] = currentRow
        resultIndex++

        currentRow = next interval

store final currentRow

return only inserted part of result
```

## Java Code

```java
import java.util.Arrays;

class Solution {
    public int[][] merge(int[][] intervals) {

        int[][] result = new int[intervals.length][2];
        int resultIndex = 0;

        Arrays.sort(
            intervals,
            (a, b) -> Integer.compare(a[0], b[0])
        );

        int[] currentRow = intervals[0];

        for (int nextRow = 1; nextRow < intervals.length; nextRow++) {

            int currentRowEnd = currentRow[1];
            int nextRowStart = intervals[nextRow][0];
            int nextRowEnd = intervals[nextRow][1];

            if (currentRowEnd >= nextRowStart) {

                currentRow[1] =
                    Math.max(currentRowEnd, nextRowEnd);

            } else {

                result[resultIndex] = currentRow;
                resultIndex++;

                currentRow = intervals[nextRow];
            }
        }

        result[resultIndex] = currentRow;

        return Arrays.copyOf(result, resultIndex + 1);
    }
}
```

## Example Walkthrough

Input:

```text
[[1,3],[2,6],[8,10],[15,18]]
```

Start:

```text
currentRow = [1,3]
```

Compare:

```text
[1,3] with [2,6]

3 >= 2
→ overlap
→ currentRow = [1,6]
```

Next:

```text
[1,6] with [8,10]

6 >= 8
→ false
→ store [1,6]
→ currentRow = [8,10]
```

Next:

```text
[8,10] with [15,18]

10 >= 15
→ false
→ store [8,10]
→ currentRow = [15,18]
```

After loop:

```text
store [15,18]
```

Result:

```text
[[1,6],[8,10],[15,18]]
```

## Complexity

Let `N` = number of intervals.

```text
Sorting: O(N log N)
Traversal: O(N)

Overall Time: O(N log N)
Space: O(N) for result
```

## Mistakes / Learnings

- Sort rows by `interval[0]`, not values inside each row.
- No inner column loop is required because each interval is always `[start, end]`.
- Overlap condition after sorting:

```text
currentEnd >= nextStart
```

- When overlapping, keep the current start and update only the end.
- Use `max(currentEnd, nextEnd)`.
- When not overlapping, store `currentRow` and move to `nextRow`.
- Maintain a separate `resultIndex`.
- Return only the inserted portion of result.

## Short Remember Rule

```text
Sort by start

current = first interval

For every next interval:

    if current.end >= next.start
        current.end = max(current.end, next.end)

    else
        store current
        current = next

store final current
```

## Takeaway

```text
Merge Overlapping Intervals - Identified that currentRow end needs to be compared with nextRow start. Without sorting rows by start value, comparing intervals becomes difficult, so sort the rows, not the columns. If currentRow end >= nextRow start, merge the intervals by keeping the current start and updating the end with max(currentEnd, nextEnd). If they do not overlap, store the currentRow in result and move currentRow to nextRow. Maintain a separate result index because input index and result index are different. Return only the inserted portion of the result array.
```
