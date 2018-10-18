package com.bbx.appstore.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.rec.FindRes;

public class StoreNearbyView extends IView {

    private static final String TAG = "StoreNearbyView";
    private StoreADInfo mInfo;

    public StoreNearbyView(StoreADInfo info) {
        mInfo = info;
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_nearby");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.gravity = Gravity.BOTTOM;
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
    }

    private void callBackDestroy() {
        if (null != mInfo) {
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishCallBack(StoreNearbyView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
        callBackDestroy();
    }

    private void initView(View view) {
        ImageView close = (ImageView) view.findViewById(FindRes.getId("bottom_store_close"));
        close.setImageDrawable(FindRes.getDrawable("store_bottom_close"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreNearbyView.this);
            }
        });
        ImageView more = (ImageView) view.findViewById(FindRes.getId("bottom_store_more"));
        more.setImageDrawable(FindRes.getDrawable("store_bottom_more"));
        ImageView icon = (ImageView) view.findViewById(FindRes.getId("bottom_store_icon"));
        icon.setImageDrawable(FindRes.getDrawable("store_bottom_icon"));
        RelativeLayout content = (RelativeLayout) view.findViewById(FindRes.getId("bottom_store_content"));
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                WindowHandler.getInstance().showStoreNearbySecond();
            }
        });
    }

}
