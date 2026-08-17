# Chapter 2: Defining Nonfunctional Requirements - Compressed Study Notes

Book: DDIA
Chapter: 2
Source PDF: books/DDIA/DDIA.pdf
Source pages: PDF pages 57-80
Method: Compressed from the PDF-derived chapter text section by section. This is not a verbatim extraction.

## What This Chapter Is Really About

Chapter 2 explains how to define and reason about nonfunctional requirements: performance, reliability, scalability, and maintainability.

Functional requirements describe what the application does: screens, buttons, operations, and behavior. Nonfunctional requirements describe how well the application must behave: fast enough, reliable enough, secure enough, compliant enough, and maintainable enough. These requirements are often implicit, but they are just as important as features; a slow or unreliable application may be practically unusable.

The chapter uses a simplified social network home timeline as a running case study. That case study makes abstract requirements concrete by showing how design choices affect read cost, write cost, freshness, fan-out, cache updates, overload, scalability, and reliability.

Source: PDF pages 57-58

## Social Network Timeline Case Study

The example is a social network like X/Twitter. Users can post messages and follow other users.

The assumed scale:

- 500 million posts per day.
- About 5,800 posts per second on average.
- Spikes up to 150,000 posts per second.
- Average user follows 200 people and has 200 followers.
- A few celebrity users may have over 100 million followers.

The basic relational model has:

- A users table.
- A posts table.
- A follows table.

The main read operation is the home timeline: recent posts by people the current user follows.

A straightforward SQL approach joins posts, follows, and users, filters by the current user, orders by timestamp, and returns the latest posts.

The problem is scale. If the client polls every five seconds and 10 million users are online, the service would run around 2 million timeline queries per second. If each user follows 200 people, that could mean around 400 million recent-post lookups per second in the average case. Users who follow tens of thousands of accounts make the query even more expensive.

Source: PDF pages 58-59

## Materializing Timelines And Fan-Out

The chapter proposes two improvements:

- Push new posts to currently online followers instead of making clients poll repeatedly.
- Precompute each user's home timeline so reads can be served from a cache.

The materialized timeline design works like this:

1. Each user has a stored home timeline.
2. When a user posts, the system finds their followers.
3. The post is inserted into each follower's home timeline.
4. When a follower opens the app, the service reads the precomputed timeline.
5. If the follower is online, their client can subscribe to new timeline entries.

This is like delivering a message to each follower's mailbox.

The trade-off: reads become fast, but writes become more expensive. The home timeline is derived data, so it must be updated whenever source data changes.

Fan-out means one incoming request causes multiple downstream requests. If a post reaches 200 followers, the fan-out factor is 200. With 5,800 posts per second and 200 followers per post, the system performs just over 1 million timeline writes per second. That is still much less than the 400 million lookup-per-second read-heavy polling design.

During post-rate spikes, timeline deliveries can be queued, accepting temporary delay while keeping timeline reads fast from cache.

Extreme cases:

- A user who follows many active accounts may receive too many timeline writes. Since they likely cannot read everything, the system may drop some writes and show a sample.
- A celebrity with millions of followers creates huge write fan-out. Dropping writes is not acceptable. One solution is to handle celebrity posts separately and merge them into timelines at read time.

Main lesson: materialization moves work from reads to writes. This can be a good trade-off when reads are frequent and must be fast.

Source: PDF pages 59-60

## Performance: Response Time And Throughput

Performance is commonly described with two metrics.

Response time is the elapsed time from when a user makes a request until they receive the answer. It is measured in seconds, milliseconds, or microseconds.

Throughput is how much work the system processes per unit time, such as requests per second, posts per second, timeline writes per second, or bytes per second.

In the timeline example:

- Posts per second is throughput.
- Timeline writes per second is throughput.
- Time to load the home timeline is response time.
- Time until a post appears for followers is response time.

Throughput and response time are related. When load is low, response time may be low. As throughput approaches the maximum the hardware can handle, response time rises sharply because requests start queueing.

Queueing is the key mental model. If all workers are busy, new requests wait. Near capacity, a small load increase can create a large delay increase.

Source: PDF pages 61-62

## Overload, Retry Storms, And Metastable Failure

An overloaded system can sometimes fail in a self-reinforcing way.

If queues grow long, clients may time out and retry. Retries increase request volume, making overload worse. This is called a retry storm.

Even after external load decreases, the system may remain stuck in an overloaded state until restarted or reset. This is called a metastable failure.

Techniques to reduce overload risk:

