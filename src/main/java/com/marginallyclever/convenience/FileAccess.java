package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Methods to make loading files from disk or jar resource easier.
 * 
 * @author Dan Royer
 */
public class FileAccess {

	private static final Logger logger = LoggerFactory.getLogger(FileAccess.class);
	/**
	 * Open a file.  open() looks in three places:<br>
	 *  - The file may be contained inside a zip, as indicated by the filename "zipname:filename".<br>
	 *  - The file may be a resource inside a jar file.
	 *  - The file may be on disk.
	 *     
	 * @param filename The file to open
	 * @return BufferedInputStream to the file contents
	 * @throws IOException file open failure
	 */
	public static BufferedInputStream open(String filename) throws IOException {
		int index = filename.lastIndexOf(":");
		int index2 = filename.lastIndexOf(":\\");  // hack for windows file system
		if(index!=-1 && index!=index2) {
			return loadFromZip(filename.substring(0, index), filename.substring(index+1,filename.length()));
		} else {
			return new BufferedInputStream(getInputStream(filename));
		}
	}
		
	private static InputStream getInputStream(String fname) throws IOException {
		InputStream s = FileAccess.class.getResourceAsStream(fname);
		if( s==null ) {
			s = new FileInputStream(new File(fname));
		}
		return s;
	}
		
	private static BufferedInputStream loadFromZip(String zipName,String fname) throws IOException {
		ZipInputStream zipFile=null;
		ZipEntry entry;
		
		zipFile = new ZipInputStream(getInputStream(zipName));
		
		String fnameSuffix = fname.substring(fname.lastIndexOf(".")+1);
		String fnameNoSuffix = fname.substring(0,fname.length()-(fnameSuffix.length()+1));

		while((entry = zipFile.getNextEntry())!=null) {
	        if( entry.getName().equals(fname) ) {
	        	File f = createTempFile(fnameNoSuffix, fnameSuffix);                
	        	readZipFileIntoTempFile(zipFile,f);
		        // return temp file as input stream
                return new BufferedInputStream(new FileInputStream(f));
	        }
	    }
		    
	    zipFile.close();

	    throw new IOException("file not found in zip.");
	}

	private static File createTempFile(String fnameNoSuffix, String fnameSuffix) throws IOException {
        File f = File.createTempFile(fnameNoSuffix, fnameSuffix);
        f.setReadable(true);
        f.setWritable(true);
        f.deleteOnExit();
		return f;
	}

	private static void readZipFileIntoTempFile(ZipInputStream zipFile, File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
		byte[] buffer = new byte[2048];
		int len;
        while ((len = zipFile.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
	}

	/**
	 * Return the current directory
	 * @return the current directory
	 */
	public static String getUserDirectory() {
		return System.getProperty("user.dir");
	}
	
	public static String getTempDirectory() { 
		return System.getProperty("java.io.tmpdir");
	}
	
	public static String getWorkingDirectory() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		return s;
	}

	/**
	 * https://stackoverflow.com/a/7322581
	 * @param file file to read
	 * @return the last line in the file
	 */
	public static String tail( File file ) {
		try (RandomAccessFile fileHandler = new RandomAccessFile( file, "r" )) {
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for(long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek( filePointer );
				int readByte = fileHandler.readByte();

				if( readByte == 0xA ) {  // 10, line feed, '\n'
					if( filePointer == fileLength ) {
						continue;
					}
					break;
				} else if( readByte == 0xD ) {  // 13, carriage-return '\r'
					if( filePointer == fileLength - 1 ) {
						continue;
					}
					break;
				}

				sb.append( ( char ) readByte );
			}

			return sb.reverse().toString();
		} catch(IOException e ) {
			logger.warn("Failed to read the last lines of the file {}", file, e);
			return "";
		}
	}
}
