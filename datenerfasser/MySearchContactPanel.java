package datenerfasser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import tools.ContactBundle;
import tools.DateTool;
import tools.ListItem;
import tools.MyLog;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.toedter.calendar.JDateChooser;

import db.AktionsErgebnis;
import db.Contact;
import db.DBTools;
import db.Gespraech;
import db.Projektleiter;

/**
 * @author basti
 * 
 *         this shows a panel with all the data of a contact (address) fom a
 *         contactbundle. it also shows the possibility to insert new aktions
 *         and to step to the next contact in a list. it sets a ontact to
 *         changed if the user changed some of its data. one can select what
 *         kind of contacts are shown: all, that with finished aktions, that
 *         without any aktion or the ones with notes.
 */
public class MySearchContactPanel extends JPanel implements ActionListener, KeyListener {

	private final String	inputTabName	= "Dateneingabe";

	private JTextField		id;
	private JTextField		nName;
	private JTextField		vName;
	private JTextField		strasse;
	private JTextField		hausnr;
	private JTextField		plz;
	private JTextField		stadt;
	private JTextField		telefax;
	private JTextField		fensterzahl;
	private JTextField		telefonPrivat;
	private JTextField		telefonBuero;
	private JTextField		email;
	private JTextField		zaunlaenge;
	private JTextField		mde;
	private JComboBox		haustuerfarbe;
	private JComboBox		fassadenart;
	private JComboBox		fassadenfarbe;
	private JCheckBox		glasbausteine;
	private JComboBox		heizung;
	private JTextArea		notiz;
	private JList			aktionList;
	private JButton			saveButton;
	private JList			resultList;
	private JComboBox		gErgList;
	private Contact			lastLoadedContact;
	private JComboBox		gProdList;
	private JComboBox		gWLList;
	private JDateChooser	gespraechDateChooser;

	/**
	 * ctor for search panel
	 * 
	 * @param theCB
	 *            the bundle this panel is displaying.
	 * @param mafo
	 *            the mafo that is working on this contactbundle. all new
	 *            aktions will be created in his name.
	 */
	public MySearchContactPanel() {
		this.setLayout(new BorderLayout());
		this.add(this.makeDataPanel());

		// other init stuff
		AktionsErgebnis.getAktionErgebnisseAsMap(); // load aktionsergebnis list
		DBTools.buildAktionsErgebnisList(false, false); // load aktionsliste
	}

