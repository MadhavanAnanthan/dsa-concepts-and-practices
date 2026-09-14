# Java Notes: Immutability, Strings, Wrappers, Records & Related Concepts

# Immutability

## What is Immutability?

An immutable object's state cannot be changed after it is created.

```java
String s = "Hello";
s = s + " World";
```

The original `"Hello"` object is not modified.

Instead:

1. A new String object is created.
2. `s` starts pointing to the new object.
3. The old object remains unchanged.

---

## Benefits

- Thread-safe by design
- Safe to share across threads
- Predictable behavior
- Easier debugging
- Useful for caching and collections
- Eliminates accidental state changes

---

## Safety Notes

### Immutability Protects State, Not References

```java
String status = "STARTED";
status = "COMPLETED";
```

Allowed.

The String object is immutable, but the reference variable can still change.

Use `final` if the reference should never change.

```java
final String status = "STARTED";
```

---

### Immutable Does Not Mean Thread Coordination

Immutability prevents object modification.

It does not solve:

- Race conditions
- Synchronization
- Atomic updates

Threads can still overwrite shared references.

---

# Creating Immutable Classes

## Rules

### 1. Make Class Final

Prevents inheritance.

```java
public final class Person {
}
```

---

### 2. Make Fields Private Final

```java
private final String name;
private final int age;
```

---

### 3. Initialize Through Constructor

```java
public Person(String name, int age) {
    this.name = name;
    this.age = age;
}
```

---

### 4. Do Not Provide Setters

Good:

```java
public String getName() {
    return name;
}
```

Bad:

```java
public void setName(String name) {
    this.name = name;
}
```

---

### 5. Protect Mutable Fields

Bad:

```java
private final List<String> skills;

public List<String> getSkills() {
    return skills;
}
```

Caller can modify the list.

---

Good:

```java
return List.copyOf(skills);
```

or

```java
return Collections.unmodifiableList(skills);
```

---

## Example

```java
public final class Person {

    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

---

## Safety Notes

### final Class != Immutable Class

This is NOT immutable:

```java
final class Person {
    String name;
}
```

Because fields can still change.

---

### final Reference != Immutable Object

```java
final List<String> list = new ArrayList<>();
list.add("Java");
```

Allowed.

`final` prevents reassignment.

It does not prevent object modification.

---

# String Immutability

## Why String Is Immutable?

- Internal value is final.
- Existing Strings are never modified.
- Every modification creates a new String object.

---

```java
String s = "Java";
s.concat("21");
```

Original String remains:

```java
"Java"
```

because the result was ignored.

---

```java
String s = "Java";
s = s.concat("21");
```

Now `s` points to a new String:

```java
"Java21"
```

---

## String Pool

```java
String a = "Hello";
String b = "Hello";
```

Both can share the same object from the String Pool.

This optimization works because Strings are immutable.

---

## Safety Notes

### Avoid String Concatenation Inside Loops

Bad:

```java
String result = "";

for(int i=0;i<1000;i++) {
    result += i;
}
```

Creates many temporary String objects.

Prefer:

```java
StringBuilder sb = new StringBuilder();
```

---

### == vs equals()

Bad:

```java
String a = new String("Java");
String b = new String("Java");

a == b
```

Compares references.

---

Good:

```java
a.equals(b)
```

Compares contents.

---

### String Is Immutable, Reference Is Not

```java
String s = "Java";
s = "Spring";
```

Allowed.

Only the String object is immutable.

---

# String vs StringBuilder vs StringBuffer

## String

### Characteristics

- Immutable
- Thread-safe because immutable
- Creates new object on modification

```java
String s = "Hello";
s = s + " World";
```

---

## StringBuilder

### Characteristics

- Mutable
- Not thread-safe
- Fastest for single-threaded operations

```java
StringBuilder sb = new StringBuilder();

sb.append("Hello");
sb.append(" World");
```

---

## StringBuffer

### Characteristics

- Mutable
- Thread-safe
- Slower than StringBuilder due to synchronization

```java
StringBuffer sb = new StringBuffer();

