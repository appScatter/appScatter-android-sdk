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

package com.appscatter.iab.core.billing;

import com.appscatter.iab.core.ASIab;

import com.appscatter.iab.core.model.billing.IapProductList;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.sku.SkuResolver;
import com.appscatter.iab.core.verification.PurchaseVerifier;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import com.appscatter.iab.core.model.event.RequestHandledEvent;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import android.app.Activity;
import android.content.Intent;

/**
 * This interface represents billing service provider, capable of handling in-app purchases.
 * <p>
 * All methods of this class should be suitable for calling from <b>single</b> background thread.
 */
public interface BillingProvider {

    /**
     * Get unique name of this BillingProvider to serve as identifier.
     *
     * @return this BillingProvider name;
     */
    @NonNull
    String getName();

    /**
     * Checks if Manifest contains all necessary entries.
     *
     * @throws IllegalStateException if manifest doesn't contain all necessary entries.
     */
    void checkManifest();

    @NonNull
    @Compatibility
    int checkCompatibility();

    /**
     * Indicates whether this provider is available on the system.
     * <p>
     * Called before each request, thus it might be a good idea to cache intermediate result.
     *
     * @return true if BillingProvider is currently available, false otherwise.
     */
    boolean isAvailable();

    /**
     * Get the permissions needed for the new permissions system for android.
     * Only the permissions with the protection level dangerous.
     * See more https://developer.android.com/reference/android/Manifest.permission.html
     *
     * @return object containing the configurations for the permissions request, null or empty list if no permissions required or if they are handled by the app
     */
    @Nullable
    List<String> getPermissionsList();

    /**
     * Entry point for all billing requests.
     * <p>
     * Called from <b>single</b> background thread.
     * <p>
     * As soon as billing request is handled, BillingProvider <b>must</b> notify library with
     * {@link RequestHandledEvent} using {@link ASIab#post(Object)}. Same method should be used
     * with proper {@link BillingResponse} object when result of performed action becomes available.
     *
     * @param billingRequest Billing request to handle with this billing provider.
     */
    void onBillingRequest(@NonNull final BillingRequest billingRequest);

    /**
     * Indicates whether supplied SkyType is supported by this billing provider.
     * <p>
     * Might be useful to update UI or adjust app logic.
     *
     * @param skuType SkuType to check.
     * @return True is supplied SkuType is supported by this billing provider, false otherwise.
     */
    boolean skuTypeSupported(@SkuType final int skuType);

    /**
     * Acquires {@link Intent} to open representation of this App within this BillingProvider.
     *
     * @return Intent object suitable for {@link Activity#startActivity(Intent)}. Can be null.
     */
    @Nullable
    Intent getStorePageIntent();

    /**
     * Acquires {@link Intent} to rate this App within this BillingProvider.
     *
     * @return Intent object suitable for {@link Activity#startActivity(Intent)}. Can be null.
     */
    @Nullable
    Intent getRateIntent();

    void registerForEvents();

    void unregisterForEvents();

    /**
     * For when using dynamic loading of the providers libraries
     *
     * @param key the key to use to validate the pruchase (provided by the billing provider)
     * @return the purchase verifier object to validate the response
     */
    @NonNull
    PurchaseVerifier getPurchaseVerifier(String key);

    /**
     * For when using dynamic loading of the providers libraries
     *
     * @return the SkuResolver object with the products
     */
    @NonNull
    SkuResolver getSkuResolver(@NonNull IapProductList products);

    <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull final C context,
            @NonNull final R skuResolver,
            @NonNull final V purchaseVerifier);
}

