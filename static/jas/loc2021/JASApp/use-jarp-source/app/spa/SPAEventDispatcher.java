/*
 * SPAEventDispatcher.java		2008-09-13
 */
package app.spa;


import java.io.IOException;

import java.util.EnumSet;

import java.net.URL;

import netscape.javascript.JSObject;


import jautil.JAOptions;
import jautil.JAAvatarsEnv;
import jautil.JAIO;
import jautil.SpeedManager;

import player.AvatarEventHandler;
import player.JALoadingPlayer;
import player.JALoadingPlayerEventHandler;
import player.JACanvasEmbedder;


import app.gui.FPSPane;

import app.spa.StateFlag;
import static app.spa.StateFlag.*;
import app.spa.EDTState;

import app.util.OneShotTimeoutBarrier;
import app.util.UseJLogger;

import app.spa.SPAEvent;
import app.spa.SPAEventStreamSource;
import app.spa.EventId;

import static app.spa.EventId.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** Runs the event handler thread for a SiGML Player Applet. */
public final class SPAEventDispatcher {

/** Logger. */
	private static final Logger				logger = LogManager.getLogger();

/** Prefix for SPA messages. */
	protected static final String			SPAED_PREFIX =
											"SPA_EDT";

/** Avatar event Javascript call-out name. */
	public static final String				JS_AVATAR_EVENT =
											"spaAvatarEvent";
/** Frames generation event Javascript call-out name. */
	public static final String				JS_FRAMES_GEN_EVENT =
											"spaFramesGenEvent";
/** Animation event Javascript call-out name. */
	public static final String				JS_ANIMATION_EVENT =
											"spaAnimationEvent";


//############  SPA JA_PLAYER thread's event sets.  ############

/** Set of frames generation completion event IDs. */
	protected static final EnumSet<EventId>	DONE_FRAMES_GEN_EVENTS =
	spaEventIds(
		LOAD_FRAMES_DONE_OK, LOAD_FRAMES_DONE_BAD);

/** Set of frames generation event IDs that carry frame/sign indices. */
	protected static final EnumSet<EventId>	WITH_INDICES_FRAMES_GEN_EVENTS =
	spaEventIds(
		LOADED_NEXT_SIGN, LOAD_FRAMES_DONE_OK);

/** Set of animation event IDs indicating the start of a new sign. */
	protected static final EnumSet<EventId>	NEW_SIGN_ANIM_EVENTS =
	spaEventIds(
		PLAY_FIRST_FRAME_OF_SIGN, SKIP_FIRST_FRAME_OF_SIGN);


//############  Host Applet envrironment data.  ############

/** Options setttings. */
	private final JAOptions						JA_OPTS;
/** Avatars environment. */
	private final JAAvatarsEnv					AVATARS_ENV;

/** JA loading avatar player supporting this dispatcher. */
	private final JALoadingPlayer				JA_PLAYER;
/** Allows dynamic variation of animation speed. */
	private final SpeedManager					SPEED_CONTROL;

/** HTML object for callouts. */
	private final JSObject						HTML_WINDOW;

/** UseJLogger to be used by this dispatcher. */
	private final UseJLogger					LOGGER;

/** Owning SiGML player applet's halt event generator. */
	private final Runnable						HALT_EVENT_GENERATOR;

//############  SPA event dispatch thread thread data  ############

/** Shut down synchronisation barrier, used to signal dispatcher thread
 * completion to the {@link #terminate()} method.
 */
	private final OneShotTimeoutBarrier			SHUT_DOWN_BARRIER;
/** Event source for this dispatcher. */
	private final SPAEventStreamSource			EVENT_SOURCE;
/** Current dispatcher thread state. */
	private EDTState							state;

/** Target for piped SiGML input to player. */
	private JALoadingPlayer.SiGMLPipeWriter		sigmlPipe;

/** Animation-suspended flag. */
	private boolean								animSuspended;


/** Constructs a new SiGML Player Applet event stream processor. */
	public SPAEventDispatcher(
		JAOptions opts, JSObject htmlwin, UseJLogger lggr,
		SPAEventStreamSource evtsrc,
		JACanvasEmbedder embedder,
		AvatarEventHandler aeh, JALoadingPlayerEventHandler lpeh,
		Runnable haltevtgen, OneShotTimeoutBarrier sdbarrier) {

		this.JA_OPTS = opts;
		this.AVATARS_ENV = this.JA_OPTS.getAvatarsEnv();

		this.HTML_WINDOW = htmlwin;
		this.LOGGER = lggr;
		this.EVENT_SOURCE = evtsrc;

		this.HALT_EVENT_GENERATOR = haltevtgen;
		this.SHUT_DOWN_BARRIER = sdbarrier;

		// Create speed control.
		this.SPEED_CONTROL = new SpeedManager();

		// Create JA loading player component with the appropriate
		// handlers.
		this.JA_PLAYER =
			new JALoadingPlayer(
				this.JA_OPTS, embedder, aeh, lpeh, this.SPEED_CONTROL);

		// Attach standard camera change monitor to the JA player.
		this.JA_PLAYER.createStandardCameraChangeMonitor();

	}

//############  SPA event dispatcher thread launcher.   ############

/** Creates and launches the SPA event dispatch thread, which
 * just runs the {@link #playerEventLoop()}.
 */
	public void launchSPAEventDispatchThread() {

		// Initialise the dispatcher thread's state, attach the logger
		// to it, and list the tags for the state's base flags set on
		// the logger.
		this.state = new EDTState(this.LOGGER);
		this.LOGGER.log("EDT state flag tags:");
		StateFlag.listAllTags(this.LOGGER);

		// Create the SPA event dispatch thread.
		final Thread SPA_EDT =
			new Thread() {
				public void run() {
					Thread.currentThread().setName("SPAEventDispatcher");
					logger.log( LoggerConfig.LOGLevel, LoggerConfig.THREADMarker,
						"Run: SPA Event Dispatcher");
					try {
						SPAEventDispatcher.this.playerEventLoop();
					}
					catch (InterruptedException ix) {
						logger.log(LoggerConfig.WARNLevel, LoggerConfig.EVENTMarker,
							SPAED_PREFIX+".launchSPAEDT(): "+ix);
					}
					logger.log( LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,
						"End: SPA Event Dispatcher");
				}
			};

		// Start the SPA event dispatch thread.
		SPA_EDT.start();
	}

/*
 * rqst-load-avatar(av)
 * rqst-play-sigml-url(surl)
 * rqst-play-sigml-text(sigml)
 * stop-playing-sigml()
 * shut-down()
 * halt()
 * 
 * av-unloaded-evt
 * av-loaded-ok-evt
 * av-loaded-bad-evt
 * frames-gen-evt
 * anim-evt
 */

//############  SPA player thread event loop.  ############

/** SPA event dispatcher thread's main event loop method:
 * this processes successive events until it receives a HALT event,
 * after which it opens the shut-down barrier (for the benefit of the
 * relevant JS applet invocation.
 */
	protected void playerEventLoop() throws InterruptedException {

		boolean stopped = false;
		this.animSuspended = false;
		this.sigmlPipe = null;
		this.state.include(IDLE);
		this.state.logChange();

		while (! stopped) {

			SPAEvent evt = this.EVENT_SOURCE.getNextEvent();

			if (evt.KIND == HALT) {

				// Probably should check state is approraite
				this.state.setOnly(HALTED);
				this.state.logChange();
				stopped = true;
				this.EVENT_SOURCE.deliverOKAck();
			}
			else if (evt.KIND == SHUT_DOWN) {

				this.handleShutDownEvent();
			}
			else if (evt.KIND == LOAD_AVATAR) {

				this.handleLoadAvatarEvent(evt);
			}
			else if (evt.KIND == PLAY_SIGML_URL) {

				this.handlePlaySiGMLURLEvent(evt);
			}
			else if (evt.KIND == PLAY_SIGML_TEXT) {

				this.handlePlaySiGMLTextEvent(evt);
			}
			else if (evt.KIND == START_PLAY_SIGML_PIPED) {

				this.handleStartPlaySiGMLPipedEvent(evt);
			}
			else if (evt.KIND == APPEND_TO_SIGML_PIPE) {

				this.handleAppendToSiGMLPipeEvent(evt);
			}
			else if (evt.KIND == CLOSE_SIGML_PIPE) {

				this.handleCloseSiGMLPipeEvent(evt);
			}
			else if (evt.KIND == STOP_PLAY_SIGML) {

				this.handleStopPlayingSiGMLEvent();
			}
			else if (evt.KIND == SET_SPEED_UP) {

				this.handleSetSpeedUpEvent(evt);
			}
			else if (evt.KIND == SET_ANIMGEN_FPS) {

				this.handleSetAnimgenFPSEvent(evt);
			}
			else if (evt.KIND == SET_DO_LOG_DROPPED_FRAMES) {

				this.handleSetDoLogDroppedFrames(evt);
			}
			else if (evt.KIND == SUSPEND_IF_PLAYING) {

				this.handleSuspendIfPlayingEvent(evt);
			}
			else if (evt.KIND == RESUME_IF_PLAYING) {

				this.handleResumeIfPlayingEvent(evt);
			}
			else if (evt.KIND.isAvatarEvent()) {

				this.handleAvatarEvent(evt);
			}
			else if (evt.KIND.isFramesGenEvent()) {

				this.handleFramesGenEvent(evt);
			}
			else if (evt.KIND.isAnimationEvent()) {

				this.handleAnimationEvent(evt);
			}
			else {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.EVENTMarker,
					SPAED_PREFIX+": UNKNOWN EVENT: "+evt.KIND);
			}
			// (end event dispatcher loop body)
		}

