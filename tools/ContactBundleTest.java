package tools;

//import junit.framework.Assert;

//import org.junit.Test;

import db.Contact;

public class ContactBundleTest {

	//@Test
	public void testCollectAdminContacts() {
		String plz = "64720";
		String stadt = "";
		// ListItem type = new ListItem("1", "Bisher ohne Kontakt");
		// ListItem type = new ListItem("2", "Bisher einmal Kontakt");
		// ListItem type = new ListItem("3", "Bereits mehrmals Kontakt");
		ListItem type = new ListItem("4", "Erstkontakte oder einmal Kontakt");
		// ListItem type = new ListItem("5", "Erstkontakte oder mehrmals
		// Kontakt");
		boolean termin = false;
		boolean auftrag = false;
		int shfflag = 1; // 1='solar, haustür, fenster'; 2='fenster,
		// haustür'; 3='solar'
		int anz = 3000;
		java.sql.Date priorDate = java.sql.Date.valueOf("2007-01-01");
		boolean bigExcelExport = false;
		ContactBundle cb = new ContactBundle(plz, stadt, type, termin, auftrag, shfflag, anz, "", "", priorDate, "",
				"", bigExcelExport, true, null);
		cb.collectAdminContacts();

		// check that given contact isnt in list
		String idOfContact = "272536";
		//for (Contact c : cb.getContacts()) {
			//Assert.assertFalse("contact found you idiot", c.getId().equals(idOfContact));
		//}
	}
}
