/*
 * SiGMLPlayer.java		2015-04-03
 *
 * Combines SiGML Service Player and SiGML URL Player applications.
 */

package app;


import java.lang.reflect.InvocationTargetException;

import java.util.Date;
import java.util.Properties;

import java.io.IOException;
import java.io.File;

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
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

import java.net.URL;

import jautil.AppletPropertiesSetter;
import jautil.JAEnv;
import jautil.JAAvatarsEnv;
import jautil.JAOptions;
import jautil.JATimer;
import jautil.JAIO;

import player.JASocketPlayer;
import player.JASocketPlayerEventHandler;
import player.JALoadingPlayer;
import player.JACanvasEmbedder;
import player.AnimationScan;
import player.AvatarEventHandler;

import app.gui.JASigningMenuBar;
import app.gui.FPSPane;
import app.gui.FrameNumberSpinner;
import app.gui.SpeedUpSlider;
import app.gui.QuitManager;
import app.gui.Wrap;

import static app.gui.FrameNumberSpinner.SignStatusHandler;
import java.net.URLDecoder;
import org.apache.logging.log4j.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoggerConfig;


/** SiGML Player app.
 * SiGML content may be contained in a URL  supplied by the user via a pair of text 
 * fields for base and file name.
 * SiGML content may be provided via a network socket
 */
public class SiGMLPlayer {

/** Logger. */
	private static Logger					logger = null;
/** SiGML Base option key name. */
	public static final String				SIGML_BASE_KEY = "sigml.base.uri";
/** SiGML File option key name. */
	public static final String				SIGML_FILE_KEY = "sigml.file";
/** Video Base option key name. */
	public static final String				VIDEO_BASE_KEY = "video.base.uri";
/** Video File option key name. */
	public static final String				VIDEO_FILE_KEY = "video.file";

/** Preempt or Queue option key name. */
	public static final String				PREEMPT_SIGML = "do.preempt.sigml";
/** Auto Run option key name. */
	public static final String				AUTO_RUN = "do.auto.run";
/** Auto Video generation option key name. */
	public static final String				AUTO_VIDEO = "do.auto.video";
/** Auto Quit option key name. */
	public static final String				AUTO_QUIT = "do.auto.quit";

	public static final String				JA_REMOTE_BASE_KEY = "ja.remote.base.url";
	public static final String				JA_VERSION_TAG_KEY = "ja.version.tag";
	public static final String				JA_REMOTE_BASE_STEM = "http://vhg.cmp.uea.ac.uk/tech/jas/";
	private static final String				DEFAULT_JA_VERSION = "std";

// Possible base URL and file strings for testing:
// UK:	"http://www.visicast.cmp.uea.ac.uk/sigml/test/sigml/"
//		"DEFKPR-ngt.sigml"
// DE:	"http://www.gebaerden.hamburg.de/sigml/";
//		"WillkHH_final_1.xml";

/** Main method -- creates a new instance of the app with the given CL args. */
	public static void main(String[] args)
	throws InterruptedException, InvocationTargetException, IOException {

		// Fix arguments in case of javaws call
		String [] fixedArgs = JAOptions.fixArgs(args);

        Properties argProps = AppletPropertiesSetter.argsToProperties(fixedArgs);

		String javntag = argProps.getProperty(JA_VERSION_TAG_KEY, DEFAULT_JA_VERSION);
		String javbase = argProps.getProperty(JA_REMOTE_BASE_KEY, JA_REMOTE_BASE_STEM);

		// Create Logger using local configuration file if found

		File logConf = new File(System.getProperty("user.dir")+"/.jasigning/"+javntag+"/log4j2.xml");
		if(logConf.isFile()) {
			System.setProperty("log4j.configurationFile", logConf.getAbsoluteFile().toURI().toString());
		} else {
			logConf = new File(System.getProperty("user.dir")+"/log4j2.xml");
			if(logConf.isFile()) {
				System.setProperty("log4j.configurationFile", logConf.getAbsoluteFile().toURI().toString());
			} else {
				System.setProperty("log4j.configurationFile", javbase+"log4j2.xml");
			}
		}

		logger = LogManager.getLogger();
		
		logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker,
			"Log4J2 config: "+System.getProperty("log4j.configurationFile", "<null>"));
		logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker, "Start Thread: SiGMLPlayer");
		logger.log(LoggerConfig.INFOLevel, LoggerConfig.SESSIONMarker, "SiGMLPlayer App Starting: Java "+System.getProperty("java.version"));
		
		SiGMLPlayer jasuapp = new SiGMLPlayer(fixedArgs, argProps);
		logger.log(LoggerConfig.INFOLevel, LoggerConfig.SESSIONMarker, "SiGMLPlayer App Created");
		logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"Ended Thread: SiGMLPlayer");
	}


/** App window left X coordinate. */
	private int							winX = -1;
/** App window top Y coordinate. */
	private int							winY = -1;
/** App window width. */
	private int							width = -1;
/** App window height. */
	private int							height = -1;

/** Options for this applet. */
	private JAOptions					JA_OPTS;
/** Avatars environment for this app. */
	private final JAAvatarsEnv			AVATARS_ENV;

/** App's main window. */
	private JFrame						window;
/** App's main main panel. */
	private JPanel						mainPane;
/** The inner panel containing the avatar's JA canvas.  */
	private JPanel						avatarPane;

/** Socket Player used by this app. */
	private JASocketPlayer				player;
/** Quit manager for this app. */
	private QuitManager					QUIT_MANAGER;

/** Menu bar for this app. */
	private JASigningMenuBar			menuBar;
/** Combo box for animgen FPS value (belongs to an FPSPane). */
	private JComboBox					comboFPS;
/** Auto-Playcheck-box (for "file: URL" selection). */
	private JCheckBox					chkAuto;
/** Cyclic-Play check-box. */
	private JCheckBox					chkCyclic;
/** Single-Sign Play check-box. */
	private JCheckBox					chkSingle;
/** Swing "file: URL" button. */
	private JButton						bttnFileURL;
/** Swing LOAD/PLAY button. */
	private JButton						bttnLoadPlay;
/** Swing REPLAY button. */
	private JButton						bttnReplay;
/** Swing STOP button. */
	private JButton						bttnStop;
/** Swing SAVE CAS button. */
	private JButton						bttnSaveCAS;
/** Swing URL text field. */
	private JTextField					txtBaseURL;
