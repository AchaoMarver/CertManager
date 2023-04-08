package com.marver.cert.manager.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private static final int PERMISSIONS_REQUEST_CODE = 42;
    private final Activity mActivity;
    private RequestPermission mRequestPermission;
    private static final String TAG = "Permission";

    public PermissionUtils(Activity activity) {
        this.mActivity = activity;
    }

    private boolean isGranted(String permission) {
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) || ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public interface RequestPermission {
        /**
         * 权限请求成功
         */
        void onRequestPermissionSuccess();

        /**
         * 用户拒绝了权限请求, 权限请求失败, 但还可以继续请求该权限
         *
         * @param permissions 请求失败的权限名
         */
        void onRequestPermissionFailure(List<String> permissions);

        /**
         * 用户拒绝了权限请求并且用户选择了以后不再询问, 权限请求失败, 这时将不能继续请求该权限, 需要提示用户进入设置页面打开该权限
         *
         * @param permissions 请求失败的权限名
         */
        void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions);
    }


    public void requestPermission(RequestPermission requestPermission, String... permissions) {
        this.mRequestPermission = requestPermission;
        if (permissions == null || permissions.length == 0) return;
        List<String> needRequest = new ArrayList<>();
        // 过滤掉已经申请过的权限
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                needRequest.add(permission);
            }
        }
        //全部权限都已经申请过，直接执行操作
        if (needRequest.isEmpty()) {
            mRequestPermission.onRequestPermissionSuccess();
        } else {//没有申请过,则开始申请
            int needRequestSize = needRequest.size();
            ActivityCompat.requestPermissions(mActivity, needRequest.toArray(new String[needRequestSize]), PERMISSIONS_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_CODE) return;
        List<String> failurePermissions = new ArrayList<>();
        List<String> askNeverAgainPermissions = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            // 权限申请失败
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                // 是否点击了不在询问 true 没点击  false 点击了
                String permission = permissions[i];
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                    failurePermissions.add(permission);
                } else {
                    askNeverAgainPermissions.add(permission);
                }
            }
        }
        if (failurePermissions.size() > 0) {
            Log.d(TAG, "Request permissions failure");
            mRequestPermission.onRequestPermissionFailure(failurePermissions);
        }
        if (askNeverAgainPermissions.size() > 0) {
            Log.d(TAG, "Request permissions failure with ask never again");
            mRequestPermission.onRequestPermissionFailureWithAskNeverAgain(askNeverAgainPermissions);
        }
        if (failurePermissions.size() == 0 && askNeverAgainPermissions.size() == 0) {
            Log.d(TAG, "Request permissions success");
            mRequestPermission.onRequestPermissionSuccess();
        }
    }
}
