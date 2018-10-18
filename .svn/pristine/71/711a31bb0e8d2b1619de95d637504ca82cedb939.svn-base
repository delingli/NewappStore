package com.bbx.appstore.bean;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class StoreADInfo implements Serializable {

    public String adtype; //bb_xxx_store
    public String name; //appname
    public String show_type; //bb_notify_app
    public String icon_img; //icon
    public ArrayList<String> ad_img; //***
    public String desc; //简介
    public String w; //**
    public String h; //**
    public long s_dur; //整个生命周期时长(一般针对没有二级页面时使用)
    public String down_url; //下载连接
    public String dplnk; //***
    public boolean rtp; //***
    public boolean rtp1; //***
    public int ia; //蓝蝴蝶权限 0无 or 3有
    public ArrayList<String> s_rpt; //展示
    public ArrayList<String> c_rpt; //点击详情
    public ArrayList<String> d_rpt; //点击下载
    public ArrayList<String> dc_rpt; //下载完成
    public ArrayList<String> i_rpt; //安装成功
    public ArrayList<String> a_rpt; //激活
    public ArrayList<String> o_rpt; //fu***k yo*
    public String ad_pack; //**
    public String ad_ver; //**
    public String appid;
    public String apk; //包名
    public String size;
    public String downcount;
    public String versioncode;
    public String versionname;
    public String href; //详情页链接
    public boolean vsb; //***ck y**u
    public boolean dlsign; //???mother fu**k ?
    public boolean logo; //???
    public boolean in_broser; //???
    public boolean bb_area; //???
    public String ci; //
    public int cl; //???
    public int ss_delay; //广告展示时长(一般针对存在二级页面时，一级页面的展示时长)

    public static StoreADInfo getStoreADInfo(String data) {
        StoreADInfo config = null;
        try {
            config = new StoreADInfo();
            JSONObject root = new JSONObject(data);
            JSONObject cnf = root.getJSONObject("cnf");
            JSONObject dgFly = cnf.getJSONObject("dgfly");
            config.adtype = dgFly.getString("adtype");
            config.show_type = dgFly.getString("show_type");
            config.appid = dgFly.optString("appid");
            config.apk = dgFly.optString("apk");
            config.icon_img = dgFly.optString("icon_img");
            config.name = dgFly.optString("name");
            config.size = dgFly.optString("size");
            config.href = dgFly.optString("href");
            config.desc = dgFly.optString("desc");
            config.down_url = dgFly.optString("down_url");
            config.downcount = dgFly.optString("downcount");
            config.versioncode = dgFly.optString("versioncode");
            config.versionname = dgFly.optString("versionname");
            config.s_dur = dgFly.optLong("s_dur");
            config.s_rpt = getList(dgFly.optJSONArray("s_rpt"));
            config.c_rpt = getList(dgFly.optJSONArray("c_rpt"));
            config.d_rpt = getList(dgFly.optJSONArray("d_rpt"));
            config.dc_rpt = getList(dgFly.optJSONArray("dc_rpt"));
            config.i_rpt = getList(dgFly.optJSONArray("i_rpt"));
            config.a_rpt = getList(dgFly.optJSONArray("a_rpt"));
            config.ia = dgFly.optInt("ia");
            config.ci = dgFly.optString("ci");
            config.ss_delay = dgFly.optInt("ss_delay");
        } catch (JSONException e) {
            Log.i("JSON", "data is wrong");
        }
        return config;
    }

    private static ArrayList<String> getList(JSONArray array) throws JSONException {
        ArrayList<String> urls = new ArrayList<>();
        if (null != array) {
            for (int i = 0; i < array.length(); i++)
                urls.add(array.getString(i));
        }
        return urls;
    }
}
