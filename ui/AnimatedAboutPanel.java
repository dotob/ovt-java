package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;

import com.jgoodies.animation.Animation;
import com.jgoodies.animation.Animations;
import com.jgoodies.animation.animations.BasicTextAnimation;
import com.jgoodies.animation.components.BasicTextLabel;

public final class AnimatedAboutPanel {

	private BasicTextLabel label1;
	private JPanel myPanel;
	private String appName;

	public AnimatedAboutPanel(String appName) {
		this.appName = appName;
	}

	/**
	 * Refers to the animation that is used in this page.
	 */
	private Animation animation;

	/**
	 * Returns the intro animation.
	 * 
	 * @return the intro animation
	 */
	public Animation animation() {
		return animation;
	}

	/**
	 * Creates and configures the UI components.
	 */
	private void initComponents() {
		Font font = getAnimationFont();
		label1 = new BasicTextLabel(" ");
		label1.setFont(font);
		label1.setOpaque(false);
	}

	public JPanel build() {
		this.myPanel = new JPanel(new BorderLayout());
		initComponents();
		animation = createAnimation();

		this.myPanel.add(label1);

		return this.myPanel;
	}

	private Animation createAnimation() {
		int duration = 1500;
		Animation showAppName = BasicTextAnimation.defaultFade(label1,
				duration, this.appName, Color.darkGray);

		Animation showOVT = BasicTextAnimation.defaultFade(label1, duration,
				"a RMK software", Color.darkGray);

		Animation showDevel = BasicTextAnimation.defaultFade(label1, duration,
				"developed by", Color.darkGray);

		Animation showDotob = BasicTextAnimation.defaultFade(label1, duration,
				"dotob", Color.darkGray);

		Animation showYear = BasicTextAnimation.defaultFade(label1, duration,
				"\u00A9 2005-2009", Color.darkGray);

		int myPause = 100;

		Animation all = Animations
				.sequential(new Animation[] { Animations.pause(myPause),
						showAppName, Animations.pause(myPause), showOVT,
						Animations.pause(myPause), showDevel,
						Animations.pause(myPause), showDotob,
						Animations.pause(myPause), showYear,
						Animations.pause(myPause), });

		return all;
	}

	private Font getAnimationFont() {
		return new Font("Tahoma", Font.BOLD, 24);
	}

}