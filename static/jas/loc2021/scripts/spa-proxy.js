//########  Javascript for the SiGML Player Applet (SPA) Proxy  ########
//
// RE  2009-03

/*
 * The SPA proxy object, conventionally called SPA, acts as a proxy
 * for the SPA itself: all invocations of SPA services are made through
 * the proxy, and the proxy provides handlers for all SPA events --
 * although current SPA callout conventions require that these handlers
 * are reached via global handler methods.
 *
 * The SP applet itself and the SPA proxy reside in their own HTML frame,
 * spaframe (spa-frame.html).  This frame is hosted in a parent frame
 * which provides objects to control the SPA.  These controlling objects
 * may or may not include GUI components.
 *
 * The SPA proxy's access to its controlling host HTML/JS is provided via an
 * an SPA HOST object, an implementation of a standard host interface, which
 * is obtained from the parent frame when the SPA proxy is created.
 *
 * The proxy itself takes responsibility for generating status messages
 * (via methods provided in the host) in response to SPA events.  Thus the
 * host's event response methods are responsible only for updatding
 * the GUI state as required.
 *
 * This file defines the following global functions and variables:
 *
 * makeObject( data, proto )
 * getSPAMethods()
 * SPA_PROTO
 * ctor_SPA( host )
 * SPA
 * spaAvatarEvent( ekind, avatar )
 * spaFramesGenEvent( ekind, nf, ns )
 * spaAnimationEvent( ekind, f, s, glss )
 * spaSetSiGMLPlayerApplet( spa ) 
 */

//########  Global support functions for dynamic applet parameter writing  ########
function spaWriteAppletParam( nm, val ) {
	window.document.write( '<param name="'+nm+'" value="'+val+'" />' );
}

function spaWriteAppletParams( params ) {
	for (var p in params) {
		if (params.hasOwnProperty( p )) {
			spaWriteAppletParam( p, params[ p ] );
		}
	}
}

// Makes a new object whose data is a copy of the given data object, and
// whose prototype is the given PROTO.
//--------------------------------
function makeObject( data, PROTO ) {
//--------------------------------
	function ctor() {
		// Copy the given data to this:
		for (var k in data) {
			// hasOwnProperty() check should be redundant;
			if (data.hasOwnProperty( k )) {
				this[ k ] = data[ k ];
			}
		}
	}
	// Use the given prototype for instances of our ctor.
	ctor.prototype = PROTO;
	return new ctor();
}

