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

package com.appscatter.iab.stores.samsung.model;

import org.json.JSONException;

import android.support.annotation.NonNull;

/**
 * Model representing purchased item returned in user inventory.
 */
public class SamsungPurchase extends SamsungBillingModel {

    protected static final String KEY_VERIFY_URL = "mVerifyUrl";


    @NonNull
    protected final String verifyUrl;

    public SamsungPurchase(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        this.verifyUrl = jsonObject.getString(KEY_VERIFY_URL);
    }

    @NonNull
    public String getVerifyUrl() {
        return verifyUrl;
    }
}
