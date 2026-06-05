package com.linus.application.backend.movie.infrastructure;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        Pageable pageable = PageRequest.of(offset, limit, toSpringSort(sortOrders));
        Specification<Movie> filter = toFilterSpecification(filterValue);
        return movieRepository.findAll(filter, pageable);
    }

    private Sort toSpringSort(List<QuerySortOrder> sortOrders) {
        if (sortOrders == null || sortOrders.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sortOrders.stream()
                .map(order -> new Sort.Order(
                        order.getDirection() == SortDirection.ASCENDING
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC,
                        order.getSorted()))
                .toList();
        return Sort.by(orders);
    }

    private Specification<Movie> toFilterSpecification(String filterValue) {
        if (filterValue == null || filterValue.isBlank()) {
            return null;
        }
        return (root, query, cb) -> {
            String pattern = "%" + filterValue.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
