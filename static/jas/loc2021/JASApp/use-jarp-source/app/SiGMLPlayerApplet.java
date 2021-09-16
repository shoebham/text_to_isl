/*
 * SiGMLPlayerApplet.java		2008-08-14
 *
 * Derived from older LCSiGMLURLPlayer.
 */
package app;


import java.applet.Applet;


import java.awt.Component;
import java.awt.BorderLayout;

import netscape.javascript.JSObject;


import jautil.JAEnv;
import jautil.JAOptions;
import jautil.AppletPropertiesSetter;

import player.AvatarEventHandler;
import player.JALoadingPlayerEventHandler;
import player.JACanvasEmbedder;

import app.util.OutermostAppletFinder;
import app.util.OneShotTimeoutBarrier;
import app.util.UseJLogger;

import app.spa.SPAEvent;
import app.spa.SPAEventAck;
import app.spa.SPAEventTarget;
//import app.spa.SPAEventStreamChannel;
import app.spa.SimplerSPAEventStreamChannel;
import app.spa.SPAAvatarEventHandler;
import app.spa.SPAPlayerEventHandler;

import app.spa.SPAEventDispatcher;

import static app.spa.EventId.*;
import static app.util.ThreadUtilities.getGroupThreads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

import util.LoggerConfig;


/** An SiGMLPlayerApplet is a simple signed animation player applet
 * which communicates with enclosing HTML/javascript using the
 * <em>LiveConnect</em> protocol defined by Netscape/Mozilla.
 * <p>
 * This class assumes that the following Javascript event-handler
 * method calls are supported:
 * <p>
 * <pre>
 *     spaSetSiGMLPlayerApplet([SiGMLPlayerApplet] spa);
 *       // spa is the present applet instance, whose identity is
 *       // communicated to the Javascript/LiveConnect host by
 *       // this call, which should be made during the applet's
 *       // init() sequence.
 *       // This method is needed to support Google Chrome, whose
 *       // LiveConnect implementation seems to perform more rigorous
 *       // integrity checks on Javascript-to-Java calls than those
 *       // of other browsers.
 *
 *     spaFramesGenEvent([String] ekind, [int] f, [int] s);
 *       // ekind is one of the event kind tags:
 *       //    "LOAD_FRAMES_START",
 *       //    "LOADED_NEXT_SIGN",
 *       //    "LOAD_FRAMES_DONE_OK",
 *       //    "LOAD_FRAMES_DONE_BAD"
 *       // f, s, when supplied, are frame and sign counts/indices.
 * 
 *     spaAnimationEvent(
 *         [String] ekind, [int] f, [int] s, [String] gloss);
 *       // ekind is one of the event kind tags:
 *       //    "PLAY_FRAME",
 *       //    "SKIP_FRAME",
 *       //    "PLAY_FIRST_FRAME_OF_SIGN",
 *       //    "SKIP_FIRST_FRAME_OF_SIGN",
 *       //    "PLAY_DONE"
 *       // f, s, when supplied, are frame and sign counts/indices;
 *       // gloss, when supplied, is the gloss name for a new sign.
 * 
 *     spaAvatarEvent([String] ekind, [String] avname);
 *       // ekind is one of the event kind tags:
 *       //    "AVATAR_LOADED_OK",
 *       //    "AVATAR_LOAD_FAILED",
 *       //    "AVATAR_UNLOADED"
 *       // avname, when supplied, is the name of the avatar in question.
 * </pre>
 */
public class SiGMLPlayerApplet extends Applet {
//public class SiGMLPlayerApplet extends Applet implements UseJLogger {

/** UseJLogger. */
	private static Logger					logger = null;

