/*
 * FPSPane.java		2009-08-24
 */
package app.gui;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;

import jautil.JAOptions;
import java.awt.FlowLayout;


/** A standard swing pane containing a combo box for the animgen
 * FPS option value.
 * This component has its own action listener to handle user selection
 * of a new FPS value: this handler updates the application's JA Options
 * set, which must therefore be supplied as a construction-time argument.
 * Although the FPS value is floating point, this implementation displays
 * it as an integer if its fractional part is zero.
 */
public class FPSPane extends JPanel {

	private final JAOptions				JA_OPTIONS;
	private JComboBox					comboFPS;
	private String						currentFPS;

	public FPSPane(JAOptions jaopts) {
		this.JA_OPTIONS = jaopts;

		JLabel label = new JLabel("FPS:");

		final ActionListener FPS_LISTENER = new ActionListener() {
			public void actionPerformed(ActionEvent aevt) {
				FPSPane.this.handleFPSAction();
			}
		};

		// Current FPS value may or may not be in our standard list.
		this.currentFPS = fpsText(this.JA_OPTIONS.animgenFPS());
		final String[] FPS_VALS = { "10", "25", "40", "50", "60", "100" };

		this.comboFPS = new JComboBox(FPS_VALS);
		int h = this.comboFPS.getHeight();
		this.comboFPS.setSize(96, h);
		this.comboFPS.addActionListener(FPS_LISTENER);
		this.comboFPS.setEditable(true);
		this.comboFPS.setSelectedItem(this.currentFPS);

		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		final JComponent[] CMPNNTS = { label, comboFPS };
		for (JComponent cpt : CMPNNTS)
			this.add(cpt);
	}

/** Returns this pane's FPS combo box. */
	public JComboBox getFPSComboBox()	{ return this.comboFPS; }

/** If the given text represents an FPS value that is acceptable for
 * a JA app(let) that value is returned; in all other circumstances
 * a negative value is returned.
 */
	public static float fpsValue(String fpstxt) {

		float fps = -1;

		if (fpstxt != null) {
			try {
				float fval = Float.parseFloat(fpstxt.trim());
				if (isValidFPS(fval)) { fps = fval; }
			}
			catch (NumberFormatException nfx) { /* Catch but ignore. */ }
		}

		return fps;
	}

/** Tests whether or not the given FPS value is acceptable for a JA app(let). */
	public static boolean isValidFPS(float fpsval) {

		return (0 < fpsval && fpsval < 1000);
	}

/** Action handler for the FPS combo box -- called whenever the box's
 * selected text changes.
 */
	protected void handleFPSAction() {

		String itemtxt = (String) this.comboFPS.getSelectedItem();

		// Make sure we avoid an infinite recursion by stopping
		// as soon as we know the combo box text matches the standard
		// string for the current FPS value.
		if (!itemtxt.equals(this.currentFPS)) {

			float fpsval = fpsValue(itemtxt);

			// fpsval is junk unless it's +ve.
			if (0 < fpsval && fpsval != this.JA_OPTIONS.animgenFPS()) {
				this.JA_OPTIONS.updateAnimgenFPS(fpsval);
				this.currentFPS = fpsText(fpsval);
			}

			// Make sure the combo box text is in standard form,
			// which may well cause a recursive call, but if so
			// the recursion must terminate.
			this.comboFPS.setSelectedItem(this.currentFPS);
		}
	}

/** Returns a standard string form for the given floating point FPS
 * value, in which a redundant decimal point is eliminated.
 */
	protected static String fpsText(float fps) {

		int ifps = (int) fps;
		return (ifps == fps ? ""+ifps : ""+fps);
	}
}
