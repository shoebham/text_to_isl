/*
 * SimplerSPAEventStreamChannel.java		2008-09-12
 */
package app.spa;


import app.util.Token;
import app.util.UseJLogger;

import app.spa.SPAEvent;
import app.spa.SPAEventAck;
import app.spa.SPAEventTarget;
import app.spa.SPAEventStreamSource;


/** A simpler version of {@link SPAEventStreamChannel}, implementing
 * exactly the same behavioural spec, but doing so using
 * synchronization {@link app.util.Token}s directly rather than indirectly
 * wrapped up in {@link app.util.SynchBufMultiProd}s and 
 * {@link app.util.SynchBufSingleProd}s.
 * <p>
 * ("Simpler" here refers to the synchronization apparatus, rather than
 * to the code which is acually a bit more complex than that of
 * {@link SPAEventStreamChannel}, due to the need for explicit
 * synchronization code in this case.)
 */
public class SimplerSPAEventStreamChannel
implements SPAEventTarget, SPAEventStreamSource {


/** Event buffer for this channel. */
	private SPAEvent				eventBuf;
/** Event acknowledgement buffer for this channel. */
	private SPAEventAck				ackBuf;
/** Mutual exclusion token for post-event operations by this channel's sources. */
	private final Token				POST_MUTEX;
/** Token controlling ack-buffer-get operations by this channel's sources. */
	private final Token				ACK_GET_TOKEN;
/** Token controlling event-buffer-get operations by this channel's target. */
	private final Token				EVENT_GET_TOKEN;
/** The logger used by this channel. */
	private final UseJLogger			LOGGER;


/** Constructs a new event channel, using the given logger. */
	public SimplerSPAEventStreamChannel(UseJLogger lggr) {

		this.LOGGER = lggr;
		this.eventBuf = null;
		this.ackBuf = null;
		this.POST_MUTEX = Token.newUnblockedToken();
		this.ACK_GET_TOKEN = Token.newBlockedToken();
		this.EVENT_GET_TOKEN = Token.newBlockedToken();
	}

/** Posts the given event to this channel, blocking if necessary until
 * it is ready to accept it, before obtaining and returning the
 * corresponding acknowledgment.
 */
	public SPAEventAck postEvent(SPAEvent evt) {

		SPAEventAck ack = null;
		try {
			this.POST_MUTEX.acquire();

			// Event put phase.
			this.eventBuf = evt;
			this.EVENT_GET_TOKEN.release();

			// Ack get phase.
			this.ACK_GET_TOKEN.acquire();
			ack = this.ackBuf;
			this.ackBuf = null;

			this.POST_MUTEX.release();
		}
		catch (InterruptedException ix) {
			ack = new SPAEventAck("SPA event channel postEvent(): "+ix);
		}
		return ack;
	}

/** Posts a new event with the given id to this channel, blocking if
 * necessary until it is ready to accept it, before obtaining and
 * returning the corresponding acknowledgment.
 */
	public SPAEventAck postEvent(EventId eid) {

		return this.postEvent(new SPAEvent(eid));
	}

/** Obtains and returns the most recently posted player event, blocking
 * if necessary until it becomes available.
 */
	public SPAEvent getNextEvent() throws InterruptedException {

		this.EVENT_GET_TOKEN.acquire();
		SPAEvent evt = this.eventBuf;
		this.eventBuf = null;

		this.LOGGER.log(evt.toString());

		return evt;
	}

/** Delivers the given acknowledgement in response to the most recently
 * obtained player event.
 */
	public void deliverAck(SPAEventAck ack) throws InterruptedException {

		this.LOGGER.log(ack.toString());

		this.ackBuf = ack;
		this.ACK_GET_TOKEN.release();
	}

/** Delivers an OK acknowledgement. */
	public void deliverOKAck() throws InterruptedException {

		this.deliverAck(SPAEventAck.okAck());
	}

/** Delivers a new failure acknowledgement with the given descriptive text. */
	public void deliverAck(String ackmsg) throws InterruptedException {

		this.deliverAck(new SPAEventAck(ackmsg));
	}
}
