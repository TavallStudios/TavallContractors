# Tavall Contractors

Tavall Contractors is a Java application repository for a contractor marketplace and intake experience, with a Spring Boot web application and an internal API module.

The checked-in source includes client intake and scope-generation endpoints, account and freelancer flows, marketplace views, and client/freelancer dashboards. Product flow notes include proposed behavior as well as implemented boundaries; use the links below to distinguish them.

## Why Tavall Contractors

- Bring client intake, project scoping, and contractor discovery into one application surface.
- Keep API handling and page rendering in a Java/Spring application.
- Separate product flow notes from the implementation that currently exists in source.

## Features

- Intake state, evaluation, generated scope text, and checkout endpoints.
- Account/authentication, freelancer, marketplace, and dashboard routes.
- Thymeleaf templates and TypeScript-backed browser interactions.
- PostgreSQL and MongoDB integration code, plus a cache boundary.

## Quick Start

The repository does not document a hosted demo or sample credentials. To build and verify the source, use JDK 25 and the committed Gradle Wrapper:

```bash
./gradlew check
```

## How It Works

`spring-webview` is the executable Spring Boot application. It composes `internal-contractor-api`, page templates, static assets, service code, and persistence integrations. The flow document is working product context; it does not prove that a proposed feature is deployed or commercially available.

## Project Structure

├── [`internal-contractor-api`](internal-contractor-api/README.md)
└── [`spring-webview`](spring-webview/README.md)

## Documentation

- [Implementation notes](docs/code.md) — current technical structure and web/API flow.
- [Product flow draft](docs/FLOWS.MD) — working flows, including behavior not established as implemented.
- [Repository workflow compatibility pointer](docs/quality/GIT_WORKFLOW.md) — redirects to shared policy.
- [Tavall Docs Git Workflow](https://github.com/TavallStudios/tavall-docs/blob/main/docs/quality/GIT_WORKFLOW.md) — contribution and review guidance.

## Requirements / Compatibility

- JDK 25 for the configured Gradle toolchain.
- External database and identity-provider configuration is required for the corresponding integrations.

## Building From Source

Run `./gradlew check`. Runtime configuration and external services are not bundled as a public demo setup.

## Contributing

Open a pull request in this repository and follow the Tavall contribution and review policy linked above.

## License

No license file is currently tracked in this repository. Contact the maintainers before redistributing or reusing the code.

## Deployment

The repository contains the independently executable `spring-webview` application. Current target and deployed source are not recorded in GitHub deployment evidence; see [the deployment record](TAVALL_CONTRACTORS_DEPLOYMENT.md).

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/TavallContractors/README.md` | 2026-09-27 12:59 PM PDT | [PR #7](https://github.com/TavallStudios/TavallContractors/pull/7) |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:59 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:59 PM PDT | GitHub | `CREATED` | `TavallStudios/TavallContractors/README.md` | — | [PR #7](https://github.com/TavallStudios/TavallContractors/pull/7) | Added a public project front door, current module map, and honest deployment routing. |

</details>
