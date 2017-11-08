/*
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

package com.appscatter.iab.stores.fortumo;

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
import com.appscatter.iab.core.util.ActivityForResultLauncher;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.core.billing.Compatibility;
import com.appscatter.iab.stores.fortumo.model.FortumoSkuDetails;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import mp.MpUtils;
import mp.PaymentRequest;
import mp.PaymentResponse;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static com.appscatter.iab.core.model.event.billing.Status.SUCCESS;
import static com.appscatter.iab.core.model.event.billing.Status.UNKNOWN_ERROR;
import static com.appscatter.iab.core.model.event.billing.Status.USER_CANCELED;
import static com.appscatter.iab.core.verification.PurchaseVerifier.DEFAULT;

public class FortumoBillingProvider extends BaseBillingProvider<FortumoMappedSkuResolver, PurchaseVerifier> {

    public static final String NAME = "Fortumo";
    protected static final String PACKAGE = "mp.PaymentRequest";
    protected static final String PURCHASES_KEY = "fortumo.purchases.messageids";

    protected FortumoBillingProvider(
            @NonNull Context context,
            @NonNull FortumoMappedSkuResolver skuResolver,
            @NonNull PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);
    }

    @SuppressWarnings("unused")
    public FortumoBillingProvider() {
        super();
    }

    @Override
    protected void skuDetails(@NonNull SkuDetailsRequest request) {
        final Collection<String> skus = request.getSkus();
        final Collection<SkuDetails> skusDetails = FortumoUtils.getSkusDetails(skus, skuResolver);
        postResponse(new SkuDetailsResponse(SUCCESS, getName(), skusDetails));
    }

    @Override
    protected void inventory(@NonNull InventoryRequest request) {
        final Collection<Purchase> inventory = FortumoUtils.getInventory(context, request, skuResolver);

        postResponse(new InventoryResponse(SUCCESS, getName(), inventory, false));
    }

    @Override
    protected void purchase(@NonNull PurchaseRequest request) {
        final String sku = request.getSku();
        FortumoSkuDetails fortumoProduct = skuResolver.getProduct(sku);

        if (null == fortumoProduct) {
            ASLog.d("launchPurchaseFlow: required sku ", sku, " was not defined");
            postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
        } else {
            Purchase purchase = null;
            final String messageId = FortumoUtils.getMessageIdInPending(context, fortumoProduct.getProductId());
            if (fortumoProduct.getItemType() == SkuType.CONSUMABLE && !TextUtils.isEmpty(messageId) && !messageId.equals("-1")) {
                final PaymentResponse paymentResponse = MpUtils.getPaymentResponse(context, Long.valueOf(messageId));
                final int billingStatus = paymentResponse.getBillingStatus();
                if (billingStatus == MpUtils.MESSAGE_STATUS_BILLED) {
                    purchase = FortumoUtils.purchaseFromPaymentResponse(paymentResponse);
                    FortumoUtils.removePendingProduct(context, sku);
                    postResponse(new PurchaseResponse(Status.SUCCESS, getName(), purchase));

                } else if (billingStatus == MpUtils.MESSAGE_STATUS_FAILED) {
                    FortumoUtils.removePendingProduct(context, sku);
                    postEmptyResponse(request, Status.BILLING_UNAVAILABLE);
                } else {
                    postEmptyResponse(request, Status.PENDING);
                }

            } else {
//                PaymentRequest paymentRequest = new PaymentRequest.PaymentRequestBuilder().setService(isNook ? fortumoProduct.getNookServiceId() : fortumoProduct.getServiceId(),
//                        isNook ? fortumoProduct.getNookInAppSecret() : fortumoProduct.getInAppSecret()).
                final PaymentRequest paymentRequest = new PaymentRequest.PaymentRequestBuilder().setService(fortumoProduct.getServiceId(), fortumoProduct.getInAppSecret()).
                        setType(FortumoUtils.reverseType(fortumoProduct.getItemType())).
                        setProductName(fortumoProduct.getProductId()).
                        setDisplayString(fortumoProduct.getTitle()).
                        build();

                final ActivityResult activityResult = requestActivityResult(request,
                        new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                            @Override
                            public void onStartForResult(@NonNull final Activity activity)
                                    throws IntentSender.SendIntentException {
                                final Intent intent = paymentRequest.toIntent(context);
                                activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
                            }
                        });

                if (activityResult == null || activityResult.getResultCode() != Activity.RESULT_OK) {
                    postEmptyResponse(request, activityResult == null ? UNKNOWN_ERROR : USER_CANCELED);

                } else {
                    PaymentResponse paymentResponse = new PaymentResponse(activityResult.getData());
                    purchase = FortumoUtils.purchaseFromPaymentResponse(paymentResponse);
                    if (paymentResponse.getBillingStatus() == MpUtils.MESSAGE_STATUS_BILLED) {
                        postResponse(new PurchaseResponse(Status.SUCCESS, getName(), purchase));
                    } else if (paymentResponse.getBillingStatus() == MpUtils.MESSAGE_STATUS_PENDING) {
                        ASLog.d("handleActivityResult: status pending for ", paymentResponse.getProductName());
                        postResponse(new PurchaseResponse(Status.PENDING, getName(), purchase));
                        if (skuResolver.getProduct(paymentResponse.getServiceId()).getItemType() == SkuType.CONSUMABLE) {
                            FortumoUtils.addPendingPayment(context, paymentResponse.getProductName(), String.valueOf(paymentResponse.getMessageId()));
                            postEmptyResponse(request, Status.PENDING);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void consume(@NonNull ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        postResponse(new ConsumeResponse(Status.SUCCESS, getName(), purchase));
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void checkManifest() {
        ASChecks.checkPermission(context, ACCESS_NETWORK_STATE);
        ASChecks.checkPermission(context, READ_PHONE_STATE);
        ASChecks.checkPermission(context, RECEIVE_SMS);
        ASChecks.checkPermission(context, SEND_SMS);
    }

    @NonNull
    @Override
    @Compatibility
    public int checkCompatibility() {
        // TODO: review and match the compatibility depending on the carrier
        return Compatibility.COMPATIBLE;
    }

    @Nullable
    @Override
    public List<String> getPermissionsList() {
        return Arrays.asList(READ_PHONE_STATE, RECEIVE_SMS, SEND_SMS);
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
        return PurchaseVerifier.DEFAULT;
    }

    @NonNull
    @Override
    public SkuResolver getSkuResolver(@NonNull IapProductList products) {

        final FortumoMappedSkuResolver skuResolver = new FortumoMappedSkuResolver();

        for (IapProductDetail product: products.getProductDetails()) {
            FortumoSkuDetails fortumoProd = new FortumoSkuDetails.Builder()
                    .setTitle(product.getTitle())
                    .setServiceId(product.getProductId())
                    .setInAppSecret(product.getProductToken())
                    .setItemType(product.getProductType())
                    .build();
            skuResolver.add(product.getGlobalProductId(), fortumoProd);
        }

        return skuResolver;
    }

    @Override
    public <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull C context,
            @NonNull R skuResolver, @NonNull V purchaseVerifier) {
        this.context = (Context)context;
        this.skuResolver = (FortumoMappedSkuResolver) skuResolver;
        this.purchaseVerifier = purchaseVerifier;
    }

    @Override
    public boolean isAvailable() {
//        return ASUtils.isInstalled(context, PACKAGE);
        return true;
    }

    /**
     * Builder class to create the FortumoBillingProvider to use on the apps
     */
    public static class Builder extends BaseBillingProviderBuilder<Builder, FortumoMappedSkuResolver,
            PurchaseVerifier> {

        public Builder(@NonNull final Context context) {
            super(context);
        }

        @Override
        public FortumoBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("FortumoMappedSkuResolver must be set.");
            }
            return new FortumoBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? DEFAULT : purchaseVerifier);
        }
    }
}
