//package com.charble.backend.stats;
//
//import com.charble.backend.model.Category;
//import com.charble.backend.model.GlobalBaseline;
//import com.charble.backend.model.Score;
//import com.charble.backend.repository.CategoryRepository;
//import com.charble.backend.repository.GlobalBaselineRepository;
//import com.charble.backend.repository.ScoreRepository;
//import com.charble.backend.service.provider.OwidHeightBaselineService;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.UUID;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
///*
// Manages statistics calculations and data retrieval for scoring categories.
// */
//@RestController
//@RequestMapping("/api/stats")
//public class StatisticsController {
//
//    private final CategoryRepository categoryRepository;
//    private final GlobalBaselineRepository globalBaselineRepository;
//    private final ScoreRepository scoreRepository;
//    private final StatisticsService statisticsService;
//    private final OwidHeightBaselineService owidHeightBaselineService;
//
//    public StatisticsController(
//            CategoryRepository categoryRepository,
//            GlobalBaselineRepository globalBaselineRepository,
//            ScoreRepository scoreRepository,
//            StatisticsService statisticsService,
//            OwidHeightBaselineService owidHeightBaselineService
//    ) {
//        this.categoryRepository = categoryRepository;
//        this.globalBaselineRepository = globalBaselineRepository;
//        this.scoreRepository = scoreRepository;
//        this.statisticsService = statisticsService;
//        this.owidHeightBaselineService = owidHeightBaselineService;
//    }
//
//    /*
//     Fetches a list of all available scoring categories.
//     Uses a 'Summary' record to exclude sensitive or bulky metadata from the response.
//     */
//    @GetMapping("/categories")
//    public List<CategorySummary> categories() {
//        return categoryRepository.findAll().stream()
//                .map(category -> new CategorySummary(
//                        category.getCategoryId(),
//                        category.getUnits(),
//                        category.getSortOrder()
//                ))
//                .toList();
//    }
//
//    /*
//     Retrieves detailed stats for a specific category.
//     @param userValue An optional score from the current user to see how they rank against others.
//     */
//    @GetMapping("/categories/{categoryId}")
//    public ResponseEntity<?> categoryStats(
//            @PathVariable String categoryId,
//            @RequestParam(required = false) Double userValue
//    ) {
//        // Since PathVariables come in as Strings, we manually validate the UUID format
//        UUID parsedCategoryId;
//        try {
//            parsedCategoryId = UUID.fromString(categoryId);
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category id format."));
//        }
//
//        return categoryRepository.findById(parsedCategoryId)
//                .<ResponseEntity<?>>map(category -> {
//                    List<Double> values = extractValuesForCategory(category.getCategoryId());
//
//                    // Avoiding passing an empty list to the calculation service.
//                    if (values.isEmpty()) {
//                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                                .body(Map.of("error", "No score data found for category: " + categoryId));
//                    }
//
//                    // Perform the statistical math via the service.
//                    StatsSummary stats = statisticsService.calculate(values, userValue);
//                    return ResponseEntity.ok(new CategoryStatsResponse(
//                            category.getCategoryId(),
//                            category.getUnits(),
//                            category.getSortOrder(),
//                            stats
//                    ));
//                })
//                // Triggered if the UUID is valid but doesn't exist in the database.
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of("error", "Unknown category: " + categoryId)));
//    }
//
//    /*
//     Allows clients to calculate stats on-the-fly by sending their own list of numbers
//     */
//    @PostMapping("/compute")
//    public ResponseEntity<?> compute(@RequestBody ComputeStatsRequest request) {
//        if (request == null || request.values() == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Request body must include values."));
//        }
//
//        try {
//            return ResponseEntity.ok(statisticsService.calculate(request.values(), request.userValue()));
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
//        }
//    }
//
//    @PostMapping("/global/height/refresh")
//    public Map<String, Object> refreshHeightBaselineFromOwid() {
//        OwidHeightBaselineService.HeightBaselineResult result =
//                owidHeightBaselineService.fetchAndSaveHeightBaseline();
//
//        return Map.of(
//                "message", "OWID height baseline updated",
//                "categoryId", result.categoryId(),
//                "latestYear", result.latestYear(),
//                "sampleSize", result.sampleSize(),
//                "mean", result.mean(),
//                "median", result.median(),
//                "standardDeviation", result.standardDeviation(),
//                "sourceName", result.sourceName()
//        );
//    }
//
//    @GetMapping("/global/{categoryId}/compare")
//    public ResponseEntity<?> compareAgainstGlobalBaseline(
//            @PathVariable String categoryId,
//            @RequestParam(required = false) Double userValue,
//            @RequestParam(required = false) Integer feet,
//            @RequestParam(required = false) Double inches
//    ) {
//        UUID parsedCategoryId;
//        try {
//            parsedCategoryId = UUID.fromString(categoryId);
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category id format."));
//        }
//
//        Category category = categoryRepository.findById(parsedCategoryId).orElse(null);
//        if (category == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "Unknown category: " + categoryId));
//        }
//
//        GlobalBaseline baseline = globalBaselineRepository.findByCategory(category).orElse(null);
//        if (baseline == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "No global baseline found for category: " + categoryId));
//        }
//
//        Float meanValue = baseline.getMean();
//        Float standardDeviationValue = baseline.getStandardDeviation();
//        if (meanValue == null || standardDeviationValue == null) {
//            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
//                    .body(Map.of("error", "Baseline is missing mean or standard deviation values."));
//        }
//
//        final double normalizedUserValue;
//        final String inputUnitDescription;
//        try {
//            normalizedUserValue = resolveComparisonValue(category.getUnits(), userValue, feet, inches);
//            inputUnitDescription = userValue != null
//                    ? category.getUnits()
//                    : "ft/in converted to " + category.getUnits();
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
//        }
//
//        double mean = meanValue.doubleValue();
//        double standardDeviation = standardDeviationValue.doubleValue();
//        Double zScore = standardDeviation > 0.0 ? (normalizedUserValue - mean) / standardDeviation : null;
//        double percentile = percentileFromBaseline(normalizedUserValue, mean, standardDeviation);
//
//        return ResponseEntity.ok(new GlobalComparisonResponse(
//                category.getCategoryId(),
//                category.getName(),
//                category.getUnits(),
//                baseline.getSourceName(),
//                baseline.getSampleSize(),
//                mean,
//                baseline.getMedian() == null ? null : baseline.getMedian().doubleValue(),
//                standardDeviation,
//                normalizedUserValue,
//                inputUnitDescription,
//                zScore,
//                percentile,
//                (int) Math.round(percentile),
//                distributionBand(zScore),
//                "Percentile uses normal-distribution approximation from baseline mean/std-dev."
//        ));
//    }
//
//    // Records are used here as Data Transfer Object
//    public record ComputeStatsRequest(List<Double> values, Double userValue) {
//    }
//
//    public record CategorySummary(UUID categoryId, String unitOfMeasurement, Boolean sortOrder) {
//    }
//
//    public record CategoryStatsResponse(
//            UUID categoryId,
//            String unitOfMeasurement,
//            Boolean sortOrder,
//            StatsSummary statistics
//    ) {
//    }
//
//    public record GlobalComparisonResponse(
//            UUID categoryId,
//            String categoryName,
//            String unitOfMeasurement,
//            String sourceName,
//            Integer baselineSampleSize,
//            double baselineMean,
//            Double baselineMedian,
//            double baselineStandardDeviation,
//            double normalizedUserValue,
//            String normalizedUserValueUnit,
//            Double zScore,
//            double percentile,
//            int rankOutOf100,
//            String distributionBand,
//            String percentileMethod
//    ) {
//    }
//
//    /*
//     Helper method to filter the global scores list down to values belonging to one category.
//     */
//    private List<Double> extractValuesForCategory(UUID categoryId) {
//        return scoreRepository.findAll().stream()
//                .filter(score -> belongsToCategory(score, categoryId))
//                .map(Score::getScore)
//                // Ensure we don't pass nulls or incompatible types to math functions.
//                .filter(Objects::nonNull)
//                .map(Float::doubleValue)
//                .toList();
//    }
//
//
//    private boolean belongsToCategory(Score score, UUID categoryId) {
//        Category scoreCategory = score.getCategory();
//        return scoreCategory != null && scoreCategory.getCategoryId() != null
//                && scoreCategory.getCategoryId().equals(categoryId);
//    }
//
//    private double resolveComparisonValue(
//            String categoryUnit,
//            Double userValue,
//            Integer feet,
//            Double inches
//    ) {
//        if (userValue != null) {
//            return userValue;
//        }
//
//        if (feet == null) {
//            throw new IllegalArgumentException(
//                    "Provide userValue in category units, or provide feet (+ optional inches).");
//        }
//
//        double inchPart = inches == null ? 0.0 : inches;
//        if (feet < 0 || inchPart < 0.0 || inchPart >= 12.0) {
//            throw new IllegalArgumentException("feet must be >= 0 and inches must be between 0 and < 12.");
//        }
//
//        double totalInches = (feet * 12.0) + inchPart;
//        if (totalInches <= 0.0) {
//            throw new IllegalArgumentException("Height must be greater than zero.");
//        }
//
//        String normalizedUnit = categoryUnit == null ? "" : categoryUnit.trim().toLowerCase();
//        return switch (normalizedUnit) {
//            case "cm" -> totalInches * 2.54;
//            case "m" -> totalInches * 0.0254;
//            case "in", "inch", "inches" -> totalInches;
//            case "ft", "foot", "feet" -> totalInches / 12.0;
//            default -> throw new IllegalArgumentException(
//                    "feet/inches conversion is supported only for cm, m, in, or ft categories.");
//        };
//    }
//
//    private double percentileFromBaseline(double userValue, double mean, double standardDeviation) {
//        if (standardDeviation <= 0.0) {
//            if (Double.compare(userValue, mean) < 0) {
//                return 0.0;
//            }
//            if (Double.compare(userValue, mean) > 0) {
//                return 100.0;
//            }
//            return 50.0;
//        }
//
//        double z = (userValue - mean) / standardDeviation;
//        return normalCdf(z) * 100.0;
//    }
//
//    private String distributionBand(Double zScore) {
//        if (zScore == null) {
//            return "No spread in baseline (std dev = 0)";
//        }
//
//        if (zScore < -2.0) {
//            return "Below -2σ";
//        }
//        if (zScore < -1.0) {
//            return "Between -2σ and -1σ";
//        }
//        if (zScore < 0.0) {
//            return "Between -1σ and mean";
//        }
//        if (zScore < 1.0) {
//            return "Between mean and +1σ";
//        }
//        if (zScore < 2.0) {
//            return "Between +1σ and +2σ";
//        }
//        return "Above +2σ";
//    }
//
//    private double normalCdf(double z) {
//        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
//    }
//
//    // Abramowitz and Stegun approximation for error function.
//    private double erf(double value) {
//        double sign = value < 0 ? -1.0 : 1.0;
//        double x = Math.abs(value);
//
//        double a1 = 0.254829592;
//        double a2 = -0.284496736;
//        double a3 = 1.421413741;
//        double a4 = -1.453152027;
//        double a5 = 1.061405429;
//        double p = 0.3275911;
//
//        double t = 1.0 / (1.0 + p * x);
//        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
//        return sign * y;
//    }
//}
