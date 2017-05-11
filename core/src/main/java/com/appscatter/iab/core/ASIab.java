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

package com.appscatter.iab.core;

import com.appscatter.iab.core.api.ActivityIabHelper;
import com.appscatter.iab.core.api.AdvancedIabHelper;
import com.appscatter.iab.core.api.FragmentIabHelper;
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.api.SimpleIabHelper;
import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.billing.Providers;
import com.appscatter.iab.core.listener.DefaultBillingListener;
import com.appscatter.iab.core.model.Configuration;
import com.appscatter.iab.core.model.billing.IapProductList;
import com.appscatter.iab.core.model.event.ASEvent;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.utils.ASChecks;
import com.appscatter.iab.utils.ASLog;
import com.appscatter.iab.utils.ASUtils;
import com.appscatter.iab.utils.exception.InitException;
import com.appscatter.iab.utils.permissions.ASPermissions;
import com.appscatter.iab.utils.permissions.ASPermissionsConfig;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;

import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

public final class ASIab {

    private static Application mApplication;
    private static String mAppScatterId;
    private static long mEnqueue;
    private static Context mContext;
    private static Configuration configuration;
    private static Pair<String, String> providerInfo;
    private static IapProductList mProductList;
    private static boolean mPending = false;

    private static PublishProcessor<ASEvent> mBus = PublishProcessor.create();

    private ASIab() {
        throw new UnsupportedOperationException();
    }

    private static void checkInit() {
        ASChecks.checkThread(true);
        if (configuration == null && !mPending) {
            throw new InitException(false);
        }
    }

    public static <T> Observable<T> getEvents(final Class<T> eventClass) {
        return mBus
                .filter(eventClass::isInstance)
                .cast(eventClass)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toObservable();
    }

    /**
     * Posts event object for delivery to all subscribers.
     * Intend to be used by {@link BillingProvider} implementations.
     *
     * @param event Event object to deliver.
     * @return True if event was delivered, false if it was skipped due to the lack of subscribers.
     */
    public static void post(@NonNull final ASEvent event) {
        mBus.onNext(event);
    }

    /**
     * @return Simple version of {@link IabHelper}.
     * @see {@link SimpleIabHelper}
     */
    @NonNull
    public static SimpleIabHelper getSimpleHelper() {
        checkInit();
        return new SimpleIabHelperImpl();
    }

    /**
     * @return Feature reach version of {@link SimpleIabHelper}.
     * @see AdvancedIabHelper
     * @see #getSimpleHelper()
     */
    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public static AdvancedIabHelper getAdvancedHelper() {
        checkInit();
        return new AdvancedIabHelperImpl();
    }

    /**
     * Support version of {@link #getActivityHelper(Activity)}.
     */
    @NonNull
    public static ActivityIabHelper getActivityHelper(
            @NonNull final FragmentActivity fragmentActivity) {
        checkInit();
        return new ActivityIabHelperImpl(fragmentActivity, null);
    }

    /**
     * Instantiates {@link IabHelper} associated with supplied activity.
     * <p>
     * This call will attach invisible fragment which monitors activity lifecycle.
     * <p>
     * Supplied activity <b>must</b> delegate {@link Activity#onActivityResult(int, int, Intent)}
     * to {@link ActivityIabHelper#onActivityResult(Activity, int, int, Intent)}.
     *
     * @param activity Activity object to associate helper with.
     * @return Version of {@link IabHelper} designed to be used from activity.
     * @see ActivityIabHelper
     */
    @NonNull
    public static ActivityIabHelper getActivityHelper(@NonNull final Activity activity) {
        checkInit();
        return new ActivityIabHelperImpl(null, activity);
    }

    /**
     * Support version of {@link #getFragmentHelper(android.app.Fragment)}.
     */
    @NonNull
    public static FragmentIabHelper getFragmentHelper(
            @NonNull final android.support.v4.app.Fragment fragment) {
        checkInit();
        return new FragmentIabHelperImpl(fragment, null);
    }

