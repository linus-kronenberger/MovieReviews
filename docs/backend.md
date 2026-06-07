# Backend-Struktur

## Package-by-Feature

```
backend.{domain}.infrastructure   # Entity, Repository, Service
backend.{domain}.api              # REST-Controller (falls nötig)
```

Verfügbare Domänen:
- `movie` – Entity + Repository + Service (vollständig)
- `rating` – Entity + Repository + Service, `rating_category` Sub-Entity
- `essay` – Entity + Repository, Service (leer)
- `user` – Entity + Repository, Role-Enum (USER, ADMIN)

## Datenbank

| Tabelle | Spalten |
|---------|---------|
| `movie` | movie_id (PK), title, description |
| `rating` | rating_id (PK), movie_id (FK→movie), category_id (FK→rating_category), user_id (FK→users), stars (1–10) |
| `rating_category` | category_id (PK), category, description |
| `users` | user_id (PK), username (unique), name, password_hash, role |
| `essay` | essay_id (PK), rating_id (FK→rating), text, version |

`data.sql` seedt 100 Marvel-Filme + 2 User (user/admin) + 3 Kategorien (Story, Acting, Visuals) bei jedem Dev-Start.

## Code Style

- Keine Kommentare
- Keine Emojis
- Nie commiten/pushen – macht der User manuell
- `@Transactional` bei Service-Methoden
- Tabellen: `snake_case` mit `@Column(name = "...")`
