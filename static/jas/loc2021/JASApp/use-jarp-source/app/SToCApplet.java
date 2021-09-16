/*
 * SToCApplet.java		2011-11-23
 *
 * Massively-reduced copy of SPA (started 2008-08-14).
 * Provides SiGML-to-CAS functions for JS/WebGL ARP.
 */
package app;


import java.io.PrintWriter;

import java.util.Date;

import java.applet.Applet;

import netscape.javascript.JSObject;

import jautil.JAEnv;
import jautil.JAOptions;
import jautil.AppletPropertiesSetter;

import app.util.UseJLogger;

import app.util.SToCThread;
import static app.util.ThreadUtilities.getGroupThreads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

import util.LoggerConfig;


/** Provides SiGML-to-CAS functions for JS/WebGL ARP.
 * Intended to have no visible on-screen presence.
 */
public class SToCApplet extends Applet {

/** UseJLogger. */
	private static Logger					logger = null;

/** SToCA build ID -- logged at start of each {@link #init()}. */
	private static final String				STOCA_BUILD_ID = " (b0020)";
/** Prefix for SPA messages. */
	private static final String				STOCA_PREFIX =
											"SiGML-To-CAS-Applet";
/** "Return CAS" Javascript call-out name. */
	public static final String				STOCA_RETURN_CAS =
											"stocaReturnCAS";

/** CAS format tag for a JSON stream result. */
	public static final String				JSON_FORMAT = "json";
/** CAS format tag for an XML result. */
	public static final String				XML_FORMAT = "xml";

//############  SiGML Player Applet instance variables.  ############

/** Options settings. */
	private JAOptions						JA_OPTS;
/** Flag indicating whether this applet has terminated. */
	private boolean							terminated;
/** HTML object for callouts. */
	private JSObject						HTML_WINDOW;
/** This applet's logger. */
	private SToCALogger						LOGGER;
/** SiGML-to-CAS thread for this applet. */
	private SToCThread						STOC_THREAD;

/** Constructs a new instance of this applet. */
	public SToCApplet() { super(); }

/** Returns this applets options set. */
	public JAOptions getOpts()		{ return this.JA_OPTS; }

//############  Public HTML/JS/LiveConnect UI methods  ############

/** Initialises this SiGML-to-CAS applet. */
	public synchronized void init() {

		// Rename Thread
		Thread.currentThread().setName(STOCA_PREFIX);

		// Create Log4J parameter directly to bypass secure property checks
		System.setProperty("log4j.configurationFile", this.getCodeBase()+"log4j2.xml");
		logger = LogManager.getLogger();
		
		logger.log(LoggerConfig.LOGLevel, LoggerConfig.THREADMarker, 
			"Start Thread: "+STOCA_PREFIX);
		logger.log(LoggerConfig.INFOLevel, LoggerConfig.SESSIONMarker, 
			STOCA_PREFIX+STOCA_BUILD_ID+" Starting: Java "+System.getProperty("java.version"));

		logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
			"Threads at start of "+STOCA_PREFIX);
		Thread[] threadArray = getGroupThreads(Thread.currentThread().getThreadGroup());
		int i;
		for (i=0; i < threadArray.length; i++) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.THREADMarker,
				"Thread "+i+": "+threadArray[i].getName()+" {"+threadArray[i].getId()+"}");
		}
		// Log to console until the logger is established.
//		String jrevn = System.getProperty("java.version");
//		System.out.println((new Date())+"   Java version "+jrevn);
//		System.out.println(STOCA_PREFIX+STOCA_BUILD_ID+"  ####");

		// Copy appropriate HTML parameter settings to the system properties.
		AppletPropertiesSetter.copyStdAppletProperties(this);

		// Establish the options set for this invocation of the applet.
		String optspath = this.getParameter("options");
		String prefs = "SToCApplet";
		// -update:  make changes stick for future sessions.
		String[] args = {"-update", optspath };
		if (optspath == null) { args = null; }

		JAEnv jaenv =  JAEnv.makeAppletJAEnv(this.getCodeBase());
		this.JA_OPTS = JAOptions.makeJAOptions(prefs, args, this, jaenv);

		// Initialise logging for this applet.
