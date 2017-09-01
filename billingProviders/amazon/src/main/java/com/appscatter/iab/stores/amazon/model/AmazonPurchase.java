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

package com.appscatter.iab.stores.amazon.model;

import com.amazon.device.iap.internal.model.ReceiptBuilder;
import com.amazon.device.iap.internal.model.UserDataBuilder;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;
import com.appscatter.iab.stores.amazon.AmazonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

public class AmazonPurchase extends AmazonModel {

    protected static final String NAME_ITEM_TYPE = "itemType";
    protected static final String NAME_RECEIPT_ID = "receiptId";
    protected static final String NAME_PURCHASE_DATE = "purchaseDate";
    protected static final String NAME_CANCEL_DATE = "endDate";
    protected static final String NAME_USERDATA = "userData";
    protected static final String NAME_USER_ID = "userId";
    protected static final String NAME_MARKETPLACE = "marketplace";

    @NonNull
    protected final Receipt receipt;
    @Nullable
    protected final UserData userData;

    public AmazonPurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        final ProductType productType = ProductType.valueOf(jsonObject.getString(NAME_ITEM_TYPE));
        if (productType == null) {
            throw new JSONException("Unknown product type.");
        }
        final ReceiptBuilder builder = new ReceiptBuilder()
                .setSku(sku)
                .setReceiptId(jsonObject.getString(NAME_RECEIPT_ID))
                .setPurchaseDate(AmazonUtils.readDate(jsonObject, NAME_PURCHASE_DATE));
        builder.setProductType(productType);
        if (jsonObject.has(NAME_CANCEL_DATE)) {
            builder.setCancelDate(AmazonUtils.readDate(jsonObject, NAME_CANCEL_DATE));
        }
        this.receipt = new Receipt(builder);

        final JSONObject userData = jsonObject.optJSONObject(NAME_USERDATA);
        if (userData == null) {
            this.userData = null;
        } else {
            this.userData = new UserDataBuilder()
                    .setUserId(userData.getString(NAME_USER_ID))
                    .setMarketplace(userData.getString(NAME_MARKETPLACE))
                    .build();
        }
    }

    /**
     * @see Receipt#getSku()
     */
    @NonNull
    public String getSku() {
        return receipt.getSku();
    }

    /**
     * @see Receipt#getProductType()
     */
    @NonNull
    public ProductType getProductType() {
        return receipt.getProductType();
    }

    /**
     * @see Receipt#getReceiptId()
     */
    @NonNull
    public String getReceiptId() {
        return receipt.getReceiptId();
    }

    /**
     * @see Receipt#getPurchaseDate()
     */
    @NonNull
    public Date getPurchaseDate() {
        return receipt.getPurchaseDate();
    }

    /**
     * @see Receipt#getCancelDate()
     */
    @Nullable
    public Date getCancelDate() {
        return receipt.getCancelDate();
    }

    /**
     * @see Receipt#isCanceled()
     */
    public boolean isCanceled() {
        return receipt.isCanceled();
    }

    /**
     * @see UserData#getUserId()
     */
    @Nullable
    public String getUserId() {
        return userData == null ? null : userData.getUserId();
    }

    /**
     * @see UserData#getMarketplace()
     */
    @Nullable
    public String getMarketplace() {
        return userData == null ? null : userData.getMarketplace();
    }

}

