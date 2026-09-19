# DSA - Points to Remember

These are decision rules, not universal laws. Confirm constraints before selecting an approach.

## Complexity

- If several auxiliary structures all scale with the same input `n`, constants are removed: `O(n) + O(n) = O(n)` space. Independent inputs remain `O(n + m)`.
- Sorting is not automatically a bottleneck. Comparison sorting is generally `O(n log n)` and is often the cleanest solution. Avoid it only when a linear or counting-based solution is required by the constraints.
- `Arrays.sort` uses different optimized algorithms by type/JDK implementation. Interview answers should focus on complexity and constraints rather than memorizing one internal algorithm.
- Watch integer overflow in formulas. Compute `n * (n + 1) / 2` with `long` when `n` can be large.

## Arrays

- Java arrays have a fixed length and one component type. Instance/static numeric arrays are initialized to zero; local array variables themselves must be initialized before use.
- There is no portable rule that a local array may contain `10^6` elements and a global array `10^7`. Limits depend on element size, heap, thread-stack size, and whether the array is local as a **reference** or allocated as an object on the heap.
- Normalize circular rotation by `k = ((k % n) + n) % n` when `n > 0`. This handles `k >= n` and negative rotation values.
- For a right rotation, reversal can produce `O(n)` time and `O(1)` extra space; repeated one-step rotations are usually unnecessarily expensive.

## Hashing

- Find duplicates or maintain unique elements -> `HashSet`.
- Maintain frequency/count -> `HashMap<value, count>`.
- Maintain value-to-index lookup -> `HashMap<value, index>`.
- Find common elements or a complement/target -> often `HashSet` or `HashMap`.
- Expected lookup is `O(1)`, but worst-case and memory cost still matter. Good `equals`/`hashCode` implementations and immutable keys are essential.
- `contains` is not always linear: `ArrayList.contains` is `O(n)`, `HashSet.contains` is expected `O(1)`, and `TreeSet.contains` is `O(log n)`.

Typical one-pass hashing solution: `O(n)` expected time and `O(n)` extra space.

### Two Sum

- Unsorted input: one-pass `HashMap`, expected `O(n)` time and `O(n)` space.
- Sorted input: opposite-end two pointers, `O(n)` time and `O(1)` extra space.

## Two-pointer patterns

- **Opposite ends:** sorted pair search, palindrome checks, partition-like problems.
- **Same direction:** read/write compaction, removing duplicates, sliding-window boundaries.
- **Slow/fast:** cycle detection or finding a middle element.

They share pointer movement, but the invariant and applicability differ. State the invariant before coding.

## Strings and characters

- `String` is immutable; operations produce another string. Use `char[]` only when in-place character mutation materially helps.
- Use `StringBuilder` for repeated concatenation within one thread. Use `StringBuffer` only when its synchronized operations are actually required.
- `" "` versus `"\t"` is a formatting decision, not a meaningful memory optimization.
- A direct character predicate such as `Character.isLetterOrDigit` is clearer for a single-character check. Regex is valuable for patterns; compile and reuse a `Pattern` in hot code instead of rejecting regex categorically.

## Numbers

- Use `int`/`long` for integral counts and identifiers within range.
- Use `BigInteger` for arbitrary-size integral arithmetic.
- Use `BigDecimal` for decimal values requiring defined precision/rounding, especially money; construct it from strings or `BigDecimal.valueOf`, not surprising binary floating-point values.
- Money also needs a currency and explicit rounding rule.
