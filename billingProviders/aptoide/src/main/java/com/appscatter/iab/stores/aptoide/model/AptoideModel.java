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

package com.appscatter.iab.stores.aptoide.model;

import com.appscatter.iab.core.model.JsonModel;

import org.json.JSONException;

import android.support.annotation.NonNull;

public class AptoideModel extends JsonModel {
    protected static final String NAME_PRODUCT_ID = "productId";


    @NonNull
    protected final String productId;

    protected AptoideModel(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.productId = jsonObject.getString(NAME_PRODUCT_ID);
    }

    /**
     * Gets item's product identifier. Every item has a product ID which you must specify in the
     * application's product list in the Aptoide Developer Console.
     *
     * @return Unique product ID, can't be null.
     */
    @NonNull
    public String getProductId() {
        return productId;
    }

}
