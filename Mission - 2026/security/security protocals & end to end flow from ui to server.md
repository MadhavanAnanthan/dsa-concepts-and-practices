# Web Application Security Master Notes

> **Learning goal:** Understand how a user moves from opening a web application URL to being authenticated by an Identity and Access Management (IAM) provider and authorized by a Spring Boot API.
>
> **Reference architecture:** Browser/UI + Microsoft Entra ID + Spring Boot + Spring Security OAuth2 Resource Server. The same standards can be implemented with Keycloak, Okta, Auth0, AWS Cognito, Ping Identity, Google Identity, and other compatible providers.

---

## 1. The Fundamental Security Questions

### 1.1 Authentication: Who are you?

Authentication verifies an identity.

Examples:

- Username and password
- Microsoft corporate SSO
- MFA or authenticator approval
- Passkey, fingerprint, or security key

Result:

```text
The system has verified that the current user is Madhavan.
```

Authentication is commonly abbreviated as **AuthN**.

### 1.2 Authorization: What are you allowed to do?

Authorization happens after authentication and decides whether the authenticated identity may perform an action.

Examples:

- A normal user can view applications.
- An administrator can create or delete applications.
- A release manager can approve a release.

Authorization is commonly abbreviated as **AuthZ**.

### 1.3 The critical distinction

```text
Authentication = establish the identity
Authorization  = enforce permitted actions
```

A user can be correctly authenticated but still access unauthorized data if the application forgets to enforce authorization.

---

## 2. HTTP, Headers, Cookies, and Browser Storage

### 2.1 HTTP request structure

A simplified API request consists of:

```text
Request method and path
Headers
Optional body
```

Example:

```http
GET /api/applications HTTP/1.1
Host: app.example.com
Accept: application/json
Authorization: Bearer <token>
```

Headers are key-value metadata. Security information commonly travels in:

```http
Authorization: Bearer <token>
Cookie: JSESSIONID=<session-id>
```

### 2.2 Cookie

A cookie is small browser-managed data associated with a domain.

For session authentication, the server creates a session and responds:

```http
Set-Cookie: JSESSIONID=ABC123; Secure; HttpOnly; SameSite=Lax
```

The browser stores it and automatically sends it to the matching domain:

```http
Cookie: JSESSIONID=ABC123
```

The browser does not invent the server's session identifier. The server creates it and instructs the browser to store it.

Useful cookie controls:

- `Secure`: send only over HTTPS.
- `HttpOnly`: JavaScript cannot read the cookie.
- `SameSite`: controls cross-site cookie sending and helps reduce CSRF risk.
- `Domain`, `Path`, and expiry: control where and how long the cookie applies.

### 2.3 Local storage

JavaScript may store data using `localStorage`.

```javascript
localStorage.setItem("token", token);
```

Characteristics:

- Survives browser restart until removed.
- Not automatically attached to HTTP requests.
- JavaScript must read it and construct the `Authorization` header.
- JavaScript-readable tokens are exposed if an attacker successfully executes script in the page through XSS.

### 2.4 Session storage

`sessionStorage` is similar to local storage, but is scoped to a browser tab/session and is normally cleared when that tab closes.

### 2.5 Storage does not itself create security

A cookie, local storage, or session storage is only a storage/transport mechanism. Security depends on token/session design, HTTPS, expiry, validation, browser protections, and application authorization.

---

## 3. Traditional Session-Based Authentication

### 3.1 End-to-end flow

```text
1. Browser submits username and password to the application.
2. Server validates the credentials.
3. Server creates a session record.
4. Server returns the session ID in Set-Cookie.
5. Browser stores the cookie.
6. Browser automatically sends the cookie on later requests.
7. Server looks up the session and reconstructs the user's identity and roles.
```

Server-side state might look like:

```text
ABC123 -> user=Madhavan, roles=[USER]
```

The browser generally stores only `ABC123`, not the complete session object.

### 3.2 Why it is stateful

The server or a shared session store must remember the mapping between the session ID and user state.

### 3.3 What if either side loses the session?

- **Browser cleared the cookie:** the next request has no session ID, so the user must authenticate again. The server then creates a new session and cookie.
- **Server lost the session:** the browser may still send the old cookie, but the server cannot find it. The application rejects the request or redirects to login.

