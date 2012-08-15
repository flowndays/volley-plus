package com.rodrigo.harryportter.util;

import java.io.File;
import java.util.HashSet;

import android.util.Log;

public class FileCleanUtils {
	private static final String TAG = "FileCleanUtils";

	/**
	 * 清理文件夹下的文件
	 * 
	 * @param targetDir
	 *            要清理的文件
	 * @param keeps
	 *            需要保留的文件（夹）名
	 * @return
	 */
	public static boolean cleanDir(File targetDir, HashSet<String> keeps) {
		if (targetDir == null)
			return false;
		boolean result = true;
		File[] files = targetDir.listFiles();
		for (File f : files) {
			if (keeps != null && !keeps.contains(f.getName())) {
				Log.d(TAG, "CleanFileSystem,delete File:" + f.getPath());
				result = deleteAll(f) && result;
			}
		}
		return result;
	}

	/**
	 * 删除所有文件
	 * 
	 * @param file
	 */
	public static boolean deleteAll(File file) {
		boolean result = true;
		if (!file.exists())
			return false;
		String[] subFiles = file.list();
		if (subFiles != null)
			for (String sub : subFiles) {
				File subFile = new File(file, sub);
				if (subFile.isDirectory()) {
					result = deleteAll(subFile) && result;
				}
				result = subFile.delete() && result;
			}
		file.delete();
		return result;
	}

}
