package com.bbx.appstore.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;

public class StorePowerView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StorePowerView";
    private StoreADInfo mInfo;

    private RelativeLayout content;
    private ImageView battery;
    private TextView status, doing, percent, tips, normalInstall, anp, time, expressInstall;
    private GridView gridView;
    private BatteryBroadcastReceiver receiver;
    private AppListInfo mListInfo;
    private Adapter adapter;
    private Random random = new Random();
    private boolean power;
    private int requestCount;

    private Runnable checkTime = new Runnable() {
        @Override
        public void run() {
            setTime();
            HANDLER.postDelayed(this, 1000);
        }
    };
    private Set<AppDetailInfo> mCacheList;
    private int x;
    private int y;

    public StorePowerView(StoreADInfo info) {
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
        return FindRes.getLayout("store_view_power_for_app_store");
        //        return LayoutInflater.from(context).inflate(FindRes.getLayout("store_view_power"), null);
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.height = -1;
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        if (null != receiver) {
            try {
                receiver.setOnBatteryChangedListener(null);
                mContext.unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                if (DBG) Log.e(TAG, e.getMessage());
            }
            receiver = null;
        }
        super.finish(floatWindow);
        DownloadLoopAndInstall.getInstance().unDNIListener();
    }

    private void initView(View view) {
        RelativeLayout root = (RelativeLayout) view.findViewById(FindRes.getId("power_store_root"));
        RelativeLayout closeContent = (RelativeLayout) view.findViewById(FindRes.getId("power_store_close_content"));
        closeContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StorePowerView.this);
            }
        });
        content = (RelativeLayout) view.findViewById(FindRes.getId("power_store_content"));
        ImageView ring = (ImageView) view.findViewById(FindRes.getId("power_store_ring"));
        ring.setImageDrawable(FindRes.getDrawable("store_bg_power_ring"));
        ImageView smallRing = (ImageView) view.findViewById(FindRes.getId("power_store_ring_small"));
        smallRing.setImageDrawable(FindRes.getDrawable("store_bg_power_ring_small"));
        ImageView close = (ImageView) view.findViewById(FindRes.getId("power_store_close_iv"));
        close.setImageDrawable(FindRes.getDrawable("store_close_power"));
        battery = (ImageView) view.findViewById(FindRes.getId("power_store_battery"));
        status = (TextView) view.findViewById(FindRes.getId("power_store_status"));
        doing = (TextView) view.findViewById(FindRes.getId("power_store_doing"));
        anp = (TextView) view.findViewById(FindRes.getId("power_store_anp"));
        time = (TextView) view.findViewById(FindRes.getId("power_store_time"));
        percent = (TextView) view.findViewById(FindRes.getId("power_store_percent"));
        tips = (TextView) view.findViewById(FindRes.getId("power_store_tips"));
