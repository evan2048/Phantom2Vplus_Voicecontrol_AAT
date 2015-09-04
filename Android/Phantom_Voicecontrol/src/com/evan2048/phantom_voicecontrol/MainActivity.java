package com.evan2048.phantom_voicecontrol;

/*
 * develop by evan2048
 * email:gch163mail@163.com
 * website:lovecoding.sinaapp.com
 */

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.evan2048.util.ChineseNumberParser;
import com.evan2048.util.GPSdistanceCalculator;
import com.evan2048.util.RecognizeResultXmlParser;
import com.evan2048.util.SharedPreferenceSetter;
import com.evan2048.util.SpeechEngineApkInstaller;
import com.evan2048.util.WifiConnectionChecker;
import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.GrammarListener;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.RecognizerListener;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechRecognizer;
import com.iflytek.speech.SpeechSynthesizer;
import com.iflytek.speech.SpeechUtility;
import com.iflytek.speech.SynthesizerListener;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.Battery.DJIBatteryProperty;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraPreviewResolustionType;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraVisionType;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.Gimbal.DJIGimbalAttitude;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef.DJIMCUErrorType;
import dji.sdk.interfaces.DJIBattryUpdateInfoCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.interfaces.DJIGimbalErrorCallBack;
import dji.sdk.interfaces.DJIGimbalUpdateAttitudeCallBack;
import dji.sdk.interfaces.DJIMcuErrorCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class MainActivity extends BluetoothBaseLibrary implements OnClickListener {
	
	private static final String TAG="MainActivity";
	private final int SETTINGACTIVITY_REQUESTCODE=2048;
	//Voice
	private final String VOICE_API_KEY="545b1820";
    private final String SPEECH_ENGINE_DOWNLOAD_URL="http://open.voicecloud.cn/speechservice";
    private boolean isSpeechServiceInstall=false;
    private boolean isSpeechServiceRunning=false;  //语音引擎运行标志
    private int speechCammandConfidenceBoundary=50;  //处理语音命令时置信度的阈值(0-100)
	private SpeechRecognizer speechRecognizer = null;  //语音识别对象
	private SpeechSynthesizer speechSynthesizer = null;  //语音合成对象
	//DJI
	//private final String DJI_API_KEY="21e74dea02834c1e9f75cf8d";  //visit dev.dji.com
    private final int LOWPOWER_WARNING_PERCENT=30;  //低电量报警阈值
	private boolean isCameraConnected = false;  //相机连接状态标志
    private boolean isRecordingVideo=false;  //正在录像标志
    private int currentGimbalPitchAngle=0;  //云台当前俯仰角度
    private int phantom_gimbal_min_angle=0;
    private int phantom_gimbal_max_angle=1000;  //在initCamera()中获取
    private boolean isLowPowerWarning=false;  //显示低电量报警标志
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;
    private DJIBattryUpdateInfoCallBack mBattryUpdateInfoCallBack = null;
    private DJIGimbalUpdateAttitudeCallBack mGimbalUpdateAttitudeCallBack = null;
    private DJIGimbalErrorCallBack mGimbalErrorCallBack = null;
    private DJIMcuUpdateStateCallBack mMcuUpdateStateCallBack = null;
    private DJIMcuErrorCallBack mMcuErrorCallBack = null;
    private String wifiSSID="";  //连接wifi ssid
    private int remainPowerPercent=0;  //电量
    private int satelliteCount=0;  //卫星数
    private double distance=0.0;  //距离
    private double altitude=0.0;  //高度
    private double speed=0.0;  //速度
    private double homeLocationLatitude=0.0,homeLocationLongitude=0.0,phantomLocationLatitude=0.0,phantomLocationLongitude=0.0;
    //config
    private SharedPreferenceSetter sharedPreferenceSetter=new SharedPreferenceSetter();
    private boolean isThreeDGlassMode=false;  //3D眼镜模式
    //Bluetooth AAT
    private boolean isBlunoConnect=false;
    //private int yawAngle=0;  //2015.3.16 改进版在天线自动追踪系统中计算
    //private int pitchAngle=0;
    
	//打印调试信息
	private void showLOG(String str)
	{
		Log.e(TAG, str);
	}
	
	//show Toast
	private void showToast(String str)
	{
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
	
	//UI界面初始化
    private DjiGLSurfaceView mDjiGLSurfaceView;  //实时预览画面
	private Button speechButton;  //语音按钮
	private Button settingButton;  //设置按钮
	private Button wifiButton;  //wifi按钮
	private Button bluetoothButton;  //蓝牙连接按钮
	private TextView speedTextView;  //速度
	private TextView altitudeTextView;  //高度
	private TextView distanceTextView;  //距离
	private TextView satelliteCountTextView;  //卫星数
	private TextView remainPowerPercentTextView;  //电量
	private Button gimbalPitchPlusButton;  //云台俯仰往上按钮
	private Button gimbalPitchMinusButton;  //云台俯仰往下按钮
	private Button takePhotoButton;  //拍照按钮
	private Button recordingVideoButton;  //录像按钮
	private LinearLayout centerLinearLayout;
	private TextView connectedWifiSSIDTextView;  //当前连接Wifi的SSID
	private Button startButton;  //开始按钮
	private TextView speechCommandTextView;  //语音命令
	private TextView commonMessageTextView;  //一般消息
	private void initUIControls()
	{
		//find view
    	mDjiGLSurfaceView=(DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView);
		speechButton=(Button)findViewById(R.id.speechButton);
		settingButton=(Button)findViewById(R.id.settingButton);
		wifiButton=(Button)findViewById(R.id.wifiButton);
		bluetoothButton=(Button)findViewById(R.id.bluetoothButton);
		speedTextView=(TextView)findViewById(R.id.speedTextView);
		altitudeTextView=(TextView)findViewById(R.id.altitudeTextView);
		distanceTextView=(TextView)findViewById(R.id.distanceTextView);
		satelliteCountTextView=(TextView)findViewById(R.id.satelliteCountTextView);
		remainPowerPercentTextView=(TextView)findViewById(R.id.remainPowerPercentTextView);
		gimbalPitchPlusButton=(Button)findViewById(R.id.gimbalPitchPlusButton);
		gimbalPitchMinusButton=(Button)findViewById(R.id.gimbalPitchMinusButton);
		takePhotoButton=(Button)findViewById(R.id.takePhotoButton);
		recordingVideoButton=(Button)findViewById(R.id.recordingVideoButton);
		centerLinearLayout=(LinearLayout)findViewById(R.id.centerLinearLayout);
		connectedWifiSSIDTextView=(TextView)findViewById(R.id.connectedWifiSSIDTextView);
		startButton=(Button)findViewById(R.id.startButton);
		speechCommandTextView=(TextView)findViewById(R.id.speechCommandTextView);
		commonMessageTextView=(TextView)findViewById(R.id.commonMessageTextView);
		//set OnClickListener
		speechButton.setOnClickListener(this);
		settingButton.setOnClickListener(this);
		wifiButton.setOnClickListener(this);
		bluetoothButton.setOnClickListener(this);
		takePhotoButton.setOnClickListener(this);
		recordingVideoButton.setOnClickListener(this);
		startButton.setOnClickListener(this);
		//set OnTouchListener
		gimbalPitchPlusButton.setOnTouchListener(new Gimbal_Pitch_Plus_Listener());
		gimbalPitchMinusButton.setOnTouchListener(new Gimbal_Pitch_Minus_Listener());
		//自定义控件行为
		connectedWifiSSIDTextView.setText(getText(R.string.wifiScanning));
		commonMessageTextView.setText("");
		speechCommandTextView.setText("");
		startButton.setClickable(false);
        takePhotoButton.setClickable(false);
        recordingVideoButton.setClickable(false);
        speechButton.setClickable(false);
        isThreeDGlassMode=sharedPreferenceSetter.getThreeDGlassMode(this);
        setThreeDGlassMode(isThreeDGlassMode);
	}
	
    //设置3D眼镜模式
	private void setThreeDGlassMode(boolean threeDGlassMode)
	{
		if(threeDGlassMode==true)
		{
	        //获取屏幕分辨率
	        DisplayMetrics displayMetrics=new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	        int displayWidth=displayMetrics.widthPixels;
	        //设置mDjiGLSurfaceView
	        RelativeLayout.LayoutParams mDjiGLSurfaceViewLayoutParams=(RelativeLayout.LayoutParams)mDjiGLSurfaceView.getLayoutParams();
	        mDjiGLSurfaceViewLayoutParams.width=displayWidth/2;  //left half screen
	        mDjiGLSurfaceView.setLayoutParams(mDjiGLSurfaceViewLayoutParams);
	        //设置云台俯仰按钮
	        gimbalPitchPlusButton.setVisibility(View.INVISIBLE);
	        gimbalPitchMinusButton.setVisibility(View.INVISIBLE);
	        //设置消息提示文字显示位置
	        RelativeLayout.LayoutParams commonMessageTextViewLayoutParams=(RelativeLayout.LayoutParams)commonMessageTextView.getLayoutParams();
	        commonMessageTextViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,0);  //0 means remove,we can't use removeRule() in API 14
	        commonMessageTextViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        commonMessageTextView.setLayoutParams(commonMessageTextViewLayoutParams);
	        RelativeLayout.LayoutParams speechCommandTextViewLayoutParams=(RelativeLayout.LayoutParams)speechCommandTextView.getLayoutParams();
	        speechCommandTextViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,0);
	        speechCommandTextViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        speechCommandTextView.setLayoutParams(speechCommandTextViewLayoutParams);
		}
		else
		{
	        //获取屏幕分辨率
	        DisplayMetrics displayMetrics=new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	        int displayWidth=displayMetrics.widthPixels;
	        //设置mDjiGLSurfaceView
	        RelativeLayout.LayoutParams mDjiGLSurfaceViewLayoutParams=(RelativeLayout.LayoutParams)mDjiGLSurfaceView.getLayoutParams();
	        mDjiGLSurfaceViewLayoutParams.width=displayWidth;
	        mDjiGLSurfaceView.setLayoutParams(mDjiGLSurfaceViewLayoutParams);
	        //设置云台俯仰按钮
	        gimbalPitchPlusButton.setVisibility(View.VISIBLE);
	        gimbalPitchMinusButton.setVisibility(View.VISIBLE);
	        //设置消息提示文字显示位置
	        RelativeLayout.LayoutParams commonMessageTextViewLayoutParams=(RelativeLayout.LayoutParams)commonMessageTextView.getLayoutParams();
	        commonMessageTextViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
	        commonMessageTextViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	        commonMessageTextView.setLayoutParams(commonMessageTextViewLayoutParams);
	        RelativeLayout.LayoutParams speechCommandTextViewLayoutParams=(RelativeLayout.LayoutParams)speechCommandTextView.getLayoutParams();
	        speechCommandTextViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
	        speechCommandTextViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	        speechCommandTextView.setLayoutParams(speechCommandTextViewLayoutParams);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		//语音按钮
		case R.id.speechButton:
		{
			if(isSpeechServiceRunning==false)
			{
				startRecognition();
			}
			else
			{
				stopRecognition();
			}
			break;
		}
		//设置按钮
		case R.id.settingButton:
		{
			settingButton.setBackgroundResource(R.drawable.setting_press);
			launchSettingActivity();
			break;
		}
		//wifi按钮
		case R.id.wifiButton:
		{
			//do nothing,目前只显示wifi信号强度
			break;
		}
		//蓝牙连接按钮
		case R.id.bluetoothButton:
		{
			buttonScanOnClickProcess();  //Alert Dialog for selecting the BLE device
			break;
		}
		//拍照按钮
		case R.id.takePhotoButton:
		{
			takePhotoButton.setBackgroundResource(R.drawable.camera_green);
            DJIDrone.getDjiCamera().startTakePhoto(new DJIExecuteResultCallback(){
                @Override
                public void onResult(DJIError mErr)
                {
                	if(mErr.errorCode==DJIError.RESULT_OK)
                	{
                		//showLOG("take photo success");
                		showCommonMessage(getString(R.string.takePhoto)+getString(R.string.success));
                	}
                	else
                	{
						//showLOG("take photo failed");
                		showCommonMessage(getString(R.string.takePhoto)+getString(R.string.failed));
					}
        			runOnUiThread(new Runnable() {				
        				@Override
        				public void run() {
        					// TODO Auto-generated method stub
        					takePhotoButton.setBackgroundResource(R.drawable.camera_gray);
        				}
        			});
                }               
            });
			break;
		}
		//录像按钮
		case R.id.recordingVideoButton:
		{
			//开始录像
			if(isRecordingVideo==false)
			{
                DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback(){
                    @Override
                    public void onResult(DJIError mErr)
                    {
                    	if(mErr.errorCode==DJIError.RESULT_OK)
                    	{
                    		//showLOG("start recording video success");
                    		isRecordingVideo=true;
            				runOnUiThread(new Runnable() {            					
            					@Override
            					public void run() {
            						// TODO Auto-generated method stub
            						recordingVideoButton.setBackgroundResource(R.drawable.video_green);
            					}
            				});
                    		showCommonMessage(getString(R.string.startRecordingVideo));
                    	}
                    	else
                    	{
    						//showLOG("start recording video failed");
                    		showCommonMessage(getString(R.string.startRecordingVideo)+getString(R.string.failed));
    					}
                    }
                    
                });
			}
			//停止录像
			else
			{
                DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback(){
                    @Override
                    public void onResult(DJIError mErr)
                    {
                    	if(mErr.errorCode==DJIError.RESULT_OK)
                    	{
                    		//showLOG("stop recording video success");
            				isRecordingVideo=false;
            				runOnUiThread(new Runnable() {          					
            					@Override
            					public void run() {
            						// TODO Auto-generated method stub
            						recordingVideoButton.setBackgroundResource(R.drawable.video_gray);
            					}
            				});
                    		showCommonMessage(getString(R.string.stopRecordingVideo));
                    	}
                    	else
                    	{
    						//showLOG("start recording video failed");
                    		showCommonMessage(getString(R.string.stopRecordingVideo)+getString(R.string.failed));
    					}
                                               
                    }
                    
                });
			}
			break;
		}
		//开始按钮
		case R.id.startButton:
		{
	        initCamera();
	        startRecognition();
	        //隐藏wifi状态标签和start按钮
	        centerLinearLayout.setVisibility(View.INVISIBLE);
	        takePhotoButton.setClickable(true);
	        recordingVideoButton.setClickable(true);
			break;
		}
		default:
		{
			break;
		}
		}
	}
	//云台Pitch上扬与下移
    private boolean mIsPitchUp = false;
    private boolean mIsPitchDown = false;
    class Gimbal_Pitch_Plus_Listener implements OnClickListener, OnTouchListener {
    	
        @Override
        public void onClick(View view) {
        	//showLOG("gimbalPitchPlusButton onClick");
        }
        @SuppressLint("ClickableViewAccessibility") @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) 
            {
                mIsPitchUp = true;
                //设置按钮UI效果
                gimbalPitchPlusButton.setBackgroundResource(R.drawable.array_up_press_hole);
                new Thread()
                {
                    public void run()
                    {
                    	//速度模式
                    	DJIGimbalRotation mPitch = null;
                        if(DJIDrone.getDjiCamera().getVisionType() == CameraVisionType.Camera_Type_Plus){
                        	mPitch = new DJIGimbalRotation(true,true,false, 150);
                        }
                        else{
                        	mPitch = new DJIGimbalRotation(true,true,false, 20);
                        }
                        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);                       
                        while(mIsPitchUp)
                        {                           
                            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);                       
                            try 
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
                    }
                }.start();           
            } else if (event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL) 
            {           
                mIsPitchUp = false;
                //设置按钮UI效果
				gimbalPitchPlusButton.setBackgroundResource(R.drawable.array_up_hole);
            }
            return false;
        }
    };
    class Gimbal_Pitch_Minus_Listener implements OnClickListener, OnTouchListener {
    	
        @Override
        public void onClick(View view) {
        	//showLOG("gimbalPitchMinusButton onClick");
        } 
        @SuppressLint("ClickableViewAccessibility") @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) 
            {
                mIsPitchDown = true;
                //设置按钮UI效果
                gimbalPitchMinusButton.setBackgroundResource(R.drawable.array_down_press_hole);
                new Thread()
                {
                    public void run()
                    {
                        //速度模式
                    	DJIGimbalRotation mPitch = null;
                        if(DJIDrone.getDjiCamera().getVisionType() == CameraVisionType.Camera_Type_Plus)
                        {
                        	mPitch = new DJIGimbalRotation(true, false,false, 150); 	
                        }else
                        {
                        	mPitch = new DJIGimbalRotation(true, false,false, 20); 
                        }                       
                        DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false,false, 0);                   	
                        while(mIsPitchDown)
                        {             
                            DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);                       
                            try 
                            {
                                Thread.sleep(50);
                            } catch (InterruptedException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop,null,null);
                    }
                }.start();               
            } else if (event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL) 
            {           
                mIsPitchDown = false;
                //设置按钮UI效果
                gimbalPitchMinusButton.setBackgroundResource(R.drawable.array_down_hole);
            }
            return false;
        }
    };
    
    //处理语音命令
    private void executeSpeechCommand(String cammand)
    {
    	if(cammand.equals("拍照"))
    	{
            DJIDrone.getDjiCamera().startTakePhoto(new DJIExecuteResultCallback(){
                @Override
                public void onResult(DJIError mErr)
                {
                	if(mErr.errorCode==DJIError.RESULT_OK)
                	{
                		showLOG("voice take photo success");
                		showSpeechCammandMessage(getString(R.string.takePhoto)+getString(R.string.success));
                	}
                	else
                	{
						showLOG("voice take photo failed");
                		showSpeechCammandMessage(getString(R.string.takePhoto)+getString(R.string.failed));
					}
                }               
            });
    	}
    	else if(cammand.equals("开始录像"))
    	{
            DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback(){
                @Override
                public void onResult(DJIError mErr)
                {
                	if(mErr.errorCode==DJIError.RESULT_OK)
                	{
                		showLOG("voice start recording video success");
                		isRecordingVideo=true;
        				runOnUiThread(new Runnable() {            					
        					@Override
        					public void run() {
        						// TODO Auto-generated method stub
        						recordingVideoButton.setBackgroundResource(R.drawable.video_green);
        					}
        				});
                		showSpeechCammandMessage(getString(R.string.startRecordingVideo));
                	}
                	else
                	{
						showLOG("voice start recording video failed");
                		showSpeechCammandMessage(getString(R.string.startRecordingVideo)+getString(R.string.failed));
					}
                }
                
            });
    	}
    	else if(cammand.equals("停止录像"))
    	{
            DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback(){
                @Override
                public void onResult(DJIError mErr)
                {
                	if(mErr.errorCode==DJIError.RESULT_OK)
                	{
                		showLOG("voice stop recording video success");
                		isRecordingVideo=false;
        				runOnUiThread(new Runnable() {          					
        					@Override
        					public void run() {
        						// TODO Auto-generated method stub
        						recordingVideoButton.setBackgroundResource(R.drawable.video_gray);
        					}
        				});
        				showSpeechCammandMessage(getString(R.string.stopRecordingVideo));
                	}
                	else
                	{
						showLOG("start recording video failed");
                		showSpeechCammandMessage(getString(R.string.stopRecordingVideo)+getString(R.string.failed));
					}
                                           
                }
                
            });
    	}
    	else if(cammand.equals("镜头平视"))
    	{
    		setGimbalPitchAngle(phantom_gimbal_min_angle);
    		showSpeechCammandMessage(getString(R.string.gimbalAngleHorizontal));
    	}
    	else if(cammand.equals("镜头俯视"))
    	{
    		setGimbalPitchAngle(phantom_gimbal_max_angle);
    		showSpeechCammandMessage(getString(R.string.gimbalAngleVertical));
    	}
    	else if(cammand.equals("镜头上移"))
    	{
    		int angle=currentGimbalPitchAngle-200;
    		setGimbalPitchAngle(angle);
    		showSpeechCammandMessage(getString(R.string.gimbalAngleUp));
    	}
    	else if(cammand.equals("镜头下移"))
    	{
    		int angle=currentGimbalPitchAngle+200;
    		setGimbalPitchAngle(angle);
    		showSpeechCammandMessage(getString(R.string.gimbalAngleDown));
    	}
    	else if(cammand.equals("电池电量"))
    	{
    		//should be "电池电量xx%"
    		startSynthesize(getString(R.string.battery)+getString(R.string.speechPause)+remainPowerPercent+"%");
    		showSpeechCammandMessage(getString(R.string.battery)+remainPowerPercent+"%");
    	}
    	else if(cammand.startsWith("镜头角度"))
    	{
    		String cammandWithoutUnit=cammand;
    		//如果带有单位度，先丢弃
    		if(cammand.endsWith("度"))
    		{
    			cammandWithoutUnit=cammand.substring(0, cammand.length()-1);
    		}
    		//提取出“十”，“二十”之类的中文
    		String chineseNumberAngle=cammandWithoutUnit.substring(4);
    		int angle=ChineseNumberParser.toIntNumber(chineseNumberAngle);
    		//将角度转换为云台对应的值(0-90转换为0-1000)
    		int gimbalAngle=(int)((angle/90.0)*phantom_gimbal_max_angle);
    		setGimbalPitchAngle(gimbalAngle);
    		showSpeechCammandMessage(getString(R.string.gimbalAngle)+angle+getString(R.string.gimbalAngleUnit));
    	}
    }
    
	//显示一般信息2s
	private Timer commonMessageTimer = new Timer();
	class commonMessageCleanTask extends TimerTask
	{
		@Override
		public void run()
		{
			runOnUiThread(new Runnable() {
				public void run() {
					commonMessageTextView.setText("");
				}
			});
		}
	}
    private void showCommonMessage(final String message)
    {
    	runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(message.equals(commonMessageTextView.getText()))
				{
					//过滤重复信息
					return;
				}
				commonMessageTextView.setText(message);
				commonMessageTimer.schedule(new commonMessageCleanTask(), 2000);
			}
		});
    }
    
	//显示语音命令2s
	private Timer speechCommandMessageTimer = new Timer();
	class speechCommandMessageCleanTask extends TimerTask
	{
		@Override
		public void run()
		{
			runOnUiThread(new Runnable() {
				public void run() {
					speechCommandTextView.setText("");
				}
			});
		}
	}
    private void showSpeechCammandMessage(String message)
    {
    	final String showMessage=getString(R.string.voice)+message;
    	runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(showMessage.equals(speechCommandTextView.getText()))
				{
					//过滤重复信息
					return;
				}
				speechCommandTextView.setText(showMessage);
				speechCommandMessageTimer.schedule(new speechCommandMessageCleanTask(), 2000);
			}
		});
    }
    
	//打开设置界面
	private void launchSettingActivity()
	{
		Intent intent=new Intent(MainActivity.this, SettingActivity.class);
		startActivityForResult(intent, SETTINGACTIVITY_REQUESTCODE);
	}
	
	//初始化语音识别监听器
	private InitListener initRecognizerListener=new InitListener() {
		@Override
		public void onInit(ISpeechModule module, int code) {
			// TODO Auto-generated method stub
			if(code==ErrorCode.SUCCESS)
			{
				showLOG("initRecognizerListener success");
			}
			else
			{
				showLOG("initRecognizerListener failed");
			}
		}
	};
	
	//初始化语音合成监听器
    private InitListener initSynthesizerListener = new InitListener() {
		@Override
		public void onInit(ISpeechModule module, int code) {
        	if (code == ErrorCode.SUCCESS)
        	{
        		showLOG("initSynthesizerListener success");
        	}
        	else
        	{
        		showLOG("initSynthesizerListener failed");
			}
		}
    };
	
	//语法构建监听器
	private GrammarListener grammarListener = new GrammarListener.Stub()
	{	
		@Override
		public void onBuildFinish(String grammarId, int errorCode) throws RemoteException
		{
			
		}
	};
	
	//初始化语音识别对象
	private void initRecognizer()
	{
		//初始化识别对象
		speechRecognizer=new SpeechRecognizer(this, initRecognizerListener);
		//设置识别参数
		speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE,"local");
		//VAD_BOS前端点超时：可选范围：1000-10000(单位ms)；默认值：短信转写5000，其他4000
		//VAD_EOS后端点超时：可选范围：0-10000(单位ms)；默认值：短信转写1800，其他700
		speechRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");
		speechRecognizer.setParameter(SpeechConstant.VAD_EOS, "700");
	}
	
	//初始化语音合成对象
	private void initSynthesizer()
	{
		//初始化语音合成对象
		speechSynthesizer=new SpeechSynthesizer(this, initSynthesizerListener);
		//设置合成参数
		speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE,SpeechSynthesizer.TTS_ENGINE_TYPE_LOCAL);  //合成引擎
		speechSynthesizer.setParameter(SpeechSynthesizer.VOICE_NAME,SpeechSynthesizer.LOCAL_TTS_ROLE_XIAOYAN);  //发音人
		speechSynthesizer.setParameter(SpeechSynthesizer.SPEED, "50");  //语速
		speechSynthesizer.setParameter(SpeechSynthesizer.PITCH, "50");  //音调
	}
	
	//读取语法文件
	private String readGrammarFile(String file,String code)
	{
		int len = 0;
		byte []buf = null;
		String grammar = "";
		try
		{
			InputStream in = getAssets().open(file);			
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			grammar = new String(buf,code);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return grammar;
	}
	
	//构建本地语法
	private boolean buildLocalGrammar()
	{
		//取得语法内容:bnf文件存放在assets目录下(本地语法使用bnf格式，在线语法使用abnf格式)
		String grammarContent=readGrammarFile("localGrammar.bnf", "utf-8");
		speechRecognizer.setParameter(SpeechRecognizer.GRAMMAR_ENCODEING, "utf-8");
		//构建语法
		int buildResult=speechRecognizer.buildGrammar("bnf", grammarContent, grammarListener);
		if(buildResult==ErrorCode.SUCCESS)
		{
			showLOG("buildLocalGrammar success");
			//设置语音识别语法
			speechRecognizer.setParameter(SpeechConstant.PARAMS, "local_grammar=localGrammar,mixed_threshold=40");
			return true;
		}
		else
		{
			showLOG("buildLocalGrammar failed");
			return false;
		}
	}
	
	//开始聆听
	private boolean startRecognition()
	{
		if(isSpeechServiceInstall==false)
		{
			showToast(getString(R.string.speechServiceNotInstall));
			return false;
		}
		if(buildLocalGrammar()==true)
		{
			//更新语音识别灵敏度阈值
			speechCammandConfidenceBoundary=sharedPreferenceSetter.getSpeechConfidenceBoundary(this);
			speechRecognizer.startListening(recognizerListener);
			isSpeechServiceRunning=true;
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					speechButton.setBackgroundResource(R.drawable.mic_0);
				}
			});
			return true;
		}
		return false;
	}
	
	//重新开始聆听(不重建语法)
	private void reStartRecognition()
	{
		speechRecognizer.startListening(recognizerListener);
		isSpeechServiceRunning=true;
	}
	
	//开始TTS发音
	private boolean startSynthesize(String text)
	{
		if(isSpeechServiceInstall==false)
		{
			showToast(getString(R.string.speechServiceNotInstall));
			return false;
		}
		speechSynthesizer.startSpeaking(text, synthesizerListener);
		return true;
	}
	
	//停止聆听
	private void stopRecognition()
	{
		speechRecognizer.cancel(recognizerListener);
		isSpeechServiceRunning=false;
		runOnUiThread(new Runnable() {					
			@Override
			public void run() {
				// TODO Auto-generated method stub
				speechButton.setBackgroundResource(R.drawable.mic_off);
			}
		});
	}
	
	//停止TTS发音
	private void stopSynthesize()
	{
		speechSynthesizer.stopSpeaking(synthesizerListener);
	}
	
	//释放语音识别
	private void destroyRecognizer()
	{
		if(speechRecognizer!=null)
		{
			stopRecognition();
			speechRecognizer.destory();
		}
	}
	
	//释放语音合成
	private void destroySynthesizer()
	{
		if(speechSynthesizer!=null)
		{
			stopSynthesize();
			speechSynthesizer.destory();
		}
	}
	
	//语音识别回调
	private RecognizerListener recognizerListener=new RecognizerListener.Stub() {
		@Override
		public void onVolumeChanged(final int v) throws RemoteException {
			//录音音量回调(android volume范围为0-30)
			//showLOG("speech volume:"+v);
			runOnUiThread(new Runnable() {			
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(v==0)
					{
						speechButton.setBackgroundResource(R.drawable.mic_0);
					}
					else if(v>0 && v<10)
					{
						speechButton.setBackgroundResource(R.drawable.mic_1);
					}
					else if(v>10 && v<20)
					{
						speechButton.setBackgroundResource(R.drawable.mic_2);
					}
					else if(v>20)
					{
						speechButton.setBackgroundResource(R.drawable.mic_3);
					}
				}
			});
		}
		
		@Override
		public void onResult(final RecognizerResult result, boolean isLast) throws RemoteException {
			//识别结果回调(在线识别返回json，离线识别返回xml)
			String resultCommand=RecognizeResultXmlParser.getResult(result.getResultString());
			int resultConfidence=RecognizeResultXmlParser.getResultConfidence(result.getResultString());
			//showLOG(result.getResultString());
			//如果识别结果为空，可能是语音引擎没配置正确
			if(resultCommand.equals(""))
			{
				showLOG("recognize result empty");
				showSpeechCammandMessage((getString(R.string.recognizeResultEmptyString)));
				return;
			}
			if(resultConfidence>=speechCammandConfidenceBoundary)
			{
				//执行语音命令
				executeSpeechCommand(resultCommand);
				showLOG("recognize success:"+resultCommand+"  confidence:"+resultConfidence+"/"+speechCammandConfidenceBoundary+"  executed");
			}
			else
			{
				showLOG("recognize success:"+resultCommand+"  confidence:"+resultConfidence+"/"+speechCammandConfidenceBoundary+"  passed");
			}
			//说话结束之后继续监听
			if(isLast==true)
			{
				reStartRecognition();
			}
		}
		
		@Override
		public void onError(int errorCode) throws RemoteException {
			//识别错误回调
			//showLOG("recognize failed");
			//说话结束之后继续监听
			reStartRecognition();
		}
		
		@Override
		public void onEndOfSpeech() throws RemoteException {
			//录音结束回调
			
		}
		
		@Override
		public void onBeginOfSpeech() throws RemoteException {
			//录音开始回调
			
		}
	};
	
	//语音合成回调
	private SynthesizerListener synthesizerListener=new SynthesizerListener.Stub() {
		@Override
		public void onSpeakResumed() throws RemoteException {
			//重新播放回调
			
		}
		
		@Override
		public void onSpeakProgress(int progress) throws RemoteException {
			//播放进度回调
			
		}
		
		@Override
		public void onSpeakPaused() throws RemoteException {
			//暂停回调
			
		}
		
		@Override
		public void onSpeakBegin() throws RemoteException {
			//开始播放回调
			//停止语音识别，防止扬声器声音干扰麦克风采集
			stopRecognition();
		}
		
		@Override
		public void onCompleted(int code) throws RemoteException {
			//播放完成回调
			reStartRecognition();
		}
		
		@Override
		public void onBufferProgress(int progress) throws RemoteException {
			//缓冲进度回调
			
		}
	};
	
	 //检测手机中是否安装语音引擎
	 private boolean checkSpeechServiceInstall()
	 {
		 String packageName = "com.iflytek.speechcloud";
		 List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		 for(int i = 0; i < packages.size(); i++)
		 {
			 PackageInfo packageInfo = packages.get(i);
			 if(packageInfo.packageName.equals(packageName))
			 {
				 return true;
			 }
		 }
		 return false;
	 }
	
	//检测语音引擎状态
	private boolean checkSpeechEngineState()
	{
		if(checkSpeechServiceInstall()==false || SpeechUtility.getUtility(this).queryAvailableEngines()==null || SpeechUtility.getUtility(this).queryAvailableEngines().length<=0)
		{
			//未检测到语音引擎
			showLOG("recognizerEngine not installed");
			//弹窗提醒用户安装语音引擎
			DialogInterface.OnClickListener positiveButtonOnClickListener=new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					//先尝试本地安装,若不成功则转至网页安装
					if(SpeechEngineApkInstaller.installFromAssets(MainActivity.this)==true)
					{
						showLOG("recognizerEngine installing from local...");
					}
					else
					{
						showLOG("recognizerEngine install from local failed");
						SpeechEngineApkInstaller.openDownloadWeb(MainActivity.this, SPEECH_ENGINE_DOWNLOAD_URL);
					}
				}
			};
			new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.alertString)).setMessage(getString(R.string.speechEngineInstallMessageString)).setPositiveButton(getString(R.string.confirmString), positiveButtonOnClickListener).setNegativeButton(getString(R.string.cancelString), null).show();
			return false;
		}
		else
		{
			//语音引擎正常
			showLOG("recognizerEngine installed");
			//设置申请的appid
			SpeechUtility.getUtility(this).setAppid(VOICE_API_KEY);
			isSpeechServiceInstall=true;
			return true;
		}
	}
	
	//初始化DJI SDK
    private void onInitDjiSDK(){
        DJIDrone.initWithType(DJIDroneType.DJIDrone_Vision);
        DJIDrone.connectToDrone();
    }
	
	//激活DJI SDK(首次使用必须激活，否则无法使用)
	private void activateDJISDK()
	{
        new Thread()
        {
            public void run()
            {
                try
                {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGerneralListener()
                    {
                        @Override
                        public void onGetPermissionResult(int result)
                        {
                        	//result=0 is success
                            showLOG("DJI SDK checkPermission="+result);
                            if(result!=0)
                            {
                            	showCommonMessage(getString(R.string.djiSdkActivateError));
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                	showLOG("activateDJISDK() onError");
                    e.printStackTrace();
                }
            }
        }.start();
        onInitDjiSDK();
	}
    
	//检测wifi连接状态
	private boolean checkWifiConnection()
	{
		//检测Wifi连接状态
		//Wifi未打开
		if(WifiConnectionChecker.isWifiOpened(this)==false)
		{
			runOnUiThread(new Runnable() {			
				@Override
				public void run() {
					// TODO Auto-generated method stub
					connectedWifiSSIDTextView.setText(getString(R.string.wifiClosed)+"\n"+getString(R.string.pleaseConnectToPhantom));
				}
			});
		}
		//Wifi已打开但未连接
		else if(WifiConnectionChecker.isWifiConnected(this)==false)
		{
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					connectedWifiSSIDTextView.setText(getString(R.string.wifiOpendButNoConnection)+"\n"+getString(R.string.pleaseConnectToPhantom));
				}
			});
		}
		//Wifi已连接
		else
		{
			wifiSSID=WifiConnectionChecker.getWifiSSID(this);
			//未连接phantom的wifi
			Locale locale=Locale.getDefault();
			if(wifiSSID.toLowerCase(locale).contains("phantom")==false)
			{
				runOnUiThread(new Runnable() {				
					@Override
					public void run() {
						// TODO Auto-generated method stub
						connectedWifiSSIDTextView.setText(getString(R.string.connectedWifiSSIDString)+wifiSSID+"\n"+getString(R.string.pleaseConnectToPhantom));
					}
				});
			}
			//成功连接至phantom wifi
			else
			{
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						connectedWifiSSIDTextView.setText(getString(R.string.wifiConnected)+wifiSSID);
					}
				});
				return true;
			}
		}
		return false;
	}
	
    //检测相机连接状态
	private Timer checkCameraConnectionTimer = new Timer();
	class CheckCameraConnectionTask extends TimerTask
	{
		@Override
		public void run()
		{
			if(checkCameraConnectState()==true)
			{
				runOnUiThread(new Runnable() {
					public void run() {
						connectedWifiSSIDTextView.setText(getString(R.string.wifiConnected)+wifiSSID);
						startButton.setBackgroundResource(R.drawable.start_green);
						startButton.setClickable(true);
						wifiButton.setBackgroundResource(R.drawable.wifi_on);
					}
				});
			}
			else
			{
				if(isCameraConnected)
				{
					showCommonMessage(getString(R.string.loseCameraConnection));
				}
				runOnUiThread(new Runnable() {
					public void run() {
						connectedWifiSSIDTextView.setText(getString(R.string.cameraConnecting));
						startButton.setBackgroundResource(R.drawable.start_gray);
						startButton.setClickable(false);
						wifiButton.setBackgroundResource(R.drawable.wifi_off);
					}
				});
			}
		}
	}
    private boolean checkCameraConnectState(){
        boolean cameraConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
        if(cameraConnectState)
        {
        	//showLOG("DJI Camera connect ok");
        	return true;
        }
        else
        {
        	//showLOG("DJI Camera connect failed");
        	return false;
        }
    }
    
    //初始化相机
    private void initCamera()
    {	
    	//云台俯仰行程
    	phantom_gimbal_min_angle=DJIDrone.getDjiGimbal().getGimbalPitchMinAngle();
    	phantom_gimbal_max_angle=DJIDrone.getDjiGimbal().getGimbalPitchMaxAngle();
        //设置相机参数
    	setCameraParameter();
    	//mDjiGLSurfaceView start decode,feed me
    	mDjiGLSurfaceView.start();
    	mReceivedVideoDataCallBack=new DJIReceivedVideoDataCallBack() {
			@Override
			public void onResult(byte[] videoBuffer,int size) {
				// TODO Auto-generated method stub
				//showLOG("videoData received");
				//showLOG(videoBuffer.toString()+"  "+size);
				mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
			}
		};
        
        //云台姿态调整回调
        mGimbalUpdateAttitudeCallBack = new DJIGimbalUpdateAttitudeCallBack(){
            @Override
            public void onResult(DJIGimbalAttitude attitude) {
                // TODO Auto-generated method stub
            	//showLOG(attitude.toString());
            	currentGimbalPitchAngle=(int)attitude.pitch;
            }
        };
        
        //云台错误回调
        mGimbalErrorCallBack = new DJIGimbalErrorCallBack(){
            @Override
            public void onError(int error) {
                // TODO Auto-generated method stub
            	//showLOG("gimbal onError");
            	if(error!=DJIError.RESULT_OK)
            	{
                	showCommonMessage(getString(R.string.gimbalError));
            	}
            }            
        };
        
        //电池信息回调
        mBattryUpdateInfoCallBack=new DJIBattryUpdateInfoCallBack() {
			@Override
			public void onResult(DJIBatteryProperty property) {
				// TODO Auto-generated method stub
				//showLOG(property.toString());
				remainPowerPercent=property.remainPowerPercent;
				//低电压报警
				if(isLowPowerWarning==false && remainPowerPercent<=LOWPOWER_WARNING_PERCENT)
				{
					//showLOG("remainPowerPercent less than "+LOWPOWER_WARNING_PERCENT+"%");
					showCommonMessage(getString(R.string.battery)+getString(R.string.lowThan)+LOWPOWER_WARNING_PERCENT+"%");
					startSynthesize(getString(R.string.battery)+getString(R.string.lowThan)+LOWPOWER_WARNING_PERCENT+"%");
					isLowPowerWarning=true;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						remainPowerPercentTextView.setText(getString(R.string.remainPowerPercentString)+remainPowerPercent+"%");
					}
				});
			}
		};
		
		//MC状态回调
		mMcuUpdateStateCallBack=new DJIMcuUpdateStateCallBack() {			
			@Override
			public void onResult(DJIMainControllerSystemState state) {
				// TODO Auto-generated method stub
				//showLOG(state.toString());
				//showLOG("MC onResult");
				DecimalFormat df=new DecimalFormat("#.0");  //保留小数点后一位
				
				satelliteCount=(int)state.satelliteCount;
				altitude=Double.parseDouble(df.format(state.altitude));
				speed=Double.parseDouble(df.format(state.speed));
				homeLocationLatitude=state.homeLocationLatitude;
				homeLocationLongitude=state.homeLocationLongitude;
				phantomLocationLatitude=state.phantomLocationLatitude;
				phantomLocationLongitude=state.phantomLocationLongitude;
				//计算gps距离
				distance=Double.parseDouble(df.format(GPSdistanceCalculator.getDistance(homeLocationLatitude, homeLocationLongitude, phantomLocationLatitude, phantomLocationLongitude)));
				
				runOnUiThread(new Runnable() {
					public void run() {
						satelliteCountTextView.setText(getString(R.string.satelliteCountString)+satelliteCount);
						altitudeTextView.setText(getString(R.string.altitudeString)+altitude+"m");
						speedTextView.setText(getString(R.string.speedString)+speed+"m/s");
						//showLOG("phantom gps location:"+phantomLocationLatitude+","+phantomLocationLongitude);
						//gps未定位成功时距离显示为N/A
						if(phantomLocationLatitude!=0.0 && phantomLocationLongitude!=0.0)
						{
							distanceTextView.setText(getString(R.string.distanceString)+distance+"m");
						}
						else
						{
							distanceTextView.setText(getString(R.string.distanceString)+"N/A");
						}
					}
				});
				//计算AAT角度(条件：AAT已连接、GPS定位成功)
				if(isBlunoConnect && phantomLocationLatitude!=0.0 && phantomLocationLongitude!=0.0)
				{
					//第一版：GPS相关计算在手机端处理
					/*
					yawAngle=(int)GPSdistanceCalculator.getDirection(homeLocationLatitude, homeLocationLongitude, phantomLocationLatitude, phantomLocationLongitude);
					//atan2:distance=0 is ok
					pitchAngle=(int)(Math.atan2(altitude, distance)/Math.PI*180);
					//showLOG("AAT:yaw "+yawAngle+" pitch "+pitchAngle);
					serialSend("<y"+yawAngle+"p"+pitchAngle+">");
					*/
					
					//2015.3.16 升级版：GPS相关计算在天线自动追踪系统里处理
					serialSend("<a"+homeLocationLatitude+"b"+homeLocationLongitude+"c"+phantomLocationLatitude+"d"+phantomLocationLongitude+"e"+altitude+">");
					showLOG("serialSend:"+homeLocationLatitude+" "+homeLocationLongitude+" "+phantomLocationLatitude+" "+phantomLocationLongitude+" "+altitude);
				}
			}
		};
		
		//MC错误回调
		mMcuErrorCallBack=new DJIMcuErrorCallBack() {			
			@Override
			public void onError(DJIMCUErrorType errorType) {
				// TODO Auto-generated method stub
				if(errorType!=DJIMCUErrorType.MCU_NO_ERROR)
				{
					showCommonMessage(getString(R.string.mcError));
				}
			}
		};
		
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);
        DJIDrone.getDjiGimbal().setGimbalUpdateAttitudeCallBack(mGimbalUpdateAttitudeCallBack);
        DJIDrone.getDjiGimbal().setGimbalErrorCallBack(mGimbalErrorCallBack);
        DJIDrone.getDjiBattery().setBattryUpdateInfoCallBack(mBattryUpdateInfoCallBack);
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mMcuUpdateStateCallBack);
        DJIDrone.getDjiMC().setMcuErrorCallBack(mMcuErrorCallBack);        
        //启动回调intervel(未找到文档,测试最低为1000)
        DJIDrone.getDjiGimbal().startUpdateTimer(1000);
        DJIDrone.getDjiBattery().startUpdateTimer(2000);
        DJIDrone.getDjiMC().startUpdateTimer(1000);
        
        isCameraConnected=true;
    }
    
    //设置相机参数(预览画质、fpv模式来自SharedPreferences)
    private void setCameraParameter()
    {
		//读取预览画质
		String cameraPreviewResolustionType=sharedPreferenceSetter.getCameraPreviewResolustionType(this);
		if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_320x240_15fpsString)))
		{
			mDjiGLSurfaceView.setStreamType(CameraPreviewResolustionType.Resolution_Type_320x240_15fps);
		}
		else if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_320x240_30fpsString)))
		{
			mDjiGLSurfaceView.setStreamType(CameraPreviewResolustionType.Resolution_Type_320x240_30fps);
		}
		else if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_640x480_15fpsString)))
		{
			mDjiGLSurfaceView.setStreamType(CameraPreviewResolustionType.Resolution_Type_640x480_15fps);
		}
		else if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_640x480_30fpsString)))
		{
			mDjiGLSurfaceView.setStreamType(CameraPreviewResolustionType.Resolution_Type_640x480_30fps);
		}
		//读取fpv模式
		boolean fpvMode=sharedPreferenceSetter.getFpvMode(this);
		if(fpvMode==true)
		{
			DJIDrone.getDjiGimbal().setGimbalFpvMode(true);
		}
		else
		{
			DJIDrone.getDjiGimbal().setGimbalFpvMode(false);
		}
    }
    
    //设置云台俯仰角度
    private void setGimbalPitchAngle(final int angle)
    {
        new Thread()
        {
            public void run()
            {
            	int tempAngle=angle;
            	//避免数值越界
            	if(tempAngle<phantom_gimbal_min_angle)
            	{
            		tempAngle=phantom_gimbal_min_angle;
            	}
            	if(tempAngle>phantom_gimbal_max_angle)
            	{
            		tempAngle=phantom_gimbal_max_angle;
            	}
                DJIGimbalRotation mPitch = new DJIGimbalRotation(true, false,true, tempAngle);             
                DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch,null,null);
            }
        }.start();
    }
    
    //释放相机
    private void destroyCamera()
    {
    	if(isCameraConnected==true)
    	{
        	DJIDrone.getDjiGimbal().stopUpdateTimer();
        	DJIDrone.getDjiBattery().stopUpdateTimer();
        	DJIDrone.getDjiMC().stopUpdateTimer();
            mDjiGLSurfaceView.destroy();
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
            DJIDrone.getDjiGimbal().setGimbalUpdateAttitudeCallBack(null);
            DJIDrone.getDjiGimbal().setGimbalErrorCallBack(null);
            DJIDrone.getDjiBattery().setBattryUpdateInfoCallBack(null);
            DJIDrone.getDjiMC().setMcuUpdateStateCallBack(null);
            DJIDrone.getDjiMC().setMcuErrorCallBack(null);
            isCameraConnected=false;
    	}
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置标题栏（无标题）  //config in AndroidManifest.xml
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        sharedPreferenceSetter.initSharedPreferences(this);
        initUIControls();
        activateDJISDK();
        if(checkWifiConnection()==true)
        {
            //每隔5秒检测一次相机连接状态
            checkCameraConnectionTimer.schedule(new CheckCameraConnectionTask(), 1000, 5000);
        }
        if(checkSpeechEngineState()==true)
        {
            initRecognizer();
            initSynthesizer();
            runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					speechButton.setClickable(true);
				}
			});
        }
        initBluetooth();
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		onResumeProcess();  //onResume Process by BlunoLibrary
	}
    
