/*
 * StateFlag.java (enum)		2009-09-12
 */
package app.spa;


import app.util.UseJLogger;


/** An enumeration of flags each representing one aspect of the internal
 * state of the {@link app.SiGMLPlayerApplet}'s event dispatch thread.
 */
public enum StateFlag {

	IDLE('I'),
	CHANGING_AVATAR('C'),
	LOADING_FRAMES('L'),
	PLAYING_SIGML_URL('U'),
	PLAYING_SIGML_TEXT('T'),
	PLAYING_SIGML_PIPED('P'),
	STOPPING_PLAY('S'),
	HALTING('H'),
	HALTED('E'),
	HAS_AVATAR('A'),
	HAS_OPEN_SIGML_PIPE('O'),
	HAS_ALL_FRAMES('F');

/** The initial state flag in this enumeration. */
	public static final StateFlag		FIRST = IDLE;
/** The final state flag in this enumeration. */
	public static final StateFlag		LAST = HAS_ALL_FRAMES;

/** The number of state flags in this enumeration. */
	public static final int				COUNT = LAST.ordinal()+1;

/** Tag character for this state flag. */
	private final char					TAG;

/** Constructs a new state flag with the given tag character. */
	private StateFlag(char tag)			{ this.TAG = tag; }

/** Returns an on/off tag character for this state flag, as specified by
 * the argument: the "on" tag is this flag's tag character, the "off"
 * tag is an underscore character.
 */
	public char onOffChar(boolean on)	{ return (on ? this.TAG : '_'); }

/** Lists the tags for the state flags in this enumeration, on the given logger. */
	public static void listAllTags(UseJLogger logger) {

		for (StateFlag f : java.util.EnumSet.allOf(StateFlag.class)) {
			logger.log(f.onOffChar(true)+": "+f);
		}
	}
}
