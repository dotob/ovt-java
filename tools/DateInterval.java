package tools;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import db.Database;

public class DateInterval {
	private Date von; 
	private Date bis;
	
	public DateInterval(Date von , Date bis){
		this.von = von;
		this.bis = bis;
	}

	public DateInterval(){
		this.von = null;
		this.bis = null;
	}
	
	public String toString(){
		// make nice date string
		String format = "dd.MM";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		if (this.von!=null && this.bis!=null){
			String ret = sdf.format(this.von)+" - "+sdf.format(this.bis);
			return ret;
		}
		return "n/a";
	}
	
	public void printMe(){
		MyLog.o("von:"+this.von+" bis:"+this.bis);
	}
	
	public void shiftWeeks(int howmany){
		try {
			ResultSet rs = Database.select("abr_woche, abr_jahr", "abrechnungswochen", "WHERE von="+this.von);
			while (rs.next()){
				int w = rs.getInt("abr_woche");
				int j = rs.getInt("abr_jahr");
				ResultSet rs2 = Database.select("von, bis", "abrechnungswochen", "WHERE abr_woche="+(w+howmany)+" AND abr_jahr="+j);
				while (rs2.next()){
					this.von = rs2.getDate("von");
					this.bis = rs2.getDate("bis");
				}
				Database.close(rs2);
			}
			Database.close(rs);
		} catch (SQLException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
	}
	
	public Date getBis() {
		return bis;
	}

	public void setBis(Date bis) {
		this.bis = bis;
	}

	public Date getVon() {
		return von;
	}

	public void setVon(Date von) {
		this.von = von;
	}
	
	public boolean equals(DateInterval range){
		boolean ret =  this.von.equals(range.von);
		ret &= this.bis.equals(range.bis);
		return ret;
	}
}
