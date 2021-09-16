/*
 * JASiGMLURLPlayer.java		2007-05-14
 */
package app;


import java.applet.Applet;

import java.util.Date;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Component;
import java.awt.Container;
import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.net.URL;

import jautil.JAIO;
import jautil.JAOptions;
import jautil.JAEnv;
import jautil.AppletPropertiesSetter;

import player.JALoadingPlayer;
import player.JALoadingPlayerEventHandler;
import player.JACanvasEmbedder;
import player.AnimationScan;


/** Simple JA demonstrator applet, which plays the SiGML file
 * at a given URL, as defined by a user-supplied text field.
 */
public class JASiGMLURLPlayer extends Applet {

/** Options for this applet. */
	private JAOptions					JA_OPTIONS;
/** Loading Player used by this applet. */
	private JALoadingPlayer				player;

/** Flag: Use AWT or Swing GUI toolkit. */
	private boolean						useAWT;

// AWT components.

/** AWT PLAY button. */
	private Button						bttnPlay;
/** AWT STOP button. */
	private Button						bttnStop;
/** AWT URL text field. */
	private TextField					txtURL;
/** AWT Status message label. */
	private Label						lblStatus;

// Swing components.

/** Swing PLAY button. */
	private JButton						bttnJPlay;
/** Swing STOP button. */
	private JButton						bttnJStop;
/** Swing URL text field. */
	private JTextField					txtJURL;
/** Swing Status message label. */
	private JLabel						lblJStatus;

/** Constructs a new instance of this applet. */
	public JASiGMLURLPlayer() {
		super();
	}

/** Initialises this applet, setting up its GUI and establishing the
 * initial avatar.
 */
	public synchronized void init() {

		String jrevn = System.getProperty("java.version");
		System.out.println((new Date())+"   Java version "+jrevn);
		System.out.println("####  JA-SiGML-URL-Player Applet  ####");

		// Copy appropriate HTML parameter settings to the system properties.
		AppletPropertiesSetter.copyStdAppletProperties(this);

		// Get JARP options for this execution of the applet.
		String optspath = this.getParameter("options");
		String prefs = "JASiGMLURLPlayer";
		// -session:  changes apply for this session only.
		String[] args = { "-session", optspath };
		if (optspath == null)
			args = null;
		JAEnv jaenv =  JAEnv.makeAppletJAEnv(this.getCodeBase());
		this.JA_OPTIONS = JAOptions.makeJAOptions(prefs, args, this, jaenv);

		this.useAWT = this.JA_OPTIONS.doForceAWT();

		// Create the applet GUI.
		this.createGUI();
		System.out.println("####  JA-URL-Player-Applet: GUI created.");

		// Determine the initial avatar and load it.
		String avatar = this.JA_OPTIONS.getAvatarsEnv().currentAvatar();
		this.player.requestSwitchAvatar(avatar);
	}

/** Starts this applet -- a no-op. */
	public synchronized void start() {
	}

/** Stops this applet, i.e. stops any current animation, and updates
 *  the window location and size.
 */
	public synchronized void stop() {

		this.player.stopPlaying();

		// Although this is an applet, we update the window data
		// for the sake of a JNLP applet (although it probably
		// won't make any difference).
		Component ancestor = this;
		while (ancestor.getParent() != null)
			ancestor = ancestor.getParent();

		Point wloc = ancestor.getLocation();
		Dimension psz = this.getSize();

		int[] xywh = new int[4];
		xywh[0] = (int) wloc.getX();
		xywh[1] = (int) wloc.getY();
		xywh[2] = (int) psz.getWidth();
		xywh[3] = (int) psz.getHeight();

		this.JA_OPTIONS.updateAppWindowLocationAndSize(xywh);
	}

/** Destroys this applet, disposing of all its JA and GL resources. */
	public synchronized void destroy() {

		try {
			this.player.terminate();
			System.out.println("####  JA-URL-Player-Applet: destroy() done.");
		}
		catch (InterruptedException ix) {
			System.out.println(
				"####  JA-URL-Player-Applet: shut-down interrupted: "+ix);
		}
		catch (Exception x) {
			System.out.println("####  JA-URL-Player-Applet: destroy() ...");
			x.printStackTrace(System.out);
		}
	}

/** Embedder for the player's avatar canvas -- which it places in the
 * centre of this player's Border Layout.
 */
	private JACanvasEmbedder EMBED_PLAYER =
	new JACanvasEmbedder() {
		public void embedInContainer(Component jacanvas) {
			JASiGMLURLPlayer.this.add(jacanvas, BorderLayout.CENTER);
		}
	};

/** Handler for Player events -- delegates to
 * {@link #handleLoadDone(boolean,int,int)},
 * {@link #handleNewFrame(AnimationScan,boolean)} and
 * {@link #handlePlayDone(AnimationScan)}
 */
	private JALoadingPlayerEventHandler PLAYER_EVENT_HANDLER =
	new JALoadingPlayerEventHandler() {
		public void loaderHasStarted() {
			JASiGMLURLPlayer.this.handleLoadStarted();
		}
		public void nextSignLoaded(int s, int flimit) {
			JASiGMLURLPlayer.this.handleSignLoaded(s, flimit);
		}
		public void loaderIsDone(boolean gotframes, int nsigns, int nframes) {
			JASiGMLURLPlayer.this.handleLoadDone(gotframes, nsigns, nframes);
		}
		public void playerHasStarted() {
			// No action
		}
		public void playerIsAtNewFrame(AnimationScan scan,  boolean dropped) {
			JASiGMLURLPlayer.this.handleNewFrame(scan, dropped);
		}
		public void playerIsDone(AnimationScan scan) {
			JASiGMLURLPlayer.this.handlePlayDone(scan);
		}
	};

/** Action listener for the PLAY button -- delegates to {@link #doPlay()}.*/
	private ActionListener PLAY_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			JASiGMLURLPlayer.this.doPlay();
		}
	};

