/*
 * JASiGMLPlayer.java		2005-12-06
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
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.UIManager;

import jautil.AppletPropertiesSetter;
import jautil.JAEnv;
import jautil.JAAvatarsEnv;
import jautil.JAOptions;
import jautil.JATimer;

import player.JASocketPlayer;
import player.JASocketPlayerEventHandler;
import player.JACanvasEmbedder;
import player.AnimationScan;
import player.AvatarEventHandler;

import sigmlanim.AnimatedSign;

import app.gui.JASigningMenuBar;
import app.gui.FPSPane;
import app.gui.FrameNumberSpinner;
import app.gui.SpeedUpSlider;
import app.gui.QuitManager;
import app.gui.Wrap;

import static app.gui.FrameNumberSpinner.SignStatusHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;

/** A SiGML-player app that, somewhat along the lines of the old eSIGN
 * <code>SiGML-Anim</code> app, accepts SiGML input over a TCP/IP server
 * socket and plays the signed animation it defines.
 * <p>
 * As well as the avatar canvas, this app provides:
 * <ul>
 * <li>Play and Stop buttons;</li>
 * <li>Auto-Play, Cyclic-Play and Single-Sign Play check-boxes;</li>
 * <li>A Frame-Index  spinner control, which also acts as a
 *     "current-frame" display during animation;</li>
 * <li>An animation Speed-Control slider. </li>
 * <li>A status message area.</li>
 * </ul>
 * <p>
 * <strong>NB</strong>&nbsp;&nbsp;
 * This app always uses Swing GUI components, regardless of the
 * setting of the <code>do.force.AWT.only</code> option.
 */
public class JASiGMLPlayer {

/** Logger. */
	private static final Logger				logger = LogManager.getLogger();

/** Main method -- creates a new instance of the app with the given CL args. */
	public static void main(String[] args)
	throws InterruptedException, InvocationTargetException, IOException {

		logger.log(LoggerConfig.INFOLevel, "App Starting");
		String jrevn = System.getProperty("java.version");
		System.out.println(
			(new Date())+"   Java version "+jrevn+"   JASiGMLPlayer");

		JASiGMLPlayer jaspapp = new JASiGMLPlayer(args);
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
/** App's main pane. */
	private JPanel							mainPane;
/** The inner pane containing the avatar's JA canvas.  */
	private JPanel							avatarPane;
/** Quit manager for this app. */
	private QuitManager						QUIT_MANAGER;
/** This app's socket player. */
	private JASocketPlayer					player;

/** Menu bar for this app. */
	private JASigningMenuBar				menuBar;
/** Combo box for animgen FPS value (belongs to an FPSPane). */
	private JComboBox						comboFPS;
/** Auto-Play check-box. */
	private JCheckBox						chkAuto;
/** Cyclic-Play check-box. */
	private JCheckBox						chkCyclic;
/** Single-Sign Play check-box. */
	private JCheckBox						chkSingle;
/** Play button. */
	private JButton							bttnPlay;
/** Stop button. */
	private JButton							bttnStop;
/** Frame Number spinner. */
	private FrameNumberSpinner				spinFrameNo;
/** Animation Speed slider. */
	private SpeedUpSlider					speedUpSlider;
/** Status Message label. */
	private JLabel							lblStatus;

/** Animation sequence play completion flag for a PLAY operation. */
	private transient boolean				animPlayComplete = true;
/** Animation sequence load completion flag for a PLAY operation. */
	private transient boolean				animLoadComplete = true;


/** Creates a new SiGML-Player instance using the given command line
 * arguments to determine the options settings.
 */
	public JASiGMLPlayer(String[] args)
	throws InterruptedException, InvocationTargetException {

		JATimer tmr = new JATimer();
        
//        System.out.println("## Original Properties:");
//        System.getProperties().list(System.out);
        
        Properties argProps = AppletPropertiesSetter.argsToProperties(args);
		AppletPropertiesSetter.copyStdAppProperties(argProps);

//        System.out.println("## Enhanced Properties:");
//        System.getProperties().list(System.out);

		// Get JARP options and environment for this execution of the app.
		this.JA_OPTS =
			JAOptions.makeJAOptions(
				"JASiGMLPlayer", args, argProps, JAEnv.makeAppJAEnv());
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
			public void run() { JASiGMLPlayer.this.createGUI(); }
		};
		EventQueue.invokeAndWait(RUNNABLE_SET_UP_GUI);
		System.out.println("####  JASiGMLPlayer:  GUI set up done.");

		// Start SiGML and Switch-Avatar input servers.
		this.player.startSiGMLInput(this.JA_OPTS);
		this.player.startSwitchAvatarInput();
		System.out.println(
			"####  JASiGMLPlayer:  "+
			"SiGML and Switch-Avatar input services started.");

		// Load the avatar.
		String avatar = this.AVATARS_ENV.currentAvatar();
		this.player.requestSwitchAvatar(avatar);

		tmr.showTimeMS("####  JASiGMLPlayer  Complete set-up: t");
	}

/** Creates the GUI for this player app, packs it and displays it. */
	protected void createGUI() { // throws IOException {

		// NB
		// Currently we completely ignore the "Use-AWT" flag.

		// Use the platform look-and-feel.
		String syslaf = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(syslaf);
		}
		catch (Exception xx) {
			System.out.println("JASiGMLPlayer l-&-f: "+xx);
		}