		// Allow the JS terminate() call to complete.
		// Now called at end of doShutDown().
		// this.SHUT_DOWN_BARRIER.open();

		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker,
			SPAED_PREFIX+": Player loop done");
	}

//############  Player event handler methods  ############

/** Player thread handler for the shut-down request event. */
	protected void handleShutDownEvent() throws InterruptedException {
		
		if (this.checkStateHasNot(HALTING, "terminate")) {

			// May need more checks to ensure activity has ceased
			String sperrmsg = null;
			if (this.state.hasBoth(PLAYING_SIGML_PIPED, HAS_OPEN_SIGML_PIPE)) {
				sperrmsg = this.doCloseSiGMLPipe();
			}
			this.state.include(HALTING);
			this.state.logChange();

			this.launchShutDownThread();
			this.deliverAck("Terminate", sperrmsg);
			//this.EVENT_SOURCE.deliverOKAck();
		}
	}

/** Player thread handler for the given load-avatar request event. */
	protected void handleLoadAvatarEvent(SPAEvent evt)
	throws InterruptedException {

		if (this.checkStateHas(IDLE, "load")) {

			this.state.oneOutOneIn(IDLE, CHANGING_AVATAR);
			this.state.logChange();

			this.AVATARS_ENV.setAvatar(evt.PARAM);
			final String AVATAR = this.AVATARS_ENV.currentAvatar();
			this.JA_PLAYER.requestSwitchAvatar(AVATAR);
			this.EVENT_SOURCE.deliverOKAck();
		}
	}

