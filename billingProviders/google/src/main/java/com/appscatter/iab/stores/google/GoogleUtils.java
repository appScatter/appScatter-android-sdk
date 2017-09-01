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

import com.appscatter.iab.utils.ASChecks;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

import static android.Manifest.permission.GET_ACCOUNTS;

@SuppressWarnings("PMD.LooseCoupling")
public final class GoogleUtils {

    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String SKU_DETAILS_LIST = "DETAILS_LIST";
    private static final String SKU_LIST = "ITEM_ID_LIST";
    private static final String BUY_INTENT = "BUY_INTENT";
    private static final String PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String SIGNATURE = "INAPP_DATA_SIGNATURE";
    private static final String ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    public static boolean hasGoogleAccount(@NonNull final Context context) {
        if (!ASChecks.hasPermission(context, GET_ACCOUNTS)) {
            return true;
        }
        final Object service = context.getSystemService(Context.ACCOUNT_SERVICE);
        final AccountManager accountManager = (AccountManager) service;
        // At least one Google account is present on device
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // return TODO;
        }
        return accountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE).length > 0;
    }

    @Nullable
    private static ArrayList<String> getList(@Nullable final Bundle bundle,
            @NonNull final String key) {
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.getStringArrayList(key);
        }
        return null;
    }

    @NonNull
    private static Bundle putList(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> list,
            @NonNull final String key) {
        if (list != null && !list.isEmpty()) {
            bundle.putStringArrayList(key, list);
        }
        return bundle;
    }

    @NonNull
    private static Bundle addList(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> list,
            @NonNull final String key) {
        if (list != null && !list.isEmpty()) {
            final ArrayList<String> newList;
            final ArrayList<String> oldList;
            if ((oldList = getList(bundle, key)) == null) {
                newList = list;
            } else {
                newList = new ArrayList<>(oldList);
                newList.addAll(list);
            }
            bundle.putStringArrayList(key, newList);
        }
        return bundle;
    }

    @NonNull
    static Bundle addSkuDetails(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> skuDetailsList) {
        return addList(bundle, skuDetailsList, SKU_DETAILS_LIST);
    }

    @Nullable
    static ArrayList<String> getSkuDetails(@Nullable final Bundle bundle) {
        return getList(bundle, SKU_DETAILS_LIST);
    }

    @NonNull
    static Bundle addDataList(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> purchaseData) {
        return addList(bundle, purchaseData, PURCHASE_DATA_LIST);
    }

    @Nullable
    static ArrayList<String> getDataList(@Nullable final Bundle bundle) {
        return getList(bundle, PURCHASE_DATA_LIST);
    }

    @NonNull
    static Bundle addItemList(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> purchaseData) {
        return addList(bundle, purchaseData, ITEM_LIST);
    }

    @Nullable
    static ArrayList<String> getItemList(@Nullable final Bundle bundle) {
        return getList(bundle, ITEM_LIST);
    }

    @NonNull
    static Bundle addSignatureList(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> purchaseData) {
        return addList(bundle, purchaseData, SIGNATURE_LIST);
    }

    @Nullable
    static ArrayList<String> getSignatureList(@Nullable final Bundle bundle) {
        return getList(bundle, SIGNATURE_LIST);
    }

    @NonNull
    static Bundle putSkuList(@NonNull final Bundle bundle,
            @Nullable final ArrayList<String> skuList) {
        return putList(bundle, skuList, SKU_LIST);
    }

    @Response
    static int getResponse(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(RESPONSE_CODE)) {
            @Response final int responseCode = bundle.getInt(RESPONSE_CODE);
            return responseCode;
        }
        return Response.UNKNOWN;
    }

    @Response
    static int getResponse(@Nullable final Intent intent) {
        return getResponse(intent == null ? null : intent.getExtras());
    }

    @NonNull
    static Bundle putResponse(@NonNull final Bundle bundle,
            @Response final int response) {
        bundle.putInt(RESPONSE_CODE, response);
        return bundle;
    }

    @Nullable
    static String getContinuationToken(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(CONTINUATION_TOKEN)) {
            return bundle.getString(CONTINUATION_TOKEN);
        }
        return null;
    }

    @Nullable
    static PendingIntent getBuyIntent(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(BUY_INTENT)) {
            return bundle.getParcelable(BUY_INTENT);
        }
        return null;
    }

    @Nullable
    static String getPurchaseData(@Nullable final Intent intent) {
        if (intent != null && intent.hasExtra(PURCHASE_DATA)) {
            return intent.getStringExtra(PURCHASE_DATA);
        }
        return null;
    }

    @Nullable
    static String getSignature(@Nullable final Intent intent) {
        if (intent != null && intent.hasExtra(SIGNATURE)) {
            return intent.getStringExtra(SIGNATURE);
        }
        return null;
    }


    private GoogleUtils() {
        throw new UnsupportedOperationException();
    }
}
