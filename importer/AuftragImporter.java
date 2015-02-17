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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


import db.Contact;
import db.Database;

public class AuftragImporter {
	
	
	private static void doImport(){
		
		// get files from actual directory
		File[] toImp  = new File("D:/eclipse/OVTDATA/input/referenzhaus").listFiles(new FilenameFilter(){
			public boolean accept(File d, String s) {
				if (d.isDirectory()) {
					if (s.indexOf("Referenzhaus")>=0 && s.endsWith("xls")){
						return true;	
					}
				}
				return false;
			}
		});
		
		// go trough all files
		POIFSFileSystem fs;
		for (File file : toImp) {
			try {
				 String fname = file.getName().replace("-", "_");
				 System.out.println(">>>>>>>>> "+fname);

				fs = new POIFSFileSystem(new FileInputStream(file));
				HSSFWorkbook wb    = new HSSFWorkbook(fs);
				HSSFSheet sheet    = wb.getSheetAt((short)0);
				int ridx = 0;
				HSSFRow aRow = sheet.getRow(ridx);
				while(aRow != null){
					HSSFCell firstCell = aRow.getCell((short)0);
//					System.out.println(ridx);
					if (firstCell != null && firstCell.getStringCellValue().trim().length()>0 && !firstCell.getStringCellValue().trim().equals("Kunde")){
						String nn  = firstCell.getStringCellValue();
						String plz = "";
						HSSFCell plzCell = aRow.getCell((short)1);
						if (plzCell.getCellType()==HSSFCell.CELL_TYPE_STRING){
							plz = plzCell.getStringCellValue();
						} else {
							plz = Integer.toString((int)plzCell.getNumericCellValue());
						}
						// try to find kunde
						String id = "";
						int count = 0;
						try {
							ResultSet rs = Database.select("id", "kunden", "WHERE nachname='"+nn+"' AND plz='"+plz+"'");
							while(rs.next()){
								id = rs.getString("id");
								count++;
							}
							Database.close(rs);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						
						// what to do 
						if (count <=0){
							// add contact first???
							Contact c = new Contact();
							c.setNachName(nn);
							c.setVorName("aus refhaus");
							c.setPlz(plz);
							String getTmp        = OVTImportHelper.getInhalt(aRow, (short)3);
							String strasseInh    = OVTImportHelper.getStrasse(getTmp);
							String hausNrInh     = OVTImportHelper.getHausNr(getTmp);
							c.setStrasse(strasseInh);
							c.setHausnr(hausNrInh);
							c.setTelefonPrivat(OVTImportHelper.getInhalt(aRow, (short)4));
							c.saveNewToDB();
							id = c.getId();
							System.out.println(id+" new for name: "+nn+" and plz: "+plz);
						} else {
							System.out.println(count+" : "+id+" found for name: "+nn+" and plz: "+plz);
						}
						
						// make auftrag
						java.sql.Date from = null;
						if (aRow.getCell((short)5).getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
							double aDate = aRow.getCell((short)5).getNumericCellValue();
							java.util.Date angelegt = null;
							try {
								angelegt = org.apache.poi.hssf.usermodel.HSSFDateUtil.getJavaDate(aDate);
								from     = new Date(angelegt.getTime());
								System.out.println(aDate+" >> "+from);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
						String liefer  = OVTImportHelper.getInhalt(aRow, (short)6);
						String produkt = OVTImportHelper.extractProdukt(OVTImportHelper.getInhalt(aRow, (short)7));
						String auftrnm = OVTImportHelper.getInhalt(aRow, (short)8);
						String vk      = OVTImportHelper.extractWL(OVTImportHelper.getInhalt(aRow, (short)9));
						
						try {
							PreparedStatement p0 = Database.getPreparedStatement("INSERT INTO auftraege " +
									"(kunde, datum, lieferung, produkt, auftragnehmer, verkaeufer) " +
							"VALUES (?,?,?,?,?,?)");
							p0.setString  ( 1, id);    
							p0.setDate    ( 2, from);  
							p0.setString  ( 3, liefer);  
							p0.setString  ( 4, produkt);  
							p0.setString  ( 5, auftrnm);  
							p0.setString  ( 6, vk);  
							p0.executeUpdate();
							Database.close(p0);
							
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					aRow = sheet.getRow(++ridx);
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] argv){
		doImport();
	}
}
