package com.bbx.appstore.bean;

public class AppUpdateLocalApp {

    public AppUpdateLocalApp(String pkgName, int versionCode) {
        this.pn = pkgName;
        this.vc = versionCode;
    }

    public String pn;
    public int vc;


    @Override
    public String toString() {
        return "AppUpdateLocalApp{" +
                "pn='" + pn + '\'' +
                ", vc=" + vc +
                '}';
    }
}
