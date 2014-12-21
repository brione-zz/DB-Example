package com.eyebrowssoftware.example.db.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.TemperatureTypeRecords;
import com.eyebrowssoftware.example.db.TemperatureTypeRecords.TemperatureTypeRecord;

public class TemperatureTypeList extends ListActivity {
	private static final String TAG = "TemperatureTypeList";
	
	private static final String[] PROJECTION = {
		TemperatureTypeRecord._ID,
		TemperatureTypeRecord.NAME,
	};
	
	public static final int COLUMN_ID_INDEX = 0;
	public static final int COLUMN_NAME_INDEX = 1;
	
	private static final String[] VALS = {
		TemperatureTypeRecord.NAME,
	};
	
	private static final int[] IDS = {
		android.R.id.text1,
	};
	
	// Menu item ids
	public static final int MENU_ITEM_DELETE = Menu.FIRST;
	public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
	public static final int MENU_ITEM_ADD = Menu.FIRST + 2;

	private static final int DELETE_DIALOG_ID = 0;
	
	// Name of the Extra for context menu item id we were working with
	private static final String CONTEXT_ID = "id";
	// Item id that we are working with in a context menu
	long _contextId = 0;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setTitle(R.string.TemperatureTypeList_Title);
		Cursor cursor = managedQuery(TemperatureTypeRecords.CONTENT_URI, PROJECTION, null, null, TemperatureTypeRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
				cursor, VALS, IDS);
		setListAdapter(adapter);
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		icicle.putLong(CONTEXT_ID, _contextId);
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

		menu.add(Menu.NONE, MENU_ITEM_ADD, 0, R.string.TemperatureTypeList_AddMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_ITEM_ADD:
			Uri uri = TemperatureTypeRecords.CONTENT_URI;
			startActivity(new Intent(Intent.ACTION_INSERT, uri, this, TemperatureTypeEditor.class));
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info;

		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor != null && cursor.moveToFirst()) {
			menu.setHeaderTitle(cursor.getString(COLUMN_NAME_INDEX));
		}
		// Add a menu item to edit the record
		menu.add(Menu.NONE, MENU_ITEM_EDIT, 0, R.string.TemperatureTypeList_EditMenu);
		// Add a menu item to delete the record
		menu.add(Menu.NONE, MENU_ITEM_DELETE, 1, R.string.TemperatureTypeList_DeleteMenu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		Intent intent;
		
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			// Get the Uri of the record we're intending to operate on
			_contextId = info.id;
			Uri contextUri = ContentUris.withAppendedId(TemperatureTypeRecords.CONTENT_URI, _contextId);
			switch (item.getItemId()) {
			case MENU_ITEM_DELETE:
				showDialog(DELETE_DIALOG_ID);
				return true;
			case MENU_ITEM_EDIT:
				intent = new Intent(Intent.ACTION_EDIT, contextUri, this, TemperatureTypeEditor.class);
				startActivity(intent);
				return true;
			default:
				return false;
			}
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

	}

	private void deleteRecord() {
		Uri contextUri = ContentUris.withAppendedId(TemperatureTypeRecords.CONTENT_URI, _contextId);
		getContentResolver().delete(contextUri, null, null);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_DIALOG_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.TemperatureTypeList_ReallyDelete))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.TemperatureTypeList_Yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteRecord();
					}
				})
				.setNegativeButton(getString(R.string.TemperatureTypeList_No), new DialogInterface.OnClickListener() {
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

		Uri uri = ContentUris.withAppendedId(TemperatureTypeRecords.CONTENT_URI, id);
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a client selected by
			// the user. The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			// Launch activity to view/edit the currently selected item
			startActivity(new Intent(Intent.ACTION_EDIT, uri, this, NoteTypeEditor.class));
		}
	}
	
}
