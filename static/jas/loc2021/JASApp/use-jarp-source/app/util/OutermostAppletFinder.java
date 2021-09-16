/*
 * OutermostAppletFinder.java		2008-06-18
 */
package app.util;

import java.applet.Applet;

import java.util.Enumeration;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** This class embodies a crude hack, which attempts to identify
 * the parent JNLPAppletLauncher instance for a given applet.
 * This parent applet is needed when calling JSObject.getWindow()
 * under Firefox on Mac OS X; for some reason, other browser/platform
 * combinations seem to cope with the child applet, although strictly
 * speaking it is not one of the HTML page's applets.
 */
public class OutermostAppletFinder {

/** Logger. */
	private static final Logger	logger = LogManager.getLogger();

	public static final String	OAF_PREFIX = "OutermostAppletFinder: ";

	public static final String	LAUNCHER_NAME = "JNLPAppletLauncher";
	public static final String	LAUNCHER_SUFFIX = "AppletLauncher";
	public static final String	GET_SA_METHOD_NAME = "getSubApplet";

/** Searches the HTML applets of the given applet, i.e. those in its applet
 * context, and tries to find the one which either <em>is</em> the given
 * applet or is a JNLPAppletLauncher instance that has the given one as
 * its subapplet: returns this "outermost" applet if found, or
 * {@code null} otherwise.
 */
	public static Applet getOutermost(Applet applet) {

		Enumeration<Applet> htmlapplets =
			applet.getAppletContext().getApplets();

		int n = 0;	// counts the HTML applets;
		Applet outermost = null;	// the search result;
		while (outermost == null && htmlapplets.hasMoreElements()) {

			++ n;
			Applet htmlapplet = htmlapplets.nextElement();
			if (htmlapplet == applet) {

				outermost = htmlapplet;
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
					OAF_PREFIX+"Given applet is outermost.");
			}
			else {
				final Class<?> HA_CLASS = htmlapplet.getClass();
				final String HA_NAME = HA_CLASS.getName();

//				if (HA_NAME.endsWith(LAUNCHER_NAME)) {
				if (HA_NAME.endsWith(LAUNCHER_SUFFIX)) {

					try {
						Method getsa = HA_CLASS.getMethod(GET_SA_METHOD_NAME);
						Object subapplet = getsa.invoke(htmlapplet);

						if (subapplet == applet) {

							outermost = (Applet) htmlapplet;
							logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
								OAF_PREFIX+"Given applet is subapplet of "+HA_NAME);
						}
					}
					catch (NoSuchMethodException nsmx) {
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SESSIONMarker, OAF_PREFIX+nsmx);
					}
					catch (IllegalAccessException iax) {
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SESSIONMarker, OAF_PREFIX+iax);
					}
					catch (InvocationTargetException itx) {
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SESSIONMarker, OAF_PREFIX+itx);
					}
					catch (NullPointerException npx) {
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SESSIONMarker, OAF_PREFIX+npx);
					}
				}
			}
		}

		// We hope we never fail to find the required applet, but if
		// we do fail we log the failure here.
		if (outermost == null) {
			logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SESSIONMarker, 
				OAF_PREFIX+"Tried "+n+" HTML applet(s) -- all failed");
		}

		return outermost;
	}
}
