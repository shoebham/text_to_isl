/*
 * SToCThread.java		2011-11-30
 */
package app.util;


import jautil.JAOptions;

import app.util.SToC;


import app.util.CASDeliveryThread;

import static app.util.SToC.CASDataReceiver;

import static app.SToCApplet.CASDispatch;
import static app.SToCApplet.SToCALogger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;



/** SiGML-to-CAS thread, providing support for the {@link app.SToCApplet}.
 */
public class SToCThread extends Thread {

/** Logger. */
	private static final Logger			logger = LogManager.getLogger();

	private static final String			STOC_T_PFX = "####  SToCThread: ";

	private static SToCThread			stocThread;

	public static SToCThread startTheThread(
		JAOptions jaopts, SToCALogger Slogger) {

		if (stocThread != null) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.THREADMarker,
				"startTheThread: thread already exists");
		}
		else {
			stocThread = new SToCThread(jaopts, Slogger);
			stocThread.start();
		}

		return stocThread;
	}

	private boolean					killed;

	// Data for synchronized task buffering.
	private boolean					putNextTaskOK;
	private boolean					getNextTaskOK;
	private TaskDesc				nextTask;

	private final SToCALogger		LOGGER;
	private final SToC				S_TO_C;


/** (Privately) constructs a new SiGML-to-CAS thread using the
 * given options settings and logger.
 */
	private SToCThread(JAOptions jaopts, SToCALogger Slogger) {

		super();

		this.LOGGER = Slogger;
		this.S_TO_C = new SToC(jaopts, Slogger);

		this.killed = false;

		this.nextTask = null;
		this.putNextTaskOK = false;
		this.getNextTaskOK = false;
	}

/** Defines this thread's action -- repeatedly obtaining and
 * processing the next S-to-C task.
 */
	public void run() { //xyz

		logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: SToCThread");

		TaskDesc task = this.getNextTask();
		while (!task.DO_KILL) {
			// FIXME: cut out the STOP stuff, or first specify it
			// properly and then implement that spec.
			if (!task.DO_STOP) {
				this.processSToCRequest(task.REQUEST);
			}
			task = this.getNextTask();
		}

		logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: SToCThread");
	}

/** Posts a kill task to this thread -- asynchronously, allowing an
 * immediate return.
 */
	public void kill() {
		this.killed = true;
		// Do the kill asynchronously, allowing us to return
		// (almost) immediately.
		final Thread T_KILL = new Thread() {
			public void run() { //xyz
				logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: SToC Kill");
				SToCThread.this.setKillTask();
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: SToC Kill");
			}
		};
		T_KILL.start();
	}

/** Accepts a CAS generation request for the given SiGML URL and
 * avatar, attaches the given {@link CASDispatch} to the request as
 * its associated results target, and posts the request to the
 * SiGML-to-CAS processing thread, waiting if necessary until that
 * thread is ready to accept it.
 */
	public void requestSiGMLURLToCAS(
		String sigmlurl, String avatar, String casfmt, CASDispatch casback) {

		SToCRequest rqst =
			new SURLToCRequest(sigmlurl, avatar, casfmt, casback);
		this.setNextTask(new TaskDesc(rqst), "s(u)-to-c");
	}

/** Accepts a CAS generation request for the given SiGML text and
 * avatar, attaches the given {@link CASDispatch} to the request as
 * its associated results target, and posts the request to the
 * SiGML-to-CAS processing thread, waiting if necessary until that
 * thread is ready to accept it.
 */
	public void requestSiGMLTextToCAS(
		String sigml, String avatar, String casfmt, CASDispatch casback) {

		SToCRequest rqst =
			new STextToCRequest(sigml, avatar, casfmt, casback);
		this.setNextTask(new TaskDesc(rqst), "s(t)-to-c");
	}

/** Performs a SiGML-to-CAS conversion as specified by the given
 * request, returning the generated CAS text, or {@code null} if
 * the conversion fails.
 */
	protected void processSToCRequest(SToCRequest rqst) {

		final CASDataReceiver CAS_RECEIVER =
			new CASDeliveryThread(rqst, LOGGER);

		if (rqst.SIGML_URL != null) {
			this.S_TO_C.sigmlURLToCAS(
				rqst.SIGML_URL, rqst.AVATAR, rqst.CAS_FORMAT, CAS_RECEIVER);
		}
		else {
			this.S_TO_C.sigmlTextToCAS(
				rqst.SIGML_TEXT, rqst.AVATAR, rqst.CAS_FORMAT, CAS_RECEIVER);
		}
	}

