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

package com.appscatter.iab.core.model.event.billing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.core.model.event.billing.BillingEventType.CONSUME;
import static com.appscatter.iab.core.model.event.billing.BillingEventType.INVENTORY;
import static com.appscatter.iab.core.model.event.billing.BillingEventType.PURCHASE;
import static com.appscatter.iab.core.model.event.billing.BillingEventType.SKU_DETAILS;

/**
 * Type of billing event.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({CONSUME, PURCHASE, SKU_DETAILS, INVENTORY})
public @interface BillingEventType {
    int CONSUME = 0;
    int PURCHASE = 1;
    int SKU_DETAILS = 2;
    int INVENTORY = 3;
}
