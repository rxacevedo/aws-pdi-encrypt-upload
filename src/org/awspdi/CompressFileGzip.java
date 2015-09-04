package org.awspdi;

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
	 * 
	 * @param sourceFilepath source file
	 * @param destinatonZipFilepath (.gzip will be added)
	 */
	public final void gzipFile(final String sourceFilepath, 
			final String destinatonZipFilepath) {

		byte[] buffer = new byte[1024];

		try {
			
			FileOutputStream fileOutputStream = 
					new FileOutputStream(destinatonZipFilepath + ".gzip");
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