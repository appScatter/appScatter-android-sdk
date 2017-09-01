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

package com.appscatter.iab.stores.samsung;

import com.appscatter.iab.core.billing.BaseBillingProvider;
import com.appscatter.iab.core.billing.BaseBillingProviderBuilder;
import com.appscatter.iab.core.model.billing.IapProductList;
import com.appscatter.iab.core.model.billing.IapProductDetail;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.android.ActivityResult;
import com.appscatter.iab.core.model.event.billing.BillingEventType;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
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
import com.appscatter.iab.core.util.ActivityForResultLauncher;
import com.appscatter.iab.core.util.SyncedReference;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.core.verification.VerificationResult;
import com.appscatter.iab.core.billing.Compatibility;
import com.appscatter.iab.stores.samsung.model.SamsungPurchase;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASPreferences;
import com.appscatter.iab.utils.ASUtils;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.INTERNET;
import static com.appscatter.iab.core.model.event.billing.Status.SERVICE_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.SUCCESS;
import static com.appscatter.iab.core.model.event.billing.Status.UNAUTHORISED;
import static com.appscatter.iab.core.model.event.billing.Status.UNKNOWN_ERROR;
import static com.appscatter.iab.core.model.event.billing.Status.USER_CANCELED;
import static com.appscatter.iab.core.verification.PurchaseVerifier.DEFAULT;
import static com.appscatter.iab.core.verification.VerificationResult.ERROR;

@SuppressWarnings({"PMD.NPathComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.GodClass", "PMD.StdCyclomaticComplexity"})
public class SamsungBillingProvider extends BaseBillingProvider<SamsungSkuResolver, PurchaseVerifier> {

    public static final String NAME = "Samsung";
    protected static final String PACKAGE = "com.sec.android.app.samsungapps";
    protected static final String INSTALLER = PACKAGE;
    protected static final String SAMSUNG_BILLING = "com.sec.android.iap.permission.BILLING";

    protected static final long ACCOUNT_TIMEOUT = Long.parseLong("5000");
    protected static final int BATCH_SIZE = 15;
    protected static final String KEY_LAST_ITEM = NAME + ".last_item";

    protected final ASPreferences preferences = new ASPreferences(context);
    protected final ASPreferences consumablePurchases = new ASPreferences(context, NAME);

    protected
    @BillingMode
    final int billingMode;
    @NonNull
    protected SamsungBillingHelper helper;
    @Nullable
    protected SyncedReference<Boolean> syncAuthorisationResult;

    protected SamsungBillingProvider(@NonNull final Context context,
            @NonNull final SamsungSkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @BillingMode final int billingMode) {
        super(context, skuResolver, purchaseVerifier);
        this.billingMode = billingMode;
        this.helper = new SamsungBillingHelper(context, billingMode);
    }

    @SuppressWarnings("unused")
    public SamsungBillingProvider() {
        super();
        this.billingMode = BillingMode.PRODUCTION;
    }

    @Override
    public void checkManifest() {
        ASChecks.checkPermission(context, INTERNET);
        ASChecks.checkPermission(context, SAMSUNG_BILLING);
    }