/** Player thread handler for the given play-SiGML-URL request event. */
	protected void handlePlaySiGMLURLEvent(SPAEvent evt)
	throws InterruptedException {

		final String PLAY_ACTION = "play SiGML URL";

		if (this.checkStateHasBoth(IDLE, HAS_AVATAR, PLAY_ACTION)) {

			final URL BASE = this.JA_OPTS.getJAEnv().getAppBaseURL();
			final URL S_URL = JAIO.resolveURL(BASE, evt.PARAM, "####  SPAEventDispatcher:  ");
			if (S_URL == null) {
				
				this.EVENT_SOURCE.deliverAck("Invalid URL: "+evt.PARAM);
			}
			else {
				this.state.setPlayingSiGMLURL();
				this.state.logChange();

				this.JA_PLAYER.playSiGMLURL(S_URL);
				this.EVENT_SOURCE.deliverOKAck();
			}
		}
	}

/** Player thread handler for the given play-SiGML-URL request event. */
	protected void handlePlaySiGMLTextEvent(SPAEvent evt)
	throws InterruptedException {

		final String PLAY_ACTION = "play SiGML text";

		if (this.checkStateHasBoth(IDLE, HAS_AVATAR, PLAY_ACTION)) {

			this.state.setPlayingSiGMLText();
			this.state.logChange();

			this.JA_PLAYER.playSiGML(evt.PARAM);
			this.EVENT_SOURCE.deliverOKAck();
		}
	}

