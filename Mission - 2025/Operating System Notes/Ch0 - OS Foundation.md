# How a Computer Starts

Before understanding an OS, we need to understand how the system starts.

**BIOS/UEFI** is firmware, which means software stored in **non-volatile flash memory on the motherboard**. Unlike the OS, it is not stored on your normal SSD/HDD. When you press the power button, the **CPU starts executing the BIOS/UEFI firmware**.

BIOS/UEFI first performs **POST (Power-On Self-Test)** and initializes the essential hardware required to start the computer, such as the CPU and RAM. It then looks for a **bootable device**, such as an SSD, HDD, or USB drive.

Once it finds one, **BIOS/UEFI loads/starts the bootloader, and the CPU executes the bootloader's instructions**. The bootloader is a small program stored on persistent storage whose main job is to find the **Operating System kernel**, load it from the SSD/HDD into **RAM**, and then transfer control to the kernel. The **CPU then starts executing the kernel's instructions**.

The **kernel is the core of the Operating System**. Once the kernel starts running, it takes control of the system and manages important resources such as the CPU, memory, processes and threads, devices, filesystems, and I/O.

Applications cannot normally access hardware or protected system resources directly. Instead, they request services from the kernel using **system calls**. For example, when an application wants to read a file, it asks the kernel, and the kernel works with the filesystem and device driver to access the storage device.

After the OS has started, BIOS/UEFI is mostly no longer involved in normal system operation. The **kernel and device drivers** handle most hardware and resource management.

### Complete Flow

```text id="o0acng"
Power ON
   ↓
CPU executes BIOS/UEFI firmware
   ↓
POST + initialize essential hardware
   ↓
BIOS/UEFI finds a bootable device
   ↓
BIOS/UEFI loads/starts Bootloader
   ↓
CPU executes Bootloader
   ↓
Bootloader loads Kernel into RAM
   ↓
CPU executes Kernel
   ↓
Kernel manages the system
   ↓
Applications → System Calls → Kernel
```

**In short:** CPU executes BIOS/UEFI → BIOS/UEFI starts the bootloader → CPU executes the bootloader → Bootloader loads the kernel → CPU executes the kernel → Kernel manages the system.
