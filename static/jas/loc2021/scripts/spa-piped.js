//########  Javascript for SiGML-Player-Applet  ########
// RE  2007-08


//########  Global Data  ########

var SPA = {

	RETURN_CHAR: 13,
	sLimit: 0,
	fLimit: 0,
	sign: -1,
	gloss: "",
	signMsg: "",
	pipeActive: false,
	curSpeed: 1.0,
	spApplet: null
};

//########  SiGML Player Subapplet Access  ########

function getSPApplet() {

	if (SPA.spApplet === null) {
		setSPA( getSPAChoice(), "initSPA()" );
		if (SPA.spApplet === null) {
			document.ctrlForm.statusExtra.value =
				"HELP! null SiGML Player subapplet.";
		}
	}
	return SPA.spApplet;
}

function getSPAChoice() {
//----------
	// ASSUME: this.appletLoadIsStable();
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
}
//----
function setSPA( spachoice, source ) {
//----
	var nosub = (spachoice.spasub === null);
	var nstag = (nosub ? "no" : "using"); 
	SPA.spApplet = (nosub ? spachoice.spamain : spachoice.spasub);
	if ((typeof console) !== "undefined") { 
		console.log( "setSPA() source: "+source+" -- "+nstag+" subapplet" )
	}
}

//########  Utility Functions  ########

function resetSignGlobals() {

	SPA.sLimit = 0;
	SPA.fLimit = 0;
	SPA.sign = -1;
	SPA.gloss = "";
	SPA.signMsg = "";
}

function resetGlobals() {

	resetSignGlobals();
	SPA.curSpeed = 1.0;
	SPA.pipeActive = false;
}

function showMessage( msg ) {

	document.forms.ctrlForm.statusExtra.value = msg;
	status = msg;
}

function setResetSpeedButton( enabled ) {

	var cform = document.forms.ctrlForm;
	cform.bttnResetSpeed.disabled	= ! enabled;
}

function setSpeedButtons( enabled ) {

	var cform = document.forms.ctrlForm;
	cform.bttnSlower.disabled		= ! enabled;
	cform.bttnFaster.disabled		= ! enabled;
}

function setPlayButtons( avenable, playenable, stopenable ) {

	var cform = document.forms.ctrlForm;
	cform.avSelect.disabled	    = ! avenable;
	cform.bttnPlayURL.disabled  = ! playenable;
	cform.bttnPlayText.disabled = ! playenable;
	cform.bttnStop.disabled     = ! (stopenable && ! SPA.pipeActive);
	cform.bttnClosePipe.disabled= ! (stopenable && SPA.pipeActive);
}

function disableAllButtons() {

	setPlayButtons( false, false, false );
	setResetSpeedButton( false );
	setSpeedButtons( false );
}

function setButtonsForPlaying() {

	setPlayButtons( false, false, true );
	setResetSpeedButton( true );
	setSpeedButtons( true );
}

function setButtonsForIdling() {

	setPlayButtons( true, true, false );
	setResetSpeedButton( true );
	setSpeedButtons( false );
}

function setButtonsForNoAvatar() {

	setPlayButtons( true, false, false );
	setResetSpeedButton( false );
	setSpeedButtons( false );
}

//########  HTML start/finish handlers  ########

function spaSetSiGMLPlayerApplet( sa ) {

	SPA.spApplet = sa;
}

function initialise() {

	resetGlobals();
	setButtonsForNoAvatar();
	showMessage( "HTML initialisation done." );
}

function terminate() {

	getSPApplet().terminate();
	disableAllButtons();
	status = "SiGML-Player-Applet terminated.";
}

//########  SiGML-Player-Applet event/callout handlers  ########

function spaAvatarEvent( ekind, avatar ) {

	var oldav = "NONE";

	if (ekind == "AVATAR_LOADED_OK") {
		// If necessary (typically only after the initial avatar load),
		// set the avatar menu selection to match the newly loaded avatar.
		oldav =  document.forms.ctrlForm.avSelect.value;
		if (oldav != avatar) {
			document.forms.ctrlForm.avSelect.value = avatar;
		}
		showMessage( "Avatar loaded: "+avatar+"." );
		setButtonsForIdling();
	}
	else if (ekind == "AVATAR_LOAD_FAILED") {
		showMessage( "Avatar load failed." );
		setButtonsForNoAvatar();
	}
	else if (ekind == "AVATAR_UNLOADED") {
		showMessage( "Avatar unloaded: "+avatar+"." );
	}
	else {
		alert( "unknown avatar event: <"+ekind+">  <"+avatar+">" );
	}
}