	/**
	 * this is the panel that shows all user informations like name, adress and
	 * all aktions
	 * 
	 * @return the datapanel
	 */
	private JPanel makeDataPanel() {
		JPanel dataPanel = new JPanel(new BorderLayout());

		// show data
		JPanel kundendaten = new JPanel(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(1, 3, 1, 3);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		double lWeight = 0.1;

		gc.gridy++;
		gc.gridx = 0;
		gc.gridwidth = 7;
		DefaultComponentFactory dcf = DefaultComponentFactory.getInstance();
		JComponent aData = dcf.createSeparator("Adressdaten");
		kundendaten.add(aData, gc);

		JLabel sLabel = new JLabel("Schlüssel");
		gc.gridwidth = 1;
		gc.gridy++;
		gc.weightx = lWeight;
		kundendaten.add(sLabel, gc);
		this.id = new JTextField();
		this.id.addKeyListener(this);
		this.id.setBackground(Color.CYAN.brighter().brighter());
		this.id
				.setToolTipText("Datenbank-Schlüssel: Diese Nummer identifiziert diese Adresse eindeutig in der Datenbank");
		// this.nName.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.id, gc);

		JLabel mdeLabel = new JLabel("Marktdatenermittler");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(mdeLabel, gc);
		this.mde = new JTextField();
		this.mde.setToolTipText("Marktdatenermittler");
		// this.nName.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.mde, gc);

		JLabel nnLabel = new JLabel("Nachname");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(nnLabel, gc);
		this.nName = new JTextField();
		this.nName.addKeyListener(this);
		this.nName.requestFocus();
		this.nName.setBackground(Color.CYAN.brighter().brighter());
		this.nName.setToolTipText("Nachname");
		// this.nName.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.nName, gc);

		JLabel vnLabel = new JLabel("Vorname");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(vnLabel, gc);
		this.vName = new JTextField();
		this.vName.addKeyListener(this);
		this.vName.setBackground(Color.CYAN.brighter().brighter());
		this.vName.setToolTipText("Vorname");
		// this.vName.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.vName, gc);

		JLabel plzLabel = new JLabel("Postleitzahl");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(plzLabel, gc);
		this.plz = new JTextField();
		this.plz.addKeyListener(this);
		this.plz.setBackground(Color.CYAN.brighter().brighter());
		this.plz.setToolTipText("Postleitzahl");
		// this.plz.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.plz, gc);

		JLabel stadtLabel = new JLabel("Stadt");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(stadtLabel, gc);
		this.stadt = new JTextField();
		this.stadt.addKeyListener(this);
		this.stadt.setBackground(Color.CYAN.brighter().brighter());
		this.stadt.setToolTipText("Stadt");
		// this.stadt.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.stadt, gc);

		JLabel strLabel = new JLabel("Straße");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(strLabel, gc);
		this.strasse = new JTextField();
		this.strasse.addKeyListener(this);
		this.strasse.setToolTipText("Straße");
		// this.strasse.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.strasse, gc);

		JLabel hnLabel = new JLabel("Hausnummer");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(hnLabel, gc);
		this.hausnr = new JTextField();
		this.hausnr.setToolTipText("Hausnummer");
		// this.hausnr.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.hausnr, gc);

		JLabel tnrLabel = new JLabel("Telefon privat");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(tnrLabel, gc);
		this.telefonPrivat = new JTextField();
		this.telefonPrivat.setToolTipText("Telefon privat");
		// this.telefon.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.telefonPrivat, gc);

		JLabel faxLabel = new JLabel("Telefax");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(faxLabel, gc);
		this.telefax = new JTextField();
		this.telefax.setToolTipText("Telefax");
		// this.telefax.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.telefax, gc);

		JLabel tnrBueroLabel = new JLabel("Telefon Büro");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(tnrBueroLabel, gc);
		this.telefonBuero = new JTextField();
		this.telefonBuero.setToolTipText("Telefon privat");
		// this.telefon.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.telefonBuero, gc);

		JLabel emailLabel = new JLabel("Email");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(emailLabel, gc);
		this.email = new JTextField();
		this.email.setToolTipText("Email");
		// this.telefax.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.email, gc);

		gc.gridy++;
		gc.gridx = 0;
		gc.gridwidth = 7;
		JComponent hData = dcf.createSeparator("Hausdaten");
		kundendaten.add(hData, gc);

		JLabel fzLabel = new JLabel("Alte FE, vorn bzw. seitlich erreichbar");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(fzLabel, gc);
		this.fensterzahl = new JTextField();
		this.fensterzahl.setToolTipText("Fensterzahl");
		// this.fensterzahl.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.fensterzahl, gc);

		JLabel hfLabel = new JLabel("Haustürfarbe");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(hfLabel, gc);
		this.haustuerfarbe = new JComboBox(DBTools.colorList());
		this.haustuerfarbe.setToolTipText("Haustürfarbe");
		// this.haustuerfarbe.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.haustuerfarbe, gc);

		JLabel faLabel = new JLabel("Fassadenart");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(faLabel, gc);
		this.fassadenart = new JComboBox(DBTools.fassadenArtList());
		this.fassadenart.setToolTipText("Fassadenart");
		// this.fassadenart.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.fassadenart, gc);

		JLabel ffLabel = new JLabel("Fassadenfarbe");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(ffLabel, gc);
		this.fassadenfarbe = new JComboBox(DBTools.colorList());
		this.fassadenfarbe.setToolTipText("Fassadenfarbe");
		// this.fassadenfarbe.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.fassadenfarbe, gc);

		JLabel gbsLabel = new JLabel("Glasbausteine");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(gbsLabel, gc);
		this.glasbausteine = new JCheckBox();
		this.glasbausteine.setToolTipText("Glasbausteine");
		// this.glasbausteine.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.glasbausteine, gc);

		JLabel zlLabel = new JLabel("Zaunlänge");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(zlLabel, gc);
		this.zaunlaenge = new JTextField();
		this.zaunlaenge.setToolTipText("Zaunlänge");
		// this.zaunlaenge.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.zaunlaenge, gc);

		JLabel heizLabel = new JLabel("Heizung");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(heizLabel, gc);
		this.heizung = new JComboBox(DBTools.heizungList());
		this.heizung.setToolTipText("Heizung");
		// this.glasbausteine.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.heizung, gc);

		JLabel noteLabel = new JLabel("Notizen");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(noteLabel, gc);
		this.notiz = new JTextArea(5, 80);
		this.notiz.setToolTipText("Notiz");
		gc.gridx++;
		gc.gridwidth = 6;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridheight = 3;
		JScrollPane scroller = new JScrollPane(this.notiz);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(100, 40));
		kundendaten.add(scroller, gc);

		// show aktions
		JPanel kundenaktionen = new JPanel(new BorderLayout());
		this.aktionList = new JList();
		JScrollPane aktionListScroller = new JScrollPane(this.aktionList);
		aktionListScroller.setPreferredSize(new Dimension(200, 40));
		kundenaktionen.add(aktionListScroller);

		// show result
		JPanel kundenPanel = new JPanel(new GridBagLayout());
		gc.insets = new Insets(1, 1, 1, 1);
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 6;
		gc.gridwidth = 2;
		this.resultList = new JList();
		this.resultList.addKeyListener(this);
		JScrollPane listScroller = new JScrollPane(this.resultList);
		listScroller.setPreferredSize(new Dimension(300, 40));
		kundenPanel.add(listScroller, gc);
		JLabel ergLab = new JLabel("Gesprächsergebnis:");
		gc.gridx = 2;
		gc.gridheight = 1;
		kundenPanel.add(ergLab, gc);
		this.gErgList = new JComboBox(DBTools.buildTerminErgebnisList());
		gc.gridy += 1;
		kundenPanel.add(this.gErgList, gc);

		JLabel ergDateLab = new JLabel("Gesprächsdatum:");
		gc.gridy += 1;
		kundenPanel.add(ergDateLab, gc);
		this.gespraechDateChooser = new JDateChooser();
		gc.gridy += 1;
		kundenPanel.add(this.gespraechDateChooser, gc);

		JLabel prodLab = new JLabel("Produkt:");
		JLabel wlLab = new JLabel("Projektleiter:");
		gc.gridy += 1;
		gc.gridwidth = 1;
		kundenPanel.add(prodLab, gc);
		gc.gridx += 1;
		kundenPanel.add(wlLab, gc);
		gc.gridx -= 1;
		this.gProdList = new JComboBox(DBTools.buildProduktList(true));
		this.gWLList = new JComboBox(DBTools.weleWeleList(true));
		gc.gridy += 1;
		kundenPanel.add(this.gProdList, gc);
		gc.gridx += 1;
		kundenPanel.add(this.gWLList, gc);

		// save button
		this.saveButton = new JButton("Speichern");
		this.saveButton.setFont(new Font("Tahoma", Font.BOLD, 20));
		this.saveButton.addActionListener(this);
		this.saveButton.setActionCommand("save");
		this.saveButton
				.setToolTipText("Speichert alle ge�nderten Daten in die Datenbank. Danach k�nnen Sie weiterarbeiten");
		// this.saveButton.setEnabled(false);
		gc.gridy += 1;
		gc.gridx = 0;
		gc.gridwidth = 4;
		kundenPanel.add(this.saveButton, gc);

		// make nice Simplelayoutframes and add stuff to them
		SimpleInternalFrame sif = new SimpleInternalFrame("Kundendaten", null, kundendaten);
		sif.setPreferredSize(new Dimension(600, 350));
		int eb = 2;
		CompoundBorder cbi = new CompoundBorder(new EmptyBorder(eb, eb, 0, eb), sif.getBorder());
		sif.setBorder(cbi);
		dataPanel.add(sif, BorderLayout.NORTH);
		SimpleInternalFrame sif2 = new SimpleInternalFrame("Kundenaktionen", null, kundenaktionen);
		sif2.setPreferredSize(new Dimension(600, 80));
		CompoundBorder cbi2 = new CompoundBorder(new EmptyBorder(eb, eb, 0, eb), sif2.getBorder());
		sif2.setBorder(cbi2);
		dataPanel.add(sif2, BorderLayout.CENTER);
		SimpleInternalFrame sif3 = new SimpleInternalFrame("Suchergebnis und Gesprächseingabe", null, kundenPanel);
		sif3.setPreferredSize(new Dimension(600, 200));
		CompoundBorder cbi3 = new CompoundBorder(new EmptyBorder(eb, eb, eb, eb), sif3.getBorder());
		sif3.setBorder(cbi3);
		dataPanel.add(sif3, BorderLayout.SOUTH);
		return dataPanel;
	}

