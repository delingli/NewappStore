package com.bbx.appstore.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 此类仅针对更新广告(没什么卵用)
 */
public class UpdateAppInfo implements Serializable {

    public int flag_replace; //是否需要替换宏
    public String err;
    public String rtp_method; //POST/GET
    public List<AppInfo> list;

    public static class AppInfo implements Serializable {
        public String appid;
        public String icon;
        public String appname;
        public String apk;
        public String size;
        public String rating;
        public String ratingperson;
        public ArrayList<String> screenshots;
        public String versioncode;
        public String versionname;
        public String downcount;
        public String description;
        public String updateinfo;
        public String updatetime;
        public String os;
        public String developer;
        public String href_download;
        public ArrayList<String> rpt_ss; //展示（单条）
        public ArrayList<String> rpt_cd; //点击下载
        public ArrayList<String> rpt_dc; //下载完成
        public ArrayList<String> rpt_ic; //成功安装
        public ArrayList<String> rpt_ac; //成功激活

        @Override
        public String toString() {
            return "AppInfo{" +
                    "appid='" + appid + '\'' +
                    ", icon='" + icon + '\'' +
                    ", appname='" + appname + '\'' +
                    ", apk='" + apk + '\'' +
                    ", size='" + size + '\'' +
                    ", rating='" + rating + '\'' +
                    ", ratingperson='" + ratingperson + '\'' +
                    ", screenshots=" + screenshots +
                    ", versioncode='" + versioncode + '\'' +
                    ", versionname='" + versionname + '\'' +
                    ", downcount='" + downcount + '\'' +
                    ", description='" + description + '\'' +
                    ", updateinfo='" + updateinfo + '\'' +
                    ", updatetime='" + updatetime + '\'' +
                    ", os='" + os + '\'' +
                    ", developer='" + developer + '\'' +
                    ", href_download='" + href_download + '\'' +
                    ", rpt_ss=" + rpt_ss +
                    ", rpt_cd=" + rpt_cd +
                    ", rpt_dc=" + rpt_dc +
                    ", rpt_ic=" + rpt_ic +
                    ", rpt_ac=" + rpt_ac +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UpdateAppInfo{" +
                "flag_replace=" + flag_replace +
                ", err='" + err + '\'' +
                ", rtp_method='" + rtp_method + '\'' +
                ", list=" + list +
                '}';
    }
}
