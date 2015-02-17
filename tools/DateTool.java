package tools;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import db.Database;

public class DateTool {

	public static String dStr(Date d) {
		String ret = "";
		String pattern = "dd.MM.yyyy";
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(pattern);
		ret = formatter.format(d);
		return ret;
	}

	/**
	 * list of abrechnungswochen
	 * 
	 * @return list of abrechnungswochen
	 */
	public static Vector<ListItem> abrechnungsWochen() {
		Vector<ListItem> ret = new Vector<ListItem>();
		try {
			ResultSet rs = Database.select("*", "abrechnungswochen", "ORDER BY id");
			while (rs.next()) {
				java.sql.Date von = rs.getDate("von");
				java.sql.Date bis = rs.getDate("bis");
				String showStr = rs.getString("abr_jahr") + " [" + rs.getString("abr_woche") + "]: "
						+ DateTool.dStr(von) + " - " + DateTool.dStr(bis);
				ret.add(new ListItem(rs.getString("id"), showStr));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * look in abrechnungswochen table for actual week and return the id
	 * 
	 * @return the id id of actual abrechnungswoche
	 */
	public static int actualAbrechnungsWocheID() {
		int ret = -1;
		ResultSet rs = null;
		try {
			rs = Database.select("id", "abrechnungswochen", "WHERE von<=CURDATE() AND bis>=CURDATE()");
			while (rs.next()) {
				ret = rs.getInt("id");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		return ret;
	}

	/**
	 * get weekcount of actual abrechnungsmonat
	 * 
	 * @return weekcount of actual abrechnungsmonat
	 */
	public static int abrechnungsMonatWeekCount(int woche) {
		int ret = 0;
		ResultSet rs = null;
		try {
			int monat = 0;
			int year = 0;
			rs = Database.select("abr_monat, abr_jahr", "abrechnungswochen", "WHERE id=" + woche);
			while (rs.next()) {
				monat = rs.getInt("abr_monat");
				year = rs.getInt("abr_jahr");
			}
			Database.close(rs);
			rs = Database.select("id", "abrechnungswochen", "WHERE abr_monat=" + monat + " AND abr_jahr=" + year
					+ " ORDER BY abr_woche");
			while (rs.next()) {
				ret++;
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		return ret;
	}

	/**
	 * which week is the given week in the suitable month
	 * 
	 * @return weekcount of actual abrechnungsmonat
	 */
	public static int abrechnungsWeekInMonth(int woche) {
		int ret = 0;
		ResultSet rs = null;
		try {
			int monat = abrechnungsMonatAsInt(woche);
			int jahr = abrechnungsJahrAsInt(woche);
			rs = Database.select("*", "abrechnungswochen", "WHERE abr_monat=" + monat + " AND abr_jahr=" + jahr
					+ " ORDER BY abr_woche");
			while (rs.next()) {
				if (rs.getInt("id") != woche) {
					ret++;
				} else {
					return ++ret;
				}
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		return ret;
	}

	/**
	 * get dateinterval of actual abrechnungswoche
	 * 
	 * @return dateinterval of actual abrechnungswoche
	 */
	public static DateInterval actualAbrechnungsWoche() {
		DateInterval ret = new DateInterval();
		ResultSet rs = null;
		try {
			rs = Database.select("von, bis", "abrechnungswochen", "WHERE id=" + actualAbrechnungsWocheID());
			while (rs.next()) {
				ret.setVon(rs.getDate("von"));
				ret.setBis(rs.getDate("bis"));
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		return ret;
	}

	/**
	 * get date of given zahltag
	 * 
	 * @return get date of given zahltag
	 */
	public static Date actualAbrechnungsTag(int which) {
		Date ret = null;
		try {
			ResultSet rs = Database.select("zahltag", "abrechnungswochen", "WHERE id=" + which);
			while (rs.next()) {
				ret = rs.getDate("zahltag");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get dateinterval of given abrechnungswoche
	 * 
	 * @return dateinterval of given abrechnungswoche
	 */
	public static DateInterval abrechnungsWoche(int woche) {
		DateInterval ret = new DateInterval();
		try {
			ResultSet rs = Database.select("von, bis", "abrechnungswochen", "WHERE id=" + woche);
			while (rs.next()) {
				ret.setVon(rs.getDate("von"));
				ret.setBis(rs.getDate("bis"));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get dateinterval of given abrechnungswoche
	 * 
	 * @return dateinterval of given abrechnungswoche
	 */
	public static DateInterval abrechnungsWoche(int jahr, int monat, int woche) {
		DateInterval ret = new DateInterval();
		try {
			ResultSet rs = Database.select("von, bis", "abrechnungswochen", "WHERE abr_monat=" + monat
					+ " AND abr_jahr=" + jahr + " ORDER BY abr_woche");
			int i = 1;
			while (rs.next()) {
				if (i == woche) {
					ret.setVon(rs.getDate("von"));
					ret.setBis(rs.getDate("bis"));
				}
				i++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get dateinterval of actual abrechnungsjahr
	 * 
	 * @return dateinterval of actual abrechnungsjahr
	 */
	public static DateInterval actualAbrechnungsJahr() {
		DateInterval ret = abrechnungsJahr(actualAbrechnungsWocheID());
		return ret;
	}

	/**
	 * get dateinterval of given abrechnungsjahr
	 * 
	 * @return dateinterval of given abrechnungsjahr
	 */
	public static DateInterval abrechnungsJahr(int which) {
		DateInterval ret = new DateInterval();
		try {
			int jahr = 0;
			ResultSet rs = Database.select("abr_jahr", "abrechnungswochen", "WHERE id=" + which);
			while (rs.next()) {
				jahr = rs.getInt("abr_jahr");
			}
			ret.setVon(Date.valueOf(jahr + "-01-01"));
			ret.setBis(Date.valueOf(jahr + "-12-31"));
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get dateinterval of actual abrechnungsmonth
	 * 
	 * @return dateinterval of actual abrechnungsmonth
	 */
	public static DateInterval actualAbrechnungsMonat() {
		DateInterval ret = abrechnungsMonat(actualAbrechnungsWocheID());
		return ret;
	}

	/**
	 * get dateinterval of given abrechnungsmonth
	 * 
	 * @return dateinterval of given abrechnungsmonth
	 */
	public static DateInterval abrechnungsMonat(int which) {
		DateInterval ret = new DateInterval();
		try {
			int monat = 0;
			int jahr = 0;
			ResultSet rs = Database.select("abr_monat, abr_jahr", "abrechnungswochen", "WHERE id=" + which);
			while (rs.next()) {
				monat = rs.getInt("abr_monat");
				jahr = rs.getInt("abr_jahr");
			}
			Database.close(rs);
			rs = Database.select("von, bis", "abrechnungswochen", "WHERE abr_monat=" + monat + " AND abr_jahr=" + jahr
					+ " ORDER BY von");
			boolean first = true;
			while (rs.next()) {
				if (first) {
					ret.setVon(rs.getDate("von"));
				}
				first = false;
				ret.setBis(rs.getDate("bis"));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get dateinterval of given abrechnungsmonth
	 * 
	 * @return dateinterval of given abrechnungsmonth
	 */
	public static DateInterval abrechnungsMonat(int year, int month) {
		DateInterval ret = new DateInterval();
		try {
			ResultSet rs = Database.select("von, bis", "abrechnungswochen", "WHERE abr_monat=" + month
					+ " AND abr_jahr=" + year + " ORDER BY von");
			boolean first = true;
			while (rs.next()) {
				if (first) {
					ret.setVon(rs.getDate("von"));
				}
				first = false;
				ret.setBis(rs.getDate("bis"));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	// ==========================================================
	// =============== RETURN INT ===============================
	// ==========================================================

	/**
	 * get int of actual abrechnungsjahr
	 * 
	 * @return int of actual abrechnungsjahr
	 */
	public static int actualAbrechnungsJahrAsInt() {
		int ret = abrechnungsJahrAsInt(actualAbrechnungsWocheID());
		return ret;
	}

	/**
	 * get int of actual abrechnungsjahr
	 * 
	 * @return int of actual abrechnungsjahr
	 */
	public static int abrechnungsJahrAsInt(int woche) {
		int ret = -1;
		try {
			ResultSet rs = Database.select("abr_jahr", "abrechnungswochen", "WHERE id=" + woche);
			while (rs.next()) {
				ret = rs.getInt("abr_jahr");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get int of actual abrechnungsmonat
	 * 
	 * @return int of actual abrechnungsmonat
	 */
	public static int actualAbrechnungsMonatAsInt() {
		int ret = abrechnungsMonatAsInt(actualAbrechnungsWocheID());
		return ret;
	}

	/**
	 * get int of month suitable for gioven week
	 * 
	 * @return int month for given week
	 */
	public static int abrechnungsMonatAsInt(int woche) {
		int ret = -1;
		try {
			ResultSet rs = Database.select("abr_monat", "abrechnungswochen", "WHERE id=" + woche);
			while (rs.next()) {
				ret = rs.getInt("abr_monat");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get int of actual abrechnungswoche
	 * 
	 * @return int of actual abrechnungswoche
	 */
	public static int actualAbrechnungsWocheAsInt() {
		int ret = abrechnungsWocheAsInt(actualAbrechnungsWocheID());
		return ret;
	}

	/**
	 * get int of given abrechnungswoche
	 * 
	 * @return int of given abrechnungswoche
	 */
	public static int abrechnungsWocheAsInt(int woche) {
		int ret = -1;
		try {
			ResultSet rs = Database.select("abr_woche", "abrechnungswochen", "WHERE id=" + woche);
			while (rs.next()) {
				ret = rs.getInt("abr_woche");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * make vector with times that a vertraggespräche could happen
	 * 
	 * @return the time vector...
	 */
	public static Vector<String> vertragsgespraechZeiten() {
		Vector<String> ret = new Vector<String>();
		ret.add("");
		ret.add("09:30");
		ret.add("11:30");
		ret.add("14:00");
		ret.add("16:00");
		ret.add("18:00");
		return ret;
	}
}
