package jp.gr.java_conf.pasora.orf2015;

import android.util.Log;
import java.util.Date;


/**
 * Created by masahikohara on 2015/11/05.
 */

public class SuicaLog {
    private char[] suicaLogChars;
    private String[] suicaLogStr;
    private int year;
    private int month;
    private int day;
    private String process;
    private String enterSection;
    private String enterStation;
    private String exitSection;
    private String exitStation;
    private String region;

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

        this.process = suicaLogStr[1];

        this.enterSection = suicaLogStr[6];
        this.enterStation = suicaLogStr[7];
        this.exitSection = suicaLogStr[8];
        this.exitStation = suicaLogStr[9];
        this.region = suicaLogStr[15];

        Log.d("suicaLog", new String(suicaLogChars));
        /*for (int i = 0; i < suicaLogStr.length; i++) {
            Log.d("suicaLogStr[" + i + "]", String.valueOf(suicaLogStr[i]));
        Log.d("log_year", String.valueOf(year));
        Log.d("log_month", String.valueOf(month));
        Log.d("log_day", String.valueOf(day));

        Log.d("log_enter_section", enterSection);
        Log.d("log_enter_station", enterStation);
        Log.d("log_exit_section", exitSection);
        Log.d("log_exit_station", exitStation);
        Log.d("log_region", region);
        */
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

    public String getProcess() {
        return process;
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

    public boolean isTrain() {
        Log.d("process", process);
        if (process.equals("01")) {
            return true;  //運賃支払(改札出場)
        }
        if (process.equals("05")) {
            return true;  //精算(入場精算)
        }
        if (process.equals("06")) {
            return true;  //窓出(改札窓口処理)
        }
        if (process.equals("08")) {
            return true;  //控除(窓口控除)
        }
        if (process.equals("13")) {
            return true;  //新幹線利用
        }
        if (process.equals("84")) {
            return true;  //精算(他社精算)
        }
        if (process.equals("85")) {
            return true;  //精算(他社入場精算)
        }

        return false;
    }

    public boolean isToday() {
        Date date = new Date();
        //TODO remove "-1"
        if (date.getDate() - 1 != this.day)
            return false;
        if (date.getMonth() + 1 != this.month)
            return false;
        if (date.getYear() + 1900 != this.year)
            return false;

        return true;
    }
}
