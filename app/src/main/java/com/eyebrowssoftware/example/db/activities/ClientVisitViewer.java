package com.eyebrowssoftware.example.db.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.ClientRecords;
import com.eyebrowssoftware.example.db.DBExampleApplication;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.ClientRecords.ClientRecord;
import com.eyebrowssoftware.example.db.VisitRecords.VisitRecord;

public class ClientVisitViewer extends Activity implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "ClientVisitViewer";

	TextView _clientNameView;
	TextView _visitDateView;
	
	Button _measTemp;
	Button _measLength;
	Button _measBP;
	Button _measPulse;
	Button _measWeight;

	Button _showTemps;
	Button _showLengths;
	Button _showBPs;
	Button _showPulses;
	Button _showWeights;
	
	Cursor _cursor = null;
	Cursor _clientCursor = null;
	
	private long _clientId;
	
	ContentResolver _cR;
	
	private static final String[] CLIENT_VISIT_PROJECTION = {
		VisitRecord._ID,
		VisitRecord.CLIENT_ID,
		VisitRecord.DATE
	};
	
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_ID_COLUMN_INDEX = 0;
	@SuppressWarnings("unused")
	private static final int CLIENT_VISIT_CLIENT_ID_COLUMN_INDEX = 1;
	private static final int CLIENT_VISIT_DATE_COLUMN_INDEX = 2;
	
	private static final String[] CLIENT_PROJECTION = {
		ClientRecord._ID,
		ClientRecord.FIRST_NAME,
		ClientRecord.LAST_NAME
	};
	
	@SuppressWarnings("unused")
	private static final int CLIENT_ID_INDEX = 0;
	private static final int CLIENT_FIRST_NAME_INDEX = 1;
	private static final int CLIENT_LAST_NAME_INDEX = 2;

	private String _client_name_format;
	private String _datetime_format;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.client_visit);
		
		setTitle(R.string.ClientVisitEditor_Title);
		
		_client_name_format = getString(R.string.ClientVisitEditor_NameFormat);
		_datetime_format = getString(R.string.ClientVisitEditor_DateTimeFormat);
		
		_clientNameView = (TextView) findViewById(R.id.client_name);
		_visitDateView = (TextView) findViewById(R.id.visit_date);

		_measTemp = (Button) findViewById(R.id.meas_temperature);
		_measTemp.setOnClickListener(this);
		_measLength = (Button) findViewById(R.id.meas_length);
		_measLength.setOnClickListener(this);
		_measBP = (Button) findViewById(R.id.meas_bp);
		_measBP.setOnClickListener(this);
		_measPulse = (Button) findViewById(R.id.meas_pulse);
		_measPulse.setOnClickListener(this);
		_measWeight = (Button) findViewById(R.id.meas_weight);
		_measWeight.setOnClickListener(this);
		
		
		_showTemps = (Button) findViewById(R.id.show_temperature);
		_showTemps.setOnClickListener(this);
		_showLengths = (Button) findViewById(R.id.show_length);
		_showLengths.setOnClickListener(this);
		_showBPs = (Button) findViewById(R.id.show_bp);
		_showBPs.setOnClickListener(this);
		_showPulses = (Button) findViewById(R.id.show_pulse);
		_showPulses.setOnClickListener(this);
		_showWeights = (Button) findViewById(R.id.show_weight);
		_showWeights.setOnClickListener(this);
		
		Intent intent = getIntent();
		Uri data = intent.getData();
		
		_clientId = Long.valueOf(data.getPathSegments().get(1));
		Uri clientUri = ContentUris.withAppendedId(ClientRecords.CONTENT_URI, _clientId);
		_clientCursor = managedQuery(clientUri, CLIENT_PROJECTION, null, null, null);
		
		_cursor = managedQuery(data, CLIENT_VISIT_PROJECTION, null, null, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (_clientCursor != null && _clientCursor.moveToFirst()) {
			String first = _clientCursor.getString(CLIENT_FIRST_NAME_INDEX);
			String last = _clientCursor.getString(CLIENT_LAST_NAME_INDEX);
			_clientNameView.setText(String.format(_client_name_format, first, last));
		}
		
		if (_cursor != null && _cursor.moveToFirst()) {
			Date date = new Date(_cursor.getLong(CLIENT_VISIT_DATE_COLUMN_INDEX));
			_visitDateView.setText(String.format(_datetime_format, DBExampleApplication.getDateString(date, DateFormat.SHORT),
					DBExampleApplication.getTimeString(date, DateFormat.SHORT)));
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
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
		if(_clientCursor != null) {
			_clientCursor.close();
			_clientCursor = null;
		}
	}

	public void onClick(View v) {
		Uri.Builder builder = getIntent().getData().buildUpon();
		Uri uri = null;
		Intent intent = new Intent();
		intent.putExtra(ClientRecord.FIRST_NAME, _clientCursor.getString(CLIENT_FIRST_NAME_INDEX));
		intent.putExtra(ClientRecord.LAST_NAME, _clientCursor.getString(CLIENT_LAST_NAME_INDEX));
		intent.putExtra(VisitRecord.DATE, _cursor.getLong(CLIENT_VISIT_DATE_COLUMN_INDEX));
		
		switch(v.getId()) {
		case R.id.meas_temperature:
			intent.setAction(Intent.ACTION_INSERT);
			uri = builder.appendPath("temperatures").build();
			intent.setData(uri);
			intent.setClass(this, TemperatureEditor.class);
			break;
		case R.id.meas_length:
			intent.setAction(Intent.ACTION_INSERT);
			uri = builder.appendPath("lengths").build();
			intent.setData(uri);
			intent.setClass(this, LengthEditor.class);
			break;
		case R.id.meas_bp:
			intent.setAction(Intent.ACTION_INSERT);
			uri = builder.appendPath("blood_pressures").build();
			intent.setData(uri);
			intent.setClass(this, BPEditor.class);
			break;
		case R.id.meas_pulse:
			intent.setAction(Intent.ACTION_INSERT);
			uri = builder.appendPath("pulses").build();
			intent.setData(uri);
			intent.setClass(this, PulseEditor.class);
			break;
		case R.id.meas_weight:
			intent.setAction(Intent.ACTION_INSERT);
			uri = builder.appendPath("weights").build();
			intent.setData(uri);
			intent.setClass(this, WeightEditor.class);
			break;
		case R.id.show_bp:
			intent.setAction(Intent.ACTION_VIEW);
			uri = builder.appendPath("blood_pressures").build();
			intent.setData(uri);
			intent.setClass(this, ClientVisitBPList.class);
			break;
		case R.id.show_length:
			intent.setAction(Intent.ACTION_VIEW);
			uri = builder.appendPath("lengths").build();
			intent.setData(uri);
			intent.setClass(this, ClientVisitLengthList.class);
			break;
		case R.id.show_pulse:
			intent.setAction(Intent.ACTION_VIEW);
			uri = builder.appendPath("pulses").build();
			intent.setData(uri);
			intent.setClass(this, ClientVisitPulseList.class);
			break;
		case R.id.show_temperature:
			intent.setAction(Intent.ACTION_VIEW);
			uri = builder.appendPath("temperatures").build();
			intent.setData(uri);
			intent.setClass(this, ClientVisitTemperatureList.class);
			break;
		case R.id.show_weight:
			intent.setAction(Intent.ACTION_VIEW);
			uri = builder.appendPath("weights").build();
			intent.setData(uri);
			intent.setClass(this, ClientVisitWeightList.class);
			break;
		default:
			break;
		}
		if(intent != null) {
			startActivity(intent);
		}
	}
}
