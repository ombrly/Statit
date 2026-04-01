//package com.charble.backend.stats;
//
//import java.util.List;
//
//public record StatsSummary(
//        int sampleSize,
//        double mean,
//        double median,
//        double standardDeviation,
//        double min,
//        double max,
//        double percentile25,
//        double percentile75,
//        double iqr,
//        double lowerOutlierFence,
//        double upperOutlierFence,
//        long outlierCount,
//        Double userValue,
//        Double userPercentile,
//        Double userZScore,
//        List<HistogramBucket> histogram
//) {
//    public record HistogramBucket(
//            double startInclusive,
//            double endInclusive,
//            int count
//    ) {
//    }
//}
