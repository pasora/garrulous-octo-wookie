package jp.gr.java_conf.pasora.orf2015;

import java.util.Date;

/**
 * Created by masahikohara on 2015/11/05.
 */
public class SuicaLog {
    private byte[] suicaLogBin;
    private Date date;
    private String enter;
    private String exit;
    private String latitude;
    private String longtitude;

    public SuicaLog(byte[] suicaLogStr) {
        this.suicaLogBin = suicaLogStr;

    }

    void setDate() {
        int year = this.suicaLogBin & 0x1F;
        int month;
        int day;

    }
}
