/*
 * QuitManager.java		2007-05-17
 */
package app.gui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** A quit manager allows an app to perform an orderly shut down in
 * response to any of several possible triggers.  At construction time
 * the owning app specifies a runnable defining the manager's fixed
 * "pre-quit" sequence.  The manager's public interface provides
 * several forms of event-handler each triggering the manager's
 * shut-down sequence.  The shut-down sequence is performed by
 * a separate thread, which runs the registered pre-quit sequence before
 * exiting. The manager ensures that at most one instance of the
 * shut-down thread is ever created and run by this manager.
 */
public class QuitManager {

/** Logger. */
	private static final Logger			logger = LogManager.getLogger();

/** Standard final delay value. */
	protected final static int			MIN_DELAY_MS = 50;
/** Standard final delay value. */
	protected final static int			STD_DELAY_MS = 200;
/** Runnable that defines this quit manager's pre-quit sequence. */
	protected final Runnable			PREPARE_FOR_QUIT;
/** Final delay value. */
	protected final int					FINAL_DELAY_MS;

/** App-quit thread for this player. */
	protected Thread					quitThread = null;

/** Constructs a new quit manager, using the pre-quit sequence
 * defined by the given runnable.
 */
	public QuitManager(Runnable prequit) {
		this(prequit, STD_DELAY_MS);
	}

	public QuitManager(Runnable prequit, int delayms) {

		this.PREPARE_FOR_QUIT = prequit;

		this.FINAL_DELAY_MS = Math.max(MIN_DELAY_MS, delayms);
		if (FINAL_DELAY_MS != STD_DELAY_MS) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.SESSIONMarker,
				"QuitManager: delay="+this.FINAL_DELAY_MS+"ms");
		}
	}

/** Returns a new runnable whose {@code run()} method performs
 * the quit sequence.
 */
	public Runnable getQuitRunnable() {

		return new Runnable() {
			public void run() { //xyz
				logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: doQuit");
				QuitManager.this.doQuit();
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: doQuit");
			}
		};
	}

/** Returns a new action listener that responds to the "action performed"
 * event by performing the quit sequence.
 */
	public ActionListener getQuitActionListener() {

		return new ActionListener() {
			public void actionPerformed(ActionEvent aevt) {
				QuitManager.this.doQuit();
			}
		};
	}

/** Returns a new window listener that responds to the "window closing"
 * event by performing the quit sequence.
 */
	public WindowListener getQuitWindowListener() {

		return new WindowAdapter() {
			public void windowClosing(WindowEvent wevt) {
				QuitManager.this.doQuit();
			}
		};
	}

/** Quit method for the controlling app -- creates and runs a
 * {@link QuitManager.QuitThread} instance if this has not already
 * been done.
 */
	protected synchronized void doQuit() {

		// Make sure we never create and run a second quit thread.
		if (this.quitThread == null) {
			// Use another thread than the AWT event dispatcher
			// thread to give the JA display shut-down sequence a chance
			// to complete before we exit.
			this.quitThread = new QuitThread();
			this.quitThread.start();
		}
	}

/** Quit Thread for the app that owns this quit manager. */
	protected class QuitThread extends Thread {
	/** Runs the outer class instance's shut-down sequence, then exits. */
		public void run() { //xyz
			logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: Quit Thread");
			QuitManager.this.PREPARE_FOR_QUIT.run();
			logger.log(LoggerConfig.INFOLevel, LoggerConfig.SESSIONMarker,
				"JASigning app/applet Quit done");
			// EXPERIMENTAL:
			// Put a small delay here, which may allow terminated
			// threads to reach more orderly conclusions.
			try {
				Thread.sleep(QuitManager.this.FINAL_DELAY_MS);
			}
			catch (InterruptedException ix) {
			}
			// This really is the end.
			logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"Die: Quit Thread: Calling System.exit(0)");
			System.exit(0);
		}
	};
}