- Exponential backoff: clients wait longer between retries.
- Randomized retry delay: clients avoid retrying in synchronized waves.
- Circuit breaker: clients temporarily stop sending requests to recently failing services.
- Token bucket: rate-limits request attempts.
- Load shedding: servers proactively reject requests when overloaded.
- Backpressure: servers ask clients or upstream systems to slow down.
- Better queueing and load-balancing algorithms.

The user usually cares most about response time. Throughput determines required hardware and cost. A system is scalable if its maximum throughput can be significantly increased by adding resources.

Source: PDF page 62

## Latency, Response Time, Service Time, And Queueing Delay

The chapter uses these terms carefully.

Response time is what the client sees. It includes all delays anywhere in the system.

Service time is the time the service actively spends processing the request.

Queueing delay is time spent waiting before processing or before a response can be sent. Queueing can happen at several points, such as waiting for a CPU or waiting for network output.

Latency is time when the request is not actively being processed. Network latency is the travel time of request and response messages through the network.

Response time varies because systems have random delays:

- Context switches.
- TCP retransmissions after packet loss.
- Garbage collection pauses.
- Page faults requiring disk reads.
- Mechanical vibrations in server racks.

Queueing delay often explains much of response-time variability. A few slow requests can block later requests, even if those later requests would be quick to process. This is called head-of-line blocking.

Important measurement lesson: measure response time from the client side, because queueing delay is not part of the service's own active processing time.

Source: PDF pages 62-63

## Averages, Medians, Percentiles, And Tail Latency

Response time is not a single number. It is a distribution.

The mean, or arithmetic average, is useful for estimating throughput limits, but it is not a good measure of the typical user experience because it does not show how many users experienced a given delay.

The median, or p50, is the halfway point: half of requests are faster and half are slower.

Higher percentiles show outliers:

- p95: 95% of requests are faster, 5% are slower.
- p99: 99% are faster, 1% are slower.
- p999: 99.9% are faster, 0.1% are slower.

High percentiles are called tail latencies. They matter because they represent real user experiences, often for users with large accounts or heavy data. The chapter gives Amazon as an example of caring about p99.9 for internal services because the slowest users may also be valuable customers with lots of data.

Very high percentiles can be expensive to optimize and can be affected by random factors outside your control, so there are diminishing returns.

Source: PDF pages 64-65

## User Impact And Response-Time Metrics

It seems obvious that faster services are better, but the chapter warns that many popular statistics about latency and revenue are unreliable or context-dependent.

Examples discussed include Google, Bing, Akamai, and Yahoo studies. The important lesson is not a universal "X milliseconds costs Y percent revenue" rule. The lesson is to be cautious, control for confounding factors, and measure your own system and users.

High percentiles are especially important when one user request depends on many backend calls. Even if backend calls happen in parallel, the whole request waits for the slowest call. If each backend call has a small chance of being slow, many calls together increase the chance that at least one call is slow. This is tail latency amplification.

SLOs and SLAs often use percentiles. An SLO might require median response time under 200 ms, p99 under 1 second, and 99.9% of valid requests returning non-error responses. An SLA is a contract describing what happens if SLO targets are missed, such as a refund.

To compute percentiles continuously, systems often use rolling windows and efficient histogram or approximation libraries such as HdrHistogram, t-digest, OpenHistogram, and DDSketch.

Do not average percentiles across machines or time intervals; that is mathematically meaningless. Aggregate the underlying histograms instead.

Source: PDF pages 65-66

## Reliability, Faults, And Failures

Reliability means continuing to work correctly even when things go wrong.

For software, working correctly includes:

- Performing the function the user expects.
- Tolerating user mistakes and unexpected use.
- Maintaining required performance under expected load and data volume.
- Preventing unauthorized access and abuse.

The chapter distinguishes faults from failures.

A fault is when one part of the system stops working correctly: a disk fails, a machine crashes, or an external dependency has an outage.

A failure is when the system as a whole stops providing the required service to the user, or fails to meet its SLO.

The same event can be a fault or a failure depending on the level. A failed hard drive is a failure for that hard drive, but only a fault for a storage system if other drives still provide the service.

Source: PDF page 67

## Fault Tolerance And Fault Injection

A fault-tolerant system continues providing the required service despite certain faults.

A single point of failure is a part that the system cannot tolerate losing. If it becomes faulty, the whole system fails.

In the timeline case study, a machine updating materialized timelines might crash. To tolerate that, another machine must take over without missing posts and without duplicating posts. The chapter connects this to exactly-once semantics, discussed later in the book.

Fault tolerance is always limited. A system might tolerate two disk failures or one of three nodes crashing, but it cannot tolerate every possible fault.

