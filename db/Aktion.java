package db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;

import tools.MyLog;

public class Aktion {

	public static final String NOT_IN_DB_ID = "-1";

	public static final int TELEFON = 1;
	public static final int MAILING = 2;
	public static final int EMAIL = 3;
	public static final int TELEFAX = 4;

	public static final int MAILINGRESULT = 32;

	private static final String splitter = ";";

	private String id;
	private String contact;
	private String mafo;
	private AktionsErgebnis ergebnis;
	private Date angelegt;
	private Time angelegtZeit;
	private int typ;
	private Date eingang;
	private int abgerechnet;
	private int shf;
	private boolean fromBackup;

	public Aktion(String s) {
		String[] data = s.split(splitter);
		this.contact = data[0];
		this.ergebnis = new AktionsErgebnis(data[1]);
		this.angelegt = java.sql.Date.valueOf(data[2]);
		this.angelegtZeit = java.sql.Time.valueOf(data[3]);
		this.fromBackup = true;
	}

	/**
	 * this is the ctor for normal creation of an action from scratch
	 * 
	 * @param c
	 *            the contact to whom the aktion belongs
	 * @param mafo
	 *            the marktforscher who made the aktion
	 * @param erg
	 *            the result of the aktion
	 * @param typ
	 *            the type of aktion, normally telefon
	 */
	public Aktion(Contact c, Marktforscher mafo, String erg, int typ, java.sql.Date aDate, java.sql.Time aTime,
			boolean fromBackup) {
		this.id = NOT_IN_DB_ID;
		this.contact = c.getId();
		this.mafo = mafo.getId();
		this.ergebnis = new AktionsErgebnis(erg);
		this.typ = typ;

		java.util.Date rightNow = new java.util.Date(); // get Today date
		long utilDate = rightNow.getTime(); // Converts ur util.date into a long
		// value
		if (aDate != null) {
			this.angelegt = aDate;
		} else {
			this.angelegt = new java.sql.Date(utilDate);
		}
		if (aTime != null) {
			this.angelegtZeit = aTime;
		} else {
			this.angelegtZeit = new java.sql.Time(utilDate);
		}
		this.abgerechnet = 0;
		this.shf = mafo.getSHFFlag();
		this.fromBackup = fromBackup;
	}

