package com.appscatter.iab.utils.permissions;

import android.support.annotation.NonNull;

/**
 * Wrapper class for a permission request
 */

public class ASPermissionRequest {

    private final String mName;

    public ASPermissionRequest(@NonNull String name) {
        this.mName = name;
    }

    /**
     * One of the values found in {@link android.Manifest.permission}
     */
    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "Permission mName: " + mName;
    }
}