/** Action listener for the STOP button -- delegates to {@link #doStop()}.*/
	private ActionListener STOP_ACTION =
	new ActionListener() {
		public void actionPerformed(ActionEvent aevt) {
			JASiGMLURLPlayer.this.doStop();
		}
	};

/** Creates the applet's GUI, with the Play/URL panel, the avatar
 * Player view, and the Status panel stacked vertically using a
 * Border Layout.
 */
	protected void createGUI() {

		// Get applet width and height.
		int w = this.getWidth();
		int h = this.getHeight();

		// Define AWT/non-AWT tag for message strings.
		String modetag = (this.useAWT ? " (AWT)" : "");
		System.out.println("####  usingAWT="+this.useAWT);

		if (! this.useAWT) {
			// Use the platform look-and-feel.
			String syslaf = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(syslaf);
			}
			catch (Exception xx) {
				String prefix = "JA-URL-Player-Applet"+modetag+" set-up: ";
				System.out.println(prefix+xx);
			}
		}

		// Create the Play, URL and Status panels; disable the Stop button.
		Component panepu = this.getNewPUPanel(w-8);
		Component panestatus = this.getNewStatusPanel();
		this.setStopEnabled(false);

		// Set up this applet's panel: add the GUI panels, create
		// the avatar player canvas and embed it in the center.
		this.setLayout(new BorderLayout());
		this.add(panepu, BorderLayout.NORTH);
		this.add(panestatus, BorderLayout.SOUTH);
		this.player =
			new JALoadingPlayer(
				this.JA_OPTIONS, EMBED_PLAYER,
				null, this.PLAYER_EVENT_HANDLER, null);
		this.player.createStandardCameraChangeMonitor();
		this.validate();
	}

	private static final String		SIGML_URL_UK =
//		"http://www.visicast.cmp.uea.ac.uk/sigml/test/sigml/"+
//		"DEFKPR-ngt.sigml";
		"http://www.visicast.cmp.uea.ac.uk/eSIGN/sigml/"+
		"scotland-H.sigml";
	private static final String		SIGML_URL_DE =
		"http://www.gebaerden.hamburg.de/sigml/"+
		"WillkHH_final_1.xml";

	private static final String		SIGML_URL = SIGML_URL_UK;

