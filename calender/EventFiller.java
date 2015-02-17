package calender;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;

class EventFiller {

	private static void ftTermine() {
		// settings
		int y         = 2007;
		int fromMonth = 5;
		int toMonth   = 5;
		int month4Cal = Calendar.MAY;

		String table               = "ft_events";
		String fehtTxt             = "frei FE+HT";
		String fehtSolarTxt        = "frei FE+HT, Solar";
		String fehtTxtRueck        = "FE+HT Rücksprache";
		String fehtSolarTxtRueck   = "FE+HT,Solar Rücksprache";
		boolean mittelNordHessen   = true;
		boolean suedHessen         = false;
		boolean badenWuerttemberg  = false;
		boolean saarland           = false;
		boolean franken            = false;
		boolean eifel              = false;
		int dCol = 1;

		// do it
		int months[] = {0,31,28,31,30,31,30,31,31,30,31,30,31};
		String insStr =  "INSERT INTO `"+table+"` ( ";
		insStr        += "`id` , `timestamp` , `title` , `cat` , `daycolumn`, `starttime` , `endtime` , `day` , `month` , `year` , `user`) VALUES (";
		insStr        += "''   , NOW( ), ";

		StringBuffer output = new StringBuffer();
		for (int m=fromMonth; m<=toMonth; m++){ // which months to do?
			for (int d=1; d<=months[m]; d++){
				Calendar cal = new GregorianCalendar(y, month4Cal, d);
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek!=Calendar.SUNDAY && dayOfWeek!=Calendar.SATURDAY && d!=24 && d!=25){

					//	mittel+Nordhessen
					//	mittel+Nordhessen
					//	mittel+Nordhessen
					int cat = 2; 
					if (mittelNordHessen){
						if (d!=1 && d!=17 && d!=28){
							if (dayOfWeek==Calendar.MONDAY || dayOfWeek==Calendar.TUESDAY || dayOfWeek==Calendar.THURSDAY){
								dCol=1;
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");

								dCol=2;
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							}
							if (dayOfWeek==Calendar.WEDNESDAY || dayOfWeek==Calendar.FRIDAY){
								dCol=1;
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							}
						}
					}

					//	Südhessen
					//	Südhessen
					//	Südhessen
					cat = 3; 
					if (suedHessen){
						switch (dayOfWeek){
						case Calendar.MONDAY:
						case Calendar.TUESDAY:
						case Calendar.THURSDAY:
							dCol=1;
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							break;
						case Calendar.WEDNESDAY:
							dCol=1;
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							dCol=2;
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							break;
						case Calendar.FRIDAY:
							dCol=1;
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");

							dCol=2;
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtSolarTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							break;

						}
					}

					// baden württemberg
					// baden württemberg
					// baden württemberg
					cat = 4;
					if (badenWuerttemberg){
						// fe+ht
						dCol=1;

						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");

						dCol=2;

						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						output.append(insStr+"'"+fehtTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					}

					// saarland
					// saarland
					// saarland
					cat = 5;
					if (saarland){
						dCol=1;
						if (dayOfWeek==Calendar.TUESDAY || dayOfWeek==Calendar.THURSDAY){
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						}							
					}

					// franken
					// franken
					// franken
					cat = 6;
					if (franken){
						// fe+ht
						dCol=1;
						if (dayOfWeek==Calendar.THURSDAY || dayOfWeek==Calendar.WEDNESDAY || dayOfWeek==Calendar.TUESDAY){
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						} 
					}
					
					// eifel
					// eifel
					// eifel
					cat = 7;
					if (eifel){
						// fe+ht
						dCol=1;
						if (dayOfWeek==Calendar.THURSDAY || dayOfWeek==Calendar.WEDNESDAY || dayOfWeek==Calendar.TUESDAY){
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxt+"', '"+cat+"', '"+dCol+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+fehtTxtRueck+"', '"+cat+"', '"+dCol+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						} 
					}
				} else if (d==13){
					// for konferenz
					
					String konferenzTxt = "FE & HT Konferenz";
					output.append(insStr+"'"+konferenzTxt+"', '2', '1'   , '9:00'     , '13:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					output.append(insStr+"'"+konferenzTxt+"', '3', '1'   , '9:00'     , '13:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					output.append(insStr+"'"+konferenzTxt+"', '4', '1'   , '9:00'     , '13:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
//					output.append(insStr+"'"+konferenzTxt+"', '5', '1'   , '9:00'     , '13:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					output.append(insStr+"'"+konferenzTxt+"', '6', '1'   , '9:00'     , '13:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					output.append(insStr+"'"+konferenzTxt+"', '7', '1'   , '9:00'     , '13:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
				}
			}
		}

		// write output
		Writer outputFile = null;
		try {
			File commandFile = new File("fillCalendar.sql");
			outputFile = new BufferedWriter(new FileWriter(commandFile));
			outputFile.write(output.toString());
			System.out.println(output.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// flush and close both "output" and its underlying FileWriter
			try {
				if (outputFile != null) outputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("###  FERTIG FENSTER TÜREN ####");
	}
	private static void solarTermine() {
		// settings
		int y         = 2007;
		int fromMonth = 5;
		int toMonth   = 5;
		int month4Cal = Calendar.MAY;

		String solarTxt            = "frei";
		String solarTxtRsp         = "nach Rücksprache";
		boolean mittelNordHessen   = true;
		boolean suedHessen         = true;
		boolean badenWuerttemberg  = true;
		boolean saarland           = false;
		boolean franken            = false;

		// do it
		int months[] = {0,31,28,31,30,31,30,31,31,30,31,30,31};
		String insStr =  "INSERT INTO `solar_events` ( ";
		insStr        += "`id` , `timestamp` , `title` , `cat` , `daycolumn`, `starttime` , `endtime` , `day` , `month` , `year` , `user`) VALUES (";
		insStr        += "''   , NOW( ), ";

		StringBuffer output = new StringBuffer();
		for (int m=fromMonth; m<=toMonth; m++){ // which months to do?
			for (int d=1; d<=months[m]; d++){
				Calendar cal = new GregorianCalendar(y, month4Cal, d);
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek!=Calendar.SUNDAY && d!=12){

					//	mittel+Nordhessen
					//	mittel+Nordhessen
					//	mittel+Nordhessen
					int cat = 2; 
					if (mittelNordHessen){
						if (dayOfWeek!=Calendar.SATURDAY){
							for (int col = 1; col<=1; col++){
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							}
						} else {
							int col = 1;
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						}
					}

					//	Südhessen
					//	Südhessen
					//	Südhessen
					cat = 3; 
					if (suedHessen){
						if (dayOfWeek!=Calendar.SATURDAY){
							for (int col = 1; col<=1; col++){
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							}
						} else {
							int col = 1;
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							
						}
					}

					// baden württemberg
					// baden württemberg
					// baden württemberg
					cat = 4;
					if (badenWuerttemberg){
						switch (dayOfWeek){
						case Calendar.MONDAY:
						case Calendar.FRIDAY:
							int col = 1;
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							col = 1;
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							break;
						case Calendar.TUESDAY:
						case Calendar.WEDNESDAY:
						case Calendar.THURSDAY:
							col = 1;
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							break;
						case Calendar.SATURDAY:
							col = 1;
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							break;
							
						}
						if (dayOfWeek!=Calendar.SATURDAY){
							for (int col = 1; col<=2; col++){
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
								output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							}
						} else {
							int col = 1;
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxtRsp+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							
						}
					}

					// saarland
					// saarland
					// saarland
					cat = 5;
					if (saarland){
						for (int col = 1; col<=3; col++){
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						}
					}

					// franken
					// franken
					// franken
					cat = 6;
					if (franken){
						for (int col = 1; col<=3; col++){
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '09:30'     , '11:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '11:30'     , '13:30'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '14:00'     , '16:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '16:00'     , '18:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
							output.append(insStr+"'"+solarTxt+"', '"+cat+"', '"+col+"'   , '18:00'     , '20:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
						}
					}
				} else if (d==12){
					// for konferenz
					
					String konferenzTxt = "Solar Konferenz";
					output.append(insStr+"'"+konferenzTxt+"', '2', '1'   , '9:00'     , '17:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					output.append(insStr+"'"+konferenzTxt+"', '3', '1'   , '9:00'     , '17:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
					output.append(insStr+"'"+konferenzTxt+"', '4', '1'   , '9:00'     , '17:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
//					output.append(insStr+"'"+konferenzTxt+"', '5', '1'   , '9:00'     , '17:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
//					output.append(insStr+"'"+konferenzTxt+"', '6', '1'   , '9:00'     , '17:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
//					output.append(insStr+"'"+konferenzTxt+"', '7', '1'   , '9:00'     , '17:00'   , '"+d+"'  , '"+m+"' , '"+y+"', 'overtuer');\n");
				}
			}
		}

		// write output
		Writer outputFile = null;
		try {
			File commandFile = new File("fillCalendar.sql");
			outputFile = new BufferedWriter(new FileWriter(commandFile));
			outputFile.write(output.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// flush and close both "output" and its underlying FileWriter
			try {
				if (outputFile != null) outputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("###  FERTIG SOLAR ####");
	}

	public static void main(String[] argv) {
		ftTermine();
//		solarTermine();
	}
}
