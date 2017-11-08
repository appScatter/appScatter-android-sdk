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

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.Arrays;

import static com.appscatter.iab.core.model.ComponentState.CREATE;
import static com.appscatter.iab.core.model.ComponentState.DESTROY;
import static com.appscatter.iab.core.model.ComponentState.PAUSE;
import static com.appscatter.iab.core.model.ComponentState.RESUME;
import static com.appscatter.iab.core.model.ComponentState.START;
import static com.appscatter.iab.core.model.ComponentState.STOP;

/**
 * Created by nunocastro on 09/11/16.
 */

public class ActivityLifecycleEvent extends LifecycleEvent {

    @NonNull
    private final Activity activity;

    public ActivityLifecycleEvent(@ComponentState final int type,
            @NonNull final Activity activity) {
        super(type);
        if (!Arrays.asList(CREATE, START, RESUME, PAUSE, STOP, DESTROY).contains(type)) {
            throw new IllegalArgumentException("Illegal lifecycle callback for Activity");
        }
        this.activity = activity;
    }

    /**
     * Gets activity associated with this event.
     *
     * @return Activity object, can't be null.
     */
    @NonNull
    public Activity getActivity() {
        return activity;
    }

}
