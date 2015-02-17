package mfassistent;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import tools.ActionLogger;
import tools.ContactBundle;
import tools.FTPTool;
import tools.MafoInfoTable;
import tools.MyLog;
import tools.SettingsReader;
import tools.StrTool;
import tools.Updater;
import ui.ContactPanel;
import ui.IMFAssiWindow;
import ui.OVTInfoPanel;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import db.Contact;
import db.Database;
import db.Marktforscher;

/**
 * @author basti this is the main tool for the marktforscher who sits on a phone
 *         and calls some adresses. it can show statistic of calls and lets the
 *         mafo create new aktions for a contact.
 */
public class OVTMafo implements ActionListener, IMFAssiWindow {
	private final static String versionString = "buildnumber";
	private final static String applicationName = "RMK MF-Assistent";

	private static JFrame frame;
	private static JLabel statusBarText;

	// attr
	private ContactBundle cb;
	private static Marktforscher MAFO;
	private static JProgressBar statusBarProgress;

	// gui
	private JTabbedPane mainTabs;
	private ContactPanel contactPanel;
	private JPanel mafoInfoPanel;
	private Thread ftpThread;
	private MafoInfoTable mfInfoTable;
	private JTextArea log;

	/**
	 * Create the GUI and show it. this check if a mafo is set and asks for it
	 * if not. it checks for updates of programm ad builds the gui
	 */
	private static void createAndShowGUI() {

		PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
		try {
			UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
		} catch (Exception e) {
		}

		// check for update
		String updateURLString = SettingsReader.getString("OVTMafoClient.updateCheckURL");
		String downloadURLString = SettingsReader.getString("OVTMafoClient.downloadURL");
		boolean isUpToDate = Updater.checkVersion(updateURLString, downloadURLString, versionString);

		if (isUpToDate) {
			// Create and set up the window.
			frame = new JFrame(applicationName + " " + versionString);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			// check for database
			if (Database.test()) {
				OVTMafo app = new OVTMafo();

				MyLog.logDebug("in the middle of nowhere");
				// ask for mafo if not set
				String amafo = SettingsReader.getString("OVTMafoClient.mafo");
				boolean dontsavemafo = amafo != null ? amafo.equals("nosave") : false;
				if (amafo == null || dontsavemafo || amafo.length() <= 0) {
					MyLog.logDebug("no mafo in properties file found");
					// get mafo from date of birth (dob)
					String dateStr = "";
					while (dateStr.equals("")) {
						String s = "Bitte geben Sie ihr Geburtsdatum ein (tt.mm.yy):";
						dateStr = JOptionPane.showInputDialog(frame, s);
						if (dateStr != null && dateStr.length() == 8) {
							Calendar cal = Calendar.getInstance();
							int year = Integer.parseInt(StrTool.strToken(dateStr, 3, ".")) + 1900;
							int month = Integer.parseInt(StrTool.strToken(dateStr, 2, ".")) - 1;
							int day = Integer.parseInt(StrTool.strToken(dateStr, 1, "."));
							cal.set(year, month, day);
							Date dob = new Date(cal.getTimeInMillis());
							MyLog.logDebug("got dob of mafo to search for: " + dob);
							try {
								ResultSet rs = Database
										.select("id, aktiv", "marktforscher", "WHERE geburtsdatum='" + dob + "' AND aktiv=1");
								while (rs.next()) {
									amafo = rs.getString("id");
									MAFO = Marktforscher.SearchMarktforscher(amafo);
								}
								Database.close(rs);
							} catch (SQLException e) {
								MyLog.showExceptionErrorDialog(e);
								e.printStackTrace();
							}
						} else if (dateStr == null) {
							// cancel
							System.exit(1);
						}
						if (MAFO != null) {
							if (MAFO.getAktiv()) {
								MyLog.logDebug("found aktiv mafo in db: " + MAFO);
								// ask for right mafoname
								Object[] options = { "Ja, ich bin es", "Nein, ich möchte mein Geburtsdatum nochmal eingeben" };
								int n = JOptionPane.showOptionDialog(frame, "Sind Sie " + MAFO.getVorName() + " " + MAFO.getNachName()
										+ "?", "MaFo-Abfrage", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
										options, options[0]);
								if (n == 1) {
									dateStr = "";
								}
							} else {
								MyLog.logDebug("found a NON aktiv mafo in db: " + MAFO);
								JOptionPane.showMessageDialog(frame, "Sie sind als Marktforscher nicht mehr aktiv");
								System.exit(1);
							}
						} else {
							JOptionPane.showMessageDialog(frame, "Kein passender Marktforscher gefunden");
							System.exit(1);
						}
					}
					if (!dontsavemafo) {
						// save mafo
						SettingsReader.setValue("OVTMafoClient.mafo", amafo);
						SettingsReader.saveProperties();
					}
				} else {
					MyLog.logDebug("found mafo in properties file: " + amafo);
					MAFO = Marktforscher.SearchMarktforscher(amafo);
				}

				// set window name to mafo
				frame.setTitle(frame.getTitle() + " von " + MAFO);

				frame.addWindowListener(new MyWindowListener(app));
				Component contents = app.createComponents();
				// app.createMenus(frame);
				Container contentPane = frame.getContentPane();
				contentPane.setLayout(new BorderLayout());
				contentPane.add(contents, BorderLayout.CENTER);
				JPanel statusBar = new JPanel(new GridLayout(1, 0));
				statusBarText = new JLabel("  ");
				statusBar.add(statusBarText);
				statusBarProgress = new JProgressBar();
				statusBar.add(statusBarProgress);
				contentPane.add(statusBar, BorderLayout.SOUTH);

				// Display the window. in the center of the screen
				Toolkit tk = Toolkit.getDefaultToolkit();
				Dimension screen = tk.getScreenSize();
				int sw = (int) screen.getWidth();
				int sh = (int) screen.getHeight();
				int myW = Math.min((int) (sw * 0.9), 800);
				int myH = Math.min((int) (sh * 0.9), 680);
				Dimension mySize = new Dimension(myW, myH);
				frame.setPreferredSize(mySize);
				int x = (int) (sw - mySize.getWidth()) / 2;
				int y = (int) (sh - mySize.getHeight()) / 2;
				frame.setLocation(x, y);
				frame.setIconImage(new ImageIcon("resources/ovt-mafo-frame.gif").getImage());

				frame.pack();
				frame.setVisible(true);

				// start collecting data
				app.getdata();

			} else {
				MyLog.logError("could not load db: " + Database.dbURL());
				JLabel error = new JLabel("<html>Konnte nicht auf die Datenbank zugreifen!" + "<br>DB-Url: " + Database.dbURLNoPass()
						+ "</html>");
				error.setFont(new Font("Tahoma", Font.BOLD, 24));
				frame.getContentPane().add(error, BorderLayout.CENTER);
				// Display the window.
				Dimension mySize = new Dimension(500, 500);
				frame.setPreferredSize(mySize);
				// Get the screen dimensions.
				Toolkit tk = Toolkit.getDefaultToolkit();
				Dimension screen = tk.getScreenSize();
				MyLog.logDebug("MF-Assi Start mit Bildschirmauflösung: " + screen);
				int x = (int) (screen.getWidth() - mySize.getWidth()) / 2;
				int y = (int) (screen.getWidth() - mySize.getHeight()) / 2;
				System.out.println(x + ":" + y);
				frame.setLocation(x, y);

				frame.pack();
				frame.setVisible(true);
			}
		}
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
		menu.getAccessibleContext().setAccessibleDescription("Hier sind Aktionen abgelegt, die Sie unternehmen können.");
		menuBar.add(menu);

		// a group of JMenuItems
		JMenuItem menuItem = new JMenuItem("Protkolldatei an Zentrale senden", KeyEvent.VK_T);
		menuItem.getAccessibleContext().setAccessibleDescription("Die Protokolldatei an Zentrale senden");
		menuItem.addActionListener(this);
		menuItem.setActionCommand("sendprotokoll");
		menu.add(menuItem);

		frame.setJMenuBar(menuBar);
	}

