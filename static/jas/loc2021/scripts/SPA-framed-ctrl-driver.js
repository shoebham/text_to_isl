//########  Javascript for SPA-framed-ctrl-driver.html  ########
//
// RE  2009-04

/*
 * This is the script for the driverframe (SPA-framed-ctrl-driver.html).
 * It assumes that it drives a sibling spawithctrlframe (spa-with-ctrl-frame.html),
 * that has a controlling member object called SPACTRL.
 */

var SPACTRLDRIVER = {

	hasAvatar : false,
	//-------
	playerBusy : false,
	//--------

	asynchRepeat : function( body, ok, tag ) {
		//----------
		var n = 0;
		function closedARFun() {
			body();
			if (! ok()) { ++ n; setTimeout( closedARFun, 1 ); }
			else if (tag) { console.log( tag+" repetition count="+n ); }
		}
		closedARFun();
	},
	initialise : function() {
	//--------
		// Handle the setting up of the event handler by our sibling frame
		// asynchronously, since we have no way of knowing when the
		// sibling frame is ready.
		var n = 0;
		var seteh = undefined;
		function tryToSetEH() {
			var swcframe = parent.spawithctrlframe;
			if (swcframe) { seteh = swcframe.setEventHandler; }
			if (seteh) { seteh( "driverframe" ); }
		}
		function done() { return Boolean( seteh ); }
		this.asynchRepeat( tryToSetEH, done/*, "CTRL Driver init"*/ );
	},
	terminate : function() {
	//-------
		var form = document.forms.spasigmlform;
		form.bttnPlayURL.disabled = true;
		form.bttnPlayText.disabled = true;
		form.bttnStopPlay.disabled = true;
	},
	updateButtonsStatus : function() {
	//-----------------
		var playoff = (!this.hasAvatar || this.playerBusy);
		var form = document.forms.spasigmlform;
		form.bttnPlayURL.disabled = playoff;
		form.bttnPlayText.disabled = playoff;
		form.bttnStopPlay.disabled = !this.playerBusy;
	},
	checkSPAError : function( err, method ) {
	//-----------
		if (err !== null && err !== undefined) {
			console.log( "Driver frame, "+method+": "+err );
			if (method !== "stopPlaySiGML") {
				this.playerBusy = false; this.updateButtonsStatus();
			}
		}
	},
	playSiGMLURL : function() {
	//----------
		this.playerBusy = true;
		this.updateButtonsStatus()
		var sigmlurl = document.forms.spasigmlform.urlText.value;
		var err = parent.spawithctrlframe.SPACTRL.playSiGMLURL( sigmlurl );
		this.checkSPAError( err, "playSiGMLURL" );
	},
	playSiGMLText : function() {
	//-----------
		this.playerBusy = true;
		this.updateButtonsStatus()
		var sigml = document.forms.spasigmlform.sigmlText.value;
		var err = parent.spawithctrlframe.SPACTRL.playSiGMLText( sigml );
		this.checkSPAError( err, "playSiGMLText" );
	},
	stopPlayingSiGML : function() {
	//--------------
		var err = parent.spawithctrlframe.SPACTRL.stopPlayingSiGML();
		this.checkSPAError( err, "stopPlaySiGML" );
	},
	handleURLKey : function( evt ) {
	//----------
		var	RETURN_CHAR = 13;
		// IE doesn't do evt.which, but always does evt.keyCode;
		var chr = evt.which || evt.keyCode;
		if (chr == RETURN_CHAR) {
			// Fix IE non-standardness.
			if (evt.preventDefault !== undefined) { /*DOM*/ evt.preventDefault(); }
			else { /*IE non-std*/ evt.returnValue = false; }
			this.playSiGMLURL();
		}
	},
	avatarLoadDone : function( av ) {
	//-----------
		if (av !== null) {
			this.hasAvatar = true;
			this.updateButtonsStatus();
		}
	},
	framesGenDone : function( fgok ) {
	//-----------
		if (!fgok) {
			this.playerBusy = false;
			this.updateButtonsStatus();
		}
	},
	animationDone : function() {
	//-----------
		this.playerBusy = false;
		this.updateButtonsStatus();
	}	
};

// Global (to this driver frame) event handler functions -- delegated to
// SPACTRLDRIVER.
function avatarLoadDone( av )  { SPACTRLDRIVER.avatarLoadDone( av ); }
function framesGenDone( fgok ) { SPACTRLDRIVER.framesGenDone( fgok ); }
function animationDone()       { SPACTRLDRIVER.framesGenDone(); }

//############  (end)  ############