	/** Prefix for SPA messages. */
	private static final String				SPA_PREFIX =
											"SiGML-Player-Applet";
/** Set sub-applet Javascript call-out name. */
	private static final String				SET_SPA =
											"spaSetSiGMLPlayerApplet";

//############  SiGML Player Applet instance variables.  ############

/** Options setttings. */
	private JAOptions						JA_OPTS;
/** Event target for incoming HTML/JS calls, and JA player event handlers. */
	private SPAEventTarget					eventTarget;
/** Synchronisation barrier for completion of {@link #terminate()}. */
	private OneShotTimeoutBarrier			terminateBarrier;
/** Flag indicating whether this applet has terminated. */
	private boolean							terminated;
/** Flag indicating whether this applet has a HALT event to be posted. */
	private boolean							haltSignalled;

/** This applet's logger. */
	private SPALogger						LOGGER;


/** Constructs a new instance of this applet. */
	public SiGMLPlayerApplet() {

		super();
	}

	public JAOptions getOpts()		{ return this.JA_OPTS; }
/*
 * initialise(initav)
 * start()
 * loadAvatar(av)
 * playSiGMLURL(surl)
 * playSiGMLText(sigml)
 * stopPlayingSiGML()
 * terminate()
 * stop()
 * destroy()
 */

//############  Public HTML/JS/LiveConnect UI methods  ############


/** Initialises this player applet. */
	public synchronized void init() {

		// Rename Thread
		Thread.currentThread().setName(SPA_PREFIX);

		// Create Log4J parameter directly to bypass secure property checks
		System.setProperty("log4j.configurationFile", this.getCodeBase()+"log4j2.xml");
		logger = LogManager.getLogger();
		
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
			"Log4J2 config: "+System.getProperty("log4j.configurationFile", "<null>"));
		logger.log(LoggerConfig.LOGLevel, LoggerConfig.THREADMarker, "Start Thread: "+SPA_PREFIX);
		logger.log(LoggerConfig.INFOLevel, LoggerConfig.SESSIONMarker, SPA_PREFIX+" Starting: Java "+System.getProperty("java.version"));

		logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
			"Threads at start of "+SPA_PREFIX);
		Thread[] threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
		int i;
		for (i=0; i < threadArray.length; i++) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
		}

//        System.out.println("## Original Properties:");
//        System.getProperties().list(System.out);
        
		// Copy appropriate HTML parameter settings to the system properties.
		AppletPropertiesSetter.copyStdAppletProperties(this);

//        System.out.println("## Enhanced Properties:");
//        System.getProperties().list(System.out);
        
