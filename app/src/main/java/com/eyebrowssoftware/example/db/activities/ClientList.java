package com.eyebrowssoftware.example.db.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.NoteRecords;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.VisitRecords;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;

public class ClientList extends ListActivity {
	private static final String TAG = "ClientList";
	
	private static final String[] PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME,
	};
	
	@SuppressWarnings("unused")
	private static final int ID_COLUMN_INDEX = 0;
	private static final int FIRST_NAME_COLUMN_INDEX = 1;
	private static final int LAST_NAME_COLUMN_INDEX = 2;
	

	private static final int DELETE_DIALOG_ID = 0;
	
	private String _name_format_string; 
	
	// Name of the Extra for context menu item id we were working with
	private static final String CONTEXT_ID = "id";
	// Item id that we are working with in a context menu
	long mContextId = 0;

	private static final String[] VALS = { 
		ClientRecord.FIRST_NAME 
	};

	private static final int[] IDS = { 
		android.R.id.text1 
	};

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setTitle(R.string.ClientList_Title);
		
		_name_format_string = getString(R.string.ClientList_NameFormat);
		
		Cursor cursor = managedQuery(ClientRecords.CONTENT_URI, PROJECTION, null, null, ClientRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
				cursor, VALS, IDS);
		adapter.setViewBinder(new MyViewBinder());
		setListAdapter(adapter);
		registerForContextMenu(getListView());
		if(icicle != null) {
			mContextId = icicle.getLong(CONTEXT_ID);
		}
	}
	
	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(cursor == null) {
				return false;
			}
			TextView nameView = (TextView) view;
			String first = cursor.getString(FIRST_NAME_COLUMN_INDEX);
			String last = cursor.getString(LAST_NAME_COLUMN_INDEX);
			if(first != null && last != null) {
				String name = String.format(_name_format_string, first, last);
				nameView.setText(name);
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		icicle.putLong(CONTEXT_ID, mContextId);
	}
	
	@Override
	protected void onDestroy() {
		cleanup();
		super.onDestroy();
	}
	
	@Override
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}
	
	private void cleanup() {
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		setListAdapter(null);
		if(adapter != null) {
			adapter.changeCursor(null);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.client_list_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.add_menu:
			Uri uri = ClientRecords.CONTENT_URI;
			startActivity(new Intent(Intent.ACTION_INSERT, uri, this, ClientEditor.class));
			return true;
		case R.id.notes_menu:
			startActivity(new Intent(Intent.ACTION_VIEW, NoteRecords.CONTENT_URI, this, NoteList.class));
			return true;
		case R.id.visits_menu:
			startActivity(new Intent(Intent.ACTION_VIEW, VisitRecords.CONTENT_URI, this, VisitList.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		AdapterView.AdapterContextMenuInfo info;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.client_list_context_menu, menu);
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Log.d(TAG, "info.position: " + info.position);
			Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
			if (cursor != null && cursor.moveToPosition(info.position)) {
				menu.setHeaderTitle(String.format(_name_format_string, 
					cursor.getString(FIRST_NAME_COLUMN_INDEX),
					cursor.getString(LAST_NAME_COLUMN_INDEX)));
			}
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterView.AdapterContextMenuInfo info;
		Intent intent;
		
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			Log.d(TAG, "info.position: " + info.position);
			// Get the Uri of the record we're intending to operate on
			mContextId = info.id;
			Uri.Builder builder = ContentUris.appendId(ClientRecords.CONTENT_URI.buildUpon(), mContextId);
			
			switch (item.getItemId()) {
			case R.id.context_delete:
				showDialog(DELETE_DIALOG_ID);
				return true;
			case R.id.context_edit:
				intent = new Intent(Intent.ACTION_EDIT, builder.build(), this, ClientEditor.class);
				startActivity(intent);
				return true;
			case R.id.context_new_visit:
				Uri newVisit = getContentResolver().insert(builder.appendPath("visits").build(), 
						new ContentValues());
				intent = new Intent(Intent.ACTION_VIEW, newVisit, this, ClientVisitViewer.class);
				startActivity(intent);
				return true;
			case R.id.context_visits:
				intent = new Intent(Intent.ACTION_VIEW, builder.appendPath("visits").build(), 
					this, ClientVisits.class);
				startActivity(intent);
				return true;
			case R.id.context_notes:
				intent = new Intent(Intent.ACTION_VIEW, builder.appendPath("notes").build(), 
					this, ClientNotes.class);
				startActivity(intent);
				return true;
			default:
				return super.onContextItemSelected(item);
			}
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return super.onContextItemSelected(item);
		}

	}

	private void deleteRecord() {
		Uri contextUri = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, mContextId);
		getContentResolver().delete(contextUri, null, null);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_DIALOG_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.ClientList_ReallyDelete))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ClientList_Yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteRecord();
					}
				})
				.setNegativeButton(getString(R.string.ClientList_No), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
			return builder.create();
		default:
			return null;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Uri uri = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, id);
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a client selected by
			// the user. The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			// Launch activity to view/edit the currently selected item
			startActivity(new Intent(Intent.ACTION_EDIT, uri, this, ClientEditor.class));
		}
	}
	
}

