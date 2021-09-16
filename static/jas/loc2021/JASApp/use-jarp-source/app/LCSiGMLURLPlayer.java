/*
 * LCSiGMLURLPlayer.java		2007-02-26
 */
package app;


import java.applet.Applet;

import java.util.Date;

import java.awt.Component;
import java.awt.BorderLayout;

import netscape.javascript.JSObject;

import java.net.URL;

import jautil.JAEnv;
import jautil.JAIO;
import jautil.JAAvatarsEnv;
import jautil.JAOptions;
import jautil.SpeedManager;
import jautil.AppletPropertiesSetter;

import player.AvatarEventHandler;
import player.JALoadingPlayer;
import player.JALoadingPlayerEventHandler;
import player.JACanvasEmbedder;
import player.AnimationScan;

import static player.JAFramesPlayer.SZ_CAM_DATA;

import app.util.OutermostAppletFinder;


/** An LCSiGMLURLPlayer is a simple signed animation player applet
 * which communicates with enclosing HTML/javascript using the
 * (Netscape/Mozilla-defined) <em>LiveConnect</em> protocol.
 * <p>
 * This class assumes that the following javascript event-handler
 * method calls are supported:
 * <p>
 * <pre>
 *     playerIsReadyForAvatar([String] defaultAvatar); // CURRENTLY DISABLED
 *     playerLoadAvatarIsDone([String] avatar);
 *     playerLoadFramesHasStarted();
 *     playerLoadFramesProgress([int] nSigns, [int] nFrames);
 *     playerLoadFramesIsDone([boolean] framesOK, [int] nSigns, [int] nFrames);
 *     playerIsAtFrameAndSign(
 *         [int] frameIndex, [int] signIndex, [String] gloss);
 *     playerIsAtFrame([int] frameIndex);
 *     playerIsDroppingFrame([int] frameIndex);
 *     playerAnimationIsDone([int] frameCount);
 * </pre>
 *
 * <p>
 * The {@code playerIsReadyForAvatar()} callout is currently disabled,
 * since the avatar-load sequence it is intended to trigger is not
 * supported by the Windows IE/LiveConnect implementation.
 * (It works fine with Safari on Mac OS X, and with Firefox on both
 * Mac OS X and Windows.)
 */
public class LCSiGMLURLPlayer extends Applet {


	private static final String	LCSUP_PREFIX =
									"####  LC-SiGML-URL-Player";

	private static final String		JS_PLAYER_IS_READY_FOR_AVATAR =
									"playerIsReadyForAvatar";
	private static final String		JS_PLAYER_LOAD_AVATAR_IS_DONE =
									"playerLoadAvatarIsDone";
	private static final String		JS_PLAYER_LOAD_FRAMES_HAS_STARTED =
									"playerLoadFramesHasStarted";
	private static final String		JS_PLAYER_LOAD_FRAMES_PROGRESS =
									"playerLoadFramesProgress";
	private static final String		JS_PLAYER_LOAD_FRAMES_IS_DONE =
									"playerLoadFramesIsDone";
	private static final String		JS_PLAYER_IS_AT_FRAME_AND_SIGN =
									"playerIsAtFrameAndSign";
	private static final String		JS_PLAYER_IS_AT_FRAME =
									"playerIsAtFrame";
	private static final String		JS_PLAYER_IS_DROPPING_FRAME =
									"playerIsDroppingFrame";
	private static final String		JS_PLAYER_ANIMATION_IS_DONE =
									"playerAnimationIsDone";

