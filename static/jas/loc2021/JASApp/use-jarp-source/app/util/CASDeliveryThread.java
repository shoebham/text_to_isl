/*
 * CASDeliveryThread.java		2012-03-21
 */
package app.util;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;


import app.SToCApplet.SToCALogger;

import app.util.SToC;

import static app.util.SToC.CASDataReceiver;
import static app.util.SToCThread.CASDataDelivery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;

/** A thread that provides a delivery mechanism for a sequence of CAS
 * chunks, providing a suitable means for a {@link SToC} CAS data
 * generator to deliver the results of an individual SiGML-to-CAS
 * request.
 */
public class CASDeliveryThread extends Thread implements CASDataReceiver {

/** Logger. */
	private static final Logger					logger = LogManager.getLogger();

	private static final String					CDT_PFX = "####  CASDeliveryThread: ";

	private static final String					CAS_QUEUE_TERMINATOR =
												"@@__C_Q_DONE__@@";
	// For testing.
	private static final boolean				DO_SUPPRESS_DELIVERY = false;


/** This thread's queue of CAS data chunks, to be filled by its
 * client {@link SToC} CAS generator, and drained by thi thread itself.
 */
	private final BlockingQueue<String>			CAS_CHUNK_QUEUE;
/** The external CAS sink to which this thread delivers the chunks
 * it receives from its generator.
 */
	private final CASDataDelivery				CAS_SINK;

	private final SToCALogger					LOGGER;


	public CASDeliveryThread(CASDataDelivery cassink, SToCALogger logger) {

		this.CAS_SINK = cassink;
		// The queue can be small: if the receiver cannot process the
		// results fast enough, then there's no harm in blocking
		// the producer at this end.
		// Oh yes there is as it may stop another SToCA request being processed.
		// Trid size 40 but seemed to starve animation queue downstream.
		this.CAS_CHUNK_QUEUE = new ArrayBlockingQueue<String>(8);

		this.LOGGER = logger;

		// Start this thread without further ado.
		this.start();
	}

///** Used for testing: returns the JSON sign index of the given chunk
// * if the chunk is JSON and has an index, returns -1 otherwise.
// */
//	private int getSignIndex(String chunk) {
//		//{ "signIndex" : NUMBER,
//		int si = -1;
//		final String SPFX = "{ \"signIndex\" : ";
//		if (chunk.startsWith(SPFX)) {
//			int lo = SPFX.length();
//			int hi = chunk.indexOf(',', lo);
//			si = Integer.parseInt(chunk.substring(lo, hi));
//		}
//		return si;
//	}

/** Posts the given CAS data chunk onto this thread's input queue. */
	public void postNextCASChunk(String caschunk) {

		if (! DO_SUPPRESS_DELIVERY) {
			try {
				logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker,
					"postNextCASChunk");
				this.CAS_CHUNK_QUEUE.put(caschunk);
			}
			catch (InterruptedException ix) {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
					"postNextCASChunk interrupted: "+ix);
				Thread.currentThread().interrupt();
			}
		}
	}

/** Indicates the completion of CAS generation, that is, that all
 * CAS data chunks have now been posted onto this thread's input queue.
 */
	public void terminateCASSequence() {

		// Must pass this particular string reference.
		logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker,
			"terminateCASSequence");
		this.postNextCASChunk(CAS_QUEUE_TERMINATOR);
	}

/** This thread's main processing method: takes successive CAS data
 * chunks from the queue and posts them (synchronously) to the
 * thread's receiver.
 */
	public void run(){

		logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,
			"Run: CASDeliveryThread");

		if (! DO_SUPPRESS_DELIVERY) {
			try {
				String caschunk = this.CAS_CHUNK_QUEUE.take();
				// Use reference equality test.
				while (caschunk != this.CAS_QUEUE_TERMINATOR) {
					this.deliverCASChunk(caschunk);
					caschunk = this.CAS_CHUNK_QUEUE.take();
				}
			}
			catch (InterruptedException ix) {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
					"CAS Delivery thread interrupted: "+ix);
				Thread.currentThread().interrupt();
			}
		}

		logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,
			"End: CASDeliveryThread");
	};

/** Delivers the given CAS data chunk to this thread's receiver. */
	protected void deliverCASChunk(String caschunk) {

		if (! DO_SUPPRESS_DELIVERY) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.STOCAMarker,
				"deliverCASChunk");
			this.CAS_SINK.deliverCAS(caschunk);
		}
	}

//	protected final void log(String msg)  { this.LOGGER.log(msg); }
//	protected final void logp(String msg) { this.logb(CDT_PFX+msg); }
//	protected final void logb(String msg) { this.LOGGER.logb(msg); }
}
