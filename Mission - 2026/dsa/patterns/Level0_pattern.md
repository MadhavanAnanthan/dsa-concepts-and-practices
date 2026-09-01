## Pair Sum / Two Sum → Hashing / Complement Pattern

### Brute Force
- Check every pair using 2 loops.
- Time: O(n²)
- Space: O(1)

### Optimal - HashSet
- Traverse array once.
- For each current value:

Calculating based on target, so its comes under complement pattern.
  needed = target - currentValue

- Check whether `needed` was already seen.
- If yes → pair found.
- Otherwise store `currentValue` in Set.

Time: O(n) average
Space: O(n)

### Pattern Hint
Need pair whose sum = target
→ Calculate complement
→ `needed = target - current`
→ Fast lookup using HashSet/HashMap

## Find Maximum in Array

Pattern: Linear Traversal / Running Maximum

Idea:
- Start `max = first element`
- Traverse remaining elements
- If current > max, update max

Time: O(n)
Space: O(1)

Avoid:
Sorting → O(n log n)

## Check if Array is Sorted

Pattern: Traversal / Adjacent Comparison

- Compare `arr[i-1]` with `arr[i]`.
- If previous > current → unsorted.
- Otherwise continue.

Time: O(n)
Space: O(1)

Already optimal.

## Second Largest Element

Pattern: Traversal / Track Top Two

- Start `max` and `secondMax` with `Integer.MIN_VALUE`.
- If `current > max` → `secondMax = max`, then update `max`.
- Else if `secondMax < current < max` → update `secondMax`.

Time: O(n)
Space: O(1)

Already optimal.