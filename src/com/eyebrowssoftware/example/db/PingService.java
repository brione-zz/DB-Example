package com.eyebrowssoftware.example.db;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.eyebrowssoftware.example.db.PingRecords.PingRecord;

public class PingService extends IntentService {
	private static final String TAG = "PingService";

	// The IP Address String and the Timeout are passed as Extras in the invoking Intent
	
	public static final int DEFAULT_TIMEOUT = 3000;
	public static final int DEFAULT_COUNT = 5;
	public static final String DEFAULT_IP = "127.0.0.1";
	public static final int DEFAULT_PORT = 80;
	
	// Address to attempt Pinging
	private String _ipAddress;
	// Port to connect on
	private int _port;
	// Millisecond timeout of the Ping operation
	private int _timeout;
	// Number of times to attempt the ping operation
	private int _count;
    // _mData is the Uri of the record in the db 
    private Uri _data;
    // Used to update the database from the service
    private ContentResolver _cR;
    
	public PingService() {
		super("PingService");
		// setIntentRedelivery(true);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		_cR = this.getContentResolver();
	}
	
	@Override
	public void onDestroy() {
		// My code here
		super.onDestroy();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		

		ContentValues vals = new ContentValues();
		
		if(intent != null) {
			_data = intent.getData();
			if (_data == null) {
				_data = _cR.insert(PingRecords.CONTENT_URI, null);
			}
			_ipAddress = (intent.hasExtra(PingRecord.IP_ADDRESS)) ? intent.getStringExtra(PingRecord.IP_ADDRESS) : DEFAULT_IP;
			_port = intent.getIntExtra(PingRecord.PORT, DEFAULT_PORT);
			_timeout = intent.getIntExtra(PingRecord.TIMEOUT, DEFAULT_TIMEOUT);
			_count = intent.getIntExtra(PingRecord.COUNT, DEFAULT_COUNT);
			
			vals.put(PingRecord.STATUS, PingRecord.STATUS_PROGRESSING);
			vals.put(PingRecord.SUCCESS, 0);
			_cR.update(_data, vals, null, null);
			
			// Perform the actual ping operation and get the # of successful pings
			int num_successes = 0;
			InetAddress addy;
			int ping_status;
			int i;
			
			try {
				addy = Inet4Address.getByName(_ipAddress);
				for(i = 0; i < _count; i++) {
					ping_status = performPing(addy, _port, _timeout);
					if(ping_status >= 0) { // success or real failure
						num_successes += ping_status;
						vals.clear();
						vals.put(PingRecord.SUCCESS, num_successes);
						_cR.update(_data, vals, null, null);
					} else {
						vals.clear();
						vals.put(PingRecord.STATUS, ping_status);
						_cR.update(_data, vals, null, null);
						break;
					}
				}
				if(i ==_count) {
					vals.clear();
					vals.put(PingRecord.STATUS, PingRecord.STATUS_COMPLETE);
					_cR.update(_data, vals, null, null);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Log.e(TAG, "Unknown Host Exception");
				vals.clear();
				vals.put(PingRecord.STATUS, PingRecord.STATUS_UNKNOWN_HOST);
				_cR.update(_data, vals, null, null);
			} catch (IOException e) {
				e.printStackTrace();
				vals.clear();
				vals.put(PingRecord.STATUS, PingRecord.STATUS_IO_EXCEPTION);
				_cR.update(_data, vals, null, null);
			}			
		} else {
			Log.e(TAG, "Intent is null!!!!");
			vals.clear();
			vals.put(PingRecord.STATUS, PingRecord.STATUS_ILLEGAL_ARGUMENT);
			_cR.update(_data, vals, null, null);
		}
	}

	public int performPing(InetAddress addy, int port, int timeout) throws IOException {
		int success = 0;
		long start = System.currentTimeMillis();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			success = -1000;
			e.printStackTrace();
		} finally {
			long end = System.currentTimeMillis();
			long delta = end - start;
			Log.i(TAG, "Ping took: " + String.valueOf(delta) + " milliseconds");
			success = 1;
		}
		return success;
	}
}
