/*
 * UseJLogger.java		2008-08-25
 */
package app.util;


/** Message logging interface. */
public interface UseJLogger {

/** Outputs the given message to the log stream if logging is enabled. */
	void log(String msg);
/** Indicates if log output generation is currently enabled. */
	boolean logIsEnabled();
}
