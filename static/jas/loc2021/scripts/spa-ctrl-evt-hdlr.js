// Interface to external event handler methods for a GUI-less SPA control
// frame.
//
// 2009-04-11

// Interface to external handler frame, may or may not be asynchronous.
function makeEventHandler( ehframename, asynch ) {
	return {
		EH_FRAME_NAME : ehframename,
		DO_ASYNCH : (asynch === undefined ? false : asynch),
		// Attempts to get the given contentframe function, returns null in case
		// of failure.
		getHandlerFunction : function( fname ) {
			var fun = null;
			var frame = parent[ this.EH_FRAME_NAME ]
			var tmpfun = frame[ fname ];
			if (tmpfun !== null && tmpfun !== undefined) { fun = tmpfun; }
			return fun;
		},
		// Invokes the given function asynchronously or synchronously.
		invoke : function( fun ) {
			if (this.DO_ASYNCH) { setTimeout( fun, 0); } else { fun(); }
		},
		// Each of these three SPA event handler methods delegates
		// (synchronously or asynchronously) to the corresponding function
		// in the handler frame, if available (and does nothing if not).
		// Delegating asynchronously prevents the SPA from locking up if/when
		// the contentframe method attempts to make a new SPA call.
		avatarLoadDone : function( av ) {
			var outerthis = this;
			this.invoke( function() {
				var aldfun = outerthis.getHandlerFunction( "avatarLoadDone" );
				if (aldfun !== null) { aldfun( av ); }
			});
		},
		framesGenDone : function( ok ) {
			var outerthis = this;
			this.invoke( function() {
				var fgdfun = outerthis.getHandlerFunction( "framesGenDone" );
				if (fgdfun != null) { fgdfun( ok ); }
			});
		},
		animationDone : function() {
			var outerthis = this;
			this.invoke( function() {
				var adfun = outerthis.getHandlerFunction( "animationDone" );
				if (adfun != null) { adfun(); }
			});
		}
	};
}


//#### (the end) ####
