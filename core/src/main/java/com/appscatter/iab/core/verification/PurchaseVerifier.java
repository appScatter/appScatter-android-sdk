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

package com.appscatter.iab.core.verification;

import com.appscatter.iab.core.model.billing.Purchase;

import android.support.annotation.NonNull;

/**
 * Interface intended to define purchase verification algorithm.
 * <p>
 * Verification process is supposed to confirm the fact that user acquired purchase legitimately.
 * <p>
 * Typically some cryptography checks or external server requests.
 */
public interface PurchaseVerifier {

    /**
     * Default implementation of {@link PurchaseVerifier} which always return
     * {@link VerificationResult#SUCCESS} from {@link #verify(Purchase)} call.
     */
    @NonNull
    PurchaseVerifier DEFAULT = new PurchaseVerifier() {

        @VerificationResult
        @Override
        public int verify(@NonNull final Purchase purchase) {
            return VerificationResult.SUCCESS;
        }
    };

    /**
     * Attempts to verify that purchase is owned by user.
     * <p>
     * Intended to be called from background thread, because it may perform long time operations.
     *
     * @param purchase Purchase object to verify.
     * @return Verification result.
     */
    @VerificationResult
    int verify(@NonNull final Purchase purchase);
}
