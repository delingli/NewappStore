package com.bbx.appstore.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bbx.appstore.adapter.ClickListener;
import com.bbx.appstore.adapter.VpAdapter;
import com.bbx.appstore.api.Report;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.support.MyOnPageChangeListener;
import com.bbx.support.MyViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangyong on 18-4-14.
 */

public class StoreRepeatPlayView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StoreRepeatPlayView";
    private TextView mTvRecommed, mTvDesktop;
    private LinearLayout mLlDot;
    private MyViewPager mVpApp;
    private List<View> mDots;
    private VpAdapter mAdapter;
    private int lastPosition = 0, totalCount;
    private AppListInfo mInfo;
    private StoreADInfo adInfo;
    private long AUTOPLAY_TIME = 3000;
    private int[] IDS = new int[]{0X11224, 0X11225, 0X11226, 0X11227, 0X11228, 0X11229, 0X11230};
    private boolean STOP;

    @Override
    public String getWindowId() {
        return TAG;
    }

    public StoreRepeatPlayView(StoreADInfo adInfo) {
        this.adInfo = adInfo;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        mInfo = (AppListInfo) extra;
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_repeat_play");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.gravity = Gravity.BOTTOM;
        viewParams.height = -1;
    }

    @Override
    public void onViewCreated(View view) {
        findId(view);
        initResUi();
        initNetUi(view);
        autoPlay();
        DownloadLoopAndInstall.getInstance().setDNIListener(this);
    }

    private void initNetUi(View view) {

        mDots = addDots(view, mLlDot, mInfo.list.size());
        mAdapter = new VpAdapter(mContext, mInfo, new ClickListener() {
            @Override
            public void onClick() {
                STOP = true;
            }
        });
        Log.e(TAG, "vp " + mVpApp);
        mVpApp.setAdapter(mAdapter);
        mVpApp.setPageTransformer(false, new VpAdapter.AlphaTransformer());
        mVpApp.setOffscreenPageLimit(mInfo.list.size());
        mVpApp.setPageMargin(-20);
        mVpApp.setOnPageChangeListener(new MyOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mDots.get(lastPosition).setSelected(false);
                mDots.get(position).setSelected(true);
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //初始
        mDots.get(lastPosition).setSelected(true);
        totalCount = mInfo.list.size();
    }

    private void initResUi() {
        Drawable icon = FindRes.getDrawable("store_return_desktop");
        icon.setBounds(0, 0, icon.getMinimumWidth(), icon.getMinimumHeight());
        mTvDesktop.setCompoundDrawables(icon, null, null, null);
        mTvDesktop.setCompoundDrawablePadding((int) Utils.dipToPixels(8, mContext));

        mTvDesktop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mInfo) {
                    AppDetailInfo appDetailInfo = mInfo.list.get(0);
                    if (null != appDetailInfo)
                        Report.getInstance().reportUrl(mContext, mInfo.rtp_method, appDetailInfo.rpt_dl, false, 0);
                }
                finishCallBack(StoreRepeatPlayView.this);
            }
        });
    }


    private void findId(View view) {
        mTvRecommed = (TextView) view.findViewById(FindRes.getId("store_repeat_play_recommend_tv"));
        mTvDesktop = (TextView) view.findViewById(FindRes.getId("store_repeat_play_return_desktop"));
        mLlDot = (LinearLayout) view.findViewById(FindRes.getId("store_repeat_play_dot_ll"));
        mVpApp = (MyViewPager) view.findViewById(FindRes.getId("store_repeat_play_app_info_vp"));
    }

    /**
     * 动态添加一个点
     *
     * @param linearLayout 添加到LinearLayout布局
     * @return
     */
    public int addDot(final LinearLayout linearLayout, int i) {
        final View dot = new View(mContext);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Drawable backgount = FindRes.getDrawable("store_dot_selector");
        dotParams.width = 48;
        dotParams.height = 48;
        dotParams.setMargins(8, 0, 8, 0);
        dot.setLayoutParams(dotParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dot.setBackground(backgount);
        } else {
            dot.setBackgroundDrawable(backgount);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            dot.setId(View.generateViewId());
        } else {
            dot.setId(IDS[i]);
        }
        linearLayout.addView(dot);
        return dot.getId();
    }

    /**
     * 添加多个轮播小点到横向线性布局
     *
     * @param linearLayout
     * @param number
     * @return
     */
    public List<View> addDots(View view, final LinearLayout linearLayout, int number) {
        List<View> dots = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            int dotId = addDot(linearLayout, i);
            dots.add(view.findViewById(dotId));
        }
        return dots;
    }

    private Runnable AUTOPLAY = new Runnable() {
        @Override
        public void run() {
            if (STOP) return;
            if (lastPosition == totalCount - 1) {
                mVpApp.setCurrentItem(0);
                Log.d(TAG, "run1: " + lastPosition);
            } else {
                Log.d(TAG, "run2: " + lastPosition);
                mVpApp.setCurrentItem(lastPosition + 1);
            }
            HANDLER.postDelayed(AUTOPLAY, AUTOPLAY_TIME);
        }
    };

    private void autoPlay() {
        HANDLER.postDelayed(AUTOPLAY, AUTOPLAY_TIME);
    }

    @Override
    public void downloadComplete(String pkgName) {
        if (mAdapter != null) mAdapter.upDataDownButton(pkgName);
    }

    @Override
    public void downloadFailed(String pkgName) {
        if (mAdapter != null) mAdapter.upDataDownButton(pkgName);
    }

    @Override
    public void installSuccessful(String pkgName) {
        if (mAdapter != null) mAdapter.upDataDownButton(pkgName);
    }

    @Override
    public void downloadProgress(String pkgName, int progress) {
        if (mAdapter != null) mAdapter.upDataProgress(pkgName, progress);
    }
}
