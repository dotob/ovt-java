package adminassistent;

import importer.OVTImporter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PrinterException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.xnap.commons.gui.DirectoryChooser;

import statistics.MainStatisticPanel;
import tools.ContactBundle;
import tools.EditGespraeche;
import tools.EditWaitingContacts;
import tools.ListItem;
import tools.MyLog;
import tools.SettingsReader;
import tools.Updater;
import ui.ContactPanel;
import ui.FreeContactsPanel;
import ui.IAdminWindow;
import ui.MFManagementPanel;
import ui.ProjektmanagerSearchPanel;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

import db.DBTools;
import db.Database;
import db.Gespraech;
import db.MailingMaker;
import db.Marktforscher;

/**
 * this is an application for ovt overtuer gmbh. it is the admin console for
 * retrieving information from the database and do some serious stuff: - collect
 * contacts to send them to a marktforscher - show statistic about mafo - free
 * contacts that belong to a mafo - create gesprächsberichte - create
 * honorarabrechungen
 * 
 * @author basti
 * 
 */
public class OVTAdmin implements ActionListener, ListSelectionListener,
		KeyListener, IAdminWindow {

	private static final String versionString = "buildnumber";
	private static final String mafoAdressTabName = "Adressen zusammenstellen";
	private static final String applicationName = "RMK Admin-Assistent";
	private static final String MAFOSTATENAME = "Adressenverbrauch";

	private static JFrame frame;
	private JButton billButton;
	private JButton resetButton;
	private JButton showWaitingButton;
	private JButton startMafoSearch;
	private JButton showGespraecheButton;
	private JButton printTable;
	private JButton billAllButton;
	private JTextField honorarProTermin;
	private JTextField honorarProAdresse;
	private JTextField kostenpauschaleProAdresse;
	private JTextField excelVorlagenDir;
	private JTextField excelOutputDir;
	private JComboBox mafoChooserList;
	private MafoStateTable mafoStateTable;
	private GespraechsErfassungsPanel gespraechsPanel;
	private JTabbedPane mainTabs;
	private JDialog settings;
	private static JLabel statusBar;

	/**
	 * Create the GUI and show it. checks for update of program. checks if
	 * honorarabrechnungverzeichnis is set properly. free all temp records from
	 * db.
	 */
	private static void createAndShowGUI() {
		PlasticLookAndFeel
				.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
		try {
			UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
		} catch (Exception e) {
		}

		// read properties
		SettingsReader dummy = new SettingsReader();
		if (dummy != null) {
			// Create and set up the window.
			frame = new JFrame(applicationName + " " + versionString);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// check for database
			if (Database.test()) {
				frame.addWindowListener(new MyWindowListener());

				OVTAdmin app = new OVTAdmin();
				Component contents = app.createComponents();
				Container contentPane = frame.getContentPane();
				contentPane.setLayout(new BorderLayout());
				contentPane.add(contents, BorderLayout.CENTER);
				statusBar = new JLabel("  ");
				contentPane.add("South", statusBar);

				// Display the window.
				frame.setIconImage(new ImageIcon(
						"resources/ovt-admin-frame.gif").getImage());
				Dimension mySize = new Dimension(950, 700);
				frame.setPreferredSize(mySize);
				Toolkit tk = Toolkit.getDefaultToolkit();
				Dimension screen = tk.getScreenSize();
				int x = (int) (screen.getWidth() - mySize.getWidth()) / 2;
				int y = (int) (screen.getHeight() - mySize.getHeight()) / 2;
				frame.setLocation(x, y);

				frame.pack();
				frame.setVisible(true);
				frame.addKeyListener(app);

				// free all temp records
				OVTAdmin.setStatusFromTempToFree();

				// check for update
				String updateURLString = SettingsReader
						.getString("OVTAdmin.updateCheckURL");
				String downloadURLString = SettingsReader
						.getString("OVTAdmin.downloadURL");
				Updater.checkVersion(updateURLString, downloadURLString,
						versionString);

				// check for directories
				boolean saveProps = false;
				String vorlagenDir = SettingsReader
						.getString("OVTAdmin.excelVorlagenVerzeichnis");
				if (vorlagenDir.length() == 0) {
					DirectoryChooser dialog = new DirectoryChooser();
					dialog.setTitle("Verzeichnis für Vorlagen wählen");
					if (dialog.showChooseDialog(frame) == DirectoryChooser.APPROVE_OPTION) {
						vorlagenDir = dialog.getSelectedDirectory()
								.getAbsolutePath();
						if (vorlagenDir != null && vorlagenDir.length() > 0) {
							SettingsReader.setValue(
									"OVTAdmin.excelVorlagenVerzeichnis",
									vorlagenDir);
							saveProps = true;
						}
					}
				}
				String outputDir = SettingsReader
						.getString("OVTAdmin.HonorarOutputVerzeichnis");
				if (outputDir.length() == 0) {
					DirectoryChooser dialog = new DirectoryChooser();
					dialog.setTitle("Honorar-Ausgabeverzeichnis wählen");
					if (dialog.showChooseDialog(frame) == DirectoryChooser.APPROVE_OPTION) {
						outputDir = dialog.getSelectedDirectory()
								.getAbsolutePath();
						if (outputDir != null && outputDir.length() > 0) {
							SettingsReader.setValue(
									"OVTAdmin.HonorarOutputVerzeichnis",
									outputDir);
							saveProps = true;
						}
					}
				}
				// save changed properties
				if (saveProps) {
					SettingsReader.saveProperties();
				}

			} else {
				JLabel error = new JLabel(
						"<html>Konnte nicht auf die Datenbank zugreifen!"
								+ "<br>DB-Url: " + Database.dbURLNoPass()
								+ "</html>");
				error.setFont(new Font("Tahoma", Font.BOLD, 24));
				frame.getContentPane().add(error, BorderLayout.CENTER);
				// Display the window.
				frame.setPreferredSize(new Dimension(600, 600));
				frame.pack();
				frame.setVisible(true);
			}
		} else {
			JLabel error = new JLabel(
					"<html>Konnte Konfigurationsdatei nicht finden!</html>");
			error.setFont(new Font("Tahoma", Font.BOLD, 24));
			frame.getContentPane().add(error, BorderLayout.CENTER);
			// Display the window.
			frame.setPreferredSize(new Dimension(600, 600));
			frame.pack();
			frame.setVisible(true);
		}
	}

	/**
	 * fill main panel with components. main panel consists of a tabbed pane
	 * with alle t aktione panels in it.
	 * 
	 * @return the components for main panel
	 */
	public Component createComponents() {
		// compos
		JPanel mainPanel = new JPanel(new BorderLayout());
		this.mainTabs = new JTabbedPane();

		// send kontakts to mafo panel
		this.mainTabs.addTab(mafoAdressTabName, new CollectAdressPanel(this));

		// conversation report
		this.gespraechsPanel = new GespraechsErfassungsPanel(this);
		this.mainTabs.addTab(this.gespraechsPanel.getTabName(),
				this.gespraechsPanel);

		// show mafo information
		this.mainTabs.addTab(MAFOSTATENAME, this.mafoStatePanel());

		// reports
		MainStatisticPanel sp = new MainStatisticPanel(this);
		this.mainTabs.addTab(sp.getTabName(), sp);

		// contact panel
		ContactPanel cp = new ContactPanel(this);
		this.mainTabs.addTab("Adressen bearbeiten", cp);

		// importer
		OVTImporter impo = new OVTImporter();
		this.mainTabs.addTab(impo.getTabName(), impo.createComponents());

		// mf manager
		MFManagementPanel mfm = new MFManagementPanel(this);
		this.mainTabs.addTab(mfm.getTabName(), mfm.buildMFMPanel());

		// projectmanager manager
		ProjektmanagerSearchPanel pms = new ProjektmanagerSearchPanel(this);
		this.mainTabs.addTab(pms.getTabName(), pms.buildPSPanel());

		// set contacts free tab
		FreeContactsPanel ip = new FreeContactsPanel(this);
		this.mainTabs.addTab(ip.getTabName(), ip);
		mainPanel.add(this.mainTabs);
		return mainPanel;
	}

	/**
	 * this could build some menubar entries for frame...
	 * 
	 * @param frame
	 *            the frame for the menubar
	 */
	private void createMenus(JFrame frame) {
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		// Build the first menu.
		JMenu menu = new JMenu("Aktionen");
		menu.getAccessibleContext().setAccessibleDescription(
				"Hier sind Aktionen abgelegt, die Sie unternehmen können.");
		menuBar.add(menu);

		// a group of JMenuItems
		JMenuItem menuItem = new JMenuItem(
				"Honorarabrechnungs-Ausgaberzeichnis wählen", KeyEvent.VK_H);
		menuItem.addActionListener(this);
		menuItem.setActionCommand("chooseOutputDir");
		menu.add(menuItem);

		frame.setJMenuBar(menuBar);
	}

	/**
	 * this panel should show information about the mafos and their contacts
	 * 
	 * @return panel with info components
	 */
	private JPanel mafoStatePanel() {
		JPanel ret = new JPanel(new BorderLayout());
		JPanel tPanel = new JPanel(new BorderLayout());

		// choose marktforscher
		JPanel mafoChooserPanel = new JPanel(new GridLayout(0, 3));
		mafoChooserPanel.add(new JLabel("Marktforscher"));
		Vector<ListItem> mListWithAll = new Vector<ListItem>();
		mListWithAll.add(new ListItem("", "Alle"));
		Vector<ListItem> mList = DBTools.mafoList();
		mListWithAll.addAll(mList);
		this.mafoChooserList = new JComboBox(mListWithAll);
		mafoChooserPanel.add(this.mafoChooserList);
		this.startMafoSearch = new JButton("Tabelle aktualisieren");
		this.startMafoSearch.setPreferredSize(new Dimension(10, 30));
		this.startMafoSearch.setActionCommand("doMafoTable");
		this.startMafoSearch.addActionListener(this);
		mafoChooserPanel.add(this.startMafoSearch);
		tPanel.add(mafoChooserPanel, BorderLayout.NORTH);

		// table
		this.printTable = new JButton("Diese Tabelle ausdrucken");
		this.showGespraecheButton = new JButton("Gespräche anzeigen");
		this.billButton = new JButton("Honorarabrechnung erzeugen");
		this.billAllButton = new JButton("Honorarabrechnung für Alle erzeugen");
		this.resetButton = new JButton(
				"Adressen dieses Marktforschers zurücksetzen");
		this.showWaitingButton = new JButton("Wartende Adressen bearbeiten");
		JPanel tablePanel = new JPanel(new BorderLayout());
		this.mafoStateTable = new MafoStateTable(this);
		ListSelectionModel rowSM = this.mafoStateTable.getSelectionModel();
		rowSM.addListSelectionListener(this);

		JScrollPane inputScroller = new JScrollPane(this.mafoStateTable);
		tablePanel.add(inputScroller);
		tPanel.add(tablePanel, BorderLayout.CENTER);

		// table action buttons
		JPanel tableButtonsPanel = new JPanel(new GridLayout(0, 2));
		this.printTable.addActionListener(this);
		this.printTable.setActionCommand("printmafostatetable");
		this.printTable.setToolTipText("Tabelle ausdrucken");
		tableButtonsPanel.add(this.printTable);
		this.showGespraecheButton.addActionListener(this);
		this.showGespraecheButton.setActionCommand("mafogespraeche");
		this.showGespraecheButton
				.setToolTipText("Tabelle anzeigen mit allen Gesprächen des Marktforschers");
		this.showGespraecheButton.setEnabled(false);
		tableButtonsPanel.add(this.showGespraecheButton);
		this.billButton.addActionListener(this);
		this.billButton.setActionCommand("mafobill");
		this.billButton
				.setToolTipText("Eine Honorarabrechnung für den Marktforscher erstellen.");
		// always enable...i think its dumb...
		// this.billButton.setEnabled(false);
		tableButtonsPanel.add(this.billButton);
		this.billAllButton.addActionListener(this);
		this.billAllButton.setActionCommand("mafobillall");
		this.billAllButton
				.setToolTipText("Eine Honorarabrechnung für alle Marktforscher erstellen.");
		tableButtonsPanel.add(this.billAllButton);
		this.showWaitingButton.addActionListener(this);
		this.showWaitingButton.setActionCommand("showwaiting");
		this.showWaitingButton
				.setToolTipText("Dies bietet die Möglichkeit einzelne wartende Adressen vom Marktforscher frei zu stellen");
		this.showWaitingButton.setEnabled(false);
		tableButtonsPanel.add(this.showWaitingButton);
		this.resetButton.addActionListener(this);
		this.resetButton.setActionCommand("resetmafo");
		this.resetButton
				.setToolTipText("Alle wartenden Adressen des Marktforschers werden freigestellt.");
		this.resetButton.setEnabled(false);
		tableButtonsPanel.add(this.resetButton);
		tPanel.add(tableButtonsPanel, BorderLayout.SOUTH);

		// put stuff into simpleframe an add them
		int eb = 8;
		SimpleInternalFrame siftable = new SimpleInternalFrame(
				"Marktforscher-Status", null, tPanel);
		siftable.setPreferredSize(new Dimension(600, 350));
		CompoundBorder cbt = new CompoundBorder(
				new EmptyBorder(eb, eb, eb, eb), siftable.getBorder());
		siftable.setBorder(cbt);
		ret.add(siftable, BorderLayout.CENTER);
		return ret;
	}

	// ================ the real action =====================================

	/**
	 * this is called at app start to set all temp records to free, seem as if
	 * app wasnt quitted correct
	 */
	public static void setStatusFromTempToFree() {
		Database.update("kunden", "bearbeitungsstatus=0",
				"WHERE bearbeitungsstatus=4");
	}

	/**
	 * start to collect data for the table where all mafo info is displayed
	 */
	public void fillMafoStateTable() {
		// get selection of mafochooserlist
		ListItem choosedMafo = (ListItem) this.mafoChooserList
				.getSelectedItem();
		Vector<Marktforscher> mafoList;
		if (choosedMafo.getKey0().equals("")) {
			mafoList = DBTools.mafoMafoList(false);
		} else {
			mafoList = new Vector<Marktforscher>();
			mafoList.add(Marktforscher.SearchMarktforscher(choosedMafo
					.getKey0()));
		}
		// progressmonitor
		int min = 0;
		int max = mafoList.size() * 10;
		ProgressMonitor pm = new ProgressMonitor(frame,
				"Marktforscherdaten sammeln", "", min, max);
		pm.setMillisToPopup(10);
		pm.setMillisToDecideToPopup(2);
		this.startMafoSearch.setEnabled(false);
		this.mafoStateTable.setEnableButton(this.startMafoSearch);
		this.mafoStateTable.setMafoList(mafoList);
		this.mafoStateTable.setPm(pm);
		new Thread(this.mafoStateTable).start();
	}

	/**
	 * make a abrechnung for the selected mafo
	 */
	private void makeHonorarAbrechnung() {
		// get mafo
		setWaitCursor();
		Marktforscher mafo = this.mafoStateTable.getSelectedMarktforscher();
		HonorarAbrechnung bill = new HonorarAbrechnung(mafo, this);
		int billWeek = bill.billWeekChooser();
		if (billWeek >= 0) {
			if (bill.collectData(billWeek)) {
				bill.makeExcelAbrechnung(true);
			}
		}
		setDefaultCursor();
	}

	/**
	 * make a abrechnung for the selected mafo
	 */
	private void makeAlleHonorarAbrechnung() {
		// get mafo
		setWaitCursor();
		Vector<Marktforscher> mafo = this.mafoStateTable
				.getAllMarktforscherToBill();
		HonorarAbrechnung bill = new HonorarAbrechnung(null, this);
		int billWeek = bill.billWeekChooser();
		String msg = "Honorarabrechnungen für \n";
		for (Iterator<Marktforscher> iter = mafo.iterator(); iter.hasNext();) {
			Marktforscher mf = iter.next();
			if (billWeek >= 0) {
				bill = new HonorarAbrechnung(mf, this);
				msg += mf + "\n";
				if (bill.collectData(billWeek)) {
					bill.makeExcelAbrechnung(false);
				}
			}
		}
		msg += "im Verzeichnis "
				+ SettingsReader.getString("OVTAdmin.HonorarOutputVerzeichnis")
						.replace("\\", "/");
		msg += " erzeugt.";
		setDefaultCursor();
		JOptionPane.showMessageDialog(frame, msg);
	}

	/**
	 * show the waiting contacts of a given mafo in a new dialog
	 * 
	 * @param mafo
	 *            mafo to show the waiting contacts of
	 */
	private void showWaitingContacts(Marktforscher mafo) {
		if (mafo != null && mafo.getWaitingContactsCount() > 0) {
			EditWaitingContacts ewc = new EditWaitingContacts(mafo, this);
			ewc.showMe();
		} else {
			JOptionPane
					.showMessageDialog(frame,
							"Keine wartenden Adressen bei diesem Marktforscher gefunden");
		}
	}

	/**
	 * show the gespraeche of a given mafo in a new dialog
	 * 
	 * @param mafo
	 *            mafo to show the gespraeche of
	 */
	private void showGespraeche(Marktforscher mafo) {
		if (mafo != null && mafo.getGespraeche().size() > 0) {
			EditGespraeche ewc = new EditGespraeche(mafo, this.gespraechsPanel,
					this);
			ewc.showMe();
		} else {
			JOptionPane.showMessageDialog(frame,
					"Keine Gespräche für diesen Marktforscher gefunden");
		}
	}

	/**
	 * show a dialog where some settings can be made
	 */
	private void showSettings() {
		JPanel inhalt = new JPanel();
		inhalt.setLayout(new GridLayout(0, 1));
		this.settings = new JDialog(frame, "Einstellungen", true);
		this.settings.setContentPane(inhalt);
		this.settings.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		inhalt.add(new JLabel("Honorar für einen erfolgten Termin:"));
		this.honorarProTermin = new JTextField(
				SettingsReader.getString("OVTAdmin.Terminpauschale"), 40);
		this.honorarProTermin.addActionListener(this);
		inhalt.add(this.honorarProTermin);
		inhalt.add(new JLabel("Vergütung pro zurückgegebener Adresse:"));
		this.honorarProAdresse = new JTextField(
				SettingsReader.getString("OVTAdmin.AktionGrundhonorar"), 40);
		this.honorarProAdresse.addActionListener(this);
		inhalt.add(this.honorarProAdresse);
		inhalt.add(new JLabel("Kostenpauschale pro zurückgegebener Adresse:"));
		this.kostenpauschaleProAdresse = new JTextField(
				SettingsReader.getString("OVTAdmin.Kostenpauschale"), 40);
		this.kostenpauschaleProAdresse.addActionListener(this);
		inhalt.add(this.kostenpauschaleProAdresse);
		inhalt.add(new JLabel("Verzeichnis für Excelvorlagen angeben:"));
		this.excelVorlagenDir = new JTextField(
				SettingsReader.getString("OVTAdmin.excelVorlagenVerzeichnis"),
				40);
		this.excelVorlagenDir.addActionListener(this);
		inhalt.add(this.excelVorlagenDir);
		inhalt.add(new JLabel(
				"Ausgabeverzeichnis für Honorarabrechnungen angeben:"));
		this.excelOutputDir = new JTextField(
				SettingsReader.getString("OVTAdmin.HonorarOutputVerzeichnis"),
				40);
		this.excelOutputDir.addActionListener(this);
		inhalt.add(this.excelOutputDir);

		// go button
		JPanel aktionButtons = new JPanel(new GridLayout(0, 2));
		JButton saveAndNew = new JButton("Einstellungen speichern und beenden");
		saveAndNew.addActionListener(this);
		saveAndNew.setActionCommand("savesettings");
		aktionButtons.add(saveAndNew);
		JButton cancel = new JButton("Abbruch");
		cancel.addActionListener(this);
		cancel.setActionCommand("cancelsettings");
		aktionButtons.add(cancel);
		inhalt.add(aktionButtons);

		this.settings.pack();
		this.settings.setVisible(true);
	}

	/**
	 * save the settings to the properties file
	 */
	private void saveSettings() {
		String newTerminHonorar = this.honorarProTermin.getText();
		SettingsReader.setValue("OVTAdmin.Terminpauschale", newTerminHonorar);

		String newAdressHonorar = this.honorarProAdresse.getText();
		SettingsReader
				.setValue("OVTAdmin.AktionGrundhonorar", newAdressHonorar);

		String newPauschaleHonorar = this.kostenpauschaleProAdresse.getText();
		SettingsReader
				.setValue("OVTAdmin.Kostenpauschale", newPauschaleHonorar);

		String newExcelVorlageDir = this.excelVorlagenDir.getText();
		SettingsReader.setValue("OVTAdmin.excelVorlagenVerzeichnis",
				newExcelVorlageDir);

		String newExcelOutputDir = this.excelOutputDir.getText();
		SettingsReader.setValue("OVTAdmin.HonorarOutputVerzeichnis",
				newExcelOutputDir);

		// write new properties file on disk
		SettingsReader.saveProperties();
	}

	// ================ actions =====================================
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.equals("showwaiting")) {
			// do report for actual provided contacts of selected mafo
			Marktforscher mafo = this.mafoStateTable.getSelectedMarktforscher();
			this.showWaitingContacts(mafo);

		} else if (com.equals("resetmafo")) {
			int row = this.mafoStateTable.getSelectedRow();
			if (row >= 0) {
				Marktforscher mafo = this.mafoStateTable
						.getSelectedMarktforscher();
				if (mafo != null) {
					Object[] options = { "Alle bereitgestellten",
							"Nur die unbearbeiteten", "Keine" };
					int n = JOptionPane.showOptionDialog(frame,
							"Welche Adressen sollen zurückgesetzt werden?",
							"Adressen zurücksetzen",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[2]);
					if (n == 0) {
						mafo.resetWaitingContacts();
						this.fillMafoStateTable();
					} else if (n == 1) {
						mafo.resetUntouchedContacts();
						this.fillMafoStateTable();
					}
				}
			}
		} else if (com.equals("mafobill")) {
			this.makeHonorarAbrechnung();

		} else if (com.equals("mafobillall")) {
			this.makeAlleHonorarAbrechnung();

		} else if (com.equals("mafogespraeche")) {
			// do report for actual provided contacts of selected mafo
			Marktforscher mafo = this.mafoStateTable.getSelectedMarktforscher();
			this.showGespraeche(mafo);

		} else if (com.equals("printmafostatetable")) {
			try {
				this.mafoStateTable.print();
			} catch (PrinterException e1) {
				e1.printStackTrace();
			}

		} else if (com.equals("settings")) {
			this.showSettings();

		} else if (com.equals("savesettings")) {
			this.saveSettings();
			this.settings.dispose();

		} else if (com.equals("cancelsettings")) {
			this.settings.dispose();

		} else if (com.equals("quit")) {
			frame.dispose();

		} else if (com.equals("doMafoTable")) {
			this.fillMafoStateTable();
		}
	}

	/**
	 * this is called if mafos data is loaded and enables the aktion buttons if
	 * needed.
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		DefaultTableModel tm = (DefaultTableModel) this.mafoStateTable
				.getMafoStateTableModel();

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		// this.billButton.setEnabled(false);
		this.showWaitingButton.setEnabled(false);
		this.resetButton.setEnabled(false);
		this.showGespraecheButton.setEnabled(false);

		if (lsm.isSelectionEmpty()) {
			// no rows are selected
			// do notting here...
		} else {
			// selectedRow is selected
			// test for billbutton
			int ready = (Integer) tm.getValueAt(e.getFirstIndex(), 4);
			int honTerm = -1;
			String ht = (String) tm.getValueAt(e.getFirstIndex(), 6);
			if (ht.length() > 0) {
				honTerm = Integer.parseInt(ht);
			}
			if (ready > 0 || honTerm > 0) {
				this.billButton.setEnabled(true);
			}
			this.showGespraecheButton.setEnabled(true);

			// test for report Button
			int i = tm.getRowCount() > 0 ? 2 : -1;
			int row = e.getFirstIndex();
			if (row > 2) {
				i = (row % 3);
			}
			if (i == 2) {
				String cb2 = (String) tm.getValueAt(e.getFirstIndex(), 2);
				if (!cb2.equals("0")) {
					this.resetButton.setEnabled(true);
					this.showWaitingButton.setEnabled(true);
				}
			}
		}
	}

	// ######################## GETTERS & SETTERS
	// ######################################
	// ######################## GETTERS & SETTERS
	// ######################################
	// ######################## GETTERS & SETTERS
	// ######################################
	/**
	 * set text of my own little status bar
	 * 
	 * @param txt
	 *            text to set a status message
	 */
	public void setStatusText(String txt) {
		statusBar.setText(txt);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setWaitCursor() {
		if (frame != null) {
			frame.getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}
	}

	public void setDefaultCursor() {
		if (frame != null) {
			frame.getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	public static void main(String[] args) {
		MyLog.logDebug("OVTAdmin started ver." + versionString);
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (RuntimeException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
		});
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("typed:" + arg0.getKeyChar());
	}

	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("pressed:" + arg0.getKeyChar());
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("released:" + arg0.getKeyChar());
	}

	@Override
	public void showGespraech(Gespraech g) {
		this.gespraechsPanel.setGespraech(g);
	}

	@Override
	public void startStatusProgress() {
		// notting
	}

	@Override
	public void stopStatusProgress() {
		// notting
	}

	public static void createMailingAktionen(ContactBundle allCB,
			String mailingName) {
		MailingMaker mm = new MailingMaker(allCB, mailingName, null);
		mm.execute();
	}

}

// ============ windowlistener ========================
class MyWindowListener implements WindowListener {

	public MyWindowListener() {
	}

	public void windowClosed(WindowEvent e) {
		// this is called at app exit to set all temp records to free
		OVTAdmin.setStatusFromTempToFree();
	}

	public void windowClosing(WindowEvent e) {
		// this is called at app exit to set all temp records to free
		OVTAdmin.setStatusFromTempToFree();
	}

	public void windowActivated(WindowEvent e) {
		// System.out.println("windowActivated");
	}

	public void windowDeactivated(WindowEvent e) {
		// System.out.println("windowDeactivated");
	}

	public void windowDeiconified(WindowEvent e) {
		// System.out.println("windowDeiconified");
	}

	public void windowIconified(WindowEvent e) {
		// System.out.println("windowIconified");
	}

	public void windowOpened(WindowEvent e) {
		// System.out.println("windowOpened");
	}

}
