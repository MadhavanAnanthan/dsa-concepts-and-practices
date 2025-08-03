
> **Question:**
> _Write a class that ensures two threads print `"foo"` and `"bar"` alternately. If one thread calls `foo()`, and the other calls `bar()`, the output should be:_
>
> ```> foobarfoobarfoobar (or similar, optional space) >```

## Solution Analysis \& Examples

We’ll look at several approaches, note which do or don’t truly guarantee alternation, and provide **corrected or illustrative code** for each.

### 1. Thread.sleep (Timer) Method — **Not Recommended**

This approach relies on thread start timing and sleep intervals, but Java thread scheduling is *not deterministic*.
**_Output may not always alternate:_**

```java
public class FooBarTimer {
    static synchronized void printMessage(String message){
        System.out.print(message);
    }
    public static void main(String[] args) throws InterruptedException {
        for(int i = 0; i < 5; i++) {
            new Thread(() -> printMessage("foo")).start();
            Thread.sleep(100); // just hoping foo runs first!
            new Thread(() -> printMessage("bar")).start();
        }
    }
}
```

**Why Not Recommended?**

- Outputs like infoofoofoobarbarbar, etc, can happen.
- Thread scheduling is not guaranteed by sleep/start order.
- Not a proper concurrency control solution.


### 2. wait/notify **(Single Lock, Correct Version)**

Classic synchronization using a shared lock and a boolean turn flag.

```java
public class FooBarAlternately {
    private final Object lock = new Object();
    private boolean fooTurn = true;

    public void foo() throws InterruptedException {
        synchronized (lock) {
            while (!fooTurn) lock.wait();
            System.out.print("foo");
            fooTurn = false;
            lock.notifyAll();
        }
    }
    public void bar() throws InterruptedException {
        synchronized (lock) {
            while (fooTurn) lock.wait();
            System.out.print("bar");
            fooTurn = true;
            lock.notifyAll();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FooBarAlternately obj = new FooBarAlternately();
        int N = 5;
        Thread t1 = new Thread(() -> {
            try { for (int i = 0; i < N; i++) obj.foo(); } catch (Exception e) {}
        });
        Thread t2 = new Thread(() -> {
            try { for (int i = 0; i < N; i++) obj.bar(); } catch (Exception e) {}
        });
        t1.start(); t2.start(); t1.join(); t2.join();
    }
}
```

**Alternation is guaranteed due to explicit signaling and waiting on one lock and a clear `fooTurn` flag.**

### 2B. **wait/notify** — Using **Two Object Locks**

Here, each thread waits on its own lock and notifies the other’s. This is another common style, especially in leetcode/grind type interviews.

```java
public class FooBarTwoLocks {
    private final Object lockFoo = new Object();
    private final Object lockBar = new Object();
    private final int n;

    public FooBarTwoLocks(int n) {
        this.n = n;
    }

    public void foo() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lockFoo) {
                System.out.print("foo");
                synchronized (lockBar) {
                    lockBar.notify();
                }
                if (i < n - 1) {
                    lockFoo.wait();
                }
            }
        }
    }

    public void bar() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lockBar) {
                lockBar.wait();
                System.out.print("bar");
            }
            synchronized (lockFoo) {
                lockFoo.notify();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FooBarTwoLocks fb = new FooBarTwoLocks(5);
        Thread t1 = new Thread(() -> {
            try { fb.foo(); } catch (Exception e) {}
        });
        Thread t2 = new Thread(() -> {
            try { fb.bar(); } catch (Exception e) {}
        });

        // Pre-lock bar's lock so it blocks first
        synchronized (fb.lockBar) {
            t1.start();
            t2.start();
            fb.lockBar.notify(); // kickstart bar's first wait
        }

        t1.join(); t2.join();
    }
}
```

**Note:**

- Each thread has its own lock for wait/notify.
- The initial `notify` on `lockBar` is needed to "wake" the first bar thread.
- Proper alternation, but more complex!


### 3. Volatile Spinlock (Not Recommended, but fixed)

This design uses busy-waiting on a volatile boolean. It works, but is a CPU resource hog and not recommended for real-world code!

```java
public class FooBarSpinLock {
    private volatile boolean fooTurn = true;
    public void foo() {
        while (true) {
            if (fooTurn) {
                System.out.print("foo");
                fooTurn = false;
                break;
            }
        }
    }
    public void bar() {
        while (true) {
            if (!fooTurn) {
                System.out.print("bar");
                fooTurn = true;
                break;
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        FooBarSpinLock obj = new FooBarSpinLock();
        int N = 5;
        for (int i = 0; i < N; i++) {
            Thread t1 = new Thread(obj::foo);
            Thread t2 = new Thread(obj::bar);
            t1.start(); t2.start();
            t1.join(); t2.join();
        }
    }
}
```

**Not recommended due to inefficiency; for reference/education only.**

### 4. CountDownLatch (Illustrative, but NOT Ideal)

CountDownLatch is single-use, so to use for multiple alternations you’d need a new latch for each pair.

