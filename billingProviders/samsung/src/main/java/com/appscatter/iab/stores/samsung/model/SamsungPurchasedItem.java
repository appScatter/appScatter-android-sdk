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
import android.support.annotation.Nullable;

import java.util.Date;

public class SamsungPurchasedItem extends SamsungBillingModel {

    protected static final String KEY_SUBSCRIPTION_END_DATE = "mSubscriptionEndDate";


    @NonNull
    protected final ItemType itemType;
    @Nullable
    protected final Date subscriptionEndDate;

    public SamsungPurchasedItem(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.itemType = SamsungUtils.getItemType(jsonObject);

        final String dateString = jsonObject.optString(KEY_SUBSCRIPTION_END_DATE);
        this.subscriptionEndDate = SamsungUtils.parseDate(dateString);
    }

    @NonNull
    public ItemType getItemType() {
        return itemType;
    }

    @Nullable
    public Date getSubscriptionEndDate() {
        return subscriptionEndDate;
    }
}
