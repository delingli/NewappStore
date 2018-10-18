package com.bbx.appstore.adapter;

import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.download.DManager;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.download.DownloadReceiver;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.support.MyPageTransformer;
import com.bbx.support.MyPagerAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.storeutils.ApkUtils.UPDATE;
import static com.bbx.appstore.storeutils.ApkUtils.hasPermission;

/**
 * Created by jiangyong on 18-4-15.
 */

public class VpAdapter extends MyPagerAdapter {

    private AppListInfo appListInfo;
    private Context mContext;
    private Set<AppDetailInfo> showReport;
    private List<String> pkgNameList;

    private DisplayImageOptions ICON_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
            .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
    private DisplayImageOptions PIC_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    private SparseArray<TextView> cacheView = new SparseArray<>();

    private ClickListener mClickListener;

    public VpAdapter(Context context, AppListInfo appListInfo, ClickListener listener) {
        this.appListInfo = appListInfo;
        pkgNameList = new ArrayList<>();
        for (AppDetailInfo detailInfo : appListInfo.list) {
            pkgNameList.add(detailInfo.apk);
        }
        this.mContext = context;
        showReport = new HashSet<>();
        mClickListener = listener;
    }

    @Override
    public int getCount() {
        return appListInfo.list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(FindRes.getLayout("store_item_app_info"), container, false);
        LinearLayout mLlAPp = (LinearLayout) view.findViewById(FindRes.getId("store_appinfo_ll"));
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(Utils.dip2Pixels(110, mContext), Utils.dip2Pixels(110, mContext));
//        mLlAPp.setLayoutParams(params);

        TextView mTvAppTitle = (TextView) view.findViewById(FindRes.getId("store_appinfo_title_tv"));
        TextView mTvAppDesc = (TextView) view.findViewById(FindRes.getId("store_appinfo_desc_tv"));
        TextView mTvAppDownload;
        if (cacheView.indexOfKey(position) >= 0) {
            mTvAppDownload = cacheView.get(position);
        } else {
            mTvAppDownload = (TextView) view.findViewById(FindRes.getId("store_appinfo_download_tv"));
            cacheView.put(position, mTvAppDownload);
        }


        ImageView mIvAppIcon = (ImageView) view.findViewById(FindRes.getId("store_appinfo_icon_iv"));
        ImageView mIvBigPic = (ImageView) view.findViewById(FindRes.getId("store_single_pic"));

        AppDetailInfo info = appListInfo.list.get(position);
        upDataDownButton(info.apk);

        ImageLoader.getInstance().displayImage(info.screenshots.get(0), mIvBigPic, PIC_OPTIONS);
        ImageLoader.getInstance().displayImage(info.icon, mIvAppIcon, ICON_OPTIONS);
        Log.d("ldl", "准备设置name了......");
        if (!TextUtils.isEmpty(info.appname)) {
            mTvAppTitle.setText(info.appname);
            Log.d(SConstant.TAG, "设置name了......appname不为空");

        }
        Log.d(SConstant.TAG, "准备设置updateinfo了......");
        if (!TextUtils.isEmpty(info.updateinfo)) {
            mTvAppDesc.setText(info.updateinfo);
            Log.d(SConstant.TAG, "设置updateinfo了......updateinfo不为空");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTvAppDownload.setBackground(FindRes.getDrawable("store_bg_roll_wifi_download"));
        } else {
            mTvAppDownload.setBackgroundDrawable(FindRes.getDrawable("store_bg_roll_wifi_download"));
        }

        if (!showReport.contains(info)) {
            showReport.add(info);
            Report.getInstance().reportListUrl(mContext, appListInfo.rtp_method, info.rpt_ss, appListInfo.flag_replace, new ClickInfo(x,y));
        }

        container.addView(view);
        return view;
    }

    public void upDataProgress(String pkgName, int percent) {
        int i = pkgNameList.indexOf(pkgName);
        if (i >= 0) {
            TextView textView = cacheView.get(i);
            if (null != textView)
                textView.setText(percent + "%");
        }
    }
    int x, y;
    public void upDataDownButton(String pkgName) {

        int i = pkgNameList.indexOf(pkgName);
        if (i >= 0) {
            final AppDetailInfo info = appListInfo.list.get(i);
            final TextView down = cacheView.get(i);
            if (null != down) {
                down.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        x = (int) motionEvent.getX();
                        y = (int) motionEvent.getY();
                        return false;
                    }
                });
                int status = ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode));
                switch (status) {
                    case DOWNLOAD:
                    case UPDATE:
                    default:
                        down.setClickable(true);
                        down.setText("下载");
                        down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mClickListener.onClick();
                                down.setText("准备中");
                                down.setClickable(false);
                                DmBean dmBean = DmBean.buildDmBean(info);
                                DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                                if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                    Log.d(SConstant.TAG, "得到数据开始上报..."+"x:"+x+"y:"+y);
                                    Report.getInstance().reportListUrl(mContext, appListInfo.rtp_method, info.rpt_cd, appListInfo.flag_replace, new ClickInfo(x,y));
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
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public static class AlphaTransformer extends MyPageTransformer {
        private static final float MIN_SCALE = 0.70f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(View page, float position) {
            if (position < -1 || position > 1) {
                page.setAlpha(MIN_ALPHA);
                page.setScaleX(MIN_SCALE);
                page.setScaleY(MIN_SCALE);
            } else if (position <= 1) { // [-1,1]
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                if (position < 0) {
                    float scaleX = 1 + 0.3f * position;
                    Log.d("DB_ADAPTER", "transformPage: scaleX:" + scaleX);
                    page.setScaleX(scaleX);
                    page.setScaleY(scaleX);
                } else {
                    float scaleX = 1 - 0.3f * position;
                    page.setScaleX(scaleX);
                    page.setScaleY(scaleX);
                }
                page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            }
        }
    }
}
