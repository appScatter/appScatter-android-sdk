package com.appscatter.iab.utils.permissions;

import java.util.LinkedList;
import java.util.List;

public class ASPermissionsReport {

    private final List<ASPermissionGrantedResponse> mGrantedPermissionResponses;
    private final List<ASPermissionDeniedResponse> mDeniedPermissionResponses;

    public ASPermissionsReport() {
        mGrantedPermissionResponses = new LinkedList<>();
        mDeniedPermissionResponses = new LinkedList<>();
    }

    /**
     * Returns a collection with all the permissions that has been granted
     */
    public List<ASPermissionGrantedResponse> getGrantedPermissionResponses() {
        return mGrantedPermissionResponses;
    }

    /**
     * Returns a collection with all the permissions that has been denied
     */
    public List<ASPermissionDeniedResponse> getDeniedPermissionResponses() {
        return mDeniedPermissionResponses;
    }

    /**
     * Returns whether the user has granted all the requested permission
     */
    public boolean areAllPermissionsGranted() {
        return mDeniedPermissionResponses.isEmpty();
    }

    /**
     * Returns whether the user has permanently denied any of the requested permissions
     */
    public boolean isAnyPermissionPermanentlyDenied() {
        boolean hasPermanentlyDeniedAnyPermission = false;

        for (ASPermissionDeniedResponse deniedResponse : mDeniedPermissionResponses) {
            if (deniedResponse.isPermanentlyDenied()) {
                hasPermanentlyDeniedAnyPermission = true;
                break;
            }
        }

        return hasPermanentlyDeniedAnyPermission;
    }

    boolean addGrantedPermissionResponse(ASPermissionGrantedResponse response) {
        return mGrantedPermissionResponses.add(response);
    }

    boolean addDeniedPermissionResponse(ASPermissionDeniedResponse response) {
        return mDeniedPermissionResponses.add(response);
    }

    void clear() {
        mGrantedPermissionResponses.clear();
        mDeniedPermissionResponses.clear();
    }
}
