package com.example.smartlink;

import com.example.smartlink.ui.CustomDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * 正在连接摄像机
 * 
 * @author Andy
 *
 */
public class ConnectingActivity extends Activity {

	/**
	 * 后台倒计时处理用到的handler和runnable
	 */
	private Handler mHandler;
	private Runnable mTimer;

	/**
	 * 倒计时秒
	 */
	private int count = 60;

	/**
	 * 显示倒计时的数字
	 */
	private TextView tvCounting;

	/**
	 * 用户点击返回键或者倒计时为0时提示对话框
	 */
	private Dialog dialog;
	
	private Button sendBroadcast;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
//		final SendBroadcastActivity s = new SendBroadcastActivity();
		setContentView(R.layout.activity_connecting);
		mHandler = new Handler();
		tvCounting = (TextView) findViewById(R.id.tvCounting);
		sendBroadcast = (Button) findViewById(R.id.button1);
		
		
		sendBroadcast.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				count = -1;
				startActivity(new Intent(ConnectingActivity.this, SendBroadcastActivity.class));
			}
		});
		setTimer();
	}

	/**
	 * 点击返回键处理
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			dialog = new CustomDialog(this, CustomDialog.DIALOG_EXIT_ADD, null);
			dialog.setCancelable(false);
			dialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 点击弹出的对话框的按钮处理
	 * 
	 * @param v
	 */
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		// 倒计时页面，点击返回键，点击退出
		case R.id.btnExit:
			// 停止倒计时
			count = -1;
			// 停止发送组播
			SnifferSmartLinker.getInstance().stop();
			finish();
			startActivity(new Intent(ConnectingActivity.this, MainActivity.class));
			break;
		// 倒计时完毕，点击退出
		case R.id.btnExitProblem:
			count = -1;
			SnifferSmartLinker.getInstance().stop();
			finish();
			startActivity(new Intent(ConnectingActivity.this, MainActivity.class));
			break;
		// 倒计时完毕，点击重新添加
		case R.id.btnAddAgain:
			count = -1;
			SnifferSmartLinker.getInstance().stop();
			finish();
			startActivity(new Intent(ConnectingActivity.this, CheckSoundActivity.class));
			break;
		// 正在倒计时时，点击返回键，点击继续等待
		case R.id.btnContinueWait:
			dialog.dismiss();
			break;
		default:
			break;
		}

	}

	/**
	 * 开启倒计时
	 */
	private void setTimer() {
		mTimer = new Runnable() {
			
			@Override
			public void run() {
				count--;
				if (count > -1) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							tvCounting.setText(count + "秒后将完成连接");
							if(ConstantValue.ip.length() > 0 ){
								sendBroadcast.setVisibility(View.VISIBLE);
							}
							if (count == 0) {
								dialog = new CustomDialog(ConnectingActivity.this, CustomDialog.DIALOG_PROBLEM, null);
								dialog.setCancelable(false);
								dialog.show();
							}

						}
					});
				}

				mHandler.postDelayed(this, 1000);
			}
		};

		mHandler.postDelayed(mTimer, 1000);

	}

}