//		this.LOGGER = new SToCALogger(this.getParameter("do.stoca.logging"));
		this.LOGGER = new SToCALogger("true");

		this.HTML_WINDOW = JSObject.getWindow(this);

		// We really expect getWindow() not to fail when outerapplet is
		// non-null, but check and report if that does happen.
		if (this.HTML_WINDOW == null) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker, 
				"JSObject.getWindow() fails");
		}

		this.STOC_THREAD =
			SToCThread.startTheThread(this.JA_OPTS, this.LOGGER);
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
			"Applet init() done");
	}

/** Applet start method.  */
	public synchronized void start() {

		// NB
		// Safari is the only browser that generates applet
		// start() and stop() calls on tab switches.
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
			"Applet start() done");

//		SPAEvent ripevt = new SPAEvent(RESUME_IF_PLAYING);
//		SPAEventAck ripack = this.eventTarget.postEvent(ripevt);
//		this.checkAckOK(ripack, mthd);
	}

/** Applet stop method. */
	public synchronized void stop() {

		// NB
		// Safari is the only browser that generates applet
		// start() and stop() calls on tab switches.
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
			"Applet stop() done");

		if (!this.terminated) {
//			SPAEvent sipevt = new SPAEvent(SUSPEND_IF_PLAYING);
//			SPAEventAck sipack = this.eventTarget.postEvent(sipevt);
//			this.checkAckOK(sipack, mthd);
		}
	}

/** Applet destroy method -- treat  like terminate(). */
	public synchronized void destroy() {

		final String MTHD = "destroy()";
		this.doTerminate(MTHD);
	}

/** Applet unload method: posts a shut-down event to the player thread,
 * and waits for this to take effect.
 */
	public void terminate() {

		final String MTHD = "terminate()";
		this.doTerminate(MTHD);
	}

	private void doTerminate(final String MTHD) {

		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
			"Terminating starting: "+MTHD);

		if (this.STOC_THREAD != null) { this.STOC_THREAD.kill(); }

		int qdelayms = this.JA_OPTS.getIntegerProperty("quit.delay.ms");
		if (qdelayms <= 0) { qdelayms = 300; } // Temporary hack.
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
			"Terminating starting: Quit delay (ms): "+qdelayms);

		try { Thread.sleep(qdelayms); }
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
				MTHD+": "+ix);
			Thread.currentThread().interrupt();
		}
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
			"Terminating completed: "+MTHD);
	}

/** Call to check applet is alive.
 */
	public String about() {
		String res = "SToCApplet";
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
				"about() called returning "+res);
		return res;
	}

/** Generates CAS data for the given SiGML URL and avatar, returning
 * the result as a CAS XML string or CAS JSON chunks as specified
 * by the given format tag -- or {@code null} in case of failure.
 */
	public void doSiGMLURLToCAS(
		String rqstid, String sigmlurl, String avatar, String casfmt) {

		CASDispatch casback = this.makeCASDispatcher(rqstid);

		try {
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
				"doSiGMLURLToCAS "+sigmlurl+" for "+avatar+" started");

			if (sigmlurl != null) {
				this.STOC_THREAD.
					requestSiGMLURLToCAS(sigmlurl, avatar, casfmt, casback);
				// Flag the fact that the CAS-generation is done.
				casback = null;
			}
		}
		catch (Exception ex) { ex.printStackTrace(); }

		// Generate a null CAS result in case of failuure.
		if (casback != null) { casback.returnCAS(null); }
	}

