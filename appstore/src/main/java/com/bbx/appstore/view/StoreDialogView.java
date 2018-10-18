package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;

public class StoreDialogView extends IView {

    private static final String TAG = "StoreDialogView";

    private LinearLayout content;
    private TextView close, oneKey, contentText;
    private AppListInfo recommendAppInfo;
    private int x;
    private int y;

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        Intent intent = (Intent) extra;
        recommendAppInfo = (AppListInfo) intent.getSerializableExtra(SConstant.RECOMMEND_LIST);
    }

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_dialog");
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
        if (null != recommendAppInfo && null != recommendAppInfo.list && recommendAppInfo.list.size() > 0) {
            contentText.setText("您已选中 " + recommendAppInfo.list.size() + " 个应用，是否立即下载安装？");
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
                    String dlUrl = null;
                    for (AppDetailInfo info : recommendAppInfo.list) {
                        if (null != info.rpt_dl && null == dlUrl) {
                            dlUrl = info.rpt_dl;
                        }
                        if (ApkUtils.DOWNLOADING != ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
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
//                            DManager.getInstance(mContext).startDownload(mContext, dmBean);
                            DownloadLoopAndInstall.getInstance().addDownloadLoop(mContext, dmBean);
                            if (DOWNLOAD == ApkUtils.checkNeedDownload(mContext, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                Report.getInstance().reportListUrl(mContext, recommendAppInfo.rtp_method, info.rpt_cd, recommendAppInfo.flag_replace, new ClickInfo(x,y));
                            }
                        }
                    }
                    if (null != dlUrl)
                        Report.getInstance().reportUrl(mContext, recommendAppInfo.rtp_method, dlUrl, false, 0);
                    finishCallBack(StoreDialogView.this);
                }
            });
        } else {
            finishCallBack(this);
        }
    }

    private void initView(View view) {
        content = (LinearLayout) view.findViewById(FindRes.getId("dialog_store_content"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            content.setBackground(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        } else {
            content.setBackgroundDrawable(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        }
        contentText = (TextView) view.findViewById(FindRes.getId("dialog_store_content_text"));
        close = (TextView) view.findViewById(FindRes.getId("dialog_store_close"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreDialogView.this);
            }
        });
        oneKey = (TextView) view.findViewById(FindRes.getId("dialog_store_one_key"));
    }
}
