/*
 * BareSiGMLPlayer		2010-01-23
 *
 * Based on JASiGMLPlayer.java
 */
package app;


import java.lang.reflect.InvocationTargetException;

import java.util.Date;
import java.util.Properties;

import java.io.IOException;

import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Component;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.UIManager;

import jautil.JAEnv;
import jautil.JAAvatarsEnv;
import jautil.JAOptions;
import jautil.JATimer;
import jautil.AppletPropertiesSetter;

import jautil.platform.OpSystem;

import player.JASocketPlayer;
import player.JASocketPlayerEventHandler;
import player.JACanvasEmbedder;
import player.AnimationScan;
import player.AvatarEventHandler;

import app.gui.QuitManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** Bare SiGML-player app that accepts SiGML input over a TCP/IP server
 * socket and plays the signed animation it defines, and also accepts
 * switch-avatar requests over a TCP/IP server socket, but with no GUI
 * beyond the avatar panel itself.
 */
public class BareSiGMLPlayer {


/** Logger. */
	private static final Logger				logger = LogManager.getLogger();

/** Main method -- creates a new instance of the app with the given CL args. */
	public static void main(String[] args)
	throws InterruptedException, InvocationTargetException, IOException {

		logger.log(LoggerConfig.INFOLevel, "App Starting");
		String jrevn = System.getProperty("java.version");
		log((new Date())+"   Java version "+jrevn+"   BareSiGMLPlayer");

		BareSiGMLPlayer bspapp = new BareSiGMLPlayer(args);
		logger.log(LoggerConfig.INFOLevel, "App Created");
	}

/** App window left X coordinate. */
	private int								winX = -1;
/** App window top Y coordinate. */
	private int								winY = -1;
/** App window width. */
	private int								width = -1;
/** App window height. */
	private int								height = -1;
	
/** JA Options settings for this app. */
	private final JAOptions					JA_OPTS;
/** Avatars environment for this app. */
	private final JAAvatarsEnv				AVATARS_ENV;

/** App's main window. */
	private JFrame							window;
/** The main pane, which is the one containing the avatar's JA canvas.  */
	private JPanel							avatarPane;
/** Quit manager for this app. */
	private QuitManager						QUIT_MANAGER;
/** This app's socket player. */
	private JASocketPlayer					player;

/** Animation sequence play completion flag for a PLAY operation. */
	private transient boolean				animPlayComplete = true;
/** Animation sequence load completion flag for a PLAY operation. */
	private transient boolean				animLoadComplete = true;

/** Creates a new SiGML-Player instance using the given command line
 * arguments to determine the options settings.
 */
	public BareSiGMLPlayer(String[] args)
	throws InterruptedException, InvocationTargetException {

		JATimer tmr = new JATimer();

        Properties argProps = AppletPropertiesSetter.argsToProperties(args);
		AppletPropertiesSetter.copyStdAppProperties(argProps);

		// Get JARP options and environment for this execution of the app.
		this.JA_OPTS =
			JAOptions.makeJAOptions(
				"BareSiGMLPlayer", args, argProps, JAEnv.makeAppJAEnv());
		this.AVATARS_ENV = this.JA_OPTS.getAvatarsEnv();

		// Use the options to get the required window location and
		// main panel size.
		int[] xywh = this.JA_OPTS.appWindowLocationAndSize();
		this.winX = xywh[0];
		this.winY = xywh[1];
		this.width = xywh[2];
		this.height = xywh[3];

		// Trigger the set-up of our GUI (from the Java GUI thread).
		final Runnable RUNNABLE_SET_UP_GUI = new Runnable() {
			public void run() { BareSiGMLPlayer.this.createGUI(); }
		};
		EventQueue.invokeAndWait(RUNNABLE_SET_UP_GUI);
		log("####  BareSiGMLPlayer:  GUI set up done.");

		// Start SiGML and Switch-Avatar input servers.
		this.player.startSiGMLInput(this.JA_OPTS);
		this.player.startSwitchAvatarInput();
		log("####  SiGML and Switch-Avatar input services started.");

		// Load the avatar.
		String avatar = this.AVATARS_ENV.currentAvatar();
		this.player.requestSwitchAvatar(avatar);

		tmr.showTimeMS("####  BareSiGMLPlayer  Complete set-up: t");
	}

/** Creates the GUI for this player app, packs it and displays it. */
	protected void createGUI() {

		// NB
		// Currently we completely ignore the "Use-AWT" flag.

		// Use the platform look-and-feel.
		String syslaf = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(syslaf);
		}
		catch (Exception xx) { log("BareSiGMLPlayer l-&-f: "+xx); }

