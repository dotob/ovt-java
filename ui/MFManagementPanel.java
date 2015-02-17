package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tools.ListItem;
import tools.StrTool;
import db.DBTools;
import db.Marktforscher;

public class MFManagementPanel extends JPanel implements ActionListener {

	private IMainWindow parentWindow;
	private JComboBox chooseMafoCombobox;
	private JTextField mfAnrede;
	private JTextField mfVorname;
	private JTextField mfNachame;
	private JTextField mfGruppe;
	private JTextField mfStrasse;
	private JTextField mfHausnummer;
	private JTextField mfPLZ;
	private JTextField mfStadt;
	private JTextField mfTelefon;
	private JTextField mfTelefax;
	private JTextField mfEMail;
	private JTextField mfGeburtstag;
	private JTextField mfKontonummer;
	private JTextField mfHonorarTermin;
	private JTextField mfHonorarAdresse;
	private JTextField mfHonorarPauschale;
	private JCheckBox mfAktiv;
	private JTextField mfHandy;
	private JTextField mfBLZ;
	private Marktforscher loadedMF;
	private JLabel mfActiveContacts;

	public MFManagementPanel(IMainWindow parentWindow) {
		super();
		this.parentWindow = parentWindow;
	}

	public JPanel buildMFMPanel() {
		JPanel dataPanel = new JPanel(new BorderLayout());

		// show data
		GridLayout gridLayout = new GridLayout(0, 2);
		gridLayout.setVgap(2);
		gridLayout.setHgap(2);
		JPanel mfmPane = new JPanel(gridLayout);
		mfmPane.add(new JLabel("Marktforscher:"));
		chooseMafoCombobox = new JComboBox();
		chooseMafoCombobox.setModel(new DefaultComboBoxModel(DBTools
				.completeMafoListWithNew()));
		chooseMafoCombobox.addActionListener(this);
		chooseMafoCombobox.setActionCommand("mafochoose");
		mfmPane.add(chooseMafoCombobox);

		mfmPane.add(new JLabel("Anrede:"));
		mfAnrede = new JTextField();
		mfmPane.add(mfAnrede);

		mfmPane.add(new JLabel("Vorname:"));
		mfVorname = new JTextField();
		mfmPane.add(mfVorname);

		mfmPane.add(new JLabel("Nachname:"));
		mfNachame = new JTextField();
		mfmPane.add(mfNachame);

		mfmPane.add(new JLabel("Gruppe:"));
		mfGruppe = new JTextField();
		mfmPane.add(mfGruppe);

		mfmPane.add(new JLabel("Straﬂe:"));
		mfStrasse = new JTextField();
		mfmPane.add(mfStrasse);

		mfmPane.add(new JLabel("Hausnummer:"));
		mfHausnummer = new JTextField();
		mfmPane.add(mfHausnummer);

		mfmPane.add(new JLabel("PLZ:"));
		mfPLZ = new JTextField();
		mfmPane.add(mfPLZ);

		mfmPane.add(new JLabel("Stadt:"));
		mfStadt = new JTextField();
		mfmPane.add(mfStadt);

		mfmPane.add(new JLabel("Telefon:"));
		mfTelefon = new JTextField();
		mfmPane.add(mfTelefon);

		mfmPane.add(new JLabel("Handy:"));
		mfHandy = new JTextField();
		mfmPane.add(mfHandy);

		mfmPane.add(new JLabel("Telefax:"));
		mfTelefax = new JTextField();
		mfmPane.add(mfTelefax);

		mfmPane.add(new JLabel("EMail:"));
		mfEMail = new JTextField();
		mfmPane.add(mfEMail);

		mfmPane.add(new JLabel("Geburtsdatum (tt.mm.yyyy):"));
		mfGeburtstag = new JTextField();
		mfmPane.add(mfGeburtstag);

		mfmPane.add(new JLabel("Kontonummer:"));
		mfKontonummer = new JTextField();
		mfmPane.add(mfKontonummer);

		mfmPane.add(new JLabel("BLZ:"));
		mfBLZ = new JTextField();
		mfmPane.add(mfBLZ);

		mfmPane.add(new JLabel("Termin-Honorar:"));
		mfHonorarTermin = new JTextField();
		mfmPane.add(mfHonorarTermin);

		mfmPane.add(new JLabel("Telefonat-Honorar:"));
		mfHonorarAdresse = new JTextField();
		mfmPane.add(mfHonorarAdresse);

		mfmPane.add(new JLabel("Telefonpauschale-Honorar:"));
		mfHonorarPauschale = new JTextField();
		mfmPane.add(mfHonorarPauschale);

		mfmPane.add(new JLabel("Aktiv:"));
		mfAktiv = new JCheckBox();
		mfmPane.add(mfAktiv);

		mfmPane.add(new JLabel("Zugewiesene Kontakte:"));
		mfActiveContacts = new JLabel();
		mfmPane.add(mfActiveContacts);

		dataPanel.add(mfmPane, BorderLayout.CENTER);
		JButton save = new JButton("Speichern");
		save.addActionListener(this);
		save.setActionCommand("save");
		dataPanel.add(save, BorderLayout.SOUTH);

		// load mf
		this.loadSelectedMF();
		return dataPanel;
	}

