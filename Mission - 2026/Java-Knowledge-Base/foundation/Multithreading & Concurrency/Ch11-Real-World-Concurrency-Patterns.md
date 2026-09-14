
# Real-World Concurrency Patterns - Complete Study Notes

Building on your comprehensive knowledge of Java concurrency fundamentals, let's explore the essential real-world patterns that solve common multithreading challenges in production systems.

## Producer-Consumer Pattern

**The Producer-Consumer pattern decouples threads that produce data from threads that consume data using a shared buffer**. This is one of the most fundamental patterns in concurrent programming.

### Classic Implementation with BlockingQueue

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Shared data model
class Task {
    private final int id;
    private final String data;
    
    public Task(int id, String data) {
        this.id = id;
        this.data = data;
    }
    
    public int getId() { return id; }
    public String getData() { return data; }
    
    @Override
    public String toString() {
        return "Task{id=" + id + ", data='" + data + "'}";
    }
}

// Producer thread
class Producer implements Runnable {
    private final BlockingQueue<Task> queue;
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private final String producerName;
    
    public Producer(BlockingQueue<Task> queue, String name) {
        this.queue = queue;
        this.producerName = name;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                int taskId = taskCounter.incrementAndGet();
                Task task = new Task(taskId, producerName + "-Data-" + taskId);
                
                // This blocks if queue is full
                queue.put(task);
                System.out.println(producerName + " produced: " + task);
                
                // Simulate work time
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(producerName + " interrupted");
        }
    }
}

// Consumer thread
class Consumer implements Runnable {
    private final BlockingQueue<Task> queue;
    private final String consumerName;
    
    public Consumer(BlockingQueue<Task> queue, String name) {
        this.queue = queue;
        this.consumerName = name;
    }
    
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // This blocks if queue is empty
                Task task = queue.take();
                processTask(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(consumerName + " interrupted");
        }
    }
    
    private void processTask(Task task) throws InterruptedException {
        System.out.println(consumerName + " processing: " + task);
        // Simulate processing time
        Thread.sleep(200);
        System.out.println(consumerName + " completed: " + task);
    }
}

// Usage example
public class ProducerConsumerExample {
    public static void main(String[] args) throws InterruptedException {
        // Bounded queue with capacity 5
        BlockingQueue<Task> queue = new ArrayBlockingQueue<>(5);
        
        // Create and start producers
        Thread producer1 = new Thread(new Producer(queue, "Producer-1"));
        Thread producer2 = new Thread(new Producer(queue, "Producer-2"));
        
        // Create and start consumers
        Thread consumer1 = new Thread(new Consumer(queue, "Consumer-1"));
        Thread consumer2 = new Thread(new Consumer(queue, "Consumer-2"));
        
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        
        // Let them run for a while
        Thread.sleep(5000);
        
        // Graceful shutdown
        producer1.interrupt();
        producer2.interrupt();
        consumer1.interrupt();
        consumer2.interrupt();
    }
}
```


### Advanced Producer-Consumer with Poison Pill

```java
class AdvancedProducerConsumer {
    // Special task to signal end of production
    private static final Task POISON_PILL = new Task(-1, "POISON_PILL");
    
    static class GracefulProducer implements Runnable {
        private final BlockingQueue<Task> queue;
        private final String name;
        
        public GracefulProducer(BlockingQueue<Task> queue, String name) {
            this.queue = queue;
            this.name = name;
        }
        
