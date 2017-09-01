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

package com.appscatter.iab.core.api;

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsResponse;

import android.support.annotation.NonNull;

import java.util.Set;

public interface IabHelper {

    /**
     * Sends {@link PurchaseRequest} to current {@link BillingProvider}.
     *
     * @param sku Stock Keeping Unit - unique product ID to purchase.
     * @see PurchaseResponse
     * @see Purchase
     */
    void purchase(@NonNull final String sku);

    /**
     * Sends {@link ConsumeRequest} to current {@link BillingProvider}.
     *
     * @param purchase Purchase object previously retrieved from {@link PurchaseResponse} or {@link InventoryResponse}.
     * @see Purchase
     */
    void consume(@NonNull final Purchase purchase);

    /**
     * Sends {@link InventoryRequest} to current {@link BillingProvider}.
     * <p>
     * For the sake of performance, large inventory might not be loaded within one request.
     * Maximum number of items queried per request depends on {@link BillingProvider} implementation.
     *
     * @param startOver Flag indicating weather library should load inventory from the start,
     *                  or continue from the point of last successful request.
     * @see InventoryResponse
     * @see InventoryResponse#hasMore()
     */
    void inventory(final boolean startOver);

    /**
     * Sends {@link SkuDetailsRequest} to current {@link BillingProvider}.
     *
     * @param skus Stock Keeping Units - unique product IDs to query details for.
     * @see SkuDetailsResponse
     * @see SkuDetails
     */
    void skuDetails(@NonNull final Set<String> skus);

    /**
     * Same as {@link #skuDetails(java.util.Set)}.
     */
    void skuDetails(@NonNull final String... skus);

}
