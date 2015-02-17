package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import statistics.StatisticErgebnisTable;
import tools.MyLog;

import com.toedter.calendar.JDateChooser;

import db.DBTools;
import db.Database;
import db.Marktforscher;

public class FreeContactsPanel extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -1703469068776343089L;
	private JDateChooser dateChooser;
	private JTextField plzInput;
	private StatisticErgebnisTable viewTable;
	private String whatToDo;
	private IMainWindow parentWindow;

	public FreeContactsPanel(IMainWindow pw) {
		this.parentWindow = pw;
		this.setLayout(new BorderLayout());
		this.add(this.makeContentPanel());
	}

	private Component makeContentPanel() {
		JPanel retPanel = new JPanel(new BorderLayout());
		JPanel inputPanel = new JPanel(new GridLayout(1, 0));
		inputPanel.add(new JLabel("Kontakte freistellen ab:"));

		this.dateChooser = new JDateChooser();
		inputPanel.add(this.dateChooser);

		inputPanel.add(new JLabel("aus:"));

		this.plzInput = new JTextField();
		inputPanel.add(this.plzInput);

		JButton goForAll = new JButton("Suchen");
		goForAll.setActionCommand("collect");
		goForAll.addActionListener(this);
		inputPanel.add(goForAll);

		retPanel.add(inputPanel, BorderLayout.NORTH);

		JPanel tablePanel = new JPanel(new BorderLayout());
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Marktforscher"));
		cn.add(new String("Anzahl Adressen"));
		int idx = 0;
		short[] cnw = new short[6];
		cnw[idx++] = 400;
		cnw[idx++] = 400;
		this.viewTable = new StatisticErgebnisTable(cn, null, cnw);
		JScrollPane tabScroller = new JScrollPane(this.viewTable);
		tablePanel.add(tabScroller, BorderLayout.CENTER);
		retPanel.add(tablePanel, BorderLayout.CENTER);

		JButton doit = new JButton("Freistellen");
		doit.setActionCommand("freethem");
		doit.addActionListener(this);
		retPanel.add(doit, BorderLayout.SOUTH);

		return retPanel;
	}

	public String getTabName() {
		return "Freistellen";
	}

	public void actionPerformed(ActionEvent e) {
		this.whatToDo = e.getActionCommand();
		// not much to do just start
		new Thread(this).start();
	}

	public void run() {
		this.parentWindow.setWaitCursor();
		boolean doit = this.whatToDo.equals("freethem");

		this.viewTable.emptyMe();
		// fill table
		Vector<Marktforscher> mafoList = DBTools.mafoMafoList(false);
		for (Iterator<Marktforscher> iter = mafoList.iterator(); iter.hasNext();) {
			int count = 0;
			Marktforscher mafo = iter.next();

			Vector<String> aRow = new Vector<String>();
			aRow.add(mafo.toString());

			// search for contacts
			try {
				java.sql.Date dd = new java.sql.Date(this.dateChooser.getDate().getTime());
				String plz = this.plzInput.getText();
				if (plz.length() > 0) {
					plz = " AND k.plz LIKE '" + plz + "'";
					plz = plz.replace("*", "%");
				}
				ResultSet rs = Database.select("k.id, k.bearbeitungsstatus, k.bereitgestellt", "kunden k", "WHERE k.marktforscher="
						+ mafo.getId() + " AND k.bereitgestellt>='" + dd + "' AND "
						+ "k.notiz='' AND (k.bearbeitungsstatus=1 OR k.bearbeitungsstatus=2) " + plz);
				while (rs.next()) {
					// set contact back
					if (doit) {
						rs.updateString("k.bearbeitungsstatus", "0");
						rs.updateDate("k.bereitgestellt", null);
						rs.updateRow();
					}
					count++;
				}
				Database.close(rs);
				if (doit && count > 0) {
					Date nowDate = new Date(new java.util.Date().getTime());
					Database.quickInsert("bereitgestellt", "NULL, '" + nowDate + "', " + mafo.getId() + ", " + (-1 * count) + ", 'reset: "
							+ nowDate + "'");
				}
				aRow.add(Integer.toString(count));

			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			if (count > 0) {
				this.viewTable.addRow(aRow);
			}
		}
		this.parentWindow.setDefaultCursor();
	}
}