/** Swing URL text field. */
	private JTextField					txtSiGMLURL;
/** Frame Number spinner. */
	private FrameNumberSpinner			spinFrameNo;
/** Animation Speed slider. */
	private SpeedUpSlider				speedUpSlider;
/** Swing Status message label. */
	private JLabel						lblStatus;
/** File chooser for file: URLs. */
	private JFileChooser				chooser;

/** Animation sequence play completion flag for a PLAY operation. */
	private transient boolean			animPlayComplete = true;
/** Animation sequence load completion flag for a PLAY operation. */
	private transient boolean			animLoadComplete = true;

/** State of Script processing. */
	private enum scriptState			{ AvatarLoading, RunPlaying, VideoLoading, VideoPlaying, Completed };
	private scriptState					theScriptState = scriptState.AvatarLoading;

	private enum scriptEvent			{ AvatarLoaded, LoadStarted, LoadDone, PlayDone };
		
	protected final JPanel getAvatarPane() { return this.avatarPane; }

/** Creates a new SiGML-URL-App instance using the given command line
 * arguments to determine the options settings.
 */
	public SiGMLPlayer(String [] fixedArgs, Properties argProps)
	throws InterruptedException, InvocationTargetException, IOException {

		JATimer tmr = new JATimer(logger, LoggerConfig.DEBUGLevel, LoggerConfig.SESSIONMarker);
		
		AppletPropertiesSetter.copyStdAppProperties(argProps);

        // Get JA options and environment for this execution of the app.
		this.JA_OPTS =
			JAOptions.makeJAOptions(
				"SiGMLPlayer", fixedArgs, argProps, JAEnv.makeAppJAEnv());
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
			public void run() { //xyz
				logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: SiGMLPlayer GUI");
				SiGMLPlayer.this.createGUI(); 
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: SiGMLPlayer GUI");
			}
		};
		EventQueue.invokeAndWait(RUNNABLE_SET_UP_GUI);
		logger.log(LoggerConfig.TRACELevel, LoggerConfig.GUIMarker, "SiGMLPlayer: GUI set up done.");

		// Start SiGML and Switch-Avatar input servers if allowing Sockets
		// Probably good to suppress Switch-Avatar socket for this player
		if (true) {
			this.player.startSiGMLInput(this.JA_OPTS);
			this.player.startSwitchAvatarInput();
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.SESSIONMarker, 
				"SiGMLPlayer: SiGML and Switch-Avatar input services started.");
		}

			
		
		// Load the initial avatar into the player's view.
		// The avatarIsLoaded event will trigger script actions.
		String avatar = this.AVATARS_ENV.currentAvatar();
		this.player.requestSwitchAvatar(avatar);

		tmr.showTimeMS("SiGMLPlayer: Complete set-up time");
	}

/** Creates the GUI for this player app, packs it and displays it. */
	protected void createGUI() { // throws IOException {

		// Use the platform look-and-feel.
		String syslaf = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(syslaf);
		}
		catch (Exception xx) {
			logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SESSIONMarker, "SiGMLPlayer: createGUI failed: "+xx);
			logger.log(LoggerConfig.ERRORLevel, LoggerConfig.GUIMarker, "SiGMLPlayer: createGUI failed: "+xx);
		}

		// Create a frame window.
		this.window = new JFrame("SiGML Player");

		this.mainPane = this.makeMainPane();

		// Create the infrastructure used by the menu bar:
		// avatar-switcher, pre-quit sequence and quit-manager.
		final Runnable AVATAR_SWITCH = new Runnable() {
			public void run() { //xyz
				logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: SiGMLPlayer Avatar Switcher");
				SiGMLPlayer.this.setGUIForAvatarSwitch(); 
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: SiGMLPlayer Avatar Switcher");
			}
		};
		final Runnable PREPARE_FOR_QUIT = new Runnable() {
			public void run() { //xyz
				logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: SiGMLPlayer Prepare for quit");
				SiGMLPlayer.this.prepareShutDown(); 
				logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: SiGMLPlayer Prepare for quit");
			}
		};
		int qdelayms = this.JA_OPTS.getIntegerProperty("quit.delay.ms");
		this.QUIT_MANAGER = new QuitManager(PREPARE_FOR_QUIT, qdelayms);

		// Create the menu bar, which cannot be done until the player
		// and other infrastructure have been created.
		final boolean USE_AV_SUBMENUS = this.JA_OPTS.avatarMenuDoUseSubmenus();
		final String AV_SUBMENU_SPECS = this.JA_OPTS.avatarSubmenuSpecs();
		this.menuBar =
			new JASigningMenuBar(
				this.player, this.QUIT_MANAGER, this.VIDEO_GEN_ACTION,
				this.AVATARS_ENV,
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
		logger.log(LoggerConfig.TRACELevel, LoggerConfig.GUIMarker, "SiGMLPlayer: Window now displayed");
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

		JPanel middlepane =
			Wrap.wrapInNCSPane(this.makeURLPane(), this.avatarPane, null);

		JPanel mainpane =
			Wrap.wrapInNWCESPane(
				null, null, middlepane, this.makeControlPane(),
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

		this.txtBaseURL.setToolTipText(
			"Base URL, against which the SiGML URL is resolved if necessary.");
		this.txtSiGMLURL.setToolTipText(
			"SiGML URL -- resolved against the Base URL if necessary.");

		this.bttnFileURL.setToolTipText(
			"Using a file dialog, select a new \"file:\" URL.");
		this.chkAuto.setToolTipText(
			"Automatically play the newly selected \"file:\" URL or streamed SiGML data after loading.");

		this.comboFPS.setToolTipText(
			"Set the animation frame rate in frames/sec.,\n"+
			"should be between 0 & 1000 exclusive.");

		this.bttnLoadPlay.setToolTipText(
			"Load SiGML from the current URL, generate an\n"+
			"animation from it, and, if autoplay is selected, play it on the avatar.");
		this.bttnReplay.setToolTipText(
			"Play or replay the current SiGML animation.");
		this.bttnStop.setToolTipText(
			"Stop the current SiGML animation.");
		this.bttnSaveCAS.setToolTipText(
			"Save the animation data (CAS) for the current animation.");

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

/** Creates and returns a new Swing panel for the status message area. */
	protected JPanel makeStatusPane() {

		this.lblStatus = new JLabel("Starting player");
		JPanel spane =
			Wrap.wrapInFlowLayoutPane(this.lblStatus, FlowLayout.LEFT);

		spane.setBorder(
			BorderFactory.createTitledBorder("Status"));

		return spane;
	}

/** Creates and returns a swing URL panel with the given width.
 * The panel contains the URL text field, to which the PLAY action
 * listener is attached, so that the ENTER keystroke has the same
 * effect as the PLAY button.
 */
	protected JPanel makeURLPane() {

		// Labels for the two URL text fields -- forced to same size.
		JLabel lblbase = new JLabel("Base:   ");
		JLabel lblsigml = new JLabel("SiGML:  ");
		lblbase.setPreferredSize(lblsigml.getPreferredSize());

		// Get initial values for Base and SiGML URLs from the Options.
		String base =
			JAIO.forceBaseURL(this.JA_OPTS.getStringProperty(SIGML_BASE_KEY));
		if (base == null)
			base = this.JA_OPTS.defaultSiGMLBaseURL();

		String SIGML =
			this.JA_OPTS.getStringProperty(SIGML_FILE_KEY);

		URL furl = JAIO.resolveURL(base, SIGML);
		if (furl != null) {
			base = JAIO.tidyBaseURL(furl.toString());
			SIGML = JAIO.getLastPathStep(furl);
		}
		
		// Create the two text fields, and package each one with its label.
		this.txtBaseURL = new JTextField(JAIO.decodeURL(base));
		this.txtSiGMLURL = new JTextField(JAIO.decodeURL(SIGML));

		JPanel panebase =
			Wrap.wrapInWCEPane(lblbase, this.txtBaseURL, null);
		JPanel panesigml =
			Wrap.wrapInWCEPane(lblsigml, this.txtSiGMLURL, null);

		// Put two pixels between the two URL panes.
		panebase.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

		JPanel paneu = Wrap.wrapInNCSPane(panebase, null, panesigml);

		// On autoplay ENTER/Return on SiGML URL is equivalent to hitting PLAY.
		this.txtSiGMLURL.addActionListener(this.ENTER_SIGML_ACTION);

		paneu.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("URLs"),
				BorderFactory.createEmptyBorder(2, 2, 2, 2)));

		return paneu;
	}