sb.append("Hello");
sb.append(" World");
```

---

## Comparison

| Feature | String | StringBuilder | StringBuffer |
|----------|----------|----------|----------|
| Mutable | No | Yes | Yes |
| Thread Safe | Yes | No | Yes |
| Performance | Slow for updates | Fastest | Slower |
| Use Case | Read-only text | Single-thread updates | Multi-thread updates |

---

## Safety Notes

### Default Choice

Use:

```java
StringBuilder
```

for most string manipulation.

---

### Avoid StringBuffer Unless Required

Use StringBuffer only when synchronization is actually needed.

---

### Large Loops

Prefer:

```java
StringBuilder
```

instead of repeated String concatenation.

---

# Pass-by-Value in Java

## Java Is Always Pass-by-Value

There is no pass-by-reference in Java.

---

## Primitive Example

```java
int x = 10;
update(x);
```

Method receives:

```java
10
```

A copy of the value.

---

```java
void update(int x) {
    x = 20;
}
```

Original value remains unchanged.

---

## Object Example

```java
Person p = new Person();
update(p);
```

Suppose:

```java
p -> Object@100
```

Method receives a copy of:

```java
Object@100
```

Not the actual variable.

---

## Modifying Object State

```java
void update(Person p) {
    p.setName("Madhavan");
}
```

Both references point to the same object.

Changes are visible.

---

## Reassigning Reference

```java
void update(Person p) {
    p = new Person();
}
```

Only local reference changes.

Caller is unaffected.

---

## Safety Notes

### Common Interview Trap

Java never passes:

```java
Person&
```

like C++.

It passes a copy of the reference value.

---

### Remember

Object state change:

```java
p.setName("Java");
```

Visible to caller.

---

Reference reassignment:

```java
p = new Person();
```

Not visible to caller.

---

# Wrapper Classes

## What Are Wrapper Classes?

Convert primitive values into objects.

| Primitive | Wrapper |
|------------|------------|
| byte | Byte |
| short | Short |
| int | Integer |
| long | Long |
| float | Float |
| double | Double |
| char | Character |
| boolean | Boolean |

---

## Why Needed?

Generics require objects.

```java
List<Integer> numbers;
```

Not:

```java
List<int> numbers;
```

---

## Characteristics

- Immutable
- Can store null
- Contain utility methods

---

## Safety Notes

### Wrapper Can Be Null

```java
Integer age = null;
```

Possible.

Primitive:

```java
int age = null;
```

Not possible.

---

### Performance

Wrapper objects consume more memory than primitives.

Prefer primitives when null is not needed.

---

### Use equals(), Not ==

Bad:

```java
Integer a = 128;
Integer b = 128;

a == b
```

Compares references.

---

Good:

```java
a.equals(b)
```

Compares values.

---

# Autoboxing / Unboxing

## Autoboxing

Automatic conversion:

```java
int x = 10;

Integer value = x;
```

Compiler converts to:

```java
Integer value = Integer.valueOf(x);
```

---

## Unboxing

Automatic conversion:

```java
Integer value = 10;

int x = value;
```

Compiler converts to:

```java
int x = value.intValue();
```

---

## Safety Notes

### Null Unboxing Danger

Bad:

```java
Integer value = null;

int x = value;
```

Internally:

```java
value.intValue();
```

Throws:

```java
NullPointerException
```

---

### Safe Approach

```java
Integer value = null;

int x = value != null ? value : 0;
```

---

Or

```java
int x = Optional.ofNullable(value)
                .orElse(0);
```

---

### Performance Consideration

Avoid unnecessary boxing/unboxing inside large loops.

Prefer primitives whenever possible.

---

# Enums

## What Is Enum?

Represents a fixed set of constants.

```java
enum Status {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
}
```

---

Usage:

```java
Status status = Status.SUCCESS;
```

---

## Benefits

- Type safety
- Readability
- No magic strings
- Compile-time validation

---

## Enum Can Have Fields And Methods

```java
enum Status {

    SUCCESS("Success"),
    FAILED("Failed");

    private final String message;

    Status(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```

---

## Safety Notes

### Prefer Enum Over String Constants

Bad:

```java
String status = "SUCCESS";
```

Typo-prone.

---

Good:

```java
Status.SUCCESS
```

Compiler validates it.

---

### Compare Using ==

Enums are singleton constants.

```java
status == Status.SUCCESS
```

Safe and recommended.

---

# Records

## What Is A Record?

A record is a compact way to create immutable data carriers.

---

Instead of:

```java
public final class Person {

    private final String name;
    private final int age;
}
```

Use:

```java
record Person(String name, int age) {}
```

---

Java automatically generates:

- private final fields
- constructor
- accessor methods
- equals()
- hashCode()
- toString()

---

Usage:

```java
Person p = new Person("Madhavan", 30);

p.name();
p.age();
```

---

## Benefits

- Less boilerplate
- Immutable by default
- Great for DTOs
- Great for request/response models
- Great for value objects

---

## Safety Notes

### Records Are Shallowly Immutable

```java
record Employee(List<String> skills) {}
```

Danger:

```java
skills.add("MongoDB");
```

List contents can still change.

---

### Protect Mutable Fields

```java
record Employee(List<String> skills) {

    public Employee {
        skills = List.copyOf(skills);
    }
}
```

---

### Records Are Not Suitable When

- State must change later
- Extensive business logic exists
- JPA entities require mutability

---

# Final Interview Summary

- Java is always pass-by-value.
- Objects receive a copy of the reference value.
- String is immutable.
- Wrapper classes are immutable.
- Records are shallowly immutable.
- StringBuilder is mutable and fastest for string manipulation.
- StringBuffer is mutable and thread-safe.
- final class does not guarantee immutability.
- final reference does not guarantee immutable object.
- Always protect mutable collections inside immutable classes.
- Use equals() for String and Wrapper value comparison.
- Use == for Enum comparison.
- Always null-check before unboxing wrappers.
- Prefer primitives unless null/object behavior is required.
- Prefer enum over string constants.
- Prefer records for DTOs and value objects.
