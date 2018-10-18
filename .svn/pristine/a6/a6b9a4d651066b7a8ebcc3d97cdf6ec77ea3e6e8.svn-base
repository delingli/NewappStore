package com.bbx.appstore.rec;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jarrahwu on 08/12/2016.
 */

public class ResourcesHelper {

    private static final String TAG = "ResourcesHelper";
    private static final boolean DEBUG = false;
    private static final Map<String, IResourceParser> CACHES = new HashMap<>();

    public static Resources getResources(Context context, String apkPath) {

        if (CACHES.containsKey(apkPath)) {
            if (DEBUG) Log.e(TAG, "ResourcesHelper." + "return cache resources parser");
            return CACHES.get(apkPath).getResources(context, apkPath);
        }
        IResourceParser parser = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            parser = new ResourcesParserLollipop();
        } else {
            parser = new ResourcesParserKitkat();
        }

        if (DEBUG) Log.e(TAG, "ResourcesHelper." + "cache resources parser with " + apkPath);
        CACHES.put(apkPath, parser);

        return parser.getResources(context, apkPath);
    }

}
