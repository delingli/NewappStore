package com.bbx.appstore.base;

public interface SConstant {

    /*=============================================SQLite=============================================*/

    String DB_NAME = "cart_store.db";
    int VERSION = 1;

    interface TABLE_DM {
        String NAME = "dm_store";
        String DM_ID = "_id";
        String DM_DID = "app_id"; //appId
        String DM_NAME = "app_name"; //应用名
        String DM_PACKAGE = "pkg_name"; //包名
        String DM_VERSIONCODE = "v_code"; //版本code
        String DM_SIZE = "size"; //大小
        String DM_ICON_URL = "icon_url";
        String DM_DOWN_URL = "down_url";
        String DM_DOWN_ED = "down_ed"; //下载完成上报
        String DM_REP_INSTALL = "install"; //安装上报
        String DM_REP_AC = "ac"; //激活上报
        String DM_REP_DEL = "del"; //删除上报
        String DM_REP_MET = "method"; //上报方式

        String CREATE_SQL = "create table " + NAME + " ("// 建表
                + DM_ID + " integer primary key autoincrement,"// _id
                + DM_DID + " varchar unique , "
                + DM_NAME + " varchar , "
                + DM_PACKAGE + " varchar , "
                + DM_VERSIONCODE + " varchar , "
                + DM_SIZE + " varchar , "
                + DM_ICON_URL + " varchar , "
                + DM_DOWN_URL + " varchar , "
                + DM_DOWN_ED + " varchar , "
                + DM_REP_INSTALL + " varchar , "
                + DM_REP_AC + " varchar , "
                + DM_REP_DEL + " varchar , "
                + DM_REP_MET + " varchar"
                + ")";
    }

    /*==============================================Bundle key========================================*/

    String PKG_NAME = "packageName";
    String APP_NAME = "appName";
    String DETAIL_NOTIFY = "detail_notify";
    String DETAIL_ELSE = "detail_else";
    String LIST_MODE = "list_mode";
    String DETAIL_MODE = "detail_mode";
    String UPDATE_LIST = "update_list";
    String RECOMMEND_LIST = "recommend_list";

    /*========================================网络请求常量==============================================*/

    //    String MARKET = "http://172.18.0.19/cyan/tang/trunk/api/src/market.php"; //测试接口
    String MARKET = "http://adapi.yiticm.com:7701/market.php"; //正式接口
    String TYPE = "?type=";
    String TYPE_CATEGORY = "category"; //获取分类/栏目
    String TYPE_LIST = "list"; //获取列表
    String TYPE_RECOMMEND_AD = "recommendad"; //洗包
    String TYPE_UPDATE = "update";
    String TYPE_WIFI = "wifi";
    String TYPE_SEARCH = "search";

    String APP_LIST = "applist"; //洗包列表 eg:type=?type=recommendad&applist=com.wuba,com.rasoft.bubble
    String APK_LIST = "apklist"; //更新

    String CID = "&cid=";
    int CID_APP_LIST = -1; // 应用列表
    int CID_HOT = -2; //热门应用
    int CID_WIFI = -4; //wifi ad
    int CID_BOTTOM = -7;
    int CID_POWER_IN = -8;
    int CID_POWER_OUT = -9;
    int CID_GUESS = -10;

    int CID_INSERT = -20;
    int CID_NEW_POWER_IN = -18;
    int CID_NEW_POWER_OUT = -19;
    int CID_NEW_BOTTOM = -17;
    int CID_NEW_RECOMMEND = -16;

    int CID_BATTERY_TIPS = -24;
    int CID_SEARCH = -25;
    int CID_SEARCH_RECOMMEND = -26;

    String T_MODE = "&tmode=";
    String T_MODE_WIFI = "wifi"; //wifi直接下载
    String T_MODE_WIFI2 = "wifi2"; //wifi进入列表和详情
    String T_MODE_NOTIFY = "notify";
    String T_MODE_ICON = "icon";

    String PAGE = "&page=";
    String PAGE_SIZE = "&pagesize=";

    /*==============================================SP KEY============================================*/

    String NOTIFY = "notify";

    /*================================================================================================*/

    String AD_TYPE = "bb_appstore";
    String SHOW_TYPE_NOTIFY = "bb_notify_appstore";
    String SHOW_TYPE_WIFI = "bb_wifi_appstore";
    String SHOW_TYPE_BANNER = "bb_update_appstore";
    String SHOW_TYPE_RECOMMEND = "bb_wash_appstore";
    String SHOW_TYPE_DOWN_STORE = "bb_install_appstore";
    String SHOW_TYPE_BOTTOM = "bb_pullup_appstore";
    String SHOW_TYPE_POWER_IN = "bb_chargein_appstore";
    String SHOW_TYPE_POWER_OUT = "bb_chargeout_appstore";
    String SHOW_TYPE_BATTERT_TIPS = "bb_electricity_appstore";

    /*==============================new ad======================================*/

    String SHOW_TYPE_NEW_RECOMMEND = "bb_wash1_appstore";
    String SHOW_TYPE_NEW_BOTTOM = "bb_pullup1_appstore";
    String SHOW_TYPE_NEW_POWER_IN = "bb_chargein1_appstore";
    String SHOW_TYPE_NEW_POWER_OUT = "bb_chargeout1_appstore";
    String SHOW_TYPE_INSERT = "bb_carousel_appstore";

    /*----------------sp------------------*/

    String SP_PKG_KEY = "dynamic_store";
    String SP_APP_NAME = "dynamic_name";
    String SP_DC_RPT = "dynamic_dc_rpt"; //下载完成
    String SP_I_RPT = "dynamic_i_rpt"; //安装成功
    String SP_A_RPT = "dynamic_a_rpt"; //激活
    String TAG = "ldl";
}