		// Create the frame window and its main pane.
		this.window = new JFrame("JA SiGML Player");

		this.mainPane = this.makeMainPane();

		// Create the infrastructure used by the menu bar:
		// avatar-switcher, pre-quit sequence and quit-manager.
		final Runnable AVATAR_SWITCH = new Runnable() {
			public void run() { JASiGMLPlayer.this.setGUIForAvatarSwitch(); }
		};
		final Runnable PREPARE_FOR_QUIT = new Runnable() {
			public void run() { JASiGMLPlayer.this.prepareShutDown(); }
		};
		int qdelayms = this.JA_OPTS.getIntegerProperty("quit.delay.ms");
		this.QUIT_MANAGER = new QuitManager(PREPARE_FOR_QUIT, qdelayms);

		// Create the menu bar, which cannot be done until the player
		// and other infrastructure have been created.
		final boolean USE_AV_SUBMENUS = this.JA_OPTS.avatarMenuDoUseSubmenus();
		final String AV_SUBMENU_SPECS = this.JA_OPTS.avatarSubmenuSpecs();
		this.menuBar =
			new JASigningMenuBar(
				this.player, this.QUIT_MANAGER, this.AVATARS_ENV,
				USE_AV_SUBMENUS, AV_SUBMENU_SPECS, AVATAR_SWITCH,
				this.JA_OPTS);

		// Attach menu bar and main panel to the window, and arrange to
		// treat the window-closing event as a quit trigger.
		this.window.setJMenuBar(this.menuBar);
		this.window.add(this.mainPane);
		this.window.addWindowListener(
			this.QUIT_MANAGER.getQuitWindowListener());

		// Disable the avatar menu; it should be enabled on completion
		// of the initial load-avatar operation.
		this.menuBar.setEnabledAvatarMenu(false);

		// Now make the window visible.
		this.window.setLocation(this.winX, this.winY);
		this.window.pack();
		this.window.setVisible(true);
		System.out.println(
			"####  JASiGMLPlayer  App window now displayed.");
	}

/** Creates and returns the main app window pane. */
	protected JPanel makeMainPane() {

		this.avatarPane = new JPanel(new BorderLayout());
		this.avatarPane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createLoweredBevelBorder()));
		// Conceptually we want the following line -- actually done
		// by JA_CANVAS_EMBEDDER when the player is created (see below).