    @Override
    public List<String> getPermissionsList() {
        return null;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isAvailable() {
        return ASUtils.isInstalled(context, PACKAGE);
    }

    @NonNull
    @Override
    @Compatibility
    public int checkCompatibility() {
        if (!ASUtils.isInstalled(context, PACKAGE)) {
            return Compatibility.INCOMPATIBLE;
        }
        if (INSTALLER.equals(ASUtils.getPackageInstaller(context))) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    @Override
    protected BillingResponse verify(@NonNull final BillingResponse response) {
        final BillingResponse verifiedResponse = super.verify(response);
        if (verifiedResponse.getStatus() != SUCCESS) {
            return verifiedResponse;
        }
        // Due to API limitations this BillingProvider doesn't return consumables in inventory
        // requests. However this there's a possibility of error during purchase verification
        // process user might not get a verified consumable purchase in onPurchase() callback.
        // To work around this issue we'll this kind of purchases in SharedPreferences.
        @BillingEventType final int type = verifiedResponse.getType();
        if (type == BillingEventType.PURCHASE) {
            final PurchaseResponse purchaseResponse = (PurchaseResponse) verifiedResponse;
            final Purchase purchase = purchaseResponse.getPurchase();
            if (purchaseResponse.getVerificationResult() == ERROR && purchase != null
                    && purchase.getType() == SkuType.CONSUMABLE) {
                final String token = purchase.getToken();
                final String originalJson = purchase.getOriginalJson();
                if (token != null && originalJson != null) {
                    consumablePurchases.put(token, originalJson);
                }
            }
        } else if (type == BillingEventType.INVENTORY) {
            final InventoryResponse inventoryResponse = (InventoryResponse) verifiedResponse;
            final Map<Purchase, Integer> inventory = inventoryResponse.getInventory();
            for (final Map.Entry<Purchase, Integer> entry : inventory.entrySet()) {
                @VerificationResult final int result = entry.getValue();
                final Purchase purchase = entry.getKey();
                final String token = purchase.getToken();
                if (token != null && result != ERROR && consumablePurchases.contains(token)) {
                    consumablePurchases.remove(token);
                }
            }
        }
        return verifiedResponse;
    }

    @Status
    protected int checkAuthorisation(@NonNull final BillingRequest billingRequest) {
        if (!SamsungUtils.hasSamsungAccount(context)) {
            return UNAUTHORISED;
        }
        final ActivityResult result = requestActivityResult(billingRequest,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        final Intent intent = SamsungUtils.getAccountIntent();
                        activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                    }
                });
        if (result != null && result.getResultCode() == Activity.RESULT_OK) {
            return Status.ERROR_NONE;
        }
        return ASUtils.isConnected(context) ? UNAUTHORISED : SERVICE_UNAVAILABLE;
    }

    @Override
    protected void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Bundle bundle = helper.getItemList(skuResolver.getGroupId());
        @Status final int error = SamsungUtils.handleError(context, bundle);
        if (error != Status.ERROR_NONE) {
            postEmptyResponse(request, error);
            return;
        }

