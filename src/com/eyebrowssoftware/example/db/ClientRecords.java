package com.eyebrowssoftware.example.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class ClientRecords {

	// Private constructor - This class cannot be instantiated
	private ClientRecords() {
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = PCProvider.CONTENT_URI.buildUpon().appendPath("clients").build();

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of
	 * breweries.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.example.client_record";

	public static final class ClientRecord implements BaseColumns {
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.example.client_record";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "last_name ASC";

		/**
		 * The id of the Client record
		 * <P>
		 * Type: INTEGER
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * The First Name of the Client
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String FIRST_NAME = "first_name";

		/**
		 * The Last Name of the Client
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String LAST_NAME = "last_name";

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
