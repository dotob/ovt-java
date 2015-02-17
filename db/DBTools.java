package db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComboBox;

import tools.ListItem;
import tools.MyLog;

public class DBTools {

	public static final int SHF_ALL = 1;
	public static final int SHF_FH = 2;
	public static final int SHF_SOLAR = 3;

	private static HashMap<Integer, Integer> termErgErfolgList;
	private static HashMap<Integer, String> shfList;
	private static HashMap<String, Integer> terminErg2Typ;
	private static HashMap<String, String> grp2Name;
	private static HashMap<String, String> hfErg2Name;
	private static HashMap<String, String> hfKurzErg2Name;
	private static HashMap<String, String> mafoID2Name;
	private static HashMap<String, String> mdeID2Name;
	private static HashMap<String, String> PLZ2Region;
	private static HashMap<String, String> region2Name;
	private static HashMap<String, String> solarErg2Name;
	private static HashMap<String, String> terminErg2KurzName;
	private static HashMap<String, String> terminErg2Name;
	private static HashMap<String, String> typ2Name;
	private static HashMap<String, String> wlID2Name;
	private static Vector<ListItem> bearbStatusList;
	private static Vector<ListItem> hfErgAllList;
	private static Vector<ListItem> hfErgList;
	private static Vector<ListItem> prodList;
	private static Vector<ListItem> shfflags;
	private static Vector<ListItem> regions;
	private static Vector<ListItem> solarErgAllList;
	private static Vector<ListItem> solarErgList;
	private static Vector<ListItem> termErgList;
	private static Vector<ListItem> wdhList;

