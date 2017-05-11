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

import com.appscatter.iab.core.billing.BillingProvider;
import com.appscatter.iab.core.model.billing.Purchase;
import com.appscatter.iab.core.verification.VerificationResult;
import com.appscatter.iab.utils.ASLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Response from {@link BillingProvider} for corresponding {@link InventoryRequest}.
 */
public class InventoryResponse extends BillingResponse {

    private static final String NAME_INVENTORY = "inventory";
    private static final String NAME_PURCHASE = "purchase";
    private static final String NAME_VERIFICATION_RESULT = "verification_result";
    private static final String NAME_HAS_MORE = "has_more";


    @NonNull
    private final Map<Purchase, Integer> inventory = new HashMap<>();
    private final boolean hasMore;

    public InventoryResponse(@NonNull @Status final int status,
            @Nullable final String providerName,
            @Nullable final Map<Purchase, Integer> inventory,
            final boolean hasMore) {
        super(BillingEventType.INVENTORY, status, providerName);
        if (inventory != null) {
            this.inventory.putAll(inventory);
        }
        this.hasMore = hasMore;
    }

    public InventoryResponse(@NonNull @Status final int status,
            @Nullable final String providerName,
            @Nullable final Iterable<Purchase> inventory,
            final boolean hasMore) {
        this(status, providerName,
                inventory == null ? null : new HashMap<Purchase, Integer>() {
                    {
                        for (final Purchase purchase : inventory) {
                            put(purchase, null);
                        }
                    }
                }, hasMore);
    }

    public InventoryResponse(@NonNull @Status final int status,
            @Nullable final String providerName) {
        this(status, providerName, (Map<Purchase, Integer>) null, false);
    }

    /**
     * Gets items owned by user along with {@link VerificationResult} of those items.
     *
     * @return Purchases made by user mapped to their verification status. Can be null.
     *
     * @see #isSuccessful()
     */
    @NonNull
    public Map<Purchase, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    /**
     * Indicates whether there are more items to be loaded with subsequent {@link InventoryRequest}s.
     *
     * @return True if there are more user owned purchases to be loaded, false otherwise.
     *
     * @see InventoryRequest#startOver()
     */
    public boolean hasMore() {
        return hasMore;
    }

    @NonNull
    @Override
    public JSONObject toJson() {
        final JSONObject jsonObject = super.toJson();
        try {
            jsonObject.put(NAME_HAS_MORE, hasMore);
            final JSONArray jsonArray = new JSONArray();
            for (final Map.Entry<Purchase, Integer> entry : inventory.entrySet()) {
                final JSONObject item = new JSONObject();
                item.put(NAME_PURCHASE, entry.getKey().toJson());
                item.put(NAME_VERIFICATION_RESULT, entry.getValue());
                jsonArray.put(item);
            }
            jsonObject.put(NAME_INVENTORY, jsonArray);
        } catch (JSONException e) {
            ASLog.e("", e);
        }
        return jsonObject;
    }
}
