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

package com.appscatter.iab.core;

import com.appscatter.iab.core.listener.BillingListener;
import com.appscatter.iab.core.listener.BillingListenerCompositor;
import com.appscatter.iab.core.model.event.RequestHandledEvent;
import com.appscatter.iab.core.model.event.SetupResponse;
import com.appscatter.iab.core.model.event.SetupStartedEvent;
import com.appscatter.iab.core.model.event.billing.BillingEventType;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import com.appscatter.iab.core.model.event.billing.ConsumeResponse;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.model.event.billing.SkuDetailsResponse;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Deque;
import java.util.LinkedList;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * This class helps to deliver all billing events to appropriate listeners.
 * <p>
 * It's intended to exist as singleton and allow to add and remove corresponding listeners by
 * {@link #register(BillingListener)} and {@link #unregister(BillingListener)} methods.
 */
final class BillingEventDispatcher extends BillingListenerCompositor {

    @Nullable
    private static BillingEventDispatcher instance;
    /**
     * Used to cache responses that are delivered while library is busy.
     *
     * @see RequestHandledEvent
     */
    private final Deque<BillingResponse> responseQueue = new LinkedList<>();

    private BillingEventDispatcher() {
        super();
    }

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static BillingEventDispatcher getInstance() {
        ASChecks.checkThread(true);
        if (instance == null) {
            instance = new BillingEventDispatcher();
        }
        return instance;
    }

    /**
     * Registers listener to receive all billing events.
     *
     * @param billingListener Listener object to getEvents.
     */
    void register(@NonNull final BillingListener billingListener) {
        addBillingListener(billingListener);
    }

    /**
     * Unregisters listener from receiving any billing events.
     *
     * @param billingListener Listener object to unregister.
     */
    void unregister(@NonNull final BillingListener billingListener) {
        removeBillingListener(billingListener);
    }

    protected void removeBillingListener(@NonNull final BillingListener billingListener) {
        billingListeners.remove(billingListener);
        setupListeners.remove(billingListener);
        purchaseListeners.remove(billingListener);
        consumeListeners.remove(billingListener);
        inventoryListeners.remove(billingListener);
        skuDetailsListeners.remove(billingListener);
    }

    public void registerForEvents() {
        ASIab.getEvents(SetupStartedEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onSetupStartedEvent);
        ASIab.getEvents(SetupResponse.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onSetupResponseEvent);
        ASIab.getEvents(BillingRequest.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onBillingRequestEvent);
        ASIab.getEvents(BillingResponse.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onBillingResponseEvent);
        ASIab.getEvents(RequestHandledEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onRequestHandledEvent);
    }

    private void onSetupStartedEvent(@NonNull final SetupStartedEvent setupStartedEvent) {
        onSetupStarted(setupStartedEvent);
    }

    private void onSetupResponseEvent(@NonNull final SetupResponse setupResponse) {
        onSetupResponse(setupResponse);
    }

    private void onBillingRequestEvent(@NonNull final BillingRequest billingRequest) {
        onRequest(billingRequest);
    }

    private void onBillingResponseEvent(@NonNull final BillingResponse billingResponse) {
        // Store response in a queue to handle it later
        if (BillingBase.getInstance().isBusy()) {
            responseQueue.addLast(billingResponse);
        } else {
            handleBillingResponse(billingResponse);
        }
    }

    private void onRequestHandledEvent(@NonNull final RequestHandledEvent event) {
        while (!responseQueue.isEmpty()) {
            handleBillingResponse(responseQueue.pollFirst());
        }
    }

    //    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST"})
    private void handleBillingResponse(@NonNull final BillingResponse billingResponse) {
        ASLog.d("Handeling Response of BillingEventType : " + billingResponse.getType());
        onResponse(billingResponse);
        switch (billingResponse.getType()) {
            case BillingEventType.PURCHASE:
                onPurchase((PurchaseResponse) billingResponse);
                break;
            case BillingEventType.CONSUME:
                onConsume((ConsumeResponse) billingResponse);
                break;
            case BillingEventType.INVENTORY:
                onInventory((InventoryResponse) billingResponse);
                break;
            case BillingEventType.SKU_DETAILS:
                onSkuDetails((SkuDetailsResponse) billingResponse);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onRequest(@NonNull final BillingRequest billingRequest) {
        ASLog.logMethod(billingRequest);
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onRequest(billingRequest);
        }
        super.onRequest(billingRequest);
    }

    @Override
    public void onResponse(@NonNull final BillingResponse billingResponse) {
        ASLog.logMethod(billingResponse);
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onResponse(billingResponse);
        }
        super.onResponse(billingResponse);
    }

    @Override
    public void onSetupStarted(@NonNull final SetupStartedEvent setupStartedEvent) {
        ASLog.logMethod(setupStartedEvent);
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onSetupStarted(setupStartedEvent);
        }
        super.onSetupStarted(setupStartedEvent);
    }

    @Override
    public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
        ASLog.logMethod(setupResponse);
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onSetupResponse(setupResponse);
        }
        super.onSetupResponse(setupResponse);
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onPurchase(purchaseResponse);
        }
        super.onPurchase(purchaseResponse);
    }

    @Override
    public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onConsume(consumeResponse);
        }
        super.onConsume(consumeResponse);
    }

    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onInventory(inventoryResponse);
        }
        super.onInventory(inventoryResponse);
    }

    @Override
    public void onSkuDetails(@NonNull final SkuDetailsResponse skuDetailsResponse) {
        final BillingListener billingListener = ASIab.getConfiguration().getBillingListener();
        if (billingListener != null) {
            billingListener.onSkuDetails(skuDetailsResponse);
        }
        super.onSkuDetails(skuDetailsResponse);
    }
}
