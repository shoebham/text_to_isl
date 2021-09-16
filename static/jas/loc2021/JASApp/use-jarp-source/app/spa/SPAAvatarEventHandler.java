/*
 * SPAPlayerEventHandler.java		2008-08-29
 *
 * Factored out of main SiGMLPlayerApplet.
 */
package app.spa;


import player.AvatarEventHandler;

import app.spa.SPAEventTarget;

import static app.spa.EventId.AVATAR_UNLOADED;
import static app.spa.EventId.AVATAR_LOADED_OK;
import static app.spa.EventId.AVATAR_LOAD_FAILED;


/** Avatar event handler: receives notification of avatar load/unload
 * events from the SiGML Player Applet's JA player component,
 * converting them into {@link SPAEvent}s which it posts (typically to
 * the SPA event dispatcher thread) via an event target supplied at
 * construction time.
 */
public class SPAAvatarEventHandler implements AvatarEventHandler {

	private final SPAEventTarget			EVT_TARGET;

/** Constructs a new avatar event handler, which will direct the
 * {@link SPAEvent}s it generates to the given target.
 */
	public SPAAvatarEventHandler(SPAEventTarget etarget) {

		this.EVT_TARGET = etarget;
	}

	//############  AEH Interface methods.  ############

/** Handles the avatar loaded event, for the given avatar, which
 * may be {@code null}, indicating an unsuccessful load operation.
 * Posts a loaded-ok or load-failed event to this handler's target,
 * and checks the resulting acknowledgement.
 */
	public void avatarIsLoaded(String avatar) {

		EventId kind =
			(avatar != null ? AVATAR_LOADED_OK : AVATAR_LOAD_FAILED);
		this.postAvatarEvent(kind, avatar);
	}

/** Handles the avatar unloaded event, for the given avatar.
 * Posts an unloaded event to this handler's target
 * and checks the resulting acknowledgement.
 */
	public void avatarIsUnloaded(String avatar) {

		this.postAvatarEvent(AVATAR_UNLOADED, avatar);
	}

	//############  Event posting method.  ############

/** Posts the given avatar event, for the given avatar, to this handler's
 * target, waits for the matching acknowledgement, and logs the
 * acknowledgement message if it is not OK.
 */
	private void postAvatarEvent(EventId kind, String av) {

		SPAEvent evt = new SPAEvent(kind, av, false, null);
		SPAEventAck ack = this.EVT_TARGET.postEvent(evt);
		if (!ack.OK) {
			System.out.println(
				"####  SPA Avatar event "+kind+": "+ack.MESSAGE);
		}
	}
}
