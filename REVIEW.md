# REVIEW.md — AI Output Review Log

Complete **one review entry per slice** before moving to the next.


---

## Slice 1 — Signup (S1)

### A. Bucket Check
List every class and method the AI produced. Tag each one:  
`Controller` / `Service` / `Repository` / `DTO` / `Security` / `Config` / `Exception`

| File / Method | Tag | In Allowed List? |
|---|---|---|
| N/A — implemented manually | N/A | N/A |

> **Important:** Anything **not** in the allowed list → **reject, re-prompt**. Don't keep it because it works.

### B. Contract Check
Compare against API contract row A1 line by line.

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/signup` | Pass |
| Returns **201** | Pass |
| Response shape: `{ id, email, fullName, role, createdAt }` | Pass |
| No `passwordHash` field in response | Pass |
| Role in response is always `STUDENT` | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| `@Valid` on controller parameter | Pass |
| `@Email`, `@NotBlank`, `@Size` on DTO fields | Pass |
| Duplicate email throws `DuplicateResourceException` | Pass |
| No new error/response wrapper class invented | Pass |
| No stack trace or exception message leaked to client | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Password hashed via injected `PasswordEncoder` (not `new BCryptPasswordEncoder()`) | Pass |
| `SignupRequest` has no `role` field, or it is ignored | Pass |
| Duplicate check uses normalised (lowercased/trimmed) email | Pass |
| AI did **not** touch `SecurityConfig`, add JWT class, or add `UserDetailsService` | Pass |

### E. Understanding Questions
*Answer in your own words (1–2 lines each)*

1. Where exactly is the password hashed, and why is the same password stored as a different string for two users?

   > The password is hashed in `AuthService` using the injected `PasswordEncoder` before the user is saved. BCrypt uses a random salt, so the same password can produce different stored hashes.

2. What would happen if `SignupRequest` accepted a `role` field and the AI used it?

   > A client could submit `ADMIN` and potentially grant itself administrator privileges. The server must always assign `STUDENT` during signup.

3. Which class turns `DuplicateResourceException` into a 409 response?

   > `GlobalExceptionHandler` handles `DuplicateResourceException` and returns HTTP 409 Conflict.

### F. Decision

| File | Decision | Reason (if modified/rejected) |
|---|---|---|
| S1 implementation | Accepted | Signup implementation completed manually. |

---

## Slice 2 — Login & JWT (S2)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `dto/LoginRequest.java` | DTO | Yes |
| `dto/LoginResponse.java` | DTO | Yes |
| `security/JwtService.java` — `generateToken(User user)` | Security | Yes |
| `service/AuthService.java` — `login(LoginRequest)` and necessary dependency-injection changes | Service | Yes |
| `controller/AuthController.java` — login endpoint | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/login` | Pass |
| Returns **200** | Pass |
| Response: `{ accessToken, tokenType: "Bearer", expiresIn }` | Pass |
| `expiresIn` matches configured value (not hardcoded) | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Same 401 message for unknown email and wrong password | Pass |
| No `try/catch` swallowing exceptions silently | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| JWT secret comes from `@Value`, no string literal secret | Pass |
| Uses jjwt 0.12 API: `.subject()`, `.claim()`, `.id()`, `.issuedAt()`, `.expiration()`, `.signWith(key)` | Pass |
| Rejects deprecated: `setSubject`, `setExpiration`, `signWith(SignatureAlgorithm.HS256, secret)`, `Jwts.parserBuilder()` | Pass |
| `jti` is a random UUID (not the user id) | Pass |
| No password or hash in token claims | Pass |
| Parse/validate method **not** written yet (that is S3) | Pass |

### E. Understanding Questions

1. Why must the error message be identical for "no such email" and "wrong password"?

   > An identical error prevents attackers from discovering which email addresses are registered. Both cases should return the same HTTP 401 response and message: `Invalid email or password`.

2. Who can read the claims inside your JWT? Who can change them?

   > Anyone who obtains the JWT can decode and read its claims because a normal signed JWT is not encrypted. A client can modify token contents, but modified claims will fail signature verification unless the client can sign the token with the server's secret key.

3. What does `jti` give us that `sub` doesn't? (Think about S5.)

   > `sub` identifies the token's subject, usually the user's email. `jti` uniquely identifies an individual token, allowing the server to revoke one token without revoking every token belonging to that user.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `dto/LoginRequest.java` | Accepted | Login request DTO and validation. |
