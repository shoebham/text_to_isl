/*
 * SpeedUpSlider.java		2007-05-16
 */
package app.gui;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import jautil.SpeedProvider;
import jautil.SpeedManager;


public class SpeedUpSlider extends JSlider {

/** Standard value for {@link #MIN}. */
	protected static final int				STD_MIN = -5;
/** Standard value for {@link #MAX}. */
	protected static final int				STD_MAX = +2;
/** Standard initial (log_2) value for this slider. */
	protected static final int				STD_INIT_VALUE = 0;
/** Standard value for {@link #SCALE}. */
	protected static final int				STD_SCALE = 4;

/** Minimum log_2 value for this speed-up slider. */
	public final int						MIN;
/** Maximum log_2 value for this speed-up slider. */
	public final int						MAX;
/** Scale, i.e. no. of steps per log_2 value for this speed-up slider. */
	public final int						SCALE;

/** The speed manager driven by this speed-up slider. */
	protected final SpeedManager			SPEED_CONTROL;

/** Change listener, connecting the effect a change of value in
 * this slider has on its speed control.
 */
	protected final ChangeListener			SPEED_CL =
	new ChangeListener() {
		public void stateChanged(ChangeEvent cevt) {
			// Get the value from the slider, cancel the effect of
			// the scale factor, take the resulting power of 2 and set
			// the speed control accordingly.
			SpeedUpSlider susthis = SpeedUpSlider.this;
			double logspeed = susthis.getValue() / (double) SCALE;
			float speed = (float) Math.pow(2.0, logspeed);
			susthis.SPEED_CONTROL.setSpeedUp(speed);
		}
	};

/** Constructs a new speed-up slider, with horizontal orientation and
 * the standard values,
 * ({@link #STD_MIN}, {@link #STD_MAX}, {@link #STD_INIT_VALUE} and
 * {@link #STD_SCALE}) for its remaining parameters.
 */
	public SpeedUpSlider() {

		this(JSlider.HORIZONTAL);
	}

/** Constructs a new speed-up slider, with the given orientation and
 * the standard values,
 * ({@link #STD_MIN}, {@link #STD_MAX}, {@link #STD_INIT_VALUE} and
 * {@link #STD_SCALE}) for its remaining parameters.
 */
	public SpeedUpSlider(final int ORIENTATATION) {

		this(ORIENTATATION, STD_MIN, STD_MAX, STD_INIT_VALUE, STD_SCALE);
	}

/** Constructs a new speed-up slider, with the given orientation, scale,
 * minimum, maximum and initial values.
 */
	public SpeedUpSlider(
		final int ORIENTATION,
		final int MIN, final int MAX, final int INIT_VALUE,
		final int SCALE) {

		super(ORIENTATION, MIN*SCALE, MAX*SCALE, INIT_VALUE*SCALE);

		this.MIN = MIN;
		this.MAX = MAX;
		this.SCALE = SCALE;

		// Create our speed-control.
		this.SPEED_CONTROL = new SpeedManager();

		Dictionary<Integer,JComponent> labeltable =
			new Hashtable<Integer,JComponent>();
		for (int i=MIN; i<=MAX; ++i)
			labeltable.put(i*SCALE, label(i, ORIENTATION));

		// Set the slider's properties.
		this.setMinorTickSpacing(1);
		this.setMajorTickSpacing(SCALE);
		this.setLabelTable(labeltable);
		this.setPaintLabels(true);
		this.setPaintTicks(true);
		this.setSnapToTicks(false);	// (?)

		// Connect the slider to the speed-control.
		this.addChangeListener(SPEED_CL);
	}

/** Returns this speed-up slider's speed control. */
	public SpeedProvider getSpeedControl()	{ return this.SPEED_CONTROL; }

/** Returns the label for the given log_2 value and slider orientation.
 * To avoid undue clutter, when the orientation is horizontal
 * a negative even log_2 value is given a blank label.
 */
	private static JLabel label(final int val, final int ORIENTATION) {

		String ltext = "";

		int nnval = (val < 0 ? -val : val);

		// Label is blank when the slider is horizontal and the
		// log_2 value is both negative and even.
		if (ORIENTATION != JSlider.HORIZONTAL
		|| 0 <= val
		|| nnval % 2 != 0) {

			// The label is non-blank: set exp := 2 ^ nnval;
			int exp = 1;
			for (int i=0; i!=nnval; ++i)
				exp *= 2;
			ltext = (val<0 ? "1/" : "  ") + exp;
		}

		return new JLabel(ltext);
	}
}