/** Creates and returns a new Swing Play panel, containing
 * PLAY and STOP buttons with the appropriate action listeners attached.
 */
	private JPanel makeControlPane() {

		// Button and checkbox for file: URL.
		this.bttnFileURL = new JButton("file: URL");
		this.chkAuto = new JCheckBox("Auto-Play", true);

		this.bttnFileURL.addActionListener(this.FILE_ACTION);
		this.chkAuto.addItemListener(this.IL_AUTO);

		// Create FPS pane, which does its own event handling.
		FPSPane fpspane = new FPSPane(this.JA_OPTS);
		this.comboFPS = fpspane.getFPSComboBox();

		// Create the buttons, with action listeners attached.
		// Renamed from Load/Play as autoplay will deternmine play behaviour
		this.bttnLoadPlay = new JButton("Load");
		// Renamed from Replay as autoplay may suppress initial play
		this.bttnReplay = new JButton("Play");
		this.bttnStop = new JButton("Stop");
		this.bttnSaveCAS = new JButton("Save CAS...");

		this.bttnLoadPlay.addActionListener(this.PLAY_ACTION);
		this.bttnReplay.addActionListener(this.REPLAY_ACTION);
		this.bttnStop.addActionListener(this.STOP_ACTION);
		this.bttnSaveCAS.addActionListener(this.SAVE_CAS_ACTION);


		// Create the checkboxes, with change listeners attached.
		this.chkCyclic = new JCheckBox("Cyclic-Play", false);
		this.chkSingle = new JCheckBox("Single-Sign", false);
		this.chkCyclic.addItemListener(this.IL_CYCLIC);
		this.chkSingle.addItemListener(this.IL_SINGLE);

		// Create the other two controls, in their panes.
		JPanel framenopane = this.makeFrameNumberPane(8);
		JPanel speedpane = this.makeSpeedUpPane(16);

		// Pane for the URL-controls buttons: file: URL, Auto-Play. 
		final JComponent[] URL_CTRLS =
			{ this.bttnFileURL, this.chkAuto };
		JPanel paneurlctrls = Wrap.wrapInFLPaneAndVBox(URL_CTRLS, 16);

		// Pane for the buttons: FPS, Load/Play, Replay, Stop.
		final JComponent[] BUTTONS =
			{ fpspane, this.bttnLoadPlay, this.bttnReplay,
				this.bttnStop, this.bttnSaveCAS };
		JPanel panebuttons = Wrap.wrapInFLPaneAndVBox(BUTTONS, 24);

		// Pane for the checkboxes: Cyclic-Play, Single-Sign.
		final JComponent[] CHECKS = { this.chkCyclic, this.chkSingle };
		JPanel panechecks = Wrap.wrapInFLPaneAndVBox(CHECKS, 8, 0);

		// Pane for the others: Frame Number and Speed Up.
		final JComponent[] OTHERS = { framenopane, speedpane };
		JPanel paneothers = Wrap.wrapInFLPaneAndVBox(OTHERS, 0);

		final JComponent[] ALL =
			{ paneurlctrls, panebuttons, panechecks, paneothers };
		JPanel paneall = Wrap.wrapInFLPaneAndVBox(ALL, 0);

		paneall.setBorder(
//			BorderFactory.createCompoundBorder(
//				BorderFactory.createTitledBorder("Player Control"),
				BorderFactory.createEmptyBorder(0, 8, 0, 8));

		return paneall;
	}

/** Creates and returns a panel for the Frame Number spinner. */
	protected JPanel makeFrameNumberPane(int gap) {

		JLabel label = new JLabel("Frame:");

		final SignStatusHandler SS_HANDLER = new SignStatusHandler() {
			public void updateSignStatus(int slimit, int s, String gloss) {
				SiGMLPlayer.this.showSignInfo(slimit, s, gloss);
			}
		};

		this.spinFrameNo = new FrameNumberSpinner(SS_HANDLER);

		final JComponent[] CMPNNTS = { label, this.spinFrameNo };

		return Wrap.wrapInFLPaneAndVBox(CMPNNTS, gap, 0);
	}

