package com.appscatter.iab.stores.amazon.events;

import com.amazon.device.iap.model.PurchaseResponse;
import com.appscatter.iab.core.model.event.ASEvent;

/**
 * Created by renatoalmeida on 21/12/2016.
 */

public class AmazonPurchaseResponse implements ASEvent {

    private final PurchaseResponse mPurchaseResponse;

    public AmazonPurchaseResponse(PurchaseResponse purchaseResponse) {
        mPurchaseResponse = purchaseResponse;
    }

    public PurchaseResponse getPurchaseResponse() {
        return mPurchaseResponse;
    }
}
