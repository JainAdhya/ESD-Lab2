# PROMPTS.md — AI Prompt Log

Record every prompt you send to the AI **starting from Slice 2**.  
Slice 1 is done manually — no AI. See `README.md` for the prompt template structure.

> **Rules:**
> - One prompt (+ follow-ups) per slice
> - Use the structured template from the README — free-form prompts are not allowed
> - Do not back-fill this file after the lab

---

## Example — How a good S1 prompt looks (for reference only, S1 is done manually)

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   CourseController.java
  ├── service/      CourseService.java
  ├── dto/          CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java


TASK
Implement only POST /api/auth/signup.
Request: SignupRequest { email (valid email, required), password (required, 8–64 chars),
         fullName (required, not blank, max 100) }.
Response: 201 with UserResponse { id, email, fullName, role, createdAt }.
The new user's role is always STUDENT, even if the request contains a role field.
Store email trimmed and lowercased. Hash the password with the existing PasswordEncoder bean.
If the email already exists, throw DuplicateResourceException.

ALLOWED TO CREATE OR CHANGE
dto/SignupRequest.java, dto/UserResponse.java,
service/AuthService.java (signup method only),
controller/AuthController.java (signup endpoint only)

RULES
- Do not create or modify any file not listed above.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

---

## Slice 2 — Login & JWT (S2)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
src/main/java/com/esd/lab2/
├── Lab2Application.java
├── config/
│   ├── SecurityConfig.java       TEMPORARY: permits all. Replace in S3.
│   └── AdminSeeder.java          Seeds admin@esd.local at startup.
├── controller/
│   └── CourseController.java     GET + POST /api/courses (unprotected for now)
├── service/
│   └── CourseService.java
├── dto/
│   ├── CourseRequest.java
│   └── CourseResponse.java
├── entity/
│   ├── User.java
│   ├── Role.java                 enum: STUDENT, ADMIN
│   └── Course.java
├── repository/
│   ├── UserRepository.java       findByEmail, existsByEmail
│   └── CourseRepository.java
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── DuplicateResourceException.java    409
│   ├── InvalidCredentialsException.java   401
│   └── ResourceNotFoundException.java     404
└── security/
    ├── JsonAuthenticationEntryPoint.java   writes ApiError for 401 from filter chain
    └── JsonAccessDeniedHandler.java        writes ApiError for 403 from filter chain
Existing code you must use (do not rewrite it):
•	Existing AuthService.java with signup implementation, UserRepository, and PasswordEncoder dependencies.
•	Existing AuthController.java with the signup endpoint.
•	Existing User.java entity and Role.java enum.
•	Existing UserRepository.java with findByEmail and existsByEmail.
•	Existing InvalidCredentialsException.java and GlobalExceptionHandler.java.
•	Existing JWT dependencies and password-encoding configuration.
TASK
Implement only: S2 - User Authentication & JWT Issuing (Login).
Contract:
•	POST /api/auth/login — authenticate a user and issue a JWT, returning HTTP 200 OK.
•	LoginRequest — record with @NotBlank and @Email on email, and @NotBlank on password.
•	LoginResponse — record with fields accessToken (String), tokenType (String default "Bearer"), and expiresIn (long seconds).
•	JwtService.generateToken(User user) — generate a JWT using JJWT 0.12.x. Read jwt.secret from @Value, configure claims (sub = email, role, jti = random UUID, iat, exp), and sign with HMAC-SHA256.
•	AuthService.login(LoginRequest) — look up user by lowercase/trimmed email, verify password with passwordEncoder.matches(), throw InvalidCredentialsException with identical message "Invalid email or password" on any failure, generate token, and return LoginResponse.
•	AuthController — add POST /api/auth/login accepting @Valid @RequestBody LoginRequest, calling authService.login(), and returning HTTP 200 OK.
ALLOWED TO CREATE OR CHANGE
•	src/main/java/com/esd/lab2/dto/LoginRequest.java
•	src/main/java/com/esd/lab2/dto/LoginResponse.java
•	src/main/java/com/esd/lab2/security/JwtService.java
•	src/main/java/com/esd/lab2/service/AuthService.java — only the login method and necessary constructor/dependency-injection changes.
•	src/main/java/com/esd/lab2/controller/AuthController.java — only the login endpoint.
RULES
•	Do not create or modify any file not listed above.
•	Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
•	Use DTOs for request and response. Never return an entity from a controller.
•	Use jakarta.validation annotations for input validation.
•	Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
•	If something is unclear, add a TODO comment instead of guessing.
OUTPUT
Each file in full, with its path. Then a list of anything you assumed.

