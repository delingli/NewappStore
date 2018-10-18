package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.google.gson.Gson;
import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.windows.WindowHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.base.SConstant.APP_NAME;
import static com.bbx.appstore.base.SConstant.DETAIL_MODE;
import static com.bbx.appstore.base.SConstant.DETAIL_ELSE;
import static com.bbx.appstore.base.SConstant.DETAIL_NOTIFY;
import static com.bbx.appstore.base.SConstant.LIST_MODE;
import static com.bbx.appstore.base.SConstant.PKG_NAME;
import static com.bbx.appstore.base.SConstant.T_MODE;
import static com.bbx.appstore.base.SConstant.T_MODE_NOTIFY;
import static com.bbx.appstore.base.SConstant.T_MODE_WIFI;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;

public class StoreDetailView extends IView {

    private static final String TAG = "StoreDetailView";

    private AppDetailInfo detailInfo;
    private String detailUrl;
    private String appName;
    private String MODE;
    private long startTime;

    private LinearLayout root, detail;
    private ImageView back, icon, iv1, iv2;
    private TextView downCount, size, name, down, version, starCount, feature, description, pkgName,
            time, rom, author, close;
//    private ProgressBar progressBar;
//    private RatingBar ratingBar;

    public StoreDetailView() {
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        Intent intent = (Intent) extra;
        if (null == intent) return;
        StoreADInfo info = (StoreADInfo) intent.getSerializableExtra(DETAIL_NOTIFY);
        if (info != null) {
            MODE = T_MODE_NOTIFY;
            detailUrl = info.href + T_MODE + T_MODE_NOTIFY;
            appName = info.name;
        } else {
            Bundle bundle = intent.getBundleExtra(DETAIL_MODE);
            if (null != bundle) {
                String mode = bundle.getString(DETAIL_ELSE);
                MODE = mode;
                detailUrl = bundle.getString(PKG_NAME) + T_MODE + mode;
                appName = bundle.getString(APP_NAME);
            }
        }
        if (DBG) Log.e(TAG, "DB_STORE# onCreate");
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_detail");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.height = -1;
    }

    @Override
    public void onViewCreated(View view) {
        if (MODE == null) finishCallBack(this);
        if (DBG) Log.e(TAG, "DB_STORE# DetailView Mode = " + MODE);
        initView(view);
        initData();
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        long time = System.currentTimeMillis() - startTime;
        if (startTime != 0 && null != detailInfo && null == detailInfo.err && time > 1000) {
            Report.getInstance().reportUrl(mContext, detailInfo.rtp_method, detailInfo.rpt_st, true, System.currentTimeMillis() - startTime);
        }
        super.finish(floatWindow);
    }

    private void initData() {
        name.setText(appName);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (T_MODE_NOTIFY.equals(MODE) || T_MODE_WIFI.equals(MODE)) {
                    finishCallBack(StoreDetailView.this);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(LIST_MODE, MODE);
                    WindowHandler.getInstance().showStoreList(intent);
                }
            }
        });
        loadData();
    }

    private void loadData() {
        RequestApi.getInstance().getAppDetail(mContext, detailUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreDetailView.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null == response.body()) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StoreDetailView.this);
                        }
                    });
                    response.body().close();
                    return;
                }
                if (response.code() == 200 && handleData(response.body().string())) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            root.setVisibility(View.VISIBLE);
                            Report.getInstance().reportListUrl(mContext, detailInfo.rtp_method, detailInfo.rpt_ss,
                                    detailInfo.flag_replace, null);
                            showData();
                            startTime = System.currentTimeMillis();
                        }
                    });
                } else {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StoreDetailView.this);
                        }
                    });
                }
                response.body().close();
            }
        });
    }
    int x,y;
    private void showData() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(detailInfo.icon, icon, options);

        name.setText(detailInfo.appname);
        try {
            double c = Double.valueOf(detailInfo.downcount);
            downCount.setText(Utils.downloadNum(c));
        } catch (NumberFormatException e) {
            downCount.setText(detailInfo.downcount);
        }
        size.setText(Utils.readableFileSize(detailInfo.size));
        version.setText(Utils.versionName(detailInfo.versionname));
        if (null != detailInfo.ratingperson) {
            String c = detailInfo.ratingperson + "人评分";
            starCount.setText(c);
        }
