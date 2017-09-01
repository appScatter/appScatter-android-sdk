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

package com.appscatter.iab.core;

import com.appscatter.iab.core.api.AdvancedIabHelper;
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.listener.BillingListener;
import com.appscatter.iab.core.listener.BillingListenerCompositor;
import com.appscatter.iab.core.listener.OnConsumeListener;
import com.appscatter.iab.core.listener.OnInventoryListener;
import com.appscatter.iab.core.listener.OnPurchaseListener;
import com.appscatter.iab.core.listener.OnSetupListener;
import com.appscatter.iab.core.listener.OnSkuDetailsListener;
import com.appscatter.iab.core.model.event.SetupResponse;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.utils.ASChecks;

import android.support.annotation.NonNull;

/**
 * This implementation of {@link IabHelper} adds two main features:
 * <ol>
 * <li> Pending request queue. With a help of {@link BillingRequestScheduler} subsequent billing
 * request will be scheduled for execution.
 * Each helper instance has a separate queue.
 * <li> API to add listeners for specific billing events.
 * </ol>
 */
class AdvancedIabHelperImpl extends SimpleIabHelperImpl implements AdvancedIabHelper {

    private final BillingRequestScheduler scheduler = BillingRequestScheduler.getInstance();
    private final BillingEventDispatcher dispatcher = BillingEventDispatcher.getInstance();
    private final BillingListenerCompositor listenerCompositor = new BillingListenerCompositor();

    AdvancedIabHelperImpl() {
        super();
    }

    private void deliverLastSetupEvent(@NonNull final OnSetupListener setupListener) {
        final SetupResponse setupResponse = billingBase.getSetupResponse();
        if (setupResponse != null) {
            setupListener.onSetupResponse(setupResponse);
        }
    }

    @Override
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        if (billingBase.getSetupResponse() == null) {
            // Lazy setup
            scheduler.schedule(this, billingRequest);
            ASIab.setup();
        } else if (!billingBase.isBusy()) {
            // No need to schedule anything
            super.postRequest(billingRequest);
        } else if (!billingRequest.equals(billingBase.getPendingRequest())) {
            // If request is not already being precessed, schedule it for later
            scheduler.schedule(this, billingRequest);
        }
    }

    @Override
    public void addSetupListener(@NonNull final OnSetupListener setupListener) {
        addSetupListener(setupListener, true);
    }

    @Override
    public void addSetupListener(@NonNull final OnSetupListener setupListener,
            final boolean deliverLast) {
        ASChecks.checkThread(true);
        listenerCompositor.addSetupListener(setupListener);
        if (deliverLast) {
            deliverLastSetupEvent(setupListener);
        }
    }

    @Override
    public void addPurchaseListener(@NonNull final OnPurchaseListener purchaseListener) {
        ASChecks.checkThread(true);
        listenerCompositor.addPurchaseListener(purchaseListener);
    }

    @Override
    public void addInventoryListener(@NonNull final OnInventoryListener inventoryListener) {
        ASChecks.checkThread(true);
        listenerCompositor.addInventoryListener(inventoryListener);
    }

    @Override
    public void addSkuDetailsListener(@NonNull final OnSkuDetailsListener skuInfoListener) {
        ASChecks.checkThread(true);
        listenerCompositor.addSkuDetailsListener(skuInfoListener);
    }

    @Override
    public void addConsumeListener(@NonNull final OnConsumeListener consumeListener) {
        ASChecks.checkThread(true);
        listenerCompositor.addConsumeListener(consumeListener);
    }

    @Override
    public void addBillingListener(@NonNull final BillingListener billingListener) {
        ASChecks.checkThread(true);
        listenerCompositor.addBillingListener(billingListener);
    }

    @Override
    public void register() {
        dispatcher.register(listenerCompositor);
    }

    @Override
    public void unregister() {
        dropQueue();
        dispatcher.unregister(listenerCompositor);
    }

    @Override
    public void dropQueue() {
        scheduler.dropQueue(this);
    }
}
