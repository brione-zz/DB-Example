package com.eyebrowssoftware.example.db.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.NoteRecords;
import com.eyebrowssoftware.example.db.NoteTypeRecords;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.NoteRecords.NoteRecord;
import com.eyebrowssoftware.example.db.NoteTypeRecords.NoteTypeRecord;

public class NoteEditor extends Activity {
	public static final String TAG = "NoteEditor";
	
	private static final String[] CLIENT_PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME
	};
	
	private static final int FIRST_NAME_INDEX = 1;
	private static final int LAST_NAME_INDEX = 2;
	
	private static final String[] CLIENT_COLUMNS = {
		ClientRecord.FIRST_NAME
	};
	
	private static final int[] CLIENT_IDS = {
		android.R.id.text1
	};
	
	private static final String[] NOTE_TYPES_PROJECTION = {
		NoteTypeRecord._ID,
		NoteTypeRecord.NAME,
		NoteTypeRecord.TEMPLATE
	};
	
	private static final int NOTE_TEMPLATE_COLUMN_INDEX = 2;
	
	private static final String[] NOTE_TYPES_COLUMNS = {
		NoteTypeRecord.NAME
	};
	
	private static final int[] NOTE_TYPES_IDS = {
		android.R.id.text1
	};
	
	private static final String[] PROJECTION = {
		NoteRecord._ID,
		NoteRecord.CLIENT_ID,
		NoteRecord.TYPE_ID,
		NoteRecord.TEXT,
		NoteRecord.DATE,
		NoteRecord.CREATED_DATE,
		NoteRecord.MODIFIED_DATE
	};
	
	private static final int CLIENT_ID_COLUMN = 1;
	private static final int TYPE_ID_COLUMN = 2;
	private static final int TEXT_COLUMN = 3;
	private static final int DATE_COLUMN = 4;
	private static final int CREATED_AT_COLUMN = 5;
	private static final int MODIFIED_AT_COLUMN = 6;
	
	private static final int MENU_ITEM_OPTION_OK = 0;
	private static final int MENU_ITEM_OPTION_CANCEL = 1;
	
	private Spinner _clientSpinner;
	private Spinner _noteTypeSpinner;
	private EditText _noteEditText;

	private String _name_format_string;
	
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
	
	private boolean _note_type_self_change = false;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		_name_format_string = getString(R.string.NoteEditor_NameFormat);
		
		this.setContentView(R.layout.note_editor);
		
		_clientSpinner = (Spinner) findViewById(R.id.client_spinner);
		_noteTypeSpinner = (Spinner) findViewById(R.id.note_type_spinner);
		_noteEditText = (EditText) findViewById(R.id.note_edit_text);
		
		Cursor clients_cursor = managedQuery(ClientRecords.CONTENT_URI, CLIENT_PROJECTION, null, null, null);
		SimpleCursorAdapter clientsAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				clients_cursor, CLIENT_COLUMNS, CLIENT_IDS);
		clientsAdapter.setViewBinder(new MyViewBinder());
		_clientSpinner.setAdapter(clientsAdapter);
		
		Cursor note_types_cursor = managedQuery(NoteTypeRecords.CONTENT_URI, NOTE_TYPES_PROJECTION, null, null, null);
		SimpleCursorAdapter noteTypeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				note_types_cursor, NOTE_TYPES_COLUMNS, NOTE_TYPES_IDS);
		_noteTypeSpinner.setAdapter(noteTypeAdapter);
		_noteTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Spinner sp = (Spinner) parent;
				Cursor cursor = (Cursor)((SimpleCursorAdapter)sp.getAdapter()).getItem(position);
				if(!_note_type_self_change) {
					_noteEditText.setText(cursor.getString(NOTE_TEMPLATE_COLUMN_INDEX));
					
				} else {
					_note_type_self_change = false;
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				if(!_note_type_self_change) {
					_noteEditText.setText("");
				} else {
					_note_type_self_change = false;
				}
			}
			
		});
		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		if(Intent.ACTION_EDIT.equals(action)) {
			_state = STATE_EDIT;
			_uri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			_state = STATE_INSERT;
			if(icicle != null) { // we saved the inserted Uri here in onPause()
				_uri = Uri.parse(icicle.getString(URI));
			} else {
				ContentValues cv = new ContentValues();
				_uri = getContentResolver().insert(NoteRecords.CONTENT_URI, cv);
			}
		} else if (DBExampleApplication.ACTION_ADD_COPY.equals(action)) {
			Uri toCopyUri = intent.getData();
			Cursor toCopyCursor = managedQuery(toCopyUri, PROJECTION, null, null, null);
			if(toCopyCursor != null && toCopyCursor.moveToFirst()) {
				ContentValues cv = new ContentValues();
				cv.put(NoteRecord.TYPE_ID, toCopyCursor.getLong(TYPE_ID_COLUMN));
				cv.put(NoteRecord.CLIENT_ID, toCopyCursor.getLong(CLIENT_ID_COLUMN));
				cv.put(NoteRecord.TEXT, toCopyCursor.getString(TEXT_COLUMN));
				cv.put(NoteRecord.DATE, toCopyCursor.getLong(DATE_COLUMN));
				_uri = getContentResolver().insert(NoteRecords.CONTENT_URI, cv);
			} else {
				Log.e(TAG, "Nothing to copy - Exiting!");
				return;
			}
			
		} else {
			Log.e(TAG, "Unknown action - Exiting!");
			finish();
			return;
		}
		_cursor = managedQuery(_uri, PROJECTION, null, null, null);
	}
	
	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView tv = (TextView) view;
			switch(tv.getId()) {
			case android.R.id.text1:
				String first = cursor.getString(FIRST_NAME_INDEX);
				String last = cursor.getString(LAST_NAME_INDEX);
				tv.setText(String.format(_name_format_string, first, last));
				return true;
			default:
				return false;
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (_state == STATE_EDIT) {
			setTitle(R.string.NoteEditor_Editing);
		} else if (_state == STATE_INSERT) {
			setTitle(R.string.NoteEditor_Adding);
		}
		if (_cursor != null && _cursor.moveToFirst()) {
			long client_id = _cursor.getLong(CLIENT_ID_COLUMN);
			long type_id = _cursor.getLong(TYPE_ID_COLUMN);
			String text = _cursor.getString(TEXT_COLUMN);
			long date = _cursor.getLong(DATE_COLUMN);
			long createdAt = _cursor.getLong(CREATED_AT_COLUMN);
			long modifiedAt = _cursor.getLong(MODIFIED_AT_COLUMN);
			
			SimpleCursorAdapter sca;
			int count;
			
			sca = (SimpleCursorAdapter) _clientSpinner.getAdapter();
			count = sca.getCount();
			for(int i = 0; i < count; ++i) {
				long id = sca.getItemId(i);
				if(id == client_id) { // i is the position of the id
					_clientSpinner.setSelection(i);
					break;
				}
			}
			sca = (SimpleCursorAdapter) _noteTypeSpinner.getAdapter();
			count = sca.getCount();
			for(int i = 0; i < count; ++i) {
				long id = sca.getItemId(i);
				if(id == type_id) { // i is the position of the id
					_note_type_self_change = true; // don't update the text from this selection
					_noteTypeSpinner.setSelection(i);
					break;
				}
			}
			_noteEditText.setText(text);
			
			if (_originalValues == null) {
				_originalValues = new Bundle();
				_originalValues.putLong(NoteRecord.CLIENT_ID, client_id);
				_originalValues.putLong(NoteRecord.TYPE_ID, type_id);
				_originalValues.putString(NoteRecord.TEXT, text);
				_originalValues.putLong(NoteRecord.DATE, date);
				_originalValues.putLong(NoteRecord.CREATED_DATE, createdAt);
				_originalValues.putLong(NoteRecord.MODIFIED_DATE, modifiedAt);
			}
		} else {
			setTitle("CURSOR ERROR!!!");
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	
		if(_cursor != null) { // we have an open record
			String text = _noteEditText.getText().toString();
			long client_id = _clientSpinner.getSelectedItemId();
			long note_type_id = _noteTypeSpinner.getSelectedItemId();
			long now = System.currentTimeMillis();
			
			ContentValues cv = new ContentValues();
			cv.put(NoteRecord.TEXT, text);
			cv.put(NoteRecord.CLIENT_ID, client_id);
			cv.put(NoteRecord.TYPE_ID, note_type_id);
			cv.put(NoteRecord.DATE, now);
			cv.put(NoteRecord.MODIFIED_DATE, now);
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
		String text = _noteEditText.getText().toString();
		if(text == null || text.length() == 0) {
			Toast.makeText(this, "The Text field cannot be blank", Toast.LENGTH_SHORT).show();
		} else {
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, MENU_ITEM_OPTION_OK, 0, R.string.NoteEditor_Ok);
		menu.add(Menu.NONE, MENU_ITEM_OPTION_CANCEL, 0, R.string.NoteEditor_Cancel);
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
			finish();
			return true;
		default:
			return false;
		}
	}

	private final void cancelRecord() {
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
			cv.put(NoteRecord.CLIENT_ID, _originalValues.getLong(NoteRecord.CLIENT_ID));
			cv.put(NoteRecord.TYPE_ID, _originalValues.getLong(NoteRecord.TYPE_ID));
			cv.put(NoteRecord.TEXT, _originalValues.getString(NoteRecord.TEXT));
			cv.put(NoteRecord.DATE, _originalValues.getLong(NoteRecord.DATE));
			cv.put(NoteRecord.CREATED_DATE, _originalValues.getLong(NoteRecord.CREATED_DATE));
			cv.put(NoteRecord.MODIFIED_DATE, _originalValues.getLong(NoteRecord.MODIFIED_DATE));
		}
		return cv;
	}


}
