# Chapter 1: Trade-Offs in Data Systems Architecture - Compressed Study Summary

Book: DDIA
Chapter: 1
Source PDF: books/DDIA/DDIA.pdf
Source pages: PDF pages 25-50
Method: Compressed from the PDF-derived chapter text section by section. This is not a verbatim extraction.

Chapter 1 teaches that data system architecture is a set of trade-offs. There is rarely one universally correct architecture; the right choice depends on workload, scale, operational skill, organization size, cost, legal duties, and user risk.

The chapter first defines data-intensive applications. These are systems where the main engineering challenge is data management: storing data, processing large volumes, handling changes, maintaining consistency under failures and concurrency, and keeping services available. Typical building blocks include databases, caches, search indexes, stream processing, and batch processing. Real applications often combine several of these tools, so the architect must understand their trade-offs.

The first major distinction is operational versus analytical systems. Operational systems are where data is created and modified by user-facing services. Analytical systems hold read-only copies or derived forms of that data for business analysts and data scientists. OLTP systems perform many small predefined point queries and updates. OLAP systems scan many records, compute aggregates, and support ad hoc exploration. Because these workloads differ in users, query patterns, schemas, performance needs, and security concerns, organizations often separate them.

Data warehouses were introduced to keep analytics away from production OLTP systems. They combine read-only data from many operational systems through ETL or ELT. This avoids data silos, protects operational performance, and gives analysts one place to query. HTAP systems try to combine transactional and analytical processing, but the OLTP/OLAP distinction still matters because many HTAP systems internally separate these functions.

Data lakes extend the analytics architecture by storing raw data in flexible file-based form rather than forcing a relational warehouse schema. This is useful for data science and ML workflows that need text, images, feature vectors, matrices, or other non-relational data. As analytics matures, organizations also need DataOps, governance, privacy controls, event streams, and sometimes reverse ETL where analytical outputs are sent back into operational systems.

The chapter also distinguishes systems of record from derived data. A system of record is the authoritative source of truth where facts are first written. Derived data is produced from other data and can be rebuilt if lost. Caches, indexes, materialized views, denormalized values, transformed datasets, and trained ML models are derived data. This distinction is about how the tool is used, not the tool itself.

The second major trade-off is cloud versus self-hosting. Cloud services outsource operation to a provider and can help teams move faster, especially when they lack operational expertise or have variable workloads. Self-hosting may be cheaper when teams already have expertise and workloads are predictable. Cloud downsides include reduced control, difficult debugging, vendor lock-in, outage dependence, geopolitical risk, and trust/compliance concerns.

Cloud-native architecture is more than running old software on cloud VMs. It means building systems around cloud primitives such as object storage, elastic compute, multitenancy, and disaggregated storage and compute. Cloud-native systems can scale and recover well, but the separation of storage and compute introduces network movement and new operational considerations. Cloud also changes operations: capacity planning becomes financial planning, and performance optimization becomes cost optimization.

The third major trade-off is distributed versus single-node systems. Distributed systems are useful for inherent multi-user communication, cloud service integration, high availability, scalability, global latency reduction, elasticity, specialized hardware, data residency, and sustainability. But distribution adds failure modes, slower network calls, unsafe retry problems, observability challenges, and harder consistency. The chapter warns not to rush into distribution when a single machine can handle the workload.

Microservices decompose a system into independently owned services with APIs. They help large teams move independently, assign resources per service, and hide implementation details. But they add deployment, testing, monitoring, logging, alerting, API evolution, and data consistency complexity. Microservices solve a people coordination problem by adding system coordination problems. Serverless further outsources infrastructure management and bills by execution, but it can impose runtime limits, cold starts, and environment restrictions.

The chapter compares cloud computing with supercomputing. Supercomputers target scientific batch computation with specialized networks and checkpoint-based recovery. Cloud systems usually target online services and business data systems that must remain continuously available, isolate mutually untrusting tenants, and sometimes span geographic regions.

Finally, the chapter argues that law and society are part of architecture. Systems storing personal data must consider GDPR, CCPA, AI regulation, privacy, ethics, and user harm. Legal requirements such as the right to erasure are technically difficult when systems use append-only logs, derived datasets, or ML training data. The chapter introduces data minimization: collect and retain only what is necessary for explicit purposes. Storage costs include not only infrastructure bills but also liability, reputational damage, legal fines, and safety risks to users.

Key memory line: architecture is the art of choosing the least bad trade-off for the actual workload, organization, and users.

Confidence: High
