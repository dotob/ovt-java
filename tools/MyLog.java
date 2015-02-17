package tools;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xnap.commons.gui.Dialogs;

public class MyLog {

	static final String logFileName = "ovt.log";
	static String completeLogFileName = logFileName;

	public static String GetCompleteLogFileName() {
		return completeLogFileName;
	}

	static Logger logger = Logger.getLogger(MyLog.class);
	static boolean inited = false;

	public static void init() {
		if (!inited) {
			FileAppender appender = null;
			PatternLayout patternLayout = new PatternLayout(
					"%d{ISO8601} : %m %n");
			try {
				// check if file is too big
				File logFile = new File(logFileName);
				if (logFile.exists() && logFile.length() > 5000000) {
					logFile.delete();
				}

				appender = new FileAppender(patternLayout, logFileName);
				logger.addAppender(appender);
				inited = true;
			} catch (Exception e) {
				// JOptionPane.showMessageDialog(null,
				// "Datei kann nicht in Programmverzeichnis geschrieben werden.\nWahrscheinlich fehlende Zugriffsrechte!"
				// , "Fehler", JOptionPane.ERROR_MESSAGE);
				appender = null;
			}

			if (appender == null) {
				String path = SysTools.GetUserHomeDirectory();
				try {
					String logPath = path + File.separator + logFileName;
					appender = new FileAppender(patternLayout, logPath);
					completeLogFileName = logPath;
					logger.addAppender(appender);
					inited = true;
				} catch (Exception e) {
					// JOptionPane.showMessageDialog(null,
					// "Datei kann nicht ins Benutzerverzeichnis " +
					// completeFogFileName +
					// " geschrieben werden.\nWahrscheinlich fehlende Zugriffsrechte!"
					// , "Fehler", JOptionPane.ERROR_MESSAGE);
					logger = null;
				}
			}
		}
	}

	public static void logDebug(String s) {
		init();
		o(s);
		if (logger != null) {
			logger.debug(s);
		}
	}

	public static void logError(String s) {
		init();
		// o(s);
		if (logger != null) {
			logger.error(s);
		}
	}

	public static void logError(Exception e) {
		init();
		// o(s);
		e.printStackTrace();
		logError(Arrays.toString(e.getStackTrace()));
	}

	public static void logError(String s, Exception e) {
		init();
		// o(s);
		e.printStackTrace();
		logError(s + Arrays.toString(e.getStackTrace()));
	}

	public static void o(Object o) {
		System.out.println(o);
	}

	public static void showExceptionErrorDialog(Exception e) {
		logError(e);
		Dialogs.showError(null, "Software Fehler", e);
	}

	public static void showOnlyExceptionErrorDialog(Exception e) {
		Dialogs.showError(null, "Software Fehler", e);
	}

	public static void showExceptionErrorDialog(String s, Exception e) {
		logError(s, e);
		Dialogs.showError(null, "Software Fehler", e);
	}
}
