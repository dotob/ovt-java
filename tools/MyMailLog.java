package tools;

import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.net.SMTPAppender;

public class MyMailLog {

	static final String logFileName = "ovt.log";
	static Logger mailLogger = Logger.getLogger(MyMailLog.class);
	static boolean inited = false;

	private static void init() {
		if (!inited) {
			// TODO: not working with tls which gmail wants
			SMTPAppender mailapp = null;
			try {
				mailapp = new SMTPAppender();
				mailapp.setFrom("sk@dotob.de");
				mailapp.setSMTPHost("smtp.gmail.com");
				mailapp.setSMTPPassword("315betty");
				mailapp.setSMTPUsername("speedcow@gmail.com");
				mailapp.setSubject("rmk mfassi error");
				mailapp.setTo("speedcow@gmail.com");
				mailapp.setSMTPDebug(true);
				mailapp.setBufferSize(5);
				mailapp.activateOptions();
				mailapp.setLayout(new SimpleLayout());
				mailLogger.addAppender(mailapp);
			} catch (Exception ex) {
				MyLog.logError(ex);
			}
		}
	}

	public static void logErrorMail(String s) {
		init();
		// o(s);
		try {
			// mailLogger.error(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
