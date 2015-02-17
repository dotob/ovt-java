package adminassistent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.xnap.commons.gui.completion.AutomaticDropDownCompletionMode;
import org.xnap.commons.gui.completion.Completion;
import org.xnap.commons.gui.completion.CompletionModeMenu;
import org.xnap.commons.gui.completion.DefaultCompletionModel;
import org.xnap.commons.gui.completion.DropDownListCompletionMode;
import org.xnap.commons.gui.util.PopupListener;

import tools.DateTool;
import tools.ListItem;
import tools.MyLog;
import tools.StrTool;
import ui.IMainWindow;

import com.toedter.calendar.JDateChooser;

import db.Contact;
import db.DBTools;
import db.Database;
import db.Gespraech;
import db.Marktforscher;
import db.Projektleiter;

public class GespraechsErfassungsPanel extends JPanel implements ActionListener, DocumentListener {

	private static final long		serialVersionUID	= 3422859174745273918L;

	private final String			tabName				= "Gesprächsbericht eingeben";

	private JTextField				inputID;
	private DefaultCompletionModel	idCompletionModel;
	private JDateChooser			vdDateChooser;
	private JComboBox				mafoBox;
	private JDateChooser			mdDateChooser;
	private JComboBox				weleBox;
	private JTabbedPane				ergPanel;
	private JComboBox				noErgList;
	private JComboBox				badErgProduktList;
	private JTextField				goodErgSumme;
	private JComboBox				badErgGrundList;
	private JComboBox				goodErgProduktList;
	private JComboBox				marktdatenermittlerBox;
	private JComboBox				noErgProduktList;
	private JComboBox				vdTimeList;
	private Gespraech				existentGespraech;

	private JButton					saveButton;

	private JLabel					infoLabel;

	private IMainWindow				parentWindow;

	public GespraechsErfassungsPanel(IMainWindow pw) {
		this.parentWindow = pw;
		this.initGUI();
	}

