/*
 * JASigningMenuBar.java		2007-05-16
 */
package app.gui;


import java.awt.EventQueue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.ButtonGroup;

import javax.swing.Action;
import javax.swing.SwingUtilities;


import jautil.JAOptions;
import jautil.JAAvatarsEnv;

import jautil.platform.OpSystem;

import player.JAFramesPlayer;

import app.gui.QuitManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


public class JASigningMenuBar extends JMenuBar {

/** Logger. */
	private static final Logger			logger = LogManager.getLogger();

/** Avatars environment for the app that owns this menu bar. */
	private final JAAvatarsEnv			AVATARS_ENV;
/** Options set for the app that owns this menu bar, to be used
 * for ambient motion control, or {@code null} if not required.
 */
	private final JAOptions				JA_OPTS_FOR_AMBIENT;
/** Player belonging to the app that owns this menu bar. */
	private final JAFramesPlayer		PLAYER;
/** App quit manager to be used by this menu bar. */
	private final QuitManager			QUIT_MANAGER;

/** Action for Video Generation menu item, or {@code null} if none. */
	private final ActionListener		VIDEO_GEN_ACTION;

/** The Avatar menu. */
	private final JMenu					AVATAR_MENU;
/** Names of possible Avatar submenus. */
	private String[]					AVATAR_SUBMENU_NAMES = { };
/** Names of avatars in each possible submenu; this must have the same
 * length as {@link #AVATAR_SUBMENU_NAMES}.
 */
	private String[][]					AVATAR_SUBMENU_AVATAR_LISTS = { };
/** The number of possible Avatar submenus. */
	private int							COUNT_AVATAR_SUBMENU_SPECS =
										AVATAR_SUBMENU_NAMES.length;

/** The menu item controlling busy (animation) ambient motion. */
	private JCheckBoxMenuItem			DO_BUSY_AMBIENT_ITEM;
/** The menu item controlling idle ambient motion. */
	private JCheckBoxMenuItem			DO_IDLE_AMBIENT_ITEM;

/** The runnable supplied by the owning app, which is to be invoked
 * every time an avatar switch is triggered.
 */
	private final Runnable				NOTIFY_AVATAR_SWITCH;

/** Action listener for the Avatar menu: responds to a menu selection
 * by calling {@link #invokeSwitchAvatarOnEDT(String)} for the apppropriate
 * avatar.
 */
	private ActionListener				AVATAR_MENU_LISTENER =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			// Trigger a switch to the avatar given by the
			// menu item name.
			String avatar = aevt.getActionCommand();
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.COMMANDMarker,
				"Avatar menu selected: "+avatar);
			JASigningMenuBar.this.invokeSwitchAvatarOnEDT(avatar);
		}
	};

/** Quit thread for the app that owns this menu bar. */
	private Thread						quitThread = null;

/** Constructs a new JASigning menu bar instance, using the given
 * player, quit-manager, avatars-environment,
 * avatars submenu flag, avatar submenu specs, and switch-avatar action.
 */
	public JASigningMenuBar(
		JAFramesPlayer player, QuitManager qtmngr, JAAvatarsEnv avenv,
		boolean doavsubmenus, String avsubmenuspecs, Runnable avswitch) {

		this(player, qtmngr, null,
			avenv, doavsubmenus, avsubmenuspecs, avswitch);
	}

/** Constructs a new JASigning menu bar instance, using the given
 * player, quit-manager, avatars-environment,
 * avatars submenu flag, avatar submenu specs, and switch-avatar action.
 */
	public JASigningMenuBar(
		JAFramesPlayer player, QuitManager qtmngr, JAAvatarsEnv avenv,
		boolean doavsubmenus, String avsubmenuspecs, Runnable avswitch,
		JAOptions optsforamb) {

		this(player, qtmngr, null,
			avenv, doavsubmenus, avsubmenuspecs, avswitch, optsforamb);
	}

