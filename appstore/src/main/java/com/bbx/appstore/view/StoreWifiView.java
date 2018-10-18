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

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.appstore.windows.WindowHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.base.SConstant.APP_NAME;
import static com.bbx.appstore.base.SConstant.DETAIL_ELSE;
import static com.bbx.appstore.base.SConstant.DETAIL_MODE;
import static com.bbx.appstore.base.SConstant.LIST_MODE;
import static com.bbx.appstore.base.SConstant.PKG_NAME;
import static com.bbx.appstore.base.SConstant.T_MODE_WIFI;
import static com.bbx.appstore.base.SConstant.T_MODE_WIFI2;
import static com.bbx.appstore.view.Page.wifiPage;

public class StoreWifiView extends IView {

    private static final String TAG = "StoreWifiView";
//    private AppListInfo listInfo;
//    private List<AppDetailInfo> tempList = new ArrayList<>();
//    private Gson gson = new Gson();
//    private Random RANDOM = new Random();

    private ImageView icon, close;
    private TextView name, countAndSize, version, oneKey, more;
    private LinearLayout root, content;
    private StoreADInfo mInfo;
    private int x;
    private int y;

    public StoreWifiView(StoreADInfo data) {
        mInfo = data;
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        if (DBG) Log.e(TAG, "DB_STORE# onCreate");
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_wifi");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {

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
                    finishCallBack(StoreWifiView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
//        RequestApi.getInstance().getAppList(mContext, wifiPage, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                HANDLER.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadFailed();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                handleData(response);
//            }
//        }, T_MODE_WIFI);
        showData();
    }

    private void reload() {
        if (null != wifiPage) {
            RequestApi.getInstance().getAppList(mContext, wifiPage, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StoreWifiView.this);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null == response.body()) {
                        finishCallBack(StoreWifiView.this);
                        return;
                    }
                    handleData(response);
                    response.body().close();
                }
            }, T_MODE_WIFI);
        } else {
            finishCallBack(this);
        }
    }

    private void handleData(Response response) throws IOException {
//        if (response.code() == 200) {
//            listInfo = gson.fromJson(response.body().string(), AppListInfo.class);
//            HANDLER.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (null == listInfo || null != listInfo.err || listInfo.list.size() <= 0) {
//                        loadFailed();
//                        return;
//                    }
//                    wifiPage = listInfo.href_next;
//                    for (AppDetailInfo info : listInfo.list) {
//                        if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versionName))) {
//                            tempList.add(info);
//                        }
//                    }
//                    if (tempList.size() >= 1) {
//                        showData();
//                    } else {
//                        reload();
//                    }
//                }
//            });
//        } else {
//            HANDLER.post(new Runnable() {
//                @Override
//                public void run() {
//                    loadFailed();
//                }
//            });
//        }
    }

    private void showData() {
//        final AppDetailInfo info = tempList.get(RANDOM.nextInt(tempList.size()));
        root.setVisibility(View.VISIBLE);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(mInfo.icon_img, icon, options);
        name.setText(mInfo.name);
        String numberAndSize;
        try {
            double count = Double.valueOf(mInfo.downcount);
            numberAndSize = Utils.downloadNum(count) + "  " + Utils.readableFileSize(mInfo.size);
            countAndSize.setText(numberAndSize);
        } catch (NumberFormatException e) {
            numberAndSize = mInfo.downcount + "  " + Utils.readableFileSize(mInfo.size);
            countAndSize.setText(numberAndSize);
        }
        version.setText(Utils.versionName(mInfo.versionname));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreWifiView.this);
            }
        });
        oneKey.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        oneKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Report.getInstance().reportListUrl(mContext, "POST", mInfo.c_rpt, 0, new ClickInfo(x,y));
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(PKG_NAME, mInfo.href);
                bundle.putString(APP_NAME, mInfo.name);
                bundle.putString(DETAIL_ELSE, T_MODE_WIFI);
                intent.putExtra(DETAIL_MODE, bundle);
                WindowHandler.getInstance().showStoreDetail(intent);
            }
        });
        more.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x= (int) motionEvent.getX();
                y= (int) motionEvent.getY();
                return false;
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Report.getInstance().reportListUrl(mContext, "POST", mInfo.c_rpt, 0, new ClickInfo(x,y));
                Intent intent = new Intent();
                intent.putExtra(LIST_MODE, T_MODE_WIFI2);
                WindowHandler.getInstance().showStoreList(intent);
            }
        });
        Report.getInstance().reportListUrl(mContext, "POST", mInfo.s_rpt, 0, new ClickInfo(x,y));
        callBackDestroy();
        if (DBG) Log.e(TAG, "DB_STORE# showData");
    }

    private void initView(View view) {
        root = (LinearLayout) view.findViewById(FindRes.getId("wifi_store_root"));
        content = (LinearLayout) view.findViewById(FindRes.getId("wifi_store_content"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            content.setBackground(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        } else {
            content.setBackgroundDrawable(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        }
        icon = (ImageView) view.findViewById(FindRes.getId("wifi_store_app_icon"));
        name = (TextView) view.findViewById(FindRes.getId("wifi_store_app_name"));
        countAndSize = (TextView) view.findViewById(FindRes.getId("wifi_store_count_and_size"));
        version = (TextView) view.findViewById(FindRes.getId("wifi_store_app_vc"));
        oneKey = (TextView) view.findViewById(FindRes.getId("wifi_store_one_key"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            oneKey.setBackground(FindRes.getDrawable("store_tv_bg_down"));
        } else {
            oneKey.setBackgroundDrawable(FindRes.getDrawable("store_tv_bg_down"));
        }
        more = (TextView) view.findViewById(FindRes.getId("wifi_store_more"));
        close = (ImageView) view.findViewById(FindRes.getId("wifi_store_close"));
        close.setImageDrawable(FindRes.getDrawable("store_wifi_close"));
    }
}
