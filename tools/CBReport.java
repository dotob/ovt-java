package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import ui.IMainWindow;
import db.Contact;
import db.DBTools;
import db.Marktforscher;

/**
 * makes a report for a contact bundle as excel sheet
 * 
 * @author basti
 * 
 */
public class CBReport extends SwingWorker<Long, Object> {

	public static final int NOAKTION = 0;
	public static final int LASTAKTION = 1;
	public static final int LASTNOTMAILAKTION = 2;

	private ContactBundle cb;
	private Marktforscher mafo;
	private int aktionInfo;
	private boolean useTemplate;
	private ProgressMonitor pm;
	private IMainWindow parentWindow;

	public static void printCBReport(ContactBundle cbi, Marktforscher mafoi, int aktionInfoI, boolean useTemplatei,
			IMainWindow pw) {
		new CBReport(cbi, mafoi, aktionInfoI, useTemplatei, pw).execute();
	}

	private CBReport(ContactBundle cbi, Marktforscher mafoi, int aktionInfoI, boolean useTemplatei, IMainWindow pw) {
		cb = cbi;
		mafo = mafoi;
		aktionInfo = aktionInfoI;
		useTemplate = useTemplatei;
		this.parentWindow = pw;
		// prepare progressmonitor
		this.pm = new ProgressMonitor(this.parentWindow.getFrame(), "Kontakte exportieren", "", 0, cbi.getContacts()
				.size());
		this.pm.setMillisToPopup(10);
		this.pm.setMillisToDecideToPopup(2);
	}

	@Override
	protected Long doInBackground() throws Exception {
		try {
			// make excel report
			String fileToOutput = "report.xls";
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet contacts = wb.createSheet("Mafo-Kontakte");
			if (useTemplate) {
				// check if vorlagefile is present
				fileToOutput = "adressReport.xls";
				// java.util.Date now = new java.util.Date();
				// SimpleDateFormat formatter;
				// formatter = new SimpleDateFormat("dd-MM-yyyy");
				// String today = formatter.format(now);
				// if (mafo!=null){
				// fileToOutput =
				// "adressReport_"+mafo.rawName()+"_"+today+".xls";
				// } else {
				// fileToOutput = "adressReport_"+today+".xls";
				// }
				String vorlagenDir = SettingsReader.getString("OVTAdmin.excelVorlagenVerzeichnis").replace("\\", "/");
				String fileToOpen = vorlagenDir + "/adressReport.xls";
				File inputFile = new File(fileToOpen);
				if (inputFile.exists()) {
					if (inputFile.canRead()) {
						try {
							POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputFile));
							wb = new HSSFWorkbook(fs);
							contacts = wb.getSheetAt(0);
							// printsetup
							HSSFPrintSetup ps = contacts.getPrintSetup();
							contacts.setAutobreaks(true);
							ps.setFitWidth((short) 1);
							short pages = (short) (cb.contactCount() / 84);
							pages++;
							ps.setFitHeight(pages);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							MyLog.showExceptionErrorDialog(e);
						} catch (IOException e) {
							e.printStackTrace();
							MyLog.showExceptionErrorDialog(e);
						}

						// something in hssf went wrong
						if (contacts == null) {
							JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
									"Unvorhergesehener Fehler: Exceldatei konnte nicht gelesen werden: " + fileToOpen,
									"Hinweis", JOptionPane.WARNING_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
								"Unvorhergesehener Fehler: Vorlage konnte nicht gelesen werden: " + fileToOpen,
								"Hinweis", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
							"Unvorhergesehener Fehler: Vorlage konnte nicht gefunden werden: " + fileToOpen, "Hinweis",
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				wb = new HSSFWorkbook();
				contacts = wb.createSheet("Mafo-Kontakte");

				// set column width by hand
				short col = 0;
				contacts.setColumnWidth(col++, (short) 1500);
				contacts.setColumnWidth(col++, (short) 6000);
				contacts.setColumnWidth(col++, (short) 4000);
				contacts.setColumnWidth(col++, (short) 4000);
				contacts.setColumnWidth(col++, (short) 4000);
				contacts.setColumnWidth(col++, (short) 4000);
				contacts.setColumnWidth(col++, (short) 6000);
				contacts.setColumnWidth(col++, (short) 4000);
				contacts.setColumnWidth(col++, (short) 9000);
				contacts.setColumnWidth(col++, (short) 4000);

				makeHeader(wb, contacts, mafo);
			}

			makeContactLines(contacts, cb, aktionInfo);

			// write excel file
			FileOutputStream fileOut = new FileOutputStream(fileToOutput);
			wb.write(fileOut);
			fileOut.close();

			// open excel
			File toOpen = new File(fileToOutput);
			SysTools.OpenFile(toOpen);
		} catch (FileNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} catch (IOException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return 1L;
	}

	/**
	 * make a line per contact
	 * 
	 * @param contacts
	 *            the sheet to print to
	 * @param cb
	 *            the contacts to print
	 */
	public void makeContactLines(HSSFSheet contacts, ContactBundle cb, int aktionInfoI) {
		// first line is header...

		short a = 0;
		short r = 2;
		for (Contact aContact : cb.getContacts()) {
			HSSFRow newRow = contacts.createRow(r);
			short c = 0;
			newRow.createCell(c++).setCellValue(r - 1);
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getNachName()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getVorName()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getStrasse()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getHausnr()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getPlz()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getStadt()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getTelefonPrivat()));
			newRow.createCell(c++).setCellValue(new HSSFRichTextString(DBTools.nameOfMDE(aContact.getMde(), true)));

			HSSFRichTextString s;
			switch (aktionInfoI) {
			case NOAKTION:
				c++;
				break;
			case LASTAKTION:
				s = new HSSFRichTextString(aContact.getNewestAktion().toNoHTMLString());
				newRow.createCell(c++).setCellValue(s);
				break;
			case LASTNOTMAILAKTION:
				s = new HSSFRichTextString(aContact.getNewestNonMailAktion(true).toNoHTMLString());
				newRow.createCell(c++).setCellValue(s);
				break;

			default:
				break;
			}

			newRow.createCell(c++).setCellValue(new HSSFRichTextString(aContact.getId()));
			r++;
			a++;

			// if cancel is pressed in progressdialog
			if (pm.isCanceled()) {
				JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Es wurden " + a + " Kontakte exportiert");
				break;
			}
			// Set new state
			pm.setProgress(a);
			// Change the note if desired
			String state = "Exportierte Kontakte: " + a;
			pm.setNote(state);
		}
	}

