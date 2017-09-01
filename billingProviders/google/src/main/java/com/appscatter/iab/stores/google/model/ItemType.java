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

package com.appscatter.iab.stores.google.model;

import com.appscatter.iab.core.model.billing.SkuType;

import android.support.annotation.Nullable;

import static com.appscatter.iab.core.model.billing.SkuType.CONSUMABLE;
import static com.appscatter.iab.core.model.billing.SkuType.ENTITLEMENT;

public enum ItemType {

    SUBSCRIPTION("subs"),
    /**
     * Google does not distinguish consumables and entitlements. Entitlement is a consumable which
     * is never consumed.
     */
    CONSUMABLE_OR_ENTITLEMENT("inapp");

    /**
     * Gets Google product type from code.
     *
     * @param code Code of product type.
     *
     * @return Product type if code is recognized, null otherwise.
     */
    @Nullable
    public static ItemType fromCode(@Nullable final String code) {
        if (code == null) {
            return null;
        }
        for (final ItemType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets Google product type from SKU type.
     *
     * @param skuType SKU type to convert.
     *
     * @return Product type if SKU type was recognized, null otherwise.
     */
    @Nullable
    public static ItemType fromSkuType(@SkuType final int skuType) {
        switch (skuType) {
            case SkuType.SUBSCRIPTION:
                return SUBSCRIPTION;
            case CONSUMABLE:
            case ENTITLEMENT:
                return CONSUMABLE_OR_ENTITLEMENT;
            default:
                return null;
        }
    }

    private final String code;

    ItemType(final String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