/** Player thread handler for the start-play-via-SiGML-pipe request event. */
	protected void handleStartPlaySiGMLPipedEvent(SPAEvent evt)
	throws InterruptedException {

		final String PLAY_ACTION = "play SiGML via pipe";

		if (this.checkStateHasBoth(IDLE, HAS_AVATAR, PLAY_ACTION)) {

			String errmsg = null;
			try {
				this.sigmlPipe = this.JA_PLAYER.playSiGMLPiped();
			}
			catch (IOException iox) { errmsg = iox.getMessage(); }
			catch (InterruptedException ix) { errmsg = ix.getMessage(); }

			if (this.sigmlPipe != null) {
				this.state.setPlayingSiGMLPiped();
				this.state.logChange();
			}

			this.deliverAck("SiGML pipe set up", errmsg);
		}
	}

/** Player thread handler for the append-to-SiGML-pipe request event. */
	protected void handleAppendToSiGMLPipeEvent(SPAEvent evt)
	throws InterruptedException {

		final String PLAY_ACTION = "append to SiGML pipe";

		if (this.checkStateHasBoth(
				PLAYING_SIGML_PIPED, HAS_OPEN_SIGML_PIPE, PLAY_ACTION)) {

			String errmsg = null;
			try {
				this.sigmlPipe.appendSiGMLFragment(evt.PARAM);
			}
			catch (IOException iox) { errmsg = iox.getMessage(); }

			this.deliverAck("SiGML pipe", errmsg);
		}
	}

/** Player thread handler for the close-SiGML-pipe request event. */
	protected void handleCloseSiGMLPipeEvent(SPAEvent evt)
	throws InterruptedException {

		final String PLAY_ACTION = "close SiGML pipe";

		if (this.checkStateHasBoth(
				PLAYING_SIGML_PIPED, HAS_OPEN_SIGML_PIPE, PLAY_ACTION)) {

			String errmsg = this.doCloseSiGMLPipe();
			this.deliverAck("SiGML pipe close", errmsg);
		}
	}

/** Player thread handler for the stop-playing-SiGML request event. */
	protected void handleStopPlayingSiGMLEvent() throws InterruptedException {

		if (this.checkStateTest(
				this.state.hasSteadyPlay(), "stop playing SiGML")) {

			String sperrmsg = null;
			if (this.state.hasBoth(PLAYING_SIGML_PIPED, HAS_OPEN_SIGML_PIPE)) {
				sperrmsg = this.doCloseSiGMLPipe();
			}

			this.state.include(STOPPING_PLAY);
			this.state.logChange();

			this.JA_PLAYER.stopPlaying();
			this.deliverAck("Stop playing SiGML", sperrmsg);
		}
	}

/** Player thread handler for the given set-speed-up request event. */
	protected void handleSetSpeedUpEvent(SPAEvent evt)
	throws InterruptedException {

		if (this.checkStateHasNot(HALTING, "set speed")) {
			try {
				// NB  This event is transiently handled -- it is not
				// accompanied by any state change.
				float speedup = Float.parseFloat(evt.PARAM);
				this.SPEED_CONTROL.setSpeedUp(speedup);
				this.EVENT_SOURCE.deliverOKAck();
			}
			catch (NumberFormatException nfx) {
				this.EVENT_SOURCE.deliverAck(
					"Badly formatted speed value: "+nfx.getMessage());
			}
		}
	}

/** Player thread handler for the given set-animgen-FPS request event. */
	protected void handleSetAnimgenFPSEvent(SPAEvent evt)
	throws InterruptedException {

		if (this.checkStateHasNot(HALTING, "set FPS")) {
			float fps = FPSPane.fpsValue(evt.PARAM);
			if (0 <= fps) {
				if (fps != this.JA_OPTS.animgenFPS()) {
					this.JA_OPTS.updateAnimgenFPS(fps);
				}
				this.EVENT_SOURCE.deliverOKAck();
			}
			else {
				this.EVENT_SOURCE.deliverAck("Invalid FPS value: "+evt.PARAM);
			}
		}
	}

