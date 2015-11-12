package jp.gr.java_conf.pasora.orf2015;

import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by masahikohara on 2015/11/05.
 */

public class SuicaLog {
    private byte[] suicaLogBin;
    private Calendar date;
    private String enter;
    private String exit;
    private String latitude;
    private String longtitude;

    public SuicaLog(byte[] suicaLogStr) {
        this.suicaLogBin = suicaLogStr;
        setDate();

    }

    boolean isToday() {
        Calendar now = Calendar.getInstance();
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(now.getTime());
        String dateStr = new java.text.SimpleDateFormat("yyy-MM-dd", Locale.JAPAN).format(date.getTime());
        return nowStr.equals(dateStr);
    }

    void setDate() {
        int year = (this.suicaLogBin[4] >> 1) + 2000;
        int month = ((this.suicaLogBin[4] & (byte)0x01) << 3) + ((this.suicaLogBin[5] & (byte)0xE0) >> 5);
        int day = this.suicaLogBin[5] & (byte) 0x1F;
        this.date.set(year, month, day);
        Log.d("date_year", String.valueOf(year));
        Log.d("date_month", String.valueOf(month));
        Log.d("date_day", String.valueOf(day));
    }
}
