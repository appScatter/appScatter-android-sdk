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
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Response from {@link BillingProvider} for corresponding {@link SkuDetailsRequest}.
 */
public class SkuDetailsResponse extends BillingResponse {

    private static final String NAME_SKUS_DETAILS = "skus_details";


    @NonNull
    private final Collection<SkuDetails> skusDetails = new ArrayList<>();

    public SkuDetailsResponse(@NonNull @Status final int status,
            @Nullable final String providerName,
            @Nullable final Collection<SkuDetails> skusDetails) {
        super(BillingEventType.SKU_DETAILS, status, providerName);
        if (skusDetails != null) {
            this.skusDetails.addAll(skusDetails);
        }
    }

    public SkuDetailsResponse(@NonNull @Status final int status,
            @Nullable final String providerName) {
        this(status, providerName, null);
    }

    /**
     * Gets details for corresponding SKUs.
     * <p>
     * Some SKUs might not have been recognized by {@link BillingProvider} and are left empty.
     *
     * @return Collection of SkuDetails objects. Can be null.
     *
     * @see #isSuccessful()
     * @see SkuDetails#isEmpty()
     */
    @NonNull
    public Collection<SkuDetails> getSkusDetails() {
        return Collections.unmodifiableCollection(skusDetails);
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            final JSONArray jsonArray = new JSONArray();
            for (final SkuDetails skuDetails : skusDetails) {
                jsonArray.put(skuDetails.toJson());
            }
            jsonObject.put(NAME_SKUS_DETAILS, jsonArray);
        } catch (JSONException exception) {
            ASLog.e("", exception);
        }
        return jsonObject;
    }
}
