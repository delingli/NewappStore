package com.bbx.appstore.sqlite;

import android.content.Context;

import com.google.gson.Gson;

import java.util.ArrayList;

import com.bbx.appstore.bean.DmBean;

public class CartDao {

    private static Gson gson;

    public static synchronized void insert(Context context, String appId, String appName, String packageName,
                                           String versionCode, String fileSize, String iconUrl, String downUrl,
                                           ArrayList<String> rpt_dc, ArrayList<String> rpt_ic, ArrayList<String> rpt_ac,
                                           String rpt_dl, String method) {
        Dao dao = new Dao(context);
        if (null == gson) {
            gson = new Gson();
        }
        if (null == dao.query(appId)) {
            dao.insert(appId, appName, packageName, versionCode, fileSize, iconUrl, downUrl,
                    null == rpt_dc ? null : gson.toJson(rpt_dc),
                    null == rpt_ic ? null : gson.toJson(rpt_ic),
                    null == rpt_ac ? null : gson.toJson(rpt_ac),
                    rpt_dl, method);
        } else {
            dao.update(appId, appName, packageName, versionCode, fileSize, iconUrl, downUrl,
                    null == rpt_dc ? null : gson.toJson(rpt_dc),
                    null == rpt_ic ? null : gson.toJson(rpt_ic),
                    null == rpt_ac ? null : gson.toJson(rpt_ac),
                    rpt_dl, method);
        }
    }

    public static synchronized void update(Context context, String appId, String appName, String packageName,
                                           String versionCode, String fileSize, String iconUrl, String downUrl,
                                           ArrayList<String> rpt_dc, ArrayList<String> rpt_ic, ArrayList<String> rpt_ac,
                                           String rpt_dl, String method) {
        Dao dao = new Dao(context);
        if (null == gson) {
            gson = new Gson();
        }
        dao.update(appId, appName, packageName, versionCode, fileSize, iconUrl, downUrl,
                null == rpt_dc ? null : gson.toJson(rpt_dc),
                null == rpt_ic ? null : gson.toJson(rpt_ic),
                null == rpt_ac ? null : gson.toJson(rpt_ac),
                rpt_dl, method);
    }

    public static synchronized DmBean queryFromPkg(Context context, String pkgName) {
        Dao dao = new Dao(context);
        return dao.queryBeanFromPkg(pkgName);
    }

    public static synchronized void delete(Context context, String appId) {
        Dao dao = new Dao(context);
        dao.delete(appId);
    }

}
