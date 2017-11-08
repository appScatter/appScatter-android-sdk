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

import com.appscatter.iab.core.model.billing.SkuType;

import android.support.annotation.Nullable;

import static com.appscatter.iab.core.model.billing.SkuType.CONSUMABLE;
import static com.appscatter.iab.core.model.billing.SkuType.ENTITLEMENT;

public enum ItemType {
/**
 * The subscription option is commented out, because, at the moment, Aptoide does not support subscription type products.
 * If in the future this changes, this needs to be re-enabled and the appropriate code needs to be re-added
 */
//    SUBSCRIPTION("subs"),
    /**
     * Aptoide does not distinguish consumables and entitlements. Entitlement is a consumable which
     * is never consumed.
     */
    CONSUMABLE_OR_ENTITLEMENT("inapp");

    /**
     * Gets Aptoide product type from code.
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
     * Gets Aptoide product type from SKU type.
     * Only Consumable and Entitlement products are supported.
     * Currently Aptoide does not support subscription type products
     *
     * @param skuType SKU type to convert.
     *
     * @return Product type if SKU type was recognized, null otherwise.
     */
    @Nullable
    public static ItemType fromSkuType(@SkuType final int skuType) {
        switch (skuType) {
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
