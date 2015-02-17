package db;

import java.sql.ResultSet;
import java.sql.SQLException;

import tools.MyLog;

public class Projektleiter {
	private String cId;
	private String nachName;
	private String vorName;
	private String kurzName;
	private String strasse;
	private String hausnr;
	private String plz;
	private String stadt;
	private String telefon;
	private String telefax;
	private String handy;
	private String email;

	/**
	 * ctor for Werbeleiter from sql.resultset
	 * 
	 * @param rs
	 */
	public Projektleiter(ResultSet rs) {
		try {
			this.cId = rs.getString("id");
			this.nachName = rs.getString("nachname");
			this.vorName = rs.getString("vorname");
			this.kurzName = rs.getString("kurzname");
			this.strasse = rs.getString("strasse");
			this.hausnr = rs.getString("hausnummer");
			this.plz = rs.getString("plz");
			this.stadt = rs.getString("stadt");
			this.telefon = rs.getString("telefon");
			this.telefax = rs.getString("telefax");
			this.handy = rs.getString("handy");
			this.email = rs.getString("email");
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	/**
	 * ctor from id
	 * 
	 * @param id
	 *            of Werbeleiter to construct
	 */
	public Projektleiter(String kurzname, String dummy) {
		try {
			ResultSet rs = Database.select("*", "werbeleiter", "WHERE kurzname='" + kurzname.trim() + "'");
			if (rs.next()) {
				this.cId = rs.getString("id");
				this.nachName = rs.getString("nachname");
				this.vorName = rs.getString("vorname");
				this.kurzName = rs.getString("kurzname");
				this.strasse = rs.getString("strasse");
				this.hausnr = rs.getString("hausnummer");
				this.plz = rs.getString("plz");
				this.stadt = rs.getString("stadt");
				this.telefon = rs.getString("telefon");
				this.telefax = rs.getString("telefax");
				this.handy = rs.getString("handy");
				this.email = rs.getString("email");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	public static Projektleiter searchProjektleiter(String id) {
		Projektleiter mf = null;
		if (id != null && id.length() > 0) {
			ResultSet rs = getResultSet(id);
			if (rs != null) {
				mf = new Projektleiter(rs);
			}
			Database.close(rs);
		}
		return mf;
	}

	/**
	 * get the resultset that points to db-data
	 * 
	 * @param id
	 *            is of wl to retrieve
	 * @return the resultset that points to db-data
	 */
	private static ResultSet getResultSet(String id) {
		ResultSet ret = null;
		if (id != null && id.length() > 0) {
			try {
				ret = Database.select("*", "werbeleiter", "WHERE     id=" + id);
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
		String ret = this.nachName + ", " + this.vorName + " [" + this.cId + "]";
		return ret;
	}

	public boolean equals(Projektleiter wl) {
		return this.cId.equals(wl.cId);
	}

	/**
	 * this is to pack mafo into xml
	 * 
	 * @return xml string representing this mafo
	 */
	public String toXMLString() {
		StringBuffer ret = new StringBuffer();
		ret.append("<werbeleiter>\n");
		ret.append("	<id>");
		ret.append(this.cId);
		ret.append("</id>\n");
		ret.append("	<nachname>");
		ret.append(this.nachName);
		ret.append("</nachname>\n");
		ret.append("	<vorname>");
		ret.append(this.vorName);
		ret.append("</vorname>\n");
		ret.append("	<kurzname>");
		ret.append(this.kurzName);
		ret.append("</kurzname>\n");
		ret.append("	<strasse>");
		ret.append(this.strasse);
		ret.append("</strasse>\n");
		ret.append("	<hausnummer>");
		ret.append(this.hausnr);
		ret.append("</hausnummer>\n");
		ret.append("	<plz>");
		ret.append(this.plz);
		ret.append("</plz>\n");
		ret.append("	<stadt>");
		ret.append(this.stadt);
		ret.append("</stadt>\n");
		ret.append("	<telefon>");
		ret.append(this.telefon);
		ret.append("</telefon>\n");
		ret.append("</werbeleiter>\n");
		return ret.toString();
	}

	// ==========================================================
	// ============= getters and setters ========================
	// ==========================================================

	public String getHausnr() {
		return hausnr;
	}

	public void setHausnr(String hausnr) {
		this.hausnr = hausnr;
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

	public String getKurzName() {
		return kurzName;
	}

}
