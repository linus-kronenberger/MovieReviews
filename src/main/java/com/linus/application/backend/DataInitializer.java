package com.linus.application.backend;

import com.linus.application.backend.movie.infrastructure.MovieImportService;
import com.linus.application.backend.rating.infrastructure.RatingCategory;
import com.linus.application.backend.rating.infrastructure.RatingCategoryRepository;
import com.linus.application.backend.user.infrastructure.Role;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RatingCategoryRepository ratingCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final MovieImportService movieImportService;

    public DataInitializer(UserRepository userRepository,
                           RatingCategoryRepository ratingCategoryRepository,
                           PasswordEncoder passwordEncoder,
                           MovieImportService movieImportService) {
        this.userRepository = userRepository;
        this.ratingCategoryRepository = ratingCategoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.movieImportService = movieImportService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        userRepository.findByUsername("admin").ifPresent(userRepository::delete);

        String password = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String hash = passwordEncoder.encode(password);

        User admin = new User();
        admin.setUsername("admin");
        admin.setName("Admin");
        admin.setPasswordHash(hash);
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        log.info("========================================");
        log.info("Admin-Zugang:");
        log.info("  Username: admin");
        log.info("  Passwort: {}", password);
        log.info("========================================");

        List<RatingCategory> categories = List.of(
                createCategory(admin, "Story", "Bewertung der Handlung und des Drehbuchs"),
                createCategory(admin, "Acting", "Bewertung der schauspielerischen Leistung"),
                createCategory(admin, "Visuals", "Bewertung der visuellen Effekte und Kinematographie")
        );
        ratingCategoryRepository.saveAll(categories);

        movieImportService.importFromCsv();
    }

    private RatingCategory createCategory(User user, String name, String description) {
        RatingCategory category = new RatingCategory();
        category.setCategory(name);
        category.setDescription(description);
        category.setUser(user);
        return category;
    }
}
