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

# 205. Isomorphic Strings

**Problem:** [LeetCode 205](https://leetcode.com/problems/isomorphic-strings/)  
**Pattern:** Hashing / One-to-one character mapping

## Key idea

Every character in `s` must map **consistently** to one character in `t`, and **two different characters in `s` cannot map to the same character in `t`**.

- `egg` → `add` ✅
- `foo` → `bar` ❌ (`o` would map to both `a` and `r`)
- `ab` → `aa` ❌ (two source characters map to `a`)

## Approach 1 — HashMap + HashSet

- `mapping` remembers `s` → `t`.
- `occurredValue` prevents multiple source characters from using the same target.

**TC:** `O(n)` average · **SC:** `O(k)` for `k` distinct characters (up to `O(n)` for an unrestricted alphabet).

```java
import java.util.*;

class Solution {
    public boolean isIsomorphic(String s, String t) {
        Map<Character, Character> mapping = new HashMap<>();
        Set<Character> occurredValue = new HashSet<>();

        for (int index = 0; index < s.length(); index++) {
            char source = s.charAt(index);
            char target = t.charAt(index);

            if (mapping.containsKey(source)) {
                if (mapping.get(source) != target) return false;
            } else {
                if (occurredValue.contains(target)) return false;
                mapping.put(source, target);
                occurredValue.add(target);
            }
        }
        return true;
    }
}
```

## Approach 2 — Two fixed arrays (optimal for ASCII)

- `frequency[source]` stores the target character encoded as `target + 1`.
- `occurrence[target]` tracks whether the target character is already used.
- Array indexing replaces HashMap/HashSet lookups. These arrays track **mapping and usage**, not actual frequencies.

**TC:** `O(n)` · **SC:** `O(1)` (two arrays of 128 integers, for ASCII inputs).

```java
class Solution {
    public boolean isIsomorphic(String s, String t) {
        int[] frequency = new int[128];
        int[] occurrence = new int[128];

        for (int index = 0; index < s.length(); index++) {
            char source = s.charAt(index);
            char target = t.charAt(index);

            if (frequency[source] != 0) {
                if (frequency[source] != target + 1) return false;
            } else {
                if (occurrence[target] == 1) return false;

                frequency[source] = target + 1;
                occurrence[target] = 1;
            }
        }
        return true;
    }
}
```

### Why `+1`?

Java initializes `int[]` elements to **0**, which we use to mean **unmapped**. But ASCII `\0` (the NUL character) also has numeric value `0`.

- **Without `+1`:** mapping `a → '\0'` stores `0`, so `a` incorrectly appears unmapped later.
- **With `+1`:** stored `0` means unmapped; values `1–128` represent ASCII codes `0–127`.
- **Important:** both storing and comparing must use `target + 1`.

Example: `s = "aa"`, `t = "\0b"` must return `false`: the same `a` cannot map first to NUL and then to `b`.

### Why keep the second array?

Without `occurrence[]`, `s = "ab"`, `t = "cc"` could wrongly pass: both `a` and `b` would map to `c`. The second array enforces the **one-to-one** rule.

## Complexity summary

| Approach | Time | Auxiliary space |
|---|---|---|
| HashMap + HashSet | `O(n)` average | `O(k)` (at most `O(n)` for unrestricted characters) |
| Two 128-element arrays | `O(n)` | `O(1)` |

**Remember:** Validate both **consistent mapping** and **unique targets**. Two fixed-size arrays are already asymptotically optimal for ASCII; removing one would not improve `O(1)` space. The 128-element version assumes ASCII input (not arbitrary Java Unicode characters).

# 14. Longest Common Prefix

**Problem:** https://leetcode.com/problems/longest-common-prefix/  
**Pattern:** String scanning / Prefix matching

## Key idea
Find the longest starting sequence shared by **every** string.

- `["flower", "flow", "flight"]` → `"fl"`
- `["dog", "racecar", "car"]` → `""`

## Approach 1 — Horizontal scanning (my approach)

Start with the first string as the current prefix. Compare it with each subsequent string, shrinking the prefix to their matching starting characters. Stop early if the prefix becomes empty.

```java
class Solution {
    public String longestCommonPrefix(String[] strs) {
        String result = strs[0];

        for (int index = 1; index < strs.length; index++) {
            String value = strs[index];
            int limit = Math.min(result.length(), value.length());
            int count = 0;

            while (count < limit && result.charAt(count) == value.charAt(count)) {
                count++;
            }

            result = result.substring(0, count);
            if (result.isEmpty()) return "";
        }
        return result;
    }
}
```

**Time:** O(S), where S is the total number of characters examined (upper bound O(N × L), with N strings and L the length of the shortest string, plus any first-string initialization).  
**Auxiliary space:** O(L) in the worst case for the newly created intermediate prefix strings; excludes the returned result.

### Why `Math.min()` is NOT redundant

```java
result = "flower";
value  = "flow";
```

After matching 4 characters, calling `value.charAt(4)` would throw `StringIndexOutOfBoundsException`. The condition `count < limit` prevents accessing past **either** string's end. Java evaluates `&&` left-to-right, so the bound is checked before `charAt()`.

The `limit` **variable** is optional, but **length checking is essential**. Equivalent condition:

```java
while (count < result.length() && count < value.length()
        && result.charAt(count) == value.charAt(count)) {
    count++;
}
```

`result.substring(0, count)` and `value.substring(0, count)` are equivalent because the first `count` characters match. Using `result` better communicates that we are shrinking the current prefix.

## Approach 2 — Vertical scanning

Compare character position `i` across **all** strings. Return the prefix immediately when a string ends or a character differs.

```java
class Solution {
    public String longestCommonPrefix(String[] strs) {
        String first = strs[0];

        for (int i = 0; i < first.length(); i++) {
            char current = first.charAt(i);

            for (int j = 1; j < strs.length; j++) {
                if (i >= strs[j].length() || strs[j].charAt(i) != current) {
                    return first.substring(0, i);
                }
            }
        }
        return first;
    }
}
```

**Time:** O(S), worst case O(N × L), for N strings and shortest-string length L.  
**Auxiliary space:** O(1), excluding the returned substring.

## Comparison

| Method | Time | Auxiliary space | Main idea |
|---|---|---|---|
| Horizontal | O(S) | O(L) | Shrink the common prefix after each string |
| Vertical | O(S) | O(1) | Check one character index across all strings |

**Revision rule:** Horizontal = compare **string by string**; vertical = compare **position by position**. Check string boundaries **before** calling `charAt()`.

> LeetCode guarantees at least one string. If the input could be an empty array outside LeetCode, add an explicit empty-array guard.
