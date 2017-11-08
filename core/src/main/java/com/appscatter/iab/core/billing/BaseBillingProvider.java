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

package com.appscatter.iab.core.billing;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.ActivityResultRequest;
import com.appscatter.iab.core.model.event.android.ActivityResult;
import com.appscatter.iab.core.model.event.billing.BillingEventType;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;
import com.appscatter.iab.core.model.event.billing.Status;
import com.appscatter.iab.core.sku.SkuResolver;
import com.appscatter.iab.core.util.ActivityForResultLauncher;
import com.appscatter.iab.core.util.BillingUtils;
import com.appscatter.iab.core.util.SyncedReference;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.utils.ASLog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.appscatter.iab.core.model.event.billing.Status.BILLING_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.ITEM_UNAVAILABLE;

/**
 * Base implementation of {@link BillingProvider}.
 * <p>
 * Most implementations should extend this one unless implementation from scratch is absolutely necessary.
 *
 * @param <R> {@link SkuResolver} subclass to use with this BillingProvider.
 * @param <V> {@link PurchaseVerifier} subclass to use with this BillingProvider.
 */
public abstract class BaseBillingProvider<R extends SkuResolver, V extends PurchaseVerifier>
        implements BillingProvider {

    protected static final int DEFAULT_REQUEST_CODE = 4232;


    @NonNull
    protected Context context;
    @NonNull
    protected R skuResolver;
    @NonNull
    protected V purchaseVerifier;

    protected BaseBillingProvider(@NonNull final Context context,
            @NonNull final R skuResolver,
            @NonNull final V purchaseVerifier) {
        init(context, skuResolver, purchaseVerifier);
    }

    public BaseBillingProvider() {

    }
    /**
     * Loads details for specified SKUs.
     * <p>
     * At this point all SKUs should be resolved with provided {@link SkuResolver}.
     */
    protected abstract void skuDetails(@NonNull final SkuDetailsRequest request);

    /**
     * Loads user's inventory.
     */
    protected abstract void inventory(@NonNull final InventoryRequest request);

    /**
     * Purchase specified SKU.
     * <p>
     * At this point sku should be already resolved with supplied {@link SkuResolver}.
     */
    protected abstract void purchase(@NonNull final PurchaseRequest request);

    /**
     * Consumes specified Purchase.
     * <p>
     * SKU available from {@link Purchase#getSku()} should be already resolved with supplied
     * {@link SkuResolver}.
     */
    protected abstract void consume(@NonNull final ConsumeRequest request);

    @Nullable
    protected ActivityResult requestActivityResult(
            @NonNull final BillingRequest billingRequest,
            @NonNull final ActivityForResultLauncher launcher) {
        final SyncedReference<ActivityResult> syncResult = new SyncedReference<>();
        ASIab.post(new ActivityResultRequest(billingRequest, launcher, syncResult));
        ASLog.d("Waiting for ActivityResult");
        return syncResult.get();
    }

    /**
     * Entry point for all incoming billing requests.
     * <p>
     * Might be a good place for intercepting request.
     *
     * @param billingRequest incoming BillingRequest object.
     */
//    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        final BillingRequest resolvedRequest = BillingUtils.resolve(skuResolver, billingRequest);
        switch (resolvedRequest.getType()) {
            case BillingEventType.CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) resolvedRequest;
                final Purchase purchase = consumeRequest.getPurchase();
                final String purchaseProviderName = purchase.getProviderName();
                final String providerName = getName();
                if (!providerName.equals(purchaseProviderName)) {
                    ASLog.e("Attempt to consume purchase from wrong provider: %s.\n"
                            + "Current provider: %s", purchaseProviderName, providerName);
                    postEmptyResponse(resolvedRequest, ITEM_UNAVAILABLE);
                    break;
                }
                consume(consumeRequest);
                break;
            case BillingEventType.PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) resolvedRequest;
                purchase(purchaseRequest);
                break;
            case BillingEventType.SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) resolvedRequest;
                skuDetails(skuDetailsRequest);
                break;
            case BillingEventType.INVENTORY:
                final InventoryRequest inventoryRequest = (InventoryRequest) resolvedRequest;
                inventory(inventoryRequest);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBillingRequest(@NonNull final BillingRequest billingRequest) {
        if (!isAvailable()) {
            postEmptyResponse(billingRequest, BILLING_UNAVAILABLE);
        } else {
            handleRequest(billingRequest);
        }
    }


    protected BillingResponse verify(@NonNull final BillingResponse response) {
        return BillingUtils.verify(purchaseVerifier, response);
    }

    protected BillingResponse revertSku(@NonNull final BillingResponse response) {
        return BillingUtils.revert(skuResolver, response);
    }

    /**
     * Notifies library about billing response from this billing provider.
     *
     * @param billingResponse BillingResponse object to send to library.
     */
    protected void postResponse(@NonNull final BillingResponse billingResponse) {
        final BillingResponse verifiedResponse = verify(billingResponse);
        final BillingResponse revertedResponse = revertSku(verifiedResponse);
        ASIab.post(revertedResponse);
    }

    /**
     * Constructs and sends empty {@link BillingResponse}.
     *
     * @param billingRequest BillingRequest object to construct corresponding response to.
     * @param status         Status object to use in BillingResponse.
     */
    protected void postEmptyResponse(@NonNull final BillingRequest billingRequest,
            @NonNull @Status final int status) {
        postResponse(BillingUtils.emptyResponse(getName(), billingRequest, status));
    }

    @Override
    public boolean skuTypeSupported(@SkuType final int skuType) {
        return true;
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Nullable
    @Override
    public Intent getStorePageIntent() {
        return null;
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Nullable
    @Override
    public Intent getRateIntent() {
        return null;
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "TypeMayBeWeakened", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        if (!toString().equals(that.toString())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    //CHECKSTYLE:ON
}
