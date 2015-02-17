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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

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

public class RegionErgebnisPanel extends JPanel implements ActionListener, ChartMouseListener {

	private static final String name = "Region-Ergebnisse";
	private static final float chartAlpha = 0.9f;

	private JComboBox region;
	private HashMap<String, Integer> grpData;
	private HashMap<String, Integer> ergData;
	private String[] mapChartIdx2Group;
	private StatisticErgebnisTable regionTable;
	private double countAll;
	private JPanel grpChartPanel;
	private JPanel ergChartPanel;
	private JButton exportTable;
	private JPanel regioChartPanel;
	private JRadioButton ftButton;
	private JRadioButton solarButton;
	private IMainWindow parentWindow;

	public RegionErgebnisPanel(IMainWindow pw) {
		this.parentWindow = pw;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel mf = new JPanel();
		JLabel mfTxt = new JLabel("Region:");
		mf.add(mfTxt);
		Vector<ListItem> rList = DBTools.regionsList();
		this.region = new JComboBox(rList);
		mf.add(this.region);

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
		this.regioChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Regionen-Übersicht", this.regioChartPanel);
		this.grpChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Ergebnisgruppen-Diagramme", this.grpChartPanel);
		this.ergChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Ergebnis-Diagramm", this.ergChartPanel);

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Gruppe"));
		cn.add(new String("Ergebnis"));
		cn.add(new String("Anzahl"));
		cn.add(new String("Prozent"));
		this.regionTable = new StatisticErgebnisTable(cn);
		JScrollPane tabScroller = new JScrollPane(this.regionTable);
		tablePanel.add(tabScroller, BorderLayout.CENTER);
		this.exportTable = new JButton("Als Exceltabelle speichern");
		this.exportTable.setActionCommand("export");
		this.exportTable.addActionListener(this);
		this.exportTable.setEnabled(false);
		tablePanel.add(this.exportTable, BorderLayout.SOUTH);
		tabs.addTab("Tabelle", tablePanel);
		this.add(tabs, BorderLayout.CENTER);
	}

