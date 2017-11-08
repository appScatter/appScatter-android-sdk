/*
 * Copyright 2012-2015 One Platform Foundation
 * Copyright 2016 AppScatter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appscatter.iab.core;

import com.appscatter.iab.core.android.ASIabActivity;
import com.appscatter.iab.core.model.ComponentState;
import com.appscatter.iab.core.model.event.ActivityResultRequest;
import com.appscatter.iab.core.model.event.android.ActivityNewIntentEvent;
import com.appscatter.iab.core.model.event.android.ActivityResult;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.util.ActivityForResultLauncher;
import com.appscatter.iab.core.util.BillingUtils;
import com.appscatter.iab.core.util.SyncedReference;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.appscatter.iab.core.model.ComponentState.CREATE;
import static com.appscatter.iab.core.model.ComponentState.DESTROY;
import static com.appscatter.iab.core.model.ComponentState.PAUSE;
import static com.appscatter.iab.core.model.ComponentState.RESUME;
import static com.appscatter.iab.core.model.ComponentState.START;
import static com.appscatter.iab.core.model.ComponentState.STOP;

/**
 * This class is designed to monitor the existing {@link Activity}s lifecycle.
 * <p>
 * Intended for internal use.
 */
public final class ActivityMonitor implements Application.ActivityLifecycleCallbacks {

    /**
     * Map of the existing activities lifecycle states.
     * <p>
     * Backed up by {@link WeakHashMap} to avoid memory leaks.
     */
    @SuppressWarnings("Convert2Diamond")
//    @SuppressFBWarnings({"PMB_POSSIBLE_MEMORY_BLOAT"})
    private static final Map<Activity, Integer> STATE_MAP =
            Collections.synchronizedMap(new WeakHashMap<Activity, Integer>());
    @Nullable
    private static ActivityMonitor instance;
    private final Context context;
    @Nullable
    private volatile SyncedReference<Activity> syncActivity;
    @Nullable
    private volatile SyncedReference<ActivityResult> syncResult;
    private volatile int pendingRequestCode;

    private ActivityMonitor(final Context context) {
        super();
        this.context = context;
    }

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    public static ActivityMonitor getInstance(@NonNull final Context context) {
        ASChecks.checkThread(true);
        if (instance == null) {
            instance = new ActivityMonitor(context);
        }
        return instance;
    }

    /**
     * Sets lifecycle state of the supplied activity.
     *
     * @param activity Activity object to set lifecycle state for.
     */
    public static void setState(@NonNull final Activity activity,
            @ComponentState final int componentState) {
        STATE_MAP.put(activity, componentState);
    }

    /**
     * Gets last known lifecycle state of the supplied activity.
     *
     * @param activity Activity object to get lifecycle state for.
     * @return Current lifecycle state if known, null otherwise.
     */
    @ComponentState
    public static int getState(@NonNull final Activity activity) {
        @ComponentState final int state = STATE_MAP.get(activity) != null ? STATE_MAP.get(activity) : ComponentState.UNKNOWN;
        return state;
    }

    /**
     * Checks if supplied activity is in resumed state.
     *
     * @param activity Activity object to check lifecycle state for.
     * @return True if supplied activity is resumed.
     * @see {@link Activity#onResume()}
     */
    public static boolean isResumed(@NonNull final Activity activity) {
        return getState(activity) == RESUME;
    }

    public static boolean isStarted(@NonNull final Activity activity) {
        return Arrays.asList(RESUME, PAUSE, START).contains(getState(activity));
    }

    public static boolean isDestroyed(@NonNull final Activity activity) {
        return getState(activity) == DESTROY;
    }

    @Nullable
    private Activity getResultHandlingActivity(
            @NonNull final BillingRequest billingRequest) {
        final Activity activity = BillingUtils.getActivity(billingRequest);
        if (activity != null && billingRequest.isActivityHandlesResult()) {
            return activity;
        }
        final SyncedReference<Activity> newSyncActivity = new SyncedReference<>();
        this.syncActivity = newSyncActivity;
        ASIabActivity.start(activity != null ? activity : context);
        ASLog.d("Waiting for activity...");
        return newSyncActivity.get();
    }

    public void registerForEvents() {
        ASIab.getEvents(ActivityResultRequest.class).subscribe(this::onActivityResultRequestEvent);
        ASIab.getEvents(ActivityNewIntentEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onActivityNewIntentEvent);
        ASIab.getEvents(ActivityResult.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onActivityResultEvent);
    }

    private void onActivityResultRequestEvent(@NonNull final ActivityResultRequest resultRequest) {
        ASChecks.checkThread(false);
        final SyncedReference<ActivityResult> syncResult = resultRequest.getSyncActivityResult();
        if (this.syncActivity != null || this.syncResult != null) {
            ASLog.e("Another ActivityResultRequest is being handled.");
            syncResult.set(null);
        }
        final BillingRequest billingRequest = resultRequest.getRequest();
        final Activity activity = getResultHandlingActivity(billingRequest);
        if (activity == null) {
            return;
        }
        final ActivityForResultLauncher launcher = resultRequest.getLauncher();
        this.pendingRequestCode = launcher.getRequestCode();
        this.syncResult = syncResult;
        try {
            launcher.onStartForResult(activity);
        } catch (ActivityNotFoundException | IntentSender.SendIntentException exception) {
            ASLog.e("", exception);
            this.syncResult = null;
            syncResult.set(null);
        }
    }

    private void onActivityNewIntentEvent(@NonNull final ActivityNewIntentEvent intentEvent) {
        final SyncedReference<Activity> syncActivity = this.syncActivity;
        final Activity activity = intentEvent.getActivity();
        if (activity.getClass() == ASIabActivity.class && syncActivity != null) {
            this.syncActivity = null;
            syncActivity.set(activity);
        }
    }

    private void onActivityResultEvent(@NonNull final ActivityResult event) {
        final SyncedReference<ActivityResult> syncResult = this.syncResult;
        if (syncResult != null && event.getRequestCode() == pendingRequestCode) {
            this.syncResult = null;
            syncResult.set(event);
            final Activity activity = event.getActivity();
            if (activity.getClass() == ASIabActivity.class) {
                activity.finish();
            }
        }
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        setState(activity, CREATE);
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        setState(activity, START);
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        setState(activity, RESUME);
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        setState(activity, PAUSE);
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        setState(activity, STOP);
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
        // ignore
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        setState(activity, DESTROY);
    }
}