//        normalInstall = (TextView) view.findViewById(FindRes.getId("power_store_install"));
        normalInstall = (TextView) view.findViewById(FindRes.getId("power_store_normal_down"));

        expressInstall = (TextView) view.findViewById(FindRes.getId("power_store_express_down"));
        ImageView expressTips = (ImageView) view.findViewById(FindRes.getId("power_store_express_tips"));
        expressTips.setImageDrawable(FindRes.getDrawable("store_power_express_tips"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root.setBackground(FindRes.getDrawable("store_bg_power"));
//            normalInstall.setBackground(FindRes.getDrawable("store_bg_up_now"));
        } else {
            root.setBackgroundDrawable(FindRes.getDrawable("store_bg_power"));
//            normalInstall.setBackgroundDrawable(FindRes.getDrawable("store_bg_up_now"));
        }
        gridView = (GridView) view.findViewById(FindRes.getId("power_store_grid"));
    }

    private void setTime() {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        if (h > 12) {
            time.setText((h - 12) + " : " + (m > 9 ? m : ("0" + m)));
            anp.setText("下午");
        } else {
            time.setText(h + " : " + (m > 9 ? m : ("0" + m)));
            anp.setText("上午");
        }
    }

    private void setPercent(int p) {
        percent.setText(p + "%");
        if (0 <= p && p <= 20) {
            battery.setImageDrawable(FindRes.getDrawable("store_power_one"));
        }
        if (21 <= p && p <= 40) {
            battery.setImageDrawable(FindRes.getDrawable("store_power_two"));
        }
        if (41 <= p && p <= 60) {
            battery.setImageDrawable(FindRes.getDrawable("store_power_three"));
        }
        if (61 <= p && p <= 80) {
            battery.setImageDrawable(FindRes.getDrawable("store_power_four"));
        }
        if (81 <= p && p <= 100) {
            battery.setImageDrawable(FindRes.getDrawable("store_power_five"));
        }
    }

    private void initData() {
        HANDLER.post(checkTime);
        if (mInfo.show_type.equals(SConstant.SHOW_TYPE_POWER_IN)) {
            registerOnIn();
            power = true;
            getPowerAppList();
            status.setText("充电已连接");
            doing.setVisibility(View.VISIBLE);
        }
        if (mInfo.show_type.equals(SConstant.SHOW_TYPE_POWER_OUT)) {
            registerOnOut();
            power = false;
            getPowerAppList();
            status.setText("充电已断开");
            doing.setVisibility(View.GONE);
        }
        normalInstall.setOnTouchListener(new View.OnTouchListener() {
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
                            Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, appInfo.rpt_cd, mListInfo.flag_replace, new ClickInfo(x, y));
                        }
                        if (null != adapter) adapter.notifyDataSetChanged();
                    }
                    content.setVisibility(View.GONE);
                }
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
                            Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, info.rpt_cd, mListInfo.flag_replace, new ClickInfo(x, y));
                        }
                    }
                    if (mListInfo.attach != null && mListInfo.attach.size() > 0) {
                        AppDetailInfo info = mListInfo.attach.get(0);
                        //上报应用圈
                        Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, info.rpt_cd, mListInfo.flag_replace, new ClickInfo(x, y));
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
                                finishCallBack(StorePowerView.this);
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
                    content.setVisibility(View.GONE);
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

    private void getPowerAppList() {
        RequestApi.getInstance().getPowerAppList(mContext, mListInfo == null ? null : mListInfo.href_next, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StorePowerView.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null == response.body()) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StorePowerView.this);
                        }
                    });
                    return;
                }
                if (response.code() == 200 && handleData(response.body().string())) {
                    requestCount++;
                    if (null == mCacheList) mCacheList = new HashSet<>();
                    if (null != mListInfo.must) {
                        for (AppDetailInfo i : mListInfo.must) {
                            if (mCacheList.size() < 4) {
                                mCacheList.add(i);
                            }
                        }
                    }
                    while (mCacheList.size() < 4 && mListInfo.list.size() > 0) {
                        AppDetailInfo info = mListInfo.list.get(random.nextInt(mListInfo.list.size()));
                        if (mCacheList.contains(info)) {
                            mListInfo.list.remove(info); // 自减,直到while不成立
                        } else {
                            mCacheList.add(info);
                        }
                    }
                    if (mCacheList.size() < 4 && requestCount < 3) { //再次请求下一页, 次数限制3次
                        response.body().close();
                        getPowerAppList();
                        return;
                    }
                    if (mCacheList.size() <= 0) {
                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                finishCallBack(StorePowerView.this);
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
                            DownloadLoopAndInstall.getInstance().setDNIListener(StorePowerView.this);
                            gridView.setAdapter(adapter = new Adapter(mContext, mListInfo));
                            tips.setText(mInfo.desc);
                            content.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    mListInfo = null;
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StorePowerView.this);
                        }
                    });
                }
                response.body().close();
            }
        }, power);
    }

    private boolean handleData(String body) {
        AppListInfo listInfo = new Gson().fromJson(body, AppListInfo.class);
        if (null == listInfo || null != listInfo.err || listInfo.list.size() <= 0) {
            return mListInfo != null;
        }

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
    }

    private static class Adapter extends BaseAdapter {
        private int x, y;
        private Context mContext;
        private AppListInfo mInfo;
        private List<AppDetailInfo> infoList;
        private LayoutInflater mInflater;
        private DisplayImageOptions options;
        private Set<String> cacheRpt;
        private List<Boolean> checkPosition; //勾选的条目

        private Adapter(Context context, AppListInfo info) {
            mContext = context;
            mInfo = info;
            infoList = info.list;
            cacheRpt = new HashSet<>();
            checkPosition = new ArrayList<>();
            for (int i = 0; i < infoList.size(); i++) {
                if (i < 2) {
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
            return null == infoList ? 0 : (infoList.size() > 4 ? 4 : infoList.size());
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
            if (position == 0 || position == 1) {
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

            final AppDetailInfo info = infoList.get(position);
            name.setText(info.appname);
            name.setTextColor(Color.WHITE);
            ImageLoader.getInstance().displayImage(info.icon, icon, options);
//            downIcon.setImageDrawable(FindRes.getDrawable("store_bottom_down_icon"));
//
//            Integer status = ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versionName));
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
//                            dmBean.versionCode = info.versionName;
//                            dmBean.size = info.size;
//                            dmBean.iconUrl = info.icon;
//                            dmBean.repDc = info.rpt_dc;
//                            dmBean.repInstall = info.rpt_ic;
//                            dmBean.repAc = info.rpt_ac;
//                            dmBean.method = mInfo.rtp_method;
////                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
//                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
//                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
//                                Report.getInstance().reportListUrl(mContext, mInfo.rtp_method, info.rpt_cd, mInfo.flag_replace, null);
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
                Report.getInstance().reportListUrl(mContext, mInfo.rtp_method, info.rpt_ss, mInfo.flag_replace, new ClickInfo(x, y));
            }
            return convertView;
        }
    }

    private void registerOnIn() {
        receiver = new BatteryBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(receiver, filter);
        receiver.setOnBatteryChangedListener(new BatteryChangedListener() {
            @Override
            public void onBatteryChangedListener(int p) {
                setPercent(p);
            }

            @Override
            public void onBatteryDisConnected() {
                finishCallBack(StorePowerView.this);
            }

            @Override
            public void onBatteryConnected() {

            }
        });
    }

    private void registerOnOut() {
        receiver = new BatteryBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        mContext.registerReceiver(receiver, filter);
        receiver.setOnBatteryChangedListener(new BatteryChangedListener() {
            @Override
            public void onBatteryChangedListener(int p) {
                setPercent(p);
            }

            @Override
            public void onBatteryDisConnected() {

            }

            @Override
            public void onBatteryConnected() {
                finishCallBack(StorePowerView.this);
            }
        });
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

    private static class BatteryBroadcastReceiver extends BroadcastReceiver {

        private BatteryChangedListener listener;

        void setOnBatteryChangedListener(BatteryChangedListener l) {
            listener = l;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.e("Battery", action);
                switch (action) {
                    case Intent.ACTION_BATTERY_CHANGED://电量发生改变
                        if (null != listener) {
                            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                            int percent = (int) (((float) level / scale) * 100);
                            listener.onBatteryChangedListener(percent);
                        }
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED://拔出电源
                        if (null != listener) listener.onBatteryDisConnected();
                        break;
                    case Intent.ACTION_POWER_CONNECTED:
                        if (null != listener) listener.onBatteryConnected();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    interface BatteryChangedListener {
        void onBatteryChangedListener(int p);

        void onBatteryDisConnected();

        void onBatteryConnected();
    }
}