//        ratingBar.setRating(Long.valueOf(detailInfo.rating));

        if (null != detailInfo.screenshots.get(0)) {
            DisplayImageOptions optionsIv1 = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(detailInfo.screenshots.get(0), iv1, optionsIv1);
        }

        if (null != detailInfo.screenshots.get(1)) {
            DisplayImageOptions optionsIv2 = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(detailInfo.screenshots.get(1), iv2, optionsIv2);
        }

        feature.setText(detailInfo.updateinfo);
        description.setText(detailInfo.description);
        pkgName.setText(detailInfo.apk);
        rom.setText(Utils.getAndroidRom(Integer.valueOf(detailInfo.os)));
        time.setText(Utils.dateString((Long.valueOf(detailInfo.updatetime)) * 1000));
        author.setText(detailInfo.developer);
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                 x= (int) motionEvent.getX();
                 y= (int) motionEvent.getY();
                return false;
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = ApkUtils.getStatus(mContext, detailInfo.appid, detailInfo.apk, Integer.valueOf(detailInfo.versioncode));
                if (DOWNLOADING == status || OPEN == status || INSTALL == status) {
                    return;
                }
                DmBean dmBean = new DmBean();
                dmBean.appId = detailInfo.appid;
                dmBean.appName = detailInfo.appname;
                dmBean.downUrl = detailInfo.href_download;
                dmBean.packageName = detailInfo.apk;
                dmBean.versionCode = detailInfo.versioncode;
                dmBean.size = detailInfo.size;
                dmBean.iconUrl = detailInfo.icon;
                dmBean.repDc = detailInfo.rpt_dc;
                dmBean.repInstall = detailInfo.rpt_ic;
                dmBean.repAc = detailInfo.rpt_ac;
                dmBean.method = detailInfo.rtp_method;
//                DManager.getInstance(mContext).startDownload(mContext, dmBean);
                DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                    Report.getInstance().reportListUrl(mContext, detailInfo.rtp_method, detailInfo.rpt_cd, detailInfo.flag_replace, new ClickInfo(x,y));
                }
                down.setClickable(false);
            }
        });
    }

    private boolean handleData(String body) {
        detailInfo = new Gson().fromJson(body, AppDetailInfo.class);
        return null != detailInfo && null == detailInfo.err;
    }

    private void initView(View view) {
        root = (LinearLayout) view.findViewById(FindRes.getId("detail_store_root"));
        back = (ImageView) view.findViewById(FindRes.getId("detail_store_back"));
        back.setImageDrawable(FindRes.getDrawable("store_app_back_selector"));

        icon = (ImageView) view.findViewById(FindRes.getId("detail_store_icon"));
        downCount = (TextView) view.findViewById(FindRes.getId("detail_store_count"));
        size = (TextView) view.findViewById(FindRes.getId("detail_store_size"));
        name = (TextView) view.findViewById(FindRes.getId("detail_store_name"));
        down = (TextView) view.findViewById(FindRes.getId("detail_store_down_app"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            down.setBackground(FindRes.getDrawable("store_tv_bg_down"));
        } else {
            down.setBackgroundDrawable(FindRes.getDrawable("store_tv_bg_down"));
        }
        version = (TextView) view.findViewById(FindRes.getId("detail_store_version_name"));
        starCount = (TextView) view.findViewById(FindRes.getId("detail_store_star_count"));
//        progressBar = (ProgressBar) view.findViewById(FindRes.getId("detail_store_progress"));
//        ratingBar = (RatingBar) view.findViewById(FindRes.getId("detail_store_star"));
//        try {
//            ratingBar.setProgressDrawable(FindRes.getDrawable("store_rating_progress"));
//        } catch (Exception e) {
//            Log.e(TAG, "DB_STORE# ratingBar setting Drawable error");
//        }
        initRatingBar(view);
        iv1 = (ImageView) view.findViewById(FindRes.getId("detail_store_iv1"));
        iv2 = (ImageView) view.findViewById(FindRes.getId("detail_store_iv2"));
        feature = (TextView) view.findViewById(FindRes.getId("detail_store_newFeature"));
        description = (TextView) view.findViewById(FindRes.getId("detail_store_description"));
        pkgName = (TextView) view.findViewById(FindRes.getId("detail_store_pkg_name"));
        time = (TextView) view.findViewById(FindRes.getId("detail_store_update_time"));
        rom = (TextView) view.findViewById(FindRes.getId("detail_store_rom"));
        author = (TextView) view.findViewById(FindRes.getId("detail_store_author_name"));
        detail = (LinearLayout) view.findViewById(FindRes.getId("detail_store_detail"));
        close = (TextView) view.findViewById(FindRes.getId("detail_store_close"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            close.setBackground(FindRes.getDrawable("store_bg_app_close"));
        } else {
            close.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_close"));
        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreDetailView.this);
            }
        });
    }

    private void initRatingBar(View view) {
        ImageView star1 = (ImageView) view.findViewById(FindRes.getId("store_rating_star1"));
        ImageView star2 = (ImageView) view.findViewById(FindRes.getId("store_rating_star2"));
        ImageView star3 = (ImageView) view.findViewById(FindRes.getId("store_rating_star3"));
        ImageView star4 = (ImageView) view.findViewById(FindRes.getId("store_rating_star4"));
        ImageView star5 = (ImageView) view.findViewById(FindRes.getId("store_rating_star5"));
        star1.setImageDrawable(FindRes.getDrawable("store_rating_full"));
        star2.setImageDrawable(FindRes.getDrawable("store_rating_full"));
        star3.setImageDrawable(FindRes.getDrawable("store_rating_full"));
        star4.setImageDrawable(FindRes.getDrawable("store_rating_full"));
        star5.setImageDrawable(FindRes.getDrawable("store_rating_empty"));
    }
}