/** Creates and returns a panel for the Speed Up slider. */
	protected JPanel makeSpeedUpPane(int gap) {

		JLabel labela = new JLabel("Speed Up");
		JLabel labelb = new JLabel("(log scale):");

		this.speedUpSlider = new SpeedUpSlider(SpeedUpSlider.VERTICAL);

		final JComponent[] CMPNNTS = { labela, labelb, this.speedUpSlider };

		return Wrap.wrapInFLPaneAndVBox(CMPNNTS, gap, 0);
	}

/** Embedder for the player's avatar canvas -- which it places in the
 * centre of this player's Border Layout.
 */
	private final JACanvasEmbedder				JA_CANVAS_EMBEDDER =
	new JACanvasEmbedder() {
		public void embedInContainer(Component jacanvas) {
			SiGMLPlayer.this.getAvatarPane().add(jacanvas, BorderLayout.CENTER);
		}
	};

/** Handler for avatar load/unload events -- delegates each event to
 * the appropriate one of our handler methods.
 */
	protected final AvatarEventHandler			AVATAR_EVENT_HANDLER =
	new AvatarEventHandler() {
		public void avatarIsLoaded(String avatar) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Avatar Event: avatarIsLoaded "+avatar);
			SiGMLPlayer.this.handleAvatarLoaded(avatar);
		}
		public void avatarIsUnloaded(String avatar) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Avatar Event: avatarIsUnloaded "+avatar);
			SiGMLPlayer.this.handleAvatarUnloaded(avatar);
		}
	};

//########  Listeners for GUI events.  ########

/** Action listener for the "file: URL" button -- delegates to
 * {@link #doFileURL()}.
 */
	private final ActionListener				FILE_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doFileURL();
		}
	};

/** Action listener for the PLAY button -- delegates to {@link #doPlay()}. */
	private final ActionListener				PLAY_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doPlay();
		}
	};

/** Action listener for the PLAY button -- delegates to {@link #doPlay()}. */
	private final ActionListener				ENTER_SIGML_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doPlay();
		}
	};

/** Action listener for the REPLAY button -- delegates to
 * {@link #doReplay()}.
 */
	private final ActionListener				REPLAY_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doReplay();
		}
	};

/** Action listener for the STOP button -- delegates to {@link #doStop()}. */
	private final ActionListener				STOP_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doStop();
		}
	};

/** Action listener for the SAVE-CAS... button -- delegates to
 * {@link #doSaveCAS()}.
 */
	private final ActionListener				SAVE_CAS_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doSaveCAS();
		}
	};

/** Action listener for the VIDEO-GEN... menu item -- delegates to
 * {@link #doVideoGen()}.
 */
	private final ActionListener				VIDEO_GEN_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			SiGMLPlayer.this.doVideoGen( true );
		}
	};

/** Auto-play checkbox's item listener -- delegates
 * the item-state-changed event to our auto-play-change method.
 */
	protected ItemListener						IL_AUTO =
	new ItemListener() {
		public void itemStateChanged(ItemEvent ievt) {
			SiGMLPlayer.this.handleAutoPlayChange();
		}
	};

	/** Cyclic-play checkbox's item listener -- delegates the
 * item-state-changed event handling to our cyclic-play-change method.
 */
	protected final ItemListener				IL_CYCLIC =
	new ItemListener() {
		public void itemStateChanged(ItemEvent ievt) {
			SiGMLPlayer.this.handleCyclicPlayChange();
		}
	};

/** Single-Sign checkbox's item listener -- delegates the
 * the item-state-changed event to our cyclic-play-change method.
 */
	protected final ItemListener				IL_SINGLE =
	new ItemListener() {
		public void itemStateChanged(ItemEvent ievt) {
			SiGMLPlayer.this.handleSingleSignPlayChange();
		}
	};

//########  Handler for Player events.  ########

/** Handler for Player events -- delegates to {@link #handleLoadDone()},
 * {@link #handleNewFrame()} and {@link #handlePlayDone()} etc.
 */
	private final JASocketPlayerEventHandler	PLAYER_EVENT_HANDLER =
	new JASocketPlayerEventHandler() {
		public void sigmlInputReceived() {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: sigmlInputReceived");
			SiGMLPlayer.this.handleInputReceived();
		}
		public void loaderHasStarted() {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: loaderHasStarted");
			SiGMLPlayer.this.handleLoadStarted();
		}
		public void nextSignLoaded(int s, int flimit) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: nextSignLoaded");
			SiGMLPlayer.this.handleSignLoaded(s, flimit);
		}
		public void loaderIsDone(boolean gotframes, int nsigns, int nframes) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: loaderIsDone");
			SiGMLPlayer.this.handleLoadDone(gotframes, nsigns, nframes);
		}
		
		public void playerHasStarted() {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: playerHasStarted");
			SiGMLPlayer.this.handlePlayStarted();
		}
		
		public void playerIsAtNewFrame(AnimationScan scan,  boolean dropped) {
			// Supporess for now
			// logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: playerIsAtNewFrame");
			SiGMLPlayer.this.handleNewFrame(scan, dropped);
		}
		public void playerIsDone(AnimationScan scan) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.EVENTMarker, "Player Event: playerIsDone");
			SiGMLPlayer.this.handlePlayDone(scan);
		}
	};

//########  Handler methods for GUI events.  ########

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

/** Handler method for "file: URL" button -- tries to update the GUI's
 * URL settings, using a file dialog, and then plays the SiGML at this
 * URL if the auto-play checkbox is set.
 */
	protected void doFileURL() {

		if (this.chooser == null) {

			this.chooser =new JFileChooser();

			final String TITLE = "Select SiGML File for file: URL";
			chooser.setDialogTitle(TITLE);
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}

		// The conversions which follow are not very efficient, but
		// their logic is straightforward.
		// JRWG TODO: How are these initialised in case they are not "normalised" to dir and file?
		String base = JAIO.decodeURL(JAIO.forceBaseURL(this.txtBaseURL.getText()));
		String sigml = JAIO.decodeURL(this.txtSiGMLURL.getText());

		// If current URL is a file URL then use it to set the file
		// dialog's starting point within the file system.
		// JRWG TODO: Maybe update fields anyway in case choice is cancelled
		URL furl = JAIO.resolveURL(base, sigml);
		if (JAIO.isFileURL(furl)) {

			File[] baseandfile = JAIO.baseAndFileForFileURL(furl);
			if (baseandfile != null) {
				chooser.setCurrentDirectory(baseandfile[0]);
				chooser.setSelectedFile(baseandfile[1]);
				chooser.ensureFileIsVisible(baseandfile[1]);
			}
		}

		// Show the dialog and try to get the user to select a new file.
		File newfile = null;
		int opt = chooser.showDialog(null, "SiGML File");
		if (opt == JFileChooser.APPROVE_OPTION)
			newfile = chooser.getSelectedFile();

		chooser.setSelectedFile(null);

		// If the user supplied a new file ...
		if (newfile != null) {

			// Extract the new base and (relative) SiGML URLs and use
			// them to update the URL text fields.
			String ubase = JAIO.fileToURL(newfile.getParentFile()).toString();
			String usigml = newfile.getName();
			this.txtBaseURL.setText(ubase);
			this.txtSiGMLURL.setText(usigml);

			// If Auto-Play is selected will treat this update as if
			// the user also clicked PLAY.
			this.doPlay();
		}
}