	/**
	 * make header in excel sheet
	 * 
	 * @param contacts
	 *            sheet to make header in
	 */
	public void makeHeader(HSSFWorkbook wb, HSSFSheet contacts, Marktforscher mafo) {
		// Create a new font and alter it.
		HSSFFont font10 = wb.createFont();
		font10.setFontHeightInPoints((short) 10);
		font10.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font10.setFontName("Arial");
		HSSFFont font14 = wb.createFont();
		font14.setFontHeightInPoints((short) 14);
		font14.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font14.setFontName("Arial");

		// Fonts are set into a style so create a new one to use.
		HSSFCellStyle style10 = wb.createCellStyle();
		style10.setFont(font10);
		style10.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		HSSFCellStyle style14 = wb.createCellStyle();
		style14.setFont(font14);

		// firstrow for mafo info
		if (mafo != null) {
			HSSFRow firstrow = contacts.createRow((short) 0);
			HSSFCell a1 = firstrow.createCell((short) 0);
			a1.setCellValue(new HSSFRichTextString("Marktforscher:"));
			a1.setCellStyle(style14);
			contacts.addMergedRegion(new Region(0, (short) 0, 0, (short) 2));
			HSSFCell a2 = firstrow.createCell((short) 3);
			a2.setCellValue(new HSSFRichTextString(mafo.toString()));
			a2.setCellStyle(style14);
		}

		// second row is header
		HSSFRow header = contacts.createRow((short) 1);
		short c = 0;
		HSSFCell c1 = header.createCell(c++);
		c1.setCellValue(new HSSFRichTextString("Nr."));
		c1.setCellStyle(style10);
		HSSFCell c2 = header.createCell(c++);
		c2.setCellValue(new HSSFRichTextString("Nachname"));
		c2.setCellStyle(style10);
		HSSFCell c3 = header.createCell(c++);
		c3.setCellValue(new HSSFRichTextString("Vorname"));
		c3.setCellStyle(style10);
		HSSFCell c4 = header.createCell(c++);
		c4.setCellValue(new HSSFRichTextString("Straﬂe"));
		c4.setCellStyle(style10);
		HSSFCell c5 = header.createCell(c++);
		c5.setCellValue(new HSSFRichTextString("Hausnr."));
		c5.setCellStyle(style10);
		HSSFCell c6 = header.createCell(c++);
		c6.setCellValue(new HSSFRichTextString("PLZ"));
		c6.setCellStyle(style10);
		HSSFCell c7 = header.createCell(c++);
		c7.setCellValue(new HSSFRichTextString("Stadt"));
		c7.setCellStyle(style10);
		HSSFCell c8 = header.createCell(c++);
		c8.setCellValue(new HSSFRichTextString("Telefon"));
		c8.setCellStyle(style10);
		HSSFCell c9 = header.createCell(c++);
		c9.setCellValue(new HSSFRichTextString("Marktdatenermittler"));
		c9.setCellStyle(style10);
		HSSFCell c11 = header.createCell(c++);
		c11.setCellValue(new HSSFRichTextString("Referenznummer"));
		c11.setCellStyle(style10);
		HSSFCell c10 = header.createCell(c++);
		c10.setCellValue(new HSSFRichTextString("Ergebnis"));
		c10.setCellStyle(style10);
	}

}
