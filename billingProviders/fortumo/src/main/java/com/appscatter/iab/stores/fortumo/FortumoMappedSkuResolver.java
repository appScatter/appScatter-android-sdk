/*
 * Copyright (c) 2016. AppScatter
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

package com.appscatter.iab.stores.fortumo;

import com.appscatter.iab.core.sku.TypedMapSkuResolver;
import com.appscatter.iab.stores.fortumo.model.FortumoSkuDetails;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FortumoMappedSkuResolver extends TypedMapSkuResolver {

    private final Map<String, FortumoSkuDetails> products = new HashMap<>();

    /**
     * Adds SKU mapping with corresponding SKU type.
     * @param sku Original SKU.
     * @param resolvedProduct the {@link FortumoSkuDetails} product that will match the original SKU
     */
    public void add(@NonNull final String sku, @Nullable final FortumoSkuDetails resolvedProduct) {
        super.add(sku, resolvedProduct.getProductId(), resolvedProduct.getItemType());
        products.put(sku, resolvedProduct);
    }

    /**
     * Gets the Fortumo product that matches the given original SKU
     * @param sku Original SKU
     * @return the {@link FortumoSkuDetails} product associated with that sku
     */
    public FortumoSkuDetails getMapedProduct(@NonNull final String sku) {
        return products.get(sku);
    }

    /**
     * Gets the Fortumo product that matches the given product id
     * @param productId the id of the Fortumo product to retreive
     * @return the {@link FortumoSkuDetails} product associated with that sku
     */
    public FortumoSkuDetails getProduct(@NonNull final String productId) {
        return products.get(revert(productId));
    }


    /**
     * Retrieves all the products associated with the Fortumo store
     * @return A @{@link Map} of products for Fortumo
     */
    @NonNull
    public Map<String, FortumoSkuDetails> getProducts() {
        return products;
    }



}
