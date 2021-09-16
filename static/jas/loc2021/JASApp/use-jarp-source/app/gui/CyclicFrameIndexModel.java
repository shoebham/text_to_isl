/*
 * CyclicFNModel.java		2008-09-27
 */
package app.gui;


import javax.swing.SpinnerNumberModel;

import player.FrameIndexScanAccess;

import app.gui.FrameNumberSpinner;


/** Cyclic frame number data model for use by a {@link FrameNumberSpinner}
 * control.  This model extends (in fact, largely supersedes) the
 * standard Swing {@code SpinnerNumberModel}.  It is bound via a
 * {@link FrameIndexScanAccess} implemention to the animation
 * scan data used by a JA avatar player component.
 */
public class CyclicFrameIndexModel extends SpinnerNumberModel {

	private static final Integer			ZERO = new Integer(0);

	private FrameIndexScanAccess			scanAccess;

	private Integer							intValue;
	private Integer							min;
	private Integer							max;

/** Constructs a new cyclic frame number model. */
	public CyclicFrameIndexModel() {

		super(ZERO, ZERO, ZERO, new Integer(1));
		this.clearAccess();
	}

/** Removes this model's current scan access, if any, and resets
 * the model's value, minimum and maximum all to zero.
 */
	public void clearAccess() {

		this.scanAccess = null;

		this.min = ZERO;
		this.max = ZERO;
		// Use setValue(), because it generates a state-changed event.
		this.setValue(ZERO);
	}

/** Binds this spinner model to the given animation scan access. */
	public void setAccess(FrameIndexScanAccess fisa) {

		this.scanAccess = fisa;
	}

/** Sets this model's value to the one given, which must be an
 * {@code Integer}.
 */
	public void setValue(Object value) {

		if (value == null || ! (value instanceof Integer)) {
			throw new IllegalArgumentException("illegal value");
		}

		if (! value.equals(this.intValue)) {
			this.intValue = (Integer) value;
			this.fireStateChanged();
		}
	}

/** Returns this model's current value as an {@code Integer}. */
	public Number getNumber() {

		return this.intValue;
	}

/** Returns this model's current value as an {@code Integer}. */
	public Object getValue() {

		return this.intValue;
	}

/** Returns the current minimum value for this spinner model
 * (as an {@code Integer}).
 * If the underlying animation scan is in single-sign mode, then the
 * the minimum is the first frame index for the current sign.
 */
	public Comparable<?> getMinimum() {

		if (this.scanAccess != null) {

			int samin = this.scanAccess.min();

			if (samin != this.min.intValue()) {
				this.min = new Integer(samin);
//				System.out.println("____  getMin()  NEW "+samin);
			}
		}

		return this.min;
	}

/** Returns the current maximum value for this spinner model
 * (as an {@code Integer}).
 * If the underlying animation scan is in single-sign mode, then the
 * the maximum is the final frame index for the current sign.
 */
	public Comparable<?> getMaximum() {

		if (this.scanAccess != null) {

			int samax = this.scanAccess.max();

			if (samax != this.max.intValue()) {
				this.max = new Integer(samax);
//				System.out.println("____  getMax()  NEW "+samax);
			}
		}

		return this.max;
	}

/** Returns the next value (as an {@code Integer}), cycling from
 * the current maximum to the current minimum if need be.
 */
	public Object getNextValue() {

		return (this.scanAccess == null ? 0 : this.scanAccess.nextValue());
	}

/** Returns the previous value (as an {@code Integer}), cycling from
 * the current minimum to the current maximum if need be.
 */
	public Object getPreviousValue() {

		return (this.scanAccess == null ? 0 : this.scanAccess.previousValue());
	}
}