//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//		onPauseProcess();  //onPause Process by BlunoLibrary
//	}

//	@Override
//	protected void onStop() {
//		// TODO Auto-generated method stub
//		super.onStop();
//		onStopProcess();  //onStop Process by BlunoLibrary
//	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		showLOG("MainActivity onDestroy()");
		destroyRecognizer();
		destroySynthesizer();
		checkCameraConnectionTimer.cancel();
		destroyCamera();
        DJIDrone.disconnectToDrone();
        //2014.12.14 solved
		//evan2048 Problem:加了DJIDrone.disconnectToDrone();后退出程序时出现以下错误:
		/*
		 * 12-14 00:51:20.300: E/AndroidRuntime(15477): FATAL EXCEPTION: Timer-8
           12-14 00:51:20.300: E/AndroidRuntime(15477): Process: com.evan2048.phantom_voicecontrol, PID: 15477
           12-14 00:51:20.300: E/AndroidRuntime(15477): java.lang.NullPointerException
           12-14 00:51:20.300: E/AndroidRuntime(15477): 	at dji.sdk.tcp.Queue.getMeg(Queue.java:54)
           12-14 00:51:20.300: E/AndroidRuntime(15477): 	at dji.sdk.tcp.vision.VisionCmd.block_GetResponse(VisionCmd.java:2679)
           12-14 00:51:20.300: E/AndroidRuntime(15477): 	at dji.sdk.tcp.vision.VisionCmd.synSendCmd(VisionCmd.java:2615)
           12-14 00:51:20.300: E/AndroidRuntime(15477): 	at dji.sdk.tcp.vision.VisionCmd.access$0(VisionCmd.java:2602)
           12-14 00:51:20.300: E/AndroidRuntime(15477): 	at dji.sdk.tcp.vision.VisionCmd$1.run(VisionCmd.java:189)
           12-14 00:51:20.300: E/AndroidRuntime(15477): 	at java.util.Timer$TimerImpl.run(Timer.java:284)
		 * 
		 */
		super.onDestroy();
		onDestroyProcess();  //onDestroy Process by BlunoLibrary
		//evan2048 DJIDrone.disconnectToDrone() Problem solved:
		Process.killProcess(Process.myPid());
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	//按两次返回键退出
	private static boolean needPressAgain = false;
	private Timer ExitTimer = new Timer();
	class ExitCleanTask extends TimerTask
	{
		@Override
		public void run()
		{               
			needPressAgain = false;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (needPressAgain) {
            	needPressAgain = false;
                finish();
            } 
            else 
            {
            	needPressAgain = true;
            	showToast(getString(R.string.pressAgainExitString));
                ExitTimer.schedule(new ExitCleanTask(), 2000);
            }
            return true;
        }
		
		return super.onKeyDown(keyCode, event);
	}
	
	//读取设置界面设置
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//from SettingActivity
		if(requestCode==SETTINGACTIVITY_REQUESTCODE && resultCode==Activity.RESULT_OK)
		{
			//更新语音识别灵敏度
			speechCammandConfidenceBoundary=sharedPreferenceSetter.getSpeechConfidenceBoundary(this);
			showLOG("speechConfidenceBoundary change to "+speechCammandConfidenceBoundary);
			//更新预览画质、fpv模式
			if(isCameraConnected)
			{
				setCameraParameter();
			}
			//更新3D眼镜模式
			if(isThreeDGlassMode!=sharedPreferenceSetter.getThreeDGlassMode(this))
			{
				isThreeDGlassMode=!isThreeDGlassMode;
				setThreeDGlassMode(isThreeDGlassMode);
			}
			
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					settingButton.setBackgroundResource(R.drawable.setting);
				}
			});
		}
		
		onActivityResultProcess(requestCode, resultCode, data);  //onActivityResult Process by BlunoLibrary
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//init bluetooth
	private void initBluetooth()
	{
        onCreateProcess();  //onCreate Process by BlunoLibrary
        serialBegin(115200);  //set the Uart Baudrate on BLE chip to 115200
	}
	
	//Once bluetooth connection state changes, this function will be called
	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState)
	{
		// TODO Auto-generated method stub
		switch (theConnectionState) {
		case isConnected:
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					bluetoothButton.setBackgroundResource(R.drawable.bluetooth_on);
				}
			});
			isBlunoConnect=true;
			break;
		case isConnecting:
			
			break;
		case isToScan:
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					bluetoothButton.setBackgroundResource(R.drawable.bluetooth_off);
				}
			});
			isBlunoConnect=false;
			break;
		case isScanning:
			
			break;
		case isDisconnecting:
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					bluetoothButton.setBackgroundResource(R.drawable.bluetooth_off);
				}
			});
			isBlunoConnect=false;
			break;
		default:
			break;
		}
	}
	
	//Once bluetooth connection data received, this function will be called
	@Override
	public void onSerialReceived(String theString) {
		// TODO Auto-generated method stub
		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		
	}
	
}
