package com.bbx.appstore.bean;

import java.util.ArrayList;

/**
 * 下载管理
 */
public class DmBean {

    public String appId;
    public String appName;
    public String packageName;
    public String versionCode;
    public String size;
    public String iconUrl;
    public String downUrl;
    public ArrayList<String> repDc;
    public ArrayList<String> repInstall;
    public ArrayList<String> repAc;
    public String repDel;
    public String method;
    public String updateRpt;

    @Override
    public String toString() {
        return "DmBean{" +
                "appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", size='" + size + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", downUrl='" + downUrl + '\'' +
                ", repDc='" + repDc + '\'' +
                ", repInstall=" + repInstall + '\'' +
                ", repAc=" + repAc + '\'' +
                ", repDel=" + repDel + '\'' +
                ", method=" + method +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DmBean && this.packageName.equals(((DmBean) o).packageName);
    }

    public static DmBean buildDmBean(AppDetailInfo info) {
        DmBean dmBean = new DmBean();
        dmBean.appId = info.appid;
        dmBean.appName = info.appname;
        dmBean.downUrl = info.href_download;
        dmBean.packageName = info.apk;
        dmBean.versionCode = info.versioncode;
        dmBean.size = info.size;
        dmBean.iconUrl = info.icon;
        dmBean.repDc = info.rpt_dc;
        dmBean.repInstall = info.rpt_ic;
        dmBean.repAc = info.rpt_ac;
        dmBean.method = info.rtp_method;
        return dmBean;
    }
}
