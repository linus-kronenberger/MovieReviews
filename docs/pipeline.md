# CI/CD & Deploy

## GitLab CI (`.gitlab-ci.yml`)

- Läuft bei Push auf `master`
- **Stage `build`**: `mvn compile` → JAR als Artefakt
- **Stage `test`**: `mvn test`
- **Stage `deploy`**: SSH in die VM → `git pull` → `docker compose down --remove-orphans` → `docker compose up --build -d` → `docker image prune -f`
- Erwartet CI-Variablen: `VM_HOST`, `VM_USER`, `SSH_PRIVATE_KEY`

## Docker / Production

```sh
docker compose up --build -d
```

### VM-Voraussetzungen
- Docker Engine + Docker Compose (Plugin)
- Git
- Port 8080 in der Firewall freigegeben
- Repo als `~/MovieReviews` geklont
- SSH-Key für `git pull` (z. B. Deploy-Key) oder öffentliches Repo

## Lokales Deploy-Skript

```sh
./deploy.sh <ipv4> [ssh-user]
```

SSHet in die VM, pullt, baut und startet neu.
