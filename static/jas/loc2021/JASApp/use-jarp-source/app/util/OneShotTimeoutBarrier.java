/*
 * OneShotTimeoutBarrier.java		2008-08-27
 */
package app.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;

/** A {@code OneShotTimeoutBarrier}, like a {@link Token}, is a form
 * of binary semaphore, but with two differences: (a) the
 * {@link #passWhenOpen(long)}/P() operation has a timeout, and (b) it is
 * "one-shot" in that the {@link #passWhenOpen(long)} method should be
 * called by a single thread on a single occasion only.
 */
public final class OneShotTimeoutBarrier {

/** Logger. */
	private static final Logger		logger = LogManager.getLogger();

/** Creates and returns a new one-shot barrier. */
	public static final OneShotTimeoutBarrier newBarrier() {

		return new OneShotTimeoutBarrier();
	}

/** Flag indicating that this barrier has been opened by
 * an invocation of {@link #open()} on it.
 */
	private boolean			isOpen;
/** Flag indicating that this barrier's {@link #passWhenOpen(long)}
 * operation has been timed out.
 */
	private boolean			timedOut;

	private TimeoutThread	timeoutThread;

/** Constructs a new as-yet unreleased barrier.
 */
	private OneShotTimeoutBarrier() {

		this.isOpen = false;
		this.timedOut = false;
	}

/** Acquires the permission to proceed represented by this barrier,
 * blocking if necessary until it is opened by another thread
 * or until the specified timeout has elapsed (more or less), and
 * returns {@code true} if and only if the barrier is open.
 */
	public final synchronized boolean passWhenOpen(long timeout)
	throws InterruptedException {

		if (! this.isOpen) {
			// NB
			// We cannot use this.wait(timeout) here, because
			// that call gives us no way of knowing what caused its
			// completion.
			logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
				"OneShotTimeoutBarrier: passWhenOpen launches TimeOut thread");
			this.launchTimeoutThread(timeout);
			while (! this.isOpen && ! this.timedOut) {
				this.wait();
			}
			// Stop the timeout thread
			logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
				"OneShotTimeoutBarrier: passWhenOpen cancelling TimeOut thread");
			if (! this.timedOut) {
				this.timeoutThread.cancelTimeout();
			}
		}
		logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
			"OneShotTimeoutBarrier: passWhenOpen yeilds isOpen="+this.isOpen+" timedOut="+this.timedOut);

		return this.isOpen;
	}

/** Opens this barrier provided it has not already timed out; if a thread
 * is currently blocked on {@link #passWhenOpen(long)}, that thread will
 * be unblocked as a result.
 */
	public final synchronized void open() {

		logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
		"OneShotTimeoutBarrier: open called with timedOut="+this.timedOut);

		if (! this.timedOut) {
			// Would it hurt to always mark it open?
			// Probably not but it would mean barrier is already passed
			this.isOpen = true;
			this.notify();
		}
	}

/** Times out the the (only) {@link #passWhenOpen(long)} call on
 * this barrier, provided the barrier has not been opened since that
 * call started.
 */
	protected final synchronized void doTimeOut() {

		if (! this.isOpen) {
			logger.log( LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
				"OneShotTimeoutBarrier: doTimeOut required");
			this.timedOut = true;
			this.notify();
		}
		else {
			logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
				"OneShotTimeoutBarrier: doTimeOut no longer required");
			// System.out.println(
			//	"####  (OneShotTimeoutBarrier: timeout attempted, but too late.)");
		}
	}

/** Creates and launches the time out thread for this barrier's (only)
 * {@link #passWhenOpen(long)} call, using the given time out period
 * in ms.
 */
	protected final void launchTimeoutThread(long timeout) {

		this.timeoutThread = new TimeoutThread(timeout);
		timeoutThread.start();
	}

/** Time out thread. */
	private final class TimeoutThread extends Thread {
		private final long	TIMEOUT;
		// private Thread theThread = null;
		
	/** Constructs a new timeout thread with the given timeout duration, in ms. */
		public TimeoutThread(long timeout) {
			this.TIMEOUT = timeout;
		}
	/** Standard {@code Runnable} interface method for the timeout thread. */
		public void run() {
			double t_actual = -1;
			Thread.currentThread().setName("Timeout");
			logger.log( LoggerConfig.LOGLevel, LoggerConfig.THREADMarker,
				"Run: OneShotTimeoutBarrier TimeoutThread");
			if (OneShotTimeoutBarrier.this.isOpen) {
				logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
					"OneShotTimeoutBarrier: TimeoutThread run when already open");
			} else {
				// this.theThread = Thread.currentThread();
				try {
					long t0 = System.nanoTime();
					Thread.sleep(this.TIMEOUT);
					long t1 = System.nanoTime();
					t_actual = (double)(t1 - t0) / (1000 * 1000);
					OneShotTimeoutBarrier.this.doTimeOut();
					logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
						"OneShotTimeoutBarrier: Timeout t="+t_actual+"ms");
					//System.out.println("####  Timeout  t="+t_actual+"ms");
				}
				catch (InterruptedException ix) {
					logger.log( LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
						"OneShotTimeoutBarrier: TimeoutThread: "+ix);
					//System.out.println("####  Timeout thread: "+ix);
				}
				// this.theThread = null;
			}
			logger.log( LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,
				"End: OneShotTimeoutBarrier TimeoutThread");
		}
	
	/** Method intended to cancel timeout. Needs work! */
		public void cancelTimeout() {
			if (OneShotTimeoutBarrier.this.timeoutThread != null) {
				logger.log( LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
					"OneShotTimeoutBarrier: cancelTimeout: Thread " + OneShotTimeoutBarrier.this.timeoutThread.getName()
					+ " needs cancelling");
				OneShotTimeoutBarrier.this.timeoutThread.interrupt();
//				while (OneShotTimeoutBarrier.this.timeoutThread != null) {
//					try {
//						this.wait();
//					} catch (InterruptedException ix) {
//						logger.log( LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
//							"OneShotTimeoutBarrier: TimeoutThread cancelling: "+ix);
//					}
//				}
				logger.log( LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
					"OneShotTimeoutBarrier: cancelTimeout completed");
			}
		}
	}

//	public static void main(String[] args) throws InterruptedException {
//
//		final int TRLS = 7, TTO = 40;//8;
//
//		System.out.println("T-RELEASE="+TRLS+"  T-TIMEOUT="+TTO);
//		OneShotTimeoutBarrier barrier = new OneShotTimeoutBarrier();
//		launchReleaseThread(barrier, TRLS);
//	
//		boolean released = barrier.passWhenOpen(TTO);
//		barrier = null;
//	
//		String description = (released?"normal release":"timed out");
//		System.out.println("Pass barrier: "+description);
//		System.exit(0);
//	}
//
//	private static void launchReleaseThread(
//		final OneShotTimeoutBarrier BARRIER, final int TRLS) {
//		Thread t = new Thread() {
//			public void run() {
//				try { Thread.sleep(TRLS); }
//				catch (InterruptedException ix) { System.out.println(ix); }
//				BARRIER.open();
//			}
//		};
//		t.start();
//	}
}
