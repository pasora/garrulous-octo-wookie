package jp.gr.java_conf.pasora.orf2015;

import android.util.Log;


/**
 * Created by masahikohara on 2015/11/05.
 */

public class SuicaLog {
    private char[] suicaLogChars;
    private String[] suicaLogStr;
    private int year;
    private int month;
    private int day;
    private String enterSection;
    private String enterStation;
    private String exitSection;
    private String exitStation;
    private String region;
    private String latitude;
    private String longitude;

    final protected static char[] hexArray;
    static {
        hexArray = "0123456789ABCDEF".toCharArray();
    }

    public SuicaLog(byte[] suicaLogBin) {
        this.suicaLogChars = bytesToHexChars(suicaLogBin);
        this.suicaLogStr = charsToStr(suicaLogChars);

        this.year = (Integer.parseInt(suicaLogStr[4], 16) >> 1) + 2000;
        this.month = ((Integer.parseInt(suicaLogStr[4], 16) & 0x01) << 3)
                + ((Integer.parseInt(suicaLogStr[5], 16) & 0xE0) >> 5);
        this.day = (Integer.parseInt(suicaLogStr[5], 16) & 0x1F);

        this.enterSection = suicaLogStr[6];
        this.enterStation = suicaLogStr[7];
        this.exitSection = suicaLogStr[8];
        this.exitStation = suicaLogStr[9];
        this.region = suicaLogStr[15];

        Log.d("nfc", new String(suicaLogChars));
        for (int i = 0; i < suicaLogStr.length; i++) {
            Log.d("suicaLogStr[" + i + "]", String.valueOf(suicaLogStr[i]));
        }
        Log.d("log_year", String.valueOf(year));
        Log.d("log_month", String.valueOf(month));
        Log.d("log_day", String.valueOf(day));

        Log.d("log_enter_section", enterSection);
        Log.d("log_enter_station", enterStation);
        Log.d("log_exit_section", exitSection);
        Log.d("log_exit_station", exitStation);
        Log.d("log_region", region);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getEnterSection() {
        return enterSection;
    }

    public String getEnterStation() {
        return enterStation;
    }

    public String getExitSection() {
        return exitSection;
    }

    public String getExitStation() {
        return exitStation;
    }

    public String getRegion() {
        return region;
    }

    char[] bytesToHexChars(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }

    String[] charsToStr(char[] suicaLogChars) {
        String[] suicaLogStr = new String[suicaLogChars.length / 2];
        for (int i = 0; i < suicaLogStr.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(suicaLogChars[i * 2]);
            sb.append(suicaLogChars[i * 2 + 1]);
            suicaLogStr[i] = new String(sb);
        }
        return suicaLogStr;
    }
}
