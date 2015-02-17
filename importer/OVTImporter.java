package importer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.xnap.commons.gui.DirectoryChooser;

import tools.MyLog;
import tools.SettingsReader;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import db.Contact;
import db.DBTools;
import db.Database;
import db.Gespraech;
import db.Marktforscher;
import db.Projektleiter;

public class OVTImporter implements ActionListener, Runnable, ChangeListener {

	private enum ROWERG {
		OK, DOUBLE, SHFCHANGE, FAIL, TNFALSCH, DOUBLEOVERWRITE
	};

	private static JFrame frame;
	private static JLabel statusBar;
	private JTextField dir;
	private JLabel importiert;
	private JLabel doubles;
	private JLabel typChange;
	private int doublesCount = 0;
	private int importCount = 0;
	private int typChangeCount = 0;
	private JScrollPane logScroller;
	private JTextArea log;
	private StringBuffer output = new StringBuffer();
	private ProgressMonitor pm;
	private JCheckBox onlySolar;
	private JCheckBox alles;
	private JCheckBox onlyFenster;
	private JCheckBox overwrite;
	private JCheckBox onlyEmptyPreNames;
	private JTextField impTag;
	private boolean cancelled;
	private JPanel mainPanel;
	private JCheckBox forceMDE;
	private JCheckBox tnFalsch;
	private JCheckBox expandSHFFlag;

	public OVTImporter() {
	}

