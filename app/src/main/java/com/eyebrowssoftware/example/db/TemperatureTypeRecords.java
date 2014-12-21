package com.eyebrowssoftware.example.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class TemperatureTypeRecords {

	// Private constructor - This class cannot be instantiated
	private TemperatureTypeRecords() {
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = PCProvider.CONTENT_URI.buildUpon().appendPath("temperature_types").build();

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of
	 * breweries.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.example.temperature_type_record";

	public static final class TemperatureTypeRecord implements BaseColumns {
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * note.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.example.temperature_type_record";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "name ASC";

		/**
		 * The id of the Temperature Type record
		 * <P>
		 * Type: INTEGER
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * The Name of the Temperature Type
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";

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
