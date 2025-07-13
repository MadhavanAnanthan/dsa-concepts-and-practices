#### OS Foundation

Before diving into OS, we need to understand how the OS is loaded from secondary storage into Main memory, So we need to understand about BIOS or Firmware.<br>
**To understand Kernal** - [Check Ch8](Ch8%20-%20Kernel%20vs%20User%20Mode.md)<br>
We aware OS will be middleware between hardware and installed applications. But Kernal which is present in OS, will take care all things.
**BIOS/UEFI Storage:** The BIOS/UEFI firmware is a software, stored in a ROM or flash memory chip on the motherboard. This is non-volatile memory, meaning it retains its contents even when the computer is powered off.<br>
**Boot Process:** When the computer is turned on, the BIOS/UEFI is the first software to run.<br>
**POST and Initialization:** The BIOS/UEFI performs the POST to check hardware functionality and initializes the hardware components.<br>
**OS Loading:** The BIOS/UEFI locates and loads the operating system(contains KERNAL) from a secondary storage device (hard drive, SSD, etc.) into RAM.<br>
**OS Control:** After the OS(contains KERNAL) is loaded into RAM, the BIOS/UEFI's role is complete. The Kernal takes over control of the system, including all hardware interactions. The OS uses its own drivers and system calls to manage hardware, memory, processes, and all other resources. The BIOS/UEFI is no longer involved in these operations.<br>

**Conclusion about BIOS**
The BIOS/UEFI is crucial for the initial startup of the computer and loading the OS. However, it does not provide ongoing services after the OS is running. The OS's Kernal is fully responsible for managing the system and interacting with hardware once it has been loaded
