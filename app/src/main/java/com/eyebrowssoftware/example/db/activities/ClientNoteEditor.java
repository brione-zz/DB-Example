package com.eyebrowssoftware.example.db.activities;

import android.app.Activity;
import android.content.ContentUris;
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
import android.widget.AdapterView.OnItemSelectedListener;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.NoteRecords;
import com.eyebrowssoftware.example.db.NoteTypeRecords;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.NoteRecords.NoteRecord;
import com.eyebrowssoftware.example.db.NoteTypeRecords.NoteTypeRecord;

public class ClientNoteEditor extends Activity implements OnItemSelectedListener {
	public static final String TAG = "ClientNoteEditor";
	
	private static final String[] CLIENT_PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME
	};
	
	private static final int FIRST_NAME_INDEX = 1;
	private static final int LAST_NAME_INDEX = 2;
	
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
	
	private Spinner mNoteTypeSpinner;
	private EditText mNoteEditText;

	// States the editor can take on
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	// Keeper of the above state
	private int mState;
	
	// Bundle name of the saved Uri
	private static final String URI = "uri";
	
	// Uri of the edited or inserted item
	private Uri _uri = null;
	
	
	private long mClientId;
	private Cursor mClientCursor = null;
	private Cursor mCursor;
	
	private Bundle mOriginalValues = null;
	
	private boolean mNote_type_self_change = false;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		this.setContentView(R.layout.client_note_editor);
		
		mNoteTypeSpinner = (Spinner) findViewById(R.id.note_type_spinner);
		mNoteEditText = (EditText) findViewById(R.id.note_edit_text);
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		Uri data = intent.getData();
		
		mClientId = Long.valueOf(data.getPathSegments().get(1));
		Uri clientUri = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, mClientId);
		mClientCursor = managedQuery(clientUri, CLIENT_PROJECTION, null, null, null);
		
		Cursor note_types_cursor = managedQuery(NoteTypeRecords.CONTENT_URI, NOTE_TYPES_PROJECTION, null, null, null);
		SimpleCursorAdapter noteTypeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				note_types_cursor, NOTE_TYPES_COLUMNS, NOTE_TYPES_IDS);
		mNoteTypeSpinner.setAdapter(noteTypeAdapter);
		mNoteTypeSpinner.setOnItemSelectedListener(this);
		
		if(Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			_uri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			if(icicle != null) { // we saved the inserted Uri here in onPause()
				_uri = Uri.parse(icicle.getString(URI));
			} else {
				ContentValues cv = new ContentValues();
				_uri = getContentResolver().insert(data, cv);
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
		mCursor = managedQuery(_uri, PROJECTION, null, null, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (mClientCursor != null && mClientCursor.moveToFirst()) {
			if (mState == STATE_EDIT) {
				String title = getString(R.string.ClientNoteEditor_Editing);
				
				setTitle(String.format(title, mClientCursor.getString(FIRST_NAME_INDEX),
						mClientCursor.getString(LAST_NAME_INDEX)));
			} else if (mState == STATE_INSERT) {
				String title = getString(R.string.ClientNoteEditor_Adding);
				
				setTitle(String.format(title, mClientCursor.getString(FIRST_NAME_INDEX),
						mClientCursor.getString(LAST_NAME_INDEX)));
			}
		}
		if (mCursor != null && mCursor.moveToFirst()) {
			long client_id = mCursor.getLong(CLIENT_ID_COLUMN);
			long type_id = mCursor.getLong(TYPE_ID_COLUMN);
			String text = mCursor.getString(TEXT_COLUMN);
			long date = mCursor.getLong(DATE_COLUMN);
			long createdAt = mCursor.getLong(CREATED_AT_COLUMN);
			long modifiedAt = mCursor.getLong(MODIFIED_AT_COLUMN);
			
			SimpleCursorAdapter sca = (SimpleCursorAdapter) mNoteTypeSpinner.getAdapter();
			int count = sca.getCount();
			for(int i = 0; i < count; ++i) {
				long id = sca.getItemId(i);
				if(id == type_id) { // i is the position of the id
					mNote_type_self_change = true; // don't update the text from this selection
					mNoteTypeSpinner.setSelection(i);
					break;
				}
			}
			mNoteEditText.setText(text);
			
			if (mOriginalValues == null) {
				mOriginalValues = new Bundle();
				mOriginalValues.putLong(NoteRecord.CLIENT_ID, client_id);
				mOriginalValues.putLong(NoteRecord.TYPE_ID, type_id);
				mOriginalValues.putString(NoteRecord.TEXT, text);
				mOriginalValues.putLong(NoteRecord.DATE, date);
				mOriginalValues.putLong(NoteRecord.CREATED_DATE, createdAt);
				mOriginalValues.putLong(NoteRecord.MODIFIED_DATE, modifiedAt);
			}
		} else {
			setTitle("CURSOR ERROR!!");
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	
		if(mCursor != null) { // we have an open record
			String text = mNoteEditText.getText().toString();
			long note_type_id = mNoteTypeSpinner.getSelectedItemId();
			long now = System.currentTimeMillis();
			
			ContentValues cv = new ContentValues();
			cv.put(NoteRecord.TEXT, text);
			cv.put(NoteRecord.CLIENT_ID, mClientId);
			cv.put(NoteRecord.TYPE_ID, note_type_id);
			cv.put(NoteRecord.DATE, now);
			cv.put(NoteRecord.MODIFIED_DATE, now);
			getContentResolver().update(_uri, cv, null, null);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle icicle) {
		icicle.putAll(mOriginalValues);
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
		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		if(mClientCursor != null) {
			mClientCursor.close();
			mClientCursor = null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.client_note_editor_options_menu, menu);		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.revert:
			cancelRecord();
			finish();
			return true;
		default:
			return false;
		}
	}

	private final void cancelRecord() {
		if (mCursor != null) {
			if (mState == STATE_EDIT) {
				// Restore the original information we loaded at first.
				cleanup();
				getContentResolver().update(_uri, getOriginalContentValues(), null, null);
			} else if (mState == STATE_INSERT) {
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
		if(mOriginalValues != null) {
			cv.put(NoteRecord.CLIENT_ID, mOriginalValues.getLong(NoteRecord.CLIENT_ID));
			cv.put(NoteRecord.TYPE_ID, mOriginalValues.getLong(NoteRecord.TYPE_ID));
			cv.put(NoteRecord.TEXT, mOriginalValues.getString(NoteRecord.TEXT));
			cv.put(NoteRecord.DATE, mOriginalValues.getLong(NoteRecord.DATE));
			cv.put(NoteRecord.CREATED_DATE, mOriginalValues.getLong(NoteRecord.CREATED_DATE));
			cv.put(NoteRecord.MODIFIED_DATE, mOriginalValues.getLong(NoteRecord.MODIFIED_DATE));
		}
		return cv;
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Spinner sp = (Spinner) parent;
		Cursor cursor = (Cursor)((SimpleCursorAdapter)sp.getAdapter()).getItem(position);
		if(!mNote_type_self_change) {
			mNoteEditText.setText(cursor.getString(NOTE_TEMPLATE_COLUMN_INDEX));
			
		} else {
			mNote_type_self_change = false;
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
		if(!mNote_type_self_change) {
			mNoteEditText.setText("");
		} else {
			mNote_type_self_change = false;
		}
	}
}