		// Create the frame window and its main pane.
		this.window = new JFrame("JA Bare SiGML Player");

		this.createAvatarPane();

		// Create the quit manager and related infrastructure.  This
		// app has neither a menu nor a menu bar unless it's on Mac OS X
		// in which case the menu bar and app menu will be there, and the
		// menu will have the quit item.
		final Runnable PREPARE_FOR_QUIT = new Runnable() {
			public void run() { BareSiGMLPlayer.this.prepareShutDown(); }
		};
		int qdelayms = this.JA_OPTS.getIntegerProperty("quit.delay.ms");
		this.QUIT_MANAGER = new QuitManager(PREPARE_FOR_QUIT, qdelayms);
		if (OpSystem.IS_MAC()) {
			OpSystem.registerMacOSXQuitter(this.QUIT_MANAGER.getQuitRunnable());
		}

		// Attach main avatar panel to the window, and arrange to
		// treat the window-closing event as a quit trigger.
		this.window.add(this.avatarPane);
		this.window.addWindowListener(this.QUIT_MANAGER.getQuitWindowListener());

		// Now make the window visible.
		this.window.setLocation(this.winX, this.winY);
		this.window.pack();
		this.window.setVisible(true);
		log("####  BareSiGMLPlayer  App window now displayed.");
	}

/** Creates the main app window pane. */
	protected void createAvatarPane() {

		this.avatarPane = new JPanel(new BorderLayout());
		this.avatarPane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createLoweredBevelBorder()));
		// Conceptually we want the following line -- actually done
		// by JA_CANVAS_EMBEDDER when the player is created (see below).
//		this.avatarPane.add(jacanvas, BorderLayout.CENTER);

		this.avatarPane.setPreferredSize(new Dimension(this.width, this.height));

		// JA_CANVAS_EMBEDDER will put the player at the CENTER of
		// the avatarPane.
		this.player =
			new JASocketPlayer(
				this.JA_OPTS, this.JA_CANVAS_EMBEDDER,
				this.AVATAR_EVENT_HANDLER, this.PLAYER_EVENT_HANDLER, null);

		this.player.createStandardCameraChangeMonitor();
	}

/** Embedder of the JA avatar canvas in our main panel. */
	protected JACanvasEmbedder					JA_CANVAS_EMBEDDER =
	new JACanvasEmbedder() {
		public void embedInContainer(Component jacanvas) {
			BareSiGMLPlayer.this.avatarPane.add(jacanvas, BorderLayout.CENTER);
		}
	};

/** Handler for avatar load/unload events -- delegates each event to
 * the appropriate one of our handler methods.
 */
	protected final AvatarEventHandler			AVATAR_EVENT_HANDLER =
	new AvatarEventHandler() {
		public void avatarIsLoaded(String avatar) {
			BareSiGMLPlayer.this.handleAvatarLoaded(avatar);
		}
		public void avatarIsUnloaded(String avatar) {
			BareSiGMLPlayer.this.handleAvatarUnloaded(avatar);
		}
	};

/** Handler for player events -- delegates each event to
 * the appropriate one of our handler methods.
 */
	protected final JASocketPlayerEventHandler	PLAYER_EVENT_HANDLER =
	new JASocketPlayerEventHandler() {
		public void sigmlInputReceived() {
			BareSiGMLPlayer.this.handleInputReceived();
		}
		public void loaderHasStarted() {
			BareSiGMLPlayer.this.handleLoadStarted();
		}
		public void nextSignLoaded(int s, int flimit) {
			BareSiGMLPlayer.this.handleSignLoaded(s, flimit);
		}
		public void loaderIsDone(boolean gotframes, int nsigns, int nframes) {
			BareSiGMLPlayer.this.handleLoadDone(gotframes, nsigns, nframes);
		}
		public void playerHasStarted() {
			// No action
		}
		public void playerIsAtNewFrame(AnimationScan scan,  boolean dropped) {
			BareSiGMLPlayer.this.handleNewFrame(scan, dropped);
		}
		public void playerIsDone(AnimationScan scan) {
			BareSiGMLPlayer.this.handlePlayDone(scan);
		}
	};

