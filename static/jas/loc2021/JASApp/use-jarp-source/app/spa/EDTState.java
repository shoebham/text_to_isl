/*
 * EDTState.java		2009-09-12
 */
package app.spa;


import java.util.EnumSet;


import app.util.UseJLogger;

import app.spa.StateFlag;
import static app.spa.StateFlag.*;


/** The internal state of the {@link app.SiGMLPlayerApplet}'s event
 * dispatch thread, represented as a set of {@link app.spa.StateFlag}s.
 * (This would be a subclass of {@code EnumSet<StateFlag>} were the
 * latter not an abstract class.)
 */
public class EDTState {

/** The empty set of state flags. */
	private static final EnumSet<StateFlag>	NO_FLAGS =
											EnumSet.noneOf(StateFlag.class);
/** The set of all state flags. */
	private static final EnumSet<StateFlag>	ALL_FLAGS =
											EnumSet.allOf(StateFlag.class);
/** The set of all flags that could be set when an animation is being played. */
	private static final EnumSet<StateFlag>	BASIC_PLAYING_FLAGS =
											EnumSet.of(
												PLAYING_SIGML_URL,
												PLAYING_SIGML_TEXT,
												PLAYING_SIGML_PIPED);
/** The set of all flags that could be set when an animation is being
 * loaded or played, or even in the process of being stopped.
 */
	private static final EnumSet<StateFlag>	PLAYING_FLAGS =
											EnumSet.of(
												LOADING_FRAMES,
												PLAYING_SIGML_URL,
												PLAYING_SIGML_TEXT,
												PLAYING_SIGML_PIPED,
												HAS_OPEN_SIGML_PIPE,
												STOPPING_PLAY);

/** The set of flags in this state */
	private EnumSet<StateFlag>				flags;
/** The string buffer used by the {@link #toString()} method. */
	private final StringBuilder				FLAG_TAGS_BUFFER;
/** The logger to be used by this state. */
	private final UseJLogger					LOGGER;
/** The string representation of the most recently logged setting of this state. */
	private String							lastLogged;
/** String representation for an indeterminate setting of this state. */
	private final String					UNKNOWN_STATE;

/** Constructs a new state with no flags and no logger. */
	public EDTState() {

		this(null);
	}

/** Constructs a new state with no flags, and using the given logger
 * (which may be null if logging is not required). */
	public EDTState(UseJLogger logger) {

		this.flags = EnumSet.copyOf(NO_FLAGS);

		this.FLAG_TAGS_BUFFER = new StringBuilder(StateFlag.COUNT);
		this.LOGGER = logger;
		this.lastLogged = this.toString();
		this.UNKNOWN_STATE = (this.lastLogged).replace('_', '?');
	}

/** Returns a string representation of this state, with one character
 * per flag: an underscore if the flag is not present, the flag's
 * tag character if it is.
 */
	public String toString() {

		this.FLAG_TAGS_BUFFER.setLength(StateFlag.COUNT);

		for (StateFlag f : ALL_FLAGS) {
			final int F_POS = f.ordinal();
			final char F_CHAR = f.onOffChar(this.has(f));
			this.FLAG_TAGS_BUFFER.setCharAt(F_POS, F_CHAR);
		}

		return this.FLAG_TAGS_BUFFER.toString();
	}

	//####  Getters.  ####

/** Indicates whether this state has the given flag. */
	public boolean has(StateFlag s) {

		return this.flags.contains(s);
	}

/** Indicates whether this state has both the given flags. */
	public boolean hasBoth(StateFlag s0, StateFlag s1) {

		return this.flags.contains(s0) && this.flags.contains(s1);
	}

/** Indicates whether this state has at least one of the given flags. */
	public boolean hasSome(StateFlag s0, StateFlag s1) {

		return this.flags.contains(s0) || this.flags.contains(s1);
	}

/** Indicates whether this state has at least one of the given flags. */
	public boolean hasSome(StateFlag s0, StateFlag s1, StateFlag s2) {

		return this.hasSome(s0, s1) || this.flags.contains(s1);
	}

/** Indicates whether this state has at least one of the given flags. */
	public boolean hasSome(EnumSet<StateFlag> ss) {

		EnumSet<StateFlag> sscopy = EnumSet.copyOf(ss);
		sscopy.retainAll(this.flags);
		return ! sscopy.isEmpty();
	}

/** Indicates whether this state is one in which animation is is progress. */
	public boolean hasBasicPlay() {

		return this.hasSome(BASIC_PLAYING_FLAGS);
	}

/** Indicates whether this state is one in which an animation is being
 * played, without a pending request to stop the player.
 */
	public boolean hasSteadyPlay() {

		return this.hasBasicPlay() && ! this.has(STOPPING_PLAY);
	}

	//####  General setters.  ####

/** Removes the given flag from this state, if it is present, leaving
 * all others unchanged.
 */
	public void remove(StateFlag s) {

		this.flags.remove(s);
	}

/** Ensures that this state includes the given flag but no others. */
	public void setOnly(StateFlag s) {

		this.flags.clear();
		this.flags.add(s);
	}

/** Adds the given flag to this state, if it is not already present,
 * leaving all others unchanged.
 */
	public void include(StateFlag s) {

		this.flags.add(s);
	}

	public void oneOutOneIn(StateFlag sout, StateFlag sin) {

		this.flags.remove(sout);
		this.flags.add(sin);
	}

	//####  Special setters.  ####

/** Adjusts this state as required for a switch to playing a SiGML URL. */
	public void setPlayingSiGMLURL() {

		this.flags.remove(IDLE);
		this.flags.remove(HAS_ALL_FRAMES);
		this.flags.add(LOADING_FRAMES);
		this.flags.add(PLAYING_SIGML_URL);
	}

/** Adjusts this state as required for a switch to playing a SiGML text. */
	public void setPlayingSiGMLText() {

		this.flags.remove(IDLE);
		this.flags.remove(HAS_ALL_FRAMES);
		this.flags.add(LOADING_FRAMES);
		this.flags.add(PLAYING_SIGML_TEXT);
	}

	public void setPlayingSiGMLPiped() {

		this.flags.remove(IDLE);
		this.flags.remove(HAS_ALL_FRAMES);
		this.flags.add(LOADING_FRAMES);
		this.flags.add(PLAYING_SIGML_PIPED);
		this.flags.add(HAS_OPEN_SIGML_PIPE);
	}

/** Removes all currently included playing flags from this state, leaving
 * all others unchanged.
 */
	public void removeAllPlaying() {

		this.flags.removeAll(PLAYING_FLAGS);
	}

	//####  Logging.  ####

/** Records on this state's logger, if it has one, the change between
 * the most recently logged state and the current state, provided the
 * two states differ from one another.
 */
	public void logChange() {

		if (this.logOK()) {
			String oldstr = this.lastLogged;
			String newstr = this.toString();

			if (!oldstr.equals(newstr)) {
				this.LOGGER.log(
					"state change: ["+oldstr+"] --> ["+newstr+"]");
			}

			this.lastLogged = newstr;
		}
		else {
			this.lastLogged = this.UNKNOWN_STATE;
		}
	}

/** Indicates if logging is currently possible. */
	protected boolean logOK() {

		return (this.LOGGER != null && this.LOGGER.logIsEnabled());
	}
}
