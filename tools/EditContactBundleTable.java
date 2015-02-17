package tools;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import ui.ToggleTableCellRenderer;
import db.Contact;
import db.DBTools;

class EditContactBundleTable extends JTable implements Runnable {

	private ContactBundle		cb;
	private BundleTableModel	bundleTableModel;

	public EditContactBundleTable() {
		this.bundleTableModel = new BundleTableModel();
		this.setModel(this.bundleTableModel);
		ListSelectionModel rowSM = this.getSelectionModel();
		rowSM.addListSelectionListener(this.bundleTableModel);
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ToggleTableCellRenderer cellRenderer = new ToggleTableCellRenderer();
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), cellRenderer);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		TableColumnModel tc = this.getColumnModel();
		int c = 0;
		tc.getColumn(c++).setPreferredWidth(50); // key
		tc.getColumn(c++).setPreferredWidth(70); // nn
		tc.getColumn(c++).setPreferredWidth(70); // vn
		tc.getColumn(c++).setPreferredWidth(70); // str
		tc.getColumn(c++).setPreferredWidth(20); // hnr
		tc.getColumn(c++).setPreferredWidth(30); // plz
		tc.getColumn(c++).setPreferredWidth(80); // stadt
		tc.getColumn(c++).setPreferredWidth(50); // mde
		tc.getColumn(c++).setPreferredWidth(80); // bereitgestellt
		tc.getColumn(c++).setPreferredWidth(30); // aktionen
		tc.getColumn(c++).setPreferredWidth(30); // gesprÃ¤che
	}

	public void run() {
		if (this.cb != null) {
			this.bundleTableModel.emptyMe();
			for (Iterator<Contact> iter = this.cb.getContacts().iterator(); iter.hasNext();) {
				Contact c = iter.next();
				this.bundleTableModel.addContactRow(c);
			}
		}
	}

	public BundleTableModel getBundleTableModel() {
		return bundleTableModel;
	}

	public void setBundleTableModel(BundleTableModel bundleTabelModel) {
		this.bundleTableModel = bundleTabelModel;
	}

	public ContactBundle getCb() {
		return cb;
	}

	public void setCb(ContactBundle cb) {
		this.cb = cb;
	}
}

// ================ tablemodel =====================================
class BundleTableModel extends DefaultTableModel implements ListSelectionListener {

	public BundleTableModel() {
		super();
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Schlüssel"));
		cn.add(new String("Nachname"));
		cn.add(new String("Vorname"));
		cn.add(new String("Straße"));
		cn.add(new String("H.nr."));
		cn.add(new String("PLZ"));
		cn.add(new String("Stadt"));
		cn.add(new String("ME"));
		cn.add(new String("Bereitgestellt"));
		cn.add(new String("A. #"));
		cn.add(new String("G. #"));
		this.setColumnIdentifiers(cn);
	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void addContactRow(Contact c) {
		Vector tableRow = new Vector();
		tableRow.add(c.getId());
		tableRow.add(c.getNachName());
		tableRow.add(c.getVorName());
		tableRow.add(c.getStrasse());
		tableRow.add(c.getHausnr());
		tableRow.add(c.getPlz());
		tableRow.add(c.getStadt());
		tableRow.add(DBTools.nameOfMDE(c.getMde(), false));
		tableRow.add(c.getBereitgestellt());
		tableRow.add(c.getAktionenFromDB(true).size());
		tableRow.add(c.getGespraeche().size());

		this.addRow(tableRow);
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
