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

package com.appscatter.iab.core.model;

import com.appscatter.iab.core.model.billing.BillingModel;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Constructor;

/**
 * Simple model class that represents java object created from JSON.
 */
public abstract class JsonModel implements JsonCompatible {

    @Nullable
    public static <E extends JsonModel> E fromOriginalJson(@NonNull final Class<E> clazz,
            @NonNull final BillingModel model) {
        try {
            final Constructor<E> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(model.getOriginalJson());
        } catch (Exception e) {
            // we can't catch separate exceptions here because ReflectiveOperationException
            // not available on pre KitKat devices
            // for more info, see https://code.google.com/p/android/issues/detail?id=153406
            ASLog.e("Can't create model class from original json", e);
        }
        return null;
    }


    @NonNull
    protected final JSONObject jsonObject;
    @NonNull
    private final String originalJson;

    public JsonModel(@NonNull final String originalJson)
            throws JSONException {
        this.jsonObject = new JSONObject(originalJson);
        this.originalJson = originalJson;
    }

    public JsonModel(@NonNull final JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        this.originalJson = jsonObject.toString();
    }

    /**
     * Gets JSON data associated with this model.
     *
     * @return JSON string.
     */
    @NonNull
    public String getOriginalJson() {
        return originalJson;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        return jsonObject;
    }

    @Override
    public String toString() {
        return ASIabUtils.toString(this);
    }
}
