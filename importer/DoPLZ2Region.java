package importer;

import java.sql.ResultSet;
import java.sql.SQLException;

import db.DBTools;
import db.Database;

public class DoPLZ2Region {

	private static void fixPLZ2Region(){
		try {
			Database.executeQuery("truncate plz2region;");
			Database.executeQuery("insert into plz2region (plz) SELECT distinct(plz) FROM kunden WHERE plz REGEXP '^[0-9]'");
			System.out.println("eins");
			ResultSet rs = Database.select("*", "plz2region");
			System.out.println("zwei");
			while (rs.next()){
				String reg = DBTools.regionOfPLZ(rs.getString("plz"));
				if (reg.length()>0){
					int regio = Integer.parseInt(reg);
					System.out.println(reg+" = "+regio);
					rs.updateInt("region", regio);
					rs.updateRow();
					System.out.println(rs.getString("plz")+":"+rs.getString("region"));
				}
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//	 ================ main =====================================
	//	 ================ main =====================================
	//	 ================ main =====================================
	//	 ================ main =====================================
	
	public static void main(String[] args) {
		fixPLZ2Region();
	}
}