### 3.4 Challenge in distributed systems

With multiple application instances:

```text
Browser -> Load balancer -> Server A / Server B / Server C
```

If the session exists only in Server A's memory and the next request reaches Server B, Server B cannot find it.

Common solutions:

1. **Sticky sessions:** route the user repeatedly to the same server. Failover and scaling become more complicated.
2. **Shared session store:** place sessions in Redis or another shared store. This works, but adds a dependency and lookup.
3. **Self-contained token model:** each instance validates the presented token without looking up an application session.

Session authentication is not obsolete. A well-designed server-side session with secure cookies remains a strong option, especially for traditional server-rendered applications and Backend-for-Frontend designs.

---

## 4. HTTPS, TLS, and Certificates

### 4.1 HTTPS

```text
HTTPS = HTTP transported through TLS
```

TLS provides:

- **Confidentiality:** traffic is encrypted.
- **Integrity:** modification in transit can be detected.
- **Server authentication:** the browser verifies the server's certificate and hostname.

### 4.2 Certificates

A TLS certificate associates a domain name with a public key and is signed by a trusted Certificate Authority (CA). The browser checks that the certificate is valid, trusted, and appropriate for the requested hostname.

Installing a certificate enables authenticated, encrypted transport. It does **not** automatically fix application vulnerabilities such as SQL injection, broken access control, insecure token handling, or weak business rules.

### 4.3 SSL versus TLS

SSL is the older predecessor. People still casually say “SSL certificate,” but modern HTTPS uses TLS.

---

## 5. Common Application Security Failures

### 5.1 SQL injection

The vulnerability occurs when untrusted input is concatenated into SQL and is interpreted as SQL logic rather than data.

Unsafe example:

```java
String sql = "SELECT * FROM users WHERE username='" + username + "'";
```

If an attacker submits specially constructed input, the resulting SQL expression may change the query's meaning.

The important mental model is:

```text
Expected: user supplies data
Vulnerable behavior: user input becomes executable query syntax
```

Protection:

- Use parameterized queries or prepared statements.
- Use JPA/repositories correctly without concatenating untrusted input.
- Apply least-privilege database accounts.
- Validate input, but do not treat validation as a replacement for parameterization.

### 5.2 Broken authentication

Authentication is broken when an attacker can impersonate another identity because identity proof or session handling is flawed.

Examples:

- Predictable session identifiers
- Weak or reusable password-reset tokens
- Reset links that remain valid too long or after use
- Missing MFA for sensitive operations
- Session fixation or failure to invalidate sessions

If a valid password-reset link reaches another person and the application does not add sufficient expiry, one-time use, or verification controls, that person may be able to reset the password.

### 5.3 Authorization bugs / broken access control

The identity and role can be completely correct, while the application forgets to enforce the required permission.

Example:

```java
@GetMapping("/admin/users")
public List<User> getAllUsers() {
    return userService.findAll();
}
```

If this endpoint has no applicable authorization rule, a normal authenticated user may reach an admin operation.

Correct role data from Entra or a database is not enough. Every protected operation must enforce an authorization decision.

### 5.4 Typical response meanings

- `401 Unauthorized`: authentication is missing or invalid.
- `403 Forbidden`: authentication succeeded, but the caller lacks permission.

---

## 6. IAM: Identity and Access Management

An IAM platform centralizes capabilities such as:

- User and service identities
- Authentication and MFA
- Groups and roles
- SSO
- Token issuance
- Application registrations
- Federation
- Conditional-access policies

Common providers:

- Microsoft Entra ID
- Keycloak
- Okta
- Auth0
- AWS Cognito
- Ping Identity
- Google Identity Platform
- ForgeRock / Ping Identity products

OIDC and OAuth 2.0 are standards. Therefore, a solution can usually preserve the same overall architecture while replacing Entra with another compliant provider. Configuration details, claims, role mapping, URLs, and provider-specific behavior still differ.

---

## 7. OAuth 1.0, OAuth 2.0, and OIDC

### 7.1 OAuth 1.0

OAuth 1.0 was an earlier delegated-authorization protocol. It required request signing and was more complex for clients.

### 7.2 OAuth 2.0