	/**
	 * Create the GUI and show it. checks for update of program. checks if
	 * honorarabrechnungverzeichnis is set properly. free all temp records from
	 * db.
	 */
	private static void createAndShowGUI() {
		PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
		try {
			UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
		} catch (Exception e) {
		}

		// Create and set up the window.
		frame = new JFrame("OVT Importer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// check for database
		if (Database.test()) {

			OVTImporter app = new OVTImporter();
			Component contents = app.createComponents();
			Container contentPane = frame.getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(contents, BorderLayout.CENTER);
			statusBar = new JLabel("  ");
			contentPane.add("South", statusBar);

			// Display the window.
			Dimension mySize = new Dimension(800, 700);
			frame.setPreferredSize(mySize);
			Toolkit tk = Toolkit.getDefaultToolkit();
			Dimension screen = tk.getScreenSize();
			int x = (int) (screen.getWidth() - mySize.getWidth()) / 2;
			int y = (int) (screen.getHeight() - mySize.getHeight()) / 2;
			frame.setLocation(x, y);

			frame.pack();
			frame.setVisible(true);

		} else {
			JLabel error = new JLabel("<html>Konnte nicht auf die Datenbank zugreifen!" + "<br>DB-Url: " + Database.dbURLNoPass()
					+ "</html>");
			error.setFont(new Font("Tahoma", Font.BOLD, 24));
			frame.getContentPane().add(error, BorderLayout.CENTER);
			// Display the window.
			frame.setPreferredSize(new Dimension(600, 600));
			frame.pack();
			frame.setVisible(true);
		}
	}

	public Component createComponents() {
		this.mainPanel = new JPanel(new BorderLayout());

		// infos
		JPanel infoPane = new JPanel(new GridLayout(0, 2));
		JButton dirSelBut = new JButton("Importverzeichnis... ");
		dirSelBut.setActionCommand("importdir");
		dirSelBut.addActionListener(this);
		infoPane.add(dirSelBut);
		this.dir = new JTextField(SettingsReader.getString("OVTAdmin.importVerzeichnis"));
		infoPane.add(this.dir);
		infoPane.add(new JLabel("Kontakte sind:"));
		JPanel checkboxes = new JPanel(new GridLayout(0, 3));
		this.alles = new JCheckBox("alles");
		this.alles.addChangeListener(this);
		checkboxes.add(this.alles);
		this.onlyFenster = new JCheckBox("nur Fenster");
		this.onlyFenster.addChangeListener(this);
		checkboxes.add(this.onlyFenster);
		this.onlySolar = new JCheckBox("nur Solar");
		this.onlySolar.addChangeListener(this);
		checkboxes.add(this.onlySolar);
		checkboxes.add(this.onlyFenster);
		this.expandSHFFlag = new JCheckBox("Kontaktart erweitern");
		this.expandSHFFlag.addChangeListener(this);
		this.expandSHFFlag
				.setToolTipText("Wenn ein Kontakt Solar ist und man hier und bei Fenster einen Haken setzt ist der Kontakt nachher Fenster und Solar. Ohne den Haken hier ist er danach nur Fenster");
		checkboxes.add(this.expandSHFFlag);
		infoPane.add(checkboxes);

		infoPane.add(new JLabel("Spezial:"));
		JPanel checkboxes2 = new JPanel(new GridLayout(0, 3));
		this.overwrite = new JCheckBox("Doubletten überschreiben");
		this.overwrite.setToolTipText("Doppelte Einträge nicht verwerfen, sondern mit neuer Adresse überschreiben");
		checkboxes2.add(this.overwrite);
		this.onlyEmptyPreNames = new JCheckBox("Nur leere Vornamen");
		this.onlyEmptyPreNames.setToolTipText("Nur Adressen mit leerem Vornamen importieren");
		checkboxes2.add(this.onlyEmptyPreNames);
		this.forceMDE = new JCheckBox("MDE setzen");
		this.forceMDE.setToolTipText("Setzt bei vorhandenen Adressen den MDE neu");
		checkboxes2.add(this.forceMDE);
		this.tnFalsch = new JCheckBox("TN falsch");
		this.tnFalsch
				.setToolTipText("Spezielle TN falsch Behandlung: Wenn Doublette und Name, Straße, Hausnummer und Telefon gleich, dann markieren und Sperrung zurücksetzen");
		checkboxes2.add(this.tnFalsch);
		infoPane.add(checkboxes2);
		infoPane.add(new JLabel("Wiedererkennungszeichen:"));
		this.impTag = new JTextField();
		infoPane.add(this.impTag);
		infoPane.add(new JLabel("Doubletten:"));
		this.doubles = new JLabel("n/a");
		infoPane.add(this.doubles);
		infoPane.add(new JLabel("Importiert:"));
		this.importiert = new JLabel("n/a");
		infoPane.add(this.importiert);
		infoPane.add(new JLabel("Typ geändert:"));
		this.typChange = new JLabel("n/a");
		infoPane.add(this.typChange);
		this.mainPanel.add(infoPane, BorderLayout.NORTH);

		// log
		JPanel logPane = new JPanel(new BorderLayout());
		this.log = new JTextArea();
		this.logScroller = new JScrollPane(this.log);
		this.logScroller.setPreferredSize(new Dimension(800, 520));
		logPane.add(this.logScroller);
		this.mainPanel.add(logPane, BorderLayout.CENTER);

		// aktion buttons
		JPanel butPane = new JPanel(new GridLayout(0, 1));
		JButton savelog = new JButton("Import starten");
		savelog.setActionCommand("startimport");
		savelog.addActionListener(this);
		butPane.add(savelog);
		JButton startImport = new JButton("Log sichern");
		startImport.setActionCommand("savelog");
		startImport.addActionListener(this);
		butPane.add(startImport);
		// JButton exit = new JButton("Beenden");
		// exit.setActionCommand("exit");
		// exit.addActionListener(this);
		// butPane.add(exit);
		this.mainPanel.add(butPane, BorderLayout.SOUTH);

		this.pm = new ProgressMonitor(frame, "Import", "", 0, 10);
		this.pm.setMillisToPopup(1);
		this.pm.setMillisToDecideToPopup(1);

		return this.mainPanel;
	}

	public void run() {
		this.cancelled = false;
		// get files from actual directory
		File[] toImp = new File(this.dir.getText()).listFiles(new FilenameFilter() {
			public boolean accept(File d, String s) {
				if (d.isDirectory()) {
					if ((s.endsWith("csv") || s.endsWith("xls")) && !s.startsWith("#")) {
						return true;
					}
				}
				return false;
			}
		});

		// go trough all files
		int MAXIMP = Math.max(0, toImp.length);
		this.pm.setMaximum(MAXIMP);
		for (int i = 0; i < MAXIMP; i++) {
			if (this.pm.isCanceled()) {
				cancelled = true;
				break;
			}
			// Set new state
			pm.setProgress(i + 1);

			try {
				File file = toImp[i];
				String fname = file.getName().replace("-", "_");
				this.logME("\n>---------------------------------------------------------- " + (i + 1) + " von " + MAXIMP + "\n");
				this.logME(">" + fname + "\n");

				// Change the note if desired
				String state = "Bearbeite " + (i + 1) + " von " + MAXIMP + "  : " + fname;
				pm.setNote(state);

				// date of dataaquisition, is in filename
				String dateStr = OVTImportHelper.strToken(fname, 2, "_");
				if (dateStr.length() == 6 && !Character.isDigit(dateStr.charAt(0))) {
					dateStr = OVTImportHelper.strToken(fname, 3, "_");
					if (dateStr.length() == 6 && !Character.isDigit(dateStr.charAt(0))) {
						dateStr = OVTImportHelper.strToken(fname, 4, "_");
						if (dateStr.length() == 6 && !Character.isDigit(dateStr.charAt(0))) {
							dateStr = OVTImportHelper.strToken(fname, 5, "_");
							if (dateStr.length() == 6 && !Character.isDigit(dateStr.charAt(0))) {
								dateStr = OVTImportHelper.strToken(fname, 6, "_");
								if (dateStr.length() == 6 && !Character.isDigit(dateStr.charAt(0))) {
									dateStr = OVTImportHelper.strToken(fname, 7, "_");
								}
							}
						}
					}
				}
				Date angelegt = null;
				if (dateStr.length() >= 6) {
					try {
						Calendar cal = Calendar.getInstance();
						int year = Integer.parseInt(dateStr.substring(0, 2)) + 2000;
						int month = Integer.parseInt(dateStr.substring(2, 4)) - 1;
						int day = Integer.parseInt(dateStr.substring(4, 6));
						cal.set(year, month, day);
						angelegt = new Date(cal.getTimeInMillis());
						this.logME("> datum:" + dateStr + " >> " + angelegt + "\n");
					} catch (NumberFormatException ex) {
						this.logME(">>>> kann datum nicht lesen :" + dateStr + "\n");
					}
				}

				// get date of aktions we use the filedate
				Date aktionDate = new Date(file.lastModified());
				if (angelegt == null) {
					angelegt = aktionDate;
				}

				if (fname.endsWith("csv")) {
					this.importCSVFile(file, fname, angelegt, "184");
				} else if (fname.endsWith("xls")) {
					// bearbeiter is also in filename
					String bearbeiter = OVTImportHelper.extractMarktdatenermittler(fname);
					this.logME("> marktdatenermittler: " + bearbeiter + "\n");
					this.importXLSFile(file, fname, angelegt, bearbeiter);
				}
			} catch (NumberFormatException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		if (!this.cancelled) {
			this.logME("================== FERTIG ====================" + "\n");
			this.pm.close();
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null, "Fertig importiert");
		} else {
			JOptionPane.showMessageDialog(null, "Import abgebrochen");
		}
		this.cancelled = false;
	}

	private void importXLSFile(File file, String fname, Date angelegt, String bearbeiter) {
		try {
			int doubleCount = 0;
			int shfChangeCount = 0;
			int singleCount = 0;
			int failCount = 0;
			int tnFalschCount = 0;
			int doubleOverwriteCount = 0;
			if (file == null)
				return;

			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheet("Marktdaten");
			if (sheet == null) {
				int nr = 1;
				sheet = wb.getSheetAt(nr);
				this.logME("   >got sheet by number, named: " + wb.getSheetName(nr) + "\n");
			}
			if (sheet != null) {
				// find out which format we have...
				int layout = -1;
				HSSFRow firstrow = sheet.getRow(0);
				if (firstrow != null) {
					HSSFCell testcell0 = firstrow.getCell((short) 0);
					String inh0 = "";
					if (testcell0 != null) {
						inh0 = OVTImportHelper.getInhalt(firstrow, (short) 0);
					}
					String inh1 = "";
					HSSFCell testcell1 = firstrow.getCell((short) 1);
					if (testcell1 != null) {
						inh1 = OVTImportHelper.getInhalt(firstrow, (short) 1);
					}

					if (inh1.startsWith("Ein")) {
						// ergColumn = 13;
						layout = 1;
						this.logME("   >excel format: eins" + "\n");
					} else if (inh0.equals(".") && inh1.startsWith("Marktdaten")) {
						// ergColumn = 11;
						layout = 2;
						this.logME("   >excel format: zwei" + "\n");
					} else if (inh0.startsWith("Marktdaten")) {
						// ergColumn = 12;
						layout = 3;
						this.logME("   >excel format: drei" + "\n");

					} else {
						this.logME("   >hilfe................." + "\n");
					}
				}
				// go trough about 30 rows
				int maxrows = 1200;
				for (int r = 4; r <= maxrows; r++) {
					if (this.pm.isCanceled()) {
						this.cancelled = true;
						break;
					}
					// test...
					HSSFRow row = sheet.getRow(r);
					if (row != null) {
						String test = OVTImportHelper.getInhalt(row, (short) 1);
						if (test.length() > 0) {
							ROWERG myRowErg = this.importRow(sheet, row, layout, bearbeiter, angelegt, angelegt, fname);
							switch (myRowErg) {
							case DOUBLE:
								doubleCount++;
								break;
							case SHFCHANGE:
								shfChangeCount++;
								break;
							case OK:
								singleCount++;
								break;
							case FAIL:
								failCount++;
								break;
							case TNFALSCH:
								tnFalschCount++;
								break;
							case DOUBLEOVERWRITE:
								doubleOverwriteCount++;
								break;

							default:
								break;
							}
						}
					}
				}
			} else {
				this.logME("   >no valid sheet" + "\n");
			}

			try {
				// log what we have done
				PreparedStatement insertIntoImportLog = Database
						.getPreparedStatement("INSERT INTO importlog (sourcefile, doubles, imported, impdate, shfflag, imptag, tnfalsefixed, doubleoverwrite) VALUES (?,?,?,NOW(),?,?,?,?)");
				insertIntoImportLog.setString(1, fname);
				insertIntoImportLog.setInt(2, doubleCount);
				insertIntoImportLog.setInt(3, singleCount);
				insertIntoImportLog.setInt(4, this.impType());
				insertIntoImportLog.setString(5, this.impTag.getText());
				insertIntoImportLog.setInt(6, tnFalschCount);
				insertIntoImportLog.setInt(7, doubleOverwriteCount);
				insertIntoImportLog.executeUpdate();
				Database.close(insertIntoImportLog);
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} catch (IOException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}

	}

	private void importCSVFile(File file, String fname, Date angelegt, String bearbeiter) {
		try {
			this.logME("   >importCSVFile, bevor filereader" + "\n");
			Reader f = new FileReader(file);
			this.logME("   >importCSVFile, nach filereader" + "\n");

			PreparedStatement p = null;
			LabeledCSVParser lcsvp = new LabeledCSVParser(new CSVParser(f));
			while (lcsvp.getLine() != null) {
				if (this.pm.isCanceled()) {
					this.cancelled = true;
					break;
				}
				// get street and hausnummer
				String strNrInh = lcsvp.getValueByLabel("STRASSE");
				String strasseInh = OVTImportHelper.getStrasse(strNrInh);
				String hausNrInh = OVTImportHelper.getHausNr(strNrInh);

				try {
					// insert into kunden
					p = Database.getPreparedStatement("INSERT INTO kunden "
							+ "(nachname, vorname, strasse, hausnummer, plz, stadt, telprivat, bearbeiter, angelegt, "
							+ "sourcefile, notiz, bearbeitungsstatus, shfflag) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
					p.setString(1, lcsvp.getValueByLabel("NAME2"));
					p.setString(2, lcsvp.getValueByLabel("NAME1"));
					p.setString(3, strasseInh);
					p.setString(4, hausNrInh);
					p.setString(5, lcsvp.getValueByLabel("PLZ").replace("D-", ""));
					p.setString(6, lcsvp.getValueByLabel("ORT"));
					String tel = OVTImportHelper.stripNumber(lcsvp.getValueByLabel("TELEFON"));
					p.setString(7, tel);
					p.setString(8, bearbeiter); // means letter24
					p.setDate(9, angelegt);
					p.setString(10, fname);
					p.setString(11, this.impTag.getText());
					p.setString(12, "0");
					p.setInt(13, this.impType());
					p.executeUpdate();

					// get id of last kunde
					int lastID = -1;
					ResultSet rs = p.getGeneratedKeys();
					while (rs.next()) {
						lastID = rs.getInt(1);
					}

					this.logME("Importiert als: " + lastID + "\n");
					this.importiert.setText(Integer.toString(++this.importCount));
				} catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException mysqlex) {
					this.logME("   >doppelt: " + lcsvp.getValueByLabel("NAME2") + ", " + lcsvp.getValueByLabel("NAME1") + "\n");
					this.doubles.setText(Integer.toString(++this.doublesCount));
				} catch (IllegalStateException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				} catch (SQLException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				} finally {
					Database.close(p);
				}
			}
		} catch (IOException e) {
			System.out.println("Fehler beim Lesen der Datei");
		}
	}

	private ROWERG importRow(HSSFSheet sheet, HSSFRow row, int layout, String bearbInh, Date angelegt, Date aktionDate, String fname) {
		ROWERG rowImpErg = ROWERG.FAIL;
		short c = 1;
		// the normal case, all fields are there and follow each other
		short nNameCol = c++, vNameCol = c++, strasseNrCol = c++, plzOrtCol = c++, telefonCol = c++, fensterCol = c++, gbsCol = c++, haustuerFarbeCol = c++, fassadePutzCol = c++, fassadeKlinkerCol = c++, fassadeFarbeCol = c++, zaunCol = c++, ergCol = c++, bearbCol = c++;

		String vNameInh = "", nNameInh = "", strasseInh = "", hausNrInh = "", plzInh = "", ortInh = "", telefonInh = "", fensterInh = "", gbsInh = "", haustuerFarbeInh = "", fassadePutzInh = "", fassadenArtInh = "", fassadeKlinkerInh = "", fassadeFarbeInh = "", zaunInh = "", ergInh = "";

		// make special settings for different layouts
		switch (layout) {
		case 2:
			// special case, no zaun, no bearbeiter, ex:
			// 63_Hasselroth_Niedermittlau-Bahnhofsiedlung_031124_2_Poth
			ergCol++;
			bearbCol++;
			zaunCol = -1;
			bearbCol = -1;
			break;
		case 3:
			// special case, no numbering, no bearbeiter ,ex: 61 Kronberg
			// Schönberg 030527_3 Maler
			c = 0;
			nNameCol = c++;
			vNameCol = c++;
			strasseNrCol = c++;
			plzOrtCol = c++;
			telefonCol = c++;
			c++;
			c++;
			fensterCol = c++;
			gbsCol = -1;
			haustuerFarbeCol = c++;
			fassadePutzCol = c++;
			fassadeKlinkerCol = c++;
			fassadeFarbeCol = c++;
			zaunCol = -1;
			ergCol = c++;
			bearbCol = -1;
			break;

		default:
			// notting to do here...
			break;
		}

		// get easy values
		vNameInh = OVTImportHelper.getInhalt(row, vNameCol);
		nNameInh = OVTImportHelper.getInhalt(row, nNameCol);

		// special for import of wrong imported files
		if (vNameInh.length() > 0 && this.onlyEmptyPreNames.isSelected()) {
			return ROWERG.FAIL;
		}

		if (nNameInh.length() > 0) {
			telefonInh = OVTImportHelper.getInhalt(row, telefonCol);
			String tttt = OVTImportHelper.getInhalt(row, fensterCol);
			fensterInh = OVTImportHelper.strToken(tttt, 1, " ");
			gbsInh = OVTImportHelper.getInhalt(row, gbsCol);
			fassadeKlinkerInh = OVTImportHelper.getInhalt(row, fassadeKlinkerCol);
			fassadePutzInh = OVTImportHelper.getInhalt(row, fassadePutzCol);
			zaunInh = OVTImportHelper.getInhalt(row, zaunCol);

			// get street and hausnummer
			String strNrInh = OVTImportHelper.getInhalt(row, strasseNrCol);
			strasseInh = OVTImportHelper.getStrasse(strNrInh);
			hausNrInh = OVTImportHelper.getHausNr(strNrInh);

			// get plz and ort
			String plzOrtInh = OVTImportHelper.getInhalt(row, plzOrtCol);
			plzInh = OVTImportHelper.getPLZ(plzOrtInh);
			ortInh = OVTImportHelper.getOrt(plzOrtInh);

			// get colors
			String haustuerFarbeReal = OVTImportHelper.getInhalt(row, haustuerFarbeCol);
			String fassadeFarbeReal = OVTImportHelper.getInhalt(row, fassadeFarbeCol);
			haustuerFarbeInh = OVTImportHelper.interpreteColor(haustuerFarbeReal);
			fassadeFarbeInh = OVTImportHelper.interpreteColor(fassadeFarbeReal);

			// fassadenart: 1=Putz ; 2=Klinker; 3=none
			if (fassadePutzInh.length() > 0) {
				fassadenArtInh = "1";
			} else if (fassadeKlinkerInh.length() > 0) {
				fassadenArtInh = "2";
			} else {
				fassadenArtInh = "3";
			}

			// txt of ergebnis
			ergInh = OVTImportHelper.getInhalt(row, ergCol).trim();

			// make sql command for kunde
			PreparedStatement insertIntoBackup = null;
			PreparedStatement insertKundeStatement = null;
			String tel = "";
			tel = OVTImportHelper.stripNumber(telefonInh);
			if (tel.length() <= 0) {
				this.logME("### keine Telefonnummer in Zeile " + row.getRowNum() + "\n");
				return ROWERG.FAIL;
			}
			try {
				// insert into kunden
				insertKundeStatement = Database.getPreparedStatement("INSERT INTO kunden "
						+ "(nachname, vorname, strasse, hausnummer, plz, stadt, telprivat, fensterzahl, "
						+ "glasbausteine, haustuerfarbe, fassadenart, fassadenfarbe, zaunlaenge, bearbeiter, angelegt, "
						+ "bearbeitungsstatus, sourcefile, shfflag, importtag) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				insertKundeStatement.setString(1, nNameInh);
				insertKundeStatement.setString(2, vNameInh);
				insertKundeStatement.setString(3, strasseInh);
				insertKundeStatement.setString(4, hausNrInh);
				insertKundeStatement.setString(5, plzInh);
				insertKundeStatement.setString(6, ortInh);
				insertKundeStatement.setString(7, tel);
				if (fensterInh.length() > 0) {
					try {
						insertKundeStatement.setInt(8, (int) Double.parseDouble(fensterInh));
					} catch (NumberFormatException e) {
						insertKundeStatement.setInt(8, 0);
						e.printStackTrace();
					}
				} else {
					insertKundeStatement.setInt(8, 0);
				}
				// glasbausteine
				if (gbsInh.length() > 0) {
					insertKundeStatement.setInt(9, 1);
				} else {
					insertKundeStatement.setInt(9, 0);
				}
				insertKundeStatement.setInt(10, Integer.parseInt(haustuerFarbeInh));
				insertKundeStatement.setString(11, fassadenArtInh); // kommt aus
				// fassadePutzInh
				// und fassadeKlinkerInh
				insertKundeStatement.setInt(12, Integer.parseInt(fassadeFarbeInh));
				if (zaunInh.length() > 0) {
					try {
						insertKundeStatement.setInt(13, (int) Double.parseDouble(zaunInh));
					} catch (NumberFormatException e) {
						insertKundeStatement.setInt(13, 0);
						e.printStackTrace();
					}
				} else {
					insertKundeStatement.setInt(13, 0);
				}
				insertKundeStatement.setString(14, bearbInh);
				insertKundeStatement.setDate(15, angelegt);
				insertKundeStatement.setString(16, "0");
				insertKundeStatement.setString(17, fname);
				insertKundeStatement.setInt(18, this.impType());
				insertKundeStatement.setString(19, this.impTag.getText());
				insertKundeStatement.executeUpdate();

				// this doesnt happen when is double:S

				// get id of last kunde
				int lastID = -1;
				ResultSet rs = insertKundeStatement.getGeneratedKeys();
				while (rs.next()) {
					lastID = rs.getInt(1);
				}

				this.logME(row.getRowNum() + ">> Importiert als: " + lastID + "\n");
				this.importiert.setText(Integer.toString(++this.importCount));
				rowImpErg = ROWERG.OK;

				// insert into backup
				insertIntoBackup = Database.getPreparedStatement("INSERT INTO backup "
						+ "(kunde, ergebnis, tuerfarbe, fassadenfarbe, strort) " + "VALUES (?,?,?,?,?)");
				insertIntoBackup.setInt(1, lastID);
				insertIntoBackup.setString(2, ergInh);
				insertIntoBackup.setString(3, haustuerFarbeReal);
				insertIntoBackup.setString(4, fassadeFarbeReal);
				insertIntoBackup.setString(5, strNrInh);
				insertIntoBackup.executeUpdate();

				// do ergebnis stuff
				if (lastID > -1) {
					doErgebnisStuff(sheet, row, aktionDate, bearbCol, ergInh, lastID, OVTImportHelper.getInhalt(row, bearbCol), fname);
				}
			} catch (SQLException insertSQLException) {
				if (insertSQLException.getMessage().indexOf("Duplicate") >= 0) {
					// if overwrite is selected, try to delete the existing
					// record and insert new one again
					if (this.tnFalsch.isSelected()) {
						rowImpErg = doTNFalsch(tel, nNameInh, strasseInh, hausNrInh, plzInh);
						// if it was not tn falsch the normal procedure can
						// happen, so ROWERG.FAIL is returned
					}
					if (rowImpErg == ROWERG.FAIL && this.overwrite.isSelected()) {
						doOverwrite(sheet, row, insertKundeStatement, insertSQLException, bearbCol, ergInh, tel, aktionDate, fname);
					} else if (rowImpErg == ROWERG.FAIL) {
						// e.printStackTrace();

						this.logME(row.getRowNum() + " >>>> doppelt: " + nNameInh + ", " + vNameInh + " aus " + ortInh + " xls: " + fname
								+ "\n");
						this.logME(" >>>> " + insertKundeStatement.toString() + "\n");
						this.logME(" >>>> " + insertSQLException.getLocalizedMessage() + "\n\n");
						rowImpErg = ROWERG.DOUBLE;

						// get existing record
						rowImpErg = fixExistingRecord(rowImpErg, tel, bearbInh);

						// set counter on display
						if (rowImpErg == ROWERG.DOUBLE) {
							this.doubles.setText(Integer.toString(++this.doublesCount));
						} else if (rowImpErg == ROWERG.SHFCHANGE) {
							this.typChange.setText(Integer.toString(++this.typChangeCount));
						}

						// do this by hand....
						// doOldrecordFix(fname, vNameInh, nNameInh, plzInh,
						// ortInh,
						// fensterInh, haustuerFarbeInh, fassadeFarbeInh,
						// doOldrecordFix);
					}
				}
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
			} finally {
				if (insertKundeStatement != null)
					Database.close(insertKundeStatement);
				if (insertIntoBackup != null)
					Database.close(insertIntoBackup);
			}
		}
		return rowImpErg;
	}

	private ROWERG doTNFalsch(String tel, String nNameInh, String strasseInh, String hausNrInh, String plzInh) {
		String[] existingInfo = Contact.getMainInfos(tel);
		if (nNameInh.equals(existingInfo[1])) {
			if (strasseInh.equals(existingInfo[2])) {
				if (hausNrInh.equals(existingInfo[3])) {
					if (plzInh.equals(existingInfo[4])) {
						if ("99".equals(existingInfo[5])) {
							// ok we got a locked adresse almost the same as the
							// one beeing imported
							// so wee need to remove the aktions with tnfalsch
							// ergs and set bearbeitungsstatus back

							// set all aktionen to a reminder where tn falsch
							// was before
							Database.update("aktionen", "ergebnis=36, bemerkung='fix für tn falsch falsch'", "WHERE kunde="
									+ existingInfo[0]);

							// set bearbeitungsstatus back to normal
							Database
									.update("kunden", "bearbeitungsstatus=0, fix='fix für tn falsch falsch'", "WHERE id=" + existingInfo[0]);

							return ROWERG.TNFALSCH;
						}
					}
				}
			}
		}
		return ROWERG.FAIL;
	}

	private void doErgebnisStuff(HSSFSheet sheet, HSSFRow row, Date aktionDate, short bearbCol, String ergInh, int lastID,
			String datePlInh, String fname) {
		// interprete value
		if (ergInh.length() > 0) {
			// test if ergebnis starts with a number, then it is a
			// id for gespräch or aktion
			String theMafo = OVTImportHelper.lookupOrCreateMafo(sheet, row, bearbCol);
			if (Character.isDigit(ergInh.trim().charAt(0))) {
				// now decide if we have the gespraech or aktion type
				if (isAktion(row)) {
					// ergInh is a integer
					// aktiondate need to come from file
					// mafo need also come from filename
					aktionDate = figureOutDateFromFilename(fname);
					theMafo = figureMafoFromFilename(fname);
					int ergAsInt = (int) Double.parseDouble(ergInh);
					String erg = String.valueOf(ergAsInt);
					extractDataAndCreateNewAction(sheet, row, aktionDate, bearbCol, ergInh, lastID, erg, theMafo);
				} else {
					// extract date and pl for the gespraechsbericht
					java.util.Date gDate = extractDateForGespraech(datePlInh);
					Projektleiter wele = extractProjektleiter(datePlInh);
					Gespraech.easyNewGespraech(lastID, ergInh, wele.getId(), gDate);
					this.logME("> Gespräch angelegt: " + wele + " am " + gDate + " mit " + ergInh + "\n");
				}
			} else {
				// ergInh is in form: "KI", "Bekannte", ... as string that needs
				// to be interpreted
				String interpretedErgebnis = OVTImportHelper.interpreteErgebnis(ergInh, lastID);
				extractDataAndCreateNewAction(sheet, row, aktionDate, bearbCol, ergInh, lastID, interpretedErgebnis, theMafo);
			}
		}
	}

	private String figureMafoFromFilename(String fname) {
		int firstUnderscore = fname.indexOf('_');
		String mafoNameString = fname.substring(0, firstUnderscore);
		for (Marktforscher mafo : DBTools.mafoMafoList(false)) {
			if (mafo.getNachName().equals(mafoNameString)) {
				return mafo.getId();
			}
		}
		return "0";
	}

	private Date figureOutDateFromFilename(String fname) {
		int firstUnderscore = fname.indexOf('_') + 1;
		int secondUnderscore = fname.indexOf('_', firstUnderscore);
		String dateString = fname.substring(firstUnderscore, secondUnderscore);
		java.util.Date gDate = new java.util.Date();
		gDate = this.parseDate(dateString, "yyMMdd");
		if (gDate != null) {
			return new Date(gDate.getTime());
		}
		return null;
	}

	private void extractDataAndCreateNewAction(HSSFSheet sheet, HSSFRow row, Date aktionDate, short bearbCol, String ergInh, int lastID,
			String erg, String mafo) {
		if (erg != null && erg.length() > 0 && !erg.trim().equals("0") && !mafo.trim().equals("0")) {
			insertNewAktion(aktionDate, lastID, erg, mafo);
		}
	}

	private boolean isAktion(HSSFRow row) {
		// find out if a aktion is meant by checking content of o-column. if it
		// is empty it should be a aktion
		short column = (short) 14;
		HSSFCell datePlCell = row.getCell(column);
		if (datePlCell == null) {
			return true;
		} else {
			String tst = OVTImportHelper.getInhalt(row, column);
			if (tst != null && tst.length() > 0) {
				return true;
			}
		}
		return false;
	}

	private Projektleiter extractProjektleiter(String wlInh) {
		String weleStr = wlInh.trim().substring(8, wlInh.length());
		Projektleiter wele = new Projektleiter(weleStr, null);
		return wele;
	}

	private java.util.Date extractDateForGespraech(String wlInh) {
		java.util.Date gDate = new java.util.Date();
		if (wlInh.length() >= 8) {
			String gDateStr = wlInh.trim().substring(0, 8);
			gDate = this.parseDate(gDateStr, "dd.MM.yy");
		}
		return gDate;
	}

	private void insertNewAktion(Date aktionDate, int lastID, String ergTyp, String mafo) {
		java.util.Date tmp = new java.util.Date();
		long nowAsLong = tmp.getTime();
		Date eingang = new Date(nowAsLong);
		PreparedStatement p1 = null;
		// add to db
		try {
			p1 = Database.getPreparedStatement("INSERT INTO aktionen "
					+ "(kunde, angelegt, aktionstyp, marktforscher, ergebnis, eingangsdatum) " + "VALUES (?,?,?,?,?,?)");
			p1.setInt(1, lastID);
			p1.setDate(2, aktionDate); // is the date when
			// telefonist did the call
			p1.setInt(3, 1); // aktionstyp is telefon = 1
			p1.setString(4, mafo);
			p1.setString(5, ergTyp);
			p1.setDate(6, eingang); // date when aktion is inserted
			// to db
			p1.executeUpdate();
			this.logME("> Ergebnis angelegt: " + ergTyp + " am " + aktionDate + " von " + mafo + "\n");
		} catch (Exception ex) {
			MyLog.showExceptionErrorDialog(ex);
		} finally {
			if (p1 != null)
				Database.close(p1);
		}
	}

	private void doOldrecordFix(String fname, String vNameInh, String nNameInh, String plzInh, String ortInh, String fensterInh,
			String haustuerFarbeInh, String fassadeFarbeInh, boolean doOldrecordFix) {
		if (doOldrecordFix) {
			// do this because of bad old imports
			// do this because of bad old imports
			// do this because of bad old imports
			PreparedStatement pin = null;
			ResultSet rs = null;
			try {
				int daid = -1;
				int fz = -1;
				rs = Database.select("id, fensterzahl", "kunden", "WHERE nachname='" + nNameInh + "' AND vorname='" + vNameInh
						+ "' AND plz='" + plzInh + "'");
				while (rs.next()) {
					daid = rs.getInt("id");
					fz = rs.getInt("fensterzahl");
				}

				// found one
				int fzahl = 0;
				if (fensterInh.length() > 0) {
					try {
						fzahl = (int) Double.parseDouble(fensterInh);
					} catch (NumberFormatException e1) {
						e1.printStackTrace();
					}
				}
				if (daid > 0 && fz == 0 && fzahl > 0) {
					this.logME("#### neue türdaten einfügen: " + daid + " >>" + nNameInh + ", " + vNameInh + " aus " + ortInh + " xls: "
							+ fname + "\n");
					pin = Database.getPreparedStatement("UPDATE kunden SET fensterzahl=?,haustuerfarbe=?, " + "fassadenfarbe=? WHERE id=?");
					pin.setInt(1, fzahl);
					pin.setString(2, haustuerFarbeInh);
					pin.setString(3, fassadeFarbeInh);
					pin.setInt(4, daid);
					pin.executeUpdate();
				}
			} catch (SQLException e1) {
				MyLog.showExceptionErrorDialog(e1);
				e1.printStackTrace();
			} finally {
				if (pin != null)
					Database.close(pin);
				if (rs != null)
					Database.close(rs);
			}
		}
	}

	private ROWERG fixExistingRecord(ROWERG rowImpErg, String tel, String mde) {
		ResultSet fixExisting = null;
		try {
			fixExisting = Database.select("id, shfflag, fix, bearbeiter", "kunden", "WHERE telprivat=" + tel);
			while (fixExisting.next()) {
				// fix type
				boolean saveMe = false;
				int todoTyp = this.impType();
				int isType = fixExisting.getShort("shfflag");
				if (this.expandSHFFlag.isSelected()) {
					// expand the typ (only solar and only senster =
					// solar+fenster)
					if (todoTyp == 1 && (isType == 2 || isType == 3)) {
						this.logME("   >>>>>> shf typ geändert von: " + isType + " auf: ALLES \n");
						fixExisting.updateInt("shfflag", todoTyp);
						fixExisting.updateString("fix", "shfchange: " + isType + ">1");
						rowImpErg = ROWERG.SHFCHANGE;
						saveMe = true;
					} else if (todoTyp == 2 && isType == 3) {
						this.logME("   >>>>>> shf typ geändert von: SOLAR auf: ALLES \n");
						fixExisting.updateInt("shfflag", 1);
						fixExisting.updateString("fix", "shfchange: 3>1");
						rowImpErg = ROWERG.SHFCHANGE;
						saveMe = true;
					} else {
						this.logME("   >>>>>> shf typ: " + isType + "\n");
					}
				} else if (todoTyp != isType) {
					// change type to new type if needed
					rowImpErg = ROWERG.SHFCHANGE;
					fixExisting.updateInt("shfflag", todoTyp);
					fixExisting.updateString("fix", "shfchange: " + isType + ">" + todoTyp);
					this.logME("   >>>>>> shf typ geändert auf: " + isType + "\n");
				}
				// set mafo if wanted
				if (this.forceMDE.isSelected()) {
					fixExisting.updateString("bearbeiter", mde);
					this.logME("   >>>>>> erfasser geändert auf: " + mde + "\n");
					saveMe = true;
				}
				if (saveMe) {
					fixExisting.updateRow();
				}
			}
		} catch (Exception fixex) {
			MyLog.showExceptionErrorDialog(fixex);
			fixex.printStackTrace();
		} finally {
			Database.close(fixExisting);
		}
		return rowImpErg;
	}

	private ROWERG doOverwrite(HSSFSheet sheet, HSSFRow row, PreparedStatement firstFailedStatement, SQLException e, short bearbCol,
			String ergInh, String tel, Date aktionDate, String fname) {
		try {
			// get id of existing adress
			String[] existingInfo = Contact.getSomeInfos(tel);

			// delete doublette
			Database.delete("kunden", "WHERE telprivat=" + tel);

			// try to insert again
			firstFailedStatement.executeUpdate();

			// get id of last kunde
			int lastID = -1;
			ResultSet rs = firstFailedStatement.getGeneratedKeys();
			while (rs.next()) {
				lastID = rs.getInt(1);
			}
			// mark deleted address so i know which one it was
			Database.update("kunden_del", "fix='doublette:" + lastID + "'", "WHERE telprivat=" + tel);

			// update gespräche and aktionen
			if (existingInfo[0].length() > 0) {
				String what = "";
				if (existingInfo[1] != null) {
					what = " bereitgestellt='" + existingInfo[1] + "'" + ", bearbeitungsstatus=" + existingInfo[2] + ", marktforscher="
							+ existingInfo[3];
				} else {
					what = " bearbeitungsstatus=" + existingInfo[2] + ", marktforscher=" + existingInfo[3];
				}
				Database.update("kunden", what, "WHERE id=" + lastID);
				Database.update("aktionen", "kunde=" + lastID, "WHERE kunde=" + existingInfo[0]);
				Database.update("gespraeche", "kunde=" + lastID, "WHERE kunde=" + existingInfo[0]);
			}

			String datePlContent = OVTImportHelper.getInhalt(row, bearbCol);
			doErgebnisStuff(sheet, row, aktionDate, bearbCol, ergInh, lastID, datePlContent, fname);

			this.logME(row.getRowNum() + ">>> Überschrieben und importiert als: " + lastID + "\n");
			this.importiert.setText(Integer.toString(++this.importCount));

			return ROWERG.DOUBLEOVERWRITE;
		} catch (SQLException e1) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
			e1.printStackTrace();
		}
		return ROWERG.FAIL;
	}

	private int impType() {
		int ret = -1;
		if (this.alles.isSelected()) {
			ret = 1;
		} else if (this.onlyFenster.isSelected()) {
			ret = 2;
		} else if (this.onlySolar.isSelected()) {
			ret = 3;
		}
		return ret;
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.alles && this.alles.isSelected()) {
			this.onlyFenster.setSelected(false);
			this.onlySolar.setSelected(false);
		} else if (e.getSource() == this.onlyFenster && this.onlyFenster.isSelected()) {
			this.alles.setSelected(false);
			this.onlySolar.setSelected(false);
		} else if (e.getSource() == this.onlySolar && this.onlySolar.isSelected()) {
			this.onlyFenster.setSelected(false);
			this.alles.setSelected(false);
		}
	}

	/**
	 * show result if import in logwindow
	 * 
	 * @param s
	 */
	private void logME(String s) {
		int maxShow = 50000;
		if (this.log.getText().length() > maxShow) {
			this.log.setText(this.output.substring(this.output.length() - maxShow, this.output.length()));
		}
		this.log.append(s);
		this.output.append(s);
		this.log.setCaretPosition(this.log.getText().length());
	}

	private java.util.Date parseDate(String dateStr, String format) {
		java.util.Date date = null;
		try {
			// Some examples
			DateFormat formatter = new SimpleDateFormat(format);
			date = (java.util.Date) formatter.parse(dateStr);
		} catch (ParseException e) {
		}
		return date;
	}

	public static void main(String[] argv) {
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

	public void actionPerformed(ActionEvent arg0) {
		String com = arg0.getActionCommand();
		if (com.equals("importdir")) {
			DirectoryChooser dialog = new DirectoryChooser();
			dialog.setTitle("Import-Verzeichnis wählen");
			if (dialog.showChooseDialog(OVTImporter.frame) == DirectoryChooser.APPROVE_OPTION) {
				String d = dialog.getSelectedDirectory().getAbsolutePath();
				this.dir.setText(d);
				SettingsReader.setValue("OVTAdmin.importVerzeichnis", d);
				SettingsReader.saveProperties();
			}
		} else if (com.equals("startimport")) {
			this.startImport();
		} else if (com.equals("savelog")) {
			// write output
			Writer outputFile = null;
			try {
				File commandFile = new File("import.log");
				outputFile = new BufferedWriter(new FileWriter(commandFile));
				outputFile.write(this.output.toString());
			} catch (IOException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} finally {
				// flush and close both "output" and its underlying FileWriter
				try {
					if (outputFile != null)
						outputFile.close();
				} catch (IOException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
		} else if (com.equals("exit")) {
			System.exit(1);
		}
	}

	public void startImport() {
		// test some stuff
		if (this.alles.isSelected() || this.onlyFenster.isSelected() || this.onlySolar.isSelected()) {
			if (this.impTag.getText().length() > 0) {
				Thread t = new Thread(this);
				t.start();
			} else {
				JOptionPane.showMessageDialog(frame, "Bitte Importtag eingeben!");
			}
		} else {
			JOptionPane.showMessageDialog(frame, "Bitte Importtyp wählen!");
		}
	}

	public String getTabName() {
		return "Import";
	}
}
