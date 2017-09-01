package com.mindera.metrics.statful;

import com.statful.sdk.domain.configuration.MetricsCollectionConfiguration;

interface AppScatterMetricsConfiguration extends MetricsCollectionConfiguration {

    boolean shouldCollectCustomMetrics();
}
