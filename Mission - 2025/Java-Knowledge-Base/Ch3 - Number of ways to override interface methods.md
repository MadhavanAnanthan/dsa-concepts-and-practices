<h1>Overriding Implemented Methods from an Interface</h1>

<h2>1. Direct Implementation in a Named Class</h2>
<p>This is the traditional way to implement an interface by creating a named class and providing implementations for all abstract methods.</p>

```java
interface MyInterface {
    void myMethod();
}

class MyClass implements MyInterface {
@Override
public void myMethod() {
System.out.println("Implemented method in MyClass");
}
}
```

<h2>2. Anonymous Inner Class</h2>
<p>An anonymous inner class allows you to create an instance of an interface or abstract class and override its methods on the fly without creating a separate named class.</p>

```java
interface MyInterface {
    void myMethod();
}

public class Main {
public static void main(String[] args) {
MyInterface myObject = new MyInterface() {
@Override
public void myMethod() {
System.out.println("Implemented method in anonymous inner class");
}
};
myObject.myMethod();
}
}
```

<h2>3. Lambda Expression</h2>
<p>Lambda expressions provide a more concise way to implement functional interfaces (interfaces with a single abstract method).</p>

```java
interface MyInterface {
    void myMethod();
}

public class Main {
public static void main(String[] args) {
MyInterface myObject = () -> System.out.println("Implemented method using lambda expression");
myObject.myMethod();
}
}
```

<h2>4. Default Methods</h2>
<p>In Java 8 and later, you can define default methods in interfaces. These methods provide default implementations that can be optionally overridden by implementing classes.</p>

```java
interface MyInterface {
    default void myMethod() {
        System.out.println("Default method in interface");
    }
}

class MyClass implements MyInterface {
@Override
public void myMethod() {
// Call the default method from the interface
MyInterface.super.myMethod();

        // Add or modify behavior here
        System.out.println("Overridden method in MyClass");
    }
}
```

<h2>Summary</h2>
<ul>
    <li><strong>Direct Implementation in a Named Class</strong>: Traditional approach, good for reusable and more complex implementations.</li>
    <li><strong>Anonymous Inner Class</strong>: Convenient for one-time-use implementations, such as event handlers.</li>
    <li><strong>Lambda Expression</strong>: Ideal for functional interfaces, offering concise and readable code.</li>
    <li><strong>Default Methods</strong>: Provide a way to add new methods to interfaces without breaking existing implementations.</li>
</ul>
