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

package com.appscatter.iab.core.model.event.billing;

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Request for {@link BillingProvider} to consume {@link Purchase}.
 * @see ConsumeResponse
 */
public class ConsumeRequest extends BillingRequest {

    private static final String NAME_PURCHASE = "purchase";


    @NonNull
    private final Purchase purchase;

    public ConsumeRequest(@NonNull final Purchase purchase) {
        this(null, false, purchase);
    }

    public ConsumeRequest(@Nullable final Activity activity,
            final boolean activityHandlesResult,
            @NonNull final Purchase purchase) {
        super(BillingEventType.CONSUME, activity, activityHandlesResult);
        this.purchase = purchase;
    }

    /**
     * Gets Purchase intended for consumption.
     *
     * @return Purchase object.
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

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final ConsumeRequest that = (ConsumeRequest) o;

        if (!purchase.equals(that.purchase)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + purchase.hashCode();
        return result;
    }
    //CHECKSTYLE:ON
}