OAuth 2.0 is an authorization framework. It defines how a client obtains and presents limited access to protected HTTP resources, either on behalf of a user or on its own behalf.

It does not by itself define the login identity information that a client needs for modern SSO.

### 7.3 OpenID Connect (OIDC)

OIDC is an identity layer built on OAuth 2.0. It introduces authentication semantics, the `openid` scope, and the ID token.

Clean mental model:

```text
OAuth 2.0 = delegated API access and access tokens
OIDC       = user authentication and ID tokens
JWT        = a token representation format
```

OIDC and OAuth 2.0 are not servers. They are protocols/rules implemented by applications and IAM providers.

---

## 8. OAuth 2.0 Actors

1. **Resource owner:** normally the user who owns or controls data.
2. **Client:** the application requesting access, such as the HawkEye UI.
3. **Authorization server / OpenID Provider:** authenticates the user, collects consent or applies policy, and issues tokens. Example: Entra.
4. **Resource server:** the protected API. Example: the Spring Boot HawkEye API.

Example:

```text
Resource owner      = Madhavan
Client              = HawkEye UI
Authorization server= Microsoft Entra ID
Resource server     = HawkEye Spring Boot API
```

---

## 9. Entra Application Registration

Before an application can use Entra, it is registered.

Typical registration/configuration includes:

- **Client ID:** identifies the application.
- **Tenant ID:** identifies the Entra tenant/directory.
- **Redirect URIs:** approved destinations to which Entra may redirect after authorization.
- **API scopes / delegated permissions:** permissions a client may request on a user's behalf.
- **Application permissions:** permissions used by a service acting without a user.
- **App roles:** application-defined roles assignable to users, groups, or applications.
- **Client credentials:** secrets or certificates used by confidential clients, when applicable.

The `client_id` is not a secret. A secret must not be embedded in browser code.

### 9.1 Redirect URI

After sign-in, Entra redirects the browser only to a registered redirect URI, for example:

```text
https://app.example.com/auth/callback
```

The redirect commonly carries an authorization code. It should not be arbitrarily accepted from the browser because an unrestricted redirect could send codes or tokens to an attacker-controlled location.

---

## 10. JWT: Token Format, Not a Protocol

JWT is a compact token format:

```text
base64url(header).base64url(payload).signature
```

### 10.1 Header

Often includes:

```json
{
  "typ": "JWT",
  "alg": "RS256",
  "kid": "signing-key-id"
}
```

- `alg`: signing algorithm.
- `kid`: identifies the signing key whose public part can verify the signature.

### 10.2 Payload / claims

Possible claims:

```json
{
  "iss": "issuer",
  "aud": "intended-audience",
  "sub": "subject-identifier",
  "iat": 1700000000,
  "nbf": 1700000000,
  "exp": 1700003600,
  "name": "Example User",
  "groups": ["group-a"],
  "roles": ["ADMIN"],
  "scp": "applications.read"
}
```

Important claims:

- `iss`: who issued the token.
- `aud`: who the token is intended for.
- `sub`: subject identifier.
- `iat`: issued-at time.
- `nbf`: not valid before this time.
- `exp`: expiration time.
- `roles`, `groups`, `scp`: possible authorization data, depending on configuration and token type.

A JWT need not contain every user detail. Claims should be limited to what is required. A signed JWT is normally readable by anyone who obtains it; signing provides integrity/authenticity, not confidentiality. JWT can also be encrypted using JWE, but that is a separate option.

### 10.3 Signature

The issuer signs using a private key. The receiver validates with the corresponding public key.

If someone changes the payload without possession of the private key, signature validation fails.

### 10.4 JWT is not a fourth token

```text
Authorization code = temporary grant/code
ID token           = a token, always JWT in OIDC
Access token       = a token; may be JWT or opaque
JWT                = format, not an additional token
```

---

## 11. ID Token, Access Token, Refresh Token, and Authorization Code

### 11.1 ID token

Purpose:

```text
Prove the authentication event and communicate identity claims to the client.
```

It is intended for the OIDC client, whose identifier is normally represented by `aud`. It is always a JWT in OIDC.

Typical identity-oriented claims include `sub`, `name`, `preferred_username`, and `nonce`.

### 11.2 Access token

Purpose:

```text
Call a protected resource/API.
```

