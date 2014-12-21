package com.eyebrowssoftware.example.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.eyebrowssoftware.example.db.BPRecords.BPRecord;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.LengthRecords.LengthRecord;
import com.eyebrowssoftware.example.db.NoteRecords.NoteRecord;
import com.eyebrowssoftware.example.db.NoteTypeRecords.NoteTypeRecord;
import com.eyebrowssoftware.example.db.PingRecords.PingRecord;
import com.eyebrowssoftware.example.db.PulseRecords.PulseRecord;
import com.eyebrowssoftware.example.db.TemperatureRecords.TemperatureRecord;
import com.eyebrowssoftware.example.db.TemperatureTypeRecords.TemperatureTypeRecord;
import com.eyebrowssoftware.example.db.VisitItemRecords.VisitItemRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;
import com.eyebrowssoftware.example.db.WeightRecords.WeightRecord;

import java.util.HashMap;


public class PCProvider extends ContentProvider {
	@SuppressWarnings("unused")
	private static final String TAG = "PointCareProvider";

	public static final String AUTHORITY = "com.eyebrowssoftware.example.db.provider";
	public static final String URI_STRING = "content://" + AUTHORITY;

	private static final String DATABASE_NAME = "exampledb.db";
	private static final int DB_VERSION_1 = 1;
	private static final int DB_VERSION_2 = 2;
	private static final int DATABASE_VERSION = DB_VERSION_1;
	
	private static final String PINGS_TABLE_NAME = "pings";
	private static final String NOTE_TYPES_TABLE_NAME = "note_types";
	private static final String TEMPERATURE_TYPES_TABLE_NAME = "temperature_types";
	private static final String CLIENTS_TABLE_NAME = "clients";
	private static final String VISIT_ITEMS_TABLE_NAME = "visit_items";
	private static final String VISITS_TABLE_NAME = "client_visits";
	private static final String NOTES_TABLE_NAME = "client_notes";
	private static final String TEMPERATURES_TABLE_NAME = "client_temperatures";
	private static final String BPS_TABLE_NAME = "visit_bps";
	private static final String LENGTHS_TABLE_NAME = "client_lengths";
	private static final String PULSES_TABLE_NAME = "visit_pulses";
	private static final String WEIGHTS_TABLE_NAME = "visit_weights";

	private static HashMap<String, String> sPingProjectionMap;
	private static HashMap<String, String> sNoteTypeProjectionMap;
	private static HashMap<String, String> sVisitItemProjectionMap;
	private static HashMap<String, String> sTemperatureTypeProjectionMap;
	private static HashMap<String, String> sClientProjectionMap;
	private static HashMap<String, String> sVisitProjectionMap;
	private static HashMap<String, String> sNoteProjectionMap;
	private static HashMap<String, String> sTemperatureProjectionMap;
	private static HashMap<String, String> sClientVisitTemperatureProjectionMap;
	private static HashMap<String, String> sClientVisitBPProjectionMap;
	private static HashMap<String, String> sClientVisitLengthProjectionMap;
	private static HashMap<String, String> sClientVisitPulseProjectionMap;
	private static HashMap<String, String> sClientVisitWeightProjectionMap;

	private static final UriMatcher sUriMatcher;

	public static final Uri CONTENT_URI = Uri.parse(URI_STRING);

	private ContentResolver mCR;

