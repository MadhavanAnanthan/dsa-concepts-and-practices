# Week 1: Arrays Basics & Big-O Complexity

## 1. Big-O Notation: The Language of Scalability
Big-O notation is the mathematical framework used to evaluate how an algorithm's resource consumption scales as the input size (N) grows from 10 to 10 million.
*   **Time Complexity:** Measures how the number of CPU operations increases as data grows.
*   **Space Complexity:** Measures how much *extra* memory (RAM) the algorithm requires as data grows.

Achieving optimal time and space complexity is what guarantees code-level (micro) scalability. Selecting the correct data structure is the most critical step in this process.

## 2. Array Fundamentals & Memory Architecture
An array is the most foundational data structure. It is a direct software wrapper over physical RAM.
*   **JVM Allocation:** In Java, arrays are allocated on the JVM Heap.
*   **Contiguous Storage:** Arrays demand a single, unbroken (contiguous) block of memory. The JVM can only create the array if it can find a free block large enough to hold the entire structure.
*   **The "Slots" of RAM:** RAM is composed of billions of 1-byte slots (e.g., 8GB RAM = ~8 billion bytes). Data is stored based on its type size. A primitive `int` takes exactly 4 consecutive bytes.

### Why is Array Access O(1)?
Array retrieval is instantaneous not just because it exists in RAM, but because the CPU does not need to search for the data. The array only stores the **Base Address** (the memory location of the very first index).

To find any other element, the CPU uses a simple mathematical formula to jump directly to the target:
$Address = BaseAddress + (index \times DataSize)$

## 3. Array Operations & Complexity

### Traversal
Traversal is the process of visiting every element in the array one by one.
*   **Time Complexity:** O(N) because you must visit all N elements.
*   **Space Complexity:** O(1) if you are only reading the values.
*   *Note on Performance:* Arrays are incredibly fast to traverse due to **CPU Cache Locality**. Because the memory is side-by-side, the CPU pulls chunks of the array into its ultra-fast L1/L2 cache, resulting in blazing-fast reads.

### Insertion and Deletion (The Shifting Tax)
Because an array is an unbroken block of memory, you cannot leave empty gaps in the middle of it.

*   **At the End:**
    *   **Time Complexity: O(1).** You can directly write to the next available slot or remove the last item without disturbing any other elements.
*   **At the Beginning / Middle:**
    *   **Time Complexity: O(N).** Inserting an element at index 0 requires physically shifting every subsequent element one slot to the right to make a gap. Deleting at index 0 requires shifting every element one slot to the left to close the gap.

### Complexity Summary Table

| Operation | Best Case Time | Worst Case Time | Space Complexity |
| :--- | :--- | :--- | :--- |
| **Access (Read/Write)** | O(1) | O(1) | O(1) |
| **Traversal** | O(N) | O(N) | O(1) |
| **Insert/Delete at End** | O(1) | O(1) | O(1) |
| **Insert/Delete at Index** | O(1) | O(N) | O(1) |