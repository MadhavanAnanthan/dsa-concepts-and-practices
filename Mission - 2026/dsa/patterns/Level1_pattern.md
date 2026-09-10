# 392. Is Subsequence

## Pattern

**Same-Direction Two Pointers**

## Idea

* One pointer tracks characters in `s`.
* One pointer scans characters in `t`.
* If both characters match, move the `s` pointer.
* Always move the `t` pointer.
* If the `s` pointer reaches the end, `s` is a subsequence.

## Better Pseudocode

```text
sPointer = 0
tPointer = 0

while sPointer < s.length
      AND tPointer < t.length

    if s[sPointer] == t[tPointer]
        sPointer++

    tPointer++

return sPointer == s.length
```

## Earlier Approach

```text
matched = 0
tPointer = 0

for each character in s

    for remaining characters in t

        if current s character == current t character
            matched++
            tPointer++
            break

return matched == s.length
```

**Better approach:** use the first pseudocode because it is simpler and directly follows the same-direction two-pointer pattern.

## Remember

> `sPointer` = character I am waiting for
> `tPointer` = character I am currently checking

**Time:** O(t.length)
**Space:** O(1)

## Valid Palindrome

Pattern: Opposite-End Two Pointers

### Idea

* Start one pointer from the left and another from the right.
* Skip characters that are not letters or digits.
* Compare the valid characters from both ends.
* Convert characters using `Character.toLowerCase()` before comparing.
* If characters are different → return `false`.
* Otherwise move both pointers inward.

### Better Pseudocode

```text
left = 0
right = s.length - 1

while left < right

    while left < right AND s[left] is not letter/digit
        left++

    while left < right AND s[right] is not letter/digit
        right--

    if lowercase(s[left]) != lowercase(s[right])
        return false

    left++
    right--

return true
```

### Cleaner Java Comparison

Instead of manually checking ASCII values:

```text
'A' to 'Z'
then add 32
```

use:

```java
Character.toLowerCase(s.charAt(left))
Character.toLowerCase(s.charAt(right))
```

Also this check:

```java
!Character.isLetterOrDigit(s.charAt(left)) || s.charAt(left) == ' '
```

can simply be:

```java
!Character.isLetterOrDigit(s.charAt(left))
```

because a space is already not a letter or digit.

### Complexity

Time: `O(n)`
Space: `O(1)`

### Remember

Palindrome means comparing characters from both ends.

### Pattern Hint

Need to compare beginning and end repeatedly
→ Opposite-end two pointers
→ Skip invalid characters
→ Compare lowercase valid characters
→ Move both pointers inward

## Longest Common Prefix

Pattern: `String Traversal`

Technique: `Horizontal Scanning / Vertical Scanning for prefix comparison`

### Initial Approach

* Compare the current prefix with the next string.
* Build the matched characters using `StringBuilder`.
* This works, but `StringBuilder` uses extra space to store the prefix again.

Time: `O(S)`
Space: up to `O(L)` for the builder

`S` = total characters checked
`L` = current prefix length

---

## Horizontal Scanning

Compare **string by string**.

* Start with the first string as the prefix.
* Compare it with the next string.
* Find how many starting characters match.
* Shorten the prefix using `substring(0, index)`.
* Continue with the next string.
* If prefix becomes empty → return `""`.

### Pseudocode

```text
prefix = first string

for each remaining string

    index = 0

    while index is valid
          AND prefix[index] == currentString[index]

        index++

    prefix = prefix.substring(0, index)

    if prefix is empty
        return ""

return prefix
```

Time: `O(S)`
Extra space: `O(1)` logic, apart from new substring objects

### Remember

Horizontal scanning
→ compare **string by string**
→ keep reducing the common prefix

---

## Vertical Scanning

Compare the **same character position across all strings**.

Example:

```text
flower
flow
flight

index 0 → f f f ✓
index 1 → l l l ✓
index 2 → o o i ✗
```

* Take each character from the first string.
* Check the same index in every other string.
* If any string ends or the character is different → return prefix up to that index.

### Pseudocode

```text
for each index i in first string

    currentChar = firstString[i]

    for each remaining string

        if i is outside current string
           OR currentString[i] != currentChar

            return firstString.substring(0, i)

return first string
```

Time: `O(S)`
Extra space: `O(1)` logic

### Remember

Horizontal scanning
→ compare **string by string**

Vertical scanning
→ compare **same index across all strings**
