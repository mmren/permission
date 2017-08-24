package com.myjf.gcs.permission;

/**
 * Created by renmingming on 2016/10/24.
 */
public abstract class GetPermissonsCallback {
    public String errhint;

    public GetPermissonsCallback() {
    }
    public GetPermissonsCallback(String errhint) {
        this.errhint = errhint;
    }

    public  abstract  void  requestPermissions();
    public  void  AfterRequestPermissions(boolean isSuccess){};
