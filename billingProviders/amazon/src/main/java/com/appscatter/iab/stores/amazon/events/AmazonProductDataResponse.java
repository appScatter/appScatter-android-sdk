package com.appscatter.iab.stores.amazon.events;

import com.amazon.device.iap.model.ProductDataResponse;
import com.appscatter.iab.core.model.event.ASEvent;

/**
 * Created by renatoalmeida on 21/12/2016.
 */

public class AmazonProductDataResponse implements ASEvent {

    private final ProductDataResponse mProductDataResponse;

    public AmazonProductDataResponse(ProductDataResponse productDataResponse) {
        mProductDataResponse = productDataResponse;
    }

    public ProductDataResponse getProductDataResponse() {
        return mProductDataResponse;
    }
}
