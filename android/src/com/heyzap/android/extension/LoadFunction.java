package com.heyzap.android.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

import android.util.Log;

import com.heyzap.sdk.HeyzapLib;

public class LoadFunction implements FREFunction
{
	private static String TAG = "LoadFunction";
	
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		Log.d(TAG, "call");
		
//		if (args.length != 1) {
//			Log.e(TAG, "Invalid amount of parameters");
//			return null;
//		}
		
		try {
			Boolean showCheckin = args[0].getAsBool();
		
			// if (showCheckin)
				HeyzapLib.load(context.getActivity(),showCheckin);
			// else
			// 	HeyzapLib.load(context.getActivity(), false);
		}
		catch (Exception e) {
			Log.e(TAG, "Exception caught " + e.toString());
		}
		
		return null;
	}
}
