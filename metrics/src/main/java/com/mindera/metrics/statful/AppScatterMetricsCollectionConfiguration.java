package com.mindera.metrics.statful;

import android.content.Context;

public class AppScatterMetricsCollectionConfiguration implements AppScatterMetricsConfiguration {

    private boolean storageUsage = true;
    private boolean memoryAllocation = true;
    private boolean dataUsage = true;
    private boolean crashCount = false;
    private boolean backgroundForegroundTimes = true;
    private boolean launchTimes = true;
    private boolean layoutTimes = true;
    private boolean custom = true;
    private long storageUsageCollectionIntervalMillis = 30000;
    private long memoryAllocationCollectionIntervalMillis = 30000;
    private long dataUsageCollectionIntervalMillis = 30000;

    private RemoteMetricsConfigurationSharedPreferencesRepository remoteMetricsCollectionConfiguration;

    public AppScatterMetricsCollectionConfiguration(final Context context) {
        this.remoteMetricsCollectionConfiguration = new RemoteMetricsConfigurationSharedPreferencesRepository(context);
        resolveCustom();
    }

    public void updateConfiguration(String jsonConfiguration) {
        this.remoteMetricsCollectionConfiguration.updateConfiguration(jsonConfiguration);
        resolveStorageUsage(this.storageUsage);
        resolveMemoryAllocation(this.memoryAllocation);
        resolveDataUsage(this.dataUsage);
        resolveCrashCount(this.crashCount);
        resolveBackgroundForegroundTimes(this.backgroundForegroundTimes);
        resolveLaunchTimes(this.layoutTimes);
        resolveLayoutTimes(this.layoutTimes);
        resolveCustom();
    }

    public void setStorageUsageCollectionIntervalMillis(long storageUsageCollectionIntervalMillis) {
        this.storageUsageCollectionIntervalMillis = storageUsageCollectionIntervalMillis;
    }

    public void setMemoryAllocationCollectionIntervalMillis(long memoryAllocationCollectionIntervalMillis) {
        this.memoryAllocationCollectionIntervalMillis = memoryAllocationCollectionIntervalMillis;
    }

    public void setDataUsageCollectionIntervalMillis(long dataUsageCollectionIntervalMillis) {
        this.dataUsageCollectionIntervalMillis = dataUsageCollectionIntervalMillis;
    }

    public void resolveStorageUsage(boolean preferredStorageUsage) {
        this.storageUsage = getCombinedConfiguration(preferredStorageUsage, remoteMetricsCollectionConfiguration.shouldCollectStorageUsageMetrics());
    }

    public void resolveMemoryAllocation(boolean preferredMemoryAllocation) {
        this.memoryAllocation = getCombinedConfiguration(preferredMemoryAllocation, remoteMetricsCollectionConfiguration.shouldCollectMemoryAllocationMetrics());
    }

    public void resolveDataUsage(boolean preferredDataUsage) {
        this.dataUsage = getCombinedConfiguration(preferredDataUsage, remoteMetricsCollectionConfiguration.shouldCollectDataUsageMetrics());
    }

    public void resolveCrashCount(boolean preferredCrashCount) {
        this.crashCount = getCombinedConfiguration(preferredCrashCount, remoteMetricsCollectionConfiguration.shouldCollectCrashesMetrics());
    }

    public void resolveBackgroundForegroundTimes(boolean preferredBackgroundForegroundTimes) {
        this.backgroundForegroundTimes = getCombinedConfiguration(preferredBackgroundForegroundTimes, remoteMetricsCollectionConfiguration.shouldCollectForegroundBackgroundTimesMetrics());
    }

    public void resolveLaunchTimes(boolean preferredLaunchTimes) {
        this.launchTimes = getCombinedConfiguration(preferredLaunchTimes, remoteMetricsCollectionConfiguration.shouldCollectLaunchTimeMetrics());
    }

    public void resolveLayoutTimes(boolean preferredLayoutTimes) {
        this.layoutTimes = getCombinedConfiguration(preferredLayoutTimes, remoteMetricsCollectionConfiguration.shouldCollectLayoutTimesMetrics());
    }

    private void resolveCustom() {
        this.custom = getCombinedConfiguration(true, remoteMetricsCollectionConfiguration.shouldCollectCustomMetrics());
    }

    @Override
    public boolean shouldCollectDataUsageMetrics() {
        return this.dataUsage;
    }

    @Override
    public boolean shouldCollectMemoryAllocationMetrics() {
        return this.memoryAllocation;
    }

    @Override
    public boolean shouldCollectStorageUsageMetrics() {
        return this.storageUsage;
    }

    @Override
    public boolean shouldCollectLaunchTimeMetrics() {
        return this.launchTimes;
    }

    @Override
    public boolean shouldCollectCrashesMetrics() {
        return this.crashCount;
    }

    @Override
    public boolean shouldCollectForegroundBackgroundTimesMetrics() {
        return this.backgroundForegroundTimes;
    }

    @Override
    public boolean shouldCollectLayoutTimesMetrics() {
        return this.layoutTimes;
    }

    @Override
    public long getDataUsageMetricsCollectionIntervalMillis() {
        return this.dataUsageCollectionIntervalMillis;
    }

    @Override
    public long getMemoryAllocationCollectionIntervalMillis() {
        return this.memoryAllocationCollectionIntervalMillis;
    }

    @Override
    public long getStorageUsageCollectionIntervalMillis() {
        return this.storageUsageCollectionIntervalMillis;
    }

    @Override
    public boolean shouldCollectCustomMetrics() {
        return this.custom;
    }

    private boolean getCombinedConfiguration(final boolean userConfiguration, final Boolean remoteConfiguration) {
        if (remoteConfiguration == null) {
            return userConfiguration;
        }

        return userConfiguration && remoteConfiguration;
    }
}