	/**
	 * this is the ctor to get an existing action from db
	 * 
	 * @param rs
	 *            the resultset with aktion in it
	 */
	public Aktion(ResultSet rs) {
		try {
			this.id = rs.getString("id");
			this.contact = rs.getString("kunde");
			this.mafo = rs.getString("marktforscher");
			this.ergebnis = new AktionsErgebnis(rs.getString("ergebnis"));
			this.typ = rs.getInt("aktionstyp");
			this.angelegt = rs.getDate("angelegt");
			this.angelegtZeit = rs.getTime("angelegtZeit");
			this.eingang = rs.getDate("eingangsdatum");
			this.abgerechnet = rs.getInt("abgerechnet");
			this.shf = rs.getInt("shf");
			this.fromBackup = rs.getBoolean("frombackup");
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	public String toString() {
		String ret = "";
		// check if there is shortname
		if (this.ergebnis != null) {
			String kurz = this.ergebnis.getKurzname();
			String pattern = "dd.MM.yyyy";
			String patternZeit = "HH:mm:ss";
			String dateStr = "";
			String timeStr = "";
			SimpleDateFormat formatter;
			formatter = new SimpleDateFormat(pattern);
			dateStr = formatter.format(this.angelegt);
			SimpleDateFormat timeFormatter;
			timeFormatter = new SimpleDateFormat(patternZeit);
			timeStr = timeFormatter.format(this.angelegtZeit);

			if (kurz != null && kurz.length() > 0) {
				ret += "<html>" + dateStr + " um " + timeStr + ", <b>" + kurz + "</b> : " + this.ergebnis.getName()
						+ "</html>";
			} else {
				ret += "<html>" + dateStr + " um " + timeStr + ", <b>" + this.ergebnis.getName() + "</b></html>";
			}
		} else {
			MyLog.logError("Kein Ergebnis bei :" + this.id);
		}
		return ret;
	}

	public String toDetailString() {
		String ret = "<html>";
		// check if there is shortname
		if (this.ergebnis != null) {
			String kurz = this.ergebnis.getKurzname();
			String pattern = "dd.MM.yyyy";
			String patternZeit = "HH:mm:ss";
			String dateStr = "";
			String timeStr = "";
			SimpleDateFormat formatter;
			formatter = new SimpleDateFormat(pattern);
			dateStr = formatter.format(this.angelegt);
			SimpleDateFormat timeFormatter;
			timeFormatter = new SimpleDateFormat(patternZeit);
			timeStr = timeFormatter.format(this.angelegtZeit);
			if (kurz != null && kurz.length() > 0) {
				ret += dateStr + " um " + timeStr + ", <b>" + kurz + "</b> : " + this.ergebnis.getName();
			} else {
				ret += dateStr + " um " + timeStr + ", <b>" + this.ergebnis.getName() + "</b>";
			}
			ret += ", angerufen von <i>" + DBTools.nameOfMafo(this.mafo, true) + "</i>";
			ret += "</html>";
		} else {
			MyLog.logError("Kein Ergebnis bei :" + this.id);
		}
		return ret;
	}

	/**
	 * no swing markup, for excel insert...
	 * 
	 * @return string for this aktion
	 */
	public String toNoHTMLString() {
		String ret = "";
		ret += this.ergebnis.getName() + " [" + this.ergebnis.getId() + "], vom " + this.angelegt + " um "
				+ this.angelegtZeit;
		return ret;
	}

	public String Serialize() {
		StringBuffer ret = new StringBuffer();
		ret.append(this.getContact());
		ret.append(splitter);
		ret.append(this.ergebnis.getId());
		ret.append(splitter);
		ret.append(this.angelegt.toString());
		ret.append(splitter);
		ret.append(this.angelegtZeit.toString());
		ret.append(splitter);
		return ret.toString();
	}

	public static Aktion Deserialize(String in) {
		return new Aktion(in);
	}

	public static String easyNewAktion(String idKunde, int ergebnis, int infoID, int typ) {
		String id = null;
		// insert new
		try {
			// add to db
			PreparedStatement p1 = Database.getPreparedStatement("INSERT INTO aktionen "
					+ "(kunde, ergebnis, angelegt, aktionsInfo, aktionstyp) VALUES (?,?,NOW(),?,?)");
			p1.setString(1, idKunde);
			p1.setInt(2, ergebnis);
			p1.setInt(3, infoID);
			p1.setInt(4, typ);
			p1.executeUpdate();

			ResultSet rs = p1.getGeneratedKeys();
			while (rs.next()) {
				id = rs.getString(1);
			}
			System.out.println("neu aktion angelegt: " + id);
			Database.close(p1);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return id;
	}

	public static int easyNewAktionsInfo(String txt) {
		int id = 0;
		// insert new
		try {
			// add to db
			PreparedStatement p1 = Database.getPreparedStatement("INSERT INTO aktionsInfo (name, angelegt) "
					+ "VALUES (?, NOW())");
			p1.setString(1, txt);
			p1.executeUpdate();

			ResultSet rs = p1.getGeneratedKeys();
			while (rs.next()) {
				id = rs.getInt(1);
			}
			Database.close(p1);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return id;
	}

	public String CalcHasAktion() {
		String ret = "1";
		if (this.ergebnis.isMailingEnabled()) {
			ret = "2";
		}
		return ret;
	}

	/**
	 * test if this aktion is a unfinished one. means for example contact has
	 * not been reached, but needs to be called again. so you need this contact
	 * to be represented.
	 * 
	 * @return
	 */
	public boolean isFinishingAktion() {
		return this.ergebnis.isFinishedAfter() == 1;
	}

	public boolean isNotInDB() {
		return id.equals(NOT_IN_DB_ID);
	}

	public String getContact() {
		return contact;
	}

	public Date getAngelegt() {
		return angelegt;
	}

	public Date getEingang() {
		return eingang;
	}

	public String getErgebnis() {
		return ergebnis.getId();
	}

	public int getErgebnisAsInt() {
		return Integer.parseInt(ergebnis.getId());
	}

	public AktionsErgebnis getErgebnisAsAE() {
		return ergebnis;
	}

	public String getMafo() {
		return mafo;
	}

	public int getTyp() {
		return typ;
	}

	public String getId() {
		return id;
	}

	public int getAbgerechnet() {
		return abgerechnet;
	}

	public boolean isAbgerechnet() {
		return abgerechnet > 0;
	}

	public void setAbgerechnet(int abgerechnet) {
		this.abgerechnet = abgerechnet;
	}

	public Time getAngelegtZeit() {
		return angelegtZeit;
	}

	public void setAngelegtZeit(Time angelegtZeit) {
		this.angelegtZeit = angelegtZeit;
	}

	public void setAngelegt(Date angelegt) {
		this.angelegt = angelegt;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setEingang(Date eingang) {
		this.eingang = eingang;
	}

	public void setErgebnis(AktionsErgebnis ergebnis) {
		this.ergebnis = ergebnis;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMafo(String mafo) {
		this.mafo = mafo;
	}

	public void setTyp(int typ) {
		this.typ = typ;
	}

	public int getShf() {
		return shf;
	}

	public void setShf(int shf) {
		this.shf = shf;
	}

	public boolean isFromBackup() {
		return fromBackup;
	}

	public void setFromBackup(boolean fromBackup) {
		this.fromBackup = fromBackup;
	}

}
