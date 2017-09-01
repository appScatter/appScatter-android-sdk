package com.mindera.metrics.statful;

import com.mindera.metrics.BuildConfig;
import com.statful.client.core.api.AggregationBuilder;
import com.statful.client.core.api.AggregationFreqBuilder;
import com.statful.client.core.api.TagBuilder;
import com.statful.sdk.core.StatfulSDKFactory;
import com.statful.sdk.core.api.configuration.SDKConfigurationBuilder;
import com.statful.sdk.domain.StatfulSDK;

import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class AppScatterBuilder {

    private static NoOpStatfulSDK noOpStatfulSDK = new NoOpStatfulSDK();
    private static AppScatterMetricsCollectionConfiguration metricsCollectionConfiguration;
    private static StatfulSDK statfulSDK;
    private static Application targetApplication;
    private static long enqueueRequestId;
    private String host;
    private String token;
    private static String userIdHash;

    private static SDKConfigurationBuilder<StatfulSDK> statfulSDKConfigurationBuilder;

    private AppScatterBuilder() {
        metricsCollectionConfiguration = new AppScatterMetricsCollectionConfiguration(targetApplication);
    }

    /**
     * Initializes ASIab with additional Statful configuration options
     *
     * @param appScatterId The {@link String} that provides appScatterId
     * @param application  The {@link Application} that uses the entire sdk
     * @return A reference to this configuration initializer
     */
    public static AppScatterBuilder with(@NonNull final String appScatterId, @NonNull final Application application) {
        targetApplication = application;
        statfulSDKConfigurationBuilder = StatfulSDKFactory.with(application);
        userIdHash = Utils.generateMD5(appScatterId);
        return new AppScatterBuilder();
    }

    /**
     * Allows to enable/disable data usage metrics collection
     *
     * @param host a host to sent metrics to
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder host(final String host) {
        this.host = host;
        return this;
    }

    /**
     * Allows to enable/disable data usage metrics collection
     *
     * @param token to authenticate with Statful host
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder token(final String token) {
        this.token = token;
        return this;
    }

    /**
     * Allows to enable/disable data usage metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder dataUsageMetricsCollection(final boolean enable) {
        metricsCollectionConfiguration.resolveDataUsage(enable);
        return this;
    }

    /**
     * Allows to enable/disable data usage metrics collection and to set a custom interval
     * for metrics collection
     *
     * @param enable         if the metric collection should be enabled
     * @param intervalMillis interval for metrics collection (ms)
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder dataUsageMetricsCollection(final boolean enable, final long intervalMillis) {
        metricsCollectionConfiguration.resolveDataUsage(enable);
        metricsCollectionConfiguration.setDataUsageCollectionIntervalMillis(intervalMillis);
        return this;
    }

    /**
     * Allows to enable/disable memory allocation metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder memoryAllocationMetricsCollection(final boolean enable) {
        metricsCollectionConfiguration.resolveMemoryAllocation(enable);
        return this;
    }

    /**
     * Allows to enable/disable memory allocation metrics collection
     *
     * @param enable         if the metric collection should be enabled
     * @param intervalMillis interval for metrics collection (ms)
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder memoryAllocationMetricsCollection(final boolean enable, final long intervalMillis) {
        metricsCollectionConfiguration.resolveMemoryAllocation(enable);
        metricsCollectionConfiguration.setMemoryAllocationCollectionIntervalMillis(intervalMillis);
        return this;
    }

    /**
     * Allows to enable/disable storage usage metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder storageUsageMetricsCollection(boolean enable) {
        metricsCollectionConfiguration.resolveStorageUsage(enable);
        return this;
    }

    /**
     * Allows to enable/disable storage usage metrics collection
     *
     * @param enable         if the metric collection should be enabled
     * @param intervalMillis interval for metrics collection (ms)
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder storageUsageMetricsCollection(boolean enable, long intervalMillis) {
        metricsCollectionConfiguration.resolveStorageUsage(enable);
        metricsCollectionConfiguration.setStorageUsageCollectionIntervalMillis(intervalMillis);
        return this;
    }

    /**
     * Allows to enable/disable launch time metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder launchTimeMetricsCollection(final boolean enable) {
        metricsCollectionConfiguration.resolveLaunchTimes(enable);
        return this;
    }

    /**
     * Allows to enable/disable crash count metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder crashesMetricsCollection(final boolean enable) {
        metricsCollectionConfiguration.resolveCrashCount(enable);
        return this;
    }

    /**
     * Allows to enable/disable foreground & background times metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder foregroundBackgroundTimesMetricsCollection(final boolean enable) {
        metricsCollectionConfiguration.resolveBackgroundForegroundTimes(enable);
        return this;
    }

    /**
     * Allows to enable/disable layout times metrics collection
     *
     * @param enable if the metric collection should be enabled
     * @return A reference to this configuration initializer
     */
    public AppScatterBuilder layoutTimesMetricsCollection(final boolean enable) {
        metricsCollectionConfiguration.resolveLayoutTimes(enable);
        return this;
    }


    /**
     * Sets if metrics is running in dry mode. If true, metrics will be logged
     * to console and not sent to server
     *
     * @param isDryRun if should be a dry run
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder isDryRun(final boolean isDryRun) {
        statfulSDKConfigurationBuilder.isDryRun(isDryRun);

        return this;
    }

    /**
     * Sets the global tags to use
     *
     * @param type  The tag type
     * @param value The tag value
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder tag(final String type, final String value) {
        statfulSDKConfigurationBuilder.tag(type, value);

        return this;
    }


    /**
     * Sets the timer method default tags
     * <p>
     * Example: <code>timer(tag("host", "localhost"), tag("cluster", production)).</code>
     *
     * @param tagBuilders An array of {@link TagBuilder} to use, which can be imported statically
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder timer(final TagBuilder... tagBuilders) {
        statfulSDKConfigurationBuilder.timer(tagBuilders);

        return this;
    }

    /**
     * Sets the timer method default aggregations
     * <p>
     * Example: <code>timer(agg(AVG), agg(LAST))</code>
     *
     * @param aggregationBuilders An array of {@link AggregationBuilder} to use, which can be imported statically
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder timer(final AggregationBuilder... aggregationBuilders) {
        statfulSDKConfigurationBuilder.timer(aggregationBuilders);

        return this;
    }

    /**
     * Sets the timer method default aggregation frequency
     * <p>
     * Example: <code>timer(aggregationFrequency(FREQ_120))</code>
     *
     * @param aggregationFreqBuilder An {@link AggregationFreqBuilder}
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder timer(final AggregationFreqBuilder aggregationFreqBuilder) {
        statfulSDKConfigurationBuilder.timer(aggregationFreqBuilder);

        return this;
    }

    /**
     * Sets the counter method default tags
     * <p>
     * Example: <code>counter(tag("host", "localhost"), tag("cluster", production))</code>
     *
     * @param tagBuilders An array of {@link TagBuilder} to use, which can be imported statically
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder counter(final TagBuilder... tagBuilders) {
        statfulSDKConfigurationBuilder.counter(tagBuilders);

        return this;
    }

    /**
     * Sets the counter method default aggregations
     * <p>
     * Example: <code>counter(agg(AVG), agg(LAST))</code>
     *
     * @param aggregationBuilders An array of {@link AggregationBuilder} to use, which can be imported statically
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder counter(final AggregationBuilder... aggregationBuilders) {
        statfulSDKConfigurationBuilder.counter(aggregationBuilders);

        return this;
    }

    /**
     * Sets the counter method default aggregation frequency
     * <p>
     * Example: <code>counter(aggregationFrequency(FREQ_120))</code>
     *
     * @param aggregationFreqBuilder An {@link AggregationFreqBuilder}
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder counter(final AggregationFreqBuilder aggregationFreqBuilder) {
        statfulSDKConfigurationBuilder.counter(aggregationFreqBuilder);

        return this;
    }

    /**
     * Sets the gauge method default tags
     * <p>
     * Example: <code>gauge(tag("host", "localhost"), tag("cluster", production))</code>
     *
     * @param tagBuilders An array of {@link TagBuilder} to use, which can be imported statically
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder gauge(final TagBuilder... tagBuilders) {
        statfulSDKConfigurationBuilder.gauge(tagBuilders);

        return this;
    }

    /**
     * Sets the gauge method default aggregations
     * <p>
     * Example: <code>gauge(agg(AVG), agg(LAST))</code>
     *
     * @param aggregationBuilders An array of {@link AggregationBuilder} to use, which can be imported statically
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder gauge(final AggregationBuilder... aggregationBuilders) {
        statfulSDKConfigurationBuilder.gauge(aggregationBuilders);

        return this;
    }

    /**
     * Sets the gauge method default aggregation frequency. Example: gauge(aggregationFrequency(FREQ_120))
     *
     * @param aggregationFreqBuilder An {@link AggregationFreqBuilder}
     * @return A reference to this configuration builder
     */
    public AppScatterBuilder gauge(final AggregationFreqBuilder aggregationFreqBuilder) {
        statfulSDKConfigurationBuilder.gauge(aggregationFreqBuilder);

        return this;
    }

    public void build() {
        statfulSDKConfigurationBuilder.dataUsageMetricsCollection(metricsCollectionConfiguration.shouldCollectDataUsageMetrics(), metricsCollectionConfiguration.getDataUsageMetricsCollectionIntervalMillis());
        statfulSDKConfigurationBuilder.memoryAllocationMetricsCollection(metricsCollectionConfiguration.shouldCollectMemoryAllocationMetrics(), metricsCollectionConfiguration.getMemoryAllocationCollectionIntervalMillis());
        statfulSDKConfigurationBuilder.storageUsageMetricsCollection(metricsCollectionConfiguration.shouldCollectStorageUsageMetrics(), metricsCollectionConfiguration.getStorageUsageCollectionIntervalMillis());
        statfulSDKConfigurationBuilder.launchTimeMetricsCollection(metricsCollectionConfiguration.shouldCollectLaunchTimeMetrics());
        statfulSDKConfigurationBuilder.crashesMetricsCollection(metricsCollectionConfiguration.shouldCollectCrashesMetrics());
        statfulSDKConfigurationBuilder.foregroundBackgroundTimesMetricsCollection(metricsCollectionConfiguration.shouldCollectForegroundBackgroundTimesMetrics());
        statfulSDKConfigurationBuilder.layoutTimesMetricsCollection(metricsCollectionConfiguration.shouldCollectLayoutTimesMetrics());


        if (!host.isEmpty()) {
            statfulSDKConfigurationBuilder.host(host);
        }

        if (!token.isEmpty()) {
            statfulSDKConfigurationBuilder.token(token);
        }

        statfulSDK = statfulSDKConfigurationBuilder.init();

        downloadMetricsConfiguration();
    }


    private static void downloadMetricsConfiguration() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueueRequestId);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String path = targetApplication.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "appscatter_configuration.json";

                            try {
                                String jsonString = Utils.getStringFromFile(path);
                                metricsCollectionConfiguration.updateConfiguration(jsonString);

                                if (statfulSDK != null) {
                                    statfulSDK.onMetricsCollectionConfigurationChanged(metricsCollectionConfiguration);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        targetApplication.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        String filename = BuildConfig.S3BUCKET + "/users/" + userIdHash + "/statful_configuration.json";
        enqueueRequestId = enqueueDownload(filename);
    }

    // this method should return functional StatfulSDK only for the users that are allowed
    // to put custom metrics, otherwise returns a no-op version
    public static StatfulSDK getMetricsProvider() {
        if (metricsCollectionConfiguration.shouldCollectCustomMetrics()) {

            if (statfulSDK == null) {
                throw new IllegalStateException("You must initialize metrics provider using AppScatterBuilder");
            }
            return statfulSDK;
        }

        return noOpStatfulSDK;
    }

    private static long enqueueDownload(final String filename) {
        DownloadManager dm = (DownloadManager) targetApplication.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(filename));
        request.setVisibleInDownloadsUi(false);
        return dm.enqueue(request.setDestinationInExternalFilesDir(targetApplication, Environment.DIRECTORY_DOWNLOADS, "appscatter_configuration.json"));
    }
}