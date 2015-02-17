package statistics;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import tools.DateInterval;
import tools.DateTool;
import tools.HSSFTools;
import tools.MyLog;
import tools.SettingsReader;
import tools.SysTools;
import tools.Table2Excel;
import ui.IMainWindow;
import db.DBTools;
import db.Database;
import db.Marktforscher;
import db.Projektleiter;

public class MoreMFCollector extends SwingWorker<Long, Object> {

	public enum whatToDo {
		MFERFOLG, MFERFOLGSOLAR, MFMOTIVATION, WLERFOLG
	};

	private enum statistic {
		solar, fht
	};

	private ProgressMonitor pm;
	private static String sonstigeGruende = "5,6,7,8,9,10,24,26,27,28";
	private static String gOhneA = "11,12,13,14,15,16,17,18,20,21,25,23,30,31,32,33,34,35,36";

	private String outPutDir;
	private whatToDo whichStatistic;
	private IMainWindow parentWindow;

	public MoreMFCollector(IMainWindow mw) {
		this.parentWindow = mw;
		// prepare progressmonitor
		this.pm = new ProgressMonitor(this.parentWindow.getFrame(), "Daten sammeln", "", 0, 100);
		this.pm.setMillisToPopup(500);
		this.pm.setMillisToDecideToPopup(500);
	}