/** Gets the next task (S-to-C conversion, or Kill), waiting until it
 * becomes available.  This method supports the main S-to-C processing
 * thread.
 */
	protected synchronized TaskDesc getNextTask() {

		TaskDesc task = null;

		// Allow the next task to be posted into the buffer.
		this.putNextTaskOK = true;
		this.notify();
		try {
			// Wait for the next task to be posted.
			while (!this.getNextTaskOK) { this.wait(); }
			this.getNextTaskOK = false;
			// Consume the newly arrived task.
			task = this.nextTask;
			this.nextTask = null;
		}
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"getNextTask: unexpected interrupt"+ix);
			// Treat interrupt as kill.
			task = new TaskDesc();
			Thread.currentThread().interrupt();
		}

		logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker,
			"getNextTask: Done");
		return task;
	}

/** Flags this thread as killed, and posts a Kill task to it, receipt
 * of which should cause the thread to terminate.
 */
	protected synchronized void setKillTask() {

		// This should have been set already, but do it synchronously
		// now to make sure.
		this.killed = true;

		// Post a Kill task.
		this.setNextTask(new TaskDesc(), "kill");
	}

/** Posts the given task, with the given descriptor string, to the
 * S-to-C processing thread, waiting if necessary until the thread
 * becomes non-busy.
 * This method is designed to support a single request at a time, e.g.
 * requests all made from a single client thread; that is, multiple
 * concurrent asynchronous client threads are not supported.
 * If the given task is a standard S-to-C request and a Kill task
 * is received while this method is waiting, then that Kill task will
 * overtake the given one.
 */
	protected synchronized void setNextTask(TaskDesc task, String kind) {

		this.nextTask = null;
		while (this.nextTask != task) {

			// Wait until the processing thread is ready for the next task.
			try { while (! this.putNextTaskOK) { this.wait(); } }
			catch (InterruptedException ix) {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
					"setNextTask: "+kind+": "+ix);
				Thread.currentThread().interrupt();
			}

			// Allow a kill request to overtake the given task -- which
			// we expect then to become the victim of an interruption
			// (on the second and final iteration).
			this.nextTask =
				(this.killed && !task.DO_KILL ? new TaskDesc() : task);

			// Post the next task, and allow the thread to read it.
			this.getNextTaskOK = true;
			this.notify();
		}
		logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker,
			"setNextTask: Done");
	}

//	private final void log(String msg)  { this.LOGGER.log(msg); }
//	private final void logp(String msg) { this.LOGGER.logb(STOC_T_PFX+msg); }

/** Task Descriptor for the SiGML-to-CAS thread. */
	protected static class TaskDesc {

		public final boolean		DO_KILL;
		public final boolean		DO_STOP;
		public final SToCRequest	REQUEST;

		public TaskDesc() { this(true, false); }
		public TaskDesc(boolean s) { this(false, s); }
		public TaskDesc(SToCRequest rqst) { this(false, false, rqst); }

		private TaskDesc(boolean k, boolean s) { this(k, s, null); }
		private TaskDesc( boolean k, boolean s, SToCRequest rqst) {
			this.DO_KILL = k;  this.DO_STOP = s;  this.REQUEST = rqst;
		}
		public String toString() {
			return this.DO_KILL?"KILL":this.DO_STOP?"STOP":"REQUEST";
		}
	}

/** SiGML-to-CAS Request descriptor and scheduler. */
	protected class SToCRequest implements CASDataDelivery {

		public final String				SIGML_URL;
		public final String				SIGML_TEXT;
		public final String				AVATAR;
		public final String				CAS_FORMAT;
		public final CASDispatch			CAS_BACK;

		public SToCRequest(
			String sigmlu, String sigmlt, String av,
			String casfmt, CASDispatch casd) {
			this.SIGML_URL = sigmlu;
			this.SIGML_TEXT = sigmlt;
			this.AVATAR = av;
			this.CAS_FORMAT = casfmt;
			this.CAS_BACK = casd;
		}
		public void deliverCAS(final String CAS) {
			this.CAS_BACK.returnCAS(CAS);
		}
		// private final void logp(String msg) { SToCThread.this.logp(msg); }
	}

/** SiGML-URL-to-CAS Request descriptor and scheduler. */
	protected class SURLToCRequest extends SToCRequest {
		public SURLToCRequest(
			String sigmlu, String av, String casfmt, CASDispatch casd) {
			super(sigmlu, null, av, casfmt, casd);
		}
	}

/** SiGML-Text-to-CAS Request descriptor and scheduler. */
	protected class STextToCRequest extends SToCRequest {
		public STextToCRequest(
			String sigmlt, String av, String casfmt, CASDispatch casd) {
			super(null, sigmlt, av, casfmt, casd);
		}
	}

/** Interface for CAS data delivery. */
	public static interface CASDataDelivery {
		/** Delivers a chunk of CAS data -- which may or may not be
		 * a complete CAS animation, and which may be XML or may be
		 * JSON.
		 */
		void deliverCAS(String casdata);
	}
}
