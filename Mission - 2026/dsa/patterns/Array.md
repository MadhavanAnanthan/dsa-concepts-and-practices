# 53. Maximum Subarray

## Pattern
**Brute Force Subarray Traversal → Kadane's Algorithm**

## Idea / Intuition

For subarray problems:

- Outer loop decides **which index to start from**.
- Inner loop starts from the **same index `i`** and keeps extending the subarray.
- For the optimal solution, if the running sum becomes negative, discard it because it will reduce any future sum.

---

## Brute Force — O(n²)

```java
class Solution {
    public int maxSubArray(int[] nums) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < nums.length; i++) {
            int currentCycleMax = 0;

            for (int current = i; current < nums.length; current++) {
                currentCycleMax += nums[current];
                max = Math.max(max, currentCycleMax);
            }
        }

        return max;
    }
}
```

### Main Brute-Force Learning

```text
Outer loop → choose starting index
Inner loop → start from i and extend the subarray
```

For subarray generation, usually think:

```java
for (int i = 0; i < nums.length; i++) {
    for (int current = i; current < nums.length; current++) {
```

not automatically `i + 1`.

---

## Optimal — Kadane's Algorithm — O(n)

If the current running sum becomes negative, reset it because carrying a negative sum will only reduce the next possible subarray sum.

```java
class Solution {
    public int maxSubArray(int[] nums) {
        int max = Integer.MIN_VALUE;
        int currentSum = 0;

        for (int num : nums) {
            currentSum += num;
            max = Math.max(max, currentSum);

            if (currentSum < 0) {
                currentSum = 0;
            }
        }

        return max;
    }
}
```

## Final Takeaway

```text
Subarray brute force:
start at every index → extend from the same index

Maximum sum optimal:
running sum becomes negative → discard it

Time:
Brute Force → O(n²)
Kadane → O(n)
```

# 152. Maximum Product Subarray

## Pattern
**Brute Force Subarray Traversal → Prefix & Suffix Product**

## Idea / Intuition

For brute force:
- Outer loop decides the starting index.
- Inner loop starts from the same index and extends the subarray.
- Keep multiplying and track the maximum product.

For the optimal approach:
- Negative numbers can flip the product from positive to negative and vice versa.
- So calculate product from both directions: **prefix from left** and **suffix from right**.
- If `0` appears, reset the running product to `1` because any product crossing `0` becomes `0`.

---

## Brute Force — O(n²)

```java
class Solution {
    public int maxProduct(int[] nums) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < nums.length; i++) {
            int currentCycleMax = 1;

            for (int j = i; j < nums.length; j++) {
                currentCycleMax *= nums[j];
                max = Math.max(max, currentCycleMax);
            }
        }

        return max;
    }
}
```

### Main Brute-Force Learning

```text
Outer loop → choose starting index
Inner loop → start from i and extend the subarray
```

---

## Optimal — Prefix & Suffix — O(n)

```java
class Solution {
    public int maxProduct(int[] nums) {
        int prefix = 1;
        int suffix = 1;

        int maxPrefix = Integer.MIN_VALUE;
        int maxSuffix = Integer.MIN_VALUE;

        for (int current = 0; current < nums.length; current++) {

            prefix *= nums[current];
            maxPrefix = Math.max(prefix, maxPrefix);

            suffix *= nums[(nums.length - 1) - current];
            maxSuffix = Math.max(maxSuffix, suffix);

            if (nums[current] == 0) {
                prefix = 1;
            }

            if (nums[(nums.length - 1) - current] == 0) {
                suffix = 1;
            }
        }

        return Math.max(maxSuffix, maxPrefix);
    }
}
```

## Main Intuition

```text
Positive product → useful directly

Negative product → may become positive after multiplying another negative

So scan from both sides:
prefix → left to right
suffix → right to left
```

If there are an odd number of negative values, the best product may come from removing either:
- the prefix up to the first negative, or
- the suffix after the last negative.

Scanning from both directions naturally covers both possibilities.

## Zero Handling

```text
0 breaks the product chain.
After processing 0, reset running product to 1.
```

No need to manually split the array into separate subarrays.

## Final Takeaway

```text
Subarray brute force:
start at every index → extend from same index → keep multiplying

Optimal:
track prefix product + suffix product
reset product to 1 after 0

Time:
Brute Force → O(n²)
Prefix/Suffix → O(n)

Space:
O(1)
```

# Merge Sorted Arrays

There are **2 common variations**.

---

## 1. Merge Sorted Array — Extra Space Available in Array 1

Example:

```text id="ru4mve"
nums1 = [1,2,3,0,0,0]
nums2 = [2,5,6]
```

Pattern: **Two Pointers / Three Pointers / Merge from End**

### Idea

- `p1` → last valid element in `nums1`
- `p2` → last element in `nums2`
- `write` → last available position in `nums1`
- Compare largest values.
- Place the larger value at `write`.
- Move backward.
- If `nums2` has leftovers, copy them.

### Code

```java id="gmw2s4"
class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {

        int p1 = m - 1;
        int p2 = n - 1;
        int write = m + n - 1;

        while (p1 >= 0 && p2 >= 0) {

            if (nums1[p1] > nums2[p2]) {
                nums1[write] = nums1[p1];
                p1--;
            } else {
                nums1[write] = nums2[p2];
                p2--;
            }

            write--;
        }

        while (p2 >= 0) {
            nums1[write] = nums2[p2];
            p2--;
            write--;
        }
    }
}
```

