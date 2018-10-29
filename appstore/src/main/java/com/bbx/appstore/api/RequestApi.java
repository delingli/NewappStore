package com.bbx.appstore.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.bbx.appstore.api.tools.Device;
import com.bbx.appstore.base.Config;
import com.bbx.appstore.base.SConstant;
import com.bbx.appstore.bean.AppListInfo;
import com.bbx.appstore.bean.LocalApp;
import com.bbx.appstore.storeutils.ApkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class RequestApi extends StoreApi {

    private volatile static RequestApi request;
    private volatile List<Call> callList = new ArrayList<>();

    private RequestApi() {
    }

    public static RequestApi getInstance() {
        if (null == request) {
            synchronized (RequestApi.class) {
                if (null == request) {
                    request = new RequestApi();
                }
            }
        }
        return request;
    }

    /**
     * 取消所有请求
     */
    public void cancelRequest() {
        for (Call call : callList) {
            if (null != call && !call.isCanceled())
                call.cancel();
        }
        callList.clear();
    }

    /**
     * 单纯获取应用列表的资源
     *
     * @param context  ...
     * @param type     ...自家后台接口类型
     * @param cid      ...同一个接口类型对应的不同广告位
     * @param page     ...下一页
     * @param pageSize ...返回应用个数（返回的不一定就等于该数）
     * @param tMode    ...入口模式（跳转源）
     * @param listener ...回调
     */
    public void fetchAppListInfo(Context context, String type, int cid,
                                 String page, int pageSize, String tMode, final ApiRequestListener listener) {
        String url;
        if (TextUtils.isEmpty(page)) {
            url = SConstant.MARKET +
                    SConstant.TYPE + type +
                    SConstant.CID + cid +
                    SConstant.PAGE + 1;

        } else {
            url = page;
        }
        url = url +
                (tMode == null ? "" : SConstant.T_MODE + tMode) +
                (pageSize == 0 ? "" : SConstant.PAGE_SIZE + pageSize);


        request(context, url, listener);
    }

    /**
     * 获取应用列表资源，凡是通过洗包接口，都需要上传本地应用为后台提供洗包资源
     * <p>
     * 参数参考{@link #fetchAppListInfo}
     */
    public void fetchAppListInfoIgnore(final Context context, final String type, final int cid,
                                       final String page, final int pageSize, final String tMode,
                                       final ApiRequestListener listener) {
        String l;
        if (TextUtils.isEmpty(page)) {
            l = SConstant.MARKET +
                    SConstant.TYPE + type +
                    SConstant.CID + cid +
                    SConstant.PAGE + 1;
        } else {
            l = page;
        }
        l = l +
                (tMode == null ? "" : SConstant.T_MODE + tMode) +
                (pageSize == 0 ? "" : SConstant.PAGE_SIZE + pageSize);

        final String url = l;
        request(context, url, listener);
    }

    /**
     * 获取更新资源
     * 需要上报本机非系统包名
     *
     * @param context  ...
     * @param listener ...回调
     */
    public void fetchUpdateAppListInfo(final Context context, final ApiRequestListener listener) {
        final String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_UPDATE;
        request(context, url, listener);

    }

    public void fetchSearchAppListInfo(final Context context, String keyWord, final ApiRequestListener listener) {
        String url = SConstant.MARKET +
                SConstant.TYPE + SConstant.TYPE_SEARCH +
                SConstant.CID + SConstant.CID_SEARCH +
                "&search=" + keyWord;
        request(context, url, listener);
    }

    /**
     * 具体请求
     *
     * @param context
     * @param url
     * @param listener
     */
    private void request(final Context context, final String url, final ApiRequestListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder builder = new FormBody.Builder();
                List<String> allApps = ApkUtils.scanAllInstallAppList(context);
                Log.d("ldl", "请求获取本地安装app成功...");
                builder.add(SConstant.APP_LIST, new Gson().toJson(allApps));
                Map<String, String> deviceInfo = Device.getDeviceInfo(context);
                for (String param : deviceInfo.keySet()) {
                    String values = deviceInfo.get(param);
                    if (null != values) {
                        builder.add(param, values);
                    } else {
                        builder.add(param, "");
                    }
                }
                toRealRequest(context, url, builder, listener);
            }
        }).start();


    }


    /*============================================================================================*/

    /**
     * 获取应用列表
     *
     * @param context
     * @param page
     * @param callback
     * @param tMode
     */
    public void getAppList(Context context, String page, okhttp3.Callback callback, String tMode) {
        String url;
        if (TextUtils.isEmpty(page)) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID
                    + SConstant.CID_APP_LIST + SConstant.PAGE + 1 + SConstant.T_MODE + tMode;
        } else {
            url = page + SConstant.T_MODE + tMode;
        }
        request(context, callback, url);
    }

    public void getAppListNoMode(Context context, String page, int pageSize, okhttp3.Callback callback) {
        String url;
        if (TextUtils.isEmpty(page)) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID
                    + SConstant.CID_BOTTOM + SConstant.PAGE + 1;
        } else {
            url = page;
        }
        url = url + SConstant.PAGE_SIZE + pageSize;
        request(context, callback, url);
    }

    public void getPowerAppList(Context context, String url, okhttp3.Callback callback, boolean in) {
        String u = url;
        if (null == u) {
            u = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID
                    + (in ? SConstant.CID_POWER_IN : SConstant.CID_POWER_OUT) + SConstant.PAGE + 1 + SConstant.PAGE_SIZE + 20;
        }
        request(context, callback, u);
    }

    /**
     * 获取应用详情
     *
     * @param context
     * @param url
     * @param callback
     */
    public void getAppDetail(Context context, String url, okhttp3.Callback callback) {
        request(context, callback, url);
    }

    /**
     * 升级应用
     *
     * @param context
     * @param callback
     */
    public void getAppUpdate(final Context context, final okhttp3.Callback callback) {  //升级应用特殊处理不做全部应用上传不做pageSize上传
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_UPDATE+SConstant.APP_MARKET+Config.market;
                FormBody.Builder builder = new FormBody.Builder();
                List<LocalApp> localApps = ApkUtils.scanNotSystemAppList(context);
                builder.add(SConstant.APK_LIST, new Gson().toJson(localApps));//非系统应用

                Map<String, String> deviceInfo = Device.getDeviceInfo(context);
                for (String param : deviceInfo.keySet()) {
                    String values = deviceInfo.get(param);
                    if (null != values) {
                        builder.add(param, values);
                    } else {
                        builder.add(param, "");
                    }
                }

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", Device.getDefaultUserAgent(context))
                        .post(builder.build())
                        .build();
                Call call = httpClient.newCall(request);
                callList.add(call);
                call.enqueue(callback);

            }
        }).start();
    }


    /**
     * 推荐洗包
     *
     * @param context
     * @param callback
     */
    public void getWashApp(final Context context, final okhttp3.Callback callback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_RECOMMEND_AD;
        request(context, callback, url);
    }

    /**
     * @param context  ...
     * @param callback ...
     */
    public void guessULike(final Context context, final int cid, final int pageSize, final okhttp3.Callback callback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_RECOMMEND_AD + SConstant.PAGE_SIZE + pageSize
                + SConstant.CID + cid;
        request(context, callback, url);
    }

    /**
     * 需要手动 response.body().close();防止内存泄漏
     *
     * @param context
     * @param cid
     * @param pageSize 不一定返回的就是这个size
     */
    public void fetchNewStore(Context context, int cid, int pageSize, String url, Callback callback) {
        if (url == null) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID + cid + SConstant.PAGE_SIZE + pageSize;
        }
        request(context, callback, url);
    }

    /**
     * 处理具体请求
     *
     * @param context
     * @param callback
     * @param url
     */
    private void request(final Context context, final Callback callback, final String url) {
        final String realurl = url + SConstant.APP_MARKET + Config.market;//统一添加应用市场参数
        Log.d("ldl", "最终得到的url:" + realurl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //添加上报应用
                FormBody.Builder builder = new FormBody.Builder();
                List<String> allApps = ApkUtils.scanAllInstallAppList(context);
                builder.add(SConstant.APP_LIST, new Gson().toJson(allApps));

                Map<String, String> deviceInfo = Device.getDeviceInfo(context);
                for (String param : deviceInfo.keySet()) {
                    String values = deviceInfo.get(param);
                    if (null != values) {
                        builder.add(param, values);
                    } else {
                        builder.add(param, "");
                    }
                }

                Request request = new Request.Builder()
                        .url(realurl)
                        .addHeader("User-Agent", Device.getDefaultUserAgent(context))
                        .post(builder.build())
                        .build();
                Call call = httpClient.newCall(request);
                callList.add(call);
                call.enqueue(callback);

            }
        }).start();


    }

    public interface ApiRequestListener {
        void onCallBack(AppListInfo appListInfo);

        void onError(String e);
    }

    private void toRealRequest(Context context, String url, FormBody.Builder builder, final ApiRequestListener listener) {

        String realUrl = url + SConstant.APP_MARKET + Config.market;//统一添加应用市场参数
        Log.d("ldl", "#RequestApi#最终的请求url：" + realUrl);
        Request request = new Request.Builder()
                .url(realUrl)
                .addHeader("User-Agent", Device.getDefaultUserAgent(context))
                .post(builder.build())
                .build();
        Call call = httpClient.newCall(request);
        callList.add(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) listener.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.code() == 200) {
                    String body = response.body().string();
                    AppListInfo listInfo = new Gson().fromJson(body, AppListInfo.class);
                    response.body().close();
                    if (null == listInfo || null != listInfo.err || listInfo.list.size() <= 0) {
                        if (listener != null) listener.onError("AppListInfo error");
                        return;
                    }
                    if (listener != null) listener.onCallBack(listInfo);
                    return;
                }
                if (listener != null) listener.onError("Response error");
            }
        });
    }

}
