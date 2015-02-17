package db;

import importer.OVTImportHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import tools.MyLog;
import tools.MyMailLog;
import tools.SettingsReader;

/**
 * @author basti contact class, represents a contact in db
 */
public class Contact {

	public static final int NAMESTYLE_NORMAL = 0;
	public static final int NAMESTYLE_DETAILED = 1;

	public static final int STATE_FREE = 0;
	public static final int STATE_WAITING = 1;
	public static final int STATE_WORKING = 2;
	public static final int STATE_FINISHED = 3;
	public static final int STATE_TEMP = 4;
	public static final int STATE_LOCKED = 99;

	public static final String ISMAILINGENABLED = "2";

	private boolean changed;
	private boolean noActionsLoadedYet = true;
	private boolean noValuesYet = false;
	private Date angelegt;
	private Date bereitgestellt;
	private int shfflag;
	private String bearbeitungsstatus;
	private String email;
	private String fassadenart;
	private String fassadenfarbe;
	private String fensterzahl;
	private String glasbausteine;
	private String hasaktion;
	private String hasauftrag;
	private String hasgespraech;
	private String hausnr;
	private String haustuerfarbe;
	private String heizung;
	private String id;
	private String mafo;
	private String mde;
	private String nachName;
	private String notiz;
	private String plz;
	private String solarProdukt;
	private String stadt;
	private String strasse;
	private String telefax;
	private String telefonBuero;
	private String telefonPrivat;
	private String vorName;
	private String zaunlaenge;

	// all aktionen from db, billed and unbilled together..
	private Vector<Aktion> dbAktionen = new Vector<Aktion>();
	// all aktionen from db that where billed, means abgerechnet!=0
	private Vector<Aktion> dbAktionenBilled = new Vector<Aktion>();
	// all aktionen from db that where billed, means abgerechnet!=0
	private Vector<Aktion> dbAktionenUnbilled = new Vector<Aktion>();
	// the aktionen from dbAktionenUnbilled that need to be redisplayed because
	// the are unfinished
	// and all aktionen that need to be displayed
	private Vector<Aktion> displayAktionen = new Vector<Aktion>();
	// all new aktionen that are not in db yet
	private Vector<Aktion> newAktionen = new Vector<Aktion>();
	private int nameStyle = NAMESTYLE_NORMAL;
	private boolean specialMailingEnabled4Search;

	/**
	 * ctor for contact from sql.resultset
	 * 
	 * @param rs
	 */
	public Contact(ResultSet rs) {
		this.fillWithValues(rs);
	}

	/**
	 * ctor for contact from sql.resultset
	 * 
	 * @param rs
	 */
	public Contact(ResultSet rs, boolean retrieveValues) {
		if (retrieveValues) {
			this.fillWithValues(rs);
		} else {
			this.fillOnlyID(rs);
			this.noValuesYet = true;
		}
	}

	public static Contact SearchContact(String id) {
		Contact c = null;
		if (id != null && id.length() > 0) {
			ResultSet rs = getResultSet(id);
			if (rs != null) {
				c = new Contact(rs, true);
			}
			Database.close(rs);
		}
		return c;
	}

	/**
	 * ctor where teh attr has to be setted trough setters
	 * 
	 * @param rs
	 */
	public Contact() {
		this.id = "";
		this.nachName = "";
		this.vorName = "";
		this.strasse = "";
		this.hausnr = "";
		this.plz = "";
		this.stadt = "";
		this.telefonPrivat = "";
		this.telefonBuero = "";
		this.email = "";
		this.mde = "";
		this.mafo = "";
		this.telefax = "";
		this.fensterzahl = "";
		this.heizung = "";
		this.fassadenart = "";
		this.fassadenfarbe = "";
		this.haustuerfarbe = "";
		this.glasbausteine = "";
		this.zaunlaenge = "";
		this.solarProdukt = "";
		this.notiz = "";
		this.bearbeitungsstatus = "";
		this.hasaktion = "";
		this.hasgespraech = "";
		this.hasauftrag = "";
		this.shfflag = -1;
		this.angelegt = null;
		this.bereitgestellt = null;
		this.changed = false;
		this.newAktionen = new Vector<Aktion>();
		this.dbAktionen = new Vector<Aktion>();
		this.dbAktionenBilled = new Vector<Aktion>();
		this.dbAktionenUnbilled = new Vector<Aktion>();
		this.displayAktionen = new Vector<Aktion>();
	}

