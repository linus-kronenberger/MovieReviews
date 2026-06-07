# AGENTS.md — MovieReviews

## Quick start

```sh
./mvnw spring-boot:run         # dev server at http://localhost:8080
./mvnw compile                  # compile only
./mvnw spotless:apply           # format code
./mvnw verify -Pit              # integration tests (needs TestBench license)
```

CI runs `mvn clean package -DskipTests` then `verify -Pit`.

## Vaadin MCP reference

When writing Vaadin code, use the Vaadin MCP server (`opencode.json` → `mcp.vaadin`) as a reference for API docs, component usage, and framework conventions.

For GitLab operations (MRs, issues, pipelines, etc.), use the gitlab-mcp-skill loaded from `.opencode/skills/gitlab-mcp/SKILL.md` and the GitLab MCP server (`opencode.json` → `mcp.gitlab`). Set `GITLAB_TOKEN` and `GITLAB_PROJECT_ID` in your environment.

## GitHub Research skill

Use the github-research skill (loaded from `.agents/skills/github-research/SKILL.md`) when the user:
- Asks to find repositories for a topic (e.g. "Find repos for multi-agent systems")
- Wants to analyze open-source code related to a topic
- Needs to find implementations of a paper or technique
- Asks which repos implement a specific algorithm
- Uses the `/github-research <deep-research-output-dir>` slash command

## Stack

- **Vaadin 25.1** (Java full-stack framework, not REST/SPA — backend renders the UI)
- **Spring Boot 4.0.6** (Java 25)
- **H2 in-memory** in dev, PostgreSQL planned for prod (stub only so far)
- **Maven wrapper** — always use `./mvnw`, not a system `mvn`

## Dev profile

Active by default. Key settings in `application-dev.yml`:
- `defer-datasource-initialization: true` — needed because JPA creates tables before `data.sql` runs
- `spring.sql.init.mode: always` — `data.sql` runs on every startup
- Hotdeploy + browser launch enabled

## Project layout

```
com.linus.application
  Application.java                         # @SpringBootApplication + AppShellConfigurator
  ui.MainLayout                            # @Layout — app shell with drawer nav
  ui.movies_view.MovieView                 # @Route("") — movie grid (lazy-loaded)
  ui.add_rating_view.AddRatingView         # @Route("/add-rating") — stub
  backend.movie.infrastructure.Movie*      # Entity, Repository, Service (only non-stub backend)
  backend.rating.infrastructure.Rating*    # Entities exist, no repositories yet
  backend.rating.api.RatingController      # Empty stub
  backend.user.infrastructure.User*        # Entity + Role enum, no repositories
  backend.essay.infrastructure.Essay       # Entity only
```

Only `Movie` → `MovieRepository` → `MovieService` is wired end-to-end.

## Database

Tables: `movie`, `rating`, `rating_category`, `users`, `essay`
- `movie`: movie_id (PK), title, description
- `rating`: rating_id (PK), movie_id (FK→movie), category_id (FK→rating_category), stars (1–10)
- `data.sql` seeds 100 Marvel movies on every start

## Vaadin Grid + backend sorting/filtering

`MovieView` uses lazy `setItems(query -> ...)` pattern. The callback receives Vaadin's `QuerySortOrder` list and a filter string. Convert correctly:

```java
// Service
private Sort toSort(List<QuerySortOrder> sortOrders) {
    if (sortOrders == null || sortOrders.isEmpty()) return Sort.unsorted();
    return Sort.by(sortOrders.stream()
        .map(o -> new Sort.Order(
            o.getDirection() == SortDirection.ASCENDING
                ? Sort.Direction.ASC : Sort.Direction.DESC,
            o.getSorted()))
        .toList());
}

if (filterValue == null || filterValue.isBlank())
    return repo.findAll(pageable);
return repo.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
    filterValue, filterValue, pageable);
```

## Route structure

| Path           | View            | Status |
|----------------|-----------------|--------|
| `/`            | MovieView       | working |
| `/add-rating`  | AddRatingView   | working |

## Production deploy (docker)

```sh
./mvnw package -DskipTests && java -jar target/*.jar --spring.profiles.active=prod
# or via docker-compose:
docker compose up --build -d
```

### VM-Voraussetzungen

- Docker Engine + Docker Compose (Plugin)
- Git
- Port 8080 in der Firewall freigegeben
- Repository als `~/MovieReviews` geklont
- SSH-Key für `git pull` (z. B. Deploy-Key) oder öffentliches Repo

### GitHub Actions (`.github/workflows/deploy.yml`)

Erwartet Secrets: `VM_HOST`, `VM_USER`, `SSH_PRIVATE_KEY`.  
Läuft bei Push auf `main` oder manuell via `workflow_dispatch`.

### Lokales Skript

```sh
./deploy.sh <ipv4> [ssh-user]
```

SSHet in die VM, pullt, baut und startet neu.

## Database

Tables: `movie`, `rating`, `rating_category`, `users`, `essay`
- `movie`: movie_id (PK), title, description
- `rating`: rating_id (PK), movie_id (FK→movie), category_id (FK→rating_category), stars (1–10)
- `data.sql` seeds 100 Marvel movies on every start (dev + first prod start)

## Code style

- No comments in code
- No emojis
- Never commit or push — the user does that manually
- Package-by-feature: `backend.{domain}.infrastructure`, `backend.{domain}.api`, `ui.{view}`
- Use `@Transactional` on service methods that read/write
- Tables use `snake_case` column names with `@Column(name = "...")`