```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 3 — JWT Filter & Protected API (S3)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
src/main/java/com/esd/lab2/
├── Lab2Application.java
├── config/
│   ├── SecurityConfig.java       TEMPORARY: permits all. Replace in S3.
│   └── AdminSeeder.java          Seeds admin@esd.local at startup.
├── controller/
│   ├── AuthController.java       POST /api/auth/signup + POST /api/auth/login
│   └── CourseController.java     GET + POST /api/courses (unprotected for now)
├── service/
│   ├── AuthService.java          Signup + login + JWT issuing
│   └── CourseService.java
├── dto/
│   ├── CourseRequest.java
│   ├── CourseResponse.java
│   ├── UserResponse.java
│   ├── LoginRequest.java
│   └── LoginResponse.java
├── entity/
│   ├── User.java
│   ├── Role.java                 enum: STUDENT, ADMIN
│   └── Course.java
├── repository/
│   ├── UserRepository.java       findByEmail, existsByEmail
│   └── CourseRepository.java
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── DuplicateResourceException.java    409
│   ├── InvalidCredentialsException.java   401
│   └── ResourceNotFoundException.java     404
└── security/
    ├── JwtService.java           JWT generation (S2); token parsing (S3)
    ├── JsonAuthenticationEntryPoint.java   writes ApiError for 401 from filter chain
    └── JsonAccessDeniedHandler.java        writes ApiError for 403 from filter chain
Existing code you must use (do not rewrite it):
•	Existing JwtService.java from S2 that generates JWTs.
•	Existing SecurityConfig.java and its current security configuration.
•	Existing JsonAuthenticationEntryPoint.java and JsonAccessDeniedHandler.java.
•	Existing User.java entity and Role.java enum.
•	Existing UserRepository.java with findByEmail and existsByEmail.
•	Existing UserResponse.java DTO from the authentication implementation.
•	Existing GlobalExceptionHandler.java and exception classes.
TASK
Implement only: S3 - JWT Filter & Protected API.
Contract:
•	parseToken(String token) — parse and cryptographically verify a JWT using Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(), check expiration, and return Claims.
•	JwtAuthenticationFilter — extend OncePerRequestFilter, extract the token from the Authorization: Bearer <token> header, parse the token, and populate SecurityContextHolder with UsernamePasswordAuthenticationToken using authority "ROLE_" + role. Clear the security context on token errors and continue the filter chain.
•	SecurityConfig — configure SecurityFilterChain, disable CSRF, set session policy to STATELESS, permit public endpoints (POST /api/auth/signup, POST /api/auth/login, GET /api/courses), require authentication for anyRequest(), wire jsonAuthenticationEntryPoint and jsonAccessDeniedHandler, and register jwtAuthenticationFilter before UsernamePasswordAuthenticationFilter.
•	UserController — add GET /api/users/me requiring authentication. Retrieve user details from the database by authentication.getName() and return UserResponse with HTTP 200 OK.
ALLOWED TO CREATE OR CHANGE
•	src/main/java/com/esd/lab2/security/JwtService.java — only add parseToken(String token) and any necessary supporting code without breaking existing token generation.
•	src/main/java/com/esd/lab2/security/JwtAuthenticationFilter.java — create the JWT authentication filter.
•	src/main/java/com/esd/lab2/config/SecurityConfig.java — configure the security filter chain and endpoint authorization.
•	src/main/java/com/esd/lab2/controller/UserController.java — create the current-user profile endpoint.
•	src/main/java/com/esd/lab2/dto/UserResponse.java — only if it does not already exist and is required for the endpoint; reuse the existing DTO if available.
•	src/main/java/com/esd/lab2/repository/UserRepository.java — only if an existing email lookup method cannot support retrieving the user for the profile endpoint; do not change unrelated methods.
RULES
•	Do not create or modify any file not listed above.
•	Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
•	Use DTOs for request and response. Never return an entity from a controller.
•	Use jakarta.validation annotations for input validation.
•	Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
•	If something is unclear, add a TODO comment instead of guessing.
OUTPUT
Each file in full, with its path. Then a list of anything you assumed.

```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 4 — Role Restriction (S4)

**Prompt 1:**

```
CONTEXT

Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.

Package layout:

src/main/java/com/esd/lab2/

├── Lab2Application.java

├── config/

│   ├── SecurityConfig.java       Configures authentication and endpoint authorization.

│   └── AdminSeeder.java         Seeds admin@esd.local at startup.

├── controller/

│   ├── AuthController.java      POST /api/auth/signup + POST /api/auth/login

│   ├── CourseController.java    GET + POST /api/courses

│   └── UserController.java      GET /api/users/me

├── service/

│   ├── AuthService.java         Signup + login + JWT issuing

│   └── CourseService.java

├── dto/

│   ├── CourseRequest.java

│   ├── CourseResponse.java

│   ├── UserResponse.java

│   ├── LoginRequest.java

│   └── LoginResponse.java

├── entity/

│   ├── User.java

│   ├── Role.java                enum: STUDENT, ADMIN

│   └── Course.java

├── repository/

│   ├── UserRepository.java      findByEmail, existsByEmail

│   └── CourseRepository.java

├── exception/

│   ├── ApiError.java

│   ├── GlobalExceptionHandler.java

│   ├── DuplicateResourceException.java    409

│   ├── InvalidCredentialsException.java   401

│   └── ResourceNotFoundException.java     404

└── security/

    ├── JwtService.java          JWT generation and token parsing

    ├── JwtAuthenticationFilter.java

    ├── JsonAuthenticationEntryPoint.java   writes ApiError for 401 from filter chain

    └── JsonAccessDeniedHandler.java        writes ApiError for 403 from filter chain

Existing code you must use (do not rewrite it):

- Existing JwtService.java that generates and parses JWTs.
- Existing JwtAuthenticationFilter.java that authenticates requests using JWTs and populates SecurityContextHolder with the user's role.
- Existing SecurityConfig.java and its current security configuration.
- Existing JsonAuthenticationEntryPoint.java and JsonAccessDeniedHandler.java.
- Existing User.java entity and Role.java enum.
- Existing UserRepository.java with findByEmail and existsByEmail.
- Existing CourseController.java and CourseService.java that implement GET and POST /api/courses.
- Existing CourseRequest.java and CourseResponse.java DTOs.
- Existing GlobalExceptionHandler.java and exception classes.

TASK

Implement only: S4 - Restricting Course Creation to ADMINs (Role-Based Access Control).

Contract:

- SecurityConfig — add `.requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")` immediately before `.anyRequest().authenticated()`. Keep the existing public endpoints and all other security configuration unchanged.
- CourseController — verify that POST /api/courses creates courses and returns HTTP 201 Created for authorized ADMIN users. Do not modify the endpoint unless a change is necessary to satisfy this contract.
- JsonAccessDeniedHandler — ensure that an authenticated non-admin user, such as a STUDENT, attempting POST /api/courses receives HTTP 403 Forbidden with the standard JSON ApiError response. Reuse the existing handler and do not create a new error class.

