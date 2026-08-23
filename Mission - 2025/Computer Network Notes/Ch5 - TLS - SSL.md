# Chapter 5: TLS and SSL

## What TLS provides

TLS protects application data over an untrusted network. Properly configured TLS provides:

- **Confidentiality:** observers cannot read the plaintext.
- **Integrity:** modification is detected.
- **Authentication:** the client can verify the server identity; mutual TLS can authenticate clients too.

SSL is the obsolete predecessor. Modern systems use TLS, even though terms such as “SSL certificate” remain common. SSLv2, SSLv3, and old TLS versions should not be treated as acceptable modern security.

TLS does not hide every detail. IP addresses, timing, packet sizes, and often the destination hostname can remain observable, depending on protocol and deployment.

## Cryptographic building blocks

### Symmetric encryption

The same secret key encrypts and decrypts. It is efficient for bulk data. Modern TLS uses authenticated encryption modes that provide confidentiality and integrity together.

### Asymmetric cryptography

A public/private key pair supports signatures and key agreement. It is more expensive, so TLS uses it during authentication and setup rather than for every application byte.

### Hashes and message authentication

Cryptographic hashes produce fixed-size fingerprints. TLS uses hash-based constructions in key derivation, transcript verification, and signatures.

### Key exchange and forward secrecy

Ephemeral Diffie–Hellman variants let peers derive the same session secret without sending it directly. With forward secrecy, later theft of the server's long-term private key does not decrypt previously recorded sessions.

## Certificates and PKI

An X.509 certificate binds a public key to identities and is signed by an issuer. A typical server sends its leaf certificate plus intermediate certificates. The client builds a chain to a trusted root certificate already in its trust store.

The client validates at least:

- the certificate chain and signatures;
- validity dates;
- the requested hostname against Subject Alternative Names;
- allowed key usages and constraints;
- revocation information where supported and available.

A valid signature is not enough—the hostname must match. A certificate for `example.com` does not automatically cover `api.example.com`; a wildcard such as `*.example.com` generally covers one label level.

## Simplified TLS 1.3 handshake

```text
Client                                             Server
  | ClientHello: versions, algorithms, key share,    |
  | SNI, ALPN --------------------------------------> |
  | <--- ServerHello: selected values, key share      |
  | <--- encrypted certificate, proof, Finished       |
  | verify chain + hostname                            |
  | Finished ---------------------------------------> |
  | ===== encrypted application data ===============> |
```

- **SNI** tells the server which hostname the client wants, allowing multiple certificates behind one IP. Traditional SNI can reveal the hostname; newer mechanisms aim to encrypt more of the client hello.
- **ALPN** negotiates the application protocol, such as HTTP/1.1 or HTTP/2.
- Handshake transcript verification detects tampering.

TLS 1.3 normally establishes a new secure session in one RTT after the underlying transport is ready. QUIC integrates the TLS 1.3 handshake.

## Session resumption and 0-RTT

After a successful connection, a client can use a session ticket or pre-shared key to resume with less work. This reduces latency and CPU.

TLS 1.3 early data (0-RTT) can be replayed by an attacker even if it cannot be read or modified. Use it only for replay-safe operations, and enforce application protections. Never assume encryption alone makes a payment mutation safe to replay.

## HTTPS request path and termination

```text
Client ==TLS 1==> CDN/load balancer ==TLS 2==> application
```

TLS may terminate at the edge proxy. The proxy decrypts, inspects, and routes the HTTP request. A separate TLS connection can protect the backend hop. Therefore “HTTPS enabled” is incomplete until the trust boundaries and every hop are understood.

Common patterns:

- **TLS termination:** proxy decrypts traffic and sends plaintext on a trusted internal network.
- **TLS re-encryption:** proxy starts a new TLS connection to the backend.
- **TLS passthrough:** load balancer forwards encrypted bytes; the backend terminates TLS.
- **Mutual TLS (mTLS):** both peers present identities, common between internal services.

## mTLS and service identity

mTLS can verify that a caller is an authorized workload, not merely that it can reach a private subnet. A service mesh may automate certificate issuance, rotation, and policy.

Authentication is not authorization. A valid service certificate identifies the peer; policy must still decide whether that peer may call a particular operation.

Operational costs include certificate lifecycle complexity, clock dependence, proxy resource usage, and difficult debugging when identity or trust bundles are wrong.

## Certificate lifecycle

A sound operational process includes:

1. prove control of the domain or service identity;
2. issue through an approved certificate authority;
3. store private keys in a protected key-management system;
4. deploy the full chain;
5. rotate automatically before expiration;
6. monitor expiry and handshake failures independently;
7. revoke/replace compromised credentials.

Short-lived certificates reduce exposure but require dependable automation. Always test renewal, not just initial issuance.

## Common mistakes

- disabling certificate validation to “fix” a connection;
- trusting any certificate signed by an internal CA without checking the service identity;
- serving an incomplete intermediate chain;
- using expired certificates or clocks that are badly wrong;
- enabling obsolete versions, weak ciphers, or compression mechanisms;
- putting secrets in URLs, logs, or plaintext after TLS termination;
- assuming a private network removes the need for encryption and authentication;
- failing to rotate keys and certificates.

## Performance considerations

TLS adds handshake round trips, CPU for cryptography, and some bytes. In most web systems the protection is worth the modest cost. Reduce cost safely through:

- persistent connections and HTTP/2 or HTTP/3 multiplexing;
- session resumption;
- efficient modern algorithms and hardware support;
- edge termination close to users;
- correctly sized connection pools.

Do not weaken verification for performance. Measure handshake latency, resumption rate, CPU, failures by reason, and certificate expiry.

## Debugging TLS

If TCP connects but TLS fails, check:

1. requested hostname and SNI;
2. certificate hostname and expiration;
3. complete certificate chain and trusted root;
4. compatible TLS versions and algorithms;
5. ALPN requirements;
6. client/server clock;
7. interception by a proxy or firewall;
8. whether mTLS requires a client certificate.

Typical tools are browser security panels, `curl -v`, and `openssl s_client`. Never paste production private keys into diagnostic tools.

## System-design decisions

- Define exactly where TLS starts and ends.
- Separate public certificate management from internal workload identity where appropriate.
- Encrypt backend hops when the threat model, regulation, or shared infrastructure requires it.
- Use centralized issuance with decentralized short-lived credentials to reduce manual work.
- Plan certificate and trust-bundle rotation without downtime; support overlap between old and new values.
- Treat TLS errors as availability signals and certificate expiry as a preventable incident.

## Quick review

1. Why does TLS use both asymmetric and symmetric cryptography?
2. What checks must a client perform besides verifying a certificate signature?
3. What can a load balancer see after terminating TLS?
4. Why is TLS 1.3 0-RTT unsafe for arbitrary mutations?

## Key takeaways

- TLS supplies encryption, integrity, and authenticated identity; it does not solve authorization or replay-safe business semantics.
- Certificates form a chain of trust and must match the requested hostname.
- TLS termination defines a real security boundary.
- Automated issuance, rotation, expiry monitoring, and strict verification are essential system-design concerns.
