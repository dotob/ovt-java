package db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import tools.ContactBundle;
import tools.DateInterval;
import tools.MyLog;
import tools.SettingsReader;

public class Marktforscher {

	public static final int NAMESTYLE_NORMAL = 0;
	public static final int NAMESTYLE_NONE = 1;
	public static final int NAMESTYLE_NOID = 2;
	public static final int NAMESTYLE_WITHGROUP = 3;

	private String cId;
	private String gruppe;
	private String nachName;
	private String vorName;
	private String kurzName;
	private String strasse;
	private String hausnummer;
	private String plz;
	private String stadt;
	private String telefon;
	private String telefax;
	private String handy;
	private String email;
	private String anrede;
	private String kontonummer;
	private String blz;
	private Date geburtstag;
	private Date eintrittsdatum;
	private double honorarTermin;
	private double honorarAdresse;
	private double honorarPauschale;
	private boolean solar;
	private boolean aktiv;
	private int nameStyle = NAMESTYLE_NORMAL; // style
	// how
	private static Double honorarPauschaleFromSetting = null;
	private static Double honorarTerminFromSetting = null;
	private static Double honorarAdresseFromSetting = null;

	public Marktforscher() {

	}

	public Marktforscher(String id) {
		ResultSet rs = Database.select("*", "marktforscher", "WHERE id=" + id);
		try {
			while (rs.next()) {
				fillMFFromResultset(rs);
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
	}

	/*
	 * doesnt close the resultset!!!!
	 */
	public Marktforscher(ResultSet rs) {
		fillMFFromResultset(rs);
	}

	private void fillMFFromResultset(ResultSet rs) {
		try {
			this.cId = DBTools.myGetString(rs, "id");
			this.anrede = DBTools.myGetString(rs, "anrede");
			this.gruppe = DBTools.myGetString(rs, "gruppe");
			this.vorName = DBTools.myGetString(rs, "vorname");
			this.nachName = DBTools.myGetString(rs, "nachname");
			this.kurzName = DBTools.myGetString(rs, "kurzname");
			this.eintrittsdatum = rs.getDate("eintrittsdatum");
			this.strasse = DBTools.myGetString(rs, "strasse");
			this.hausnummer = DBTools.myGetString(rs, "hausnummer");
			this.plz = DBTools.myGetString(rs, "plz");
			this.stadt = DBTools.myGetString(rs, "stadt");
			this.kontonummer = DBTools.myGetString(rs, "kontonummer");
			this.telefon = DBTools.myGetString(rs, "telefon");
			this.handy = DBTools.myGetString(rs, "handy");
			this.telefax = DBTools.myGetString(rs, "telefax");
			this.email = DBTools.myGetString(rs, "email");
			this.blz = DBTools.myGetString(rs, "blz");
			this.setAktiv(rs.getBoolean("aktiv"));
			this.solar = rs.getBoolean("solar");
			this.geburtstag = rs.getDate("geburtsdatum");
			this.honorarTermin = rs.getDouble("honorar_termin");
			if (this.honorarTermin == 0) {
				this.honorarTermin = getHonorarTerminFromSetting();
			}
			this.honorarAdresse = rs.getDouble("honorar_adresse");
			if (this.honorarAdresse == 0) {
				this.honorarAdresse = getHonorarAdresseFromSettings();
			}
			this.honorarPauschale = rs.getDouble("honorar_pauschale");
			if (this.honorarPauschale == 0) {
				this.honorarPauschale = getHonorarPauschaleFromSettings();
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	public boolean SaveToDB() {
		boolean ret = false;
		PreparedStatement p1 = null;
		// add to db
		try {
			boolean isNew = this.getId() == null;
			if (isNew) {
				p1 = Database.getPreparedStatement("INSERT INTO marktforscher " + "(aktiv, solar, eintrittsdatum, gruppe, anrede, "
						+ "nachname, vorname, kurzname, strasse, hausnummer, " + "plz, stadt, telefon, handy, telefax, "
						+ "email, geburtsdatum, kontonummer, blz, honorar_termin, " + "honorar_adresse, honorar_pauschale) "
						+ "VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)");
			} else {
				p1 = Database.getPreparedStatement("UPDATE marktforscher SET " + "aktiv=?, solar=?, eintrittsdatum=?, gruppe=?, anrede=?, "
						+ "nachname=?, vorname=?, kurzname=?, strasse=?, hausnummer=?, "
						+ "plz=?, stadt=?, telefon=?, handy=?, telefax=?, "
						+ "email=?, geburtsdatum=?, kontonummer=?, blz=?, honorar_termin=?, "
						+ "honorar_adresse=?, honorar_pauschale=? WHERE id=" + cId);

			}
			int i = 1;
			p1.setBoolean(i++, this.isAktiv());
			p1.setBoolean(i++, this.solar);
			p1.setDate(i++, this.eintrittsdatum);
			p1.setString(i++, this.gruppe);
			p1.setString(i++, this.anrede);
			p1.setString(i++, this.nachName);
			p1.setString(i++, this.vorName);
			p1.setString(i++, this.kurzName);
			p1.setString(i++, this.strasse);
			p1.setString(i++, this.hausnummer);
			p1.setString(i++, this.plz);
			p1.setString(i++, this.stadt);
			p1.setString(i++, this.telefon);
			p1.setString(i++, this.handy);
			p1.setString(i++, this.telefax);
			p1.setString(i++, this.email);
			p1.setDate(i++, this.geburtstag);
			p1.setString(i++, this.kontonummer);
			p1.setString(i++, this.blz);
			p1.setDouble(i++, this.honorarTermin);
			p1.setDouble(i++, this.honorarAdresse);
			p1.setDouble(i++, this.honorarPauschale);
			p1.executeUpdate();
			if (isNew) {
				ResultSet rs = p1.getGeneratedKeys();
				while (rs.next()) {
					this.cId = rs.getString(1);
				}
			}
			ret = true;
		} catch (Exception ex) {
			MyLog.showExceptionErrorDialog(ex);
		} finally {
			if (p1 != null)
				Database.close(p1);
		}
		return ret;
	}

	private double getHonorarTerminFromSetting() {
		if (honorarTerminFromSetting == null) {
			honorarTerminFromSetting = getDefaultHonorarTermin();
			try {
				honorarTerminFromSetting = Double.parseDouble(SettingsReader.getString("OVTAdmin.Terminpauschale"));
			} catch (NumberFormatException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return honorarTerminFromSetting;
	}

	private double getHonorarPauschaleFromSettings() {
		if (honorarPauschaleFromSetting == null) {
			honorarPauschaleFromSetting = getDefaultHonorarPauschale();
			try {
				honorarPauschaleFromSetting = Double.parseDouble(SettingsReader.getString("OVTAdmin.Kostenpauschale"));
			} catch (NumberFormatException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return honorarPauschaleFromSetting;
	}

	private double getHonorarAdresseFromSettings() {
		if (honorarAdresseFromSetting == null) {
			honorarAdresseFromSetting = getDefaultHonorarAdresse();
			try {
				honorarAdresseFromSetting = Double.parseDouble(SettingsReader.getString("OVTAdmin.AktionGrundhonorar"));
			} catch (NumberFormatException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return honorarAdresseFromSetting;
	}

	public static Marktforscher SearchMarktforscher(String id) {
		Marktforscher mf = null;
		if (id != null && id.length() > 0) {
			ResultSet rs = getResultSet(id);
			if (rs != null) {
				mf = new Marktforscher(rs);
			}
			Database.close(rs);
		}
		return mf;
	}

	/**
	 * get the resultset that points to db-data
	 * 
	 * @param id
	 *            is of mafo to retrieve
	 * @return the resultset that points to db-data
	 */
	private static ResultSet getResultSet(String id) {
		ResultSet ret = null;
		if (id != null && id.length() > 0) {
			try {
				ret = Database.select("*", "marktforscher", "WHERE     id=" + id);
				// if the resultset has no data return null, else set it back to
				// first record
				if (!ret.next()) {
					// important: need to close rs here
					Database.close(ret);
					ret = null;
				} else {
					ret.first();
				}
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return ret;
	}

	// ==========================================================
	// ================== methods ===============================
	// ==========================================================

	public String toString() {
		String ret = "";
		if (this.nameStyle == NAMESTYLE_NORMAL) {
			ret = this.nachName + ", " + this.vorName + " [" + this.cId + "]";
		} else if (this.nameStyle == NAMESTYLE_NOID) {
			ret = this.nachName + ", " + this.vorName;
		} else if (this.nameStyle == NAMESTYLE_WITHGROUP) {
			ret = this.nachName + ", " + this.vorName + " [" + this.cId + "] (" + this.gruppe + ")";
		}
		return ret;
	}

	public boolean equals(Marktforscher mf) {
		return this.cId.equals(mf.cId);
	}

	public String rawName() {
		String ret = this.nachName + "_" + this.vorName;
		return ret;
	}

	public String realName() {
		String ret = this.vorName + " " + this.nachName;
		return ret;
	}

	public void resetWaitingContacts() {
		// do not reset contacts that have actions...
		ContactBundle waitingContacts = this.getWaitingContacts();
		if (waitingContacts.getContactCount() > 0) {
			String reset = waitingContacts.idsString();
			try {
				// decrease counter from bereitgestellt
				MyLog.logDebug("reset contacts:" + reset);
				ResultSet rs = Database.select("marktforscher, bereitgestellt, count(*)", "kunden", "WHERE id IN (" + reset
						+ ") GROUP BY bereitgestellt HAVING count(*)>0");
				while (rs.next()) {
					String bDate = "'" + rs.getDate("bereitgestellt").toString() + "'";
					String mf = rs.getString("marktforscher");
					int count = rs.getInt(3);
					Date nowDate = new Date(new java.util.Date().getTime());
					Database.quickInsert("bereitgestellt", "NULL, " + bDate + ", " + mf + ", " + (-1 * count) + ", 'reset: " + nowDate
							+ "', ''");
					MyLog.logDebug("change bereitgestellt: " + mf + "|" + bDate + "|" + count);
				}
				Database.close(rs);
				// set contacts back
				DBTools.setContactsFree(reset);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
			}
		}
	}

	public void resetUntouchedContacts() {
		// do not reset contacts that have actions...
		String reset = this.getWaitingContacts().idsString();
		try {
			// decrease counter from bereitgestellt
			MyLog.logDebug("reset contacts:" + reset);
			ResultSet rs = Database.select("k.id", "kunden k LEFT OUTER JOIN aktionen a ON k.id=a.kunde", "WHERE k.id IN (" + reset
					+ ") AND a.id IS NULL");
			int count = 0;
			String reallyReset = "";
			while (rs.next()) {
				count++;
				reallyReset += rs.getString("k.id") + ",";
			}
			if (reallyReset.length() > 0) {
				reallyReset = reallyReset.substring(0, reallyReset.length() - 1);
			}
			Date nowDate = new Date(new java.util.Date().getTime());
			Database.quickInsert("bereitgestellt", "NULL, NOW(), " + this.getId() + ", " + (-1 * count) + ", 'reset: " + nowDate + "', '<"
					+ reallyReset + ">'");
			MyLog.logDebug("change bereitgestellt: " + this.getNachName() + "|" + count);
			Database.close(rs);
			// set contacts back
			DBTools.setContactsFree(reallyReset);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
		}
	}

	/**
	 * get contacts that are waiting for mafo to be used
	 * 
	 * @param range
	 *            dateintervall where to search
	 * @return count of provided contacts
	 */
	public int getProvidedContacts(DateInterval range) {
		int ret = 0;
		Date von = range.getVon();
		Date bis = range.getBis();

		try {
			String tables = "bereitgestellt";
			String wc = "WHERE marktforscher=" + this.getId();
			wc += " AND wann>='" + von + "' AND wann<='" + bis + "'";
			ResultSet rs = Database.select("sum(count)", tables, wc);
			while (rs.next()) {
				ret = rs.getInt(1);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get contacts that are waiting for mafo to be used
	 * 
	 * @return contactbundle
	 */
	public ContactBundle getWaitingContacts() {
		ContactBundle ret = new ContactBundle();
		ResultSet rs = null;
		try {
			String tables = "kunden";
			String wc = "WHERE kunden.marktforscher=" + this.getId() + " AND kunden.bearbeitungsstatus=" + Contact.STATE_WAITING;
			rs = Database.select("id", tables, wc);
			while (rs.next()) {
				ret.addContact(new Contact(rs, false));
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
	 * get contacts that are waiting for mafo to be used
	 * 
	 * @return count of waiting contacts
	 */
	public int getWaitingContactsCount() {
		int ret = 0;

		try {
			String tables = "kunden";
			String wc = "WHERE kunden.marktforscher=" + this.getId() + " AND kunden.bearbeitungsstatus=" + Contact.STATE_WAITING;
			ResultSet rs = Database.select("count(*)", tables, wc);
			while (rs.next()) {
				ret = rs.getInt(1);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get contacts that are at mafoclient in use
	 * 
	 * @param range
	 *            dateintervall where to search
	 * @return contactbundle
	 */
	public ContactBundle getWorkingContacts(DateInterval range) {
		ContactBundle ret = new ContactBundle();
		Date von = range.getVon();
		Date bis = range.getBis();
		// MyLog.o(von+" <-> "+bis);
		ResultSet rs = null;
		try {
			String tables = "kunden";
			String wc = "WHERE kunden.marktforscher=" + this.getId() + " AND kunden.bearbeitungsstatus=" + Contact.STATE_WORKING;
			wc += " AND bereitgestellt>='" + von + "' AND bereitgestellt<='" + bis + "' ";
			rs = Database.select("id", tables, wc);
			while (rs.next()) {
				ret.addContact(new Contact(rs, false));
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
	 * get contacts that are ready an have finishing but not yet billed aktions.
	 * 
	 * @param range
	 *            dateintervall where to search
	 * @return contactbundle
	 */
	public ContactBundle getFinishedContacts(DateInterval range) {
		ContactBundle ret = new ContactBundle();
		Date von = range.getVon();
		Date bis = range.getBis();
		ResultSet rs = null;
		try {
			String tables = "kunden, aktionen, ergebnisse";
			String wc = "WHERE aktionen.marktforscher=" + this.getId();
			wc += " AND kunden.id=aktionen.kunde";
			// the nomoney aktions are here too !!!!!!!!!!!
			wc += " AND aktionen.ergebnis=ergebnisse.id AND ergebnisse.finishedafter=1 ";

			// check intervall
			if (von != null) {
				wc += " AND aktionen.angelegt>='" + von + "'";
			}
			if (bis != null) {
				wc += " AND aktionen.angelegt<='" + bis + "'";
			}
			wc += " GROUP BY aktionen.kunde";
			rs = Database.select("kunden.id", tables, wc);
			while (rs.next()) {
				Contact c = new Contact(rs, false);
				ret.addContact(c);
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
	 * get contacts that are ready an have finishing but not yet billed aktions.
	 * 
	 * @param range
	 *            dateintervall where to search
	 * @return contactbundle
	 */
	public int getFinishedContactsCount(DateInterval range) {
		int ret = 0;
		Date von = range.getVon();
		Date bis = range.getBis();

		try {
			String tables = "kunden, aktionen, ergebnisse";
			String wc = "WHERE aktionen.marktforscher=" + this.getId();
			wc += " AND kunden.id=aktionen.kunde";
			// the nomoney aktions are here too !!!!!!!!!!!
			wc += " AND aktionen.ergebnis=ergebnisse.id AND ergebnisse.finishedafter=1 ";

			// check intervall
			if (von != null) {
				wc += " AND aktionen.angelegt>='" + von + "'";
			}
			if (bis != null) {
				wc += " AND aktionen.angelegt<='" + bis + "'";
			}

			ResultSet rs = Database.select("DISTINCT kunden.id", tables, wc);
			while (rs.next()) {
				ret++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get contacts that are ready an have new aktions.
	 * 
	 * @param range
	 *            dateintervall where to search
	 * @return contactbundle
	 */
	public ContactBundle getNoMoneyContacts(DateInterval range) {
		ContactBundle ret = new ContactBundle();
		Date von = range.getVon();
		Date bis = range.getBis();
		// MyLog.o(von+" <-> "+bis);
		ResultSet rs = null;
		try {
			String tables = "kunden, aktionen, ergebnisse";
			String wc = "WHERE aktionen.marktforscher=" + this.getId() + " AND kunden.id=aktionen.kunde";
			// check money
			wc += " AND aktionen.ergebnis=ergebnisse.id AND ergebnisse.nomoney=1";

			// check intervall
			if (von != null) {
				wc += " AND aktionen.angelegt>='" + von + "'";
			}
			if (bis != null) {
				wc += " AND aktionen.angelegt<='" + bis + "'";
			}

			int i = 0;
			rs = Database.select("kunden.id", tables, wc);
			while (rs.next()) {
				// a contact could appear more than once because of
				// finishedafter>1
				Contact c = new Contact(rs, false);
				ret.addContact(c);
				i++;
			}
			// System.out.println("nomoney:"+i);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		return ret;
	}

	/**
	 * get contacts that are ready an have new aktions.
	 * 
	 * @param range
	 *            dateintervall where to search
	 * @return contactbundle
	 */
	public int getNoMoneyContactsCount(DateInterval range) {
		int ret = 0;
		try {
			String tables = "aktionen, ergebnisse";
			String wc = "WHERE aktionen.marktforscher=" + this.getId();
			// check money
			wc += " AND aktionen.ergebnis=ergebnisse.id AND ergebnisse.nomoney=1";

			// check intervall
			if (range.getVon() != null) {
				wc += " AND aktionen.angelegt>='" + range.getVon() + "'";
			}
			if (range.getBis() != null) {
				wc += " AND aktionen.angelegt<='" + range.getBis() + "'";
			}

			ResultSet rs = Database.select("count(*)", tables, wc);
			while (rs.next()) {
				ret = rs.getInt(1);
			}
			Database.close(rs);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get honorar
	 * 
	 * @return sum all honorare
	 */
	public int getHonorar(DateInterval range) {
		int ret = 0;
		try {
			Date von = range.getVon();
			Date bis = range.getBis();
			String wc = "WHERE ( gespraeche.marktforscher=" + this.cId;
			wc += " AND gespraeche.ergebnis=terminergebnisse.id ) ";
			if (von != null) {
				wc += "AND gespraeche.angelegt>='" + von + "'";
			}
			if (bis != null) {
				wc += " AND gespraeche.angelegt<='" + bis + "'";
			}
			ResultSet rs = Database.select("terminergebnisse.erfolg", "gespraeche, terminergebnisse", wc);
			while (rs.next()) {
				int erg = rs.getInt("erfolg");
				// no gespräch no money, erg==0 menas no gespräch
				if (erg > 0) {
					ret += this.getHonorarTermin();
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get aufträge
	 * 
	 * @return has this contact one or more auftraege
	 */
	public Vector<Gespraech> getAuftraege() {
		Vector<Gespraech> ret = new Vector<Gespraech>();
		try {
			ResultSet rs = Database.select("*", "gespraeche, terminergebnisse",
					"WHERE gespraeche.ergebnis=terminergebnisse.id AND gespraeche.marktforscher=" + this.cId
							+ " AND terminergebnisse.erfolg=1");
			while (rs.next()) {
				ret.add(new Gespraech(rs));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get aufträge
	 * 
	 * @param range
	 *            date range for termin searching
	 * @return has this contact one or more auftraege
	 */
	public Vector<Gespraech> getAuftraege(DateInterval range) {
		Date von = range.getVon();
		Date bis = range.getBis();
		Vector<Gespraech> ret = new Vector<Gespraech>();
		try {
			ResultSet rs = Database.select("*", "gespraeche, terminergebnisse",
					"WHERE gespraeche.ergebnis=terminergebnisse.id AND gespraeche.marktforscher=" + this.cId
							+ " AND terminergebnisse.erfolg=1" + " AND gespraeche.angelegt>=" + von + " AND gespraeche.angelegt<=" + bis);
			while (rs.next()) {
				ret.add(new Gespraech(rs));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get only the termine, means gespraeche that are payed
	 * 
	 * @return has this contact one or more termine
	 */
	public Vector<Gespraech> getTermine() {
		Vector<Gespraech> ret = new Vector<Gespraech>();
		try {
			ResultSet rs = Database.select("*", "gespraeche", "WHERE marktforscher=" + this.cId);
			while (rs.next()) {
				Gespraech g = new Gespraech(rs);
				if (g.ergebnisType() > 0) {
					ret.add(g);
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get only the termine, means gespraeche that are payed
	 * 
	 * @param range
	 *            date range for termin searching
	 * @return has this contact one or more termine
	 */
	public Vector<Gespraech> getHonorierbareTermine(DateInterval range) {
		Date von = range.getVon();
		Date bis = range.getBis();
		Vector<Gespraech> ret = new Vector<Gespraech>();
		try {
			// ResultSet rs = Database.select("*", "gespraeche", "WHERE
			// marktforscher="+this.cId+
			// " AND angelegt>='"+von+"' AND angelegt<='"+bis+"' ORDER BY
			// kunde");
			ResultSet rs = Database.select("*", "gespraeche", "WHERE marktforscher=" + this.cId + " AND angelegt>=ADDDATE('" + von
					+ "',INTERVAL 7 DAY) AND angelegt<=ADDDATE('" + bis + "',INTERVAL 7 DAY) ORDER BY kunde");
			while (rs.next()) {
				Gespraech g = new Gespraech(rs);
				if (g.ergebnisType() > 0) {
					ret.add(g);
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get all gespraeche, the one that will not be payed too
	 * 
	 * @return has this contact one or more termine
	 */
	public Vector<Gespraech> getGespraeche() {
		Vector<Gespraech> ret = new Vector<Gespraech>();
		ResultSet rs = null;
		try {
			rs = Database.select("*", "gespraeche", "WHERE marktforscher=" + this.cId + " ORDER BY angelegt DESC");
			while (rs.next()) {
				Gespraech g = new Gespraech(rs);
				ret.add(g);
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
	 * get all gespraeche, the one that will not be payed too
	 * 
	 * @param range
	 *            date range for termin searching
	 * @return has this contact one or more termine
	 */
	public Vector<Gespraech> getGespraeche(DateInterval range) {
		Date von = range.getVon();
		Date bis = range.getBis();
		Vector<Gespraech> ret = new Vector<Gespraech>();
		ResultSet rs = null;
		try {
			rs = Database.select("*", "gespraeche", "WHERE marktforscher=" + this.cId + " AND angelegt>='" + von + "' AND angelegt<='"
					+ bis + "' ORDER BY kunde");
			while (rs.next()) {
				ret.add(new Gespraech(rs));
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		return ret;
	}

	// ==========================================================
	// ============= getters and setters ========================
	// ==========================================================

	public String getHausnummer() {
		return hausnummer;
	}

	public void setHausnummer(String hausnr) {
		this.hausnummer = hausnr;
	}

	public String getId() {
		return cId;
	}

	public void setId(String id) {
		this.cId = id;
	}

	public String getNachName() {
		return nachName;
	}

	public void setNachName(String nachName) {
		this.nachName = nachName;
	}

	public String getPlz() {
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getStadt() {
		return stadt;
	}

	public void setStadt(String stadt) {
		this.stadt = stadt;
	}

	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	public String getVorName() {
		return vorName;
	}

	public void setVorName(String vorName) {
		this.vorName = vorName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHandy() {
		return handy;
	}

	public void setHandy(String handy) {
		this.handy = handy;
	}

	public String getTelefax() {
		return telefax;
	}

	public void setTelefax(String telefax) {
		this.telefax = telefax;
	}

	public int getNameStyle() {
		return nameStyle;
	}

	public void setNameStyle(int nameStyle) {
		this.nameStyle = nameStyle;
	}

	public boolean getAktiv() {
		return isAktiv();
	}

	public String getAnrede() {
		return anrede;
	}

	public String getBlz() {
		return blz;
	}

	public double getHonorarAdresse() {
		return honorarAdresse;
	}

	public double getHonorarPauschale() {
		return honorarPauschale;
	}

	public double getHonorarTermin() {
		return honorarTermin;
	}

	public String getKontonummer() {
		return kontonummer;
	}

	public boolean isSolar() {
		return solar;
	}

	public void setSolar(boolean solar) {
		this.solar = solar;
	}

	public static double getDefaultHonorarAdresse() {
		return 0.4;
	}

	public static double getDefaultHonorarPauschale() {
		return 0.13;
	}

	public static double getDefaultHonorarTermin() {
		return 30;
	}

	public int getSHFFlag() {
		return this.solar ? DBTools.SHF_SOLAR : DBTools.SHF_FH;
	}

	public void setGruppe(String gruppe) {
		this.gruppe = gruppe;
	}

	public String getGruppe() {
		return gruppe;
	}

	public void setGeburtstag(Date geburtstag) {
		this.geburtstag = geburtstag;
	}

	public Date getGeburtstag() {
		return geburtstag;
	}

	public void setEintrittsdatum(Date eintrittsdatum) {
		this.eintrittsdatum = eintrittsdatum;
	}

	public Date getEintrittsdatum() {
		return eintrittsdatum;
	}

	public void setKurzName(String kurzName) {
		this.kurzName = kurzName;
	}

	public String getKurzName() {
		return kurzName;
	}

	public void setKontonummer(String kontonummer) {
		this.kontonummer = kontonummer;
	}

	public void setBlz(String blz) {
		this.blz = blz;
	}

	public void setHonorarTermin(double honorarTermin) {
		this.honorarTermin = honorarTermin;
	}

	public void setHonorarAdresse(double honorarAdresse) {
		this.honorarAdresse = honorarAdresse;
	}

	public void setHonorarPauschale(double honorarPauschale) {
		this.honorarPauschale = honorarPauschale;
	}

	public void setAktiv(boolean aktiv) {
		this.aktiv = aktiv;
	}

	public boolean isAktiv() {
		return aktiv;
	}
}
