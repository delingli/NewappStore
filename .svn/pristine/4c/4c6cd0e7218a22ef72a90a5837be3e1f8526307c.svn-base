package com.bbx.appstore.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import com.bbx.appstore.storeutils.Utils;
import com.bbx.appstore.windows.WindowHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.aigestudio.downloader.bizs.DLManager;
import cn.aigestudio.downloader.interfaces.IDListener;

import static android.content.Intent.ACTION_PACKAGE_ADDED;
import static android.content.Intent.ACTION_PACKAGE_REMOVED;
import static android.content.Intent.ACTION_PACKAGE_REPLACED;
import static com.bbx.appstore.download.DownloadReceiver.IA;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;

public class DownloadLoopAndInstall {

    private static final String TAG = "DownloadLoop";
    private static boolean DEBUG = true;
    private static AppListInfo DOWN_LIST_CACHE;
    private static String STORE_PKG;

    private static DownloadLoopAndInstall downloadLoopAndInstall;

    private static Queue<DmBean> downApkQueue = new LinkedList<>();
    private AtomicBoolean mRunTag = new AtomicBoolean();
    private final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final int MAX_COUNT = 1;

    private static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver installReceiver;
    private OnDownloadAndInstallListener onDownloadAndInstallListener;

    private Runnable washLoop = new Runnable() {
        @Override
        public void run() {
            while (!downApkQueue.isEmpty()) {
                mRunTag.set(true);
                if (COUNTER.get() > MAX_COUNT) {
                    SystemClock.sleep(500);
                    continue;
                }
                COUNTER.incrementAndGet();
                DmBean dmBean = downApkQueue.poll();
                Log.e(TAG, "downLoop apk : " + dmBean.appName);
                startDownload(mContext, dmBean);
            }
            mRunTag.set(false);
            Log.e(TAG, "downQueue isEmpty");
        }
    };

    private DownloadLoopAndInstall() {
    }

    public static DownloadLoopAndInstall getInstance() {
        if (downloadLoopAndInstall == null) {
            synchronized (DownloadLoopAndInstall.class) {
                if (downloadLoopAndInstall == null) {
                    downloadLoopAndInstall = new DownloadLoopAndInstall();
                }
            }
        }
        return downloadLoopAndInstall;
    }

    /**
     * 接收安装广播
     *
     * @param context
     */
    public void startReceiver(Context context) {
        if (null == installReceiver) {
            installReceiver(context);
        }
    }

