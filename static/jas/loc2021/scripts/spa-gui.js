//########  Javascript for the SiGML Player Applet (SPA) with a GUI  ########
//
// RE  2009-03

/*
 * The SPA GUI object, conventionally called SPAGUI, defines the behaviour
 * of the HTML/JS GUI for a SiGML Player Applet instance, or at least those
 * those aspects of the GUI that are not defined in the HTML page hosting
 * the applet.
 *
 * The SPA GUI is the top-level JS component for the given SPA instance.
 * As such it contains references both to the associated SPA proxy object
 * (spa-proxy.js) and to the associated SPA host object (spa-host-for-gui.js),
 * which acts as an intermediary between the proxy, especially its event
 * callouts, and the controlling GUI itself.
 *
 * The SPA GUI object is hosted in a frame called spaguiframe
 * (spa-with-gui-frame.html) which is the parent of the spaframe
 * (spa-frame.html) and the spaguiframe (spa-gui-frame.html)
 *
 * This file defines the following global functions and variables:
 *
 * makePlayEnableChangeHandler( targetframe )
 * getSPAGUIProto()
 * SPA_GUI_PROTO
 * ctor_SPAGUI()
 * getSPAHost()
 * setSPA( spa )
 * notifyPlayEnableChangeTarget( targetframe )
 *
 * Internal structure of the SPA-with-GUI frame:
 *
 *  FRAME spawithguiframe  src = spa-with-gui-frame.html
 *
 *      spa-params.js
 *          ctor_Params( data )
 *      spa-host-for-gui.js
 *          ctor_SPAGUIHOST( guihandlerobj, params )
 *      spa-gui.js  [ THE PRESENT FILE ]
 *          ctor_SPAGUI()
 *          SPAGUI
 *          SPAGUI.HOST
 *          getSPAHost()
 *          setSPA( spa )
 *          notifyPlayEnableChangeTarget( targetframe )
 *      HTML_PARAMETERS
 *      SPAGUI.setParams( HTML_PARAMETERS )
 *
 *      FRAME spaframe  src = spa-frame.html
 *
 *          spa-proxy.js
 *              SPA = new ctor_SPA( parent.getSPAHost() )
 *              parent.setSPA( SPA )
 *              spaAvatarEvent( ekind, avatar)        --> SPA.avatarEH(...)
 *              spaFramesGenEvent( ekind, nf, ns)     --> SPA.framesGenEH(...)
 *              spaAnimationEvent( ekind, f, s ,glss) --> SPA.animEH(...)
 *              spaSetSiGMLPlayerApplet( spa )        --> null
 *
 *          <body> onload=SPA.initialise( "spApplet" ) onunload=SPA.terminate()
 *              <applet> id="spApplet"
 *
 *      FRAME spaguiframe  src = spa-gui-frame.html
 *
 *          SPAGUI = parent.SPAGUI
 *          <form> id="ctrlForm
 *          - components invoke methods on SPAGUI, SPAGUI.SPA;
 */

// Creates and returns a Play Enable Change handler for the given target frame.
//-------------------------------------------------
function makePlayEnableChangeHandler( targetframe ) {
//-------------------------------------------------
	return {
		TARGET_FRAME : targetframe,
		fireChange : function( enable ) {
			var pecfun = this.TARGET_FRAME.playEnableChange;
			if (pecfun !== undefined && pecfun !== null) {
				pecfun( enable );
			}
		}
	};
}

