package com.bbx.appstore.storeutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class DateUtils {

    private static final String TAG = "mDateUtils";
    private static DateUtils mDateUtils;

    private static SharedPreferences mSp;

    private DateUtils() {

    }

    public static DateUtils getInstance(Context context) {
        if (null == mDateUtils) {
            synchronized (DateUtils.class) {
                if (null == mDateUtils) {
                    mDateUtils = new DateUtils();
                    mSp = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                }
            }
        }
        return mDateUtils;
    }

    /**
     * 是否是同一天
     *
     * @return 0 : 同一天
     */
    public int differentDay() {
        Date oldDate = new Date(mSp.getLong("save_time", 0));
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(oldDate);
        int oldYear = calendar.get(YEAR);
        int oldMonth = calendar.get(MONTH) + 1;
        int oldDay = calendar.get(DAY_OF_MONTH);
        Log.e(TAG, "oldDay calendar " + calendar.get(YEAR) + "-" + (calendar.get(MONTH) + 1) + "-" + calendar.get(DAY_OF_MONTH));

        calendar.setTime(new Date());
        int nowYear = calendar.get(YEAR);
        int nowMonth = calendar.get(MONTH) + 1;
        int nowDay = calendar.get(DAY_OF_MONTH);
        Log.e(TAG, "nowDay calendar " + calendar.get(YEAR) + "-" + (calendar.get(MONTH) + 1) + "-" + calendar.get(DAY_OF_MONTH));
        int year = nowYear - oldYear;
        if (year == 0) {
            int month = nowMonth - oldMonth;
            if (month == 0) {
                return nowDay - oldDay;
            }
            return month;
        }
        return year;
    }

    public void saveCurrentTime() {
        long time = System.currentTimeMillis();
        mSp.edit().putLong("save_time", time).apply();
    }
}
