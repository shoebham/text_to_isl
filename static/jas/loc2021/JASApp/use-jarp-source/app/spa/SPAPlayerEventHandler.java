/*
 * SPAPlayerEventHandler.java		2008-08-29
 *
 * Factored out of main SiGMLPlayerApplet.
 */
package app.spa;


import player.JALoadingPlayerEventHandler;
import player.AnimationScan;

import app.spa.SPAEventTarget;

import static app.spa.EventId.*;


/** Loading player event handler: receives notification of SiGML loading
 * and animation events from the SiGML Player Applet's JA player component,
 * converting them into {@link SPAEvent}s which it posts (typically to
 * the SPA event dispatcher thread) via an event target supplied at
 * construction time.
 */
public class SPAPlayerEventHandler implements JALoadingPlayerEventHandler {

	private final SPAEventTarget			EVT_TARGET;

/** Constructs a new loading player event handler,  which will direct
 * the events it generates to the given target.
 */
	public SPAPlayerEventHandler(SPAEventTarget etarget) {

		this.EVT_TARGET = etarget;
	}

	//############  JALPEH Interface methods.  ############

/** Handles a loader started event, posting a corresponding event
 * to this handler's target, and checking the resulting acknowledgement.
 */
	public void loaderHasStarted() {

		this.postPlayerEvent(LOAD_FRAMES_START);
	}

/** Handles a next sign (in animation) loaded event, with the given
 * sign index and frame index limit values.
 * Posts a loaded-next-sign event to this handler's target and
 * checks the resulting acknowledgement.
 */
	public void nextSignLoaded(int s, int flimit) {

		final int[] FS_COUNTS = { flimit, s+1 };
		this.postPlayerEvent(LOADED_NEXT_SIGN, FS_COUNTS);
	}

/** Handles an animation load done event, with the given success
 * flag and sign- and frame-counts.
 * Posts a load-frames-done-bad/ok event to this handler's target and
 * checks the resulting acknowledgement.
 */
	public void loaderIsDone(boolean gotanim, int nsigns, int nframes) {

		if (!gotanim) {
			this.postPlayerEvent(LOAD_FRAMES_DONE_BAD);
		}
		else {
			final int[] FS_COUNTS = { nframes, nsigns };
			this.postPlayerEvent(LOAD_FRAMES_DONE_OK, FS_COUNTS);
		}
	}

	/** Handles a player started event.
	 */
	public void playerHasStarted() {
			// No action
	}

/** Handles a player at new frame event, with the associated scanner,
 * and frame dropped flag.
 * Posts a play-done event to this handler's target and checks the
 * resulting acknowledgement.
 */
	public void playerIsAtNewFrame(AnimationScan scan,  boolean dropped) {

		boolean newsign = scan.scanIsAtNewSign();
		EventId kind =
			newsign ?
				(dropped ?
					SKIP_FIRST_FRAME_OF_SIGN : PLAY_FIRST_FRAME_OF_SIGN) :
				(dropped ? SKIP_FRAME : PLAY_FRAME);

		if (newsign) {
			final String GLOSS = scan.sign().getGloss();
			final int[] FS_COUNTS = { scan.f(), scan.s() };
			this.postPlayerEvent(kind, GLOSS, false, FS_COUNTS);
		}
		else {
			final int[] F_COUNT = { scan.f() };
			this.postPlayerEvent(kind, F_COUNT);
		}
	}

/** Handles a player done event, with the associated scanner.
 * Posts a play-done event to this handler's target and checks the
 * resulting acknowledgement.
 */
	public void playerIsDone(AnimationScan scan) {

		final int[] F_COUNT = { scan.f() };
		this.postPlayerEvent(PLAY_DONE, F_COUNT);
	}

	//############  Event posting methods.  ############

/** Posts a player event with the given parameters to this handler's
 * target, waits for the matching acknowledgement, and logs the
 * acknowledgement message if it is not OK.
 */
	private void postPlayerEvent(
		EventId kind, String str, boolean flag, int[] counts) {

		SPAEvent evt = new SPAEvent(kind, str, flag, counts);
		SPAEventAck ack = this.EVT_TARGET.postEvent(evt);
		if (!ack.OK) {
			System.out.println(
				"####  SPA JA-LPEH event "+kind+": "+ack.MESSAGE);
		}
	}

/** Posts a player event with the given parameters to this handler's
 * target, and checks the matching acknowledgement.
 */
	private void postPlayerEvent(
		EventId kind, boolean flag, int[] counts) {

		this.postPlayerEvent(kind, null, flag, counts);
	}

/** Posts a player event with the given parameters to this handler's
 * target, and checks the matching acknowledgement.
 */
	private void postPlayerEvent(EventId kind, int[] counts) {

		this.postPlayerEvent(kind, false, counts);
	}

/** Posts a player event of the given kind, but with no other data, to
 * target, and checks the matching acknowledgement.
 */
	private void postPlayerEvent(EventId kind) {

		this.postPlayerEvent(kind, null);
	}
}