// Creates and returns a prototype defining methods for an SPA proxy object.
//-------------------------
function getSPAProxyProto() {
//-------------------------
	return {
		onWin : function() { return (this.osTag=="win"); },
		//---
		onMac : function() { return (this.osTag=="mac"); },
		//---
		isInSafari : function() { return (this.appTag == "safari"); },
		//--------
		isInFirefox : function() { return (this.appTag == "firefox"); },
		//---------
		isInChrome : function() { return (this.appTag == "chrome"); },
		//--------
		isInIE : function() { return (this.appTag == "ie"); },
		//----
		// A crude way of dealing with the fact that IE has no console:
		log : function( msg ) {
		//-
			if ((typeof console) !== "undefined") { console.log( msg ); }
		},
		envInit : function() {
		//-----
			var avlc = window.navigator.appVersion.toLowerCase();
			var ualc = window.navigator.userAgent.toLowerCase();
			
			if (avlc.indexOf( "win" ) != -1) { this.osTag = "win"; }
			else if (avlc.indexOf( "mac" ) != -1) { this.osTag = "mac"; }
			
			// Must look for chrome before safari.
			if (ualc.indexOf( "chrome" ) != -1) {this.appTag = "chrome"; }
			else if (ualc.indexOf( "safari" ) != -1) { this.appTag = "safari"; }
			else if (ualc.indexOf( "firefox" ) != -1) { this.appTag = "firefox"; }
			else if (ualc.indexOf( "msie" ) != -1) { this.appTag = "ie"; }
		},
		appletLoadIsStable : function() {
		//----------------
			// This is a surprisingly, not to say scarily, tricky test to do
			// safely:
			// First, we need to know that the primary applet is properly loaded
			// -- do this by checking that its init() method is well-defined.
			// Second, we need to know whether or not it has a getSubApplet()
			// method.
			// Third, if it does have getSubApplet() we need to know that that
			// method is "initialised" -- i.e. it is ready to deliver a well-
			// defined result.
			// Note also that when the primary applet is the JAL and the
			// browser is Chrome on Mac OS X, the following code breaks --
			// and hence this method must not be called in that case.
			var ok = false;
			var applet = window.document.spApplet;
			// this.log( "appletLoadIsStable: typeof applet="+(typeof applet)+" typeof applet.init="+(typeof applet.init));
			if ((typeof applet) !== "undefined") {
				// 2016-05-03 Handle Windows Firefox
				if (this.onWin() && this.isInFirefox()) {
					ok = true;
				} else if ((typeof applet.init) !== "undefined") {
					ok =
						((typeof applet.getSubApplet) === "undefined") ||
						Boolean( applet.getSubApplet() );
				}
			}
			return ok;
		},
		getSPAChoice : function() {
		//----------
			// ASSUME: this.appletLoadIsStable( this.appletObj );
			var applet = window.document.spApplet;
			var choice = { spamain : null, spasub : null };
			if ((typeof applet.getSubApplet) === "undefined") {
				choice.spamain = applet;
			}
			else {
				choice.spasub = applet.getSubApplet();
			}
			// Return an object in which one of spamain and spasub is the
			// spa object and the other is null.
			return choice;
		},
		//----
		setSPA : function( spachoice, source ) {
		//----
			var nosub = (spachoice.spasub === null);
			var nstag = (nosub ? "no" : "using"); 
			this.spApplet = (nosub ? spachoice.spamain : spachoice.spasub);
			this.log( "setSPA() source: "+source+" -- "+nstag+" subapplet" );
		},
		initialise : function( appletobj ) {
		//--------
//			this.initSPA( appletobj );
//			this.resetSPAData();
			// envInit() should have been called already, but play safe.
			if (!this.osTag) { this.envInit(); }
			this.log( "OS: "+this.osTag+"   Browser: "+this.appTag );
			this.resetSPAData();
			// this.appletObj = appletobj;
			// Chrome on Mac OS X cannot withstand much interrrogation -- that
			// is, it either locks up or throws an exception -- until its
			// applet load sequence is complete.
			if (!this.isInChrome() || !this.onMac()) { this.initSPA(); }
		},
		// Sets this SPA proxy's applet; should be called as part of the
		// initialisation sequence, i.e. before any applet service is invoked
		// -- but note that the desired outcome may be achieved asynchronously
		// after this method has returned.
		initSPA : function() {
		//-----
/*			var gsatype = typeof (appletobj.getSubApplet);
			//var atype = typeof (appletobj);
			//console.log( "applet type: "+atype+" 'getSubApplet' type: "+gsatype );
			if (gsatype === 'function') {
				// We're running the JNLP Applet Launcher, with the SPA as
				// its sub-applet.
				this.asynchGetSubApplet( appletobj );
			}
			else if (gsatype === 'undefined') {
				// We're running the SiGML Player Applet directly via JNLP.
				this.spApplet = appletobj;
			}
			else {
				window.alert( "Unexpected type for getSubApplet: "+gsatype );
			}
 */
			if (this.appletLoadIsStable()) {
				this.setSPA( this.getSPAChoice(), "initSPA()" );
			}
			else {
				this.asynchGetSPA();
			}
		},
		// Asynchronously obtains the SPA applet, whenever it becomes
		// accessible, and assigns it to this proxy's spApplet field.
		asynchGetSPA : function() {
		//----------
			var thisobj = this;
			var spachoice;
			function tryFindSPA() {
				if (thisobj.appletLoadIsStable()) {
					spachoice = thisobj.getSPAChoice();
					thisobj.setSPA( spachoice, "asynchGetSPA()" );
				}
			}
			function done() { return Boolean( spachoice ); }
			this.asynchRepeat( tryFindSPA, done/**/, "asynchGetSPA(): "/**/ );
		},
/*		asynchGetSubApplet : function( appletobj ) {
		//----------------
			var spa;
			var thisspa = this;
			function trySetSPA() {
				// We assume that the top-level applet is an instance
				// of the JNLP Applet Launcher, and that the SPA
				// itself is the sub-applet of this JAL instance.
				spa = appletobj.getSubApplet();
				if (spa) { thisspa.spApplet = spa; }
			}
			function done() { return Boolean( spa ); }
*/
//			this.asynchRepeat( trySetSPA, done/*, "asynchGetSubApplet(): "*/ );
//		},
		// Asynchronously makes repeated calls to the body function until
		// the ok function succeeds, i.e. returns a true result.
		// On completion, if tag is defined then log the tag and repetition count.
		asynchRepeat : function( body, ok, tag ) {
		//----------
			var thisobj = this, n = 0;
			function closedARFun() {
				body();
				if (! ok()) { ++ n; window.setTimeout( closedARFun, 1 ); }
				else if (tag) { thisobj.log( tag+" repetition count="+n ); }
			}
			closedARFun();
		},
		// Resets all this proxy's sign-related data.
		resetSignData : function() {
		//-----------
			this.sLimit = 0;
			this.fLimit = 0;
			this.sign = -1;
			this.gloss = "";
			this.signMsg = "";
		},
		// Resets all this proxy's applet-related data.
		resetSPAData : function() {
		//----------
			this.resetSignData();
			this.curSpeed = 1.0;
		},
		getSPA : function() {
		//----
			if (this.spApplet === null) {
				// We should never get here except when running Chrome on
				// Mac OS X -- but in that case we definitely need to get here.
				// Irrelevant now Chrome has no Java support.
				if (this.appletLoadIsStable()) {
					this.setSPA( this.getSPAChoice(), "getSPA()" );
				}
				else {
					this.log( "getSPA(): catastrophe -- cannot get SPA!." );
				}
			}
			return this.spApplet;
		},
		// Switches the applet to the given avatar; returns null/undefined
		// if successful, or an error string object (not a String!) in case
		// of failure.
		setAvatar : function( avatar ) {
		//-------
			var err = this.getSPA().setAvatar( avatar );
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Sets the applet's FPS value from the given string; returns null/undefined
		// if successful, or an error string object (not a String!) in case
		// of failure.
		setAnimgenFPS : function( fpsstr ) {
		//-----------
			var err = this.getSPA().setAnimgenFPS( fpsstr );
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Gets the applet to play the given SiGML URL; returns null/undefined
		// if successful, or an error string object (not a String!) in case
		// of failure.
		playSiGMLURL : function( sigmlurl ) {
		//----------
			var err = this.getSPA().playSiGMLURL( sigmlurl );
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Gets the applet to play the given SiGML text; returns null/undefined
		// if successful, or an error string object (not a String!) in case
		// of failure.
		playSiGMLText : function( sigml ) {
		//-----------
			var err = this.getSPA().playSiGMLText( sigml );
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Gets the applet to set up a pipe for the subsequent input of
		// SiGML fragments; returns null/undefined if successful, or an error
		// string object (not a String!) in case of failure.
		startPlaySiGMLPiped : function() {
		//-----------------
			var err = this.getSPA().startPlaySiGMLPiped();
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Gets the applet to feed the given SiGML fragment into its current
		// SiGML input pipe; returns null/undefined if successful, or an error
		// string object (not a String!) in case of failure.
		appendToSiGMLPipe : function( fragment ) {
		//---------------
			var err = this.getSPA().appendToSiGMLPipe( fragment );
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Gets the applet to close the current SiGML pipe; returns
		// null/undefined if successful, or an error string object (not a
		// String!) in case of failure.
		closeSiGMLPipe : function() {
		//------------
			var err = this.getSPA().closeSiGMLPipe();
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Requests the applet to stop its current SiGML animation; returns
		// null/undefined if successful, or an error string object (not a
		// String!) in case of failure.
		stopPlayer : function() {
		//--------
			var err = this.getSPA().stopPlayingSiGML();
			if (err) { this.HOST.message( String( err ) ); }
			return err;
		},
		// Sets the applet's animation speed-up factor to the given value;
		// displays a status message announcing the new speed-up value and
		// returns null/undefined if successful, or returns an error string
		// object (not a String!) in case of failure.
		setSpeed : function( speedval ) {
		//------
			this.curSpeed = String( speedval );
			var err = this.getSPA().setSpeed( this.curSpeed );
			if (err) { this.HOST.message( String( err ) ); }
			else { this.HOST.message( "Speed up factor="+speedval ); }
			return err;
		},
		// Increases the applet's animation speed-up factor by a factor of
		// sqrt( 2 ), achieved by passing the new value to setSpeed();
		// returns the setSpeed() result.
		raiseSpeed : function() {
		//--------
			return this.setSpeed( this.curSpeed * Math.SQRT2 );
		},
		// Decreases the applet's animation speed-up factor by a factor of
		// sqrt( 2 ), achieved by passing the new value to setSpeed();
		// returns the setSpeed() result.
		lowerSpeed : function() {
		//--------
			// use sqrt( 1/2 )
			return this.setSpeed( this.curSpeed * Math.SQRT1_2 );
		},
		// Resets the applet's animation speed-up factor to 1,
		// achieved by passing this value to setSpeed();
		// returns the setSpeed() result.
		resetSpeed : function() {
		//--------
			return this.setSpeed( 1.0 );
		},
		// Terminates the applet.
		terminate : function() {
		//-------
			this.getSPA().terminate();
			this.HOST.message( "SPA termination requested." );
		},
		// Sets the applet's logging output generation flag to the given
		// Boolean value.
		setLogFlag : function( checked ) {
		//--------
			var logstr = (checked ? "true" : "false");
			this.getSPA().switchLogEnabled( logstr );
		},
		// Sets the applet's dropped frames logging output generation flag
		// to the given Boolean value.
		setLogDroppedFramesFlag : function ( ldfchecked ) {
		//---------------------
			var logdfstr = (ldfchecked ? "true" : "false");
			var err = this.getSPA().setDoLogDroppedFrames( logdfstr );
			if (err) { this.HOST.message( String( err ) ); }
		},
		// Handler for the applet's avatar load/unload-related events;
		// ekind is the event kind tag (a string-like object but not a String!);
		// avatar is the avatar name.
		avatarEH : function( ekind, avatar ) {
		//------
		/*	AVATAR_LOADED_OK
			AVATAR_LOAD_FAILED
			AVATAR_UNLOADED
		*/
			if (ekind == "AVATAR_LOADED_OK") {
				// NB  avatar is a (rather weird) object.
				this.HOST.notifyAvatarLoad( String( avatar ) );
				this.HOST.message( "Avatar loaded: "+avatar+"." );
			}
			else if (ekind == "AVATAR_LOAD_FAILED") {
				this.HOST.notifyAvatarLoad( null );
				this.HOST.message( "Avatar load failed." );
			}
			else if (ekind == "AVATAR_UNLOADED") {
				this.HOST.notifyAvatarUnload();
				this.HOST.message( "Avatar unloaded: "+avatar+"." );
			}
			else {
				window.alert( "unknown avatar event: <"+ekind+">  <"+avatar+">" );
			}
		},
		// Handler for the applet's animation frames-generation-related events;
		// ekind is the event kind tag (a string-like object but not a String!);
		// where relevant, nf is the frame count, and ns is the sign count.
		framesGenEH : function( ekind, nf, ns ) {
		//---------
		/*	LOAD_FRAMES_START
			LOADED_NEXT_SIGN     nf, ns
			LOAD_FRAMES_DONE_OK  nf, ns
			LOAD_FRAMES_DONE_BAD
		*/
			var msg = "No frames generated from URL.";
			var prevFLimit = -1;
			
			if (ekind == "LOAD_FRAMES_START") {
				this.resetSignData();
				msg = "Loading of frames has started.";
			}
			else if (ekind == "LOADED_NEXT_SIGN") {
				prevFLimit = this.fLimit;
				this.fLimit = nf;
				this.sLimit = ns;
				if (prevFLimit === 0 && this.fLimit !== 0) {
					this.HOST.notifyFirstFramesGenerated();
					msg = ""+ns+" sign(s) now ready to play.";
				}
			}
			else if (ekind == "LOAD_FRAMES_DONE_OK") {
				prevFLimit = this.fLimit;
				this.fLimit = nf;
				this.sLimit = ns;
				// (Use default message if this.fLimit === 0.)
				if (prevFLimit === 0 && this.fLimit === 0) {
					this.HOST.notifyFirstFramesGenerated();
					msg = "Ready to play.";
				}
				else {
					// We are already playing frames from streamed load.
					msg =
						"All frames loaded: "+this.sLimit+" signs, "+
						this.fLimit+" frames.";
				}
				this.HOST.notifyFramesGenerationDone( this.fLimit !== 0 );
			}
			else if (ekind == "LOAD_FRAMES_DONE_BAD") {
				// There are no frames.
				this.HOST.notifyFramesGenerationDone( false );
				// Use default message.
			}
			else {
				msg = "unknown frames-gen event: "+ekind;
				window.alert( msg );
			}
			// If animation is in progress, or is about to be so, then this
			// message will be swamped, but there's no harm in trying.
			this.HOST.message( msg );
		},
		// Handler for the applet's animation-related events;
		// ekind is the event kind tag (a string-like object but not a String!);
		// f is the relevant frame index, and where relevant s is the
		// corresponding sign index.
		animEH : function( ekind, f, s, glss ) {
		//----
		/*	PLAY_FRAME               f
			SKIP_FRAME               f
			PLAY_FIRST_FRAME_OF_SIGN f, s
			SKIP_FIRST_FRAME_OF_SIGN f, s
			PLAY_DONE                f
		*/
			if (ekind == "PLAY_FRAME") {
				this.HOST.detailMessage( "Frame="+f+"  "+this.signMsg );
			}
			else if (ekind == "SKIP_FRAME") {
				this.HOST.detailMessage( "DROPPED frame "+f );
			}
			else if (ekind == "PLAY_FIRST_FRAME_OF_SIGN" ||
					 ekind == "SKIP_FIRST_FRAME_OF_SIGN") {
				this.sign = s;
				this.gloss = String( glss );
				this.signMsg =
					"  Sign="+this.sign+"/"+this.sLimit+",  \""+this.gloss+"\"";
				this.HOST.detailMessage( "Frame="+f+"  "+this.signMsg );
			}
			else if (ekind == "PLAY_DONE") {
				var f_num = f - 0, flimit_num = this.fLimit - 0;
				var smsg = (f_num === flimit_num ? "sign "+this.sLimit+", " : "");
				if (f_num !== 0) {
					this.HOST.message( "Player stopped at "+smsg+"frame "+f+"." );
				}
				this.HOST.notifyAnimationDone();
			}
			else {
				window.alert( "unknown animation event: "+ekind );
			}
		}
	};
}

// The only instance of getSPAProxyProto().
var SPA_PROTO = getSPAProxyProto();

// Constructs a new SPA proxy using the given HOST.
//-----------------------
function ctor_SPA( host ) {
//-----------------------

	var spadata = {
		osTag      : "UNSUPPORTED",  // Set during initialisation (see below).
		appTag     : "UNKNOWN",      // Set during initialisation (see below).
		// appletObj  : null,  // Primary HTML applet, set during HTML initialisation.
		spApplet   : null,  // The true SPA applet, set during HTML initialisation.
		HOST       : host,
		sLimit     : 0,
		fLimit     : 0,
		sign       : -1,
		gloss      : "",
		signMsg    : "",
		curSpeed   : 1
	};

	var spa = makeObject( spadata, SPA_PROTO );
	spa.envInit();  // Set osTag and appTag.

	return spa;
}

//########  Global SPA proxy instance  ########

// This frame depends on its parent to supply the HOST object for the SPA proxy.
var SPA = new ctor_SPA( window.parent.getSPAHost() );
// Once this frame has created the SPA proxy, the parent frame needs to be
// given a reference to the proxy.
window.parent.setSPA( SPA );

//########  Global SiGML-Player-Applet event/callout handlers  ########
// SPA currently requires these to be global.
function spaAvatarEvent( ekind, avatar )        { SPA.avatarEH( ekind, avatar ); }
function spaFramesGenEvent( ekind, nf, ns )     { SPA.framesGenEH( ekind, nf, ns ); }
function spaAnimationEvent( ekind, f, s, glss ) { SPA.animEH( ekind, f, s, glss ); }
// This callout from the SP applet is not used now.
function spaSetSiGMLPlayerApplet( spa ) {}

//############  (end)  ############
