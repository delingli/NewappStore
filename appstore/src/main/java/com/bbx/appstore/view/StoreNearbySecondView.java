package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.DownloadCart;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.view.Page.bottomPage;

public class StoreNearbySecondView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StoreNearbySecondView";
    private AppListInfo mListInfo;
    private Adapter adapter;
    private LinearLayout root;
    private GridView gridView;
    private StoreADInfo mInfo;
    private TextView normalInstall, expressInstall;
    private int requestCount;
    private Set<AppDetailInfo> mCacheList;
    private int y;

    public StoreNearbySecondView(StoreADInfo info) {
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
        return FindRes.getLayout("store_view_nearby_second");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.gravity = Gravity.BOTTOM;
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        super.finish(floatWindow);
        DownloadLoopAndInstall.getInstance().unDNIListener();
    }

    private void callBackDestroy() {
        if (null != mInfo) {
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishCallBack(StoreNearbySecondView.this);
                }
            }, mInfo.ss_delay);
        }
    }

    private void initData() {
        getAppList();
    }

    private void getAppList() {
        RequestApi.getInstance().getAppListNoMode(mContext, bottomPage,8, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreNearbySecondView.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null == response.body()) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StoreNearbySecondView.this);
                        }
                    });
                    response.body().close();
                    return;
                }
                if (response.code() == 200 && handleData(response.body().string())) {
                    bottomPage = mListInfo.href_next;
                    requestCount++;
                    if (null == mCacheList) mCacheList = new HashSet<>();

                    for (int i = 0; i < mListInfo.list.size(); i++) {
                        mCacheList.add(mListInfo.list.get(i));
                    }

                    if (mCacheList.size() < 8 && requestCount < 3) { //再次请求下一页, 次数限制3次
                        response.body().close();
                        getAppList();
                        return;
                    }
                    if (mCacheList.size() <= 0) {
                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                finishCallBack(StoreNearbySecondView.this);
                            }
                        });
                        response.body().close();
                        return;
                    }
                    mListInfo.list.clear();
                    mListInfo.list.addAll(mCacheList);

                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            DownloadLoopAndInstall.getInstance().setDNIListener(StoreNearbySecondView.this);
                            root.setVisibility(View.VISIBLE);
                            gridView.setAdapter(adapter = new Adapter(mContext, mListInfo));
                            initButton();
                            callBackDestroy();
                        }
                    });
                } else {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            bottomPage = null;
                            finishCallBack(StoreNearbySecondView.this);
                        }
                    });
                }
                response.body().close();
            }
        });
    }
    private int x;
    private void initButton() {
        normalInstall.setOnTouchListener(new View.OnTouchListener() {



            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        expressInstall.setOnTouchListener(new View.OnTouchListener() {



            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        normalInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListInfo && null != mListInfo.list) {
                    List<AppDetailInfo> checkAppInfo = getCheckAppInfo(mListInfo.list);
                    if (checkAppInfo.size() <= 0) {
                        Toast.makeText(mContext, "请勾选应用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < checkAppInfo.size(); i++) {
                        AppDetailInfo appInfo = checkAppInfo.get(i);
                        if (DOWNLOADING == DownloadCart.getInstance().getApkStatus(appInfo.appid)) {
                            continue;
                        }
                        DmBean dmBean = new DmBean();
                        dmBean.appId = appInfo.appid;
                        dmBean.appName = appInfo.appname;
                        dmBean.downUrl = appInfo.href_download;
                        dmBean.packageName = appInfo.apk;
                        dmBean.versionCode = appInfo.versioncode;
                        dmBean.size = appInfo.size;
                        dmBean.iconUrl = appInfo.icon;
                        dmBean.repDc = appInfo.rpt_dc;
                        dmBean.repInstall = appInfo.rpt_ic;
                        dmBean.repAc = appInfo.rpt_ac;
                        dmBean.method = mListInfo.rtp_method;
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                        DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                        if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                            Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, appInfo.rpt_cd, mListInfo.flag_replace, new ClickInfo(x,y));
                        }
                        if (null != adapter) adapter.notifyDataSetChanged();
                    }
                    finishCallBack(StoreNearbySecondView.this);
                }
            }
        });
        expressInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListInfo && null != mListInfo.list) {
                    List<AppDetailInfo> checkAppInfo = getCheckAppInfo(mListInfo.list);
                    if (checkAppInfo.size() <= 0) {
                        Toast.makeText(mContext, "请勾选应用", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //先上报开始下载
                    for (AppDetailInfo info : checkAppInfo) {
                        if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versioncode))) {
                            Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, info.rpt_cd, mListInfo.flag_replace, new ClickInfo(x,y));
                        }
                    }
                    if (mListInfo.attach != null && mListInfo.attach.size() > 0) {
                        AppDetailInfo info = mListInfo.attach.get(0);
                        //上报应用圈
                        Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, info.rpt_cd, mListInfo.flag_replace, new ClickInfo(x,y));
                        mListInfo.list = checkAppInfo;
                        if (OPEN == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versioncode))) {
                            Intent intent = new Intent("com.hai.appstore.intent.action.VIEW");
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                //打开应用圈下载
                                String json = new Gson().toJson(mListInfo);
                                Log.e(TAG, "toJson " + json);
                                intent.putExtra("DOWN_LIST", json);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                                finishCallBack(StoreNearbySecondView.this);
                                return;
                            }
                        }
                        //下载应用圈, 保存AppListInfo
                        DownloadLoopAndInstall.getInstance().setDownListCache(info.apk, mListInfo);
                        DmBean dmBean = buildDmBean(info);
