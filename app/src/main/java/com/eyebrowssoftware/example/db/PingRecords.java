package com.eyebrowssoftware.example.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PingRecords {

	// Private constructor - This class cannot be instantiated
	private PingRecords() {
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = PCProvider.CONTENT_URI.buildUpon().appendPath("pings").build();

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of
	 * breweries.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.example.ping_record";

	public static final class PingRecord implements BaseColumns {
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.example.ping_record";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "name ASC";

		/**
		 * The id of the Ping record
		 * <P>
		 * Type: INTEGER
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * The IP Address value of the Ping
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String IP_ADDRESS = "addr";

		/**
		 * The Port number on the server to Ping
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String PORT = "port";

		/**
		 * The Timeout value of the Ping
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String TIMEOUT = "timeout";

		/**
		 * The Ping count of the Ping record
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String COUNT = "count";

		/**
		 * The Ping success count of the Ping record
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String SUCCESS = "success";

		/**
		 * The Ping status of the Ping record
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String STATUS = "status";
		
		public static final int STATUS_ILLEGAL_ARGUMENT = -3;
		public static final int STATUS_IO_EXCEPTION = -2;
		public static final int STATUS_UNKNOWN_HOST = -1;
		public static final int STATUS_INITIALIZED = 0;
		public static final int STATUS_PROGRESSING = 1;
		public static final int STATUS_COMPLETE = 2;

		/**
		 * The DATETIME that the entry was created in the database
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String CREATED_DATE = "created_at";

		/**
		 * The DATETIME that the entry was modified in the database
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MODIFIED_DATE = "modified_at";
	}
}
