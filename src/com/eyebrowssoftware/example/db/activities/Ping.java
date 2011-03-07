package com.eyebrowssoftware.example.db.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eyebrowssoftware.example.db.PingRecords;
import com.eyebrowssoftware.example.db.PingService;
import com.eyebrowssoftware.example.db.R;
import com.eyebrowssoftware.example.db.PingRecords.PingRecord;

public class Ping extends Activity implements Handler.Callback {
	private static final String TAG = "Ping";
    
	private EditText _editAddress;
	private EditText _editPort;
	private EditText _editTimeout;
	private EditText _editCount;
	private TextView _textSuccesses;
	private TextView _textStatus;
	private Button _pingButton;
	
	private static final String[] PROJECTION = {
		PingRecord._ID,
		PingRecord.IP_ADDRESS,
		PingRecord.PORT,
		PingRecord.TIMEOUT,
		PingRecord.COUNT,
		PingRecord.SUCCESS,
		PingRecord.STATUS,
		PingRecord.CREATED_DATE,
		PingRecord.MODIFIED_DATE
	};
	
	@SuppressWarnings("unused")
	private static final int COLUMN_ID_INDEX = 0;
	private static final int COLUMN_ADDRESS_INDEX = 1;
	private static final int COLUMN_PORT_INDEX = 2;
	private static final int COLUMN_TIMEOUT_INDEX = 3;
	private static final int COLUMN_COUNT_INDEX = 4;
	private static final int COLUMN_SUCCESS_INDEX = 5;
	private static final int COLUMN_STATUS_INDEX = 6;
	@SuppressWarnings("unused")
	private static final int COLUMN_CREATED_DATE_INDEX = 7;
	@SuppressWarnings("unused")
	private static final int COLUMN_MODIFIED_DATE_INDEX = 8;
	
	// Key for the saved Uri in the onSavedInstanceState bundle
	private static final String URI = "uri";
	
	// Uri for the created result record
	private Uri _mUri;
	
	private Cursor _mCursor;
		
	private MyContentObserver _mCO;
	
