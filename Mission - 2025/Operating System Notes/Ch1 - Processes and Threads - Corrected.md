# Processes and Threads — High-Level Understanding

When we open an application such as Excel or launch our own program, several things happen between storage, memory, the operating system, and the CPU. We do not need every internal detail at first. The following mental model is enough to understand how processes and threads work.

## The complete picture in one flow

```text
Program stored on SSD/HDD
        ↓
User launches the program
        ↓
OS creates a process and an initial thread
        ↓
OS creates the process's virtual address space
        ↓
Required code and data are brought into RAM when needed
        ↓
OS scheduler assigns a runnable thread to a CPU core
        ↓
CPU executes its instructions using registers and caches
        ↓
The thread updates memory, files, the screen, or other resources
```

This is the common case. Some applications are already running in the background, so opening another file or window may send work to an existing process instead of creating a new one.

## Program

A **program** is a set of instructions stored on secondary storage, such as an SSD or HDD. It may consist of an executable, a script, bytecode, libraries, and supporting files. At this point, the program is passive; it is not doing any work.

Examples include an Excel executable, a compiled C++ application, a Python script, or Java bytecode together with the runtime needed to execute it.

The programming language does not change the fundamental OS model. Languages and runtimes may expose different APIs, but the work eventually has to be performed by threads scheduled by the operating system.

## Process

A **process** is a running environment created to execute a program. It acts as a container for the memory and OS resources required by that program.

A process normally contains:

- a process ID;
- a virtual address space;
- code and data loaded or mapped from files;
- heap memory for dynamically created data;
- open files, sockets, and other OS resources;
- security and permission information;
- at least one thread of execution.

The process provides the environment, but the **threads inside it perform the actual execution**. In modern operating systems, the scheduler normally schedules threads onto CPU cores rather than scheduling an empty process container by itself.

### One program does not always mean one process

Launching an ordinary small program commonly creates one process. However, this is not a permanent one-to-one rule.

- The same program can be launched more than once, creating multiple processes.
- One application can create helper or child processes.
- An application can reuse an already-running process.
- Multiple processes can execute code originating from the same executable file.

Therefore, “launching a program usually creates a process” is a useful starting point, while “one program always equals one process” is incorrect.

## Thread

A **thread** is a sequence of instructions being executed inside a process. It is the basic unit of work that the OS scheduler assigns to a CPU core.

When a normal process starts, it has an initial thread. Languages and runtimes often call this the **main thread**, although the OS mainly sees it as a thread belonging to that process.

The initial thread can:

- continue doing all the work by itself;
- ask the OS to create more threads;
- ask the OS to create child processes;
- communicate with existing processes or services.

## Single-threaded program

Consider a simple Hello World program. Its application logic can run completely on its initial thread:

```text
Process
└── Initial/main thread → prints "Hello World"
```

From the application developer's point of view, this is a single-threaded program.

A language runtime may still create internal threads. For example, a JVM may use garbage-collector or compiler threads even when the developer's code only uses `main`. That does not change the simple understanding that the application's own logic has one thread. It only means the complete runtime process may contain additional supporting threads.

## Multithreaded process

An application can request additional threads when it has work that can be performed concurrently.

```text
Process
├── Main/UI thread
├── Calculation thread
├── File-saving thread
└── Background worker thread
```

All these threads belong to the same process. They can perform different work, but they also share much of the process's memory and resources.

### What threads share

Threads in one process normally share:

- the process's virtual address space;
- executable code;
- heap and global data;
- open files and sockets;
- other process-level resources.

### What each thread keeps separately

Each thread has its own:

- program counter, which identifies its current instruction;
- CPU register state;
- logical call stack;
- thread-local data;
- scheduling state.

The stack holds information about active function calls and usually contains items such as parameters and local variables. A compiler may keep some values in registers or optimize them away, so “all local variables are always on the stack” is only a simplified model.

## Who creates additional processes and threads?

The application or its runtime normally **decides** when another process or thread is needed. It then asks the operating system to create it.

The OS does not usually observe heavy application load and independently invent new application threads or processes. Instead:

1. The application, framework, or runtime detects or anticipates work.
2. Its programmed logic requests a new thread or process, or uses an existing worker pool.
3. The OS creates the requested OS object and allocates the required resources.
4. The OS scheduler decides when and where its runnable threads execute.

For example, a server may be programmed to add workers when traffic increases. It may look as though the OS created workers based on load, but the server or its runtime made the decision. The OS provided the creation and scheduling mechanisms.

Programs can explicitly create both threads and processes. The exact API name differs by operating system and programming language.

The stable understanding is:

> The program or runtime decides the work structure; the OS creates, protects, and schedules the requested processes and threads.

## Thread safety

Multiple threads in one process can access the same heap data. This makes communication fast, but it can also create correctness problems.

Imagine that two Excel worker threads update the same cell:

1. The cell currently contains `10`.
2. Thread A reads `10` and plans to add `1`.
3. Thread B also reads `10` and plans to add `1`.
4. Thread A writes `11`.
5. Thread B writes `11` using its earlier value.

The expected result was `12`, but the actual result is `11`. This is a **race condition**.

Preventing this is mainly a program-level responsibility. The application, runtime, and libraries use mechanisms such as:

