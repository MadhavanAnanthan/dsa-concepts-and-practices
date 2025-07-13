**_First understanding processes are important, because IPC's are required to connect processes or share the common data by two processes._**<br>
While processes are typically independent, each having its own storage space, resources, and execution context,
some processes require interdependency. This necessitates Inter-Process Communication (IPC), which provides 
various mechanisms like pipes, FIFOs, message queues, semaphores, mutexes, and sockets for communication. 
The primary purpose of IPC is to enable efficient communication between processes, minimizing overhead like 
deadlocks.
![img_5.png](img_5.png)
**Common IPC Mechanisms:**

**1. Pipes:**<br>
* Simple, unidirectional communication: Data flows in one direction, like water in a pipe.<br>
* Typically used between related processes (parent-child). <br>
**Example:** A command in a shell like ls | grep "file" uses a pipe to pass the output of ls to grep.<br>
**2. Named Pipes (FIFO):**
* Similar to pipes but with a name: Allows communication between unrelated processes.<br> 
* Data still flows unidirectionally.<br>
**Example:** A server process might use a named pipe to receive requests from client processes.<br> 
**3. Message Queues:**<br>
* Allow processes to exchange messages: Messages are stored in a queue until the receiving process retrieves them.<br>
* Can be used for both synchronous and asynchronous communication.<br>
**Example:** A print spooler might use a message queue to receive print jobs from different applications.<br>
**4. Shared Memory:**<br>
* Provides a shared region of memory that multiple processes can access.<br>
* Fastest form of IPC: Processes can directly read and write to the shared memory.<br>
* Requires synchronization mechanisms (like semaphores or mutexes) to prevent race conditions.<br>
**Example:** A large database might use shared memory to allow multiple processes to access the same data.<br>
**5. Semaphores:**<br>
* Synchronization primitives: Used to control access to shared resources and prevent race conditions.<br>
* Act as counters: Processes can increment or decrement the semaphore value.<br>
**Example:** A semaphore can be used to control the number of processes that can access a shared resource at the same time.<br>
**6. Mutexes (Mutual Exclusion):**
* Similar to semaphores but provide mutual exclusion: Only one process can acquire a mutex at a time.<br>
* Used to protect critical sections of code.<br>
**Example:** A mutex can be used to prevent multiple processes from modifying a shared variable simultaneously.<br>
**7. Sockets:**<br>
* Enable communication between processes on the same or different machines over a network.<br>
* Used for client-server communication.<br>
**Example:** A web browser uses sockets to communicate with a web server.<br>
**8. Signals:**<br>
* Software interrupts: Used to notify a process of an event.<br>
* Limited data transfer: Primarily used for signaling events, not for transferring large amounts of data.<br>
**Example:** A signal can be used to interrupt a process when a user presses Ctrl+C.<br>