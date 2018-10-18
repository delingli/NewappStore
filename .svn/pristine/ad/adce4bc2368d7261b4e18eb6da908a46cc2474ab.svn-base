package com.bbx.appstore.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.bbx.appstore.base.SConstant.DB_NAME;
import static com.bbx.appstore.base.SConstant.TABLE_DM.CREATE_SQL;
import static com.bbx.appstore.base.SConstant.VERSION;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static MySQLiteHelper mInstance = null;
    private static final Object LOCK = new Object();

    private MySQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static MySQLiteHelper getInstance(Context context) {
        synchronized (LOCK) {
            if (mInstance == null) {
                synchronized (LOCK) {
                    mInstance = new MySQLiteHelper(context);
                    return mInstance;
                }
            }
            return mInstance;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.delete()
//        db.execSQL();
    }
}
