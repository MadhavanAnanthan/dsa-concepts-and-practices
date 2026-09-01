# Week 1: Arrays Basics & Big-O Complexity

## 1. Big-O Notation: The Language of Scalability

Big-O notation is a mathematical way to describe how an algorithm's resource consumption grows as the input size `N` increases.

For example, we are interested in understanding how an algorithm behaves when the input grows from:

```text
10
100
1,000
1,000,000
10,000,000
```

Big-O focuses mainly on the **growth rate**, rather than the exact execution time or exact number of CPU instructions.

* **Time Complexity:** Describes how the amount of work performed by an algorithm grows as the input size `N` increases.
* **Space Complexity:** Describes how much **additional memory** the algorithm requires as the input size `N` increases.

Common complexities:

```text
O(1)        Constant
O(log N)    Logarithmic
O(N)        Linear
O(N log N)  Linearithmic
O(N²)       Quadratic
```

Choosing the correct data structure and algorithm is an important part of writing scalable and efficient software.

---

## 2. Array Fundamentals & Memory Architecture

An array is one of the most fundamental data structures.

It stores multiple elements of the **same type** and allows us to access each element using an index.

Example:

```java
int[] numbers = new int[10];
```

This creates an array with:

```text
Length       = 10
Valid indexes = 0 to 9
Element type = int
```

Since `int` is a primitive type, every position initially contains its default value:

```text
0
```

So internally, we can think of it as:

```text
Index:  0 1 2 3 4 5 6 7 8 9
Value:  0 0 0 0 0 0 0 0 0 0
```

---

## 3. JVM Allocation

In Java, arrays are **objects**.

An array object is normally allocated on the **JVM Heap**.

Example:

```java
int[] numbers = new int[10];
```

Conceptually:

```text
Thread Stack                     JVM Heap

numbers
   │
   │ reference
   ▼
                              int[] array object
                              ┌───┬───┬───┬───┬───┐
                              │ 0 │ 0 │ 0 │ 0 │...│
                              └───┴───┴───┴───┴───┘
                               0   1   2   3
```

The variable:

```java
numbers
```

contains a reference to the array object.

The array object also contains JVM-managed information such as:

* Array type
* Array length
* Object metadata

Java does not directly expose the actual physical memory address of the array to our program.

---

## 4. Contiguous / Sequential Storage

Conceptually, array elements are stored sequentially in memory.

This allows the JVM and CPU to locate an element directly using its index instead of searching through all previous elements.

For a primitive:

```java
int
```

the primitive value itself requires:

```text
4 bytes
```

A useful conceptual formula is:

```text
Element Address
      =
Base Address + (Index × Element Size)
```

For example:

```text
int[] arr
```

and accessing:

```java
arr[5]
```

can conceptually be understood as:

```text
Address =
Base Address + (5 × 4 bytes)
```

However, this is only a **conceptual model**.

In a real JVM, additional details exist such as:

```text
Object header
Array length
Alignment
GC-related information
Compressed references
JVM implementation details
```

Java developers normally do not work with these addresses directly.

---

## 5. Why is Array Access O(1)?

Suppose:

```java
int value = arr[7];
```

The JVM does not search like this:

```text
arr[0]
   ↓
arr[1]
   ↓
arr[2]
   ↓
arr[3]
   ↓
...
arr[7]
```

Instead, the position can be calculated directly using the index.

Conceptually:

```text
Base Address
      +
(index × element size)
```

Therefore:

```java
arr[0];
arr[5];
arr[500];
arr[500000];
```

all require approximately the same amount of work to locate the element.

Therefore:

```text
Array Index Access = O(1)
```

Both reading and updating by index are O(1).

Example:

```java
int x = arr[5];   // O(1)

arr[5] = 100;     // O(1)
```

---

# 6. Array Length is Fixed

This is one of the most important properties of a Java array.

Once an array is created:

```java
int[] arr = new int[10];
```

its length permanently remains:

