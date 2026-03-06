package com.charble.backend.stats;

import com.charble.backend.model.Category;
import com.charble.backend.model.Score;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.ScoreRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 Manages statistics calculations and data retrieval for scoring categories.
 */
@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    private final CategoryRepository categoryRepository;
    private final ScoreRepository scoreRepository;
    private final StatisticsService statisticsService;

    public StatisticsController(
            CategoryRepository categoryRepository,
            ScoreRepository scoreRepository,
            StatisticsService statisticsService
    ) {
        this.categoryRepository = categoryRepository;
        this.scoreRepository = scoreRepository;
        this.statisticsService = statisticsService;
    }

    /*
     Fetches a list of all available scoring categories.
     Uses a 'Summary' record to exclude sensitive or bulky metadata from the response.
     */
    @GetMapping("/categories")
    public List<CategorySummary> categories() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategorySummary(
                        category.categoryId(),
                        category.units(),
                        category.sortOrder()
                ))
                .toList();
    }

    /*
     Retrieves detailed stats for a specific category.
     @param userValue An optional score from the current user to see how they rank against others.
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> categoryStats(
            @PathVariable String categoryId,
            @RequestParam(required = false) Double userValue
    ) {
        // Since PathVariables come in as Strings, we manually validate the UUID format 
        UUID parsedCategoryId;
        try {
            parsedCategoryId = UUID.fromString(categoryId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category id format."));
        }

        return categoryRepository.findById(parsedCategoryId)
                .<ResponseEntity<?>>map(category -> {
                    List<Double> values = extractValuesForCategory(category.categoryId());
                    
                    // Avoiding passing an empty list to the calculation service.
                    if (values.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", "No score data found for category: " + categoryId));
                    }

                    // Perform the statistical math via the service.
                    StatsSummary stats = statisticsService.calculate(values, userValue);
                    return ResponseEntity.ok(new CategoryStatsResponse(
                            category.categoryId(),
                            category.units(),
                            category.sortOrder(),
                            stats
                    ));
                })
                // Triggered if the UUID is valid but doesn't exist in the database.
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Unknown category: " + categoryId)));
    }

    /*
     Allows clients to calculate stats on-the-fly by sending their own list of numbers
     */
    @PostMapping("/compute")
    public ResponseEntity<?> compute(@RequestBody ComputeStatsRequest request) {
        if (request == null || request.values() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body must include values."));
        }

        try {
            return ResponseEntity.ok(statisticsService.calculate(request.values(), request.userValue()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Records are used here as Data Transfer Object
    public record ComputeStatsRequest(List<Double> values, Double userValue) {
    }

    public record CategorySummary(UUID categoryId, String unitOfMeasurement, Boolean sortOrder) {
    }

    public record CategoryStatsResponse(
            UUID categoryId,
            String unitOfMeasurement,
            Boolean sortOrder,
            StatsSummary statistics
    ) {
    }

    /*
     Helper method to filter the global scores list down to values belonging to one category.
     */
    private List<Double> extractValuesForCategory(UUID categoryId) {
        return scoreRepository.findAll().stream()
                .filter(score -> belongsToCategory(score, categoryId))
                .map(Score::score)
                // Ensure we don't pass nulls or incompatible types to math functions.
                .filter(Objects::nonNull)
                .map(Float::doubleValue)
                .toList();
    }

    
    private boolean belongsToCategory(Score score, UUID categoryId) {
        Category scoreCategory = score.category();
        return scoreCategory != null && scoreCategory.categoryId() != null
                && scoreCategory.categoryId().equals(categoryId);
    }
}
