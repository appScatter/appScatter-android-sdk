/*
 * Copyright 2012-2015 One Platform Foundation
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
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.api.SimpleIabHelper;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.event.android.ActivityResult;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This implementation of {@link IabHelper} allows passing existing {@link Activity} object thus
 * avoiding usage of {@link ASIabActivity}.
 * <p>
 * Supplied Activity <b>must</b> delegate {@link Activity#onActivityResult(int, int, Intent)}
 * callback to this helper.
 *
 * @see #purchase(Activity, String)
 */
class SimpleIabHelperImpl extends IabHelperImpl implements SimpleIabHelper {

    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final String sku) {
        postRequest(new PurchaseRequest(activity, true, sku));
    }

    @Override
    public void consume(@NonNull final Activity activity, @NonNull final Purchase purchase) {
        postRequest(new ConsumeRequest(activity, true, purchase));
    }

    @Override
    public void inventory(@NonNull final Activity activity, final boolean startOver) {
        postRequest(new InventoryRequest(activity, true, startOver));
    }

    @Override
    public void skuDetails(@NonNull final Activity activity, @NonNull final Set<String> skus) {
        postRequest(new SkuDetailsRequest(activity, true, skus));
    }

    @Override
    public void skuDetails(@NonNull final Activity activity, @NonNull final String... skus) {
        skuDetails(activity, new HashSet<>(Arrays.asList(skus)));
    }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
            final int resultCode, @Nullable final Intent data) {
        ASIab.post(new ActivityResult(activity, requestCode, resultCode, data));
    }
}
