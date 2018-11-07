package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;

/**
 * 鹿角样式
 */
public class StoreAntlersView extends BaseSuspensionView {
    private static final String TAG = "StoreAntlersView";
    private ImageView icon;
    private ImageView close;
    private RelativeLayout rl_suspension_antlers_root;
    private int x;
    private int y;

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_suspension_antlers");
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .displayer(new CircleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        if (DBG) Log.d(SConstant.TAG, "DB_STORE# onCreate");
    }

    public StoreAntlersView(StoreADInfo mInfo) {
        super(mInfo);
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
    }

    @Override
    public String getWindowId() {
        return StoreAntlersView.class.getSimpleName();
    }

    @Override
    protected void showView(final AppDetailInfo appDetailInfo) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                rl_suspension_antlers_root.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(appDetailInfo.icon, icon, options);
                Report.getInstance().reportListUrl(mContext, appDetailInfo.rtp_method, appDetailInfo.rpt_ss, appDetailInfo.flag_replace, null);
                if (DBG) {
                    Log.d(SConstant.TAG, "StoreGlassView 显示上报成功...");
                }
            }
        });
        rl_suspension_antlers_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toHandlerClick(appDetailInfo, new ClickInfo(x, y));
            }
        });
        callBackDestroy();
    }

    @Override
    protected void initView(View view) {
        rl_suspension_antlers_root = (RelativeLayout) view.findViewById(FindRes.getId("rl_suspension_antlers_root"));
        close = (ImageView) view.findViewById(FindRes.getId("suspension_antlers_close"));
        close.setImageDrawable(FindRes.getDrawable("store_suspension_black_close"));
        ImageView antlersHead = (ImageView) view.findViewById(FindRes.getId("suspension_antlers_head"));
        icon = (ImageView) view.findViewById(FindRes.getId("suspension_antlers_icon"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            antlersHead.setBackground(FindRes.getDrawable("store_suspension_antlers"));
        } else {
            antlersHead.setBackgroundDrawable(FindRes.getDrawable("store_suspension_antlers"));
        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreAntlersView.this);
            }
        });
        rl_suspension_antlers_root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
    }
}
