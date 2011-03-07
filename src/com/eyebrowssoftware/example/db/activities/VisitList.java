package com.eyebrowssoftware.example.db.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.VisitRecords;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;

public class VisitList extends ListActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "ClientVisitList";

	private static final String[] VISIT_PROJECTION = {
		VisitRecord._ID,
		VisitRecord.DATE,
		VisitRecord.CLIENT_ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME
	};
	
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_ID_INDEX = 0;
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_DATE_INDEX = 1;
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_CLIENT_ID_INDEX = 2;
	private static final int CLIENT_VISIT_FIRST_NAME_INDEX = 3;
	private static final int CLIENT_VISIT_LAST_NAME_INDEX = 4;
	
	private static final String[] VALS = { 
		VisitRecord.DATE,
		ClientRecord.FIRST_NAME
	};

	private static final int[] IDS = { 
		android.R.id.text1,
		android.R.id.text2
	};
	
	private static final String URI = "uri";
	private long _context_id;
	
	private String _datetime_format;
	private String _name_format;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.client_visit_list);
		setTitle(R.string.VisitList_TitleFormat);

		_datetime_format = getString(R.string.VisitList_DateTimeFormat);
		_name_format = getString(R.string.VisitList_NameFormat);
		
		Cursor cursor = managedQuery(VisitRecords.CONTENT_URI, VISIT_PROJECTION, 
				null, null, VisitRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, 
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
			TextView tv = (TextView) view;
			switch(view.getId()) {
			case android.R.id.text1:
				Date date = new Date(cursor.getLong(columnIndex));
				tv.setText(String.format(_datetime_format, DBExampleApplication.getDateString(date, DateFormat.SHORT),
						DBExampleApplication.getTimeString(date, DateFormat.SHORT)));
				return true;
			case android.R.id.text2:
				String first = cursor.getString(CLIENT_VISIT_FIRST_NAME_INDEX);
				String last = cursor.getString(CLIENT_VISIT_LAST_NAME_INDEX);
				tv.setText(String.format(_name_format, first, last));
				return true;
			default:
				return false;
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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
	}
}