//        System.out.println("## Some Parameters:");
//        System.out.println("avatar.id: " + this.getParameter("avatar.id"));
//        System.out.println("avatar.id.list: " + this.getParameter("avatar.id.list"));
//        System.out.println("cacheable.avatar.list: " + this.getParameter("cacheable.avatar.list"));

		// Establish the options set for this invocation of the applet.
		String optspath = this.getParameter("options");
		String prefs = "SiGMLPlayerApplet";
		// -update:  make changes stick for future sessions.
		String[] args = {"-update", optspath };
		if (optspath == null) {
			args = null;
		}
		JAEnv jaenv =  JAEnv.makeAppletJAEnv(this.getCodeBase());
		this.JA_OPTS = JAOptions.makeJAOptions(prefs, args, this, jaenv);

		// Initialise logging for this applet.
		this.LOGGER = new SPALogger(logger, this.getParameter("do.spa.logging"));

		// Javascript access -- more complicated in the context
		// of the JNLPAppletLauncher.
		Applet outerapplet = OutermostAppletFinder.getOutermost(this);
		JSObject htmlwin =
			outerapplet==null ? null : JSObject.getWindow(outerapplet);

		// We really expect getWindow() not to fail when outerapplet is
		// non-null, but check and report if that does happen.
		if (outerapplet != null && htmlwin == null) {
			System.out.println(SPA_PREFIX+": JSObject.getWindow() fails.");
		}

		// Ensure we use a border layout, since our canvas embedder
		// places the avatar canvas in its CENTER.
		this.setLayout(new BorderLayout());

		// Create the event stream channel for this applet.
	//	SPAEventStreamChannel evtchan = new SPAEventStreamChannel(this);
		SimplerSPAEventStreamChannel evtchan =
			new SimplerSPAEventStreamChannel(this.LOGGER);
		this.eventTarget = evtchan;

		// Create event handlers for the supporting JA player component.
		AvatarEventHandler aeh = new SPAAvatarEventHandler(this.eventTarget);
		JALoadingPlayerEventHandler jalpeh =
			new SPAPlayerEventHandler(this.eventTarget);

		// Create a runnable to enable the event dispatcher to
		// connect to our HALT event generator.
		// JRWG: Probably better to delay the HALT event until the Applet destroy() call
		Runnable haltevtgen =
			new Runnable() {
				public void run() {
					Thread.currentThread().setName("postHaltEvent");
					logger.log( LoggerConfig.LOGLevel, LoggerConfig.THREADMarker,
						"Run: Post HALT Event");
					SiGMLPlayerApplet.this.postHaltEvent();
					SiGMLPlayerApplet.this.haltSignalled = true;
					logger.log( LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,
						"End: Post HALT Event");
				}
			};

		// Create the synchronisation barrier for terminate().
		this.terminateBarrier = OneShotTimeoutBarrier.newBarrier();
		this.terminated = false;

		// Create the SPA event dispatcher thread manager.
		SPAEventDispatcher evtdispatcher =
			new SPAEventDispatcher(
				this.JA_OPTS, htmlwin, this.LOGGER, evtchan,
				this.JA_CANVAS_EMBEDDER, aeh, jalpeh,
				haltevtgen, this.terminateBarrier);

		// Launch the SPA event dispatcher thread.
		evtdispatcher.launchSPAEventDispatchThread();

		// Post the first event to the SPA event dispatcher thread,
		// requesting the loading of the initially specified avatar.
		this.setAvatar(this.JA_OPTS.getAvatarsEnv().currentAvatar());

		logger.log(LoggerConfig.INFOLevel, LoggerConfig.SESSIONMarker, SPA_PREFIX+" Created");
		logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: "+SPA_PREFIX);
}

/** Applet start method.  */
	public synchronized void start() {

		// NB
		// Safari is the only browser that generates applet
		// start() and stop() calls on tab switches.
		String mthd = "start()";
		this.LOGGER.log("Cal: " + mthd, LoggerConfig.DEBUGLevel);

		SPAEvent ripevt = new SPAEvent(RESUME_IF_PLAYING);
		SPAEventAck ripack = this.eventTarget.postEvent(ripevt);
		this.checkAckOK(ripack, mthd);
		
		this.LOGGER.log("End: " + mthd, LoggerConfig.DEBUGLevel);
	}

/** Applet stop method. */
	public synchronized void stop() {

		// NB
		// Safari is the only browser that generates applet
		// start() and stop() calls on tab switches.
		// Not clear that is still true.
		String mthd = "stop()";
		this.LOGGER.log("Cal: " + mthd, LoggerConfig.DEBUGLevel);

		if (!this.terminated) {
			SPAEvent sipevt = new SPAEvent(SUSPEND_IF_PLAYING);
			SPAEventAck sipack = this.eventTarget.postEvent(sipevt);
			this.checkAckOK(sipack, mthd);
		}
		this.LOGGER.log("End: " + mthd, LoggerConfig.DEBUGLevel);
	}

/** Applet destroy method -- a no-op in this case. */
	public synchronized void destroy() {

		String mthd = "destroy()";
		this.LOGGER.log("Cal: " + mthd, LoggerConfig.DEBUGLevel);
		
		this.LOGGER.log("Threads before " + mthd, LoggerConfig.TRACELevel, LoggerConfig.THREADMarker);
		Thread[] threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
		int i;
		for (i=0; i < threadArray.length; i++) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
		}

		// Post a HALT event. Check that one has been signalled
		if (! this.haltSignalled) {
			this.LOGGER.log("Expecting HALT to have been signalled: " + mthd, LoggerConfig.WARNLevel);
			this.postHaltEvent();
		}

