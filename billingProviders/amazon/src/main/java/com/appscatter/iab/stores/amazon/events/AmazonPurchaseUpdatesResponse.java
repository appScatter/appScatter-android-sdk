package com.appscatter.iab.stores.amazon.events;

import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.appscatter.iab.core.model.event.ASEvent;

/**
 * Created by renatoalmeida on 21/12/2016.
 */

public class AmazonPurchaseUpdatesResponse implements ASEvent {

    private final PurchaseUpdatesResponse mPurchaseUpdatesResponse;

    public AmazonPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
        mPurchaseUpdatesResponse = purchaseUpdatesResponse;
    }

    public PurchaseUpdatesResponse getPurchaseUpdatesResponse() {
        return mPurchaseUpdatesResponse;
    }
}
