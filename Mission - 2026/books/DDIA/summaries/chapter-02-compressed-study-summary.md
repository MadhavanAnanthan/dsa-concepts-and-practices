# Chapter 2: Defining Nonfunctional Requirements - Compressed Study Summary

Book: DDIA
Chapter: 2
Source PDF: books/DDIA/DDIA.pdf
Source pages: PDF pages 57-80
Method: Compressed from the PDF-derived chapter text section by section. This is not a verbatim extraction.

Chapter 2 explains how to define nonfunctional requirements: performance, reliability, scalability, and maintainability. Functional requirements describe what the application does; nonfunctional requirements describe how well it must work. A feature-complete application that is too slow, unreliable, or impossible to operate is still a failed system.

The chapter uses a social network home timeline as the main case study. A naive timeline query repeatedly joins posts, follows, and users to find recent posts from followed accounts. At large scale this becomes expensive: many online users polling frequently can create millions of timeline queries per second, and each query may require looking up posts from hundreds or thousands of followed accounts.

The alternative is materialization: precompute each user's home timeline. When a user posts, the system inserts that post into follower timelines. Reads become fast because they come from a cache, but writes become more expensive because derived data must be updated. This is fan-out: one post may create many downstream timeline writes. Celebrity accounts and users who follow many active accounts create extreme cases that require special handling.

Performance is described with response time and throughput. Response time is what the client experiences from request to answer. Throughput is work processed per unit time, such as requests per second or timeline writes per second. As throughput approaches system capacity, response time rises sharply because of queueing. Overload can create retry storms, where timeouts trigger retries that make load even worse, sometimes leading to metastable failure.

The chapter distinguishes response time, service time, queueing delay, and latency. Service time is active processing. Queueing delay is waiting. Latency is time when the request is not actively processed, such as network travel. Because queueing often dominates user-visible delays, response time should be measured from the client side.

Response time should be treated as a distribution. Averages can hide bad user experiences. Median, p95, p99, and p999 percentiles show typical and tail behavior. Tail latency matters because one slow backend call can delay an entire user request, especially when many backend calls are made in parallel. SLOs and SLAs often use percentiles and availability targets.

Reliability means continuing to work correctly even when things go wrong. A fault is a component-level problem, such as a disk failure or service outage. A failure is when the system as a whole stops meeting its required service. Fault-tolerant systems prevent faults from becoming failures, but only for certain kinds and numbers of faults.

Hardware faults become normal at large scale, so systems use redundancy and distributed fault tolerance. Software faults are often harder because they are correlated: the same bug may affect many nodes. Human mistakes are also part of reliability, but the chapter warns against simplistic blame. Incidents often reveal weaknesses in the sociotechnical system: tooling, incentives, process, monitoring, interfaces, and priorities. Blameless postmortems help organizations learn.

Scalability is the ability to cope with increased load, but it must be discussed with specific load parameters. Useful metrics include requests per second, data volume, read/write ratio, cache hit rate, number of online users, and number of items per user. Linear scalability means doubling resources handles double the load at the same performance, but real systems often scale worse than linearly.

The chapter compares scaling approaches. Vertical scaling uses a bigger machine and shared memory, but costs and bottlenecks grow faster than linearly. Shared-disk systems use multiple machines with shared storage but suffer from contention and locking. Shared-nothing systems scale out horizontally with independent nodes, offering better scaling and fault tolerance but requiring sharding and distributed systems complexity.

Maintainability matters because most software cost comes after initial development. The chapter breaks maintainability into operability, simplicity, and evolvability. Operability means making the system easy to run through monitoring, observability, predictable behavior, documentation, safe defaults, and appropriate automation. Simplicity means reducing complexity through clear patterns and good abstractions. Evolvability means making future change easier through loose coupling, simplicity, good abstractions, and minimizing irreversible decisions.

Key memory line: nonfunctional requirements are how a system survives real users, real growth, real failures, and real future change.

Confidence: High
