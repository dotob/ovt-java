package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;

import statistics.StatisticErgebnisTable;
import tools.ListItem;
import tools.SettingsReader;
import tools.Table2Excel;

import com.toedter.calendar.JDateChooser;

import db.DBTools;
import db.Database;

public class ProjektmanagerSearchPanel extends JPanel implements ActionListener, Runnable {

	private IMainWindow parentWindow;
	private JTextField plzTextbox;
	private JTextField cityTextbox;
	private Vector<JComboBox> erglists;
	private String whatToDo;
	private ProgressMonitor pm;
	private int maxContacts = 500;
	private JComboBox includelist;
	private JDateChooser kontaktDate;
	private JCheckBox showID;
	private JCheckBox showVorname;
	private JCheckBox showTelefon;
	private JCheckBox showFEZahl;
	private JCheckBox showErgebnis;
	private JCheckBox showAngelegt;
	private JCheckBox showErgebnisID;
	private JCheckBox showAktionID;
	private JComboBox amountlist;

	public ProjektmanagerSearchPanel(IMainWindow parentWindow) {
		super();
		this.parentWindow = parentWindow;

		this.pm = new ProgressMonitor(this.parentWindow.getFrame(), "Kontakte sammeln", "", 0, 2 * this.maxContacts);
		this.pm.setMillisToPopup(50);
		this.pm.setMillisToDecideToPopup(50);
	}

	public JPanel buildPSPanel() {
		JPanel dataPanel = new JPanel(new BorderLayout());

		// show data
		GridLayout gridLayout = new GridLayout(0, 2);
		gridLayout.setVgap(2);
		gridLayout.setHgap(2);
		JPanel mfmPane = new JPanel(gridLayout);

		mfmPane.add(new JLabel("PLZ:"));
		plzTextbox = new JTextField();
		mfmPane.add(plzTextbox);

		mfmPane.add(new JLabel("Ort:"));
		cityTextbox = new JTextField();
		mfmPane.add(cityTextbox);

		erglists = new Vector<JComboBox>();
		Vector<ListItem> aktionsErgebnisList = DBTools.buildAktionsErgebnisList(false, true);
		aktionsErgebnisList.insertElementAt(new ListItem("3,6,9,10,11",
				"<html><b>Standard 3</b> (jetzt nicht, +1, +1-2, +1-5, verkauft)</html>"), 1);
		aktionsErgebnisList.insertElementAt(new ListItem("1,17,18,25,31",
				"<html><b>Standard 2</b> (Alter, Bekannte, Erben, Freiberufler, selbständig)</html>"), 1);
		aktionsErgebnisList.insertElementAt(new ListItem("3,6,7,9,10,11,14,15,34",
				"<html><b>Standard 1</b> (NE, jetzt nicht, +1, +1-2, +1-5, TN falsch, verzogen, verkauft, Wettbewerb)</html>"), 1);
		for (int i = 1; i <= 13; i++) {
			JComboBox e = new JComboBox(aktionsErgebnisList);
			mfmPane.add(new JLabel(Integer.toString(i) + ". Wahl Ergebnis:"));
			erglists.add(e);
			mfmPane.add(e);
		}

		mfmPane.add(new JLabel("Einschränkung:"));
		Vector<ListItem> v = new Vector<ListItem>();
		v.add(new ListItem("last", "Nur das zeitlich letzte der gewählten Ergebnisse"));
		v.add(new ListItem("all", "Alle gewählten Ergebnisse"));
		// v.add(new ListItem("before", "Nur Ergebnisse vor..."));
		// v.add(new ListItem("after", "Nur Ergebnisse nach..."));
		includelist = new JComboBox(v);
		// includelist.setEnabled(false);
		mfmPane.add(includelist);

		mfmPane.add(new JLabel("Datum:"));
		this.kontaktDate = new JDateChooser();
		this.kontaktDate.setEnabled(false);
		mfmPane.add(this.kontaktDate);

		mfmPane.add(new JLabel("Menge:"));
		v = new Vector<ListItem>();
		v.add(new ListItem("500", "500"));
		v.add(new ListItem("1000", "1000"));
		v.add(new ListItem("1500", "1500"));
		v.add(new ListItem("2000", "2000"));
		amountlist = new JComboBox(v);
		mfmPane.add(amountlist);

		this.showID = new JCheckBox("Zeige Schlüsselnr.");
		mfmPane.add(showID);
		this.showVorname = new JCheckBox("Zeige Vorname");
		mfmPane.add(showVorname);
		this.showTelefon = new JCheckBox("Zeige Telefon");
		mfmPane.add(showTelefon);
		this.showFEZahl = new JCheckBox("Zeige FE");
		mfmPane.add(showFEZahl);
		this.showErgebnis = new JCheckBox("Zeige Ergebnis");
		mfmPane.add(showErgebnis);
		this.showAngelegt = new JCheckBox("Zeige Datum");
		mfmPane.add(showAngelegt);

		dataPanel.add(mfmPane, BorderLayout.NORTH);

		JButton save = new JButton("Excel erzeugen");
		save.addActionListener(this);
		save.setActionCommand("start");
		dataPanel.add(save, BorderLayout.SOUTH);

		// load mf
		return dataPanel;
	}

