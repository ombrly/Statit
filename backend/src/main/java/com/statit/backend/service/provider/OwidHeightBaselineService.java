//package com.charble.backend.service.provider;
//
//import com.charble.backend.model.Category;
//import com.charble.backend.model.GlobalBaseline;
//import com.charble.backend.model.User;
//import com.charble.backend.model.enums.Region;
//import com.charble.backend.model.enums.Sex;
//import com.charble.backend.repository.CategoryRepository;
//import com.charble.backend.repository.GlobalBaselineRepository;
//import com.charble.backend.repository.UserRepository;
//import com.charble.backend.stats.StatisticsService;
//import com.charble.backend.stats.StatsSummary;
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OwidHeightBaselineService {
//    private static final String HEIGHT_CATEGORY_NAME = "Average Adult Height";
//    private static final String HEIGHT_UNIT = "cm";
//    private static final String SYSTEM_USERNAME = "systemadmin";
//    private static final String SYSTEM_EMAIL = "system@charble.com";
//    private static final String PLACEHOLDER_HASH = "placeholder_hash";
//
//    private final CategoryRepository categoryRepository;
//    private final GlobalBaselineRepository globalBaselineRepository;
//    private final UserRepository userRepository;
//    private final StatisticsService statisticsService;
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//
//    @Value("${owid.height.csv-url:https://ourworldindata.org/grapher/average-height-by-year-of-birth.csv}")
//    private String owidHeightCsvUrl;
//
//    public OwidHeightBaselineService(
//            CategoryRepository categoryRepository,
//            GlobalBaselineRepository globalBaselineRepository,
//            UserRepository userRepository,
//            StatisticsService statisticsService
//    ) {
//        this.categoryRepository = categoryRepository;
//        this.globalBaselineRepository = globalBaselineRepository;
//        this.userRepository = userRepository;
//        this.statisticsService = statisticsService;
//    }
//
//    public HeightBaselineResult fetchAndSaveHeightBaseline() {
//        String csv = fetchCsv(owidHeightCsvUrl);
//        List<HeightObservation> observations = parseHeightObservations(csv);
//
//        if (observations.isEmpty()) {
//            throw new IllegalStateException("No OWID height observations were parsed.");
//        }
//
//        int latestYear = observations.stream()
//                .map(HeightObservation::year)
//                .max(Comparator.naturalOrder())
//                .orElseThrow();
//
//        List<Double> latestYearValues = observations.stream()
//                .filter(observation -> observation.year() == latestYear)
//                .map(HeightObservation::value)
//                .toList();
//
//        if (latestYearValues.isEmpty()) {
//            throw new IllegalStateException("No height values found for latest year: " + latestYear);
//        }
//
//        StatsSummary summary = statisticsService.calculate(latestYearValues, null);
//
//        User systemUser = userRepository.findByUsername(SYSTEM_USERNAME)
//                .orElseGet(() -> userRepository.save(new User(
//                        SYSTEM_USERNAME,
//                        SYSTEM_EMAIL,
//                        PLACEHOLDER_HASH,
//                        null,
//                        null
//                )));
//
//        Category heightCategory = categoryRepository.findByCategoryName(HEIGHT_CATEGORY_NAME)
//                .orElseGet(() -> categoryRepository.save(
//                        new Category(HEIGHT_CATEGORY_NAME, null, null, HEIGHT_UNIT, false, systemUser)));
//
//        String sourceName = "OWID average-height-by-year-of-birth (latest year " + latestYear + ")";
//
//        GlobalBaseline baseline = globalBaselineRepository.findByCategory(heightCategory)
//                .orElse(new GlobalBaseline(
//                        heightCategory,
//                        null,
//                        0.0f,
//                        0.0f,
//                        0.0f,
//                        null,
//                        null,
//                        null,
//                        0,
//                        "My Global Ranking Team"
//                ));
//
//        baseline.update(
//                null,
//                0.0f,
//                0.0f,
//                0.0f,
//                null,
//                null,
//                null,
//                0,
//                "My Global Ranking Team"
//        );
//        globalBaselineRepository.save(baseline);
//
//        return new HeightBaselineResult(
//                heightCategory.getCategoryId().toString(),
//                latestYear,
//                summary.sampleSize(),
//                summary.mean(),
//                summary.median(),
//                summary.standardDeviation(),
//                sourceName
//        );
//    }
//
//    private String fetchCsv(String url) {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();
//
//        try {
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            if (response.statusCode() < 200 || response.statusCode() >= 300) {
//                throw new IllegalStateException("OWID request failed with status " + response.statusCode());
//            }
//            return response.body();
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//            throw new IllegalStateException("Failed to fetch OWID height CSV.", ex);
//        } catch (IOException ex) {
//            throw new IllegalStateException("Failed to fetch OWID height CSV.", ex);
//        }
//    }
//
//    private List<HeightObservation> parseHeightObservations(String csv) {
//        String[] lines = csv.split("\\R");
//        if (lines.length <= 1) {
//            return List.of();
//        }
//
//        List<String> header = parseCsvLine(lines[0]);
//        int yearIndex = indexOfIgnoreCase(header, "Year");
//        int valueIndex = resolveValueColumnIndex(header);
//
//        if (yearIndex < 0 || valueIndex < 0) {
//            throw new IllegalStateException("CSV is missing required Year/value columns.");
//        }
//
//        List<HeightObservation> observations = new ArrayList<>();
//        for (int lineIndex = 1; lineIndex < lines.length; lineIndex++) {
//            String line = lines[lineIndex];
//            if (line == null || line.isBlank()) {
//                continue;
//            }
//
//            List<String> fields = parseCsvLine(line);
//            if (fields.size() <= Math.max(yearIndex, valueIndex)) {
//                continue;
//            }
//
//            String yearRaw = fields.get(yearIndex).trim();
//            String valueRaw = fields.get(valueIndex).trim();
//            if (yearRaw.isEmpty() || valueRaw.isEmpty()) {
//                continue;
//            }
//
//            try {
//                int year = Integer.parseInt(yearRaw);
//                double value = Double.parseDouble(valueRaw);
//                if (Double.isFinite(value)) {
//                    observations.add(new HeightObservation(year, value));
//                }
//            } catch (NumberFormatException ignored) {
//                // Ignore non-numeric rows and continue parsing.
//            }
//        }
//
//        return observations;
//    }
//
//    private int resolveValueColumnIndex(List<String> header) {
//        for (int i = 0; i < header.size(); i++) {
//            String normalized = header.get(i).toLowerCase();
//            if (normalized.contains("height")) {
//                return i;
//            }
//        }
//
//        return header.size() - 1;
//    }
//
//    private int indexOfIgnoreCase(List<String> values, String expected) {
//        for (int i = 0; i < values.size(); i++) {
//            if (expected.equalsIgnoreCase(values.get(i).trim())) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    private List<String> parseCsvLine(String line) {
//        String cleaned = line;
//        if (cleaned.startsWith("\uFEFF")) {
//            cleaned = cleaned.substring(1);
//        }
//
//        return Arrays.stream(cleaned.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1))
//                .map(this::stripCsvWrapperQuotes)
//                .toList();
//    }
//
//    private String stripCsvWrapperQuotes(String value) {
//        String trimmed = value.trim();
//        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
//            trimmed = trimmed.substring(1, trimmed.length() - 1);
//        }
//        return trimmed.replace("\"\"", "\"");
//    }
//
//    private record HeightObservation(int year, double value) {
//    }
//
//    public record HeightBaselineResult(
//            String categoryId,
//            int latestYear,
//            int sampleSize,
//            double mean,
//            double median,
//            double standardDeviation,
//            String sourceName
//    ) {
//    }
//}
