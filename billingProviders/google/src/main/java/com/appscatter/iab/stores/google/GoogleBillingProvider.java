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

package com.appscatter.iab.stores.google;

import com.appscatter.iab.core.billing.BaseBillingProvider;
import com.appscatter.iab.core.billing.BaseBillingProviderBuilder;
import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.billing.IapProductList;
import com.appscatter.iab.core.model.billing.IapProductDetail;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SignedPurchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.android.ActivityResult;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.ConsumeResponse;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsResponse;
import com.appscatter.iab.core.model.event.billing.Status;
import com.appscatter.iab.core.sku.SkuResolver;
import com.appscatter.iab.core.sku.TypedMapSkuResolver;
import com.appscatter.iab.core.sku.TypedSkuResolver;
import com.appscatter.iab.core.util.ActivityForResultLauncher;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.core.verification.SimplePublicKeyPurchaseVerifier;
import com.appscatter.iab.core.billing.Compatibility;
import com.appscatter.iab.stores.google.model.GooglePurchase;
import com.appscatter.iab.stores.google.model.GoogleSkuDetails;
import com.appscatter.iab.stores.google.model.ItemType;
import com.appscatter.iab.stores.google.model.PurchaseState;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;

import org.json.JSONException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This {@link BillingProvider} implementation adds support for
 * <a href="https://play.google.com/store">Google Play</a> App Store.
 */
@SuppressWarnings("PMD.GodClass")
public class GoogleBillingProvider extends BaseBillingProvider<TypedSkuResolver, PurchaseVerifier> {

    public static final String NAME = "Google";
    protected static final String PACKAGE = "com.android.vending";
    protected static final String INSTALLER = PACKAGE;
    protected static final String PERMISSION_BILLING = "com.android.vending.BILLING";

    protected final boolean debugMode;
    /**
     * Helper object to delegate all Google specific calls to.
     */
    @NonNull
    protected GoogleBillingHelper helper;

    protected GoogleBillingProvider(
            @NonNull final Context context,
            @NonNull final TypedSkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier,
            final boolean debugMode) {
        super(context, skuResolver, purchaseVerifier);
        this.debugMode = debugMode;
        this.helper = new GoogleBillingHelper(context);
    }

    @SuppressWarnings("unused")
    public GoogleBillingProvider() {
        super();
        this.debugMode = true;
    }

    /**
     * Resolves proper SKU type from supplied Google product type.
     *
     * @param sku      SKU to resolve type for.
     * @param itemType Supplied SKU type.
     * @return Resolved SKU type, cannot be null.
     */
    @SkuType
    protected int skuType(@NonNull final String sku, @NonNull final ItemType itemType) {
        switch (itemType) {
            case CONSUMABLE_OR_ENTITLEMENT:
                return skuResolver.resolveType(sku);
            case SUBSCRIPTION:
                return SkuType.SUBSCRIPTION;
            default:
                return SkuType.UNKNOWN;
        }
    }

    /**
     * Transforms Google product details into the library SKU details model.
     *
     * @param googleSkuDetails Google product details to transform.
     * @return Newly constructed SKU details object, can't be null.
     */
    @NonNull
    protected SkuDetails newSkuDetails(@NonNull final GoogleSkuDetails googleSkuDetails) {
        final String sku = googleSkuDetails.getProductId();
        final ItemType itemType = googleSkuDetails.getItemType();
        @SkuType final int skuType = skuType(sku, itemType);
        return new SkuDetails.Builder(sku)
                .setType(skuType)
                .setProviderName(getName())
                .setOriginalJson(googleSkuDetails.getOriginalJson())
                .setPrice(googleSkuDetails.getPrice())
                .setTitle(googleSkuDetails.getTitle())
                .setDescription(googleSkuDetails.getDescription())
                .build();
    }

    /**
     * Transforms Google purchase to library specific model.
     *
     * @param googlePurchase Google purchase to transform.
     * @return Newly constructed purchase object, can't be null.
     */
    @NonNull
    protected Purchase newPurchase(@NonNull final GooglePurchase googlePurchase,
            @Nullable final String signature) {
        final String sku = googlePurchase.getProductId();
        @SkuType final int skuType = skuResolver.resolveType(sku);
        return new SignedPurchase.Builder(sku)
                .setType(skuType)
                .setProviderName(getName())
                .setOriginalJson(googlePurchase.getOriginalJson())
                .setToken(googlePurchase.getPurchaseToken())
                .setPurchaseTime(googlePurchase.getPurchaseTime())
                .setCanceled(googlePurchase.getPurchaseState() == PurchaseState.CANCELED)
                .setSignature(signature)
                .build();
    }