```java
import java.util.concurrent.CountDownLatch;
public class FooBarLatch {
    public static void main(String[] args) throws InterruptedException {
        int N = 5;
        for (int i = 0; i < N; i++) {
            CountDownLatch latch = new CountDownLatch(1);

            Thread fooThread = new Thread(() -> {
                System.out.print("foo");
                latch.countDown();
            });

            Thread barThread = new Thread(() -> {
                try { latch.await(); System.out.print("bar"); } catch (Exception e) {}
            });

            fooThread.start();
            barThread.start();
            fooThread.join(); barThread.join();
        }
    }
}
```

**Each alternation needs a new latch. Not a scalable or idiomatic solution, for comparison only!**

### 4B. **CyclicBarrier** Solution

Here’s how you’d use a `CyclicBarrier` to synchronize alternation:

```java
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class FooBarCyclicBarrier {
    public static void main(String[] args) throws InterruptedException {
        int N = 5;
        CyclicBarrier barrier = new CyclicBarrier(2);
        Object lock = new Object();
        boolean[] fooTurn = {true}; // box boolean for outer scope

        Thread t1 = new Thread(() -> {
            try {
                for (int i = 0; i < N; i++) {
                    synchronized (lock) {
                        while (!fooTurn[0]) lock.wait();
                        System.out.print("foo");
                        fooTurn[0] = false;
                        lock.notifyAll();
                    }
                    barrier.await();
                }
            } catch (Exception e) {}
        });

        Thread t2 = new Thread(() -> {
            try {
                for (int i = 0; i < N; i++) {
                    barrier.await();
                    synchronized (lock) {
                        while (fooTurn[0]) lock.wait();
                        System.out.print("bar");
                        fooTurn[0] = true;
                        lock.notifyAll();
                    }
                }
            } catch (Exception e) {}
        });

        t1.start(); t2.start();
        t1.join(); t2.join();
    }
}
```

**Pairing a barrier with a turn-flag ensures strict alternation.**

### 5. Dual Semaphore (Best Recommendation)

The clearest and safest solution uses two semaphores:

```java
import java.util.concurrent.Semaphore;
public class FooBarSemaphore {
    private final Semaphore fooSem = new Semaphore(1);
    private final Semaphore barSem = new Semaphore(0);
    private final int n;

    public FooBarSemaphore(int n) { this.n = n; }

    public void foo() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            fooSem.acquire();
            System.out.print("foo");
            barSem.release();
        }
    }

    public void bar() throws InterruptedException {
        for (int i = 0; i < n; i++) {
            barSem.acquire();
            System.out.print("bar");
            fooSem.release();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FooBarSemaphore fb = new FooBarSemaphore(5);
        Thread t1 = new Thread(() -> {
            try { fb.foo(); } catch (InterruptedException e) {}
        });
        Thread t2 = new Thread(() -> {
            try { fb.bar(); } catch (InterruptedException e) {}
        });
        t1.start(); t2.start(); t1.join(); t2.join();
    }
}
```

**This works reliably for any number of cycles and is used in many production codes.**

### 6. ForkJoinPool Example

You can also coordinate alternation using a **ForkJoinPool** and the dual-semaphore method:

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

public class FooBarForkJoin {
    public static void main(String[] args) throws InterruptedException {
        final int N = 5;
        final Semaphore fooSem = new Semaphore(1);
        final Semaphore barSem = new Semaphore(0);

        ForkJoinPool pool = new ForkJoinPool();

        Runnable fooTask = () -> {
            for (int i = 0; i < N; i++) {
                try {
                    fooSem.acquire();
                    System.out.print("foo");
                    barSem.release();
                } catch (InterruptedException ignored) {}
            }
        };

        Runnable barTask = () -> {
            for (int i = 0; i < N; i++) {
                try {
                    barSem.acquire();
                    System.out.print("bar");
                    fooSem.release();
                } catch (InterruptedException ignored) {}
            }
        };

        pool.submit(fooTask);
        pool.submit(barTask);

        pool.shutdown(); // Let the pool die when tasks are done
        // Optionally, block until all tasks are done:
        while (!pool.isTerminated()) {
            Thread.sleep(50);
        }
    }
}
```

**Result:** Alternation is guaranteed regardless of pool scheduling; semaphores enforce strict order.

## **Comparative Summary Table**

| Approach | Works? | Notes |
| :-- | :--: | :-- |
| Thread.sleep | ❌ | Not guaranteed, only demonstrative |
| wait/notify | ✅ | Proper use guarantees alternation |
| wait/notify (2) | ✅ | Two-lock version, more complex |
| Volatile spinlock | ⚠️ | Works, but busy-waits! |
| CountDownLatch | ⚠️ | Not reusable, for illustration |
| CyclicBarrier | ✅ | Requires extra turn logic + barrier |
| Dual Semaphore | ✅ | Best, clean and idiomatic |
| ForkJoinPool | ✅ | Works with dual semaphores |

## **Key Takeaways**

- **Always** use a real concurrency primitive (semaphores, wait/notify, locks) to coordinate thread alternation!
- **Avoid** using `sleep()` or busy-wait (`volatile`) for thread communication.
- **Semaphores** (dual) or **proper lock/wait/notify** solutions are the best practice in Java.

