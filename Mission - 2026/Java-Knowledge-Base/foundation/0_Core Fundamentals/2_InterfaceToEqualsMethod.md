# Java Notes: Interfaces, Methods, Modifiers, Object & Equality

# Big Picture

```text
Interface / abstract class
-> define abstraction and shared behavior

Overloading / overriding
-> provide compile-time and runtime polymorphism

static / final / finally
-> control ownership, change, inheritance and cleanup

Access modifiers
-> control visibility

Object / equals() / hashCode()
-> define the common object contract and logical identity

Composition
-> build behavior using HAS-A relationships and delegation
```

---

# Interface vs Abstract Class

## Interface

An interface defines a contract: what an implementation must be able to do.

```java
public interface PaymentGateway {
    PaymentResult pay(BigDecimal amount);
}
```

Implementations provide the behavior:

```java
public final class StripeGateway implements PaymentGateway {
    @Override
    public PaymentResult pay(BigDecimal amount) {
        return new PaymentResult("PAID", amount);
    }
}
```

The caller can depend on the abstraction:

```java
public final class PaymentService {
    private final PaymentGateway gateway;

    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public PaymentResult checkout(BigDecimal amount) {
        return gateway.pay(amount);
    }
}
```

This enables interchangeable implementations.

---

## What an Interface Can Contain

An interface can contain:

- Abstract methods
- `default` methods
- `static` methods
- `private` helper methods
- Constants
- Nested types

```java
public interface Auditable {

    int MAX_RETRIES = 3; // implicitly public static final

    void audit(String message); // implicitly public abstract

    default void auditSuccess() {
        audit(format("SUCCESS"));
    }

    static boolean isValid(String message) {
        return message != null && !message.isBlank();
    }

    private String format(String message) {
        return "AUDIT: " + message;
    }
}
```

Important:

```text
Interface fields are always public static final.
Interface abstract methods are public.
An interface has no constructor.
An interface cannot hold per-object mutable instance state.
```

---

## Abstract Class

An abstract class is a partially implemented base class.

It can define both common state and common behavior.

```java
public abstract class PaymentProcessor {

    private final String merchantId;

    protected PaymentProcessor(String merchantId) {
        this.merchantId = merchantId;
    }

    public final PaymentResult process(BigDecimal amount) {
        validate(amount);
        return doPayment(amount);
    }

    protected abstract PaymentResult doPayment(BigDecimal amount);

    private void validate(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    protected String merchantId() {
        return merchantId;
    }
}
```

A subclass completes the missing behavior:

```java
public final class CardProcessor extends PaymentProcessor {

    public CardProcessor(String merchantId) {
        super(merchantId);
    }

    @Override
    protected PaymentResult doPayment(BigDecimal amount) {
        return new PaymentResult("PAID", amount);
    }
}
```

---

## Comparison

| Feature | Interface | Abstract Class |
|---|---|---|
| Main purpose | Define a contract/capability | Share a base abstraction, state and behavior |
| Inheritance | A class can implement multiple interfaces | A class can extend only one class |
| Instance fields | No per-object instance state | Allowed |
| Constructors | Not allowed | Allowed |
| Abstract methods | Allowed | Allowed |
| Concrete methods | `default`, `static` and `private` | Allowed with any valid modifier |
| Method visibility | Contract methods are public | Can use private, package-private, protected or public |
| Fields | Only `public static final` constants | Instance and static fields are allowed |
| Best fit | Unrelated types sharing a capability | Closely related types sharing implementation/state |

---

## When to Choose an Interface

Choose an interface when:

- Different classes should obey the same contract.
- Multiple implementations may be swapped.
- The caller should depend on behavior, not implementation.
- A class may need multiple capabilities.
- You are defining an API boundary.

Examples:

```text
Comparable
Runnable
PaymentGateway
NotificationSender
Repository
```

---

## When to Choose an Abstract Class

Choose an abstract class when:

- Closely related subclasses genuinely share an IS-A relationship.
- They need common instance state.
- They need a protected constructor or protected helper methods.
- A stable algorithm has a few customizable steps.

Do not choose an abstract class only to reuse a few methods. Composition may be cleaner.

---

## Default Method Conflict Rules

If multiple interfaces supply the same default method, the class must resolve the conflict.

