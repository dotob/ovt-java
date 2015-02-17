package tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class Updater {

	private static String newVersionString;

	public static boolean checkVersion(String updateURLString,
			String downloadURL, String aktVersion) {
		boolean ret = true;
		String ignoreUpdate = SettingsReader.getString("OVT.noupdate");
		if (ignoreUpdate != null && ignoreUpdate.length() > 0) {
			return ret;
		}
		String erg = "";
		String err = "";
		if (aktVersion.equals("buildnumber")) {
			return true;
		}
		try {
			// get last version info from internet
			URL updateURL = new URL(updateURLString);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					updateURL.openStream()));
			newVersionString = "";
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				newVersionString += inputLine;
			}

			// check version string
			if (!newVersionString.equals(aktVersion)) {
				erg = "<html>Es liegt eine neuere Version vor."
						+ "<br><b>Bitte downloaden und installieren Sie diese unbedingt.</b>"
						+ "<br>Ihre Version: <b>" + aktVersion
						+ "</b> aktuelle Version: <b>" + newVersionString
						+ "</b></html>";
			}

			// close stream
			in.close();
		} catch (FileNotFoundException e) {
			err = "Kein Updateinformationen im Internet gefunden ("
					+ updateURLString + ")";
			// e.printStackTrace();
		} catch (UnknownHostException e) {
			err = "Kein Zugang zum Internet gefunden.";
			// e.printStackTrace();
		} catch (ConnectException e) {
			err = "Kein Zugang zum Internet gefunden.";
			// e.printStackTrace();
		} catch (MalformedURLException e) {
			err = "Kein Zugang zum Internet gefunden.";
			// MyLog.exceptionError(e);
			e.printStackTrace();
		} catch (IOException e) {
			err = "Kein Zugang zum Internet gefunden.";
			// MyLog.exceptionError(e);
			e.printStackTrace();
		}

		if (erg.length() > 0 && err.length() == 0) {
			JOptionPane.showMessageDialog(null, erg);
			SysTools.ShowInBrowser(downloadURL);
			ret = false;
		} else if (erg.length() == 0 && err.length() > 0) {
			JOptionPane.showMessageDialog(null, err);
			ret = false;
		}
		return ret;
	}

	public static String getNewVersionString() {
		return newVersionString;
	}
}
