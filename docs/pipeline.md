# CI/CD & Deploy

## GitHub Actions (`.github/workflows/deploy.yml`)

- Läuft bei Push auf `main` oder manuell via `workflow_dispatch`
- Erwartet Secrets: `VM_HOST`, `VM_USER`, `SSH_PRIVATE_KEY`
- CI: `mvn clean package -DskipTests` → `verify -Pit`

## Docker / Production

```sh
./mvnw package -DskipTests && java -jar target/*.jar --spring.profiles.active=prod
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
