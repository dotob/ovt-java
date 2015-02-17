package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tools.ActionLogger;
import tools.ContactBundle;
import tools.ListItem;
import tools.MyLog;
import tools.SettingsReader;
import tools.SipHelper;
import tools.StrTool;
import tools.SysTools;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

import db.Aktion;
import db.AktionsErgebnis;
import db.Contact;
import db.DBTools;
import db.Database;
import db.Gespraech;
import db.Marktforscher;

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
public class ContactPanel extends JPanel implements ActionListener, ListSelectionListener, KeyListener {

	private static final long serialVersionUID = -313660276470694160L;

	private static final String LAST = "last";
	private static final String FIRST = "first";
	private static final String PREV = "prev";
	private static final String NEXT = "next";

	private final String inputTabName = "Dateneingabe";

	private JTextField contactInfo = new JTextField();
	private JTextField email = new JTextField();
	private JTextField fensterzahl = new JTextField();
	private JTextField hausnr = new JTextField();
	private JTextField id = new JTextField();
	private JTextField mde = new JTextField();
	private JTextField nName = new JTextField();
	private JTextField plz = new JTextField();
	private JTextField stadt = new JTextField();
	private JTextField strasse = new JTextField();
	private JTextField telefax = new JTextField();
	private JTextField telefonBuero = new JTextField();
	private JTextField telefonPrivat = new JTextField();
	private JTextField vName = new JTextField();
	private JTextField zaunlaenge = new JTextField();
	private JComboBox solar = new JComboBox();
	private JComboBox shfTyp = new JComboBox(DBTools.shfflagList());
	private JComboBox haustuerfarbe = new JComboBox();
	private JComboBox fassadenart = new JComboBox();
	private JComboBox fassadenfarbe = new JComboBox();
	private JComboBox heizung = new JComboBox();
	private JComboBox bearbStatus = new JComboBox(DBTools.bearbStatus());
	private JCheckBox mailingEnabled = new JCheckBox();
	private JComboBox whichContactsToShow;
	private JButton delAktion;
	private JButton newAktionButton;
	private JButton firstContact;
	private JButton prevContact;
	private JButton nextContact;
	private JButton lastContact;
	private JButton kalender;
	private JButton saveButton;
	private JButton showGespraechButton;
	private JButton toggleContactLockButton;
	private JButton sipCall;
	private JList aktionList;
	private Contact visibleContact;
	private ContactBundle cb;
	private Marktforscher mafo;
	private JTextArea notiz;
	private String actualRegion;

	private IMainWindow parentWindow;
	private IAdminWindow adminWindow;
	private IMFAssiWindow mfassiWindow;
	private AktionsWahl aktionsWahl;
	private boolean isMFPanel;
	private boolean isSolar;

	private JButton emptyFieldsButton;

	private JButton startSearchButton;

	/**
	 * ctor for mf panel
	 * 
	 * @param theCB
	 *            the bundle this panel is displaying.
	 * @param mafo
	 *            the mafo that is working on this contactbundle. all new
	 *            aktions will be created in his name.
	 */
	public ContactPanel(ContactBundle theCB, Marktforscher mafo, IMFAssiWindow pw) {
		this.cb = theCB;
		this.mafo = mafo;
		this.parentWindow = pw;
		this.mfassiWindow = pw;
		this.isMFPanel = true;
		this.isSolar = mafo.isSolar();

		this.setLayout(new BorderLayout());
		this.add(this.makeDataPanel());
		this.enableAdressWidgets(false);

		// other init stuff
		AktionsErgebnis.getAktionErgebnisseAsMap(); // load aktionsergebnis list
		DBTools.buildAktionsErgebnisList(this.isSolar, false); // load
																// aktionsliste
	}

	/**
	 * ctor for search panel
	 * 
	 * @param theCB
	 *            the bundle this panel is displaying.
	 * @param mafo
	 *            the mafo that is working on this contactbundle. all new
	 *            aktions will be created in his name.
	 */
	public ContactPanel(IAdminWindow pw) {
		this.cb = null;
		this.mafo = null;
		this.parentWindow = null;
		this.parentWindow = pw;
		this.adminWindow = pw;
		this.isMFPanel = false;
		this.isSolar = false;

		this.setLayout(new BorderLayout());
		this.add(this.makeDataPanel());
		this.enableActionButtons(false);

		// other init stuff
		AktionsErgebnis.getAktionErgebnisseAsMap(); // load aktionsergebnis list
		DBTools.buildAktionsErgebnisList(this.isSolar, false); // load
																// aktionsliste
	}

	/**
	 * this is the panel that shows all user informations like name, adress and
	 * all aktions
	 * 
	 * @return the datapanel
	 */
	private JPanel makeDataPanel() {
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
		this.id.setToolTipText("Datenbank-Schlüssel: Diese Nummer identifiziert diese " + "Adresse eindeutig in der Datenbank");
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.id, gc);
		if (!this.isMFPanel) {
			this.id.addKeyListener(this);
		}

