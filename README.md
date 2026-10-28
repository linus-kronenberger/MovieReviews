# MovieReviews

## Homepage

[Link zur Homepage](http://72.62.57.181:8080)

Persönliche Web-App zum Bewerten und Reviewen von Filmen – ideal für den großen Marvel-Marathon.

Mit dieser App kannst du Filme anlegen, nach Kategorien (Story, Acting, Visuals) mit 1–10 Sternen bewerten, Essays schreiben und alle Bewertungen auf einen Blick sehen.

## Technologie-Stack

- **Vaadin 25** (Java Full-Stack, kein REST/SPA – das Backend rendert die UI)
- **Spring Boot 4** (Java 25)
- **H2** in Entwicklung, **PostgreSQL** in Produktion
- **Maven Wrapper**

## Schnellstart (Entwicklung)

```sh
./mvnw spring-boot:run
```

Die App ist dann unter [http://localhost:8080](http://localhost:8080) erreichbar.  
Es werden automatisch 100 Marvel-Filme in die lokale H2-Datenbank geladen.

## Produktion mit Docker

```sh
cp .env.example .env
openssl rand -hex 32
openssl rand -base64 32
```

Die beiden generierten Werte als `DB_PASSWORD` und `JWT_AUTH_SECRET` in `.env` eintragen, dann starten:

```sh
docker compose up --build -d --remove-orphans
```

Startet PostgreSQL 16 und die App im Produktionsmodus auf Port 8080. Docker baut Java und das Vaadin-Frontend; Java und Maven müssen auf dem VPS nicht installiert werden. `.env` wird weder eingecheckt noch ins Docker-Build übertragen.

| Variable | Default | Beschreibung |
|---|---|---|
| `DB_NAME` | `moviereviews` | Datenbankname |
| `DB_USERNAME` | `moviereviews` | DB-Benutzer |
| `DB_PASSWORD` | `moviereviews` | Eigenes starkes Passwort in `.env` setzen |
| `APP_PORT` | `8080` | Veröffentlichter App-Port auf dem VPS |
| `JWT_AUTH_SECRET` | Pflichtfeld | Zufälliger Base64-Schlüssel, erzeugt mit `openssl rand -base64 32` |

Innerhalb von Docker Compose nutzt die App `DB_HOST=db` und `DB_PORT=5432`. PostgreSQL ist nicht auf einem Port des VPS veröffentlicht.

## VPS einrichten (Ubuntu/Debian)

### 1. Docker, Compose und Git installieren

Per SSH mit einem Benutzer mit `sudo`-Rechten anmelden. Die folgenden Befehle richten das offizielle Docker-Paketrepository ein:

```sh
sudo apt-get update
sudo apt-get install -y ca-certificates curl git nano openssl
sudo install -m 0755 -d /etc/apt/keyrings
. /etc/os-release
curl -fsSL "https://download.docker.com/linux/$ID/gpg" | sudo tee /etc/apt/keyrings/docker.asc > /dev/null
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/$ID $VERSION_CODENAME stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

Danach abmelden und erneut per SSH anmelden, damit die Docker-Gruppenzugehörigkeit aktiv wird. Der spätere Deployment-Benutzer benötigt ebenfalls Docker-Zugriff; die Docker-Gruppe hat administrative Rechte auf dem VPS. Prüfen:

```sh
docker version
docker compose version
```

### 2. Repository und Konfiguration vorbereiten

Als derselbe Benutzer arbeiten, der später unter `VM_USER` hinterlegt wird:

```sh
git clone https://github.com/linus-kronenberger/MovieReviews.git ~/MovieReviews
cd ~/MovieReviews
git switch master
cp .env.example .env
chmod 600 .env
openssl rand -hex 32
openssl rand -base64 32
nano .env
```

`DB_PASSWORD` mit dem Hex-Wert und `JWT_AUTH_SECRET` mit dem Base64-Wert befüllen. Für ein vorhandenes `~/MovieReviews` stattdessen vor dem Kopieren der Vorlage den Remote wechseln:

```sh
cd ~/MovieReviews
git remote set-url origin https://github.com/linus-kronenberger/MovieReviews.git
git switch master
git pull --ff-only origin master
```

Eine bestehende `.env` beibehalten und um `JWT_AUTH_SECRET` ergänzen. Bei einem bereits vorhandenen PostgreSQL-Volume müssen `DB_NAME`, `DB_USERNAME` und `DB_PASSWORD` den bisherigen Werten entsprechen. Neue Umgebungswerte ändern die Zugangsdaten einer bereits initialisierten Datenbank nicht.

Das öffentliche GitHub-Repository kann ohne zusätzlichen Schlüssel geklont werden. Bei einem privaten Repository braucht der VPS eigenen GitHub-Lesezugriff, beispielsweise einen GitHub-Deploy-Key und eine Git-`insteadOf`-Regel, die diese HTTPS-URL auf die entsprechende SSH-URL umschreibt. Der Actions-SSH-Schlüssel für den VPS erteilt keinen GitHub-Zugriff.

### 3. App starten und prüfen

```sh
cd ~/MovieReviews
docker compose up --build -d --remove-orphans
docker compose ps
docker compose logs --tail=100 app
curl -I http://localhost:8080
```

Die App ist unter `http://VPS-IP:8080` erreichbar. Port 8080 (beziehungsweise `APP_PORT`) in der Firewall des Providers freigeben. Bei aktiver UFW zusätzlich `sudo ufw allow 8080/tcp` ausführen; SSH-Zugriff muss weiterhin erlaubt bleiben. Für den Betrieb über eine Domain HTTPS mit einem Reverse Proxy wie Caddy oder Nginx einrichten.

PostgreSQL-Daten bleiben im Docker-Volume `postgres_data` erhalten. `docker compose down -v` löscht dieses Volume samt Daten. Für Updates reicht `docker compose up --build -d --remove-orphans`; die Datenbank muss dafür nicht heruntergefahren werden.

## GitHub Actions und automatisches Deployment

Der Workflow [CI and Deploy](.github/workflows/deploy.yml) baut und prüft bei jedem Push, Pull Request und manuellen Start das Produktionspaket mit Java 25 und Node 24. Eine TestBench-Lizenz ist dafür nicht erforderlich. Nach erfolgreichem Build auf `master` wird genau der geprüfte Commit per SSH auf dem VPS ausgecheckt und mit Docker Compose gestartet. Pull Requests und andere Branches lösen kein Deployment aus.

### SSH-Zugang für Actions einrichten

Auf dem eigenen Rechner einen separaten Schlüssel ohne Passphrase erstellen:

```sh
ssh-keygen -t ed25519 -C github-actions-moviereviews -f ./moviereviews-deploy -N ''
ssh-copy-id -i ./moviereviews-deploy.pub DEPLOY_USER@VPS-IP
```

Damit liegt der öffentliche Schlüssel in `~/.ssh/authorized_keys` des Deployment-Benutzers auf dem VPS. Dieser Benutzer muss `~/MovieReviews` und `.env` besitzen sowie Docker ohne `sudo` ausführen können. Der private Schlüssel bleibt außerhalb des Repositorys.

Den SSH-Hostkey über die vertrauenswürdige VPS-Konsole prüfen:

```sh
sudo ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

Auf dem eigenen Rechner die `known_hosts`-Zeile erfassen und ihren Fingerprint mit dem Wert auf dem VPS vergleichen:

```sh
ssh-keyscan -t ed25519 VPS-IP > moviereviews-known-hosts
ssh-keygen -lf moviereviews-known-hosts
```

In GitHub unter **Settings → Secrets and variables → Actions → New repository secret** anlegen:

| Secret | Wert |
|---|---|
| `VM_HOST` | VPS-IP oder Hostname; muss dem Eintrag in `SSH_KNOWN_HOSTS` entsprechen |
| `VM_USER` | SSH-Benutzer mit Docker-Zugriff |
| `SSH_PRIVATE_KEY` | Vollständiger Inhalt von `moviereviews-deploy`, einschließlich BEGIN/END-Zeilen; kein Base64-Encoding |
| `SSH_KNOWN_HOSTS` | Inhalt von `moviereviews-known-hosts` nach dem Fingerprint-Vergleich |

`DB_PASSWORD` und `JWT_AUTH_SECRET` bleiben in `.env` auf dem VPS. Der Workflow verwendet SSH-Port 22 und prüft den hinterlegten Hostkey. Nach einem Push beziehungsweise Merge nach `master` lässt sich das Ergebnis im GitHub-Tab **Actions** verfolgen. Für einen manuellen Start **CI and Deploy → Run workflow → Branch: master** wählen. Deployments laufen nacheinander; überholte Runs werden übersprungen, sobald `master` einen neueren Commit enthält.

Nach einem automatischen Deployment steht das VPS-Repository auf einem bestimmten Commit (detached HEAD). Für ein manuelles Update auf dem VPS:

```sh
cd ~/MovieReviews
git switch master
git pull --ff-only origin master
docker compose up --build -d --remove-orphans
```

Alternativ vom eigenen Rechner: `./deploy.sh VPS-IP DEPLOY_USER`.

## Features

- **Filmliste** (`/`) – Übersicht aller Filme mit Filter, Sortierung und Klick für Bewertungs-Detail
- **Neue Bewertung** (`/add-review`) – Film auswählen, Kategorie wählen, Sterne vergeben, Essay schreiben und speichern
- **Film anlegen** – direkt auf der Filmliste per Titel- und Beschreibungsfeld

## Projektstruktur

```
com.linus.application
  Application.java               # @SpringBootApplication
  ui.MainLayout                  # App Shell mit Drawer-Navigation
  ui.movies_view.MovieView       # Filmliste (lazy Grid)
  ui.movies_view.popup.MoviePopUp # Bewertungs-Dialog
  ui.add_rating_view.AddRatingView # Bewertung anlegen
  backend.movie.infrastructure   # Movie-Entity, Repository, Service
  backend.review.infrastructure  # Rating/RatingCategory-Entity, Repository, Service
  backend.essay.infrastructure   # Essay-Entity, Repository
  backend.user.infrastructure    # User-Entity (vorbereitet)
```