	/**
	 * save the data from gui into memory contact
	 */
	private void saveGUIContact() {
		// check if contact is unchanged
		Contact fromGUI = this.getContactFromGUI();
		if (fromGUI != null) {
			// is it new, then save everything, otherweise just save gespraech
			if (fromGUI.getId().trim().length() == 0) {
				System.out.println("savedcontact");
				fromGUI.saveNewToDB();
			}
			if (fromGUI.getId().length() > 0) {
				// always save gespraech
				ListItem ergLI = (ListItem) this.gErgList.getSelectedItem();
				ListItem prodLI = (ListItem) this.gProdList.getSelectedItem();
				Projektleiter wl = (Projektleiter) this.gWLList.getSelectedItem();
				Gespraech ttt = new Gespraech(fromGUI, this.gespraechDateChooser.getDate(), this.gespraechDateChooser
						.getDate(), DateTool.vertragsgespraechZeiten().firstElement(), ergLI.getKey0(), prodLI
						.getKey0(), 0, "", null, wl);
				ttt.saveToDB();
			}
			// null input
			this.showContact(new Contact());
			this.resultList.removeAll();
		}
	}

	/**
	 * show the given contact on gui
	 * 
	 * @param c
	 *            contact to show
	 */
	@SuppressWarnings("unchecked")
	public void showContact(Contact c) {
		if (c != null) {
			this.lastLoadedContact = c;
			this.id.setText(c.getId());
			this.mde.setText(DBTools.nameOfMDE(c.getMde(), false));
			this.nName.setText(c.getNachName());
			this.vName.setText(c.getVorName());
			this.strasse.setText(c.getStrasse());
			this.hausnr.setText(c.getHausnr());
			this.plz.setText(c.getPlz());
			this.stadt.setText(c.getStadt());
			this.telefonPrivat.setText(c.getTelefonPrivat());
			this.telefonBuero.setText(c.getTelefonBuero());
			this.email.setText(c.getEmail());
			this.telefax.setText(c.getTelefax());
			this.fensterzahl.setText(c.getFensterzahl());
			try {
				if (c.getHaustuerfarbe().length() > 0) {
					this.haustuerfarbe.setSelectedIndex(Integer.parseInt(c.getHaustuerfarbe()));
				} else {
					this.haustuerfarbe.setSelectedIndex(0);
				}
				if (c.getFassadenfarbe().length() > 0) {
					this.fassadenfarbe.setSelectedIndex(Integer.parseInt(c.getFassadenfarbe()));
				} else {
					this.fassadenfarbe.setSelectedIndex(0);
				}
				if (c.getFassadenart().length() > 0) {
					this.fassadenart.setSelectedIndex(Integer.parseInt(c.getFassadenart()));
				} else {
					this.fassadenart.setSelectedIndex(0);
				}
			} catch (NumberFormatException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			if (c.getGlasbausteine().length() > 0 && c.getGlasbausteine().equals("0")) {
				this.glasbausteine.setSelected(false);
			} else if (c.getGlasbausteine().length() > 0 && c.getGlasbausteine().equals("1")) {
				this.glasbausteine.setSelected(true);
			} else {
				this.glasbausteine.setSelected(false);
			}
			this.zaunlaenge.setText(c.getZaunlaenge());
			this.notiz.setText(c.getNotiz());

			Vector aktions = null;
			aktions = c.getDbAktionen();
			// add gespräche as well
			Vector<Gespraech> gs = c.getGespraeche();
			for (Iterator<Gespraech> iter = gs.iterator(); iter.hasNext();) {
				Gespraech g = iter.next();
				g.setNameStyle(Gespraech.NAMESTYLE_DETAILED);
			}
			aktions.addAll(gs);
			if (aktions != null && !aktions.isEmpty()) {
				this.aktionList.setListData(aktions);
			}
		} else {
			MyLog.logDebug("not contact found to show");
		}
	}

	/**
	 * get the displayed contact from gui
	 * 
	 * @return the contact actually displayed on the gui
	 */
	public Contact getContactFromGUI() {
		Contact c = null;
		// only get contact from gui if there is one or we are searching for one
		c = new Contact();
		c.setId(this.id.getText());
		c.setNachName(this.nName.getText());
		c.setVorName(this.vName.getText());
		c.setStrasse(this.strasse.getText());
		c.setHausnr(this.hausnr.getText());
		c.setPlz(this.plz.getText());
		c.setStadt(this.stadt.getText());
		c.setTelefonPrivat(this.telefonPrivat.getText());
		c.setTelefonBuero(this.telefonBuero.getText());
		c.setEmail(this.email.getText());
		return c;
	}

	private void startSearch() {
		Datenerfasser.setWaitCursor();
		Contact searcho = this.getContactFromGUI();
		ContactBundle cb = new ContactBundle(searcho);
		if (cb.contactCount() == ContactBundle.SEARCHLIMIT) {
			Datenerfasser.setStatusText(cb.contactCount() + " Adressen gefunden, "
					+ "es sind mehr Adressen zu diesem Muster vorhanden. Verfeinern Sie die Suche.");
			Toolkit.getDefaultToolkit().beep();
		} else if (cb.contactCount() > 0) {
			Datenerfasser.setStatusText(cb.contactCount() + " Adressen gefunden");
		} else {
			Datenerfasser.setStatusText("Keine Adressen gefunden");
			Toolkit.getDefaultToolkit().beep();
			Toolkit.getDefaultToolkit().beep();
		}
		this.showFoundContacts(cb);
		Datenerfasser.setDefaultCursor();
	}

	private void showFoundContacts(ContactBundle bundle) {
		for (Iterator<Contact> iter = bundle.getContacts().iterator(); iter.hasNext();) {
			Contact c = iter.next();
			c.setNameStyle(Contact.NAMESTYLE_DETAILED);
		}
		this.resultList.setListData(bundle.getContacts());
		this.resultList.setSelectedIndex(0);
		this.resultList.requestFocus();
	}

	// ================ actions =====================================
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.equals("startsearch")) {
			this.startSearch();
		} else if (com.equals("save")) {
			this.saveGUIContact();
		}
	}

	// ================ getters and setters
	// =====================================

	public String getInputTabName() {
		return inputTabName;
	}

	public void keyTyped(KeyEvent arg0) {
		if (arg0.getSource() instanceof JList) {
			Contact c = (Contact) this.resultList.getSelectedValue();
			this.showContact(c);
			System.out.println(this.resultList.getSelectedValue());
		} else {
			if (arg0.getKeyChar() == KeyEvent.VK_ENTER) {
				this.startSearch();
			}
		}
		if (arg0.getKeyChar() == KeyEvent.VK_ESCAPE) {
			this.showContact(new Contact());
			this.resultList.removeAll();
			this.nName.requestFocus();
		}
	}

	public void keyPressed(KeyEvent arg0) {
	}

	public void keyReleased(KeyEvent arg0) {
	}
}
