//########  Javascript for Sign Wiki SiGML-Player-Applet  ########
// Creates SWSPA object, which acts as a proxy/controller for the SP applet.
// RE  2011-11  Adapted from standard spa-object.js;

/*globals window
*/

//-----------------------------
function makeObj( data, PROTO ) {
//-----------------------------
	function Ctor() {
		var k;
		// Copy the given data to this:
		for (k in data) {
			// hasOwnProperty() check should be redundant;
			if (data.hasOwnProperty( k )) { this[ k ] = data[ k ]; }
		}
	}
	// Use the given prototype for instances of our ctor.
	Ctor.prototype = PROTO;
	return new Ctor();
}

//--------------------
function getSWSPAProto() {
//--------------------
	return {
	//########  Messy/low-level plumbing methods  ########
		//---
		onWin : function() { return (this.osTag==="win"); },
		//---
		//---
		onMac : function() { return (this.osTag==="mac"); },
		//---
		//--------
		isInSafari : function() { return (this.appTag === "safari"); },
		//--------
		//---------
		isInFirefox : function() { return (this.appTag === "firefox"); },
		//---------
		//--------
		isInChrome : function() { return (this.appTag === "chrome"); },
		//--------
		//----
		isInIE : function() { return (this.appTag === "ie"); },
		//----
		// A crude way of dealing with the fact that IE has no console:
		//-
		log : function( msg ) {
		//-
			if ((typeof window.console) !== "undefined") {
				window.console.log( msg );
			}
		},
		//-----
		preInit : function() {
		//-----
			var avlc = window.navigator.appVersion.toLowerCase(),
				ualc = window.navigator.userAgent.toLowerCase();

			if (avlc.indexOf( "win" ) !== -1) { this.osTag = "win"; }
			else if (avlc.indexOf( "mac" ) !== -1) { this.osTag = "mac"; }

			// Must look for chrome before safari.
			if (ualc.indexOf( "chrome" ) !== -1) {this.appTag = "chrome"; }
			else if (ualc.indexOf( "safari" ) !== -1) { this.appTag = "safari"; }
			else if (ualc.indexOf( "firefox" ) !== -1) { this.appTag = "firefox"; }
			else if (ualc.indexOf( "msie" ) !== -1) { this.appTag = "ie"; }
		},
		//----------------
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
		//------------
		getSWSPAChoice : function() {
		//------------
			// ASSUME: this.appletLoadIsStable();
			var applet = window.document.swSPApplet,
				choice = { spamain : null, spasub : null };
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
		//------
		setSWSPA : function( swspachoice, source ) {
		//------
			var nosub = (swspachoice.spasub === null),
				nstag = (nosub ? "no" : "using"); 
			this.swSPApplet = (nosub ? swspachoice.spamain : swspachoice.spasub);
			this.log( "setSWSPA() source: "+source+" -- "+nstag+" subapplet" );
		},
	//########  HTML start/finish handlers  ########
		//--------
		initialise : function( doplayalways ) {
		//--------
			// preInit() should have been called already, but play safe.
			if (!this.osTag) { this.preInit(); }
			this.log( "OS: "+this.osTag+"   Browser: "+this.appTag );
			// By default doPlayAlways is false.  It will only be true
			// if an actual argument is passed to this method and that
			// argument is (when coerced to Boolean) true.
			this.doPlayAlways = Boolean( doplayalways );
			if (this.doPlayAlways) {
				this.log( "Allowing a new Play to abort any current Play." );
			}
			this.doSynchInit();
			// Chrome on Mac OS X cannot withstand much interrogation -- that
			// is, it either locks up or throws an exception -- until its
			// applet load sequence is complete.
			if (!this.isInChrome() || !this.onMac()) { this.initSWSPApplet(); }
		},
		//---------
		doSynchInit : function() {
		//---------			
			this.resetSWSPAData();
			this.ctrlsA = window.document.forms.ctrlAForm;
			this.ctrlsB = window.document.forms.ctrlBForm;
			this.setButtonsForNoAvatar();
			this.ctrlsA.fpsSelect.value = this.curFPS;
			
			this.showMessage( "HTML synchronous initialisation done." );
		},
		//------------
		initSWSPApplet : function() {
		//------------
			if (this.appletLoadIsStable()) {
				this.setSWSPA( this.getSWSPAChoice(), "initSWSPApplet()" );
			}
			else {
				this.asynchGetSWSPA();
			}
		},
		//------------
		asynchGetSWSPA : function() {
		//------------
			var thisobj = this, swspachoice;
			function tryFindSPA() {
				if (thisobj.appletLoadIsStable()) {
					swspachoice = thisobj.getSWSPAChoice();
					thisobj.setSWSPA( swspachoice, "asynchGetSWSPA()" );
				}
			}
			function done() { return Boolean( swspachoice ); }
			this.asynchRepeat( tryFindSPA, done/**/, "asynchGetSWSPA(): "/**/ );
		},
		//-------
		terminate : function() {
		//-------
			this.getSWSPA().terminate();
			this.disableAllButtons();
			window.status = "SignWiki SPA terminated.";
		},
	//########  Sign Wiki SiGML Player (Sub)Applet Access  ########
		//------
		getSWSPA : function() {
		//------
			if (this.swSPApplet === null) {
				// We should never get here except when running Chrome on
				// Mac OS X -- but in that case we definitely need to get here.
				if (this.appletLoadIsStable()) {
					this.setSWSPA( this.getSWSPAChoice(), "getSWSPA()" );
				}
				else {
					this.log( "getSWSPA(): catastrophe -- cannot get SW-SPA!." );
				}
			}
			return this.swSPApplet;
		},
	//########  Utility Functions  ########
		// Several applet calls return null or undefined if OK,
		// otherwise an error text.
		//---
		isBad : function( err ) { return (err !== undefined && err !== null); },
		//---
		// Asynchronously make repeated calls to the body function until
		// the ok function succeeds, i.e. returns a true result.
		// On completion, if tag is defined then log the tag and repetition count.
		//----------
		asynchRepeat : function( body, ok, tag ) {
		//----------
			var thisobj = this, n = 0;
			function closedARFun() {
				body();
				if (! ok()) { n += 1; window.setTimeout( closedARFun, 1 ); }
				else if (tag) { thisobj.log( tag+" repetition count="+n ); }
			}
			closedARFun();
		},
		//-----------
		resetSignData : function() {
		//-----------
			this.sLimit = 0;
			this.fLimit = 0;
			this.sign = -1;
			this.gloss = "";
			this.signMsg = "";
		},
		//------------
		resetSWSPAData : function() {
		//------------
			this.resetSignData();
			// curFPS should match applet's "animgen.fps" <param> value;
			this.curFPS = "50";
			this.curSpeed = 1.0;
		},
		//---------
		showMessage : function( msg ) {
		//---------
			this.ctrlsB.statusExtra.value = msg;
			window.status = msg;
		},
		//-----------------
		setResetSpeedButton : function( enabled ) {
		//-----------------
			this.ctrlsA.bttnResetSpeed.disabled	= ! enabled;
		},
		//-------------
		setSpeedButtons : function( enabled ) {
		//-------------
			this.ctrlsA.bttnSlower.disabled		= ! enabled;
			this.ctrlsA.bttnFaster.disabled		= ! enabled;
		},
		//---------------
		setAvSelectButton : function( avenable ) {
		//---------------
			this.ctrlsA.avSelect.disabled = ! avenable;
		},
		//------------
		setPlayButtons : function( playenable, stopenable ) {
		//------------
			this.ctrlsA.bttnPlayURL.disabled  = ! playenable;
			this.ctrlsA.bttnPlayText.disabled = ! playenable;
			this.ctrlsA.bttnStop.disabled     = ! stopenable;
		},
		//---------------
		setDoLogDFEnabled :function( enable ) {
		//---------------
			this.ctrlsA.chkDoLogDroppedFrames.disabled = ! enable;
		},
		//---------------
		disableAllButtons : function() {
		//---------------
			this.setAvSelectButton( false );
			this.setPlayButtons( false, false );
			this.setResetSpeedButton( false );
			this.setSpeedButtons( false );
			this.setDoLogDFEnabled( false );
		},
		//------------------
		setButtonsForPlaying : function() {
		//------------------
			this.setAvSelectButton( false );
			this.setPlayButtons( this.doPlayAlways, true );
			this.setResetSpeedButton( true );
			this.setSpeedButtons( true );
			this.setDoLogDFEnabled( false );
		},
		//-----------------
		setButtonsForIdling : function() {
		//-----------------
			this.setAvSelectButton( true );
			this.setPlayButtons( true, false );
			this.setResetSpeedButton( true );
			this.setSpeedButtons( false );
			this.setDoLogDFEnabled( true );
		},
		//-------------------
		setButtonsForNoAvatar : function() {
		//-------------------
			this.setAvSelectButton( true );
			this.setPlayButtons( false, false );
			this.setResetSpeedButton( false );
			this.setSpeedButtons( false );
			this.setDoLogDFEnabled( true );
		},
	//########  SiGML-Player-Applet event/callout handlers  ########
		//------
		avatarEH : function ( ekind, avatar ) {
		//------
			var ekind_str = String( ekind ), avmenu;
			if (ekind_str === "AVATAR_LOADED_OK") {
				// If necessary (typically only after the initial avatar load),
				// set the avatar menu selection to match the newly loaded avatar.
				avmenu =  this.ctrlsA.avSelect;
				if (avmenu.value !== avatar) { avmenu.value = avatar; }
				this.showMessage( "Avatar loaded: "+avatar+"." );
				this.setButtonsForIdling();
			}
			else if (ekind_str === "AVATAR_LOAD_FAILED") {
				this.showMessage( "Avatar load failed." );
				this.setButtonsForNoAvatar();
			}
			else if (ekind_str === "AVATAR_UNLOADED") {
				this.showMessage( "Avatar unloaded: "+avatar+"." );
			}
			else {
				window.alert( "unknown avatar event: <"+ekind+">  <"+avatar+">" );
			}
		},
		//---------
		framesGenEH : function( ekind, nf, ns ) {
		//---------
			/*	LOAD_FRAMES_START( FRAMES_GEN_EVENT ),
				LOADED_NEXT_SIGN( FRAMES_GEN_EVENT ),
				LOAD_FRAMES_DONE_OK( FRAMES_GEN_EVENT ),
				LOAD_FRAMES_DONE_BAD( FRAMES_GEN_EVENT ),
			 */
			var ekind_str = String( ekind ),
				msg = "No frames generated from URL.", prevFLimit = -1;
			if (ekind_str === "LOAD_FRAMES_START") {
				this.resetSignData();
				msg = "Loading of frames has started.";
			}
			else if (ekind_str === "LOADED_NEXT_SIGN") {
				prevFLimit = this.fLimit;
				this.fLimit = nf;
				this.sLimit = ns;
				if (prevFLimit === 0 && this.fLimit !== 0) {
					// For this animation these are the
					// first frames that we know of.
					this.setButtonsForPlaying();
					msg = ns+" sign(s) now ready to play.";
				}
			}
			else if (ekind_str === "LOAD_FRAMES_DONE_OK") {
				prevFLimit = this.fLimit;
				this.fLimit = nf;
				this.sLimit = ns;
				if (this.fLimit === 0) {
					// Load failed to generate any frames.
					this.setButtonsForIdling();
					// Use default message.
				}
				else if (prevFLimit === 0) {
					// Now there are frames, and they're the first we know of.
					this.setButtonsForPlaying();
					msg = "Ready to play.";
				}
				else {
					// We are already playing frames from streamed load.
					msg =
						"All frames loaded: "+this.sLimit+" signs, "+
						this.fLimit+" frames.";
				}
			}
			else if (ekind_str === "LOAD_FRAMES_DONE_BAD") {
				// There are no frames.
				this.setButtonsForIdling();
				// Use default message.
			}
			else {
				msg = "unknown frames-gen event: "+ekind;
				window.alert( msg );
			}
			// If animation is in progress, or is about to be so, then this
			// message will be swamped, but there should be no harm in trying.
			this.showMessage( msg );
		},
		//----
		animEH : function( ekind, f, s, glss ) {
		//----
			/*	play-frame            f
			 *  play-at-new-sign      f, s
			 *  skip-frame            f
			 *  skip-at-new-sign      f, s
			 *  done-play             f
			 */
			/*
			 PLAY_FRAME( ANIMATION_EVENT ),
			 SKIP_FRAME( ANIMATION_EVENT ),
			 PLAY_FIRST_FRAME_OF_SIGN( ANIMATION_EVENT ),
			 SKIP_FIRST_FRAME_OF_SIGN( ANIMATION_EVENT ),
			 PLAY_DONE( ANIMATION_EVENT )
			 */
			var ekind_str = String( ekind ),
				f_num, flimit_num, smsg, thisswspa, asynchSPP;
			if (ekind_str === "PLAY_FRAME") {
				this.showMessage( "Frame="+f+"  "+this.signMsg );
			}
			else if (ekind_str === "SKIP_FRAME") {
				this.showMessage( "DROPPED frame "+f );
			}
			else if (ekind_str === "PLAY_FIRST_FRAME_OF_SIGN" ||
					 ekind_str === "SKIP_FIRST_FRAME_OF_SIGN") {
				this.sign = s;
				this.gloss = glss;
				this.signMsg =
					"  Sign="+this.sign+"/"+this.sLimit+",  \""+this.gloss+"\"";
				this.showMessage( "Frame="+f+"  "+this.signMsg );
			}
			else if (ekind_str === "PLAY_DONE") {
				f_num = Number( f );
				flimit_num = Number( this.fLimit );
				smsg = (f_num===flimit_num ? "sign "+this.sLimit+", " : "");
				if (f_num !== 0) {
					this.showMessage( "Player stopped at "+smsg+"frame "+f+"." );
				}
				if (this.urlPending!==null || this.textPending!==null) {
					// We're on a Java GUI/GL thread, so we must
					// invoke startPendingPlay() asynchronously.
					thisswspa = this;
					asynchSPP = function() { thisswspa.startPendingPlay(); };
					window.setTimeout( asynchSPP, 0 );
				}
				else {
					this.playIsBusy = false;
					this.setButtonsForIdling();
				}
			}
			else {
				window.alert( "unknown animation event: "+ekind );
			}
		},
	//########  HTML button/input handlers  ########
		//----------
		selectAvatar : function() {
		//----------
			//###### hack: insert a test:
			var dotest = false;
			if (dotest) {
				window.console.log( "start swop test" );
				var swop = this.getSWSPA().siWikiOp( "swkConcat", "alpha", "beta" );
				var swoptp = typeof swop;
				window.alert( "swOp: type="+swoptp+"  val=|"+swop+"|" );
				window.console.log( "done swop test" );
			}
			//######
			window.console.log( "start setAvatar()" );
			this.getSWSPA().setAvatar( this.ctrlsA.avSelect.value );
			window.console.log( "done setAvatar()" );
		},
		//-----------
		setAnimgenFPS : function() {
		//-----------
			var fpsmenu = this.ctrlsA.fpsSelect, fpsstr = fpsmenu.value,
				fpserr = this.getSWSPA().setAnimgenFPS( fpsstr );
			if (this.isBad( fpserr )) {
				if (this.curFPS !== fpsstr) { fpsmenu.value = this.curFPS; }
				this.showMessage( fpserr );
				window.alert( "bad FPS: "+fpserr );
			}
			else {
				this.curFPS = fpsstr;
			}
		},
		//-------------------
		setDoLogDroppedFrames : function() {
		//-------------------
			var checked = this.ctrlsA.chkDoLogDroppedFrames.checked,
				dldfstr = (checked ? "true" : "false"),
				dldferr = this.getSWSPA().setDoLogDroppedFrames( dldfstr );
			if (this.isBad( dldferr )) { this.showMessage( dldferr); }
		},
		//------
		setSpeed : function( speedval ) {
		//------
			var speedstr = String( speedval ),
				sserr = this.getSWSPA().setSpeed( speedstr );
			// The per-frame messages drown out the following one.
			//this.showMessage( "Speed="+speedstr );
			if (this.isBad( sserr )) {
				this.showMessage( sserr );
				window.alert( "bad speed: "+sserr );
			}
		},
		//--------
		raiseSpeed : function() {
		//--------
			this.curSpeed *= Math.SQRT2;
			this.setSpeed( this.curSpeed );
		},
		//--------
		lowerSpeed : function() {
		//--------
			this.curSpeed *= Math.SQRT1_2;  // i.e. sqrt(1/2)
			this.setSpeed( this.curSpeed );
		},
		//--------
		resetSpeed : function() {
		//--------
			this.curSpeed = 1.0;
			this.setSpeed( this.curSpeed );
		},
		//----------
		startPlayURL : function() {
		//----------
			var sigmlurl = this.ctrlsB.urlText.value;
			// The (real) SPA should cope with any bad URL, but it seems
			// to take a long time over an empty one, so catch that case
			// here.
			if (sigmlurl.length === 0) {
				this.log( "Ignoring zero-length SiGML URL." );
			}
			else {
				if (this.playIsBusy) {
					if (this.doPlayAlways && this.checkNoPendingPlay()) {
						this.urlPending = sigmlurl;
						this.stopPlayer();
					}
				}
				else {
					this.doPlayURL( sigmlurl );
				}
			}
		},
		//-----------
		startPlayText : function() {
		//-----------
			var sigmltxt = this.ctrlsB.sigmlText.value;
			if (this.playIsBusy) {
				if (this.doPlayAlways && this.checkNoPendingPlay()) {
					this.textPending = sigmltxt;
					this.stopPlayer();
				}
			}
			else {
				this.doPlayText( sigmltxt );
			}
		},
		//--------
		stopPlayer : function() {
		//--------
			this.setButtonsForIdling();
			var serr = this.getSWSPA().stopPlayingSiGML();
			if (this.isBad( serr )) { this.showMessage( serr ); }
		},
		//--------------
		setLogFlag : function() {
		//--------------
			var logstr = (this.ctrlsA.chkLog.checked ? "true" : "false");
			this.getSWSPA().switchLogEnabled( logstr );
		},
		//--------------
		doPreventDefault : function( evt ) {
		//--------------
			// Fix IE non-standardness.
			if (evt.preventDefault !== undefined) {
				evt.preventDefault();		// DOM
			}
			else {
				evt.returnValue = false;	// IE nonstandard
			}
		},
		//--------------
		startPendingPlay : function() {
		//--------------
			var sigmlurl, sigmltxt;
			if (this.urlPending !== null) {
				sigmlurl = this.urlPending;
				this.urlPending = null;
				this.doPlayURL( sigmlurl );
			}
			else if (this.textPending !== null) {
				sigmltxt = this.textPending;
				this.textPending = null;
				this.doPlayText( sigmltxt );
			}
			//else NEVER
		},
		//----------------
		checkNoPendingPlay : function() {
		//----------------
			var nopp = (this.urlPending===null) && (this.textPending===null);
			if (! nopp) {
				this.log("SPA (pa=true): cannot accept second pending play.");
			}
			return nopp;
		},
		//-------
		doPlayURL : function( sigmlurl ) {
		//-------
			this.resetSignData();
			this.setButtonsForPlaying();
			var perr = this.getSWSPA().playSiGMLURL( sigmlurl );
			this.playIsBusy = ! this.isBad( perr );
			if (! this.playIsBusy) {
				// 2011-10: Not sure whether "for no avatar" is the
				// appropriate choice here.
				this.setButtonsForNoAvatar();
				this.showMessage( perr );
			}
		},
		//--------
		doPlayText : function( sigmltxt ) {
		//--------
			this.resetSignData();
			this.setButtonsForPlaying();
			var perr = this.getSWSPA().playSiGMLText( sigmltxt );
			this.playIsBusy = ! this.isBad( perr );
			if (! this.playIsBusy) {
				// 2011-10: Not sure whether "for no avatar" is the
				// appropriate choice here.
				this.setButtonsForNoAvatar();
				this.showMessage( perr );
			}
		},
		//----------
		handleURLKey : function( evt ) {
		//----------
			// IE doesn't do evt.which, but always does evt.keyCode;
			var chr = evt.which || evt.keyCode;
			if (chr === this.RETURN_CHAR) {
				// Treat "return" on the URL field like a click on the
				// Play-URL button.
				this.doPreventDefault( evt );
				this.startPlayURL();
			}
		}
		,
		//----
		swTest : function( arg ) {
		//----
			this.ctrlsA.bttnSWTest.disabled	= true;
			//----------------
			var msgarg = "swTest:\n"+arg, args = [ arg ], result, N,
				timemsg, docheckin = false, docheckout = false;
			if (docheckin) {
				//window.alert( msgarg );
				window.console.log( msgarg );
			}
			result = this.doSiWikiOp( "swopGetSignTimes", args );
			if (docheckout) {
				N = (result ? result.length : 0);
				window.console.log( "RESULT len="+N );
				for (var i=0; i!=N; i+=1) {
					console.log( i+": "+result[ i ] );
				}
			}
			if (this.swopIsOK( result )) {
				timemsg =
					"status="+result[0]+
					";  sign times: "+result[1]+", "+result[2];
			//	window.alert( timemsg );
				window.console.log( timemsg );
			}
			//----------------
			result = this.doSiWikiOp( "swopGetSignHands", args );
			window.console.log(
				"swopGetSignHands(): "+result[ this.swopIsOK( result )?1:0 ] );
			//----------------
			result = this.doSiWikiOp( "swopSignH2G", args );
			if (this.swopIsOK( result )) {
				window.console.log( "g-SiGML ..." );
				window.console.log( result[ 1 ] );
			}
			else {
				window.console.log( result[ 0 ] );
			}
			//----------------
			gargs = [ result[1] ];
			result = this.doSiWikiOp( "swopSignG2H", gargs );
			if (this.swopIsOK( result )) {
				window.console.log( "h-SiGML ..." );
				window.console.log( result[ 1 ] );
			}
			else {
				window.console.log( result[ 0 ] );
			}
			//----------------
			this.ctrlsA.bttnSWTest.disabled	= false;
		}
		,
		//------
		swopIsOK : function( result ) {
		//------
			return result && result.length !== 0 && result[0] == "OK";
		}
		,
		//--------
		doSiWikiOp : function( op, args ) {
		//--------
			return this.makeStrings( this.getSWSPA().siWikiOp( op, args ) );
		}
		,
		//---------
		makeStrings : function( vec ) {
		//---------
			var i, N = vec.length, strvec = [];
			for (i=0; i!==N; i+=1) { strvec.push( String( vec[ i ] ) ); }
			return strvec;
		}
	};
}

//-----------------
function Ctor_SWSPA() {
//-----------------
	var swspadata = {
		RETURN_CHAR     : 13,
		sLimit          : 0,
		fLimit          : 0,
		sign            : -1,
		gloss           : "",
		signMsg         : "",
		curFPS          : "50",	// to match applet's "animgen.fps" <param> value;
		curSpeed        : 1,
		osTag           : "UNSUPPORTED",
		appTag          : "UNKNOWN",
		ctrlsA          : null,
		ctrlsB          : null,
		swSPApplet      : null,
		//---- 2011-10  Extra members for "play always" enhancement:
		doPlayAlways    : false,
		playIsBusy      : false,
		urlPending      : null,
		textPending     : null
	};
	return makeObj( swspadata, getSWSPAProto() );
}

//------------------------
var SWSPA = new Ctor_SWSPA();
//------------------------
SWSPA.preInit();

// Global SP applet event-handler functions -- delegating to the SPA object.
function spaAvatarEvent( ekind, avatar ) { SWSPA.avatarEH( ekind, avatar ); }
function spaFramesGenEvent( ekind, nf, ns ) { SWSPA.framesGenEH( ekind, nf, ns ); }
function spaAnimationEvent( ekind, f, s, glss ) { SWSPA.animEH( ekind, f, s, glss ); }

// This callout from the SP applet is not used now.
function spaSetSiGMLPlayerApplet( /*spa*/ ) {}

// (END javascript)
