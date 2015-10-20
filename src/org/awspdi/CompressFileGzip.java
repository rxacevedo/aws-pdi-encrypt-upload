package org.awspdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author Kristofer Ranström
 *
 */
public class CompressFileGzip {
	
	/**
	 * Drag and drop it, zip, unzip it...
	 * 
	 * If we desire to gzip the file before upload
	 * then we rely on this class to handle the
	 * zipping process.
	 * 
	 * @param sourceFilepath source file
	 * @param destinatonZipFilepath (.gzip will be added)
	 */
	public final void gzipFile(final String sourceFilepath, 
			final String destinatonZipFilepath) {

		byte[] buffer = new byte[1024];

		try {
			File file = new File(sourceFilepath);
			
			FileOutputStream fileOutputStream = 
					new FileOutputStream(destinatonZipFilepath + "/"
							+ file.getName() + ".gzip");
			GZIPOutputStream gzipOuputStream = 
					new GZIPOutputStream(fileOutputStream);
			FileInputStream fileInput = new FileInputStream(sourceFilepath);

			int bytesRead;
			
			while ((bytesRead = fileInput.read(buffer)) > 0) {
				gzipOuputStream.write(buffer, 0, bytesRead);
			}

			fileInput.close();

			gzipOuputStream.finish();
			gzipOuputStream.close();

			System.out.println("The file was compressed successfully!");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}