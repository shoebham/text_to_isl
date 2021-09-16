/*
 * SynchBufMultiProd.java		2008-09-12
 */
package app.util;


/** A synchronized buffer with a generic buffered item type,
 * for the use of one or more producer threads and a single consumer thread.
 * This differs from its superclass {@link SynchBufSingleProd} only in
 * the extra synchronization it applies to the {@link #put(Object)} method
 * in order to impose mutual exclusion on the producer threads' access
 * to the buffer.
 */
public final class SynchBufMultiProd<E> extends SynchBufSingleProd<E> {

	private final Token			prodToken;

/** Constructs a new multi-producer synchronized buffer. */
	public SynchBufMultiProd() {

		super();
		this.prodToken = Token.newUnblockedToken();
	}

/** Producer side of a synchronized distributed exchange via this buffer. */
	public final void put(E item) throws InterruptedException {

		this.prodToken.acquire();
		super.put(item);
		this.prodToken.release();
	}
}
