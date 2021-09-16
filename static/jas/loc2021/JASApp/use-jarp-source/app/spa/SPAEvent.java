/*
 * SPAEvent.java		2008-08-23
 */
package app.spa;


import app.spa.EventId;


/** SPA event descriptor. */
public final class SPAEvent {

/** The event kind. */
	public final EventId	KIND;
/** String parameter for this event. */
	public final String		PARAM;
/** Boolean parameter for this event. */
	public final boolean	FLAG;
/** List of integer parameter values for this event, may be {@code null}. */
	public final int[]		INTS;

/** Constructs a new event of the given kind, with default parameter values. */
	public SPAEvent(EventId kind) {
		this(kind, null);
	}

/** Constructs a new event of the given kind, with the given string
 * parameter value, and default values for the remaining parameters.
 */
	public SPAEvent(EventId kind, String param) {
		this(kind, param, false, null);
	}

/** Constructs a new event of the given kind, with the given boolean
 * parameter value, and default values for the remaining parameters.
 */
	public SPAEvent(EventId kind, boolean flag) {
		this(kind, null, flag, null);
	}

/** Constructs a new event of the given kind, with the given string,
 * boolean, and integer list parameter values.
 */
	public SPAEvent(EventId kind, String param, boolean flag, int[] ints) {
		this.KIND = kind;
		this.PARAM = param;
		this.FLAG = flag;
		this.INTS = ints;
	}
/** Returns this event's kind as a string. */
	public String kindTag()			{ return this.KIND.toString(); }

/** Returns a legible text representation of this event. */
	public String toString() {
		StringBuilder sbuf = new StringBuilder(32);
		sbuf.append("event ").append(this.KIND);
		if (this.PARAM != null) { sbuf.append(" ").append(this.PARAM); }
		if (this.FLAG) { sbuf.append(" true"); }
		if (this.INTS != null) {
			String ipfx = " { ";
			for (int i : this.INTS) {
				sbuf.append(ipfx).append(i);
				ipfx = " ";
			}
			sbuf.append(" }");
		}
		return sbuf.toString();
	}
}
