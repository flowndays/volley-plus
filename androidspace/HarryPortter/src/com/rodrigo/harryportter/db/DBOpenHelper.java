package com.rodrigo.harryportter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "db_mybook66";
	public static final String DB_TABLENAME_BOOK = "book";
	public static final int DB_VERSION = 3;

	/*
	 * book表的字段：
	 * _Id INTEGER, name text, author text, status tinyint, sequence long
	 * brief text, lReadPlace int, lReadWords text
	 */
	private static final String DB_CREATE_BOOK = "CREATE TABLE book (_Id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ " name text, author text, status tinyint, sequence long, "
			+ " brief text, lReadPlace int, fileName text, size int); ";

	public DBOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	/*
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database
	 * .sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + DB_TABLENAME_BOOK + "; ");
		db.execSQL(DB_CREATE_BOOK);
		Log.d(this.getClass().getName(), "onCreate");
	}

	/*
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion) {
			Log.d(this.getClass().getName(), "oldVersion=" + oldVersion + ";newVersion=" + newVersion);
		}
	}
}