/** Performs the PLAY action, attempting to obtain a valid SiGML URL
 * from the URL text area, resolving that text against the default
 * SiGML base, and starting a thread to load and play the SiGML document
 * thus identified, enabling/disabling the PLAY button and setting the
 * Status message as appropriate.
 */
	protected void doPlay() {

		String errmsg = null;
		URL sigmlurl = null;

		// Try to establish the SiGML URL using the text fields.
		String rawurl = JAIO.decodeURL(this.txtSiGMLURL.getText());
		if (rawurl.length() == 0) {
			errmsg = "Blank SiGML URL string.";
		}
		else {
			final String S_BASE = JAIO.decodeURL(JAIO.forceBaseURL(this.txtBaseURL.getText()));
			sigmlurl = JAIO.resolveURL(S_BASE, rawurl);
			if (sigmlurl == null)
				errmsg = "Bad SiGML URL: "+rawurl;
		}
		
		logger.log(LoggerConfig.LOGLevel,LoggerConfig.COMMANDMarker,
				"doPlay: URL "+sigmlurl+(errmsg==null?"":" ["+errmsg+"]"));
		
		this.setGUIForStartPlay();

		if (sigmlurl == null) {

			// No SiGML URL: revert to status quo ante.
			this.setStatus(errmsg);
			this.setGUIForAnimationLoadDone(false);
		}
		else {
			// Update text fields to ensure normalised
			this.txtBaseURL.setText(JAIO.tidyBaseURL(sigmlurl.toString()));
			this.txtSiGMLURL.setText(JAIO.getLastPathStep(sigmlurl));
			// Create and start a thread to run the player on the SiGML.
			this.animLoadComplete = false;
			this.animPlayComplete = false;
			final URL  SIGML_URL = sigmlurl;
			final Thread  T_RUN_PLAYER  = new Thread() {
				public void run() { //xyz
					logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: Player playSiGML");
					try {
						SiGMLPlayer.this.playSiGML(SIGML_URL);
					}
					catch (InterruptedException ix) {
						logger.log(LoggerConfig.WARNLevel, LoggerConfig.CONTROLMarker,
							"SiGMLPlayer: playSiGML() interrupted: "+ix);
					}
					logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: Player playSiGML");
				}
			};
			T_RUN_PLAYER.start();
		}
	}

/** Plays the given SiGML URL on this app's player. */
	protected void playSiGML(URL sigmlurl) throws InterruptedException {

		this.player.playSiGMLURL(sigmlurl, this.player.autoPlayIsOn());
	}

/** Replays the animation for the current SiGML URL on this applet's player. */
	protected void doReplay() {

		// NB  The check for animation data should be redundant,
		// because the disabling of the REPLAY button should prevent
		// us reaching this point if there is no data.
		
		if (this.player.hasAnimationData()) {

			this.setGUIForAnimationLoadDone(true);

			// TBD
			// Do we really need another thread here?  the player's
			// replay() method immediately creates an animation play
			// thread in any case?

			final Thread  T_RUN_REPLAY  = new Thread() {
				public void run() { //xyz
					logger.log(LoggerConfig.INFOLevel, LoggerConfig.THREADMarker,"Run: Player replaySiGML");
					SiGMLPlayer.this.replaySiGML();
					logger.log(LoggerConfig.STATSLevel, LoggerConfig.THREADMarker,"End: Player replaySiGML");
				}
			};
			T_RUN_REPLAY.start();
		} else {
			logger.log(LoggerConfig.ERRORLevel, LoggerConfig.CONTROLMarker,
				"SiGMLPlayer.doReplay(): Animation data missing.");
		}
	}

/** Replay's the current SiGML animation on this app's player. */
	protected void replaySiGML() {

		this.player.replay();
	}

/** Performs the STOP action, disabling the STOP button and stopping
 * the player.
 */
	protected void doStop() {

		this.bttnStop.setEnabled(false);
		this.player.stopPlaying();
	}

/** Performs the SAVE CAS action. */
	protected void doSaveCAS() {

		JFileChooser chooser = new JFileChooser();
		final String TITLE = "Save CAS file where?";
		chooser.setDialogTitle(TITLE);
		// Get the user to choose the CAS output file.
		int opt = chooser.showSaveDialog(this.window);
		if (opt == JFileChooser.APPROVE_OPTION) {
			final File CAS_FILE = chooser.getSelectedFile();
			if (this.casFileCanBeUsed(CAS_FILE)) {
				this.player.saveCAS(
					this.AVATARS_ENV.currentAvatar(), CAS_FILE);
			}
		}
	}

/** Checks whether the given CAS output file is usable, which will involve
 * a dialogue with the user if the file already exists within the file
 * system.
 */
	private boolean casFileCanBeUsed(File casfile) {

		// Initially assume the file cannot be used.
		boolean ok = false;
		if (casfile != null) {
			// The file (path) object exists, so assume it can be used.
			ok = true;
			// But we need to check whether the file system already has
			// a file with this path.
			if (casfile.exists()) {
				// The file system does already have a file with this
				// path, so check whether the user is happy to
				// overwrite it.
				final int OVRWRT_CHOICE =
					JOptionPane.showConfirmDialog(
						this.window,
						"File \""+casfile.getName()+"\" already exists."+
						"  Overwrite?",
						"Overwrite Existing File?",
						JOptionPane.YES_NO_OPTION);
				ok = (OVRWRT_CHOICE == JOptionPane.YES_OPTION);
			}
		}
		return ok;
	}

	private static final String				NO_VIDEO_SERVICE_MSG =
	"Video generation is not possible unless you have\n"+
	"the JASigning video service app running locally.";
	private static final String				BAD_PLAYER_STATE_FOR_VIDEO_MSG =
	"The SiGML player is not currently in a suitable state\n"+
	"for video generation -- e.g. no avatar or no animation.";