It is normally sent as:

```http
Authorization: Bearer <access-token>
```

The access token's audience should identify the API/resource for which it was issued. It may contain scopes, roles, or other claims. It can be JWT-formatted or opaque.

### 11.3 Refresh token

A refresh token lets a client request a new access token without asking the user to perform a full interactive login every time. It is sensitive, normally longer-lived than an access token, and must be stored and handled carefully.

### 11.4 Authorization code

A short-lived, single-use intermediate code returned to the registered redirect URI. The client exchanges it at the token endpoint for tokens.

It is not normally a JWT and is not sent to the application API as the Bearer credential.

### 11.5 Correct relationship

```text
Authorization code
        |
        | exchanged at token endpoint
        v
ID token + access token + optional refresh token
```

The ID token and access token are separate. If both are JWTs, each has its own header, payload, and signature.

---

## 12. Bearer Token Meaning

`Bearer` is the HTTP authorization scheme used to present a token:

```http
Authorization: Bearer <token-value>
```

A bearer credential is usable by whoever possesses it. Therefore:

- Always protect it with HTTPS.
- Never place it in logs, tickets, chat, source code, or screenshots.
- Keep its lifetime appropriately short.
- Do not send it to unintended domains.

The bearer scheme is not another token type. It is how the token is presented to an API.

---

## 13. Authorization Code Flow with PKCE

### 13.1 End-to-end flow

```text
1. User opens the application URL.
2. The host returns the UI over HTTPS.
3. UI determines that no valid login state exists.
4. UI generates a PKCE code_verifier.
5. UI derives code_challenge = BASE64URL(SHA256(code_verifier)).
6. Browser redirects to Entra's authorization endpoint with:
   - client_id
   - redirect_uri
   - response_type=code
   - scope including openid
   - state
   - nonce (OIDC)
   - code_challenge
   - code_challenge_method=S256
7. Entra authenticates the user, possibly using an existing Entra SSO session and MFA/policy.
8. Entra redirects the browser to the registered callback with a short-lived authorization code and state.
9. Client verifies state.
10. Client sends authorization code + code_verifier to Entra's token endpoint.
11. Entra hashes the verifier and compares it with the challenge associated with the code.
12. If valid, Entra returns the applicable tokens.
13. Client validates/uses the ID token for the OIDC login context.
14. UI normally sends the access token to the protected API as a Bearer token.
15. Spring Security validates and authorizes the API request.
```

### 13.2 Why PKCE exists

Without PKCE, an intercepted authorization code might be exchanged by an attacker. PKCE cryptographically binds the authorization request and token exchange.

```text
Client retains: code_verifier
Entra receives initially: code_challenge
Entra returns: authorization code
Client exchanges: code + original code_verifier
Entra verifies: SHA256(verifier) matches stored challenge
```

Entra does not need to return the challenge to the browser. It associates the challenge with the authorization transaction/code.

### 13.3 `state` versus `nonce` versus PKCE

- `state`: correlates request/response and protects the client from request-forgery/login-confusion attacks when correctly generated and verified.
- `nonce`: binds an OIDC authentication request to the ID token and helps prevent replay/substitution.
- PKCE: binds the authorization code to the client instance that created the verifier.

They solve related but different problems.

---

## 14. Browser SSO: How the Browser/Entra Knows the User

When the user first opens the application, the application may not know the identity.

After redirect to Entra, one of two things happens:

1. Entra asks for credentials/MFA.
2. The browser already has a valid Entra SSO cookie, so Entra can reuse that authentication session subject to policy.

The application UI does not need to collect or forward the corporate password. Credentials are entered at or handled by Entra.

After authentication, Entra creates the authorization response and later issues tokens through the token endpoint.

---

## 15. Scopes, Roles, Groups, Claims, and Permissions

### 15.1 Claims

Claims are statements carried by a token, such as issuer, subject, tenant, groups, roles, scopes, and expiry.

### 15.2 Scope

A scope represents delegated access requested by a client, commonly on behalf of a user.

Examples:

```text
applications.read
applications.write
User.Read
```

The authorization server issues only scopes that are valid for the client, resource, consent, user, and policy context.

### 15.3 App role

An app role is an application-specific role such as:

```text
ADMIN
RELEASE_MANAGER
VIEWER
```

