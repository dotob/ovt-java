package statistics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tools.DateInterval;
import tools.DateTool;
import tools.ListItem;
import ui.IMainWindow;

public class MainStatisticPanel extends JPanel implements ActionListener {

	private static final String name = "Statistik";
	private static JComboBox vonList;
	private static JComboBox bisList;

	private JTabbedPane tabs;
	private MFErgebnisPanel mfErgPanel;
	private MFGespraechPanel mfeGespraechPanel;
	private MDErgebnisPanel mdeErgPanel;
	private RegionErgebnisPanel regErgPanel;
	private RegionGespraechPanel regGespraechPanel;
	private WLGespraechPanel wlGespraechPanel;
	private GesamtUebersichtPanel gesamtPanel;
	private GesamtRegionPanel regGesamtPanel;
	private AuftragsPanel auftragsPanel;
	private PLZUebersichtPanel plzPanel;
	private MoreMF motiPanel;
	private IMainWindow parentWindow;

	public MainStatisticPanel(IMainWindow pw) {
		this.parentWindow = pw;
		init();
	}

	private void init() {
		this.setLayout(new BorderLayout());

		int actualAW = DateTool.actualAbrechnungsWocheID(); // just for
		// preselecting
		// actual week
		if (actualAW <= 0) {
			actualAW = 1;
		}
		Vector aws = DateTool.abrechnungsWochen();
		ListItem aaw = (ListItem) aws.get(actualAW - 1);

		// datechooser
		JPanel datePanel = new JPanel();
		// datePanel.setLayout(new GridLayout(1,0));
		JLabel vonTxt = new JLabel("Von:");
		datePanel.add(vonTxt);
		vonList = new JComboBox(aws);
		vonList.setSelectedItem(aaw);
		datePanel.add(vonList);
		JLabel bisTxt = new JLabel("Bis:");
		datePanel.add(bisTxt);
		bisList = new JComboBox(aws);
		bisList.setSelectedItem(aaw);
		datePanel.add(bisList);
		JButton goForAll = new JButton("Alle aktualisieren");
		goForAll.setActionCommand("dostatistic");
		goForAll.addActionListener(this);
		datePanel.add(goForAll);
		this.add(datePanel, BorderLayout.NORTH);

		// tabs for statistics
		this.tabs = new JTabbedPane();
		this.tabs.setTabPlacement(JTabbedPane.LEFT);
		this.add(this.tabs, BorderLayout.CENTER);

		this.gesamtPanel = new GesamtUebersichtPanel(this.parentWindow);
		this.tabs.addTab(this.gesamtPanel.getTabName(), this.gesamtPanel);

		this.mfErgPanel = new MFErgebnisPanel(this.parentWindow);
		this.tabs.addTab(this.mfErgPanel.getTabName(), this.mfErgPanel);

		this.mdeErgPanel = new MDErgebnisPanel(this.parentWindow);
		this.tabs.addTab(this.mdeErgPanel.getTabName(), this.mdeErgPanel);

		this.mfeGespraechPanel = new MFGespraechPanel(this.parentWindow);
		this.tabs.addTab(this.mfeGespraechPanel.getTabName(), this.mfeGespraechPanel);

		this.wlGespraechPanel = new WLGespraechPanel(this.parentWindow);
		this.tabs.addTab(this.wlGespraechPanel.getTabName(), this.wlGespraechPanel);

		this.regErgPanel = new RegionErgebnisPanel(this.parentWindow);
		this.tabs.addTab(this.regErgPanel.getTabName(), this.regErgPanel);

		this.regGespraechPanel = new RegionGespraechPanel(this.parentWindow);
		this.tabs.addTab(this.regGespraechPanel.getTabName(), this.regGespraechPanel);

		this.regGesamtPanel = new GesamtRegionPanel(this.parentWindow);
		this.tabs.addTab(this.regGesamtPanel.getTabName(), this.regGesamtPanel);

		this.auftragsPanel = new AuftragsPanel(this.parentWindow);
		this.tabs.addTab(this.auftragsPanel.getTabName(), this.auftragsPanel);

		this.plzPanel = new PLZUebersichtPanel(this.parentWindow);
		this.tabs.addTab(this.plzPanel.getTabName(), this.plzPanel);

		this.motiPanel = new MoreMF(this.parentWindow);
		this.tabs.addTab(this.motiPanel.getTabName(), this.motiPanel);
	}

	public String getTabName() {
		return name;
	}

	public static DateInterval getVonBis() {
		DateInterval ret = new DateInterval();
		ListItem lvon = (ListItem) vonList.getSelectedItem();
		java.sql.Date von = DateTool.abrechnungsWoche(Integer.parseInt(lvon.getKey0())).getVon();
		ret.setVon(von);
		ListItem lbis = (ListItem) bisList.getSelectedItem();
		java.sql.Date bis = DateTool.abrechnungsWoche(Integer.parseInt(lbis.getKey0())).getBis();
		ret.setBis(bis);
		return ret;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("dostatistic")) {
			int answ = JOptionPane.showConfirmDialog(this, "Möchten Sie wirklich alle Statistiken aktualisieren?");
			if (answ == JOptionPane.OK_OPTION) {
				this.gesamtPanel.doStatistics();
				this.mfErgPanel.doStatistics();
				this.mfeGespraechPanel.doStatistics();
				this.regErgPanel.doStatistics();
				this.regGespraechPanel.doStatistics();
				this.regGesamtPanel.doStatistics();
				this.auftragsPanel.doStatistics();
			}
		}
	}
}
