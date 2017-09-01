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

import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.billing.Status;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.stores.samsung.model.ItemType;
import com.appscatter.iab.stores.samsung.model.SamsungPurchase;
import com.appscatter.iab.stores.samsung.model.SamsungPurchasedItem;
import com.appscatter.iab.stores.samsung.model.SamsungSkuDetails;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.GET_ACCOUNTS;
import static com.appscatter.iab.stores.samsung.SamsungBillingProvider.NAME;

@SuppressWarnings({"ClassWithTooManyMethods", "PMD.GodClass"})
public final class SamsungUtils {

    private static final int BILLING_SIGNATURE_HASHCODE = 0x7a7eaf4b;
    private static final String BILLING_PACKAGE_NAME = "com.sec.android.iap";
    private static final String ACCOUNT_ACTIVITY = "com.sec.android.iap.activity.AccountActivity";
    private static final String PURCHASE_ACTIVITY
            = "com.sec.android.iap.activity.PaymentMethodListActivity";
    private static final String INSTALL_URI = "samsungapps://ProductDetail/com.sec.android.iap";
    private static final String ACCOUNT_TYPE_SAMSUNG = "com.osp.app.signin";

    private static final String KEY_TYPE = "mType";
    private static final String KEY_HAS_MORE = "hasMore";

