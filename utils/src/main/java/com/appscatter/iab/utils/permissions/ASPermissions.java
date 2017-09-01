package com.appscatter.iab.utils.permissions;

import com.appscatter.iab.utils.ASLog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import android.app.Application;
import android.os.Build;

import java.util.Collection;
import java.util.List;

import static com.appscatter.iab.utils.permissions.ASPermissionsUtils.convertMultiplePermissionsReport;
import static com.appscatter.iab.utils.permissions.ASPermissionsUtils.convertPermissionsRequestList;
import static com.appscatter.iab.utils.permissions.ASPermissionsUtils.convertToken;

public class ASPermissions {

    private static ASPermissionsConfig mConfiguration;

    public static void init(Application application, ASPermissionsConfig permissionsConfig) {
        Dexter.initialize(application);
        mConfiguration = permissionsConfig;
    }

    public static void checkPermissions(ASPermissionsConfig config, Collection<String> permissionsList) {
        if (config == null || !config.handlePermissions() || Dexter.isRequestOngoing()) {
            return;
        }

        if (!permissionsList.isEmpty()) {
            MultiplePermissionsListener listener = getDexterMultiplePermissionsListener(config);
            Dexter.checkPermissions(listener, permissionsList);
        } else {
            ASLog.i("ASPermissions - checkPermissions - Empty permissions");
        }
    }

    private static MultiplePermissionsListener getDexterMultiplePermissionsListener(final ASPermissionsConfig config) {
        return new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (config.getListener() == null) {
                    ASLog.e("ASPermissions - onPermissionsChecked - no permissions listener defined");
                    return;
                }

                config.getListener().onPermissionsChecked(convertMultiplePermissionsReport(report));
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                if (config.getListener() == null) {
                    ASLog.e("ASPermissions - onPermissionRationaleShouldBeShown - no permissions listener defined");
                    return;
                }

                config.getListener().onPermissionRationaleShouldBeShown(convertPermissionsRequestList(permissions), convertToken(token));
            }
        };
    }

    public static void requestPermissions(List<String> permissionsList) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                !mConfiguration.handlePermissions() ||
                permissionsList == null ||
                permissionsList.isEmpty()) {
            return;
        }

        ASPermissions.checkPermissions(mConfiguration, permissionsList);
    }
}
