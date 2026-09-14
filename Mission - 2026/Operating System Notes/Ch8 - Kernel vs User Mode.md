## Kernel vs User Mode

**First we need to understand what is Kernal and what it's responsible for**<br>
Kernal is the software which is exists inside an OS, whenever the BIOS/UEFI loads the OS into the RAM the Kernal will be exists inside the OS. its a kind of admin and it take care of scheduling, allocating time for processes, process preemption, managing memory and IO, and if an application started by user a system call will sent to Kernal and it will allocate the time.<br>

**Analogy:**
* Think of the OS kernel as the "manager" and user programs as "employees."<br>
* The manager has access to all the company's resources and can make any decision.<br>
* Employees have limited access and must ask the manager for permission to do certain things.<br>
* System calls are like the employees' requests to the manager.<br>

**Understand different modes**
1. In operating systems, there are typically two primary modes of operation: kernel mode and user mode.<br>
2. The kernel, which is the core of the OS, runs in kernel mode, having unrestricted access to system resources. <br>
3. User-installed applications and other user-level programs run in user mode, with limited access to hardware and critical system functions.<br>
4. When a user application needs to perform a privileged operation, such as accessing hardware or requesting system resources (request from user mode into kernal), it triggers a system call. This call transitions the system from user mode to kernel mode, allowing the kernel to execute the requested operation on behalf of the application. Once the operation is complete, the system returns to user mode (called mode switch).

**Kernel Mode (Supervisor Mode, Privileged Mode):**
* This is the most privileged mode of operation.
* The OS kernel runs in kernel mode.
* It has direct access to all hardware resources (CPU, memory, I/O devices).
* It can execute any instruction in the CPU's instruction set.
* It's responsible for managing system resources, handling interrupts, and providing services to user programs. 
* If a process running in kernel mode crashes, the entire system can crash.

**User Mode (Non-Privileged Mode):**
* This is the less privileged mode of operation.
* User applications run in user mode.
* They have limited access to hardware resources.
* They cannot directly execute certain privileged instructions.
* They must request services from the OS kernel through system calls.
* If a process running in user mode crashes, it typically doesn't affect the rest of the system.