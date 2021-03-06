package com.bbx.appstore.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DManager;
import com.bbx.appstore.rec.FindRes;

public class StoreDownStoreView extends IView {

    private static final String TAG = "StoreDownStoreView";

    private StoreADInfo mInfo;

    private LinearLayout mContent;
    private TextView mTitle, mTips, mClose, mOneKey;

    public StoreDownStoreView(StoreADInfo info) {
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
        return FindRes.getLayout("store_view_down_store");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {

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
                    finishCallBack(StoreDownStoreView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
        if (null != mInfo && !TextUtils.isEmpty(mInfo.desc)) {
            String[] desc = mInfo.desc.trim().split("\\|");
            if (desc.length > 3) {
                mTitle.setText(desc[0]);
                mTips.setText(desc[1]);
                mClose.setText(desc[2]);
                mOneKey.setText(desc[3]);
                Report.getInstance().reportListUrl(mContext, "GET", mInfo.s_rpt, 0, null);
                callBackDestroy();
                return;
            }
        }
        finishCallBack(StoreDownStoreView.this);
    }
int x,y;
    private void initView(View view) {
        mContent = (LinearLayout) view.findViewById(FindRes.getId("down_store_content"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mContent.setBackground(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        } else {
            mContent.setBackgroundDrawable(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        }
        mTitle = (TextView) view.findViewById(FindRes.getId("down_store_title"));
        mTips = (TextView) view.findViewById(FindRes.getId("down_store_tips"));
        mClose = (TextView) view.findViewById(FindRes.getId("down_store_close"));
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreDownStoreView.this);
            }
        });
        mOneKey = (TextView) view.findViewById(FindRes.getId("down_store_one_key"));
        mOneKey.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        mOneKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mInfo) {
                    DManager.getInstance(mContext).startDownload(mContext, mInfo);
                    Report.getInstance().reportListUrl(mContext, "GET", mInfo.c_rpt, 0, new ClickInfo(x,y));
                }
                finishCallBack(StoreDownStoreView.this);
            }
        });
    }
}
