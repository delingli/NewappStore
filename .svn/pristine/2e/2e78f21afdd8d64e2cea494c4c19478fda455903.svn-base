package com.bbx.appstore.manager;

import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by jarrahwu on 6/20/16.
 */
public class  LayoutParamsBuilder {
    private static final boolean DEBUG = false;
    private static final String TAG = LayoutParamsBuilder.class.getSimpleName();

    protected int type;

    protected int height;

    protected int width;

    protected int gravity;

    protected int y;

    protected int x;

    protected int flag;

    private WindowManager.LayoutParams layoutParams;

    public LayoutParamsBuilder() {
    }

    public LayoutParamsBuilder type(int type) {
        this.type = type;
        return this;
    }

    public LayoutParamsBuilder height(int height) {
        this.height = height;
        return this;
    }

    public LayoutParamsBuilder width(int width) {
        this.width = width;
        return this;
    }

    public LayoutParamsBuilder gravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public LayoutParamsBuilder flag(int flag) {
        this.flag = flag;
        return this;
    }

    public LayoutParamsBuilder y(int y) {
        this.y = y;
        return this;
    }

    public LayoutParamsBuilder x(int x) {
        this.x = x;
        return this;
    }

    public WindowManager.LayoutParams build() {

        int flag = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        if (this.flag == 0) {
            this.flag = flag;
        }

        if (this.width == 0) {
            this.width = -1;
        }

        if (this.height == 0) {
            this.height = -1;
        }

        if (this.type == 0) {
            this.type = typeCompat();
        }

        if (this.gravity == 0) {
            this.gravity = Gravity.TOP;
        }

        layoutParams = new WindowManager.LayoutParams(this.width, this.height, this.type, this.flag, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = this.gravity;

        if (this.x != 0) {
            layoutParams.x = this.x;
        }

        if (this.y != 0) {
            layoutParams.y = this.y;
        }

        return layoutParams;
    }

    public static int typeCompat() {
        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }

        if (SystemProperties.get("ro.miui.ui.version.name", "")
                .equalsIgnoreCase("V6")) {
            if (DEBUG) {
                Log.e(TAG, "TYPE_TOAST");
            }
            type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        if (DEBUG) {
            Log.e(TAG, "type  : " + type);
        }

        return type;

    }
}
