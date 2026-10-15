# 151. Reverse Words in a String

**Pattern:** String Traversal · Reverse Traversal  
**Optimal complexity:** O(n) time, O(n) space in Java (including output)

## 1. Requirements

Given a string `s`, reverse the **order of words**, not the characters within each word.

- A word is a sequence of non-space characters.
- Remove leading and trailing spaces.
- Keep **exactly one space** between adjacent words in the result.

**Example:** `"  the   sky is  blue  "` → `"blue is sky the"`

## 2. Edge Cases

| Input | Expected output | Reason |
|---|---|---|
| `"hello"` | `"hello"` | Single word |
| `"  hello world  "` | `"world hello"` | Leading/trailing spaces |
| `"a   b"` | `"b a"` | Multiple internal spaces |
| `"a b c"` | `"c b a"` | Normal case |
| `"   "` | `""` | All spaces (extra defensive case) |

LeetCode guarantees at least one word, but handling all-spaces input is still straightforward.

## 3. My Original Solution — `split(" ")` + Boolean Flag

**Idea:** Split on a single literal space, traverse the resulting array backward, skip empty entries, and use a `firstWord` flag so the output does not begin or end with extra spaces.

```java
class Solution {
    public String reverseWords(String s) {
        StringBuilder str = new StringBuilder();
        boolean firstWord = false;
        String[] splittedString = s.split(" ");
        for(int splitStr=splittedString.length-1; splitStr >= 0; splitStr--){
            if(!splittedString[splitStr].isEmpty()){
            if(!firstWord){
                str.append(splittedString[splitStr].trim());
                firstWord=true;
            }else{
                str.append(" "+splittedString[splitStr].trim());
            }
            }
        }
        return str.toString();
    }
}
```

**What works:** `split(" ")` splits at each individual space (so multiple spaces can produce empty strings); the empty-string check skips those gaps. The boolean flag avoids a leading separator. `trim()` on each word is unnecessary for LeetCode's space-separated input.

**Complexity:** O(n) time, O(n) extra space (split array + output).

## 4. My Improved Solution — Normalize Spaces, No Flag

**Evolution:** Use `trim().split("\\s+")` to get just the words. Append each word, adding a space **only if `i > 0`**; the flag and empty-entry check are no longer required.

```java
class Solution {
    public String reverseWords(String s) {
        StringBuilder str = new StringBuilder();
        String[] splittedString = s.trim().split("\\s+");

        for (int i = splittedString.length - 1; i >= 0; i--) {
            str.append(splittedString[i]);
            if (i > 0) {
                str.append(" ");
            }
        }

        return str.toString();
    }
}
```

**Remember:**
- `split(" ")` splits on one ordinary space at a time; `split("\\s+")` matches **one or more whitespace characters** as a single delimiter.
- `trim()` removes surrounding spaces; it does **not** remove internal spaces. The regex handles gaps when splitting.
- `if (i > 0)` prevents a trailing space since the last word appended is `splittedString[0]`.
- `StringBuilder` is mutable and efficient for appending, but not thread-safe.

**Complexity:** O(n) time, O(n) extra space. LeetCode guarantees at least one word; otherwise the all-spaces case needs separate handling.

## 5. Interview Takeaways

- **Why reverse traversal?** The final word in the input must become the first word in the output.
- **Why not O(log n)?** We must process and output the characters: **Ω(n) lower bound**, so O(n) is time-optimal.
- **Opposite-end pointers ≠ binary search:** Moving pointers one position at a time is still O(n), not O(log n).
- **Evolution:** Original boolean flag + empty checks → cleaner `trim().split("\\s+")` + `i > 0`. The improved solution is interview-ready.

# 1903. Largest Odd Number in String

**Pattern:** String Traversal / Reverse Traversal / Early Termination  
**LeetCode:** https://leetcode.com/problems/largest-odd-number-in-string/

### Problem Understanding

Find the largest odd-valued substring of the given numeric string.

- The largest odd number must start at index `0`.
- An odd number must end with an odd digit (`1, 3, 5, 7, 9`).
- Find the **rightmost odd digit** and return the substring from `0` to that index.
- Avoid converting the entire String into `int` or `long` because `num.length()` can be up to `10^5`.

**Example:**

```text
Input:  "135753311357513715175324680"
Output: "1357533113575137151753"
```

## 1. Initial Approach — Forward Traversal

```java
class Solution {
    public String largestOddNumber(String num) {
        String result = "";

        for (int index = 0; index < num.length(); index++) {
            if (num.charAt(index) % 2 != 0) {
                result = num.substring(0, index + 1);
            }
        }
        return result;
    }
}

// TC: O(n²) worst case
// SC: O(n) peak auxiliary space
```

**Why O(n²)?**

- The loop runs O(n) times.
- `substring(0, index + 1)` copies O(k) characters per execution.
- When every digit is odd, repeated copying costs `1 + 2 + 3 + ... + n = O(n²)`.
- Each new substring replaces the previous result, so peak auxiliary space is O(n), although total allocation can be O(n²).

## 2. Optimized Approach — Reverse Traversal

```java
class Solution {
    public String largestOddNumber(String num) {

        for (int index = num.length() - 1; index >= 0; index--) {

            // Convert char digit into numeric value
            if ((num.charAt(index) - '0') % 2 != 0) {

                // Copy the required substring only once
                return num.substring(0, index + 1);
            }
        }

        return "";
    }
}

// TC: O(n) worst case
// SC: O(n) including returned substring
// Working space excluding output: O(1)
```

**Why is this better?**

- Scan from right to left to identify the last odd digit.
- Skip trailing even digits without copying anything.
- Create the substring only once and return immediately.
- O(n) scanning + at most O(n) copying = O(n).

## 3. Why `charAt(index) - '0'`?

```java
char digit = '7';

int value = digit - '0'; // 55 - 48 = 7
```

- `charAt()` returns a `char`, not an `int` digit.
- Subtracting `'0'` converts a character digit (`'0'`–`'9'`) into its numeric value (`0`–`9`).
- It is an O(1) operation and doesn't require `Integer.parseInt()`.

```java
num.charAt(index) % 2 != 0         // Works for digit chars
(num.charAt(index) - '0') % 2 != 0 // Clearer numeric intent
```

**Important:** Subtracting `'0'` improves readability, not asymptotic performance. Both checks are O(1).

## 4. Complexity Comparison

| Approach | Time (worst) | Space (peak) |
|---|---|---|
| Forward traversal + repeated substring | O(n²) | O(n) |
| Reverse traversal + single substring | O(n) | O(n) |

*Space includes the returned String. The optimized algorithm uses O(1) working space excluding output.*

## Key Interview Takeaways

1. **One loop can still be O(n²)** when it repeatedly invokes an O(n) operation.
2. `substring(start, end)` costs O(k) time and space for a copied substring of length k in modern Java.
3. Avoid repeated substring creation; identify the required boundaries first.
4. Reverse traversal + early termination eliminates unnecessary copying.
5. For numeric strings, process individual digits without converting the entire String into an integer.

**Revision one-liner:** Scan right to left, find the rightmost odd digit, and return the prefix using a single `substring(0, index + 1)`. TC O(n), working SC O(1), output-inclusive SC O(n).