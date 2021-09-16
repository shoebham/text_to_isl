/*
 * Token.java		2008-08-23
 */
package app.util;


/** A synchronization Token is essentially a binary semaphore,
 * with {@link #acquire()} as the potentially blocking P() operation,
 * and {@link #release()} as the potentially unblocking V() operation.
 */
public final class Token {

/** "Token-allocated" flag: {@code true} is equivalent to a semaphore
 * value of 0, and {@code false} to a semaphore value of 1.
 */
	private boolean		isAllocated;

/** Constructs a new token, initially free or blocked as specified
 * by the given flag value.
 */
	private Token(boolean free) {
		this.isAllocated = ! free;
	}

/** Acquires this token, blocking if necessary until it is available. */
	public final synchronized void acquire() throws InterruptedException {
		while (this.isAllocated) {
			this.wait();
		}
		this.isAllocated = true;
	}

/** Releases this token, possibly unblocking a thread currently
 * attempting to {@link #acquire()} it.
 */
	public final synchronized void release() throws IllegalStateException {
		if (! this.isAllocated) {
			throw new IllegalStateException(
				"Release cannot be applied to an unallocated token.");
		}
		this.isAllocated = false;
		this.notifyAll();
	}

/** Creates and returns a new token, initially "blocked", i.e. allocated. */
	public static final Token newBlockedToken() {
		return new Token(false);
	}

/** Creates and returns a new token, initially "unblocked", i.e. unallocated. */
	public static final Token newUnblockedToken() {
		return new Token(true);
	}
}
