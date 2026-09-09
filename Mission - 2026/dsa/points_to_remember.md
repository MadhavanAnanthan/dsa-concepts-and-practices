1. Extra space: even with multiple structures, if all scale with the same size n → O(n); if sizes are independent → O(n + m).

2. in 2 pointer, same direction pattern is very broader, slow/fast and read/write pointer are also same direction specific pointer pattern. opposite ends are completely different pattern.
## Hashing - Points to Remember

- Need to find duplicates → `HashSet`
- Need only unique elements → `HashSet`
- Need frequency/count → `HashMap<value, count>`
- Need value + index → `HashMap<value, index>`
- Need fast existence/lookup → Hashing
- Need common elements between arrays → `HashSet`
- Need pair/target lookup → often `HashMap` / `HashSet`

Typical time: O(n) average  
Typical extra space: O(n)

Two Sum
→ unsorted
→ HashMap
→ O(n) extra space

Two Sum II
→ sorted
→ constant extra space
→ opposite-end two pointers
→ O(1) space