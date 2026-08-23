# Chapter 7: CDNs and Caching

## Why caching works

A cache stores a reusable result closer to where it is needed. It can reduce latency, origin load, bandwidth cost, and blast radius during short origin failures.

Caching is a deliberate consistency trade-off: faster reads and reduced work in exchange for possible staleness and invalidation complexity.

A **CDN** is a geographically distributed edge network that proxies and commonly caches content near users. Modern CDNs can also terminate TLS, absorb attacks, execute edge logic, route traffic, and protect origins.

## Cache layers in a web system

```text
Browser cache
    ↓ miss
CDN edge cache
    ↓ miss
Reverse-proxy/application cache
    ↓ miss
Database/buffer cache
    ↓
Source of truth
```

Every layer needs its own key, freshness, eviction, and failure behavior. A “cache hit” at one layer may still be a miss at another.

## CDN request flow

1. DNS or anycast directs a client to a nearby edge point of presence (PoP).
2. The edge terminates the connection and checks security/policy.
3. It builds a cache key, usually from hostname, path, query rules, and selected request headers.
4. On a fresh hit, it returns the stored response.
5. On a miss or stale entry, it contacts an upstream shield or origin.
6. It may store an eligible response and return it to the client.

“Nearest” often means best according to network routing and measured performance, not shortest geographic distance.

## HTTP cache semantics

### Freshness

Important response directives include:

- `Cache-Control: max-age=300` — reusable for 300 seconds by caches allowed to store it.
- `s-maxage=300` — freshness for shared caches such as CDNs.
- `public` — response may be stored by shared caches.
- `private` — response is intended only for a private client cache.
- `no-store` — do not store the response.
- `no-cache` — storage is allowed, but it must be revalidated before reuse.
- `immutable` — content is expected not to change during freshness lifetime.
- `stale-while-revalidate` — serve stale briefly while refreshing in the background.
- `stale-if-error` — serve stale for a bounded period when the origin fails.

`no-cache` does not mean “never cache”; `no-store` is the directive for that.

### Validation

An origin can provide an `ETag` or `Last-Modified`. A stale cache asks conditionally:

```http
If-None-Match: "product-42-v7"
```

If unchanged, the origin replies `304 Not Modified` without the full body. Validation saves bandwidth but still requires a round trip.

### `Vary`

`Vary: Accept-Encoding` tells a shared cache that gzip and uncompressed variants are different. Varying on highly diverse values such as full cookies can destroy hit ratio and consume storage.

## Cache keys

A cache key must include every request property that can change the response, but no unnecessary high-cardinality properties.

Potential components:

- scheme/host/path;
- normalized query parameters;
- selected headers such as language or encoding;
- tenant, authorization scope, or feature variant where caching private data is explicitly safe.

A missing component can leak one user's data to another. Too many components fragment the cache and reduce benefit. Shared caches should normally bypass personalized or authenticated responses unless isolation is carefully designed.

## Caching strategies

### Cache-aside

The application checks cache, reads the database on miss, then fills the cache. It is simple and common, but concurrent misses can duplicate work and cached data can lag writes.

### Read-through

The cache abstraction loads missing data on behalf of the caller. This centralizes loading policy.

### Write-through

Writes update the cache and backing store synchronously. Reads are fresh, but writes have more latency and two-system failure cases.

### Write-behind

Writes reach cache first and backing storage later. It improves write latency but risks data loss and complex recovery; use only when the durability model permits it.

### Refresh-ahead

Popular entries refresh before expiry. It reduces user-visible misses but may refresh data nobody uses unless access popularity is considered.

## Eviction and expiration

Expiration is time-based invalidation. Eviction removes entries to stay within capacity.

Common policies include LRU (least recently used), LFU (least frequently used), FIFO, and size/TTL-aware variants. Real cache systems often approximate them for performance.

TTL should reflect acceptable staleness and recovery cost. Add randomized TTL jitter so millions of entries do not expire at the same instant.

## Invalidation

Famous because it is genuinely hard:

- **TTL only:** simplest; stale until expiry.
- **Explicit purge:** remove keys after a write; fast but must reach all layers.
- **Versioned keys:** change key when content changes; old data becomes unreachable and expires later.
- **Event-driven invalidation:** publish changes to cache nodes; scalable but delivery and ordering must be handled.

For static assets, use content-hashed filenames such as `app.a81f3c.js` with a very long TTL. A new deployment creates a new URL, avoiding purge races.

## Failure and overload patterns

### Cache stampede

A hot entry expires and many requests hit the origin simultaneously. Mitigations include request coalescing/single-flight, locks, refresh-ahead, TTL jitter, stale-while-revalidate, and origin concurrency limits.

### Cache penetration

Repeated requests for nonexistent keys always miss. Cache negative results briefly, validate requests, use rate limits, and consider probabilistic filters for suitable datasets.

### Cache avalanche

Many entries expire together or a cache fleet fails, overwhelming the origin. Use staggered expiration, multi-layer caches, capacity headroom, warm-up, and load shedding.

### Hot key

One object receives disproportionate traffic. Replicate it, use local near-caches, partition by an additional dimension when safe, or serve it at CDN edges.

### Cache outage

Treating every cache failure as a database fallback can take down the database. Bound fallback concurrency and shed optional traffic. Sometimes serving stale or a degraded response is safer.

## CDN architecture decisions

### Origin shielding

A shield cache sits between edge PoPs and the origin, combining misses so worldwide edges do not independently fetch the same object.

### Dynamic content

Even uncacheable APIs can benefit from nearby TLS termination, optimized backbone routing, connection reuse to origins, DDoS filtering, and edge authorization. But extra proxy hops can hurt if poorly routed.

### Purge behavior

Global purge is not always instantaneous. Decide whether the product accepts bounded staleness. Security-sensitive content may require versioning/revocation logic rather than relying only on CDN purge propagation.

### Multi-CDN

Multiple providers can improve resilience and reach but increase cost, routing complexity, inconsistent features, and cache fragmentation. Test failover and configuration parity.

## Measuring cache effectiveness

- **Request hit ratio:** hits divided by requests.
- **Byte hit ratio:** bytes served from cache divided by total bytes; important when object sizes vary.
- origin requests and egress bandwidth;
- hit latency, miss latency, and revalidation latency;
- eviction rate, memory use, and key cardinality;
- stale responses, purge completion, and errors;
- miss reason: cold, expired, evicted, bypassed, or key mismatch.

A high hit ratio can still hide poor performance if the largest or most expensive objects miss.

## System-design checklist

- Define the source of truth and acceptable staleness.
- Design and review cache keys as security-sensitive code.
- Choose write and invalidation behavior before adding the cache.
- Protect the origin from stampedes and cache failure.
- Use immutable versioned static assets when possible.
- Ensure sensitive and personalized responses are not publicly cached.
- Monitor both hit ratio and origin work saved.

## Quick review

1. What is the difference between `no-cache` and `no-store`?
2. Why can a wrong cache key become a data-leak vulnerability?
3. How does request coalescing prevent a stampede?
4. When is serving stale data during an error a good trade-off?

## Key takeaways

- Caches trade freshness and complexity for latency, capacity, and resilience.
- CDNs combine edge caching with routing, connection termination, and protection.
- Cache keys, invalidation, and origin protection determine correctness.
- Design explicitly for stampedes, penetration, avalanches, hot keys, and cache outages.
