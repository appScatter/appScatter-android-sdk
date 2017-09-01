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

package com.appscatter.iab.core;

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.Configuration;
import com.appscatter.iab.core.model.billing.Compatibility;
import com.appscatter.iab.core.model.event.SetupResponse;
import com.appscatter.iab.core.model.event.SetupStartedEvent;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASPreferences;
import com.appscatter.iab.utils.permissions.ASPermissions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.appscatter.iab.core.model.event.SetupResponse.Status.FAILED;
import static com.appscatter.iab.core.model.event.SetupResponse.Status.PROVIDER_CHANGED;
import static com.appscatter.iab.core.model.event.SetupResponse.Status.SUCCESS;

/**
 * This class tries to pick one {@link BillingProvider} from those available from
 * {@link Configuration#getProviders()}.
 * <p>
 * Providers are picked according to this priority rules:
 * <ul>
 * <li> Only available providers will be considered, according to {@link BillingProvider#isAvailable()}.
 * <li> If some provider had been already used by this app, it is considered first.
 * <li> If provider returns {@link Compatibility#PREFERRED} from
 * {@link BillingProvider#checkCompatibility()} it is considered next.
 * <li> First suitable provider will be picked according to order it was added in
 * {@link Configuration.Builder#addBillingProvider(BillingProvider)}.
 * </ul>
 */
@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
final class SetupManager {

    private static final String KEY_LAST_PROVIDER = "last_provider";

    private static SetupManager instance;
    private final ASPreferences preferences;
    /**
     * Flag indicating whether setup process is happening at the moment.
     */
    private boolean setupInProgress;
    /**
     * Configuration object from last received setup request.
     * <p>
     * Used to determine whether {@link SetupResponse} is relevant when it's ready.
     */
    @Nullable
    private Configuration lastConfiguration;

    private SetupManager(@NonNull final Context context) {
        super();
        preferences = new ASPreferences(context);
    }

    @SuppressWarnings({"PMD.NonThreadSafeSingleton"})
    static SetupManager getInstance(@NonNull final Context context) {
        ASChecks.checkThread(true);
        if (instance == null) {
            instance = new SetupManager(context);
        }
        return instance;
    }

    /**
     * Filters out unavailable {@link BillingProvider}s.
     *
     * @param providers Providers to filter.
     * @return Collection of available providers.
     */
    @NonNull
    public Iterable<BillingProvider> getAvailable(
            @NonNull final Iterable<BillingProvider> providers) {
        final Collection<BillingProvider> availableProviders = new LinkedHashSet<>();
        for (final BillingProvider provider : providers) {
            if (provider.isAvailable()) {
                availableProviders.add(provider);
            }
        }
        return availableProviders;
    }

    @SuppressWarnings({"PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts"})
    @NonNull
    private SetupResponse newResponse(@NonNull final SetupStartedEvent setupStartedEvent) {
        ASLog.logMethod(setupStartedEvent);

        final Configuration configuration = setupStartedEvent.getConfiguration();
        final Iterable<BillingProvider> providers = configuration.getProviders();
        final Iterable<BillingProvider> availableProviders = getAvailable(providers);

        final boolean hadProvider = preferences.contains(KEY_LAST_PROVIDER);
        if (hadProvider) {
            // Try previously used provider
            final String lastProvider = preferences.getString(KEY_LAST_PROVIDER, "");
            ASLog.d("Previous provider: %s", lastProvider);
            for (final BillingProvider provider : availableProviders) {
                if (lastProvider.equals(provider.getName())) {
                    // Use last provider if it's compatible.
                    if (provider.checkCompatibility() != Compatibility.INCOMPATIBLE) {
                        return new SetupResponse(configuration, SUCCESS, provider);
                    }
                    break;
                }
            }
        }

        // Use appropriate success status
        final SetupResponse.Status successStatus = hadProvider ? PROVIDER_CHANGED : SUCCESS;

        BillingProvider compatibleProvider = null;
        for (final BillingProvider provider : availableProviders) {
            @Compatibility final int compatibility = provider.checkCompatibility();
            ASLog.d("Checking provider: %s, compatibility: %s", provider.getName(), compatibility);
            if (compatibility == Compatibility.PREFERRED) {
                // Pick preferred provider
                return new SetupResponse(configuration, successStatus, provider);
            } else if (compatibility == Compatibility.COMPATIBLE && compatibleProvider == null) {
                compatibleProvider = provider;
            }
        }

        // Pick first compatible provider
        if (compatibleProvider != null) {
            return new SetupResponse(configuration, successStatus, compatibleProvider);
        }

        // No suitable provider was found
        return new SetupResponse(configuration, FAILED, null);
    }

    /**
     * Tries to start setup process for the supplied configuration.
     * <p>
     * If setup is already in progress, new configuration object is stored and used after
     * current setup is finished.
     *
     * @param configuration Configuration object to perform setup for.
     * @see ASIab#setup()
     */
    void startSetup(@NonNull final Configuration configuration) {
        ASChecks.checkThread(true);
        lastConfiguration = configuration;
        if (setupInProgress) {
            return;
        }

        setupInProgress = true;
        ASIab.post(new SetupStartedEvent(configuration));
    }

    public void registerForEvents() {
        ASIab.getEvents(SetupResponse.class).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onSetupResponseEvent);
        ASIab.getEvents(SetupStartedEvent.class).subscribe(this::onSetupStartedEvent);
    }

    private void onSetupResponseEvent(@NonNull final SetupResponse setupResponse) {
        setupInProgress = false;
        if (lastConfiguration != null && lastConfiguration != setupResponse.getConfiguration()) {
            // If another setup was requested with different configuration
            startSetup(lastConfiguration);
        } else {
            lastConfiguration = null;
        }

        if (setupResponse.getBillingProvider() != null) {
            List<String> permissions = setupResponse.getBillingProvider().getPermissionsList();
            if (permissions == null || permissions.isEmpty()) {
                return;
            } else {
                ASPermissions.requestPermissions(setupResponse.getBillingProvider().getPermissionsList());
            }
        }
    }

    private void onSetupStartedEvent(@NonNull final SetupStartedEvent setupStartedEvent) {
        final SetupResponse setupResponse = newResponse(setupStartedEvent);
        if (setupResponse.isSuccessful()) {
            // Suitable provider successfully picked, save it for next setup.
            //noinspection ConstantConditions
            preferences.put(KEY_LAST_PROVIDER, setupResponse.getBillingProvider().getName());
        }
        ASIab.post(setupResponse);
    }
}