```text
10
```

You cannot increase it to:

```text
11
```

and you cannot reduce it to:

```text
9
```

The following value will always remain:

```java
arr.length == 10
```

until that array object itself becomes unreachable and is eventually garbage collected.

A Java array does not provide methods such as:

```java
arr.add(...)
arr.remove(...)
```

If we need a larger array, we must create a **new array**.

---

# 7. An Array Never Automatically Shifts Elements

This is extremely important.

A raw Java array does **not** understand concepts such as:

```text
empty position
deleted position
gap
active element
inactive element
```

Every index always exists.

For example:

```java
int[] arr = {10, 20, 30, 40, 50};

arr[2] = 0;
```

Now the array becomes:

```text
Index:  0   1   2   3   4
Value: 10  20   0  40  50
```

Java does NOT automatically convert it into:

```text
10 20 40 50 0
```

because the value:

```text
0
```

is simply another valid `int` value.

Similarly, for an object array:

```java
String[] names = {"A", "B", "C", "D"};

names[1] = null;
```

the result is:

```text
["A", null, "C", "D"]
```

Nothing automatically shifts.

---

# 8. Physical Length vs Logical Size

When learning Data Structures and Algorithms, we often use an array to implement a list-like structure.

In that case, we usually maintain a separate variable called:

```text
size
```

Example:

```java
int[] arr = new int[10];

int size = 5;
```

Suppose:

```text
Index:

0   1   2   3   4   5   6   7   8   9
---------------------------------------
10  20  30  40  50  0   0   0   0   0
```

Then:

```text
Physical array length = 10

Logical size = 5
```

Meaning:

```text
Indexes 0-4
```

currently contain meaningful elements in our logical data structure.

Indexes:

```text
5-9
```

are currently unused.

The JVM itself does not know that they are "unused."

This concept is maintained by our program.

---

# 9. Traversal

Traversal means visiting every relevant element one by one.

Example:

```java
for (int i = 0; i < arr.length; i++) {
    System.out.println(arr[i]);
}
```

If the array contains:

```text
N elements
```

we need to visit all `N` elements.

Therefore:

```text
Time Complexity = O(N)
```

If we do not create any additional data structure proportional to `N`:

```text
Extra Space Complexity = O(1)
```

---

# 10. Why Arrays Are Fast to Traverse: CPU Cache Locality

Arrays are generally very fast to traverse sequentially.

This is partly because array elements are stored close together.

Modern CPUs do not normally fetch only one tiny value from RAM.

Instead, memory is transferred into CPU caches in blocks called:

```text
cache lines
```

Suppose the CPU accesses:

```text
arr[0]
```

Nearby elements such as:

```text
arr[1]
arr[2]
arr[3]
```

may also be loaded into the CPU cache.

Therefore, when the program accesses the next elements, they may already exist in the much faster CPU cache.

This behavior is known as:

```text
Spatial Locality
```

or:

```text
Cache Locality
```

Conceptually:

```text
RAM

arr[0] arr[1] arr[2] arr[3] arr[4] arr[5]
   │
   │ CPU fetches a nearby block
   ▼
CPU Cache

arr[0] arr[1] arr[2] arr[3]
```

This is one reason arrays are extremely efficient for sequential traversal.

---

# 11. Insertion into an Array

A raw Java array does not actually have an:

```text
insert()
```

operation.

When DSA discussions say:

> "Insert an element into an array"

they normally mean:

> We are maintaining a logical list inside an array and manually moving elements when required.

---

## Insert at the Logical End

Suppose:

```java
int[] arr = new int[10];

int size = 4;
```

and the array currently represents:

```text
10 20 30 40 _ _ _ _ _ _
            ↑
           size
```

We can insert:

```text
50
```

at the logical end:

```java
arr[size] = 50;

size++;
```

Result:

```text
10 20 30 40 50 _ _ _ _ _
```

No existing elements have to move.

Therefore:

