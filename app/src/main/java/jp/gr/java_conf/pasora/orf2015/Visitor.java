package jp.gr.java_conf.pasora.orf2015;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by masahikohara on 2015/11/16.
 */
public class Visitor {
    private Date date;
    private int year;
    private int month;
    private int day;
    private String startSection;
    private String startStation;
    private String startStationName;
    private String startStationLatitude;
    private String startStationLongitude;
    private String destSection;
    private String destStation;
    private String destStationName;
    private String destStaLatitude;
    private String destStaLongitude;
    private ArrayList<SuicaLog> suicaLogArrayList = new ArrayList<SuicaLog>();

    public Visitor() {
        this.date = new Date();
        this.year = date.getYear();
        this.month = date.getMonth();
        this.day = date.getDay();
    }

    public void importSuicaLog(SuicaLog suicaLog) {
        suicaLogArrayList.add(suicaLog);
    }

    public boolean isLatestLogToday() {
        SuicaLog lastSuicaLog = suicaLogArrayList.get(suicaLogArrayList.size() - 1);

        if (lastSuicaLog.getDay() != this.day)
            return false;
        if (lastSuicaLog.getMonth() != this.month)
            return false;
        if (lastSuicaLog.getYear() != this.year)
            return false;

        return true;
    }

    public boolean fixStationData() {
        if (!isLatestLogToday()) return false;
        this.destSection = suicaLogArrayList.get(0).getExitSection();
        this.destStation = suicaLogArrayList.get(0).getExitStation();
        //駅名設定
        //緯度経度設定
        this.startSection = suicaLogArrayList.get(suicaLogArrayList.size() - 1).getEnterSection();
        this.startStation = suicaLogArrayList.get(suicaLogArrayList.size() - 1).getEnterStation();
        //駅名設定
        //緯度経度設定

        return true;
    }

}
