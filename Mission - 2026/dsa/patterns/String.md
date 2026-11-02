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

# 5. Longest Palindromic Substring

**Pattern:** String · Two Pointers · Expand Around Center  
**Problem:** [LeetCode 5](https://leetcode.com/problems/longest-palindromic-substring/)

Given a string `s`, return its longest contiguous palindromic substring.

**Example:** `s = "babad"` → `"bab"` or `"aba"` (both valid).

## Approach 1 — Brute Force

**Idea:** Try each possible substring using two loops. For every candidate, compare its leftmost and rightmost characters, moving inward until they mismatch or meet. Keep the longest palindrome.

> The code below reconstructs the brute-force approach from my description; it is not my exact original submission.

```java
class Solution {
    public String longestPalindrome(String s) {
        int from = 0, to = 0;

        for (int i = 0; i < s.length(); i++) {
            for (int j = s.length() - 1; j > i; j--) {
                // Skip substrings that cannot improve the current answer.
                if (j - i <= to - from) break;

                int left = i, right = j;
                while (left < right && s.charAt(left) == s.charAt(right)) {
                    left++;
                    right--;
                }

                if (left >= right) { // Valid palindrome
                    from = i;
                    to = j;
                    break; // Longest palindrome starting at i found
                }
            }
        }
        return s.substring(from, to + 1);
    }
}
```

- **Time:** O(n³) worst case — O(n²) candidate substrings, each requiring up to O(n) comparisons.
- **Auxiliary space:** O(1) — only indexes/pointers; excludes the returned substring.

## Approach 2 — Expand Around Center (Recommended)

**Key insight:** Every palindrome is symmetric around a center. Instead of trying all substrings, start at each center and expand **outward** while the characters match.

Two types of centers must be checked at **every index**:

- **Odd length:** `(i, i)` — e.g., `"aba"`.
- **Even length:** `(i, i + 1)` — e.g., `"abba"`.

```java
class Solution {
    public String longestPalindrome(String s) {
        int from = 0;
        int to = 0;

        for (int i = 0; i < s.length(); i++) {
            int[] odd = expand(s, i, i);       // One-character center
            int[] even = expand(s, i, i + 1);  // Center between characters

            if (odd[1] - odd[0] > to - from) {
                from = odd[0];
                to = odd[1];
            }

            if (even[1] - even[0] > to - from) {
                from = even[0];
                to = even[1];
            }
        }

        return s.substring(from, to + 1); // End index is exclusive
    }

    public int[] expand(String s, int left, int right) {
        while (left >= 0 && right < s.length()
                && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        // Undo the last expansion: pointers are now beyond the palindrome.
        return new int[]{left + 1, right - 1};
    }
}
```

- **Time:** O(n²) — O(n) centers, each expanding up to O(n) characters.
- **Auxiliary space:** O(1) — fixed number of indexes and 2-element arrays at a time (though those arrays cause O(n) total allocations).
- **Result allocation:** O(k) for the returned substring of length `k` in modern Java.

### Important details

1. The **whole string's length** does not determine the answer's parity: `"xabba"` has odd length, but `"abba"` is its longest palindrome.
2. `expand()` returns inclusive bounds `[left + 1, right - 1]`; the final failed comparison has already pushed the pointers outside the palindrome.
3. Comparing `end - start` is valid here because all saved ranges use **inclusive** endpoints. Their actual lengths are `end - start + 1`.
4. `substring(from, to + 1)` includes the character at index `to`.
5. LeetCode guarantees a nonempty string. For general-purpose code, handle `s.isEmpty()` before returning `substring(0, 1)`.

## Complexity summary

| Approach | Time | Auxiliary space |
|---|---|---|
| Brute force | O(n³) | O(1) |
| Expand around center | **O(n²)** | **O(1)** |

## One-line takeaway

**Palindrome + symmetry → expand two pointers from each possible center; check both `(i, i)` and `(i, i + 1)` and record the longest valid bounds.**

# 38. Count and Say

**Pattern:** String Traversal / Run-Length Encoding

## Approach (Iterative)
- Start with `result = "1"`.
- Repeat from term `2` to `n`.
- Scan the previous term, counting **consecutive** occurrences of each character.
- When the character changes, append `count` followed by the previous character.
- Append the final group after the inner loop, then use the built string as the next term.

## Optimized Java Code

```java
class Solution {
    public String countAndSay(int n) {
        String result = "1";

        for (int term = 2; term <= n; term++) {
            StringBuilder next = new StringBuilder();
            char previous = result.charAt(0);
            int count = 1;

            for (int i = 1; i < result.length(); i++) {
                char current = result.charAt(i);

                if (current == previous) {
                    count++;
                } else {
                    next.append(count).append(previous);
                    previous = current;
                    count = 1;
                }
            }

            next.append(count).append(previous); // Final group
            result = next.toString();
        }

        return result;
    }
}
```

## Evolution of My Solution

1. **Initial counting logic:** Reset `count = 0` on a character change and used a boolean to handle the last group. Fixed this by starting with `count = 1`, resetting to `1` for a new group, and always appending the final group after the loop.
2. **String concatenation:** Repeatedly reassigned strings using `concat()`, copying previously built content and causing up to **O(n × m²)** time.
3. **StringBuilder:** Switched to `append()` to build each term efficiently, reducing the time bound to **O(n × m)**.
4. **Simplified traversal:** Started the inner loop at index `1`, compared `char` values directly, and updated `previous` only when the character changed.

## Complexity

Let `n` be the requested term and `m` the maximum generated string length among terms `1` through `n`.

| Version | Time Complexity | Space Complexity |
|---|---|---|
| Repeated `String.concat()` | O(n × m²) upper bound | O(m) peak live space* |
| `StringBuilder` (optimized) | **O(n × m)** | **O(m)** |

- Outer loop runs `n - 1` times; each inner loop scans a previous term of length at most `m`.
- `StringBuilder` appends are amortized efficient, and `toString()` copies the output at most once per term.
- Only the current and next generated strings are retained, so the iterative solution uses **O(m)** space.

*For the concatenation version, O(m) refers to peak live memory; repeated concatenation can create many temporary strings and extra allocation/GC work.

## Takeaway

**Count and Say:** Use an outer loop to generate terms up to `n`. For each term, scan the previous result, count consecutive occurrences of the same character, and append `count + character` to build the next term. Initially, repeated string reassignment using `concat()` increased the time bound to **O(n × m²)**. Replacing it with `StringBuilder` improved the time bound to **O(n × m)** while keeping auxiliary space at **O(m)**. Solved using the iterative approach.

# [2942. Find Words Containing Character](https://leetcode.com/problems/find-words-containing-character/)

**Difficulty:** Easy  
**Pattern:** Array Traversal + String Searching

### 1. Approach 1 – Using Nested Loops (Previous)

```java
class Solution {
    public List<Integer> findWordsContaining(String[] words, char x) {
        List<Integer> result = new ArrayList<>();

        for (int index = 0; index < words.length; index++) {
            for (int iterateIndex = 0; iterateIndex < words[index].length(); iterateIndex++) {
                if (words[index].charAt(iterateIndex) == x) {
                    result.add(index);
                    break;
                }
            }
        }
        return result;
    }
}
```

- Traverse each word and check every character using `charAt()`.
- Use `break` after finding the character to avoid duplicate indices and unnecessary iterations.

**TC: O(n × m)**  
**SC: O(1)** auxiliary space.

### 2. Approach 2 – Using `indexOf()` (Improved)

```java
class Solution {
    public List<Integer> findWordsContaining(String[] words, char x) {
        List<Integer> result = new ArrayList<>();

        for (int index = 0; index < words.length; index++) {
            if (words[index].indexOf(x) != -1) {
                result.add(index);
            }
        }
        return result;
    }
}
```

**TC: O(n × m)**  
**SC: O(1)** auxiliary space.

Where:
- `n` = Number of words.
- `m` = Maximum word length.
- Output list requires O(n) space in the worst case.

### 3. Code Evolution

| Previous – `charAt()` | Improved – `indexOf()` |
|---|---|
| Manually iterate through characters | Built-in character search |
| Requires nested loops | Requires only one explicit loop |
| Uses `break` to stop searching | `indexOf()` handles searching internally |
| TC: O(n × m) | TC: O(n × m) |
| SC: O(1) | SC: O(1) |

### 4. Importance of `indexOf()`

- `indexOf(char)` returns the **first matching character's index**.
- Returns `-1` when the character is absent.
- Internally performs a linear search: **O(m)**.
- Also supports substring searches using `indexOf(String)`.
- Improves readability without changing asymptotic time complexity.

```java
"apple".indexOf('p');    // 1
"apple".indexOf('z');    // -1
"hello".indexOf("ll");  // 2
```

### 5. Key Takeaways

1. **Pattern:** Array Traversal + Character Searching.
2. **`charAt(index)`:** Accesses a character at a specific position in O(1).
3. **`indexOf(char)`:** Searches for a character in O(m).
4. **Early termination:** Use `break` when manually searching after finding a match.
5. **Code optimization:** Prefer built-in methods when they simplify the implementation without sacrificing complexity.

**Remember:** `charAt()` → Access by index | `indexOf()` → Search and return index.

# [771. Jewels and Stones](https://leetcode.com/problems/jewels-and-stones/)

**Difficulty:** Easy  
**Pattern:** Nested Loops → HashSet / Frequency Lookup

### 1. Approach 1 – Nested Loops (Previous)

```java
class Solution {
    public int numJewelsInStones(String jewels, String stones) {
        int totalJewels = 0;

        for (int i = 0; i < jewels.length(); i++) {
            char jewel = jewels.charAt(i);

            for (int j = 0; j < stones.length(); j++) {
                if (stones.charAt(j) == jewel) {
                    totalJewels++;
                }
            }
        }
        return totalJewels;
    }
}
```

**TC: O(n × m)** – For each jewel, scan all stones.  
**SC: O(1)** – No additional data structures.

Where `n` = jewels length and `m` = stones length.

### 2. Approach 2 – HashSet (Optimized)

```java
class Solution {
    public int numJewelsInStones(String jewels, String stones) {
        Set<Character> jewelSet = new HashSet<>();

        for (char jewel : jewels.toCharArray()) {
            jewelSet.add(jewel);
        }

        int totalJewels = 0;

        for (int i = 0; i < stones.length(); i++) {
            if (jewelSet.contains(stones.charAt(i))) {
                totalJewels++;
            }
        }

        return totalJewels;
    }
}
```

**TC: O(n + m)** – Build HashSet and scan stones.  
**SC: O(n)** – HashSet stores jewel characters.

*HashSet operations are O(1) on average.*

### 3. Code Evolution

| Previous – Nested Loops | Optimized – HashSet |
|---|---|
| Compare each jewel against every stone | Store jewels in a HashSet |
| Repeatedly scan stones | Scan stones only once |
| TC: O(n × m) | TC: O(n + m) average |
| SC: O(1) | SC: O(n) |

### 4. Pattern Recognition

- **Repeated membership checks** → Consider a HashSet.
- **Count matching elements** → Build a lookup structure and traverse the input.
- **Small, fixed character set** → A boolean array can also provide O(1) lookup with O(1) auxiliary space.

### 5. Key Takeaways

- Nested loops are valid but perform repeated comparisons.
- HashSet eliminates repeated scanning through average O(1) membership checks.
- `indexOf()` is useful for checking character existence, but calling it for every stone still requires O(n × m) time in the worst case.
- Java character comparison is case-sensitive (`'a' != 'A'`).
- **Important:** The inner loop runs O(m), not O(m²), so the original solution is O(n × m).

### One-Paragraph Takeaway

**Jewels and Stones** – Initially, I used two nested loops: the outer loop to iterate through each jewel and the inner loop to compare it against every stone, resulting in **TC: O(n × m) and SC: O(1)**. Later, I learned that repeated character membership checks can be optimized using a **HashSet**. By storing jewels in a HashSet and traversing stones only once, we can reduce the average time complexity to **O(n + m)** with **O(n) auxiliary space**. The key pattern is to replace repeated searches with efficient lookup using a HashSet.

# [3541. Find Most Frequent Vowel and Consonant](https://leetcode.com/problems/find-most-frequent-vowel-and-consonant/)

**Difficulty:** Easy  
**Pattern:** Frequency Counting + Fixed-Size Array

### 1. Approach 1 – HashMap (Previous)

```java
class Solution {
    public int maxFreqSum(String s) {
        int vowelMax = 0;
        int consonantMax = 0;

        Map<Character, Integer> frequency = new HashMap<>();

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            frequency.put(ch, frequency.getOrDefault(ch, 0) + 1);
        }

        for (var entry : frequency.entrySet()) {
            char ch = entry.getKey();

            if (ch == 'a' || ch == 'e' || ch == 'i' ||
                ch == 'o' || ch == 'u') {
                vowelMax = Math.max(vowelMax, entry.getValue());
            } else {
                consonantMax = Math.max(consonantMax, entry.getValue());
            }
        }

        return vowelMax + consonantMax;
    }
}
```

**TC:** O(n)  
**SC:** O(1) – At most 26 distinct lowercase characters.

### 2. Approach 2 – Frequency Array (Improved)

```java
class Solution {
    public int maxFreqSum(String s) {
        int vowelMax = 0;
        int consonantMax = 0;

        int[] frequency = new int[26];

        for (int i = 0; i < s.length(); i++) {
            frequency[s.charAt(i) - 'a']++;
        }

        for (int i = 0; i < frequency.length; i++) {
            char ch = (char) ('a' + i);

            if (ch == 'a' || ch == 'e' || ch == 'i' ||
                ch == 'o' || ch == 'u') {
                vowelMax = Math.max(vowelMax, frequency[i]);
            } else {
                consonantMax = Math.max(consonantMax, frequency[i]);
            }
        }

        return vowelMax + consonantMax;
    }
}
```

**TC:** O(n + 26) = **O(n)**  
**SC:** **O(1)** – Fixed array of 26 elements.

### 3. Code Evolution

| Previous – HashMap | Improved – Frequency Array |
|---|---|
| `Map<Character, Integer>` | `int[26]` |
| Hashing and map operations | Direct array indexing |
| Additional map entries/objects | Fixed-size primitive array |
| TC: O(n) average | TC: O(n) |
| SC: O(1) | SC: O(1) |

### 4. Pattern Recognition

- **Count character occurrences** → Frequency counting.
- **Only lowercase English letters** → Prefer `int[26]` over HashMap.
- **Find highest frequency** → Maintain maximum frequency for each category.
- **Convert array index to character** → `(char) ('a' + index)`.

### 5. Key Takeaways

**Find Most Frequent Vowel and Consonant** – Initially, I used a HashMap to calculate character frequencies and identify the maximum vowel and consonant occurrences. Later, I optimized it using a fixed-size `int[26]` array because the input contains only lowercase English letters. This avoids HashMap overhead and provides direct O(1) frequency updates. Both approaches have **TC: O(n) and SC: O(1)**, but the frequency array is more memory-efficient and generally faster.

# [1221. Split a String in Balanced Strings](https://leetcode.com/problems/split-a-string-in-balanced-strings/)

**Difficulty:** Easy  
**Pattern:** Greedy + Running Balance / Counter

### 1. Approach 1 – Two Counters (Previous)

```java
class Solution {
    public int balancedStringSplit(String s) {
        int result = 0;
        int l = 0;
        int r = 0;

        for (int index = 0; index < s.length(); index++) {
            char currentChar = s.charAt(index);

            if (currentChar == 'R') {
                r++;
            } else if (currentChar == 'L') {
                l++;
            }

            if (l == r) {
                result++;
                l = 0;
                r = 0;
            }
        }
        return result;
    }
}
```

**TC: O(n)** – Traverse the string once.  
**SC: O(1)** – Two counters.

### 2. Approach 2 – Single Balance Counter (Improved)

```java
class Solution {
    public int balancedStringSplit(String s) {
        int result = 0;
        int balance = 0;

        for (char currentChar : s.toCharArray()) {
            if (currentChar == 'R') {
                balance++;
            } else {
                balance--;
            }

            if (balance == 0) {
                result++;
            }
        }
        return result;
    }
}
```

**TC: O(n)** – Single traversal.  
**SC: O(n)** – `toCharArray()` creates an additional character array.

### 3. Code Evolution

| Previous – Two Counters | Improved – Single Counter |
|---|---|
| Maintain `l` and `r` separately | Maintain one `balance` |
| Check `l == r` | Check `balance == 0` |
| Reset both counters | No reset required |
| Indexed traversal using `charAt()` | Enhanced for-loop using `toCharArray()` |
| TC: O(n), SC: O(1) | TC: O(n), SC: O(n) |

**Further Optimization:** Use `charAt()` with the single `balance` counter to achieve **TC: O(n) and SC: O(1)**.

### 4. Pattern Recognition

- **Equal occurrences of two characters** → Consider a running balance.
- **Split into maximum valid substrings** → Greedily split whenever `balance == 0`.
- **Track the difference between two counts** → Use one counter instead of two.

**Why Greedy?** Splitting immediately when the balance becomes zero produces the maximum number of balanced substrings.

### 5. Key Takeaway

**Split a String in Balanced Strings** – Initially, I used two counters (`l` and `r`) to track the occurrences of each character, incrementing the result whenever both counts matched and resetting them afterward. Later, I realized that maintaining a single `balance` variable is sufficient: increment for `R`, decrement for `L`, and whenever `balance == 0`, a balanced substring is identified. This eliminates redundant variables and resets while keeping **TC: O(n)**. I also used `toCharArray()` for cleaner iteration, but it creates an additional array with **O(n) space**, whereas `charAt()` maintains **O(1) auxiliary space**. The key learning is to recognize the **Running Balance + Greedy** pattern and simplify multiple counters into a single variable.

# [49. Group Anagrams](https://leetcode.com/problems/group-anagrams/)

**Difficulty:** Medium  
**Pattern:** HashMap + Frequency Counting + Grouping by Signature

### 1. Approach 1 – Nested Loops + Frequency Comparison (Previous)

```java
class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {
        List<List<String>> endResult = new ArrayList<>();
        Set<Integer> processedIndexes = new HashSet<>();

        for (int index = 0; index < strs.length; index++) {
            if (processedIndexes.contains(index)) {
                continue;
            }

            List<String> interiumResult = new ArrayList<>();
            interiumResult.add(strs[index]);

            Map<Character, Integer> frequency = returnFrequencies(strs[index]);

            for (int compareIndex = index + 1; compareIndex < strs.length; compareIndex++) {
                if (processedIndexes.contains(compareIndex) ||
                    strs[index].length() != strs[compareIndex].length()) {
                    continue;
                }

                boolean allDone = true;

                for (char c : strs[compareIndex].toCharArray()) {
                    if (frequency.containsKey(c)) {
                        frequency.put(c, frequency.get(c) - 1);
                    } else {
                        allDone = false;
                        frequency = returnFrequencies(strs[index]);
                        break;
                    }
                }

                if (allDone && allProcessed(frequency)) {
                    interiumResult.add(strs[compareIndex]);
                    processedIndexes.add(compareIndex);
                    frequency = returnFrequencies(strs[index]);
                }
            }

            endResult.add(interiumResult);
            processedIndexes.add(index);
        }
        return endResult;
    }

    public boolean allProcessed(Map<Character, Integer> frequency) {
        for (Integer val : frequency.values()) {
            if (val != 0) {
                return false;
            }
        }
        return true;
    }

    public Map<Character, Integer> returnFrequencies(String word) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : word.toCharArray()) {
            frequency.put(c, frequency.getOrDefault(c, 0) + 1);
        }
        return frequency;
    }
}
```

**TC: O(n² × m)** average case  
**SC: O(n + m)** auxiliary space, excluding output.

**Issue:** Repeated comparisons, frequency reconstruction, and tracking processed indices.

**Important bug:** When two equal-length words are not anagrams, the frequency map may remain modified. It must be reset after every comparison, not only after a successful match or an unknown character.

### 2. Approach 2 – HashMap + Frequency Signature (Improved)

```java
class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {

        List<List<String>> endResult = new ArrayList<>();
        Map<String, List<String>> keyWithLists = new HashMap<>();

        for (int index = 0; index < strs.length; index++) {
            int[] frequency = new int[26];

            for (char c : strs[index].toCharArray()) {
                frequency[c - 'a']++;
            }

            StringBuilder key = new StringBuilder();

            for (int number : frequency) {
                key.append("#").append(number);
            }

            keyWithLists.computeIfAbsent(
                key.toString(), k -> new ArrayList<>()
            ).add(strs[index]);
        }

        for (var list : keyWithLists.values()) {
            endResult.add(list);
        }

        return endResult;
    }
}
```

**TC: O(n × m)**  
**SC: O(n)** auxiliary space, assuming a fixed 26-letter alphabet and bounded word length by problem constraints.

**Improvement:** Instead of comparing every pair of words, generate a frequency-based key and group matching words directly.

### 3. Approach 3 – Compact Frequency Signature (Final)

```java
class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {

        Map<String, List<String>> keyWithLists = new HashMap<>();

        for (int index = 0; index < strs.length; index++) {
            char[] frequency = new char[26];

            for (char c : strs[index].toCharArray()) {
                frequency[c - 'a']++;
            }

            keyWithLists.computeIfAbsent(
                new String(frequency), k -> new ArrayList<>()
            ).add(strs[index]);
        }

        return new ArrayList<>(keyWithLists.values());
    }
}
```

**TC: O(n × m)**  
**SC: O(n)** auxiliary space, with fixed-size keys.

**Improvement:**
- Replaced `StringBuilder` with `new String(frequency)`.
- Eliminated the additional result-list loop.
- Used `map.values()` directly to construct the final result.

**Note:** The `char[26]` approach is valid under LeetCode's constraints because character counts cannot exceed the `char` limit of 65,535. For unrestricted string lengths, use an `int[26]` frequency array with an unambiguous serialized key.

### 4. Code Evolution

| Approach | Technique | TC | Auxiliary SC |
|---|---|---|---|
| Previous | Nested loops + frequency comparison | O(n² × m) | O(n + m) |
| Improved | HashMap + `StringBuilder` signature | O(n × m) | O(n) |
| Final | HashMap + `char[26]` signature | O(n × m) | O(n) |

`n` = number of strings; `m` = maximum string length.

Complexities assume average O(1) HashMap operations and exclude the returned groups and their string references. The first approach additionally stores processed indices and temporary character data.

### 5. Pattern Recognition

- **Group elements sharing the same property** → HashMap grouping.
- **Anagrams contain identical character frequencies** → Frequency signature.
- **Lowercase English letters** → Fixed-size array of 26.
- **Repeated pairwise comparisons** → Generate a canonical key instead.
- **Map key to multiple values** → `computeIfAbsent()`.

### 6. Importance of `computeIfAbsent()`

```java
map.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
```

- If the key is absent, create a new list and associate it with the key.
- If the key exists, reuse its existing list.
- Eliminates separate `containsKey()` and `put()` checks.
- Average O(1) map lookup, excluding key construction.

### 7. Key Takeaway

**Group Anagrams** – Initially, I used nested loops to compare each word against the remaining words using character frequencies, requiring a HashSet to track processed indices. This resulted in **O(n² × m)** time complexity and introduced unnecessary frequency recalculations. Later, I recognized that anagrams share the same character frequency signature, allowing me to group them directly using a **HashMap<String, List<String>>**. I first generated keys using `StringBuilder` with frequency counts, then simplified the implementation using `new String(char[26])` and `computeIfAbsent()`. This reduced the average time complexity to **O(n × m)** while making the code cleaner. The key learning is to **replace repeated pairwise comparisons with a canonical signature and HashMap grouping**.

**Pattern to Remember:** `Generate Frequency Signature → Use as HashMap Key → Group Matching Strings`

# [921. Minimum Add to Make Parentheses Valid](https://leetcode.com/problems/minimum-add-to-make-parentheses-valid/)

**Difficulty:** Medium  
**Pattern:** Stack → Counter Optimization / Greedy

### 1. Approach 1 – Using Stack (Previous)

```java
class Solution {
    public int minAddToMakeValid(String s) {
        Deque<Character> stackForOpenings = new ArrayDeque<>();
        int closings = 0;

        for (char c : s.toCharArray()) {
            if (c == '(') {
                stackForOpenings.push(c);
            } else {
                if (!stackForOpenings.isEmpty()) {
                    char lastAdded = stackForOpenings.peek();

                    if (lastAdded == '(') {
                        stackForOpenings.pop();
                    } else {
                        closings++;
                    }
                } else {
                    closings++;
                }
            }
        }
        return stackForOpenings.size() + closings;
    }
}
```

**TC: O(n)** – Single traversal with O(1) stack operations.  
**SC: O(n)** – Stack and `toCharArray()` require additional space.

### 2. Approach 2 – Using Counters (Optimized)

```java
class Solution {
    public int minAddToMakeValid(String s) {
        int openings = 0;
        int closings = 0;

        for (char c : s.toCharArray()) {
            if (c == '(') {
                openings++;
            } else {
                if (openings > 0) {
                    openings--;
                } else {
                    closings++;
                }
            }
        }
        return openings + closings;
    }
}
```

**TC: O(n)** – Single traversal.  
**SC: O(n)** – Due to `toCharArray()`.

**Further optimization:** Using `charAt()` instead of `toCharArray()` reduces auxiliary space to **O(1)**.

### 3. Code Evolution

| Previous – Stack | Improved – Counters |
|---|---|
| Store opening brackets in a stack | Track unmatched openings using an integer |
| `push()` and `pop()` | Increment and decrement |
| Stack size = unmatched openings | `openings` = unmatched openings |
| `closings` tracks unmatched closing brackets | Same counter retained |
| TC: O(n), SC: O(n) | TC: O(n), SC: O(n) with `toCharArray()` |

**Key Improvement:** A stack is unnecessary because we only need the count of unmatched opening brackets, not their actual positions or values.

### 4. Pattern Recognition

- **Matching parentheses** → Initially consider Stack.
- **Only one type of bracket `()`** → A counter may replace the Stack.
- **Unmatched opening brackets** → Increment `openings`.
- **Closing bracket with available opening** → Decrement `openings`.
- **Closing bracket without available opening** → Increment `closings`.

**Important:** Simply counting the total number of `(` and `)` is insufficient because their order matters.

Example: `")("` requires 2 additions even though both counts are equal.

### 5. Key Takeaways

- Use a Stack when matching brackets requires tracking individual elements.
- For a single bracket type, a running counter can replace the Stack.
- `openings` tracks unmatched `(`, while `closings` tracks unmatched `)`.
- Final answer = `openings + closings`.
- Using `charAt()` avoids creating an additional character array.

### 6. One-Paragraph Takeaway

**Minimum Add to Make Parentheses Valid** – Initially, I used an `ArrayDeque` as a Stack to store opening brackets, popping whenever a matching closing bracket appeared and separately counting unmatched closings. Later, I realized that storing actual brackets was unnecessary because we only needed their counts. I replaced the Stack with two counters: `openings` to track unmatched opening brackets and `closings` to track unmatched closing brackets. Whenever a closing bracket appears, decrement `openings` if available; otherwise, increment `closings`. Finally, return their sum. Both approaches have **TC: O(n)**, but replacing the Stack with counters eliminates stack overhead. Using `charAt()` instead of `toCharArray()` further reduces auxiliary space to **O(1)**. The key learning is to recognize when a **Stack can be simplified into a running counter**.

**Pattern to Remember:** `Opening → Increment | Closing → Match or Count | Return Unmatched Total`

# 1781. Sum of Beauty of All Substrings

**Difficulty:** Medium  
**Pattern:** Substring Enumeration + Frequency Array

### 1. Pattern Recognition

- **All substrings** → Fix the starting index and expand the ending index using nested loops.
- **Character frequency** → Use `int[26]` for lowercase English letters.
- **Avoid repeated calculations** → Maintain running frequencies while expanding the substring.
- **Beauty** → Maximum frequency − Minimum non-zero frequency.

**Core Idea:** Fix Start → Expand End → Update Frequency → Calculate Beauty.

### 2. Approach 1 – Using `toCharArray()` (Previous)

```java
class Solution {
    public int beautySum(String s) {
        int sum = 0;
        char[] input = s.toCharArray();

        for (int outer = 0; outer < input.length - 1; outer++) {
            int[] frequency = new int[26];
            frequency[input[outer] - 'a']++;

            for (int inner = outer + 1; inner < input.length; inner++) {
                frequency[input[inner] - 'a']++;

                int max = 0;
                int min = Integer.MAX_VALUE;

                for (int itr : frequency) {
                    if (itr > 0) {
                        max = Math.max(itr, max);
                        min = Math.min(itr, min);
                    }
                }

                sum += max - min;
            }
        }
        return sum;
    }
}
```

**TC:** O(n²)  
**SC:** O(n) – Additional character array.

### 3. Approach 2 – Using `charAt()` (Improved)

```java
class Solution {
    public int beautySum(String s) {
        int sum = 0;

        for (int outer = 0; outer < s.length() - 1; outer++) {
            int[] frequency = new int[26];
            frequency[s.charAt(outer) - 'a']++;

            for (int inner = outer + 1; inner < s.length(); inner++) {
                frequency[s.charAt(inner) - 'a']++;

                int max = 0;
                int min = Integer.MAX_VALUE;

                for (int itr : frequency) {
                    if (itr > 0) {
                        max = Math.max(itr, max);
                        min = Math.min(itr, min);
                    }
                }

                sum += max - min;
            }
        }
        return sum;
    }
}
```

**TC:** O(n²) – Nested loops × constant 26-character scan.  
**SC:** O(1) – Fixed-size frequency array.

### 4. Code Evolution

| Previous Approach | Improved Approach |
|---|---|
| `s.toCharArray()` | `s.charAt(index)` |
| Creates an additional array | Direct character access |
| O(n) auxiliary space | O(1) auxiliary space |
| O(n²) time | O(n²) time |

**Why the improvement?**

- `String` is immutable, but `charAt()` allows O(1) read access.
- `toCharArray()` creates a separate array, which is unnecessary when we only need to read characters.
- Eliminated O(n) additional space without affecting time complexity.

### 5. Key Takeaways

1. **Substring enumeration:** Fix a starting index and expand the ending index.
2. **Running frequency:** Update only the newly added character instead of recalculating frequencies.
3. **Frequency array:** `frequency[s.charAt(i) - 'a']++` provides O(1) updates.
4. **Minimum frequency:** Ignore zero-frequency characters when calculating beauty.
5. **Loop optimization:** Start `inner = outer + 1` since single-character substrings have zero beauty.
6. **String immutability:** `charAt()` reads characters without modifying the original String.
7. **Space optimization:** Prefer `charAt()` over `toCharArray()` when an additional array isn't required.

**Pattern to Remember:** `Fix Start → Expand End → Maintain State → Calculate Result`
