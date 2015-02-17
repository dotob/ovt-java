package db;

/**
 * Database.java
 * 
 * class for connecting to the database configured in settings
 *
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import tools.MyLog;
import tools.SettingsReader;

public class Database {
	private static String driver = SettingsReader.getString("DBSettings.jdbc_driver");
	private static String url = SettingsReader.getString("DBSettings.jdbc_url");
	private static String username = SettingsReader.getString("DBSettings.jdbc_username");
	private static String password = SettingsReader.getString("DBSettings.jdbc_password");

	private static String[] specialDelTables = new String[] { "aktionen", "kunden", "gespraeche" };

	private static DataSource pool = null;

	private static String error;

	public Database() {
	}

	private static void init() {
		if (pool == null) {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(driver);
			ds.setUsername(username);
			ds.setPassword(password);
			ds.setDefaultAutoCommit(true);
			ds.setUrl(url);
			ds.setMaxActive(50);
			// ds.setInitialSize(50);
			// System.out.println(dbURL());
			// TODO why is maxwait not working?
			ds.setMaxWait(5); // wait before throwing an exception when no
			// connection comes...
			pool = ds;
		}
	}

	/**
	 * @return has connection been established
	 */
	public static boolean test() {
		init();
		boolean ret = false;
		Connection con = null;
		try {
			con = pool.getConnection();
			ret = true;
		} catch (SQLException e) {
			e.printStackTrace();
			MyLog.logError("databasetest", e);
		} finally {
			try {
				con.close();
			} catch (Exception e) {
			}
		}
		return ret;
	}

	/**
	 * @return has connection been established
	 */
	public static boolean ping() {
		init();
		boolean ret = false;
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				String query = "SELECT DATABASE()";
				System.out.println(query);
				s.executeQuery(query);
			}
			ret = true;
		} catch (SQLException e) {
			// notting..
		} finally {
			try {
				con.close();
			} catch (Exception e) {
			}
		}
		return ret;
	}

	public static String dbURL() {
		return "(" + driver + ") " + url + ":" + username + ":" + password;
	}

	public static String dbURLNoPass() {
		return "(" + driver + ") " + url + ":" + username + ":***";
	}

	/**
	 * @return has connection been established
	 */
	public static boolean connect(String urlIn, String user, String pass) {
		if (pool == null) {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(driver);
			ds.setUsername(user);
			ds.setPassword(pass);
			ds.setUrl(urlIn);
		}
		return true;
	}

	/**
	 * @return has connection been shutdown
	 */
	public static boolean close(ResultSet rs) {
		if (rs != null) {
			try {
				Statement st = rs.getStatement();
				Connection con = st.getConnection();
				// close anything
				rs.close();
				st.close();
				con.close();
				return true;
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * @return has connection been shutdown
	 */
	public static boolean close(PreparedStatement rs) {
		try {
			Connection con = rs.getConnection();
			// close anything
			rs.close();
			con.close();
			return true;
		} catch (Exception e) {
			MyLog.showExceptionErrorDialog(e);
			return false;
		}
	}

	/**
	 * @param rs
	 *            ResultSet to print fieldnames to
	 */
	public static void printRSInfo(ResultSet rs) {
		try {
			ResultSetMetaData erg = rs.getMetaData();
			for (int m = 1; m <= erg.getColumnCount(); m++) {
				System.out.println(erg.getTableName(m) + "." + erg.getColumnName(m));
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	/**
	 * @param key
	 *            which fields to return
	 * @param from
	 *            which table
	 * @return the resultset of the query
	 */
	public static ResultSet select(String key, String from) {
		return select(key, from, "");
	}

	/**
	 * same like slect, but print the sql statement...
	 * 
	 * @param key
	 *            which fields to return
	 * @param from
	 *            which table
	 * @param condition
	 *            where clause of query
	 * @return the resultset of the query
	 */
	public static ResultSet selectDebug(String key, String from, String condition) {
		System.out.println("SELECT " + key + " FROM " + from + " " + condition);
		ResultSet ret = select(key, from, condition);
		// System.out.println(" ready");
		return ret;
	}

	/**
	 * @param key
	 *            which fields to return
	 * @param from
	 *            which table
	 * @param condition
	 *            where clause of query
	 * @return the resultset of the query
	 */
	public static ResultSet select(String key, String from, String condition) {
		return selectWithLimits(key, from, condition, 0, 0);
	}

	/**
	 * @param key
	 *            which fields to return
	 * @param from
	 *            which table
	 * @param condition
	 *            where clause of query
	 * @return the resultset of the query
	 */
	public static ResultSet selectWithLimits(String key, String from, String condition, int howMany, int startFrom) {
		ping();
		ResultSet ret = null;
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				String query = "SELECT " + key + " FROM " + from + " " + condition;
				if (howMany > 0 && startFrom > 0) {
					query += " LIMIT " + startFrom + "," + howMany;
				} else if (howMany > 0) {
					query += " LIMIT " + howMany;
				}
				MyLog.logDebug(query);
				ResultSet r = s.executeQuery(query);
				ret = r;
			} else {
				System.out.println("Database.select(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.select(2): " + e);
		}
		return ret;
	}

	public static void update(String table, String values, String condition) {
		ping();
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				String query = "UPDATE " + table + " SET " + values + " " + condition;
				MyLog.logDebug("DB:" + query);
				System.out.println(query);
				s.executeUpdate(query);
				s.close();
			} else {
				System.out.println("Database.update(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.update(2): " + e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				System.out.println("Database.update(3): " + e);
			}
		}
	}

	public static int countQueryResult(String afterfrom) {
		int count = 0;
		ping();
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				String query = "SELECT count(*) FROM " + afterfrom;
				MyLog.logDebug("DB:" + query);
				System.out.println(query);
				ResultSet rs = s.executeQuery(query);
				while (rs.next()) {
					count = rs.getInt(1);
				}
				s.close();
			} else {
				System.out.println("Database.update(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.update(2): " + e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				System.out.println("Database.update(3): " + e);
			}
		}
		return count;
	}

	public static String updateThrowException(String table, String values, String condition) throws SQLException {
		ping();
		Connection con = null;
		String query = "UPDATE " + table + " SET " + values + " " + condition;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				MyLog.logDebug("DB:" + query);
				System.out.println(query);
				s.executeUpdate(query);
				s.close();
			} else {
				System.out.println("Database.update(1): conn is null");
			}
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				System.out.println("Database.update(3): " + e);
			}
		}
		return query;
	}

	public static int quickInsert(String table, String values) {
		int insertedID = -1;
		ping();
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				String query = "INSERT INTO " + table + " VALUES (" + values + ")";
				MyLog.logDebug("DB:" + query);
				System.out.println(query);
				s.executeUpdate(query);
				ResultSet rs = s.getGeneratedKeys();
				while (rs.next()) {
					insertedID = rs.getInt(1);
				}
				s.close();
			} else {
				System.out.println("Database.quickInsert(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.quickInsert(2): " + e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				System.out.println("Database.quickInsert(3): " + e);
			}
		}
		return insertedID;
	}

	public static void executeQuery(String query) {
		ping();
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				MyLog.logDebug("DB:" + query);
				// System.out.println(query);
				s.execute(query);
			} else {
				System.out.println("Database.quickInsert(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.quickInsert(2): " + e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				System.out.println("Database.quickInsert(3): " + e);
			}
		}
	}

	/**
	 * @param key
	 *            which fields to return
	 * @param from
	 *            which table
	 * @param condition
	 *            where clause of query
	 * @return the resultset of the query
	 */
	public static String delete(String table, String condition) {
		String ret = null;
		ping();
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				Statement s = con.createStatement();
				// if table is one of special tables, save deleted entries
				boolean specialTable = false;
				for (int i = 0; i < specialDelTables.length; i++) {
					specialTable = specialDelTables[i].equals(table);
					if (specialTable) {
						break;
					}
				}
				if (specialTable) {
					String saveQuery = "INSERT INTO " + table + "_del (SELECT * FROM " + table + " " + condition + ")";
					MyLog.logDebug("DB:" + saveQuery);
					System.out.println(saveQuery);
					ret = saveQuery;
					s.executeUpdate(saveQuery);
				} else {
					ret = "";
				}

				String query = "DELETE FROM " + table + " " + condition;
				ret += "\n";
				ret += query;
				MyLog.logDebug("DB:" + query);
				// System.out.println(query);
				s.executeUpdate(query);
				s.close();
			} else {
				System.out.println("Database.delete(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.delete(2): " + e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				System.out.println("Database.delete(3): " + e);
			}
		}
		return ret;
	}

	/**
	 * @param r
	 *            the resultset to work on
	 * @param colname
	 *            the field which fill the vector
	 * @return a vector filled with the given fields values
	 */
	public static Vector<String> getVecString(ResultSet r, String colname) {
		Vector<String> v = new Vector<String>();
		try {
			while (r.next()) {
				v.add(r.getString(colname));
			}
			r.beforeFirst();
			return v;
		} catch (Exception e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.getVecSelect: " + e);
			return null;
		}
	}

	public static PreparedStatement getPreparedStatement(String stmt) {
		ping();
		PreparedStatement ret = null;
		Connection con = null;
		try {
			con = pool.getConnection();
			if (con != null) {
				MyLog.logDebug("DB:" + stmt);
				ret = con.prepareStatement(stmt);
			} else {
				System.out.println("Database.getPreparedStatement(1): conn is null");
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			System.out.println("Database.getPreparedStatement(2): " + e);
		}
		return ret;
	}

	public static String getError() {
		return error;
	}
}
