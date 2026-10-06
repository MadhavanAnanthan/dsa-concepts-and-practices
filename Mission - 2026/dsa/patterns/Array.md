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