function spaFramesGenEvent( ekind, nf, ns ) {

/*	LOAD_FRAMES_START( FRAMES_GEN_EVENT ),
	LOADED_NEXT_SIGN( FRAMES_GEN_EVENT ),
	LOAD_FRAMES_DONE_OK( FRAMES_GEN_EVENT ),
	LOAD_FRAMES_DONE_BAD( FRAMES_GEN_EVENT ),
 */	var msg = "No frames generated from URL.";
	var prevFLimit = -1;

	if (ekind == "LOAD_FRAMES_START") {
		resetSignGlobals();
		msg = "Loading of frames has started.";
	}
	else if (ekind == "LOADED_NEXT_SIGN") {
		prevFLimit = SPA.fLimit;
		SPA.fLimit = nf;
		SPA.sLimit = ns;
		if (prevFLimit === 0 && SPA.fLimit !== 0) {
			// These are the first frames for this animation that we know of.
			setButtonsForPlaying();
			msg = ""+ns+" sign(s) now ready to play.";
		}
	}
	else if (ekind == "LOAD_FRAMES_DONE_OK") {
		prevFLimit = SPA.fLimit;
		SPA.fLimit = nf;
		SPA.sLimit = ns;
		if (SPA.fLimit === 0) {
			// Load failed to generate any frames.
			setButtonsForIdling();
			// Use default message.
		}
		else if (prevFLimit === 0) {
			// Now there are frames, and they're the first we know of.
			setButtonsForPlaying();
			msg = "Ready to play.";
		}
		else {
			// We are already playing frames from streamed load.
			msg = "All frames loaded: "+SPA.sLimit+" signs, "+SPA.fLimit+" frames.";
		}
	}
	else if (ekind == "LOAD_FRAMES_DONE_BAD") {
		// There are no frames.
		setButtonsForIdling();
		// Use default message.
	}
	else {
		alert( "unknown frames-gen event: "+ekind );
	}
	// If animation is in progress, or is about to be so, then this message
	// will be swamped, but there's no harm in trying.
	showMessage( msg );
}

function spaAnimationEvent( ekind, f, s, glss ) {

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

	if (ekind == "PLAY_FRAME") {
		showMessage( "Frame="+f+"  "+SPA.signMsg );
	}
	else if (ekind == "SKIP_FRAME") {
		showMessage( "DROPPED frame "+f );
	}
	else if (ekind == "PLAY_FIRST_FRAME_OF_SIGN" ||
			 ekind == "SKIP_FIRST_FRAME_OF_SIGN") {
		SPA.sign = s;
		SPA.gloss = glss;
		SPA.signMsg = "  Sign="+SPA.sign+"/"+SPA.sLimit+",  \""+SPA.gloss+"\"";
		showMessage( "Frame="+f+"  "+SPA.signMsg );
	}
	else if (ekind == "PLAY_DONE") {
		var f_num = f - 0, flimit_num = SPA.fLimit - 0;
		var smsg = (f_num === flimit_num ? "sign "+SPA.sLimit+", " : "");
		if (f_num !== 0) {
			showMessage( "Player stopped at "+smsg+"frame "+f+"." );
		}
		setButtonsForIdling();
	}
	else {
		alert( "unknown animation event: "+ekind );
	}
}

//########  HTML button/input handlers  ########

function selectAvatar() {

	var avatar = document.forms.ctrlForm.avSelect.value;
	getSPApplet().setAvatar( avatar );
}

function setSpeed( speedval ) {

	var speedstr = String( speedval );
	var sserr = getSPApplet().setSpeed( speedstr );
	// The per-frame messages drown out the following one.
	//showMessage( "Speed="+speedstr );
	// Applet call returns null or undefined if OK, error text otherwise.
	if (sserr) {
		showMessage( sserr );
		alert( "bad speed: "+sserr );
	}
}

function raiseSpeed() {

	SPA.curSpeed *= Math.SQRT2;
	setSpeed( SPA.curSpeed );
}

function lowerSpeed() {

	SPA.curSpeed *= Math.SQRT1_2;  // i.e. sqrt(1/2)
	setSpeed( SPA.curSpeed );
}

function resetSpeed() {

	SPA.curSpeed = 1.0;
	setSpeed( SPA.curSpeed );
}

function startPlayURL() {

	resetSignGlobals();
	setButtonsForPlaying();
	var sigmlURL = document.forms.ctrlForm.urlText.value;
	var perr = getSPApplet().playSiGMLURL( sigmlURL );
	// Applet call returns null or undefined if OK, error text otherwise.
	if (perr) {
		setButtonsForNoAvatar();
		showMessage( perr );
	}
}

function startPlayText() {

	if (document.forms.ctrlForm.chkPiped.checked) { startPlayTextPiped(); }
	else { startPlayTextNormal(); }
}

