package tools;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class SysTools {

	public static void ShowInBrowser(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (Exception /* IOException, URISyntaxException */e) {
			e.printStackTrace();
		}
	}

	public static void OpenFile(File file) {
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			MyLog.showExceptionErrorDialog(e);
		}
		// Execute("rundll32 SHELL32.DLL,ShellExec_RunDLL " +
		// file.getAbsolutePath());
	}

	public static void OpenFile(String fileName) {
		OpenFile(new File(fileName));
	}

	public static void Execute(String toExe) {
		try {
			Runtime.getRuntime().exec(toExe);
		} catch (Exception /* IOException, URISyntaxException */e) {
			e.printStackTrace();
		}
	}

	public static String QueryRegistry(String q) {
		try {
			String query = "reg query " + q;
			MyLog.logDebug("execute registry query: " + query);
			Process process = Runtime.getRuntime().exec(query);

			StringBuilder sb = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String currentLine = null;
			while ((currentLine = in.readLine()) != null) {
				sb.append(currentLine);
				sb.append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public static boolean IsKeyPresent(String q) {
		return QueryRegistry(q).trim().length() > 0;
	}

	public static String GetUserHomeDirectory() {
		// This is the key that we used to obtain user home directory
		// in the operating system
		String userHome = "user.home";
		// We get the path by getting the system property with the
		// defined key above.
		String path = System.getProperty(userHome);
		return path;
	}

	public static void main(String[] args) {
		System.out.println("-----QueryRegistry");
		System.out.println(QueryRegistry("HKEY_CURRENT_USER\\Software\\sipgate\\X-Lite"));
		System.out.println("-----IsKeyPresent");
		System.out.println(IsKeyPresent("HKEY_CURRENT_USER\\Software\\sipgate\\X-Lite"));
		System.out.println("-----GetUserHomeDirectory");
		System.out.println(GetUserHomeDirectory());
		System.out.println("-----");
	}
}
