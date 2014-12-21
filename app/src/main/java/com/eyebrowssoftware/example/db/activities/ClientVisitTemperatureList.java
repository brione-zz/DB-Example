package com.eyebrowssoftware.example.db.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.TemperatureRecords.TemperatureRecord;
import com.eyebrowssoftware.example.db.TemperatureTypeRecords.TemperatureTypeRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;

public class ClientVisitTemperatureList extends ListActivity {

	private static final String[] TEMPERATURES_PROJECTION = {
		TemperatureRecord._ID,
		TemperatureRecord.TEMPERATURE,
		TemperatureRecord.DATE,
		TemperatureTypeRecord.NAME
	};
	
	@SuppressWarnings("unused")
	private static final int TEMP_ID_COLUMN = 0;
	@SuppressWarnings("unused")
	private static final int TEMP_COLUMN = 1;
	@SuppressWarnings("unused")
	private static final int TEMP_TYPE_COLUMN = 3;
	private static final int TEMP_DATE_COLUMN = 2;
	
	private static final String[] TEMP_VALS = {
		TemperatureRecord.TEMPERATURE,
		TemperatureTypeRecord.NAME,
		TemperatureRecord.DATE,
		TemperatureRecord.DATE
	};
	
	private static final int[] TEMP_IDS = {
		R.id.temperature,
		R.id.method,
		R.id.date,
		R.id.time
	};
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.client_visit_item_value_list);
		
		Intent intent = getIntent();
		Uri data = intent.getData();
		
		String first = intent.getStringExtra(ClientRecord.FIRST_NAME);
		String last = intent.getStringExtra(ClientRecord.LAST_NAME);
		Date datetime = new Date(intent.getLongExtra(VisitRecord.DATE, -1));
		String date = DBExampleApplication.getDateString(datetime, DateFormat.SHORT);
		String time = DBExampleApplication.getTimeString(datetime, DateFormat.SHORT);
		String title_fmt = getString(R.string.TemperatureEditor_Title);
		setTitle(String.format(title_fmt, first, last, date, time));
		
		Cursor temperaturesCursor = managedQuery(data, TEMPERATURES_PROJECTION, null, null, TemperatureRecord.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.client_visit_temperature_item,
				temperaturesCursor, TEMP_VALS, TEMP_IDS);
		adapter.setViewBinder(new MyViewBinder());
		setListAdapter(adapter);
	}
	
	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			TextView tv = (TextView) view;
			switch(view.getId()) {
			case R.id.time:
				tv.setText(DBExampleApplication.getTimeString(new Date(cursor.getLong(TEMP_DATE_COLUMN)), DateFormat.SHORT));
				return true;
			case R.id.date:
				tv.setText(DBExampleApplication.getDateString(new Date(cursor.getLong(TEMP_DATE_COLUMN)), DateFormat.SHORT));
				return true;
			default:
				return false;
			}
		}
	}
	

}
