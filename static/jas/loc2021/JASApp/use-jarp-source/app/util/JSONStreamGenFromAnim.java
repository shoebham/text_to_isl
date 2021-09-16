/*
 * JSONStreamGenForAnim.java		2012-03-20
 */
package app.util;


import java.util.Locale;


import cas.CASFrame;
import cas.CASMorph;
import cas.CASTRSet;

import jautil.JATimer;
import jautil.MapInt;

import sigmlanim.AnimatedSign;
import sigmlanim.SiGMLAnimation;

import player.SignsArrayAccess;


import app.util.SToC.CASDataReceiver;


import static jautil.FourCCUtil.fourCCString;

import static app.SToCApplet.SToCALogger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** Generator, and deliverer, of a stream of JSON chunks for a
 * SiGML animation.
 */
public class JSONStreamGenFromAnim {

/** Logger. */
	private static final Logger					logger = LogManager.getLogger();

	private static final String					JSGFA_PFX =
												"####  JSONStreamGenForAnim: ";

	private static final Locale					JSON_LOCALE = Locale.UK;

	private static final String					JSON_END_MARKER = "{ }";

	// For testing.
	private static final boolean				DO_SUPPRESS_DELIVERY = false;

	private final String						AVATAR;
	private final SiGMLAnimation				ANIM;
	private final CASDataReceiver				CAS_RECEIVER;

	private MapInt<CASTRSet>					currentBones;
	private int									sCount;
	private int									fCount;
	private StringBuilder						jsonBuf;

/** Logger */
//	private final SToCALogger					LOGGER;


