package com.bbx.appstore.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class AppInfo implements Serializable{

    public String appid;
    public String icon;
    public String appname;
    public String apk;
    public String downcount;
    public String size;
    public String versioncode;
    public String versionname;
    public String href_download;
    public String href_detail;
    public String description; //简介
    public ArrayList<String> rpt_ss;    //展示（单条上报）
    public ArrayList<String> rpt_ct;    //点击详情
    public ArrayList<String> rpt_cd;    //点击下载
    public ArrayList<String> rpt_dc;    //下载完成
    public ArrayList<String> rpt_ic;    //安装成功
    public ArrayList<String> rpt_ac;    //激活
    public String rpt_dl;    //删除

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppInfo appInfo = (AppInfo) o;

        if (!appid.equals(appInfo.appid)) return false;
        return apk.equals(appInfo.apk);

    }

    @Override
    public int hashCode() {
        int result = appid.hashCode();
        result = 31 * result + apk.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appid='" + appid + '\'' +
                ", icon='" + icon + '\'' +
                ", appname='" + appname + '\'' +
                ", apk='" + apk + '\'' +
                ", downcount='" + downcount + '\'' +
                ", size='" + size + '\'' +
                ", versioncode='" + versioncode + '\'' +
                ", versionname='" + versionname + '\'' +
                ", href_download='" + href_download + '\'' +
                ", href_detail='" + href_detail + '\'' +
                ", description='" + description + '\'' +
                ", rpt_ss=" + rpt_ss +
                ", rpt_ct=" + rpt_ct +
                ", rpt_cd=" + rpt_cd +
                ", rpt_dc=" + rpt_dc +
                ", rpt_ic=" + rpt_ic +
                ", rpt_ac=" + rpt_ac +
                ", rpt_dl='" + rpt_dl + '\'' +
                '}';
    }
}
