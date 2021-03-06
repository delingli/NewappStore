package com.bbx.appstore.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.bean.DownloadCart;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.sqlite.CartDao;
import com.bbx.appstore.sqlite.Dao;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.SPUtils;
import com.bbx.appstore.storeutils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.bbx.appstore.download.DownloadReceiver.IA;
import static com.bbx.appstore.download.UserPresentReceiverManager.DAY_COUNT;
import static com.bbx.appstore.download.UserPresentReceiverManager.SP_UP_TAG;

public class StoreSecondInstallView extends IView {
    private int x, y;
    private static final String TAG = "StoreSecondInstallView";
    private TextView mInstall, mName, mSize, mTrash;
    private ImageView mIcon;
    private Dao mDao;
    private Random mRandom = new Random();
    private DmBean mDmBean;

    public StoreSecondInstallView() {

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
        return FindRes.getLayout("store_view_second_install");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {

    }

    @Override
    public void onViewCreated(View view) {
        initView(view);
        initData();
    }

    private void initData() {
        List<DmBean> localFileList = new ArrayList<>();
        mDao = new Dao(mContext);
        List<DmBean> query = mDao.query();
        for (DmBean dmBean : query) {
            File file = DownloadLoopAndInstall.getInstance().getApkFile(dmBean.appName);
            if (file.exists() && file.isFile()) {
                int status = ApkUtils.getStatus(mContext, dmBean.appId, dmBean.packageName, Integer.valueOf(dmBean.versionCode));
                switch (status) {
                    case ApkUtils.INSTALL:
                    case ApkUtils.DOWNLOAD:
                        localFileList.add(dmBean);
                        break;
                    case ApkUtils.DOWNLOADING:
                        break;
                    case ApkUtils.OPEN:
                    case ApkUtils.UPDATE:
                    default:
                        file.delete();
                        mDao.delete(dmBean.appId);
                        break;
                }
            } else { //上报被用户主动删除的包
                if (null != dmBean.repDc) {
                    List<String> s_rpt = new ArrayList<>();
                    for (String r : dmBean.repDc) {
                        if (r.contains("act=dc")) {
                            String replace = r.replace("act=dc", "act=n2");
                            s_rpt.add(replace);
                            break;
                        }
                    }
                    Report.getInstance().reportListUrl(mContext, "POST", s_rpt, 0, new ClickInfo(x, y));
                }
                mDao.delete(dmBean.appId);
            }
        }
        if (localFileList.size() <= 0) {
            Log.e(TAG, "sql not data");
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    finish(StoreSecondInstallView.this);
                }
            });
            return;
        }

        //随机取一个
        mDmBean = localFileList.get(mRandom.nextInt(localFileList.size()));
        Log.e(TAG, "sql get data dmBean " + mDmBean);
        mIcon.setImageDrawable(ApkUtils.getApkIcon(DownloadLoopAndInstall.getInstance().getApkFile(mDmBean.appName).getAbsolutePath(), mContext));
        mName.setText(mDmBean.appName);
        long size = Long.valueOf(mDmBean.size);
        mSize.setText(Utils.readableFileSize(size));
        long t = (mRandom.nextInt(41943040) + 10485760);
        mTrash.setText(Utils.readableFileSize(t));
        String s = "安装并清理" + Utils.readableFileSize(size + t);
        mInstall.setText(s);
        mInstall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                motionEvent.getY();
                return false;
            }
        });
        mInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                install();
                List<String> c_rpt = new ArrayList<>();
                if (null != mDmBean.repDc) {
                    for (String s : mDmBean.repDc) {
                        if (s.contains("act=dc")) {
                            String replace = s.replace("act=dc", "act=c2");
                            c_rpt.add(replace);
                            break;
                        }
                    }
                    Report.getInstance().reportListUrl(mContext, "POST", c_rpt, 0, new ClickInfo(x, y));
                }
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finish(StoreSecondInstallView.this);
                    }
                });
            }
        });
        if (null != mDmBean.repDc) {
            List<String> s_rpt = new ArrayList<>();
            for (String r : mDmBean.repDc) {
                if (r.contains("act=dc")) {
                    String replace = r.replace("act=dc", "act=s2");
                    s_rpt.add(replace);
                    break;
                }
            }
            Report.getInstance().reportListUrl(mContext, "POST", s_rpt, 0, null);
        }
        int anInt = SPUtils.getInt(mContext, SP_UP_TAG, DAY_COUNT);
        SPUtils.putInt(mContext, SP_UP_TAG, anInt - 1);
        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish(StoreSecondInstallView.this);
            }
        }, 2 * 60 * 1000);
    }

    private void deleteFile() {
        if (null != mDmBean && null != mDao) {
            File apkFile = DownloadLoopAndInstall.getInstance().getApkFile(mDmBean.appName);
            if (apkFile.exists() && apkFile.isFile()) {
                apkFile.delete();
            }
            mDao.delete(mDmBean.appId);
            DownloadCart.getInstance().setApkStatus(mDmBean.appId, ApkUtils.DOWNLOAD);
            Log.e(TAG, "deleteFile and deleteSql");
        }
    }

    private void install() {
        if (null != mDmBean) {
            List<String> rpt = new ArrayList<>();
            for (int i = 0; i < mDmBean.repAc.size(); i++) {
                String s = mDmBean.repAc.get(i);
                if (s.endsWith("&second=2")) {
                    rpt.add(s);
                } else {
                    rpt.add(s + "&second=2");
                }
            }
            mDmBean.repAc.clear();
            mDmBean.repAc.addAll(rpt);
            rpt.clear();

            for (int i = 0; i < mDmBean.repDc.size(); i++) {
                String s = mDmBean.repDc.get(i);
                if (s.endsWith("&second=2")) {
                    rpt.add(s);
                } else {
                    rpt.add(s + "&second=2");
                }
            }
            mDmBean.repDc.clear();
            mDmBean.repDc.addAll(rpt);
            rpt.clear();

            for (int i = 0; i < mDmBean.repInstall.size(); i++) {
                String s = mDmBean.repInstall.get(i);
                if (s.endsWith("&second=2")) {
                    rpt.add(s);
                } else {
                    rpt.add(s + "&second=2");
                }
            }
            mDmBean.repInstall.clear();
            mDmBean.repInstall.addAll(rpt);
            rpt.clear();

            Log.e(TAG, "bean " + mDmBean);
            CartDao.update(mContext, mDmBean.appId, mDmBean.appName, mDmBean.packageName, mDmBean.versionCode,
                    mDmBean.size, mDmBean.iconUrl, mDmBean.downUrl, mDmBean.repDc,
                    mDmBean.repInstall, mDmBean.repAc, mDmBean.repDel, mDmBean.method);
            ApkUtils.blueInstall(mContext, DownloadLoopAndInstall.getInstance().getApkFile(mDmBean.appName), IA);
        }
    }

    private void initView(View view) {
        mIcon = (ImageView) view.findViewById(FindRes.getId("second_install_store_icon"));
        mSize = (TextView) view.findViewById(FindRes.getId("second_install_store_size"));
        mTrash = (TextView) view.findViewById(FindRes.getId("second_install_store_trash_size"));
        mName = (TextView) view.findViewById(FindRes.getId("second_install_store_name"));
        ImageView close = (ImageView) view.findViewById(FindRes.getId("second_install_store_close"));
        close.setImageDrawable(FindRes.getDrawable("store_second_install_close"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        finish(StoreSecondInstallView.this);
                    }
                });
                List<String> c_rpt = new ArrayList<>();
                if (null != mDmBean.repDc) {
                    for (String s : mDmBean.repDc) {
                        if (s.contains("act=dc")) {
                            String replace = s.replace("act=dc", "act=x2");
                            c_rpt.add(replace);
                            break;
                        }
                    }
                    Report.getInstance().reportListUrl(mContext, "POST", c_rpt, 0, null);
                }
                deleteFile();
            }
        });
        mInstall = (TextView) view.findViewById(FindRes.getId("second_install_store_install"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mInstall.setBackground(FindRes.getDrawable("store_bg_second_install"));
        } else {
            mInstall.setBackgroundDrawable(FindRes.getDrawable("store_bg_second_install"));
        }
    }
}
