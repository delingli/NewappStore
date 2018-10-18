package com.bbx.appstore.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 应用列表，包括列表和热门
 */
public class AppListInfo implements Serializable {

    public String err;
    public String href_next;    //下一页url
    public String rpt_st;   //页面停留时间上报url, 时长替换宏SZST_ST;
    public int flag_replace;    //1/0，是否需要替换宏（不包括页面停留时长替换）
    public List<AppDetailInfo> list; //默认列表
    public List<AppDetailInfo> must; //必须要展示的
    public List<AppDetailInfo> attach; //应用圈单包
    public String rtp_method;

    @Override
    public String toString() {
        return "AppListInfo{" +
                "err='" + err + '\'' +
                ", href_next='" + href_next + '\'' +
                ", rpt_st='" + rpt_st + '\'' +
                ", flag_replace=" + flag_replace +
                ", list=" + list +
                ", must=" + must +
                ", attach=" + attach +
                ", rtp_method='" + rtp_method + '\'' +
                '}';
    }
}