    /**
     * 添加到队列
     *
     * @param context
     * @param dmBean
     */
    //在队列中则不再添加, 但是不判断是否在下载中
    public void addDownloadLoop(Context context, DmBean dmBean) {
        mContext = context;
        if (downApkQueue.contains(dmBean)) return;
        CartDao.insert(context, dmBean.appId, dmBean.appName, dmBean.packageName, dmBean.versionCode, dmBean.size, dmBean.iconUrl,
                dmBean.downUrl, dmBean.repDc, dmBean.repInstall, dmBean.repAc, null, dmBean.method);
        DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOADING);
        downApkQueue.add(dmBean);
        if (mRunTag.get()) {
            Log.e(TAG, "loop are running");
        } else {
            Log.e(TAG, "loop start");
            new Thread(washLoop).start();
        }
    }

    /**
     * 获取下载好的apk文件
     *
     * @param appName
     * @return
     */
    public static File getApkFile(String appName) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + appName + ".apk");
    }

    public void setDownListCache(String pkg, AppListInfo info) {
        STORE_PKG = pkg;
        DOWN_LIST_CACHE = info;
    }

    private void installReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PACKAGE_ADDED);
        intentFilter.addAction(ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        installReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (TextUtils.equals(intent.getAction(), ACTION_PACKAGE_ADDED)) {
                    Log.e(TAG, "ADD");
                    if (packageName.equals(SPUtils.getString(context, SConstant.SP_PKG_KEY))) {
                        rptStore(context, SConstant.SP_I_RPT, packageName);
                    } else {
                        checkSB(context, packageName, true);
                    }
                } else if (TextUtils.equals(intent.getAction(), ACTION_PACKAGE_REPLACED)) {
                    Log.e(TAG, "REPLACED");
                    if (packageName.equals(SPUtils.getString(context, SConstant.SP_PKG_KEY))) {
                        rptStore(context, SConstant.SP_I_RPT, packageName);
                    } else {
                        checkSB(context, packageName, false);
                    }
                } else if (TextUtils.equals(intent.getAction(), ACTION_PACKAGE_REMOVED)) {
                    Log.e(TAG, "REMOVED");
                    DmBean dmBean = CartDao.queryFromPkg(context, packageName);
                    if (null != dmBean) {
                        DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
                    }
                }
            }
        };
        context.registerReceiver(installReceiver, intentFilter);
    }

    /**
     * 单包推广广告上报, 参考 StoreDownStoreView
     *
     * @param context
     * @param rptKey
     * @param pkgName
     */
    private void rptStore(Context context, String rptKey, String pkgName) {
        String i = SPUtils.getString(context, rptKey);
        if (DEBUG) Log.e(TAG, "rptStore i = " + i);
        if (null != i) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            ArrayList<String> i_rpt = new Gson().fromJson(i, type);
            Report.getInstance().reportListUrl(context, "GET", i_rpt, 0, null);
            ApkUtils.startApp(context, pkgName);
            String a = SPUtils.getString(context, SConstant.SP_A_RPT);
            if (DEBUG) Log.e(TAG, "rptStore a = " + a);
            if (null != a) {
                ArrayList<String> a_rpt = new Gson().fromJson(a, type);
                Report.getInstance().reportListUrl(context, "GET", a_rpt, 0, null);
            }
        }
    }

    /**
     * @param context
     * @param packageName
     * @param added
     */
    private void checkSB(Context context, String packageName, boolean added) {
        DmBean dmBean = CartDao.queryFromPkg(context, packageName);
        if (null != dmBean) {
            DownloadCart.getInstance().setApkStatus(dmBean.appId, ApkUtils.OPEN);
            if (added) {
                Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repInstall, 0, null);
                if (null != STORE_PKG && packageName.equals(STORE_PKG) && null != DOWN_LIST_CACHE) {
                    startAppForDownList(context.getApplicationContext());
                } else {
                    ApkUtils.startApp(context, packageName);
                }
                Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repAc, 0, null);
            }
            if (null != onDownloadAndInstallListener)
                onDownloadAndInstallListener.installSuccessful(packageName);
            ApkUtils.deleteFile(getApkFile(dmBean.appName));
        }
    }

    private void startAppForDownList(Context context) {
        Intent intent = new Intent("com.hai.appstore.intent.action.VIEW");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            //打开应用圈下载
            String json = new Gson().toJson(DOWN_LIST_CACHE);
            Log.e(TAG, "toJson " + json);
            intent.putExtra("DOWN_LIST", json);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        STORE_PKG = null;
        DOWN_LIST_CACHE = null;
    }


    private void startDownload(final Context context, final DmBean dmBean) {
        Log.e(TAG, "down " + dmBean);
        DLManager.getInstance(context).dlStart(dmBean.downUrl, DOWNLOAD_PATH, dmBean.appName + ".apk", new IDListener() {
            @Override
            public void onPrepare() {

            }

            @Override
            public void onStart(String s, String s1, int i) {
                Log.e(TAG, "onStart apk " + dmBean.appName + ", size " + Utils.readableFileSize(dmBean.size));
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "开始下载 " + dmBean.appName, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(final int i) {
                Log.e(TAG, "Thread name : " + Thread.currentThread().getName() + ", progress " + i);
                if (null != onDownloadAndInstallListener) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int size = Integer.valueOf(dmBean.size);
                            int percent = (int) (i * 1.0 / size * 100);
                            onDownloadAndInstallListener.downloadProgress(dmBean.packageName, percent);
                        }
                    });
                }
            }

            @Override
            public void onStop(int i) {
                COUNTER.decrementAndGet();
            }

            @Override
            public void onFinish(File file) {
//                if (!(ApkUtils.hasPermission(context) && IA == 3)) {
//                    WindowHandler.getInstance().cleanWindow();
//                }
                DownloadCart.getInstance().setApkStatus(dmBean.appId, INSTALL);
                reportAndInstall(context, dmBean);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != onDownloadAndInstallListener)
                            onDownloadAndInstallListener.downloadComplete(dmBean.packageName);
                    }
                });
                COUNTER.decrementAndGet();
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "onError i : " + i + ", s : " + s);
                CartDao.delete(context, dmBean.appId);
                DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, dmBean.appName + " 下载失败", Toast.LENGTH_SHORT).show();
                        if (null != onDownloadAndInstallListener)
                            onDownloadAndInstallListener.downloadFailed(dmBean.packageName);
                        COUNTER.decrementAndGet();
                    }
                });
            }
        });
    }

    private void reportAndInstall(final Context context, DmBean dmBean) {
        if (DOWNLOAD == ApkUtils.checkNeedDownload(context, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
            Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repDc, 0, null);
        } else {
            if (null != dmBean.updateRpt && "update".equals(dmBean.updateRpt)) {
                Report.getInstance().reportListUrl(context, dmBean.method, dmBean.repDc, 0, null);
            }
        }
        try {
            ApkUtils.blueInstall(context, getApkFile(dmBean.appName), IA);
            if (!ApkUtils.hasPermission(context) || IA != 3) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        WindowHandler.getInstance().cleanWindow();
                        WindowHandler.getInstance().destroyCallback();
                    }
                });
            }
        } catch (Exception e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), "解析包名错误", Toast.LENGTH_SHORT).show();
                }
            });
            ApkUtils.deleteFile(getApkFile(dmBean.appName));
            CartDao.delete(context, dmBean.appId);
            DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
        }
    }

    public void setDNIListener(OnDownloadAndInstallListener listener) {
        onDownloadAndInstallListener = listener;
    }

    public void unDNIListener() {
        onDownloadAndInstallListener = null;
    }

    public interface OnDownloadAndInstallListener {
        void downloadComplete(String pkgName);

        void downloadFailed(String pkgName);

        void installSuccessful(String pkgName);

        void downloadProgress(String pkgName, int progress);
    }
}
