package com.trek.pricer.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trek.pricer.R;
import com.trek.pricer.database.DatabaseHandler;
import com.trek.pricer.database.SavedLineup;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;


public class TrekDetailActivity extends AppCompatActivity {

    private static GoogleMap googleMap;

    private SavedLineup mItem = null;
    private String value_distance, value_avgSpeed, value_maxSpeed, value_duration, value_trekType, value_trekName,
            value_trekDate, value_trekStartTime, value_trekCost, value_trekSaving, value_trekGpxFile;
    List<SavedLineup> treklist;
    RelativeLayout chat_layout;
    LineChartView speedchat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trek_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.trek_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });


        TextView maxSpeed = (TextView) findViewById(R.id.text_trek_max_speed);
        TextView avgSpeed = (TextView) findViewById(R.id.text_trek_avg_speed);
        TextView trekDistance = (TextView) findViewById(R.id.text_trek_distance);
        TextView trekDuration = (TextView) findViewById(R.id.text_trek_duration);
        TextView trekCost = (TextView) findViewById(R.id.edittext_trek_cost);
        TextView trekSaving = (TextView) findViewById(R.id.text_trek_saving);
        chat_layout=(RelativeLayout)findViewById(R.id.speed_chat_layout);
        speedchat=(LineChartView)findViewById(R.id.chart);
        Intent intent = getIntent();
        String trek_id = intent.getStringExtra("id");


        Long id = Long.parseLong(trek_id);
        treklist=new ArrayList<>();
        mItem = DatabaseHandler.getInstance(TrekDetailActivity.this).getTrek(id);
        treklist=DatabaseHandler.getInstance(TrekDetailActivity.this).getAllTrek();
        Log.d("ASDFSFSDFSDFSDF",Integer.toString(treklist.size()));
        value_trekName = mItem.trek_name;
        value_trekType = mItem.trek_mode;
        value_trekDate = mItem.trek_date;
        value_trekStartTime = mItem.trek_start_time;
        value_duration = mItem.trek_duration;
        value_distance = mItem.trek_distance;
        value_maxSpeed = mItem.trek_max_speed;
        value_avgSpeed = mItem.trek_avg_speed;
        value_trekCost = mItem.trek_cost;
        value_trekSaving = mItem.trek_saving;
        value_trekGpxFile = mItem.trek_gpx_file;

        toolbar.setTitle(value_trekName);
        maxSpeed.setText(value_maxSpeed);
        avgSpeed.setText(value_avgSpeed);
        trekDistance.setText(value_distance);
        trekDuration.setText(value_duration);
        trekCost.setText(value_trekCost);
        trekSaving.setText(value_trekSaving);

        drawspeed();

        if (value_trekType.equals("Still") || value_trekType.equals("Unknown") || value_trekType.equals("Walking") ||
                value_trekType.equals("On Foot") || value_trekType.equals("Running") || value_trekType.equals("On Bicycle") || value_trekType.equals("Tilting")) {

            trekCost.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (value_trekType.equals("Driving")) { // Own Vehicle
            trekSaving.setTextColor(Color.parseColor("#C0C0C0"));
        } else { //Shared
            trekSaving.setTextColor(Color.parseColor("#C0C0C0"));
        }


        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.trek_map, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {

                googleMap = map;

                try {
                    MapsInitializer.initialize(TrekDetailActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    googleMap.setMyLocationEnabled(false);
                    googleMap.getUiSettings().setZoomControlsEnabled(false);
                    googleMap.getUiSettings().setZoomGesturesEnabled(true);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                drawTrek();


            }

        });



    }


    public void drawTrek(){
        GPXParser mParser = new GPXParser();

        Gpx parsedGpx = null;
        try {

            String path = Environment.getExternalStorageDirectory() + "/TrekPricer/" + value_trekGpxFile + ".gpx";
            File file = new File(path);
            FileInputStream in = new FileInputStream(file);
            parsedGpx = mParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        if (parsedGpx != null) {

            PolylineOptions polylineOptions = new PolylineOptions();

            double startLatitude = 0.0, startLongitude = 0.0;

            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(i);
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {

                        if(startLatitude == 0.0 || startLongitude == 0.0){
                            startLatitude = trackPoint.getLatitude();
                            startLongitude = trackPoint.getLongitude();
                        }

                        polylineOptions.add(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude()));
                    }
                }
            }

            polylineOptions.geodesic(false).width(12).color(Color.BLUE);
            googleMap.addPolyline(polylineOptions);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(startLatitude, startLongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));


        }

    }
    public void drawspeed (){

        List<PointValue> values = new ArrayList<PointValue>();
        for (int j = 0; j < 50; ++j) {
            values.add(new PointValue(j, j%2));
        }





        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(Color.parseColor("#0277BD")).setCubic(false);
        line.setFilled(true);
        line.setPointRadius(1);
        line.setStrokeWidth(1);
        line.setHasLines(true);
        line.setHasLabelsOnlyForSelected(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setValueLabelBackgroundColor(Color.parseColor("#81D4FA"));
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName("S");
        axisY.setName("Km/h");
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        speedchat.setLineChartData(data);

    }

}



