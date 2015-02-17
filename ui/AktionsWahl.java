package ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import db.Database;

public class AktionsWahl extends JDialog implements ActionListener {

	private static final long serialVersionUID = 2755787653866364417L;
	private static final String[] grpNames = { "Termin", "Renovierungsbedarf", "Nicht erreicht",
			"OVT nicht interessiert" };
	private ContactPanel cp;

	public AktionsWahl(ContactPanel cp) {
		super();
		this.cp = cp;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.getContentPane().add(this.createGUI());
		this.pack();
		this.setVisible(true);
	}

	/**
	 * make a panel with a table and some aktionbuttons.
	 * 
	 * @return panel with gui-elements
	 */
	private Component createGUI() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		int grpIdx = 0;
		// collect ergebnisse
		try {
			ResultSet rs = Database.select("*", "ergebnisse", "WHERE gruppe!=99 ORDER BY gruppe, id");
			int lastGroup = -1;
			JPanel grpPanel = null;
			while (rs.next()) {
				// draw ergebnis
				if (lastGroup != rs.getInt("gruppe")) {
					if (grpPanel != null) {
						TitledBorder title = BorderFactory.createTitledBorder(grpNames[grpIdx++]);
						grpPanel.setBorder(title);
						ret.add(grpPanel);
					}
					grpPanel = new JPanel(new GridLayout(0, 2, 2, 2));
				}

				JButton a = new JButton(rs.getString("name"));
				a.addActionListener(this);
				a.setActionCommand(rs.getString("id"));
				grpPanel.add(a);
				lastGroup = rs.getInt("gruppe");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void actionPerformed(ActionEvent arg0) {
		System.out.println(arg0.getActionCommand());
		this.setVisible(false);
		this.cp.addNewAktionToList(arg0.getActionCommand());
	}
}
