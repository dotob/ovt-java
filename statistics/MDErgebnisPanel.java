package statistics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

import tools.DateInterval;
import tools.ListItem;
import tools.MyLog;
import tools.Table2Excel;
import ui.IMainWindow;
import db.DBTools;
import db.Database;
import db.Marktforscher;

public class MDErgebnisPanel extends JPanel implements ActionListener, ChartMouseListener {

	private static final String name = "MDE-Ergebnisse";
	private static final float chartAlpha = 0.9f;

	private JComboBox mafos;
	private HashMap<String, Integer> groupData;
	private HashMap<String, Integer> ergData;
	private String[] mapChartIdx2Group;
	private StatisticErgebnisTable mfeTable;
	private double countAll;
	private JPanel grpChartPanel;
	private JPanel ergChartPanel;
	private JButton exportTable;
	private JPanel mfChartPanel;
	private JLabel plzTxt;
	private JTextField plzInput;
	private IMainWindow parentWindow;

	public MDErgebnisPanel(IMainWindow pw) {
		this.parentWindow = pw;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel mf = new JPanel();
		JLabel mfTxt = new JLabel("Marktdatenermittler:");
		mf.add(mfTxt);
		this.mafos = new JComboBox(DBTools.marktdatenermittlerList());
		mf.add(this.mafos);

		this.plzTxt = new JLabel("PLZ:");
		mf.add(this.plzTxt);
		this.plzInput = new JTextField(7);
		mf.add(this.plzInput);

		JButton goButton = new JButton("Aktualisieren");
		goButton.setActionCommand("start");
		goButton.addActionListener(this);
		mf.add(goButton);
		this.add(mf, BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		this.mfChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Marktforscher-Diagramm", this.mfChartPanel);
		this.grpChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Gruppen-Diagramme", this.grpChartPanel);
		this.ergChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Ergebnis-Diagramm", this.ergChartPanel);

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Gruppe"));
		cn.add(new String("Ergebnis"));
		cn.add(new String("Anzahl"));
		cn.add(new String("Prozent"));
		this.mfeTable = new StatisticErgebnisTable(cn);
		JScrollPane tabScroller = new JScrollPane(this.mfeTable);
		tablePanel.add(tabScroller, BorderLayout.CENTER);
		this.exportTable = new JButton("Als Exceltabelle speichern");
		this.exportTable.setActionCommand("export");
		this.exportTable.addActionListener(this);
		this.exportTable.setEnabled(false);
		tablePanel.add(this.exportTable, BorderLayout.SOUTH);
		tabs.addTab("Tabelle", tablePanel);
		this.add(tabs, BorderLayout.CENTER);
	}

	private String getMDESQL() {
		ListItem ri = (ListItem) this.mafos.getSelectedItem();
		String mf = ri.getKey0();
		String mdeSQL = "";
		if (mf.trim().length() > 0) {
			mdeSQL = " AND k.bearbeiter=" + mf;
		}
		return mdeSQL;
	}

	private String getPLZSQL() {
		String plzSQL = "";
		String plz = this.plzInput.getText().trim();
		if (plz.length() > 0) {
			if (plz.contains("*") || plz.contains("*")) {
				plzSQL = " AND k.plz LIKE '" + plz.replace('*', '%').replace('?', '_') + "'";
			} else {
				plzSQL = " AND k.plz=" + plz;
			}
		}
		return plzSQL;
	}

	private String getMDEName() {
		ListItem ri = (ListItem) this.mafos.getSelectedItem();
		return ri.getValue();
	}

	/**
	 * collect the counts of ergebnisses and group of ergebnisses
	 */
	private void collectData() {
		this.mfeTable.getTableModel().emptyMe();
		this.exportTable.setEnabled(true);

		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();

		// retrieve data
		try {
			// count all
			this.countAll = 0;
			ResultSet rsAll = Database.select("count(*)", "kunden k, aktionen a, ergebnisse e", "WHERE "
					+ "a.kunde=k.id AND a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
					+ "' AND a.angelegt<='" + range.getBis() + "' " + getMDESQL() + getPLZSQL());
			if (rsAll.next()) {
				this.countAll = rsAll.getInt(1);
			}
			Database.close(rsAll);
			// System.out.println(this.countAll);

			Vector<String> allRow = new Vector<String>();
			allRow.add(getMDEName());
			allRow.add("");
			allRow.add(Integer.toString((int) this.countAll));
			allRow.add("100.00 %");
			this.mfeTable.addRow(allRow);

			Vector<String> emptyRow = new Vector<String>();
			emptyRow.add("");
			emptyRow.add("");
			emptyRow.add("");
			emptyRow.add("");
			this.mfeTable.addRow(emptyRow);

			// count groups
			this.groupData = new HashMap<String, Integer>();
			ResultSet rs = Database.select("e.gruppe, count(e.gruppe)", "kunden k, aktionen a, ergebnisse e", "WHERE "
					+ "a.kunde=k.id AND a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
					+ "' AND a.angelegt<='" + range.getBis() + "' " + getMDESQL() + getPLZSQL() + " GROUP BY e.gruppe");
			while (rs.next()) {
				String grp = rs.getString("e.gruppe");
				int countGrp = rs.getInt(2);
				this.groupData.put(grp, countGrp);

				Vector<String> grpRowData = new Vector<String>();
				grpRowData.add(DBTools.nameOfGruppe(grp));
				grpRowData.add("");
				grpRowData.add(Integer.toString(countGrp));
				BigDecimal tmp = new BigDecimal(100 / (this.countAll / countGrp));
				tmp = tmp.setScale(2, BigDecimal.ROUND_HALF_UP);
				grpRowData.add(tmp.toString() + " %");
				this.mfeTable.addRow(grpRowData);

				// count ergebnisses
				this.ergData = new HashMap<String, Integer>();
				ResultSet rsErg = Database.select("a.ergebnis, count(a.ergebnis)",
						"kunden k, aktionen a, ergebnisse e", "WHERE "
								+ "a.kunde=k.id AND a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='"
								+ range.getVon() + "' AND e.gruppe=" + grp + " AND a.angelegt<='" + range.getBis()
								+ "' " + getMDESQL() + getPLZSQL() + " GROUP BY a.ergebnis");
				while (rsErg.next()) {
					String erg = rsErg.getString("a.ergebnis");
					int countErg = rsErg.getInt(2);
					this.ergData.put(erg, countErg);

					Vector<String> ergRowData = new Vector<String>();
					ergRowData.add("");
					ergRowData.add(DBTools.nameOfErgebnis(erg, false));
					ergRowData.add(Integer.toString(countErg));
					BigDecimal tmp2 = new BigDecimal(100 / (this.countAll / countErg));
					tmp2 = tmp2.setScale(2, BigDecimal.ROUND_HALF_UP);
					ergRowData.add(tmp2.toString() + " %");
					this.mfeTable.addRow(ergRowData);
				}
				Database.close(rsErg);
			}
			Database.close(rs);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	private void chartGroupData() {
		DefaultPieDataset result = new DefaultPieDataset();
		int i = 0;
		this.mapChartIdx2Group = new String[this.groupData.keySet().size()];
		for (Iterator<String> iter = this.groupData.keySet().iterator(); iter.hasNext();) {
			String grp = iter.next();
			int val = this.groupData.get(grp);
			if (val > 0) {
				String grpName = DBTools.nameOfGruppe(grp);
				result.setValue(grpName, val);
				this.mapChartIdx2Group[i] = grp;
				// System.out.println("grpidx:"+i+":"+grp+":"+val);
				i++;
			}
		}

		ListItem ri = (ListItem) this.mafos.getSelectedItem();

		JFreeChart chart = ChartFactory.createPieChart("Gruppen: " + ri.getValue(), // chart
				// title
				result, // data
				false, // include legend
				true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(chartAlpha);
		plot.setNoDataMessage("Keine Ergebnisse gefunden");

		ChartPanel cp = new ChartPanel(chart);
		cp.addChartMouseListener(this);
		this.grpChartPanel.removeAll();
		this.grpChartPanel.add(cp, BorderLayout.CENTER);
		this.grpChartPanel.updateUI();
	}

	private void chartGrpErgData(String grp) {
		// System.out.println("grp:"+grp);
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();

		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("a.ergebnis, count(a.ergebnis)", "kunden k, aktionen a, ergebnisse e",
				"WHERE " + "a.kunde=k.id AND a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
						+ "' AND e.gruppe=" + grp + " AND a.angelegt<='" + range.getBis() + "' " + getMDESQL()
						+ getPLZSQL() + " GROUP BY a.ergebnis");
		try {
			while (rsErg.next()) {
				String erg = rsErg.getString("a.ergebnis");
				int countErg = rsErg.getInt(2);
				String ergName = DBTools.nameOfErgebnis(erg, false);
				result.setValue(ergName, countErg);
				// System.out.println(ergName+":"+countErg);
			}
			Database.close(rsErg);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String grpName = DBTools.nameOfGruppe(grp);
		JFreeChart chart = ChartFactory.createPieChart("MF-Ergebnisgruppen: " + grpName, // chart
				// title
				result, // data
				false, // include legend
				true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(chartAlpha);
		plot.setNoDataMessage("Keine Ergebnisse gefunden");

		ChartPanel cp = new ChartPanel(chart);
		cp.addChartMouseListener(this);

		this.grpChartPanel.removeAll();
		this.grpChartPanel.add(cp, BorderLayout.CENTER);
		this.grpChartPanel.updateUI();
	}

	private void chartErgData() {
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();

		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("a.ergebnis, count(a.ergebnis)", "kunden k, aktionen a, ergebnisse e",
				"WHERE " + "a.kunde=k.id AND a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
						+ "' AND a.angelegt<='" + range.getBis() + "' " + getMDESQL() + getPLZSQL()
						+ " GROUP BY a.ergebnis");
		try {
			while (rsErg.next()) {
				String erg = rsErg.getString("a.ergebnis");
				int countErg = rsErg.getInt(2);
				String ergName = DBTools.nameOfErgebnis(erg, false);
				result.setValue(ergName, countErg);
				// System.out.println(ergName+":"+countErg);
			}
			Database.close(rsErg);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JFreeChart chart = ChartFactory.createPieChart("Anzahl bearbeiteter Adressen: " + getMDEName(), // chart
				// title
				result, // data
				false, // include legend
				true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(200);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(chartAlpha);
		plot.setNoDataMessage("Keine Ergebnisse gefunden");

		ChartPanel cp = new ChartPanel(chart);
		this.ergChartPanel.removeAll();
		this.ergChartPanel.add(cp, BorderLayout.CENTER);
		this.ergChartPanel.updateUI();
	}

	private void chartMFData() {
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();

		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("a.marktforscher, count(a.marktforscher)",
				"kunden k, aktionen a, ergebnisse e", "WHERE "
						+ "a.kunde=k.id AND a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
						+ "' AND a.angelegt<='" + range.getBis() + "' " + getMDESQL() + getPLZSQL()
						+ " GROUP BY a.marktforscher");
		try {
			while (rsErg.next()) {
				String mf = rsErg.getString("a.marktforscher");
				String mfName = Marktforscher.SearchMarktforscher(mf).toString();
				int countErg = rsErg.getInt(2);
				result.setValue(mfName, countErg);
				// System.out.println(ergName+":"+countErg);
			}
			Database.close(rsErg);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JFreeChart chart = ChartFactory.createPieChart("Ergebnisse aller Marktforscher", // chart
				// title
				result, // data
				false, // include legend
				true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(200);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(chartAlpha);
		plot.setNoDataMessage("Keine Ergebnisse gefunden");

		ChartPanel cp = new ChartPanel(chart);
		this.mfChartPanel.removeAll();
		this.mfChartPanel.add(cp, BorderLayout.CENTER);
		this.mfChartPanel.updateUI();
	}

	public void doStatistics() {
		this.parentWindow.setWaitCursor();
		this.collectData();
		this.chartGroupData();
		this.chartErgData();
		this.chartMFData();
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
		Table2Excel xls = new Table2Excel(this.mfeTable, "statisticsTMP.xls", "MF-Ergebnisse", fields);
		xls.openXLS();
	}

	public String getTabName() {
		return name;
	}

	public void chartMouseClicked(ChartMouseEvent event) {
		JFreeChart cp = (JFreeChart) event.getSource();
		System.out.println(":::::" + cp.getTitle().getText());
		if (!cp.getTitle().getText().startsWith("Gru")) {
			this.chartGroupData();
		} else {
			if (event.getEntity() instanceof PieSectionEntity) {
				PieSectionEntity pse = (PieSectionEntity) event.getEntity();
				if (pse != null) {
					// System.out.println(pse.getSectionIndex());
					this.chartGrpErgData(this.mapChartIdx2Group[pse.getSectionIndex()]);
				}
			}
		}
	}

	public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub

	}
}
