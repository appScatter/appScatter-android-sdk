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

package com.appscatter.iab.core.model.billing;

import com.appscatter.iab.core.model.JsonModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;


public class IapProductDetail extends JsonModel {

    private static final String GLOBAL_PRODUCT_ID = "global_id";
    private static final String PRODUCT_ID = "id";
    private static final String PRODUCT_TITLE = "title";
    private static final String PRODUCT_DESCRIPTION = "description";
    private static final String PRODUCT_PRICE = "price";
    private static final String PRODUCT_TYPE = "type";
    private static final String PRODUCT_TOKEN = "token";
    private static final String PRODUCT_LOCALIZATION = "localization";
    //    protected static final String PRODUCT_LOCALIZATION_COUNTRY = "country";
    private static final String PRODUCT_LOCALIZATION_LOCALE = "locale";

    //product id used on the app
    private String globalProductId;
    //product id
    private String productId;
    //title
    private String baseTitle;
    private final HashMap<String, String> localeToTitleMap = new HashMap<String, String>();
    //description
    private String baseDescription;
    private final HashMap<String, String> localeToDescriptionMap = new HashMap<String, String>();
    //price
    private float basePrice;
    private final HashMap<String, Float> localeToPrice = new HashMap<String, Float>();
    private int productType;
    private String productToken;

    public IapProductDetail(@NonNull final String originalJson) throws JSONException {
        super(originalJson);
        init();
    }

    public IapProductDetail(@NonNull final JSONObject jsonObj) throws JSONException {
        super(jsonObj);
        init();
    }

    private void init() throws JSONException {

        this.globalProductId = jsonObject.getString(GLOBAL_PRODUCT_ID);
        this.productId = jsonObject.getString(PRODUCT_ID);
        this.baseTitle = jsonObject.getString(PRODUCT_TITLE);
        this.baseDescription = jsonObject.getString(PRODUCT_DESCRIPTION);
        this.basePrice = Float.parseFloat(jsonObject.getString(PRODUCT_PRICE));
        this.productType = jsonObject.getInt(PRODUCT_TYPE);
        this.productToken = jsonObject.optString(PRODUCT_TOKEN);

        JSONArray jsonArray = jsonObject.getJSONArray(PRODUCT_LOCALIZATION);
        String localeString;
        JSONObject localeObject;
        Locale locale;

        if (jsonArray != null) {
            for(int x= 0; x < jsonArray.length(); x++) {
                localeObject = jsonArray.getJSONObject(x);
                localeString = localeObject.getString(PRODUCT_LOCALIZATION_LOCALE);
                if (localeString != null && !localeString.isEmpty()) {
                    locale = new Locale(localeString);

                    this.localeToTitleMap.put(localeString, localeObject.has(PRODUCT_TITLE) ? localeObject.getString(PRODUCT_TITLE) : this.baseTitle);
                    this.localeToDescriptionMap.put(localeString, localeObject.has(PRODUCT_DESCRIPTION) ? localeObject.getString(PRODUCT_DESCRIPTION) : this.baseDescription);
                    this.localeToPrice.put(locale.getCountry(), localeObject.has(PRODUCT_PRICE) ? Float.parseFloat(localeObject.getString(PRODUCT_PRICE)) : this.basePrice);
                }
            }
        }

    }

    public IapProductDetail(@NonNull IapProductDetail otherProduct) throws JSONException {
        super(otherProduct.getOriginalJson());

        this.globalProductId = otherProduct.globalProductId;
        this.productType = otherProduct.productType;
        this.productId = otherProduct.productId;
        this.productToken = otherProduct.productToken;
        this.baseTitle = otherProduct.baseTitle;
        this.baseDescription = otherProduct.baseDescription;
        this.basePrice = otherProduct.basePrice;
        this.localeToTitleMap.putAll(otherProduct.localeToTitleMap);
        this.localeToDescriptionMap.putAll(otherProduct.localeToDescriptionMap);
        this.localeToPrice.putAll(otherProduct.localeToPrice);
    }

    public String getGlobalProductId() {
        return globalProductId;
    }

    public void setGlobalProductId(String globalProductId) {
        this.globalProductId = globalProductId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBaseTitle() {
        return baseTitle;
    }

    public void setBaseTitle(String baseTitle) {
        this.baseTitle = baseTitle;
    }

    public String getBaseDescription() {
        return baseDescription;
    }

    public void setBaseDescription(String baseDescription) {
        this.baseDescription = baseDescription;
    }

    public void addTitleLocalization(String locale, String title) {
        localeToTitleMap.put(locale, title);
    }

    public String getTitleByLocale(String locale) {
        String mapValue = localeToTitleMap.get(locale);
        if (!TextUtils.isEmpty(mapValue)) {
            return mapValue;
        } else {
            return baseTitle;
        }
    }

    public String getTitle() {
        return getTitleByLocale(Locale.getDefault().toString());
    }

    public void addDescriptionLocalization(String locale, String description) {
        localeToDescriptionMap.put(locale, description);
    }

    public String getDescriptionByLocale(String locale) {
        String mapValue = localeToDescriptionMap.get(locale);
        if (!TextUtils.isEmpty(mapValue)) {
            return mapValue;
        } else {
            return baseDescription;
        }
    }

    public String getDescription() {
        return getDescriptionByLocale(Locale.getDefault().toString());
    }

    public void addCountryPrice(String countryCode, float price) {
        localeToPrice.put(countryCode, price);
    }

    public float getPriceByCountryCode(String countryCode) {
        Float mapValue = localeToPrice.get(countryCode);
        if (mapValue != null) {
            return mapValue;
        } else {
            return basePrice;
        }
    }

    public String getPriceDetails() {
        Locale defaultLocale = Locale.getDefault();
        Float mapValue = localeToPrice.get(defaultLocale.getCountry());
        float price = mapValue != null ? mapValue : basePrice;
        String symbol = mapValue != null ? Currency.getInstance(defaultLocale).getSymbol() : Currency.getInstance(Locale.US).getSymbol();
        return String.format("%.2f %s", price, symbol);
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    public void validateItem() {
        //todo add own string builder with dividers
        StringBuilder builder = getValidateInfo();
        if (builder.length() > 0) {
            throw new IllegalStateException("in-app product is not valid: " + builder.toString());
        }
    }

    @NonNull
    protected StringBuilder getValidateInfo() {
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(globalProductId)) {
            builder.append("global product id is empty");
        }
        if (TextUtils.isEmpty(productId)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("product id is empty");
        }
        if (TextUtils.isEmpty(baseTitle)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("base title is empty");
        }
        if (TextUtils.isEmpty(baseDescription)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("base description is empty");
        }
        if (basePrice == 0) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("base price is not defined");
        }
        return builder;
    }

    @Override
    public String toString() {
        return "InappBaseProduct{" +
                ", globalProductId='" + globalProductId + '\'' +
                ", productId='" + productId + '\'' +
                ", baseTitle='" + baseTitle + '\'' +
                ", productType='" + productType + '\'' +
                ", productToken='" + productToken + '\'' +
                ", localeToTitleMap=" + localeToTitleMap +
                ", baseDescription='" + baseDescription + '\'' +
                ", localeToDescriptionMap=" + localeToDescriptionMap +
//                ", autoFill=" + autoFill +
                ", basePrice=" + basePrice +
                ", localeToPrice=" + localeToPrice +
                '}';
    }

    public @SkuType int getProductType() {
        return productType;
    }

    public String getProductToken() {
        return productToken;
    }
}
