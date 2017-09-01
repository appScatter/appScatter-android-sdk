package com.appscatter.iab.utils.permissions;

import android.support.annotation.NonNull;

public class ASPermissionGrantedResponse {

    private final ASPermissionRequest requestedPermission;

    public ASPermissionGrantedResponse(@NonNull ASPermissionRequest requestedPermission) {
        this.requestedPermission = requestedPermission;
    }

    /**
     * Builds a new instance of PermissionGrantedResponse from a given permission string
     */
    public static ASPermissionGrantedResponse from(@NonNull String permission) {
        return new ASPermissionGrantedResponse(new ASPermissionRequest(permission));
    }

    public ASPermissionRequest getRequestedPermission() {
        return requestedPermission;
    }

    public String getPermissionName() {
        return requestedPermission.getName();
    }
}