/** Performs the VIDEO GENERATION action. */
	protected void doVideoGen( boolean withDialog ) {

		if (! this.player.ServiceIsAvailable()) {
			logger.log(LoggerConfig.INFOLevel, LoggerConfig.VIDEOMarker, 
				"JASigning video service app not running");
			JOptionPane.showMessageDialog(
					null, NO_VIDEO_SERVICE_MSG,
					"No Video Service",
					JOptionPane.ERROR_MESSAGE);
		}
		else if (! this.player.GenIsPossible()) {
			logger.log(LoggerConfig.INFOLevel, LoggerConfig.VIDEOMarker, 
				"No animation for video generation");
			JOptionPane.showMessageDialog(
					null, BAD_PLAYER_STATE_FOR_VIDEO_MSG,
					"Player Cannot Do Video Now",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			// Based on URL chooser code
			// The conversions which follow are not very efficient, but
			// their logic is straightforward.
			String vidBase =
				JAIO.decodeURL(JAIO.forceBaseURL(this.JA_OPTS.getStringProperty(VIDEO_BASE_KEY)));

			String vidFile =
				JAIO.decodeURL(this.JA_OPTS.getStringProperty(VIDEO_FILE_KEY));

			// If current URL is a file URL then use it to set the file
			// dialog's starting point within the file system.
			URL furl = JAIO.resolveURL(vidBase, vidFile);
			logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.VIDEOMarker, 
				"Video File URL "+furl );
			File videopath = null;
			File[] baseandfile = (JAIO.isFileURL(furl) ? JAIO.baseAndFileForFileURL(furl) : null);

			if (withDialog) {
				JFileChooser vidChooser = new JFileChooser();
				final String TITLE = "Save New .mov Video File Where?";
				vidChooser.setDialogTitle(TITLE);
				
				if (baseandfile != null) {
					vidChooser.setCurrentDirectory(baseandfile[0]);
					vidChooser.setSelectedFile(baseandfile[1]);
					vidChooser.ensureFileIsVisible(baseandfile[1]);
				}

				int opt = vidChooser.showSaveDialog(this.window);
				if (opt == JFileChooser.APPROVE_OPTION) {
					File selpath = vidChooser.getSelectedFile();
					File vparent = selpath.getParentFile();
					String vname = JAIO.decodeURL(selpath.getName());
					if (!vname.endsWith(".mov")) {
						vname = vname+".mov";
					}
					videopath = new File(vparent, vname);
					// Update property settings according to chosen file
					String vbase = JAIO.fileToURL(vparent).toString();
					this.JA_OPTS.updateStringProperty(VIDEO_BASE_KEY, vbase);
					this.JA_OPTS.updateStringProperty(VIDEO_FILE_KEY, vname);
				}
			} else if (baseandfile != null) {
				File vparent = baseandfile[0];
				String vname = baseandfile[1].getName();
				if (!vname.endsWith(".mov")) {
					vname = vname+".mov";
				}
				videopath = new File(vparent, vname);
			}
			
			if (videopath != null) {
				logger.log(LoggerConfig.DEBUGLevel, LoggerConfig.VIDEOMarker,
					"doVideoGen(): videopath "+videopath );
				boolean dolog = this.JA_OPTS.getBooleanProperty("do.video.gen.log");
				try {
					this.player.doVideoGen(videopath.getCanonicalPath(), dolog);
				} catch (IOException iox) {
					logger.log(LoggerConfig.WARNLevel, LoggerConfig.VIDEOMarker,
						"doVideoGen(): "+iox );
				}
			} else {
				logger.log(LoggerConfig.INFOLevel, LoggerConfig.VIDEOMarker,
					"Video generation cancelled" );
			}
		}
	}

//########  Handler methods for Player events.  ########

/** Handler method for "avatar-loaded" event. */
	protected void handleAvatarLoaded(String avatar) {

		// Do not change GUI if playing
		// JRWG but need to change selected avatar
		if (this.bttnStop.isEnabled()) {
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.GUIMarker,
				"Avatar "+avatar+" loaded during play");
		} else {
			this.setButtonsStatus(true, true, false, false, false);
			logger.log(LoggerConfig.TRACELevel, LoggerConfig.GUIMarker,
				"Avatar "+avatar+" loaded while stopped");
			this.menuBar.setEnabledAvatarMenu(true);
			// Use this to trigger script actions
			this.ProgressScript(scriptEvent.AvatarLoaded);
		}
		this.menuBar.enableAvatarMenuEntry(avatar);
		this.setStatus("Loaded avatar: "+avatar);
	}

/** Handler method for "avatar-unloaded" event. */
	protected void handleAvatarUnloaded(String avatar) {

		this.setStatus("Unloaded avatar: "+avatar+".");
	}

/** Handler method for "new-(SiGML)-input-received" event. */
	protected void handleInputReceived() {

		this.animLoadComplete = false;
		this.animPlayComplete = false;
		
		// GUI as for Load/Play
		this.setGUIForStartPlay();
		this.lblStatus.setText("SiGML input received.");
	}

	/** Handler for the player's (animation) "Load Started" event. */
	protected void handleLoadStarted() {

		this.spinFrameNo.startNewAnimation();

		if (this.JA_OPTS.doStreamedAnimationBuild()) {
			this.bttnStop.setEnabled(true);
		}
		this.ProgressScript(scriptEvent.LoadStarted);
	}

/** Handler for the player's (animation) "Sign Loaded" event. */
	protected void handleSignLoaded(int s, int flimit) {

		// (Nothing to do here.)
	}

