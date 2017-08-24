package com.myjf.gcs.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.android.netactivity.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 弹窗辅助类
 * Created by renmingming on 15/9/30.
 */
public class WindowUtils implements OnClickListener {

    private static final String LOG_TAG = "WindowUtils";

    private Dialog mDialog;

    private boolean isComfirmDialog;

    private View mView;

    private final HashMap<View, ClickCallback> callbacks = new HashMap<View, ClickCallback>();

    long olderClickTime = 0L;

    private Context mContext;

    private final ArrayList<Integer> hids = new ArrayList<Integer>();

    private List<Dialog> views = new ArrayList<Dialog>();

    public static synchronized WindowUtils getInstance(Context context) {

        WindowUtils windowUtils = new WindowUtils(context);
        return windowUtils;
    }

    private WindowUtils(Context context) {
        mContext = context;

    }

    /**
     * 隐藏对话框
     * 功能详细描述
     */
    public void hidePopupWindow(boolean isAll) {
        if (isAll) {
            for (int i = 0; i < views.size(); i++) {
                if (views.get(i) != null && views.get(i).isShowing()) {
                    views.get(i).cancel();
                }
            }
            views.clear();
        } else {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
                views.remove(views.size() - 1);
            }
        }
    }

    /**
     * 显示弹出框
     *
     * @param layoutId    dialog布局id
     * @param dialogStyle dialog的style
     */
    public View showPopupWindow(int layoutId, int dialogStyle, boolean isOnTouchOutsideExit, int gravity) {
        return showPopupWindow(layoutId, dialogStyle, -1, true, isOnTouchOutsideExit, gravity);
    }

    /**
     * 显示弹出框
     *
     * @param layoutId    dialog布局id
     * @param dialogStyle dialog的style
     */
    public View showPopupWindow(int layoutId, int dialogStyle, int animStyle, boolean isOnTouchOutsideExit, int gravity) {
        return showPopupWindow(layoutId, dialogStyle, animStyle, true, isOnTouchOutsideExit, gravity);
    }

    public View showPopupWindow(int layoutId, int dialogStyle, int animStyle, boolean isOnTouchOutsideExit, int gravity, DestoryCallback destoryCallback) {
        return showPopupWindow(layoutId, dialogStyle, animStyle, true, isOnTouchOutsideExit, gravity, destoryCallback);
    }

    public View showPopupWindow(int layoutId, int dialogStyle, int animStyle, boolean isShownCancle, boolean isOnTouchOutsideExit, int gravity) {
        return showPopupWindow(layoutId, dialogStyle, animStyle, isShownCancle, isOnTouchOutsideExit, gravity, null);
    }

    /**
     * 显示弹出框
     *
     * @param layoutId      dialog布局id
     * @param dialogStyle   dialog的style
     * @param isShownCancle 是否需要取消按钮
     */
    public View showPopupWindow(int layoutId, int dialogStyle, int animStyle, boolean isShownCancle, boolean isOnTouchOutsideExit, int gravity, final DestoryCallback destoryCallback) {

        isComfirmDialog = layoutId == R.layout.dialog_cancel_comfrim;

        mView = LayoutInflater.from(mContext).inflate(layoutId, null);
        if (isComfirmDialog) {
            TextView cancleBt = (TextView) mView.findViewById(R.id.dialog_cancel);
            View line = mView.findViewById(R.id.dialog_divider);
            if (!isShownCancle) {
                cancleBt.setVisibility(View.GONE);
                line.setVisibility(View.GONE);
            } else {
                cancleBt.setVisibility(View.VISIBLE);
                line.setVisibility(View.VISIBLE);
            }
        }

        mDialog = new Dialog(mContext, dialogStyle);
        mDialog.setContentView(mView);
        if (isOnTouchOutsideExit) {
            mDialog.setCanceledOnTouchOutside(true);
        } else {
            mDialog.setCanceledOnTouchOutside(false);
        }
        WindowManager m = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = m.getDefaultDisplay();
        LayoutParams p = mDialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.gravity = gravity;
        p.width = (int) (d.getWidth());    //宽度设置为全屏
        mDialog.getWindow().setAttributes(p);     //设置生效
        if (animStyle > 0) {
            mDialog.getWindow().setWindowAnimations(animStyle);
        } else {
            mDialog.getWindow().setWindowAnimations(R.style.dialog_bg_style);
        }
        try {
            mDialog.show();
        } catch (Exception e) {
            if (mDialog != null) {
                hidePopupWindow(false);
            }
        }
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    if (destoryCallback != null) {
                        destoryCallback.destory();
                    }
                }
                return false;
            }
        });
        views.add(mDialog);
        return mView;
    }

    /**
     * <dismiss Dialog>
     * <功能详细描述>
     *
     * @see [类、类#方法、类#成员]
     */
    public void dismissDialog() {
        hidePopupWindow(false);
    }

    public void registClickEvent(int vid, ClickCallback callback) {
        View v = mView.findViewById(vid);
        v.setOnClickListener(this);
        callbacks.put(v, callback);
    }

    @Override
    public void onClick(final View v) {
        long currentClickTime = System.currentTimeMillis();

        if (currentClickTime - olderClickTime > 500) {

            olderClickTime = currentClickTime;

            ClickCallback cb = callbacks.get(v);
            if (hids.contains(v.getId())) {

                hidePopupWindow(false);
            }
            if (cb != null) {

                cb.onClick(v);

            }

        }
    }

    public void setDismissCallback(DialogInterface.OnDismissListener onDismissListener) {
        if (mDialog != null && onDismissListener != null) {
            mDialog.setOnDismissListener(onDismissListener);
        }
    }

    /**
     * 注册隐藏popwindow 事件
     *
     * @param hidId
     */
    public void registhideListener(int hidId) {
        hids.add(hidId);
        View v = mView.findViewById(hidId);
        v.setOnClickListener(this);
    }

    public interface ClickCallback {
        void onClick(View v);
    }

    public interface DestoryCallback {
        void destory();
    }

    public View getmView() {
        return mView;
    }
}
