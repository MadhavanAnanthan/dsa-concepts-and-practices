# OOD Basics & Design Principles - Interview Notes

1. **Interfaces vs Classes:** Interface methods are abstract by default (no body), but use `default` for implementations, and all interface fields are implicitly `public static final`.
2. **Encapsulation & Initialization:** Constructor initialization with overloading blocks is preferred over setters because it prevents objects from existing in an uninitialized, half-baked state.
3. **Liskov Substitution Principle (LSP):** Subtypes must be substitutable for their base types without altering expected behavior, results, or exception contracts.
4. **Open-Closed Principle (OCP):** Software entities should be open for extension but closed for modification, allowing new features to be added without rewriting existing code.
5. **Constructor Initialization Win:** Using constructors (along with constructor overloading for specific fields) ensures objects are fully valid upon creation and prevents half-baked states, while setters offer flexibility for optional properties at the risk of leaving fields uninitialized. The constructor is the real winner because it follows robust design without losing any field values.

The code below demonstrates an interview-ready Low-Level Design (LLD) approach to cleanly implementing classes, abstract classes, interfaces, instance and abstract methods, and access modifiers while strictly adhering to encapsulation and other design best practices.
```java
public interface PaymentProcessor {
    int MAX_RETRIES = 3; // Implicitly public static final

    boolean processPayment(double amount); // Abstract method (no body)

    default void logTransactionDetails() {
        System.out.println("Logging transaction details...");
    }
}

public abstract class BasePayment implements PaymentProcessor {
    private long transactionId;
    private boolean isSecure;

    // Constructor for initialization
    public BasePayment(long transactionId, boolean isSecure) {
        this.transactionId = transactionId;
        this.isSecure = isSecure;
    }

    public long getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isSecure() {
        return this.isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    // Abstract method can be implemented here or left to subclasses
    @Override
    public abstract boolean processPayment(double amount);
}

public class CreditCardProcessor extends BasePayment {

    public CreditCardProcessor(long transactionId, boolean isSecure) {
        super(transactionId, isSecure);
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("Received amount from card: " + amount);
        HelperClass helper = new HelperClass();
        return helper.validateToken();
    }
}

public class UPIProcessor extends BasePayment {

    public UPIProcessor(long transactionId, boolean isSecure) {
        super(transactionId, isSecure);
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("Received amount from UPI: " + amount);
        HelperClass helper = new HelperClass();
        return helper.validateToken();
    }
}

public class HelperClass {
    public boolean validateToken() {
        // Validation logic here
        return true;
    }
}
```