/** Player thread handler for the given set-do-log-dropped-frames request event. */
	protected void handleSetDoLogDroppedFrames(SPAEvent evt)
	throws InterruptedException {

		if (this.checkStateHasNot(HALTING, "set do-log-dropped-frames")) {
			final boolean ON = evt.PARAM.equalsIgnoreCase("true");
			final boolean OFF = evt.PARAM.equalsIgnoreCase("false");
			if (ON || OFF) {
				this.JA_OPTS.updateDoLogDroppedFrames(ON);
				this.EVENT_SOURCE.deliverOKAck();
			}
			else {
				this.EVENT_SOURCE.deliverAck(
					"Invalid Do-Log-Dropped-Frames value: "+evt.PARAM);
			}
		}
	}
/** Player thread handler for the given suspend-if-playing request event. */
	protected void handleSuspendIfPlayingEvent(SPAEvent evt)
	throws InterruptedException {

		if (this.state.hasSteadyPlay()) {

			this.JA_PLAYER.suspendPlaying();
			this.animSuspended = true;
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker,
				SPAED_PREFIX+" Suspend animation");
		}

		this.EVENT_SOURCE.deliverOKAck();
	}

/** Player thread handler for the given suspend-if-playing request event. */
	protected void handleResumeIfPlayingEvent(SPAEvent evt)
	throws InterruptedException {

		if (this.animSuspended && this.state.hasSteadyPlay()) {

			this.JA_PLAYER.resumePlaying();
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.EVENTMarker,
				SPAED_PREFIX+": Resume animation.");
		}

		this.animSuspended = false;

		this.EVENT_SOURCE.deliverOKAck();
	}

/** Player thread handler for the given avatar-loaded/unloaded event. */
	protected void handleAvatarEvent(SPAEvent evt)
	throws InterruptedException {

		/*	avatar-loaded-ok      av
		 *  avatar-load-failed
		 *  avatar-unloaded       av
		 */

		if (evt.KIND == AVATAR_UNLOADED) {
			this.handleAvatarUnloadedEvent(evt);
		}
		else {
			this.handleAvatarLoadedEvent(evt);
		}
	}

/** Handles the given (successful or failed) avatar-loaded event. */
	protected void handleAvatarLoadedEvent(SPAEvent evt)
	throws InterruptedException {

		/*	avatar-loaded-ok      av
		 *  avatar-load-failed
		 */

		final String AE_ACTION = "accept avatar event "+evt.KIND;

		if (this.checkStateHas(CHANGING_AVATAR, AE_ACTION)) {

			this.state.oneOutOneIn(CHANGING_AVATAR, IDLE);
			this.state.remove(HAS_ALL_FRAMES);
			if (evt.KIND == AVATAR_LOADED_OK) {
				this.state.include(HAS_AVATAR);
			}
			this.state.logChange();

			this.EVENT_SOURCE.deliverOKAck();
			final Object[] ARGS = { evt.kindTag(), evt.PARAM };
			this.doJSCall(JS_AVATAR_EVENT, ARGS);
		}
	}

/** Handles the given avatar-unloaded event. */
	protected void handleAvatarUnloadedEvent(SPAEvent evt)
	throws InterruptedException {

		/*	avatar-unloaded       av
		 */

		final String AE_ACTION = "accept avatar event "+evt.KIND;

		if (this.checkStateHasSome(CHANGING_AVATAR, HALTING, AE_ACTION)) {

			this.state.remove(HAS_AVATAR);
			this.state.remove(HAS_ALL_FRAMES);
			if (this.state.has(HALTING)) {
				this.state.remove(CHANGING_AVATAR);
			}
			this.state.logChange();

			this.EVENT_SOURCE.deliverOKAck();
			final Object[] ARGS = { evt.kindTag(), evt.PARAM };
			this.doJSCall(JS_AVATAR_EVENT, ARGS);
		}
	}

