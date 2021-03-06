package com.bbx.appstore.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bbx.appstore.adapter.ClickListener;
import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.StoreADInfo;
import com.bbx.appstore.download.DManager;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.download.DownloadReceiver;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.support.MyOnPageChangeListener;
import com.bbx.support.MyPagerAdapter;
import com.bbx.support.MyViewPager;
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
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.storeutils.ApkUtils.UPDATE;
import static com.bbx.appstore.storeutils.ApkUtils.hasPermission;
import static com.bbx.appstore.view.Page.listPage;

public class StoreNewPowerView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StoreNewPowerView";
    private StoreADInfo mInfo;

    private LinearLayout content;
    private ImageView battery;
    private TextView status, doing, percent, anp, time;
    private BatteryBroadcastReceiver receiver;
    private AppListInfo mListInfo;
    private Random random = new Random();
    private boolean power;
    private int requestCount;
    private static final int DELAY = 3 * 1000;
    private boolean STOP;

    private Runnable checkTime = new Runnable() {
        @Override
        public void run() {
            setTime();
            HANDLER.postDelayed(this, 1000);
        }
    };
    private Set<AppDetailInfo> mCacheList;
    private MyViewPager vp;
    private ImageView view1, view2, view3, view4;

    public StoreNewPowerView(StoreADInfo info) {
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
        return FindRes.getLayout("store_view_new_power");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root.setBackground(FindRes.getDrawable("store_bg_power"));
        } else {
            root.setBackgroundDrawable(FindRes.getDrawable("store_bg_power"));
        }
        RelativeLayout closeContent = (RelativeLayout) view.findViewById(FindRes.getId("power_store_close_content"));
        closeContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListInfo && mListInfo.list.size() > 0) {
                    AppDetailInfo appDetailInfo = mListInfo.list.get(0);
                    if (null != appDetailInfo) {
                        Report.getInstance().reportUrl(mContext, mListInfo.rtp_method, appDetailInfo.rpt_dl, false, 0);
                    }
                }
                finishCallBack(StoreNewPowerView.this);
            }
        });
        content = (LinearLayout) view.findViewById(FindRes.getId("new_power_store_content"));
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

        vp = (MyViewPager) view.findViewById(FindRes.getId("new_power_store_vp"));

        view1 = (ImageView) view.findViewById(FindRes.getId("new_power_store_v1"));
        view2 = (ImageView) view.findViewById(FindRes.getId("new_power_store_v2"));
        view3 = (ImageView) view.findViewById(FindRes.getId("new_power_store_v3"));
        view4 = (ImageView) view.findViewById(FindRes.getId("new_power_store_v4"));
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
        if (mInfo.show_type.equals(SConstant.SHOW_TYPE_NEW_POWER_IN)) {
            registerOnIn();
            power = true;
            getPowerAppList();
            status.setText("充电已连接");
            doing.setVisibility(View.VISIBLE);
        }
        if (mInfo.show_type.equals(SConstant.SHOW_TYPE_NEW_POWER_OUT)) {
            registerOnOut();
            power = false;
            getPowerAppList();
            status.setText("充电已断开");
            doing.setVisibility(View.GONE);
        }
    }

    private void getPowerAppList() {

        RequestApi.getInstance().fetchNewStore(mContext, power ? SConstant.CID_NEW_POWER_IN : SConstant.CID_NEW_POWER_OUT, 3, listPage, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finishCallBack(StoreNewPowerView.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null == response.body()) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StoreNewPowerView.this);
                        }
                    });
                    return;
                }
                if (handleData(response.body().string())) {
                    requestCount++;
                    if (null == mCacheList) mCacheList = new HashSet<>();
//                    if (null != mListInfo.must) {
//                        for (AppDetailInfo i : mListInfo.must) {
//                            if (mCacheList.size() < 3) {
//                                mCacheList.add(i);
//                            }
//                        }
//                    }
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
                                finishCallBack(StoreNewPowerView.this);
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
                            DownloadLoopAndInstall.getInstance().setDNIListener(StoreNewPowerView.this);
                            vp.setAdapter(vpa = new ViewpagerAdapter(mContext, mListInfo, new ClickListener() {
                                @Override
                                public void onClick() {
                                    STOP = true;
                                }
                            }));
                            vp.setOffscreenPageLimit(3);
                            content.setVisibility(View.VISIBLE);
                            setPageChangeListener();
                            setAutoPlay();
                        }
                    });
                } else {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            finishCallBack(StoreNewPowerView.this);
                        }
                    });
                }
                response.body().close();
            }
        });
    }

    private ViewpagerAdapter vpa;

    private void setAutoPlay() {
        HANDLER.postDelayed(autoPlayRunnable, DELAY);
    }

    private Runnable autoPlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (STOP) return;
            int index = vp.getCurrentItem();
            index++;
            vp.setCurrentItem((index > mListInfo.list.size() - 1 ? 0 : index));
            HANDLER.postDelayed(autoPlayRunnable, DELAY);
        }
    };

    private void setPageChangeListener() {
        int size = mListInfo.list.size();
        switch (size) {
            case 1:
                view1.setVisibility(View.VISIBLE);
                break;
            case 2:
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.VISIBLE);
                break;
            case 3:
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.VISIBLE);
                view3.setVisibility(View.VISIBLE);
                break;
            case 4:
            default:
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.VISIBLE);
                view3.setVisibility(View.VISIBLE);
                view4.setVisibility(View.VISIBLE);
                break;
        }
        showIndicator(0);
        vp.addOnPageChangeListener(new MyOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void showIndicator(int position) {
        view1.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_normal"));
        view2.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_normal"));
        view3.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_normal"));
        view4.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_normal"));
        switch (position) {
            case 0:
                view1.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_selector"));
                break;
            case 1:
                view2.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_selector"));
                break;
            case 2:
                view3.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_selector"));
                break;
            case 3:
                view4.setImageDrawable(FindRes.getDrawable("store_bg_shape_oval_selector"));
                break;
        }
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

    private static class ViewpagerAdapter extends MyPagerAdapter {

        private AppListInfo mAppListInfo;
        private Context mContext;
        private DisplayImageOptions options;
        private Set<AppDetailInfo> showReport;
        private List<String> pkgNameList;
        private SparseArray<TextView> cacheView;
        private ClickListener mListener;

        private ViewpagerAdapter(Context context, AppListInfo appListInfo, ClickListener listener) {
            mListener = listener;
            mContext = context;
            mAppListInfo = appListInfo;
            pkgNameList = new ArrayList<>();
            cacheView = new SparseArray<>();
            for (AppDetailInfo appDetailInfo : mAppListInfo.list) {
                pkgNameList.add(appDetailInfo.apk);
            }
            showReport = new HashSet<>();
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                    .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        private void upDataProgress(String pkgName, int percent) {
            int i = pkgNameList.indexOf(pkgName);
            if (i >= 0) {
                TextView textView = cacheView.get(i);
                if (null != textView)
                    textView.setText(percent + "%");
            }
        }
        int x,y;
        private void upDataButton(String pkgName) {
            int i = pkgNameList.indexOf(pkgName);
            if (i >= 0) {
                final AppDetailInfo info = mAppListInfo.list.get(i);
                final TextView down = cacheView.get(i);
                if (null != down) {
                    int status = ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode));
                    down.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                             x= (int) motionEvent.getX();
                             y= (int) motionEvent.getY();
                            return false;
                        }
                    });
                    switch (status) {
                        case DOWNLOAD:
                        case UPDATE:
                        default:
                            down.setClickable(true);
                            down.setText("下载");
                            down.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    down.setText("准备中");
                                    down.setClickable(false);
                                    DmBean dmBean = buildDmBean(info);
                                    DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                                    if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                        Report.getInstance().reportListUrl(mContext, mAppListInfo.rtp_method, info.rpt_cd, mAppListInfo.flag_replace, new ClickInfo(x,y));
                                    }
                                }
                            });
                            break;
                        case INSTALL:
                            down.setClickable(true);
                            down.setText("安装");
                            down.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ApkUtils.blueInstall(mContext, DManager.getInstance(mContext).getApkFile(info.appname), DownloadReceiver.IA);
                                    if (hasPermission(mContext) && DownloadReceiver.IA == 3) {
                                        down.setText("安装中");
                                        down.setClickable(false);
                                    }
                                }
                            });
                            break;
                        case OPEN:
                            down.setClickable(true);
                            down.setText("打开");
                            down.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ApkUtils.startApp(mContext, info.apk);
                                }
                            });
                            break;
                        case DOWNLOADING:
                            down.setClickable(false);
                            down.setText("下载中");
                            break;
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return mAppListInfo.list.size() > 4 ? 4 : mAppListInfo.list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public Object instantiateItem(ViewGroup container, int position) {//必须实现，实例化
            Log.e(TAG, "instantiateItem position " + position);
            View view = LayoutInflater.from(mContext).inflate(FindRes.getLayout("store_item_view_new_power"), container, false);
            AppDetailInfo appDetailInfo = mAppListInfo.list.get(position);

            TextView tv = (TextView) view.findViewById(FindRes.getId("store_new_power_item_name"));
            tv.setText(appDetailInfo.appname);

            TextView edit = (TextView) view.findViewById(FindRes.getId("store_new_power_item_edit"));
            if (!TextUtils.isEmpty(appDetailInfo.description)) {
                edit.setText(appDetailInfo.description);
            } else {
                try {
                    double downCount = Double.valueOf(appDetailInfo.downcount);
                    edit.setText(Utils.downloadNum(downCount));
                } catch (NumberFormatException e) {
                    edit.setText(appDetailInfo.downcount);
                }
            }

            ImageView icon = (ImageView) view.findViewById(FindRes.getId("store_new_power_item_icon"));
            ImageLoader.getInstance().displayImage(appDetailInfo.icon, icon, options);

            ImageView imageView1 = (ImageView) view.findViewById(FindRes.getId("store_new_power_item_pic1"));
            if (appDetailInfo.screenshots.size() > 0 && null != appDetailInfo.screenshots.get(0)) {
                ImageLoader.getInstance().displayImage(appDetailInfo.screenshots.get(0), imageView1);
            }

            ImageView imageView2 = (ImageView) view.findViewById(FindRes.getId("store_new_power_item_pic2"));
            if (appDetailInfo.screenshots.size() > 1 && null != appDetailInfo.screenshots.get(1)) {
                ImageLoader.getInstance().displayImage(appDetailInfo.screenshots.get(1), imageView2);
            }

            ImageView imageView3 = (ImageView) view.findViewById(FindRes.getId("store_new_power_item_pic3"));
            if (appDetailInfo.screenshots.size() > 2 && null != appDetailInfo.screenshots.get(2)) {
                ImageLoader.getInstance().displayImage(appDetailInfo.screenshots.get(2), imageView3);
            }

            TextView down;
            if (cacheView.indexOfKey(position) >= 0) {
                down = cacheView.get(position);
            } else {
                down = (TextView) view.findViewById(FindRes.getId("store_new_power_item_down"));
                cacheView.put(position, down);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                down.setBackground(FindRes.getDrawable("store_bg_app_down"));
            } else {
                down.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
            }

            if (!showReport.contains(appDetailInfo)) {
                showReport.add(appDetailInfo);
                Report.getInstance().reportListUrl(mContext, mAppListInfo.rtp_method, appDetailInfo.rpt_ss, mAppListInfo.flag_replace, new ClickInfo(x,y));
            }
            down.setTag(position);
            down.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    x= (int) motionEvent.getX();
                    y= (int) motionEvent.getY();
                    return false;
                }
            });
            int status = ApkUtils.getStatus(mContext, appDetailInfo.appid, appDetailInfo.apk, Integer.valueOf(appDetailInfo.versioncode));
            switch (status) {
                case DOWNLOAD:
                case UPDATE:
                default:
                    down.setClickable(true);
                    down.setText("下载");
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int p = (Integer) v.getTag();
                            mListener.onClick();
                            cacheView.get(p).setText("准备中");
                            AppDetailInfo tag = mAppListInfo.list.get(p);
                            cacheView.get(p).setClickable(false);
                            DmBean dmBean = buildDmBean(tag);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                Report.getInstance().reportListUrl(mContext, mAppListInfo.rtp_method, tag.rpt_cd, mAppListInfo.flag_replace, new ClickInfo(x,y));
                            }
                        }
                    });
                    break;
                case INSTALL:
                    down.setClickable(true);
                    down.setText("安装");
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int p = (Integer) v.getTag();
                            AppDetailInfo tag = mAppListInfo.list.get(p);
                            ApkUtils.blueInstall(mContext, DManager.getInstance(mContext).getApkFile(tag.appname), DownloadReceiver.IA);
                            if (hasPermission(mContext) && DownloadReceiver.IA == 3) {
                                cacheView.get(p).setText("安装中");
                                cacheView.get(p).setClickable(false);
                            }
                        }
                    });
                    break;
                case OPEN:
                    down.setClickable(true);
                    down.setText("打开");
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int p = (Integer) v.getTag();
                            ApkUtils.startApp(mContext, mAppListInfo.list.get(p).apk);
                        }
                    });
                    break;
                case DOWNLOADING:
                    down.setClickable(false);
                    down.setText("下载中");
                    break;
            }
            container.addView(view);    //这一步很重要
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {//必须实现，销毁
            container.removeView(cacheView.get(position));
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
            dmBean.method = mAppListInfo.rtp_method;
            return dmBean;
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
                finishCallBack(StoreNewPowerView.this);
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
                finishCallBack(StoreNewPowerView.this);
            }
        });
    }

    @Override
    public void downloadComplete(String pkgName) {
        if (null != vpa) vpa.upDataButton(pkgName);
    }

    @Override
    public void downloadFailed(String pkgName) {
        if (null != vpa) vpa.upDataButton(pkgName);
    }

    @Override
    public void installSuccessful(String pkgName) {
        if (null != vpa) vpa.upDataButton(pkgName);
    }

    @Override
    public void downloadProgress(String pkgName, int progress) {
        if (null != vpa) vpa.upDataProgress(pkgName, progress);
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
