/*
 * LCSiGMLPlayer.java
 *
 * 2005-12-15
 */
package app;


import java.applet.Applet;

import java.util.Date;

import java.awt.Component;
import java.awt.BorderLayout;

import netscape.javascript.JSObject;

import jautil.JAOptions;
import jautil.JAEnv;
import jautil.AppletPropertiesSetter;

import player.JALoadingPlayer;
import player.JALoadingPlayerEventHandler;
import player.JACanvasEmbedder;
import player.AnimationScan;

import static player.JAFramesPlayer.SZ_CAM_DATA;

import app.util.OutermostAppletFinder;


/** An LCSiGMLPlayer is a simple signed animation player applet
 * which communicates with enclosing HTML/javascript using the
 * (Netscape-defined) <em>LiveConnect</em> protocol.
 * <p>
 * This class assumes that the following javascript event-handler
 * method calls are supported:
 * <p>
 * <pre>
 *     playerLoadHasStarted()
 *     playerLoadProgress([int] nSigns, [int] nFrames);
 *     playerLoadIsDone([boolean] hasFrames, [int] nSigns, [int] nFrames)
 *     playerIsAtFrameAndSign(
 *         [int] frameIndex, [int] signIndex, [String] gloss)
 *     playerIsAtFrame([int] frameIndex)
 *     playerIsDroppingFrame([int] frameIndex)
 *     playerIsDone([int] frameCount)
 * </pre>
 */
public class LCSiGMLPlayer extends Applet {

	private static final String		JS_PLAYER_LOAD_HAS_STARTED =
									"playerLoadHasStarted";
	private static final String		JS_PLAYER_LOAD_PROGRESS =
									"playerLoadProgress";
	private static final String		JS_PLAYER_LOAD_IS_DONE =
									"playerLoadIsDone";
	private static final String		JS_PLAYER_IS_AT_FRAME_AND_SIGN =
									"playerIsAtFrameAndSign";
	private static final String		JS_PLAYER_IS_AT_FRAME =
									"playerIsAtFrame";
	private static final String		JS_PLAYER_IS_DROPPING_FRAME =
									"playerIsDroppingFrame";
	private static final String		JS_PLAYER_IS_DONE =
									"playerIsDone";

	private JSObject				htmlWindow;
	private LCCallOutHandler		callOut;

	private JAOptions				JA_OPTIONS;
	private JALoadingPlayer			player;


	/** Constructs a new instance of this applet. */
	public LCSiGMLPlayer() {
		super();
	}

	/** Initialises this player applet. */
	public synchronized void init() {

		String jrevn = System.getProperty("java.version");
		System.out.println((new Date())+"   Java version "+jrevn);
		System.out.println("####  LC-SiGML-Player Applet  ####");

		// Copy appropriate HTML parameter settings to the system properties.
		AppletPropertiesSetter.copyStdAppletProperties(this);

		// Establish the options set for this invocation of the applet.
		String optspath = this.getParameter("options");
		String prefs = "LCSiGMLPlayer";
		// -update:  make changes stick for future sessions.
		String[] args = {"-update", optspath };
		if (optspath == null)
			args = null;
		JAEnv jaenv =  JAEnv.makeAppletJAEnv(this.getCodeBase());
		this.JA_OPTIONS = JAOptions.makeJAOptions(prefs, args, this, jaenv);

		// Javascript access -- more complicated in the context
		// of the JNLPAppletLauncher.
		Applet outerapplet = OutermostAppletFinder.getOutermost(this);
		this.htmlWindow =
			outerapplet==null ? null : JSObject.getWindow(outerapplet);

		// getWindow() really shouldn't fail when outerapplet is
		// non-null, but check and report if it does fail.
		if (outerapplet != null && this.htmlWindow == null) {
			System.out.println(
				"####  LC-SiGML-Player: JSObject.getWindow() fails.");
		}

		this.callOut = new LCCallOutHandler();

		// The embedder will install the avatar canvas in the CENTER
		// of the border-layout.
		this.setLayout(new BorderLayout());
		this.player =
			new JALoadingPlayer(
				this.JA_OPTIONS, JA_EMBEDDER, null, this.callOut, null);
		this.player.createStandardCameraChangeMonitor();
		System.out.println("####  LC-SiGML-Player: GUI created.");

		// Leave avatar-loading to start(), below -- see the doc-
		// comment there for explanation.
	}

