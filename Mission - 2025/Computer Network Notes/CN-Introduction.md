<h1><center>Computer Networks - Introduction</center></h1>

<b>Network</b> - In layman terms network is group of more than one computer or devices connected together to share resources with one another.

In general computer can be connected by two concepts

<ul>
<li><b>Physical Connections</b></li>
<li><b>Logical Connections</b></li>
</ul>

<b>Physical Connections</b> - In physical connections actual hardware and media drivers are used to connect devices to network.
<ul>
<li><b>Cabling:</b></li> It is connected with the help of cables like <b>Twisted Pair Cables</b> - Ethernet Cable. <b>Coaxial Cables</b> - Which is being used in older television network cables. <b>Fiber Optic Cables</b> - Which we are using right now for high speed data and internet connection.
</ul>
<ul>
<li><b>Wireless Media:</b></li> Eventhough it is wireless but still it requires device drivers to be present in our devices hence it falls under the category physical connection. <b>Wi-Fi</b> - Uses radio waves to connect.<b>Bluetooth</b> - Most commonly used concept to transfer data.
</ul>

<ul>
<li><b>Networking Devices:</b></li> <b>Switches</b> - Uses LAN and MAC address. <b>Routers</b> - Uses IP address. <b>Hubs</b> - Mainly used for broadcasting<sup>*</sup> data.
</ul>
*Broadcasting - sending data to all the devices in our network system.

<b>Logical Connections</b> - Logical connections refer to the abstract, software-based links that define how data is transmitted and managed within a network. These connections are established through network protocols and addressing schemes.

<ul>
  <li><b>IP Addressing:</b></li> Unique addresses assigned to devices for identification and communication. <b>IPV4</b> - 32 bit eg :- 192.168.154.2 <b>IPV6</b> - 128 bit eg:- 2006:8bcd:5443:1243
</ul>

<ul>
  <li><b>Subnetting:</b></li> Dividing a network into smaller sub-networks to improve efficiency and security.
</ul>
<ul>
  <li><b>Network Protocols:</b></li> <b>TCP/IP</b> - The foundational protocol suite for the internet, responsible for data transmission and routing. <b>HTTP/HTTPS</b> - Protocols for web communication. <b>FTP</b> - Protocol for file transfer. <b>SMTP</b> - Protocol for email transmission.
</ul>


<ul>
  <li><b>Routing:</b></li> The process of selecting paths in a network along which to send data packets from a source to a destination. <b>Static Routing</b> - Manually configured routes for data transmission. <b>Dynamic Routing</b> - Routes determined by routing protocols like OSPF, BGP, and RIP.
</ul>

<h2>Example:</h2>
<b>Consider a corporate office network:</b>

<b>Physical Connections:</b> The office uses Ethernet cables (twisted pair) to connect computers to switches. The switches are connected to a central router, which connects to the internet. Wi-Fi access points provide wireless connectivity. <br>
<b>Logical Connections:</b> The network uses IP addressing to assign unique addresses to each device. VLANs are configured to separate the HR, Finance, and IT departments, ensuring that data traffic is isolated and secure. Routing protocols manage the flow of data between different subnets and the internet.

<b>Basic Rules</b>
<ul><li>To send and receive data we must use some communications protocols</li><li>Data should be uncorrupted.</li><li>Computer or devices in network should be able to identify the sender and destination address using IP and MAC address.</li></ul>
