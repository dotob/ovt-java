package statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import tools.ListItem;
import tools.MyLog;
import ui.IMainWindow;
import ui.ToggleTableCellRenderer;

import com.toedter.calendar.JDateChooser;

import db.Database;

public class GesamtUebersichtPanel extends JPanel implements ActionListener, ItemListener {

	private static final String name = "Adressenanzahl";

	private JComboBox whatToDo;
	private StatisticErgebnisTable aktionTerminAuftragTable;
	private double countAll;
	private JTextField plzInput;
	private JLabel plzTxt;
	private JDateChooser dateChooser;
	private JCheckBox useDate;
	private IMainWindow parentWindow;
	private StatisticErgebnisTable otherTable;

	public GesamtUebersichtPanel(IMainWindow pw) {
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

		JPanel kriterien = new JPanel();
		JLabel mfTxt = new JLabel("Wo:");
		kriterien.add(mfTxt);
		Vector<ListItem> mListWithAll = new Vector<ListItem>();
		mListWithAll.add(new ListItem("all", "Gesamt"));
		mListWithAll.add(new ListItem("2", "Mittel+Nord-Hessen-Nördliches Rheinland-Pfalz"));
		mListWithAll.add(new ListItem("3", "Südhessen-Südliches Rheinland-Pfalz"));
		mListWithAll.add(new ListItem("4", "Baden-Württemberg"));
		mListWithAll.add(new ListItem("5", "Saarland"));
		mListWithAll.add(new ListItem("plz", "Nach Postleitzahl..."));
		this.whatToDo = new JComboBox(mListWithAll);
		this.whatToDo.setActionCommand("what");
		this.whatToDo.addActionListener(this);
		kriterien.add(this.whatToDo);
		this.plzTxt = new JLabel("PLZ:");
		this.plzTxt.setEnabled(false);
		kriterien.add(this.plzTxt);
		this.plzInput = new JTextField(7);
		this.plzInput.setEnabled(false);
		kriterien.add(this.plzInput);
		this.useDate = new JCheckBox();
		this.useDate.addItemListener(this);
		// TODO: reactivate use date checkbox
		this.useDate.setEnabled(false);
		kriterien.add(this.useDate);
		this.dateChooser = new JDateChooser(new java.util.Date());
		this.dateChooser.setPreferredSize(new Dimension(130, 20));
		this.dateChooser.setEnabled(false);
		kriterien.add(this.dateChooser);

		// tables
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Aktion"));
		cn.add(new String("Gespräch"));
		cn.add(new String("Auftrag"));
		cn.add(new String("Anzahl FHT/Solar"));
		cn.add(new String("Anzahl Solar"));
		cn.add(new String("Anzahl FHT"));
		cn.add(new String("Anzahl Gesamt"));
		cn.add(new String("Prozent Gesamt"));
		int idx = 0;
		short[] cnw = new short[8];
		cnw[idx++] = 50;
		cnw[idx++] = 50;
		cnw[idx++] = 50;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		this.aktionTerminAuftragTable = new StatisticErgebnisTable(cn, new ToggleTableCellRenderer(true), cnw);

		cn = new Vector<String>();
		cn.add(new String("Was"));
		cn.add(new String("Anzahl FHT/Solar"));
		cn.add(new String("Anzahl Solar"));
		cn.add(new String("Anzahl FHT"));
		cn.add(new String("Anzahl Gesamt"));
		cn.add(new String("Prozent Gesamt"));
		idx = 0;
		cnw = new short[6];
		cnw[idx++] = 300;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		cnw[idx++] = 80;
		this.otherTable = new StatisticErgebnisTable(cn, new ToggleTableCellRenderer(true), cnw);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(this.aktionTerminAuftragTable), new JScrollPane(
				this.otherTable));
		splitPane.setDividerLocation(200);

		this.add(splitPane, BorderLayout.CENTER);

