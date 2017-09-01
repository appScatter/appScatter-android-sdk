package com.appscatter.iab.core.android;

import com.appscatter.iab.core.ASIab;
import com.appscatter.iab.core.model.event.android.SupportFragmentLifecycleEvent;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

public class ASIabSupportFragment extends Fragment {

    @NonNull
    public static ASIabSupportFragment newInstance() {
        return new ASIabSupportFragment();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ASIab.post(new SupportFragmentLifecycleEvent(ATTACH, this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ASIab.post(new SupportFragmentLifecycleEvent(CREATE, this));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ASIab.post(new SupportFragmentLifecycleEvent(CREATE_VIEW, this));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ASIab.post(new SupportFragmentLifecycleEvent(START, this));
    }

    @Override
    public void onResume() {
        super.onResume();
        ASIab.post(new SupportFragmentLifecycleEvent(RESUME, this));
    }

    @Override
    public void onPause() {
        ASIab.post(new SupportFragmentLifecycleEvent(PAUSE, this));
        super.onPause();
    }

    @Override
    public void onStop() {
        ASIab.post(new SupportFragmentLifecycleEvent(STOP, this));
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        ASIab.post(new SupportFragmentLifecycleEvent(DESTROY_VIEW, this));
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        ASIab.post(new SupportFragmentLifecycleEvent(DESTROY, this));
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        ASIab.post(new SupportFragmentLifecycleEvent(DETACH, this));
        super.onDetach();
    }
}
