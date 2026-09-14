
---
# Creating Threads in Java and Understanding Thread Lifecycle
### 26-07-2025
---

## 🧵 How Can We Create a Thread in Java?

Now let's see how many ways we can create a thread in Java.  
Later, we’ll look at how to use the **Concurrency APIs** to build an **efficient multithreaded system**.

---

## ✅ Manual Thread Creation (Multithreading Basics)

There are **2 primary ways** to create a thread manually in Java:

1. **Extending the Thread class**
2. **Implementing the Runnable interface**

You can also use:
- **Lambda expressions**
- **Anonymous inner classes**
- **Direct instance creation**

Use whichever is convenient based on your requirement and code style.

> ⚠️ These methods fall under **pure multithreading**, not concurrency utilities.

---


## 🔄 Thread Lifecycle (Very Important to Understand)

Whether you create a thread manually or use Java’s concurrency utilities, the **thread lifecycle** remains the same.

### 🧭 Thread Lifecycle States:

**New → Runnable → Running → Blocked/Waiting → Terminated**

| State          | Description |
|----------------|-------------|
| **New**        | Thread is created but not started yet |
| **Runnable**   | `start()` is called — thread is ready to run but waiting for CPU |
| **Running**    | Thread is actively executing its `run()` method |
| **Blocked**    | Thread is waiting to enter a synchronized block or method |
| **Waiting**    | Thread is waiting indefinitely using `wait()` or similar methods |
| **Terminated** | Thread has finished executing or was interrupted/killed |


### 💡 Note:
- **Blocked** = Waiting to acquire a **lock** (e.g., trying to enter a synchronized method already in use)
- **Waiting** = Waiting because of methods like `wait()`, `join()`, or `sleep()`


Understanding this lifecycle is **essential** before diving into thread coordination or Java concurrency APIs.

---

## 🧵 Understand Daemon vs Non-Daemon Threads

### 🔹 Non-Daemon Threads
- These are also called **user threads** — created by the user (you), including the `main` thread.
- JVM will **not shut down** until all non-daemon threads are finished.
- You can wait for other non-daemon threads using `.join()`, otherwise the `main` thread can exit before them.
- These threads are treated as important. JVM gives them time to finish properly.

### 🔹 Daemon Threads
- Daemon threads are **background/support threads** — they help but aren't critical.
- We can mark any thread as daemon using `thread.setDaemon(true)` **before** calling `start()`.
- JVM **automatically creates some daemon threads** (like for GC, Finalizer, etc.) when it starts.

### ⚠️ Important Rule:
> JVM will exit as soon as **all non-daemon threads finish**,  
> and it will **immediately kill all daemon threads**, even if they're still running.

So:
- Daemon threads are **ignored** by JVM during shutdown.
- Main thread **does not wait** for daemon threads.
- Daemon threads are useful for tasks like background monitoring, logging, or cleanup (but not for saving files or critical work).

### ✅ Summary Table

| Type          | Can be created manually? | JVM waits to finish? | Example                         |
|---------------|---------------------------|-----------------------|----------------------------------|
| Non-Daemon    | ✅ Yes                    | ✅ Yes                | `main` thread, business logic   |
| Daemon        | ✅ Yes (or by JVM)        | ❌ No                 | GC, loggers, heartbeat monitors |

---

🧠 **Tip:**  
If you're doing important work inside a thread (like saving files, writing to DB), **don't use daemon threads** — JVM won’t wait for them!


now we can see how to create a thread using Thread class and Runnable interface

```java
public class CreateThreadUsingThreadClassAndRunnable {

    public static void main(String[] args) {
        
        // Created runnable instance as an anonymous inner class
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 1 executed");
            }
        });

        // Created runnable instance using Lambda
        Runnable runnable = () ->{
            System.out.println("Thread 2 executed");
        };

        thread.start();
        new Thread(runnable).start();
    }
}
```

## 🧵 Producer-Consumer Example: Understanding Thread Coordination

The following is a classic **Producer-Consumer** example that demonstrates the use and purpose of multiple threads working together.

- It uses a shared **monitor lock (via the class object)** to ensure thread-safe access.
- **`synchronized` blocks** manage mutual exclusion.
- **`wait()` and `notify()`** methods facilitate **inter-thread communication**, allowing one thread to wait for a condition while another signals when the condition is met.
- `join()` used to inform to main thread to wait till the thread execution finishes. Ex- thread1.join() - main thread will wait to complete thread1. 

This example helps visualize how threads can coordinate with each other to produce a desired and predictable behavior in concurrent environments.

```java
public class ProducerConsumerExample {

    static boolean foodIsReady = false;
    static Object monitorLock = ProducerConsumerExample.class;

    public static void main(String[] args) throws InterruptedException {

        Runnable pickupRunnable = ()->{
            synchronized (monitorLock) {
                while (!foodIsReady) {
                    try {
                        monitorLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Pickup person picked up the food");
            }
        };

        Runnable foodRunnable = ()->{
            synchronized (monitorLock) {
                foodIsReady = true;
                System.out.println("Food is ready to pickup");
                monitorLock.notify();
            }
        };
        Thread restaurant = new Thread(foodRunnable);
        restaurant.start();
        Thread pickupPersonThread = new Thread(pickupRunnable);
        pickupPersonThread.start();
    }
}
```