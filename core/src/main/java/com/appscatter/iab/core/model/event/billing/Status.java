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

import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.billing.Purchase;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.appscatter.iab.core.model.event.billing.Status.BILLING_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.BUSY;
import static com.appscatter.iab.core.model.event.billing.Status.ERROR_NONE;
import static com.appscatter.iab.core.model.event.billing.Status.ITEM_ALREADY_OWNED;
import static com.appscatter.iab.core.model.event.billing.Status.ITEM_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.NO_BILLING_PROVIDER;
import static com.appscatter.iab.core.model.event.billing.Status.PENDING;
import static com.appscatter.iab.core.model.event.billing.Status.SERVICE_UNAVAILABLE;
import static com.appscatter.iab.core.model.event.billing.Status.SUCCESS;
import static com.appscatter.iab.core.model.event.billing.Status.UNAUTHORISED;
import static com.appscatter.iab.core.model.event.billing.Status.UNKNOWN_ERROR;
import static com.appscatter.iab.core.model.event.billing.Status.USER_CANCELED;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@IntDef({SUCCESS, PENDING, UNAUTHORISED, BUSY, USER_CANCELED, BILLING_UNAVAILABLE, NO_BILLING_PROVIDER, SERVICE_UNAVAILABLE, ITEM_UNAVAILABLE,
        ITEM_ALREADY_OWNED, UNKNOWN_ERROR, ERROR_NONE})
public @interface Status{
    /**
     * Everything is OK.
     */
    int SUCCESS = 0;
    /**
     * Request was handled successfully, but takes considerable time to process.
     */
    int PENDING = 1;
    /**
     * {@link BillingProvider} requires authorization.
     */
    int UNAUTHORISED = 2;
    /**
     * Library is busy with another request.
     */
    int BUSY = 3;
    /**
     * User canceled billing request.
     */
    int USER_CANCELED = 4;
    /**
     * {@link BillingProvider} reported that it can't handle billing.
     */
    int BILLING_UNAVAILABLE = 5;
    /**
     * Library has no working billing provider.
     */
    int NO_BILLING_PROVIDER = 6;
    /**
     * Request can't be handled at a time.
     * <p>
     * Most likely - connection went down.
     */
    int SERVICE_UNAVAILABLE = 7;
    /**
     * Requested sku is unavailable from current {@link BillingProvider}.
     */
    int ITEM_UNAVAILABLE = 8;
    /**
     * Item is already owned by user.
     * <p>
     * If it's {@link com.appscatter.iab.core.model.billing.SkuType#CONSUMABLE} - purchase must be consumed
     * using {@link IabHelper#consume(Purchase)}.
     */
    int ITEM_ALREADY_OWNED = 9;
    /**
     * For some reason {@link BillingProvider} refused to handle request.
     */
    int UNKNOWN_ERROR = 10;
    /**
     * For some reason {@link BillingProvider} returned an empty response.
     */
    int ERROR_NONE = 11;
}
