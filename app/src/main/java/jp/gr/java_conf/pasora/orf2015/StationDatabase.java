package jp.gr.java_conf.pasora.orf2015;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by masahikohara on 2015/11/15.
 */

/**
 * 0: region code
 * 1: line code
 * 2: station code
 * 3: carrier name
 * 4: line name
 * 5: station name
 * 6: longitude
 * 7: latitude
 * 8: address
 * 9: remark
 */

public class StationDatabase {
    private Context context;
    List<String[]> list = new ArrayList<>();

    public StationDatabase(Context context) {
        this.context = context;
        parse();
        check();
    }

    private void parse() {
        String next[] = {};

        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.station_code)));
            while (true) {
                next = csvReader.readNext();
                if (next != null) {
                    list.add(next);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void check() {
        for (int i = 0; i < list.size(); i++) {
            Log.d("csv:" + i, list.get(i)[5]);
        }
    }

    public int getLineNumber(String region, String area, String station) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)[2].equals(station))
                if (list.get(i)[1].equals(area))
                    if (list.get(i)[0].equals(region))
                        return i;
        }
        return -1;
    }

    public String getLineName(int lineNumber) {
        return list.get(lineNumber)[4];
    }

    public String getStationName(int lineNumber) {
        return list.get(lineNumber)[5];
    }

    public String getLongitude(int lineNumber) {
        return list.get(lineNumber)[6];
    }

    public String getLatitude(int lineNumber) {
        return list.get(lineNumber)[7];
    }
}
