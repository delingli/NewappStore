package com.bbx.appstore.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
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
import android.widget.TextView;

import com.bbx.appstore.api.Report;
import com.bbx.appstore.api.RequestApi;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppDetailInfo;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.ClickInfo;
import com.bbx.appstore.bean.DmBean;
import com.bbx.appstore.download.DManager;
import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.download.DownloadReceiver;
import com.bbx.appstore.manager.FloatWindowManager;
import com.bbx.appstore.rec.FindRes;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.storeutils.Utils;
import com.bbx.support.MyLinearLayoutManager;
import com.bbx.support.MyRecyclerView;
import com.bbx.support.MyRecyclerViewAdapter;
import com.bbx.support.MyViewHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOAD;
import static com.bbx.appstore.storeutils.ApkUtils.DOWNLOADING;
import static com.bbx.appstore.storeutils.ApkUtils.INSTALL;
import static com.bbx.appstore.storeutils.ApkUtils.OPEN;
import static com.bbx.appstore.storeutils.ApkUtils.UPDATE;

public class StoreSearchView extends IView implements DownloadLoopAndInstall.OnDownloadAndInstallListener {

    private static final String TAG = "StoreSearchView";
    private String searchKey;
    private MyRecyclerView mRecyclerView;
    private AppListInfo mResultList;
    private List<AppDetailInfo> mTempList;
    private Adapter mAdapter;
    private int resultCount = 3;
    private List<SearchModel> pageList;

    @Override
    protected XmlResourceParser onSetLayout() {
        return FindRes.getLayout("store_view_search");
    }

    @Override
    protected void setLayoutParams(ViewParams viewParams) {
        viewParams.height = -1;
        viewParams.gravity = Gravity.TOP;
    }

    @Override
    public String getWindowId() {
        return TAG;
    }

    @Override
    public void onViewCreated(View view) {
        findView(view);
        search();
    }

    private void loadRecommend() {
        RequestApi.getInstance().fetchAppListInfoIgnore(mContext, SConstant.TYPE_RECOMMEND_AD, SConstant.CID_SEARCH_RECOMMEND, null, 6, null,
                new RequestApi.ApiRequestListener() {
                    @Override
                    public void onCallBack(AppListInfo appListInfo) {
                        mResultList = appListInfo;
                        if (pageList == null) pageList = new ArrayList<>();
                        if (mTempList == null) mTempList = new ArrayList<>();
                        pageList.add(new SearchModel(SearchModel.TYPE_RECOMMEND));
                        mTempList.add(new AppDetailInfo());
                        for (AppDetailInfo detailInfo : appListInfo.list) {
                            mTempList.add(detailInfo);
                            pageList.add(new SearchModel(SearchModel.TYPE_NORMAL, detailInfo.apk));
                        }
                        showContent();
                    }

                    @Override
                    public void onError(String e) {
                        showContent();
                    }
                });
    }

    private void search() {
        mRecyclerView.setVisibility(View.GONE);
        if (pageList != null)
            pageList.clear();
        if (mTempList != null)
            mTempList.clear();
        RequestApi.getInstance().fetchSearchAppListInfo(mContext, searchKey, new RequestApi.ApiRequestListener() {
            @Override
            public void onCallBack(AppListInfo appListInfo) {
                mResultList = appListInfo;
                if (mTempList == null) mTempList = new ArrayList<>();
                if (pageList == null) pageList = new ArrayList<>();
                pageList.add(new SearchModel(SearchModel.TYPE_TITLE));
                mTempList.add(new AppDetailInfo());
                for (AppDetailInfo detailInfo : appListInfo.list) {
                    mTempList.add(detailInfo);
                    pageList.add(new SearchModel(SearchModel.TYPE_NORMAL, detailInfo.apk));
                    if (pageList.size() == resultCount + 1) break;
                }
                loadRecommend();
            }

            @Override
            public void onError(String e) {
                loadRecommend();
            }
        });
    }

    private void showContent() {
        if (mResultList != null) {
            mResultList.list = mTempList;
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    DownloadLoopAndInstall.getInstance().setDNIListener(StoreSearchView.this);
                    mRecyclerView.setLayoutManager(new MyLinearLayoutManager(mContext));
                    mRecyclerView.setItemAnimator(null);
                    mRecyclerView.setAdapter(mAdapter = new Adapter(mContext, pageList, mResultList));
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            finishCallBack(StoreSearchView.this);
        }
    }