//                    DManager.getInstance(mContext).startDownload(mContext, dmBean);
                        DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                    } else {
                        //服务器配置错误, 正常下载
                        for (AppDetailInfo info : checkAppInfo) {
                            if (ApkUtils.DOWNLOADING != ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                                DmBean dmBean = buildDmBean(info);
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                                DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            }
                        }
                    }
                    finishCallBack(StoreNearbySecondView.this);
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
        dmBean.method = mListInfo.rtp_method;
        return dmBean;
    }

    private List<AppDetailInfo> getCheckAppInfo(List<AppDetailInfo> list) {
        List<Boolean> checkPosition = adapter.getCheckPosition();
        List<AppDetailInfo> infoList = new ArrayList<>();
        for (int i = 0; i < checkPosition.size(); i++) {
            if (checkPosition.get(i)) {
                infoList.add(list.get(i));
            }
        }
        return infoList;
    }

    private boolean handleData(String body) {
        AppListInfo listInfo = new Gson().fromJson(body, AppListInfo.class);
        if (null != listInfo && null == listInfo.err && listInfo.list.size() > 0) {
            List<AppDetailInfo> list = new ArrayList<>();
            for (AppDetailInfo info : listInfo.list) {
                if (ApkUtils.DOWNLOAD == ApkUtils.checkNeedDownload(mContext, info.apk, Integer.valueOf(info.versioncode))) {
                    list.add(info);
                } else {
                    Log.e(TAG, "clean pkg " + info.appname);
                }
            }
            if (mListInfo == null) { //第一次请求
                listInfo.list = list;
                mListInfo = listInfo;
            } else { //第二次或之后的请求
                mListInfo.list = list;
            }
            return true;
        } else {
            return mListInfo != null;
        }
    }

    private void initView(View view) {
        root = (LinearLayout) view.findViewById(FindRes.getId("bottom_store_second_root"));
        ImageView icon = (ImageView) view.findViewById(FindRes.getId("bottom_store_second_icon"));
        icon.setImageDrawable(FindRes.getDrawable("store_bottom_icon"));
        ImageView close = (ImageView) view.findViewById(FindRes.getId("bottom_store_second_close"));
        close.setImageDrawable(FindRes.getDrawable("store_bottom_close"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreNearbySecondView.this);
            }
        });

        normalInstall = (TextView) view.findViewById(FindRes.getId("bottom_store_normal_down"));
        expressInstall = (TextView) view.findViewById(FindRes.getId("bottom_store_express_down"));
        ImageView expressTips = (ImageView) view.findViewById(FindRes.getId("bottom_store_express_tips"));
        expressTips.setImageDrawable(FindRes.getDrawable("store_power_express_tips"));

        gridView = (GridView) view.findViewById(FindRes.getId("bottom_store_second_grid"));
    }

    @Override
    public void downloadComplete(String pkgName) {
        if (null != adapter) adapter.notifyDataSetChanged();
    }

    @Override
    public void downloadFailed(String pkgName) {
        if (null != adapter) adapter.notifyDataSetChanged();
    }

    @Override
    public void installSuccessful(String pkgName) {
        if (null != adapter) adapter.notifyDataSetChanged();
    }

    @Override
    public void downloadProgress(String pkgName, int progress) {

    }

    private static class Adapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private AppListInfo mListInfo;
        private List<AppDetailInfo> appInfoList;
        private DisplayImageOptions options;
        private Set<String> cacheRpt;
        private List<Boolean> checkPosition; //勾选的条目

        private Adapter(Context context, AppListInfo listInfo) {
            mContext = context;
            mListInfo = listInfo;
            appInfoList = listInfo.list;
            cacheRpt = new HashSet<>();
            checkPosition = new ArrayList<>();
            for (int i = 0; i < appInfoList.size(); i++) {
                if (i == 0 || i == 4) {
                    checkPosition.add(true);
                } else {
                    checkPosition.add(false);
                }
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

        public List<Boolean> getCheckPosition() {
            return checkPosition;
        }

        @Override
        public int getCount() {
            return null == appInfoList ? 0 : (appInfoList.size() > 8 ? 8 : appInfoList.size());
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
//            convertView = mInflater.inflate(FindRes.getLayout("store_item_view_bottom_second"), null);
//            TextView name = (TextView) convertView.findViewById(FindRes.getId("store_bottom_item_name"));
//            ImageView icon = (ImageView) convertView.findViewById(FindRes.getId("store_bottom_item_icon"));
//            final TextView percent = (TextView) convertView.findViewById(FindRes.getId("store_bottom_item_icon_percent"));
//            final ImageView downIcon = (ImageView) convertView.findViewById(FindRes.getId("store_bottom_item_down_icon"));
            convertView = mInflater.inflate(FindRes.getLayout("store_item_view_power"), null);
            TextView name = (TextView) convertView.findViewById(FindRes.getId("store_power_item_name"));
            ImageView icon = (ImageView) convertView.findViewById(FindRes.getId("store_power_item_icon"));

            final CheckBox cb = (CheckBox) convertView.findViewById(FindRes.getId("store_power_item_cb"));
            cb.setButtonDrawable(FindRes.getDrawable("store_down_selector"));
            if (position == 0 || position == 4) {
                cb.setChecked(true);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkPosition.set(position, isChecked);
                }
            });
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cb.setChecked(!cb.isChecked());
                    checkPosition.set(position, cb.isChecked());
                }
            });

            final AppDetailInfo info = appInfoList.get(position);
            name.setText(info.appname);
            ImageLoader.getInstance().displayImage(info.icon, icon, options);
