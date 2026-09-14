# Processes, User Mode and System Calls

When a program is running, the OS represents and manages it as a **process**. A process can contain one or more **threads**, which are the actual execution units scheduled on the CPU.

Normal application code runs in **user mode**. In user mode, it has restricted privileges and cannot directly perform privileged operations such as accessing hardware or another process's protected memory.

Whenever an application needs a service that only the OS can provide, it requests the **kernel** through a **system call**.

Common examples are:

* Reading or writing a file
* Creating a process or thread
* Sending or receiving network data
* Accessing a device
* Requesting/managing memory from the OS
* Communicating with another process

Usually, developers don't make system calls directly. We use programming-language libraries, runtimes, or OS APIs, which make the necessary system calls internally when required.

```text
Application / Process runs in User Mode
                |
                v
        Calls a Library / API
                |
                v
     System call when required
                |
                v
CPU transfers execution to Kernel Mode
                |
                v
Kernel validates request and performs operation
                |
                v
       Returns result / error
                |
                v
       Back to User Mode
                |
                v
     Application continues
```

> **Important:** Not every library/API call results in a system call. For example, a calculation can happen completely in user mode. A system call is needed when the application requires a service from the OS kernel.

## Java Example

Consider:

```java
FileInputStream input = new FileInputStream("data.txt");
int data = input.read();
```

Our Java code does not directly communicate with the SSD/HDD.

Conceptually, the flow is:

```text
Java Application
      ↓
FileInputStream.read()
      ↓
JVM / Native OS API
      ↓
System Call
      ↓
Kernel
      ↓
Filesystem
      ↓
Storage Driver / Device
```

The kernel retrieves the requested data and returns it to the application.

There is one useful nuance here: the data may already be in the **OS filesystem cache**, so every `read()` does not necessarily cause a physical SSD/HDD read.

## Postman Example

When we select a file and upload it using `multipart/form-data` in Postman:

```text
Postman
   ↓
Uses file APIs to read the selected file
   ↓
OS reads the file
   ↓
Postman prepares the HTTP multipart/form-data request
   ↓
Uses socket / networking APIs
   ↓
OS kernel networking stack
   ↓
Network Driver
   ↓
Network Hardware
```

Postman handles the **application-level HTTP logic**, while the OS provides lower-level services such as file access and networking.

## In Short

> Developers normally write application logic using libraries and APIs. When those libraries require a privileged OS service, they make **system calls**. A system call transfers execution from **user mode to kernel mode**, allowing the kernel to safely perform the requested operation. The result is then returned and the application continues running in **user mode**.
