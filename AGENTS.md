# MovieReviews — AGENTS.md

## Quick Start

```sh
./mvnw spring-boot:run         # dev server at localhost:8080
./mvnw compile                  # compile only
./mvnw verify -Pit              # integration tests (needs TestBench license)
```

## Referenz-Dokumente

- [Backend-Struktur & Datenbank](docs/backend.md)
- [Vaadin Frontend](docs/frontend.md)
- [CI/CD & Deploy](docs/pipeline.md)

## Skills & MCP

- **Vaadin**: MCP-Server `opencode.json → mcp.vaadin` für API-Docs
- **GitLab**: `gitlab-mcp-skill` + MCP-Server `opencode.json → mcp.gitlab`; braucht `GITLAB_TOKEN` und `GITLAB_PROJECT_ID`
- **GitHub Research*: `.agents/skills/github-research/SKILL.md`

## Dev-Team

Agents in `.opencode/agents/`:
- `planer.md` – Architektur & Aufgabenplanung
- `reviewer.md` – Code-Reviews (read-only)
- `senior-dev.md` – Implementation (full access)