		JLabel mdeLabel = new JLabel("Marktdatenermittler");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(mdeLabel, gc);
		this.mde.setToolTipText("Marktdatenermittler");
		this.mde.setEditable(false);
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
		this.nName.setToolTipText("Nachname");
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.nName, gc);

		JLabel vnLabel = new JLabel("Vorname");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(vnLabel, gc);
		this.vName.setToolTipText("Vorname");
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.vName, gc);

		JLabel strLabel = new JLabel("Straße");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(strLabel, gc);
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
		this.hausnr.setToolTipText("Hausnummer");
		// this.hausnr.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.hausnr, gc);

		JLabel plzLabel = new JLabel("Postleitzahl");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(plzLabel, gc);
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
		this.stadt.setToolTipText("Stadt");
		// this.stadt.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.stadt, gc);

		JLabel tnrLabel = new JLabel("Telefon privat");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(tnrLabel, gc);
		this.telefonPrivat.setToolTipText("Telefon privat");
		// this.telefon.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 1;
		gc.weightx = 1;
		kundendaten.add(this.telefonPrivat, gc);
		gc.gridx++;
		this.sipCall = new JButton("Anrufen");
		this.sipCall.setActionCommand("call");
		this.sipCall.addActionListener(this);
		this.sipCall.setEnabled(SipHelper.IsSipEnabled());
		kundendaten.add(this.sipCall, gc);

		JLabel faxLabel = new JLabel("Telefax");
		gc.gridx++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(faxLabel, gc);
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
		this.email.setToolTipText("Email");
		// this.telefax.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.email, gc);

		if (!this.isMFPanel) {
			JLabel shfLabel = new JLabel("SHF-Typ");
			gc.gridx = 0;
			gc.gridy++;
			gc.gridwidth = 1;
			gc.weightx = lWeight;
			kundendaten.add(shfLabel, gc);
			this.shfTyp.setToolTipText("Adresstyp");
			this.shfTyp.setEditable(false);
			this.shfTyp.setSelectedIndex(0);
			gc.gridx++;
			gc.gridwidth = 2;
			gc.weightx = 1;
			kundendaten.add(this.shfTyp, gc);

			JLabel bsLabel = new JLabel("Bearbeitungsstatus");
			gc.gridx += 2;
			gc.gridwidth = 1;
			gc.weightx = lWeight;
			kundendaten.add(bsLabel, gc);
			this.bearbStatus.setToolTipText("Bearbeitungsstatus");
			this.bearbStatus.setEnabled(false);
			gc.gridx++;
			gc.gridwidth = 2;
			gc.weightx = 1;
			kundendaten.add(this.bearbStatus, gc);
		}

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

		JLabel solarLabel = new JLabel("Solar");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(solarLabel, gc);
		this.solar = new JComboBox(DBTools.solarList());
		this.solar.setToolTipText("Solarprodukt");
		// this.solar.addActionListener(this);
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.solar, gc);

		JLabel zlLabel = new JLabel("Zaunlänge");
		gc.gridx += 2;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(zlLabel, gc);
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
		gc.gridx++;
		gc.gridwidth = 2;
		gc.weightx = 1;
		kundendaten.add(this.heizung, gc);

		JLabel weleLabel = new JLabel("Kalender");
		this.kalender = new JButton();
		if (this.isMFPanel) {
			gc.gridx += 2;
			gc.gridwidth = 1;
			gc.weightx = lWeight;
			kundendaten.add(weleLabel, gc);
			this.kalender.setToolTipText("Kalender (Internet) dieser Region öffnen");
			this.kalender.addActionListener(this);
			this.kalender.setActionCommand("openkalender");
			gc.gridx++;
			gc.gridwidth = 2;
			gc.weightx = 1;
			kundendaten.add(this.kalender, gc);
		} else {

			JLabel meLabel = new JLabel("Mailing geeignet");
			gc.gridx += 2;
			gc.gridwidth = 1;
			gc.weightx = lWeight;
			kundendaten.add(meLabel, gc);
			this.mailingEnabled.setToolTipText("Mailing geeignet");
			gc.gridx++;
			gc.gridwidth = 2;
			gc.weightx = 1;
			kundendaten.add(this.mailingEnabled, gc);

			this.fensterzahl.setEnabled(false);
			this.haustuerfarbe.setEnabled(false);
			this.fassadenfarbe.setEnabled(false);
			this.zaunlaenge.setEnabled(false);
			this.fassadenart.setEnabled(false);
			this.heizung.setEnabled(false);
		}

		JLabel noteLabel = new JLabel("Notizen");
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 1;
		gc.weightx = lWeight;
		kundendaten.add(noteLabel, gc);
		this.notiz = new JTextArea(5, 80);
		this.notiz.setToolTipText("Notiz");
		double tmp = gc.weighty;
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
		JPanel kundenaktionen = new JPanel(new GridBagLayout());
		gc.insets = new Insets(1, 1, 1, 1);
		gc.weighty = tmp;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;

		JLabel aLabel = new JLabel("Aktionsergebnisse:");
		gc.gridwidth = 6;
		kundenaktionen.add(aLabel, gc);

		this.aktionList = new JList();
		if (this.isMFPanel) {
			this.aktionList.setCellRenderer(new ActionListCellRenderer(false));
		} else {
			this.aktionList.setCellRenderer(new ActionListCellRenderer(true));
		}
		this.aktionList.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(this.aktionList);

		gc.gridheight = 3;
		gc.gridy = 1;
		gc.weighty = 1;
		gc.weightx = 1;
		kundenaktionen.add(listScroller, gc);

		if (this.isMFPanel) {
			this.newAktionButton = new JButton("\n Ergebnistexte");
			this.newAktionButton.setActionCommand("newAktion");
			this.newAktionButton.addActionListener(this);
			this.newAktionButton.setToolTipText("Hiermit wählen Sie die vorgegebenen Ergebnisbeschreibungen aus.");
			this.newAktionButton.setIcon(new ImageIcon("resources/document-new.png"));
			gc.gridy = 0;
			gc.gridx = 6;
			gc.gridheight = 3;
			gc.weighty = 1;
			kundenaktionen.add(this.newAktionButton, gc);

			this.delAktion = new JButton("Ergebnis löschen");
			this.delAktion.setActionCommand("delAktion");
			this.delAktion.addActionListener(this);
			this.delAktion.setIcon(new ImageIcon("resources/document-del.png"));
			this.delAktion.setToolTipText("Links das zu löschende Ergebnis auswählen und diese Taste drücken.");
			this.delAktion.setEnabled(false);
			gc.gridy = 3;
			gc.weighty = 1;
			kundenaktionen.add(this.delAktion, gc);

			// dummies
			this.toggleContactLockButton = new JButton();
			this.emptyFieldsButton = new JButton();
			this.startSearchButton = new JButton();
		} else {
			gc.weighty = 1;
			gc.weightx = 0.5;
			gc.gridheight = 1;
			gc.gridwidth = 2;

			this.startSearchButton = new JButton("Suche starten");
			this.startSearchButton.setActionCommand("startsearch");
			this.startSearchButton.addActionListener(this);
			this.startSearchButton.setToolTipText("Die Suche nach Adressen starten.");
			gc.gridx = 6;
			gc.gridy = 0;
			kundenaktionen.add(this.startSearchButton, gc);

			this.emptyFieldsButton = new JButton("Felder leeren");
			this.emptyFieldsButton.setActionCommand("emptyentries");
			this.emptyFieldsButton.addActionListener(this);
			this.emptyFieldsButton.setToolTipText("Alle eingaben zurücksetzen");
			gc.gridx = 8;
			gc.gridy = 0;
			kundenaktionen.add(this.emptyFieldsButton, gc);

			this.newAktionButton = new JButton("Aktion anlegen");
			this.newAktionButton.setActionCommand("newAktion");
			this.newAktionButton.addActionListener(this);
			this.newAktionButton.setToolTipText("Neue Aktion anlegen");
			gc.gridy = 1;
			gc.gridx = 6;
			kundenaktionen.add(this.newAktionButton, gc);

			this.delAktion = new JButton("Aktion löschen");
			this.delAktion.setActionCommand("delAktion");
			this.delAktion.addActionListener(this);
			this.delAktion.setEnabled(false);
			gc.gridy = 1;
			gc.gridx = 8;
			kundenaktionen.add(this.delAktion, gc);

			this.showGespraechButton = new JButton("Gespräch bearbeiten");
			this.showGespraechButton.setActionCommand("showGespraech");
			this.showGespraechButton.addActionListener(this);
			this.showGespraechButton.setEnabled(false);
			gc.gridy = 3;
			gc.gridx = 6;
			kundenaktionen.add(this.showGespraechButton, gc);

			this.toggleContactLockButton = new JButton("Adresse sperren");
			this.toggleContactLockButton.setActionCommand("lockContact");
			this.toggleContactLockButton.addActionListener(this);
			this.toggleContactLockButton.setEnabled(false);
			gc.gridy = 3;
			gc.gridx = 8;
			kundenaktionen.add(this.toggleContactLockButton, gc);
		}

		// show aktions
		JPanel kundenPanel = new JPanel(new BorderLayout());
		JPanel chooserPanel = new JPanel(new GridBagLayout());
		chooserPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.insets = new Insets(0, 0, 0, 0);
		chooserPanel.add(new JLabel("Adressenanzeige:"), gc);
		if (this.isMFPanel) {
			// which contacts to show
			Vector<String> whichContact = new Vector<String>();
			whichContact.add("Alle");
			whichContact.add("Adressen mit Notizen");
			whichContact.add("Nicht fertige Adressen");
			whichContact.add("Fertige Adressen");
			// whichContact.add("Unfertige Adressen");
			this.whichContactsToShow = new JComboBox(whichContact);
			this.whichContactsToShow.setActionCommand("showonlaktionlesscontacts");
			this.whichContactsToShow.addActionListener(this);
			gc.gridx++;
			gc.gridwidth = 2;
			chooserPanel.add(this.whichContactsToShow, gc);
			this.contactInfo = new JTextField("Info über Kontakt");
			this.contactInfo.setBackground(new Color(255, 247, 116));
			// this.contactInfo.setBorder(new CompoundBorder(new
			// EmptyBorder(2,2,2,2), new LineBorder(Color.BLACK)));
			this.contactInfo.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			this.contactInfo.setForeground(Color.BLUE);
			this.contactInfo.setHorizontalAlignment(SwingConstants.CENTER);
			this.contactInfo.setFont(new Font("Arial", Font.BOLD, 12));
			this.contactInfo.setPreferredSize(new Dimension(400, 30));
			gc.gridx += 2;
			gc.gridwidth = 3;
			chooserPanel.add(this.contactInfo, gc);
			kundenPanel.add(chooserPanel, BorderLayout.NORTH);
		} else {
			this.contactInfo = new JTextField();
		}

		JPanel kundenbuttons = new JPanel(new GridLayout(1, 0));
		ImageIcon firstIcon = new ImageIcon("resources/go-first.png");
		this.firstContact = new JButton(firstIcon);
		this.firstContact.addActionListener(this);
		this.firstContact.setActionCommand(ContactPanel.FIRST);
		this.firstContact.setToolTipText("Zeige erste Adresse");
		kundenbuttons.add(this.firstContact);
		ImageIcon prevIcon = new ImageIcon("resources/go-previous.png");
		this.prevContact = new JButton(prevIcon);
		this.prevContact.addActionListener(this);
		this.prevContact.setActionCommand(ContactPanel.PREV);
		this.prevContact.setToolTipText("Zeige vorige Adresse");
		kundenbuttons.add(this.prevContact);
		ImageIcon nextIcon = new ImageIcon("resources/go-next.png");
		this.nextContact = new JButton(nextIcon);
		this.nextContact.addActionListener(this);
		this.nextContact.setActionCommand(ContactPanel.NEXT);
		this.nextContact.setToolTipText("Zeige nächste Adresse");
		kundenbuttons.add(this.nextContact);
		ImageIcon lastIcon = new ImageIcon("resources/go-last.png");
		this.lastContact = new JButton(lastIcon);
		this.lastContact.addActionListener(this);
		this.lastContact.setActionCommand(ContactPanel.LAST);
		this.lastContact.setToolTipText("Zeige letzte Adresse");
		kundenbuttons.add(this.lastContact);

		kundenPanel.add(kundenbuttons, BorderLayout.CENTER);

		this.saveButton = new JButton("Zwischenspeichern");
		this.saveButton.setFont(new Font("Tahoma", Font.BOLD, 20));
		this.saveButton.addActionListener(this);
		this.saveButton.setActionCommand("savechanged");
		this.saveButton.setToolTipText("Speichert alle geänderten Daten in die Datenbank. Danach können Sie weiterarbeiten");
		this.enableSaveButton(false);
		kundenPanel.add(this.saveButton, BorderLayout.SOUTH);

		JPanel dataPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(1, 1, 1, 1);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.weightx = 1;
		gc.weighty = 5;

		// make nice Simplelayoutframes and add stuff to them
		SimpleInternalFrame sif = new SimpleInternalFrame("Kundendaten", null, kundendaten);
		sif.setPreferredSize(new Dimension(600, 350));
		int eb = 2;
		CompoundBorder cbi = new CompoundBorder(new EmptyBorder(eb, eb, 0, eb), sif.getBorder());
		sif.setBorder(cbi);
		dataPanel.add(sif, gc);

		gc.gridy = 1;
		gc.weighty = 2;
		SimpleInternalFrame sif2 = new SimpleInternalFrame("Kundenaktionen", null, kundenaktionen);
		sif2.setPreferredSize(new Dimension(600, 120));
		CompoundBorder cbi2 = new CompoundBorder(new EmptyBorder(eb, eb, 0, eb), sif2.getBorder());
		sif2.setBorder(cbi2);
		dataPanel.add(sif2, gc);

		gc.gridy = 2;
		gc.weighty = 1;
		SimpleInternalFrame sif3 = new SimpleInternalFrame("Kundenwahl", null, kundenPanel);
		sif3.setPreferredSize(new Dimension(600, 135));
		CompoundBorder cbi3 = new CompoundBorder(new EmptyBorder(eb, eb, eb, eb), sif3.getBorder());
		sif3.setBorder(cbi3);
		dataPanel.add(sif3, gc);

		return dataPanel;
	}

	private void enableAdressWidgets(boolean enable) {
		this.id.setEditable(enable);
		this.strasse.setEditable(enable);
		this.vName.setEditable(enable);
		this.nName.setEditable(enable);
		this.hausnr.setEditable(enable);
		this.plz.setEditable(enable);
		this.stadt.setEditable(enable);
		this.telefonPrivat.setEditable(enable);
	}

	private void enableHausdatenWidgets(boolean enable) {
		this.fensterzahl.setEnabled(enable);
		this.haustuerfarbe.setEnabled(enable);
		this.fassadenfarbe.setEnabled(enable);
		this.zaunlaenge.setEnabled(enable);
		this.fassadenart.setEnabled(enable);
		this.heizung.setEnabled(enable);
	}

	public void enableMoveButtons(boolean enable) {
		this.firstContact.setEnabled(enable);
		this.prevContact.setEnabled(enable);
		this.nextContact.setEnabled(enable);
		this.lastContact.setEnabled(enable);
	}

	public void enableActionButtons(boolean enable) {
		this.emptyFieldsButton.setEnabled(enable);
		this.newAktionButton.setEnabled(enable);
		this.kalender.setEnabled(enable);
		this.toggleContactLockButton.setEnabled(enable);
	}

	/**
	 * this check if the list of aktions contains any aktions and if one is
	 * selected and then enables the delaktion button if there is selected
	 * aktion...
	 */
	private void toggleDelAktionButton() {
		// check if there are aktionen and activate/disable delete aktion button
		this.delAktion.setEnabled(false);
		if (this.aktionList.getModel().getSize() > 0) {
			if (this.aktionList.getSelectedIndex() >= 0) {
				this.delAktion.setEnabled(true);
			}
		}
	}

	public void enableSaveButton(boolean active) {
		// this.saveButton.setEnabled(active);
	}

	/**
	 * save the data from gui into memory contact
	 */
	private void setValuesFromGUI2Contact() {
		// check if contact is unchanged
		Contact fromGUI = this.getContactFromGUI();
		if (fromGUI != null) {
			Contact lastContact = this.cb.getAktContact();
			if (lastContact != null) {
				// do nothing if id is not matching!!!!!
				if (lastContact.getId().equals(fromGUI.getId())) {
					if (!lastContact.nearlyEqual(fromGUI, true)) {
						System.out.println("geändert");
						MyLog.logDebug("Adresse geändert: " + lastContact);
						lastContact.setSomeValues(fromGUI);
						lastContact.setChanged(true);
					}
				}
			}
		}
	}

	/**
	 * an action button is pressed and another contact has to be loaded
	 * 
	 * @param com
	 *            what to do
	 * @return the next/previous best contact that was loaded. null if nothing
	 *         changed.
	 */
	private Contact loadContact(String com) {
		// save data
		this.setValuesFromGUI2Contact();

		// check if all or just some contacts will be shown
		int whichContacts = 0;
		if (this.whichContactsToShow != null) {
			whichContacts = this.whichContactsToShow.getSelectedIndex();
		}

		// load new contact
		Contact c = null;
		// only load other contact if needed
		if (com.equals(ContactPanel.NEXT)) {
			c = this.cb.getNextContact(whichContacts);
		} else if (com.equals(ContactPanel.PREV)) {
			c = this.cb.getPrevContact(whichContacts);
		} else if (com.equals(ContactPanel.FIRST)) {
			c = this.cb.getFirstContact(whichContacts);
		} else if (com.equals(ContactPanel.LAST)) {
			c = this.cb.getLastContact(whichContacts);
		}
		if (c != null) {
			this.showContact(c);
			// System.out.println(com +" :show: "+c);
		}
		return c;
	}

	/**
	 * show the given contact on gui
	 * 
	 * @param c
	 *            contact to show
	 */
	public void showContact(Contact c) {
		if (c != null) {
			this.visibleContact = c;
			this.id.setText(c.getId());
			this.mde.setText(DBTools.nameOfMDE(c.getMde(), false));
			this.nName.setText(c.getNachName());
			this.vName.setText(c.getVorName());
			this.strasse.setText(c.getStrasse());
			this.hausnr.setText(c.getHausnr());
			this.plz.setText(c.getPlz());
			this.stadt.setText(c.getStadt());
			this.telefonPrivat.setText(StrTool.fillChar(c.getTelefonPrivat(), " ", 3));
			this.telefonBuero.setText(c.getTelefonBuero());
			this.email.setText(c.getEmail());
			this.telefax.setText(c.getTelefax());
			this.fensterzahl.setText(c.getFensterzahl());
			this.shfTyp.setSelectedIndex(c.getShfflag());
			this.mailingEnabled.setSelected(c.isMailingenabled());
			try {
				if (c.getHaustuerfarbe().length() > 0) {
					this.haustuerfarbe.setSelectedIndex(Integer.parseInt(c.getHaustuerfarbe()));
				} else {
					this.haustuerfarbe.setSelectedIndex(0);
				}
				if (c.getSolarProdukt().length() > 0) {
					this.solar.setSelectedIndex(Integer.parseInt(c.getSolarProdukt()));
				} else {
					this.solar.setSelectedIndex(0);
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
				if (c.getBearbeitungsstatus().length() > 0) {
					int i = 0;
					for (ListItem li : DBTools.bearbStatus()) {
						if (li.getKey0().equals(c.getBearbeitungsstatus())) {
							this.bearbStatus.setSelectedIndex(i);
						}
						i++;
					}
				} else {
					this.bearbStatus.setSelectedIndex(0);
				}
			} catch (NumberFormatException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			this.zaunlaenge.setText(c.getZaunlaenge());
			// set kalender link text
			String region = DBTools.regionOfPLZ(c.getPlz());
			String wlID = DBTools.wlOfPLZ(c.getPlz());
			String wlName = DBTools.nameOfWL(wlID, false);
			String tmp = "<html>Projektleiter: <b>" + wlName + "</b></html>";
			if (region.length() == 0) {
				tmp = "Keinen Projektleiter gefunden";
				this.kalender.setEnabled(false);
			} else {
				this.kalender.setEnabled(true);
				this.actualRegion = region;
			}
			this.kalender.setText(tmp);
			this.notiz.setText(c.getNotiz());

			updateActions(c);
			this.setContactInfo(c);
		} else {
			MyLog.logDebug("not contact found to show");
		}
	}

	public void updateActions(Contact c) {
		this.aktionList.setListData(new Object[] {});
		Vector aktions = null;
		if (c.getId().length() > 0) {
			if (this.isMFPanel) {
				// aktions = c.getDisplayAktionen();
				aktions = c.getDisplayAktionen4Mafo(this.mafo);
			} else {
				aktions = c.getDbAktionen();

				// do i really need those?
				aktions.addAll(c.getNewAktionen());

				// add gespräche as well
				Vector<Gespraech> gs = c.getGespraeche();
				aktions.addAll(gs);
			}
		}
		if (aktions != null && !aktions.isEmpty()) {
			this.aktionList.setListData(aktions);
		}
	}

	/**
	 * updates a field where is an infostring if the contact that show the
	 * status of that contact.
	 * 
	 * @param c
	 *            the contact that is informed about
	 */
	private void setContactInfo(Contact c) {
		// show contact info in info field
		if (c != null && this.isMFPanel) {
			if (c.hasAktionen(true) && c.hasFinishingAktionOnDisplay()) {
				this.contactInfo.setForeground(Color.GREEN.darker());
				this.contactInfo.setText("Kontakt wurde schon bearbeitet");
			} else if (c.hasAktionen(true) && !c.hasFinishingAktionOnDisplay()) {
				this.contactInfo.setForeground(Color.BLUE);
				this.contactInfo.setText("Kontakt wurde noch nicht fertig bearbeitet");
			} else {
				this.contactInfo.setForeground(Color.RED);
				this.contactInfo.setText("Kontakt wurde noch nicht bearbeitet");
			}

		} else if (this.isMFPanel) {
			MyLog.logDebug("setContactInfo: got null contact");
		}
		// show status
		int idx = this.cb.getAktContactIndex() + 1;
		int anz = this.cb.contactCount();
		int anzChanged = this.cb.getChangedContactCount();
		String statusInfo = "";
		if (anz == 0) {
			statusInfo = "Keine Adressen";
		} else if (anz == 1) {
			if (anzChanged == 0) {
				statusInfo = "Eine Adresse, unverändert";
			} else {
				statusInfo = "Eine Adresse, verändert";
			}
		} else {
			statusInfo = idx + ". von " + anz + " geladenen Adressen, davon " + anzChanged + " geändert";
		}
		if (!this.isMFPanel) {
			this.parentWindow.setStatusText(statusInfo);
			this.toggleContactLockButton.setText(c.isLocked() ? "Adresse entsperren" : "Adresse sperren");
		} else {
			this.parentWindow.setStatusText(statusInfo);
		}
		// toggle save button
		if (this.cb.getChangedContactCount() > 0) {
			this.enableSaveButton(true);
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
		if (this.id.getText().length() > 0 || !this.isMFPanel) {
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
			// mde is faked later....
			c.setTelefax(this.telefax.getText());
			c.setFensterzahl(this.fensterzahl.getText());
			c.setShfflag(this.shfTyp.getSelectedIndex());
			c.setIsMailingenabledSpecial(this.mailingEnabled.isSelected());
			ListItem li = (ListItem) this.haustuerfarbe.getSelectedItem();
			if (li != null) {
				c.setHaustuerfarbe(li.getKey0());
			}
			li = (ListItem) this.fassadenfarbe.getSelectedItem();
			if (li != null) {
				c.setFassadenfarbe(li.getKey0());
			}
			li = (ListItem) this.fassadenart.getSelectedItem();
			if (li != null) {
				c.setFassadenart(li.getKey0());
			}
			li = (ListItem) this.heizung.getSelectedItem();
			if (li != null) {
				c.setHeizung(li.getKey0());
			}
			li = (ListItem) this.solar.getSelectedItem();
			if (li != null) {
				c.setSolarProdukt(li.getKey0());
			}
			li = (ListItem) this.bearbStatus.getSelectedItem();
			if (li != null) {
				c.setBearbeitungsstatus(li.getKey0());
			}
			c.setZaunlaenge(this.zaunlaenge.getText());
			// get aktionen from liste
			Vector<Aktion> newA = new Vector<Aktion>();
			Vector<Aktion> dspA = new Vector<Aktion>();
			ListModel alm = this.aktionList.getModel();
			if (alm.getSize() > 0) {
				for (int i = 0; i < alm.getSize(); i++) {
					Object o = alm.getElementAt(i);
					if (o instanceof Aktion) {
						Aktion a = (Aktion) o;
						dspA.add(a);
						if (a.getId().equals("-1")) {
							newA.add(a);
						}
					} else if (o instanceof Gespraech) {
						// notting here...
					}
				}
			}
			c.setNotiz(this.notiz.getText().trim());
			c.setNewAktionen(newA);
			c.setDisplayAktionen(dspA);
		}
		return c;
	}

	/**
	 * create new aktion an add it to the visible contact
	 * 
	 * @param erg
	 *            id of result for aktion
	 */
	public void addNewAktionToList(String erg) {
		boolean addMe = false;
		Aktion a = new Aktion(this.visibleContact, this.mafo, erg, Aktion.TELEFON, null, null, false);
		if (this.isMFPanel && this.visibleContact.hasSingleFinishingAktionOnDisplay()) {
			Object[] options = { "Vorhandenes Ergebnis behalten neues Ergebnis verwerfen", "Neues Ergebnis nehmen und vorhandenes Löschen",
					"Neues Ergebnis trotzdem hinzufügen" };
			int n = JOptionPane.showOptionDialog(this.parentWindow.getFrame(), "Es existiert schon eine fertigstellendes Ergebnis.\n"
					+ "Sie können nur ein fertigstellendes Ergebnis anlegen.\n" + "Was möchten Sie tun?", "Widerspruch!",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 1) {
				// remove existing finisheraktion
				this.visibleContact.removeSingleFinisherAktionFromDisplay();
				addMe = true;
			} else if (n == 2) {
				addMe = true;
			}
		} else {
			addMe = true;
		}

		if (addMe) {
			// add new finisher aktion
			this.visibleContact.addAktion(a, true);
			this.updateActions(this.visibleContact);
			this.setContactInfo(this.visibleContact);
			this.toggleDelAktionButton();
			MyLog.logDebug("Aktion angelegtGUI: " + a.toNoHTMLString() + " für Adresse: " + this.visibleContact);
			ActionLogger.logAktion(a, this.visibleContact);
		}
	}

	/**
	 * ask user for new aktion
	 */
	private void newAktion() {
		if (!this.isMFPanel) {
			this.mafo = this.getMafo4Aktion();
		}
		if (this.mafo != null) {
			// get ergebnis
			ListItem erg = (ListItem) JOptionPane.showInputDialog(null, "Wählen sie das Ergebnis der Aktion aus", "Ergebnis wählen",
					JOptionPane.PLAIN_MESSAGE, null, DBTools.buildAktionsErgebnisList(this.isSolar, false).toArray(), "1");
			if (erg != null) {
				this.addNewAktionToList(erg.getKey0());
			}
		} else {
			JOptionPane.showMessageDialog(null, "Kein Marktforscher ausgewählt.");
		}
	}

	/**
	 * if we are in admin, we dont know which mafo to use for a new aktion, so
	 * ask
	 * 
	 * @return the marktforscher for the new aktion
	 */
	private Marktforscher getMafo4Aktion() {
		Marktforscher ret = null;
		// get ergebnis
		ListItem erg = (ListItem) JOptionPane.showInputDialog(null, "Wählen sie das Ergebnis der Aktion aus", "Ergebnis wählen",
				JOptionPane.PLAIN_MESSAGE, null, DBTools.mafoList().toArray(), "1");
		if (erg != null) {
			ret = Marktforscher.SearchMarktforscher(erg.getKey0());
		}
		return ret;
	}

	/**
	 * delete the selected aktion from actual contact
	 */
	private void delAktion() {
		Object unknown = this.aktionList.getSelectedValue();
		if (unknown instanceof Aktion) {
			Aktion a = (Aktion) unknown;
			if (a != null) {
				this.visibleContact.delAktion(a);
				this.updateActions(this.visibleContact);
				this.setContactInfo(this.visibleContact);
				this.toggleDelAktionButton();
				MyLog.logDebug("Aktion gelöschtGUI: " + a.toNoHTMLString() + " für Adresse: " + this.visibleContact);
			} else {
				JOptionPane.showMessageDialog(this, "Bitte erst eine Aktion markieren.", "Fehler", JOptionPane.WARNING_MESSAGE);
			}
		} else if (unknown instanceof Gespraech) {
			Gespraech g = (Gespraech) unknown;
			if (g != null) {
				g.delMe();
				DefaultListModel dlm = (DefaultListModel) this.aktionList.getModel();
				dlm.remove(this.aktionList.getSelectedIndex());
				this.setContactInfo(this.visibleContact);
				this.toggleDelAktionButton();
				MyLog.logDebug("Gespräch gelöschtGUI: " + g.toNoHTMLString() + " für Adresse: " + this.visibleContact);
			} else {
				JOptionPane.showMessageDialog(this, "Bitte erst eine Aktion markieren.", "Fehler", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * all contacts have a certain region and the webcalendar represents these
	 * regions. so when a contact needs a termin you can press the right button
	 * and this function is called that opens a browser that conects you to the
	 * webcalendar with the right region for this contact. its a nice feature..
	 */
	private void openCalendar() {
		String gotourl = SettingsReader.getString("OVTMafoClient.calendarUrlFT") + "/calendar.php?catview=" + this.actualRegion;
		if (this.isSolar) {
			gotourl = SettingsReader.getString("OVTMafoClient.calendarUrlSolar") + "/calendar.php?catview=" + this.actualRegion;
		}
		SysTools.ShowInBrowser(gotourl);
	}

	/**
	 * this send changed data back to db but stays with unchanged
	 */
	public void saveChangedData() {
		// test for db first
		int i = 0;
		int maxPings = 20;
		while (!Database.ping() || i >= maxPings) {
			i++;
		}
		if (Database.ping()) {
			// try to find a suitable contact
			int whichContacts = 0;
			if (this.whichContactsToShow != null) {
				whichContacts = this.whichContactsToShow.getSelectedIndex();
			}
			Contact currentContact = this.cb.getAktContact();
			Contact lastContact = this.cb.getAktContact();
			if (this.isMFPanel) {
				// first search backwards
				while (!this.cb.isCurrentFirst() && lastContact.isChanged() && lastContact.hasFinishingAktionOnDisplay()) {
					lastContact = this.cb.getPrevContact(whichContacts);
				}
				if (lastContact == null || lastContact.equals(currentContact)) {
					// if not found search forwards
					lastContact = currentContact;
					this.cb.setAktContact(currentContact);
					while (!this.cb.isCurrentLast() && lastContact.isChanged() && lastContact.hasFinishingAktionOnDisplay()) {
						lastContact = this.cb.getNextContact(whichContacts);
					}
				}
			}

			// save stuff from gui
			this.setValuesFromGUI2Contact();

			// go through changed contacts and really save them
			Vector<Contact> readyContacts = this.cb.getReadyContacts();
			Vector<Contact> changedContacts = this.cb.getChangedContacts();
			MyLog.logDebug("saveChangedData: " + changedContacts.size() + " changed Contacts");
			for (Contact c : changedContacts) {
				c.saveToDB(); // sets changed flag back
			}

			// tell user what happened
			String saveInfo = changedContacts.size() + " geänderte Adresse(n)";

			// reload data from db
			if (!this.isMFPanel) {
				this.cb.updateContacts();
				this.showContact(new Contact());
			} else {
				// now remove the changes and saved contacts from the bundle
				this.cb.removeContacts(readyContacts);

				int readyCount = readyContacts.size();
				if (readyCount > 0) {
					saveInfo += ", davon " + readyCount + " fertige Adresse(n) gespeichert.\n";
				} else {
					saveInfo += ".\n";
				}

				// update infotable
				this.mfassiWindow.startInfoPanelUpdate();
			}

			this.showContact(lastContact);

			// disable save button
			this.enableSaveButton(false);

			saveInfo += "Sie können jetzt weiterarbeiten.";
			JOptionPane.showMessageDialog(this, saveInfo, "Speicherinformation", JOptionPane.INFORMATION_MESSAGE);

			MyLog.logDebug("saveChangedData: end");
		} else {
			String msg = "Der Datenbank-Server kann nicht erreicht werden.\n"
					+ "Schließen sie nicht das Programm. Versuchen Sie später nochmal zu speichern.";
			JOptionPane.showMessageDialog(this, msg);
		}
	}

	public void noContactsFound() {
		this.mfassiWindow.disableDataInputPanel();
	}

	private void emptyFields() {
		Contact rc = this.cb.getContact(this.visibleContact);
		this.cb.removeContact(rc);
		this.showContact(new Contact());

		this.enableSaveButton(false);
		this.enableHausdatenWidgets(true);
		this.enableAdressWidgets(true);
		this.enableActionButtons(false);
	}

	private void toggleContactLock() {
		Contact c = this.visibleContact;
		// check what we want to do
		String whatToDo = "sperren";
		if (c.isLocked()) {
			whatToDo = "entsperren";
		}
		int answ = JOptionPane.showConfirmDialog(this, "<html>Wollen Sie die Adresse <b>" + c.getId() + "</b> wirklich " + whatToDo
				+ "? </html>");
		if (answ == JOptionPane.OK_OPTION) {
			if (c.isLocked()) {
				c.Unlock();
			} else {
				c.Lock();
			}
			this.showContact(c);
		}
	}

	// ================ actions =====================================
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.equals(ContactPanel.FIRST) || com.equals(ContactPanel.PREV) || com.equals(ContactPanel.NEXT)
				|| com.equals(ContactPanel.LAST)) {
			this.loadContact(com);

		} else if (com.equals("newAktion")) {
			this.newAktion();

		} else if (com.equals("openkalender")) {
			this.openCalendar();

		} else if (com.equals("savechanged")) {
			this.saveChangedData();

		} else if (com.equals("delAktion")) {
			this.delAktion();

		} else if (com.equals("lockContact")) {
			this.toggleContactLock();

		} else if (com.equals("showGespraech")) {
			this.showGespraechAndPanel();

		} else if (com.equals("startsearch")) {
			startSearch();

		} else if (com.equals("emptyentries")) {
			this.emptyFields();

		} else if (com.equals("call")) {
			SipHelper.CallNumber(this.cb.getAktContact().getTelefonPrivat());

		} else if (com.equals("showonlaktionlesscontacts")) {
			int whichContacts = this.whichContactsToShow.getSelectedIndex();
			Contact c = this.cb.getAktContact();
			boolean loadNew = true;

			if (whichContacts > ContactBundle.ANY) {
				// check for aktiontype
				if (whichContacts == ContactBundle.WITHOUTFINISHINGACTIONS && !c.hasFinishingAktionOnDisplay()) {
					loadNew = false;
				} else if (whichContacts == ContactBundle.WITHFINISHINGACTIONS && c.hasFinishingAktionOnDisplay()) {
					loadNew = false;
				} else if (whichContacts == ContactBundle.WITHNOTE && c.hasNotiz()) {
					loadNew = false;
				}
			} else {
				loadNew = false;
			}
			if (loadNew) {
				if (this.loadContact(ContactPanel.PREV) == null) {
					this.loadContact(ContactPanel.NEXT);
				}
			}
		}
	}

	private void startSearch() {
		Contact searcho = this.getContactFromGUI();
		// set mde
		String mdeName = this.mde.getText();
		if (mdeName != null && mdeName.length() > 0) {
			searcho.setMde(DBTools.idOfMDE(mdeName));
		}

		this.cb = new ContactBundle(searcho);
		int searchMax = ContactBundle.getSEARCHLIMIT();
		if (this.cb.contactCount() > searchMax - 1) {
			JOptionPane.showMessageDialog(this, "Es wurden mehr als " + searchMax + " Adressen gefunden. "
					+ "\nVerfeinern Sie die Suche, da nur " + searchMax + " Adressen angezeigt werden.");
		}// donno why: else { if (this.cb.contactCount()==1){
			// if there is only one contact found enable save button
		this.enableSaveButton(true);
		this.enableAdressWidgets(true);
		this.enableHausdatenWidgets(true);
		this.enableActionButtons(true);
		this.showContact(this.cb.getFirstContact());
		this.enableMoveButtons(this.cb.getContactCount() > 1);
		if (!this.isMFPanel) {
			this.parentWindow.setStatusText(this.cb.contactCount() + " Adressen gefunden");
		}
	}

	private void showGespraechAndPanel() {
		Gespraech g = (Gespraech) this.aktionList.getSelectedValue();
		if (g != null) {
			this.adminWindow.showGespraech(g);
		}
	}

	public void valueChanged(ListSelectionEvent arg0) {
		if (this.showGespraechButton != null && this.aktionList.getSelectedValue() instanceof Gespraech) {
			this.showGespraechButton.setEnabled(true);
		} else if (this.showGespraechButton != null) {
			this.showGespraechButton.setEnabled(false);
		}

		this.toggleDelAktionButton();
	}

	// ================ getters and setters
	// =====================================

	public void setCB(ContactBundle cb) {
		this.cb = cb;
	}

	public String getInputTabName() {
		return inputTabName;
	}

	public Contact getVisibleContact() {
		return visibleContact;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			this.startSearch();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// notting
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// notting
	}
}

// ================ getters and setters =====================================

/**
 * @author basti this rendere renders the aktionlist. its only purpose is to
 *         make a nicer (brighter) highlight (selection).
 */
class ActionListCellRenderer extends JLabel implements ListCellRenderer {

	boolean moreDetail = false;

	public ActionListCellRenderer(boolean moreDetails) {
		super();
		this.moreDetail = moreDetails;
	}

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getListCellRendererComponent(JList list, Object value, // value
			// to
			// display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		if (value != null) {
			// color action when abgerechnet
			Color listEntry = list.getForeground();
			if (value instanceof Aktion) {
				Aktion a = (Aktion) value;
				if (a.getAbgerechnet() > 0) {
					listEntry = Color.GREEN.darker();
				} else {
					listEntry = Color.RED;
				}
				// if admin show more...
				if (this.moreDetail) {
					setText(a.toDetailString());
				} else {
					setText(a.toString());
				}
			} else if (value instanceof Gespraech) {
				Gespraech g = (Gespraech) value;
				g.setNameStyle(Gespraech.NAMESTYLE_DETAILED);
				setText(g.toString());
				listEntry = Color.BLUE;
			}
			if (isSelected) {
				Color c = list.getSelectionBackground();
				setBackground(c.brighter());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(listEntry);
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
		}
		return this;
	}
}