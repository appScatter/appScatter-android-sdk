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

import com.appscatter.iab.core.model.JsonCompatible;
import com.appscatter.iab.core.model.event.ASEvent;
import com.appscatter.iab.core.util.ASIabUtils;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;

import java.io.Serializable;

abstract class BillingEvent implements JsonCompatible, Serializable, ASEvent {

    private static final String NAME_TYPE = "type";

    @BillingEventType
    private final int type;

    protected BillingEvent(@BillingEventType final int type) {
        this.type = type;
    }

    /**
     * Gets type of this event.
     *
     * @return Type of this event.
     */
    @BillingEventType
    public int getType() {
        return type;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NAME_TYPE, type);
        } catch (JSONException exception) {
            ASLog.e("", exception);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return ASIabUtils.toString(this);
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BillingEvent event = (BillingEvent) o;

        if (type != event.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return type;
    }
    //CHECKSTYLE:ON

}
