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
import db.Gespraech;

class EditGespraecheTable extends JTable implements Runnable {

	private Vector<Gespraech>		gespraeche;
	private GespraecheTableModel	gespraecheTableModel;

	public EditGespraecheTable(Vector<Gespraech> gespraeche) {
		this.gespraeche = gespraeche;
		this.gespraecheTableModel = new GespraecheTableModel();
		this.setModel(this.gespraecheTableModel);
		ListSelectionModel rowSM = this.getSelectionModel();
		rowSM.addListSelectionListener(this.gespraecheTableModel);
		this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		ToggleTableCellRenderer cellRenderer = new ToggleTableCellRenderer();
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), cellRenderer);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

		TableColumnModel tc = this.getColumnModel();
		int c = 0;
		tc.getColumn(c++).setPreferredWidth(20); // key
		tc.getColumn(c++).setPreferredWidth(120); // kunde
		tc.getColumn(c++).setPreferredWidth(45); // angelegt
		tc.getColumn(c++).setPreferredWidth(45); // md
		tc.getColumn(c++).setPreferredWidth(45); // vd
		tc.getColumn(c++).setPreferredWidth(45); // vd
		tc.getColumn(c++).setPreferredWidth(50); // wele
		tc.getColumn(c++).setPreferredWidth(30); // mde
		tc.getColumn(c++).setPreferredWidth(100); // ergebnis
	}

	public void run() {
		this.gespraecheTableModel.emptyMe();
		for (Iterator<Gespraech> iter = this.gespraeche.iterator(); iter.hasNext();) {
			Gespraech c = iter.next();
			this.gespraecheTableModel.addGespraechRow(c);
		}
	}

	public GespraecheTableModel getGespraecheTableModel() {
		return gespraecheTableModel;
	}

	public void setBundleTableModel(GespraecheTableModel gespraecheTableModel) {
		this.gespraecheTableModel = gespraecheTableModel;
	}

	public void loadContacts() {
		this.run();

		// Thread t = new Thread(this);
		// t.start();
	}

	public Vector<Gespraech> getGespraeche() {
		return gespraeche;
	}

	public void setGespraeche(Vector<Gespraech> gespraeche) {
		this.gespraeche = gespraeche;
	}
}

// ================ tablemodel =====================================
class GespraecheTableModel extends DefaultTableModel implements ListSelectionListener {

	public GespraecheTableModel() {
		super();
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Schlüssel"));
		cn.add(new String("Kunde"));
		cn.add(new String("Angelegt"));
		cn.add(new String("MD"));
		cn.add(new String("VD"));
		cn.add(new String("VD-Zeit"));
		cn.add(new String("Projektleiter"));
		cn.add(new String("ME"));
		cn.add(new String("Ergebnis"));
		this.setColumnIdentifiers(cn);
	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void addGespraechRow(Gespraech g) {
		Vector tableRow = new Vector();
		tableRow.add(g);
		tableRow.add(Contact.SearchContact(g.getKundeID()).toStringNoID());
		tableRow.add(g.getDatumangelegt());
		tableRow.add(g.getDatumMF());
		tableRow.add(g.getDatumWele());
		tableRow.add(g.getTerminZeit());
		tableRow.add(DBTools.nameOfWL(g.getProjektleiter(), false));
		tableRow.add(DBTools.nameOfMDE(g.getMde(), false));
		String erg = g.getErgebnis();
		if (erg.equals("19")) {
			tableRow.add(g.getVertragsBruttoSumme());
		} else {
			tableRow.add(DBTools.kurzNameOfTerminErgebnis(erg));
		}
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
