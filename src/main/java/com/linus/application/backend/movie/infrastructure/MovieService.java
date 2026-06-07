package com.linus.application.backend.movie.infrastructure;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MovieService(MovieRepository movieRepository, ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Transactional
    public Slice<Movie> getAllMovies(int offset, int limit, List<QuerySortOrder> sortOrders, String filterValue) {
        Pageable pageable = PageRequest.of(offset, limit, toSort(sortOrders));
        if (filterValue == null || filterValue.isBlank()) {
            return movieRepository.findAll(pageable);
        }
        return movieRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(filterValue, filterValue, pageable);
    }

    @Transactional
    @RolesAllowed("admin")
    public Movie saveMovie(String title, String description, String imdbId, MovieType type) {
        if (imdbId == null || imdbId.isBlank()) {
            imdbId = searchImdbId(title);
        }
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setImdbId(imdbId);
        movie.setType(type);
        return movieRepository.save(movie);
    }

    public String searchImdbId(String title) {
        if (title == null || title.isBlank()) return null;
        try {
            String encoded = java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.imdbapi.dev/search/titles?query=" + encoded))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode titles = root.get("titles");
            if (titles == null || !titles.isArray() || titles.isEmpty()) return null;

            return titles.get(0).get("id").asText();
        } catch (Exception e) {
            return null;
        }
    }

    public String fetchPosterUrl(String imdbId) {
        if (imdbId == null || imdbId.isBlank()) return null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.imdbapi.dev/titles/" + imdbId + "/images"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode images = root.get("images");
            if (images == null || !images.isArray()) return null;

            for (JsonNode image : images) {
                if ("poster".equals(image.get("type").asText())) {
                    return image.get("url").asText();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public List<Movie> getAllMoviesList() {
        List<Movie> movies = new ArrayList<>();
        movieRepository.findAll(Sort.unsorted()).forEach(movies::add);
        return movies;
    }

    private Sort toSort(List<QuerySortOrder> sortOrders) {
        if (sortOrders == null || sortOrders.isEmpty()) {
            return Sort.unsorted();
        }
        return Sort.by(sortOrders.stream()
                .map(order -> new Sort.Order(
                        order.getDirection() == SortDirection.ASCENDING
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC,
                        order.getSorted()))
                .toList());
    }
}
