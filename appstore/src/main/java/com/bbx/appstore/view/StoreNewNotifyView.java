package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bbx.appstore.api.Api;
import com.bbx.appstore.api.Callback;
import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.tools.Address;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.windows.WindowHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import static com.bbx.appstore.base.SConstant.DETAIL_NOTIFY;

public class StoreNewNotifyView extends IView {

    private static final String TAG = "StoreNewNotifyView";

    private ImageView icon, close;
    private TextView title;
    private TextView content;
    private RelativeLayout root;
    private StoreADInfo mInfo;
    private int x;
    private int y;

    public StoreNewNotifyView(StoreADInfo data) {
        mInfo = data;
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        if (DBG) Log.e(SConstant.TAG , "#StoreNewNotifyView#DB_STORE# onCreate");
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_new_notify");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.dimBehind = false;
        viewParams.gravity = Gravity.TOP;
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        loadIconImage();
    }

    private void callBackDestroy() {
        if (null != mInfo) {
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishCallBack(StoreNewNotifyView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
        Api.getInstance().fetchConfig(Address.Area.IN, new Callback<StoreADInfo>() {
            @Override
            public void onFinish(boolean isSuccess, StoreADInfo storeADInfo, int code, Object tag) {
                if (DBG)
                    Log.e(TAG, "DB_STORE# handlerData finish, isSuccess = " + isSuccess + ", adConfig = " + storeADInfo);
                if (!isSuccess && storeADInfo == null) {
                    finishCallBack(StoreNewNotifyView.this);
                    return;
                }

                if (ApkUtils.DOWNLOAD != ApkUtils.checkNeedDownload(mContext, storeADInfo.apk, Integer.valueOf(storeADInfo.versioncode))) {
                    if (DBG) Log.e(TAG, "DB_STORE# 本机已存在此应用，不再展示 " + storeADInfo.name);
                    finishCallBack(StoreNewNotifyView.this);
                    return;
                }
                if (DBG) Log.e(TAG, "DB_STORE# show_type = " + storeADInfo.show_type);
                if (null != storeADInfo.show_type && storeADInfo.show_type.endsWith("bb_notify_appstore")) {
                    mInfo = storeADInfo;
                    if (DBG) Log.e(TAG, "DB_STORE# buildMessage");
                    loadIconImage();
                    return;
                }
                finishCallBack(StoreNewNotifyView.this);
            }
        });
    }

    private void loadIconImage() {
        if (mInfo.icon_img == null) {
            if (DBG) Log.e(SConstant.TAG, "#StoreNewNotifyView#DB_STORE# icon_url == null");
            finishCallBack(this);
            return;
        }
        ImageLoader.getInstance().loadImage(mInfo.icon_img, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        showNotify(loadedImage);
                    }
                });
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                if (DBG) Log.e(SConstant.TAG, "#StoreNewNotifyView#DB_STORE# onLoadingFailed !!!");
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreNewNotifyView.this);
                    }
                });
            }
        });
    }

    private void showNotify(Bitmap loadedImage) {
        if (DBG) Log.e(TAG, "DB_STORE# showNotify !!!");
        title.setText(getNotifyTitle());
        content.setText(getNotifyContent());
        icon.setImageBitmap(loadedImage);
        root.setVisibility(View.VISIBLE);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreNewNotifyView.this);
            }
        });
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Report.getInstance().reportListUrl(mContext, "POST", mInfo.c_rpt, 0, new ClickInfo(x,y));
                Intent intent = new Intent();
                intent.putExtra(DETAIL_NOTIFY, mInfo);
                WindowHandler.getInstance().showStoreDetail(intent);
            }
        });
        if (root.getVisibility() == View.VISIBLE) {
            Report.getInstance().reportListUrl(mContext, "POST", mInfo.s_rpt, 0, new ClickInfo(x,y));
            callBackDestroy();
        } else {
            finishCallBack(this);
        }
    }

    private String getNotifyTitle() {
        return "【有人@你】" + mInfo.name + " 送来惊喜";
    }

    private String getNotifyContent() {
        return " 精彩内容一刷就有！";
    }

    private void initView(View view) {



        root = (RelativeLayout) view.findViewById(FindRes.getId("notify_store_new_root"));
        icon = (ImageView) view.findViewById(FindRes.getId("notify_store_new_icon"));
        close = (ImageView) view.findViewById(FindRes.getId("notify_store_new_close"));
        close.setImageDrawable(FindRes.getDrawable("store_suspension_black_close"));
        title = (TextView) view.findViewById(FindRes.getId("notify_store_new_title"));
        content = (TextView) view.findViewById(FindRes.getId("notify_store_new_content"));
    }
}