Sometimes it helps to deliberately cause faults so the system's recovery mechanisms are exercised. This is fault injection. Chaos engineering uses experiments such as fault injection to increase confidence that fault-tolerance mechanisms work.

Fault tolerance is often better than trying to prevent every fault, but prevention is still necessary where damage cannot be undone, such as security breaches exposing sensitive data.

Source: PDF pages 67-68

## Hardware Faults And Redundancy

Hardware faults include:

- Magnetic disk failures.
- SSD failures and uncorrectable errors.
- Power supply, RAID controller, and memory module failures.
- CPU cores that occasionally compute wrong results.
- RAM corruption from random events or physical defects.
- Whole datacenter outages or destruction from power failures, network misconfiguration, fire, flood, earthquake, or solar storms.

In small systems, these events may be rare enough that easy replacement is sufficient. In large systems, hardware faults are normal operation.

The usual response is redundancy:

- RAID spreads data across disks.
- Servers may have dual power supplies and hot-swappable parts.
- Datacenters may have batteries and generators.

Redundancy works best when faults are independent. But real faults are often correlated: whole racks, zones, or datacenters can fail together.

Cloud systems therefore focus less on making individual machines reliable and more on making services available despite faulty nodes. Availability zones identify resources that are physically co-located and therefore more likely to fail together.

Fault-tolerance techniques in later chapters handle loss of machines, racks, or availability zones by allowing other machines or datacenters to take over.

A useful operational benefit: multi-node systems can support rolling upgrades, restarting or patching one node at a time without planned service downtime.

Source: PDF pages 68-70

## Software Faults

Software faults are often harder than hardware faults because they are highly correlated. Many machines may run the same buggy software, so the same trigger can make many nodes fail together.

Examples:

- A bug causing every node to fail under particular conditions, such as leap-second-related failures.
- Firmware bugs causing devices to fail after a specific runtime.
- Runaway processes consuming CPU, memory, disk, network bandwidth, or threads.
- Dependent services slowing down, becoming unresponsive, or returning corrupted responses.
- Interactions between systems causing emergent behavior not seen in isolated tests.
- Cascading failures, where one overloaded component causes another to fail, spreading through the system.

These bugs may remain dormant until unusual conditions violate an assumption the software was making.

No single fix exists, but helpful practices include:

- Thinking carefully about assumptions and interactions.
- Thorough testing.
- Process isolation.
- Allowing processes to crash and restart.
- Avoiding retry storms and other feedback loops.
- Measuring, monitoring, and analyzing production behavior.

Source: PDF pages 70-71

## Humans And Reliability

Humans design, build, operate, and maintain systems. Human creativity and adaptation are strengths, but mistakes can contribute to failures.

The chapter warns against simplistic "human error" explanations. What is called human error is usually a symptom of a broader sociotechnical system problem: tools, incentives, procedures, interfaces, priorities, and organizational context.

Technical measures that reduce the impact of human mistakes:

- Thorough tests, including handwritten tests and property tests over random inputs.
- Rollback mechanisms for configuration changes.
- Gradual rollouts.
- Clear monitoring.
- Observability tools.
- Interfaces that encourage correct actions and discourage dangerous actions.

Organizations often prioritize feature work over resilience work. When a preventable incident happens, blaming an individual does not fix the underlying priority problem.

Blameless postmortems help teams learn from incidents. People involved in the incident share what happened without fear of punishment so the organization can improve systems, incentives, priorities, and processes.

Good incident analysis avoids simplistic answers. It should learn how the sociotechnical system actually works from the perspective of the people operating it.

Source: PDF pages 71-72

## How Important Is Reliability?

Reliability is not only for obvious high-risk domains such as nuclear power or air traffic control. Ordinary business applications also require reliability.

Unreliable business software can cause lost productivity, incorrect legal reporting, lost ecommerce revenue, and reputational damage.

Some temporary outages may be tolerable, but permanent data loss or corruption can be catastrophic. A photo application losing family photos is an example of personal impact.

The chapter also discusses the Post Office Horizon scandal: software-generated accounting shortfalls contributed to wrongful convictions of many Post Office branch managers in Britain. The lesson is that unreliable software can cause severe human harm, especially when institutions assume computer output is correct.

Sometimes teams deliberately reduce reliability investment to reduce development cost, such as when building a prototype for an unproven market. The chapter's warning is that this should be a conscious decision, with awareness of possible consequences.

Source: PDF pages 72-73

## Scalability

Scalability is the system's ability to cope with increased load.

