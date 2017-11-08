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

import com.appscatter.iab.core.api.FragmentIabHelper;
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.model.ComponentState;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * This {@link IabHelper} implementation works with supplied fragment instance.
 * <p>
 * {@link com.appscatter.iab.core.android.ASIabFragment} is attached to it to monitor lifecycle and
 * automatically call {@link #register()} and {@link #unregister()} when appropriate.
 */
class FragmentIabHelperImpl extends ComponentIabHelper implements FragmentIabHelper {

    @Nullable
    private final android.app.Fragment fragment;
    @Nullable
    private final android.support.v4.app.Fragment supportFragment;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    FragmentIabHelperImpl(
            @Nullable final android.support.v4.app.Fragment supportFragment,
            @Nullable final android.app.Fragment fragment) {
        super(supportFragment == null ? null : supportFragment.getChildFragmentManager(),
                fragment == null ? null : fragment.getChildFragmentManager());
        this.fragment = fragment;
        this.supportFragment = supportFragment;
    }

    @NonNull
    @Override
    protected Activity getActivity() {
        final Activity activity;
        if (supportFragment != null) {
            activity = supportFragment.getActivity();
        } else if (fragment != null) {
            activity = fragment.getActivity();
        } else {
            // Fragment is detached from the activity.
            throw new IllegalStateException("Fragment is detached!");
        }
        return activity;
    }

    @Override
    protected void handleState(@ComponentState final int type) {
        // Handle billing events depending on fragment lifecycle
        if (type == ComponentState.ATTACH || type == ComponentState.CREATE_VIEW) {
            // Attach - subscribe for billing events right away when helper is created
            // CreateView - subscribe after view is recreated
            register();
        } else if (type == ComponentState.DESTROY_VIEW) {
            // DestroyView - don't handle any callbacks if fragment view is destroyed
            unregister();
        } else if (type == ComponentState.DESTROY) {
            // Destroy - fragment is being destroyed, unsubscribe from everything
            unregister();
            unregisterEvents();
        }
    }

    @Override
    public void purchase(@NonNull final String sku) {
        postRequest(new PurchaseRequest(getActivity(), false, sku));
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        postRequest(new ConsumeRequest(getActivity(), false, purchase));
    }

    @Override
    public void inventory(final boolean startOver) {
        postRequest(new InventoryRequest(getActivity(), false, startOver));
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        postRequest(new SkuDetailsRequest(getActivity(), false, skus));
    }
}
