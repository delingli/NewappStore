package com.bbx.appstore.bean;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadCart {

    private static final DownloadCart INSTANCE = new DownloadCart();
    private static final Object LOCK = new Object();

    /**
     * apkCarStatus 保存状态：下载中(downloading)，下载完成（install），未下载(download), 安装完成(open)
     */
    private Map<String, Integer> apkCarStatus = new LinkedHashMap<>();

    /**
     * apkCarDownloadStatus 记录下载进度
     */
//    private Map<String, DownloadStatus> apkCarDownloadStatus = new LinkedHashMap<>();

    public static DownloadCart getInstance() {
        synchronized (LOCK) {
            return INSTANCE;
        }
    }

    private DownloadCart() {

    }

    public void setApkStatus(String appId, int status) {
        apkCarStatus.put(appId, status);
    }

    public Integer getApkStatus(String appId) {
        return null == apkCarStatus.get(appId) ? 0 : apkCarStatus.get(appId);
    }

    public Map<String, Integer> getApkStatus() {
        return apkCarStatus;
    }

    public boolean inquire(String appId) {
        return null != apkCarStatus.get(appId);
    }

    public void clear() {
        apkCarStatus.clear();
    }

    public void remove(String appId) {
        apkCarStatus.remove(appId);
    }

}