// Creates and returns a prototype defining methods for an SPA GUI object.
//-----------------------
function getSPAGUIProto() {
//-----------------------
	return {
		//########  Message display  ########
		statusMessage : function( msg ) { window.status = msg; },
		//-----------
		showMessage : function( msg ) { this.statusMessage( msg ); },
		//---------
		showDetailMessage : function( msg ) { this.statusMessage( msg ); },
		//---------------

		//########  Button status management  ########
		setSpeedButtons : function( resetenable, changeenable ) {
		//-------------
			this.GUI_FORM.bttnResetSpeed.disabled = ! resetenable;
			this.GUI_FORM.bttnSlower.disabled     = ! changeenable;
			this.GUI_FORM.bttnFaster.disabled     = ! changeenable;
		},
		setPlayButtons : function( avenable, signenable, stopenable ) {
		//------------
			this.GUI_FORM.avSelect.disabled	= ! avenable;
			this.GUI_FORM.bttnSign.disabled = ! signenable;
			this.GUI_FORM.bttnStop.disabled = ! stopenable;
		},
		setDoLogDFEnabled : function( enable ) {
		//---------------
			this.GUI_FORM.chkDoLogDroppedFrames.disabled = ! enable;
		},
		disableAllButtons : function() {
		//---------------
			this.setPlayButtons( false, false, false );
			this.setSpeedButtons( false, false );
			this.setDoLogDFEnabled( false );
		},
		setButtonsForAnimating : function() {
		//--------------------
			this.setPlayButtons( false, false, true );
			this.setSpeedButtons( true, true );
			this.setDoLogDFEnabled( false );
		},
		setButtonsForAvatarIdle : function() {
		//---------------------
			this.setPlayButtons( true, this.sigmlIsAvailable(), false );
			this.setSpeedButtons( true, false );
			this.setDoLogDFEnabled( true );
		},
		setButtonsForNoAvatar : function() {
		//-------------------
			this.setPlayButtons( true, false, false );
			this.setSpeedButtons( false, false );
			this.setDoLogDFEnabled( true );
		},
		setButtonsForSPABusy : function() {
		//------------------
			this.disableAllButtons();
		},

		//########  Applet status tests  ########
		avatarIsAvailable : function() { return Boolean( this.avatar ); },
		//---------------
		sigmlURLIsAvailable : function() { return (this.sigmlURL.length != 0); },
		//-----------------
		sigmlTextIsAvailable : function() { return (this.sigmlText.length != 0); },
		//------------------
		sigmlIsAvailable : function() {
		//--------------
			return (this.sigmlURL.length != 0 || this.sigmlText.length != 0);
		},
		appletIsIdle : function() {
		//----------
			return (
				! this.avatarLoadBusy &&
				! this.animationBusy &&
				! this.terminationBusy );
		},
		readyToPlaySign : function() {
		//-------------
			return (this.appletIsIdle() && this.avatarIsAvailable());
		},
		okToStartAnimation : function() {
		//----------------
			return (this.readyToPlaySign && this.sigmlIsAvailable());
		},

		//########  Driving an external Play-Enable-Change handler  ########
		setPlayEnableChangeHandler : function( pech ) {
		//------------------------
			this.PLAY_ENABLE_CHANGE_HANDLER = pech;
			if (this.readyToPlaySign()) { this.firePlayEnableChange( true ); }
		},
		firePlayEnableChange : function( enable ) {
		//------------------
			var handler = this.PLAY_ENABLE_CHANGE_HANDLER;
			if (handler) { 	handler.fireChange( enable ); }
		},

		//########  Environment Parameter access  ########
		// NB  this is called _earlier_ than initialise(), below.
		updateParams : function( htmlparams ) {
		//----------
			this.SPAHOST.updateParams( htmlparams );
		},

		//########  Avatar switching  ########
		setAvatar : function( av ) {
		//-------
			this.avatarLoadBusy = true;
			this.setButtonsForSPABusy();
			this.firePlayEnableChange( false );
			var aerr = this.SPA.setAvatar( av );
			if (aerr) {
				this.avatarLoadBusy = false;
				this.setButtonsForNoAvatar();
				this.firePlayEnableChange( true );
			}
		},
		// SPA event handler.
		avatarUnloaded : function() {
		//------------
			// Not much to be done now: other than at shutdown time this will
			// be followed by an updateAvatar() call.
			this.avatar = null;
		},
		// SPA event handler.
		avatarLoadDone : function( av ) {
		//------------
			// (av is null in case of load failure.)
			this.avatar = av;
			this.avatarLoadBusy = false;
			if (av) {
				// Only set the selector value if necessary (to avoid an infinite
				// regress of setting and responding).  Typically this happens
				// (if at all) only after the initial avatar load.
				var avpopup = this.GUI_FORM.avSelect;
				if (avpopup.value !== av) {
					avpopup.value = av;
				}
				this.setButtonsForAvatarIdle();
				this.firePlayEnableChange( true );
			}
			else {
				this.setButtonsForNoAvatar();
			}
		},

		//########  FPS setting  ########
		setAnimgenFPS : function( fpsstr ) {
		//-----------
			// No further action is needed if we already have the new FPS
			// value.  That could happen on our first invocation, it it
			// comes via initialise() -- see below.
			if (this.fpsStr != fpsstr) {
				var fpserr = this.SPA.setAnimgenFPS( fpsstr );
				if (fpserr) {
					this.GUI_FORM.fpsSelect.value = this.fpsStr;
				}
				else {
					this.fpsStr = fpsstr;
				}
			}
		},

		//########  Frames Generation  and Animation event handlers  ########
		// SPA event handler.
		setFramesGenDone : function( fgok ) {
		//--------------
			this.framesGenBusy = false;
			if (!fgok) {
				this.setAnimationDone();
				this.firePlayEnableChange( true );
			}
		},
		// SPA event handler.
		setAnimationDone : function() {
		//--------------
			this.animationBusy = false;
			this.setButtonsForAvatarIdle();
			this.firePlayEnableChange( true );
		},

		//########  HTML start/finish handlers  ########
		// NB  initialise() is called _later_ than setParams(), above.
		initialise : function() {
		//--------
			this.GUI_FORM = spaguiframe.document.forms.ctrlForm;
			// It may be a good idea to integrate FPS menu management
			// into the general enable/disable framework, but it can be on
			// throughout the applet's lifetime, so we manage its initial
			// state here after which we leave it alone.
			var fpsstr = this.SPAHOST.PARAMS.get( "initialFPSStr" );
			this.fpsStr = fpsstr;
			this.GUI_FORM.fpsSelect.value = fpsstr;
			this.GUI_FORM.fpsSelect.disabled = false;
		},
		terminate : function() {
		//-------
			this.terminationBusy = true;
			this.setButtonsForSPABusy();
		},

		//########  HTML initialisation support  ########
		getAvatarOptionElements : function() {
		//-----------------------
			function optstr( av ) {
				return "<option value=\""+av+"\">"+av+"</option>";
			}
			var std = [ "anna", "francoise", "marc" ];
			var eat = this.SPAHOST.param( "extraAvatarsText" );
			var avs = (eat.length === 0 ? std : eat.split( ":" ).concat( std ));
			var avoptels = [];
			for (var i=0; i!=avs.length; ++i) {
				avoptels.push( optstr( avs[ i ]  ) );
			}
			return avoptels;
		},
		getFPSOptionElements : function() {
		//------------------
			function optstr( fps ) {
				return "<option value=\""+fps+"\">"+fps+"</option>";
			}
			var fpsvals = [ 10, 25, 40, 50, 60, 100 ];
			var fpsoptels = [];
			for (var i=0; i!=fpsvals.length; ++i) {
				fpsoptels.push( optstr( fpsvals[ i ]  ) );
			}
			return fpsoptels;
		},
		//########  HTML SiGML playing button handlers  ########
		// (private)
		doPlay : function( playfunction ) {
		//----
			this.framesGenBusy = true;
			this.animationBusy = true;
			this.setButtonsForSPABusy();
			this.SPA.resetSignData();
			var perr = playfunction();
			if (perr) {
				this.framesGenBusy = false;
				this.animationBusy = false;
				this.setButtonsForAvatarIdle();
			}
			else {
				this.firePlayEnableChange( false );
			}
		},
		// (private)
		doPlayURL : function() {
		//-------
			// Assume this.readyToPlaySign(), and this.sigmlURL is not empty.
			var spa = this.SPA;
			var sigmlurl = this.sigmlURL;
			this.doPlay( function(){ spa.playSiGMLURL( sigmlurl ); } );
		},
		// (private)
		doPlayText : function() {
		//--------
			// Assume this.readyToPlaySign(), and this.sigml is not empty.
			var spa = this.SPA;
			var sigml = this.sigmlText;
			this.doPlay( function(){ spa.playSiGMLText( sigml ); } );
		},
		// "Sign" handler, for the case where the SiGML URL/text has been
		// established previously.
		playSiGML : function() {
		//-------
			// When this method is called, it is expected that the initial
			// text and one of the following pair will succeed.
			if (this.readyToPlaySign()) {
				if (this.sigmlURLIsAvailable()) { this.doPlayURL(); }
				else if (this.sigmlTextIsAvailable()) { this.doPlayText(); }
			}
		},
		// "Sign SiGML URL" handler.
		playSiGMLURL : function( url ) {
		//----------
			if (this.readyToPlaySign()) {
				this.sigmlText = "";
				this.sigmlURL = url || "";
				if (this.sigmlURL !== "") { this.doPlayURL(); }
			}
			else {
				console.log( "SPA GUI: cannot play URL when applet is busy." );
			}
		},
		// "Sign SiGML text" handler.
		playSiGMLText : function( sigml ) {
		//-----------
			if (this.readyToPlaySign()) {
				this.sigmlURL = "";
				this.sigmlText = sigml || "";
				if (this.sigmlText !== "") { this.doPlayText(); }
			}
			else {
				console.log( "SPA GUI: cannot play text when applet is busy." );
			}
		},
		// "Stop playing SiGML" handler.
		stopPlaySiGML : function() {
		//-----------
			var serr = this.SPA.stopPlayer();
			if (serr) {
				// We should never get here, and if we do it's anybody's guess
				// what state we're in, but doing the following (i.e. behaving
				// as though a successful stop-player operation has been
				// completed) just might get us out of a hole.
				this.framesGenBusy = false;
				this.animationBusy = false;
				this.setButtonsForAvatarIdle();
				this.firePlayEnableChange( true );
			}
		}
	};
}

