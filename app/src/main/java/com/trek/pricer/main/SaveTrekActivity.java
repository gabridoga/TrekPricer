package com.trek.pricer.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.trek.pricer.R;
import com.trek.pricer.database.DatabaseHandler;
import com.trek.pricer.database.SavedLineup;


public class SaveTrekActivity extends AppCompatActivity {

    private SavedLineup mItem = null;
    private String value_distance, value_avgSpeed, value_maxSpeed, value_duration, value_trekType, value_trekName,
            value_trekDate, value_trekStartTime, value_trekCost, value_trekSaving, value_trekGpxFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_trek);

        TextView maxSpeed = (TextView) findViewById(R.id.text_trek_max_speed);
        TextView avgSpeed = (TextView) findViewById(R.id.text_trek_avg_speed);
        TextView trekDistance = (TextView) findViewById(R.id.text_trek_distance);
        TextView trekDuration = (TextView) findViewById(R.id.text_trek_duration);
        EditText trekCost = (EditText) findViewById(R.id.edittext_trek_cost);
        TextView trekSaving = (TextView) findViewById(R.id.text_trek_saving);
        TextView trekType = (TextView) findViewById(R.id.text_trek_type);
        EditText trekName = (EditText) findViewById(R.id.edittext_trek_name);

        Intent intent = getIntent();
        value_distance = intent.getStringExtra("distance");
        value_avgSpeed = intent.getStringExtra("avgspeed");
        value_maxSpeed = intent.getStringExtra("maxspeed");
        value_duration = intent.getStringExtra("duration");
        value_trekType = intent.getStringExtra("activitytype");
        value_trekName = intent.getStringExtra("trekName");
        value_trekCost = intent.getStringExtra("trekCost");
        value_trekSaving = intent.getStringExtra("trekSaving");
        value_trekGpxFile = intent.getStringExtra("trekGpxFile");
        value_trekDate = intent.getStringExtra("trekDate");
        value_trekStartTime = intent.getStringExtra("trekStartTime");

        maxSpeed.setText(value_maxSpeed);
        avgSpeed.setText(value_avgSpeed);
        trekDistance.setText(value_distance);
        trekDuration.setText(value_duration);
        trekType.setText(value_trekType);
        trekName.setText(value_trekName);
        trekCost.setText(value_trekCost);
        trekSaving.setText(value_trekSaving);




        if (value_trekType.equals("STILL") || value_trekType.equals("UNKNOWN") || value_trekType.equals("WALKING") ||
                value_trekType.equals("ON_FOOT") || value_trekType.equals("RUNNING") || value_trekType.equals("ON_BICYCLE") || value_trekType.equals("TILTING")) {

            trekCost.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (value_trekType.equals("CAR")) { // Own Vehicle
            trekSaving.setTextColor(Color.parseColor("#C0C0C0"));
        } else { //Shared
            trekSaving.setTextColor(Color.parseColor("#C0C0C0"));
        }



        ButtonRectangle discardTrek = (ButtonRectangle) findViewById(R.id.btn_discard_trek);

        discardTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = confirmDiscardTrek();
                dialog.show();
            }
        });

        ButtonRectangle saveTrek = (ButtonRectangle) findViewById(R.id.btn_save_trek);

        saveTrek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

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
                //do something you want

                //Dialog dialog = confirmDiscardTrek();
                //dialog.show();

                finish();



            }
        });

        saveTrek();


    }


    public Dialog confirmDiscardTrek() {

        //Initialize the Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SaveTrekActivity.this, R.style.AppCompatAlertDialogStyle);


        // Set the dialog title
        builder.setTitle("Discard Trek")
                .setMessage("Trek Data will be lost. Are you sure you want to discard trek data?")

                // Set the action buttons
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }

    public void saveTrek() {
        SavedLineup p = new SavedLineup();
        DatabaseHandler.getInstance(SaveTrekActivity.this).putTrek(p);
        // Open a new fragment with the new id
        mItem = DatabaseHandler.getInstance(SaveTrekActivity.this).getTrek(p.id);
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


            DatabaseHandler.getInstance(SaveTrekActivity.this).putTrek(mItem);

        }

    }
}
