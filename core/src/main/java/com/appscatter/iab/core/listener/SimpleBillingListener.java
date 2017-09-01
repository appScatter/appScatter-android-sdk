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

package com.appscatter.iab.core.listener;

import com.appscatter.iab.core.model.event.SetupResponse;
import com.appscatter.iab.core.model.event.SetupStartedEvent;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import com.appscatter.iab.core.model.event.billing.ConsumeResponse;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.model.event.billing.SkuDetailsResponse;

import android.support.annotation.NonNull;

/**
 * Stub implementation of {@link BillingListener} interface.
 */
@SuppressWarnings("PMD.UncommentedEmptyMethod")
public class SimpleBillingListener implements BillingListener {

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) { }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) { }

    @Override
    public void onRequest(@NonNull final BillingRequest billingRequest) { }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) { }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) { }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) { }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) { }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) { }
}
