package com.heyzap.android.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

import android.util.Log;

import com.heyzap.sdk.HeyzapLib;

public class IsSupportedFunction implements FREFunction
{
	private static String TAG = "IsSupportedFunction";
	
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		Log.d(TAG, "call");
		
		try {
			return FREObject.newObject(HeyzapLib.isSupported(context.getActivity()));
		}
		catch (Exception e) {
			Log.e(TAG, "Exception caught " + e.toString());
		}
		
		return null;
	}
}
