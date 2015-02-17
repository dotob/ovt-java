package tools;

public class SipHelper {

	private static String enabledStr = SettingsReader.getString("Sip.enabled");
	private static String exe = SettingsReader.getString("Sip.executable");
	private static String param = SettingsReader.getString("Sip.option");
	private static String regFile = SettingsReader.getString("Sip.regfile");
	private static String regKey = SettingsReader.getString("Sip.regkeycheck");

	public static void CallNumber(String nr) {
		Register();
		String e = exe + " " + nr;
		MyLog.logDebug("initated sip call with: " + e);
		MyLog.o("initated sip call with: " + e);
		SysTools.Execute(e);
	}

	public static void Register() {
		// check for regkey
		if (!SysTools.IsKeyPresent(regKey)) {
			SysTools.Execute("reg import " + regFile);
			MyLog.logDebug("registered sip xlite settings of file: " + regFile);
		}
	}

	public static boolean IsSipEnabled() {
		return enabledStr.equals("yes");
	}
}
