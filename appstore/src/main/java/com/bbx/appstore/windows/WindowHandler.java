package com.bbx.appstore.windows;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.view.IView;
import com.bbx.appstore.view.StoreAntlersView;
import com.bbx.appstore.view.StoreBannerView;
import com.bbx.appstore.view.StoreBatteryTipsView;
import com.bbx.appstore.view.StoreCrownView;
import com.bbx.appstore.view.StoreDetailView;
import com.bbx.appstore.view.StoreDialogView;
import com.bbx.appstore.view.StoreDownStoreView;
import com.bbx.appstore.view.StoreGlassView;
import com.bbx.appstore.view.StoreKnotView;
import com.bbx.appstore.view.StoreListView;
import com.bbx.appstore.view.StoreNearbySecondView;
import com.bbx.appstore.view.StoreNearbyView;
import com.bbx.appstore.view.StoreNewNotifyView;
import com.bbx.appstore.view.StoreNewPowerView;
import com.bbx.appstore.view.StoreNewRecommendView;
import com.bbx.appstore.view.StoreNotifyView;
import com.bbx.appstore.view.StorePowerView;
import com.bbx.appstore.view.StoreRecommendView;
import com.bbx.appstore.view.StoreRepeatPlayView;
import com.bbx.appstore.view.StoreRollWifiView;
import com.bbx.appstore.view.StoreSearchView;
import com.bbx.appstore.view.StoreSecondInstallView;
import com.bbx.appstore.view.StoreSnowManView;
import com.bbx.appstore.view.StoreUpdateListView;
import com.bbx.appstore.view.StoreWifiView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.base.SConstant.CID_INSERT;
import static com.bbx.appstore.base.SConstant.CID_NEW_BOTTOM;

public class WindowHandler {

    private static final String TAG = "WindowHandler";
    private static boolean DBG = true;
    private static WindowHandler windowHandler;
    private boolean initStatus; //控制初始化
    private AppStoreHandler.OnStoreDestroyListener listener;
    private Context mContext;
    private IView windowView;
    protected final Handler HANDLER = new Handler(Looper.getMainLooper());
    private AppListInfo mListInfo = null;
    private long requestCount = 0;

    public synchronized static WindowHandler getInstance() {
        if (windowHandler == null) {
            windowHandler = new WindowHandler();
        }
        return windowHandler;
    }

    private WindowHandler() {

    }

    //application before show
    WindowHandler initStore(Context context, String path, String packageName) {
        if (initStatus) return windowHandler;
        initStatus = true;
        FindRes.initAppStore(context, path, packageName);
        FloatWindowManager.getInstance().init(context);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
        mContext = context;
        if (DBG) Log.e(TAG, "DB_STORE# initStore");
        return windowHandler;
    }

    void setOnDestroyListener(AppStoreHandler.OnStoreDestroyListener listener) {
        this.listener = listener;
    }

    public void destroyCallback() {
        if (null != listener) {
            listener.onDestroy();
            listener = null;
        }
    }

    public void cleanWindow() {
        if (windowView != null && !windowView.isFinish()) {
            windowView.finish(windowView);
            windowView = null;
        }
        RequestApi.getInstance().cancelRequest();
    }

    public void showView(StoreADInfo info, int cid) {
        requestNet(mContext, cid, info);
    }

    /**
     * wifi下上拉
     *
     * @param info
     */
    public void showRollWifiView(AppListInfo info, StoreADInfo adInfo) {
        cleanWindow();
        windowView = new StoreRollWifiView(adInfo);
        FloatWindowManager.getInstance().show(windowView, info);
        if (DBG) Log.e(TAG, "DB_STORE# showRollWifiView");
    }

    /**
     * 轮播
     *
     * @param info
     */
    public void showRepeatPlayView(AppListInfo info, StoreADInfo adInfo) {
        cleanWindow();
        windowView = new StoreRepeatPlayView(adInfo);
        FloatWindowManager.getInstance().show(windowView, info);
        if (DBG) Log.e(TAG, "DB_STORE# showRepeatPlayView");
    }

    /*------ 1 弃用 -------*/
    public void showStoreNotify(StoreADInfo info) { //需要外围传入数据
        cleanWindow();
        windowView = new StoreNotifyView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreNotify");
    }

    public void showStoreNewNotify(StoreADInfo info) { //需要外围传入数据 修改Notify
        cleanWindow();
        windowView = new StoreNewNotifyView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreNotify");
    }

    public void showStoreWifiTips(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreWifiView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreWifiTips");
    }

    /*----- 2 ----*/
    public void showStoreBanner(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreBannerView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreBanner");
    }

    public void showStoreRecommend(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreRecommendView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreRecommend");
    }