	protected boolean						isFirstStart = true;

	/** Applet start method: creates the loads a SiGMLInLib instance
	 * and the avatar; NB for LiveConnect it is important that this
	 * is done here and not in {@link #init()}: otherwise, the applet
	 * becomes invisible on any load after the first.
	 */
	public synchronized void start() {

		if (this.isFirstStart) {

			System.out.println("####  LC-SiGML-Player: first start().");

			this.isFirstStart = false;

			// Get the avatar definition options.
			String avatar = this.JA_OPTIONS.getAvatarsEnv().currentAvatar();

			// Get the player to load its initial avatar.
			this.player.requestSwitchAvatar(avatar);

			this.runPlayThread();
		}
	}

	/* Applet.stop() and Applet.destroy() are not needed for this
	 * LiveConnect applet: termination is handled by our terminate()
	 * method.  That method is invoked from the HTML page's "onunload"
	 * javascript handler (before stop() and destroy() are called).
	 */

	/*############  Input from HTML/Javascript.  ############*/

	/** Accepts a new SiGML string to be played by this player. */
	public synchronized void playSiGML(String sigml) {

		this.putSiGML(sigml);
	}

	/** Accepts a stop-player request. */
	public synchronized void stopPlayingSiGML() {

		this.player.stopPlaying();
	}

	/** Terminates this player; in particular, updates its camera data
	 *  and terminates its play-request servicing thread.
	 */
	public synchronized void terminate() {

		try {
			System.out.println("####  LC-SiGML-Player:  terminate().");
			this.player.stopPlaying();	//######## 2008-07
			this.player.terminate();
			this.stopPlayerThread();
//			System.out.println("####  LC-SiGML-Player: final delay starts ...");
			Thread.sleep(200);
			System.out.println("####  LC-SiGML-Player: terminate() done.");
		}
		catch (InterruptedException ix) {
			System.out.println(
				"####  LC-SiGML-Player: shut-down interrupted: "+ix);
		}
		catch (Exception x) {
			System.out.println("####  LC-SiGML-Player: terminate() ...");
			x.printStackTrace(System.out);
		}
	}

	/*############  Player thread control.  ############*/

	protected boolean					playerThreadStopped;
	protected Thread					playerThread;

	protected synchronized void stopPlayerThread() {

		if (this.playerThread != null) {
			this.playerThreadStopped = true;
			this.playerThread.interrupt();
		}
	}

	protected synchronized boolean playerThreadIsStopped() {

		return this.playerThreadStopped;
	}

	protected void runPlayThread() {

		this.playerThreadStopped = false;
		this.playerThread =
			new Thread() {
				public void run() {
					try {
						LCSiGMLPlayer.this.playerLoop();
					}
					catch (InterruptedException ix) {
						System.out.println(
							"####  LCSiGMLPlayer: playerLoop() interrupted: "+ix);
					}
				}
			};

		this.playerThread.start();
	}

	protected void playerLoop() throws InterruptedException {

		// Block until SiGML string is available;
		String sigml = this.getSiGML();

		// (Quasi-) eternal loop.
		// NB (2008-06)
		// getSiGML() may get interrupted directly from the browser,
		// rather than via our carefully crafted stopPlayerThread()
		// mechanism.  To counteract this, we need to include the
		// check for null sigml here.
		while (! this.playerThreadIsStopped() && sigml != null) {

			// Play the SiGML.
			this.player.playSiGML(sigml);

			// Block until the next SiGML string is available, or
			// until interrupted by shut-down.
			sigml = this.getSiGML();
		}

		System.out.println("####  LCSiGMLPlayer: player thread is done.");
		this.playerThread = null;
	}

