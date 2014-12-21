package com.eyebrowssoftware.example.db.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.TemperatureTypeRecords;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.TemperatureRecords.TemperatureRecord;
import com.eyebrowssoftware.example.db.TemperatureTypeRecords.TemperatureTypeRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;

public class TemperatureEditor extends Activity {
	private static final String TAG = "TemperatureEditor";
	
	private static final String[] TEMP_TYPE_PROJECTION = {
		TemperatureTypeRecord._ID,
		TemperatureTypeRecord.NAME
	};
	
	@SuppressWarnings("unused")
	private static final int TEMP_TYPE_ID_COLUMN = 0;
	@SuppressWarnings("unused")
	private static final int TEMP_TYPE_NAME_COLUMN = 1;
	
	private static final String[] VALS = {
		TemperatureTypeRecord.NAME
	};
	
	private static final int[] IDS = {
		android.R.id.text1
	};
	
	private static final String[] PROJECTION = {
		TemperatureRecord._ID,
		TemperatureRecord.VISIT_ID,
		TemperatureRecord.TYPE_ID,
		TemperatureRecord.TEMPERATURE,
		TemperatureRecord.DATE,
		TemperatureRecord.OVER_RANGE,
		TemperatureRecord.CREATED_DATE,
		TemperatureRecord.MODIFIED_DATE
	};
	
	@SuppressWarnings("unused")
	private static final int _ID_COLUMN = 0;
	private static final int VISIT_ID_COLUMN = 1;
	private static final int TYPE_ID_COLUMN = 2;
	private static final int TEMPERATURE_COLUMN = 3;
	private static final int DATE_COLUMN = 4;
	private static final int OVER_RANGE_COLUMN = 5;
	private static final int CREATED_DATE_COLUMN = 6;
	private static final int MODIFIED_DATE_COLUMN = 7;
	
	
	private EditText _temperature_edit_text;
	private Spinner _temperature_type_spinner;
	private CheckBox _max_exceeded_checkbox;
	
	public static final String TEMPERATURE = "temp";
	public static final String TEMPERATURE_TYPE = "type";

	private static final int MENU_ITEM_OPTION_OK = 0;
	private static final int MENU_ITEM_OPTION_CANCEL = 1;
	
	public static final int RESULT_OK = Activity.RESULT_OK;
	public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
	public static final int RESULT_OVER = Activity.RESULT_FIRST_USER; 
	
	// States the editor can take on
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	// Keeper of the above state
	private int _state;
	
	// Bundle name of the saved Uri
	private static final String URI = "uri";
	
	// Uri of the edited or inserted item
	private Uri _uri = null;
	
	private Cursor _cursor;
	
