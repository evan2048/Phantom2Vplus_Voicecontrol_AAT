package com.evan2048.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;

public class SpeechEngineApkInstaller {
	
	private static String sdcardPath=Environment.getExternalStorageDirectory().getPath();
	
	//从assets中安装语音识别服务
	public static boolean installFromAssets(Context context)
	{
		try
		{
			AssetManager assets = context.getAssets();
			InputStream SpeechServiceStream = assets.open("SpeechService.apk");
			if (SpeechServiceStream == null)
			{
				return false;
			}
			File SpeechServiceDirectory = new File(sdcardPath);
			if (!SpeechServiceDirectory.exists())
			{
				SpeechServiceDirectory.mkdirs();
			}
			//copy SpeechServive.apk
			String SpeechServicePath=sdcardPath+"/SpeechService.apk";
			File SpeechServiceFile = new File(SpeechServicePath);
			if (!writeStreamToFile(SpeechServiceStream, SpeechServiceFile))
			{
				return false;
			}
			//install SpeechService.apk
			installApk(context, SpeechServicePath);
		}
		catch (IOException e)
		{
			e.printStackTrace();			
			return false;
		}
		return true;
	}

	//打开语音服务组件下载页面
	public static void openDownloadWeb(Context context, String url)
	{
		Uri uri = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(it);
	}

	//从输入流中写数据到一个文件中
	private static boolean writeStreamToFile(InputStream stream, File file)
	{
		OutputStream output = null;
		try
		{
			output = new FileOutputStream(file);
			final byte[] buffer = new byte[1024];
			int read;
			while((read = stream.read(buffer)) != -1)
			{
				output.write(buffer, 0, read);
			}
			output.flush();
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				output.close();
				stream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	//根据apk路径安装apk包
	private static void installApk(Context context, String apkPath)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(apkPath)),"application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
}
