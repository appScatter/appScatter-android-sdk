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

package com.appscatter.iab.stores.openstore;

import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SignedPurchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.stores.openstore.model.OpenPurchase;
import com.appscatter.iab.stores.openstore.model.OpenSkuDetails;
import com.appscatter.iab.stores.openstore.model.PurchaseState;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;

import org.json.JSONException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class OpenStoreUtils {

    public static final String ACTION_BIND_OPENSTORE = "org.onepf.oms.openappstore.BIND";

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String SKU_DETAILS_LIST = "DETAILS_LIST";
    private static final String SKU_LIST = "ITEM_ID_LIST";
    private static final String BUY_INTENT = "BUY_INTENT";
    private static final String PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String SIGNATURE = "INAPP_DATA_SIGNATURE";
    //    private static final String ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    @Nullable
    public static Intent getOpenStoreIntent(@NonNull final Context context) {
        final Intent intent = new Intent(ACTION_BIND_OPENSTORE);
//        intent.setPackage("com.slideme.sam.manager"); //TODO maybe we should add the store package here
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent, 0);
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            final Intent explicitIntent = new Intent(intent);
            //TODO maybe we should validate if the service matches the service that we want- this may be fixed by the setPackage??
            final ServiceInfo serviceInfo = resolveInfos.get(0).serviceInfo;
//            Log.d("cenas", "cenas " + serviceInfo.packageName + " -- " + serviceInfo.name);
//            ComponentName component = new ComponentName(serviceInfo.packageName, serviceInfo.name);
//            Log.d("cenas", "cenas " + component);
//            explicitIntent.setComponent(component);
//            explicitIntent.setPackage(serviceInfo.packageName);
////            explicitIntent.setClassName(serviceInfo.packageName, serviceInfo.name);
//            Log.d("cenas", "package: " + explicitIntent.getPackage());
//
            explicitIntent.setClassName(serviceInfo.packageName, serviceInfo.name);
            return explicitIntent;
        }
        return null;
    }

    @NonNull
    public static OpenStoreIntentMaker getIntentMaker(@NonNull final String name,
            @NonNull final String... packages) {
        //noinspection OverlyComplexAnonymousInnerClass
        return new OpenStoreIntentMaker() {
            @Nullable
            @Override
            public Intent makeIntent(@NonNull final Context context) {
                for (final String packageName : packages) {
                    if (ASUtils.isInstalled(context, packageName)) {
                        final Intent intent = new Intent(ACTION_BIND_OPENSTORE);
                        intent.setPackage(packageName);
                        return intent;
                    }
                }
                return null;
            }

            @NonNull
            @Override
            public String getProviderName() {
                return name;
            }
        };
    }

    @NonNull
    public static Bundle putSkus(@NonNull final Bundle bundle,
            @NonNull final Collection<String> skus) {
        return ASIabUtils.putList(bundle, new ArrayList<>(skus), SKU_LIST);
    }

    @NonNull
    public static Bundle addSkuDetails(@NonNull final Bundle bundle,
            @Nullable final Bundle source) {
        final ArrayList<String> skuDetailsList = getSkusDetailsList(source);
        return ASIabUtils.addList(bundle, skuDetailsList, SKU_DETAILS_LIST);
    }

    @Nullable
    public static String getPurchaseData(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(PURCHASE_DATA);
    }

    @Nullable
    public static String getSignature(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(SIGNATURE);
    }

    @Nullable
    public static String getContinuationToken(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getString(CONTINUATION_TOKEN);
    }

    @Response
    public static int getResponse(@Nullable final Bundle bundle) {
        if (bundle == null || !bundle.containsKey(RESPONSE_CODE)) {
            return Response.UNKNOWN;
        }
        @Response final int responseCode = bundle.getInt(RESPONSE_CODE);
        return responseCode;
    }

    @Nullable
    public static PendingIntent getPurchaseIntent(@Nullable final Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getParcelable(BUY_INTENT);
    }

    @Nullable
    public static OpenPurchase getPurchase(@Nullable final Bundle bundle) {
        final String purchaseData = getPurchaseData(bundle);
        if (purchaseData == null) {
            return null;
        }
        try {
            return new OpenPurchase(purchaseData);
        } catch (JSONException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    @SuppressWarnings("PMD.LooseCoupling")
    @Nullable
    public static ArrayList<String> getSkusDetailsList(@Nullable final Bundle bundle) {
        return ASIabUtils.getList(bundle, SKU_DETAILS_LIST);
    }

    @SuppressWarnings({"PMD.LooseCoupling", "CollectionDeclaredAsConcreteClass"})
    @Nullable
    public static ArrayList<String> getPurchasesList(@Nullable final Bundle bundle) {
        return ASIabUtils.getList(bundle, PURCHASE_DATA_LIST);
    }

    @SuppressWarnings({"PMD.LooseCoupling", "CollectionDeclaredAsConcreteClass"})
    @Nullable
    public static ArrayList<String> getSignaturesList(@Nullable final Bundle bundle) {
        return ASIabUtils.getList(bundle, SIGNATURE_LIST);
    }

    @Nullable
    public static Collection<OpenSkuDetails> getSkusDetails(@Nullable final Bundle bundle) {
        final Collection<String> skusDetailsList = getSkusDetailsList(bundle);
        if (skusDetailsList == null) {
            return null;
        }
        final Collection<OpenSkuDetails> skusDetails = new ArrayList<>(skusDetailsList.size());
        for (final String skuDetails : skusDetailsList) {
            try {
                skusDetails.add(new OpenSkuDetails(skuDetails));
            } catch (JSONException exception) {
                ASLog.e("", exception);
            }
        }
        return skusDetails;
    }

    @Nullable
    public static Collection<OpenPurchase> getPurchases(@Nullable final Bundle bundle) {
        final Collection<String> purchasesList = getPurchasesList(bundle);
        if (purchasesList == null) {
            return null;
        }
        final Collection<OpenPurchase> purchases = new ArrayList<>(purchasesList.size());
        for (final String purchase : purchasesList) {
            try {
                purchases.add(new OpenPurchase(purchase));
            } catch (JSONException exception) {
                ASLog.e("", exception);
            }
        }
        return purchases;
    }

    @NonNull
    public static SkuDetails convertSkuDetails(@NonNull final OpenSkuDetails openSkuDetails,
            @NonNull final String name,
            @SkuType final int skuType) {
        return new SkuDetails.Builder(openSkuDetails.getProductId())
                .setType(skuType)
                .setProviderName(name)
                .setTitle(openSkuDetails.getTitle())
                .setPrice(openSkuDetails.getPrice())
                .setDescription(openSkuDetails.getDescription())
                .setOriginalJson(openSkuDetails.getOriginalJson())
                .build();
    }

    @NonNull
    public static Purchase convertPurchase(@NonNull final OpenPurchase openPurchase,
            @NonNull final String name,
            @SkuType final int skuType,
            @Nullable final String signature) {
        return new SignedPurchase.Builder(openPurchase.getProductId())
                .setType(skuType)
                .setProviderName(name)
                .setSignature(signature)
                .setToken(openPurchase.getPurchaseToken())
                .setPurchaseTime(openPurchase.getPurchaseTime())
                .setOriginalJson(openPurchase.getOriginalJson())
                .setCanceled(openPurchase.getPurchaseState() != PurchaseState.PURCHASED)
                .build();

    }

    private OpenStoreUtils() {
        throw new UnsupportedOperationException();
    }
}
