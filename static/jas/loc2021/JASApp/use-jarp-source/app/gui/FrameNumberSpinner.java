/*
 * FrameNumberSpinner.java		2007-05-19
 */
package app.gui;


import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField;

import java.text.ParseException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import player.JAFramesPlayer;
import player.SignStatusRecord;


import app.gui.CyclicFrameIndexModel;


/** A {@code JSpinner}, equipped with a {@link CyclicFrameIndexModel}
 * which typically is obtained from a {@link JAFramesPlayer}, being
 * attached to the player's animation sequence scanner.
 * In that case, the scanner's dynamically changing frame index data
 * determines the values used by this model.
 */
public class FrameNumberSpinner extends JSpinner {

/** Data model for this spinner. */
	protected final CyclicFrameIndexModel	FRAMES_MODEL;
/** The player with whose frames this spinner is synchronized. */
	protected JAFramesPlayer				PLAYER = null;

/** Handler for sign status info updates. */
	protected final SignStatusHandler		SIGN_STATUS_HANDLER;

/** Sign status data for the associated player. */
	protected SignStatusRecord				signInfo;

/** Flag used to stifle an infinite regress when the owning app makes a
 * change to the frame number spinner's data model (which in turn feeds
 * a model-changed event back to the app).
 */
	protected transient boolean				framesModelChangeIsInternal
											= false;

/** Frame number spinner's change listener -- delegates
 * state-changed event to the spinner's model change method.
 */
	protected final ChangeListener			FRAME_NO_CHANGE_LISTENER =
	new ChangeListener() {
		public void stateChanged(ChangeEvent cevt) {
			FrameNumberSpinner.this.handleFrameModelChange();
		}
	};

/** Action for the ENTER key on this spinner's frame number field. */
	protected final ActionListener			FRAME_NO_ENTER_LISTENER =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			FrameNumberSpinner.this.doNumberEnterAction();
		}
	};

/** Constructs a new spinner, using the given sign status info update
 * handler.
 */
	public FrameNumberSpinner(SignStatusHandler sshdlr) {

		super(new CyclicFrameIndexModel());

		this.SIGN_STATUS_HANDLER = sshdlr;

		this.FRAMES_MODEL = (CyclicFrameIndexModel) super.getModel();
		this.FRAMES_MODEL.addChangeListener(FRAME_NO_CHANGE_LISTENER);

		// Get the frame number text field, then set its width, and define
		// the action to be taken when ENTER is pressed on it, or
		// when it loses focus.
		//
		// (It's not entirely clear to me why the last is necessary,
		// i.e. why the not inconsiderable JFormattedTextField
		// infrastructure does not by default produce a suitable
		// response to the ENTER key. And, given that apparently it is
		// necessary, I don't know whether or not the following is the
		// most appropriate way to achieve the desired effect.)
		JFormattedTextField nedtxtfld =
			((JSpinner.NumberEditor) this.getEditor()).getTextField();
		nedtxtfld.setColumns(4);
		nedtxtfld.addActionListener(FRAME_NO_ENTER_LISTENER);
		// The following would cause a focus lost event to be ignored
		// -- an unsatisfactory response in this case, since it allows
		// the spinner display and its frame index model to get out
		// of synch with each other.
//		nedtxtfld.setFocusLostBehavior(JFormattedTextField.PERSIST);
	}

/** Sets the frames-player for this spinner, should only be called once,
 * at set up time.
 */
	public void setPlayer(JAFramesPlayer player) {

		// This should only be done once; it would be done in the
		// constructor were it not that the player may not exist
		// at that stage.
		if (this.PLAYER == null) {
			this.PLAYER = player;
		}
	}

/** Prepares to deal with a new animation on the associated player,
 * by establishing a new sign status record for the animation.
 */
	public void startNewAnimation() {

		// Create a sign-status record for the new animation scanner.
		this.signInfo = this.PLAYER.makeSignStatusRecord();

		// Attach our frame-number model to the player's scanner.
		// (Done as late as this, since it is only now, approximately,
		// that the scanner is guaranteed to exist and to be attached
		// to the relevant animation sequence.)
		this.FRAMES_MODEL.setAccess(this.PLAYER.getFrameIndexAccess());
	}

/** Resets the model to its neutral position in which the maximum
 * frame number is zero, effectively disabling it.
 */
	public void resetModelToNeutral() {

		boolean oldintflag = this.framesModelChangeIsInternal;
		this.framesModelChangeIsInternal = true;
		//####
		this.FRAMES_MODEL.clearAccess();
		//####
		this.framesModelChangeIsInternal = oldintflag;
	}

/** Sets the model's value to the given frame number, flagging the
 * change as internal, in order to prevent the change being propagated
 * to the player in {@link #handleFrameModelChange()}.
 */
	public void internalSetValue(int frameno) {

		boolean oldintflag = this.framesModelChangeIsInternal;
		this.framesModelChangeIsInternal = true;
		//####
		this.FRAMES_MODEL.setValue(frameno);
		//####
		this.framesModelChangeIsInternal = oldintflag;
	}

/** Handler method for the "frame number model changed" event.
 * For a user generated model change this method updates the player and
 * the sign status. Both for internally and for user generated changes,
 * this method forwards a request to the appropriate handler to update
 * the frame number range.
 */
	protected void handleFrameModelChange() {

		// We get the player to show a new frame only if the event is
		// user-generated via the GUI, rather than internally generated
		// during auto-play, when the spinner is used as a frame-index
		// display mechanism.
		if (! this.framesModelChangeIsInternal) {

			// Get the frame index.
			int f = (Integer) this.FRAMES_MODEL.getValue();

			// Play this frame, trying to get the associated sign data.
			this.PLAYER.showFrame(f, this.signInfo);

			// See if we have sign data.
			if (this.signInfo != null) {

				// Extract the values from the sign-data record.
				int slimit = this.signInfo.signLimit();
				int s = this.signInfo.sign();
				String gloss = this.signInfo.gloss();

				// Update our status panel with these values.
				this.updateSignInfo(slimit, s, gloss);
			}
		}
	}

/** Handler for ENTER key applied to this spinner's frame number
 * text field: attempts to update the spinner model by committing the
 * edit, but resets the text field's value if this update fails.
 */
	protected void doNumberEnterAction() {

		try {
			this.commitEdit();
		}
		catch (ParseException px) {
			// If new value is no good, then revert to the current one.
			JFormattedTextField nedtxtfld =
				((JSpinner.NumberEditor)this.getEditor()).getTextField();
			Object okval = this.FRAMES_MODEL.getValue();
			nedtxtfld.setValue(okval);
		}
	}

/** Shows sign-related information in the status panel. */
	protected void updateSignInfo(int slimit, int s, String gloss) {

		if (this.SIGN_STATUS_HANDLER != null) {
			this.SIGN_STATUS_HANDLER.updateSignStatus(slimit, s, gloss);
		}
	}

/** Interface defining the sign status update operation. */
	public static interface SignStatusHandler {
	/** Provides notification of a sign status update, with the given
	 * sign limit index, current index and gloss name.
	 */
		void updateSignStatus(int slimit, int s, String gloss);
	}
}