| `dto/LoginResponse.java` | Accepted | Login response DTO. |
| `security/JwtService.java` | Accepted | JWT generation. |
| `service/AuthService.java` | Accepted | Login and credential verification. |
| `controller/AuthController.java` | Accepted | Login endpoint. |

---

## Slice 3 — JWT Filter & Protected API (S3)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `security/JwtService.java` — `parseToken(String token)` | Security | Yes |
| `security/JwtAuthenticationFilter.java` — JWT authentication filter | Security | Yes |
| `config/SecurityConfig.java` — `SecurityFilterChain` configuration | Config | Yes |
| `controller/UserController.java` — `GET /api/users/me` | Controller | Yes |
| `dto/UserResponse.java` — only if required and not already present | DTO | Yes, conditionally |
| `repository/UserRepository.java` — only if an existing email lookup cannot support the endpoint | Repository | Yes, conditionally |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `GET /api/courses` works **without** token | Pass |
| `GET /api/users/me` returns **401** with no token | Pass |
| `GET /api/users/me` returns **200** with valid token | Pass |
| 401 body is an `ApiError` JSON object | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Filter does not write a response on error — continues chain | Pass |
| Filter clears SecurityContext on bad token | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Spring Security 6 style: lambda DSL, `requestMatchers`, `authorizeHttpRequests` | Pass |
| Rejected: `WebSecurityConfigurerAdapter`, `antMatchers`, `authorizeRequests`, `.and()` | Pass |
| `permitAll` covers only the three specific public endpoints | Pass |
| `GET /api/courses` is public but `POST /api/courses` is NOT (watch for broad matcher) | Pass |
| Signature + expiry verified with `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | Pass |
| Authorities use `ROLE_` prefix | Pass |
| Entry point and access-denied handler wired via `exceptionHandling(...)` | Pass |
| No `UserDetailsService`, `AuthenticationManager`, sessions or CORS added | Pass |
| `GET /api/users/me` returns `UserResponse`, not the entity | Pass |

### E. Understanding Questions

1. Why is CSRF protection disabled here, and when would disabling it be wrong?

   > This API uses stateless authentication with a JWT supplied in the Authorization header, rather than relying on browser-managed session cookies. Disabling CSRF is generally appropriate for this setup, but can be unsafe if authentication relies on cookies that browsers automatically attach to requests.

2. Walk through what happens to a request with an expired token, step by step, until the client sees 401.

   > The filter extracts the Bearer token and passes it to `JwtService.parseToken()`. Signature or expiration validation fails, so the filter clears the security context and continues the filter chain. The protected endpoint requires authentication, so Spring Security invokes the configured authentication entry point, which returns HTTP 401 with an `ApiError` JSON response.

3. Why don't 401/403 errors from the filter chain reach `GlobalExceptionHandler`?

   > Spring Security handles authentication and authorization failures within its filter chain, before requests necessarily reach a controller. Its configured authentication entry point and access-denied handler produce the 401 and 403 responses, rather than the controller-level exception handler.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `security/JwtService.java` | Accepted | JWT generation and token parsing. |
| `security/JwtAuthenticationFilter.java` | Accepted | JWT authentication filter. |
| `config/SecurityConfig.java` | Accepted | Endpoint authorization and security configuration. |
| `controller/UserController.java` | Accepted | Current-user profile endpoint. |
| `dto/UserResponse.java` | Accepted | User profile response DTO. |
| `repository/UserRepository.java` | Accepted | User lookup for the profile endpoint. |

---

## Slice 4 — Role Restriction (S4)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `config/SecurityConfig.java` — ADMIN restriction for `POST /api/courses` | Config | Yes |
| `controller/CourseController.java` — POST endpoint, only if a change is necessary | Controller | Yes, conditionally |
| `security/JsonAccessDeniedHandler.java` — only if a change is necessary | Security | Yes, conditionally |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/courses` with no token → **401** | Pass |
| `POST /api/courses` with STUDENT token → **403** | Pass |
| `POST /api/courses` with ADMIN token → **201** | Pass |
| 403 body is an `ApiError` JSON object | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Role naming consistent: `hasRole("ADMIN")` ↔ authority `ROLE_ADMIN` | Pass |
| If `@PreAuthorize` used: `@EnableMethodSecurity` is present | Pass |
| Rule placed **before** `anyRequest().authenticated()` if in SecurityConfig | Pass |
| Diff is small — AI did not rewrite the whole config | Pass |

