package com.evan2048.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.evan2048.phantom_voicecontrol.R;

public class SharedPreferenceSetter {
	
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

	//初始化配置信息
	public void initSharedPreferences(Context context)
	{
		sharedPreferences=context.getSharedPreferences("voiceControlConfig", Activity.MODE_PRIVATE);
		sharedPreferencesEditor=sharedPreferences.edit();
		//first time?
		if(sharedPreferences.contains("imExist")==false)
		{
			sharedPreferencesEditor.putBoolean("imExist", true);
			sharedPreferencesEditor.putString(context.getString(R.string.cameraPreviewResolustionTypeString), context.getString(R.string.resolution_type_320x240_15fpsString));
			sharedPreferencesEditor.putBoolean(context.getString(R.string.fpvModeString), false);
			sharedPreferencesEditor.putInt(context.getString(R.string.speechConfidenceBoundaryString), 50);
			sharedPreferencesEditor.putBoolean(context.getString(R.string.threeDGlassModeString), false);
			sharedPreferencesEditor.commit();
		}
	}
	
	//get CameraPreviewResolustionType
	public String getCameraPreviewResolustionType(Context context)
	{
		return sharedPreferences.getString(context.getString(R.string.cameraPreviewResolustionTypeString), "");
	}
	//get FpvMode
	public boolean getFpvMode(Context context)
	{
		return sharedPreferences.getBoolean(context.getString(R.string.fpvModeString), false);
	}
	//get SpeechConfidenceBoundary
	public int getSpeechConfidenceBoundary(Context context)
	{
		return sharedPreferences.getInt(context.getString(R.string.speechConfidenceBoundaryString), 0);
	}
	//get threeDGlassMode
	public boolean getThreeDGlassMode(Context context)
	{
		return sharedPreferences.getBoolean(context.getString(R.string.threeDGlassModeString), false);
	}
	
	//set CameraPreviewResolustionType
	public void setCameraPreviewResolustionType(String cameraPreviewResolustionType, Context context)
	{
		sharedPreferencesEditor.putString(context.getString(R.string.cameraPreviewResolustionTypeString), cameraPreviewResolustionType);
		sharedPreferencesEditor.commit();
	}
	//set FpvMode
	public void setFpvMode(boolean fpvMode, Context context)
	{
		sharedPreferencesEditor.putBoolean(context.getString(R.string.fpvModeString), fpvMode);
		sharedPreferencesEditor.commit();
	}
	//set SpeechConfidenceBoundary
	public void setSpeechConfidenceBoundary(int speechConfidenceBoundary, Context context)
	{
		sharedPreferencesEditor.putInt(context.getString(R.string.speechConfidenceBoundaryString), speechConfidenceBoundary);
		sharedPreferencesEditor.commit();
	}
	//set threeDGlassMode
	public void setThreeDGlassMode(boolean threeDGlassMode, Context context)
	{
		sharedPreferencesEditor.putBoolean(context.getString(R.string.threeDGlassModeString), threeDGlassMode);
		sharedPreferencesEditor.commit();
	}
	
}
