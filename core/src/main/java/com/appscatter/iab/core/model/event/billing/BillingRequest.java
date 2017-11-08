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

package com.appscatter.iab.core.model.event.billing;

import com.appscatter.iab.core.billing.BillingProvider;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Model class representing request for some action from {@link BillingProvider}.
 * <p>
 * Please note that not every single request will lead to a corresponding {@link BillingResponse},
 * different {@link BillingProvider}s can behave differently.
 */
public abstract class BillingRequest extends BillingEvent {

//    @SuppressFBWarnings({"NFF_NON_FUNCTIONAL_FIELD"})
    @Nullable
    private final transient Reference<Activity> activityReference;
    private final boolean activityHandlesResult;

    protected BillingRequest(@BillingEventType final int type,
            @Nullable final Activity activity,
            final boolean activityHandlesResult) {
        super(type);
        if (activity == null && activityHandlesResult) {
            throw new IllegalArgumentException();
        }
        this.activityReference = activity == null ? null : new WeakReference<>(activity);
        this.activityHandlesResult = activityHandlesResult;
    }

    @Nullable
    public Reference<Activity> getActivity() {
        return activityReference;
    }

    /**
     * Checks whether activity from {@link #getActivity()} delegates
     * {@link Activity#onActivityResult(int, int, Intent)} to library.
     *
     * @return True if onActivityResult() is delegated, false otherwise.
     */
    public boolean isActivityHandlesResult() {
        return activityHandlesResult;
    }
}
