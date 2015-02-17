package statistics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
import db.Projektleiter;

public class WLGespraechPanel extends JPanel implements ActionListener {

	private static final long			serialVersionUID	= -22348298304355088L;
	private static final String			name				= "PL-Termine";
	private static final float			chartAlpha			= 0.9f;

	private JComboBox					mafos;
	private HashMap<String, Integer>	ergData;
	private StatisticErgebnisTable		wleTable;
	private double						countAll;
	private JPanel						ergChartPanel;
	private JButton						exportTable;
	private JPanel						wlChartPanel;
	private JLabel						plzTxt;
	private JTextField					plzInput;
	private IMainWindow					parentWindow;

	public WLGespraechPanel(IMainWindow pw) {
		this.parentWindow = pw;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel mf = new JPanel();
		JLabel mfTxt = new JLabel("Projektleiter:");
		mf.add(mfTxt);
		Vector<ListItem> mListWithAll = new Vector<ListItem>();
		mListWithAll.add(new ListItem("", "Alle"));
		mListWithAll.add(new ListItem("allFT", "Alle, nur Fenster + Türen"));
		mListWithAll.add(new ListItem("allSolar", "Alle, nur Solar"));
		Vector<ListItem> mList = DBTools.wlList();
		mListWithAll.addAll(mList);
		this.mafos = new JComboBox(mListWithAll);
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
		this.wlChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Projektleiter-Diagramm", this.wlChartPanel);
		this.ergChartPanel = new JPanel(new BorderLayout());
		tabs.addTab("Terminergebnis-Diagramm", this.ergChartPanel);

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Ergebnis"));
		cn.add(new String("Anzahl"));
		cn.add(new String("Prozent"));
		this.wleTable = new StatisticErgebnisTable(cn);
		JScrollPane tabScroller = new JScrollPane(this.wleTable);
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
		this.wleTable.getTableModel().emptyMe();
		this.exportTable.setEnabled(true);

		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();
		range.shiftWeeks(1);

		// get mafo
		ListItem mfItem = (ListItem) this.mafos.getSelectedItem();

		// retrieve data
		try {
			// count all
			this.countAll = 0;
			ResultSet rsAll = Database.select("count(*)", "kunden k, gespraeche g, terminergebnisse e", "WHERE "
					+ "k.id=g.kunde AND g.ergebnis=e.id AND g.datum_vd>='" + range.getVon() + "' AND g.datum_vd<='"
					+ range.getBis() + "' " + this.getMaFoSQL() + this.getPLZSQL());
			if (rsAll.next()) {
				this.countAll = rsAll.getInt(1);
			}
			Database.close(rsAll);
			// System.out.println(this.countAll);

			Vector<String> allRow = new Vector<String>();
			allRow.add(mfItem.getValue());
			allRow.add(Integer.toString((int) this.countAll));
			allRow.add("100.00 %");
			this.wleTable.addRow(allRow);

			Vector<String> emptyRow = new Vector<String>();
			emptyRow.add("");
			emptyRow.add("");
			emptyRow.add("");
			emptyRow.add("");
			this.wleTable.addRow(emptyRow);

			// count ergebnisses
			this.ergData = new HashMap<String, Integer>();
			ResultSet rsErg = Database.select("g.ergebnis, count(g.ergebnis)", "kunden k, gespraeche g",
					"WHERE  k.id=g.kunde AND g.datum_vd>='" + range.getVon() + "' AND g.datum_vd<='" + range.getBis()
							+ "' " + this.getMaFoSQL() + this.getPLZSQL() + " GROUP BY g.ergebnis");
			while (rsErg.next()) {
				String erg = rsErg.getString("g.ergebnis");
				int countErg = rsErg.getInt(2);
				this.ergData.put(erg, countErg);

				Vector<String> ergRowData = new Vector<String>();
				ergRowData.add(DBTools.nameOfTerminErgebnis(erg));
				ergRowData.add(Integer.toString(countErg));
				BigDecimal tmp2 = new BigDecimal(100 / (this.countAll / countErg));
				tmp2 = tmp2.setScale(2, BigDecimal.ROUND_HALF_UP);
				ergRowData.add(tmp2.toString() + " %");
				this.wleTable.addRow(ergRowData);
			}
			Database.close(rsErg);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	private String getMaFoSQL() {
		// get mafo
		ListItem ri = (ListItem) this.mafos.getSelectedItem();
		String mf = ri.getKey0();
		String mafoSQL = "";
		if (mf.trim().equals("allFT")) {
			mafoSQL = " AND g.shf=2";
		} else if (mf.trim().equals("allSolar")) {
			mafoSQL = " AND g.shf=3";
		} else if (mf.trim().length() > 0) {
			mafoSQL = " AND g.werbeleiter=" + mf;
		}
		return mafoSQL;
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

	private void chartErgData() {
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();
		range.shiftWeeks(1);

		// get mafo
		ListItem ri = (ListItem) this.mafos.getSelectedItem();
		String mf = ri.getKey0();
		String mafoSQL = "";
		if (mf.trim().equals("allFT")) {
			mafoSQL = " AND g.shf=2";
		} else if (mf.trim().equals("allSolar")) {
			mafoSQL = " AND g.shf=3";
		} else if (mf.trim().length() > 0) {
			mafoSQL = " AND g.werbeleiter=" + mf;
		}

		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("g.ergebnis, count(g.ergebnis)", "gespraeche g, terminergebnisse e", "WHERE "
				+ "g.ergebnis=e.id AND g.datum_vd>='" + range.getVon() + "' AND g.datum_vd<='" + range.getBis() + "' "
				+ mafoSQL + " GROUP BY g.ergebnis, g.kunde, g.datum_vd HAVING count(*)=1");
		try {
			while (rsErg.next()) {
				String erg = rsErg.getString("g.ergebnis");
				int countErg = rsErg.getInt(2);
				String ergName = DBTools.nameOfTerminErgebnis(erg);
				result.setValue(ergName, countErg);
				// System.out.println(ergName+":"+countErg);
			}
			Database.close(rsErg);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String mfName = ri.getValue();

		JFreeChart chart = ChartFactory.createPieChart("Gesprächsergebnisse: " + mfName, // chart
				// title
				result, // data
				false, // include legend
				true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(200);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(chartAlpha);
		plot.setNoDataMessage("Keine Gesprächsergebnisse gefunden");

		ChartPanel cp = new ChartPanel(chart);
		this.ergChartPanel.removeAll();
		this.ergChartPanel.add(cp, BorderLayout.CENTER);
		this.ergChartPanel.updateUI();
	}

	private void chartMFData() {
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();
		range.shiftWeeks(1);

		// get mafo
		ListItem ri = (ListItem) this.mafos.getSelectedItem();
		String mfl = ri.getKey0();
		String mafoSQL = "";
		if (mfl.trim().equals("allFT")) {
			mafoSQL = " AND g.shf=2";
		} else if (mfl.trim().equals("allSolar")) {
			mafoSQL = " AND g.shf=3";
		} else if (mfl.trim().length() > 0) {
			mafoSQL = " AND g.werbeleiter=" + mfl;
		}

		DefaultPieDataset result = new DefaultPieDataset();
		ResultSet rsErg = Database.select("g.werbeleiter, count(g.werbeleiter)", "gespraeche g, terminergebnisse e",
				"WHERE " + "g.ergebnis=e.id AND g.datum_vd>='" + range.getVon() + "' AND g.datum_vd<='"
						+ range.getBis() + "' " + mafoSQL + " GROUP BY g.werbeleiter");
		try {
			while (rsErg.next()) {
				String wl = rsErg.getString("g.werbeleiter");
				Projektleiter wele = Projektleiter.searchProjektleiter(wl);
				if (wele != null) {
					String mfName = wele.toString();
					int countErg = rsErg.getInt(2);
					result.setValue(mfName, countErg);
					// System.out.println(ergName+":"+countErg);
				}
			}
			Database.close(rsErg);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JFreeChart chart = ChartFactory.createPieChart("Anzahl Termine aller Werbeleiter", // chart
				// title
				result, // data
				false, // include legend
				true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(200);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(chartAlpha);
		plot.setNoDataMessage("Keine Gesprächsergebnisse gefunden");

		ChartPanel cp = new ChartPanel(chart);
		this.wlChartPanel.removeAll();
		this.wlChartPanel.add(cp, BorderLayout.CENTER);
		this.wlChartPanel.updateUI();
	}

	public void doStatistics() {
		this.parentWindow.setWaitCursor();
		this.collectData();
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
		Table2Excel xls = new Table2Excel(this.wleTable, "statisticsTMP.xls", "WL-Gesprächsergebnisse", fields);
		xls.openXLS();
	}

	public String getTabName() {
		return name;
	}
}