```text
Time Complexity = O(1)
```

provided the array still has free capacity.

---

# 12. Insertion at the Beginning

Suppose the logical elements are:

```text
10 20 30 40
```

and we want to insert:

```text
5
```

at index:

```text
0
```

We cannot simply overwrite:

```text
10
```

because we would lose that value.

Therefore, our program must manually shift the existing values toward the right.

Before:

```text
Index:

0   1   2   3   4
------------------
10  20  30  40  _
```

Shift from right to left:

```text
arr[4] = arr[3]
arr[3] = arr[2]
arr[2] = arr[1]
arr[1] = arr[0]
```

Now:

```text
_ 10 20 30 40
```

Then:

```java
arr[0] = 5;
```

Result:

```text
5 10 20 30 40
```

If there are `N` elements, we might need to shift approximately `N` elements.

Therefore:

```text
Worst-Case Time Complexity = O(N)
```

The array itself did not perform this shift.

**Our code performed the shift.**

---

# 13. Insertion in the Middle

Suppose:

```text
10 20 30 40 50
```

and we want to insert:

```text
25
```

at index:

```text
2
```

We must move:

```text
30
40
50
```

one position toward the right.

Conceptually:

```text
Before

10 20 30 40 50 _
      ↑

Shift

10 20 _ 30 40 50

Insert

10 20 25 30 40 50
```

Again, the array does not shift by itself.

The program performs the shifting.

Worst case:

```text
O(N)
```

---

# 14. Deletion from an Array

A raw Java array also does not have a real:

```text
delete()
```

operation.

Setting an element to:

```text
0
```

or:

```text
null
```

does not reduce the array length.

Example:

```java
int[] arr = {10, 20, 30, 40, 50};

arr[2] = 0;
```

produces:

```text
10 20 0 40 50
```

Length is still:

```text
5
```

Nothing moves.

---

# 15. Logical Deletion with Shifting

When DSA says:

> Delete an element from an array

it often means that we want our **logical elements to remain packed together**.

Suppose:

```text
10 20 30 40 50 _ _ _
```

and:

```text
logical size = 5
```

Delete:

```text
30
```

at index:

```text
2
```

Our code can shift:

```text
40 → index 2
50 → index 3
```

Result:

```text
10 20 40 50 50 _ _ _
```

Now index `4` still contains the old value:

```text
50
```

but it is no longer considered part of our logical data.

We may clear it:

```java
arr[size - 1] = 0;
```

Result:

```text
10 20 40 50 0 _ _ _
```

Then:

```java
size--;
```

Now:

```text
Physical array length = 8

Logical size = 4
```

The actual array length did not decrease.

---

# 16. Delete at the End

Suppose:

```text
10 20 30 40 50 _ _ _
```

with:

```text
size = 5
```

If we logically remove:

```text
50
```

we do not need to move any other element.

We can simply do:

```java
size--;
```

Optionally:

```java
arr[size] = 0;
```

Now:

```text
10 20 30 40 0 _ _ _
```

Therefore:

```text
Delete Last Logical Element = O(1)
```

---

# 17. Delete at the Beginning

Suppose:

```text
10 20 30 40 50
```

and we want to delete:

```text
10
```

To keep the logical elements packed together, our code must shift:

```text
20 → index 0
30 → index 1
40 → index 2
50 → index 3
```

Result:

```text
20 30 40 50 0
```

Approximately `N` elements may need to move.

Therefore:

```text
Worst-Case Time Complexity = O(N)
```

Again:

**Java array did not automatically shift the values.**

Our logical deletion implementation performed the shifting.

---

# 18. Why Do We Shift at All?

Consider:

```text
10 20 30 40 50
```

If we "remove" `30` simply by writing:

```text
0
```

we get:

```text
10 20 0 40 50
```

This is completely valid for a Java array.

However, sometimes our application wants the data to behave like a list:

```text
10
20
40
50
```

with no unused slot between active elements.

