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

import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.ConsumeRequest;
import com.appscatter.iab.core.model.event.billing.InventoryRequest;
import com.appscatter.iab.core.model.event.billing.PurchaseRequest;
import com.appscatter.iab.core.model.event.billing.SkuDetailsRequest;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The Very basic {@link IabHelper} implementation.
 * <p>
 * Responsible for {@link BillingRequest}s creation and sending them to {@link BillingBase}.
 */
class IabHelperImpl implements IabHelper {

    protected final BillingBase billingBase = BillingBase.getInstance();

    IabHelperImpl() {
        super();
    }

    /**
     * Sends supplied billing request for execution.
     *
     * @param billingRequest BillingRequest to execute.
     * @see BillingBase
     */
    protected void postRequest(@NonNull final BillingRequest billingRequest) {
        billingBase.postRequest(billingRequest);
    }

    @Override
    public void purchase(@NonNull final String sku) {
        postRequest(new PurchaseRequest(sku));
    }

    @Override
    public void consume(@NonNull final Purchase purchase) {
        postRequest(new ConsumeRequest(purchase));
    }

    @Override
    public void inventory(final boolean startOver) {
        postRequest(new InventoryRequest(startOver));
    }

    @Override
    public void skuDetails(@NonNull final Set<String> skus) {
        postRequest(new SkuDetailsRequest(skus));
    }

    @Override
    public final void skuDetails(@NonNull final String... skus) {
        skuDetails(new HashSet<>(Arrays.asList(skus)));
    }
}
