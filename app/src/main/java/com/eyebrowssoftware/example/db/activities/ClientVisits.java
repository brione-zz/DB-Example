package com.eyebrowssoftware.example.db.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;

public class ClientVisits extends ListActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "ClientVisitList";

	private static final String[] VISIT_PROJECTION = {
		VisitRecord._ID,
		VisitRecord.DATE
	};
	
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_ID_INDEX = 0;
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_DATE_INDEX = 1;
	
	private static final String[] CLIENT_PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME
	};

	@SuppressWarnings("unused")
	private static final int CLIENT_ID_INDEX = 0;
	private static final int CLIENT_FIRST_NAME_INDEX = 1;
	private static final int CLIENT_LAST_NAME_INDEX = 2;

	// Menu item ids
	private static final int MENU_ITEM_ADD = Menu.FIRST + 2;
	
	private static final String[] VALS = { 
		VisitRecord.DATE 
	};

	private static final int[] IDS = { 
		android.R.id.text1 
	};
	
	private static final String URI = "uri";
	private long _context_id;
	
	private String _datetime_format;
	
	private Cursor _clientCursor;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.client_visit_list);

		_datetime_format = getString(R.string.ClientVisitList_DateTimeFormat);
		
		Intent intent = this.getIntent();
		Uri uri = intent.getData();
		long clientId = Long.valueOf(uri.getPathSegments().get(1));
		Uri clientUri = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, clientId);
		_clientCursor = managedQuery(clientUri, CLIENT_PROJECTION, null, null, null);
		
		Cursor cursor = managedQuery(uri, VISIT_PROJECTION, null, null, VisitRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
				cursor, VALS, IDS);
		adapter.setViewBinder(new MyViewBinder());
		setListAdapter(adapter);
		
		if(icicle != null) {
			_context_id = icicle.getLong(URI);
		}
	}
	
	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(cursor == null) {
				return false;
			}
			TextView dateView = (TextView) view;
			Date date = new Date(cursor.getLong(columnIndex));
			dateView.setText(String.format(_datetime_format, DBExampleApplication.getDateString(date, DateFormat.SHORT),
					DBExampleApplication.getTimeString(date, DateFormat.SHORT)));
			return true;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(_clientCursor != null && _clientCursor.moveToFirst()) {
			String first = _clientCursor.getString(CLIENT_FIRST_NAME_INDEX);
			String last = _clientCursor.getString(CLIENT_LAST_NAME_INDEX);
			this.setTitle(String.format(this.getString(R.string.ClientVisitList_TitleFormat, first, last)));
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		icicle.putLong(URI, _context_id);
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
		if(_clientCursor != null) {
			_clientCursor.close();
			_clientCursor = null;
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
			Intent intent = new Intent(Intent.ACTION_EDIT, uri, this, ClientVisitViewer.class);
			intent.putExtras(getIntent());
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_ITEM_ADD, 0, R.string.ClientList_AddMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_ITEM_ADD:
			Uri uri = getContentResolver().insert(getIntent().getData(), new ContentValues());
			startActivity(new Intent(Intent.ACTION_VIEW, uri, this, ClientVisitViewer.class));
			return true;
		default:
			return false;
		}
	}
}
