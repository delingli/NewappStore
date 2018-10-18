package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.bean.UpdateAppInfo;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.windows.WindowHandler;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StoreBannerView extends IView {

    private static final String TAG = "StoreBannerView";

    private LinearLayout root;
    private ImageView mUpArrow, mAppOne, mAppTwo, mAppThree, mClose;
    private TextView mTitle, mUpNow;
    private UpdateAppInfo updateAppInfo;
    private StoreADInfo mInfo;

    public StoreBannerView(StoreADInfo info) {
        mInfo = info;
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_banner");
    }

    @Override
    protected void setLayoutParams(IView.ViewParams viewParams) {
        viewParams.gravity = Gravity.TOP;
        viewParams.dimBehind = false;
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
    }

    private void callBackDestroy() {
        if (null != mInfo) {
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishCallBack(StoreBannerView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
        RequestApi.getInstance().getAppUpdate(mContext, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (null != call)
                    call.cancel();
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreBannerView.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response) {
                    String json = response.body().string();
                    updateAppInfo = new Gson().fromJson(json, UpdateAppInfo.class);
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != updateAppInfo && null == updateAppInfo.err && null != updateAppInfo.list && updateAppInfo.list.size() > 0) {
                                root.setVisibility(View.VISIBLE);
                                List<UpdateAppInfo.AppInfo> list = updateAppInfo.list;
                                DisplayImageOptions options = new DisplayImageOptions.Builder()
                                        .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                                        .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                                        .cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .bitmapConfig(Bitmap.Config.RGB_565)
                                        .build();
                                ImageLoader.getInstance().displayImage(list.get(0).icon, mAppOne, options);
                                Report.getInstance().reportListUrl(mContext, updateAppInfo.rtp_method, list.get(0).rpt_ss, updateAppInfo.flag_replace, null);
                                if (list.size() > 1) {
                                    ImageLoader.getInstance().displayImage(list.get(1).icon, mAppTwo, options);
                                    Report.getInstance().reportListUrl(mContext, updateAppInfo.rtp_method, list.get(1).rpt_ss, updateAppInfo.flag_replace, null);
                                }
                                if (list.size() > 2) {
                                    ImageLoader.getInstance().displayImage(list.get(2).icon, mAppThree, options);
                                    Report.getInstance().reportListUrl(mContext, updateAppInfo.rtp_method, list.get(2).rpt_ss, updateAppInfo.flag_replace, null);
                                }
                                mTitle.setText(list.size() + "款");
                                callBackDestroy();
                            } else {
                                finishCallBack(StoreBannerView.this);
                            }
                        }
                    });
                    response.body().close();
                    return;
                }
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreBannerView.this);
                    }
                });
            }
        });
    }

    private void initView(View view) {
        root = (LinearLayout) view.findViewById(FindRes.getId("banner_store_root"));
        mUpArrow = (ImageView) view.findViewById(FindRes.getId("banner_store_up_iv"));
        mUpArrow.setImageDrawable(FindRes.getDrawable("store_banner_store_up"));
        mAppOne = (ImageView) view.findViewById(FindRes.getId("banner_store_app_one"));
        mAppTwo = (ImageView) view.findViewById(FindRes.getId("banner_store_app_two"));
        mAppThree = (ImageView) view.findViewById(FindRes.getId("banner_store_app_three"));
        mClose = (ImageView) view.findViewById(FindRes.getId("banner_store_close"));
        mClose.setImageDrawable(FindRes.getDrawable("store_banner_close"));
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreBannerView.this);
            }
        });
        mTitle = (TextView) view.findViewById(FindRes.getId("banner_store_title"));
        mUpNow = (TextView) view.findViewById(FindRes.getId("banner_store_update"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mUpNow.setBackground(FindRes.getDrawable("store_bg_up_now"));
        } else {
            mUpNow.setBackgroundDrawable(FindRes.getDrawable("store_bg_up_now"));
        }
        mUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(SConstant.UPDATE_LIST, updateAppInfo);
                WindowHandler.getInstance().showStoreUpdateList(intent);
            }
        });
    }
}
