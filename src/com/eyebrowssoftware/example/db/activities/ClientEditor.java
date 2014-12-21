package com.eyebrowssoftware.example.db.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.R;


public class ClientEditor extends Activity {
	private static final String TAG = "ClientEditor";
	
	private static final String[] PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME,
		ClientRecord.CREATED_DATE,
		ClientRecord.MODIFIED_DATE
	};
	
	@SuppressWarnings("unused")
	private static final int ID_COLUMN_INDEX = 0;
	private static final int FIRST_NAME_COLUMN_INDEX = 1;
	private static final int LAST_NAME_COLUMN_INDEX = 2;
	private static final int CREATED_DATE_COLUMN_INDEX = 3;
	private static final int MODIFIED_DATE_COLUMN_INDEX = 4;
	
	private EditText mFirstName;
	private EditText mLastName;
	
	// States the editor can take on
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	// Keeper of the above state
	private int mState;
	
	// Bundle name of the saved Uri
	private static final String URI = "uri";
	
	// Uri of the edited or inserted item
	private Uri mUri = null;
	
	private Cursor mCursor;
	
	private Bundle mOriginalValues = null;
		
	private static final int DELETE_DIALOG_ID = 0;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.client_editor);
		
		mFirstName = (EditText) findViewById(R.id.first_name);
		mLastName = (EditText) findViewById(R.id.last_name);
		
		if (icicle != null) {
			mOriginalValues = new Bundle(icicle);
		}

		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		if(Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			if(icicle != null) { // we saved the inserted Uri here in onPause()
				mUri = Uri.parse(icicle.getString(URI));
			} else {
				ContentValues cv = new ContentValues();
				mUri = getContentResolver().insert(ClientRecords.CONTENT_URI, cv);
			}
		} else {
			Log.e(TAG, "Unknown action - Exiting!");
			finish();
			return;
		}
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mCursor != null && mCursor.moveToFirst()) {
			String first = mCursor.getString(FIRST_NAME_COLUMN_INDEX);
			String last = mCursor.getString(LAST_NAME_COLUMN_INDEX);
			long createdAt = mCursor.getLong(CREATED_DATE_COLUMN_INDEX);
			long modifiedAt = mCursor.getLong(MODIFIED_DATE_COLUMN_INDEX);
			
			mFirstName.setText(first);
			mLastName.setText(last);
			
			if (mOriginalValues == null) {
				mOriginalValues = new Bundle();
				mOriginalValues.putString(ClientRecord.FIRST_NAME, first);
				mOriginalValues.putString(ClientRecord.LAST_NAME, last);
				mOriginalValues.putLong(ClientRecord.CREATED_DATE, createdAt);
				mOriginalValues.putLong(ClientRecord.MODIFIED_DATE, modifiedAt);
			}
		} else {
			setTitle("CURSOR ERROR!!");
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	
		if(mCursor != null) { // we have an open record
			String first = mFirstName.getText().toString();
			String last = mLastName.getText().toString();
			long now = System.currentTimeMillis();
			
			if(isFinishing()  && first.length() == 0 
					&& last.length() == 0 && mState == STATE_INSERT) {
				setResult(RESULT_CANCELED);
				deleteRecord();
			} else {
				ContentValues cv = new ContentValues();
				cv.put(ClientRecord.FIRST_NAME, first);
				cv.put(ClientRecord.LAST_NAME, last);
				cv.put(ClientRecord.MODIFIED_DATE, now);
				getContentResolver().update(mUri, cv, null, null);
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle icicle) {
		icicle.putAll(mOriginalValues);
		if(mUri != null)
			icicle.putString(URI, mUri.toString());
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
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.client_editor_options_menu, menu);
		return true;
	}
	
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		if(mState == STATE_EDIT) {
			menu.setGroupEnabled(R.id.group_edit, true);
			menu.setGroupVisible(R.id.group_insert, false);
			return true;
		} else if (mState == STATE_INSERT) {
			menu.setGroupEnabled(R.id.group_edit, false);
			menu.setGroupVisible(R.id.group_insert, true);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.delete:
			showDialog(DELETE_DIALOG_ID);
			return true;
		case R.id.revert:
			cancelRecord();
			finish();
			return true;
		case R.id.cancel:
			cancelRecord();
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private final void cancelRecord() {
		if (mCursor != null) {
			if (mState == STATE_EDIT) {
				// Restore the original information we loaded at first.
				cleanup();
				getContentResolver().update(mUri, getOriginalContentValues(), null, null);
			} else if (mState == STATE_INSERT) {
				// We inserted an empty record, make sure to delete it
				deleteRecord();
			}
		}
		setResult(RESULT_CANCELED);
		finish();
	}


	private ContentValues getOriginalContentValues() {
		ContentValues cv = new ContentValues();
		if(mOriginalValues != null) {
			cv.put(ClientRecord.FIRST_NAME, mOriginalValues.getString(ClientRecord.FIRST_NAME));
			cv.put(ClientRecord.LAST_NAME, mOriginalValues.getString(ClientRecord.LAST_NAME));
			cv.put(ClientRecord.CREATED_DATE, mOriginalValues.getLong(ClientRecord.CREATED_DATE));
			cv.put(ClientRecord.MODIFIED_DATE, mOriginalValues.getLong(ClientRecord.MODIFIED_DATE));
		}
		return cv;
	}

	private final void deleteRecord() {
		cleanup();
		getContentResolver().delete(mUri, null, null);
		mUri = null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_DIALOG_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.ClientEditor_ReallyDelete))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ClientEditor_Yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteRecord();
						setResult(RESULT_OK);
						finish();
					}
				})
				.setNegativeButton(getString(R.string.ClientEditor_No), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
			return builder.create();
		default:
			return null;
		}
	}
}
