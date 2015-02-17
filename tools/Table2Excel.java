package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class Table2Excel {

	private HSSFWorkbook xlsFile;
	private HSSFSheet sheet;
	private JTable table;
	private String filename;
	private short[] fieldWidths;

	public Table2Excel(JTable table, String fileName, String sheetName, short[] fieldWidths) {
		this.table = table;
		this.filename = fileName;
		this.xlsFile = new HSSFWorkbook();
		this.sheet = this.xlsFile.createSheet(sheetName);
		this.fieldWidths = fieldWidths;
		this.fillSheetWithTableData();
	}

	public Table2Excel(JTable table, File excelTemplate, String filenameToSaveTo) {
		this.table = table;
		this.filename = filenameToSaveTo;
		if (excelTemplate.exists()) {
			if (excelTemplate.canRead()) {
				try {
					POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelTemplate));
					this.xlsFile = new HSSFWorkbook(fs);
					this.sheet = this.xlsFile.getSheetAt(0);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					MyLog.showExceptionErrorDialog(e);
				} catch (IOException e) {
					e.printStackTrace();
					MyLog.showExceptionErrorDialog(e);
				}
			}
		}
		this.fillSheetWithTableData();
	}

	public void addTableAsSheet(JTable table, String sheetName, short[] fieldWidths) {
		this.table = table;
		this.sheet = this.xlsFile.createSheet(sheetName);
		this.fieldWidths = fieldWidths;
		this.fillSheetWithTableData();
	}

	public void openXLS() {
		try {
			// write excel file
			FileOutputStream fileOut = new FileOutputStream(this.filename);
			this.xlsFile.write(fileOut);
			fileOut.close();

			// open excel
			File toOpen = new File(this.filename);
			SysTools.OpenFile(toOpen);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveXLS(String where) {
		try {
			// write excel file
			FileOutputStream fileOut = new FileOutputStream(where);
			this.xlsFile.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveXLS() {
		try {
			// write excel file
			FileOutputStream fileOut = new FileOutputStream(this.filename);
			this.xlsFile.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fillSheetWithTableData() {
		if (this.fieldWidths != null) {
			short col = 0;
			for (int i = 0; i < this.fieldWidths.length; i++) {
				short w = this.fieldWidths[i];
				this.sheet.setColumnWidth(col++, w);
			}
		}

		DefaultTableModel dtm = (DefaultTableModel) this.table.getModel();
		// fill header
		HSSFRow row = this.sheet.createRow(0);
		for (short i = 0; i < dtm.getColumnCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(new HSSFRichTextString(dtm.getColumnName(i)));
		}

		// fill data
		Vector data = dtm.getDataVector();
		short r = 1, c = 0;
		for (Iterator<Vector> iter = data.iterator(); iter.hasNext(); r++) {
			Vector rowData = iter.next();
			row = this.sheet.createRow(r);
			for (Iterator iterator = rowData.iterator(); iterator.hasNext(); c++) {
				Object cellData = (Object) iterator.next();
				if (cellData != null) {
					HSSFCell cell = row.createCell(c);
					cell.setCellValue(new HSSFRichTextString(cellData.toString()));
				}
			}
			c = 0;
		}
	}

}
