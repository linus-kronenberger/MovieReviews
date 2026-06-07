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
Es werden automatisch 100 Marvel-Filme in die H2-In-Memory-Datenbank geladen.

## Produktion mit Docker

```sh
docker compose up -d
```

Startet PostgreSQL 16 und die App (Port 8080). Alle Werte sind über Umgebungsvariablen konfigurierbar:

| Variable | Default | Beschreibung |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_PORT` | `5432` | PostgreSQL Port |
| `DB_NAME` | `moviereviews` | Datenbankname |
| `DB_USERNAME` | `postgres` | DB-Benutzer |
| `DB_PASSWORD` | `postgres` | DB-Passwort |
| `APPLICATION_PORT` | `8080` | App-Port |

## Features

- **Filmliste** (`/`) – Übersicht aller Filme mit Filter, Sortierung und Klick für Bewertungs-Detail
- **Neue Bewertung** (`/add-rating`) – Film auswählen, Kategorie wählen, Sterne vergeben, Essay schreiben und speichern
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
  backend.rating.infrastructure  # Rating/RatingCategory-Entity, Repository, Service
  backend.essay.infrastructure   # Essay-Entity, Repository
  backend.user.infrastructure    # User-Entity (vorbereitet)
```