/** Constructs a new JASigning menu bar instance, using the given
 * player, quit-manager, video-generation action, avatars-environment,
 * avatars submenu flag, avatar submenu specs, and switch-avatar action.
 */
	public JASigningMenuBar(
		JAFramesPlayer player, QuitManager qtmngr, ActionListener vgaction,
		JAAvatarsEnv avenv,
		boolean doavsubmenus, String avsubmenuspecs, Runnable avswitch) {

		this(player, qtmngr, vgaction,
			avenv, doavsubmenus, avsubmenuspecs, avswitch,
			null);
	}

/** Constructs a new JASigning menu bar instance, using the given
 * player, quit-manager, video-generation action, avatars-environment,
 * avatars submenu flag, avatar submenu specs., switch-avatar action,
 * and options set for ambient motion control.
 */
	public JASigningMenuBar(
		JAFramesPlayer player, QuitManager qtmngr, ActionListener vgaction,
		JAAvatarsEnv avenv,
		boolean doavsubmenus, String avsubmenuspecs, Runnable avswitch,
		JAOptions optsforamb) {

		// Create this menu bar.
		super();

		this.PLAYER = player;
		this.QUIT_MANAGER = qtmngr;
		this.VIDEO_GEN_ACTION = vgaction;
		this.AVATARS_ENV = avenv;
		this.NOTIFY_AVATAR_SWITCH = avswitch;
		this.JA_OPTS_FOR_AMBIENT = optsforamb;

		boolean needvg = (vgaction != null);

		// Create File menu if needed, i.e. if not on Mac OS X, since
		// it has Quit on the app menu.
		JMenu menufile = null;
		if (needvg || ! OpSystem.IS_MAC()) {
			menufile = new JMenu("File");
			this.add(menufile);
		}

		// Avatar menu ...
		this.AVATAR_MENU = new JMenu("Avatar");
		this.add(this.AVATAR_MENU);

		// File menu, Video Gen... item, if needed.
		if (needvg) {
			JMenuItem itemvg = new JMenuItem("Video Generation...");
			itemvg.addActionListener(vgaction);
			menufile.add(itemvg);
		}

		// Quit menu item, and quit handler.
		if (OpSystem.IS_MAC()) {

			OpSystem.registerMacOSXQuitter(
				this.QUIT_MANAGER.getQuitRunnable());
		}
		else {
			// Quit item on File menu.
			JMenuItem itemquit = new JMenuItem("Quit");
			itemquit.addActionListener(
				this.QUIT_MANAGER.getQuitActionListener());
			menufile.add(itemquit);
		}

		// Populate Avatar menu and any submenus, as determined
		// by the given user options ...
		this.setAvatarSubmenuData(doavsubmenus, avsubmenuspecs);

		// AVS holds the avatar list for this app;
		// av_menu_map specifies the menu for each of these avatars.
		final String[] AVS = this.AVATARS_ENV.getAvatars();
		JMenu[] av_menu_map = new JMenu[AVS.length];
		this.buildAvatarMenuMap(AVS, av_menu_map);
		this.populateAvatarMenu(AVS, av_menu_map);

		if (this.JA_OPTS_FOR_AMBIENT != null) {

			JMenu menuamb = new JMenu("Ambient");
			this.add(menuamb);

			this.populateAmbientMenu(menuamb);
		}
	}
    
/** Handler for Avatar menu item selection: this triggers a switch
 * to the selected avatar, run on the EDT.
 */
	protected void invokeSetEnabledAvatarMenuOnEDT(final boolean enabled) {

		final Runnable RUN_TASK = new Runnable() {
			public void run() { //xyz
                logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: invoke SetEnabledAvatarMenu");
				final boolean prev = JASigningMenuBar.this.AVATAR_MENU.isEnabled();
//                logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.EVENTMarker,
//					"setEnabledAvatarMenuOnEDT " + (EventQueue.isDispatchThread()?"EDT":"Not EDT"));
				JASigningMenuBar.this.AVATAR_MENU.setEnabled(enabled);
                SwingUtilities.updateComponentTreeUI(JASigningMenuBar.this);
                logger.log(LoggerConfig.LOGLevel, LoggerConfig.GUIMarker,
					"setEnabledAvatarMenuOnEDT " + prev + " -> " + 
                    JASigningMenuBar.this.AVATAR_MENU.isEnabled());
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: invoke SetEnabledAvatarMenu");
			}
		};
//        logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.EVENTMarker,
//			"setEnabledAvatarMenu      " + (EventQueue.isDispatchThread()?"EDT":"Not EDT"));
		EventQueue.invokeLater(RUN_TASK);
	}

