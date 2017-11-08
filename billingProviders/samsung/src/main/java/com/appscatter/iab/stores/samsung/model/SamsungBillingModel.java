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

import java.util.Date;

public abstract class SamsungBillingModel extends SamsungModel {

    protected static final String KEY_PURCHASE_ID = "mPurchaseId";
    protected static final String KEY_PAYMENT_ID = "mPaymentId";
    protected static final String KEY_PURCHASE_DATE = "mPurchaseDate";

    @NonNull
    protected final String purchaseId;
    @NonNull
    protected final String paymentId;
    @NonNull
    protected final Date purchaseDate;

    public SamsungBillingModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.purchaseId = jsonObject.getString(KEY_PURCHASE_ID);
        this.paymentId = jsonObject.getString(KEY_PAYMENT_ID);

        final String dateString = jsonObject.getString(KEY_PURCHASE_DATE);
        final Date date = SamsungUtils.parseDate(dateString);
        if (date == null) {
            throw new JSONException("Invalid purchase date: " + dateString);
        }
        this.purchaseDate = date;
    }

    @NonNull
    public String getPurchaseId() {
        return purchaseId;
    }

    @NonNull
    public String getPaymentId() {
        return paymentId;
    }

    @NonNull
    public Date getPurchaseDate() {
        return purchaseDate;
    }
}