/** Performs the PLAY action, attempting to obtain a valid SiGML URL
 * from the URL text area, resolving that text against the default
 * SiGML abse, and starting a thread to load and play the SiGML document
 * thus identified, enabling/disabling the PLAY button and setting the
 * Status message as appropriate.
 */
	protected void doPlay() {

		this.setPlayEnabled(false);
		String errmsg = null;
		URL sigmlurl = null;

		String rawurl = this.getURLText();
		if (rawurl.length() == 0) {
			errmsg = "Blank SiGML URL string.";
		}
		else {
			// (Alternatively, we could resolve against the current user base.)
			final String S_BASE = this.JA_OPTIONS.defaultSiGMLBaseURL();
			sigmlurl = JAIO.resolveURL(S_BASE, rawurl);
			if (sigmlurl == null) {
				errmsg = "Bad SiGML URL: "+rawurl;
			}
		}

		if (sigmlurl == null) {
			this.setStatus(errmsg);
			this.setPlayEnabled(true);
		}
		else {
			// Create and start a thread to run the player on the SiGML.
			final URL  SIGMLURL = sigmlurl;
			final Thread  T_RUN_PLAYER  =
			new Thread() {
				public void run() {
					try {
						JASiGMLURLPlayer.this.playSiGML(SIGMLURL);
					}
					catch (InterruptedException ix) {
						System.out.println(
							"####  JASiGMLURLPlayer: playSiGML() interrupted: "+ix);
					}
				}
			};
			T_RUN_PLAYER.start();
		}
	}

/** Plays the given SiGML URL on this applet's player. */
	protected void playSiGML(URL sigmlurl) throws InterruptedException {

		this.player.playSiGMLURL(sigmlurl);
	}

/** Performs the STOP action, disabling the STOP button and stopping
 * the player.
 */
	protected void doStop() {

		this.setStopEnabled(false);
		this.player.stopPlaying();
	}

/** Handler for the player's "Load Started" event. */
	protected void handleLoadStarted() {
	}

/** Handler for the player's "Sign Loaded" event. */
	protected void handleSignLoaded(int s, int flimit) {
	}

/** Handler for the player's "Load Done" event, with the given
 * success/failure status flag -- responds by enabling/disabling the
 * Play panel buttons accordingly and by setting the status message
 * accordingly.
 */
	protected void handleLoadDone(boolean loadok, int nsigns, int nframes) {

		this.setPlayEnabled(!loadok);
		this.setStopEnabled(loadok);

		if (! this.JA_OPTIONS.doStreamedAnimationBuild()) {

			String statusmsg = null;
			if (loadok) {
				String smsg = (0 < nsigns ? nsigns+" signs: " : "");
				statusmsg = smsg+nframes+" frames generated.";
				// ... at present this status message is very short-lived.
			}
			else {
				statusmsg = "SiGML processing failure.";
			}

			this.setStatus(statusmsg);
			System.out.println(statusmsg);
		}
	}

/** Handler for the player's "Play Done" event, with the given
 * animation scan -- responds by enabling/disabling the Play panel
 * buttons accordingly.
 */
	protected void handlePlayDone(AnimationScan scan) {

		this.setPlayEnabled(true);
		this.setStopEnabled(false);
	}

/** Handler for the player's "New Frame" event, with the given
 * animation scan and frame-dropped flag -- responds by updating
 * the status message if (and only if) the scan is at a new sign.
 */
	protected void handleNewFrame(AnimationScan scan, boolean dropped) {

		if (scan.scanIsAtNewSign()) {
			int s = scan.s();
			int ss = scan.sCount();
			String gloss = scan.sign().getGloss();
			String msg = "[Limit="+ss+"]  Sign "+s+":  \""+gloss+"\"";
			this.setStatus(msg);
		}
	}

/** Creates and returns a new Status message panel, using the
 * appropriate GUI toolkit.
 */
	private Component getNewStatusPanel() {

		return
			this.useAWT ?
				this.makeStatusPanelAWT() : this.makeStatusPanel();
	}

/** Creates and returns a new AWT panel for the status message area. */
	private Component makeStatusPanelAWT() {
		
		this.lblStatus = new Label("Starting player");
		Panel panestatus = new Panel(new FlowLayout(FlowLayout.LEFT));
		panestatus.add(this.lblStatus);

		return panestatus;
	}

/** Creates and returns a new Swing panel for the status message area. */
	private Component makeStatusPanel() {
		
		this.lblJStatus = new JLabel("Starting player");
		JPanel panestatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panestatus.add(this.lblJStatus);

		return panestatus;
	}

