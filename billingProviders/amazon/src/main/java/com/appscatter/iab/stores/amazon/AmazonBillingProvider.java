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

package com.appscatter.iab.stores.amazon;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.ResponseReceiver;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;
import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.billing.BaseBillingProvider;
import com.appscatter.iab.core.billing.BaseBillingProviderBuilder;
import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.billing.Compatibility;
import com.appscatter.iab.core.model.billing.IapProductDetail;
import com.appscatter.iab.core.model.billing.IapProductList;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.ConsumeResponse;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsResponse;
import com.appscatter.iab.core.model.event.billing.Status;
import com.appscatter.iab.core.sku.MapSkuResolver;
import com.appscatter.iab.core.sku.SkuResolver;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.stores.amazon.events.AmazonProductDataResponse;
import com.appscatter.iab.stores.amazon.events.AmazonPurchaseResponse;
import com.appscatter.iab.stores.amazon.events.AmazonPurchaseUpdatesResponse;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static com.appscatter.iab.core.model.event.billing.Status.ITEM_ALREADY_OWNED;
import static com.appscatter.iab.core.model.event.billing.Status.ITEM_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.SUCCESS;
import static com.appscatter.iab.core.model.event.billing.Status.UNAUTHORISED;
import static com.appscatter.iab.core.model.event.billing.Status.UNKNOWN_ERROR;

/**
 * This {@link BillingProvider} implementation adds support of
 * <a href="http://www.amazon.com/mobile-apps/b?node=2350149011">Amazon Appstore</a>
 */
@SuppressWarnings("PMD.GodClass")
public class AmazonBillingProvider extends BaseBillingProvider<SkuResolver, PurchaseVerifier> {

    public static final String NAME = "Amazon";
    protected static final String INSTALLER = "com.amazon.venezia";
    protected static final Pattern PATTERN_STORE_PACKAGE = Pattern.compile(
            "(com\\.amazon\\.venezia)|([a-z]{2,3}\\.amazon\\.mShop\\.android(\\.apk)?)");
    protected static final String TESTER_PACKAGE = "com.amazon.sdktestclient";

    /**
     * Helper object handles all Amazon SDK related calls.
     */
    @NonNull
    protected AmazonBillingHelper billingHelper;

    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    protected AmazonBillingProvider(
            @NonNull final Context context,
            @NonNull final SkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
        this.billingHelper = getHelper();
    }

    @SuppressWarnings("unused")
    public AmazonBillingProvider() {
        super();
    }

    @NonNull
    protected AmazonBillingHelper getHelper() {
        return AmazonBillingHelper.getInstance(context);
    }

    @Override
    public void registerForEvents() {
        ASIab.getEvents(AmazonProductDataResponse.class).subscribe(this::onAmazonProductDataResponseEvent);
        ASIab.getEvents(AmazonPurchaseUpdatesResponse.class).subscribe(this::onAmazonPurchaseUpdatesResponseEvent);
        ASIab.getEvents(AmazonPurchaseResponse.class).subscribe(this::onAmazonPurchaseResponseEvent);
    }

    @Override
    public void unregisterForEvents() {

    }

    @NonNull
    @Override
    public PurchaseVerifier getPurchaseVerifier(String key) {
        return PurchaseVerifier.DEFAULT;
    }

    @NonNull
    @Override
    public SkuResolver getSkuResolver(@NonNull IapProductList products) {

        final MapSkuResolver skuResolver = new MapSkuResolver();
        for(IapProductDetail product : products.getProductDetails()) {
            skuResolver.add(product.getGlobalProductId(), product.getProductId());
        }
        return skuResolver;
    }