    private static final String KEY_THIRD_PARTY = "THIRD_PARTY_NAME";
    private static final String KEY_STATUS_CODE = "STATUS_CODE";
    private static final String KEY_ERROR_STRING = "ERROR_STRING";
    private static final String KEY_IAP_UPGRADE_URL = "IAP_UPGRADE_URL";
    private static final String KEY_ITEM_GROUP_ID = "ITEM_GROUP_ID";
    private static final String KEY_ITEM_ID = "ITEM_ID";
    private static final String KEY_RESULT_LIST = "RESULT_LIST";
    private static final String KEY_RESULT_OBJECT = "RESULT_OBJECT";

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        }
    };

    private SamsungUtils() {
        throw new IllegalStateException();
    }

    public static boolean checkSignature(@NonNull final Context context) {
        final Signature[] signatures = ASIabUtils
                .getPackageSignatures(context, BILLING_PACKAGE_NAME);
        for (final Signature signature : signatures) {
            if (signature.hashCode() == BILLING_SIGNATURE_HASHCODE) {
                return true;
            }
        }
        ASLog.e("Samsung signature check failed.");
        return false;
    }

    public static boolean hasSamsungAccount(@NonNull final Context context) {
        if (!ASChecks.hasPermission(context, GET_ACCOUNTS)) {
            return true;
        }
        final Object service = context.getSystemService(Context.ACCOUNT_SERVICE);
        final AccountManager accountManager = (AccountManager) service;
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
        return accountManager.getAccountsByType(ACCOUNT_TYPE_SAMSUNG).length > 0;
    }

    @SuppressWarnings("PMD.MissingBreakInSwitch")
    @Nullable
    @Status
    public static int handleError(@NonNull final Context context,
            @Nullable final Bundle bundle) {
        @Response final int response = getResponse(bundle);
        if (response == Response.ERROR_NONE) {
            return Status.ERROR_NONE;
        }
        if (bundle == null || response == Response.ERROR_UNKNOWN) {
            return Status.UNKNOWN_ERROR;
        }
        ASLog.e("Response %s: %s", response, getErrorString(bundle));
        switch (response) {
            case Response.PAYMENT_IS_CANCELED:
                return Status.USER_CANCELED;
            case Response.ERROR_ALREADY_PURCHASED:
                return Status.ITEM_ALREADY_OWNED;
            case Response.ERROR_PRODUCT_DOES_NOT_EXIST:
            case Response.ERROR_ITEM_GROUP_ID_DOES_NOT_EXIST:
                return Status.ITEM_UNAVAILABLE;
            case Response.ERROR_NEED_APP_UPGRADE:
                SamsungUtils.promptUpgrade(context, bundle);
            case Response.ERROR_NETWORK_NOT_AVAILABLE:
            case Response.ERROR_CONNECT_TIMEOUT:
            case Response.ERROR_SOCKET_TIMEOUT:
                return Status.SERVICE_UNAVAILABLE;
            default:
                return Status.UNKNOWN_ERROR;
        }
    }

    public static void promptInstall(@NonNull final Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(INSTALL_URI));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            ASLog.e("", exception);
        }
    }

    public static void promptUpgrade(@NonNull final Context context,
            @Nullable final Bundle bundle) {
        final String uri;
        if (bundle == null || (uri = bundle.getString(KEY_IAP_UPGRADE_URL)) == null) {
            ASLog.e("No upgrade url.");
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            ASLog.e("Failed to open upgrade activity", exception);
        }
    }

    @NonNull
    public static String getNowDate() {
        //noinspection AccessToNonThreadSafeStaticField
        return DATE_FORMAT.get().format(new Date());
    }

    @Nullable
    public static Date parseDate(@Nullable final String date) {
        if (date == null) {
            return null;
        }
        try {
            return DATE_FORMAT.get().parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    @NonNull
    public static Intent getAccountIntent() {
        final Intent intent = new Intent();
        final ComponentName component = new ComponentName(BILLING_PACKAGE_NAME, ACCOUNT_ACTIVITY);
        intent.setComponent(component);
        return intent;
    }

    @NonNull
    public static Intent getPurchaseIntent(@NonNull final Context context,
            @NonNull final String groupId,
            @NonNull final String itemId) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        final ComponentName component = new ComponentName(BILLING_PACKAGE_NAME, PURCHASE_ACTIVITY);
        intent.setComponent(component);

        final Bundle bundle = new Bundle();
        final String packageName = context.getPackageName();
        bundle.putString(KEY_THIRD_PARTY, packageName);
        bundle.putString(KEY_ITEM_GROUP_ID, groupId);
        bundle.putString(KEY_ITEM_ID, itemId);
        intent.putExtras(bundle);

        return intent;
    }

    @NonNull
    public static ItemType getItemType(@NonNull final JSONObject jsonObject) throws JSONException {
        final String type = jsonObject.getString(KEY_TYPE);
        final ItemType itemType = ItemType.fromCode(type);
        if (itemType == null) {
            throw new JSONException("Unrecognized item type: " + type);
        }
        return itemType;
    }

    @Response
    public static int getResponse(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_STATUS_CODE)) {
            @Response final int code = bundle.getInt(KEY_STATUS_CODE);
            return code;
        }
        return Response.ERROR_UNKNOWN;
    }

    @Nullable
    public static String getErrorString(@Nullable final Bundle bundle) {
        if (bundle != null && bundle.containsKey(KEY_ERROR_STRING)) {
            return bundle.getString(KEY_ERROR_STRING);
        }
        return null;
    }

    @Nullable
    public static Collection<String> getItems(@Nullable final Bundle bundle) {
        final List<String> items;
        if (bundle == null || (items = bundle.getStringArrayList(KEY_RESULT_LIST)) == null) {
            return null;
        }

        if (items.isEmpty()) {
            Collections.emptyList();
        }

        return items;
    }

    @Nullable
    public static Collection<Purchase> getPurchasedItems(@Nullable final Bundle bundle,
            final boolean loadConsumable) {
        final Collection<String> items = getItems(bundle);
        if (items == null) {
            return null;
        }

        final Collection<Purchase> purchases = new ArrayList<>();
        for (final String item : items) {
            try {
                final SamsungPurchasedItem purchasedItem = new SamsungPurchasedItem(item);
                if (loadConsumable || purchasedItem.getItemType() != ItemType.CONSUMABLE) {
                    purchases.add(SamsungUtils.convertPurchasedItem(purchasedItem));
                }
            } catch (JSONException exception) {
                ASLog.e("Filed to decode Samsung inventory item.", exception);
            }
        }

        return purchases;
    }

    @Nullable
    public static Collection<SkuDetails> getSkusDetails(@Nullable final Bundle bundle,
            @NonNull final Collection<String> skus) {
        final Collection<String> items = getItems(bundle);
        if (items == null) {
            return null;
        }

        final Collection<SkuDetails> skusDetails = new ArrayList<>(skus.size());
        final Collection<String> unloadedItems = new ArrayList<>(skus);
        for (final String item : items) {
            try {
                final SamsungSkuDetails skuDetails = new SamsungSkuDetails(item);
                final String sku = skuDetails.getItemId();
                if (unloadedItems.contains(sku)) {
                    skusDetails.add(convertSkuDetails(skuDetails));
                    unloadedItems.remove(sku);
                }
            } catch (JSONException exception) {
                ASLog.e("Filed to decode Samsung sku details.", exception);
            }
        }
        for (final String sku : unloadedItems) {
            skusDetails.add(new SkuDetails(sku));
        }

        return skusDetails;
    }

    @Nullable
    public static SamsungPurchase getPurchase(@Nullable final Bundle bundle) {
        final String item;
        if (bundle == null || (item = bundle.getString(KEY_RESULT_OBJECT)) == null) {
            return null;
        }
        try {
            return new SamsungPurchase(item);
        } catch (JSONException exception) {
            ASLog.e("Failed to decode Samsung purchase.", exception);
        }
        return null;
    }

    @Nullable
    public static Boolean getHasMore(@Nullable final Bundle bundle) {
        if (bundle == null || !bundle.containsKey(KEY_HAS_MORE)) {
            return null;
        }
        return bundle.getBoolean(KEY_HAS_MORE);
    }

    @Nullable
    public static Bundle putHasMore(@Nullable final Bundle bundle, final boolean hasMore) {
        if (bundle == null) {
            return null;
        }
        bundle.putBoolean(KEY_HAS_MORE, hasMore);
        return bundle;
    }

    @SkuType
    public static int convertType(@NonNull final ItemType itemType) {
        switch (itemType) {
            case CONSUMABLE:
                return SkuType.CONSUMABLE;
            case NON_CONSUMABLE:
                return SkuType.ENTITLEMENT;
            case SUBSCRIPTION:
                return SkuType.SUBSCRIPTION;
            default:
                return SkuType.UNKNOWN;
        }
    }

    @NonNull
    public static SkuDetails convertSkuDetails(@NonNull final SamsungSkuDetails samsungSkuDetails) {
        return new SkuDetails.Builder(samsungSkuDetails.getItemId())
                .setOriginalJson(samsungSkuDetails.getOriginalJson())
                .setType(convertType(samsungSkuDetails.getItemType()))
                .setTitle(samsungSkuDetails.getName())
                .setDescription(samsungSkuDetails.getDescription())
                .setPrice(samsungSkuDetails.getPriceString())
                .setProviderName(NAME)
                .build();
    }

    @NonNull
    public static Purchase convertPurchasedItem(@NonNull final SamsungPurchasedItem purchasedItem) {
        final Date endDate = purchasedItem.getSubscriptionEndDate();
        return new Purchase.Builder(purchasedItem.getItemId())
                .setOriginalJson(purchasedItem.getOriginalJson())
                .setType(convertType(purchasedItem.getItemType()))
                .setToken(purchasedItem.getPurchaseId())
                .setPurchaseTime(purchasedItem.getPurchaseDate().getTime())
                .setCanceled(endDate != null && endDate.before(new Date()))
                .setProviderName(NAME)
                .build();
    }

    @NonNull
    public static Purchase convertPurchase(@NonNull final SamsungPurchase purchase,
            @SkuType final int skuType) {
        return new Purchase.Builder(purchase.getItemId())
                .setOriginalJson(purchase.getOriginalJson())
                .setType(skuType)
                .setToken(purchase.getPurchaseId())
                .setPurchaseTime(purchase.getPurchaseDate().getTime())
                .setProviderName(NAME)
                .build();
    }
}
