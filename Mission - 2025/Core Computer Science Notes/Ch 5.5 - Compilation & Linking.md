# Compilation & Linking

## 1. Overview
When you write code in a high-level language like C, C++, or Java, the computer cannot execute it directly. It must go through a structured pipeline that translates human-readable source code into machine-executable binary instructions.

---

## 2. The Compilation Pipeline (Step-by-Step)

The compilation process is typically broken down into four main stages (commonly seen in compiled languages like C/C++):

### Phase 1: Preprocessing (`.c` $\rightarrow$ `.i`)
* **What happens:** The preprocessor handles directives starting with `#` (like `#include` and `#define`).
* **Key Actions:**
    * Strips out comments.
    * Expands macros (e.g., replacing `#define MAX 100` with `100`).
    * Inlines header files (copy-pasting the content of files like `#include <stdio.h>` directly into your code).

### Phase 2. Compilation (`.i` $\rightarrow$ `.s`)
* **What happens:** The compiler takes the preprocessed code and translates it into **assembly code** specific to the target processor architecture (e.g., x86, ARM).
* **Key Actions:**
    * Performs syntactic and semantic analysis (checking for syntax errors, type safety).
    * Optimizes the code structure for performance.

### Phase 3. Assembly (`.s` $\rightarrow$ `.o` or `.obj`)
* **What happens:** The assembler takes the assembly text file and translates it into **object code** (machine-readable binary instructions: `0`s and `1`s).
* **Key Actions:**
    * Generates machine code blocks, but leaves placeholders for external function calls (like `printf`).

### Phase 4. Linking (`.o` $\rightarrow$ Executable binary)
* **What happens:** Most programs rely on external libraries or multiple source files. The **Linker** binds everything together.
* **Key Actions:**
    * **Symbol Resolution:** Matches function calls (e.g., `printf`) in your object code with their actual definitions inside pre-compiled system libraries (`libc`).
    * **Address Relocation:** Assigns final memory addresses to all functions and global variables, producing a single standalone executable file (e.g., `.exe` on Windows or an ELF binary on Linux).

---

## 3. Static vs. Dynamic Linking

During the linking phase, libraries can be bound to your program in two ways:

* **Static Linking (`.a` or `.lib`):**
    * The linker copies the actual machine code of the external library directly into your final executable binary.
    * **Pros:** Self-contained executable (no missing dependency errors at runtime).
    * **Cons:** Results in larger file sizes and duplicated memory if multiple apps use the same library.

* **Dynamic Linking (`.so`, `.dll`, or `.dylib`):**
    * The executable only contains small references or stubs pointing to external shared libraries. The operating system loads the shared library into memory dynamically when the program runs.
    * **Pros:** Smaller executable files and shared memory usage across multiple apps.
    * **Cons:** Risk of "DLL Hell" or missing dependency errors if the shared library is removed or updated incompatibly on the host machine.