# Employee Common Free Slot

## Problem Idea

Given multiple employees' busy time slots:

```java
int[][] emp1 = {
    {9,10},
    {12,13},
    {15,16}
};

int[][] emp2 = {
    {10,11},
    {13,14},
    {16,17}
};

int[][] emp3 = {
    {9,10},
    {12,14},
    {17,18}
};
```

Also given:

```java
int startTime = 9;
int endTime = 18;
int duration = 1;
```

Find the free slots within the working window where the available gap is at least the required `duration`.

---

## Pattern

**Merge Intervals + Find Gaps**

---

## Approach

### 1. Combine all employee busy intervals

Put all employee intervals into one `int[][]`.

Example:

```text
emp1 + emp2 + emp3
        ↓
all busy intervals together
```

---

### 2. Sort by start time

```java
Arrays.sort(arr, (a,b) -> Integer.compare(a[0], b[0]));
```

Meaning:

```text
a[0] → start time of first row
b[0] → start time of second row
```

Sort all intervals in ascending order based on their start time.

Example:

```text
[12,13]
[9,10]
[10,11]

becomes

[9,10]
[10,11]
[12,13]
```

If two intervals have the same start time, it is still fine for the merge logic.

Example:

```text
[16,18]
[16,17]
```

Both will eventually merge into:

```text
[16,18]
```

because we update the end using:

```java
Math.max(currentRow[1], nextRow[1])
```

---

## 3. Merge Busy Intervals

Keep one interval as:

```java
int[] currentRow
```

Compare it with:

```java
int[] nextRow
```

### Overlap condition

```java
if(currentRow[1] >= nextRow[0])
```

Meaning:

```text
current end >= next start
```

Then the intervals overlap or touch.

Example:

```text
[9,10]
[10,11]
```

Since:

```text
10 >= 10
```

merge them:

```text
[9,11]
```

Update the end using:

```java
currentRow[1] = Math.max(currentRow[1], nextRow[1]);
```

This is important because one interval may fully contain another.

Example:

```text
[16,18]
[16,17]
```

Result:

```text
[16,18]
```

---

### No overlap

If:

```java
currentRow[1] < nextRow[0]
```

then there is a gap.

Before moving forward:

```text
add currentRow to result
currentRow = nextRow
```

Finally, after the loop, add the last `currentRow`.

---

## 4. Find Free Slots

After merging, suppose the busy intervals are:

```text
[9,11]
[12,14]
[15,18]
```

Now check free time in three places.

### A. Start boundary

Check:

```text
startTime → first busy interval start
```

Condition:

```java
firstBusyStart - startTime >= duration
```

Example:

```text
startTime = 9
first busy = [10,11]

free = [9,10]
```

---

### B. Between merged busy intervals

For every two merged intervals:

```text
current busy end → next busy start
```

Condition:

```java
nextRow.get(0) - currentRowList.get(1) >= duration
```

Example:

```text
[9,11]
[12,14]

gap = 12 - 11 = 1
```

If:

```text
duration = 1
```

then:

```text
[11,12]
```

is a valid free slot.

---

### C. End boundary

Check:

```text
last busy interval end → endTime
```

Condition:

```java
endTime - lastBusyEnd >= duration
```

Example:

```text
last busy = [16,17]
endTime = 18

free = [17,18]
```

---

## Important Rule

Do not print every gap blindly.

Always check:

```java
gap >= duration
```

Because a free gap smaller than the required meeting duration is not useful.

---

## Mental Model

```text
1. Combine
      ↓
2. Sort
      ↓
3. Merge all busy intervals
      ↓
4. Find gaps
      ↓
5. Check gap >= duration
```

---

## Pseudocode

```text
combine all employees' busy intervals

sort intervals by start time

currentRow = first interval

for each nextRow:
    if currentRow.end >= nextRow.start:
        currentRow.end = max(currentRow.end, nextRow.end)
    else:
        add currentRow to merged result
        currentRow = nextRow

add final currentRow

check startTime → first busy start

for each pair of merged intervals:
    check current end → next start

check last busy end → endTime

only keep a free slot when:
gap >= duration
```

---

## Key Takeaway

This problem is not about checking every hour from `startTime` to `endTime`.

The better approach is:

```text
merge all busy intervals first
```

and then:

```text
find the gaps between the merged intervals
```

The gaps are the common available/free slots.

Also remember the two boundary cases:

```text
startTime → first busy interval
last busy interval → endTime
```