### Complexity

```text id="sub90d"
Time  : O(m+n)
Space : O(1)
```

### Remember

```text id="4gvtyt"
Two sorted arrays + free space in first array
→ merge from right to left
→ O(m+n), O(1)
```

---

# 2. Merge Without Extra Space

Example:

```text id="d6ot0s"
a = [1,5,9,10,15,20]
b = [2,3,8,13]
```

Goal:

```text id="h6a2au"
a = [1,2,3,5,8,9]
b = [10,13,15,20]
```

Important:

```text id="hnhsc0"
Simple swapping may break sorted order.

After every modification ask:
"Is my core assumption still valid?"
```

---

## Approach 1 — Insertion-Sort Style

Compare from the boundary.

If the largest value in `a` is greater than the smallest in `b`, swap them.

Then move the new value in `a` to its correct position so `a` remains sorted.

### Code

```java id="ic6i9m"
class Solution {
    public void mergeArrays(int[] a, int[] b) {

        int n = a.length;
        int m = b.length;

        for (int i = 0; i < m; i++) {

            if (a[n - 1] > b[i]) {

                int temp = a[n - 1];
                a[n - 1] = b[i];
                b[i] = temp;

                int current = n - 1;

                while (current > 0 &&
                       a[current] < a[current - 1]) {

                    int swap = a[current];
                    a[current] = a[current - 1];
                    a[current - 1] = swap;

                    current--;
                }
            }
        }

        Arrays.sort(b);
    }
}
```

### Complexity

```text id="9y2nst"
Moving inserted value in a: O(n)
Repeated for up to m elements

Time  : O(m*n) + O(m log m)
≈ O(m*n)

Space : O(1)
```

---

## Approach 2 — Swap and Sort

Use:

```text id="s3hlrk"
left  = end of a
right = start of b
```

If:

```text id="qk4ppx"
a[left] > b[right]
```

swap them.

After all necessary swaps, sort both arrays once.

### Code

```java id="5zhnn2"
class Solution {
    public void mergeArrays(int[] a, int[] b) {

        int left = a.length - 1;
        int right = 0;

        while (left >= 0 && right < b.length) {

            if (a[left] > b[right]) {

                int temp = a[left];
                a[left] = b[right];
                b[right] = temp;
            }

            left--;
            right++;
        }

        Arrays.sort(a);
        Arrays.sort(b);
    }
}
```

### Complexity

```text id="9gj6ev"
Comparison : O(min(m,n))
Sort a     : O(n log n)
Sort b     : O(m log m)

Time  : O(n log n + m log m)
Space : O(1)*
```

`*`Ignoring internal sorting implementation space.

---

## Approach 3 — Gap Method

Technique: **Gap Method / Shell-Sort-style comparison**

Treat:

```text id="953j5p"
a + b
```

as one virtual array.

Start with:

```text id="2wcmfz"
gap = ceil((m+n)/2)
```

Then compare values `gap` distance apart.

After one full pass:

```text id="69lcyp"
gap = ceil(gap/2)
```

Example:

```text id="zx118e"
total = 10

gap:
5 → 3 → 2 → 1
```

### Code

```java id="ckjy86"
class Solution {
    public void mergeArrays(int[] a, int[] b) {

        int n = a.length;
        int m = b.length;

        int totalLength = n + m;

        int gap = (totalLength + 1) / 2;

        while (gap >= 1) {

            int left = 0;
            int right = left + gap;

            while (right < totalLength) {

                // left in a, right in b
                if (left < n && right >= n) {

                    if (a[left] > b[right - n]) {

                        int temp = a[left];

                        a[left] = b[right - n];
                        b[right - n] = temp;
                    }
                }

                // both in a
                else if (right < n) {

                    if (a[left] > a[right]) {

                        int temp = a[left];

                        a[left] = a[right];
                        a[right] = temp;
                    }
                }

                // both in b
                else {

                    if (b[left - n] > b[right - n]) {

                        int temp = b[left - n];

                        b[left - n] = b[right - n];
                        b[right - n] = temp;
                    }
                }

                left++;
                right++;
            }

            if (gap == 1) {
                break;
            }

            gap = (gap + 1) / 2;
        }
    }
}
```

### Virtual Index Mapping

```text id="no7iey"
index < n
→ a[index]

index >= n
→ b[index - n]
```

### Complexity

```text id="2vl9o9"
Each pass      : O(m+n)
Gap reductions : O(log(m+n))

Time  : O((m+n) log(m+n))
Space : O(1)
```

---

## Quick Comparison

| Variation / Approach | Time | Extra Space |
|---|---:|---:|
| Array 1 has free space — merge from end | `O(m+n)` | `O(1)` |
| No space — insertion-style | `O(m*n)` | `O(1)` |
| No space — swap + sort | `O(n log n + m log m)` | `O(1)*` |
| No space — gap method | `O((m+n) log(m+n))` | `O(1)` |

## Final Memory Rule

```text id="0xn3g9"
Free space available in first array
→ Three pointers
→ Merge from end

No free space
→ Simple swap may break sorted invariant
→ Insertion-style = brute force
→ Swap + Sort = better
→ Gap Method = optimized in-place technique
```