        @Override
        public void run() {
            try {
                for (int i = 1; i <= 5; i++) {
                    Task task = new Task(i, name + "-Task-" + i);
                    queue.put(task);
                    System.out.println(name + " produced: " + task);
                    Thread.sleep(100);
                }
                // Signal end of production
                queue.put(POISON_PILL);
                System.out.println(name + " sent poison pill");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    static class GracefulConsumer implements Runnable {
        private final BlockingQueue<Task> queue;
        private final String name;
        
        public GracefulConsumer(BlockingQueue<Task> queue, String name) {
            this.queue = queue;
            this.name = name;
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    Task task = queue.take();
                    
                    if (task == POISON_PILL) {
                        // Put poison pill back for other consumers
                        queue.put(POISON_PILL);
                        System.out.println(name + " received poison pill, shutting down");
                        break;
                    }
                    
                    processTask(task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void processTask(Task task) throws InterruptedException {
            System.out.println(name + " processing: " + task);
            Thread.sleep(150);
        }
    }
}
```


### Use Cases for Producer-Consumer:

- **Web servers** - Accepting requests (producers) and processing them (consumers)
- **ETL pipelines** - Data extraction (producers) and transformation/loading (consumers)
- **Message queues** - Publishers (producers) and subscribers (consumers)
- **File processing** - Reading files (producers) and parsing/analyzing (consumers)


## Reader-Writer Pattern

**The Reader-Writer pattern allows multiple threads to read data concurrently but ensures exclusive access for writers**. This optimizes for scenarios where reads are more frequent than writes.

### ReadWriteLock Implementation

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

class ReadWriteCache<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    
    // Multiple readers can access simultaneously
    public V get(K key) {
        readLock.lock();
        try {
            V value = cache.get(key);
            System.out.println("Read by " + Thread.currentThread().getName() + 
                             ": " + key + " = " + value);
            
            // Simulate read operation time
            Thread.sleep(100);
            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    // Only one writer can access at a time
    public void put(K key, V value) {
        writeLock.lock();
        try {
            System.out.println("Write by " + Thread.currentThread().getName() + 
                             ": " + key + " = " + value);
            cache.put(key, value);
            
            // Simulate write operation time
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            writeLock.unlock();
        }
    }
    
    // Check if key exists (read operation)
    public boolean containsKey(K key) {
        readLock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }
    
    // Remove entry (write operation)
    public V remove(K key) {
        writeLock.lock();
        try {
            System.out.println("Remove by " + Thread.currentThread().getName() + 
                             ": " + key);
            return cache.remove(key);
        } finally {
            writeLock.unlock();
        }
    }
    
    public int size() {
        readLock.lock();
        try {
            return cache.size();
        } finally {
            readLock.unlock();
        }
    }
}

// Usage example
public class ReaderWriterExample {
    public static void main(String[] args) throws InterruptedException {
        ReadWriteCache<String, Integer> cache = new ReadWriteCache<>();
        
        // Initialize some data
        cache.put("key1", 100);
        cache.put("key2", 200);
        cache.put("key3", 300);
        
        // Create multiple readers
        for (int i = 1; i <= 5; i++) {
            Thread reader = new Thread(() -> {
                for (int j = 1; j <= 3; j++) {
                    cache.get("key" + j);
                }
            }, "Reader-" + i);
            reader.start();
        }
        
        // Create fewer writers
        for (int i = 1; i <= 2; i++) {
            Thread writer = new Thread(() -> {
                cache.put("newKey", 999);
                cache.remove("key2");
            }, "Writer-" + i);
            writer.start();
        }
        
        Thread.sleep(3000);
    }
}
```


### StampedLock - Optimistic Reading

```java
import java.util.concurrent.locks.StampedLock;

class OptimisticReadWriteCache<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    private final StampedLock lock = new StampedLock();
    
    public V get(K key) {
        // Try optimistic read first
        long stamp = lock.tryOptimisticRead();
        V value = cache.get(key); // Read without locking
        
        // Check if read was valid (no writers interfered)
        if (!lock.validate(stamp)) {
            // Optimistic read failed, fall back to pessimistic read
            stamp = lock.readLock();
            try {
                value = cache.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        
        return value;
    }
    
    public void put(K key, V value) {
        long stamp = lock.writeLock();
        try {
            cache.put(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
```


### Use Cases for Reader-Writer:

- **Configuration management** - Frequent reads of config, infrequent updates
- **Caching systems** - Many cache lookups, occasional cache updates
- **Resource pools** - Checking resource availability vs. adding/removing resources
- **Statistics tracking** - Reading metrics frequently, updating occasionally


## Thread-Safe Singleton Patterns

**Singleton ensures only one instance of a class exists while being thread-safe**. Multiple implementation strategies offer different trade-offs.

### 1. Eager Initialization (Thread-Safe)

```java
// Simple and thread-safe, but creates instance even if never used
public class EagerSingleton {
    // Instance created at class loading time
    private static final EagerSingleton INSTANCE = new EagerSingleton();
    
    private EagerSingleton() {
        // Prevent instantiation
        if (INSTANCE != null) {
            throw new IllegalStateException("Singleton already initialized");
        }
    }
    
    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
    
    public void doSomething() {
        System.out.println("Doing something...");
    }
}
```


### 2. Double-Checked Locking (Lazy + Thread-Safe)

```java
public class DoubleCheckedLockingSingleton {
    // MUST be volatile for proper double-checked locking
    private static volatile DoubleCheckedLockingSingleton instance;
    
    private DoubleCheckedLockingSingleton() {
        // Simulate expensive construction
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static DoubleCheckedLockingSingleton getInstance() {
        // First check - no synchronization
        if (instance == null) {
            synchronized (DoubleCheckedLockingSingleton.class) {
                // Second check - with synchronization
                if (instance == null) {
                    instance = new DoubleCheckedLockingSingleton();
                }
            }
        }
        return instance;
    }
    
    public void performAction() {
        System.out.println("Performing action in " + 
                         Thread.currentThread().getName());
    }
}
```


### 3. Initialization-on-Demand Holder (Best Performance)

```java
public class HolderSingleton {
    
    private HolderSingleton() {
        System.out.println("Creating singleton instance");
    }
    
    // Inner class is not loaded until getInstance() is called
    private static class SingletonHolder {
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }
    
    public static HolderSingleton getInstance() {
        // Thread-safe due to class loading mechanism
        return SingletonHolder.INSTANCE;
    }
    
    public void execute() {
        System.out.println("Executing in singleton instance");
    }
}
```


### 4. Enum Singleton (Simplest and Most Robust)

```java
public enum EnumSingleton {
    INSTANCE;
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    // Constructor called only once when enum is first accessed
    EnumSingleton() {
        System.out.println("Enum singleton initialized");
    }
    
    public void addToCache(String key, String value) {
        cache.put(key, value);
    }
    
    public String getFromCache(String key) {
        return cache.get(key);
    }
    
    public void displayCache() {
        System.out.println("Cache contents: " + cache);
    }
}

// Usage
EnumSingleton.INSTANCE.addToCache("key1", "value1");
EnumSingleton.INSTANCE.displayCache();
```


### 5. Singleton with Dependency Injection

```java
public class ConfigurableSingleton {
    private static volatile ConfigurableSingleton instance;
    private final String configuration;
    private final DatabaseConnection connection;
    
    private ConfigurableSingleton(String config, DatabaseConnection conn) {
        this.configuration = config;
        this.connection = conn;
    }
    
    public static ConfigurableSingleton getInstance(String config, 
                                                   DatabaseConnection conn) {
        if (instance == null) {
            synchronized (ConfigurableSingleton.class) {
                if (instance == null) {
                    instance = new ConfigurableSingleton(config, conn);
                }
            }
        }
        return instance;
    }
    
    public static ConfigurableSingleton getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Singleton not initialized with dependencies");
        }
        return instance;
    }
    
    public void processData(String data) {
        System.out.println("Processing: " + data + " with config: " + configuration);
    }
}
```


### Singleton Comparison Table:

| Pattern | Thread Safety | Performance | Lazy Loading | Complexity |
| :-- | :-- | :-- | :-- | :-- |
| **Eager** | ✅ Yes | ✅ Excellent | ❌ No | ✅ Simple |
| **Double-Checked** | ✅ Yes | ✅ Good | ✅ Yes | ⚠️ Medium |
| **Holder** | ✅ Yes | ✅ Excellent | ✅ Yes | ✅ Simple |
| **Enum** | ✅ Yes | ✅ Excellent | ✅ Yes | ✅ Simple |

## Work-Stealing (ForkJoinPool) Pattern

**Work-stealing allows idle threads to "steal" tasks from busy threads' queues, maximizing CPU utilization**. This pattern is implemented in ForkJoinPool and is perfect for divide-and-conquer algorithms.

### Understanding Work-Stealing Mechanics

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

// Custom work-stealing example for parallel computation
class ParallelArraySum extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000;
    private final int[] array;
    private final int start, end;
    
    // Static counter to track task creation
    private static final AtomicLong taskCount = new AtomicLong(0);
    
    public ParallelArraySum(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
        taskCount.incrementAndGet();
    }
    
    @Override
    protected Long compute() {
        int length = end - start;
        
        // Base case: compute directly if small enough
        if (length <= THRESHOLD) {
            return computeDirectly();
        }
        
        // Divide: split work into two subtasks
        int mid = start + length / 2;
        ParallelArraySum leftTask = new ParallelArraySum(array, start, mid);
        ParallelArraySum rightTask = new ParallelArraySum(array, mid, end);
        
        // Fork left task (add to work queue)
        leftTask.fork();
        
        // Compute right task in current thread
        Long rightResult = rightTask.compute();
        
        // Join left task (steal from queue if needed)
        Long leftResult = leftTask.join();
        
        return leftResult + rightResult;
    }
    
    private Long computeDirectly() {
        long sum = 0;
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " computing range [" + start + "," + end + ")");
        
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        
        return sum;
    }
    
    public static long getTaskCount() {
        return taskCount.get();
    }
    
    public static void resetTaskCount() {
        taskCount.set(0);
    }
}

// Advanced work-stealing example with custom splitting
class ParallelMatrixMultiply extends RecursiveAction {
    private static final int THRESHOLD = 64;
    private final double[][] A, B, C;
    private final int startRow, endRow, startCol, endCol;
    
    public ParallelMatrixMultiply(double[][] A, double[][] B, double[][] C,
                                 int startRow, int endRow, int startCol, int endCol) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.startRow = startRow;
        this.endRow = endRow;
        this.startCol = startCol;
        this.endCol = endCol;
    }
    
    @Override
    protected void compute() {
        int rows = endRow - startRow;
        int cols = endCol - startCol;
        
        // Base case: compute directly if small enough
        if (rows <= THRESHOLD && cols <= THRESHOLD) {
            computeDirectly();
            return;
        }
        
        // Decide how to split: rows vs columns
        if (rows >= cols) {
            // Split by rows
            int midRow = startRow + rows / 2;
            ParallelMatrixMultiply topTask = 
                new ParallelMatrixMultiply(A, B, C, startRow, midRow, startCol, endCol);
            ParallelMatrixMultiply bottomTask = 
                new ParallelMatrixMultiply(A, B, C, midRow, endRow, startCol, endCol);
            
            invokeAll(topTask, bottomTask);
        } else {
            // Split by columns
            int midCol = startCol + cols / 2;
            ParallelMatrixMultiply leftTask = 
                new ParallelMatrixMultiply(A, B, C, startRow, endRow, startCol, midCol);
            ParallelMatrixMultiply rightTask = 
                new ParallelMatrixMultiply(A, B, C, startRow, endRow, midCol, endCol);
            
            invokeAll(leftTask, rightTask);
        }
    }
    
    private void computeDirectly() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " computing matrix block [" + 
                         startRow + "," + endRow + ") x [" + startCol + "," + endCol + ")");
        
