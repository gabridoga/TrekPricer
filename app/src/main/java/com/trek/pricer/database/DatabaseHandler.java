package com.trek.pricer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	private static DatabaseHandler singleton;
	
	public static DatabaseHandler getInstance(final Context context) {
		if (singleton == null) {
			singleton = new DatabaseHandler(context);
		}
		return singleton;
	}

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "SavedLineups";

	private final Context context;

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// Good idea to have the context that doesn't die with the window
		this.context = context.getApplicationContext();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SavedLineup.CREATE_TABLE);


	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public synchronized SavedLineup getTrek(final long id) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.query(SavedLineup.TABLE_NAME, SavedLineup.FIELDS,
				SavedLineup.COL_ID + " IS ?", new String[] { String.valueOf(id) },
				null, null, null, null);
		if (cursor == null || cursor.isAfterLast()) {
			return null;
		}

		SavedLineup item = null;
		if (cursor.moveToFirst()) {
			item = new SavedLineup(cursor);
		}
		cursor.close();
		return item;
	}
    public synchronized List<SavedLineup> getAllTrek(){

        List<SavedLineup> treklist;

		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.query(SavedLineup.TABLE_NAME, SavedLineup.FIELDS, null, null, null,
				null, SavedLineup.COL_TREKDATE + " DESC", null);;
		if (cursor == null || cursor.isAfterLast()) {
			return null;
		}
        else {
			treklist=new ArrayList<>();
			SavedLineup item = null;
			if (cursor.moveToFirst()) {
				do{

					item = new SavedLineup(cursor);
					treklist.add(item);

				}while(cursor.moveToNext());

			}
			cursor.close();
		}




		return treklist;
	};
	public synchronized boolean putTrek(final SavedLineup lineup) {
		boolean success = false;
		int result = 0;
		final SQLiteDatabase db = this.getWritableDatabase();

		if (lineup.id > -1) {
			result += db.update(SavedLineup.TABLE_NAME, lineup.getContent(),
					SavedLineup.COL_ID + " IS ?",
					new String[] { String.valueOf(lineup.id) });
		}

		if (result > 0) {
			success = true;
		} else {
			// Update failed or wasn't possible, insert instead
			final long id = db.insert(SavedLineup.TABLE_NAME, null,
					lineup.getContent());

			if (id > -1) {
				lineup.id = id;
				success = true;
			}
		}
		
		if (success) {
			notifyProviderOnLineupChange();
		}
		
		return success;
	}

	public synchronized int removeTrek(final SavedLineup person) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final int result = db.delete(SavedLineup.TABLE_NAME,
				SavedLineup.COL_ID + " IS ?",
				new String[] { Long.toString(person.id) });

		if (result > 0) {
			notifyProviderOnLineupChange();
		}
		return result;
	}
	
	private void notifyProviderOnLineupChange() {
		context.getContentResolver().notifyChange(
				LineupProvider.URI_PERSONS, null, false);
	}
}
