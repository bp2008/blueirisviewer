package org.brian.blueirisviewer.util;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Contains string manipulation and analysis functions that are otherwise missing or inadequate.
 */
public class string
{

	public static boolean IsNullOrEmpty(String s)
	{
		return s == null || s.equals("");
	}

	public static boolean IsNullOrWhitespace(String s)
	{
		if (s == null)
			return true;
		for(int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if(c != ' ' && c != '\t' && c != '\r' && c != '\n')
				return false;
		}
		return true;
	}

	public static String Join(String separator, int[] array)
	{
		if (array == null)
			return "";
		return Join(separator, array, 0, array.length);
	}

	public static String Join(String separator, int[] array, int startIndex, int count)
	{
		if (array == null)
			return "";
		StringBuilder sb = new StringBuilder();
		int end = Math.min(array.length, startIndex + count);
		for (int i = startIndex; i < end; i++)
		{
			if (i != startIndex)
				sb.append(separator);
			sb.append(array[i]);
		}
		return sb.toString();
	}

	public static String Join(String separator, String[] array)
	{
		if (array == null)
			return "";
		return Join(separator, array, 0, array.length);
	}

	public static String Join(String separator, String[] array, int startIndex, int count)
	{
		if (array == null)
			return "";
		StringBuilder sb = new StringBuilder();
		int end = Math.min(array.length, startIndex + count);
		for (int i = startIndex; i < end; i++)
		{
			if (i != startIndex)
				sb.append(separator);
			sb.append(array[i]);
		}
		return sb.toString();
	}

	public static String Join(String separator, ArrayList<Integer> arrayList)
	{
		if (arrayList == null)
			return "";
		return Join(separator, arrayList, 0, arrayList.size());
	}

	public static String Join(String separator, ArrayList<Integer> arrayList, int startIndex, int count)
	{
		if (arrayList == null)
			return "";
		StringBuilder sb = new StringBuilder();
		int end = Math.min(arrayList.size(), startIndex + count);
		for (int i = startIndex; i < end; i++)
		{
			if (i != startIndex)
				sb.append(separator);
			sb.append(arrayList.get(i));
		}
		return sb.toString();
	}

	public static String JoinStringArrayList(String separator, ArrayList<String> arrayList)
	{
		if (arrayList == null)
			return "";
		return JoinStringArrayList(separator, arrayList, 0, arrayList.size());
	}

	public static String JoinStringArrayList(String separator, ArrayList<String> arrayList, int startIndex, int count)
	{
		if (arrayList == null)
			return "";
		StringBuilder sb = new StringBuilder();
		int end = Math.min(arrayList.size(), startIndex + count);
		for (int i = startIndex; i < end; i++)
		{
			if (i != startIndex)
				sb.append(separator);
			sb.append(arrayList.get(i));
		}
		return sb.toString();
	}

	/**
	 * Returns true if the specified bytes (interpreted as a UTF-8 String) match the specified String value.
	 * 
	 * @param data
	 *            The bytes to compare.
	 * @param val
	 *            The String to compare the bytes to.
	 * @return true if the bytes match the String.
	 */
	public static boolean BytesMatch(byte[] data, String val)
	{
		if (data == null || data.length == 0)
			return string.IsNullOrEmpty(val);
		else if (string.IsNullOrEmpty(val))
			return false;
		else if (val.length() * 3 < data.length)
			return false;
		try
		{
			String sData = new String(data, "UTF-8");
			return sData.equals(val);
		}
		catch (Exception ex)
		{
			// I'm not necessarily giving this valid UTF-8 so I won't be
			// surprised if an exception is thrown.
		}
		return false;
	}

	/**
	 * Returns true if the specified bytes (interpreted as a UTF-8 String) start with the specified String value.
	 * 
	 * @param data
	 *            The bytes to compare.
	 * @param val
	 *            The String to compare the bytes to.
	 * @return true if the bytes start with the String.
	 */
	public static boolean BytesStartWith(byte[] data, String val)
	{
		if (data == null || data.length == 0)
			return string.IsNullOrEmpty(val);
		else if (string.IsNullOrEmpty(val))
			return true;
		try
		{
			String sData = new String(data, "UTF-8");
			return sData.startsWith(val);
		}
		catch (Exception ex)
		{
			// I'm not necessarily giving this valid UTF-8 so I won't be
			// surprised if an exception is thrown.
		}
		return false;
	}

