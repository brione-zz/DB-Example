package com.eyebrowssoftware.example.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BPRecords {

	// Private constructor - This class cannot be instantiated
	private BPRecords() {
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = PCProvider.CONTENT_URI.buildUpon().appendPath("blood_pressures").build();

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of client temperature records.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.example.bp_record";

	public static final class BPRecord implements BaseColumns {
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.example.bp_record";

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

		/**
		 * The Systolic BP Item
		 * <P>
		 * Type: Float
		 * </P>
		 */
		public static final String SYSTOLIC = "systolic";

		/**
		 * The Diastolic BP Item
		 * <P>
		 * Type: Float
		 * </P>
		 */
		public static final String DIASTOLIC = "diastolic";
	}
}
