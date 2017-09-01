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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.appscatter.iab.core.verification.VerificationResult.ERROR;
import static com.appscatter.iab.core.verification.VerificationResult.FAILED;
import static com.appscatter.iab.core.verification.VerificationResult.SUCCESS;
import static com.appscatter.iab.core.verification.VerificationResult.UNKNOWN;

@Retention(RetentionPolicy.SOURCE)
@IntDef({SUCCESS, FAILED, ERROR, UNKNOWN})

public @interface VerificationResult {

    /**
     * Purchase is legitimate.
     */
    int SUCCESS = 0;
    /**
     * Purchase is fake and wasn't acquired legitimately.
     */
    int FAILED = 1;
    /**
     * There was an error during verification process.
     */
    int ERROR = 2;
    /**
     * No result returned
     */
    int UNKNOWN = 3;
}
