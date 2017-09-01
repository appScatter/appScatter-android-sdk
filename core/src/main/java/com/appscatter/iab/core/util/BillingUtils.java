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

package com.appscatter.iab.core.util;

import com.appscatter.iab.core.ActivityMonitor;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.event.billing.BillingEventType;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.ConsumeResponse;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsResponse;
import com.appscatter.iab.core.model.event.billing.Status;
import com.appscatter.iab.core.sku.SkuResolver;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.core.verification.VerificationResult;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BillingUtils {

    public static boolean isStale(@NonNull final BillingRequest request) {
        final Reference<Activity> reference = request.getActivity();
        final Activity activity = reference == null ? null : reference.get();
        return reference != null && (activity == null || !ActivityMonitor.isStarted(activity));
    }

    /**
     * Constructs empty response corresponding to supplied request.
     *
     * @param providerName   Name of the provider handling request, can be null.
     * @param billingRequest Request to make response for.
     * @param status         Status for newly constructed response.
     *
     * @return Newly constructed BillingResponse with no data.
     */
//    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    @NonNull
    public static BillingResponse emptyResponse(@Nullable final String providerName,
            @NonNull final BillingRequest billingRequest,
            @NonNull @Status final int status) {
        final BillingResponse billingResponse;
        switch (billingRequest.getType()) {
            case BillingEventType.CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) billingRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                billingResponse = new ConsumeResponse(status, providerName, purchase);
                break;
            case BillingEventType.PURCHASE:
                billingResponse = new PurchaseResponse(status, providerName);
                break;
            case BillingEventType.SKU_DETAILS:
                billingResponse = new SkuDetailsResponse(status, providerName);
                break;
            case BillingEventType.INVENTORY:
                billingResponse = new InventoryResponse(status, providerName);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return billingResponse;
    }

    @Nullable
    public static Activity getActivity(@NonNull final BillingRequest request) {
        final Reference<Activity> reference = request.getActivity();
        return reference == null ? null : reference.get();
    }

    @NonNull
    public static BillingResponse verify(@NonNull final PurchaseVerifier verifier,
            @NonNull final BillingResponse response) {
        @Status final int status = response.getStatus();
        @BillingEventType final int type = response.getType();
        final String name = response.getProviderName();
        if (type == BillingEventType.PURCHASE) {
            final Purchase purchase = ((PurchaseResponse) response).getPurchase();
            if (purchase != null) {
                return new PurchaseResponse(status, name, purchase, verifier.verify(purchase));
            }
        } else if (type == BillingEventType.INVENTORY) {
            final InventoryResponse inventoryResponse = (InventoryResponse) response;
            final Map<Purchase, Integer> inventory = inventoryResponse.getInventory();
            final Collection<Purchase> purchases = inventory.keySet();
            if (!purchases.isEmpty()) {
                final boolean hasMore = inventoryResponse.hasMore();
                return new InventoryResponse(status, name, verify(verifier, purchases), hasMore);
            }
        }
        return response;
    }

    @NonNull
    public static Map<Purchase, Integer> verify(
            @NonNull final PurchaseVerifier verifier,
            @NonNull final Iterable<Purchase> purchases) {
        final Map<Purchase, Integer> verifiedPurchases = new HashMap<>();
        for (final Purchase purchase : purchases) {
            verifiedPurchases.put(purchase, verifier.verify(purchase));
        }
        return verifiedPurchases;
    }

    @NonNull
    public static SkuDetails substituteSku(@NonNull final SkuDetails skuDetails,
            @NonNull final String sku) {
        if (TextUtils.equals(skuDetails.getSku(), sku)) {
            return skuDetails;
        }
        return skuDetails.copyWithSku(sku);
    }

    @NonNull
    public static Purchase substituteSku(@NonNull final Purchase purchase,
            @NonNull final String sku) {
        if (TextUtils.equals(purchase.getSku(), sku)) {
            return purchase;
        }
        return purchase.copyWithSku(sku);
    }

    @NonNull
    public static BillingRequest resolve(@NonNull final SkuResolver resolver,
            @NonNull final BillingRequest request) {
        @BillingEventType final int type = request.getType();
        final Activity activity = getActivity(request);
        final boolean handlesResult = request.isActivityHandlesResult();
        if (type == BillingEventType.PURCHASE) {
            final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
            final String sku = purchaseRequest.getSku();
            final String newSku = resolver.resolve(sku);
            return new PurchaseRequest(activity, handlesResult, newSku);
        } else if (type == BillingEventType.CONSUME) {
            final ConsumeRequest consumeRequest = (ConsumeRequest) request;
            final Purchase purchase = consumeRequest.getPurchase();
            final String sku = purchase.getSku();
            final String newSku = resolver.resolve(sku);
            final Purchase newPurchase = substituteSku(purchase, newSku);
            return new ConsumeRequest(activity, handlesResult, newPurchase);
        } else if (type == BillingEventType.SKU_DETAILS) {
            final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
            final Collection<String> skus = skuDetailsRequest.getSkus();
            final Set<String> newSkus = resolve(resolver, skus);
            return new SkuDetailsRequest(activity, handlesResult, newSkus);
        }
        return request;
    }

    @NonNull
    public static BillingResponse revert(@NonNull final SkuResolver resolver,
            @NonNull final BillingResponse response) {
        @Status final int status = response.getStatus();
        @BillingEventType final int type = response.getType();
        final String name = response.getProviderName();
        if (type == BillingEventType.PURCHASE) {
            final PurchaseResponse purchaseResponse = (PurchaseResponse) response;
            final Purchase purchase = purchaseResponse.getPurchase();
            @VerificationResult final int verification = purchaseResponse.getVerificationResult();
            if (purchase != null) {
                final Purchase newPurchase = revert(resolver, purchase);
                return new PurchaseResponse(status, name, newPurchase, verification);
            }
        } else if (type == BillingEventType.CONSUME) {
            final ConsumeResponse consumeResponse = (ConsumeResponse) response;
            final Purchase newPurchase = revert(resolver, consumeResponse.getPurchase());
            return new ConsumeResponse(status, name, newPurchase);
        } else if (type == BillingEventType.SKU_DETAILS) {
            final SkuDetailsResponse skuDetailsResponse = (SkuDetailsResponse) response;
            final Collection<SkuDetails> skusDetails = skuDetailsResponse.getSkusDetails();
            return new SkuDetailsResponse(status, name, revert(resolver, skusDetails));
        } else if (type == BillingEventType.INVENTORY) {
            final InventoryResponse inventoryResponse = (InventoryResponse) response;
            final Map<Purchase, Integer> inventory = inventoryResponse.getInventory();
            final boolean hasMore = inventoryResponse.hasMore();
            return new InventoryResponse(status, name, revert(resolver, inventory), hasMore);
        }
        return response;
    }

    @NonNull
    public static SkuDetails resolve(@NonNull final SkuResolver skuResolver,
            @NonNull final SkuDetails skuDetails) {
        final String resolvedSku = skuResolver.resolve(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    @NonNull
    public static Purchase resolve(@NonNull final SkuResolver skuResolver,
            @NonNull final Purchase purchase) {
        final String resolvedSku = skuResolver.resolve(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }

    @NonNull
    public static Set<String> resolve(@NonNull final SkuResolver resolver,
            @NonNull final Iterable<String> skus) {
        final Set<String> resolvedSkus = new HashSet<>();
        for (final String sku : skus) {
            resolvedSkus.add(resolver.resolve(sku));
        }
        return resolvedSkus;
    }

    @NonNull
    public static SkuDetails revert(@NonNull final SkuResolver skuResolver,
            @NonNull final SkuDetails skuDetails) {
        final String resolvedSku = skuResolver.revert(skuDetails.getSku());
        return substituteSku(skuDetails, resolvedSku);
    }

    @NonNull
    public static Purchase revert(@NonNull final SkuResolver skuResolver,
            @NonNull final Purchase purchase) {
        final String resolvedSku = skuResolver.revert(purchase.getSku());
        return substituteSku(purchase, resolvedSku);
    }

    @NonNull
    public static Collection<SkuDetails> revert(@NonNull final SkuResolver resolver,
            @NonNull final Iterable<SkuDetails> skusDetails) {
        final Collection<SkuDetails> newSkusDetails = new ArrayList<>();
        for (final SkuDetails skuDetails : skusDetails) {
            newSkusDetails.add(revert(resolver, skuDetails));
        }
        return newSkusDetails;
    }


    @NonNull
    public static Map<Purchase, Integer> revert(
            @NonNull final SkuResolver resolver,
            @NonNull final Map<Purchase, Integer> inventory) {
        if (inventory.isEmpty()) {
            return inventory;
        }
        final Map<Purchase, Integer> newInventory = new HashMap<>();
        for (final Map.Entry<Purchase, Integer> entry : inventory.entrySet()) {
            newInventory.put(revert(resolver, entry.getKey()), entry.getValue());
        }
        return newInventory;
    }

    private BillingUtils() {
        throw new UnsupportedOperationException();
    }
}
