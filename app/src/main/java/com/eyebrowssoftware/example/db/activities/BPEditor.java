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
import android.widget.EditText;
import android.widget.Toast;

import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.BPRecords.BPRecord;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;

public class BPEditor extends Activity {
	private static final String TAG = "BPEditor";
	
	private static final String[] PROJECTION = {
		BPRecord._ID,
		BPRecord.VISIT_ID,
		BPRecord.SYSTOLIC,
		BPRecord.DIASTOLIC,
		BPRecord.DATE,
		BPRecord.CREATED_DATE,
		BPRecord.MODIFIED_DATE
	};
	
	@SuppressWarnings("unused")
	private static final int _ID_COLUMN = 0;
	private static final int VISIT_ID_COLUMN = 1;
	private static final int SYSTOLIC_COLUMN = 2;
	private static final int DIASTOLIC_COLUMN = 3;
	private static final int DATE_COLUMN = 4;
	private static final int CREATED_DATE_COLUMN = 5;
	private static final int MODIFIED_DATE_COLUMN = 6;
	
	private EditText _systolic_edit_text;
	private EditText _diastolic_edit_text;
	
	private static final int MENU_ITEM_OPTION_OK = 0;
	private static final int MENU_ITEM_OPTION_CANCEL = 1;
	
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
		
		setContentView(R.layout.bp_editor);
		
		_systolic_edit_text = (EditText) findViewById(R.id.systolic);
		_diastolic_edit_text = (EditText) findViewById(R.id.diastolic);
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		Uri startUri = intent.getData();
		
		String first = intent.getStringExtra(ClientRecord.FIRST_NAME);
		String last = intent.getStringExtra(ClientRecord.LAST_NAME);
		Date datetime = new Date(intent.getLongExtra(VisitRecord.DATE, -1));
		String date = DBExampleApplication.getDateString(datetime, DateFormat.SHORT);
		String time = DBExampleApplication.getTimeString(datetime, DateFormat.SHORT);
		String title_fmt = getString(R.string.BPEditor_Title);
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
			int systolic = _cursor.getInt(SYSTOLIC_COLUMN);
			int diastolic = _cursor.getInt(DIASTOLIC_COLUMN);
			long date = _cursor.getLong(DATE_COLUMN);
			long createdAt = _cursor.getLong(CREATED_DATE_COLUMN);
			long modifiedAt = _cursor.getLong(MODIFIED_DATE_COLUMN);
			
			if(!_cursor.isNull(SYSTOLIC_COLUMN)) {
				_systolic_edit_text.setText(String.valueOf(systolic));
			}
			if(!_cursor.isNull(DIASTOLIC_COLUMN)) {
				_diastolic_edit_text.setText(String.valueOf(diastolic));
			}
			
			if (_originalValues == null) {
				_originalValues = new Bundle();
				_originalValues.putLong(BPRecord.VISIT_ID, visit_id);
				_originalValues.putFloat(BPRecord.SYSTOLIC, systolic);
				_originalValues.putFloat(BPRecord.DIASTOLIC, diastolic);
				_originalValues.putLong(BPRecord.DATE, date);
				_originalValues.putLong(BPRecord.CREATED_DATE, createdAt);
				_originalValues.putLong(BPRecord.MODIFIED_DATE, modifiedAt);
			}
		} else {
			setTitle("CURSOR ERROR!!");
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	
		if(_cursor != null) { // we have an open record
			int systolic = Integer.valueOf(_systolic_edit_text.getText().toString());
			int diastolic = Integer.valueOf(_diastolic_edit_text.getText().toString());
			
			long now = System.currentTimeMillis();
			
			ContentValues cv = new ContentValues();
			cv.put(BPRecord.SYSTOLIC, systolic);
			cv.put(BPRecord.DIASTOLIC, diastolic);
			cv.put(BPRecord.DATE, now);
			cv.put(BPRecord.MODIFIED_DATE, now);
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
		boolean sys_valid = false;
		boolean dia_valid = false;
		Editable value;
		int num;
		
		value =_systolic_edit_text.getText();
		if(value == null || value.length() == 0) {
			Toast.makeText(this, "Systolic field cannot be left blank", Toast.LENGTH_SHORT).show();
		} else {
			num = Integer.valueOf(value.toString());
			if(num < 20) {
				Toast.makeText(this, "Systolic is too low", Toast.LENGTH_SHORT).show();
			} else if (num > 280) {
				Toast.makeText(this, "Systolic is too high", Toast.LENGTH_SHORT).show();
			} else {
				sys_valid = true;
			}
		}
		value =_diastolic_edit_text.getText();
		if(value == null || value.length() == 0) {
			Toast.makeText(this, "Diastolic field cannot be left blank", Toast.LENGTH_SHORT).show();
		} else {
			num = Integer.valueOf(value.toString());
			if(num < 20) {
				Toast.makeText(this, "Diastolic is too low", Toast.LENGTH_SHORT).show();
			} else if (num > 280) {
				Toast.makeText(this, "Diastolic is too high", Toast.LENGTH_SHORT).show();
			} else {
				dia_valid = true;
			}
		}
		if(sys_valid && dia_valid) {
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, MENU_ITEM_OPTION_OK, 0, R.string.BPEditor_Ok);
		menu.add(Menu.NONE, MENU_ITEM_OPTION_CANCEL, 0, R.string.BPEditor_Cancel);
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
			cv.put(BPRecord.SYSTOLIC, 
					_originalValues.getInt(BPRecord.SYSTOLIC));
			cv.put(BPRecord.DIASTOLIC, 
					_originalValues.getInt(BPRecord.DIASTOLIC));
			cv.put(BPRecord.VISIT_ID, 
					_originalValues.getLong(BPRecord.VISIT_ID));
			cv.put(BPRecord.DATE, 
					_originalValues.getLong(BPRecord.DATE));
			cv.put(BPRecord.CREATED_DATE, 
					_originalValues.getLong(BPRecord.CREATED_DATE));
			cv.put(BPRecord.MODIFIED_DATE, 
					_originalValues.getLong(BPRecord.MODIFIED_DATE));
		}
		return cv;
	}


}
