package jp.gr.java_conf.pasora.orf2015;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MapsActivity
        extends FragmentActivity
        implements OnMapReadyCallback, CardReader.CardReaderListener  {

    private CardReader mCardReader;
    private LogDatabaseHelper logDatabaseHelper;
    private final ThreadLocal<GoogleMap> mMap = new ThreadLocal<>();
    StationDatabase stationDatabase;
    TextView logTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        int mUIFlag =
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getMap().moveCamera(CameraUpdateFactory.zoomTo(10));

        logTextView = (TextView)this.findViewById(R.id.showLogData);

        logDatabaseHelper = new LogDatabaseHelper(this);

        mCardReader = new CardReader(this, this);

        if (!mCardReader.isNfcInstalled()) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_nfc_nosupport), Toast.LENGTH_LONG).show();
            return;
        }
        if (!mCardReader.isNfcEnabled()) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_nfc_disable), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }

        mCardReader.enable();

        stationDatabase = new StationDatabase(this);
    }

    @Override
    public void onDiscovered(Visitor visitor) {
        if (visitor.fixStationData()) {
            (new visitorTask(visitor)).execute();
        } else {
            (new logErrorTask()).execute();
        }
    }

    @Override
    public void onError(Exception exception) {

    }

    public class visitorTask extends AsyncTask<Visitor, Void, String> {
        private Visitor visitor;

        public visitorTask(Visitor visitor) {
            super();
            this.visitor = visitor;
        }

        @Override
        protected String doInBackground(Visitor ... params) {
            visitor.setStationName(stationDatabase);
            Log.d("start line", visitor.getStartLineName());
            Log.d("start", visitor.getStartStationName());
            Log.d("startLat", visitor.getStartLatitude());
            Log.d("startLng", visitor.getStartLongitude());
            Log.d("dest line", visitor.getDestLineName());
            Log.d("dest", visitor.getDestStationName());
            Log.d("destLat", visitor.getDestLatitude());
            Log.d("destLng", visitor.getDestLongitude());
            saveLog(visitor);
            return String.format("%s線 %s駅 から %s線 %s駅",
                    visitor.getStartLineName(),
                    visitor.getStartStationName(),
                    visitor.getDestLineName(),
                    visitor.getDestStationName());
        }

        @Override
        protected void onPostExecute(String result) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMap()
                    .addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(visitor.getStartLatitude()), Double.parseDouble(visitor.getStartLongitude())))
                            .title(visitor.getStartStationName()));
            mapFragment.getMap()
                    .addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(visitor.getDestLatitude()), Double.parseDouble(visitor.getDestLongitude())))
                            .title(visitor.getDestStationName()));
            logTextView.setText(result);

        }
    }

    public class logErrorTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            Log.d("logErrorTask", "doInBackground");
            return null;
        }

        @Override
        protected void onPostExecute(Object nothing) {
            Log.d("logErrorTask", "onPostExecute");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
            alertDialogBuilder
                    .setTitle("履歴参照エラー")
                    .setMessage("認識できる鉄道利用情報がありません。")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO 地図クリア
                                }
                            })
                    .create();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap.set(googleMap);
        LatLng tokyoMidtown = new LatLng(35.6655766,139.7304511);
        mMap.get().addMarker(new MarkerOptions().position(tokyoMidtown).title("here!"));
        mMap.get().moveCamera(CameraUpdateFactory.newLatLng(tokyoMidtown));
    }

    @Override
    public void onPause() {
        super.onPause();
        mCardReader.disable();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCardReader.enable();
    }

    public void saveLog(Visitor visitor) {
        SQLiteDatabase db = logDatabaseHelper.getWritableDatabase();
        String dateStr = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(visitor.getDate());

        ContentValues values = new ContentValues();
        values.put(LogDatabaseHelper.COLUMN_DATE, dateStr);
        values.put(LogDatabaseHelper.COLUMN_START, visitor.getStartStationName());
        values.put(LogDatabaseHelper.COLUMN_DESTINATION, visitor.getDestStationName());
        values.put(LogDatabaseHelper.COLUMN_START_LATITUDE, visitor.getStartLatitude());
        values.put(LogDatabaseHelper.COLUMN_START_LONGTITUDE, visitor.getStartLongitude());
        values.put(LogDatabaseHelper.COLUMN_DESTINATION_LATITUDE, visitor.getDestLatitude());
        values.put(LogDatabaseHelper.COLUMN_DESTINATION_LONGTITUDE, visitor.getDestLongitude());
        try {
            db.insert(LogDatabaseHelper.TABLE_LOGRECORD, "not available", values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}
