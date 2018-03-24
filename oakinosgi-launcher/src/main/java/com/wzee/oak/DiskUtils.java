package com.wzee.oak;

import java.io.File;
import java.io.IOException;

public class DiskUtils {
	public static String CURRENTDIRECTORY = ".";

	public static File createDirectory(String path) {
		File directory = null;
		boolean created = false;
		if (path != null && isFileNameValid(path)) {
			directory = new File(path);
			if (directory.exists()){
				created = true;
		} else {
			created = directory.mkdir();
		}
		}
		return created ? directory : null;

	}
	
	public static boolean isFileNameValid(String file) {
	    File f = new File(file);
	    try {
	       f.getCanonicalPath();
	       return true;
	    }
	    catch (IOException e) {
	       return false;
	    }
	  }
	
	public static File createFile(String file) throws IOException{
		File fileCreated = null;
		boolean created = false;
		if (file != null && isFileNameValid(file)) {
			fileCreated = new File(file);
			if (fileCreated.exists()){
				created = true;
		} else {
			created = fileCreated.createNewFile();
		}
		}
		return created ? fileCreated : null;
		
	}
	
	public static File createFileInDirectory(String filePath) throws IOException{
		File fileCreated = null;
		boolean created = false;
		if (filePath != null && isFileNameValid(filePath)) {
			fileCreated = new File(filePath);
			if (fileCreated.getParentFile() != null){
				fileCreated.getParentFile().mkdirs(); 
				fileCreated.createNewFile();
				created = true;
		} else {
			throw new IOException("Invalid Path : "+ filePath);
		}
			
		}
		return created ? fileCreated : null;
		
	}
}
