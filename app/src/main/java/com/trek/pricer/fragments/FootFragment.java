package com.trek.pricer.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.trek.pricer.R;
import com.trek.pricer.main.Global;
import com.trek.pricer.main.SaveTrekActivity;
import com.trek.pricer.services.LocationData;
import com.trek.pricer.utils.GeneralUtils;
import com.trek.pricer.utils.LocationTracker;
import com.trek.pricer.utils.TrackerSettings;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.trek.pricer.utils.GeneralUtils.round;


public class FootFragment extends Fragment {

    View parentView;
    private static TextView date, distance, duration, avgSpeed, maxSpeed, savings, todaySavings, netSavings;
    private AsyncTaskRunner runner;
    private static TextClock time;
    private ArrayList<Location> locations;
    private LocationTracker tracker;
    private float previousMaxSpeed = 0, speedValue = 0;
    private ButtonRectangle startTrek, pauseTrek, endTrek, resumeTrek, discardTrek, saveTrek;
    private LinearLayout bottomButtonBar, bottomButtonBar2;
    private Boolean isTrekOn = false;
    private String trekName;
    private String timeString, dateString,Maxspeedstring;
    private ArrayList<String> speedValues;
    private long elapsedMillis;
    private Chronometer stopWatch;
    private long timeWhenPaused = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_foot, container, false);

        distance = (TextView) parentView.findViewById(R.id.foot_distance);
        duration = (TextView) parentView.findViewById(R.id.foot_duration);
        avgSpeed = (TextView) parentView.findViewById(R.id.foot_avg_speed);
        maxSpeed = (TextView) parentView.findViewById(R.id.foot_max_speed);
        date = (TextView) parentView.findViewById(R.id.foot_date);
        time = (TextClock) parentView.findViewById(R.id.foot_time);
        savings = (TextView) parentView.findViewById(R.id.foot_savings);
        todaySavings = (TextView) parentView.findViewById(R.id.foot_today_savings);
        netSavings = (TextView) parentView.findViewById(R.id.foot_net_saving);
        stopWatch = (Chronometer) parentView.findViewById(R.id.foot_chrono);
        startTrek = (ButtonRectangle) parentView.findViewById(R.id.foot_start_trek);
        pauseTrek = (ButtonRectangle) parentView.findViewById(R.id.foot_pause_trek);
        resumeTrek = (ButtonRectangle) parentView.findViewById(R.id.foot_resume_trek);
        saveTrek = (ButtonRectangle) parentView.findViewById(R.id.btn_foot_save_trek);
        discardTrek = (ButtonRectangle) parentView.findViewById(R.id.btn_foot_discard_trek);
        endTrek = (ButtonRectangle) parentView.findViewById(R.id.foot_end_trek);
        bottomButtonBar = (LinearLayout) parentView.findViewById(R.id.foot_bottom_button_bar);
        bottomButtonBar2 = (LinearLayout) parentView.findViewById(R.id.foot_bottom_button_bar_2);

        date.setText(GeneralUtils.getDisplayDate());

        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer c) {
                elapsedMillis = SystemClock.elapsedRealtime() - c.getBase();
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
                locations = new ArrayList<>();
                speedValues = new ArrayList<>();
                startTrek.setVisibility(View.GONE);
                pauseTrek.setVisibility(View.VISIBLE);
                stopWatch.setBase(SystemClock.elapsedRealtime());
                stopWatch.start();
                timeString = time.getText().toString();
                dateString = GeneralUtils.getCurrentDate();
                trekName = "Walk" + "_" + dateString;

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

                isTrekOn = false;
                bottomButtonBar.setVisibility(View.GONE);
                bottomButtonBar2.setVisibility(View.GONE);
                pauseTrek.setVisibility(View.GONE);
                startTrek.setVisibility(View.VISIBLE);

                timeWhenPaused = 0;
                stopWatch.stop();
                timeWhenPaused = 0;
                speedValue = 0;
                previousMaxSpeed = 0;

                speedValues.clear();
                locations.clear();

                duration.setText("00:00:00");
                maxSpeed.setText("0.0");
                distance.setText("0.0");
                avgSpeed.setText("0.0");


            }
        });

        saveTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(locations.size()==0){

                    Dialog dialog = errorSavingTrek();
                    dialog.show();


                } else {

                    Intent intent = new Intent(getActivity(), SaveTrekActivity.class);
                    intent.putExtra("distance", distance.getText().toString());
                    intent.putExtra("avgspeed", avgSpeed.getText().toString());
                    intent.putExtra("maxspeed", maxSpeed.getText().toString());
                    intent.putExtra("duration", duration.getText().toString());
                    intent.putExtra("trekName", trekName);
                    intent.putExtra("trekGpxFile", trekName);
                    intent.putExtra("trekDate", dateString);
                    intent.putExtra("trekStartTime", timeString);
                    intent.putExtra("activitytype", "Car");
                    intent.putExtra("trekCost", "0.0");
                    intent.putExtra("trekSaving", "0.0");
                    startActivity(intent);
                    isTrekOn = false;
                    duration.setText("00:00:00");
                    distance.setText("0.0");
                    avgSpeed.setText("0.0");
                    maxSpeed.setText("0.0");
                    timeWhenPaused = 0;
                    previousMaxSpeed = 0;
                    speedValue = 0;
                    startTrek.setVisibility(View.VISIBLE);
                    bottomButtonBar.setVisibility(View.GONE);
                    bottomButtonBar2.setVisibility(View.GONE);

                    GeneralUtils.writePath(trekName, locations);

                    speedValues.clear();
                    locations.clear();

                }


            }
        });

        endTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                runner.cancel(true);
                stopWatch.stop();
                timeWhenPaused = 0;
                speedValue = 0;
                timeWhenPaused = 0;
                previousMaxSpeed = 0;
                stopWatch.setBase(SystemClock.elapsedRealtime());
                bottomButtonBar.setVisibility(View.GONE);
                bottomButtonBar2.setVisibility(View.VISIBLE);
                LocationData.getInstance().resetLocationData();

            }
        });


        return parentView;

    }

    /**
     * @author Prabu
     *         Private class which runs the long operation. ( Sleeping for some time )
     */
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {

            try {

                int updateFrequency =  Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("foot_frequency", "10"));

                tracker = new LocationTracker(
                        getActivity(),
                        new TrackerSettings()
                                .setUseGPS(true)
                                .setUseNetwork(false)
                                .setUsePassive(false)
                                .setTimeout(3 * 60 * 1000)
                                .setMetersBetweenUpdates(Global.CAR_MIN_METERS_BW_UPDATES)
                                .setTimeBetweenUpdates(updateFrequency * 1000)) {

                    @Override
                    public void onLocationFound(Location location) {

                        if (location.getAccuracy() <= 200 && isTrekOn) {

                            locations.add(location);
                            speedValue = ((location.getSpeed() * 3600) / 1000);
                            speedValues.add(String.valueOf(speedValue));
                            LocationData.getInstance().setCurrLocation(location);
                            distance.setText(String.valueOf(round(LocationData.getInstance().getDistance(), 2)));
                            setMaxSpeed(speedValue);
                            date.setText(GeneralUtils.getDisplayDate());
                            double avg_speed = GeneralUtils.getAverageSpeed(LocationData.getInstance().getDistance(), duration.getText().toString());

                            if(avg_speed < previousMaxSpeed) {
                                DecimalFormat df = new DecimalFormat("#.##");
                                avgSpeed.setText(String.valueOf(df.format(GeneralUtils.getAverageSpeed(LocationData.getInstance().getDistance(), duration.getText().toString()))));
                            }

                        }

                    }


                    @Override
                    public void onTimeout() {

                    }


                };

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tracker.startListening();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                });


            } catch (SecurityException e) {
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


    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onDestroyView() {
        if (tracker != null) {
            tracker.stopListening();
        }
        super.onDestroyView();

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    public void setMaxSpeed(float speed) {

        if (speed > previousMaxSpeed) {
            DecimalFormat df = new DecimalFormat("#.##");
            Maxspeedstring=String.valueOf(df.format(speed));
            previousMaxSpeed = speed;
        }

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

                        isTrekOn = false;
                        bottomButtonBar.setVisibility(View.GONE);
                        bottomButtonBar2.setVisibility(View.GONE);
                        pauseTrek.setVisibility(View.GONE);
                        startTrek.setVisibility(View.VISIBLE);

                        timeWhenPaused = 0;
                        stopWatch.stop();
                        timeWhenPaused = 0;
                        speedValue = 0;
                        previousMaxSpeed = 0;

                        speedValues.clear();
                        locations.clear();

                        duration.setText("00:00:00");
                        maxSpeed.setText("0.0");
                        distance.setText("0.0");
                        avgSpeed.setText("0.0");

                    }
                });


        return builder.create();
    }



}
