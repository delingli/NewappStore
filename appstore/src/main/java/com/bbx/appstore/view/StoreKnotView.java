package com.bbx.appstore.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;

/**
 * 中国结样式
 */
public class StoreKnotView extends BaseSuspensionView {
    private ImageView icon;
    private ImageView close;
    private RelativeLayout root;
    private int x;
    private int y;

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_suspension_knot");
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        if (DBG) Log.d(SConstant.TAG, "DB_STORE# onCreate");
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .displayer(new CircleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public StoreKnotView(StoreADInfo mInfo) {
        super(mInfo);
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
    }

    @Override
    public String getWindowId() {
        return StoreKnotView.class.getSimpleName();
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
                    Log.d(SConstant.TAG, "StoreKnotView 显示上报成功...");
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

        root = (RelativeLayout) view.findViewById(FindRes.getId("rl_suspension_knot_root"));
        RelativeLayout  rl_suspension_knot_bg = (RelativeLayout) view.findViewById(FindRes.getId("rl_suspension_knot_bg"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rl_suspension_knot_bg.setBackground(FindRes.getDrawable("store_suspension_chinaknot"));
        } else {
            rl_suspension_knot_bg.setBackgroundDrawable(FindRes.getDrawable("store_suspension_chinaknot"));
        }

        close = (ImageView) view.findViewById(FindRes.getId("iv_suspension_knot_close"));
        close.setImageDrawable(FindRes.getDrawable("store_suspension_close"));
        icon = (ImageView) view.findViewById(FindRes.getId("iv_suspension_knot_icon"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreKnotView.this);
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
