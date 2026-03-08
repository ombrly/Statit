package com.charble.backend.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/*
Service responsible for the heavy mathematical lifting.
It transforms a list of raw scores into a comprehensive statistical report.
 */
@Service
public class StatisticsService {
    private static final int MIN_HISTOGRAM_BUCKETS = 5;
    private static final int MAX_HISTOGRAM_BUCKETS = 10;

    /*
     This remains a full-data recompute path. It is still needed for exact quantiles/median/IQR/histogram,
     which are order-dependent statistics.
     */
    public StatsSummary calculate(List<Double> rawValues, Double userValue) {
        Objects.requireNonNull(rawValues, "Values list is required.");
        if (rawValues.isEmpty()) {
            throw new IllegalArgumentException("Values list must contain at least one number.");
        }

        List<Double> sortedValues = rawValues.stream()
                .peek(this::validateNumber)
                .sorted(Comparator.naturalOrder())
                .toList();

        if (userValue != null) {
            validateNumber(userValue);
        }

        int sampleSize = sortedValues.size();

        /*
         Mean/std-dev now come from a single running-moments pass (Welford),
         which is numerically stable and is the same primitive used for O(1) per-submission updates below.
         */
        RunningMoments moments = accumulateRunningMoments(sortedValues);
        double mean = moments.mean();
        double standardDeviation = moments.sampleStandardDeviation();

        // Quantiles still require sorted full data (not O(1) with this simple service).
        double median = quantile(sortedValues, 0.50); 
        double q1 = quantile(sortedValues, 0.25);     
        double q3 = quantile(sortedValues, 0.75);     
        double iqr = q3 - q1;                         
        double min = sortedValues.getFirst();
        double max = sortedValues.getLast();

        // Outlier Detection 
        // Anything much smaller than Q1 or much larger than Q3 is flagged as an outlier.
        double lowerOutlierFence = q1 - (1.5 * iqr);
        double upperOutlierFence = q3 + (1.5 * iqr);
        long outlierCount = sortedValues.stream()
                .filter(value -> value < lowerOutlierFence || value > upperOutlierFence)
                .count();
        Double userPercentile = userValue == null ? null : percentileRank(sortedValues, userValue);
        Double userZScore = null;
        if (userValue != null && standardDeviation > 0.0) {
            userZScore = (userValue - mean) / standardDeviation;
        }

        return new StatsSummary(
                sampleSize,
                mean,
                median,
                standardDeviation,
                min,
                max,
                q1,
                q3,
                iqr,
                lowerOutlierFence,
                upperOutlierFence,
                outlierCount,
                userValue,
                userPercentile,
                userZScore,
                buildHistogram(sortedValues, min, max) 
        );
    }

    /*
     O(1) update for mean and standard deviation when one new score arrives.
     Use this in your score-submission path to avoid rescanning all historical scores every write.
     */
    public RunningMoments updateRunningMoments(RunningMoments current, double newValue) {
        validateNumber(newValue);
        RunningMoments safeCurrent = current == null ? new RunningMoments(0, 0.0, 0.0) : current;

        long nextSampleSize = safeCurrent.sampleSize() + 1;
        double delta = newValue - safeCurrent.mean();
        double nextMean = safeCurrent.mean() + (delta / nextSampleSize);
        double delta2 = newValue - nextMean;
        double nextM2 = safeCurrent.m2() + (delta * delta2);

        return new RunningMoments(nextSampleSize, nextMean, nextM2);
    }

    /*
     Gaussian fallback: if you only have baseline mean/std-dev (and not full distribution data),
     this estimates percentile from z-score using a normal CDF.
     */
    public GaussianFallbackResult gaussianFallback(double userValue, double mean, double standardDeviation) {
        validateNumber(userValue);
        validateNumber(mean);
        validateNumber(standardDeviation);
        if (standardDeviation < 0.0) {
            throw new IllegalArgumentException("Standard deviation cannot be negative.");
        }

        if (standardDeviation == 0.0) {
            double percentile = (Double.compare(userValue, mean) < 0) ? 0.0
                    : (Double.compare(userValue, mean) > 0) ? 100.0 : 50.0;
            return new GaussianFallbackResult(userValue, mean, standardDeviation, null, percentile);
        }

        double zScore = (userValue - mean) / standardDeviation;
        double percentile = normalCdf(zScore) * 100.0;
        return new GaussianFallbackResult(userValue, mean, standardDeviation, zScore, percentile);
    }

