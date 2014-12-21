package com.eyebrowssoftware.example.db.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.NoteRecords;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.NoteRecords.NoteRecord;

public class ClientNotes extends ListActivity {
	public static final String TAG = "ClientNotes";
	
	private static final String[] CLIENT_PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME
	};
	
	@SuppressWarnings("unused")
	private static final int CLIENT_ID_INDEX = 0;
	private static final int CLIENT_FIRST_NAME_INDEX = 1;
	private static final int CLIENT_LAST_NAME_INDEX = 2;
	
	private static final String[] NOTE_PROJECTION = {
		NoteRecord._ID,
		NoteRecord.DATE,
		NoteRecord.TEXT
	};
	
	@SuppressWarnings("unused")
	private static final int NOTE_ID_INDEX = 0;
	private static final int NOTE_DATE_INDEX = 1;
	@SuppressWarnings("unused")
	private static final int NOTE_TEXT_INDEX = 2;
	
	private static final String[] VALUES = {
		NoteRecord.DATE,
		NoteRecord.TEXT
	};
	
	private static final int[] IDS = {
		android.R.id.text1,
		android.R.id.text2
	};
	
	private static final int MENU_ITEM_CONTEXT_EDIT = Menu.FIRST;
	private static final int MENU_ITEM_CONTEXT_DELETE = Menu.FIRST + 1;
	private static final int MENU_ITEM_CONTEXT_ADD_COPY = Menu.FIRST + 2;
	private static final int MENU_ITEM_OPTION_ADD = Menu.FIRST + 3;
	
	private static final int DELETE_DIALOG_ID = 0;
	
	private static final String CONTEXT_ID = "context_id";
	
	private long _contextMenuRecordId = MENU_ITEM_CONTEXT_EDIT;
	
	private String _name_format_string;
	private String _date_format_string;
	
	Cursor _clientCursor = null;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		_name_format_string = getString(R.string.ClientNotes_Header);
		_date_format_string = getString(R.string.ClientNotes_DateTimeFormat);
		
		this.setContentView(R.layout.client_notes);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		
		long clientId = Long.valueOf(uri.getPathSegments().get(1));
		Uri clientUri = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, clientId);
		_clientCursor = managedQuery(clientUri, CLIENT_PROJECTION, null, null, null);
		if(_clientCursor != null && _clientCursor.moveToFirst()) {
			setTitle(String.format(_name_format_string, _clientCursor.getString(CLIENT_FIRST_NAME_INDEX),
				_clientCursor.getString(CLIENT_LAST_NAME_INDEX)));
		} else {
			setTitle("Error");
		}
		Cursor cursor = managedQuery(uri, NOTE_PROJECTION, null, null, NoteRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter notesAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, 
				cursor, VALUES, IDS);
		notesAdapter.setViewBinder(new MyViewBinder());
		
		this.registerForContextMenu(getListView());
		this.setListAdapter(notesAdapter);
		if(icicle != null) {
			_contextMenuRecordId = icicle.getLong(CONTEXT_ID);	
		}
	}

	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView tv = (TextView) view;
			switch(tv.getId()) {
			case android.R.id.text1:
				Date date = new Date(cursor.getLong(NOTE_DATE_INDEX));
				tv.setText(String.format(_date_format_string, 
					DBExampleApplication.getDateString(date, DateFormat.SHORT),
					DBExampleApplication.getTimeString(date, DateFormat.SHORT)));
				return true;
			default:
				return false;
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		icicle.putLong(CONTEXT_ID, _contextMenuRecordId);
	}
	
	@Override
	protected void onDestroy() {
		cleanup();
		super.onDestroy();
	}
	
	@Override
	public void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}
	
	private void cleanup() {
		if(_clientCursor != null) {
			_clientCursor.close();
			_clientCursor = null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, MENU_ITEM_OPTION_ADD, 0, R.string.ClientNotes_Add);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_OPTION_ADD:
			Intent intent = new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, ClientNoteEditor.class);
			startActivity(intent);
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
			e.printStackTrace();
			return;
		}
		@SuppressWarnings("unused")
		Cursor noteCursor = (Cursor) getListAdapter().getItem(info.position);
		menu.add(Menu.NONE, MENU_ITEM_CONTEXT_EDIT, 0, R.string.ClientNotes_Edit);
		menu.add(Menu.NONE, MENU_ITEM_CONTEXT_DELETE, 0, R.string.ClientNotes_Delete);
		menu.add(Menu.NONE, MENU_ITEM_CONTEXT_ADD_COPY, 0, R.string.ClientNotes_AddCopy);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			_contextMenuRecordId = info.id;
			Uri contextUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
			
			@SuppressWarnings("unused")
			Cursor noteCursor = (Cursor) getListAdapter().getItem(info.position);
			switch(item.getItemId()) {
			case MENU_ITEM_CONTEXT_EDIT:
				startActivity(new Intent(Intent.ACTION_EDIT, contextUri, this, ClientNoteEditor.class));
				return true;
			case MENU_ITEM_CONTEXT_DELETE:
				showDialog(DELETE_DIALOG_ID);
				return true;
			case MENU_ITEM_CONTEXT_ADD_COPY:
				startActivity(new Intent(DBExampleApplication.ACTION_ADD_COPY, contextUri, this, ClientNoteEditor.class));
				return true;
			default:
				return false;
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void deleteRecord() {
		Uri contextUri = ContentUris.withAppendedId(NoteRecords.CONTENT_URI, _contextMenuRecordId);
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

		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a client selected by
			// the user. The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			// Launch activity to view/edit the currently selected item
			startActivity(new Intent(Intent.ACTION_EDIT, uri, this, ClientNoteEditor.class));
		}
	}
	

}
