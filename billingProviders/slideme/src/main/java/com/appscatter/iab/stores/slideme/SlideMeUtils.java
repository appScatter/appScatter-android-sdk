/*
 * Copyright (c) 2017. AppScatter
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

package com.appscatter.iab.stores.slideme;

import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.sku.TypedSkuResolver;
import com.slideme.sam.manager.inapp.ListResult;
import com.slideme.sam.manager.inapp.Product;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SlideMeUtils {

    /**
     * Transforms Amazon product into library SKU details model.
     *
     * @param product Amazon product to transform.
     *
     * @return Newly constructed SkuDetails object.
     */
    @Nullable
    public static SkuDetails convertSkuDetails(@NonNull final Product product, TypedSkuResolver skuResolver) {
        final String originalJson;
        originalJson = product.toString();

        final SkuDetails.Builder builder = new SkuDetails.Builder(product.id);
        final int productType = skuResolver.resolveType(product.type);
        builder.setType(productType);
        builder.setTitle(product.title);
        builder.setDescription(product.description);
        builder.setPrice(String.valueOf(product.price));
        builder.setProviderName(SlideMeBillingProvider.NAME);
        builder.setOriginalJson(originalJson);

        return builder.build();
    }


    @NonNull
    public static Collection<SkuDetails> getSkusDetails(@NonNull final ListResult response, TypedSkuResolver skuResolver) {

        final Collection<SkuDetails> skusDetails = new ArrayList<>();
        final List<Product> products = response.products;
        for (final Product product : products) {
            final SkuDetails skuDetails = convertSkuDetails(product, skuResolver);
            if (skuDetails != null) {
                skusDetails.add(skuDetails);
            }
        }

        return skusDetails;
    }


}