function startPlayTextNormal() {

	//console.log( "startPlayTextNormal()" );
	resetSignGlobals();
	setButtonsForPlaying();
	var sigmlText = document.forms.ctrlForm.sigmlText.value;
	var perr = getSPApplet().playSiGMLText( sigmlText );
	// Applet call returns null or undefined if OK, error text otherwise.
	if (perr) {
		setButtonsForNoAvatar();
		showMessage( perr );
	}
}

function startPlayTextPiped() {

	//console.log( "startPlayTextPiped()" );
	resetSignGlobals();
	SPA.pipeActive = true;
	setButtonsForPlaying();
	var sigmlText = document.forms.ctrlForm.sigmlText.value;
	var schunks = splitSiGML( sigmlText );
	var pperr = getSPApplet().startPlaySiGMLPiped();
	if (pperr) {
		SPA.pipeActive = false;
		setButtonsForNoAvatar();
		if ((typeof console) !== "undefined") { console.log( perr ); } 
		showMessage( pperr );
	}
	//else { console.log( "startPSPiped() OK" ); }

	var N = schunks.length, i = 0;

	function oneChunk() {
		if (SPA.pipeActive) {
			var fragment = String( schunks[ i ] );
			//console.log( "Append fragment "+i+" to pipe:\n"+fragment );
			var aerr = getSPApplet().appendToSiGMLPipe( fragment );
			if (aerr) { 
				if ((typeof console) !== "undefined") { console.log( aerr ); }
				showMessage( aerr ); 
			}
			++ i;
			if (i != N) { setTimeout( oneChunk, 3000 ); }
			//else { console.log( "SiGML pipe has all "+N+" signs." ); }
		}
		else if ((typeof console) !== "undefined") { 
			console.log( "SiGML pipe becomes inactive." ); 
		}
	}

	if (i != N) { oneChunk(); }
}

function closePipe() {

	if (SPA.pipeActive) {
		SPA.pipeActive = false;
		setButtonsForPlaying();
		var cerr = getSPApplet().closeSiGMLPipe();
		if (cerr) { 
			if ((typeof console) !== "undefined") { console.log(cerr); } 	
			showMessage( cerr ); 
		}
	}
	else if ((typeof console) !== "undefined") {
		console.log( "closePipe() when SiGML pipe is not active." );
	}
}

function stopPlayer() {

	SPA.pipeActive = false;
	setButtonsForIdling();
	var serr = getSPApplet().stopPlayingSiGML();
	// Applet call returns null or undefined if OK, error text otherwise.
	if (serr !== null && serr !== undefined) {
		showMessage( serr );
	}
}

function setLogFlag() {

	var checked = document.forms.ctrlForm.chkLog.checked;
	var logstr = (checked ? "true" : "false");
	getSPApplet().switchLogEnabled( logstr );
}

function doPreventDefault( evt ) {

	// Fix IE non-standardness.
	if (evt.preventDefault !== undefined) {
		evt.preventDefault();		// DOM
	}
	else {
		evt.returnValue = false;	// IE nonstandard
	}
}

function handleURLKey( evt ) {

	// IE doesn't do evt.which, but always does evt.keyCode;
	var chr = evt.which || evt.keyCode;
	if (chr == SPA.RETURN_CHAR) {
		doPreventDefault( evt );
		startPlayURL();
	}
}

// Returns an array of SiGML sign elements.
function splitSiGML( sigml ) {

	// At present we extract only <hns_sign>s and <hamgestural_sign>s,
	// i.e. no signing_refs etc.
	//console.log( "splitSiGML()" );
	var schunks = [];
	var hbegtag = "<hns_sign", hendtag = "</hns_sign>";
	var gbegtag = "<hamgestural_sign", gendtag = "</hamgestural_sign>";
	var hetlen = hendtag.length, getlen = gendtag.length;
	var ii = -1, jj = 0;
	var iih = sigml.indexOf( hbegtag, jj );
	var iig = sigml.indexOf( gbegtag, jj );
	while (0 <= iih || 0 <= iig) {
		var gwins = (iih < 0 || (0 <= iig && iig < iih));
		if (gwins) {
			ii = iig;
			jj = sigml.indexOf( gendtag, iig ) + getlen;
		}
		else {
			ii = iih;
			jj = sigml.indexOf( hendtag, iih ) + hetlen;
		}
		//console.log( "fragment "+(schunks.length)+":  "+ii+" to "+jj );
		schunks.push( sigml.substring( ii, jj ) );
		//alert( schunks[ schunks.length - 1 ] );
		if (0 <= iih) { iih = sigml.indexOf( hbegtag, jj ); }
		if (0 <= iig) { iig = sigml.indexOf( gbegtag, jj ); }
	}
	//console.log( "N_signs="+schunks.length );
	return schunks;
}

// (END javascript)
