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

import static com.appscatter.iab.stores.samsung.Response.ERROR_ALREADY_PURCHASED;
import static com.appscatter.iab.stores.samsung.Response.ERROR_COMMON;
import static com.appscatter.iab.stores.samsung.Response.ERROR_CONFIRM_INBOX;
import static com.appscatter.iab.stores.samsung.Response.ERROR_CONNECT_TIMEOUT;
import static com.appscatter.iab.stores.samsung.Response.ERROR_INITIALIZATION;
import static com.appscatter.iab.stores.samsung.Response.ERROR_IOEXCEPTION_ERROR;
import static com.appscatter.iab.stores.samsung.Response.ERROR_ITEM_GROUP_ID_DOES_NOT_EXIST;
import static com.appscatter.iab.stores.samsung.Response.ERROR_NEED_APP_UPGRADE;
import static com.appscatter.iab.stores.samsung.Response.ERROR_NETWORK_NOT_AVAILABLE;
import static com.appscatter.iab.stores.samsung.Response.ERROR_NONE;
import static com.appscatter.iab.stores.samsung.Response.ERROR_PRODUCT_DOES_NOT_EXIST;
import static com.appscatter.iab.stores.samsung.Response.ERROR_SOCKET_TIMEOUT;
import static com.appscatter.iab.stores.samsung.Response.ERROR_UNKNOWN;
import static com.appscatter.iab.stores.samsung.Response.ERROR_WHILE_RUNNING;
import static com.appscatter.iab.stores.samsung.Response.PAYMENT_IS_CANCELED;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ERROR_NONE, PAYMENT_IS_CANCELED, ERROR_INITIALIZATION, ERROR_NEED_APP_UPGRADE, ERROR_COMMON, ERROR_ALREADY_PURCHASED, ERROR_WHILE_RUNNING,
        ERROR_PRODUCT_DOES_NOT_EXIST, ERROR_CONFIRM_INBOX, ERROR_ITEM_GROUP_ID_DOES_NOT_EXIST, ERROR_NETWORK_NOT_AVAILABLE, ERROR_IOEXCEPTION_ERROR,
        ERROR_SOCKET_TIMEOUT, ERROR_CONNECT_TIMEOUT, ERROR_UNKNOWN})
@SuppressWarnings("MagicNumber")
public @interface Response {
    int ERROR_NONE = 0;
    int PAYMENT_IS_CANCELED = 1;
    int ERROR_INITIALIZATION = -1000;
    int ERROR_NEED_APP_UPGRADE = -1001;
    int ERROR_COMMON = -1002;
    int ERROR_ALREADY_PURCHASED = -1003;
    int ERROR_WHILE_RUNNING = -1004;
    int ERROR_PRODUCT_DOES_NOT_EXIST = -1005;
    int ERROR_CONFIRM_INBOX = -1006;
    int ERROR_ITEM_GROUP_ID_DOES_NOT_EXIST = -1007;
    int ERROR_NETWORK_NOT_AVAILABLE = -1008;
    int ERROR_IOEXCEPTION_ERROR = -1009;
    int ERROR_SOCKET_TIMEOUT = -1010;
    int ERROR_CONNECT_TIMEOUT = -1011;
    int ERROR_UNKNOWN = -2000;
}
