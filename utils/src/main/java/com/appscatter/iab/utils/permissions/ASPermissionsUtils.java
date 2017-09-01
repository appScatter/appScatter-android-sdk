package com.appscatter.iab.utils.permissions;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

class ASPermissionsUtils {

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    static ASPermissionsReport convertMultiplePermissionsReport(MultiplePermissionsReport report) {
        ASPermissionsReport asReport = new ASPermissionsReport();

        for (final PermissionGrantedResponse permissionGrantedResponse : report.getGrantedPermissionResponses()) {
            asReport.addGrantedPermissionResponse(convertPermissionGrantedResponse(permissionGrantedResponse));
        }

        for (final PermissionDeniedResponse permissionDeniedResponse : report.getDeniedPermissionResponses()) {
            asReport.addDeniedPermissionResponse(convertPermissionDeniedResponse(permissionDeniedResponse));
        }

        return asReport;
    }

    private static ASPermissionGrantedResponse convertPermissionGrantedResponse(PermissionGrantedResponse response) {
        return ASPermissionGrantedResponse.from(response.getPermissionName());
    }

    private static ASPermissionDeniedResponse convertPermissionDeniedResponse(PermissionDeniedResponse response) {
        return ASPermissionDeniedResponse.from(response.getPermissionName(), response.isPermanentlyDenied());
    }

    static List<ASPermissionRequest> convertPermissionsRequestList(List<PermissionRequest> permissions) {
        List<ASPermissionRequest> asPermissions = new ArrayList<>();

        for (final PermissionRequest permission : permissions) {
            asPermissions.add(new ASPermissionRequest(permission.getName()));
        }

        return asPermissions;
    }

    static ASPermissionToken convertToken(final PermissionToken token) {
        return new ASPermissionToken() {
            @Override
            public void continuePermissionRequest() {
                token.continuePermissionRequest();
            }

            @Override
            public void cancelPermissionRequest() {
                token.cancelPermissionRequest();
            }
        };
    }
}
