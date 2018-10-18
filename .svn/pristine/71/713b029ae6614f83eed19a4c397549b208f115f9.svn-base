package com.bbx.appstore.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.DownloadCart;
import com.bbx.appstore.sqlite.CartDao;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;
import static android.content.Intent.ACTION_PACKAGE_ADDED;
import static android.content.Intent.ACTION_PACKAGE_REMOVED;
import static android.content.Intent.ACTION_PACKAGE_REPLACED;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;


/**
 * 系统下载管理广播和安装广播(已被注释)
 */
public class DownloadReceiver {

    private static boolean DEBUG = false;
    private static final String TAG = "DownloadReceiver";
    public static int IA;
    private static BroadcastReceiver downloadReceiver;
//    private static BroadcastReceiver installReceiver;
//    private static OnDownloadAndInstallListener onDownloadAndInstallListener;
//    private static AppListInfo DOWN_LIST_CACHE;
//    private static String STORE_PKG;

    private DownloadReceiver() {

    }

//    public static void setDownListCache(String pkg, AppListInfo info) {
//        STORE_PKG = pkg;
//        DOWN_LIST_CACHE = info;
//    }

    public static void startReceiver(Context context) {
        if (null == downloadReceiver) {
            downloadReceiver(context);
        }
//        if (null == installReceiver) {
//            installReceiver(context); //需要分开注册
//        }
    }

//    private static void installReceiver(Context context) {
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ACTION_PACKAGE_ADDED);
//        intentFilter.addAction(ACTION_PACKAGE_REPLACED);
//        intentFilter.addAction(ACTION_PACKAGE_REMOVED);
//        intentFilter.addDataScheme("package");
//        installReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String packageName = intent.getData().getSchemeSpecificPart();
//                if (TextUtils.equals(intent.getAction(), ACTION_PACKAGE_ADDED)) {
//                    Log.e(TAG, "ADD");
//                    if (packageName.equals(SPUtils.getString(context, SConstant.SP_PKG_KEY))) {
//                        rptStore(context, SConstant.SP_I_RPT, packageName);
//                    } else {
//                        checkSB(context, packageName, true);
//                    }
//                } else if (TextUtils.equals(intent.getAction(), ACTION_PACKAGE_REPLACED)) {
//                    Log.e(TAG, "REPLACED");
//                    if (packageName.equals(SPUtils.getString(context, SConstant.SP_PKG_KEY))) {
//                        rptStore(context, SConstant.SP_I_RPT, packageName);
//                    } else {
//                        checkSB(context, packageName, false);
//                    }
//                } else if (TextUtils.equals(intent.getAction(), ACTION_PACKAGE_REMOVED)) {
//                    Log.e(TAG, "REMOVED");
//                    DmBean dmBean = CartDao.queryFromPkg(context, packageName);
//                    if (null != dmBean) {
//                        DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
//                    }
//                }
//            }
//        };
//        context.registerReceiver(installReceiver, intentFilter);
//    }

//    private static void rptStore(Context context, String rptKey, String pkgName) {
//        String i = SPUtils.getString(context, rptKey);
//        if (DEBUG) Log.e(TAG, "rptStore i = " + i);
//        if (null != i) {
//            Type type = new TypeToken<ArrayList<String>>() {
//            }.getType();
//            ArrayList<String> i_rpt = new Gson().fromJson(i, type);
//            Report.getInstance().reportListUrl(context, "GET", i_rpt, 0, null);
//            ApkUtils.startApp(context, pkgName);
//            String a = SPUtils.getString(context, SConstant.SP_A_RPT);
//            if (DEBUG) Log.e(TAG, "rptStore a = " + a);
//            if (null != a) {
//                ArrayList<String> a_rpt = new Gson().fromJson(a, type);
//                Report.getInstance().reportListUrl(context, "GET", a_rpt, 0, null);
//            }
//        }
//    }
//
//    private static void checkSB(Context context, String packageName, boolean added) {
//        DmBean dmBean = CartDao.queryFromPkg(context, packageName);
//        if (null != dmBean) {
//            DownloadCart.getInstance().setApkStatus(dmBean.appId, ApkUtils.OPEN);
//            if (added) {
//                Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repInstall, 0, null);
//                if (null != STORE_PKG && packageName.equals(STORE_PKG) && null != DOWN_LIST_CACHE) {
//                    startAppForDownList(context.getApplicationContext());
//                } else {
//                    ApkUtils.startApp(context, packageName);
//                }
//                Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repAc, 0, null);
//            }
//            if (null != onDownloadAndInstallListener)
//                onDownloadAndInstallListener.installSuccessful(packageName);
//            ApkUtils.deleteFile(DManager.getApkFile(dmBean.appName));
//        }
//    }

