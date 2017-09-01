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

package com.appscatter.iab.core.model.event.android;

import com.appscatter.iab.core.model.ComponentState;

import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Event that indicates {@link Fragment} lifecycle state change.
 */
public class FragmentLifecycleEvent extends LifecycleEvent {

    @NonNull
    private final Fragment fragment;

    public FragmentLifecycleEvent(@ComponentState final int type,
            @NonNull final Fragment fragment) {
        super(type);
        this.fragment = fragment;
    }

    /**
     * Gets fragment associated with this event.
     *
     * @return Fragment object, can't be null.
     */
    @NonNull
    public Fragment getFragment() {
        return fragment;
    }
}