ALLOWED TO CREATE OR CHANGE

- src/main/java/com/esd/lab2/config/SecurityConfig.java — add the ADMIN role restriction for POST /api/courses.
- src/main/java/com/esd/lab2/controller/CourseController.java — only if a change is necessary to ensure POST /api/courses returns HTTP 201 Created.
- src/main/java/com/esd/lab2/security/JsonAccessDeniedHandler.java — only if a change is necessary to ensure authenticated users without the ADMIN role receive the standard JSON HTTP 403 response.

RULES

- Do not create or modify any file not listed above.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT

Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 5 — Logout *(stretch, S5)*

**Prompt 1:**

```
## CONTEXT

Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.

Package layout:

```text
src/main/java/com/esd/lab2/
├── Lab2Application.java
├── config/
│   ├── SecurityConfig.java
│   └── AdminSeeder.java
├── controller/
│   ├── AuthController.java
│   ├── CourseController.java
│   └── UserController.java
├── service/
│   ├── AuthService.java
│   └── CourseService.java
├── dto/
│   ├── CourseRequest.java
│   ├── CourseResponse.java
│   ├── UserResponse.java
│   ├── LoginRequest.java
│   └── LoginResponse.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   └── Course.java
├── repository/
│   ├── UserRepository.java
│   └── CourseRepository.java
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── DuplicateResourceException.java
│   ├── InvalidCredentialsException.java
│   └── ResourceNotFoundException.java
└── security/
    ├── JwtService.java
    ├── JwtAuthenticationFilter.java
    ├── JsonAuthenticationEntryPoint.java
    ├── JsonAccessDeniedHandler.java
    └── TokenDenylist.java (to be created)
```

Existing code you must use (do not rewrite it):

- `JwtService.java`
- `AuthController.java`
- `JwtAuthenticationFilter.java`
- `JsonAuthenticationEntryPoint.java`
- `ApiError.java`
- Existing exceptions in `com.esd.lab2.exception`

TASK

Implement only: **S5 — Server-Side Token Invalidation (Logout)**

Contract:

1. **TokenDenylist**
   - Create `src/main/java/com/esd/lab2/security/TokenDenylist.java`.
   - Create a thread-safe `@Component` using `ConcurrentHashMap<String, Instant>`.
   - Implement `revoke(jti, expiry)` to store revoked token IDs.
   - Implement `isRevoked(jti)` to check revocation.
   - Implement `cleanExpired()` to remove naturally expired tokens.

2. **Logout API**
   - Add `POST /api/auth/logout` to the existing `AuthController.java`.
   - Extract the Bearer token from the `Authorization` header.
   - Parse the claims to retrieve `jti` and the expiration date.
   - Call `tokenDenylist.revoke(jti, expiry)`.
   - Return HTTP `204 No Content`.

3. **JWT Filter**
   - Modify the existing `JwtAuthenticationFilter.java`.
   - Inject `TokenDenylist`.
   - After parsing claims, check `tokenDenylist.isRevoked(jti)`.
   - If revoked, clear `SecurityContextHolder` and stop filter execution so subsequent calls are rejected with HTTP `401 Unauthorized`.

ALLOWED TO CREATE OR CHANGE

- `src/main/java/com/esd/lab2/security/TokenDenylist.java` — create `TokenDenylist` and implement `revoke`, `isRevoked`, and `cleanExpired`.
- `src/main/java/com/esd/lab2/controller/AuthController.java` — add `POST /api/auth/logout`.
- `src/main/java/com/esd/lab2/security/JwtAuthenticationFilter.java` — add the denylist check for revoked tokens.

RULES

- Do not create or modify any file not listed above.
- Use the existing exceptions in `com.esd.lab2.exception`. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT

Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
(paste here)
```
