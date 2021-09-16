//########  Javascript for the SiGML Player Applet (SPA) GUI  ########
//
// RE  2009-03

/*
 * The SPA control object, conventionally called SPACTRL, defines the behaviour
 * of the HTML/JS controller for a SiGML Player Applet instance with no
 * immediately associated GUI..
 *
 * The SPA control is the top-level JS component for the given SPA instance.
 * As such it contains references both to the associated SPA proxy object
 * (spa-proxy.js) and to the associated SPA host object (spa-host-no-gui.js),
 * which acts as an intermediary between the proxy's event handlers and
 * the SPA control itself.
 *
 * The SPA control is hosted in a frame called spanoguiframe
 * (spa-no-gui-frame.html) which is the parent of the spaframe (spa-frame.html),
 * its only child..
 *
 * This file defines the following global functions and variables:
 *
 * getSPACTRLProto()
 * SPA_CTRL_PROTO
 * ctor_SPACTRL()
 * getSPAHost()
 * setSPA( spa )
 *
 * Internal structure of the GUI-less SPA control frame:
 *
 *  FRAME spactrlframe  src = spa-ctrl-frame.html
 *
 *      spa-params.js
 *          ctor_Params( data )
 *      spa-host-for-ctrl.js
 *          ctor_SPACTRLHOST( ctrlhandlerobj, params )
 *      spa-ctrl-evt-hdlr.js
 *          makeEventHandler( ehframename, asynch )
 *      spa-ctrl.js  [ THE PRESENT FILE ]
 *          ctor_SPACTRL()
 *          SPACTRL
 *          SPACTRL.HOST
 *          getSPAHost()
 *          setSPA( spa )
 *      HTML_PARAMETERS
 *      SPACTRL.setParams( HTML_PARAMETERS )
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
 */

function getSPACTRLProto() {

	return {

		//########  Driving an external event handler  ########
		setEventHandler : function( spactrleh ) {
		//-------------
			this.EVENT_HANDLER = spactrleh;
			// If the handler is set late, then the initial avatar load may
			// already have happened, in which case we must simulate its
			// occurence now.
			this.checkFireAvatarLoaded();
		},
		checkFireAvatarLoaded : function() {
		//--------------
			if (this.avatar) {
				var handler = this.EVENT_HANDLER;
				if (handler) { handler.avatarLoadDone( this.avatar ); }
			}
		},
		fireFramesGenDone : function( fgok ) {
		//---------------
			var handler = this.EVENT_HANDLER;
			if (handler) { handler.framesGenDone( fgok ); }
		},
		fireAnimationDone : function() {
		//---------------
			var handler = this.EVENT_HANDLER;
			if (handler) { handler.animationDone(); }
		},
		
		//########  Environment Parameter access  ########
		setParams : function( htmlparams ) {
		//-------
			this.SPAHOST.updateParams( htmlparams );
		},

		//########  Event handling  ########
		avatarLoadDone : function( av ) {
		//------------
			// (av is null in case of load failure.)
			this.avatar = av;
			this.checkFireAvatarLoaded();
		},

		//########  HTML SiGML playing button handlers  ########
		playSiGMLURL : function( url ) { return this.SPA.playSiGMLURL( url ); },
		//----------
		playSiGMLText : function( sigml ) { return this.SPA.playSiGMLText( sigml ); },
		//-----------
		stopPlayingSiGML : function() { return this.SPA.stopPlayer(); },
		//--------------
		startPlaySiGMLPiped : function() { return this.SPA.startPlaySiGMLPiped(); },
		//-----------------
		appendToSiGMLPipe : function( fragment ) {
		//---------------
			return this.SPA.appendToSiGMLPipe( fragment );
		},
		closeSiGMLPipe : function() { return this.SPA.closeSiGMLPipe(); }
		//------------
	};
}

// The only instance of getSPACTRLProto();
var SPA_CTRL_PROTO = getSPACTRLProto();

// Constructs a new SPAGUI and its subordinate host object.
//---------------------
function ctor_SPACTRL() {
//---------------------

	var scdata = {
		SPA             : null,  // JS proxy for SiGML Player Applet;
		SPAHOST         : null,  // Callback interface for SPA proxy;
		EVENT_HANDLER   : null,
		avatar          : null
	};

	var SPACTRL = makeObject( scdata, SPA_CTRL_PROTO );
	// Host interface object has a reference back to SPACTRL.
	SPACTRL.SPAHOST = new ctor_SPACTRLHOST( SPACTRL, new ctor_Params() );

	return SPACTRL;
}

// The global SPA CTRL instance, the route to all SPA data.
var SPACTRL = new ctor_SPACTRL();
// Sometime after the above line has created the SPACTRL object, the SPA
// subframe will create the SPA proxy object.  The following pair of functions
// allow the SPA proxy's initialisation sequence to obtain its HOST from us,
// and to provide us with a reference back to the SPA proxy itself, which is
// needed by SPACTRL
function getSPAHost()  { return SPACTRL.SPAHOST; }
function setSPA( spa ) { SPACTRL.SPA = spa; }

// Attaches a external event handler for the given target frame to the
// SPAGUI, asynchronous or not as stipulated by the second parameter.
//---------------------------------------------
function setEventHandler( ehframename, asynch ) {
//---------------------------------------------
	SPACTRL.setEventHandler( makeEventHandler( ehframename, asynch ) );
}


//############  (end)  ############