    @NonNull
    protected GooglePurchase newPurchase(@NonNull final String purchaseData) throws JSONException {
        final GooglePurchase googlePurchase = new GooglePurchase(purchaseData);
        if (!debugMode && googlePurchase.getOrderId() == null) {
            throw new JSONException("orderId value is null.");
        }
        return googlePurchase;
    }

    /**
     * Picks proper response status for supplied Google response.
     *
     * @param response Response to pick status for.
     * @return Billing response status most fitting supplied response. Can't be null.
     */
    @NonNull
    @Status
    protected int getStatus(@Response final int response) {
        if (response == Response.UNKNOWN) {
            return Status.UNKNOWN_ERROR;
        }
        switch (response) {
            case Response.OK:
                return Status.SUCCESS;
            case Response.USER_CANCELED:
                return Status.USER_CANCELED;
            case Response.SERVICE_UNAVAILABLE:
                return Status.SERVICE_UNAVAILABLE;
            case Response.ITEM_UNAVAILABLE:
                return Status.ITEM_UNAVAILABLE;
            case Response.ITEM_ALREADY_OWNED:
                return Status.ITEM_ALREADY_OWNED;
            case Response.BILLING_UNAVAILABLE:
                return GoogleUtils.hasGoogleAccount(context)
                        ? Status.BILLING_UNAVAILABLE
                        : Status.UNAUTHORISED;
            default:
                return Status.UNKNOWN_ERROR;
        }
    }

    @Override
    public void checkManifest() {
        ASChecks.checkPermission(context, PERMISSION_BILLING);
    }

    @Override
    public List<String> getPermissionsList() {
        return null;
    }

    @Override
    public void registerForEvents() {
        //No events to handle
    }

    @Override
    public void unregisterForEvents() {
        //No events to handle
    }

    @Override
    public PurchaseVerifier getPurchaseVerifier(@NonNull String key) {
        return new SimplePublicKeyPurchaseVerifier(key);
    }

    @NonNull
    @Override
    public SkuResolver getSkuResolver(@NonNull IapProductList products) {

        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();
        for(IapProductDetail product:products.getProductDetails()) {
            skuResolver.add(product.getGlobalProductId(), product.getProductId(), product.getProductType());
        }

        return skuResolver;
    }