Scalability is not a one-dimensional label. Saying "X scales" is not meaningful without specifying the kind of growth and the workload.

Useful scalability questions:

- If the system grows in a particular way, what options do we have?
- How can we add computing resources to handle additional load?
- Based on growth projections, when will the current architecture reach its limits?

For a new product with few users, the best engineering goal is often simplicity and flexibility. Premature scalability work can waste effort or lock the product into an inflexible design.

Source: PDF page 73

## Understanding Load

Before discussing growth, define the current load.

Load is often measured by throughput:

- Requests per second.
- Gigabytes of new data per day.
- Shopping cart checkouts per hour.
- Number of simultaneously online users.

Other workload characteristics can matter:

- Read/write ratio.
- Cache hit rate.
- Number of data items per user, such as followers.
- Whether the average case or a small number of extreme cases dominates the bottleneck.

Once load is understood, ask two questions:

1. If load increases and resources remain unchanged, how does performance change?
2. If load increases and performance must stay unchanged, how many more resources are needed?

The usual goal is to meet SLO requirements while minimizing cost.

Linear scalability means doubling resources lets the system handle double the load while keeping performance the same. This is good, but cost often grows faster than linearly because of coordination, data size, bottlenecks, or inefficient scaling.

Source: PDF pages 74-75

## Shared-Memory, Shared-Disk, And Shared-Nothing Architectures

Vertical scaling, or scaling up, means moving to a more powerful machine with more CPU cores, RAM, and disk.

Shared-memory architecture uses parallelism within one machine. Threads in one process can access the same RAM. The downside is that high-end machines usually cost more than linearly, and bottlenecks may prevent twice the hardware from handling twice the load.

Shared-disk architecture uses multiple machines with independent CPU and RAM, but shared disk storage through NAS or SAN. This has been used in on-premises data warehouses, but locking and contention limit scalability.

Shared-nothing architecture, also called horizontal scaling or scaling out, uses multiple nodes, each with its own CPU, RAM, and disks. Coordination happens in software over a normal network.

Shared-nothing advantages:

- Potential for linear scalability.
- Can use hardware with good price/performance.
- Easier to add or remove resources as load changes.
- Greater fault tolerance across datacenters or regions.

Shared-nothing downsides:

- Requires explicit sharding.
- Inherits distributed systems complexity.

Some cloud-native databases separate storage and transaction execution. Multiple compute nodes share a specialized storage service. This resembles shared-disk but avoids some old scalability limits because the storage API is designed specifically for the database rather than as a generic filesystem or block device.

Source: PDF page 75

## Principles For Scalability

There is no universal scalable architecture. The chapter calls this the absence of "magic scaling sauce."

Example: a system handling 100,000 requests per second at 1 KB each is very different from one handling 3 requests per minute at 2 GB each, even though both move about 100 MB per second.

An architecture that works at one load level may not work at ten times that load. Fast-growing services may need architectural changes at every order-of-magnitude increase. It is usually not worth planning more than one order of magnitude ahead because application needs will change.

General scalability principle: break the system into smaller components that can operate mostly independently. This principle appears in microservices, sharding, stream processing, and shared-nothing architectures.

The hard part is deciding where to split: what belongs together and what should be separated.

Second principle: avoid unnecessary complexity. If a single-machine database works, it may be better than a distributed setup. Autoscaling is useful for variable load, but manual scaling may be simpler when load is predictable. Five services are simpler than fifty. Good architecture is usually a pragmatic mixture of approaches.

Source: PDF page 76

## Maintainability

Software does not wear out physically, but it changes continuously:

- Requirements evolve.
- Dependencies and platforms change.
- Bugs must be fixed.
- New use cases appear.
- Technical debt accumulates.
- Failures need investigation.

Most software cost is not initial development; it is ongoing maintenance.

Legacy systems are hard because old technologies, lost institutional knowledge, and human organizational dependencies accumulate over time. Any useful system may eventually become legacy software.

The chapter highlights three maintainability goals:

- Operability: make it easy to keep the system running.
- Simplicity: make it easy for engineers to understand the system.
- Evolvability: make it easy to change the system later.

Source: PDF pages 76-77

## Operability

Good operations can often work around incomplete software, but good software cannot run reliably with bad operations.

Automation is essential at large scale, but more automation is not always better. Rare edge cases still require skilled human intervention, and broken automation can be harder to troubleshoot than manual processes.

Good operability means routine tasks are easy so operations teams can focus on high-value work.

Data systems improve operability by:

