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

public class ResourcesParserLollipop implements IResourceParser {

    private static final String TAG = "ResourcesParserLollipop";
    private static final boolean DEBUG = false;

    @Override
    public Resources getResources(Context context, String apkPath) {
        File file = new File(apkPath);
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
//
//            for (Constructor<?> constructor : pkgParserCls.getConstructors()) {
//                String typeStr = "";
//                for (Class<?> aClass : constructor.getParameterTypes()) {
//                    typeStr += aClass.getSimpleName();
//                    typeStr += ",";
//                }
//                if (DEB) LogUtil.e(TAG, "ResourcesHelper." + "PackageParser constructor :" + typeStr);
//            }

            Class<?>[] typeArgs = {String.class};
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(new Class[0]);
            Object[] valueArgs = {file.getAbsolutePath()};
            Object pkgParser = pkgParserCt.newInstance();

            DisplayMetrics metrics = new DisplayMetrics();
            typeArgs = new Class<?>[]{File.class, int.class};

//            for (Method method : pkgParserCls.getDeclaredMethods()) {
//                String typeStr = method.getName() + "|";
//                for (Class<?> aClass : method.getParameterTypes()) {
//                    typeStr += aClass.getSimpleName();
//                    typeStr += ",";
//                }
//                typeStr += "return " + method.getReturnType().getSimpleName();
//                if (DEB)
//                    LogUtil.e(TAG, "ResourcesHelper." + "PackageParser method params :" + typeStr);
//            }

            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
            valueArgs = new Object[]{file, 0};

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