/** Handler for the player's (animation) "Load Done" event, with the given
 * success/failure status flag -- responds by enabling/disabling the
 * Play panel buttons accordingly and by setting the status message
 * accordingly.
 */
	protected void handleLoadDone(boolean loadok, int nsigns, int nframes) {

		this.animLoadComplete = true;

		if (this.animPlayComplete) {

			this.player.ensureAnimationIsComplete();
			this.setGUIForPlayDoneOK();
		} else if (! this.player.autoPlayIsOn()) {
			
			this.animPlayComplete = true;

			if (loadok) {
				this.setGUIForPlayDoneOK();
			} else {
				this.setGUIForAnimationLoadDone(false);
			}
			this.setStatus(this.newAnimMessage(nsigns, nframes));
			
		} else if (! this.JA_OPTS.doStreamedAnimationBuild()) {

			this.setGUIForAnimationLoadDone(loadok);

			String statusmsg = null;
			if (loadok) {
				statusmsg = this.newAnimMessage(nsigns, nframes);
				// ... at present this status message is very short-lived.
				this.spinFrameNo.startNewAnimation();
			}
			else {
				statusmsg = "SiGML processing failure.";
			}

			this.setStatus(statusmsg);
		}
		// Consider script actions
		this.ProgressScript(scriptEvent.LoadDone);
	}

	/** Returns a "New animation ..." message for the given sign and frame counts. */
	protected String newAnimMessage(int nsigns, int nframes) {

		String smsg = (0 < nsigns ? nsigns+" signs: " : "");
		return "New animation: "+smsg+nframes+" frames generated.";
	}

/** Handler for a "Player Started" event.
 * No action required
 */
	protected void handlePlayStarted() {
		
	}
	
/** Handler for a "New Frame" event with the given animation scan and
 * frame-dropped flag, the event being generated from an auto-play thread
 * within the player; this method responds by updating the status
 * message if (and only if) the scan is at a new sign, and by updating
 * the frame number spinner's value.
 */
	protected void handleNewFrame(AnimationScan scan, boolean dropped) {

		if (scan.scanIsAtNewSign()) {
			int ss = scan.sCount();
			int s = scan.s();
			String gloss = scan.sign().getGloss();
			this.showSignInfo(ss, s, gloss);
		}

		if (!dropped) {
			this.spinFrameNo.internalSetValue(scan.f());
		}
	}

/** Handler for a "Play Done" event with the given animation scan,
 * the event being generated from an auto-play thread within
 * the player; provided the animation load is complete, this method
 * responds to the event by enabling/disabling the Play panel
 * buttons accordingly.
 */
	protected void handlePlayDone(AnimationScan scan) {

		this.animPlayComplete = true;

		if (this.animLoadComplete) {
			// Account for an empty scan
			if (scan != null && scan.fCount() > 0) {
				this.setGUIForPlayDoneOK();
			} else {
				// Leaves replay unavailable
				this.setGUIForAnimationLoadDone(false);
			}
		}
		// Consider script actions
		this.ProgressScript(scriptEvent.PlayDone);
	}

//########  GUI controls' status management.  ########

/** Handler method to update GUI in response to a new selection on the
 * Avatars menu.
 */
	protected void setGUIForAvatarSwitch() {

		this.resetPlayerGUI();
		this.menuBar.setEnabledAvatarMenu(false);
	}

/** Sets the enabled status of the "file: URL", Load/Play, Replay,
 * and Stop buttons as specified respectively by the given flags.
 */
	protected void setButtonsStatus(
		boolean fuon, boolean lpon, boolean ron, boolean son, boolean con) {

		this.bttnFileURL.setEnabled(fuon);
		this.bttnLoadPlay.setEnabled(lpon);
		this.bttnReplay.setEnabled(ron);
		this.bttnStop.setEnabled(son);
		this.bttnSaveCAS.setEnabled(con);
		// Log based on resulting status
		logger.log(LoggerConfig.LOGLevel, LoggerConfig.GUIMarker,
			"Buttons: File"+(this.bttnFileURL.isEnabled()?"+":"-")+
			" Load"+(this.bttnLoadPlay.isEnabled()?"+":"-")+
			" Play"+(this.bttnReplay.isEnabled()?"+":"-")+
			" Stop"+(this.bttnStop.isEnabled()?"+":"-")+
			" CAS"+(this.bttnSaveCAS.isEnabled()?"+":"-"));
	}

/** Resets the player GUI to a basic state: all buttons disabled,
 * and [0..0] as the entire value range for the frame number spinner.
 */
	protected void resetPlayerGUI() {

		this.setButtonsStatus(false, false, false, false, false);
		this.spinFrameNo.resetModelToNeutral();
	}

/** Sets the GUI controls' status for an animation start-play event. */
	protected void setGUIForStartPlay() {

		this.setButtonsStatus(false, false, false, false, false);
		this.menuBar.setEnabledAvatarMenu(false);
	}

/** Sets the GUI controls' status for an animation load-completed event,
 * successful or not, as indicated by the flag argument.
 */
	protected void setGUIForAnimationLoadDone(boolean loadok) {

		this.setButtonsStatus(!loadok, !loadok, false, loadok, false);
		// JRWG: Not sure about this choice
		this.menuBar.setEnabledAvatarMenu(!loadok);
	}

/** Sets the GUI controls' status for an animation play-completed event. */
	protected void setGUIForPlayDoneOK() {

		this.setButtonsStatus(true, true, true, false, true);
		this.menuBar.setEnabledAvatarMenu(true);
	}

//########  Status message display.  ########

/** Shows sign-related information in the status panel. */
	protected void showSignInfo(int slimit, int s, String gloss) {

		this.setStatus(
			"[Limit="+slimit+"]  Sign "+s+":  \""+gloss+"\"");
	}

/** Puts the given message in the status label. */
	protected void setStatus(String msg) {

		// Set the status label width to 400, and then update its text.
		int h = this.lblStatus.getHeight();
		this.lblStatus.setSize(400,  h);	// ???? (looks precarious)
		this.lblStatus.setText(msg);
	}

//########  Shut-down support.  ########