In that situation, **our data structure implementation chooses to shift elements**.

Therefore:

```text
Array itself
    ↓
does NOT require shifting


Logical list implemented using array
    ↓
may require shifting


ArrayList
    ↓
performs shifting internally for us
```

---

# 19. Java Array vs ArrayList

This distinction is extremely important.

## Raw Java Array

```java
int[] arr = new int[10];
```

Characteristics:

```text
Fixed length

No add()

No remove()

Does not automatically shift

Does not automatically resize

Direct indexed access
```

---

## ArrayList

Example:

```java
List<Integer> list = new ArrayList<>();

list.add(10);
list.add(20);
list.add(30);
list.add(40);
```

Internally, `ArrayList` uses an array-like backing structure.

But it manages:

```text
Logical size

Capacity

Resizing

Insertion

Deletion

Shifting
```

for us.

Example:

```java
list.remove(1);
```

Before:

```text
10 20 30 40
```

After:

```text
10 30 40
```

`ArrayList` performs the necessary internal shifting.

Therefore:

```text
Raw Array
   ↓
You manage shifting


ArrayList
   ↓
Java library manages shifting internally
```

---

# 20. Array Operations & Complexity

Assume we are using an array with a separately maintained logical size.

| Operation                                                                 | Best Case Time | Worst Case Time | Extra Space |
|:--------------------------------------------------------------------------| :------------: | :-------------: | :---------: |
| **Access by Index**                                                       |      O(1)      |       O(1)      |     O(1)    |
| **Update by Index**                                                       |      O(1)      |       O(1)      |     O(1)    |
| **Traversal**                                                             |      O(N)      |       O(N)      |     O(1)    |
| **Search in Unsorted Array**                                              |      O(1)      |       O(N)      |     O(1)    |
| **Insert at Logical End with Free Capacity**                              |      O(1)      |       O(1)      |     O(1)    |
| **Insert at Arbitrary Index(Arbitrary index means any index you choose)** |      O(1)      |       O(N)      |     O(1)    |
| **Delete Last Logical Element**                                           |      O(1)      |       O(1)      |     O(1)    |
| **Delete at Arbitrary Index while preserving order**                      |      O(1)      |       O(N)      |     O(1)    |

---

# 21. Final Mental Model

Keep this mental model:

```text
int[] arr = new int[10];

Physical array
─────────────────────────────────────────

Index:
0   1   2   3   4   5   6   7   8   9

Length = 10 forever
```

Java itself only understands:

```text
There are 10 indexes.
Each index contains a value.
```

Java does NOT understand:

```text
"This element was deleted."

"This slot is empty."

"Shift everything left."

"Only five elements are active."
```

Those are concepts introduced by:

```text
our program
        or
a higher-level data structure
```

such as:

```text
ArrayList
```

Therefore:

```text
Raw Java Array
      │
      ├── Fixed length
      │
      ├── O(1) indexed access
      │
      ├── No automatic shifting
      │
      ├── No automatic deletion
      │
      └── No automatic resizing
              │
              ▼
If we want list behavior
              │
              ▼
We maintain:
    size
    insertion logic
    deletion logic
    shifting logic
              │
              ▼
Or simply use ArrayList
```
## Arrays in Java – Memory Basics

1. **Arrays are considered objects in Java.**

2. **Arrays of primitive types store the actual values inside the array object in the Heap.**

```java
int[] a = {1, 2, 3};
```

```text
Stack                Heap

a --------->       [1][2][3]
```

- `a` stores the reference.
- The actual primitive values are stored inside the array in the Heap.

3. **Arrays of objects store references inside the array, while the actual objects are stored separately in the Heap.**

```java
String[] str = {"A", "B"};
```

```text
Stack                Heap

str --------->     [ref1][ref2]
                     |     |
                     v     v
                    "A"   "B"
```

- `str` stores the reference to the array.
- The array contains references to the `String` objects.
- The actual `String` objects are stored separately in the Heap.
