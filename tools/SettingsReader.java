package tools;

/**
 * @author basti
 * this class can retrieve and store values into properties files. that means normally are all 
 * values in a properties file in the jar package. but when we want to change a value i couldnt 
 * help me but write a new file outside the jar file. values in that file are preferred, so when 
 * there are changed values you should get them instead of the original ones.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsReader {
	private static String EXTENDED_PROPERTIES_FILENAME = "ovt_extended.properties";
	private static final String BUNDLE_NAME = "ovt";

	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);
	private static Properties EXTENDED_PROPERTIES;

	private static boolean inited = false;

	/**
	 * main init method could be called anytime
	 */
	public static void init() {
		// only do something if props is null
		if (!inited && EXTENDED_PROPERTIES == null) {
			// try to load from plain file if exists
			EXTENDED_PROPERTIES = initExtendedPropertiesFile();
			inited = true;
		}
	}

	/**
	 * try to load a plain properties file that is not in the jar file. that
	 * happens if some values in the jar files properties are changed and need
	 * to be stored anywhere. than the peoperties in that file are read in an
	 * get a higher priority
	 * 
	 * @return the extended properties file
	 */
	public static Properties initExtendedPropertiesFile() {
		Properties ret = new Properties();
		try {
			File test = new File(EXTENDED_PROPERTIES_FILENAME);
			if (test != null && test.exists()) {
				MyLog.logDebug("try to read properties from plain file");
				ret.load(new FileInputStream(EXTENDED_PROPERTIES_FILENAME));
			} else {
				String userPath = SysTools.GetUserHomeDirectory()
						+ File.separator;
				EXTENDED_PROPERTIES_FILENAME = userPath
						+ EXTENDED_PROPERTIES_FILENAME;
				test = new File(EXTENDED_PROPERTIES_FILENAME);
				if (test != null && test.exists()) {
					MyLog
							.logDebug("try to read properties from plain file in user dir");
					ret.load(new FileInputStream(EXTENDED_PROPERTIES_FILENAME));
				} else {
					MyLog.logError("couldnt find plain properties file: "
							+ EXTENDED_PROPERTIES_FILENAME);
					ret = null;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// MyLog.exceptionError(e);
		} catch (IOException e) {
			e.printStackTrace();
			// MyLog.exceptionError(e);
		} finally {
			// if properties file couldnt be found or is empty set it to null
			if (ret != null && ret.isEmpty()) {
				ret = null;
				MyLog
						.logError("SettingsReader::no plain properties file found");
			}
		}
		return ret;
	}

	/**
	 * get a string value from properties file, extended properties (that where
	 * saved before) are preferred
	 * 
	 * @param key
	 *            the key of the value to get
	 * @return the value that belongs to the key
	 */
	public static String getString(String key) {
		MyLog.logDebug("SettingsReader::getString > " + key);
		init();
		String ret = "";
		// at first try to read the value from the extended, means previously
		// saved, properties file
		if (EXTENDED_PROPERTIES != null) {
			try {
				ret = EXTENDED_PROPERTIES.getProperty(key);
				MyLog.logDebug("SettingsReader::getString >> " + key + "="
						+ ret);
			} catch (RuntimeException e) {
				// MyLog.exceptionError(e);
				e.printStackTrace();
			}
		}
		// if not in properties file assume theres no one and try to load from
		// resbundle
		if (ret == null || (ret != null && ret.length() <= 0)) {
			try {
				ret = RESOURCE_BUNDLE.getString(key);
				MyLog.logDebug("SettingsReader::getString >> " + key + "="
						+ ret);
			} catch (MissingResourceException e) {
				// MyLog.exceptionError(e);
				ret = "";// '!' + key + '!';
			}
		}
		return ret;
	}

	/**
	 * set a value in the properties file, when there was never a properties
	 * file, probably when program is started for the first time, create a new
	 * properties set
	 * 
	 * @param key
	 *            the key to save to
	 * @param value
	 *            the value for given key
	 */
	public static void setValue(String key, String value) {
		// TODO do we have to load the setting into props file???
		if (EXTENDED_PROPERTIES == null) {
			EXTENDED_PROPERTIES = new Properties();
		}
		if (EXTENDED_PROPERTIES != null) {
			MyLog.logDebug("SettingsReader::setValue > " + key + "=" + value);
			EXTENDED_PROPERTIES.put(key, value);
		}
	}

	/**
	 * save the properties to the extended properties file
	 */
	public static void saveProperties() {
		try {
			boolean doit = false;
			File test = new File(EXTENDED_PROPERTIES_FILENAME);
			if (test != null && test.canWrite()) {
				MyLog.logDebug("SettingsReader::saveProperties.progdir");
				doit = false;
			} else {
				test = new File(EXTENDED_PROPERTIES_FILENAME);
				if (test != null && test.canWrite()) {
					String userPath = SysTools.GetUserHomeDirectory()
							+ File.separator;
					EXTENDED_PROPERTIES_FILENAME = userPath
							+ EXTENDED_PROPERTIES_FILENAME;
					MyLog.logDebug("SettingsReader::saveProperties.userdir");
					doit = false;
				}
			}
			if (doit) {
				FileOutputStream out = new FileOutputStream(
						EXTENDED_PROPERTIES_FILENAME);
				MyLog.logDebug("SettingsReader::saveProperties");
				EXTENDED_PROPERTIES
						.store(out, "OVT Overtuer Einstellungsdatei");
			}
		} catch (FileNotFoundException e) {
			// MyLog.showExceptionErrorDialog(e);
			MyLog.logError(e);
			e.printStackTrace();
		} catch (IOException e) {
			// MyLog.showExceptionErrorDialog(e);
			MyLog.logError(e);
			e.printStackTrace();
		} catch (Exception e) {
			// MyLog.showExceptionErrorDialog(e);
			MyLog.logError(e);
			e.printStackTrace();
		}
	}
}