- Supporting monitoring and observability.
- Avoiding dependence on individual machines.
- Providing clear documentation and an understandable operational model.
- Having good defaults while allowing administrators to override them.
- Offering self-healing where appropriate while preserving manual control.
- Behaving predictably and minimizing surprises.

Source: PDF pages 77-78

## Simplicity

As systems grow, complexity slows everyone down and raises maintenance cost. Complex systems are harder to reason about, increasing the risk of bugs when changes are made.

A badly complex system is sometimes called a big ball of mud.

Simplicity means solving the problem in the simplest reasonable way, but simplicity is subjective. One system may hide complex internals behind a simple interface; another may expose more details but have simpler internals.

The chapter discusses essential versus accidental complexity:

- Essential complexity is inherent in the problem.
- Accidental complexity arises from tooling or implementation limitations.

The distinction is imperfect because what counts as accidental changes as tools improve.

Abstraction is one of the best tools for managing complexity. Good abstractions hide implementation detail behind a simpler interface and can be reused across many applications.

Examples:

- High-level languages hide machine code, CPU registers, and system calls.
- SQL hides on-disk structures, in-memory structures, concurrency, and crash recovery details.

This book focuses on general-purpose abstractions for data systems, such as transactions, indexes, and event logs.

Source: PDF pages 78-79

## Evolvability

Requirements rarely stay fixed. Systems must adapt as teams learn new facts, users request features, business priorities change, platforms change, laws change, and growth forces architectural changes.

Agile practices help organizations adapt. TDD and refactoring help at the code level. The book is interested in agility at the level of data systems that may contain several applications or services.

Evolvability means making it easy to modify and extend a system for future, unanticipated needs.

Loose coupling, simplicity, and good abstractions improve evolvability. Tight coupling and complexity make change harder.

Irreversibility makes change risky. For example, during a database migration, if you cannot switch back to the old system when the new one has problems, the migration has high stakes. Minimizing irreversible decisions improves flexibility.

Source: PDF pages 79-80

## Chapter-Level Memory Hooks

- Functional requirements define what the system does; nonfunctional requirements define how well it must behave.
- The timeline case study is about trading read cost against write cost.
- Materialized views make reads fast by doing more work on writes.
- Fan-out is the multiplier from one request to many downstream updates.
- Response time is what the client sees; service time is only active processing.
- Queueing near capacity causes response time to rise sharply.
- Tail latency matters because one slow backend call can slow the whole user request.
- Faults are component problems; failures are user-visible service problems.
- Fault tolerance is always scoped to certain types and numbers of faults.
- Software faults are often correlated and therefore more dangerous than independent hardware faults.
- Human error is usually a symptom of a sociotechnical system problem.
- Scalability must be discussed with a specific workload and growth dimension.
- Shared-nothing systems scale horizontally but add distributed systems complexity.
- Maintainability means operability, simplicity, and evolvability.

## Interview Perspective

If asked "What is the difference between response time and latency?", answer:

Response time is the total time the client observes from request to response. Latency is time when the request is not actively being processed, such as network delay or waiting. Service time is active processing. Queueing delay can dominate response time, so client-side measurement is important.

If asked "Why use percentiles instead of averages?", answer:

Response times form a distribution. The mean hides whether most users are fast and a few are very slow. Median shows typical experience, while p95, p99, and p999 show tail latency. Tail latency matters because slow outliers affect real users and can be amplified when one user request depends on many backend calls.

If asked "How does materialization help timelines?", answer:

Instead of computing each timeline on every read, the system precomputes each user's timeline when posts are written. This makes reads fast but increases write work. It is a read-versus-write trade-off. Celebrity accounts and users following many accounts create extreme fan-out cases that need special handling.

If asked "How should scalability be discussed?", answer:

Do not say simply that a system scales or does not scale. Define the load parameter, such as requests per second, data volume, read/write ratio, or number of online users. Then ask how performance changes if load increases with fixed resources, or how resources must increase to keep performance unchanged.

If asked "What makes maintainability good?", answer:

A maintainable system is operable, simple, and evolvable. It is easy to monitor, operate, understand, and change. Good abstractions and loose coupling help; unnecessary complexity and irreversible decisions make maintenance harder.

## Final Takeaways

- Nonfunctional requirements are first-class design requirements, not optional extras.
- Performance must be measured with distributions, especially percentiles.
- Reliability means preventing component faults from becoming user-visible failures.
- Scalability is workload-specific and cost-sensitive.
- Maintainability is mostly about reducing future human and organizational pain.
- Good architecture is not maximum sophistication; it is the right trade-off for the current and near-future system.

Confidence: High
