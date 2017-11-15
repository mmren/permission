# permission
### 实例代码
```
private static final int REQUEST_CODE_CONTACTES = PermissionManager.getRequestCode();
private void read_sms() {
        if (UserProfile.getInstance(activity).getUploadFlagBean().getMessages().equals("0")) {
            String[] strs = new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS};
            PermissionManager.getInstance(activity).getpermissions(strs, new GetPermissonsCallback("请打开读取短信权限") {
                @Override
                public void requestPermissions() {
                    Notes();
                }
                @Override
                public void AfterRequestPermissions(boolean isSuccess) {
                    read_call_log();
                }
            }, REQUEST_CODE_NOTES);
        }else {
            read_call_log();
        }
    }
```