package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class HSSFTools {
	/**
	 * fill a cell with a string
	 * 
	 * @param sheet
	 *            sheet where the cell is in
	 * @param col
	 *            column (starts by 0)
	 * @param row
	 *            row (starts by 0)
	 * @param inh
	 *            the string to fill in
	 */
	public static HSSFCell fillCell(HSSFSheet sheet, int col, int row, String inh) {
		HSSFCell cell = null;
		HSSFRow xlsrow = sheet.getRow(row);
		if (xlsrow == null) {
			xlsrow = sheet.createRow(row);
		}
		if (xlsrow != null) {
			cell = xlsrow.getCell((short) col);
			if (cell == null) {
				cell = xlsrow.createCell((short) col);
			}
			if (cell != null) {
				try {
					cell.setCellValue(new HSSFRichTextString(inh));
				} catch (ClassCastException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
		}
		return cell;
	}

	public static HSSFCell clearCell(HSSFSheet sheet, int col, int row) {
		HSSFCell cell = null;
		HSSFRow xlsrow = sheet.getRow(row);
		if (xlsrow == null) {
			xlsrow = sheet.createRow(row);
		}
		if (xlsrow != null) {
			cell = xlsrow.getCell((short) col);
			if (cell == null) {
				cell = xlsrow.createCell((short) col);
			}
			if (cell != null) {
				try {
					cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
				} catch (ClassCastException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
		}
		return cell;
	}

	/**
	 * fill a cell with a string
	 * 
	 * @param sheet
	 *            sheet where the cell is in
	 * @param col
	 *            column (starts by 0)
	 * @param row
	 *            row (starts by 0)
	 * @param inh
	 *            the string to fill in
	 */
	public static HSSFCell fillCellFormula(HSSFSheet sheet, int col, int row, String inh) {
		HSSFCell cell = null;
		HSSFRow xlsrow = sheet.getRow(row);
		if (xlsrow == null) {
			xlsrow = sheet.createRow(row);
		}
		if (xlsrow != null) {
			cell = xlsrow.getCell((short) col);
			if (cell == null) {
				cell = xlsrow.createCell((short) col);
			}
			if (cell != null) {
				try {
					// cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
					cell.setCellFormula(inh);
				} catch (ClassCastException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
		}
		return cell;
	}

	/**
	 * fill a cell with a int
	 * 
	 * @param sheet
	 *            sheet where the cell is in
	 * @param col
	 *            column (starts by 0)
	 * @param row
	 *            row (starts by 0)
	 * @param inh
	 *            the string to fill in
	 */
	public static HSSFCell fillCell(HSSFSheet sheet, int col, int row, int inh) {

		HSSFCell cell = null;
		HSSFRow xlsrow = sheet.getRow(row);
		if (xlsrow == null) {
			xlsrow = sheet.createRow(row);
		}
		if (xlsrow != null) {
			cell = xlsrow.getCell((short) col);
			if (cell == null) {
				cell = xlsrow.createCell((short) col);
			}
			if (cell != null) {
				try {
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue((double) inh);
				} catch (ClassCastException e) {
					MyLog.showExceptionErrorDialog(e);
					e.printStackTrace();
				}
			}
		}
		return cell;
	}

	/**
	 * fill a cell with a date
	 * 
	 * @param sheet
	 *            sheet where the cell is in
	 * @param col
	 *            column (starts by 0)
	 * @param row
	 *            row (starts by 0)
	 * @param inh
	 *            the date to fill in
	 */
	public static HSSFCell fillCell(HSSFSheet sheet, int col, int row, Date inh, boolean mitJahr) {
		HSSFCell cell = null;
		HSSFRow xlsrow = sheet.getRow(row);
		if (xlsrow == null) {
			xlsrow = sheet.createRow(row);
		}
		if (xlsrow != null) {
			cell = xlsrow.getCell((short) col);
			if (cell == null) {
				cell = xlsrow.createCell((short) col);
			}
			try {
				// make nice date string
				String format = "dd.MM";
				if (mitJahr) {
					format = "dd.MM.yy";
				}
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				cell.setCellValue(new HSSFRichTextString(sdf.format(inh)));
			} catch (ClassCastException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		}
		return cell;
	}

	/**
	 * fill a cell with a date
	 * 
	 * @param sheet
	 *            sheet where the cell is in
	 * @param col
	 *            column (starts by 0)
	 * @param row
	 *            row (starts by 0)
	 * @param inh
	 *            the date to fill in
	 */
	public static HSSFCell fillCell(HSSFSheet sheet, int col, int row, double inh) {
		HSSFCell cell = null;
		HSSFRow xlsrow = sheet.getRow(row);
		if (xlsrow == null) {
			xlsrow = sheet.createRow(row);
		}
		if (xlsrow != null) {
			cell = xlsrow.getCell((short) col);
			if (cell == null) {
				cell = xlsrow.createCell((short) col);
			}
			try {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue(inh);
			} catch (ClassCastException e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			} catch (Exception e) {
				MyLog.showExceptionErrorDialog(e);
				e.printStackTrace();
			}
		} else {
			MyLog.logError("Konnte HSSFRow " + row + " nicht finden.");
		}
		return cell;
	}

	public enum BorderStyle {
		lr, lrtb, lrt, lrb, tb, n
	};

	public enum Alignment {
		c, l, r
	};

	public static void setCellStyle(HSSFWorkbook wb, HSSFCell cell, BorderStyle bs) {
		setCellStyle(wb, cell, bs, Alignment.l);
	}

	public static void setCellStyle(HSSFWorkbook wb, HSSFCell cell, BorderStyle bs, Alignment a) {
		if (cell != null) {
			HSSFCellStyle style = wb.createCellStyle();
			switch (bs) {
			case lr:
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				style.setLeftBorderColor(HSSFColor.BLACK.index);
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
				style.setRightBorderColor(HSSFColor.BLACK.index);
				break;
			case lrtb:
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				style.setLeftBorderColor(HSSFColor.BLACK.index);
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
				style.setRightBorderColor(HSSFColor.BLACK.index);
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				style.setBottomBorderColor(HSSFColor.BLACK.index);
				style.setBorderTop(HSSFCellStyle.BORDER_THIN);
				style.setTopBorderColor(HSSFColor.BLACK.index);
				break;
			case lrt:
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				style.setLeftBorderColor(HSSFColor.BLACK.index);
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
				style.setRightBorderColor(HSSFColor.BLACK.index);
				style.setBorderTop(HSSFCellStyle.BORDER_THIN);
				style.setTopBorderColor(HSSFColor.BLACK.index);
				break;
			case lrb:
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				style.setLeftBorderColor(HSSFColor.BLACK.index);
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
				style.setRightBorderColor(HSSFColor.BLACK.index);
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				style.setBottomBorderColor(HSSFColor.BLACK.index);
				break;
			case tb:
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				style.setBottomBorderColor(HSSFColor.BLACK.index);
				style.setBorderTop(HSSFCellStyle.BORDER_THIN);
				style.setTopBorderColor(HSSFColor.BLACK.index);
				break;
			default:
				break;
			}
			switch (a) {
			case l:
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				break;
			case r:
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				break;
			case c:
				style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				break;
			default:
				break;
			}
			cell.setCellStyle(style);
		}
	}

	// tut nich...
	public static String int2ExcelKoords(int row, int col) {
		String ret = "";
		String[] alph = { "", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
				"S", "T", "U", "V", "W", "X", "Y", "Z" };
		if (col > 26) {
			ret += alph[col % 26] + alph[col / 26] + Integer.toString(row);
		} else {
			ret += alph[col] + Integer.toString(row);
		}
		return ret;
	}

	public static Vector<Vector<String>> getStringVectorFromExcel(String fileName, String sheetName) {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		HSSFSheet sheet = GetSheet(fileName, sheetName);
		if (sheet != null) {
			// perhaps physical nuber of rows is not right...when there are
			// empty rows some later cell could be ignored
			int maxRows = sheet.getPhysicalNumberOfRows();
			for (int i = 0; i < maxRows; i++) {
				HSSFRow aRow = sheet.getRow(i);
				if (aRow != null) {
					int maxCols = aRow.getPhysicalNumberOfCells();
					Vector<String> aRowAsString = new Vector<String>();
					for (int j = 0; j < maxCols; j++) {
						try {
							String inh = "";
							HSSFCell cell = aRow.getCell((short) j);
							if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
								// check cell format and get value
								if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
									inh = cell.getRichStringCellValue().getString().trim();
								} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
									if (HSSFDateUtil.isCellDateFormatted(cell)) {
										java.util.Date d = cell.getDateCellValue();
										inh = d.toString();
									} else {
										double tmp = cell.getNumericCellValue();
										inh = Double.toString(tmp);
									}
								} else {
									System.out.println("cell has not right format: " + cell.getCellType());
								}
							}
							aRowAsString.add(inh);
						} catch (RuntimeException e) {
							e.printStackTrace();
						}
					}
					ret.add(aRowAsString);
				}
			}
		}
		return ret;
	}

	private static HSSFSheet GetSheet(String fileName, String sheetName) {
		HSSFSheet sheetToFill = null;
		// check for that file
		File inputFile = new File(fileName);
		if (inputFile.exists()) {
			if (inputFile.canRead()) {
				try {
					POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inputFile));
					HSSFWorkbook wb = new HSSFWorkbook(fs);
					sheetToFill = wb.getSheet(sheetName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// something in hssf went wrong
				if (sheetToFill == null) {
					System.out.println("Unvorhergesehener Fehler: Tabellenblatt konnte nicht gefunden werden: "
							+ sheetName);
				}
			} else {
				System.out.println("Unvorhergesehener Fehler: Exceldatei konnte nicht gelesen werden: " + fileName);
			}
		}
		// check if vorlage exists
		// return vorlage
		return sheetToFill;
	}

	public static void main(String[] ergv) {
		Vector<Vector<String>> v = getStringVectorFromExcel("imp.xls", "Tabelle1");
		for (Vector<String> vector : v) {
			for (String string : vector) {
				System.out.print(string + ";");
			}
			System.out.println();
		}
	}
}
