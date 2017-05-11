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
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Response from {@link BillingProvider} for corresponding {@link ConsumeRequest}.
 */
public class ConsumeResponse extends BillingResponse {

    private static final String NAME_PURCHASE = "purchase";

    @NonNull
    private final Purchase purchase;

    public ConsumeResponse(@NonNull @Status final int status,
            @Nullable final String providerName,
            @NonNull final Purchase purchase) {
        super(BillingEventType.CONSUME, status, providerName);
        this.purchase = purchase;
    }

    /**
     * Gets Purchase intended for consumption.
     *
     * @return Purchase object. Can't be null.
     */
    @NonNull
    public Purchase getPurchase() {
        return purchase;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_PURCHASE, purchase.toJson());
        } catch (JSONException exception) {
            ASLog.e("", exception);
        }
        return jsonObject;
    }
}
