package statistics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tools.ListItem;
import tools.MyLog;
import tools.Table2Excel;
import ui.IMainWindow;
import db.DBTools;
import db.Database;

public class AuftragsPanel extends JPanel implements ActionListener {

	private static final String name = "Aufträge";

	private JButton exportTable;
	private JComboBox whatToDo;
	private StatisticErgebnisTable auftrTable;

	private IMainWindow parentWindow;

	public AuftragsPanel(IMainWindow pw) {
		this.parentWindow = pw;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel mf = new JPanel();
		JLabel mfTxt = new JLabel("Auswahl:");
		mf.add(mfTxt);
		Vector<ListItem> mListWithAll = new Vector<ListItem>();
		mListWithAll.add(new ListItem("", "Alle"));
		mListWithAll.addAll(DBTools.buildProduktList(false));
		mListWithAll.add(new ListItem("reg3", "Region Südhessen-Südliches Rheinland-Pfalz"));
		mListWithAll.add(new ListItem("reg2", "Region Mittel+Nord-Hessen-Nördliches Rheinland-Pfalz"));
		mListWithAll.add(new ListItem("reg4", "Region Baden-Württemberg"));
		mListWithAll.add(new ListItem("reg5", "Region Saarland"));
		this.whatToDo = new JComboBox(mListWithAll);
		this.whatToDo.setActionCommand("what");
		this.whatToDo.addActionListener(this);
		mf.add(this.whatToDo);

		JButton goButton = new JButton("Aktualisieren");
		goButton.setActionCommand("start");
		goButton.addActionListener(this);
		mf.add(goButton);
		this.add(mf, BorderLayout.NORTH);

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Kunde"));
		cn.add(new String("PLZ"));
		cn.add(new String("Stadt"));
		cn.add(new String("Straße"));
		cn.add(new String("Auftragsdatum"));
		cn.add(new String("Lieferdatum"));
		cn.add(new String("Produkt"));
		cn.add(new String("Auftragsnehmer"));
		cn.add(new String("VK"));
		int c = 0;
		short[] colWidths = new short[9];
		colWidths[c++] = 100;
		colWidths[c++] = 70;
		colWidths[c++] = 100;
		colWidths[c++] = 100;
		colWidths[c++] = 100;
		colWidths[c++] = 80;
		colWidths[c++] = 100;
		colWidths[c++] = 100;
		colWidths[c++] = 40;
		this.auftrTable = new StatisticErgebnisTable(cn, null, colWidths);
		JScrollPane tabScroller = new JScrollPane(this.auftrTable);
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
		this.auftrTable.getTableModel().emptyMe();
		this.exportTable.setEnabled(true);

		// get what to do
		ListItem ri = (ListItem) this.whatToDo.getSelectedItem();
		String what = ri.getKey0();

		String wc = "";
		if (what.startsWith("reg")) {
			wc = " AND r.region=" + what.substring(3, what.length());
		} else if (what.length() > 0) {
			wc = " AND p.id=" + what;
		}

		// retrieve data
		try {
			ResultSet rsAll = Database.select("*, CONCAT(k.strasse, ' ' , k.hausnummer) as strhnr",
					"auftraege a, kunden k, produkte p, werbeleiter w, plz2region r",
					"WHERE a.kunde=k.id AND a.produkt=p.id AND a.verkaeufer=w.id AND k.plz=r.plz " + wc + " ORDER BY a.datum");
			// "AND a.datum>='"+range.getVon()+"' AND
			// a.datum<='"+range.getBis()+"' "+
			while (rsAll.next()) {
				Vector<String> allRow = new Vector<String>();
				allRow.add(rsAll.getString("k.nachname"));
				allRow.add(rsAll.getString("k.plz"));
				allRow.add(rsAll.getString("k.stadt"));
				allRow.add(rsAll.getString("strhnr"));
				allRow.add(rsAll.getString("a.datum"));
				allRow.add(rsAll.getString("a.lieferung"));
				allRow.add(rsAll.getString("p.name"));
				allRow.add(rsAll.getString("a.auftragnehmer"));
				allRow.add(rsAll.getString("w.kurzname"));
				this.auftrTable.addRow(allRow);
			}
			Database.close(rsAll);

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
		Table2Excel xls = new Table2Excel(this.auftrTable, "statisticsTMP.xls", "Auträge", fields);
		xls.openXLS();
	}

	public String getTabName() {
		return name;
	}
}
