package com.bbx.appstore.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.bbx.appstore.storeutils.DateUtils;
import com.bbx.appstore.storeutils.SPUtils;
import com.bbx.appstore.windows.WindowHandler;

public class UserPresentReceiverManager {

    private static PresentReceiver presentReceiver;
    private static final String TAG = "UPRManager";
    public static final String SP_UP_TAG = "second_install_count_dev";
    public static final int DAY_COUNT = 3;

    //解锁时发出的intent广播监听
    public static void receiverPresentStatus(Context context) {
        if (null != presentReceiver) return;
        presentReceiver = new PresentReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(presentReceiver, intentFilter);
    }

    private static class PresentReceiver extends BroadcastReceiver {

        private Handler handler = new Handler();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                boolean wifiConnected = isWifiConnected(context);
                Log.e(TAG, "wifiConnected " + wifiConnected);
                if (wifiConnected && checkDayCount(context.getApplicationContext())) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            WindowHandler.getInstance().showSecondInstall();
                        }
                    });
                }
            }
        }

        private boolean isWifiConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) && activeNetwork.isConnected();
        }

        private boolean checkDayCount(Context context) {
            int i = DateUtils.getInstance(context).differentDay();
            if (i == 0) {
                int count = SPUtils.getInt(context, SP_UP_TAG, DAY_COUNT);
                Log.e(TAG, "one day count = " + count);
                return (count > 0);
            }
            if (i > 0) {
                DateUtils.getInstance(context).saveCurrentTime();
                SPUtils.putInt(context, SP_UP_TAG, DAY_COUNT);
                Log.e(TAG, "new day count = " + DAY_COUNT);
                return true;
            }
            return false;
        }
    }
}