//		this.avatarPane.add(jacanvas, BorderLayout.CENTER);

		JPanel mainpane =
			Wrap.wrapInNCSPane(
				this.makeControlPane(), this.avatarPane,
				this.makeStatusPane());
		mainpane.setPreferredSize(new Dimension(this.width, this.height));

		// Player must come after the controls, since it depends on some
		// of them -- e.g. speed control and checkboxes. 
		// JA_CANVAS_EMBEDDER will put the player at the CENTER of
		// the avatarPane.
		this.player =
			new JASocketPlayer(
				this.JA_OPTS, this.JA_CANVAS_EMBEDDER,
				this.AVATAR_EVENT_HANDLER, this.PLAYER_EVENT_HANDLER,
				this.speedUpSlider.getSpeedControl(),
				this.chkCyclic.isSelected(), this.chkSingle.isSelected(),
				this.chkAuto.isSelected());

		this.player.createStandardCameraChangeMonitor();

		this.resetPlayerGUI();

		this.spinFrameNo.setPlayer(this.player);

		this.setToolTips();

		return mainpane;
	}

/** Sets the tool tips for this app's components.*/
	protected void setToolTips() {

		this.chkAuto.setToolTipText(
			"Automatically play each SiGML text at it is received.");

		this.comboFPS.setToolTipText(
			"Set animation frame rate for the next SiGML sequence\n"+
			"received, in frames/sec., between 0 & 1000 exclusive.");

		this.bttnPlay.setToolTipText(
			"Plays the most recently received SiGML sequence.");
		this.bttnStop.setToolTipText(
			"Stop the current SiGML animation.");

		this.chkCyclic.setToolTipText(
			"Player should cycle repeatedly through the animation.");
		this.chkSingle.setToolTipText(
			"Player should play the current sign only.");

		this.spinFrameNo.setToolTipText(
			"Current animation frame number.");
		this.speedUpSlider.setToolTipText(
			"Adjusts the playing speed for the current animation.");

		this.lblStatus.setToolTipText(
			"Latest status message for this SiGML animation player.");
	}

/** Creates the panel containing the controls: check-boxes, buttons,
 * frame-count spinner, speed-slider.
 */
	protected Component makeControlPane() {

		// Use this panel for the buttons,
		// checkboxes and frame counter.
		JPanel panebttns =
			new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));

		Box boxchks = Box.createVerticalBox();
		Box boxbttns = Box.createVerticalBox();

		// Animgen FPS combo box pane, which does its own event handling.
		FPSPane fpspane = new FPSPane(this.JA_OPTS);
		this.comboFPS = fpspane.getFPSComboBox();

		// Checkboxes.
		this.chkAuto = new JCheckBox("Auto-Play", true);
		this.chkCyclic = new JCheckBox("Cyclic-Play", false);
		this.chkSingle = new JCheckBox("Single-Sign", false);

		// Buttons.
		this.bttnPlay = new JButton("Play");
		this.bttnStop = new JButton("Stop");
		this.bttnPlay.setEnabled(false);
		this.bttnStop.setEnabled(false);

		this.setButtonHandlers();

		// Create the other two controls, in their panes.
		JPanel framenopane = this.makeFrameNumberPane();
		JPanel speedpane = this.makeSpeedUpPane();

		// Box with FPS combo box and check boxes (on left).
		boxchks.add(fpspane);
		boxchks.add(this.chkAuto);
		boxchks.add(this.chkCyclic);
		boxchks.add(this.chkSingle);
		fpspane.setAlignmentX(0);
		this.chkAuto.setAlignmentX(0);
		this.chkCyclic.setAlignmentX(0);
		this.chkSingle.setAlignmentX(0);

		// Box of buttons, and frames-spinner (to right of checkboxes).
		boxbttns.add(this.bttnPlay);
		boxbttns.add(this.bttnStop);
		boxbttns.add(Box.createRigidArea(new Dimension(0, 2)));
		boxbttns.add(framenopane);
		this.bttnPlay.setAlignmentX(0.5f);
		this.bttnStop.setAlignmentX(0.5f);
		framenopane.setAlignmentX(0.5f);

		// Pane with box of check-boxes and box of buttons.
		boxchks.setAlignmentY(1.0f);
		boxbttns.setAlignmentY(1.0f);
		panebttns.add(boxchks);
		panebttns.add(boxbttns);

		// All-enclosing box:
		// - check-boxes/buttons on left,
		// - speed-up slider on right.
		Box boxall = Box.createHorizontalBox();
		boxall.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Player Control"),
				// put some space at the bottom ...
				BorderFactory.createEmptyBorder(0, 0, 4, 0)));

		boxall.add(panebttns);
		boxall.add(Box.createHorizontalStrut(20));
		boxall.add(speedpane);
		
		return boxall;
	}

