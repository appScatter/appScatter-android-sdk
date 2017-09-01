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

package com.appscatter.iab.core.model.billing;

import com.appscatter.iab.core.model.JsonModel;

import org.json.JSONArray;
import org.json.JSONException;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class IapProductList extends JsonModel {

    private static final String HASH_KEY = "hash";
    private static final String PRODUCTS = "products";
    private static final String PROVIDED_ID = "provided_id";

    private String hash;
    private String providedId;
    private final ArrayList<IapProductDetail> mProductDetails = new ArrayList<>();

    public IapProductList(@NonNull final String originalJson) throws JSONException {
        super(originalJson);

        this.hash = jsonObject.getString(HASH_KEY);
        this.providedId = jsonObject.optString(PROVIDED_ID);
        JSONArray jArray = jsonObject.getJSONArray(PRODUCTS);
        if (jArray != null) {
            for (int i=0;i<jArray.length();i++){
                mProductDetails.add(new IapProductDetail(jArray.getJSONObject(i)));
            }
        }
    }

    public String getHash() {
        return hash;
    }

    public ArrayList<IapProductDetail> getProductDetails() {
        return mProductDetails;
    }

    public String getProvidedId() {
        return providedId;
    }
}