	private static final String	STOP_PSEUDO_URL = "STOP";

/** HTML object for callouts. */
	private JSObject				htmlWindow;

/** Options setttings. */
	private JAOptions				JA_OPTS;
/** Avatars environment. */
	private JAAvatarsEnv			AVATARS_ENV;
/** Animation loader/player. */
	private JALoadingPlayer			player;
/** Allows dynamic variation of animation speed. */
	private SpeedManager			speedControl;


/** Constructs a new instance of this applet. */
	public LCSiGMLURLPlayer() {
		super();
	}

/** Initialises this player applet. */
	public synchronized void init() {

		String jrevn = System.getProperty("java.version");
		System.out.println((new Date())+"   Java version "+jrevn);
		System.out.println(LCSUP_PREFIX+" Applet  ####");

		// Copy appropriate HTML parameter settings to the system properties.
		AppletPropertiesSetter.copyStdAppletProperties(this);

		// Establish the options set for this invocation of the applet.
		String optspath = this.getParameter("options");
		String prefs = "LCSiGMLURLPlayer";
		// -update:  make changes stick for future sessions.
		String[] args = {"-update", optspath };
		if (optspath == null)
			args = null;
		JAEnv jaenv =  JAEnv.makeAppletJAEnv(this.getCodeBase());
		this.JA_OPTS = JAOptions.makeJAOptions(prefs, args, this, jaenv);

		// Retrieve the avatars environment.
		this.AVATARS_ENV = this.JA_OPTS.getAvatarsEnv();

		// Javascript access -- more complicated in the context
		// of the JNLPAppletLauncher.
		Applet outerapplet = OutermostAppletFinder.getOutermost(this);
		this.htmlWindow =
			outerapplet==null ? null : JSObject.getWindow(outerapplet);

		// We really expect getWindow() not to fail when outerapplet is
		// non-null, but check and report if that does happen.
		if (outerapplet != null && this.htmlWindow == null) {
			System.out.println(LCSUP_PREFIX+": JSObject.getWindow() fails.");
		}

		// Create speed control.
		this.speedControl = new SpeedManager();

		// The embedder will install the avatar canvas in the CENTER
		// of the border-layout.
		this.setLayout(new BorderLayout());
		this.player =
			new JALoadingPlayer(
				this.JA_OPTS, this.JA_CANAVAS_EMBEDDER,
				this.AVATAR_EVENT_HANDLER, this.PLAYER_EVENT_HANDLER,
				this.speedControl);

		this.player.createStandardCameraChangeMonitor();

		System.out.println(LCSUP_PREFIX+": GUI created.");

		// Leave avatar-loading to start(), below -- see the doc-
		// comment there for explanation.
	}

/** Flag: is applet being started for the first time? */
	protected boolean						isFirstStart = true;

/** Applet start method: creates a SiGMLInLib instance and loads
 * the avatar; note that this method is {@code unsynchronized}.
 */
	public void start() {

		/* On past experience (not sure if it is still valid) ...
		 * For LiveConnect it is important that we do these things
		 * here and not in {@link #init()}: otherwise, the applet
		 * becomes invisible on any load after the first.
		 */

		if (this.isFirstStart) {

			System.out.println(LCSUP_PREFIX+": first start().");

			this.isFirstStart = false;

			// Determine the initial avatar and load it.
			final String AVATAR = this.AVATARS_ENV.currentAvatar();
			this.player.requestSwitchAvatar(AVATAR);

			// This needs to be here, even though we have no avatar yet.
			this.runPlayThread();
		}

		// NB
		// The following is disabled, since the
		// Windows IE/LiveConnect implementation does not support it
		// -- more precisely, it does not support the following
		// Javascript callin which it is intended to evoke.
		// Instead, the initial avatar is loaded automatically as
		// determined by the options settings.

		// Tell the host HTML page that we are now ready for it
		// to choose an avatar, and what the default choice is.
//		final String AVATAR = this.AVATARS_ENV.currentAvatar();
//		final Object[] DEFAULT_AVATAR = { AVATAR };
//		this.doJSCall(JS_PLAYER_IS_READY_FOR_AVATAR, DEFAULT_AVATAR);
	}