//            downIcon.setImageDrawable(FindRes.getDrawable("store_bottom_down_icon"));
//            Integer status = DownloadCart.getInstance().getApkStatus(info.appid);
//            switch (status) {
//                case DOWNLOAD:
//                case UPDATE:
//                default:
//                    icon.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            percent.setVisibility(View.VISIBLE);
//                            percent.setText("下载中");
//                            downIcon.setVisibility(View.GONE);
//                            DmBean dmBean = new DmBean();
//                            dmBean.appId = info.appid;
//                            dmBean.appName = info.appname;
//                            dmBean.downUrl = info.href_download;
//                            dmBean.packageName = info.apk;
//                            dmBean.versionCode = info.versioncode;
//                            dmBean.size = info.size;
//                            dmBean.iconUrl = info.icon;
//                            dmBean.repDc = info.rpt_dc;
//                            dmBean.repInstall = info.rpt_ic;
//                            dmBean.repAc = info.rpt_ac;
//                            dmBean.method = mListInfo.rtp_method;
////                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
//                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
//                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
//                                Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, info.rpt_cd, mListInfo.flag_replace, null);
//                            }
//                            notifyDataSetChanged();
//                        }
//                    });
//                    break;
//                case INSTALL:
//                    downIcon.setVisibility(View.GONE);
//                    percent.setVisibility(View.GONE);
//                    icon.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ApkUtils.blueInstall(mContext, DownloadLoopAndInstall.getInstance().getApkFile(info.appname), DownloadReceiver.IA);
//                            Toast.makeText(mContext, "开始安装" + info.appname, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    break;
//                case OPEN:
//                    downIcon.setVisibility(View.GONE);
//                    percent.setVisibility(View.GONE);
//                    icon.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ApkUtils.startApp(mContext, info.apk);
//                        }
//                    });
//                    break;
//                case DOWNLOADING:
//                    percent.setVisibility(View.VISIBLE);
//                    percent.setText("下载中");
//                    downIcon.setVisibility(View.GONE);
//                    icon.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                        }
//                    });
//                    break;
//            }
            if (!cacheRpt.contains(info.appid)) {
                cacheRpt.add(info.appid);
                Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, info.rpt_ss, mListInfo.flag_replace, null);
            }
            return convertView;
        }
    }
}
