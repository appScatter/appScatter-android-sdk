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

package com.appscatter.iab.stores.samsung;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.stores.samsung.BillingMode.PRODUCTION;
import static com.appscatter.iab.stores.samsung.BillingMode.TEST_FAIL;
import static com.appscatter.iab.stores.samsung.BillingMode.TEST_SUCCESS;
import static com.appscatter.iab.stores.samsung.BillingMode.UNKNOWN;

@Retention(RetentionPolicy.SOURCE)
@IntDef({TEST_SUCCESS, TEST_FAIL, PRODUCTION, UNKNOWN})
public @interface BillingMode {

    int TEST_SUCCESS = 1;
    int TEST_FAIL = -1;
    int PRODUCTION = 0;
    int UNKNOWN = 2;
}