/** Player thread handler for the given frames-generation event. */
	protected void handleFramesGenEvent(SPAEvent evt)
	throws InterruptedException {

		/*	load-frames-start
		 *  loaded-next-sign      nf, ns
		 *  load-frames-done-ok   nf, ns
		 *  load-frames-done-bad
		 */

		final String FG_ACTION = "accept frames-gen event "+evt.KIND;

		if (this.checkStateHasPlaySiGML(FG_ACTION)) {

			if (evt.KIND.isIn(DONE_FRAMES_GEN_EVENTS)) {
				if (evt.KIND == LOAD_FRAMES_DONE_OK) {
					this.state.oneOutOneIn(LOADING_FRAMES, HAS_ALL_FRAMES);
				}
				else {
					this.state.remove(LOADING_FRAMES);
				}
				this.state.logChange();
			}

			Object[] args = { evt.kindTag() };
			boolean hassf = evt.KIND.isIn(WITH_INDICES_FRAMES_GEN_EVENTS);
			if (hassf) {
				Object[] fsargs = { evt.kindTag(), evt.INTS[0], evt.INTS[1] };
				args = fsargs;
			}
			this.EVENT_SOURCE.deliverOKAck();
			this.doJSCall(JS_FRAMES_GEN_EVENT, args);
		}
	}

/** Player thread handler for the given animation event. */
	protected void handleAnimationEvent(SPAEvent evt)
	throws InterruptedException {

		/*	play-frame            f
		 *  play-at-new-sign      f, s
		 *  skip-frame            f
		 *  skip-at-new-sign      f, s
		 *  done-play             f
		 */

		final String ANIM_EVT_ACTION = "accept animation event "+evt.KIND;

		if (this.checkStateHasPlaySiGML(ANIM_EVT_ACTION)) {

			// If we have just suspended animation, then we need to
			// suppress the ensuing PLAY_DONE event so that the player
			// thread stays in its animating state.
			if (this.animSuspended && evt.KIND == PLAY_DONE) {

				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.EVENTMarker,
					SPAED_PREFIX+": Suppressed "+evt.kindTag()+
					" event while suspended");
				this.EVENT_SOURCE.deliverOKAck();
			}
			else {
				if (evt.KIND == PLAY_DONE) {
					this.state.include(IDLE);
					this.state.removeAllPlaying();
					this.state.logChange();
				}

				Object[] args = { evt.kindTag(), evt.INTS[0] };
				if (evt.KIND.isIn(NEW_SIGN_ANIM_EVENTS)) {
					// evt.PARAM is gloss name.
					Object[] sfargs = {
						evt.kindTag(), evt.INTS[0], evt.INTS[1], evt.PARAM
					};
					args = sfargs;
				}
				this.EVENT_SOURCE.deliverOKAck();
				this.doJSCall(JS_ANIMATION_EVENT, args);
			}
		}
	}

//############  Shut-down thread launcher.   ############

/** Creates and launches the shut-down thread, which just runs the
 * {@link #doShutDown()}.
 */
	protected void launchShutDownThread() {

		// Create the shut-down thread.
		Thread tsd =
			new Thread() {
				public void run() {
					Thread.currentThread().setName("ShutDownThread");
					logger.log( LoggerConfig.LOGLevel, LoggerConfig.THREADMarker,
						"Run: SPA Shutdown Thread");
					SPAEventDispatcher.this.doShutDown();
					logger.log( LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,
						"End: SPA Shutdown Thread");
				}
			};

		// Start the shut-down thread.
		tsd.start();
	}

/** Performs the shut-down sequence for this applet, finally running
 * this dispatcher's HALT event generator, which should feed a HALT
 * request back to this dispatcher.
 */
	protected void doShutDown() {

		final String SD_PFX = SPAED_PREFIX+": doShutDown()";
		// Probably superfluous
		// logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker, SD_PFX);

		try {
			// Stop the player component, but don't kill its canvas.
			this.JA_PLAYER.stopPlaying();
			this.AVATARS_ENV.terminate();

			// Really wipe out the player component.
			this.JA_PLAYER.terminate();

			// JA_PLAYER.terminate() is synchronous, so now we really can
			// halt.  If it were asynchronous we would need to do some
			// synchronization here with the dying player events.

			// Post a HALT event -- which we expect to come back to this
			// event dispatcher thread.
			this.HALT_EVENT_GENERATOR.run();
			
			// Previsously called as event dispatcher loop closed.
			// Allows JS terminate() call to complete.
			this.SHUT_DOWN_BARRIER.open();

			// Probably superfluous
			// logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker,SD_PFX+" Done");
		}
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker,SD_PFX+" Interrupted: "+ix);
		}
	}