// The only instance of getSPAGUIProto().
var SPA_GUI_PROTO = getSPAGUIProto();

// Constructs a new SPAGUI and its associated subordinate host object.
//--------------------
function ctor_SPAGUI() {
//--------------------

	var sgdata = {
		SPA             : null,  // JS proxy for SiGML Player Applet;
		SPAHOST         : null,  // Callback interface for SPA proxy;
		GUI_FORM        : null,  // Set by initialise();
 		PLAY_ENABLE_CHANGE_HANDLER
		                : null,
		avatar          : null,
		avatarLoadBusy  : true,
		fpsStr          : "",
		framesGenBusy   : false,
		animationBusy   : false,
		terminationBusy : false,
		sigmlURL        : "",
		sigmlText       : ""
	};

	var SPAGUI = makeObject( sgdata, SPA_GUI_PROTO );
	// Host interface object has a reference back to SPAGUI.
	SPAGUI.SPAHOST = new ctor_SPAGUIHOST( SPAGUI, new ctor_Params() );

	return SPAGUI;
}

// The global SPA GUI instance, the route to all SPA data.
var SPAGUI = new ctor_SPAGUI();
// Some time after the above line has created the SPAGUI object, the SPA
// subframe will create the SPA proxy object.  The following pair of functions
// allow the SPA proxy's initialisation sequence to obtain its HOST from us,
// and to provide us with a reference back to the SPA proxy itself, which is
// needed by SPAGUI.
function getSPAHost()  { return SPAGUI.SPAHOST; }
function setSPA( spa ) { SPAGUI.SPA = spa; }

// Attaches a Play Enable Change handler for the given target frame to the
// SPAGUI.
//--------------------------------------------------
function notifyPlayEnableChangeTarget( targetframe ) {
//--------------------------------------------------
	var handler = makePlayEnableChangeHandler( targetframe );
	SPAGUI.setPlayEnableChangeHandler( handler );
}


//############  (end)  ############
