package adminassistent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import tools.CBReport;
import tools.ContactBundle;
import tools.DateInterval;
import tools.DateTool;
import tools.ListItem;
import tools.MyMailLog;
import ui.IMainWindow;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.toedter.calendar.JDateChooser;

import db.DBTools;
import db.Database;
import db.Marktforscher;

public class CollectAdressPanel extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private JTextField plz;
	private JTextField stadt;
	private JComboBox kontaktArt;
	private JComboBox mdeList;
	private JComboBox terminProduktList;
	private JComboBox auftragProduktList;
	private JComboBox wdhList;
	private JCheckBox terminBox;
	private JCheckBox auftragBox;
	private JCheckBox mdeBox;
	private JCheckBox wdhBox;
	private JCheckBox useFTBox;
	private JCheckBox useSolarBox;
	private JCheckBox dateBox;
	private JCheckBox exportWithResult;
	private JCheckBox handleBigExportNormal;
	private JButton showContacts;
	private JButton makeMailing;
	private JButton delContacts;
	private JButton packContacts;
	private JDateChooser kontaktDate;
	private JTable orderTable;
	private ContactBundle searchContactBundle;
	private ContactBundleTableModel bundleTableModel;

	private boolean madeMailingExport = false;
	private IMainWindow parentWindow;

	public CollectAdressPanel(IMainWindow pw) {
		this.parentWindow = pw;
		this.createGUI();
	}

	private void createGUI() {
		JPanel mafoPanel = new JPanel(new BorderLayout());
		JPanel inputs = new JPanel(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(3, 9, 3, 9);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		double lWeight = 0.1;

		JLabel plzLabel = new JLabel("Postleitzahl");
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(plzLabel, gc);
		this.plz = new JTextField("");
		this.plz
				.setToolTipText("<html>Suchmuster für die Postleitzahl<br> <b>*</b> ersetzt mehrere Zeichen im Suchmuster<br> <b>?</b> ersetzt ein Zeichen im Suchmuster</html>");
		this.plz.addActionListener(this);
		// this.plz.setActionCommand("in");
		gc.gridx++;
		gc.gridwidth = 3;
		gc.weightx = 1;
		inputs.add(this.plz, gc);

		JLabel stadtLabel = new JLabel("Stadt");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(stadtLabel, gc);
		this.stadt = new JTextField("");
		this.stadt
				.setToolTipText("<html>Suchmuster für die Stadt<br> <b>*</b> ersetzt mehrere Zeichen im Suchmuster<br> <b>?</b> ersetzt ein Zeichen im Suchmuster</html>");
		this.stadt.addActionListener(this);
		// this.stadt.setActionCommand("in");
		gc.gridx++;
		gc.gridwidth = 3;
		gc.weightx = 1;
		inputs.add(this.stadt, gc);

		JLabel kontaktArtLabel = new JLabel("Kontaktart");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(kontaktArtLabel, gc);
		this.kontaktArt = new JComboBox(this.kontaktArtList());
		this.kontaktArt.setToolTipText("Die Art der Kontakte, die gesucht werden sollen");
		gc.gridx++;
		gc.gridwidth = 3;
		gc.weightx = 1;
		inputs.add(this.kontaktArt, gc);

		JLabel terminLabel = new JLabel("Gespräch");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(terminLabel, gc);
		this.terminBox = new JCheckBox();
		this.terminBox.setToolTipText("Sollen nur Kontakte berücksichtigt werden, die schon einen Gespräch hatten?");
		gc.gridx++;
		inputs.add(this.terminBox, gc);
		gc.gridx++;
		// JLabel lProd = new JLabel("Produkt", SwingConstants.RIGHT);
		JLabel lProd = new JLabel("Produkt");
		inputs.add(lProd, gc);
		gc.gridx++;
		this.terminProduktList = new JComboBox(DBTools.buildProduktList(true));
		inputs.add(this.terminProduktList, gc);

		JLabel auftragLabel = new JLabel("Auftrag");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(auftragLabel, gc);
		this.auftragBox = new JCheckBox();
		this.auftragBox.setToolTipText("Sollen nur Kontakte berücksichtigt werden, die schon einen Auftrag hatten?");
		gc.gridx++;
		inputs.add(this.auftragBox, gc);
		gc.gridx++;
		JLabel lAuftr = new JLabel("Produkt");
		inputs.add(lAuftr, gc);
		gc.gridx++;
		this.auftragProduktList = new JComboBox(DBTools.buildProduktList(true));
		inputs.add(this.auftragProduktList, gc);

		JLabel mdeLabel = new JLabel("Marktdatenermittler");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(mdeLabel, gc);
		this.mdeBox = new JCheckBox();
		this.mdeBox.setToolTipText("Sollen nur Kontakte berücksichtigt werden, die von einem bestimmten MDE erfasst wurden?");
		gc.gridx++;
		inputs.add(this.mdeBox, gc);
		gc.gridx++;
		gc.gridwidth++;
		this.mdeList = new JComboBox(DBTools.marktdatenermittlerList());
		inputs.add(this.mdeList, gc);

		JLabel wdhLabel = new JLabel("Wiedererkennungszeichen");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(wdhLabel, gc);
		this.wdhBox = new JCheckBox();
		this.wdhBox.setToolTipText("Ein Wiedererkennungsmerkmal auswählen");
		gc.gridx++;
		inputs.add(this.wdhBox, gc);
		gc.gridx++;
		gc.gridwidth++;
		this.wdhList = new JComboBox(DBTools.wiederErkennungsMerkmale());
		inputs.add(this.wdhList, gc);

		JLabel solarLabel = new JLabel("Welche Adressen:");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(solarLabel, gc);
		this.useFTBox = new JCheckBox("Fenster und Türen", true);
		this.useFTBox.setToolTipText("Nur Kontakte aufnehmen, die auch Fenster/Haustür fähig sind.?");
		gc.gridx++;
		inputs.add(this.useFTBox, gc);
		this.useSolarBox = new JCheckBox("Solar", false);
		this.useSolarBox.setToolTipText("Nur Kontakte aufnehmen, die Solar fähig sind.?");
		gc.gridx++;
		inputs.add(this.useSolarBox, gc);

		JLabel kontaktDateLabel = new JLabel("Letzter Kontakt vor dem:");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(kontaktDateLabel, gc);
		this.dateBox = new JCheckBox();
		this.dateBox.setToolTipText("Sollen nur Kontakte berücksichtigt die vor dem " + "einzugebenden Datum kontakiert worden sind?");
		gc.gridx++;
		inputs.add(this.dateBox, gc);
		this.kontaktDate = new JDateChooser();
		this.kontaktDate.setToolTipText("Es werden nur Adressen gesucht die schon einmal "
				+ "kontaktiert wurden und diese vor dem genannten Datum lag.");
		gc.gridx++;
		gc.gridwidth = 3;
		gc.weightx = 1;
		inputs.add(this.kontaktDate, gc);

		JLabel moreLabel = new JLabel("Weitere Einstellungen:");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		inputs.add(moreLabel, gc);
		this.exportWithResult = new JCheckBox("Export mit Ergebnis");
		this.exportWithResult.setToolTipText("Soll im ExcelExport das letzte Ergebnis angezeigt werden?");
		gc.gridx++;
		inputs.add(this.exportWithResult, gc);
		this.handleBigExportNormal = new JCheckBox("Export normal behandeln");
		this.handleBigExportNormal.setToolTipText("Soll der ExcelExport normal behandelt werden?");
		gc.gridx++;
		inputs.add(this.handleBigExportNormal, gc);

		JButton add15MaFos = new JButton("15 Adressen hinzufügen");
		add15MaFos.addActionListener(this);
		add15MaFos.setActionCommand("add15MaFos");
		add15MaFos.setToolTipText("Suche der Adressen starten");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 2;
		gc.gridheight = 2;
		gc.weightx = 1;
		gc.weighty = 1;
		inputs.add(add15MaFos, gc);

		JButton add30MaFos = new JButton("30 Adressen hinzufügen");
		add30MaFos.addActionListener(this);
		add30MaFos.setActionCommand("add30MaFos");
		add30MaFos.setToolTipText("Suche der Adressen starten");
		gc.gridx += 2;
		inputs.add(add30MaFos, gc);

		JButton add1000MaFos = new JButton("<html>1.000 Adressen hinzufügen <b>(EXPORT)</b></html>");
		add1000MaFos.addActionListener(this);
		add1000MaFos.setActionCommand("add1000MaFos");
		add1000MaFos.setToolTipText("Suche der Adressen starten");
		gc.gridx -= 2;
		gc.gridy += 3;
		inputs.add(add1000MaFos, gc);

		JButton add10000MaFos = new JButton("<html>10.000 Adressen hinzufügen <b>(EXPORT)</b></html>");
		add10000MaFos.addActionListener(this);
		add10000MaFos.setActionCommand("add10000MaFos");
		add10000MaFos.setToolTipText("Suche der Adressen starten");
		gc.gridx += 2;
		inputs.add(add10000MaFos, gc);

		// table
		JPanel tPanel = new JPanel(new BorderLayout());
		this.bundleTableModel = new ContactBundleTableModel();
		JPanel tablePanel = new JPanel(new BorderLayout());
		this.orderTable = new JTable(this.bundleTableModel);
		this.orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane inputScroller = new JScrollPane(this.orderTable);
		tablePanel.add(inputScroller);
		tPanel.add(tablePanel, BorderLayout.CENTER);

		// table action buttons
		JPanel tableButtonsPanel = new JPanel(new GridLayout(1, 0));

		this.makeMailing = new JButton("Mailing erstellen");
		this.makeMailing.addActionListener(this);
		this.makeMailing.setActionCommand("makemailing");
		this.makeMailing.setToolTipText("Für alle Adressen eine Mailingaktion anlegen");
		this.makeMailing.setEnabled(false);
		tableButtonsPanel.add(this.makeMailing);

		this.showContacts = new JButton("Exceldatei aller Adressen erstellen");
		this.showContacts.addActionListener(this);
		this.showContacts.setActionCommand("showcontacts");
		this.showContacts.setToolTipText("Erstelle Exceldatei der Adressen aller Adressgruppen");
		this.showContacts.setEnabled(false);
		tableButtonsPanel.add(this.showContacts);

		this.delContacts = new JButton("Adressgruppe entfernen");
		this.delContacts.addActionListener(this);
		this.delContacts.setActionCommand("delcontacts");
		this.delContacts.setToolTipText("Adressgruppe aus der Liste entfernen");
		this.delContacts.setEnabled(false);
		tableButtonsPanel.add(this.delContacts);

		this.packContacts = new JButton("Adressgruppen für MaFo bereitstellen");
		this.packContacts.addActionListener(this);
		this.packContacts.setActionCommand("packcontacts");
		this.packContacts.setToolTipText("Adressgruppen in der Datenbank für MaFo markieren");
		this.packContacts.setEnabled(false);
		tableButtonsPanel.add(this.packContacts);
		tPanel.add(tableButtonsPanel, BorderLayout.SOUTH);

		// put stuff into simpleframe an add them
		SimpleInternalFrame sifinputs = new SimpleInternalFrame("Eingaben", null, inputs);
		sifinputs.setPreferredSize(new Dimension(600, 380));
		int eb = 8;
		CompoundBorder cbi = new CompoundBorder(new EmptyBorder(eb, eb, 0, eb), sifinputs.getBorder());
		sifinputs.setBorder(cbi);
		SimpleInternalFrame siftable = new SimpleInternalFrame("Adressgruppen", null, tPanel);
		siftable.setPreferredSize(new Dimension(600, 250));
		CompoundBorder cbt = new CompoundBorder(new EmptyBorder(eb, eb, eb, eb), siftable.getBorder());
		siftable.setBorder(cbt);
		mafoPanel.add(sifinputs, BorderLayout.NORTH);
		mafoPanel.add(siftable, BorderLayout.CENTER);

		this.setLayout(new BorderLayout());
		this.add(mafoPanel);
	}

	// ================ helper =====================================

	/**
	 * type of contacts to consider in search
	 * 
	 * @return list of contacttypes
	 */
	public Vector<ListItem> kontaktArtList() {
		Vector<ListItem> ret = new Vector<ListItem>();
		ret.add(new ListItem("1", "Ohne Kontakt"));
		// ret.add(new ListItem("2", "Bisher einmal Kontakt"));
		// ret.add(new ListItem("3", "Bereits mehrmals Kontakt"));
		// ret.add(new ListItem("4", "Erstkontakte oder einmal Kontakt"));
		ret.add(new ListItem("5", "Ohne Kontakt oder mehrmals Kontakt"));
		return ret;
	}

	// ================ eventhandler =====================================

	@Override
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.endsWith("MaFos") || com.equals("in")) {
			int anz = 30;
			boolean doit = true;
			boolean bigExport = false;
			if (com.indexOf("15") > 0) {
				anz = 15;
				bigExport = false;
			} else if (com.indexOf("10000") > 0) {
				anz = 10000;
				bigExport = !this.handleBigExportNormal.isSelected();
				doit = false;
			} else if (com.indexOf("1000") > 0) {
				anz = 1000;
				bigExport = !this.handleBigExportNormal.isSelected();
				doit = false;
			}
			if (anz > 100) {
				JPasswordField passwordField = new JPasswordField(10);
				passwordField.setEchoChar('*');
				passwordField.requestFocus();
				String s = "Geben Sie das Passwort an:";
				JOptionPane.showMessageDialog(null, passwordField, s, JOptionPane.OK_OPTION);
				doit = new String(passwordField.getPassword()).equals("rossi315");
				this.madeMailingExport = true;
			}
			if (doit) {
				this.collectContactBundle(anz, bigExport);
			} else {
				JOptionPane.showMessageDialog(null, "Falsches Passwort!");
			}

		} else if (com.equals("makemailing")) {
			this.createMailing();

		} else if (com.equals("showcontacts")) {
			this.createReport();

		} else if (com.equals("delcontacts")) {
			this.removeContactBundle();

		} else if (com.equals("packcontacts")) {
			this.packAndTagContactBundle();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// only do stuff if swingworker is done
		if ("state".equals(evt.getPropertyName()) && evt.getNewValue().equals(StateValue.DONE)) {
			// here we come after contactbundle is ready with collecting data
			// beep if ready
			this.parentWindow.setDefaultCursor();

			Toolkit.getDefaultToolkit().beep();

			// test if contacts where found
			if (searchContactBundle.contactCount() > 0) {
				// add line to table
				this.bundleTableModel.addContactBundle(searchContactBundle);

				// set temp flag to bundle
				if (!searchContactBundle.isBigExcelExport()) {
					searchContactBundle.setStatusToTemp();
				}

				// select last added row
				int lastRow = this.bundleTableModel.getRowCount() - 1;
				this.orderTable.clearSelection();
				this.orderTable.addRowSelectionInterval(lastRow, lastRow);
				// toggle buttons
				if (this.madeMailingExport) {
					this.makeMailing.setEnabled(true);
					this.showContacts.setEnabled(false);
					this.packContacts.setEnabled(false);
				} else {
					this.makeMailing.setEnabled(false);
					this.showContacts.setEnabled(true);
					this.packContacts.setEnabled(true);
				}
				this.delContacts.setEnabled(true);
			} else {
				JOptionPane.showMessageDialog(null, "Es wurden keine Kontakte gefunden", "Hinweis", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	// ================ action =====================================

	/**
	 * collect ANZTOBUNDLE free contacts from db with given attributes, and show
	 * result in table.
	 * 
	 * @param bigExport
	 */
	private void collectContactBundle(int anz, boolean bigExport) {
		String plzIn = this.plz.getText();
		String stadtIn = this.stadt.getText();
		ListItem type = (ListItem) this.kontaktArt.getSelectedItem();
		if (bigExport) {
			type = new ListItem("9999", "Mailing-Export");
		}
		boolean useSolar = this.useSolarBox.isSelected();
		boolean useFT = this.useFTBox.isSelected();
		int shfFlag = 0;
		if (useSolar && useFT) {
			shfFlag = 1;
		} else if (useSolar) {
			shfFlag = 3;
		} else if (useFT) {
			shfFlag = 2;
		}
		boolean termin = this.terminBox.isSelected();
		String tProd = null;
		if (termin) {
			tProd = ((ListItem) this.terminProduktList.getSelectedItem()).getKey0();
		}
		boolean auftrag = this.auftragBox.isSelected();
		String aProd = null;
		if (auftrag) {
			aProd = ((ListItem) this.terminProduktList.getSelectedItem()).getKey0();
		}
		boolean mde = this.mdeBox.isSelected();
		String aMDE = null;
		if (mde) {
			aMDE = ((ListItem) this.mdeList.getSelectedItem()).getKey0();
		}
		boolean wdh = this.wdhBox.isSelected();
		String aWdh = null;
		if (wdh) {
			aWdh = ((ListItem) this.wdhList.getSelectedItem()).getKey0();
		}
		java.sql.Date priorDate = null;
		if (this.dateBox.isSelected() && this.kontaktDate.getDate() != null) {
			priorDate = new java.sql.Date(this.kontaktDate.getDate().getTime());
		} else if (this.dateBox.isSelected() && this.kontaktDate.getDate() == null) {

		}

		// start colecting adresses, propertychangeevent will be called, so look
		// at "propertyChange" what happens after exec
		searchContactBundle = new ContactBundle(plzIn, stadtIn, type, termin, auftrag, shfFlag, anz, tProd, aProd, priorDate, aMDE, aWdh,
				bigExport, false, this.parentWindow);
		searchContactBundle.addPropertyChangeListener(this);
		searchContactBundle.execute();
	}

	private void createMailing() {
		// ask for name
		String mailingName = JOptionPane.showInputDialog(null, "Geben Sie einen Namen für das Mailing an:");
		if (mailingName.length() > 0) {
			// create aktions
			ContactBundle allCB = mergeBundles();
			OVTAdmin.createMailingAktionen(allCB, mailingName);
		}
		// now we can have bernd made the excellist
		this.makeMailing.setEnabled(false);
		this.showContacts.setEnabled(true);
	}

	private void createReport() {
		ContactBundle allCB = mergeBundles();
		int ai = CBReport.NOAKTION;
		if (this.exportWithResult.isSelected()) {
			if (madeMailingExport) {
				ai = CBReport.LASTNOTMAILAKTION;
			} else {
				ai = CBReport.LASTAKTION;
			}
		}
		CBReport.printCBReport(allCB, null, ai, true, this.parentWindow);
	}

	private ContactBundle mergeBundles() {
		// do report for collected mafo-contacts
		ContactBundle allCB = new ContactBundle();
		for (int i = 0; i < this.bundleTableModel.getRowCount(); i++) {
			ContactBundle cb = (ContactBundle) this.bundleTableModel.getValueAt(i, 5);
			allCB.addBundleNoDoubles(cb);
			allCB.setLastSqlUsed(allCB.getLastSqlUsed() + "\n" + cb.getLastSqlUsed());
		}
		return allCB;
	}

	/**
	 * remove selected row from table
	 */
	private void removeContactBundle() {
		int selRow = this.orderTable.getSelectedRow();

		// set status to free
		ContactBundle cb = (ContactBundle) this.bundleTableModel.getValueAt(this.orderTable.getSelectedRow(), 5);
		cb.setStatusToFree();

		// remove row
		this.bundleTableModel.removeRow(selRow);

		// select another row
		if (this.bundleTableModel.getRowCount() > 0) {
			if (selRow > 0) {
				this.orderTable.addRowSelectionInterval(selRow - 1, selRow - 1);
			} else {
				this.orderTable.addRowSelectionInterval(selRow, selRow);
			}
		} else {
			// disable table action buttons when table is empty
			this.showContacts.setEnabled(false);
			this.makeMailing.setEnabled(false);
			this.delContacts.setEnabled(false);
			this.packContacts.setEnabled(false);
		}
	}

	/**
	 * pack all the stuf into an xmlfile and email it
	 */
	private void packAndTagContactBundle() {
		if (this.bundleTableModel.getRowCount() > 0) {
			// get mafo
			ListItem mafoItem = (ListItem) JOptionPane.showInputDialog(null,
					"Marktforscher auswählen, an den Kontakte geschickt werden sollen", "MF wählen", JOptionPane.PLAIN_MESSAGE, null,
					DBTools.mafoList().toArray(), "1");
			if (mafoItem != null) {
				Marktforscher mafo = Marktforscher.SearchMarktforscher(mafoItem.getKey0());
				DateInterval woche = DateTool.actualAbrechnungsWoche();
				int before = mafo.getProvidedContacts(woche);
				ContactBundle allCB = mergeBundles();
				int realCount = allCB.getContactCount();
				int bereitID = allCB.setStatusToMaFoAndMAfo(mafo);
				int after = mafo.getProvidedContacts(woche);
				int afterReal = Database.countQueryResult("kunden WHERE lastbereitID=" + bereitID);
				if (after - before != realCount || afterReal != realCount) {
					// tell anyone
					String message = "Bereitgestellt vorher: " + before + " nachher: " + after + ". Differenz: " + (after - before)
							+ " Soll: " + realCount + " bzw. " + afterReal + "\nInfomail wurde geschickt.";
					JOptionPane.showMessageDialog(this, message, "Problem beim Bereitstellen", JOptionPane.WARNING_MESSAGE);
					message += "\n";
					message += "mf: " + mafo + "\n";
					message += "bereitID: " + bereitID + "\n";
					message += allCB.idsString();
					message += "\n\n\n";
					message += allCB.getLastSqlUsed();
					MyMailLog.logErrorMail(message);
				}
				// reset table
				this.resetBundleTable();
			}
		}
	}

	/**
	 * remove alle contactbundles and disble all buttons
	 */
	private void resetBundleTable() {
		this.bundleTableModel.emptyMe();
		// disable table action buttons when table is empty
		this.showContacts.setEnabled(false);
		this.makeMailing.setEnabled(false);
		this.delContacts.setEnabled(false);
		this.packContacts.setEnabled(false);
	}
}

// ================ tablemodel =====================================
class ContactBundleTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -4624168342190201267L;

	public ContactBundleTableModel() {
		super();
		Vector<String> cn = new Vector<String>();
		cn.add(new String("PLZ"));
		cn.add(new String("Stadt"));
		cn.add(new String("Kontaktart"));
		cn.add(new String("Gespräch"));
		cn.add(new String("Auftrag"));
		cn.add(new String("Anzahl"));
		this.setColumnIdentifiers(cn);
	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void addContactBundle(ContactBundle cb) {
		Vector tableRow = new Vector();
		tableRow.add(cb.getPlz());
		tableRow.add(cb.getStadt());
		tableRow.add(cb.getType());
		tableRow.add(cb.isTermin());
		tableRow.add(cb.isAuftrag());
		tableRow.add(cb);
		this.addRow(tableRow);
	}

	public void emptyMe() {
		setRowCount(0);
	}
}