//		this.LOGGER.log("Threads within " + mthd);
//		threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
//		for (i=0; i < threadArray.length; i++) {
//			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker,
//				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
//		}
//
//		this.LOGGER.log("Gap: " + mthd);
//		try {
//			Thread.sleep(5000);                 // milliseconds
//		} catch(InterruptedException ex) {
//			  Thread.currentThread().interrupt();
//		}

		this.LOGGER.log("Threads after " + mthd, LoggerConfig.TRACELevel, LoggerConfig.THREADMarker);
		threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
		for (i=0; i < threadArray.length; i++) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
		}

		this.LOGGER.log("End: " + mthd, LoggerConfig.DEBUGLevel);
	}

/** Applet page unload method: calls shutDown() to post a shut-down event to the 
 * player thread.
 */
	public void terminate() {

		String mthd = "terminate()";
		this.LOGGER.log("Cal: " + mthd, LoggerConfig.DEBUGLevel);

		// Early action for now
		shutDown();		
		
//		this.LOGGER.log("Gap: " + mthd);
//		try {
//			Thread.sleep(10000);                 // milliseconds
//		} catch(InterruptedException ex) {
//			Thread.currentThread().interrupt();
//		}
		
		this.LOGGER.log("End: " + mthd, LoggerConfig.DEBUGLevel);

	}

/** Applet page unload method: posts a shut-down event to the player thread,
 * and waits for this to take effect.
 */
	protected void shutDown() {

		String mthd = "shutDown()";
		this.LOGGER.log("Cal: " + mthd, LoggerConfig.DEBUGLevel);

		this.LOGGER.log("Threads before " + mthd);
		Thread[] threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
		int i;
		for (i=0; i < threadArray.length; i++) {
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.THREADMarker,
				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
		}

		try {
			// Post the SHUT_DOWN event to the player thread.
			SPAEventAck sdack = this.eventTarget.postEvent(SHUT_DOWN);
			this.checkAckOK(sdack, mthd);

			// Determine the timeout for our shutdown barrier wait.
			int TIMEOUT = Math.max(100, this.getQuitDelayParamValue());

			// Wait for the signal that the shutdown really is done.
			long t0 = System.nanoTime();
			boolean sdok = this.terminateBarrier.passWhenOpen(TIMEOUT);
			long t1 = System.nanoTime();
			int t = (int)Math.round((double)(t1 - t0) / (1000 * 1000));
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
				SPA_PREFIX+" passed shutdown barrier OK="+sdok+
				" timeout="+TIMEOUT+"ms t_actual="+t);

			this.terminated = true;
		}
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
				SPA_PREFIX + "." + mthd + ": "+ix);
		}
		
		this.LOGGER.log("Threads after " + mthd, LoggerConfig.TRACELevel, LoggerConfig.THREADMarker);
		threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
		for (i=0; i < threadArray.length; i++) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
		}
		
		this.LOGGER.log("End: " + mthd, LoggerConfig.DEBUGLevel);

	}