- locks or mutexes;
- synchronized or critical sections;
- atomic operations;
- semaphores;
- immutable data;
- thread-safe collections;
- message queues.

Different languages use different names and APIs, but the underlying purpose is the same: coordinate shared data so that threads do not interfere with one another.

`volatile` is not a general replacement for locking or atomic operations. In languages that support it, its exact meaning depends on the language memory model. For example, Java `volatile` provides visibility and ordering guarantees, but it does not make a multi-step operation such as `count++` atomic.

### Can one thread affect the entire process?

Yes. Threads have weak isolation because they share the process's memory and resources.

One faulty thread can:

- store an incorrect shared value;
- create a race condition or deadlock;
- consume too much CPU or memory;
- close a resource required by another thread;
- corrupt memory in unsafe or native code;
- cause a fatal error that terminates the process.

A handled error may affect only one operation. An unhandled exception may terminate only that thread in some runtimes, but fatal memory corruption or a process-level failure can terminate the whole process. The exact outcome depends on the OS, language, and runtime.

## Why use a separate process instead of another thread?

Threads are useful when workers need efficient access to the same data. Separate processes are useful when stronger isolation is more important.

Two separate processes do not normally share an address space. If one crashes, it usually cannot directly corrupt the private memory of the other. Processes communicate using controlled **inter-process communication**, or IPC, such as pipes, sockets, shared memory, files, or OS message mechanisms.

This gives us the main trade-off:

| Threads | Processes |
|---|---|
| Easier and faster to share data | Stronger memory and failure isolation |
| Usually cheaper to create and switch | Usually more expensive to create and switch |
| Shared-memory bugs are possible | Communication requires IPC |
| One faulty thread can damage its process | A failure is more often contained to one process |

These are general tendencies, not fixed performance guarantees.

## Excel as a high-level example

When Excel is launched, a simplified flow might be:

1. The OS creates or activates an Excel process.
2. The process begins with an initial thread.
3. The main thread initializes the application and user interface.
4. Excel creates or reuses worker threads for calculations, file operations, or background work.
5. Excel may create helper processes when it wants additional isolation or when its architecture requires them.
6. The OS schedules all runnable Excel threads onto available CPU cores.

The exact number of processes and threads is an Excel implementation detail and may change between versions. The durable lesson is that Excel's developers choose the application's work and isolation model; the OS supplies and manages the underlying processes and threads.

## Concurrency, parallelism, and CPU cores

**Concurrency** means multiple tasks make progress during overlapping periods of time. **Parallelism** means multiple tasks execute at the same instant.

On one logical CPU, the OS can rapidly switch between runnable threads:

```text
Time →  A A A | B B | A A | C C | B B
```

Only one thread is selected at each instant, but all three make progress. This is concurrency.

With multiple available CPU cores, different threads can run at the same time:

```text
Core 1 → Thread A
Core 2 → Thread B
Core 3 → Thread C
```

This is parallel execution.

A single thread cannot execute on multiple CPU cores simultaneously. The scheduler may move it from one core to another, but at a particular instant it executes on only one logical CPU.

Switching the CPU from one thread to another is called a **context switch**. The system saves enough of the current thread's execution state to resume it later and restores another thread's state.

## Where is the program actually running?

It is more accurate to think of execution as a flow across storage, memory, and CPU rather than saying that the whole process runs in one place.

### Secondary storage

The SSD or HDD stores the program's executable files, libraries, and permanent data. A file stored there is not currently executing.

### Virtual address space

When a process is created, the OS gives it a **virtual address space**. This is the process's private view of memory addresses. It is an abstraction managed by the OS and the CPU's memory-management hardware, not simply one continuous physical block created in RAM.

The address space contains mappings for executable code, loaded libraries, heap memory, thread stacks, mapped files, and other regions.

### RAM

The OS brings the currently needed pages of code and data into RAM. It normally does not have to load the entire program at once. Additional pages can be loaded when the process accesses them.

Virtual addresses used by the process are translated into physical RAM locations. Two processes can use the same virtual address while that address maps to different physical memory for each process.

### CPU caches and registers

When a thread is scheduled, the CPU executes its instructions. Frequently used instructions and data move through CPU caches, and the values currently being operated on are held in CPU registers.

The results can then move back to cache and RAM. If the program saves a file, the data is eventually written to secondary storage through the OS and storage system.

### Process isolation

One ordinary process cannot directly access another process's private virtual memory. The OS and CPU enforce this protection.

Controlled sharing is still possible through mechanisms such as shared memory, IPC, mapped files, or authorized debugging. The kernel can access and manage process memory because it is responsible for the entire system.

Therefore, the correct high-level statement is:

> Every process normally has an isolated virtual address space. Required pages are backed by RAM or other storage as managed by the OS, and a thread performs actual computation when the scheduler runs it on a CPU core.

## Final mental model

> A program is passive code stored on secondary storage. When it is launched, the OS usually creates a process with an initial thread. The process is an isolated container for virtual memory and resources, while its threads perform the actual work. The application or runtime may request more threads for shared concurrent work or more processes for stronger isolation. The OS creates and schedules those threads on CPU cores. Required code and data are mapped into the process's virtual address space, brought into RAM as needed, and processed through CPU caches and registers. Because threads share process memory, thread safety must be designed in the program using appropriate synchronization or communication mechanisms.
