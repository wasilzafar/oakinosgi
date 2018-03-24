package com.wzee.oak;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class LoggerDelegate extends Logger {

	private static final String STARTUP_LOG_FILE = "framework.log";
	private static String LOGDIRECTORY = "logs";
	private static String homeDirectory = null;
	private static PrintStream printStream;

	private File createLogFile() throws IOException {
		File logHome = new File(DiskUtils.CURRENTDIRECTORY + File.separator	+ homeDirectory + File.separator+ LOGDIRECTORY);	
		return DiskUtils.createFileInDirectory(logHome
				.getAbsolutePath() + File.separator + STARTUP_LOG_FILE);

	}

	public void setUpPrintStream(PrintStream outStream) {
		if (outStream == null)
			printStream = outStream;

	}

	public LoggerDelegate(LMSLogManager lmsLogger, String home) throws FileNotFoundException,
			IOException {
		homeDirectory = home;
		if (printStream == null) {
			printStream = new PrintStream(createLogFile());
			System.setOut(printStream);
			System.setErr(printStream);
		}
	}

	protected void doLog(Bundle bundle, ServiceReference sr, int level,
			String msg, Throwable throwable) {

		// unwind throwable if it is a BundleException
		if ((throwable instanceof BundleException)
				&& (((BundleException) throwable).getNestedException() != null)) {
			throwable = ((BundleException) throwable).getNestedException();
		}

		final StringBuilder sb = new StringBuilder();
		if (sr != null) {
			sb.append("SvcRef ");
			sb.append(sr);
			sb.append(" ");
		} else if (bundle != null) {
			sb.append("Bundle '");
			sb.append(String.valueOf(bundle.getBundleId()));
			sb.append("' ");
		}
		sb.append(msg);
		if (throwable != null) {
			sb.append(" (");
			sb.append(throwable);
			sb.append(")");
		}
		final String s = sb.toString();

		switch (level) {
		case LOG_DEBUG:
			debug("DEBUG: " + s);
			break;
		case LOG_INFO:
			info("INFO: " + s, throwable);
			break;
		case LOG_WARNING:
			warn("WARNING: " + s, throwable);
			break;
		case LOG_ERROR:
			error("ERROR: " + s, throwable);
			break;
		default:
			warn("UNKNOWN[" + level + "]: " + s, null);
		}
	}

	// emit an debugging message to standard out
	public static void debug(String message, Throwable t) {
		log(printStream, "*DEBUG #", message, t);
	}

	// emit an informational message to standard out
	public static void info(String message, Throwable t) {
		log(printStream, "*INFO #", message, t);
	}

	// emit an warning message to standard out
	public static void warn(String message, Throwable t) {
		log(printStream, "*WARN #", message, t);
	}

	// emit an error message to standard err
	public static void error(String message, Throwable t) {
		log(printStream, "*ERROR #", message, t);
	}

	private static final DateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS ");

	// helper method to format the message on the correct output channel
	// the throwable if not-null is also prefixed line by line with the prefix
	private static void log(PrintStream out, String prefix, String message,
			Throwable t) {

		final StringBuilder linePrefixBuilder = new StringBuilder();
		synchronized (fmt) {
			linePrefixBuilder.append(fmt.format(new Date()));
		}
		linePrefixBuilder.append(prefix);
		linePrefixBuilder.append(" [");
		linePrefixBuilder.append(Thread.currentThread().getName().toUpperCase());
		linePrefixBuilder.append("] ");
		final String linePrefix = linePrefixBuilder.toString();

		synchronized (out) {
			out.print(linePrefix);
			out.println(message);
			if (t != null) {
				t.printStackTrace(new PrintStream(out) {
					@Override
					public void println(String x) {
						synchronized (this) {
							print(linePrefix);
							super.println(x);
							flush();
						}
					}
				});
			}
		}
	}
	
	@Override
	protected void doLog(int level, String msg, Throwable throwable) {
		if (level <= this.getLogLevel()) {
			String s = msg;
			if (throwable != null) {
				s = msg + " (" + throwable + ")";
			}

			switch (level) {
				case 1 :
					log(printStream, "*ERROR #", s, throwable);
					if (throwable != null) {
						throwable.printStackTrace();
					}
					break;
				case 2 :
					log(printStream, "*WARNING #", s, throwable);
					break;
				case 3 :
					log(printStream, "*INFO #", s, throwable);
					break;
				case 4 :
					log(printStream, "*DEBUG #", s, throwable);
					break;
				default :
					log(printStream, "UNKNOWN[" + level + "]: ", s, throwable);
			}

		}
	
		
	}
}