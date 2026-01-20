package main.java.com.linkmanager.app.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ian Gardea
 *
 */
public class AppConfig {

	private static AppConfig instance;

	private Pattern _section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
	private Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
	private Map<String, Map<String, String>> _entries = new HashMap<>();

	private String lastKey; // Track which key was accessed.

	private AppConfig(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			String section = null;
			while ((line = br.readLine()) != null) {
				Matcher m = _section.matcher(line);
				if (m.matches()) {
					section = m.group(1).trim();
				} else if (section != null) {
					m = _keyValue.matcher(line);
					if (m.matches()) {
						String key = m.group(1).trim();
						String value = m.group(2).trim();
						Map<String, String> kv = _entries.get(section);
						if (kv == null) {
							_entries.put(section, kv = new HashMap<>());
						}
						kv.put(key, value);
					}
				}
			}
		}
	}

	/**
	 * Initialize the configuration manager as static (which will be called one time
	 * at application startup).
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public static void init(String filePath) throws IOException {
		if (instance == null) {
			instance = new AppConfig(filePath);
		}
	}

	/**
	 * Get the instance of this class.
	 * 
	 * @return AppConfig instance.
	 */
	public static AppConfig getInstance() {
		if (instance == null) {
			throw new IllegalStateException("AppConfig not initialized. Call init() first.");
		}
		return instance;
	}

	/**
	 * 
	 * @param section
	 * @param key
	 * @param defaultvalue
	 * @return the value in the provided key/section as a string.
	 */
	public String getString(String section, String key, String defaultvalue) {
		Map<String, String> kv = _entries.get(section);
		if (kv == null) {
			return defaultvalue;
		}
		return kv.get(key);
	}

	/**
	 * 
	 * @param section
	 * @param key
	 * @param defaultvalue
	 * @return the value in the provided key/section as an int.
	 */
	public int getInt(String section, String key, int defaultvalue) {
		Map<String, String> kv = _entries.get(section);
		if (kv == null) {
			return defaultvalue;
		}
		return Integer.parseInt(kv.get(key));
	}

	/**
	 * 
	 * @param section
	 * @param key
	 * @param defaultvalue
	 * @return the value in the provided key/section as a float.
	 */
	public float getFloat(String section, String key, float defaultvalue) {
		Map<String, String> kv = _entries.get(section);
		if (kv == null) {
			return defaultvalue;
		}
		return Float.parseFloat(kv.get(key));
	}

	/**
	 * 
	 * @param section
	 * @param key
	 * @param defaultvalue
	 * @return the value in the provided key/section as a double.
	 */
	public double getDouble(String section, String key, double defaultvalue) {
		Map<String, String> kv = _entries.get(section);
		if (kv == null) {
			return defaultvalue;
		}
		return Double.parseDouble(kv.get(key));
	}

	/**
	 * 
	 * @return the last key that was accessed.
	 */
	public String getLastKey() {
		return lastKey;
	}
}