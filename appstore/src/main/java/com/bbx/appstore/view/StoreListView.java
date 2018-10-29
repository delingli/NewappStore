package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.appstore.windows.WindowHandler;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.base.SConstant.APP_NAME;
import static com.bbx.appstore.base.SConstant.DETAIL_ELSE;
import static com.bbx.appstore.base.SConstant.DETAIL_MODE;
import static com.bbx.appstore.base.SConstant.LIST_MODE;
import static com.bbx.appstore.base.SConstant.PKG_NAME;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.view.Page.listPage;

public class StoreListView extends IView {

    private static final String TAG = "StoreListView";

    private String MODE = SConstant.T_MODE_ICON;
    private AppListInfo listInfo;
    private Adapter adapter;
    private long startTime;

    private ImageView back;
    private ListView listView;
    private LinearLayout root;
    private TextView close;

    public StoreListView() {
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        Intent intent = (Intent) extra;
        if (null != intent) {
            MODE = intent.getStringExtra(LIST_MODE);
        }
        if (DBG) Log.e(TAG, "DB_STORE# onCreate");
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_list");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.height = -1;
    }

    @Override
    public void onViewCreated(View view) {
        if (DBG) Log.e(TAG, "DB_STORE# ListView Mode = " + MODE);
        initView(view);
        initData();
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        long time = System.currentTimeMillis() - startTime;
        if (startTime != 0 && null != listInfo && null == listInfo.err && time > 1000) {
            Report.getInstance().reportUrl(mContext, listInfo.rtp_method, listInfo.rpt_st, true, System.currentTimeMillis() - startTime);
        }
        super.finish(floatWindow);
    }

    private void initData() {
        loadData();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreListView.this);
            }
        });
    }

    private void loadData() {
        if (DBG) Log.e(TAG, "DB_STORE# listView loadData...");
        RequestApi.getInstance().getAppList(mContext, listPage, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreListView.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null == response.body()) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listPage = null;
                            finishCallBack(StoreListView.this);
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
                            if (DBG) Log.e(TAG, "DB_STORE# listView = " + listView);
                            listPage = listInfo.href_next;
                            listView.setAdapter(adapter = new Adapter(mContext, listInfo));
                            adapter.setDownClickListener(new AdapterListener() {
                                @Override
                                public void onDownClick() {
//                                    finish();
                                }
                            });
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    AppDetailInfo appInfo = listInfo.list.get(position);
                                    int x=0, y=0;
                                    if (view != null) {
                                        x = (int) view.getX();
                                        y = (int) view.getY();
                                    }
                                    Report.getInstance().reportListUrl(mContext, listInfo.rtp_method, appInfo.rpt_ct, listInfo.flag_replace, new ClickInfo(x, y));
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(PKG_NAME, appInfo.href_detail);
                                    bundle.putString(APP_NAME, appInfo.appname);
                                    bundle.putString(DETAIL_ELSE, MODE);
                                    intent.putExtra(DETAIL_MODE, bundle);
                                    WindowHandler.getInstance().showStoreDetail(intent);
                                }
                            });
                            startTime = System.currentTimeMillis();
                        }
                    });
                } else {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listPage = null;
                            finishCallBack(StoreListView.this);
                        }
                    });
                }
                response.body().close();
            }
        }, MODE);
    }

    private boolean handleData(String body) {
        listInfo = new Gson().fromJson(body, AppListInfo.class);
        return null != listInfo && null == listInfo.err && listInfo.list.size() > 0;
    }

    private void initView(View view) {
        back = (ImageView) view.findViewById(FindRes.getId("list_store_back"));
        listView = (ListView) view.findViewById(FindRes.getId("list_store_list"));
        root = (LinearLayout) view.findViewById(FindRes.getId("list_store_root"));
        back.setImageDrawable(FindRes.getDrawable("store_app_back_selector"));
        close = (TextView) view.findViewById(FindRes.getId("list_store_close"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            close.setBackground(FindRes.getDrawable("store_bg_app_close"));
        } else {
            close.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_close"));
        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreListView.this);
            }
        });
    }

    private static class Adapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context context;
        private AppListInfo listInfo;
        private List<AppDetailInfo> appInfoList;
        private AdapterListener listener;
        private List<AppDetailInfo> tempInfoList = new ArrayList<>();
        private int x;
        private int y;

        private Adapter(Context context, AppListInfo listInfo) {
            this.context = context;
            this.listInfo = listInfo;
            this.appInfoList = listInfo.list;
            mInflater = LayoutInflater.from(context);
        }

        private void setDownClickListener(AdapterListener listener) {
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return appInfoList.size() > 8 ? 8 : appInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return appInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final AppDetailInfo info = appInfoList.get(position);
            convertView = mInflater.inflate(FindRes.getLayout("store_item_view_list"), null);

            if (!tempInfoList.contains(info)) {
                Report.getInstance().reportListUrl(context, listInfo.rtp_method, info.rpt_ss, listInfo.flag_replace, null);
                tempInfoList.add(info);
            }

            ImageView icon = (ImageView) convertView.findViewById(FindRes.getId("store_list_item_icon"));
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                    .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(info.icon, icon, options);

            TextView name = (TextView) convertView.findViewById(FindRes.getId("store_list_item_name"));
            name.setText(info.appname);

            TextView count = (TextView) convertView.findViewById(FindRes.getId("store_list_item_count"));
            try {
                if(!TextUtils.isEmpty(info.downcount)){
                    double downCount = Double.valueOf(info.downcount);
                    count.setText(Utils.downloadNum(downCount));
                }


            } catch (Exception e) {
                if(!TextUtils.isEmpty(info.downcount)){
                    count.setText(info.downcount);
                }

            }

            TextView size = (TextView) convertView.findViewById(FindRes.getId("store_list_item_size"));
            size.setText(Utils.readableFileSize(info.size));

            TextView version = (TextView) convertView.findViewById(FindRes.getId("store_list_item_version"));
            version.setText(Utils.versionName(info.versionname));

            final TextView down = (TextView) convertView.findViewById(FindRes.getId("store_list_item_down"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                down.setBackground(FindRes.getDrawable("store_bg_app_down"));
            } else {
                down.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
            }
            down.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    x = (int) motionEvent.getX();
                    y = (int) motionEvent.getY();
                    return false;
                }
            });
            down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int status = ApkUtils.getStatus(context, info.appid, info.apk, Integer.valueOf(info.versioncode));
                    if (DOWNLOADING == status || OPEN == status || INSTALL == status) {
                        return;
                    }
                    DmBean dmBean = new DmBean();
                    dmBean.appId = info.appid;
                    dmBean.appName = info.appname;
                    dmBean.downUrl = info.href_download;
                    dmBean.packageName = info.apk;
                    dmBean.versionCode = info.versioncode;
                    dmBean.size = info.size;
                    dmBean.iconUrl = info.icon;
                    dmBean.repDc = info.rpt_dc;
                    dmBean.repInstall = info.rpt_ic;
                    dmBean.repAc = info.rpt_ac;
                    dmBean.method = listInfo.rtp_method;
//                    DManager.getInstance(context).startDownload(context, dmBean);
                    DownloadLoopAndInstall.getInstance().addDownloadLoop(context, dmBean);
                    if (DOWNLOAD == ApkUtils.checkNeedDownload(context, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                        Report.getInstance().reportListUrl(context, listInfo.rtp_method, info.rpt_cd, listInfo.flag_replace, new ClickInfo(x,y));
                    }
                    listener.onDownClick();
                }
            });
            return convertView;
        }
    }

    interface AdapterListener {
        void onDownClick();
    }
}
