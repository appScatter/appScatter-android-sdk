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

import com.appscatter.iab.core.api.ActivityIabHelper;
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.model.ComponentState;
import com.appscatter.iab.core.model.billing.Purchase;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.Set;

/**
 * This {@link IabHelper} implementation works with supplied Activity instance.
 * <p>
 * {@link com.appscatter.iab.core.android.ASIabFragment} will be attached to it to monitor lifecycle and
 * automatically call {@link #register()} and {@link #unregister()} when appropriate.
 */
class ActivityIabHelperImpl extends ComponentIabHelper implements ActivityIabHelper {

    @Nullable
    private final Activity activity;
    @Nullable
    private final FragmentActivity fragmentActivity;


    ActivityIabHelperImpl(@Nullable final FragmentActivity fragmentActivity,
            @Nullable final Activity activity) {
        super(fragmentActivity == null ? null : fragmentActivity.getSupportFragmentManager(),
                activity == null ? null : activity.getFragmentManager());
        this.activity = activity;
        this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @Override
    protected Activity getActivity() {
        if (fragmentActivity != null) {
            return fragmentActivity;
        }
        if (activity != null) {
            return activity;
        }

        throw new IllegalStateException();
    }

    @Override
    protected void handleState(@ComponentState final int type) {
        // Handle billing events depending on fragment lifecycle
        if (type == ComponentState.ATTACH || type == ComponentState.START || type == ComponentState.RESUME) {
            // Attach - subscribe for billing events right away when helper is created
            // Start - necessary to handle onActivityResult since it's called before onResume
            // Resume - re-subscribe for billing events if we unsubscribed in onPause
            register();
        } else if (type == ComponentState.STOP || type == ComponentState.PAUSE) {
            // Pause - only callback guaranteed to be called
            // Stop - mirror onStart
            unregister();
            // We won't be needing any lifecycle events if activity is finishing
            if (getActivity().isFinishing()) {
                unregisterEvents();
            }
        } else if (type == ComponentState.DETACH) {
            // Detach - fragment is removed, unsubscribe from everything
            unregister();
            unregisterEvents();
        }
    }

    @Override
    public void purchase(@NonNull final String sku) {
        purchase(getActivity(), sku);
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        consume(getActivity(), purchase);
    }

    @Override
    public void inventory(final boolean startOver) {
        inventory(getActivity(), startOver);
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        skuDetails(getActivity(), skus);
    }
}