/** Creates and returns a new panel, of the given width and using
 * the appropriate toolkit, containing a new Play panel and a new
 * URL panel as vertically stacked sub-panels.
 */
	private Component getNewPUPanel(int width) {

		Component cptpu = null;
		Component paneplay = this.getNewPlayPanel();
		Component paneurl = this.getNewURLPanel(width);
		if (this.useAWT) {
			Panel panepu = new Panel();
			panepu.add(paneplay, BorderLayout.NORTH);
			panepu.add(paneurl, BorderLayout.SOUTH);
			panepu.setPreferredSize(new Dimension(width, 80));
			cptpu = panepu;
		}
		else {
			JPanel panepu = new JPanel();
			panepu.add(paneplay, BorderLayout.NORTH);
			panepu.add(paneurl, BorderLayout.SOUTH);
			panepu.setPreferredSize(new Dimension(width, 80));
			cptpu = panepu;
		}
		return cptpu;
	}

/** Creates and returns a new Play panel, using the appropriate GUI
 * toolkit.
 */
	private Component getNewPlayPanel() {

		return
			this.useAWT ?
				this.makePlayPanelAWT() : this.makePlayPanel();
	}

/** Creates and returns a new AWT Play panel, containing
 * PLAY and STOP buttons with the appropriate action listeners attached.
 */
	private Component makePlayPanelAWT() {
		
		this.bttnPlay = new Button("Play");
		this.bttnStop = new Button("Stop");
		Panel paneplay = new Panel(new FlowLayout());
		paneplay.add(this.bttnPlay);
		paneplay.add(this.bttnStop);
		this.bttnPlay.addActionListener(this.PLAY_ACTION);
		this.bttnStop.addActionListener(this.STOP_ACTION);

		return paneplay;
	}

/** Creates and returns a new Swing Play panel, containing
 * PLAY and STOP buttons with the appropriate action listeners attached.
 */
	private Component makePlayPanel() {
		
		this.bttnJPlay = new JButton("Play");
		this.bttnJStop = new JButton("Stop");
		JPanel paneplay = new JPanel(new FlowLayout());
		paneplay.add(this.bttnJPlay);
		paneplay.add(this.bttnJStop);
		this.bttnJPlay.addActionListener(this.PLAY_ACTION);
		this.bttnJStop.addActionListener(this.STOP_ACTION);

		return paneplay;
	}

/** Creates and returns a new URL panel of the given width, using
 * the appropriate GUI toolkit.
 */	
	private Component getNewURLPanel(int w) {

		return
			this.useAWT ?
				this.makeURLPanelAWT(w) : this.makeURLPanel(w);
	}

/** Creates and returns an AWT URL panel with the given width.
 * The panel contains the URL text field, to which the PLAY action
 * listener is attached, so that the ENTER keystroke has the same
 * effect as the PLAY button.
 */
	private Component makeURLPanelAWT(int w) {

		this.txtURL = new TextField(SIGML_URL);
		this.txtURL.addActionListener(this.PLAY_ACTION);
		this.txtURL.setPreferredSize(new Dimension(w, 24));

		return this.txtURL;
	}

/** Creates and returns a swing URL panel with the given width.
 * The panel contains the URL text field, to which the PLAY action
 * listener is attached, so that the ENTER keystroke has the same
 * effect as the PLAY button.
 */
	private Component makeURLPanel(int w) {
		
		this.txtJURL = new JTextField(SIGML_URL);
		this.txtJURL.addActionListener(this.PLAY_ACTION);
		this.txtJURL.setPreferredSize(new Dimension(w, 24));

		return this.txtJURL;
	}

/** Returns the text from the URL text field. */
	private String getURLText() {

		return
			this.useAWT ? this.txtURL.getText() : this.txtJURL.getText();
	}

/** Enables/disables the PLAY button as specified. */
	private void setPlayEnabled(boolean enabled) {

		if (this.useAWT)
			this.bttnPlay.setEnabled(enabled);
		else
			this.bttnJPlay.setEnabled(enabled);
	}

/** Enables/disables the STOP button as specified. */
	private void setStopEnabled(boolean enabled) {

		if (this.useAWT)
			this.bttnStop.setEnabled(enabled);
		else
			this.bttnJStop.setEnabled(enabled);
	}

/** Puts the given message in the status label. */
	private void setStatus(String msg) {

		// Set the status label width to 400, and then update its text.
		int h = 0;
		if (this.useAWT) {
			h = this.lblStatus.getHeight();
			this.lblStatus.setSize(400,  h);
			this.lblStatus.setText(msg);
		}
		else {
			h = this.lblJStatus.getHeight();
			this.lblJStatus.setSize(400,  h);
			this.lblJStatus.setText(msg);
		}
	}
}
