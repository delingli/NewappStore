package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.appstore.windows.WindowHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 做一个悬浮窗的Base
 */
public abstract class BaseSuspensionView extends IView {
    protected AppListInfo mAppInfoList;
    protected List<AppDetailInfo> cacheInfo;
    protected StoreADInfo mInfo;
    protected DisplayImageOptions options;

    public BaseSuspensionView(StoreADInfo mInfo) {
        this.mInfo = mInfo;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .displayer(new RoundedBitmapDisplayer(Utils.dip2Pixels(4, mContext)))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        RequestApi.getInstance().fetchAppListInfo(mContext, SConstant.TYPE_LIST, SConstant.CID_BATTERY_TIPS, Page.listPage, 4, null, new RequestApi.ApiRequestListener() {
            @Override
            public void onCallBack(AppListInfo appListInfo) {
                if (mAppInfoList == null)
                    mAppInfoList = appListInfo;
                toBeInstall(appListInfo.list);
            }

            @Override
            public void onError(String e) {
                Page.listPage = null;
                if (DBG) {
                    Log.d(SConstant.TAG, "请求应用列表失败");
                }
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(BaseSuspensionView.this);
                    }
                });
            }
        });

    }

    /**
     * 全装
     *
     * @param list
     */
    private void toBeInstall(List<AppDetailInfo> list) {
        if (null != list) {
            if (DBG) {
                Log.d(SConstant.TAG, "获取列表成功");
            }
            cacheInfo = new ArrayList<>();
            //填充数据
            Random random = new Random();
            int index = random.nextInt(list.size());
            AppDetailInfo appDetailInfo = list.get(index);
            if (appDetailInfo != null) {
                cacheInfo.add(appDetailInfo);
                showView(appDetailInfo);
            }
        } else {
            if (DBG) {
                Log.e(SConstant.TAG, " 获取应用列表为null");
            }
            finishCallBack(BaseSuspensionView.this);
        }

    }

    /**
     * 显示View
     *
     * @param appDetailInfo
     */
    protected abstract void showView(final AppDetailInfo appDetailInfo);

    /**
     * 初始化id
     *
     * @param view
     */
    protected abstract void initView(View view);


    protected void callBackDestroy() {
        if (null != mInfo) {
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishCallBack(BaseSuspensionView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    /**
     * 同一处理点击
     *
     * @param appDetailInfo
     */
    protected void toHandlerClick(AppDetailInfo appDetailInfo,ClickInfo clickInfo) {
        if (null != mInfo && "1".equals(mInfo.ci)) {
            mAppInfoList.list = cacheInfo;
            Intent intent = new Intent();
            intent.putExtra(SConstant.RECOMMEND_LIST, mAppInfoList);
            WindowHandler.getInstance().showDialog(intent);
        } else {
            if (ApkUtils.DOWNLOADING != ApkUtils.getStatus(mContext, appDetailInfo.appid, appDetailInfo.apk, Integer.valueOf(appDetailInfo.versioncode))) {
                DmBean dmBean = new DmBean();
                dmBean.appId = appDetailInfo.appid;
                dmBean.appName = appDetailInfo.appname;
                dmBean.downUrl = appDetailInfo.href_download;
                dmBean.packageName = appDetailInfo.apk;
                dmBean.versionCode = appDetailInfo.versioncode;
                dmBean.size = appDetailInfo.size;
                dmBean.iconUrl = appDetailInfo.icon;
                dmBean.repDc = appDetailInfo.rpt_dc;
                dmBean.repInstall = appDetailInfo.rpt_ic;
                dmBean.repAc = appDetailInfo.rpt_ac;
                dmBean.method = appDetailInfo.rtp_method;
                DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                Report.getInstance().reportListUrl(mContext, appDetailInfo.rtp_method, appDetailInfo.rpt_cd, appDetailInfo.flag_replace, clickInfo);
                if (DBG) {
                    Log.d(SConstant.TAG, " 点击上报成功...");
                }
            }
        }
    }
}
