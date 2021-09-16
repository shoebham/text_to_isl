/*
 * SynchBufSingleProd.java		2008-08-23
 */
package app.util;


/** A synchronized buffer with a generic buffered item type,
 * for the use of a single producer thread and a single consumer thread.
 * The producer uses the {@link #put(Object)} method to insert an
 * item, and the consumer uses the {@link #get()} method
 * to extract an item.  The transfer of an item from producer to
 * consumer using these operations is a completely synchronized distributed
 * assignment, i.e. it's a rendezvous with a data exchange:
 * the exchange is perfomed only when both partners
 * are available, and each partner's transfer operation completes only
 * after the exchange itself is completed.
 */
public class SynchBufSingleProd<E> {

	private final Token			getToken;
	private final Token			putToken;
	private E					buf;

/** Constructs a new synchronized buffer. */
	public SynchBufSingleProd() {

		this.putToken = Token.newUnblockedToken();
		this.getToken = Token.newBlockedToken();
		this.buf = null;
	}

/** Producer side of a synchronized distributed exchange via this buffer. */
	public void put(E item) throws InterruptedException {

		this.putToken.acquire();
		this.insert(item);
		this.getToken.release();
	}

/** Consumer side of a synchronized distributed exchange via this buffer. */
	public final E get() throws InterruptedException {

		this.getToken.acquire();
		E item = this.extract();
		this.putToken.release();
		return item;
	}

	private synchronized void insert(E item) {

		this.buf = item;
	}

	private synchronized E extract() {

		E item = this.buf;
		this.buf = null;
		return item;
	}
}
