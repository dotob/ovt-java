package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.animation.Animations;
import com.jgoodies.animation.Animator;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame.ShadowBorder;

public class OVTInfoPanel extends JPanel implements ChangeListener {

	// private final static ImageIcon dotobPicture = new ImageIcon("dotob.png");
	// private final static ImageIcon ovtPicture = new ImageIcon("ovt.png");
	private String appName;
	private MyInfo infoPanel;

	public OVTInfoPanel(String appName) {
		this.appName = appName;
		this.setLayout(new BorderLayout());
		this.infoPanel = new MyInfo(this.appName);
		int eb = 15;
		CompoundBorder cbi = new CompoundBorder(
				new EmptyBorder(eb, eb, eb, eb), new ShadowBorder());
		infoPanel.setBorder(cbi);
		this.add(infoPanel);
	}

	public void stateChanged(ChangeEvent e) {
		JTabbedPane tp = (JTabbedPane) e.getSource();
		int idx = tp.getSelectedIndex();
		if (idx >= 0) {
			String nam = tp.getTitleAt(idx);
			if (nam.startsWith("Über")) {
				this.startAni();
			} else {
				this.stopAni();
			}
		}
	}

	public void startAni() {
		this.infoPanel.startAni();
	}

	public void stopAni() {
		this.infoPanel.stopAni();
	}
}

class MyInfo extends JPanel {

	private String appName;
	private Animator animator;

	public MyInfo(String appName) {
		this.appName = appName;
		AnimatedAboutPanel ip = new AnimatedAboutPanel(this.appName);
		JPanel jp = ip.build();
		this.animator = new Animator(Animations.repeat(100, ip.animation()), 40);
		this.setLayout(new BorderLayout());
		this.add(jp);
	}

	public void startAni() {
		animator.start();
	}

	public void stopAni() {
		animator.stop();
	}
}