	private Bundle _originalValues = null;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.temperature_editor);
		
		_temperature_edit_text = (EditText) findViewById(R.id.temperature);
		_temperature_type_spinner = (Spinner) findViewById(R.id.temperature_type);
		_max_exceeded_checkbox = (CheckBox) findViewById(R.id.max_exceeded);
		
		Cursor tTypesCursor = managedQuery(TemperatureTypeRecords.CONTENT_URI, TEMP_TYPE_PROJECTION, null, null, TemperatureTypeRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, 
				tTypesCursor, VALS, IDS);
		_temperature_type_spinner.setAdapter(adapter);
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		Uri startUri = intent.getData();
		String first = intent.getStringExtra(ClientRecord.FIRST_NAME);
		String last = intent.getStringExtra(ClientRecord.LAST_NAME);
		Date datetime = new Date(intent.getLongExtra(VisitRecord.DATE, -1));
		String date = DBExampleApplication.getDateString(datetime, DateFormat.SHORT);
		String time = DBExampleApplication.getTimeString(datetime, DateFormat.SHORT);
		String title_fmt = getString(R.string.TemperatureEditor_Title);
		setTitle(String.format(title_fmt, first, last, date, time));
		
		
		if(Intent.ACTION_EDIT.equals(action)) {
			_state = STATE_EDIT;
			_uri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			_state = STATE_INSERT;
			if(icicle != null) { // we saved the inserted Uri here in onPause()
				_uri = Uri.parse(icicle.getString(URI));
			} else {
				ContentValues cv = new ContentValues();
				_uri = getContentResolver().insert(startUri, cv);
			}
		} else {
			Log.e(TAG, "Unknown action - Exiting!");
			finish();
			return;
		}
		_cursor = managedQuery(_uri, PROJECTION, null, null, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (_cursor != null && _cursor.moveToFirst()) {
			long visit_id = _cursor.getLong(VISIT_ID_COLUMN);
			long type_id = _cursor.getLong(TYPE_ID_COLUMN);
			float temperature = _cursor.getFloat(TEMPERATURE_COLUMN);
			long date = _cursor.getLong(DATE_COLUMN);
			long createdAt = _cursor.getLong(CREATED_DATE_COLUMN);
			long modifiedAt = _cursor.getLong(MODIFIED_DATE_COLUMN);
			boolean over_range = _cursor.getInt(OVER_RANGE_COLUMN) == 1 ? true : false;
			
			SimpleCursorAdapter sca;
			int count;
			
			sca = (SimpleCursorAdapter) _temperature_type_spinner.getAdapter();
			count = sca.getCount();
			for(int i = 0; i < count; ++i) {
				long id = sca.getItemId(i);
				if(id == type_id) { // i is the position of the id
					_temperature_type_spinner.setSelection(i);
					break;
				}
			}
			if(!_cursor.isNull(TEMPERATURE_COLUMN)) {
				_temperature_edit_text.setText(String.valueOf(temperature));
			}
			
			if (_originalValues == null) {
				_originalValues = new Bundle();
				_originalValues.putLong(TemperatureRecord.VISIT_ID, visit_id);
				_originalValues.putLong(TemperatureRecord.TYPE_ID, type_id);
				_originalValues.putFloat(TemperatureRecord.TEMPERATURE, temperature);
				_originalValues.putLong(TemperatureRecord.DATE, date);
				_originalValues.putBoolean(TemperatureRecord.OVER_RANGE, over_range);
				_originalValues.putLong(TemperatureRecord.CREATED_DATE, createdAt);
				_originalValues.putLong(TemperatureRecord.MODIFIED_DATE, modifiedAt);
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	
		if(_cursor != null) { // we have an open record
			float temperature = Float.valueOf(_temperature_edit_text.getText().toString());
			long type_id = _temperature_type_spinner.getSelectedItemId();
			boolean over_range = _max_exceeded_checkbox.isChecked();
			
			long now = System.currentTimeMillis();
			
			ContentValues cv = new ContentValues();
			cv.put(TemperatureRecord.TEMPERATURE, temperature);
			cv.put(TemperatureRecord.OVER_RANGE, over_range ? 1 : 0);
			cv.put(TemperatureRecord.TYPE_ID, type_id);
			cv.put(TemperatureRecord.DATE, now);
			cv.put(TemperatureRecord.MODIFIED_DATE, now);
			getContentResolver().update(_uri, cv, null, null);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle icicle) {
		icicle.putAll(_originalValues);
		icicle.putString(URI, _uri.toString());
	}

	@Override
	public void onDestroy() {
		cleanup();
		super.onDestroy();
	}
	
	@Override
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}
	
	private void cleanup() {
		if(_cursor != null) {
			_cursor.close();
			_cursor = null;
		}
	}
	
	@Override
	public void onBackPressed() {
		Editable value =_temperature_edit_text.getText();
		Float num;
		if(value == null || value.length() == 0) {
			Toast.makeText(this, "Temperature field cannot be left blank", Toast.LENGTH_SHORT).show();
		} else {
			num = Float.valueOf(value.toString());
			if(num < 70.0) {
				Toast.makeText(this, "Temperature is too low", Toast.LENGTH_SHORT).show();
			} else if (num > 120.0) {
				Toast.makeText(this, "Temperature is too high", Toast.LENGTH_SHORT).show();
			} else {
				finish();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, MENU_ITEM_OPTION_OK, 0, R.string.TemperatureEditor_Ok);
		menu.add(Menu.NONE, MENU_ITEM_OPTION_CANCEL, 0, R.string.TemperatureEditor_Cancel);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_OPTION_OK:
			onBackPressed();
			return true;
		case MENU_ITEM_OPTION_CANCEL:
			cancelRecord();
			return true;
		default:
			return false;
		}
	}
	
	private void cancelRecord() {
		if (_cursor != null) {
			if (_state == STATE_EDIT) {
				// Restore the original information we loaded at first.
				cleanup();
				getContentResolver().update(_uri, getOriginalContentValues(), null, null);
			} else if (_state == STATE_INSERT) {
				// We inserted an empty record, make sure to delete it
				deleteRecord();
			}
		}
		setResult(RESULT_CANCELED);
		finish();
	}

	private final void deleteRecord() {
		cleanup();
		getContentResolver().delete(_uri, null, null);
	}

	private ContentValues getOriginalContentValues() {
		ContentValues cv = new ContentValues();
		if(_originalValues != null) {
			cv.put(TemperatureRecord.TEMPERATURE, 
					_originalValues.getFloat(TemperatureRecord.TEMPERATURE));
			cv.put(TemperatureRecord.OVER_RANGE, 
					_originalValues.getBoolean(TemperatureRecord.OVER_RANGE));
			cv.put(TemperatureRecord.TYPE_ID, 
					_originalValues.getLong(TemperatureRecord.TYPE_ID));
			cv.put(TemperatureRecord.VISIT_ID, 
					_originalValues.getLong(TemperatureRecord.VISIT_ID));
			cv.put(TemperatureRecord.DATE, 
					_originalValues.getLong(TemperatureRecord.DATE));
			cv.put(TemperatureRecord.CREATED_DATE, 
					_originalValues.getLong(TemperatureRecord.CREATED_DATE));
			cv.put(TemperatureRecord.MODIFIED_DATE, 
					_originalValues.getLong(TemperatureRecord.MODIFIED_DATE));
		}
		return cv;
	}


}