	/* Applet.stop() and Applet.destroy() are not needed for this
	 * LiveConnect applet: termination is handled by our terminate()
	 * method.  That method is invoked from the HTML page's "onunload"
	 * javascript handler (before stop() and destroy() are called).
	 */

/** Flag: false until the first avatar is successfully loaded.  */
	private boolean					firstAvatarIsSet = false;

/** Used by the player thread to wait until an avatar is loaded. */
	protected synchronized void waitForFirstAvatar() {

		try {
			while (!this.firstAvatarIsSet) {
				this.wait();
			}
		}
		catch (InterruptedException ix) {
			System.out.println(
				"waitForFirstAvatar() interrupted: "+ix.getMessage());
		}
	}

/** Handles successful avatar-loaded callback from player. */
	protected synchronized void avatarIsLoaded() {

		System.out.println(
			LCSUP_PREFIX+": avatar is loaded.");

		if (!this.firstAvatarIsSet) {
			this.firstAvatarIsSet = true;
			this.notify();
		}
	}

/** Handles unsuccessful avatar-loaded callback from player. */
	protected synchronized void avatarLoadFailed() {

		System.out.println(
			LCSUP_PREFIX+": failed to load "+
			this.AVATARS_ENV.currentAvatar()+".");
	}

/** Allows HTML/Javascript to request a particular avatar. */
	public void setAvatar(String avatar) {

		this.AVATARS_ENV.setAvatar(avatar.trim());
		final String AVATAR = this.AVATARS_ENV.currentAvatar();

		if (! this.player.playerIsActive()) {
			this.player.requestSwitchAvatar(AVATAR);
		}

//		// The first time we specify the avatar to the player we
//		// need to use a different player method.
//		if (! this.firstAvatarIsSet) {
//			this.player.completePlayerSetUp(AVATAR);
//		}
//		else {
//			if (! this.player.playerIsActive()) {
//				this.player.requestSwitchAvatar(AVATAR);
//			}
//		}
	}

/** Allows HTML/Javascript to change the speed at which the current
 * animation is played, as the player is running.
 */
	public void setSpeed(String speedupstr) {

		try {
			float speedup = Float.parseFloat(speedupstr.trim());
			this.speedControl.setSpeedUp(speedup);
		}
		catch (NumberFormatException nfx) {
			System.out.println(
				"Bad speed value format \""+speedupstr+"\": "+
				nfx.getMessage());
		}
	}

	/*############  Input from HTML/Javascript.  ############*/

/** Accepts a new SiGML string to be played by this player. */
	public synchronized void playSiGMLURL(String url) {

		this.putURL(url.trim());
	}

/** Accepts a stop-player request. */
	public synchronized void stopPlayingSiGML() {

		this.player.stopPlaying();
	}

/** stop()is a no-op for this applet. */
	public synchronized void stop() {
	}

/** destroy()is a no-op for this applet. */
	public synchronized void destroy() {
	}

/** Terminates this player; in particular, updates its camera data
 *  and terminates its play-request servicing thread.
 */
	public synchronized void terminate() {

		try {
			System.out.println(LCSUP_PREFIX+":  terminate().");
			// Stop the player, but don't kill its canvas.
			this.player.stopPlaying();
			this.AVATARS_ENV.terminate();
			// Really wipe out the player.
			this.player.terminate();
			this.stopPlayerThread();
			//########
//			System.out.println(LCSUP_PREFIX+": final delay starts ...");
			Thread.sleep(200);
			//########
			System.out.println(LCSUP_PREFIX+": terminate() done.");
		}
		catch (InterruptedException ix) {
			System.out.println(
				LCSUP_PREFIX+": shut-down interrupted: "+ix);
		}
		catch (Exception x) {
			System.out.println(LCSUP_PREFIX+": terminate() ...");
			x.printStackTrace(System.out);
		}
	}

/** Stops the SiGML-player thread, if it's not already stopped. */
	protected synchronized void stopPlayerThread() {

		if (this.playerThread != null) {
			this.playerThreadStopped = true;
			this.putURL(STOP_PSEUDO_URL);
		}
	}

	/*############  Player thread control.  ############*/

/** Flag: have we had a stop request for the SiGML-player thread? */
	protected boolean					playerThreadStopped;
/** The SiGML-player thread,*/
	protected Thread					playerThread;