### E. Understanding Questions

1. A STUDENT calls `POST /api/courses`. Why does the server return 403 and not 401?

   > The STUDENT's token is valid, so the user is authenticated. However, the STUDENT does not have the required ADMIN authority. HTTP 403 means access is forbidden for an authenticated user; HTTP 401 means authentication is missing or invalid.

2. Where does the role in the JWT come from, and can the client change it?

   > The role comes from the user's server-side role when the JWT is generated. A client can decode or modify the token contents, but a modified role will fail signature verification unless the client can sign the token with the server's secret key.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `config/SecurityConfig.java` | Accepted | ADMIN restriction for course creation. |
| `controller/CourseController.java` | Accepted | Existing endpoint retains the required HTTP 201 response. |
| `security/JsonAccessDeniedHandler.java` | Accepted | Standard JSON response for forbidden requests. |

---

## Slice 5 — Logout (S5) *(stretch)*

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `security/TokenDenylist.java` — `revoke(jti, expiry)` | Security | Yes |
| `security/TokenDenylist.java` — `isRevoked(jti)` | Security | Yes |
| `security/TokenDenylist.java` — `cleanExpired()` | Security | Yes |
| `controller/AuthController.java` — `POST /api/auth/logout` | Controller | Yes |
| `security/JwtAuthenticationFilter.java` — denylist check | Security | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/auth/logout` returns **204** | Pass |
| Same token returns **401** after logout | Pass |
| Fresh login after logout returns a new working token | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Logout adds entry to server-side denylist (not just tells client to delete token) | Pass |
| Denylist keyed by **jti**, not email | Pass |
| Map is `ConcurrentHashMap` (thread-safe) | Pass |
| Logout endpoint is NOT under `permitAll` | Pass |
| Filter checks the denylist and treats denylisted jti as invalid | Pass |
| (Bonus) Expired entries are removed from the map | Pass |

### E. Understanding Questions

1. Why does logout need server-side state if JWT is "stateless"?

   > A JWT normally remains valid until it expires, even after the client deletes its local copy. A server-side denylist records revoked token IDs, allowing the server to reject a token before its natural expiration.

2. What happens to the denylist when the server restarts? What if there are two server instances?

   > An in-memory denylist is lost when the server restarts. With multiple server instances, each instance has its own map, so a token revoked on one instance may still work on another. A shared store, such as Redis or a database, would be needed for consistent revocation across instances and restarts.

3. Why store the expiry time with each jti?

   > The expiry time lets the server remove a revoked token ID once the JWT would naturally expire. After that point, the token should no longer be accepted, so retaining its denylist entry is unnecessary.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `security/TokenDenylist.java` | Accepted | Server-side token revocation and expiry cleanup. |
| `controller/AuthController.java` | Accepted | Logout endpoint and token revocation. |
| `security/JwtAuthenticationFilter.java` | Accepted | Rejects revoked JWTs. |

---

## No-AI Change Tasks

**Files changed:**

| File | What changed and why |
|---|---|
| `src/main/java/com/esd/lab2/security/JwtService.java` | Changed JWT expiry to 15 minutes and ensured `expiresIn` reflects 900 seconds. |
| `src/main/java/com/esd/lab2/controller/CourseController.java` | Added public `GET /api/courses/{id}` to retrieve a course by ID. |
| `src/main/java/com/esd/lab2/service/CourseService.java` | Added course lookup by ID and used `ResourceNotFoundException` when the course does not exist. |
| `src/main/java/com/esd/lab2/controller/UserController.java` | Added authenticated `PUT /api/users/me` to update the logged-in user's full name only. |
| `src/main/java/com/esd/lab2/dto/SignupRequest.java` | Added password validation requiring at least one digit, returning HTTP 400 with a field error when invalid. |
| `src/main/java/com/esd/lab2/config/SecurityConfig.java` | Changed `GET /api/courses` from public to authenticated access. |

**Explanation (your own words):**

> I reduced the JWT expiry to 15 minutes and updated `expiresIn` accordingly. I added a public endpoint to retrieve a course by ID, returning 404 when the course does not exist. I added an authenticated endpoint allowing users to update only their own full name. I strengthened signup validation to require at least one digit in the password. Finally, I made `GET /api/courses` require authentication instead of being public. Tests expecting `GET /api/courses` to succeed without a token will now fail and must be updated to expect 401 or provide a valid token.
