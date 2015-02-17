package tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import db.Marktforscher;

public class MafoInfoTable extends JTable implements Runnable {

	private Marktforscher mafo;
	private MFInfoTableModel mfiTableModel;

	public MafoInfoTable(Marktforscher mafo) {
		this.mafo = mafo;
		this.mfiTableModel = new MFInfoTableModel();
		this.setModel(this.mfiTableModel);
		ListSelectionModel rowSM = this.getSelectionModel();
		rowSM.addListSelectionListener(this.mfiTableModel);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		MITTableCellRenderer cellRenderer = new MITTableCellRenderer();
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), cellRenderer);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		// sizes
		this.setRowHeight(40);
		TableColumnModel tc = this.getColumnModel();
		tc.getColumn(0).setPreferredWidth(250); // zeitraum
		tc.getColumn(4).setPreferredWidth(100); // nicht hono
	}

	@SuppressWarnings("unchecked")
	public void run() {
		this.mfiTableModel.emptyMe();

		// collect date stuff
		DateInterval jahr = DateTool.actualAbrechnungsJahr();
		DateInterval monat = DateTool.actualAbrechnungsMonat();
		DateInterval woche = DateTool.actualAbrechnungsWoche();

		// add year row
		Vector tableRow = new Vector();
		tableRow.add(jahr.toString());
		int bereit = this.mafo.getProvidedContacts(jahr);
		tableRow.add(Integer.toString(bereit));
		tableRow.add("-");
		int fini = this.mafo.getFinishedContactsCount(jahr);
		int nomo = this.mafo.getNoMoneyContactsCount(jahr);
		tableRow.add(Integer.toString(fini));
		tableRow.add(Integer.toString(nomo));
		tableRow.add(Integer.toString(fini - nomo));
		this.mfiTableModel.addRow(tableRow);
		MyLog.logDebug("mfstatistic(year): " + bereit + "|" + fini + "|" + nomo);

		// add month row
		tableRow = new Vector();
		tableRow.add(monat.toString());
		bereit = this.mafo.getProvidedContacts(monat);
		tableRow.add(Integer.toString(bereit));
		tableRow.add("-");
		fini = this.mafo.getFinishedContactsCount(monat);
		nomo = this.mafo.getNoMoneyContactsCount(monat);
		tableRow.add(Integer.toString(fini));
		tableRow.add(Integer.toString(nomo));
		tableRow.add(Integer.toString(fini - nomo));
		this.mfiTableModel.addRow(tableRow);
		MyLog.logDebug("mfstatistic(month): " + bereit + "|" + fini + "|" + nomo);

		// add week row
		tableRow = new Vector();
		tableRow.add(woche.toString());
		tableRow.add(Integer.toString(this.mafo.getProvidedContacts(woche)));
		tableRow.add(Integer.toString(this.mafo.getWaitingContactsCount()));
		fini = this.mafo.getFinishedContactsCount(woche);
		nomo = this.mafo.getNoMoneyContactsCount(woche);
		tableRow.add(Integer.toString(fini));
		tableRow.add(Integer.toString(nomo));
		tableRow.add(Integer.toString(fini - nomo));
		this.mfiTableModel.addRow(tableRow);
		MyLog.logDebug("mfstatistic(week): " + fini + "|" + nomo);
	}

	public MFInfoTableModel getMfiTableModel() {
		return mfiTableModel;
	}

	public void setMfiTableModel(MFInfoTableModel bundleTabelModel) {
		this.mfiTableModel = bundleTabelModel;
	}

}

// ================ tablemodel =====================================
class MFInfoTableModel extends DefaultTableModel implements ListSelectionListener {

	public MFInfoTableModel() {
		super();
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Zeitraum"));
		cn.add(new String("Bereitgestellt"));
		cn.add(new String("Wartend"));
		cn.add(new String("Fertig"));
		cn.add(new String("Nicht honorarfähig"));
		cn.add(new String("Honorarfähig"));
		this.setColumnIdentifiers(cn);
	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;
		return ret;
	}

	public void emptyMe() {
		setRowCount(0);
	}

	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (lsm.isSelectionEmpty()) {
			// no rows are selected
			// do notting here...
		} else {
			// selectedRow is selected
		}
	}
}

// ============ cell rendere, for alternating cell color
// ========================
class MITTableCellRenderer extends DefaultTableCellRenderer {
	public static final long serialVersionUID = 0;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		cell.setFont(new Font("Tahoma", Font.BOLD, 20));
		cell.setHorizontalAlignment(SwingConstants.CENTER);
		if (column == 4) {
			cell.setForeground(Color.RED);
		} else if (column == 5) {
			cell.setForeground(Color.GREEN);
		} else {
			cell.setForeground(Color.BLACK);

		}

		return cell;
	}
}