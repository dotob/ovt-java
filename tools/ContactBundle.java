package tools;

import java.awt.Dimension;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import ui.ContactPanel;
import ui.IMainWindow;
import db.Aktion;
import db.Contact;
import db.DBTools;
import db.Database;
import db.Gespraech;
import db.Marktforscher;

/**
 * a contact bundle is a set of contacts to work on
 * 
 * @author basti
 * 
 */
public class ContactBundle extends SwingWorker<Long, Object> {

	private ProgressMonitor pm;

	public final static int ANY = 0;
	public final static int WITHNOTE = 1;
	public final static int WITHOUTFINISHINGACTIONS = 2;
	public final static int WITHFINISHINGACTIONS = 3;
	public final static int WITHSOMEACTIONS = 4;

	public final static int SEARCHLIMIT = 50;

	public final int MFASSICOLLECT = 0;
	public final int ADMINCOLLECT = 1;
	public final int SEARCHCOLLECT = 2;

	private int WHATTODO = MFASSICOLLECT;
	private Vector<Contact> contacts;
	private String plz;
	private String stadt;
	private ListItem type;
	private boolean termin;
	private boolean auftrag;
	private int aktContactIndex;
	private Marktforscher mafo;
	private ContactPanel contactPanel;
	private int shfFlaeg;
	private int howManyContacts;
	private String terminProduct;
	private Date priorDate;
	private String auftragProduct;
	private String mde;
	private String importString;
	private boolean bigExcelExport;
	private boolean produceOutput;

	private IMainWindow parentWindow;

	private String lastSqlUsed = "";

	/**
	 * build a contactbundle from various settings
	 * 
	 * @param plz
	 *            pattern of plz to search in contacts
	 * @param stadt
	 *            pattern of stadt to search in contacts
	 * @param type
	 *            contakttype, like how often we had contact
	 * @param termin
	 *            has contacts to be with appointments
	 * @param auftrag
	 *            has contacts to be with orders
	 * @param anz
	 *            count of contact in bundle
	 * @param withOutput
	 *            TODO
	 * @param pw
	 *            TODO
	 */
	public ContactBundle(String plz, String stadt, ListItem type, boolean termin, boolean auftrag, int shfflag, int anz, String tProd,
			String aProd, Date priorDate, String mde, String wdh, boolean bigEE, boolean withOutput, IMainWindow pw) {
		this.plz = plz;
		this.stadt = stadt;
		this.type = type;
		this.termin = termin;
		this.auftrag = auftrag;
		this.contacts = new Vector<Contact>();
		this.shfFlaeg = shfflag;
		this.howManyContacts = anz;
		this.terminProduct = tProd;
		this.auftragProduct = aProd;
		this.priorDate = priorDate;
		this.mde = mde;
		this.importString = wdh;
		this.produceOutput = withOutput;
		this.setBigExcelExport(bigEE);
		this.parentWindow = pw;
		WHATTODO = ADMINCOLLECT;
		// prepare progressmonitor
		this.pm = new ProgressMonitor(this.parentWindow.getFrame(), "Kontakte sammeln", "", 0, this.howManyContacts);
		this.pm.setMillisToPopup(500);
		this.pm.setMillisToDecideToPopup(500);
	}

	/**
	 * this is the usual constructor for mafo to get the contacts to work on
	 * from database
	 * 
	 * @param mafo
	 *            the marktforscher to get the contacts for
	 */
	public ContactBundle(Marktforscher mafo, ContactPanel cp, IMainWindow pw) {
		this.contactPanel = cp;
		this.mafo = mafo;
		this.parentWindow = pw;
		WHATTODO = MFASSICOLLECT;
	}

