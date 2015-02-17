package adminassistent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import tools.ContactBundle;
import tools.DateInterval;
import tools.DateTool;
import tools.HSSFTools;
import tools.ListItem;
import tools.MyLog;
import tools.SettingsReader;
import tools.SysTools;
import ui.IMainWindow;
import db.Contact;
import db.DBTools;
import db.Gespraech;
import db.Marktforscher;
import db.Projektleiter;

public class HonorarAbrechnung {

	private Marktforscher mafo;
	private Date payDay;
	private int month;
	private int year;
	private int abrechnungsWochenID;
	private int terminCountYear;
	private int terminCountMonth;
	private double terminHonorar;
	private int providedYear;
	private int readyYear;
	private int providedMonth;
	private int readyMonth;
	private int providedLive;
	private int readyLive;
	private int noMoneyYear;
	private int noMoneyMonth;
	private int noMoneyLive;
	private DateInterval yearInterval;
	private DateInterval monthInterval;
	private DateInterval telefonWeekInterval;
	private DateInterval gespraechWeekInterval; // this is for gespräche
	private HSSFWorkbook abrechnungWorkBook;
	private String outPutFilename;
	private Vector<Gespraech> gespraecheToBill;
	private ContactBundle readyLiveCB;
	private IMainWindow parentWindow;

	public HonorarAbrechnung(Marktforscher mafo, IMainWindow pw) {
		this.mafo = mafo;
		this.parentWindow = pw;
	}

	public int billWeekChooser() {
		int ret = -1;
		// which timeslot?
		int actualAW = DateTool.actualAbrechnungsWocheID(); // just for
		// preselecting
		// actual week
		// use week before the week where today is in
		if (actualAW > 1) {
			actualAW--;
		}
		Vector<ListItem> aws = DateTool.abrechnungsWochen();
		ListItem aaw = aws.get(actualAW - 1);
		ListItem awIDLI = (ListItem) JOptionPane.showInputDialog(
				this.parentWindow.getFrame(), "Abrechnungswoche auswählen",
				"Abrechnungswoche auswählen", JOptionPane.PLAIN_MESSAGE, null,
				aws.toArray(), aaw);

		if (awIDLI != null) {
			ret = Integer.parseInt(awIDLI.getKey0());
		}
		return ret;
	}

	/**
	 * collect data for mafo
	 * 
	 * @return success
	 */
	public boolean collectData(int weekToBill) {
		boolean ret = true;
		this.abrechnungsWochenID = weekToBill;

		// prices
		this.terminHonorar = mafo.getHonorarTermin();

		// make intervals
		this.payDay = DateTool.actualAbrechnungsTag(this.abrechnungsWochenID);
		this.month = DateTool.abrechnungsMonatAsInt(this.abrechnungsWochenID);
		this.year = DateTool.abrechnungsJahrAsInt(this.abrechnungsWochenID);
		this.telefonWeekInterval = DateTool
				.abrechnungsWoche(this.abrechnungsWochenID);
		this.gespraechWeekInterval = DateTool
				.abrechnungsWoche(this.abrechnungsWochenID + 1);

		// collect data
		this.yearInterval = DateTool.abrechnungsJahr(this.abrechnungsWochenID);
		if (this.yearInterval.getVon() != null
				&& this.yearInterval.getVon() != null) {
			this.readyYear = mafo.getFinishedContactsCount(yearInterval);
			this.noMoneyYear = mafo.getNoMoneyContactsCount(yearInterval);
			this.providedYear = mafo.getProvidedContacts(yearInterval);

			this.terminCountYear = mafo.getHonorierbareTermine(yearInterval)
					.size();
		} else {
			System.out.println("Jahresintervall konnte nicht erstellt werden");
			ret = false;
		}

		// view only actual month
		this.monthInterval = DateTool
				.abrechnungsMonat(this.abrechnungsWochenID);
		if (this.monthInterval.getVon() != null
				&& this.monthInterval.getVon() != null) {
			this.readyMonth = mafo.getFinishedContactsCount(monthInterval);
			this.noMoneyMonth = mafo.getNoMoneyContactsCount(monthInterval);
			this.providedMonth = mafo.getProvidedContacts(monthInterval);

			this.terminCountMonth = mafo.getHonorierbareTermine(monthInterval)
					.size();
		} else {
			System.out.println("Monatsintervall konnte nicht erstellt werden");
			ret = false;
		}

		// view live data
		this.readyLiveCB = mafo.getFinishedContacts(this.telefonWeekInterval);
		this.readyLive = readyLiveCB.contactCount();
		this.noMoneyLive = mafo
				.getNoMoneyContactsCount(this.telefonWeekInterval);
		this.providedLive = mafo.getProvidedContacts(this.telefonWeekInterval);

		return ret;
	}

