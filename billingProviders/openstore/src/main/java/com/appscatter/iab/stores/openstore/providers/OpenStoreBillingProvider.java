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

package com.appscatter.iab.stores.openstore.providers;

import com.appscatter.iab.core.billing.BaseBillingProvider;
import com.appscatter.iab.core.billing.BaseBillingProviderBuilder;
import com.appscatter.iab.core.model.billing.IapProductList;
import com.appscatter.iab.core.model.billing.IapProductDetail;
import com.appscatter.iab.core.model.billing.Purchase;
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
import com.appscatter.iab.stores.openstore.OpenStoreBillingHelper;
import com.appscatter.iab.stores.openstore.OpenStoreIntentMaker;
import com.appscatter.iab.stores.openstore.OpenStoreUtils;
import com.appscatter.iab.stores.openstore.Response;
import com.appscatter.iab.stores.openstore.model.ItemType;
import com.appscatter.iab.stores.openstore.model.OpenPurchase;
import com.appscatter.iab.stores.openstore.model.OpenSkuDetails;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASPreferences;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

@SuppressWarnings({"PMD.GodClass", "PMD.EmptyMethodInAbstractClassShouldBeAbstract", "PMD.NPathComplexity"})
public class OpenStoreBillingProvider extends BaseBillingProvider<TypedSkuResolver, PurchaseVerifier> {

    protected static final String DEFAULT_NAME = "OpenStore";
    protected static final String KEY_TOKEN = "continuation_token";

    @Nullable
    protected OpenStoreIntentMaker intentMaker;
    @NonNull
    protected final OpenStoreBillingHelper helper = getHelper();
    @Nullable
    private String name;

    protected OpenStoreBillingProvider(@NonNull final Context context,
            @NonNull final TypedSkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @Nullable final OpenStoreIntentMaker intentMaker) {
        super(context, skuResolver, purchaseVerifier);
        this.intentMaker = intentMaker;
    }


    public OpenStoreBillingProvider(){

    }

    @NonNull
    protected OpenStoreBillingHelper getHelper() {
        return new OpenStoreBillingHelper(context, intentMaker);
    }

    @NonNull
    protected final ASPreferences getPreferences() {
        // Store name can't be null. If store is unavailable this method shouldn't be used.
        return new ASPreferences(context, getName());
    }