	/**
	 * Removes from baseVal all occurrences of any of the characters in [chars].
	 * 
	 * @param baseVal
	 *            The String to remove characters from.
	 * @param chars
	 *            The String containing the characters to be removed.
	 * @return A copy of baseVal without any of the chars in [chars].
	 */
	public static String RemoveOccurrencesOfAny(String baseVal, String chars)
	{
		StringBuilder sb = new StringBuilder(baseVal.length());
		for (int i = 0; i < baseVal.length(); i++)
			if (chars.indexOf(baseVal.charAt(i)) == -1)
				sb.append(baseVal.charAt(i));
		return sb.toString();
	}

	/**
	 * Removes any occurrences of the chars in [chars] from the start of baseVal.
	 * 
	 * @param baseVal
	 *            The String to remove characters from.
	 * @param chars
	 *            The String containing the characters to be removed.
	 * @return A copy of baseVal without any of the chars in [chars] at the beginning of baseVal.
	 */
	public static String TrimStart(String baseVal, String chars)
	{
		int idxStart;
		for(idxStart = 0; idxStart < baseVal.length(); idxStart++)
		{
			if (chars.indexOf(baseVal.charAt(idxStart)) == -1)
				break;
		}
		if(idxStart == 0)
			return baseVal;
		else if(idxStart == baseVal.length())
			return "";
		else
			return baseVal.substring(idxStart);
	}
	/**
	 * Removes any occurrences of the chars in [chars] from the end of baseVal.
	 * 
	 * @param baseVal
	 *            The String to remove characters from.
	 * @param chars
	 *            The String containing the characters to be removed.
	 * @return A copy of baseVal without any of the chars in [chars] at the trailing end of baseVal.
	 */
	public static String TrimEnd(String baseVal, String chars)
	{
		int idxLastOkay;
		for(idxLastOkay = baseVal.length() - 1; idxLastOkay > -1 ; idxLastOkay--)
		{
			if (chars.indexOf(baseVal.charAt(idxLastOkay)) == -1)
				break;
		}
		if(idxLastOkay == baseVal.length() - 1)
			return baseVal;
		else if(idxLastOkay < 0)
			return "";
		else
			return baseVal.substring(0, idxLastOkay + 1);
	}

	/**
	 * Removes any occurrences of the chars in [chars] from the beginning and end of baseVal.
	 * 
	 * @param baseVal
	 *            The String to remove characters from.
	 * @param chars
	 *            The String containing the characters to be removed.
	 * @return A copy of baseVal without any of the chars in [chars] at the beginning or at the trailing end of baseVal.
	 */
	public static String Trim(String baseVal, String chars)
	{
		int idxStart;
		for(idxStart = 0; idxStart < baseVal.length(); idxStart++)
		{
			if (chars.indexOf(baseVal.charAt(idxStart)) == -1)
				break;
		}
		if(idxStart == baseVal.length())
			return "";
		int idxLastOkay;
		for(idxLastOkay = baseVal.length() - 1; idxLastOkay > -1 ; idxLastOkay--)
		{
			if (chars.indexOf(baseVal.charAt(idxLastOkay)) == -1)
				break;
		}
		if(idxLastOkay < 0)
			return "";
		
		if(idxStart == 0 && idxLastOkay == baseVal.length() - 1)
			return baseVal;
		else
			return baseVal.substring(idxStart, idxLastOkay + 1);
	}
	/**
	 * Inserts a String inside another String at a specific index.
	 * 
	 * @param originalStr
	 *            The original String to insert the other String into.
	 * @param str
	 *            The String to insert into the original String.
	 * @param idxToInsertAt
	 *            The index to insert at.
	 */
	public static String Insert(String originalStr, String str, int idxToInsertAt)
	{
		if (idxToInsertAt < 0 || idxToInsertAt > originalStr.length())
			return originalStr;
		StringBuilder sb = new StringBuilder(originalStr.length() + str.length());
		sb.append(originalStr.substring(0, idxToInsertAt));
		sb.append(str);
		sb.append(originalStr.substring(idxToInsertAt));
		return sb.toString();
	}
	
	/**
	 * Converts this string to lower case using Locale.US to prevent surprising special cases.
	 * @param originalStr The string to convert to lower case.
	 * @return The lower case string.
	 */
	public static String ToLower(String originalStr)
	{
		return originalStr.toLowerCase(Locale.US);
	}
	
	/**
	 * Converts this string to upper case using Locale.US to prevent surprising special cases.
	 * @param originalStr The string to convert to upper case.
	 * @return The upper case string.
	 */
	public static String ToUpper(String originalStr)
	{
		return originalStr.toUpperCase(Locale.US);
	}

	public static boolean Equals(String s1, String s2)
	{
		if (s1 == null && s2 == null)
			return true;
		if (s1 == null || s2 == null)
			return false;
		return s1.equals(s2);
	}
}
