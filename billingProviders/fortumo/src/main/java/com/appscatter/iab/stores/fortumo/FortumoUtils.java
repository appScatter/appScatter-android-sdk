/*
 * Copyright (c) 2016. AppScatter
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

package com.appscatter.iab.stores.fortumo;

import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.stores.fortumo.model.FortumoSkuDetails;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASPreferences;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mp.MpUtils;
import mp.PaymentResponse;

import static com.appscatter.iab.core.model.billing.SkuType.CONSUMABLE;
import static com.appscatter.iab.core.model.billing.SkuType.ENTITLEMENT;
import static com.appscatter.iab.core.model.billing.SkuType.SUBSCRIPTION;
import static com.appscatter.iab.stores.fortumo.FortumoBillingProvider.PURCHASES_KEY;

public class FortumoUtils {
    private static final String FORTUMO_PENDING_PRODUCT = "fortumo.purchases.pending.";


    /**
     * Converts from the Fortumo product type available on MpUtils to our @{@link SkuType}
     * @param fortumoProductType the Fortumo value that represents the product type
     * @return The corresponding @{@link SkuType} for the given Fortumo product type
     */
    @SkuType
    public static int convertType(final int fortumoProductType) {
        @SkuType final int convertedType;
        switch (fortumoProductType) {
            case MpUtils.PRODUCT_TYPE_CONSUMABLE:
                convertedType = CONSUMABLE;
                break;
            case MpUtils.PRODUCT_TYPE_NON_CONSUMABLE:
                convertedType = ENTITLEMENT;
                break;
            case MpUtils.PRODUCT_TYPE_SUBSCRIPTION:
                convertedType = SUBSCRIPTION;
                break;
            default:
                convertedType = SkuType.UNKNOWN;
                break;
        }

        return convertedType;
    }

    /**
     * Converts from our @{@link SkuType} to the Fortumo product type available on MpUtils
     * @param skuType the Fortumo value that represents the product type
     * @return The corresponding {@link MpUtils} constant for the given SkuType type
     */
    public static int reverseType(@SkuType final int skuType) {
        final int convertedType;
        switch (skuType) {
            case CONSUMABLE:
                convertedType = MpUtils.PRODUCT_TYPE_CONSUMABLE;
                break;
            case ENTITLEMENT:
                convertedType = MpUtils.PRODUCT_TYPE_NON_CONSUMABLE;
                break;
            case SUBSCRIPTION:
                convertedType = MpUtils.PRODUCT_TYPE_SUBSCRIPTION;
                break;
            default:
                convertedType = MpUtils.PRODUCT_TYPE_NON_CONSUMABLE;
                break;
        }

        return convertedType;
    }


    @NonNull
    public static Collection<Purchase> getInventory(@NonNull final Context context, @NonNull final InventoryRequest request,
            @NonNull final FortumoMappedSkuResolver skuResolver) {

        // Handle consumable products
        ASPreferences preferences = new ASPreferences(context);
        final Collection<Purchase> purchases = new ArrayList<>();

        Set<String> messageIds = preferences.getStringSet(PURCHASES_KEY);
        HashSet<String> finalMessageIds = new HashSet<>();

        if (messageIds != null) {
            long id;

            for (String messageId : messageIds) {
                if (messageId != null) {
                    id = Long.valueOf(messageId);
                    final PaymentResponse paymentResponse = MpUtils.getPaymentResponse(context, id);

                    if (paymentResponse.getBillingStatus() == MpUtils.MESSAGE_STATUS_BILLED) {
                        Purchase purchase = purchaseFromPaymentResponse(paymentResponse);
                        purchases.add(purchase);
                        finalMessageIds.add(messageId);
                    }
                    if (paymentResponse.getBillingStatus() != MpUtils.MESSAGE_STATUS_FAILED) {
                        finalMessageIds.add(messageId);
                    }
                }
            }
            preferences.put(PURCHASES_KEY, finalMessageIds);
        }

        // handle non consumable products
        for (FortumoSkuDetails fortumoProduct : skuResolver.getProducts().values()) {
            if (fortumoProduct.getItemType() != CONSUMABLE) {
                final List purchaseHistory = MpUtils.getPurchaseHistory(context, fortumoProduct.getServiceId(), fortumoProduct.getInAppSecret(), 5000);
                if (purchaseHistory != null && purchaseHistory.size() > 0) {
                    for (Object response : purchaseHistory) {
                        PaymentResponse paymentResponse = (PaymentResponse) response;
                        if (paymentResponse.getProductName().equals(fortumoProduct.getProductId())) {
                            purchases.add(purchaseFromPaymentResponse(paymentResponse));
                            break;
                        }
                    }
                }
            }
        }

        return purchases;
    }

    @NonNull
    public static Collection<SkuDetails> getSkusDetails(@NonNull final Collection<String> skus, @NonNull final FortumoMappedSkuResolver skuResolver) {

        final Collection<SkuDetails> skusDetails = new ArrayList<>();

        for (final String sku : skus) {
//            skusDetails.add(skuResolver.getMapedProduct(sku).toSkuDetails());
            if (skuResolver.getProduct(sku) != null) {
                skusDetails.add(skuResolver.getProduct(sku).toSkuDetails());
            }
        }

        return skusDetails;
    }

    public static String getMessageIdInPending(@NonNull Context context, String productId) {
        ASPreferences preferences = new ASPreferences(context);
        return preferences.getString(productId, null);
    }

    @NonNull
    public static Purchase purchaseFromPaymentResponse(@NonNull PaymentResponse paymentResponse) {
        Purchase.Builder purchaseBuilder = new Purchase.Builder(paymentResponse.getProductName())
                .setType(FortumoUtils.convertType(paymentResponse.getProductType()))
                .setProviderName(FortumoBillingProvider.NAME)
                .setOriginalJson("")
                .setToken(Long.toString(paymentResponse.getMessageId()))
                .setCanceled(paymentResponse.getBillingStatus() == MpUtils.MESSAGE_STATUS_FAILED);

        if (paymentResponse.getDate() != null) {
            purchaseBuilder.setPurchaseTime(paymentResponse.getDate().getTime());
        }

        return purchaseBuilder.build();
    }

    static void addPendingPayment(@NonNull Context context, String productId, String messageId) {
        ASPreferences preferences = new ASPreferences(context);
        preferences.put(FORTUMO_PENDING_PRODUCT + productId, messageId);
        ASLog.d(productId, " was added to pending");
    }

    public static void removePendingProduct(@NonNull Context context, String productId) {
        ASPreferences preferences = new ASPreferences(context);
        preferences.remove(FORTUMO_PENDING_PRODUCT + productId);
        ASLog.d(productId, " was removed from pending");
    }


}
