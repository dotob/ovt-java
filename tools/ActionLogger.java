package tools;

import java.io.File;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import db.Aktion;
import db.Contact;

public class ActionLogger {

	public static final String logFileName = "aktion.log";
	static Logger logger = Logger.getLogger(ActionLogger.class);
	static boolean inited = false;

	public static void init() {
		if (!inited) {
			FileAppender appender = null;
			try {
				// check if file is too big
				File logFile = new File(logFileName);
				if (logFile.exists() && logFile.length() > 5000000) {
					logFile.delete();
				}

				appender = new FileAppender(new PatternLayout("%m %n"), logFileName);
				logger.addAppender(appender);
				inited = true;
			} catch (Exception e) {
				// what now?
			}
		}
	}

	public static void logAktion(Aktion a, Contact c) {
		init();
		logger.info(a.toNoHTMLString() + " für " + c);
	}
}