	private void collectMFErfolgsData(statistic whatToDo) {
		Object[] answ = { "2006", "2007", "2008" };
		int y = JOptionPane.showOptionDialog(this.parentWindow.getFrame(),
				"F¸r welches Jahr sollen die Daten gesammelt werden", "Jahresauswahl",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, answ, answ[2]);
		int year = 2006;
		if (y == 1) {
			year = 2007;
		} else if (y == 2) {
			year = 2008;
		}

		this.parentWindow.setWaitCursor();
		// get dates (build a year interval)
		Calendar calFrom = new GregorianCalendar();
		calFrom.set(year, 0, 1);
		Calendar calTo = new GregorianCalendar();
		calTo.set(year + 1, 0, 1);
		DateInterval range = new DateInterval();
		range.setVon(new java.sql.Date(calFrom.getTime().getTime()));
		java.util.Date toDate = new java.util.Date();
		if (toDate.after(calTo.getTime())) {
			toDate = calTo.getTime();
		}
		range.setBis(new java.sql.Date(toDate.getTime()));

		// open vorlage
		// check if vorlagefile is present
		String vorlagenDir = SettingsReader.getString("OVTAdmin.excelVorlagenVerzeichnis").replace("\\", "/");
		String fileToOpen = vorlagenDir + "/mferfolgsrechnung.xls";
		File inputFile = new File(fileToOpen);
		HSSFWorkbook excelFile = null;
		HSSFSheet sheetToFill = null;
		if (inputFile.exists()) {
			if (inputFile.canRead()) {
				try {
					POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputFile));
					excelFile = new HSSFWorkbook(fs);
					sheetToFill = excelFile.getSheetAt(0);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					MyLog.showExceptionErrorDialog(e);
				} catch (IOException e) {
					e.printStackTrace();
					MyLog.showExceptionErrorDialog(e);
				}
			}
		}
		// something in hssf went wrong
		if (sheetToFill == null) {
			JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
					"Unvorhergesehener Fehler: Exceldatei konnte nicht gelesen werden: " + fileToOpen, "Hinweis",
					JOptionPane.WARNING_MESSAGE);
		} else {
			// retrieve data
			try {
				int progress = 0;
				HSSFTools.fillCell(sheetToFill, 29, 0, range.getBis(), true);

				String whichMF = "";
				if (whatToDo == statistic.fht) {
					whichMF = " AND solar=0 ";
				} else if (whatToDo == statistic.solar) {
					whichMF = " AND solar=1 ";
				}

				// count mf
				ResultSet rsMF = Database.select("id, nachname, eintrittsdatum", "marktforscher", "WHERE aktiv=1 "
						+ whichMF + " ORDER BY nachname");
				rsMF.last();
				int mfCount = rsMF.getRow();
				this.pm.setMaximum(mfCount + 3);

				// get all abbrecher
				int row = 6;
				this.pm.setNote("Abbrecher sammeln");
				boolean foundAbbrecher = false;
				String abbrecher = " IN (";
				ResultSet rs = Database.select("id", "marktforscher", "WHERE aktiv=0 " + whichMF);
				while (rs.next()) {
					abbrecher += rs.getString("id") + ",";
					foundAbbrecher = true;
				}
				abbrecher = abbrecher.substring(0, abbrecher.length() - 1) + ")";
				if (!foundAbbrecher) {
					abbrecher = "";
				}
				this.oneLine(sheetToFill, abbrecher, "Abbrecher", null, range, null, row, year);
				this.pm.setProgress(progress++);
				row++;
				row++;
				Database.close(rs);

				// get all mafos
				this.pm.setNote("Mafos sammeln");
				rsMF.beforeFirst();
				while (rsMF.next()) {
					String mfID = rsMF.getString("id");
					Marktforscher mf = Marktforscher.SearchMarktforscher(mfID);
					this.oneLine(sheetToFill, "=" + mfID, rsMF.getString("nachname"), mf, range, rsMF
							.getDate("eintrittsdatum"), row, year);
					this.pm.setProgress(progress++);
					row++;
				}
				Database.close(rsMF);

				// this is the control of the above data, two small region at
				// the bottom
				// get some other data: wl data from teams
				// get some other data: wl data from teams
				// get some other data: wl data from teams
				int columnGohneA = 14;
				int columnGmitA = 16;
				String whichProduct = "";
				if (whatToDo == statistic.fht) {
					whichProduct = " AND p.shf=2 ";
				} else if (whatToDo == statistic.solar) {
					whichProduct = " AND p.shf=3 ";
				}
				for (int i = 1; i <= 3; i++) {
					ResultSet rsTeamsGohneA = Database.select("count(*)", "gespraeche g, werbeleiter w, produkte p",
							"WHERE g.werbeleiter=w.id AND g.produkt=p.id AND g.ergebnis IN (" + gOhneA
									+ ") AND w.team=" + i + " " + whichProduct + " AND g.angelegt>='" + range.getVon()
									+ "' AND g.angelegt<='" + range.getBis() + "'");
					while (rsTeamsGohneA.next()) {
						HSSFTools.fillCell(sheetToFill, columnGohneA, 110 + i, rsTeamsGohneA.getInt(1));
					}
					Database.close(rsTeamsGohneA);

					ResultSet rsTeamsGmitA = Database.select("count(*)",
							"gespraeche g, werbeleiter w, terminergebnisse t, produkte p",
							"WHERE g.werbeleiter=w.id AND g.ergebnis=t.id AND g.produkt=p.id AND t.erfolg=2 AND w.team="
									+ i + " " + whichProduct + " AND g.angelegt>='" + range.getVon()
									+ "' AND g.angelegt<='" + range.getBis() + "'");
					while (rsTeamsGmitA.next()) {
						HSSFTools.fillCell(sheetToFill, columnGmitA, 110 + i, rsTeamsGmitA.getInt(1));
					}
					Database.close(rsTeamsGmitA);
				}
				this.pm.setProgress(progress++);

				// sonstige
				ResultSet rsTeamsSonstigeGohneA = Database.select("count(*)",
						"gespraeche g, werbeleiter w, produkte p",
						"WHERE g.werbeleiter=w.id AND g.produkt=p.id AND g.ergebnis IN (" + gOhneA
								+ ") AND w.team NOT IN (1,2,3) " + whichProduct + " AND g.angelegt>='" + range.getVon()
								+ "' AND g.angelegt<='" + range.getBis() + "'");
				while (rsTeamsSonstigeGohneA.next()) {
					HSSFTools.fillCell(sheetToFill, columnGohneA, 110 + 4, rsTeamsSonstigeGohneA.getInt(1));
				}
				Database.close(rsTeamsSonstigeGohneA);

				ResultSet rsTeamsSonstigeGmitA = Database.select("count(*)",
						"gespraeche g, werbeleiter w, terminergebnisse t, produkte p",
						"WHERE g.werbeleiter=w.id AND g.ergebnis=t.id AND g.produkt=p.id AND t.erfolg=2 AND w.team NOT IN (1,2,3) "
								+ whichProduct + " AND g.angelegt>='" + range.getVon() + "' AND g.angelegt<='"
								+ range.getBis() + "'");
				while (rsTeamsSonstigeGmitA.next()) {
					HSSFTools.fillCell(sheetToFill, columnGmitA, 110 + 4, rsTeamsSonstigeGmitA.getInt(1));
				}
				Database.close(rsTeamsSonstigeGmitA);

				this.pm.setProgress(progress++);

				// write year-header
				// write year-header
				// write year-header
				String fnameType = "";
				if (whatToDo == statistic.fht) {
					HSSFTools.fillCell(sheetToFill, 0, 0, "MF - Erfolgsrechnung FE+HT " + year);
					fnameType = "_fht";
				} else if (whatToDo == statistic.solar) {
					HSSFTools.fillCell(sheetToFill, 0, 0, "MF - Erfolgsrechnung Solar " + year);
					fnameType = "_solar";
				}

				// write file
				// write file
				// write file
				boolean success = false;
				String fileToSave = this.outPutDir + "\\mferfolgsrechnung" + year + fnameType + ".xls";
				try {
					FileOutputStream fileOut = new FileOutputStream(fileToSave);
					excelFile.write(fileOut);
					fileOut.close();
					success = true;
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "Erst Exceldatei " + fileToSave + " schlieﬂen");
					e.printStackTrace();
				} catch (IOException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
				if (success) {
					this.pm.close();
					Object[] options = { "Ja, MF-Erfolgsrechnung ˆffnen", "Nein, fortfahren" };
					int n = JOptionPane.showOptionDialog(this.parentWindow.getFrame(),
							"Die MF-Erfolgsrechnung wurde gespeichert: " + fileToSave
									+ "\n Mˆchten Sie die MF-Erfolgsrechnung ˆffnen?",
							"MF-Erfolgsrechnung fertiggestellt", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					if (n == 0) {
						SysTools.OpenFile(fileToSave);
					}
				}
			} catch (SQLException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			this.parentWindow.setDefaultCursor();
			this.pm.close();
		}
	}

	private void oneLine(HSSFSheet sheetToFill, String mf, String mfnn, Marktforscher mfMF, DateInterval range,
			java.sql.Date entry, int row, int year) {
		int col = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		HSSFTools.fillCell(sheetToFill, col, row, mfnn);
		col++;
		if (entry != null) {
			HSSFTools.fillCell(sheetToFill, col, row, sdf.format(entry));
		}
		col++;
		// col++;

		this.pm.setNote("Daten f¸r " + mfnn + " sammeln");
		try {
			// get bereitgestellt data
			ResultSet rs;
			// = Database.select("SUM(count)", "bereitgestellt",
			// "WHERE marktforscher" + mf + " AND wann>='"
			// + range.getVon() + "' AND wann<='" + range.getBis() + "'");
			// while (rs.next()) {
			// HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			// }
			// Database.close(rs);
			// col++;
			// col++;

			// get honorierte adressen
			rs = Database.select("count(*)", "aktionen a, ergebnisse e", "WHERE marktforscher" + mf
					+ " AND a.ergebnis=e.id AND e.nomoney=0 AND angelegt>='" + range.getVon() + "' AND angelegt<='"
					+ range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;

			// get zur¸ckgegeben
			Database.close(rs);
			rs = Database
					.select("angelegt", "aktionen", "WHERE marktforscher" + mf + " ORDER BY angelegt DESC LIMIT 1");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, sdf.format(rs.getDate(1)));
			}
			col++;
			col++;

			// // get termin data
			// nicht anwesend
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche", "WHERE marktforscher" + mf + " AND ergebnis=2"
					+ " AND angelegt>='" + range.getVon() + "' AND angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;
			col++;

			// nicht geˆffnet, abgewiesen
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche", "WHERE marktforscher" + mf + " AND ergebnis IN (3,4) "
					+ " AND angelegt>='" + range.getVon() + "' AND angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;
			col++;

			// sonstige gr¸nde
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche", "WHERE marktforscher" + mf + " AND ergebnis IN ("
					+ sonstigeGruende + ") " + " AND angelegt>='" + range.getVon() + "' AND angelegt<='"
					+ range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;

			// kulanz
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche", "WHERE marktforscher" + mf + " AND ergebnis=22 "
					+ " AND angelegt>='" + range.getVon() + "' AND angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;

			// abgesagt
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche", "WHERE marktforscher" + mf + " AND ergebnis=1 "
					+ " AND angelegt>='" + range.getVon() + "' AND angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;

			// all but kulanz
			// Database.close(rs);
			// rs = Database.select("count(*)", "gespraeche", "WHERE
			// marktforscher"+mf+
			// " AND ergebnis NOT IN (22) " +
			// " AND angelegt>='"+range.getVon()+"' AND
			// angelegt<='"+range.getBis()+"'");
			// while (rs.next()){
			// HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			// }
			col++;
			col++;

			// GohneA
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche g", "WHERE marktforscher" + mf + " AND ergebnis IN (" + gOhneA
					+ ") " + " AND g.angelegt>='" + range.getVon() + "' AND g.angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col++;
			col++;

			// GmitA
			Database.close(rs);
			rs = Database.select("count(*)", "gespraeche g, terminergebnisse t", "WHERE marktforscher" + mf
					+ " AND g.ergebnis=t.id AND t.erfolg=2 " + " AND angelegt>='" + range.getVon()
					+ "' AND angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				HSSFTools.fillCell(sheetToFill, col, row, rs.getInt(1));
			}
			col += 7;

			// brutto AE
			Database.close(rs);
			rs = Database.select("SUM(summe)", "gespraeche", "WHERE marktforscher" + mf + " AND ergebnis=19 "
					+ " AND angelegt>='" + range.getVon() + "' AND angelegt<='" + range.getBis() + "'");
			while (rs.next()) {
				int aebrutto = rs.getInt(1);
				int mwst = 16;
				if (year >= 2007) {
					mwst = 19;
				}
				int aenetto = aebrutto / (100 + mwst) * 100;
				HSSFTools.fillCell(sheetToFill, col, row, aenetto);
			}

			if (false) {
				if (mfMF != null) {
					// try to get some idea about honorar...
					col++;
					Database.close(rs);
					double honorarEuro = 0;
					rs = Database.select("count(*)", "aktionen a, ergebnisse e", "WHERE marktforscher" + mf
							+ " AND a.ergebnis=e.id AND e.nomoney=0" + " AND angelegt>='" + range.getVon()
							+ "' AND angelegt<='" + range.getBis() + "'");
					while (rs.next()) {
						int anzHonorableAktions = rs.getInt(1);
						honorarEuro += anzHonorableAktions * (mfMF.getHonorarAdresse() + mfMF.getHonorarPauschale());
					}
					Database.close(rs);

					rs = Database.select("count(*)", "gespraeche a, terminergebnisse t", "WHERE marktforscher" + mf
							+ " AND a.ergebnis=t.id AND t.erfolg>=1" + " AND angelegt>='" + range.getVon()
							+ "' AND angelegt<='" + range.getBis() + "'");
					while (rs.next()) {
						int anzHonorableTermine = rs.getInt(1);
						honorarEuro += anzHonorableTermine * mfMF.getHonorarTermin();
					}
					// fill honorar cell
					HSSFTools.fillCell(sheetToFill, col, row, honorarEuro);
				} else {
					// try to get some idea about honorar...special
					// abbrecher!!!!
					col++;
					Database.close(rs);
					double honorarEuro = 0;
					rs = Database.select("count(*)", "aktionen a, ergebnisse e, marktforscher m",
							"WHERE a.ergebnis=e.id AND a.marktforscher=m.id AND e.nomoney=0 AND m.aktiv=0"
									+ " AND a.angelegt>='" + range.getVon() + "' AND a.angelegt<='" + range.getBis()
									+ "'");
					while (rs.next()) {
						int anzHonorableAktions = rs.getInt(1);
						honorarEuro += anzHonorableAktions
								* (Marktforscher.getDefaultHonorarAdresse() + Marktforscher
										.getDefaultHonorarPauschale());
					}
					Database.close(rs);

					rs = Database.select("count(*)", "gespraeche g, terminergebnisse t, marktforscher m",
							"WHERE g.ergebnis=t.id AND g.marktforscher=m.id AND m.aktiv=0 AND t.erfolg>=1"
									+ " AND g.angelegt>='" + range.getVon() + "' AND g.angelegt<='" + range.getBis()
									+ "'");
					while (rs.next()) {
						int anzHonorableTermine = rs.getInt(1);
						honorarEuro += anzHonorableTermine * Marktforscher.getDefaultHonorarTermin();
					}
					// fill honorar cell
					HSSFTools.fillCell(sheetToFill, col, row, honorarEuro);
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void collectWLErfolgsData() {
		this.parentWindow.setWaitCursor();
		// get dates
		Calendar calNow = Calendar.getInstance();
		int year = calNow.get(Calendar.YEAR);
		Calendar cal = new GregorianCalendar();
		cal.set(year, 0, 1);
		DateInterval range = new DateInterval();
		range.setVon(new java.sql.Date(cal.getTime().getTime()));
		range.setBis(new java.sql.Date(new java.util.Date().getTime()));

		// create new File
		HSSFWorkbook excelFile = new HSSFWorkbook();

		// something in hssf went wrong
		// retrieve data
		try {
			ResultSet rsTeams = Database.select("*", "wlteams", "ORDER BY id");
			while (rsTeams.next()) {

				HSSFSheet sheetToFill = excelFile.createSheet(rsTeams.getString("name"));
				// fill team header
				sheetToFill.createRow(0);
				HSSFTools.fillCell(sheetToFill, 0, 0, "WL - Erfolgsrechnung");
				HSSFTools.fillCell(sheetToFill, 2, 0, year);
				HSSFTools.fillCell(sheetToFill, 11, 0, rsTeams.getString("name"));
				HSSFTools.fillCell(sheetToFill, 21, 0, "Stand");
				HSSFTools.fillCell(sheetToFill, 22, 0, new SimpleDateFormat("dd.MM.yy").format(range.getBis()));

				String teamID = rsTeams.getString("id");
				int wlCount = 0;
				ResultSet rsWL = Database.select("id", "werbeleiter", "WHERE team=" + teamID);
				while (rsWL.next()) {
					Projektleiter wl = Projektleiter.searchProjektleiter(rsWL.getString("id"));
					this.fillWLYear(excelFile, sheetToFill, wl, wlCount);
					wlCount++;
				}
				Database.close(rsWL);
				this.pm.setNote("Fertig");
			}
			Database.close(rsTeams);
			boolean success = false;
			String fileToSave = this.outPutDir + "\\wlerfolgsrechnung" + year + ".xls";
			try {
				FileOutputStream fileOut = new FileOutputStream(fileToSave);
				excelFile.write(fileOut);
				fileOut.close();
				success = true;
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(null, "Erst Exceldatei " + fileToSave + " schlieﬂen");
				e.printStackTrace();
			} catch (IOException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
			if (success) {
				Object[] options = { "Ja, WL-Erfolgsrechnung ˆffnen", "Nein, fortfahren" };
				int n = JOptionPane.showOptionDialog(this.parentWindow.getFrame(),
						"Die WL-Erfolgsrechnung wurde gespeichert: " + fileToSave
								+ "\n Mˆchten Sie die WL-Erfolgsrechnung ˆffnen?", "WL-Erfolgsrechnung fertiggestellt",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (n == 0) {
					SysTools.OpenFile(fileToSave);
				}
			}
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		} catch (Exception e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		this.parentWindow.setDefaultCursor();
		this.pm.close();
	}

	private void fillWLYear(HSSFWorkbook wb, HSSFSheet sheetToFill, Projektleiter wl, int wlCount) {

		// calc positions
		short row = 2;
		short col = 0;
		if (wlCount % 2 != 0) {
			col += 12;
		}
		if (wlCount % 2 == 0) {
			row += (wlCount / 2) * 42;
		}

		// make header
		short startCol = col;
		sheetToFill.createRow(row);
		HSSFCell cell = HSSFTools.fillCell(sheetToFill, col, row, wl.getKurzName());
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
		sheetToFill.setColumnWidth(col++, (short) 3000);

		cell = HSSFTools.fillCell(sheetToFill, col, row, "Termine");
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
		sheetToFill.setColumnWidth(col++, (short) 2500);

		cell = HSSFTools.fillCell(sheetToFill, col, row, "GohneA");
		sheetToFill.addMergedRegion(new Region(row, col, row, (short) (col + 1)));
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt, HSSFTools.Alignment.c);
		sheetToFill.setColumnWidth(col++, (short) 1500);
		cell = HSSFTools.fillCell(sheetToFill, col, row, "");
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
		sheetToFill.setColumnWidth(col++, (short) 1500);

		cell = HSSFTools.fillCell(sheetToFill, col, row, "GmitA");
		sheetToFill.addMergedRegion(new Region(row, col, row, (short) (col + 1)));
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt, HSSFTools.Alignment.c);
		sheetToFill.setColumnWidth(col++, (short) 1500);
		cell = HSSFTools.fillCell(sheetToFill, col, row, "");
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
		sheetToFill.setColumnWidth(col++, (short) 1500);

		cell = HSSFTools.fillCell(sheetToFill, col, row, "Quote");
		sheetToFill.addMergedRegion(new Region(row, col, row, (short) (col + 2)));
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb, HSSFTools.Alignment.c);
		sheetToFill.setColumnWidth(col++, (short) 1500);
		cell = HSSFTools.fillCell(sheetToFill, col, row, "");
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb);
		sheetToFill.setColumnWidth(col++, (short) 900);
		cell = HSSFTools.fillCell(sheetToFill, col, row, "");
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb);
		sheetToFill.setColumnWidth(col++, (short) 900);

		cell = HSSFTools.fillCell(sheetToFill, col, row, "ME+MF-Kosten/Auftrag");
		sheetToFill.addMergedRegion(new Region(row, col, row, (short) (col + 1)));
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
		sheetToFill.setColumnWidth(col++, (short) 2800);
		cell = HSSFTools.fillCell(sheetToFill, col, row, "");
		HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
		sheetToFill.setColumnWidth(col++, (short) 2800);
		row++;
		col = startCol;

		String[] months = { "Jan", "Feb", "Mrz", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez",
				"kumuliert" };
		for (int i = 0; i < months.length; i++) {
			row = this.fillWLMonth(wb, sheetToFill, wl, i, months[i], row, col);
			row++;
		}
	}

	private short fillWLMonth(HSSFWorkbook wb, HSSFSheet sheetToFill, Projektleiter wl, int month, String monthName,
			short row, short col) {
		this.parentWindow.setStatusText("Sammle Daten f¸r " + wl + " von " + monthName);
		// calc positions
		short startCol = col;

		Calendar calNow = Calendar.getInstance();
		int year = calNow.get(Calendar.YEAR);
		DateInterval range = null;
		if (month == 12) {
			range = DateTool.actualAbrechnungsJahr();
		} else {
			range = DateTool.abrechnungsMonat(year, month + 1);
		}

		try {
			sheetToFill.createRow(row);
			HSSFCell cell = HSSFTools.fillCell(sheetToFill, col, row, wl.getKurzName());
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
			cell = HSSFTools.fillCell(sheetToFill, col, row, "<=16:00");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);
			// goa
			int goa_early = 0;
			ResultSet rs = Database.select("count(*)", "gespraeche g, terminergebnisse t", "WHERE g.werbeleiter="
					+ wl.getId()
					+ " AND g.ergebnis=t.id AND t.erfolg=1 AND g.terminzeit IN ('9:30','11:30','14:00','16:00')"
					+ " AND g.datum_vd>='" + range.getVon() + "' AND g.datum_vd<='" + range.getBis() + "'");
			while (rs.next()) {
				goa_early = rs.getInt(1);
			}
			Database.close(rs);
			cell = HSSFTools.fillCell(sheetToFill, col, row, goa_early);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt, HSSFTools.Alignment.c);
			// gma
			int gma_early = 0;
			rs = Database.select("count(*)", "gespraeche g, terminergebnisse t", "WHERE g.werbeleiter=" + wl.getId()
					+ " AND g.ergebnis=t.id AND t.erfolg=2 AND g.terminzeit IN ('9:30','11:30','14:00','16:00')"
					+ " AND g.datum_vd>='" + range.getVon() + "' AND g.datum_vd<='" + range.getBis() + "'");
			while (rs.next()) {
				gma_early = rs.getInt(1);
			}
			Database.close(rs);
			cell = HSSFTools.fillCell(sheetToFill, col, row, gma_early);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);

			double qe = gma_early == 0 ? 0 : goa_early / gma_early;
			cell = HSSFTools.fillCell(sheetToFill, col, row, qe);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.n, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "zu");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.n, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, 1);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.n, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "kosten");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt, HSSFTools.Alignment.r);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrt);

			col = startCol;
			row++;
			sheetToFill.createRow(row);
			cell = HSSFTools.fillCell(sheetToFill, col, row, range.toString());
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lr);

			cell = HSSFTools.fillCell(sheetToFill, col, row, ">=18:00");
			col += 2;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lr);
			// goa
			int goa_late = 0;
			rs = Database.select("count(*)", "gespraeche g, terminergebnisse t", "WHERE g.werbeleiter=" + wl.getId()
					+ " AND g.ergebnis=t.id AND t.erfolg=1 AND g.terminzeit IN ('18:00')" + " AND g.datum_vd>='"
					+ range.getVon() + "' AND g.datum_vd<='" + range.getBis() + "'");
			while (rs.next()) {
				goa_late = rs.getInt(1);
			}
			Database.close(rs);
			cell = HSSFTools.fillCell(sheetToFill, col, row, goa_late);
			col += 2;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lr, HSSFTools.Alignment.c);

			// gma
			int gma_late = 0;
			rs = Database.select("count(*)", "gespraeche g, terminergebnisse t", "WHERE g.werbeleiter=" + wl.getId()
					+ " AND g.ergebnis=t.id AND t.erfolg=2 AND g.terminzeit IN ('18:00')" + " AND g.datum_vd>='"
					+ range.getVon() + "' AND g.datum_vd<='" + range.getBis() + "'");
			while (rs.next()) {
				gma_late = rs.getInt(1);
			}
			Database.close(rs);
			cell = HSSFTools.fillCell(sheetToFill, col, row, gma_late);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lr, HSSFTools.Alignment.c);

			double ql = gma_late == 0 ? 0 : goa_late / gma_late;
			cell = HSSFTools.fillCell(sheetToFill, col, row, ql);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.n, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "zu");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.n, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, 1);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.n, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lr, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "kosten");
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lr);

			col = startCol;
			row++;
			sheetToFill.createRow(row);
			cell = HSSFTools.fillCell(sheetToFill, col, row, monthName);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "alle");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, goa_early + goa_late);
			sheetToFill.addMergedRegion(new Region(row, col, row, (short) (col + 1)));
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, gma_early + gma_late);
			sheetToFill.addMergedRegion(new Region(row, col, row, (short) (col + 1)));
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb, HSSFTools.Alignment.c);

			double tmp = gma_early + gma_late;
			double qa = tmp == 0 ? 0 : (goa_early + goa_late) / (gma_early + gma_late);
			cell = HSSFTools.fillCell(sheetToFill, col, row, qa);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.tb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "zu");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.tb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, 1);
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.tb, HSSFTools.Alignment.c);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "");
			col++;
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb);

			cell = HSSFTools.fillCell(sheetToFill, col, row, "kosten");
			HSSFTools.setCellStyle(wb, cell, HSSFTools.BorderStyle.lrtb);

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		return row;
	}

	private void collectMotivationData() {
		this.parentWindow.setWaitCursor();
		// get dates
		DateInterval range = MainStatisticPanel.getVonBis();
		DateInterval rangeG = MainStatisticPanel.getVonBis();
		rangeG.shiftWeeks(1);

		// retrieve data
		try {
			this.parentWindow.setStatusText("Besten Mafo finden");
			// get count for telefon and for termine
			int sumTmp = 0;
			int count = 0;
			int sumTmpG = 0;
			int countG = 0;

			// count of aktions grouped by ergebnis
			HashMap<Integer, Integer> erg = new HashMap<Integer, Integer>();
			ResultSet rsAll = Database.select("count(a.marktforscher) AS anz, a.marktforscher",
					"aktionen a, ergebnisse e", "WHERE " + "a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='"
							+ range.getVon() + "' AND a.angelegt<='" + range.getBis()
							+ "' GROUP BY a.marktforscher ORDER BY anz DESC");
			while (rsAll.next()) {
				erg.put(rsAll.getInt("a.marktforscher"), rsAll.getInt("anz"));
				sumTmp += rsAll.getInt("anz");
				count++;
			}
			Database.close(rsAll);

			// count of honorable termine grouped by ergebnis
			HashMap<Integer, Integer> ergGHT = new HashMap<Integer, Integer>();
			ResultSet rsAllGHT = Database.select("count(a.marktforscher) AS anz, a.marktforscher",
					"gespraeche a, terminergebnisse e", "WHERE " + "a.ergebnis=e.id AND e.erfolg>0 AND a.angelegt>='"
							+ rangeG.getVon() + "' AND a.angelegt<='" + rangeG.getBis()
							+ "' GROUP BY a.marktforscher ORDER BY anz DESC");
			while (rsAllGHT.next()) {
				ergGHT.put(rsAllGHT.getInt("a.marktforscher"), rsAllGHT.getInt("anz"));
				sumTmpG += rsAllGHT.getInt("anz");
				countG++;
			}
			Database.close(rsAllGHT);

			// count of all termine grouped by ergebnis
			HashMap<Integer, Integer> ergG = new HashMap<Integer, Integer>();
			ResultSet rsAllG = Database.select("count(a.marktforscher) AS anz, a.marktforscher",
					"gespraeche a, terminergebnisse e", "WHERE " + "a.ergebnis=e.id AND a.angelegt>='"
							+ rangeG.getVon() + "' AND a.angelegt<='" + rangeG.getBis()
							+ "' GROUP BY a.marktforscher ORDER BY anz DESC");
			while (rsAllG.next()) {
				ergG.put(rsAllG.getInt("a.marktforscher"), rsAllG.getInt("anz"));
				sumTmpG += rsAllG.getInt("anz");
				countG++;
			}
			Database.close(rsAllG);

			// calc best and mean
			double sumHT = 0;
			HashMap<Integer, Double> telPerHonTerm = new HashMap<Integer, Double>();
			for (Iterator<Integer> iter = erg.keySet().iterator(); iter.hasNext();) {
				int mf = iter.next();
				int telCount = erg.get(mf);
				if (ergG.containsKey(mf)) {
					int termCount = ergG.get(mf);
					double w = (double) telCount / termCount;
					sumHT += w;
					telPerHonTerm.put(mf, w);
				}
			}

			// find mean-mf that mf with the lowest difference to mean-ht
			double meanHT = sumHT / telPerHonTerm.size();
			int bestMF = -1;
			int meanMF = -1;
			double diff = 1000;
			double tmpLowHT = 1000;
			for (Iterator<Integer> iter = telPerHonTerm.keySet().iterator(); iter.hasNext();) {
				int mf = iter.next(); // actual mf
				double v = telPerHonTerm.get(mf); // actual tel/honterm value
				double ad = Math.abs(v - meanHT); // difference of actual
				// value to mean
				if (ad < diff) {
					meanMF = mf;
					diff = ad;
				}
				if (v < tmpLowHT) {
					bestMF = mf;
				}
			}

			// collect max values
			this.parentWindow.setStatusText("Werte von bestem Mafo sammeln");
			int countAllBestMF = 0;
			HashMap<Integer, Integer> ergBestMF = new HashMap<Integer, Integer>();
			ResultSet rsMax = Database.select("count(a.ergebnis) AS anz, a.ergebnis", "aktionen a, ergebnisse e",
					"WHERE " + "a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
							+ "' AND a.angelegt<='" + range.getBis() + "' AND a.marktforscher=" + bestMF
							+ " GROUP BY a.ergebnis ORDER BY e.id");
			while (rsMax.next()) {
				countAllBestMF += rsMax.getInt("anz");
				ergBestMF.put(rsMax.getInt("a.ergebnis"), rsMax.getInt("anz"));
			}
			Database.close(rsMax);

			int countAllBestMFG = 0;
			HashMap<Integer, Integer> ergBestMFG = new HashMap<Integer, Integer>();
			ResultSet rsMaxG = Database.select("count(a.ergebnis) AS anz, a.ergebnis",
					"gespraeche a, terminergebnisse e", "WHERE " + "a.ergebnis=e.id AND a.angelegt>='"
							+ rangeG.getVon() + "' AND a.angelegt<='" + rangeG.getBis() + "' AND a.marktforscher="
							+ bestMF + " GROUP BY a.ergebnis ORDER BY e.id");
			while (rsMaxG.next()) {
				countAllBestMFG += rsMaxG.getInt("anz");
				ergBestMFG.put(rsMaxG.getInt("a.ergebnis"), rsMaxG.getInt("anz"));
			}
			Database.close(rsMaxG);

			// collect mean values
			this.parentWindow.setStatusText("Durchschnitts-Mafo Werte sammeln");
			int countAllMeanMF = 0;
			HashMap<Integer, Integer> ergMeanMF = new HashMap<Integer, Integer>();
			ResultSet rsMean = Database.select("count(a.ergebnis) AS anz, a.ergebnis", "aktionen a, ergebnisse e",
					"WHERE " + "a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
							+ "' AND a.angelegt<='" + range.getBis() + "' AND a.marktforscher=" + meanMF
							+ " GROUP BY a.ergebnis ORDER BY e.id");
			while (rsMean.next()) {
				countAllMeanMF += rsMean.getInt("anz");
				ergMeanMF.put(rsMean.getInt("a.ergebnis"), rsMean.getInt("anz"));
			}
			Database.close(rsMean);

			int countAllMeanG = 0;
			HashMap<Integer, Integer> ergMeanMFG = new HashMap<Integer, Integer>();
			ResultSet rsMeanG = Database.select("count(a.ergebnis) AS anz, a.ergebnis",
					"gespraeche a, terminergebnisse e", "WHERE " + "a.ergebnis=e.id AND a.angelegt>='"
							+ rangeG.getVon() + "' AND a.angelegt<='" + rangeG.getBis() + "' AND a.marktforscher="
							+ meanMF + " GROUP BY a.ergebnis ORDER BY e.id");
			while (rsMeanG.next()) {
				countAllMeanG += rsMeanG.getInt("anz");
				ergMeanMFG.put(rsMeanG.getInt("a.ergebnis"), rsMeanG.getInt("anz"));
			}
			Database.close(rsMeanG);

			// ================================= get mf telefondata
			// ================================= get mf telefondata
			// ================================= get mf telefondata
			// ================================= get mf telefondata
			// ================================= get mf telefondata
			Vector<String> cn = new Vector<String>();
			cn.add(new String("Ergebnis"));
			cn.add(new String("Anzahl Sie"));
			cn.add(new String("Prozent Sie"));
			cn.add(new String("Prozent Beste(r)"));
			cn.add(new String("Prozent Durchschnitt"));

			int i = 1;
			Table2Excel xls = null;
			HashMap<Integer, Table2Excel> xlsTables = new HashMap<Integer, Table2Excel>();
			// loop over all mf
			for (Integer e : erg.keySet()) {
				i++;
				int mf = e;

				// get list of resulttypes
				Vector<String> resTypes = new Vector<String>();
				ResultSet resultTypes = Database.select("id", "ergebnisse", "ORDER BY id");
				while (resultTypes.next()) {
					resTypes.add(resultTypes.getString("id"));
				}
				Database.close(resultTypes);

				int countAll = 0;
				this.parentWindow.setStatusText("(" + i + "/" + erg.size() + ") "
						+ DBTools.nameOfMafo(Integer.toString(mf), true) + " Telefonergebnisse Werte sammeln");
				StatisticErgebnisTable table = new StatisticErgebnisTable(cn);

				// get count of all results for % calc, its just one number!!!
				ResultSet rsAlles = Database.select("count(a.ergebnis) AS anz", "aktionen a, ergebnisse e", "WHERE "
						+ "a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
						+ "' AND a.angelegt<='" + range.getBis() + "' AND a.marktforscher=" + mf);
				while (rsAlles.next()) {
					countAll = rsAlles.getInt("anz");
				}
				Database.close(rsAlles);

				Vector<String> firstRow = new Vector<String>();
				firstRow.add("Gesamt");
				firstRow.add(Integer.toString(erg.get(mf)));
				firstRow.add("100.0 %");
				if (countAllBestMF > 0 && countAll > 0) {
					BigDecimal tmp2 = new BigDecimal(100 / ((double) countAllBestMF / countAll));
					if (tmp2.doubleValue() > 0) {
						tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
						firstRow.add(tmp2.toString() + " % (" + countAllBestMF + ")");
					} else {
						firstRow.add("0.0 % (0)");
					}
				} else {
					firstRow.add("0.0 % (0)");
				}
				if (countAllMeanMF > 0 && countAll > 0) {
					BigDecimal tmp2 = new BigDecimal(100 / ((double) countAllMeanMF / countAll));
					if (tmp2.doubleValue() > 0) {
						tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
						firstRow.add(tmp2.toString() + " % (" + countAllMeanMF + ")");
					} else {
						firstRow.add("0.0 % (0)");
					}
				} else {
					firstRow.add("0.0 % (0)");
				}
				table.addRow(firstRow);
				// count of result of one type
				BigDecimal tmp2;
				for (String resID : resTypes) {
					ResultSet rs = Database.select("count(*)", "aktionen a, ergebnisse e", "WHERE "
							+ "a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + range.getVon()
							+ "' AND a.angelegt<='" + range.getBis() + "' AND a.marktforscher=" + mf
							+ " AND a.ergebnis=" + resID + " ORDER BY e.id");
					while (rs.next()) {
						Vector<String> row = new Vector<String>();
						Marktforscher mafo = Marktforscher.SearchMarktforscher(Integer.toString(mf));
						row.add(DBTools.nameOfErgebnis(resID, mafo.isSolar()));
						int countErg = rs.getInt(1);
						row.add(Integer.toString(countErg));
						// percentage of best mf count
						if (countErg > 0) {
							tmp2 = new BigDecimal(100 / ((double) countAll / countErg));
							tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
							row.add(tmp2.toString() + " %");
							if (ergBestMF.get(Integer.parseInt(resID)) != null) {
								int countErgMax = ergBestMF.get(Integer.parseInt(resID));
								if (countAllBestMF > 0 && countErgMax > 0) {
									tmp2 = new BigDecimal(100 / ((double) countAllBestMF / countErgMax));
									if (tmp2.doubleValue() > 0) {
										tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
										row.add(tmp2.toString() + " % (" + countErgMax + ")");
									} else {
										row.add("0.0 % (0)");
									}
								} else {
									row.add("0.0 % (0)");
								}
							} else {
								row.add("0.0 % (0)");
							}
						} else {
							row.add("0.0 % (0)");
							row.add("0.0 % (0)");
						}

						// percentage of mean mf count
						if (countErg > 0) {
							if (ergMeanMF.get(Integer.parseInt(resID)) != null) {
								int countErgMean = ergMeanMF.get(Integer.parseInt(resID));
								if (countAllMeanMF > 0 && countErgMean > 0) {
									tmp2 = new BigDecimal(100 / ((double) countAllMeanMF / countErgMean));
									if (tmp2.doubleValue() > 0) {
										tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
										row.add(tmp2.toString() + " % (" + countErgMean + ")");
									} else {
										row.add("0.0 % (0)");
									}
								} else {
									row.add("0.0 % (0)");
								}
							} else {
								row.add("0.0 % (0)");
							}
						} else {
							row.add("0.0 % (0)");
						}
						table.addRow(row);
					}
					Database.close(rs);
				}
				// save table to excel
				this.parentWindow
						.setStatusText("(" + i + "/" + erg.size() + ") "
								+ DBTools.nameOfMafo(Integer.toString(mf), true)
								+ " Telefonergebnisse in Exceldatei speichern");
				short[] fields = new short[5];
				fields[0] = 9000;
				fields[1] = 5000;
				fields[2] = 5000;
				fields[3] = 5000;
				fields[4] = 5000;
				String fname = this.outPutDir + "/" + DBTools.nameOfMafo(Integer.toString(mf), false) + "_statistik_"
						+ new SimpleDateFormat("yyyy-MM").format(range.getVon()) + ".xls";
				xls = new Table2Excel(table, fname, "MF-Ergebnisse", fields);
				xlsTables.put(mf, xls);
			}

			// ====================== gespr√§che
			// ====================== gespr√§che
			// ====================== gespr√§che
			// ====================== gespr√§che

			// shift daterange one week up
			range.shiftWeeks(1);
			i = 1;
			for (Integer e : ergG.keySet()) {
				i++;
				int mf = e;
				int countAll = 0;

				// get list of resulttypes
				Vector<String> resTypes = new Vector<String>();
				ResultSet resultTypes = Database.select("id", "terminergebnisse", "ORDER BY id");
				while (resultTypes.next()) {
					resTypes.add(resultTypes.getString("id"));
				}
				Database.close(resultTypes);

				this.parentWindow.setStatusText("(" + i + "/" + erg.size() + ") "
						+ DBTools.nameOfMafo(Integer.toString(mf), true) + " Gespr‰chsergebnisse Werte sammeln");
				StatisticErgebnisTable table = new StatisticErgebnisTable(cn);

				// get count of all results for % calc, its just one number!!!
				ResultSet rsAlles = Database.select("count(a.ergebnis) AS anz", "gespraeche a, terminergebnisse e",
						"WHERE " + "a.ergebnis=e.id AND a.angelegt>='" + rangeG.getVon() + "' AND a.angelegt<='"
								+ rangeG.getBis() + "' AND a.marktforscher=" + mf);
				while (rsAlles.next()) {
					countAll = rsAlles.getInt("anz");
				}
				Database.close(rsAlles);

				Vector<String> firstRow = new Vector<String>();
				firstRow.add("Gesamt");
				firstRow.add(Integer.toString(ergG.get(mf)));
				firstRow.add("100.0 %");
				if (countAllBestMFG > 0 && countAll > 0) {
					BigDecimal tmp2 = new BigDecimal(100 / ((double) countAllBestMFG / countAll));
					if (tmp2.doubleValue() > 0) {
						tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
						firstRow.add(tmp2.toString() + " % (" + countAllBestMFG + ")");
					} else {
						firstRow.add("0.0 % (0)");
					}
				} else {
					firstRow.add("0.0 % (0)");
				}
				if (countAllMeanG > 0 && countAll > 0) {
					BigDecimal tmp2 = new BigDecimal(100 / ((double) countAllMeanG / countAll));
					if (tmp2.doubleValue() > 0) {
						tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
						firstRow.add(tmp2.toString() + " % (" + countAllMeanG + ")");
					} else {
						firstRow.add("0.0 % (0)");
					}
				} else {
					firstRow.add("0.0 % (0)");
				}
				table.addRow(firstRow);
				// count of result of one type
				BigDecimal tmp2;
				for (String resID : resTypes) {
					ResultSet rs = Database.select("count(*)", "gespraeche a, ergebnisse e", "WHERE "
							+ "a.ergebnis=e.id AND e.finishedafter=1 AND a.angelegt>='" + rangeG.getVon()
							+ "' AND a.angelegt<='" + rangeG.getBis() + "' AND a.marktforscher=" + mf
							+ " AND a.ergebnis=" + resID + " ORDER BY e.gruppe, e.id");
					while (rs.next()) {
						Vector<String> row = new Vector<String>();
						row.add(DBTools.nameOfTerminErgebnis(resID));
						int countErg = rs.getInt(1);
						row.add(Integer.toString(countErg));
						if (countErg > 0) {
							tmp2 = new BigDecimal(100 / ((double) countAll / countErg));
							tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
							row.add(tmp2.toString() + " %");
							if (ergBestMF.get(Integer.parseInt(resID)) != null) {
								int countErgMax = ergBestMF.get(Integer.parseInt(resID));
								if (countAllBestMF > 0 && countErgMax > 0) {
									tmp2 = new BigDecimal(100 / ((double) countAllBestMF / countErgMax));
									if (tmp2.doubleValue() > 0) {
										tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
										row.add(tmp2.toString() + " % (" + countErgMax + ")");
									} else {
										row.add("0.0 % (0)");
									}
								} else {
									row.add("0.0 % (0)");
								}
							} else {
								row.add("0.0 % (0)");
							}
						} else {
							row.add("0.0 % (0)");
							row.add("0.0 % (0)");
						}
						if (countErg > 0) {
							if (ergMeanMF.get(Integer.parseInt(resID)) != null) {
								int countErgMean = ergMeanMF.get(Integer.parseInt(resID));
								if (countAllMeanMF > 0 && countErgMean > 0) {
									tmp2 = new BigDecimal(100 / ((double) countAllMeanMF / countErgMean));
									if (tmp2.doubleValue() > 0) {
										tmp2 = tmp2.setScale(1, BigDecimal.ROUND_HALF_UP);
										row.add(tmp2.toString() + " % (" + countErgMean + ")");
									} else {
										row.add("0.0 % (0)");
									}
								} else {
									row.add("0.0 % (0)");
								}
							} else {
								row.add("0.0 % (0)");
							}
						} else {
							row.add("0.0 % (0)");
						}
						table.addRow(row);
					}
					Database.close(rs);
				}
				// save table to excel
				this.parentWindow.setStatusText("(" + i + "/" + ergG.size() + ") "
						+ DBTools.nameOfMafo(Integer.toString(mf), true) + " Gespr‰che in Exceldatei speichern");
				short[] fields = new short[5];
				fields[0] = 9000;
				fields[1] = 5000;
				fields[2] = 5000;
				fields[3] = 5000;
				fields[4] = 5000;
				xls = xlsTables.get(mf);
				if (xls != null) {
					xls.addTableAsSheet(table, "Gespr‰chs-Ergebnisse", fields);
				} else {
					String fname = this.outPutDir + "/" + DBTools.nameOfMafo(Integer.toString(mf), false)
							+ "_statistik_" + new SimpleDateFormat("yyyy-MM").format(range.getVon()) + ".xls";
					xls = new Table2Excel(table, fname, "MF-Ergebnisse", fields);
				}

				// save xls file
				// save xls file
				// save xls file
				xls.saveXLS();
			}
			this.parentWindow.setStatusText("Alles gespeichert");
			Toolkit.getDefaultToolkit().beep();

		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		this.parentWindow.setDefaultCursor();
		this.pm.close();
	}

	@Override
	protected Long doInBackground() throws Exception {
		switch (whichStatistic) {
		case MFERFOLG:
			collectMFErfolgsData(statistic.fht);
			break;
		case MFERFOLGSOLAR:
			collectMFErfolgsData(statistic.solar);
			break;
		case MFMOTIVATION:
			collectMotivationData();
			break;
		case WLERFOLG:
			collectWLErfolgsData();
			break;

		default:
			break;
		}
		return 1L;
	}

	public void setOutputDir(String outputdir) {
		this.outPutDir = outputdir;
	}

	public void setWhichStatistic(whatToDo whichStatistic) {
		this.whichStatistic = whichStatistic;
	}

}
