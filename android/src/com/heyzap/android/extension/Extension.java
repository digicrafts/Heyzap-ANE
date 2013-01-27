package com.heyzap.android.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class Extension implements FREExtension
{
	public static FREContext context;
	
	@Override
	public FREContext createContext(String contextType)
	{
		if (context == null)
			context = new ExtensionContext();		
		return context;
	}
	
	@Override
	public void dispose()
	{
	}

	@Override
	public void initialize()
	{
	}
}
