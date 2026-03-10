///**
// * Filename: BaselineManagementServiceUnitTest.java
// * Author: Wilson Jimenez
// * Description: Manual API-backed unit test helpers for BaselineManagementService
// */
//
////----------------------------------------------------------------------------------------------------
//// Package
////----------------------------------------------------------------------------------------------------
//package com.statit.backend.unit_tests;
//
////----------------------------------------------------------------------------------------------------
//// Imports
////----------------------------------------------------------------------------------------------------
//import com.statit.backend.model.Category;
//import com.statit.backend.model.GlobalBaseline;
//import com.statit.backend.model.User;
//import com.statit.backend.model.enums.Region;
//import com.statit.backend.model.enums.Sex;
//import com.statit.backend.repository.CategoryRepository;
//import com.statit.backend.repository.UserRepository;
//import com.statit.backend.service.GlobalBaselineService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
////----------------------------------------------------------------------------------------------------
//// Class Definition
////----------------------------------------------------------------------------------------------------
//@RestController
//@RequestMapping("api/unit_tests/baseline")
//public class BaselineManagementServiceUnitTest
//{
//    //------------------------------------------------------------------------------------------------
//    // Constructors
//    //------------------------------------------------------------------------------------------------
//    public BaselineManagementServiceUnitTest(GlobalBaselineService baselineManagementService,
//                                             UserRepository userRepository,
//                                             CategoryRepository categoryRepository)
//    {
//        this.baselineManagementService = baselineManagementService;
//        this.userRepository = userRepository;
//        this.categoryRepository = categoryRepository;
//    }
//
//    //------------------------------------------------------------------------------------------------
//    // Public Methods
//    //------------------------------------------------------------------------------------------------
//    @PostMapping("/generate-baseline")
//    public ResponseEntity<Map<String, Object>> generateBaseline()
//    {
//        User foundingUser = userRepository.findByUsername(TEST_BASELINE_USER)
//                .orElseGet(() -> {
//                    Map<String, String> demographics = new HashMap<>();
//                    demographics.put("region", Region.NORTH_AMERICA.name());
//                    demographics.put("sex", Sex.MALE.name());
//
//                    User newUser = new User(
//                            TEST_BASELINE_USER,
//                            TEST_BASELINE_EMAIL,
//                            "baseline-password",
//                            LocalDate.of(2000, 1, 1),
//                            demographics
//                    );
//
//                    return userRepository.save(newUser);
//                });
//
//        Category category = categoryRepository.findByCategoryName(TEST_BASELINE_CATEGORY)
//                .orElseGet(() -> {
//                    Category newCategory = new Category(
//                            TEST_BASELINE_CATEGORY,
//                            "Category used for BaselineManagementService unit tests",
//                            "units",
//                            List.of("north_america", "male"),
//                            true,
//                            foundingUser
//                    );
//
//                    return categoryRepository.save(newCategory);
//                });
//
//        GlobalBaseline baseline;
//        try
//        {
//            baseline = baselineManagementService.createBaseline(
//                    category.getCategoryId(),
//                    TEST_REGION,
//                    TEST_SEX,
//                    TEST_AGE_MIN,
//                    TEST_AGE_MAX,
//                    50.0f,
//                    49.0f,
//                    5.0f,
//                    null,
//                    null,
//                    null,
//                    100,
//                    "Unit Test Baseline Source"
//            );
//        }
//        catch(IllegalArgumentException e)
//        {
//            baseline = baselineManagementService.getBaselineByKey(
//                    category.getCategoryId(),
//                    TEST_REGION,
//                    TEST_SEX,
//                    TEST_AGE_MIN,
//                    TEST_AGE_MAX
//            );
//        }
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("message", "Baseline test data is ready.");
//        response.put("categoryId", category.getCategoryId());
//        response.put("baseline", toBaselineSummary(baseline));
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/by-category")
//    public ResponseEntity<Map<String, Object>> getByCategory(@RequestParam UUID categoryId)
//    {
//        List<GlobalBaseline> baselines = baselineManagementService.getBaselinesByCategory(categoryId);
//
//        List<Map<String, Object>> summaries = new ArrayList<>();
//        for(GlobalBaseline baseline : baselines)
//        {
//            summaries.add(toBaselineSummary(baseline));
//        }
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("categoryId", categoryId);
//        response.put("count", summaries.size());
//        response.put("baselines", summaries);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/by-key")
//    public ResponseEntity<Map<String, Object>> getByKey(
//            @RequestParam UUID categoryId,
//            @RequestParam(defaultValue = "NORTH_AMERICA") Region region,
//            @RequestParam(defaultValue = "MALE") Sex sex,
//            @RequestParam(defaultValue = "18") Integer ageMinYears,
//            @RequestParam(defaultValue = "25") Integer ageMaxYears)
//    {
//        GlobalBaseline baseline = baselineManagementService.getBaselineByKey(
//                categoryId,
//                region,
//                sex,
//                ageMinYears,
//                ageMaxYears
//        );
//
//        return ResponseEntity.ok(toBaselineSummary(baseline));
//    }
//
//    //------------------------------------------------------------------------------------------------
//    // Private Methods
//    //------------------------------------------------------------------------------------------------
//    private Map<String, Object> toBaselineSummary(GlobalBaseline baseline)
//    {
//        Map<String, Object> summary = new LinkedHashMap<>();
//        summary.put("baselineId", baseline.getBaselineId());
//        summary.put("filters", baseline.getFilters());
//        summary.put("mean", baseline.getMean());
//        summary.put("median", baseline.getMedian());
//        summary.put("standardDeviation", baseline.getStandardDeviation());
//        summary.put("sampleSize", baseline.getSampleSize());
//        summary.put("sourceName", baseline.getSourceName());
//        return summary;
//    }
//
//    //------------------------------------------------------------------------------------------------
//    // Private Variables
//    //------------------------------------------------------------------------------------------------
//    private final GlobalBaselineService baselineManagementService;
//    private final UserRepository userRepository;
//    private final CategoryRepository categoryRepository;
//
//    //------------------------------------------------------------------------------------------------
//    // Private Constants
//    //------------------------------------------------------------------------------------------------
//    private static final String TEST_BASELINE_USER = "BaselineTestFounder";
//    private static final String TEST_BASELINE_EMAIL = "baseline_unit_test@myglobalranking.com";
//    private static final String TEST_BASELINE_CATEGORY = "Baseline Unit Test Category";
//    private static final Region TEST_REGION = Region.NORTH_AMERICA;
//    private static final Sex TEST_SEX = Sex.MALE;
//    private static final Integer TEST_AGE_MIN = 18;
//    private static final Integer TEST_AGE_MAX = 25;
//}