        for (int i = startRow; i < endRow; i++) {
            for (int j = startCol; j < endCol; j++) {
                double sum = 0.0;
                for (int k = 0; k < A[i].length; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
    }
}

// Comprehensive work-stealing demonstration
public class WorkStealingExample {
    public static void main(String[] args) {
        demonstrateArraySum();
        demonstrateCustomPoolConfiguration();
        demonstrateWorkStealingBehavior();
    }
    
    private static void demonstrateArraySum() {
        System.out.println("=== Array Sum with Work Stealing ===");
        
        int[] largeArray = new int[100000];
        for (int i = 0; i < largeArray.length; i++) {
            largeArray[i] = i + 1;
        }
        
        ParallelArraySum.resetTaskCount();
        
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ParallelArraySum task = new ParallelArraySum(largeArray, 0, largeArray.length);
        
        long startTime = System.currentTimeMillis();
        Long result = pool.invoke(task);
        long endTime = System.currentTimeMillis();
        
        System.out.println("Sum: " + result);
        System.out.println("Tasks created: " + ParallelArraySum.getTaskCount());
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        System.out.println("Pool parallelism: " + pool.getParallelism());
        System.out.println();
    }
    
    private static void demonstrateCustomPoolConfiguration() {
        System.out.println("=== Custom ForkJoinPool Configuration ===");
        
        // Create custom pool with specific parallelism
        ForkJoinPool customPool = new ForkJoinPool(
            2,                                    // Parallelism level
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,                                 // Exception handler
            false                                 // Async mode
        );
        
        try {
            int[] array = new int[50000];
            for (int i = 0; i < array.length; i++) {
                array[i] = 1;
            }
            
            ParallelArraySum task = new ParallelArraySum(array, 0, array.length);
            Long result = customPool.invoke(task);
            
            System.out.println("Custom pool result: " + result);
            System.out.println("Custom pool parallelism: " + customPool.getParallelism());
            System.out.println("Active thread count: " + customPool.getActiveThreadCount());
            System.out.println("Steal count: " + customPool.getStealCount());
        } finally {
            customPool.shutdown();
        }
        System.out.println();
    }
    
    private static void demonstrateWorkStealingBehavior() {
        System.out.println("=== Work Stealing Behavior Analysis ===");
        
        ForkJoinPool pool = ForkJoinPool.commonPool();
        
        // Submit multiple independent tasks to observe work stealing
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("Task 1 starting on " + Thread.currentThread().getName());
            simulateWork(1000);
        }, pool);
        
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            System.out.println("Task 2 starting on " + Thread.currentThread().getName());
            simulateWork(500);
        }, pool);
        
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            System.out.println("Task 3 starting on " + Thread.currentThread().getName());
            simulateWork(1500);
        }, pool);
        
        // Wait for all tasks to complete
        CompletableFuture.allOf(future1, future2, future3).join();
        
        System.out.println("All tasks completed");
        System.out.println("Final steal count: " + pool.getStealCount());
    }
    
    private static void simulateWork(int milliseconds) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < milliseconds) {
            // Simulate CPU-intensive work
            Math.sqrt(Math.random());
        }
        System.out.println("Work completed on " + Thread.currentThread().getName());
    }
}
```


### Work-Stealing Benefits and Use Cases:

**Benefits:**

- **Load balancing** - Automatically distributes work among threads
- **CPU utilization** - Keeps all cores busy by stealing idle work
- **Scalability** - Performance improves with more cores
- **Fault tolerance** - If one thread blocks, others continue working

**Use Cases:**

- **Parallel algorithms** - Sorting, searching, mathematical computations
- **Tree/graph processing** - Parallel tree traversal, graph algorithms
- **Divide-and-conquer problems** - Merge sort, quick sort, recursive algorithms
- **Parallel streams** - Java 8+ parallel stream operations use ForkJoinPool


### Performance Tuning for Work-Stealing:

```java
public class WorkStealingTuning {
    