	// Set the callback to handleMessage() in this activity
	private Handler _mMyHandler = new Handler(this);
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ping);
        
        _editAddress = (EditText) findViewById(R.id.ping_edit_address);
        _editPort = (EditText) findViewById(R.id.ping_edit_port);
        _editTimeout = (EditText) findViewById(R.id.ping_edit_timeout);
        _editCount = (EditText) findViewById(R.id.ping_edit_count);
        _textSuccesses = (TextView) findViewById(R.id.ping_tv_successes);
        _textStatus = (TextView) findViewById(R.id.ping_tv_status);
        _pingButton = (Button) findViewById(R.id.ping_button);
        
        if(savedInstanceState != null) {
        	// get the saved Uri from the bundle
        	_mUri = Uri.parse(savedInstanceState.getString(URI));
        } else {
        	_mUri = getContentResolver().insert(PingRecords.CONTENT_URI, null);
        }
        
        _mCursor = this.managedQuery(_mUri, PROJECTION, null, null, null);
        if(_mCursor != null) {
        	_mCO = new MyContentObserver(_mMyHandler);
        	// _mCO = new MyContentObserver(null);
        	_mCursor.registerContentObserver(_mCO);
        }
        
        Intent intent = this.getIntent();
        // See if we got initialization information via extras in the Intent
		if (intent.hasExtra(PingRecord.IP_ADDRESS) && intent.hasExtra(PingRecord.TIMEOUT)
				&& intent.hasExtra(PingRecord.COUNT)) {
			String addy = intent.getStringExtra(PingRecord.IP_ADDRESS);
			_editAddress.setText(addy);
			int timeout = intent.getIntExtra(PingRecord.TIMEOUT, PingService.DEFAULT_TIMEOUT);
			_editTimeout.setText(String.valueOf(timeout));
			int count = intent.getIntExtra(PingRecord.COUNT, PingService.DEFAULT_COUNT);
			_editCount.setText(String.valueOf(count));
		}
		// Set up the pushbutton to kick off the Ping Service
		_pingButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveUIValues();
				Intent intent = new Intent(Ping.this, PingService.class);
				intent.setData(_mUri); // pass the result record to the IntentService
				intent.putExtra(PingRecord.IP_ADDRESS, _editAddress.getText().toString());
				intent.putExtra(PingRecord.PORT, Integer.valueOf(_editPort.getText().toString()));
				intent.putExtra(PingRecord.TIMEOUT, Integer.valueOf(_editTimeout.getText().toString()));
				intent.putExtra(PingRecord.COUNT, Integer.valueOf(_editCount.getText().toString()));
				Ping.this.startService(intent);
			}
		});
    }
	
	private void saveUIValues() {
		ContentValues vals = new ContentValues();
		vals.put(PingRecord.IP_ADDRESS, _editAddress.getText().toString());
		vals.put(PingRecord.PORT, Integer.valueOf(_editPort.getText().toString()));
		vals.put(PingRecord.TIMEOUT, Integer.valueOf(_editTimeout.getText().toString()));
		vals.put(PingRecord.COUNT, Integer.valueOf(_editCount.getText().toString()));
		this.getContentResolver().update(_mUri, vals, null, null);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveUIValues();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateUI(false);
	}
	
	private void updateUI(boolean requery) {
		if(_mCursor != null) {
			if(requery) {
				_mCursor.requery();
			}
			if(_mCursor.moveToFirst()) {
				int status_res;
				int status = _mCursor.getInt(COLUMN_STATUS_INDEX);
				switch(status) {
				case PingRecord.STATUS_INITIALIZED:
					status_res = R.string.ping_status_init;
					break;
				case PingRecord.STATUS_PROGRESSING:
					status_res = R.string.ping_status_progress;
					break;
				case PingRecord.STATUS_COMPLETE:
					status_res = R.string.ping_status_complete;
					break;
				case PingRecord.STATUS_ILLEGAL_ARGUMENT:
					status_res = R.string.ping_status_illegal_argument;
					break;
				case PingRecord.STATUS_IO_EXCEPTION:
					status_res = R.string.ping_status_io_error;
					break;
				case PingRecord.STATUS_UNKNOWN_HOST:
					status_res = R.string.ping_status_unknown_host;
					break;
				default:
					status_res = R.string.ping_status_error;
					break;
				}
				_textStatus.setText(status_res);
				_editAddress.setText(_mCursor.getString(COLUMN_ADDRESS_INDEX));
				_editPort.setText(String.valueOf(_mCursor.getString(COLUMN_PORT_INDEX)));
				_editTimeout.setText(String.valueOf(_mCursor.getInt(COLUMN_TIMEOUT_INDEX)));
				_editCount.setText(String.valueOf(_mCursor.getInt(COLUMN_COUNT_INDEX)));
				_textSuccesses.setText(String.valueOf(_mCursor.getInt(COLUMN_SUCCESS_INDEX)));
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		if(_mCursor != null) {
			_mCursor.unregisterContentObserver(_mCO);
			_mCursor.close();
			_mCursor = null;
		}
		super.onDestroy();
	}
	
	@Override
	protected void finalize() {
		try {
			if(_mCursor != null) {
				_mCursor.unregisterContentObserver(_mCO);
				_mCursor.close();
				_mCursor = null;
			}
			super.finalize();
		} catch (Throwable e) {
			Log.e(TAG, "finalize() error", e);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		icicle.putString(URI, _mUri.toString());
	}
	
	private class MyContentObserver extends ContentObserver {

		public MyContentObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
		
		@Override
		public void onChange(boolean selfChange) {
			_mMyHandler.sendEmptyMessage(0);
		}
	}
	
	public boolean handleMessage(Message msg) {
		updateUI(true);
		return true;
	}
	
    
}