/** Handler method for "avatar-loaded" event. */
	protected void handleAvatarLoaded(String avatar) {

		log("####  Loaded avatar "+avatar+".");
	}

/** Handler method for "avatar-unloaded" event. */
	protected void handleAvatarUnloaded(String avatar) {

		log("####  Unloaded avatar "+avatar+".");
	}

/** Handler method for "new-(SiGML)-input-received" event. */
	protected void handleInputReceived() {

		this.animLoadComplete = false;
		this.animPlayComplete = false;
		log("####  SiGML input received.");
	}

/** Handler for the player's (animation) "Load Started" event. */
	protected void handleLoadStarted() {
	}

/** Handler for the player's (animation) "Sign Loaded" event. */
	protected void handleSignLoaded(int s, int flimit) {
	}

/** Handler method for "animation-load-done" event. */
	protected void handleLoadDone(boolean loadok, int nsigns, int nframes) {

		this.animLoadComplete = true;

		if (this.animPlayComplete) {

			this.player.ensureAnimationIsComplete();
		}
		else if (!this.JA_OPTS.doStreamedAnimationBuild()) {

			String message =
				loadok ?
					this.newAnimMessage(nsigns, nframes) :
					"SiGML input processing failure!";

			log("####  BareSiGMLPlayer: "+message);
		}
	}

/** Returns a "New animation ..." message for the given sign and frame counts. */
	protected String newAnimMessage(int nsigns, int nframes) {

		String smsg = (0 < nsigns ? nsigns+" signs: " : "");
		return "New animation: "+smsg+nframes+" frames generated.";
	}

/** Handler method for "player-at-new-frame" event. */
	protected void handleNewFrame(AnimationScan scan, boolean dropped) {

		if (scan.scanIsAtNewSign()) {

			int s = scan.s();
			int slimit = scan.sCount();
			String gloss = scan.sign().getGloss();

			this.showSignInfo(slimit, s, gloss);
		}
	}

/** Handler method for "player-done" event. */
	protected void handlePlayDone(AnimationScan scan) {

		this.animPlayComplete = true;

		int s = scan.s();
		int f = scan.f();
		String smsg = (s<0 ? "" : ", sign="+s);
		log("####  BareSiGMLPlayer stopped: frame="+f+smsg+".");
	}

/** Shows sign-related information in the status panel. */
	protected void showSignInfo(int slimit, int s, String gloss) {

		log("####  [Limit="+slimit+"]  Sign "+s+":  \""+gloss+"\"");
	}

/** Handler for the window-close or quit events: stops the player,
 * flushes window-size, and kills the player.
 */
	protected void prepareShutDown() {

		try {
			// At this stage, stop the player, but don't kill its canvas.
			this.player.stopPlaying();

			// Flush some preferences data.
			this.updateWindowData();

			// Now really wipe the player and its canvas.
			this.player.terminate();
		}
		catch (InterruptedException ix) {
			log("####  BareSiGMLPlayer shut-down interrupted: "+ix);
		}
	}

/** Updates the user preferences with the app's window location and
 * main panel size.
 * Theory says we should drive this off the window's component
 * events (move, resize), but a single drag can give a lot of move
 * events, and it seems somewhat excessive to update the registry
 * on each one of these.  If we knew how to detect cessation of
 * MOUSE-DOWN under these circumstances, we could change our approach.
 */
	protected void updateWindowData() {

		Point wloc = this.window.getLocation();
		Dimension psz = this.avatarPane.getSize();

		int[] xywh = new int[4];
		xywh[0] = (int) wloc.getX();
		xywh[1] = (int) wloc.getY();
		xywh[2] = (int) psz.getWidth();
		xywh[3] = (int) psz.getHeight();

		this.JA_OPTS.updateAppWindowLocationAndSize(xywh);
	}

/** Logs the given message text on the console. */
	protected static void log(String msg) { System.out.println(msg); }
}