Roles can be assigned to users/groups, and application permissions can be assigned to service principals depending on the provider and flow.

### 15.4 Group

A token may carry directory group identifiers/names. The application can map those groups to authorities or personas.

Be careful with large group memberships because providers may use overage indicators instead of placing every group in the token.

### 15.5 Application database alternative

The token can identify the user while the application loads authorization data from MongoDB, SQL, Redis, or another store.

```text
Validate token -> obtain stable user ID -> load current app permissions -> authorize
```

Trade-offs:

- Token-contained roles: fast and stateless, but changes may not take effect until a new token is issued.
- Database permissions: changes can take effect immediately, but add a lookup/dependency.
- Cached hybrid: validate locally, then load app-specific permissions from a cache/data source.

### 15.6 Custom headers such as `X-USER-ROLES`

These are not OAuth standards. A trusted gateway may validate a token and add identity headers for an internal backend, but the backend must accept them only from the trusted gateway and prevent callers from bypassing or spoofing it.

---

## 16. Client Credentials Flow

Used for machine-to-machine access when no end user is participating.

```text
Backend service
   -> authenticates to authorization server
   -> receives access token
   -> calls resource server
```

The client uses a protected credential such as a secret, certificate, or private-key assertion. The resulting authorization represents the application/service, not a logged-in user.

Example:

```text
HawkEye scheduled service -> protected downstream API
```

Do not put a client secret in a browser SPA.

---

## 17. Public Keys, JWK, JWKS, and Local JWT Validation

### 17.1 Private/public keys

```text
IAM private key -> signs token
Public key      -> verifies signature
```

The API never needs Entra's private signing key.

### 17.2 JWK and JWKS

- **JWK:** JSON representation of one cryptographic key.
- **JWKS:** JSON Web Key Set containing one or more public keys.

Multiple keys support key rotation.

### 17.3 Discovery

Given an `issuer-uri`, Spring Security can use the provider metadata/discovery endpoint to find the JWKS URI and validation information.

Conceptually:

```text
issuer-uri
   -> provider metadata
   -> jwks_uri
   -> public signing keys
```

### 17.4 Validation per request

The UI does **not** send a public key. It sends a signed JWT.

```text
1. Spring extracts the Bearer value.
2. Spring reads the JWT header, including kid and alg.
3. Spring selects the matching trusted public key from the JWKS-backed cache.
4. Spring verifies the signature.
5. Spring validates time claims such as exp and nbf.
6. Spring validates the configured issuer.
7. The API should validate audience so a token created for another resource is rejected.
8. Spring maps token claims to authorities.
9. Spring creates an Authentication object for the request.
10. Authorization rules decide whether the endpoint may execute.
```

### 17.5 Does Spring call Entra for every request?

For a JWT resource server, signature and claim validation are normally local using cached public keys. Spring may retrieve/refresh the JWKS as needed, including when signing keys rotate, rather than introspecting every JWT request.

This enables stateless request authentication:

```text
Every request carries the evidence needed for local validation.
The API does not maintain a user session merely to identify the caller.
```

### 17.6 Key rotation

The token's `kid` identifies the signing key. When a new `kid` is encountered or cached keys require refresh, the decoder can refresh from the JWKS endpoint. Applications must allow outbound access to the provider endpoints required by their configuration.

---

## 18. Spring Security Resource Server

Typical dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Typical issuer configuration:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://idp.example.com/issuer
```

Conceptual filter flow:

```text
HTTP request
 -> Spring Security filter chain
 -> Bearer token extraction
 -> JwtDecoder
 -> signature and claim validation
 -> token-to-authorities conversion
 -> JwtAuthenticationToken
 -> SecurityContext for current request
 -> URL/method authorization
 -> controller
```

`JwtAuthenticationToken` is created for an authenticated request, not globally for all users at application startup.

Example authorization rules:

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/public/**").permitAll()
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
);
```

Method authorization:

```java
@PreAuthorize("hasAuthority('SCOPE_applications.read')")
```

Provider claim names vary. A custom `JwtAuthenticationConverter` may be needed to map `groups`, `roles`, or another claim into Spring `GrantedAuthority` values.

### 18.1 Authentication versus authorization inside Spring