    /**
     * Picks proper response status for supplied response.
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
                return Status.UNAUTHORISED;
            default:
                return Status.UNKNOWN_ERROR;
        }
    }

    @Override
    public String toString() {
        return intentMaker != null ? intentMaker.getProviderName() : DEFAULT_NAME;
    }

    @NonNull
    @Override
    public String getName() {
        if (name == null) {
            throw new IllegalStateException();
        }
        return name;
    }

    @Override
    public void checkManifest() {
        // Nothing to check
    }

    @Override
    public List<String> getPermissionsList() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        ASChecks.checkThread(false);
        final String appstoreName = helper.getAppstoreName();
        if (appstoreName == null) {
            return false;
        }
        this.name = appstoreName;
        return helper.isBillingAvailable();
    }

    @NonNull
    @Override
    @Compatibility
    public int checkCompatibility() {
        if (helper.isPackageInstaller()) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    @Override
    public boolean skuTypeSupported(@SkuType final int skuType) {
        ASChecks.checkThread(false);
        final ItemType itemType = ItemType.fromSkuType(skuType);
        return itemType != null && helper.isBillingSupported(itemType) == Response.OK;
    }

//    @Nullable
//    @Override
//    public Intent getStorePageIntent() {
//        ASChecks.checkThread(false);
//        return helper.getProductPageIntent();
//    }
//
//    @Nullable
//    @Override
//    public Intent getRateIntent() {
//        ASChecks.checkThread(false);
//        return helper.getProductPageIntent();
//    }

    @Override
    protected void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Map<ItemType, Collection<String>> typeSkuMap = new HashMap<>();
        final Set<String> skus = request.getSkus();
        for (final String sku : skus) {
            @SkuType final int skuType = skuResolver.resolveType(sku);
            final ItemType itemType = ItemType.fromSkuType(skuType);
            if (itemType == null) {
                ASLog.e("Unknown SKU type: " + sku);
                continue;
            }
            final Collection<String> typeSkus;
            if (!typeSkuMap.containsKey(itemType)) {
                typeSkus = new ArrayList<>();
                typeSkuMap.put(itemType, typeSkus);
            } else {
                typeSkus = typeSkuMap.get(itemType);
            }
            typeSkus.add(sku);
        }
        final Bundle result = helper.getSkuDetails(typeSkuMap);
        @Response final int response = OpenStoreUtils.getResponse(result);
        if (response != Response.OK) {
            postEmptyResponse(request, getStatus(response));
            return;
        }
        final Collection<OpenSkuDetails> openSkusDetails = OpenStoreUtils.getSkusDetails(result);
        if (openSkusDetails == null) {
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final Collection<SkuDetails> skusDetails = new ArrayList<>();
        final Collection<String> unresolvedSkus = new HashSet<>(skus);
        for (final OpenSkuDetails openSkuDetails : openSkusDetails) {
            final String sku = openSkuDetails.getProductId();
            @SkuType final int skuType = skuResolver.resolveType(sku);
            skusDetails.add(OpenStoreUtils.convertSkuDetails(openSkuDetails, getName(), skuType));
            unresolvedSkus.remove(sku);
        }
        for (final String unresolvedSku : unresolvedSkus) {
            skusDetails.add(new SkuDetails(unresolvedSku));
        }
        postResponse(new SkuDetailsResponse(Status.SUCCESS, getName(), skusDetails));
    }

    private String getTokenKey(@NonNull final ItemType itemType) {
        return KEY_TOKEN + "." + itemType.toString();
    }

    @Override
    protected void inventory(@NonNull final InventoryRequest request) {
        final boolean startOver = request.startOver();
        final ASPreferences preferences = getPreferences();
        final Map<ItemType, Bundle> resultMap = new HashMap<>();
        for (@SkuType int skuType = 0; skuType < SkuType.UNAVAILABLE; skuType++) {
            final ItemType itemType = ItemType.fromSkuType(skuType);
            if (itemType == null || resultMap.containsKey(itemType)
                    || !skuTypeSupported(skuType)) {
                continue;
            }
            final String token = startOver ? null : preferences.getString(getTokenKey(itemType));
            final Bundle result = helper.getPurchases(itemType, token);
            @Response final int response = OpenStoreUtils.getResponse(result);
            if (response != Response.OK) {
                postEmptyResponse(request, getStatus(response));
                return;
            }
            resultMap.put(itemType, result);
        }

        final Collection<Purchase> inventory = new ArrayList<>();
        boolean hasMore = false;
        for (final Map.Entry<ItemType, Bundle> entry : resultMap.entrySet()) {
            final Bundle result = entry.getValue();
            final Collection<OpenPurchase> purchases = OpenStoreUtils.getPurchases(result);
            final Collection<String> signatures = OpenStoreUtils.getSignaturesList(result);
            if (purchases == null || signatures == null) {
                ASLog.e("Invalid inventory data. Purchases: %s. Signatures: %s."
                        , purchases, signatures);
                postEmptyResponse(request, Status.UNKNOWN_ERROR);
                return;
            }
            final Iterator<OpenPurchase> purchaseIterator = purchases.iterator();
            final Iterator<String> signatureIterator = signatures.iterator();
            while (purchaseIterator.hasNext()) {
                final OpenPurchase purchase = purchaseIterator.next();
                final String signature = signatureIterator.next();
                @SkuType final int skuType = skuResolver.resolveType(purchase.getProductId());
                inventory.add(
                        OpenStoreUtils.convertPurchase(purchase, getName(), skuType, signature));
            }

            final String continuationToken = OpenStoreUtils.getContinuationToken(result);
            final String tokenKey = getTokenKey(entry.getKey());
            if (TextUtils.isEmpty(continuationToken)) {
                preferences.remove(tokenKey);
            } else {
                preferences.put(tokenKey, continuationToken);
                hasMore = true;
            }
        }
        postResponse(new InventoryResponse(Status.SUCCESS, getName(), inventory, hasMore));
    }

    @Override
    protected void consume(@NonNull final ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        final String token = purchase.getToken();
        //noinspection ConstantConditions
        @Response final int response = helper.consumePurchase(token);
        if (response != Response.OK) {
            postEmptyResponse(request, getStatus(response));
            return;
        }

        postResponse(new ConsumeResponse(Status.SUCCESS, getName(), purchase));
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {
        final String sku = request.getSku();
        @SkuType final int skuType = skuResolver.resolveType(sku);
        //noinspection ConstantConditions
        final ItemType itemType = ItemType.fromSkuType(skuType);
        if (itemType == null) {
            ASLog.e("Unknown SKU type: " + sku);
            postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
            return;
        }
        final Bundle intentBundle = helper.getBuyIntent(sku, itemType);
        @Response final int intentResponse = OpenStoreUtils.getResponse(intentBundle);
        if (intentResponse != Response.OK) {
            postEmptyResponse(request, getStatus(intentResponse));
            return;
        }

        final PendingIntent intent = OpenStoreUtils.getPurchaseIntent(intentBundle);
        if (intent == null) {
            ASLog.e("No purchase intent.");
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }

        final ActivityResult activityResult = requestActivityResult(request,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        activity.startIntentSenderForResult(intent.getIntentSender(),
                                DEFAULT_REQUEST_CODE, new Intent(), 0, 0, 0);
                    }
                });
        if (activityResult == null) {
            ASLog.e("No activity result.");
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final int resultCode = activityResult.getResultCode();
        final Intent data;
        if (resultCode != RESULT_OK || (data = activityResult.getData()) == null) {
            @Status final int status = resultCode == RESULT_CANCELED
                    ? Status.USER_CANCELED : Status.UNKNOWN_ERROR;
            postEmptyResponse(request, status);
            return;
        }

        final Bundle result = data.getExtras();
        @Response final int response = OpenStoreUtils.getResponse(result);
        if (response != Response.OK) {
            postEmptyResponse(request, getStatus(response));
            return;
        }
        final OpenPurchase openPurchase = OpenStoreUtils.getPurchase(result);
        if (openPurchase == null) {
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }
        final String signature = OpenStoreUtils.getSignature(result);
        final Purchase purchase = OpenStoreUtils
                .convertPurchase(openPurchase, getName(), skuType, signature);
        postResponse(new PurchaseResponse(Status.SUCCESS, getName(), purchase));
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
    public PurchaseVerifier getPurchaseVerifier(String key) {
        return key != null ? new SimplePublicKeyPurchaseVerifier(key) : null;
    }

    @NonNull
    @Override
    public SkuResolver getSkuResolver(@NonNull IapProductList products) {
        final TypedMapSkuResolver skuResolver = new TypedMapSkuResolver();

        for(IapProductDetail product: products.getProductDetails()) {
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
    }

    protected abstract static class OpenStoreBuilder<B extends OpenStoreBuilder>
            extends BaseBillingProviderBuilder<B, TypedSkuResolver, PurchaseVerifier> {

        @Nullable
        protected OpenStoreIntentMaker intentMaker;

        public OpenStoreBuilder(@NonNull final Context context) {
            super(context);
        }

        protected OpenStoreBuilder setIntentMaker(
                @NonNull final OpenStoreIntentMaker intentMaker) {
            this.intentMaker = intentMaker;
            return this;
        }
    }

    public static class Builder extends OpenStoreBuilder<Builder> {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public OpenStoreBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException();
            }
            return new OpenStoreBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier,
                    intentMaker);
        }
    }
}
