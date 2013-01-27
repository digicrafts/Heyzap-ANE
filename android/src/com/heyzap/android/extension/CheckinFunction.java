package com.heyzap.android.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

import android.util.Log;

import com.heyzap.sdk.HeyzapLib;

public class CheckinFunction implements FREFunction
{
	private static String TAG = "CheckinFunction";
	
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		Log.d(TAG, "call");
		
		if (args.length != 1) {
			Log.e(TAG, "Invalid amount of parameters");
			return null;
		}
		
		try {
			String text = args[0].getAsString();
		
			HeyzapLib.checkin(context.getActivity(), text);
		}
		catch (Exception e) {
			Log.e(TAG, "Exception caught " + e.toString());
		}
		
		return null;
	}
}
