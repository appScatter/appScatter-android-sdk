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

package com.appscatter.iab.stores.samsung.model;

import com.appscatter.iab.stores.samsung.SamsungUtils;

import org.json.JSONException;

import android.support.annotation.NonNull;

public class SamsungSkuDetails extends SamsungModel {

    protected static final String KEY_SUBSCRIPTION_UNIT = "mSubscriptionDurationUnit";
    protected static final String KEY_SUBSCRIPTION_MULTIPLIER = "mSubscriptionDurationMultiplier";


    @NonNull
    protected final ItemType itemType;
    @NonNull
    protected final String subscriptionUnit;
    @NonNull
    protected final String subscriptionMultiplier;

    public SamsungSkuDetails(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.subscriptionUnit = jsonObject.getString(KEY_SUBSCRIPTION_UNIT);
        this.subscriptionMultiplier = jsonObject.getString(KEY_SUBSCRIPTION_MULTIPLIER);
        this.itemType = SamsungUtils.getItemType(jsonObject);
    }

    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    @NonNull
    public String getSubscriptionUnit() {
        return subscriptionUnit;
    }

    @NonNull
    public String getSubscriptionMultiplier() {
        return subscriptionMultiplier;
    }
}
