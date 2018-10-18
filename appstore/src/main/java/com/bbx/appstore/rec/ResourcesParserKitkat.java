package com.bbx.appstore.rec;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by jarrahwu on 09/12/2016.
 */

public class ResourcesParserKitkat implements IResourceParser {

    private static final String TAG = "ResourcesParserKitkat";
    private static final boolean DEBUG = false;

    @Override
    public Resources getResources(Context context, String apkPath) {
        File file = new File(apkPath);
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = {String.class};
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = {file.getAbsolutePath()};
            Object pkgParser = pkgParserCt.newInstance(valueArgs);

            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            typeArgs = new Class<?>[]{File.class, String.class, DisplayMetrics.class, int.class};
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
            valueArgs = new Object[]{file, file.getAbsolutePath(), metrics, 0};

            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

            if (pkgParserPkg == null) {
                return null;
            }
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

            if (appInfoFld.get(pkgParserPkg) == null) {
                return null;
            }
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Object assetMag = assetMagCls.newInstance();
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = file.getAbsolutePath();
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = context.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = resCt.newInstance(valueArgs);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