/** Attaches the appropriate handler to each of the app's buttons and
 * checkboxes.
 */
	protected void setButtonHandlers() {

		this.chkAuto.addItemListener(this.IL_AUTO);
		this.chkCyclic.addItemListener(this.IL_CYCLIC);
		this.chkSingle.addItemListener(this.IL_SINGLE);

		this.bttnPlay.addActionListener(this.AL_PLAY);
		this.bttnStop.addActionListener(this.AL_STOP);
	}

/** Creates and returns a panel for the Frame Number spinner. */
	protected JPanel makeFrameNumberPane() {

		final SignStatusHandler SS_HANDLER = new SignStatusHandler() {
			public void updateSignStatus(int slimit, int s, String gloss) {
				JASiGMLPlayer.this.showSignInfo(slimit, s, gloss);
			}
		};

		this.spinFrameNo = new FrameNumberSpinner(SS_HANDLER);

		final JComponent[] CMPNNT = { this.spinFrameNo };

		JPanel fnpane = Wrap.wrapInFLPaneAndVBox(CMPNNT, 0, 0);

		return fnpane;
	}

/** Creates and returns a panel for the Speed Up slider. */
	protected JPanel makeSpeedUpPane() {

		this.speedUpSlider = new SpeedUpSlider();

		final JComponent[] CMPNNT = { this.speedUpSlider };

		JPanel supane = Wrap.wrapInFLPaneAndVBox(CMPNNT, 0, 0);

		supane.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Speed-Up (log scale)"));

		return supane;
	}

/** Creates the status message panel, to go at the bottom of the window. */
	protected Component makeStatusPane() {

		this.lblStatus = new JLabel("Starting socket-driven SiGML player");
		JPanel spane =
			Wrap.wrapInFlowLayoutPane(this.lblStatus, FlowLayout.LEFT);

		spane.setBorder(
			BorderFactory.createTitledBorder("Status"));

		return spane;
	}

/** Embedder of the JA avatar canvas in our main panel. */
	protected JACanvasEmbedder					JA_CANVAS_EMBEDDER =
	new JACanvasEmbedder() {
		public void embedInContainer(Component jacanvas) {
			JASiGMLPlayer.this.avatarPane.add(jacanvas, BorderLayout.CENTER);
		}
	};

/** Handler for avatar load/unload events -- delegates each event to
 * the appropriate one of our handler methods.
 */
	protected final AvatarEventHandler			AVATAR_EVENT_HANDLER =
	new AvatarEventHandler() {
		public void avatarIsLoaded(String avatar) {
			JASiGMLPlayer.this.handleAvatarLoaded(avatar);
		}
		public void avatarIsUnloaded(String avatar) {
			JASiGMLPlayer.this.handleAvatarUnloaded(avatar);
		}
	};

/** Play button's action listener -- delegates the action-performed
 * event to our play-action method.
 */
	protected final ActionListener				AL_PLAY =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			JASiGMLPlayer.this.doPlayAction();
		}
	};

