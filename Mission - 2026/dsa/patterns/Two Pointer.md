# 11. Container With Most Water

**Difficulty:** Medium  
**Pattern:** Two Pointers — Opposite Direction (not conventional Sliding Window)  
**Problem:** https://leetcode.com/problems/container-with-most-water/

## Problem Understanding

Given an array `height`, choose two vertical lines that hold the maximum amount of water. The water level is limited by the shorter wall.

**Formula:** `area = (right - left) * min(height[left], height[right])`

**Example:** `height = [1,8,6,2,5,4,8,3,7]` → **49** (indices 1 and 8: width 7 × height 7).

## Approach 1 — Brute Force

Check every possible pair of walls, calculate its area, and track the maximum.

```java
class Solution {
    public int maxArea(int[] height) {
        int maxHold = 0;
        for (int left = 0; left < height.length; left++) {
            for (int right = left + 1; right < height.length; right++) {
                int width = right - left;
                int minHeight = Math.min(height[left], height[right]);
                maxHold = Math.max(maxHold, width * minHeight);
            }
        }
        return maxHold;
    }
}
```

- **TC:** O(n²) — `n(n - 1) / 2` pairs.
- **SC:** O(1).

## Approach 2 — My Optimal Two-Pointer Solution

```java
class Solution {
    public int maxArea(int[] height) {
        int maxHold = 0;
        int left = 0;
        int right = height.length - 1;

        while (left < right) {
            int leftValue = height[left];
            int rightValue = height[right];
            int width = right - left;
            int minHeightOfContainerWall = Math.min(leftValue, rightValue);
            maxHold = Math.max(minHeightOfContainerWall * width, maxHold);

            if (leftValue < rightValue) {
                left++;
            } else {
                right--;
            }
        }
        return maxHold;
    }
}
```

- **TC:** O(n) — the pointers move inward a total of `n - 1` times.
- **SC:** O(1) — only a few variables.

### Why Move the Shorter Wall?

Moving either pointer reduces the width. If we move the taller wall while keeping the shorter wall, the limiting height cannot increase, so the area cannot improve. Moving the shorter wall gives us a chance to find a taller limiting wall. When both heights are equal, either pointer can move.

## Dry Run (Key Steps)

For `[1,8,6,2,5,4,8,3,7]`:

| Left | Right | Width | Min height | Area | Max |
|---:|---:|---:|---:|---:|---:|
| 0 (1) | 8 (7) | 8 | 1 | 8 | 8 |
| 1 (8) | 8 (7) | 7 | 7 | 49 | 49 |
| 1 (8) | 7 (3) | 6 | 3 | 18 | 49 |
| 1 (8) | 6 (8) | 5 | 8 | 40 | 49 |

The remaining iterations cannot exceed 49; final answer = **49**.

## My Takeaway (Corrected)

I used AI to understand the problem statement, but solved it myself using the opposite-direction two-pointer technique. I recognized that starting from both ends gives the maximum initial width. For each pair of walls, I calculated the width and the shorter wall's height, multiplied them to find the water-holding area, and tracked the maximum. I moved the shorter wall inward because moving the taller wall cannot improve the area while the shorter wall remains the limiting factor. This gives **O(n) time and O(1) space**.

## Pattern Recognition

- **Two Pointers (opposite direction):** Compare candidates at both ends and discard one side using a valid rule.
- **Sliding Window:** Typically expand/shrink a contiguous range while maintaining a window condition.
- This problem may appear in a Sliding Window section, but its primary pattern is **opposite-direction Two Pointers**.