	/**
	 * fill excel vorlage with values
	 */
	public void makeExcelAbrechnung(boolean openExcel) {
		try {
			// find right excel vorlage
			HSSFSheet sheetToFill = this.openExcelVorlage();
			if (sheetToFill != null) {
				this.clearExcelAbrechnung(sheetToFill);

				// fill sheet with values
				this.fillExcelAbrechnung(sheetToFill);

				// save as new dokument
				// open newly created dokument
				this.saveExcelAbrechnung(openExcel);

				// set data of the abrechnungswoche back
				this.readyLiveCB.setStatusToBilled(this.mafo,
						this.abrechnungsWochenID);
			}
		} catch (Exception ex) {
			MyLog.showExceptionErrorDialog(ex);
		}
	}

	/**
	 * open excel-vorlage and get the right sheet
	 * 
	 * @return the sheet to write into
	 */
	private HSSFSheet openExcelVorlage() {
		HSSFSheet sheetToFill = null;
		// find out which vorlage 4 or 5 week month?
		int weeksPerMonth = DateTool
				.abrechnungsMonatWeekCount(this.abrechnungsWochenID);
		int weekInMonth = DateTool
				.abrechnungsWeekInMonth(this.abrechnungsWochenID);
		if (weeksPerMonth == 4 || weeksPerMonth == 5) {
			String fileToOpen = "";
			// check what we need todo
			// case 1: a file for this week is not existant, create new or...
			// case 2: there is a file for this week, so open that one
			String dirToSaveTo = SettingsReader.getString(
					"OVTAdmin.HonorarOutputVerzeichnis").replace("\\", "/");
			this.outPutFilename = dirToSaveTo + "/" + this.mafo.rawName() + "_"
					+ this.year + "_" + this.month + ".xls";
			fileToOpen = this.outPutFilename;
			// check for that file
			File inputFile = new File(this.outPutFilename);
			boolean isEmptyVorlage = false;
			if (!inputFile.exists()) {
				// check if vorlagefile is present
				String vorlagenDir = SettingsReader.getString(
						"OVTAdmin.excelVorlagenVerzeichnis").replace("\\", "/");
				fileToOpen = vorlagenDir + "/" + this.mafo.getGruppe();
				if (weeksPerMonth == 4) {
					fileToOpen += SettingsReader
							.getString("OVTAdmin.excelVorlage4Wochen");
				} else {
					fileToOpen += SettingsReader
							.getString("OVTAdmin.excelVorlage5Wochen");
				}
				inputFile = new File(fileToOpen);
				isEmptyVorlage = true;
			}
			if (inputFile.exists()) {
				if (inputFile.canRead()) {
					try {
						POIFSFileSystem fs = new POIFSFileSystem(
								new FileInputStream(inputFile));
						this.abrechnungWorkBook = new HSSFWorkbook(fs);
						// fill all headers if it is opened from vorlage
						if (isEmptyVorlage) {
							this.fillHeader(this.abrechnungWorkBook,
									weeksPerMonth);
						}
						sheetToFill = this.abrechnungWorkBook
								.getSheetAt((short) weekInMonth - 1);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						MyLog.showExceptionErrorDialog(e);
					} catch (IOException e) {
						e.printStackTrace();
						MyLog.showExceptionErrorDialog(e);
					}

					// something in hssf went wrong
					if (sheetToFill == null) {
						JOptionPane.showMessageDialog(this.parentWindow
								.getFrame(),
								"Unvorhergesehener Fehler: Exceldatei konnte nicht gelesen werden: "
										+ fileToOpen, "Hinweis",
								JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
							"Unvorhergesehener Fehler: Vorlage konnte nicht gelesen werden: "
									+ fileToOpen, "Hinweis",
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
						"Unvorhergesehener Fehler: Vorlage konnte nicht gefunden werden: "
								+ fileToOpen, "Hinweis",
						JOptionPane.WARNING_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
					"Unvorhergesehener Fehler: Falsche Anzahl von Abrechnungswochen: "
							+ weeksPerMonth, "Hinweis",
					JOptionPane.WARNING_MESSAGE);
		}
		// check if vorlage exists
		// return vorlage
		return sheetToFill;
	}

	/**
	 * fill all sheets in workbook with header
	 * 
	 * @param wb
	 *            workbook to fill
	 * @param sheetCount
	 *            how many sheet are in this workbook?
	 */
	private void fillHeader(HSSFWorkbook wb, int sheetCount) {
		for (short i = 0; i < sheetCount; i++) {
			HSSFSheet sheet = this.abrechnungWorkBook.getSheetAt(i);

			// === mafoname & adress
			HSSFTools.fillCell(sheet, 2, 7, this.mafo.getAnrede());
			HSSFTools.fillCell(sheet, 2, 8, this.mafo.realName());
			HSSFTools.fillCell(sheet, 2, 9, this.mafo.getStrasse() + " "
					+ this.mafo.getHausnummer());
			HSSFTools.fillCell(sheet, 2, 10, this.mafo.getPlz() + " "
					+ this.mafo.getStadt());

			// === filename
			// this.fillCell(sheet, 2, 14, this.outPutFilename);

			// === kto & blz
			HSSFTools.fillCell(sheet, 8, 10, this.mafo.getKontonummer());
			HSSFTools.fillCell(sheet, 10, 10, this.mafo.getBlz());

			// === fax & mail
			HSSFTools.fillCell(sheet, 8, 11, this.mafo.getTelefax());
			HSSFTools.fillCell(sheet, 8, 12, this.mafo.getEmail());

			// === dates
			DateInterval week = DateTool.abrechnungsWoche(this.year,
					this.month, i + 1);

			// ====== von bis monat
			HSSFTools.fillCell(sheet, 8, 9, this.monthInterval.getVon(), false);
			HSSFTools
					.fillCell(sheet, 10, 9, this.monthInterval.getBis(), false);
			// ====== von bis woche
			HSSFTools.fillCell(sheet, 8, 14, week.getVon(), false);
			HSSFTools.fillCell(sheet, 10, 14, week.getBis(), false);
		}
	}

	private void clearExcelAbrechnung(HSSFSheet sheetToFill) {
		int tStartColumn = 0;
		int tStartRow = 26;
		int rowCount = 13;
		int weeksPerMonth = DateTool
				.abrechnungsMonatWeekCount(this.abrechnungsWochenID);
		int weekInMonth = DateTool
				.abrechnungsWeekInMonth(this.abrechnungsWochenID);
		if (weeksPerMonth == weekInMonth) {
			rowCount = 8;
		}
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < 10; j++) {
				try {
					HSSFTools.clearCell(sheetToFill, tStartColumn + j,
							tStartRow + i);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * fill the gathered values into excel sheet
	 */
	private void fillExcelAbrechnung(HSSFSheet sheet) {
		// make this sheet aktive
		sheet.setSelected(true); // TODO why is select sheet not working like
		// expected

		// ====== today
		HSSFTools.fillCell(sheet, 8, 7, this.payDay, true);

		// === counts
		// ====== jahr
		int startRow = 19;
		HSSFTools.fillCell(sheet, 3, startRow, this.providedYear);
		HSSFTools.fillCell(sheet, 4, startRow, this.readyYear);
		HSSFTools.fillCell(sheet, 5, startRow, this.noMoneyYear);
		HSSFTools.fillCell(sheet, 6, startRow, this.readyYear
				- this.noMoneyYear);
		HSSFTools.fillCell(sheet, 7, startRow, this.terminCountYear);
		HSSFTools.fillCellFormula(sheet, 8, startRow, "G20/H20");

		// ====== monat
		startRow++;
		HSSFTools.fillCell(sheet, 0, startRow, this.monthInterval.toString());
		HSSFTools.fillCell(sheet, 3, startRow, this.providedMonth);
		HSSFTools.fillCell(sheet, 4, startRow, this.readyMonth);
		HSSFTools.fillCell(sheet, 5, startRow, this.noMoneyMonth);
		HSSFTools.fillCell(sheet, 6, startRow, this.readyMonth
				- this.noMoneyMonth);
		HSSFTools.fillCell(sheet, 7, startRow, this.terminCountMonth);

		HSSFTools.fillCellFormula(sheet, 8, startRow, "G21/H21");

		// ====== woche (live)
		startRow++;
		HSSFTools.fillCell(sheet, 0, startRow, this.telefonWeekInterval
				.toString());
		HSSFTools.fillCell(sheet, 3, startRow, this.providedLive);
		HSSFTools.fillCell(sheet, 4, startRow, this.readyLive);
		HSSFTools.fillCell(sheet, 5, startRow, this.noMoneyLive);
		HSSFTools.fillCell(sheet, 6, startRow, this.readyLive
				- this.noMoneyLive);

		HSSFTools.fillCellFormula(sheet, 10, startRow, "G22*I22");

		// === information text
		// String terminInfo = "k Termin = kein Termin im Sinne der Definition,
		// " +
		// "Termin ergibt Honorar = "+this.terminHonorar+" Euro / Termin,";
		// this.fillCell(sheet, 0, 46, terminInfo);
		// String adressInfo = "Grundhonorar
		// "+(this.adressHonorar+this.telefonPauschale)+
		// " Euro = "+this.adressHonorar+" Euro pro bearbeiteter und
		// zurÃ¼ckgegebener Adresse plus "+
		// this.telefonPauschale+" Euro Kostenpauschale";
		// this.fillCell(sheet, 0, 47, adressInfo);

		// show termine stuff
		this.gespraecheToBill = this.mafo
				.getGespraeche(this.gespraechWeekInterval);

		// === termine
		int tStartColumn = 0;
		int tStartRow = 26;
		for (Iterator<Gespraech> iter = this.gespraecheToBill.iterator(); iter
				.hasNext();) {
			Gespraech g = iter.next();
			int et = g.ergebnisType();
			// make row with termin infos
			HSSFTools.fillCell(sheet, tStartColumn, tStartRow, g.getDatumMF(),
					false);
			HSSFTools.fillCell(sheet, tStartColumn + 1, tStartRow, g
					.getDatumWele(), false);
			Projektleiter wl = Projektleiter.searchProjektleiter(g
					.getProjektleiter());
			HSSFTools.fillCell(sheet, tStartColumn + 2, tStartRow, wl
					.getKurzName());
			Contact c = Contact.SearchContact(g.getKundeID());
			HSSFTools.fillCell(sheet, tStartColumn + 3, tStartRow, c
					.toStringNoID());
			HSSFTools.fillCell(sheet, tStartColumn + 5, tStartRow, c.getPlz()
					+ " " + c.getStadt());
			String ergKlarText = DBTools.nameOfTerminErgebnis(g.getErgebnis());
			if (et == 0) {
				// kein ergebnis???
				HSSFTools.fillCell(sheet, tStartColumn + 8, tStartRow,
						ergKlarText);
				// no gespräch no money...
				// this.fillCell(sheet, tStartColumn+10, tStartRow,
				// this.terminHonorar);
			} else if (et == 1) {
				// termin but no auftrag
				if (g.getErgebnis().equals("22")) {
					// special case for kulanz
					HSSFTools.fillCell(sheet, tStartColumn + 9, tStartRow,
							ergKlarText);
				} else {
					HSSFTools.fillCell(sheet, tStartColumn + 9, tStartRow,
							"i.O.");
				}
				HSSFTools.fillCell(sheet, tStartColumn + 10, tStartRow,
						(int) this.terminHonorar);
			} else if (et == 2) {
				// termin which follows a auftrag
				// HSSFTools.fillCell(sheet, tStartColumn + 9, tStartRow,
				// g.getVertragsBruttoSumme());
				// change 1.11.09 to i.O
				HSSFTools.fillCell(sheet, tStartColumn + 9, tStartRow, "i.O.");
				HSSFTools.fillCell(sheet, tStartColumn + 10, tStartRow,
						(int) this.terminHonorar);
			}
			tStartRow++;
		}
		// HSSFTools.fillCellFormula(sheet, tStartColumn + 10, 41, "K22+K40");
		// HSSFTools.fillCellFormula(sheet, tStartColumn + 10, 39,
		// "K39+K38+K37+K36+K35+K34+K33+K32+K31+K30+K29+K28+K27");
	}

	/**
	 * save the excel abrechnungsfile
	 */
	private void saveExcelAbrechnung(boolean openExcel) {
		boolean saveSuccessful = false;
		try {
			FileOutputStream fileOut = new FileOutputStream(this.outPutFilename);
			this.abrechnungWorkBook.write(fileOut);
			fileOut.close();
			saveSuccessful = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MyLog.logError(e);
		} catch (IOException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		if (!saveSuccessful) {
			JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
					"Unvorhergesehener Fehler: Exceldatei konnte nicht gespeichert werden: "
							+ this.outPutFilename + "\n"
							+ "Möglicherweise ist die Datei geöffnet.",
					"Hinweis", JOptionPane.WARNING_MESSAGE);
		} else if (openExcel) {
			Object[] options = { "Ja, Abrechnung öffnen", "Nein, fortfahren" };
			int n = JOptionPane.showOptionDialog(this.parentWindow.getFrame(),
					"Die Abrechnung wurde gespeichert: " + this.outPutFilename
							+ "\n Möchten Sie die Abrechnung öffnen?",
					"Abrechnung fertiggestellt",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				SysTools.OpenFile(this.outPutFilename);
			}
		}
	}
}
