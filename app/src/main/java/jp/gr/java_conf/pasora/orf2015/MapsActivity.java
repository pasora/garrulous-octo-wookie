package jp.gr.java_conf.pasora.orf2015;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

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

    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                logTextView.setText((String) message.obj);
            }
        };
    }

    @Override
    public void onDiscovered(final Visitor visitor) {
        if (visitor.fixStationData()) {
            visitor.setStationName(stationDatabase);
            Log.d("start line", visitor.getStartLineName());
            Log.d("start", visitor.getStartStationName());
            Log.d("startLat", visitor.getStartLatitude());
            Log.d("startLng", visitor.getStartLongitude());
            Log.d("dest line", visitor.getDestLineName());
            Log.d("dest", visitor.getDestStationName());
            Log.d("destLat", visitor.getDestLatitude());
            Log.d("destLng", visitor.getDestLongitude());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = Message.obtain();
                    message.obj = String.format("%s 線 %s 駅 から %s 線 %s 駅",
                            visitor.getStartLineName(),
                            visitor.getStartStationName(),
                            visitor.getDestLineName(),
                            visitor.getDestStationName());
                    handler.handleMessage(message);
                }
            });
            saveLog(visitor);
        }  else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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

    @Override
    public void onError(Exception exception) {
        logTextView.setText("カードを認識できません。");
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
        // Add a marker in Sydney and move the camera
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
