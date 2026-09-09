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
