package com.example.smartlink;

import com.example.smartlink.ui.CustomDialog;
import com.example.smartlink.ui.CustomTextView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * @author Andy 检查用户是否听到提示音
 */
public class CheckSoundActivity extends Activity implements OnClickListener {
	/**
	 * 没有听到声音
	 */
	private CustomTextView noSound;
	/**
	 * 已经听到声音
	 */
	private Button heardSound;

	/**
	 * 返回箭头按钮
	 */
	private Button btnSoundBack;
	
	/**
	 * 播放声音
	 */
	private MediaPlayer mp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.check_sound);
		initView();
	}

	private void initView() {
		noSound = (CustomTextView) findViewById(R.id.noSound);
		heardSound = (Button) findViewById(R.id.heardSound);
		btnSoundBack = (Button) findViewById(R.id.btnSoundBack);
		btnSoundBack.setOnClickListener(this);
		noSound.setOnClickListener(this);
		heardSound.setOnClickListener(this);
		playMySound();
	}
	
	
	/**
	 * 播放语音
	 */
	private void playMySound() {
		mp = MediaPlayer.create(this, R.raw.check_sound);
		try {
			mp.prepare();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		mp.start();
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSoundBack:
			finish();
			break;
		case R.id.noSound:
			Dialog dialog = new CustomDialog(this, "没有听到声音？", R.drawable.no_sound);
			dialog.show();
			break;
		case R.id.heardSound:
			startActivity(new Intent(this, RaiseVolumeActivity.class));
			break;
		default:
			break;
		}

	}
	
	@Override
	protected void onPause() {
		mp.stop();
		super.onPause();
	}
}