```text
JWT passed cryptographic/claim validation -> authenticated principal created
Endpoint rule/role/scope check            -> authorization decision
```

A valid token does not automatically authorize every API.

---

## 19. HawkEye-Specific Observation: ID Token Sent to the API

During this learning session, the observed HawkEye Bearer JWT appeared identity-oriented because it included claims such as:

```text
name
email
preferred_username
nonce
groups
```

and did not show a usual delegated `scp` claim. This is consistent with an ID-token-like payload, but token purpose should be verified from the application's Entra registration, requested response, audience, and authentication library rather than inferred only from claim appearance.

### 19.1 Important standards guidance

The normal standards-aligned design is:

```text
ID token     -> consumed by the OIDC client to establish UI login
Access token -> sent to and validated by the protected API
```

Microsoft explicitly advises not to use an ID token to call an API or for API authorization. Therefore, HawkEye's current use can function technically because Spring can validate a signed JWT and read its groups, but it should be treated as a project-specific/legacy design to review, not as the recommended reusable design.

### 19.2 Why it can still technically work

A generic JWT decoder can verify cryptographic validity and claims if configured to accept the token's issuer/audience. The application can then map identity/group claims and authorize requests.

But “cryptographically valid” does not automatically mean “correct token type for this API.” Correct audience and token-purpose enforcement are part of secure design.

### 19.3 Recommended architecture for new/refactored services

```text
UI performs OIDC sign-in
UI receives/maintains login state
UI obtains an access token whose audience is the HawkEye API
UI sends Authorization: Bearer <access-token>
API validates issuer, audience, signature, times, and authorization claims
```

If the application uses a Backend-for-Frontend, the browser can instead hold a secure session cookie while the BFF safely manages tokens server-side.

---

## 20. Stateless Does Not Mean “No State Anywhere”

JWT-based API authentication is described as stateless because the resource server does not require a per-user application session lookup on every request.

State can still exist in:

- Entra SSO cookies
- Browser/client token cache
- Refresh-token records or policies
- Consent and app registrations
- User/group directory
- Application authorization database
- Revocation/deny lists
- Audit logs

JWT local validation also creates a revocation trade-off: a previously issued token may remain accepted until expiry unless the architecture adds mechanisms such as short lifetimes, Continuous Access Evaluation where supported, a deny list, or opaque-token introspection.

---

## 21. Alternative Authentication and Authorization Mechanisms

### 21.1 Secure server-side session cookie

Best fit:

- Traditional server-rendered applications
- Backend-for-Frontend patterns
- Applications that prefer server-controlled session revocation

It remains a valid modern approach when cookie and CSRF protections are correctly implemented.

### 21.2 SAML 2.0

SAML is an XML-based federation/SSO standard.

```text
Identity Provider -> signed SAML assertion -> Service Provider
```

Common in older enterprise SaaS and corporate SSO integrations. It is browser/federation-focused and less natural than OAuth 2.0 access tokens for modern REST APIs.

### 21.3 LDAP / Active Directory

LDAP is a protocol for accessing directory services containing users, groups, and organizational data. Applications may bind to LDAP to verify credentials or query attributes.

LDAP is not a direct replacement for OAuth 2.0 API delegation. An IAM provider can authenticate against an LDAP/Active Directory directory and then issue OIDC/OAuth tokens to applications.

### 21.4 API keys

Simple credential for identifying a calling application or project.

```http
X-API-Key: <key>
```

Useful for controlled service integrations, metering, or low-complexity APIs. It usually lacks rich user identity, delegated consent, token audience, and standardized scopes unless additional mechanisms are built around it.

### 21.5 Mutual TLS (mTLS)

Both client and server present certificates. Strong for service identity and high-trust B2B/financial integrations. It can complement OAuth 2.0 rather than replace it.

### 21.6 HTTP Basic authentication

Sends a username/password-like credential in each request and must be protected using HTTPS. Simple, but generally unsuitable for modern federated user SSO and delegated access.

### 21.7 Opaque bearer tokens and introspection

Instead of a self-contained JWT, the access token can be an opaque random value.

```text
API receives opaque token
 -> calls authorization server introspection endpoint
 -> receives active/inactive status and metadata
```

Pros:

- Centralized validation and easier immediate revocation semantics.

