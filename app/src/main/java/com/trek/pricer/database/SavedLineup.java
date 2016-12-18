package com.trek.pricer.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * A class representation of a row in table "MyTreks".
 */
public class SavedLineup {

    public static final String TABLE_NAME = "MyTreks";

    public static final String COL_ID = "_id";
    public static final String COL_TREKNAME = "trek_name";
    public static final String COL_TREKMODE = "trek_mode";
    public static final String COL_TREKDATE = "trek_date";
    public static final String COL_TREKSTARTTIME = "trek_start_time";
    public static final String COL_TREKDURATION = "trek_duration";
    public static final String COL_TREKDISTANCE = "trek_distance";
    public static final String COL_TREKMAXSPEED = "trek_max_speed";
    public static final String COL_TREKAVGSPEED = "trek_avg_speed";
    public static final String COL_TREKCOST = "trek_cost";
    public static final String COL_TREKSAVING = "trek_saving";
    public static final String COL_TREKGPXFILE = "trek_gpx_file";



    // For database projection so order is consistent
    public static final String[] FIELDS = {COL_ID, COL_TREKNAME, COL_TREKMODE, COL_TREKDATE, COL_TREKSTARTTIME, COL_TREKDURATION, COL_TREKDISTANCE,
            COL_TREKMAXSPEED, COL_TREKAVGSPEED, COL_TREKCOST, COL_TREKSAVING, COL_TREKGPXFILE};

    /*
     * The SQL code that creates a Table for storing Persons in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_TREKNAME + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKMODE + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKDATE + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKSTARTTIME + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKDURATION + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKDISTANCE + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKMAXSPEED + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKAVGSPEED + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKCOST + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKSAVING + " TEXT NOT NULL DEFAULT '',"
                    + COL_TREKGPXFILE + " TEXT NOT NULL DEFAULT ''"
                    + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String trek_name = "";
    public String trek_mode = "";
    public String trek_date = "";
    public String trek_start_time = "";
    public String trek_duration = "";
    public String trek_distance = "";
    public String trek_max_speed = "";
    public String trek_avg_speed = "";
    public String trek_cost = "";
    public String trek_saving = "";
    public String trek_gpx_file = "";


    /**
     * No need to do anything, fields are already set to default values above
     */
    public SavedLineup() {
    }

    /**
     * Convert information from the database into a SavedLineup object.
     */
    public SavedLineup(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id          = cursor.getLong(0);
        this.trek_name = cursor.getString(1);
        this.trek_mode = cursor.getString(2);
        this.trek_date = cursor.getString(3);
        this.trek_start_time = cursor.getString(4);
        this.trek_duration = cursor.getString(5);
        this.trek_distance = cursor.getString(6);
        this.trek_max_speed = cursor.getString(7);
        this.trek_avg_speed = cursor.getString(8);
        this.trek_cost = cursor.getString(9);
        this.trek_saving = cursor.getString(10);
        this.trek_gpx_file = cursor.getString(11);

    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_TREKNAME, trek_name);
        values.put(COL_TREKMODE, trek_mode);
        values.put(COL_TREKDATE, trek_date);
        values.put(COL_TREKSTARTTIME, trek_start_time);
        values.put(COL_TREKDURATION, trek_duration);
        values.put(COL_TREKDISTANCE, trek_distance);
        values.put(COL_TREKMAXSPEED, trek_max_speed);
        values.put(COL_TREKAVGSPEED, trek_avg_speed);
        values.put(COL_TREKCOST, trek_cost);
        values.put(COL_TREKSAVING, trek_saving);
        values.put(COL_TREKGPXFILE, trek_gpx_file);


        return values;
    }
}