	private static final int PINGS = 0;
	private static final int PING_ID = 1;
	private static final int NOTE_TYPES = 2;
	private static final int NOTE_TYPE_ID = 3;
	private static final int CLIENTS = 4;
	private static final int CLIENT_ID = 5;
	private static final int NOTES = 6;
	private static final int NOTE_ID = 7;
	private static final int TEMPERATURES = 8;
	private static final int TEMPERATURE_ID = 9;
	private static final int TEMPERATURE_TYPES = 10;
	private static final int TEMPERATURE_TYPE_ID = 11;
	private static final int VISITS = 12;
	private static final int VISIT_ID = 13;
	private static final int CLIENT_NOTES = 14;
	private static final int CLIENT_NOTE_ID = 15;
	private static final int CLIENT_VISITS = 16;
	private static final int CLIENT_VISIT_ID = 17;
	private static final int VISIT_ITEMS = 18;
	private static final int VISIT_ITEM_ID = 19;
	private static final int CLIENT_VISIT_TEMPERATURES = 20;
	private static final int CLIENT_VISIT_TEMPERATURE_ID = 21;
	private static final int CLIENT_VISIT_BPS = 22;
	private static final int CLIENT_VISIT_BP_ID = 23;
	private static final int CLIENT_VISIT_LENGTHS = 24;
	private static final int CLIENT_VISIT_LENGTH_ID = 25;
	private static final int CLIENT_VISIT_PULSES = 26;
	private static final int CLIENT_VISIT_PULSE_ID = 27;
	private static final int CLIENT_VISIT_WEIGHTS = 28;
	private static final int CLIENT_VISIT_WEIGHT_ID = 29;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "pings", PINGS);
		sUriMatcher.addURI(AUTHORITY, "pings/#", PING_ID);
		sUriMatcher.addURI(AUTHORITY, "note_types", NOTE_TYPES);
		sUriMatcher.addURI(AUTHORITY, "note_types/#", NOTE_TYPE_ID);
		sUriMatcher.addURI(AUTHORITY, "clients", CLIENTS);
		sUriMatcher.addURI(AUTHORITY, "clients/#", CLIENT_ID);
		sUriMatcher.addURI(AUTHORITY, "notes", NOTES);
		sUriMatcher.addURI(AUTHORITY, "notes/#", NOTE_ID);
		sUriMatcher.addURI(AUTHORITY, "temperatures", TEMPERATURES);
		sUriMatcher.addURI(AUTHORITY, "temperatures/#", TEMPERATURE_ID);
		sUriMatcher.addURI(AUTHORITY, "temperature_types", TEMPERATURE_TYPES);
		sUriMatcher.addURI(AUTHORITY, "temperature_types/#", TEMPERATURE_TYPE_ID);
		sUriMatcher.addURI(AUTHORITY, "visits", VISITS);
		sUriMatcher.addURI(AUTHORITY, "visits/#", VISIT_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/notes", CLIENT_NOTES);
		sUriMatcher.addURI(AUTHORITY, "clients/#/notes/#", CLIENT_NOTE_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits", CLIENT_VISITS);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#", CLIENT_VISIT_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/temperatures", CLIENT_VISIT_TEMPERATURES);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/temperatures/#", CLIENT_VISIT_TEMPERATURE_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/blood_pressures", CLIENT_VISIT_BPS);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/blood_pressures/#", CLIENT_VISIT_BP_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/lengths", CLIENT_VISIT_LENGTHS);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/lengths/#", CLIENT_VISIT_LENGTH_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/pulses", CLIENT_VISIT_PULSES);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/pulses/#", CLIENT_VISIT_PULSE_ID);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/weights", CLIENT_VISIT_WEIGHTS);
		sUriMatcher.addURI(AUTHORITY, "clients/#/visits/#/weights/#", CLIENT_VISIT_WEIGHT_ID);
		sUriMatcher.addURI(AUTHORITY, "visit_items", VISIT_ITEMS);
		sUriMatcher.addURI(AUTHORITY, "visit_items/#", VISIT_ITEM_ID);

		sPingProjectionMap = new HashMap<>();
		sPingProjectionMap.put(PingRecord._ID, PINGS_TABLE_NAME+"."+PingRecord._ID);
		sPingProjectionMap.put(PingRecord.IP_ADDRESS, PINGS_TABLE_NAME+"."+PingRecord.IP_ADDRESS);
		sPingProjectionMap.put(PingRecord.PORT, PINGS_TABLE_NAME+"."+PingRecord.PORT);
		sPingProjectionMap.put(PingRecord.TIMEOUT, PINGS_TABLE_NAME+"."+PingRecord.TIMEOUT);
		sPingProjectionMap.put(PingRecord.COUNT, PINGS_TABLE_NAME+"."+PingRecord.COUNT);
		sPingProjectionMap.put(PingRecord.SUCCESS, PINGS_TABLE_NAME+"."+PingRecord.SUCCESS);
		sPingProjectionMap.put(PingRecord.STATUS, PINGS_TABLE_NAME+"."+PingRecord.STATUS);
		sPingProjectionMap.put(PingRecord.CREATED_DATE, PINGS_TABLE_NAME+"."+PingRecord.CREATED_DATE);
		sPingProjectionMap.put(PingRecord.MODIFIED_DATE, PINGS_TABLE_NAME+"."+PingRecord.MODIFIED_DATE);

		sVisitItemProjectionMap = new HashMap<>();
		sVisitItemProjectionMap.put(VisitItemRecord._ID, VisitItemRecord._ID);
		sVisitItemProjectionMap.put(VisitItemRecord.NAME, VisitItemRecord.NAME);
		sVisitItemProjectionMap.put(VisitItemRecord.CREATED_DATE, VisitItemRecord.CREATED_DATE);
		sVisitItemProjectionMap.put(VisitItemRecord.MODIFIED_DATE, VisitItemRecord.MODIFIED_DATE);
		
		sNoteTypeProjectionMap = new HashMap<>();
		sNoteTypeProjectionMap.put(NoteTypeRecord._ID, 
				NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord._ID);
		sNoteTypeProjectionMap.put(NoteTypeRecord.NAME, 
				NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord.NAME);
		sNoteTypeProjectionMap.put(NoteTypeRecord.TEMPLATE, 
				NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord.TEMPLATE);
		sNoteTypeProjectionMap.put(NoteTypeRecord.CREATED_DATE, 
				NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord.CREATED_DATE);
		sNoteTypeProjectionMap.put(NoteTypeRecord.MODIFIED_DATE, 
				NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord.MODIFIED_DATE);
		
		sTemperatureTypeProjectionMap = new HashMap<>();
		sTemperatureTypeProjectionMap.put(TemperatureTypeRecord._ID, 
				TEMPERATURE_TYPES_TABLE_NAME+"."+TemperatureTypeRecord._ID);
		sTemperatureTypeProjectionMap.put(TemperatureTypeRecord.NAME, 
				TEMPERATURE_TYPES_TABLE_NAME+"."+TemperatureTypeRecord.NAME);
		sTemperatureTypeProjectionMap.put(TemperatureTypeRecord.CREATED_DATE, 
				TEMPERATURE_TYPES_TABLE_NAME+"."+TemperatureTypeRecord.CREATED_DATE);
		sTemperatureTypeProjectionMap.put(TemperatureTypeRecord.MODIFIED_DATE, 
				TEMPERATURE_TYPES_TABLE_NAME+"."+TemperatureTypeRecord.MODIFIED_DATE);
		
		sClientProjectionMap = new HashMap<>();
		sClientProjectionMap.put(ClientRecord._ID, 
				CLIENTS_TABLE_NAME+"."+ClientRecord._ID);
		sClientProjectionMap.put(ClientRecord.FIRST_NAME, 
				CLIENTS_TABLE_NAME+"."+ClientRecord.FIRST_NAME);
		sClientProjectionMap.put(ClientRecord.LAST_NAME, 
				CLIENTS_TABLE_NAME+"."+ClientRecord.LAST_NAME);
		sClientProjectionMap.put(ClientRecord.CREATED_DATE, 
				CLIENTS_TABLE_NAME+"."+ClientRecord.CREATED_DATE);
		sClientProjectionMap.put(ClientRecord.MODIFIED_DATE, 
				CLIENTS_TABLE_NAME+"."+ClientRecord.MODIFIED_DATE);
		
		sVisitProjectionMap = new HashMap<>();
		sVisitProjectionMap.put(VisitRecord._ID, VISITS_TABLE_NAME+"."+VisitRecord._ID);
		sVisitProjectionMap.put(VisitRecord.CLIENT_ID, VISITS_TABLE_NAME+"."+VisitRecord.CLIENT_ID);
		sVisitProjectionMap.put(VisitRecord.DATE, VISITS_TABLE_NAME+"."+VisitRecord.DATE);
		sVisitProjectionMap.put(VisitRecord.CREATED_DATE, VISITS_TABLE_NAME+"."+VisitRecord.CREATED_DATE);
		sVisitProjectionMap.put(VisitRecord.MODIFIED_DATE, VISITS_TABLE_NAME+"."+VisitRecord.MODIFIED_DATE);
		// These values provide access to the join table information for ClientRecord and ClientNoteTypeRecord
		sVisitProjectionMap.put(ClientRecord.FIRST_NAME, CLIENTS_TABLE_NAME+"."+ClientRecord.FIRST_NAME);
		sVisitProjectionMap.put(ClientRecord.LAST_NAME, CLIENTS_TABLE_NAME+"."+ClientRecord.LAST_NAME);
		
		sNoteProjectionMap = new HashMap<>();
		sNoteProjectionMap.put(NoteRecord._ID, NOTES_TABLE_NAME+"."+NoteRecord._ID);
		sNoteProjectionMap.put(NoteRecord.CLIENT_ID,NOTES_TABLE_NAME+"."+NoteRecord.CLIENT_ID);
		sNoteProjectionMap.put(NoteRecord.DATE, NOTES_TABLE_NAME+"."+NoteRecord.DATE);
		sNoteProjectionMap.put(NoteRecord.TYPE_ID, NOTES_TABLE_NAME+"."+NoteRecord.TYPE_ID);
		sNoteProjectionMap.put(NoteRecord.TEXT, NOTES_TABLE_NAME+"."+NoteRecord.TEXT);
		sNoteProjectionMap.put(NoteRecord.CREATED_DATE,NOTES_TABLE_NAME+"."+NoteRecord.CREATED_DATE);
		sNoteProjectionMap.put(NoteRecord.MODIFIED_DATE,NOTES_TABLE_NAME+"."+NoteRecord.MODIFIED_DATE);
		// These values provide access to the join table information for ClientRecord and ClientNoteTypeRecord
		sNoteProjectionMap.put(ClientRecord.FIRST_NAME, CLIENTS_TABLE_NAME+"."+ClientRecord.FIRST_NAME);
		sNoteProjectionMap.put(ClientRecord.LAST_NAME, CLIENTS_TABLE_NAME+"."+ClientRecord.LAST_NAME);
		sNoteProjectionMap.put(NoteTypeRecord.NAME, NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord.NAME);
		sNoteProjectionMap.put(NoteTypeRecord.TEMPLATE,NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord.TEMPLATE);
		
		sTemperatureProjectionMap = new HashMap<>();
		sTemperatureProjectionMap.put(TemperatureRecord._ID, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord._ID);
		sTemperatureProjectionMap.put(TemperatureRecord.VISIT_ID, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.VISIT_ID);
		sTemperatureProjectionMap.put(TemperatureRecord.TYPE_ID, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.TYPE_ID);
		sTemperatureProjectionMap.put(TemperatureRecord.DATE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.DATE);
		sTemperatureProjectionMap.put(TemperatureRecord.TEMPERATURE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.TEMPERATURE);
		sTemperatureProjectionMap.put(TemperatureRecord.OVER_RANGE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.OVER_RANGE);
		sTemperatureProjectionMap.put(TemperatureRecord.CREATED_DATE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.CREATED_DATE);
		sTemperatureProjectionMap.put(TemperatureRecord.MODIFIED_DATE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.MODIFIED_DATE);

		sClientVisitTemperatureProjectionMap = new HashMap<>();
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord._ID, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord._ID);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.VISIT_ID, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.VISIT_ID);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.TYPE_ID, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.TYPE_ID);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.DATE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.DATE);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.TEMPERATURE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.TEMPERATURE);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.OVER_RANGE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.OVER_RANGE);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.CREATED_DATE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.CREATED_DATE);
		sClientVisitTemperatureProjectionMap.put(TemperatureRecord.MODIFIED_DATE, 
				TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.MODIFIED_DATE);
		sClientVisitTemperatureProjectionMap.put(TemperatureTypeRecord.NAME,
				TEMPERATURE_TYPES_TABLE_NAME+"."+TemperatureTypeRecord.NAME);

		sClientVisitBPProjectionMap = new HashMap<>();
		sClientVisitBPProjectionMap.put(BPRecord._ID, 
				BPS_TABLE_NAME+"."+BPRecord._ID);
		sClientVisitBPProjectionMap.put(BPRecord.VISIT_ID, 
				BPS_TABLE_NAME+"."+BPRecord.VISIT_ID);
		sClientVisitBPProjectionMap.put(BPRecord.DATE, 
				BPS_TABLE_NAME+"."+BPRecord.DATE);
		sClientVisitBPProjectionMap.put(BPRecord.SYSTOLIC, 
				BPS_TABLE_NAME+"."+BPRecord.SYSTOLIC);
		sClientVisitBPProjectionMap.put(BPRecord.DIASTOLIC, 
				BPS_TABLE_NAME+"."+BPRecord.DIASTOLIC);
		sClientVisitBPProjectionMap.put(BPRecord.CREATED_DATE, 
				BPS_TABLE_NAME+"."+BPRecord.CREATED_DATE);
		sClientVisitBPProjectionMap.put(BPRecord.MODIFIED_DATE, 
				BPS_TABLE_NAME+"."+BPRecord.MODIFIED_DATE);

		sClientVisitLengthProjectionMap = new HashMap<>();
		sClientVisitLengthProjectionMap.put(LengthRecord._ID, 
				LENGTHS_TABLE_NAME+"."+LengthRecord._ID);
		sClientVisitLengthProjectionMap.put(LengthRecord.VISIT_ID, 
				LENGTHS_TABLE_NAME+"."+LengthRecord.VISIT_ID);
		sClientVisitLengthProjectionMap.put(LengthRecord.DATE, 
				LENGTHS_TABLE_NAME+"."+LengthRecord.DATE);
		sClientVisitLengthProjectionMap.put(LengthRecord.LENGTH, 
				LENGTHS_TABLE_NAME+"."+LengthRecord.LENGTH);
		sClientVisitLengthProjectionMap.put(LengthRecord.CREATED_DATE, 
				LENGTHS_TABLE_NAME+"."+LengthRecord.CREATED_DATE);
		sClientVisitLengthProjectionMap.put(LengthRecord.MODIFIED_DATE, 
				LENGTHS_TABLE_NAME+"."+LengthRecord.MODIFIED_DATE);

		sClientVisitPulseProjectionMap = new HashMap<>();
		sClientVisitPulseProjectionMap.put(PulseRecord._ID, 
				PULSES_TABLE_NAME+"."+PulseRecord._ID);
		sClientVisitPulseProjectionMap.put(PulseRecord.VISIT_ID, 
				PULSES_TABLE_NAME+"."+PulseRecord.VISIT_ID);
		sClientVisitPulseProjectionMap.put(PulseRecord.DATE, 
				PULSES_TABLE_NAME+"."+PulseRecord.DATE);
		sClientVisitPulseProjectionMap.put(PulseRecord.PULSE, 
				PULSES_TABLE_NAME+"."+PulseRecord.PULSE);
		sClientVisitPulseProjectionMap.put(PulseRecord.CREATED_DATE, 
				PULSES_TABLE_NAME+"."+PulseRecord.CREATED_DATE);
		sClientVisitPulseProjectionMap.put(PulseRecord.MODIFIED_DATE, 
				PULSES_TABLE_NAME+"."+PulseRecord.MODIFIED_DATE);

		sClientVisitWeightProjectionMap = new HashMap<>();
		sClientVisitWeightProjectionMap.put(WeightRecord._ID, 
				WEIGHTS_TABLE_NAME+"."+WeightRecord._ID);
		sClientVisitWeightProjectionMap.put(WeightRecord.VISIT_ID, 
				WEIGHTS_TABLE_NAME+"."+WeightRecord.VISIT_ID);
		sClientVisitWeightProjectionMap.put(WeightRecord.DATE, 
				WEIGHTS_TABLE_NAME+"."+WeightRecord.DATE);
		sClientVisitWeightProjectionMap.put(WeightRecord.WEIGHT, 
				WEIGHTS_TABLE_NAME+"."+WeightRecord.WEIGHT);
		sClientVisitWeightProjectionMap.put(WeightRecord.CREATED_DATE, 
				WEIGHTS_TABLE_NAME+"."+PulseRecord.CREATED_DATE);
		sClientVisitWeightProjectionMap.put(WeightRecord.MODIFIED_DATE, 
				WEIGHTS_TABLE_NAME+"."+WeightRecord.MODIFIED_DATE);
	}
	
	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + PINGS_TABLE_NAME + " ("
                    + PingRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + PingRecord.IP_ADDRESS + " TEXT,"
                    + PingRecord.PORT + " INTEGER,"
                    + PingRecord.TIMEOUT + " INTEGER,"
                    + PingRecord.COUNT + " INTEGER,"
                    + PingRecord.SUCCESS + " INTEGER,"
                    + PingRecord.STATUS + " INTEGER,"
                    + PingRecord.CREATED_DATE + " INTEGER,"
                    + PingRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + NOTE_TYPES_TABLE_NAME + " ("
                    + NoteTypeRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NoteTypeRecord.NAME + " TEXT,"
                    + NoteTypeRecord.TEMPLATE + " TEXT,"
                    + NoteTypeRecord.CREATED_DATE + " INTEGER,"
                    + NoteTypeRecord.MODIFIED_DATE + " INTEGER"
                    + ");");


            db.execSQL("CREATE TABLE " + TEMPERATURE_TYPES_TABLE_NAME + " ("
                    + TemperatureTypeRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TemperatureTypeRecord.NAME + " TEXT,"
                    + TemperatureTypeRecord.CREATED_DATE + " INTEGER,"
                    + TemperatureTypeRecord.MODIFIED_DATE + " INTEGER"
                    + ");");


            db.execSQL("CREATE TABLE " + CLIENTS_TABLE_NAME + " ("
                    + ClientRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ClientRecord.FIRST_NAME + " TEXT,"
                    + ClientRecord.LAST_NAME + " TEXT,"
                    + ClientRecord.CREATED_DATE + " INTEGER,"
                    + ClientRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + VISITS_TABLE_NAME + " ("
                    + NoteRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NoteRecord.CLIENT_ID + " INTEGER REFERENCES clients(_id),"
                    + NoteRecord.DATE + " INTEGER,"
                    + NoteRecord.CREATED_DATE + " INTEGER,"
                    + NoteRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + " ("
                    + NoteRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NoteRecord.CLIENT_ID + " INTEGER REFERENCES clients(_id),"
                    + NoteRecord.TYPE_ID + " INTEGER REFERENCES note_types(_id),"
                    + NoteRecord.DATE + " INTEGER,"
                    + NoteRecord.TEXT + " TEXT,"
                    + NoteRecord.CREATED_DATE + " INTEGER,"
                    + NoteRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + TEMPERATURES_TABLE_NAME + " ("
                    + TemperatureRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TemperatureRecord.VISIT_ID + " INTEGER REFERENCES client_visits(_id),"
                    + TemperatureRecord.TYPE_ID + " INTEGER REFERENCES temperature_types(_id),"
                    + TemperatureRecord.DATE + " INTEGER,"
                    + TemperatureRecord.TEMPERATURE + " REAL,"
                    + TemperatureRecord.OVER_RANGE + " INTEGER,"
                    + TemperatureRecord.CREATED_DATE + " INTEGER,"
                    + TemperatureRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + BPS_TABLE_NAME + " ("
                    + BPRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BPRecord.VISIT_ID + " INTEGER REFERENCES client_visits(_id),"
                    + BPRecord.DATE + " INTEGER,"
                    + BPRecord.SYSTOLIC + " INTEGER,"
                    + BPRecord.DIASTOLIC + " INTEGER,"
                    + BPRecord.CREATED_DATE + " INTEGER,"
                    + BPRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + LENGTHS_TABLE_NAME + " ("
                    + LengthRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LengthRecord.VISIT_ID + " INTEGER REFERENCES client_visits(_id),"
                    + LengthRecord.DATE + " INTEGER,"
                    + LengthRecord.LENGTH + " INTEGER,"
                    + LengthRecord.CREATED_DATE + " INTEGER,"
                    + LengthRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + PULSES_TABLE_NAME + " ("
                    + PulseRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + PulseRecord.VISIT_ID + " INTEGER REFERENCES client_visits(_id),"
                    + PulseRecord.DATE + " INTEGER,"
                    + PulseRecord.PULSE + " INTEGER,"
                    + PulseRecord.CREATED_DATE + " INTEGER,"
                    + PulseRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + WEIGHTS_TABLE_NAME + " ("
                    + WeightRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + WeightRecord.VISIT_ID + " INTEGER REFERENCES client_visits(_id),"
                    + WeightRecord.DATE + " INTEGER,"
                    + WeightRecord.WEIGHT + " INTEGER,"
                    + WeightRecord.CREATED_DATE + " INTEGER,"
                    + WeightRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + VISIT_ITEMS_TABLE_NAME + " ("
                    + VisitItemRecord._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + VisitItemRecord.NAME + " TEXT,"
                    + VisitItemRecord.CREATED_DATE + " INTEGER,"
                    + VisitItemRecord.MODIFIED_DATE + " INTEGER"
                    + ");");

            createNoteTypes(db);
            createTemperatureTypes(db);
            createVisitTypes(db);
        }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion == DB_VERSION_1) {
				if(newVersion == DB_VERSION_2) {
                    Log.e(TAG, "No version 2 available");
				}
			}
		}
	}

    /**
     * Create the canned note types. In real life this would come from a sync
     *
     */
    private static void createNoteTypes(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        long now;

        cv.put(NoteTypeRecord.NAME, "TYPE 1");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 1 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(NoteTypeRecord.NAME, "TYPE 2");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 2 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(NoteTypeRecord.NAME, "TYPE 3");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 3 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(NoteTypeRecord.NAME, "TYPE 4");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 4 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(NoteTypeRecord.NAME, "TYPE 5");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 5 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(NoteTypeRecord.NAME, "TYPE 6");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 6 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(NoteTypeRecord.NAME, "TYPE 7");
        cv.put(NoteTypeRecord.TEMPLATE, "TYPE 7 TEMPLATE");
        now = System.currentTimeMillis();
        cv.put(NoteTypeRecord.CREATED_DATE, now);
        cv.put(NoteTypeRecord.MODIFIED_DATE, now);
        db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.CREATED_DATE, cv);


    }

    private static void createTemperatureTypes(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        long now;

        cv.clear();
        cv.put(TemperatureTypeRecord.NAME, "AXILARY");
        now = System.currentTimeMillis();
        cv.put(TemperatureTypeRecord.CREATED_DATE, now);
        cv.put(TemperatureTypeRecord.MODIFIED_DATE, now);
        db.insert(TEMPERATURE_TYPES_TABLE_NAME, TemperatureTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(TemperatureTypeRecord.NAME, "OTIC");
        now = System.currentTimeMillis();
        cv.put(TemperatureTypeRecord.CREATED_DATE, now);
        cv.put(TemperatureTypeRecord.MODIFIED_DATE, now);
        db.insert(TEMPERATURE_TYPES_TABLE_NAME, TemperatureTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(TemperatureTypeRecord.NAME, "TEMPORAL");
        now = System.currentTimeMillis();
        cv.put(TemperatureTypeRecord.CREATED_DATE, now);
        cv.put(TemperatureTypeRecord.MODIFIED_DATE, now);
        db.insert(TEMPERATURE_TYPES_TABLE_NAME, TemperatureTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(TemperatureTypeRecord.NAME, "RECTAL");
        now = System.currentTimeMillis();
        cv.put(TemperatureTypeRecord.CREATED_DATE, now);
        cv.put(TemperatureTypeRecord.MODIFIED_DATE, now);
        db.insert(TEMPERATURE_TYPES_TABLE_NAME, TemperatureTypeRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(TemperatureTypeRecord.NAME, "ORAL");
        now = System.currentTimeMillis();
        cv.put(TemperatureTypeRecord.CREATED_DATE, now);
        cv.put(TemperatureTypeRecord.MODIFIED_DATE, now);
        db.insert(TEMPERATURE_TYPES_TABLE_NAME, TemperatureTypeRecord.CREATED_DATE, cv);

    }

    private static void createVisitTypes(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        long now;

        cv.put(VisitItemRecord.NAME, "TEMPERATURE");
        now = System.currentTimeMillis();
        cv.put(VisitItemRecord.CREATED_DATE, now);
        cv.put(VisitItemRecord.MODIFIED_DATE, now);
        db.insert(VISIT_ITEMS_TABLE_NAME, VisitItemRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(VisitItemRecord.NAME, "LENGTH");
        now = System.currentTimeMillis();
        cv.put(VisitItemRecord.CREATED_DATE, now);
        cv.put(VisitItemRecord.MODIFIED_DATE, now);
        db.insert(VISIT_ITEMS_TABLE_NAME, VisitItemRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(VisitItemRecord.NAME, "WEIGHT");
        now = System.currentTimeMillis();
        cv.put(VisitItemRecord.CREATED_DATE, now);
        cv.put(VisitItemRecord.MODIFIED_DATE, now);
        db.insert(VISIT_ITEMS_TABLE_NAME, VisitItemRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(VisitItemRecord.NAME, "BP");
        now = System.currentTimeMillis();
        cv.put(VisitItemRecord.CREATED_DATE, now);
        cv.put(VisitItemRecord.MODIFIED_DATE, now);
        db.insert(VISIT_ITEMS_TABLE_NAME, VisitItemRecord.CREATED_DATE, cv);

        cv.clear();
        cv.put(VisitItemRecord.NAME, "PULSE");
        now = System.currentTimeMillis();
        cv.put(VisitItemRecord.CREATED_DATE, now);
        cv.put(VisitItemRecord.MODIFIED_DATE, now);
        db.insert(VISIT_ITEMS_TABLE_NAME, VisitItemRecord.CREATED_DATE, cv);
    }

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		Context c = getContext();
		mOpenHelper = new DatabaseHelper(c);
		mCR = c.getContentResolver();
		return true;
	}

	@Override
	public void finalize() throws Throwable {
		mOpenHelper.close();
        super.finalize();
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case PINGS:
			return PingRecords.CONTENT_TYPE;
		case PING_ID:
			return PingRecord.CONTENT_ITEM_TYPE;
		case NOTE_TYPES:
			return NoteTypeRecords.CONTENT_TYPE;
		case NOTE_TYPE_ID:
			return NoteTypeRecord.CONTENT_ITEM_TYPE;
		case CLIENTS:
			return ClientRecords.CONTENT_TYPE;
		case CLIENT_ID:
			return ClientRecord.CONTENT_ITEM_TYPE;
		case VISITS:
			return VisitRecords.CONTENT_TYPE;
		case VISIT_ID:
			return VisitRecord.CONTENT_ITEM_TYPE;
		case NOTES:
			return NoteRecords.CONTENT_TYPE;
		case NOTE_ID:
			return NoteRecord.CONTENT_ITEM_TYPE;
		case TEMPERATURE_TYPES:
			return TemperatureTypeRecords.CONTENT_TYPE;
		case TEMPERATURE_TYPE_ID:
			return TemperatureTypeRecord.CONTENT_ITEM_TYPE;
		case TEMPERATURES:
			return TemperatureRecords.CONTENT_TYPE;
		case TEMPERATURE_ID:
			return TemperatureRecord.CONTENT_ITEM_TYPE;
		case VISIT_ITEMS:
			return VisitItemRecords.CONTENT_TYPE;
		case VISIT_ITEM_ID:
			return VisitItemRecord.CONTENT_ITEM_TYPE;
		case CLIENT_VISIT_TEMPERATURES:
			return TemperatureTypeRecords.CONTENT_TYPE;
		case CLIENT_VISIT_TEMPERATURE_ID:
			return TemperatureTypeRecord.CONTENT_ITEM_TYPE;
		case CLIENT_VISIT_BPS:
			return BPRecords.CONTENT_TYPE;
		case CLIENT_VISIT_BP_ID:
			return BPRecord.CONTENT_ITEM_TYPE;
		case CLIENT_VISIT_LENGTHS:
			return LengthRecords.CONTENT_TYPE;
		case CLIENT_VISIT_LENGTH_ID:
			return LengthRecord.CONTENT_ITEM_TYPE;
		case CLIENT_VISIT_PULSES:
			return PulseRecords.CONTENT_TYPE;
		case CLIENT_VISIT_PULSE_ID:
			return PulseRecord.CONTENT_ITEM_TYPE;
		case CLIENT_VISIT_WEIGHTS:
			return WeightRecords.CONTENT_TYPE;
		case CLIENT_VISIT_WEIGHT_ID:
			return WeightRecord.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	private static final String VISIT_FULL_ID = VISITS_TABLE_NAME+"."+VisitRecord._ID;
	
	private static final String VISIT_FULL_CLIENT_ID = VISITS_TABLE_NAME+"."+VisitRecord.CLIENT_ID;
	
	private static final String NOTE_FULL_ID = NOTES_TABLE_NAME+"."+NoteRecord._ID;
	
	@SuppressWarnings("unused")
	private static final String NOTE_FULL_TYPE_ID = NOTES_TABLE_NAME+"."+NoteRecord.TYPE_ID;
	
	private static final String NOTE_FULL_CLIENT_ID = NOTES_TABLE_NAME+"."+NoteRecord.CLIENT_ID;
	
	private static final String NOTE_TYPE_FULL_ID = NOTE_TYPES_TABLE_NAME+"."+NoteTypeRecord._ID;
	
	private static final String CLIENT_FULL_ID = CLIENTS_TABLE_NAME+"."+ClientRecord._ID;
	
	private static final String TEMPERATURE_TYPE_FULL_ID = 
		TEMPERATURE_TYPES_TABLE_NAME+"."+TemperatureTypeRecord._ID;
	
	private static final String TEMPERATURE_FULL_TYPE_ID = 
		TEMPERATURES_TABLE_NAME+"."+TemperatureRecord.TYPE_ID;
	
	@SuppressWarnings("unused")
	private static final String VISIT_TEMPERATURE_FULL_ID = 
		TEMPERATURES_TABLE_NAME+"."+TemperatureRecord._ID;

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor c;

		switch (sUriMatcher.match(uri)) {
		// Result is a page of bp_records
		case PINGS:
			qb.setTables(PINGS_TABLE_NAME);
			qb.setProjectionMap(sPingProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		// Result is a single bp_record
		case PING_ID:
			qb.setTables(PINGS_TABLE_NAME);
			qb.setProjectionMap(sPingProjectionMap);
			qb.appendWhere(PingRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case NOTE_TYPES:
			qb.setTables(NOTE_TYPES_TABLE_NAME);
			qb.setProjectionMap(sNoteTypeProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case NOTE_TYPE_ID:
			qb.setTables(NOTE_TYPES_TABLE_NAME);
			qb.setProjectionMap(sNoteTypeProjectionMap);
			qb.appendWhere(NoteTypeRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENTS:
			qb.setTables(CLIENTS_TABLE_NAME);
			qb.setProjectionMap(sClientProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_ID:
			qb.setTables(CLIENTS_TABLE_NAME);
			qb.setProjectionMap(sClientProjectionMap);
			qb.appendWhere(ClientRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case VISITS:
			qb.setTables(CLIENTS_TABLE_NAME + "," + VISITS_TABLE_NAME);
			qb.setProjectionMap(sVisitProjectionMap);
			qb.appendWhere(VISIT_FULL_CLIENT_ID + " = " + CLIENT_FULL_ID);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case VISIT_ID:
			qb.setTables(VISITS_TABLE_NAME);
			qb.setProjectionMap(sVisitProjectionMap);
			qb.appendWhere(VISIT_FULL_CLIENT_ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISITS:
			qb.setTables(VISITS_TABLE_NAME);
			qb.setProjectionMap(sVisitProjectionMap);
			qb.appendWhere(VISIT_FULL_CLIENT_ID+"=" + Long.valueOf(uri.getPathSegments().get(1)));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_ID:
			qb.setTables(VISITS_TABLE_NAME);
			qb.setProjectionMap(sVisitProjectionMap);
			qb.appendWhere(VISIT_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1))
					+ " AND " + VISIT_FULL_ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case NOTES:
			qb.setTables(CLIENTS_TABLE_NAME + "," + NOTES_TABLE_NAME + "," + NOTE_TYPES_TABLE_NAME);
			qb.setProjectionMap(sNoteProjectionMap);
			qb.appendWhere("("
				+ NoteRecord.CLIENT_ID + "=" + CLIENT_FULL_ID + " AND "
				+ NoteRecord.TYPE_ID + "=" + NOTE_TYPE_FULL_ID + ")");
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case NOTE_ID:
			qb.setTables(NOTES_TABLE_NAME);
			qb.setProjectionMap(sNoteProjectionMap);
			qb.appendWhere(NOTE_FULL_ID + " = " + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_NOTES:
			qb.setTables(NOTES_TABLE_NAME);
			qb.setProjectionMap(sNoteProjectionMap);
			qb.appendWhere(NOTE_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1)));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_NOTE_ID:
			qb.setTables(NOTES_TABLE_NAME);
			qb.setProjectionMap(sNoteProjectionMap);
			qb.appendWhere(NOTE_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1))
					+ " AND " + NOTE_FULL_ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case TEMPERATURE_TYPES:
			qb.setTables(TEMPERATURE_TYPES_TABLE_NAME);
			qb.setProjectionMap(sTemperatureTypeProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case TEMPERATURE_TYPE_ID:
			qb.setTables(TEMPERATURE_TYPES_TABLE_NAME);
			qb.setProjectionMap(sTemperatureTypeProjectionMap);
			qb.appendWhere(TemperatureTypeRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case TEMPERATURES:
			qb.setTables(TEMPERATURES_TABLE_NAME);
			qb.setProjectionMap(sTemperatureProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case TEMPERATURE_ID:
			qb.setTables(TEMPERATURES_TABLE_NAME);
			qb.setProjectionMap(sTemperatureProjectionMap);
			qb.appendWhere(TemperatureRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_TEMPERATURES:
			qb.setTables(TEMPERATURES_TABLE_NAME + "," + TEMPERATURE_TYPES_TABLE_NAME);
			qb.setProjectionMap(sClientVisitTemperatureProjectionMap);
			qb.appendWhere(TemperatureRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + TEMPERATURE_FULL_TYPE_ID + "=" + TEMPERATURE_TYPE_FULL_ID);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_TEMPERATURE_ID:
			qb.setTables(TEMPERATURES_TABLE_NAME);
			qb.setProjectionMap(sTemperatureProjectionMap);
			qb.appendWhere(TemperatureRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + TemperatureRecord.TYPE_ID + "=" + TemperatureTypeRecord._ID
				+ " AND " + TemperatureRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_BPS:
			qb.setTables(BPS_TABLE_NAME);
			qb.setProjectionMap(sClientVisitBPProjectionMap);
			qb.appendWhere(BPRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3)));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_BP_ID:
			qb.setTables(BPS_TABLE_NAME);
			qb.setProjectionMap(sClientVisitBPProjectionMap);
			qb.appendWhere(BPRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + BPRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_LENGTHS:
			qb.setTables(LENGTHS_TABLE_NAME);
			qb.setProjectionMap(sClientVisitLengthProjectionMap);
			qb.appendWhere(LengthRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3)));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_LENGTH_ID:
			qb.setTables(LENGTHS_TABLE_NAME);
			qb.setProjectionMap(sClientVisitLengthProjectionMap);
			qb.appendWhere(LengthRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + LengthRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_PULSES:
			qb.setTables(PULSES_TABLE_NAME);
			qb.setProjectionMap(sClientVisitPulseProjectionMap);
			qb.appendWhere(PulseRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3)));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_PULSE_ID:
			qb.setTables(PULSES_TABLE_NAME);
			qb.setProjectionMap(sClientVisitPulseProjectionMap);
			qb.appendWhere(PulseRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + PulseRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_WEIGHTS:
			qb.setTables(WEIGHTS_TABLE_NAME);
			qb.setProjectionMap(sClientVisitWeightProjectionMap);
			qb.appendWhere(WeightRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3)));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case CLIENT_VISIT_WEIGHT_ID:
			qb.setTables(WEIGHTS_TABLE_NAME);
			qb.setProjectionMap(sClientVisitWeightProjectionMap);
			qb.appendWhere(WeightRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + WeightRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case VISIT_ITEMS:
			qb.setTables(VISIT_ITEMS_TABLE_NAME);
			qb.setProjectionMap(sVisitItemProjectionMap);
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case VISIT_ITEM_ID:
			qb.setTables(VISIT_ITEMS_TABLE_NAME);
			qb.setProjectionMap(sVisitItemProjectionMap);
			qb.appendWhere(VisitItemRecord._ID + "=" + ContentUris.parseId(uri));
			c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// Tell the cursor what uri to watch, so it knows when its source data changes
		if(c != null) {
			c.setNotificationUri(mCR, uri);
		}
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

		Uri ret = null;

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long item_id = -1;
		ContentValues values = new ContentValues();
		if (initialValues != null) {
			values = initialValues;
		} else {
			values = new ContentValues();
		}
		Long now = Long.valueOf(System.currentTimeMillis());

		switch (sUriMatcher.match(uri)) {
		case PINGS:
			if (!values.containsKey(PingRecord.TIMEOUT)) {
				values.put(PingRecord.TIMEOUT, PingService.DEFAULT_TIMEOUT);
			}
			if (!values.containsKey(PingRecord.COUNT)) {
				values.put(PingRecord.COUNT, PingService.DEFAULT_COUNT);
			}
			if (!values.containsKey(PingRecord.PORT)) {
				values.put(PingRecord.PORT, PingService.DEFAULT_PORT);
			}
			if (!values.containsKey(PingRecord.STATUS)) {
				values.put(PingRecord.STATUS, PingRecord.STATUS_INITIALIZED);
			}
			if (!values.containsKey(PingRecord.CREATED_DATE)) {
				values.put(PingRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(PingRecord.MODIFIED_DATE)) {
				values.put(PingRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(PINGS_TABLE_NAME, PingRecord.IP_ADDRESS, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(PingRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case NOTE_TYPES:
			if (!values.containsKey(NoteTypeRecord.CREATED_DATE)) {
				values.put(NoteTypeRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(NoteTypeRecord.MODIFIED_DATE)) {
				values.put(NoteTypeRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(NOTE_TYPES_TABLE_NAME, NoteTypeRecord.NAME, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(NoteTypeRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENTS:
			if (!values.containsKey(ClientRecord.FIRST_NAME)) {
				values.put(ClientRecord.FIRST_NAME, "");
			}
			if (!values.containsKey(ClientRecord.LAST_NAME)) {
				values.put(ClientRecord.LAST_NAME, "");
			}
			if (!values.containsKey(ClientRecord.CREATED_DATE)) {
				values.put(ClientRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(ClientRecord.MODIFIED_DATE)) {
				values.put(ClientRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(CLIENTS_TABLE_NAME, ClientRecord.FIRST_NAME, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case VISITS:
			if (!values.containsKey(VisitRecord.DATE)) {
				values.put(VisitRecord.DATE, now);
			}
			if (!values.containsKey(VisitRecord.CREATED_DATE)) {
				values.put(VisitRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(VisitRecord.MODIFIED_DATE)) {
				values.put(VisitRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(VISITS_TABLE_NAME, VisitRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(VisitRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_VISITS:
			if (!values.containsKey(VisitRecord.CLIENT_ID)) {
				values.put(VisitRecord.CLIENT_ID, Long.valueOf(uri.getPathSegments().get(1)));
			}
			if (!values.containsKey(VisitRecord.DATE)) {
				values.put(VisitRecord.DATE, now);
			}
			if (!values.containsKey(VisitRecord.CREATED_DATE)) {
				values.put(VisitRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(VisitRecord.MODIFIED_DATE)) {
				values.put(VisitRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(VISITS_TABLE_NAME, VisitRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case NOTES:
			if (!values.containsKey(NoteRecord.DATE)) {
				values.put(NoteRecord.DATE, now);
			}
			if (!values.containsKey(NoteRecord.CREATED_DATE)) {
				values.put(NoteRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(NoteRecord.MODIFIED_DATE)) {
				values.put(NoteRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(NOTES_TABLE_NAME, NoteRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(NoteRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_NOTES:
			if (!values.containsKey(NoteRecord.CLIENT_ID)) {
				values.put(NoteRecord.CLIENT_ID, Long.valueOf(uri.getPathSegments().get(1)));
			}
			if (!values.containsKey(NoteRecord.DATE)) {
				values.put(NoteRecord.DATE, now);
			}
			if (!values.containsKey(NoteRecord.CREATED_DATE)) {
				values.put(NoteRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(NoteRecord.MODIFIED_DATE)) {
				values.put(NoteRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(NOTES_TABLE_NAME, NoteRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case TEMPERATURE_TYPES:
			if (!values.containsKey(TemperatureTypeRecord.CREATED_DATE)) {
				values.put(TemperatureTypeRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(TemperatureTypeRecord.MODIFIED_DATE)) {
				values.put(TemperatureTypeRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(TEMPERATURE_TYPES_TABLE_NAME, TemperatureTypeRecord.NAME, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(TemperatureTypeRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case TEMPERATURES:
			if (!values.containsKey(TemperatureRecord.DATE)) {
				values.put(TemperatureRecord.DATE, now);
			}
			if (!values.containsKey(TemperatureRecord.CREATED_DATE)) {
				values.put(TemperatureRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(TemperatureRecord.MODIFIED_DATE)) {
				values.put(TemperatureRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(TEMPERATURES_TABLE_NAME, TemperatureRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(TemperatureRecords.CONTENT_URI, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_VISIT_TEMPERATURES:
			if (!values.containsKey(TemperatureRecord.VISIT_ID)) {
				values.put(TemperatureRecord.VISIT_ID, Long.valueOf(uri.getPathSegments().get(3)));
			}
			if (!values.containsKey(TemperatureRecord.DATE)) {
				values.put(TemperatureRecord.DATE, now);
			}
			if (!values.containsKey(TemperatureRecord.CREATED_DATE)) {
				values.put(TemperatureRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(TemperatureRecord.MODIFIED_DATE)) {
				values.put(TemperatureRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(TEMPERATURES_TABLE_NAME, TemperatureRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_VISIT_BPS:
			if (!values.containsKey(BPRecord.VISIT_ID)) {
				values.put(BPRecord.VISIT_ID, Long.valueOf(uri.getPathSegments().get(3)));
			}
			if (!values.containsKey(BPRecord.DATE)) {
				values.put(BPRecord.DATE, now);
			}
			if (!values.containsKey(BPRecord.CREATED_DATE)) {
				values.put(BPRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(BPRecord.MODIFIED_DATE)) {
				values.put(BPRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(BPS_TABLE_NAME, BPRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_VISIT_LENGTHS:
			if (!values.containsKey(LengthRecord.VISIT_ID)) {
				values.put(LengthRecord.VISIT_ID, Long.valueOf(uri.getPathSegments().get(3)));
			}
			if (!values.containsKey(LengthRecord.DATE)) {
				values.put(LengthRecord.DATE, now);
			}
			if (!values.containsKey(LengthRecord.CREATED_DATE)) {
				values.put(LengthRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(LengthRecord.MODIFIED_DATE)) {
				values.put(LengthRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(LENGTHS_TABLE_NAME, LengthRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_VISIT_PULSES:
			if (!values.containsKey(PulseRecord.VISIT_ID)) {
				values.put(PulseRecord.VISIT_ID, Long.valueOf(uri.getPathSegments().get(3)));
			}
			if (!values.containsKey(PulseRecord.DATE)) {
				values.put(PulseRecord.DATE, now);
			}
			if (!values.containsKey(PulseRecord.CREATED_DATE)) {
				values.put(PulseRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(PulseRecord.MODIFIED_DATE)) {
				values.put(PulseRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(PULSES_TABLE_NAME, PulseRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		case CLIENT_VISIT_WEIGHTS:
			if (!values.containsKey(WeightRecord.VISIT_ID)) {
				values.put(WeightRecord.VISIT_ID, Long.valueOf(uri.getPathSegments().get(3)));
			}
			if (!values.containsKey(WeightRecord.DATE)) {
				values.put(WeightRecord.DATE, now);
			}
			if (!values.containsKey(WeightRecord.CREATED_DATE)) {
				values.put(WeightRecord.CREATED_DATE, now);
			}
			if (!values.containsKey(WeightRecord.MODIFIED_DATE)) {
				values.put(WeightRecord.MODIFIED_DATE, now);
			}
			item_id = db.insert(WEIGHTS_TABLE_NAME, WeightRecord.DATE, values);
			if (item_id > 0) {
				ret = ContentUris.withAppendedId(uri, item_id);
				mCR.notifyChange(ret, null);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

		int count = 0;
		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long recId;
		String whereClause;

		switch (sUriMatcher.match(uri)) {
		case PINGS:
			count = db.update(PINGS_TABLE_NAME, values, where, whereArgs);
			break;
		case PING_ID:
			recId = ContentUris.parseId(uri);
			whereClause = PingRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(PINGS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case NOTE_TYPES:
			count = db.update(NOTE_TYPES_TABLE_NAME, values, where, whereArgs);
			break;
		case NOTE_TYPE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = NoteTypeRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(NOTE_TYPES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENTS:
			count = db.update(CLIENTS_TABLE_NAME, values, where, whereArgs);
			break;
		case CLIENT_ID:
			recId = ContentUris.parseId(uri);
			whereClause = ClientRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(CLIENTS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case VISITS:
			count = db.update(VISITS_TABLE_NAME, values, where, whereArgs);
			break;
		case VISIT_ID:
			recId = ContentUris.parseId(uri);
			whereClause = VisitRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(VISITS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISITS:
			whereClause = VISIT_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(VISITS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_ID:
			whereClause = VISIT_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1)) + " AND "
				+ VISIT_FULL_ID + "=" + ContentUris.parseId(uri) 
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(VISITS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case NOTES:
			count = db.update(NOTES_TABLE_NAME, values, where, whereArgs);
			break;
		case NOTE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = NoteRecord._ID + "=" + recId
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(NOTES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_NOTES:
			whereClause = NOTE_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(NOTES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_NOTE_ID:
			whereClause = NOTE_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1)) + " AND "
				+ NOTE_FULL_ID + "=" + ContentUris.parseId(uri) 
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(NOTES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case TEMPERATURE_TYPES:
			count = db.update(TEMPERATURE_TYPES_TABLE_NAME, values, where, whereArgs);
			break;
		case TEMPERATURE_TYPE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = TemperatureTypeRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(TEMPERATURE_TYPES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case TEMPERATURES:
			count = db.update(TEMPERATURES_TABLE_NAME, values, where, whereArgs);
			break;
		case TEMPERATURE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = TemperatureRecord._ID + "=" + recId
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(TEMPERATURES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_TEMPERATURES:
			whereClause = TemperatureRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(TEMPERATURES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_TEMPERATURE_ID:
			whereClause = TemperatureRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + TemperatureRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(TEMPERATURES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_BPS:
			whereClause = BPRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(BPS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_BP_ID:
			whereClause = BPRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + BPRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(BPS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_LENGTHS:
			whereClause = LengthRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(LENGTHS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_LENGTH_ID:
			whereClause = LengthRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + LengthRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(LENGTHS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_PULSES:
			whereClause = PulseRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(PULSES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_PULSE_ID:
			whereClause = PulseRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + PulseRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(PULSES_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_WEIGHTS:
			whereClause = WeightRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(WEIGHTS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_WEIGHT_ID:
			whereClause = WeightRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + WeightRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.update(WEIGHTS_TABLE_NAME, values, whereClause, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		mCR.notifyChange(uri, null);
		return count;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		int count = 0;
		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long recId;
		String whereClause;

		switch (sUriMatcher.match(uri)) {
		case PINGS:
			count = db.delete(PINGS_TABLE_NAME, where, whereArgs);
			break;
		case PING_ID:
			recId = ContentUris.parseId(uri);
			whereClause = PingRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(PINGS_TABLE_NAME, whereClause, whereArgs);
			break;
		case NOTE_TYPES:
			count = db.delete(NOTE_TYPES_TABLE_NAME, where, whereArgs);
			break;
		case NOTE_TYPE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = NoteTypeRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(NOTE_TYPES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENTS:
			count = db.delete(CLIENTS_TABLE_NAME, where, whereArgs);
			break;
		case CLIENT_ID:
			recId = ContentUris.parseId(uri);
			whereClause = ClientRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(CLIENTS_TABLE_NAME, whereClause, whereArgs);
			break;
		case VISITS:
			count = db.delete(VISITS_TABLE_NAME, where, whereArgs);
			break;
		case VISIT_ID:
			recId = ContentUris.parseId(uri);
			whereClause = VisitRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(VISITS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISITS:
			whereClause = VISIT_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(VISITS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_ID:
			whereClause = VISIT_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1)) + " AND "
				+ VISIT_FULL_ID + "=" + ContentUris.parseId(uri) 
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(VISITS_TABLE_NAME, whereClause, whereArgs);
			break;
		case NOTES:
			count = db.delete(NOTES_TABLE_NAME, where, whereArgs);
			break;
		case NOTE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = NoteRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(NOTES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_NOTES:
			whereClause = NOTE_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(NOTES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_NOTE_ID:
			whereClause = NOTE_FULL_CLIENT_ID + "=" + Long.valueOf(uri.getPathSegments().get(1)) + " AND "
				+ NOTE_FULL_ID + "=" + ContentUris.parseId(uri) 
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(NOTES_TABLE_NAME, whereClause, whereArgs);
			break;
		case TEMPERATURE_TYPES:
			count = db.delete(TEMPERATURE_TYPES_TABLE_NAME, where, whereArgs);
			break;
		case TEMPERATURE_TYPE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = TemperatureTypeRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(TEMPERATURE_TYPES_TABLE_NAME, whereClause, whereArgs);
			break;
		case TEMPERATURES:
			count = db.delete(TEMPERATURES_TABLE_NAME, where, whereArgs);
			break;
		case TEMPERATURE_ID:
			recId = ContentUris.parseId(uri);
			whereClause = TemperatureRecord._ID + "=" + recId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(TEMPERATURES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_TEMPERATURES:
			whereClause = TemperatureRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(TEMPERATURES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_TEMPERATURE_ID:
			whereClause = TemperatureRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + TemperatureRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(TEMPERATURES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_BPS:
			whereClause = BPRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(BPS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_BP_ID:
			whereClause = BPRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + BPRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(BPS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_LENGTHS:
			whereClause = LengthRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(LENGTHS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_LENGTH_ID:
			whereClause = LengthRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + LengthRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(LENGTHS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_PULSES:
			whereClause = PulseRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(PULSES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_PULSE_ID:
			whereClause = PulseRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + PulseRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(PULSES_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_WEIGHTS:
			whereClause = WeightRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(WEIGHTS_TABLE_NAME, whereClause, whereArgs);
			break;
		case CLIENT_VISIT_WEIGHT_ID:
			whereClause = WeightRecord.VISIT_ID + "=" + Long.valueOf(uri.getPathSegments().get(3))
				+ " AND " + WeightRecord._ID + "=" + ContentUris.parseId(uri)
				+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
			count = db.delete(WEIGHTS_TABLE_NAME, whereClause, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		mCR.notifyChange(uri, null);
		return count;
	}
}
