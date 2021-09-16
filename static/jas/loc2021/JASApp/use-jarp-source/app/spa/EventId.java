/*
 * EventId.java (enum)		2008-08-25
 */
package app.spa;


import java.util.EnumSet;

import app.spa.EventCategory;

import static app.spa.EventCategory.UI_EVENT;
import static app.spa.EventCategory.AVATAR_EVENT;
import static app.spa.EventCategory.FRAMES_GEN_EVENT;
import static app.spa.EventCategory.ANIMATION_EVENT;


/** An enumeration containing all the event ids handled by
 * the {@link app.SiGMLPlayerApplet}'s event dispatch thread.
 * These event ids are partitioned into the four categories defined
 * in the {@link EventCategory} enumeration.
 */
public enum EventId {

	//####  UI Events  ####

	HALT( UI_EVENT ),
	SHUT_DOWN( UI_EVENT ),
	LOAD_AVATAR( UI_EVENT ),
	PLAY_SIGML_URL( UI_EVENT ),
	PLAY_SIGML_TEXT( UI_EVENT ),
	START_PLAY_SIGML_PIPED( UI_EVENT ),
	APPEND_TO_SIGML_PIPE( UI_EVENT ),
	CLOSE_SIGML_PIPE( UI_EVENT ),
	STOP_PLAY_SIGML( UI_EVENT ),
	SET_SPEED_UP( UI_EVENT ),
	SET_ANIMGEN_FPS( UI_EVENT ),
	SET_DO_LOG_DROPPED_FRAMES( UI_EVENT ),
	SUSPEND_IF_PLAYING( UI_EVENT ),
	RESUME_IF_PLAYING( UI_EVENT ),

	//####  Avatar events  ####

	AVATAR_LOADED_OK( AVATAR_EVENT ),
	AVATAR_LOAD_FAILED( AVATAR_EVENT ),
	AVATAR_UNLOADED( AVATAR_EVENT ),

	//####  Frames Generation events  ####

	LOAD_FRAMES_START( FRAMES_GEN_EVENT ),
	LOADED_NEXT_SIGN( FRAMES_GEN_EVENT ),
	LOAD_FRAMES_DONE_OK( FRAMES_GEN_EVENT ),
	LOAD_FRAMES_DONE_BAD( FRAMES_GEN_EVENT ),

	//####  Animation events  ####

	PLAY_FRAME( ANIMATION_EVENT ),
	SKIP_FRAME( ANIMATION_EVENT ),
	PLAY_FIRST_FRAME_OF_SIGN( ANIMATION_EVENT ),
	SKIP_FIRST_FRAME_OF_SIGN( ANIMATION_EVENT ),
	PLAY_DONE( ANIMATION_EVENT )
	;

	public static void main(String[] args) {
		EventId evt = EventId.AVATAR_UNLOADED;
		System.out.println("EVT: <"+evt.toString()+">");
	}
/** The category to which this event belongs. */
	private final EventCategory	CATEGORY;

/** Constructs a new event in the given category. */
	private EventId(EventCategory cat) {

		this.CATEGORY = cat;
	}

/** Tests whether this event is in the given category. */
	private final boolean isInCategory(EventCategory cat) {
		return this.CATEGORY == cat;
	}

/** Tests whether this is a HTML/LiveConnect UI generated event. */
	public final boolean isUIEvent() {
		return this.isInCategory(UI_EVENT);
	}

/** Tests whether this is an avatar event. */
	public final boolean isAvatarEvent() {
		return this.isInCategory(AVATAR_EVENT);
	}

/** Tests whether this is a frames generation event. */
	public final boolean isFramesGenEvent() {
		return this.isInCategory(FRAMES_GEN_EVENT);
	}

/** Tests whether this is an animation event. */
	public final boolean isAnimationEvent() {
		return this.isInCategory(ANIMATION_EVENT);
	}

/** Tests whether this event is in the given set. */
	public final boolean isIn(EnumSet<EventId> eids) {
		return eids.contains(this);
	}

/** Returns a set containing the given pair of SPA event ids. */
	public static final EnumSet<EventId> spaEventIds(
		EventId e0, EventId e1) {

		return EnumSet.of(e0, e1);
	}

/** Returns a set containing the given trio of SPA event ids. */
	public static final EnumSet<EventId> spaEventIds(
		EventId s0, EventId s1, EventId s2) {

		return EnumSet.of(s0, s1, s2);
	}
}
