//########  Javascript for SPA-framed-gui-driver.html  ########
//
// RE  2009-04

/*
 * This is the script for the driverframe (SPA-framed-gui-driver.html).
 * It assumes that it drives a sibling spawithguiframe (spa-with-gui-frame.html),
 * that has a controlling member object called SPAGUI.
 */

var SPAGUIDRIVER = {

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
		// Set up PEC handler in our sibling frame asynchronously, since we
		// have no way of knowing when the sibling frame is ready.
		var notifypect = undefined;
		function tryToNotifyPECT() {
			var swgframe = parent.spawithguiframe;
			if (swgframe) { notifypect = swgframe.notifyPlayEnableChangeTarget; }
			if (notifypect) { notifypect( window ); }
		}
		function done() { return Boolean( notifypect ); } 
		this.asynchRepeat( tryToNotifyPECT, done/*, "GUI Driver init"*/ );
	},
	terminate : function() { playEnableChange( false ); },
	//-------
	playSiGMLURL : function() {
	//----------
		var sigmlurl = window.document.forms.spasigmlform.urlText.value;
		parent.spawithguiframe.SPAGUI.playSiGMLURL( sigmlurl );
	},
	playSiGMLText : function() {
	//-----------
		var sigml = window.document.forms.spasigmlform.sigmlText.value;
		parent.spawithguiframe.SPAGUI.playSiGMLText( sigml );
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
	}
};

// Global Play Enable Change handler function.
//---------------------------------
function playEnableChange( enable ) {
//---------------------------------
	document.forms.spasigmlform.bttnPlayURL.disabled = (!enable);
	document.forms.spasigmlform.bttnPlayText.disabled = (!enable);
}


//############  (end)  ############