        final Set<String> skus = request.getSkus();
        final Collection<SkuDetails> skusDetails = SamsungUtils.getSkusDetails(bundle, skus);
        @Status final int status = skusDetails == null ? UNKNOWN_ERROR : SUCCESS;
        postResponse(new SkuDetailsResponse(status, getName(), skusDetails));
    }

    @Override
    protected void inventory(@NonNull final InventoryRequest request) {
        @Status final int authStatus = checkAuthorisation(request);
        if (authStatus != Status.ERROR_NONE) {
            postEmptyResponse(request, authStatus);
            return;
        }

        final boolean startOver = request.startOver();
        final int start = startOver ? 1 : preferences.getInt(KEY_LAST_ITEM, 1);
        final int end = start + BATCH_SIZE - 1;
        final Bundle bundle = helper.getItemsInbox(skuResolver.getGroupId(), start, end);
        @Status final int error = SamsungUtils.handleError(context, bundle);
        if (error != Status.ERROR_NONE) {
            postEmptyResponse(request, error);
            return;
        }

        final Collection loadedItems = SamsungUtils.getItems(bundle);
        final int loadedCount = loadedItems == null ? 0 : loadedItems.size();
        if (loadedCount > 0) {
            preferences.put(KEY_LAST_ITEM, start + loadedCount);
        }

        //TODO check if consumables should be loaded
        final Collection<Purchase> purchases = SamsungUtils.getPurchasedItems(bundle, false);
        if (purchases != null) {
            // Add all consumables that might be stored in SharedPreferences.
            final Map<String, ?> all = consumablePurchases.getPreferences().getAll();
            for (final Map.Entry<String, ?> entry : all.entrySet()) {
                final String value = (String) entry.getValue();
                try {
                    final SamsungPurchase samsungPurchase = new SamsungPurchase(value);
                    final Purchase purchase = SamsungUtils
                            .convertPurchase(samsungPurchase, SkuType.CONSUMABLE);
                    purchases.add(purchase);
                } catch (JSONException exception) {
                    ASLog.e("", exception);
                    consumablePurchases.remove(entry.getKey());
                }
            }
        }
        @Status final int status = purchases == null ? UNKNOWN_ERROR : SUCCESS;
        final boolean hasMore = loadedCount == BATCH_SIZE;
        postResponse(new InventoryResponse(status, getName(), purchases, hasMore));
    }

    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {
        //TODO make sure init is not required
        //        final Status initError = SamsungUtils.handleError(context, helper.init());
        //        if (initError != null) {
        //            postEmptyResponse(request, initError);
        //            return;
        //        }

        @Status final int authStatus = checkAuthorisation(request);
        if (authStatus != Status.ERROR_NONE) {
            postEmptyResponse(request, authStatus);
            return;
        }

        final String sku = request.getSku();
        final ActivityResult result = requestActivityResult(request,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        final String groupId = skuResolver.getGroupId();
                        final Intent intent = SamsungUtils.getPurchaseIntent(context, groupId, sku);
                        activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                    }
                });
        if (result == null || result.getResultCode() != Activity.RESULT_OK) {
            postEmptyResponse(request, result == null ? UNKNOWN_ERROR : USER_CANCELED);
            return;
        }
        final Intent data = result.getData();
        final Bundle bundle = data == null ? null : data.getExtras();
        @Status final int error = SamsungUtils.handleError(context, bundle);
        final SamsungPurchase samsungPurchase = SamsungUtils.getPurchase(bundle);
        if (error != Status.ERROR_NONE || samsungPurchase == null) {
            postEmptyResponse(request, error != Status.ERROR_NONE ? error : UNKNOWN_ERROR);
            return;
        }
        @SkuType final int skuType = skuResolver.resolveType(sku);
        final Purchase purchase = SamsungUtils.convertPurchase(samsungPurchase, skuType);
        postResponse(new PurchaseResponse(SUCCESS, getName(), purchase));
    }

    @Override
    protected void consume(@NonNull final ConsumeRequest request) {
        // Samsung doesn't support consume http://developer.samsung.com/forum/thread/a/201/244297
        postResponse(new ConsumeResponse(SUCCESS, getName(), request.getPurchase()));
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
        return new SamsungPurchaseVerifier(context, BillingMode.PRODUCTION);
    }

    @NonNull
    @Override
    public SkuResolver getSkuResolver(@NonNull IapProductList products) {

        final SamsungMapSkuResolver skuResolver = new SamsungMapSkuResolver(products.getProvidedId());

        for(IapProductDetail product : products.getProductDetails()) {
            skuResolver.add(product.getGlobalProductId(), product.getProductId(), product.getProductType());
        }

        return skuResolver;
    }

    @Override
    public <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull C context,
            @NonNull R skuResolver, @NonNull V purchaseVerifier) {
        this.context = (Context)context;
        this.skuResolver = (SamsungSkuResolver) skuResolver;
        this.purchaseVerifier = purchaseVerifier;

        this.helper = new SamsungBillingHelper(this.context, billingMode);
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, SamsungSkuResolver,
            PurchaseVerifier> {

        @BillingMode
        private int billingMode = BillingMode.PRODUCTION;

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @NonNull
        public Builder setBillingMode(@BillingMode final int billingMode) {
            this.billingMode = billingMode;
            return this;
        }

        @Override
        public SamsungBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("SamsungSkuResolver must be set.");
            }
            return new SamsungBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? DEFAULT : purchaseVerifier,
                    billingMode);
        }
    }
}