    /**
     * Instantiates {@link IabHelper} associated with supplied fragment.
     * <p>
     * This call will attach invisible child fragment which monitors parent lifecycle.
     * <p>
     * If parent activity delegates {@link Activity#onActivityResult(int, int, Intent)}
     * to {@link ActivityIabHelper#onActivityResult(Activity, int, int, Intent)}, consider using
     * {@link SimpleIabHelper}.
     * <p>
     * Nested fragments were introduced in Android API 17, use
     * {@link #getFragmentHelper(android.support.v4.app.Fragment)} for earlier versions.
     *
     * @param fragment Fragment object to associate helper with.
     * @return Version of {@link IabHelper} designed to be used from fragment.
     */
    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static FragmentIabHelper getFragmentHelper(@NonNull final android.app.Fragment fragment) {
        checkInit();
        return new FragmentIabHelperImpl(null, fragment);
    }

    @NonNull
    public static Configuration getConfiguration() {
        checkInit();
        return configuration;
    }

    /**
     * Initialize ASIab library with supplied configuration.
     * <p>
     * It's strongly recommended to call this method from {@link Application#onCreate()}.
     * <p>
     * Subsequent calls are supported but will reset any previous setup state.
     *
     * @param application   Application object to add {@link Application.ActivityLifecycleCallbacks}
     *                      to and to use as {@link Context}.
     * @param configuration Configuration object to use.
     */
//    @SuppressFBWarnings({"LI_LAZY_INIT_UPDATE_STATIC", "LI_LAZY_INIT_STATIC"})
    public static void init(@NonNull final Application application,
            @NonNull final Configuration configuration) {
        ASLog.logMethod(configuration);
        ASChecks.checkThread(true);
        ASIab.mContext = application.getApplicationContext();

        initialize(application, configuration);

    }

