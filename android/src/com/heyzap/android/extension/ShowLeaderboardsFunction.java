package com.heyzap.android.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

import android.util.Log;

import com.heyzap.sdk.HeyzapLib;

public class ShowLeaderboardsFunction implements FREFunction
{
	private static String TAG = "ShowLeaderboardsFunction";
	
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		Log.d(TAG, "call");
		
		try {
			HeyzapLib.showLeaderboards(context.getActivity());
		}
		catch (Exception e) {
			Log.e(TAG, "Exception caught " + e.toString());
		}
		
		return null;
	}
}