    @Override
    public <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull C context,
            @NonNull R skuResolver, @NonNull V purchaseVerifier) {
        this.context = (Context)context;
        this.skuResolver = (TypedSkuResolver)skuResolver;
        this.purchaseVerifier = purchaseVerifier;
        this.helper = new GoogleBillingHelper(this.context);
    }

    @Override
    public boolean isAvailable() {
        final boolean installed = ASUtils.isInstalled(context, PACKAGE);
        ASLog.d("Google package installed: %b", installed);
        return installed;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @NonNull
    @Override
    @Compatibility
    public int checkCompatibility() {
        @Response final int response = helper.isBillingSupported();
        ASLog.d("Check if Google billing supported: %s", response);
        @Status final int status = getStatus(response);
        if (!Arrays.asList(Status.SUCCESS, Status.UNAUTHORISED).contains(status)) {
            return Compatibility.INCOMPATIBLE;
        }
        return INSTALLER.equals(ASUtils.getPackageInstaller(context))
                ? Compatibility.PREFERRED : Compatibility.COMPATIBLE;
    }

    @Override
    protected void consume(@NonNull final ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        final String token = purchase.getToken();
        if (TextUtils.isEmpty(token)) {
            ASLog.e("Purchase toke in empty.");
            postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
            return;
        }

        @Response final int response = helper.consumePurchase(token);
        if (response != Response.OK) {
            ASLog.e("Consume failed.");
            postEmptyResponse(request, getStatus(response));
            return;
        }

        postResponse(new ConsumeResponse(Status.SUCCESS, getName(), purchase));
    }

    @Override
    protected void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Set<String> skus = request.getSkus();
        final Bundle result = helper.getSkuDetails(skus);
        @Response final int response = GoogleUtils.getResponse(result);
        //noinspection ConstantConditions
        if (response != Response.OK || result == null) {
            ASLog.e("Failed to retrieve sku details.");
            postEmptyResponse(request, getStatus(response));
            return;
        }

        final Collection<String> jsonSkuDetails = GoogleUtils.getSkuDetails(result);
        if (jsonSkuDetails == null) {
            ASLog.d("No sku details data: %s", result);
            postEmptyResponse(request, Status.SUCCESS);
            return;
        }

        // Some details might not have been loaded
        final Collection<SkuDetails> skusDetails = new ArrayList<>();
        final Collection<String> unresolvedSkus = new LinkedList<>(skus);
        for (final String jsonSku : jsonSkuDetails) {
            try {
                final GoogleSkuDetails googleSkuDetails = new GoogleSkuDetails(jsonSku);
                final SkuDetails skuDetails = newSkuDetails(googleSkuDetails);
                unresolvedSkus.remove(skuDetails.getSku());
                skusDetails.add(skuDetails);
            } catch (JSONException exception) {
                ASLog.e("Failed to parse sku details: " + skusDetails, exception);
            }
        }
        for (final String sku : unresolvedSkus) {
            ASLog.e("No details for SKU: " + sku);
            skusDetails.add(new SkuDetails(sku));
        }
        postResponse(new SkuDetailsResponse(Status.SUCCESS, getName(), skusDetails));
    }

    @Override
    protected void inventory(@NonNull final InventoryRequest request) {
        final boolean startOver = request.startOver();
        final Bundle result = helper.getPurchases(startOver);
        @Response final int response = GoogleUtils.getResponse(result);
        // noinspection ConstantConditions
        if (response != Response.OK || result == null) {
            ASLog.e("Failed to retrieve purchase data.");
            postEmptyResponse(request, getStatus(response));
            return;
        }

        final Collection<String> itemList = GoogleUtils.getItemList(result);
        final List<String> dataList = GoogleUtils.getDataList(result);
        final List<String> signatureList = GoogleUtils.getSignatureList(result);
        if (itemList == null || dataList == null || signatureList == null) {
            ASLog.d("No purchases data: %s", response);
            postEmptyResponse(request, Status.SUCCESS);
            return;
        }

        final int size = dataList.size();
        if (itemList.size() < size || signatureList.size() < size) {
            ASLog.e("Failed to parse purchase data response.");
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }

        final Collection<Purchase> inventory = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final String data = dataList.get(i);
            try {
                final GooglePurchase googlePurchase = newPurchase(data);
                final String signature = signatureList.get(i);
                final Purchase purchase = newPurchase(googlePurchase, signature);
                inventory.add(purchase);
            } catch (JSONException exception) {
                ASLog.e("Failed to parse purchase data.", exception);
            }
        }
        final String token = GoogleUtils.getContinuationToken(result);
        final boolean hasMore = !TextUtils.isEmpty(token);
        postResponse(new InventoryResponse(Status.SUCCESS, getName(), inventory, hasMore));
    }

    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {
        final String sku = request.getSku();
        @SkuType final int skuType = skuResolver.resolveType(sku);
        final ItemType itemType = ItemType.fromSkuType(skuType);
        // Google can't process purchase with unknown type
        if (itemType == null) {
            ASLog.e("Unknown sku type: %s", sku);
            postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
            return;
        }

        final Bundle result = helper.getBuyIntent(sku, itemType);
        @Response final int response = GoogleUtils.getResponse(result);
        final PendingIntent intent = GoogleUtils.getBuyIntent(result);
        if (response != Response.OK || intent == null) {
            ASLog.e("Failed to retrieve buy intent.");
            postEmptyResponse(request, getStatus(response));
            return;
        }

        final ActivityResult activityResult = requestActivityResult(request,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        final IntentSender sender = intent.getIntentSender();
                        activity.startIntentSenderForResult(sender, DEFAULT_REQUEST_CODE,
                                new Intent(), 0, 0, 0);
                    }
                });
        if (activityResult == null) {
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final Intent data = activityResult.getData();
        final int resultCode = activityResult.getResultCode();
        @Response final int purchaseResponse = GoogleUtils.getResponse(data);
        final String purchaseData = GoogleUtils.getPurchaseData(data);
        final String signature = GoogleUtils.getSignature(data);
        if (resultCode != Activity.RESULT_OK || purchaseResponse != Response.OK
                || purchaseData == null || signature == null) {
            ASLog.e("Failed to handle activity result. Code:%s, Data:%s",
                    resultCode, ASUtils.toString(data));
            postEmptyResponse(request, getStatus(purchaseResponse));
            return;
        }

        final GooglePurchase googlePurchase;
        try {
            googlePurchase = newPurchase(purchaseData);
        } catch (JSONException exception) {
            ASLog.e("Failed to parse purchase data: " + purchaseData, exception);
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }

        final Purchase purchase = newPurchase(googlePurchase, signature);
        postResponse(new PurchaseResponse(Status.SUCCESS, getName(), purchase));
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, TypedSkuResolver,
            PurchaseVerifier> {

        private boolean debugMode;

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @NonNull
        public Builder setDebugMode(final boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        @Override
        public GoogleBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("TypedSkuResolver must be set.");
            }
            return new GoogleBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier,
                    debugMode);
        }
    }
}
