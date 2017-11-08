package com.appscatter.iab.utils.permissions;

import java.util.List;

public class ASPermissionsConfig {

    private boolean mHandlePermissions;
    private Listener mListener;

    private ASPermissionsConfig(boolean handlePermissions, Listener listener) {
        mHandlePermissions = handlePermissions;
        mListener = listener;

    }

    public boolean handlePermissions() {
        return mHandlePermissions;
    }

    public Listener getListener() {
        return mListener;
    }

    public interface Listener {

        void onPermissionsChecked(ASPermissionsReport permissionsReport);

        void onPermissionRationaleShouldBeShown(List<ASPermissionRequest> permissionRequests, ASPermissionToken permissionToken);
    }

    /**
     * Builder class to configure the displayed dialog.
     * Non set fields will be initialized to an empty string.
     */
    public static class Builder {

        private boolean handlePermissions = true;
        private Listener listener;

        public Builder handlePermissions(boolean handlePermissions) {
            this.handlePermissions = handlePermissions;
            return this;
        }

        public Builder withCallback(Listener listener) {
            this.listener = listener;
            return this;
        }

        public ASPermissionsConfig build() {
            return new ASPermissionsConfig(handlePermissions, listener);
        }
    }
}
