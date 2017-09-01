/*
 * Copyright (c) 2016. AppScatter
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

package com.appscatter.iab.stores.fortumo.model;

import com.appscatter.iab.core.model.billing.SkuDetails;
import com.appscatter.iab.core.model.billing.SkuType;
import com.appscatter.iab.stores.fortumo.FortumoBillingProvider;

import android.support.annotation.NonNull;

public class FortumoSkuDetails extends FortumoModel {


    protected @SkuType final int itemType;
    @NonNull
    protected final String price;
    @NonNull
    protected final String currency;
    @NonNull
    protected final String title;
    @NonNull
    protected final String description;

    @NonNull
    protected final String serviceId;
    @NonNull
    protected final String inAppSecret;

    private FortumoSkuDetails(@NonNull Builder builder) {
        productId = builder.serviceId;
        itemType = builder.itemType;
        price = builder.price;
        currency = builder.currency;
        title = builder.title;
        description = builder.description;
        serviceId = builder.serviceId;
        inAppSecret = builder.inAppSecret;
    }


    @SkuType
    public int getItemType() {
        return itemType;
    }

    @NonNull
    public String getPrice() {
        return price;
    }

    @NonNull
    public String getCurrency() {
        return currency;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getInAppSecret() {
        return inAppSecret;
    }

    @NonNull
    public String getServiceId() {
        return serviceId;
    }

    public SkuDetails toSkuDetails() {
        return new SkuDetails.Builder(this.serviceId)
                .setType(this.itemType)
                .setProviderName(FortumoBillingProvider.NAME)
                .setOriginalJson("")
                .setPrice(this.price)
                .setTitle(this.title)
                .setDescription(this.description)
                .build();
    }

    public static class Builder {
        @SkuType
        private int itemType;
        @NonNull
        private String price;
        @NonNull
        private String currency;
        @NonNull
        private String title;
        @NonNull
        private String description;
        @NonNull
        private String serviceId;
        @NonNull
        private String inAppSecret;

        public FortumoSkuDetails build() {
            return new FortumoSkuDetails(this);
        }

        public Builder setItemType(@SkuType int itemType) {
            this.itemType = itemType;
            return this;
        }

        public Builder setPrice(@NonNull String price) {
            this.price = price;
            return this;
        }

        public Builder setCurrency(@NonNull String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setTitle(@NonNull String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(@NonNull String description) {
            this.description = description;
            return this;
        }

        public Builder setServiceId(@NonNull String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder setInAppSecret(@NonNull String inAppSecret) {
            this.inAppSecret = inAppSecret;
            return this;
        }

    }
}