//############  Event handler support: SiGML pipe handling  ############

/** Delivers the acknowledgment appropriate to the given error message
 * value -- which may be null, indicating "no error" -- and using the
 * given message prefix if needed.
 */
	protected void deliverAck(String prefix, String errmsg)
	throws InterruptedException {

		if (errmsg == null) {
			this.EVENT_SOURCE.deliverOKAck();
		}
		else {
			this.EVENT_SOURCE.deliverAck(prefix+" error: "+errmsg);
		}
	}

/** Terminates the SiGML pipe -- which is assumed currently to exist and
 * to be open -- and updates the EDT state accordingly, returning
 * {@code null} in case of success, or an error message in case of failure.
 */
	protected String doCloseSiGMLPipe() {

		String errmsg = null;
		try {
			this.sigmlPipe.terminatePipe();
		}
		catch (IOException iox) { errmsg = iox.getMessage(); }

		this.sigmlPipe = null;
		this.state.remove(HAS_OPEN_SIGML_PIPE);
		this.state.logChange();

		return errmsg;
	}

//############  Event handler support: state checking  ############

/** Delivers an invalid-state acknowledgement with the given message prefix. */
	protected void putBadStateAck(String prefix)
	throws InterruptedException {

		this.EVENT_SOURCE.deliverAck(prefix+" in state "+this.state);
	}

/** Delivers an invalid-state-for-request acknowledgement for the given request. */
	protected void putCannotDoInStateAck(String dorqst)
	throws InterruptedException {

		this.putBadStateAck("Cannot "+dorqst);
	}

/** Checks the given state test result flag and, if it's false, delivers
 * an invalid-state acknowledgment for the given request, finally
 * returning the flag value.
 */
	protected boolean checkStateTest(boolean ok, String dorqst)
	throws InterruptedException {
		
		if (! ok) {
			this.putCannotDoInStateAck(dorqst);
		}

		return ok;
	}

/** Checks that the given flag is included in the event dispatcher
 * thread's current state, and if not, delivers an invalid-state
 * acknowledgment for the given request.
 */
	protected boolean checkStateHas(StateFlag sflag, String dorqst)
	throws InterruptedException {

		return this.checkStateTest(this.state.has(sflag), dorqst);
	}

/** Checks that both the given flags are included in the event dispatcher
 * thread's current state, and if not, delivers an invalid-state
 * acknowledgment for the given request.
 */
	protected boolean checkStateHasBoth(
		StateFlag sf0, StateFlag sf1, String dorqst)
	throws InterruptedException {

		return this.checkStateTest(this.state.hasBoth(sf0, sf1), dorqst);
	}

/** Checks that at least one of the given flags is included in the event
 * dispatcher thread's current state, and if not, delivers an
 * invalid-state acknowledgment for the given request.
 */
	protected boolean checkStateHasSome(
		StateFlag sf0, StateFlag sf1, String dorqst)
	throws InterruptedException {

		return this.checkStateTest(this.state.hasSome(sf0, sf1), dorqst);
	}

/** Checks that the given flag is not included in the event dispatcher
 * thread's current state, and if it is included, delivers an invalid-state
 * acknowledgment for the given request.
 */
	protected boolean checkStateHasNot(StateFlag sflag, String dorqst)
	throws InterruptedException {

		return this.checkStateTest(! this.state.has(sflag), dorqst);
	}

/** Checks that the event dispatcher thread's current state includes
 * one of the play-SiGML flags, and if not, delivers an
 * invalid-state acknowledgment for the given request.
 */
	protected boolean checkStateHasPlaySiGML(String dorqst)
	throws InterruptedException {

		return this.checkStateTest(this.state.hasBasicPlay(), dorqst);
	}

//############  Output to HTML/Javascript.  ############

/** Calls out to the given Javascript function with the given arguments. */
	protected void doJSCall(String func, Object[] args) {

		// Guard the call with a check that the JS environment is
		// accessible -- gives us some chance of soldiering on even
		// if it isn't.
		if (this.HTML_WINDOW != null) {
			this.HTML_WINDOW.call(func, args);
		}
	}
}
