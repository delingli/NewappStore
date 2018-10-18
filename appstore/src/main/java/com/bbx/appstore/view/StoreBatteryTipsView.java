package com.bbx.appstore.view;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.bbx.appstore.windows.WindowHandler;
import com.bbx.support.MyLinearLayoutManager;
import com.bbx.support.MyRecyclerView;
import com.bbx.support.MyRecyclerViewAdapter;
import com.bbx.support.MyViewHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.storeutils.ApkUtils.UPDATE;

public class StoreBatteryTipsView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StoreBatteryTipsView";
    private StoreADInfo mAdInfo;
    private TextView mPercent;
    private ImageView mBattery;
    private BatteryBroadcastReceiver mReceiver;
    private MyRecyclerView mRecyclerView;
    private AppListInfo mAppListInfo;
    private Set<AppDetailInfo> mAppSet;
    private int loadCount;
    private LinearLayout mRootLayout;
    private Adapter mAdapter;
    private boolean loading;
    private TextView mBatteryStatus;
    private TextView mDoing;
    private String[] mAdText;

    public StoreBatteryTipsView(StoreADInfo info) {
        mAdInfo = info;
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_battery_tips");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {

    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        if (null != mReceiver) {
            try {
                mReceiver.setOnBatteryChangedListener(null);
                mContext.unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                if (DBG) Log.e(TAG, e.getMessage());
            }
            mReceiver = null;
        }
        DownloadLoopAndInstall.getInstance().unDNIListener();
        super.finish(floatWindow);
    }

    @Override
    public void onViewCreated(View view) {
        findView(view);
    }

    private void findView(View view) {
        ImageView close;
        final EditText edit;
        LinearLayout editBg;
        ImageView search;
        FrameLayout frameLayout;
        LinearLayout refreshLL;
        ImageView refreshIV;
        RelativeLayout titleRl;

        titleRl = (RelativeLayout) view.findViewById(FindRes.getId("store_battery_tips_status_rl"));

        mRootLayout = (LinearLayout) view.findViewById(FindRes.getId("store_battery_tips_root"));

        mBattery = (ImageView) view.findViewById(FindRes.getId("store_battery_tips_battery"));
        mBatteryStatus = (TextView) view.findViewById(FindRes.getId("store_battery_tips_status"));
        mDoing = (TextView) view.findViewById(FindRes.getId("store_battery_tips_doing"));

        mPercent = (TextView) view.findViewById(FindRes.getId("store_battery_tips_percent"));

        close = (ImageView) view.findViewById(FindRes.getId("store_battery_tips_close"));
        close.setImageDrawable(FindRes.getDrawable("store_banner_close"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreBatteryTipsView.this);
            }
        });

        edit = (EditText) view.findViewById(FindRes.getId("store_battery_tips_edit"));
        edit.setHint(mAdInfo.name == null ? "抖音" : mAdInfo.name);

        mAdText = mAdInfo.desc.trim().split("\\|");

        edit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_UP:
                            String keyWord = edit.getText().toString().trim();
                            if (TextUtils.isEmpty(keyWord)) {
                                keyWord = edit.getHint().toString();
                            }
                            hideKeyboard();
                            Intent intent = new Intent();
                            intent.putExtra("SEARCH_KEY", keyWord);
                            WindowHandler.getInstance().showSearchView(intent);
                            return true;
                        default:
                            return true;
                    }
                }
                return false;
            }
        });
        editBg = (LinearLayout) view.findViewById(FindRes.getId("store_battery_tips_search_ll"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            titleRl.setBackground(FindRes.getDrawable("store_battery_tips_title_bg"));
            editBg.setBackground(FindRes.getDrawable("store_bg_search"));
        } else {
            titleRl.setBackgroundDrawable(FindRes.getDrawable("store_battery_tips_title_bg"));
            editBg.setBackgroundDrawable(FindRes.getDrawable("store_bg_search"));
        }

        search = (ImageView) view.findViewById(FindRes.getId("store_battery_tips_search"));
        search.setImageDrawable(FindRes.getDrawable("store_ic_search"));
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyWord = edit.getText().toString().trim();
                if (TextUtils.isEmpty(keyWord)) {
                    keyWord = edit.getHint().toString();
                }
                hideKeyboard();
                Intent intent = new Intent();
                intent.putExtra("SEARCH_KEY", keyWord);
                WindowHandler.getInstance().showSearchView(intent);
            }
        });

        frameLayout = (FrameLayout) view.findViewById(FindRes.getId("store_battery_tips_recycler"));
        mRecyclerView = new MyRecyclerView(mContext);
        frameLayout.addView(mRecyclerView);

        refreshLL = (LinearLayout) view.findViewById(FindRes.getId("store_battery_tips_refresh_ll"));
        refreshLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loading) return;
                loading = true;
                loadCount = 0;
                if (mAppSet != null) {
                    mAppSet.clear();
                }
                loadData();
            }
        });
        refreshIV = (ImageView) view.findViewById(FindRes.getId("store_battery_tips_refresh_iv"));
        refreshIV.setImageDrawable(FindRes.getDrawable("store_recommend_refresh"));

        registerBattery();
        loadData();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void loadData() {
        RequestApi.getInstance().fetchAppListInfo(mContext, SConstant.TYPE_LIST, SConstant.CID_BATTERY_TIPS, Page.listPage, 4, null,
                new RequestApi.ApiRequestListener() {
                    @Override
                    public void onCallBack(AppListInfo appListInfo) {
                        Page.listPage = appListInfo.href_next;
                        if (mAppListInfo == null)
                            mAppListInfo = appListInfo;
                        filterAreInstall(appListInfo.list);
                    }

                    @Override
                    public void onError(String e) {
                        Page.listPage = null;
                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                finishCallBack(StoreBatteryTipsView.this);
                            }
                        });
                    }
                });
    }

    private void filterAreInstall(List<AppDetailInfo> list) {
        if (mAppSet == null)
            mAppSet = new LinkedHashSet<>();
        for (AppDetailInfo detailInfo : list) {
            if (!ApkUtils.isAvailable(mContext, detailInfo.apk)) {
                mAppSet.add(detailInfo);
            }
        }
        if (mAppSet.size() < 4 && loadCount < 3) {
            loadData();
            loadCount++;
        } else {
            mAppListInfo.list.clear();
            mAppListInfo.list.addAll(mAppSet);
            showView();
        }
    }

    private void showView() {
        loading = false;
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                DownloadLoopAndInstall.getInstance().setDNIListener(StoreBatteryTipsView.this);
                mRecyclerView.setLayoutManager(new MyLinearLayoutManager(mContext));
                mRecyclerView.setItemAnimator(null);
                mRecyclerView.setAdapter(mAdapter = new Adapter(mContext, mAppListInfo, mAdInfo));
                mRootLayout.setVisibility(View.VISIBLE);
            }
        });
        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishCallBack(StoreBatteryTipsView.this);
            }
        }, mAdInfo.ss_delay);
    }

    private void registerBattery() {
        mReceiver = new BatteryBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        mReceiver.setOnBatteryChangedListener(new BatteryChangedListener() {
            @Override
            public void onBatteryChangedListener(int p) {
                setPercent(p);
            }
        });
    }

    private void setPercent(int p) {
        mPercent.setText(p + "%");
        if (0 <= p && p <= 20) {
            mBattery.setImageDrawable(FindRes.getDrawable("store_power_one"));
        }
        if (21 <= p && p <= 40) {
            mBattery.setImageDrawable(FindRes.getDrawable("store_power_two"));
        }
        if (41 <= p && p <= 60) {
            mBattery.setImageDrawable(FindRes.getDrawable("store_power_three"));
        }
        if (61 <= p && p <= 80) {
            mBattery.setImageDrawable(FindRes.getDrawable("store_power_four"));
        }
        if (81 <= p && p <= 100) {
            mBattery.setImageDrawable(FindRes.getDrawable("store_power_five"));
        }

        if (0 <= p && p <= 30) {
            mBatteryStatus.setText("电量不足");
            if (mAdText.length >= 3) {
                mDoing.setText(mAdText[0]);
            } else {
                mDoing.setText("请尽快充电");
            }
        }
        if (31 <= p && p <= 70) {
            mBatteryStatus.setText("电量充足");
            if (mAdText.length >= 3) {
                mDoing.setText(mAdText[1]);
            } else {
                mDoing.setText("畅玩无忧");
            }
        }
        if (71 <= p) {
            mBatteryStatus.setText("电量饱满");
            if (mAdText.length >= 3) {
                mDoing.setText(mAdText[2]);
            } else {
                mDoing.setText("肆无忌惮");
            }
        }
    }

    @Override
    public void downloadComplete(String pkgName) {
        if (null != mAdapter) {
            mAdapter.upDataButton(pkgName);
        }
    }

    @Override
    public void downloadFailed(String pkgName) {
        if (null != mAdapter) {
            mAdapter.upDataButton(pkgName);
        }
    }

    @Override
    public void installSuccessful(String pkgName) {
        if (null != mAdapter) {
            mAdapter.upDataButton(pkgName);
        }
    }

    @Override
    public void downloadProgress(String pkgName, int progress) {
        if (null != mAdapter) {
            mAdapter.upData(pkgName, progress);
        }
    }

    private static class Adapter extends MyRecyclerViewAdapter {
        private int x, y;
        private Context mContext;
        private LayoutInflater inflater;
        private AppListInfo mListInfo;
        private DisplayImageOptions options;
        private List<String> pkgNameList;
        private List<Integer> progressList;
        private Set<AppDetailInfo> showReport;
        private StoreADInfo adInfo;

        private Adapter(Context context, AppListInfo listInfo, StoreADInfo adInfo) {
            this.adInfo = adInfo;
            mContext = context;
            inflater = LayoutInflater.from(mContext);
            mListInfo = listInfo;
            showReport = new HashSet<>();
            pkgNameList = new ArrayList<>();
            progressList = new ArrayList<>();
            for (AppDetailInfo appDetailInfo : mListInfo.list) {
                pkgNameList.add(appDetailInfo.apk);
                progressList.add(-1);
            }
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                    .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        private void upData(String pkgName, int progress) {
            int i = pkgNameList.indexOf(pkgName);
            if (i >= 0) {
                progressList.set(i, progress);
                notifyItemChanged(i);
            }
        }

        private void upDataButton(String pkgName) {
            if (pkgNameList.contains(pkgName)) {
                notifyItemChanged(pkgNameList.indexOf(pkgName));
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(FindRes.getLayout("store_item_view_recommend"), parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
            AppDetailInfo appDetailInfo = mListInfo.list.get(position);
            final ViewHolder holder = (ViewHolder) myViewHolder;
            holder.name.setText(appDetailInfo.appname);

            ImageLoader.getInstance().displayImage(appDetailInfo.icon, holder.icon, options);

            if (!TextUtils.isEmpty(appDetailInfo.description)) {
                holder.edit.setText(appDetailInfo.description);
            } else {
                try {
                    double downCount = Double.valueOf(appDetailInfo.downcount);
                    holder.edit.setText(Utils.downloadNum(downCount));
                } catch (NumberFormatException e) {
                    holder.edit.setText(appDetailInfo.downcount);
                }
            }

            if (!showReport.contains(appDetailInfo)) {
                showReport.add(appDetailInfo);
                Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, appDetailInfo.rpt_ss, mListInfo.flag_replace, null);
            }

            holder.down.setTag(appDetailInfo);
            int status = ApkUtils.getStatus(mContext, appDetailInfo.appid, appDetailInfo.apk, Integer.valueOf(appDetailInfo.versioncode));
            holder.down.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    x = (int) motionEvent.getX();
                    y = (int) motionEvent.getY();
                    return false;
                }
            });
            switch (status) {
                case DOWNLOAD:
                case UPDATE:
                default:
                    holder.down.setClickable(true);
                    holder.down.setText("下载");
                    holder.down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppDetailInfo tag = (AppDetailInfo) v.getTag();
                            if ("1".equals(adInfo.ci)) {
                                mListInfo.list.clear();
                                mListInfo.list.add(tag);
                                Intent intent = new Intent();
                                intent.putExtra(SConstant.RECOMMEND_LIST, mListInfo);
                                WindowHandler.getInstance().showDialog(intent);
                            } else {
                                holder.down.setText("准备中");
                                holder.down.setClickable(false);
                                DmBean dmBean = buildDmBean(tag);
                                DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                                if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                    Report.getInstance().reportListUrl(mContext, mListInfo.rtp_method, tag.rpt_cd, mListInfo.flag_replace, new ClickInfo(x, y));
                                }
                            }
                        }
                    });
                    break;
                case DOWNLOADING:
                    holder.down.setClickable(false);
                    Integer integer = progressList.get(position);
                    holder.down.setText(integer + "%");
                    break;
                case OPEN:
                    holder.down.setClickable(true);
                    holder.down.setText("打开");
                    holder.down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppDetailInfo tag = (AppDetailInfo) v.getTag();
                            ApkUtils.startApp(mContext, tag.apk);
                        }
                    });
                    break;
                case INSTALL:
                    holder.down.setClickable(true);
                    holder.down.setText("安装");
                    holder.down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppDetailInfo tag = (AppDetailInfo) v.getTag();
                            ApkUtils.blueInstall(mContext, DManager.getInstance(mContext).getApkFile(tag.appname), DownloadReceiver.IA);
                            if (ApkUtils.hasPermission(mContext) && DownloadReceiver.IA == 3) {
                                holder.down.setText("安装中");
                                holder.down.setClickable(false);
                            }
                        }
                    });
                    break;
            }

        }

        @Override
        public int getItemCount() {
            return mListInfo.list.size() > 4 ? 4 : mListInfo.list.size();
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
    }

    private static class ViewHolder extends MyViewHolder {

        private ImageView icon;
        private TextView name, edit, down;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(FindRes.getId("store_recommend_list_item_icon"));
            name = (TextView) itemView.findViewById(FindRes.getId("store_recommend_list_item_name"));
            edit = (TextView) itemView.findViewById(FindRes.getId("store_recommend_list_item_edit"));
            down = (TextView) itemView.findViewById(FindRes.getId("store_recommend_list_item_tv"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                down.setBackground(FindRes.getDrawable("store_bg_app_down_blue"));
            } else {
                down.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down_blue"));
            }
            down.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
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
                switch (action) {
                    case Intent.ACTION_BATTERY_CHANGED://电量发生改变
                        if (null != listener) {
                            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                            int percent = (int) (((float) level / scale) * 100);
                            listener.onBatteryChangedListener(percent);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    interface BatteryChangedListener {
        void onBatteryChangedListener(int p);
    }
}
