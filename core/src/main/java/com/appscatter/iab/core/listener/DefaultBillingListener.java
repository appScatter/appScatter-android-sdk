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

package com.appscatter.iab.core.listener;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.model.Configuration;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.core.model.event.billing.InventoryResponse;
import com.appscatter.iab.core.model.event.billing.PurchaseResponse;
import com.appscatter.iab.core.verification.VerificationResult;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Default implementation of {@link BillingListener} interface.
 * <p>
 * Intended to be used in {@link Configuration.Builder#setBillingListener(BillingListener)}.
 * <p>
 * Implements following features:
 * <ul>
 *  <li>Attempts to consume all verified consumable purchases.
 *  <li>Attempts to fully load user's inventory via subsequent calls to {@link IabHelper#inventory(boolean)}.
 * </ul>
 */
public class DefaultBillingListener extends SimpleBillingListener {

    /**
     * Lazy initialized {@link IabHelper} instance used for any billing actions performed by
     * {@link DefaultBillingListener}.
     */
    @Nullable
    private IabHelper iabHelper;

    @NonNull
    protected IabHelper getHelper() {
        if (iabHelper == null) {
            iabHelper = ASIab.getAdvancedHelper();
        }
        return iabHelper;
    }

    /**
     * Checks if supplied purchase can be consumed.
     *
     * @param purchase Purchase object to check.
     * @return True if purchase can be consumed, false otherwise.
     * @see IabHelper#consume(Purchase)
     */
    protected boolean canConsume(@Nullable final Purchase purchase) {
        return purchase != null && purchase.getType() == SkuType.CONSUMABLE;
    }

    @Override
    public void onPurchase(@NonNull final PurchaseResponse purchaseResponse) {
        super.onPurchase(purchaseResponse);
        final Purchase purchase;
        if (purchaseResponse.isSuccessful()
                && canConsume(purchase = purchaseResponse.getPurchase())) {
            //noinspection ConstantConditions
            getHelper().consume(purchase);
        }
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    @Override
    public void onInventory(@NonNull final InventoryResponse inventoryResponse) {
        super.onInventory(inventoryResponse);
        if (inventoryResponse.isSuccessful()) {
            // Inventory request was successful
            final Map<Purchase, Integer> inventory = inventoryResponse.getInventory();
            for (final Map.Entry<Purchase, Integer> entry : inventory.entrySet()) {
                @VerificationResult final int verificationResult = entry.getValue();
                final Purchase purchase;
                if (verificationResult == VerificationResult.SUCCESS
                        && canConsume(purchase = entry.getKey())) {
                    getHelper().consume(purchase);
                }
            }
            // Load next batch if there's more
            if (inventoryResponse.hasMore()) {
                getHelper().inventory(false);
            }
        }
    }
}
