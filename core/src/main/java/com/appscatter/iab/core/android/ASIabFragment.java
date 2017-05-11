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

package com.appscatter.iab.core.android;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.model.event.android.FragmentLifecycleEvent;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.appscatter.iab.core.model.ComponentState.ATTACH;
import static com.appscatter.iab.core.model.ComponentState.CREATE;
import static com.appscatter.iab.core.model.ComponentState.CREATE_VIEW;
import static com.appscatter.iab.core.model.ComponentState.DESTROY;
import static com.appscatter.iab.core.model.ComponentState.DESTROY_VIEW;
import static com.appscatter.iab.core.model.ComponentState.DETACH;
import static com.appscatter.iab.core.model.ComponentState.PAUSE;
import static com.appscatter.iab.core.model.ComponentState.RESUME;
import static com.appscatter.iab.core.model.ComponentState.START;
import static com.appscatter.iab.core.model.ComponentState.STOP;

/**
 * Invisible fragment intended to monitor parent lifecycle.
 */
public class ASIabFragment extends Fragment {

    @NonNull
    public static ASIabFragment newInstance() {
        return new ASIabFragment();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ASIab.post(new FragmentLifecycleEvent(ATTACH, this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ASIab.post(new FragmentLifecycleEvent(CREATE, this));
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ASIab.post(new FragmentLifecycleEvent(CREATE_VIEW, this));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ASIab.post(new FragmentLifecycleEvent(START, this));
    }

    @Override
    public void onResume() {
        super.onResume();
        ASIab.post(new FragmentLifecycleEvent(RESUME, this));
    }

    @Override
    public void onPause() {
        ASIab.post(new FragmentLifecycleEvent(PAUSE, this));
        super.onPause();
    }

    @Override
    public void onStop() {
        ASIab.post(new FragmentLifecycleEvent(STOP, this));
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        ASIab.post(new FragmentLifecycleEvent(DESTROY_VIEW, this));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        ASIab.post(new FragmentLifecycleEvent(DESTROY, this));
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        ASIab.post(new FragmentLifecycleEvent(DETACH, this));
        super.onDetach();
    }

}
