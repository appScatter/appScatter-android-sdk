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

package com.appscatter.iab.stores.aptoide;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.stores.aptoide.Response.BILLING_UNAVAILABLE;
import static com.appscatter.iab.stores.aptoide.Response.DEVELOPER_ERROR;
import static com.appscatter.iab.stores.aptoide.Response.ERROR;
import static com.appscatter.iab.stores.aptoide.Response.ITEM_ALREADY_OWNED;
import static com.appscatter.iab.stores.aptoide.Response.ITEM_NOT_OWNED;
import static com.appscatter.iab.stores.aptoide.Response.ITEM_UNAVAILABLE;
import static com.appscatter.iab.stores.aptoide.Response.OK;
import static com.appscatter.iab.stores.aptoide.Response.SERVICE_UNAVAILABLE;
import static com.appscatter.iab.stores.aptoide.Response.UNKNOWN;
import static com.appscatter.iab.stores.aptoide.Response.USER_CANCELED;

/**
 * All possible responses from Google service.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({OK, USER_CANCELED, SERVICE_UNAVAILABLE, BILLING_UNAVAILABLE, ITEM_UNAVAILABLE, DEVELOPER_ERROR, ERROR, ITEM_ALREADY_OWNED, ITEM_NOT_OWNED, UNKNOWN})

public @interface Response {
    int OK = 0;
    int USER_CANCELED = 1;
    int SERVICE_UNAVAILABLE = 2;
    int BILLING_UNAVAILABLE = 3;
    int ITEM_UNAVAILABLE = 4;
    int DEVELOPER_ERROR = 5;
    int ERROR = 6;
    int ITEM_ALREADY_OWNED = 7;
    int ITEM_NOT_OWNED = 8;
    int UNKNOWN = 9;
}
