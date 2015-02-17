package statistics;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import tools.DateInterval;
import tools.ListItem;
import tools.MyLog;
import tools.Table2Excel;
import ui.IMainWindow;
import db.DBTools;
import db.Database;

public class GesamtRegionPanel extends JPanel implements ActionListener {

	private static final String name = "Regionen-Gesamt";

	private StatisticErgebnisTable telefonTable;
	private JButton exportTable;
	private StatisticErgebnisTable gespraechTable;
	private JButton export2Table;

	private JRadioButton solarButton;

	private JRadioButton ftButton;

	private IMainWindow parentWindow;

	public GesamtRegionPanel(IMainWindow pw) {
		this.parentWindow = pw;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel mf = new JPanel();

		JRadioButton allButton = new JRadioButton("Alle");
		allButton.setSelected(true);
		this.ftButton = new JRadioButton("Nur FT");
		this.solarButton = new JRadioButton("Nur Solar");
		ButtonGroup shfSelector = new ButtonGroup();
		shfSelector.add(allButton);
		shfSelector.add(this.ftButton);
		shfSelector.add(this.solarButton);
		JPanel radioPanel = new JPanel(new GridLayout(1, 0));
		radioPanel.add(allButton);
		radioPanel.add(this.ftButton);
		radioPanel.add(this.solarButton);
		mf.add(radioPanel);

		JButton goButton = new JButton("Aktualisieren");
		goButton.setActionCommand("start");
		goButton.addActionListener(this);
		mf.add(goButton);
		this.add(mf, BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<ListItem> rn = DBTools.regionsList();
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Ergebnis"));
		for (Iterator<ListItem> iter = rn.iterator(); iter.hasNext();) {
			ListItem li = iter.next();
			cn.add(new String(li.getValue()));
			cn.add("%");
		}
		this.telefonTable = new StatisticErgebnisTable(cn, null);
		JScrollPane tabScroller = new JScrollPane(this.telefonTable);
		tablePanel.add(tabScroller, BorderLayout.CENTER);
		this.exportTable = new JButton("Als Exceltabelle speichern");
		this.exportTable.setActionCommand("export");
		this.exportTable.addActionListener(this);
		this.exportTable.setEnabled(false);
		tablePanel.add(this.exportTable, BorderLayout.SOUTH);
		tabs.addTab("MF-Ergebnisse", tablePanel);

		JPanel table2Panel = new JPanel(new BorderLayout());
		short[] colWidths = new short[1];
		colWidths[0] = 250;
		this.gespraechTable = new StatisticErgebnisTable(cn, null, colWidths);
		JScrollPane tab2Scroller = new JScrollPane(this.gespraechTable);
		table2Panel.add(tab2Scroller, BorderLayout.CENTER);
		this.export2Table = new JButton("Als Exceltabelle speichern");
		this.export2Table.setActionCommand("export");
		this.export2Table.addActionListener(this);
		this.export2Table.setEnabled(false);
		table2Panel.add(this.export2Table, BorderLayout.SOUTH);
		tabs.addTab("Termine", table2Panel);
		this.add(tabs, BorderLayout.CENTER);
	}

	/**
	 * collect the counts of ergebnisses and group of ergebnisses
	 */
	private void collectData() {
		this.telefonTable.getTableModel().emptyMe();
		this.gespraechTable.getTableModel().emptyMe();
		this.exportTable.setEnabled(true);
		this.export2Table.setEnabled(true);

		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();
		DateInterval rangeG = MainStatisticPanel.getVonBis();
		rangeG.shiftWeeks(1);

		// find out which group: all, ft or solar
		String solarFlag = "";
		String solarGFlag = "";
		if (this.ftButton.isSelected()) {
			solarFlag = " AND a.shf=2 ";
			solarGFlag = " AND g.shf=2 ";
		} else if (this.solarButton.isSelected()) {
			solarFlag = " AND a.shf=3 ";
			solarGFlag = " AND g.shf=3 ";
		}

		// retrieve data
		try {

			// cellect data from aktionen (telefon)

			HashMap<Integer, Integer> countOfAlle = new HashMap<Integer, Integer>();
			Vector<String> rowData = new Vector<String>();
			rowData.add("Gesamt");
			ResultSet rsAll = Database.select("p.region, count(p.region) AS anz", "aktionen a, ergebnisse e, kunden k, plz2region p",
					"WHERE " + "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND a.angelegt>='" + range.getVon()
							+ "' AND a.angelegt<='" + range.getBis() + "' " + solarFlag + " GROUP BY p.region");
			while (rsAll.next()) {
				countOfAlle.put(rsAll.getInt("p.region"), rsAll.getInt("anz"));
				rowData.add(rsAll.getString("anz"));
				rowData.add("100 %");
			}
			this.telefonTable.addRow(rowData);

			Vector<ListItem> telErg = DBTools.buildAktionsErgebnisListAll(false);
			for (Iterator<ListItem> iter = telErg.iterator(); iter.hasNext();) {
				ListItem li = iter.next();

				// count telefon ergebnisse
				rowData = new Vector<String>();
				rowData.add(li.getKey1());
				ResultSet rs = Database.select("p.region, count(p.region)", "aktionen a, ergebnisse e, kunden k, plz2region p", "WHERE "
						+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND a.angelegt>='" + range.getVon() + "' AND a.angelegt<='"
						+ range.getBis() + "' AND e.id=" + li.getKey0() + " " + solarFlag + " GROUP BY p.region");
				while (rs.next()) {
					int countErg = rs.getInt(2);
					rowData.add(Integer.toString(countErg));
					int countAlle = countOfAlle.get(rs.getInt("p.region"));
					BigDecimal tmp = new BigDecimal(100 / (countAlle / countErg));
					tmp = tmp.setScale(1, BigDecimal.ROUND_HALF_UP);
					rowData.add(tmp.toString() + " %");
				}
				Database.close(rs);
				this.telefonTable.addRow(rowData);
			}

			// collect data from termine
			Vector regions = DBTools.buildTerminErgebnisList();
			// find count of alle
			countOfAlle = new HashMap<Integer, Integer>();
			rowData = new Vector<String>();
			rowData.add("Gesamt");
			rsAll = Database.select("p.region, count(p.region) AS anz", "gespraeche g, terminergebnisse e, kunden k, plz2region p",
					"WHERE " + "g.ergebnis=e.id AND k.id=g.kunde AND k.plz=p.plz AND g.angelegt>='" + rangeG.getVon()
							+ "' AND g.angelegt<='" + rangeG.getBis() + "' " + solarGFlag + " GROUP BY p.region");
			while (rsAll.next()) {
				countOfAlle.put(rsAll.getInt("p.region"), rsAll.getInt("anz"));
				rowData.add(rsAll.getString("anz"));
				rowData.add("100 %");
			}
			this.gespraechTable.addRow(rowData);

			Database.close(rsAll);
			for (Iterator iter = regions.iterator(); iter.hasNext();) {
				ListItem li = (ListItem) iter.next();

				// count termin ergebnisse
				rowData = new Vector<String>();
				rowData.add(li.getValue());
				ResultSet rs = Database.select("p.region, count(p.region)", "gespraeche g, terminergebnisse e, kunden k, plz2region p",
						"WHERE " + "g.ergebnis=e.id AND k.id=g.kunde AND k.plz=p.plz AND g.angelegt>='" + rangeG.getVon()
								+ "' AND g.angelegt<='" + rangeG.getBis() + "' AND g.ergebnis=" + li.getKey0() + " " + solarGFlag
								+ " GROUP BY p.region");
				while (rs.next()) {
					int countErg = rs.getInt(2);
					int countAlle = countOfAlle.get(rs.getInt("p.region"));
					rowData.add(Integer.toString(countErg));
					BigDecimal tmp = new BigDecimal(100 / (countAlle / countErg));
					tmp = tmp.setScale(1, BigDecimal.ROUND_HALF_UP);
					rowData.add(tmp.toString() + " %");
				}
				Database.close(rs);
				this.gespraechTable.addRow(rowData);

			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
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

	private void exportTableData() {
		short[] fields = new short[4];
		fields[0] = 7500;
		fields[1] = 9000;
		fields[2] = 2500;
		fields[3] = 2500;
		Table2Excel xls = new Table2Excel(this.telefonTable, "statisticsTMP.xls", "Regionen-Gesamt", fields);
		xls.openXLS();
	}

	public String getTabName() {
		return name;
	}
}