```java
interface EmailNotifier {
    default void send() {
        System.out.println("Email");
    }
}

interface SmsNotifier {
    default void send() {
        System.out.println("SMS");
    }
}

final class AlertService implements EmailNotifier, SmsNotifier {
    @Override
    public void send() {
        EmailNotifier.super.send();
        SmsNotifier.super.send();
    }
}
```

Rules to remember:

```text
Class method wins over an interface default method.
More specific interface wins over its parent interface.
Unrelated conflicting defaults must be resolved explicitly.
```

---

## Safety Notes

### Interface Is Not Automatically Good Design

Do not create an interface for every class.

Use one when it creates a useful boundary or supports real variation.

### Avoid Constants-Only Interfaces

Do not implement an interface merely to inherit constants.

Prefer a constants class, enum or configuration object.

### Program to an Interface, Not Every Interface

Depend on the smallest useful abstraction.

```java
List<String> names = new ArrayList<>();
```

The variable uses the `List` contract while the object uses an `ArrayList` implementation.

---

# Composition

## What Is Composition?

Composition builds a class using other objects.

It models a HAS-A relationship.

```text
Car HAS-A Engine
OrderService HAS-A PaymentGateway
ReportService HAS-A Formatter
```

The containing class delegates work to its collaborators.

```java
public final class OrderService {

    private final PaymentGateway paymentGateway;
    private final NotificationSender notificationSender;

    public OrderService(
            PaymentGateway paymentGateway,
            NotificationSender notificationSender) {
        this.paymentGateway = paymentGateway;
        this.notificationSender = notificationSender;
    }

    public void placeOrder(Order order) {
        paymentGateway.pay(order.total());
        notificationSender.send("Order placed");
    }
}
```

---

## Advantages of Composition

- Combines small, focused classes.
- Allows behavior to be changed by supplying another collaborator.
- Reduces coupling to superclass implementation details.
- Makes dependencies explicit through constructors.
- Improves unit testing with fakes or stubs.
- Avoids deep and fragile inheritance hierarchies.
- Lets a class combine multiple independent behaviors.
- Supports runtime configuration.
- Usually preserves encapsulation better than exposing protected state.

Example test:

```java
PaymentGateway fakeGateway = amount -> new PaymentResult("PAID", amount);
NotificationSender fakeNotifier = message -> { };

OrderService service = new OrderService(fakeGateway, fakeNotifier);
```

---

## Composition Over Inheritance

"Favor composition over inheritance" means:

```text
Prefer assembling behavior with collaborators when inheritance is being
used only for code reuse or when behavior needs to vary independently.
```

It does not mean inheritance is always wrong.

### Inheritance Version

```java
class ReportService extends CsvFormatter {
    public String createReport(Report report) {
        return format(report);
    }
}
```

Problems:

- `ReportService IS-A CsvFormatter` is not meaningful.
- The formatting choice is fixed by the superclass.
- Changing to JSON requires a different hierarchy.
- The subclass is coupled to inherited implementation details.

### Composition Version

```java
interface ReportFormatter {
    String format(Report report);
}

final class ReportService {
    private final ReportFormatter formatter;

    ReportService(ReportFormatter formatter) {
        this.formatter = formatter;
    }

    String createReport(Report report) {
        return formatter.format(report);
    }
}
```

Now the behavior is replaceable:

```java
ReportService csvService = new ReportService(new CsvFormatter());
ReportService jsonService = new ReportService(new JsonFormatter());
```

---

## When Inheritance Is Appropriate

Inheritance can be appropriate when:

- The relationship is a genuine and stable IS-A relationship.
- The subtype can safely replace the base type.
- The base class was intentionally designed for extension.
- Shared behavior and invariants truly belong to the hierarchy.

Ask the Liskov substitution question:

```text
Can every subclass be used wherever the parent is expected
without surprising or breaking the caller?
```

If not, the inheritance model is probably wrong.

---

## Composition Trade-Offs

Composition may require:

- More objects and constructor parameters.
- Delegating wrapper methods.
- Clear ownership and lifecycle decisions.

These costs are often worthwhile, but composition should also be used intentionally.

---

# Method Overloading vs Method Overriding

## Method Overloading

Overloading means using the same method name with different parameter lists in the same class or inheritance hierarchy.