	/**
	 * this gets the marktforscher from db and adds them to a vector
	 * 
	 * @return vector of marktforscher
	 */
	public static Vector<ListItem> mafoList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		try {
			ResultSet rs = Database.select("*", "marktforscher",
					"WHERE aktiv=1 ORDER BY nachname");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("nachname")
						+ ", " + rs.getString("vorname"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the marktforscher from db and adds them to a vector
	 * 
	 * @return vector of marktforscher
	 */
	public static Vector<ListItem> completeMafoListWithNew() {
		Vector<ListItem> ret = new Vector<ListItem>();
		try {
			ResultSet rs = Database.select("*", "marktforscher",
					"ORDER BY aktiv DESC, nachname");
			while (rs.next()) {
				String aktiv = "";
				if (!rs.getBoolean("aktiv")) {
					aktiv = "inaktv: ";
				}
				ListItem app = new ListItem(rs.getString("id"), aktiv
						+ rs.getString("nachname") + ", "
						+ rs.getString("vorname"));
				ret.add(app);
			}
			Database.close(rs);
			ret.add(new ListItem("-1", "<Neuen Mafo anlegen>"));
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the marktdatenermittler from db and adds them to a vector
	 * 
	 * @return vector of marktdatenermittler
	 */
	public static Vector<ListItem> marktdatenermittlerList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		ret.add(new ListItem("", ""));
		try {
			ResultSet rs = Database.select("*", "marktdatenermittler",
					"ORDER BY nachname");
			while (rs.next()) {
				// ListItem app = new ListItem(rs.getString("id"),
				// rs.getString("nachname")+", "+rs.getString("vorname"));
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("nachname"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the marktforscher from db and adds them to a vector
	 * 
	 * @return vector of marktforscher
	 */
	public static Vector<Marktforscher> mafoMafoList(boolean withEmptyLine) {
		Vector<Marktforscher> ret = new Vector<Marktforscher>();
		if (withEmptyLine) {
			ret.add(null);
		}
		try {
			ResultSet rs = Database.select("*", "marktforscher",
					"WHERE aktiv=1 AND nachname IS NOT NULL ORDER BY nachname");
			while (rs.next()) {
				Marktforscher app = new Marktforscher(rs);
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the werbeleiter from db and adds them to a vector
	 * 
	 * @return vector of werbeleiter
	 */
	public static Vector<ListItem> wlList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		try {
			ResultSet rs = Database.select("*", "werbeleiter",
					"WHERE aktiv=1 ORDER BY nachname");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("nachname")
						+ ", " + rs.getString("vorname"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the werbeleiter from db and adds them to a vector
	 * 
	 * @return vector of werbeleiter
	 */
	public static Vector<Projektleiter> weleWeleList(boolean withEmptyLine) {
		Vector<Projektleiter> ret = new Vector<Projektleiter>();
		if (withEmptyLine) {
			ret.add(null);
		}
		try {
			ResultSet rs = Database.select("*", "werbeleiter",
					"WHERE aktiv=1 ORDER BY nachname");
			while (rs.next()) {
				Projektleiter app = new Projektleiter(rs);
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available colors from db and adds them to a vector
	 * 
	 * @return vector of colors
	 */
	public static Vector<ListItem> colorList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		ret.add(new ListItem("0", "keine"));
		try {
			ResultSet rs = Database.select("*", "farben", "ORDER BY id");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available heizung from db and adds them to a vector
	 * 
	 * @return vector of heizung
	 */
	public static Vector<ListItem> heizungList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		ret.add(new ListItem("0", "keine"));
		try {
			ResultSet rs = Database.select("*", "heizung", "ORDER BY id");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available heizung from db and adds them to a vector
	 * 
	 * @return vector of heizung
	 */
	public static Vector<ListItem> solarList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		ret.add(new ListItem("0", "keine"));
		try {
			ResultSet rs = Database.select("*", "solar", "ORDER BY id");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available terminergebnisse from db and adds them to a
	 * vector
	 * 
	 * @return vector of terminergebnisse
	 */
	public static Vector<ListItem> terminErgebnisListKomplett() {
		Vector<ListItem> ret = new Vector<ListItem>();
		try {
			ResultSet rs = Database.select("*", "terminergebnisse",
					"ORDER BY name");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available terminergebnisse from db and adds them to a
	 * vector
	 * 
	 * @return vector of terminergebnisse
	 */
	public static Vector<ListItem> terminErgebnisList(boolean withEmptyLine) {
		Vector<ListItem> ret = new Vector<ListItem>();
		if (withEmptyLine) {
			ret.add(new ListItem("", ""));
		}
		try {
			ResultSet rs = Database.select("*", "terminergebnisse",
					"WHERE erfolg=1 ORDER BY name");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available terminergebnisse from db and adds them to a
	 * vector
	 * 
	 * @return vector of terminergebnisse
	 */
	public static Vector<ListItem> noTerminErgebnisList(boolean withEmptyLine) {
		Vector<ListItem> ret = new Vector<ListItem>();
		if (withEmptyLine) {
			ret.add(new ListItem("", ""));
		}
		try {
			ResultSet rs = Database.select("*", "terminergebnisse",
					"WHERE erfolg=0 ORDER BY name");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * this gets the available fassadenarten from db and adds them to a vector
	 * 
	 * @return vector of fassadenarten
	 */
	public static Vector<ListItem> fassadenArtList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		ret.add(new ListItem("0", ""));
		try {
			ResultSet rs = Database.select("*", "fassadenart", "ORDER BY id");
			while (rs.next()) {
				ListItem app = new ListItem(rs.getString("id"), rs
						.getString("name"));
				ret.add(app);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * build list with all ergebnisvalues
	 * 
	 * @param withEmptyEntry
	 *            TODO
	 * 
	 * @return the list of ergebnisse (results)
	 */
	public static Vector<ListItem> buildAktionsErgebnisList(boolean solar,
			boolean withEmptyEntry) {
		Vector<ListItem> ret = null;

		if (solar) {
			solarErgList = new Vector<ListItem>();
			if (withEmptyEntry) {
				solarErgList.add(new ListItem("-", ""));
			}
			try {
				// do not use mailing and special erg group
				ResultSet rs = Database
						.select("*", "ergebnisse",
								"WHERE gruppe AND showinmafo=1 NOT IN (5,99) ORDER BY hkeit");
				while (rs.next()) {
					String kn = rs.getString("kurzname");
					String mainString = "<html><b>" + rs.getString("nameSolar")
							+ "</b>";
					if (kn.length() > 0) {
						mainString = "<html><b>" + kn + "</b>, "
								+ rs.getString("nameSolar") + "</html>";
					} else {
						kn = rs.getString("nameSolar");
					}
					solarErgList.add(new ListItem(rs.getString("id"), kn,
							mainString));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			ret = solarErgList;
		} else {
			hfErgList = new Vector<ListItem>();
			if (withEmptyEntry) {
				hfErgList.add(new ListItem("-", ""));
			}
			try {
				ResultSet rs = Database.select("*", "ergebnisse",
						"WHERE gruppe!=99 AND showinmafo=1 ORDER BY hkeit");
				while (rs.next()) {
					String kn = rs.getString("kurzname");
					String mainString = "<html><b>" + rs.getString("name")
							+ "</b>";
					if (kn.length() > 0) {
						mainString = "<html><b>" + kn + "</b>, "
								+ rs.getString("name") + "</html>";
					} else {
						kn = rs.getString("name");
					}
					hfErgList.add(new ListItem(rs.getString("id"), kn,
							mainString));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			ret = hfErgList;
		}
		return ret;
	}

	/**
	 * build list with all ergebnisvalues
	 * 
	 * @return the list of ergebnisse (results)
	 */
	public static Vector<ListItem> buildAktionsErgebnisListAll(boolean solar) {
		Vector<ListItem> ret = null;
		if (solar) {
			if (solarErgAllList == null) {
				solarErgAllList = new Vector<ListItem>();
				try {
					ResultSet rs = Database.select("*", "ergebnisse",
							"ORDER BY gruppe, nameSolar");
					while (rs.next()) {
						String kn = rs.getString("kurzname");
						String mainString = "<html><b>"
								+ rs.getString("nameSolar") + "</b>";
						if (kn.length() > 0) {
							mainString = "<html><b>" + kn + "</b>, "
									+ rs.getString("nameSolar") + "</html>";
						} else {
							kn = rs.getString("nameSolar");
						}
						solarErgAllList.add(new ListItem(rs.getString("id"),
								kn, mainString));
					}
					Database.close(rs);
				} catch (SQLException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
			ret = solarErgAllList;
		} else {
			if (hfErgAllList == null) {
				hfErgAllList = new Vector<ListItem>();
				try {
					ResultSet rs = Database.select("*", "ergebnisse",
							"ORDER BY gruppe, name");
					while (rs.next()) {
						String kn = rs.getString("kurzname");
						String mainString = "<html><b>" + rs.getString("name")
								+ "</b>";
						if (kn.length() > 0) {
							mainString = "<html><b>" + kn + "</b>, "
									+ rs.getString("name") + "</html>";
						} else {
							kn = rs.getString("name");
						}
						hfErgAllList.add(new ListItem(rs.getString("id"), kn,
								mainString));
					}
					Database.close(rs);
				} catch (SQLException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
			ret = hfErgAllList;
		}
		return ret;
	}

	/**
	 * build list with all ergebnisvalues
	 * 
	 * @return the list of ergebnisse (results)
	 */
	public static Vector<ListItem> buildTerminErgebnisList() {
		if (termErgList == null) {
			termErgList = new Vector<ListItem>();
			try {
				ResultSet rs = Database.select("*", "terminergebnisse",
						" ORDER BY erfolg, id");
				while (rs.next()) {
					String kn = rs.getString("kurzname");
					String mainString = "<html><b>" + rs.getString("name")
							+ "</b>";
					if (kn.length() > 0) {
						mainString = "<html><b>" + kn + "</b>, "
								+ rs.getString("name") + "</html>";
					} else {
						kn = rs.getString("name");
					}
					termErgList.add(new ListItem(rs.getString("id"), kn,
							mainString));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return termErgList;
	}

	public static int getErfolgOfTermErg(int erg) {
		if (termErgErfolgList == null) {
			termErgErfolgList = new HashMap<Integer, Integer>();
			try {
				ResultSet rs = Database.select("*", "terminergebnisse",
						" ORDER BY erfolg, id");
				while (rs.next()) {
					termErgErfolgList.put(rs.getInt("id"), rs.getInt("erfolg"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return termErgErfolgList.get(erg);
	}

	/**
	 * build list with all produkts
	 * 
	 * @return the list of produkts
	 */
	public static Vector<ListItem> buildProduktList(boolean withEmptyLine) {
		Vector<ListItem> ret = new Vector<ListItem>();
		if (withEmptyLine) {
			ret.add(new ListItem("", ""));
			ret.addAll(buildProduktList());
		} else {
			ret = buildProduktList();
		}
		return ret;
	}

	public static Vector<ListItem> buildProduktList() {
		if (prodList == null) {
			prodList = new Vector<ListItem>();
			try {
				ResultSet rs = Database
						.select("*", "produkte", "ORDER BY name");
				while (rs.next()) {
					prodList.add(new ListItem(rs.getString("id"), rs
							.getString("name")));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return prodList;
	}

	/**
	 * build list with all Auftragnehmer
	 * 
	 * @return the list of Auftragnehmer
	 */
	public static Vector<String> buildAuftragnehmerList() {
		Vector<String> anList = new Vector<String>();
		anList.add("");
		try {
			ResultSet rs = Database.select("DISTINCT auftragnehmer",
					"auftraege", "ORDER BY auftragnehmer");
			while (rs.next()) {
				String inh = rs.getString("auftragnehmer");
				if (inh != null && inh.trim().length() > 0) {
					anList.add(inh);
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return anList;
	}

	/**
	 * get the name of terminresulttype
	 * 
	 * @param typId
	 *            id of type
	 * @return name of type
	 */
	public static String nameOfTerminErgebnis(String ergId) {
		String ret = "";
		if (terminErg2Name == null) {
			// list doesnt exist, so make it
			terminErg2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, name", "terminergebnisse");
				while (rs.next()) {
					terminErg2Name
							.put(rs.getString("id"), rs.getString("name"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = terminErg2Name.get(ergId);
		return ret;
	}

	/**
	 * get the type of terminresult
	 * 
	 * @param typId
	 *            id of type
	 * @return name of type
	 */
	public static int typeOfTerminErgebnis(String ergId) {
		int ret = -1;
		if (terminErg2Typ == null) {
			// list doesnt exist, so make it
			terminErg2Typ = new HashMap<String, Integer>();
			ResultSet rs = null;
			try {
				rs = Database.select("id, erfolg", "terminergebnisse");
				while (rs.next()) {
					terminErg2Typ.put(rs.getString("id"), rs.getInt("erfolg"));
				}
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} finally {
				Database.close(rs);
			}
		}
		Object o = terminErg2Typ.get(ergId);
		if (o != null) {
			ret = (Integer) o;
		}
		return ret;
	}

	/**
	 * get the name of terminresulttype
	 * 
	 * @param typId
	 *            id of type
	 * @return name of type
	 */
	public static String kurzNameOfTerminErgebnis(String ergId) {
		String ret = "";
		if (terminErg2KurzName == null) {
			// list doesnt exist, so make it
			terminErg2KurzName = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, kurzname",
						"terminergebnisse");
				while (rs.next()) {
					terminErg2KurzName.put(rs.getString("id"), rs
							.getString("kurzname"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = terminErg2KurzName.get(ergId);
		return ret;
	}

	/**
	 * get the name of a resulttype
	 * 
	 * @param typId
	 *            id of type
	 * @return name of type
	 */
	public static String nameOfErgebnis(String ergId, boolean solar) {
		String ret = "";
		if (solar) {
			if (solarErg2Name == null) {
				// list doesnt exist, so make it
				solarErg2Name = new HashMap<String, String>();
				try {
					ResultSet rs = Database.select("id, nameSolar",
							"ergebnisse");
					while (rs.next()) {
						solarErg2Name.put(rs.getString("id"), rs
								.getString("nameSolar"));
					}
					Database.close(rs);
				} catch (SQLException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
			ret = solarErg2Name.get(ergId);
		} else {
			if (hfErg2Name == null) {
				// list doesnt exist, so make it
				hfErg2Name = new HashMap<String, String>();
				try {
					ResultSet rs = Database.select("id, name", "ergebnisse");
					while (rs.next()) {
						hfErg2Name
								.put(rs.getString("id"), rs.getString("name"));
					}
					Database.close(rs);
				} catch (SQLException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
			ret = hfErg2Name.get(ergId);
		}
		return ret;
	}

	/**
	 * get the name of a resultgroup
	 * 
	 * @param id
	 *            id of gruppe
	 * @return name of gruppe
	 */
	public static String nameOfGruppe(String ergId) {
		String ret = "";
		if (grp2Name == null) {
			// list doesnt exist, so make it
			grp2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("*", "ergebnisgruppen");
				while (rs.next()) {
					grp2Name.put(rs.getString("id"), rs.getString("name"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = grp2Name.get(ergId);
		return ret;
	}

	/**
	 * get the kurzname of a resulttype
	 * 
	 * @param typId
	 *            id of type
	 * @return name of type
	 */
	public static String kurznameOfErgebnis(String ergId, boolean solar) {
		String ret = "";
		if (hfKurzErg2Name == null) {
			// list doesnt exist, so make it
			hfKurzErg2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, kurzname", "ergebnisse");
				while (rs.next()) {
					hfKurzErg2Name.put(rs.getString("id"), rs
							.getString("kurzname"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = hfKurzErg2Name.get(ergId);
		return ret;
	}

	/**
	 * get the name of a actiontype
	 * 
	 * @param typId
	 *            id of type
	 * @return name of type
	 */
	public static String nameOfTyp(String typId) {
		String ret = "";
		if (typ2Name == null) {
			// list doesnt exist, so make it
			typ2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, name", "aktionstyp");
				while (rs.next()) {
					typ2Name.put(rs.getString("id"), rs.getString("name"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = typ2Name.get(typId);
		return ret;
	}

	/**
	 * helper method that gets a string from resultset but never return null
	 * 
	 * @param rs
	 *            the resultset data
	 * @param field
	 *            the name of the field to get
	 * @return the vlue of the field or "" but never null
	 */
	public static String myGetString(ResultSet rs, String field) {
		String ret = "";
		try {
			ret = rs.getString(field);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		if (ret == null) {
			ret = "";
		}
		return ret;
	}

	/**
	 * this should set a combobox list to a listitem
	 * 
	 * @param jcb
	 *            the combobox to select the item in
	 * @param li
	 *            the item that should be selected
	 * @return the index of the selected item
	 */
	public static int setList2ListItem(JComboBox jcb, ListItem li) {
		int ret = -1;
		Vector<ListItem> data = new Vector<ListItem>();
		for (int i = 0; i < jcb.getItemCount(); i++) {
			data.add((ListItem) jcb.getItemAt(i));
		}
		int i = 0;
		for (Iterator<ListItem> iter = data.iterator(); iter.hasNext();) {
			ListItem item = iter.next();
			if (item.getKey0().equals(li.getKey0())) {
				jcb.setSelectedItem(item);
				ret = i;
			}
			i++;
		}
		return ret;
	}

	public static int setList2Object(JComboBox jcb, Object li) {
		int ret = -1;
		if (li != null) {
			Vector data = new Vector();
			for (int i = 0; i < jcb.getItemCount(); i++) {
				data.add(jcb.getItemAt(i));
			}
			int i = 0;
			for (Iterator<Object> iter = data.iterator(); iter.hasNext();) {
				Object item = iter.next();
				if (item != null) {
					String s1 = item.toString();
					String s2 = li.toString();
					if (s1.equals(s2)) {
						jcb.setSelectedItem(item);
						ret = i;
					}
				}
				i++;
			}
		}
		return ret;
	}

	/**
	 * @param name
	 *            give a pattern to search for
	 * @return name of mde
	 */
	public static String idOfMDE(String name) {
		String ret = "";
		try {
			ResultSet rs = Database.select("id", "marktdatenermittler",
					"WHERE nachname LIKE '"
							+ name.replace('*', '%').replace('?', '_') + "'");
			while (rs.next()) {
				ret += rs.getString("id") + ",";
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		return ret.substring(0, ret.length() - 1);
	}

	/**
	 * @param id
	 *            id of mde to get
	 * @param komplett
	 *            should the namen given with pre and surname
	 * @return name of mde
	 */
	public static String nameOfMDE(String id, boolean komplett) {
		String ret = "";
		if (mdeID2Name == null) {
			// list doesnt exist, so make it
			mdeID2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, nachname, vorname",
						"marktdatenermittler");
				while (rs.next()) {
					String tmp = rs.getString("vorname");
					tmp = (tmp != null && tmp.length() > 0) ? rs
							.getString("nachname")
							+ ", " + tmp : rs.getString("nachname");
					mdeID2Name.put(rs.getString("id"), tmp);
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = mdeID2Name.get(id);
		if (!komplett && ret != null && ret.indexOf(",") >= 0) {
			ret = ret.substring(0, ret.indexOf(","));
		}
		return ret;
	}

	/**
	 * @param komplett
	 *            should the namen given with pre and surname
	 * @return name of mafo
	 */
	public static String nameOfMafo(String id, boolean komplett) {
		String ret = "";
		if (mafoID2Name == null) {
			// list doesnt exist, so make it
			mafoID2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, nachname, vorname",
						"marktforscher");
				while (rs.next()) {
					String tmp = rs.getString("vorname");
					tmp = (tmp != null && tmp.length() > 0) ? rs
							.getString("nachname")
							+ ", " + tmp : rs.getString("nachname");
					mafoID2Name.put(rs.getString("id"), tmp);
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = mafoID2Name.get(id);
		if (!komplett && ret != null && ret.indexOf(",") >= 0) {
			ret = ret.substring(0, ret.indexOf(","));
		}
		return ret;
	}

	/**
	 * @param komplett
	 *            should the namen given with pre and surname
	 * @return name of mde
	 */
	public static String nameOfWL(String id, boolean komplett) {
		String ret = "";
		if (wlID2Name == null) {
			// list doesnt exist, so make it
			wlID2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, nachname, vorname",
						"werbeleiter");
				while (rs.next()) {
					String tmp = rs.getString("vorname");
					tmp = (tmp != null && tmp.length() > 0) ? rs
							.getString("nachname")
							+ ", " + tmp : rs.getString("nachname");
					wlID2Name.put(rs.getString("id"), tmp);
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = wlID2Name.get(id);
		if (!komplett && ret != null && ret.indexOf(",") >= 0) {
			ret = ret.substring(0, ret.indexOf(","));
		}
		return ret;
	}

	/**
	 * @return list of regions
	 */
	public static Vector<ListItem> regionsList() {
		if (regions == null) {
			// list doesnt exist, so make it
			regions = new Vector<ListItem>();
			try {
				ResultSet rs = Database.select("id, name", "region",
						"ORDER BY id");
				while (rs.next()) {
					regions.add(new ListItem(rs.getString("id"), rs
							.getString("name")));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return regions;
	}

	/**
	 * @return list of shfflags
	 */
	public static Vector<ListItem> shfflagList() {
		if (shfflags == null) {
			// list doesnt exist, so make it
			shfflags = new Vector<ListItem>();
			shfflags.add(new ListItem("0", ""));
			try {
				ResultSet rs = Database.select("id, text", "shfflag",
						"ORDER BY id");
				while (rs.next()) {
					shfflags.add(new ListItem(rs.getString("id"), rs
							.getString("text")));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return shfflags;
	}

	/**
	 * @param id
	 *            of region
	 * @return name of region
	 */
	public static String nameOfRegion(String id) {
		String ret = "";
		if (region2Name == null) {
			// list doesnt exist, so make it
			region2Name = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("id, name", "region");
				while (rs.next()) {
					region2Name.put(rs.getString("id"), rs.getString("name"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = region2Name.get(id);
		return ret;
	}

	/**
	 * @param id
	 *            of shfflag
	 * @return name of shfflag
	 */
	public static String nameOfSHFFlag(int id) {
		String ret = "";
		if (shfList == null) {
			// list doesnt exist, so make it
			shfList = new HashMap<Integer, String>();
			try {
				ResultSet rs = Database.select("id, text", "shfflag");
				while (rs.next()) {
					shfList.put(rs.getInt("id"), rs.getString("text"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		ret = shfList.get(id);
		return ret;
	}

	/**
	 * get the region and werbeleiter of plz
	 * 
	 * @param plzIn
	 * @return
	 */
	public static String plzBereich(String plzIn) {
		String ret = "";
		// get values from db
		if (PLZ2Region == null) {
			// list doesnt exist, so make it
			PLZ2Region = new HashMap<String, String>();
			try {
				ResultSet rs = Database.select("*", "plzbereiche",
						"ORDER BY LENGTH(plz) DESC");
				while (rs.next()) {
					String tmp = rs.getString("region") + ":"
							+ rs.getString("werbeleiter");
					PLZ2Region.put(rs.getString("plz"), tmp);
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		// check for plz
		Set<String> plzs = PLZ2Region.keySet();
		for (Iterator<String> iter = plzs.iterator(); iter.hasNext();) {
			String p = iter.next();
			if (plzIn.startsWith(p)) {
				ret = PLZ2Region.get(p);
			}
		}
		return ret;
	}

	public static String regionOfPLZ(String plz) {
		String tmp = plzBereich(plz);
		System.out.println(tmp);
		if (tmp.length() > 0) {
			tmp = tmp.substring(0, tmp.indexOf(":"));
		}
		return tmp;
	}

	public static String wlOfPLZ(String plz) {
		String tmp = plzBereich(plz);
		if (tmp.length() > 0) {
			tmp = tmp.substring(tmp.indexOf(":") + 1, tmp.length());
		}
		return tmp;
	}

	/**
	 * this sets contacts back to free state, and saves the notes they got in a
	 * different table
	 * 
	 * @param ids
	 *            of contacts to set free
	 */
	public static void setContactsFree(String ids) {
		if (ids != null && ids.length() > 0) {
			try {
				ResultSet rs = Database.select("id, notiz, marktforscher",
						"kunden", "WHERE id IN (" + ids + ")");
				while (rs.next()) {
					if (rs.getString("notiz") != null
							&& rs.getString("notiz").trim().length() > 0) {
						Date nowDate = new Date(new java.util.Date().getTime());
						PreparedStatement p1 = Database
								.getPreparedStatement("INSERT INTO notes "
										+ "(eingang, kunde, note, currentmafo) "
										+ "VALUES (?,?,?,?)");
						p1.setDate(1, nowDate);
						p1.setString(2, rs.getString("id"));
						p1.setString(3, rs.getString("notiz"));
						p1.setString(4, rs.getString("marktforscher"));
						p1.executeUpdate();
						Database.close(p1);
					}
				}
				Database.close(rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// do not reset the bearbeitungssstatus when the adress was dirty
			Database.update("kunden",
					"bereitgestellt=NULL, marktforscher=0, notiz='', "
							+ "bearbeitungsstatus=" + Contact.STATE_FREE,
					"WHERE id IN (" + ids + ") AND bearbeitungsstatus!=99");
			Database.update("kunden",
					"bereitgestellt=NULL, marktforscher=0, notiz=''",
					"WHERE id IN (" + ids + ") AND bearbeitungsstatus=99");
		}
	}

	public static Vector<ListItem> wiederErkennungsMerkmale() {
		if (wdhList == null) {
			// list doesnt exist, so make it
			wdhList = new Vector<ListItem>();
			wdhList.add(new ListItem("", ""));
			try {
				ResultSet rs = Database.select("DISTINCT(importtag)", "kunden",
						"ORDER BY importtag");
				while (rs.next()) {
					wdhList.add(new ListItem(rs.getString("importtag"), rs
							.getString("importtag")));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		Vector<ListItem> ret = wdhList;
		return ret;
	}

	public static Vector<ListItem> bearbStatus() {
		if (bearbStatusList == null) {
			// list doesnt exist, so make it
			bearbStatusList = new Vector<ListItem>();
			bearbStatusList.add(new ListItem("", ""));
			try {
				ResultSet rs = Database.select("*", "bearbeitungsstatus",
						"ORDER BY id");
				while (rs.next()) {
					bearbStatusList.add(new ListItem(rs.getString("id"), rs
							.getString("name")));
				}
				Database.close(rs);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		Vector<ListItem> ret = bearbStatusList;
		return ret;
	}
}
