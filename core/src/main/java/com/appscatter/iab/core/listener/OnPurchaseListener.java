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

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.event.billing.BillingResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;

import android.support.annotation.NonNull;

/**
 * Listener for purchase billing events.
 */
public interface OnPurchaseListener {

    /**
     * Called every time PurchaseResponse is sent by current {@link BillingProvider}.
     *
     * @param purchaseResponse {@link BillingResponse} object sent by BillingProvider.
     */
    void onPurchase(@NonNull final PurchaseResponse purchaseResponse);
}
