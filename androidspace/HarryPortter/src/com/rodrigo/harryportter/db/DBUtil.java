package com.rodrigo.harryportter.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.os.Environment;
import android.util.Log;

public class DBUtil {
	final static File sd = Environment.getExternalStorageDirectory();
	final static File data = Environment.getDataDirectory();
	final static String romDBPath = "/data/com.rodrigo.reader/databases/";
	final static String sdDBPath = "/commonReader/backup/";
	final static String dbName = "db_commonReader";
	final static String Tag = "DBUtil";

	public static boolean backup() {
		boolean result = false;
		if (sd.canWrite()) {
			File backupDir = new File(sd, sdDBPath);
			if (!backupDir.exists())
				backupDir.mkdirs();

			File currentDB = new File(data, romDBPath + dbName);
			File backupDB = new File(backupDir, dbName);
			if (backupDB.exists())
				backupDB.delete();

			result = copyDB(currentDB, backupDB);
		}

		Log.d(Tag, result ? " backup success!" : " backup failed!");
		return result;
	}

	public static boolean recover() {
		File romDir = new File(data, romDBPath);
		if (!romDir.exists())
			romDir.mkdirs();

		File sdDB = new File(sd, sdDBPath + dbName);
		if (!sdDB.exists()) {
			return false;
		}

		File romDB = new File(romDir, dbName);
		if (romDB.exists())
			romDB.delete();

		return copyDB(sdDB, romDB);
	}

	public static void tryRecoverDB() {
		File romDB = new File(data, romDBPath + dbName);
		if (!romDB.exists()) {
			Log.d(Tag, " db not exists, tryto recover...");
			if (recover()) {
				Log.d(Tag, " recover success!");
			} else {
				Log.d(Tag, " recover failed!");
			}
		}
	}

	private static boolean copyDB(File src, File target) {
		try {
			if (target.exists())
				target.delete();

			if (src.exists()) {
				FileChannel srcChannel = new FileInputStream(src).getChannel();
				FileChannel dstChannel = new FileOutputStream(target)
						.getChannel();
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
				dstChannel.close();
			}

			return true;
		} catch (Exception e) {
			Log.e(DBUtil.class.getSimpleName(),
					"error when backup db:" + e.getMessage());
			return false;
		}
	}
}
