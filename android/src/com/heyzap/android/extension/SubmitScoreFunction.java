package com.heyzap.android.extension;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

import android.util.Log;

import com.heyzap.sdk.HeyzapLib;

public class SubmitScoreFunction implements FREFunction
{
	private static String TAG = "SubmitScoreFunction";
	
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		Log.d(TAG, "call");
		
		if (args.length != 3) {
			Log.e(TAG, "Invalid amount of parameters");
			return null;
		}
		
		try {
			String score = args[0].getAsString();
			String displayScore = args[1].getAsString();
			String levelId = args[2].getAsString();
			
			HeyzapLib.submitScore(context.getActivity(), score, displayScore, levelId);			
		}
		catch (Exception e) {
			Log.e(TAG, "Exception caught " + e.toString());
		}
		
		return null;
	}
}
