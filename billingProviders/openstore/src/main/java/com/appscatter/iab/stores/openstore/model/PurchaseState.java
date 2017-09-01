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

package com.appscatter.iab.stores.openstore.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.stores.openstore.model.PurchaseState.CANCELED;
import static com.appscatter.iab.stores.openstore.model.PurchaseState.PURCHASED;
import static com.appscatter.iab.stores.openstore.model.PurchaseState.REFUNDED;
import static com.appscatter.iab.stores.openstore.model.PurchaseState.UNKNOWN;

@Retention(RetentionPolicy.SOURCE)
@IntDef({PURCHASED, CANCELED, REFUNDED, UNKNOWN})
public @interface PurchaseState {

    int PURCHASED = 0;
    int CANCELED = 1;
    int REFUNDED = 2;
    int UNKNOWN = 3;

}
