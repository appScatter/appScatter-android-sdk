package com.appscatter.iab.core.model;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.model.event.ActivityResultRequest;
import com.appscatter.iab.core.model.event.RequestHandledEvent;
import com.appscatter.iab.core.model.event.SetupResponse;
import com.appscatter.iab.core.model.event.SetupStartedEvent;
import com.appscatter.iab.core.model.event.android.ActivityEvent;
import com.appscatter.iab.core.model.event.android.ActivityNewIntentEvent;
import com.appscatter.iab.core.model.event.android.ActivityResult;
import com.appscatter.iab.core.model.event.android.FragmentLifecycleEvent;
import com.appscatter.iab.core.model.event.android.SupportFragmentLifecycleEvent;
import com.appscatter.iab.core.model.event.billing.BillingRequest;
import com.appscatter.iab.core.model.event.billing.BillingResponse;

import io.reactivex.Observable;

/**
 * Created by renatoalmeida on 23/01/2017.
 */

public class BillingEventsProvider {

    public Observable<ActivityEvent> getActivityEvent() {
        return ASIab.getEvents(ActivityEvent.class);
    }

    public Observable<ActivityResultRequest> getActivityResultRequest() {
        return ASIab.getEvents(ActivityResultRequest.class);
    }

    public Observable<ActivityNewIntentEvent> getActivityNewIntentEvent() {
        return ASIab.getEvents(ActivityNewIntentEvent.class);
    }

    public Observable<ActivityResult> getActivityResult() {
        return ASIab.getEvents(ActivityResult.class);
    }

    public Observable<SetupStartedEvent> getSetupStartedEvent() {
        return ASIab.getEvents(SetupStartedEvent.class);
    }

    public Observable<SetupResponse> getSetupResponse() {
        return ASIab.getEvents(SetupResponse.class);
    }

    public Observable<BillingRequest> getBillingRequest() {
        return ASIab.getEvents(BillingRequest.class);
    }

    public Observable<RequestHandledEvent> getRequestHandledEvent() {
        return ASIab.getEvents(RequestHandledEvent.class);
    }

    public Observable<BillingResponse> getBillingResponse() {
        return ASIab.getEvents(BillingResponse.class);
    }

    public Observable<FragmentLifecycleEvent> getFragmentLifecycleEvent() {
        return ASIab.getEvents(FragmentLifecycleEvent.class);
    }

    public Observable<SupportFragmentLifecycleEvent> getSupportFragmentLifecycleEvent() {
        return ASIab.getEvents(SupportFragmentLifecycleEvent.class);
    }
}
