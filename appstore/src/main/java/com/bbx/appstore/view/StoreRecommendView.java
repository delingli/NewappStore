package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
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

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;

public class StoreRecommendView extends IView {

    private static final String TAG = "StoreRecommendView";

    private LinearLayout content, root;
    private ListView recommendList;
    //    private TextView close;
    private ImageView ivClose;
    private TextView oneKey;
    private TextView oneKeyForAppStore;
    private AppListInfo recommendAppInfo;
    private Adapter mAdapter;
    private List<AppDetailInfo> cacheInfo;
    private StoreADInfo mInfo;
    private int y;
    private int x;

    public StoreRecommendView(StoreADInfo info) {
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
        return FindRes.getLayout("store_view_recommend_for_app_store");
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
                    finishCallBack(StoreRecommendView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
        RequestApi.getInstance().getWashApp(mContext, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreRecommendView.this);
                    }
                });
                call.cancel();
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (null != response) {
                    String json = response.body().string();
                    recommendAppInfo = new Gson().fromJson(json, AppListInfo.class);
                    if (null != recommendAppInfo && null == recommendAppInfo.err && null != recommendAppInfo.list && recommendAppInfo.list.size() > 0) {
                        cacheInfo = new ArrayList<>();
                        List<AppDetailInfo> list = recommendAppInfo.list;
                        cacheInfo.add(list.get(0));
                        if (list.size() > 1) {
                            cacheInfo.add(list.get(1));
                        }
                        if (list.size() > 2) {
                            cacheInfo.add(list.get(2));
                        }
                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                root.setVisibility(View.VISIBLE);
                                recommendList.setAdapter(mAdapter = new Adapter(mContext, recommendAppInfo, cacheInfo));
                                mAdapter.setCheckListener(new Adapter.CheckListener() {
                                    @Override
                                    public void onCheckListener(AppDetailInfo appInfo, boolean check) {
                                        if (check) {
                                            if (!cacheInfo.contains(appInfo)) {
                                                cacheInfo.add(appInfo);
                                            }
                                        } else {
                                            if (cacheInfo.contains(appInfo)) {
                                                cacheInfo.remove(appInfo);
                                            }
                                        }
                                    }
                                });
                                callBackDestroy();
                            }
                        });
                        return;
                    }
                }
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreRecommendView.this);
                    }
                });
            }
        });
    }

    private void initView(View view) {
        root = (LinearLayout) view.findViewById(FindRes.getId("recommend_store_root"));
        content = (LinearLayout) view.findViewById(FindRes.getId("recommend_store_content"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            content.setBackground(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        } else {
            content.setBackgroundDrawable(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        }
        recommendList = (ListView) view.findViewById(FindRes.getId("recommend_store_list"));
//        close = (TextView) view.findViewById(FindRes.getId("recommend_store_close"));
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finishCallBack();
//            }
//        });
        ivClose = (ImageView) view.findViewById(FindRes.getId("recommend_store_iv_close"));
        ivClose.setImageDrawable(FindRes.getDrawable("store_second_install_close"));
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreRecommendView.this);
            }
        });

        oneKeyForAppStore = (TextView) view.findViewById(FindRes.getId("recommend_store_install_for_app_store"));
        oneKeyForAppStore.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        oneKeyForAppStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cacheInfo.size() <= 0) {
                    Toast.makeText(mContext, "请勾选应用", Toast.LENGTH_SHORT).show();
                    return;
                }
                //先上报点击
                for (AppDetailInfo info : cacheInfo) {
                    if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versioncode))) {
                        Report.getInstance().reportListUrl(mContext, recommendAppInfo.rtp_method, info.rpt_cd, recommendAppInfo.flag_replace, new ClickInfo(x, y));
                    }
                }
                if (recommendAppInfo.attach != null && recommendAppInfo.attach.size() > 0) {
                    AppDetailInfo info = recommendAppInfo.attach.get(0);
                    //上报应用圈
                    Report.getInstance().reportListUrl(mContext, recommendAppInfo.rtp_method, info.rpt_cd, recommendAppInfo.flag_replace, new ClickInfo(x, y));
                    recommendAppInfo.list = cacheInfo;
                    if (OPEN == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versioncode))) {
                        Intent intent = new Intent("com.hai.appstore.intent.action.VIEW");
                        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                            //打开应用圈下载
                            String json = new Gson().toJson(recommendAppInfo);
                            Log.e(TAG, "toJson " + json);
                            intent.putExtra("DOWN_LIST", json);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            finishCallBack(StoreRecommendView.this);
                            return;
                        }
                    }
                    //下载应用圈, 保存AppListInfo
                    DownloadLoopAndInstall.getInstance().setDownListCache(info.apk, recommendAppInfo);
                    DmBean dmBean = buildDmBean(info);