	/**
	 * collect the counts of ergebnisses and group of ergebnisses
	 */
	private void collectData() {
		this.regionTable.getTableModel().emptyMe();
		this.exportTable.setEnabled(true);

		// find out which group: all, ft or solar
		String solarFlag = "";
		if (this.ftButton.isSelected()) {
			solarFlag = " AND a.shf=2 ";
		} else if (this.solarButton.isSelected()) {
			solarFlag = " AND a.shf=3 ";
		}

		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();

		// get mafo
		ListItem ri = (ListItem) this.region.getSelectedItem();
		String mf = ri.getKey0();
		String regionSQL = "";
		if (mf.trim().length() > 0) {
			regionSQL = " AND p.region=" + mf;
		}

		// retrieve data
		try {
			// count all
			this.countAll = 0;
			ResultSet rsAll = Database.select("count(*)", "kunden k, aktionen a, ergebnisse e, plz2region p", "WHERE "
					+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND e.finishedafter=1 AND a.angelegt>='"
					+ range.getVon() + "' AND a.angelegt<='" + range.getBis() + "' " + regionSQL + solarFlag);
			if (rsAll.next()) {
				this.countAll = rsAll.getInt(1);
			}
			Database.close(rsAll);

			Vector<String> allRow = new Vector<String>();
			allRow.add(ri.getValue());
			allRow.add("");
			allRow.add(Integer.toString((int) this.countAll));
			allRow.add("100.00 %");
			this.regionTable.addRow(allRow);

			Vector<String> emptyRow = new Vector<String>();
			emptyRow.add("");
			emptyRow.add("");
			emptyRow.add("");
			emptyRow.add("");
			this.regionTable.addRow(emptyRow);

			// count regions
			this.grpData = new HashMap<String, Integer>();
			ResultSet rs = Database
					.select(
							"e.gruppe, count(e.gruppe)",
							"aktionen a, ergebnisse e, kunden k, plz2region p",
							"WHERE "
									+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND e.finishedafter=1 AND a.angelegt>='"
									+ range.getVon() + "' AND a.angelegt<='" + range.getBis() + "' " + regionSQL
									+ solarFlag + " GROUP BY e.gruppe");
			while (rs.next()) {
				String grp = rs.getString("e.gruppe");
				int countGrp = rs.getInt(2);
				this.grpData.put(grp, countGrp);

				Vector<String> grpRowData = new Vector<String>();
				grpRowData.add(DBTools.nameOfGruppe(grp));
				grpRowData.add("");
				grpRowData.add(Integer.toString(countGrp));
				BigDecimal tmp = new BigDecimal(100 / (this.countAll / countGrp));
				tmp = tmp.setScale(2, BigDecimal.ROUND_HALF_UP);
				grpRowData.add(tmp.toString() + " %");
				this.regionTable.addRow(grpRowData);

				// count ergebnisses
				this.ergData = new HashMap<String, Integer>();
				ResultSet rsErg = Database
						.select(
								"a.ergebnis, count(a.ergebnis)",
								"aktionen a, ergebnisse e, kunden k, plz2region p",
								"WHERE "
										+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND e.finishedafter=1 AND a.angelegt>='"
										+ range.getVon() + "' AND e.gruppe=" + grp + " AND a.angelegt<='"
										+ range.getBis() + "' " + regionSQL + solarFlag + " GROUP BY a.ergebnis");
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
					this.regionTable.addRow(ergRowData);
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
		this.mapChartIdx2Group = new String[this.grpData.keySet().size()];
		for (Iterator<String> iter = this.grpData.keySet().iterator(); iter.hasNext();) {
			String grp = iter.next();
			int val = this.grpData.get(grp);
			if (val > 0) {
				String grpName = DBTools.nameOfGruppe(grp);
				result.setValue(grpName, val);
				this.mapChartIdx2Group[i] = grp;
				// System.out.println("grpidx:"+i+":"+grp+":"+val);
				i++;
			}
		}

		ListItem ri = (ListItem) this.region.getSelectedItem();
		String reg = ri.getValue();

		JFreeChart chart = ChartFactory.createPieChart("Region: " + reg, // chart
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
		// get mafo
		String mf = ((ListItem) this.region.getSelectedItem()).getKey0();
		String regionSQL = "";
		if (mf.trim().length() > 0) {
			regionSQL = " AND p.region=" + mf;
		}
		// find out which group: all, ft or solar
		String solarFlag = "";
		if (this.ftButton.isSelected()) {
			solarFlag = " AND a.shf=2 ";
		} else if (this.solarButton.isSelected()) {
			solarFlag = " AND a.shf=3 ";
		}
		DefaultPieDataset result = new DefaultPieDataset();

		ResultSet rsErg = Database.select("a.ergebnis, count(a.ergebnis)",
				"aktionen a, ergebnisse e, kunden k, plz2region p", "WHERE "
						+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND e.finishedafter=1 AND a.angelegt>='"
						+ range.getVon() + "' AND e.gruppe=" + grp + " AND a.angelegt<='" + range.getBis() + "' "
						+ regionSQL + solarFlag + " GROUP BY a.ergebnis");
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
		JFreeChart chart = ChartFactory.createPieChart("Ergebnisse von Gruppe: " + grpName, // chart
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
		// get mafo
		ListItem ri = (ListItem) this.region.getSelectedItem();
		String reg = ri.getKey0();
		String regionSQL = "";
		if (reg.trim().length() > 0) {
			regionSQL = " AND p.region=" + reg;
		}
		// find out which group: all, ft or solar
		String solarFlag = "";
		if (this.ftButton.isSelected()) {
			solarFlag = " AND a.shf=2 ";
		} else if (this.solarButton.isSelected()) {
			solarFlag = " AND a.shf=3 ";
		}
		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("a.ergebnis, count(a.ergebnis)",
				"aktionen a, ergebnisse e, kunden k, plz2region p", "WHERE "
						+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND e.finishedafter=1 AND a.angelegt>='"
						+ range.getVon() + "' AND a.angelegt<='" + range.getBis() + "' " + regionSQL + solarFlag
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

		JFreeChart chart = ChartFactory.createPieChart("Ergebnisse: " + ri.getValue(), // chart
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

	private void chartRegioData() {
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();
		// find out which group: all, ft or solar
		String solarFlag = "";
		if (this.ftButton.isSelected()) {
			solarFlag = " AND a.shf=2 ";
		} else if (this.solarButton.isSelected()) {
			solarFlag = " AND a.shf=3 ";
		}
		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("p.region, count(p.region)",
				"aktionen a, ergebnisse e, kunden k, plz2region p", "WHERE "
						+ "a.ergebnis=e.id AND k.id=a.kunde AND k.plz=p.plz AND e.finishedafter=1 AND a.angelegt>='"
						+ range.getVon() + "' AND a.angelegt<='" + range.getBis() + "' " + solarFlag
						+ " GROUP BY p.region");
		try {
			while (rsErg.next()) {
				String reg = rsErg.getString("p.region");
				String regName = DBTools.nameOfRegion(reg);
				int countErg = rsErg.getInt(2);
				result.setValue(regName, countErg);
				// System.out.println(ergName+":"+countErg);
			}
			Database.close(rsErg);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JFreeChart chart = ChartFactory.createPieChart("Ergebnisse aller Regionen", // chart
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
		this.regioChartPanel.removeAll();
		this.regioChartPanel.add(cp, BorderLayout.CENTER);
		this.regioChartPanel.updateUI();
	}

	public void doStatistics() {
		this.parentWindow.setWaitCursor();
		this.collectData();
		this.chartGroupData();
		this.chartErgData();
		this.chartRegioData();
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
		Table2Excel xls = new Table2Excel(this.regionTable, "statisticsTMP.xls", "Region-Ergebnisse", fields);
		xls.openXLS();
	}

	public String getTabName() {
		return name;
	}

	public void chartMouseClicked(ChartMouseEvent event) {
		JFreeChart cp = (JFreeChart) event.getSource();
		if (cp.getTitle().getText().startsWith("Erg")) {
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
