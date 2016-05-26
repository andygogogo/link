package com.example.smartlink;

import com.example.smartlink.ui.CustomDialog;
import com.example.smartlink.ui.CustomTextView;
import com.natasa.progressviews.CircleProgressBar;
import com.natasa.progressviews.utils.OnProgressViewListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 发送声波
 * 
 * @author Andy
 *
 */
@SuppressLint("ResourceAsColor")
public class SoundWaveActivity extends Activity implements OnClickListener {

	/**
	 * 点击发送声波的红色字
	 */
	private TextView tvSound;

	/**
	 * 发送完毕，等待摄像机发出
	 */
	private TextView tvWaitCamera;
	/**
	 * 如果没有听到提示，请再次发送声波
	 */
	private TextView tvSendAgain;

	/**
	 * 下一步按钮
	 */
	private Button btnSoundWaveNext;

	/**
	 * 返回按钮
	 */
	private Button btnSoundWaveBack;

	/**
	 * 没有听到接受成功
	 */
	private CustomTextView noSuccess;

	/**
	 * 发送声波图片
	 */
	private ImageView ivSoundWave;

	/**
	 * 圆形进度条
	 */
	private CircleProgressBar circleProgressBar;

	/**
	 * 圆形进度条的进度指示
	 */
	protected int progress;
	private Handler mHandler;
	private Runnable mTimer;
	private boolean soundWave = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_wave);
		mHandler = new Handler();

		tvSound = (TextView) findViewById(R.id.tvSound);
		btnSoundWaveNext = (Button) findViewById(R.id.btnSoundWaveNext);
		btnSoundWaveBack = (Button) findViewById(R.id.btnSoundWaveBack);
		noSuccess = (CustomTextView) findViewById(R.id.noSuccess);
		ivSoundWave = (ImageView) findViewById(R.id.ivSoundWave);
		tvWaitCamera = (TextView) findViewById(R.id.tvWaitCamera);
		tvSendAgain = (TextView) findViewById(R.id.tvSendAgain);

		// 点击发送声波按钮，发送声波为红色字
		tvSound.setText(Html.fromHtml(getString(R.string.send_sound_wave)));
		tvWaitCamera.setText(getString(R.string.conn_wifi));

		btnSoundWaveBack.setOnClickListener(this);
		btnSoundWaveNext.setOnClickListener(this);
		noSuccess.setOnClickListener(this);
		ivSoundWave.setOnClickListener(this);
		initCircleProgressBar();

	}

	/**
	 * 初始化圆形进度条
	 */
	private void initCircleProgressBar() {
		circleProgressBar = (CircleProgressBar) findViewById(R.id.circle_progress);
		circleProgressBar.setRoundEdgeProgress(true);
		circleProgressBar.setTextSize(52);
		circleProgressBar.setStartPositionInDegrees(0);
		circleProgressBar.setOnProgressViewListener(new OnProgressViewListener() {
			@Override
			public void onFinish() {
				// do something on progress finish
				// circleProgressBar.setText("done!");
				ivSoundWave.setImageResource(R.drawable.send_sound);
				// circleProgressBar.resetProgressBar();
				btnSoundWaveNext.setEnabled(true);
				btnSoundWaveNext.setBackgroundResource(R.color.next_button_normal);
				ivSoundWave.setImageResource(R.drawable.send_sound);

				tvSound.setText("发送完毕，等待摄像机发出");
				tvWaitCamera.setVisibility(View.VISIBLE);
				tvWaitCamera.setText(Html.fromHtml(getString(R.string.success_prompt)));
				tvSendAgain.setVisibility(View.VISIBLE);
				waiting(false);
			}

			@SuppressLint("NewApi")
			@Override
			public void onProgressUpdate(float progress) {
				circleProgressBar.setText("" + (int) progress);
			}
		});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btnSoundWaveBack:
			finish();
			break;
		case R.id.btnSoundWaveNext:
			startActivity(new Intent(SoundWaveActivity.this, ConnectingActivity.class));
			break;
		case R.id.noSuccess:
			Dialog dialog = new CustomDialog(this, "没有听到\"接受成功\"？", R.drawable.no_receive_success);
			dialog.show();
			break;
		// 点击发送声波图片
		case R.id.ivSoundWave:
			tvSendAgain.setVisibility(View.INVISIBLE);
			tvWaitCamera.setVisibility(View.INVISIBLE);
			tvSound.setText("正在发送消息...");
			ivSoundWave.setImageResource(R.drawable.wave_sending);
			btnSoundWaveNext.setEnabled(false);
			btnSoundWaveNext.setBackgroundResource(R.color.next_button_unable);
			progress = 0;
			if (soundWave) {
				setTimer();
				soundWave = false;
			}
			waiting(true);
			break;
		default:
			break;
		}

	}

	/**
	 * 进度条读取中控件不可点击
	 * 
	 * @param isWaiting
	 */
	private void waiting(boolean isWaiting) {
		btnSoundWaveNext.setClickable(!isWaiting);
		btnSoundWaveBack.setClickable(!isWaiting);
		ivSoundWave.setClickable(!isWaiting);
		noSuccess.setClickable(!isWaiting);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mTimer);
	}

	/**
	 * 圆形进度条时间控制
	 */
	private void setTimer() {
		mTimer = new Runnable() {
			@Override
			public void run() {
				progress += 1;
				if (progress <= 100)
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							circleProgressBar.setProgress(progress);
						}
					});

				mHandler.postDelayed(this, 100);
			}
		};

		mHandler.postDelayed(mTimer, 1000);

	}
}
