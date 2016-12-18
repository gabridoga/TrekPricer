package com.trek.pricer.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trek.pricer.R;
import com.trek.pricer.database.DatabaseHandler;
import com.trek.pricer.database.SavedLineup;
import com.trek.pricer.main.MainActivity;
import com.trek.pricer.main.SaveTrekActivity;
import com.trek.pricer.services.LocationData;
import com.trek.pricer.utils.CostInstace;
import com.trek.pricer.utils.GPSTracker;
import com.trek.pricer.utils.GeneralUtils;
import com.trek.pricer.utils.LocationTracker;
import com.trek.pricer.utils.Prefs;
import com.trek.pricer.utils.TrackerSettings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.trek.pricer.utils.GeneralUtils.isPlugged;
import static com.trek.pricer.utils.GeneralUtils.round;


public class TrackingFragment extends Fragment {

    private View parentView;
    private static GoogleMap googleMap;
    private double mapCenterLatitude = 0.0;
    private double mapCenterLongitude = 0.0;
    private static double startLatitude = 0.0;
    private static double startLongitude = 0.0;
    private ArrayList<Location> locations;
    private static List<SavedLineup> savingslist;
    private static List<SavedLineup> costslist;
    private LocationTracker tracker;
    private static TextView savings, todayCost, netCost,  activityType, distance, duration, avgSpeed,saving_flag;
    private static Boolean onVehicle = false, isCharging = false;
    private static GPSTracker gpsTracker;
    private float previousMaxSpeed = 0, speedValue = 0 ,RealTodaySaving=0,RealToddayCost=0;
    private ButtonRectangle startTrek, pauseTrek, endTrek, resumeTrek, discardTrek, saveTrek;
    private LinearLayout bottomButtonBar, bottomButtonBar2,saving_backgroud,today_background,net_background;
    private static ImageView trekmode_imageview;
    public static Boolean isTrekOn = false ,trak_change=false;
    public static String dateString, timeString, MaxspeedString,Todaysaving,Todaycost,Start_TrekMode;
    private SavedLineup mItem = null;
    private String value_distance, value_avgSpeed, value_maxSpeed, value_duration, value_trekType, value_trekName,
            value_trekDate, value_trekStartTime, value_trekCost, value_trekSaving, value_trekGpxFile;
    private static TextClock time;
    public static String trekName;
    public static String trekMode = "auto";
    private ArrayList<String> speedValues;
    private Chronometer stopWatch;
    private AsyncTaskRunner runner;
    private long timeWhenPaused = 0;



    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_map, container, false);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.map, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }

        trekMode = Prefs.getString("activityType", "auto");

        if(!trekMode.equals("auto")){
            activityType.setText( trekMode + " - Confidence: " + "100 %");
        }

        gpsTracker = new GPSTracker(getActivity());

        locations = new ArrayList<>();

        savings = (TextView) parentView.findViewById(R.id.text_savings);
        todayCost = (TextView) parentView.findViewById(R.id.text_today);
        netCost = (TextView) parentView.findViewById(R.id.text_net_cost);
        distance = (TextView) parentView.findViewById(R.id.text_distance);
        duration = (TextView) parentView.findViewById(R.id.text_duration);
        avgSpeed = (TextView) parentView.findViewById(R.id.text_avg_speed);
        saving_flag=(TextView) parentView.findViewById(R.id.saving_flag);
        time = (TextClock) parentView.findViewById(R.id.map_time);
        stopWatch = (Chronometer) parentView.findViewById(R.id.chrono);
        activityType = (TextView) parentView.findViewById(R.id.text_trek_activity);
        trekmode_imageview=(ImageView)parentView.findViewById(R.id.trekmode_imageView);

        bottomButtonBar = (LinearLayout) parentView.findViewById(R.id.bottom_button_bar);
        bottomButtonBar2 = (LinearLayout) parentView.findViewById(R.id.bottom_button_bar_2);
        saving_backgroud=(LinearLayout)parentView.findViewById(R.id.saving_background);
        today_background=(LinearLayout)parentView.findViewById(R.id.today_background);
        net_background=(LinearLayout)parentView.findViewById(R.id.net_background);
        startTrek = (ButtonRectangle) parentView.findViewById(R.id.start_trek);
        pauseTrek = (ButtonRectangle) parentView.findViewById(R.id.pause_trek);
        resumeTrek = (ButtonRectangle) parentView.findViewById(R.id.resume_trek);
        saveTrek = (ButtonRectangle) parentView.findViewById(R.id.btn_map_save_trek);
        discardTrek = (ButtonRectangle) parentView.findViewById(R.id.btn_map_discard_trek);
        endTrek = (ButtonRectangle) parentView.findViewById(R.id.end_trek);
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer c) {
                long elapsedMillis = SystemClock.elapsedRealtime() - c.getBase();
                if (elapsedMillis > 3600000L) {
                    c.setFormat("0%s");
                } else {
                    c.setFormat("00:%s");
                }

                duration.setText(c.getText().toString());

            }
        });


        startTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isTrekOn = true;
                startTrek.setVisibility(View.GONE);
                pauseTrek.setVisibility(View.VISIBLE);
                stopWatch.setBase(SystemClock.elapsedRealtime());
                stopWatch.start();

                speedValues = new ArrayList<>();
                timeString = time.getText().toString();
                dateString = GeneralUtils.getCurrentDate();
                trekName = trekMode + "_" + dateString;

                gpsTracker = new GPSTracker(getActivity());

                googleMap.addMarker(new MarkerOptions().position(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude())).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(GeneralUtils.getMarker(getActivity(), "start"))));

                gpsTracker.stopUsingGPS();

                runner = new AsyncTaskRunner();
                runner.execute();
            }
        });

        pauseTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pauseTrek.setVisibility(View.GONE);
                bottomButtonBar.setVisibility(View.VISIBLE);
                timeWhenPaused = stopWatch.getBase() - SystemClock.elapsedRealtime();
                stopWatch.stop();
                isTrekOn = false;
                runner.cancel(true);
                tracker.stopListening();

            }
        });


        resumeTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isTrekOn = true;
                bottomButtonBar.setVisibility(View.GONE);
                pauseTrek.setVisibility(View.VISIBLE);
                stopWatch.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
                stopWatch.start();
                runner = new AsyncTaskRunner();
                runner.execute();

            }
        });

        discardTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomButtonBar.setVisibility(View.GONE);
                bottomButtonBar2.setVisibility(View.GONE);
                pauseTrek.setVisibility(View.GONE);
                startTrek.setVisibility(View.VISIBLE);

                resetFields();



            }
        });

        saveTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double total_distance=Double.parseDouble(distance.getText().toString());
                if(locations.size()==0||total_distance<0.1||trekMode.equals("STILL")){

                    Dialog dialog = errorSavingTrek();
                    dialog.show();


                } else {

                    Intent intent = new Intent(getActivity(), SaveTrekActivity.class);
                    intent.putExtra("distance", distance.getText().toString());
                    intent.putExtra("avgspeed", avgSpeed.getText().toString());
                    intent.putExtra("maxspeed", MaxspeedString);
                    intent.putExtra("duration", duration.getText().toString());
                    intent.putExtra("trekName", trekName);
                    intent.putExtra("activitytype", activityType.getText().toString());
                    if (trekMode.equals("IN_VEHICLE")||trekMode.equals("CAR")||trekMode.equals("SHARED")){

                        intent.putExtra("trekCost",savings.getText());
                        intent.putExtra("trekSaving","0.0");

                    }
                    else {
                        intent.putExtra("trekSaving",savings.getText());
                        intent.putExtra("trekCost","0.0");
                    }
//                    intent.putExtra("trekCost", savings.getText());
//                    intent.putExtra("trekSaving", "0.0");

                    intent.putExtra("trekGpxFile", trekName.replace(" ", "_"));
                    intent.putExtra("trekDate", dateString);
                    intent.putExtra("trekStartTime", timeString);
                    startActivity(intent);

                    startTrek.setVisibility(View.VISIBLE);
                    bottomButtonBar.setVisibility(View.GONE);
                    bottomButtonBar2.setVisibility(View.GONE);

                    GeneralUtils.writePath(trekName, locations);

                    resetFields();
                    runner.cancel(true);
                    saving_backgroud.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    today_background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    net_background.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    activityType.setBackgroundColor(Color.parseColor("#FFFFFF"));

                }

            }
        });

        endTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                runner.cancel(true);
                gpsTracker = new GPSTracker(getActivity());
                googleMap.addMarker(new MarkerOptions().position(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude())).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(GeneralUtils.getMarker(getActivity(), "end"))));
                gpsTracker.stopUsingGPS();
                stopWatch.stop();
                timeWhenPaused = 0;
                stopWatch.setBase(SystemClock.elapsedRealtime());
                bottomButtonBar.setVisibility(View.GONE);
                bottomButtonBar2.setVisibility(View.VISIBLE);


            }
        });



        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {

                googleMap = map;

                try {
                    MapsInitializer.initialize(getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    googleMap.getUiSettings().setZoomGesturesEnabled(true);
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                if (gpsTracker.canGetLocation()) {
                    startLongitude = gpsTracker.getLongitude();
                    startLatitude = gpsTracker.getLatitude();
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(startLatitude, startLongitude)).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                    LocationData.getInstance().setCurrLocation(gpsTracker.getLocation());
                    gpsTracker.stopUsingGPS();

                }

            }

        });



        if (isPlugged(getActivity())) {
            isCharging = true;

        } else {
            isCharging = false;

        }
        return parentView;

    }

    public float getTodaySavings(){
        savingslist=new ArrayList<>();
        savingslist= DatabaseHandler.getInstance(getActivity()).getAllTrek();
        double todaysaving_tmp=0;
        for (SavedLineup save :savingslist){
//        Log.d("NUMBER",save.trek_max_speed);
            todaysaving_tmp=Double.parseDouble(save.trek_saving);
            todaysaving_tmp+=todaysaving_tmp;
        }
        float saving_resut= (float) todaysaving_tmp;
        return saving_resut;
    }
    public float getTodayCosts(){

        costslist=new ArrayList<>();
        costslist= DatabaseHandler.getInstance(getActivity()).getAllTrek();
        double todaycosts_tmp=0;
        for (SavedLineup cost :costslist){
//      Log.d("NUMBER",save.trek_max_speed);
            todaycosts_tmp=Double.parseDouble(cost.trek_cost);
            todaycosts_tmp+=todaycosts_tmp;
        }
        float cost_result= (float) todaycosts_tmp;
        return cost_result;

    }
    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onDetach() {
        super.onDetach();

        Fragment parentFragment = getParentFragment();
        FragmentManager manager;
        if (parentFragment != null) {
            // If parent is another fragment, then this fragment is nested
            manager = parentFragment.getChildFragmentManager();
        } else {
            // This fragment is placed into activity
            manager = getActivity().getSupportFragmentManager();
        }
        if(!getActivity().isDestroyed()){
            manager.beginTransaction().remove(this).commitAllowingStateLoss();
        }


        if (tracker != null) {
            tracker.stopListening();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }


    public static void updateTrekMode(String mode, String confidence){


        if (mode.equals("IN_VEHICLE") && isCharging) {
            activityType.setText("CAR (" + confidence+")");
            trekMode="CAR";

            trekName = trekMode + "_" + dateString;

        } else if (mode.equals("IN_VEHICLE")) {
            activityType.setText("SHARED (" + confidence+")");
            trekMode="SHARED";

            trekName = trekMode + "_" + dateString;

        } else {
            activityType.setText(mode + " (" + confidence+")");
            trekMode=mode;

            trekName = trekMode + "_" + dateString;

        }

        if(trekMode.equals("CAR")&&Start_TrekMode.equals("ON_BICYCLE")){
            trak_change=true;

            trekName = trekMode + "_" + dateString;
        }
    }


    public static void setUpMap(){
            startLongitude = MainActivity.getLongitude();
            startLatitude =  MainActivity.getLatitude();

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(startLatitude, startLongitude)).zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            LocationData.getInstance().setCurrLocation(gpsTracker.getLocation());

            gpsTracker.stopUsingGPS();

    }


    private void addLines() {

        PolylineOptions polylineOptions = new PolylineOptions();

        for (Location loc : locations) {
            polylineOptions.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        polylineOptions.geodesic(true).width(16).color(Color.BLUE);

        googleMap.addPolyline(polylineOptions);


    }

    /**
     * @author Prabu
     * Private class which runs the long operation. ( Sleeping for some time )
     */
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {

            try {

                tracker = new LocationTracker(
                        getActivity(),
                        new TrackerSettings()
                                .setUseGPS(true)
                                .setUseNetwork(false)
                                .setUsePassive(false)
                                .setTimeout(3 * 60 * 1000)
                                .setMetersBetweenUpdates(5)
                                .setTimeBetweenUpdates(5 * 1000)

                ) {

                    @Override
                    public void onLocationFound(Location location) {
                        Toast.makeText(getActivity(),trekMode,Toast.LENGTH_SHORT).show();
                        // Do some stuff when a new GPS Location has been found
                        if (isTrekOn) {
                            Toast.makeText(getActivity(),"trek is on",Toast.LENGTH_SHORT).show();
                            if(trekMode.equals("STILL")||trekMode.equals("TILTING")||trekMode.equals("UNKNOWN")){
                                Toast.makeText(getActivity(),"Trek mode is still,titlling,unknown",Toast.LENGTH_SHORT).show();
                                Dialog dialog = errorSavingTrek();
                                dialog.show();
                                endTrek_decettion();
                            }
                            else{

                                if (trak_change){
                                    Toast.makeText(getActivity(),"trekmode changed",Toast.LENGTH_SHORT).show();
                                    pauseTrek_decettion();
                                    endTrek_decettion();
                                    savetrek_decettion();
                                    startTrek_decettion();

                                }
                                else {

                                    Toast.makeText(getActivity(),"trek mode is start+",Toast.LENGTH_SHORT).show();

                                    Start_TrekMode=trekMode;

                                    mapCenterLatitude = location.getLatitude();
                                    mapCenterLongitude = location.getLongitude();

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(new LatLng(mapCenterLatitude, mapCenterLongitude)).zoom(17).build();
                                    googleMap.animateCamera(CameraUpdateFactory
                                            .newCameraPosition(cameraPosition));


                                    if (isPlugged(getActivity())) {
                                        isCharging = true;
                                        //  activityType.setText("Driving");
                                    } else {
                                        isCharging = false;
                                    }

//                            if (speedValue > 40) {
//                                onVehicle = true;
//                            } else {
//                                onVehicle = false;
//                            }

                                    locations.add(location);
                                    speedValue = ((location.getSpeed() * 3600) / 1000);
                                    speedValues.add(String.valueOf(speedValue));
                                    addLines();
                                    LocationData.getInstance().setCurrLocation(location);
                                    distance.setText(String.valueOf(round(LocationData.getInstance().getDistance(), 2)));
                                    setMaxSpeed(speedValue);
                                    if (trekMode.equals("CAR")||trekMode.equals("SHARED")){

                                        saving_flag.setText("Costs($)");
                                        saving_backgroud.setBackgroundColor(Color.parseColor("#E53935"));
                                        today_background.setBackgroundColor(Color.parseColor("#E53935"));
                                        net_background.setBackgroundColor(Color.parseColor("#E53935"));


                                    }
                                    else{

                                        saving_flag.setText("Saving($)");
                                        saving_backgroud.setBackgroundColor(Color.parseColor("#009688"));
                                        today_background.setBackgroundColor(Color.parseColor("#009688"));
                                        net_background.setBackgroundColor(Color.parseColor("#009688"));


                                    }
                                    if(trekMode.equals("CAR")){
                                        trekmode_imageview.setImageResource(R.drawable.car);
                                        activityType.setBackgroundColor(Color.parseColor("#E53935"));

// cost or saving calculate
                                        Float distance_temp=round(LocationData.getInstance().getDistance(),2);
                                        float saviing_temp= (float) (distance_temp* CostInstace.carpriceunit);
                                        DecimalFormat df = new DecimalFormat("#.##");
                                        savings.setText(String.valueOf(df.format(round(saviing_temp,2))));
//net calculate
                                        float total_cost=getTodayCosts();
                                        float total_savings=getTodaySavings();
                                        float total_net= (float) (total_cost-total_savings+4.2);
                                        netCost.setText(String.valueOf(df.format(round(total_net,1))));

// today saving or cost;

                                        float today_total=saviing_temp+getTodayCosts();
                                        todayCost.setText(String.valueOf(df.format(round(today_total,2))));



                                    }
                                    else if (trekMode.equals("SHARED")){
                                        trekmode_imageview.setImageResource(R.drawable.bus);
                                        activityType.setBackgroundColor(Color.parseColor("#FF5722"));


// cost or saving calculate
                                        Float distance_temp=round(LocationData.getInstance().getDistance(),2);
                                        float saviing_temp= (float) (distance_temp* CostInstace.buspriceunit);
                                        DecimalFormat df = new DecimalFormat("#.##");
                                        savings.setText(String.valueOf(df.format(round(saviing_temp,2))));
//net calculate
                                        float total_cost=getTodayCosts();
                                        float total_savings=getTodaySavings();
                                        float total_net= (float) (total_cost-total_savings+4.2);
                                        netCost.setText(String.valueOf(df.format(round(total_net,1))));

// today saving or cost;

                                        float today_total=saviing_temp+getTodayCosts();
                                        todayCost.setText(String.valueOf(df.format(round(today_total,2))));

                                    }
                                    else if (trekMode.equals("ON_FOOT")||trekMode.equals("WALKING")||trekMode.equals("RUNNING")){
                                        trekmode_imageview.setImageResource(R.drawable.foot);
                                        activityType.setBackgroundColor(Color.parseColor("#4CAF50"));


// cost or saving calculate
                                        Float distance_temp=round(LocationData.getInstance().getDistance(),2);
                                        float saviing_temp= (float) (distance_temp* CostInstace.footpriceunit);
                                        DecimalFormat df = new DecimalFormat("#.##");
                                        savings.setText(String.valueOf(df.format(round(saviing_temp,2))));
//net calculate
                                        float total_cost=getTodayCosts();
                                        float total_savings=getTodaySavings();
                                        float total_net= (float) (total_cost-total_savings);
                                        netCost.setText(String.valueOf(df.format(round(total_net,1))));

// today saving or cost;

                                        float today_total=saviing_temp+getTodaySavings();
                                        todayCost.setText(String.valueOf(df.format(round(today_total,2))));

                                    }
                                    else if (trekMode.equals("ON_BICYCLE")){
                                        trekmode_imageview.setImageResource(R.drawable.bike);
                                        activityType.setBackgroundColor(Color.parseColor("#4CAF50"));

// cost or saving calculate
                                        Float distance_temp=round(LocationData.getInstance().getDistance(),2);
                                        float saviing_temp= (float) (distance_temp* CostInstace.bikepriceunit);
                                        DecimalFormat df = new DecimalFormat("#.##");
                                        savings.setText(String.valueOf(df.format(round(saviing_temp,2))));
//net calculate
                                        float total_cost=getTodayCosts();
                                        float total_savings=getTodaySavings();
                                        float total_net= (float) (total_cost-total_savings);
                                        netCost.setText(String.valueOf(df.format(round(total_net,1))));

// today saving or cost;

                                        float today_total=saviing_temp+getTodaySavings();
                                        todayCost.setText(String.valueOf(df.format(round(today_total,2))));
                                    }




                                    double avg_speed = GeneralUtils.getAverageSpeed(LocationData.getInstance().getDistance(), duration.getText().toString());

                                    if(avg_speed > previousMaxSpeed) {
                                        DecimalFormat df = new DecimalFormat("#.##");

                                        avgSpeed.setText(String.valueOf(df.format(GeneralUtils.getAverageSpeed(LocationData.getInstance().getDistance(), duration.getText().toString()))));
                                    }


                                }



                            }






                        }


                    }


                    @Override
                    public void onTimeout() {
                        // Do some stuff when a new GPS Location has been found
                    }


                };

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tracker.startListening();
                        } catch (SecurityException e){
                            e.printStackTrace();
                        }
                    }
                });




            } catch (SecurityException e){
                e.printStackTrace();
            }


            return resp;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }



    public float setMaxSpeed(float speed) {

        if (speed > previousMaxSpeed) {
            DecimalFormat df = new DecimalFormat("#.##");
            MaxspeedString=String.valueOf(df.format(speed));
            previousMaxSpeed = speed;
        }

        return previousMaxSpeed;

    }

    public boolean savetrek_decettion(){
        boolean result=false;
        double total_distance=Double.parseDouble(distance.getText().toString());
        if(locations.size()==0||total_distance<0.1){

            Dialog dialog = errorSavingTrek();
            dialog.show();


        }else {

            value_distance=distance.getText().toString();
            value_avgSpeed=avgSpeed.getText().toString();
            value_maxSpeed=MaxspeedString;
            value_duration=duration.getText().toString();
            value_trekName=trekName;
            value_trekType=activityType.getText().toString();
            if (trekMode.equals("CAR")||trekMode.equals("SHARED")){
                value_trekCost=savings.getText().toString();
                value_trekSaving="0.0";

            }
            else {

                value_trekSaving=savings.getText().toString();
                value_trekCost="0.0";
            }



            value_trekGpxFile=trekName.replace("","_");
            value_trekDate=dateString;
            value_trekStartTime=timeString;

            startTrek.setVisibility(View.VISIBLE);
            bottomButtonBar.setVisibility(View.GONE);
            bottomButtonBar2.setVisibility(View.GONE);





        SavedLineup p = new SavedLineup();
        DatabaseHandler.getInstance(getActivity()).putTrek(p);
        // Open a new fragment with the new id
        mItem = DatabaseHandler.getInstance(getActivity()).getTrek(p.id);
        // onItemSelected(p.id);


        if (mItem != null) {
            mItem.trek_name = value_trekName;
            mItem.trek_mode = value_trekType;
            mItem.trek_date = value_trekDate;
            mItem.trek_start_time = value_trekStartTime;
            mItem.trek_duration = value_duration;
            mItem.trek_distance = value_distance;
            mItem.trek_max_speed = value_maxSpeed;
            mItem.trek_avg_speed = value_avgSpeed;
            mItem.trek_cost = value_trekCost;
            mItem.trek_saving = value_trekSaving;
            mItem.trek_gpx_file = value_trekGpxFile;
            DatabaseHandler.getInstance(getActivity()).putTrek(mItem);

        }






            GeneralUtils.writePath(trekName, locations);

            resetFields();
            runner.cancel(true);
            saving_backgroud.setBackgroundColor(Color.parseColor("#FFFFFF"));
            today_background.setBackgroundColor(Color.parseColor("#FFFFFF"));
            net_background.setBackgroundColor(Color.parseColor("#FFFFFF"));
            activityType.setBackgroundColor(Color.parseColor("#FFFFFF"));
            result=true;

        }





        return result;
    }

    public boolean startTrek_decettion(){
        boolean start_flag=false;


        isTrekOn = true;
        startTrek.setVisibility(View.GONE);
        pauseTrek.setVisibility(View.VISIBLE);
        stopWatch.setBase(SystemClock.elapsedRealtime());
        stopWatch.start();

        speedValues = new ArrayList<>();
        timeString = time.getText().toString();
        dateString = GeneralUtils.getCurrentDate();
        trekName = trekMode + "_" + dateString;

        gpsTracker = new GPSTracker(getActivity());

//        googleMap.addMarker(new MarkerOptions().position(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude())).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(GeneralUtils.getMarker(getActivity(), "start"))));

//                gpsTracker.stopUsingGPS();

        runner = new AsyncTaskRunner();
        runner.execute();
        start_flag=true;


        return start_flag;
    }

    public boolean endTrek_decettion(){

        boolean end_flag=false;

        runner.cancel(true);
        gpsTracker = new GPSTracker(getActivity());
        googleMap.addMarker(new MarkerOptions().position(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude())).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(GeneralUtils.getMarker(getActivity(), "end"))));
        gpsTracker.stopUsingGPS();
        stopWatch.stop();
        timeWhenPaused = 0;
        stopWatch.setBase(SystemClock.elapsedRealtime());
        bottomButtonBar.setVisibility(View.GONE);
        bottomButtonBar2.setVisibility(View.VISIBLE);
        end_flag=true;
        return end_flag;
    }



 public boolean pauseTrek_decettion(){

            pauseTrek.setVisibility(View.GONE);
            bottomButtonBar.setVisibility(View.VISIBLE);
            timeWhenPaused = stopWatch.getBase() - SystemClock.elapsedRealtime();
            stopWatch.stop();
            isTrekOn = false;
            runner.cancel(true);
            tracker.stopListening();
            return true;
        }