/** Generates CAS data for the given SiGML text and avatar, returning
 * the result as a CAS XML string or CAS JSON chunks as specified
 * by the given format tag -- or returning {@code null} in case of failure.
 */
	public void doSiGMLTextToCAS(
		String rqstid, String sigml, String avatar, String casfmt) {

		CASDispatch casback = this.makeCASDispatcher(rqstid);

		try {
			if (sigml == null) {
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
					"doSiGMLTextToCAS for "+avatar+": SiGML is null"); 
			} else {
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
					"doSiGMLTextToCAS for "+avatar+": SiGML length="+sigml.length());
			}

			if (sigml != null) {
				this.STOC_THREAD.
					requestSiGMLTextToCAS(sigml, avatar, casfmt, casback);
				// Flag the fact that the CAS-generation is done.
				casback = null;
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker, 
					"doSiGMLTextToCAS for "+avatar+": Done");
			}
		}
		catch (Exception ex) { ex.printStackTrace(); }

		// Generate a null CAS result in case of failure.
		if (casback != null) { casback.returnCAS(null); }
	}

	protected final CASDispatch makeCASDispatcher(final String RQST_TAG) {

		return new CASDispatch(){ public void returnCAS(final String CAS) {

			final JSObject HTML_WIN = SToCApplet.this.HTML_WINDOW;
			if (HTML_WIN != null) {
				final String[] ARGS = {RQST_TAG, CAS};
				logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker, 
					"SToCA returnCAS call "+RQST_TAG);
				// JRWG: (Object) becomes (Object []) but not checked
				HTML_WIN.call(STOCA_RETURN_CAS, (Object []) ARGS);
				logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker, 
					"SToCA returnCAS done "+RQST_TAG);
			}
		}};
	}

/** Updates this applet's log-enabled flag to the given setting. */
	// To be removed
	public void switchLogEnabled(String enableflag) {

		String mthd = "switchLogEnabled()";

		// Log this call both before and after the switch -- just
		// one of the two attempts will be effective.
		this.LOGGER.logp(mthd+": "+enableflag);

		this.LOGGER.setLogEnabled(enableflag);

		// See previous comment.
		this.LOGGER.logp(mthd+": "+enableflag);
	}

//	private final void log(String msg) { this.LOGGER.log(msg); }
//	private final void logp(String msg) { this.LOGGER.logp(msg); }
//	private final void logb(String msg) { this.LOGGER.logb(msg); }

//############  Logging.  ############

/** UseJLogger implementation for a SToC Applet, using standard output
 as the target for logging output, also providing a means
 ({@link #setLogEnabled(boolean)}) of dynamically switching logging
 * on and off.
 */
	public static class SToCALogger implements UseJLogger {

	/** The current logging output target, if there is one. */
		private PrintWriter				logWriter;

	/** Constructs a new logger, initially enabled or not as specified
	 * by the given flag string.
	 */
		public SToCALogger(String enabledflag) {

			this.logWriter = null;
			this.setLogEnabled(enabledflag);
		}

	/** Outputs the given message on the log writer, if SPA logging is
	 * enabled.
	 */
		public final void log(String msg) {

			if (this.logIsEnabled()) {
				this.logWriter.print("####  ");
				this.logWriter.println(msg);
			}
		}

	/** Outputs the given message on the log writer with the standard
	 * prefix, if SPA logging is enabled.
	 */
		public final void logp(String msg) {

			if (this.logIsEnabled()) {
				this.logWriter.print("####  SToCA: ");
				this.logWriter.println(msg);
			}
		}

	/** Outputs the given message on the log writer with no prefix
	 * (i.e. bare), if SPA logging is enabled.
	 */
		public final void logb(String msg) {

			if (this.logIsEnabled()) { this.logWriter.println(msg); }
		}

	/** Indicates if SPA logging is currently  enabled. */
		public final synchronized boolean logIsEnabled() {

			return (this.logWriter != null);
		}

	/** Enables or disables SPA logging as specified. */
		protected final synchronized void setLogEnabled(boolean enable) {

			// Do nothing unless the current state differs from
			// the one requested.
			if (enable != (this.logWriter != null)) {

				if (enable) {
					this.logWriter = new PrintWriter(System.out, true);
				}
				else {
					// DON'T close the log writer, since that also closes
					// the underlying print stream, e.g. System.out.
					this.logWriter = null;
				}
			}
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

/** Interface for return of CAS data to host HTML/JS environment. */
	public static interface CASDispatch {
		void returnCAS(String cas);
	}
}
