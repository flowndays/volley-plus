package com.rodrigo.harryportter.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: AaronYi Date: 11-8-17 Time: 下午12:53
 */
public class StringUtil {
	private static final ConcurrentHashMap<String, Pattern> PATTERNS = new ConcurrentHashMap<String, Pattern>();

	/**
	 * Returns the contents of inputStream as a string converted according to
	 * the encoding declared in encoding.
	 * 
	 * @param inputStream
	 * @param encoding
	 * @return Contents of inputStream
	 */
	public static String readFromStream(InputStream inputStream, String encoding) {
		if (inputStream == null)
			return "";

		int i = -1;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			while ((i = inputStream.read()) != -1) {
				byteArrayOutputStream.write(i);
			}

			return encoding == null ? byteArrayOutputStream.toString()
					: byteArrayOutputStream.toString(encoding);
		} catch (IOException e) {
			return "";
		}
	}

	public static String validate(String input) {
		return input == null ? "" : input;
	}

	public static String readFromStream(InputStream inputStream) {
		return readFromStream(inputStream, null);
	}

	public static boolean isNullOrEmpty(String text) {
		return text == null || text.length() == 0;
	}

	/**
	 * Finds the first string by giving regex pattern, if pattern contains
	 * mutilple group, returns the first. pattern will be cached
	 * 
	 * @param pattern
	 *            if empty then return original text
	 * @param text
	 * @return empty string or string found
	 */
	public static String findString(String pattern, String text) {
		if (isNullOrEmpty(text))
			return "";

		Pattern regexPattern;

		if (PATTERNS.containsKey(pattern)) {
			regexPattern = PATTERNS.get(pattern);
		} else {
			regexPattern = Pattern.compile(pattern);
			PATTERNS.put(pattern, regexPattern);
		}

		final Matcher matcher = regexPattern.matcher(text);
		if (matcher.find())
			if (matcher.groupCount() > 0)
				return matcher.group(1);
			else
				return matcher.group();

		return "";
	}

	/**
	 * Finds the first string by giving regex pattern, if pattern contains
	 * mutilple group, returns the first. pattern will be cached
	 * 
	 * @param pattern
	 *            if empty then return original text
	 * @param text
	 * @return empty string or string found
	 */
	public static ArrayList<String> findStrings(String pattern, String text) {
		final ArrayList<String> result = new ArrayList<String>();

		if (isNullOrEmpty(text))
			return result;

		Pattern regexPattern;

		if (PATTERNS.containsKey(pattern)) {
			regexPattern = PATTERNS.get(pattern);
		} else {
			regexPattern = Pattern.compile(pattern);
			PATTERNS.put(pattern, regexPattern);
		}

		final Matcher matcher = regexPattern.matcher(text);
		// if pattern contains group, return the first
		while (matcher.find()) {
			if (matcher.groupCount() > 0)
				result.add(matcher.group(1));
			else
				result.add(matcher.group());
		}

		return result;
	}

	/**
	 * Replaces each substring of this string that matches the given regex
	 * pattern pattern will be cached
	 * 
	 * @param text
	 *            text to be searched
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	public static String replaceAll(String text, String pattern,
			String replacement) {
		Pattern regexPattern;

		if (PATTERNS.containsKey(pattern)) {
			regexPattern = PATTERNS.get(pattern);
		} else {
			regexPattern = Pattern.compile(pattern);
			PATTERNS.put(pattern, regexPattern);
		}

		return regexPattern.matcher(text).replaceAll(replacement);
	}

	/**
	 * Replaces each substring of this string that matches the given
	 * charSequence pattern will be cached
	 * 
	 * @param text
	 *            text to be searched
	 * @param target
	 * @param replacement
	 * @return
	 */
	public static String replace(String text, CharSequence target,
			CharSequence replacement) {
		Pattern regexPattern;
		final String pattern = target.toString();
		if (PATTERNS.containsKey(pattern)) {
			regexPattern = PATTERNS.get(pattern);
		} else {
			regexPattern = Pattern.compile(pattern, Pattern.LITERAL);
			PATTERNS.put(pattern, regexPattern);
		}

		return regexPattern.matcher(text).replaceAll(
				Matcher.quoteReplacement(replacement.toString()));
	}
}