/** Accepts a new SiGML URL to be played by this player. */
	public String playSiGMLURL(String url) {

		String mthd = "playSiGMLURL()";
		this.LOGGER.log(mthd);
		
		SPAEvent psuevt = new SPAEvent(PLAY_SIGML_URL, url.trim());
		SPAEventAck psuack = this.eventTarget.postEvent(psuevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return psuack.MESSAGE;
	}

/** Accepts a new SiGML string to be played by this player.
 * 
 * @return undefined/null if successful; the error description otherwise.
 */
	public String playSiGMLText(String sigml) {

		//if (TMP_TEXT_VIA_PIPE) return this.playPipedSiGML(sigml);

		String mthd = "playSiGMLText()";
		this.LOGGER.log(mthd);

		SPAEvent pstevt = new SPAEvent(PLAY_SIGML_TEXT, sigml);
		SPAEventAck pstack = this.eventTarget.postEvent(pstevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return pstack.MESSAGE;
	}

/** Accepts a request for a new piped SiGML input session for this player.
 *
 * @return undefined/null if successful; the error description otherwise.
 */
	public String startPlaySiGMLPiped() {

		String mthd = "startPlaySiGMLPiped()";
		this.LOGGER.log(mthd);

		SPAEvent spspevt = new SPAEvent(START_PLAY_SIGML_PIPED);
		SPAEventAck spspack = this.eventTarget.postEvent(spspevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return spspack.MESSAGE;
	}

/** Accepts a request for the given SiGML fragment to be appended to
 * the current piped SiGML input for this player.
 *
 * @return undefined/null if successful; the error description otherwise.
 */
	public String appendToSiGMLPipe(String fragment) {

		String mthd = "appendToSiGMLPipe()";
		this.LOGGER.log(mthd);

		SPAEvent aspevt = new SPAEvent(APPEND_TO_SIGML_PIPE, fragment);
		SPAEventAck aspack = this.eventTarget.postEvent(aspevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return aspack.MESSAGE;
	}

/** Accepts a request to close the current piped SiGML input session
 * for this player.
 *
 * @return undefined/null if successful; the error description otherwise.
 */
	public String closeSiGMLPipe() {

		String mthd = "closeSiGMLPipe()";
		this.LOGGER.log(mthd);

		SPAEvent cspevt = new SPAEvent(CLOSE_SIGML_PIPE);
		SPAEventAck cspack = this.eventTarget.postEvent(cspevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return cspack.MESSAGE;
	}

/** Accepts a stop-player request.
 * 
 * @return undefined/null if successful; the error description otherwise.
 */
	public String stopPlayingSiGML() {

		String mthd = "stopPlayingSiGML()";
		this.LOGGER.log(mthd);

		SPAEventAck spsack = this.eventTarget.postEvent(STOP_PLAY_SIGML);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return spsack.MESSAGE;
	}

/** Allows HTML/Javascript to request a particular avatar.
 * 
 * @return undefined/null if successful -- the expected outcome for this
 *         method, since the request is handled asynchronously;
 *         the error description otherwise.
 */
	public String setAvatar(String avatar) {

		String mthd = "setAvatar()";
		this.LOGGER.log(mthd);

		SPAEvent laevt = new SPAEvent(LOAD_AVATAR, avatar.trim());
		SPAEventAck laack = this.eventTarget.postEvent(laevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return laack.MESSAGE;
	}

/** Allows HTML/Javascript to change the speed at which the current
 * animation is played, as the player is running
 * 
 * @return undefined/null if successful; the error description otherwise.
 */
	public String setSpeed(String speedupstr) {

		String mthd = "setSpeed()";
		this.LOGGER.log(mthd);

		SPAEvent ssuevt =
			new SPAEvent(SET_SPEED_UP, speedupstr.trim());
		SPAEventAck ssuack = this.eventTarget.postEvent(ssuevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return ssuack.MESSAGE;
	}

/** Allows HTML/Javascript to change the speed at which the current
 * animation is played, as the player is running
 *
 * @return undefined/null if successful; the error description otherwise.
 */
	public String setAnimgenFPS(String fpsstr) {

		String mthd = "setAnimgenFPS()";
		this.LOGGER.log(mthd);

		SPAEvent sfpsevt = new SPAEvent(SET_ANIMGEN_FPS, fpsstr.trim());
		SPAEventAck sfpsack = this.eventTarget.postEvent(sfpsevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return sfpsack.MESSAGE;
	}

/** Allows HTML/Javascript to set/unset the avatar panel's
 * "player.do.log.dropped.frames" flag option setting.
 *
 * @return undefined/null if successful; the error description otherwise.
 */
	public String setDoLogDroppedFrames(String dldfstr) {

		String mthd = "setDoLogDroppedFrames()";
		this.LOGGER.log(mthd);

		SPAEvent sdldfevt =
			new SPAEvent(SET_DO_LOG_DROPPED_FRAMES, dldfstr.trim());
		SPAEventAck sdldfack = this.eventTarget.postEvent(sdldfevt);

		// Return status message to JS (OK/null --> undefined or null (JS))
		return sdldfack.MESSAGE;
	}

	public void switchLogEnabled(String enableflag) {

		String mthd = "switchLogEnabled()";

		// Log this call both before and after the switch -- just
		// one of the two attempts will be effective.
		this.LOGGER.log(mthd+": "+enableflag);

		this.LOGGER.setLogEnabled(enableflag);

		// See previous comment.
		this.LOGGER.log(mthd+": "+enableflag);
	}

//############  Support methods.   ############

/** Checks that the given acknowledgment is OK and, if not, prints
 * its message prefixed with the given method name.
 */
	protected void checkAckOK(SPAEventAck ack, String mthd) {

		if (!ack.OK) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.EVENTMarker,
				SPA_PREFIX+"."+mthd+": "+ack.MESSAGE);
		}
	}

/** Reads and returns the value of the "quit.delay.ms" applet parameter,
 * or returns 0 if the parameter is undefined or erroneously formatted.
 */
	protected int getQuitDelayParamValue() {

		int qd = 0;
		final String QD_PARAM = this.getParameter("quit.delay.ms");
		if (QD_PARAM != null) {
			try {
				qd = Integer.parseInt(QD_PARAM);
			}
			catch (NumberFormatException nfx) {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
					SPA_PREFIX+".getQuitDelayParamValue(): "+nfx);
			}
		}
		return qd;
	}

/** Posts a HALT event to the player thread, and checks the acknowledgement. */
	protected void postHaltEvent() {

		// Post a HALT event back to the player thread.
		SPAEventAck hack = this.eventTarget.postEvent(HALT);
		this.checkAckOK(hack, "postHaltEvent()");
	}

//############  Avatar canvas embedder.  ############

/** Embedder for JA canvas: provides the JA player with a method that
 * adds the avatar canvas to this applet's panel.
 */
	protected final JACanvasEmbedder	JA_CANVAS_EMBEDDER =
	new JACanvasEmbedder() {
		public void embedInContainer(final Component jarpcanvas) {
			SiGMLPlayerApplet.this.add(jarpcanvas, BorderLayout.CENTER);
		}
	};

//############  Logging.  ############

/** UseJLogger implementation for a SiGML Player Applet, using standard output
 as the target for logging output, also providing a means
 ({@link #setLogEnabled(boolean)}) of dynamically switching logging
 * on and off.
 */
	protected static class SPALogger implements UseJLogger {

	/** The current logging output target, if there is one. */
		// private PrintWriter				logWriter;
		private boolean			enabled = true;
		private final Logger			logger;

	/** Constructs a new logger, initially enabled or not as specified
	 * by the given flag string.
	 */
		public SPALogger(Logger logger, String enabledflag) {

			// this.logWriter = null;
			this.logger = logger;
			// Let log4j determine filtering for now
			//this.setLogEnabled(enabledflag);
			this.setLogEnabled("true");
		}

	/** Outputs the given message on the log writer, if SPA logging is
	 * enabled.
	 * Better to put massage after optional Level and Marker.
	 */
		public final void log(String msg) {
			log(msg, LoggerConfig.LOGLevel);
		}
		
		public final void log(String msg, Level lev) {
			log(msg, lev, LoggerConfig.COMMANDMarker);
		}
		public final void log(String msg, Level lev, Marker mrk) {

			if (this.logIsEnabled()) {
				this.logger.log(lev, mrk, "SPA: "+msg);
				// this.logWriter.print("####  SPA: ");
				// this.logWriter.println(msg);
			}
		}

	/** Indicates if SPA logging is currently  enabled. */
		public final synchronized boolean logIsEnabled() {

			// return (this.logWriter != null);
			return (this.enabled);
		}

	/** Enables or disables SPA logging as specified. */
		protected final synchronized void setLogEnabled(boolean enable) {

			// Do nothing unless the current state differs from
			// the one requested.
			//if (enable != (this.logWriter != null)) {
			//
			//	if (enable) {
			//		this.logWriter = new PrintWriter(System.out, true);
			//	}
			//	else {
			//		// DON'T close the log writer, since that also closes
			//		// the underlying print stream, e.g. System.out
			//		this.logWriter = null;
			//	}
			//}
			
			this.enabled = enable;
		}

	/** Enables SPA logging if the given string is non-null and
	 * matches {@code "true"} (case-insensitively), and disables logging
	 * otherwise.
	 */
		protected final void setLogEnabled(String enableflag) {

			this.setLogEnabled(
				enableflag != null && enableflag.equalsIgnoreCase("true"));
		}
	}

//______________________________________________________________________

//	private static final String TTVP_VERSION = "____  PIPING version D";
//	private static final boolean TMP_TEXT_VIA_PIPE = false;
//
//	private String playPipedSiGML( String sigml) {
//
//		SPAEvent spspevt = new SPAEvent(START_PLAY_SIGML_PIPED);
//		SPAEventAck spspack = this.eventTarget.postEvent(spspevt);
//
//		if (spspack.OK) {
//
//			final java.util.ArrayList<String> signs =
//				new java.util.ArrayList<String>();
//
//			java.util.regex.Pattern signbegre =
//				 java.util.regex.Pattern.compile("<hns_sign",
//					java.util.regex.Pattern.DOTALL);
//			java.util.regex.Pattern signendre =
//				 java.util.regex.Pattern.compile("</hns_sign>",
//					java.util.regex.Pattern.DOTALL);
//			java.util.regex.Matcher begmtchr =signbegre.matcher(sigml);
//			java.util.regex.Matcher endmtchr =signendre.matcher(sigml);
//
//			while(begmtchr.find()) {
//				int beg = begmtchr.start();
//				endmtchr.find();
//				int end = endmtchr.end();
//				String sign = sigml.substring(beg, end);
//				System.out.println("SIGN "+signs.size()+":");
////				System.out.println("==========");
////				System.out.println(sign);
//				signs.add(sign+"\n");
//			}
//
//			final SPAEventTarget EVT_TARGET = this.eventTarget;
//
//			Thread tpiping = new Thread() {
//				public void run() {
//					final int N = signs.size();
//					for (int i=0; i!=N; ++i) {
//						try { Thread.sleep(2000); }
//						catch (InterruptedException ix) {}
//						SPAEvent apevt =
//							new SPAEvent(
//								APPEND_TO_SIGML_PIPE, signs.get(i));
//						SPAEventAck apack = EVT_TARGET.postEvent(apevt);
//						System.out.println(
//							"____  Piping sign "+i+": "+
//							(apack.OK?"OK":apack.MESSAGE));
//					}
//					try { Thread.sleep(2000); }
//					catch (InterruptedException ix) {}
//					SPAEvent xpevt = new SPAEvent(CLOSE_SIGML_PIPE);
//					SPAEventAck xpack = EVT_TARGET.postEvent(xpevt);
//					System.out.println(
//						"____  Close pipe: "+(xpack.OK?"OK":xpack.MESSAGE));
//				}
//			};
//			tpiping.start();
//			System.out.println(TTVP_VERSION);
//			System.out.println("____  piping thread started");
//		}
//
//		return spspack.MESSAGE;
//	}
}
