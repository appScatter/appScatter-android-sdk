/*
 * Copyright 2012-2015 One Platform Foundation
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

package com.appscatter.iab.core.model.event;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.Configuration;
import com.appscatter.iab.core.model.JsonCompatible;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.appscatter.iab.core.model.event.SetupResponse.Status.PROVIDER_CHANGED;
import static com.appscatter.iab.core.model.event.SetupResponse.Status.SUCCESS;
import static org.json.JSONObject.NULL;

/**
 * Class intended to indicate that setup process has finished.
 *
 * @see SetupStartedEvent
 * @see ASIab#setup()
 */
public class SetupResponse implements JsonCompatible, ASEvent {

    private static final String NAME_STATUS = "status";
    private static final String NAME_PROVIDER = "provider";

    /**
     * Status of corresponding {@link SetupResponse}.
     */
    public enum Status {

        /**
         * {@link BillingProvider} has been successfully picked.
         */
        SUCCESS,
        /**
         * Setup resulted in a different {@link BillingProvider} being picked then one that was used
         * for this application previously.
         * <p>
         * Some items might be missing in user's inventory.
         */
        PROVIDER_CHANGED,
        /**
         * Library failed to pick suitable {@link BillingProvider}.
         */
        FAILED,
    }

    private static final Collection<Status> SUCCESSFUL =
            Arrays.asList(SUCCESS, PROVIDER_CHANGED);

    @NonNull
    private final Configuration configuration;
    @NonNull
    private final Status status;
    @Nullable
    private final BillingProvider billingProvider;

    public SetupResponse(@NonNull final Configuration configuration,
            @NonNull final Status status,
            @Nullable final BillingProvider billingProvider) {
        this.configuration = configuration;
        this.status = status;
        this.billingProvider = billingProvider;
        if (billingProvider == null && isSuccessful()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Gets configuration object which is used for the setup.
     *
     * @return Configuration object.
     * @see ASIab#init(Application, Configuration)
     */
    @NonNull
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets status of this setup event.
     *
     * @return Status.
     */
    @NonNull
    public Status getStatus() {
        return status;
    }

    /**
     * Gets billing provider that was picked during setup.
     *
     * @return BillingProvider object if setup was successful, null otherwise.
     * @see #isSuccessful()
     */
    @Nullable
    public BillingProvider getBillingProvider() {
        return billingProvider;
    }

    /**
     * Indicates whether billing provider was successfully picked or not.
     *
     * @return True if BillingProvider was picked, false otherwise.
     */
    public final boolean isSuccessful() {
        return SUCCESSFUL.contains(status);
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME_STATUS, status);
            jsonObject.put(NAME_PROVIDER, billingProvider == null ? NULL : billingProvider);
        } catch (JSONException exception) {
            ASLog.e("", exception);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return ASIabUtils.toString(this);
    }
}
