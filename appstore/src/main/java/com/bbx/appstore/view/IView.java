package com.bbx.appstore.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.manager.LayoutParamsBuilder;
import com.bbx.appstore.windows.WindowHandler;

public abstract class IView implements FloatWindowManager.IFloatWindow {
    static boolean DBG = true;
    protected Context mContext;
    protected boolean finish;
    protected final Handler HANDLER = new Handler(Looper.getMainLooper());

    /**
     * @param context ...
     * @param extra ...show方法传进来的广告配置或者其他对象
     */
    @Override
    public void onCreate(Context context, Object extra) {
        mContext = context;
        finish = false;
    }

    @Override
    public View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(onSetLayout(), null);
    }

    /**
     * @return FindRes.getLayout(layoutName);
     */
    protected abstract XmlResourceParser onSetLayout();

    @Override
    public WindowManager.LayoutParams getLayoutParams(Context context) {
        LayoutParamsBuilder layoutParamsBuilder = new LayoutParamsBuilder();

        ViewParams viewParams = new ViewParams();
        setLayoutParams(viewParams);

        int flag;
        if (viewParams.dimBehind) {
            flag = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        } else {
            flag = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }

        WindowManager.LayoutParams wlp = layoutParamsBuilder.width(viewParams.width).height(viewParams.height).gravity(viewParams.gravity).flag(flag).build();
        wlp.type = FloatWindowManager.getWindowType(mContext);
        wlp.dimAmount = 0.5f;
        return wlp;
    }

    /**
     * @param viewParams 窗口配置，不修改则使用默认值
     */
    protected abstract void setLayoutParams(ViewParams viewParams);

    //确保在UI线程
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        HANDLER.removeCallbacksAndMessages(null);
        FloatWindowManager.getInstance().dismiss(floatWindow);
    }

    /**
     * 确保在UI线程
     *
     * **** 注意 ****
     * finishCallBack 的时机，当整个广告所有窗口的生命周期没有完成时，请勿调用此方法
     *
     * @param floatWindow ...
     */
    protected void finishCallBack(final FloatWindowManager.IFloatWindow floatWindow) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                finish(floatWindow);
                WindowHandler.getInstance().destroyCallback();
            }
        });
    }

    public boolean isFinish() {
        return finish;
    }

    @Override
    public void onStop(Context context) {

    }

    @Override
    public void onDestroy(Context context) {
        finish = true;
    }

    class ViewParams {
        int gravity = Gravity.CENTER; //不用解释了吧...
        int width = -1; //MATCH_PARENT
        int height = -2; //WRAP_CONTENT
        boolean dimBehind = true; //背景是否变暗
    }
}
