# Vaadin Frontend

## Stack
- **Vaadin 25.1** (Java Full-Stack – kein REST/SPA, Backend rendert UI)
- **Spring Boot 4.0.6** (Java 25)
- Maven Wrapper (`./mvnw`, nie system `mvn`)

## Routen

| Pfad | View | Status |
|------|------|--------|
| `/` | MovieView | fertig |
| `/add-review` | AddRatingView | fertig |

## Dev-Profil
- `defer-datasource-initialization: true` (JPA erstellt Tabellen vor `data.sql`)
- `spring.sql.init.mode: always` (`data.sql` läuft bei jedem Start)
- Hotdeploy + Browser-Launch aktiv

## Vaadin Grid + Lazy Loading

`MovieView` nutzt lazy `setItems(query -> ...)`. Query-Objekt enthält `QuerySortOrder`-Liste und Filter-String.

```java
// Service: QuerySortOrder -> Spring Sort
private Sort toSort(List<QuerySortOrder> sortOrders) {
    if (sortOrders == null || sortOrders.isEmpty()) return Sort.unsorted();
    return Sort.by(sortOrders.stream()
        .map(o -> new Sort.Order(
            o.getDirection() == SortDirection.ASCENDING
                ? Sort.Direction.ASC : Sort.Direction.DESC,
            o.getSorted()))
        .toList());
}
```

## Vaadin MCP

Bei Vaadin-Fragen das Vaadin MCP (`opencode.json` → `mcp.vaadin`) als API-Referenz nutzen.
