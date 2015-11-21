package jp.gr.java_conf.pasora.orf2015;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by masahikohara on 2015/11/16.
 */
public class Visitor {
    private Date date;

    private String startSection = "";
    private String startStation = "";
    private String startLineName = "";
    private String startStationName = "";
    private String startLatitude = "";
    private String startLongitude = "";

    private String destSection = "";
    private String destStation = "";
    private String destLineName = "";
    private String destStationName = "";
    private String destLatitude = "";
    private String destLongitude = "";

    private String region = "";

    private ArrayList<SuicaLog> suicaLogArrayList = new ArrayList<SuicaLog>();

    public Visitor() {
        this.date = new Date();
    }

    public void importSuicaLog(SuicaLog suicaLog) {
        suicaLogArrayList.add(suicaLog);
    }

    public boolean fixStationData() {
        int i = 0;
        boolean dest = false;
        boolean start = false;
        for (; i < suicaLogArrayList.size(); i++) {
            SuicaLog suicaLogTemp = suicaLogArrayList.get(i);
            Log.d("fixStationData dest", Integer.toString(i));
            if (suicaLogTemp.isTrain()&& suicaLogTemp.isToday()) {
                this.destSection = suicaLogTemp.getExitSection();
                this.destStation = suicaLogTemp.getExitStation();
                Log.d("fixStationData deststa", this.destStation);
                dest = true;
                break;
            }
        }

        for (int j = suicaLogArrayList.size() - 1; j >= i; j--) {
            SuicaLog suicaLogTemp = suicaLogArrayList.get(j);
            Log.d("fixStationData start", Integer.toString(j));
            if (suicaLogTemp.isTrain()&& suicaLogTemp.isToday()) {
                this.startSection = suicaLogTemp.getEnterSection();
                this.startStation = suicaLogTemp.getEnterStation();
                Log.d("fixStationData startsta", this.startStation);
                start = true;
                break;
            }
        }

        this.region = suicaLogArrayList.get(0).getRegion();

        return dest && start;
    }

    public void setStationName(StationDatabase sd) {
        int start = sd.getLineNumber(region, startSection, startStation);
        if (start == -1) {
            this.startLineName = this.startStationName = "不明";
        } else {
            this.startLineName = sd.getLineName(start);
            this.startStationName = sd.getStationName(start);
            this.startLatitude = sd.getLatitude(start);
            this.startLongitude = sd.getLongitude(start);
        }

        int dest = sd.getLineNumber(region, destSection, destStation);
        if (dest == -1) {
            this.destLineName = this.destStationName = "不明";
        } else {
            this.destLineName = sd.getLineName(dest);
            this.destStationName = sd.getStationName(dest);
            this.destLatitude = sd.getLatitude(dest);
            this.destLongitude = sd.getLongitude(dest);
        }
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

    public Date getDate() {
        return date;
    }
}