    /*
     Esures we don't try to do math on something that is not a number
     */
    private void validateNumber(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            throw new IllegalArgumentException("Values must be finite numbers.");
        }
    }

    private RunningMoments accumulateRunningMoments(List<Double> values) {
        RunningMoments current = new RunningMoments(0, 0.0, 0.0);
        for (double value : values) {
            current = updateRunningMoments(current, value);
        }
        return current;
    }

    /*
     Finds a value at a specific point
     If the point falls between two numbers, it calculates the value in between.
     */
    private double quantile(List<Double> sortedValues, double percentile01) {
        if (percentile01 < 0.0 || percentile01 > 1.0) {
            throw new IllegalArgumentException("Percentile must be between 0.0 and 1.0.");
        }

        int sampleSize = sortedValues.size();
        if (sampleSize == 1) {
            return sortedValues.getFirst();
        }

        double index = percentile01 * (sampleSize - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }

        double lower = sortedValues.get(lowerIndex);
        double upper = sortedValues.get(upperIndex);
        double fraction = index - lowerIndex;
        return lower + (fraction * (upper - lower));
    }

    /*
     Determines where a specific value sits compared to the rest of the group.
     */
    private double percentileRank(List<Double> sortedValues, double userValue) {
        int below = 0;
        int equal = 0;

        for (double current : sortedValues) {
            if (current < userValue) {
                below++;
            } else if (Double.compare(current, userValue) == 0) {
                equal++;
            }
        }

        double rank = (below + 0.5 * equal) / sortedValues.size();
        return rank * 100.0;
    }

    /*
     Groups scores into buckets. 
     */
    private List<StatsSummary.HistogramBucket> buildHistogram(List<Double> sortedValues, double min, double max) {
        int sampleSize = sortedValues.size();
        // If everyone has the exact same score, we only need one bucket.
        if (Double.compare(min, max) == 0) {
            return List.of(new StatsSummary.HistogramBucket(min, max, sampleSize));
        }

        // Decide how many bars to show. We use the Square Root Choice rule (sqrt(n)), which is a standard 
        // statistical way to choose bucket counts.
        int bucketCount = Math.min(MAX_HISTOGRAM_BUCKETS, Math.max(MIN_HISTOGRAM_BUCKETS, 
                                    (int) Math.ceil(Math.sqrt(sampleSize))));
        double width = (max - min) / bucketCount;
        int[] counts = new int[bucketCount];

        for (double value : sortedValues) {
            int index = (int) ((value - min) / width);
            // Handle the edge case where the maximum value would overflow the last bucket index.
            if (index >= bucketCount) {
                index = bucketCount - 1;
            }
            counts[index]++;
        }

        List<StatsSummary.HistogramBucket> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            double start = min + (i * width);
            double end = (i == bucketCount - 1) ? max : min + ((i + 1) * width);
            buckets.add(new StatsSummary.HistogramBucket(start, end, counts[i]));
        }

        return buckets;
    }

    private double normalCdf(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    // Abramowitz and Stegun approximation for the error function.
    private double erf(double value) {
        double sign = value < 0 ? -1.0 : 1.0;
        double x = Math.abs(value);

        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
        return sign * y;
    }

    /*
     Running moments for incremental updates:
     - sampleSize: n
     - mean: running mean
     - m2: running sum of squared deviations from the mean
     */
    public record RunningMoments(long sampleSize, double mean, double m2) {
        public double sampleVariance() {
            return sampleSize < 2 ? 0.0 : m2 / (sampleSize - 1);
        }

        public double sampleStandardDeviation() {
            return Math.sqrt(sampleVariance());
        }
    }

    public record GaussianFallbackResult(
            double userValue,
            double mean,
            double standardDeviation,
            Double zScore,
            double percentile
    ) {
    }
}
