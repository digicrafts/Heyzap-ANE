package com.heyzap.android.extension;

import java.util.HashMap;
import java.util.Map;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;

public class ExtensionContext extends FREContext
{
	@Override
	public Map<String, FREFunction> getFunctions()
	{
		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		functionMap.put("load", new LoadFunction());
		functionMap.put("checkin", new CheckinFunction());
		functionMap.put("isSupported", new IsSupportedFunction());
		functionMap.put("submitScore", new SubmitScoreFunction());
		functionMap.put("showLeaderboards", new ShowLeaderboardsFunction());
	    return functionMap;
	}

	@Override
	public void dispose()
	{
	}
}
