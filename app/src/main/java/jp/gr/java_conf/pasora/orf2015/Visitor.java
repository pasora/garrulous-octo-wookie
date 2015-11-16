package jp.gr.java_conf.pasora.orf2015;

import android.util.Log;

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
    private String startLineName;
    private String startStationName;
    private String startLatitude;
    private String startLongitude;

    private String destSection;
    private String destStation;
    private String destLineName;
    private String destStationName;
    private String destLatitude;
    private String destLongitude;

    private String region;

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
        /*
        if (lastSuicaLog.getDay() != this.day)
            return false;
        if (lastSuicaLog.getMonth() != this.month)
            return false;
        if (lastSuicaLog.getYear() != this.year)
            return false;
        */
        return true;
    }

    public boolean fixStationData() {
        //if (!isLatestLogToday()) return false;
        this.destSection = suicaLogArrayList.get(0).getExitSection();
        this.destStation = suicaLogArrayList.get(0).getExitStation();
        //駅名設定
        //緯度経度設定
        this.startSection = suicaLogArrayList.get(suicaLogArrayList.size() - 1).getEnterSection();
        this.startStation = suicaLogArrayList.get(suicaLogArrayList.size() - 1).getEnterStation();
        //駅名設定
        //緯度経度設定
        this.region = suicaLogArrayList.get(0).getRegion();

        return true;
    }

    public void setStationName(StationDatabase sd) {
        Log.d("startSection", startSection);
        Log.d("startStation", startStation);
        int start = sd.getLineNumber(region, startSection, startStation);
        Log.d("destSection", destSection);
        Log.d("destStation", destStation);
        int dest = sd.getLineNumber(region, destSection, destStation);

        this.startLineName = sd.getLineName(start);
        this.startStationName = sd.getStationName(start);
        this.startLatitude = sd.getLatitude(start);
        this.startLongitude = sd.getLongitude(start);

        this.destLineName = sd.getLineName(dest);
        this.destStationName = sd.getStationName(dest);
        this.destLatitude = sd.getLatitude(dest);
        this.destLongitude = sd.getLongitude(dest);
    }

    public String getStartStationName() {
        return startStationName;
    }

    public String getDestStationName() {
        return destStationName;
    }

    public String getDestLatitude() {
        return destLatitude;
    }

    public String getDestLongitude() {
        return destLongitude;
    }

    public String getStartLatitude() {
        return startLatitude;
    }

    public String getStartLongitude() {
        return startLongitude;
    }

    public String getStartLineName() {
        return startLineName;
    }

    public String getDestLineName() {
        return destLineName;
    }
}
