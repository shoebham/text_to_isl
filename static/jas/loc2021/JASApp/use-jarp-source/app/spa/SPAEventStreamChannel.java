/*
 * SPAEventStreamChannel.java		2008-08-25
 */
package app.spa;


import app.util.SynchBufSingleProd;
import app.util.SynchBufMultiProd;
import app.util.UseJLogger;

import app.spa.SPAEvent;
import app.spa.SPAEventAck;
import app.spa.SPAEventTarget;
import app.spa.SPAEventStreamSource;


/** Synchronous SPA event stream channel, acting both as a
 * {@link SPAEventTarget} and as a {@link SPAEventStreamSource}.
 * This is effectively a zero-slot channel connecting one or more event
 * source(s) (clients) to a single event target (server):
 * a new event cannot be accepted
 * (via the channel's {@link SPAEventTarget} implementation) before
 * the acknowledgement for its predecessor has been delivered
 * (via the channel's {@link SPAEventStreamSource} implementation).
 */
public class SPAEventStreamChannel
implements SPAEventTarget, SPAEventStreamSource {

/** Synchronized event buffer for this channel. */
	private final SynchBufMultiProd<SPAEvent>		EVENT_BUF;

/** Synchronized event acknowledgement buffer for this channel. */
	private final SynchBufSingleProd<SPAEventAck>	ACK_BUF;

/** The logger used by this channel. */
	private final UseJLogger							LOGGER;


/** Constructs a new event channel, using the given logger. */
	public SPAEventStreamChannel(UseJLogger lggr) {

		this.LOGGER = lggr;
		this.EVENT_BUF = new SynchBufMultiProd<SPAEvent>();
		this.ACK_BUF = new SynchBufSingleProd<SPAEventAck>();
	}

/** Posts the given event to this channel, blocking if necessary until
 * it is ready to accept it, before obtaining and returning the
 * corresponding acknowledgment.
 */
	public SPAEventAck postEvent(SPAEvent evt) {

		SPAEventAck ack = null;
		try {
			this.EVENT_BUF.put(evt);
			ack = this.ACK_BUF.get();
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

		SPAEvent evt = this.EVENT_BUF.get();
		this.LOGGER.log(evt.toString());

		return evt;
	}

/** Delivers the given acknowledgement in response to the most recently
 * obtained player event.
 */
	public void deliverAck(SPAEventAck ack) throws InterruptedException {

		this.ACK_BUF.put(ack);
		this.LOGGER.log(ack.toString());
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
