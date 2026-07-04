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

## Dev-Team & Workflow

Agents in `.opencode/agents/`:
- `planer.md` – Architektur & Aufgabenplanung
- `reviewer.md` – Code-Reviews (read-only)
- `senior-dev.md` – Implementation (full access)

**Workflow für jedes Feature:**
1. **Planer** → Anforderung analysieren, Lösung skizzieren, Aufgaben aufteilen
2. **Senior Dev** → Implementierung nach Plan
3. **Reviewer** → Fertigen Code reviewen

**Branching & MR:**
- **Vor jedem Feature** einen neuen Branch erstellen: `git switch -c feature/KURZBESCHREIBUNG`
- **Nach Fertigstellung** (nach erfolgreichem Review / letztem Commit) einen **Draft MR** via GitLab CLI öffnen:
  ```sh
  glab mr create --draft \
    --title "Kurzbeschreibung" \
    --description "Closes #ISSUE-NUMMER" \
    --label frontend,backend \
    --fill
  ```
- Branch-Namen und Issue-Nummer aus der Aufgabenstellung ableiten. Falls kein Issue existiert, `--fill` ohne `--description` nutzen.
