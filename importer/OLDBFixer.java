package importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import db.Database;

public class OLDBFixer {

	public static void main(String[] args) {
		freeSomeKunden();
	}

	private static void freeSomeKunden() {
		try {
			System.out.println("start");
			// config
			String where = "marktforscher.aktiv=0 AND bearbeitungsstatus!=0";
			String comment = "reset manually";

			ResultSet rs = Database.select(
					"marktforscher.id, marktforscher.nachname, count(*)",
					"kunden, marktforscher",
					"WHERE marktforscher.id=kunden.marktforscher AND " + where
							+ " GROUP BY marktforscher.id");
			System.out.println("after select");
			while (rs.next()) {
				// int count = rs.getInt(3);
				// String mf = rs.getString(1);
				// String mfName = rs.getString(2);
				// String ids = "<";
				// ResultSet rsIds = Database.select("kunden.id",
				// "kunden, marktforscher",
				// "WHERE marktforscher.id=kunden.marktforscher AND "
				// + where + " AND kunden.marktforscher=" + mf);
				// while (rsIds.next()) {
				// ids += rsIds.getString(1);
				// ids += ",";
				// }
				// ids += ">";
				// Database.close(rsIds);
				// Database.quickInsert("bereitgestellt", "NULL, CURDATE()," +
				// mf
				// + "," + count + ",'" + comment + "','" + ids + "'");
				// System.out.println("reset: " + mfName + " reduce by: " +
				// count);
			}
			Database.close(rs);
			System.out.println("end of inserting");
			Database
					.update(
							"kunden join marktforscher on kunden.marktforscher=marktforscher.id",
							"kunden.bearbeitungsstatus=0, kunden.marktforscher=0, kunden.bereitgestellt=NULL",
							"WHERE " + where);
			System.out.println("end");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixMDE() {
		try {
			System.out.println("eins");
			ResultSet rs = Database.select("DISTINCT nachname, id",
					"marktdatenermittler", "WHERE id NOT IN (6,7,40,176)");
			System.out.println("zwei");
			HashMap<String, String> hm = new HashMap<String, String>();
			while (rs.next()) {
				hm.put(rs.getString("nachname"), rs.getString("id"));
			}
			Database.close(rs);
			hm.put("Laengin", "16");
			hm.put("Hoerske", "3");
			hm.put("Joachim", "6");
			hm.put("Patricia", "7");
			hm.put("Vogley", "140");
			hm.put("Reipich", "79");
			System.out.println("eins");
			rs = Database.select("sourcefile, id", "kunden",
					"where bearbeiter=40");
			System.out.println("zwei");
			int i = 0;
			while (rs.next()) {
				String file = rs.getString("sourcefile");
				for (Iterator<String> iter = hm.keySet().iterator(); iter
						.hasNext();) {
					String mde = iter.next();
					if (file.toLowerCase().matches(
							".*" + mde.toLowerCase() + ".*")) {
						String mdeID = hm.get(mde);
						Database.update("kunden", "bearbeiter=" + mdeID,
								"WHERE id=" + rs.getString("kunden.id"));
						System.out.println(i + " : " + mde + " : " + file);
						i++;
					}
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void fixDoubles() {
		try {
			ResultSet rs = Database.select("id", "idtemp");
			System.out.println("selectDone");
			while (rs.next()) {
				ResultSet rs1 = Database.select("*", "kunden", "WHERE id="
						+ rs.getString("id"));
				if (rs1.next()) {
					ResultSet rs2 = Database.select("*", "kunden",
							"WHERE telprivat='" + rs1.getString("telprivat")
									+ "'");
					if (rs2.next()) {
						String ha1 = rs1.getString("hasaktion");
						String ha2 = rs2.getString("hasgespraech");
						String hg1 = rs1.getString("hasaktion");
						String hg2 = rs2.getString("hasgespraech");
						String delID = "";
						if (ha1.equals("1") && ha2.equals("0")) {
							if (hg2.equals("1") && hg1.equals("0")) {
								// move gespraech to hg1
								Database.update("gespraeche", "kunde="
										+ rs1.getString("id"), "WHERE kunde="
										+ rs2.getString("id"));
							}
							Database.delete("kunden", "WHERE id="
									+ rs2.getString("id"));
							delID = rs2.getString("id");
						} else if (ha1.equals("0") && ha2.equals("1")) {
							if (hg1.equals("1") && hg2.equals("0")) {
								// move gespraech to hg2
								Database.update("gespraeche", "kunde="
										+ rs2.getString("id"), "WHERE kunde="
										+ rs1.getString("id"));
							}
							Database.delete("kunden", "WHERE id="
									+ rs1.getString("id"));
							delID = rs1.getString("id");
						} else if (ha1.equals("1") && ha2.equals("1")) {
							// seems as both are good, so use rs1
							Database.update("gespraeche", "kunde="
									+ rs1.getString("id"), "WHERE kunde="
									+ rs2.getString("id"));
							Database.update("aktionen", "kunde="
									+ rs1.getString("id"), "WHERE kunde="
									+ rs2.getString("id"));
							Database.delete("kunden", "WHERE id="
									+ rs2.getString("id"));
						} else {
							// nothing at both, delete rs2
							Database.delete("kunden", "WHERE id="
									+ rs2.getString("id"));
							delID = rs2.getString("id");
						}
						Database.close(rs1);
						Database.close(rs2);
						System.out.println("deleted: " + delID);
					}
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void fixTel() {
		try {
			ResultSet rs = Database.select("id, telprivat, telbuero, telefax",
					"kunden");
			System.out.println("selectDone");
			int i = 0;
			while (rs.next()) {
				String tp = OLDBFixer.stripNumber(rs.getString("telprivat"));
				String tb = OLDBFixer.stripNumber(rs.getString("telbuero"));
				String fax = OLDBFixer.stripNumber(rs.getString("telefax"));
				Database.update("kunden", "telprivat='" + tp + "', telbuero='"
						+ tb + "', telefax='" + fax + "'", "WHERE id="
						+ rs.getString("id"));
				System.out.println(i++);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static String stripNumber(String s) {
		String ret = "";
		if (s != null) {
			for (int i = 0; i < s.length(); i++) {
				char c = (char) s.charAt(i);
				if (Character.isDigit(c)) {
					ret += c;
				}
			}
		}
		return ret;
	}

	private static void fixStreetNumbers() {
		// before you start: truncate idtemp, set all kunden.fix to 0

		try {
			System.out.println("eins");
			ResultSet rs = Database
					.select("*", "kunden", "WHERE hausnummer=''");
			System.out.println("zwei");
			int n = 0;
			while (rs.next()) {
				String street = rs.getString("strasse");
				if (street.length() > 0
						&& Character
								.isDigit(street.charAt(street.length() - 1))) {
					String nr = OVTImportHelper.getHausNr(street);
					String realstreet = OVTImportHelper.getStrasse(street);
					System.out.println(street + ":" + nr + ":" + realstreet);
					rs.updateString("strasse", realstreet);
					rs.updateString("hausnummer", nr);
					rs.updateRow();
				}
				n++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixDiplDoc() {
		// before you start: truncate idtemp, set all kunden.fix to 0

		try {
			System.out.println("eins");
			ResultSet rs = Database.select("*", "kunden",
					"where vorname LIKE '%Dipl.%' and id>64045");
			System.out.println("zwei");
			int n = 0;
			while (rs.next()) {
				String nn = rs.getString("nachname");
				String vn = rs.getString("vorname");
				String realNN = nn;
				String realVN = vn;
				if (nn.lastIndexOf(' ') >= 0) {
					realNN = nn.substring(nn.lastIndexOf(' '), nn.length());
					realVN = vn + " " + nn.substring(0, nn.lastIndexOf(' '));
				}
				rs.updateString("nachname", realNN);
				rs.updateString("vorname", realVN);
				rs.updateRow();
				System.out.println(nn + "     :     " + realVN + "     :     "
						+ realNN);
				n++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void markDoubles() {
		// before you start: truncate idtemp, set all kunden.fix to 0

		try {
			System.out.println("eins");
			String wc = "";
			// wc += " where instr(sourcefile, stadt) ";
			ResultSet rs = Database
					.select(
							"id, nachname, vorname, stadt, count(*)",
							"kunden",
							wc
									+ " group by nachname, vorname, stadt having count(*)>1");
			System.out.println("zwei");
			String stmt = "INSERT INTO idtemp (id) VALUES (?)";
			PreparedStatement p1 = null;
			int n = 0;
			while (rs.next()) {
				p1 = Database.getPreparedStatement(stmt);
				p1.setString(1, rs.getString("id"));
				p1.executeUpdate();
				System.out.println(n + ":id:" + rs.getString("id"));
				Database.close(p1);
				n++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void delDoubleAktions() {

		try {
			System.out.println("eins");
			// ResultSet rs = Database.select("id", "aktionen", "where ergebnis
			// not in (14,13,1014,1013) group by kunde having count(*)>1");
			// ResultSet rs = Database.select("id", "aktionen", "where ergebnis
			// not in (14,13) group by kunde,ergebnis,marktforscher having
			// count(*)>1");
			ResultSet rs = Database
					.select(
							"aktionen.id",
							"aktionen,kunden,ergebnisse",
							"WHERE kunden.id=aktionen.kunde AND aktionen.ergebnis=ergebnisse.id AND ergebnisse.finishedafter=1  AND aktionen.angelegt>='2006-01-28' AND aktionen.angelegt<='2006-03-03'group by kunden.id having count(*)>1");
			System.out.println("zwei");
			int n = 0;
			while (rs.next()) {
				Database.delete("aktionen", "WHERE id=" + rs.getInt(1));
				System.out.println(n + ":" + rs.getInt(1));
				n++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void doIDTemp() {
		// marks double with fix=99
		// sets all aktionen to id from idtemp

		try {
			ResultSet rs = Database.select("*", "idtemp");
			System.out.println("hallo");
			int i = 0;
			while (rs.next()) {
				String mainID = rs.getString("id");
				ResultSet krs = Database.selectDebug(
						"nachname, vorname, stadt", "kunden", "WHERE id="
								+ mainID + " AND fix=0");
				while (krs.next()) {
					String otherids = "";
					ResultSet kkrs = Database.selectDebug("*", "kunden",
							"WHERE nachname='" + krs.getString("nachname")
									+ "' AND vorname='"
									+ krs.getString("vorname")
									+ "' AND stadt='" + krs.getString("stadt")
									+ "' AND fix=0");
					while (kkrs.next()) {
						String anID = kkrs.getString("id");
						if (!anID.equals(mainID)) {
							otherids += anID + ",";
							kkrs.updateString("fix", "99");
							kkrs.updateRow();
						}
					}
					Database.close(kkrs);
					// update aktions
					if (otherids.length() > 0) {
						otherids = otherids.substring(0, otherids.length() - 1);
						Database.update("aktionen", "kunde=" + mainID,
								"WHERE kunde IN (" + otherids + ")");
					}
				}
				Database.close(krs);
				System.out.println(i++);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixStreetNamesStep1() {
		// collect data from xls files...

		// get files from actual directory
		File[] toImp = new File("D:/eclipse/OVTDATA/firstimport")
				.listFiles(new FilenameFilter() {
					public boolean accept(File d, String s) {
						if (d.isDirectory()) {
							if (s.endsWith("xls") && s.indexOf("#") < 0) {
								return true;
							}
						}
						return false;
					}
				});

		POIFSFileSystem fs;
		int MAXIMP = Math.max(15, toImp.length);
		for (int i = 0; i < MAXIMP; i++) {
			try {
				File file = toImp[i];
				String fname = file.getName().replace("-", "_");
				System.out.println();
				System.out
						.println(">---------------------------------------------------------- "
								+ i + " von " + MAXIMP);
				System.out.println(">" + fname);
				fs = new POIFSFileSystem(new FileInputStream(file));
				HSSFWorkbook wb = new HSSFWorkbook(fs);
				HSSFSheet sheet = wb.getSheet("Marktdaten");
				if (sheet == null) {
					int nr = 1;
					if (wb.getNumberOfSheets() > nr) {
						sheet = wb.getSheetAt(nr);
						System.out.println("   >got sheet by number, named: "
								+ wb.getSheetName(nr));
					}
				}
				if (sheet != null) {
					// ooooooooooooo
					Coords colStr = findColumn(sheet, "stra�e");
					Coords colPLZ = findColumn(sheet, "plz");
					if (colPLZ == null) {
						colPLZ = findColumn(sheet, "ort");
					}
					if (colStr != null && colPLZ != null) {
						for (int r = colStr.y; r < 40; r++) {
							HSSFRow row = sheet.getRow(r);
							if (row != null) {
								HSSFCell cStr = row.getCell(colStr.x);
								String rawStr = "";
								String xlsStr = "";
								if (cStr != null
										&& cStr.getCellType() == HSSFCell.CELL_TYPE_STRING) {
									rawStr = cStr.getRichStringCellValue()
											.getString();
									xlsStr = OVTImportHelper.getStrasse(rawStr);
								}

								HSSFCell cStadt = row.getCell(colPLZ.x);
								String rawStadt = "";
								String xlsStadt = "";
								if (cStadt != null
										&& cStadt.getCellType() == HSSFCell.CELL_TYPE_STRING) {
									rawStadt = cStadt.getRichStringCellValue()
											.getString();
									xlsStadt = OVTImportHelper.getOrt(rawStadt);
								}

								if (rawStr.length() > 0
										&& rawStadt.length() > 0) {
									try {
										// check double
										boolean isUnique = true;
										ResultSet ru = Database.select("*",
												"streetfix", " WHERE stadt=\""
														+ xlsStadt
														+ "\" AND street=\""
														+ xlsStr + "\"");
										if (ru.next()) {
											isUnique = false;
										}
										Database.close(ru);

										if (isUnique) {
											PreparedStatement p = Database
													.getPreparedStatement("INSERT INTO streetfix "
															+ "(streetraw, street, stadtraw, stadt, file) "
															+ "VALUES (?,?,?,?,?)");
											p.setString(1, rawStr);
											p.setString(2, xlsStr);
											p.setString(3, rawStadt);
											p.setString(4, xlsStadt);
											p.setString(5, fname);
											p.executeUpdate();
											Database.close(p);
											System.out.println();
											System.out.println("added street: "
													+ xlsStr + " in "
													+ xlsStadt);
										} else {
											// System.out.println(">>double
											// street: "+xlsStr+" in
											// "+xlsStadt);
											System.out.print("d");
										}
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
						}
					} else {
						System.out.println("######### ALARM:" + fname);
					}
				}

				// rename file
				String t = file.getAbsolutePath();
				String path = t.substring(0, t.length()
						- file.getName().length());
				File renameed = new File(path + "#" + file.getName());
				file.renameTo(renameed);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void markDoubles2() {
		try {
			System.out.println("eins");
			ResultSet rs = Database.select(
					"id,swap,fix,nachname,vorname,stadt", "kunden",
					"where fix>=1 order by nachname");
			System.out.println("zwei");
			String last = "";
			String lastID = "";
			while (rs.next()) {
				try {
					String aktID = rs.getString(1);
					String akt = rs.getString(3) + rs.getString(4)
							+ rs.getString(5);
					if (akt.equals(last)) {
						rs.updateString("swap", "killme");
						rs.updateString("fix", "-" + lastID);
						rs.updateRow();
						System.out.println(rs.getString("id"));
					}
					last = akt;
					lastID = aktID;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixMafoStep1() {
		try {
			ResultSet rs = Database.select("id, vorname, nachname, kurzname",
					"marktforscher", "ORDER BY id");
			int i = 0;
			while (rs.next()) {
				String id = rs.getString("id");
				String vn = rs.getString("vorname");
				if (vn != null)
					vn = vn.trim();
				if (vn == null)
					vn = "";
				String nn = rs.getString("nachname");
				if (nn != null)
					nn = nn.trim();
				if (nn == null)
					nn = "";
				String kn = rs.getString("kurzname");
				if (kn == null)
					kn = "";
				if (nn.length() == 2 && nn.toUpperCase().equals(nn)) {
					kn = nn;
				}
				Database.update("marktforscher", "vorname='" + vn
						+ "', nachname='" + nn + "', kurzname='" + kn + "'",
						"WHERE id=" + id);
				System.out.println(i + " : " + vn + " : " + nn + " : " + kn);
				i++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixAuftraege() {
		try {
			ResultSet rs = Database.select("*", "auftraege");
			while (rs.next()) {
				PreparedStatement p1 = Database
						.getPreparedStatement("INSERT INTO gespraeche "
								+ "(angelegt, kunde, ergebnis)"
								+ "VALUES (?,?,?)");
				p1.setString(1, rs.getString("datum"));
				p1.setString(2, rs.getString("kunde"));
				p1.setString(3, "19");
				p1.executeUpdate();
				Database.close(p1);
				System.out.println(rs.getString("id"));
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixAktionenAgain() {
		try {
			System.out.println("eins");
			ResultSet rs = Database.select("*", "kunden",
					"where bearbeitungsstatus=3 AND hasaktion=0");
			System.out.println("zwei");
			int n = 0;
			Date nowDate = new Date(new java.util.Date().getTime());
			Time nowTime = new Time(new java.util.Date().getTime());

			while (rs.next()) {
				String id = rs.getString("id");
				// set hasaktion
				rs.updateString("hasaktion", "1");
				rs.updateRow();
				// make aktion
				PreparedStatement p1 = Database
						.getPreparedStatement("INSERT INTO aktionen "
								+ "(kunde, angelegt, angelegtZeit, aktionstyp, marktforscher, ergebnis, eingangsdatum) "
								+ "VALUES (?,?,?,?,?,?,?)");
				p1.setString(1, id);
				p1.setDate(2, nowDate); // is the date when telefonist did the
				// call
				p1.setTime(3, nowTime); // is the date when telefonist did the
				// call
				p1.setInt(4, 1);
				p1.setString(5, rs.getString("marktforscher"));
				p1.setString(6, "4");
				p1.setDate(7, nowDate); // date when aktion is inserted to db
				p1.executeUpdate();
				Database.close(p1);
				n++;
				System.out.println(n);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixStreetNumbersStep1() {
		POIFSFileSystem fs;
		try {
			System.out.println("eins");
			// ResultSet rs = Database.select("*", "xls");
			ResultSet rsCount = Database.select("count(*)", "kunden",
					"WHERE hausnummer='' AND fix=0");
			int anz = 0;
			while (rsCount.next()) {
				anz = rsCount.getInt(1);
			}
			ResultSet rs = Database.select("*", "kunden",
					"WHERE hausnummer='' AND fix=0 ORDER BY sourcefile");
			System.out.println("zwei");
			int n = 0;
			HSSFSheet sheet = null;
			String aktxls = "";
			String lastxls = "";
			while (rs.next()) {
				String dir = "D:/eclipse/OVTDATA/_DONE/firstimport/";
				String xlsname = rs.getString("sourcefile");
				File file = new File(dir + xlsname);
				if (!file.exists()) {
					dir = "D:/eclipse/OVTDATA/_DONE/import_jan2006_1/";
					file = new File(dir + xlsname);
					if (!file.exists()) {
						dir = "D:/eclipse/OVTDATA/_DONE/import_jan2006_2/";
						file = new File(dir + xlsname);
						if (!file.exists()) {
							dir = "D:/eclipse/OVTDATA/_DONE/import_mar2006/";
							file = new File(dir + xlsname);
						}
					}
				}
				if (file.exists() && file.canRead()) {
					aktxls = file.getName();
					try {
						if (!aktxls.equals(lastxls)) {
							System.out.println("new xls:" + file);
							fs = new POIFSFileSystem(new FileInputStream(file));
							HSSFWorkbook wb = new HSSFWorkbook(fs);
							sheet = wb.getSheet("Marktdaten");
							if (sheet == null) {
								int nr = 1;
								if (wb.getNumberOfSheets() > nr) {
									sheet = wb.getSheetAt(nr);
									System.out
											.println("   >got sheet by number, named: "
													+ wb.getSheetName(nr));
								}
							}
						}
						if (sheet != null) {
							Coords posKunden = findColumn(sheet, rs
									.getString("nachname"));
							Coords colStr = findColumn(sheet, "stra�e");
							Coords colPLZ = findColumn(sheet, "plz");
							// Coords colNam = findColumn(sheet, "name");
							// Coords colFenst = findColumn(sheet, "FE");
							// if (colFenst==null){
							// colFenst = findColumn(sheet, "vorn");
							// }
							// Coords colHT = findColumn(sheet, "HT");
							// if (colHT==null){
							// colHT = findColumn(sheet, "Farbe");
							// }
							// System.out.println(file+">>"+posKunden+"||"+colStr+"-"+colPLZ+"-"+colNam+"-"+colFenst+"-"+colHT);
							// if (colPLZ==null){
							// colPLZ = findColumn(sheet, "ort");
							// }
							if (posKunden != null) {
								HSSFRow row = sheet.getRow(posKunden.y);
								if (row != null && colStr != null) {
									System.out.println(n
											+ "/"
											+ anz
											+ "  :"
											+ rs.getString("nachname")
											+ ": name  :"
											+ row.getCell(posKunden.x)
													.getRichStringCellValue()
													.getString() + " : "
											+ colStr);
									String newHausNR = "";
									if (row.getCell(colStr.x) != null
											&& row.getCell(colStr.x)
													.getRichStringCellValue()
													.getString() != null) {
										newHausNR = OVTImportHelper
												.getHausNr(row
														.getCell(colStr.x)
														.getRichStringCellValue()
														.getString());
									}
									if (row.getCell(colStr.x) != null
											&& rs.getString("hausnummer")
													.trim().length() == 0) {
										rs
												.updateString("hausnummer",
														newHausNR);
										System.out
												.println("nr    :"
														+ newHausNR
														+ " : "
														+ row
																.getCell(
																		colStr.x)
																.getRichStringCellValue()
																.getString());
									}

									// String fenst = "";
									// if (colFenst!=null &&
									// row.getCell(colFenst.x)!=null &&
									// row.getCell(colFenst.x).getCellType()==HSSFCell.CELL_TYPE_STRING){
									// fenst =
									// row.getCell(colFenst.x).getStringCellValue();
									// }
									// if
									// (rs.getString("fensterzahl").equals("0")){
									// rs.updateString("fensterzahl", fenst);
									// }
									// String ht =
									// OVTImportHelper.interpreteColor(row.getCell(colHT.x).getStringCellValue());

									// rs.updateString("haustuerfarbe", ht);
									rs.updateString("fix", "1");
									rs.updateRow();
									// System.out.println("fenst :"+fenst);
									// System.out.println("ht
									// :"+OVTImportHelper.interpreteColor(row.getCell(colHT.x).getStringCellValue()));
									n++;
								}
							}

							lastxls = aktxls;

							// System.out.println("file found: "+file);
							// String t = file.getAbsolutePath();
							// String path = t.substring(0,
							// t.length()-file.getName().length());
							// File renameed = new
							// File(path+"#"+file.getName());
							// file.renameTo(renameed);
						}
					} catch (FileNotFoundException e) {
						System.out.println("file not found: " + file);
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println(n + ">>>>>>>> nicht gefunden: " + file);
				}
				// System.out.println(n);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Coords findColumn(HSSFSheet sheet, String s) {
		Coords ret = null;
		HSSFRow row;
		HSSFCell c;
		for (short i = 0; i < 20 && ret == null; i++) {
			row = sheet.getRow(i);
			if (row != null) {
				for (short j = 0; j < 20 && ret == null; j++) {
					c = row.getCell(j);
					if (c != null
							&& c.getCellType() == HSSFCell.CELL_TYPE_STRING
							&& c.getRichStringCellValue().getString()
									.toLowerCase().indexOf(s.toLowerCase()) == 0) {
						ret = new Coords(j, i);
					}
				}
			}
		}
		return ret;
	}

	private static HSSFRow findRow(HSSFSheet sheet, String s) {
		HSSFRow ret = null;
		HSSFRow row = null;
		HSSFCell c;
		for (short i = 0; i < 20 && ret == null; i++) {
			row = sheet.getRow(i);
			for (short j = 0; j < 20 && ret == null; j++) {
				c = row.getCell(j);
				if (c != null
						&& c.getCellType() == HSSFCell.CELL_TYPE_STRING
						&& c.getRichStringCellValue().getString().toLowerCase()
								.indexOf(s.toLowerCase()) == 0) {
					ret = row;
				}
			}
		}
		return ret;
	}

	private static void fixNames() {
		try {
			ResultSet rs = Database.select("id, vorname, nachname, fix",
					"kunden", "WHERE fix=0 ORDER BY id");
			int i = 0;
			while (rs.next()) {
				String namganz = rs.getString("vorname") + " "
						+ rs.getString("nachname");
				String id = rs.getString("id");
				// System.out.println(namganz);
				int pos = namganz.lastIndexOf(" ");
				String vn = OVTImportHelper.strHead(namganz, pos);
				String nn = OVTImportHelper.strTail(namganz, namganz.length()
						- pos);
				// System.out.println(nn+" : "+vn);
				Database.update("kunden", "vorname='" + vn + "', nachname='"
						+ nn + "', fix=1", "WHERE id=" + id);
				System.out.println(i + " : " + vn + " : " + nn);
				i++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixStreetsNames() {
		try {
			ResultSet rs = Database.select(
					"id, vorname, nachname, strasse, stadt, sourcefile",
					"kunden", "ORDER BY id LIMIT 1000");
			while (rs.next()) {
				String ws = "WHERE file='" + rs.getString("sourcefile") + "'";
				ws += " AND street LIKE '" + rs.getString("strasse") + "%'";
				ResultSet krs = Database.select("street", "streetfix", ws);
				if (krs.next()) {
					rs.updateString("strasse", krs.getString("street"));
					rs.updateRow();
					System.out.println(krs.getString("street") + " :: "
							+ rs.getString("strasse"));
				}
				Database.close(krs);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void testoooo() {
		try {
			ResultSet rs = Database.select("*", "kunden");
			while (rs.next()) {
				ResultSet krs = Database.select("*", "kunden");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixAktionen() {
		// get files from actual directory
		File[] toImp = new File("D:/eclipse/OVTDATA/checkDATAumlaut")
				.listFiles(new FilenameFilter() {
					public boolean accept(File d, String s) {
						if (d.isDirectory()) {
							if (s.endsWith("xls")) {
								return true;
							}
						}
						return false;
					}
				});

		// go trough all files
		int MAXIMP = Math.max(15, toImp.length);
		int delCounter = 0;
		for (int i = 0; i < MAXIMP; i++) {
			File file = toImp[i];
			String fname = file.getName().replace("-", "_");
			System.out
					.println(">---------------------------------------------------------- "
							+ i + " von " + MAXIMP);
			System.out.println(">" + fname);
			try {
				ResultSet rs = Database.select("*", "kunden",
						"WHERE sourcefile LIKE '%" + fname + "%'");
				while (rs.next()) {
					Database.delete("aktionen", "WHERE kunde="
							+ rs.getString("id"));
					System.out
							.println(delCounter + " :: " + rs.getString("id"));
					delCounter++;
				}
				Database.close(rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void fixMafoStep2() {
		try {
			ResultSet rs = Database
					.select(
							"aktionen.id, aktionen.marktforscher, marktforscher.id, marktforscher.anrede",
							"aktionen, marktforscher",
							"WHERE aktionen.marktforscher=marktforscher.id ORDER BY aktionen.id");
			int i = 0;
			while (rs.next()) {
				String id = rs.getString("aktionen.id");
				String tomafo = rs.getString("marktforscher.anrede");
				if (Character.isDigit(tomafo.charAt(0))) {
					if (tomafo.equals("0")) {
						Database.update("aktionen", "marktforscher=''",
								"WHERE id=" + id);
					} else {
						Database.update("aktionen", "marktforscher=" + tomafo,
								"WHERE id=" + id);
					}
				}
				// if (tomafo.length()>0){
				// Database.update("aktionen", "marktforscher="+tomafo, "WHERE
				// id="+id);
				// System.out.println(i+" : "+id+" : "+mafo+" : "+tomafo);
				// }

				i++;
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixMultiFinishedAktionen() {
		try {
			ResultSet rs = Database
					.selectDebug(
							"*",
							"aktionen, ergebnisse",
							"where aktionen.ergebnis=ergebnisse.id AND ergebnisse.finishedafter>1 order by aktionen.kunde");
			int c = 0;
			int i = 0;
			int lastID = -1;
			while (rs.next()) {
				int aktID = rs.getInt("aktionen.kunde");
				if (aktID == lastID) {
					if (i > 0) {
						Database.quickInsert("idtemp", rs
								.getString("aktionen.id")
								+ ",'del'");
					} else {
						Database.quickInsert("idtemp", rs
								.getString("aktionen.id")
								+ ",'set'");
					}
					i++;
				} else {
					i = 0;
					Database.quickInsert("idtemp", rs.getString("aktionen.id")
							+ ",'set'");
				}
				lastID = aktID;
				System.out.println(c++);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixTeam2() {
		try {
			ResultSet rs = Database.selectDebug("xlsname", "fixteams", "");
			int i = 0;
			int j = 0;
			while (rs.next()) {
				String ff = rs.getString("xlsname");
				String f = ff;
				if (ff.indexOf("Ã¶") >= 0) {
					f = ff.substring(ff.indexOf("Ã¶") + 2, ff.length());
				} else {
					f = ff.substring(ff.indexOf("Ã¼") + 2, ff.length());
				}
				f = "%" + f + "%";
				ResultSet krs = Database.selectDebug("id", "kunden",
						"WHERE sourcefile LIKE '" + f + "'");
				while (krs.next()) {
					Database.quickInsert("idtemp", krs.getString("id")
							+ ",'aktdel'");
					j++;
				}
				Database.close(krs);
				System.out.println(j);
				j = 0;
			}
			Database.close(rs);
			System.out.println(i++);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void removeDoubles() {
		try {

			ResultSet rs = Database.select("id, fix", "kunden",
					"WHERE swap='killme'");
			System.out.println("eins");
			int i = 0;
			while (rs.next()) {
				// has first one aktion
				boolean firstHasAktion = false;
				ResultSet aktrs = Database.select("id", "aktionen",
						"WHERE kunde=" + rs.getString("id"));
				if (aktrs.next()) {
					firstHasAktion = true;
				} else {
					rs.updateString("swap", "dokillme");
					rs.updateRow();
				}
				Database.close(aktrs);

				String otherid = rs.getString("fix");
				otherid = otherid.substring(1, otherid.length() - 1);
				ResultSet otheraktrs = Database.select("id", "aktionen",
						"WHERE kunde=" + rs.getString("fix"));
				if (otheraktrs.next() && firstHasAktion) {
					otheraktrs.updateString("kunde", rs.getString("id"));
					otheraktrs.updateRow();

				} else if (firstHasAktion) {
					rs.updateString("swap", "dokillme");
					rs.updateRow();
				}
				Database.close(otheraktrs);

				i++;
				System.out.println(i + " :checked: " + rs.getString("id"));
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fixMafosLastStep() {
		try {
			ResultSet rs = Database.select("id, nachname",
					"marktdatenermittler", "ORDER BY id");
			while (rs.next()) {
				String id = rs.getString("id");
				String nn = rs.getString("nachname");
				ResultSet krs = Database.selectDebug("*", "kunden",
						"WHERE sourcefile LIKE '%" + nn + ".xls'");
				while (krs.next()) {
					krs.updateString("bearbeiter", id);
					krs.updateString("fix", "1");
					System.out.println(krs.getString("id") + " set "
							+ krs.getString("sourcefile") + " to " + nn + ":"
							+ id);
				}
				Database.close(krs);
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
