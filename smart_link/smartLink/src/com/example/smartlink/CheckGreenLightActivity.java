/**
 * 
 */
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
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @author Andy 检查绿灯是否已经闪烁
 */
@SuppressLint("NewApi")
public class CheckGreenLightActivity extends Activity implements OnClickListener {

	/**
	 * 绿灯没有闪烁
	 */
	private CustomTextView noSplash;
	/**
	 * 返回按钮
	 */
	private Button back;

	/**
	 * 闪烁绿点图片
	 */
	private ImageView blueDot;

	/**
	 * 绿灯已经闪烁按钮
	 */
	private Button splashed;

	/**
	 * 声波添加设备
	 */
	private CustomTextView addBySound;
	
	/**
	 * 播放声音
	 */
	private MediaPlayer mp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.check_green_light);
		initView();
		
	}

	/**
	 * 播放语音
	 */
	private void playMySound() {
		mp = MediaPlayer.create(this, R.raw.check_green_light);
		try {
			mp.prepare();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		mp.start();
	}
	
	
	@Override
	protected void onPause() {
		mp.stop();
		super.onPause();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		back = (Button) findViewById(R.id.back);
		noSplash = (CustomTextView) findViewById(R.id.noSplash);
		blueDot = (ImageView) findViewById(R.id.blueDot);
		splashed = (Button) findViewById(R.id.splashedButton);
		setGreenDot();

		addBySound = (CustomTextView) findViewById(R.id.addBySound);
		back.setOnClickListener(this);
		noSplash.setOnClickListener(this);
		splashed.setOnClickListener(this);
		addBySound.setOnClickListener(this);
		
		playMySound();
	}

	/**
	 * 设置闪烁的绿点动画效果
	 */
	private void setGreenDot() {
		// 创建一个AnimationSet对象，参数为Boolean型，
		// true表示使用Animation的interpolator，false则是使用自己的
		AnimationSet animationSet = new AnimationSet(true);
		// 创建一个AlphaAnimation对象，参数从完全的透明度，到完全的不透明
		AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
		// 设置动画执行的时间
		alphaAnimation.setDuration(1000);
		alphaAnimation.setRepeatCount(Integer.MAX_VALUE);
		// 将alphaAnimation对象添加到AnimationSet当中
		animationSet.addAnimation(alphaAnimation);
		// 使用ImageView的startAnimation方法执行动画
		blueDot.startAnimation(animationSet);
	}

	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.noSplash:
			Dialog dialog = new CustomDialog(this, "绿灯没有闪烁？", R.drawable.green_light_no_splash4);
			dialog.show();
			break;
		case R.id.splashedButton:
			// 跳转到声音检测页面
			startActivity(new Intent(this, CheckSoundActivity.class));
			break;
		case R.id.addBySound:
			Toast.makeText(this, "正在建设中......", 1).show();
			break;
		default:
			break;
		}

	}

}
