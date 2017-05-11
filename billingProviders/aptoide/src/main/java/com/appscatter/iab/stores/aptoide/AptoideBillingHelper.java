/*
 * Copyright (c) 2016. AppScatter
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

package com.appscatter.iab.stores.aptoide;

import com.appscatter.iab.core.billing.AidlBillingHelper;
import com.appscatter.iab.stores.aptoide.model.ItemType;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASPreferences;
import com.appscatter.iab.utils.ASUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cm.aptoide.pt.iab.AptoideInAppBillingService;

import static com.appscatter.iab.stores.aptoide.AptoideBillingProvider.NAME;

public class AptoideBillingHelper extends AidlBillingHelper<AptoideInAppBillingService> {
    protected static final String INTENT_ACTION = "cm.aptoide.pt.iab.action.BIND";
    protected static final String INTENT_PACKAGE = "cm.aptoide.pt";
    protected static final String KEY_CONTINUATION_TOKEN = NAME + ".continuation_token.";

    protected static final int API = 4;
    protected static final int BATCH_SIZE = 20;


    @NonNull
    protected final String packageName = context.getPackageName();
    @NonNull
    protected final ASPreferences preferences = new ASPreferences(context);

    protected AptoideBillingHelper(@NonNull final Context context) {
        super(context, AptoideInAppBillingService.class);
    }

    /**
     * Tries to check if Aptoide billing is available on current device.
     *
     * @return {@link Response#OK} if billing is supported, another corresponding {@link Response}
     * if it's not. Returns null if error has occurred.
     */
    @Response
    public int isBillingSupported() {
        ASLog.logMethod();
        final AptoideInAppBillingService service = getService();
        if (service == null) {
            // Can't connect to service.
            return Response.UNKNOWN;
        }
        try {
            for (final ItemType itemType : ItemType.values()) {
                @Response final int response = service.isBillingSupported(API, packageName, itemType.toString());
                if (response != Response.OK) {
                    // Report first encountered unsuccessful response.
                    return response;
                }
            }
            return Response.OK;
        } catch (RemoteException exception) {
            ASLog.d("Billing check failed.", exception);
        }
        return Response.UNKNOWN;
    }

    /**
     * Wraps {@link AptoideInAppBillingService#getBuyIntent(int, String, String, String, String)}
     *
     * @param sku      SKU of a product to purchase.
     * @param itemType Type of an item to purchase.
     *
     * @return Bundle containing purchase intent. Can be null.
     */
    @Nullable
    public Bundle getBuyIntent(@NonNull final String sku, @NonNull final ItemType itemType) {
        ASLog.logMethod(sku, itemType);
        final AptoideInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        try {
            final String type = itemType.toString();
            final Bundle result = service.getBuyIntent(API, packageName, sku, type, "");
            @Response final int response = AptoideUtils.getResponse(result);
            ASLog.d("Response: %s. Result: %s", response, ASUtils.toString(result));
            return result;
        } catch (RemoteException exception) {
            ASLog.d("getBuyIntent request failed.", exception);
        }
        return null;
    }

    /**
     * Wraps {@link AptoideInAppBillingService#consumePurchase(int, String, String)}
     *
     * @param token Token of a purchase to consume.
     *
     * @return Result of the operation. Can be null.
     */
    @Response
    public int consumePurchase(@NonNull final String token) {
        ASLog.logMethod(token);
        final AptoideInAppBillingService service = getService();
        if (service == null) {
            return Response.UNKNOWN;
        }
        try {
            @Response final int response = service.consumePurchase(API, packageName, token);
            ASLog.d("Response: %s", response);
            return response;
        } catch (RemoteException exception) {
            ASLog.e("consumePurchase request failed.", exception);
        }
        return Response.UNKNOWN;
    }

    /**
     * Wraps {@link AptoideInAppBillingService#getSkuDetails(int, String, String, Bundle)}.
     *
     * @param skus SKUs to load details for.
     *
     * @return Bundle containing requested SKUs details. Can be null.
     */
    @Nullable
    public Bundle getSkuDetails(@NonNull final Collection<String> skus) {
        ASLog.logMethod(Arrays.toString(skus.toArray()));
        final AptoideInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        final List<String> skuList = new ArrayList<>(skus);
        final Bundle result = new Bundle();
        try {
            final int size = skuList.size();
            final int batchCount = size / BATCH_SIZE;
            for (int i = 0; i <= batchCount; i++) {
                final int first = i * BATCH_SIZE;
                final int last = Math.min((i + 1) * BATCH_SIZE, size);
                final ArrayList<String> batch = new ArrayList<>(skuList.subList(first, last));
                final Bundle bundle = AptoideUtils.putSkuList(new Bundle(), batch);
                for (final ItemType itemType : ItemType.values()) {
                    final String type = itemType.toString();
                    final Bundle details = service.getSkuDetails(API, packageName, type, bundle);
                    @Response final int response = AptoideUtils.getResponse(details);
                    ASLog.d("From %d to %d. Type: %s. Response: %s. Details: %s.",
                            first, last, itemType, response, ASUtils.toString(details));
                    if (response != Response.OK) {
                        // Return received bundle if error is encountered
                        return details;
                    } else {
                        // Aggregate all loaded details in a single bundle
                        final ArrayList<String> skuDetails = AptoideUtils.getSkuDetails(details);
                        AptoideUtils.addSkuDetails(result, skuDetails);
                    }
                }
            }
        } catch (RemoteException exception) {
            ASLog.e("getSkuDetails request failed.", exception);
            return null;
        }
        return AptoideUtils.putResponse(result, Response.OK);
    }

    /**
     * Wraps {@link AptoideInAppBillingService#getPurchases(int, String, String, String)}.
     *
     * @param startOver Flag indicating whether inventory should be loaded from the start or from
     *                  the point of the previous successful request.
     *
     * @return Bundle containing user inventory. Can be null.
     */
    @Nullable
    public Bundle getPurchases(final boolean startOver) {
        ASLog.logMethod(startOver);
        final AptoideInAppBillingService service = getService();
        if (service == null) {
            return null;
        }
        final Bundle result = new Bundle();
        try {
            for (final ItemType itemType : ItemType.values()) {
                final String type = itemType.toString();
                final String key = KEY_CONTINUATION_TOKEN + type;
                // Try to use last successful request token if required
                final String token = startOver ? null : preferences.getString(key);
                final Bundle purchases = service.getPurchases(API, packageName, type, token);
                @Response final int response = AptoideUtils.getResponse(purchases);
                ASLog.d("Type: %s. Response: %s. Purchases: %s.",
                        itemType, response, ASUtils.toString(purchases));
                if (response != Response.OK) {
                    return purchases;
                } else {
                    final ArrayList<String> purchaseDataList = AptoideUtils.getDataList(purchases);
                    final ArrayList<String> itemList = AptoideUtils.getItemList(purchases);
                    final ArrayList<String> signatureList = AptoideUtils.getSignatureList(purchases);
                    final String newToken = AptoideUtils.getContinuationToken(purchases);
                    // Aggregate all responses in a single bundle
                    AptoideUtils.addDataList(result, purchaseDataList);
                    AptoideUtils.addItemList(result, itemList);
                    AptoideUtils.addSignatureList(result, signatureList);
                    // Save token for future use
                    if (TextUtils.isEmpty(newToken)) {
                        preferences.remove(key);
                    } else {
                        preferences.put(key, newToken);
                    }
                }
            }
        } catch (RemoteException exception) {
            ASLog.e("getPurchases request failed.", exception);
            return null;
        }
        return AptoideUtils.putResponse(result, Response.OK);
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent serviceIntent = new Intent(INTENT_ACTION);
        serviceIntent.setPackage(INTENT_PACKAGE);
        return serviceIntent;
    }

}
