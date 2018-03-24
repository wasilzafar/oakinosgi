package com.wzee.oak;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LMSLogManager {

	Handler consoleHandler = null;
	Handler fileHandler  = null;
	private static String LOGDIRECTORY = "logs";
	private static String LOGFILENAME = "start.log";
	private String lmsHome = null;
	public static final Logger LOGGER = Logger.getAnonymousLogger();
	public LMSLogManager(String lmsHome) {
		this.lmsHome = lmsHome;
	}
	public void setUpLogger() {
		String logFilePath = DiskUtils.CURRENTDIRECTORY + File.separator
				+ lmsHome + File.separator
				+ LOGDIRECTORY + File.separator + LOGFILENAME;
		File logFile = null;
		try {
			logFile = DiskUtils.createFileInDirectory(logFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fileHandler = new FileHandler(logFile.getPath());
			fileHandler.setFormatter(new Formatter() {
				public String format(LogRecord record) {
					return record.getLevel() + "  :  "+ record.getSourceClassName() + "  :  " + record.getMessage()
							+ "\n";
				}
			});
			consoleHandler = new ConsoleHandler();
		} catch (SecurityException e) {
			log(e.getMessage(), Level.SEVERE);
		} catch (IOException e) {
			log(e.getMessage(), Level.SEVERE);
		}

		// Assigning handlers to LOGGER object
		for (Handler iHandler : LOGGER.getParent().getHandlers()) {
			// LOGGER.getParent().removeHandler(iHandler);
		}

		LOGGER.setUseParentHandlers(false);
		consoleHandler.setLevel(Level.INFO);
		fileHandler.setLevel(Level.INFO);
		LOGGER.setLevel(Level.INFO);
		LOGGER.addHandler(consoleHandler);
		LOGGER.addHandler(fileHandler);
	}
	
	public File getLogsDirectory(){
		return new File(DiskUtils.CURRENTDIRECTORY + File.separator
				+ lmsHome + File.separator
				+ LOGDIRECTORY);		
	}
	
	private void log(String message, Level severe) {
		
		
	}

	public Logger setUpFelixLogger(Properties props){
		return null;
	}
}