/** Sets the enabled state of the Avatar menu as specified by the argument. */
	public void setEnabledAvatarMenu(boolean enabled) {
		this.invokeSetEnabledAvatarMenuOnEDT(enabled);
    }

/** Sets up the Avatar submenu data from the given option settings data. */
	protected void setAvatarSubmenuData(boolean dosms, String avsmspecs) {

		// The existing empty lists will do if submenus are not required.
		if (dosms && avsmspecs != null) {

			// avsmspecs says what the possible submenus are, and
			// what the possible avatars are for each submenu.

			// Split on "|" to get the submenu specs.
			final String[] SM_SPECS = avsmspecs.split("\\|");
			final int M = SM_SPECS.length;
			this.COUNT_AVATAR_SUBMENU_SPECS = M;

			this.AVATAR_SUBMENU_NAMES = new String[M];
			this.AVATAR_SUBMENU_AVATAR_LISTS = new String[M][];

			final String[] EMPTY_LIST = { };

			// Iterate over the submenu specs.
			for (int m=0; m!=M; ++m) {

				// Default submenu name and empty list, used in case of error.
				this.AVATAR_SUBMENU_NAMES[m] = "EXTRA";
				this.AVATAR_SUBMENU_AVATAR_LISTS[m] = EMPTY_LIST;

				// Split on ":" to get the submenu name and the list
				// of avatar names for that submenu.
				final String[] SM_SPEC_PAIR = SM_SPECS[m].split(":");
				if (SM_SPEC_PAIR.length == 2) {
					// Use the submenu name only if it's not empty.
					final String SM_NAME = SM_SPEC_PAIR[0].trim();
					if (SM_NAME.length() != 0) {
						this.AVATAR_SUBMENU_NAMES[m] = SM_NAME;
					}
					// Split the list on "/" to get the individual avatars.
					String[] smavnames = SM_SPEC_PAIR[1].split("/");
					// Trim each name.
					for (int i=0; i!=smavnames.length; ++i) {
						smavnames[i] = smavnames[i].trim();
					}
					this.AVATAR_SUBMENU_AVATAR_LISTS[m] = smavnames;
				}
				else {
					logger.log(LoggerConfig.WARNLevel, LoggerConfig.GUIMarker,
						"Bad avatar submenu spec: \""+SM_SPECS[m]+"\"");
				}
			}

			// Display results on console:
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.GUIMarker,
				this.COUNT_AVATAR_SUBMENU_SPECS+" Avatar submenu specs:");
			for (int j=0; j!=this.COUNT_AVATAR_SUBMENU_SPECS; ++j) {
				String AvNames = ":";
				for (String AV : this.AVATAR_SUBMENU_AVATAR_LISTS[j]) {
					AvNames += " "+AV;
				}
				logger.log(LoggerConfig.TRACELevel, LoggerConfig.GUIMarker,
					"   "+this.AVATAR_SUBMENU_NAMES[j]+AvNames);
			}
		}
	}