	/**
	 * this is the constructor just an empty bundle
	 */
	public ContactBundle(Contact c) {
		this.WHATTODO = SEARCHCOLLECT;
		this.contacts = new Vector<Contact>();
		this.aktContactIndex = 0;
		ResultSet rs = null;
		try {
			String whereClause = "";
			whereClause += this.addSearchField("id", c.getId());
			whereClause += this.addSearchField("nachname", c.getNachName());
			whereClause += this.addSearchField("vorname", c.getVorName());
			whereClause += this.addSearchField("strasse", c.getStrasse());
			whereClause += this.addSearchField("hausnummer", c.getHausnr());
			whereClause += this.addSearchField("stadt", c.getStadt());
			whereClause += this.addSearchField("plz", c.getPlz());
			whereClause += this.addSearchField("telprivat", c.getTelefonPrivat());
			whereClause += this.addSearchField("telbuero", c.getTelefonBuero());
			whereClause += this.addSearchField("telefax", c.getTelefax());
			whereClause += this.addSearchField("notiz", c.getNotiz());
			whereClause += this.addSearchField("email", c.getEmail());
			whereClause += this.addSearchField("bearbeiter", c.getMde());
			whereClause += this.addSearchField("bearbeitungsstatus", c.getBearbeitungsstatus());
			if (c.getIsMailingenabledSpecial()) {
				whereClause += this.addSearchField("hasaktion", "2");
			}
			if (whereClause.startsWith(" AND")) {
				whereClause = whereClause.substring(4, whereClause.length());
			}
			if (whereClause.length() > 0) {
				whereClause = " WHERE " + whereClause.replace('*', '%').replace('?', '_');
			}

			if (whereClause.length() > 0) {
				rs = Database.selectDebug("*", "kunden searchAdmin", whereClause + " ORDER BY id LIMIT " + SEARCHLIMIT);
				while (rs.next()) {
					Contact cadd = new Contact(rs, true);
					cadd.retrieveAktionen();
					this.contacts.add(cadd);
				}
			} else {
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
	}

	private String addSearchField(String fieldname, String searchPattern) {
		String ret = "";
		if (searchPattern.length() > 0) {
			if (searchPattern.indexOf("*") >= 0 || searchPattern.indexOf("?") >= 0) {
				ret = " AND " + fieldname + " LIKE '" + searchPattern + "'";
			} else {
				ret = " AND " + fieldname + "='" + searchPattern + "'";
			}
		}
		return ret;
	}

	public void collectAdminContacts() {
		int notUsed = 0;
		this.parentWindow.setWaitCursor();
		ResultSet rs = null;
		try {
			String tables = "kunden";

			String whereClause = "WHERE kunden.bearbeitungsstatus=0 ";
			if (plz.length() > 0) {
				String plzQuery = "AND kunden.plz LIKE '" + plz.replace('*', '%').replace('?', '_') + "' ";
				whereClause += plzQuery;
			}
			if (stadt.length() > 0) {
				String stadtQuery = "AND kunden.stadt LIKE '" + stadt.replace('*', '%').replace('?', '_') + "' ";
				whereClause += stadtQuery;
			}
			if (mde != null && mde.length() > 0) {
				String mdeQuery = "AND kunden.bearbeiter=" + mde + " ";
				whereClause += mdeQuery;
			}
			if (this.importString != null && this.importString.length() > 0) {
				String wdhQuery = "AND kunden.importtag='" + this.importString + "' ";
				whereClause += wdhQuery;
			}

			// when searching for date join aktion table
			if (priorDate != null) {
				tables += ", aktionen a";
				whereClause += "AND kunden.id=a.kunde AND a.angelegt<='" + priorDate + "' ";
			}

			String whichKindOfContacts = type.getKey0();
			boolean noAktions = true;
			if (bigExcelExport) {
				// removed "kunden.hasgespraech=0 AND " for bernd at 25.12.07
				whereClause += "AND (kunden.hasaktion=0 OR kunden.hasaktion=2) AND kunden.hasauftrag=0 ";
			} else {
				noAktions = whichKindOfContacts.equals("1");
				if (whichKindOfContacts.equals("2") || whichKindOfContacts.equals("3")) {
					whereClause += "AND kunden.hasaktion=1 ";
				} else if (noAktions) {
					whereClause += "AND kunden.hasaktion=0 ";
				}
				if (termin) {
					whereClause += "AND kunden.hasgespraech=1 ";
				} else {
					whereClause += "AND kunden.hasgespraech=0 ";
				}
			}
			if (this.shfFlaeg > 0) {
				// contacts that can hf
				whereClause += "AND kunden.shfflag=" + Integer.toString(this.shfFlaeg) + " "; // use
			}
			whereClause += "ORDER BY kunden.stadt, kunden.strasse, kunden.hausnummer, kunden.nachname";
			String fields = "kunden.id";

			int countRequests = 0;
			int start = 0;
			int limit = this.howManyContacts * 2;

			lastSqlUsed = "SELECT " + fields + " FROM " + tables + whereClause + " LIMIT " + limit + "," + start;
			rs = Database.selectWithLimits(fields, tables, whereClause, limit, start);

			int loopCount = 0;
			int addedCount = 0;
			while (rs.next() && addedCount < this.howManyContacts) {
				loopCount++;
				// System.out.println(count);
				Contact c = new Contact(rs, false);
				// if termin is false. we excluded all gespräche with
				// hasgespraech=0 !!
				boolean addMe = false;
				// consider termin & auftrag
				if (!this.existsContact(c)) {
					if (!noAktions || !bigExcelExport) {
						// TODO: check if this is a nice way. or if we can do it
						// better
						addMe = termin && c.hasAuftrag() == auftrag && c.hasTermin() == termin;
						// if not yet added try more stuff
						if (!addMe) {
							// consider only aktions with aktions from group 2
							int a = c.onlyGroupTwoAktions();

							// check how many aktion we search
							// "1", "Bisher ohne Kontakt"
							// "2", "Bisher einmal Kontakt"
							// "3", "Bereits mehrmals Kontakt"
							// "4", "Erstkontakte oder einmal Kontakt"
							// "5", "Erstkontakte oder mehrmals Kontakt"
							if (whichKindOfContacts.equals("1") && a == 0) {
								addMe = true;
							} else if (whichKindOfContacts.equals("2") && a == 1) {
								addMe = true;
							} else if (whichKindOfContacts.equals("3") && a >= 1) {
								addMe = true;
							} else if (whichKindOfContacts.equals("4") && (a == 0 || a == 1)) {
								addMe = true;
							} else if (whichKindOfContacts.equals("5") && a >= 0) {
								addMe = true;
							}

							if (!addMe) {
								MyLog.logDebug("nouse of " + c.getId() + " because count of actions doesnt match filter");
							}
						} else {
							MyLog.logDebug("nouse of " + c.getId() + " because termin or auftrag doesnt match");
						}
						// donno how to do this in sql, so do it the lame way
						if (priorDate != null) {
							addMe = c.hasOnlyAktionenPrior(priorDate);
							if (!addMe) {
								MyLog.logDebug("nouse of " + c.getId() + " because action date filter doesnt match");
							}
						}
						// test for produkte if choosen
						if (addMe) {
							if (termin && this.terminProduct != null && this.terminProduct.length() > 0) {
								addMe = false;
								for (Gespraech g : c.getTermine()) {
									if (g.getProdukt().equals(this.terminProduct)) {
										addMe = true;
									}
								}
							}
							if (!addMe) {
								MyLog.logDebug("nouse of " + c.getId() + " because termin-product doesnt match");
							}
							if (auftrag && this.auftragProduct != null && this.auftragProduct.length() > 0) {
								addMe = false;
								for (Gespraech g : c.getGespraeche()) {
									if (g.getProdukt().equals(this.auftragProduct)) {
										addMe = true;
									}
								}
							}
							if (!addMe) {
								MyLog.logDebug("nouse of " + c.getId() + " because auftrag-product doesnt match");
							}
						}
					} else {
						addMe = true;
					}
				}
				if (addMe) {
					this.contacts.add(c);
					// System.out.println("found contact: "+c.getVorName());
					addedCount++;
					if (this.produceOutput) {
						MyLog.logDebug(addedCount + ":" + notUsed + "/" + howManyContacts + " found contact: " + c);
					}
				} else {
					MyLog.logDebug("nouse of " + c.getId() + " donno why ");
					notUsed++;
				}
				// if cancel is pressed in progressdialog
				if (pm.isCanceled()) {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Es wurden " + addedCount + " Kontakte gesammelt");
					break;
				}
				// if we have gone to the limit but didnt found something, send
				// database request again
				if (loopCount == this.howManyContacts && addedCount < this.howManyContacts) {
					countRequests++;
					loopCount = 0;
					start = countRequests * limit;
					MyLog.logDebug("request another package from " + start);
					rs = Database.selectWithLimits(fields, tables, whereClause, limit, start);
					lastSqlUsed += "\nSELECT " + fields + " FROM " + tables + whereClause + " LIMIT " + limit + "," + start;
				}

				// Set new state
				pm.setProgress(addedCount);
				// Change the note if desired
				String state = "Gefundene Kontakte: " + addedCount;
				pm.setNote(state);
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} finally {
			Database.close(rs);
		}
		this.getFirstContact();
		this.aktContactIndex = 0;
		pm.close();
		this.parentWindow.setDefaultCursor();
	}

	/**
	 * this is the constructor for just an empty bundle to collect somecontacts
	 * externally
	 */
	public ContactBundle() {
		this.contacts = new Vector<Contact>();
		this.aktContactIndex = 0;
	}

	// ==========================================================
	// =================== methods ==============================
	// ==========================================================

	public String toString() {
		return Integer.toString(this.contactCount());
	}

	@Override
	public Long doInBackground() {
		switch (WHATTODO) {
		case MFASSICOLLECT:
			collectMafoContacts();
			break;
		case ADMINCOLLECT:
			collectAdminContacts();
			break;

		default:
			break;
		}
		return 1L;
	}

	public void collectMafoContacts() {
		this.parentWindow.startStatusProgress();
		this.contactPanel.enableActionButtons(false);
		this.contacts = new Vector<Contact>();
		try {
			String whereClause = "WHERE bearbeitungsstatus=1 AND marktforscher=" + this.mafo.getId();
			whereClause += " ORDER BY stadt, strasse, hausnummer, nachname, vorname";
			ResultSet rs = Database.select("*", "kunden", whereClause);
			int i = 0;
			while (rs.next()) {
				Contact c = new Contact(rs);
				// System.out.println(i+":"+c);
				this.contacts.add(c);
				this.contactPanel.showContact(c);
				this.parentWindow.setStatusText("Adresse geladen: " + c);
				MyLog.logDebug("Adresse geladen:" + c);
				i++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		this.getFirstContact();
		this.aktContactIndex = 0;
		this.parentWindow.stopStatusProgress();

		this.parentWindow.setStatusText("Adressen fertig geladen");
		this.contactPanel.showContact(this.getFirstContact());
		this.contactPanel.enableActionButtons(true);

		if (this.contactCount() <= 0) {
			this.contactPanel.noContactsFound();
			JOptionPane.showMessageDialog(this.contactPanel, "Keine Adressen gefunden");
		} else {
			// restore unsaved aktions
			int restored = this.restoreDiskCache();
			this.contactPanel.updateActions(this.contactPanel.getVisibleContact());
			if (restored > 0) {
				JOptionPane.showMessageDialog(this.contactPanel, "Es wurden " + restored + " Aktion(en) wiederhegestellt.");
				this.contactPanel.enableSaveButton(true);
			}
		}
	}

	/**
	 * this merges this and the given bundle
	 * 
	 * @param cb
	 *            the bundle to merge into this
	 */
	public void addBundle(ContactBundle cb) {
		for (Iterator<Contact> iter = cb.getContacts().iterator(); iter.hasNext();) {
			Contact c = iter.next();
			this.addContact(c);
		}
	}

	/**
	 * this merges this and the given bundle
	 * 
	 * @param cb
	 *            the bundle to merge into this
	 */
	public void addBundleNoDoubles(ContactBundle cb) {
		for (Iterator<Contact> iter = cb.getContacts().iterator(); iter.hasNext();) {
			Contact c = iter.next();
			this.addContactNotTwice(c);
		}
	}

	/**
	 * this adds a contact
	 * 
	 * @param c
	 *            the contact to add
	 */
	public void addContact(Contact c) {
		this.contacts.add(c);
	}

	/**
	 * this adds a contact if it is not already in bundle
	 * 
	 * @param c
	 *            the contact to add
	 */
	public void addContactNotTwice(Contact c) {
		boolean found = false;
		for (Iterator<Contact> iter = this.contacts.iterator(); iter.hasNext() && !found;) {
			Contact in = iter.next();
			if (in.getId().equals(c.getId())) {
				found = true;
			}
		}
		if (!found) {
			this.contacts.add(c);
		}
	}

	/**
	 * this removes a contact
	 * 
	 * @param c
	 *            the contact to remove
	 */
	public void removeContact(Contact c) {
		if (this.contacts.indexOf(c) < this.aktContactIndex) {
			this.aktContactIndex--;
		}
		this.contacts.remove(c);
	}

	public void removeContacts(Vector<Contact> c) {
		for (Contact contact : c) {
			this.removeContact(contact);
		}
	}

	/**
	 * is the given contact in this bundle?
	 * 
	 * @param c
	 *            the contact to test
	 * @return is the contact in this bundle
	 */
	public boolean existsContact(Contact c) {
		return this.getContact(c) != null;
	}

	/**
	 * get list of contact with changed flag true
	 * 
	 * @return changed contacts
	 */
	public Vector<Contact> getChangedContacts() {
		Vector<Contact> ret = new Vector<Contact>();
		for (Contact c : this.contacts) {
			if (c.isChanged()) {
				ret.add(c);
			}
		}
		return ret;
	}

	/**
	 * get list of contact with changed flag false
	 * 
	 * @return changed contacts
	 */
	public Vector<Contact> getUnchangedContacts() {
		Vector<Contact> ret = new Vector<Contact>();
		for (Iterator<Contact> iter = this.contacts.iterator(); iter.hasNext();) {
			Contact c = iter.next();
			if (!c.isChanged()) {
				ret.add(c);
			}
		}
		return ret;
	}

	public Vector<Contact> getReadyContacts() {
		Vector<Contact> changed = this.getChangedContacts();
		Vector<Contact> ready = new Vector<Contact>();
		for (Contact c : changed) {
			if (c.hasFinishingAktionOnDisplay()) {
				ready.add(c);
			}
		}
		return ready;
	}

	/**
	 * are there changed contact in this bundle?
	 * 
	 * @return are there changed contact in this bundle?
	 */
	public boolean hasChangedContacts() {
		return getChangedContactCount() > 0;
	}

	/**
	 * are there changed contact in this bundle?
	 * 
	 * @return are there changed contact in this bundle?
	 */
	public int getChangedContactCount() {
		return this.getChangedContacts().size();
	}

	public int getContactCount() {
		return this.contacts.size();
	}

	/**
	 * this is to pack contactbundle into xml
	 * 
	 * @return xml string representing this contactbundle
	 */
	public String toXMLString() {
		StringBuffer ret = new StringBuffer();
		ret.append("<kontaktgruppe>\n");
		for (Iterator<Contact> iter = this.contacts.iterator(); iter.hasNext();) {
			Contact c = iter.next();
			ret.append(c.toXMLString());
		}
		ret.append("</kontaktgruppe>\n");
		return ret.toString();
	}

	/**
	 * set the bearbeitungsstatus of contact to mafo so it is clear that this
	 * contact cant be used furthermore
	 */
	public int setStatusToMaFoAndMAfo(Marktforscher mafo) {
		int id = -1;
		String ids = this.idsString();
		if (ids.length() > 0) {
			// insert tuple into bereitgestellt relation
			id = Database.quickInsert("bereitgestellt", "NULL, NOW(), " + mafo.getId() + ", " + this.contactCount() + ", 'normal', '<"
					+ ids + ">'");

			// mark kunden
			Database.update("kunden", "bereitgestellt=NOW(), bearbeitungsstatus=" + Contact.STATE_WAITING + ", marktforscher="
					+ mafo.getId() + ", lastbereitID=" + id, "WHERE id IN (" + ids + ")");
		}
		return id;
	}

	/**
	 * make a csv string of all ids in this bundle, for better sql statements...
	 * 
	 * @return
	 */
	public String idsString() {
		String ret = "";
		StringBuffer ids = new StringBuffer();
		for (Contact c : this.contacts) {
			ids.append(c.getId());
			ids.append(",");
		}
		ret = ids.toString();
		if (ret.length() > 0 && ret.endsWith(",")) {
			ret = ret.substring(0, ids.length() - 1);
		}
		return ret;
	}

	public boolean isCurrentFirst() {
		return this.aktContactIndex == 0;
	}

	public boolean isCurrentLast() {
		return this.aktContactIndex == this.contacts.size() - 1;
	}

	/**
	 * set the bearbeitungsstatus of contact to mafo so it is clear that this
	 * contact cant be used furthermore
	 */
	public void setStatusAtMaFo() {
		this.setStatus(Integer.toString(Contact.STATE_WORKING));
	}

	/**
	 * set the bearbeitungsstatus of contact to mafo so it is clear that this
	 * contact cant be used furthermore
	 */
	public void setStatusToWaiting() {
		this.setStatus(Integer.toString(Contact.STATE_WAITING));
	}

	/**
	 * set the bearbeitungsstatus of contact to mafo so it is clear that this
	 * contact cant be used furthermore
	 */
	public void setStatusToBill() {
		this.setStatus(Integer.toString(Contact.STATE_FINISHED));
	}

	/**
	 * set the bearbeitungsstatus of contact to temp while program is running,
	 * so it is not selected twice
	 */
	public void setStatusToTemp() {
		this.setStatus(Integer.toString(Contact.STATE_TEMP));
	}

	/**
	 * set the bearbeitungsstatus of contact to free, so it can be used again
	 */
	public void setStatusToFree() {
		String ids = this.idsString();
		if (ids.length() > 0) {
			DBTools.setContactsFree(ids);
		}
	}

	/**
	 * set the bearbeitungsstatus of contact to free and actions to billed
	 */
	public void setStatusToBilled(Marktforscher mafo, int abrechnungsWoche) {
		if (this.contactCount() > 0) {
			String ids = this.idsString();
			if (ids.length() > 0) {
				DBTools.setContactsFree(ids);
			}
			// collect all actions and set "abgerechnet"-field to
			// abrechnungswoche
			Database.update("aktionen", "abgerechnet=" + abrechnungsWoche, "WHERE marktforscher=" + mafo.getId()
					+ " AND abgerechnet=0 AND kunde IN (" + ids + ")");
		}
	}

	/**
	 * set the bearbeitungsstatus of contact to given string, so it can be used
	 * again
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		String ids = this.idsString();
		if (ids.length() > 0) {
			Database.update("kunden", "bearbeitungsstatus=" + status, "WHERE id IN (" + ids + ")");
		}
	}

	public void updateContacts() {
		for (Contact c : this.contacts) {
			c.update();
		}
	}

	/**
	 * @return next contact in list
	 */
	public Contact getNextContact() {
		Contact c = null;
		if (this.contacts.size() > 0 && this.aktContactIndex < this.contacts.size() - 1) {
			this.aktContactIndex++;
			c = this.contacts.get(this.aktContactIndex);
		}
		// System.out.println(c);
		return c;
	}

	/**
	 * get next contact in list with given count of aktions
	 * 
	 * @param aktions
	 *            type of aktion this contact has
	 * @return next contact in list with given count of aktions
	 */
	public Contact getNextContact(int aktions) {
		Contact old = this.getAktContact();
		Contact c = this.getNextContact();
		System.out.println("1:" + c);
		if (this.contacts.size() > 0 && this.aktContactIndex < this.contacts.size() - 1) {
			if (aktions > ANY) {
				// check for aktiontype
				if (aktions == WITHOUTFINISHINGACTIONS) {
					while (c != null && c.hasFinishingAktionOnDisplay()) {
						c = this.getNextContact();
					}
				} else if (aktions == WITHFINISHINGACTIONS) {
					while (c != null && !c.hasFinishingAktionOnDisplay()) {
						c = this.getNextContact();
					}
					// } else if (aktions==WITHSOMEACTIONS){
					// while (c!=null && c.hasAktionen(true) &&
					// c.hasFinishingAktionOnDisplay()){
					// c = this.getNextContact();
					// }
				} else if (aktions == WITHNOTE) {
					while (c != null && !c.hasNotiz()) {
						c = this.getNextContact();
					}
				}
			}
		}
		// if end was reached go one step back
		if (c == null && this.aktContactIndex == this.contactCount() - 1 && aktions > ANY) {
			// c = this.getPrevContact();
		}
		// retest found contact
		if (c != null
				&& ((aktions == WITHOUTFINISHINGACTIONS && c.hasFinishingAktionOnDisplay())
						|| (aktions == WITHFINISHINGACTIONS && !c.hasFinishingAktionOnDisplay()) || (aktions == WITHNOTE && !c.hasNotiz()))) {
			c = this.getPrevContact(aktions);
			System.out.println("2:" + c);
		}
		System.out.println(old + "3:" + c);
		return c;
	}

	/**
	 * @return previous contact in list
	 */
	public Contact getPrevContact() {
		Contact c = null;
		if (this.contacts.size() > 0 && this.aktContactIndex > 0) {
			this.aktContactIndex--;
			c = this.contacts.get(this.aktContactIndex);
		}
		// System.out.println(c);
		return c;
	}

	/**
	 * get previous contact in list with given count of aktions
	 * 
	 * @param aktions
	 *            type of aktion this contact has
	 * @return previous contact in list with given count of aktions
	 */
	public Contact getPrevContact(int aktions) {
		Contact c = this.getPrevContact();

		if (this.contacts.size() > 0 && this.aktContactIndex > 0) {
			if (aktions > ANY) {
				// check for aktiontype
				if (aktions == WITHOUTFINISHINGACTIONS) {
					while (c != null && c.hasFinishingAktionOnDisplay()) {
						c = this.getPrevContact();
					}
				} else if (aktions == WITHFINISHINGACTIONS) {
					while (c != null && !c.hasFinishingAktionOnDisplay()) {
						c = this.getPrevContact();
					}
					// } else if (aktions==WITHSOMEACTIONS){
					// while (c!=null && c.hasAktionen(true) &&
					// c.hasFinishingAktionOnDisplay()){
					// c = this.getPrevContact();
					// }
				} else if (aktions == WITHNOTE) {
					while (c != null && !c.hasNotiz()) {
						c = this.getPrevContact();
					}
				}
			}
		}
		// if end was reached go one step back
		if (c == null && this.aktContactIndex == 0 && aktions > ANY) {
			// c = this.getNextContact();
		}
		// retest found contact
		if (c != null
				&& ((aktions == WITHOUTFINISHINGACTIONS && c.hasFinishingAktionOnDisplay())
						|| (aktions == WITHFINISHINGACTIONS && !c.hasFinishingAktionOnDisplay()) || (aktions == WITHNOTE && !c.hasNotiz()))) {
			c = this.getNextContact(aktions);
		}
		// System.out.println(c);
		return c;
	}

	/**
	 * @return first contact in list
	 */
	public Contact getFirstContact() {
		Contact c = null;
		if (this.contacts.size() > 0) {
			c = this.contacts.firstElement();
			this.aktContactIndex = 0;
		}
		// System.out.println(c);
		return c;
	}

	/**
	 * @param aktions
	 *            type of aktion this contact has
	 * @return first contact in list with given aktionscount
	 */
	public Contact getFirstContact(int aktions) {
		Contact c = this.getFirstContact();
		if (c != null
				&& ((aktions == WITHFINISHINGACTIONS && !c.hasFinishingAktionOnDisplay())
						|| (aktions == WITHOUTFINISHINGACTIONS && c.hasFinishingAktionOnDisplay()) || (aktions == WITHNOTE && !c.hasNotiz()))) {
			c = this.getNextContact(aktions);
		}
		return c;
	}

	/**
	 * @return last contact in list
	 */
	public Contact getLastContact() {
		Contact c = null;
		if (this.contacts.size() > 0) {
			c = this.contacts.lastElement();
			this.aktContactIndex = this.contacts.size() - 1;
		}
		// System.out.println(c);
		return c;
	}

	/**
	 * @return previous contact in list
	 */
	public Contact setAktContact(int idx) {
		Contact c = null;
		if (this.contacts.size() > 0) {
			if (idx >= 0 && idx < this.contacts.size()) {
				this.aktContactIndex = idx;
				c = this.contacts.get(this.aktContactIndex);
			} else {
				this.aktContactIndex = 0;
				c = this.contacts.get(this.aktContactIndex);
			}
		}
		// System.out.println(c);
		return c;
	}

	public int setAktContact(Contact cin) {
		int i = -1;
		if (cin != null) {
			for (Contact c : this.contacts) {
				i++;
				if (c.equals(cin)) {
					this.setAktContact(i);
				}
			}
		}
		// System.out.println(c);
		return i;
	}

	/**
	 * @param aktions
	 *            type of aktion this contact has
	 * @return last contact in list with given aktionscount
	 */
	public Contact getLastContact(int aktions) {
		Contact c = this.getLastContact();
		if (c != null
				&& ((aktions == WITHFINISHINGACTIONS && !c.hasFinishingAktionOnDisplay())
						|| (aktions == WITHOUTFINISHINGACTIONS && c.hasFinishingAktionOnDisplay()) || (aktions == WITHNOTE && !c.hasNotiz()))) {
			c = this.getPrevContact(aktions);
		}
		return c;
	}

	public Contact getAktContact() {
		Contact c = null;
		if (this.contacts.size() > 0 && this.aktContactIndex < this.contacts.size()) {
			c = this.contacts.get(this.aktContactIndex);
		}
		return c;
	}

	/**
	 * search a contact in this bundle
	 * 
	 * @param c
	 *            the contact to search
	 * @return the object from bundle (are not the same objects!!, but have the
	 *         same id!!)
	 */
	public Contact getContact(Contact c) {
		Contact ret = null;
		for (Iterator<Contact> iter = this.contacts.iterator(); iter.hasNext() && ret == null;) {
			Contact cbc = iter.next();
			if (cbc.equals(c)) {
				ret = cbc;
			}
		}
		return ret;
	}

	/**
	 * show contacts from bundle
	 */
	public void show(JFrame frame) {
		JList cbList = new JList(this.contacts);
		JScrollPane jsp = new JScrollPane(cbList);
		jsp.setPreferredSize(new Dimension(100, 400));
		JOptionPane pane = new JOptionPane(jsp);
		JDialog dialog = pane.createDialog(frame, "Liste der Kontakte");
		dialog.setVisible(true);
	}

	public int restoreDiskCache() {
		MyLog.logDebug("start restore actions");
		int ret = 0;
		Vector<String> data = Contact.readDiskCache();
		for (Contact c : this.contacts) {
			String removeMe = null;
			for (String str : data) {
				Aktion in = Aktion.Deserialize(str);
				if (in.getContact().equals(c.getId())) {
					Aktion out = new Aktion(c, this.mafo, in.getErgebnis(), Aktion.TELEFON, in.getAngelegt(), in.getAngelegtZeit(), true);
					c.addAktion(out, false);
					ret++;
					removeMe = str;
				}
			}
			if (ret > 0) {
				data.remove(removeMe);
			}
		}
		MyLog.logDebug("done restore actions");
		return ret;
	}

	// ==========================================================
	// ============= getters and setters ========================
	// ==========================================================

	public int contactCount() {
		return this.contacts.size();
	}

	public boolean isAuftrag() {
		return auftrag;
	}

	public Vector<Contact> getContacts() {
		return contacts;
	}

	public String getPlz() {
		return plz;
	}

	public String getStadt() {
		return stadt;
	}

	public boolean isTermin() {
		return termin;
	}

	public String getType() {
		return type.getValue();
	}

	public String getTypeID() {
		return type.getKey0();
	}

	public int getAktContactIndex() {
		return aktContactIndex;
	}

	public static int getSEARCHLIMIT() {
		return SEARCHLIMIT;
	}

	public void setBigExcelExport(boolean bigExcelExport) {
		this.bigExcelExport = bigExcelExport;
	}

	public boolean isBigExcelExport() {
		return bigExcelExport;
	}

	public String getLastSqlUsed() {
		return lastSqlUsed;
	}

	public void setLastSqlUsed(String lastSqlUsed) {
		this.lastSqlUsed = lastSqlUsed;
	}
}