    @Override
    public void finish(FloatWindowManager.IFloatWindow floatWindow) {
        DownloadLoopAndInstall.getInstance().unDNIListener();
        super.finish(floatWindow);
    }


    private static class Adapter extends MyRecyclerViewAdapter {
        private  int x,y;
        private LayoutInflater mInflater;
        private Context context;
        private AppListInfo resultInfo;
        private Set<AppDetailInfo> tempInfoList = new HashSet<>();
        private List<SearchModel> models;
        private DisplayImageOptions options;
        private List<String> pkgList = new ArrayList<>();

        private Adapter(Context context, List<SearchModel> list, AppListInfo resultInfo) {
            this.resultInfo = resultInfo;
            for (AppDetailInfo detailInfo : resultInfo.list) {
                pkgList.add(detailInfo.apk == null ? "" : detailInfo.apk);
            }
            this.context = context;
            this.models = list;
            mInflater = LayoutInflater.from(context);
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(FindRes.getDrawable("store_icon_loading"))
                    .showImageOnFail(FindRes.getDrawable("store_icon_loading"))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        public void upDataButton(String pkgName) {
            if (pkgList.contains(pkgName)) {
                notifyItemChanged(pkgList.indexOf(pkgName));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return models.get(position).type;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == SearchModel.TYPE_NORMAL) {
                return new NormalHolder(mInflater.inflate(FindRes.getLayout("store_item_view_list"), parent, false));
            }
            if (viewType == SearchModel.TYPE_TITLE) {
                return new TitleHolder(mInflater.inflate(FindRes.getLayout("store_item_view_search_text"), parent, false), "搜索结果");
            }
            if (viewType == SearchModel.TYPE_RECOMMEND) {
                String percent = "的用户搜索该词后下载";
                percent = (new Random().nextInt(38) + 51) + "%" + percent;
                return new TitleHolder(mInflater.inflate(FindRes.getLayout("store_item_view_search_text"), parent, false), percent);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            if (myViewHolder instanceof NormalHolder) {
                final NormalHolder normalHolder = (NormalHolder) myViewHolder;
                AppDetailInfo info = resultInfo.list.get(i);

                if (!tempInfoList.contains(info)) {
                    Report.getInstance().reportListUrl(context, resultInfo.rtp_method, info.rpt_ss, resultInfo.flag_replace, new ClickInfo(x,y));
                    tempInfoList.add(info);
                }

                ImageLoader.getInstance().displayImage(info.icon, normalHolder.icon, options);
                normalHolder.name.setText(info.appname);
                try {
                    double downCount = Double.valueOf(info.downcount);
                    normalHolder.count.setText(Utils.downloadNum(downCount));
                } catch (Exception e) {
                    if(info.downcount!=null){
                        normalHolder.count.setText(info.downcount);
                    }
                }
                normalHolder.size.setText(Utils.readableFileSize(info.size));
                normalHolder.version.setText(Utils.versionName(info.versionname));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    normalHolder.down.setBackground(FindRes.getDrawable("store_bg_app_down"));
                } else {
                    normalHolder.down.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
                }

                normalHolder.down.setTag(info);
                normalHolder.down.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                         x= (int) motionEvent.getX();
                         y= (int) motionEvent.getY();
                        return false;
                    }
                });
                int status = ApkUtils.getStatus(context, info.appid, info.apk, Integer.valueOf(info.versioncode));
                switch (status) {
                    case DOWNLOAD:
                    case UPDATE:
                    default:
                        normalHolder.down.setClickable(true);
                        normalHolder.down.setText("下载");
                        normalHolder.down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppDetailInfo tag = (AppDetailInfo) v.getTag();
                                normalHolder.down.setText("准备中");
                                normalHolder.down.setClickable(false);
                                DmBean dmBean = buildDmBean(tag);
                                DownloadLoopAndInstall.getInstance().addDownloadLoop(context, dmBean);
                                if (DOWNLOAD == ApkUtils.checkNeedDownload(context, dmBean.packageName, Integer.valueOf(dmBean.versionCode))) {
                                    Report.getInstance().reportListUrl(context, resultInfo.rtp_method, tag.rpt_cd, resultInfo.flag_replace, new ClickInfo(x,y));
                                }
                            }
                        });
                        break;
                    case DOWNLOADING:
                        normalHolder.down.setClickable(false);
                        normalHolder.down.setText("下载中");
                        break;
                    case OPEN:
                        normalHolder.down.setClickable(true);
                        normalHolder.down.setText("打开");
                        normalHolder.down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppDetailInfo tag = (AppDetailInfo) v.getTag();
                                ApkUtils.startApp(context, tag.apk);
                            }
                        });
                        break;
                    case INSTALL:
                        normalHolder.down.setClickable(true);
                        normalHolder.down.setText("安装");
                        normalHolder.down.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppDetailInfo tag = (AppDetailInfo) v.getTag();
                                ApkUtils.blueInstall(context, DManager.getInstance(context).getApkFile(tag.appname), DownloadReceiver.IA);
                                if (ApkUtils.hasPermission(context) && DownloadReceiver.IA == 3) {
                                    normalHolder.down.setText("安装中");
                                    normalHolder.down.setClickable(false);
                                }
                            }
                        });
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return models == null ? 0 : models.size();
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
            dmBean.method = resultInfo.rtp_method;
            return dmBean;
        }
    }

    private static class NormalHolder extends MyViewHolder {

        private ImageView icon;
        private TextView name, count, size, version, down;

        public NormalHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(FindRes.getId("store_list_item_icon"));
            name = (TextView) itemView.findViewById(FindRes.getId("store_list_item_name"));
            count = (TextView) itemView.findViewById(FindRes.getId("store_list_item_count"));
            size = (TextView) itemView.findViewById(FindRes.getId("store_list_item_size"));
            version = (TextView) itemView.findViewById(FindRes.getId("store_list_item_version"));
            down = (TextView) itemView.findViewById(FindRes.getId("store_list_item_down"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                down.setBackground(FindRes.getDrawable("store_bg_app_down"));
            } else {
                down.setBackgroundDrawable(FindRes.getDrawable("store_bg_app_down"));
            }
            down.setVisibility(View.VISIBLE);
        }
    }

    private static class TitleHolder extends MyViewHolder {

        private TextView title;

        public TitleHolder(View view, String t) {
            super(view);
            title = (TextView) view.findViewById(FindRes.getId("store_search_list_title"));
            title.setText(t);
        }
    }

    @Override
    public void onCreate(Context context, Object extra) {
        super.onCreate(context, extra);
        Intent intent = (Intent) extra;
        searchKey = intent.getStringExtra("SEARCH_KEY");
    }

    private void findView(View view) {
        ImageView close;
        LinearLayout editLL;
        final EditText edit;
        ImageView clean;

        LinearLayout mRootLayout = (LinearLayout) view.findViewById(FindRes.getId("store_search_root"));
        FrameLayout frameLayout = (FrameLayout) view.findViewById(FindRes.getId("store_search_fl"));
        mRecyclerView = new MyRecyclerView(mContext);
        frameLayout.addView(mRecyclerView);


        close = (ImageView) view.findViewById(FindRes.getId("store_search_close"));
        close.setImageDrawable(FindRes.getDrawable("store_app_back_selector"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCallBack(StoreSearchView.this);
            }
        });

        editLL = (LinearLayout) view.findViewById(FindRes.getId("store_search_edit_bg"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            editLL.setBackground(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        } else {
            editLL.setBackgroundDrawable(FindRes.getDrawable("store_bg_dialog_wifi_ad"));
        }
        edit = (EditText) view.findViewById(FindRes.getId("store_search_edit"));
        edit.setText(searchKey);
        edit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_UP:
                            hideKeyboard();
                            String keyWord = edit.getText().toString().trim();
                            if (TextUtils.isEmpty(keyWord)) {
                                return true;
                            }
                            searchKey = keyWord;
                            search();
                            return true;
                        default:
                            return true;
                    }
                }
                return false;
            }
        });

        clean = (ImageView) view.findViewById(FindRes.getId("store_search_clean"));
        clean.setImageDrawable(FindRes.getDrawable("store_wifi_close"));
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.setText("");
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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
            mAdapter.upDataButton(pkgName);
        }
    }

    private static class SearchModel {
        private static final int
                TYPE_NORMAL = 0,//默认
                TYPE_TITLE = 1,//结果标题
                TYPE_RECOMMEND = 2; //推荐标题

        public int type;// 类型 标题或普通
        public String pkgName;

        private SearchModel(int i) {
            type = i;
        }

        private SearchModel(int i, String pkgName) {
            type = i;
            this.pkgName = pkgName;
        }

        @Override
        public String toString() {
            return "type = " + type;
        }
    }
}
