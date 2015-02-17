package importer;

import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import db.Database;

public class DBFixer {

	public static void fixStreetNames() {
		java.util.Date start = new java.util.Date();
		try {
			int count = 0;
			HSSFSheet sheet = null;
			String xlsFilename = "";
			String useXLSDir = "d:\\tmp\\ovt\\";
			ResultSet ru = Database.select("id, nachname, strasse, sourcefile, fix", "kunden",
					"WHERE strasse NOT LIKE '%str%' AND strasse NOT LIKE '%Str%' "
							+ "AND strasse NOT LIKE '% %' AND strasse NOT LIKE '%weg' "
							+ "AND strasse NOT LIKE '%ring' AND strasse NOT LIKE '%gasse' "
							+ "AND strasse NOT LIKE '%allee' AND strasse NOT LIKE '%berg' "
							+ "AND strasse NOT LIKE '%winkel' AND TRIM(strasse)!='' "
							+ "AND strasse NOT LIKE '%platz' "
							// + "AND fix NOT LIKE 'oldstreet%' AND fix NOT LIKE
							// 'samestreet%' "
							+ "AND fix LIKE 'missingxls%' AND sourcefile LIKE '%kraft%'" + " ORDER BY sourcefile");
			while (ru.next()) {
				count++;
				String str2search = ru.getString("strasse");
				String nn2search = ru.getString("nachname");
				String xls2search = ru.getString("sourcefile");

				// get xls file, but not if it is already open
				if (!xlsFilename.equals(useXLSDir + xls2search)) {
					xlsFilename = useXLSDir + xls2search;
					sheet = getSheet(xlsFilename);
				}
				if (sheet != null) {
					HSSFRow row = findRow(sheet, nn2search);
					if (row != null) {
						Coords posStr = findColumn(sheet, "straße");
						// if no straße column found try column 4
						boolean tryme = posStr == null;
						if (tryme) {
							posStr = new Coords((short) 3, (short) 0);
						}
						String inh = OVTImportHelper.getInhalt(row, posStr.x);
						String newStr = OVTImportHelper.getStrasse(inh);
						boolean goon = true;
						if (tryme) {
							goon = newStr.startsWith(str2search);
						}
						if (goon && !str2search.equals(newStr)) {
							ru.updateString("strasse", newStr);
							ru.updateString("fix", "oldstreet(" + str2search + ")");
							ru.updateRow();

							System.out.println(count + "  old street: " + str2search + "  orig street: " + inh
									+ " new street: " + newStr);
						} else if (!goon) {
							System.out.println(count + "  seems like not street column hit: " + inh + " should be: "
									+ str2search);
						} else {
							System.out.println(count + "  samestreet ###############");
							ru.updateString("fix", "samestreet");
							ru.updateRow();
						}
					} else {
						System.out.println("cannot find: " + nn2search + " [" + ru.getString("id") + "] in :"
								+ xls2search);
					}
				} else {
					ru.updateString("fix", "missingxls");
					ru.updateRow();
					System.out.println("cannot find: " + xls2search);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("start: " + start);
		System.out.println("ende: " + new java.util.Date());
	}

	private static HSSFSheet getSheet(String file) {
		HSSFSheet ret = null;
		POIFSFileSystem fs;
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			ret = wb.getSheet("Marktdaten");
			if (ret == null) {
				int nr = 1;
				if (wb.getNumberOfSheets() > nr) {
					ret = wb.getSheetAt(nr);
					System.out.println("   >got sheet by number, named: " + wb.getSheetName(nr));
				}
			}
			if (ret != null) {

			}
		} catch (Exception ex) {

		}
		return ret;
	}

	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================
	// ================ main =====================================

	public static void main(String[] args) {
		fixStreetNames();
	}

	private static Coords findColumn(HSSFSheet sheet, String s) {
		Coords ret = null;
		HSSFRow row;
		HSSFCell c;
		for (short i = 0; i < 40 && ret == null; i++) {
			row = sheet.getRow(i);
			if (row != null) {
				for (short j = 0; j < 20 && ret == null; j++) {
					c = row.getCell(j);
					if (c != null && c.getCellType() == HSSFCell.CELL_TYPE_STRING
							&& c.getRichStringCellValue().getString().toLowerCase().indexOf(s.toLowerCase()) == 0) {
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
		int nullrowCount = 0;
		for (short i = 0; i < 1000 && ret == null; i++) {
			row = sheet.getRow(i);
			// System.out.println(rowAsCSV(row));
			if (row != null) {
				for (short j = 0; j < 20 && ret == null; j++) {
					c = row.getCell(j);
					if (c != null && c.getCellType() == HSSFCell.CELL_TYPE_STRING
							&& c.getRichStringCellValue().getString().toLowerCase().indexOf(s.toLowerCase()) == 0) {
						ret = row;
					}
				}
			} else {
				if (nullrowCount > 20) {
					break;
				}
				nullrowCount++;
			}
		}
		return ret;
	}

	private static String rowAsCSV(HSSFRow row) {
		String ret = "";
		HSSFCell c;
		for (short j = 0; j < 30; j++) {
			c = row.getCell(j);
			if (c != null && c.getCellType() == HSSFCell.CELL_TYPE_STRING) {
				ret += c.getRichStringCellValue().getString() + ";";
			}
		}
		return ret;
	}
}

class Coords {
	public short x;
	public short y;

	public Coords(short x, short y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return this.x + ":" + this.y;
	}
}