package org.brian.blueirisviewer.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Encryption
{
	/**
	 * Hard coded encryption key for storing settings.
	 */
	private static final SecretKey key1 = getKey(new byte[] { (byte) 110, 42, (byte) 320, (byte) 255, 7, (byte) 132,
			(byte) 133, 0, 64, 110, (byte) 223, 72, (byte) 629, (byte) 176, 30, 47 });

	/**
	 * Returns a Hex encoded String representing the encrypted version of the original text. If anything goes wrong, the
	 * passed String is returned unchanged. The key used differs from the key used by the byte[] encryption function.
	 * 
	 * @param text
	 *            The String to encrypt.
	 * @return The encrypted String encoded in Hex (or the unchanged passed String if something went wrong).
	 */
	public static String Encrypt(String text)
	{
		try
		{
			text = EncryptInternal(text);
		}
		catch (Exception ex)
		{
			Logger.debug(ex, Encryption.class);
		}
		return text;
	}

	/**
	 * Returns the decrypted String. If anything goes wrong, the passed String is returned unchanged. If the passed
	 * string is null or empty, "" is returned without involving decryption code. The key used differs from the key used
	 * by the byte[] decryption function.
	 * 
	 * @param text
	 *            The String to decrypt.
	 * @return The decrypted String (or the unchanged passed String if something went wrong).
	 */
	public static String Decrypt(String text)
	{
		if (string.IsNullOrEmpty(text))
			return "";
		try
		{
			text = DecryptInternal(text);
		}
		catch (Exception ex)
		{
			Logger.debug(ex, Encryption.class);
		}
		return text;
	}

	private static String EncryptInternal(String plainText) throws Exception
	{
		if (key1 == null)
			return plainText;
		byte[] decrypted = plainText.getBytes("UTF-8");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key1);
		byte[] encrypted = cipher.doFinal(decrypted);
		return BytesToHex(encrypted);
	}

	private static String DecryptInternal(String cipherText) throws Exception
	{
		if (key1 == null)
			return cipherText;
		byte[] encrypted = HexToBytes(cipherText);
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key1);
		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted, "UTF-8");
	}

	/**
	 * Returns the encrypted version of the original bytes. If anything goes wrong, the passed data is returned
	 * unchanged. The key used differs from the key used by the String encryption function.
	 * 
	 * @param data
	 *            The data to encrypt.
	 * @return The encrypted data (or the unchanged passed data if something went wrong).
	 */
	public static byte[] Encrypt(byte[] data)
	{
		try
		{
			data = EncryptInternal(data);
		}
		catch (Exception ex)
		{
			Logger.debug(ex, Encryption.class);
		}
		return data;
	}

	/**
	 * Returns the decrypted data. If anything goes wrong, the passed data is returned unchanged. If the passed data is
	 * null or empty, an empty byte[] is returned without involving decryption code. The key used differs from the key
	 * used by the String decryption function.
	 * 
	 * @param data
	 *            The data to decrypt.
	 * @return The decrypted data (or the unchanged passed data if something went wrong).
	 */
	public static byte[] Decrypt(byte[] data)
	{
		if (data == null || data.length == 0)
			return new byte[0];
		try
		{
			data = DecryptInternal(data);
		}
		catch (Exception ex)
		{
			Logger.debug(ex, Encryption.class);
		}
		return data;
	}

	private static byte[] EncryptInternal(byte[] plainText) throws Exception
	{
		if (key1 == null)
			return plainText;
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key1);
		byte[] encrypted = cipher.doFinal(plainText);
		return encrypted;
	}

	private static byte[] DecryptInternal(byte[] cipherText) throws Exception
	{
		if (key1 == null)
			return cipherText;
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key1);
		byte[] decrypted = cipher.doFinal(cipherText);
		return decrypted;
	}

	private static SecretKey getKey(byte[] seed)
	{
		try
		{
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(seed);
			keyGenerator.init(128, secureRandom); // 128 is the safest one to
													// use for compatibility?
			SecretKey skey = keyGenerator.generateKey();
			return skey;
		}
		catch (Exception ex)
		{
			Logger.debug(ex, Encryption.class);
		}
		return null;
	}

	public static byte[] HexToBytes(String str)
	{
		int length = str.length() / 2;
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = Integer.valueOf(str.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;
	}

	private final static String HexChars = "0123456789ABCDEF";

	public static String BytesToHex(byte[] bytes)
	{
		if (bytes == null)
			return "";
		StringBuilder result = new StringBuilder(2 * bytes.length);
		for (int i = 0; i < bytes.length; i++)
			result.append(HexChars.charAt((bytes[i] >> 4) & 0x0f)).append(HexChars.charAt(bytes[i] & 0x0f));
		return result.toString();
	}
}