# Tavall Contractors Java Tools Contract

Tavall Contractors is a Tavall-owned Java consumer. Tavall DI is the universal first-party composition/lifecycle baseline across `internal-contractor-api` and `spring-webview`.

The Spring runtime uses Tavall Database Postgres/Mongo for shared persistence infrastructure, Tavall Logging for application/runtime diagnostics, Tavall Concurrency for asynchronous work, and the existing Tavall Cache integration for bounded cache semantics.

Spring remains the HTTP/security/view framework. Existing Spring Data JPA/MongoDB and direct PostgreSQL/Mongo infrastructure are migration seams where they duplicate Tavall Database mechanics; contractor domain entities/repositories remain product-owned.

Use Tavall Registry, EventBus, Reflection, and Scheduler whenever those concerns exist rather than creating project-local infrastructure.

Do not add first-party ServiceLoader composition, service locators, executor frameworks, logging wrappers, registry/cache/event frameworks, reflection scanners, scheduled executors, or database infrastructure when the corresponding Tavall tool owns the concern.

Exact Java 25 verification, dependency-lock refresh, PostgreSQL/Mongo integration tests, and Spring runtime acceptance are required before promotion.