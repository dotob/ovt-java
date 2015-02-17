package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.ListOrderedMap;

import tools.MyLog;

public class AktionsErgebnis {

	private String id;
	private String gruppe;
	private String name;
	private String kurzname;
	private String langtext;
	private boolean setAdressDirty;
	private boolean noMoney;
	private boolean statistic;
	private boolean mailingEnabled;
	private int finishedAfter;

	private static ListOrderedMap ergebnisMap;
	private static int[][] ergWithMultiFinishCount;

	/**
	 * this is the ctor to get an existing action from db
	 * 
	 * @param rs
	 *            the resultset with aktion in it
	 */
	public AktionsErgebnis(ResultSet rs) {
		try {
			this.id = rs.getString("id");
			this.gruppe = rs.getString("gruppe");
			this.name = rs.getString("name");
			this.kurzname = rs.getString("kurzname");
			this.setAdressDirty = rs.getBoolean("setadressdirty");
			this.noMoney = rs.getBoolean("nomoney");
			this.statistic = rs.getBoolean("statistic");
			this.finishedAfter = rs.getInt("finishedafter");
			this.mailingEnabled = rs.getBoolean("mailingEnabled");
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	/**
	 * this is the ctor to get an existing action from db
	 * 
	 * @param rs
	 *            the resultset with aktion in it
	 */
	public AktionsErgebnis(int id) {
		this.copy((AktionsErgebnis) getAktionErgebnisseAsMap().get(Integer.toString(id)));
	}

	/**
	 * this is the ctor to get an existing action from db
	 * 
	 * @param rs
	 *            the resultset with aktion in it
	 */
	public AktionsErgebnis(String id) {
		this.copy((AktionsErgebnis) getAktionErgebnisseAsMap().get(id));
	}

	public void copy(AktionsErgebnis ae) {
		if (ae != null) {
			this.id = ae.id;
			this.gruppe = ae.gruppe;
			this.name = ae.name;
			this.kurzname = ae.kurzname;
			this.setAdressDirty = ae.setAdressDirty;
			this.noMoney = ae.noMoney;
			this.statistic = ae.statistic;
			this.finishedAfter = ae.finishedAfter;
			this.mailingEnabled = ae.mailingEnabled;
		}
	}

	public String toString() {
		String ret = "";
		// check if there is shortname
		String kurz = this.kurzname;
		if (kurz.length() > 0) {
			ret += "<html><b>" + kurz + "</b> : " + this.name + "</html>";
		} else {
			ret += "<html><b>" + this.name + "</b></html>";
		}
		return ret;
	}

	/**
	 * no swing markup, for excel insert...
	 * 
	 * @return string for this aktion
	 */
	public String toNoHTMLString() {
		String ret = this.kurzname + ", vom " + this.name;
		return ret;
	}

	/**
	 * make map of aktionsergebnisse
	 * 
	 * @return map of aktionsergebnisse
	 */
	public static ListOrderedMap getAktionErgebnisseAsMap() {
		if (ergebnisMap == null) {
			ergebnisMap = new ListOrderedMap();
			try {
				ResultSet rs = Database.select("*", "ergebnisse", "ORDER BY gruppe, name");
				while (rs.next()) {
					AktionsErgebnis ae = new AktionsErgebnis(rs);
					ergebnisMap.put(ae.id, ae);
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return ergebnisMap;
	}

	/**
	 * search for aktionsergebnisse that need a finishcount higher than 1.
	 * means: there need to be more than one of this aktions to finish work on
	 * an address
	 * 
	 * @return integer array with ids of aktionsergebniss with higher
	 *         finishcount
	 */
	public static int[][] getErgebnisseWithHigherFinishingCount() {
		if (ergWithMultiFinishCount == null) {
			Vector<Integer> tmp = new Vector<Integer>();
			for (MapIterator iter = ergebnisMap.mapIterator(); iter.hasNext();) {
				iter.next();
				AktionsErgebnis ae = (AktionsErgebnis) iter.getValue();
				if (ae.finishedAfter > 1) {
					tmp.add(new Integer(ae.getIdAsInt()));
				}
			}
			ergWithMultiFinishCount = new int[2][tmp.size()];
			int i = 0;
			for (Iterator<Integer> iter = tmp.iterator(); iter.hasNext();) {
				Integer a = (Integer) iter.next();
				ergWithMultiFinishCount[0][i] = a.intValue();
				ergWithMultiFinishCount[1][i] = new AktionsErgebnis(Integer.toString(a)).finishedAfter;
				i++;
			}
		}
		return ergWithMultiFinishCount;
	}

	/**
	 * get string with multifinsher ergebnis ids
	 * 
	 * @return
	 */
	public static String multifinisherErgebnisse() {
		String ret = "";
		for (MapIterator iter = ergebnisMap.mapIterator(); iter.hasNext();) {
			iter.next();
			AktionsErgebnis ae = (AktionsErgebnis) iter.getValue();
			if (ae.finishedAfter > 1) {
				ret += ae.getId() + ",";
			}
		}
		return ret.substring(0, ret.length() - 1);
	}

	public String getGruppe() {
		return gruppe;
	}

	public String getKurzname() {
		return kurzname;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public int getIdAsInt() {
		return Integer.parseInt(id);
	}

	public int isFinishedAfter() {
		return finishedAfter;
	}

	public String getLangtext() {
		return langtext;
	}

	public boolean isNoMoney() {
		return noMoney;
	}

	public boolean isSetAdressDirty() {
		return setAdressDirty;
	}

	public boolean isStatistic() {
		return statistic;
	}

	public boolean isMailingEnabled() {
		return mailingEnabled;
	}

	public void setMailingEnabled(boolean mailingEnabled) {
		this.mailingEnabled = mailingEnabled;
	}
}