	private void loadMF(Marktforscher mf) {
		this.mfAktiv.setSelected(mf.getAktiv());
		this.mfAnrede.setText(mf.getAnrede());
		this.mfVorname.setText(mf.getVorName());
		this.mfNachame.setText(mf.getNachName());
		this.mfGruppe.setText(mf.getGruppe());
		this.mfStrasse.setText(mf.getStrasse());
		this.mfHausnummer.setText(mf.getHausnummer());
		this.mfPLZ.setText(mf.getPlz());
		this.mfStadt.setText(mf.getStadt());
		this.mfTelefon.setText(mf.getTelefon());
		this.mfHandy.setText(mf.getHandy());
		this.mfTelefax.setText(mf.getTelefax());
		this.mfEMail.setText(mf.getEmail());
		Date dob = mf.getGeburtstag();
		if (dob != null) {
			String dateStr = "";
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			dateStr = formatter.format(dob);
			this.mfGeburtstag.setText(dateStr);
			// Calendar cal = Calendar.getInstance();
			// cal.setTime(dob);
			// this.mfGeburtstag.setText(cal.get(Calendar.DAY_OF_MONTH) + "."
			// + (cal.get(Calendar.MONTH) + 1) + "."
			// + cal.get(Calendar.YEAR));
		} else {
			this.mfGeburtstag.setText("01.01.1900");
		}
		this.mfKontonummer.setText(mf.getKontonummer());
		this.mfBLZ.setText(mf.getBlz());
		this.mfHonorarTermin.setText(Double.toString(mf.getHonorarTermin()));
		this.mfHonorarAdresse.setText(Double.toString(mf.getHonorarAdresse()));
		this.mfHonorarPauschale.setText(Double.toString(mf
				.getHonorarPauschale()));
		this.mfActiveContacts.setText(Integer.toString(mf
				.getWaitingContactsCount()));
		this.loadedMF = mf;
	}

	private void saveMF() {
		this.parentWindow.setWaitCursor();
		this.loadedMF.setAktiv(this.mfAktiv.isSelected());
		this.loadedMF.setVorName(this.mfVorname.getText());
		this.loadedMF.setNachName(this.mfNachame.getText());
		this.loadedMF.setGruppe(this.mfGruppe.getText());
		this.loadedMF.setStrasse(this.mfStrasse.getText());
		this.loadedMF.setHausnummer(this.mfHausnummer.getText());
		this.loadedMF.setPlz(this.mfPLZ.getText());
		this.loadedMF.setStadt(this.mfStadt.getText());
		this.loadedMF.setTelefon(this.mfTelefon.getText());
		this.loadedMF.setHandy(this.mfHandy.getText());
		this.loadedMF.setTelefax(this.mfTelefax.getText());
		this.loadedMF.setEmail(this.mfEMail.getText());

		String dateStr = this.mfGeburtstag.getText();
		Date dob = getDobFromString(dateStr);
		this.loadedMF.setGeburtstag(dob);

		this.loadedMF.setKontonummer(this.mfKontonummer.getText());
		this.loadedMF.setBlz(this.mfBLZ.getText());
		this.loadedMF.setHonorarTermin(Double.parseDouble(this.mfHonorarTermin
				.getText().replaceAll(",", ".")));
		this.loadedMF.setHonorarAdresse(Double
				.parseDouble(this.mfHonorarAdresse.getText().replaceAll(",",
						".")));
		this.loadedMF.setHonorarPauschale(Double
				.parseDouble(this.mfHonorarPauschale.getText().replaceAll(",",
						".")));
		this.loadedMF.SaveToDB();

		// if inaktiv free contacts
		if (!this.loadedMF.getAktiv()) {
			this.loadedMF.resetWaitingContacts();
		}

		// reload combobox
		chooseMafoCombobox.setModel(new DefaultComboBoxModel(DBTools
				.completeMafoListWithNew()));
		// TODO: select last mafo
		this.loadSelectedMF();
		this.parentWindow.setDefaultCursor();
		this.parentWindow.setStatusText("MF: " + this.loadedMF
				+ " gespeichert.");
	}

	private Date getDobFromString(String dateStr) {
		Date dob = null;
		if (dateStr != null && dateStr.length() == 10) {
			Calendar cal = Calendar.getInstance();
			int year = Integer.parseInt(StrTool.strToken(dateStr, 3, "."));
			int month = Integer.parseInt(StrTool.strToken(dateStr, 2, ".")) - 1;
			int day = Integer.parseInt(StrTool.strToken(dateStr, 1, "."));
			cal.set(year, month, day);
			dob = new Date(cal.getTimeInMillis());
		}
		return dob;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("mafochoose")) {
			loadSelectedMF();
		} else if (arg0.getActionCommand().equals("save")) {
			this.saveMF();
		}
	}

	private void loadSelectedMF() {
		String mfid = getSelectedMF();
		int mfidInt = Integer.parseInt(mfid);
		Marktforscher mf2load;
		if (mfidInt < 0) {
			// means new mafo
			mf2load = new Marktforscher();
		} else {
			// load existing mafo
			mf2load = new Marktforscher(mfid);
		}
		this.loadMF(mf2load);
	}

	private String getSelectedMF() {
		ListItem li = (ListItem) this.chooseMafoCombobox.getSelectedItem();
		String mfid = li.getKey0();
		return mfid;
	}

	public String getTabName() {
		return "MF-Administration";
	}

}
