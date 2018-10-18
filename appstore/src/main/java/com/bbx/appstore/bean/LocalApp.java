package com.bbx.appstore.bean;

public class LocalApp {

    public LocalApp(String pkgName, int versionCode) {
        this.pn = pkgName;
        this.vc = versionCode;
    }

    public String pn;
    public int vc;

    @Override
    public String toString() {
        return "LocalApp{" +
                "pkgName='" + pn + '\'' +
                ", vc=" + vc +
                '}';
    }
}
