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

public abstract class BaseActivity extends CoreActivity {

    public WindowUtils mWindowUtils;

    private int statusBarH = 0;
    protected boolean isBind = false;
    private View emptyView;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusBarH = ScreenUtil.getStatusHeight(this);
        if (mWindowUtils == null) {
            mWindowUtils = WindowUtils.getInstance(this);
        }
        setLeftBtnDrawable(getResources().getDrawable(com.android.netactivity.R.mipmap.return_black), 1, "");
        setTitleTextSize(18);
        setActionbarColor();
        permissionManager = PermissionManager.getInstance(this);
    }
    public void setNoActionbar() {
        addTitleBar.hide();
        setContentTop();
        BarTextColorUtils.StatusBarLightMode(this);
        ImmerseHelper.setSystemBarTransparent(this, R.color.transparent_all);
    }

    public void sethaveStatusbarNoActionbar() {
        addTitleBar.hide();
        setContentAfterStatusBarNew();
    }

    // only for this app
    public void setActionbarColor() {
        setActionBarBackGround(R.color.white);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setActionBarTitleColor(getResources().getColor(R.color.color_333333, getTheme()));
        } else {
            setActionBarTitleColor(getResources().getColor(R.color.color_333333));
        }
        int res = BarTextColorUtils.StatusBarLightMode(this);
        if (4 == res) {
            setActionBarBackGround(R.color.color_00a4ff);
        }
    }

    private void setTitleTextSize(float size) {
        ((TextView) addTitleBar.getTitlebarLayout().findViewById(R.id.title)).setTextSize(size);
    }
    //默认状态栏和标题栏透明颜色
    protected void setContentAfterStatusBarNew() {
        BarTextColorUtils.StatusBarLightMode(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            rootView.setPadding(0, statusBarH, 0, 0);
        } else {
            rootView.setPadding(0, 0, 0, 0);
        }
    }

    //回到用户中心
    public void gotoMine() {
        Intent intent = new Intent(this, HomeActivity_.class);
        intent.putExtra("selectIndex", "hxgj");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //回到首页
    public void goBack() {
        Intent intent = new Intent(this, HomeActivity_.class);
        startActivity(intent);
    }
    @Override
    public void goToLogin() {
        UserProfile.clearUserOnly();
        EventBus.getDefault().postSticky(new UnBindEvent(BaseActivity.class.getName(), ""));
        Intent intent = new Intent(this, LoginOrRegisterActivity_.class);
        intent.putExtra("backtohome", 1);
        startActivity(intent);
    }

    @Override
    public void goToHome() {
        Intent intent = new Intent(this, HomeActivity_.class);
        intent.putExtra("selectIndex", "home");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public WindowUtils getWindowUtils() {
        return mWindowUtils;
    }



    public <D extends BaseBean> GsonRequest inStanceGsonRequest(int method, String urlstr, Class<D> data, Map<String, String> params, boolean isNeedShowDialog) {
        return super.inStanceGsonRequest(method, urlstr, urlstr, data, params, VolleyUtil.getHeadersStatic(), isNeedShowDialog, true);
    }

    public <D extends BaseBean> GsonRequest inStanceGsonRequest(int method, String urlstr, String tag, Class<D> data, Map<String, String> params, boolean isNeedShowDialog) {
        return super.inStanceGsonRequest(method, urlstr, tag, data, params, VolleyUtil.getHeadersStatic(), isNeedShowDialog, true);
    }

    public <D extends BaseBean> GsonRequest inStanceGsonRequest(int method, String urlstr, Class<D> data, Map<String, String> params) {
        return super.inStanceGsonRequest(method, urlstr, urlstr, data, params, VolleyUtil.getHeadersStatic(), true, true);
    }

    public <D extends BaseBean> GsonRequest inStanceGsonRequest(int method, String urlstr, Class<D> data, Map<String, String> params, boolean isNeedShowDialog, boolean canDialogBackClose) {
        return super.inStanceGsonRequest(method, urlstr, urlstr, data, params, VolleyUtil.getHeadersStatic(), isNeedShowDialog, canDialogBackClose);
    }

    @Override
    public <D extends NetBean> boolean doAfterBusinessError(String urlTag, D data) {
        if (data instanceof BaseBean) {
            BaseBean baseBean = (BaseBean) data;
            if (formatUpdate(baseBean)) {
                return true;
            }
            return doAfterBusinessErrorIml(urlTag, baseBean);
        }
        return false;
    }

    @Override
    public <D extends NetBean> void doAfterBusinessSuccess(String urlTag, D data) {
        if (data instanceof BaseBean)
            doAfterBusinessSuccessIml(urlTag, (BaseBean) data);
    }

    public abstract <D extends BaseBean> boolean doAfterBusinessErrorIml(String urlTag, D data);

    public abstract <D extends BaseBean> void doAfterBusinessSuccessIml(String urlTag, D data);


    //////升级///////////////////////////////////
    public boolean formatUpdate(BaseBean baseBean) {
        if (baseBean.getRetcode().equals("-9999") || baseBean.getRetcode().equals("-8888")) {
            UpdateBean updateBean = UserProfile.getInstance(getApplicationContext()).strToUpdate(baseBean.getRetcode(), baseBean.getRetinfo());
            UserProfile.getInstance(this).saveUpdateDate(updateBean);
            ((GcsApplication) getApplication()).update(this, baseBean.getRetcode(), updateBean.getComment(), updateBean.getAppUpgradeUrl());
            clearError();
            return true;
        }
        return false;
    }

    public void clearError() {

    }
    ///////////////////////////////////




    @Override
    protected void onResume() {
        super.onResume();
        TCAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TCAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((GcsApplication) getApplicationContext()).stopUpdate();
        if (mWindowUtils != null) {
            mWindowUtils.hidePopupWindow(true);
        }
    }


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CollectService service = ((CollectService.MyBinder) iBinder).getService();
            isBind = true;
            if (service != null && activity != null && activity.get() != null) {
                service.setActivity((BaseActivity) activity.get());
                service.collect();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    public void insertDummyContact() {
        Intent startIntent = new Intent(this, CollectService.class);
        bindService(startIntent, mServiceConnection, BIND_AUTO_CREATE);
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

    /**
     * @param status 0:有消息内容  1：没有消息内容
     */
    public void hidOrShowNUllView(int status) {
        if (emptyView == null) {
            return;
        }
        emptyView.setVisibility(View.VISIBLE);
        switch (status) {
            case 0:
                emptyView.setVisibility(View.GONE);
                break;
            case 1:
                ((ImageView) emptyView.findViewById(R.id.empty_iv)).setImageResource(R.mipmap.no_news);
                ((TextView) emptyView.findViewById(R.id.empty_tv)).setText(getResources().getString(R.string.message_empty_tip));
                emptyView.findViewById(R.id.retry_btn).setVisibility(View.GONE);
                break;
            case 2:
                ((ImageView) emptyView.findViewById(R.id.empty_iv)).setImageResource(R.mipmap.no_net);
                emptyView.findViewById(R.id.retry_btn).setVisibility(View.VISIBLE);
                ((TextView) emptyView.findViewById(R.id.empty_tv)).setText(R.string.no_net);
        }
    }

    /**
     * @param status 0:有消息内容  1：没有消息内容
     */
    public void hidOrShowNUllView(int status, View showView) {
        hidOrShowNUllView(status);
        switch (status) {
            case 0:
                showView.setVisibility(View.VISIBLE);
                break;
            case 1:
                showView.setVisibility(View.VISIBLE);
                break;
            case 2:
                showView.setVisibility(View.INVISIBLE);
        }
    }

    public void registeEmptyView(View v) {
        this.emptyView = v;
    }


    //////////////////////弹出框///////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void showLoginConfirm(String content, final int backtohome) {
        UserProfile.clearUserOnly();
        EventBus.getDefault().postSticky(new UnBindEvent(BaseActivity.class.getName(), ""));
        View view = View.inflate(this, R.layout.dialog_cancel_comfrim, null);
        Dialog dialog = NetLoadingDialog.getInstance().loading(this, view, "relogin");
        TextView titleTV = (TextView) view.findViewById(R.id.title);
        TextView confirm = (TextView) view.findViewById(com.android.netactivity.R.id.dialog_confirm);
        confirm.setText("重新登录");
        titleTV.setText(content);
        view.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing()) {

                    Intent intent = new Intent(BaseActivity.this, LoginOrRegisterActivity_.class);
                    intent.putExtra("backtohome", backtohome);
                    startActivity(intent);
                }
                NetLoadingDialog.getInstance().dismissDialog("relogin");
            }
        });
        view.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing()) {
                    Intent intent = new Intent(BaseActivity.this, HomeActivity_.class);
                    startActivity(intent);
                }
                NetLoadingDialog.getInstance().dismissDialog("relogin");
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    if (!isFinishing()) {
                        Intent intent = new Intent(BaseActivity.this, HomeActivity_.class);
                        startActivity(intent);
                    }
                }
                NetLoadingDialog.getInstance().dismissDialog("relogin");
                return false;
            }
        });
    }

    public void showPhoneDialog() {
        mWindowUtils.showPopupWindow(R.layout.dialog_cancel_comfrim, R.style.loading_dialog, R.style.anim_menu_bottombar, true, Gravity.CENTER);
        View rootView = mWindowUtils.getmView();
        TextView titleTV = (TextView) rootView.findViewById(R.id.title);
        String str = getString(R.string.service_tel_content, "025-69633066");
        SpannableString styledText = new SpannableString(str);
        styledText.setSpan(new TextAppearanceSpan(this, R.style.text_333333_12), str.indexOf("客服在线时间：周一至周五8:30-17:30"), str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleTV.setText(styledText, TextView.BufferType.SPANNABLE);
        TextView confirm = (TextView) rootView.findViewById(R.id.dialog_confirm);
        confirm.setText(getString(R.string.call));
        mWindowUtils.registhideListener(R.id.dialog_cancel);
        mWindowUtils.registhideListener(R.id.dialog_confirm);
        mWindowUtils.registClickEvent(R.id.dialog_confirm, new WindowUtils.ClickCallback() {
            @Override
            public void onClick(View v) {
                GeneralUtils.setTel(getApplicationContext(), "02569633066");
            }
        });
    }

    public void showRZDialog(final WindowUtils.ClickCallback callback) {
        mWindowUtils.showPopupWindow(R.layout.dialog_rz, R.style.loading_dialog, R.style.anim_menu_bottombar, true, Gravity.CENTER);
        View rootView = mWindowUtils.getmView();
        TextView confirm = (TextView) rootView.findViewById(R.id.dialog_confirm);
        mWindowUtils.registhideListener(R.id.dialog_confirm);
        mWindowUtils.registClickEvent(R.id.dialog_confirm, new WindowUtils.ClickCallback() {
            @Override
            public void onClick(View v) {
                callback.onClick(v);
            }
        });
    }

    public void showPromptPopWindow(String content) {
        showPromptPopWindow(content, null, false);
    }

    public void showPromptPopWindow(String content, WindowUtils.ClickCallback callback, boolean isShowCancel) {
        showPromptPopWindow(content, callback, isShowCancel, "", true);
    }

    public void showOpenGesturePopWindow(String content, WindowUtils.ClickCallback callback, WindowUtils.DestoryCallback destoryCallback) {
        if (!isFinishing()) {
            if (mWindowUtils == null) {
                mWindowUtils = WindowUtils.getInstance(this);
            }
            mWindowUtils.hidePopupWindow(true);
            mWindowUtils.showPopupWindow(com.android.netactivity.R.layout.dialog_cancel_comfrim, com.android.netactivity.R.style.loading_dialog, com.android.netactivity.R.style.dialog_bg_style, false, false, Gravity.CENTER, destoryCallback);
            View rootView = mWindowUtils.getmView();

            TextView titleTV = (TextView) rootView.findViewById(com.android.netactivity.R.id.title);
            titleTV.setText(content);
            mWindowUtils.registhideListener(com.android.netactivity.R.id.dialog_confirm);
            mWindowUtils.registhideListener(com.android.netactivity.R.id.dialog_cancel);
            if (callback != null) {
                mWindowUtils.registClickEvent(com.android.netactivity.R.id.dialog_confirm, callback);
            }
        }
    }

    public void showPromptPopWindow(String content, WindowUtils.ClickCallback callback, boolean isShowCancel, boolean isHide) {
        showPromptPopWindow(content, callback, isShowCancel, "", isHide);
    }

    public void showPromptPopWindow(String content, WindowUtils.ClickCallback callback, boolean isShowCancel, String confirmStr) {
        showPromptPopWindow(content, callback, isShowCancel, confirmStr, true);
    }

    public void showPromptPopWindow(String content, WindowUtils.ClickCallback callback, boolean isShowCancel, String confirmStr, boolean isHide) {
        showPromptPopWindow(content, callback, null, isShowCancel, confirmStr, isHide);
    }

    public void showPromptPopWindow(String content, WindowUtils.ClickCallback confirm_callback, WindowUtils.ClickCallback cancel_callback, boolean isShowCancel, String confirmStr, boolean isHide) {
        if (!isFinishing()) {
            if (mWindowUtils == null) {
                mWindowUtils = WindowUtils.getInstance(this);
            }
            if (isHide) {
                mWindowUtils.hidePopupWindow(false);
            }
            mWindowUtils.showPopupWindow(com.android.netactivity.R.layout.dialog_cancel_comfrim, com.android.netactivity.R.style.loading_dialog, com.android.netactivity.R.style.dialog_bg_style, isShowCancel, true, Gravity.CENTER);
            View rootView = mWindowUtils.getmView();

            if (!StringUtils.isEmpty(confirmStr)) {
                ((TextView) rootView.findViewById(com.android.netactivity.R.id.dialog_confirm)).setText(confirmStr);
            }
            TextView titleTV = (TextView) rootView.findViewById(com.android.netactivity.R.id.title);
            titleTV.setText(content);
            mWindowUtils.registhideListener(com.android.netactivity.R.id.dialog_confirm);
            mWindowUtils.registhideListener(com.android.netactivity.R.id.dialog_cancel);
            if (confirm_callback != null) {
                mWindowUtils.registClickEvent(com.android.netactivity.R.id.dialog_confirm, confirm_callback);
            }
            if (cancel_callback != null) {
                mWindowUtils.registClickEvent(com.android.netactivity.R.id.dialog_cancel, cancel_callback);
            }
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("确认", okListener)
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Response.ErrorListener errorListener(String urlTag, boolean isNeedShowDialog) {
        return null;
    }

    @Override
    public boolean doAfterError(String urlTag) {
        return false;
    }

    @Override
    public void cancelRequests() {

    }

    @Override
    public String getCurrentUrlTag() {
        return null;
    }

    @Override
    public void setCurrentUrlTag(String tag) {

    }

    @Override
    protected void onFragmentInteractionImpl(Uri uri) {

    }
}