public boolean    resumeTrek_decettion(){

            isTrekOn = true;
            bottomButtonBar.setVisibility(View.GONE);
            pauseTrek.setVisibility(View.VISIBLE);
            stopWatch.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            stopWatch.start();
            runner = new AsyncTaskRunner();
            runner.execute();
            return true;
        }

 public boolean   discardTrek_decettion(){
            bottomButtonBar.setVisibility(View.GONE);
            bottomButtonBar2.setVisibility(View.GONE);
            pauseTrek.setVisibility(View.GONE);
            startTrek.setVisibility(View.VISIBLE);

            resetFields();
            return true;


        }





    public Dialog errorSavingTrek() {

        //Initialize the Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);


        // Set the dialog title
        builder.setTitle("Error")
                .setMessage("Unfortunately there is insufficient data to save this trek.")

                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        bottomButtonBar.setVisibility(View.GONE);
                        bottomButtonBar2.setVisibility(View.GONE);
                        pauseTrek.setVisibility(View.GONE);
                        startTrek.setVisibility(View.VISIBLE);

                        resetFields();

                    }
                });


        return builder.create();
    }

    public void resetFields(){

        duration.setText("0:0:0");
        savings.setText("0.0");
        distance.setText("0.0");
        avgSpeed.setText("0.0");
        todayCost.setText("0.0");
        netCost.setText("0.0");

        timeWhenPaused = 0;
        previousMaxSpeed = 0;
        speedValue = 0;

        isTrekOn = false;

        googleMap.clear();
        speedValues.clear();
        locations.clear();

    }

}
