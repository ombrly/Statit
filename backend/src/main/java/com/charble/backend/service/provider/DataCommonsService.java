package com.charble.backend.service.provider;

import com.charble.backend.model.Category;
import com.charble.backend.model.GlobalBaseline;
import com.charble.backend.model.User;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.GlobalBaselineRepository;
import com.charble.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class DataCommonsService
{
    private final GlobalBaselineRepository baselineRepo;
    private final CategoryRepository categoryRepo;
    private final UserRepository userRepo;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${google.datacommons.api-key}")
    private String apiKey;

    public DataCommonsService(GlobalBaselineRepository baselineRepo, CategoryRepository categoryRepo, UserRepository userRepo)
    {
        this.baselineRepo = baselineRepo;
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
    }

    private record CategoryConfig(String name, String units, Boolean sortOrder) {}
    private final Map<String, CategoryConfig> categoryConfigs = Map.of(
        "LifeExpectancy_Person", new CategoryConfig("Life Expectancy", "Years", true),
        "Median_Age_Person", new CategoryConfig("Median Age", "Years", true)
    );

    public void fetchAndSaveGlobalBaselines()
    {
        try
        {
            List<String> countryDcids = List.of(
                    "country/USA", "country/GBR", "country/JPN", "country/FRA",
                    "country/BRA", "country/IND", "country/ZAF", "country/AUS"
            );

            List<String> variableDcids = List.of(
                    "LifeExpectancy_Person",
                    "Median_Age_Person"
            );

            String requestBody = buildRequestBody(countryDcids, variableDcids);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.datacommons.org/v2/observation?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .header("X-API-KEY", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP Status: " + response.statusCode());

            Map<String, List<Float>> groupedData = parseDataCommonsResponse(response.body());

            User systemUser = userRepo.findByUsername("systeadmin")
                    .orElseGet(() -> userRepo.save(new User("systeadmin", "admin@charble.com", "placeholder_hash")));

            for(Map.Entry<String, List<Float>> entry : groupedData.entrySet())
            {
                String variableDcid = entry.getKey();
                List<Float> values = entry.getValue();

                if(values.isEmpty() || !categoryConfigs.containsKey(variableDcid)) continue;

                Float mean = calculateMean(values);
                Float median = calculateMedian(values);
                Float stdDev = calculateStandardDeviation(values, mean);
                Integer sampleSize = values.size();

                CategoryConfig config = categoryConfigs.get(variableDcid);

                Category category = categoryRepo.findByName(config.name())
                        .orElseGet(() -> categoryRepo.save(new Category(config.name, config.units, config.sortOrder, systemUser)));

                GlobalBaseline baseline = baselineRepo.findByCategory(category)
                        .orElse(new GlobalBaseline(category, mean, median, stdDev, sampleSize, "Data Commons API V2"));


                baseline.updateStatistics(mean, median, stdDev, sampleSize, "Data Commons API V2");
                baselineRepo.save(baseline);

                System.out.println("Succesfully updated baseline for: " + config.name());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    private String buildRequestBody(List<String> entities, List<String> variables) throws Exception
    {
        Map<String, Object> body = new HashMap<>();
        body.put("date", "LATEST");
        body.put("select", List.of("date", "entity", "variable", "value"));
        body.put("entity", Map.of("dcids", entities));
        body.put("variable", Map.of("dcids", variables));
        return mapper.writeValueAsString(body);
    }

    private Map<String, List<Float>> parseDataCommonsResponse(String jsonResponse) throws Exception
    {
        // 1. Print the raw response so we can see if the API Key worked!
        System.out.println("API Response: " + jsonResponse);

        Map<String, List<Float>> dataMap = new HashMap<>();
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode byVariable = root.path("byVariable");

        // If the API failed or returned empty data, stop here.
        if (byVariable.isMissingNode()) {
            System.err.println("Error: 'byVariable' node not found. Check the API response above!");
            return dataMap;
        }

        // 2. Loop through each variable (e.g., "LifeExpectancy_Person")
        Iterator<Map.Entry<String, JsonNode>> variables = byVariable.fields();
        while (variables.hasNext()) {
            Map.Entry<String, JsonNode> varEntry = variables.next();
            String variableName = varEntry.getKey();
            JsonNode byEntity = varEntry.getValue().path("byEntity");

            // 3. Loop through each country inside that variable
            Iterator<Map.Entry<String, JsonNode>> entities = byEntity.fields();
            while (entities.hasNext()) {
                Map.Entry<String, JsonNode> entEntry = entities.next();
                JsonNode orderedFacets = entEntry.getValue().path("orderedFacets");

                // 4. Drill down to the actual data point and save it
                if (orderedFacets.isArray() && !orderedFacets.isEmpty()) {
                    JsonNode observations = orderedFacets.get(0).path("observations");
                    if (observations.isArray() && !observations.isEmpty()) {
                        Float value = (float) observations.get(0).path("value").asDouble();

                        dataMap.putIfAbsent(variableName, new ArrayList<>());
                        dataMap.get(variableName).add(value);
                    }
                }
            }
        }
        return dataMap;
    }

    private Float calculateMean(List<Float> values)
    {
        float sum = 0;
        for(Float v : values) sum += v;
        return sum / values.size();
    }

    private Float calculateMedian(List<Float> values) {
        List<Float> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;

        if (sorted.size() % 2 == 1) return sorted.get(middle);
        else return ((sorted.get(middle - 1) + sorted.get(middle)) / 2.0f);
    }

    private Float calculateStandardDeviation(List<Float> values, Float mean)
    {
        float sumSquaredDiffs = 0;
        for(Float v : values) sumSquaredDiffs += (float)Math.pow(v - mean, 2);
        return ((float)Math.sqrt(sumSquaredDiffs / values.size()));
    }
}