```java
public final class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public long add(long a, long b) {
        return a + b;
    }

    public int add(int a, int b, int c) {
        return a + b + c;
    }
}
```

Overloading can differ by:

- Number of parameters
- Parameter types
- Parameter order

It cannot differ only by return type:

```java
int find() { return 1; }
String find() { return "one"; } // compilation error
```

The return type is not part of a Java method signature.

---

## Compile-Time Selection

Overloaded methods are selected at compile time using the declared types of arguments and references.

```java
void print(Object value) {
    System.out.println("Object");
}

void print(String value) {
    System.out.println("String");
}

Object value = "Java";
print(value); // Object
```

The runtime object is a `String`, but the declared argument type is `Object`.

---

## Overload Resolution Notes

Java generally prefers:

```text
exact match
-> primitive widening
-> boxing/unboxing when applicable
-> varargs
```

Example:

```java
void show(long value) { }
void show(Integer value) { }

show(10); // chooses long through primitive widening
```

Avoid clever overload sets involving boxing, varargs and `null`; they can be confusing or ambiguous.

```java
void send(String value) { }
void send(Integer value) { }

send(null); // compilation error: ambiguous
```

Constructors and static methods can also be overloaded.

---

## Method Overriding

Overriding means a subclass provides its own implementation of an inherited instance method.

```java
class Animal {
    public void speak() {
        System.out.println("Animal sound");
    }
}

final class Dog extends Animal {
    @Override
    public void speak() {
        System.out.println("Bark");
    }
}
```

Runtime polymorphism chooses the method from the actual object:

```java
Animal animal = new Dog();
animal.speak(); // Bark
```

---

## Rules for Overriding

An overriding method must follow these rules:

- Same method name and parameter types.
- Return type must be the same or covariant.
- Access cannot be more restrictive.
- It cannot throw broader checked exceptions.
- `final` methods cannot be overridden.
- `private` methods are not inherited, so they are not overridden.
- Static methods are hidden, not overridden.

Covariant return example:

```java
class AnimalFactory {
    Animal create() {
        return new Animal();
    }
}

class DogFactory extends AnimalFactory {
    @Override
    Dog create() {
        return new Dog();
    }
}
```

Always use `@Override`. It allows the compiler to catch signature mistakes.

---

## Static Method Hiding

Static methods belong to the class and are resolved from the declared reference type.

```java
class Parent {
    static void show() {
        System.out.println("Parent");
    }
}

class Child extends Parent {
    static void show() {
        System.out.println("Child");
    }
}

Parent value = new Child();
value.show(); // Parent
```

Calling a static method through an object reference is legal but misleading. Prefer:

```java
Parent.show();
```

---

## Overloading vs Overriding Comparison

| Feature | Overloading | Overriding |
|---|---|---|
| Meaning | Same name, different parameters | New implementation of inherited method |
| Polymorphism | Compile-time | Runtime |
| Inheritance required | No | Yes |
| Parameters | Must differ | Must match |
| Return type | May differ, but not by itself | Same or covariant |
| Static methods | Can be overloaded | Hidden, not overridden |
| Binding | Declared argument/reference types | Actual runtime object |

---

## Safety Notes

### Fields Are Hidden, Not Overridden

```java
class Parent {
    String name = "Parent";
}

class Child extends Parent {
    String name = "Child";
}

Parent value = new Child();
System.out.println(value.name); // Parent
```

Field access uses the declared reference type.

### Avoid Calling Overridable Methods from Constructors

A superclass constructor can invoke the subclass override before subclass fields are initialized.

This can expose incomplete object state.

---

# static, final and finally

# static

## What Does static Mean?

`static` associates a member with the class rather than with each object.

```java
public final class Employee {
    private static int count;
    private final long id;

    public Employee(long id) {
        this.id = id;
        count++;
    }

    public static int count() {
        return count;
    }
}
```

All `Employee` objects share one `count` for that loaded class.

---

## Static Members

Java allows:

- Static fields
- Static methods
- Static initialization blocks
- Static nested classes
- Static interface methods

```java
public final class IdGenerator {

    private static final String PREFIX;

    static {
        PREFIX = "ORD";
    }

    public static String create(long id) {
        return PREFIX + "-" + id;
    }
}
```

