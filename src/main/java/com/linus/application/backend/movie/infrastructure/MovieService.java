package com.linus.application.backend.movie.infrastructure;

import com.vaadin.flow.data.provider.QuerySortOrder;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Slice<Movie> getAllMovies(int offset, int limit, List<QuerySortOrder>  sortOrders, String filterValue) {
        Pageable pageable = PageRequest.of(offset, limit);

        return movieRepository.findAll(pageable);
    }
}
