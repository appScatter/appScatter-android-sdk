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

package com.appscatter.iab.core.sku;

import com.appscatter.iab.core.billing.BillingProvider;

import android.support.annotation.NonNull;

/**
 * Interface intended to help resolve {@link BillingProvider} specific SKUs.
 */
public interface SkuResolver {

    /**
     * Default implementation of {@link SkuResolver}.
     * <p>
     * Maps all SKUs to themselves.
     */
    @NonNull
    SkuResolver DEFAULT = new SkuResolver() {
        @NonNull
        @Override
        public String resolve(@NonNull final String sku) {
            return sku;
        }

        @NonNull
        @Override
        public String revert(@NonNull final String resolvedSku) {
            return resolvedSku;
        }
    };


    /**
     * Gets {@link BillingProvider} specific SKU value.
     *
     * @param sku SKU to resolve.
     * @return Resolved SKU value.
     */
    @NonNull
    String resolve(@NonNull final String sku);

    /**
     * Gets original SKU value from {@link BillingProvider} specific one.
     *
     * @param resolvedSku SKU to revert.
     * @return Reverted SKU value.
     */
    @NonNull
    String revert(@NonNull final String resolvedSku);
}
