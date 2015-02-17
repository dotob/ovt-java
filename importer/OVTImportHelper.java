package importer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import db.Database;

public class OVTImportHelper {

	private static HashMap<String, String>	hm;
	private static HashMap<String, String>	products;

	// here comes only static stuff
	public static String getInhalt(HSSFRow row, short column) {
		String ret = "";
		if (row != null && column > 0) {
			HSSFCell cell = row.getCell(column);
			if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
				// check cell format and get value
				if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
					ret = cell.getRichStringCellValue().getString().trim().replaceAll("'", " ");
				} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
					double tmp = cell.getNumericCellValue();
					ret = Double.toString(tmp);
				} else {
					System.out.println("cell has not right format: " + cell.getCellType());

				}
			}
		}
		return ret;
	}

	public static String getPLZ(String input) {
		return strHead(input, 5);
	}

	public static String getOrt(String input) {
		return strTail(input.trim(), -5).trim();
	}

	public static String getHausNr(String input) {
		String ret = "";
		if (input.length() > 0) {
			boolean go = true;
			int i = 0;
			int a = input.length() - 1;
			char c = input.charAt(a);
			while (a >= 0 && go) {
				if (!Character.isWhitespace(c)) {
					if ((i < 1 && Character.isLetter(c)) || Character.isDigit(c)) {
						c = input.charAt(--a);
						i++;
					} else {
						go = false;
					}
				} else {
					go = false;
				}
			}
			ret = strTail(input, -1 * a - 1);
		}
		return ret;
	}

	public static String getStrasse(String input) {
		// System.out.println(input);
		int a = 0;
		if (input != null && input.length() > 0) {
			char c = input.charAt(a);
			while (a < input.length() && !Character.isDigit(c)) {
				c = input.charAt(a++);
				// System.out.println(c+":"+Character.isLetter(c)+":"+Character.isWhitespace(c));
			}
			a--;
		}
		String ret = strHead(input, a).trim();
		// System.out.println(a+" ret:"+ret);
		// System.out.println();
		return ret;
	}

	public static String interpreteColor(String colStr) {
		String ret = "0";
		if (colStr.indexOf("weiÃŸ") >= 0 || colStr.toLowerCase().equals("w")) {
			ret = "1";
		} else if (colStr.indexOf("beige") >= 0 || colStr.toLowerCase().equals("bg")) {
			ret = "2";
		} else if (colStr.indexOf("grÃ¼n") >= 0) {
			ret = "3";
		} else if (colStr.indexOf("gelb") >= 0) {
			ret = "4";
		} else if (colStr.indexOf("schwarz") >= 0) {
			ret = "5";
		} else if (colStr.indexOf("rot") >= 0) {
			ret = "6";
		} else if (colStr.indexOf("gold") >= 0) {
			ret = "7";
		} else if (colStr.indexOf("braun") >= 0 || colStr.toLowerCase().equals("br")) {
			ret = "8";
		} else if (colStr.toLowerCase().indexOf("alu") >= 0) {
			ret = "9";
		} else if (colStr.indexOf("grau") >= 0) {
			ret = "10";
		} else if (colStr.indexOf("blau") >= 0) {
			ret = "15";
		} else if (colStr.indexOf("silber") >= 0) {
			ret = "17";
		}
		return ret;
	}

	public static String extractMarktdatenermittler(String fileName) {
		if (hm == null) {
			try {
				ResultSet rs = Database.select("DISTINCT nachname, id", "marktdatenermittler",
						"WHERE id NOT IN (6,7,40,176)");
				hm = new HashMap<String, String>();
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
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		String ret = "";
		for (Iterator<String> iter = hm.keySet().iterator(); iter.hasNext();) {
			String mde = iter.next();
			if (fileName.toLowerCase().matches(".*" + mde.toLowerCase() + ".*")) {
				ret = hm.get(mde);
				// System.out.println("MDE: "+mde+" : "+fileName);
			}
		}
		return ret;
	}

	public static String extractProdukt(String prodInh) {
		if (products == null) {
			try {
				ResultSet rs = Database.select("kurzformen, id", "produkte");
				products = new HashMap<String, String>();
				while (rs.next()) {
					products.put(rs.getString("kurzformen"), rs.getString("id"));
				}
				Database.close(rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		String ret = "";
		for (Iterator<String> iter = products.keySet().iterator(); iter.hasNext();) {
			String prodNames = iter.next();
			// System.out.println(prodInh+":"+prodNames);
			if (prodNames.matches(".*" + prodInh + ".*")) {
				ret = products.get(prodNames);
			}
		}
		return ret;
	}

	public static String extractWL(String wlInh) {
		String ret = "";
		try {
			ResultSet rs = Database.select("id", "werbeleiter", "WHERE kurzname LIKE '%" + wlInh + "%'");
			while (rs.next()) {
				ret = rs.getString("id");
			}
			Database.close(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	// ============================================================================
	// ============================================================================
	// ============================================================================
	// ============================================================================
	public static String interpreteErgebnis(String input, int id) {
		String ret = "";
		if (input.length() > 0) {
			// ergebnisse:
			// 1 Erben
			// 2 gekauft
			// 3 jetzt nicht
			// 4 Kein Interesse
			// 5 Termin
			// 6 verkauft
			// 7 Wettbewerb
			// 8 Wintergarten-Interessent
			// 9 im nähsten Jahr
			// 10 in ein bis zwei Jahren
			// 11 in den nÃ¤chsten Jahren
			// 12 nur nach 18Uhr oder Samstags
			// 13 Anrufbeantworter
			// 14 nicht erreicht
			// 15 Falsche Daten
			// 16 Ausländer
			// 17 Bekannte
			// 18 Freiberufler
			// 19 Mangelhafte Daten
			// 20 Mehrfamilienhaus
			// 21 alles neu
			// 22 in den letzten 4 Wochen erneuert
			// 23 in den letzten 12 Monaten erneuert
			// 24 in den letzten 24 Monaten erneuert
			// 25 selbstständig
			// 26 mieter

			input = input.toLowerCase();
			input = input.replace("plus", "+");
			if (input.indexOf("erb") >= 0 || input.indexOf("kind") >= 0) {
				ret = "1"; // 1 Erben
			} else if (input.indexOf("gekau") >= 0) {
				ret = "2"; // 2 gekauft
			} else if ((input.indexOf("nicht") >= 0 && input.indexOf("jetzt") >= 0) || input.indexOf("urlaub") >= 0) {
				ret = "3"; // 3 jetzt nicht
			} else if (input.indexOf("ki") >= 0 || input.indexOf("inter") >= 0) {
				ret = "4"; // 4 Kein Interesse
			} else if (input.indexOf("t.") >= 0 || input.indexOf("term") >= 0 || input.indexOf("fenster") >= 0) {
				ret = "5"; // 5 Termin
			} else if (input.indexOf("verka") >= 0) {
				ret = "6"; // 6 verkauft
			} else if (input.indexOf("wett") >= 0) {
				ret = "7"; // 7 Wettbewerb
			} else if (input.indexOf("wint") >= 0 || input.indexOf("wg") >= 0) {
				ret = "8"; // 8 Wintergarten-Interessent
			} else if (input.startsWith("1") || input.startsWith("+")) {
				if (input.indexOf("2") >= 0) {
					ret = "10"; // 10 in ein bis zwei Jahren
				} else if (input.indexOf("3") >= 0 || input.indexOf("6") >= 0 || input.indexOf("5") >= 0) {
					ret = "11"; // 11 in den nächsten Jahren
				} else {
					ret = "9"; // 9 im nächsten Jahr
				}
			} else if (input.indexOf("18") >= 0 || input.indexOf("samstag") >= 0) {
				ret = "12"; // 12 nur nach 18Uhr oder Samstags
			} else if (input.indexOf("ab") >= 0 || input.indexOf("beantworter") >= 0) {
				ret = "13"; // 13 Anrufbeantworter
			} else if (input.indexOf("ne") >= 0 || input.indexOf("erreicht") >= 0) {
				ret = "14"; // 14 nicht erreicht
			} else if (input.indexOf("falsch") >= 0 || input.indexOf("anschl") >= 0 || input.indexOf("fn") >= 0
					|| input.indexOf("nr") >= 0 || input.indexOf("nummer") >= 0) {
				ret = "15"; // 15 Falsche Daten
			} else if (input.indexOf("ausl") >= 0) {
				ret = "16"; // 16 Ausländer
			} else if (input.indexOf("bekann") >= 0 || input.indexOf("freund") >= 0 && input.indexOf("freundl") < 0) {
				ret = "17"; // 17 Bekannte
			} else if (input.indexOf("freiberuf") >= 0) {
				ret = "18"; // 18 Freiberufler
			} else if (input.indexOf("markt") >= 0 || input.indexOf("mangel") >= 0) {
				ret = "19"; // 19 Mangelhafte Daten
			} else if (input.indexOf("mehrfa") >= 0) {
				ret = "20"; // 20 Mehrfamilienhaus
			} else if (input.startsWith("neu")) {
				if (input.indexOf("24") >= 0) {
					ret = "24"; // 24 in den letzten 24 Monaten erneuert
				} else if (input.indexOf("12") >= 0) {
					ret = "23"; // 23 in den letzten 12 Monaten erneuert
				} else if (input.indexOf("1") >= 0) {
					ret = "22"; // 22 in den letzten 4 Wochen erneuert
				} else {
					ret = "21"; // 21 alles neu
				}
			} else if (input.indexOf("selbst") >= 0) {
				ret = "25"; // 25 selbstständig
			} else if (input.indexOf("mieter") >= 0) {
				ret = "26"; // 26 mieter
			} else if (input.toLowerCase().startsWith("au")) {
				ret = "30"; // 26 mieter
			} else {
				System.out.println(id + "  kein ergebnis: " + input);
			}
		}
		return ret;
	}

	/**
	 * searches for an mde in several places
	 * 
	 * @return id of found marktdatenermittler
	 */
	public static String lookupOrCreateMafo(HSSFSheet sheet, HSSFRow row, short bearbCol) {
		String ret = "";
		String mafo = "";
		// check for value of bearbeiter
		HSSFCell aCell = row.getCell(bearbCol);
		if (aCell != null && aCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			String inh = aCell.getRichStringCellValue().getString();
			if (inh != null) {
				inh = inh.trim();
				if (inh.length() > 0) {
					// starts with date:
					if (Character.isDigit(inh.charAt(0))) {
						int blankPos = inh.indexOf(" ");
						if (blankPos > 0) {
							mafo = strTail(inh, -1 * blankPos);
						}
					} else {
						// perhaps it starts with bearbeiter
						int blankPos = inh.indexOf(" ");
						if (blankPos > 0) {
							mafo = strHead(inh, blankPos);
						}
					}
				}
			}

			if (mafo.length() > 0) {
				// if there was some inhalt try to find the right marktforcher
				try {
					ResultSet rs = Database.select("*", "marktforscher", "WHERE nachname='" + mafo + "' OR kurzname='"
							+ mafo + "'");
					while (rs.next()) {
						ret = rs.getString("id");
					}
					Database.close(rs);
					if (ret.length() > 0) {
						System.out.println("> found mafo: " + ret + " for:" + inh);
					} else {
						// insert into marktdatenermittler
						PreparedStatement p = Database.getPreparedStatement("INSERT INTO marktforscher "
								+ "(nachname) VALUES (?)");
						p.setString(1, mafo);
						p.executeUpdate();
						Database.close(p);
						// get id of last marktdatenermittler
						p = Database.getPreparedStatement("SELECT LAST_INSERT_ID()");
						rs = p.executeQuery();
						while (rs.next()) {
							ret = rs.getString(1);
						}
						Database.close(rs);
						System.out.println("> neuen marktforscher angelegt: " + mafo + " id: " + ret);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public static void main(String[] argv) {
		// testing
		System.out.println(false && false);
		System.out.println(Character.isLetter('4'));
		getStrasse("Alter Weg 4");

		// extractBearbeiter();
		// try{
		// FileReader f = new FileReader (
		// "D:/eclipse/OVT/daten/input/ergebniswerte_gruppiert28.9.05.txt" );
		//
		// StringBuffer aLine = new StringBuffer();
		// for ( int c; ( c = f.read() ) != -1; ){
		// char b = (char)c;
		// if (Character.isISOControl(b)){
		// String t = aLine.toString();
		// if (!t.startsWith("#") && t.trim().length()>0){
		// System.out.println(interpreteErgebnis(t, 0)+" :"+t);
		// }
		// aLine = new StringBuffer();
		// } else {
		// aLine.append(b);
		// }
		// }
		//
		// f.close();
		// } catch ( IOException e ) {
		// System.out.println( "Fehler beim Lesen der Datei" );
		// }
	}

	public static String strHead(String input, int to) {
		String ret = "";
		int l = input.length();
		if (to > 0 && to < l) {
			to = Math.min(l, to);
			ret = input.substring(0, to);
		} else if (to < 0 && to * -1 < l) {
			to = Math.max(l * -1, to);
			ret = input.substring(0, l + to);
		}
		return ret;
	}

	public static String strTail(String input, int to) {
		String ret = "";
		int l = input.length();
		if (to > 0) {
			to = Math.min(l, to);
			ret = input.substring(l - to, l);
		} else if (to < 0) {
			to = Math.max(l * -1, to);
			ret = input.substring(to * -1, l);
		}
		return ret;
	}

	public static String strToken(String input, int which, String trenner) {
		String ret = "";
		String[] ss = input.split(trenner);
		if (which > 0 && which <= ss.length) {
			ret = ss[which - 1];
		}
		return ret;
	}

	public static String stripNumber(String s) {
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
}
