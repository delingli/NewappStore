package com.bbx.appstore.download;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.DownloadCart;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.sqlite.CartDao;
import com.bbx.appstore.storeutils.SPUtils;
import com.google.gson.Gson;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;


/**
 * 系统下载管理器
 */
public class DManager {

    private static DManager windowHandler;
    public static final Map<Long, DmBean> queue = new HashMap<>();
    static DownloadManager mDm;
    static long STORE_ID; //保存自有应用市场的下载id

    public synchronized static DManager getInstance(Context context) {
        if (windowHandler == null) {
            windowHandler = new DManager(context);
        }
        return windowHandler;
    }

    private DManager(Context context) {
        DownloadReceiver.startReceiver(context);
    }

    public File getApkFile(String appName) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + appName + ".apk");
    }

    /**
     * 普通广告下载
     *
     * @param context
     * @param dmBean
     */
    public void startDownload(Context context, DmBean dmBean) {
        deleteExistsApk(dmBean.appName);
        CartDao.insert(context, dmBean.appId, dmBean.appName, dmBean.packageName, dmBean.versionCode, dmBean.size, dmBean.iconUrl,
                dmBean.downUrl, dmBean.repDc, dmBean.repInstall, dmBean.repAc, null, dmBean.method);
        DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOADING);
        long ID = download(context, dmBean.downUrl, dmBean.appId, dmBean.appName);
        Log.e("DL_ID", "" + ID);
        if (ID != 0) {
            Toast.makeText(context, "开始下载" + dmBean.appName, Toast.LENGTH_SHORT).show();
            queue.put(ID, dmBean);
        } else {
            CartDao.delete(context, dmBean.appId);
            DownloadCart.getInstance().setApkStatus(dmBean.appId, DOWNLOAD);
            Toast.makeText(context, "下载失败 ID = 0", Toast.LENGTH_SHORT).show();
        }
    }

    private long download(Context context, String url, String apkId, String apkName) {
        if (null == url || null == apkId || null == apkName) return 0;
        if (null == mDm) {
            mDm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(true);
        request.setMimeType("application/vnd.android.package-archive");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName + ".apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        return mDm.enqueue(request);
    }

    /**
     * 单包推广广告下载 参考 StoreDownStoreView
     *
     * @param context
     * @param info
     */
    public void startDownload(Context context, StoreADInfo info) {
        deleteExistsApk(info.name.trim());
        if (null != info.down_url) {
            Gson gson = new Gson();
            SPUtils.putString(context, SConstant.SP_PKG_KEY, info.apk.trim());
            SPUtils.putString(context, SConstant.SP_APP_NAME, info.name.trim());
            SPUtils.putString(context, SConstant.SP_DC_RPT, gson.toJson(info.dc_rpt));
            SPUtils.putString(context, SConstant.SP_A_RPT, gson.toJson(info.a_rpt));
            SPUtils.putString(context, SConstant.SP_I_RPT, gson.toJson(info.i_rpt));
            long ID = download(context, info.down_url, info.name.trim());
            Log.e("DL_ID", "" + ID);
            if (ID != 0) {
                STORE_ID = ID;
                Report.getInstance().reportListUrl(context, "GET", info.d_rpt, 0, null);
            }
        }
    }

    private long download(Context context, String url, String apkName) {
        if (null == mDm) {
            mDm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(true);
        request.setMimeType("application/vnd.android.package-archive");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName + ".apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        return mDm.enqueue(request);
    }

    private void deleteExistsApk(String appName) {
        File file = getApkFile(appName);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }
}