/** Determines which submenus are needed for the given avatar list,
 * creates those submenus, adds them to the Avatar menu, and finally
 * populates the given array {@code av_menu_map}, so that
 * each entry gives the menu for the corresponding avatar in the list.
 */
	protected void buildAvatarMenuMap(
		final String[] AVS, JMenu[] av_menu_map) {

		final int N = AVS.length;

		if (this.COUNT_AVATAR_SUBMENU_SPECS == 0) {

			// Map each avatar to the Avatar menu itself.
			for (int n=0; n!=N; ++n) {
				av_menu_map[n] = this.AVATAR_MENU;
			}
		}
		else {
			// n_used records the number of items in each submenu's names
			// list that are actually used, i.e. that appear in AVS.
			final int M = this.COUNT_AVATAR_SUBMENU_SPECS;
			int[] n_used = new int[M];

			// submenu_id maps each avatar index (for AVS) to the
			// index of the submenu for that avatar, or to M if there
			// is no such avatar.
			int[] submenu_id = new int[N];

			// Iterate over AVS to build the submenu_id and n_used lists.
			for (int n=0; n!=N; ++n) {
				int smi = this.submenuIndex(AVS[n]);
				submenu_id[n] = smi;
				if (smi != M) {
					++ n_used[smi];
				}
			}

			// menu_for_ix maps each potential submenu index to the actual
			// sub_for_ix or to AV_MENU if no submenu is allocated.
			JMenu[] menu_for = new JMenu[M];
			for (int m=0; m!=M; ++m) {

				// Create submenu m if and only if there are at least
				// two avatars to go on it.
				final JMenu MENU =
					n_used[m] < 2 ?
						this.AVATAR_MENU :
						new JMenu(AVATAR_SUBMENU_NAMES[m]+" Avatar");

				// If there's a new submenu add it to the main Avatar menu.
				if (MENU != this.AVATAR_MENU) {
					this.AVATAR_MENU.add(MENU);
				}

				menu_for[m] = MENU;
			}

			// Now set all the av_menu_map entries.
			for (int n=0; n!=N; ++n) {
				final int SM_N = submenu_id[n];
				av_menu_map[n] =
					SM_N == M ? this.AVATAR_MENU : menu_for[SM_N];
			}
		}
	}

/** Puts all the avatars at the appropriate position on the app's Avatar
 * menu or one of its submenus, using the corresponding map of avatars
 * to menus.
 */
	protected void populateAvatarMenu(
		final String[] AVS, final JMenu[] AV_MENU_MAP) {

		ButtonGroup avgroup = new ButtonGroup();

		final String CURRENT_AV = this.AVATARS_ENV.currentAvatar();

		final int N = AVS.length;
		for (int n=0; n!=N; ++n) {

			final String AV = AVS[n];

			// Create the menu item and attach the appropriate action-
			// listener to it.
			JRadioButtonMenuItem avitem = new JRadioButtonMenuItem(AV);
			avitem.addActionListener(this.AVATAR_MENU_LISTENER);

			// Put the avatar item in the appropriate menu.
			AV_MENU_MAP[n].add(avitem);

			// Add the menu item to our radio-buttons group, and mark
			// it as selected, if necessary.
			avgroup.add(avitem);
			if (AV.equals(CURRENT_AV)) {
				avitem.setSelected(true);
			}
		}
	}

/** Returns the index of the (first) possible submenu whose list
 * contains the given avatar, or returns the number of such lists
 * if none of them contains the avatar.
 */
    protected int submenuIndex(final String AV) {

		// Bounded linear search for the index, m, of the submenu
		// containing AV.
		int m = 0, mm = COUNT_AVATAR_SUBMENU_SPECS;
		while (m != mm) {
			final String[] SM_AVS = AVATAR_SUBMENU_AVATAR_LISTS[m];
			final int IX_AV = findIndex(SM_AVS, AV);
			if (IX_AV != SM_AVS.length)  mm = m;  else  ++m;
		}
		return m;
    }

/** Returns the index in the given list of (the first occurrence of)
 * the given string item, or returns the list length N if it does not
 * appear at all in the list.
 */
	protected static int findIndex(final String[] LIST, final String ITEM) {

		// Bounded linear search of LIST for ITEM.
		final int N = LIST.length;
		int i= 0, ii = N;
		while (i != ii) {
			if (LIST[i].equalsIgnoreCase(ITEM)) ii = i; else ++i;
		}
		return i;
	}

/** Process the Avatar menu to enable the entry for the given avatar.
 */
	public void enableAvatarMenuEntry(String avtr) {
		enableAvatarMenuEntry(this.AVATAR_MENU, avtr);
	}