A static method:

- Has no `this` reference.
- Has no `super` reference.
- Cannot directly access instance members.
- Can be called without creating an object.

---

## Common Uses of static

- Constants
- Stateless utility methods
- Factory methods
- Shared counters or caches
- Application entry point: `public static void main(String[] args)`
- Static nested classes that do not require an outer object

---

## Static Safety Notes

### Shared Mutable State Is Risky

```java
private static int count;
```

Concurrent increments are not automatically thread-safe.

Static mutable state can also make tests dependent on execution order.

### Static Does Not Mean Constant

```java
static int count;
```

The value can change.

Use `static final` for a class-level reference that cannot be reassigned.

---

# final

## Final Variable

A final variable can be assigned only once.

```java
final int maxRetries = 3;
```

A blank final field can be assigned in every constructor:

```java
public final class User {
    private final long id;

    public User(long id) {
        this.id = id;
    }
}
```

Local variables captured by lambdas must be final or effectively final.

---

## Final Reference

`final` prevents reference reassignment, not mutation of the referenced object.

```java
final List<String> names = new ArrayList<>();

names.add("Madhav");       // allowed
// names = new ArrayList<>(); // compilation error
```

Therefore:

```text
final reference != immutable object
```

---

## Final Method

A final method cannot be overridden.

```java
class PaymentProcessor {
    public final void validate() {
        // invariant that subclasses must not replace
    }
}
```

---

## Final Class

A final class cannot be extended.

```java
public final class Money {
}
```

This can protect invariants, but:

```text
final class != immutable class
```

Its fields may still be mutable.

---

## Constants

Java constants are normally `static final`:

```java
private static final int MAX_RETRIES = 3;
```

For mutable objects, the reference is constant but contents may not be:

```java
private static final List<String> NAMES = new ArrayList<>();
```

Prefer an immutable value when exposing a constant collection:

```java
public static final List<String> ROLES = List.of("ADMIN", "USER");
```

---

# finally

## What Is finally?

`finally` is a block associated with `try` that normally runs whether an exception occurs or not.

```java
Lock lock = new ReentrantLock();
lock.lock();

try {
    updateSharedState();
} finally {
    lock.unlock();
}
```

It is useful for cleanup that must happen after a `try` block.

---

## finally with catch

```java
try {
    process();
} catch (IllegalArgumentException exception) {
    handle(exception);
} finally {
    cleanup();
}
```

Order:

```text
try succeeds      -> finally
try throws caught -> catch -> finally
try throws uncaught -> finally -> exception continues
```

---

## Prefer Try-With-Resources

For resources implementing `AutoCloseable`, prefer:

```java
try (BufferedReader reader = Files.newBufferedReader(path)) {
    return reader.readLine();
}
```

It closes resources automatically and handles suppressed exceptions correctly.

---

## finally Safety Notes

### Do Not Return from finally

Bad:

```java
try {
    throw new IllegalStateException("failed");
} finally {
    return; // can suppress the exception
}
```

A `return` or new exception from `finally` can hide the original result or exception.

### finally Is Not Absolutely Guaranteed

It may not run if the JVM/process terminates abruptly, such as through `System.exit`, a forced kill or a fatal runtime failure.

---

## final vs finally vs finalize()

| Term | Meaning |
|---|---|
| `final` | Keyword restricting reassignment, overriding or inheritance |
| `finally` | Cleanup block used with `try` |
| `finalize()` | Deprecated cleanup mechanism; do not use it |

Use try-with-resources, explicit lifecycle methods or cleaners designed for the requirement instead of `finalize()`.

---

# Access Modifiers

## Purpose

Access modifiers define where a type, constructor, field or method can be accessed.

Java has four access levels:

```text
private
package-private (no keyword)
protected
public
```

---

## Access Table

| Modifier | Same Class | Same Package | Subclass in Other Package | Unrelated Other Package |
|---|---:|---:|---:|---:|
| `private` | Yes | No | No | No |
| package-private | Yes | Yes | No | No |
| `protected` | Yes | Yes | Yes, through inheritance | No |
| `public` | Yes | Yes | Yes | Yes |

Package-private means no modifier:

```java
class OrderValidator {
    boolean isValid(Order order) {
        return order != null;
    }
}
```

---

