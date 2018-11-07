package com.bbx.dynamicas;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import com.bbx.appstore.windows.AppStoreHandler;

public class TestActivity extends Activity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        setContentView(R.layout.activity_test);
        AppStoreHandler.getInstance().init(getApplicationContext(), "/sdcard/storeapkres-debug.apk", "com.test.storeapkres");
    }
    public void btn_chinaKonk(View v){
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly, 1,new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void btn_lujiao(View v){
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly,2, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void btn_xueren(View v){
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly,3, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void btn_bolibei(View v){
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly,4, new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void btn_huanguan(View v){
        AppStoreHandler.getInstance().show(getApplicationContext(), dgfly, 5,new AppStoreHandler.OnStoreDestroyListener() {
            @Override
            public void onDestroy() {
                Toast.makeText(getApplicationContext(), "callback", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
