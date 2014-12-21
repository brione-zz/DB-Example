package com.eyebrowssoftware.example.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class LengthRecords {

	// Private constructor - This class cannot be instantiated
	private LengthRecords() {
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = PCProvider.CONTENT_URI.buildUpon().appendPath("lengths").build();

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of client temperature records.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.example.length_record";

	public static final class LengthRecord implements BaseColumns {
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.example.length_record";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "date DESC";

		/**
		 * The id of the Client record
		 * <P>
		 * Type: INTEGER
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * The id of the Client Record
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String VISIT_ID = "visit_id";


		/**
		 * The DateTime of the Client Note
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String DATE = "date";

		/**
		 * The Measured Length of the Client Length Record
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String LENGTH = "length";

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
