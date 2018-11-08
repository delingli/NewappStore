package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.windows.WindowHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 皇冠
 */

public class StoreCrownView extends BaseSuspensionView {
    private static final String TAG = "StoreCrownView";
    private ImageView icon;
    private ImageView close;
    private RelativeLayout root;
    private int x;
    private int y;

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_suspension_crown");
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        if (DBG) Log.d(SConstant.TAG, "DB_STORE# onCreate");
    }

    public StoreCrownView(StoreADInfo mInfo) {
        super(mInfo);
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
    }

    @Override
    public String getWindowId() {
        return StoreCrownView.class.getSimpleName();
    }

    @Override
    protected void showView(final AppDetailInfo appDetailInfo) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                root.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(appDetailInfo.icon, icon, options);
                Report.getInstance().reportListUrl(mContext, appDetailInfo.rtp_method, appDetailInfo.rpt_ss, appDetailInfo.flag_replace, null);
                if (DBG) {
                    Log.d(SConstant.TAG, "StoreCrownView 显示上报成功...");
                }
            }
        });
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toHandlerClick(appDetailInfo, new ClickInfo(x, y));
            }
        });
        callBackDestroy();
    }

    @Override
    protected void initView(View view) {
        root = (RelativeLayout) view.findViewById(FindRes.getId("rl_suspension_crown_root"));
        close = (ImageView) view.findViewById(FindRes.getId("iv_suspension_crown_close"));
        close.setImageDrawable(FindRes.getDrawable("store_suspension_black_close"));
        ImageView crownHead = (ImageView) view.findViewById(FindRes.getId("iv_suspension_crown_head"));
        crownHead.setImageDrawable(FindRes.getDrawable("store_suspension_crown"));
        RelativeLayout rl_suspension_crown_bg = (RelativeLayout) view.findViewById(FindRes.getId("rl_suspension_crown_bg"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rl_suspension_crown_bg.setBackground(FindRes.getDrawable("store_bg_crown"));
        } else {
            rl_suspension_crown_bg.setBackgroundDrawable(FindRes.getDrawable("store_bg_crown"));
        }

        icon = (ImageView) view.findViewById(FindRes.getId("iv_suspension_crown_icon"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreCrownView.this);
            }
        });
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
    }
}