	/**
	 * show gui for input of termin
	 */
	public void initGUI() {

		// layout stuff
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(2, 6, 2, 6);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		gc.weightx = 1;
		gc.weighty = 1;

		this.setLayout(new GridBagLayout());

		this.add(new JLabel("Suchen Sie hier den Nachnamen der Adresse:"), gc);
		// make textfield with completion
		this.inputID = new JTextField(40);
		this.inputID.setToolTipText("<html>Wenn sie hier anfangen zu schreiben werden "
				+ "Vorschläge für mögliche Adressen gemacht.<br> "
				+ "Sie können dann mit dem Curser oder der Maus der richtigen auswählen</html>");
		this.inputID.setActionCommand("inputID");
		this.inputID.addActionListener(this);
		gc.gridy++;
		this.add(this.inputID, gc);

		this.infoLabel = new JLabel();
		gc.gridy++;
		this.add(this.infoLabel, gc);

		// completion
		this.idCompletionModel = new DefaultCompletionModel(new String[] { "loading..." });
		// TODO: reactivate completion
		Completion comp = new Completion(this.inputID, this.idCompletionModel);
		comp.setMode(new DropDownListCompletionMode());
		comp.setMode(new AutomaticDropDownCompletionMode());

		CompletionModeMenu menu = new CompletionModeMenu(comp);
		this.inputID.addMouseListener(new PopupListener(menu));
		this.inputID.getDocument().addDocumentListener(this);

		gc.gridy++;
		this.add(new JLabel("M-D = Datum der Gesprächsvereinbarung:"), gc);
		this.mdDateChooser = new JDateChooser();
		gc.gridy++;
		this.add(this.mdDateChooser, gc);

		gc.gridy++;
		this.add(new JLabel("V-D = Datum der Vertragsgespräches:"), gc);
		this.vdDateChooser = new JDateChooser();
		gc.gridy++;
		this.add(this.vdDateChooser, gc);

		gc.gridy++;
		this.add(new JLabel("Uhrzeit des Vertragsgespräches:"), gc);
		this.vdTimeList = new JComboBox(DateTool.vertragsgespraechZeiten());
		gc.gridy++;
		this.add(this.vdTimeList, gc);

		// ergebnis stuff, make a tabbed pane
		gc.gridy++;
		this.add(new JLabel("Gesprächsergebnis:"), gc);
		this.ergPanel = new JTabbedPane();

		// kein ergebnis
		JPanel noerg = new JPanel(new GridLayout(0, 1));
		JLabel noergListLabel = new JLabel("Begründung:");
		noerg.add(noergListLabel);
		this.noErgList = new JComboBox(DBTools.noTerminErgebnisList(true));
		this.noErgList.addActionListener(this);
		noerg.add(this.noErgList);
		JLabel noergList2Label = new JLabel("Produkt:");
		noerg.add(noergList2Label);
		this.noErgProduktList = new JComboBox(DBTools.buildProduktList(true));
		noerg.add(this.noErgProduktList);
		this.ergPanel.addTab("kein Termin", noerg);

		// Termin ohne Abschluss
		JPanel baderg = new JPanel(new GridLayout(0, 1));
		JLabel badergList1Label = new JLabel("Begründung:");
		baderg.add(badergList1Label);
		this.badErgGrundList = new JComboBox(DBTools.terminErgebnisList(true));
		baderg.add(this.badErgGrundList);
		JLabel badergList2Label = new JLabel("Produkt:");
		baderg.add(badergList2Label);
		this.badErgProduktList = new JComboBox(DBTools.buildProduktList(true));
		baderg.add(this.badErgProduktList);
		this.ergPanel.addTab("Gespräch ohne Abschluss", baderg);

		// Termin mit Abschluss
		JPanel gooderg = new JPanel(new GridLayout(0, 1));
		JLabel goodergList1Label = new JLabel("Produkt:");
		gooderg.add(goodergList1Label);
		this.goodErgProduktList = new JComboBox(DBTools.buildProduktList(true));
		gooderg.add(this.goodErgProduktList);
		JLabel goodergList2Label = new JLabel("Vertragsbruttosumme:");
		gooderg.add(goodergList2Label);
		this.goodErgSumme = new JTextField();
		gooderg.add(this.goodErgSumme);
		this.ergPanel.addTab("Gespräch mit Abschluss", gooderg);

		gc.gridy++;
		gc.gridheight = 3;
		this.add(this.ergPanel, gc);

		gc.gridy += 3;
		gc.gridheight = 1;
		this.add(new JLabel("Wer hat die Marktdaten ermittelt:"), gc);
		this.marktdatenermittlerBox = new JComboBox(DBTools.marktdatenermittlerList());
		gc.gridy++;
		this.add(this.marktdatenermittlerBox, gc);

		gc.gridy += 3;
		gc.gridheight = 1;
		this.add(new JLabel("Wer hat den Termin vereinbart:"), gc);
		this.mafoBox = new JComboBox(DBTools.mafoMafoList(true));
		gc.gridy++;
		this.add(this.mafoBox, gc);

		gc.gridy++;
		this.add(new JLabel("Wer hat das Gespräch geführt:"), gc);
		this.weleBox = new JComboBox(DBTools.weleWeleList(true));
		gc.gridy++;
		this.add(this.weleBox, gc);

		// go buttons
		JPanel aktionButtons = new JPanel(new BorderLayout());
		this.saveButton = new JButton("Gespräch speichern");
		this.saveButton.addActionListener(this);
		this.saveButton.setActionCommand("save");
		aktionButtons.add(this.saveButton);
		gc.gridy++;
		this.add(aktionButtons, gc);
	}

	/**
	 * search for names of kunden that will be shown in the completion list
	 */
	private void loadNewCompletions() {
		this.parentWindow.setWaitCursor();
		this.showInfo(false);
		String inh = this.inputID.getText();
		if (inh.length() > 0) {
			// check if user clicked on an adress
			String id = this.extractID(inh);
			if (id.length() > 0) {
				this.contactWasChoosen(id);
			} else {
				Vector<String> foundIDs = new Vector<String>();
				String limit = "";
				if (inh.length() <= 2) {
					int maxComp = 50 + inh.length() * 15;
					limit = "LIMIT " + maxComp;
				}
				try {
					ResultSet rs;
					if (!inh.startsWith("#")) {
						rs = Database.select("id, nachname, vorname, stadt", "kunden", "WHERE nachname LIKE '" + inh
								+ "%' ORDER BY nachname " + limit);
						while (rs.next()) {
							String add = rs.getString("nachname") + ", " + rs.getString("vorname") + " ["
									+ rs.getString("id") + "] aus " + rs.getString("stadt");
							foundIDs.add(add);
						}
					} else {
						String search4 = inh.substring(1);
						rs = Database.select("id, nachname, vorname, stadt", "kunden", "WHERE id='" + search4
								+ "%' ORDER BY nachname " + limit);
						while (rs.next()) {
							String add = "#" + rs.getString("id") + "    " + rs.getString("nachname") + ", "
									+ rs.getString("vorname") + "aus " + rs.getString("stadt") + "["
									+ rs.getString("id") + "]";
							foundIDs.add(add);
						}
					}
					Database.close(rs);
				} catch (SQLException e) {
					e.printStackTrace();
					MyLog.showExceptionErrorDialog(e);
				}
				this.idCompletionModel.clear();
				this.idCompletionModel.insert(foundIDs.toArray());
			}
		}
		this.parentWindow.setDefaultCursor();
	}

