package com.bbx.appstore.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.bbx.appstore.bean.DmBean;

import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_DID;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_DOWN_ED;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_DOWN_URL;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_ICON_URL;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_NAME;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_PACKAGE;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_REP_AC;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_REP_DEL;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_REP_INSTALL;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_REP_MET;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_SIZE;
import static com.bbx.appstore.base.SConstant.TABLE_DM.DM_VERSIONCODE;
import static com.bbx.appstore.base.SConstant.TABLE_DM.NAME;

public class Dao {

    private MySQLiteHelper mySQLiteHelper;
    private Gson gson;

    public Dao(Context context) {
        if (null == mySQLiteHelper) {
            mySQLiteHelper = MySQLiteHelper.getInstance(context);
        }
    }

    /*插入*/
    public void insert(String appId, String appName, String pkgName, String versioncode, String size,
                       String iconUrl, String downUrl, String downed, String repInstall, String repAc,
                       String repDel, String method) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DM_DID, appId);
        cv.put(DM_NAME, appName);
        cv.put(DM_PACKAGE, pkgName);
        cv.put(DM_VERSIONCODE, versioncode);
        cv.put(DM_SIZE, size);
        cv.put(DM_ICON_URL, iconUrl);
        cv.put(DM_DOWN_URL, downUrl);
        if (null != downed) {
            cv.put(DM_DOWN_ED, downed);
        }
        if (null != repInstall) {
            cv.put(DM_REP_INSTALL, repInstall);
        }
        if (null != repAc) {
            cv.put(DM_REP_AC, repAc);
        }
        if (null != repDel) {
            cv.put(DM_REP_DEL, repDel);
        }
        cv.put(DM_REP_MET, method);
        database.insert(NAME, null, cv);
        database.close();
    }

    /*查找包名*/
    public String query(String did) {
        String pkg;
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String[] columns = new String[]{DM_PACKAGE};// 要返回哪几个列的数据.如果传入null就等价于select  *,
        String selection = DM_DID + " = ?"; // 查询条件
        String[] selectionArgs = new String[]{did};// 条件的值
        Cursor cursor = database.query(NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            pkg = cursor.getString(0);
        } else {
            pkg = null;
        }
        if (null != cursor) cursor.close();
        database.close();
        return pkg;
    }

    public DmBean queryBeanFromPkg(String pkgName) {
        DmBean bean = null;
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String[] columns = new String[]{DM_DID, DM_NAME, DM_PACKAGE, DM_VERSIONCODE, DM_SIZE,
                DM_ICON_URL, DM_DOWN_URL, DM_DOWN_ED, DM_REP_INSTALL, DM_REP_AC, DM_REP_DEL, DM_REP_MET};// 要返回哪几个列的数据.如果传入null就等价于select  *,
        String selection = DM_PACKAGE + " = ?"; // 查询条件
        String[] selectionArgs = new String[]{pkgName};// 条件的值
        Cursor cursor = database.query(NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            bean = new DmBean();
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            bean.appId = cursor.getString(0);
            bean.appName = cursor.getString(1);
            bean.packageName = cursor.getString(2);
            bean.versionCode = cursor.getString(3);
            bean.size = cursor.getString(4);
            bean.iconUrl = cursor.getString(5);
            bean.downUrl = cursor.getString(6);

            String downed = cursor.getString(7);
            if (null != downed)
                bean.repDc = gson.fromJson(downed, type);

            String repInstall = cursor.getString(8);
            if (null != repInstall)
                bean.repInstall = gson.fromJson(repInstall, type);

            String repAc = cursor.getString(9);
            if (null != repAc)
                bean.repAc = gson.fromJson(repAc, type);

            String repDel = cursor.getString(10);
            if (null != repDel)
                bean.repDel = repDel;
            bean.method = cursor.getString(11);
        }
        if (null != cursor) cursor.close();
        database.close();
        return bean;
    }

    /*查找包名*/
    public DmBean queryForBean(String did) {
        DmBean bean = null;
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String[] columns = new String[]{DM_DID, DM_NAME, DM_PACKAGE, DM_VERSIONCODE, DM_SIZE,
                DM_ICON_URL, DM_DOWN_URL, DM_DOWN_ED, DM_REP_INSTALL, DM_REP_AC, DM_REP_DEL, DM_REP_MET};// 要返回哪几个列的数据.如果传入null就等价于select  *,
        String selection = DM_DID + " = ?"; // 查询条件
        String[] selectionArgs = new String[]{did};// 条件的值
        Cursor cursor = database.query(NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            bean = new DmBean();
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            bean.appId = cursor.getString(0);
            bean.appName = cursor.getString(1);
            bean.packageName = cursor.getString(2);
            bean.versionCode = cursor.getString(3);
            bean.size = cursor.getString(4);
            bean.iconUrl = cursor.getString(5);
            bean.downUrl = cursor.getString(6);

            String downed = cursor.getString(7);
            if (null != downed)
                bean.repDc = gson.fromJson(downed, type);

            String repInstall = cursor.getString(8);
            if (null != repInstall)
                bean.repInstall = gson.fromJson(repInstall, type);

            String repAc = cursor.getString(9);
            if (null != repAc)
                bean.repAc = gson.fromJson(repAc, type);

            String repDel = cursor.getString(10);
            if (null != repDel)
                bean.repDel = repDel;
            bean.method = cursor.getString(11);
        }
        if (null != cursor) cursor.close();
        database.close();
        return bean;
    }

    public List<DmBean> query() {
        List<DmBean> dm = new ArrayList<>();
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        Cursor cursor = database.query(NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DmBean dmBean = new DmBean();
                if (null == gson) {
                    gson = new Gson();
                }
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                dmBean.appId = cursor.getString(1);
                dmBean.appName = cursor.getString(2);
                dmBean.packageName = cursor.getString(3);
                dmBean.versionCode = cursor.getString(4);
                dmBean.size = cursor.getString(5);
                dmBean.iconUrl = cursor.getString(6);
                dmBean.downUrl = cursor.getString(7);

                String downed = cursor.getString(8);
                if (null != downed)
                    dmBean.repDc = gson.fromJson(downed, type);

                String repInstall = cursor.getString(9);
                if (null != repInstall)
                    dmBean.repInstall = gson.fromJson(repInstall, type);

                String repAc = cursor.getString(10);
                if (null != repAc)
                    dmBean.repAc = gson.fromJson(repAc, type);

                String repDel = cursor.getString(11);
                if (null != repAc)
                    dmBean.repDel = repDel;

                dmBean.method = cursor.getString(12);
                dm.add(dmBean);
            } while (cursor.moveToNext());
        }
        if (null != cursor) cursor.close();
        database.close();
        return dm;
    }

    public void delete(String did) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String whereClause = DM_DID + " = ?";  //占位符
        String[] whereArgs = new String[]{did};
        database.delete(NAME, whereClause, whereArgs);
        database.close();
    }

    public void update(String did, String apkName, String pkg, String versioncode, String size,
                       String iconUrl, String downUrl, String downed, String repInstall, String repAc,
                       String repDel, String method) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DM_NAME, apkName);
        values.put(DM_PACKAGE, pkg);
        values.put(DM_VERSIONCODE, versioncode);
        values.put(DM_SIZE, size);
        values.put(DM_ICON_URL, iconUrl);
        values.put(DM_DOWN_URL, downUrl);
        if (null != downed) {
            values.put(DM_DOWN_ED, downed);
        }
        if (null != repInstall) {
            values.put(DM_REP_INSTALL, repInstall);
        }
        if (null != repAc) {
            values.put(DM_REP_AC, repAc);
        }
        if (null != repDel) {
            values.put(DM_REP_DEL, repDel);
        }
        values.put(DM_REP_MET, method);
        String whereClause = DM_DID + " = ?";
        String[] whereArgs = new String[]{did};
        database.update(NAME, values, whereClause, whereArgs);
        database.close();
    }
}
