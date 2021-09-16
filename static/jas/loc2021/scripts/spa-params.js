//########  Javascript for SiGML Player Applet (SPA) HTML Parameters  ########
//
// RE  2009-04

// Creates and returns a prototype defining methods for an SPA
// HTML Parameters object.
function getSPAParamsMethods() {

	return {
		update : function( newdata ) {
		//----
			for (var p in newdata) {
				// hasOwnProperty() check should be redundant;
				if (newdata.hasOwnProperty( p )) {
					this[ p ] = newdata[ p ];
				}
			}
		},
		//-------
		get : function( pname ) { return this[ pname ]; }
		//-
	};
}

// The only instance of getSPAParamsMethods().
var SPA_PARAMS_PROTO = getSPAParamsMethods();

// Constructs a new SPA HTML Parameters object, using the given
// initial data.
// O actual parameters, i.e. data === undefined, is acceptable here.
function ctor_Params( data ) {

	var thedata = data;
	if (thedata === undefined) {
		thedata = {
			extraAvatarsText : "",
			initialAvatar : "anna",
			backgroundRGB : "",
			initialFPSStr : "50",
			spaWidth : "100%",
			spaHeight : "100%"
		};
	}

	return makeObject( thedata, SPA_PARAMS_PROTO );
}


//############  (end)  ############
