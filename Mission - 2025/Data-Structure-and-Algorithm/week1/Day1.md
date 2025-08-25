# 📘 Array Fundamentals & Patterns

Arrays are a **fundamental data structure** in computer science. They provide **contiguous memory allocation**, enabling efficient random access by index.

---

## ✅ Properties of Arrays
1. **Fixed Size** → Not dynamic (in low-level arrays).
2. **Contiguous Memory** → Elements are stored sequentially.
3. **Homogeneous** → Same data type.
4. **Index-based Access** → Direct access by index.
5. **Types** →
    - **1D Array** → Linear (list of elements).
    - **2D Array** → Matrix/grid representation.
    - **Multidimensional Arrays** → Higher-order representation.

---

## ⏱️ Time & Space Complexity Analysis
| Operation | Time Complexity | Space Complexity | Notes |
|-----------|-----------------|------------------|-------|
| **Access** | `O(1)` | `O(1)` | Direct access via index. |
| **Update** | `O(1)` | `O(1)` | Replace element at index. |
| **Insert/Delete** | `O(n)` | `O(1)` | Shift elements after index. |
| **Traverse** | `O(n)` | `O(1)` | Visit each element. |

**Deletion Logic**: When deleting an element at index `i`, elements from `i+1` to end shift left by one position. Actual memory size remains the same.

---

## 🔑 Problem-Solving Patterns in Arrays
When solving array problems, you usually move from **brute force → optimized → optimal patterns**. Common patterns include:

1. **Prefix Sum / Running Sum**
    - Useful for subarray sums, running totals.
    - Brute Force: Nested loop (`O(n^2)`) → Better (`O(n)` + extra space) → Optimal (`O(n)`, in-place).

2. **Two-Pointer / In-Pointer / Out-Pointer**
    - Used for merging, comparisons, rearrangements.
    - Common in subarray and partition problems.

3. **Sliding Window**
    - For subarray problems with length/k condition.
    - Optimizes from `O(n^2)` → `O(n)`.

4. **Hashing based patterns**
    - When we need fast lookup (`O(1)` avg).

5. **Greedy Iterative Updates**
    - Incrementally build answer in-place.

---

## 💡 Problems & Solutions

### 1. Running Sum of 1D Array
**Brute Force Idea**  
For index `i`, sum all elements `[0..i]` again.
- Time: `O(n^2)`
- Space: `O(1)`

**Better Solution (Auxiliary Array)**  
- Time: `O(n)`
- Space: `O(n)`
- **Pattern Used**: Prefix Sum with extra array
```java
class Solution {
public int[] runningSum(int[] nums) {
int[] result = new int[nums.length];
result = nums;
for (int i = 1; i < nums.length; i++) {
result[i] = nums[i] + result[i - 1];
}
return result;
}
}
```
**Optimal Solution (In-place)11**  
- Time: `O(n)`
- Space: `O(1)`
- **Pattern Used**: Prefix Sum + In-place Update
```java
  class Solution {
public int[] runningSum(int[] nums) {
for (int i = 1; i < nums.length; i++) {
nums[i] = nums[i] + nums[i - 1];
}
return nums;
}
}
```


### 2. Shuffle the Array
Array: `[x1,x2,...,xn,y1,y2,...,yn]` → `[x1,y1,x2,y2,...,xn,yn]`.

**Brute Force Idea**  
Pick each `xi`, then scan for corresponding `yi`.
- Time: `O(n^2)`
- Space: `O(n)`

**Better Solution (Two Pointers + Extra Array)**  
- Time: `O(n)`
- Space: `O(n)`
- **Pattern Used**: Two-Pointer (split halves, merge together)
```java
class Solution {
public int[] shuffle(int[] nums, int n) {
int[] result = new int[nums.length];
int left = 0, right = n;
int index = 0;
while (left < n) {
result[index++] = nums[left++];
result[index++] = nums[right++];
}
return result;
}
}
```
**Optimal Solution (In-place if allowed)**
- Encode two numbers into one slot (math encoding).
- Time: `O(n)`
- Space: `O(1)`
- **Pattern Used**: Two-Pointer + In-place Encoding

---

## 📊 Key Takeaways
- Arrays excel at **direct access & updates** (`O(1)`), but **insertion/deletion is costly** (`O(n)`).
- **Running Sum** → classic **Prefix Sum** problem (can optimize to in-place).
- **Shuffle Problem** → natural **Two-Pointer Approach**.
- Always ask: *Can I reduce extra space? Can I do it in-place?*  
