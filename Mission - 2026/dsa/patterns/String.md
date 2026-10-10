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
