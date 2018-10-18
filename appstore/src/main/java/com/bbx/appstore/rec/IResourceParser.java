package com.bbx.appstore.rec;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by jarrahwu on 08/12/2016.
 */

public interface IResourceParser {

    Resources getResources(Context context, String apkPath);
}
