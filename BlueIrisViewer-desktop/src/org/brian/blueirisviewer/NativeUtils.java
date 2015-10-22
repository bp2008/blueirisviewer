package org.brian.blueirisviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple library class for working with JNI (Java Native Interface)
 * 
 * @see http://adamheinrich.com/2012/how-to-load-native-jni-library-from-jar
 *
 * @author Adam Heirnich &lt;adam@adamh.cz&gt;, http://www.adamh.cz
 */
public class NativeUtils
{

	/**
	 * Private constructor - this class will never be instanced
	 */
	private NativeUtils()
	{
	}

	/**
	 * Loads library from current JAR archive
	 * 
	 * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after
	 * exiting. Method uses String as filename because the pathname is "abstract", not system-dependent.
	 * 
	 * @param path
	 *            The filename inside JAR as absolute path (beginning with '/'), e.g. "/libturbojpeg32.dll" or
	 *            "/libturbojpeg64.dll"
	 * @param outPath
	 *            The path on disk to write the file to. e.g. "libturbojpeg.dll"
	 * @throws IOException
	 *             If temporary file creation or read/write operation fails
	 * @throws IllegalArgumentException
	 *             If source file (param path) does not exist
	 * @throws IllegalArgumentException
	 *             If the path is not absolute or if the filename is shorter than three characters (restriction of
	 *             {@see File#createTempFile(java.lang.String, java.lang.String)}).
	 * @return True if successful. Exception otherwise.
	 */
	public static boolean extractFileFromJar(String path, String outPath) throws IOException
	{
		if (!path.startsWith("/"))
		{
			throw new IllegalArgumentException("The path has to be absolute (start with '/').");
		}

		// Obtain filename from path
		String[] parts = path.split("/");
		String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

		// Split filename to prexif and suffix (extension)
		String prefix = "";
		if (filename != null)
		{
			parts = filename.split("\\.", 2);
			prefix = parts[0];
		}

		// Check if the filename is okay
		if (filename == null || prefix.length() < 3)
		{
			throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
		}

		// Prepare temporary file
		File outFile = new File(outPath);

		if (outFile.exists())
		{
			outFile.delete();
		}

		// Prepare buffer for data copying
		byte[] buffer = new byte[1024];
		int readBytes;

		// Open and check input stream
		InputStream is = NativeUtils.class.getResourceAsStream(path);
		if (is == null)
		{
			throw new FileNotFoundException("File " + path + " was not found inside JAR.");
		}

		// Open output stream and copy data between source file in JAR and the temporary file
		OutputStream os = null;
		try
		{
			os = new FileOutputStream(outFile);
			while ((readBytes = is.read(buffer)) != -1)
			{
				os.write(buffer, 0, readBytes);
			}
		}
		catch (FileNotFoundException ex)
		{
			// Eat this error; it is almost certainly caused by the file being open by another instance of the app.
		}
		finally
		{
			// If read/write fails, close streams safely before throwing an exception
			if (os != null)
				os.close();
			is.close();
		}
		return true;
	}
}