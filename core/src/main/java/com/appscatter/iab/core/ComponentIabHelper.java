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

import com.appscatter.iab.core.android.ASIabFragment;
import com.appscatter.iab.core.android.ASIabSupportFragment;
import com.appscatter.iab.core.api.IabHelper;
import com.appscatter.iab.core.model.ComponentState;
import com.appscatter.iab.core.model.event.android.FragmentLifecycleEvent;
import com.appscatter.iab.core.model.event.android.SupportFragmentLifecycleEvent;
import com.appscatter.iab.utils.ASLog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

/**
 * This class contains common code for all {@link IabHelper} implementations intended to use from
 * within Android components ({@link Activity}, {@link Fragment}).
 * <p>
 * Helper attempts to attach instance of {@link ASIabFragment} to supplied fragment manager.
 * Fragment will monitor component lifecycle and report it to the library.
 */
abstract class ComponentIabHelper extends AdvancedIabHelperImpl {

    protected static final String FRAGMENT_TAG = "ASIabFragment";

    @NonNull
    protected final Object opfFragment;

    private DisposableObserver<FragmentLifecycleEvent> mFragmentLifecycleEventObserver;
    private DisposableObserver<SupportFragmentLifecycleEvent> mSupportFragmentLifecycleEvent;

    ComponentIabHelper(
            @Nullable final android.support.v4.app.FragmentManager supportFragmentManager,
            @Nullable final android.app.FragmentManager fragmentManager) {
        super();
        // Register for lifecycle event right a way
        registerForEvents();

        if (supportFragmentManager != null) {
            ASLog.d("ComponentIabHelper uses android.support.v4.app.Fragment.");
            final android.support.v4.app.Fragment existingFragment = supportFragmentManager
                    .findFragmentByTag(FRAGMENT_TAG);
            if (existingFragment != null) {
                // Fragment already attached
                opfFragment = existingFragment;
                register();
                return;
            }
            final android.support.v4.app.Fragment fragment = ASIabSupportFragment.newInstance();
            opfFragment = fragment;
            // Attach new fragment
            supportFragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
            // wait for onAttach() callback
            supportFragmentManager.executePendingTransactions();
            return;
        }

        if (fragmentManager != null) {
            ASLog.d("ComponentIabHelper uses android.app.Fragment.");
            final Fragment existingFragment = fragmentManager
                    .findFragmentByTag(FRAGMENT_TAG);
            if (existingFragment != null) {
                opfFragment = existingFragment;
                register();
                return;
            }
            final Fragment fragment = ASIabFragment.newInstance();
            opfFragment = fragment;
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
            return;
        }

        throw new IllegalStateException();
    }

    /**
     * Gets activity from the associated android component.
     *
     * @return Activity object, can't be null.
     */
    @NonNull
    protected abstract Activity getActivity();

    /**
     * Handles reported lifecycle state.
     *
     * @param type Component state to handle.
     * @see #register()
     * @see #unregister()
     */
    protected abstract void handleState(@ComponentState final int type);

    public void registerForEvents() {

        mFragmentLifecycleEventObserver = new DisposableObserver<FragmentLifecycleEvent>() {

            @Override
            public void onError(Throwable e) {
                //Nothing to do...
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onNext(FragmentLifecycleEvent fragmentLifecycleEvent) {
                onFragmentLifecycleEvent(fragmentLifecycleEvent);
            }
        };

        mSupportFragmentLifecycleEvent = new DisposableObserver<SupportFragmentLifecycleEvent>() {
            @Override
            public void onComplete() {
                //Nothing to do...
            }

            @Override
            public void onError(Throwable e) {
                //Nothing to do...
            }

            @Override
            public void onNext(SupportFragmentLifecycleEvent supportFragmentLifecycleEvent) {
                onSupportFragmentLifecycleEvent(supportFragmentLifecycleEvent);
            }
        };

        ASIab.getEvents(FragmentLifecycleEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(mFragmentLifecycleEventObserver);
        ASIab.getEvents(SupportFragmentLifecycleEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(mSupportFragmentLifecycleEvent);
    }

    public void unregisterEvents() {
        mFragmentLifecycleEventObserver.dispose();
        mSupportFragmentLifecycleEvent.dispose();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void onFragmentLifecycleEvent(@NonNull final FragmentLifecycleEvent event) {
        if (opfFragment == event.getFragment()) {
            handleState(event.getType());
        }
    }

    private void onSupportFragmentLifecycleEvent(@NonNull final SupportFragmentLifecycleEvent event) {
        if (opfFragment == event.getFragment()) {
            handleState(event.getType());
        }
    }
}