	public void actionPerformed(ActionEvent e) {
		this.whatToDo = e.getActionCommand();
		// not much to do just start
		new Thread(this).start();
	}

	public void run() {
		this.parentWindow.setWaitCursor();
		boolean doit = this.whatToDo.equals("start");
		int addedCount = 0;
		// output to table
		int c = 0;
		short[] colWidths = new short[11];
		Vector<String> cn = new Vector<String>();
		if (this.showID.isSelected()) {
			cn.add(new String("Schl.-Nr."));
			colWidths[c++] = 30;
		}
		cn.add(new String("Stadt"));
		colWidths[c++] = 60;
		cn.add(new String("Straße + Nr."));
		colWidths[c++] = 75;

		cn.add(new String("Name"));
		colWidths[c++] = 50;
		if (this.showVorname.isSelected()) {
			cn.add(new String("Vorname"));
			colWidths[c++] = 50;
		}

		if (this.showTelefon.isSelected()) {
			cn.add(new String("Telefon"));
			colWidths[c++] = 50;
		}
		if (this.showErgebnis.isSelected()) {
			cn.add(new String("Ergebnis"));
			colWidths[c++] = 45;
		}
		if (this.showAngelegt.isSelected()) {
			cn.add(new String("Datum"));
			colWidths[c++] = 25;
		}
		if (this.showFEZahl.isSelected()) {
			cn.add(new String("FE"));
			colWidths[c++] = 30;
		}

		// restriktion
		String restrictType = "all";
		ListItem selectedItem = (ListItem) this.includelist.getSelectedItem();
		restrictType = selectedItem.getKey0();

		StatisticErgebnisTable ergTable = new StatisticErgebnisTable(cn, null, colWidths);

		// collecting user data
		String plz = this.plzTextbox.getText().replace('*', '%').replace('?', '_');
		String city = this.cityTextbox.getText().replace('*', '%').replace('?', '_');
		String results = "(";
		for (JComboBox cb : this.erglists) {
			ListItem selectedListItem = (ListItem) cb.getSelectedItem();
			if (!selectedListItem.getKey0().equals("-")) {
				results += selectedListItem.getKey0() + ",";
			}
		}
		results = results.substring(0, results.length() - 1) + ")";

		String whereClause = "WHERE k.id=a.kunde AND a.ergebnis=e.id";
		if (!results.equals(")")) {
			whereClause += " AND a.ergebnis IN " + results;
		}
		if (plz.length() > 0) {
			whereClause += " AND plz LIKE '" + plz + "'";
		}
		if (city.length() > 0) {
			whereClause += " AND stadt LIKE '" + city + "'";
		}
		if (restrictType.equals("last")) {
			whereClause += " GROUP BY a.kunde ORDER BY a.angelegt, k.id, a.ergebnis";
		} else {
			whereClause += " ORDER BY a.angelegt, k.id, a.ergebnis";
		}
		// limit
		int limit = 500;
		selectedItem = (ListItem) this.amountlist.getSelectedItem();
		if (selectedItem.getKey0().equals("1000")) {
			limit = 1000;
		} else if (selectedItem.getKey0().equals("1500")) {
			limit = 1500;
		} else if (selectedItem.getKey0().equals("2000")) {
			limit = 2000;
		}

		pm.setProgress(1);
		ResultSet foundActions = Database.selectWithLimits("*", "kunden k, aktionen a, ergebnisse e", whereClause, limit, 0);
		pm.setProgress(this.maxContacts);
		try {
			while (foundActions.next()) {
				addedCount++;

				Vector<String> aRow = new Vector<String>();
				if (this.showID.isSelected()) {
					aRow.add(foundActions.getString("k.id"));
				}
				aRow.add(foundActions.getString("k.plz") + " " + foundActions.getString("k.stadt"));
				aRow.add(foundActions.getString("k.strasse") + " " + foundActions.getString("k.hausnummer"));

				aRow.add(foundActions.getString("k.nachname"));

				if (this.showVorname.isSelected()) {
					aRow.add(foundActions.getString("k.vorname"));
				}
				if (this.showTelefon.isSelected()) {
					aRow.add(foundActions.getString("k.telprivat"));
				}
				if (this.showErgebnis.isSelected()) {
					aRow.add(foundActions.getString("e.name"));
				}
				if (this.showAngelegt.isSelected()) {
					aRow.add(foundActions.getString("a.angelegt"));
				}
				if (this.showFEZahl.isSelected()) {
					aRow.add(foundActions.getString("k.fensterzahl"));
				}

				ergTable.addRow(aRow);

				// Set new state
				pm.setProgress(limit + addedCount);
				// Change the note if desired
				String state = "Gefundene Kontakte: " + addedCount;
				pm.setNote(state);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Database.close(foundActions);
		}

		for (int i = 0; i < colWidths.length; i++) {
			colWidths[i] = (short) (colWidths[i] * 75);
		}

		String vorlagenDir = SettingsReader.getString("OVTAdmin.excelVorlagenVerzeichnis").replace("\\", "/");
		File fileToOpen = new File(vorlagenDir + "/mfad.xls");
		Table2Excel xls = new Table2Excel(ergTable, fileToOpen, "mfad.xls");
		xls.openXLS();

		pm.close();
		this.parentWindow.setDefaultCursor();
	}

	public String getTabName() {
		return "MFAD-Adressen";
	}

}
