package com.bbx.appstore.bean;

public class LocalApp {

    public LocalApp(String pkgName) {
        this.pn = pkgName;
//        this.vc = versionCode;
    }

    public String pn;
//    public int vc;


    @Override
    public String toString() {
        return "LocalApp{" +
                "pn='" + pn + '\'' +
                '}';
    }
}
