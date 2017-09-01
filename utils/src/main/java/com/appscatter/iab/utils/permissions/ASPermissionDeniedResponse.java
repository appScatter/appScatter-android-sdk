package com.appscatter.iab.utils.permissions;

import android.support.annotation.NonNull;

/**
 * If a permission was denied, an instance of this class will be returned
 * in the callback.
 */
public class ASPermissionDeniedResponse {

    private final ASPermissionRequest mRequestedPermission;
    private final boolean mPermanentlyDenied;

    public ASPermissionDeniedResponse(@NonNull ASPermissionRequest requestedPermission, boolean permanentlyDenied) {
        this.mRequestedPermission = requestedPermission;
        this.mPermanentlyDenied = permanentlyDenied;
    }

    /**
     * Builds a new instance of PermissionDeniedResponse from a given permission string
     * and a permanently-denied boolean flag
     */
    public static ASPermissionDeniedResponse from(@NonNull String permission, boolean permanentlyDenied) {
        return new ASPermissionDeniedResponse(new ASPermissionRequest(permission), permanentlyDenied);
    }

    public ASPermissionRequest getRequestedPermission() {
        return mRequestedPermission;
    }

    public String getPermissionName() {
        return mRequestedPermission.getName();
    }

    public boolean isPermanentlyDenied() {
        return mPermanentlyDenied;
    }
}
