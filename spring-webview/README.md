# spring-webview

Owns the executable Spring Boot application and checked-in server-rendered web, intake, authentication, marketplace, and dashboard implementation.

## Responsibility

### Owns
- Spring Boot application entry point and HTTP request handling.
- Thymeleaf templates and TypeScript/static web assets.
- Current intake, account, portfolio, marketplace, and dashboard service flows.

### Does Not Own
- A verified hosted deployment or external payment/identity provider.
- The lifecycle of PostgreSQL, MongoDB, or other external services.

## Repository Structure

TavallContractors/
├── [`internal-contractor-api`](../internal-contractor-api/README.md)
└── **[`spring-webview`](README.md) ← This Module**

## Relationships

| Module / System | Relationship |
| --- | --- |
| [`internal-contractor-api`](../internal-contractor-api/README.md) | Includes the internal API/health boundary. |
| Tavall Database | Uses PostgreSQL/JPA and MongoDB integration code from the application. |

## Documentation

| Type | Document | Purpose | Surface |
| --- | --- | --- | --- |
| Technical | [Implementation notes](../docs/code.md) | Describes the current Spring Boot, web, API, and persistence structure. | GitHub |
| Design | [Product flow draft](../docs/FLOWS.MD) | Working flow notes; distinguish proposals from checked-in behavior. | GitHub |

## Deployment

> This module is the independently executable runtime.

Current target and exact deployed source are not recorded in GitHub deployment evidence. See [TAVALL_CONTRACTORS_DEPLOYMENT.md](../TAVALL_CONTRACTORS_DEPLOYMENT.md).

## Development

- **Module Type:** `RUNTIME`
- **Runtime:** `Self`
- **Current PR Stack:** [product integration root #5](https://github.com/TavallStudios/TavallContractors/pull/5), [Java Tools adoption #4](https://github.com/TavallStudios/TavallContractors/pull/4), [CI transition #3](https://github.com/TavallStudios/TavallContractors/pull/3); documentation update: [PR #7](https://github.com/TavallStudios/TavallContractors/pull/7).
- Shared contribution policy: [Tavall Docs Git Workflow](https://github.com/TavallStudios/tavall-docs/blob/main/docs/quality/GIT_WORKFLOW.md).


<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/TavallContractors/spring-webview/README.md` | 2026-09-27 12:59 PM PDT | [PR #7](https://github.com/TavallStudios/TavallContractors/pull/7) |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:59 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:59 PM PDT | GitHub | `CREATED` | `TavallStudios/TavallContractors/spring-webview/README.md` | — | [PR #7](https://github.com/TavallStudios/TavallContractors/pull/7) | Added a module README grounded in current Spring source and product-flow documents. |

</details>
