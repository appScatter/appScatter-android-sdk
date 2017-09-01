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

package com.appscatter.iab.core.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
import static com.appscatter.iab.core.model.ComponentState.UNKNOWN;

/**
 * Android component state.
 * <p>
 * Intended for internal use.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ATTACH, CREATE, CREATE_VIEW, START, RESUME, PAUSE, STOP, DESTROY_VIEW, DESTROY, DETACH, UNKNOWN})
public @interface ComponentState{
    int ATTACH = 0;
    int CREATE = 1;
    int CREATE_VIEW = 2;
    int START = 3;
    int RESUME = 4;
    int PAUSE = 5;
    int STOP = 6;
    int DESTROY_VIEW = 7;
    int DESTROY = 8;
    int DETACH = 9;
    int UNKNOWN = 10;
}
