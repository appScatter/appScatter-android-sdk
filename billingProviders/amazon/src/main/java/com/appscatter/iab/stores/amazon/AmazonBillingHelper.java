/*
 * Copyright 2012-2015 One Platform Foundation
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

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;
import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.util.SyncedReference;
import com.appscatter.iab.stores.amazon.events.AmazonProductDataResponse;
import com.appscatter.iab.stores.amazon.events.AmazonPurchaseResponse;
import com.appscatter.iab.stores.amazon.events.AmazonPurchaseUpdatesResponse;
import com.appscatter.iab.utils.ASLog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class handles all communications between library and Amazon SDK.
 * <p>
 * Intended to exist as singleton, which is registered for Amazon callbacks as soon as it's
 * created.
 */
public class AmazonBillingHelper implements PurchasingListener {

    /**
     * Timeout to give up on waiting for user data.
     */
    protected static final long USER_DATA_TIMEOUT = Long.parseLong("5000");

    private static AmazonBillingHelper instance;
    // User data is requested from library thread, but delivered on main.
    @Nullable
    private volatile SyncedReference<UserData> syncUserData;

    protected AmazonBillingHelper() {
        super();
    }

    @SuppressWarnings("PMD.NonThreadSafeSingleton")
    public static AmazonBillingHelper getInstance(@NonNull final Context context) {
        if (instance == null) {
            instance = new AmazonBillingHelper();
            PurchasingService.registerListener(context, instance);
        }
        return instance;
    }

    /**
     * Requests user data form Amazon SDK.
     *
     * @return User data if received withing {@link #USER_DATA_TIMEOUT}, null otherwise.
     */
    @Nullable
    public UserData getUserData() {
        final SyncedReference<UserData> syncUserData = new SyncedReference<>();
        try {
            this.syncUserData = syncUserData;
            PurchasingService.getUserData();
            return syncUserData.get(USER_DATA_TIMEOUT);
        } finally {
            this.syncUserData = null;
        }
    }

    @Override
    public void onUserDataResponse(@NonNull final UserDataResponse userDataResponse) {
        ASLog.logMethod(userDataResponse);
        final SyncedReference<UserData> syncUserData = this.syncUserData;
        if (syncUserData == null) {
            return;
        }
        switch (userDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                syncUserData.set(userDataResponse.getUserData());
                break;
            case FAILED:
            case NOT_SUPPORTED:
                ASLog.e("UserData request failed: %s", userDataResponse);
                break;
        }
    }

    @Override
    public void onProductDataResponse(@NonNull final ProductDataResponse productDataResponse) {
        ASIab.post(new AmazonProductDataResponse(productDataResponse));
    }

    @Override
    public void onPurchaseResponse(@NonNull final PurchaseResponse purchaseResponse) {
        ASIab.post(new AmazonPurchaseResponse(purchaseResponse));
    }

    @Override
    public void onPurchaseUpdatesResponse(@NonNull final PurchaseUpdatesResponse purchaseUpdatesResponse) {
        ASIab.post(new AmazonPurchaseUpdatesResponse(purchaseUpdatesResponse));
    }
}
