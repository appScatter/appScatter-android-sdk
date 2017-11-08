package com.mindera.metrics.statful;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public class RemoteMetricsConfigurationSharedPreferencesRepository implements RemoteMetricsConfigurationRepository, ConfigurationUpdater {

    private static final String REMOTE_CONFIG_PREFERENCES = "RemoteConfigSharePreferences";

    private static final String KEY_DATA_USAGE = "dataUsage";
    private static final String KEY_MEMORY_ALLOCATION = "memoryAllocation";
    private static final String KEY_STORAGE_USAGE = "storageUsage";
    private static final String KEY_CRASHES = "crashCount";
    private static final String KEY_FOREGROUND_BACKGROUND_TIMES = "backgroundForegroundTimes";
    private static final String KEY_LAUNCH_TIMES = "launchTimes";
    private static final String KEY_LAYOUT_TIMES = "layoutTimes";
    private static final String KEY_CUSTOM = "custom";

    private SharedPreferences configurationSharedPreferences;

    RemoteMetricsConfigurationSharedPreferencesRepository(final Context context) {
        this.configurationSharedPreferences = context.getSharedPreferences(REMOTE_CONFIG_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void updateConfiguration(String remoteJsonConfiguration) {
        SharedPreferences.Editor editor = configurationSharedPreferences.edit();
        try {
            JSONObject jsonObject = new JSONObject(remoteJsonConfiguration);
            JSONObject statful = jsonObject.getJSONObject("statful");

            editor.putBoolean(KEY_DATA_USAGE, statful.getBoolean(KEY_DATA_USAGE));
            editor.putBoolean(KEY_MEMORY_ALLOCATION, statful.getBoolean(KEY_MEMORY_ALLOCATION));
            editor.putBoolean(KEY_STORAGE_USAGE, statful.getBoolean(KEY_STORAGE_USAGE));
            editor.putBoolean(KEY_CRASHES, statful.getBoolean(KEY_CRASHES));
            editor.putBoolean(KEY_FOREGROUND_BACKGROUND_TIMES, statful.getBoolean(KEY_FOREGROUND_BACKGROUND_TIMES));
            editor.putBoolean(KEY_LAUNCH_TIMES, statful.getBoolean(KEY_LAUNCH_TIMES));
            editor.putBoolean(KEY_LAYOUT_TIMES, statful.getBoolean(KEY_LAYOUT_TIMES));
            editor.putBoolean(KEY_CUSTOM, statful.getBoolean(KEY_CUSTOM));
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean shouldCollectDataUsageMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_DATA_USAGE)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_DATA_USAGE,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectMemoryAllocationMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_MEMORY_ALLOCATION)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_MEMORY_ALLOCATION,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectStorageUsageMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_STORAGE_USAGE)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_STORAGE_USAGE,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectLaunchTimeMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_LAUNCH_TIMES)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_LAUNCH_TIMES,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectCrashesMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_CRASHES)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_CRASHES,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectForegroundBackgroundTimesMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_FOREGROUND_BACKGROUND_TIMES)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_FOREGROUND_BACKGROUND_TIMES,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectLayoutTimesMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_LAYOUT_TIMES)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_LAYOUT_TIMES,
                    true
            );
        }

        return null;
    }

    @Override
    public Boolean shouldCollectCustomMetrics() {
        if (this.configurationSharedPreferences.contains(KEY_CUSTOM)) {
            return this.configurationSharedPreferences.getBoolean(
                    KEY_CUSTOM,
                    true
            );
        }

        return null;
    }

    @Override
    public long getDataUsageMetricsCollectionIntervalMillis() {
        return 0;
    }

    @Override
    public long getMemoryAllocationCollectionIntervalMillis() {
        return 0;
    }

    @Override
    public long getStorageUsageCollectionIntervalMillis() {
        return 0;
    }
}