## private

Accessible only inside the declaring top-level class/nest.

Use it for internal state and implementation details.

```java
public final class BankAccount {
    private BigDecimal balance;

    public void deposit(BigDecimal amount) {
        validate(amount);
        balance = balance.add(amount);
    }

    private void validate(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }
}
```

---

## Package-Private

Accessible anywhere in the same package.

Useful for implementation types that package collaborators need but callers should not expose.

---

## protected

Accessible:

- Inside the same package, like package-private.
- In subclasses outside the package, through inheritance.

Cross-package nuance:

```java
package child;

class Child extends parent.Parent {
    void example(Child child, parent.Parent parent) {
        child.protectedMethod();  // allowed through subclass-compatible reference
        // parent.protectedMethod(); // not allowed here
    }
}
```

`protected` is not simply "public for subclasses." Cross-package access is tied to inherited access through the subclass.

Use protected members carefully because they become part of the subclass extension contract.

---

## public

Accessible wherever the declaring type itself is accessible.

Public members form the API exposed to callers and should be kept stable and intentional.

---

## Top-Level Types

A top-level class or interface can be only:

- `public`
- package-private

Top-level types cannot be `private` or `protected`.

Nested types may use all access modifiers.

---

## Constructor Access

Constructor visibility controls object creation.

```java
public final class Token {
    private Token() {
    }

    public static Token create() {
        return new Token();
    }
}
```

Common uses:

- `private`: factories, utility classes, controlled creation
- package-private: package-controlled construction
- `protected`: subclass construction
- `public`: unrestricted construction

---

## Access Modifier Safety Notes

### Use the Most Restrictive Useful Access

Start with `private`. Widen access only when another class genuinely needs it.

### Overriding Cannot Reduce Visibility

```java
class Parent {
    protected void process() { }
}

class Child extends Parent {
    @Override
    public void process() { } // allowed: wider access
}
```

Changing it to `private` or package-private would be illegal.

### Access and Non-Access Modifiers Are Different

`static`, `final`, `abstract`, `synchronized` and `volatile` affect behavior, not visibility.

---

# Object Class

## What Is Object?

`java.lang.Object` is the root class of Java's class hierarchy.

Every class directly or indirectly extends `Object`.

```java
class Person {
}
```

is conceptually:

```java
class Person extends Object {
}
```

Interfaces do not extend `Object`, but an object implementing an interface is still an instance of a class derived from `Object`.

---

## Important Object Methods

| Method | Purpose |
|---|---|
| `equals(Object)` | Logical equality |
| `hashCode()` | Hash value consistent with equality |
| `toString()` | Human-readable representation |
| `getClass()` | Runtime class metadata |
| `clone()` | Field-by-field cloning mechanism; usually avoid |
| `wait()` | Wait on an object's monitor |
| `notify()` | Wake one thread waiting on the monitor |
| `notifyAll()` | Wake all threads waiting on the monitor |
| `finalize()` | Deprecated finalization mechanism; do not use |

---

## Default Object Behavior

Unless overridden:

- `equals()` behaves like reference identity.
- `hashCode()` supplies an identity-oriented hash consistent with that equality.
- `toString()` returns class name, `@`, and an unsigned hexadecimal hash representation.

Example shape:

```text
com.example.Person@6d311334
```

Do not assume the default hash code is a memory address.

---

## toString()

Override `toString()` to provide useful diagnostics:

```java
@Override
public String toString() {
    return "Person{id=" + id + ", name='" + name + "'}";
}
```

Do not include passwords, tokens or sensitive personal information in logs.

---

## getClass()

```java
Object value = "Java";
System.out.println(value.getClass()); // class java.lang.String
```

`getClass()` returns the actual runtime class and cannot be overridden.

---

## clone()

`Object.clone()` is protected and performs a shallow field copy.

Shallow copying can accidentally share mutable nested objects.

Prefer:

- Copy constructors
- Static factory methods
- Explicit mapping
- Immutable objects

These approaches make copy semantics visible and controllable.

---

## wait(), notify() and notifyAll()

These methods coordinate threads using an object's intrinsic monitor.

The calling thread must own that monitor, normally inside `synchronized`:

```java
synchronized (lock) {
    while (!condition) {
        lock.wait();
    }
}
```

