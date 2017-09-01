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

package com.appscatter.iab.core.model;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.listener.BillingListener;
import com.appscatter.iab.core.listener.DefaultBillingListener;
import com.appscatter.iab.utils.permissions.ASPermissionsConfig;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Model class representing library configuration.
 *
 * @see ASIab#init(android.app.Application, Configuration)
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Configuration {

    @NonNull
    private final Set<BillingProvider> providers;
    @Nullable
    private final BillingListener billingListener;
    private final boolean skipStaleRequests;
    private final boolean autoRecover;
    private final BillingEventsProvider billingEventsProvider;
    private ASPermissionsConfig permissionsConfig;

    Configuration(@NonNull final Set<BillingProvider> providers,
            @Nullable final BillingListener billingListener,
            final boolean skipStaleRequests,
            final boolean autoRecover,
            final ASPermissionsConfig permissionsConfig) {
        this.skipStaleRequests = skipStaleRequests;
        this.autoRecover = autoRecover;
        this.providers = Collections.unmodifiableSet(providers);
        this.billingListener = billingListener;
        this.permissionsConfig = permissionsConfig;
        this.billingEventsProvider = new BillingEventsProvider();
    }

    /**
     * Gets supported billing providers.
     *
     * @return Collection of BillingProvider objects.
     */
    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public Set<BillingProvider> getProviders() {
        return providers;
    }

    /**
     * Gets persistent listener which is used to handle all billing events.
     *
     * @return BillingListener object. Can be null.
     */
    @Nullable
    public BillingListener getBillingListener() {
        return billingListener;
    }

    /**
     * Returns the configurations for the permissions handling
     *
     * @return permissions configuration object, or null if none provided
     */
    public ASPermissionsConfig getPermissionsConfig() {
        return permissionsConfig;
    }

    public boolean skipStaleRequests() {
        return skipStaleRequests;
    }

    /**
     * Indicates whether library should attempt to pick another suitable {@link BillingProvider} if
     * current one becomes unavailable.
     *
     * @return True if library will attempt to pick another BillingProvider. False otherwise.
     */
    public boolean autoRecover() {
        return autoRecover;
    }

    /**
     * Returns the billing events object
     *
     * @return returns the billing events provider
     */
    public BillingEventsProvider getBillingEventsProvider() {
        return billingEventsProvider;
    }

    /**
     * Builder class for {@link Configuration} object.
     */
    public static class Builder {

        @NonNull
        private final Set<BillingProvider> providers = new LinkedHashSet<>();
        @Nullable
        private BillingListener billingListener;
        private boolean skipStaleRequests = true;
        private boolean autoRecover;
        private ASPermissionsConfig permissionsConfig;

        /**
         * Adds supported billing provider.
         * <p>
         * During setup process billing providers will be considered in the order they were added.
         *
         * @param provider BillingProvider object to add.
         * @return this object.
         */
        public Builder addBillingProvider(@NonNull final BillingProvider provider) {
            providers.add(provider);
            return this;
        }

        /**
         * Sets global listener to handle all billing events.
         * <p>
         * This listener will be stored in a static reference.
         *
         * @param billingListener BillingListener object to use.
         * @return this object.
         * @see DefaultBillingListener
         */
        public Builder setBillingListener(@Nullable final BillingListener billingListener) {
            this.billingListener = billingListener;
            return this;
        }

        public Builder setSkipStaleRequests(final boolean skipStaleRequests) {
            this.skipStaleRequests = skipStaleRequests;
            return this;
        }

        /**
         * Sets flag indicating whether library should attempt to substitute current
         * {@link BillingProvider} if it becomes unavailable.
         *
         * @param autoRecover True to attempt substitution of unavailable provider.
         * @return this object.
         * @see BillingProvider#isAvailable()
         */
        public Builder setAutoRecover(final boolean autoRecover) {
            this.autoRecover = autoRecover;
            return this;
        }

        /**
         * Sets configuration for handling the permissions .
         *
         * @param permissionsConfig configuration to be used
         * @return this object
         */
        public Builder setPermissionsConfig(final ASPermissionsConfig permissionsConfig) {
            this.permissionsConfig = permissionsConfig;
            return this;
        }

        /**
         * Constructs new Configuration object.
         *
         * @return Newly constructed Configuration instance.
         */
        public Configuration build() {
            if (permissionsConfig == null) {
                permissionsConfig = new ASPermissionsConfig.Builder().build();
            }
            return new Configuration(providers, billingListener, skipStaleRequests, autoRecover, permissionsConfig);
        }
    }
}
