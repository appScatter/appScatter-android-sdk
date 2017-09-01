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

import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.core.verification.PurchaseVerifier;
import com.appscatter.iab.core.verification.VerificationResult;
import com.appscatter.iab.stores.samsung.model.SamsungVerification;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;

import org.json.JSONException;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SamsungPurchaseVerifier implements PurchaseVerifier {

    protected static final int TIMEOUT = 3000;
    protected static final String VERIFY_URL
            = "https://iap.samsungapps.com/iap/appsItemVerifyIAPReceipt.as?protocolVersion=2.0"
            + "&purchaseID=";


    @NonNull
    protected final Context context;

    protected @BillingMode final int billingMode;

    public SamsungPurchaseVerifier(@NonNull final Context context,
            @BillingMode final int billingMode) {
        this.context = context;
        this.billingMode = billingMode;
    }

    @VerificationResult
    @Override
    public int verify(@NonNull final Purchase purchase) {
        if (!ASUtils.isConnected(context)) {
            ASLog.e("Can't verify purchase, no connection.");
            return VerificationResult.ERROR;
        }
        try {
            final HttpURLConnection connection = (HttpURLConnection)
                    new URL(VERIFY_URL + purchase.getToken()).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.connect();
            final int responseCode = connection.getResponseCode();
            ASLog.d("Verify response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return VerificationResult.ERROR;
            }
            final String body = ASIabUtils.toString(connection.getInputStream());
            final SamsungVerification verification = new SamsungVerification(body);
            ASLog.d("Samsung verification: " + verification);
            return verification.isStatus()  && verification.getMode() == this.billingMode
                    ? VerificationResult.SUCCESS : VerificationResult.FAILED;
        } catch (IOException | JSONException exception) {
            ASLog.e("", exception);
        }
        return VerificationResult.ERROR;
    }
}
