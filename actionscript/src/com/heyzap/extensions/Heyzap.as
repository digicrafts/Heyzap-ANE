package com.heyzap.extensions
{
	import flash.external.ExtensionContext;
	
	public class Heyzap
	{	
		private var exContext : ExtensionContext;
		
		public function Heyzap()
		{
			exContext = ExtensionContext.createExtensionContext("com.heyzap.extension", "heyzap");
		}
		
		public function load(showCheckin : Boolean, appStoreId : String, appUrl : String) : void
		{
			if (exContext != null)
				exContext.call("load", showCheckin, appStoreId, appUrl);
		}
		
		public function checkin(text : String) : void
		{
			if (exContext != null)
				exContext.call("checkin", text);
		}
		
		public function submitScore(score : String, displayScore : String, levelId : String) : void
		{
            if (exContext != null)
                exContext.call("submitScore",score,displayScore,levelId);
		}
		
		public function showLeaderboards() : void
		{
            if (exContext != null)
                exContext.call("showLeaderboards");
		}

		public function isSupported() : Boolean
		{
			return exContext != null && exContext.call("isSupported");
		}
	}
}
