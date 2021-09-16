/*
 * SPAEventStreamSource.java		2008-08-25
 */
package app.spa;


import app.spa.SPAEvent;
import app.spa.SPAEventAck;


/** Interface defining a source from which successive items in an
 * {@link SPAEvent} stream can be obtained, and via which
 * an {@link SPAEventAck} can be delivered in response.
 */
public interface SPAEventStreamSource {

/** Obtains the next event from this source, waiting if necessary until
 * it becomes available.
 */
	SPAEvent getNextEvent() throws InterruptedException;

/** Delivers the given acknowledgement in response to the event
 * most recently obtained via {@link #getNextEvent()},
 * waiting if necessary until it can be accepted.
 */
	void deliverAck(SPAEventAck ack) throws InterruptedException;

/** Delivers an OK acknowledgement. */
	void deliverOKAck() throws InterruptedException;

/** Delivers a new failure acknowledgement with the given descriptive
 * text in response to the event most recently obtained via
 * {@link #getNextEvent()}, waiting if necessary until it can be accepted.
 */
	void deliverAck(String ackmsg) throws InterruptedException;
}
