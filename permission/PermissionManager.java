package com.myjf.gcs.permission;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.myjf.gcs.app.BaseActivity;
import com.myjf.gcs.app.WindowUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by renmingming on 2016/10/24.
 */
public class PermissionManager {

    private static PermissionManager permissionManager;

    private HashMap<Integer, GetPermissonsCallback> permissonsCallbackHashMap;

    private static BaseActivity mActivity;

    private static final int MODE_SHIFT = 10;
    private static final int MODE_MASK = 3 << MODE_SHIFT;
    public static final int REQUEST_CODE_ASK_SETTINGBACK = 2;

    public static int REQUEST_CODE = 100;

    public static int getRequestCode() {
        REQUEST_CODE++;
        return REQUEST_CODE;
    }

    private PermissionManager(BaseActivity activity) {

        permissonsCallbackHashMap = new HashMap<>();
    }

    public static synchronized PermissionManager getInstance(BaseActivity activity) {
        mActivity = activity;
        if (permissionManager == null) {
            permissionManager = new PermissionManager(activity);
        }
        return permissionManager;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(String permission) {

        if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermissionMust(List<String> permissionsList, String permission) {

        if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getpermissions(String str, GetPermissonsCallback callback, int reqeustCode) {
        String[] strs = new String[]{str};
        getpermissions(strs, callback, reqeustCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getpermissions(String[] strs, GetPermissonsCallback callback, int reqeustCode) {
        getpermissions(strs, null, callback, reqeustCode);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void getpermissions(String[] strs, String[] strs2, GetPermissonsCallback callback, int reqeustCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<>();
            final List<String> permissionsList = new ArrayList<>();
            if (strs != null) {
                for (int i = 0; i < strs.length; i++) {
                    if (!addPermissionMust(permissionsList, strs[i])) {
                        permissionsNeeded.add(strs[i]);
                    }
                }
            }
            if (strs2 != null) {
                for (int i = 0; i < strs2.length; i++) {
                    if (!addPermission(strs2[i])) {
                        permissionsNeeded.add(strs2[i]);
                    }
                }
            }
            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    permissonsCallbackHashMap.put(reqeustCode, callback);
                    ActivityCompat.requestPermissions(mActivity, permissionsList.toArray(new String[permissionsList.size()]), reqeustCode);
                }else {
                    cancleCallback(callback,reqeustCode);
                }
            } else if (permissionsNeeded.size() > 0) {
                ActivityCompat.requestPermissions(mActivity, permissionsNeeded.toArray(new String[permissionsList.size()]), reqeustCode);
                callback.requestPermissions();
                callback.AfterRequestPermissions(true);
            } else {
                callback.requestPermissions();
                callback.AfterRequestPermissions(true);
            }
        } else {
            callback.requestPermissions();
            callback.AfterRequestPermissions(true);
        }


    }

    public void cancleCallback(final GetPermissonsCallback callback , final int requestCode) {
        showWarnPop(callback.errhint, new WindowUtils.ClickCallback() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                mActivity.startActivityForResult(intent, (REQUEST_CODE_ASK_SETTINGBACK << MODE_SHIFT) + requestCode);
            }
        }, new WindowUtils.ClickCallback() {
            @Override
            public void onClick(View v) {
                callback.AfterRequestPermissions(false);
                permissonsCallbackHashMap.remove(callback);
            }
        });
    }

    public void onActivityResult(int requestCode) {
        int mode = requestCode & MODE_MASK;
        switch (mode) {
            case REQUEST_CODE_ASK_SETTINGBACK << MODE_SHIFT:
                int code = requestCode & ~MODE_MASK;
                GetPermissonsCallback callback = permissonsCallbackHashMap.containsKey(code) ? permissonsCallbackHashMap.get(code) : null;
                if (callback != null) {
                    callback.AfterRequestPermissions(false);
                    permissonsCallbackHashMap.remove(callback);
                }
        }
    }

    public void onRequestPermissionsResult(final int requestCode, @NonNull int[] grantResults) {
        if (permissonsCallbackHashMap != null && permissonsCallbackHashMap.size() > 0) {
            Set<Integer> keys = permissonsCallbackHashMap.keySet();
            ArrayList<Integer> keyarry = new ArrayList<>(keys);
            for (int i = 0; i < keyarry.size(); i++) {
                if (requestCode == keyarry.get(i)) {
                    boolean permissions_accuess = true;
                    for (int j = 0; j < grantResults.length; j++) {
                        permissions_accuess = permissions_accuess && grantResults[j] == PackageManager.PERMISSION_GRANTED;
                    }
                    final GetPermissonsCallback callBack = permissonsCallbackHashMap.get(requestCode);
                    WindowUtils.ClickCallback docallBack = new WindowUtils.ClickCallback() {
                        @Override
                        public void onClick(View v) {
                            callBack.AfterRequestPermissions(false);
                            permissonsCallbackHashMap.remove(callBack);
                        }
                    };
                    WindowUtils.ClickCallback gotoSetting = new WindowUtils.ClickCallback() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            mActivity.startActivityForResult(intent, (REQUEST_CODE_ASK_SETTINGBACK << MODE_SHIFT) + requestCode);
                        }
                    };
                    if (callBack != null) {
                        if (permissions_accuess) {
                            callBack.requestPermissions();
                            callBack.AfterRequestPermissions(true);
                            permissonsCallbackHashMap.remove(callBack);
                        } else {
                            showWarnPop(callBack.errhint, gotoSetting, docallBack);
                        }
                    }
                }
            }
        }
    }

    /**
     * 权限设置提示框
     *
     * @param content
     */
    public void showWarnPop(String content, WindowUtils.ClickCallback confirmCallback, WindowUtils.ClickCallback cancleCallback) {
        mActivity.showPromptPopWindow(content, confirmCallback, cancleCallback, true, "设置", true);
    }




}
