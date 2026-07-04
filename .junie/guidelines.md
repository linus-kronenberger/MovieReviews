# Junie Lokale Konfiguration

## Referenz-Dokumente

- Verwende die `AGENTS.md` im Projekt-Root als primäre Anleitung für Workflow, Skills und MCP-Konfiguration.

## Skills & MCP

Die Skills, MCPs und Subagents stammen aus dem `.opencode/`-Verzeichnis dieses Projekts:

- **MCP-Server**: Konfiguration in `opencode.json` (Root-Verzeichnis)
  - `vaadin` — Remote MCP für Vaadin API-Docs (`https://mcp.vaadin.com/docs`)
  - `gitlab` — Lokaler MCP via `@zereight/gitlab-mcp` (benötigt `GITLAB_TOKEN` und `GITLAB_PROJECT_ID`)

- **Skills**: Verzeichnis `.opencode/skills/`
  - `gitlab-mcp` — GitLab MCP Skill

- **Subagents / Agents**: Verzeichnis `.opencode/agents/`
  - `planer.md` — Architektur & Aufgabenplanung
  - `reviewer.md` — Code-Reviews (read-only)
  - `senior-dev.md` — Implementation (full access)

## Dokumentation

- **Vaadin**: https://vaadin.com/docs/latest
- **Spring Boot**: https://docs.spring.io/spring-boot/reference/
- **Spring Framework**: https://docs.spring.io/spring-framework/reference/
- **Java (JDK 21)**: https://docs.oracle.com/en/java/javase/21/docs/api/

## Workflow

Siehe `AGENTS.md` für den vollständigen Workflow (Planer → Senior Dev → Reviewer).
