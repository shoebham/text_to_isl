/*
 * SPAEventAck.java		2008-08-23
 */
package app.spa;


/** SPA event acknowledgement descriptor. */
public final class SPAEventAck {

/** Success/failure (==true/false) flag for this acknowledgement. */
	public final boolean	OK;
/** Descriptive message text for a failure acknowledgement. */
	public final String		MESSAGE;

/** Constructs a new success acknowledgment. */
	public SPAEventAck()					{ this(true); }

/** Constructs a new acknowledgment with the given success/failure
 * flag and a {@code null} descriptive text.
 */
	public SPAEventAck(boolean ok)			{ this(ok, null); }

/** Constructs a new failure acknowledgment with the given message text. */
	public SPAEventAck(String msg)			{ this(false, msg); }

/** Constructs a new failure acknowledgment with the given
 * success/failure flag and message text.
 */
	public SPAEventAck(boolean ok, String msg) {
		this.OK = ok;
		this.MESSAGE = msg;
	}

/** Returns a legible text representation of this acknowledgement. */
	public String toString() {
		return
			this.OK ?
				"ack OK" :
			this.MESSAGE == null ?
				"ack FAIL" :
				"ack FAIL: "+this.MESSAGE;
	}

/** Returns the OK event acknowledgement. */
	public static final SPAEventAck okAck() {
		return OK_ACK;
	}

/** OK acknowledgement. */
	private static final SPAEventAck		OK_ACK = new SPAEventAck();
}
