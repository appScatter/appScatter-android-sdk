/*
 * Copyright (c) 2017. AppScatter
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

package com.appscatter.iab.stores.slideme;

import com.appscatter.iab.core.billing.BaseBillingProvider;
import com.appscatter.iab.core.billing.BaseBillingProviderBuilder;
import com.appscatter.iab.core.billing.Compatibility;
import com.appscatter.iab.core.model.billing.IapProductDetail;
import com.appscatter.iab.core.model.billing.IapProductList;
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
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;
import com.slideme.sam.manager.inapp.Constants;
import com.slideme.sam.manager.inapp.InAppHelper;
import com.slideme.sam.manager.inapp.ListResult;
import com.slideme.sam.manager.inapp.PurchaseResult;
import com.slideme.sam.manager.inapp.PurchasesListResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.slideme.sam.manager.inapp.Constants.InAppStatus.INVALID_IAP_ID;
import static com.slideme.sam.manager.inapp.Constants.InAppStatus.SUCCESS;
import static com.slideme.sam.manager.inapp.Constants.InAppStatus.UNSUPPORTED_VERSION;
import static com.slideme.sam.manager.inapp.Constants.PERMISSION_BILLING;

public class SlideMeBillingProvider extends BaseBillingProvider<TypedSkuResolver, PurchaseVerifier> implements InAppHelper.InAppHelperCallback {

    public static final String NAME = "SlideMe";
    protected static final String PACKAGE = "com.slideme.sam.manager";
    protected static final String INSTALLER = PACKAGE;

    private InAppHelper inAppHelper;
    private ConsumeRequest mConsumeRequest;
    private InventoryRequest mInventoryRequest;


    protected SlideMeBillingProvider(@NonNull Activity context, @NonNull TypedSkuResolver skuResolver,
            @NonNull PurchaseVerifier purchaseVerifier) {
        super(context, skuResolver, purchaseVerifier);

        inAppHelper = new InAppHelper().create(context, this);
    }

    protected SlideMeBillingProvider() {
        super();
    }

    @Override
    public <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull C context, @NonNull R skuResolver, @NonNull V purchaseVerifier) {
        this.context = (Context)context;
        this.skuResolver = (TypedSkuResolver)skuResolver;
        this.purchaseVerifier = purchaseVerifier;

        this.inAppHelper = new InAppHelper().create(getActivity(), this);
    }

    @Override
    public PurchaseVerifier getPurchaseVerifier(@NonNull String key) {
        return PurchaseVerifier.DEFAULT;
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
    protected void skuDetails(@NonNull SkuDetailsRequest request) {
        final List<String> skus = new ArrayList<>(request.getSkus());
        inAppHelper.loadList(skus);
    }

    @Override
    protected void inventory(@NonNull InventoryRequest request) {
        mInventoryRequest = request;
        inAppHelper.loadPurchases(Constants.InAppProductType.ALL);
    }

    @Override
    protected void purchase(@NonNull final PurchaseRequest request) {

        final ActivityResult activityResult = requestActivityResult(request,
                new ActivityForResultLauncher(DEFAULT_REQUEST_CODE) {
                    @Override
                    public void onStartForResult(@NonNull final Activity activity)
                            throws IntentSender.SendIntentException {
                        final String sku = request.getSku();
                        String payload = "";
                        inAppHelper.purchase(sku, payload);                    }
                });

        if (activityResult == null) {
            postEmptyResponse(request, Status.UNKNOWN_ERROR);
            return;
        }

        final Intent data = activityResult.getData();
        final int resultCode = activityResult.getResultCode();

        switch (resultCode) {
            case InAppHelper.REQUEST_CODE_BUY:
                Bundle extrasBundle;
                if(data == null || data.getExtras() == null) {
                    extrasBundle = new Bundle();
                    extrasBundle.putInt(Constants.BUNDLE_STATUS, Constants.InAppStatus.ERROR);
                } else {
                    extrasBundle = data.getBundleExtra(Constants.EXTRA_RESPONSE);
                }
                PurchaseResult result = new PurchaseResult(extrasBundle);

                if (result.status == SUCCESS) {
                    final Purchase purchase = newPurchase(result);
                    postResponse(new PurchaseResponse(Status.SUCCESS, getName(), purchase));

                } else {
                    postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
                }
                break;
            default:
                postEmptyResponse(request, Status.UNKNOWN_ERROR);
                break;
        }

    }

    @Override
    protected void consume(@NonNull ConsumeRequest request) {
        final Purchase purchase = request.getPurchase();
        final String token = purchase.getToken();
        if (!TextUtils.isEmpty(token)) {
            mConsumeRequest = request;
            inAppHelper.consumePurchase(token);
        } else {
            postEmptyResponse(request, Status.ITEM_UNAVAILABLE);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void checkManifest() {
        ASChecks.checkPermission(context, PERMISSION_BILLING);
    }

    @NonNull
    @Override
    public int checkCompatibility() {
        //TODO Check SlideMe classes
        if (INSTALLER.equals(ASUtils.getPackageInstaller(context))) {
            return Compatibility.PREFERRED;
        }
        return Compatibility.COMPATIBLE;    }

    @Override
    public boolean isAvailable() {
        final boolean installed = ASUtils.isInstalled(context, PACKAGE);
        ASLog.d("SlideMe package installed: %b", installed);
        return installed;
    }

    @Nullable
    @Override
    public List<String> getPermissionsList() {
        return null;
    }

    @Override
    public void registerForEvents() {

    }

    @Override
    public void unregisterForEvents() {

    }

    @Override
    public void onIapReady() {
        ASLog.d("SlideMe IAP ready");
    }

    @Override
    public void onIapError(InAppHelper.IapError iapError) {
        ASLog.d(iapError.toString());
    }

    @Override
    public void onListLoaded(ListResult listResult) {

        switch (listResult.status) {
            case SUCCESS:
                final Collection<SkuDetails> skusDetails = SlideMeUtils.getSkusDetails(listResult, skuResolver);
                postResponse(new SkuDetailsResponse(Status.SUCCESS, getName(), skusDetails));
                break;
            case INVALID_IAP_ID:
                ASLog.e("Product data request failed: %s", listResult);
                postResponse(new SkuDetailsResponse(Status.ITEM_UNAVAILABLE, getName()));
                break;
            case UNSUPPORTED_VERSION:
                ASLog.e("Product data request failed: %s", listResult);
                postResponse(new SkuDetailsResponse(Status.SERVICE_UNAVAILABLE, getName()));
                break;
            default:
                ASLog.e("Unknown status: " + listResult.status);
                postResponse(new SkuDetailsResponse(Status.UNKNOWN_ERROR, getName()));
                break;
        }

    }

    @Override
    public void onPurchasesLoaded(PurchasesListResult purchasesListResult) {
        if (mInventoryRequest != null) {
            if (purchasesListResult.status == Constants.InAppStatus.SUCCESS) {
                if (purchasesListResult.productIds == null || purchasesListResult.purchaseData == null || purchasesListResult.signatures == null) {
                    ASLog.d("No purchases data: %s", purchasesListResult.status);
                    postEmptyResponse(mInventoryRequest, Status.SUCCESS);
                } else {
                    final int size = purchasesListResult.productIds.size();

                    final Collection<Purchase> inventory = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        final com.slideme.sam.manager.inapp.Purchase slidemePurchase = purchasesListResult.purchaseData.get(i);

                        final String signature = purchasesListResult.signatures.get(i);
                        final Purchase purchase = newPurchase(slidemePurchase, signature, "", purchasesListResult.status);
                        inventory.add(purchase);
                    }
//                    final String token = AptoideUtils.getContinuationToken(result);
//                    final boolean hasMore = !TextUtils.isEmpty(token);
                    postResponse(new InventoryResponse(Status.SUCCESS, getName(), inventory, false));

                }

            } else {
                postEmptyResponse(mInventoryRequest, Status.UNKNOWN_ERROR);
            }
        }
        mInventoryRequest = null;
    }

    @Override
    public void onPurchaseConsumed(int result) {
        if (mConsumeRequest != null) {
            final Purchase purchase = mConsumeRequest.getPurchase();
            if (result == SUCCESS) {
                postResponse(new ConsumeResponse(Status.SUCCESS, getName(), purchase));
            } else {
                postEmptyResponse(mConsumeRequest, Status.UNKNOWN_ERROR);
            }
            mConsumeRequest = null;
        }
    }

    /**
     * Transforms SlideMe purchase to library specific model.
     *
     * @param result SlideMe purchase to transform.
     *
     * @return Newly constructed purchase object, can't be null.
     */
    @NonNull
    public Purchase newPurchase(PurchaseResult result) {
        return newPurchase(result.purchaseData, result.signature,result.signedPurchaseData, result.status );

//        final String sku = result.purchaseData.iapId;
//        @SkuType final int skuType = skuResolver.resolveType(sku);
//        return new SignedPurchase.Builder(sku)
//                .setType(skuType)
//                .setProviderName(getName())
//                .setOriginalJson(result.signedPurchaseData)
//                .setToken(result.purchaseData.token)
//                .setPurchaseTime(result.purchaseData.purchaseTime)
//                .setCanceled(result.status != SUCCESS)
//                .setSignature(result.signature)
//                .build();
    }

    @NonNull
    public Purchase newPurchase(com.slideme.sam.manager.inapp.Purchase purchase, String signature, String json, int status ) {
        final String sku = purchase.iapId;
        @SkuType final int skuType = skuResolver.resolveType(sku);
        return new SignedPurchase.Builder(sku)
                .setType(skuType)
                .setProviderName(getName())
                .setOriginalJson(json)
                .setToken(purchase.token)
                .setPurchaseTime(purchase.purchaseTime)
                .setCanceled(status != SUCCESS)
                .setSignature(signature)
                .build();
    }

    private Activity getActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if(activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class Builder extends BaseBillingProviderBuilder<Builder, TypedSkuResolver,
            PurchaseVerifier> {
        private Activity mActivity;

        public Builder(@NonNull final Context context, @NonNull Activity activity) {
            super(context);
            mActivity = activity;
        }

        @Override
        public SlideMeBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException("TypedSkuResolver must be set.");
            }

            return new SlideMeBillingProvider(mActivity, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier);
        }
    }

}