//                    DManager.getInstance(mContext).startDownload(mContext, dmBean);
                    DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                } else {
                    //服务器配置错误, 正常下载
                    for (AppDetailInfo info : cacheInfo) {
                        if (ApkUtils.DOWNLOADING != ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                            DmBean dmBean = buildDmBean(info);
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                        }
                    }
                }
                finishCallBack(StoreRecommendView.this);
            }
        });

        oneKey = (TextView) view.findViewById(FindRes.getId("recommend_store_one_key"));
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
                if (cacheInfo.size() <= 0) {
                    Toast.makeText(mContext, "请勾选应用", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (null != mInfo && "1".equals(mInfo.ci)) {
                    recommendAppInfo.list = cacheInfo;
                    Intent intent = new Intent();
                    intent.putExtra(SConstant.RECOMMEND_LIST, recommendAppInfo);
                    WindowHandler.getInstance().showDialog(intent);
                } else {
                    for (AppDetailInfo info : cacheInfo) {
                        if (ApkUtils.DOWNLOADING != ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                            DmBean dmBean = buildDmBean(info);
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                Report.getInstance().reportListUrl(mContext, recommendAppInfo.rtp_method, info.rpt_cd, recommendAppInfo.flag_replace, new ClickInfo(x, y));
                            }
                        }
                    }
                    finishCallBack(StoreRecommendView.this);
                }
            }
        });
    }

    private DmBean buildDmBean(AppDetailInfo info) {
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
        dmBean.method = recommendAppInfo.rtp_method;
        return dmBean;
    }

    private static class Adapter extends BaseAdapter {
        int x, y;
        private LayoutInflater mInflater;
        private AppListInfo appInfo;
        private List<AppDetailInfo> infoList;
        private CheckListener listener;
        private final DisplayImageOptions options;
        private List<Boolean> check;
        private Context context;

        private Adapter(Context context, AppListInfo info, List<AppDetailInfo> list) {
            this.context = context;
            infoList = list;
            appInfo = info;
            check = new ArrayList<>();
            for (int i = 0; i < infoList.size(); i++) {
                check.add(true);
            }
            mInflater = LayoutInflater.from(context);
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                    .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        @Override
        public int getCount() {
            return infoList.size();
        }

        @Override
        public Object getItem(int position) {
            return infoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.e("getView", "position " + position);
            final AppDetailInfo info = infoList.get(position);
            convertView = mInflater.inflate(FindRes.getLayout("store_item_view_recommend"), null);
            ImageView icon = (ImageView) convertView.findViewById(FindRes.getId("store_recommend_list_item_icon"));
            ImageLoader.getInstance().displayImage(info.icon, icon, options);
            TextView name = (TextView) convertView.findViewById(FindRes.getId("store_recommend_list_item_name"));
            name.setText(info.appname);
            TextView edit = (TextView) convertView.findViewById(FindRes.getId("store_recommend_list_item_edit"));
            if (!TextUtils.isEmpty(info.description)) {
                edit.setText(info.description);
            } else {
                try {
                    double downCount = Double.valueOf(info.downcount);
                    edit.setText(Utils.downloadNum(downCount));
                } catch (NumberFormatException e) {
                    edit.setText(info.downcount);
                }
            }
            CheckBox cb = (CheckBox) convertView.findViewById(FindRes.getId("store_recommend_list_item_cb"));
            cb.setVisibility(View.VISIBLE);
            cb.setButtonDrawable(FindRes.getDrawable("store_checkbox_selector"));
            cb.setChecked(check.get(position));
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.onCheckListener(info, isChecked);
                    check.set(position, isChecked);
                }
            });
            Report.getInstance().reportListUrl(context, appInfo.rtp_method, info.rpt_ss, appInfo.flag_replace, new ClickInfo(x, y));
            return convertView;
        }

        interface CheckListener {
            void onCheckListener(AppDetailInfo info, boolean check);
        }

        private void setCheckListener(CheckListener listener) {
            this.listener = listener;
        }
    }
}
