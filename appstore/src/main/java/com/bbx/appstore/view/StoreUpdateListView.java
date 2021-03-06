package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
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
import com.bbx.appstore.bean.DownloadCart;
import com.bbx.appstore.bean.UpdateAppInfo;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.download.DownloadReceiver;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.Utils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.storeutils.ApkUtils.UPDATE;

public class StoreUpdateListView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StoreUpdateListView";
    private  int x,y;
    private LinearLayout root, guess;
    private TextView oneKey, guessInstall;
    private ListView upDateList;
    private GridView gridView;
    private UpdateAppInfo updateAppInfo;
    private Adapter mAdapter;
    private AppListInfo recommendAppInfo;

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        Intent intent = (Intent) extra;
        if (null != intent) {
            updateAppInfo = (UpdateAppInfo) intent.getSerializableExtra(SConstant.UPDATE_LIST);
        }
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_update_list");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.height = -1;
    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
        loadAD();
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        super.finish(floatWindow);
        DownloadLoopAndInstall.getInstance().unDNIListener();
    }

    private void initData() {
        root.setVisibility(View.VISIBLE);
        if (null != updateAppInfo) {
            upDateList.setAdapter(mAdapter = new Adapter(mContext, updateAppInfo));
            long size = 0;
            try {
                for (UpdateAppInfo.AppInfo info : updateAppInfo.list) {
                    size += Long.valueOf(info.size);
                }
            } catch (Exception e) {

            }
            if (size != 0) {
                oneKey.setText("全部更新(" + Utils.readableFileSize(size) + ")");
            }
        } else {
            finishCallBack(this);
        }
    }

    private void initView(View view) {
        guess = (LinearLayout) view.findViewById(FindRes.getId("update_list_store_guess"));
        root = (LinearLayout) view.findViewById(FindRes.getId("update_list_store_root"));
        guessInstall = (TextView) view.findViewById(FindRes.getId("update_list_store_guess_install"));
        gridView = (GridView) view.findViewById(FindRes.getId("update_list_store_grid"));
        TextView close = (TextView) view.findViewById(FindRes.getId("update_list_store_close"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreUpdateListView.this);
            }
        });
        ImageView back = (ImageView) view.findViewById(FindRes.getId("update_list_store_back"));
        back.setImageDrawable(FindRes.getDrawable("store_update_back"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreUpdateListView.this);
            }
        });
        oneKey = (TextView) view.findViewById(FindRes.getId("update_list_store_one_key"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            close.setBackground(FindRes.getDrawable("store_bg_app_down"));
            oneKey.setBackground(FindRes.getDrawable("store_bg_app_down"));
            guessInstall.setBackground(FindRes.getDrawable("store_bg_app_down"));
        } else {
            close.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
            oneKey.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
            guessInstall.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
        }
        oneKey.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                 x= (int) motionEvent.getX();
                 y= (int) motionEvent.getY();
                return false;
            }
        });
        oneKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mAdapter && null != updateAppInfo.list) {
                    for (UpdateAppInfo.AppInfo info : updateAppInfo.list) {
                        int status = ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode));
                        if (ApkUtils.INSTALL == status) {
                            try {
                                ApkUtils.blueInstall(mContext, DownloadLoopAndInstall.getInstance().getApkFile(info.appname), DownloadReceiver.IA);
                                Toast.makeText(mContext, "开始安装" + info.appname, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                DownloadCart.getInstance().setApkStatus(info.appid, DOWNLOAD);
                            }
                        } else if (ApkUtils.DOWNLOADING != status) {
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
                            dmBean.method = updateAppInfo.rtp_method;
                            dmBean.updateRpt = "update";
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            Report.getInstance().reportListUrl(mContext, updateAppInfo.rtp_method, info.rpt_cd, updateAppInfo.flag_replace, new ClickInfo(x,y));
                        }
                    }
                    oneKey.setClickable(false);
                    mAdapter.upDate();
                }
            }
        });
        upDateList = (ListView) view.findViewById(FindRes.getId("update_list_store_list"));
    }

    @Override
    public void downloadComplete(String pkgName) {
        if (null != guessAdapter) guessAdapter.notifyDataSetChanged();
    }

    @Override
    public void downloadFailed(String pkgName) {
        if (null != guessAdapter) guessAdapter.notifyDataSetChanged();
    }

    @Override
    public void installSuccessful(String pkgName) {
        if (null != guessAdapter) guessAdapter.notifyDataSetChanged();
    }

    @Override
    public void downloadProgress(String pkgName, int progress) {

    }

    private static class Adapter extends BaseAdapter {
        private int x,y;
        private Context context;
        private LayoutInflater mInflater;
        private UpdateAppInfo updateAppInfo;
        private List<UpdateAppInfo.AppInfo> infoList;
        private boolean allUp;
        private final DisplayImageOptions options;

        Adapter(Context context, UpdateAppInfo info) {
            this.context = context;
            this.updateAppInfo = info;
            infoList = info.list;
            mInflater = LayoutInflater.from(context);
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                    .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        void upDate() {
            allUp = true;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return infoList.size() > 3 ? 3 : infoList.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final UpdateAppInfo.AppInfo info = infoList.get(position);
            convertView = mInflater.inflate(FindRes.getLayout("store_item_view_update_list"), null);
            ImageView iconView = (ImageView) convertView.findViewById(FindRes.getId("store_update_list_item_icon"));

            ImageLoader.getInstance().displayImage(info.icon, iconView, options);
            TextView name = (TextView) convertView.findViewById(FindRes.getId("store_update_list_item_name"));
            name.setText(info.appname);
            TextView oldVersion = (TextView) convertView.findViewById(FindRes.getId("store_update_list_item_old_version"));
            oldVersion.setText(ApkUtils.getVersionName(context, info.apk));
            TextView newVersion = (TextView) convertView.findViewById(FindRes.getId("store_update_list_item_new_version"));
            newVersion.setText(info.versionname);

            TextView size = (TextView) convertView.findViewById(FindRes.getId("store_update_list_item_size"));
            size.setText(Utils.readableFileSize(info.size));

            final TextView install = (TextView) convertView.findViewById(FindRes.getId("store_update_list_item_update"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                install.setBackground(FindRes.getDrawable("store_bg_update"));
            } else {
                install.setBackgroundDrawable(FindRes.getDrawable("store_bg_update"));
            }

            install.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                     x= (int) motionEvent.getX();
                     y = (int) motionEvent.getY();
                    Log.d(SConstant.TAG,"点击获取:"+"x:"+x+"y:"+y);
                    return false;
                }
            });
            if (allUp) {
                install.setText("更新中");
                install.setClickable(false);
            } else {
                install.setText("更新");
                install.setClickable(true);
                install.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        install.setClickable(false);
                        install.setText("更新中");
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
                        dmBean.method = updateAppInfo.rtp_method;
                        dmBean.updateRpt = "update";
//                        DManager.getInstance(context).startDownload(context, dmBean);
                        DownloadLoopAndInstall.getInstance().addDownloadLoop(context, dmBean);
                        Report.getInstance().reportListUrl(context, updateAppInfo.rtp_method, info.rpt_cd, updateAppInfo.flag_replace, new ClickInfo(x,y));
                    }
                });
            }
            return convertView;
        }
    }

    private GuessAdapter guessAdapter;

    private void loadAD() {

        RequestApi.getInstance().guessULike(mContext, SConstant.CID_GUESS, 8, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (null != call)
                    call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response) {
                    String json = response.body().string();
                    recommendAppInfo = new Gson().fromJson(json, AppListInfo.class);
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != recommendAppInfo && null == recommendAppInfo.err && null != recommendAppInfo.list && recommendAppInfo.list.size() > 0) {
                                DownloadLoopAndInstall.getInstance().setDNIListener(StoreUpdateListView.this);
                                guess.setVisibility(View.VISIBLE);
                                gridView.setAdapter(guessAdapter = new GuessAdapter(mContext, recommendAppInfo));
                            }
                        }
                    });
                    response.body().close();
                }
            }
        });

        guessInstall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                 x= (int) motionEvent.getX();
                 y= (int) motionEvent.getY();
                return false;
            }
        });
        guessInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recommendAppInfo) {
                    List<AppDetailInfo> list = recommendAppInfo.list;
                    for (int i = 0; i < 8; i++) {
                        AppDetailInfo info = list.get(i);
                        int status = ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode));
                        if (ApkUtils.DOWNLOADING != status) {
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
                            dmBean.method = updateAppInfo.rtp_method;
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                Report.getInstance().reportListUrl(mContext, updateAppInfo.rtp_method, info.rpt_cd, updateAppInfo.flag_replace, new ClickInfo(x,y));
                            }
                        }
                    }
                }
                if (null != guessAdapter) guessAdapter.notifyDataSetChanged();
            }
        });
    }

    private static class GuessAdapter extends BaseAdapter {

        private Context mContext;
        private List<AppDetailInfo> infoList;
        private AppListInfo mInfo;
        private LayoutInflater mInflater;
        private DisplayImageOptions options;
        private Set<String> cacheRpt;
private int x,y;
        private GuessAdapter(Context context, AppListInfo info) {
            this.mContext = context;
            this.mInfo = info;
            infoList = info.list;
            cacheRpt = new HashSet<>();
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
            return null == infoList ? 0 : (infoList.size() > 8 ? 8 : infoList.size());
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
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(FindRes.getLayout("store_item_view_bottom_second"), null);
            TextView name = (TextView) convertView.findViewById(FindRes.getId("store_bottom_item_name"));
            ImageView icon = (ImageView) convertView.findViewById(FindRes.getId("store_bottom_item_icon"));
            final TextView percent = (TextView) convertView.findViewById(FindRes.getId("store_bottom_item_icon_percent"));
            final ImageView downIcon = (ImageView) convertView.findViewById(FindRes.getId("store_bottom_item_down_icon"));

            final AppDetailInfo info = infoList.get(position);
            name.setText(info.appname);
            name.setTextColor(Color.BLACK);
            ImageLoader.getInstance().displayImage(info.icon, icon, options);
            downIcon.setImageDrawable(FindRes.getDrawable("store_bottom_down_icon"));
            icon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    x= (int) motionEvent.getX();
                    y= (int) motionEvent.getY();
                    return false;
                }
            });
            Integer status = DownloadCart.getInstance().getApkStatus(info.appid);
            switch (status) {
                case DOWNLOAD:
                case UPDATE:
                default:
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            percent.setVisibility(View.VISIBLE);
                            percent.setText("下载中");
                            downIcon.setVisibility(View.GONE);
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
                            dmBean.method = mInfo.rtp_method;
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                Report.getInstance().reportListUrl(mContext, mInfo.rtp_method, info.rpt_cd, mInfo.flag_replace, new ClickInfo(x,y));
                            }
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case INSTALL:
                    downIcon.setVisibility(View.GONE);
                    percent.setVisibility(View.GONE);
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ApkUtils.blueInstall(mContext, DownloadLoopAndInstall.getInstance().getApkFile(info.appname), DownloadReceiver.IA);
                            Toast.makeText(mContext, "开始安装" + info.appname, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case OPEN:
                    downIcon.setVisibility(View.GONE);
                    percent.setVisibility(View.GONE);
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ApkUtils.startApp(mContext, info.apk);
                        }
                    });
                    break;
                case DOWNLOADING:
                    percent.setVisibility(View.VISIBLE);
                    percent.setText("下载中");
                    downIcon.setVisibility(View.GONE);
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    break;
            }

            if (!cacheRpt.contains(info.appid)) {
                cacheRpt.add(info.appid);
                Report.getInstance().reportListUrl(mContext, mInfo.rtp_method, info.rpt_ss, mInfo.flag_replace, new ClickInfo(x,y));
            }
            return convertView;
        }
    }
}
