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

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.Configuration;
import com.appscatter.iab.core.model.event.RequestHandledEvent;
import com.appscatter.iab.core.model.event.SetupResponse;
import com.appscatter.iab.core.model.event.SetupStartedEvent;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import com.appscatter.iab.core.util.BillingUtils;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.appscatter.iab.core.model.event.billing.Status.BILLING_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.BUSY;
import static com.appscatter.iab.core.model.event.billing.Status.NO_BILLING_PROVIDER;

/**
 * This class is intended to be a single entry point for all {@link BillingRequest}s, it also holds
 * library state (current {@link BillingProvider}) and last {@link SetupResponse}.
 */
final class BillingBase {

    private static BillingBase instance;
    /**
     * Currently used configuration object.
     *
     * @see ASIab#init(Application, Configuration)
     */
    private Configuration configuration;
    /**
     * Last received setup response.
     *
     * @see ASIab#setup()
     */
    @Nullable
    private SetupResponse setupResponse;
    /**
     * Currently used billing provider.
     */
    @Nullable
    private BillingProvider currentProvider;
    /**
     * Request being executed by {@link #currentProvider}.
     *
     * @see RequestHandledEvent
     */
    @Nullable
    private BillingRequest pendingRequest;

    private BillingBase() {
        super();
    }

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static BillingBase getInstance() {
        ASChecks.checkThread(true);
        if (instance == null) {
            instance = new BillingBase();
        }
        return instance;
    }

    /**
     * Sets configuration currently used by library.
     * <p>
     * This method resets library setup state.
     *
     * @param configuration Current configuration object
     */
    void setConfiguration(@NonNull final Configuration configuration) {
        this.configuration = configuration;
        this.setupResponse = null;
        this.currentProvider = null;
    }

    /**
     * Gets last setup response.
     *
     * @return SetupResponse object if setup has finished at least once, null otherwise.
     */
    @Nullable
    SetupResponse getSetupResponse() {
        ASChecks.checkThread(true);
        return setupResponse;
    }

    /**
     * Gets request currently being executed.
     *
     * @return BillingRequest object if there's one, null otherwise.
     */
    @Nullable
    BillingRequest getPendingRequest() {
        ASChecks.checkThread(true);
        return pendingRequest;
    }

    /**
     * Indicates whether current {@link BillingProvider} is busy executing request.
     *
     * @return True is BillingProvider is busy, false otherwise.
     */
    boolean isBusy() {
        ASChecks.checkThread(true);
        return getPendingRequest() != null;
    }

    /**
     * Attempts to execute supplied billing request using current billing provider.
     * <p>
     * If current provider is unavailable or busy, supplied request will not be executed and
     * instead corresponding response will be send immediately.
     *
     * @param billingRequest BillingRequest to execute.
     * @see #isBusy()
     */
    void postRequest(@NonNull final BillingRequest billingRequest) {
        ASChecks.checkThread(true);
        final SetupResponse setupResponse;
        if (isBusy()) {
            // Library is busy with another request
            ASIab.post(BillingUtils.emptyResponse(null, billingRequest, BUSY));
        } else if ((setupResponse = getSetupResponse()) == null || !setupResponse.isSuccessful()) {
            // Setup was not started, is in progress or failed
            ASIab.post(BillingUtils.emptyResponse(null, billingRequest, NO_BILLING_PROVIDER));
        } else if (configuration.skipStaleRequests() && BillingUtils.isStale(billingRequest)) {
            // Request is no longer relevant, try next one
            ASLog.d("Skipping stale request: " + billingRequest);
            BillingRequestScheduler.getInstance().handleNext();
        } else {
            pendingRequest = billingRequest;
            // Send request to be handled by BillingProvider
            ASIab.post(billingRequest);
        }
    }

    public void registerForEvents() {
        ASIab.getEvents(SetupStartedEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onSetupStartedEvent);
        ASIab.getEvents(SetupResponse.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onSetupResponseEvent);
        ASIab.getEvents(BillingRequest.class).subscribe(this::onBillingRequestEvent);
        ASIab.getEvents(RequestHandledEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onRequestHandledEvent);
        ASIab.getEvents(BillingResponse.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onBillingResponseEvent);
    }

    private void onSetupStartedEvent(@NonNull final SetupStartedEvent event) {
        ASChecks.checkThread(true);
        this.currentProvider = null;
        this.setupResponse = null;
    }

    private void onSetupResponseEvent(@NonNull final SetupResponse setupResponse) {
        // Called before any other SetupResponse handler
        this.setupResponse = setupResponse;
        if (setupResponse.isSuccessful()) {
            // Suitable provider was found
            currentProvider = setupResponse.getBillingProvider();
        }
    }

    private void onBillingRequestEvent(@NonNull final BillingRequest billingRequest) {
        final BillingProvider billingProvider = this.currentProvider;
        if (billingProvider != null) {
            billingProvider.onBillingRequest(billingRequest);
        }
        ASIab.post(new RequestHandledEvent(billingRequest));
    }

    private void onRequestHandledEvent(@NonNull final RequestHandledEvent event) {
        if (!event.getBillingRequest().equals(pendingRequest)) {
            throw new IllegalStateException();
        }
        pendingRequest = null;
    }

    private void onBillingResponseEvent(@NonNull final BillingResponse billingResponse) {
        // Current provider is set but is not available
        if (currentProvider != null && billingResponse.getStatus() == BILLING_UNAVAILABLE
                // However last setup attempt was successful
                && setupResponse != null && setupResponse.isSuccessful()
                // Auto-recovery is set
                && configuration.autoRecover()) {
            // Attempt to pick new billing provider
            ASIab.setup();
        }
    }
}
