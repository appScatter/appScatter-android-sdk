package com.mindera.metrics.statful;

public interface RemoteMetricsConfigurationRepository {

    Boolean shouldCollectDataUsageMetrics();

    Boolean shouldCollectMemoryAllocationMetrics();

    Boolean shouldCollectStorageUsageMetrics();

    Boolean shouldCollectLaunchTimeMetrics();

    Boolean shouldCollectCrashesMetrics();

    Boolean shouldCollectForegroundBackgroundTimesMetrics();

    Boolean shouldCollectLayoutTimesMetrics();

    Boolean shouldCollectCustomMetrics();

    long getDataUsageMetricsCollectionIntervalMillis();

    long getMemoryAllocationCollectionIntervalMillis();

    long getStorageUsageCollectionIntervalMillis();
}
