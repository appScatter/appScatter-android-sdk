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

import com.appscatter.iab.core.billing.AidlBillingHelper;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.stores.openstore.model.ItemType;
import com.appscatter.iab.utils.ASLog;

import org.onepf.oms.IOpenAppstore;
import org.onepf.oms.IOpenInAppBillingService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OpenStoreBillingHelper {

    protected static final int API = 3;
    protected static final int BATCH_SIZE = 20;


    @Nullable
    protected final OpenStoreIntentMaker intentMaker;
    @NonNull
    private final OpenAppstoreHelper openAppstoreHelper;
    @NonNull
    private final OpenInAppHelper openInAppHelper;
    @NonNull
    private final String packageName;

    public OpenStoreBillingHelper(@NonNull final Context context,
            @Nullable final OpenStoreIntentMaker intentMaker) {
        this.intentMaker = intentMaker;
        this.openAppstoreHelper = new OpenAppstoreHelper(context, IOpenAppstore.class);
        this.openInAppHelper = new OpenInAppHelper(context, IOpenInAppBillingService.class);
        this.packageName = context.getPackageName();
    }

    @Nullable
    public String getAppstoreName() {
        final IOpenAppstore openAppstore = openAppstoreHelper.getService();
        try {
            return openAppstore == null ? null : openAppstore.getAppstoreName();
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    public boolean isPackageInstaller() {
        final IOpenAppstore openAppstore = openAppstoreHelper.getService();
        try {
            return openAppstore != null && openAppstore.isPackageInstaller(packageName);
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return false;
    }

    public boolean isBillingAvailable() {
        final IOpenAppstore openAppstore = openAppstoreHelper.getService();
        try {
            return openAppstore != null && openAppstore.isBillingAvailable(packageName);
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return false;
    }

    @Nullable
    public Intent getBillingServiceIntent() {
        final IOpenAppstore openAppstore = openAppstoreHelper.getService();
        try {
            return openAppstore == null ? null : openAppstore.getBillingServiceIntent();
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    @Nullable
    public Intent getProductPageIntent() {
        final IOpenAppstore openAppstore = openAppstoreHelper.getService();
        try {
            return openAppstore == null ? null : openAppstore.getProductPageIntent(packageName);
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    @Nullable
    public Intent getRateItPageIntent() {
        final IOpenAppstore openAppstore = openAppstoreHelper.getService();
        try {
            return openAppstore == null ? null : openAppstore.getRateItPageIntent(packageName);
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    @Response
    public int isBillingSupported(@NonNull final ItemType itemType) {
        final IOpenInAppBillingService openInApp = openInAppHelper.getService();
        if (openInApp == null) {
            return Response.UNKNOWN;
        }
        try {
            final String type = itemType.toString();
            @Response final int response = openInApp.isBillingSupported(API, packageName, type);
            return response;
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return Response.UNKNOWN;
    }

    public Bundle getBuyIntent(@NonNull final String sku,
            @NonNull final ItemType itemType) {
        final IOpenInAppBillingService openInApp = openInAppHelper.getService();
        final String type = itemType.toString();
        try {
            return openInApp == null
                    ? null : openInApp.getBuyIntent(API, packageName, sku, type, null);
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    @Nullable
    public Bundle getSkuDetails(@NonNull final Map<ItemType, Collection<String>> typeSkuMap) {
        final IOpenInAppBillingService openInApp = openInAppHelper.getService();
        if (openInApp == null) {
            return null;
        }
        try {
            Bundle result = null;
            for (final Map.Entry<ItemType, Collection<String>> entry : typeSkuMap.entrySet()) {
                final String type = entry.getKey().toString();
                final Collection<String> skus = entry.getValue();
                for (final List<String> skuBatch : ASIabUtils.partition(skus, BATCH_SIZE)) {
                    final Bundle skuBundle = OpenStoreUtils.putSkus(new Bundle(), skuBatch);
                    final Bundle batch = openInApp.getSkuDetails(API, packageName, type, skuBundle);
                    if (OpenStoreUtils.getResponse(batch) != Response.OK) {
                        return batch;
                    }
                    if (result == null) {
                        result = batch;
                    } else {
                        OpenStoreUtils.addSkuDetails(result, batch);
                    }
                }
            }
            return result;
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    public Bundle getPurchases(@NonNull final ItemType itemType,
            @Nullable final String continuationToken) {
        final IOpenInAppBillingService openInApp = openInAppHelper.getService();
        final String type = itemType.toString();
        try {
            return openInApp == null
                    ? null : openInApp.getPurchases(API, packageName, type, continuationToken);
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return null;
    }

    @Response
    public int consumePurchase(@NonNull final String purchaseToken) {
        final IOpenInAppBillingService openInApp = openInAppHelper.getService();
        try {
            @Response final int response = openInApp == null ? Response.UNKNOWN
                    : openInApp.consumePurchase(API, packageName, purchaseToken);
            return response;
        } catch (RemoteException exception) {
            ASLog.e("", exception);
        }
        return Response.UNKNOWN;
    }

    protected class OpenAppstoreHelper extends AidlBillingHelper<IOpenAppstore> {

        public OpenAppstoreHelper(final Context context, final Class<IOpenAppstore> clazz) {
            super(context, clazz);
        }

        @Nullable
        @Override
        protected Intent getServiceIntent() {
            if (intentMaker != null) {
                return intentMaker.makeIntent(context);
            }
            return OpenStoreUtils.getOpenStoreIntent(context);
        }
    }

    protected class OpenInAppHelper extends AidlBillingHelper<IOpenInAppBillingService> {

        public OpenInAppHelper(final Context context, final Class<IOpenInAppBillingService> clazz) {
            super(context, clazz);
        }

        @Nullable
        @Override
        protected Intent getServiceIntent() {
            return getBillingServiceIntent();
        }
    }
}