	protected synchronized boolean playerThreadIsStopped() {

		return this.playerThreadStopped;
	}

/** Creates and starts the SiGML URL player thread, which is driven
 * by a series of requests from HTML/Javascript.
 */
	protected void runPlayThread() {

		this.playerThreadStopped = false;
		this.playerThread =
			new Thread() {
				public void run() {
					try {
						LCSiGMLURLPlayer.this.playerLoop();
					}
					catch (InterruptedException ix) {
						System.out.println(
							LCSUP_PREFIX+": play thread interrupted: "+ix);
					}
				}
			};

		this.playerThread.start();
	}

/** The method defining the behaviour of the SiGML URL player
 * thread: blocks until an avatar is successfully loaded, then
 * process successive SiGML URLs as they come from the
 * HTML/Javascript.
 */
	protected void playerLoop() throws InterruptedException {

		// No point in starting until we have an avatar.
		this.waitForFirstAvatar();
		System.out.println(
			LCSUP_PREFIX+": playLoop() is ready for SiGML.");

		// Block until SiGML string is available;
		String url = this.getURL();

		// (Quasi-) eternal loop.
		while (! this.playerThreadIsStopped()
		&&  url != null
		&&  ! url.equals(STOP_PSEUDO_URL)) {

			// Have the player play the SiGML URL.
			this.playURL(url);

			// Block until the next SiGML string is available, or
			// until interrupted by shut-down.
			url = this.getURL();
		}

		// Proclaim our death, externally.
		System.out.println(LCSUP_PREFIX+": player thread is done.");

		// Proclaim it, internally.
		this.playerThread = null;
	}

/** Tries to play the given SiGMl URL. */
	protected void playURL(String url) throws InterruptedException {

		// The URL should not be null, but play safe anyway.
		if (url == null) {
			System.out.println(LCSUP_PREFIX+": URL is null.");
		}
		else {
			// Arguably, it would be better to use the default SiGML
			// base URL as the base against which to resolve: then
			// specifying ./ as the base would achieve the same effect
			// as what we have below, i.e. the effect of using the
			// applet's codebase as the base.
			//final String BASE = this.JA_OPTS.defaultSiGMLBaseURL();
			final URL BASE = this.JA_OPTS.getJAEnv().getAppBaseURL();
			final URL S_URL = JAIO.resolveURL(BASE, url);

			if (S_URL == null) {
				System.out.println(LCSUP_PREFIX+": Invalid URL: "+url);
			}
			else {
				this.player.playSiGMLURL(S_URL);
			}
		}
	}

	/*############  Synchronized SiGML Buffering  ############*/

/** Flag: do we have a SiGML URL from HTML/Javascript. */
	protected boolean					urlIsAvailable = false;
/** Single-slot buffer for the SiGML URL most recently received
 * from HTML/Javascript.
 */
	protected String					urlBuffer;

/** Extracts the next SiGML URL from the buffer when it becomes
 * available -- this will be the {@link #STOP_PSEUDO_URL} when
 * the player thread is shut down.
 */
	protected synchronized String getURL() {

		String url = null;
		// Wait for the trigger.
		try {
			while (! this.urlIsAvailable)
				this.wait();

			// Get the URL string.
			url = this.urlBuffer;

			// Signal that the URL string has been received.
			this.urlIsAvailable = false;
			this.notify();
		}
		catch (InterruptedException ix) {
			System.out.println(LCSUP_PREFIX+".getURL(): "+ix);
		}

		// Return the URL string (may be the STOP pseudo-URL at
		// shut-down).
		return url;
	}

/** Enters the given SiGML URL into the buffer, waiting if need be
 * until its predecessor is removed from the buffer.
 */
	protected synchronized void putURL(String url) {

		// Wait until we know that the last URL string, if any, has
		// been received.
		try {
			while (this.urlIsAvailable)
				this.wait();
		}
		catch (InterruptedException ix) {
			System.out.println(LCSUP_PREFIX+".putURL(): "+ix);
		}

		// Save the URL string reference.
		this.urlBuffer = url;

		// Signal to the player thread that the URL string is available.
		this.urlIsAvailable = true;
		this.notify();

	}