Cons:

- Network dependency and latency for validation unless carefully cached.

### 21.8 Signed requests / proof-of-possession

Bearer tokens are usable by whoever holds them. Stronger designs can bind tokens/requests to a key or certificate using mechanisms such as mTLS or DPoP, reducing usefulness of a stolen token.

---

## 22. Why OAuth 2.0 + OIDC Became Widely Adopted

They separate concerns and enable interoperability:

```text
IAM provider handles authentication, MFA, SSO, and token issuance.
Client follows a standard flow instead of collecting corporate passwords.
Resource server validates standardized credentials and claims.
Applications request limited access rather than sharing master credentials.
```

The same broad model works across browsers, mobile apps, APIs, microservices, enterprise SSO, and third-party delegated access.

Because these are standards, application architecture can be replicated across Entra, Keycloak, Okta, Auth0, Cognito, and compatible products. Provider migration still requires claim mapping, registration, policy, URL, SDK, and operational changes.

---

## 23. Complete End-to-End HawkEye-Style Flow

```text
A. Page loading
1. User enters the HawkEye URL.
2. DNS/load balancer/hosting routes the request to the web application.
3. TLS authenticates the host and encrypts browser-server traffic.
4. Host returns the UI with HTTP 200.

B. Login initiation
5. UI discovers no usable app login state and offers SSO.
6. UI creates state, nonce, PKCE verifier, and PKCE challenge.
7. Browser redirects to Entra with client ID, registered redirect URI,
   requested scopes, state, nonce, and PKCE challenge.

C. Entra authentication
8. Entra identifies the tenant/application registration.
9. Entra authenticates the user or reuses an existing SSO session.
10. Entra applies applicable MFA/conditional-access policy.
11. Entra sends a short-lived authorization code to the registered callback.

D. Token exchange
12. Client verifies state.
13. Client sends code + PKCE verifier to Entra's token endpoint.
14. Entra verifies the code, redirect URI, client, and PKCE binding.
15. Entra returns an ID token and access token as applicable,
    plus an optional refresh token depending on client/flow/policy.

E. UI identity and API call
16. Client uses the ID token/login result to establish the UI identity.
17. UI calls the protected API with:

    Authorization: Bearer <access-token>

18. In the currently observed HawkEye implementation, an ID-token-like JWT
    appears to be used instead. This functions as project-specific behavior,
    but the recommended API credential is an access token intended for that API.

F. Spring validation
19. Spring Security's filter chain extracts the Bearer token.
20. JwtDecoder reads the JWT header and selects a trusted JWKS public key by kid.
21. It verifies the signature and validates time/issuer constraints.
22. The API validates that the audience identifies HawkEye API.
23. Claims are converted to an authenticated principal and authorities.
24. Spring stores JwtAuthenticationToken in the request's SecurityContext.

G. Authorization and response
25. URL/method rules check role, group, scope, or an application permission.
26. Missing/invalid authentication results in 401.
27. Authenticated but insufficient permission results in 403.
28. If allowed, the controller and business logic execute.
29. The API returns the response over the encrypted TLS connection.
```

---

## 24. Security Design Checklist

### Transport

- Enforce HTTPS.
- Use valid certificates and modern TLS configuration.
- Mark session cookies `Secure`, `HttpOnly`, and with an appropriate `SameSite` value.

### OIDC/OAuth client

- Use Authorization Code Flow with PKCE.
- Use exact registered redirect URIs.
- Generate and verify `state` and OIDC `nonce`.
- Never embed a confidential client secret in SPA/browser code.
- Store tokens according to the architecture's threat model; consider a BFF and secure cookie for high-sensitivity apps.

### Token/API validation

- Validate signature using trusted algorithms/keys.
- Validate `iss`, `aud`, `exp`, and `nbf` as applicable.
- Accept the correct token type intended for the API.
- Do not use an ID token as a substitute for an API access token in new designs.
- Do not log Bearer tokens.
- Keep tokens short-lived and plan for key rotation.

### Authorization

- Protect every endpoint by default and explicitly permit public endpoints.
- Enforce authorization server-side, not only by hiding UI buttons.
- Map roles/groups/scopes deliberately.
- Consider object-level checks, such as whether the user may access this particular FI/application record.
- Review role changes, token staleness, and emergency revocation requirements.

