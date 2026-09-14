#### What is Disk Scheduling

* Disk scheduling is about optimizing the order in which requests to read or write data are serviced by the disk drive.<br> 
* The OS kernel utilizes disk scheduling algorithms to determine the sequence of these requests, minimizing the physical movement of the disk's read/write head.<br>
* Once the data is located, the kernel provides memory references (addresses) to the requesting process, enabling the transfer of data between the disk and main memory.

**Disk vs CPU Scheduling Analogy:**
* Think of disk scheduling as organizing the order in which a library's books are retrieved from the shelves.
* Think of CPU scheduling as deciding which person gets to read those books at the library's tables.

**Some imp Disk Scheduling algos**
1. First Come First Serve (FCFS)
2. Shortest Seek Time First (SSTF)
3. SCAN Algorithm
4. Circular Scan (C-SCAN) Algorithm
5. LOOK Algorithm
6. Circular Look (C-LOOK) Algorithm