//    private static void startAppForDownList(Context context) {
//        Intent intent = new Intent("com.hai.appstore.intent.action.VIEW");
//        if (intent.resolveActivity(context.getPackageManager()) != null) {
//            //打开应用圈下载
//            String json = new Gson().toJson(DOWN_LIST_CACHE);
//            Log.e(TAG, "toJson " + json);
//            intent.putExtra("DOWN_LIST", json);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        }
//        STORE_PKG = null;
//        DOWN_LIST_CACHE = null;
//    }

    private static void downloadReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter(ACTION_DOWNLOAD_COMPLETE);
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (DManager.queue.keySet().contains(ID) || DManager.STORE_ID == ID) {
                    checkStatus(context, ID);
                }
            }
        };
        context.registerReceiver(downloadReceiver, intentFilter);
    }

    //检查下载状态
    private static void checkStatus(Context context, long downloadId) {
        if (DEBUG) Log.e(TAG, "checkStatus id = " + downloadId);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor c = DManager.mDm.query(query);
        if (c.moveToFirst()) {
            if (DEBUG) Log.e(TAG, "rptStore downloadId = " + downloadId);
            if (DManager.STORE_ID == downloadId) { //-----------------------------------------------单包推广下载状态
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    //下载完成
                    case DownloadManager.STATUS_SUCCESSFUL:
                        DManager.queue.remove(downloadId);
                        DManager.STORE_ID = 0;
                        String dc = SPUtils.getString(context, SConstant.SP_DC_RPT);
                        if (DEBUG) Log.e(TAG, "rptStore dc = " + dc);
                        if (null != dc) {
                            Type type = new TypeToken<ArrayList<String>>() {
                            }.getType();
                            ArrayList<String> rpt = new Gson().fromJson(dc, type);
                            Report.getInstance().reportListUrl(context, "GET", rpt, 0, null);
                        }
                        try {
                            ApkUtils.blueInstall(context, DownloadLoopAndInstall.getInstance().getApkFile(SPUtils.getString(context, SConstant.SP_APP_NAME)), IA);
                        } catch (Exception e) {
                            Toast.makeText(context, "解析包名错误", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    //下载失败
                    case DownloadManager.STATUS_FAILED:
                        DManager.queue.remove(downloadId);
                        DManager.STORE_ID = 0;
                        break;
                }
//            } else { -----------------------------------------------------------------------------普通下载状态
//                DmBean dmBean = DManager.queue.get(downloadId);
//                if (null == dmBean) {
//                    c.close();
//                    return;
//                }
//                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//                switch (status) {
//                    //下载暂停
//                    case DownloadManager.STATUS_PAUSED:
//                        break;
//                    //下载延迟
//                    case DownloadManager.STATUS_PENDING:
//                        break;
//                    //正在下载
//                    case DownloadManager.STATUS_RUNNING:
//                        break;
//                    //下载完成
//                    case DownloadManager.STATUS_SUCCESSFUL:
////                    if (!(ApkUtils.hasPermission(context) && IA == 3)) {
////                        WindowHandler.getInstance().cleanWindow();
////                    }
//                        DManager.queue.remove(downloadId);
//                        DownloadCart.getInstance().setApkStatus(dmBean.appId, INSTALL);
//                        reportAndInstall(context, dmBean);
//                        if (null != onDownloadAndInstallListener)
//                            onDownloadAndInstallListener.downloadComplete(dmBean.packageName);
//                        break;
//                    //下载失败
//                    case DownloadManager.STATUS_FAILED:
//                        DManager.queue.remove(downloadId);
//                        CartDao.delete(context, dmBean.appId);
//                        DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
//                        if (null != onDownloadAndInstallListener)
//                            onDownloadAndInstallListener.downloadFailed(dmBean.packageName);
//                        break;
//                }
            }
        }
        c.close();
    }

//    private static void reportAndInstall(Context context, DmBean dmBean) {
//        if (DOWNLOAD == ApkUtils.checkNeedDownload(context, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
//            Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repDc, 0, null);
//        } else {
//            if (null != dmBean.updateRpt && "update".equals(dmBean.updateRpt)) {
//                Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repDc, 0, null);
//            }
//        }
//        try {
//            ApkUtils.blueInstall(context, DManager.getApkFile(dmBean.appName), IA);
//        } catch (Exception e) {
//            Toast.makeText(context, "解析包名错误", Toast.LENGTH_SHORT).show();
//            ApkUtils.deleteFile(DManager.getApkFile(dmBean.appName));
//            CartDao.delete(context, dmBean.appId);
//            DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
//        }
//    }

    public static void unRegisterReceiver(Context context) {
        context.unregisterReceiver(downloadReceiver);
//        context.unregisterReceiver(installReceiver);
    }

//    public static void setDNIListener(OnDownloadAndInstallListener listener) {
//        onDownloadAndInstallListener = listener;
//    }
//
//    public static void unDNIListener() {
//        onDownloadAndInstallListener = null;
//    }
//
//    public interface OnDownloadAndInstallListener {
//        void downloadComplete(String pkgName);
//
//        void downloadFailed(String pkgName);
//
//        void installSuccessful(String pkgName);
//    }
}