    @Override
    public <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull C context,
            @NonNull R skuResolver, @NonNull V purchaseVerifier) {
        this.context = (Context)context;
        this.skuResolver = skuResolver;
        this.purchaseVerifier = purchaseVerifier;
        this.billingHelper = getHelper();
    }

    /**
     * Handles sku details response from Amazon.
     *
     * @param response Response to handle.
     */
    private void onAmazonProductDataResponseEvent(@NonNull final AmazonProductDataResponse response) {
        final ProductDataResponse.RequestStatus status = response.getProductDataResponse().getRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                final Collection<SkuDetails> skusDetails = AmazonUtils.getSkusDetails(response.getProductDataResponse());
                postResponse(new SkuDetailsResponse(SUCCESS, getName(), skusDetails));
                break;
            case FAILED:
            case NOT_SUPPORTED:
                ASLog.e("Product data request failed: %s", response);
                postResponse(new SkuDetailsResponse(AmazonUtils.handleFailure(context), getName()));
                break;
            default:
                ASLog.e("Unknown status: " + status);
                postResponse(new SkuDetailsResponse(UNKNOWN_ERROR, getName()));
                break;
        }
    }

    /**
     * Handles inventory response from Amazon.
     *
     * @param response Response to handle.
     */
    private void onAmazonPurchaseUpdatesResponseEvent(@NonNull final AmazonPurchaseUpdatesResponse response) {
        final PurchaseUpdatesResponse.RequestStatus status = response.getPurchaseUpdatesResponse().getRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                final Collection<Purchase> inventory = AmazonUtils.getInventory(response.getPurchaseUpdatesResponse());
                final boolean hasMore = response.getPurchaseUpdatesResponse().hasMore();
                postResponse(new InventoryResponse(SUCCESS, getName(), inventory, hasMore));
                break;
            case FAILED:
            case NOT_SUPPORTED:
                ASLog.e("Purchase updates request failed: %s", response);
                postResponse(new InventoryResponse(AmazonUtils.handleFailure(context), getName()));
                break;
            default:
                ASLog.e("Unknown status: " + status);
                postResponse(new InventoryResponse(UNKNOWN_ERROR, getName()));
                break;
        }
    }

    /**
     * Handles purchase response from Amazon.
     *
     * @param response Response to handle.`
     */
    private void onAmazonPurchaseResponseEvent(@NonNull final AmazonPurchaseResponse response) {
        final com.amazon.device.iap.model.PurchaseResponse.RequestStatus status =
                response.getPurchaseResponse().getRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                final Receipt receipt = response.getPurchaseResponse().getReceipt();
                final UserData userData = response.getPurchaseResponse().getUserData();
                final Purchase purchase = AmazonUtils.convertPurchase(receipt, userData);
                @Status final int responseStatus = purchase == null ? UNKNOWN_ERROR : SUCCESS;
                postResponse(new PurchaseResponse(responseStatus, getName(), purchase));
                break;
            case INVALID_SKU:
                postResponse(new PurchaseResponse(ITEM_UNAVAILABLE, getName()));
                break;
            case ALREADY_PURCHASED:
                postResponse(new PurchaseResponse(ITEM_ALREADY_OWNED, getName()));
                break;
            case FAILED:
            case NOT_SUPPORTED:
                ASLog.e("Purchase request failed: %s", response);
                postResponse(new PurchaseResponse(AmazonUtils.handleFailure(context), getName()));
                break;
            default:
                ASLog.e("Unknown status: " + status);
                postResponse(new PurchaseResponse(UNKNOWN_ERROR, getName()));
                break;
        }
    }

    //    @SuppressFBWarnings({"EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    @Override
    public void checkManifest() {
        ASChecks.checkPermission(context, ACCESS_NETWORK_STATE);
        //TODO ASChecks.checkReceiver
        final PackageManager packageManager = context.getPackageManager();
        final ComponentName componentName = new ComponentName(context, ResponseReceiver.class);
        try {
            if (!packageManager.getReceiverInfo(componentName, 0).exported) {
                throw new IllegalStateException("Amazon receiver must be exported.");
            }
        } catch (PackageManager.NameNotFoundException exception) {
            throw new IllegalStateException(
                    "You must declare Amazon receiver to use Amazon billing provider.", exception);
        }
    }

    @Override
    public List<String> getPermissionsList() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        final PackageManager packageManager = context.getPackageManager();
        for (final PackageInfo info : packageManager.getInstalledPackages(0)) {
            if (PATTERN_STORE_PACKAGE.matcher(info.packageName).matches()) {
                // Check sdk tester package if app is in sandbox mode.
                return !PurchasingService.IS_SANDBOX_MODE
                        || ASUtils.isInstalled(context, TESTER_PACKAGE);
            }
        }
        return false;
    }

    @NonNull
    @Override
    @Compatibility
    public int checkCompatibility() {
        //TODO Check Amazon classes
        if (INSTALLER.equals(ASUtils.getPackageInstaller(context))) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;
    }

    @Override
    protected void handleRequest(@NonNull final BillingRequest billingRequest) {
        if (billingHelper.getUserData() == null) {
            postEmptyResponse(billingRequest, UNAUTHORISED);
            return;
        }
        super.handleRequest(billingRequest);
    }

    @Override
    public void skuDetails(@NonNull final SkuDetailsRequest request) {
        final Set<String> skus = request.getSkus();
        PurchasingService.getProductData(skus);
    }

    @Override
    public void inventory(@NonNull final InventoryRequest request) {
        final boolean startOver = request.startOver();
        PurchasingService.getPurchaseUpdates(startOver);
    }

    @Override
    public void purchase(@NonNull final PurchaseRequest request) {
        final String sku = request.getSku();
        PurchasingService.purchase(sku);
    }

    @Override
    public void consume(@NonNull final ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        final String token = purchase.getToken();
        if (!TextUtils.isEmpty(token)) {
            PurchasingService.notifyFulfillment(token, FulfillmentResult.FULFILLED);
            postResponse(new ConsumeResponse(SUCCESS, getName(), purchase));
        } else {
            postEmptyResponse(request, ITEM_UNAVAILABLE);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, SkuResolver,
            PurchaseVerifier> {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public AmazonBillingProvider build() {
            return new AmazonBillingProvider(context,
                    skuResolver == null ? SkuResolver.DEFAULT : skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier);
        }
    }
}