    // Optimal threshold depends on problem size and complexity
    private static int calculateOptimalThreshold(int totalWork, int parallelism) {
        // Rule of thumb: total work / (parallelism * 4)
        return Math.max(100, totalWork / (parallelism * 4));
    }
    
    // Monitor pool performance
    public static void monitorPool(ForkJoinPool pool) {
        System.out.println("Pool Statistics:");
        System.out.println("  Parallelism: " + pool.getParallelism());
        System.out.println("  Active threads: " + pool.getActiveThreadCount());
        System.out.println("  Running threads: " + pool.getRunningThreadCount());
        System.out.println("  Queued submissions: " + pool.getQueuedSubmissionCount());
        System.out.println("  Queued tasks: " + pool.getQueuedTaskCount());
        System.out.println("  Steal count: " + pool.getStealCount());
        System.out.println("  Pool size: " + pool.getPoolSize());
    }
}
```


## Summary: When to Use Each Pattern

| Pattern | Best Use Case | Key Benefit | Performance Characteristic |
| :-- | :-- | :-- | :-- |
| **Producer-Consumer** | Decoupling data generation from processing | Throughput optimization | Scales with queue size and thread count |
| **Reader-Writer** | High read-to-write ratio scenarios | Concurrent read access | Excellent for read-heavy workloads |
| **Singleton** | Single point of access/control | Resource management | Minimal overhead after initialization |
| **Work-Stealing** | CPU-intensive divide-and-conquer | Maximum CPU utilization | Scales linearly with CPU cores |

### Combining Patterns:

Real-world applications often combine these patterns:

```java
// Example: Log processing system combining multiple patterns
public class LogProcessingSystem {
    // Singleton for system configuration
    private static final LogProcessingSystem INSTANCE = new LogProcessingSystem();
    
    // Producer-Consumer for log ingestion
    private final BlockingQueue<LogEntry> logQueue = new ArrayBlockingQueue<>(10000);
    
    // Reader-Writer for statistics cache
    private final ReadWriteCache<String, LogStatistics> statsCache = new ReadWriteCache<>();
    
    // Work-stealing for parallel log analysis
    private final ForkJoinPool analysisPool = ForkJoinPool.commonPool();
    
    public static LogProcessingSystem getInstance() {
        return INSTANCE;
    }
    
    // Combines all patterns for comprehensive log processing
    public void processLogs(List<LogEntry> logs) {
        // Use work-stealing for parallel processing
        analysisPool.invoke(new ParallelLogAnalysis(logs, statsCache));
    }
}
```

These real-world patterns form the building blocks of robust concurrent applications. Understanding when and how to apply each pattern is essential for designing scalable, maintainable multithreaded systems.

