package com.evan2048.phantom_voicecontrol;

import com.evan2048.util.SharedPreferenceSetter;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnClickListener {

	private static final String TAG="SettingActivity";
	private SharedPreferenceSetter sharedPreferenceSetter=new SharedPreferenceSetter();
	
	//打印调试信息
	private void showLOG(String str)
	{
		Log.e(TAG, str);
	}
	
	//UI界面初始化
	private Button backButton;	
	private RadioButton resolution_type_320x240_15fpsRadioButton;
	private RadioButton resolution_type_320x240_30fpsRadioButton;
	private RadioButton resolution_type_640x480_15fpsRadioButton;
	private RadioButton resolution_type_640x480_30fpsRadioButton;
	private CheckBox fpvModeCheckBox;
	private SeekBar speechConfidenceBoundarySeekBar;
	private TextView speechConfidenceBoundaryTextView;
	private CheckBox threeDGlassModeCheckBox;
	private Button resetToDefaultSettingButton;
	private void initUIControls()
	{
		backButton=(Button)findViewById(R.id.backButton);
		resolution_type_320x240_15fpsRadioButton=(RadioButton)findViewById(R.id.resolution_type_320x240_15fpsRadioButton);
		resolution_type_320x240_30fpsRadioButton=(RadioButton)findViewById(R.id.resolution_type_320x240_30fpsRadioButton);
		resolution_type_640x480_15fpsRadioButton=(RadioButton)findViewById(R.id.resolution_type_640x480_15fpsRadioButton);
		resolution_type_640x480_30fpsRadioButton=(RadioButton)findViewById(R.id.resolution_type_640x480_30fpsRadioButton);
		fpvModeCheckBox=(CheckBox)findViewById(R.id.fpvModeCheckBox);
		speechConfidenceBoundarySeekBar=(SeekBar)findViewById(R.id.speechConfidenceBoundarySeekBar);
		speechConfidenceBoundaryTextView=(TextView)findViewById(R.id.speechConfidenceBoundaryTextView);
		threeDGlassModeCheckBox=(CheckBox)findViewById(R.id.threeDGlassModeCheckBox);
		resetToDefaultSettingButton=(Button)findViewById(R.id.resetToDefaultSettingButton);
		//set OnClickListener
		backButton.setOnClickListener(this);
		resolution_type_320x240_15fpsRadioButton.setOnClickListener(this);
		resolution_type_320x240_30fpsRadioButton.setOnClickListener(this);
		resolution_type_640x480_15fpsRadioButton.setOnClickListener(this);
		resolution_type_640x480_30fpsRadioButton.setOnClickListener(this);
		fpvModeCheckBox.setOnClickListener(this);
		threeDGlassModeCheckBox.setOnClickListener(this);
		resetToDefaultSettingButton.setOnClickListener(this);
		//update seekbar value
		speechConfidenceBoundarySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				sharedPreferenceSetter.setSpeechConfidenceBoundary(seekBar.getProgress(),SettingActivity.this);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				speechConfidenceBoundaryTextView.setText(getString(R.string.speechConfidenceBoundaryString)+progress+"%");
			}
		});
		
		getSharedPreferences();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		//返回按键
		case R.id.backButton:
		{
			backButton.setBackgroundResource(R.drawable.back_button_press);
			setResult(Activity.RESULT_OK);
			finish();
			break;
		}
		case R.id.resolution_type_320x240_15fpsRadioButton:
		{
			sharedPreferenceSetter.setCameraPreviewResolustionType(getString(R.string.resolution_type_320x240_15fpsString),SettingActivity.this);
			break;
		}
		case R.id.resolution_type_320x240_30fpsRadioButton:
		{
			sharedPreferenceSetter.setCameraPreviewResolustionType(getString(R.string.resolution_type_320x240_30fpsString),SettingActivity.this);
			break;
		}
		case R.id.resolution_type_640x480_15fpsRadioButton:
		{
			sharedPreferenceSetter.setCameraPreviewResolustionType(getString(R.string.resolution_type_640x480_15fpsString),SettingActivity.this);
			break;
		}
		case R.id.resolution_type_640x480_30fpsRadioButton:
		{
			sharedPreferenceSetter.setCameraPreviewResolustionType(getString(R.string.resolution_type_640x480_30fpsString),SettingActivity.this);
			break;
		}
		case R.id.fpvModeCheckBox:
		{
			if(fpvModeCheckBox.isChecked())
			{
				sharedPreferenceSetter.setFpvMode(true,SettingActivity.this);
			}
			else
			{
				sharedPreferenceSetter.setFpvMode(false,SettingActivity.this);
			}
			break;
		}
		case R.id.threeDGlassModeCheckBox:
		{
			if(threeDGlassModeCheckBox.isChecked())
			{
				sharedPreferenceSetter.setThreeDGlassMode(true,SettingActivity.this);
			}
			else
			{
				sharedPreferenceSetter.setThreeDGlassMode(false,SettingActivity.this);
			}
			break;
		}
		case R.id.resetToDefaultSettingButton:
		{
			sharedPreferenceSetter.setCameraPreviewResolustionType(getString(R.string.resolution_type_320x240_15fpsString),SettingActivity.this);
			sharedPreferenceSetter.setFpvMode(false,SettingActivity.this);
			sharedPreferenceSetter.setSpeechConfidenceBoundary(50,SettingActivity.this);
			sharedPreferenceSetter.setThreeDGlassMode(false, SettingActivity.this);
			//刷新界面
			getSharedPreferences();
		}
		default:
		{
			break;
		}
		}
	}
	
	//读取配置信息
	private void getSharedPreferences()
	{
		//读取预览画质
		String cameraPreviewResolustionType=sharedPreferenceSetter.getCameraPreviewResolustionType(this);
		if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_320x240_15fpsString)))
		{
			resolution_type_320x240_15fpsRadioButton.setChecked(true);
		}
		else if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_320x240_30fpsString)))
		{
			resolution_type_320x240_30fpsRadioButton.setChecked(true);
		}
		else if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_640x480_15fpsString)))
		{
			resolution_type_640x480_15fpsRadioButton.setChecked(true);
		}
		else if(cameraPreviewResolustionType.equals(getString(R.string.resolution_type_640x480_30fpsString)))
		{
			resolution_type_640x480_30fpsRadioButton.setChecked(true);
		}
		//读取fpv模式
		boolean fpvMode=sharedPreferenceSetter.getFpvMode(this);
		if(fpvMode==true)
		{
			fpvModeCheckBox.setChecked(true);
		}
		else
		{
			fpvModeCheckBox.setChecked(false);
		}
		//读取3D眼镜模式
		boolean threeDGlassMode=sharedPreferenceSetter.getThreeDGlassMode(this);
		if(threeDGlassMode==true)
		{
			threeDGlassModeCheckBox.setChecked(true);
		}
		else
		{
			threeDGlassModeCheckBox.setChecked(false);
		}
		//读取语音识别灵敏度阈值
		int speechConfidenceBoundary=sharedPreferenceSetter.getSpeechConfidenceBoundary(this);
		speechConfidenceBoundarySeekBar.setProgress(speechConfidenceBoundary);
		speechConfidenceBoundaryTextView.setText(getString(R.string.speechConfidenceBoundaryString)+speechConfidenceBoundary+"%");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        //设置标题栏（无标题）  //config in AndroidManifest.xml
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);
        
        sharedPreferenceSetter.initSharedPreferences(SettingActivity.this);
        initUIControls();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub		
		super.onDestroy();
	}

	//重写 返回键按键
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		showLOG("SettingActivity finished by onBackPressed() method");
		setResult(Activity.RESULT_OK);
		
		super.onBackPressed();
	}
	
}