/** Handler for the window-close or quit events: stops the player,
 * flushes window-size, and kills the player.
 */
	protected void prepareShutDown() {

		try {
			// At this stage, stop the player, but don't kill its canvas.
			this.player.stopPlaying();

			this.AVATARS_ENV.terminate();

			// Flush some preferences data.
			this.updateWindowData();
			this.updateSiGMLURLData();

			// Now really wipe the player and its canvas.
			this.player.terminate();

			logger.log(LoggerConfig.TRACELevel, LoggerConfig.SESSIONMarker,
				"SiGMLPlayer: Shut-down preparation done");
		}
		catch (InterruptedException ix) {
			logger.log(LoggerConfig.WARNLevel, LoggerConfig.SESSIONMarker,
				"SiGMLPlayer: Shut-down interrupted: "+ix);
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

/** Updates the user preferences with the current SiGML URL details. */
	protected void updateSiGMLURLData() {

		final String BASE = this.txtBaseURL.getText();
		final String FILE = this.txtSiGMLURL.getText();

		if (BASE.length() != 0 && FILE.length() != 0) {
			this.JA_OPTS.updateStringProperty(SIGML_BASE_KEY, BASE);
			this.JA_OPTS.updateStringProperty(SIGML_FILE_KEY, FILE);
		}
	}

/** Call the Quit Manager doQuit method. */
	
	protected void doQuit() {
		this.QUIT_MANAGER.getQuitRunnable().run();
	}

/** Handle settings for automatic playing, video generation, and quitting.
 * Relies on each significant action creating a thread to run the action,
 * so the call will exit quickly.
 * Call initially, then after PlayerIsDone events. May stall and give odd
 * effects if there are errors. Could make actions notify failure if there
 * will not be future player events.
 * Handle AUTO_RUN then AUTO_VIDEO then AUTO_QUIT as provided.
 */
	protected void ProgressScript(scriptEvent event) {
		// Log autoscripting settings
		scriptState oldState = this.theScriptState;
		logger.log(LoggerConfig.TRACELevel, LoggerConfig.SCRIPTMarker,
			"Script event: "+event+", old state: "+oldState);
		if(this.JA_OPTS.getBooleanProperty(AUTO_RUN)) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.SCRIPTMarker,
				"Script command: " + AUTO_RUN);
		}
		if(this.JA_OPTS.getBooleanProperty(AUTO_VIDEO)) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.SCRIPTMarker,
				"Script command: " + AUTO_VIDEO);
		}
		if(this.JA_OPTS.getBooleanProperty(AUTO_QUIT)) {
			logger.log(LoggerConfig.LOGLevel, LoggerConfig.SCRIPTMarker, "Script command: " + AUTO_QUIT);
		}
		// Restore the autoplay setting in the player if necessary.
		this.handleAutoPlayChange();
		// Handle settings in order
		switch (oldState){
			case AvatarLoading:
				switch (event) {
					case AvatarLoaded:
						if (this.JA_OPTS.getBooleanProperty(AUTO_RUN)) {
							// AUTO_RUN selected
							this.JA_OPTS.updateBooleanProperty(AUTO_RUN, false);
							this.player.setAutoPlay(true);
							// Loads and plays given SiGML file
							this.theScriptState = scriptState.RunPlaying;
							this.doPlay();
						} else if (this.JA_OPTS.getBooleanProperty(AUTO_VIDEO)) {
							// AUTO_VIDEO selected
							this.JA_OPTS.updateBooleanProperty(AUTO_VIDEO, false);
							this.player.setAutoPlay(false);
							// Loads given SiGML file but deos not play
							this.theScriptState = scriptState.VideoLoading;
							this.doPlay();
						} else if (this.JA_OPTS.getBooleanProperty(AUTO_QUIT)) {
							// AUTO_QUIT selected
							this.JA_OPTS.updateBooleanProperty(AUTO_QUIT, false);
							setScriptCompleted();
							this.doQuit();
						} else {
							// Later trigger other actions perhaps
							setScriptCompleted();
						}
						break;
					default:
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SCRIPTMarker,
							"Script: Unexpected event: "+event+" in state "+oldState);
						setScriptCompleted();
						break;
				}
				break;
				
			case RunPlaying:
				switch (event) {
					case LoadStarted:
					case AvatarLoaded:
					case LoadDone:
						// No action
						break;
					case PlayDone:
						if (this.JA_OPTS.getBooleanProperty(AUTO_VIDEO)) {
							// AUTO_VIDEO selected
							this.JA_OPTS.updateBooleanProperty(AUTO_VIDEO, false);
							// SiGML file already loaded
							this.theScriptState = scriptState.VideoPlaying;
							this.doVideoGen( false );
						} else if (this.JA_OPTS.getBooleanProperty(AUTO_QUIT)) {
							// AUTO_QUIT selected
							this.JA_OPTS.updateBooleanProperty(AUTO_QUIT, false);
							setScriptCompleted();
							this.doQuit();
						} else {
							// Later trigger other actions perhaps
							setScriptCompleted();
						}
						break;
					default:
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SCRIPTMarker,
							"Script: Unexpected event: "+event+" in state "+oldState);
						setScriptCompleted();
						break;
				}
				break;
				
			case VideoLoading:
				switch (event) {
					case LoadStarted:
						// No action
						break;
					case LoadDone:
						this.doVideoGen( false );
						this.theScriptState = scriptState.VideoPlaying;
						break;
					default:
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SCRIPTMarker,
							"Script: Unexpected event: "+event+" in state "+oldState);
						setScriptCompleted();
						break;
				}
				break;
				
			case VideoPlaying:
				switch (event) {
					case AvatarLoaded:
						// No action
						break;
					case PlayDone:
						if (this.JA_OPTS.getBooleanProperty(AUTO_QUIT)) {
							// AUTO_QUIT selected
							this.JA_OPTS.updateBooleanProperty(AUTO_QUIT, false);
							setScriptCompleted();
							this.doQuit();
						} else {
							setScriptCompleted();
						}
						break;
					default:
						logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SCRIPTMarker,
							"Script: Unexpected event: "+event+" in state "+oldState);
						setScriptCompleted();
						break;
				}
				break;
				
			case Completed:
				// Ignore further events
				break;
				
			default:
				logger.log(LoggerConfig.ERRORLevel, LoggerConfig.SCRIPTMarker,
					"Script: Unexpected event: "+event+" in state "+oldState);
				setScriptCompleted();
				break;
		}
		logger.log((this.theScriptState == oldState ? LoggerConfig.TRACELevel : LoggerConfig.LOGLevel), LoggerConfig.SCRIPTMarker, 
			"Script event: "+event+", old state: "+oldState+", new state: "+this.theScriptState);
	}
	
	protected void setScriptCompleted() {
		this.theScriptState = scriptState.Completed;
		// Wanted to clear times only when a new load was started. However, player
		// started events can happen first so we must lose the chance to replay
		// with the same start and end times.
		// Clear player begin and end times in case set
		// Would rather use null but that leaves the property unchanged
		if (false) {
			this.JA_OPTS.updateStringProperty(JAOptions.PLAYER_BEGIN, "0");
			this.JA_OPTS.updateStringProperty(JAOptions.PLAYER_END, "0");
		}
	}
}
