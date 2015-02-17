package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.commons.net.ftp.FTPClient;

public class FTPTool {

	private static String tmpFileName;

	public static Thread sendLogFile(String name) {
		tmpFileName = name;
		Runnable r = new Runnable() {
			public void run() {
				File file = new File(MyLog.GetCompleteLogFileName());
				if (file.exists()) {
					try {
						MyLog.logDebug("start with ftp");
						ProgressMonitor pm = new ProgressMonitor(null, "Protokolldatei senden", "", 0, 10);
						pm.setMillisToPopup(10);
						pm.setMillisToDecideToPopup(2);
						pm.setProgress(2);
						pm.setNote("Verbinde zum Server...");
						FTPClient f = new FTPClient();
						f.connect("overtuer.de");
						f.login("mflogger", "gubKxJir4E");
						// f.changeWorkingDirectory("files");
						MyLog.logDebug("got connected, logged in and changeddir");
						pm.setNote("Sende Datei...");
						pm.setProgress(6);
						InputStream is = new FileInputStream(file);
						String nowStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
						String remoteFileName = "logfile_" + tmpFileName + "_" + nowStr + ".log";
						f.storeFile(remoteFileName, is);
						f.disconnect();
						MyLog.logDebug("file transferred and disconnected");
						pm.setProgress(10);
						pm.setNote("Fertig...");
						pm.close();
						is.close();
						JOptionPane.showMessageDialog(null, "Datei versandt. Vielen Dank!");
					} catch (SocketException e) {
						MyLog.showExceptionErrorDialog(e);
					} catch (IOException e) {
						MyLog.showExceptionErrorDialog(e);
					} catch (Exception e) {
						MyLog.showExceptionErrorDialog(e);
					}
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		return t;
	}
}