    public static void init(@NonNull final String appScatterId, @NonNull final Application application, @NonNull final DefaultBillingListener listener) {
        ASChecks.checkThread(true);
        ASIab.mContext = application.getApplicationContext();
        ASIab.mAppScatterId = ASIabUtils.generateMD5(appScatterId);
        ASIab.mApplication = application;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager dm = (DownloadManager) ASIab.mContext.getSystemService(DOWNLOAD_SERVICE);

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(ASIab.mEnqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                            try {
                                String jsonProducts = ASIabUtils.getStringFromFile(uriString);
                                ASIab.mProductList = new IapProductList(jsonProducts);

                                initialize(ASIab.mApplication, getPreferedConfiguration(listener));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ASIab.providerInfo = getInstallerDownloadFileName("", ASUtils.getPackageInstaller(ASIab.mContext));
                            downloadProviderInfo(ASIab.providerInfo.first);
                        }
                    }
                }
            }
        };

        mPending = true;
        ASIab.mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        ASIab.providerInfo = getInstallerDownloadFileName(ASIab.mAppScatterId, ASUtils.getPackageInstaller(ASIab.mContext));
        downloadProviderInfo(ASIab.providerInfo.first);

    }

    private static void initialize(@NonNull final Application application, @NonNull final Configuration configuration) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && configuration.getPermissionsConfig().handlePermissions()) {
            ASPermissions.init(application, configuration.getPermissionsConfig());
        }

        final BillingBase billingBase = BillingBase.getInstance();
        final BillingRequestScheduler scheduler = BillingRequestScheduler.getInstance();
        final Set<BillingProvider> providers = configuration.getProviders();
        if (ASIab.configuration == null) {
            // first init
            final ActivityMonitor activityMonitor = ActivityMonitor.getInstance(mContext);
            application.registerActivityLifecycleCallbacks(activityMonitor);
            billingBase.registerForEvents();
            SetupManager.getInstance(application).registerForEvents();
            scheduler.registerForEvents();
            activityMonitor.registerForEvents();
            BillingEventDispatcher.getInstance().registerForEvents();
        } else {
            for (final BillingProvider provider : ASIab.configuration.getProviders()) {
                provider.unregisterForEvents();
            }
        }
        ASIab.configuration = configuration;

        for (final BillingProvider provider : providers) {
            provider.checkManifest();
            provider.registerForEvents();
        }

        scheduler.dropQueue();
        billingBase.setConfiguration(configuration);

        ASIab.mPending = false;
        ASIab.mApplication = null;
    }

    /**
     * Tries to pick one of the {@link BillingProvider}s supplied in {@link Configuration}.
     * <p>
     * {@link #init(Application, Configuration)} must be called prior to this method.
     * <p>
     * Subsequent calls are supported.
     */
    public static void setup() {
        checkInit();
        SetupManager.getInstance(mContext).startSetup(configuration);
    }

    /**
     * Sets if the logs enabled and if the build debug.
     *
     * @param isDebug Set {@code BuildConfig.DEBUG} value of your application.
     * @param enabled {@code true} if logs should be enabled.
     */
    public static void setLogEnabled(final boolean isDebug, final boolean enabled) {
        ASLog.setEnabled(isDebug, enabled);
    }

    private static Pair<String, String> getInstallerDownloadFileName(String userHash, String currentInstallerPackage) {
//        String currentInstallerPackage = ASUtils.getPackageInstaller(ASIab.mContext);
        String downloadFileName = BuildConfig.S3BUCKET;
        String providerClass = "";

        if (userHash != null && !userHash.isEmpty()) {
            downloadFileName += "/users/" + userHash + "/appscatter.products.";

            if (currentInstallerPackage.equals(Providers.Namespaces.GOOGLE)) {
                downloadFileName += Providers.GOOGLE;
                providerClass = Providers.Classes.GOOGLE;
            } else if (currentInstallerPackage.equals(Providers.Namespaces.AMAZON)) {
                downloadFileName += Providers.AMAZON;
                providerClass = Providers.Classes.AMAZON;
            } else if (currentInstallerPackage.equals(Providers.Namespaces.APTOIDE)) {
                downloadFileName += Providers.APTOIDE;
                providerClass = Providers.Classes.APTOIDE;
            } else if (currentInstallerPackage.equals(Providers.Namespaces.SAMSUNG)) {
                downloadFileName += Providers.SAMSUNG;
                providerClass = Providers.Classes.SAMSUNG;
            } else if (currentInstallerPackage.equals(Providers.Namespaces.APPLAND)) {
                downloadFileName += Providers.APPLAND;
                providerClass = Providers.Classes.APPLAND;
            } else if (currentInstallerPackage.equals(Providers.Namespaces.SLIDEME)) {
                downloadFileName += Providers.SLIDEME;
                providerClass = Providers.Classes.SLIDEME;
            } else if (currentInstallerPackage.equals(Providers.Namespaces.YANDEX)) {
                downloadFileName += Providers.YANDEX;
                providerClass = Providers.Classes.YANDEX;
            } else {
                if (ASUtils.isGooglePlayInstalled(ASIab.mContext)) {
                    downloadFileName += Providers.GOOGLE;
                    providerClass = Providers.Classes.GOOGLE;
                }
            }

        }

        downloadFileName += ".json";

        return new Pair<>(downloadFileName, providerClass);
    }

    private static Configuration getPreferedConfiguration(DefaultBillingListener listener) {
        Configuration preferedConfiguration;

        Class libProviderClass = null;
        BillingProvider billingProvider = null;
        try {
            // Load the class for the specific library.
            libProviderClass = Class.forName(ASIab.providerInfo.second);
            // Cast the return object to the library interface so that the
            // caller can directly invoke methods in the interface.
            // Alternatively, the caller can invoke methods through reflection,
            // which is more verbose.
            billingProvider = (BillingProvider)libProviderClass.newInstance();
            billingProvider.init(mContext,
                    billingProvider.getSkuResolver(ASIab.mProductList),
                    billingProvider.getPurchaseVerifier(ASIab.mProductList.getHash()));

            final Configuration.Builder builder = new Configuration.Builder();
            builder.setBillingListener(listener);
            builder.addBillingProvider(billingProvider);
            builder.setAutoRecover(false);
            builder.setPermissionsConfig(new ASPermissionsConfig.Builder().build());
            preferedConfiguration = builder.build();

        } catch (Exception e) {
            e.printStackTrace();
            preferedConfiguration = null;
        }

        return preferedConfiguration;
    }

    private static void downloadProviderInfo(final String filename) {

        DownloadManager dm = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(filename));
        request.setVisibleInDownloadsUi(false);
        mEnqueue = dm.enqueue(request);

    }

}
