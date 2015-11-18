package jp.gr.java_conf.pasora.orf2015;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by masahikohara on 2015/11/14.
 */
public class LogDatabaseHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "logrecord.db";
    private static final int DBVERSION = 1;
    public static final String TABLE_LOGRECORD = "logrecord";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IDM = "idm";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_START_LINE = "startline";
    public static final String COLUMN_DESTINATION = "destination";
    public static final String COLUMN_DESTINATION_LINE = "destinationline";
    public static final String COLUMN_START_LATITUDE = "startlatitude";
    public static final String COLUMN_START_LONGTITUDE = "startlongtitude";
    public static final String COLUMN_DESTINATION_LATITUDE = "destinationlatitude";
    public static final String COLUMN_DESTINATION_LONGTITUDE = "destinationlongtitude";
    private static final String CREATE_TABLE_SQL =
            "create table " + TABLE_LOGRECORD + " "
                    + "(" + COLUMN_ID + " integer primary key autoincrement,"
                    + COLUMN_IDM + " text,"
                    + COLUMN_DATE + " text not null,"
                    + COLUMN_START + " text not null,"
                    + COLUMN_START_LINE + " text not null,"
                    + COLUMN_DESTINATION + " text not null,"
                    + COLUMN_DESTINATION_LINE + " text not null,"
                    + COLUMN_START_LATITUDE + " text,"
                    + COLUMN_START_LONGTITUDE + " text,"
                    + COLUMN_DESTINATION_LATITUDE + " text,"
                    + COLUMN_DESTINATION_LONGTITUDE + " text)";

    public LogDatabaseHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
