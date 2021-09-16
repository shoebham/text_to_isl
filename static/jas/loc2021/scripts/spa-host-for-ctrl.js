//########  Javascript for SiGML Player Applet (SPA) Host  ########
//
// RE  2009-03

/*
 * An SPA HOST object implements the standard interface used by an SPA proxy
 * (spa-proxy.js) to communicate with its host environment.  Most of the
 * methods in this interface are callout methods, which in this case manipulate
 * the (GUI-less) SPA control on behalf of the proxy.  There are also methods
 * providing the SPA proxy with access to parameters defined in the host HTML
 * environment.
 *
 * This file defines the following global functions and variables:
 *
 * makeObject( data, PROTO )
 * getSPACTRLHostProto()
 * SPA_CTRL_HOST_PROTO
 * getSPACTRLHostHandlerName()
 * ctor_SPACTRLHOST( ctrlhandlerobj, params )
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
// that is attached to a GUI-less SPA controller.
//----------------------------
function getSPACTRLHostProto() {
//----------------------------

	return {

		message : function( msg ) {},
		//-----
		detailMessage : function( msg ) {},
		//-----------
		notifyAvatarLoad : function( av ) { this.CTRL.avatarLoadDone( av ); },
		//--------------
		notifyAvatarUnload : function() {},
		//----------------
		notifyFirstFramesGenerated : function() {},
		//------------------------
		notifyFramesGenerationDone :
		//------------------------
			function( fgok ) { this.CTRL.fireFramesGenDone( fgok ); },

		notifyAnimationDone : function() { this.CTRL.fireAnimationDone(); },
		//-----------------
		updateParams : function( newparams ) { this.PARAMS.update( newparams ); }, 
		//-----------------------
		param : function( pname ) { return this.PARAMS.get( pname ); }
		//---
	};
}

// The only instance of getSPACTRLHostProto().
var SPA_CTRL_HOST_PROTO = getSPACTRLHostProto();

// Returns the name used by SPA HOST to access its controller.
//----------------------------------
function getSPACTRLHostHandlerName() { return "CTRL"; }
//----------------------------------

//-------------------------------------------------
function ctor_SPACTRLHOST( ctrlhandlerobj, params ) {
//-------------------------------------------------
	
	var ctrlhostdata = { PARAMS : params };
	// The methods in the no-GUI Host Prototype use the following property in
	// ctrlhostdata to get access to the supporting (CTRL) handler object.
	ctrlhostdata[ getSPACTRLHostHandlerName() ] = ctrlhandlerobj;

	return makeObject( ctrlhostdata, SPA_CTRL_HOST_PROTO );
}


//############  (end)  ############