	/**
	 * retrieve values from db-resultset
	 * 
	 * @param rs
	 *            the resultset pointing to the data
	 */
	private void fillWithValues(ResultSet rs) {
		if (rs != null) {
			try {
				this.id = DBTools.myGetString(rs, "id");
				this.bearbeitungsstatus = DBTools.myGetString(rs, "bearbeitungsstatus");
				this.hasaktion = DBTools.myGetString(rs, "hasaktion");
				this.hasgespraech = DBTools.myGetString(rs, "hasgespraech");
				this.hasauftrag = DBTools.myGetString(rs, "hasauftrag");
				this.nachName = DBTools.myGetString(rs, "nachname");
				this.vorName = DBTools.myGetString(rs, "vorname");
				this.strasse = DBTools.myGetString(rs, "strasse");
				this.hausnr = DBTools.myGetString(rs, "hausnummer");
				this.plz = DBTools.myGetString(rs, "plz");
				this.stadt = DBTools.myGetString(rs, "stadt");
				this.telefonPrivat = DBTools.myGetString(rs, "telprivat");
				this.telefonBuero = DBTools.myGetString(rs, "telbuero");
				this.telefax = DBTools.myGetString(rs, "telefax");
				this.email = DBTools.myGetString(rs, "email");
				this.mde = DBTools.myGetString(rs, "bearbeiter");
				this.mafo = DBTools.myGetString(rs, "marktforscher");
				this.heizung = DBTools.myGetString(rs, "heizung");
				String fz = Integer.toString(rs.getInt("fensterzahl"));
				if (fz == null) {
					fz = "";
				}
				this.fensterzahl = fz;
				this.fassadenart = DBTools.myGetString(rs, "fassadenart");
				this.fassadenfarbe = DBTools.myGetString(rs, "fassadenfarbe");
				this.haustuerfarbe = DBTools.myGetString(rs, "haustuerfarbe");
				this.glasbausteine = DBTools.myGetString(rs, "glasbausteine");
				this.solarProdukt = DBTools.myGetString(rs, "solarprodukt");
				this.notiz = DBTools.myGetString(rs, "notiz");
				this.angelegt = rs.getDate("angelegt");
				this.bereitgestellt = rs.getDate("bereitgestellt");
				this.shfflag = rs.getInt("shfflag");
				String zl = Integer.toString(rs.getInt("zaunlaenge"));
				if (zl == null) {
					zl = "";
				}
				this.zaunlaenge = zl;
				this.changed = false;

				this.noValuesYet = false;
				MyLog.logDebug("Adresse aus DB: " + this.toString());
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * get all aktionen stuff from db
	 */
	public void retrieveAktionen() {
		if (this.noActionsLoadedYet) {
			this.newAktionen = new Vector<Aktion>();
			this.dbAktionen = new Vector<Aktion>();
			this.dbAktionenBilled = new Vector<Aktion>();
			this.dbAktionenUnbilled = new Vector<Aktion>();
			this.displayAktionen = new Vector<Aktion>();
			ResultSet rs = null;
			try {
				rs = Database.select("*", "aktionen", "WHERE kunde=" + this.id + " ORDER BY angelegt");
				while (rs.next()) {
					Aktion a = new Aktion(rs);
					// now decide in which vectors we need to write the aktions
					this.dbAktionen.add(a); // always add here...
					if (a.getAbgerechnet() > 0) {
						// is billed one
						this.dbAktionenBilled.add(a);
					} else {
						// is unbilled one
						this.dbAktionenUnbilled.add(a);
						// now check if it is a aktion we need to redisplay
						if (!a.isFinishingAktion() && !a.isAbgerechnet()) {
							this.displayAktionen.add(a);
						}
					}
				}
				this.noActionsLoadedYet = false;
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} finally {
				Database.close(rs);
			}
		} else {
			// System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOO");
		}
	}

	/**
	 * get all aktionen stuff from db
	 */
	public void retrieveUnbilledMoreFinisherDBAktionen() {
		this.dbAktionenUnbilled = new Vector<Aktion>();
		ResultSet rs = null;
		try {
			rs = Database.select("*", "aktionen, ergebnisse", "WHERE aktionen.ergebnis=ergebnisse.id AND aktionen.kunde=" + this.id + " AND aktionen.abgerechnet=0 " + "AND ergebnisse.finishedafter>1 ORDER BY aktionen.angelegt");
			while (rs.next()) {
				Aktion a = new Aktion(rs);
				// is unbilled one
				this.dbAktionenUnbilled.add(a);
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
	}

	/**
	 * retrieve values from db-resultset
	 * 
	 * @param rs
	 *            the resultset pointing to the data
	 */
	private void fillOnlyID(ResultSet rs) {
		if (rs != null) {
			this.id = DBTools.myGetString(rs, "kunden.id");
		}
	}

	/**
	 * get the resultset that points to db-data
	 * 
	 * @param id
	 *            is of contact to retrieve
	 * @return the resultset that points to db-data
	 */
	private static ResultSet getResultSet(String id) {
		ResultSet ret = null;
		if (id != null && id.length() > 0) {
			try {
				ret = Database.select("*", "kunden", "WHERE     id=" + id);
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

	private boolean checkForValuesAndRetrieve() {
		boolean ret = false;
		if (this.noValuesYet) {
			ResultSet rs = getResultSet(this.id);
			if (rs != null) {
				this.fillWithValues(rs);
				Database.close(rs);
				ret = true;
			}
		} else {
			ret = true;
		}
		return ret;
	}

	/*
	 * reload all values
	 */
	public void update() {
		this.noValuesYet = true;
		this.checkForValuesAndRetrieve();
		this.noActionsLoadedYet = true;
		this.retrieveAktionen();
	}

	// ==========================================================
	// ================== methods ===============================
	// ==========================================================

	public String toString() {
		String ret = "n/a";
		if (this.checkForValuesAndRetrieve()) {
			if (this.nameStyle == NAMESTYLE_NORMAL) {
				ret = this.nachName + ", " + this.vorName + " [" + this.id + "]";
			} else if (this.nameStyle == NAMESTYLE_DETAILED) {
				ret = this.nachName + ", " + this.vorName + " [" + this.id + "] aus " + this.stadt;
			}
		}
		return ret;
	}

	public String toStringNoID() {
		String ret = "n/a";
		if (this.checkForValuesAndRetrieve()) {
			ret = this.nachName + ", " + this.vorName;
		}
		return ret;
	}

	public boolean equals(Object o) {
		if (o instanceof Contact) {
			if (this.id != null) {
				return this.id.equals(((Contact) o).id);
			}
		}
		return false;
	}

	/**
	 * has this contact one or more auftraege?
	 * 
	 * @return has this contact one or more auftraege
	 */
	public boolean hasAuftrag() {
		boolean ret = false;
		try {
			ResultSet rs = Database.select("*", "gespraeche, terminergebnisse", "WHERE gespraeche.ergebnis=terminergebnisse.id AND gespraeche.kunde=" + this.id + " AND terminergebnisse.erfolg=2");
			if (rs.next()) {
				ret = true;
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * has this contact one or more termine?
	 * 
	 * @return has this contact one or more termine
	 */
	public boolean hasTermin() {
		boolean ret = false;
		try {
			ResultSet rs = Database.select("*", "gespraeche, terminergebnisse", "WHERE gespraeche.ergebnis=terminergebnisse.id AND gespraeche.kunde=" + this.id + " AND terminergebnisse.erfolg>1");
			if (rs.next()) {
				ret = true;
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * get termine
	 * 
	 * @return has this contact one or more termine
	 */
	public Vector<Gespraech> getTermine() {
		Vector<Gespraech> ret = new Vector<Gespraech>();
		try {
			ResultSet rs = Database.select("*", "gespraeche, terminergebnisse", "WHERE gespraeche.ergebnis=terminergebnisse.id AND gespraeche.kunde=" + this.id + " AND terminergebnisse.erfolg>1");
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
	 * get gesprÃ¤che
	 * 
	 * @return vectore of gespraeche (including termine and auftrÃ¤ge)
	 */
	public Vector<Gespraech> getGespraeche() {
		Vector<Gespraech> ret = new Vector<Gespraech>();
		if (this.id != null && this.id.length() > 0) {
			try {
				ResultSet rs = Database.select("*", "gespraeche", "WHERE kunde=" + this.id);
				while (rs.next()) {
					ret.add(new Gespraech(rs));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * has this contact one or more aktions?
	 * 
	 * @return has this contact one or more aktions
	 */
	public boolean hasAktionen(boolean onlyNew) {
		this.retrieveAktionen();
		boolean ret = false;
		if (onlyNew) {
			ret = this.newAktionen.size() > 0;
		} else {
			ret = this.dbAktionen.size() > 0;
			ret |= this.newAktionen.size() > 0;
		}
		return ret;
	}

	/**
	 * checks if this contact has aktionen on display that are finishing the work on that contakt
	 * 
	 * @return true if contact has finishing aktion on display
	 */
	public boolean hasFinishingAktionOnDisplay() {
		return this.vectorContainsAnyFinishingActions(this.getDisplayAktionen());
	}

	/**
	 * checks if this contact has one aktion on display that finishes the work on this contact with a single appearance
	 * 
	 * @return true if contact has finishing aktion on display
	 */
	public boolean hasSingleFinishingAktionOnDisplay() {
		return this.vectorContainsSingleFinishingActions(this.getDisplayAktionen());
	}

	/**
	 * checks if this contact has aktionen in db that are finishing the work on that contakt
	 * 
	 * @return true if contact has finishing aktion on display
	 */
	public boolean hasFinishingButUnbilledAktionInDB() {
		this.retrieveAktionen();
		return this.vectorContainsAnyFinishingActions(this.dbAktionenUnbilled);
	}

	/**
	 * checks if this contact has aktionen in db that are finishing the work on that contakt
	 * 
	 * @return true if contact has finishing aktion on display
	 */
	public boolean hasFinishingButUnbilledMoreFinisherAktionInDB() {
		this.retrieveUnbilledMoreFinisherDBAktionen();
		return this.vectorContainsAnyFinishingActions(this.dbAktionenUnbilled);
	}

	/**
	 * check if given vector contains finishing actions
	 * 
	 * @param va
	 *            the vector to check
	 * @return true if vector contains finishing aktion
	 */
	private boolean vectorContainsAnyFinishingActions(Vector<Aktion> va) {
		boolean ret = false;
		// check all aktions for finish count
		if (va != null && va.size() > 0) {
			for (Iterator<Aktion> iter = va.iterator(); iter.hasNext();) {
				Aktion a = iter.next();
				// MyLog.o(this.cId+" :"+a);
				ret |= a.isFinishingAktion();
			}
		}
		// MyLog.o("ret:"+ret);
		// ok there is no finishing aktion. now we need to test if there
		// are aktion with a higher finisher count like "nicht erreicht".
		// it needs to be called more than once to finish this contakt
		if (!ret && va != null && va.size() > 0) {
			int ergCount = AktionsErgebnis.getAktionErgebnisseAsMap().size();
			int[] tmp = new int[ergCount];
			// find aktions that need more than one occurence to finish
			for (Aktion a : va) {
				int finishCount = a.getErgebnisAsAE().isFinishedAfter();
				if (finishCount > 1) {
					int e = a.getErgebnisAsInt();
					tmp[e]++;
					ret = tmp[e] >= finishCount;
				}
			}
		}
		return ret;
	}

	/**
	 * check if given vector contains actions that finishes with one appearance
	 * 
	 * @param va
	 *            the vector to check
	 * @return true if vector contains finishing aktion
	 */
	private boolean vectorContainsSingleFinishingActions(Vector<Aktion> va) {
		boolean ret = false;
		// check all aktions for finish count
		if (va != null && va.size() > 0) {
			for (Aktion a : va) {
				ret |= a.isFinishingAktion();
			}
		}
		return ret;
	}

	/**
	 * remove first found finisher aktion from newaktionen
	 */
	public void removeSingleFinisherAktionFromDisplay() {
		Aktion toDel = null;
		for (Iterator<Aktion> iter = this.displayAktionen.iterator(); iter.hasNext();) {
			Aktion a = iter.next();
			if (a.isFinishingAktion()) {
				toDel = a;
			}
		}
		this.displayAktionen.remove(toDel);
		this.newAktionen.remove(toDel);
	}

	/**
	 * has this contact only actions before given date?
	 * 
	 * @return has this contact only actions before given date?
	 */
	public boolean hasOnlyAktionenPrior(java.sql.Date d) {
		boolean ret = true;
		Vector<Aktion> as = getDbAktionen();
		if (as != null && as.size() > 0) {
			for (Aktion a : as) {
				ret &= a.getAngelegt().before(d);
			}
		}
		return ret;
	}

	/**
	 * this is to pack contact into xml
	 * 
	 * @return xml string representing this contact
	 */
	public String toXMLString() {
		this.checkForValuesAndRetrieve();
		StringBuffer ret = new StringBuffer();
		ret.append("<adresse>\n");
		ret.append("	<id>");
		ret.append(this.id);
		ret.append("</id>\n");
		ret.append("	<nachname>");
		ret.append(this.nachName);
		ret.append("</nachname>\n");
		ret.append("	<vorname>");
		ret.append(this.vorName);
		ret.append("</vorname>\n");
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
		ret.append("	<telprivat>");
		ret.append(this.telefonPrivat);
		ret.append("</telprivat>\n");
		ret.append("	<telbüro>");
		ret.append(this.telefonBuero);
		ret.append("</telbüro>\n");
		ret.append("	<telefax>");
		ret.append(this.telefax);
		ret.append("</telefax>\n");
		ret.append("	<email>");
		ret.append(this.email);
		ret.append("</email>\n");
		ret.append("	<note>");
		ret.append(this.notiz);
		ret.append("</note>\n");
		ret.append("</adresse>\n");
		return ret.toString();
	}

	/**
	 * compares this to c and returns if they differ
	 * 
	 * @param c
	 *            the contact to compare to this
	 * @return if this differs from c
	 */
	public boolean nearlyEqual(Contact c, boolean considerOnlyNewAktions) {
		this.checkForValuesAndRetrieve();
		boolean ret = true;
		ret &= this.nachName.equals(c.getNachName().trim());
		ret &= this.vorName.equals(c.getVorName().trim());
		ret &= this.strasse.equals(c.getStrasse().trim());
		ret &= this.hausnr.equals(c.getHausnr().trim());
		ret &= this.plz.equals(c.getPlz().trim());
		ret &= this.stadt.equals(c.getStadt().trim());
		ret &= this.telefonPrivat.equals(c.getTelefonPrivat().trim());
		ret &= this.telefonBuero.equals(c.getTelefonBuero().trim());
		ret &= this.telefax.equals(c.getTelefax().trim());
		ret &= this.email.equals(c.getEmail().trim());
		ret &= this.heizung.equals(c.getHeizung().trim());
		ret &= this.fensterzahl.equals(c.getFensterzahl().trim());
		ret &= this.fassadenart.equals(c.getFassadenart().trim());
		ret &= this.fassadenfarbe.equals(c.getFassadenfarbe().trim());
		ret &= this.haustuerfarbe.equals(c.getHaustuerfarbe().trim());
		ret &= this.zaunlaenge.equals(c.getZaunlaenge().trim());
		ret &= this.solarProdukt.equals(c.getSolarProdukt().trim());
		// ret &= this.glasbausteine.equals(c.getGlasbausteine().trim());
		ret &= this.notiz.equals(c.getNotiz().trim());
		ret &= this.shfflag == c.getShfflag();
		if (considerOnlyNewAktions) {
			ret &= !this.hasAktionen(true);
		} else {
			ret &= this.dbAktionen.size() == c.getDbAktionen().size();
		}
		return ret;
	}

	/**
	 * compares this to c and returns if they differ
	 * 
	 * @param c
	 *            the contact to compare to this
	 * @return if this differs from c
	 */
	public void setValues(Contact c) {
		this.setSomeValues(c);
		this.mde = c.getMde();
		this.mafo = c.getMafo();
	}

	/**
	 * set some values from a contact coming from gui. that means not alle values need to be set, because the gui does not contain all values
	 * 
	 * @param c
	 */
	public void setSomeValues(Contact c) {
		this.nachName = c.getNachName();
		this.vorName = c.getVorName();
		this.strasse = c.getStrasse();
		this.hausnr = c.getHausnr();
		this.plz = c.getPlz();
		this.stadt = c.getStadt();
		this.telefonPrivat = c.getTelefonPrivat();
		this.telefonBuero = c.getTelefonBuero();
		this.telefax = c.getTelefax();
		this.email = c.getEmail();
		this.fensterzahl = c.getFensterzahl();
		this.heizung = c.getHeizung();
		this.fassadenart = c.getFassadenart();
		this.fassadenfarbe = c.getFassadenfarbe();
		this.haustuerfarbe = c.getHaustuerfarbe();
		this.zaunlaenge = c.getZaunlaenge();
		this.solarProdukt = c.getSolarProdukt();
		this.shfflag = c.getShfflag();
		this.notiz = c.getNotiz();
		// aktionen
		this.dbAktionen = new Vector<Aktion>(c.getDbAktionen());
		this.newAktionen = new Vector<Aktion>(c.getNewAktionen());
		this.displayAktionen = new Vector<Aktion>(c.getDisplayAktionen());
		this.dbAktionenBilled = new Vector<Aktion>(c.getDbAktionenBilled());
		this.dbAktionenUnbilled = new Vector<Aktion>(c.getDbAktionenUnbilled());
		this.noActionsLoadedYet = false;
	}

	public void saveNewToDB() {
		PreparedStatement p1 = null;
		try {
			// add to db
			p1 = Database.getPreparedStatement("INSERT INTO kunden " + "(fassadenfarbe, nachname, vorname, strasse, " + "hausnummer, plz, stadt, telprivat, telbuero, telefax, email, heizung, fensterzahl, fassadenart, " + "haustuerfarbe, zaunlaenge, solarprodukt, angelegt, bearbeiter, notiz) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			p1.setString(1, this.fassadenfarbe);
			p1.setString(2, this.nachName);
			p1.setString(3, this.vorName);
			p1.setString(4, this.strasse);
			p1.setString(5, this.hausnr);
			p1.setString(6, this.plz);
			p1.setString(7, this.stadt);
			p1.setString(8, this.telefonPrivat);
			p1.setString(9, this.telefonBuero);
			p1.setString(10, this.telefax);
			p1.setString(11, this.email);
			p1.setString(12, this.heizung);
			p1.setString(13, this.fensterzahl);
			p1.setString(14, this.fassadenart);
			p1.setString(15, this.haustuerfarbe);
			p1.setString(16, this.zaunlaenge);
			p1.setString(17, this.solarProdukt);
			p1.setDate(18, this.angelegt);
			p1.setString(19, this.mde);
			p1.setString(20, this.notiz);
			p1.executeUpdate();

			// get id of last kunde
			ResultSet rs = p1.getGeneratedKeys();
			while (rs.next()) {
				this.id = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("doppelt:" + this.nachName);
		} finally {
			if (p1 != null) {
				Database.close(p1);
			}
		}
	}

	/**
	 * this saves any changes from contact: - changes of contactattributes - changes of aktions that means:
	 * 
	 * @param onlyNewActions
	 */
	public void saveToDB() {
		if (this.id.length() > 0) {
			String lastQuery = null;
			Marktforscher mf = null;
			try {
				mf = Marktforscher.SearchMarktforscher(this.mafo);
				String newBearbeitungsstatus = this.bearbeitungsstatus;
				String hasAktion = this.hasaktion;
				// ##################### SAVE THE AKTION DATA
				MyLog.logDebug("Aktionen bearbeiten start");

				// delete unnecessary multifinisher aktions
				if (this.hasSingleFinishingAktionOnDisplay()) {
					// just do anything if contact dont need to be locked, or is
					// locked already
					if (!newBearbeitungsstatus.equals(Integer.toString(Contact.STATE_LOCKED))) {
						// this is for actions from admin, when contact has no
						// mafo set, set him free directly
						if (mf == null) {
							newBearbeitungsstatus = Integer.toString(Contact.STATE_FREE);
						} else {
							newBearbeitungsstatus = Integer.toString(Contact.STATE_FINISHED);
						}
					}

					// check for unnecessary multifinisher aktions
					// only if there are single finisher actions...when contacts
					// is finished from multifinisher
					// aktions they will be deleted later
					lastQuery = Database.delete("aktionen", "WHERE marktforscher=" + this.mafo + " AND kunde=" + this.id + " AND ergebnis IN (" + AktionsErgebnis.multifinisherErgebnisse() + ")"
					// we had only deleted the last 7 days but we think now it
					// would be better to delete all.
					// +
					// " AND angelegt>=DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
					);
				}

				// now date
				Date nowDate = new Date(new java.util.Date().getTime());
				for (Iterator<Aktion> iter = this.newAktionen.iterator(); iter.hasNext();) {
					Aktion a = iter.next();
					// check if there is a aktion that marks the contact as
					// dirty
					if (!newBearbeitungsstatus.equals(Integer.toString(Contact.STATE_LOCKED)) && a.getErgebnisAsAE().isSetAdressDirty()) {
						// need to mark this contact as dirty...
						newBearbeitungsstatus = Integer.toString(Contact.STATE_LOCKED);
					}

					// save only new aktionen to db
					if (a.isNotInDB()) {
						// add action to db
						PreparedStatement p1 = Database.getPreparedStatement("INSERT INTO aktionen " + "(kunde, angelegt, angelegtZeit, aktionstyp, marktforscher, ergebnis, " + "eingangsdatum, shf, frombackup) " + "VALUES (?,?,?,?,?,?,NOW(),?,?)");
						p1.setString(1, a.getContact());
						p1.setDate(2, a.getAngelegt()); // is the date when
						// telefonist did the
						// call
						p1.setTime(3, a.getAngelegtZeit()); // is the date when
						// telefonist did
						// the call
						p1.setInt(4, a.getTyp());
						p1.setString(5, a.getMafo());
						p1.setString(6, a.getErgebnis());
						// p1.setDate(7, nowDate); // date when aktion is
						// inserted
						p1.setInt(7, a.getShf());
						p1.setBoolean(8, a.isFromBackup());

						// to db
						lastQuery = p1.toString();
						p1.executeUpdate();
						// get id of last kunde
						int lastID = -1;
						ResultSet rs = p1.getGeneratedKeys();
						while (rs.next()) {
							lastID = rs.getInt(1);
						}
						// set new aktion data
						a.setId(Integer.toString(lastID));
						// a.setEingang(nowDate);
						MyLog.logDebug("Aktion angelegtDB: " + lastID + "|" + a);
						Database.close(p1);
						// consider hasaktion, set it to 1 for all normal
						// aktions and set it to 2 for all
						// aktion that enable the contact for bigexcelexport
						hasAktion = a.CalcHasAktion();
					}
				}

				// TODO: is it right to empty the newAktionen vector? cause all
				// aktionen are in db now...
				this.dbAktionen.addAll(this.newAktionen);
				this.dbAktionenUnbilled.addAll(this.newAktionen);
				this.newAktionen.clear();

				// check for multi finishing aktions and compact them
				// this mean replace n multifinishingaktions with one
				// placeholder
				// better search only for the right mafo here!!
				int[][] toCompact = AktionsErgebnis.getErgebnisseWithHigherFinishingCount();
				for (int i = 0; i < toCompact[0].length; i++) {
					int ergID = toCompact[0][i];
					int finishCount = toCompact[1][i];
					ResultSet rs = Database.select("*", "aktionen", "WHERE kunde=" + this.id + " AND ergebnis=" + ergID + " AND marktforscher=" + this.mafo + " ORDER by ID");
					rs.last();
					int rows = rs.getRow();
					rs.beforeFirst();
					if (rows >= finishCount) {
						// delete all this rows
						String erg = Integer.toString(1000 + ergID);
						String todel = "";
						boolean first = true;
						while (rs.next()) {
							if (first) {
								// use the first one to set to other erg
								MyLog.logDebug("Multifinisher Aktion geändert: " + rs.getString("id") + " zu " + erg);
								rs.updateString("angelegt", nowDate.toString());
								rs.updateString("ergebnis", erg);
								rs.updateRow();
								// if we change notfinisher into finisher aktion
								// we need to change state to ready
								newBearbeitungsstatus = Integer.toString(Contact.STATE_FINISHED);
								first = false;
							} else {
								// delete all other aktions
								todel += rs.getString("id") + ",";
							}
						}
						// delete rows
						if (todel.length() > 0) {
							todel = todel.substring(0, todel.length() - 1);
							lastQuery = Database.delete("aktionen", "WHERE id IN (" + todel + ")");
							MyLog.logDebug("Multifinisher Aktion gelöscht: " + todel);
						}
					}
					Database.close(rs);
				}
				MyLog.logDebug("Aktionen bearbeiten ende");

				// ##################### SAVE THE CONTACT DATA
				MyLog.logDebug("Adresse speichern: " + this.id);
				// update contact data
				StringBuffer upStr = new StringBuffer();
				upStr.append("vorname='");
				upStr.append(this.vorName.trim());
				upStr.append("', ");
				upStr.append("nachname='");
				upStr.append(this.nachName.trim());
				upStr.append("', ");
				upStr.append("strasse='");
				upStr.append(this.strasse.trim());
				upStr.append("', ");
				upStr.append("hausnummer='");
				upStr.append(this.hausnr.trim());
				upStr.append("', ");
				upStr.append("plz='");
				upStr.append(this.plz.trim());
				upStr.append("', ");
				upStr.append("stadt='");
				upStr.append(this.stadt.trim());
				upStr.append("', ");
				upStr.append("telprivat='");
				upStr.append(this.telefonPrivat.trim());
				upStr.append("', ");
				upStr.append("telbuero='");
				upStr.append(this.telefonBuero.trim());
				upStr.append("', ");
				upStr.append("telefax='");
				upStr.append(this.telefax.trim());
				upStr.append("', ");
				upStr.append("email='");
				upStr.append(this.email.trim());
				upStr.append("', ");
				upStr.append("shfflag='");
				upStr.append(this.shfflag);
				upStr.append("', ");
				upStr.append("bearbeiter='");
				upStr.append(this.mde.trim());
				upStr.append("', ");
				upStr.append("fensterzahl='");
				upStr.append(this.fensterzahl.trim());
				upStr.append("', ");
				upStr.append("fassadenart='");
				upStr.append(this.fassadenart.trim());
				upStr.append("', ");
				upStr.append("heizung='");
				upStr.append(this.heizung.trim());
				upStr.append("', ");
				upStr.append("fassadenfarbe='");
				upStr.append(this.fassadenfarbe.trim());
				upStr.append("', ");
				upStr.append("zaunlaenge='");
				upStr.append(this.zaunlaenge.trim());
				upStr.append("', ");
				upStr.append("solarprodukt='");
				upStr.append(this.solarProdukt.trim());
				upStr.append("', ");
				// upStr.append("glasbausteine='");
				// upStr.append(this.glasbausteine.trim());
				// upStr.append("', ");
				upStr.append("notiz='");
				// upStr.append(this.notiz.replaceAll("'", " ").trim());
				upStr.append(this.notiz.trim());
				upStr.append("', ");
				// very important fields:
				upStr.append("bearbeitungsstatus='" + newBearbeitungsstatus + "', ");
				upStr.append("hasaktion='" + hasAktion + "' ");

				// do it
				MyLog.logDebug("Adresse statechange from:" + this.bearbeitungsstatus + " to: " + bearbeitungsstatus);
				// TODO: lastquery is not set when there is a exception thrown
				lastQuery = Database.updateThrowException("kunden", upStr.toString(), "WHERE id=" + this.id);

				// remove contact from the save list
				this.removeContactFromDiskCache();

			} catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException de) {
				String saveInfo = "Beim Speichern ist ein Fehler aufgetreten. \nDie Adresse (bzw. die Telefonnumer=" + this.telefonPrivat.trim() + ") mit dem Schlüssel " + this.id + " ist doppelt vorhanden.";
				JOptionPane.showMessageDialog(null, saveInfo, "Speicherfehler", JOptionPane.INFORMATION_MESSAGE);
				MyLog.logError(de);
				de.printStackTrace();

			} catch (SQLException e) {
				// MyLog.showExceptionErrorDialog("savingaktion", e);
				String saveInfo = "Beim Speichern ist ein Fehler aufgetreten. Bitte versuchen Sie es nochmal";
				JOptionPane.showMessageDialog(null, saveInfo, "Speicherfehler", JOptionPane.INFORMATION_MESSAGE);
				MyLog.logError(e);
				if (mf != null) {
					MyMailLog.logErrorMail("MF: " + mf.toString() + "\n\nCONTACT: " + this.toString() + "\n\nERROR: " + e.getMessage() + "\n\nLASTQUERY: " + lastQuery);
				} else {
					MyMailLog.logErrorMail("CONTACT: " + this.toString() + "\n\nERROR: " + e.getMessage() + "\n\nLASTQUERY: " + lastQuery);
				}
				e.printStackTrace();
			}
		}
		// set changed flag back
		this.changed = false;
	}

	private static String diskCacheFile = SettingsReader.getString("OVTMafoClient.DiskCacheFile");

	private void removeContactFromDiskCache() {
		// read
		Vector<String> data = readDiskCache();
		Vector<String> outData = new Vector<String>();

		// remove all the contacts data
		for (String str : data) {
			Aktion a = Aktion.Deserialize(str);
			if (!a.getContact().equals(this.id)) {
				outData.add(str);
			}
		}

		// write
		writeDiskCache(outData);
	}

	private void addAktionToDiskCache(Aktion a) {
		// read
		Vector<String> data = readDiskCache();

		// add a aktion to cache
		data.add(a.Serialize());

		// write
		writeDiskCache(data);
	}

	private void removeAktionFromDiskCache(Aktion a) {
		// read
		Vector<String> data = readDiskCache();
		Vector<String> outData = new Vector<String>();

		// remove one aktion from cache
		for (String str : data) {
			Aktion aa = Aktion.Deserialize(str);
			if (!aa.getContact().equals(a.getContact()) && !aa.getErgebnis().equals(a.getErgebnis())) {
				outData.add(str);
			}
		}

		// write
		writeDiskCache(outData);
	}

	public static Vector<String> readDiskCache() {
		Vector<String> ret = new Vector<String>();
		try {
			File f = new File(diskCacheFile);
			if (f.exists()) {
				LineNumberReader lnr = new LineNumberReader(new FileReader(f));
				String line = null;
				while ((line = lnr.readLine()) != null) {
					ret.add(line);
				}
			}
		} catch (Exception e) {
			MyLog.logError(e);
		}
		return ret;
	}

	public static void writeDiskCache(Vector<String> in) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(diskCacheFile));
			for (String str : in) {
				pw.println(str);
			}
			pw.close();
		} catch (Exception e) {
			MyLog.logError(e);
		}
	}

	/**
	 * get all aktionen for contact
	 * 
	 * @return list of aktions for contact
	 */
	public Vector<Aktion> getAktionenFromDB(boolean onlyNotBilled) {
		Vector<Aktion> ret = new Vector<Aktion>();
		try {
			String wc = "WHERE kunde=" + this.id + " ORDER BY angelegt";
			if (onlyNotBilled) {
				wc = "WHERE kunde=" + this.id + " AND abgerechnet=0 ORDER BY angelegt";
			}
			ResultSet rs = Database.select("*", "aktionen", wc);
			while (rs.next()) {
				ret.add(new Aktion(rs));
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * add aktion to the list of aktion in this contact
	 * 
	 * @param a
	 *            the aktion to add
	 */
	public void addAktion(Aktion a, boolean withDiskCache) {
		if (withDiskCache) {
			this.addAktionToDiskCache(a);
		}
		this.newAktionen.add(a);
		this.displayAktionen.add(a);
		this.changed = true;
	}

	/**
	 * del aktion from the list of aktion in this contact
	 * 
	 * @param a
	 *            the aktion to delete
	 */
	public void delAktion(Aktion a) {
		this.removeAktionFromDiskCache(a);
		this.newAktionen.remove(a);
		this.displayAktionen.remove(a);
		this.dbAktionen.remove(a);
	}

	/**
	 * delete this contact
	 */
	public void delMe() {
		Database.delete("kunden", "WHERE id=" + this.id);
	}

	public static String searchContactID(String telprivat) {
		String ret = "";
		try {
			ResultSet rs = Database.select("id", "kunden", "WHERE telprivat=" + telprivat);
			while (rs.next()) {
				ret = rs.getString("id");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	public static String[] getSomeInfos(String telprivat) {
		String[] ret = new String[4];
		try {
			ResultSet rs = Database.select("id, bereitgestellt, bearbeitungsstatus, marktforscher", "kunden", "WHERE telprivat=" + telprivat);
			while (rs.next()) {
				ret[0] = rs.getString("id");
				ret[1] = rs.getString("bereitgestellt");
				ret[2] = rs.getString("bearbeitungsstatus");
				ret[3] = rs.getString("marktforscher");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	public static String[] getMainInfos(String telprivat) {
		String[] ret = new String[6];
		try {
			ResultSet rs = Database.select("id, nachname, strasse, hausnummer, plz, bearbeitungsstatus", "kunden", "WHERE telprivat=" + telprivat);
			while (rs.next()) {
				ret[0] = rs.getString("id");
				ret[1] = rs.getString("nachname");
				ret[2] = rs.getString("strasse");
				ret[3] = rs.getString("hausnummer");
				ret[4] = rs.getString("plz");
				ret[5] = rs.getString("bearbeitungsstatus");
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	public void Lock() {
		this.bearbeitungsstatus = "99";
		this.saveToDB();
	}

	public void Unlock() {
		this.bearbeitungsstatus = "0";
		this.saveToDB();
	}

	// ==========================================================
	// ============= getters and setters ========================
	// ==========================================================

	public boolean isMailingenabled() {
		return this.hasaktion.equals(ISMAILINGENABLED);
	}

	public void setIsMailingenabledSpecial(boolean me) {
		this.specialMailingEnabled4Search = me;
	}

	public boolean getIsMailingenabledSpecial() {
		return this.specialMailingEnabled4Search;
	}

	public Vector<Aktion> getDbAktionen() {
		this.retrieveAktionen();
		return this.dbAktionen;
	}

	public int onlyGroupTwoAktions() {
		this.checkForValuesAndRetrieve();
		int ret = 0;
		for (Iterator<Aktion> iter = this.getDbAktionen().iterator(); iter.hasNext();) {
			Aktion a = iter.next();
			if (a.getErgebnisAsAE().getGruppe().equals("2")) {
				ret++;
			}
		}
		return ret;
	}

	/**
	 * look for newest aktion, in newaktionen vector (only unsaved aktionen stay there), and in oldaktionen vector if none was found
	 * 
	 * @return the newest aktion
	 */
	public Aktion getNewestAktion() {
		Aktion ret = null;
		// assume newest aktion is in newaktionen array
		Vector<Aktion> newAktions = this.getNewAktionen();
		if (newAktions != null && newAktions.size() > 0) {
			ret = newAktions.lastElement();
		}
		// if there was no element in newaktoinen retrieve newest aktion from db
		if (ret == null) {
			Vector<Aktion> oldAktions = this.getDbAktionen();
			if (oldAktions != null && oldAktions.size() > 0) {
				ret = oldAktions.lastElement();
			}
		}
		return ret;
	}

	/**
	 * look for newest aktion, in newaktionen vector (only unsaved aktionen stay there), and in oldaktionen vector if none was found
	 * 
	 * @return the newest aktion
	 */
	public Aktion getNewestNonMailAktion(boolean useNormalIfNonFound) {
		Aktion ret = null;
		// assume newest aktion is in newaktionen array
		Vector<Aktion> newAktions = this.getNewAktionen();
		for (Aktion a : newAktions) {
			if (a.getErgebnisAsAE().isMailingEnabled()) {
				ret = a;
			}
		}
		// if there was no element in newaktoinen retrieve newest aktion from db
		if (ret == null) {
			Vector<Aktion> oldAktions = this.getDbAktionen();
			for (Aktion a : oldAktions) {
				if (a.getErgebnisAsAE().isMailingEnabled()) {
					ret = a;
				}
			}
		}
		// if there is no nonmailing aktion return normal one
		if (ret == null && useNormalIfNonFound) {
			ret = this.getNewestAktion();
		}
		return ret;
	}

	/**
	 * getting only aktionen that where newly added
	 * 
	 * @return only aktionen that where newly added
	 */
	public Vector<Aktion> getNewAktionen() {
		this.retrieveAktionen();
		return this.newAktionen;
	}

	/**
	 * getting only aktionen that where newly added
	 * 
	 * @return only aktionen that where newly added
	 */
	public Vector<Aktion> getDisplayAktionen() {
		this.retrieveAktionen();
		return this.displayAktionen;
	}

	/**
	 * getting only aktionen that where newly added
	 * 
	 * @return only aktionen that where newly added
	 */
	public Vector<Aktion> getDisplayAktionen4Mafo(Marktforscher mf) {
		this.retrieveAktionen();
		Vector<Aktion> ret = new Vector<Aktion>();
		for (Aktion a : this.displayAktionen) {
			if (a.getMafo().equals(mf.getId())) {
				ret.add(a);
			}
		}
		return ret;
	}

	/**
	 * this removes all finishing aktionen from aktionen list
	 */
	public Vector<Aktion> getDbAktionenUnbilled() {
		this.retrieveAktionen();
		return this.dbAktionenUnbilled;
	}

	public Vector<Aktion> getDbAktionenBilled() {
		this.retrieveAktionen();
		return dbAktionenBilled;
	}

	public void setNewAktionen(Vector<Aktion> newA) {
		this.newAktionen = newA;
		this.noActionsLoadedYet = false;
	}

	public void setDisplayAktionen(Vector<Aktion> dspA) {
		this.displayAktionen = dspA;
		this.noActionsLoadedYet = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		this.changed = true;
	}

	public String getNachName() {
		this.checkForValuesAndRetrieve();
		return nachName;
	}

	public void setNachName(String nachName) {
		this.nachName = nachName;
		this.changed = true;
	}

	public String getVorName() {
		this.checkForValuesAndRetrieve();
		return vorName;
	}

	public void setVorName(String vorName) {
		this.vorName = vorName;
		this.changed = true;
	}

	public String getPlz() {
		this.checkForValuesAndRetrieve();
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
		this.changed = true;
	}

	public String getStadt() {
		this.checkForValuesAndRetrieve();
		return stadt;
	}

	public void setStadt(String stadt) {
		this.stadt = stadt;
		this.changed = true;
	}

	public String getStrasse() {
		this.checkForValuesAndRetrieve();
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
		this.changed = true;
	}

	public String getHausnr() {
		this.checkForValuesAndRetrieve();
		return hausnr;
	}

	public void setHausnr(String hausnr) {
		this.checkForValuesAndRetrieve();
		this.hausnr = hausnr;
		this.changed = true;
	}

	public String getTelefonPrivat() {
		this.checkForValuesAndRetrieve();
		return telefonPrivat;
	}

	public void setTelefonPrivat(String telefon) {
		this.telefonPrivat = OVTImportHelper.stripNumber(telefon);
		this.changed = true;
	}

	public String getTelefonBuero() {
		this.checkForValuesAndRetrieve();
		return telefonBuero;
	}

	public void setTelefonBuero(String telefonBuero) {
		this.telefonBuero = OVTImportHelper.stripNumber(telefonBuero);
		this.changed = true;
	}

	public String getTelefax() {
		this.checkForValuesAndRetrieve();
		return telefax;
	}

	public void setTelefax(String telefax) {
		this.telefax = OVTImportHelper.stripNumber(telefax);
		this.changed = true;
	}

	public String getEmail() {
		this.checkForValuesAndRetrieve();
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		this.changed = true;
	}

	public String getMde() {
		return mde;
	}

	public void setMde(String mde) {
		this.mde = mde;
		this.changed = true;
	}

	public String getHeizung() {
		this.checkForValuesAndRetrieve();
		return heizung;
	}

	public void setHeizung(String heizung) {
		this.heizung = heizung;
		this.changed = true;
	}

	public String getFassadenart() {
		this.checkForValuesAndRetrieve();
		return fassadenart;
	}

	public void setFassadenart(String fassadenart) {
		this.fassadenart = fassadenart;
		this.changed = true;
	}

	public String getFassadenfarbe() {
		this.checkForValuesAndRetrieve();
		return fassadenfarbe;
	}

	public void setFassadenfarbe(String fassadenfarbe) {
		this.fassadenfarbe = fassadenfarbe;
		this.changed = true;
	}

	public String getFensterzahl() {
		this.checkForValuesAndRetrieve();
		return fensterzahl;
	}

	public void setFensterzahl(String fensterzahl) {
		// check it is not out of range
		try {
			int fz = Integer.parseInt(fensterzahl);
			this.fensterzahl = Integer.toString(Math.max(0, fz));
		} catch (NumberFormatException e) {
			this.fensterzahl = "0";
		}
		this.changed = true;
	}

	public String getGlasbausteine() {
		this.checkForValuesAndRetrieve();
		return glasbausteine;
	}

	public void setGlasbausteine(String glasbausteine) {
		this.glasbausteine = glasbausteine;
		this.changed = true;
	}

	public String getHaustuerfarbe() {
		this.checkForValuesAndRetrieve();
		return haustuerfarbe;
	}

	public void setHaustuerfarbe(String haustuerfarbe) {
		this.haustuerfarbe = haustuerfarbe;
		this.changed = true;
	}

	public String getZaunlaenge() {
		this.checkForValuesAndRetrieve();
		return zaunlaenge;
	}

	public void setZaunlaenge(String zaunlaenge) {
		this.zaunlaenge = zaunlaenge;
		this.changed = true;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public Date getAngelegt() {
		this.checkForValuesAndRetrieve();
		return angelegt;
	}

	public void setAngelegt(Date angelegt) {
		this.angelegt = angelegt;
		this.changed = true;
	}

	public Date getBereitgestellt() {
		this.checkForValuesAndRetrieve();
		return bereitgestellt;
	}

	public void setBereitgestellt(Date bereitgestellt) {
		this.bereitgestellt = bereitgestellt;
		this.changed = true;
	}

	public boolean isNoValuesYet() {
		return noValuesYet;
	}

	public boolean isChanged() {
		return changed;
	}

	public String getNotiz() {
		this.checkForValuesAndRetrieve();
		return notiz;
	}

	public boolean hasNotiz() {
		this.checkForValuesAndRetrieve();
		return notiz.length() > 0;
	}

	public void setNotiz(String notiz) {
		this.notiz = notiz;
	}

	public String getMafo() {
		this.checkForValuesAndRetrieve();
		return mafo;
	}

	public void setMafo(String mafo) {
		this.mafo = mafo;
	}

	public int getNameStyle() {
		return nameStyle;
	}

	public void setNameStyle(int nameStyle) {
		this.nameStyle = nameStyle;
	}

	public int getShfflag() {
		this.checkForValuesAndRetrieve();
		return shfflag;
	}

	public void setShfflag(int shfflag) {
		this.shfflag = shfflag;
	}

	public String getBearbeitungsstatus() {
		this.checkForValuesAndRetrieve();
		return bearbeitungsstatus;
	}

	public void setBearbeitungsstatus(String bearbeitungsstatus) {
		this.bearbeitungsstatus = bearbeitungsstatus;
	}

	public boolean isLocked() {
		return this.bearbeitungsstatus.equals("99");
	}

	public String getSolarProdukt() {
		return solarProdukt;
	}

	public void setSolarProdukt(String solarProdukt) {
		this.solarProdukt = solarProdukt;
	}

	public String getHasgespraech() {
		return hasgespraech;
	}

	public void setHasgespraech(String hasgespraech) {
		this.hasgespraech = hasgespraech;
	}

	public String getHasauftrag() {
		return hasauftrag;
	}

	public void setHasauftrag(String hasauftrag) {
		this.hasauftrag = hasauftrag;
	}
}
