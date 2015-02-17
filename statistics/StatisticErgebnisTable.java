package statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import tools.MyLog;

public class StatisticErgebnisTable extends JTable {

	private StatisticErgebnisTableModel tableModel;

	public StatisticErgebnisTable(Vector cn) {
		this.tableModel = new StatisticErgebnisTableModel(cn);
		this.setModel(this.tableModel);
		MFETableCellRenderer cellRenderer = new MFETableCellRenderer();
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), cellRenderer);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		TableColumnModel tc = this.getColumnModel();
		int c = 0;
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(150);
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(160);
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(50);
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(50);
	}

	public StatisticErgebnisTable(Vector cn, DefaultTableCellRenderer dtcr) {
		this.tableModel = new StatisticErgebnisTableModel(cn);
		this.setModel(this.tableModel);
		if (dtcr == null) {
			dtcr = new MFETableCellRenderer();
		}
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), dtcr);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		TableColumnModel tc = this.getColumnModel();
		int c = 0;
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(150);
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(160);
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(50);
		if (tc.getColumnCount() < c)
			tc.getColumn(c++).setPreferredWidth(50);
	}

	public StatisticErgebnisTable(Vector cn, DefaultTableCellRenderer dtcr, short[] colWidths) {
		this.tableModel = new StatisticErgebnisTableModel(cn);
		this.setModel(this.tableModel);
		if (dtcr == null) {
			dtcr = new MFETableCellRenderer();
		}
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), dtcr);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		TableColumnModel tc = this.getColumnModel();
		int c = 0;
		for (int i = 0; i < tc.getColumnCount() && i < colWidths.length; i++) {
			tc.getColumn(c++).setPreferredWidth(colWidths[i]);
		}
	}

	public void addRow(Vector data) {
		this.tableModel.addRow(data);
	}

	public StatisticErgebnisTableModel getTableModel() {
		return tableModel;
	}

	public void emptyMe() {
		this.tableModel.emptyMe();
	}
}

// ##########################################################

class StatisticErgebnisTableModel extends DefaultTableModel {

	public StatisticErgebnisTableModel(Vector cn) {
		super();

		this.setColumnIdentifiers(cn);

	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;
		return ret;
	}

	public void emptyMe() {
		setRowCount(0);
	}
}

// ============ cell renderer, for alternating cell color
// ========================

class MFETableCellRenderer extends DefaultTableCellRenderer {
	public static final long serialVersionUID = 0;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Color highlight = new Color(235, 240, 255);
		if (!isSelected) {
			int tmp = row % 2;
			if (tmp == 0)
				cell.setBackground(highlight);
			else
				cell.setBackground(Color.white);
			// if first column is filled, then it is group line, so make it fat
			String inh = (String) table.getValueAt(row, 0);
			if (inh != null && inh.length() > 0) {
				cell.setFont(new Font("Tahoma", Font.BOLD, 12));
			}
		}

		JLabel txt = (JLabel) cell;
		if (column > 0) {
			txt.setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			txt.setHorizontalAlignment(SwingConstants.LEFT);

		}
		return cell;
	}
}
