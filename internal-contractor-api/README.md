# internal-contractor-api

Owns the internal Spring API boundary currently included by the Contractors application, including its health endpoint.

## Responsibility

### Owns
- The internal API's Spring request boundary and health endpoint.

### Does Not Own
- The public web application, product-flow policy, or persistence services.
- A standalone server process.

## Repository Structure

TavallContractors/
├── **[`internal-contractor-api`](README.md) ← This Module**
└── [`spring-webview`](../spring-webview/README.md)

## Relationships

| Module / System | Relationship |
| --- | --- |
| [`spring-webview`](../spring-webview/README.md) | The executable application composes this API module. |

## Documentation

| Type | Document | Purpose | Surface |
| --- | --- | --- | --- |
| Technical | [Implementation notes](../docs/code.md) | Describes the current Spring Boot, web, API, and persistence structure. | GitHub |
| Design | [Product flow draft](../docs/FLOWS.MD) | Working flow notes; distinguish proposals from checked-in behavior. | GitHub |

## Deployment

> This module is not independently deployed.

Runtime owner: [`spring-webview`](../spring-webview/README.md). Deployment record: [TAVALL_CONTRACTORS_DEPLOYMENT.md](../TAVALL_CONTRACTORS_DEPLOYMENT.md).

## Development

- **Module Type:** `API`
- **Runtime:** `spring-webview`
- **Current PR Stack:** [product integration root #5](https://github.com/TavallStudios/TavallContractors/pull/5), [Java Tools adoption #4](https://github.com/TavallStudios/TavallContractors/pull/4), [CI transition #3](https://github.com/TavallStudios/TavallContractors/pull/3); documentation update: __PR_LINK__.
- Shared contribution policy: [Tavall Docs Git Workflow](https://github.com/TavallStudios/tavall-docs/blob/main/docs/quality/GIT_WORKFLOW.md).


<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/TavallContractors/internal-contractor-api/README.md` | 2026-09-27 12:51 PM PDT | __PR_URL__ |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:51 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:51 PM PDT | GitHub | `CREATED` | `TavallStudios/TavallContractors/internal-contractor-api/README.md` | — | __PR_URL__ | Added a module README grounded in current Spring source and product-flow documents. |

</details>