Prefer higher-level concurrency tools such as locks, latches, semaphores, blocking queues and executors when appropriate.

---

# equals() and hashCode()

## Reference Equality vs Logical Equality

Default `Object.equals()` checks whether two references point to the same object.

A class can override it to define logical equality.

```java
Money first = new Money(new BigDecimal("10.00"), "INR");
Money second = new Money(new BigDecimal("10.00"), "INR");

first == second       // false: different objects
first.equals(second)  // true if Money defines value equality
```

---

## equals() Contract

For non-null references, `equals()` must be:

### Reflexive

```java
x.equals(x) == true
```

### Symmetric

```java
x.equals(y) == y.equals(x)
```

### Transitive

```java
x.equals(y) && y.equals(z) -> x.equals(z)
```

### Consistent

Repeated calls return the same result while equality-relevant state is unchanged.

### Null-Safe

```java
x.equals(null) == false
```

---

## hashCode() Contract

The essential rule:

```text
If a.equals(b) is true, a.hashCode() must equal b.hashCode().
```

The reverse is not required:

```text
Same hash code does not guarantee equality.
Different objects may have hash collisions.
```

If equality-relevant state does not change, repeated `hashCode()` calls must remain consistent during an execution.

---

## Why hashCode() Matters

Hash-based collections use both methods:

```text
hashCode() -> choose a bucket
equals()   -> find the matching key inside candidate entries
```

Examples:

- `HashMap`
- `HashSet`
- `Hashtable`
- `ConcurrentHashMap`

If equal objects have different hash codes, lookups and duplicate detection can fail logically.

---

## Correct Value Object Example

```java
public final class EmployeeId {

    private final String value;

    public EmployeeId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EmployeeId other)) {
            return false;
        }
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
```

The fast identity check:

```java
if (this == object) {
    return true;
}
```

is an optimization and handles comparing the object with itself.

---

## getClass() vs instanceof in equals()

Exact-class check:

```java
if (object == null || getClass() != object.getClass()) {
    return false;
}
```

This disallows equality between a base object and a subclass object.

`instanceof` check:

```java
if (!(object instanceof EmployeeId other)) {
    return false;
}
```

This allows subtype instances, but subclass-added equality state can break symmetry or transitivity.

Practical guidance:

- Prefer final value classes or records for straightforward value equality.
- Use exact-class equality when equality is not designed to cross subclasses.
- Design inheritance-based equality very carefully.

---

## Mutable Hash Key Danger

Never mutate equality/hash fields while an object is used as a hash key.

```java
Set<User> users = new HashSet<>();
users.add(user);

user.setEmail("new@example.com"); // dangerous if email affects hashCode()

users.contains(user); // may now be false
```

The object may remain stored in the bucket selected by its old hash code.

Prefer immutable keys.

---

## Equality Safety Notes

### Override Both or Neither

If you override `equals()`, also override `hashCode()` using the same logical fields.

### Use the Same Fields

Fields used by `equals()` should also contribute to `hashCode()`.

### Arrays Need Content Methods

```java
Arrays.equals(first, second);
Arrays.hashCode(first);
```

An array's inherited `equals()` checks identity.

For nested arrays, consider `Arrays.deepEquals()` and `Arrays.deepHashCode()`.

### BigDecimal Has Scale-Sensitive equals()

```java
new BigDecimal("1.0").equals(new BigDecimal("1.00")) // false
new BigDecimal("1.0").compareTo(new BigDecimal("1.00")) == 0 // true
```

Choose equality semantics intentionally for domain values.

### Records Generate Equality

Records automatically generate component-based `equals()` and `hashCode()`.

```java
record Point(int x, int y) { }
```

---

# == vs equals()

## == with Primitives

For primitives, `==` compares values after applicable numeric promotion.

```java
int a = 10;
long b = 10L;

a == b // true
```

Floating-point values need care because of representation and special values.

```java
Double.NaN == Double.NaN // false
0.0 == -0.0             // true
```

For calculated decimal quantities, compare using an appropriate tolerance or domain type rather than blindly using `==`.

---

## == with References

For object references, `==` checks identity:

```text
Do both references point to the exact same object?
```

```java
Person first = new Person("Madhav");
Person second = new Person("Madhav");
Person alias = first;

first == second // false
first == alias  // true
```

