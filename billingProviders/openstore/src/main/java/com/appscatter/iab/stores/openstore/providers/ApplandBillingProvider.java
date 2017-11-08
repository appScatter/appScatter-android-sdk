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

package com.appscatter.iab.stores.openstore.providers;

import com.appscatter.iab.core.billing.BaseBillingProvider;
import com.appscatter.iab.core.sku.SkuResolver;
import com.appscatter.iab.core.sku.TypedSkuResolver;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.stores.openstore.OpenStoreIntentMaker;
import com.appscatter.iab.stores.openstore.OpenStoreUtils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ApplandBillingProvider extends OpenStoreBillingProvider {

    public static final String NAME = "APPLAND";
    protected static final String[] PACKAGES = new String[]{"se.appland.market.android"};


    public ApplandBillingProvider(@NonNull final Context context,
            @NonNull final TypedSkuResolver skuResolver,
            @NonNull final PurchaseVerifier purchaseVerifier,
            @Nullable final OpenStoreIntentMaker intentMaker) {
        super(context, skuResolver, purchaseVerifier, intentMaker);
    }

    @SuppressWarnings("unused")
    public ApplandBillingProvider(){
        super();
    }

    @Override
    public <C, R extends SkuResolver, V extends PurchaseVerifier> void init(@NonNull C context,
            @NonNull R skuResolver, @NonNull V purchaseVerifier) {
        super.init(context, skuResolver, purchaseVerifier);
        this.intentMaker = OpenStoreUtils.getIntentMaker(NAME, PACKAGES);
    }

    public static class Builder extends OpenStoreBuilder<Builder> {

        public Builder(@NonNull final Context context) {
            super(context);
            setIntentMaker(OpenStoreUtils.getIntentMaker(NAME, PACKAGES));
        }

        @Override
        public BaseBillingProvider build() {
            if (skuResolver == null) {
                throw new IllegalStateException();
            }
            return new ApplandBillingProvider(context, skuResolver,
                    purchaseVerifier == null ? PurchaseVerifier.DEFAULT : purchaseVerifier,
                    intentMaker);
        }
    }
}