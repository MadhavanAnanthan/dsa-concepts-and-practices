### Synchronization and Concurrency and Deadlock

#### Synchronization

* To achieve Synchronisation, we can do it using Monitor or Spin locks.<br>
* Maximum Deadlock might be occurred compare to parallelism, if the synchronization is not properly handled. But if the parallel process requires synchronization, then deadlock might occur. Simply deadlock will occur only for synchronization concepts.<br>
* Monitor contains Mutex and Conditional check as internal mechanism, also semaphore will be used by the OS (and programmers) to signal one thread to another about resource availability and related events, and for managing waiting (awaiting) threads.<br>
* When we talk about synchronization, we need to understand below things<br>

**1. Mutex or Mutual Exclusion or Binary Semaphores (comes under Monitor)<br>**
   Mutexes (or binary semaphores) are fundamental synchronization primitives in operating systems, using binary values (0 and 1) to control access to shared resources.  A mutex value of 0 signifies that the resource is in use, forcing other threads to wait. Upon completion, the owning thread releases the resource, changing the mutex value to 1 and allowing a waiting thread to proceed. This mechanism ensures mutual exclusion, preventing concurrent access and potential data corruption.  Therefore, mutexes are essential for managing concurrency and protecting critical sections of code.<br>
**2. Semaphore (not comes under Monitor)**
A semaphore is a synchronization mechanism that uses an integer counter to manage access to shared resources. The counter's initial value determines the maximum number of concurrent accesses. When a thread wants to access the resource, it attempts to acquire a permit by decrementing the counter. If the counter is 0, the thread is blocked. When a thread finishes, it releases the permit, incrementing the counter and potentially unblocking a waiting thread. Semaphores can be binary (mutexes) or counting, offering flexible resource management.<br>
**3. Conditional Check (comes under Monitor)**
Conditional checks in OS synchronization are implemented using condition variables, which are always associated with a mutex. The mutex protects the shared data and the condition itself. When a thread (e.g., a producer) updates the shared data, it signals the condition variable. This wakes up waiting threads (e.g., consumers).  Crucially, waiting threads, upon being awakened, must reacquire the mutex and recheck the condition before proceeding. This ensures that they don't operate on stale or inconsistent data due to race conditions or spurious wake-ups.<br>
**4. Monitor (@Synchronized in Java)**
* Monitors are a high-level abstraction in OS synchronization that simplifies concurrent programming by encapsulating shared data and operations, using mutexes and condition variables internally. The synchronized keyword in Java is a perfect example of how monitors are implemented in a programming language, automatically handling the low-level details of locking(Mutex) and conditional waiting(conditional check).<br>
* Thread will be in sleep state, if the lock occured. Also, it relyed on OS for blocking and unblocking.<br>
* In Java level, The @synchronized keyword is handled by the JVM. The JVM uses intrinsic locks (monitor objects) which might, in turn, rely on OS-level primitives (like mutexes or futexes) for blocking and unblocking threads.<br>
**5. Spin locks (Not supported in Java, support in C and C++)**
   Spin locks are explicit, user-level locking mechanisms implemented by programmers, not the OS. They are primarily used for short-duration critical sections. Spin locks use a loop for busy-waiting, where a thread repeatedly checks the lock's status. This is in contrast to blocking locks (like those used by Java's @synchronized keyword), where waiting threads go to sleep.  Spin locks are most effective when the critical section is very short, and the cost of blocking/unblocking is higher than the cost of spinning.  While C and C++ are common languages for implementing spin locks, the underlying atomic operations often rely on hardware support and might involve some OS interaction at a very low level.<br>

**Conclusion about OS Synchronization**

Monitors and spin locks are distinct synchronization mechanisms in 
operating systems. Monitors provide a high-level abstraction, 
encapsulating shared data and operations, and internally using
mutexes and condition variables. Spin locks, on the other hand, are low-level, user-level constructs
implemented by programmers. Spin locks involve a busy-waiting loop, repeatedly checking the lock.
While potentially costly in CPU cycles for long lock holds, spin locks are designed for very 
short critical sections where the overhead of blocking/unblocking is a greater concern.  Monitors 
are more general-purpose, while spin locks are specialized for brief, low-contention scenarios. 
While the spinning in a spin lock is managed by the programmer, the underlying atomic operations
often rely on hardware support and minimal OS interaction.<br>


#### Concurrency
Concurrency in an OS is the ability to handle multiple tasks seemingly at the same time.  It achieves this by rapidly switching between tasks (time-sharing) or overlapping I/O operations, giving the illusion of simultaneous execution, even if the system only has a single core.  This improves responsiveness and resource utilization.<br>

#### Deadlock
While an operating system (OS) manages resources through mechanisms like priority scheduling and sometimes resource preemption, deadlocks occur when a specific set of conditions, known as the Coffman conditions, are simultaneously met. These conditions are:

**Mutual Exclusion:** A resource is held in a non-sharable mode. This means that only one process can access the resource at any given time. While other processes may request the resource, the key factor is that exclusive access is enforced.<br>
**Hold and Wait:** A process currently holding at least one resource (e.g., R1) is also waiting to acquire additional resources (e.g., R2) that are held by other processes.<br>
**No Preemption:** Resources cannot be forcibly taken away from a process that is currently holding them. Resources can only be released voluntarily by the holding process.<br>
**Circular Wait:** A circular chain of processes exists, where each process is waiting for a resource held by the next process in the chain.1 Essentially, process A is waiting for a resource held by process B, process B is waiting for a resource held by process C, and so on, until2 a process in the chain is waiting for a resource held by process A.<br>

It is critical to understand that a deadlock will only occur if and only if all four of these conditions are met simultaneously. The OS's standard resource management practices do not prevent deadlocks when these four conditions manifest. To effectively manage deadlocks, the OS must address at least one of these conditions, either through prevention, avoidance, or detection and recovery.