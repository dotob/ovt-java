package statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tools.MyLog;
import tools.Table2Excel;
import ui.IMainWindow;
import db.Database;

public class PLZUebersichtPanel extends JPanel implements ActionListener, ItemListener {

	private static final long serialVersionUID = -1265109804422695892L;

	private static final String name = "PLZ-Übersicht";

	private StatisticErgebnisTable mfeTable;
	private JButton exportTable;

	private IMainWindow parentWindow;

	public PLZUebersichtPanel(IMainWindow pw) {
		this.parentWindow = pw;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel headPanel = new JPanel(new BorderLayout());

		JLabel info = new JLabel("<html><h3>Datum spielt hier keine Rolle!!!</h3></html>");
		info.setForeground(Color.RED);
		info.setHorizontalAlignment(JLabel.CENTER);
		headPanel.add(info, BorderLayout.NORTH);

		JButton goButton = new JButton("Aktualisieren");
		goButton.setActionCommand("start");
		goButton.addActionListener(this);
		headPanel.add(goButton, BorderLayout.SOUTH);
		this.add(headPanel, BorderLayout.NORTH);

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<String> cn = new Vector<String>();
		cn.add(new String("PLZ"));
		cn.add(new String("Ort"));
		cn.add(new String("FHT"));
		cn.add(new String("FHT ohne Kontakt"));
		cn.add(new String("FHT mit Kontakt ohne Gespräch"));
		cn.add(new String("FHT mit Gespräch"));
		cn.add(new String("Solar ohne Kontakt"));
		cn.add(new String("Solar mit Kontakt ohne Gespräch"));
		cn.add(new String("Solar mit Gespräch"));
		int idx = 0;
		short[] cnw = new short[9];
		cnw[idx++] = 60;
		cnw[idx++] = 150;
		cnw[idx++] = 100;
		cnw[idx++] = 100;
		cnw[idx++] = 100;
		cnw[idx++] = 100;
		cnw[idx++] = 100;
		cnw[idx++] = 100;
		cnw[idx++] = 100;
		this.mfeTable = new StatisticErgebnisTable(cn, null, cnw);
		JScrollPane tabScroller = new JScrollPane(this.mfeTable);
		tablePanel.add(tabScroller, BorderLayout.CENTER);
		this.exportTable = new JButton("Als Exceltabelle speichern");
		this.exportTable.setActionCommand("export");
		this.exportTable.addActionListener(this);
		this.exportTable.setEnabled(false);
		tablePanel.add(this.exportTable, BorderLayout.SOUTH);
		this.add(tablePanel, BorderLayout.CENTER);
	}

	/**
	 * collect the counts of ergebnisses and group of ergebnisses
	 */
	private void collectData() {
		this.mfeTable.getTableModel().emptyMe();
		this.exportTable.setEnabled(true);
		java.util.Date start = new java.util.Date();
		// retrieve data
		try {
			ResultSet rsplz = Database.select("DISTINCT(plz), stadt", "kunden", "WHERE plz REGEXP '^[0-9]{5}' ORDER BY plz");
			System.out.println("plz eingelesen");
			while (rsplz.next()) {
				Vector<String> allRow = new Vector<String>();
				String plz = rsplz.getString("plz");
				if (plz != null && plz.length() > 0) {
					allRow.add(plz);
					allRow.add(rsplz.getString("stadt"));

					// now get count of contacts
					String shfflag = "1";
					// FHT all
					int fhtall = -1;
					ResultSet rsfhtall = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag + " AND plz=" + plz);
					while (rsfhtall.next()) {
						fhtall = rsfhtall.getInt(1);
					}
					Database.close(rsfhtall);
					allRow.add(Integer.toString(fhtall));

					// FHT no contact
					int fhtnocontact = -1;
					ResultSet rsfhtnocontact = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag
							+ " AND hasaktion=0 AND plz=" + plz);
					while (rsfhtnocontact.next()) {
						fhtnocontact = rsfhtnocontact.getInt(1);
					}
					Database.close(rsfhtnocontact);
					allRow.add(Integer.toString(fhtnocontact));

					// FHT contact but no gespraech
					int fhtconactnotermin = -1;
					ResultSet rsfhtconactnotermin = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag
							+ " AND hasaktion=1 AND hasgespraech=0 AND plz=" + plz);
					while (rsfhtconactnotermin.next()) {
						fhtconactnotermin = rsfhtconactnotermin.getInt(1);
					}
					Database.close(rsfhtconactnotermin);
					allRow.add(Integer.toString(fhtconactnotermin));

					// FHT contact and gespraech
					int fhtconactandtermin = -1;
					ResultSet rsfhtconactandtermin = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag
							+ " AND hasaktion=1 AND hasgespraech=1 AND plz=" + plz);
					while (rsfhtconactandtermin.next()) {
						fhtconactandtermin = rsfhtconactandtermin.getInt(1);
					}
					Database.close(rsfhtconactandtermin);
					allRow.add(Integer.toString(fhtconactandtermin));

					// solar all
					shfflag = "3";

					int solartall = -1;
					ResultSet rssolartall = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag + " AND plz=" + plz);
					while (rssolartall.next()) {
						solartall = rssolartall.getInt(1);
					}
					Database.close(rssolartall);
					allRow.add(Integer.toString(solartall));

					// solarT no contact
					int solartnocontact = -1;
					ResultSet rssolartnocontact = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag
							+ " AND hasaktion=0 AND plz=" + plz);
					while (rssolartnocontact.next()) {
						solartnocontact = rssolartnocontact.getInt(1);
					}
					Database.close(rssolartnocontact);
					allRow.add(Integer.toString(solartnocontact));

					// solarT contact but no gespraech
					int solartconactnotermin = -1;
					ResultSet rssolartconactnotermin = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag
							+ " AND hasaktion=1 AND hasgespraech=0 AND plz=" + plz);
					while (rssolartconactnotermin.next()) {
						solartconactnotermin = rssolartconactnotermin.getInt(1);
					}
					Database.close(rssolartconactnotermin);
					allRow.add(Integer.toString(solartconactnotermin));

					// solarT contact and gespraech
					int solartconactandtermin = -1;
					ResultSet rssolartconactandtermin = Database.select("count(*)", "kunden", "WHERE shfflag=" + shfflag
							+ " AND hasaktion=1 AND hasgespraech=1 AND plz=" + plz);
					while (rssolartconactandtermin.next()) {
						solartconactandtermin = rssolartconactandtermin.getInt(1);
					}
					Database.close(rssolartconactandtermin);
					allRow.add(Integer.toString(solartconactandtermin));

					System.out.println("plz: " + plz);
					this.mfeTable.addRow(allRow);
				}
			}
			Database.close(rsplz);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		System.out.println("start: " + start);
		System.out.println("ende: " + new java.util.Date());
	}

	public void doStatistics() {
		this.parentWindow.setWaitCursor();
		this.collectData();
		this.parentWindow.setDefaultCursor();
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("start")) {
			this.doStatistics();

		} else if (arg0.getActionCommand().equals("export")) {
			this.exportTableData();
		}
	}

	public void itemStateChanged(ItemEvent arg0) {
	}

	private void exportTableData() {
		short[] fields = new short[4];
		fields[0] = 7500;
		fields[1] = 9000;
		fields[2] = 2500;
		fields[3] = 2500;
		Table2Excel xls = new Table2Excel(this.mfeTable, "statisticsTMP.xls", "Adressenanzahl", fields);
		xls.openXLS();
	}

	public String getTabName() {
		return name;
	}
}
