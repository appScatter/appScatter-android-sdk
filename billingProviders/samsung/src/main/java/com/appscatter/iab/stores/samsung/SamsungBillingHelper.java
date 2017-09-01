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

import com.appscatter.iab.core.billing.AidlBillingHelper;
import com.appscatter.iab.stores.samsung.model.ItemType;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;
import com.sec.android.iap.IAPConnector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SamsungBillingHelper extends AidlBillingHelper<IAPConnector> {

    protected static final String IAP_PACKAGE = "com.sec.android.iap";
    protected static final String SERVICE_CLASS = "com.sec.android.iap.service.IAPService";

    protected static final String START_DATE = "20130101";


    protected @BillingMode final int billingMode;
    @NonNull
    protected final String packageName;

    public SamsungBillingHelper(@NonNull final Context context,
            @BillingMode final int billingMode) {
        super(context, IAPConnector.class);
        this.billingMode = billingMode;
        this.packageName = context.getPackageName();
    }

    @Nullable
    @Override
    public IAPConnector getService() {
        if (!ASUtils.isInstalled(context, IAP_PACKAGE)) {
            SamsungUtils.promptInstall(context);
            return null;
        }
        return super.getService();
    }

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(IAP_PACKAGE, SERVICE_CLASS));
        return intent;
    }

    @Nullable
    protected Bundle init(@NonNull final IAPConnector iapConnector) {
        ASLog.logMethod();
        try {
            final Bundle bundle = iapConnector.init(billingMode);
            return SamsungUtils.checkSignature(context) ? bundle : null;
        } catch (RemoteException exception) {
            ASLog.e("init failed.", exception);
        }
        return null;
    }

    @Nullable
    public Bundle getItemsInbox(@NonNull final String groupId, final int start, final int end) {
        ASLog.logMethod();
        final IAPConnector iapConnector = getService();
        if (iapConnector == null) {
            return null;
        }
        final Bundle init = init(iapConnector);
        if (SamsungUtils.getResponse(init) != Response.ERROR_NONE) {
            return init;
        }
        final String now = SamsungUtils.getNowDate();
        try {
            return iapConnector.getItemsInbox(packageName, groupId, start, end, START_DATE, now);
        } catch (RemoteException exception) {
            ASLog.e("getItemsInbox failed.", exception);
        }
        return null;
    }

    @Nullable
    public Bundle getItemList(@NonNull final String groupId) {
        ASLog.logMethod();
        final IAPConnector iapConnector = getService();
        if (iapConnector == null) {
            return null;
        }
        final Bundle init = init(iapConnector);
        if (SamsungUtils.getResponse(init) != Response.ERROR_NONE) {
            return init;
        }
        try {
            return iapConnector.getItemList(billingMode, packageName, groupId, 1, Integer.MAX_VALUE,
                    ItemType.ALL.getCode());
        } catch (RemoteException exception) {
            ASLog.e("getItemList failed.", exception);
        }
        return null;
    }
}