	/**
	 * fill main panel with components. main panel shows a tabbedpane with panes
	 * for information, contact working and about the program
	 * 
	 * @return the components for main panel
	 */
	public Component createComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		this.mainTabs = new JTabbedPane();

		MyLog.logDebug("load mafoInfoPanel");
		this.mafoInfoPanel = this.makeMafoInfoPanel();
		this.mainTabs.addTab("Informationen und Hilfe", this.mafoInfoPanel);

		MyLog.logDebug("load contactPanel");
		this.contactPanel = new ContactPanel(this.cb, MAFO, this);
		this.mainTabs.addTab(this.contactPanel.getInputTabName(), this.contactPanel);

		// MyLog.logDebug("load logPanel");
		// this.mainTabs.addTab("Aktionslog", this.makeLogPanel());

		MyLog.logDebug("load infoPanel");
		OVTInfoPanel ip = new OVTInfoPanel(applicationName + " " + versionString);
		this.mainTabs.addTab("Über " + applicationName, ip);
		this.mainTabs.addChangeListener(ip);

		this.mainTabs.setSelectedIndex(0);
		mainPanel.add(this.mainTabs);
		return mainPanel;
	}

	private JPanel makeLogPanel() {
		JPanel logPane = new JPanel(new BorderLayout());
		this.log = new JTextArea();
		reloadAktionLog();
		JScrollPane logScroller = new JScrollPane(this.log);
		// logScroller.setPreferredSize(new Dimension(800,520));
		logPane.add(logScroller, BorderLayout.CENTER);
		JPanel buttPane = new JPanel(new GridLayout(0, 1));
		JButton reload = new JButton("Aktualisieren");
		reload.addActionListener(this);
		reload.setActionCommand("reload");
		buttPane.add(reload);
		JButton print = new JButton("Drucken");
		print.addActionListener(this);
		print.setActionCommand("print");
		buttPane.add(print);
		logPane.add(buttPane, BorderLayout.SOUTH);
		return logPane;
	}

	private void reloadAktionLog() {
		StringBuffer sb = new StringBuffer();
		try {
			File f = new File(ActionLogger.logFileName);
			FileReader in = new FileReader(f);
			int bytesRead = 0;
			char[] charArr = new char[512];
			while ((bytesRead = in.read(charArr)) > 0) {
				sb.append(charArr, 0, bytesRead);
			}
		} catch (FileNotFoundException e) {
			MyLog.logError(e);
		} catch (IOException e) {
			MyLog.showExceptionErrorDialog(e);
		}
		this.log.setText(sb.toString());
	}

	private void printAktionLog() {
		try {
			this.log.print();
		} catch (PrinterException e) {
			MyLog.showExceptionErrorDialog(e);
		}
	}

	/**
	 * make a panel with info about mafo statistics and a sendprotokoll button,
	 * that send a logfile via ftp to bastis server.
	 * 
	 * @return the components for mfinfo panel
	 */
	public JPanel makeMafoInfoPanel() {
		JPanel mafoInfo = new JPanel(new BorderLayout());

		JPanel tabPanel = new JPanel(new BorderLayout());
		this.mfInfoTable = new MafoInfoTable(MAFO);
		this.startInfoPanelUpdate();
		JScrollPane jsp = new JScrollPane(this.mfInfoTable);
		tabPanel.add(jsp);
		tabPanel.setPreferredSize(new Dimension(300, 300));
		mafoInfo.add(tabPanel, BorderLayout.NORTH);

		JButton logSendButton = new JButton("Protokolldatei an die Zentrale schicken");
		logSendButton.addActionListener(this);
		logSendButton.setActionCommand("sendprotokoll");
		logSendButton.setPreferredSize(new Dimension(300, 300));
		logSendButton.setFont(new Font("Tahoma", Font.BOLD, 20));
		mafoInfo.add(logSendButton, BorderLayout.SOUTH);
		return mafoInfo;
	}

	/**
	 * this send changed data back to db
	 */
	public void sendData() {
		// go trough changed contacts and save them
		Vector<Contact> cC = this.cb.getChangedContacts();
		MyLog.logDebug("senddata: " + cC.size() + " changed Contacts");
		for (Iterator<Contact> iter = cC.iterator(); iter.hasNext();) {
			Contact c = iter.next();
			// System.out.println(c+":"+c.isChanged());
			c.saveToDB();
		}

		// TODO: i think this is unnecessary because we didnt set them to "at
		// mafo"-state...
		// Vector<Contact> stillWaiting = this.cb.getUnchangedContacts();
		// ContactBundle sw = new ContactBundle(stillWaiting);
		// sw.setStatusToWaiting();

		// set cb to null
		this.cb = null;
	}

	/**
	 * this gets data for actual mafo from db so it can be used
	 */
	private void getdata() {
		if (MAFO != null) {
			MyLog.logDebug("getdata for " + MAFO);
			this.cb = new ContactBundle(MAFO, this.contactPanel, this);
			this.contactPanel.setCB(this.cb);
			this.cb.execute();

		} else {
			MyLog.logError("no MAFO given for data retrieval");
			JOptionPane.showMessageDialog(frame, "Es wurde kein Mafo angegeben", "Hinweis", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void disableDataInputPanel() {
		this.mainTabs.setEnabledAt(1, false);
	}

	// ================ actions =====================================
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.equals("getdata")) {
			this.getdata();

		} else if (com.equals("sendprotokoll")) {
			if (this.ftpThread == null || (this.ftpThread != null && !this.ftpThread.isAlive())) {
				this.ftpThread = FTPTool.sendLogFile(MAFO.rawName());
			} else {
				MyLog.logDebug("tried to hit ftp button while thread is running");
			}
		} else if (com.equals("senddata")) {
			this.sendData();
		} else if (com.equals("reload")) {
			this.reloadAktionLog();
		} else if (com.equals("print")) {
			this.printAktionLog();
		}
	}

	// =============== getters
	public ContactBundle getCb() {
		return cb;
	}

	public void startStatusProgress() {
		statusBarProgress.setIndeterminate(true);
	}

	public void stopStatusProgress() {
		statusBarProgress.setIndeterminate(false);
	}

	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	public static void main(String[] args) {
		MyLog.logDebug("OVTMafo started ver." + versionString);
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	public MafoInfoTable getMfInfoTable() {
		return mfInfoTable;
	}

	@Override
	public void startInfoPanelUpdate() {
		String infoPanelActive = SettingsReader.getString("OVTMafoClient.InfoPanelActive");
		if (!infoPanelActive.equals("no")) {
			this.mfInfoTable.run();
		}
	}

	@Override
	public JFrame getFrame() {
		return frame;
	}

	@Override
	public void setDefaultCursor() {
		if (frame != null) {
			frame.getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	@Override
	public void setStatusText(String txt) {
		statusBarText.setText(txt);
	}

	@Override
	public void setWaitCursor() {
		if (frame != null) {
			frame.getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}
	}

	@Override
	public ContactBundle getCB() {
		return this.cb;
	}
}

// ============ WindowAdapter ========================
/**
 * helper class that listens if user closes programm and if there is something
 * to save. we dont wan to leave anyone without saving.
 */
class MyWindowListener extends WindowAdapter {
	private IMFAssiWindow prog;

	public MyWindowListener(IMFAssiWindow pw) {
		this.prog = pw;
	}

	private void checkForChange() {
		if (this.prog.getCB() != null && this.prog.getCB().hasChangedContacts()) {
			Object[] options = { "Daten in der Datenbank speichern", "Abbrechen" };
			int n = JOptionPane.showOptionDialog(prog.getFrame(), "Sie haben Änderungen/Ergänzungen gemacht.", "Geänderte Daten!",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				// send and close
				MyLog.logDebug("MF-Assi beenden start");
				this.prog.sendData();
				MyLog.logDebug("MF-Assi beenden ende");
				System.exit(1);
			} else {
			}
		} else {
			System.exit(1);
		}
	}

	public void windowClosed(WindowEvent e) {
		// this is called at app exit check if something is changed
		this.checkForChange();
	}

	public void windowClosing(WindowEvent e) {
		// this is called at app exit check if something is changed
		this.checkForChange();
	}
}