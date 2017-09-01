package com.appscatter.iab.utils.permissions;

/**
 * Utility class to let clients show the user how is the permission going to be used
 * Clients of this class must call one of the two methods and only once
 */
public interface ASPermissionToken {

    /**
     * Continues with the permission request process
     */
    void continuePermissionRequest();

    /**
     * Cancels the permission request process
     */
    void cancelPermissionRequest();
}