		JButton goButton = new JButton("Aktualisieren");
		goButton.setActionCommand("start");
		goButton.addActionListener(this);
		kriterien.add(goButton);
		headPanel.add(kriterien, BorderLayout.SOUTH);
		this.add(headPanel, BorderLayout.NORTH);
	}

	/**
	 * collect the counts of ergebnisses and group of ergebnisses
	 */
	private void collectData() {
		this.aktionTerminAuftragTable.getTableModel().emptyMe();
		this.otherTable.getTableModel().emptyMe();

		java.util.Date before = null;
		if (this.useDate.isSelected()) {
			before = this.dateChooser.getDate();
		}

		// get what to do
		ListItem ri = (ListItem) this.whatToDo.getSelectedItem();
		String theRegion = ri.getKey0();

		String tables = "kunden k, plz2region p";
		String wc = "";
		String awc = "";
		String infoAll = "";
		if (theRegion.equals("all")) {
			tables = "kunden k";
			awc = " 1=1 ";
			wc = awc;
		} else if (theRegion.equals("plz")) {
			wc = " k.plz LIKE '" + this.plzInput.getText().replace('*', '%').replace('?', '_') + "'";
			awc = wc;
			tables = "kunden k";
			infoAll = this.plzInput.getText();
		} else {
			// is region here
			wc = " p.region=" + theRegion;
			awc = "k.plz=p.plz AND " + wc;
			wc = awc;
		}
		String grpSQL = " GROUP BY k.shfflag ORDER BY k.shfflag";

		// retrieve data
		try {
			collectOtherData(before, ri, tables, wc, awc, infoAll, grpSQL);
			collectAktionTerminAuftragData(before, ri, tables, wc, awc, infoAll, grpSQL);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}

	private void collectAktionTerminAuftragData(Date before, ListItem ri, String tables, String wc, String awc, String infoAll,
			String grpSQL) throws SQLException {

		collectOnePermutation(tables, awc, grpSQL, "=0", "=0", "=0");
		collectOnePermutation(tables, awc, grpSQL, ">=1", "=0", "=0");
		collectOnePermutation(tables, awc, grpSQL, "=0", ">=1", "=0");
		collectOnePermutation(tables, awc, grpSQL, "=0", "=0", ">=1");
		collectOnePermutation(tables, awc, grpSQL, ">=1", ">=1", "=0");
		collectOnePermutation(tables, awc, grpSQL, ">=1", ">=1", ">=1");
		collectOnePermutation(tables, awc, grpSQL, "=0", ">=1", ">=1");
		collectOnePermutation(tables, awc, grpSQL, ">=1", "=0", ">=1");
	}

	private void collectOnePermutation(String tables, String awc, String grpSQL, String hasaktion, String hastermin, String hasauftrag)
			throws SQLException {
		ResultSet rsAll = Database.select("count(*) as permutations, k.shfflag", tables, "WHERE hasaktion" + hasaktion
				+ " AND hasgespraech" + hastermin + " AND hasauftrag" + hasauftrag + " AND " + awc + grpSQL);
		int bothCount = 0;
		int solarCount = 0;
		int fhtCount = 0;
		while (rsAll.next()) {
			switch (rsAll.getInt("k.shfflag")) {
			case 1:
				bothCount = rsAll.getInt(1);
				break;
			case 2:
				fhtCount = rsAll.getInt(1);
				break;
			case 3:
				solarCount = rsAll.getInt(1);
				break;
			default:
				break;
			}
		}
		int allthree = bothCount + solarCount + fhtCount;
		Vector<String> allRow = new Vector<String>();
		allRow.add(hasaktion.equals("=0") ? "" : "x");
		allRow.add(hastermin.equals("=0") ? "" : "x");
		allRow.add(hasauftrag.equals("=0") ? "" : "x");
		allRow.add(Integer.toString(bothCount));
		allRow.add(Integer.toString(solarCount));
		allRow.add(Integer.toString(fhtCount));
		allRow.add(Integer.toString((int) allthree));
		if (this.countAll > 0) {
			BigDecimal tmp = new BigDecimal(100 / (this.countAll / allthree));
			tmp = tmp.setScale(2, BigDecimal.ROUND_HALF_UP);
			allRow.add(tmp.toString() + " %");
		} else {
			allRow.add("0 %");
		}
		this.aktionTerminAuftragTable.addRow(allRow);
		Database.close(rsAll);
	}

	private void collectOtherData(java.util.Date before, ListItem ri, String tables, String wc, String awc, String infoAll, String grpSQL)
			throws SQLException {
		BigDecimal tmp2;

		// count all
		this.countAll = 0;
		int bothCount = 0;
		int solarCount = 0;
		int fhtCount = 0;
		ResultSet rsAll = Database.select("count(*) as allcontacts, k.shfflag", tables, "WHERE " + awc + grpSQL);
		while (rsAll.next()) {
			this.countAll += rsAll.getInt(1);
			switch (rsAll.getInt("k.shfflag")) {
			case 1:
				bothCount = rsAll.getInt(1);
				break;
			case 2:
				fhtCount = rsAll.getInt(1);
				break;
			case 3:
				solarCount = rsAll.getInt(1);
				break;
			default:
				break;
			}
		}
		Database.close(rsAll);
		Vector<String> allRow = new Vector<String>();
		allRow.add(ri.getValue() + infoAll);
		allRow.add(Integer.toString(bothCount));
		allRow.add(Integer.toString(solarCount));
		allRow.add(Integer.toString(fhtCount));
		allRow.add(Integer.toString((int) this.countAll));
		allRow.add("100.00 %");
		this.otherTable.addRow(allRow);

		int allDirty = 0;
		bothCount = 0;
		fhtCount = 0;
		solarCount = 0;
		ResultSet rsDirty = Database.select("count(*) as gesperrt, k.shfflag", tables, "WHERE k.bearbeitungsstatus=99 AND " + awc + grpSQL);
		while (rsDirty.next()) {
			allDirty += rsDirty.getInt(1);
			switch (rsDirty.getInt("k.shfflag")) {
			case 1:
				bothCount = rsDirty.getInt(1);
				break;
			case 2:
				fhtCount = rsDirty.getInt(1);
				break;
			case 3:
				solarCount = rsDirty.getInt(1);
				break;
			default:
				break;
			}
		}
		Database.close(rsDirty);

		allRow = new Vector<String>();
		allRow.add("gesperrt (Bearbeitungsstatus=99)");
		allRow.add(Integer.toString(bothCount));
		allRow.add(Integer.toString(solarCount));
		allRow.add(Integer.toString(fhtCount));
		allRow.add(Integer.toString((int) allDirty));
		if (this.countAll > 0) {
			tmp2 = new BigDecimal(100 / (this.countAll / allDirty));
			tmp2 = tmp2.setScale(2, BigDecimal.ROUND_HALF_UP);
			allRow.add(tmp2.toString() + " %");
		} else {
			allRow.add("0 %");
		}
		this.otherTable.addRow(allRow);
	}

	public void doStatistics() {
		this.parentWindow.setWaitCursor();
		this.collectData();
		this.parentWindow.setDefaultCursor();
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("start")) {
			this.doStatistics();

		} else if (arg0.getActionCommand().equals("usedate")) {
			System.out.println("usedate");
			this.dateChooser.setEnabled(this.useDate.isSelected());

		} else if (arg0.getActionCommand().equals("what")) {
			JComboBox jc = (JComboBox) arg0.getSource();
			ListItem li = (ListItem) jc.getSelectedItem();
			String what = li.getKey0();
			if (what.equals("plz")) {
				this.plzTxt.setEnabled(true);
				this.plzInput.setEnabled(true);
			} else {
				this.plzTxt.setEnabled(false);
				this.plzInput.setEnabled(false);
			}
		}
	}

	public void itemStateChanged(ItemEvent arg0) {
		System.out.println("itemStateChanged");
		this.dateChooser.setEnabled(this.useDate.isSelected());
	}

	public String getTabName() {
		return name;
	}

}
