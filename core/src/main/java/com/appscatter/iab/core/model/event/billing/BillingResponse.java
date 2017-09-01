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

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.billing.BillingModel;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.appscatter.iab.core.model.event.billing.Status.PENDING;
import static com.appscatter.iab.core.model.event.billing.Status.SUCCESS;
import static org.json.JSONObject.NULL;

/**
 * Model class that represents response from {@link BillingProvider} for
 * corresponding {@link BillingRequest}.
 */
public abstract class BillingResponse extends BillingEvent {

    private static final String NAME_PROVIDER_NAME = "provider_info";
    private static final String NAME_STATUS = "status";

    private static final Collection<Integer> SUCCESSFUL = Arrays.asList(SUCCESS, PENDING);


    @Nullable
    private final String providerName;
    @NonNull
    private final @Status int status;

    protected BillingResponse(@BillingEventType final int type,
            @NonNull @Status final int status,
            @Nullable final String providerName) {
        super(type);
        this.status = status;
        this.providerName = providerName;
    }

    /**
     * Gets name of {@link BillingProvider} responsible for this BillingResponse.
     * <p>
     * Might be useful to properly handle some data from {@link BillingModel#getOriginalJson()}.
     *
     * @return Name of corresponding {@link BillingProvider}.
     */
    @Nullable
    public String getProviderName() {
        return providerName;
    }

    /**
     * Indicates whether corresponding billing operation was successful or what kind of error caused
     * it to fail.
     *
     * @return Status of this BillingResponse.
     */
    @NonNull
    @Status
    public int getStatus() {
        return status;
    }

    /**
     * Indicates whether status of this response is successful.
     *
     * @return True if this BillingResponse was not caused by any kind of error, false otherwise.
     */
    public boolean isSuccessful() {
        return SUCCESSFUL.contains(getStatus());
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_STATUS, status);
            jsonObject.put(NAME_PROVIDER_NAME, providerName == null ? NULL : providerName);
        } catch (JSONException exception) {
            ASLog.e("", exception);
        }
        return jsonObject;
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingResponse)) return false;
        if (!super.equals(o)) return false;

        final BillingResponse that = (BillingResponse) o;

        if (providerName != null ? !providerName.equals(
                that.providerName) : that.providerName != null)
            return false;
        if (getStatus() != that.getStatus()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (providerName != null ? providerName.hashCode() : 0);
        result = 31 * result + getStatus();
        return result;
    }
    //CHECKSTYLE:ON
}