/** Stop button's action-listener -- delegates the action-performed
 * event to our stop-action method.
 */
	protected final ActionListener				AL_STOP =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			JASiGMLPlayer.this.doStopAction();
		}
	};

/** Auto-play checkbox's item listener -- delegates
 * the item-state-changed event to our auto-play-change method.
 */
	protected ItemListener						IL_AUTO =
	new ItemListener() {
		public void itemStateChanged(ItemEvent ievt) {
			JASiGMLPlayer.this.handleAutoPlayChange();
		}
	};

/** Cyclic-play checkbox's item listener -- delegates
 * the item-state-changed event to our cyclic-play-change method.
 */
	protected final ItemListener				IL_CYCLIC =
	new ItemListener() {
		public void itemStateChanged(ItemEvent ievt) {
			JASiGMLPlayer.this.handleCyclicPlayChange();
		}
	};

/** Single-Sign checkbox's item listener -- delegates
 * the item-state-changed event to our cyclic-play-change method.
 */
	protected final ItemListener				IL_SINGLE =
	new ItemListener() {
		public void itemStateChanged(ItemEvent ievt) {
			JASiGMLPlayer.this.handleSingleSignPlayChange();
		}
	};

/** Handler for player events -- delegates each event to
 * the appropriate one of our handler methods.
 */
	protected final JASocketPlayerEventHandler	PLAYER_EVENT_HANDLER =
	new JASocketPlayerEventHandler() {
		public void sigmlInputReceived() {
			JASiGMLPlayer.this.handleInputReceived();
		}
		public void loaderHasStarted() {
			JASiGMLPlayer.this.handleLoadStarted();
		}
		public void nextSignLoaded(int s, int flimit) {
			JASiGMLPlayer.this.handleSignLoaded(s, flimit);
		}
		public void loaderIsDone(boolean gotframes, int nsigns, int nframes) {
			JASiGMLPlayer.this.handleLoadDone(gotframes, nsigns, nframes);
		}
		public void playerHasStarted() {
			// No action
		}
		public void playerIsAtNewFrame(AnimationScan scan,  boolean dropped) {
			JASiGMLPlayer.this.handleNewFrame(scan, dropped);
		}
		public void playerIsDone(AnimationScan scan) {
			JASiGMLPlayer.this.handlePlayDone(scan);
		}
	};

/** Handler method for "auto-play checkbox-change" event. */
	protected void handleAutoPlayChange() {

		this.player.setAutoPlay(this.chkAuto.isSelected());
	}

/** Handler method for "cyclic-play checkbox-change" event. */
	protected void handleCyclicPlayChange() {

		this.player.setCyclicPlay(this.chkCyclic.isSelected());
	}

/** Handler method for "single-sign-play checkbox-change" event. */
	protected void handleSingleSignPlayChange() {

		this.player.setSingleSignPlay(this.chkSingle.isSelected());
	}

/** Handler method to update GUI in response to a new selection on the
 * Avatars menu.
 */
	protected void setGUIForAvatarSwitch() {

		this.resetPlayerGUI();
		this.menuBar.setEnabledAvatarMenu(false);
	}

/** Resets the player GUI to its initial state, as when there
 * is no animation: Play and Stop buttons both disabled, frames
 * spinner has a maximum value of 0.
 */
	protected void resetPlayerGUI() {

		this.disablePlayButtons();
		this.spinFrameNo.resetModelToNeutral();
	}

/** Handler method for "avatar-loaded" event. */
	protected void handleAvatarLoaded(String avatar) {

		this.menuBar.setEnabledAvatarMenu(true);
		this.lblStatus.setText("Loaded avatar: "+avatar+".");
	}

/** Handler method for "avatar-unloaded" event. */
	protected void handleAvatarUnloaded(String avatar) {

		this.lblStatus.setText("Unloaded avatar: "+avatar+".");
	}