	private void contactWasChoosen(String id) {
		Contact c = Contact.SearchContact(id);
		if (c != null) {
			// check if there is any aktion, if not it seems like wrong data
			if (c.getAktionenFromDB(false).size() <= 0) {
				// nothing found...bad
				this.showInfo(true);
			}
		}
	}

	private void showInfo(boolean error) {
		if (error) {
			this.infoLabel.setOpaque(true);
			this.infoLabel.setForeground(Color.WHITE);
			this.infoLabel.setBackground(Color.RED);
			this.infoLabel.setText("<html><h2>Der gewählte Kontakt hat keine Aktionen. "
					+ "Wollen Sie wirklich einen Gesprächsbericht anlegen?</h2></html>");
		} else {
			this.infoLabel.setOpaque(false);
			this.infoLabel.setForeground(Color.WHITE);
			this.infoLabel.setText("");
		}
	}

	/**
	 * fills a given gespraech into the inputs
	 * 
	 * @param g
	 */
	public void setGespraech(Gespraech g) {
		this.existentGespraech = g;
		Contact c = Contact.SearchContact(g.getKundeID());
		this.inputID.setText("Gespräch: " + g.getId() + " von " + c.getNachName() + ", " + c.getVorName() + " aus "
				+ c.getStadt());
		this.mdDateChooser.setDate(g.getDatumMF());
		this.vdDateChooser.setDate(g.getDatumWele());

		DBTools.setList2Object(this.mafoBox, Marktforscher.SearchMarktforscher(g.getMafo()));
		Projektleiter wl = Projektleiter.searchProjektleiter(g.getProjektleiter());
		DBTools.setList2Object(this.weleBox, wl);
		DBTools.setList2ListItem(this.marktdatenermittlerBox, new ListItem(g.getMde(), ""));
		this.vdTimeList.setSelectedItem(g.getTerminZeit());

		int etype = g.ergebnisType();
		if (etype == 0) {
			DBTools.setList2ListItem(this.noErgList, new ListItem(g.getErgebnis(), ""));
			DBTools.setList2ListItem(this.noErgProduktList, new ListItem(g.getProdukt(), ""));
			this.ergPanel.setSelectedIndex(0);
		} else if (etype == 1) {
			DBTools.setList2ListItem(this.badErgGrundList, new ListItem(g.getErgebnis(), ""));
			DBTools.setList2ListItem(this.badErgProduktList, new ListItem(g.getProdukt(), ""));
			this.ergPanel.setSelectedIndex(1);
		} else if (etype == 2) {
			this.goodErgSumme.setText(Integer.toString(g.getVertragsBruttoSumme()));
			DBTools.setList2ListItem(this.goodErgProduktList, new ListItem(g.getProdukt(), ""));
			this.ergPanel.setSelectedIndex(2);
		}
	}

