package com.bbx.appstore.windows;

import android.content.Context;

import com.bbx.appstore.base.Config;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.download.DownloadReceiver;
import com.bbx.appstore.download.UserPresentReceiverManager;
import com.bbx.appstore.storeutils.ApkUtils;

import static com.bbx.appstore.base.SConstant.CID_INSERT;
import static com.bbx.appstore.base.SConstant.CID_NEW_BOTTOM;

/**
 * 公开的接口
 */
public class AppStoreHandler {

    private static AppStoreHandler appStoreHandler;

    public synchronized static AppStoreHandler getInstance() {
        if (appStoreHandler == null) {
            appStoreHandler = new AppStoreHandler();
        }
        return appStoreHandler;
    }

    public void init(Context context, String path, String pkgName) {
        WindowHandler.getInstance().initStore(context, path, pkgName);
//        DownloadReceiver.startReceiver(context);
        DownloadLoopAndInstall.getInstance().startReceiver(context);
        UserPresentReceiverManager.receiverPresentStatus(context);
    }

    public void show(Context context, String data, OnStoreDestroyListener listener) {
        StoreADInfo info = StoreADInfo.getStoreADInfo(data);
        if (!SConstant.AD_TYPE.equals(info.adtype)) return;
        Config.market = info.market;
        WindowHandler.getInstance().setOnDestroyListener(listener);
        switch (info.show_type) {
            case SConstant.SHOW_TYPE_NOTIFY:
                try {
                    if (ApkUtils.DOWNLOAD == ApkUtils.checkNeedDownload(context, info.apk, Integer.valueOf(info.versioncode))) {
                        WindowHandler.getInstance().showStoreNotify(info);
                    } else {
                        if (null != listener) listener.onDestroy();
                        return;
                    }
                } catch (NumberFormatException e) {
                    if (null != listener) listener.onDestroy();
                    return;
                }
                break;
            case SConstant.SHOW_TYPE_WIFI:
                WindowHandler.getInstance().showStoreWifiTips(info);
                break;
            case SConstant.SHOW_TYPE_BANNER:
                WindowHandler.getInstance().showStoreBanner(info);
                break;
            case SConstant.SHOW_TYPE_RECOMMEND:
                WindowHandler.getInstance().showStoreRecommend(info);
                break;
            case SConstant.SHOW_TYPE_DOWN_STORE:
                try {
                    if (ApkUtils.OPEN != ApkUtils.checkNeedDownload(context, info.apk, Integer.valueOf(info.versioncode))) {
                        WindowHandler.getInstance().showDownStoreView(info);
                    } else {
                        if (null != listener) listener.onDestroy();
                        return;
                    }
                } catch (NumberFormatException e) {
                    if (null != listener) listener.onDestroy();
                    return;
                }
                break;
            case SConstant.SHOW_TYPE_BOTTOM:
                WindowHandler.getInstance().showStoreNearbySecond(info);
                break;
            case SConstant.SHOW_TYPE_POWER_IN:
            case SConstant.SHOW_TYPE_POWER_OUT:
                WindowHandler.getInstance().showStorePowerView(info);
                break;
            case SConstant.SHOW_TYPE_NEW_RECOMMEND:
                WindowHandler.getInstance().showNewRecommendView(info);
                break;
            case SConstant.SHOW_TYPE_NEW_POWER_OUT:
            case SConstant.SHOW_TYPE_NEW_POWER_IN:
                WindowHandler.getInstance().showStoreNewPowerView(info);
                break;
            case SConstant.SHOW_TYPE_INSERT:
                WindowHandler.getInstance().showView(info, CID_INSERT);
                break;
            case SConstant.SHOW_TYPE_NEW_BOTTOM:
                WindowHandler.getInstance().showView(info, CID_NEW_BOTTOM);
                break;
            case SConstant.SHOW_TYPE_BATTERT_TIPS:
                WindowHandler.getInstance().showBatteryTipsView(info);
                break;
            default:
                if (null != listener) listener.onDestroy();
                return;
        }
        DownloadReceiver.IA = info.ia;
    }

    public void destroy() {
        WindowHandler.getInstance().cleanWindow();
    }

    public interface OnStoreDestroyListener {
        void onDestroy();
    }
}
