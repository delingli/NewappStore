# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/sant/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep ICON_OPTIONS here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-repackageclasses 'com.bbx'

#okhttp
#-dontwarn okhttp3.**
#-keep class okhttp3.**{*;}

##okio
-dontwarn okio.**
#-keep class okio.**{*;}

# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
#-keep class com.google.gson.** { *; }
# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.bbx.appstore.bean.**{*;}

-dontwarn com.nostra13.universalimageloader.**
#-keep class com.nostra13.universalimageloader.** { *; }

-dontwarn com.bbx.**
-keep class com.bbx.appstore.windows.AppStoreHandler {
    public <methods>;
}

-keep public class com.bbx.appstore.R$*{
    public static final int *;
}

-keepnames class com.bbx.appstore.windows.AppStoreHandler$* {
    public <fields>;
    public <methods>;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.bbx.HW {
    public <methods>;
}

-keep class com.bbx.support.** {*;}
