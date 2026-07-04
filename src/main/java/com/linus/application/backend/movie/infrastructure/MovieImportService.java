package com.linus.application.backend.movie.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class MovieImportService {

    private static final Logger log = LoggerFactory.getLogger(MovieImportService.class);

    private final MovieRepository movieRepository;
    private final MovieService movieService;

    public MovieImportService(MovieRepository movieRepository, MovieService movieService) {
        this.movieRepository = movieRepository;
        this.movieService = movieService;
    }

    @Transactional
    public void importFromCsv() {
        if (movieRepository.count() > 0) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("movies.csv").getInputStream(),
                        StandardCharsets.UTF_8))) {

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = parseCsvLine(line);
                if (parts.length < 2) continue;

                String title = parts[0];
                String description = parts[1];
                String imdbId = parts.length > 2 ? parts[2] : movieService.searchImdbId(title);

                Movie movie = new Movie();
                movie.setTitle(title);
                movie.setDescription(description);
                movie.setImdbId(imdbId);
                movie.setType(MovieType.FILM);
                movieRepository.save(movie);
                count++;

                if (count % 10 == 0) {
                    log.info("{} Filme importiert...", count);
                }

                Thread.sleep(200);
            }

            log.info("Import abgeschlossen: {} Filme", count);
        } catch (Exception e) {
            log.error("Fehler beim CSV-Import", e);
        }
    }

    private String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()) return new String[0];
        if (!line.startsWith("\"")) return new String[0];

        java.util.List<String> fields = new java.util.ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            if (line.charAt(i) == '\"') {
                StringBuilder field = new StringBuilder();
                i++;
                while (i < line.length()) {
                    if (line.charAt(i) == '\"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                            field.append('\"');
                            i += 2;
                        } else {
                            i++;
                            break;
                        }
                    } else {
                        field.append(line.charAt(i));
                        i++;
                    }
                }
                fields.add(field.toString());
                if (i < line.length() && line.charAt(i) == ';') {
                    i++;
                }
            } else {
                i++;
            }
        }
        return fields.toArray(new String[0]);
    }
}