	public JSONStreamGenFromAnim(
		String av, SiGMLAnimation anim, CASDataReceiver casrcvr,
		SToCALogger logger) {

		this.AVATAR = av;
		this.ANIM = anim;
		this.CAS_RECEIVER = casrcvr;
//		// Queue can be small: if the receiver cannot process the
//		// results fast enough, then there's no harm in blocking
//		// the producer at this end.
//		this.JSON_CHUNK_QUEUE = new ArrayBlockingQueue<String>(8);
//		this.LOGGER = logger;

		this.doJSONStreamGen();
	}

/** Generates a CAS string for the given animation.
 */
	protected void doJSONStreamGen() {

		this.sCount = 0;
		this.fCount = 0;
		this.jsonBuf = new StringBuilder(16 * 2048);
		this.currentBones = new MapInt<CASTRSet>();

		boolean all_signs_done = (this.ANIM == null);
		
		final SignsArrayAccess SA_ACCESS = all_signs_done ? null : this.ANIM.getSignsArray();

		try {
			while (!all_signs_done) {
				// Does the animation generator have one or more
				// uncollected signs?  Here, != is equivalent to < .
				if (this.sCount != SA_ACCESS.countSigns()) {
					// Process the next sign.
					this.doJSONGenForSign(SA_ACCESS.signs()[this.sCount]);
					++ this.sCount;
				}
				// Has the animation generator yet generated all the signs?
				else if (!SA_ACCESS.arrayIsFinal()) {
					// Wait until either at least one more sign has been
					// generated or the completion indicator is set.
					SA_ACCESS.waitForProgress();
				}
				else {
					// We've processed all signs: flag that fact.
					all_signs_done = true;
				}
			}
		}
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.STOCAMarker,
				"doJSONStreamGen "+ix.toString());
			this.completeJSONStreamGen();
		}

		this.completeJSONStreamGen();

		logger.log(LoggerConfig.INFOLevel, LoggerConfig.STOCAMarker,
			"doJSONStreamGen signs count="+this.sCount);
	}

	protected void completeJSONStreamGen() {

		this.CAS_RECEIVER.postNextCASChunk(JSON_END_MARKER);
		this.CAS_RECEIVER.terminateCASSequence();
		if (DO_SUPPRESS_DELIVERY) {
			System.out.println(JSON_END_MARKER);
		}
	}

	protected void doJSONGenForSign(AnimatedSign sign) {

		this.jsonBuf.setLength(0);
		this.jsonBuf.append("{ \"signIndex\" : ").append(this.sCount)
			.append(",  \"gloss\" : \"").append(sign.getGloss())
			.append("\",  \"baseFrameIndex\" : ").append(this.fCount)
			// Second line ...
			.append(",\n  \"frames\" : [");

		final CASFrame[] FRAMES = sign.getFrames();
		final int NF = FRAMES.length;

		String fpfx = "\n";
		for (int f=0; f!=NF; ++f) {
			String jframe =
				this.makeJSONFrame((this.sCount==0 && f==0), FRAMES[f]);
			this.jsonBuf.append(fpfx).append(jframe);
			fpfx = ",\n";
		}

		// End frames array and sign object.
		this.jsonBuf.append(NF==0?" ]":"\n  ]").append("\n}");

		String signjson = this.jsonBuf.toString();
		this.CAS_RECEIVER.postNextCASChunk(signjson);

		if (DO_SUPPRESS_DELIVERY) { this.dumpSignData(signjson); }

		this.fCount += NF;
	}

	protected String makeJSONFrame(boolean isfirst, CASFrame frm) {

		StringBuilder jfbuf = new StringBuilder(2048);

		jfbuf.append("    {\n");

		// Time, duration, and morphs.
		jfbuf.append(
			"      \"time\" : "+fStr(frm.getTime())+
			",  \"duration\" : "+fStr(frm.getDuration())+
			",  \"morphs\" : [");
		int nmorphs = 0;
		String mprefix = "\n";
		for (CASMorph morph : frm.getMorphs()) {
			final float AMT = morph.getAmount();
			if (AMT != 0) {
				jfbuf.append(mprefix).append("        ");
				jfbuf.append(morphDefStr(morph.getName(), AMT));
				mprefix = ",\n";
				++ nmorphs;
			}
		}
		jfbuf.append(nmorphs==0?" ],":"\n      ],");

		// Bones.
		jfbuf.append("\n      \"bones\" : [");
		int nbones = 0;
		String bprefix = "\n";
		for (CASTRSet bone : frm.getTRSets()) {
			final int B_NAME = bone.getFourCC();
			if (isfirst || this.boneRotIsChanged(bone)) {
				jfbuf.append(bprefix).append("        { ")
					.append(fourCCIDStr(B_NAME));
				if (isfirst) {
					// Include translation for initial frame.
					jfbuf.append(",  \"trans\" : ");
					appendFloatVec(bone.getTranslation(), jfbuf);
				}
				jfbuf.append(",  \"rot\" : ");
				appendFloatVec(bone.getRotation(), jfbuf);
				jfbuf.append(" }");
				// Update current bones map.
				this.currentBones.put(B_NAME, bone);
				bprefix = ",\n";
				++ nbones;
			}
		}
		jfbuf.append(nbones==0?" ]":"\n      ]").append("\n    }");

		return jfbuf.toString();
	}

	private final boolean boneRotIsChanged(CASTRSet bone) {

		boolean changed = true;

		final int B_NAME = bone.getFourCC();
		final CASTRSet OLD_BONE = this.currentBones.get(B_NAME);
		if (OLD_BONE != null) {
			final float[] OLD_ROT = OLD_BONE.getRotation();
			final float[] ROT = bone.getRotation();
			changed = ! CASTRSet.approxEqRots(OLD_ROT, ROT);
		}

		return changed;
	}

	private static final void appendFloatVec(float[] ff, StringBuilder sbuf) {

		String pfx = "[ ";
		for (float f : ff) {
			sbuf.append(pfx).append(fStr(f));
			pfx = ", ";
		}
		sbuf.append(" ]");
	}

	protected static final String morphDefStr(int m4cc, float amt) {

		return "{ "+fourCCIDStr(m4cc)+",  \"amount\" : "+fStr(amt)+" }";
	}

	protected static final String fourCCIDStr(int fourcc) {

		return "\"id4cc\" : \""+fourCCString(fourcc)+"\"";
	}

	protected static final String fStr(float f) {

		return fStr(f, (float)5e-5);
	}

	protected static final String fStr(float f, final float EPSILON) {

		// This is not completely trivial, because we use integer
		// format when the float value is sufficiently close
		// to its closest integer.
		return
			isApproxInt(f, EPSILON) ?
				iStr(f) : String.format(JSON_LOCALE, "%f", f);
	}

	protected static String iStr(float f)	{ return iStr(Math.round(f)); }
	protected static String iStr(int i)		{ return ""+i; }

	protected static boolean isApproxInt(float f) {

		return isApproxInt(f, (float)5e-5);
	}

	private static boolean isApproxInt(float f, final float EPSILON) {

		boolean isapint = false;

		final float ABS_F = Math.abs(f);
		final int INT_F = Math.round(f);
		final float DIFF = Math.abs(f - INT_F);

		if (ABS_F <= 1) {
			isapint = (DIFF < EPSILON);
		}
		else {
			final int ABS_INT_F = Math.abs(INT_F);
			isapint = (DIFF / ABS_INT_F < EPSILON);
		}

		return isapint;
	}

//	private final void log(String msg) { this.LOGGER.log(msg); }
//	private final void logp(String msg) { this.logb(JSGFA_PFX+msg); }
//	private final void logb(String msg) { this.LOGGER.logb(msg); }

	protected void dumpSignData(String signjson) {

		System.out.println("========  sign "+this.sCount+"  ========");
		final int N = signjson.length();
		int i = 0;
		while (i != N) {
			// Rigmarole to deal with limit on Java console trace
			// message length -- done by skipping the interior
			// of the JSON chunk.
			final int LIM = Math.min(N, i+5000);
			if (i == 0 || LIM == N) {
				System.out.println(signjson.substring(i, LIM));
			}
			else if (i == 5000) {
				System.out.println("    .... (etc.) ....");
			}
			i = LIM;
		}
		System.out.println();
	}
}
