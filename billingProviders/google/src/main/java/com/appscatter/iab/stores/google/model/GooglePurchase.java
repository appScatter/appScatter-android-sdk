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

package com.appscatter.iab.stores.google.model;

import org.json.JSONException;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This model represents purchase made in Google Play.
 */
public class GooglePurchase extends GoogleModel {

    protected static final String NAME_ORDER_ID = "orderId";
    protected static final String NAME_PACKAGE_NAME = "packageName";
    protected static final String NAME_PURCHASE_TOKEN = "purchaseToken";
    protected static final String NAME_PURCHASE_STATE = "purchaseState";
    protected static final String NAME_PURCHASE_TIME = "purchaseTime";
    protected static final String NAME_DEVELOPER_PAYLOAD = "developerPayload";
    protected static final String NAME_AUTO_RENEWING = "autoRenewing";


    @Nullable
    protected final String orderId;
    @NonNull
    protected final String packageName;
    @NonNull
    protected final String purchaseToken;

    protected @PurchaseState final int purchaseState;
    @Nullable
    protected final String developerPayload;
    protected final long purchaseTime;
    protected final boolean autoRenewing;


    public GooglePurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.orderId = jsonObject.optString(NAME_ORDER_ID, null);
        this.packageName = jsonObject.getString(NAME_PACKAGE_NAME);
        this.purchaseToken = jsonObject.getString(NAME_PURCHASE_TOKEN);
        this.developerPayload = jsonObject.optString(NAME_DEVELOPER_PAYLOAD, null);
        this.purchaseTime = jsonObject.getLong(NAME_PURCHASE_TIME);
        this.autoRenewing = jsonObject.optBoolean(NAME_AUTO_RENEWING, false);

        @PurchaseState final int purchaseStateCode = jsonObject.getInt(NAME_PURCHASE_STATE);
        if (purchaseStateCode > PurchaseState.REFUNDED) {
            throw new JSONException("Unrecognized purchase state: " + purchaseStateCode);
        }
        this.purchaseState = purchaseStateCode;
    }

    /**
     * Gets a unique order identifier of the transaction. This identifier corresponds to the
     * Google Wallet Order ID.
     *
     * @return Unique order ID, can be null for test purchases.
     */
    @Nullable
    public String getOrderId() {
        return orderId;
    }

    /**
     * Gets application package from which the purchase is originated.
     *
     * @return Package name, can't be null.
     */
    @NonNull
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets token that uniquely identifies a purchase for a given item and user pair.
     *
     * @return Purchase token, can't be null.
     */
    @NonNull
    public String getPurchaseToken() {
        return purchaseToken;
    }

    /**
     * Gets purchase state of the order.
     *
     * @return Purchase state, can't be null.
     */
    @PurchaseState
    public int getPurchaseState() {
        return purchaseState;
    }

    /**
     * A developer-specified string that contains supplemental information about an order.
     *
     * @return Developer payload, can be null.
     */
    @Nullable
    public String getDeveloperPayload() {
        return developerPayload;
    }

    /**
     * Gets the time the product was purchased, in milliseconds since the epoch (Jan 1, 1970).
     *
     * @return Time of purchase.
     */
    public long getPurchaseTime() {
        return purchaseTime;
    }

    /**
     * Indicates whether the subscription renews automatically.
     *
     * @return True if subscription is active, and will automatically renew on the next billing
     * date. False if the user has canceled the subscription.
     */
    public boolean isAutoRenewing() {
        return autoRenewing;
    }
}
