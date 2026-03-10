//package com.charble.backend.stats;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Objects;
//import org.springframework.stereotype.Service;
//
///*
//Service responsible for the heavy mathematical lifting.
//It transforms a list of raw scores into a comprehensive statistical report.
// */
//@Service
//public class StatisticsService {
//    private static final int MIN_HISTOGRAM_BUCKETS = 5;
//    private static final int MAX_HISTOGRAM_BUCKETS = 10;
//
//    public StatsSummary calculate(List<Double> rawValues, Double userValue) {
//        Objects.requireNonNull(rawValues, "Values list is required.");
//        if (rawValues.isEmpty()) {
//            throw new IllegalArgumentException("Values list must contain at least one number.");
//        }
//
//        List<Double> sortedValues = rawValues.stream()
//                .peek(this::validateNumber)
//                .sorted(Comparator.naturalOrder())
//                .toList();
//
//        if (userValue != null) {
//            validateNumber(userValue);
//        }
//
//        int sampleSize = sortedValues.size();
//
//        // Basic descriptive statistics
//        double mean = sortedValues.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
//        double median = quantile(sortedValues, 0.50);
//        double q1 = quantile(sortedValues, 0.25);
//        double q3 = quantile(sortedValues, 0.75);
//        double iqr = q3 - q1;
//        double min = sortedValues.getFirst();
//        double max = sortedValues.getLast();
//        double standardDeviation = sampleStandardDeviation(sortedValues, mean);
//
//        // Outlier Detection
//        // Anything much smaller than Q1 or much larger than Q3 is flagged as an outlier.
//        double lowerOutlierFence = q1 - (1.5 * iqr);
//        double upperOutlierFence = q3 + (1.5 * iqr);
//        long outlierCount = sortedValues.stream()
//                .filter(value -> value < lowerOutlierFence || value > upperOutlierFence)
//                .count();
//        Double userPercentile = userValue == null ? null : percentileRank(sortedValues, userValue);
//        Double userZScore = null;
//        if (userValue != null && standardDeviation > 0.0) {
//            userZScore = (userValue - mean) / standardDeviation;
//        }
//
//        return new StatsSummary(
//                sampleSize,
//                mean,
//                median,
//                standardDeviation,
//                min,
//                max,
//                q1,
//                q3,
//                iqr,
//                lowerOutlierFence,
//                upperOutlierFence,
//                outlierCount,
//                userValue,
//                userPercentile,
//                userZScore,
//                buildHistogram(sortedValues, min, max)
//        );
//    }
//
//    /*
//     Esures we don't try to do math on something that is not a number
//     */
//    private void validateNumber(Double value) {
//        if (value == null || value.isNaN() || value.isInfinite()) {
//            throw new IllegalArgumentException("Values must be finite numbers.");
//        }
//    }
//
//    /*
//     Calculates Standard Deviation
//     */
//    private double sampleStandardDeviation(List<Double> sortedValues, double mean) {
//        int sampleSize = sortedValues.size();
//        if (sampleSize < 2) {
//            return 0.0;
//        }
//
//        double squaredErrorSum = sortedValues.stream()
//                .mapToDouble(value -> {
//                    double delta = value - mean;
//                    return delta * delta;
//                })
//                .sum();
//
//        return Math.sqrt(squaredErrorSum / (sampleSize - 1));
//    }
//
//    /*
//     Finds a value at a specific point
//     If the point falls between two numbers, it calculates the value in between.
//     */
//    private double quantile(List<Double> sortedValues, double percentile01) {
//        if (percentile01 < 0.0 || percentile01 > 1.0) {
//            throw new IllegalArgumentException("Percentile must be between 0.0 and 1.0.");
//        }
//
//        int sampleSize = sortedValues.size();
//        if (sampleSize == 1) {
//            return sortedValues.getFirst();
//        }
//
//        double index = percentile01 * (sampleSize - 1);
//        int lowerIndex = (int) Math.floor(index);
//        int upperIndex = (int) Math.ceil(index);
//
//        if (lowerIndex == upperIndex) {
//            return sortedValues.get(lowerIndex);
//        }
//
//        double lower = sortedValues.get(lowerIndex);
//        double upper = sortedValues.get(upperIndex);
//        double fraction = index - lowerIndex;
//        return lower + (fraction * (upper - lower));
//    }
//
//    /*
//     Determines where a specific value sits compared to the rest of the group.
//     */
//    private double percentileRank(List<Double> sortedValues, double userValue) {
//        int below = 0;
//        int equal = 0;
//
//        for (double current : sortedValues) {
//            if (current < userValue) {
//                below++;
//            } else if (Double.compare(current, userValue) == 0) {
//                equal++;
//            }
//        }
//
//        double rank = (below + 0.5 * equal) / sortedValues.size();
//        return rank * 100.0;
//    }
//
//    /*
//     Groups scores into buckets.
//     */
//    private List<StatsSummary.HistogramBucket> buildHistogram(List<Double> sortedValues, double min, double max) {
//        int sampleSize = sortedValues.size();
//        // If everyone has the exact same score, we only need one bucket.
//        if (Double.compare(min, max) == 0) {
//            return List.of(new StatsSummary.HistogramBucket(min, max, sampleSize));
//        }
//
//        // Decide how many bars to show. We use the Square Root Choice rule (sqrt(n)), which is a standard
//        // statistical way to choose bucket counts.
//        int bucketCount = Math.min(MAX_HISTOGRAM_BUCKETS, Math.max(MIN_HISTOGRAM_BUCKETS,
//                                    (int) Math.ceil(Math.sqrt(sampleSize))));
//        double width = (max - min) / bucketCount;
//        int[] counts = new int[bucketCount];
//
//        for (double value : sortedValues) {
//            int index = (int) ((value - min) / width);
//            // Handle the edge case where the maximum value would overflow the last bucket index.
//            if (index >= bucketCount) {
//                index = bucketCount - 1;
//            }
//            counts[index]++;
//        }
//
//        List<StatsSummary.HistogramBucket> buckets = new ArrayList<>(bucketCount);
//        for (int i = 0; i < bucketCount; i++) {
//            double start = min + (i * width);
//            double end = (i == bucketCount - 1) ? max : min + ((i + 1) * width);
//            buckets.add(new StatsSummary.HistogramBucket(start, end, counts[i]));
//        }
//
//        return buckets;
//    }
//}