`==` is also safe for null checks:

```java
value == null
value != null
```

---

## equals() with References

`equals()` asks whether two objects are logically equal according to the class contract.

Null-safe comparison:

```java
Objects.equals(first, second);
```

It behaves like:

```text
both null       -> true
only one null   -> false
otherwise       -> first.equals(second)
```

Calling an instance method on null throws `NullPointerException`:

```java
value.equals(other) // unsafe if value may be null
```

---

## String Example

```java
String first = new String("Java");
String second = new String("Java");

first == second      // false
first.equals(second) // true
```

String literals may be interned:

```java
String first = "Java";
String second = "Java";

first == second // often true because both use the pooled literal
```

Do not use this optimization as value-comparison logic. Use `equals()` for String content.

---

## Wrapper Example

```java
Integer a = 127;
Integer b = 127;
Integer c = 128;
Integer d = 128;

a == b // may be true because of wrapper caching
c == d // commonly false
```

Never depend on wrapper caching for value equality.

Use:

```java
Objects.equals(c, d)
```

Be careful when one operand is primitive because unboxing can occur:

```java
Integer value = null;
value == 0 // throws NullPointerException during unboxing
```

---

## Enum Comparison

Enum constants are unique singleton instances.

Use `==`:

```java
status == Status.SUCCESS
```

It is null-safe and recommended for enums.

---

## Array Comparison

```java
int[] first = {1, 2};
int[] second = {1, 2};

first == second      // false
first.equals(second) // false
Arrays.equals(first, second) // true
```

Both `==` and inherited `equals()` compare array identity.

---

## Comparison Summary

| Data | Recommended Comparison |
|---|---|
| Primitive values | `==` |
| Object identity | `==` |
| Null check | `== null` / `!= null` |
| General object value | `equals()` / `Objects.equals()` |
| String content | `equals()` |
| Wrapper value | `equals()` / `Objects.equals()` |
| Enum constant | `==` |
| Array contents | `Arrays.equals()` / `Arrays.deepEquals()` |
| `BigDecimal` numeric ordering | `compareTo()` when scale should be ignored |

---

# Final Interview Summary

- An interface defines a contract; an abstract class can share instance state and implementation.
- A class can implement multiple interfaces but extend only one class.
- Interface fields are implicitly `public static final`.
- Prefer composition when behavior must vary or inheritance would exist only for code reuse.
- Use inheritance only for a valid, substitutable IS-A relationship.
- Overloading is selected at compile time from parameter and declared types.
- Overriding is selected at runtime from the actual object.
- Static methods are hidden, not overridden.
- `static` belongs to the class; mutable static state is shared and may require synchronization.
- `final` prevents reassignment, overriding or inheritance depending on where it is used.
- A final reference does not make its object immutable.
- `finally` normally executes for cleanup; prefer try-with-resources for `AutoCloseable` resources.
- Use the most restrictive access modifier that satisfies the design.
- `Object` is the root of every Java class hierarchy.
- Override `equals()` and `hashCode()` together using the same logical fields.
- Equal objects must have equal hash codes; equal hash codes do not prove equality.
- Do not mutate equality fields while an object is a key in a hash-based collection.
- `==` compares primitive values or object identity.
- `equals()` compares logical equality as defined by the class.
- Use `Objects.equals()` for null-safe object comparison.
- Use `==` for enum constants and `Arrays.equals()` for array contents.

---

# Wall Notes / Rules to Remember

```text
Interface
-> contract; multiple capabilities; no per-object state

Abstract class
-> shared base state/behavior; single inheritance

Composition
-> HAS-A + delegation; flexible and testable

Inheritance
-> use only for a genuine, substitutable IS-A relationship

Overloading
-> same name, different parameters, compile-time selection

Overriding
-> same signature, subclass implementation, runtime selection

static
-> belongs to the class; shared across its objects

final
-> assign once / cannot override / cannot extend

finally
-> cleanup after try; do not return from it

Access modifiers
-> expose the minimum necessary API

equals()
-> logical equality

hashCode()
-> equal objects must have equal hashes

==
-> primitive value or reference identity

Prefer immutable hash keys.
Override equals() and hashCode() together.
Prefer composition when behavior should be replaceable.
```
