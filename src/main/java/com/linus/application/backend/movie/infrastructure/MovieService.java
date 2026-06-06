package com.linus.application.backend.movie.infrastructure;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
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
    public Movie saveMovie(String title, String description) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        return movieRepository.save(movie);
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
