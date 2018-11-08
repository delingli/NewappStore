/*
package com.bbx.dynamicas;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bbx.appstore.download.DownloadLoopAndInstall;
import com.bbx.appstore.storeutils.ApkUtils;
import com.bbx.appstore.windows.AppStoreHandler;

import java.io.File;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        setContentView(R.layout.activity_main);
        AppStoreHandler.getInstance().init(getApplicationContext(), "/sdcard/storeapkres-debug.apk", "com.test.storeapkres");
//        IntentFilter newbrazenreceiver2Filter = new IntentFilter();
//        newbrazenreceiver2Filter.addAction("test");
//        registerReceiver(bro, newbrazenreceiver2Filter);
    }

    private BroadcastReceiver bro = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals("test")) {
                Intent intents = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult("AA", intents, 10, null);
            }

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void test(View view) {
    }

    public void insert(View view) {
    }

    public void notify(View view) {
//        AppStoreHandler.getInstance().show(getApplicationContext(), notifyTest, new AppStoreHandler.OnStoreDestroyListener() {
//            @Override
//            public void onDestroy() {
//                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public void wifi(View view) {
        String t_wifi = "{\n" +
                "\t\"cnf\": {\n" +
                "\t\t\"dgfly\": {\n" +
                "\t\t\t\"appid\": \"\",\n" +
                "\t\t\t\"apk\": \"\",\n" +
                "\t\t\t\"adtype\": \"bb_appstore\",\n" +
                "\t\t\t\"name\": \"\",\n" +
                "\t\t\t\"show_type\": \"bb_wifi_appstore\",\n" +
                "\t\t\t\"icon_img\": \"\",\n" +
                "\t\t\t\"ad_img\": [],\n" +
                "\t\t\t\"desc\": \"共产党万岁万岁万万岁\",\n" +
                "\t\t\t\"w\": 1280,\n" +
                "\t\t\t\"h\": 720,\n" +
                "\t\t\t\"s_dur\": 60000,\n" +
                "\t\t\t\"down_url\": \"\",\n" +
                "\t\t\t\"dplnk\": \"\",\n" +
                "\t\t\t\"rtp\": false,\n" +
                "\t\t\t\"rtp1\": false,\n" +
                "\t\t\t\"ia\": 3,\n" +
                "\t\t\t\"s_rpt\": [\"http:\\/\\/139.196.171.67:5678\\/dgfly_rpt.php?act=s&ad=maapi&adid=aa02c351&cp=AA506&did=865341030555223&aid=a145468c9f115a51\"],\n" +
                "\t\t\t\"c_rpt\": [],\n" +
                "\t\t\t\"d_rpt\": [],\n" +
                "\t\t\t\"dc_rpt\": [],\n" +
                "\t\t\t\"market\": \"\",\n" +
                "\t\t\t\"i_rpt\": [],\n" +
                "\t\t\t\"a_rpt\": [],\n" +
                "\t\t\t\"o_rpt\": [],\n" +
                "\t\t\t\"ad_pack\": \"\",\n" +
                "\t\t\t\"ad_ver\": \"\",\n" +
                "\t\t\t\"vsb\": true,\n" +
                "\t\t\t\"dlsign\": true,\n" +
                "\t\t\t\"logo\": true,\n" +
                "\t\t\t\"is_act\": \"1\",\n" +
                "\t\t\t\"downcount\": \"\",\n" +
                "\t\t\t\"versioncode\": \"\",\n" +
                "\t\t\t\"versionname\": \"\",\n" +
                "\t\t\t\"desc\": \"\",\n" +
                "\t\t\t\"ci\": \"0\",\n" +
                "\t\t\t\"in_broser\": false,\n" +
                "\t\t\t\"cl\": 1,\n" +
                "\t\t\t\"bb_area\": false,\n" +
                "\t\t\t\"ss_delay\": 10000\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        String wifiTest = "{\"cnf\":{\"dgfly\":{\"adtype\":\"bb_appstore\",\"name\":\"\",\"show_type\":\"bb_wifi_appstore\",\"icon_img\":\"\",\"ad_img\":[],\"desc\":\"共产党万岁万岁万万岁\",\"w\":1280,\"h\":720,\"s_dur\":60000,\"down_url\":\"\",\"dplnk\":\"\",\"rtp\":false,\"rtp1\":false,\"ia\":3,\"s_rpt\":[\"http:\\/\\/139.196.171.67:5678\\/dgfly_rpt.php?act=s&ad=maapi&adid=aa02c351&cp=AA506&did=865341030555223&aid=a145468c9f115a51\"],\"c_rpt\":[],\"d_rpt\":[],\"dc_rpt\":[],\"i_rpt\":[],\"a_rpt\":[],\"o_rpt\":[],\"ad_pack\":\"\",\"ad_ver\":\"\",\"vsb\":true,\"dlsign\":true,\"logo\":true,\"is_act\":\"1\",\"ci\":\"0\",\"in_broser\":false,\"cl\":1,\"bb_area\":false,\"ss_delay\":100000}}}";

        AppStoreHandler.getInstance().show(getApplicationContext(), t_wifi, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void icon(View view) {
    }

    public void banner(View view) {
//        AppStoreHandler.getInstance().show(getApplicationContext(), testUP, new AppStoreHandler.OnStoreDestroyListener() {
//            @Override
//            public void onDestroy() {
//                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public void dialog(View view) {
//        WindowHandler.getInstance().showDialog(null);
    }

    public void recommend(View view) {
        String wash = "{\"cnf\":{\"dgfly\":{\"adtype\":\"bb_appstore\",\"name\":\"\",\"show_type\":\"bb_wash_appstore\",\"icon_img\":\"\",\"ad_img\":[],\"desc\":\"共产党万岁万岁万万岁\",\"w\":1280,\"h\":720,\"s_dur\":60000,\"down_url\":\"\",\"dplnk\":\"\",\"rtp\":false,\"rtp1\":false,\"ia\":3,\"s_rpt\":[\"http:\\/\\/139.196.171.67:5678\\/dgfly_rpt.php?act=s&ad=maapi&adid=aa02c351&cp=AA506&did=865341030555223&aid=a145468c9f115a51\"],\"c_rpt\":[],\"d_rpt\":[],\"dc_rpt\":[],\"i_rpt\":[],\"a_rpt\":[],\"o_rpt\":[],\"ad_pack\":\"\",\"ad_ver\":\"\",\"vsb\":true,\"dlsign\":true,\"logo\":true,\"is_act\":\"1\",\"ci\":\"0\",\"in_broser\":false,\"cl\":1,\"bb_area\":false,\"ss_delay\":10000}}}";

        AppStoreHandler.getInstance().show(getApplicationContext(), wash, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
                Log.d("AA", "AAA...");
            }
        });
    }

    public void upDate(View view) {
//        WindowHandler.getInstance().showStoreUpdateList(null);
    }

    public void down(View view) {
        String down = "{\n" +
                "\t\"cnf\": {\n" +
                "\t\t\"dgfly\": {\n" +
                "\t\t\t\"adtype\": \"bb_appstore\",\n" +
                "\t\t\t\"name\": \"\",\n" +
                "\t\t\t\"show_type\": \"bb_update_appstore\",\n" +
                "\t\t\t\"icon_img\": \"\",\n" +
                "\t\t\t\"ad_img\": [],\n" +
                "\t\t\t\"desc\": \"共产党万岁万岁万万岁\",\n" +
                "\t\t\t\"w\": 1280,\n" +
                "\t\t\t\"h\": 720,\n" +
                "\t\t\t\"s_dur\": 60000,\n" +
                "\t\t\t\"down_url\": \"\",\n" +
                "\t\t\t\"dplnk\": \"\",\n" +
                "\t\t\t\"rtp\": false,\n" +
                "\t\t\t\"rtp1\": false,\n" +
                "\t\t\t\"ia\": 3,\n" +
                "\t\t\t\"s_rpt\": [\"http:\\/\\/139.196.171.67:5678\\/dgfly_rpt.php?act=s&ad=maapi&adid=aa02c351&cp=AA070&did=865341030555223&aid=a145468c9f115a51\"],\n" +
                "\t\t\t\"c_rpt\": [],\n" +
                "\t\t\t\"d_rpt\": [],\n" +
                "\t\t\t\"dc_rpt\": [],\n" +
                "\t\t\t\"i_rpt\": [],\n" +
                "\t\t\t\"a_rpt\": [],\n" +
                "\t\t\t\"o_rpt\": [],\n" +
                "\t\t\t\"ad_pack\": \"\",\n" +
                "\t\t\t\"ad_ver\": \"\",\n" +
                "\t\t\t\"vsb\": true,\n" +
                "\t\t\t\"dlsign\": true,\n" +
                "\t\t\t\"logo\": true,\n" +
                "\t\t\t\"is_act\": \"1\",\n" +
                "\t\t\t\"ci\": \"1\",\n" +
                "\t\t\t\"in_broser\": false,\n" +
                "\t\t\t\"cl\": 1,\n" +
                "\t\t\t\"market\": \"Maapi\",\n" +
                "\t\t\t\"bb_area\": false,\n" +
                "\t\t\t\"ss_delay\": 10000\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        AppStoreHandler.getInstance().show(getApplicationContext(), down, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void bottom(View view) {
        String dgfly = "{\n" +
                "\t\"cnf\": {\n" +
                "\t\t\"dgfly\": {\n" +
                "\t\t\t\"adtype\": \"bb_appstore\",\n" +
                "\t\t\t\"name\": \"\",\n" +
                "\t\t\t\"show_type\": \"bb_pullup_appstore\",\n" +
                "\t\t\t\"market\": \"Maapi\",\n" +
                "\t\t\t\"icon_img\": \"\",\n" +
                "\t\t\t\"ad_img\": [],\n" +
                "\t\t\t\"desc\": \"共产党万岁万岁万万岁\",\n" +
                "\t\t\t\"w\": 1280,\n" +
                "\t\t\t\"h\": 720,\n" +
                "\t\t\t\"s_dur\": 60000,\n" +
                "\t\t\t\"down_url\": \"\",\n" +
                "\t\t\t\"dplnk\": \"\",\n" +
                "\t\t\t\"rtp\": false,\n" +
                "\t\t\t\"rtp1\": false,\n" +
                "\t\t\t\"ia\": 3,\n" +
                "\t\t\t\"s_rpt\": [],\n" +
                "\t\t\t\"c_rpt\": [],\n" +
                "\t\t\t\"d_rpt\": [],\n" +
                "\t\t\t\"dc_rpt\": [],\n" +
                "\t\t\t\"i_rpt\": [],\n" +
                "\t\t\t\"a_rpt\": [],\n" +
                "\t\t\t\"o_rpt\": [],\n" +
                "\t\t\t\"ad_pack\": \"\",\n" +
                "\t\t\t\"ad_ver\": \"\",\n" +
                "\t\t\t\"vsb\": true,\n" +
                "\t\t\t\"dlsign\": true,\n" +
                "\t\t\t\"logo\": true,\n" +
                "\t\t\t\"is_act\": \"1\",\n" +
                "\t\t\t\"ci\": \"0\",\n" +
                "\t\t\t\"in_broser\": false,\n" +
                "\t\t\t\"cl\": 1,\n" +
                "\t\t\t\"bb_area\": false,\n" +
                "\t\t\t\"ss_delay\": 10000\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void secondInstall(View view) {
        String dgfly = "{\n" +
                "\t\"cnf\": {\n" +
                "\t\t\"dgfly\": {\n" +
                "\t\t\t\"adtype\": \"bb_appstore\",\n" +
                "\t\t\t\"name\": \"天天快报\",\n" +
                "\t\t\t\"show_type\": \"bb_electricity_appstore\",\n" +
                "\t\t\t\"icon_img\": \"\",\n" +
                "\t\t\t\"ad_img\": [],\n" +
                "\t\t\t\"desc\": \"请尽快充电呀|畅玩无忧呀|肆不忌惮呀\",\n" +
                "\t\t\t\"w\": 1280,\n" +
                "\t\t\t\"h\": 720,\n" +
                "\t\t\t\"s_dur\": 60000,\n" +
                "\t\t\t\"down_url\": \"\",\n" +
                "\t\t\t\"dplnk\": \"\",\n" +
                "\t\t\t\"rtp\": false,\n" +
                "\t\t\t\"rtp1\": false,\n" +
                "\t\t\t\"ia\": 3,\n" +
                "\t\t\t\"s_rpt\": [],\n" +
                "\t\t\t\"c_rpt\": [],\n" +
                "\t\t\t\"d_rpt\": [],\n" +
                "\t\t\t\"dc_rpt\": [],\n" +
                "\t\t\t\"i_rpt\": [],\n" +
                "\t\t\t\"a_rpt\": [],\n" +
                "\t\t\t\"o_rpt\": [],\n" +
                "\t\t\t\"ad_pack\": \"\",\n" +
                "\t\t\t\"ad_ver\": \"\",\n" +
                "\t\t\t\"vsb\": true,\n" +
                "\t\t\t\"dlsign\": true,\n" +
                "\t\t\t\"logo\": true,\n" +
                "\t\t\t\"market\": \"ceshi\",\n" +
                "\t\t\t\"is_act\": \"1\",\n" +
                "\t\t\t\"ci\": \"0\",\n" +
                "\t\t\t\"in_broser\": false,\n" +
                "\t\t\t\"cl\": 0,\n" +
                "\t\t\t\"bb_area\": false,\n" +
                "\t\t\t\"ss_delay\": 10000\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void power(View view) {
//  String str=      "{\"cnf":{"dgfly":{"adtype":"bb_appstore","name":"","show_type":"bb_carousel_appstore","icon_img":"","ad_img":[],"desc":"","w":1280,"h":720,"s_dur":50000,"down_url":"","dplnk":"","rtp":false,"rtp1":false,"ia":3,"s_rpt":["http:\/\/101.132.170.187:7701\/dgfly_rpt.php?reqid=1538203701.986899.c980b&act=s&ad=maapi&adid=5929432b&cp=AA518&did=865122031741024&aid=dd296732460a1ac0"],"c_rpt":[],"d_rpt":[],"dc_rpt":[],"i_rpt":[],"a_rpt":[],"o_rpt":[],"ad_pack":"banner","ad_ver":"","ss_delay":50000,"vsb":true,"dlsign":true,"logo":true,"is_act":"1","ci":"1","in_broser":false,"cl":1,"bb_area":false,"aicnf":[]}}};
        String dgfly = "{\n" +
                "\t\"cnf\": {\n" +
                "\t\t\"dgfly\": {\n" +
                "\t\t\t\"adtype\": \"bb_appstore\",\n" +
                "\t\t\t\"name\": \"QQ\",\n" +
                "\t\t\t\"show_type\": \"bb_carousel_appstore\",\n" +
                "\t\t\t\"icon_img\": \"\",\n" +
                "\t\t\t\"ad_img\": [],\n" +
                "\t\t\t\"desc\": \"请尽快充电呀|畅玩无忧呀|肆不忌惮呀\",\n" +
                "\t\t\t\"w\": 1280,\n" +
                "\t\t\t\"h\": 720,\n" +
                "\t\t\t\"s_dur\": 60000,\n" +
                "\t\t\t\"down_url\": \"\",\n" +
                "\t\t\t\"dplnk\": \"\",\n" +
                "\t\t\t\"rtp\": false,\n" +
                "\t\t\t\"rtp1\": false,\n" +
                "\t\t\t\"ia\": 3,\n" +
                "\t\t\t\"s_rpt\": [],\n" +
                "\t\t\t\"c_rpt\": [],\n" +
                "\t\t\t\"d_rpt\": [],\n" +
                "\t\t\t\"dc_rpt\": [],\n" +
                "\t\t\t\"i_rpt\": [],\n" +
                "\t\t\t\"a_rpt\": [],\n" +
                "\t\t\t\"o_rpt\": [],\n" +
                "\t\t\t\"ad_pack\": \"\",\n" +
                "\t\t\t\"ad_ver\": \"\",\n" +
                "\t\t\t\"vsb\": true,\n" +
                "\t\t\t\"dlsign\": true,\n" +
                "\t\t\t\"logo\": true,\n" +
                "\t\t\t\"is_act\": \"1\",\n" +
                "\t\t\t\"ci\": \"1\",\n" +
                "\t\t\t\"in_broser\": false,\n" +
                "\t\t\t\"cl\": 1,\n" +
                "\t\t\t\"bb_area\": false,\n" +
                "\t\t\t\"market\": \"Maapi\",\n" +
                "\t\t\t\"ss_delay\": 10000\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void testinstall(View v) {
        File f = DownloadLoopAndInstall.getApkFile("赶集网");
        ApkUtils.blueInstall(MainActivity.this, f, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean b = Settings.canDrawOverlays(MainActivity.this);
            Log.d("ldl", "onResume" + b);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean b = Settings.canDrawOverlays(MainActivity.this);
            Log.d("ldl", "onStart" + b);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean b = Settings.canDrawOverlays(MainActivity.this);
            Log.d("ldl", "onStop" + b);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean b = Settings.canDrawOverlays(MainActivity.this);
            Log.d("ldl", "onPause" + b);
        }
    }
}
*/