/** Process the Avatar menu to enable the entry for the given avatar.
 */
	protected void enableAvatarMenuEntry(JMenu avMenu, String avtr) {
		int ents = avMenu.getItemCount();
		for (int i = 0; i < ents; i++) {
			JMenuItem avItem = avMenu.getItem(i);
			if (avItem instanceof JRadioButtonMenuItem) {
//				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.GUIMarker,
//					"Menu "+avMenu.getText()+" enable "+avtr+": RadioButton "+avItem.getText());
				if (avItem.getText().equals(avtr)) {
					logger.log(LoggerConfig.LOGLevel, LoggerConfig.GUIMarker,
						"Avatar Menu: Selected "+avItem.getText());
					avItem.setSelected(true);
				} else if(avItem.isSelected()) {
					logger.log(LoggerConfig.LOGLevel, LoggerConfig.GUIMarker,
						"Avatar Menu: Unselected "+avItem.getText());
					avItem.setSelected(false);
				}
			} else if (avItem instanceof JMenu) {
//				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.GUIMarker,
//					"Menu "+avMenu.getText()+" enable "+avtr+": SubMenu "+avItem.getText());
				enableAvatarMenuEntry((JMenu) avItem, avtr);
			} else {
				logger.log(LoggerConfig.ERRORLevel, LoggerConfig.GUIMarker,
					"Menu "+avMenu.getText()+" enable "+avtr+": Unexpected Item "+avItem.getText());
			}
		}
	}

/** Handler method for "new avatar selected" menu event. */
	protected void doSwitchAvatar(String avtr) {

		// No action if avatar already selected
        String curAvatar = this.AVATARS_ENV.currentAvatar();
        if (curAvatar == null || ! curAvatar.equals(avtr)) {
            this.AVATARS_ENV.setAvatar(avtr);
            // We should be able simply to switch to avtr, but we play 
            // by the rule that says that AVATARS_ENV determines
            // the identity of the current avatar.
            this.PLAYER.requestSwitchAvatar(this.AVATARS_ENV.currentAvatar());
            // Allow owning app to do anything else it wants to.
            this.NOTIFY_AVATAR_SWITCH.run();
        } else {
			// No action as avatar already current
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.COMMANDMarker,
				"Avatar already current: "+avtr);
		}
	}

/** Handler for Avatar menu item selection: this triggers a switch
 * to the selected avatar, run on the EDT.
 */
	protected void invokeSwitchAvatarOnEDT(final String AVATAR) {

		final Runnable RUN_AVATAR_SWITCH = new Runnable() {
			public void run() { //xyz
                logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: invoke doSwitchAvatar");
				JASigningMenuBar.this.doSwitchAvatar(AVATAR);
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: invoke doSwitchAvatar");
			}
		};

		EventQueue.invokeLater(RUN_AVATAR_SWITCH);
	}

/** Creates the ambient menu items and attaches them to the given menu. */
	protected void populateAmbientMenu(JMenu menuamb) {

			boolean busyon = this.JA_OPTS_FOR_AMBIENT.doBusyAmbient();
			boolean idleon = this.JA_OPTS_FOR_AMBIENT.doIdleAmbient();

			this.DO_BUSY_AMBIENT_ITEM =
				new JCheckBoxMenuItem("Do Signing Ambient Motion", busyon);
			this.DO_IDLE_AMBIENT_ITEM =
				new JCheckBoxMenuItem("Do Idle Ambient Motion", idleon);

			final ItemListener AI_HNDLR = new ItemListener() {
				public void itemStateChanged(ItemEvent ievt) {
					JASigningMenuBar.this.updateAmbientItemSetting(
						(JCheckBoxMenuItem) ievt.getSource());
				}
			};

			this.DO_BUSY_AMBIENT_ITEM.addItemListener(AI_HNDLR);
			this.DO_IDLE_AMBIENT_ITEM.addItemListener(AI_HNDLR);

			menuamb.add(this.DO_BUSY_AMBIENT_ITEM);
			menuamb.add(this.DO_IDLE_AMBIENT_ITEM);
	}

/** Handles a change of state in the given ambient menu item. */
	protected void updateAmbientItemSetting(JCheckBoxMenuItem aitem) {

		final boolean DO_AMB = aitem.isSelected();

		if (aitem == this.DO_BUSY_AMBIENT_ITEM) {
			this.JA_OPTS_FOR_AMBIENT.updateDoBusyAmbient(DO_AMB);
		}
		else if (aitem == this.DO_IDLE_AMBIENT_ITEM) {
			this.JA_OPTS_FOR_AMBIENT.updateDoIdleAmbient(DO_AMB);
			this.PLAYER.handleDoIdleAmbientChange();
		}
	}
}
