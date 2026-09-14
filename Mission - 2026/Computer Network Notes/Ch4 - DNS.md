# Chapter 4: Domain Name System (DNS)

## DNS's role

DNS is a distributed, hierarchical database that maps names to typed records. Its most familiar job is resolving `api.example.com` to an IP address, but it also supports mail routing, service discovery, ownership verification, and policy publication.

DNS separates a service's stable name from changing server addresses. It is part of a system's control path: even when application servers are healthy, failed or stale DNS can make them unreachable.

## Names and hierarchy

Read a domain name from right to left:

```text
api.eu.example.com.
 |   |    |     |
host sub  domain root
```

- The invisible trailing dot is the DNS root.
- `.com` is a top-level domain (TLD).
- `example.com` is a registered domain and can be a DNS zone.
- `eu.example.com` may be a subdomain or a separately delegated zone.
- A **zone** is an administrative portion of the namespace, not necessarily the same as a domain.

## Resolution flow

When an application needs an address:

1. It asks the operating system's **stub resolver**.
2. Local browser/OS caches and the hosts file may answer.
3. Otherwise the request goes to a **recursive resolver**, commonly run by an ISP, company, or public provider.
4. On a cache miss, the resolver asks a root server where to find the TLD servers.
5. It asks a TLD server where to find the domain's authoritative server.
6. It asks the **authoritative server** for the record.
7. It caches the answer according to TTL and returns it to the client.

```text
Client → recursive resolver → root → TLD → authoritative server
             ↑                                      |
             └──────────── final cached answer ─────┘
```

The root and TLD usually return referrals, not the final host address. Cached referrals mean this entire walk is not performed for every lookup.

## Common record types

| Type | Meaning | Example use |
|---|---|---|
| A | Name to IPv4 address | `api` → `192.0.2.10` |
| AAAA | Name to IPv6 address | `api` → `2001:db8::10` |
| CNAME | Alias to another canonical name | `www` → CDN hostname |
| NS | Authoritative name servers for a zone | Delegation |
| MX | Mail exchangers with preference | Email delivery |
| TXT | Arbitrary text | Domain verification, SPF policy |
| SOA | Zone metadata and timing | Authority/serial information |
| PTR | Reverse lookup address to name | Diagnostics and mail checks |
| SRV | Service location with port/priority/weight | Some discovery systems |
| CAA | Which certificate authorities may issue certificates | PKI policy |

A CNAME is an alias and normally cannot coexist with other data at the same name. The zone apex needs records such as SOA and NS, so providers often offer non-standard `ALIAS`/`ANAME` flattening for apex-like behavior.

## TTL, caching, and staleness

Every answer has a **TTL** telling caches how long it may be reused. Caching reduces latency and authoritative-server load, but creates a propagation trade-off.

- Long TTL: efficient and resilient to brief authoritative outages, but changes are slower.
- Short TTL: faster planned changes, but more query load and resolver dependence.

Changing a TTL immediately does not shorten copies already cached with the old TTL. Before a migration, reduce the TTL early enough for old cached values to expire, make the record change, verify it, then raise the TTL later.

Resolvers can also cache negative answers such as “name does not exist.” This explains why a newly created record may still appear missing for a while.

## DNS transport

Traditional DNS usually uses UDP port 53 for small queries and responses and TCP when needed, including zone transfers and responses that do not fit. Extension mechanisms allow larger UDP messages, but large responses can be fragmented or abused for amplification.

Encrypted client-to-resolver options include:

- **DNS over TLS (DoT)**;
- **DNS over HTTPS (DoH)**;
- **DNS over QUIC (DoQ)**.

These protect the local DNS exchange from passive observers but do not automatically make every part of resolution private or make the answer trustworthy.

## DNS security

### DNSSEC

DNSSEC signs DNS data so a validating resolver can verify origin authenticity and detect tampering through a chain of trust. It does not encrypt queries or responses.

### Common risks

- cache poisoning and spoofed responses;
- dangling CNAMEs that allow subdomain takeover;
- unauthorized registrar or DNS account access;
- DDoS against authoritative infrastructure;
- open resolvers used for reflection/amplification;
- rebinding attacks that make a domain resolve to internal addresses.

Use protected registrar accounts, least privilege, change auditing, multiple authoritative locations/providers where justified, DNSSEC with careful key operations, and monitoring of critical records.

## DNS-based traffic management

Authoritative DNS can return different answers based on:

- health checks;
- weighted rollout percentage;
- geographic region or latency policy;
- active/standby failover;
- client/resolver location.

This is useful but not instant or perfectly precise. Caches can keep old answers, clients may continue existing connections, and the authoritative server often sees the recursive resolver rather than the exact client. DNS does not observe per-request backend load.

**Anycast** is different: many locations advertise the same IP, and network routing selects a path. Large DNS services often combine DNS hierarchy with anycast endpoints.

## DNS for service discovery

Internal systems can register service names such as `orders.service.internal`. DNS is simple and universally supported, but clients must handle multiple addresses, TTLs, removal of unhealthy instances, connection reuse, and possibly stale records.

Container platforms often expose stable service DNS while virtual networking/load balancing chooses individual instances. Public DNS should not leak sensitive internal topology.

## Failure scenarios

### DNS server or resolver outage

Cached answers may keep existing systems working until TTL expiry. Use redundant recursive and authoritative infrastructure and monitor resolution from multiple networks.

### Stale address after deployment

Removing the old endpoint too soon breaks clients holding cached data. Keep old and new endpoints available during at least the relevant TTL and connection-draining period.

### DNS works, application fails

Successful resolution proves only that a record was returned. The destination port, TLS certificate, route, load balancer, or application can still fail.

### Application works by IP but not by name

Investigate resolver configuration, record type, search suffix, cache, split-horizon DNS, and DNSSEC validation. Also remember that HTTPS certificates validate names, so testing by IP is not equivalent.

## System-design checklist

- Choose names that remain stable when infrastructure changes.
- Select TTLs based on failover needs and query cost, not a universal number.
- Plan migrations around cached answers and persistent connections.
- Make clients tolerate multiple A/AAAA addresses and partial failure.
- Avoid using DNS as the only real-time load-balancing mechanism.
- Monitor lookup success, authoritative health, latency, record correctness, and certificate alignment.
- Understand whether private/public “split-horizon” DNS returns different answers.

## Quick review

1. What is the difference between a recursive resolver and an authoritative server?
2. Why must TTL be lowered before, not during, a planned migration?
3. How do DNSSEC and DoH solve different problems?
4. Why is DNS failover not instantaneous?

## Key takeaways

- DNS is a cached hierarchical database, not just a name-to-IP lookup API.
- Recursive resolvers follow delegations; authoritative servers own zone data.
- TTL is a consistency-versus-efficiency trade-off.
- DNS traffic management is powerful but must account for caches, client behavior, and connection lifetime.
