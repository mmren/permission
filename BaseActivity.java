package com.myjf.gcs.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.netactivity.app.CoreActivity;
import com.android.netactivity.app.ImmerseHelper;
import com.android.netactivity.net.GsonRequest;
import com.android.netactivity.net.NetBean;
import com.android.netactivity.net.ScreenUtil;
import com.android.netactivity.net.StringUtils;
import com.android.netactivity.net.VolleyUtil;
import com.android.netactivity.ui.NetLoadingDialog;
import com.android.volley.Response;
import com.myjf.gcs.R;
import com.myjf.gcs.bean.BaseBean;
import com.myjf.gcs.bean.UpdateBean;
import com.myjf.gcs.bean.event.UnBindEvent;
import com.myjf.gcs.config.UserProfile;
import com.myjf.gcs.permission.PermissionManager;
import com.myjf.gcs.service.CollectService;
import com.myjf.gcs.ui.home.HomeActivity_;
import com.myjf.gcs.ui.login_register.LoginOrRegisterActivity_;
import com.myjf.gcs.util.BarTextColorUtils;
import com.myjf.gcs.util.GeneralUtils;
import com.tendcloud.tenddata.TCAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

/**
 * Created by renmingming on 2016/10/24.
 * 基础activity类， goToLogin，goToHome
 */

public  class YourBaseActivity {

    public WindowUtils mWindowUtils;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager = PermissionManager.getInstance(this);
    }
    

    public WindowUtils getWindowUtils() {
        return mWindowUtils;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWindowUtils != null) {
            mWindowUtils.hidePopupWindow(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionManager.onActivityResult(requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.onRequestPermissionsResult(requestCode, grantResults);
    }

    
}
