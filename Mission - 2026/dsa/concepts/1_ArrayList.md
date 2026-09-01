# ArrayList – DSA Notes

## 1. What is an ArrayList?

`ArrayList` is a **resizable array** implementation in Java.

Unlike a normal array, whose size is fixed after creation, an `ArrayList` can grow and shrink dynamically.

```java
import java.util.ArrayList;

ArrayList<Integer> list = new ArrayList<>();
```

Conceptually:

```text
ArrayList
   |
   v
[10][20][30][ ][ ]
```

Internally, `ArrayList` uses an array.

---

## 2. Array vs ArrayList

| Feature | Array | ArrayList |
|---|---|---|
| Size | Fixed | Dynamic |
| Stores primitives directly | Yes | No |
| Stores objects | Yes | Yes |
| Generic support | No | Yes |
| Built-in methods | Very limited | Many |
| Random access | O(1) | O(1) |

Example:

```java
int[] arr = new int[5];

ArrayList<Integer> list = new ArrayList<>();
```

`ArrayList` cannot directly store primitive types.

```java
ArrayList<int> list; // Invalid
```

Use wrapper classes:

```java
ArrayList<Integer> list = new ArrayList<>();
```

Java performs autoboxing:

```java
list.add(10);
```

Conceptually:

```java
int 10
   |
Autoboxing
   |
   v
Integer object
```

---

## 3. Internal Structure

Internally, an `ArrayList` stores elements in an object array.

Conceptually:

```text
ArrayList object
      |
      v
Object[] elementData

[ref1][ref2][ref3][null][null]
   |     |     |
   v     v     v
  10    20    30
```

The internal array may have more capacity than the current number of elements.

---

## 4. Size vs Capacity

### Size

The number of elements currently stored.

```java
list.size();
```

### Capacity

The number of elements the internal array can currently hold before resizing is required.

Example:

```text
Size = 3
Capacity = 5

[10][20][30][ ][ ]
```

The public `ArrayList` API exposes size, but not its internal capacity directly.

---

## 5. Dynamic Resizing

When the internal array becomes full, `ArrayList` creates a larger array and copies the elements.

Conceptually:

```text
Old array

[10][20][30]
```

Need to insert `40`.

```text
Create larger array

[ ][ ][ ][ ][ ]
```

Copy:

```text
[10][20][30][ ][ ]
```

Insert:

```text
[10][20][30][40][ ]
```

This resizing operation costs:

```text
O(n)
```

because existing elements must be copied.

However, resizing does not happen on every insertion.

Therefore, appending is considered:

```text
Amortized O(1)
```

---

## 6. Important Operations

### Add at End

```java
list.add(10);
```

Average / amortized complexity:

```text
O(1)
```

Worst case during resizing:

```text
O(n)
```

---

### Access by Index

```java
list.get(2);
```

Complexity:

```text
O(1)
```

because `ArrayList` internally uses an array.

---

### Update by Index

```java
list.set(2, 100);
```

Complexity:

```text
O(1)
```

---

### Insert at Arbitrary Index

```java
list.add(2, 50);
```

Elements after the index must shift to the right.

Example:

```text
Before:

[10][20][30][40]

Insert 25 at index 2

[10][20][25][30][40]
```

Complexity:

```text
O(n)
```

---

### Remove from End

```java
list.remove(list.size() - 1);
```

Complexity:

```text
O(1)
```

---

### Remove from Arbitrary Index

```java
list.remove(1);
```

Elements after the removed element must shift left.

```text
Before:

[10][20][30][40]

Remove index 1

[10][30][40]
```

Complexity:

```text
O(n)
```

---

### Search

```java
list.contains(30);
list.indexOf(30);
```

The list may need to scan elements one by one.

Complexity:

```text
O(n)
```

---

## 7. Time Complexity Summary

| Operation | Time Complexity |
|---|---:|
| Access by index | O(1) |
| Update by index | O(1) |
| Add at end | Amortized O(1) |
| Insert at beginning | O(n) |
| Insert at middle | O(n) |
| Remove from end | O(1) |
| Remove from beginning | O(n) |
| Remove from middle | O(n) |
| Search by value | O(n) |
| Contains | O(n) |

---

## 8. Why Insertion and Deletion Can Be Expensive

`ArrayList` maintains elements in index order.

If an element is inserted or removed in the middle, later elements must move.

Example:

```text
Index:  0   1   2   3
       [A] [B] [C] [D]
```

Insert `X` at index `1`:

```text
       [A] [X] [B] [C] [D]
```

`B`, `C`, and `D` are shifted right.

That is why insertion can become:

```text
O(n)
```

---

## 9. Iterating an ArrayList

### Using for loop

```java
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}
```

### Enhanced for loop

```java
for (Integer value : list) {
    System.out.println(value);
}
```

### Iterator

```java
Iterator<Integer> iterator = list.iterator();

while (iterator.hasNext()) {
    System.out.println(iterator.next());
}
```

Traversal complexity:

```text
O(n)
```

---

## 10. ArrayList and Memory

An `ArrayList` may reserve unused capacity.

Example:

```text
size = 3
capacity = 8

[10][20][30][ ][ ][ ][ ][ ]
```

Therefore, it can use some extra memory to make future additions efficient.

---

## 11. When to Use ArrayList

Prefer `ArrayList` when:

- You need frequent random access by index.
- Most insertions happen at the end.
- You perform more reads than insertions/deletions in the middle.
- Cache-friendly contiguous-array storage is beneficial.

Typical DSA use cases:

- Dynamic arrays
- Storing traversal results
- Adjacency lists
- Prefix/suffix values
- Intermediate results
- Sliding-window storage when deque behavior is not required

---

## 12. When ArrayList Is Not Ideal

Avoid relying on `ArrayList` when you frequently:

- Insert at the beginning
- Delete from the beginning
- Insert/delete repeatedly in the middle

These operations require shifting elements.

---

## 13. Important Interview Point

`ArrayList` gives:

```text
Fast index access
        +
Dynamic size
```

but pays for it with:

```text
Expensive middle insertion/deletion
        +
Occasional resize and copy
```

---

## 14. DSA Mental Model

Think of `ArrayList` as:

```text
Dynamic Array
```

Its main strengths are:

```text
Index-based access → O(1)
Append → Amortized O(1)
```

Its main weakness is:

```text
Insertion / deletion in middle → O(n)
```