### Application security

- Use parameterized database queries.
- Protect against XSS and CSRF according to storage/session architecture.
- Apply secure password-reset and session invalidation design.
- Rate-limit sensitive endpoints.
- Log security events without logging credentials or tokens.
- Follow least privilege for users, service identities, and database accounts.

---

## 25. Common Misconceptions Corrected

| Misconception | Correct understanding |
|---|---|
| JWT is authentication | JWT is a format for carrying claims. |
| JWT is always encrypted | JWT is commonly signed, not encrypted; payload can be decoded. |
| ID token and access token are inside one JWT | They are separate tokens; each may be its own JWT. |
| Authorization code is another JWT | It is normally a temporary opaque code exchanged for tokens. |
| OAuth 2.0 authenticates the user | OAuth 2.0 is an authorization framework; OIDC adds standardized authentication. |
| Spring asks Entra about every JWT request | JWT verification is normally local using cached JWKS public keys. |
| UI sends the public key | UI sends the signed token; Spring already trusts the issuer's public keys. |
| A valid token may call every API | The API must verify audience and required roles/scopes/permissions. |
| Correct roles in Entra prevent authorization bugs | The application must still enforce those roles on every protected operation. |
| JWT completely replaces sessions | JWT is one architecture; secure sessions/BFF remain valid and often preferable. |
| ID token can normally be used to call APIs | The intended API credential is an access token. |

---

## 26. Interview-Ready Explanation

> The application uses an IAM provider such as Microsoft Entra ID for SSO. The browser starts an OpenID Connect Authorization Code Flow with PKCE. Entra authenticates the user and returns a short-lived authorization code to the registered callback. The client exchanges the code and PKCE verifier for an ID token and an API access token. The ID token establishes the user's login context in the client, while the access token is sent to the Spring Boot resource server using the Bearer authorization scheme. Spring Security discovers or is configured with the issuer's JWKS public keys, validates the JWT signature and claims such as issuer, audience, expiry, and not-before time, creates a `JwtAuthenticationToken`, maps scopes/roles/groups to authorities, and applies endpoint authorization rules. Because JWT validation is local, the API does not need to call Entra or maintain a server-side user session for each request. Entra can be replaced by another standards-compatible IAM product such as Keycloak, Okta, Auth0, or AWS Cognito, with provider-specific configuration and claim mapping changes.

---

## 27. Recommended Learning Sequence

1. Authentication versus authorization
2. HTTP requests, headers, cookies, and browser storage
3. Session-based authentication
4. HTTPS, TLS, certificates
5. Common vulnerabilities and server-side authorization
6. IAM, users, groups, roles, and app registration
7. OAuth 2.0 actors and access tokens
8. OIDC and ID tokens
9. Authorization Code Flow with PKCE
10. JWT claims and signatures
11. JWK/JWKS and key rotation
12. Spring Security filter chain and claim-to-authority mapping
13. Client Credentials Flow and service identities
14. Refresh tokens, expiry, revocation, and logout
15. SAML, LDAP, opaque tokens, BFF, mTLS, and other alternatives

---

## 28. References

- RFC 6749, OAuth 2.0 Authorization Framework: https://www.rfc-editor.org/info/rfc6749/
- OpenID Connect Core 1.0: https://openid.net/specs/openid-connect-core-1_0.html
- RFC 7519, JSON Web Token: https://www.rfc-editor.org/info/rfc7519/
- RFC 7636, PKCE: https://www.rfc-editor.org/info/rfc7636/
- Microsoft identity platform access tokens: https://learn.microsoft.com/en-us/entra/identity-platform/access-tokens
- Microsoft identity platform ID tokens: https://learn.microsoft.com/en-us/entra/identity-platform/id-tokens
- Spring Security OAuth 2.0 Resource Server JWT: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html

---

## 29. Security Note About Real Tokens

Never paste a real Bearer token into chat, email, tickets, logs, source code, or public JWT-decoder websites. A bearer credential may be usable by anyone who possesses it until it expires or is invalidated. If a real token is accidentally exposed, treat it as compromised: end the relevant sign-in session where practical, revoke credentials/tokens using the organization's approved process, and notify the security/IAM team if required by policy.
