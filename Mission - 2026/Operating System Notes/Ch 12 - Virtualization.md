<h3>Virtualization</h3>

<p>Virtualization creates a simulated computing environment, called a Virtual Machine (VM), that has its own virtual hardware (CPU, memory, storage, network). This virtual environment is created when you set up a new VM using virtualization software.  Each VM is isolated from the underlying physical hardware and from other VMs.</p>

<h3>Hypervisor</h1>

<p>A hypervisor is essential software that must be installed <em>before</em> creating any VMs. It manages and allocates resources to the VMs, ensuring they run in isolation and don't interfere with each other or the host system.</p>

<p>The hypervisor handles:</p>

<ul>
  <li><b>Isolation:</b> Keeping VMs separate from each other.</li>
  <li><b>Hardware Access:</b> Controlling how VMs access physical hardware.</li>
  <li><b>Resource Allocation:</b> Assigning resources like CPU, memory, and storage to VMs.</li>
  <li><b>Instruction Execution:</b> Managing the execution of instructions within VMs.</li>
  <li><b>Resource Management:</b> Optimizing the use of hardware resources.</li>
</ul>

<h3>Types of Hypervisors</h2>

<ul>
  <li><b>Type 1 (Bare-Metal):</b> Runs directly on the hardware, like an operating system (e.g., VMware ESXi, Microsoft Hyper-V).</li>
  <li><b>Type 2 (Hosted):</b> Runs on top of an existing operating system (e.g., VMware Workstation, VirtualBox).</li>
</ul>

<h3>Benefits of Virtualization</h1>

<ul>
  <li><b>Run Multiple Operating Systems:</b> Use different operating systems on a single machine.</li>
  <li><b>Increase Resource Efficiency:</b> Optimize hardware utilization by sharing resources among VMs.</li>
  <li><b>Improve Isolation:</b> Enhance security and stability by isolating applications or services.</li>
  <li><b>Facilitate Testing and Development:</b> Create safe and isolated environments for testing and development.</li>
  <li><b>Provide Legacy Support:</b> Enable running older software or operating systems.</li>
</ul>