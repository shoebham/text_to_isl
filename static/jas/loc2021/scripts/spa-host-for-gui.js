//########  Javascript for SiGML Player Applet (SPA) Host  ########
//
// RE  2009-03

/*
 * An SPA HOST object implements the standard interface used by an SPA proxy
 * (spa-proxy.js) to communicate with its host environment.  Most of the
 * methods in this interface are callout methods, which in this case manipulate
 * the SPA GUI on behalf of the proxy.  There are also methods providing
 * the SPA proxy with access to parameters defined in the host HTML environment.
 *
 * This file defines the following global functions and variables:
 *
 * makeObject( data, PROTO )
 * getSPAGUIHostProto()
 * SPA_GUI_HOST_PROTO
 * getSPAGUIHostHandlerName()
 * ctor_SPAGUIHOST( guihandlerobj, params )
 */

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

// Creates and returns a prototype defining methods for an SPA HOST object
// that is attached to a controlling GUI.
//---------------------------
function getSPAGUIHostProto() {
//---------------------------
	return {
		//########  Message display  ########
		message : function( msg ) { this.GUI.showMessage( msg ); },
		//-----
		detailMessage : function( msg ) { this.GUI.showDetailMessage( msg ); },
		//-----------

		//########  Event notification  ########
		notifyAvatarLoad : function( av ) { this.GUI.avatarLoadDone( av ); },
		//--------------
		notifyAvatarUnload : function() { this.GUI.avatarUnloaded(); },
		//----------------
		notifyFirstFramesGenerated : function() {
		//------------------------
			// (If we're not doing asynchronous "streamed" animation
			// frames generation, this may be premature.)
			this.GUI.setButtonsForAnimating();
		},
		notifyFramesGenerationDone : function( fgok ) {
		//------------------------
			this.GUI.setFramesGenDone( fgok );
		},
		notifyAnimationDone : function() { this.GUI.setAnimationDone(); },
		//-----------------

		//########  HTML Parameter handling  ########
		updateParams : function( newparams ) { this.PARAMS.update( newparams ); }, 
		//-----------------------
		param : function( pname ) { return this.PARAMS.get( pname ); }
		//---
	};
}

// The only instance of getSPAGUIHostProto().
var SPA_GUI_HOST_PROTO = getSPAGUIHostProto();

// Returns the name used by SPA HOST to access its controlling SPA GUI.
//---------------------------------
function getSPAGUIHostHandlerName() { return "GUI"; }
//---------------------------------

// Constructs a new SPA GUI HOST using the given GUI handler and HTML parameters.
//-----------------------------------------------
function ctor_SPAGUIHOST( guihandlerobj, params ) {
//-----------------------------------------------

	var guihostdata = { PARAMS : params };
	// The methods in the GUI Host Prototype use the following property in
	// guihostdata to get access to the supporting (GUI) handler object.
	guihostdata[ getSPAGUIHostHandlerName() ] = guihandlerobj;

	return makeObject( guihostdata, SPA_GUI_HOST_PROTO );
}


//############  (end)  ############