	/*############  Output to HTML/Javascript.  ############*/

/** Calls out to the given Javascript function with
 * the given arguments.
 */
	protected void doJSCall(String func, Object[] args) {
		// Guard the call with a check that the JS environment is
		// accessible -- gives us some chance of soldiering on even
		// if JS environment is not accessible.
		if (this.htmlWindow != null)
			this.htmlWindow.call(func, args);
	}

/** Loading player event handler: receives notification of SiGML loading
 * and animation events from the JA player component.
 */
	protected final JALoadingPlayerEventHandler	PLAYER_EVENT_HANDLER =
	new JALoadingPlayerEventHandler() {
		// 2007-08
		// Currently, we assume that the player's loader is NOT streamed.
		// 2008-06
		// Now we allow for both streamed and non-streamed.
		public void loaderHasStarted() {
			final Object[] EMPTY_DATA = { };
			LCSiGMLURLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_FRAMES_HAS_STARTED, EMPTY_DATA);
		}
		public void nextSignLoaded(int s, int flimit) {
			final Object[] SF_COUNTS = { s+1, flimit };
			LCSiGMLURLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_FRAMES_PROGRESS, SF_COUNTS);
		}
		public void loaderIsDone(boolean gotanim, int nsigns, int nframes) {
			final Object[] ANIM_STATUS = { gotanim, nsigns, nframes };
			LCSiGMLURLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_FRAMES_IS_DONE, ANIM_STATUS);
		}
		public void playerHasStarted() {
			// No action
		}
		public void playerIsAtNewFrame(
			AnimationScan scan,  boolean dropped) {
			if (scan.scanIsAtNewSign()) {
				final Object[] F_S_GLOSS = {
					new Integer(scan.f()),
					new Integer(scan.s()),
					scan.sign().getGloss()
				};
				LCSiGMLURLPlayer.this.doJSCall(
					JS_PLAYER_IS_AT_FRAME_AND_SIGN, F_S_GLOSS);
			}
			else {
				final Integer[] I_FRAME = { scan.f() };
				final String JS_FUNC =
					(! dropped ?
						JS_PLAYER_IS_AT_FRAME :
						JS_PLAYER_IS_DROPPING_FRAME);
				LCSiGMLURLPlayer.this.doJSCall(JS_FUNC, I_FRAME);
			}
		}
		public void playerIsDone(AnimationScan scan) {
			final Integer[] I_FRAME = { scan.f() };
			LCSiGMLURLPlayer.this.doJSCall(
				JS_PLAYER_ANIMATION_IS_DONE, I_FRAME);
		}
	};

/** Avatar event handler: receives notification of avatar load and
 * unload events.
 */
	protected final AvatarEventHandler	AVATAR_EVENT_HANDLER =
	new AvatarEventHandler() {
		public void avatarIsLoaded(String avatar) {
			boolean loadok = (avatar != null);
			if (loadok) {
				LCSiGMLURLPlayer.this.avatarIsLoaded();
			}
			else {
				LCSiGMLURLPlayer.this.avatarLoadFailed();
			}
			// NB  LiveConnect translates a null argument into an
			// empty argument list on the JS side, so pass an
			// explicit string "[NONE]" in this case.
			final Object[] AVATAR = { (loadok ? avatar : "[NONE]") };
			LCSiGMLURLPlayer.this.doJSCall(
				JS_PLAYER_LOAD_AVATAR_IS_DONE, AVATAR);
		}
		public void avatarIsUnloaded(String avatar) {
			// no-op currently, but we could do another callout;
		}
	};

/** Embedder for JA canvas: provides the JA player with a method that
 * adds the avatar canvas to this applet's panel.
 */
	protected final JACanvasEmbedder	JA_CANAVAS_EMBEDDER =
	new JACanvasEmbedder() {
		public void embedInContainer(Component jarpcanvas) {
			LCSiGMLURLPlayer.this.add(jarpcanvas, BorderLayout.CENTER);
		}
	};
}