    public void showDownStoreView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreDownStoreView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showDownStoreView");
    }

    /*----- 3 ----*/

    public void showStoreNearbyView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreNearbyView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showBottomStoreView");
    }

    public void showStoreNearbySecond(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreNearbySecondView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreNearbySecond");
    }

    /**
     * 悬浮窗-玻璃瓶样式
     *
     * @param info
     */
    public void showStoreGlassView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreGlassView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreGlassView");
    }

    /**
     * 悬浮窗-皇冠样式
     *
     * @param info
     */
    public void showStoreCrownView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreCrownView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreCrownView");
    }

    /**
     * 悬浮窗- 雪人样式
     *
     * @param info
     */
    public void showStoreSnowManView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreSnowManView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreSnowManView");
    }

    /**
     * 悬浮窗- 鹿角样式
     *
     * @param info
     */
    public void showStoreKnotView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreKnotView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreKnotView");
    }

    /**
     * 悬浮窗- 中国结样式
     *
     * @param info
     */
    public void showStoreAntlersView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreAntlersView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreAntlersView");
    }
    /*-------- 4 -------*/

    public void showStorePowerView(StoreADInfo info) {
        cleanWindow();
        windowView = new StorePowerView(info);
        FloatWindowManager.getInstance().show(windowView, null);
    }

    /*-------- 5 -------*/

    public void showSecondInstall() {
        cleanWindow();
        windowView = new StoreSecondInstallView();
        FloatWindowManager.getInstance().show(windowView, null);
    }
    /*-------------------------------------*/

    /*---------------*/

    public void showStoreDetail(Intent intent) {//不需要外围传入数据
        cleanWindow();
        windowView = new StoreDetailView();
        FloatWindowManager.getInstance().show(windowView, intent);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreDetail");
    }

    public void showStoreList(Intent intent) {
        cleanWindow();
        windowView = new StoreListView();
        FloatWindowManager.getInstance().show(windowView, intent);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreList");
    }

    public void showStoreUpdateList(Intent intent) {
        cleanWindow();
        windowView = new StoreUpdateListView();
        FloatWindowManager.getInstance().show(windowView, intent);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreUpdateList");
    }

    public void showDialog(Intent intent) {
        cleanWindow();
        windowView = new StoreDialogView();
        FloatWindowManager.getInstance().show(windowView, intent);
        if (DBG) Log.e(TAG, "DB_STORE# showDialog");
    }

    public void showStoreNewPowerView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreNewPowerView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showStoreNewPowerView");
    }

    public void showNewRecommendView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreNewRecommendView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showNewRecommendView");
    }

    public void showBatteryTipsView(StoreADInfo info) {
        cleanWindow();
        windowView = new StoreBatteryTipsView(info);
        FloatWindowManager.getInstance().show(windowView, null);
        if (DBG) Log.e(TAG, "DB_STORE# showBatteryTipsView");
    }

    public void showSearchView(Intent intent) {
        cleanWindow();
        windowView = new StoreSearchView();
        FloatWindowManager.getInstance().show(windowView, intent);
        if (DBG) Log.e(TAG, "DB_STORE# showSearchView");
    }

    /*==================================================================*/

    private void requestNet(final Context context, final int cid, final StoreADInfo adInfo) {
        RequestApi.getInstance().fetchNewStore(context, cid, 3, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) Log.e(TAG, "onFailure: can't fetch config");
                //通知销毁
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        WindowHandler.getInstance().destroyCallback();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (DBG) Log.d(TAG, "onResponse: " + requestCount + " times request");

                if (response == null || response.code() != 200) {
                    if (DBG) Log.d(TAG, "onResponse: #this response is null");
                    //通知销毁
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            WindowHandler.getInstance().destroyCallback();
                        }
                    });
                    return;
                }

                final AppListInfo listInfo = new Gson().fromJson(response.body().string(), AppListInfo.class);
                response.body().close();

                if (null == listInfo || null != listInfo.err || listInfo.list.size() <= 0) {
                    if (DBG) Log.d(TAG, "onResponse: # this appListInfo is error");
                    //通知销毁
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            WindowHandler.getInstance().destroyCallback();
                        }
                    });
                    return;
                }

                List<AppDetailInfo> list = new ArrayList<>();
                for (AppDetailInfo info : listInfo.list) {
                    if (info.screenshots != null && !info.screenshots.isEmpty() &&
                            info.screenshots != null && ApkUtils.DOWNLOAD == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versioncode))) {
                        list.add(info);
                        if (DBG) Log.d(TAG, "onResponse: add this info");
                    } else {
                        if (DBG) Log.e(TAG, "clean pkg " + info.appname);
                    }
                }

                if (mListInfo == null) { //第一次请求
                    listInfo.list = list;
                    mListInfo = listInfo;
                } else { //第二次或之后的请求
                    mListInfo.list.addAll(list);
                }
                if (DBG)
                    Log.d(TAG, "onResponse: mListInfo=" + mListInfo.list.size());
                switch (cid) {
                    case CID_NEW_BOTTOM:     //展示上拉
                        if (mListInfo.list.size() >= 1) {
                            Log.d(TAG, "onResponse: show roll");
                            HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    showRollWifiView(mListInfo, adInfo);
                                    mListInfo = null;
                                    requestCount = 0;
                                }
                            });
                        } else {
                            requestAgain(context, cid, adInfo);
                        }
                        break;
                    case CID_INSERT:
                        if (mListInfo.list.size() >= 3) {
                            if (DBG) Log.d(TAG, "onResponse: show insert");
                            HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    //添加前三个
                                    if (mListInfo.list.size() > 3) {
                                        List<AppDetailInfo> list = new ArrayList<>();
                                        for (int i = 0; i < 3; i++) {
                                            list.add(mListInfo.list.get(i));
                                        }
                                        mListInfo.list = list;
                                    }
                                    showRepeatPlayView(mListInfo, adInfo);
                                    mListInfo = null;
                                    requestCount = 0;
                                }
                            });
                        } else {
                            requestAgain(context, cid, adInfo);
                        }
                        break;
                }
            }
        });
    }

    private void requestAgain(Context context, int cid, StoreADInfo adInfo) {
        if (requestCount < 3) {
            requestNet(context, cid, adInfo);
            requestCount++;
        } else {
            if (DBG) Log.e(TAG, "onResponse: over 3 times");
            //通知销毁
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    WindowHandler.getInstance().destroyCallback();
                }
            });
        }
    }
}
