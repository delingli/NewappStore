package com.bbx.appstore.bean;

import java.util.List;

/**
 * 应用详情
 */
public class AppDetailInfo extends AppInfo {
    public String err;
    public int flag_replace;    //1/0，是否需要替换宏（不包括页面停留时长替换）
    public String rating;
    public String ratingperson;
    public List<String> screenshots;
    public String updateinfo;
    public String updatetime;
    public String os;
    public String developer;
    public String rpt_st;    //页面停留时间上报url，时长替换宏SZST_ST,
    public String rtp_method; //上报方式

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppDetailInfo detailInfo = (AppDetailInfo) o;

        if (!appid.equals(detailInfo.appid)) return false;
        return apk.equals(detailInfo.apk);

    }

    @Override
    public int hashCode() {
        int result = appid.hashCode();
        result = 31 * result + apk.hashCode();
        return result;
    }
}
