package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.rec.FindRes;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 玻璃瓶样式
 */
public class StoreGlassView extends BaseSuspensionView {
    private ImageView icon;
    private ImageView close;
    private RelativeLayout fl_content;
    private int x;
    private int y;

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_suspension_glass");
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        if (DBG) Log.d(SConstant.TAG, "DB_STORE# onCreate");
    }

    public StoreGlassView(StoreADInfo mInfo) {
        super(mInfo);
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
    }

    @Override
    public String getWindowId() {
        return StoreGlassView.class.getSimpleName();
    }

    @Override
    protected void showView(final AppDetailInfo appDetailInfo) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                fl_content.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(appDetailInfo.icon, icon, options);
                Report.getInstance().reportListUrl(mContext, appDetailInfo.rtp_method, appDetailInfo.rpt_ss, appDetailInfo.flag_replace, null);
                if (DBG) {
                    Log.d(SConstant.TAG, "StoreGlassView 显示上报成功...");
                }
            }
        });
        fl_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toHandlerClick(appDetailInfo, new ClickInfo(x, y));
            }
        });
        callBackDestroy();
    }


    @Override
    protected void initView(View view) {

        fl_content = (RelativeLayout) view.findViewById(FindRes.getId("fl_suspension_glass_content"));
        close = (ImageView) view.findViewById(FindRes.getId("iv_suspension_glass_close"));
        close.setImageDrawable(FindRes.getDrawable("store_suspension_black_close"));
        ImageView glassHead = (ImageView) view.findViewById(FindRes.getId("iv_suspension_glass_head"));
        glassHead.setImageDrawable(FindRes.getDrawable("store_suspension_glass"));
        RelativeLayout rl_suspension_glass_bg = (RelativeLayout) view.findViewById(FindRes.getId("rl_suspension_glass_bg"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rl_suspension_glass_bg.setBackground(FindRes.getDrawable("store_bg_glass"));
        } else {
            rl_suspension_glass_bg.setBackgroundDrawable(FindRes.getDrawable("store_bg_glass"));
        }

        icon = (ImageView) view.findViewById(FindRes.getId("iv_suspension_glass_icon"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreGlassView.this);
            }
        });
        fl_content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
    }

}