	/*############  Synchronized SiGML Buffering  ############*/

	protected boolean					sigmlIsAvailable = false;
	protected String					sigmlBuffer;

	protected synchronized String getSiGML() {

		String sigml = null;
		// Wait for the trigger.
		try {
			while (! this.sigmlIsAvailable)
				this.wait();

			// Get the SiGML string.
			sigml = this.sigmlBuffer;

			// Signal that the SiGML string has been received.
			this.sigmlIsAvailable = false;
			this.notify();
		}
		catch (InterruptedException ix) {
			System.out.println("####  LC-SiGML-Player.getSiGML(): "+ix);
		}

		// Return the SiGML string if any (we may be interrupted).
		return sigml;
	}

	protected synchronized void putSiGML(String sigml) {

		// Wait until we know that the last SiGML string, if any, has
		// been received.
		try {
			while (this.sigmlIsAvailable)
				this.wait();
		}
		catch (InterruptedException ix) {
			System.out.println("####  LC-SiGML-Player.putSiGML(): "+ix);
		}

		// Save the SiGML string reference.
		this.sigmlBuffer = sigml;

		// Signal to the player thread that the SiGML string is available.
		this.sigmlIsAvailable = true;
		this.notify();

	}

	/*############  Output to HTML/Javascript.  ############*/

	protected void doJSCall(String func, Object[] args) {
		if (this.htmlWindow != null)
			this.htmlWindow.call(func, args);
	}

	protected class LCCallOutHandler
	implements JALoadingPlayerEventHandler {
		// 2007-08
		// Currently, we assume that the player's loader is NOT streamed.
		// 2008-06
		// Now we allow for both streamed and non-streamed.
		public void loaderHasStarted() {
			final Object[] EMPTY_DATA = { };
			LCSiGMLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_HAS_STARTED, EMPTY_DATA);
		}
		public void nextSignLoaded(int s, int flimit) {
			final Integer[] SF_COUNTS = { s+1, flimit };
			LCSiGMLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_PROGRESS, SF_COUNTS);
		}
		public void loaderIsDone(boolean gotanim, int nsigns, int nframes) {
			final Boolean ANIM_OK = gotanim;
			final Integer N_SIGNS = nsigns;
			final Integer N_FRAMES = nframes;
			final Object[] ANIM_STATUS = { ANIM_OK, N_SIGNS, N_FRAMES };
			LCSiGMLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_IS_DONE, ANIM_STATUS);
		}
		public void playerHasStarted() {
			// No action
		}
		public void playerIsAtNewFrame(AnimationScan scan,  boolean dropped) {
			if (scan.scanIsAtNewSign()) {
				final Object[] F_S_GLOSS = {
					new Integer(scan.f()),
					new Integer(scan.s()),
					scan.sign().getGloss()
				};
				LCSiGMLPlayer.this.doJSCall(
					JS_PLAYER_IS_AT_FRAME_AND_SIGN, F_S_GLOSS);
			}
			else {
				final Integer[] I_FRAME = { scan.f() };
				String func =
					! dropped ?
						JS_PLAYER_IS_AT_FRAME : JS_PLAYER_IS_DROPPING_FRAME;
				LCSiGMLPlayer.this.doJSCall(func, I_FRAME);
			}
		}
		public void playerIsDone(AnimationScan scan) {
			final Integer[] I_FRAME = { scan.f() };
			LCSiGMLPlayer.this.doJSCall("playerIsDone", I_FRAME);
		}
	}

	protected final JACanvasEmbedder	JA_EMBEDDER =
	new JACanvasEmbedder() {
		public void embedInContainer(Component jarpcanvas) {
			LCSiGMLPlayer.this.add(jarpcanvas, BorderLayout.CENTER);
		}
	};
}
