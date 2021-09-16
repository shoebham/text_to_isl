/*
 * SToC.java		2011-11-30
 */
package app.util;


import java.io.StringReader;
import java.io.StringWriter;


import jautil.JAOptions;
import jautil.JATimer;

import casxml.CASWriter;

import sigmlanim.AnimatedSign;
import sigmlanim.SiGMLAnimation;
import sigmlanim.StreamedAnimationLoader;

import player.SignsArrayAccess;


import app.util.JSONStreamGenFromAnim;


import static app.SToCApplet.SToCALogger;
import static app.SToCApplet.JSON_FORMAT;
import static app.SToCApplet.XML_FORMAT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** SiGML-to-CAS converter for the {@link app.SToCApplet}.
 */
public class SToC {

/** Logger. */
	private static final Logger			logger = LogManager.getLogger();

	private static final String			STOC_PFX = "####  SToC: ";

//	// Configuration flag -- JSON vs. XML output.
//	private static final boolean			DO_SEND_JSON_STREAM = true;

/** Options settings. */
	private final JAOptions				JA_OPTS;
/** Logger */
	private final SToCALogger			LOGGER;


	public SToC(JAOptions jaopts, SToCALogger logger) {

		this.JA_OPTS = jaopts;
		this.LOGGER = logger;
	}


	public void sigmlURLToCAS(
		String sigmlurl, String avatar, String casfmt, CASDataReceiver casrcvr) {

		String cas = null;
		SiGMLAnimation sa = this.sigmlURLToAnim(sigmlurl, avatar);

		if (casfmt.equalsIgnoreCase(JSON_FORMAT)) {
			final JSONStreamGenFromAnim JSGEN =
				new JSONStreamGenFromAnim(avatar, sa, casrcvr, this.LOGGER);
		}
		else if (casfmt.equalsIgnoreCase(XML_FORMAT)) {
			// JRWG: Ought to handle null SiGMLAnimation
			cas = this.animToCASString(avatar, sa);
			if (cas != null) {
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.STOCAMarker,
					"CAS length="+cas.length());
			} else {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
					"CAS gen failed");
			}
			// For CAS XML there is just the one chunk to be posted
			// to the receiver.
			casrcvr.postNextCASChunk(cas);
			casrcvr.terminateCASSequence();
		}
		else {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"Unknown CAS result format tag: "+casfmt);
		}
	}

	public void sigmlTextToCAS(
		String sigml, String avatar, String casfmt, CASDataReceiver casrcvr) {

		String cas = null;
		SiGMLAnimation sa = this.sigmlToAnim(sigml, avatar);

		if (casfmt.equalsIgnoreCase(JSON_FORMAT)) {
			final JSONStreamGenFromAnim JSGEN =
				new JSONStreamGenFromAnim(avatar, sa, casrcvr, this.LOGGER);
		}
		else if (casfmt.equalsIgnoreCase(XML_FORMAT)) {
			// JRWG: Ought to handle null SiGMLAnimation
			cas = this.animToCASString(avatar, sa);
			if (cas != null) {
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.STOCAMarker,
					"CAS length="+cas.length());
			} else {
				logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
					"CAS gen failed");
			}
			// For CAS XML there is just the one chunk to be posted
			// to the receiver.
			casrcvr.postNextCASChunk(cas);
			casrcvr.terminateCASSequence();
		}
		else {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"Unknown CAS result format tag: "+casfmt);
		}
	}

	protected SiGMLAnimation sigmlURLToAnim(String sigmlurl, String avatar) {

		if (this.JA_OPTS.getAvatarsEnv().isValidAvatar(avatar)) {
			final StreamedAnimationLoader SA_LOADER =
				new StreamedAnimationLoader(avatar, this.JA_OPTS, sigmlurl, null);
			SA_LOADER.processSiGML();

			return SA_LOADER.getAnimation();
		} else {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"Invalid avatar for SiGML URL: "+avatar);
			return null;
		}
	}

	protected SiGMLAnimation sigmlToAnim(String sigml, String avatar) {

		if (this.JA_OPTS.getAvatarsEnv().isValidAvatar(avatar)) {
			final StringReader SIGML_RDR = new StringReader(sigml);
			final StreamedAnimationLoader SA_LOADER =
				new StreamedAnimationLoader(avatar, this.JA_OPTS, SIGML_RDR, null);
			SA_LOADER.processSiGML();

			return SA_LOADER.getAnimation();
		} else {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"Invalid avatar for SiGML Text: "+avatar);
			return null;
		}
	}

	protected void ensureAnimationIsComplete(SiGMLAnimation anim) {

		final SignsArrayAccess SIGNS = anim.getSignsArray();

		// NB If something odd happens, this loop might become eternal.
		try {
			while (!SIGNS.arrayIsFinal()) {
				SIGNS.waitForProgress();
			}
		}
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"ensureAnimationIsComplete: "+ix);
		}
	}

/** Generates a CAS string for the given animation.
 */
	protected String animToCASString(String av, SiGMLAnimation anim) {

		String cas = null;

		this.ensureAnimationIsComplete(anim);

		JATimer tmr = new JATimer();

		final String MSG_PFX = "SToC.animToCASString(): ";
		try {
			StringWriter caswrtr = new StringWriter(16384);
			final AnimatedSign[] SIGNS =  anim.getSignsArray().signs();
			CASWriter.writeDocument(caswrtr, av, SIGNS);
			cas = caswrtr.toString();
			tmr.showTimeMS("####  CAS string generated.  t");
		}
		catch (CASWriter.CASWriterException cwx) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				MSG_PFX+cwx);
		}

		return cas;
	}

//	private final void log(String msg) { this.LOGGER.log(msg); }
//	private final void logp(String msg) { this.logb(STOC_PFX+msg); }
//	private final void logb(String msg) { this.LOGGER.logb(msg); }

/** Interface for delivery of a sequence of CAS data chunks. */
	public static interface CASDataReceiver {
	/** Posts the given CAS data chunk to this receiver. */
		void postNextCASChunk(String caschunk);
	/** Indicates the completion of CAS generation, that is, that all
	 * CAS data chunks have now been posted to this receiver.
	 */
		void terminateCASSequence();
	}
}
