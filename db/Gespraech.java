package db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import tools.MyLog;

public class Gespraech {
	public static final int NAMESTYLE_NORMAL = 0;
	public static final int NAMESTYLE_DETAILED = 1;
	public static final String AUFTRAGID = "19";

	private String id;
	private String kundeID;
	private Date datumangelegt;
	private Date datumMF;
	private Date datumWele;
	private String ergebnis;
	private String produkt;
	private int vertragsBruttoSumme;
	private String werbeleiter;
	private String mafo;
	private String mde;
	private boolean abgerechnet;
	private String terminZeit;
	private boolean changed;
	private int shf;
	private int nameStyle = NAMESTYLE_NORMAL;

	/**
	 * ctor for termin
	 */
	public Gespraech() {
		this.id = "";
		this.kundeID = "";
		this.datumangelegt = null;
		this.datumMF = null;
		this.datumWele = null;
		this.terminZeit = "";
		this.ergebnis = "-1";
		this.produkt = "";
		this.werbeleiter = "";
		this.mafo = "";
		this.mde = "";
		this.vertragsBruttoSumme = 0;
		this.abgerechnet = false;
		this.changed = false;
		this.shf = -1;
	}

	/**
	 * ctor for termin from db
	 */
	public Gespraech(ResultSet rs) {
		try {
			this.id = rs.getString("id");
			this.kundeID = rs.getString("kunde");
			this.datumangelegt = rs.getDate("angelegt");
			this.datumMF = rs.getDate("datum_md");
			this.datumWele = rs.getDate("datum_vd");
			this.terminZeit = rs.getString("terminzeit");
			this.ergebnis = rs.getString("ergebnis");
			this.produkt = rs.getString("produkt");
			this.vertragsBruttoSumme = rs.getInt("summe");
			this.werbeleiter = rs.getString("werbeleiter");
			this.mafo = rs.getString("marktforscher");
			this.mde = rs.getString("marktdatenermittler");
			this.abgerechnet = rs.getBoolean("abgerechnet");
			this.shf = rs.getInt("shf");

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	/**
	 * create a new termin and save it to db
	 * 
	 * @param c
	 *            the contact for whom the termin is
	 * @param vDate
	 *            date of termin
	 * @param ergebnis
	 *            result of termin
	 * @param mafo
	 *            the marktforscher which made the termin
	 */
	public Gespraech(Contact c, java.util.Date mDate, java.util.Date vDate,
			String vdTime, String ergebnis, String produkt, int summe,
			String marktdatenermittler, Marktforscher mafo, Projektleiter wele) {
		this.kundeID = c.getId();
		if (mDate != null) {
			this.datumMF = new Date(mDate.getTime());
		}
		if (vDate != null) {
			this.datumWele = new Date(vDate.getTime());
		}
		this.terminZeit = vdTime;
		this.ergebnis = ergebnis;
		this.produkt = produkt;
		this.vertragsBruttoSumme = summe;
		if (mafo != null) {
			this.mafo = mafo.getId();
		}
		if (wele != null) {
			this.werbeleiter = wele.getId();
		}
		this.mde = marktdatenermittler;
		this.shf = mafo.getSHFFlag();
	}

	public void saveToDB() {
		if (this.id != null && this.id.length() > 0) {
			// update
			try {
				PreparedStatement p1 = Database
						.getPreparedStatement("UPDATE gespraeche "
								+ "SET datum_md=?, datum_vd=?, terminzeit=?, ergebnis=?, produkt=?, summe=?, werbeleiter=?, "
								+ "marktdatenermittler=?, marktforscher=? , shf=? WHERE id=?");
				p1.setDate(1, this.datumMF);
				p1.setDate(2, this.datumWele);
				p1.setString(3, this.terminZeit);
				p1.setString(4, this.ergebnis);
				p1.setString(5, this.produkt);
				p1.setInt(6, this.vertragsBruttoSumme);
				p1.setString(7, this.werbeleiter);
				p1.setString(8, this.mde);
				p1.setString(9, this.mafo);
				p1.setInt(10, this.shf);
				p1.setString(11, this.id);
				p1.executeUpdate();
				Database.close(p1);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			// insert new
			try {
				// add to db
				PreparedStatement p1 = Database
						.getPreparedStatement("INSERT INTO gespraeche "
								+ "(kunde, datum_md, datum_vd, terminzeit, ergebnis, produkt, summe, werbeleiter, "
								+ "marktdatenermittler, marktforscher, angelegt, shf) "
								+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
				p1.setString(1, this.kundeID);
				p1.setDate(2, this.datumMF);
				p1.setDate(3, this.datumWele);
				p1.setString(4, this.terminZeit);
				p1.setString(5, this.ergebnis);
				p1.setString(6, this.produkt);
				p1.setInt(7, this.vertragsBruttoSumme);
				p1.setString(8, this.werbeleiter);
				p1.setString(9, this.mde);
				p1.setString(10, this.mafo);
				java.util.Date nowTmp = new java.util.Date();
				java.sql.Date now = new java.sql.Date(nowTmp.getTime());
				p1.setDate(11, now);
				p1.setInt(12, this.shf);
				p1.executeUpdate();

				ResultSet rs = p1.getGeneratedKeys();
				while (rs.next()) {
					this.id = rs.getString(1);
				}
				Database.close(p1);
				this.changed = false;

				// update hasgespraech
				if (this.id != null && this.id.length() > 0) {
					// remember an auftrag and lock adress
					if (this.ergebnis.equals(AUFTRAGID)) {
						Database.update("kunden",
								"hasgespraech=1, hasauftrag=1, bearbeitungsstaus="
										+ Contact.STATE_LOCKED, "WHERE id="
										+ this.kundeID);
					} else {
						Database.update("kunden", "hasgespraech=1", "WHERE id="
								+ this.kundeID);
					}
				}
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
	}

	public void printMe() {
		System.out.println("id:         " + this.id);
		System.out.println("kunde:      " + this.kundeID);
		System.out.println("md-Date:    " + this.datumMF);
		System.out.println("vd-Date:    " + this.datumWele);
		System.out.println("terminzeit: " + this.terminZeit);
		System.out.println("ergebnis:   " + this.ergebnis);
		System.out.println("produkt:    " + this.produkt);
		System.out.println("summe:      " + this.vertragsBruttoSumme);
		System.out.println("w.leiter:   " + this.werbeleiter);
		System.out.println("mafo:       " + this.mafo);
		System.out.println("mde:        " + this.mde);
		System.out.println("shf:        " + this.shf);
	}

	public String toRawString() {
		StringBuffer sb = new StringBuffer();
		sb.append("id:           " + this.id);
		sb.append("|kunde:       " + this.kundeID);
		sb.append("|md-Date:     " + this.datumMF);
		sb.append("|vd-Date:     " + this.datumWele);
		sb.append("|terminzeit:  " + this.terminZeit);
		sb.append("|ergebnis:    " + this.ergebnis);
		sb.append("|produkt:     " + this.produkt);
		sb.append("|summe:       " + this.vertragsBruttoSumme);
		sb.append("|w.leiter:    " + this.werbeleiter);
		sb.append("|mafo:        " + this.mafo);
		sb.append("|mde:         " + this.mde);
		sb.append("|shf:         " + this.shf);
		return sb.toString();
	}

	/**
	 * check which kind of ergebnis this gesprÃ¤che had 0 = there was no
	 * gesprÃ¤ch 1 = gesprÃ¤ch w/o auftrag 2 = gesprÃ¤ch with auftrag
	 * 
	 * @return type of ergebnis
	 */
	public int ergebnisType() {
		return DBTools.typeOfTerminErgebnis(this.ergebnis);
	}

	public String toString() {
		String ret = "";
		if (this.id.length() > 0) {
			if (this.nameStyle == NAMESTYLE_DETAILED) {
				if (this.datumWele != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
					ret = "<html><b>Gespräch vom " + sdf.format(this.datumWele)
							+ "</b>: "
							+ DBTools.nameOfTerminErgebnis(this.ergebnis)
							+ "</html>";
				} else if (this.ergebnis != null) {
					ret = "<html><b>Gespräch</b>: "
							+ DBTools.nameOfTerminErgebnis(this.ergebnis)
							+ "</html>";
				} else {
					ret = "<html><b>Gesprächschlüssel</b>: " + this.id
							+ "</html>";
				}
			} else {
				ret = this.id;
			}
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
		ret += this.ergebnis + ", vom " + this.datumangelegt + " um "
				+ this.terminZeit;
		return ret;
	}

	/**
	 * delete this gespräch
	 */
	public void delMe() {
		Database.delete("gespraeche", "WHERE id=" + this.id);
	}

	/**
	 * insert new gesprÃ¤ch, w/o details
	 * 
	 * @param idKunde
	 * @param ergebnis
	 * @return
	 */
	public static String easyNewGespraech(int idKunde, String ergebnis,
			String weleStr, java.util.Date date) {
		String id = null;
		// insert new
		try {
			// add to db
			PreparedStatement p1 = Database
					.getPreparedStatement("INSERT INTO gespraeche "
							+ "(kunde, ergebnis, angelegt, werbeleiter) "
							+ "VALUES (?,?,?,?)");
			p1.setInt(1, idKunde);
			p1.setString(2, ergebnis);
			java.sql.Date now = new java.sql.Date(new java.util.Date()
					.getTime());
			if (date != null) {
				now = new java.sql.Date(date.getTime());
			}
			p1.setDate(3, now);
			p1.setString(4, weleStr);
			p1.executeUpdate();

			ResultSet rs = p1.getGeneratedKeys();
			while (rs.next()) {
				id = rs.getString(1);
			}
			Database.close(p1);

			// remember an auftrag
			if (ergebnis.equals(AUFTRAGID)) {
				Database.update("kunden", "hasgespraech=1, hasauftrag=1",
						"WHERE id=" + idKunde);
			} else {
				Database.update("kunden", "hasgespraech=1", "WHERE id="
						+ idKunde);
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
			System.out.println(idKunde + "," + ergebnis + "," + weleStr);
		}
		return id;
	}

	public Date getDatumWele() {
		return datumWele;
	}

	public void setDatumWele(Date datumtermin) {
		this.changed = true;
		this.datumWele = datumtermin;
	}

	public Date getDatumMF() {
		return datumMF;
	}

	public void setDatumMF(Date datumvereinbart) {
		this.changed = true;
		this.datumMF = datumvereinbart;
	}

	public String getErgebnis() {
		return ergebnis;
	}

	public void setErgebnis(String ergebnis) {
		this.changed = true;
		this.ergebnis = ergebnis;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKundeID() {
		return kundeID;
	}

	public void setKundeID(String kunde) {
		this.changed = true;
		this.kundeID = kunde;
	}

	public String getProjektleiter() {
		return werbeleiter;
	}

	public void setProjektleiter(String verkaeufer) {
		this.changed = true;
		this.werbeleiter = verkaeufer;
	}

	public String getMafo() {
		return mafo;
	}

	public void setMafo(String mafoID) {
		this.changed = true;
		this.mafo = mafoID;
	}

	public String getProdukt() {
		return produkt;
	}

	public void setProdukt(String produkt) {
		this.changed = true;
		this.produkt = produkt;
	}

	public int getVertragsBruttoSumme() {
		return vertragsBruttoSumme;
	}

	public void setVertragsBruttoSumme(int vertragsBruttoSumme) {
		this.changed = true;
		this.vertragsBruttoSumme = vertragsBruttoSumme;
	}

	public String getMde() {
		return mde;
	}

	public void setMde(String mde) {
		this.changed = true;
		this.mde = mde;
	}

	public boolean isAbgerechnet() {
		return abgerechnet;
	}

	public void setAbgerechnet(boolean abgerechnet) {
		this.changed = true;
		this.abgerechnet = abgerechnet;
	}

	public Date getDatumangelegt() {
		return datumangelegt;
	}

	public void setDatumangelegt(Date datumangelegt) {
		this.changed = true;
		this.datumangelegt = datumangelegt;
	}

	public String getTerminZeit() {
		return terminZeit;
	}

	public void setTerminZeit(String terminZeit) {
		this.changed = true;
		this.terminZeit = terminZeit;
	}

	public int getNameStyle() {
		return nameStyle;
	}

	public void setNameStyle(int nameStyle) {
		this.nameStyle = nameStyle;
	}

	public boolean isChanged() {
		return changed;
	}
}