	/**
	 * save the termin that was build through the gui
	 */
	private boolean saveNewGespraech() {
		boolean success = true;
		String cStrID = this.inputID.getText();
		if (cStrID != null && cStrID.length() > 0) {
			Contact con;
			if (this.existentGespraech == null) {
				String cID = extractID(cStrID);
				// System.out.println(cID);
				con = Contact.SearchContact(cID);
			} else {
				con = Contact.SearchContact(this.existentGespraech.getKundeID());
			}
			if (con.getId() != null) {
				Date mdDate = this.mdDateChooser.getDate();
				Date vdDate = this.vdDateChooser.getDate();
				// check ergebnis
				String erg = "";
				String produkt = "0";
				int summe = 0;
				ListItem liErg = null;
				ListItem liProd = null;
				String vdTime = (String) this.vdTimeList.getSelectedItem();
				int shownErgPanel = this.ergPanel.getSelectedIndex();
				switch (shownErgPanel) {
				case 0:
					// kein ergebnis
					liErg = (ListItem) this.noErgList.getSelectedItem();
					liProd = (ListItem) this.noErgProduktList.getSelectedItem();
					if (liErg != null) {
						erg = liErg.getKey0();
					}
					if (liProd != null) {
						produkt = liProd.getKey0();
					}
					break;
				case 1:
					// ohne abschluss
					liErg = (ListItem) this.badErgGrundList.getSelectedItem();
					liProd = (ListItem) this.badErgProduktList.getSelectedItem();
					if (liErg != null) {
						erg = liErg.getKey0();
					}
					if (liProd != null) {
						produkt = liProd.getKey0();
					}
					break;
				case 2:
					// mit abschluss
					erg = Gespraech.AUFTRAGID; // this is the terminergebnis
					// for auftrag: preis
					liProd = (ListItem) this.goodErgProduktList.getSelectedItem();
					if (liProd != null) {
						produkt = liProd.getKey0();
					}
					try {
						summe = Integer.parseInt(this.goodErgSumme.getText());
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
								"Das Feld Vertragsbruttosumme enthält keine gültige Zahl", "Hinweis",
								JOptionPane.WARNING_MESSAGE);
						this.goodErgSumme.setText("");
						success = false;
					}
					break;
				default:
					// do notting here...
					break;
				}
				// check erg
				if (erg.length() <= 0) {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Kein Ergebnis gewählt", "Hinweis",
							JOptionPane.WARNING_MESSAGE);
					success = false;
				}

				// check dates
				if (mdDate == null) {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Kein MD Datum eingegeben", "Hinweis",
							JOptionPane.WARNING_MESSAGE);
					success = false;
				}
				if (vdDate == null) {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Kein VD Datum eingegeben", "Hinweis",
							JOptionPane.WARNING_MESSAGE);
					success = false;
				}

				// check mafo
				ListItem mde = (ListItem) this.marktdatenermittlerBox.getSelectedItem();
				String mdeStr = mde.getKey0();
				// fetsch unset mde
				if (mdeStr == null || mdeStr.length() == 0) {
					mdeStr = "-1";
				}
				Marktforscher mafo = (Marktforscher) this.mafoBox.getSelectedItem();
				if (mafo != null) {
					Projektleiter wele = (Projektleiter) this.weleBox.getSelectedItem();
					success = wele != null;
					if (success) {
						if (this.existentGespraech == null) {
							Gespraech ttt = new Gespraech(con, mdDate, vdDate, vdTime, erg, produkt, summe, mdeStr,
									mafo, wele);
							ttt.saveToDB();
							ttt.printMe();
							MyLog.logDebug(ttt.toRawString());
						} else {
							Gespraech g = this.existentGespraech;
							g.setDatumMF(new java.sql.Date(mdDate.getTime()));
							g.setDatumWele(new java.sql.Date(vdDate.getTime()));
							g.setTerminZeit(vdTime);
							g.setErgebnis(erg);
							g.setProdukt(produkt);
							g.setVertragsBruttoSumme(summe);
							g.setMafo(mafo.getId());
							g.setMde(mdeStr);
							g.setProjektleiter(wele.getId());
							g.saveToDB();
							g.printMe();
						}
					} else {
						JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
								"Bitte einen Projektleiter auswählen", "Hinweis", JOptionPane.WARNING_MESSAGE);
						success = false;
					}
				} else {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Bitte einen Marktforscher auswählen",
							"Hinweis", JOptionPane.WARNING_MESSAGE);
					success = false;
				}
			} else {
				success = false;
			}
		} else {
			JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Bitte einen Kontakt auswählen", "Hinweis",
					JOptionPane.WARNING_MESSAGE);
			success = false;
		}
		return success;
	}

	private String extractID(String cStrID) {
		String ret = "";
		if (cStrID.indexOf("[") >= 0) {
			String tmp = StrTool.strTail(cStrID, -1 * (cStrID.indexOf("[") + 1));
			ret = StrTool.strHead(tmp, tmp.indexOf("]"));
		}
		return ret;
	}

	/**
	 * reset the termin input gui
	 */
	private void emptyInput() {
		this.inputID.setText("");
		this.mdDateChooser.setDate(new java.util.Date());
		this.vdDateChooser.setDate(new java.util.Date());
		this.noErgList.setSelectedIndex(0);
		this.noErgProduktList.setSelectedIndex(0);
		this.badErgGrundList.setSelectedIndex(0);
		this.badErgProduktList.setSelectedIndex(0);
		this.goodErgProduktList.setSelectedIndex(0);
		this.goodErgSumme.setText("");
		this.mafoBox.setSelectedIndex(0);
		this.weleBox.setSelectedIndex(0);
		this.vdTimeList.setSelectedIndex(0);
		this.marktdatenermittlerBox.setSelectedIndex(0);
	}

	// ================ actions =====================================
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.equals("save")) {
			if (this.saveNewGespraech()) {
				this.emptyInput();
			}
		} else if (com.equals("noerg") || com.equals("ergbad") || com.equals("erggood")) {
			// this.toggleErg(com);
			System.out.println("anyerg...");
		}
	}

	// DocumentListene
	public void changedUpdate(DocumentEvent e) {
		this.loadNewCompletions();
	}

	public void insertUpdate(DocumentEvent e) {
		this.loadNewCompletions();
	}

	public void removeUpdate(DocumentEvent e) {
		this.loadNewCompletions();
	}

	public String getTabName() {
		return tabName;
	}
}
