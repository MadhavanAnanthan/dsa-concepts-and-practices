# 🧠 Points to Remember in JVM Internal Working

- When we compile the application, all the `.java` files are converted into `.class` files — even if some of those class files are not directly used in the application.

- During class loading, only the **referred (used)** class files are loaded **on-the-fly** (lazy loading). In each class, only the **static methods, static variables, static blocks**, and **constructor bytecode** are loaded into the **Method Area**.
    - **Instance variables** are stored in the **heap**, but they are only initialized **when the corresponding object is created**.

- The **ClassLoader** uses a **delegation hierarchy** (Parent Delegation Model) to locate and load class files into the Method Area.

- The **ClassLoader** is the **first component executed** when the application starts. It has three main steps:
    - **Loading**: Loads the referenced class files into the Method Area.
    - **Linking**:
        - **Verification**: Performs a bytecode sanity check.
        - **Preparation**: Allocates memory and sets default values for static variables.
        - **Resolution**: Resolves symbolic references (like method and field names).
    - **Initialization**:
        - Executes **static blocks** (they run before the `main()` method if it's in the same class).
        - Assigns values to **static fields**.

- The **Runtime Data Area** is divided into several memory regions:
    - **Method Area**: Stores class-level metadata — static variables, methods, blocks, constant pool, constructor/method bytecode.
    - **Heap**: Stores **objects** and **instance variables**.
    - **JVM Stack**: Stores **frames** for each method call (local variables, operand stack, return address).
    - **Program Counter (PC) Register**: Keeps track of the next bytecode instruction to execute per thread.
    - **Native Method Stack**: Stores frames for native method invocations (via JNI).

- The **Execution Engine** is the component responsible for running bytecode:
    - **Interpreter**: Executes bytecode line-by-line using the PC register.
    - **JIT Compiler**: Optimizes performance by compiling frequently executed code ("hotspots") into **native machine code**.
        - For example, if a method is called over 10,000 times (configurable threshold), it is treated as a hotspot and JIT-compiled for faster execution.
    - **Garbage Collector (GC)**: Automatically triggered when **Eden space (Young Generation)** is filled. It clears **only unreferenced objects**, and promotes survivors to older generations.

- The `native` keyword can be used for **methods** (not variables).
    - Native methods are written in languages like C/C++ and interact with the JVM via **JNI (Java Native Interface)**.
    - These allow direct interaction with **hardware or OS-level functionality**, enabling Java to perform low-level operations.

---

## 🚀 Best Practices to Improve JVM Performance

- Avoid unnecessary object creation in code.
- Limit the use of **static variables**, especially in a single class.
- Prefer **local variables** over global static access inside methods.
- If a class-level object is no longer needed after some execution, explicitly **nullify the reference** to make it eligible for GC.
- Choose the appropriate **GC algorithm** for your application using JVM startup flags (e.g., G1GC, ZGC, ParallelGC).
- Allocate sufficient **heap memory** (`-Xms`, `-Xmx`) based on expected load.
- Use **profilers** (JVisualVM, JFR, YourKit, etc.) during development and load testing to monitor memory and GC behavior.
