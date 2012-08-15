package com.rodrigo.harryportter.util;

/**
 * 用于修正旧版本下载来的章节内容
 * 
 * @author TangCan
 * 
 */
public class Fixer {
	public static String fixContent(String str) {
		str = str.replaceAll("\t|\r", "");
		str = str.replaceAll("\n\n", "\n");
		str = str.replaceAll("\n", "\n    ");

		return str;
	}
}