/** Handler method for "play-button-click" event. */
	protected void doPlayAction() {

		this.setPlayButtons(true);
		this.menuBar.setEnabledAvatarMenu(false);
		this.player.startPlaying();
	}

/** Handler method for "stop-button-click" event. */
	protected void doStopAction() {

		this.bttnStop.setEnabled(false);
		this.player.stopPlaying();
	}

/** Enables/disables Play and Stop buttons when player
 * starts or stops, as specified by the flag argument.
 */
	protected void setPlayButtons(boolean startnotstop) {

		this.bttnPlay.setEnabled(!startnotstop);
		this.bttnStop.setEnabled(startnotstop);
	}

	protected void disablePlayButtons() {

		this.bttnPlay.setEnabled(false);
		this.bttnStop.setEnabled(false);
	}

/** Handler method for "new-(SiGML)-input-received" event. */
	protected void handleInputReceived() {

		this.animLoadComplete = false;
		this.animPlayComplete = false;
		this.disablePlayButtons();
		this.menuBar.setEnabledAvatarMenu(false);
		this.lblStatus.setText("SiGML input received.");
	}

/** Handler for the player's (animation) "Load Started" event. */
	protected void handleLoadStarted() {

		this.spinFrameNo.startNewAnimation();
//		this.spinFrameNo.resetModelToNeutral();

		if (this.JA_OPTS.doStreamedAnimationBuild()) {
			this.bttnStop.setEnabled(true);
		}
	}

/** Handler for the player's (animation) "Sign Loaded" event. */
	protected void handleSignLoaded(int s, int flimit) {

		// (Nothing to do here.)
	}

/** Handler method for "animation-load-done" event. */
	protected void handleLoadDone(boolean loadok, int nsigns, int nframes) {

		this.animLoadComplete = true;

		if (this.animPlayComplete) {

			this.player.ensureAnimationIsComplete();

			this.setPlayButtons(false);
			this.menuBar.setEnabledAvatarMenu(true);		
		}
		else if (!this.player.autoPlayIsOn()) {

			this.animPlayComplete = true;

			this.setPlayButtons(false);
			this.menuBar.setEnabledAvatarMenu(true);

			this.lblStatus.setText(this.newAnimMessage(nsigns, nframes));
		} else if (!this.JA_OPTS.doStreamedAnimationBuild()) {

			this.setPlayButtons(loadok);
			this.menuBar.setEnabledAvatarMenu(!loadok);

			String message = "";
			if (loadok) {
				message = this.newAnimMessage(nsigns, nframes);
				this.spinFrameNo.startNewAnimation();
			}
			else {
				message = "SiGML input processing failure!";
			}

			this.lblStatus.setText(message);
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

		if (!dropped) {
			this.spinFrameNo.internalSetValue(scan.f());
		}
	}

/** Handler method for "player-done" event. */
	protected void handlePlayDone(AnimationScan scan) {

		this.animPlayComplete = true;

		if (this.animLoadComplete) {
			this.setPlayButtons(false);
			this.menuBar.setEnabledAvatarMenu(true);		
		}

		int s = scan.s();
		int f = scan.f();
		String smsg = (s<0 ? "" : ", sign="+s);
		this.lblStatus.setText("Player stopped: frame="+f+smsg+".");
	}

/** Shows sign-related information in the status panel. */
	protected void showSignInfo(int slimit, int s, String gloss) {

		this.lblStatus.setText(
			"[Limit="+slimit+"]  Sign "+s+":  \""+gloss+"\"");
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
			System.out.println(
				"####  JASiGMLPlayer  shut-down interrupted: "+ix);
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
		Dimension psz = this.mainPane.getSize();

		int[] xywh = new int[4];
		xywh[0] = (int) wloc.getX();
		xywh[1] = (int) wloc.getY();
		xywh[2] = (int) psz.getWidth();
		xywh[3] = (int) psz.getHeight();

		this.JA_OPTS.updateAppWindowLocationAndSize(xywh);
	}
}
