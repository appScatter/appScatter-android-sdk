/*
 * Copyright (c) 2017. AppScatter
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

package com.appscatter.iab.core.billing;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.core.billing.Providers.AMAZON;
import static com.appscatter.iab.core.billing.Providers.APPLAND;
import static com.appscatter.iab.core.billing.Providers.APTOIDE;
import static com.appscatter.iab.core.billing.Providers.GOOGLE;
import static com.appscatter.iab.core.billing.Providers.SAMSUNG;
import static com.appscatter.iab.core.billing.Providers.SLIDEME;
import static com.appscatter.iab.core.billing.Providers.YANDEX;

@Retention(RetentionPolicy.SOURCE)
@StringDef({GOOGLE, AMAZON, APTOIDE, SAMSUNG, APPLAND, SLIDEME, YANDEX})
public @interface Providers {
    String AMAZON = "amazon";
    String APTOIDE = "aptoide";
    String GOOGLE = "google";
    String SAMSUNG = "samsung";
    String SLIDEME = "slideme";
    String APPLAND = "appland";
    String YANDEX = "yandex";

    interface Namespaces {
        String AMAZON = "com.amazon.venezia";
        String APTOIDE = "cm.aptoide.pt";
        String GOOGLE = "com.android.vending";
        String SAMSUNG = "com.sec.android.app.samsungapps";
        String SLIDEME = "com.slideme.sam.manager";
        String APPLAND = "se.appland.market.android";
        String YANDEX = "com.yandex.store";
    }

    interface Classes {
        String AMAZON = "com.appscatter.iab.stores.amazon.AmazonBillingProvider";
        String APTOIDE = "com.appscatter.iab.stores.aptoide.AptoideBillingProvider";
        String GOOGLE = "com.appscatter.iab.stores.google.GoogleBillingProvider";
        String SAMSUNG = "com.appscatter.iab.stores.samsung.SamsungBillingProvider";
        String SLIDEME = "com.appscatter.iab.stores.slideme.SlidemeBillingProvider";
        String APPLAND = "com.appscatter.iab.stores.openstore.providers.ApplandBillingProvider";
        String YANDEX = "com.appscatter.iab.stores.openstore.providers.YandexBillingProvider";
    }
}

