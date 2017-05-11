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

package com.appscatter.iab.core.model.billing;

/**
 * SKU type represented by {@link BillingModel}.
 */

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.core.model.billing.SkuType.CONSUMABLE;
import static com.appscatter.iab.core.model.billing.SkuType.ENTITLEMENT;
import static com.appscatter.iab.core.model.billing.SkuType.SUBSCRIPTION;
import static com.appscatter.iab.core.model.billing.SkuType.UNAVAILABLE;
import static com.appscatter.iab.core.model.billing.SkuType.UNKNOWN;

@Retention(RetentionPolicy.SOURCE)
@IntDef({CONSUMABLE, ENTITLEMENT, SUBSCRIPTION, UNKNOWN, UNAVAILABLE})
public @interface SkuType {
    int CONSUMABLE = 0;
    int ENTITLEMENT = 1;
    int SUBSCRIPTION = 2;
    int UNKNOWN = 3;
    int UNAVAILABLE = 4;
}
