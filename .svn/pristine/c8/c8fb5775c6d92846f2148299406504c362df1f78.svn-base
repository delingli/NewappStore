package com.bbx.appstore.rec;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 找资源文件
 */
public class FindRes {

    private static final boolean DEBUG = false;
    private static final String TAG = "FindRes";

    private static Resources resources;
    private static String pkgName;

    public static void initAppStore(Context context, String path, String packageName) {
        resources = ResourcesHelper.getResources(context, path);
        pkgName = packageName;
    }

    public static XmlResourceParser getLayout(String layoutName) {
        int layout = resources.getIdentifier(layoutName, "layout", pkgName);
        XmlResourceParser layoutXml = resources.getLayout(layout);
        if (DEBUG) Log.d(TAG, "DB_STORE# getLayout: "+ layoutXml);
        return layoutXml;
    }

    public static int getId(String idName) {
        return resources.getIdentifier(idName, "id", pkgName);
    }

    public static Drawable getDrawable(String drawableId) {
        int drawable = resources.getIdentifier(drawableId, "drawable", pkgName);
        return resources.getDrawable(drawable);
    }

    public static String getString(String stringId) {
        return resources.getString(resources.getIdentifier(stringId, "string", pkgName));
    }
}
